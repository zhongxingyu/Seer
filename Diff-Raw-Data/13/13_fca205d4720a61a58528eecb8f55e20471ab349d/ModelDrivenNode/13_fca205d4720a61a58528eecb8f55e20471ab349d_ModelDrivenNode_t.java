 package com.nodemy.modeldrivennode.parser;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 
 /**
  * Build our node.js server here.
  * 
  * @author broward
  * 
  */
 public class ModelDrivenNode {
 	public static String APPLICATION = "ModelDrivenNodeA";
 	public static String SCHEMA = "AppA";
 	public static String VERSION = "v1";
 	public static String ROOT = "/Dev/nodejs/mdn/";
 	public static String VERSION_PATH = ROOT + VERSION + "/";
 	public static String URL_ROOT = "Mdn/" + SCHEMA + "/" + VERSION;
 	
 	// template input paths
 	public static String NODEJS = "nodejs/";
 	public static String VIEWS = "views/";
 	public static String SOCKETS = "sockets/";
 	public static String QUEUES = "queues/";
 
 	// template output paths
 	public static String MODEL_OUTPUT = VERSION_PATH + "models/";
 	public static String ROUTE_OUTPUT = VERSION_PATH + "routes/";
 	public static String JADE_OUTPUT = VERSION_PATH + "views/";
 	public static String SOCKET_OUTPUT = VERSION_PATH + "sockets/";
 	public static String QUEUE_OUTPUT = VERSION_PATH + "queues/";
 
 	public static VelocityEngine ve = new VelocityEngine();
 	public static VelocityContext context = new VelocityContext();
 
 	static {
 		ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
 		ve.setProperty("classpath.resource.loader.class",
 				ClasspathResourceLoader.class.getName());
 		ve.init();
 	}
 
 	public ModelDrivenNode() {
 	}
 
 	public void buildServer(String schema) {
 		// clear any previous entries
 		ObjectGrapher.INSTANCE.clear();
 		ResourceList.INSTANCE.getResources().clear();
 
 		// Read the xsd schema
 		ResourceBuilder.INSTANCE.addSchema(schema);
 		ResourceBuilder.INSTANCE.go();
 
 		System.out.println("resources:"
 				+ ResourceList.INSTANCE.getResources().size());
 
 		try {
 			// Run the templates and build the node.js server
 			new NodeJsTemplates();
 			new JadeTemplates();
 			new RoutesJsTemplates();
 			new SocketJsTemplates();
 			new ModelsJsTemplates();
 			new QueuesJsTemplates();
 			System.out.println("NodeServer for " + SCHEMA + " built!");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void clean() {
 		// Remove pre-existing deployment
 		try {
 			FileUtils.deleteDirectory(new File(VERSION_PATH));
 
 			// Recreate directories
 			FileUtils.forceMkdir(new File(MODEL_OUTPUT));
 			FileUtils.forceMkdir(new File(ROUTE_OUTPUT));
 			FileUtils.forceMkdir(new File(JADE_OUTPUT));
 			FileUtils.forceMkdir(new File(SOCKET_OUTPUT));
 			FileUtils.forceMkdir(new File(QUEUE_OUTPUT));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		// nb.buildServer("schema/OAGIS/Local/BODs/CancelInvoice/CancelInvoice.xsd");
 		// nb.buildServer("schema/niem/domains/immigration/2.1/immigration.xsd");
 		// nb.buildServer("schema/cia/CIALib.xsd");
 		// nb.buildServer("schema/OTA/OTA_TravelItineraryRS.xsd");
 		// nb.buildServer("schema/cia/CustomerAccountPayment.xsd");
 		// nb.buildServer("schema/OTA/OTA_HotelRFP_MeetingRS.xsd");
 		// nb.buildServer("schema/OTA/OTA_HotelEvent.xsd"); // works 1152!
 		// nb.buildServer("schema/OTA/OTA_CancelRQ.xsd"); // works
 		// nb.buildServer("schema/OTA/OTA_CruiseInfoRS.xsd"); // works
 		// nb.buildServer("schema/OTA/OTA_profile.xsd"); //works
 		// nb.buildServer("schema/RIX/RIXML-2_3_1.xsd"); // works
 		// nb.buildServer("schema/rets/RETS-Syndication-Archive/RETSCommons/2011-06/Person.xsd");
 		// // works
 		// nb.buildServer("schema/axis/PNRViewRS.xsd"); // works
 		// nb.buildServer("schema/axis/PNRChangeRQ.xsd"); // works
 		// nb.buildServer("schema/Star/BODs/Developer/ShowPartsInventory.xsd"); //
 		// works! 995!
 		// nb.buildServer("schema/Star/BODs/StandAlone/GetVehicleOrder.xsd");
 		// nb.buildServer("schema/OpenHR/BODs/RespondTimeCard.xsd");
 
 		// Build first schema
 		ModelDrivenNode nb = new ModelDrivenNode();
 		nb.clean();
		/*ModelDrivenNode.APPLICATION = "ModelDrivenNode";
 		ModelDrivenNode.SCHEMA = "AppA";
		nb.buildServer("schema/OTA/OTA_HotelEvent.xsd"); */
 
 		// Build second schema
		ModelDrivenNode.APPLICATION = "ModelDrivenNodeB";
 		ModelDrivenNode.SCHEMA = "AppB";
		ModelDrivenNode.URL_ROOT = "Mdn/" + SCHEMA + "/" + VERSION;
 		//nb.buildServer("schema/OTA/OTA_HotelEvent.xsd");
		nb.buildServer("schema/Star/BODs/Developer/ShowPartsInventory.xsd");
 	}
 }
