 /*******************************************************************************
  * Copyright (c) EclipseSource (2011). All Rights Reserved.
  * 
  * Contributors:
  *      Holger Staudacher - initial API and Implementation
  ******************************************************************************/
 package pickupnet.ui.driver;
 
 import java.io.IOException;
 import java.util.Enumeration;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.emf.common.util.EList;
 
 import pickupnet.Driver;
 import pickupnet.Pickupnet;
 import pickupnet.Shipment;
 import pickupnet.ShipmentStatus;
 
 
 public class AssignServlet extends HttpServlet {
 
   private static final long serialVersionUID = -9081814342184175154L;
   
   @Override
   protected void doGet( HttpServletRequest req, HttpServletResponse resp )
     throws ServletException, IOException
   {
     String driverId = req.getParameter( "id" );
     if( driverId == null ) {
      resp.getWriter().write( "No driver spcified" );
     } else {
       String content = createContent( driverId );
       resp.getWriter().write( content );
     }
   }
 
   private String createContent( String driverId ) {
     StringBuffer buffer = new StringBuffer();
     buffer.append( "<html>" );
     addHead( driverId, buffer );
     addShipments( driverId, buffer );
     buffer.append( "</html></body>" );
     return buffer.toString();
   }
 
   private void addHead( String driverId, StringBuffer buffer ) {
     buffer.append( "<head><title>Do a Job</title>" );
     buffer.append( "<meta name=\"viewport\" content=\"initial-scale=1.3, user-scalable=no\">" );
     buffer.append( "</head>" );
     buffer.append( "<body style=\"font-family: Helvetica, Sans-Serif\">" );
     buffer.append( "<a href=\"/assignments?id=" + driverId + "\">Your Assignments</a>" );
     buffer.append( "&nbsp;&nbsp;&nbsp;" );
     buffer.append( "Do a job" );
   }
 
   private void addShipments( String driverId, StringBuffer buffer ) {
     EList<Shipment> shipments = Pickupnet.STATION_1.getShipments();
     buffer.append( "<form method=\"post\">" );
     buffer.append( "<ul>" );
     for( Shipment shipment : shipments ) {
       Driver driver = shipment.getDriver();
       if( driver == null ) {
         addShipment( shipment, buffer );
       }
     }
     buffer.append( "</ul>" );
     buffer.append( "<input type=\"submit\" value=\"Apply\">" );
     buffer.append( "</form>" );
   }
 
   private void addShipment( Shipment shipment, StringBuffer buffer ) {
     buffer.append( "<li>" );
     buffer.append( "From: " + shipment.getPickUpAddress().getText() + "<br>" );
     buffer.append( "To: " + shipment.getShipToAddress().getText() + "<br>" );
     buffer.append( "<input type=\"checkbox\" name=\"" + shipment.getId() + "\">" );
     buffer.append( "Assign to me" );
     buffer.append( "</li>" );
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected void doPost( HttpServletRequest req, HttpServletResponse resp )
     throws ServletException, IOException
   {
     String id = req.getParameter( "id" );
     Enumeration<String> parameterNames = req.getParameterNames();
     while( parameterNames.hasMoreElements() ) {
       String name = ( String )parameterNames.nextElement();
       updateShipment( name, id );
     }
     resp.getWriter().write( "<html><body>" );
     resp.getWriter().write( "Shipments successfully updated.</br>" );
     resp.getWriter().write( "<a href=\"/assign?id=" + id + "\">Go Back</a>" );
     resp.getWriter().write( "</html></body>" );
   }
 
   private void updateShipment( String name, String driverId ) {
     EList<Shipment> shipments = Pickupnet.STATION_1.getShipments();
     for( Shipment shipment : shipments ) {
       if( shipment.getId().equals( name ) ) {
         shipment.setStatus( ShipmentStatus.ASSIGNED );
         shipment.setDriver( getDriver( driverId ) );
       }
     }
   }
 
   private Driver getDriver( String driverId ) {
     Driver result = null;
     EList<Driver> drivers = Pickupnet.STATION_1.getDrivers();
     for( Driver driver : drivers ) {
       if( driver.getId().equals( driverId ) ) {
         result = driver;
       }
     }
     return result;
   }
 }
