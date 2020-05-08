 package com.group7.dragonwars;
 
 import android.app.Activity;
 
 import android.content.Context;
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
 import android.view.Window;
 import android.view.WindowManager;
 
 import com.group7.dragonwars.engine.Building;
 import com.group7.dragonwars.engine.DrawableMapObject;
 import com.group7.dragonwars.engine.GameField;
 import com.group7.dragonwars.engine.GameMap;
 import com.group7.dragonwars.engine.GameState;
 import com.group7.dragonwars.engine.Logic;
 import com.group7.dragonwars.engine.MapReader;
 import com.group7.dragonwars.engine.Position;
 import com.group7.dragonwars.engine.Pair;
 import com.group7.dragonwars.engine.Unit;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.InputStreamReader;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONException;
 
 
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
     }
 
 }
 
 class GameView extends SurfaceView implements SurfaceHolder.Callback,
                                               OnGestureListener,
                                               OnDoubleTapListener {
     private final String TAG = "GameView";
 
     private final int tilesize = 64;
 
     private Bitmap bm;
     private GameState state;
     private Logic logic;
     private GameMap map;
     private Position selected; // Currently selected position
 
     private FloatPair scrollOffset; // offset caused by scrolling, in pixels
     private GestureDetector gestureDetector;
 
     private DrawingThread dt;
     private Bitmap highlighter;
     private Bitmap selector;
 
     private Bitmap fullMap;
 
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
 
 
     public GameView(final Context ctx, final AttributeSet attrset) {
         super(ctx, attrset);
         Log.d(TAG, "GameView ctor");
 
         GameView gameView = (GameView) this.findViewById(R.id.game_view);
         GameMap gm = null;
 
         Log.d(TAG, "nulling GameMap");
 
         try {
             gm = MapReader.readMap(readFile(R.raw.cornermap)); // ugh
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
 
         loadBorders();
 
         /* Load selector and highlighter */
         selector = getResource("selector", "drawable",
                                "com.group7.dragonwars");
         highlighter = getResource("highlight", "drawable",
                                   "com.group7.dragonwars");
 
         holder.addCallback(this);
 
         selected = new Position(0, 0);
 
         gestureDetector = new GestureDetector(this.getContext(), this);
         scrollOffset = new FloatPair(0f, 0f);
 
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
 
         /* First time clicking */
         if (lastUnit == null || lastField == null || lastDestinations == null) {
             lastField = selectedField;
             if (selectedField.hostsUnit()) {
                 Unit unit = selectedField.getUnit();
                 lastUnit = unit;
                 unitDests = logic.destinations(map, unit);
                 lastDestinations = unitDests;
             } else {
                 unitDests = new ArrayList<Position>(0);
                 lastDestinations = unitDests;
             }
         }
 
         if (selectedField.equals(lastField) && selectedField.hostsUnit()) {
             /* Same unit, same place */
             if (selectedField.getUnit().equals(lastUnit)) {
                 unitDests = lastDestinations;
             }
         } else {
             if (selectedField.hostsUnit()) {
                 unitDests = logic.destinations(map, selectedField.getUnit());
             }
             lastDestinations = unitDests;
             lastField = selectedField;
             lastUnit = selectedField.getUnit(); /* Fine if null */
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
 
         if (gfn.equals("Water")) {
             if (w.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Right grass->water border"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (e.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Left grass->water border"),
                                   null, dest, null);
             }
         }
 
 
         if (gfn.equals("Water")) {
             if (s.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Top grass->water border"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (n.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Bottom grass->water border"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (s.equals("Water") && w.equals("Water") && sw.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Grass->water corner SW"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (s.equals("Water") && e.equals("Water") && se.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Grass->water corner SE"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (n.equals("Water") && w.equals("Water") && nw.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Grass->water corner NW"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (n.equals("Water") && e.equals("Water") && ne.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Grass->water corner NE"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (w.equals("Grass") && s.equals("Grass") && sw.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Water->grass corner SW"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (e.equals("Grass") && s.equals("Grass") && se.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Water->grass corner SE"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (w.equals("Grass") && n.equals("Grass") && nw.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Water->grass corner NW"),
                                   null, dest, null);
             }
         }
 
         if (gfn.equals("Water")) {
             if (e.equals("Grass") && n.equals("Grass") && ne.equals("Grass")) {
                 canvas.drawBitmap(graphics.get("Fields")
                                   .get("Water->grass corner NE"),
                                   null, dest, null);
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
         Long startingTime = System.currentTimeMillis();
 
 
         Configuration c = getResources().getConfiguration();
 
         canvas.drawColor(Color.BLACK);
 
         GameField selectedField = map.getField(selected);
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
 
         RectF dest = getSquare(
             tilesize * selected.getX() + scrollOffset.getX(),
             tilesize * selected.getY() + scrollOffset.getY(),
             tilesize);
         canvas.drawBitmap(selector, null, dest, null);
 
         if (selectedField.hostsUnit()) {
             drawInfoBox(canvas, selectedField.getUnit(), selectedField, true);
         } else {
             drawInfoBox(canvas, null, selectedField, true);
         }
 
         framesSinceLastSecond++;
 
         timeElapsed += System.currentTimeMillis() - startingTime;
         if (timeElapsed >= 1000) {
             fps = framesSinceLastSecond / (timeElapsed * 0.001);
             framesSinceLastSecond = 0L;
             timeElapsed = 0L;
         }
         String fpsS = fps.toString();
         Paint paint = new Paint();
         paint.setARGB(255, 255, 255, 255);
         canvas.drawText("FPS: " + fpsS, this.getWidth() - 80, 20, paint);
     }
 
     public float getMapDrawWidth() {
         return map.getWidth() * tilesize;
     }
 
     public float getMapDrawHeight() {
         return map.getHeight() * tilesize;
     }
 
     public void drawInfoBox(final Canvas canvas, final Unit unit,
                             final GameField field, final boolean left) {
         String info = "";
 
         info += field.getName() + "\n"; /* Always print for debug */
         // unit info
         if (unit != null) {
             info += unit.getName() + "\n";
             info += "Health: " + unit.getHealth() + "/" + unit.getMaxHealth() + "\n";
             info += "Attack: " + unit.getAttack() + "\n";
             info += "Defense: " + unit.getMeleeDefense() + " (Melee) "
                 + unit.getRangeDefense() + " (Ranged)" + "\n";
         }
 
         // field names
         if (field.hostsBuilding()) {
             Building building = field.getBuilding();
 
             info += building.getName();
 
             if (building.hasOwner()) {
                 info += " ~ " + building.getOwner().getName();
             }
 
             info += "\n";
 
             if (building.canProduceUnits()) {
                 for (Unit u : building.getProducableUnits()) {
                     info += "I can produce " + u + " - "
                         + u.getProductionCost() + "g" + "\n";
                 }
             } else {
                 info += "I can't produce anything." + "\n";
             }
         }
 
         // field stats
         info += "Attack: " + field.getAttackModifier()
             + " Defense: " + field.getDefenseModifier()
             + " Move: " + field.getMovementModifier() + "\n";
 
         Paint textPaint = new Paint();
         textPaint.setColor(Color.WHITE);
 
         String longestLine = "";
        for (String s : info.split("\n")) {
             if (s.length() > longestLine.length()) {
                 longestLine = s;
             }
         }
 
 
         Rect bounds = new Rect();
         Paint paintMeasure = new Paint();
         paintMeasure.getTextBounds(longestLine, 0, longestLine.length(), bounds);
         Integer boxWidth = bounds.width(); /* Might have to Math.ceil first */
        Integer boxHeight = info.length() * bounds.height();
 
         Paint backPaint = new Paint();
         backPaint.setColor(Color.BLACK);

         Rect backRect = new Rect(0, canvas.getHeight() - boxHeight,
                                  boxWidth, canvas.getHeight());
         canvas.drawRect(backRect, backPaint);
 
        String[] ss = info.split("\n");
         for (int i = ss.length - 1; i >= 0; --i) {
             canvas.drawText(ss[i], 0, ss[i].length(), 0,
                             canvas.getHeight() - (bounds.height() * i),
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
             this.selected = newselected;
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
 
     public Boolean equals(final FloatPair other) {
         return this.getX() == other.getX() && this.getY() == other.getY();
     }
 
     public String toString() {
         return String.format("(%d, %d)", this.pair.getLeft(),
                              this.pair.getRight());
     }
 
 }
