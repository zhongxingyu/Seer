 package org.openplans.security;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.logging.Logger;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Iterator;
 import java.util.Properties;
 
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.acegisecurity.Authentication;
 import org.acegisecurity.AuthenticationException;
 import org.acegisecurity.BadCredentialsException;
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.GrantedAuthorityImpl;
 import org.acegisecurity.providers.AuthenticationProvider;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.acegisecurity.userdetails.User;
 
 import org.vfny.geoserver.global.GeoserverDataDirectory;
 import org.vfny.geoserver.global.ConfigurationException;
 
 /**
  * An authentication provider that can validate OpenPlans.org username and authentication token pairs.
  * The authentication token consists of a hash of the username and a secret key; validation is done by 
  * hashing the plaintext username with the secret key and comparing with the token. 
  * 
  * @author David Winslow - TOPP
  */
 public class OpenPlansAuthenticationProvider implements AuthenticationProvider {
 
 	/**
 	 * The secret key.
 	 */
 	final String secret;
 	
     private Map roles;
 
     static Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.community");
   
   private void loadRoles() {
     Map tempRoles = new HashMap();
     try{
       File securityDir = GeoserverDataDirectory.findCreateConfigDir("security");
       File roleFile = new File(securityDir, "roles.properties");
       Properties roleProperties = new Properties();
       roleProperties.load(new BufferedInputStream(new FileInputStream(roleFile)));
       
       Iterator it = roleProperties.entrySet().iterator();
       while(it.hasNext()){
         try {
           Map.Entry entry = (Map.Entry)it.next();
           String username = (String)entry.getKey();
           String[] roleArray = ((String)entry.getValue()).split(",");
           Set roleSet = new TreeSet();
           for (int i = 0; i < roleArray.length; i++){
             roleSet.add(roleArray[i]);
           }
           
           tempRoles.put(username, roleSet);
         } catch (Exception e){
           continue; // of course any problems can be ignored while parsing the file! we have defaults!
         }
       }
     } catch (ConfigurationException gce){
       LOGGER.warning("Couldn't find or create geoserver security directory!!!");
     } catch (IOException ioe){
       LOGGER.warning("Couldn't read extra roles file");
     }
     roles = tempRoles;
   }
 
 	
 	/**
 	 * Override the default constructor to read in the secret key from a file on disk.
 	 */
 	public OpenPlansAuthenticationProvider (){
     loadRoles(); 
 		String tempSecret = "";
 		try{
 			File secretFile = new File("/var/lib/secret.txt");
 			BufferedReader br =
 				new BufferedReader(new InputStreamReader(new FileInputStream(secretFile)));
 			tempSecret = br.readLine();
 		} catch (IOException ioe){
 			tempSecret = "blah";
 			LOGGER.severe("couldn't read file for secret");
 		}
 		secret = tempSecret;
 	}
 	
 	public Authentication authenticate(Authentication arg0)
 			throws AuthenticationException {
 		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken)arg0;
 
         String token = (auth.getCredentials() == null ? "" : auth.getCredentials().toString());
     
         token = token.substring(0, 40);  
             // the token is expected to be 40 characters, this may change depending on the hash function used
             // the truncating is only needed to deal with weird garbage characters added by tomcat
         
        LOGGER.info("input:" + token);
 
 		if (getAuth(auth.getName()).equals(token)) {
 			return createNewAuthentication(auth);
 		}
 		
 		throw new BadCredentialsException("something went wrong");
 	}
 
 	/**
 	 * Create an authenticated AuthenticationToken with the same credentials as an existing one.
 	 * @param auth the original authentication token
 	 * @return the authenticated one
 	 */
     private Authentication createNewAuthentication(UsernamePasswordAuthenticationToken auth) {
       GrantedAuthority[] ga = null;
       Set roleSet = null;
       
       try { 
         roleSet = (Set)roles.get(auth.getName());
       } catch (Exception e){
         // we can ignore this error because we handle the case where the map.get() returns null anyway
       } 
       
       if (roleSet == null) {
         roleSet = new TreeSet();
        roleSet.add("ROLE_AUTHENTICATED");
       }
       
       ga = new GrantedAuthority[roleSet.size() + (auth.getAuthorities() == null? 0 : auth.getAuthorities().length)];
       
       for (int i = 0;
            auth.getAuthorities() != null 
            && i < auth.getAuthorities().length;
            i++){
              ga[i] = auth.getAuthorities()[i];
            }
       
       Iterator iter = roleSet.iterator();
       for (int i = (auth.getAuthorities() != null ? auth.getAuthorities().length : 0);
            i < ga.length;
            i++){
              ga[i] = new GrantedAuthorityImpl((String)iter.next());
            }
 
 	  UsernamePasswordAuthenticationToken upat =
 		  new UsernamePasswordAuthenticationToken(
 				  new User(auth.getName(),
 					  (auth.getCredentials() == null ? null : auth.getCredentials().toString()),
 					  true,
 					  true,
 					  true,
 					  true,
 					  ga
 					  ),
 				  auth.getCredentials(),
 				  ga
 				  );
 	  return upat;
 	}
 
 	public boolean supports(Class arg0) {
 		return UsernamePasswordAuthenticationToken.class.equals(arg0);
 	}
 
 	/**
 	 * Find the authentication token for a particular username
 	 * @param username the username to authenticate
 	 * @return the token for that username
 	 */
 	private String getAuth(String username) {
 		String auth = "";
 		SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "SHA");
 		try {
 			Mac mac = Mac.getInstance("HmacSHA1");
 			mac.init(key);
 			mac.update(username.getBytes());
 			byte[] result = (mac.doFinal());
 			
 			String charmap = "0123456789abcdef";
 			for (int i = 0; i < result.length; i++) {
 				int first = (result[i] >> 4) & 0x0f;
 				int second = result[i] & 0x0f;
 				auth += (charmap.charAt(first));
 				auth += (charmap.charAt(second));
 			}
 		} catch (Exception nsae) {
 			nsae.printStackTrace();
 		}
        LOGGER.info("auth: " + auth);
 		return auth;
 	}
 }
