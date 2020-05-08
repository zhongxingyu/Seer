 package com.goeswhere.tt;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.nio.charset.Charset;
 import java.sql.SQLException;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 
 public class TT {
 
 	private static final int PORT = 23000;
 	private static final int ESTIMATED_TRACKS_PER_PAGE = 25;
 	private static final Charset CHARSET = Charsets.UTF_8;
 
 	private static class ListElement {
 		private final int no;
 		private final boolean costs;
 
 		public ListElement(int no, boolean costs) {
 			this.no = no;
 			this.costs = costs;
 		}
 
 		@Override
 		public String toString() {
 			return "ListElement [no=" + no + (costs ? ", NON FREE" : "") + "]";
 		}
 	}
 
 	public static void main(String[] args) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
 		final DAO dao = TTLevelCache.dao();
 		final Socket s = new Socket(InetAddress.getByName("truck.gravitysensation.com"), PORT);
 
 		try {
 			final OutputStream os = s.getOutputStream();
 			final InputStream is = s.getInputStream();
 			final IOHelper h = IOHelper.loggingToStdErr(is);
 			setup(os, h);
 
 			final int pages = 1;
 			final List<Integer> tracks = Lists.newArrayListWithExpectedSize(pages * ESTIMATED_TRACKS_PER_PAGE);
 			write(os, SortOrder.DEFAULT.forPage(0));
 			readFreeTracks(is, tracks);
 			for (int i = 0; i < pages; ++i) {
 				Thread.sleep(100);
 				write(os, SortOrder.NEWEST.forPage(i));
 				readFreeTracks(is, tracks);
 				System.out.printf("Stage 1 / 2: %3d%% done\n", (int)(100 * (i+1) / (float)pages));
 			}
 
 			for (int id : tracks)
 				if (!dao.hasNameFor(id)) {
 					System.out.println("Stage 2/3: Fetching name for " + id + "...");
 					requestDetails(os, id);
 					dao.saveTrackName(id, readName(is));
 				}
 
 			for (int i = 0; i < tracks.size(); ++i) {
 				Thread.sleep(100);
 				int id = tracks.get(i);
 				final List<Score> scores = getScores(os, is, id, 0);
 				final List<Object[]> times = Lists.newArrayListWithCapacity(scores.size());
 				int pos = 0;
 				for (Score sc : scores) {
 					times.add(new Object[] { id, ++pos, sc.name, sc.time, sc.hard } );
 				}
 
 				dao.saveTrackTimes(id, times);
 				System.out.printf("Stage 3 / 3: %3d%% done.\n", (int)(100. * (i+1) / tracks.size()));
 			}
 
 		} finally {
 			s.close();
 		}
 	}
 
 	static void readFreeTracks(final InputStream is, final Collection<Integer> tracks) throws IOException {
 		final int length = readTwo(is) - 1;
 		skip(is, 1);
 		byte[] pkt = IOHelper.read(is, length);
 		for (ListElement l : parseListPacket(decode(pkt)))
 			if (!l.costs)
 				tracks.add(l.no);
 	}
 
 	static void discardWelcomePacket(final IOHelper h) throws IOException {
 		h.readOrWarn("start of welcome (unknown; probably header)",
 				new byte[] { 0x11, 0x00, 0x05 });
 		h.readName();
 		h.readOrWarn("middle of welcome (unknown)",
 				new byte[] { 0x05, 0x00, });
 		h.skip(5); // unknown field
 		decode(h.read(h.readTwo()));
 	}
 
 	private static List<ListElement> parseListPacket(char[] c) {
 		final int packetLength = 436;
 		int ptr = 0;
 		List<ListElement> l = Lists.newArrayListWithCapacity(ESTIMATED_TRACKS_PER_PAGE);
 		while (ptr < c.length - packetLength) {
 			int no = readTwo(c, ptr + 24);
 			boolean costs = c[ptr + packetLength] != 0;
 			l.add(new ListElement(no, costs));
 			ptr += packetLength;
 		}
 		return l;
 	}
 
 	static String readName(InputStream is) throws IOException {
 		final int nameLocation = 0x28;
 		final int nameLength = 16;
 
 		final int length = readTwo(is);
 		skip(is, nameLocation);
 		final String ret = cstring(is, nameLength);
 		skip(is, length - nameLength - nameLocation);
 		return ret;
 	}
 
 	static void skip(InputStream is, long length) throws IOException {
 		long rem = length;
 		while (rem != 0)
 			rem -= is.skip(rem);
 	}
 
 	private static int readTwo(char[] q, int ptr) {
 		return (q[ptr + 1] * 0x100) + q[ptr];
 	}
 
 	static int readTwo(InputStream is) throws IOException {
 		return read(is) + read(is) * 0x100;
 	}
 
 	private static int read(InputStream is) throws IOException {
 		int read = is.read();
 		if (-1 == read)
 			throw new IOException("End of stream");
 		return read;
 	}
 
 	public static void format(char[] c) {
 		format(c, c.length);
 	}
 
 	static void format(char[] c, int length) {
 		byte[] b = new byte[length];
 		for (int i = 0; i < length; ++i)
 			b[i] = (byte) c[i];
 		debugOutput(b, length);
 	}
 
 	/** @param track As displayed; apart from home screen.
 	 * @param truck 0 for free truck, upwards. */
 	private static List<Score> getScores(final OutputStream os,
 			final InputStream is, int track, int truck) throws IOException {
 		requestScores(os, track, truck);
 		return readScores(is);
 	}
 
 	static List<Score> readScores(final InputStream is) throws IOException {
 		final int magicOffset = 61;
 
 		final int length = readTwo(is);
 		skip(is, magicOffset);
		return parse(decode(IOHelper.read(is, length - magicOffset)));
 	}
 
 	private static void requestScores(final OutputStream os, int track, int truck) throws IOException {
 		byte[] req = new byte[] { 0x09, 0, 0x19, (byte) (track & 0xff), (byte) ((track >> 8) & 0xff),
 				(byte) ((track >> 16) & 0xff), (byte) ((track >> 24) & 0xff), (byte) truck, 0, 0, 0 };
 		write(os, req);
 	}
 
 	private static void requestDetails(final OutputStream os, int track) throws IOException {
 		byte[] req = new byte[] { 0x06, 0, 0x0a, 0x01, (byte) (track & 0xff), (byte) ((track >> 8) & 0xff),
 				(byte) ((track >> 16) & 0xff), (byte) ((track >> 24) & 0xff), };
 		write(os, req);
 	}
 
 	private static enum SortOrder {
 		DEFAULT(0),
 		NEWEST(2),
 		USERS(1),
 		RATED(3),
 		ALPHABETIC(5);
 
 		private final int number;
 
 		SortOrder(int number) {
 			this.number = number;
 		}
 
 		byte[] forPage(int page) {
 			return new byte[] { 0x04, 00, 0x09, 00, (byte) number, (byte) page };
 		}
 	}
 
 	private static void setup(final OutputStream os, IOHelper h) throws IOException {
 
 		final byte[] initial = new byte[] {
 				0x0d, 0x00, 0x04, 0x7e, (byte) 0xc1, 0x36, 0x65, 0x10,
 				0x00, 0x00, 0x00, (byte) 0xfa, 0x0d, 0x08, 0x0a };
 		final byte[] login = new byte[] { 0x09, 00, 0x1a, 00, 00, 00, 00, 00, 00, 00, 00, };
 		final byte[] privacies = new byte[] { 0x54, 00, 0x05, 0x65, 00, 00, 00, 00, 00,
 				00,
 				00, // username
 				00, 00, 00, 00, 00, 00, 00, 00, 65, 84, 73, 32, 82, 97, 100, 101, 111, 110,
 				32,
 				72, // system config
 				68, 32, 53, 56, 48, 48, 32, 83, 101, 114, 105, 101, 115, 32, 32, 59, 97, 116, 105, 99, 102, 120, 51,
 				50, 46, 100, 108, 108, 59, 99, 111, 114, 101, 115, 58, 52, 59, 49, 51, 50, 56, 120, 56, 52, 48, 59,
 				100, 101, 116, 97, 105, 108, 58, 50, 59, };
 
 		write(os, initial);
 		discardWelcomePacket(h);
 		write(os, login);
 		// consumeAvailable(is); // sometimes?
 		write(os, privacies);
 	}
 
 	private static void write(final OutputStream os, final byte[] bytes) throws IOException {
 		os.write(bytes);
 		os.flush();
 	}
 
 	private static void consumeAvailable(final InputStream is) throws IOException {
 		byte[] first = new byte[90000];
 		int len = is.read(first);
 		debugOutput(first, len);
 	}
 
 	private static void consumeToFile(final InputStream is, String name) throws IOException {
 		byte[] first = new byte[90000];
 		int len = is.read(first);
 		final FileOutputStream fos = new FileOutputStream(name);
 		fos.write(first, 0, len);
 		System.out.println("Written " + len + " bytes to " + name);
 		fos.flush();
 		fos.close();
 	}
 
 	private static void debugOutput(byte[] first, int len) {
 		final PrintStream out = System.out;
 		debugOutput(first, len, out);
 	}
 
 	static void debugOutput(byte[] first, int len, final PrintStream out) {
 		final int WIDTH = 16;
 		out.print("       ");
 		for (int i = 0; i < WIDTH; ++i) {
 			addBreak(WIDTH, i, out);
 			out.printf("%2x ", i);
 		}
 		out.print("  ");
 		for (int i = 0; i < WIDTH; ++i) {
 			addBreak(WIDTH, i, out);
 			out.printf("%x", i);
 		}
 		out.print("  ");
 		for (int i = 0; i < WIDTH; ++i)
 			out.printf("%x", i);
 
 		out.println();
 		for (int l = 0; l <= len / WIDTH; ++l) {
 			out.printf("%5x  ", l*WIDTH);
 			for (int i = 0; i < WIDTH; ++i) {
 				addBreak(WIDTH, i, out);
 				int ind = l*WIDTH+i;
 				if (ind < len) {
 					out.printf("%2x ", first[ind]);
 				}
 				else
 					out.print("   ");
 			}
 
 			out.print("  ");
 			printAscii(first, len, WIDTH, l, true, out);
 			out.print("  ");
 			printAscii(first, len, WIDTH, l, false, out);
 			out.println();
 		}
 	}
 
 	private static void printAscii(byte[] first, int len, final int WIDTH, int l, boolean doBreak, PrintStream out) {
 		for (int i = 0; i < WIDTH; ++i) {
 			if (doBreak)
 				addBreak(WIDTH, i, out);
 			int ind = l*WIDTH+i;
 			if (ind < len) {
 				byte chr = first[ind];
 				out.printf("%c", isPrint((char)chr) ?
 						(char)chr : chr == 0 ? '_' : '.');
 			} else
 				out.print(" ");
 		}
 	}
 
 	private static boolean isPrint(char c) {
 		return c >= 32 && c <= 128;
 	}
 
 	private static void addBreak(final int WIDTH, int i, PrintStream out) {
 		if (i % (WIDTH/4) == 0)
 			out.print(" ");
 	}
 
 	private static char charise(byte by) {
 		return (char) (by < 0 ? 256 + by : by);
 	}
 
	private static List<Score> parse(char[] b) {
		final List<Score> ret = new ArrayList<Score>(b.length / 32);
 		int ptr = 0;
 		int pos = 0;
		while (ptr < b.length - 30) {
 			final String name = cstring(b, ptr, 16);
 			ptr += 16;
 			final double time = dword(b, ptr) / 120.;
 			ptr += 4;
 			final boolean hard = b[ptr + 6] != 0;
 			final boolean online = b[ptr + 7] != 0;
 			ptr += 12;
 			ret.add(new Score(++pos, name, time, hard, online));
 		}
 		return ret;
 	}
 
 	private static long dword(char[] b, int ptr) {
		return (b[ptr] + (b[ptr + 1] << 8));
 	}
 
 	private static String cstring(char[] b, int ptr, int i) {
 		return trimAtNull(new String(b, ptr, i));
 	}
 
 	static String cstring(InputStream is, int nameLength) throws IOException {
 		return trimAtNull(new String(IOHelper.read(is, nameLength), CHARSET));
 	}
 
 	private static String trimAtNull(final String s) {
 		final int ind = s.indexOf(0);
 		if (-1 == ind)
 			return s;
 		return s.substring(0, ind);
 	}
 
 	static char[] decode(byte[] in) {
 		char[] cin = new char[in.length];
 		for (int i = 0; i < in.length; ++i)
 			cin[i] = charise(in[i]);
 		return decode(cin);
 	}
 
 	static char[] decode(char[] in) {
 		char[] out = new char[in.length * 20];
 
 		int outptr = 0;
 		int inptr = 0;
 		char flag = in[inptr++];
 		flag &= 0x1f;
 		char esp10 = flag;
 
 		while (true) {
 			if (flag >= 0x20) {
 
 				char highflag = (char) (flag >> 5);
 				int lowflag = -((flag & 0x1f) << 8);
 
 				--highflag;
 
 				if (6 == highflag) {
 					highflag = (char) (in[inptr++] + 6);
 				}
 
 				lowflag -= in[inptr++];
 
 				int sourceptr = outptr + lowflag;
 
 				if (inptr < in.length)
 					esp10 = flag = in[inptr++];
 				else
 					throw new AssertionError();
 
 				if (outptr == sourceptr) {
 
 					char thing = out[outptr - 1];
 
 					out[outptr++] = thing;
 					out[outptr++] = thing;
 					out[outptr++] = thing;
 
 					if (highflag != 0) {
 
 						flag = esp10;
 
 						for (int i = 0; i < highflag; ++i)
 							out[outptr++] = thing;
 					}
 				} else {
 
 					--sourceptr;
 
 					out[outptr++] = out[sourceptr++];
 					out[outptr++] = out[sourceptr++];
 					out[outptr++] = out[sourceptr++];
 
 					if ((highflag & 1) == 1) {
 
 						out[outptr++] = out[sourceptr++];
 
 						--highflag;
 					}
 
 					int tooutptr = outptr;
 					outptr += highflag;
 					highflag >>= 1;
 
 					while (highflag != 0) {
 						out[tooutptr++] = out[sourceptr++];
 						out[tooutptr++] = out[sourceptr++];
 
 						--highflag;
 					}
 				}
 			} else {
 				++flag;
 				int inend = inptr + flag;
 				if (inend >= in.length)
 					return Arrays.copyOfRange(out, 0, outptr);
 
 				for (int i = 0; i < flag; ++i)
 					out[outptr++] = in[inptr++];
 
 				flag = in[inptr++];
 				esp10 = flag;
 			}
 		}
 
 	}
 }
