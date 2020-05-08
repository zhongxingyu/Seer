 /**
  * 
  */
 package webctdbexport.jdbc;
 
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.hibernate.Session;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import webctdbexport.db.LearningContext;
 import webctdbexport.jdbc.model.Person;
 import webctdbexport.test.TestRepository;
 import webctdbexport.tools.DumpUtils;
 import webctdbexport.utils.DbUtils;
 //import webctdbexport.utils.MoodleRepository;
 
 /** Dump all users and their own file areas in whole database ?!
  * 
  * @author cmg
  *
  */
 public class DumpUsers {
 	private static final String DONE_FILE = "done.ts";
 	static Logger logger = Logger.getLogger(DumpUsers.class.getName());
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length<3) {
 			System.err.println("Usage: <jdbc.properties> <outputdir> <filedir> [usernames...]");
 			System.exit(-1);
 		}
 		File outputdir = new File(args[1]);
 		if (!outputdir.exists() || !outputdir.canWrite() || !outputdir.isDirectory()) {
 			logger.log(Level.SEVERE, "Output directory does not exist or is not writable: "+outputdir);
 			System.exit(-1);
 		}
 		File filedir = new File(args[2]);
 		if (!filedir.exists() || !filedir.canWrite() || !filedir.isDirectory()) {
 			logger.log(Level.SEVERE, "File directory does not exist or is not writable: "+filedir);
 			System.exit(-1);
 		}
 		Connection conn = JdbcUtils.getConnection(args[0]);
 		
 		try {
 			logger.log(Level.INFO, "output folders to "+outputdir);
 			List<BigDecimal> personIds = null;
 			if (args.length<=3) {
 				logger.log(Level.INFO, "Dump all users...");
				MoodleRepository.getPersonIds(conn);
 				logger.log(Level.INFO, "Found "+personIds.size()+" active nondemo users");
 			} else {
 				personIds = new LinkedList<BigDecimal>();
 				for (int ai=3; ai<args.length; ai++) {
 					Person p = MoodleRepository.getPersonByWebctId(conn, args[ai]);
 					if (p==null)
 						logger.log(Level.WARNING,"Could not find user "+args[ai]);
 					else
 						personIds.add(p.getId());
 				}
 			}
 			for (BigDecimal personId : personIds) {
 				Person p = MoodleRepository.getPerson(conn, personId);
 				if (p==null) {
 					logger.log(Level.WARNING,"Could not find Person "+personId);
 					continue;
 				}
 				String username = p.getWebctId();
 				if (username==null || username.length()<3) {
 					logger.log(Level.WARNING,"Ignoring user "+username+" (short username)");
 					continue;
 				}
 				logger.log(Level.INFO, "Dump Person "+username);
 
 				// learning contexts...
 				JSONObject listing = MoodleRepository.getListingForUser(conn, username, true, true);				
 				List<JSONObject> items = new LinkedList<JSONObject>();
 				if (listing.getJSONArray("list").length()==0)
 				{
 					logger.log(Level.INFO,"Skip user "+username+" (no folder, etc.)");
 					continue;
 				}
 				String user2 = username.substring(0,2);
 				String user3 = username.substring(0,3);
 				File userdir = new File(new File(new File(new File(outputdir, "user"), user2), user3), username);
 				userdir.mkdirs();
 				
 				DumpUtils.writeResponse(listing, userdir);
 				//DumpUtils.addPersonItems(items, listing, "/");
 				DumpUtils.addItems(items, listing, "/");
 
 				DumpAll.processItems(conn, items, outputdir, filedir);
 			}
 			logger.log(Level.INFO, "Done all users");
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error", e);
 		}
 		finally {
 			try { conn.close(); } catch (Throwable ignore) {}
 		}
 	}
 	
 }
