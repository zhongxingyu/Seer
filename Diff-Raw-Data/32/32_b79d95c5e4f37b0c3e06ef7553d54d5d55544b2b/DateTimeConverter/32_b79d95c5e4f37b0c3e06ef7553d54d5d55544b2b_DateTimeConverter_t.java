 package fi.vincit.jmobster.processor.languages.javascript.valueconverters;
 
 /*
  * Copyright 2012 Juha Siponen
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 /**
  * Converter for Java Date objects. Converts values to quoted strings.
  */
 public class DateTimeConverter extends StringConverter {
 
     /**
      * What time should be used as default time
      */
     public static enum DefaultTime {
         /**
          * Time the default value was generated
          */
         NOW,
         /**
          * Unix time 0 (January 1, 1970). Default
          */
         EPOCH_0
     }
 
     private DateFormat dateFormat;
     private DefaultTime defaultTime = DefaultTime.EPOCH_0;
 
     public DateTimeConverter(String pattern) {
         dateFormat = new SimpleDateFormat(pattern);
     }
 
     public DateTimeConverter(String pattern, Locale locale) {
         dateFormat = new SimpleDateFormat(pattern, locale);
     }
 
     public DateTimeConverter( DateFormat dateFormat ) {
         this.dateFormat = dateFormat;
     }
 
     public DefaultTime getDefaultTime() {
         return defaultTime;
     }
 
     public void setDefaultTime( DefaultTime defaultTime ) {
         this.defaultTime = defaultTime;
     }
 
     @Override
     protected String getTypeDefaultValue() {
         if( defaultTime == null ) {
             throw new RuntimeException("DefaultTime for DateTimeConverter is not set");
         }
        Date defaultDate;
         switch( defaultTime ) {
             case NOW: defaultDate = new Date(); break;
             case EPOCH_0: defaultDate = new Date(0); break;
             default: throw new RuntimeException("DefaultTime for DateTimeConverter is not set");
         }
         return super.getValueAsString( dateFormat.format( defaultDate ) );
     }
 
     @Override
     protected String getValueAsString( Object value ) {
         return super.getValueAsString( dateFormat.format( (Date)value ) );
     }
 }
