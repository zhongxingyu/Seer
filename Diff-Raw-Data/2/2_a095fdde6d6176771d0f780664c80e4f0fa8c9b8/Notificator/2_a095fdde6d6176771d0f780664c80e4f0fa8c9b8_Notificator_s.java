 package portal.login.server;
 
 import java.io.File;
 import java.util.Enumeration;
 
 import org.apache.log4j.Logger;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.GlobusCredentialException;
 
 import portal.login.util.LoadProperties;
 import portal.login.util.SendMail;
 
 
 public class Notificator implements Runnable{
 	
 	private static final Logger log = Logger
 			.getLogger(Notificator.class);
 	
 	
 	public void run(){
 		try{
 			log.debug("############## NOTIFICATOR ##############");
 			
 			/*
 			 * Recupera cose da notificate
 			 * 
 			 * notifica se il tempo del proxy rimante è inferiore ad un'ora.
 			 * 
 			 */
 			
 			LoadProperties props = new LoadProperties("checkProxy.properties");
 			
 			if(props.getProperties().isEmpty())
 				return;
 			
 			for (Enumeration<Object> e = props.getProperties().keys() ; e.hasMoreElements() ;) {
 				
 				String key = (String) e.nextElement();
 		        log.info(key);
 		        
 		        
 		        String value = props.getValue(key);
 		        log.info(value);
 		        
 		        
 		        String proxyFile = value.split(";")[0];
 		        int limit = Integer.parseInt(value.split(";")[1]);
 		        String mail = value.split(";")[2];
 		        String user = value.split(";")[3];
 		        
 		        String voName =key.split("\\.")[1];
 		        
 		        log.info("RESULT: " + proxyFile + " | " + limit + " | " + voName + " | " + mail + " | " + user);
 		        
 		        File proxy = new File(proxyFile);
 		        
 		        if(proxy.exists()){
 		        	try{
 		        		GlobusCredential cred = new GlobusCredential(
 								proxy.toString());
 		        		
 		        		log.info(cred.getTimeLeft());
 		        		
 		        		long totalSecs =cred.getTimeLeft();
 						long hours = totalSecs / 3600;
 						long minutes = (totalSecs % 3600) / 60;
 						long seconds = totalSecs % 60;
 						
 						String timeLeft = hours + ":" + minutes +  ":" + seconds;
 						
 						log.info(timeLeft);
 						
 						if((minutes<(long) limit)&&(hours==0)){
 							
 							log.info("sono dentro");
 							
 							switch(limit){
 							case 60: props.putValue(key, value.replace("60","30"));
 									
 									 log.info("60  minutes limit");
 							
 									 sendMail(mail, user, limit, voName);
 									 break;
 							case 30: props.deleteValue(key);
 							
 							 		 log.info("30  minutes limit");
 					
 									 sendMail(mail, user, limit, voName);
 									 break;
 							}
 							
 						}
 		        	}  catch (GlobusCredentialException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 						log.error("*** GlobusCredentialException: " + e1.getMessage() + " ***");
 					}
 		        }
 		     }
 
 		}catch (Exception e) {
 			// TODO: handle exception
 			log.error(e.getMessage());
 			e.printStackTrace();
 			return;
 		}
 	}
 
 
 	private void sendMail(String mail, String user, int limit, String voName) {
 		// TODO Auto-generated method stub
		String text= "Dear " + user + ",\n your proxy expire in " + limit + " minutes.\n\n If necessary renew the proxy into https://portal.italiangrid.it \n\n [If you won't receive this mail go to Advanced option into My Data page and uncheck che option.] \n\n      - Portal Administrators";
 		SendMail sm = new SendMail("igi-portal-admin@lists.italiangrid.it", mail, "Proxy Expiration for " + voName, text);
 		sm.send();
 	}
 
 }
