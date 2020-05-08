 package nl.sense_os.commonsense.client.main;
 
 import nl.sense_os.commonsense.client.CommonSense;
 import nl.sense_os.commonsense.client.login.LoginEvents;
 import nl.sense_os.commonsense.client.main.components.HelpScreen;
 import nl.sense_os.commonsense.client.main.components.HomeScreen;
 import nl.sense_os.commonsense.client.main.components.NavPanel;
 import nl.sense_os.commonsense.client.utility.Log;
 import nl.sense_os.commonsense.client.visualization.VizEvents;
 import nl.sense_os.commonsense.shared.UserModel;
 
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 import com.extjs.gxt.ui.client.mvc.Controller;
 import com.extjs.gxt.ui.client.mvc.Dispatcher;
 import com.extjs.gxt.ui.client.mvc.View;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.Component;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.Text;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class MainView extends View {
 
     private static final String TAG = "MainView";
     private Viewport viewport;
     private LayoutContainer center;
     private NavPanel navPanel;
     private Component homeComponent;
     private Component helpComponent;
 
     public MainView(Controller controller) {
         super(controller);
     }
 
     private void createCenter() {
         this.center = new LayoutContainer(new FitLayout());
 
         BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
         this.viewport.add(this.center, centerData);
 
     }
 
     private void createFooter() {
         LayoutContainer footer = new LayoutContainer(new CenterLayout());
         Text footerText = new Text("&#169;2011 Sense Observation Systems - Last update: "
                 + CommonSense.LAST_DEPLOYED);
         footerText.setStyleAttribute("font-size", "10pt");
         footer.add(footerText);
 		footer.setId("footer-bar");
         
 		BorderLayoutData southData = new BorderLayoutData(LayoutRegion.SOUTH, 30);
         southData.setMargins(new Margins(0));
         southData.setSplit(false);
         this.viewport.add(footer, southData);
     }
 
     private void createNavigation() {
         this.navPanel = new NavPanel();
         this.navPanel.setId("navigation-bar");
 
         BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 23);
         northData.setMargins(new Margins(0));
         northData.setSplit(false);
         this.viewport.add(this.navPanel, northData);
     }
 
     @Override
     protected void handleEvent(AppEvent event) {
         EventType type = event.getType();
 
         if (type.equals(MainEvents.Error)) {
             Log.e(TAG, "Error");
             onError(event);
 
         } else if (type.equals(MainEvents.Init)) {
             Log.d(TAG, "Init");
             // do nothing: actual initialization is done in initialize()
 
         } else if (type.equals(MainEvents.UiReady)) {
             Log.d(TAG, "UiReady");
             onUiReady(event);
 
         } else if (type.equals(MainEvents.Navigate)) {
             // Log.d(TAG, "Navigate: \'" + event.<String> getData() + "\'");
             onNavigate(event);
 
         } else if (type.equals(LoginEvents.LoggedIn)) {
             // Log.d(TAG, "LoggedIn");
             onLoggedIn(event);
 
         } else if (type.equals(LoginEvents.LoggedOut)) {
             // Log.d(TAG, "LoggedOut");
             onLoggedOut(event);
 
         } else {
             Log.e(TAG, "Unexpected event type: " + type);
         }
     }
 
     @Override
     protected void initialize() {
         super.initialize();
 
         // ViewPort fills browser screen and automatically resizes content
         this.viewport = new Viewport();
         this.viewport.setId("viewport");
         this.viewport.setLayout(new BorderLayout());
         this.viewport.setStyleAttribute("background",
                 "url('img/bg/right_top_pre-light.png') no-repeat top right;");
 
         createNavigation();
         createCenter();
         createFooter();
     }
 
     private void onError(AppEvent event) {
         Log.e(TAG, "Error: " + event.<String> getData());
     }
 
     private void onLoggedIn(AppEvent event) {
         final UserModel user = event.<UserModel> getData();
         this.navPanel.setUser(user);
         this.navPanel.setLoggedIn(true);
     }
 
     private void onLoggedOut(AppEvent event) {
         this.navPanel.setLoggedIn(false);
     }
 
     private void onNavigate(AppEvent event) {
         String location = event.<String> getData("new");
 
         // select the new center content
         Component newContent = null;
         if (null != location) {
             if (location.equals(NavPanel.SIGN_IN)) {
                 newContent = new LayoutContainer();
                 Dispatcher.forwardEvent(LoginEvents.Show);
             } else if (location.equals(NavPanel.SIGN_OUT)) {
                 newContent = new LayoutContainer();
                 Dispatcher.forwardEvent(LoginEvents.RequestLogout);
             } else if (location.equals(NavPanel.HOME)) {
                 if (null == this.homeComponent) {
                     this.homeComponent = new HomeScreen();
                 }
                 newContent = this.homeComponent;
             } else if (location.equals(NavPanel.HELP)) {
                 if (null == this.helpComponent) {
                     this.helpComponent = new HelpScreen();
                 }
                 newContent = this.helpComponent;
             } else if (location.equals(NavPanel.VISUALIZATION)) {
                 Dispatcher.forwardEvent(VizEvents.Show, this.center);
             } else {
                 LayoutContainer lc = new LayoutContainer(new CenterLayout());
                 lc.add(new Text("Under construction..."));
                 newContent = lc;
             }
         }
 
         // remove old center content
         if (null != newContent) {
            newContent.setId("center-content");
             this.center.removeAll();
             this.center.add(newContent);
             this.center.layout();
         }
 
         // hide login window
         String oldLocation = event.<String> getData("old");
         if (NavPanel.SIGN_IN.equalsIgnoreCase(oldLocation)
                 && !NavPanel.SIGN_IN.equalsIgnoreCase(location)) {
             Dispatcher.forwardEvent(LoginEvents.Hide);
         }
 
         // update navigation panel
         this.navPanel.setHighlight(location);
     }
 
     private void onUiReady(AppEvent event) {
         RootPanel.get().add(this.viewport);
     }
 }
