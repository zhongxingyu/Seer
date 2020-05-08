 package com.epam.lab.buyit.controller.web.client.notification;
 
 import java.net.URI;
 
 import org.codehaus.jettison.json.JSONObject;
 
 import com.epam.lab.buyit.controller.service.user.UserService;
import com.epam.lab.buyit.controller.service.user.UserServiceImpl;
 import com.epam.lab.buyit.controller.utils.WebServicesPropertiesBundle;
 import com.epam.lab.buyit.controller.utils.builder.jsonbuilder.JSONBuilder;
 import com.epam.lab.buyit.controller.utils.builder.jsonbuilder.adapters.UserSerializationAdapter;
 import com.epam.lab.buyit.model.User;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 class NotificatorThread extends Thread {
 	private static final URI BASE_URI;
 	private static final Client CLIENT;
 	private UserService userService;
 	private int id;
 	
 	static {
 		BASE_URI = WebServicesPropertiesBundle.getForumBaseURI();
 		CLIENT = Client.create(new DefaultClientConfig());
 	}
 	
 	public NotificatorThread(int userId) {
 		this.id = userId;
		userService = new UserServiceImpl();
 	}
 	
 	@Override
 	public void run() {
 		WebResource service = CLIENT.resource(BASE_URI);
 		
 		User user = userService.getItemById(id);
 		JSONObject json = JSONBuilder.buildJSONObject(user, new UserSerializationAdapter());
 		service.path("/update").queryParam("user", json.toString()).post();
 	}
 
 }
