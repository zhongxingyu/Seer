 package main;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import data.Regions;
 
 public class DataLoader {
 
 	public enum TimeSlice {
 		Day, Hour, Half_Hour, Quarter_Hour
 	}
 
 	/**
 	 * Defines a start / end year to filter by. Both values are inclusive, e.g.
 	 * if start is 2001, then year 2001 is included in the results
 	 * 
 	 */
 	public static class YearFilter {
 		int startYear;
 		int endYear;
 	}
 
 	/**
 	 * Defines a start / end month to filter by. Both values are inclusive, e.g.
 	 * if start is Jan, then month Jan is included in the results. The values
 	 * should be retrieved from {@link java.util.Calendar}
 	 * 
 	 */
 	public static class MonthFilter {
 		int startMonth;
 		int endMonth;
 	}
 
 	/**
 	 * Defines a start / end day to filter by. Both values are inclusive, e.g.
 	 * if start is Monday, then day Monday is included in the results. The
 	 * values should be retrieved from {@link java.util.Calendar}
 	 * 
 	 */
 	public static class DayFilter {
 		int startDay;
 		int endDay;
 	}
 
 	/**
 	 * Defines a start / end hour to filter by. Both values are inclusive, e.g.
 	 * if start is 11, then hour 11 is included in the results. These values are
 	 * used for the {@link java.util.Calendar#HOUR_OF_DAY}, and should therefore
 	 * be in [0,1,...,22,23]
 	 * 
 	 */
 	public static class HourFilter {
 		int startHour;
 		int endHour;
 	}
 
 	private TimeSlice mSliceSize;
 	private GregorianCalendar mEndOfCurrentTimeSlice = null;
 	private HourFilter mHourFilter;
 	private GregorianCalendar mRangeStart;
 	private GregorianCalendar mRangeEnd;
 	private BufferedReader mInputFile;
 
 	private GregorianCalendar mTempCalendar = new GregorianCalendar();
 
 	private static final String dataFileName = "sorted_data_by_mysql.tsv";
 	private static final int xScaleFactor = 104; // (822600-817400)/50
 	private static final int xBase = 817400;
 	private static final int yScaleFactor = 70; // (441400-437900)/50
 	private static final int yBase = 437900;
 
 	/**
 	 * Note that the passed {@link HourFilter} is substantially different than
 	 * the year, month, or day filters. The year, month, or day filters set a
 	 * start and end range, which filters everything not in that range. The hour
 	 * filter, on the other hand, determines the hours that are allowed for each
 	 * day within the y/m/d range. For example, you cannot define 1PM on May 1st
 	 * 2011 to 7PM on May 7th 2011, as that is not how the start and end hours
 	 * work. You can specify the date range May 1 to May 7, and for every day in
 	 * that range the hours 1PM to 7PM will be allowed through the filters
 	 * 
 	 * @param sliceSlize
 	 *            Defines the length that each slice of time should be divided
 	 *            into. Uses even divisions, e.g. 0, 15, 30, 45, etc minutes.
 	 *            Additionally, starts at the first even division occurring
 	 *            before the first reading. For days, this is the start of the
 	 *            day that the first reading was entered on
 	 * @param yf
 	 * @param mf
 	 * @param df
 	 * @param hf
 	 */
 	@SuppressWarnings("hiding")
 	public DataLoader(TimeSlice sliceSlize, YearFilter yf, MonthFilter mf,
 			DayFilter df, HourFilter hf) {
 		mSliceSize = sliceSlize;
 
 		try {
 			mInputFile = new BufferedReader(new FileReader(dataFileName));
 			// mInputFile.readLine(); // toss out the header
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			System.err.println("There was no information in the file!");
 			e.printStackTrace();
 			return;
 		}
 
 		GregorianCalendar temp = new GregorianCalendar();
 
 		YearFilter yearFilter = null;
 		if (yf != null)
 			yearFilter = yf;
 		else {
 			yearFilter = new YearFilter();
 			yearFilter.startYear = temp.getMinimum(Calendar.YEAR);
 			yearFilter.endYear = temp.getMaximum(Calendar.YEAR);
 		}
 
 		MonthFilter monthFilter = null;
 		if (mf != null)
 			monthFilter = mf;
 		else {
 			monthFilter = new MonthFilter();
 			monthFilter.startMonth = temp.getMinimum(Calendar.MONTH);
 			monthFilter.endMonth = temp.getMaximum(Calendar.MONTH);
 		}
 
 		DayFilter dayFilter = null;
 		if (df != null)
 			dayFilter = df;
 		else {
 			dayFilter = new DayFilter();
 			dayFilter.startDay = temp.getMinimum(Calendar.DAY_OF_MONTH);
 			dayFilter.endDay = temp.getMaximum(Calendar.DAY_OF_MONTH);
 		}
 
 		mRangeStart = new GregorianCalendar(0, 0, 0, 0, 0, 0);
 		mRangeStart.set(Calendar.YEAR, yearFilter.startYear);
 		mRangeStart.set(Calendar.MONTH, monthFilter.startMonth);
 		mRangeStart.set(Calendar.DAY_OF_MONTH, dayFilter.startDay);
 
 		mRangeEnd = new GregorianCalendar(0, 0, 0, 0, 0, 0);
 		mRangeEnd.set(Calendar.YEAR, yearFilter.endYear);
 		mRangeEnd.set(Calendar.MONTH, monthFilter.endMonth);
 		mRangeEnd.set(Calendar.DAY_OF_MONTH, dayFilter.endDay);
 
 		if (hf != null)
 			mHourFilter = hf;
 		else {
 			mHourFilter = new HourFilter();
 			mHourFilter.startHour = temp.getMinimum(Calendar.HOUR_OF_DAY);
 			mHourFilter.endHour = temp.getMaximum(Calendar.HOUR_OF_DAY);
 		}
 
 		advanceInputFileToFirstLine();
 
 	}
 
 	/**
 	 * Skips the input buffer ahead by large ranges.
 	 */
 	private void advanceInputFileToFirstLine() {
 		int approxCharsPerLine = 60;
 		int numberOfLines = 100000;
 
 		while (true) {
 			try {
 				mInputFile.mark((approxCharsPerLine + 15) * numberOfLines);
 				mInputFile.skip((approxCharsPerLine) * numberOfLines);
 				mInputFile.readLine(); // Toss out the (potentially incomplete)
 				// line
 				String line = mInputFile.readLine();
 				if (line == null)
 					return;
 
 				long time = Long.parseLong(line.split("\t")[0]) * 1000;
 				mTempCalendar.setTimeInMillis(time);
 				//System.out.println("Checking "
 				//		+ mTempCalendar.getTime().toLocaleString());
				if (passesDayMonthYearTimeFilters(time)) {
 					mInputFile.reset();
 					return;
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 				return;
 			}
 		}
 	}
	
	private boolean passesDayMonthYearTimeFilters(long timeStamp) {
		mTempCalendar.setTimeInMillis(timeStamp);

		if (mTempCalendar.before(mRangeStart) || mTempCalendar.after(mRangeEnd))
			return false;

		return true;
	}
 
 	/**
 	 * Reads data from the file, adding pixels as we go
 	 * 
 	 * @param r
 	 * @return 0 if pixels were read and added, 1 if the end of the file was
 	 *         reached (whether or not pixels were actually added is
 	 *         unreported), -1 if there was an error (such as an IO exception)
 	 */
 	public int addPixels(Regions r) {
 		String nextLine = null;
 
 		while (true) {
 			// Read in next line
 			try {
 				mInputFile.mark(200);
 				nextLine = mInputFile.readLine();
 				if (nextLine == null) {
 					mInputFile.close();
 					return 1;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				return -1;
 			}
 
 			// Check filters
 			String[] values = nextLine.split("\t");
 			long timeStamp = Long.parseLong(values[0]) * 1000;
 			if (passesTimeFilters(timeStamp) == false) {
 				//System.out.println("Time of "
 				//		+ mTempCalendar.getTime().toLocaleString()
 				//		+ " didn't pass filters");
 				continue;
 			}
 
 			// Check current time slice
 			if (isInCurrentTimeSlice(timeStamp) == false) {
 				try {
 					mInputFile.reset();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				updateCurrentTimeSlice();
 				return 0;
 			}
 
 			// Add point :D
 			double x = Double.parseDouble(values[2]) - xBase;
 			double y = Double.parseDouble(values[3].trim()) - yBase;
 
 			int xScaled = (int) (x / xScaleFactor);
 			int yScaled = (int) (y / yScaleFactor);
 			r.addDataReading(new Point(xScaled, yScaled));
 
 		}
 
 	}
 
 	private void updateCurrentTimeSlice() {
 		switch (mSliceSize) {
 		case Day:
 			mEndOfCurrentTimeSlice.add(Calendar.DAY_OF_MONTH, 1);
 		case Hour:
 			mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
 			break;
 		case Half_Hour:
 			mEndOfCurrentTimeSlice.add(Calendar.MINUTE, 30);
 			break;
 		case Quarter_Hour:
 			mEndOfCurrentTimeSlice.add(Calendar.MINUTE, 15);
 			break;
 		}
 	}
 
 	private boolean isInCurrentTimeSlice(long timeStamp) {
 		mTempCalendar.setTimeInMillis(timeStamp);
 		if (mEndOfCurrentTimeSlice == null) {
 			switch (mSliceSize) {
 			case Day:
 				mEndOfCurrentTimeSlice = new GregorianCalendar();
 				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
 				mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.HOUR, 0);
 				mEndOfCurrentTimeSlice.add(Calendar.DAY_OF_MONTH, 1);
 				break;
 			case Hour:
 				mEndOfCurrentTimeSlice = new GregorianCalendar();
 				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
 				mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
 				mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
 				return true;
 			case Half_Hour:
 				mEndOfCurrentTimeSlice = new GregorianCalendar();
 				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
 				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
 				int min = mEndOfCurrentTimeSlice.get(Calendar.MINUTE);
 				if (min < 30)
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 30);
 				else {
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
 					mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
 				}
 				return true;
 			case Quarter_Hour:
 				mEndOfCurrentTimeSlice = new GregorianCalendar();
 				mEndOfCurrentTimeSlice.setTimeInMillis(timeStamp);
 				mEndOfCurrentTimeSlice.set(Calendar.SECOND, 0);
 				mEndOfCurrentTimeSlice.set(Calendar.MILLISECOND, 0);
 				min = mEndOfCurrentTimeSlice.get(Calendar.MINUTE);
 				if (min < 15)
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 15);
 				else if (min < 30)
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 30);
 				else if (min < 45)
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 45);
 				else {
 					mEndOfCurrentTimeSlice.set(Calendar.MINUTE, 0);
 					mEndOfCurrentTimeSlice.add(Calendar.HOUR, 1);
 				}
 				return true;
 			}
 		}
 
 		if (mTempCalendar.before(mEndOfCurrentTimeSlice)
 				|| mTempCalendar.equals(mEndOfCurrentTimeSlice))
 			return true;
 		return false;
 	}
 
 	/**
 	 * Checks if the passed timestamp passes all filters
 	 * 
 	 * @param timeStamp
 	 * @return
 	 */
 	private boolean passesTimeFilters(long timeStamp) {
 
 		mTempCalendar.setTimeInMillis(timeStamp);
 
 		if (mTempCalendar.before(mRangeStart) || mTempCalendar.after(mRangeEnd))
 			return false;
 
 		int hour = mTempCalendar.get(Calendar.HOUR_OF_DAY);
 		if (hour < mHourFilter.startHour || hour > mHourFilter.endHour)
 			return false;
 
 		return true;
 	}
 }
