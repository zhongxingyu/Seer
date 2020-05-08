 package com.xoba.util;
 
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Desktop.Action;
 import java.awt.Dimension;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.RandomAccessFile;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Formatter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.Random;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.UUID;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class MraUtils {
 
 	private static final ILogger logger = LogFactory.getDefault().create();
 
 	private MraUtils() {
 	}
 
 	public static final String US_ASCII = "US-ASCII";
 
 	public static interface IDependentCallable<KEY, VALUE> {
 
 		public VALUE call(Map<KEY, VALUE> priorResults) throws Exception;
 
 	}
 
 	public static interface IDependentTask<KEY, VALUE> {
 
 		public KEY getTaskID();
 
 		public VALUE call(Map<KEY, VALUE> priorResults) throws Exception;
 
 		/**
 		 * set of tasks that must be run first, before this one
 		 * 
 		 */
 		public Set<KEY> getDependsOn();
 
 	}
 
 	public static <KEY, VALUE> Map<KEY, VALUE> runIdempotentTasks(Collection<IDependentTask<KEY, VALUE>> tasks,
 			ExecutorService es, int maxRounds) throws Exception {
 
 		final Map<KEY, VALUE> out = new HashMap<KEY, VALUE>();
 
 		Map<KEY, IDependentTask<KEY, VALUE>> allTasks = new HashMap<KEY, MraUtils.IDependentTask<KEY, VALUE>>();
 		for (IDependentTask<KEY, VALUE> t : tasks) {
 			allTasks.put(t.getTaskID(), t);
 		}
 
 		Set<KEY> remaining = new HashSet<KEY>(allTasks.keySet());
 		Set<KEY> done = new HashSet<KEY>();
 
 		Map<KEY, Integer> failures = new HashMap<KEY, Integer>();
 
 		while (remaining.size() > 0) {
 
 			Set<KEY> toRun = new HashSet<KEY>();
 
 			for (KEY k : remaining) {
 				if (done.containsAll(allTasks.get(k).getDependsOn())) {
 					toRun.add(k);
 				}
 			}
 
 			if (toRun.size() == 0) {
 				throw new IllegalStateException("can't run any more tasks");
 			}
 
 			Map<KEY, Future<VALUE>> futures = new HashMap<KEY, Future<VALUE>>();
 
 			int submitted = 0;
 
 			for (KEY k : toRun) {
 
 				final IDependentTask<KEY, VALUE> t = allTasks.get(k);
 
 				boolean block = false;
 
 				if (failures.containsKey(k)) {
 					if (failures.get(k) > maxRounds) {
 						block = true;
 					}
 				}
 
 				if (!block) {
 					submitted++;
 					futures.put(k, es.submit(new Callable<VALUE>() {
 						@Override
 						public VALUE call() throws Exception {
 							return t.call(out);
 						}
 					}));
 				}
 			}
 
 			if (submitted == 0) {
 				throw new IllegalStateException("can't run any more tasks");
 			}
 
 			for (KEY k : futures.keySet()) {
 				try {
 					out.put(k, futures.get(k).get());
 					done.add(k);
 				} catch (Exception e) {
 					if (failures.containsKey(k)) {
 						failures.put(k, failures.get(k) + 1);
 					} else {
 						failures.put(k, 1);
 					}
 					logger.warnf("exception running %s: %s", k, e);
 					e.printStackTrace();
 				}
 			}
 
 			remaining.removeAll(done);
 
 		}
 
 		return out;
 	}
 
 	public static Map<String, String> parseCommandLineArgs(String... args) {
 		Map<String, String> out = new HashMap<String, String>();
 		for (String a : args) {
 			String[] split = a.trim().split("=");
 			if (split.length == 2) {
 				out.put(split[0].trim(), split[1].trim());
 			} else {
 				logger.warnf("ignoring argument \"%s\"", a);
 			}
 		}
 		return out;
 	}
 
 	public static String formatZeroDecimals(Number x) {
 		return new Formatter().format("%,.0f", x == null ? Double.NaN : x.doubleValue()).toString();
 	}
 
 	public static Dimension getMaxWindowDimension(double fraction) {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gc = ge.getDefaultScreenDevice();
 		int height = new Double(fraction * gc.getDisplayMode().getHeight()).intValue();
 		int width = new Double(fraction * gc.getDisplayMode().getWidth()).intValue();
 		return new Dimension(width, height);
 	}
 
 	public static void setCenteredInScreen(Component frame) {
 		setCenteredInScreen(frame, -1);
 	}
 
 	/**
 	 * 
 	 * @param frame
 	 * @param scale
 	 *            0.0 (exclusive) to 1.0 (inclusive, full-screen); if negative,
 	 *            use existing geometry of component
 	 */
 	public static void setCenteredInScreen(Component frame, double scale) {
 		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 		GraphicsDevice gd = ge.getDefaultScreenDevice();
 		GraphicsConfiguration gc = gd.getDefaultConfiguration();
 		Rectangle bounds = gc.getBounds();
 
 		int width = new Double(scale * bounds.getWidth()).intValue();
 		int height = new Double(scale * bounds.getHeight()).intValue();
 
 		if (scale < 0) {
 			width = frame.getWidth();
 			height = frame.getHeight();
 		}
 
 		int x = new Double((bounds.getWidth() - width) / 2).intValue();
 		int y = new Double((bounds.getHeight() - height) / 2).intValue();
 
 		frame.setSize(width, height);
 		frame.setLocation(x, y);
 	}
 
 	public static byte[] load(InputStream in) throws IOException {
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		copy(in, out);
 		out.close();
 		return out.toByteArray();
 	}
 
 	public static String getRandomMD5Hash(int bytesOfRandomness) {
 		byte[] buf = new byte[bytesOfRandomness];
 		new Random().nextBytes(buf);
 		return md5Hash(buf);
 	}
 
 	public static String getRandomMD5Hash(int bytesOfRandomness, Random random) {
 		byte[] buf = new byte[bytesOfRandomness];
 		random.nextBytes(buf);
 		return md5Hash(buf);
 	}
 
 	public static String md5Hash(String buf) {
 		try {
 			return md5Hash(buf.getBytes(US_ASCII));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static byte[] md5HashStringToBytes(String buf) {
 		try {
 			return md5HashBytesToBytes(buf.getBytes(US_ASCII));
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static String md5Hash(File in) throws IOException {
 		return hash(new BufferedInputStream(new FileInputStream(in)), "MD5");
 	}
 
 	public static String md5Hash(InputStream in) throws IOException {
 		return hash(in, "MD5");
 	}
 
 	public static String sha1Hash(InputStream in) throws IOException {
 		return hash(in, "SHA1");
 	}
 
 	public static String hash(File f, String algo) throws IOException {
 		return hash(new BufferedInputStream(new FileInputStream(f), 65536), algo);
 	}
 
 	public static String hash(File f, MessageDigest md) throws IOException {
 		return hash(new BufferedInputStream(new FileInputStream(f), 65536), md);
 	}
 
 	public static String hash(InputStream in, String algo) throws IOException {
 		try {
 			MessageDigest md = MessageDigest.getInstance(algo);
 			return hash(in, md);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public final static String hash(final InputStream in, final MessageDigest md) throws IOException {
 		try {
 			boolean done = false;
 			final byte[] buf = new byte[65536];
 			while (!done) {
 				final int length = in.read(buf);
 				if (length <= 0) {
 					done = true;
 				} else {
 					md.update(buf, 0, length);
 				}
 			}
 			return convertToHex(md.digest());
 		} finally {
 			in.close();
 		}
 	}
 
 	public static interface IHashWithLength {
 
 		public String getHash();
 
 		public long getByteCount();
 	}
 
 	public static byte[] md5HashBytesToBytes(byte[] buf) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(buf);
 			return md.digest();
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static byte[] md5HashBytesToBytes(byte[] buf, int offset, int length) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(buf, offset, length);
 			return md.digest();
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static String md5Hash(byte[] buf) {
 		try {
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			md.update(buf);
 			return convertToHex(md.digest());
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static String convertToHex(final byte[] buf) {
 		if (buf == null) {
 			return null;
 		}
 		final StringBuffer out = new StringBuffer();
 		final int n = buf.length;
 		for (int i = 0; i < n; i++) {
 			out.append(convertLowerBitsToHex((buf[i] >> 4) & 0x0f));
 			out.append(convertLowerBitsToHex(buf[i] & 0x0f));
 		}
 		return out.toString();
 	}
 
 	public final static byte[] convertFromHex(final String hex) {
 		final int n = hex.length() / 2;
 		byte[] out = new byte[n];
 		for (int i = 0; i < n; i++) {
 			out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
 		}
 		return out;
 	}
 
 	public static boolean isEven(int x) {
 		return (x & 1) == 0;
 	}
 
 	public static boolean isOdd(int x) {
 		return !isEven(x);
 	}
 
 	private static char convertLowerBitsToHex(int b) {
 		switch (b) {
 		case 0:
 			return '0';
 		case 1:
 			return '1';
 		case 2:
 			return '2';
 		case 3:
 			return '3';
 		case 4:
 			return '4';
 		case 5:
 			return '5';
 		case 6:
 			return '6';
 		case 7:
 			return '7';
 		case 8:
 			return '8';
 		case 9:
 			return '9';
 		case 10:
 			return 'a';
 		case 11:
 			return 'b';
 		case 12:
 			return 'c';
 		case 13:
 			return 'd';
 		case 14:
 			return 'e';
 		case 15:
 			return 'f';
 		}
 		throw new IllegalArgumentException("can't convert to hex character: " + b);
 	}
 
 	public static double interpolateYFromX(double x, double x0, double x1, double y0, double y1) {
 
 		if (x < x0 || x > x1) {
 			throw new IllegalArgumentException("can't extrapolate");
 		}
 		if (x0 == x1) {
 			return y0;
 		}
 		double dy = y1 - y0;
 		double dx = x1 - x0;
 		double v = y0 + (dy / dx) * (x - x0);
 		if (Double.isNaN(v)) {
 			throw new IllegalArgumentException("can't interpolate");
 		}
 
 		return v;
 	}
 
 	public static String[] splitCSVWithQuoteEscape(String line) {
 
 		SortedSet<Integer> commaIndicies = new TreeSet<Integer>();
 		Set<Integer> quoteIndicies = new HashSet<Integer>();
 		Map<Integer, Integer> priorQuotes = new HashMap<Integer, Integer>();
 
 		char[] array = line.toCharArray();
 		for (int i = 0; i < array.length; i++) {
 			switch (array[i]) {
 			case ',':
 				commaIndicies.add(i);
 				break;
 			case '"':
 			case '\'':
 				quoteIndicies.add(i);
 				break;
 			}
 			priorQuotes.put(i, quoteIndicies.size());
 		}
 
 		Iterator<Integer> it = commaIndicies.iterator();
 		while (it.hasNext()) {
 			Integer i = it.next();
 			if (isOdd(priorQuotes.get(i))) {
 				it.remove();
 			}
 		}
 
 		Integer[] commas = commaIndicies.toArray(new Integer[commaIndicies.size()]);
 
 		List<String> list = new LinkedList<String>();
 
 		if (commas.length > 0) {
 			list.add(line.substring(0, commas[0]));
 		} else {
 			list.add(line);
 		}
 
 		for (int i = 0; i < commas.length - 1; i++) {
 			list.add(line.substring(commas[i] + 1, commas[i + 1]));
 		}
 
 		if (commas.length > 0) {
 			list.add(line.substring(commas[commas.length - 1] + 1));
 		}
 
 		return list.toArray(new String[list.size()]);
 	}
 
 	public static Point centerLocation(Dimension window, int instance) {
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Dimension screenSize = toolkit.getScreenSize();
 		return new Point((screenSize.width - window.width) / 2 + 100 * (instance % 3),
 				(screenSize.height - window.height) / 2 + 100 * (instance % 3));
 	}
 
 	public static Point centerLocation(Dimension window) {
 		return centerLocation(window, 0);
 	}
 
 	public static String join(String separator, List<? extends Object> items) {
 		return join(separator, items.toArray(new Object[items.size()]));
 	}
 
 	public static <T> String join(String separator, T... items) {
 		return join(separator, 0, items);
 	}
 
 	public static <T> String join(String separator, int offset, T... items) {
 		StringBuffer buf = new StringBuffer();
 		for (int i = offset; i < items.length; i++) {
 			buf.append(items[i]);
 			if (i < items.length - 1) {
 				buf.append(separator);
 			}
 		}
 		return buf.toString();
 	}
 
 	public static void copy(InputStream in, File file) throws IOException {
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 65536);
 		try {
 			copy(in, out);
 		} finally {
 			out.close();
 		}
 	}
 
 	public static interface ICopyMonitor {
 		public void copiedMoreBytes(int n);
 	}
 
 	/**
 	 * copies the entire stream and closes input within "finally" clause
 	 */
 	public static void copy(InputStream in, OutputStream out) throws IOException {
 		copy(getReadable(in), getWriteable(out));
 	}
 
 	public static void copy(InputStream in, OutputStream out, ICopyMonitor mon) throws IOException {
 		copy(getReadable(in), getWriteable(out), mon);
 	}
 
 	public static void copy(InputStream in, IWriteable out) throws IOException {
 		copy(getReadable(in), out);
 	}
 
 	/**
 	 * copies the entire stream and closes input within "finally" clause
 	 */
 	public final static void copy(final IReadable in, final IWriteable out) throws IOException {
 		try {
 			boolean done = false;
 			final byte[] buf = new byte[65536];
 			while (!done) {
 				final int length = in.read(buf);
 				if (length <= 0) {
 					done = true;
 				} else {
 					out.write(buf, 0, length);
 				}
 			}
 		} finally {
 			in.close();
 		}
 	}
 
 	public final static void copy(final IReadable in, final IWriteable out, final ICopyMonitor mon) throws IOException {
 		try {
 			boolean done = false;
 			final byte[] buf = new byte[65536];
 			while (!done) {
 				final int length = in.read(buf);
 				if (length <= 0) {
 					done = true;
 				} else {
 					out.write(buf, 0, length);
 					mon.copiedMoreBytes(length);
 				}
 			}
 		} finally {
 			in.close();
 		}
 	}
 
 	public static void copy(InputStream in, RandomAccessFile out) throws IOException {
 		copy(in, getWriteable(out));
 	}
 
 	/**
 	 * copies the given length from in to out and DOESN'T close input stream
 	 */
 	public static void copy(InputStream in, OutputStream out, long len) throws IOException {
 		copy(getReadable(in), getWriteable(out), len);
 	}
 
 	/**
 	 * copies the given length from in to out and DOESN'T close input
 	 */
 	public static void copy(IReadable in, OutputStream out, long len) throws IOException {
 		copy(in, getWriteable(out), len);
 	}
 
 	/**
 	 * copies the given length from in to out and DOESN'T close input stream
 	 */
 	public static void copy(InputStream in, RandomAccessFile out, long len) throws IOException {
 		copy(getReadable(in), getWriteable(out), len);
 	}
 
 	private static IWriteable getWriteable(final RandomAccessFile raf) {
 		return new SimpleWritable2(raf);
 	}
 
 	public static IWriteable getWriteable(final OutputStream out) {
 		return new SimpleWritable(out);
 	}
 
 	public static IReadable getReadable(final RandomAccessFile raf) {
 		return new SimpleReadable2(raf);
 	}
 
 	private static IReadable getReadable(final InputStream in) {
 		return new SimpleReadable(in);
 	}
 
 	/**
 	 * copies the given length from in to out and DOESN'T close input stream
 	 */
 	public static void copy(IReadable in, IWriteable out, long len) throws IOException {
 		boolean done = false;
 		final byte[] buf = new byte[65536];
 		long alreadyCopied = 0;
 		while (!done && alreadyCopied < len) {
 			final long leftToRead = len - alreadyCopied;
 			final long readThisTime = leftToRead > buf.length ? buf.length : leftToRead;
 			int length = in.read(buf, 0, (int) readThisTime);
 			if (length <= 0) {
 				done = true;
 			} else {
 				alreadyCopied += length;
 				out.write(buf, 0, length);
 			}
 		}
 		if (alreadyCopied != len) {
 			throw new IOException("couldn't copy " + len + " bytes");
 		}
 	}
 
 	/**
 	 * copies the given length from in to out and DOESN'T close input stream
 	 */
 	public static void copy(RandomAccessFile in, OutputStream out, long len) throws IOException {
 		copy(getReadable(in), getWriteable(out), len);
 	}
 
 	public static void bufferedCopy(InputStream in, OutputStream out) throws IOException {
 		copy(new BufferedInputStream(in, 65536), out);
 	}
 
 	public static void copy(File from, File to) throws IOException {
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(to), 65536);
 		try {
 			copy(new BufferedInputStream(new FileInputStream(from), 65536), out);
 		} finally {
 			out.close();
 		}
 	}
 
 	public static void copy(File from, OutputStream out) throws IOException {
 		copy(new BufferedInputStream(new FileInputStream(from), 65536), out);
 	}
 
 	public static void copy(File from, OutputStream out, long len) throws IOException {
 		InputStream in = new BufferedInputStream(new FileInputStream(from), 65536);
 		try {
 			copy(in, out, len);
 		} finally {
 			in.close();
 		}
 	}
 
 	public static void copy(InputStream in, File file, long len) throws IOException {
 		OutputStream out = new BufferedOutputStream(new FileOutputStream(file), 65536);
 		try {
 			copy(in, out, len);
 		} finally {
 			out.close();
 		}
 	}
 
 	public static void copy(File from, RandomAccessFile out) throws IOException {
 		copy(new BufferedInputStream(new FileInputStream(from), 65536), out);
 	}
 
 	public static <A, B> Map<A, B> sortByValues(Map<A, B> map, final Comparator<B> comp) {
 		List<Map.Entry<A, B>> entries = new LinkedList<Entry<A, B>>(map.entrySet());
 		Collections.sort(entries, new Comparator<Map.Entry<A, B>>() {
 			@Override
 			public int compare(Entry<A, B> o1, Entry<A, B> o2) {
 				return comp.compare(o1.getValue(), o2.getValue());
 			}
 		});
 		Map<A, B> out = new LinkedHashMap<A, B>();
 		for (Map.Entry<A, B> e : entries) {
 			out.put(e.getKey(), e.getValue());
 		}
 		return out;
 	}
 
 	public static <A, B extends Comparable<B>> Map<A, B> sortByComparableValues(Map<A, B> map, boolean ascending) {
 		List<Map.Entry<A, B>> entries = new LinkedList<Entry<A, B>>(map.entrySet());
 		final int sign = ascending ? 1 : -1;
 		Collections.sort(entries, new Comparator<Map.Entry<A, B>>() {
 			@Override
 			public int compare(Entry<A, B> o1, Entry<A, B> o2) {
 				return sign * o1.getValue().compareTo(o2.getValue());
 			}
 		});
 		Map<A, B> out = new LinkedHashMap<A, B>();
 		for (Map.Entry<A, B> e : entries) {
 			out.put(e.getKey(), e.getValue());
 		}
 		return out;
 	}
 
 	public static interface IComparableRange<T> {
 		public SortedSet<T> getRange();
 	}
 
 	private static final double LOG10_2 = Math.log10(2);
 
 	public static final double log2(final double x) {
 		return Math.log10(x) / LOG10_2;
 	}
 
 	public static final double log2(final Number x) {
 		return log2(x.doubleValue());
 	}
 
 	/**
 	 * choose anything, but same seed leads to exact; different seeds give
 	 * different output
 	 */
 	public static <T> SortedMap<Integer, List<T>> splitListIntoRoughlyEqualSizePartsPseudoRandomly(long seed,
 			List<T> list, int parts) {
 
 		if (parts <= 1) {
 			return new TreeMap<Integer, List<T>>(Collections.singletonMap(1, list));
 		}
 
 		Random random = new Random(seed);
 
 		SortedMap<Integer, List<T>> map = new TreeMap<Integer, List<T>>();
 		for (int i = 0; i < parts; i++) {
 			map.put(i, new LinkedList<T>());
 		}
 
 		for (T item : list) {
 			int minIndex = random.nextInt(parts);
 			List<Integer> indicies = new LinkedList<Integer>(map.keySet());
 			Collections.shuffle(indicies, random);
 			for (Integer x : indicies) {
 				int size = map.get(x).size();
 				if (size < map.get(minIndex).size()) {
 					minIndex = x;
 				}
 			}
 			map.get(minIndex).add(item);
 		}
 
 		return map;
 	}
 
 	public static <T> List<List<T>> splitListIntoRoughlyEqualSizeParts(List<T> list, int parts) {
 		return new LinkedList<List<T>>(splitListIntoRoughlyEqualSizePartsPseudoRandomly(1, list, parts).values());
 	}
 
 	public static List<Double> getLogRange(double min, double max, int n) {
 		if (min == max && n == 1) {
 			return Collections.singletonList(min);
 		}
 
 		if (min < 0 && max < 0) {
 			return negate(getLogRange(-max, -min, n));
 		}
 
 		List<Double> out = new LinkedList<Double>();
 		double logMin = Math.log(min);
 		double logMax = Math.log(max);
 		double dx = (logMax - logMin) / (n - 1);
 		for (int i = 0; i < n; i++) {
 			double log = logMin + i * dx;
 			out.add(Math.exp(log));
 		}
 		return out;
 	}
 
 	private static List<Double> negate(List<Double> list) {
 		List<Double> out = new LinkedList<Double>();
 		for (Double x : list) {
 			out.add(-x);
 		}
 		return out;
 	}
 
 	public static List<Double> getUniformRange(double min, double max, int n) {
 		List<Double> out = new LinkedList<Double>();
 		double dx = (max - min) / (n - 1);
 		for (int i = 0; i < n; i++) {
			double log = min + i * dx;
			out.add(log);
 		}
 		return out;
 	}
 
 	public static interface ICloseableIterator<E> extends Iterator<E> {
 
 		public void close();
 
 	}
 
 	public static ICloseableIterator<String> lineIteratorForTextFile(final File f) throws IOException {
 		return lineIteratorForTextFile(f, 2048, false);
 	}
 
 	public static ICloseableIterator<String> lineIteratorForTextFile(final File f, final int bufferSize,
 			final boolean deleteWhenClosed) throws IOException {
 
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
 				new FileInputStream(f), bufferSize)));
 
 		return new ICloseableIterator<String>() {
 
 			private String nextLine;
 
 			private boolean done;
 
 			private void done() {
 				try {
 					if (!done) {
 						reader.close();
 					}
 				} catch (IOException e) {
 					logger.warnf("can't close reader for file %s: %s", f, e);
 				} finally {
 					done = true;
 				}
 			}
 
 			@Override
 			public boolean hasNext() {
 				try {
 					if (done) {
 						return false;
 					}
 					if (nextLine == null) {
 						nextLine = reader.readLine();
 						if (nextLine == null) {
 							done();
 							return false;
 						} else {
 							return true;
 						}
 					} else {
 						return true;
 					}
 				} catch (IOException e) {
 					done();
 					throw new RuntimeException(e);
 				}
 			}
 
 			@Override
 			public String next() {
 				try {
 					if (done) {
 						throw new NoSuchElementException();
 					}
 					if (nextLine == null) {
 						nextLine = reader.readLine();
 					}
 					if (nextLine == null) {
 						done();
 						throw new NoSuchElementException();
 					} else {
 						try {
 							return nextLine;
 						} finally {
 							nextLine = null;
 						}
 					}
 				} catch (IOException e) {
 					done();
 					throw new NoSuchElementException(e.toString());
 				}
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException("can't remove from file " + f);
 			}
 
 			@Override
 			public void close() {
 				try {
 					done();
 				} finally {
 					if (deleteWhenClosed) {
 						boolean deleted = f.delete();
 						if (!deleted) {
 							logger.warnf("can't delete file %s", f);
 						}
 					}
 				}
 			}
 		};
 	}
 
 	public static InputStream getResourceInputStream(Package p, String name) throws IOException {
 		return getResourceURI(p, name).toURL().openStream();
 	}
 
 	public static String getResourceAsString(Package p, String name) throws IOException {
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(
 				getResourceInputStream(p, name))));
 		try {
 			boolean done = false;
 			while (!done) {
 				String line = reader.readLine();
 				if (line == null) {
 					done = true;
 				} else {
 					pw.println(line);
 				}
 			}
 			pw.close();
 			return sw.toString();
 		} finally {
 			reader.close();
 		}
 	}
 
 	public static byte[] getResourceAsBytes(Package p, String name) throws IOException {
 		InputStream in = new BufferedInputStream(getResourceInputStream(p, name));
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		copy(in, out);
 		out.close();
 		return out.toByteArray();
 	}
 
 	public static URI getResourceURI(Package p, String name) {
 		try {
 			String resource = (p.getName()).replaceAll("\\.", "/") + "/" + name;
 			return MraUtils.class.getClassLoader().getResource(resource).toURI();
 		} catch (URISyntaxException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static void outputMemoryDebuggingInformation(Object msg) {
 		Runtime r = Runtime.getRuntime();
 		r.gc();
 		long used = r.totalMemory() - r.freeMemory();
 
 		logger.debugf("memory debugging (%s): %,15d total, " + "%,15d max," + " %,15d free, " + "%,15d used", msg,
 				r.totalMemory(), r.maxMemory(), r.freeMemory(), used);
 	}
 
 	public final static long extractLongValue(final byte[] buf) {
 		return (((long) buf[0] << 56) + ((long) (buf[1] & 255) << 48) + ((long) (buf[2] & 255) << 40)
 				+ ((long) (buf[3] & 255) << 32) + ((long) (buf[4] & 255) << 24) + ((buf[5] & 255) << 16)
 				+ ((buf[6] & 255) << 8) + ((buf[7] & 255) << 0));
 	}
 
 	public final static long extractLongValue(final byte[] readBuffer, final int offset) {
 		return (((long) readBuffer[0 + offset] << 56) + ((long) (readBuffer[1 + offset] & 255) << 48)
 				+ ((long) (readBuffer[2 + offset] & 255) << 40) + ((long) (readBuffer[3 + offset] & 255) << 32)
 				+ ((long) (readBuffer[4 + offset] & 255) << 24) + ((readBuffer[5 + offset] & 255) << 16)
 				+ ((readBuffer[6 + offset] & 255) << 8) + ((readBuffer[7 + offset] & 255) << 0));
 	}
 
 	public static final int compareLongs(final long a, final long b) {
 		if (a < b) {
 			return -1;
 		} else if (a > b) {
 			return +1;
 		} else {
 			return 0;
 		}
 	}
 
 	public static final int compareIntegers(final int a, final int b) {
 		if (a < b) {
 			return -1;
 		} else if (a > b) {
 			return +1;
 		} else {
 			return 0;
 		}
 	}
 
 	public static final int compareArrays(final byte[] a, final byte[] b) {
 		if (a == null) {
 			return -1;
 		} else if (b == null) {
 			return 1;
 		} else {
 			int min = Math.min(a.length, b.length);
 			for (int i = 0; i < min; i++) {
 				int cmp = compareBytes(a[i], b[i]);
 				if (cmp != 0) {
 					return cmp;
 				}
 			}
 			if (a.length > b.length) {
 				return +1;
 			} else if (b.length > a.length) {
 				return -1;
 			}
 			return 0;
 		}
 
 	}
 
 	public static final int compareDoubles(final double a, final double b) {
 		if (a < b) {
 			return -1;
 		} else if (a > b) {
 			return +1;
 		} else {
 			return 0;
 		}
 	}
 
 	public static final int compareBytes(final byte a, final byte b) {
 		if (a < b) {
 			return -1;
 		} else if (a > b) {
 			return +1;
 		} else {
 			return 0;
 		}
 	}
 
 	public final static UUID marshalUUIDFromBytes(final byte[] array) {
 		final long msb = extractLongValue(array);
 		final long lsb = extractLongValue(array, 8);
 		return new UUID(msb, lsb);
 	}
 
 	public final static byte[] serialiseUUID(final UUID u) {
 		final byte[] out = new byte[16];
 		writeLong(u.getMostSignificantBits(), out, 0);
 		writeLong(u.getLeastSignificantBits(), out, 8);
 		return out;
 	}
 
 	public final static void writeLong(final long v, final byte[] writeBuffer, final int offset) {
 		writeBuffer[0 + offset] = (byte) (v >>> 56);
 		writeBuffer[1 + offset] = (byte) (v >>> 48);
 		writeBuffer[2 + offset] = (byte) (v >>> 40);
 		writeBuffer[3 + offset] = (byte) (v >>> 32);
 		writeBuffer[4 + offset] = (byte) (v >>> 24);
 		writeBuffer[5 + offset] = (byte) (v >>> 16);
 		writeBuffer[6 + offset] = (byte) (v >>> 8);
 		writeBuffer[7 + offset] = (byte) (v >>> 0);
 	}
 
 	public static double calcEffectiveNumber(Collection<? extends Number> weights) {
 		double m1 = 0;
 		double m2 = 0;
 		for (Number y : weights) {
 			double x = y.doubleValue();
 			m1 += Math.abs(x);
 			m2 += x * x;
 		}
 		return m1 * m1 / m2;
 	}
 
 	private static final String BASE_DIGITS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
 
 	public static long fromOtherBaseToDecimal(int base, String number) {
 		int iterator = number.length();
 		long returnValue = 0;
 		long multiplier = 1;
 
 		while (iterator > 0) {
 			returnValue = returnValue + (BASE_DIGITS.indexOf(number.substring(iterator - 1, iterator)) * multiplier);
 			multiplier = multiplier * base;
 			--iterator;
 		}
 
 		return returnValue;
 	}
 
 	public static long decodeBase36(String x) {
 		return fromOtherBaseToDecimal(36, x);
 	}
 
 	public static final int compareByteArrays(final byte[] a, final byte[] b) {
 		if (a == null) {
 			return -1;
 		} else if (b == null) {
 			return 1;
 		} else {
 			final int min = Math.min(a.length, b.length);
 			for (int i = 0; i < min; i++) {
 				final int cmp = compareBytes(a[i], b[i]);
 				if (cmp != 0) {
 					return cmp;
 				}
 			}
 			if (a.length > b.length) {
 				return +1;
 			} else if (b.length > a.length) {
 				return -1;
 			}
 			return 0;
 		}
 
 	}
 
 	public static <K, T> Map<K, T> runIdempotentJobsWithRetries(int threads, Map<K, ? extends Callable<T>> tasks,
 			final int maxRounds) {
 		return runIdempotentJobsWithRetries(threads, tasks, maxRounds, null);
 	}
 
 	public static <K, T> Map<K, T> runIdempotentJobsWithRetries(int threads, Map<K, ? extends Callable<T>> tasks,
 			final int maxRounds, IJobListener<K, T> jobListener) {
 		ExecutorService es = Executors.newFixedThreadPool(threads);
 		try {
 			return runIdempotentJobsWithRetries(es, tasks, maxRounds, jobListener);
 		} finally {
 			es.shutdown();
 		}
 	}
 
 	public static <T> T repeatedlyTry(Callable<T> task, int maxRounds, long backoff) throws Exception {
 		List<Exception> list = new LinkedList<Exception>();
 		int round = 0;
 		while (round++ < maxRounds) {
 			try {
 				return task.call();
 			} catch (Exception e) {
 				list.add(e);
 				try {
 					Thread.sleep(backoff);
 				} catch (InterruptedException e1) {
 					e1.printStackTrace();
 				}
 			}
 		}
 		throw new Exception("can't run: " + list);
 	}
 
 	public static interface IJobListener<K, T> {
 
 		public void jobDone(K job, T result);
 
 		public void jobException(K job, Exception e);
 
 	}
 
 	public static <K, T> Map<K, T> runIdempotentJobsWithRetries(ExecutorService es,
 			Map<K, ? extends Callable<T>> tasks, final int maxRounds) {
 		return runIdempotentJobsWithRetries(es, tasks, maxRounds, null);
 	}
 
 	public static <K, T> Map<K, T> runIdempotentJobsWithRetries(ExecutorService es,
 			Map<K, ? extends Callable<T>> tasks, final int maxRounds, final IJobListener<K, T> jobListener) {
 
 		Map<K, T> out = new HashMap<K, T>();
 
 		Set<K> remainingTasks = new HashSet<K>(tasks.keySet());
 
 		long round = 0;
 
 		while (remainingTasks.size() > 0 && round++ < maxRounds) {
 
 			List<K> keys = new LinkedList<K>(remainingTasks);
 			Collections.shuffle(keys);
 
 			Map<K, Future<T>> futures = new HashMap<K, Future<T>>();
 			for (final K k : keys) {
 				final Callable<T> task = tasks.get(k);
 
 				futures.put(k, es.submit(new Callable<T>() {
 
 					@Override
 					public T call() throws Exception {
 						try {
 							T result = task.call();
 							if (jobListener != null) {
 								jobListener.jobDone(k, result);
 							}
 							return result;
 						} catch (Exception e) {
 							if (jobListener != null) {
 								jobListener.jobException(k, e);
 							}
 							throw e;
 						}
 					}
 				}));
 			}
 
 			Set<K> done = new HashSet<K>();
 
 			for (K k : futures.keySet()) {
 				try {
 					T result = futures.get(k).get();
 					out.put(k, result);
 					done.add(k);
 				} catch (Exception e) {
 					logger.warnf("exception running %s: %s", k, e);
 					e.printStackTrace();
 				}
 			}
 
 			for (K d : done) {
 				remainingTasks.remove(d);
 			}
 
 		}
 
 		if (remainingTasks.size() > 0) {
 			logger.warnf("%,d tasks not completed", remainingTasks.size());
 		}
 
 		return out;
 	}
 
 	public static double calcRadians(double x, double y) {
 
 		double theta = 0;
 
 		if (x >= 0) {
 			if (y >= 0) {
 				// QUAD 1
 				theta = Math.atan(y / x);
 			} else {
 				// QUAD 4
 				theta = 2 * Math.PI - Math.atan(-y / x);
 			}
 		} else {
 			if (y > 0) {
 				// QUAD 2
 				theta = Math.PI - Math.atan(-y / x);
 			} else {
 				// QUAD 3
 				theta = Math.PI + Math.atan(y / x);
 			}
 		}
 
 		return theta;
 	}
 
 	public static double[] calcCoords(double radians) {
 		return new double[] { Math.cos(radians), Math.sin(radians) };
 	}
 
 	public static <T> Set<T> intersection(Set<T> a, Set<T> b) {
 		Set<T> out = new HashSet<T>();
 		for (T item : a) {
 			if (b.contains(item)) {
 				out.add(item);
 			}
 		}
 		return out;
 	}
 
 	public static String getFileExtension(String name) {
 		char[] array = name.toCharArray();
 		for (int i = 0; i < 10; i++) {
 			int index = array.length - i - 1;
 			if (array[index] == '.') {
 				char[] out = new char[i];
 				System.arraycopy(array, index + 1, out, 0, out.length);
 				return new String(out);
 			}
 		}
 		return "";
 	}
 
 	public static URI displayURI(URI u) {
 		try {
 			Desktop dt = Desktop.getDesktop();
 
 			boolean display = false;
 			if (dt != null) {
 				if (dt.isSupported(Action.BROWSE)) {
 					display = true;
 				}
 			}
 
 			if (display) {
 				dt.browse(u);
 			} else {
 				logger.warnf("can't browse to %s", u);
 			}
 		} catch (Throwable e) {
 			logger.errorf("exception browsing to %s: %s", u, e);
 		}
 
 		return u;
 	}
 
 }
