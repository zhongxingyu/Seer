 package mclauncher;
 
 import mclauncher.PathDecodes.IPathDecode;
 import mclauncher.PathDecodes.PathDecodeFactory;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import javax.swing.*;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Random;
 
 /**
  * Created with IntelliJ IDEA.
  * User: martin
  * Date: 7/10/13
  * Time: 4:43 PM
  */
 
 public class Main {
 
     private static Logger logger = Logger.getLogger(Main.class);
 
     public static void main(String[] args) {
 
         init();
 
         final MainGUI dialog = new MainGUI();
 
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                dialog.setTitle("MCLauncher v1.0.0 by osiutino");
                 dialog.pack();
                 dialog.setLocationRelativeTo(null);
                 dialog.setVisible(true);
             }
         });
 
 
     }
 
     private static void init(){
 
         PropertyConfigurator.configure("setting.properties");
 
         logger.info("-------------- MC launcher -------------");
 
         String homeDir = System.getProperty("user.home");
         String osName = System.getProperty("os.name");
         logger.info( "homeDir=".concat(homeDir) );
         logger.info( "osName=".concat(osName) );
 
         pathDecode = PathDecodeFactory.create(osName,homeDir);
 
         loadConfigs();
     }
 
     private static void loadConfigs() {
 
         //java environment configuration
         Properties javaProps = new Properties();
 
         try {
             javaProps.load(new FileInputStream("env.properties"));
 
             javaConfig.setJava( pathDecode.Apply( javaProps.getProperty("java")) );
             javaConfig.setXms(  pathDecode.Apply(javaProps.getProperty("Xms")) );
             javaConfig.setXmx( pathDecode.Apply(javaProps.getProperty("Xmx")) );
             javaConfig.setJava_library_path( pathDecode.Apply(javaProps.getProperty("java.library.path")) );
             javaConfig.setClasspath( pathDecode.Apply(javaProps.getProperty("classpath")) );
 
         }catch (IOException e) {
             e.printStackTrace();
         }
 
         //game enviroment configuration
         Properties gameProps = new Properties();
 
         try {
             gameProps.load(new FileInputStream("game.properties"));
 
             Random random = new Random(System.currentTimeMillis());
             gameConfig.setSession(String.valueOf(random.nextInt()));
             gameConfig.setGameDir( pathDecode.Apply(gameProps.getProperty("gameDir")) );
             gameConfig.setAssetsDir( pathDecode.Apply(gameProps.getProperty("assetsDir")) );
             gameConfig.setVersion(gameProps.getProperty("version"));
 
         } catch (IOException e) {
             e.printStackTrace();
 
         }
 
         printConfigs();
     }
 
     private static void printConfigs(){
 
         logger.info("------------ Print Configs --------------");
 
         logger.info("java=".concat(String.valueOf(javaConfig.getJava())));
         logger.info("Xms=".concat(String.valueOf(javaConfig.getXms())));
         logger.info("Xmx=".concat(String.valueOf(javaConfig.getXmx())));
         logger.info("java.library.path=".concat(String.valueOf(javaConfig.getJava_library_path())));
         logger.info("classpath=".concat(String.valueOf(javaConfig.getClasspath())));
         logger.info("session=".concat(String.valueOf(gameConfig.getSession())));
         logger.info("username=".concat(String.valueOf(gameConfig.getUserName())));
         logger.info("gameDir=".concat(String.valueOf(gameConfig.getGameDir())));
         logger.info("assetsDir=".concat(String.valueOf(gameConfig.getAssetsDir())));
         logger.info("version=".concat(String.valueOf(gameConfig.getVersion())));
 
     }
 
 
     public static void RunGame(){
         GameExecuter gameExecuter = new GameExecuter(javaConfig, gameConfig);
         gameExecuter.run();
     }
 
 
     public static JavaConfig javaConfig = new JavaConfig();
     public static GameConfig gameConfig = new GameConfig();
     private static IPathDecode pathDecode;
 }
