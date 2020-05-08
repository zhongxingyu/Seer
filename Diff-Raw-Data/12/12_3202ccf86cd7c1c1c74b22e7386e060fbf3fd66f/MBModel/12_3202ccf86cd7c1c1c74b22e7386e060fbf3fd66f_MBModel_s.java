 package rogatkin.music_barrel.model;
 
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Date;
 
 import javax.sql.DataSource;
 
 import org.aldan3.data.DODelegator;
 import org.aldan3.data.DOService;
 import org.aldan3.model.ProcessException;
 
 import photoorganizer.formats.MP3;
 import photoorganizer.formats.MediaFormatFactory;
 
 import mediautil.gen.MediaFormat;
 import mediautil.gen.MediaInfo;
 
 import rogatkin.music_barrel.srv.MediaCrawler;
 import rogatkin.music_barrel.srv.PlayerService;
 
 import com.beegman.webbee.model.AppModel;
 
 public class MBModel extends AppModel implements Name {
 
 	private mb_setting settings;
 
 	@Override
 	public String getAppName() {
 		return "music-barrel";
 	}
 
 	public String getCharEncoding() {
 		return "UTF-8";
 	}
 
 	@Override
 	protected void deactivateServices() {
 		PlayerService ps = (PlayerService) unregister(PlayerService.NAME);
 		ps.stopAll();
 		MediaCrawler mc = (MediaCrawler) unregister(MediaCrawler.NAME);
 		if (mc != null)
 			mc.shutdown();
 		try {
 			saveSettings();
 		} catch (ProcessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		super.deactivateServices();
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
 		} catch (ProcessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
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
 
 	public void addToPlayList(mb_media_item item, String listName) throws MBError {
 		MediaFormat mf = MediaFormatFactory.createMediaFormat(getItemPath(item.path).toFile(), getCharEncoding(), true);
 		if (mf == null || mf.isValid() == false)
 			throw new MBError("Ivalid format :" + item);
 		mb_play_list pl = new mb_play_list(this);
 		if (listName == null || listName.isEmpty())
 			pl.title = "On The Go";
 		else
 			pl.title = listName;
 		DOService dos = getDOService();
 		try {
 			dos.getObjectLike(new DODelegator(pl, null, "", "title") {
 				@Override
 				protected String normilizeFieldName(String fieldName) {
 					return fieldName.toUpperCase();
 				}
 			});
 			if (pl.id <= 0) {
 				pl.created_on = new Date();
 				dos.addObject(new DODelegator(pl, null, "", "id"), "id");
 			}
 			dos.getObjectLike(new DODelegator(item, null, "", "path") {
 				@Override
 				protected String normilizeFieldName(String fieldName) {
 					return fieldName.toUpperCase();
 				}
 			});
 			//log("Object %s for path %s", null, oo, li.path);
 			if (item.id <= 0) {
 				fillMediaModel(item, mf.getMediaInfo());
 				dos.addObject(new DODelegator(item, null, "", "id"), "id");
 			}
 			mb_play_list_map plm = new mb_play_list_map(this);
 			plm.item_id = item.id;
 			plm.list_id = pl.id;
 			plm.related_on = new Date();
 			dos.addObject(new DODelegator(plm));
 		} catch (Exception e) {
 			throw new MBError("Add item to list error", e);
 		}
 	}
 
 	public void addToLibrary(MediaFormat mf, Path p) throws MBError {
 		mb_media_item item = new mb_media_item(this);
 		mb_media_set set = new mb_media_set(this);
 		fillMediaModel(item, set, mf.getMediaInfo());
 		DODelegator dod;
 		try {
 			if (set.title != null && set.title.isEmpty() == false) {
 				getDOService().addObject(dod = new DODelegator(set, null, "", "id,title,subset_num"), "id", new DODelegator(set, null, "id", "title,subset_num"));
				if (dod.get("id") == null) {
					getDOService().getObjectLike(dod = new DODelegator(set, null, "title,subset_num", "title,subset_num"));
 				}
				item.set_id = (int) dod.get("id");
 			}
 		} catch (ProcessException e) {
 			throw new MBError("Add set to library error: " + mf, e);
 		}
 		item.path = p.toString();
 		try {
 			getDOService().addObject(new DODelegator(item, null, "", "id"), "id");
 		} catch (Exception e) {
 			throw new MBError("Add item to library error: " + mf, e);
 		}
 	}
 
 	public static void fillMediaModel(mb_media_item mi, MediaInfo info) {
 		fillMediaModel(mi, null, info);
 	}
 
 	public static void fillMediaModel(mb_media_item mi, mb_media_set set, MediaInfo info) {
 		mi.title = (String) info.getAttribute(MediaInfo.TITLE);
 		mi.performer = (String) info.getAttribute(MediaInfo.ARTIST);
 		mi.track = intValue(info.getAttribute(MediaInfo.TRACK));
 		mi.year = intValue(info.getAttribute(MediaInfo.YEAR));
 		mi.genre = MP3.findGenre(info);
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
 			Path result = FileSystems.getDefault().getPath(path);
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
