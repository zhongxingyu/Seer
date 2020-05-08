 package info.bytecraft.database.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import info.bytecraft.Bytecraft;
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.Rank;
 import info.bytecraft.api.BytecraftPlayer.Flag;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IPlayerDAO;
 
 public class DBPlayerDAO implements IPlayerDAO
 {
     
     private Bytecraft plugin;
     private Connection conn;
     
     public DBPlayerDAO(Connection conn, Bytecraft plugin)
     {
         this.plugin = plugin;
         this.conn = conn;
     }
 
     @SuppressWarnings("serial")
     private static final Map<String, ChatColor> GOD_COLORS =
             new HashMap<String, ChatColor>() {
                 {
                     put("red", ChatColor.RED);
                     put("aqua", ChatColor.AQUA);
                     put("gold", ChatColor.GOLD);
                     put("yellow", ChatColor.YELLOW);
                     put("dark_aqua", ChatColor.DARK_AQUA);
                     put("pink", ChatColor.LIGHT_PURPLE);
                     put("purple", ChatColor.DARK_PURPLE);
                     put("green", ChatColor.GREEN);
                     put("dark_green", ChatColor.DARK_GREEN);
                     put("dark_red", ChatColor.DARK_RED);
                     put("gray", ChatColor.GRAY);
                 }
             };
 
     private final SimpleDateFormat format = new SimpleDateFormat(
             "MM/dd/YY hh:mm:ss a");
 
     public BytecraftPlayer getPlayer(Player player) throws DAOException
     {
         return getPlayer(player.getName(), player);
     }
 
     public BytecraftPlayer getPlayer(String name) throws DAOException
     {
         return getPlayer(name, null);
     }
 
     public BytecraftPlayer getPlayer(String name, Player wrap)
             throws DAOException
     {
         BytecraftPlayer player;
         if (wrap != null) {
             player = new BytecraftPlayer(wrap, plugin);
         }
         else {
             player = new BytecraftPlayer(name, plugin);
         }
 
         String sql = "SELECT * FROM player WHERE player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, name);
             stm.execute();
 
             try (ResultSet rs = stm.getResultSet()) {
                 if (!rs.next()) {
                    return null;
                 }
 
                 player.setId(rs.getInt("player_id"));
                 player.setRank(Rank.getRank(rs.getString("player_rank")));
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         loadFlags(player);
         return player;
     }
     
     public void createFlags(BytecraftPlayer player) throws DAOException
     {
         String sql = "INSERT INTO player_property (player_name, invisible, tpblock, god_color, noble) VALUES (?, ?, ?, ?, ?)";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, player.getName());
             stm.setString(2, "false");
             stm.setString(3, "false");
             stm.setString(5, "false");
             stm.setString(4, "red");
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     @Override
     public void loadFlags(BytecraftPlayer player) throws DAOException
     {
         String sql = "SELECT * FROM player_property WHERE player_name = ?";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, player.getName());
             stm.execute();
             
             try(ResultSet rs = stm.getResultSet()){
                 if(rs.next()){
                     boolean tpb = Boolean.valueOf(rs.getString("tpblock"));
                     boolean invisible = Boolean.valueOf(rs.getString("invisible"));
                     boolean noble = Boolean.valueOf(rs.getString("noble"));
                     
                     player.setFlag(Flag.TPBLOCK, tpb);
                     player.setFlag(Flag.INVISIBLE, invisible);
                     player.setFlag(Flag.NOBLE, noble);
 
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
 
     public BytecraftPlayer createPlayer(Player wrap) throws DAOException
     {
         BytecraftPlayer player = new BytecraftPlayer(wrap, plugin);
         String sql = "INSERT INTO player (player_name) VALUE (?)";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.execute();
 
             stm.executeQuery("SELECT LAST_INSERT_ID()");
 
             try (ResultSet rs = stm.getResultSet()) {
                 if (!rs.next()) {
                     throw new SQLException("Failed to get player id");
                 }
 
                 player.setId(rs.getInt(1));
                 player.setRank(Rank.NEWCOMER);
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         createFlags(player);
        loadFlags(player);
         return player;
     }
 
     public void updatePermissions(BytecraftPlayer player) throws DAOException
     {
         String sql = "UPDATE player SET player_rank = ? WHERE player_id = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getRank().toString().toLowerCase());
             stm.setInt(2, player.getId());
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
     
     public void updateProperties(BytecraftPlayer player)
             throws DAOException
     {
         String sql =
                 "REPLACE INTO player_property (player_name, invisible, tpblock) VALUES (?, ?, ?)";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.setString(2, String.valueOf(player.hasFlag(Flag.INVISIBLE)));
             stm.setString(3, String.valueOf(player.hasFlag(Flag.TPBLOCK)));
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     public long getBalance(BytecraftPlayer player) throws DAOException
     {
         if (player == null) {
             throw new RuntimeException("Player can not be null");
         }
 
         String sql = "SELECT * FROM player WHERE `player_id`=?";
 
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setInt(1, player.getId());
 
             stm.execute();
 
             try (ResultSet rs = stm.getResultSet()) {
                 if (rs.next()) {
                     return rs.getInt("player_wallet");
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         return 0;
     }
 
     public void give(BytecraftPlayer player, long toAdd) throws DAOException
     {
         String sql =
                 "UPDATE player SET player_wallet = player_wallet + ? WHERE player_id = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setLong(1, toAdd);
             stm.setInt(2, player.getId());
 
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     public boolean take(BytecraftPlayer player, long toTake)
             throws DAOException
     {
         if ((getBalance(player) - toTake) < 0)
             return false;
         String sql =
                 "UPDATE player SET player_wallet = player_wallet - ? WHERE player_id = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setLong(1, toTake);
             stm.setInt(2, player.getId());
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         return true;
     }
 
     public String formattedBalance(BytecraftPlayer player) throws DAOException
     {
         NumberFormat nf = NumberFormat.getNumberInstance();
         return ChatColor.GOLD + nf.format(getBalance(player)) + ChatColor.AQUA
                 + " bytes";
     }
 
     public ChatColor getGodColor(BytecraftPlayer player) throws DAOException
     {
         String sql =
                 "SELECT * FROM player_property WHERE player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.execute();
             try (ResultSet rs = stm.getResultSet()) {
                 if (rs.next()) {
                     return GOD_COLORS.get(rs.getString("god_color"));
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         return ChatColor.RED;
     }
 
     public void promoteToSettler(BytecraftPlayer player) throws DAOException
     {
         String sql =
                 "UPDATE player SET player_promoted = unix_timestamp() WHERE player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     public long getPromotedTime(BytecraftPlayer player) throws DAOException
     {
         String sql = "SELECT * FROM player WHERE player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.execute();
 
             try (ResultSet rs = stm.getResultSet()) {
                 if (rs.next()) {
                     return rs.getLong("player_promoted");
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
         return 0;
     }
 
     public String formattedPropmotedTime(BytecraftPlayer player)
             throws DAOException
     {
         Date date = new Date(getPromotedTime(player) * 1000L);
         return format.format(date);
     }
 
     public int getPlayTime(BytecraftPlayer player) throws DAOException
     {
         String sql = "SELECT * FROM player WHERE player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setString(1, player.getName());
             stm.execute();
             try (ResultSet rs = stm.getResultSet()) {
                 if (rs.next()) {
                     return rs.getInt("player_playtime");
                 }
                 else {
                     return 0;
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     public void updatePlayTime(BytecraftPlayer player) throws DAOException
     {
         String sql =
                 "UPDATE player SET player_playtime = ? WHERE player_name = ?";
         int playTime = player.getPlayTime() + player.getOnlineTime();
         try (PreparedStatement stm = conn.prepareStatement(sql)) {
             stm.setInt(1, playTime);
             stm.setString(2, player.getName());
             stm.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 }
