 package nsf;
 
 import com.ibm.sbt.services.client.connections.profiles.Profile;
 import com.ibm.sbt.services.client.connections.profiles.ProfileService;
 import com.ibm.sbt.services.client.connections.profiles.ProfileServiceException;
 import com.ibm.sbt.services.endpoints.BasicEndpoint;
 import com.ibm.sbt.services.endpoints.ConnectionsBasicEndpoint;
 import com.ibm.sbt.services.endpoints.ConnectionsSSOEndpoint;
 import com.ibm.sbt.services.endpoints.SSOEndpoint;
 
 public class ConnectionsBean {
 	private ConnectionsBasicEndpoint connBasicBean = null;
 	private static ConnectionsSSOEndpoint connSSOBean = null;
 	
 	public final String CONNECTIONS_BASIC = "basic";
 	public final String CONNECTIONS_SSO = "sso";
 	
 	public ConnectionsBean() {
 		//AUTO-GENERATED constructor
 	}
 	
 	public BasicEndpoint createBasicBean(String beanName, String URL) {
 		if(beanName.equals("connections")) {this.connBasicBean = new ConnectionsBasicEndpoint();
 			connBasicBean.setForceTrustSSLCertificate(true);
 			connBasicBean.setUrl(URL);
 			return connBasicBean;
 		}else{
 			return null;
 		}
 	}
 	
 	public SSOEndpoint createSSOBean(String beanName, String URL) {
 		if(beanName.equals("connections")) {
 			connSSOBean = new ConnectionsSSOEndpoint();
 			connSSOBean.setForceTrustSSLCertificate(true);
 			connSSOBean.setUrl(URL);
 			return connSSOBean;
 		}else{
 			return null;
 		}
 	}
 	
 	public BasicEndpoint getBasicBean(String beanName) {
 		if(beanName.equals("connections")) {
 			return this.connBasicBean;
 		}else{
 			return null;
 		}
 	}
 	
 	public static SSOEndpoint getSSOBean(String beanName) {
 		if(beanName.equals("connections")) {
 			return connSSOBean;
 		}else{
 			return null;
 		}
 	}
 	
 	/**
 	 * getConUserId calls the SBTSDK to get a Unique User Id
 	 * 
 	 * @param email
 	 * @return
 	 */
 	public static String getConUserId(String email) {
 		String result = "";
 		try {
 			ProfileService profileService = new ProfileService(connSSOBean);
 			Profile profile = profileService.getProfile(email);
 			result = profile.getName();
 		} catch (ProfileServiceException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	/*
 	 * Temporary hack for making the page sleep for testing loading functionality
 	 */
 	public void sleep(int milliseconds) {
 		try{
 			Thread.sleep(milliseconds);
 		}catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
