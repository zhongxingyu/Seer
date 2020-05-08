 /*
  * Copyright 2012 Oliver Siegmar <oliver@siegmar.net>
  *
  * This file is part of Billomat4J.
  *
  * Billomat4J is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Billomat4J is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Billomat4J.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.siegmar.billomat4j.sdk.service.impl;
 
 import net.siegmar.billomat4j.sdk.domain.types.PaymentType;
 import net.siegmar.billomat4j.sdk.json.CustomBooleanDeserializer;
 import net.siegmar.billomat4j.sdk.json.PaymentTypesDeserializer;
 import net.siegmar.billomat4j.sdk.json.PaymentTypesSerializer;
 import net.siegmar.billomat4j.sdk.json.Views;
 
 import com.fasterxml.jackson.annotation.JsonInclude;
 import com.fasterxml.jackson.core.Version;
 import com.fasterxml.jackson.databind.DeserializationFeature;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.ObjectReader;
 import com.fasterxml.jackson.databind.ObjectWriter;
 import com.fasterxml.jackson.databind.PropertyNamingStrategy;
 import com.fasterxml.jackson.databind.SerializationFeature;
 import com.fasterxml.jackson.databind.module.SimpleModule;
 
 public class BillomatConfiguration {
 
     private String billomatId;
     private String apiKey;
     private boolean secure = true;
     private boolean ignoreUnknownProperties = true;
     private RequestHelper requestHelper;
     private ObjectReader objectReader;
     private ObjectWriter objectWriter;
 
     public String getBillomatId() {
         return billomatId;
     }
 
     public void setBillomatId(final String billomatId) {
         this.billomatId = billomatId;
     }
 
     public String getApiKey() {
         return apiKey;
     }
 
     public void setApiKey(final String apiKey) {
         this.apiKey = apiKey;
     }
 
     public boolean isSecure() {
         return secure;
     }
 
     /**
      * Sets secure mode (HTTPS instead of HTTP). This is enabled by default.
      *
      * @param secure
      *            {@code true} for HTTPS, {@code false} for HTTP
      */
     public void setSecure(final boolean secure) {
         this.secure = secure;
     }
 
     public boolean isIgnoreUnknownProperties() {
         return ignoreUnknownProperties;
     }
 
     /**
      * Defines if unmappable API response should be ignores. This is the default.
      *
      * @param ignoreUnknownProperties
      *            {@code true} for ignore unknown response attributes
      */
     public void setIgnoreUnknownProperties(final boolean ignoreUnknownProperties) {
         this.ignoreUnknownProperties = ignoreUnknownProperties;
     }
 
     RequestHelper getRequestHelper() {
         return requestHelper;
     }
 
     ObjectReader getObjectReader() {
         return objectReader;
     }
 
     ObjectWriter getObjectWriter() {
         return objectWriter;
     }
 
     synchronized void init() {
         if (requestHelper != null) {
             return;
         }
 
         requestHelper = new RequestHelper(this);
 
         final ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
         objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
 
         objectMapper.registerModule(
                 new SimpleModule("CustomBooleanDeserializer",
                         new Version(1, 0, 0, null, "net.siegmar", "billomat4j"))
                 .addDeserializer(Boolean.class, new CustomBooleanDeserializer()));
 
         objectMapper.registerModule(
                 new SimpleModule("PaymentTypesDeserializer",
                         new Version(1, 0, 0, null, "net.siegmar", "billomat4j"))
                 .addDeserializer(PaymentType[].class, new PaymentTypesDeserializer()));
 
         objectMapper.registerModule(
                 new SimpleModule("PaymentTypesSerializer",
                         new Version(1, 0, 0, null, "net.siegmar", "billomat4j"))
                 .addSerializer(PaymentType[].class, new PaymentTypesSerializer()));
 
         objectReader = objectMapper.reader()
                 .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
                .with(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                 .with(DeserializationFeature.UNWRAP_ROOT_VALUE);
 
         if (isIgnoreUnknownProperties()) {
             objectReader = objectReader.without(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
         }
 
         objectWriter = objectMapper.writer()
                 .withView(Views.Default.class)
                 .with(SerializationFeature.WRAP_ROOT_VALUE)
                 .without(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)
                 .without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
     }
 
 }
