 import java.util.*;
 import java.io.*;
 import javax.swing.*;
 
 class Core{
     static Graph g;
     static Calendar c;
     static LogWindow lw;
     static GUI_menu menu;
     static volatile boolean isClear;
     //static Object lock = new Object();
     
     /**
      * =Continue
      */
    public static synchronized void cont()  throws Exception
     {
        //isClear = true;
        c.end();
         /*synchronized(c)
         {
             c.notifyAll();
         }*/
         //isClear = true;
     }
     
     public static void generateNew()
     {
         g = new Graph();
         g.generate();
         //System.out.println("1");
         g.generatePeople();
         //System.out.println("2");
         g.createEdge();
         //System.out.println("3");
         g.createSupplied();
         //System.out.print(g.statistic());
 
         lw.log(g.statistic());
     }
     
     public static void load()
     {
         try
         {
             g.load("graph.txt");
         }
         catch (IOException e)
         {
             return;
         }
         lw.log(g.statistic());
     }
     
     public static synchronized void log(String s)
     {
         lw.log(s);
         //System.out.println(s);
     }
     
    public static synchronized void pause()  throws Exception
     {
        //isClear = false;
        c.end();
         /*synchronized(c)
         {
             try
             {
                 while(!isClear)
                 {
                     c.wait();
                 }
                 //c.yield();
                 //c.notify();
             }
             catch (InterruptedException ie)
             {
                 ie.printStackTrace();
                 return;
             }
         }*/
         //isClear = false;
     }
     
     public static void save()
     {
         //g.save("graph.txt");
         //System.out.println(Arrays.toString(g.dijkstra(g.firstNode,g.lastNode)));
         try
         {
             g.save("graph.txt");
         }
         catch (IOException e)
         {
             return;
         }
     }
     
     public static synchronized void start()
     {
         //lw = new LogWindow();
         isClear = true;
         
         c = new Calendar(g);
 
         //c.startt();
         /*try
         {
             c.createStatistics();
         }
         catch (Exception e)
         {
         }*/
     }
     
 //<<<<<<< HEAD
     public static void stop() throws Exception
     {
         //c.end();
     }
     
 //=======
 //>>>>>>> 67850bb8e846596279e9c927ffc5b3973eb18057
     public static void main(String[] args) throws Exception {
         
         //System.out.print(Arrays.toString(g.nodes));
         
         g = new Graph();
         
         //System.setProperty("swing.defaultlaf", "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
         /*javax.swing.SwingUtilities.invokeLater(new Runnable()
         {
             public void run() {
                 new GUI_menu();
             }
         }
         );*/
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception useDefault)
         {
             return;
         }
         
         javax.swing.SwingUtilities.invokeLater(menu = new GUI_menu());
         
         //lw = new LogWindow();
         Thread t = new Thread(lw = new LogWindow(), "LW");
         //Thread t = new Thread(lw, "LW");
         t.run();
         //lw = new LogWindow();
         //lw.run();
         
     }
     
 }
