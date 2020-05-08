 import play.*;
 import models.*;
 
 public class Global extends GlobalSettings {
 
 	@Override
 	public void onStart(Application app) {
 		Logger.info("Application has started");
		Substance substance = new Substance();
		substance.name = "Новое вещество";
		substance.s_pdk = 1.35;
		substance.k = 1.75;
		substance.save();
 	}
   
 	@Override
 	public void onStop(Application app) {
 		Logger.info("Application shutdown...");
 	}
 }
