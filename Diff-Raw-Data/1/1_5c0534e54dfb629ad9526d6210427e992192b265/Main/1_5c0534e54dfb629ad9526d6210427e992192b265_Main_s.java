 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pkg4511;
 
 import java.awt.Color;
 import java.awt.ComponentOrientation;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.TexturePaint;
 import java.awt.image.BufferedImage;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.Random;
 
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.Timer;
 import javax.swing.AbstractButton;
 import java.awt.Container;
 import java.awt.Insets;
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Scanner;
 import javax.swing.JFileChooser;
 import javax.swing.filechooser.*;
 
 public class Main extends JPanel implements ActionListener {
 
     Random generator = new Random();
     public int pNodeCount = 0;
     public int CameraCount = 0;
     Node[][] floormap = new Node[400][400]; // Contains unchanging floormap data. No cameras, covered, or "possible"
     Node[][] Nodes = new Node[400][400]; //the array where the loaded image is mapped
 //  Node[][] possibleNodes = new Node[400][400]; //these are possible nodes for camera placement
 //  Node[][] cameras = new Node[400][400]; //array where cameras are placed
     static private final String newline = "\n"; //Why?! --p
     Timer timer;
     public String wall = "wall";
     public String floor = "floor";
     public String nothing = "nothing";
     public String camera = "camera";
     public String possible = "possible";
     //private int y = 0;
     //private int x = 0;
     private int img_w;
     private int img_h;
     private int time = 1;
     JButton b1 = new JButton("Open");
     JButton b2 = new JButton("Scan Nodes");
     JButton b3 = new JButton("Clear");
     JButton b4 = new JButton("Place Random");
     JButton b5 = new JButton("Improve State");
     JButton b6 = new JButton("Pathing");
     JButton b7 = new JButton("Improve To Completion");
     JButton b8 = new JButton("Generate Random Map");
     JFileChooser fc;
     File file = null;
     boolean run = false;
     boolean placeRandom = false;
     boolean addNode = false;
     boolean initialScan = false;
     boolean runCameraPlacementInitializer = false;
     BufferedImage bimage1 = null;
     CreateImage imgCreator = new CreateImage();
     Graphics g3d;
     boolean possibleLocationsCalculated = false; // Becomes true after Scan Nodes is run
     boolean randomMapGenerated = false;
 
     //<editor-fold defaultstate="collapsed" desc="Path Variables">
     Node[][] pathing = new Node[400][400];
     ArrayList<MeshPoint> mesh = new ArrayList();
     PathFinding pathFind = null;
     //</editor-fold>
     
     
     
     public static void addComponentsToPane(Container pane) {
     }
 
     public Main() {
         timer = new Timer(time, this);
         timer.start();
         this.setLayout(null);
         b1.setVerticalTextPosition(AbstractButton.CENTER);                                          //CREATING BUTTONS
         b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b1.setMnemonic(KeyEvent.VK_D);
         b1.setActionCommand("Open");
         b1.setEnabled(true);
         add(b1);
         b2.setVerticalTextPosition(AbstractButton.CENTER);
         b2.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b2.setMnemonic(KeyEvent.VK_D);
         b2.setActionCommand("Run");
         b2.setEnabled(true);
         add(b2);
         b3.setVerticalTextPosition(AbstractButton.CENTER);
         b3.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b3.setMnemonic(KeyEvent.VK_D);
         b3.setActionCommand("Clear");
         b3.setEnabled(true);
         add(b3);
         b4.setVerticalTextPosition(AbstractButton.CENTER);
         b4.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b4.setMnemonic(KeyEvent.VK_D);
         b4.setActionCommand("Random");
         b4.setEnabled(true);
         add(b4);
         b5.setVerticalTextPosition(AbstractButton.CENTER);
         b5.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b5.setMnemonic(KeyEvent.VK_D);
         b5.setActionCommand("Improve");
         b5.setEnabled(true);
         add(b5);
         b6.setVerticalTextPosition(AbstractButton.CENTER);
         b6.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b6.setMnemonic(KeyEvent.VK_D);
         b6.setActionCommand("Pathing");
         b6.setEnabled(true);
         add(b6);
         b7.setVerticalTextPosition(AbstractButton.CENTER);
         b7.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b7.setMnemonic(KeyEvent.VK_D);
         b7.setActionCommand("ImproveComplete");
         b7.setEnabled(true);
         add(b7);
         b8.setVerticalTextPosition(AbstractButton.CENTER);
         b8.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
         b8.setMnemonic(KeyEvent.VK_D);
         b8.setActionCommand("RandomMap");
         b8.setEnabled(true);
         add(b8);
         b1.addActionListener(this);
         b2.addActionListener(this);
         b3.addActionListener(this);
         b4.addActionListener(this);
         b5.addActionListener(this);
         b6.addActionListener(this);
         b7.addActionListener(this);
         b8.addActionListener(this);
         b1.setBounds(10, 425, 80, 20);
         b2.setBounds(90, 425, 110, 20);
         b3.setBounds(200, 425, 110, 20);
         b4.setBounds(10, 445, 125, 20);
         b5.setBounds(135, 445, 125, 20);
         b6.setBounds(260, 445, 125, 20);
         b7.setBounds(10, 465, 160, 20);
         b8.setBounds(170, 465, 180, 20);
         fc = new JFileChooser();
 
     }
 
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
 
         Graphics2D g2d = (Graphics2D) g;
         g3d = g2d; //needed this for drawing within my coverage algorithm, kinda hacky
         g2d.setColor(new Color(0, 212, 212)); //cyan for possible nodes
 
 
 
 
 
         if (file != null) { //reading the bitmap in
             try {
                 bimage1 = ImageIO.read(file);
 
             } catch (IOException ioe) {
                 ioe.printStackTrace();
             }
 
             g2d.drawImage(bimage1, null, 0, 0);
 
             //Drawing 'possible nodes', if they exist:
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.POSSIBLE) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
 
 
             g2d.setColor(new Color(255, 0, 0));//Bright red dot for camera placement
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.CAMERA) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
 
             g2d.setColor(new Color(125, 30, 30));//darker red for coverage
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.COVERED) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
             
             
            if(pathFind!=null){
             g2d.setColor(new Color(236, 202, 97));//golden yellow - mesh lines
             //Iterator<MeshOption> itr = pathFind.options.iterator();
             //MeshOption o;
             //while(itr.hasNext()){
             for(MeshPoint m : pathFind.mesh){
                 for(MeshOption o : m.options){
                     //System.out.println("Should be drawing lines");
                     //o = itr.next();
                     //System.out.println("o.sp: ("+o.startPoint.x+", "+o.startPoint.y+")");
                    // System.out.println("o.ep: ("+o.endPoint.x+", "+o.endPoint.y+")");
                     g2d.drawLine(o.sNode.x, o.sNode.y, o.eNode.x, o.eNode.y);
                 }
             }
             
             g2d.setColor(new Color(251, 255, 0));//yellow - mesh point
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.MESHPOINT) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
            }
 
 
 
             img_w = bimage1.getWidth();
             img_h = bimage1.getHeight();
         }
         else if (randomMapGenerated) {
             
             g2d.drawImage(bimage1, null, 0, 0);
 
             //Drawing 'possible nodes', if they exist:
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.POSSIBLE) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
 
 
             g2d.setColor(new Color(255, 0, 0));//Bright red dot for camera placement
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.CAMERA) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
 
             g2d.setColor(new Color(125, 30, 30));//darker red for coverage
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.COVERED) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
             
             
            if(pathFind!=null){
             g2d.setColor(new Color(236, 202, 97));//golden yellow - mesh lines
             //Iterator<MeshOption> itr = pathFind.options.iterator();
             //MeshOption o;
             //while(itr.hasNext()){
             for(MeshPoint m : pathFind.mesh){
                 for(MeshOption o : m.options){
                     System.out.println("Should be drawing lines");
                     //o = itr.next();
                     System.out.println("o.sp: ("+o.sNode.x+", "+o.sNode.y+")");
                     System.out.println("o.ep: ("+o.eNode.x+", "+o.eNode.y+")");
                     g2d.drawLine(o.sNode.x, o.sNode.y, o.eNode.x, o.eNode.y);
                 }
             }
             
             g2d.setColor(new Color(251, 255, 0));//yellow - mesh point
 
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.MESHPOINT) {
                         g2d.drawLine(x, y, x, y);
                     }
                 }
             }
            }
 
 
 
             img_w = bimage1.getWidth();
             img_h = bimage1.getHeight();
         }
         
         //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //            if(placeRandom){ //used for testing my coverage algorithm, not really random just looks for the first downward oriented node (x+100)
 //                int xRan = 0;   //this is specific to my floormap
 //                int yRan = 0;
 //                for (int y = 0; y < img_h-1; y++){
 //                for (int x = 0; x < img_w-1; x++){
 //                    //System.out.println("x: " + x);
 //                    //System.out.println("y: " + y);
 //                    if(Nodes[x][y].type == NodeType.POSSIBLE){
 //                    if(Nodes[x][y].getOri().equals("down")){
 //                        xRan = x;
 //                        yRan = y;
 //                        x = img_w-1;
 //                        y = img_h-1;
 //                    }
 //                    }
 //                }
 //                }
 //                g2d.setColor(new Color(220, 0, 0));
 //                g2d.drawLine(xRan+100, yRan, xRan+100, yRan);
 //                System.out.println("Placing random camera and calculating it's coverage at (" + (xRan+100) + "," + yRan + ")");
 //                System.out.println("Coverage for this camera is: " + fixed_coverage2(xRan+100,yRan,"down") + " nodes");
 //                placeRandom=false;
 //            }
         //</editor-fold>
         
         int pxColor = 0;
         if (initialScan) { //maps the loaded image to a two dimensional array
             System.out.println("Running initial image scan.");
             //Blank out old map data, if applicable
             Nodes = new Node[400][400];
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     pxColor = bimage1.getRGB(x, y);
                     if (Integer.toHexString(pxColor).equals("ff000000")) { //This is wall
                         Nodes[x][y] = new Node(x, y, NodeType.WALL);
                         floormap[x][y] = new Node(x, y, NodeType.WALL);
                     }
                     if (Integer.toHexString(pxColor).equals("ffc0c0c0")) { //This is floor
                         Nodes[x][y] = new Node(x, y, NodeType.FLOOR);
                         floormap[x][y] = new Node(x, y, NodeType.FLOOR);
                     }
                     if (Integer.toHexString(pxColor).equals("ffffffff")) { //This is nothing
                         Nodes[x][y] = new Node(x, y, NodeType.NOTHING);
                         floormap[x][y] = new Node(x, y, NodeType.NOTHING);
                     }
                     if (Integer.toHexString(pxColor).equals("ff71b84a")) { //This is door - color is green
                         Nodes[x][y] = new Node(x, y, NodeType.DOOR);
                         floormap[x][y] = new Node(x, y, NodeType.DOOR);
                     }
                 }
             }
             initialScan = false;
         }
         String direction = "";
 //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //            if(run){                                    //This will mark and add to possibleNodes[] nodes that can become cameras.
 //            for (int y = 0; y < img_h; y++){          //Only floor nodes that are adjacent to walls are possible.
 //            for (int x = 0; x < img_w; x++){
 //                int rgb = bimage1.getRGB(x, y); //center
 //                //System.out.println(Integer.toHexString(rgb));
 //                int rgb2, rgb3, rgb4,rgb5;
 //                
 //                rgb2=0;
 //                rgb3=0;
 //                rgb4=0;
 //                rgb5=0;
 //                
 //                if(x!=img_w-1){
 //                rgb2 = bimage1.getRGB(x+1, y);
 //                }
 //                if(y!=img_h-1){
 //                rgb3 = bimage1.getRGB(x, y+1);
 //                }
 //                if (x!=0){
 //                rgb4 = bimage1.getRGB(x-1, y);
 //                }
 //                if (y!=0){
 //                rgb5 = bimage1.getRGB(x, y-1);    
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb4 != rgb && x!=0){ //these if statements set the orientation of the possible camera
 ////                    g2d.drawLine(x,y,x,y);                                              //node depending on which side the wall is
 //                    direction = "right";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb5 != rgb && y!=0){
 ////                    g2d.drawLine(x,y,x,y);
 //                    direction = "down";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb2 != rgb){
 ////                    g2d.drawLine(x,y,x,y);
 //                    direction = "left";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb3 != rgb){
 ////                    g2d.drawLine(x,y,x,y);
 //                    direction = "up";
 //                    addNode=true;
 //                }
 //                if(addNode){
 //                    System.out.println("Adding a node.");
 //                    Nodes[x][y].setType(NodeType.POSSIBLE);
 //                    Nodes[x][y].setOri(direction);
 //                    addNode=false;
 //                }
 //            }
 //            }
 //            run = false;
 //            possibleLocationsCalculated = true;
 //        }
 //</editor-fold>
     }
     
 //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //    private void calculatePossibleLocations(){
 ////                    if(run){                                    //This will mark and add to possibleNodes[] nodes that can become cameras.
 //            for (int y = 0; y < img_h; y++){          //Only floor nodes that are adjacent to walls are possible.
 //            for (int x = 0; x < img_w; x++){
 //                int rgb = bimage1.getRGB(x, y); //center
 //                //System.out.println(Integer.toHexString(rgb));
 //                int rgb2, rgb3, rgb4,rgb5;
 //                
 //                rgb2=0;
 //                rgb3=0;
 //                rgb4=0;
 //                rgb5=0;
 //                
 //                if(x!=img_w-1){
 //                rgb2 = bimage1.getRGB(x+1, y);
 //                }
 //                if(y!=img_h-1){
 //                rgb3 = bimage1.getRGB(x, y+1);
 //                }
 //                if (x!=0){
 //                rgb4 = bimage1.getRGB(x-1, y);
 //                }
 //                if (y!=0){
 //                rgb5 = bimage1.getRGB(x, y-1);    
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb4 != rgb && x!=0){ //these if statements set the orientation of the possible camera
 ////                    g2d.drawLine(x,y,x,y);                                              //node depending on which side the wall is
 ////                    direction = "right";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb5 != rgb && y!=0){
 ////                    g2d.drawLine(x,y,x,y);
 ////                    direction = "down";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb2 != rgb){
 ////                    g2d.drawLine(x,y,x,y);
 ////                    direction = "left";
 //                    addNode=true;
 //                }
 //                if(Integer.toHexString(rgb).equals("ffc0c0c0") && rgb3 != rgb){
 ////                    g2d.drawLine(x,y,x,y);
 ////                    direction = "up";
 //                    addNode=true;
 //                }
 //                if(addNode){
 ////                    System.out.println("Adding a node.");
 //                    Nodes[x][y].setType(NodeType.POSSIBLE);
 ////                    Nodes[x][y].setOri(direction);
 //                    addNode=false;
 //                }
 //            }
 //            }
 ////            run = false;
 //            possibleLocationsCalculated = true;
 //    }
 ////</editor-fold>  
     
     //<editor-fold defaultstate="collapsed" desc="=========== Camera Methods ===========">
     private void calculatePossibleLocations() {
 //                    if(run){                                    //This will mark and add to possibleNodes[] nodes that can become cameras.
         for (int y = 0; y < Nodes.length; y++) {          //Only floor nodes that are adjacent to walls are possible.
             for (int x = 0; x < Nodes[y].length; x++) {
                 Node evalNode = Nodes[x][y]; //center
                 //System.out.println(Integer.toHexString(rgb));
                 Node rightNode, topNode, leftNode, bottomNode;
 
                 rightNode = new Node(0, 0, NodeType.UNASSIGNED);
                 topNode = new Node(0, 0, NodeType.UNASSIGNED);
                 bottomNode = new Node(0, 0, NodeType.UNASSIGNED);
                 leftNode = new Node(0, 0, NodeType.UNASSIGNED);
 
 
                 if (x != Nodes[y].length - 1) {
                     rightNode = Nodes[x + 1][y];
                 }
                 if (y != Nodes.length - 1) {
                     topNode = Nodes[x][y + 1];
                 }
                 if (x != 0) {
                     leftNode = Nodes[x - 1][y];
                 }
                 if (y != 0) {
                     bottomNode = Nodes[x][y - 1];
                 }
                 if (evalNode.getType() == NodeType.FLOOR && ((rightNode.getType() == NodeType.WALL)
                         || (topNode.getType() == NodeType.WALL) || (bottomNode.getType() == NodeType.WALL)
                         || (leftNode.getType() == NodeType.WALL))) {
                     Nodes[x][y].setType(NodeType.POSSIBLE);
                 }
 
             }
         }
 //            run = false;
     }
 
     private void clearPossibleCameraPositions() {
         for (int y = 0; y < img_h; y++) {
             for (int x = 0; x < img_w; x++) {
                 if (Nodes[x][y].type == NodeType.POSSIBLE) {
                     Nodes[x][y].setType(NodeType.FLOOR);
                 }
             }
         }
         possibleLocationsCalculated = false;
     }
     
 //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //    private void findPotentialCameraLocations(){
 //        String floor = "ffc0c0c0";
 //        String wall = "ff000000";
 //        String nothing = "ffffffff";
 //        for(int x = 0; x<img_w; x++){
 //            for(int y = 0; y<img_h; y++){
 //                
 //            }
 //            
 //        }
 //        
 //        
 //        
 //    }
     //</editor-fold>
     
     public void placeCamera(int xPos, int yPos, int ori) {
         assert (xPos >= 0);
         assert (xPos < img_w);
         assert (yPos >= 0);
         assert (yPos < img_h);
 //        if(possibleLocationsCalculated){
         Nodes[xPos][yPos].setType(NodeType.CAMERA);
         Nodes[xPos][yPos].setOri(ori);
         calculateCoverage(xPos, yPos, ori, 5);
 //        }
     }
 
     private void placeRandomCamera() {
 //            clearCameras();//Only one random camera for now
         calculatePossibleLocations();
         while (true) {
             for (int y = 0; y < img_h; y++) {
                 for (int x = 0; x < img_w; x++) {
                     if (Nodes[x][y].type == NodeType.POSSIBLE) {
                         if (4 == Math.floor(Math.random() * 1000)) {
                             //Gives all the nodes a decent chance of being chosen
                             placeCamera(x, y, (int) Math.floor(Math.random() * 365));
                             System.out.printf("Placed camera at %d,%d\n", x, y);
                             return;//Only one camera at a time!
                         }
                     }
                 }
             }
         }
     }
     
 //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //    public int fixed_coverage2(int xCord, int yCord, String direction){ //this will calculate coverage for a node, given its coordinates and direction
 //        int covArea=0;                                                  //this is still very experimental, hence the lack of clean up
 //        int xLine = xCord;
 //        int yLine = yCord;
 //        int xFan = xCord;
 //        int yFan = yCord;
 //        int xFan_pos=xCord;
 //        int yy = 0;
 //        int xx = 0;
 //        int b = yCord;
 //        double m;
 //        double xTemp = 0;
 //        double yTemp = 0;
 //        double rad_angle = 0;
 //        int angle_count = 0;
 //        int color_grad=150;
 //        Node[] line = new Node[400];
 //        
 //        if(direction.equals("left")){ //going left - x is decrementing
 //            while(!(Nodes[xLine][yCord].getType().equals(wall))){
 //                xLine--;
 //            }
 //            
 //        }
 //        if(direction.equals("right")){ //going right - x is incrementing
 //            while(!(Nodes[xLine][yCord].getType().equals(wall))){
 //                xLine++;
 //            }
 //        }
 //        if(direction.equals("up")){ //going up - y is decrementing
 //            while(!(Nodes[xCord][yLine].getType().equals(wall))){
 //                yLine--;
 //            }
 //        }
 //        if(direction.equals("down")){ //going down - y is incrementing
 //            g3d.setColor(new Color(color_grad, 0, 0));
 //            for (angle_count = 45; angle_count <= 60 ; angle_count++){
 //                g3d.setColor(new Color(color_grad, 0, 0));
 //                color_grad=color_grad+5;
 //                rad_angle = angle_count*(Math.PI/180);
 //                m = Math.tan(rad_angle);
 //                System.out.println("angle_count: " + angle_count);
 //
 //                //g3d.drawLine(xFan,yFan,angle_count,399);
 //                while(!(Nodes[xFan][yFan].getType().equals(wall)) && angle_count!=0){ //fan left
 //                    if(!(Nodes[xFan][yFan].getType().equals(nothing))){
 //                    covArea++;
 //                    g3d.drawLine(xFan,yFan,xFan,yFan);
 //                    }
 //                    xFan = xFan+1;
 //                    yFan = round(m*xFan-108);
 //                }
 //                xFan = xCord;
 //                yFan = yCord;
 //                while(!(Nodes[xFan][yFan].getType().equals(wall)) && angle_count!=0){ //fan left
 //                    if(!(Nodes[xFan][yFan].getType().equals(nothing))){
 //                    covArea++;
 //                    g3d.drawLine(xFan,yFan,xFan,yFan);
 //                    }
 //                    xFan_pos = xFan_pos+1;
 //                    xFan = xFan-1;
 //                    yFan = round(m*xFan_pos-109);
 //                }
 //                xFan = xCord;
 //                yFan = yCord;
 //                xFan_pos = xCord;
 //            }
 //        }
 //        //covArea = covArea - Math.abs()
 //        System.out.println("I have returned: " + covArea);
 //        return covArea;
 //    }
     //</editor-fold>
     
     public void calculateCoverage(int xPos, int yPos, int oriDeg, int fan) {
         //orient == degrees from pure right
         //fan == number of degrees from center we're fanning out
 //        assert(orient>=0);
 //        assert(orient<(2*Math.PI));
         assert (xPos >= 0);
         assert (xPos < img_w);
         assert (yPos >= 0);
         assert (yPos < img_h);
 //        ArrayList<Coord> inclusiveList = 
         for (double f = -1.0 * fan; f <= fan; f += 0.1) {
             double orient = degToRad((oriDeg + f));
             double xSlope = Math.cos(orient);
             double ySlope = Math.sin(orient);
             //By what value do we have to jump to get to the next round X value?
             //By what value... for Y?
 //           double xJump = 1.0 / xSlope;
 //           double yJump = 1.0 / ySlope;
             double xAccum = 0.0;
             double yAccum = 0.0;
             int xCur = xPos;
             int yCur = yPos;
             do {
                 if (Nodes[xCur][yCur].getType() != NodeType.CAMERA) {
                     //We don't want to cover the camera, but we want to continue the loop
                     //Thus, we skip ONLY this part, not the iteration
                     Nodes[xCur][yCur].setType(NodeType.COVERED);
                 }
 
                 xAccum += xSlope;
                 yAccum += ySlope;
                 xCur = xPos + (int) Math.round(xAccum);
                 yCur = yPos + (int) Math.round(yAccum);
             } while (Nodes[xCur][yCur].type != NodeType.WALL && Nodes[xCur][yCur].type != NodeType.NOTHING);
         }
         //Initial pass completed. Let's fill in the holes:
 
 
 
 //        Scanner pauser = new Scanner(System.in);
 //        System.out.println("Initial pass made...");
 //        pauser.nextLine();
 
     }
 
     public void calculateCoverage(int fan) {
         //For use with a map that has all the cameras already set up
         for (int y = 0; y < img_h; y++) {
             for (int x = 0; x < img_w; x++) {
                 if (Nodes[x][y].type == NodeType.CAMERA) {
                     calculateCoverage(x, y, Nodes[x][y].getOri(), fan);
                 }
             }
         }
     }
 //<editor-fold defaultstate="collapsed" desc="Commented out code">
 //    private void fleshOutCoverage(){
 //                //CELLULAR AUTONOMA SCHEME:
 //        //If >3 Nodes surrounding this one are covered, this node is also covered.
 //        //Continue until no changes are detected.
 //        
 //        boolean changesMade = false;
 //        int iter = 0;
 //        do{
 //          changesMade = false;//Doh. Forgot this... --p
 //          for(int y = 0; y < img_h; y++){
 //          for(int x = 0; x < img_w; x++){
 //            if(Nodes[x][y].type==NodeType.FLOOR){
 //                if(determineNodeNeighborCoverage(x,y) > 4){
 //                    System.out.printf("Node %d %d converted to Covered\n",x,y);
 //                    Nodes[x][y].setType(NodeType.COVERED);
 //                    changesMade = true;
 //                }
 //            }
 //          }
 //          }
 //            System.out.println("on iter" + iter++);
 //        }while(changesMade);
 //    }
     //</editor-fold>
     
     private int determineNodeNeighborCoverage(int xCent, int yCent) {
         int neighborsCovered = 0;
         for (int xDelta = -1; xDelta <= 1; xDelta++) {
             for (int yDelta = -1; yDelta <= 1; yDelta++) {
                 try {
                     if (Nodes[xCent + xDelta][yCent + yDelta].type == NodeType.COVERED) {
                         neighborsCovered++;
                         //I realize that this will count the node itself. That's fine.
                     }
 
                 } catch (ArrayIndexOutOfBoundsException e) {
                     //Ignore and move on
                 }
             }
         }
         return neighborsCovered;
     }
 
     public void clearCoverage() {
         for (int y = 0; y < img_h; y++) {
             for (int x = 0; x < img_w; x++) {
                 if (Nodes[x][y].type == NodeType.COVERED) {
                     Nodes[x][y].setType(NodeType.FLOOR);
                 }
             }
         }
         possibleLocationsCalculated = false;//Because we cover overwrite them!
 
     }
 
     private void clearCameras() {
         for (int y = 0; y < img_h; y++) {
             for (int x = 0; x < img_w; x++) {
                 if (Nodes[x][y].type == NodeType.CAMERA) {
                     Nodes[x][y].setType(NodeType.FLOOR);
                 }
             }
         }
         clearCoverage();
     }
 
     private double degToRad(double a) {
         return (((double) a) / 180.0) * Math.PI;
     }
 
     private int round(double d) { //rounding numbers traditionally
         double dAbs = Math.abs(d);
         int i = (int) dAbs;
         double result = dAbs - (double) i;
         if (result < 0.5) {
             return d < 0 ? -i : i;
         } else {
             return d < 0 ? -(i + 1) : i + 1;
         }
     }
     
     private boolean improveState() {
         //            fleshOutCoverage();
         //Send info to CameraPlacementEngine
 
 
 
         System.out.printf("Number of cameras: %d\n",
                 CameraPlacementEngine.extractCameras(Nodes).size());
 
         if (runCameraPlacementInitializer) {
             (new CameraPlacementState(new Node[0][0], null)).initializeQuick();//File under: things I'll burn in Hell for
             runCameraPlacementInitializer = false;
         }
 
 
         CameraPlacementState newPlacement = CameraPlacementEngine.getImprovedState(Nodes, floormap);
 
 
 
         //Check that info gotten back is not null (null means we're at the peak)
         if (newPlacement == null) {
             System.out.println("Not getting any better!");
             return false; //State did not improve
         } else {
             //Reset map
             resetToFloormap();
             //Update Nodes to reflect the new camera pattern
             for (Node camera : newPlacement.cameraLocations) {
                 Nodes[camera.x][camera.y].setType(NodeType.CAMERA);
                 Nodes[camera.x][camera.y].setOri(camera.orientation);
             }
             //Calculate coverage
             calculateCoverage(5);
             System.out.printf("New number of cameras: %d\n",
                     CameraPlacementEngine.extractCameras(Nodes).size());
             //Show me the money
 //            repaint();
             return true;
         }
     }
 
     //</editor-fold>
 
     private void resetToFloormap() {
         for (int x = 0; x < Nodes.length; x++) {
             for (int y = 0; y < Nodes[x].length; y++) {
                 Nodes[x][y] = floormap[x][y].clone();
             }
         }
     }
 
     public static void main(String[] args) { //main function, intializes the gui
 
         JFrame frame = new JFrame("Security Coverage");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.add(new Main());
         //frame.add(rects);
         frame.setSize(407, 600);
         frame.setLocationRelativeTo(null);
         frame.setVisible(true);
         frame.setResizable(false);
 
 
     }
 
 
     //<editor-fold defaultstate="collapsed" desc="============ Path methods ============"> 
     private void createPathMesh(){
         pathFind = new PathFinding(Nodes);
         pathFind.createMeshPoints();
         mesh = pathFind.getMesh();
         int x=0;
         int y=0;
         for(MeshPoint p: mesh){
             x = p.currentNode.x;
             y = p.currentNode.y;
             Nodes[x][y].setType(NodeType.MESHPOINT);
         }
     }    
     
     private void clearPaths() {
         int x = 0;
         int y = 0;
         for(MeshPoint p: mesh){
             x = p.currentNode.x;
             y = p.currentNode.y;
             if(Nodes[x][y].type==NodeType.MESH || Nodes[x][y].type==NodeType.MESHPOINT){
                 Nodes[x][y].setType(NodeType.FLOOR);
             }
         }
     }
     //</editor-fold>
     
     public void actionPerformed(ActionEvent e) { //listener used for button calls, button presses will set off the if statement with the corresponding
         // action command
 
         if ("Open".equals(e.getActionCommand())) {
             int returnVal = fc.showOpenDialog(Main.this);
 
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 file = fc.getSelectedFile();
                 initialScan = true;
                 randomMapGenerated = false;
                 repaint();
                 //This is where a real application would open the file.
                 System.out.println("Opening: " + file.getName() + "." + newline);
             } else {
                 System.out.println("Open command cancelled by user." + newline);
             }
         }
         if ("Run".equals(e.getActionCommand())) {
             run = true;
             System.out.println("Scanning for possible camera positions");
             calculatePossibleLocations();
             repaint();
 //            repaint();//Let's get that color up!
         }
 
         if ("Random".equals(e.getActionCommand())) {
 //            placeRandom = true;
             System.out.println("Placing a random camera");
             placeRandomCamera();
             repaint();
         }
         if ("Clear".equals(e.getActionCommand())) {
             run = false;
             System.out.println("Clearing possible camera positions");
             clearPossibleCameraPositions();
             clearCameras();
             clearPaths();
             repaint();
         }
 
         if ("Improve".equals(e.getActionCommand())) {
             improveState();
             repaint();
 
 //            repaint();//Test to make sure stuff sticks around between 'frames'
         }
 
         if ("ImproveComplete".equals(e.getActionCommand())) {
             runCameraPlacementInitializer = true;
             while (improveState()) {
                 repaint();
 
             }//Oh man, that's awesome
         }
 
         if ("Pathing".equals(e.getActionCommand())) {
 
             System.out.println("Pathing: Setting up");
             createPathMesh();
             repaint();
         }
         
         if ("RandomMap".equals(e.getActionCommand())) {
             randomMapGenerated = true;
             initialScan = true;
             file = null;
             System.out.println("Generating random floor map...");
             bimage1 = imgCreator.generateRandomBMP();
             repaint();
         }
 
     }
 }
