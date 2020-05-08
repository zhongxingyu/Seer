 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import play.mvc.Controller;
 
 import models.Port;
 import models.Tariff;
 import models.Tug;
 
 public class Ports extends CRUD {
 	
 	public static void show(Long id) {
 		if(id != null) {
 			Port port = Port.findById(id);
 			render(port);
 		}
 		render();
 	}
 	
 	public static void save(Long id, String code, String name, String info,
 			String type, double harbour, double quay, double pilotfix, double pilotvar, 
 			double light, List<Tug> tug) {
 		Port port;
 		if(id != null) {
 	    	port = Port.findById(id);
 			port.code = code;
 			port.name = name;
 			port.info = info;
 			port.costtariff.type = type;
 			port.costtariff.harbour = harbour;
 			port.costtariff.quay = quay;
 			port.costtariff.pilotfix = pilotfix;
 			port.costtariff.pilotvar = pilotvar;
 			port.costtariff.light = light;
 			port.costtariff.tug = new ArrayList<Tug>();
 			
 			int i = 0;
 			while(i < tug.size()) {
				if(tug.get(i).minimum == null) tug.get(i).minimum = 0;
				if(tug.get(i).maximum == null) tug.get(i).maximum = 0;
 				port.costtariff.addTugTariff(tug.get(i).minimum, 
 						tug.get(i).maximum, tug.get(i).fixed, tug.get(i).var);
 				i++;
 			}
 
 		} else {
 			Tariff cost = new Tariff(type, harbour, quay, pilotfix, pilotvar, 
 					light);
 			cost.tug = new ArrayList<Tug>();
 			
 			int i = 0;
 			while(i < tug.size()) {
 				if(tug.get(i).minimum == null) tug.get(i).minimum = 0;
 				if(tug.get(i).maximum == null) tug.get(i).maximum = 0;
 				cost.addTugTariff(tug.get(i).minimum, 
 						tug.get(i).maximum, tug.get(i).fixed, tug.get(i).var);
 				i++;
 			}
 	    	port = new Port(name, cost);
 	    	port.code = code;
 	    	port.info = info;
 		}
 		
 		port.save();
         redirect(request.controller + ".list");
 	}
 	
 }
