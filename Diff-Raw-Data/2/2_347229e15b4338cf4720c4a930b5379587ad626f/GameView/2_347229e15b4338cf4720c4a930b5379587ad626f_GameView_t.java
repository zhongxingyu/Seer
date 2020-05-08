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
 
 import android.annotation.TargetApi;
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
 import android.graphics.Paint.Align;
 import android.graphics.Rect;
 import android.graphics.RectF;
 import android.os.Build;
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
 import android.widget.Toast;
 
 import com.group7.dragonwars.engine.Building;
 import com.group7.dragonwars.engine.BitmapChanger;
 import com.group7.dragonwars.engine.DrawableMapObject;
 import com.group7.dragonwars.engine.FloatPair;
 import com.group7.dragonwars.engine.Func;
 import com.group7.dragonwars.engine.GameField;
 import com.group7.dragonwars.engine.GameFinishedException;
 import com.group7.dragonwars.engine.GameMap;
 import com.group7.dragonwars.engine.GameState;
 import com.group7.dragonwars.engine.Logic;
 import com.group7.dragonwars.engine.MapReader;
 import com.group7.dragonwars.engine.Pair;
 import com.group7.dragonwars.engine.Player;
 import com.group7.dragonwars.engine.Position;
 import com.group7.dragonwars.engine.Unit;
 
 /* Please tell me if the below causes problems, Android/Eclipse
  * suddenly decided to refuse to compile without it (because
  * GestureDetector was only introduced in API level 3 (Cupcake)
  */
 @TargetApi(Build.VERSION_CODES.CUPCAKE)
 public class GameView extends SurfaceView implements SurfaceHolder.Callback,
                                                      OnGestureListener,
                                                      OnDoubleTapListener,
                                                      DialogInterface.OnClickListener,
                                                      OnClickListener {
     private final String TAG = "GameView";
 
     private final int tilesize = 64;
 
     private GameState state;
     private Logic logic = new Logic();
     private GameMap map;
     private Position selected = new Position(0, 0);
 
     private Position attack_location;
     private Position action_location = new Position(0, 0);;
 
     private FloatPair scrollOffset = new FloatPair(0f, 0f);
     private GestureDetector gestureDetector;
 
     private DrawingThread dt;
     private Bitmap highlighter;
     private Bitmap pathHighlighter;
     private Bitmap selector;
     private Bitmap attack_highlighter;
     
     private Paint cornerBoxTextPaint;
     private Paint cornerBoxBackPaint;
     private Paint unitHealthPaint;
     private Paint unitHealthOutlinePaint;
 
     private Bitmap fullMap;
 
     private boolean unit_selected; // true if there is a unit at selection
 
     private Context context;
     private HashMap<String, HashMap<String, Bitmap>> graphics
         = new HashMap<String, HashMap<String, Bitmap>>();
 
     private GameField lastField;
     private Unit lastUnit;
     private Set<Position> lastAttackables;
     private List<Position> lastDestinations;
     private List<Position> path;
 
     private Long timeElapsed = 0L;
     private Long framesSinceLastSecond = 0L;
     private Long timeNow = 0L;
     private Double fps = 0.0;
 
     private enum MenuType {NONE, ACTION, BUILD, MENU};
 
     private MenuType whichMenu = MenuType.NONE;
 
     private DecimalFormat decformat = new DecimalFormat("#.##");
 
     public GameView(final Context ctx, final AttributeSet attrset) {
         super(ctx, attrset);
         Log.d(TAG, "GameView ctor");
 
         GameView gameView = (GameView) this.findViewById(R.id.gameView);
 
         try {
             map = MapReader.readMap(readFile(R.raw.mixmap));
         } catch (JSONException e) {
             Log.d(TAG, "Failed to load the map: " + e.getMessage());
         }
 
         if (map == null) {
             Log.d(TAG, "map is null");
             System.exit(1);
         }
 
         this.state = new GameState(map, logic, map.getPlayers());
 
         context = ctx;
         SurfaceHolder holder = getHolder();
 
         /* Load and colour all the sprites we'll need */
         Log.d(TAG, "Initialising graphics.");
         initialiseGraphics();
         Log.d(TAG, "Done initalising graphics.");
 
         holder.addCallback(this);
 
         gestureDetector = new GestureDetector(this.getContext(), this);
         
 
         cornerBoxTextPaint = new Paint();
         cornerBoxTextPaint.setColor(Color.WHITE);
         cornerBoxTextPaint.setStyle(Paint.Style.FILL);
         cornerBoxTextPaint.setTextSize(15);
         // textPaint.setAntiAlias(true); /* uncomment for better text, worse fps */
 
         cornerBoxBackPaint = new Paint();
         cornerBoxBackPaint.setARGB(150, 0, 0, 0);
         
         unitHealthPaint = new Paint();
         unitHealthPaint.setColor(Color.WHITE);
         unitHealthPaint.setStyle(Paint.Style.FILL);
         unitHealthPaint.setAntiAlias(true);
         unitHealthPaint.setTextSize(15);
         unitHealthPaint.setTextAlign(Align.RIGHT);
         
         unitHealthOutlinePaint = new Paint();
         unitHealthOutlinePaint.setColor(Color.BLACK);
         unitHealthOutlinePaint.setStyle(Paint.Style.STROKE);
         unitHealthOutlinePaint.setStrokeWidth(2);
         unitHealthOutlinePaint.setAntiAlias(unitHealthPaint.isAntiAlias());
         unitHealthOutlinePaint.setTextSize(unitHealthPaint.getTextSize());
         unitHealthOutlinePaint.setTextAlign(unitHealthPaint.getTextAlign());
 
     }
 
     private void initialiseGraphics() {
         final int DEAD_COLOUR = Color.rgb(211, 31, 45);
 
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
         pathHighlighter = getResource("path_highlight", "drawable",
                                       "com.group7.dragonwars");
         attack_highlighter = getResource("attack_highlight", "drawable",
                                          "com.group7.dragonwars");
 
         /* Prerender combined map */
         fullMap = combineMap();
         recycleBorders();
 
         /* Colour and save sprites for each player */
         for (Player p : state.getPlayers()) {
             /* Flag */
             Bitmap flagBitmap = graphics.get("Misc").get("flag");
             Bitmap colourFlag = BitmapChanger.changeColour(
                 flagBitmap, DEAD_COLOUR, p.getColour());
             p.setFlag(colourFlag);
 
             /* All possible units */
             Map<String, Bitmap> personalUnits = new HashMap<String, Bitmap>();
             for (Map.Entry<String, Bitmap> uGfx :
                      graphics.get("Units").entrySet()) {
                 Bitmap uBmap = uGfx.getValue();
                 Bitmap personal = BitmapChanger.changeColour(
                     uBmap, DEAD_COLOUR, p.getColour());
                 personalUnits.put(uGfx.getKey(), personal);
             }
             p.setUnitSprites(personalUnits);
         }
 
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
         if (!graphics.containsKey(category)) {
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
 
     private void loadBorders() {
         List<String> borderList = new ArrayList<String>();
         borderList.add("water");
         borderList.add("sand");
         borderList.add("grass");
         borderList.add("lava");
 
         for (String b : borderList) {
             for (Integer i = 1; i <= 4; i++) {
                 loadField(String.format("border_%s_%d", b, i),
                           String.format("border %s %d", b, i));
                 loadField(String.format("corner_%s_%d", b, i),
                           String.format("corner %s %d", b, i));
                 loadField(String.format("fullcorner_%s_%d", b, i),
                           String.format("fullcorner %s %d", b, i));
             }
         }
     }
 
     private void recycleBorders() {
         List<String> borderList = new ArrayList<String>();
         borderList.add("water");
         borderList.add("sand");
         borderList.add("grass");
         borderList.add("lava");
 
         for (String b : borderList) {
             for (Integer i = 1; i <= 4; i++) {
                 graphics.get("Fields").get(String.format("border %s %d", b, i)).recycle();
                 graphics.get("Fields").get(String.format("corner %s %d", b, i)).recycle();
                 graphics.get("Fields").get(String.format("fullcorner %s %d", b, i)).recycle();
             }
         }
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
             lastAttackables = null;
             path = null;
             return unitDests;
         }
 
         Unit u = selectedField.getUnit();
         if (u.getOwner() != state.getCurrentPlayer() ||
             u.hasFinishedTurn()) {
             lastUnit = null;
             lastField = null;
             path = null;
             lastAttackables = null;
 
             return unitDests;
         }
 
         if (u.hasMoved()) {
             lastUnit = null;
             lastField = null;
             path = null;
             lastAttackables = logic.getAttackableUnitPositions(map, u);
             return unitDests;
         }
 
         if (lastDestinations == null || lastUnit == null || lastField == null ||
             lastAttackables == null) {
             lastUnit = u;
             lastField = selectedField;
             unitDests = logic.destinations(map, u);
             lastDestinations = unitDests;
             lastAttackables = logic.getAttackableUnitPositions(map, u);
             path = null;
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
 
         String gfn = map.getField(currentField).getSpriteLocation();
 
         Position nep, np, nwp, ep, cp, wp, sep, sp, swp;
         String ne, n, nw, e, c, w, se, s, sw;
 
         nep = new Position(i + 1, j - 1);
         np = new Position(i, j - 1);
         nwp = new Position(i - 1, j - 1);
 
         ep = new Position(i + 1, j);
         cp = new Position(i, j);
         wp = new Position(i - 1, j);
 
         sep = new Position(i + 1, j + 1);
         sp = new Position(i, j + 1);
         swp = new Position(i - 1, j + 1);
 
         ne = map.isValidField(nep) ? map.getField(nep).getSpriteLocation() : FAKE;
         n = map.isValidField(np) ? map.getField(np).getSpriteLocation() : FAKE;
         nw = map.isValidField(nwp) ? map.getField(nwp).getSpriteLocation() : FAKE;
 
         e = map.isValidField(ep) ? map.getField(ep).getSpriteLocation() : FAKE;
         c = map.isValidField(cp) ? map.getField(cp).getSpriteLocation() : FAKE;
         w = map.isValidField(wp) ? map.getField(wp).getSpriteLocation() : FAKE;
 
         se = map.isValidField(sep) ? map.getField(sep).getSpriteLocation() : FAKE;
         s = map.isValidField(sp) ? map.getField(sp).getSpriteLocation() : FAKE;
         sw = map.isValidField(swp) ? map.getField(swp).getSpriteLocation() : FAKE;
 
         Func<String, Void> drawer = new Func<String, Void>() {
             public Void apply(String sprite) {
                 canvas.drawBitmap(graphics.get("Fields").get(sprite),
                                   null, dest, null);
                 return null; /* Java strikes again */
             }
         };
         List<String> land = new ArrayList<String>();
         land.add("grass");
         land.add("sand");
         List<String> liquid = new ArrayList<String>();
         liquid.add("water");
         liquid.add("lava");
         if (liquid.contains(gfn)) {
 
             if (land.contains(s)) {
                 drawer.apply(String.format("border %s %d", s, 1));
             }
 
             if (land.contains(e)) {
                 drawer.apply(String.format("border %s %d", e, 2));
             }
 
             if (land.contains(n)) {
                 drawer.apply(String.format("border %s %d", n, 3));
             }
 
             if (land.contains(w)) {
                 drawer.apply(String.format("border %s %d", w, 4));
             }
 
             if (liquid.contains(s) && liquid.contains(e) && land.contains(se)) {
                 drawer.apply(String.format("corner %s %d", se, 1));
             }
 
             if (liquid.contains(n) && liquid.contains(e) && land.contains(ne)) {
                 drawer.apply(String.format("corner %s %d", ne, 2));
             }
 
             if (liquid.contains(n) && liquid.contains(w) && land.contains(nw)) {
                 drawer.apply(String.format("corner %s %d", nw, 3));
             }
 
             if (liquid.contains(s) && liquid.contains(w) && land.contains(sw)) {
                 drawer.apply(String.format("corner %s %d", sw, 4));
             }
 
             if (land.contains(s) && land.contains(e) && land.contains(se)) {
                 if (s.equals(e) && e.equals(se)) {
                     drawer.apply(String.format("fullcorner %s %d", s, 1));
                 }
             }
 
             if (land.contains(n) && land.contains(e) && land.contains(ne)) {
                 if (n.equals(e) && e.equals(ne)) {
                     drawer.apply(String.format("fullcorner %s %d", n, 2));
                 }
             }
 
             if (land.contains(n) && land.contains(w) && land.contains(nw)) {
                 if (n.equals(w) && w.equals(nw)) {
                     drawer.apply(String.format("fullcorner %s %d", n, 3));
                 }
             }
 
             if (land.contains(s) && land.contains(w) && land.contains(sw)) {
                 if (s.equals(w) && w.equals(sw)) {
                     drawer.apply(String.format("fullcorner %s %d", s, 4));
                 }
             }
 
         }
     }
 
 
     public void doDraw(final Canvas canvas) {
         Long startingTime = System.currentTimeMillis();
 
 
         Configuration c = getResources().getConfiguration();
 
         canvas.drawColor(Color.BLACK);
 
         GameField selectedField = map.isValidField(selected) ? map.getField(selected) : map.getField(0, 0);
         List<Position> unitDests = getUnitDestinations(selectedField);
 
         Player player = state.getCurrentPlayer();
 
         Paint playerPaint = new Paint();
         playerPaint.setColor(player.getColour());
 
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
                     Player owner = gf.getBuilding().getOwner();
                     if (owner.getName().equals("Gaia")) { /* TODO proper Gaia handling */
                         canvas.drawBitmap(graphics.get("Misc").get("flag"),
                                           null, dest, null);
                     } else {
                         canvas.drawBitmap(owner.getFlag(), null, dest, null);
                     }
                 }
 
 
                 if (gf.hostsUnit()) {
                     Unit unit = gf.getUnit();
 
                     if (unit != null) {
                         String un = unit.toString();
                         Player owner = unit.getOwner();
                         if (owner.getName().equals("Gaia")) { /* TODO proper Gaia handling */
                             canvas.drawBitmap(graphics.get("Units").get(un),
                                               null, dest, null);
                         } else {
                             Bitmap unitGfx = owner.getUnitSprite(un);
                             canvas.drawBitmap(unitGfx, null, dest, null);
                             String healthText = decformat.format(unit.getHealth());
                             canvas.drawText(healthText, dest.right, dest.bottom, unitHealthOutlinePaint);
                             canvas.drawText(healthText, dest.right, dest.bottom, unitHealthPaint);
                         }
                     }
                 }
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
 
         if (unitDests.size() > 0 && path != null) {
             for (Position pos : path) {
                 RectF dest = getSquare(
                     tilesize * pos.getX() + scrollOffset.getX(),
                     tilesize * pos.getY() + scrollOffset.getY(),
                     tilesize);
                 canvas.drawBitmap(pathHighlighter, null, dest, null);
             }
         }
 
         /* Always draw attackables */
         if (map.getField(selected).hostsUnit()) {
             Unit u = map.getField(selected).getUnit();
             if (lastAttackables != null) {
                 for (Position pos : lastAttackables) {
                     RectF attack_dest = getSquare(
                         tilesize * pos.getX() + scrollOffset.getX(),
                         tilesize * pos.getY() + scrollOffset.getY(),
                         tilesize);
                     canvas.drawBitmap(attack_highlighter, null, attack_dest, null);
                 }
             }
         }
 
 
         RectF select_dest = getSquare(
             tilesize * selected.getX() + scrollOffset.getX(),
             tilesize * selected.getY() + scrollOffset.getY(),
             tilesize);
         canvas.drawBitmap(selector, null, select_dest, null);
 
         double offsetTiles = -scrollOffset.getX() / (double)tilesize;
         double tpw = canvas.getWidth() / (double)tilesize;
         boolean infoLeft = (selected.getX() - offsetTiles) > (tpw / 2);
 
         if (selectedField.hostsUnit()) {
             drawInfoBox(canvas, selectedField.getUnit(), selectedField, infoLeft);
         } else {
             drawInfoBox(canvas, null, selectedField, infoLeft);
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
         drawCornerBox(canvas, false, true, player.getName() + " - " + player.getGoldAmount() + " Gold"
                       + "\nFPS: " + fpsS, true, playerPaint);
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
 
         drawCornerBox(canvas, left, false, info);
     }
 
     public void drawCornerBox(Canvas canvas, boolean left, boolean top, String text) {
         drawCornerBox(canvas, left, top, text, false, null);
     }
 
     public void drawCornerBox(Canvas canvas, boolean left, boolean top, String text, boolean box, Paint boxPaint) {
         cornerBoxTextPaint.setTextAlign(left ? Paint.Align.LEFT : Paint.Align.RIGHT);
         
         String[] ss = text.split("\n");
         String longestLine = "";
         for (String s : ss) {
             if (s.length() > longestLine.length()) {
                 longestLine = s;
             }
         }
 
         Rect bounds = new Rect();
         cornerBoxTextPaint.getTextBounds(longestLine, 0,
                                 longestLine.length(), bounds);
         Integer boxWidth = bounds.width(); // Might have to Math.ceil first
         Integer boxHeight = ss.length * bounds.height();
 
         Rect backRect = new Rect(left ? 0 : canvas.getWidth() - boxWidth,
                                  top ? 0 : canvas.getHeight() - boxHeight,
                                  left ? boxWidth : canvas.getWidth(),
                                  top ? boxHeight : canvas.getHeight());
         float radius = 5f;
         RectF backRectF = new RectF(backRect.left - radius - (box && !left ? radius + boxHeight : 0),
                                     backRect.top - radius,
                                     backRect.right + radius + (box && left ? radius + boxHeight : 0),
                                     backRect.bottom + radius);
         canvas.drawRoundRect(backRectF, 5f, 5f, cornerBoxBackPaint);
 
         if (box) {
             canvas.drawRect(new RectF(left ? backRect.right + radius : backRect.left - radius - boxHeight,
                                       backRect.top,
                                       left ? backRect.right + radius + boxHeight : backRect.left - radius,
                                       backRect.top + boxHeight), boxPaint);
         }
 
         for (Integer i = 0; i < ss.length; ++i) {
             canvas.drawText(ss[i], 0, ss[i].length(),
                             left ? backRect.left : backRect.right,
                             backRect.top + (bounds.height() * (i + 1)),
                             cornerBoxTextPaint);
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
             if (lastAttackables == null || !lastAttackables.contains(newselected)) {
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
                                 if (path == null) {
                                     path = logic.findPath(map, unit, newselected);
                                 } else {
                                     if (path.contains(newselected)) {
                                         state.move(unit, newselected);
                                     } else {
                                         path = logic.findPath(map, unit, newselected);
                                     }
                                 }
                                 newselected = selected;
                             }
                     }
                 } else if (!newselected_field.hostsUnit() &&
                            newselected_field.hostsBuilding()) {
                     path = null;
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
             } else { // attack
                 GameField field = map.getField(selected);
                 Unit attacker = field.getUnit();
                 Unit defender = map.getField(newselected).getUnit();
 
                 Log.v(null, "attack(!): " + attacker
                       + " attacks " + defender);
                 state.attack(attacker, defender);
                 attacker.setFinishedTurn(true);
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
                 try {
                     state.nextPlayer();
                     Log.v(null, "advancing player");
                     Toast.makeText(context,
                                   String.format("%s's turn!",
                                                  state.getCurrentPlayer()),
                                    Toast.LENGTH_LONG).show();
                 }
                 catch (GameFinishedException e) {
                     Player winner = e.getWinner();
                     Toast.makeText(context,
                                    String.format("%s has won the game!",
                                                  winner.getName()),
                                    Toast.LENGTH_LONG).show();
                     /* TODO finish game somehow */
                 }
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
