 import tcwi.TCWI;
 
 public class Web_ProjectInitializator {
	private static final String VERSION = "0.0.2.1";
 	private static final String AUTHORS = "EifX & hulllemann";
 
 	public static void main(String[] args) {
 		if(args.length!=3){
 			System.out.println("Help - Web_ProjectInitializator "+VERSION+" by "+AUTHORS);
 			System.out.println("----------------------------------------------------");
 			System.out.println("\nUsage: Web_ProjectInitializator [PROJECTPATH] [PROJECTNAME] [GLOBAL_SETTINGS]");
 			System.out.println("\n\n[PROJECTPATH]");
 			System.out.println("\n     Absolute Path for scan for TypeChef-Files\n");
 			System.out.println("\n[PROJECTNAME]");
 			System.out.println("\n     Project name\n");
 			System.out.println("\n[PROJECTVERSION]");
 			System.out.println("\n     Project version\n");
 			System.out.println("\n[PROJECTAUTHOR]");
 			System.out.println("\n     Project author\n");
 			System.out.println("\n[GLOBAL_SETTINGS]");
 			System.out.println("\n     Absolute Path for the global_settings.xml\n     (include the name of the settings file)\n");
 		}else{
 			TCWI tcwi = new TCWI();
			tcwi.initialisize(args[0], args[1], args[2], args[3], args[4]);
 		}
 	}
 
 }
