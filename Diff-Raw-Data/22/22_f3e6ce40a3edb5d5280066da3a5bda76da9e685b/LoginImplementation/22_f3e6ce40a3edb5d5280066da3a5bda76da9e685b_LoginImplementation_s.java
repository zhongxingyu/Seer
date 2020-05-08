 package gwt.server;
 
 import java.io.IOException;
 
 import gwt.client.LoginService;
 
 import com.unboundid.ldap.sdk.LDAPException;
 
 import dbrown.ldap.Ldap;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class LoginImplementation extends RemoteServiceServlet implements LoginService {
 
 	private static final String WLU_RESOURCE = "resources/wlu.properties";
 	private  Ldap wlu;// = new Ldap(WLU_RESOURCE, "wlu");
 	
 	@Override
 	public int checkLogin(String username, String password)  {
 		try {
 			wlu = new Ldap(WLU_RESOURCE, "wlu");
 			wlu.login(username, password);
 			System.out.println("Connected");
 		} catch (LDAPException e) {
 			e.printStackTrace();
 			System.out.println(e.getMessage());
 			System.out.println("Login fails");
 			return -1;
 		} catch ( IOException ioe) {
 			System.out.println("IO Exception");
 			return -2;
 		}
 		return 0;
 	}
 
 }
