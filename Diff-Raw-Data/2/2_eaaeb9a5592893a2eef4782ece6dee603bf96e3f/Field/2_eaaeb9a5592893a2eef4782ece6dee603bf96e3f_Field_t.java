 /*
  *  @author Intexon
  */
 package core;
 
 import core.Map.Map;
 import core.Tile.Tower.Tower;
 import core.Tile.Tower.UberTower;
 import core.Unit.Unit;
 import java.awt.Point;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.*;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  *
  * @author Intexon
  */
 public final class Field extends BasicGameState
 {
   public final int WIDTH = 20, HEIGHT = 15, SCALE = 32;
   
   private Map map = new Map(WIDTH, HEIGHT) {};
   
   private int stateID;
   
   private HashMap<String, Image> textureCache = new HashMap<String, Image>();
   
   private int state = STATE_PAUSED;
   private List<Tower> availableTowers = new LinkedList<Tower>();
   
   private Tower building = null;
   private int currentState = STATE_PAUSED;
   public static final int STATE_PAUSED = 0,
                           STATE_SPAWNING = 1,
                           STATE_IDLE = 2,
                           STATE_BUILDING = 3,
                           STATE_DEAD = 4;
   
   // dummy unit movement
   private int lastMove = 0, moveInterval = 1000; // ms
   // font
   private UnicodeFont segoeUI, trajan;
 
   public Field(int stateID)
   {
     this.stateID = stateID;
   }
   
   
   /**
    * units
    */
   
   public void addUnit(Unit unit, Point coords) throws Exception {
     map.addUnit(unit, coords);
   }
   
   public void removeUnit(Point coords) {
     map.removeUnit(coords);
   }
 
   /**
    * state
    */
   
   public int getCurrentState() {
     return currentState;
   }
   
   @Override
   public int getID()
   {
     return stateID;
   }
 
   @Override
   public void init(GameContainer container, StateBasedGame game) throws SlickException
   {
     try {
       map.generateMap();
     } catch (Exception e) {
       System.out.println("Map generation failed");
     }
    currentState = Field.STATE_SPAWNING;
     
     java.awt.Font f = new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12);
     segoeUI = new UnicodeFont(f);
     segoeUI.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
     segoeUI.addAsciiGlyphs();
     segoeUI.loadGlyphs();
     
     f = new java.awt.Font("Trajan Pro", java.awt.Font.PLAIN, 14);
     trajan = new UnicodeFont(f);
     trajan.getEffects().add(new ColorEffect(java.awt.Color.WHITE));
     trajan.addAsciiGlyphs();
     trajan.loadGlyphs();
     
     // towers
     availableTowers.add(new UberTower());
     availableTowers.add(new UberTower());
   }
 
   @Override
   public void render(GameContainer container, StateBasedGame game, Graphics g) throws SlickException
   {
     for (int i = 0; i < WIDTH; i++) {
       for (int j = 0; j < HEIGHT; j++) {
         try {
           getCachedTexture(map.getTileAt(i, j).getTexturePath()).draw(i * SCALE, j * SCALE);
         } catch (Exception e) {
           System.out.println("null tile");
         }
         if (map.isTileOccupied(i, j)) {
           try {
             getCachedTexture(map.getUnitAt(i, j).getTexturePath()).draw(i * SCALE, j * SCALE);
           } catch (Exception e) {
             System.out.println(e.getMessage());
           }
         }
       }
     }
    
     // draw tower selection list
     for (int it = 0; it < availableTowers.size(); it++) {
       Tower tower = availableTowers.get(it);
       Rectangle rect = new Rectangle(SCALE, (HEIGHT + it + 1) * SCALE, SCALE, SCALE);
       tower.setRectangle(rect);
       g.draw(rect);
       getCachedTexture(tower.getTexturePath()).draw(SCALE, (HEIGHT + it + 1) * SCALE);
       
       g.setFont(trajan);
       g.drawString(tower.getName(), (float) 2.5 * SCALE, (HEIGHT + it + 1) * SCALE);
     
       g.setFont(segoeUI);
       g.drawString(tower.getDescription(), (float) 2.5 * SCALE, (float) (HEIGHT + it + 1.5) * SCALE);
     }
     
     // drag tower
     if (building != null) {
       Input in = container.getInput();
       if (in.getMouseX() < WIDTH * SCALE && in.getMouseY() < HEIGHT * SCALE) {
         getCachedTexture(building.getTexturePath()).draw(in.getMouseX() - in.getMouseX() % SCALE, in.getMouseY() - in.getMouseY() % SCALE);
       } else {
         getCachedTexture(building.getTexturePath()).draw(in.getMouseX() - SCALE / 2, in.getMouseY() - SCALE / 2);
       }
     }
   }
 
   @Override
   public void update(GameContainer container, StateBasedGame game, int delta) throws SlickException
   {
     for (int i = 0; i < WIDTH; i++) {
       for (int j = 0; j < HEIGHT; j++) {
         try {
           map.getTileAt(i, j).update(this, delta);
         } catch (Exception e) {
           System.out.println("null tile");
         }
         // dummy unit movement
         lastMove += delta;
         if (lastMove > moveInterval) {
           lastMove = 0;
           try {
             map.moveUnit(new Point(i, j), 1, 0);
           } catch (Exception e) {
             System.out.println(e.getMessage());
           }
         }
       }
       
       // detect tower selection
       Input in = container.getInput();
       if (in.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
         if (building == null) {
           Iterator<Tower> it = availableTowers.iterator();
           while (it.hasNext()) {
             Tower t = it.next();
             if (t.getRectangle().contains(in.getMouseX(), in.getMouseY())) {
               building = t;
               break;
             }
           }
         } else {
           try {
             // place the tower
             map.addTile(building, new Point(in.getMouseX() / SCALE, in.getMouseY() / SCALE));
           } catch (Exception e) {
             System.out.println("cannot place");
           }
           building = null;
         }
       }
     }
     
     
   }
   
   /**
    * misc
    */
 
   private Image getCachedTexture(String path) throws SlickException
   {
     if (textureCache.containsKey(path)) {
       return textureCache.get(path);
     }
     Image texture = new Image(path);
     textureCache.put(path, texture);
     return texture;
   }
 }
