 import java.awt.*;
 import javax.swing.*;
 import java.awt.image.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.io.*;
 
 public class quincunx extends JPanel implements KeyListener {
 
  static int       DRADIUS   = 15; //viewport size
   int              DRADNEG   = (-1) * DRADIUS;
  static int       VRADIUS   = 10;  //sight distance
   Rectangle        VPORT     = new Rectangle(DRADIUS*2,DRADIUS*2);
 
   TileSheet        theTiles;
   Level            theLevel  = new Level(true);
   ArrayList<Actor> actors;
 
   int              COLS;  // cols on board
   int              ROWS;  // rows on board
 
   public quincunx() throws IOException {
     super();
     this.setFocusable(true);
     this.addKeyListener(this);
 
     theTiles    = new TileSheet("resources/height.png",10,10);
 
     actors    = theLevel.getActors();
 
     COLS      = theLevel.getW();
     ROWS      = theLevel.getH();
 
   }
 
   /*
   protected void paintMapIso(Graphics g) {
     int e = 0;
     for(int c=COLS-1;c>=0;c--) {
       for(int r=0;r<ROWS;r++) {
         e = theLevel.getElevation(r*COLS+c);
         theTilesIso.drawTile(g, e, c, r, e, this);
       }
     }
     int actorX = (int)actors.get(0).getX();
     int actorY = (int)actors.get(0).getY();
     int actorI = (int)actors.get(0).getI();
     int actorE = theLevel.getElevation(actorY*COLS+actorX);
     theTilesIso.drawActor(g,actorI,actorX,actorY,actorE,this);
   }
   */
 
   protected void paintMapOrtho(Graphics g) {
     VPORT.setLocation(actors.get(0));
     VPORT.translate(DRADNEG,DRADNEG);
     int XX=(int)VPORT.getX(); int YY=(int)VPORT.getY();
     if(XX<0) { XX=0; } else if((XX + VPORT.getWidth())>COLS)  { XX=COLS-(int)VPORT.getWidth();  }
     if(YY<0) { YY=0; } else if((YY + VPORT.getHeight())>ROWS) { YY=ROWS-(int)VPORT.getHeight(); }
     VPORT.setLocation(XX,YY);
     int i = 0;
 
     for(int c=0;c<COLS;c++) {
       for(int r=0;r<ROWS;r++) {
         if(VPORT.contains(c,r)) theLevel.clrLOS(c,r);
       }
     }
 
     doFOV(actors.get(0));
 
     for(int c=0;c<COLS;c++) {
       for(int r=0;r<ROWS;r++) {
         i = r*COLS + c;
         if(VPORT.contains(c,r)) {
           if( theLevel.isLOS(c,r) ) {
             theTiles.drawTile(g, theLevel.getElevation(i) * 3, c-XX, r-YY, this);
           } else if(theLevel.getSeen(c,r)) {
             theTiles.drawTile(g, theLevel.getElevation(i), c-XX, r-YY, this);
           } else {
             theTiles.drawTile(g, 0, c-XX, r-YY, this);
           }
         }
       }
     }
 
     updateActors();
 
     for(int a=(actors.size()-1);a>=0;a--) {
       if(VPORT.contains(actors.get(a))) {
         if(theLevel.isLOS((int)actors.get(a).getX(), (int)actors.get(a).getY() )) {
           theTiles.drawTile(g,actors.get(a).getI(),((int)actors.get(a).getX()-XX),((int)actors.get(a).getY()-YY),this);
         }
       }
     }
     //drawStatus(g, 321, 0);
   }
 
   void updateActors() {
     for(int a=(actors.size()-1);a>0;a--) {
       Actor thisActor = actors.get(a);
       Point dst = new Point(thisActor.getLocation());
       if(thisActor.canAct()) {
         if(thisActor.getAI()==Actor.ai.TOPLAYER) {
           int deltaX = (int)actors.get(0).getX() - (int)thisActor.getX();
           int deltaY = (int)actors.get(0).getY() - (int)thisActor.getY();
           if(Math.abs(deltaX)>=Math.abs(deltaY)) {
             dst.translate((int)Math.signum((double)deltaX),0);
           } else {
             dst.translate(0,(int)Math.signum((double)deltaY));
           }
           if(canMoveTo(thisActor.getLocation(),dst)) {
             thisActor.move((int)dst.getX(),(int)dst.getY());
           }
         }
       } 
     }
   }
 
   protected void paintComponent(Graphics g) {
     paintMapOrtho(g);
   }
 
   public static void main(String arg[]) throws IOException {
     JFrame theFrame = new JFrame("Quincunx");
     theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     quincunx panel = new quincunx();
     theFrame.setSize(437,358);
     theFrame.setContentPane(panel);
     theFrame.setVisible(true);
     panel.requestFocus();
   }
 
   public void keyPressed(KeyEvent e) {
     processKey(e);
   }
 
   public void processKey(KeyEvent e) {
     Point t = new Point(0,0);  // move destination
     Point p = new Point(0,0);  // push destination
     Point a = actors.get(0).getLocation();
     switch (e.getKeyCode()) {
       case KeyEvent.VK_LEFT:
         t.setLocation(-1,0);  p.setLocation(-2,0);
         break;
       case KeyEvent.VK_RIGHT:
         t.setLocation(1,0);  p.setLocation(2,0);
         break;
       case KeyEvent.VK_UP:
         t.setLocation(0,-1);  p.setLocation(0,-2);
         break;
       case KeyEvent.VK_DOWN:
         t.setLocation(0,1);  p.setLocation(0,2);
         break;
       default:  // some other unused key
         return;
     }
     t.translate((int)a.getX(),(int)a.getY());
     p.translate((int)a.getX(),(int)a.getY());
 
     int q = isOccupied(t);
     if(q>0) {
       switch(actors.get(q).getInteraction()) {
         case PUSH:
           if(canMoveTo(t,p)) {
             actors.get(0).setLocation(t);
             actors.get(q).setLocation(p);
           }
           break;
         case HIT:
           System.out.println("OUCH!");
           break;
         case PORT:
           actors.get(0).setLocation(actors.get(actors.get(q).getDest()));
           break;
         case TALK:
           System.out.println("blah blah blah");
           break;
         case NONE:
           actors.get(0).setLocation(t);
           break;
         case COLLIDE:
           break;
         default:
         break;
       }
     }
     else {
       if(canMoveTo(a,t)) {
         actors.get(0).setLocation(t);
       }
     }
     repaint();
   }
 
   void drawStatus(Graphics g, int x, int y) {
     g.setColor(new Color(30,30,30));
     g.fillRect(x, y, 100, 320);
   }
 
   boolean canMoveTo(Point src, Point dst) {
     Rectangle r = new Rectangle(COLS,ROWS);
     if(!r.contains(dst)) { return(false); }
 
     int dstElevation = theLevel.getElevation(dst) ;
     int srcElevation = theLevel.getElevation(src);
     if(Math.abs(dstElevation-srcElevation) > 1) { return(false); }
     
     if(theLevel.collides((int)dst.getX(),(int)dst.getY())) { return(false); }
     
     int a = isOccupied(dst);
     if(a>0) { return(!actors.get(a).hasInteraction()); }
     return(true);
   }
 
   int isOccupied(Point t) {
     for(int i=0;i<actors.size();i++) {
       if(actors.get(i).getLocation().equals(t)) { return(i); }
     }
     return(-1);
   }
 
   void doFOV(Point src) {
     double r;
     double vx, vy;
     double cx, cy;
     double twoPI     = Math.PI * 2;
     double oneDegree = twoPI / 360;
     int    i;
 
     for(r=0 ; r<twoPI ; r+=oneDegree) {
       vx = Math.cos(r);
       vy = Math.sin(r);
 
       cx = src.getX();
       cy = src.getY();
 
      if(VPORT.contains(cx,cy)) {
        for(i=0 ; i<VRADIUS ; i++) {
          theLevel.setLOS((int)cx,(int)cy);
          theLevel.setSeen((int)cx,(int)cy);
          if(theLevel.getElevation((int)cx,(int)cy)==0) break;
          cx+=vx;
          cy+=vy;
        }
       }
 
     }
 
   }
 
   int linearize(Point p) { return( (int)p.getY()*COLS + (int)p.getX() ); }
 
   public void keyTyped(KeyEvent e)    { }
   public void keyReleased(KeyEvent e) { }
   
 }
