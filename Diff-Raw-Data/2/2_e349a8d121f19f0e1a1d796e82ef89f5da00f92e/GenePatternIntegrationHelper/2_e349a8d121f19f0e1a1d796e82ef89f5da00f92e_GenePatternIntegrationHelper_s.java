 package gov.nih.nci.caintegrator.application.analysis.gp;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import gov.nih.nci.caintegrator.security.PublicUserPool;
 import gov.nih.nci.caintegrator.security.EncryptionUtil;
 import gov.nih.nci.caintegrator.application.analysis.gp.GenePatternPublicUserPool;
 import gov.nih.nci.caintegrator.application.cache.PresentationCacheManager;
 import gov.nih.nci.caintegrator.application.cache.PresentationTierCache;
 import gov.nih.nci.caintegrator.application.util.ApplicationConstants;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 
 public class GenePatternIntegrationHelper {
 	public static String gpPoolString = ":GP30:RBT";
 	private static Logger logger = Logger.getLogger(GenePatternIntegrationHelper.class);
 	
 	public static String gpHomeURL(HttpServletRequest request) 
 				throws Exception {
 		HttpSession session = request.getSession();
 		PresentationTierCache presentationTierCache = PresentationCacheManager.getInstance();
 	    
 		String user;
 		UserCredentials info = (UserCredentials) presentationTierCache.getNonPersistableObjectFromSessionCache(session.getId(), ApplicationConstants.userInfoBean);
         if (info != null ){
 			user = info.getUserName();
 		}
 		else {
 		  //for backward compatability
 		  user = (String) session.getAttribute("name");
 		}
 		
 		String publicUser = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.name");
 		String encryptKey = System.getProperty("gov.nih.nci.caintegrator.gp.desencrypter.key");
 		
 		return gpHomeURL(request, user, publicUser, encryptKey);
 		
 	}
 	
 	public static String gpHomeURL(HttpServletRequest request,String userName,  String publicUserName, String encryptKey)
 		throws Exception {
     	HttpSession session = request.getSession();
     	String ticketString = null;
 		String gpserverURL = System.getProperty("gov.nih.nci.caintegrator.gp.server")!=null ? 
 				(String)System.getProperty("gov.nih.nci.caintegrator.gp.server") : "localhost:8080"; //default to localhost
 		try {
 
			if (userName.equals(publicUserName)){
 				String gpUser = (String)session.getAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME);
 				if (gpUser == null){
 					PublicUserPool pool = GenePatternPublicUserPool.getInstance();
 					gpUser = pool.borrowPublicUser();
 					session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME, gpUser);
 					session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_POOL, pool);
 				}
 				userName = gpUser;
 			}
 			
 			String urlString = EncryptionUtil.encrypt(userName+ gpPoolString, encryptKey);
 			urlString = URLEncoder.encode(urlString, "UTF-8");
 			ticketString = gpserverURL+"gp?ticket="+ urlString;
 			
 			logger.debug(ticketString);
 			URL url;
             try {
             	url = new java.net.URL(ticketString);
             	URLConnection conn = url.openConnection();
             	final int size = conn.getContentLength();
             	logger.debug(Integer.toString(size));
 
             } catch (Exception e) {
             	logger.error(e.getMessage());
             }
 		} catch (Exception e) {
 			StringWriter sw = new StringWriter();
 			PrintWriter pw = new PrintWriter(sw);
 			e.printStackTrace(pw);
 			logger.error(sw.toString());
 			throw new Exception(e.getMessage());
 		}
 		//ticketString
 		logger.debug(ticketString);
 
         return ticketString;
     }
 
 }
