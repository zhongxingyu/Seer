 package org.genericsystem.cdi;
 
 import java.util.Arrays;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 
 import org.genericsystem.core.Engine;
 import org.genericsystem.core.GenericSystem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ApplicationScoped
 public class EngineProvider {
 
 	protected static Logger log = LoggerFactory.getLogger(EngineProvider.class);
 
 	private transient Engine engine;
 
 	@Inject
 	private UserClassesProvider userClassesProvider;
 
 	@Inject
	PersistentDirectoryProvider persistentDirectoryProvider;
 
 	@Inject
 	CdiFactory factory;
 
 	@PostConstruct
 	public void init() {
 		log.info("$$$$$$$$$$$$$$ START GS ENGINE $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
 
 		String logo = "\n";
 		logo += ("____________________________________________________________________________________________________________\n");
 		logo += ("|___________________________________________________________________________________________________________|\n");
 		logo += ("|___________________________________________________________________________________________________________|\n");
 		logo += ("|____________|         ____                      _      ____             __                  /______________|\n");
 		logo += ("|____________|        / ___)___  _  _____  ___  /_)__  / ___)_  __ ___  / /  ___  ____      /_______________|\n");
 		logo += ("|____________|       / /___/ __)/ \\/ / __)/ _ )/ |/ _)/___ \\/ \\/  ) __)/___)/ __)/    )    /________________|\n");
 		logo += ("|____________|      / /_  / __)/    / __)/   \\/  / /_ ___/ /\\    (__  / /_ / __)/ / / /   /_________________|\n");
 		logo += ("|____________|      \\____(____(_/\\_(____(_/\\_(__(____(____/  \\  (____(____(____(_/_/_/   /__________________|\n");
 		logo += ("|____________|                                               /_/                        /___________________|\n");
 		logo += ("|____________|_________________________________________________________________________/____________________|\n");
 		logo += ("|___________________________________________________________________________________________________________|\n");
 		logo += ("|___________________________________________________________________________________________________________|  \n");
 
 		log.info(logo);
 
 		// log.info("");
 		// log.info("   _____________   ____________    ________ ");
 		// log.info("  |___________ /  / ____/  ___/   /________|");
 		// log.info("  |___________/  / /___ \\__  \\   /_________|");
 		// log.info("  |__________/  / /_  /___/  /  /__________|");
 		// log.info("  |_________/   \\____/______/  /___________|");
 		// log.info("");
 		// log.info("");
 
 		log.info("-----------------------------------------------------------------------------------------------");
 		log.info("-  directory path : " + persistentDirectoryProvider.getDirectoryPath());
 		log.info("-  userClasses : " + Arrays.toString(userClassesProvider.getUserClassesArray()));
 		log.info("-----------------------------------------------------------------------------------------------");
 		engine = GenericSystem.newPersistentEngine(factory, persistentDirectoryProvider.getDirectoryPath(), userClassesProvider.getUserClassesArray());
 		// engine = GenericSystem.newInMemoryEngine(factory, userClassesProvider.getUserClassesArray());
 	}
 
 	@Produces
 	public Engine getEngine() {
 		return engine;
 	}
 
 	@PreDestroy
 	public void destroy() {
 		log.info("$$$$$$$$$$$$$$ STOP GS ENGINE $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
 		engine.close();
 		engine = null;
 	}
 }
