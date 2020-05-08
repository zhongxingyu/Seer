 package server;
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.restlet.Application;
 import org.restlet.Restlet;
 import org.restlet.routing.Router;
 
 public class SpeciesServerApplication extends Application{
 
     // Liste des espèces persistente en mémoire
 	private final ConcurrentMap<String, SpeciesStats> speciesMap = new ConcurrentHashMap<String, SpeciesStats>(); 
     
 	public SpeciesServerApplication(){		
 		SpeciesStats loup = new SpeciesStats();
 		loup.setNom("Loup");
 		loup.setDescriptif("Carnivore méchant");
 		loup.setIsHerbivorious(false);
 		loup.setType("Canis Rufus");
 		loup.setSmellPoint(270.0);
 		loup.setVisionPoint(45.0);
 		loup.setMovePoint(120000.0);
 		loup.setMaxLifetime(11.0*365.25);
 		loup.setMinimumWeightToDeath(25.0);
 		loup.setWeightConsumeByDay(4.5);
 		loup.setMaxNbDaySafe(7.0);
 		loup.setAttackPoint(80.0);
 		loup.setDefendPoint(50.0);
 		loup.setIsUseHiddenDefense(false);
 		loup.setInitWeight(40.0);
 		loup.setInitAge(22.0 * 30.5);
 		loup.setBirthRateByDay(6.0/365.25);
 		loup.getEatableFoodList().add("Lepus Europaeus");
 		getStats().put(loup.getType(), loup);
 			
 		SpeciesStats lievre =  new SpeciesStats();
 		lievre.setNom("Lièvre");
 		lievre.setDescriptif("gentil petit");
 		lievre.setIsHerbivorious(true);
		lievre.setType("Lepus");
 		lievre.setSmellPoint(100.0);
 		lievre.setVisionPoint(30.0);
 		lievre.setMovePoint(18.5*3600*24);
 		lievre.setMaxLifetime(7.0*365);
 		lievre.setMinimumWeightToDeath(1.0);
 		lievre.setWeightConsumeByDay(0.4);
 		lievre.setMaxNbDaySafe(7.0);
 		lievre.setAttackPoint(5.0);
 		lievre.setDefendPoint(40.0);
 		lievre.setIsUseHiddenDefense(true);
 		lievre.setInitWeight(4.0);
 		lievre.setInitAge(3.5 * 30.5);
 		lievre.setBirthRateByDay(4.0*9.0*0.75/365.25);
 		getStats().put(lievre.getType(), lievre);	
 	}
 	
 	@Override
 	public Restlet createInboundRoot() {
 		Router router = new Router(getContext());
 		router.attach(
 				"/species/{id}",
 				SpeciesServiceResource.class);
 		
 		router.attach(
 			"/species/index/",
 			SpeciesListServiceResource.class);
 		return router;
 	}
 	
 	//retourne la table
     public ConcurrentMap<String, SpeciesStats> getStats() {  
         return speciesMap;
     }
     
 }
 
