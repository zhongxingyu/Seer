 package rogatkin.music_barrel.model;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.Date;
 import java.util.HashMap;
 
 import javax.sql.DataSource;
 
 import org.aldan3.data.DODelegator;
 import org.aldan3.data.DOService;
 import org.aldan3.model.Log;
 import org.aldan3.model.ProcessException;
 import org.aldan3.util.DataConv;
 
 import photoorganizer.formats.MP3;
 import photoorganizer.formats.MediaFormatFactory;
 
 import mediautil.gen.MediaFormat;
 import mediautil.gen.MediaInfo;
 
 import rogatkin.music_barrel.ctrl.Navigator;
 import rogatkin.music_barrel.srv.MediaCrawler;
 import rogatkin.music_barrel.srv.PlayerService;
 
 import com.beegman.webbee.model.AppModel;
 
 public class MBModel extends AppModel implements Name {
 
 	private mb_setting settings;
 	private HashMap<String, Object> preserve;
 
 	@Override
 	public String getAppName() {
 		return "music-barrel";
 	}
 
 	public String getCharEncoding() {
 		if (settings.char_encoding != null)
 			return settings.char_encoding;
 		return "UTF-8";
 	}
 
 	@Override
 	protected void deactivateServices() {
 		PlayerService ps = (PlayerService) unregister(PlayerService.NAME);
 		ps.terminate();
 		
 		MediaCrawler mc = (MediaCrawler) unregister(MediaCrawler.NAME);
 		if (mc != null)
 			mc.shutdown();
 		try {
 			saveSettings();
 		} catch (ProcessException e) {
 			Log.l.error("Save settings", e);
 		}
 		super.deactivateServices();
 		preserve.clear();
 	}
 
 	public void saveSettings() throws ProcessException {
 		//System.err.printf("--->Object: %s%n", new DODelegator(settings, null, "", "id").get("output_type").getClass());
 		getDOService().addObject(new DODelegator(settings, null, "", "id"), null,
 				new DODelegator(settings, null, "", "id"));
 	}
 
 	@Override
 	protected String getServletName() {
 		return super.getServletName();
 	}
 
 	@Override
 	protected void initServices() {
 		preserve = new HashMap<>();
 		super.initServices();
 		settings = new mb_setting(this);
 		settings.id = 1;
 		try {
 			getDOService().getObjectLike(new DODelegator(settings, null, "", "id") {
 				@Override
 				protected String normilizeFieldName(String fieldName) {
 					return fieldName.toUpperCase();
 				}
 			});
 			preserveSate(settings.last_directory, Navigator.class.getName());
 		} catch (ProcessException e) {
 			Log.l.error("Load settings", e);
 		}
 		register(new PlayerService(this));
 		if (settings.perform_scan)
 			register(new MediaCrawler(this));
 	}
 
 	@Override
 	protected DOService createDataService(DataSource datasource) {
 		return new DOService(datasource) {
 			@Override
 			protected int getInsertUpdateVariant() {
 				return 2;
 			}
 			@Override
 			protected boolean isCreateIndex() {
 				return true;
 			}
 		};
 	}
 
 	@Override
 	public Behavior getCommonBehavior() {
 		return new Behavior();
 	}
 
 	public PlayerService getPlayer() {
 		return (PlayerService) getService(PlayerService.NAME);
 	}
 
 	public mb_setting getSettings() {
 		return settings;
 	}
 	
 	synchronized public <T> T preserveSate(T state, String name) {
 		T oldState = null;
 		// TODO generally can be stored on session level but since no sessions
 		if (preserve.containsKey(name)) {
 			oldState = (T) preserve.remove(name);
 		}
 		preserve.put(name, state);
 		return oldState;
 	}
 	
 	synchronized public <T>  T  getState(String name, T defVal) {
 		if (preserve.containsKey(name)) 
 		return (T) preserve.get(name);
 		return defVal;	
 	}
 
 	public void addToPlayList(mb_media_item item, String listName) throws MBError {
 		MediaFormat mf = MediaFormatFactory.createMediaFormat(getItemPath(item.path).toFile(), getCharEncoding(), true);
 		if (mf == null || mf.isValid() == false)
 			throw new MBError("Ivalid format :" + item);
 		mb_play_list pl = new mb_play_list(this);
 		if (listName == null || listName.isEmpty())
 			pl.title = ON_THE_GO;
 		else
 			pl.title = listName;
 		DOService dos = getDOService();
 		// TODO synchronize playlist creation to avoid multiple
 		try {
 			// TODO use create or update
 			dos.getObjectLike(new DODelegator(pl, null, "", "title") {
 				@Override
 				protected String normilizeFieldName(String fieldName) {
 					return fieldName.toUpperCase();
 				}
 			});
 			if (pl.id <= 0) {
 				pl.created_on = new Date();
 				pl.modified_on = pl.created_on;
 				dos.addObject(new DODelegator(pl, null, "", "id"), "id");
 			}
 			item = addToLibrary(mf);
 			//log("Object %s for path %s", null, oo, li.path);
 			if (item.id <= 0) {
 				throw new MBError("Can't add item "+mf +" to library");
 			}
 			mb_play_list_map plm = new mb_play_list_map(this);
 			plm.item_id = item.id;
 			plm.list_id = pl.id;
 			plm.related_on = new Date();
 			if (getSettings().allow_duplicates)
 				dos.addObject(new DODelegator(plm));
 			else
 				dos.addObject(new DODelegator(plm, null, "item_id,list_id", "id"), null, new DODelegator(plm, null, "", "item_id,list_id"));
 		} catch (Exception e) {
 			throw new MBError("Add item to list error", e);
 		}
 	}
 
 	public mb_media_item addToLibrary(MediaFormat mf) throws MBError {
 		mb_media_item item = new mb_media_item(this);
 		mb_media_set set = new mb_media_set(this);
 		fillMediaModel(item, set, mf.getMediaInfo());
 		item.losed = "FLACWVM4AAPE".indexOf(mf.getDescription()) < 0;
 		item.path = mf.getFile().toPath().toString();
 		DODelegator dod;
 		try {
 			if (set.title != null && set.title.isEmpty() == false) {
 				getDOService().addObject(dod = new DODelegator(set, null, "", "id,title,subset_num"), "id", new DODelegator(set, null, "id", "title,subset_num"));
 				if (set.id == 0) {
 					getDOService().getObjectLike(dod = new DODelegator(set, null, "num_subsets,studio,year", "title,subset_num") {
 						@Override
 						protected String normilizeFieldName(String fieldName) {
 							return fieldName.toUpperCase();
 						}			
 					});
 				}
 				item.set_id = set.id;
 			}
 		} catch (ProcessException e) {
 			throw new MBError("Add set to library error: " + mf, e);
 		};
 		try {
 			getDOService().addObject(new DODelegator(item, null, "path", "id"), "id", dod = new DODelegator(item, null, "", "path") {
 				@Override
 				protected String normilizeFieldName(String fieldName) {
 					return fieldName.toUpperCase();
 				}
 			});
 			if (item.id <= 0) {
 				if (getDOService().getObjectLike(dod) == null)
 					throw new MBError("Can't resolve item at "+item.path); 
 			}
 		} catch (Exception e) {
 			throw new MBError("Add item to library error: " + mf, e);
 		}
 		return item;
 	}
 	
 
 	public static void fillMediaModel(mb_media_item mi, MediaInfo info) {
 		fillMediaModel(mi, null, info);
 	}
 
 	public static void fillMediaModel(mb_media_item mi, mb_media_set set, MediaInfo info) {
 		mi.title = (String) info.getAttribute(MediaInfo.TITLE);
 		mi.performer = (String) info.getAttribute(MediaInfo.ARTIST);
 		mi.track = intValue(info.getAttribute(MediaInfo.TRACK));
 		mi.year = intValue(info.getAttribute(MediaInfo.YEAR));
 		mi.genre = DataConv.ifNull(MP3.findGenre(info), "Unknown");
 		mi.added_on = new Date();
 		if (set != null) {
 			set.title = (String) info.getAttribute(MediaInfo.ALBUM);
 			String partofset = (String) info.getAttribute(MediaInfo.PARTOFSET);
 			if (partofset != null && !partofset.isEmpty()) {
 				String[] parts = partofset.split("/");
 				set.subset_num = intValue(parts[0]);
 				if (parts.length > 1)
 					set.num_subsets = intValue(parts[1]);
 			}
 			set.studio = (String) info.getAttribute(MediaInfo.PUBLISHER);
 			set.year = intValue(info.getAttribute(MediaInfo.YEAR));
 		}
 	}
 
 	public static Path getItemPath(String path) {
 		if (path != null) {
 			Path result = Paths.get(path);
 			if (Files.isRegularFile(result) && Files.isReadable(result))
 				return result;
 		}
 		return null;
 	}
 
 	public static int intValue(Object o) {
 		if (o instanceof Number)
 			return ((Number) o).intValue();
 		else if (o instanceof String)
 			try {
 				return Integer.valueOf((String) o).intValue();
 			} catch (NumberFormatException e) {
 		
 			}
 		return 0;
 	}
 }
