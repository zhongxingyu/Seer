 package muCkk.DeathAndRebirth.Messages;
 
 public final class DARErrors {
 
 	private static String prefix = "[Death and Rebirth] ";
 	private static String prefixError = "[Death and Rebirth] ERROR: ";
 	
 	
 	// ___ ERRORS ___
	
 	
 	//DARHandler
 	public static void corruptGhostFile() {
 		System.out.println(prefixError+"Corrupt ghost file!");
 	}
 	public static void couldNotReadGhostFile() {
 		System.out.println(prefixError+"Could not read ghost file!");
 	}
 	public static void couldNotSaveGhostFile() {
 		System.out.println(prefixError+"Could not save ghost-file!");
 	}
 	
 	//DARSigns
 	public static void corruptSignsFile() {
 		System.out.println(prefixError+"Corrupt grave file!");
 	}
 	public static void couldNotReadSignsFile() {
 		System.out.println(prefixError+"Could not read grave file!");
 	}
 	public static void couldNotSaveSignsFile() {
 		System.out.println(prefixError+"Could not save grave-file!");
 	}
 	
 	
 	// ___ MESSAGES ___
 	
 	//DARShrines
 	public static void shrinesLoaded() {
 		System.out.println(prefix+"Shrines loaded.");
 	}
 	
 	//DARGraves
 	public static void gravesLoaded() {
 		System.out.println(prefix+"Graves loaded.");
 	}
 	
 	//Spout
 	public static void foundSpout() {
 		System.out.println(prefix +"Found Spout-plugin!");
 	}
	
 }
