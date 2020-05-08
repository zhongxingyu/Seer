 package com.aldaviva.dreamstats.stats;
 
 import com.aldaviva.dreamstats.data.dto.axis.Axis;
 import com.aldaviva.dreamstats.data.dto.axis.DurationAxis;
 import com.aldaviva.dreamstats.data.dto.axis.SleepDurationAxis;
 import com.aldaviva.dreamstats.data.dto.table.StatsTable;
 import com.aldaviva.dreamstats.data.enums.EventName;
 import com.aldaviva.dreamstats.data.model.CalendarEvent;
 
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.springframework.stereotype.Component;
 
 import com.google.common.base.Predicate;
 
 @Component
 public class DurationSinceEatingVsSleepDuration extends BaseStatsCalculator<Duration, Duration> {
 
 	@Override
 	protected void calculateStats(final StatsTable<Duration, Duration> results) {
 		final List<CalendarEvent> events = calendarService.findEvents(new Predicate<CalendarEvent>() {
 			@Override
 			public boolean apply(final CalendarEvent input) {
 				final EventName name = input.getName();
 				return EventName.Sleep.equals(name) || EventName.Food.equals(name);
 			}
 		});
 
 		DateTime mostRecentFoodEnd = null;
 
 		for (final CalendarEvent event : events) {
 			if(EventName.Food.equals(event.getName())){
 				mostRecentFoodEnd = event.getEnd();
 
 			} else if(EventName.Sleep.equals(event.getName())){
 				if(mostRecentFoodEnd != null){
 					final Duration timeBetweenFoodEndingAndSleepStarting = new Duration(mostRecentFoodEnd, event.getStart());
					incrementTableBucket(results, timeBetweenFoodEndingAndSleepStarting, event.getDuration());
 				}
 			}
 		}
 	}
 
 	@Override
 	protected Duration getIndependentBucket(final Duration exact) {
 		return bucketizeDuration(exact);
 	}
 
 	@Override
 	protected Duration getDependentBucket(final Duration exact) {
 		return bucketizeDuration(exact);
 	}
 
 	@Override
 	protected Axis<Duration> getIndependentAxis() {
 		return new DurationAxis("duration-since-eating", "Duration Since Eating", DEFAULT_DURATION_INTERVAL);
 	}
 
 	@Override
 	protected Axis<Duration> getDependentAxis() {
 		return new SleepDurationAxis();
 	}
 
 }
