 package pleocmd.pipe.data;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import pleocmd.Log;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.val.Value;
 import pleocmd.pipe.val.ValueType;
 
 /**
  * Helper class for converting {@link Data} objects from and to Ascii.
  * <p>
  * Data must consist of fields (separated by '|') and followed by '\n'.<br>
  * Whitespaces are allowed and will be ignored.<br>
  * Fields may be prefixed by a type identifier followed by ':'. Allowed type
  * identifiers are 'I', 'F', 'S' and 'B' for int, float, string or binary
  * values, optionally followed by modifier 'x' for hexadecimal data.<br>
  * A data block may be preceded by a list of flags between '[' and ']'. Valid
  * flags are:
  * <table>
  * <tr>
  * <th align=left>Character</th>
  * <th align=left>Description</th>
  * </tr>
  * <tr>
  * <td>P</td>
  * <td>Priority:<br>
  * The next 2 characters must be digits and will be interpreted as the priority
  * (may be preceded by a '-' for negative numbers)</td>
  * </tr>
  * <tr>
  * <td>T</td>
  * <td>Time:<br>
  * The next up to 10 characters must be digits and will be interpreted as the
  * time in milliseconds if followed by 'ms' or seconds if followed by 's' after
  * starting the {@link Pipe} at which this {@link Data} should be executed</td>
  * </tr>
  * </table>
  * <p>
  * Examples:<br>
  * 25|7.3|Hello<br>
  * [P-05]I:3|F:2.0|S:Some String<br>
  * S: 12345678 | 100 | F: 100 | Bx: F0DD35007E | Sx: 48454C4C4F<br>
  * [T1sP99]S:Very High Priority, executed after 1 second<br>
  * 
  * @author oliver
  */
 public final class DataAsciiConverter extends AbstractDataConverter {
 
 	/**
 	 * 08 => may be part of a decimal number (string otherwise)<br>
 	 * 09 => may be part of a floating point (string otherwise)<br>
 	 * 10 => valid decimal / floating point number<br>
 	 * 30 => valid string<br>
 	 * 40 => invalid
 	 */
 	private static final int[] TYPE_AUTODETECT_TABLE = { // 
 	/**//**/40, 40, 40, 40, 40, 40, 40, 40, // 00 - 07
 			40, 30, 40, 40, 40, 40, 40, 40, // 08 - 0F
 			40, 40, 40, 40, 40, 40, 40, 40, // 10 - 17
 			40, 40, 40, 40, 40, 40, 40, 40, // 18 - 1F
 			30, 30, 30, 30, 30, 30, 30, 30, // 20 - 27
 			30, 30, 30, +8, 30, +8, +9, 30, // 28 - 2F
 			10, 10, 10, 10, 10, 10, 10, 10, // 30 - 37
 			10, 10, 30, 30, 30, 30, 30, 30, // 38 - 3F
 			30, 30, 30, 30, 30, +9, 30, 30, // 40 - 47
 			30, 30, 30, 30, 30, 30, 30, 30, // 48 - 4F
 			30, 30, 30, 30, 30, 30, 30, 30, // 50 - 57
 			30, 30, 30, 30, 30, 30, 30, 30, // 58 - 5F
 			30, 30, 30, 30, 30, +9, 30, 30, // 60 - 67
 			30, 30, 30, 30, 30, 30, 30, 30, // 68 - 6F
 			30, 30, 30, 30, 30, 30, 30, 30, // 70 - 77
 			30, 30, 30, 30, 40, 30, 30, 40, // 78 - 7F
 			40, 40, 40, 40, 40, 40, 40, 40, // 80 - 87
 			40, 40, 40, 40, 40, 40, 40, 40, // 88 - 8F
 			40, 40, 40, 40, 40, 40, 40, 40, // 90 - 97
 			40, 40, 40, 40, 40, 40, 40, 40, // 98 - 9F
 			40, 40, 40, 40, 40, 40, 40, 40, // A0 - A7
 			40, 40, 40, 40, 40, 40, 40, 40, // A8 - AF
 			40, 40, 40, 40, 40, 40, 40, 40, // B0 - B7
 			40, 40, 40, 40, 40, 40, 40, 40, // B8 - BF
 			40, 40, 40, 40, 40, 40, 40, 40, // C0 - C7
 			40, 40, 40, 40, 40, 40, 40, 40, // C8 - CF
 			40, 40, 40, 40, 40, 40, 40, 40, // D0 - D7
 			40, 40, 40, 40, 40, 40, 40, 40, // D8 - DF
 			40, 40, 40, 40, 40, 40, 40, 40, // E0 - E7
 			40, 40, 40, 40, 40, 40, 40, 40, // E8 - EF
 			40, 40, 40, 40, 40, 40, 40, 40, // F0 - F7
 			40, 40, 40, 40, 40, 40, 40, 40, // F8 - FF
 	};
 
 	private static final byte[] HEX_TABLE = new byte[] { '0', '1', '2', '3',
 			'4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
 
 	private byte[] buf;
 
 	private int buflen;
 
 	private ValueType type;
 
 	private boolean isHex;
 
 	private int index;
 
 	/**
 	 * Creates a new {@link DataAsciiConverter} that wraps an existing
 	 * {@link Data} object.
 	 * 
 	 * @param data
 	 *            {@link Data} to read from
 	 */
 	public DataAsciiConverter(final Data data) {
 		super(data);
 	}
 
 	/**
 	 * Creates a new {@link DataAsciiConverter} and sets all its fields
 	 * according to the Ascii representation of a {@link Data} in the
 	 * {@link DataInput}.
 	 * 
 	 * @param in
 	 *            Input Stream with text data in ISO-8859-1 encoding
 	 * @throws IOException
 	 *             if data could not be read from {@link DataInput}, is of an
 	 *             invalid type or is of an invalid format for its type
 	 */
 	public DataAsciiConverter(final DataInput in) throws IOException {
 		Log.detail("Started parsing an ASCII Data object");
 		buf = new byte[64];
 		index = -1;
 		while (true) {
 			++index;
 			final byte b = in.readByte();
 			switch (b) {
 			case '\n':
 				parseValue(getValues().isEmpty());
 				// this was the end of the data block
 				trimValues();
 				Log.detail("Finished parsing an ASCII Data object");
 				return;
 			case '|':
 				parseValue(false);
 				// prepare for the next value
 				type = null;
 				isHex = false;
 				buflen = 0;
 				break;
 			case '[':
 				if (index == 0)
 					parseFlags(in);
 				else
 					// treat '[' on other positions as a normal character
 					putByteIntoBuffer(b);
 				break;
 			case ':':
 				if (type == null && (buflen == 1 || buflen == 2))
 					parseTypeIdentifier();
 				else
 					// treat (second) ':' on other positions as
 					// a normal character
 					putByteIntoBuffer(b);
 				break;
 			case ' ':
 				if (buflen > 0) // ignore whitespaces at the beginning
 					putByteIntoBuffer(b);
 				break;
 			default:
 				putByteIntoBuffer(b);
 			}
 		}
 	}
 
 	private void parseValue(final boolean ignoreIfEmpty) throws IOException {
 		// trim whitespaces
 		while (buflen > 0 && buf[buflen - 1] == ' ')
 			--buflen;
 
 		if (ignoreIfEmpty && buflen == 0) return;
 
 		if (type == null) {
 			// autodetect type
 			isHex = false;
 			type = detectDataType(buf, buflen);
 			Log.detail("Autodetecting resulted in: %s", type);
 		}
 
 		// create fitting value
 		final Value val = Value.createForType(type);
 		if (val == null) throw new InternalError("Invalid value type");
 
 		if (isHex) {
 			// we need to decode the data from a hex string
 			Log.detail("Converting hex data with length %d", buflen);
 			if (buflen % 2 != 0)
 				throw new IOException(String.format("Broken hexadecimal data: "
 						+ "Length must be multiple of two: %d", buflen));
 			final byte[] buf2 = new byte[buflen / 2];
 			for (int i = 0, j = 0; i < buflen;) {
 				final int d1 = Character.digit(buf[i++], 16); // CS_IGNORE
 				final int d2 = Character.digit(buf[i++], 16); // CS_IGNORE
 				if (d1 == -1)
 					throw new IOException(String.format("Broken hexadecimal "
 							+ "data: Invalid character at %d: 0x%02X", index
 							- buflen + i - 2, buf[i - 2]));
 				if (d2 == -1)
 					throw new IOException(String.format("Broken hexadecimal "
 							+ "data: Invalid character at %d: 0x%02X", index
 							- buflen + i - 1, buf[i - 1]));
 				buf2[j++] = (byte) (d1 << 4 | d2); // CS_IGNORE
 			}
 			val.readFromAscii(buf2, buf2.length);
 		} else
 			val.readFromAscii(buf, buflen);
 		getValues().add(val);
 	}
 
 	public void writeToAscii(final DataOutput out, final boolean writeLF)
 			throws IOException {
 		Log.detail("Writing Data to ASCII output stream");
 		writeFlags(out);
 
 		boolean first = true;
 		for (final Value value : getValues()) {
 			// write delimiter if needed
 			if (!first) {
 				out.writeByte(' ');
 				out.writeByte('|');
 				out.writeByte(' ');
 			}
 			first = false;
 
 			final boolean hex = value.mustWriteAsciiAsHex();
 			// write the field type identifier (and modifier if needed)
 			if (hex) {
 				out.writeByte(Value.getAsciiTypeChar(value));
 				out.writeByte('x');
 				out.writeByte(':');
 				out.writeByte(' ');
 			}
 
 			// write the field content in decimal or hex
 			if (hex) {
 				final ByteArrayOutputStream hexOut = new ByteArrayOutputStream();
 				value.writeToAscii(new DataOutputStream(hexOut));
 				final byte[] ba = hexOut.toByteArray();
 				for (final byte b : ba) {
 					out.write(HEX_TABLE[b >> 4 & 0x0F]);
 					out.write(HEX_TABLE[b & 0x0F]);
 				}
 			} else
 				value.writeToAscii(out);
 		}
 
 		// write the final block delimiter
 		if (writeLF) out.writeByte('\n');
 	}
 
 	private void writeFlags(final DataOutput out) throws IOException {
 		if (getPriority() == Data.PRIO_DEFAULT && getTime() == Data.TIME_NOTIME)
 			return;
 		out.writeByte('[');
 		out.writeByte(' ');
 		if (getPriority() != Data.PRIO_DEFAULT) {
 			out.writeByte('P');
 			if (getPriority() < 0) out.writeByte('-');
 			out.writeByte('0' + Math.abs(getPriority()) / 10);
 			out.writeByte('0' + Math.abs(getPriority()) % 10);
 			out.writeByte(' ');
 		}
 		if (getTime() != Data.TIME_NOTIME) {
 			out.writeByte('T');
 			final boolean inSec = getTime() % 1000 == 0;
 			final long val = inSec ? getTime() / 1000 : getTime();
 			out.write(String.valueOf(val).getBytes("ISO-8859-1"));
 			if (!inSec) out.write('m');
 			out.write('s');
 			out.writeByte(' ');
 		}
 		out.writeByte(']');
 		out.writeByte(' ');
 	}
 
 	private void parseFlags(final DataInput in) throws IOException {
 		while (true) {
 			++index;
 			final byte b = in.readByte();
 			switch (b) {
 			case ' ': // just ignore any spaces in flag list
 				break;
 			case ']': // end of flag list
 				return;
 			case 'P':
 			case 'p':
 				parseFlagPriority(in);
 				break;
 			case 'T':
 			case 't':
 				parseFlagTime(in);
 				break;
 			default:
 				throw new IOException(String.format("Invalid character 0x%02X "
 						+ "in flag list at position %d", b, index));
 			}
 		}
 	}
 
 	private void parseFlagPriority(final DataInput in) throws IOException {
 		++index;
 		byte b = in.readByte();
 		final boolean neg = b == '-';
 		if (neg) {
 			++index;
 			b = in.readByte();
 		}
 		byte res = 0;
 		if (b < '0' || b > '9')
 			throw new IOException(String.format("Invalid character 0x%02X "
 					+ "in priority at position %d", b, index));
 		res += (b - '0') * 10;
 		++index;
 		b = in.readByte();
 		if (b < '0' || b > '9')
 			throw new IOException(String.format("Invalid character 0x%02X "
 					+ "in priority at position %d", b, index));
 		res += b - '0';
 		if (neg) res = (byte) -res;
 
 		Log.detail("Parsed priority: %d", res);
 
 		if (res < Data.PRIO_LOWEST || res > Data.PRIO_HIGHEST)
 			throw new IOException(String.format("Priority is out of range: "
 					+ "%d not between %d and %d", res, Data.PRIO_LOWEST,
 					Data.PRIO_HIGHEST));
 		setPriority(res);
 	}
 
 	private void parseFlagTime(final DataInput in) throws IOException {
 		long res = 0;
 		while (true) {
 			++index;
 			final byte b = in.readByte();
 			if (b == 'm') {
 				++index;
 				final byte b2 = in.readByte();
 				if (b2 != 's')
 					throw new IOException(String.format("Invalid character "
 							+ "0x%02X in time at position %d", b2, index));
 				break;
 			}
 			if (b == 's') {
 				res *= 1000;
 				break;
 			}
 			if (b < '0' || b > '9')
 				throw new IOException(String.format("Invalid character 0x%02X "
 						+ "in time at position %d", b, index));
 			res *= 10;
 			res += b - '0';
 		}
 
 		Log.detail("Parsed time: %d ms", res);
 
 		if (res > 0xFFFFFFFFL)
 			throw new IOException(String.format("Time is out of range: "
 					+ "%d not between 0 and 0xFFFFFFFF", res));
 		setTime(res);
 	}
 
 	private void parseTypeIdentifier() throws IOException {
 		type = Value.detectFromTypeChar((char) buf[0], index - buflen);
 		if (buflen == 2) {
 			if (buf[1] != 'x')
 				throw new IOException(String.format(
 						"Invalid type modifier: 0x%02X at " + "position %d",
 						buf[1], index - 1));
 			isHex = true;
 		} else
 			isHex = false;
 		buflen = 0;
 		Log.detail("Forced type '%s' - hex: %s", type, isHex);
 	}
 
 	private void putByteIntoBuffer(final byte b) {
 		if (buflen == buf.length) {
 			final int bufcap = buf.length * 2;
 			final byte[] buf2 = new byte[bufcap];
 			System.arraycopy(buf, 0, buf2, 0, buflen);
 			buf = buf2;
 		}
 		buf[buflen++] = b;
 	}
 
 	/**
 	 * Returns the most specific {@link ValueType} which can read the data.
 	 * 
 	 * @param data
 	 *            the Ascii data which should be converted
 	 * @param len
 	 *            length of the data
 	 * @return one of {@link ValueType#Int64}, {@link ValueType#Float64} or
 	 *         {@link ValueType#NullTermString}
 	 * @throws IOException
 	 *             if the data is not in one of the known data formats
 	 */
 	private static ValueType detectDataType(final byte[] data, final int len)
 			throws IOException {
 		Log.detail("Autodetecting data type of %d bytes", len);
 		int tat;
 		int res = 0;
 		final boolean[] found = new boolean[256];
 		for (int i = 0; i < len; ++i) {
 			found[tat = TYPE_AUTODETECT_TABLE[data[i] & 0xFF]] = true;
			if ((res = Math.max(res, tat)) == 4)
 				throw new IOException(String.format(
 						"Invalid character for any known data type: 0x%02X "
 								+ "at position %d in '%s'", data[i], i,
 						toHexString(data, len)));
 		}
 		switch (res) {
 		case 0: // treat empty data as string
 		case 8: // treat incomplete decimal number as string
 		case 9: // treat incomplete floating point as string
 		case 30: // valid string characters
 			return ValueType.NullTermString;
 		case 10: // valid digits
 			return found[9] ? ValueType.Float64 : ValueType.Int64;
 		default:
 			throw new InternalError("Invalid entry in TYPE_AUTODETECT_TABLE");
 		}
 	}
 
 	public static String toHexString(final byte[] a, final int len) {
 		final StringBuilder sb = new StringBuilder("[");
 		for (int i = 0; i < len; ++i) {
 			if (i > 0) sb.append(", ");
 			sb.append(a[i]);
 		}
 		return sb.append(']').toString();
 	}
 
 }
