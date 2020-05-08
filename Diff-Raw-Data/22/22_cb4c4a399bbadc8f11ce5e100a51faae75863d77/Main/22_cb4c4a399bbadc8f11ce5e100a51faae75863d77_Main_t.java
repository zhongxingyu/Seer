 package org.lehirti.engine;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.concurrent.BlockingQueue;
 
 import javax.swing.JFrame;
 
 import org.lehirti.engine.events.Event;
 import org.lehirti.engine.gui.ImageArea;
 import org.lehirti.engine.gui.OptionArea;
 import org.lehirti.engine.gui.StatsArea;
 import org.lehirti.engine.gui.TextArea;
 import org.lehirti.engine.res.ResourceCache;
 import org.lehirti.engine.res.images.CommonImage;
 import org.lehirti.engine.res.images.ImageKey;
 import org.lehirti.engine.res.text.CommonText;
 import org.lehirti.engine.state.BoolState;
 import org.lehirti.engine.state.IntState;
 import org.lehirti.engine.state.State;
 import org.lehirti.engine.state.StaticInitializer;
 import org.lehirti.engine.state.StringState;
 import org.lehirti.engine.util.ClassFinder;
 import org.lehirti.engine.util.LogUtils;
 import org.lehirti.engine.util.PathFinder;
 import org.lehirti.engine.util.PropertyUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class Main {
   static {
     final File logsDir = new File("logs");
     if (!logsDir.exists() && !logsDir.mkdirs()) {
       System.err.println("Could not create logs dir " + logsDir.getAbsolutePath());
     }
     LogUtils.createDefaultLogConfigFileIfMissing();
     System.setProperty("java.util.logging.config.file", "config/logging.properties");
   }
   
   private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
   
   public static JFrame MAIN_WINDOW;
   private static KeyListener KEY_LISTENER = new GameKeyListener();
   
   private static final double SCREEN_X = 64.0;
   private static final double SCREEN_Y = 48.0;
   
   public static TextArea TEXT_AREA;
   public static ImageArea IMAGE_AREA;
   public static OptionArea OPTION_AREA;
   public static StatsArea STATS_AREA;
   
   public static TextArea INVENTORY_TEXT_AREA;
   public static ImageArea INVENTORY_IMAGE_AREA;
   public static OptionArea INVENTORY_OPTION_AREA;
   
   private static volatile ImageArea currentImageArea = null;
   private static volatile TextArea currentTextArea = null;
   private static volatile StatsArea currentStatsArea = null;
   private static volatile OptionArea currentOptionsArea = null;
   
   public static boolean IS_DEVELOPMENT_VERSION = false;
   
   private static volatile Event<?> currentEvent = null;
   private static volatile Event<?> currentInventoryEvent = null;
   
   private void createAndShowGUI() {
     
     Main.MAIN_WINDOW = new JFrame(getGameName());
     MAIN_WINDOW.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     MAIN_WINDOW.getContentPane().setLayout(new GridBagLayout());
     
     TEXT_AREA = new TextArea(SCREEN_X, SCREEN_Y, 16.0, 36.0);
     TEXT_AREA.addKeyListener(KEY_LISTENER);
     IMAGE_AREA = new ImageArea(SCREEN_X, SCREEN_Y, 48.0, 36.0);
     STATS_AREA = new StatsArea(SCREEN_X, SCREEN_Y, 64.0, 6.0);
     OPTION_AREA = new OptionArea(SCREEN_X, SCREEN_Y, 64.0, 6.0, 4, 3);
     
     INVENTORY_TEXT_AREA = new TextArea(SCREEN_X, SCREEN_Y, 16.0, 36.0);
     INVENTORY_TEXT_AREA.addKeyListener(KEY_LISTENER);
     INVENTORY_TEXT_AREA.setVisible(false);
     INVENTORY_IMAGE_AREA = new ImageArea(SCREEN_X, SCREEN_Y, 48.0, 36.0);
     INVENTORY_IMAGE_AREA.setVisible(false);
     INVENTORY_OPTION_AREA = new OptionArea(SCREEN_X, SCREEN_Y, 64.0, 6.0, 4, 3);
     INVENTORY_OPTION_AREA.setVisible(false);
     
     INVENTORY_IMAGE_AREA.setBackgroundImage(CommonImage.INVENTORY_BACKGROUND);
     INVENTORY_TEXT_AREA.setText(ResourceCache.get(CommonText.INVENTORY));
     
     GridBagConstraints c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 0;
     c.gridwidth = 1;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(IMAGE_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 1;
     c.gridy = 0;
     c.gridwidth = 1;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(TEXT_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 1;
     c.gridwidth = 2;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(STATS_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 2;
     c.gridwidth = 2;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(OPTION_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 0;
     c.gridwidth = 1;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(INVENTORY_IMAGE_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 1;
     c.gridy = 0;
     c.gridwidth = 1;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(INVENTORY_TEXT_AREA, c);
     
     c = new GridBagConstraints();
     c.gridx = 0;
     c.gridy = 2;
     c.gridwidth = 2;
     c.gridheight = 1;
     MAIN_WINDOW.getContentPane().add(INVENTORY_OPTION_AREA, c);
     
     setCurrentImageArea(IMAGE_AREA);
     setCurrentTextArea(TEXT_AREA);
     setCurrentStatsArea(STATS_AREA);
     setCurrentOptionArea(OPTION_AREA);
     
     MAIN_WINDOW.validate();
     MAIN_WINDOW.repaint();
     
     MAIN_WINDOW.pack();
     MAIN_WINDOW.setVisible(true);
   }
   
   protected static void loadGame() {
     final File sav = new File("save"); // TODO save file name
     FileInputStream fis = null;
     ObjectInputStream ois = null;
     try {
       if (currentEvent == null || !currentEvent.isLoadSavePoint()) {
         LOGGER.warn("Another game cannot be loaded right now"); // better notification for user
         return;
       }
       
       fis = new FileInputStream(sav);
       ois = new ObjectInputStream(fis);
       State.load(ois);
       IMAGE_AREA.readExternal(ois);
       TEXT_AREA.readExternal(ois);
       STATS_AREA.readExternal(ois);
       OPTION_AREA.readExternal(ois);
       OPTION_AREA.repaint();
       INVENTORY_IMAGE_AREA.readExternal(ois);
       INVENTORY_TEXT_AREA.readExternal(ois);
       INVENTORY_OPTION_AREA.readExternal(ois);
       final Event<?> oldEvent = currentEvent;
       currentEvent = (Event<?>) ois.readObject();
       currentInventoryEvent = (Event<?>) ois.readObject();
       synchronized (oldEvent) {
         oldEvent.newEventHasBeenLoaded();
         oldEvent.notifyAll();
       }
       
       LOGGER.info("Game loaded");
     } catch (final FileNotFoundException e) {
       LOGGER.error("Savegame " + sav.getAbsolutePath() + " not found for loading", e);
     } catch (final IOException e) {
       LOGGER.error("IOException tryting to load from " + sav.getAbsolutePath(), e);
     } catch (final ClassNotFoundException e) {
       LOGGER.error("Savegame " + sav.getAbsolutePath() + " incompatible with current program version.", e);
     } finally {
       if (ois != null) {
         try {
           ois.close();
         } catch (final IOException e) {
           LOGGER.warn("Failed to close object input stream for " + sav.getAbsolutePath(), e);
         }
       }
       if (fis != null) {
         try {
           fis.close();
         } catch (final IOException e) {
           LOGGER.warn("Failed to close file input stream for " + sav.getAbsolutePath(), e);
         }
       }
     }
   }
   
   protected static void saveGame() {
     final File sav = new File("save"); // TODO save file name
     FileOutputStream fos = null;
     ObjectOutputStream oos = null;
     try {
       if (currentEvent == null || !currentEvent.isLoadSavePoint()) {
         LOGGER.warn("The game cannot be saved right now"); // better notification for user
         return;
       }
       
       fos = new FileOutputStream(sav);
       oos = new ObjectOutputStream(fos);
       State.save(oos);
       IMAGE_AREA.writeExternal(oos);
       TEXT_AREA.writeExternal(oos);
       STATS_AREA.writeExternal(oos);
       OPTION_AREA.writeExternal(oos);
       INVENTORY_IMAGE_AREA.writeExternal(oos);
       INVENTORY_TEXT_AREA.writeExternal(oos);
       INVENTORY_OPTION_AREA.writeExternal(oos);
       oos.writeObject(currentEvent);
       oos.writeObject(currentInventoryEvent);
       
       LOGGER.info("Game saved");
     } catch (final FileNotFoundException e) {
       LOGGER.error("Savegame " + sav.getAbsolutePath() + " not found for saving", e);
     } catch (final IOException e) {
       LOGGER.error("IOException tryting to save to " + sav.getAbsolutePath(), e);
     } finally {
       if (oos != null) {
         try {
           oos.close();
         } catch (final IOException e) {
           LOGGER.warn("Failed to close object output stream for " + sav.getAbsolutePath(), e);
         }
       }
       if (fos != null) {
         try {
           fos.close();
         } catch (final IOException e) {
           LOGGER.warn("Failed to close file output stream for " + sav.getAbsolutePath(), e);
         }
       }
     }
   }
   
   public static synchronized Event<?> getCurrentEvent() {
     if (currentImageArea == IMAGE_AREA) {
       return Main.currentEvent;
     }
     return Main.currentInventoryEvent;
   }
   
   public static synchronized void setCurrentEvent(final Event<?> event) {
     if (currentImageArea == IMAGE_AREA) {
       Main.currentEvent = event;
     } else {
       Main.currentInventoryEvent = event;
     }
   }
   
   public static TextArea getCurrentTextArea() {
     return currentTextArea;
   }
   
   public static ImageArea getCurrentImageArea() {
     return currentImageArea;
   }
   
   public static StatsArea getCurrentStatsArea() {
     return currentStatsArea;
   }
   
   public static OptionArea getCurrentOptionArea() {
     return currentOptionsArea;
   }
   
   public static void setCurrentTextArea(final TextArea textArea) {
     if (textArea == TEXT_AREA) {
       INVENTORY_TEXT_AREA.setVisible(false);
     } else {
       TEXT_AREA.setVisible(false);
     }
     textArea.setVisible(true);
    textArea.requestFocusInWindow();
     currentTextArea = textArea;
   }
   
   public static void setCurrentImageArea(final ImageArea imageArea) {
     if (imageArea == IMAGE_AREA) {
       INVENTORY_IMAGE_AREA.setVisible(false);
     } else {
       IMAGE_AREA.setVisible(false);
     }
     imageArea.setVisible(true);
     currentImageArea = imageArea;
   }
   
   public static void setCurrentStatsArea(final StatsArea statsArea) {
     currentStatsArea = statsArea;
   }
   
   public static void setCurrentOptionArea(final OptionArea optionArea) {
     if (optionArea == OPTION_AREA) {
       INVENTORY_OPTION_AREA.setVisible(false);
     } else {
       OPTION_AREA.setVisible(false);
     }
     optionArea.setVisible(true);
     currentOptionsArea = optionArea;
   }
   
   protected void engineMain(final String[] args) throws InterruptedException, InvocationTargetException {
     boolean exportDefaults = false;
     for (final String arg : args) {
       if (arg.equals("--exportDefaults")) {
         exportDefaults = true;
       }
     }
     
     readVersion();
     
     readContent();
     
     // start background daemon thread to preload next images
     final Thread backgroundImageLoader = new Thread(new Runnable() {
       
       @Override
       public void run() {
         final BlockingQueue<ImageKey> imagesToPreload = ResourceCache.getImagesToPreload();
         try {
           while (true) {
             final ImageKey nextImageToPreload = imagesToPreload.take();
             LOGGER.info("Caching image " + nextImageToPreload.getClass().getName() + "." + nextImageToPreload.name());
             ResourceCache.cache(nextImageToPreload);
           }
         } catch (final InterruptedException e) {
           LOGGER.warn("Thread " + Thread.currentThread().getName() + " has been interrupted and is terminating.", e);
           return;
         }
       }
     }, "BackgroundImageLoader");
     backgroundImageLoader.setDaemon(true);
     backgroundImageLoader.start();
     
     /*
      * load all modules
      */
     final Vector<Class<?>> modules = new ClassFinder().findSubclasses(StaticInitializer.class.getName());
     for (final Class<?> module : modules) {
       LOGGER.debug("Loaded module: {}", module.getName());
     }
     
     Vector<Class<?>> states = new ClassFinder().findSubclasses(IntState.class.getName());
     for (final Class<?> intState : states) {
       if (intState.isEnum()) {
         State.initIntDefaults((Class<IntState>) intState);
         if (exportDefaults) {
           PropertyUtils.setDefaultProperties((Class<IntState>) intState, PropertyUtils
               .getDefaultProperties((Class<IntState>) intState));
         }
       }
     }
     
     states = new ClassFinder().findSubclasses(StringState.class.getName());
     for (final Class<?> stringState : states) {
       if (stringState.isEnum()) {
         State.initStringDefaults((Class<StringState>) stringState);
         if (exportDefaults) {
           PropertyUtils.setDefaultProperties((Class<StringState>) stringState, PropertyUtils
               .getDefaultProperties((Class<StringState>) stringState));
         }
       }
     }
     
     states = new ClassFinder().findSubclasses(BoolState.class.getName());
     for (final Class<?> boolState : states) {
       if (boolState.isEnum()) {
         State.initBoolDefaults((Class<BoolState>) boolState);
         if (exportDefaults) {
           PropertyUtils.setDefaultProperties((Class<BoolState>) boolState, PropertyUtils
               .getDefaultProperties((Class<BoolState>) boolState));
         }
       }
     }
     
     javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
       public void run() {
         createAndShowGUI();
       }
     });
     
     LOGGER.info("{} started.", getGameName());
     
     while (true) {
       getCurrentEvent().execute();
     }
   }
   
   private void readVersion() {
     final InputStream versionInputStream = ClassLoader.getSystemResourceAsStream(PathFinder
         .getLocationOfVerionsFileOnClasspath());
     final Properties versionProps = new Properties();
     try {
       versionProps.load(versionInputStream);
     } catch (final Exception e) {
       LOGGER.info(getGameName() + " Development Version");
       IS_DEVELOPMENT_VERSION = true;
       return;
     }
     
     LOGGER.info(getGameName() + " " + versionProps.getProperty("flavor") + " Build "
         + versionProps.getProperty("build") + " " + versionProps.getProperty("date"));
   }
   
   abstract protected void readContent();
   
   abstract protected String getGameName();
 }
