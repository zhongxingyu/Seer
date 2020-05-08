 package syam.BoatRace.Race;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import syam.BoatRace.BoatRace;
 import syam.BoatRace.Util.Cuboid;
 
 public class RaceFileManager{
 	// Logger
 	public static final Logger log = BoatRace.log;
 	private static final String logPrefix = BoatRace.logPrefix;
 	private static final String msgPrefix = BoatRace.msgPrefix;
 
 	private final BoatRace plugin;
 
 	public RaceFileManager(final BoatRace plugin){
 		this.plugin = plugin;
 	}
 
 	/* ゲームデータ保存/読み出し */
 	public void saveGames(){
 		FileConfiguration confFile = new YamlConfiguration();
 		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") +
 				"gameData" + System.getProperty("file.separator");
 
 		for (Race race : plugin.races.values()){
 			File file = new File(fileDir + race.getName() + ".yml");
 
 			// マップデータをリストに変換
 			List<String> startList = convertStartSetToList(race.getStartPos());
 
 			// 保存するデータをここに
 			confFile.set("RaceName", race.getName());
 			confFile.set("TimeLimit", race.getTimeLimit());
 
 			confFile.set("StartPoints", startList); // スタート地点
 			confFile.set("GoalRegion", convertCuboidToString(race.getGoalZone())); // ゴールエリア
 			confFile.set("Checkpoints", convertCpSetToList(race.getCheckpoints())); // チェックポイント
 		}
 	}
 
 	public void loadGames(){
 		FileConfiguration confFile = new YamlConfiguration();
 		String fileDir = plugin.getDataFolder() + System.getProperty("file.separator") + "gameData";
 
 		File dir = new File(fileDir);
 		File[] files = dir.listFiles();
 
 		// ゲームデータクリア
 		plugin.races.clear();
 
 		// ファイルなし
 		if (files == null || files.length == 0)
 			return;
 
 		// ファイル取得
 		String name;
 		for (File file : files){
 			try{
 				confFile.load(file);
 
 				// 読むデータキー
 				name = confFile.getString("RaceName", null);
 
 				// ゲーム追加
 				Race race = new Race(plugin, name);
 
 				// 各設定やマップを追加
 				race.setTimeLimit(confFile.getInt("TimeLimit", 60 * 10));
 
 				race.setStartPos(convertStartListToSet(confFile.getStringList("StartPoints"))); // スタート地点
				race.setGoal(convertStringToCuboid(confFile.getString("GoalRegion"))); // ゴールエリア
 				race.setCheckpoints(convertCpListToSet(confFile.getStringList("Checkpoints"))); // チェックポイント
 
 				log.info(logPrefix + "Loaded Race: "+file.getName()+" ("+name+")");
 			}catch (Exception ex){
 				ex.printStackTrace();
 			}
 		}
 
 	}
 
 
 	/* レースデータを変換 */
 
 	// スタート地点
  	private List<String> convertStartSetToList(Set<Location> startPos) {
 		List<String> ret = new ArrayList<String>();
 		ret.clear();
 
 		for (Location loc : startPos){
 			// 単に座標形式に変換する
 			String s = loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
 
 			// 返すリストに追加
 			ret.add(s);
 		}
 
 		return ret;
 	}
  	private Set<Location> convertStartListToSet(List<String> starts){
  		Set<Location> ret = new HashSet<Location>();
  		ret.clear();
 
  		String[] coord;
 
  		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);
 
  		int line = 0;
  		for (String s : starts){
  			line++;
  			// デリミタで分ける
  			coord = s.split(",");
  			if (coord.length != 3){
  				log.warning(logPrefix+ "Skipping StartPointsLine "+line+": incorrect coord format (,)");
  				continue;
  			}
 
  			Location loc = new Location(world, new Double(coord[0]), new Double(coord[1]), new Double(coord[2])).getBlock().getLocation();
  			ret.add(loc);
  		}
 
  		return ret;
  	}
 
  	// ゴールエリア
  	private String convertCuboidToString(Cuboid region){
  		String ret;
 
  		// 座標を取得する
  		Location pos1 = region.getPos1();
  		Location pos2 = region.getPos2();
 
  		ret = pos1.getBlockX()+","+pos1.getBlockY()+","+pos1.getBlockZ() + "@";
  		ret = ret + pos2.getBlockX()+","+pos2.getBlockY()+","+pos2.getBlockZ();
 
  		return ret;
  	}
  	private Cuboid convertStringToCuboid(String s){
  		String[] data;
  		String[] coord;
  		Location pos1, pos2;
 
  		World world = Bukkit.getWorld(plugin.getConfigs().gameWorld);
 
  		data = s.split("@");
  		if (data.length != 2){
  			log.warning(logPrefix+ "Skipping Region: incorrect format (@)");
  			return null;
  		}
  		// Pos1
  		coord = data[0].split(",");
  		if (coord.length != 3){
 			log.warning(logPrefix+ "Skipping Region: incorrect 1st coord format (,)");
 			return null;
 		}
  		pos1 = new Location(world, new Double(coord[0]), new Double(coord[1]), new Double(coord[2])).getBlock().getLocation();
 
  		// Pos2
  		coord = data[1].split(",");
  		if (coord.length != 3){
 			log.warning(logPrefix+ "Skipping Region: incorrect 2nd coord format (,)");
 			return null;
 		}
  		pos2 = new Location(world, new Double(coord[0]), new Double(coord[1]), new Double(coord[2])).getBlock().getLocation();
 
  		return new Cuboid(pos1, pos2);
  	}
 
  	// チェックポイント
  	private List<String> convertCpSetToList(Set<Cuboid> regions){
  		List<String> ret = new ArrayList<String>();
  		ret.clear();
 
  		for (Cuboid region : regions){
  			ret.add(convertCuboidToString(region));
  		}
 
  		return ret;
  	}
  	private Set<Cuboid> convertCpListToSet(List<String> regions){
  		Set<Cuboid> ret = new HashSet<Cuboid>();
  		ret.clear();
 
  		for (String s : regions){
  			ret.add(convertStringToCuboid(s));
  		}
 
  		return ret;
  	}
 
 }
