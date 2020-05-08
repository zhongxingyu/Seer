 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.pms.uitzendinggemist;
 
 import javax.swing.JComponent;
 import net.pms.PMS;
 import net.pms.dlna.DLNAMediaInfo;
 import net.pms.dlna.DLNAResource;
 import net.pms.dlna.virtual.VirtualFolder;
 import net.pms.external.AdditionalResourceFolderListener;
 import net.pms.external.ExternalListener;
 import net.pms.external.StartStopListener;
 
 /**
  *
  * @author Paul Wagener
  */
 public class UitzendingGemist implements ExternalListener {
 
     public UitzendingGemist() {
         VirtualFolder mainFolder = new VirtualFolder("Uitzending Gemist", null);
 
         VirtualFolder dezeweekFolder = new VirtualFolder("Deze week", null);
         dezeweekFolder.addChild(new OverzichtDagFolder("Vandaag", "vandaag"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Gisteren", "gisteren"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Maandag", "1"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Dinsdag", "2"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Woensdag", "3"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Donderdag", "4"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Vrijdag", "5"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Zaterdag", "6"));
         dezeweekFolder.addChild(new OverzichtDagFolder("Zondag", "0"));
 
         VirtualFolder titelFolder = new VirtualFolder("Op titel", null);
         titelFolder.addChild(new TitelFolder("0-9", 0));
 
         String alfabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
         for(int i = 0; i < alfabet.length(); i++)
             titelFolder.addChild(new TitelFolder(""+alfabet.charAt(i), i+1));
 
         mainFolder.addChild(dezeweekFolder);
         mainFolder.addChild(titelFolder);
 
 
        PMS.get().getRootFolder(null).addChild(mainFolder);
     }
 
     public JComponent config() {
         return null; 
     }
 
     public String name() {
         return "Uitzending Gemist";
     }
 
     public void shutdown() {
         
     }
 }
