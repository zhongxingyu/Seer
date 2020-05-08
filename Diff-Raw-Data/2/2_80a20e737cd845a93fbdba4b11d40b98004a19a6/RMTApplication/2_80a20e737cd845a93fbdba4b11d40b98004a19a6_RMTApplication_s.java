 package de.flower.rmt.ui.app;
 
 import de.flower.common.ui.serialize.ISerializerListener;
 import de.flower.common.ui.serialize.SerializerWrapper;
 import de.flower.rmt.ui.page.about.AboutPage;
 import de.flower.rmt.ui.page.about.ChangeLogPage;
 import de.flower.rmt.ui.page.account.AccountPage;
 import de.flower.rmt.ui.page.blog.ArticlePage;
 import de.flower.rmt.ui.page.blog.BlogPage;
 import de.flower.rmt.ui.page.calendar.CalendarPage;
 import de.flower.rmt.ui.page.error.AccessDenied403Page;
 import de.flower.rmt.ui.page.error.InternalError500Page;
 import de.flower.rmt.ui.page.error.PageExpiredPage;
 import de.flower.rmt.ui.page.error.PageNotFound404Page;
 import de.flower.rmt.ui.page.event.manager.EventPage;
 import de.flower.rmt.ui.page.events.manager.EventsPage;
 import de.flower.rmt.ui.page.login.LoginPage;
 import de.flower.rmt.ui.page.login.PasswordForgottenPage;
 import de.flower.rmt.ui.page.opponents.manager.OpponentsPage;
 import de.flower.rmt.ui.page.teams.manager.TeamsPage;
 import de.flower.rmt.ui.page.users.UsersPage;
 import de.flower.rmt.ui.page.venues.manager.VenuesPage;
 import de.flower.rmt.ui.page.venues.player.VenuePage;
 import org.apache.wicket.RuntimeConfigurationType;
 import org.apache.wicket.Session;
 import org.apache.wicket.protocol.http.WebApplication;
 import org.apache.wicket.request.Request;
 import org.apache.wicket.request.Response;
 import org.apache.wicket.serialize.ISerializer;
 import org.apache.wicket.settings.IExceptionSettings;
 import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 @Component(value = "wicketApplication")
 public class RMTApplication extends WebApplication {
 
     private final static Logger log = LoggerFactory.getLogger(RMTApplication.class);
 
     @Autowired
     private ISerializerListener pageSerializationValidatorListener;
 
     public static RMTApplication get() {
         return (RMTApplication) WebApplication.get();
     }
 
     @Override
     protected void init() {
         log.info("***********************************************************************************");
         log.info("*** Version " + Version.VERSION);
         log.info("***********************************************************************************");
         super.init();
         // add support for @SpringBean
         getComponentInstantiationListeners().add(getSpringComponentInjector());
         // google maps have problems when wicket tags are rendered in development mode, so strip those tags
         getMarkupSettings().setStripWicketTags(true);
         // don't use <em>link</em> when disabling links.
         getMarkupSettings().setDefaultBeforeDisabledLink(null);
         getMarkupSettings().setDefaultAfterDisabledLink(null);
 
         // wicket tries to survive session timeout by recreating the original page. but that leads to strange behavior
         // when clicking links as nothing happens (only current page is refreshed).
         getPageSettings().setRecreateMountedPagesAfterExpiry(false);
 
         if (usesDevelopmentConfig()) {
             // enable debug bar
             getDebugSettings().setDevelopmentUtilitiesEnabled(true);
             getDebugSettings().setOutputComponentPath(true);
             getDebugSettings().setOutputMarkupContainerClassName(true);
             getRequestLoggerSettings().setRequestLoggerEnabled(true);
 
             // getComponentInstantiationListeners().add(new RenderPerformanceListener());
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
         wrapper.addListener(pageSerializationValidatorListener);
         getFrameworkSettings().setSerializer(wrapper);
     }
 
     protected SpringComponentInjector getSpringComponentInjector() {
         return new SpringComponentInjector(this);
     }
 
     private void initBookmarkablePages() {
         mountPage("manager", EventsPage.class);
         mountPage("manager/teams", TeamsPage.class);
         mountPage("manager/events", EventsPage.class);
         mountPage("manager/event/${" + EventPage.PARAM_EVENTID + "}", EventPage.class);
         mountPage("manager/opponents", OpponentsPage.class);
         mountPage("manager/venues", VenuesPage.class);
         mountPage("events", de.flower.rmt.ui.page.events.player.EventsPage.class);
        // TODO (flowerrrr - 23.06.12) remove next line in july
        mountPage("player/event/${" + EventPage.PARAM_EVENTID + "}", de.flower.rmt.ui.page.event.player.EventPage.class);
         mountPage("event/${" + EventPage.PARAM_EVENTID + "}", de.flower.rmt.ui.page.event.player.EventPage.class);
         mountPage("calendar", CalendarPage.class);
         mountPage("blog", BlogPage.class);
         mountPage("blog/${" + ArticlePage.PARAM_ARTICLEID + "}", ArticlePage.class);
         mountPage("users", UsersPage.class);
         mountPage("venues", de.flower.rmt.ui.page.venues.player.VenuesPage.class);
         mountPage("venue/${" + VenuePage.PARAM_VENUEID + "}", VenuePage.class);
         mountPage("account", AccountPage.class);
         mountPage("login/passwordforgotten", PasswordForgottenPage.class);
         mountPage("login", LoginPage.class);
         mountPage("about", AboutPage.class);
         mountPage("changelog", ChangeLogPage.class);
     }
 
     private void initErrorPages() {
         // same url as in web.xml
         mountPage("error/404", PageNotFound404Page.class);
 
         mountPage("error/500", InternalError500Page.class);
         getRequestCycleListeners().add(new ExceptionRequestCycleListener());
         getApplicationSettings().setInternalErrorPage(InternalError500Page.class);
         getExceptionSettings().setUnexpectedExceptionDisplay(IExceptionSettings.SHOW_INTERNAL_ERROR_PAGE);
 
         // access denied is not handled by wicket. spring security will redirect request to this url
         mountPage("error/403", AccessDenied403Page.class);
 
         mountPage("error/" + PageExpiredPage.SC, PageExpiredPage.class);
         getApplicationSettings().setPageExpiredErrorPage(PageExpiredPage.class);
     }
 
     @Override
     public Class getHomePage() {
         return HomePageResolver.getHomePage();
     }
 
     @Override
     public Session newSession(final Request request, final Response response) {
         return new RMTSession(request);
     }
 
     /**
      * Output to log instead of System.err.
      */
     @Override
     protected void outputDevelopmentModeWarning() {
         log.warn("\n********************************************************************\n"
                 + "*** WARNING: Wicket is running in DEVELOPMENT mode.              ***\n"
                 + "***                               ^^^^^^^^^^^                    ***\n"
                 + "*** Do NOT deploy to your live server(s) without changing this.  ***\n"
                 + "*** See Application#getConfigurationType() for more information. ***\n"
                 + "********************************************************************\n");
     }
 
     @Value("${wicket.configurationtype}")
     public void setRuntimeConfigurationType(final RuntimeConfigurationType runtimeConfigurationType) {
         setConfigurationType(runtimeConfigurationType);
     }
 }
