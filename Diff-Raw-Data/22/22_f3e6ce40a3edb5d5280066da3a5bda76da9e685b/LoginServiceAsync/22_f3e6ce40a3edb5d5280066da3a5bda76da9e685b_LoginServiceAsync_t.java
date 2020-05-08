 package gwt.client;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 public interface LoginServiceAsync {
 	void checkLogin(String username, String password, AsyncCallback<Integer> callback);
 }
