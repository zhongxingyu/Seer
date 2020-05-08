 package syam.ProjectManager.Util;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.server.PluginEnableEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.dynmap.DynmapAPI;
 import org.dynmap.markers.AreaMarker;
 import org.dynmap.markers.MarkerAPI;
 import org.dynmap.markers.MarkerSet;
 
 import syam.ProjectManager.ProjectManager;
 import syam.ProjectManager.Enum.MemberType;
 import syam.ProjectManager.Project.Project;
 
 public class DynmapHandler{
 	public final static Logger log = ProjectManager.log;
 	public final static String logPrefix = ProjectManager.logPrefix;
 	public final static String msgPrefix = ProjectManager.msgPrefix;
 
 	private final ProjectManager plugin;
 
 	// Dynmap
 	private Plugin dynmap;
 	private DynmapAPI api;
 	private MarkerAPI markerapi;
 
 	// flags
 	private boolean activated = false;
 
 	// Markers
 	private MarkerSet set;
 	private Map<String, AreaMarker> resareas = new HashMap<String, AreaMarker>();
 	private String infowindow;
 	private boolean use3D;
 
 	// final
 	private final static String defaultInfowindow =
 			"<div class=\"infowindow\">" +
			"[ProjectManager] プロジェクト情報:<br />" +
			"<span style=\"font-size:120%;\">%projecttitle% [ID: %projectid%]</span><br />" +
			"マネージャー <span style=\"font-weight:bold;\">%managers%</span><br />" +
			"メンバー <span style=\"font-weight:bold;\">%members%</span><br />" +
			"ゲームモード <span style=\"font-weight:bold;\">%gamemode%</span><br />" +
 			"</div>";
 
 	public DynmapHandler(final ProjectManager plugin){
 		this.plugin = plugin;
 	}
 
 	/**
 	 * 初期化
 	 */
 	public void init(){
 		PluginManager pm = plugin.getServer().getPluginManager();
 
 		// Get dynmap
 		dynmap = pm.getPlugin("dynmap");
 		if (dynmap == null){
 			log.severe(logPrefix+ "Cannot find dynmap!");
 			return;
 		}
 
 		// Get dynmap API
 		api = (DynmapAPI)dynmap;
 
 		// Regist Listener
 		pm.registerEvents(new OurServerListener(), plugin);
 
 		// dynmapが有効なら有効化
 		if (dynmap.isEnabled())
 			activate();
 	}
 
 	/**
 	 * 有効化
 	 */
 	private void activate(){
 		// 既に有効化済みなら何もしない
 		if (activated)
 			return;
 
 		// Get markers API
 		markerapi = api.getMarkerAPI();
 		if (markerapi == null){
 			log.severe(logPrefix+ "Cannot loading Dynmap marker API!");
 			return;
 		}
 
 		// TODO: Load config.yml
 		infowindow = defaultInfowindow;
 		use3D = true;
 
 		// Set markers
 		set = markerapi.getMarkerSet("projectmanager.markerset");
 		if (set == null){
 			set = markerapi.createMarkerSet("projectmanager.markerset", "プロジェクト", null, false);
 		}else{
 			set.setMarkerSetLabel("プロジェクト");
 		}
 
 		if (set == null){
 			log.severe(logPrefix+ "Cannot creating dynmap marker set!");
 			return;
 		}
 		// set.setMinZoom(0);
 		set.setLayerPriority(10);
 		set.setHideByDefault(false);
 
 		log.info(logPrefix+ "Hooked to dynmap!");
 		activated = true;
 		updateRegions();
 	}
 
 	/**
 	 * プロジェクト領域をアップデートする
 	 */
 	public void updateRegions(){
 		if (!activated) return;
 
 		Map<String, AreaMarker> newmap = new HashMap<String, AreaMarker>();
 
 		for (Project project : plugin.projects.values()){
 			handleProject(project, newmap);
 		}
 
 		// 古いマーカーを削除
 		for (AreaMarker oldm : resareas.values()){
 			oldm.deleteMarker();
 		}
 
 		// 新マーカーセット
 		resareas.clear();
 		resareas = newmap;
 	}
 
 	private void handleProject(Project project, Map<String, AreaMarker> newmap){
 		String title = project.getTitle();
 		String id = project.getID();
 		double[] x = null;
 		double[] z = null;
 
 		// プロジェクトエリア未設定ならスキップ
 		if (project.getArea() == null)
 			return;
 
 		// プロジェクトエリアの各頂点を取得
 		Location l0 = project.getArea().getPos1();
 		Location l1 = project.getArea().getPos2();
 		World world = l0.getWorld();
 
 		x = new double[4];
 		z = new double[4];
 
 		x[0] = l0.getX();
 		z[0] = l0.getZ();
 		x[1] = l0.getX();
 		z[1] = l1.getZ() + 1.0;
 		x[2] = l1.getX() + 1.0;
 		z[2] = l1.getZ() + 1.0;
 		x[3] = l1.getX() + 1.0;
 		z[3] = l0.getZ();
 
 		// 既にマーカーが存在すれば更新、なければ新規追加
 		String markerid = world.getName() + "_" + id;
 		AreaMarker m = resareas.remove(markerid);
 		if (m == null){
 			// 新規マーカー登録
 			m = set.createAreaMarker(markerid, title, false, world.getName(), x, z, false);
 
 			if (m == null)
 				return;
 		}
 		// マーカーデータ更新
 		else{
 			m.setCornerLocations(x, z);
 			m.setLabel(title);
 		}
 
 		// 3D表示設定
 		if (use3D){
 			m.setRangeY(l1.getY() + 1.0D, l0.getY());
 		}
 
 		// Set/Add styles here..
 
 		// ポップアップするバルーンに詳細情報を設定する
 		m.setDescription(formatInfoWindow(project, m));
 
 
 		// 新マーカーマップに追加
 		newmap.put(markerid, m);
 	}
 
 	/**
 	 * dynmapのエリアマーカー用の表示内容フォーマッティングする
 	 * @param project
 	 * @param m
 	 * @return
 	 */
 	private String formatInfoWindow(Project project, AreaMarker m){
 		String s = "<div class=\"regioninfo\">"+infowindow+"</div>";
 		// Build project title/ID
 		s = s.replaceAll("%projecttitle%", project.getTitle());
 		s = s.replaceAll("%projectid%", project.getID());
 
 		// Build project members/managers
 		String managers = "(none)";
 		String members = "(none)";
 		if (project.getPlayersByType(MemberType.MANAGER).size() >= 1){
 			managers = Util.join(project.getPlayersByType(MemberType.MANAGER), ", ");
 		}
 		if (project.getPlayersByType(MemberType.MEMBER).size() >= 1){
 			members = Util.join(project.getPlayersByType(MemberType.MEMBER), ", ");
 		}
 		s = s.replaceAll("%managers%", managers);
 		s = s.replaceAll("%members%", members);
 
 		// Build project default gamemode
 		if (project.getCreative()){
 			s = s.replaceAll("%gamemode%", "クリエイティブ");
 		}else{
 			s = s.replaceAll("%gamemode%", "サバイバル");
 		}
 
 		return s;
 	}
 
 	/**
 	 * dynmap連携を無効にする
 	 */
 	public void disableDynmap(){
 		if (set != null){
 			set.deleteMarkerSet();
 			set = null;
 		}
 		resareas.clear();
 		activated = false;
 	}
 
 	/**
 	 * dynmap有効時にイベントを取る
 	 * @author syam
 	 */
 	private class OurServerListener implements Listener{
 		@SuppressWarnings("unused")
 		@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 		public void onPluginEnable(final PluginEnableEvent event) {
 			Plugin plugin = event.getPlugin();
 			String name = plugin.getDescription().getName();
 			if(name.equals("dynmap")) {
 				if(dynmap.isEnabled())
 					activate();
 			}
 		}
 	}
 
 	/* getter / setter */
 	public Server getServer(){
 		return plugin.getServer();
 	}
 	public boolean isActivated(){
 		return activated;
 	}
 	public MarkerAPI getMarkerAPI(){
 		return markerapi;
 	}
 }
