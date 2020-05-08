 package com.axiomalaska.sos;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.opengis.ows.x11.ExceptionReportDocument;
 import net.opengis.sos.x20.GetObservationResponseDocument;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlbeans.XmlException;
 import org.apache.xmlbeans.XmlObject;
 import org.joda.time.DateTime;
 
 import com.axiomalaska.ioos.sos.exception.UnsupportedGeometryTypeException;
 import com.axiomalaska.phenomena.Phenomenon;
 import com.axiomalaska.sos.data.ObservationCollection;
 import com.axiomalaska.sos.data.SosSensor;
 import com.axiomalaska.sos.exception.InvalidObservationCollectionException;
 import com.axiomalaska.sos.exception.ObservationRetrievalException;
 import com.axiomalaska.sos.exception.SosCommunicationException;
 import com.axiomalaska.sos.tools.HttpSender;
 import com.axiomalaska.sos.tools.ResponseInterpretter;
 import com.axiomalaska.sos.tools.XmlHelper;
 import com.axiomalaska.sos.xmlbuilder.GetNewestObservationBuilder;
 import com.axiomalaska.sos.xmlbuilder.GetObservationBuilder;
 import com.axiomalaska.sos.xmlbuilder.GetOldestObservationBuilder;
 import com.axiomalaska.sos.xmlbuilder.InsertObservationBuilder;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * This class is used to push observations from a station and it's 
  * phenomena/sensor into the SOS
  * 
  * There are two ways to add observations to the SOS. 
  * 1.) Push an ObservationCollection
  * 2.) Pull observations from a ObservationRetriever
  * 
  * 1.) Call method insertObservations(ObservationCollection observationCollection) with a
  * prebuilt ObservationCollection. This will only add the observations that have not 
  * already been added to the SOS. 
  * 
  * 2.) Call update(SosSensor sensor, Phenomenon phenomenon, ObservationRetriever observationRetriever)
  * 
  * @author Lance Finfrock
  */
 public class ObservationSubmitter {
 	// -------------------------------------------------------------------------
 	// Private Data
 	// -------------------------------------------------------------------------
 	private static final Logger LOGGER = Logger.getLogger(ObservationSubmitter.class);
 	private String sosUrl;
     private String authorizationToken;
     private final int MAX_OBS_COLLECTION_SIZE = 200;
 	
 	// -------------------------------------------------------------------------
 	// Public Members
 	// -------------------------------------------------------------------------
 
 	public ObservationSubmitter(String sosUrl, String authorizationToken) {
 	    this.sosUrl = sosUrl;
 	    this.authorizationToken = authorizationToken;	    
 	}	
 	
 	/**
 	 * Pull: Update the observations of a station with a specific phenomenon in the SOS server.
 	 * 
 	 * @param sensor - the sensor from which to pull observations 
 	 * @param phenomenon - the phenomenon for which to pull observations 
 	 * @param observationRetriever - retrieves observations from the sensor
 	 * @throws InvalidObservationCollectionException 
 	 * @throws ObservationRetrievalException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	public void update(SosSensor sensor, Phenomenon phenomenon, ObservationRetriever observationRetriever)
 	           throws InvalidObservationCollectionException, ObservationRetrievalException, SosCommunicationException,
 	           UnsupportedGeometryTypeException{
 		DateTime startDateForAllGeometries = getNewestObservationDateForAllGeometries(sensor, phenomenon);
 		
 		List<ObservationCollection> observationCollections = observationRetriever
 				.getObservationCollection(sensor, phenomenon, startDateForAllGeometries);
 
 		for(ObservationCollection observationCollection : observationCollections){
 			DateTime startDate = getNewestObservationDate(sensor, phenomenon,
 			        observationCollection.getGeometry());
 			insertObservations(observationCollection, startDate);
 		}
 	}
 	
 	/**
 	 * Insert observations from the observationCollection object to the SOS. 
 	 * 
 	 * @param observationCollection - contains the observations to be placed in the SOS
 	 * @throws InvalidObservationCollectionException 
 	 * @throws ObservationRetrievalException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	public void insertObservations(ObservationCollection observationCollection) throws
 	        InvalidObservationCollectionException, ObservationRetrievalException, SosCommunicationException,
 	        UnsupportedGeometryTypeException{
 		DateTime newestObservationInSosDate = getNewestObservationDate(
 				observationCollection.getSensor(),
 				observationCollection.getPhenomenon(), observationCollection.getGeometry());
 
 		insertObservations(observationCollection, newestObservationInSosDate);
 	}
 	
 	/**
 	 * Insert observations from the observationCollection object in the SOS. 
 	 * 
 	 * @param observationCollection - contains the observations to be placed in the SOS 
 	 * @throws InvalidObservationCollectionException 
 	 * @throws ObservationRetrievalException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	private void insertObservations(ObservationCollection observationCollection, DateTime startDate)
 	        throws InvalidObservationCollectionException, ObservationRetrievalException, SosCommunicationException,
 	        UnsupportedGeometryTypeException  {		
 		DateTime oldestDate = getOldestObservationDate(observationCollection.getSensor(), 
 				observationCollection.getPhenomenon(), observationCollection.getGeometry());
 		
 		insertObservations(observationCollection, startDate, oldestDate);
 	}
 	
 	/**
 	 * Insert observations from the observationCollection object in the SOS. 
 	 * 
 	 * @param observationCollection - contains the observations to be placed in the
 	 * SOS. This object should have been validated before this method is called
 	 * @param newestObservationInSosDate - the newest observation's date
 	 * @throws InvalidObservationCollectionException 
 	 * @throws IOException 
 	 * @throws XmlException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	private void insertObservations(ObservationCollection observationCollection, 
 			DateTime newestObservationInSosDate, DateTime oldestObservationInSosDate)
 			        throws InvalidObservationCollectionException, SosCommunicationException,
 			        UnsupportedGeometryTypeException {
 	    if (!observationCollection.isValid()) {
 	        throw new InvalidObservationCollectionException(observationCollection);
 	    }
 		observationCollection.filterObservations(oldestObservationInSosDate, newestObservationInSosDate);		
 		if (observationCollection.getObservationValues().size() > 0) {
             XmlObject xbResponse;		    
 		    for (ObservationCollection splitObsCollection : splitObservationCollection(observationCollection)){
                 LOGGER.info("Inserting " + splitObsCollection.toString());		        
 	            try {
 	                xbResponse = ResponseInterpretter.getXmlObject(
 	                    HttpSender.sendPostMessage(sosUrl, authorizationToken,
 	                            new InsertObservationBuilder(splitObsCollection).build()));
 	            } catch (XmlException e) {
 	                throw new SosCommunicationException(e);
 	            } catch (IOException e) {
 	                throw new SosCommunicationException(e);
 	            }
 	            if (xbResponse == null || ResponseInterpretter.isError(xbResponse)) {
 	                LOGGER.error("Error while inserting " + splitObsCollection.toString()
 	                        + ":\n" + XmlHelper.xmlText(xbResponse));                   
 	            } else {
 	                LOGGER.info("Inserted " + splitObsCollection.toString());
 	            }               		        
 		    }
 		}
 	}
 	
 	private List<ObservationCollection> splitObservationCollection(ObservationCollection obsCollection) {
 	    List<ObservationCollection> obsCollections = new ArrayList<ObservationCollection>();
 	    
 	    //if obs collection is under size limit, return it in a list
 	    if( obsCollection.getObservationValues().size() <= MAX_OBS_COLLECTION_SIZE ){
 	        obsCollections.add(obsCollection);
 	        return obsCollections;
 	    }
 	    
         List<DateTime> times = new ArrayList<DateTime>(
                 obsCollection.getObservationValues().keySet());
         Collections.sort(times);
         
         for (int i = 0; i < times.size(); i += MAX_OBS_COLLECTION_SIZE) {
             List<DateTime> thisTimeRange = times.subList(i,
                     i + Math.min(MAX_OBS_COLLECTION_SIZE, times.size() - i));
             
             ObservationCollection thisChunk = new ObservationCollection();
             thisChunk.setGeometry(obsCollection.getGeometry());
             thisChunk.setPhenomenon(obsCollection.getPhenomenon());
             thisChunk.setSensor(obsCollection.getSensor());
             
             thisChunk.setObservationValues(obsCollection.getObservationValues().subMap(
                     thisTimeRange.get(0), thisTimeRange.get(thisTimeRange.size()-1)));
         }
 	    return obsCollections;
 	}
 	
 	private DateTime getNewestObservationDateForAllGeometries(SosSensor sensor,
 	        Phenomenon phenomenon) throws ObservationRetrievalException, SosCommunicationException,
 	        UnsupportedGeometryTypeException  {
        return getNewestObservationDate(sensor, phenomenon, null);
 	}
 	
 	/**
 	 * Get the newest observation date from the SOS server for the station and phenomenon. 
 	 * 
 	 * @param station - the station to look up the date from
 	 * @param phenomenon - the phenomenon to look up the date from
 	 * 
 	 * @return returns a Calendar object of the newest observation in the SOS 
 	 * from the station phenomenon passed in. If there are no observations in 
 	 * the SOS it returns a date from the first century.
 	 * @throws ObservationRetrievalException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	private DateTime getNewestObservationDate(SosSensor sensor, Phenomenon phenomenon, Geometry geometry)
 	        throws ObservationRetrievalException, SosCommunicationException, UnsupportedGeometryTypeException {
         DateTime dateTime = getObservationDateExtrema(sensor, phenomenon, geometry, ObservationExtremaType.NEWEST);
         return dateTime != null ? dateTime : new DateTime(1970,1,1,0,0);        
 	}
 
 	/**
 	 * Get the oldest observation date from the SOS server for the station and phenomenon. 
 	 * 
 	 * @param station - the station to look up the date from
 	 * @param phenomenon - the phenomenon to look up the date from
 	 * 
 	 * @return returns a Calendar object of the oldest observation in the SOS 
 	 * from the station phenomenon passed in. If there are no observations in 
 	 * the SOS it returns a date from the first century.
 	 * @throws ObservationRetrievalException 
 	 * @throws SosCommunicationException 
 	 * @throws UnsupportedGeometryTypeException 
 	 */
 	private DateTime getOldestObservationDate(SosSensor sensor, Phenomenon phenomenon, Geometry geometry)
 	        throws ObservationRetrievalException, SosCommunicationException, UnsupportedGeometryTypeException{
 	    DateTime dateTime = getObservationDateExtrema(sensor, phenomenon, geometry, ObservationExtremaType.OLDEST);
 	    return dateTime != null ? dateTime : new DateTime(1,1,1,0,0); 
 	}
 	
     /**
      * Get the oldest observation date from the SOS server for the station and phenomenon. 
      * 
      * @param station - the station to look up the date from
      * @param phenomenon - the phenomenon to look up the date from
      * 
      * @return returns a Calendar object of the oldest observation in the SOS 
      * from the station phenomenon passed in. If there are no observations in 
      * the SOS it returns a date from the first century.
      * @throws ObservationRetrievalException 
      * @throws SosCommunicationException 
      * @throws UnsupportedGeometryTypeException 
      */
     private DateTime getObservationDateExtrema(SosSensor sensor, Phenomenon phenomenon, Geometry geometry,
             ObservationExtremaType type) throws ObservationRetrievalException, SosCommunicationException,
             UnsupportedGeometryTypeException {
         GetObservationBuilder builder = null;
         switch (type){
             case NEWEST:
                 builder = new GetNewestObservationBuilder(sensor, phenomenon, geometry);
                 break;
             case OLDEST:
                 builder = new GetOldestObservationBuilder(sensor, phenomenon, geometry);
                 break;
         }
         
         XmlObject xbResponse = null;
         try {
             xbResponse = ResponseInterpretter.getXmlObject(HttpSender.sendPostMessage(sosUrl, authorizationToken,
                     builder.build()));
         } catch (XmlException e) {
             throw new SosCommunicationException(e);
         } catch (IOException e) {
             throw new SosCommunicationException(e);
         }   
         
         DateTime dateTime = null;        
         if (ResponseInterpretter.isError(xbResponse) || !(xbResponse instanceof GetObservationResponseDocument)) {
             //ignore foi errors, since the foi won't be valid until the first result is inserted
             if( !ResponseInterpretter.onlyExceptionContains((ExceptionReportDocument) xbResponse,
                     "of the parameter 'featureOfInterest' is invalid")) {
                 throw new ObservationRetrievalException(sensor, phenomenon, geometry, type);
             }
         } else {
             dateTime = ResponseInterpretter.parseDateFromGetObservationResponse(
                     (GetObservationResponseDocument) xbResponse);            
         }
 
         if (dateTime == null) {
             LOGGER.debug("No observations found in SOS for Sensor: "
                     + sensor.getId() + " phenomonon: "
                     + phenomenon.getId());                  
         }
         return dateTime;        
     }	
 }
