 package code.gui.railboard;
 
 import code.model.VIRunnableEndListener;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JLabel;
 
 /**
  *
  * @author Jose Carlos
  */
 public class VAnt extends JLabel implements Runnable
 {
     private List<VIRunnableEndListener> killers;
     private Thread runner;
     private int x;
     private int y;
     
     public VAnt(String text, int x, int y) 
     {
         super(text);
         this.x = x;
         this.y = y;
         this.runner = new Thread();
         this.killers = new ArrayList<VIRunnableEndListener>();
         setLocation(this.x, this.y);
     }
 
     public int x() 
     {
         return x;
     }
 
     public int y() 
     {
         return y;
     }
     
     public void addRunnableEndListener(VIRunnableEndListener listener)
     {
         this.killers.add(listener);
     }
     
     public void go()
     {
         this.runner = new Thread(this);
         this.runner.start();
     }
     
     @Override
     public void run() 
     {
         try
         {
             while (this.y != 0)
             {
                 this.y -= 0.05;
                 setLocation(this.x, this.y);
                 Thread.sleep(10);
             }
            setVisible(false);
         }
         catch (Exception e)
         {   
         }
         finally
         {
             for (VIRunnableEndListener killer : this.killers)
             {
                 killer.finishRunnable();
             }
         }
     }
     
 }
