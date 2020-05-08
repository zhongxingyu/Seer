 import java.applet.Applet;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import javax.imageio.ImageIO;
 import javax.swing.Timer;
 
 /**
  *
  * @author Ragnar
  */
 public class paint extends Applet implements KeyListener, MouseListener, MouseMotionListener, ActionListener {
     Image offscreen;
     Graphics offg;
     Timer timer;
     int selection, layer;
     Point lastFree;
     ArrayList<drawnObject> deletedObjects;
     ArrayList<Integer> lineList;
     ArrayList<ArrayList<drawnObject>> layerList;
     boolean dragging, paintMenu, editMenu, square, circle, hoverCircle, hoverSquare, layerMenu, allLayers;
     Color color;
     
     @Override
     public void init() {
         //Initial setup
         this.setSize(1000, 490);
         this.addKeyListener(this);
         this.addMouseListener(this);
         this.addMouseMotionListener(this);
         timer = new Timer(10, this);
         offscreen = createImage(this.getWidth(), this.getHeight());
         offg = offscreen.getGraphics();
         deletedObjects = new ArrayList();
         lineList = new ArrayList();
         layerList = new ArrayList();
         layerList.add(new ArrayList<drawnObject>());
         color = Color.BLACK;
         dragging = false;
         paintMenu = false;
         square = false;
         circle = false;
         layerMenu = false;
         allLayers = false;
         selection = 0;
         layer = 0;
     }
     
     @Override
     public void paint(Graphics g) {
         //Governs what will be drawn on the screen every refresh
         offg.setColor(Color.WHITE);
         offg.fillRect(0, 0, 1000, 490);
         
         offg.setColor(Color.BLACK);
         if(allLayers) {
             for(int j = 0; j < layerList.size(); j++) {
                 for(int i = 0; i < layerList.get(j).size(); i++) {
                     layerList.get(j).get(i).paint(offg);
                 }
             }
         } else {
             for(int i = 0; i < layerList.get(layer).size(); i++) {
                 layerList.get(layer).get(i).paint(offg);
             }
         }
         //Tool menu
         offg.setColor(Color.WHITE);
         offg.fillRect(0, 0, 70, 490);
         offg.setColor(Color.BLACK);
         for(int i = 0; i < 6; i++) {
             if(selection == i) {
                 offg.setColor(Color.LIGHT_GRAY);
                 offg.fillRect(0, (0 + (70 * i)), 70, 70);
                 offg.setColor(Color.BLACK);
                 offg.drawRect(0, (0 + (70 * i)), 70, 70);
             }
             else {
                 offg.drawRect(0, (0 + (70 * i)), 70, 70);
             }
         }
 
         //freehand
         offg.drawLine(20, 55, 22, 48);
         offg.drawLine(20, 55, 25, 50);
         offg.drawLine(22, 48, 45, 20);
         offg.drawLine(25, 50, 48, 22);
         offg.drawLine(45, 20, 48, 22);
         //straight line
         offg.drawLine(15, 120, 55, 90);
         //rectangle
         if(square) {
             offg.drawRect(15, 155, 40, 40);
         }
         else {
             offg.drawRect(15, 165, 40, 20);
         }
         if(hoverSquare) {
             offg.setColor(Color.WHITE);
             offg.fillRect(70, 140, 70, 70);
             offg.setColor(Color.LIGHT_GRAY);
             offg.drawRect(70, 140, 70, 70);
             
             if(square) {
                 offg.drawRect(85, 165, 40, 20);
             }
             else {
                 offg.drawRect(85, 155, 40, 40);
             }
         }
         offg.setColor(Color.BLACK);
         //circle
         if(circle) {
             offg.drawOval(13, 222, 45, 45);
         }
         else {
             offg.drawOval(13, 230, 45, 28);
         }
         if(hoverCircle) {
             offg.setColor(Color.WHITE);
             offg.fillRect(70, 210, 70, 70);
             offg.setColor(Color.LIGHT_GRAY);
             offg.drawRect(70, 210, 70, 70);
             
             if(circle) {
                 offg.drawOval(83, 230, 45, 28);
             }
             else {
                 offg.drawOval(83, 222, 45, 45);
             }
             offg.setColor(Color.BLACK);
         }
         //layers
         offg.drawLine(15, 330, 45, 330);
         offg.drawLine(15, 322, 45, 322);
         offg.drawLine(15, 314, 45, 314);
         offg.drawLine(30, 300, 60, 300);
         offg.drawLine(15, 314, 30, 300);
         offg.drawLine(45, 314, 60, 300);
         offg.drawLine(15, 322, 24, 314);
         offg.drawLine(15, 330, 24, 322);
         offg.drawLine(45, 322, 60, 308);
         offg.drawLine(52, 308, 60, 308);
         offg.drawLine(45, 330, 60, 315);
         offg.drawLine(52, 315, 60, 315);
         //colors
         if(paintMenu) {
             for(int i = 1; i < 11; i++) {
                 if(i == 1) offg.setColor(Color.BLACK);
                 else if(i == 10) offg.setColor(Color.BLUE);
                 else if(i == 3) offg.setColor(Color.GREEN);
                 else if(i == 4) offg.setColor(Color.ORANGE);
                 else if(i == 5) offg.setColor(Color.PINK);
                 else if(i == 6) offg.setColor(Color.RED);
                 else if(i == 7) offg.setColor(Color.MAGENTA);
                 else if(i == 8) offg.setColor(Color.YELLOW);
                 else if(i == 9) offg.setColor(Color.CYAN);
                 else if(i == 2) offg.setColor(Color.DARK_GRAY);
                 offg.fillRect((70 * i), 420, 70, 70);
                 offg.setColor(Color.BLACK);
                 offg.drawRect((70 * i), 420, 70, 69);
             }
         }
         //Settings menu
         offg.drawOval(13, 380, 7, 7);
         offg.drawOval(32, 380, 7, 7);
         offg.drawOval(50, 380, 7, 7);
         if(editMenu) {
             for(int i = 1; i < 7; i++) {
                 offg.setColor(Color.WHITE);
                 offg.fillRect((70 * i), 350, 70, 70);
                 offg.setColor(Color.BLACK);
                 offg.drawRect((70 * i), 350, 70, 70);
             }
             //undo
             if(layerList.get(layer).isEmpty()) offg.setColor(Color.LIGHT_GRAY);
             offg.drawLine(85, 385, 125, 385);
             offg.drawLine(85, 385, 95, 380);
             offg.drawLine(85, 385, 95, 390);
             offg.setColor(Color.BLACK);
             
             //redo
             if(deletedObjects.isEmpty()) offg.setColor(Color.LIGHT_GRAY);
             offg.drawLine(155, 385, 195, 385);
             offg.drawLine(195, 385, 185, 380);
             offg.drawLine(195, 385, 185, 390);
             offg.setColor(Color.BLACK);
             //clear all
             offg.drawLine(220, 360, 270, 410);
             offg.drawLine(220, 410, 270, 360);
             //save
             offg.drawRect(300, 380, 30, 30);
             offg.drawLine(315, 360, 315, 398);
             offg.drawLine(310, 385, 315, 398);
             offg.drawLine(320, 385, 315, 398);
             //load
             offg.drawRect(370, 380, 30, 30);
             offg.drawLine(385, 360, 385, 398);
             offg.drawLine(385, 360, 380, 370);
             offg.drawLine(385, 360, 390, 370);
             //exit
             offg.drawOval(435, 365, 40, 40);
             offg.drawLine(455, 357, 455, 377);
             
         }
         
         //layer menu
         if(layerMenu) {
             for(int i = 1; i < 7; i++) {
                 if(i == 4 && allLayers) offg.setColor(Color.LIGHT_GRAY);
                 else offg.setColor(Color.WHITE);
                 offg.fillRect((70 * i), 280, 70, 70);
                 offg.setColor(Color.BLACK);
                 offg.drawRect((70 * i), 280, 70, 70);
             }
            offg.drawString("" + layer, 103, 320);
             //up layer
             if(layer >= layerList.size() - 1) offg.setColor(Color.LIGHT_GRAY);
             offg.drawLine(175, 295, 175, 335);
             offg.drawLine(175, 295, 170, 305);
             offg.drawLine(175, 295, 180, 305);
             offg.setColor(Color.BLACK);
             
             //down layer
             if(layer == 0) offg.setColor(Color.LIGHT_GRAY);
             offg.drawLine(245, 295, 245, 335);
             offg.drawLine(245, 335, 240, 325);
             offg.drawLine(245, 335, 250, 325);
             offg.setColor(Color.BLACK);
             
             //all layers
             offg.drawLine(300, 335, 315, 295);
             offg.drawLine(315, 295, 330, 335);
             offg.drawLine(305, 322, 325, 322);
             //add layer
             offg.drawLine(385, 295, 385, 335);
             offg.drawLine(365, 315, 405, 315);
             //remove layer
             if(layerList.size() == 1) offg.setColor(Color.LIGHT_GRAY);
             offg.drawLine(435, 315, 475, 315);
             offg.setColor(Color.BLACK);
         }
         offg.setColor(color);
         offg.fillRect(0, 420, 70, 70);
         offg.setColor(Color.BLACK);
         offg.drawRect(0, 420, 70, 69);
         
         g.drawImage(offscreen, 0, 0, this);
         repaint();
     }
     
     public void drawShape(Point p) {
         if(selection == 0) {
             layerList.get(layer).add(new drawnObject(p, color, false, 0));
         }
         if(selection == 1) {
             //objectList.add(new drawnObject(p, color, false, 1));
             layerList.get(layer).add(new drawnObject(p, color, false, 1));
         }
         if(selection == 2) {
             //objectList.add(new drawnObject(p, color, square, 2));
             layerList.get(layer).add(new drawnObject(p, color, square, 2));
         }
         if(selection == 3) {
             //objectList.add(new drawnObject(p, color, circle, 3));
             layerList.get(layer).add(new drawnObject(p, color, circle, 3));
         }
         dragging = true;
     }
     @Override
     public void update(Graphics g) {
         paint(g);
     }
     @Override
     public void start() {
         timer.start();
     }
 
     @Override
     public void stop() {
         timer.stop();
     }
     
     @Override
     public void destroy() {
         
     }
     
     @Override
     public void actionPerformed(ActionEvent e) {
         
     }
     
     @Override
     public void keyTyped(KeyEvent ke) {
         
     }
 
     @Override
     public void keyPressed(KeyEvent ke) {
     }
 
     @Override
     public void keyReleased(KeyEvent ke) {
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
        if(e.getX() > 70 && !editMenu && !layerMenu) {
             //starting an object
             drawShape(e.getPoint());
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
         if(me.getX() <= 70 && !dragging) {
             //selecting a tool
             selection = getSelection(me.getPoint());
         }
         else if(me.getX() > 70) {
             //ending an object
             dragging = false;
         }
         if(layerMenu) {
             if(me.getY() >= 280 && me.getY() <= 350 && me.getX() <= 490) {
                 for(int i = 1; i < 7; i++) {
                     if(me.getX() >= (70 * i) && me.getX() <= (70 + (70 * i))) {
                         if(i == 1) {
                             //layer number, no action
                         }
                         else if(i == 2) {
                             //up layer
                             if(layer < layerList.size() - 1) {
                                 layer++;
                             }
                         }
                         else if(i == 3) {
                             //down layer
                             if(layer > 0) {
                                 layer--;
                             }
                         }
                         else if(i == 4) {
                             //view all layers
                             if(allLayers) allLayers = false;
                             else allLayers = true;
                         }
                         else if(i == 5) {
                             //add layer
                             layerList.add(new ArrayList<drawnObject>());
                         }
                         else if(i == 6) {
                             //remove layer
                             if(layerList.size() != 1) {
                                 layerList.remove(layer);
                                 layer = 0;
                             }
                         }
                     }
                 }
             }
             else {
                 layerMenu = false;
             }
         }
         if(editMenu) {
             //selecting an edit tool
             if(me.getY() >= 350 && me.getY() <= 420 && me.getX() <= 490) {
                 for(int i = 1; i < 7; i++) {
                     if(me.getX() >= (70 * i) && me.getX() <= (70 + (70 * i))) {
                         if(i == 1) {
                             //undo
                             if(!layerList.get(layer).isEmpty()) {
                                 if(layerList.get(layer).get(layerList.get(layer).size() - 1).type == 0 && !lineList.isEmpty()) {
                                     for(int j = lineList.get(lineList.size() - 1) - 1; j > lineList.get(lineList.size() - 2) - 1; j--) {
                                         layerList.get(layer).remove(j);
                                     }
                                     lineList.remove(lineList.size() - 1);
                                     lineList.remove(lineList.size() - 1);
                                     return;
                                 }
                                 deletedObjects.add(layerList.get(layer).remove(layerList.get(layer).size() - 1));
                                 return;
                             }
                         }
                         else if(i == 2) {
                             //redo
                             if(!deletedObjects.isEmpty()) {
                                 layerList.get(layer).add(deletedObjects.remove(deletedObjects.size() - 1));
                                 return;
                             }
                         }
                         else if(i == 3) {
                             //clear
                             layerList.get(layer).clear();
                         }
                         else if(i ==4) {
                             //save
                             saveImage();
                         }
                         else if(i == 5) {
                             //load
                             loadImage();
                         }
                         else if(i == 6) {
                             //quit
                             System.exit(0);
                         }
                     }
                 }
             }
             else {
                 editMenu = false;
             }
         }
         if(paintMenu) {
             //selecting a color
             if(me.getY() >= 420) {
                 for(int i = 1; i < 11; i++) {
                     if(me.getX() >= 70 && me.getX() <= (70 + (70 * i))) {
                         paintMenu = false;
                         
                         if(i == 1) {color = Color.BLACK; return;}
                         else if(i == 10) {color = Color.BLUE; return;}
                         else if(i == 3) {color = Color.GREEN; return;}
                         else if(i == 4) {color = Color.ORANGE; return;}
                         else if(i == 5) {color = Color.PINK; return;}
                         else if(i == 6) {color = Color.RED; return;}
                         else if(i == 7) {color = Color.MAGENTA; return;}
                         else if(i == 8) {color = Color.YELLOW; return;}
                         else if(i == 9) {color = Color.CYAN; return;}
                         else if(i == 2) {color = Color.DARK_GRAY; return;}   
                     }
                 }
             }
         }
     }
     
     public int getSelection(Point p) {
         for(int i = 0; i < 7; i++) {
             if(p.y <= 70 + (i * 70)) {
                 if(selection == 2 && i == 2) {
                     //toggle between rectangle and square
                     if(square) square = false;
                     else square = true;
                 }
                 if(selection == 3 && i == 3) {
                     //toggle between ellipse and circle
                     if(circle) circle = false;
                     else circle = true;
                 }
                 if(i == 5) {
                     //toggle edit menu
                     if(editMenu) editMenu = false;
                     else editMenu = true;
                     return selection;
                 }
                 if(i == 6) {
                     //toggle paint menu
                     if(paintMenu) paintMenu = false;
                     else paintMenu = true;
                     return selection;
                 }
                 if(i == 4) {
                     if(layerMenu) layerMenu = false;
                     else layerMenu = true;
                     return selection;
                 }
                 //returns square number with cursor hit
                 return i;
             }
         }
         //else return invalid
         return 0;
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         if(dragging && selection == 0) {
             //drawing a freehand line, adds a new segment on every mouse drag
             layerList.get(layer).get(layerList.get(layer).size() - 1).updatePosition(e.getPoint());
             layerList.get(layer).add(new drawnObject(e.getPoint(), color, false, 0));
             //lineList.set(lineList.size() - 1, layerList.get(layer).size());
         }
         if(dragging && (selection == 1 || selection == 2 || selection == 3)) {
             //updates square
             layerList.get(layer).get(layerList.get(layer).size() - 1).updatePosition(e.getPoint());
             return;
         }
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         if(e.getX() <= 70 ) {
             if(e.getY() > 140 && e.getY() < 210) {
                 hoverSquare = true;
             }
             else hoverSquare = false;
             if(e.getY() > 210 && e.getY() < 280) {
                 hoverCircle = true;
             }
             else hoverCircle = false;
         }
         else {
             hoverSquare = false;
             hoverCircle = false;
         }
     }
     public void saveImage() {
         offscreen = createImage(this.getWidth(), this.getHeight());
         for(int j = 0; j < layerList.size(); j++) {
                 for(int i = 0; i < layerList.get(j).size(); i++) {
                     layerList.get(j).get(i).paint(offscreen.getGraphics());
                 }
             }
         
         try {
             BufferedImage bi = (BufferedImage)offscreen;
             File outputfile = new File("saved.png");
             ImageIO.write(bi, "png", outputfile);
         } catch (IOException e) {
             System.out.println("fucked up");
         }
         offg = offscreen.getGraphics();
     }
     
     public void loadImage() {
         try {
            URL url = new URL(getCodeBase(), "saved.jpg");
             offscreen = ImageIO.read(url);
         } catch (IOException e) {
             System.out.println("fucked up");
         }
     } 
 }
