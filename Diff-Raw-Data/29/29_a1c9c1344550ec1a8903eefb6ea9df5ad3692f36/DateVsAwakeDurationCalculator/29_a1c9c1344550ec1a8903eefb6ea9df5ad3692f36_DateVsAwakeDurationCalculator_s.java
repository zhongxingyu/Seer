 package com.aldaviva.dreamstats.stats;
 
 import com.aldaviva.dreamstats.data.dto.axis.Axis;
 import com.aldaviva.dreamstats.data.dto.axis.DurationAxis;
 import com.aldaviva.dreamstats.data.dto.axis.LocalDateAxis;
 import com.aldaviva.dreamstats.data.dto.table.StatsTable;
 import com.aldaviva.dreamstats.data.enums.EventName;
 import com.aldaviva.dreamstats.data.model.CalendarEvent;
 
 import java.util.List;
 
 import org.joda.time.Duration;
 import org.joda.time.LocalDate;
 import org.springframework.stereotype.Component;
 
 import com.google.common.base.Predicate;
 
 @Component
 public class DateVsAwakeDurationCalculator extends BaseStatsCalculator<LocalDate, Duration> {
 
 	@Override
 	protected void calculateStats(final StatsTable<LocalDate, Duration> results) {
 		final List<CalendarEvent> events = calendarService.findEvents(new Predicate<CalendarEvent>() {
 			@Override
 			public boolean apply(final CalendarEvent input) {
 				return EventName.Sleep.equals(input.getName());
 			}
 		});
 
 		CalendarEvent previousSleepEvent = null;
 		for (final CalendarEvent event : events) {
 			if(previousSleepEvent != null){
				final LocalDate independentExact = new LocalDate(event.getEnd());
				final Duration dependentExact = new Duration(previousSleepEvent.getEnd(), event.getStart());
				incrementTableBucket(results, independentExact, dependentExact);
 			}
 			previousSleepEvent = event;
 		}
 	}
 
 	@Override
 	protected LocalDate getIndependentBucket(final LocalDate exact) {
 		return exact;
 	}
 
 	@Override
 	protected Duration getDependentBucket(final Duration exact) {
 		return bucketizeDuration(exact);
 	}
 
 	@Override
 	protected Axis<LocalDate> getIndependentAxis() {
 		return new LocalDateAxis("date", "Date");
 	}
 
 	@Override
 	protected Axis<Duration> getDependentAxis() {
 		return new DurationAxis("awake-duration", "Awake Duration", DEFAULT_DURATION_INTERVAL);
 	}
 
 }
