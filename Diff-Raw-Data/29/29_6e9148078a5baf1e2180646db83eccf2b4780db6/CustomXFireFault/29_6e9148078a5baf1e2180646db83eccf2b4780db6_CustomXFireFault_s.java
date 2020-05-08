 package org.codehaus.xfire.fault;
 
import org.codehaus.yom.Element;
 
 public class CustomXFireFault
     extends XFireFault
 {
     public CustomXFireFault()
     {
         super("CustomFault", XFireFault.MUST_UNDERSTAND);
         
        getDetail().appendChild(new Element("test"));
     }
 }
