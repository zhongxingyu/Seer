 package org.codehaus.xfire.fault;
 
 import org.codehaus.xfire.service.Echo;
import org.jdom.Element;
 
 /**
  * Throws an exception while echoing.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  * @since Feb 18, 2004
  */
 public class CustomFaultEcho
     implements Echo
 {
     public Element echo(Element e) 
         throws XFireFault
     {
         throw new CustomXFireFault();
     }
 }
