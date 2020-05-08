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
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.ISOPeriodFormat;
 import org.joda.time.format.PeriodFormatter;
 
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.metabroadcast.common.time.SystemClock;
 
 public class DateTimeInQueryParser {
 
 	private final static Pattern EXPRESSION = Pattern.compile("now(?:\\.(plus|minus)\\.((?:P)?[a-zA-Z0-9]+)|)");
 
 	private final PeriodFormatter isoParser = ISOPeriodFormat.standard();
 	private final Clock clock;
 
 	public DateTimeInQueryParser(Clock clock) {
 		this.clock = clock;
 	}
 	
 	public DateTimeInQueryParser() {
 		this(new SystemClock());
 	}
 	
 	public DateTime parse(String value) throws MalformedDateTimeException {
 		
 		if (!StringUtils.isBlank(value) && StringUtils.isNumeric(value)) {
			return new DateTime(Long.valueOf(value), DateTimeZones.UTC);
 		}
 		
 		Matcher matcher = EXPRESSION.matcher(value);
 		if (matcher.matches()) {
 			String operator = matcher.group(1);
 			if (operator == null) {
 				return clock.now();
 			}
 			else  {
 				Period period = periodFrom(matcher.group(2));
 				if ("plus".equals(operator)) {
 					return clock.now().plus(period);
 				} else {
 					return clock.now().minus(period);
 				}
 			}
 		}
 		
 		throw new MalformedDateTimeException();
 	}
 	
 	private Period periodFrom(String period) {
 		period = period.toUpperCase();
 		try {
 			if (!period.startsWith("P")) {
 				period = "PT" + period;
 			}
 			return isoParser.parsePeriod(period);
 		} catch (IllegalArgumentException e) {
 			throw new MalformedDateTimeException(e);
 		}
 	}
 
 	public static class MalformedDateTimeException extends IllegalArgumentException {
 
 		private static final long serialVersionUID = 1L;
 		
 		public MalformedDateTimeException() {
 			super("DateTime not in a recognised format");
 		}
 
 		public MalformedDateTimeException(Exception e) {
 			super("DateTime not in a recognised format", e);
 		}
 	}
 }
