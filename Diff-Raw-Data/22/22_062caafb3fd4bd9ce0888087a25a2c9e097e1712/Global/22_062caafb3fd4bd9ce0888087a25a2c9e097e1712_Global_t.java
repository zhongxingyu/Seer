 import play.*;
 import models.*;
 
 public class Global extends GlobalSettings {
 
 	@Override
 	public void onStart(Application app) {
 		Logger.info("Application has started");
		if(Substance.all().size()==0){
			Substance substance = new Substance();
			substance.name = "Первое вещество";
			substance.s_pdk = 1.35;
			substance.k = 1.75;
			substance.save();
			Substance substance2 = new Substance();
			substance2.name = "Второе вещество";
			substance2.s_pdk = 1.35;
			substance2.k = 1.75;
			substance2.save();
			Substance substance3 = new Substance();
			substance3.name = "Третье вещество";
			substance3.s_pdk = 1.35;
			substance3.k = 1.75;
			substance3.save();
		}
 	}
   
 	@Override
 	public void onStop(Application app) {
 		Logger.info("Application shutdown...");
 	}
 }
