 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Main;
 
 import GameElements.DialogBox;
 import GameElements.FeedbackBox;
 import GameElements.Popup;
 import GameElements.TodoBoard;
 import static GameElements.TodoBoard.DONE;
 import static GameElements.TodoBoard.TODO;
 import Listeners.PauseListener;
 import Map.BuildLevels;
 import Map.TileMap;
 import Player.NPC;
 import Player.Player;
 import Player.BuildNPCs;
 import Settings.*;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import javax.sound.sampled.AudioSystem;
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 /**
  *
  * @author Rune
  */
 public class GamePanel extends JPanel implements Runnable {
 
     BuildNPCs buildNPC = new BuildNPCs();
     BuildLevels buildLevels = new BuildLevels();
     private Settings settings;
     private boolean running;
     private BufferedImage image;
     private int maxrameCount = 60;
     private GameStateSettings optionState;
     int frameCount = 1;
     private Thread thread;
     private Graphics2D g;
     private Player player;
     private int currentLevel;
     private ArrayList<NPC> npcs = new ArrayList<NPC>();
     private Popup popup;
     private int score = 0;
     private boolean pointsGiven = false;
     private ArrayList<TileMap> levels = new ArrayList();
     private PlayerSettings playersettings;
     private JFrame frame;
 
     public GamePanel(PlayerSettings playersettings, Settings settings,JFrame frame) {
         super();
         this.playersettings = playersettings;
         npcs = buildNPC.getLevel_one();
         levels = buildLevels.getLevels();
         this.settings = settings;
         this.popup = new Popup();
         this.frame = frame;
         setPreferredSize(new Dimension(settings.WITDH, settings.HEIGHT));
         setFocusable(true);
         requestFocus();
         if (settings.sound) {
             try {
                 settings.clip = AudioSystem.getClip();
                 settings.startMusic(this);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public void addNotify() {
         super.addNotify();
         if (thread == null) {
             thread = new Thread(this);
             thread.start();
             running = true;
         }
     }
 
     public void init() {
         image = new BufferedImage(settings.WITDH, settings.HEIGHT, BufferedImage.TYPE_INT_ARGB);
         g = (Graphics2D) image.getGraphics();
 
         //Anti-aliasing        g.
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         
         buildLevels.loadTilesLevel(levels);
         player = new Player(levels.get(0), 0, -200, 200, 5, "blue");
 
         //player.setNPCs(npcs); //må forandres når level skkiftes
         optionState = new GameStateSettings(settings);
         addKeyListener(player);
     }
 
     public void run() {
         init();
         long start, loopTime, wait;
         long targetTime = 1000 / settings.FPS;
         long totalTime = 0;
 
         //gameloop
         while (running) {
             start = System.nanoTime();
 
             update();
             render();
             draw();
 
             loopTime = (System.nanoTime() - start) / 1000000;
 
             wait = targetTime - loopTime;
             if (wait < 0) {
                 wait = 0;
             }
             try {
                 thread.sleep(wait);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             totalTime += System.nanoTime() - start;
             frameCount++;
 
             if (frameCount == maxrameCount) {
                 settings.setAvrageFPS(1000D / ((totalTime / frameCount) / 1000000));
                 totalTime = 0;
                 frameCount = 0;
             }
         }
     }
 
     public void update() {
         player.update();
         currentLevel = player.getLevel();
         if (currentLevel == 1) {
             player.updateTitleMap(levels.get(0));
         } else if (currentLevel == 2) {
             player.updateTitleMap(levels.get(1));
         }else if(currentLevel == 3){
             player.updateTitleMap(levels.get(2));
         }else if(currentLevel==4){
             player.updateTitleMap(levels.get(3));
         }
     }
 
     private String[] drawText() {
         String[] s = new String[3];
         int interaction = player.interaction();
         s[0] = "Hei! Trykk E for å snakke med meg!";
         if (interaction != -1 && interaction < s.length) {
             s[1] = ((Integer) npcs.get(interaction).getX()).toString();
             s[2] = ((Integer) npcs.get(interaction).getY()).toString();
             return s;
         }
         return null;
     }
 
     public void drawNPCs() {
         for (NPC n : npcs) {
             n.draw(g);
         }
         player.setNPCs(npcs);
     }
 
     public void render() {
         //Litt haxx for å få pauseknapper til å fungere
         MouseListener pauselistener = new PauseListener(player);
         addMouseListener(pauselistener);
         if (!player.getOptionValue()) {
             //          remove(dialogbox);
             if (!player.getInterOk()) {
                 if (player.answer != -1) {
                     popup = new DialogBox(playersettings);
                     FeedbackBox feedback = new FeedbackBox(((DialogBox)popup).question.getAnswers().get(player.answer));
                     feedback.paintComponent(g);
 
                     if (feedback.getBoolAnswer() && pointsGiven == false) {
                         score += 10;
                         pointsGiven = true;
                     }
                     
                     add(feedback);
                     if (player.confirmedFeedback) {
                         pointsGiven = false;
                         player.answer = -1;
                     }
                 } else {
                     if (currentLevel == 1) {
                         levels.get(0).draw(g);
                         npcs = buildNPC.getLevel_one();
                         drawNPCs();
                         npcs.add(buildNPC.getScrumBoard());
                      //npc1.draw(g);
                         //npc2.draw(g);
                         String[] s = drawText();
                         if (s != null) {
                             g.setColor(Color.WHITE);
                             g.fillOval(Integer.parseInt(s[1]) - 10, Integer.parseInt(s[2]) - 60, 220, 50);
                             g.setColor(Color.BLACK);
                             g.drawString(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]) - 37);
                         }
                     } else if (currentLevel == 2) {
                         levels.get(1).draw(g);
                         npcs = buildNPC.getLevel_two();
                         drawNPCs();
                         npcs.add(buildNPC.getLibary());
                         
                     }else if(currentLevel == 3){
                         levels.get(2).draw(g);
                         npcs = buildNPC.getLevel_three();
                         drawNPCs();
                     }else if(currentLevel == 4){
                         levels.get(3).draw(g);
                         npcs = buildNPC.getLevel_four();
                         drawNPCs();
                     }
                     g.setColor(Color.WHITE);
                     Font font;
                     try{
                     font = Font.createFont(Font.TRUETYPE_FONT, new File("res/font/Minecraftia.ttf"));
                     }catch (Exception e){
                         e.printStackTrace();
                     }
                     g.drawString("Poeng " + score, settings.WITDH - 150, 20);
 //                    font = new Font("DejaVu Sans", Font.PLAIN,12);
                     player.draw(g);
                 }
             } else {
                 if (!player.finishedInteractedNPCs.contains(player.interactedNPCID)) {
                     if (player.interactedNPCID==101){
                         popup = new TodoBoard();
                         setKeyBindings();
                     }else
                         popup = new DialogBox(playersettings);
                     popup.setInteractedNPCID(player.interactedNPCID);
                     popup.paintComponent(g);
                    // add(dialogbox);
                     player.setDialogBoxDrawn(true);
                 }
             }
         } else {
             optionState.draw(g);
             player.setInterOk(false);
         }
     }
     public void draw() {
         Graphics g2 = this.getGraphics();
         g2.drawImage(image, 0, 0, null);
         g2.dispose();
     }
         private void setKeyBindings() {        
         String l = "goLeft";
         getInputMap().put(KeyStroke.getKeyStroke("LEFT"), l);
         getActionMap().put(l, new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e){
                 System.out.println("goLeft actionPerformed");
                 
                 if (((TodoBoard)popup).showPart!=TODO) {
                     ((TodoBoard)popup).showPart--;
                 }              
             }
         });
         String r = "goRight";
         getInputMap().put(KeyStroke.getKeyStroke("RIGHT"),r);
         getActionMap().put(r, new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e){
                 System.out.println("goRight actionPerformed");
                 
                 if (((TodoBoard)popup).showPart!=DONE) {
                     ((TodoBoard)popup).showPart++;
                 }                
             }
         });
     }
 }
