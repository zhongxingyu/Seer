 /**
  * Likes - Package: syam.likes.database
  * Created: 2012/10/10 7:03:46
  */
 package syam.likes.database;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import syam.likes.LikesPlugin;
 
 /**
  * Database (Database.java)
  * @author syam(syamn)
  */
 public class Database {
 	// Logger
 	public static final Logger log = LikesPlugin.log;
 	private static final String logPrefix = LikesPlugin.logPrefix;
 	private static final String msgPrefix = LikesPlugin.msgPrefix;
 
 	private static LikesPlugin plugin;
 
 	private static String connectionString = null;
 	private static String tablePrefix = null;
 	private static Connection connection = null;
 	private static long reconnectTimestamp = 0;
 
 	/**
 	 * コンストラクタ
 	 * @param plugin FlagGameプラグインインスタンス
 	 */
 	public Database(final LikesPlugin plugin){
 		this.plugin = plugin;
 
 		// 接続情報読み込み
 		connectionString = "jdbc:mysql://" + plugin.getConfigs().getMySQLaddress() + ":" + plugin.getConfigs().getMySQLport()
 				+ "/" + plugin.getConfigs().getMySQLdbname() + "?user=" + plugin.getConfigs().getMySQLusername() + "&password=" + plugin.getConfigs().getMySQLuserpass();
 		tablePrefix = plugin.getConfigs().getMySQLtablePrefix();
 
 		connect(); // 接続
 
 		// ドライバを読み込む
 		try{
 			Class.forName("com.mysql.jdbc.Driver");
 			DriverManager.getConnection(connectionString);
 		}catch (ClassNotFoundException ex1){
 			log.severe(ex1.getLocalizedMessage());
 		}catch (SQLException ex2){
 			log.severe(ex2.getLocalizedMessage());
 			printErrors(ex2);
 		}
 	}
 
 	/**
 	 * データベースに接続する
 	 */
 	public static void connect(){
 		try{
 			log.info(logPrefix+ "Attempting connection to MySQL..");
 
 			Properties connectionProperties = new Properties();
 			connectionProperties.put("autoReconnect", "false");
 			connectionProperties.put("maxReconnects", "0");
 			connection = DriverManager.getConnection(connectionString, connectionProperties);
 
 			log.info(logPrefix+ "Connected MySQL database!");
 		}catch (SQLException ex){
 			log.severe(logPrefix+ "Could not connect MySQL database!");
 			ex.printStackTrace();
 			printErrors(ex);
 		}
 	}
 
 	/**
 	 * データベース構造を構築する
 	 */
 	public void createStructure(){
 		// ユーザーデータテーブル
 		write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "users` (" +
 				"`player_id` int(10) unsigned NOT NULL AUTO_INCREMENT," +	// 割り当てるID
 				"`player_name` varchar(32) NOT NULL," +						// プレイヤー名
 				"PRIMARY KEY (`player_id`)," +
 				"UNIQUE KEY `player_name` (`player_name`)" +
 				") ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");
 
 		// ユーザープロフィールテーブル
		write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "profile` (" +
 				"`player_id` int(10) unsigned NOT NULL," +					// 割り当てられたプレイヤーID
 				"`status` int(2) NOT NULL DEFAULT '0'," +					// ステータス値
 				"`like_give` int(12) unsigned NOT NULL DEFAULT '0'," +		// Likeした回数
 				"`like_receive` int(12) unsigned NOT NULL DEFAULT '0'," +	// Likeされた回数
 				"`lastlikegave` int(32) unsigned NOT NULL DEFAULT '0'," +	// 最後にLikeした日時
 				"PRIMARY KEY (`player_id`)" +
 				") ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");
 
 		// Like看板データテーブル
 		write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "signs` (" +
 				"`sign_id` int(10) unsigned NOT NULL AUTO_INCREMENT," +		// 割り当てる看板ID
 				"`sign_name` varchar(32) NOT NULL," +						// 看板の名前
 				"`player_id` int(10) unsigned NOT NULL," +					// 所有者のユーザID
 				"`status` int(2) NOT NULL DEFAULT '0'," +					// ステータス値
 				"`text` varchar(255) DEFAULT NULL," +						// メッセージ
 				"`liked` int(12) unsigned NOT NULL DEFAULT '0'," +			// Likeカウント
 				"`lastliked` int(32) unsigned NOT NULL DEFAULT '0'," +		// 最後にLikeされた日時
 				"`world` varchar(255) NOT NULL," +							// ワールド名
 				"`x` int(10) NOT NULL," +									// x
 				"`y` int(10) NOT NULL," +									// y
 				"`z` int(10) NOT NULL," +									// z
 				"PRIMARY KEY (`sign_id`)," +
 				"UNIQUE KEY `sign_name` (`sign_name`)," +
 				"KEY `x_y_z` (`x`,`y`,`z`)" +
 				") ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");
 
 		// ログデータ
 		write("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "likes` (" +
 				"`like_id` int(12) unsigned NOT NULL AUTO_INCREMENT," +		// 割り当てるLikeID
 				"`player_id` int(10) unsigned NOT NULL," +					// プレイヤーID
 				"`sign_id` int(10) unsigned NOT NULL," +					// 対象の看板ID
 				"`text` varchar(255) DEFAULT NULL," +						// メッセージ
 				"`timestamp` int(32) unsigned NOT NULL," +					// タイムスタンプ
 				"PRIMARY KEY (`like_id`)" +
 				") ENGINE=MyISAM DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;");
 	}
 
 	/**
 	 * 書き込みのSQLクエリーを発行する
 	 * @param sql 発行するSQL文
 	 * @return クエリ成功ならtrue、他はfalse
 	 */
 	public boolean write(String sql){
 		// 接続確認
 		if (isConnected()){
 			try{
 				PreparedStatement statement = connection.prepareStatement(sql);
 				statement.executeUpdate(); // 実行
 
 				// 後処理
 				statement.close();
 
 				return true;
 			}catch (SQLException ex){
 				printErrors(ex);
 
 				return false;
 			}
 		}
 		// 未接続
 		else{
 			attemptReconnect();
 		}
 
 		return false;
 	}
 
 	/**
 	 * 読み出しのSQLクエリーを発行する
 	 * @param sql 発行するSQL文
 	 * @return SQLクエリで得られたデータ
 	 */
 	public HashMap<Integer, ArrayList<String>> read(String sql){
 		ResultSet resultSet;
 		HashMap<Integer, ArrayList<String>> rows = new HashMap<Integer, ArrayList<String>>();
 
 		// 接続確認
 		if (isConnected()){
 			try{
 				PreparedStatement statement = connection.prepareStatement(sql);
 				resultSet = statement.executeQuery(); // 実行
 
 				// 結果のレコード数だけ繰り返す
 				while (resultSet.next()){
 					ArrayList<String> column = new ArrayList<String>();
 
 					// カラム内のデータを順にリストに追加
 					for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++){
 						column.add(resultSet.getString(i));
 					}
 
 					// 返すマップにレコード番号とリストを追加
 					rows.put(resultSet.getRow(), column);
 				}
 
 				// 後処理
 				resultSet.close();
 				statement.close();
 			}catch (SQLException ex){
 				printErrors(ex);
 			}
 		}
 		// 未接続
 		else{
 			attemptReconnect();
 		}
 
 		return rows;
 	}
 
 	/**
 	 * int型の値を取得します
 	 * @param sql 発行するSQL文
 	 * @return 最初のローにある数値
 	 */
 	public int getInt(String sql){
 		ResultSet resultSet;
 		int result = 0;
 
 		// 接続確認
 		if (isConnected()){
 			try{
 				PreparedStatement statement = connection.prepareStatement(sql);
 				resultSet = statement.executeQuery(); // 実行
 
 				if (resultSet.next()){
 					result = resultSet.getInt(1);
 				}else{
 					// 結果がなければ0を返す
 					result = 0;
 				}
 
 				// 後処理
 				resultSet.close();
 				statement.close();
 			}catch (SQLException ex){
 				printErrors(ex);
 			}
 		}
 		// 未接続
 		else{
 			attemptReconnect();
 		}
 
 		return result;
 	}
 
 
 
 	/**
 	 * 接続状況を返す
 	 * @return 接続中ならtrue、タイムアウトすればfalse
 	 */
 	public static boolean isConnected(){
 		if (connection == null){
 			return false;
 		}
 
 		try{
 			return connection.isValid(3);
 		}catch (SQLException ex){
 			return false;
 		}
 	}
 
 	/**
 	 * MySQLデータベースへ再接続を試みる
 	 */
 	public static void attemptReconnect(){
 		final int RECONNECT_WAIT_TICKS = 60000;
 		final int RECONNECT_DELAY_TICKS = 1200;
 
 		if (reconnectTimestamp + RECONNECT_WAIT_TICKS < System.currentTimeMillis()){
 			reconnectTimestamp = System.currentTimeMillis();
 			log.severe(logPrefix+ "Conection to MySQL was lost! Attempting to reconnect 60 seconds...");
 			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new MySQLReconnect(plugin), RECONNECT_DELAY_TICKS);
 		}
 	}
 
 	/**
 	 * エラーを出力する
 	 * @param ex
 	 */
 	private static void printErrors(SQLException ex){
 		log.warning("SQLException:" +ex.getMessage());
 		log.warning("SQLState:" +ex.getSQLState());
 		log.warning("ErrorCode:" +ex.getErrorCode());
 	}
 }
 
