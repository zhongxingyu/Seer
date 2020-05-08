 package snakemeleon;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.PointerInfo;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import maryb.player.Player;
 
 import org.jbox2d.callbacks.ContactImpulse;
 import org.jbox2d.callbacks.ContactListener;
 import org.jbox2d.collision.Manifold;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyType;
 import org.jbox2d.dynamics.contacts.Contact;
 
 import snakemeleon.types.ChameleonScript;
 import snakemeleon.types.ChameleonStickyScript;
 import snakemeleon.types.Collectable;
 import snakemeleon.types.KeyTriggerEntityAction;
 import toritools.debug.Debug;
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.entity.ReservedTypes;
 import toritools.entity.sprite.AbstractSprite.AbstractSpriteAdapter;
 import toritools.entity.sprite.ImageSprite;
 import toritools.entrypoint.Binary;
 import toritools.io.FontLoader;
 import toritools.io.Importer;
 import toritools.math.MidpointChain;
 import toritools.math.Vector2;
 import toritools.physics.Universe;
 import toritools.scripting.EntityScript;
 import toritools.scripting.EntityScript.EntityScriptAdapter;
 import toritools.scripting.ScriptUtils;
 
 public class Snakemeleon extends Binary {
 
     /*
      * CONSTANTS!
      */
 
     /**
      * The core universe. Reference this to apply forces, etc.
      */
     public static Universe uni;
 
     /*
      * The midpoint chain with the a tracking the player, and being the camera
      * offset.
      */
     private static MidpointChain camera;
     private static Vector2 offset = Vector2.ZERO;
 
     public static boolean isMouseDragging = false, rightSticking = false;
     public static Vector2 mousePos = Vector2.ZERO;
 
     private static int currentLevel = 0;
     private static String[] levels = new String[] { "snakemeleon/level1.xml", "snakemeleon/level2.xml",
             "snakemeleon/level3.xml", "snakemeleon/level4.xml", "snakemeleon/level5.xml", "snakemeleon/level6.xml" };
 
     private static Font uiFont;
 
     private static SnakemeleonHUD hud = new SnakemeleonHUD();
 
     private static File bgFile;
 
     public static void main(String[] args) {
         new Snakemeleon();
     }
 
     public Snakemeleon() {
         super(new Vector2(800, 600), 60, "Snakemeleon");
         super.getApplicationFrame().setIconImage(ScriptUtils.fetchImage(new File("snakemeleon/chameleon_head.png")));
         super.getApplicationFrame().setResizable(true);
     }
 
     @Override
     protected void initialize() {
 
         try {
             FontLoader.loadFont(new File("snakemeleon/eartm.ttf"));
         } catch (Exception e1) {
             e1.printStackTrace();
         }
 
         uiFont = new Font("Earth's Mightiest", Font.TRUETYPE_FONT, 40);
 
         bgFile = new File("snakemeleon/forest1.png");
 
         Player player = new Player();
         player.setSourceLocation("snakemeleon/sounds/BGM/Wallpaper.mp3");
        // player.play();
 
         // Create a new blank cursor.
         Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                 new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB), new Point(), "Red Circle Cursor");
         // Set the blank cursor to the JFrame.
         super.getApplicationFrame().getContentPane().setCursor(blankCursor);
 
         super.getApplicationFrame().addMouseListener(new MouseAdapter() {
             @Override
             public void mousePressed(MouseEvent arg0) {
                 if (arg0.getButton() == MouseEvent.BUTTON1)
                     Snakemeleon.isMouseDragging = true;
                 else
                     Snakemeleon.rightSticking = true;
             }
 
             @Override
             public void mouseReleased(MouseEvent arg0) {
                 if (arg0.getButton() == MouseEvent.BUTTON1)
                     Snakemeleon.isMouseDragging = false;
                 else
                     Snakemeleon.rightSticking = false;
             }
         });
     }
 
     @Override
     protected void globalLogic(Level level) {
 
         if (ScriptUtils.getKeyHolder().isPressed(KeyEvent.VK_ESCAPE)) {
             System.exit(0);
         }
 
         if (ScriptUtils.getKeyHolder().isPressedThenRelease(KeyEvent.VK_F12)) {
             nextLevel();
         }
 
         if (ScriptUtils.getKeyHolder().isPressedThenRelease(KeyEvent.VK_P)) {
             Debug.showDebugPrintouts = !Debug.showDebugPrintouts;
         }
 
         uni.step(60 / 1000f);
 
         camera.setA(level.getEntityWithId(SnakemeleonConstants.playerTypeId).getPos());
         camera.smoothTowardA();
 
         Vector2 halfPort = VIEWPORT.scale(.5f);
 
         float x = Math.min(Math.max(camera.getB().x, halfPort.x), level.getDim().x - halfPort.x);
         float y = Math.min(Math.max(camera.getB().y, halfPort.y), level.getDim().y - halfPort.y);
         offset = halfPort.sub(new Vector2(x, y));
 
         PointerInfo e = MouseInfo.getPointerInfo();
         Point frameLoc = super.getApplicationFrame().getLocationOnScreen();
         mousePos = new Vector2(-offset.getWidth() + e.getLocation().x - frameLoc.x, -offset.getHeight()
                 + e.getLocation().y - frameLoc.y);
 
         hud.update(1, level);
     }
 
     @Override
     protected void setupCurrentLevel(Level levelBeingLoaded) {
 
         uni = new Universe(SnakemeleonConstants.gravity);
 
         final Entity cham = levelBeingLoaded.getEntityWithId(SnakemeleonConstants.playerTypeId);
         cham.addScript(new ChameleonScript());
         // A script to enable the chameleon to stick to things. Should be the
         // last script you add to cham.
         cham.addScript(new ChameleonStickyScript());
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType(ReservedTypes.WALL)) {
             uni.addEntity(e, BodyType.STATIC, false, false, 1f, .3f);
         }
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType("RIGHT_DIAG_WALL")) {
             uni.addEntity(e, BodyType.STATIC, false, false, 1f, .3f,
                     new Vector2[] { (e.getDim().scale(.5f, -.5f)), (e.getDim().scale(-.5f, .5f)) }, false);
         }
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType("LEFT_DIAG_WALL")) {
             uni.addEntity(e, BodyType.STATIC, false, false, 1f, .3f,
                     new Vector2[] { (e.getDim().scale(-.5f, -.5f)), (e.getDim().scale(.5f, .5f)) }, false);
         }
 
         final EntityScript breakScript = new EntityScriptAdapter() {
             @Override
             public void onDeath(Entity self, Level level, boolean isRoomExit) {
                 boolean childrenAreProps = self.getDim().x > 32 && self.getDim().y > 32;
                 for (int i = 0; i < 2; i++) {
                     for (int i2 = 0; i2 < 2; i2++) {
                         try {
                             Entity e = new Entity();
                             e.setPos(self.getPos().add(self.getDim().scale(i * .5f)));
                             e.setDim(self.getDim().scale(.5f));
                             e.setSprite(new ImageSprite(self.getSprite().getImageIndex(), 2, 2));
                             e.getSprite().set(i, i2);
                             level.spawnEntity(e);
                             if (childrenAreProps) {
                                 e.addScript(this);
                                 e.setType(SnakemeleonConstants.dynamicPropType);
                             }
                             Body body = Snakemeleon.uni.addEntity(e, BodyType.DYNAMIC, true, false, .2f, .03f);
                             Snakemeleon.uni.applyLinearImpulse(e, new Vector2((float) (-.5 + Math.random()) * .01f,
                                     (float) (-.5 + Math.random()) * .01f));
                             if (!childrenAreProps) {
                                 body.getFixtureList().m_filter.categoryBits = 3;
                             }
                         } catch (final Exception e) {
                             e.printStackTrace();
                         }
                     }
                 }
             }
         };
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType(SnakemeleonConstants.dynamicPropType)) {
             String isRound = e.getVariableCase().getVar("round");
             boolean round = false;
             if (isRound != null && isRound.equalsIgnoreCase("true")) {
                 round = true;
             }
             uni.addEntity(e, BodyType.DYNAMIC, true, round, 1f, .3f);
             if (e.getVariableCase().getVar("key") != null) {
                 e.addScript(new KeyTriggerEntityAction());
             }
 
             if (e.getVariableCase().getVar("id") != null && e.getVariableCase().getVar("id").equals("collectable")) {
                 e.addScript(new Collectable());
             } else {
                 e.addScript(breakScript);
             }
         }
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType(SnakemeleonConstants.hingeType)) {
             levelBeingLoaded.despawnEntity(e);
             Entity a = null, b = null;
             Vector2 pos = e.getPos().add(e.getDim().scale(.5f));
             for (Entity object : levelBeingLoaded.getNewEntities()) {
                 if (!object.getType().equals(ReservedTypes.BACKGROUND) && ScriptUtils.isPointWithin(object, pos)
                         && object != e) {
                     if (a == null)
                         a = object;
                     else if (a != null)
                         b = object;
                 }
             }
 
             if (a != null && b != null)
                 uni.addHinge(a, b, pos);
         }
 
         EntityScript spikeScript = new EntityScript() {
 
             Entity player;
 
             @Override
             public void onSpawn(Entity self, Level level) {
                 player = level.getEntityWithId("player");
             }
 
             @Override
             public void onUpdate(Entity self, float time, Level level) {
                 if (!Debug.showDebugPrintouts && player.isActive() && ScriptUtils.isColliding(self, player)) {
                     player.setActive(false);
                 }
             }
 
             @Override
             public void onDeath(Entity self, Level level, boolean isRoomExit) {
 
             }
 
         };
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType("spike")) {
             e.addScript(spikeScript);
         }
 
         for (Entity e : levelBeingLoaded.getEntitiesWithType("message")) {
             final String message = e.getVariableCase().getVar("message");
             e.setSprite(new AbstractSpriteAdapter() {
                 @Override
                 public void draw(Graphics2D g, Entity self) {
                     g.setColor(Color.CYAN);
                     g.drawString(message, self.getPos().getWidth(), self.getPos().getHeight());
                 }
             });
         }
 
         /*
          * if a dynamic prop is dangerous, hurt cham
          */
         uni.addContactListener(new ContactListener() {
 
             @Override
             public void beginContact(Contact arg0) {
                 Entity a = (Entity) arg0.m_fixtureA.getUserData();
                 Entity b = (Entity) arg0.m_fixtureB.getUserData();
 
                 if ((a == cham && b.getVariableCase().getVar("hurt") != null)
                        || (b == cham && a.getVariableCase().getVar("hurt") != null)) {
                     if (!Debug.showDebugPrintouts && cham.isActive())
                         cham.setActive(false);
                } else if (a.getType().equals(SnakemeleonConstants.dynamicPropType)
                         && a.getVariableCase().getVar("hurt") == null && b.getVariableCase().getVar("hurt") != null
                         && !"collectable".equals(a.getVariableCase().getVar("id")))
                     ScriptUtils.getCurrentLevel().despawnEntity(a);
                 else if (a.getType().equals(SnakemeleonConstants.dynamicPropType)
                        && b.getVariableCase().getVar("hurt") == null && a.getVariableCase().getVar("hurt") != null
                         && !"collectable".equals(b.getVariableCase().getVar("id")))
                     ScriptUtils.getCurrentLevel().despawnEntity(b);
 
             }
 
             @Override
             public void endContact(Contact arg0) {
 
             }
 
             @Override
             public void postSolve(Contact arg0, ContactImpulse arg1) {
 
             }
 
             @Override
             public void preSolve(Contact arg0, Manifold arg1) {
 
             }
         });
 
         levelBeingLoaded.bakeBackground();
 
         camera = new MidpointChain(levelBeingLoaded.getEntityWithId(SnakemeleonConstants.playerTypeId).getPos(),
                 SnakemeleonConstants.cameraLag);
     }
 
     @Override
     protected Level getStartingLevel() {
         try {
             return Importer.importLevel(new File(levels[0]));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         }
         return null;
     }
 
     private final BasicStroke dottedStroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10,
             new float[] { 5, 5, 5, 5 }, 0);
 
     @Override
     protected boolean render(Graphics2D rootCanvas, Level level) {
         try {
             rootCanvas.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             rootCanvas.setFont(uiFont);
 
             Entity player = level.getEntityWithId(SnakemeleonConstants.playerTypeId);
             if (player == null) {
                 System.out.println("You need to make an entity with id set to player!");
                 System.exit(1);
             }
 
             rootCanvas.drawImage(ScriptUtils.fetchImage(bgFile), (int) 0, (int) 0, VIEWPORT.getWidth(),
                     VIEWPORT.getHeight(), null);
 
             rootCanvas.translate(offset.getWidth(), offset.getHeight());
 
             rootCanvas.drawImage(level.getBakedBackground(), (int) 0, (int) 0, (int) level.getDim().x,
                     (int) level.getDim().y, null);
 
             if (ChameleonStickyScript.isGrabbing && player.isActive()) {
                 rootCanvas.setColor(Color.GREEN);
                 rootCanvas.setStroke(dottedStroke);
                 rootCanvas.drawOval(player.getPos().getWidth(), player.getPos().getHeight(),
                         player.getDim().getWidth(), player.getDim().getHeight());
 
             }
 
             for (int i = level.getLayers().size() - 1; i >= 0; i--) {
                 for (Entity e : level.getLayers().get(i)) {
                     if (e.isVisible() && e.isInView())
                         e.draw(rootCanvas);
                 }
             }
 
             if (Debug.showDebugPrintouts)
                 for (Entity wall : level.getEntitiesWithType("WALL"))
                     wall.draw(rootCanvas);
 
             rootCanvas.translate(-offset.getWidth(), -offset.getHeight());
 
             hud.draw(rootCanvas, VIEWPORT);
 
         } catch (final Exception e) {
             return false;
         }
         return true;
     }
 
     public static void restartLevel() {
         try {
             ScriptUtils.queueLevelSwitch(Importer.importLevel(new File(levels[currentLevel])));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     public static void nextLevel() {
         try {
             ScriptUtils.queueLevelSwitch(Importer.importLevel(new File(levels[++currentLevel])));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IndexOutOfBoundsException winner) {
             System.out.println("You won!");
             System.exit(1);
         }
     }
 }
