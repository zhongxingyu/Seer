 package ws.alek.torrator.torrent.bencoded;
 
 import java.io.BufferedInputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PushbackInputStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class BEDecoder {
 	private final PushbackInputStream pis;
 	// Let the maximum allowable string size would be 10M
 	private final static long MAX_STRING_LENGTH = 10 * 1024 * 1024;
 
 	private final static int MAX_LENGTH_FIELD_SIZE = 255;
 
 	private boolean insideInfo = false;
 	private boolean calculateInfoHash = false;
	private MessageDigest digest;
 	private BinaryString infoHash = null;
 	private final static String INFO_KEY = "info";
 	private boolean isDecoded = false; // if false, then we didn't perform decoding yet.
 
 	public BEDecoder(InputStream in) {
 		pis = new PushbackInputStream(new BufferedInputStream(in), 1);
 	}
 
 	/**
 	 * @param in
 	 *            InputStream of bencoded data
 	 * @param calculateInfoHash
 	 *            should we perform calculation of info_hash from bencoded data.
 	 */
 	public BEDecoder(InputStream in, boolean calculateInfoHash) {
 		this(in);
 		this.calculateInfoHash = calculateInfoHash;
 	}
 
 	/**
 	 * Decodes and return proper object from bencoded InputStream. Returned
 	 * objects may be of type: BinaryString, BigInteger, List or Map.
 	 * 
 	 * @param in
 	 *            InputStream to read bencoded data from
 	 * @return BinaryString, BigInteger, List or Map
 	 * @throws IOException
 	 */
 	public static Object decode(InputStream in) throws IOException {
 		return new BEDecoder(in).decode();
 	}
 
 	public Object decode() throws IOException {
 		isDecoded = true;
 		int marker = readBack();
 		if (marker == -1) {
 			throw new EOFException();
 		} else if (marker >= '0' && marker <= '9') {
 			return readBinaryString();
 		} else if (marker == 'i') {
 			return readInteger();
 		} else if (marker == 'l') {
 			return readList();
 		} else if (marker == 'd') {
 			return readMap();
 		}
 		throw new BencodedFormatException("Invalid bencoded format.");
 	}
 
 	private BinaryString readBinaryString() throws IOException {
 		StringBuilder sb = new StringBuilder();
 		int ch;
 		int counter = 0;
 		while ((ch = read()) >= '0' && ch <= '9') {
 			sb.append((char) ch);
 
 			if (++counter > MAX_LENGTH_FIELD_SIZE)
 				throw new IllegalStateException("No, really? So big string?");
 		}
 
 		if (ch != ':' || counter <= 0)
 			throw new BencodedFormatException(
 					"Can't find ':' char after string length");
 
 		BigInteger length = new BigInteger(sb.toString(), 10);
 		if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
 				|| length.intValue() > MAX_STRING_LENGTH)
 			throw new IllegalArgumentException(
 					"Maximum allowable size for binary strings is "
 							+ MAX_STRING_LENGTH + " bytes");
 
 		return new BinaryString(read(length.intValue()));
 	}
 
 	private BigInteger readInteger() throws IOException {
 
 		// Read 'i' marker
 		if (read() != 'i') {
 			throw new IllegalStateException("Invalid marker.");
 		}
 
 		byte[] bytes = new byte[MAX_LENGTH_FIELD_SIZE];
 		int ch;
 		int idx = 0;
 
 		while ((ch = read()) != 'e') {
 			if (ch == -1)
 				throw new EOFException();
 			else if (ch < '0' || ch > '9')
 				throw new BencodedFormatException("Invalid integer format.");
 
 			bytes[idx] = (byte) ch;
 			idx++;
 			if (idx >= MAX_LENGTH_FIELD_SIZE) {
 				throw new IllegalStateException("Integer length overflow.");
 			}
 		}
 
 		return new BigInteger(new String(bytes, 0, idx));
 	}
 
 	private List<Object> readList() throws IOException {
 		// Read 'l' marker
 		if (read() != 'l') {
 			throw new IllegalStateException("Invalid marker.");
 		}
 
 		List<Object> list = new ArrayList<Object>();
 		int ch;
 		while ((ch = readBack()) != 'e') {
 			if (ch == -1)
 				throw new EOFException();
 
 			list.add(decode());
 		}
 		read(); // Read last 'e' marker
 		return list;
 	}
 
 	private Map<BinaryString, Object> readMap() throws IOException {
 		// Read 'd' marker
 		if (read() != 'd') {
 			throw new IllegalStateException("Invalid marker.");
 		}
 
 		Map<BinaryString, Object> map = new HashMap<BinaryString, Object>();
 		int ch;
 		while ((ch = readBack()) != 'e') {
 			if (ch == -1)
 				throw new EOFException();
 
 			BinaryString key = readBinaryString();
 
 			// Start calculate SHA1 digest if we found INFO entry.
			if (calculateInfoHash && INFO_KEY.equals(key.toString())) {
 				try {
 					digest = MessageDigest.getInstance("SHA1");
 					insideInfo = true;
 				} catch (NoSuchAlgorithmException e) {
 					e.printStackTrace();
 				}
 			}
 
 			Object value = decode();
 			map.put(key, value);
 		}
 		read(); // Read last 'e' marker

		// Save calculated INFO entry SHA1 checksum.
		if (insideInfo) {
			insideInfo = false;
			infoHash = new BinaryString(digest.digest());
			digest = null;
		}

 		return map;
 	}
 
 	private byte[] read(int length) throws IOException {
 		if (length > MAX_STRING_LENGTH) {
 			throw new IllegalArgumentException(
 					"Maximum allowable size for binary strings is "
 							+ MAX_STRING_LENGTH + " bytes");
 		}
 
 		int size = 0;
 		byte[] buf = new byte[length];
 		int readLen;
 		while (size < length) {
 			readLen = pis.read(buf, size, length - size);
 			if (readLen == -1)
 				throw new EOFException();
 			size += readLen;
 		}
 		if (insideInfo)
 			digest.update(buf);
 		return buf;
 	}
 
 	private int read() throws IOException {
 		int b = pis.read();
 		if (insideInfo)
 			digest.update((byte) b);
 		return b;
 	}
 
 	private int readBack() throws IOException {
 		int b = pis.read();
 		pis.unread(b);
 		return b;
 	}
 
 	public BinaryString getInfoHash() {
 		if(!isDecoded) {
 			throw new IllegalStateException("Need to perform decoding before getting info_hash");
 		}
 		return infoHash;
 	}
 
 }
