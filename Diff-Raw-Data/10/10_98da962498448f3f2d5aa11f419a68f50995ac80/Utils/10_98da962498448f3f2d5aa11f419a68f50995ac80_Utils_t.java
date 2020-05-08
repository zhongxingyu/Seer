 package org.tomac.utils;
 
 import java.nio.ByteBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.tomac.protocol.fix.FixUtils;
 
 public class Utils {
 	private static Utils						utils							= new Utils();
 
 	public static final Calendar				calendarUTC							= new GregorianCalendar(TimeZone.getTimeZone("UTC"));
 	public static final UtcTimestampConverter	utcTimestampConverter				= utils.new UtcTimestampConverter();
 	public static final UtcTimeOnlyConverter	utcTimeOnlyConverter				= utils.new UtcTimeOnlyConverter();
 	public static final UtcDateOnlyConverter	utcDateOnlyConverter				= utils.new UtcDateOnlyConverter();
 
 	public static byte[] digitsBuf = new byte[FixUtils.FIX_MAX_DIGITS];
 
 	public static void copy(final byte dst[], final byte src[]) {
 		int length = src.length > dst.length ? dst.length : src.length;
 
 		System.arraycopy(src, 0, dst, 0, length);
 	}
 
	public static void copyTrim(final byte dst[], final byte src[]) {
 		int length = src.length > dst.length ? dst.length : src.length;
 
 		int begin = 0;
 		for (begin = 0; begin < length; begin++) {
 			if (src[begin] != (byte)' ' && src[begin] != (byte)0 && src[begin] != (byte)1 ) break;
 		}
 
		System.arraycopy(src, begin, dst, 0, length - begin);
 	}
 	
 	public static void copy(final byte src[], int srcOffset, final byte dst[], int dstOffset, int length ) {
 		System.arraycopy(src, srcOffset, dst, dstOffset, length);
 	}
 	
 	public static int scan(ByteBuffer buf, int offset, byte b) {
 		int pos = buf.position();
 		
 		buf.position(offset);
 		
 		while (buf.hasRemaining()) {
 			if (buf.get() == b) {
 				int outPos = buf.position() - 1;
 				buf.position(pos);
 				return outPos;
 			}
 		}
 
 		return -1;
 	}
 	
 	public static boolean contains(ByteBuffer buf, byte[] target) {
 		int pos = buf.position();
 		//find the first character
 		int from = scan(buf, buf.position(), target[0]);
 		
 		while(from >= 0) 
 		{
 			ByteBuffer sub = buf.slice();
 			
 			if(equals(sub, target, 0, target.length)) {
 				buf.position(from + target.length);
 				return true;
 			}
 				
 			from = scan(buf, from+1, target[0]);
 		}
 
 		buf.position(pos);
 		return false;
 	}
 	
 	public static final int digits( long number ) {
         
         number = Math.abs( number );
         
         if ( number < 10L ) return 1;
         if ( number < 100L ) return 2;
         if ( number < 1000L ) return 3;
         if ( number < 10000L ) return 4;
         if ( number < 100000L ) return 5;
         if ( number < 1000000L ) return 6;
         if ( number < 10000000L ) return 7;
         if ( number < 100000000L ) return 8;
         if ( number < 1000000000L ) return 9;
         if ( number < 10000000000L ) return 10;
         if ( number < 100000000000L ) return 11;
         if ( number < 1000000000000L ) return 12;
         if ( number < 10000000000000L ) return 13;
         if ( number < 100000000000000L ) return 14;
         if ( number < 1000000000000000L ) return 15;
         if ( number < 10000000000000000L ) return 16;
         if ( number < 100000000000000000L ) return 17;
         if ( number < 1000000000000000000L ) return 18;
         
         return 19;
     }
 	
 	public static final int multiplier( int number ) {
         
 		switch(number) {
 		case 0: return 1;
 		case 1: return 10;
 		case 2: return 100;
 		case 3: return 1000;
 		case 4: return 10000;
 		case 5: return 100000;
 		case 6: return 1000000;
 		case 7: return 10000000;
 		case 8: return 100000000;
 		case 9: return 1000000000;
 		default: return 1000000000;
 		}
     }
     
 	public static boolean equals(final byte[] x, final byte[] y) {
 
 		int len = x.length < y.length ? x.length : y.length;
 		int offset = 0;
 
 		while (len > 0) {
 			if (x[offset] != y[offset])
 				return false;
 
 			offset++;
 			len--;
 		}
 
 		return true;
 	}
 
 	private static boolean equals(ByteBuffer x, byte[] y, int yOffset, int len) {
 		int pos = x.position();
 		int xOffset = pos;
 		
 		while (len > 0 && x.limit() > xOffset) {
 			if (x.get(xOffset) != y[yOffset])
 				return false;
 
 			xOffset++;
 			yOffset ++;
 			len--;
 		}
 
 		return true;
 	}
 
 	public static boolean equals(ByteBuffer x, int xOffset, int length, byte[] y) {
 		int yOffset = 0;
 
 		int len = length < y.length ? length : y.length;
 
 		while (len > 0 && x.limit() > xOffset) {
 			if (x.get(xOffset) != y[yOffset])
 				return false;
 
 			xOffset++;
 			yOffset++;
 			len--;
 		}
 
 		return true;
 	}
 
 	public static void fillNul(final byte[] buf) {
 		for (int i = 0; i < buf.length; i++)
 			buf[i] = (byte) 0;
 	}
 
 	public static void fill(final byte[] buf, byte b) {
 		for (int i = 0; i < buf.length; i++)
 			buf[i] = b;
 	}
 
 	public static void fill(final byte[] buf, int offset, int len, byte b) {
 		for (int i = offset; i < len; i++)
 			buf[i] = b;
 	}
 	
 	/**
 	 * Index if last byte. Trimming trailing bytes.
 	 * @param arr
 	 * @return A copy of the byte array, with leading and trailing whitespace
      * omitted
 	 */
 	public static int lastIndexTrim(byte[] arr, byte trimmer){
 		int len=arr.length;
 		if(len > 0) {
 			while(len>0 && arr[len-1] == trimmer) len--; 		
 			return len;
 		}
 		return len;
 	}
 	
 	/**
 	 * Trims trailing trimmer bytes.
 	 * @param arr
 	 * @return A copy of the byte array, with leading and trailing whitespace
      * omitted
 	 */
 	public static byte[] trim(byte[] arr, byte trimmer){
 		int len=arr.length;
 		byte[] dest;
 		if(len > 0) {
 			while(len>0 && arr[len-1] == trimmer) len--; 		
 			dest = new byte[len];
 			Utils.copy(arr, 0, dest, 0, len);
 		}
 		else {
 			dest = new byte[0];
 		}
 		return dest;
 	}
 
 	
 	
 
 	public static void generateCheckSum(final byte[] checkSum, final ByteBuffer buf, final int startPos, final int endPos) {
 		final int currPos = buf.position();
 		int cks = 0;
 		final int bufLen = endPos - startPos;
 
 		buf.position(startPos);
 		for (int i = 0; i < bufLen; i++)
 			cks += buf.get();
 		Utils.longToNumeric(checkSum, 0, cks % 256, checkSum.length);
 		buf.position(currPos);
 	}
 
 	public static int intTagValueOf(ByteBuffer out, int len) {
 		int x = 0;
 
 		if (len == 0)
 			return 0;
 
 		int pos = 0;
 		
 		out.get(digitsBuf, 0, len);
 		
 		out.position(out.position() - len);
 		
 		while (len > 0 && digitsBuf[pos] >= '0' && digitsBuf[pos] <= '9') {
 
 			x *= 10;
 
 			x += digitsBuf[pos] - '0';
 
 			len--;
 
 			pos++;
 		}
 
 		if (len != 0) 
 			return 0;
 
 		return x;
 	}
 
 	public static int intValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && ( b[pos] == (byte) 0 || b[pos] == (byte) ' ' )) {
 			pos++;
 			len--;
 		}
 
 		int sign = 1;
 
 		if (b[pos] == '+') {
 			sign = 1;
 			len--;
 			pos++;
 		} else if (b[pos] == '-') {
 			sign = -1;
 			len--;
 			pos++;
 		}
 
 		int x = 0;
 
 		while (len > 0 && b[pos] >= '0' && b[pos] <= '9') {
 
 			x *= 10;
 
 			x += b[pos] - '0';
 
 			len--;
 
 			pos++;
 		}
 
 		if (len != 0)
 			throw new NumberFormatException();
 
 		return sign * x;
 	}
 
 	public static final boolean isNumeric(final int b) {
 
 		return b >= '0' && b <= '9';
 	}
 
 	public static void longToNumeric(ByteBuffer out, long l, int digits) {
 		longToNumeric(digitsBuf, 0, l, digits);
 		out.put(digitsBuf, 0, digits);
 	}
 
 	public static void longToNumeric(final byte out[], final int offset, long l, final int length) {
 		longToNumeric(out, offset, out.length, l, length);
 
 	}
 	
 	public static void longToNumeric(final byte out[], final int offset, int outLen, long l, final int length) {
 		final int radix = 10;
 		
 		fill(out, offset, outLen, (byte) '0');
 
 		if (l == 0) {
 			out[offset + length - 1] = (byte) '0';
 			return;
 		}
 
 		int count = 2;
 		long j = l;
 		final boolean negative = l < 0;
 		if (!negative) {
 			count = 1;
 			j = -l;
 		}
 		while ((l /= radix) != 0)
 			count++;
 
 		int pos = offset + length;
 		
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			out[--pos] = (byte) ch;
 
 		} while ((j /= radix) != 0);
 		if (negative)
 			out[0] = (byte) '-';
 			//out[--pos] = (byte) '-';
 
 	}
 
 	public static long longValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && (  b[pos] ==  (byte) 0 || b[pos] == (byte)' ' ) ) {
 			pos++;
 			len--;
 		}
 
 		long x = 0;
 
 		while (len > 0 && b[pos] >= '0' && b[pos] <= '9') {
 
 			x *= 10;
 
 			x += b[pos] - '0';
 
 			len--;
 
 			pos++;
 
 		}
 
 		if (len != 0)
 			throw new NumberFormatException();
 
 		return x;
 
 	}
 
 
 	
 	
 	// Helper classes
 	
 	/**
 	 * 
 	 * yyyyMMdd-HH:mm:SS[.sss] HH:mm:SS[.sss] yyyyMMdd
 	 * 
 	 **/
 	public static Date convert(byte[] buf, boolean getTime, boolean getDate) {
 
 		if (getTime && getDate && buf.length < 18)
 			return null;
 		else if (getTime && buf.length < 9)
 			return null;
 		else if (getDate && buf.length < 8)
 			return null;
 
 		int year = 0;
 		int month = 0;
 		int date = 0;
 		int hourOfDay = 0;
 		int minute = 0;
 		int second = 0;
 
 		int pos = 0;
 
 		if (getDate) {
 			year = (int) Utils.longValueOf(buf, 0, 4);
 			month = -1 + (int) Utils.longValueOf(buf, 4, 2);
 			date = (int) Utils.longValueOf(buf, 6, 2);
 			pos = 9;
 		} else {
 			year = (int) calendarUTC.get(Calendar.YEAR);
 			month = (int) calendarUTC.get(Calendar.MONTH);
 			date = (int) calendarUTC.get(Calendar.DATE);
 		}
 
 		if (getTime) {
 			hourOfDay = (int) Utils.longValueOf(buf, pos, 2);
 			minute = (int) Utils.longValueOf(buf, pos + 3, 2);
 			second = (int) Utils.longValueOf(buf, pos + 6, 2);
 			pos += 8;
 		}
 
 		calendarUTC.clear();
 		calendarUTC.set(year, month, date, hourOfDay, minute, second);
 		long basMillis = calendarUTC.getTimeInMillis();
 
 		long millis = 0;
 
 		if (buf.length == pos + 4) {
 			millis = Utils.longValueOf(buf, pos + 1, 3);
 		}
 
 		calendarUTC.setTimeInMillis(basMillis + millis);
 
 		return calendarUTC.getTime();
 	}
 
 	public static class FixFloatConverter {
 
 		public static String convert(long l) {
 			return convert(l, FixUtils.FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS);
 		}
 
 		public static long convert(byte[] buf) {
 			return FixUtils.fixFloatValueOf(buf, buf.length);
 		}
 
 		public static String convert(long l, int count) {
 			double d = l;
 
 			while (count > 0) {
 				d /= 10;
 				count--;
 			}
 			return String.valueOf(d);
 		}
 
 	}
 
 	public static class CharConverter {
 
 		public static String convert(char c) {
 			return String.valueOf(c);
 		}
 
 		public static byte convert(String s) {
 			byte[] buf = s.getBytes();
 			return buf.length == 1 ? buf[0] : null;
 		}
 
 	}
 
 	public static class BooleanConverter {
 
 		public static String convert(boolean b) {
 			return b ? "Y" : "N";
 		}
 
 		public static boolean convert(String s) {
 			if (s.equals("Y"))
 				return true;
 			else if (s.equals("N"))
 				return false;
 			else
 				throw new NumberFormatException();
 		}
 
 	}
 
 	public class UtcTimestampConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
 
 		public UtcTimestampConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return Utils.convert(buf, true, true);
 		}
 
 		public void convertToUtcTimestamp(byte[] buf, long currentTimeMillis, boolean useMillis) {
 			calendarUTC.clear();
 			calendarUTC.setTimeInMillis(currentTimeMillis);
 			Utils.longToNumeric(buf, 0, calendarUTC.get(Calendar.YEAR), 4); // 0 yyyy
 			Utils.longToNumeric(buf, 4, calendarUTC.get(Calendar.MONTH + 1), 2); // 4 yyyyMM
 			Utils.longToNumeric(buf, 6, calendarUTC.get(Calendar.DAY_OF_MONTH), 2); // 6 yyyyMMdd
 			buf[8] = (byte)'-'; // 8 yyyyMMdd-
 			Utils.longToNumeric(buf, 9, calendarUTC.get(Calendar.HOUR_OF_DAY), 2); // 9 yyyyMMdd-HH
 			buf[11] = (byte)':'; // 11 yyyyMMdd-HH: 
 			Utils.longToNumeric(buf, 12, calendarUTC.get(Calendar.MINUTE), 2); // 12 yyyyMMdd-HH:mm
 			buf[14] = (byte)':'; // 14 yyyyMMdd-HH:mm:
 			Utils.longToNumeric(buf, 15, calendarUTC.get(Calendar.SECOND), 2); // 15 yyyyMMdd-HH:mm:ss
 			if (useMillis) {
 				buf[17] = (byte)'.'; // 17 yyyyMMdd-HH:mm:ss.
 				Utils.longToNumeric(buf, 18, calendarUTC.get(Calendar.MILLISECOND), 3); // 18 yyyyMMdd-HH:mm:ss.SSS
 			}
 
 		}
 
 	}
 
 	public class UtcTimeOnlyConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("HH:mm:ss.SSS");
 
 		UtcTimeOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return Utils.convert(buf, true, false);
 		}
 
 	}
 
 	public class UtcDateOnlyConverter {
 		private SimpleDateFormat	format	= new SimpleDateFormat("yyyyMMdd");
 
 		UtcDateOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 
 		public String convert(Date date) {
 
 			calendarUTC.setTime(date);
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return Utils.convert(buf, false, true);
 		}
 
 	}
 
 	
 }
