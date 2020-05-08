 /* Copyright 2010 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.query.content.parser;
 
 import junit.framework.TestCase;
 
 import org.atlasapi.query.content.parser.DateTimeInQueryParser;
 import org.atlasapi.query.content.parser.DateTimeInQueryParser.MalformedDateTimeException;
 import org.joda.time.DateTime;
 
 import com.metabroadcast.common.time.Clock;
import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.TimeMachine;
 
 public class DateTimeInQueryParserTest extends TestCase {
 
 	public void testTheParser() throws Exception {
 		DateTime now = new DateTime();
 
 		Clock clock = new TimeMachine(now);
 		
 		DateTimeInQueryParser parser = new DateTimeInQueryParser(clock);
 		
		assertEquals(new DateTime(2009, 2, 13, 23, 31, 30, 0, DateTimeZones.UTC), parser.parse("1234567890"));
 
 		assertEquals(parser.parse("now"), now);
 
 		assertEquals(parser.parse("now.plus.1h"), now.plusHours(1));
 		assertEquals(parser.parse("now.plus.PT1H"), now.plusHours(1));
 
 		assertEquals(parser.parse("now.minus.1h"), now.minusHours(1));
 
 		assertEquals(parser.parse("now.minus.1s"), now.minusSeconds(1));
 
 		assertEquals(parser.parse("now.minus.1m"), now.minusMinutes(1));
 
 		assertEquals(parser.parse("now.minus.PT1M"), now.minusMinutes(1));
 		
 		assertEquals(parser.parse("now.minus.P1M"), now.minusMonths(1));
 
 		assertIsMalformed(parser, "");
 		assertIsMalformed(parser, "-");
 		assertIsMalformed(parser, "+");
 		assertIsMalformed(parser, "now.plus");
 		assertIsMalformed(parser, "now.plus.bob");
 		assertIsMalformed(parser, "now.plus.bob10");
 	}
 	
 	private void assertIsMalformed(DateTimeInQueryParser parser, String value) {
 		try {
 			parser.parse(value);
 			fail("Expected " + MalformedDateTimeException.class.getSimpleName());
 		}
 		catch (MalformedDateTimeException e) {
 			// expected
 		}
 	}
 }
