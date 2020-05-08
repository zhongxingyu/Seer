 package project.client;
 
 
 import project.client.editor.AceEditorWidget;
 import project.client.login.LoginInfo;
 import project.client.login.LoginService;
 import project.client.login.LoginServiceAsync;
 import project.client.login.LoginWidget;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class AceProject implements EntryPoint {
 	
 	
 	private LoginInfo loginInfo = null;
	private VerticalPanel loginPanel; 
	private VerticalPanel editor; 
 	/**
 	 * This is the entry point method.
 	 * @wbp.parser.entryPoint
 	 */
 	public void onModuleLoad() {
 		// Check login status using login service.
		System.out.println("Step 1");
 		LoginServiceAsync loginService = GWT.create(LoginService.class);
 		loginService.login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
 		      public void onFailure(Throwable error) {
 		      }
 
 		      public void onSuccess(LoginInfo result) {
		    	  System.out.println("Step 2");
 		        loginInfo = result;
 		        if(loginInfo.isLoggedIn()) {
		        	System.out.println("Step 3");
 		        	loadEditorAndService();
 		        } else {
 		          loadLogin();
 		        }
 		      }
 		    });		
 	}
 	
 	private void loadLogin() {
 	    if(loginPanel==null)
 	    	loginPanel=new LoginWidget(loginInfo);
 	    RootPanel.get("ace").add(loginPanel);//see the html file for id
   }
 
 	private void loadEditorAndService() {
 		
 		if(editor==null)
 			editor=new AceEditorWidget(loginInfo);
		//RootPanel.get("ace").add(editor);
		
 		
 	}
 
 	
 	
 }
