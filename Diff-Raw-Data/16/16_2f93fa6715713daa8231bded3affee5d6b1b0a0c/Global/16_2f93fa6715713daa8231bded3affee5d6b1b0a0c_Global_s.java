 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import play.*;
 import utils.*;
 import models.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * Benutzer: Crassus
  * Date: 09.06.13
  * Time: 17:42
  * To change this template use File | Settings | File Templates.
  */
 public class Global extends GlobalSettings {
 	
     @Override
     public void onStart(Application app) {
         if(Benutzer.find.findRowCount()==0){
             new Benutzer("daniel", "1234", "Daniel Rehmann").save();
         }
         if (Archetype.find.findRowCount() == 0) {
         	checkingArchetypes();
         }
     }
     
     public void checkingArchetypes() {
     	ArrayList<File> files = Helper.checkFiles(new File(Helper.getCurrentDir()+"resource/"));
 		for (File aFile : files) {
 			Components comp = new Components(Helper.getCurrentDir()+"resource/"+aFile.getName());
 			try {
 				Module m = comp.getArchetype(Helper.extractFileNameWithoutEnding(aFile.getName()));
				String id = m.getDataElement().get(0).getValue("id");
 				System.out.println(id);
 				String name = Helper.getArcheName(aFile.getName());
 				System.out.println(name);
				String purpose = m.getDataElement().get(0).getValue("purpose");
 				System.out.println(purpose);
				String usage = m.getDataElement().get(0).getValue("use");
 				System.out.println(usage);
				String misusage = m.getDataElement().get(0).getValue("misuse");
 				System.out.println(misusage);
 				Archetype arche = new Archetype( id, name, purpose, usage, misusage);
 				List<Element> temp = new ArrayList<Element>();
				temp.add(Helper.decideWhichType(1, m.getDataElement().get(0).getValue("elementType"), m.getChoices()));
				System.out.println(temp);
 				arche.setElements(temp);
 				arche.save();
 			} catch (Exception e) {
 				System.err.println("failed to generate Archetype object: " + aFile.getName());
 			}
 		}
     }
     
 }
