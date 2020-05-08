 package ibeans.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 import java.util.List;
 import java.util.Map;
 import java.util.HashMap;
 
 import ibeans.client.model.Plugin;
 
 /**
  * TODO
  */
 public interface IBeansCentralServiceAsync
 {
 
     void getAvailableIBeans(AsyncCallback<List<Plugin>> async);
 
    void getAvailableModules(AsyncCallback<List<Plugin>> async);

     void downloadIBean(String user, String pass, String id, String version, AsyncCallback<String> async);
 
     void verifyUser(String user, String password, AsyncCallback<Boolean> async);
 }
