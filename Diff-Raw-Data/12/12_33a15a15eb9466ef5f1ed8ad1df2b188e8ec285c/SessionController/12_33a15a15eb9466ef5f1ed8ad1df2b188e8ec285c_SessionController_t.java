 package georeduy.server.logic.controllers;
 
 import georeduy.server.logic.model.Client;
 import static georeduy.server.logic.model.Roles.ADMIN;
 import static georeduy.server.logic.model.Roles.REG_USER;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class SessionController {
 	private static SessionController s_instance = null;
 	
 	private Map<String, Client> m_onlineClients;
 	
	public SessionController() {
		m_onlineClients = new HashMap<String, Client>();
	}
	
     public static SessionController getInstance(){
         if (s_instance == null)
         {
         	s_instance = new SessionController();
         }
         
         return s_instance;
     }
     
 	public String LogIn(String userName, String password) {
 		// Esto es de prueba y debe ser remplazado por consultas a la base de datos
		if (userName != null && userName.equals("Agustin") && password != null && password.equals("1234")) {
 			List<String> roles = new ArrayList<String>();
 	    	roles.add(REG_USER);
 	    	roles.add(ADMIN);
 	    	Client client = new Client(0, "Agustin", "Agustin", "Clavelli", "agustin@lavabit.com", roles);
 	    	String token;
 	    	do {
 	    		token = GenerateToken();
 	    	} while (m_onlineClients.containsKey(token));
 	    	m_onlineClients.put(token, client);
 	    	
 	    	return token;
 		}
 		
 	    return null; // Hay que hacer algo para el manejo de errores
 	}
 	
 	public void LogOut(String token)
 	{
 		m_onlineClients.remove(token);
 	}
 	
 	public boolean Register(String userName, String password) {
     	return true;
 	}
 	
 	public boolean isTokenValid(String token) {
 		return m_onlineClients.containsKey(token);
 	}
 	
 	public Client GetClient(String token) {
 		return m_onlineClients.get(token);
 	}
 	
 	private String GenerateToken() {
 	    SecureRandom secureRandom = null;
 	    MessageDigest digest = null;
         try {
 	        secureRandom = SecureRandom.getInstance("SHA1PRNG");
 	        digest = MessageDigest.getInstance("SHA-256");
         } catch (NoSuchAlgorithmException e) {
         }
 	    secureRandom.setSeed(secureRandom.generateSeed(128));
	    return digest.digest((secureRandom.nextLong() + "").getBytes()).toString();
 	}
 }
