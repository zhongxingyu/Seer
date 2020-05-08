 package vahdin;
 
 import java.lang.reflect.Method;
 
 import vahdin.component.GoogleMap;
 import vahdin.data.User;
 import vahdin.layout.SideBar;
 
 import com.vaadin.annotations.Theme;
 import com.vaadin.event.MethodEventSource;
 import com.vaadin.navigator.Navigator;
 import com.vaadin.navigator.Navigator.ComponentContainerViewDisplay;
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 /**
  * Main UI class
  */
 @Theme("vahdintheme")
 @SuppressWarnings("serial")
 public class VahdinUI extends UI implements MethodEventSource {
 
     private static final String GOOGLE_MAPS_API_KEY = "AIzaSyD723LQ68aCdI37_yhUNDQVHj3zzAfPDVo";
 
     private User currentUser = User.guest();
 
     /** Initializes the UI. */
     @Override
     protected void init(VaadinRequest request) {
         getPage().setTitle("Vahdin");
 
         GoogleMap map = new GoogleMap(GOOGLE_MAPS_API_KEY);
         map.setSizeFull();
         VerticalLayout sidebar = new VerticalLayout();
 
         CustomLayout layout = new CustomLayout("base-template");
         buildMenuBar(layout);
         layout.addComponent(map, "map-container");
         layout.addComponent(sidebar, "sidebar-container");
         layout.setSizeFull();
 
         setContent(layout);
         ComponentContainerViewDisplay viewDisplay = new ComponentContainerViewDisplay(
                 sidebar);
         Navigator navigator = new Navigator(UI.getCurrent(), viewDisplay);
         navigator.addView("", SideBar.class);
     }
 
     /**
      * Builds the menu bar by adding the necessary components.
      * 
      * @param layout
      *            The layout to add the components to.
      */
     private void buildMenuBar(CustomLayout layout) {
 
         final VahdinUI ui = (VahdinUI) UI.getCurrent();
 
         Button logoLink = new Button();
         logoLink.setSizeFull();
         logoLink.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 ui.getNavigator().navigateTo("/");
             }
         });
 
         Button statsLink = new Button();
         statsLink.setSizeFull();
         statsLink.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO Auto-generated method stub
             }
         });
 
         final Button loginLink = new Button();
         loginLink.addStyleName("login-link");
         loginLink.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 User currentUser = ui.getCurrentUser();
                 if (currentUser.isLoggedIn()) {
                     // logout actions
                     ui.setCurrentUser(User.guest());
                 } else {
                     // login actions
                     ui.setCurrentUser(User.load("testi")); // XXX: logs in a
                                                            // test user
                     // TODO: actual login
                 }
             }
         });
 
         Label score = new Label(""); // TODO: user score
 
         final Label username = new Label(getCurrentUser().getName());
 
         addLoginListener(new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User currentUser = ui.getCurrentUser();
                 if (currentUser.isLoggedIn()) {
                     loginLink.removeStyleName("login-link");
                     loginLink.addStyleName("logout-link");
                 } else {
                     loginLink.removeStyleName("logout-link");
                     loginLink.addStyleName("login-link");
                 }
                 username.setValue(currentUser.getName());
             }
         });
 
         layout.addComponent(logoLink, "logo-link");
         layout.addComponent(statsLink, "stats-link");
         layout.addComponent(loginLink, "login-logout-link");
         layout.addComponent(score, "user-score");
         layout.addComponent(username, "username");
 
     }
 
     /**
      * Gets the current user.
      * 
      * @return The currently logged in user, or a guest user if there is none.
      */
     public User getCurrentUser() {
         return currentUser;
     }
 
     /**
      * Sets the current user, marks them as logged in and fires the login event.
      * 
      * @param user
      *            The user to set as currently logged in.
      */
     public void setCurrentUser(User user) {
         currentUser.markLoggedOut();
         currentUser = user;
         if (!currentUser.isGuest()) {
             currentUser.markLoggedIn();
         }
         fireEvent(new LoginEvent(this));
     }
 
     /**
      * Adds a login listener.
      * 
      * @param listener
      *            The listener to add.
      */
     public void addLoginListener(LoginListener listener) {
         addListener(LoginEvent.class, listener, LoginListener.LOGIN_METHOD);
     }
 
     /** Login event listener. */
     public static abstract class LoginListener {
 
         public static final Method LOGIN_METHOD;
 
         static {
             try {
                 LOGIN_METHOD = LoginListener.class.getMethod("login",
                         LoginEvent.class);
             } catch (NoSuchMethodException e) {
                 throw new Error(e);
             }
         }
 
         /***/
         public abstract void login(LoginEvent event);
 
     }
 
     /** Login event. */
     public static class LoginEvent extends Component.Event {
 
         private LoginEvent(UI source) {
             super(source);
         }
     }
 }
