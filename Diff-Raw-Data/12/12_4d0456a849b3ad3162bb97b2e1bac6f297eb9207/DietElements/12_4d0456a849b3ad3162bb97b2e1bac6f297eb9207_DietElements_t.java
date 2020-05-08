 /*@GODIET_LICENSE*/
 /*
  * Elements.java
  *
  * Created on 13 avril 2004, 14:44
  */
 package goDiet.Model;
 
 import Model.ElementCfg;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Iterator;
 
 /**
  * This class is use for all DIET elements (SeD, MA, LA, MA_DAG)
  * @author  rbolze
  */
 public class DietElements extends Elements {
 
     protected boolean useDietStats = goDiet.Defaults.USE_DIET_STATS;     
 
     /* Constructor for Elements. */
     public DietElements(String name, ComputeResource compRes, String binary) {
         super(name, compRes, binary);
         updateCfg();
     }
 
     protected void updateCfg() {
        if (compHost.getEndPointContact() != null) {
             elConfig.addOption(new Option("dietHostname",compHost.getEndPointContact()));
         }
         int port = compHost.allocateAllowedPort();
         // port will be -1 if we don't need to use port, or if all ports
         // have been allocated (in which case we try without specifying port)
         if (port > 0) {            
             elConfig.addOption(new Option("dietPort","" + port));
         }
     }
 
     public void writeCfgFile(FileWriter out) throws IOException {
         for (Iterator it = elConfig.getOptions().iterator(); it.hasNext();) {
             Option o = (Option)it.next();            
             out.write(o.getName()+ " = " + o.getValue() + "\n");
         }
     }
 
     public void setName(String name) {
         super.setName(name);
         this.elConfig.setCfgFileName(name + ".cfg");
     }
 
     public void setUseDietStats(boolean flag) {
         this.useDietStats = flag;
     }
 
     public boolean getUseDietStats() {
         return this.useDietStats;
     }
 }
