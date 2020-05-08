 package vahdin.component;
 
 import java.lang.reflect.Method;
 import java.util.UUID;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import com.vaadin.ui.Component;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.JavaScript;
 import com.vaadin.ui.JavaScriptFunction;
 
 public class OAuth2Button extends CustomComponent {
 
     private final String id;
 
     public OAuth2Button(final String provider) {
         final OAuth2Button button = this;
         id = UUID.randomUUID().toString();
         setId(id);
         addStyleName(provider);
 
        JavaScript.getCurrent().addFunction(
                "OAuth2Button.authenticate_" + provider,
                 new JavaScriptFunction() {
 
                     @Override
                     public void call(JSONArray arguments) throws JSONException {
                         fireEvent(new AuthEvent(button, arguments.getString(0)));
                     }
 
                 });
 
         addAttachListener(new AttachListener() {
             @Override
             public void attach(AttachEvent event) {
                 JavaScript.getCurrent().execute(
                         "window['" + id + "'] = new OAuth2Button('" + id
                                 + "', '" + provider + "');");
             }
         });
     }
 
     /**
      * Adds an authentication listener.
      * 
      * @param listener
      *            The listener to add.
      */
     public void addAuthListener(AuthListener listener) {
         addListener(AuthEvent.class, listener, AuthListener.AUTH_METHOD);
     }
 
     /** Authentication event listener. */
     public static abstract class AuthListener {
 
         public static final Method AUTH_METHOD;
 
         static {
             try {
                 AUTH_METHOD = AuthListener.class.getMethod("auth",
                         AuthEvent.class);
             } catch (NoSuchMethodException e) {
                 throw new Error(e);
             }
         }
 
         /***/
         public abstract void auth(AuthEvent event);
 
     }
 
     /** Authentication event. */
     public static class AuthEvent extends Component.Event {
 
         public final String userId;
 
         private AuthEvent(OAuth2Button source, String id) {
             super(source);
             userId = id;
         }
     }
 }
