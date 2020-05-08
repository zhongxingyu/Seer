 package org.iucn.sis.client.panels.zendesk;
 import org.iucn.sis.client.api.utils.UriBase;
 import org.iucn.sis.client.container.SimpleSISClient;
 import org.iucn.sis.shared.api.debug.Debug;
 
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.google.gwt.user.client.Cookies;
 import com.google.gwt.user.client.ui.TextBox;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
import com.solertium.util.extjs.client.WindowUtils;
 
 public class ZendeskPanel extends Window {
 	
 	@SuppressWarnings("unused")
 	private final TextBox subject;
 	
 	@SuppressWarnings("unused")
 	private final TextArea ticket;
 	
 	public ZendeskPanel() {
		super();
		setSize(800, 600);
		setHeading("Zendesk");
		
 		subject = new TextBox();
 		ticket = new TextArea();
		
 		build();
 	}
 	
 	private void build(){
 		getCredentials(new GenericCallback<String>() {
 			public void onFailure(Throwable caught) {
				WindowUtils.errorAlert("Credentials failed, can not login to ZenDesk");
 			}
 			public void onSuccess(String result) {
 				Debug.println("https://iucnsis.zendesk.com/access/remote/"+result);
 				setUrl("https://iucnsis.zendesk.com/access/remote/"+result);
 				show();
 			}
 		});
 	}
 	
 	/*private void sendTicket(){
 		
 		NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		
 		doc.post(UriBase.getInstance().getZendeskBase() +"/zendesk/tickets", buildTicketRequest(ticket.getValue()), new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				// TODO Auto-generated method stub
 				
 			}
 			public void onFailure(Throwable caught) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 	}*/
 	
 	/*private String buildTicketRequest(String description){
 		final String email = SimpleSISClient.currentUser.getUsername();
 		String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
 		
 		return "<ticket>"+
 		  "<description>"+description+"</description>"+
 		  "<priority-id>0</priority-id>"+  
 		  "<requester-name>"+name+"</requester-name>"+
 		  "<requester-email>"+email+"</requester-email>"+   
 		"</ticket>";
 	}*/
 	
 	private void getCredentials(final GenericCallback<String> callback){
 		final String email = SimpleSISClient.currentUser.getUsername();
 		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
 		String organization = SimpleSISClient.currentUser.getAffiliation();
 		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
 		
 		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		
		doc.postAsText(UriBase.getInstance().getZendeskBase() + "/zendesk/login", xml, new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				callback.onSuccess(doc.getText());
 				
 			}
 			public void onFailure(Throwable caught) {
 				caught.printStackTrace();
 				
 			}
 		});
 	}
 	
 
 	private void logout(){
 		final String email = SimpleSISClient.currentUser.getUsername();
 		final String name = SimpleSISClient.currentUser.getFirstName() + " " +  SimpleSISClient.currentUser.getLastName();
 		String organization = SimpleSISClient.currentUser.getAffiliation();
 		//String external_id = String.valueOf(SimpleSISClient.currentUser.getId());
 		
 		String xml = "<root><user name=\""+name+"\" email=\""+email+"\" organization=\""+organization+"\"/></root>";
 		final NativeDocument doc = SimpleSISClient.getHttpBasicNativeDocument();
 		
 		doc.postAsText(UriBase.getInstance().getZendeskBase() +"/zendesk/logout", xml, new GenericCallback<String>() {
 			public void onSuccess(String result) {
 				//callback.onSuccess(doc.getText());
 				
 			}
 			public void onFailure(Throwable caught) {
				Debug.println(caught);
 			}
 		});
 	}
 		
 	@Override
 	public void close() {
 		Cookies.removeCookie("_zendesk_session");
 		logout();
 	
 		super.hide();
 	}
 	
 }
