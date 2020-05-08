 package net.link.safeonline.wicket;
 
 import net.link.safeonline.wicket.component.linkid.LinkIDPageAuthenticationListener;
 import net.link.util.common.ApplicationMode;
 import net.link.util.j2ee.FieldNamingStrategy;
 import net.link.util.wicket.util.WicketUtils;
 import org.apache.wicket.Application;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.util.time.Duration;
 
 
 public abstract class LinkIDApplication extends WebApplication {
 
     public static LinkIDApplication get() {
 
         return (LinkIDApplication) WebApplication.get();
     }
 
     @Override
     protected void init() {
 
         // Java EE annotations injector.
         WicketUtils.addInjector( this, new FieldNamingStrategy() );
 
         // Poll resources, even in PRODUCTION.
         getResourceSettings().setResourcePollFrequency( Duration.ONE_MINUTE );
 
         // LinkID Authentication.
         LinkIDPageAuthenticationListener linkIDAuthenticationListener = new LinkIDPageAuthenticationListener();
         addPreComponentOnBeforeRenderListener( linkIDAuthenticationListener );
         addComponentInstantiationListener( linkIDAuthenticationListener );
     }
 
     @Override
     public String getConfigurationType() {
 
         switch (ApplicationMode.get()) {
             case DEBUG:
             case DEMO:
                 return Application.DEVELOPMENT;
             case DEPLOYMENT:
                 return Application.DEPLOYMENT;
         }
 
         return Application.DEPLOYMENT;
     }
 }
