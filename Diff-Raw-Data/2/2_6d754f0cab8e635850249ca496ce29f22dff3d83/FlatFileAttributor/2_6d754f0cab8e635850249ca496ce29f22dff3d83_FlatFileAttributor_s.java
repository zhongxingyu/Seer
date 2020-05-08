 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.idp.farm.attributors;
 
 import org.guanxi.common.GuanxiException;
 import org.guanxi.common.GuanxiPrincipal;
 import org.guanxi.xal.idp.*;
 import org.apache.xmlbeans.XmlException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * <h1>FlatFileAttributor</h1>
  * Attributor implementation that gets it's attribute information from a flat file.
  *
  * @author Alistair Young
  */
 public class FlatFileAttributor extends SimpleAttributor {
   /** Our config */
   private FlatFileAuthenticatorConfigDocument.FlatFileAuthenticatorConfig ffConfig = null;
 
   public void init() {
     try {
       super.init();
       
       FlatFileAuthenticatorConfigDocument configDoc = FlatFileAuthenticatorConfigDocument.Factory.parse(new File(servletContext.getRealPath(attributorConfig)));
       ffConfig = configDoc.getFlatFileAuthenticatorConfig();
     }
     catch(IOException me) {
       logger.error("Can't load attributor config file", me);
     }
     catch(XmlException xe) {
       logger.error("Can't parse attributor config file", xe);
     }
   }
 
   /**
    * Retrieves attributes for a user from a flat file.
    *
    * @param principal GuanxiPrincipal identifying the previously authenticated user
    * @param relyingParty The providerId of the relying party the attribute are for
    * @param attributes The document into whic to put the attributes
    * @throws GuanxiException if an error occurs
    */
   public void getAttributes(GuanxiPrincipal principal, String relyingParty, UserAttributesDocument.UserAttributes attributes) throws GuanxiException {
     // GuanxiPrincipal is storing their username, put there by the authenticator
     String username = (String)principal.getPrivateProfileDataEntry("username");
 
     // Look for the user in the config file
     User[] users = ffConfig.getUserArray();
     for (int c=0; c < users.length; c++) {
       if (users[c].getUsername().equals(username)) {
         // Load up their attributes from the config file
         UserAttribute[] attrs = users[c].getUserAttributeArray();
         for (int cc=0; cc < attrs.length; cc++) {
           // This is the default name and value for the attribute
           String attrName = attrs[cc].getName();
           String attrValue = attrs[cc].getValue();
 
           // Can we release the original attributes without mapping?
           if (arpEngine.release(relyingParty, attrName, attrValue)) {
             AttributorAttribute attribute = attributes.addNewAttribute();
             attribute.setName(attrName);
             attribute.setValue(attrValue);
 
             logger.debug("Released attribute " + attrName);
           }
 
           // Sort out any mappings. This will change the default name/value if necessary...
           if (mapper.map(principal, relyingParty, attrName, attrValue)) {
             for (int mapCount = 0; mapCount < mapper.getMappedNames().length; mapCount++) {
              logger.debug("Mapped attribute " + attrName + " to " + mapper.getMappedNames()[mapCount]);
 
               attrName = mapper.getMappedNames()[mapCount];
               attrValue = mapper.getMappedValues()[mapCount];
 
               // ...then run the original or mapped attribute through the ARP
               if (arpEngine.release(relyingParty, attrName, attrValue)) {
                 AttributorAttribute attribute = attributes.addNewAttribute();
                 attribute.setName(attrName);
                 attribute.setValue(attrValue);
 
                 logger.debug("Released attribute " + attrName);
               }
             } // for (int mapCount = 0; mapCount < mapper.getMappedNames().length; mapCount++) {
           } // if (mapper.map(principal.getProviderID(), attrName, attrValue)) {
         } // for (int cc=0; cc < attrs.length; cc++)
       } // if (users[c].getUsername().equals(username))
     } // for (int c=0; c < users.length; c++)
   }
 }
