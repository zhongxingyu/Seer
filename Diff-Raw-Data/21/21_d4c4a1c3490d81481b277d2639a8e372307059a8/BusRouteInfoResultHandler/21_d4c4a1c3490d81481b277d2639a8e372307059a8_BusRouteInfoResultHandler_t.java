 package com.api.handler.cta.bus;
 
 import java.util.LinkedList;
 
 import org.apache.commons.lang3.StringUtils;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.api.constants.cta.bus.BusEtaXmlConstants;
 import com.api.model.cta.bus.BusETA;
 
 public class BusRouteInfoResultHandler extends DefaultHandler {
 	
 	private final StringBuffer buffer = new StringBuffer();
 	private LinkedList<BusETA> busRoutes;
	private String errorDescription;
 	private BusETA busETA;
     @Override
 	public void startDocument() throws SAXException {
         System.out.println("start document   : ");
     }
 
     @Override
 	public void endDocument() throws SAXException {
         System.out.println("end document     : ");
     }
 
     @Override
 	public void startElement(String uri, String localName,
         String qName, Attributes attributes)
     throws SAXException {
 
         System.out.println("start element    : " + qName);
         
         buffer.setLength(0);
         
         if(StringUtils.equals(qName, BusEtaXmlConstants.BUSTIME_RESPONSE_CONTAINER_TAG)){
         	this.busRoutes = new LinkedList<BusETA>();
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.PREDICTED_ARRIVAL_CONTAINER_TAG)){
         	this.busETA = new BusETA();
         }
         
     }
 
     @Override
 	public void endElement(String uri, String localName, String qName)
     throws SAXException {
         System.out.println("end element      : " + qName);
         
         if(StringUtils.equals(qName, BusEtaXmlConstants.PREDICTED_ARRIVAL_CONTAINER_TAG)){
         	this.busRoutes.add(busETA);
         	System.out.println(busETA.toString());
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.PREDICTION_GENERATED_TIMESTAMP_TAG)){
         	this.busETA.setTimeStamp(buffer.toString());
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.TYPE_TAG)){
         	this.busETA.setType(buffer.toString());
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.NUMERIC_STOP_ID_TAG)){
         	this.busETA.setNumericStopId(Integer.parseInt(buffer.toString()));
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.STOP_DESCRIPTION_TAG)){
         	this.busETA.setStopDescription(buffer.toString());
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.NUMERIC_VEHICLE_ID_TAG)){
         	this.busETA.setNumericVehicleId(Integer.parseInt(buffer.toString()));
         }
         if(StringUtils.equals(qName, BusEtaXmlConstants.DISTANCE_TO_STOP_TAG)){
         	this.busETA.setDistanceToStopInFeet(Integer.parseInt(buffer.toString()));
         }
         if(StringUtils.equals(qName,BusEtaXmlConstants.NUMERIC_ROUTE_ID_TAG)) {
         	this.busETA.setNumericRouteId(Integer.parseInt(buffer.toString()));
         }
         if(StringUtils.equals(qName,BusEtaXmlConstants.ROUTE_DIRECTION_TAG)) {
         	this.busETA.setRouteDirection(buffer.toString());
         }
         if(StringUtils.equals(qName,BusEtaXmlConstants.DESTINATION_TAG)) {
         	this.busETA.setRouteDestination(buffer.toString());
         }
         if(StringUtils.equals(qName,BusEtaXmlConstants.PREDICTED_ARRIVAL_TIME_TAG)) {
         	this.busETA.setPredictedArrival(buffer.toString());
         }
        if(StringUtils.equals(qName, BusEtaXmlConstants.ERROR_TAG)) {
        	this.busETA.setErrorDescription(buffer.toString());
        	this.errorDescription = buffer.toString();
        }
     }
 
     @Override
 	public void characters(char ch[], int start, int length)
     throws SAXException {
     	buffer.append(ch, start, length);
         System.out.println("start characters : " + buffer);
     }
 }
