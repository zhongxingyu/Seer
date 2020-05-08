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
 
 package org.guanxi.common;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.StringTokenizer;
 
 /**
  * The Bag contains SAML attributes as convenience objects (Strings). The Bag sits in a Pod which
  * is associated with a Guard session. The Bag provides a way to easily work with SAML attributes
  * by treating them as simple String/Value pairs.
  * The Bag also stores the original SAML Response XML as a String to allow applications to parse
  * and interpret the raw SAML as they see fit. This allows policy rules to be enforced etc, as
  * the attributes do not contain any information other than their names and values.
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  */
 public class Bag {
   /** The value used as a delimiter in multi-valued attributes */
   public static final String ATTR_VALUE_DELIM = ";";
   /** String representing the raw SAML Response */
   private String samlResponse = null;
   /** The attributes as Strings */
   private Hashtable<String, String> attributes = null;
   /** The session ID associated with this bag */
   private String sessionID = null;
 
   /**
    * Default constructor
    */
   public Bag() {
     init();
   }
 
   /**
    * Constructs a Bag of attributes from JSON
    *
    * @param json the attributes in JSON format
    */
   public Bag(String json) {
     init();
     
     String token = null;
     String attributeName = null;
     boolean inSessionID = false, inSAMLResponse = false,
             inAttributeName = false, inAttributeValue = false;
 
    StringTokenizer st = new StringTokenizer(json, "\" ");
     while (st.hasMoreTokens()) {
       token = st.nextToken();
      // Ignore noise
      if ((token.equals(" ")) ||
          (token.contains("{")) ||
          (token.contains("}")) ||
          (token.contains("[")) ||
          (token.contains("]")) ||
          (token.equals(",")) ||
          (token.equals(":"))) {
        continue;
      }
       else if (token.equals("sessionID")) {
         inSessionID = true;
       }
       else if (token.equals("samlResponse")) {
         inSAMLResponse = true;
       }
       else if (token.equals("attributeName")) {
         inAttributeName = true;
       }
       else if (token.equals("attributeValue")) {
         inAttributeValue = true;
       }
       else {
         if (inSessionID) {
           setSessionID(token);
           inSessionID = false;
         }
         if (inSAMLResponse) {
           setSamlResponse(Utils.decodeBase64(token));
           inSAMLResponse = false;
         }
         if (inAttributeName) {
           attributeName = token;
           inAttributeName = false;
         }
         if (inAttributeValue) {
           addAttribute(attributeName, token);
           inAttributeValue = false;
         }
       }
     }
   }
 
   /**
    * Initialises the Bag
    */
   private void init() {
     attributes = new Hashtable<String, String>();
   }
 
   /**
    * Determines whether this Bag contains any attributes.
    *
    * @return true if this Bag contains attributes otherwise false
    */
   public boolean hasAttributes() {
     return !attributes.isEmpty();
   }
 
   /**
    * Adds an attribute to the Bag. The method takes care of multiple valued
    * attributes. Attributes with multiple values have their individual values
    * separated by ATTR_VALUE_DELIM.
    * 
    * @param name The name of the attribute to add
    * @param value The attribute's value
    */
   public void addAttribute(String name, String value) {
     if (attributes.containsKey(name)) {
       String buffer = (String)attributes.get(name);
       buffer += ATTR_VALUE_DELIM + value;
       attributes.remove(name);
       attributes.put(name, buffer);
     }
     else
       attributes.put(name, value);
   }
 
   /**
    * Returns the value of the specified attribute. If the attribute has
    * multiple values, they will be separated by ATTR_VALUE_DELIM.
    *
    * @param name The attribute name
    * @return The values of the attribute, separated by ATTR_VALUE_DELIM if
    * there are multiple values.
    */
   public String getAttributeValue(String name) {
     return (String)attributes.get(name);
   }
 
   /**
    * Returns all the names of the attributes stored in this Bag.
    *
    * @return Enumeration listing all stored attribute names.
    */
   public Enumeration<String> getAttributeNames() {
     return attributes.keys();
   }
 
   /**
    * Sets the raw SAML Response. This can be used by applications to parse to
    * an object model.
    *
    * @param samlResponse String containing the raw SAML Response from which the
    * attributes have been extracted and passed to addAttribute.
    */
   public void setSamlResponse(String samlResponse) { this.samlResponse = samlResponse; }
 
   /**
    * Retrieves the raw SAML Response from which the attributes stored in this bag were
    * extracted.
    *
    * @return String representing the raw SAML Response. 
    */
   public String getSamlResponse() { return samlResponse; }
 
   /**
    * Sets the session ID for the bag
    *
    * @param sessionID the session ID
    */
   public void setSessionID(String sessionID) { this.sessionID = sessionID; }
 
   /**
    * Gets the session ID for the bag
    *
    * @return the session ID
    */
   public String getSessionID() { return sessionID; }
 
   /**
    * Emits the Bag as JSON
    *
    * @return the Bag in JSON format:
    * 
    * {
    *   "bag": {
    *     "sessionID": "2342342343",
    *     "samlResponse": "bas64 encoded SAML Response",
    *     "attributes": [
    *       "attribute": {
    *         "attributeName": "eduPersonTargetedID",
    *         "attributeValue": "12312312312"
    *       },
    *       "attribute": {
    *         "attributeName": "eduPersonAffiliation",
    *         "attributeValue": "member"
    *       }
    *     ]
    *  }
    *}
    */
   public String toJSON() {
     String json = "{";
     json += "\"bag\": {";
     json += "\"sessionID\": \"" + getSessionID() + "\",";
     json += "\"samlResponse\": \"" + getSamlResponse() + "\",";
     json += "\"attributes\": [";
 
     Enumeration<String> names = getAttributeNames();
     String name,value = null;
     boolean first = true;
     while (names.hasMoreElements()) {
       name = (String)names.nextElement();
       value = getAttributeValue(name);
       if (!first) json += ",";
       json += "\"attribute\": {";
       json += "\"attributeName\": " + "\"" + name + "\",";
       json += "\"attributeValue\": " + "\"" + value + "\"";
       json += "}";
       first = false;
     }
 
     json += "]"; // "attributes" [
     json += "}"; // "bag" {
     json += "}"; // {
 
     return json;
   }
 }
