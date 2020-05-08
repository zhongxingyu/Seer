package motherbug2;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.service.http.HttpContext;
 
 /**
  * @author jconnolly
  *
  */
 
 public class CaptureHttpContext implements HttpContext {
 
 	public static final String CAPTURE_ALIAS = "/servodata";
 	public static final String NAME = "/Resources";;
 	private BundleContext context;
 	
 	public CaptureHttpContext(BundleContext context){
 		this.context = context;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.service.http.HttpContext#getMimeType(java.lang.String)
 	 */
 	public String getMimeType(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.service.http.HttpContext#getResource(java.lang.String)
 	 */
 	@SuppressWarnings("deprecation")
 	public URL getResource(String path) {
 		String fileName = path.substring( CAPTURE_ALIAS .length() + 1+NAME.length() );
 		try {
 			return context.getDataFile( fileName ).toURL();
 		}
 		catch ( MalformedURLException e ) {
 			System.out.println( "Unable to formulate URL for " + fileName );
 			return null;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.service.http.HttpContext#handleSecurity(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	public boolean handleSecurity(HttpServletRequest request,
 			HttpServletResponse response) throws IOException {
 		// TODO Auto-generated method stub
 		return true;
 	}
 	
 
 }
