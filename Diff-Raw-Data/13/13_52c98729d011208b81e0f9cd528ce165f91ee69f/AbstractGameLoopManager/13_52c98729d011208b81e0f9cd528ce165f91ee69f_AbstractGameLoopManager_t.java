 /*
  * 
  */
 package forscene.system.managers;
 
 import java.util.ArrayList;
 
 import playn.core.GroupLayer;
 import playn.core.PlayN;
 import forscene.core.entities.AbstractScene;
 import forscene.core.entities.AbstractSceneGroup;
 import forscene.core.entities.AbstractSceneObjectGroup;
 import forscene.core.objects.DefaultSceneGroup;
 import forscene.core.objects.NullAbstractSceneObject;
 import forscene.system.Asserts;
 import forscene.system.entities.ForSceneConfigurator;
 import forscene.system.events.DrawSceneEvent;
 import forscene.system.events.InitEvent;
 import forscene.system.events.LoadSceneEvent;
 import forscene.system.events.LoadSceneGroupEvent;
 import forscene.system.events.NextEvent;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AbstractGameLoopManager. It's a Singlethon class. It is true core
  * of this library because it manages game loop.
  * 
  */
 public abstract class AbstractGameLoopManager implements IGameLoopManager {
 
   /** Singlethon instance. */
   private static AbstractGameLoopManager instance         = null;
 
   /**
    * DebugMode: if you set true a useless layer will be added to the game that
    * show ticks in the upper-left corner.
    */
   private boolean                        DEBUGMODE        = true;
 
   /** currentScene: reference to current scene. */
   private AbstractScene                  currentScene;
 
   /** prevScene: reference to previous scene. */
   private AbstractScene                  prevScene;
 
   /** currentSceneGroup: reference to current sceneGroup. */
   private AbstractSceneGroup             currentSceneGroup;
 
   /** sceneGroups: container of all sceneGroups. */
   private ArrayList<AbstractSceneGroup>  sceneGroups;
 
   /** seconds: seconds elapsed from starting game. */
   private long                           seconds;
 
   /**
    * ticks: ticks elapsed from starting game. Tick is a call of update function
    * ( inherited from PlayN )
    */
   private long                           ticks            = 0;
 
   /**
    * ms: milliseconds elapsed from starting game. It's calculated from alpha
    * passed by PlayN.
    */
   private float                          ms               = 0;
 
   /** tickRate: tick per seconds. It's similar to fps. */
   private short                          tickRate         = 25;
 
   /** The start timer. */
   private boolean                        startTimer       = false;
 
   /** The current time timer. */
   private long                           currentTimeTimer = 0;
 
   // PaintManager
   /** The root. */
   private static GroupLayer              root;
 
   // Fake Scene ... used to parent
   /** The parent. */
   private AbstractSceneObjectGroup       parent           = new NullAbstractSceneObject();
 
   /**
    * Instantiates a new abstract game loop manager.
    */
   protected AbstractGameLoopManager() {
 
     sceneGroups = new ArrayList<AbstractSceneGroup>();
     EventManager.getInstance().push(new InitEvent(), (short) 0);
     AbstractGameLoopManager.root = PlayN.graphics().rootLayer();
   }
 
   /**
    * Gets the single instance of AbstractGameLoopManager.
    * 
    * @return single instance of AbstractGameLoopManager
    */
   public static IGameLoopManager getInstance() {
     if (AbstractGameLoopManager.instance == null) {
       AbstractGameLoopManager.instance = new AbstractGameLoopManager() {
       };
     }
     return AbstractGameLoopManager.instance;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getCurrentScene()
    */
   public AbstractScene getCurrentScene() {
     return currentScene;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#setCurrentScene(forscene.
    * core.entities.AbstractScene)
    */
   public void setCurrentScene(AbstractScene scene) {
     Asserts.check(scene != null, "scene can't be null");
     currentScene = scene;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getCurrentSceneGroup()
    */
   public AbstractSceneGroup getCurrentSceneGroup() {
     return currentSceneGroup;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#setCurrentSceneGroup(forscene
    * .core.entities.AbstractSceneGroup)
    */
   public void setCurrentSceneGroup(AbstractSceneGroup sceneGroup) {
     Asserts.check(sceneGroup != null, "scenegroup can't be null");
     currentSceneGroup = sceneGroup;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#addSceneGroup(forscene.core
    * .entities.AbstractSceneGroup)
    */
   public void addSceneGroup(AbstractSceneGroup sceneGroup) {
     Asserts.check(sceneGroup != null, "scenegroup can't be null");
     AbstractSceneGroup tmp = null;
     if (sceneGroups.size() > 0) {
       tmp = sceneGroups.get(sceneGroups.size() - 1);
       sceneGroup.setPrev(tmp);
       tmp.setNext(sceneGroup);
       sceneGroups.set(sceneGroups.size() - 1, tmp);
     }// else setCurrentSceneGroup(sceneGroup);
     sceneGroups.add(sceneGroup);
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getNextSceneGroup()
    */
   public AbstractSceneGroup getNextSceneGroup() {
     if (currentSceneGroup == null) {
       return null;
     }
     return currentSceneGroup.getNext();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getNextScene()
    */
   public AbstractScene getNextScene() {
     if (currentScene == null) {
       return null;
     }
     return currentScene.getNext();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getSceneGroups()
    */
   public ArrayList<AbstractSceneGroup> getSceneGroups() {
     return sceneGroups;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#loadSceneGroup(forscene.core
    * .entities.AbstractSceneGroup)
    */
   public void loadSceneGroup(AbstractSceneGroup sceneGroup) {
     Asserts.check(sceneGroup != null, "scenegroup can't be null");
     if (sceneGroup == null) {
       return;
     }
     currentSceneGroup = sceneGroup;
     currentSceneGroup.chain(currentSceneGroup.build());
     prevScene = currentScene;
     currentScene = null;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#loadScene(forscene.core.entities
    * .AbstractScene)
    */
   public void loadScene(AbstractScene scene) {
     Asserts.check(scene != null, "scene can't be null");
     if (scene == null) {
       scene = currentSceneGroup.getFirstScene();
     } else {
       PlayN.log().debug("loadscene build/buildchild");
       scene.systemBuild();
       // currentScene = scene;
     }
     if (currentScene != null) {
       prevScene = currentScene;
     }
     currentScene = scene;
     draw(scene);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#removeSceneGroup(forscene
    * .core.entities.AbstractSceneGroup)
    */
   public void removeSceneGroup(AbstractSceneGroup sceneGroup) {
     Asserts.check(sceneGroup != null, "scenegroup can't be null");
     if (sceneGroup == null) {
       throw new NullPointerException();
     }
     sceneGroups.remove(sceneGroups);
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#setDebugMode(boolean)
    */
   /**
    * Sets the debug mode.
    * 
    * @param debug
    *          the new debug mode
    */
   public void setDebugMode(boolean debug) {
     DEBUGMODE = debug;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#isDebugMode()
    */
   /**
    * Checks if is debug mode.
    * 
    * @return true, if is debug mode
    */
   public boolean isDebugMode() {
     return DEBUGMODE;
   }
 
   // PaintMan
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getRoot()
    */
   public GroupLayer getRoot() {
     return AbstractGameLoopManager.root;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#setRoot(playn.core.GroupLayer
    * )
    */
   public void setRoot(GroupLayer root) {
     Asserts.check(root != null, "root can't be null");
     AbstractGameLoopManager.root = root;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#setSize(int, int)
    */
   public void setSize(int width, int height) {
     PlayN.graphics().setSize(width, height);
   }
 
   // Time Monitor
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#setSeconds(long)
    */
   public void setSeconds(long seconds) {
     this.seconds = seconds;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#setCurrentTimeTimer(long)
    */
   public void setCurrentTimeTimer(long currentTimeTimer) {
     this.currentTimeTimer = currentTimeTimer;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getSeconds()
    */
   public long getSeconds() {
     return seconds;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getCurrentTimeTimer()
    */
   public long getCurrentTimeTimer() {
     return currentTimeTimer;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#incTicks()
    */
   public void incTicks() {
     ticks++;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#resetSeconds()
    */
   public void resetSeconds() {
     seconds = 0;
   }
 
   // =======
 
   // most complex .................
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#goNext()
    */
   public void goNext() {
     if (currentScene == null) {
       if (currentSceneGroup == null) {
         if (sceneGroups.size() > 0) {
           if (sceneGroups.get(0) == null) {
             return;
           }
           EventManager.getInstance().push(
               new LoadSceneGroupEvent(sceneGroups.get(0)),
               ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
         }
       } else // if (currentSceneGroup != null )
       {
         EventManager.getInstance().push(
             new LoadSceneEvent(currentSceneGroup.getFirstScene()),
             ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
       }
     } else // (currentScene != null)
     {
       if (currentScene.hasNext()) // switch scene
       {
         EventManager.getInstance().push(
             new LoadSceneEvent(currentScene.getNext()),
             ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
       } else // switch sceneGroup
       {
         PlayN.log().debug("GoScene !currentScene.hasNext() ...");
         if (currentSceneGroup.hasNext()) {
           EventManager.getInstance().push(
               new LoadSceneGroupEvent(currentSceneGroup.getNext()),
               ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
         }
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#draw(forscene.core.entities
    * .AbstractScene)
    */
   public void draw(AbstractScene scene) {
     Asserts.check(scene != null, "scene can't be null");
     if ((scene != null) && (currentScene != scene)) {
       if (scene.getRoot() != null) {
         getRoot().clear();
         getRoot().add(scene.getRoot());
         scene.setParent(parent);
       }
     }
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#paint()
    */
   public void paint() {
     // ATTENZIONE ELIMINATO MOMENTANEAMENTE
     if (currentScene != null) {
       EventManager.getInstance().push(new DrawSceneEvent(currentScene));
     }
   }
 
   // =======
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#updateState()
    */
   public void updateState() {
     EventManager.getInstance().update();
     if (currentScene != null) {
 
       if (currentScene.isReadyToSwitch()) {
         EventManager.getInstance().push(new NextEvent(),
             ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
       }
 
       if (currentScene.hasTimeout()) {
         if (startTimer == false) {
           startTimer = true;
           currentTimeTimer = getSeconds();
         }
 
         if ((startTimer)
             && ((getSeconds() - currentTimeTimer) >= currentScene.getTimeout())) {
           EventManager.getInstance().push(new NextEvent(),
               ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
           startTimer = false;
         }
       }
     } else {
       if (EventManager.getInstance().getEvents().isEmpty()) {
         EventManager.getInstance().push(new NextEvent(),
             ForSceneConfigurator.EVENT_MANAGER_DEFAULT_EVENT_SYSTEM_PRIORITY);
       }
     }
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#init()
    */
   public void init() {
    PlayN.log().debug("glc.Init");
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getTicks()
    */
   public long getTicks() {
     return ticks;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getTickRate()
    */
   public short getTickRate() {
     return tickRate;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see
    * forscene.core.LoopController.IGameLoopManager#addScene(forscene.core.entities
    * .AbstractScene)
    */
   public void addScene(AbstractScene scene) {
     Asserts.check(scene != null, "scene can't be null");
     DefaultSceneGroup.getInstance().addScene(scene);
     if (getSceneGroups().contains(DefaultSceneGroup.getInstance())) {
       return;
     }
     addSceneGroup(DefaultSceneGroup.getInstance());
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#incTime(float)
    */
   public void incTime(float delta) {
     ms += delta;
     if (ms > 1000) {
       seconds++;
       ms -= 1000;
     }
 
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getHeight()
    */
   public float getHeight() {
     return PlayN.graphics().height();
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see forscene.core.LoopController.IGameLoopManager#getWidth()
    */
   public float getWidth() {
     return PlayN.graphics().width();
   }
 
   /**
    * Redraw.
    */
   public void redraw() {
     AbstractScene tmp = getCurrentScene();
     if (tmp != null) {
       getRoot().clear();
       getRoot().add(tmp.getRoot());
     }
     PlayN.log().debug("redraw");
   }
 
 }
