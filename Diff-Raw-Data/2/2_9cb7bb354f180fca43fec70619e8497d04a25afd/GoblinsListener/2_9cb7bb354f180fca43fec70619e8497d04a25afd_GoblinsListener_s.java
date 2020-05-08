 import java.util.Random;
 import java.util.logging.Logger;
 
 
 
 public class GoblinsListener extends PluginListener {
 
 	private static Logger log = Logger.getLogger("Minecraft");
 	
 	static Random generator = new Random();
 	
 	  public static String get (String[] array) {
 	        int rnd = generator.nextInt(array.length);
 	        return array[rnd];
 	    }
 	
 	public void onLogin(Player player) {
 		String[] goblins = { "Gmonur", "Nurezk","Gnutz", "Gkazor", "Snuz", "Rezuxk",  "Drak", "Gexomd", "Zekr", "Regoms"};
 
 		String ret = GoblinsListener.get(goblins);
		player.sendMessage("~" + ret + "greets you, 'Welcome master!'");
 	}
 	
 }
 
