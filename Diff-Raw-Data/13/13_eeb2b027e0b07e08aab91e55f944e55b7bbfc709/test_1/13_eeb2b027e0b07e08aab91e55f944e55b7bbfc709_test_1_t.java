 /**
  * @Skilaverkefni2 Nov 6, 2010 Arnr Barkarson
  *
  * Copyright (c) Arnr Barkarson
  */
 package is.ru.honn.rusquare.test;
 
 import is.ru.honn.rusquare.data.content.CheckinDTO;
 import is.ru.honn.rusquare.data.content.CheckinGateway;
 import is.ru.honn.rusquare.data.content.DataGateway;
 import is.ru.honn.rusquare.data.content.VenueDTO;
 import is.ru.honn.rusquare.data.content.VenueGateway;
 import is.ruframework.data.RuDataAccessFactory;
 import is.ruframework.factory.RuException;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 /**
  * @author Arnr Barkarson
  *
  * @version 1.0, Nov 6, 2010
  */
 public class test_1 {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		Logger log = Logger.getLogger("Test"); RuDataAccessFactory factory = null; try {
 			factory = RuDataAccessFactory.getInstance("data.xml");
 		} catch (RuException e) {
 			log.severe("Unable to load data specification in data.xml" + e.getMessage());
 		}
 		
 		DataGateway checkinDataGateway = (CheckinGateway)factory.getDataAccess("checkinDataAccsess");
 		
		
 		checkinDataGateway.add(new CheckinDTO(1, "title!", new Date(), "name", 1));
 		
 		DataGateway venueDataGateway = (VenueGateway)factory.getDataAccess("venueDataAccsess");
 		
 		venueDataGateway.add(new VenueDTO(1, "nafn", "gata", "borg", "land", "101!", "BAR", 1));
 	}
 
 }
