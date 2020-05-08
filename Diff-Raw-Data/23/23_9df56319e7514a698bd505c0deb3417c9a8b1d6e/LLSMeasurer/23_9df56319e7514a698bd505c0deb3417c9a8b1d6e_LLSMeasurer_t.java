 package jp.thisnor.dre.lls;
 
import java.awt.Color;
 import java.awt.Graphics2D;
import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Arrays;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.imageio.ImageIO;
 
 import jp.thisnor.dre.core.FileEntry;
 import jp.thisnor.dre.core.Measurer;
 import jp.thisnor.dre.core.MeasurerPackage;
 
 public class LLSMeasurer implements Measurer {
 	/* キャッシュ保管場所 */
 	private static final File CACHE_DIR = new File("cache");
 	private static final String CACHE_FILE_PATH = "jdbc:sqlite:cache/jp.thisnor.dre.lls.cache.sqlite3";
 	private static final String TABLE_NAME = "tb_lls_cache";
 
 	private volatile boolean useCache;
 	private volatile int distType;
 	private static final int
 		DISTTYPE_MANHATTAN = 1,
 		DISTTYPE_EUCLIDEAN = 2,
 		DISTTYPE_CHEBYSHEV = 0;
 
 	private Queue<CacheEntry> storeCacheQueue;
 	private static final int PREF_QUEUE_SIZE = 64;
 
 	@Override
 	public void init(MeasurerPackage mpack) {
 		useCache = mpack.getOptionMap().get("useCache").getValue().equals(mpack.getLocalizedMessage("OPTION_USECACHE_TRUE"));
 		if (useCache) {
 			try {
 				initDB();
 				storeCacheQueue = new ConcurrentLinkedQueue<CacheEntry>();
 			} catch (SQLException e) {
 				useCache = false;
 			} catch (ClassNotFoundException e) {
 				useCache = false;
 			}
 		}
 
 		String distTypeStr = mpack.getOptionMap().get("distType").getValue();
 		distType = "Manhattan".equals(distTypeStr) ? 1 : "Chebyshev".equals(distTypeStr) ? 0 : 2;
 	}
 
 	@Override
 	public Object convert(FileEntry fileEntry) throws Exception {
 		if (useCache) {
 			/* キャッシュが存在すれば、それを利用する */
 			byte[] data = loadCache(fileEntry);
 			if (data != null) return data;
 		}
 
 		/* イメージファイル読み込み */
 		BufferedImage srcImage = null;
 		InputStream in = null;
 		try {
 			in = fileEntry.open();
 			srcImage = ImageIO.read(in);
 		} catch (IOException e) {
 			throw new IOException(String.format("Failed in reading: %s", fileEntry.getPath()), e);
 		} finally {
 			try {
 				if (in != null) in.close();
 			} catch (IOException e) {}
 		}
 		if (srcImage == null) {
 			new IOException(String.format("Failed in reading: %s", fileEntry.getPath()));
 		}
 
 		int srcWidth = srcImage.getWidth();
 		int srcHeight = srcImage.getHeight();
 
 		/* グレースケール化 */
 		BufferedImage dstImage = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_BYTE_GRAY);
 		Graphics2D g2 = dstImage.createGraphics();
		if (srcImage.getColorModel().getTransparency() != Transparency.OPAQUE) {
			System.out.println(fileEntry.getName());
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, srcWidth, srcHeight);
		}
 		g2.drawImage(srcImage, 0, 0, null);
 		g2.dispose();
		if (fileEntry.getName().endsWith(".png")) {
			if (fileEntry.getName().contains("101") || fileEntry.getName().contains("185")) {
				ImageIO.write(srcImage, "png", new File(fileEntry.getName() + ".src.png"));
				ImageIO.write(dstImage, "png", new File(fileEntry.getName() + ".dst.png"));
			}
		}
 
 		byte[] pixels = new byte[srcWidth * srcHeight];
 		dstImage.getRaster().getDataElements(0, 0, srcWidth, srcHeight, pixels);
 
 		/* スケーリング */
 		/* 幅圧縮 */
 		final int wssp = 8, wdsp = srcWidth;
 		for (int y = 0; y < srcHeight; y++) {
 			int sx = 0, nssdx = wssp, sval = pixels[y * srcWidth] & 0xff;
 			int dx = 0, ndsdx = wdsp, dval = 0;
 			for (int sdx = 0; sdx < srcWidth * 8; ) {
 				if (sdx == nssdx) {
 					++sx;
 					nssdx += wssp;
 					sval = pixels[y * srcWidth + sx] & 0xff;
 				}
 				dval += sval;
 				++sdx;
 				if (sdx == ndsdx) {
 					pixels[y * srcWidth + dx] = (byte)(dval / wdsp);
 					++dx;
 					ndsdx += wdsp;
 					dval = 0;
 				}
 			}
 		}
 
 		/* 高さ圧縮 */
 		final int hssp = 8, hdsp = srcHeight;
 		for (int x = 0; x < 8; x++) {
 			int sy = 0, nssdy = hssp, sval = pixels[x] & 0xff;
 			int dy = 0, ndsdy = hdsp, dval = 0;
 			for (int sdy = 0; sdy < srcHeight * 8; ) {
 				if (sdy == nssdy) {
 					++sy;
 					nssdy += hssp;
 					sval = pixels[sy * srcWidth + x] & 0xff;
 				}
 				dval += sval;
 				++sdy;
 				if (sdy == ndsdy) {
 					pixels[dy * 8 + x] = (byte)(dval / hdsp);
 					++dy;
 					ndsdy += hdsp;
 					dval = 0;
 				}
 			}
 		}
 
 		/* 分散正規化 */
 		int sum = 0, sum2 = 0;
 		for (int i = 0; i < 64; i++) {
 			int srcPixel = pixels[i] & 0xff;
 			sum += srcPixel;
 			sum2 += srcPixel * srcPixel;
 		}
 		int mean = sum / 64;
 		int var = (int)Math.sqrt(sum2 / 64 - sum * sum / 4096);
 		if (var == 0) var = 1;
 		for (int i = 0; i < 64; i++) {
 			int dstPixel = ((pixels[i] & 0xff) - mean) * 64 / var;
 			if (dstPixel > 127) dstPixel = 127;
 			if (dstPixel < -127) dstPixel = -127;
 			pixels[i] = (byte)dstPixel;
 		}
 
 		/* データオブジェクトを生成 */
 		byte[] data = Arrays.copyOf(pixels, 64);
 
 		if (useCache) {
 			/* キャッシュに保存する */
 			storeCacheQueue.offer(new CacheEntry(fileEntry, data));
 			if (storeCacheQueue.size() >= PREF_QUEUE_SIZE) {
 				storeCaches();
 			}
 		}
 
 		return data;
 	}
 
 	@Override
 	public int measure(Object o1, Object o2, int threshold) {
 		byte[] data1 = (byte[])o1;
 		byte[] data2 = (byte[])o2;
 		int sum = 0;
 		switch (distType) {
 		case DISTTYPE_EUCLIDEAN:
 			int th = (threshold + 1) * (threshold + 1);
 			for (int i = 0; i < 64; i++) {
 				int d = (int)data1[i] - (int)data2[i];
 				sum += d * d;
 				if (sum >= th) break;
 			}
 			return (int)Math.sqrt(sum);
 		case DISTTYPE_MANHATTAN:
 			for (int i = 0; i < 64; i++) {
 				sum += Math.abs(data1[i] - data2[i]);
 				if (sum > threshold) break;
 			}
 			return sum;
 		case DISTTYPE_CHEBYSHEV:
 			int max = 0;
 			for (int i = 0; i < 64; i++) {
 				int d = Math.abs(data1[i] - data2[i]);
 				if (d > max) max = d;
 				if (max > threshold) break;
 			}
 			return max;
 		default:
 			throw new IllegalStateException();
 		}
 	}
 
 	@Override
 	public void dispose() {
 		if (useCache) {
 			storeCaches();
 		}
 	}
 
 	private void initDB() throws SQLException, ClassNotFoundException {
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			useCache = false;
 			throw e;
 		}
 
 		if (!CACHE_DIR.exists()) {
 			CACHE_DIR.mkdirs();
 		}
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection(CACHE_FILE_PATH);
 			Statement stat = conn.createStatement();
 			stat.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(name TEXT, date INTEGER, data BLOB);");
 			stat.execute("CREATE INDEX IF NOT EXISTS id_date_size ON " + TABLE_NAME + "(name, date);");
 			conn.close();
 		} catch (SQLException e) {
 			useCache = false;
 			throw e;
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 	}
 
 	private byte[] loadCache(FileEntry entry) {
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection(CACHE_FILE_PATH);
 			Statement stat = conn.createStatement();
 			ResultSet res = stat.executeQuery(String.format(
 					"SELECT data FROM %s WHERE name = \"%s\" AND date = %s;",
 					TABLE_NAME, entry.getPath(), entry.getLastModified()));
 			if (!res.next()) return null;
 			byte[] data = res.getBytes(1);
 			if (res.next()) return null;
 			return data;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		} finally {
 			try {
 				if (conn != null) conn.close();
 			} catch (SQLException e) {}
 		}
 	}
 
 	private void storeCaches() {
 		Connection conn = null;
 		try {
 			conn = DriverManager.getConnection(CACHE_FILE_PATH);
 			conn.setAutoCommit(false);
 			CacheEntry cacheEntry = null;
 			while ((cacheEntry = storeCacheQueue.poll()) != null) {
 				PreparedStatement stat = conn.prepareStatement(String.format(
 						"INSERT INTO %s VALUES(\"%s\", %s, ?);",
 						TABLE_NAME, cacheEntry.fileEntry.getPath(), cacheEntry.fileEntry.getLastModified()));
 				stat.setBytes(1, cacheEntry.data);
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
 		private final byte[] data;
 		private CacheEntry(FileEntry fileEntry, byte[] data) {
 			this.fileEntry = fileEntry;
 			this.data = data;
 		}
 	}
 }
