 package at.photoselector.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.stream.ImageInputStream;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 
 import at.photoselector.Settings;
 import at.photoselector.Workspace;
 
 public class Photo {
 
 	// ################################ STATICS ################################
 
 	public final static int UNPROCESSED = 1 << 0;
 	public final static int ACCEPTED = 1 << 1;
 	public final static int DECLINED = 1 << 2;
 
 	private static Database database;
 	private static Map<Integer, Photo> cache;
 
 	public static void init(Database db) {
 		cache = new HashMap<Integer, Photo>();
 		database = db;
 	}
 
 	public static void create(File path) {
 
 		try {
 			database.execute("INSERT INTO photos (path, status) VALUES ('"
 				+ path.getAbsolutePath() + "', " + UNPROCESSED + ")");
 
 			// add to cache
 			int newId = database.getInteger("SELECT MAX(pid) FROM photos");
 			cache.put(newId, new Photo(newId, path, UNPROCESSED));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static List<Photo> getFiltered(boolean stageless, int filter) {
 		updateCache();
 
 		try {
 			String sql = "SELECT pid FROM photos";
 
 			String stage = "";
 			if (stageless)
 				stage += "stage IS NULL";
 
 			String status = "";
 			if ((UNPROCESSED & filter) > 0)
 				status += "status = " + UNPROCESSED + " OR ";
 			if ((ACCEPTED & filter) > 0)
 				status += "status = " + ACCEPTED + " OR ";
 			if ((DECLINED & filter) > 0)
 				status += "status = " + DECLINED + " OR ";
 
 			if (0 < stage.length() || 0 < status.length())
 				sql += " WHERE ";
 
 			if (0 < status.length())
 				sql += stage;
 
 			if (0 < stage.length() && 0 < status.length())
 				sql += " AND ";
 
 			if (0 < status.length())
 				sql += "(" + status.substring(0, status.length() - 4)
 						+ ")";
 
 			// TODO find better way
 			List<Photo> result = new ArrayList<Photo>();
 			for (int current : database.getIntegerList(sql))
 				result.add(cache.get(current));
 
 			return result;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static Photo get(int id) {
 		updateCache();
 		return cache.get(id);
 	}
 
 	private static void updateCache() {
 		try {
 			List<Integer> allIds = database
 					.getIntegerList("SELECT pid FROM photos");
 			allIds.removeAll(cache.keySet());
 
 			for (int currentId : allIds)
 				cache.put(
 						currentId,
 						new Photo(
 								currentId,
 								new File(
 										database.getString("SELECT path FROM photos WHERE pid = "
 												+ currentId)),
 								database.getInteger("SELECT status FROM photos WHERE pid = "
 										+ currentId),
 								Stage.get(database
 										.getInteger("SELECT stage FROM photos WHERE pid = "
 												+ currentId))));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	// ############################## NON-STATICS ##############################
 
 	private int id;
 	private File path;
 	private int status;
 	private Stage stage = null;
 	private File cacheDir;
 	private String delimiter;
 	private int width = 0;
 	private int height = 0;
 	private final Map<File, Image> imageCache = new HashMap<File, Image>();
 	private int section;
 
 	public Photo(int newId, File path, int status) {
 		id = newId;
 		this.path = path;
 		this.status = status;
 
 		delimiter = System.getProperty("file.separator");
 		cacheDir = new File(Workspace.getLocation().getParent() + delimiter
 				+ ".cache" + delimiter);
 		if (!cacheDir.exists())
 			cacheDir.mkdir();
 	}
 
 	public Photo(int currentId, File path, int status, Stage stage) {
 		this(currentId, path, status);
 		this.stage = stage;
 	}
 
 	public boolean isRaw() {
 		return getPath().getName().toLowerCase().matches(".*cr2$");
 	}
 
 	private File preprocessRawImage() {
 		File cachedFullImage = new File(cacheDir.getPath() + delimiter
 				+ path.getName() + ".full.jpg");
 		if (!cachedFullImage.exists()) {
 			try {
 				Process p = Runtime.getRuntime().exec(
 						Settings.getDCRawLocation() + " -w " + // Use camera
 																// white
 																// balance, if
 																// possible
 						"-T " + // Write TIFF instead of PPM
 								// "-j " + // Don't stretch or rotate raw pixels
 								// "-W " + // Don't automatically brighten the
 								// image);
 						getPath().getAbsolutePath()
 				);
 				p.waitFor();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			File tiffImagePath = new File(getPath().getAbsolutePath()
 					.replace("CR2", "tiff"));
 
 			// use imagemagic to convert to jpg
 			try {
 				Process p = Runtime.getRuntime().exec(
 						"convert " + tiffImagePath.getAbsolutePath() + " "
 								+ cachedFullImage.getAbsolutePath());
 				p.waitFor();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			tiffImagePath.delete();
 		}
 
 		return cachedFullImage;
 	}
 
 	private Image getCachedImage(int level) {
 		int cachedSize = getSize(level);
 
 		File cachedImageLocation = new File(cacheDir.getPath() + delimiter
 				+ path.getName() + "." + cachedSize + ".jpg");
 
 		if (!cachedImageLocation.exists()) {
 			File fullImage;
 			if (isRaw()) {
 				fullImage = preprocessRawImage();
 			} else
 				fullImage = getPath();
 
 			try {
 				Process p = Runtime.getRuntime().exec(
 						"convert -verbose " + fullImage + " -resize "
 								+ cachedSize + "x" + cachedSize + " "
 								+ cachedImageLocation.getAbsolutePath());
 				p.waitFor();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		Image cachedImage = imageCache.get(cachedImageLocation);
 		if (null == cachedImage) {
 			cachedImage = new Image(Display.getCurrent(),
 					cachedImageLocation.getAbsolutePath());
 			imageCache.put(cachedImageLocation, cachedImage);
 		}
 
 		return cachedImage;
 	}
 
 	private int getCacheLevel(int boundingBox) {
 		if (100 >= boundingBox)
 			return 0;
 		int full = Math.max(getDimensions().x, getDimensions().y) - 100;
 		section = full / 5; // make 5 a configurable number
 		return boundingBox / section + 1;
 	}
 
 	private int getSize(int cacheLevel) {
 		if (0 > cacheLevel)
 			cacheLevel = 0;
 		int size = 100 + cacheLevel * section;
 		int fullsize = Math.max(getDimensions().x, getDimensions().y);
 		return size <= fullsize ? size : fullsize;
 	}
 
 	public Image getImage(int boundingBox) {
 		Image cached = getCachedImage(getCacheLevel(boundingBox));
 		Rectangle dimensions = scaleAndCenterImage(boundingBox);
 		Image result = new Image(Display.getCurrent(), dimensions.width,
 				dimensions.height);
 		GC gc = new GC(result);
 		gc.setAntialias(SWT.ON);
 		gc.drawImage(cached, 0, 0, cached.getBounds().width,
 				cached.getBounds().height, 0, 0, dimensions.width,
 				dimensions.height);
 		gc.dispose();
 
 		return result;
 	}
 
 	public void preCacheNeighbors(int boundingBox) {
 		getCachedImage(getCacheLevel(boundingBox) + 1);
 		getCachedImage(getCacheLevel(boundingBox) - 1);
 	}
 
 	public void clearCachedImages() {
 		for (Image current : imageCache.values())
 			current.dispose();
 		imageCache.clear();
 	}
 
 	public Rectangle scaleAndCenterImage(int boundingBox) {
 		getDimensions();
 
 		Rectangle result = new Rectangle(0, 0, boundingBox, boundingBox);
 
 		// scale
 		if (width > height) {
 			result.height = (int) (1.0 * boundingBox / width * height);
 			result.y = (int) (1.0 * ((boundingBox - result.height) / 2));
 		} else {
 			result.width = (int) (1.0 * boundingBox / height * width);
 			result.x = (int) (1.0 * ((boundingBox - result.width) / 2));
 		}
 
 		return result;
 	}
 
 	public Point getDimensions() {
 		if (width == 0 || height == 0) {
 			File resourceFile = null;
 			if (isRaw())
 				resourceFile = preprocessRawImage();
 			else
 				resourceFile = getPath();
 
 			ImageInputStream in = null;
 			try {
 				in = ImageIO.createImageInputStream(resourceFile);
				final Iterator<ImageReader> readers = ImageIO
						.getImageReaders(in);
 				if (readers.hasNext()) {
 					ImageReader reader = (ImageReader) readers.next();
 					try {
 						reader.setInput(in);
 						width = reader.getWidth(0);
 						height = reader.getHeight(0);
 					} finally {
 						reader.dispose();
 					}
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				if (in != null)
 					try {
 						in.close();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 			}
 		}
 
 		return new Point(width, height);
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public File getPath() {
 		return path;
 	}
 
 	public Stage getStage() {
 		return stage;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public void setStatus(int status) {
 		try {
 			database.execute("UPDATE photos SET status=" + status
 					+ " WHERE pid=" + getId());
 			this.status = status;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void setStage(Stage stage) {
 		try {
 			database.execute("UPDATE photos SET stage=" + stage.getId()
 					+ " WHERE pid=" + getId());
 			this.stage = stage;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
