 /*
  jBilling - The Enterprise Open Source Billing System
  Copyright (C) 2003-2011 Enterprise jBilling Software Ltd. and Emiliano Conde
 
  This file is part of jbilling.
 
  jbilling is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  jbilling is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.sapienter.jbilling.server.metafields.db.value;
 
 import com.sapienter.jbilling.server.metafields.db.MetaField;
 import com.sapienter.jbilling.server.metafields.db.MetaFieldValue;
 import org.apache.log4j.Logger;
 import org.codehaus.groovy.grails.web.json.JSONObject;
 import org.codehaus.groovy.grails.web.json.parser.JSONParser;
 import org.codehaus.groovy.grails.web.json.parser.ParseException;
 
 import javax.persistence.Column;
 import javax.persistence.DiscriminatorValue;
 import javax.persistence.Entity;
 import javax.persistence.Transient;
 import java.io.StringReader;
 
 @Entity
 @DiscriminatorValue("json")
public class JsonMetaFieldValue extends MetaFieldValue<JSONObject> {
 
     private static final Logger LOG = Logger.getLogger(JsonMetaFieldValue.class);
 
     private String json;
 
     public JsonMetaFieldValue() {
     }
 
     public JsonMetaFieldValue(MetaField name) {
         super(name);
     }
 
     @Column(name = "string_value", nullable = true)
    public String getJson() {
         return json;
     }
 
    public void setJson(String json) {
         this.json = json;
     }
 
     @Transient
    public JSONObject getValue() {
         try {
             return (JSONObject) new JSONParser(new StringReader(json)).parse();
         } catch (ParseException e) {
             LOG.error("Could not parse string as JSON object.", e);
             return null;
         }
     }
 
    public void setValue(JSONObject value) {
         this.json = value != null ? value.toString() : null;
     }
 }
