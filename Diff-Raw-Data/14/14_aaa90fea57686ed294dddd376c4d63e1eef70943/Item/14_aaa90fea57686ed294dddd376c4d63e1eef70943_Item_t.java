 package itemfiler.model;
 
 import itemfiler.Photomanager;
 import itemfiler.util.FileUtils;
 import itemfiler.util.ImageUtils;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 
 import com.drew.imaging.ImageMetadataReader;
 import com.drew.imaging.ImageProcessingException;
 import com.drew.metadata.Metadata;
 import com.drew.metadata.exif.ExifSubIFDDirectory;
 
 
 public class Item {
 
 	private static Map<Integer, Item> cache = new HashMap<Integer, Item>();
 
 	// ######### STATICS #########
 	public static void create(String path) {
 		try {
 			// move it to internal storage
 			File file = new File(path);
 			File target = new File(Photomanager.home.getAbsolutePath()
 					+ File.separatorChar
 					+ "data"
 					+ File.separatorChar + file.getName());
 
 			if (!target.getParentFile().exists())
 				target.getParentFile().mkdirs();
 
 			for(int i = 0; i < 100 && target.exists(); i++)
 				target = new File(target.getParent() + File.separatorChar + i
 						+ file.getName());
 
 			Files.move(file.toPath(), target.toPath());
 
			Database.execute("INSERT INTO objects (path) VALUES ('"
 					+ FileUtils.getRelativePath(Photomanager.home, target)
 					+ "')");
 
 			// add to cache
 			int newId = Database.getInteger("SELECT MAX(oid) FROM objects");
 			cache.put(newId, new Item(newId));

			Database.execute("UPDATE objects SET creationDate='"
					+ new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
							.format(getCreationDate(target))
					+ "' WHERE oid = '" + newId + "'");
		} catch (SQLException | IOException | NullPointerException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static Collection<Item> getAll() {
 		updateCache();
 
 		return cache.values();
 	}
 
 	public static Collection<Item> getFiltered(Filter filter) {
 		if (filter.isShowAll())
 			return getAll();
 
 		updateCache();
 
 		try {
 			List<Integer> ids = null;
 			if (0 < filter.getTags().size()) {
 				for (Entry<String, Collection<String>> currentEntry : filter
 						.getTags().entrySet()) {
 					String sql = "SELECT oid FROM objects_tags JOIN tags ON objects_tags.tid=tags.tid";
 
 					if (0 < currentEntry.getValue().size())
 						sql += " WHERE";
 
 					for (String current : currentEntry.getValue())
 						sql += " tags.name LIKE '" + current + "%' OR";
 
 					if (sql.endsWith("OR"))
 						sql = sql.substring(0, sql.length() - 3);
 
 					if (null == ids) {
 						ids = new ArrayList<>();
 						ids.addAll(Database.getIntegerList(sql));
 					} else
 						ids.retainAll(Database.getIntegerList(sql));
 				}
 
 			}
 
 			if (!filter.getDates().isEmpty()) {
 				String sql = "SELECT oid FROM objects WHERE creationDate LIKE '"
 						+ filter.getDates().iterator().next()
 						.substring(5) + "%'";
 				if(null == ids) {
 					ids = new ArrayList<>();
 					ids.addAll(Database.getIntegerList(sql));
 				} else
 					ids.retainAll(Database.getIntegerList(sql));
 			}
 
 			if (null == ids)
 				ids = new ArrayList<>();
 
 			if (filter.isIncludeUntagged())
 				ids.addAll(Database
 						.getIntegerList("SELECT oid FROM objects WHERE oid NOT IN (SELECT oid FROM objects_tags)"));
 
 			List<Integer> trashed = Database
 					.getIntegerList("SELECT oid FROM objects WHERE trash IS TRUE");
 			if (filter.isIncludeTrash())
 				ids.addAll(trashed);
 			else
 				ids.removeAll(trashed);
 
 			HashMap<Integer, Item> result = new HashMap<Integer, Item>(cache);
 			result.keySet().retainAll(ids);
 			return result.values();
 
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	private static void updateCache() {
 		try {
 			List<Integer> allIds = Database
 					.getIntegerList("SELECT oid FROM objects");
 			allIds.removeAll(cache.keySet());
 
 			for (int currentId : allIds)
 				cache.put(currentId, new Item(currentId));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static Date getCreationDate(File path) {
 		try {
 			Metadata metaData = ImageMetadataReader.readMetadata(path);
 			if (metaData.containsDirectory(ExifSubIFDDirectory.class)) {
 				return metaData.getDirectory(ExifSubIFDDirectory.class)
 						.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
 			}
 		} catch (ImageProcessingException | IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return new Date();
 	}
 
 	// ####### NON-STATICS #######
 	private int id;
 	private File path;
 	private int boundingBox = 100;
 	private Image cachedImage;
 	private boolean portrait;
 
 	private Item(int currentId) {
 		id = currentId;
 		try {
 			path = FileUtils.recreateAbsolutPath(
 					Photomanager.home,
 					Database.getString("SELECT path FROM objects WHERE oid = "
 							+ currentId));
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public File getPath() {
 		return path;
 	}
 
 	public String getCreationDate() {
 		try {
 			return Database
 					.getString("SELECT creationDate FROM objects WHERE oid = '"
 							+ id + "'");
 		} catch (SQLException e) {
 			return "";
 		}
 	}
 
 	public Image getImage() {
 		return ImageUtils.load(path);
 	}
 
 	public Image getThumbnail() {
 		if (null == cachedImage) {
 			Image fullImage = ImageUtils.load(path);
 			int width = fullImage.getBounds().width;
 			int height = fullImage.getBounds().height;
 
 			// limit caching sizes to 100%
 			int maxDimensions = Math.max(width, height);
 			if (boundingBox > maxDimensions)
 				boundingBox = maxDimensions;
 
 			// scale
 			int newheight = boundingBox;
 			int newwidth = boundingBox;
 			if (width > height) {
 				newheight = (int) (1.0 * boundingBox / width * height);
 			} else {
 				newwidth = (int) (1.0 * boundingBox / height * width);
 			}
 
 			cachedImage = new Image(Display.getCurrent(), newwidth, newheight);
 			GC gc = new GC(cachedImage);
 			gc.setAdvanced(true);
 			// gc.setAntialias(SWT.ON); // is about 10% slower if activated
 			gc.drawImage(fullImage, 0, 0, fullImage.getBounds().width,
 					fullImage.getBounds().height, 0, 0, newwidth, newheight);
 			gc.dispose();
 
 			if (cachedImage.getBounds().height > cachedImage.getBounds().width)
 				setPortrait(true);
 		}
 		return cachedImage;
 	}
 
 	private void setPortrait(boolean b) {
 		portrait = b;
 	}
 
 	public boolean isPortrait() {
 		return portrait;
 	}
 
 	public String getName() {
 		try {
 			String tmp = Database
 					.getString("SELECT name FROM objects WHERE oid = "
 					+ id);
 			return null == tmp ? path.getName() : tmp;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return "error";
 	}
 
 	public void setName(String newName) {
 		try {
 			Database.execute("UPDATE objects SET name='" + newName
 					+ "' WHERE oid='" + id + "'");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void addTags(List<String> tags) {
 		for (String current : tags)
 			addTag(current);
 	}
 
 	public void addTag(String tag) {
 		try {
 			int tid = Database.getInteger("SELECT tid FROM tags WHERE name='"
 					+ tag
 					+ "'");
 			Database.execute("INSERT INTO objects_tags (oid, tid) VALUES ("
 					+ id + "," + tid + ")");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public List<String> getTags() {
 		try {
 			return Database
 					.getStringList("SELECT name FROM tags JOIN objects_tags ON tags.tid=objects_tags.tid WHERE objects_tags.oid='"
 							+ id + "'");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public void removeTag(String tag) {
 		int tid;
 		try {
 			tid = Database.getInteger("SELECT tid FROM tags WHERE name='" + tag
 					+ "'");
 			Database.execute("DELETE FROM objects_tags WHERE tid='" + tid + "'");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void moveToTrash() {
 		try {
 			Database.execute("UPDATE objects SET trash = TRUE WHERE oid='" + id
 					+ "'");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
