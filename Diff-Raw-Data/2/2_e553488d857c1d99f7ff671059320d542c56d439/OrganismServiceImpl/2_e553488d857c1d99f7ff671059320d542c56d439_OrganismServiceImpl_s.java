 package org.concord.geniverse.server;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 
 import org.concord.biologica.engine.Characteristic;
 import org.concord.biologica.engine.Organism;
 import org.concord.biologica.engine.Species;
 import org.concord.biologica.engine.SpeciesImage;
 import org.concord.biologica.engine.Trait;
 import org.concord.biologica.engine.World;
 import org.concord.geniverse.client.GOrganism;
 import org.concord.geniverse.client.OrganismService;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class OrganismServiceImpl extends RemoteServiceServlet implements OrganismService {
 	private static final Logger logger = Logger.getLogger(OrganismServiceImpl.class.getName());
 	private static final long serialVersionUID = 1L;
 	private World world = new World("org/concord/biologica/worlds/new-dragons.xml");
 	private Species species = world.getCurrentSpecies();
 	private static int currentDragonNumber = 0;
 
 	private void cleanupWorld(Organism org) {
 		world.deleteOrganism(org);
 	}
 
 	private GOrganism createGOrg(Organism org) {
 		GOrganism gOrg = new GOrganism();
 		gOrg.setName(org.getName());
 		gOrg.setSex(org.getSex());
 		gOrg.setAlleles(org.getAlleleString());
 		gOrg.setImageURL(getOrganismImageURL(org, SpeciesImage.XLARGE_IMAGE_SIZE));
 		
 		HashMap<String, String> characteristicMap = getOrganismPhenotypes(org);
 		ArrayList<String> phenotypes = new ArrayList<String>();
 		phenotypes.addAll(characteristicMap.values());
 		
 		gOrg.setCharacteristics(phenotypes);
 		gOrg.setCharacteristicMap(characteristicMap);
 		return gOrg;
 	}
 
 	private Organism createOrg(GOrganism gOrg) {
 		Organism org = new Organism(world, gOrg.getName(), species, gOrg.getSex(), gOrg.getAlleles());
 		return org;
 	}
 
 	public GOrganism getOrganism(int sex) {
 		System.out.println("getOrganism(int sex) called:");
 		Organism dragon = new Organism(world, sex, "Organism " + (++currentDragonNumber), world.getCurrentSpecies());
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public GOrganism getOrganism(int sex, String alleles) {
 		Organism dragon = new Organism(world, "Organism " + (++currentDragonNumber), world.getCurrentSpecies(), sex, alleles) ;
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public GOrganism getOrganism() {
 		System.out.println("getOrganism(int sex) called:");
 		Organism dragon = new Organism(world, Organism.RANDOM_SEX, "Organism " + (++currentDragonNumber), world.getCurrentSpecies());
 		GOrganism gOrg = createGOrg(dragon);
 		cleanupWorld(dragon);
 		return gOrg;
 	}
 
 	public ArrayList<String> getOrganismPhenotypes(GOrganism gOrg) {
 		Organism org = createOrg(gOrg);
 		ArrayList<String> phenotypes = new ArrayList<String>();
 		phenotypes.addAll(getOrganismPhenotypes(org).values());
 		cleanupWorld(org);
 		return phenotypes;
 	}
 
 	private HashMap<String, String> getOrganismPhenotypes(Organism org) {
 		HashMap<String, String> phenotypes = new HashMap<String, String>();
 
 		Enumeration<Characteristic> chars = org.getCharacteristics();
 		while (chars.hasMoreElements()) {
 			Characteristic c = chars.nextElement();
 			Trait t = c.getTrait();
 			phenotypes.put(t.getName().toLowerCase(), c.getName());
 		}
 
 		return phenotypes;
 	}
 
 	public String getOrganismImageURL() {
 		// FIXME cleanup
 		try {
 			return getOrganismImageURL(getOrganism(), SpeciesImage.XLARGE_IMAGE_SIZE);
 		} catch(Exception e) {
 			return e.toString();
 		}
 	}
 
 	public String getOrganismImageURL(GOrganism organism, int imageSize) {
 		Organism dragon = createOrg(organism);
 		String imageUrl = getOrganismImageURL(dragon, imageSize);
 		cleanupWorld(dragon);
 		return imageUrl;
 	}
 
 	private String getOrganismImageURL(Organism dragon, int imageSize) {
 		String filename = generateFilename(dragon, imageSize);
 		String host = getThreadLocalRequest().getServerName();
 		int port = getThreadLocalRequest().getServerPort();
 		String scheme = getThreadLocalRequest().getScheme();
 		String prefix = scheme + "://" + host;
 		if (port != 80) {
 		    prefix += ":" + Integer.toString(port);
 		}
 		
 		return prefix + "/resources/drakes/images/" + filename;
 	}
 
 	private String generateFilename(Organism org, int imageSize) {
 		
 		if (getCharacteristic(org, "Liveliness").equalsIgnoreCase("Dead")){
 			return "dead-drake.png";
 		}
 		
 
 		// [color]_[sex]_[wing]_[limbs]_[armor]_[tail]_[horn]_[rostralHorn]_[health].png
 
 		String filename = "";
 
 		// color
 		filename += getCharacteristic(org, "Color").toLowerCase().substring(0,
 				2)
 				+ "_";
 
 		// sex
 		filename += org.getSex() == 0 ? "m_" : "f_";
 
 		// wings
 		filename += getCharacteristic(org, "Wings").equalsIgnoreCase("Wings") ? "wing_"
 				: "noWing_";
 
 		// limbs
 		String limbs = "";
 		boolean forelimbs = getCharacteristic(org, "Forelimbs")
 				.equalsIgnoreCase("Forelimbs");
 		boolean hindlimbs = getCharacteristic(org, "Hindlimbs")
 				.equalsIgnoreCase("Hindlimbs");
 		if (forelimbs) {
 			if (hindlimbs) {
 				limbs = "allLimb";
 			} else {
 				limbs = "fore";
 			}
 		} else if (hindlimbs) {
 			limbs = "hind";
 		} else {
 			limbs = "noLimb";
 		}
 		filename += limbs + "_";
 
 		// armor
 		String armor = getCharacteristic(org, "Armor");
 		String armorStr = "";
 		if (armor.equalsIgnoreCase("Five armor")) {
 			armorStr = "a5";
 		} else if (armor.equalsIgnoreCase("Three armor")) {
 			armorStr = "a3";
 		} else if (armor.equalsIgnoreCase("One armor")) {
 			armorStr = "a1";
 		} else {
 			armorStr = "a0";
 		}
 		filename += armorStr + "_";
 
 		// tail
 	    String tail = getCharacteristic(org, "Tail");
 		String tailStr = "";
 		if (tail.equalsIgnoreCase("Long tail")) {
 			tailStr = "flair";
 		} else if (tail.equalsIgnoreCase("Kinked tail")) {
			tailStr = "kinked";
 		} else {
 			tailStr = "short";
 		}
 		filename += tailStr + "_";
 
 		// horns
 		filename += getCharacteristic(org, "Horns").equalsIgnoreCase("Horns") ? "horn_"
 				: "noHorn_";
 		
 		// rostral horn
 		filename += getCharacteristic(org, "Rostral horn").equalsIgnoreCase("Rostral horn") ? "rostral_"
 				: "noRostral_";
 		
 		// health
 		filename += "healthy";
 
 		filename += ".png";
 
 		return filename;
 	}
 	
 	private String getCharacteristic(Organism org, String trait) {
 		// rm "Characteristic: "
 	    Characteristic c = org.getCharacteristicOfTrait(trait);
 	    if (c == null) {
 	        // FIXME probably we should do something smarter... throw a custom exception?
 	        return "unknown";
 	    }
 		return c.toString().split(": ")[1];
 	}
 
 	public GOrganism breedOrganism(GOrganism gorg1, GOrganism gorg2) {
 		System.out.println("breedOrganism(GOrganism gorg1, GOrganism gorg2) called:");
 		Organism org1 = createOrg(gorg1);
 		Organism org2 = createOrg(gorg2);
 		try {
 			Organism child = new Organism(org1, org2, "child");
 			GOrganism gOrg = createGOrg(child);
 			cleanupWorld(child);
 			cleanupWorld(org1);
 			cleanupWorld(org2);
 			return gOrg;
 		} catch (IllegalArgumentException e){
 			logger.log(Level.SEVERE, "Could not breed these organisms!", e);
 
 			cleanupWorld(org1);
 			cleanupWorld(org2);
 			return null;
 		}
 	}
 
     public ArrayList<GOrganism> breedOrganisms(int number, GOrganism gorg1, GOrganism gorg2) {
         logger.warning("Actually breeding " + number + " dragons");
         ArrayList<GOrganism> orgs = new ArrayList<GOrganism>(number);
         Organism org1 = createOrg(gorg1);
         Organism org2 = createOrg(gorg2);
         
         for (int i = 0; i < number; i++) {
             Organism child = new Organism(org1, org2, "child " + i);
             logger.warning("Bred " + child.getSexAsString() + " child: " + child.getAlleleString(false));
             GOrganism gChild = createGOrg(child);
             
             orgs.add(gChild);
             cleanupWorld(child);
         }
         
         cleanupWorld(org1);
         cleanupWorld(org2);
         
         return orgs;
     }
     
     public ArrayList<GOrganism> breedOrganismsWithCrossover(int number, GOrganism gorg1, GOrganism gorg2, boolean crossingOver) {
         logger.warning("Actually breeding " + number + " dragons");
         ArrayList<GOrganism> orgs = new ArrayList<GOrganism>(number);
         Organism org1 = createOrg(gorg1);
         Organism org2 = createOrg(gorg2);
         
         for (int i = 0; i < number; i++) {
             Organism child = new Organism(org1, org2, "child " + i, crossingOver);
             logger.warning("Bred " + child.getSexAsString() + " child: " + child.getAlleleString(false));
             GOrganism gChild = createGOrg(child);
             
             orgs.add(gChild);
             cleanupWorld(child);
         }
         
         cleanupWorld(org1);
         cleanupWorld(org2);
         
         return orgs;
     }
 }
