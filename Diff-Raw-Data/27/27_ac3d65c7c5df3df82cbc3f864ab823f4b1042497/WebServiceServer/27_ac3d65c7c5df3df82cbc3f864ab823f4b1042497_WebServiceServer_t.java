 package com.scurab.web.drifmaps.server.restlet;
 
 import org.restlet.Component;
 import org.restlet.Server;
 import org.restlet.data.Protocol;
 import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
 
 public class WebServiceServer extends ServerResource
 {
 	private static Component sComponent = null;
 	private static int port = 8182; 
 	
 	static
 	{
 		sComponent = new Component(); 
 	}
 	
 	public static void start() throws Exception
 	{
 		try
 		{
			
 			System.out.println("Starting WebServiceServer");
 			sComponent = new Component();
 			Server server = new Server(Protocol.HTTP, port);
 			sComponent.getServers().add(server);
 			
 			sComponent.getDefaultHost().attach("/stars", StarRestlet.class);
 			sComponent.getDefaultHost().attach("/stars/{id}", StarRestlet.class);
 			sComponent.getDefaultHost().attach("/mapitems", MapItemRestlet.class);
 			sComponent.getDefaultHost().attach("/mapitems/{id}", MapItemRestlet.class);
 			sComponent.getDefaultHost().attach("/mapitems/{x1}/{y1}/{x2}/{y2}", MapItemRestlet.class);
 			sComponent.getDefaultHost().attach("/mapitemtypes", MapItemTypeRestlet.class);
 			sComponent.getDefaultHost().attach("/mapitemtypes/{name}", MapItemTypeRestlet.class);
 			sComponent.start();		 
 			System.out.println("Started WebServiceServer on port " + port);
 		}
 		catch(Exception e)
 		{
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public static void stop() throws Exception
 	{
 		sComponent.stop();
 	}
 	
 }
