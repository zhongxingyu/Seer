 package what.sp_parser;
 
 import java.io.IOException;
 
 import controllers.Localize;
 
 import what.sp_parser.sp_GeoIp.*;
 
 /**
  * This tool adds the City and Country of a request to a dataEntry.
  * It includes GeoLite data created by MaxMind, available from http://www.maxmind.com. It uses the
  * MaxMind Java-API, which is a little bit changed for speed-up.
  * @author Alex
  *
  */
 public class GeoIPTool {
 	
 	/**
 	 * The lookupservice which looks for country and city of every request.
 	 */
 	private static LookupService cl;
 	
 	//Cache for the last IP and location.
 	private static Location lastLoc = null;
 	private static String lastIp = null;
 		
 	/**
 	 * Sets up the IpTool and creates a new lookupService
 	 * @param pm the ParserMediator which will use this tool
 	 */
 	protected static boolean setUpIpTool(ParserMediator pm) {
 		
 		String dir = System.getProperty("user.dir");
 		String seperator = System.getProperty("file.separator");
 		String lookup = dir + seperator + "data\\geoLiteCity.dat";
 				
 		
 		try {
 			cl = new LookupService(lookup);
 		} catch (IOException e) {
 			pm.error(Localize.getString("Error.120"));
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Adds the location info to the IP of a certain dataEntry
 	 * @param pt the parsing-task which parsed the dataEntry
 	 */
 	protected static void getLocationInfo(ParsingTask pt) {
 
 		try {		
 			
 									
 			Location loc;
 			
 			//Looks if the IP is the same as the one which started the last request, uses the same location info
 			//if this is true. If not, it uses the LookupService to create a new Location-object loc.
 			if (pt.getSplitStr()[6].equals(lastIp)) {
 				loc = lastLoc;
 			} else {
				loc = cl.getLocation((String) pt.getSplitStr()[6]);
 			}
 			
			
 			//Sets country and city to the last 2 spots of the DataEntry.
 			pt.getDe().setInfo(loc.countryName, pt.getPm().getConfig().getSize());	
 			pt.getDe().setInfo(loc.city, pt.getPm().getConfig().getSize() - 1);
 			
 			lastLoc = loc;
 			lastIp = (String) pt.getDe().getInfo(6);
						
 			
 			
 		} catch (NullPointerException e) {
 			pt.getDe().setInfo("other", pt.getPm().getConfig().getSize());
 			pt.getDe().setInfo("other", pt.getPm().getConfig().getSize() - 1);
 		}
 	}
 }
