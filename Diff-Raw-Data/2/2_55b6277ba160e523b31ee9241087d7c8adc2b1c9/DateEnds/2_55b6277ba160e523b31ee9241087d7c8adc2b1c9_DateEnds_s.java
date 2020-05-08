 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dateends;
 
 /**
  *
  * @author messuti.edd
  */
 public class DateEnds {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
 
 		String curDir = System.getProperty("user.home");
 
		SecureAppProperty sapp = new SecureAppProperty("Anferth TPV", curDir, "anferth_tpv_config.dat", 1);
 		
 //		sapp.saveFile();
 //		sapp.loadFile();
 		System.out.println(sapp.check());
 
 		if (args.length > -1) {
 		}
 	}
 }
