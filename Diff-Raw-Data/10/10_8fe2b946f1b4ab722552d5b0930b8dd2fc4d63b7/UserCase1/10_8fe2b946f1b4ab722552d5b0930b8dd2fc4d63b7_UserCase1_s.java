 import java.math.BigInteger;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class UserCase1 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		IniParser iniP = new IniParserImpl();
 		
 		IniSection section = null;
 		try{
 			section = iniP.addSection("Database");
 
 			// both options are mandatory
 			section.defineOptString("user");
 			section.defineOptString("pass");
 			// secure, with default value
 			section.defineOptBoolean("secure", true);
 
 		} catch( Exception e) {
 			System.out.println(e.toString());
 		}
 		// rest of initialization
 
 
 		// read configuration
 		try {
 			iniP.readFile("database.ini");
 		} catch (IOException e) {
 			// catch any io exception
 			e.printStackTrace();
		} catch (ParseException e) {
 			// catch any parser exception
 			e.printStackTrace();
 		}
 
 
 		// access parsed data
 		iniP.getSection("Database").getOption("user").getValueString();
 		iniP.getSection("Database").getOption("pass").getValueString();
 		iniP.getSection("Database").getOption("secure").getValueBool();
 		
 	}
 
 }
