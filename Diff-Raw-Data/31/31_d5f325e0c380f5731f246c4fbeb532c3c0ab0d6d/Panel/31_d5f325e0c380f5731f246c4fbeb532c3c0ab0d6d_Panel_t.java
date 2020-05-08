 package general;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vans
  * Date: 12.04.13
  * Time: 14:19
  * To change this template use File | Settings | File Templates.
  */
 public class Panel extends JPanel implements Runnable {
     private Image dbImage;
     private Graphics dbg;
 
     static final int GWIDTH = 1024, GHEIGHT = 768;
     static final Dimension fieldDim = new Dimension(GWIDTH,GHEIGHT);
 
     private Thread field;
     private volatile boolean running = false;
 
     Tile tile;
     Tile[][] tiles;
     
     Dip dip;
     int coordX = 0;
     int coordY = 0;
 
     public Panel()
     {
         //tile = new Tile(250,200);
         setPreferredSize(fieldDim);
         setBackground(Color.WHITE);
         setFocusable(true);
         requestFocus();
         tiles = new Tile[64][64];
         Scheme.getInitData();
         
         //here we initialize every tile
         for (int i = 0; i < 64; i++)
         {
             for (int j = 0; j < 64; j++)
             {
                tiles[i][j] = new Tile(i*Tile.tileSize,j*Tile.tileSize);
                 tiles[i][j].setType(Scheme.tiles[i][j]);
             }
         }
         
     }
 
 
     public void run()
     {
         while (running)
         {
             fieldUpdate();
             fieldRender();
             paintScreen();
         }
     }
 
     private void fieldUpdate(){
         if (running && field != null)
         {
 
         }
     }
 
 
     private void fieldRender(){
         if (dbImage == null){
            dbImage = createImage(GWIDTH, GHEIGHT);
             if (dbImage == null){
                 System.err.println("dbImage still null");
                 return;
             }else{
               dbg = dbImage.getGraphics();
             }
         }
         //clear the screen
         dbg.setColor(Color.WHITE);
         dbg.fillRect(0,0,GWIDTH, GHEIGHT);
         //draw field elements
         draw(dbg);
     }
 
     public void draw(Graphics g)
     {
 
         for (int i = 0; i < 64; i++)
         {
             for (int j = 0; j < 64; j++)
             {
                 tiles[i][j].setType(Scheme.tiles[i][j]);
 
                 if (Scheme.labels[i][j]!="")
                 {
                 	tiles[i][j].makeTextTile(Scheme.labels[i][j]);
                 }
                 
                 tiles[i][j].draw(g);
             }
         }
     }
 
     private void paintScreen(){
      Graphics g;
         try{
             g = this.getGraphics();
             if (dbImage != null && g != null){
                 g.drawImage(dbImage,0,0,null);
             }
             g.dispose();
         }catch (Exception e){
             System.err.println(e);
         }
     }
     private void startField(){
           if (field == null || !running)
           {
               field = new Thread(this);
               field.start();
               running = true;
           }
     }
 
     public void stopField(){
         if (running)
             running = false;
 
     }
 
     public void addNotify(){
         super.addNotify();
         startField();
     }
 }
