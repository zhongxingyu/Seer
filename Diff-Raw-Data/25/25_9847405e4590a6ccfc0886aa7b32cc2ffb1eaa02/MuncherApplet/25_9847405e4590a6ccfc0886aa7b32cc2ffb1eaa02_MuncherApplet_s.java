 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Random;
 
 public class MuncherApplet extends java.applet.Applet implements Runnable{
   
   private int paletteSize;
   private int stepping;
   private Color[] palette;
   private int width;
   private int height;
   private int iteration;
   private byte[][] arena;
   private byte[][] newarena;
   private byte[][] dummy;
//  private long date;
   private Random rnd;
   private boolean stop;
   private boolean live;
 
   public void init(){
     rnd = new Random();
     paletteSize = Integer.parseInt(getParameter("PaletteSize"));
     stepping = Integer.parseInt(getParameter("Stepping"));
     stop = true;
     live = true;
     width = getBounds().width;
     height = getBounds().height;
     iteration = 0;
     arena = new byte[width][height];
     newarena = new byte[width][height];
     palette = new Color[paletteSize];
     for(int i=0;i<paletteSize;i++)
       palette[i] = new Color(Math.abs(rnd.nextInt())%256,Math.abs(rnd.nextInt())%256,Math.abs(rnd.nextInt())%256);
     for(int i=0;i<width;i++)
       for(int j=0;j<height;j++)
         arena[i][j] = (byte) (Math.abs(rnd.nextInt())%paletteSize);
     (new Thread(this)).start();
   }
 
   public void start(){    
     stop = false;
     synchronized(this){
       notify();
     }
   }
 
   public void stop(){
     stop = true;
   }
   
   public void destroy(){
     live = false;
   }
   
   public void run(){
     Graphics g;
     int eater;
     while(live){
       while(stop){
         synchronized(this){
           try{
             wait(10000);
           } catch(InterruptedException ex){;}
         }
       }
 
       if(iteration%stepping == 0){
         g = getGraphics();
         for(int i=0;i<width;i++){
           for(int j=0;j<height;j++){
             g.setColor(palette[arena[i][j]]);
             g.drawLine(i,j,i,j);
           }
         }
       }
 
//      System.out.print("Iteration "+iteration);
//      date = System.currentTimeMillis();
       for(int i=0;i<width;i++){
         for(int j=0;j<height;j++){
           eater = (arena[i][j]+1)%paletteSize;
           if(arena[(i-1+width)%width][j] == eater   ||
              arena[(i+1)%width][j] == eater         ||
              arena[i][(j-1+height)%height] == eater ||
              arena[i][(j+1)%height] == eater
             ) newarena[i][j] = (byte) eater;
           else newarena[i][j] = arena[i][j];
         }
       }
       dummy = newarena;
       newarena=arena;
       arena=dummy;
//      System.out.println(": " + (System.currentTimeMillis()-date));
       iteration++;
     }
     palette = null;
     arena = null;
     newarena = null;
     dummy = null;
     rnd = null;
   }
 
 }
