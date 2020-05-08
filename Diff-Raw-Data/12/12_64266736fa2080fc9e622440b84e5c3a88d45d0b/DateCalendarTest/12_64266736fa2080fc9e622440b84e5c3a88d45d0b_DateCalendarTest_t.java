 /*
  * Copyright 2011 Matthew Avery, mavery@advancedpwr.com
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
 package com.advancedpwr.record;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import com.advancedpwr.record.examples.Person;
 
 public class DateCalendarTest extends AbstractRecorderTest
 {
 
 
 	protected void setUp()
 	{
 		setWriteFiles();
 		super.setUp();
 	}
 
 	public void testRecordPerson() throws Exception
 	{
 		Person person = Person.createExamplePerson();
 		Person dad = person.getDad();
 		Calendar calendar = GregorianCalendar.getInstance();
 		calendar.setTimeInMillis( 123456789 );
 		dad.setBirthday( calendar );
 		dad.setAnniversary( new Date( 987654321 ) );
 
 		recorder.record( person );
     assertContains("	protected Date date_2_2;\n" +
     				"\n" +
     				"	protected Date buildDate_2_2()\n" +
     				"	{\n" +
     				"		date_2_2 = new Date( 987654321l );\n" +
     				"		return date_2_2;\n" +
     				"	}\n" +
     				"\n" +
     				"	protected Calendar gregoriancalendar_10_2;\n" +
     				"\n" +
     				"	protected Calendar buildGregorianCalendar_10_2()\n" +
     				"	{\n" +
     				"		gregoriancalendar_10_2 = GregorianCalendar.getInstance();\n" +
    				"		gregoriancalendar_10_2.setTimeInMillis( new Long( 123456789l ) );\n" +
     				"		return gregoriancalendar_10_2;\n" +
     				"	}\n"
     );
 	}
 }
