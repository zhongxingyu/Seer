 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 /**
  * Copyright (c) 2010, Ben Fortuna
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *  o Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  *  o Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  *  o Neither the name of Ben Fortuna nor the names of any other contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.bedework.util.calendar;
 
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.CalendarException;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.TimeZoneRegistry;
 import net.fortuna.ical4j.model.property.DateListProperty;
 import net.fortuna.ical4j.model.property.DateProperty;
 import net.fortuna.ical4j.model.property.Geo;
 import net.fortuna.ical4j.model.property.RequestStatus;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URISyntaxException;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 
 /**
  * Parses and builds an iCalendar model from a json input stream.
  * Note that this class is not thread-safe.
  *
  * @version 1.0
  * @author Mike Douglass
  *
  * Created: Sept 8, 2010
  *
  */
 public class JsonCalendarBuilder {
   private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
 
   private final static JsonFactory jsonFactory;
 
   static {
     jsonFactory = new JsonFactory();
     jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
     jsonFactory.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
     jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
   }
 
   //private Logger log = Logger.getLogger(XmlCalendarBuilder.class);
 
   private final TimeZoneRegistry tzRegistry;
 
   /**
    * @param tzRegistry a custom timezone registry
    */
   public JsonCalendarBuilder(final TimeZoneRegistry tzRegistry) {
     this.tzRegistry = tzRegistry;
   }
 
   /**
    * Builds an iCalendar model from the specified input stream.
    * @param in an input stream to read calendar data from
    * @return a calendar parsed from the specified input stream
    * @throws java.io.IOException where an error occurs reading data from the specified stream
    * @throws net.fortuna.ical4j.data.ParserException where an error occurs parsing data from the stream
    */
   public Calendar build(final InputStream in) throws IOException,
   ParserException {
     return build(new InputStreamReader(in, DEFAULT_CHARSET));
   }
 
   /**
    * Build an iCalendar model by parsing data from the specified reader.
    *
    * @param in an unfolding reader to read data from
    * @return a calendar parsed from the specified reader
    * @throws java.io.IOException where an error occurs reading data from the specified reader
    * @throws net.fortuna.ical4j.data.ParserException where an error occurs parsing data from the reader
    */
   public Calendar build(final Reader in)
           throws IOException, ParserException {
     BuildState bs = new BuildState(tzRegistry);
 
     bs.setContentHandler(new ContentHandlerImpl(bs));
 
     try {
       JsonParser parser = jsonFactory.createParser(in);
 
       process(parser, bs);
     } catch (Throwable t) {
       throw new ParserException(t.getMessage(), 0, t);
     }
 
     if ((bs.getDatesMissingTimezones().size() > 0) && (tzRegistry != null)) {
       resolveTimezones(bs);
     }
 
     return bs.getCalendars().iterator().next();
   }
 
   private void process(final JsonParser parser,
                        final BuildState bs) throws ParserException {
     /* ["vcalendar",
           [ <properties> ],
           [ <components> ]
       ]
       */
 
     try {
       arrayStart(parser);
       String ctype = textField(parser);
 
       if (!ctype.equals("vcalendar")) {
         // error
         throwException("Expected vcalendar: found " + ctype, parser);
       }
 
       bs.setCalendar(null);
       processVcalendar(parser, bs);
 
       if (bs.getCalendar() != null) {
         bs.getCalendars().add(bs.getCalendar());
       }
 
       arrayEnd(parser);
     } catch (Throwable t) {
       handleException(t, parser);
     }
   }
 
   private void processVcalendar(final JsonParser parser,
                                 final BuildState bs) throws ParserException {
     bs.getContentHandler().startCalendar();
 
     /* Properties first */
     processProperties(parser, bs);
 
     /* Now components */
     processCalcomps(parser, bs);
   }
 
   private void processProperties(final JsonParser parser,
                                  final BuildState bs) throws ParserException {
     arrayStart(parser);
 
     while (!testArrayEnd(parser)) {
       processProperty(parser, bs);
     }
   }
 
   private void processCalcomps(final JsonParser parser,
                                final BuildState bs) throws ParserException {
     arrayStart(parser);
 
     while (!testArrayEnd(parser)) {
       processComponent(parser, bs);
     }
   }
 
   private void processComponent(final JsonParser parser,
                                 final BuildState bs) throws ParserException {
     currentArrayStart(parser);
 
     String cname = textField(parser).toUpperCase();
 
     /* Properties first */
     processProperties(parser, bs);
 
     /* Now components */
     processCalcomps(parser, bs);
 
     bs.getContentHandler().endComponent(cname);
 
     arrayEnd(parser);
   }
 
   private void processProperty(final JsonParser parser,
                                final BuildState bs) throws ParserException {
     /* Each individual iCalendar property is represented in jCal by an array
       with three fixed elements, followed by at one or more additional
       elements, depending on if the property is a multi-value property as
       described in Section 3.1.2 of [RFC5545].
 
       The array consists of the following fixed elements:
       1. The name of the property as a string, but in lowercase.
       2. An object containing the parameters as described in Section 3.5.
       3. The type identifier string of the value, in lowercase.
 
       The remaining elements of the array are used for the value of the
       property. For single-value properties, the array MUST have exactly
       four elements, for multi-valued properties as described in
       Section 3.4.1.1 there can be any number of additional elements.
 
       array start should be current token
     */
 
     currentArrayStart(parser);
 
     String name = textField(parser);
     bs.getContentHandler().startProperty(name);
 
     processParameters(parser, bs);
 
     if (!processValue(parser, bs, textField(parser))) {
       throwException("Bad property", parser);
     }
 
     bs.getContentHandler().endProperty(name);
 
     arrayEnd(parser);
   }
 
   private void processParameters(final JsonParser parser,
                                  final BuildState bs) throws ParserException {
     objectStart(parser);
 
     while (!testObjectEnd(parser)) {
       processParameter(parser, bs);
     }
   }
 
   private void processParameter(final JsonParser parser,
                                 final BuildState bs) throws ParserException {
     try {
       bs.getContentHandler().parameter(currentFieldName(parser),
                                        textField(parser));
     } catch (Throwable t) {
       handleException(t, parser);
     }
   }
 
   private boolean processValue(final JsonParser parser,
                                final BuildState bs,
                                final String type) throws ParserException {
     try {
       if (bs.getProperty() instanceof Geo) {
         // 2 floats in an array
         arrayStart(parser);
 
         StringBuilder sb = new StringBuilder();
 
         sb.append(String.valueOf(floatField(parser)));
         sb.append(",");
         sb.append(String.valueOf(floatField(parser)));
 
         arrayEnd(parser);
 
         bs.getContentHandler().propertyValue(sb.toString());
 
         return true;
       }
 
       if (bs.getProperty() instanceof RequestStatus) {
         arrayStart(parser);
 
         StringBuilder sb = new StringBuilder();
 
         sb.append(textField(parser));
         sb.append(",");
         sb.append(textField(parser));
 
         if (!testArrayEnd(parser)) {
           sb.append(",");
           sb.append(currentTextField(parser));
 
           arrayEnd(parser);
         }
 
         bs.getContentHandler().propertyValue(sb.toString());
 
         return true;
       }
 
       if (type.equals("recur")) {
         /*
             value-recur = element recur {
               type-freq,
               (type-until | type-count)?,
               element interval  { text }?,
               element bysecond  { text }*,
               element byminute  { text }*,
               element byhour    { text }*,
               type-byday*,
               type-bymonthday*,
               type-byyearday*,
               type-byweekno*,
               element bymonth   { text }*,
               type-bysetpos*,
               element wkst { type-weekday }?
           }
 
          */
         StringBuilder sb = new StringBuilder();
 
         String delim = "";
 
         while (!testObjectEnd(parser)) {
           sb.append(delim);
           delim = ";";
           String recurEl = textField(parser);
           sb.append(recurEl.toUpperCase());
           sb.append("=");
           sb.append(recurElVal(parser, recurEl));
         }
 
         bs.getContentHandler().propertyValue(sb.toString());
 
         return true;
       }
 
       if (type.equals("boolean")) {
         bs.getContentHandler().propertyValue(String.valueOf(
                 booleanField(parser)));
 
         return true;
       }
 
       if (type.equals("binary") ||
           type.equals("cal-address") ||
           type.equals("duration") ||
           type.equals("text") ||
           type.equals("uri") ||
           type.equals("utc-offset")) {
         bs.getContentHandler().propertyValue(textField(parser));
 
         return true;
       }
 
       if (type.equals("integer")) {
         bs.getContentHandler().propertyValue(String.valueOf(intField(
                 parser)));
 
         return true;
       }
 
       if (type.equals("float")) {
         bs.getContentHandler().propertyValue(String.valueOf(intField(
                 parser)));
 
         return true;
       }
 
       if (type.equals("date") ||
           type.equals("dateTime") ||
           type.equals("time")) {
         bs.getContentHandler().propertyValue(
                 XcalUtil.getIcalFormatDateTime(textField(parser)));
         return true;
       }
 
       if (type.equals("time")) {
         bs.getContentHandler().propertyValue(
                 XcalUtil.getIcalFormatTime(textField(parser)));
         return true;
       }
 
       if (type.equals("period")) {
         String[] parts = textField(parser).split("/");
 
         StringBuilder sb = new StringBuilder();
 
         sb.append(XcalUtil.getIcalFormatDateTime(parts[0]));
 
         if (parts[1].toUpperCase().startsWith("P")) {
           sb.append(parts[1]);
         } else {
           sb.append(XcalUtil.getIcalFormatDateTime(parts[1]));
         }
 
         bs.getContentHandler().propertyValue(sb.toString());
 
         return true;
       }
 
       return false;
     } catch (URISyntaxException e) {
       throw new ParserException(e.getMessage(), 0, e);
     } catch (ParseException e) {
       throw new ParserException(e.getMessage(), 0, e);
     } catch (IOException e) {
       throw new ParserException(e.getMessage(), 0, e);
     }
   }
 
   private String recurElVal(final JsonParser parser,
                             final String el) throws ParserException {
     if (el.equals("freq")) {
       return textField(parser);
     }
 
     if (el.equals("wkst")) {
       return textField(parser);
     }
 
     if (el.equals("until")) {
       return textField(parser);
     }
 
     if (el.equals("count")) {
       return String.valueOf(intField(parser));
     }
 
     if (el.equals("interval")) {
       return String.valueOf(intField(parser));
     }
 
     if (el.equals("bymonth")) {
       return intList(parser);
     }
 
     if (el.equals("byweekno")) {
       return intList(parser);
     }
 
     if (el.equals("byyearday")) {
       return intList(parser);
     }
 
     if (el.equals("bymonthday")) {
       return intList(parser);
     }
 
     if (el.equals("byday")) {
       return textList(parser);
     }
 
     if (el.equals("byhour")) {
       return intList(parser);
     }
 
     if (el.equals("byminute")) {
       return intList(parser);
     }
 
     if (el.equals("bysecond")) {
       return intList(parser);
     }
 
     if (el.equals("bysetpos")) {
       return intList(parser);
     }
 
     throwException("Unexpected recur field " + el, parser);
     return null;
   }
 
   /**
    * Returns the timezone registry used in the construction of calendars.
    * @return a timezone registry
    */
   public final TimeZoneRegistry getRegistry() {
     return tzRegistry;
   }
 
   private void resolveTimezones(final BuildState bs) throws IOException {
 
     // Go through each property and try to resolve the TZID.
     for (final Property property: bs.getDatesMissingTimezones()) {
       final Parameter tzParam = property.getParameter(Parameter.TZID);
 
       // tzParam might be null:
       if (tzParam == null) {
         continue;
       }
 
       //lookup timezone
       final TimeZone timezone = tzRegistry.getTimeZone(tzParam.getValue());
 
       // If timezone found, then update date property
       if (timezone != null) {
         // Get the String representation of date(s) as
         // we will need this after changing the timezone
         final String strDate = property.getValue();
 
         // Change the timezone
         if(property instanceof DateProperty) {
           ((DateProperty) property).setTimeZone(timezone);
         }
         else if(property instanceof DateListProperty) {
           ((DateListProperty) property).setTimeZone(timezone);
         }
 
         // Reset value
         try {
           property.setValue(strDate);
         } catch (ParseException e) {
           // shouldn't happen as its already been parsed
           throw new CalendarException(e);
         } catch (URISyntaxException e) {
           // shouldn't happen as its already been parsed
           throw new CalendarException(e);
         }
       }
     }
   }
 
   /* ====================================================================
    *                   XmlUtil wrappers
    * ==================================================================== */
 
   private void throwException(final String msg,
                               final JsonParser parser) throws ParserException {
     handleException(new Throwable(msg), parser);
   }
 
   private Object handleException(final Throwable t,
                                  final JsonParser parser) throws ParserException {
     if (t instanceof ParserException) {
       throw (ParserException)t;
     }
 
     try {
       int lnr = parser.getCurrentLocation().getLineNr();
       throw new ParserException(t.getLocalizedMessage(), lnr);
     } catch (Throwable t1) {
       throw new ParserException(t.getLocalizedMessage(), -1);
     }
   }
 
   private void arrayStart(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.START_ARRAY,
                 "Expected array start");
   }
 
   private void arrayEnd(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.END_ARRAY,
                 "Expected array end");
   }
 
   private void currentArrayStart(final JsonParser parser) throws ParserException {
     expectCurrentToken(parser, JsonToken.START_ARRAY,
                 "Expected array start");
   }
 
   private boolean testNextArrayStart(final JsonParser parser) throws ParserException {
     return testToken(parser, JsonToken.START_ARRAY);
   }
 
   private boolean testArrayEnd(final JsonParser parser) throws ParserException {
     return testToken(parser, JsonToken.END_ARRAY);
   }
 
   private void objectStart(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.START_OBJECT,
                 "Expected object start");
   }
 
   private boolean testObjectEnd(final JsonParser parser) throws ParserException {
     return testToken(parser, JsonToken.END_OBJECT);
   }
 
   private void expectToken(final JsonParser parser,
                            final JsonToken expected,
                            final String message) throws ParserException {
     try {
       JsonToken t = parser.nextToken();
 
       if (t != expected) {
         throwException(message, parser);
       }
     } catch (Throwable t) {
       handleException(t, parser);
     }
   }
 
   private void expectCurrentToken(final JsonParser parser,
                                   final JsonToken expected,
                                   final String message) throws ParserException {
     try {
       JsonToken t = parser.getCurrentToken();
 
       if (t != expected) {
         throwException(message, parser);
       }
     } catch (Throwable t) {
       handleException(t, parser);
     }
   }
 
   private boolean testToken(final JsonParser parser,
                             final JsonToken expected) throws ParserException {
     try {
       JsonToken t = parser.nextToken();
 
       return t == expected;
     } catch (Throwable t) {
       return (Boolean)handleException(t, parser);
     }
   }
 
   private String textField(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.VALUE_STRING,
                 "Expected string field");
     try {
       return parser.getText();
     } catch (Throwable t) {
       return (String)handleException(t, parser);
     }
   }
 
   private String currentTextField(final JsonParser parser) throws ParserException {
     try {
       return parser.getText();
     } catch (Throwable t) {
       return (String)handleException(t, parser);
     }
   }
 
   private int currentIntField(final JsonParser parser) throws ParserException {
     try {
       return parser.getIntValue();
     } catch (Throwable t) {
       return (Integer)handleException(t, parser);
     }
   }
 
   private int intField(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.VALUE_NUMBER_INT,
                 "Expected integer field");
     try {
       return parser.getIntValue();
     } catch (Throwable t) {
       return (Integer)handleException(t, parser);
     }
   }
 
   private float floatField(final JsonParser parser) throws ParserException {
     expectToken(parser, JsonToken.VALUE_NUMBER_FLOAT,
                 "Expected float field");
     try {
       return parser.getFloatValue();
     } catch (Throwable t) {
       return (Float)handleException(t, parser);
     }
   }
 
   private boolean booleanField(final JsonParser parser) throws ParserException {
     try {
       if (parser.getCurrentToken() == JsonToken.VALUE_FALSE) {
         return false;
       }
 
       if (parser.getCurrentToken() == JsonToken.VALUE_TRUE) {
         return true;
       }
 
       throwException("expected boolean constant", parser);
       return false;
     } catch (Throwable t) {
       return (Boolean)handleException(t, parser);
     }
   }
 
   private String currentFieldName(final JsonParser parser) throws ParserException {
     expectCurrentToken(parser, JsonToken.FIELD_NAME,
                 "Expected field name");
     try {
       return parser.getText();
     } catch (Throwable t) {
       return (String)handleException(t, parser);
     }
   }
 
   private String textList(final JsonParser parser) throws ParserException {
     if (!testNextArrayStart(parser)) {
       // Single textt value
       return currentTextField(parser);
     }
 
     StringBuilder sb = new StringBuilder(currentTextField(parser));
     while (!testArrayEnd(parser)) {
       sb.append(",");
       sb.append(currentTextField(parser));
     }
 
     return sb.toString();
   }
 
   private String intList(final JsonParser parser) throws ParserException {
     if (!testNextArrayStart(parser)) {
       // Single int value
       return String.valueOf(currentIntField(parser));
     }
 
     StringBuilder sb = new StringBuilder(currentIntField(parser));
     while (!testArrayEnd(parser)) {
       sb.append(",");
       sb.append(currentIntField(parser));
     }
 
     return sb.toString();
   }
 }
