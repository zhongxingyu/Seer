 package kniemkiewicz.jqblocks;
 
 import de.matthiasmann.twl.GUI;
 import kniemkiewicz.jqblocks.ingame.MainGameState;
 import kniemkiewicz.jqblocks.ingame.controller.EndGameController;
 import kniemkiewicz.jqblocks.ingame.controller.SaveGameListener;
 import kniemkiewicz.jqblocks.ingame.ui.renderer.Image;
 import kniemkiewicz.jqblocks.twl.BasicTWLGameState;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanDefinitionStoreException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.support.StaticApplicationContext;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.util.Map;
 
 public class SpringMainGameStateAdaptor extends BasicTWLGameState implements ApplicationContextAware {
 
   private static final Log logger = LogFactory.getLog(SpringMainGameStateAdaptor.class);
 
   public static final String gameContextPath = "context/ingame.xml";
 
   ApplicationContext mainApplicationContext;
 
   ApplicationContext stateApplicationContext;
 
   EndGameController endGameController;
 
   MainGameState gameState;
 
   @Override
   public int getID() {
     return 0;
   }
 
   @Override
   public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
     MainGameState.Settings gameSettings = new MainGameState.Settings();
    gameSettings.seed = 1L;
     initInternal(gameContainer, stateBasedGame, gameSettings);
   }
 
   void initInternal(GameContainer gameContainer, StateBasedGame stateBasedGame, MainGameState.Settings gameSettings) throws SlickException {
     // This a fix for some stupid idea-spring conflict on Linux
     while (stateApplicationContext == null) {
       try {
         stateApplicationContext = getContext(mainApplicationContext, new String[]{gameContextPath}, new Object[]{});
       } catch (BeanDefinitionStoreException e) {
         logger.error("???", e);
       }
     }
     gameState = stateApplicationContext.getBean(MainGameState.class);
     gameState.setSettings(gameSettings);
     endGameController = stateApplicationContext.getBean(EndGameController.class);
     gameContainer.getInput().removeAllListeners();
     System.gc();
     gameState.init(gameContainer, stateBasedGame);
   }
 
   public static ApplicationContext getContext(ApplicationContext parent, String[] contextPath, Object[] staticBeans) {
     StaticApplicationContext staticContext = new StaticApplicationContext(parent);
     for (Object bean : staticBeans) {
       staticContext.getDefaultListableBeanFactory().registerSingleton(bean.getClass().getName(), bean);
     }
     staticContext.refresh();
     return new ClassPathXmlApplicationContext(contextPath, true, staticContext);
   }
 
   @Override
   public void onGuiInit(GUI gui) {
     Map<String, Image> imageMap = stateApplicationContext.getBeansOfType(Image.class);
     for (Image image : imageMap.values()) {
       image.init(gui);
     }
   }
 
   @Override
   public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
     gameState.render(gameContainer, stateBasedGame, graphics);
   }
 
   @Override
   public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int delta) throws SlickException {
     if (endGameController.isGameShouldRestart()) {
       MainGameState.Settings settings = new MainGameState.Settings();
       try {
         if (endGameController.isGameShouldBeLoaded()) {
           FileInputStream input = new FileInputStream(SaveGameListener.FILENAME);
           BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
           ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream);
           settings.savegame = objectInputStream;
         }
         initInternal(gameContainer, stateBasedGame, settings);
       } catch (IOException e) {
         throw new RuntimeException(e);
       }
 
     }
     if (endGameController.isGameShouldEnd()) {
       gameContainer.exit();
     }
     gameState.update(gameContainer, stateBasedGame, delta);
   }
 
   @Override
   public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
     this.mainApplicationContext = applicationContext;
   }
 
   @Override
   protected void createRootPane() {
     gameState.createRootPane();
     rootPane = gameState.getRootPane();
   }
 }
