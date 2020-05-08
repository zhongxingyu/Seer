 package de.htwg.seapal.boat.app;
 
 
 import com.google.inject.AbstractModule;
 
 import de.htwg.seapal.boat.controllers.IBoatController;
 import de.htwg.seapal.boat.models.IBoat;
 
 public class BoatMockModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		bind(IBoat.class).to(de.htwg.seapal.boat.models.mock.Boat.class);
 		bind(IBoatController.class).to(de.htwg.seapal.boat.controllers.mock.BoatController.class);	
 	}
 
 }
