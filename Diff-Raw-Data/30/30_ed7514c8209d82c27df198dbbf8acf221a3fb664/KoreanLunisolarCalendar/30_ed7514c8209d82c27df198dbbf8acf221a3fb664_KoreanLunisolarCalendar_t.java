 package com.lge.calendar;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 
 public class KoreanLunisolarCalendar extends Calendar {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8177237662194876813L;

 	private static final int KOREA_TIME_ZONE = 9 * 60 * 60 * 1000;
 
 	private static final long HOUR_IN_MILLIS = 1000 * 60 * 60;
 	private static final long DAY_IN_MILLIS = HOUR_IN_MILLIS * 24;
 	private static int[] maximums = new int[] { 1, 2050, 12, 53, 6, 30,
 			385, 7, 6, 1, 11, 23, 59, 59, 999, 14 * 3600 * 1000, 7200000 };
 
 	private static int[] minimums = new int[] { 0, 1969, 0, 1, 0, 1, 1, 1, 1, 0,
 			0, 0, 0, 0, 0, -13 * 3600 * 1000, 0 };
 
 	private static int[] leastMaximums = new int[] { 1, 2050, 11, 50, 3,
 			29, 354, 7, 3, 1, 11, 23, 59, 59, 999, 50400000, 1200000 };
 
 
 	
 	static int[] daysInYear = { 354, // 1969
 			355, // 1970
 			384, // 1971
 			354, // 1972
 			354, // 1973
 			384, // 1974
 			354, // 1975
 			384, // 1976
 			354, // 1977
 			355, // 1978
 			384, // 1979
 			355, // 1980
 			354, // 1981
 			384, // 1982
 			354, // 1983
 			384, // 1984
 			354, // 1985
 			354, // 1986
 			385, // 1987
 			354, // 1988
 			355, // 1989
 			384, // 1990
 			354, // 1991
 			354, // 1992
 			383, // 1993
 			355, // 1994
 			384, // 1995
 			355, // 1996
 			354, // 1997
 			384, // 1998
 			354, // 1999
 			354, // 2000
 			384, // 2001
 			354, // 2002
 			355, // 2003
 			384, // 2004
 			354, // 2005
 			385, // 2006
 			354, // 2007
 			354, // 2008
 			384, // 2009
 			354, // 2010
 			354, // 2011
 			384, // 2012
 			355, // 2013
 			384, // 2014
 			354, // 2015
 			355, // 2016
 			384, // 2017
 			354, // 2018
 			354, // 2019
 			384, // 2020
 			354, // 2021
 			355, // 2022
 			384, // 2023
 			354, // 2024
 			384, // 2025
 			355, // 2026
 			354, // 2027
 			383, // 2028
 			355, // 2029
 			354, // 2030
 			384, // 2031
 			355, // 2032
 			384, // 2033
 			354, // 2034
 			354, // 2035
 			384, // 2036
 			354, // 2037
 			354, // 2038
 			384, // 2039
 			355, // 2040
 			355, // 2041
 			384, // 2042
 			354, // 2043
 			384, // 2044
 			354, // 2045
 			354, // 2046
 			384, // 2047
 			354, // 2048
 			355, // 2049
 			384, // 2050
 	};
 
 	static int[][] daysInMonth = {
 			{ 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, }, // 1969
 			{ 30, 29, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30, }, // 1970
 			{ 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30, }, // 1971
 			{ 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, }, // 1972
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 30, 29, }, // 1973
 			{ 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, }, // 1974
 			{ 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, }, // 1975
 			{ 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, }, // 1976
 			{ 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, }, // 1977
 			{ 30, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, }, // 1978
 			{ 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, }, // 1979
 			{ 30, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, }, // 1980
 			{ 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, }, // 1981
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, }, // 1982
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, }, // 1983
 			{ 30, 29, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, }, // 1984
 			{ 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, }, // 1985
 			{ 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, 29, }, // 1986
 			{ 30, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, }, // 1987
 			{ 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, }, // 1988
 			{ 30, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, }, // 1989
 			{ 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 30, }, // 1990
 			{ 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30, }, // 1991
 			{ 29, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, }, // 1992
 			{ 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, }, // 1993
 			{ 30, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, }, // 1994
 			{ 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, }, // 1995
 			{ 29, 30, 29, 30, 30, 29, 30, 29, 30, 30, 29, 30, }, // 1996
 			{ 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, }, // 1997
 			{ 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, 30, 29, }, // 1998
 			{ 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30, 29, }, // 1999
 			{ 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 29, }, // 2000
 			{ 30, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 29, 30, }, // 2001
 			{ 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, }, // 2002
 			{ 30, 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, }, // 2003
 			{ 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, }, // 2004
 			{ 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, 29, }, // 2005
 			{ 30, 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, }, // 2006
 			{ 29, 29, 30, 29, 29, 30, 29, 30, 30, 30, 29, 30, }, // 2007
 			{ 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30, }, // 2008
 			{ 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, }, // 2009
 			{ 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, }, // 2010
 			{ 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, }, // 2011
 			{ 30, 29, 30, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, }, // 2012
 			{ 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, }, // 2013
 			{ 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, }, // 2014
 			{ 29, 30, 29, 29, 30, 29, 30, 30, 30, 29, 30, 29, }, // 2015
 			{ 30, 29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, }, // 2016
 			{ 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, }, // 2017
 			{ 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, }, // 2018
 			{ 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, }, // 2019
 			{ 30, 29, 30, 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, }, // 2020
 			{ 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, }, // 2021
 			{ 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, }, // 2022
 			{ 29, 30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, }, // 2023
 			{ 29, 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, }, // 2024
 			{ 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, }, // 2025
 			{ 30, 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, }, // 2026
 			{ 29, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 30, }, // 2027
 			{ 29, 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, }, // 2028
 			{ 30, 30, 29, 30, 30, 29, 29, 30, 29, 29, 30, 30, }, // 2029
 			{ 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, }, // 2030
 			{ 30, 29, 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, }, // 2031
 			{ 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, }, // 2032
 			{ 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, 30, }, // 2033
 			{ 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 30, 29, }, // 2034
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, }, // 2035
 			{ 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, }, // 2036
 			{ 30, 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, }, // 2037
 			{ 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, 30, 29, }, // 2038
 			{ 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29, 29, }, // 2039
 			{ 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, }, // 2040
 			{ 30, 29, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, }, // 2041
 			{ 29, 30, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, }, // 2042
 			{ 29, 30, 29, 29, 30, 29, 29, 30, 30, 29, 30, 30, }, // 2043
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, 30, }, // 2044
 			{ 30, 29, 30, 29, 29, 30, 29, 29, 30, 29, 30, 30, }, // 2045
 			{ 30, 29, 30, 30, 29, 29, 30, 29, 29, 30, 29, 30, }, // 2046
 			{ 30, 29, 30, 30, 29, 30, 29, 30, 29, 29, 30, 29, 30, }, // 2047
 			{ 29, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, 29, }, // 2048
 			{ 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, 30, 29, }, // 2049
 			{ 30, 29, 29, 30, 29, 30, 29, 30, 30, 29, 30, 30, 29, }, // 2050
 	};
 
 	static int[] leapMonths = { 0, // 1969
 			0, // 1970
 			5, // 1971
 			0, // 1972
 			0, // 1973
 			4, // 1974
 			0, // 1975
 			8, // 1976
 			0, // 1977
 			0, // 1978
 			6, // 1979
 			0, // 1980
 			0, // 1981
 			4, // 1982
 			0, // 1983
 			10, // 1984
 			0, // 1985
 			0, // 1986
 			6, // 1987
 			0, // 1988
 			0, // 1989
 			5, // 1990
 			0, // 1991
 			0, // 1992
 			3, // 1993
 			0, // 1994
 			8, // 1995
 			0, // 1996
 			0, // 1997
 			5, // 1998
 			0, // 1999
 			0, // 2000
 			4, // 2001
 			0, // 2002
 			0, // 2003
 			2, // 2004
 			0, // 2005
 			7, // 2006
 			0, // 2007
 			0, // 2008
 			5, // 2009
 			0, // 2010
 			0, // 2011
 			3, // 2012
 			0, // 2013
 			9, // 2014
 			0, // 2015
 			0, // 2016
 			5, // 2017
 			0, // 2018
 			0, // 2019
 			4, // 2020
 			0, // 2021
 			0, // 2022
 			2, // 2023
 			0, // 2024
 			6, // 2025
 			0, // 2026
 			0, // 2027
 			5, // 2028
 			0, // 2029
 			0, // 2030
 			3, // 2031
 			0, // 2032
 			11, // 2033
 			0, // 2034
 			0, // 2035
 			6, // 2036
 			0, // 2037
 			0, // 2038
 			5, // 2039
 			0, // 2040
 			0, // 2041
 			2, // 2042
 			0, // 2043
 			7, // 2044
 			0, // 2045
 			0, // 2046
 			5, // 2047
 			0, // 2048
 			0, // 2049
 			3, // 2050
 	};
 
 
 
 	public KoreanLunisolarCalendar(Date time) {
 		this(time.getTime());
 	}
 
 	public KoreanLunisolarCalendar() {
 		this(System.currentTimeMillis());
 	}
 
 	/**
 	 * @param millis
 	 *            the number of milliseconds since January 1, 1970, 00:00:00 GMT
 	 */
 	public KoreanLunisolarCalendar(long millis) {
 
 		setTimeInMillis(millis);
 	}
 
 	private void internal_set(int field, int value) {
 		fields[field] = value;
 	}
 	
 	private int internal_get(int field) {
 		return fields[field];
 	}
 
 	@Override
 	protected void computeTime() {
 		if (!isSet[YEAR] || !isSet[MONTH] || !isSet[DAY_OF_MONTH]) {
 			throw new IllegalArgumentException();
 		}
 		long days = 0;
 		int year = internal_get(YEAR) - 1969;
 		for (int y=0; y<year; y++) {
 			days += daysInYear[y];
 		}
 		int month = internal_get(MONTH);
 		for (int m=0; m<month; m++) {
 			days += daysInMonth[year][m];
 		}
 		days += internal_get(DAY_OF_MONTH) - 1 - 318;
 		time = days * DAY_IN_MILLIS;
 		
 		if (isSet[HOUR_OF_DAY]) {
 			time += internal_get(HOUR_OF_DAY) * HOUR_IN_MILLIS;
 		}
 		
 		time -= KOREA_TIME_ZONE;
 	}
 
 	@Override
 	protected void computeFields() {
 		long days = daysSince1970(time) + 318; // 1969 11 24  319°
 
 		int y = 0;
 		while (days >= daysInYear[y]) {
 			days -= daysInYear[y];
 			y++;
 		}
 
 		int m = 0;
 		while (days >= daysInMonth[y][m]) {
 			days -= daysInMonth[y][m];
 			m++;
 		}
 
 		internal_set(YEAR, y + 1969);
 		internal_set(MONTH, m);
 		internal_set(DAY_OF_MONTH, (int) days + 1);
 		isSet[YEAR] = isSet[MONTH] = isSet[DAY_OF_MONTH] = true;
 	}
 
 	@Override
 	public void add(int field, int amount) {
 		switch (field) {
 		case DAY_OF_MONTH:
 		{
 			long time = getTimeInMillis() + amount * DAY_IN_MILLIS;
 			setTimeInMillis(time);
 		}
 			break;
 		case MONTH:
 		{
 			int year = internal_get(YEAR)-1969;
 			int newMonth = internal_get(MONTH) + amount;
 			while (newMonth < 0) {
 				year--;
 				newMonth += daysInMonth[year].length;
 			}
 			while (newMonth >= daysInMonth[year].length) {
 				newMonth -= daysInMonth[year].length;
 				year++;
 			}
 
 			int day = internal_get(DAY_OF_MONTH);
 			if (day > daysInMonth[year][newMonth]) {
 				set(DAY_OF_MONTH, daysInMonth[year][newMonth]);
 			}
 			set(YEAR, year+1969);
 			set(MONTH, newMonth);
 		}
 			break;
 		case YEAR:
 		{
 			int newYear = internal_get(YEAR)+amount-1969;
 			int month = internal_get(MONTH);
 			if (month >= daysInMonth[newYear].length)
 				month = daysInMonth[newYear].length - 1;
 			int day = internal_get(DAY_OF_MONTH);
 			if (day > daysInMonth[newYear][month])
 				day = daysInMonth[newYear][month];
 			set(YEAR, newYear+1969);
 			set(MONTH, month);		
 			set(DAY_OF_MONTH, day);
 		}	
 			break;
 		default:
 			throw new UnsupportedOperationException();
 		}
 	}
 
 	@Override
 	public void roll(int field, boolean up) {
 		int[] old = Arrays.copyOfRange(fields, 0, field);
 		set(field, internal_get(field) + (up?1:-1));
 		complete();
 		for (int f=0; f<field; f++)
 			set(f, old[f]);
 	}
 
 	@Override
 	public int getMinimum(int field) {
 		return minimums[field];
 	}
 
 	@Override
 	public int getMaximum(int field) {
 		return maximums[field];
 	}
 
 	@Override
 	public int getGreatestMinimum(int field) {
 		return minimums[field];
 	}
 
 	@Override
 	public int getLeastMaximum(int field) {
 		return leastMaximums[field];
 	}
 
 	/**
 	 * @param millis
 	 *            since 1970, 1, 1 (GMT)
 	 * @return
 	 */
 	public static long daysSince1970(long millis) {
 		return (millis + KOREA_TIME_ZONE) / DAY_IN_MILLIS;
 	}
 
 	public static int getDaysInYear(int year) {
 		return daysInYear[year - 1969];
 	}
 
 	public static int getMonthsInYear(int year) {
 		return daysInMonth[year - 1969].length;
 	}
 
 	public static int getLeapMonthInYear(int year) {
 		return leapMonths[year - 1969];
 	}
 }
