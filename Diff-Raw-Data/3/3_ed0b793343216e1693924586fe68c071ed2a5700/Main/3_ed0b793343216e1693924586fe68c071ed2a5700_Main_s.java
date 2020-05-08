 package Project.Game;
 
 import Project.Game.AI.ParticleSwarm.PlanningGrid;
 import Project.Game.Blueprints.BlueprintRegistry;
 import Project.Game.Buildings.BuildingRegistry;
 import Project.Game.Influence.InfluenceMap;
 import Project.Game.Registries.EntityRegistry;
 import Project.Game.Registries.NameRegistry;
 import Project.Game.UI.UIManager;
 import Project.NLP.Pipeline;
 import de.matthiasmann.twl.GUI;
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.matthiasmann.twl.theme.ThemeManager;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * User: Piers
  * Date: 16/10/12
  * Time: 11:49
  */
 public class Main {
     public static int SCREEN_WIDTH = 1600;
     public static int SCREEN_HEIGHT = 900;
 
     public static int MAP_WIDTH = 3200;
     public static int MAP_HEIGHT = 1800;
     public static int SQUARE_WIDTH = 6;
     public static boolean FULL_SCREEN = true;
     // Collision detection cell size
     public static final int CELL_SIZE = 75;
 
     public static GameLoop GAME_LOOP;
     public static CollisionBoard COLLISION_BOARD;
     public static InfluenceMap INFLUENCE_MAP;
     public static CachedVector2DSource VECTOR2D_SOURCE;
     public static BlueprintRegistry BLUEPRINT_REGISTRY;
     public static BuildingRegistry BUILDING_REGISTRY;
     public static Main MAIN;
     public static PlanningGrid PLANNING_GRID;
     public static Pipeline PIPELINE;
     public static NameRegistry REGISTRY;
 
     // Please don't re-assign this one
     public static Faction HUMAN_FACTION;
 
     public static ControlManager Control_MANAGER;
     private Boolean paused;
 
     private GUI gui;
     private LWJGLRenderer renderer;
     private ThemeManager themeManager;
 
     private UIManager UIManager;
     public String blueprintToBuild;
 
     // Where the view is located
     public static final Vector2D viewLocation = new Vector2D(0, 0);
 
     public Main() {
 
         Main.MAIN = this;
         BLUEPRINT_REGISTRY = BlueprintRegistry.load("Content/Bases/Bases.xml");
         BUILDING_REGISTRY = new BuildingRegistry();
         BUILDING_REGISTRY.load();
         paused = true;
         Main.GAME_LOOP = new GameLoop(20);
         VECTOR2D_SOURCE = new CachedVector2DSource();
         Main.COLLISION_BOARD = new CollisionBoard(CELL_SIZE);
         Main.INFLUENCE_MAP = new InfluenceMap(Main.MAP_WIDTH, Main.MAP_HEIGHT, 30, 40);
         Control_MANAGER = new ControlManager(this);
         PLANNING_GRID = new PlanningGrid(50, 50, 100, 400);
         PIPELINE = new Pipeline();
        REGISTRY = new EntityRegistry();
         if (FULL_SCREEN) System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
 
     }
 
     public void start() {
         // Set up the display
         try {
             Display.setDisplayMode(new DisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT));
             Display.setFullscreen(false);
             Display.create();
 
             GL11.glMatrixMode(GL11.GL_PROJECTION);
             GL11.glLoadIdentity();
             GL11.glOrtho(0, SCREEN_WIDTH, 0, SCREEN_HEIGHT, 1, -1);
             GL11.glMatrixMode(GL11.GL_MODELVIEW);
             GL11.glEnable(GL11.GL_BLEND);
             //GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
             GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
             BLUEPRINT_REGISTRY.calculateUpgrades();
             UIManager = new UIManager("button");
             initTWL();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         Thread physics = new Thread(Main.COLLISION_BOARD);
         physics.setDaemon(true);
         physics.setName("Collision Detection");
         physics.start();
 
         Thread loop = new Thread(GAME_LOOP);
         loop.setDaemon(true);
         loop.setName("Game Loop");
         loop.start();
 
         Thread influence = new Thread(INFLUENCE_MAP);
         influence.setDaemon(true);
         influence.setName("Influence Thread");
         influence.start();
 
         Thread planning = new Thread(PLANNING_GRID);
         planning.setDaemon(true);
         planning.setName("Planning Grid Thread");
         planning.start();
 
         for (Factions factions : Factions.values()) {
             GAME_LOOP.addFaction(factions.getFaction());
         }
 
         /**
          * Main render loop
          */
         while (!Display.isCloseRequested()) {
             try {
                 Thread.sleep(20);
             } catch (Exception e) {
 
             }
 
             // Keyboard release
             if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                 break;
             }
             UIManager.update(HUMAN_FACTION);
 
             GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
             GL11.glTranslated(viewLocation.x, viewLocation.y, 0);
 
             Main.GAME_LOOP.draw();
             INFLUENCE_MAP.draw();
 
             GL11.glTranslated(-viewLocation.x, -viewLocation.y, 0);
             drawBoundary();
             gui.update();
             Display.update();
             Control_MANAGER.update();
         }
         Display.destroy();
     }
 
     // TODO Get this working
     private void drawBoundary() {
         GL11.glColor4f(255, 255, 255, 255);
 
         // Left
         GL11.glBegin(GL11.GL_LINE);
         GL11.glVertex2d(0, 0);
         GL11.glVertex2d(10, Main.MAP_HEIGHT);
         GL11.glEnd();
 
         // Top
         GL11.glBegin(GL11.GL_LINE);
         GL11.glVertex2d(0, 0);
         GL11.glVertex2d(Main.MAP_WIDTH, 10);
         GL11.glEnd();
 
         // Bottom
         GL11.glBegin(GL11.GL_LINE);
         GL11.glVertex2d(0, Main.MAP_HEIGHT);
         GL11.glVertex2d(Main.MAP_WIDTH, Main.MAP_HEIGHT);
         GL11.glEnd();
 
         // Right
         GL11.glBegin(GL11.GL_LINE);
         GL11.glVertex2d(Main.MAP_WIDTH, 0);
         GL11.glVertex2d(Main.MAP_WIDTH, Main.MAP_HEIGHT);
         GL11.glEnd();
     }
 
     public static void main(String[] args) {
         new Main().start();
     }
 
     /**
      * Switches the game from a paused state to an un-paused state or vice visa
      */
     public void togglePause() {
         paused = !paused;
 
         // Set the loops to do work
         COLLISION_BOARD.setPaused(paused);
         GAME_LOOP.setPaused(paused);
         INFLUENCE_MAP.setPaused(paused);
         UIManager.showInput = paused;
         Control_MANAGER.paused = paused;
     }
 
     public void shiftView(Vector2D shiftAmount) {
         Main.viewLocation.add(shiftAmount);
     }
 
     private void initTWL() {
         try {
             renderer = new LWJGLRenderer();
             themeManager = ThemeManager.createThemeManager(new File("Content/UITheme/simple.xml").toURL(), renderer);
 
         } catch (LWJGLException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         gui = new GUI(UIManager, renderer);
         gui.applyTheme(themeManager);
     }
 
 }
