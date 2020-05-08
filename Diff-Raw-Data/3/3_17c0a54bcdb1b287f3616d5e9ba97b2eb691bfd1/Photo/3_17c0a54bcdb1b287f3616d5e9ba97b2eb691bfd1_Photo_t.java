 package at.photoselector.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 
 import at.photoselector.Settings;
 import at.photoselector.Workspace;
 import at.photoselector.util.ImageUtils;
 
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
 			// allIds.removeAll(cache.keySet());
 
 			for (int currentId : allIds) {
 				Photo fresh = new Photo(
 						currentId,
 						new File(
 								database.getString("SELECT path FROM photos WHERE pid = "
 										+ currentId)),
 						database.getInteger("SELECT status FROM photos WHERE pid = "
 								+ currentId),
 						Stage.get(database
 								.getInteger("SELECT stage FROM photos WHERE pid = "
 										+ currentId)));
 
 				// check if cache is up to date
 				if (!fresh.equals(cache.get(currentId))) {
 					// if not - recreate
 					cache.put(currentId, fresh);
 				}
 			}
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
 	private final SortedMap<Integer, Image> imageCache = new TreeMap<Integer, Image>();
 	private boolean portrait = false;
 	private Image fullImage;
 
 	private Photo(int newId, File path, int status) {
 		id = newId;
 		this.path = path;
 		this.status = status;
 
 		delimiter = System.getProperty("file.separator");
 		cacheDir = new File(Workspace.getLocation().getParent() + delimiter
 				+ ".cache" + delimiter);
 		if (!cacheDir.exists())
 			cacheDir.mkdir();
 	}
 
 	private Photo(int currentId, File path, int status, Stage stage) {
 		this(currentId, path, status);
 		this.stage = stage;
 	}
 
 	public boolean isRaw() {
 		return !getPath().getName().toLowerCase().matches(".*jpe?g$");
 	}
 
 	private File preprocessRawImage() {
 		if (!isRaw())
 			return getPath();
 
 		File cachedFullImage = new File(cacheDir.getPath() + delimiter
 				+ path.getName() + ".full.jpg");
 		if (!cachedFullImage.exists()) {
 			try {
 				Process p = Runtime.getRuntime().exec(
 						new String[] { Settings.getDCRawLocation(), "-w", "-T",
 								getPath().getAbsolutePath() });
 				p.waitFor();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			File tiffImagePath = new File(getPath().getAbsolutePath()
					.replaceFirst(
					"[a-zA-Z0-9]+$", "tiff"));
 
 			// use imagemagic to convert to jpg
 			try {
 				Process p = Runtime.getRuntime().exec(
 						new String[] { Settings.getImageMagicBinaryLocation(),
 								tiffImagePath.getAbsolutePath(),
 								cachedFullImage.getAbsolutePath() });
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
 
 	private Image getCachedImage(int boundingBox) {
 		Image cachedImage;
 		try {
 
 			// limit caching sizes to 100%
 			int maxDimensions = Math.max(getDimensions().x, getDimensions().y);
 			if (boundingBox > maxDimensions)
 				boundingBox = maxDimensions;
 
 			System.out.println("request: " + boundingBox);
 
 			boundingBox = imageCache.subMap(boundingBox,
 					(int) (boundingBox * 1.1))
 					.firstKey();
 
 			cachedImage = imageCache.get(boundingBox);
 			System.out.println("cache hit: got " + boundingBox);
 		} catch (Exception e) {
 			cacheFullImage();
 
 			Rectangle dimensions = scaleAndCenterImage(boundingBox);
 			cachedImage = new Image(Display.getCurrent(), dimensions.width,
 					dimensions.height);
 			GC gc = new GC(cachedImage);
 			gc.setAdvanced(true);
 			gc.setAntialias(SWT.ON); // is about 10% slower if activated
 			gc.drawImage(fullImage, 0, 0, fullImage.getBounds().width,
 					fullImage.getBounds().height, 0, 0, dimensions.width,
 					dimensions.height);
 			gc.dispose();
 			imageCache.put(boundingBox, cachedImage);
 		}
 
 		if (cachedImage.getBounds().height > cachedImage.getBounds().width)
 			setPortrait(true);
 
 		return cachedImage;
 	}
 
 	private void cacheFullImage() {
 		// cache full image
 		if (null == fullImage) {
 			File imagePath;
 			if (isRaw())
 				imagePath = preprocessRawImage();
 			else
 				imagePath = path;
 			fullImage = ImageUtils.load(imagePath);
 		}
 	}
 
 	private void setPortrait(boolean b) {
 		portrait = b;
 	}
 
 	public boolean isPortrait() {
 		return portrait;
 	}
 
 	public Image getImage(int boundingBox) {
 		return getCachedImage(boundingBox);
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
 			cacheFullImage();
 
 			width = fullImage.getBounds().width;
 			height = fullImage.getBounds().height;
 		}
 
 		if (isPortrait() && width > height) {
 			int tmp = width;
 			width = height;
 			height = tmp;
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
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((cacheDir == null) ? 0 : cacheDir.hashCode());
 		result = prime * result + id;
 		result = prime * result + ((path == null) ? 0 : path.hashCode());
 		result = prime * result + ((stage == null) ? 0 : stage.hashCode());
 		result = prime * result + status;
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Photo other = (Photo) obj;
 		if (cacheDir == null) {
 			if (other.cacheDir != null)
 				return false;
 		} else if (!cacheDir.equals(other.cacheDir))
 			return false;
 		if (id != other.id)
 			return false;
 		if (path == null) {
 			if (other.path != null)
 				return false;
 		} else if (!path.equals(other.path))
 			return false;
 		if (stage == null) {
 			if (other.stage != null)
 				return false;
 		} else if (!stage.equals(other.stage))
 			return false;
 		if (status != other.status)
 			return false;
 		return true;
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		fullImage.dispose();
 		super.finalize();
 	}
 
 }
