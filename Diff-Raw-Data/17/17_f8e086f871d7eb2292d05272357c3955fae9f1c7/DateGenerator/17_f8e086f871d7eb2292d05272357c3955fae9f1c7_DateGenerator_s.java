 package com.narmnevis.range.generator;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import com.narmnevis.range.Generator;
 import com.narmnevis.range.RangeContext;
 
 public class DateGenerator extends AbstractGenerator implements Generator {
 
 	private static final long ONE_YEAR = TimeUnit.DAYS.toMillis(365);
	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 
 	private final Date start;
	private List<Double> samples;
 
 	public DateGenerator() {
 		this.start = new Date(System.currentTimeMillis() - ONE_YEAR);
 	}
 
 	@Override
 	public Object generate(RangeContext context) {
		if (samples == null) {
			samples = generateGaussianRandoms(context);
		}
 		try {
			Double d = samples.remove(0);
 			long time = start.getTime() + new Double(ONE_YEAR * d).longValue();
			return FORMAT.format(new Date(time));
 		} catch (Exception e) {
 			logger.error("Failed to generate a date: {}", e);
 		}
 		return null;
 	}
 
 }
