 package com.voracious.dragons.client;
 
 import java.applet.Applet;
 import java.awt.Frame;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import com.voracious.dragons.client.utils.InputHandler;
 
 public class Main extends Applet implements WindowListener {
     private static final long serialVersionUID = 1L;
 
     public static final String logfile = "client.log";
 
     private static Logger logger;
     private static Game game;
 
     public static void main(String[] args) {
         Frame window = new Frame(Game.NAME);
 
         initLog4J();
         logger.setLevel(Level.INFO);
         if (args.length > 0) {
             if (args[0].equals("--debug")) {
                 logger.setLevel(Level.ALL);
             }
         }
         
         window.addWindowListener(new Main());
         
         game = new Game();
         
         InputHandler ih = new InputHandler();
         game.addKeyListener(ih);
         game.addMouseListener(ih);
         game.addMouseMotionListener(ih);
         
         game.init();
         window.add(game);
         
         try {
             window.setIconImage(ImageIO.read(Main.class.getResourceAsStream("/icon.png")));
         } catch (IOException e) {
             logger.error("Could not load icon image", e);
         }
 
         window.setLocationRelativeTo(null);
         window.setResizable(false);
         window.pack();
         window.setVisible(true);
 
         game.start();
     }
 
     @Override
     public void windowClosing(WindowEvent arg0) {
         game.stop();
         System.exit(0);
     }
 
     @Override
     public void init() {
         initLog4J();
         logger.setLevel(Level.ALL);
 
         game = new Game();
         
         InputHandler ih = new InputHandler();
         game.addKeyListener(ih);
         game.addMouseListener(ih);
         game.addMouseMotionListener(ih);
         
         this.add(game);
         game.init();
         
         this.requestFocus();
     }
 
     @Override
     public void start() {
         game.start();
     }
 
     @Override
     public void stop() {
         game.stop();
     }
 
     public static void initLog4J() {
         logger = Logger.getLogger(Main.class);
 
         Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n")));
         try {
             Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout("%-4r [%t] %-5p %c %x - %m%n"), logfile));
         } catch (IOException e) {
             logger.error("Could not write log file", e);
         }
     }
 
     @Override
     public void windowClosed(WindowEvent arg0) {
     }
 
     @Override
     public void windowActivated(WindowEvent arg0) {
     }
 
     @Override
     public void windowDeactivated(WindowEvent arg0) {
     }
 
     @Override
     public void windowDeiconified(WindowEvent arg0) {
     }
 
     @Override
     public void windowIconified(WindowEvent arg0) {
     }
 
     @Override
     public void windowOpened(WindowEvent arg0) {
     }
 }
