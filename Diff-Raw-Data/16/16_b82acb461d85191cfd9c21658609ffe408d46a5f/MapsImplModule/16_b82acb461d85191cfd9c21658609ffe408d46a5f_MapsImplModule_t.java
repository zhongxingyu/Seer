 package de.htwg.seapal.maps.app;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.multibindings.Multibinder;
 import de.htwg.seapal.boat.views.tui.BoatTUI;
 import de.htwg.seapal.maps.controllers.IMapsController;
 import de.htwg.seapal.maps.database.IMapsDatabase;
 import de.htwg.seapal.maps.models.IMaps;
 
 import de.htwg.seapal.person.views.tui.PersonTUI;
 import de.htwg.seapal.common.plugin.Plugin;
 
 /**
  * Final Google Guice module description of the maps module.
  * @author Benjamin
  */
 public class MapsImplModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 		// TUI multibindings
 		Multibinder<Plugin> plugins = Multibinder.newSetBinder(binder(), Plugin.class);
 		//plugins.addBinding().to(BoatTUI.class);
		//plugins.addBinding().to(PersonTUI.class);
 	    
 	    // component bindings
 	    bind(IMaps.class).to(de.htwg.seapal.maps.models.impl.Maps.class);
 	    bind(IMapsController.class).to(de.htwg.seapal.maps.controllers.MapsController.class);
 	    bind(IMapsDatabase.class).to(de.htwg.seapal.maps.database.impl.MapsDb4oDatabase.class);	
 	}
 }
