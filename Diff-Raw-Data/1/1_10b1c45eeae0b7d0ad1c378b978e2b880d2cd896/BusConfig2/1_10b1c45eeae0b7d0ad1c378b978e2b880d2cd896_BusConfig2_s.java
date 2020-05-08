 /*
  * Copyright 2012 Janrain, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.janrain.backplane2.server.config;
 
 import com.janrain.backplane2.server.InvalidRequestException;
 import com.janrain.backplane2.server.dao.DaoFactory;
 import com.janrain.backplane2.server.provision.ProvisioningConfig;
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.message.MessageField;
 
 import java.util.EnumSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Johnny Bufu
  */
 public class BusConfig2 extends ProvisioningConfig {
 
     // - PUBLIC
 
     public BusConfig2() {}
 
     public BusConfig2(String busName, String busOwner, String retentionTimeSeconds, String retentionTimeStickySeconds) throws SimpleDBException {
         Map<String,String> d = new LinkedHashMap<String, String>();
         d.put(Field.BUS_NAME.getFieldName(), busName);
         d.put(Field.OWNER.getFieldName(), busOwner);
         d.put(Field.RETENTION_TIME_SECONDS.getFieldName(), retentionTimeSeconds);
         d.put(Field.RETENTION_STICKY_TIME_SECONDS.getFieldName(), retentionTimeStickySeconds);
         super.init(busName, d);
     }
 
     @Override
     public String getIdValue() {
         return get(Field.BUS_NAME);
     }
 
     @Override
     public Set<? extends MessageField> getFields() {
         return EnumSet.allOf(Field.class);
     }
 
     @Override
     public void validate(DaoFactory daoFactory) throws Exception {
         User user = daoFactory.getBusOwnerDAO().retrieveBusOwner(get(Field.OWNER.getFieldName()));
         if (user == null) {
             throw new InvalidRequestException("Invalid bus owner: " + get(Field.OWNER.getFieldName()));
         }
     }
 
     public static enum Field implements MessageField {
 
         BUS_NAME,
 
         OWNER,
 
         RETENTION_TIME_SECONDS {
             @Override
             public void validate(String value) throws SimpleDBException {
                 if (isRequired() || value != null) {
                     String fieldName = getFieldName();
                     int intValue = validateInt(fieldName, value);
                     if (intValue < RETENTION_MIN_SECONDS || intValue > RETENTION_MAX_VALUE) {
                         throw new InvalidRequestException("Value of " + fieldName + " = " + intValue + " but must be between " + RETENTION_MIN_SECONDS + " and " + RETENTION_MAX_VALUE);
                     }
                 }
             }},
 
         RETENTION_STICKY_TIME_SECONDS {
             @Override
             public void validate(String value) throws SimpleDBException {
                 if (isRequired() || value != null) {
                     String fieldName = getFieldName();
                     validateInt(fieldName, value);
                     int intValue = validateInt(fieldName, value);
                     if (intValue < RETENTION_STICKY_MIN_SECONDS || intValue > RETENTION_STICKY_MAX_VALUE) {
                         throw new InvalidRequestException("Value of " + fieldName + " = " + intValue + " but must be between " + RETENTION_STICKY_MIN_SECONDS + " and " + RETENTION_STICKY_MAX_VALUE);
                     }
                 }
             }};
 
 
 
 
         @Override
         public String getFieldName() {
             return name();
         }
 
         @Override
         public boolean isRequired() {
             return true;
         }
 
         @Override
         public void validate(String value) throws SimpleDBException {
             if (isRequired()) validateNotNull(name(), value);
         }
 
         // - PRIVATE
 
         private static final int RETENTION_MIN_SECONDS = 60;
         private static final int RETENTION_MAX_VALUE = 604800; // one week
         private static final int RETENTION_STICKY_MIN_SECONDS = 28800; // eight hours
         private static final int RETENTION_STICKY_MAX_VALUE = 604800; // one week
 
     }
 }
