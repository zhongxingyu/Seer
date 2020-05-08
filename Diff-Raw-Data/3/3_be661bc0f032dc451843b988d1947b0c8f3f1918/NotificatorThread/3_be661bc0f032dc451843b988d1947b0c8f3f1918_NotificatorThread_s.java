 package com.epam.lab.buyit.controller.web.client.notification;
 
 import java.net.URI;
 
 import com.epam.lab.buyit.controller.utils.WebServicesPropertiesBundle;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 class NotificatorThread extends Thread {
 	private static final URI BASE_URI;
 	private static final Client CLIENT;
 	private int id;
 	
 	static {
 		BASE_URI = WebServicesPropertiesBundle.getForumBaseURI();
 		CLIENT = Client.create(new DefaultClientConfig());
 	}
 	
 	public NotificatorThread(int userId) {
 		this.id = userId;
 	}
 	
 	@Override
 	public void run() {
 		WebResource service = CLIENT.resource(BASE_URI);
 		
 		service.path("/").path("/").post(id);
 	}
 
}
