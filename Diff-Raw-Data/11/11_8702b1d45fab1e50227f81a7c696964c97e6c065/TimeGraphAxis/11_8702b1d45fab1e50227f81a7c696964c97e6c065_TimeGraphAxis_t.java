 package org.bodytrack.client;
 
 import java.util.Date;
 
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.i18n.client.TimeZone;
 import com.google.gwt.user.client.Window;
 
 @SuppressWarnings("deprecation")
 public class TimeGraphAxis extends GraphAxis implements TimestampFormatter {
 
 	public TimeGraphAxis(String divName, double min, double max, Basis basis,
 			double width, boolean isXAxis) {
 		super(divName, min, max, basis, width, isXAxis);
 		minRange = -2147483640;
 		maxRange = 2147483640;
 		hasMinRange = hasMaxRange = true;
 	}
 	
 	private TimeZoneSegmentWrapper[] timeZones = {};
 
 	private final long secondsInHour = 3600;
 	private final long secondsInDay = secondsInHour * 24;
 	private final long secondsInWeek = secondsInDay * 7;
 	private final long secondsInYear = 31556926;
 	private final long secondsInMonth = (long)Math.round(secondsInYear / 12.);
 
 	private final double timeTickSizes[][] = {
 			{1, 5, 15, 30},                                            // 1, 5, 15, 30 seconds
 			{60*1, 60*5, 60*15, 60*30},                                // 1, 5, 15, 30 mins
 			{3600*1, 3600*3, 3600*6, 3600*12},                         // 1, 3, 6, 12 hours
 			{secondsInDay},                                            // 1 day
 			{secondsInWeek},                                           // 1 weeks
 			{secondsInMonth, secondsInMonth * 3, secondsInMonth * 6},  // 1, 3, 6 months
 			{secondsInYear}                                            // 1 year
 	};
 
 	private int previousPaintEventId = 0;
 
 	private double computeTimeTickSize(double minPixels) {
 		//double minDelta = Math.max(minTickSize,
 		// (this.max - this.min) * (minPixels / this.length));
 		double minDelta = (this.max - this.min) * (minPixels / this.length);
 
 		if (minDelta < 1)
 			return computeTickSize(minPixels);
 
 		for (int unit = 0; unit < timeTickSizes.length; unit++)
 			for (int i = 0; i < timeTickSizes[unit].length; i++)
 				if (timeTickSizes[unit][i] >= minDelta)
 					return timeTickSizes[unit][i];
 
 		return computeTickSize(minPixels, secondsInYear) * secondsInYear;
 	}
 
 	// Like computeTimeTickSize, but constrain the minimum size of the tick like so:
 	// Select minorTickSize to be of the same units as majorTickSize, unless
 	//   majorTickSize is the minimum value of the unit (e.g. 1 second, 1 minute,
 	//   1 hour), in which case select minorTickSize from the next smaller unit
 	private double computeTimeMinorTickSize(double minPixels, double majorTickSize) {
 		// Find unit matching majorTickSize
 		double epsilon = 1e-10;
 		if (majorTickSize <= 1 + epsilon) return computeTickSize(minPixels);
 		for (int unit = 0; unit < timeTickSizes.length; unit++) {
 			for (int i = 0; i < timeTickSizes[unit].length; i++) {
 				if (majorTickSize <= timeTickSizes[unit][i]) {
 					double minTickSize;
 					if (i == 0) {
 						// Major tick is minimum value of unit;  OK to use
 						// next lower unit
 						minTickSize = timeTickSizes[unit-1][0];
 					} else {
 						// Don't go smaller than major tick's unit
 						minTickSize = timeTickSizes[unit][0];
 					}
 					return Math.max(minTickSize, computeTimeTickSize(minPixels));
 				}
 			}
 		}
 		return computeTimeTickSize(minPixels);
 	}
 
 	private class TimeLabelFormatter extends LabelFormatter {
 		String format(double time) {
 			// Compute time, rounded to nearest microsecond, then truncated
 			// to second only
 			double whole = Math.floor(time + (.5/1000000.));
 			// Compute fractional second in microseconds, rounded to nearest
 			int microseconds = (int) Math.round(1000000 * (time - whole));
 
 			Date d = new Date((long) (adjustTimestamp(whole)*1000));
 			
 			String ret = NumberFormat.getFormat("00").format(d.getHours())
 				+ ":" +	NumberFormat.getFormat("00").format(d.getMinutes());
 			
 			int seconds = d.getSeconds();
 			if (seconds != 0 || microseconds != 0) {
 				ret += ":" + NumberFormat.getFormat("00").format(seconds);
 				if (microseconds != 0) {
 					ret += "."
 						+ NumberFormat.getFormat("000000").format(microseconds);
 					// Remove trailing zeros
 					ret = ret.replaceFirst("0+$", "");
 				}
 			}
 
 			return ret;
 		}
 	}
 
 	private class DayLabelFormatter extends LabelFormatter {
 		final String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
 		String format(double time) {
 			Date d = new Date((long) Math.round(adjustTimestamp(time)*1000));
 			String ret = days[d.getDay()] + " " + d.getDate();
 			return ret;
 		}
 	}	
 
 	private class MonthLabelFormatter extends LabelFormatter {
 		final String[] months = {"Jan", "Feb", "Mar", "Apr", "May",
 				"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
 		String format(double time) {
 			Date d = new Date((long) Math.round(adjustTimestamp(time) * 1000));
 			return months[d.getMonth()];
 		}
 	}	
 
 	private class YearLabelFormatter extends LabelFormatter {
 		String format(double time) {
 			Date d = new Date((long) Math.round(adjustTimestamp(time) * 1000));
 			return String.valueOf(d.getYear() + 1900);
 		}
 	}	
 
 	private TickGenerator createDateTickGenerator(double tickSize) {
 		int nHours = (int) Math.round(tickSize / secondsInHour);
 		if (nHours <= 1) return null;
 		if (nHours < 24) return new HourTickGenerator(nHours);
 		int nDays = (int) Math.round(tickSize / secondsInDay);
 		if (nDays == 1) return new DayTickGenerator();
 		int nWeeks = (int) Math.round(tickSize / secondsInWeek);
 		if (nWeeks == 1) return new WeekTickGenerator();
 		int nMonths = (int) Math.round(tickSize / secondsInMonth);
 		if (nMonths < 12) return new MonthTickGenerator(nMonths);
 		int nYears = (int) Math.round(tickSize / secondsInYear);
 
 		return new YearTickGenerator(nYears);
 	}
 
 	private double closestDay(double time) {
 		Date timeDate = new Date(((long)time) * 1000);
 		double hour = timeDate.getHours() + timeDate.getMinutes() / 30.0
 			+ timeDate.getSeconds() / 1800.;
 		if (hour >= 12) {
 			// Advance day by moving to one min before midnight
 			timeDate.setHours(23);
 			timeDate.setMinutes(59);
 			timeDate.setSeconds(59);
 			// Advance 12 hours
 			timeDate.setTime(timeDate.getTime() + secondsInHour * 12 * 1000);
 		}
 		
 		// Truncate to beginning of day
 		timeDate.setHours(0);
 		timeDate.setMinutes(0);
 		timeDate.setSeconds(0);
 		double epsilon = 1e-10;
 		// Return time in seconds, truncating fractional second
 		double ret=Math.floor(timeDate.getTime() / 1000 + epsilon);
 		return adjustTickTimestamp(ret);			
 	}
 
 	private class YearTickGenerator extends MonthTickGenerator {
 		YearTickGenerator(int tickSizeYears) {
 			super(12 * tickSizeYears);
 		}
 	}
 
 	private class MonthTickGenerator extends TickGenerator {
 		private int tickSizeMonths;
 
 		MonthTickGenerator(int tickSizeMonths) {
 			super(secondsInMonth * tickSizeMonths, 0);
 			this.tickSizeMonths = tickSizeMonths;
 		}
 
 		private int divideFloor(int numerator, int divisor) {
 			if (numerator < 0) {
 				return -(((Math.abs(divisor) - numerator - 1)) / divisor);
 			} else {
 				return numerator / divisor;
 			}
 		}
 
 		double closestTick(double time) {
 			Date timeDate = new Date((long) (time*1000));
 
 			double monthsSince1970 = timeDate.getYear() * 12
 			+ timeDate.getMonth()
 			+ (timeDate.getDate() * secondsInDay / secondsInMonth);
 
 			int tickMonthsSince1970 =
 				(int) Math.round(monthsSince1970 / tickSizeMonths)
 				* tickSizeMonths;
 
 			int tickYear = divideFloor(tickMonthsSince1970, 12);
 			int tickMonth = tickMonthsSince1970 - tickYear * 12; 
 			Date tickDate = new Date(tickYear, tickMonth, 1);
 
 			return adjustTickTimestamp(Math.round(tickDate.getTime() / 1000));
 		}
 	}
 
 	private class WeekTickGenerator extends TickGenerator {
 		WeekTickGenerator() {
 			super(secondsInWeek, 0);
 		}
 
 		double closestTick(double time) {
 			Date timeDate = new Date((long) (time*1000));
 
 			double day = ((60 * (60 * timeDate.getSeconds()) + timeDate.getMinutes())
 					+ timeDate.getHours()) / 24.;
 			int daysSinceMonday = timeDate.getDay() - 1;
 
 			if (daysSinceMonday < 0)
 				daysSinceMonday += 7;
 
 			day += daysSinceMonday;
 
 			if (day >= 3.5) {
 				return closestDay(time + secondsInDay * (7 - day));
 			} else {
 				return closestDay(time - secondsInDay * day);
 			}
 		}
 	}
 
 	private class DayTickGenerator extends TickGenerator {
 		DayTickGenerator() {
 			super(secondsInDay, 0);
 		}
 
 		double closestTick(double time) {
 			return closestDay(time);
 		}		
 	}
 
 	private class HourTickGenerator extends TickGenerator {
 		private int tickSizeHours;
 		HourTickGenerator(int tickSizeHours) {
 			super(tickSizeHours * secondsInHour, 0);
 			this.tickSizeHours = tickSizeHours;
 		}
 		double closestTick(double time) {
 			Date timeDate = new Date((long) (time * 1000));
 			double hour = timeDate.getHours() + timeDate.getMinutes() / 30.0
 			+ timeDate.getSeconds() / 1800.0;
 
 			int closestHour =
 				(int) Math.round(hour / tickSizeHours) * tickSizeHours;
 			if (closestHour == 24) {
 				// Midnight of next day.  Advance time and return closest
 				// beginning of day
 				return closestDay(time + (24-hour) * secondsInHour);
 			} else {
 				timeDate.setHours(closestHour);
 			}
 
 			// Remove minutes and seconds
 			timeDate.setMinutes(0);
 			timeDate.setSeconds(0);
 			double epsilon = 1e-10;
 			// Return time in seconds, truncating fractional second
 			return adjustTickTimestamp(Math.floor(timeDate.getTime() / 1000 + epsilon));
 		}
 	}
 
 	@Override
 	public void paint(final int newPaintEventId) {
 		// guard against redundant paints
 		if (previousPaintEventId != newPaintEventId) {
 			previousPaintEventId = newPaintEventId;
 
 			paint();
 		}
 	}
 
 	private void paint() {
 		Canvas canvas = getDrawingCanvas();
 		if (canvas == null)
 			return;
 
 		canvas.clear();
 
 		// Pick the color to use, based on highlighting status
 		if (isHighlighted())
 			canvas.setStrokeStyle(HIGHLIGHTED_COLOR);
 		else
 			canvas.setStrokeStyle(NORMAL_COLOR);
 
 		canvas.beginPath();
 		canvas.drawLineSegment(project2D(this.min), project2D(this.max));
 		double epsilon = 1e-10;
 
 		// Year out of line
 		// Year inline
 		// Month out of line ; Year inline 
 		// Month inline ; Year inline 
 		// DOW DOM out of line ; Month inline ; Year inline
 		// DOW DOM inline ; Month inline ; Year inline
 		// HH:MM[:SS[.NNN]] on tick ; DOW DOM inline ;  Month inline ; Year inline 
 
 		double pixelOffset = 0;
 
 		double timeMajorPixels = 50;
 		double timeMajorNoLabelPixels = 10;
 		double timeMinorPixels = 5;
 		double timeMajorTickSize = computeTimeTickSize(timeMajorPixels);
 		double timeMajorNoLabelTickSize =
 			computeTimeTickSize(timeMajorNoLabelPixels);
 
 		if (timeMajorNoLabelTickSize <= 3600*12 + epsilon) {
 			double timeLabelHeight;
 			if (timeMajorTickSize <= 3600*12 + epsilon) {
 				renderTicks(pixelOffset, timeMajorTickSize,
 						createDateTickGenerator(timeMajorTickSize), canvas,
 						majorTickWidthPixels, new TimeLabelFormatter());
 				timeLabelHeight = 10;
 			} else {
 				timeMajorTickSize = 3600*12;
 				renderTicks(pixelOffset, timeMajorTickSize,
 						createDateTickGenerator(timeMajorTickSize), canvas,
 						majorTickWidthPixels, null);
 				timeLabelHeight = 0;
 			}
 
 			double timeMinorTickSize =
 				computeTimeMinorTickSize(timeMinorPixels, timeMajorTickSize);
 
 			//double timeMinorTickSize = computeTimeTickSize(timeMinorPixels);
 
 			renderTicks(pixelOffset, timeMinorTickSize,
 					createDateTickGenerator(timeMinorTickSize), canvas,
 					minorTickWidthPixels, null);
 			pixelOffset += 12 + timeLabelHeight;
 		}
 
 		double inlineTickWidthPixels = 15;
 
 		double dayMajorPixels = 50;
 		double dayMinorPixels = 7;
 		double dayMajorTickSize = Math.max(secondsInDay,
 				computeTimeTickSize(dayMajorPixels));
 		double dayMinorTickSize = Math.max(secondsInDay,
 				computeTimeTickSize(dayMinorPixels));
 
 		if (dayMajorTickSize == secondsInDay) {
 			renderTicksRangeLabelInline(pixelOffset, dayMajorTickSize,
 					createDateTickGenerator(dayMajorTickSize), canvas,
 					inlineTickWidthPixels, new DayLabelFormatter());
 			renderTicks(pixelOffset, dayMinorTickSize,
 					createDateTickGenerator(dayMinorTickSize), canvas,
 					minorTickWidthPixels, null);
 			pixelOffset += inlineTickWidthPixels;
 		} else if (dayMajorTickSize == secondsInWeek) {
 			renderTicksRangeLabel(pixelOffset, dayMajorTickSize,
 					createDateTickGenerator(dayMajorTickSize),
 					createDateTickGenerator(secondsInDay), canvas,
 					majorTickWidthPixels, new DayLabelFormatter());
 			renderTicks(pixelOffset, dayMinorTickSize,
 					createDateTickGenerator(dayMinorTickSize), canvas,
 					minorTickWidthPixels, null);
 			pixelOffset += 22;
 		}
 
 		double monthPixels = 30;
 		double monthTickSize =
 			Math.max(secondsInMonth, computeTimeTickSize(monthPixels));
 
 		if (monthTickSize == secondsInMonth) {
 			renderTicksRangeLabelInline(pixelOffset, monthTickSize,
 					createDateTickGenerator(monthTickSize), canvas,
 					inlineTickWidthPixels, new MonthLabelFormatter());
 			pixelOffset += inlineTickWidthPixels;
 		} else if (monthTickSize < secondsInYear - epsilon) {
 			renderTicksRangeLabel(pixelOffset, monthTickSize,
 					createDateTickGenerator(monthTickSize),
 					createDateTickGenerator(secondsInMonth), canvas,
 					majorTickWidthPixels, new MonthLabelFormatter());
 			pixelOffset += 22;			
 		}
 
 		double yearPixels = 40;
 		double yearTickSize =
 			Math.max(secondsInYear, computeTimeTickSize(yearPixels));
 		if (yearTickSize == secondsInYear) {
 			renderTicksRangeLabelInline(pixelOffset, yearTickSize,
 					createDateTickGenerator(yearTickSize), canvas,
 					inlineTickWidthPixels, new YearLabelFormatter());
 			pixelOffset += inlineTickWidthPixels;
 		} else {
 			renderTicksRangeLabel(pixelOffset, yearTickSize,
 					createDateTickGenerator(yearTickSize),
 					createDateTickGenerator(secondsInYear), canvas,
 					majorTickWidthPixels, new YearLabelFormatter());
 			pixelOffset += 22;			
 		}
 
 		canvas.stroke();
 
 		renderHighlight(canvas, getHighlightedPoint());
 
 		// Clean up after ourselves
 		canvas.setStrokeStyle(Canvas.DEFAULT_COLOR);
 	}
 	
 	@Override
 	public void setTimeZoneMapping(TimeZoneMapping mapping) {
 		if (mapping != null)
 			timeZones = mapping.getTimeZones();
 		else
 			timeZones = new TimeZoneSegmentWrapper[]{};
 		paint();
 	}
 
 	@Override
 	public String formatTimestamp(double timestamp) {
 		return PlottablePoint.DATE_TIME_FORMAT.format(new Date((long) adjustTimestamp(timestamp) * 1000));
 	}
 	
 	private double adjustTimestamp(double timestamp) {
 		int additionalOffset = new Date((long) (timestamp * 1000)).getTimezoneOffset() * 60;
 		return timestamp + additionalOffset + getOffsetAtTimestamp(timestamp);
 	}
 	
 	private double adjustTickTimestamp(double timestamp) {
 		int additionalOffset = new Date((long) (timestamp * 1000)).getTimezoneOffset() * 60;
 		return timestamp - additionalOffset - getOffsetAtTimestamp(timestamp);
 	}
 	
 	private double getOffsetAtTimestamp(double timestamp) {
		TimeZoneSegmentWrapper prevSegment = null;
		for (TimeZoneSegmentWrapper segment: timeZones) {
			if (segment.getStart() > timestamp)
				break;
			prevSegment = segment;
		}

		if (prevSegment != null)
			return prevSegment.getTimeZone().getOffset(new Date((long) (timestamp * 1000))) * -60;

		// Default to the current timezone if all timezone segments start after timestamp
 		return new Date((long) (timestamp * 1000)).getTimezoneOffset() * -60;
 	}
 }
