 package computingmusic;
 
 
 
 public class Version {
 
	public static String version = "0.0822";
 	public static double v = Double.parseDouble(version);
 	//public static String version = "0.2";
 	
 	public static void check() {
 		//String newVersion = Logger.latestVersion();
 		//double v1 = Double.parseDouble(newVersion);
 		/*if (v < v1){
 			JOptionPane.showMessageDialog(null,
 				"A newer version of String Fingering is available at\n "
 				+	"http://www.colba.net/~eliot/fingering.html.");
 		// JOptionPane.showMessageDialog(null,"What does null do?");
 		}*/
 	}
 
 	public static void main(String[] s){
 		check();
 	}
 }
