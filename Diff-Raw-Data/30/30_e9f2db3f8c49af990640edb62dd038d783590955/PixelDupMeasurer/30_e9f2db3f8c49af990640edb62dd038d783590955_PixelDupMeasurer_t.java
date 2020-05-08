 package jp.thisnor.dre.pixeldup;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.ComponentSampleModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.PixelInterleavedSampleModel;
 import java.awt.image.SampleModel;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.MessageDigest;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.imageio.ImageIO;
 
 import jp.thisnor.dre.core.FileEntry;
 import jp.thisnor.dre.core.Measurer;
 import jp.thisnor.dre.core.MeasurerPackage;
 
 public class PixelDupMeasurer implements Measurer {
 	private static final File CACHE_DIR = new File("cache");
 	private static final File CACHE_FILE = new File("cache/jp.thisnor.dre.pixeldup.cache.sqlite3");
 	private static final String TABLE_NAME = "pixeldup_cache";
 
 	private volatile String algorithm;
 	private volatile boolean useCache;
 
 	private Queue<CacheEntry> storeCacheQueue;
 	private static final int PREF_QUEUE_SIZE = 64;
 
 	@Override
 	public void init(MeasurerPackage mpack) {
 		algorithm = mpack.getOptionMap().get("hashAlgorithm").getValue();
 		useCache = mpack.getOptionMap().get("useCache").getValue().equals(mpack.getLocalizedMessage("OPTION_USECACHE_TRUE"));
 		if (useCache) {
 			initDB();
 			storeCacheQueue = new ConcurrentLinkedQueue<CacheEntry>();
 		}
 	}
 
 	@Override
 	public Object convert(FileEntry fileEntry) throws Exception {
 		if (useCache) {
 			PixelDupData data = loadCache(fileEntry);
 			if (data != null) return data;
 		}
 
 		BufferedImage srcImage = null;
 		InputStream in = null;
 		try {
 			in = fileEntry.open();
 			srcImage = ImageIO.read(in);
 		} catch (IOException e) {
 			throw new IOException(String.format("Failed to read image file: %s.", fileEntry.getPath()), e);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException e) {}
 		}
 		if (srcImage == null) {
 			throw new IOException(String.format("Failed to read image file: %s.", fileEntry.getPath()));
 		}
 
 		ColorModel cm = srcImage.getColorModel();
 		SampleModel sm = srcImage.getSampleModel();
 		if (!cm.getColorSpace().isCS_sRGB() ||
 				!(sm instanceof PixelInterleavedSampleModel) ||
 				sm.getDataType() != DataBuffer.TYPE_BYTE) {
 			BufferedImage tmpImage = new BufferedImage(srcImage.getWidth(), srcImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
 			Graphics2D g2 = tmpImage.createGraphics();
 			g2.drawImage(srcImage, 0, 0, null);
 			g2.dispose();
 			srcImage = tmpImage;
 		}
 		byte[] srcPixelBytes = new byte[srcImage.getWidth() * srcImage.getHeight() * ((ComponentSampleModel)srcImage.getSampleModel()).getPixelStride()];
 		srcImage.getRaster().getDataElements(0, 0, srcImage.getWidth(), srcImage.getHeight(), srcPixelBytes);
 
 		MessageDigest md = MessageDigest.getInstance(algorithm);
 		md.update(srcPixelBytes);
 		byte[] hash = md.digest();
 
 		PixelDupData data = new PixelDupData(hash);
 
 		if (useCache) {
 //			storeCache(entry, data);
 			storeCacheQueue.offer(new CacheEntry(fileEntry, data));
 			if (storeCacheQueue.size() >= PREF_QUEUE_SIZE) {
 				storeCaches();
 			}
 		}
 
 		return data;
 	}
 
 	@Override
 	public int measure(Object o1, Object o2, int threshold) {
 		PixelDupData data1 = (PixelDupData)o1;
 		PixelDupData data2 = (PixelDupData)o2;
 		int len = Math.min(data1.hash.length, data2.hash.length);
 		int sum = 0;
 		for (int i = 0; i < len; i++) {
 			int d = (int)data1.hash[i] - (int)data2.hash[i];
 			sum += Math.abs(d);
 			if (sum > threshold) break;
 		}
 		return sum;
 	}
 
 	@Override
 	public void dispose() {
 		if (useCache) {
 			storeCaches();
 		}
 	}
 
 	private void initDB() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 
 			if (!CACHE_DIR.exists()) {
 				CACHE_DIR.mkdirs();
 			}
 			Connection conn = null;
 			try {
 				conn = DriverManager.getConnection("jdbc:sqlite:" + CACHE_FILE.getPath());
 				Statement stat = conn.createStatement();
 				stat.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(name TEXT, date INTEGER, hash BLOB);");
 				stat.execute("CREATE INDEX IF NOT EXISTS id_date_size on " + TABLE_NAME + "(name, date);");
 				conn.close();
 			} finally {
 				try {
 					if (conn != null) conn.close();
 				} catch (SQLException e) {}
 			}
 		} catch (Exception e) {
 			useCache = false;
 		}
 	}
 
 	private PixelDupData loadCache(FileEntry entry) {
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection("jdbc:sqlite:" + CACHE_FILE.getPath());
 			Statement stat = conn.createStatement();
 			ResultSet res = stat.executeQuery(String.format(
 					"SELECT hash FROM %s WHERE name = %s AND date = %s;",
 					TABLE_NAME, entry.getPath(), entry.getLastModified()));
 			if (!res.next()) return null;
 			byte[] hash = res.getBytes(1);
 			if (res.next()) return null;
 			return new PixelDupData(hash);
 		} catch (SQLException e) {
 			return null;
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 	}
 /*
 	private void storeCache(FileEntry entry, PixelDupData data) {
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection("jdbc:sqlite:" + CACHE_FILE.getPath());
 			PreparedStatement stat = conn.prepareStatement("INSERT INTO " + TABLE_NAME + " VALUES(" + entry.getLastModified() + ", " + entry.getSize() + ", ?);");
 			stat.setBytes(1, data.hash);
 			stat.execute();
 		} catch (SQLException e) {
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 	}
 */
 	private void storeCaches() {
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection("jdbc:sqlite:" + CACHE_FILE.getPath());
 			conn.setAutoCommit(false);
 			CacheEntry cacheEntry = null;
 			while ((cacheEntry = storeCacheQueue.poll()) != null) {
 				PreparedStatement stat = conn.prepareStatement(String.format(
 						"INSERT INTO %s VALUES(%s, %s, ?);",
 						TABLE_NAME, cacheEntry.fileEntry.getPath(), cacheEntry.fileEntry.getLastModified()));
 				stat.setBytes(1, cacheEntry.data.hash);
 				stat.executeUpdate();
 			}
 			conn.commit();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 	}
 
 	private static class CacheEntry {
 		private final FileEntry fileEntry;
 		private final PixelDupData data;
 		private CacheEntry(FileEntry fileEntry, PixelDupData data) {
 			this.fileEntry = fileEntry;
 			this.data = data;
 		}
 	}
 }
