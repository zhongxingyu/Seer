 package com.group7.dragonwars;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.json.JSONException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnShowListener;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Bundle;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.GestureDetector.OnDoubleTapListener;
 import android.view.GestureDetector.OnGestureListener;
 import android.view.MotionEvent;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.group7.dragonwars.engine.Building;
 import com.group7.dragonwars.engine.BitmapChanger;
 import com.group7.dragonwars.engine.DrawableMapObject;
 import com.group7.dragonwars.engine.Func;
 import com.group7.dragonwars.engine.GameField;
 import com.group7.dragonwars.engine.GameMap;
 import com.group7.dragonwars.engine.GameState;
 import com.group7.dragonwars.engine.Logic;
 import com.group7.dragonwars.engine.MapReader;
 import com.group7.dragonwars.engine.Pair;
 import com.group7.dragonwars.engine.Player;
 import com.group7.dragonwars.engine.Position;
 import com.group7.dragonwars.engine.Unit;
 
 public class GameActivity extends Activity {
     private static final String TAG = "GameActivity";
     private Integer orientation;
     private Boolean orientationChanged = false;
 
     @Override
     protected final void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // remove the title bar
         requestWindowFeature(Window.FEATURE_NO_TITLE);
 
         // remove the status bar
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                              WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
         Log.d(TAG, "in onCreate");
         setContentView(R.layout.activity_game);
         Log.v(null, "on inCreate");
 
         Button menuButton = (Button) this.findViewById(R.id.menuButton);
         GameView gameView = (GameView) this.findViewById(R.id.gameView);
         menuButton.setOnClickListener(gameView);
     }
 
 }
 
 class GameView extends SurfaceView implements SurfaceHolder.Callback,
                                               OnGestureListener,
                                               OnDoubleTapListener,
                                               DialogInterface.OnClickListener,
                                               OnClickListener {
     private final String TAG = "GameView";
 
     private final int tilesize = 64;
 
     private Bitmap bm;
     private GameState state;
     private Logic logic;
     private GameMap map;
     private Position selected; // Currently selected position
 
     private Position attack_location;
     private Position action_location;
 
     private FloatPair scrollOffset; // offset caused by scrolling, in pixels
     private GestureDetector gestureDetector;
 
     private DrawingThread dt;
     private Bitmap highlighter;
     private Bitmap selector;
     private Bitmap attack_highlighter;
 
     private Paint move_high_paint; // used to highlight movements
     private Paint attack_high_paint; // used to highlight possible attacks
 
     private Bitmap fullMap;
 
     private boolean unit_selected; // true if there is a unit at selection
 
     private boolean attack_action;
     /* true if during an attack action:
      * user selects a unit, selects a field
      * chooses attack
      **attack_move is now true
      * selects another field
      **attack_location is the location of the attack
      *
      */
 
     private Context context;
     private HashMap<String, HashMap<String, Bitmap>> graphics;
     private Integer orientation;
 
     private GameField lastField;
     private Unit lastUnit;
     private List<Position> lastDestinations;
 
     private Long timeElapsed = 0L;
     private Long framesSinceLastSecond = 0L;
     private Long timeNow = 0L;
     private Double fps = 0.0;
 
     private enum MenuType {NONE, ACTION, BUILD, MENU};
 
     private MenuType whichMenu;
 
     private DecimalFormat decformat;
 
     public GameView(final Context ctx, final AttributeSet attrset) {
         super(ctx, attrset);
         Log.d(TAG, "GameView ctor");
 
         GameView gameView = (GameView) this.findViewById(R.id.gameView);
         GameMap gm = null;
 
         //this.findViewById(R.id.menuButton).setOnClickListener(gameView);
 
         whichMenu = MenuType.NONE;
 
         Log.d(TAG, "nulling GameMap");
 
         try {
             gm = MapReader.readMap(readFile(R.raw.overmap)); // ugh
         } catch (JSONException e) {
             Log.d(TAG, "Failed to load the map: " + e.getMessage());
         }
 
         if (gm == null) {
             Log.d(TAG, "gm is null");
             System.exit(1);
         }
 
         Log.d(TAG, "before setMap");
         gameView.setMap(gm);
         this.logic = new Logic();
         this.state = new GameState(map, logic, map.getPlayers());
 
         context = ctx;
         bm = BitmapFactory.decodeResource(context.getResources(),
                                           R.drawable.ic_launcher);
         SurfaceHolder holder = getHolder();
         this.graphics = new HashMap<String, HashMap<String, Bitmap>>();
 
         /* Register game fields */
         putGroup("Fields", map.getGameFieldMap());
         putGroup("Units", map.getUnitMap());
         putGroup("Buildings", map.getBuildingMap());
         putResource("Misc", "flag", "drawable", "com.group7.dragonwars", "flag");
 
         loadBorders();
 
         /* Load selector and highlighters */
         selector = getResource("selector", "drawable",
                                "com.group7.dragonwars");
         highlighter = getResource("highlight", "drawable",
                                   "com.group7.dragonwars");
         attack_highlighter = getResource("attack_highlight", "drawable", "com.group7.dragonwars");
 
         holder.addCallback(this);
 
         selected = new Position(0, 0);
         action_location = new Position(0, 0);
 
         gestureDetector = new GestureDetector(this.getContext(), this);
         scrollOffset = new FloatPair(0f, 0f);
 
         move_high_paint = new Paint();
         move_high_paint.setStyle(Paint.Style.FILL);
         move_high_paint.setARGB(150, 0, 0, 255); // semi-transparent blue
 
         attack_high_paint = new Paint();
         attack_high_paint.setStyle(Paint.Style.FILL);
         attack_high_paint.setARGB(150, 255, 0, 0); // semi-transparent red
 
         decformat = new DecimalFormat("#.##");
 
         attack_action = false;
 
         /* Prerender combined map */
         fullMap = combineMap();
 
     }
 
     private <T extends DrawableMapObject>
                        void putGroup(final String category,
                                      final Map<Character, T> objMap) {
         graphics.put(category, new HashMap<String, Bitmap>());
 
         for (Map.Entry<Character, T> ent : objMap.entrySet()) {
             T f = ent.getValue();
             putResource(category, f.getSpriteLocation(), f.getSpriteDir(),
                         f.getSpritePack(), f.getName());
         }
     }
 
     private void putResource(final String category, final String resName,
                              final String resDir, final String resPack,
                              final String regName) {
         Bitmap bMap = getResource(resName, resDir, resPack);
        if (!graphics.contains(category)) {
             graphics.put(category, new HashMap<String, Bitmap>());
         }
 
         graphics.get(category).put(regName, bMap);
     }
 
     private Bitmap getResource(final String resName, final String resDir,
                                final String resPack) {
         Integer resourceID = getResources().getIdentifier(resName, resDir,
                                                           resPack);
         Bitmap bMap = BitmapFactory.decodeResource(context.getResources(),
                                                    resourceID);
 
         return bMap;
     }
 
     /* Helper for loadBorders() */
     private void loadField(final String resName, final String regName) {
         putResource("Fields", resName, "drawable",
                     "com.group7.dragonwars", regName);
     }
 
     /* TODO do not hardcode */
     private void loadBorders() {
         loadField("water_grass_edge1", "Top grass->water border");
         loadField("water_grass_edge2", "Right grass->water border");
         loadField("water_grass_edge3", "Bottom grass->water border");
         loadField("water_grass_edge4", "Left grass->water border");
 
         loadField("grass_water_corner1", "Water->grass corner NE");
         loadField("grass_water_corner2", "Water->grass corner SE");
         loadField("grass_water_corner3", "Water->grass corner SW");
         loadField("grass_water_corner4", "Water->grass corner NW");
 
         loadField("water_grass_corner1", "Grass->water corner SW");
         loadField("water_grass_corner2", "Grass->water corner NW");
         loadField("water_grass_corner3", "Grass->water corner NE");
         loadField("water_grass_corner4", "Grass->water corner SE");
     }
 
     /*
      * This method will combine the static part of the map into
      * a single bitmap. This should make it far faster to render
      * and prevent any tearing while scrolling the map
      */
     private Bitmap combineMap() {
         Bitmap result = null;
         Integer width = map.getWidth() * tilesize;
         Integer height = map.getHeight() * tilesize;
 
         result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 
         Canvas combined = new Canvas(result);
 
         for (int i = 0; i < map.getWidth(); ++i) {
             for (int j = 0; j < map.getHeight(); j++) {
                 Position pos = new Position(i, j);
                 GameField gf = map.getField(i, j);
                 String gfn = gf.getName();
                 RectF dest = getSquare(tilesize * i, tilesize * j, tilesize);
                 combined.drawBitmap(graphics.get("Fields").get(gfn),
                                     null, dest, null);
 
                 drawBorder(combined, pos, dest);
 
                 if (gf.hostsBuilding()) {
                     Building b = gf.getBuilding();
                     String n = b.getName();
                     combined.drawBitmap(graphics.get("Buildings").get(n),
                                         null, dest, null);
                 }
             }
         }
 
         return result;
     }
 
     private List<String> readFile(final int resourceid) {
         List<String> text = new ArrayList<String>();
 
         try {
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(this.getResources()
                                       .openRawResource(resourceid)));
             String line;
 
             while ((line = in.readLine()) != null) {
                 text.add(line);
             }
 
             in.close();
         } catch (FileNotFoundException fnf) {
             System.err.println("Couldn't find " + fnf.getMessage());
             System.exit(1);
         } catch (IOException ioe) {
             System.err.println("Couldn't read " + ioe.getMessage());
             System.exit(1);
         }
 
         return text;
     }
 
     public void setMap(final GameMap newmap) {
         this.map = newmap;
     }
 
     @Override
     public void surfaceChanged(final SurfaceHolder arg0, final int arg1,
                                final int arg2, final int arg3) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void surfaceCreated(final SurfaceHolder arg0) {
         dt = new DrawingThread(arg0, context, this);
         dt.setRunning(true);
         dt.start();
     }
 
     @Override
     public void surfaceDestroyed(final SurfaceHolder arg0) {
         dt.setRunning(false);
 
         try {
             dt.join();
         } catch (InterruptedException e) {
             // TODO something, perhaps, but what?
             return;
         }
     }
 
     @Override
     public boolean onTouchEvent(final MotionEvent event) {
         /* Functionality moved to onSingleTapConfirmed() */
         gestureDetector.onTouchEvent(event);
         return true;
     }
 
     public RectF getSquare(final float x, final float y, final float length) {
         return new RectF(x, y, x + length, y + length);
     }
 
     public List<Position> getUnitDestinations(final GameField selectedField) {
         List<Position> unitDests = new ArrayList<Position>(0);
         if (!selectedField.hostsUnit()) {
             lastUnit = null;
             lastField = null;
             return unitDests;
         }
 
         Unit u = selectedField.getUnit();
         if (u.getOwner() != state.getCurrentPlayer() ||
             u.hasFinishedTurn() || u.hasMoved()) {
             lastUnit = null;
             lastField = null;
             return unitDests;
         }
 
         if (lastDestinations == null || lastUnit == null || lastField == null) {
             lastUnit = u;
             lastField = selectedField;
             unitDests = logic.destinations(map, u);
             lastDestinations = unitDests;
             return unitDests;
         }
 
         if (u.equals(lastUnit) && selectedField.equals(lastField)) {
             return lastDestinations;
         }
 
         return unitDests;
     }
 
     public void drawBorder(final Canvas canvas, final Position currentField,
                            final RectF dest) {
         /* TODO not hard-code this */
         Integer i = currentField.getX();
         Integer j = currentField.getY();
 
         final String FAKE = "NoTaReAlTiL3";
 
         String gfn = map.getField(currentField).getName();
 
         Position nep, np, nwp, ep, cp, wp, sep, sp, swp;
         String ne, n, nw, e, c, w, se, s, sw;
 
         nep = new Position(i - 1, j - 1);
         np = new Position(i, j - 1);
         nwp = new Position(i + 1, j - 1);
 
         ep = new Position(i - 1, j);
         cp = new Position(i, j);
         wp = new Position(i + 1, j);
 
         sep = new Position(i - 1, j + 1);
         sp = new Position(i, j + 1);
         swp = new Position(i + 1, j + 1);
 
         ne = map.isValidField(nep) ? map.getField(nep).toString() : FAKE;
         n = map.isValidField(np) ? map.getField(np).toString() : FAKE;
         nw = map.isValidField(nwp) ? map.getField(nwp).toString() : FAKE;
 
         e = map.isValidField(ep) ? map.getField(ep).toString() : FAKE;
         c = map.isValidField(cp) ? map.getField(cp).toString() : FAKE;
         w = map.isValidField(wp) ? map.getField(wp).toString() : FAKE;
 
         se = map.isValidField(sep) ? map.getField(sep).toString() : FAKE;
         s = map.isValidField(sp) ? map.getField(sp).toString() : FAKE;
         sw = map.isValidField(swp) ? map.getField(swp).toString() : FAKE;
 
         Func<String, Void> drawer = new Func<String, Void>() {
             public Void apply(String sprite) {
                 canvas.drawBitmap(graphics.get("Fields").get(sprite),
                                   null, dest, null);
                 return null; /* Java strikes again */
             }
         };
 
         if (gfn.equals("Water")) {
             if (w.equals("Grass")) {
                 drawer.apply("Right grass->water border");
             }
 
             if (e.equals("Grass")) {
                 drawer.apply("Left grass->water border");
             }
 
             if (s.equals("Grass")) {
                 drawer.apply("Top grass->water border");
             }
 
             if (n.equals("Grass")) {
                 drawer.apply("Bottom grass->water border");
             }
 
             if (s.equals("Water") && w.equals("Water") && sw.equals("Grass")) {
                 drawer.apply("Grass->water corner SW");
             }
 
             if (s.equals("Water") && e.equals("Water") && se.equals("Grass")) {
                 drawer.apply("Grass->water corner SE");
             }
 
             if (n.equals("Water") && w.equals("Water") && nw.equals("Grass")) {
                 drawer.apply("Grass->water corner NW");
             }
 
             if (n.equals("Water") && e.equals("Water") && ne.equals("Grass")) {
                 drawer.apply("Grass->water corner NE");
             }
 
             if (w.equals("Grass") && s.equals("Grass") && sw.equals("Grass")) {
                 drawer.apply("Water->grass corner SW");
             }
 
             if (e.equals("Grass") && s.equals("Grass") && se.equals("Grass")) {
                 drawer.apply("Water->grass corner SE");
             }
 
             if (w.equals("Grass") && n.equals("Grass") && nw.equals("Grass")) {
                 drawer.apply("Water->grass corner NW");
             }
 
             if (e.equals("Grass") && n.equals("Grass") && ne.equals("Grass")) {
                 drawer.apply("Water->grass corner NE");
             }
 
         }
     }
 
     /* Debug method */
     private List<String> getFieldSquare(final Position p) {
         List<String> r = new ArrayList<String>();
 
         Integer i, j;
         i = p.getX();
         j = p.getY();
 
         Position nep, np, nwp, ep, cp, wp, sep, sp, swp;
         String ne, n, nw, e, c, w, se, s, sw;
 
         nep = new Position(i - 1, j - 1);
         np = new Position(i, j - 1);
         nwp = new Position(i + 1, j - 1);
 
         ep = new Position(i - 1, j);
         cp = new Position(i, j);
         wp = new Position(i + 1, j);
 
         sep = new Position(i - 1, j + 1);
         sp = new Position(i, j + 1);
         swp = new Position(i + 1, j + 1);
 
         ne = map.isValidField(nep) ? map.getField(nep).toString() : " ";
         n = map.isValidField(np) ? map.getField(np).toString() : " ";
         nw = map.isValidField(nwp) ? map.getField(nwp).toString() : " ";
 
         e = map.isValidField(ep) ? map.getField(ep).toString() : " ";
         c = map.isValidField(cp) ? map.getField(cp).toString() : " ";
         w = map.isValidField(wp) ? map.getField(wp).toString() : " ";
 
         se = map.isValidField(sep) ? map.getField(sep).toString() : " ";
         s = map.isValidField(sp) ? map.getField(sp).toString() : " ";
         sw = map.isValidField(swp) ? map.getField(swp).toString() : " ";
 
         r.add("" + ne.toString().charAt(0) + n.toString().charAt(0)
               + nw.toString().charAt(0));
         r.add("" + e.toString().charAt(0) + c.toString().charAt(0)
               + w.toString().charAt(0));
         r.add("" + se.toString().charAt(0) + s.toString().charAt(0)
               + sw.toString().charAt(0));
 
         return r;
     }
 
     public void doDraw(final Canvas canvas) {
         final int FLAG_GRAY = (255 << 24) | (156 << 16) | (156 << 8) | 156;
         Long startingTime = System.currentTimeMillis();
 
 
         Configuration c = getResources().getConfiguration();
 
         canvas.drawColor(Color.BLACK);
 
         GameField selectedField = map.isValidField(selected) ? map.getField(selected) : map.getField(0, 0);
         List<Position> unitDests = getUnitDestinations(selectedField);
 
         canvas.drawBitmap(fullMap, scrollOffset.getX(),
                           scrollOffset.getY(), null);
         for (int i = 0; i < map.getWidth(); ++i) {
             for (int j = 0; j < map.getHeight(); j++) {
                 GameField gf = map.getField(i, j);
                 RectF dest = getSquare(
                     tilesize * i + scrollOffset.getX(),
                     tilesize * j + scrollOffset.getY(),
                     tilesize);
 
                 if (gf.hostsBuilding()) {
                     canvas.drawBitmap(graphics.get("Misc").get("flag"),
                                       null, dest, null);
                 }
 
 
                 if (gf.hostsUnit()) {
                     Unit unit = gf.getUnit();
 
                     if (unit != null) {
                         String un = unit.toString();
 
                         canvas.drawBitmap(graphics.get("Units").get(un),
                                           null, dest, null);
                     }
                 }
                 /* Uncomment to print red grid with some info */
                 // Paint p = new Paint();
                 // p.setColor(Color.RED);
                 // canvas.drawText(pos.toString() + gfn.charAt(0),
                 //                 tilesize * i + scrollOffset.getX(),
                 //                 tilesize * j + scrollOffset.getY() + 64, p);
                 // List<String> aoe = getFieldSquare(pos);
                 // for (int x = 0; x < aoe.size(); ++x)
                 // canvas.drawText(aoe.get(x), tilesize * i
                 //                 + scrollOffset.getX(),
                 //                 tilesize * j + scrollOffset.getY()
                 //                 + 20 + (x * 10), p);
                 // Paint r = new Paint();
                 // r.setStyle(Paint.Style.STROKE);
                 // r.setColor(Color.RED);
                 // canvas.drawRect(dest, r);
             }
         }
 
         /* Destination highlighting */
         for (Position pos : unitDests) {
             RectF dest = getSquare(
                 tilesize * pos.getX() + scrollOffset.getX(),
                 tilesize * pos.getY() + scrollOffset.getY(),
                 tilesize);
             canvas.drawBitmap(highlighter, null, dest, null);
         }
 
         /* Sometimes draw attackables */
         if (attack_action && map.getField(selected).hostsUnit()) {
             Unit u = map.getField(selected).getUnit();
             Set<Position> attack_destinations =
                 logic.getAttackableUnitPositions(map, u, attack_location);
             //logic.getAttackableFields(map, u);
             for (Position pos : attack_destinations) {
                 RectF attack_dest = getSquare(
                     tilesize * pos.getX() + scrollOffset.getX(),
                     tilesize * pos.getY() + scrollOffset.getY(),
                     tilesize);
                 canvas.drawBitmap(attack_highlighter, null, attack_dest, null);
             }
         }
 
         // /* Always draw attackables */
         // if (map.getField(selected).hostsUnit()) {
         //     Unit u = map.getField(selected).getUnit();
         //     Set<Position> attack_destinations =
         //         logic.getAttackableUnitPositions(map, u, selected);
         //     //logic.getAttackableFields(map, u);
         //     for (Position pos : attack_destinations) {
         //         RectF dest = getSquare(
         //             tilesize * pos.getX() + scrollOffset.getX(),
         //             tilesize * pos.getY() + scrollOffset.getY(),
         //             tilesize);
         //         canvas.drawRect(dest, attack_high_paint);
         //     }
         // }
 
         // RectF dest = getSquare(
         //     tilesize * selected.getX() + scrollOffset.getX(),
         //     tilesize * selected.getY() + scrollOffset.getY(),
         //     tilesize);
         // canvas.drawBitmap(selector, null, dest, null);
 
         RectF select_dest = getSquare(
             tilesize * selected.getX() + scrollOffset.getX(),
             tilesize * selected.getY() + scrollOffset.getY(),
             tilesize);
         canvas.drawBitmap(selector, null, select_dest, null);
 
         if (selectedField.hostsUnit()) {
             drawInfoBox(canvas, selectedField.getUnit(), selectedField, true);
         } else {
             drawInfoBox(canvas, null, selectedField, true);
         }
 
         drawCornerBox(canvas, true, true, "Turn " + state.getTurns());
 
         framesSinceLastSecond++;
 
         timeElapsed += System.currentTimeMillis() - startingTime;
         if (timeElapsed >= 1000) {
             fps = framesSinceLastSecond / (timeElapsed * 0.001);
             framesSinceLastSecond = 0L;
             timeElapsed = 0L;
         }
         String fpsS = decformat.format(fps);
         Player player = state.getCurrentPlayer();
         drawCornerBox(canvas, false, true, player.getName() + " - " + player.getGoldAmount() + " Gold"
                 + "\nFPS: " + fpsS);
     }
 
     public float getMapDrawWidth() {
         return map.getWidth() * tilesize;
     }
 
     public float getMapDrawHeight() {
         return map.getHeight() * tilesize;
     }
 
     public void drawInfoBox(final Canvas canvas, final Unit unit,
                             final GameField field, final boolean left) {
         String info = field.getInfo();
 
         if (unit != null) {
             info += "\n" + unit.getInfo();
         }
 
         if (field.hostsBuilding()) {
             info += "\n" + field.getBuilding().getInfo();
         }
 
         drawCornerBox(canvas, true, false, info);
     }
 
     public void drawCornerBox(Canvas canvas, boolean left, boolean top, String text) {
         Paint textPaint = new Paint();
         textPaint.setColor(Color.WHITE);
         textPaint.setTextSize(15);
         // textPaint.setAntiAlias(true); /* uncomment for better text, worse fps */
         textPaint.setTextAlign(left ? Paint.Align.LEFT : Paint.Align.RIGHT);
 
         Paint backPaint = new Paint();
         backPaint.setARGB(150, 0, 0, 0);
 
         String[] ss = text.split("\n");
         String longestLine = "";
         for (String s : ss) {
             if (s.length() > longestLine.length()) {
                 longestLine = s;
             }
         }
 
         Rect bounds = new Rect();
         textPaint.getTextBounds(longestLine, 0,
                                    longestLine.length(), bounds);
         Integer boxWidth = bounds.width(); // Might have to Math.ceil first
         Integer boxHeight = ss.length * bounds.height();
 
         Rect backRect = new Rect(left ? 0 : canvas.getWidth() - boxWidth,
                 top ? 0 : canvas.getHeight() - boxHeight,
                 left ? boxWidth : canvas.getWidth(),
                 top ? boxHeight : canvas.getHeight());
         float radius = 5f;
         RectF backRectF = new RectF(backRect.left - radius,
                 backRect.top - radius,
                 backRect.right + radius,
                 backRect.bottom + radius);
         canvas.drawRoundRect(backRectF, 5f, 5f, backPaint);
 
 
         for (Integer i = 0; i < ss.length; ++i) {
             canvas.drawText(ss[i], 0, ss[i].length(),
                     left ? backRect.left : backRect.right,
                     backRect.top + (bounds.height() * (i + 1)),
                     textPaint);
         }
     }
 
     @Override
     public boolean onDown(final MotionEvent e) {
         return false;
     }
 
     @Override
     public boolean onFling(final MotionEvent e1, final MotionEvent e2,
                            final float velocityX, final float velocityY) {
         return false;
     }
 
     @Override
     public void onLongPress(final MotionEvent e) {
     }
 
     @Override
     public void onShowPress(final MotionEvent e) {
     }
 
     @Override
     public boolean onSingleTapUp(final MotionEvent e) {
         return false;
     }
 
     @Override
     public boolean onDoubleTap(final MotionEvent e) {
         return false;
     }
 
     @Override
     public boolean onDoubleTapEvent(final MotionEvent e) {
         return false;
     }
 
     @Override
     public boolean onSingleTapConfirmed(final MotionEvent event) {
         /* Coordinates of the pressed tile */
         int touchX = (int) ((event.getX() - scrollOffset.getX()) / tilesize);
         int touchY = (int) ((event.getY() - scrollOffset.getY()) / tilesize);
 
         Position newselected = new Position(touchX, touchY);
 
         if (this.map.isValidField(touchX, touchY)) {
             whichMenu = MenuType.NONE;
             GameField newselected_field = map.getField(newselected);
             if (!attack_action) {
                 if (map.getField(selected).hostsUnit()) {
                     //Log.v(null, "A unit is selected!");
                     /* If the user currently has a unit selected and
                      * selects a field that this unit could move to
                      * (and the unit has not finished it's turn)
                      */
                     GameField selected_field = map.getField(selected);
                     Unit unit = selected_field.getUnit();
                     if (unit.getOwner().equals(state.getCurrentPlayer()) &&
                         !unit.hasFinishedTurn() &&
                         (!map.getField(newselected).hostsUnit() ||
                          selected.equals(newselected))) {
                         List<Position> unit_destinations =
                             getUnitDestinations(selected_field);
 
                         if (unit_destinations.contains(newselected) ||
                             selected.equals(newselected)) {
                             /* pop up a menu with options:
                              * - Wait (go here and do nothing else
                              * - Attack (if there are units to attack)
                              * Currently, to dismiss/cancel the menu
                              * press anywhere
                              * other than the menu and it will go away.
                              *
                              */
                             AlertDialog.Builder actions_builder =
                                 new AlertDialog.Builder(this.getContext());
                             actions_builder.setTitle("Actions");
                             String[] actions = {"Wait here", "Attack",
                                                 "Cancel"};
                             actions_builder.setItems(actions, this);
                             actions_builder.create().show();
                             action_location = newselected;
                             // onClick handles the result
                             whichMenu = MenuType.ACTION;
                             newselected = selected; // do not move the selection
                         }
                     }
                 } else if (!newselected_field.hostsUnit() &&
                            newselected_field.hostsBuilding()) {
                     Building building = map.getField(newselected).getBuilding();
                     if (building.getOwner().equals(state.getCurrentPlayer())
                             && building.canProduceUnits()) {
                         AlertDialog.Builder buildmenu_builder
                         = new AlertDialog.Builder(this.getContext());
                         buildmenu_builder.setTitle("Build");
 
                         List<Unit> units = building.getProducibleUnits();
                         String[] buildable_names = new String[units.size() + 1];
                         for (int i = 0; i < units.size(); ++i) {
                             buildable_names[i] = units.get(i).toString() + " - "
                                 + units.get(i).getProductionCost() + " Gold";
                         }
                         buildable_names[units.size()] = "Cancel";
                         buildmenu_builder.setItems(buildable_names, this);
                         buildmenu_builder.create().show();
 
                         whichMenu = MenuType.BUILD;
                     } // build menu isn't shown if it isn't the user's turn
                 }
             } else { // attack_action
                 GameField field = map.getField(selected);
                 if (field.hostsUnit()) {
                     Unit attacker = field.getUnit();
                     Set<Position> attack_positions
                         = logic.getAttackableUnitPositions(map, attacker,
                                                            attack_location);
 
                     if (map.getField(newselected).hostsUnit() &&
                         attack_positions.contains(newselected)) {
                         Unit defender = map.getField(newselected).getUnit();
                         field.setUnit(null);
                         // move attacker to attack
                         attacker.setPosition(attack_location);
                         map.getField(attack_location).setUnit(attacker);
 
                         Log.v(null, "attack(!): " + attacker
                               + " attacks " + defender);
                         state.attack(attacker, defender);
                         attacker.setFinishedTurn(true);
 
                     } else {
                         attack_action = false; // no target unit
                     }
                 } else {
                     attack_action = false; // no unit to perform action
                 }
             }
             selected = newselected;
         }
         return true;
     }
 
     @Override
     public boolean onScroll(final MotionEvent e1, final MotionEvent e2,
                             final float distanceX, final float distanceY) {
 
         float newX = scrollOffset.getX() - distanceX;
 
         if (this.getWidth() >= this.getMapDrawWidth()) {
             newX = 0;
         } else if ((-newX) > (getMapDrawWidth() - getWidth())) {
             newX = -(getMapDrawWidth() - getWidth());
         } else if (newX > 0) {
             newX = 0;
         }
 
         float newY = scrollOffset.getY() - distanceY;
 
         if (this.getHeight() >= this.getMapDrawHeight()) {
             newY = 0;
         } else if ((-newY) > (getMapDrawHeight() - getHeight())) {
             newY = -(getMapDrawHeight() - getHeight());
         } else if (newY > 0) {
             newY = 0;
         }
 
         scrollOffset = new FloatPair(newX, newY);
 
         return true;
     }
 
     @Override
     public void onClick(final DialogInterface dialog, final int which) {
         Log.v(null, "selected option: " + which);
         switch (whichMenu) {
         case ACTION:
             switch (which) {
             case 0: // move
                 Unit unit = map.getField(selected).getUnit();
                 Boolean moved = state.move(unit, action_location);
                 break;
             case 1: // attack
                 attack_location = action_location;
                 attack_action = true;
                 /* the user will then select one of the attackable spaces
                  * (handled in onSingleTapConfirmed) to perform the attack
                  * onDraw will highlight attackable locations in red
                  */
                 break;
             }
             break;
         case BUILD:
             GameField field = map.getField(selected);
 
             if (field.hostsBuilding() &&
                 (field.getBuilding().getProducibleUnits().size() > which) &&
                 state.getCurrentPlayer().equals(
                     field.getBuilding().getOwner())) {
                 Unit unit = map.getField(selected)
                     .getBuilding().getProducibleUnits().get(which);
                 Log.v(null, "building a " + unit);
                 Boolean result = state.produceUnit(map.getField(selected),
                                                    unit);
                 if (!result) {
                     alertMessage(String.format(
                         "Could not build unit %s (Cost: %s Gold)", unit,
                         unit.getProductionCost()));
                 }
             } else if (field.getBuilding().getProducibleUnits().size()
                        != which) {
                 // how did the user manage that?
                 alertMessage("It's not the building's owner's turn"
                              + ", or there is no building");
             }
             break;
         case MENU:
             switch (which) {
             case 0:
                 state.nextPlayer();
                 Log.v(null, "advancing player");
                 break;
             }
             break;
         }
         whichMenu = MenuType.NONE;
     }
 
     private void alertMessage(String text) {
         new AlertDialog.Builder(context).setMessage(text)
             .setPositiveButton("OK", null).show();
     }
 
     @Override
     public void onClick(View arg0) {
         // This onClick if for the Menu button
         AlertDialog.Builder menu_builder
             = new AlertDialog.Builder(this.getContext());
         menu_builder.setTitle("Menu");
         String[] actions = {"End Turn", "Cancel"};
         menu_builder.setItems(actions, this);
         menu_builder.create().show();
         whichMenu = MenuType.MENU;
     }
 }
 
 class DrawingThread extends Thread {
     private boolean run;
     private Canvas canvas;
     private SurfaceHolder surfaceholder;
     private Context context;
     private GameView gview;
 
     public DrawingThread(final SurfaceHolder sholder,
                          final Context ctx, final GameView gv) {
         surfaceholder = sholder;
         context = ctx;
         run = false;
         gview = gv;
     }
 
     public void setRunning(final boolean newrun) {
         run = newrun;
     }
 
     @Override
     public void run() {
         super.run();
 
         while (run) {
             canvas = surfaceholder.lockCanvas();
 
             if (canvas != null) {
                 gview.doDraw(canvas);
                 surfaceholder.unlockCanvasAndPost(canvas);
             }
         }
     }
 
 }
 
 class FloatPair {
     private Pair<Float, Float> pair;
 
     public FloatPair(final Float x, final Float y) {
         this.pair = new Pair<Float, Float>(x, y);
     }
 
     public Float getX() {
         return this.pair.getLeft();
     }
 
     public Float getY() {
         return this.pair.getRight();
     }
 
     @Override
     public boolean equals(final Object other) {
         if (this == other) {
             return true;
         }
 
         if (!(other instanceof FloatPair)) {
             return false;
         }
 
         FloatPair that = (FloatPair) other;
         return this.getX() == that.getX() && this.getY() == that.getY();
 
     }
 
     public String toString() {
         return String.format("(%d, %d)", this.pair.getLeft(),
                              this.pair.getRight());
     }
 }
