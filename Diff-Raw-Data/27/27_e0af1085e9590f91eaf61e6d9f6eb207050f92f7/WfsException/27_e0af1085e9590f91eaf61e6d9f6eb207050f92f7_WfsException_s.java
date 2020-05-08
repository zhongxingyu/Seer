 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.responses;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * This utility defines a general Request type and provides accessor methods 
  * for unversal request information.
  * 
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @tasks REVISIT: refactor this to be named ServiceException?  To reflect
  * the 1.0 spec better?  Backwards compatible exceptions for .14 and .15
  * @version $VERSION$
  */
 public class WfsException extends Exception {
     
     /** message inserted by GeoServer as to what it thinks happened */
     protected String preMessage = new String();
     
     /** full classpath of originating GeoServer class */
     protected String locator = new String();
     
     /** the standard exception that was thrown */
     protected Exception standardException = new Exception();
     
     protected String code = new String();
 
     /** Class logger */
     private static Logger LOGGER =  
         Logger.getLogger("org.vfny.geoserver.responses");
     
     
     /** Empty constructor. */
     public WfsException () { super(); }
      
     /**
      * Assigns a diagnostic code to this exception.
      */
     public void setCode(String code){
 	this.code = code;
     }
  
     /**
      * Empty constructor.
      * @param message The message for the .
      */
     public WfsException (String message) {
         super( message );
         LOGGER.fine( this.getMessage() );
     }
     
     
     /**
      * Empty constructor.
      * @param message The message for the .
      */
    public WfsException (Exception e) {
        super( e.getMessage() );
         LOGGER.fine( this.getMessage() );             
     }
     
     
     /**
      * Empty constructor.
      * @param message The message for the .
      * @param locator The message for the .
      */
     public WfsException (String message, String locator) {
         super(message);
         this.locator = locator;
         LOGGER.fine( "> [" + this.locator + "]:\n  " + this.getMessage());
     }
     
     
     /**
      * Empty constructor.
      * @param message The message for the .
      * @param locator The message for the .
      */
    public WfsException (Exception e, String preMessage, String locator) {
        super( e.getMessage() );
         this.preMessage = preMessage;               
         this.locator = locator;
         LOGGER.fine( "> [" + this.locator + "]:\n  " + 
                      this.preMessage + ": " + this.getMessage());
     }
     
     
     private boolean isEmpty(String testString){
 	return (testString == null) || testString.equals("");
     }
 
     /**
      * Return request type.
      */
    public String getXmlResponse () {
 	String indent = "   ";
        StringBuffer returnXml = new StringBuffer("<?xml version=\"1.0\" ?>\n");
 	returnXml.append("<ServiceExceptionReport\n");
 	returnXml.append(indent + "version=\"1.2.0\"\n");
 	returnXml.append(indent + "xmlns=\"http://www.opengis.net/ogc\"\n");
 	returnXml.append(indent + "xmlns:xsi=\"http://www.w3.org/2001/" + 
 			 "XMLSchema-instance\"\n");
 	//REVISIT: this probably isn't right, need to learn about xml schemas
 	returnXml.append(indent + "xsi:schemaLocation=\"http://www.opengis"
 			 + ".net/ogc ../wfs/1.0.0/OGC-exception.xsd\">\n");
 	//REVISIT: handle multiple service exceptions?  must refactor class.
 	returnXml.append(indent + "<ServiceException");	
 	if (!isEmpty(this.code)) {
 	    returnXml.append(" code=\"" + this.code + "\"");
 	}
 	if (!isEmpty(this.locator)) {
 	    returnXml.append(" locator=\"" + this.locator + "\"");
 	} 
 	returnXml.append(">\n" + indent + indent);
 	if (!isEmpty(this.preMessage)){
 	    returnXml.append(this.preMessage + ": ");
 	} 
 	returnXml.append(this.getMessage() + "\n");
 	returnXml.append(indent + "</ServiceException>\n");
 	returnXml.append("</ServiceExceptionReport>");
 	LOGGER.fine("return wfs exception is " + returnXml);
 	return returnXml.toString();
 	
 	//this is code from .14 and .15.  It can be reused if we need 
 	//backwards compatibility.
         /*String returnXml;
         returnXml = "<WFS_Exception>\n";
         returnXml = returnXml + "   <Exception>\n";
         returnXml = returnXml + "      <Locator>" + this.locator + 
             "</Locator>\n";
         returnXml = returnXml + "      <Message>" + this.preMessage + ": " + 
             this.getMessage() + "</Message>\n";
         returnXml = returnXml + "   </Exception>\n";
         returnXml = returnXml + "</WFS_Exception>\n";
         return returnXml;*/
     }    
 }
