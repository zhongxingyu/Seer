 package jobs;
 
 import java.io.IOException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.Query;
 
 import org.junit.Before;
 
 
 import models.Enhancer;
 import models.Species;
 import models.TFsite;
 
 import play.Logger;
 import play.db.jpa.JPA;
 import play.jobs.Job;
 import play.jobs.OnApplicationStart;
 import play.test.Fixtures;
 
 
 
@OnApplicationStart
 public class LoadDB extends Job {
 
 	@Before
 	public void setup() {
 		Fixtures.deleteDatabase();
 	}
 
 	public void doJob() throws NumberFormatException, IOException{
 		Logger.info("loading DB now...");
 
 		Species dmel = new Species("dmel");
 		Species dsim = new Species("dsim");
 		Species dyak = new Species("dyak");
 		Species dpse = new Species("dpse");
 		Species anc1 = new Species("#1#");
 		Species anc2 = new Species("#2#");
 		Species anc3 = new Species("#3#");
 		dmel.save();
 		dsim.save();
 		dyak.save();
 		dpse.save();
 		anc1.save();
 		anc2.save();
 		anc3.save();
 
 
 		//Start off by creating one of each class to create the relations
 		Enhancer firstEnhancer = new Enhancer("test");
 		TFsite firstSite = new TFsite(firstEnhancer, "test", "test", 450, 451, 3.4);
 
 		int counter = 0;
 		//Read flat file
 		File dir = new File("/home/sarah/utilities/play-1.2.7/sox_sites/data/");
 		for (File childdir : dir.listFiles()) {
 			System.out.println(childdir.getName());
 			Pattern p = Pattern.compile("(.+)_scan90");
 			Matcher m = p.matcher(childdir.getName());
 			if (m.lookingAt() == true) {
 				String TF = m.group(1);
 				for (File childfile : childdir.listFiles()) {
 					counter++;
 					Logger.info("Counter: " + counter);
 					if (counter%10==0) {
 						flush(TFsite.class);
 						flush(Enhancer.class);
 						flush(Species.class);
 					}
 					Pattern p1 = Pattern.compile("/home/sarah/utilities/play-1.2.7/sox_sites/data/" + TF + "_scan90/(.+)_(.+).ft");
 					Matcher m1 = p1.matcher(childfile.getPath());
 					m1.lookingAt();
 					String eName = m1.group(1);
 					Enhancer enhancer = null;
 					if (counter <= 725) {
 						enhancer = new Enhancer(eName);
 						enhancer.save();
					} else {
						enhancer = Enhancer.find("byName", eName).first();
 					}
 
 					BufferedReader reader = new BufferedReader(new FileReader(childfile));
 					String line = null;
 					String speciesname = null;
 					String sequence = null;
 					int relstart = 0;
 					int relend = 0;
 					double wscore = 0;
 					while ((line = reader.readLine()) != null) {
 						if ((line.indexOf(";") == -1) && (line.indexOf("#seq_id") == -1)) {
 							String[] parts = line.split("\\s");
 							speciesname = parts[0];
 							sequence = parts[6];
 							relstart = Integer.parseInt(parts[4]);
 							relend = Integer.parseInt(parts[5]);
 							wscore = Double.parseDouble(parts[7]);
 
 							Species currentSpecies = Species.find("byName", speciesname).first();
 							TFsite siteOfInterest = null;
 							Query newquery = JPA.em().createQuery("Select t from TFsite t where t.enhancer = :enhancer AND t.TF = :TF AND t.relstart = :relstart");
 							newquery.setParameter("enhancer", enhancer);
 							newquery.setParameter("TF", TF);
 							newquery.setParameter("relstart", relstart);
 							List results = newquery.getResultList();
 							if (results.size() > 0) {
 								siteOfInterest = (TFsite) results.get(0);
 							}	
 							if (siteOfInterest == null) {
 								siteOfInterest = new TFsite(enhancer, TF, sequence, relstart, relend, wscore);
 							} 
 							siteOfInterest.tagSpecies(currentSpecies);
 							siteOfInterest.save();
 							//System.out.printf("TFsite %s saved%n", siteOfInterest.sequence);
 
 							if (enhancer == null) {
 								enhancer = Enhancer.find("byName", eName).first();
 							}
 							enhancer.tagTF(siteOfInterest);
 							enhancer.save();
 						}
 					}
 					reader.close();
 				}
 			}
 		}
 		firstEnhancer.delete();
 		firstSite.delete();
 	}
 
 	private void flush(Class<?> type) {
 		Logger.info("Flushing...");
 		if(type == TFsite.class){
 			TFsite.em().flush();
 			TFsite.em().clear();
 		} else if (type == Enhancer.class){
 			Enhancer.em().flush();
 			Enhancer.em().clear();
 		} else if (type == Species.class) {
 			Species.em().flush();
 			Species.em().clear();
 		}
 
 	}
 }
