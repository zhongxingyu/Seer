 package gwt.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
 
 public interface LoginServiceAsync {
 	void checkLogin(String username, String password, AsyncCallback<Integer> callback);
 }
