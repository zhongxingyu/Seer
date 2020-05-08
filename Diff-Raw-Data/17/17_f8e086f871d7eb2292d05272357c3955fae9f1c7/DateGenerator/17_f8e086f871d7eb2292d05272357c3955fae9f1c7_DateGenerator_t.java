 package com.narmnevis.range.generator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.TimeUnit;
 
 import com.narmnevis.range.Generator;
 import com.narmnevis.range.RangeContext;
 
 public class DateGenerator extends AbstractGenerator implements Generator {
 
 	private static final long ONE_YEAR = TimeUnit.DAYS.toMillis(365);
 
 	private final Date start;
 
 	public DateGenerator() {
 		this.start = new Date(System.currentTimeMillis() - ONE_YEAR);
 	}
 
 	@Override
 	public Object generate(RangeContext context) {
 		try {
			// Costly but thread-safe
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Double d = getNextRandom(context);
 			long time = start.getTime() + new Double(ONE_YEAR * d).longValue();
			return format.format(new Date(time));
 		} catch (Exception e) {
 			logger.error("Failed to generate a date: {}", e);
 		}
 		return null;
 	}
 
 }
