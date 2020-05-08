 package bootstrap;
 
 import java.util.List;
 
 import models.Admin;
 import models.Location;
 
 import org.springframework.context.support.GenericXmlApplicationContext;
 
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import simulation.Dataset;
 
 public class Global extends GlobalSettings {
 
 	private static GenericXmlApplicationContext springContext;
 
 	@Override
 	public void onStart(final Application app) {
 
 		loadSpringContext();
 
 		loadDataset();
 
 		super.onStart(app);
 	}
 
 	protected void loadDataset() {
 		if (Location.locationsCount() == 0) {
 
 			Logger.info("Loading some datasets...");
 			for (int i = 0; i <= 8; i++) {
				List<Location> locations = Dataset.findLocations(Dataset.SIMULATION_DATASET_JSON, String
 						.valueOf(i));
 				if (locations != null) {
 					for (Location location : locations) {
 						location.save();
 					}
 				}
 			}
 
 			// create a new Admin
 			new Admin("ndeverge@ekito.fr", "secret").save();
 		}
 	}
 
 	protected void loadSpringContext() {
 		springContext = new GenericXmlApplicationContext();
 
 		// start the Spring context
 		springContext.load("/spring/springContext.xml");
 		springContext.refresh();
 	}
 
 	@Override
 	public void onStop(final Application app) {
 		destroySpringContext();
 
 		super.onStop(app);
 	}
 
 	protected void destroySpringContext() {
 
 		// close the spring context
 		springContext.close();
 	}
 
 	public static <T> T getBean(final Class<T> clazz) {
 		return springContext.getBean(clazz);
 	}
 }
