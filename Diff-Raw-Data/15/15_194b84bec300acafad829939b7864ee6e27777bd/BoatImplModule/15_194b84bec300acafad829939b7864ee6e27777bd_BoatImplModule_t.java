 package de.htwg.seapal.boat.app;
 
 
 import com.google.inject.AbstractModule;
 
 import de.htwg.seapal.boat.controllers.IBoatController;
import de.htwg.seapal.boat.database.IBoatDatabase;
 import de.htwg.seapal.boat.models.IBoat;
 import de.htwg.seapal.person.controllers.IPersonController;
 import de.htwg.seapal.person.models.IPerson;
 
 public class BoatImplModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		bind(IBoat.class).to(de.htwg.seapal.boat.models.impl.BoatEBean.class);
		bind(IBoatController.class).to(de.htwg.seapal.boat.controllers.impl.BoatController.class);	
		bind(IBoatDatabase.class).to(de.htwg.seapal.boat.database.impl.BoatDb4oDatabase.class);	
 	}
 
 }
