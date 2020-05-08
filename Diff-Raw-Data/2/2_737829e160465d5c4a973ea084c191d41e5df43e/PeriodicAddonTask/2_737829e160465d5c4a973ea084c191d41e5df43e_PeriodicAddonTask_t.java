 package bashoid;
 
 import java.util.ArrayList;
 import java.util.TimerTask;
 
 
 public class PeriodicAddonTask extends TimerTask {
    private ArrayList<PeriodicAddonListener> listeners = new ArrayList<PeriodicAddonListener>();
 
     public void addEventListener(PeriodicAddonListener pl) {
         listeners.add(pl);
     }
 
     @Override
     public void run() {
         for(PeriodicAddonListener l : listeners)
             l.periodicAddonUpdate();
     }
 
     public void clear() {
         listeners.clear();
     }
 }
