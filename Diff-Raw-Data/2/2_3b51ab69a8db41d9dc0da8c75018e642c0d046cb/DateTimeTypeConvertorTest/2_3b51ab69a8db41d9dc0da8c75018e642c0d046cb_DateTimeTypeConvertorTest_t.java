 /*
  * This file is a component of thundr, a software library from 3wks.
  * Read more: http://3wks.github.io/thundr/
  * Copyright (C) 2014 3wks, <thundr@3wks.com.au>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.threewks.thundr.json;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Test;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 public class DateTimeTypeConvertorTest {
 
 	@Test
 	public void shouldSerializeDateTime() {
 		DateTimeTypeConvertor dateTimeTypeConvertor = new DateTimeTypeConvertor();
 		DateTime srcInUtc = new DateTime(2010, 1, 1, 12, 0, 0, 0).withZoneRetainFields(DateTimeZone.UTC);
 		assertThat(dateTimeTypeConvertor.serialize(srcInUtc, null, null).toString(), is("\"2010-01-01T12:00:00.000Z\""));
 
 		DateTime srcInSydney = new DateTime(2010, 1, 1, 12, 0, 0, 0).withZoneRetainFields(DateTimeZone.forID("Australia/Sydney"));
 		assertThat(dateTimeTypeConvertor.serialize(srcInSydney, null, null).toString(), is("\"2010-01-01T12:00:00.000+11:00\""));
 	}
 
 	@Test
 	public void shouldDeserializeDateTime() {
 		GsonBuilder builder = new GsonBuilder();
 		builder.registerTypeAdapter(DateTime.class, new DateTimeTypeConvertor());
 		Gson gson = builder.create();
 
 		DateTime result = gson.fromJson("\"2010-01-01T12:00:00.000+11:00\"", DateTime.class);
 
 		DateTime srcInSydney = new DateTime(2010, 1, 1, 12, 0, 0, 0).withZoneRetainFields(DateTimeZone.forID("Australia/Sydney"));
		assertThat(result.compareTo(srcInSydney), is(0));
 	}
 }
