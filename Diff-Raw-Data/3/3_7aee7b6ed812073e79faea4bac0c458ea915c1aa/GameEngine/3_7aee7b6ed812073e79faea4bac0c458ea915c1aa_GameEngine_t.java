 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 public class GameEngine {
     JFrame window;
     JPanel area;
     HashSet<Integer> keys;
     Menu mainMenu, inGameMenu;
     boolean paused;
     boolean mainMenuDisplayed, inGameMenuDisplayed;
     LinkedList<GameObject> objList;
 
     GameEngine() {
         keys = new HashSet<Integer>();
 
         window = new JFrame();
         window.setLayout(new BorderLayout());
         window.setSize(800, 600);
         window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         area = new JDrawingArea(this);
         window.add(area, BorderLayout.CENTER);
 
         window.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent keyEvent) {
             }
 
             @Override
             public void keyPressed(KeyEvent keyEvent) {
                 keys.add(keyEvent.getKeyCode());
             }
 
             @Override
             public void keyReleased(KeyEvent keyEvent) {
                 keys.remove(keyEvent.getKeyCode());
             }
         });
 
         showMainMenu();
         objList= new LinkedList<GameObject>();
        window.setVisible(true);
     }
 
     void nextFrame(long delta, Graphics2D g) {
         if(mainMenuDisplayed)
             mainMenu.draw(delta, g);
         else if (inGameMenuDisplayed)
             inGameMenu.draw(delta, g);
         else {
             for (GameObject go:objList)
                 go.update(delta);
         }
     }
 
     int width() {
         return area.getWidth();
     }
 
     int height() {
         return area.getHeight();
     }
 
     HashSet<Integer> getKeys() {
         return keys;
     }
 
     void showMainMenu() {
         if (mainMenu == null) {
             mainMenu = new Menu(this);
             final GameEngine engine = this;
             mainMenu.addItem("Нова гра", new Runnable() {
                 @Override
                 public void run() {
                 }
             });
             mainMenu.addItem("Завантажити гру", new Runnable() {
                 @Override
                 public void run() {
                 }
             });
             mainMenu.addItem("Вийти", new Runnable() {
                 @Override
                 public void run() {
                     engine.exit();
                 }
             });
         }
         mainMenuDisplayed = true;
     }
 
     void addObject(GameObject g) {
         objList.add(g);
     }
 
     void deleteObject(GameObject g){
         objList.remove(g);
     }
 
     void pause(){
         paused = true;
         showInGameMenu();
     }
 
     void resume(){
         paused = false;
         inGameMenuDisplayed = false;
     }
     void exit(){
         window.setVisible(false);
     }
 
     void showInGameMenu() {
         inGameMenuDisplayed = true;
         final GameEngine engine = this;
         if(inGameMenu == null) {
             inGameMenu = new Menu(this);
             inGameMenu.addItem("Продовжити", new Runnable() {
                 @Override
                 public void run() {
                     engine.resume();
                 }
             });
             inGameMenu.addItem("Вийти в головне меню", new Runnable() {
                 @Override
                 public void run() {
                     engine.resume();
                     engine.showMainMenu();
                 }
             });
         }
     }
 }
