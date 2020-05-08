 package core;
 
 import husbandry.HusAnimal;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.persistence.PersistenceException;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.avaje.ebean.Query;
 
 public class Core extends JavaPlugin {
 
 	Logger log;
 	List<HusAnimal> husAnimalsList;
	//hola
 
 	@Override
 	public void onEnable() {
 
 		log = this.getLogger();
 		
 		log.info("Setting up database");
 		setupDatabase();
 		this.husAnimalsList = this.getDataHusAnimals();
 
 	}
 
 	@Override
 	public void onDisable() {
 		log.info("Saving animals");
 		this.getDatabase().save(husAnimalsList);
 	}
 	
 	/*
 	 *   ANIMALS
 	 * 
 	 */
 	public void saveHusAnimal(Object et) {
 		this.getDatabase().save(et);
 		husAnimalsList.add((HusAnimal) et);
 	}
 
 	private List<HusAnimal> getDataHusAnimals() {
 
 		List<HusAnimal> husAnimalsList2;
 
 		Query<HusAnimal> query = getDatabase().find(HusAnimal.class);
 		husAnimalsList2 = query.findList();
 
 		return husAnimalsList2;
 
 	}
 
 	private void setupDatabase() {
 		try {
 			getDatabase().find(IndePlayer.class).findRowCount();
 			getDatabase().find(HusAnimal.class).findRowCount();
 		} catch (PersistenceException ex) {
 			System.out.println("Installing database for "
 					+ getDescription().getName() + " due to first time usage");
 			installDDL();
 		}
 	}
 
 	@Override
 	public List<Class<?>> getDatabaseClasses() {
 
 		List<Class<?>> classes = new LinkedList<Class<?>>();
 		classes.add(IndePlayer.class);
 		classes.add(MulaPlayer.class);
 		classes.add(HusAnimal.class);
 
 		return classes;
 	}
 }
