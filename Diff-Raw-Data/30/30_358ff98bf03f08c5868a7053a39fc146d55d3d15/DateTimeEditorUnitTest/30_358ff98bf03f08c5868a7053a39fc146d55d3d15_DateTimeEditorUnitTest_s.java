 /*
  * Copyright 2002-2009 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.synyx.minos.core.web;
 
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.text.ParseException;
import java.util.Locale;
 
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 
 
 /**
  * Unit test for JodaTime related {@link Formatter} implementations.
 * 
 * @author Oliver Gierke - gierke@synyx.de
  */
 public class DateTimeEditorUnitTest {
 
     private DateTimeEditor editor;
 
     private DateTime date;
     private String dateAsString;
     private String dateTimeAsString;
 
     private String dateAsStringShort;
     private String shortPattern;
 

     @Before
     public void setUp() {
 
         editor = new DateTimeEditor(Locale.GERMAN);
 
         date = new DateTime(2009, 9, 1, 10, 58, 0, 0);
         dateAsString = "01.09.2009";
         dateTimeAsString = dateAsString + " 10:58:00";
 
         dateAsStringShort = "09/2009";
         shortPattern = "MM/yyyy";
     }
 
 
     @Test
     public void formatsDateTimeCorrectly() throws Exception {
 
         editor.setValue(date);
         assertEquals(dateTimeAsString, editor.getAsText());
     }
 
 
     @Test
     public void parsesDateTimeCorrectly() throws Exception {
 
         editor.setAsText(dateTimeAsString);
         assertEquals(date, editor.getValue());
     }
 
 
     @Test
     public void parsesDateMidnightCorrectly() throws Exception {
 
         editor.forDateMidnight();
         editor.setAsText(dateTimeAsString);
         assertEquals(date.toDateMidnight(), editor.getValue());
     }
 
 
     @Test
     public void usesNullOnInvalidParsing() {
 
         editor.setAsText(dateAsStringShort);
         assertNull(editor.getValue());
     }
 
 
     @Test
     public void usesAdditionalPatternsForParsing() throws ParseException {
 
         editor.withAdditionalParsersFor(shortPattern);
         editor.setAsText(dateAsStringShort);
 
         assertEquals(date.toDateMidnight(), editor.getValue());
     }
 
 
     @Test
     public void parsesTwoDigitYearsCorrectly() throws Exception {
 
         assertDateForParsers(new DateMidnight(1910, 1, 12), "12.01.1910", "dd.MM.yy", "dd.MM.yyyy");
 
         assertDateForParsers(new DateMidnight(2029, 12, 31), "31.12.29", "dd.MM.yy");
        assertDateForParsers(new DateMidnight(1930, 1, 1), "01.01.30", "dd.MM.yy");
     }
 
 
     @Test
     public void prefersAdditionalParsersIfConfigured() throws Exception {
 
         DateTimeEditor editor = new DateTimeEditor(Locale.GERMAN, "dd.MM.yyyy").forDateMidnight();
         editor.withAdditionalParsersFor("dd.MM.yy");
         editor.preferAdditionalPatternsForParsing(true).setAsText("12.01.10");
 
         assertThat((DateMidnight) editor.getValue(), is(new DateMidnight(2010, 1, 12)));
     }
 
 
     private void assertDateForParsers(DateMidnight date, String dateAsString, String pattern,
            String... additionalPatterns) {
 
         DateTimeEditor editor = new DateTimeEditor(Locale.GERMAN, pattern).forDateMidnight();
         editor.withAdditionalParsersFor(additionalPatterns).setAsText(dateAsString);
 
         assertThat((DateMidnight) editor.getValue(), is(date));
     }
 }
