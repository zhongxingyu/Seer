 package de.flower.rmt.ui.app;
 
 import de.flower.common.ui.serialize.Filter;
 import de.flower.common.ui.serialize.LoggingSerializer;
 import de.flower.common.ui.serialize.SerializerWrapper;
 import de.flower.rmt.model.RSVPStatus;
 import de.flower.rmt.model.event.EventType;
 import de.flower.rmt.model.type.Notification;
 import de.flower.rmt.service.type.Password;
 import de.flower.rmt.ui.page.about.AboutPage;
 import de.flower.rmt.ui.page.account.AccountPage;
 import de.flower.rmt.ui.page.base.manager.ManagerHomePage;
 import de.flower.rmt.ui.page.base.player.PlayerHomePage;
 import de.flower.rmt.ui.page.error.AccessDenied403Page;
 import de.flower.rmt.ui.page.error.InternalError500Page;
 import de.flower.rmt.ui.page.error.PageNotFound404Page;
 import de.flower.rmt.ui.page.event.manager.EventPage;
 import de.flower.rmt.ui.page.events.manager.EventsPage;
 import de.flower.rmt.ui.page.login.LoginPage;
 import de.flower.rmt.ui.page.opponents.manager.OpponentsPage;
 import de.flower.rmt.ui.page.teams.manager.TeamsPage;
 import de.flower.rmt.ui.page.users.manager.PlayersPage;
 import de.flower.rmt.ui.page.venues.manager.VenuesPage;
 import de.flower.rmt.ui.page.venues.player.VenuePage;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.devutils.inspector.RenderPerformanceListener;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 import org.apache.wicket.serialize.ISerializer;
 import org.apache.wicket.settings.IExceptionSettings;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component(value = "wicketApplication")
 public class RMTApplication extends WebApplication {
 
     public final static String VERSION = "1.2.1";
 
     private final static Logger log = LoggerFactory.getLogger(RMTApplication.class);
 
     public static RMTApplication get() {
         return (RMTApplication) WebApplication.get();
     }
 
     @Override
     protected void init() {
         log.info("***********************************************************************************");
         log.info("*** Version " + VERSION);
         log.info("***********************************************************************************");
         super.init();
         // add support for @SpringBean
         getComponentInstantiationListeners().add(getSpringComponentInjector());
         // google maps have problems when wicket tags are rendered in development mode, so strip those tags
         getMarkupSettings().setStripWicketTags(true);
 
         if (usesDevelopmentConfig()) {
             getDebugSettings().setDevelopmentUtilitiesEnabled(true);
             getDebugSettings().setOutputComponentPath(true);
             getDebugSettings().setOutputMarkupContainerClassName(true);
 
             getComponentInstantiationListeners().add(new RenderPerformanceListener());
             initSerializer();
         }
 
         initBookmarkablePages();
 
         initErrorPages();
     }
 
     /**
      * Adds a listener that analyses and logs the serialized pages to find out
      * if unwanted objects (e.g. domain objects) are serialized.
      */
     private void initSerializer() {
         final ISerializer serializer = getFrameworkSettings().getSerializer();
         SerializerWrapper wrapper = new SerializerWrapper(serializer);
         Filter filter = new Filter("\"de\\.flower\\.rmt\\.model\\.[^-]*?\"");
         filter.addExclusion(RSVPStatus.class.getName());
         filter.addExclusion(Password.class.getName());
         filter.addExclusion(Notification.class.getName());
         filter.addExclusion(EventType.class.getName());
         wrapper.addListener(new LoggingSerializer(filter));
         getFrameworkSettings().setSerializer(wrapper);
     }
 
     protected SpringComponentInjector getSpringComponentInjector() {
         return new SpringComponentInjector(this);
     }
 
     private void initBookmarkablePages() {
         mountPage("manager", ManagerHomePage.class);
         mountPage("manager/teams", TeamsPage.class);
         mountPage("manager/players", PlayersPage.class);
         mountPage("manager/events", EventsPage.class);
         mountPage("manager/event/${" + EventPage.PARAM_EVENTID + "}", EventPage.class);
         mountPage("manager/opponents", OpponentsPage.class);
         mountPage("manager/venues", VenuesPage.class);
         mountPage("player", PlayerHomePage.class);
         mountPage("player/events", de.flower.rmt.ui.page.events.player.EventsPage.class);
         mountPage("player/event/${" + EventPage.PARAM_EVENTID + "}", de.flower.rmt.ui.page.event.player.EventPage.class);
         mountPage("player/venues", de.flower.rmt.ui.page.venues.player.VenuesPage.class);
         mountPage("player/venue/${" + VenuePage.PARAM_VENUEID + "}", VenuePage.class);
        mountPage("common/account", AccountPage.class);
         mountPage("login", LoginPage.class);
         mountPage("about", AboutPage.class);
     }
 
     private void initErrorPages() {
         mountPage("error404", PageNotFound404Page.class);
 
         mountPage("error500", InternalError500Page.class);
         getRequestCycleListeners().add(new ExceptionRequestCycleListener());
         getApplicationSettings().setInternalErrorPage(InternalError500Page.class);
         getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
 
         // access denied is not handled by wicket. spring security will redirect request to this url
         mountPage("error403", AccessDenied403Page.class);
 
     }
 
     @Override
     public Class getHomePage() {
         return HomePageResolver.getHomePage();
     }
 
     @Override
     public RMTSession newSession(Request request, Response response) {
         return new RMTSession(request);
     }
 
     /**
      * Output to log instead of System.err.
      */
     @Override
     protected void outputDevelopmentModeWarning()
     {
         log.warn("\n********************************************************************\n"
                 + "*** WARNING: Wicket is running in DEVELOPMENT mode.              ***\n"
                 + "***                               ^^^^^^^^^^^                    ***\n"
                 + "*** Do NOT deploy to your live server(s) without changing this.  ***\n"
                 + "*** See Application#getConfigurationType() for more information. ***\n"
                 + "********************************************************************\n");
     }
 
     // TODO (flowerrrr - 14.04.12) could be annotated field as well ??
     @Value("${wicket.configurationtype}")
     public void setRuntimeConfigurationType(final RuntimeConfigurationType runtimeConfigurationType) {
         setConfigurationType(runtimeConfigurationType);
     }
 }
