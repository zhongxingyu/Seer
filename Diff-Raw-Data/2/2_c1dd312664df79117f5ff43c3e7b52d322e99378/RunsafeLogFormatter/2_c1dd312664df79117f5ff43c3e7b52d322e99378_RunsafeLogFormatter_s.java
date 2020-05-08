 package no.runsafe.framework.internal;
 
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 
 import java.util.logging.LogRecord;
 import java.util.logging.SimpleFormatter;
 
 public class RunsafeLogFormatter extends SimpleFormatter
 {
 	public RunsafeLogFormatter(String format)
 	{
 		logFormat = format + "\n";
 	}
 
 	@Override
 	public synchronized String format(LogRecord record)
 	{
 		String message = formatMessage(record);
 		return String.format(
 			logFormat,
 			datestamp.print(record.getMillis()),
 			timestamp.print(record.getMillis()),
 			record.getLevel().getName(),
 			message
 		);
 	}
 
 	private final DateTimeFormatter datestamp = new DateTimeFormatterBuilder()
		.appendYear(4,4).appendLiteral('-').appendMonthOfYear(2).appendDayOfMonth(2)
 		.toFormatter();
 	private final DateTimeFormatter timestamp = new DateTimeFormatterBuilder()
 		.appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2)
 		.toFormatter();
 	private final String logFormat;
 }
