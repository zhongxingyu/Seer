 package org.tomac.protocol.fix;
 
 import java.nio.ByteBuffer;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.TimeZone;
 
 import org.tomac.protocol.fix.messaging.FixMessageInfo;
 import org.tomac.protocol.fix.messaging.FixTags;
 
 public class FixUtils {
 	private static FixUtils						fixUtils							= new FixUtils();
 
 	public static final int						FIX_HEADER							= 5 + FixTags.BEGINSTRING_LENGTH;										// '8' + '=' + BeginString[7] + SOH + '9' + '='
 	public static final int						FIX_MESSAGE_START					= FIX_HEADER + 5;							// '8' + '=' + BeginString[7] + SOH + '9' + '=' '0000' + SOH
 	public static final int						FIX_TRAILER							= 7;										// '10' + = + checkSum[3] + SOH
 
 	public static final byte					SOH									= (byte) 0x01;
 	public static final byte					EQUALS								= (byte) '=';
 	public static final byte					DECIMAL								= (byte) '.';
 
 	public static final short					TAG_HAS_NO_VALUE					= 0;
 	public static final short					TAG_HAS_VALUE						= -1;
 	// public static final short BUFFER_HAS_TAG > 0;
 
 	public static int							FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS	= 2;
 	public static int							FIX_FLOAT_NUMBER_OF_DECIMALS		= 100;
 	public static final int						FIX_MAX_DIGITS						= 19;
 	public static final int						FIX_MAX_TAG_LENGTH					= 4;
 	public static final int						FIX_MAX_INT_LENGTH					= 19;
 	public static final int						FIX_MAX_STRING_LENGTH				= 64;
 	public static final int						UTCTIMESTAMP_LENGTH					= 24;
 	public static final int						CURRENCY_LENGTH						= 3;
 	public static final int						FIX_MAX_STRING_TEXT_LENGTH			= 64;
 	public static final int						FIX_MAX_NOINGROUP					= 5;
 
 	private static byte[]						beginsStringTmp						= new byte[FixTags.BEGINSTRING_LENGTH];
 
 	public static boolean						isNasdaqOMX							= true;
 
 	private static byte[]						calcCheckSum						= new byte[FixTags.CHECKSUM_LENGTH];
 
 	private static byte[]						currCheckSum						= new byte[FixTags.CHECKSUM_LENGTH];
 
 	public static boolean						validateChecksum					= true;
 
     public static final Calendar 				calendarUTC							= new GregorianCalendar(TimeZone.getTimeZone("UTC"));
     
     public static final UtcTimestampConverter   utcTimestampConverter               = fixUtils.new UtcTimestampConverter();
 
     public static final UtcTimeOnlyConverter   utcTimeOnlyConverter               = fixUtils.new UtcTimeOnlyConverter();
 
     public static final UtcDateOnlyConverter   utcDateOnlyConverter               = fixUtils.new UtcDateOnlyConverter();
 
 	/**
 	 * Non intrusive check of header. Returns fix length of message.
 	 * 
 	 * @param buf
 	 * @param err
 	 * @return
 	 */
 	public static int checkHeaderAndTrailer(final ByteBuffer buf, final FixValidationError err) {
 		final int startPos = buf.position();
 		int length = 0;
 
 		if (buf.limit() <= FixUtils.FIX_HEADER)
 			err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "No room in message for body length", FixTags.BODYLENGTH_INT);
 
 		// BeginString
 		if (!err.hasError()) {
 			final int tag = FixMessage.getTag(buf, err);
 
 			if (tag != FixTags.BEGINSTRING_INT)
 				err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "BeginString missing", FixTags.BEGINSTRING_INT);
 			else {
 				FixMessage.getTagStringValue(buf, beginsStringTmp, 0, FixTags.BEGINSTRING_LENGTH, err);
 
 				if (!FixUtils.equals(beginsStringTmp, FixMessageInfo.BEGINSTRING_VALUE))
 					err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "BeginString not set to " + new String(FixMessageInfo.BEGINSTRING_VALUE), FixTags.BEGINSTRING_INT);
 			}
 
 		}
 
 		// BodyLength
 		if (!err.hasError()) {
 			final int tag = FixMessage.getTag(buf, err);
 
 			if (!err.hasError())
 				if (tag != FixTags.BODYLENGTH_INT)
 					err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "BodyLength missing", FixTags.BEGINSTRING_INT);
 				else
 					length = FixMessage.getTagIntValue(buf, err);
 		}
 
 		// CheckSum and trailer
 		if (!err.hasError() && validateChecksum) {
 
 			generateCheckSum(calcCheckSum, buf, buf.position(), length + buf.position());
 
 			buf.position(length + buf.position());
 			final int tag = FixMessage.getTag(buf, err);
 
 			if (!err.hasError())
 				if (tag != FixTags.CHECKSUM_INT)
 					err.setError((int) FixMessageInfo.SessionRejectReason.REQUIRED_TAG_MISSING, "CheckSum missing", FixTags.CHECKSUM_INT);
 				else {
 
 					FixMessage.getTagStringValue(buf, currCheckSum, 0, FixTags.CHECKSUM_LENGTH, err);
 
 					if (!err.hasError())
 						if (FixUtils.equals(currCheckSum, calcCheckSum))
 							err.setError((int) FixMessageInfo.SessionRejectReason.VALUE_IS_INCORRECT_OUT_OF_RANGE_FOR_THIS_TAG, "Checksum incorrect " + new String(currCheckSum), FixTags.CHECKSUM_INT);
 				}
 		}
 
 		buf.position(startPos);
 		return length;
 	}
 
 	public static void copy(final byte dst[], final byte src[]) {
 		int length = src.length > dst.length ? dst.length : src.length;
 
 		System.arraycopy(src, 0, dst, 0, length);
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
 
 	public static void fillSpace(final byte[] buf) {
 		for (int i = 0; i < buf.length; i++)
 			buf[i] = (byte) ' ';
 	}
 
 	public static long fixFloatValueOf(final byte[] s, int length) {
 		int start = 0;
 		boolean negative = false;
 		int decimal = -1;
 		long value;
 
 		final byte c = s[start];
 		if (c == '-') {
 			++start;
 			--length;
 			negative = true;
 		} else if (c == '+') {
 			++start;
 			--length;
 		}
 		if (length == 0)
 			throw new NumberFormatException("to short number");
 
 		for (decimal = length - 1; decimal >= 0; decimal--)
 			if (s[decimal] == (byte) '.')
 				break;
 
 		final int decimals = length - 1  - decimal;
 
 		if (decimal > -1) {
 			value = longValueOf(s, 0, decimal) * FIX_FLOAT_NUMBER_OF_DECIMALS;
 			value += intValueOf(s, decimal + 1 < length ? decimal + 1 : length - 1, FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS < decimals ? FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS : decimals);
 		} else
 			value = longValueOf(s, 0, decimals) * FIX_FLOAT_NUMBER_OF_DECIMALS;
 
 		if (negative)
 			return -1 * value;
 		return value;
 
 	}
 
 	
 	public static void generateCheckSum(final byte[] checkSum, final ByteBuffer buf, final int startPos, final int endPos) {
 		final int currPos = buf.position();
 		int cks = 0;
 		final int bufLen = endPos - startPos;
 
 		buf.position(startPos);
 		for (int i = 0; i < bufLen; i++)
 			cks += buf.get();
 		FixUtils.longToNumeric(checkSum, 0, cks % 256, checkSum.length);
 		buf.position(currPos);
 	}
 
 	public static int intValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && b[pos] == (byte) ' ') {
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
 		
 		if (len != 0) throw new NumberFormatException();
 
 		return sign * x;
 	}
 
 	public static final boolean isNumeric(final int b) {
 
 		return b >= '0' && b <= '9';
 	}
 
 	public static void longToFixFloat(final byte out[], final int offset, long l, final int length) {
 		final int radix = 10;
 
 		if (l == 0) {
 			out[offset] = (byte) '0';
 			return;
 		}
 
 		int count = 2;
 		long j = l;
 		final boolean negative = l < 0;
 		if (!negative) {
 			count = 1;
 			j = -l;
 		}
 		count++; // the decimal
 		int decimalPos = length - offset - FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS;
 
 		while ((l /= radix) != 0 && count < length - offset )
 			count++;
 		
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			if (count == decimalPos - 1) out[--count] = (byte)'.';
 			out[--count] = (byte) ch;
 
 		} while ((j /= radix) != 0);
 		if (negative)
 			out[0] = (byte) '-';
 
 	}	
 	
 	public static void longToNumeric(final byte out[], final int offset, long l, final int length) {
 		final int radix = 10;
 
 		if (l == 0) {
 			out[offset] = (byte) '0';
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
 
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			out[--count] = (byte) ch;
 
 		} while ((j /= radix) != 0);
 		if (negative)
 			out[0] = (byte) '-';
 
 	}
 
 	public static long longValueOf(final byte[] b, int pos, int len) {
 
 		while (len > 0 && b[pos] == (byte) ' ') {
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
 
 	public static void put(final ByteBuffer out, final byte b) {
 		out.put(b);
 	}
 
 	public static void put(final ByteBuffer out, final byte[] buf) {
 		for (final byte b : buf) {
 			if (b == SOH || b == 0)
 				break;
 
 			out.put(b);
 		}
 	}
 
 	public static void put(final ByteBuffer out, long l) {
 		final int radix = 10;
 		final int endPos;
 
 		if (l == 0) {
 			out.put((byte) '0');
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
 
 		int index = endPos = out.position() + count;
 
 		do {
 			int ch = 0 - (int) (j % radix);
 			if (ch > 9)
 				ch = ch - 10 + (byte) 'a';
 			else
 				ch += (byte) '0';
 			index--;
 			count--;
 			out.position(index);
 			out.put((byte) ch);
 
 		} while ((j /= radix) != 0);
 		if (negative) {
 			out.position(--index);
 			out.put((byte) '-');
 		}
 
 		out.position(endPos);
 	}
 
 	/**
 	 * Allocates byte[] copy without leading and trailing white spaces.
 	 * 
 	 * @param buf
 	 * @return byte array without trailing and leading whitespace characters
 	 */
 	public static byte[] trim(final byte[] buf) {
 		int start = 0;
 		for (final byte b : buf) {
 			if (b > 0x20)
 				break;
 			start++;
 		}
 		int end = buf.length;
 		for (int i = buf.length - 1; i > 0; i--) {
 			if (buf[i] > 0x20)
 				break;
 			end--;
 		}
 
 		final byte[] ret = new byte[end - start > 0 ? end - start : buf.length];
 		System.arraycopy(buf, start, ret, 0, ret.length);
 		return ret;
 	}
 	
	public static int getNoInGroup(FixGroup[] groups) {
 		int i = 0;
		for (FixGroup group : groups) {
			if (!group.hasGroup()) break;
			i++;
 		}
		return i;
 	}
 	
 	
 
 	/**
 	 * 
 	 * yyyyMMdd-HH:mm:SS[.sss]
 	 * HH:mm:SS[.sss]
 	 * yyyyMMdd
 	 * 
 	 **/
 	public static Date convert(byte[] buf, boolean getTime, boolean getDate) {
 
 		if (getTime && getDate && buf.length < 18) return null;
 		else if (getTime && buf.length < 9) return null;
 		else if (getDate && buf.length < 8) return null;
 		
 		int year = 0;
 		int month = 0;
 		int date = 0;
 		int hourOfDay = 0;
 		int minute = 0;
 		int second = 0;
 
 		int pos = 0;
 		
 		if (getDate) {
 			year = (int) FixUtils.longValueOf(buf, 0, 4);
 		    month = -1 + (int) FixUtils.longValueOf(buf, 4, 2);
 		    date = (int) FixUtils.longValueOf(buf, 6, 2);
 		    pos = 9;
 		} else {
 			year = (int) calendarUTC.get(Calendar.YEAR);
 		    month = (int) calendarUTC.get(Calendar.MONTH);
 		    date = (int) calendarUTC.get(Calendar.DATE);
 		}
 		
 		if (getTime) {
 			hourOfDay = (int) FixUtils.longValueOf(buf, pos, 2);
 			minute = (int) FixUtils.longValueOf(buf, pos + 3, 2);
 			second = (int) FixUtils.longValueOf(buf, pos + 6, 2);
 			pos += 8;
 		}
 		
 		calendarUTC.clear();
 		calendarUTC.set(year, month, date, hourOfDay, minute, second);
 		long basMillis = calendarUTC.getTimeInMillis();
 
 		long millis = 0;
 		
 		if (buf.length == pos + 4) {
 			millis = FixUtils.longValueOf(buf, pos + 1, 3);
 		}
 
 		calendarUTC.setTimeInMillis(basMillis + millis);
 
 		return calendarUTC.getTime();
 	}
 	
 	public static class FixFloatConverter {
 
 		public static String convert(long l) {
 			return convert(l, FIX_FLOAT_NUMBER_OF_DECIMALS_DIGITS);
 		}
 
 		public static long convert(byte[] buf) {
 			return FixUtils.fixFloatValueOf(buf, buf.length);
 		}
 
 		public static String convert(long l, int count) {
 			double d = l;
 
 			while (count>0) {
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
 			if (s.equals("Y")) return true;
 			else if (s.equals("N")) return false;
 			else throw new NumberFormatException();
 		}
 		
 	}
 
 	class UtcTimestampConverter {
 		private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
 
 		public UtcTimestampConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 		
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		// "yyyyMMdd-HH:mm:SS[.sss]"
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, true, true);
 		}
 		
 	}
 
 	class UtcTimeOnlyConverter {
 		private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
 
 		UtcTimeOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 		
 		public String convert(Date date) {
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, true, false);
 		}
 		
 	}
 
 	class UtcDateOnlyConverter {
 		private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
 
 		UtcDateOnlyConverter() {
 			format.setTimeZone(calendarUTC.getTimeZone());
 		}
 		
 		public String convert(Date date) {
 
 			calendarUTC.setTime(date);
 			return format.format(date);
 		}
 
 		public Date convert(byte[] buf) {
 			return FixUtils.convert(buf, false, true);
 		}
 		
 	}
 	
 	
 }
