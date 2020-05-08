 package org.imirsel.nema.webapp.jobs;
 
 import org.imirsel.nema.service.impl.MeandreProxyWrapper;
 
 /**This Class is called by the quartz scheduler every n number of
  * seconds checking if the meandre server is still running.
  * If the server is disconnected or for some reason unreachable
  * it tries to connect to it again.
  * 
  * @author Amit Kumar
  *
  */
 public class CheckMeandreProxyAvailability {
 	
 	private MeandreProxyWrapper meandreProxyWrapper;
 
 	public void setMeandreProxyWrapper(MeandreProxyWrapper meandreProxyWrapper) {
 		this.meandreProxyWrapper = meandreProxyWrapper;
 	}
 
 	public MeandreProxyWrapper getMeandreProxyWrapper() {
 		return meandreProxyWrapper;
 	}
 	
 	public void checkAvailability(){
 		meandreProxyWrapper.init();
 	}
 
 }
