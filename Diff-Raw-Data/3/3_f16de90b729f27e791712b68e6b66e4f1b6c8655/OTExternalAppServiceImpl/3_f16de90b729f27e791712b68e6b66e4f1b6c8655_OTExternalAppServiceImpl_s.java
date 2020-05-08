 package org.concord.otrunk.view;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import org.concord.framework.otrunk.view.OTExternalAppService;
 
 public class OTExternalAppServiceImpl
     implements OTExternalAppService
 {
 	public boolean showDocument(URL url)
 	{
 		if(jnlpShowDocument(url)){
 			return true;
 		}
 		
 		// if we are here the jnlp classes can't be found
 		
 		String osName = System.getProperty("os.name");
 		if(osName.equals("Mac OS X")){
 			// on OSX the open command can be used.
 			try {
 	            Runtime.getRuntime().exec(new String []{"open", url.toExternalForm()});
 	            return true;
             } catch (IOException e1) {
 	            // TODO Auto-generated catch block
 	            e1.printStackTrace();
             }			
 		}
 		
 		if(osName.startsWith("Windows")){
 			// on windows the start command can be used
 			try {
 	            Runtime.getRuntime().exec(new String []{"cmd", "/c", "start", url.toExternalForm()});
 	            return true;
             } catch (IOException e1) {
 	            // TODO Auto-generated catch block
 	            e1.printStackTrace();
             }						
 		}
 		System.err.println("Opening links outside of jnlp on " + osName +
 				" is not supported yet");
 		
 		return false;
 	}
 
 	public boolean jnlpShowDocument(URL url)
 	{
 		try {
			// FIXME this should be changed to be a service
			// so external links can work in both a jnlp
			// env and a regular application env
 			Class serviceManager = Class
 					.forName("javax.jnlp.ServiceManager");
 			Method lookupMethod = serviceManager.getMethod(
 					"lookup", new Class[] { String.class });
 			Object basicService = lookupMethod.invoke(null,
 					new Object[] { "javax.jnlp.BasicService" });
 			Method showDocument = basicService.getClass()
 					.getMethod("showDocument",
 							new Class[] { URL.class });
 			showDocument.invoke(basicService,
 					new Object[] { url });
 			return true;
 		} catch (ClassNotFoundException e){
 			// the jnlp classes can't be found so 
 			return false;
 		}catch (Exception exp) {
 			// Some other exception occurred.
 			System.err.println("Can't open external link.");
 			exp.printStackTrace();
 		}
 		return false;		
 	}
 }
