 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Enriko Käsper
  */
 
 package eionet.webq.dto;
 
 import java.util.Date;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Object for transferring XML save result messages to XForms. The object is marshaled into XML in HTTP response.
  *
  * @author Enriko Käsper
  */
 @XmlRootElement(name = "saveresult")
 public class XmlSaveResult {
 
     /** Result code: 0 - failed; 1 - successful save. */
     private int code;
     /** Save result message. */
     private String message;
     /** Timestamp of action took place. */
     private Date timestamp;
 
     /** Message to be returned in case of successful save action. */
     private static final String SUCCESS_MESSAGE = "Content saved successfully.";
     /** Message to be returned in case of error. */
     private static final String ERROR_MESSAGE = "Error on saving data! ";
 
     /**
      * No-arg default constructor required by {@link org.springframework.oxm.jaxb.Jaxb2Marshaller}.
      */
     public XmlSaveResult() {
         super();
     }
 
     /**
      * Private constructor for generating object with correct data in fields.
      *
      * @param code save result code
      * @param message result message
     * @param timestamp timestamp the object was generated
      */
     private XmlSaveResult(int code, String message) {
         super();
         this.code = code;
         this.message = message;
         this.timestamp = new Date();
     }
 
     /**
      * Generates save result object with error code.
      *
      * @return the save result object
      */
     public static XmlSaveResult valueOfSuccess() {
         return new XmlSaveResult(1, SUCCESS_MESSAGE);
     }
 
     /**
      * Generates save result object with success code.
      *
      * @return the save result object
      */
     public static XmlSaveResult valueOfError(String message) {
         return new XmlSaveResult(0, ERROR_MESSAGE.concat(" ").concat(message));
     }
 
     /**
      * @return the code
      */
     @XmlElement(name = "code")
     public int getCode() {
         return code;
     }
 
     /**
      * @return the message
      */
     @XmlElement(name = "message")
     public String getMessage() {
         return message;
     }
 
     /**
      * @return the timestamp
      */
     @XmlElement(name = "timestamp")
     public Date getTimestamp() {
         return timestamp;
     }
 
 }
