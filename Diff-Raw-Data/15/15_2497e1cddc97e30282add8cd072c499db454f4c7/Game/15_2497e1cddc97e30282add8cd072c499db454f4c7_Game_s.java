 package irgame;
 
 import irgame.input.Keyboard;
 import irgame.object.Character;
 import irgame.object.Ground;
 import irgame.object.Obstacle;
 import irgame.sound.Sound;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.filechooser.FileSystemView;
 
 /**
  * @author Daniel Johansson
  */
 public class Game extends Canvas implements Runnable {   
     private static final long serialVersionUID = 1L;
     
     private static final String GAMETITLE = "Wüüd teh Würm";
     public static final int WIDTH = 640;
     public static final int HEIGHT = WIDTH * 9 / 16;
     
     private Thread thread;
     private JFrame frame;
     private Keyboard key;
     private boolean running = false;
     private boolean paused = false; 
     private String elapsedMilliSeconds = "1";
     private String elapsedSeconds = "1";
     private String elapsedMinutes = "1";
     
     public static final Ground[] ground = new Ground[WIDTH / 32 + 1];
     private static final Ground[] groundFill = new Ground[(WIDTH / 32 + 1) * 2];
     public static ArrayList<Obstacle> obstacle;
     public static Character chaR;
     public static Sound sound;
     public static BufferedImage bG, pausedImg, gameOverImg;
     
     public static int gravity = 4;
     private int jump = 0;
     private int level = 1;
     
     //Gets the "My Documents" path
     private final JFileChooser jfc = new JFileChooser();
     private final FileSystemView fsv = jfc.getFileSystemView();
     private final File gameDir = new File (fsv.getDefaultDirectory() + "\\" + GAMETITLE);
     private final File highScoreDir = new File(gameDir + "\\highscore.txt");
     private FileWriter fw;
     
     private boolean newHighScore = false;
     private String highScore;
     
     //Test
     long lastTime = System.nanoTime();
     long timer = System.currentTimeMillis();
     final double ns = 1000000000.0 / 60.0;
     double delta = 0;
 
     long startTimeMS = System.currentTimeMillis();
     long startTimeS = System.currentTimeMillis();
     long startTimeM = System.currentTimeMillis();
 
 
     int updates = 0;
     int frames = 0;
     
     public Game(){
         Dimension winSize = new Dimension(WIDTH, HEIGHT);
         setPreferredSize(winSize);
         
         frame = new JFrame();
         
         key = new Keyboard();
         addKeyListener(key);
     }
     
     public synchronized void start() {  //Starts the game.
         running = true;
         thread = new Thread(this, "Display");
         thread.start();
         if (!gameDir.exists()){ //Creates a directory for the game at the users "Documents"-folder if it doesn't exists.
             gameDir.mkdirs();
         }
         
         try {
             if(!highScoreDir.exists()){ //Creates a highscore.txt-file in the directory of the game if it doesn't exists.
                 fw = new FileWriter(highScoreDir);
             }
             bG = ImageIO.read(getClass().getResource("/irgame/res/textures/bg_img.png"));   //Gets the background image for the game.
             pausedImg = ImageIO.read(getClass().getResource("/irgame/res/textures/paused_img.png"));    //Gets the image showed when the game is paused.
             gameOverImg = ImageIO.read(getClass().getResource("/irgame/res/textures/game_over_img.png"));   //Gets the image showed when the game lost.
         } catch (IOException ex) {
             Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
     
     public synchronized void stop() {   //Stops the game.
         running = false;
         try {
             thread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
     
     public void run() {
         initialize();   //Explained further down.
         
         while(running) {    //The game loop.
             long now = System.nanoTime();
             delta += (now - lastTime) / ns;
             lastTime = now;
             
             long currTime = System.currentTimeMillis();
             long deltaTimeMS = currTime - startTimeMS;
             long deltaTimeS = currTime - startTimeS;
             long deltaTimeM = currTime - startTimeM;
             
             if (Integer.parseInt(elapsedMilliSeconds) < 100){
                 elapsedMilliSeconds = Integer.toString((int) ((deltaTimeMS) / 10));
                 if (Integer.parseInt(elapsedMilliSeconds) < 10){
                     elapsedMilliSeconds = "0" + Integer.toString((int) ((deltaTimeMS) / 10)); 
                 }
             }else {
                 startTimeMS = currTime;
                 elapsedMilliSeconds = "1";
             }          
             
             if (Integer.parseInt(elapsedSeconds) < 60){
                 elapsedSeconds = Integer.toString((int) ((deltaTimeS) / 1000));
                 if (Integer.parseInt(elapsedSeconds) < 10){
                     elapsedSeconds = "0" + Integer.toString((int) ((deltaTimeS) / 1000)); 
                 }
             }else {
                 startTimeS = currTime;
                 elapsedSeconds = "1";
             }
             
             if (Integer.parseInt(elapsedMinutes) < 60){
                 elapsedMinutes = Integer.toString((int) ((deltaTimeM) / 60000));
                 if (Integer.parseInt(elapsedMinutes) < 10){
                     elapsedMinutes = "0" + Integer.toString((int) ((deltaTimeM) / 60000)); 
                 }
             }else {
                 startTimeM = currTime;
                 elapsedMinutes = "1";
             }
             
             while (delta >= 1){
                 update();   //Explained further down.
                 updates++;
                 delta--;
             }
             
             render();   //Explained further down.
             frames++;
             
             if (System.currentTimeMillis() - timer > 1000) {
                 timer += 1000;
                 frame.setTitle(GAMETITLE + " | " + updates + " ups, " + frames + " fps");
                 updates = 0;
                 frames = 0;
             }
             
             while(!running){    //If the player lost this loop will start.
                 key.update();   //Explained in the Keyboard class.
                 if (key.r){ //If the player presses "r" the game will restart.
                     newHighScore = false;
                     running = true;
                     initialize();   //Explained further down.
                 }
             }
         }
         
     }
     
     public void update(){   //Handles the locical parts of the game.
         key.update();   //Explained in the Keyboard class
         if (key.up || chaR.state.equals("jumping")){                            //If the up-key is pressed or the character is already moving upwards (jumping),
             if (chaR.state.equals("walking") || chaR.state.equals("jumping")){  //and if the character is standing or jumping
                 if (jump < chaR.JUMP_HEIGHT / chaR.JUMP_FORCE){                 //and if the current height is less than above the ground is less than the height you can jump, the current height will increase.
                     chaR.yPos -= chaR.JUMP_FORCE + gravity;
                     chaR.headHitBox.setLocation(chaR.xPos + 11, chaR.yPos);
                     chaR.bodyHitBox.setLocation(chaR.xPos + 12, chaR.yPos + 45);
                     chaR.state = "jumping";
                     jump++;
                 }else {
                     jump = 0;
                     chaR.headHitBox.setLocation(chaR.xPos + 11, chaR.yPos);
                     chaR.bodyHitBox.setLocation(chaR.xPos + 12, chaR.yPos + 45);
                     chaR.state = "falling";
                 }
             }
         }
         
         //if (key.down){}
         if (key.left){
             chaR.xPos -= ground[0].HORIZ_VEL;
             chaR.HORIZ_VEL = -2;
             chaR.headHitBox.setLocation(chaR.xPos + 11, chaR.yPos);
             chaR.bodyHitBox.setLocation(chaR.xPos + 11, chaR.yPos + 45);
         }else if (key.right){
             chaR.xPos += ground[0].HORIZ_VEL; 
             chaR.HORIZ_VEL = 2;
             chaR.headHitBox.setLocation(chaR.xPos + 11, chaR.yPos);
             chaR.bodyHitBox.setLocation(chaR.xPos + 11, chaR.yPos + 45);
         }else{
             chaR.HORIZ_VEL = 0;
         }
         
         if (key.p){
             paused = true;
             //running = false;
         }
         
         //What happens when the character is outside the grapichal area
         if (chaR.xPos < 0 || chaR.dead(obstacle)){
             sound.stop();
             int newHS = Integer.parseInt(elapsedMinutes +""+ elapsedSeconds +""+ elapsedMilliSeconds);
             String content;
             int prevHS = 0;
             try {
                 content = new String(Files.readAllBytes(Paths.get(highScoreDir.toString()))); //Reads the higscore.txt-file
                 if (!content.isEmpty()){
                     prevHS = Integer.parseInt(content); //converts the content of the file to an int
                 }
             } catch (IOException ex) {
                 Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
             }
             if (newHS > prevHS){ //Compares the new high score (newHS) with the previous one (prevHS) and if the new high score is a higher value than the previous one
                 newHighScore = true;
                 try {
                     String score = Integer.toString(newHS); //the new high score (newHS) gets converted to a String
 
                     fw = new FileWriter(highScoreDir);
                     fw.write(score);
                     fw.close();
                 }catch (IOException iox){
                     //do stuff with exception
                     iox.printStackTrace();
                 }
             }
             
             switch(Integer.toString(prevHS).length()){
                 case 0:
                     highScore = "00 : 00 : 00";
                     break;
                 case 1:
                     highScore = "00 : 00 : 0" + Integer.toString(prevHS);
                     break;
                 case 2:
                     highScore = "00 : 00 :" + Integer.toString(prevHS);
                     break;
                 case 3:
                     highScore = "00 : 0" + Integer.toString(prevHS).substring(0, 1) + " : " + Integer.toString(prevHS).substring(1, 3);
                     break;
                 case 4:
                     highScore = "00 : " + Integer.toString(prevHS).substring(0, 2) + " : " + Integer.toString(prevHS).substring(2, 4);
                     break;
                 case 5:
                     highScore = "0" + Integer.toString(prevHS).substring(0, 1) + " : " + Integer.toString(prevHS).substring(1, 3) + " : " + Integer.toString(prevHS).substring(3, 5);
                     break;
                 case 6:
                     highScore = Integer.toString(prevHS).substring(0, 2) + " : " + Integer.toString(prevHS).substring(2, 4) + " : " + Integer.toString(prevHS).substring(4, 6);
                     break;
             }
             level = 1;
             running = false;
         }else if (paused){
             if (key.u){
                 paused = false;
             }
         }else {
             if (chaR.outOfArea()){  //Prevents the player from running out of the windowarea
                 chaR.xPos -= Game.chaR.HORIZ_VEL;;
             }
             
             chaR.yPos += gravity;
             chaR.headHitBox.setLocation(chaR.xPos + 11, chaR.yPos);
             chaR.bodyHitBox.setLocation(chaR.xPos + 11, chaR.yPos + 45);
             
             
             //Ground moving
             for (int i = 0; i < ground.length; i++){
                if(Ground.newLvl(Integer.parseInt(elapsedMinutes), level)){
                     ground[i].HORIZ_VEL++;
                     groundFill[i].HORIZ_VEL++;
                     groundFill[21+i].HORIZ_VEL++;
                     if (i == 20){
                         level++;
                     }
                 }
 
                 if (Ground.outOfArea(ground, i)){
                     Ground.rePos(ground, groundFill, i);
 
                     //The ground the character is running on
                     ground[i].yPos = Game.HEIGHT - ground[i].HEIGHT * Ground.yCoordinates[i];
                     ground[i].xPos += getWidth() + ground[i].WIDTH;
                     ground[i].hitBox.setLocation(ground[i].xPos, ground[i].yPos);
 
                     //The filling ground 
                     groundFill[i].yPos = Game.HEIGHT - groundFill[i].HEIGHT * (Ground.yCoordinates[i]-1);
                     groundFill[i].xPos += getWidth() + groundFill[i].WIDTH;
                     groundFill[i].hitBox.setLocation(groundFill[i].xPos, groundFill[i].yPos);
                     groundFill[21+i].yPos = Game.HEIGHT - groundFill[21+i].HEIGHT * (Ground.yCoordinates[i]-2);
                     groundFill[21+i].xPos += getWidth() + groundFill[21+i].WIDTH;
                     groundFill[21+i].hitBox.setLocation(groundFill[21+i].xPos, groundFill[21+i].yPos);
                     
                     //Creating new obstacles
                     int r = (int)(Math.random() * 10 + 1);
                     if (Obstacle.newObstacle(obstacle, ground, r, i)){
                         obstacle.add(new Obstacle(WIDTH + (ground[i].WIDTH - obstacle.get(obstacle.size()-1).WIDTH)/2, ground[i].yPos - 1));
                     }
                     
                     //Removes unseen obstacles
                     for (int x = 0; x < obstacle.size(); x++){
                         if (Obstacle.delObstacle(obstacle, ground, x)){
                             obstacle.remove(x);
                         }
                     }
                 }
 
                 //The ground the character is running on
                 ground[i].xPos -= ground[i].HORIZ_VEL;
                 ground[i].hitBox.setLocation(ground[i].xPos, ground[i].yPos);
 
                 //The filling ground 
                 groundFill[i].xPos -= groundFill[i].HORIZ_VEL;
                 groundFill[i].hitBox.setLocation(groundFill[i].xPos, groundFill[i].yPos);
                 groundFill[21+i].xPos -= groundFill[21+i].HORIZ_VEL;
                 groundFill[21+i].hitBox.setLocation(groundFill[21+i].xPos, groundFill[21+i].yPos);
                 
                 
             }
             
             //Obstacles moving
             for (int i = 0; i < obstacle.size(); i++){
                 obstacle.get(i).xPos -= ground[0].HORIZ_VEL;
                 obstacle.get(i).hitBox.setLocation(obstacle.get(i).xPos, obstacle.get(i).yPos);    
             }
             
             Ground.setSprite(ground);
             
             chaR.collision(ground);
             chaR.setSprite(updates);    //Explained in the Character class.
         }
     }
     
     public void render(){   //Handles the rendering of objects and images
         BufferStrategy bs = getBufferStrategy();
         if (bs == null) {
             createBufferStrategy(3);
             return;
         }
         
         Graphics g = bs.getDrawGraphics();
         
         g.drawImage(bG, 0, 0, null);    //Draws the background image.
 
         //Ground rendering
         Ground.render(g, ground, groundFill);   //Explained in the Ground class.
         /*for (int i = 0; i < ground.length; i++){
             g.setColor(Color.red);                                       //
             g.drawRect(ground[i].hitBox.x, ground[i].hitBox.y,           //Hitbox
                     ground[i].hitBox.width, ground[i].hitBox.height);    //
             g.drawString(""+(i+1), ground[i].xPos+10, ground[i].yPos+20);//Number
         }*/
         
         
         //Obstacle rendering
         Obstacle.render(g, obstacle);   //Explained in the Obstacle class.
         /*for (int i = 0; i < obstacle.size(); i++){
             //g.setColor(Color.red);
             //g.drawRect(obstacle.get(i).hitBox.x, obstacle.get(i).hitBox.y, obstacle.get(i).hitBox.width, obstacle.get(i).hitBox.height);
         }*/
         
         
         //Character rendering
         chaR.render(g); //Explained in the Character class.
         //g.setColor(Color.red);                                      //Hitbox
         //g.drawRect(chaR.headHitBox.x, chaR.headHitBox.y, chaR.headHitBox.width, chaR.headHitBox.height);  //
         //g.drawRect(chaR.bodyHitBox.x, chaR.bodyHitBox.y, chaR.bodyHitBox.width, chaR.bodyHitBox.height);
         
         //Time rendering
         g.setColor(Color.WHITE);
         g.setFont(new Font("Arial", Font.BOLD, 16));
         g.drawString(elapsedMinutes + " : " + elapsedSeconds + " : " + elapsedMilliSeconds, 550, 30);
         
         //Text rendering
         g.drawString("p - pause", 286, 30);
         
         if (paused){
             g.drawImage(pausedImg, 0, 0, null);
         }else if(!running){
             g.setColor(Color.BLACK);
             g.drawImage(gameOverImg, 0, 0, null);
             g.drawString("Scüre", 340, 40);
             g.drawString(elapsedMinutes + " : " + elapsedSeconds + " : " + elapsedMilliSeconds, 302, 60);
             if (newHighScore){
                 g.setColor(Color.RED);
                 g.drawString("New High Scüre", 262, 90);
                 g.drawString(elapsedMinutes + " : " + elapsedSeconds + " : " + elapsedMilliSeconds, 303, 110);
             }else {
                 g.drawString("High Scüre", 300, 90);
                 g.drawString(highScore, 303, 110);
             } 
         }
         
         g.dispose();
         bs.show();
     }
     
     public static void main(String[] args) {
         Game game = new Game();
         game.frame.setResizable(false);
         game.frame.setTitle(GAMETITLE);
         game.frame.add(game);
         game.frame.pack();
         game.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         game.frame.setLocationRelativeTo(null);
         game.frame.setVisible(true);
         
         game.start();
     }
     
     public void initialize(){
         lastTime = System.nanoTime();
         timer = System.currentTimeMillis();
         delta = 0;
         
         startTimeMS = System.currentTimeMillis();
         startTimeS = System.currentTimeMillis();
         startTimeM = System.currentTimeMillis();   
         
         updates = 0;
         frames = 0;
         
         for (int i = 0; i < ground.length; i++){
             Ground.yCoordinates[i] = 1;
             ground[i] = new Ground(0, 0, i, Ground.yCoordinates[i]);
             groundFill[i] = new Ground(4, 0, i, Ground.yCoordinates[i]-1);
             groundFill[21+i] = new Ground(4, 0, i, Ground.yCoordinates[i]-2);
         }
         
        int r = (int)(Math.random() * ground.length/3 + 2*ground.length/3);
         obstacle = new ArrayList<Obstacle>();
         obstacle.add(new Obstacle(ground[r].xPos + (ground[r].WIDTH - Obstacle.WIDTH)/2, ground[r].yPos));
         
         chaR = new irgame.object.Character();
         sound = new Sound("/irgame/res/sounds/kludd.wav");
         sound.loop();
     }
 }
