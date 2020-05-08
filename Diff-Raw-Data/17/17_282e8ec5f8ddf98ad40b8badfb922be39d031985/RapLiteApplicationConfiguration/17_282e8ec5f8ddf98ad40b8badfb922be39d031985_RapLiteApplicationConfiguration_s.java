 package org.eclipse.rap.lite;
 
 import org.eclipse.rap.rwt.application.Application;
 import org.eclipse.rap.rwt.application.ApplicationConfiguration;
 import org.eclipse.rap.rwt.internal.application.ApplicationImpl;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.FrameworkUtil;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 
 
 @SuppressWarnings("restriction")
 public class RapLiteApplicationConfiguration implements ApplicationConfiguration {
 
   @Override
   public void configure( Application application ) {
     application.addEntryPoint( "/application", RapLiteEntryPoint.class, null );
     ApplicationImpl impl = ( ApplicationImpl )application;
     impl.addClientProvider( new RapLiteClientProvider() );
     BundleContext bc = FrameworkUtil.getBundle( this.getClass() ).getBundleContext();
     ServiceReference<HttpService> reference = bc.getServiceReference( HttpService.class );
     HttpService httpService = bc.getService( reference );
     try {
       httpService.registerServlet( "/lite", new RapLiteServlet(), null, null );
       httpService.registerServlet( "/full", new RedirectServlet(), null, null );
     } catch( Throwable e ) {
       e.printStackTrace();
       // find a cleaner way to register
     }
   }
 
 }
