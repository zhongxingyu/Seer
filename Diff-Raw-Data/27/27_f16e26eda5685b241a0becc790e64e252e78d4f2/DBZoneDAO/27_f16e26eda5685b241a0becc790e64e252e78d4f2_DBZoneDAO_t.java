 package info.bytecraft.database.db;
 
 import info.bytecraft.api.BytecraftPlayer;
 import info.bytecraft.api.math.Rectangle;
 import info.bytecraft.database.DAOException;
 import info.bytecraft.database.IZoneDAO;
 import info.bytecraft.zones.Lot;
 import info.bytecraft.zones.Zone;
 import info.bytecraft.zones.Zone.Flag;
 import info.bytecraft.zones.Zone.Permission;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 public class DBZoneDAO implements IZoneDAO
 {
     private Connection conn;
     
     public DBZoneDAO(Connection conn)
     {
         this.conn = conn;
     }
     
     @Override
     public List<Zone> loadZones() throws DAOException
     {
         List<Zone> zones = Lists.newArrayList();
         
         String sql = "SELECT * FROM zone";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 while(rs.next()){
                     Zone zone = new Zone();
                     zone.setName(rs.getString("zone_name"));
                     zone.setId(rs.getInt("zone_id"));
                     zone.setEnterMessage(rs.getString("zone_entermsg"));
                     zone.setExitMessage(rs.getString("zone_exitmsg"));
                     zone.setFlag(Flag.PVP, false);
                     zone.setFlag(Flag.BUILD, Boolean.parseBoolean(rs.getString("zone_build")));
                     zone.setFlag(Flag.HOSTILES, Boolean.parseBoolean(rs.getString("zone_hostile")));
                     zone.setFlag(Flag.WHITELIST, Boolean.parseBoolean(rs.getString("zone_whitelist")));
                     zone.setFlag(Flag.CREATIVE, Boolean.parseBoolean(rs.getString("zone_creative")));
                     zone.setWorld(rs.getString("zone_world"));
                     zone.setRectangle(getRect(zone));
                     zone.setPermissions(getPermissions(zone));
                     zone.setLots(getLots(zone));
                     zones.add(zone);
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return zones;
     }
     
     public List<Zone> getZones(String world)
     throws DAOException
     {
         List<Zone> zones = Lists.newArrayList();
         
         String sql = "SELECT * FROM zone WHERE zone_world = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, world);
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 while(rs.next()){
                     Zone zone = new Zone();
                     zone.setName(rs.getString("zone_name"));
                     zone.setId(rs.getInt("zone_id"));
                     zone.setEnterMessage(rs.getString("zone_entermsg"));
                     zone.setExitMessage(rs.getString("zone_exitmsg"));
                     zone.setFlag(Flag.PVP, false);
                     zone.setFlag(Flag.BUILD, Boolean.parseBoolean(rs.getString("zone_build")));
                     zone.setFlag(Flag.HOSTILES, Boolean.parseBoolean(rs.getString("zone_hostile")));
                     zone.setFlag(Flag.WHITELIST, Boolean.parseBoolean(rs.getString("zone_whitelist")));
                    zone.setFlag(Flag.CREATIVE, Boolean.parseBoolean(rs.getString("zone_creative")));
                     zone.setWorld(rs.getString("zone_world"));
                     zone.setRectangle(getRect(zone));
                     zone.setPermissions(getPermissions(zone));
                     zone.setLots(getLots(zone));
                     zones.add(zone);
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return zones;
     }
     
     public void createZone(Zone zone, BytecraftPlayer player)
     throws DAOException
     {
         String sql = "INSERT INTO zone (zone_name, zone_world, zone_entermsg, zone_exitmsg) VALUES (?, ?, ?, ?)";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.setString(2, zone.getWorld());
             stm.setString(3, "Welcome to " + zone.getName());
             stm.setString(4, "Now leaving " + zone.getName());
             stm.execute();
             
             sql = "INSERT INTO zone_rect (zone_name, rect_x1, rect_z1, rect_x2, rect_z2) VALUES (?, ?, ?, ?, ?)";
             try(PreparedStatement stmt = conn.prepareStatement(sql)){
                 stmt.setString(1, zone.getName());
                 stmt.setInt(2, player.getZoneBlock1().getX());
                 stmt.setInt(3, player.getZoneBlock1().getZ());
                 stmt.setInt(4, player.getZoneBlock2().getX());
                 stmt.setInt(5, player.getZoneBlock2().getZ());
                 stmt.execute();
             }
             
             sql = "SELECT * FROM zone WHERE zone_name = ?";
             try(PreparedStatement stm2 = conn.prepareStatement(sql)){
                 stm2.setString(1, zone.getName());
                 stm2.execute();
                 
                 try(ResultSet rs = stm2.getResultSet()){
                     if(rs.next()){
                         zone.setId(rs.getInt("zone_id"));
                         zone.setEnterMessage(rs.getString("zone_entermsg"));
                         zone.setExitMessage(rs.getString("zone_exitmsg"));
                         zone.setFlag(Flag.PVP, false);
                         zone.setFlag(Flag.BUILD, Boolean.parseBoolean(rs.getString("zone_build")));
                         zone.setFlag(Flag.HOSTILES, Boolean.parseBoolean(rs.getString("zone_hostile")));
                         zone.setFlag(Flag.WHITELIST, Boolean.parseBoolean(rs.getString("zone_whitelist")));
                        zone.setFlag(Flag.CREATIVE, Boolean.parseBoolean(rs.getString("zone_creative")));
                         zone.setWorld(rs.getString("zone_world"));
                         zone.setRectangle(getRect(zone));
                         zone.setPermissions(getPermissions(zone));
                     }
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
     
     public Zone getZone(String name)
     throws DAOException
     {
         Zone zone = new Zone(name);
         String sql = "SELECT * FROM zone WHERE zone_name = ?";
         
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, name);
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 if(!rs.next()){
                     return null;
                 }
                 zone.setId(rs.getInt("zone_id"));
                 zone.setEnterMessage(rs.getString("zone_entermsg"));
                 zone.setExitMessage(rs.getString("zone_exitmsg"));
                 zone.setFlag(Flag.PVP, false);
                 zone.setFlag(Flag.BUILD, Boolean.parseBoolean(rs.getString("zone_build")));
                 zone.setFlag(Flag.HOSTILES, Boolean.parseBoolean(rs.getString("zone_hostile")));
                 zone.setFlag(Flag.WHITELIST, Boolean.parseBoolean(rs.getString("zone_whitelist")));
                 zone.setWorld(rs.getString("zone_world"));
                 zone.setRectangle(getRect(zone));
                 zone.setPermissions(getPermissions(zone));
                 zone.setLots(getLots(zone));
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return zone;
     }
     
     public Rectangle getRect(Zone zone)
     throws DAOException
     {
         String sql = "SELECT * FROM zone_rect WHERE zone_name = ?";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 if(rs.next()){
                     int x1 = rs.getInt("rect_x1");
                     int z1 = rs.getInt("rect_z1");
                     int x2 = rs.getInt("rect_x2");
                     int z2 = rs.getInt("rect_z2");
                     return new Rectangle(x1, z1, x2, z2);
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return null;
     }
     
     public Map<String, Permission> getPermissions(Zone zone)
     throws DAOException
     {
         if(zone == null)return null;
         Map<String, Permission> map = Maps.newHashMap();
         
         String sql = "SELECT * FROM zone_user WHERE zone_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 while(rs.next()){
                     map.put(rs.getString("player_name"), 
                             Permission.valueOf(rs.getString("player_perm").toUpperCase()));
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return map;
     }
     
     public Permission getUser(Zone zone, BytecraftPlayer player)
     throws DAOException
     {
         String sql = "SELECT * FROM zone_user WHERE zone_name = ? AND player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.setString(2, player.getName());
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 if(rs.next()){
                     return Permission.valueOf(rs.getString("player_perm").toUpperCase());
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return null;
     }
     
     public void deleteZone(String name)
     throws DAOException
     {
         String sql = "DELETE FROM zone WHERE zone_name = ?";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, name);
             stm.execute();
             
             sql = "DELETE FROM zone_rect WHERE zone_name = ?";
             try(PreparedStatement stm1 = conn.prepareStatement(sql)){
                 stm1.setString(1, name);
                 stm1.execute();
                 
                 sql = "DELETE FROM zone_user WHERE zone_name = ?";
                 try(PreparedStatement stm2 = conn.prepareStatement(sql)){
                     stm2.setString(1, name);
                     stm2.execute();
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
     
     public void updateFlag(Zone zone, Flag flag, String value)
     throws DAOException
     {
         String sql = null;
         switch(flag)
         {
         case PVP: sql = "UPDATE zone SET zone_pvp = ? WHERE zone_name = ?";
             break;
         case WHITELIST: sql = "UPDATE zone SET zone_whitelist = ? WHERE zone_name = ?";
             break;
         case HOSTILES: sql = "UPDATE zone SET zone_hostile = ? WHERE zone_name = ?";
             break;
         case BUILD: sql = "UPDATE zone SET zone_build = ? WHERE zone_name = ?";
             break;
         case ENTERMSG: sql = "UPDATE zone SET zone_entermsg = ? WHERE zone_name = ?";
             break;
         case EXITMSG: sql = "UPDATE zone SET zone_exitmsg = ? WHERE zone_name = ?";
             break;
         case CREATIVE: sql = "UPDATE zone SET zone_creative = ? WHERE zone_name = ?";
         }
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, value);
             stm.setString(2, zone.getName());
             stm.execute();
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
     
     public void addUser(Zone zone, String name, Permission p)
     throws DAOException
     {
         String sql = "SELECT * FROM zone_user WHERE zone_name = ? AND player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.setString(2, name);
             stm.execute();
             try(ResultSet rs = stm.getResultSet()){
                 if(rs.next()){
                     updateUser(zone, name, p);
                 }else{
                     sql = "INSERT INTO zone_user (zone_name, player_name, player_perm) VALUES (?, ?, ?)"; 
                     try(PreparedStatement stm1 = conn.prepareStatement(sql)){
                         stm1.setString(1, zone.getName());
                         stm1.setString(2, name);
                         stm1.setString(3, p.name().toLowerCase());
                         stm1.execute();
                     }
                 }
             }
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
     
     public void updateUser(Zone zone, String name, Permission p)
     throws DAOException
     {
         String sql = "UPDATE zone_user SET player_perm = ? WHERE zone_name = ? AND player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, p.name().toLowerCase());
             stm.setString(2, zone.getName());
             stm.setString(3, name);
             stm.execute();
             zone.addPermissions(name, p);
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
     }
     
     public boolean deleteUser(Zone zone, String name)
     throws DAOException
     {
         String sql = "DELETE FROM zone_user WHERE zone_name = ? AND player_name = ?";
         try (PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.setString(2, name);
             stm.execute();
             zone.removePermission(name);
         }catch(SQLException e){
             throw new DAOException(sql, e);
         }
         return true;
     }
 
     @Override
     public List<Lot> getLots(String world) throws DAOException
     {
         String sql = "SELECT zone_lot.* FROM zone_lot " +
             "INNER JOIN zone USING (zone_name) WHERE zone_world = ?";
 
         List<Lot> lots = new ArrayList<Lot>();
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
 
             stmt.setString(1, world);
             stmt.execute();
 
             try (ResultSet rs = stmt.getResultSet()) {
                 while (rs.next()) {
                     Lot lot = new Lot();
                     lot.setId(rs.getInt("lot_id"));
                     lot.setName(rs.getString("lot_name"));
                     lot.setZoneName(rs.getString("zone_name"));
 
                     int x1 = rs.getInt("lot_x1");
                     int z1 = rs.getInt("lot_z1");
                     int x2 = rs.getInt("lot_x2");
                     int z2 = rs.getInt("lot_z2");
                     
                     lot.setRect(new Rectangle(x1, z1, x2, z2));
 
                     lots.add(lot);
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
 
         for (Lot lot : lots) {
             lot.setOwner(getLotOwners(lot.getId()));
         }
 
         return lots;
     }
     
     public Map<String, Lot> getLots(Zone zone) throws DAOException
     {
         Map<String, Lot> lots = Maps.newHashMap();
         String sql = "SELECT * FROM zone_lot WHERE zone_name = ?";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone.getName());
             stm.execute();
             
             try (ResultSet rs = stm.getResultSet()) {
                 while (rs.next()) {
                     Lot lot = new Lot();
                     lot.setId(rs.getInt("lot_id"));
                     lot.setName(rs.getString("lot_name"));
                     lot.setZoneName(rs.getString("zone_name"));
 
                     int x1 = rs.getInt("lot_x1");
                     int z1 = rs.getInt("lot_z1");
                     int x2 = rs.getInt("lot_x2");
                     int z2 = rs.getInt("lot_z2");
                     
                     lot.setRect(new Rectangle(x1, z1, x2, z2));
 
                     lots.put(lot.getName(), lot);
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
 
         for (Lot lot : lots.values()) {
             lot.setOwner(getLotOwners(lot.getId()));
         }
         return lots;
     }
 
     @Override
     public List<String> getLotOwners(int lotId) throws DAOException
     {
         String sql = "SELECT * FROM zone_lotuser " +
             "WHERE lot_id = ?";
 
         List<String> owners = new ArrayList<>();
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
 
             stmt.setInt(1, lotId);
             stmt.execute();
 
             try (ResultSet rs = stmt.getResultSet()) {
                 while (rs.next()) {
                     owners.add(rs.getString("player_name"));
                 }
             }
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
 
         return owners;
     }
 
     @Override
     public void addLot(Lot lot) throws DAOException
     {
         String sql = "INSERT INTO zone_lot (zone_name, lot_name, lot_x1, " +
                 "lot_z1, lot_x2, lot_z2) VALUES (?,?,?,?,?,?)";
 
             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setString(1, lot.getZoneName());
                 stmt.setString(2, lot.getName());
 
                 Rectangle rect = lot.getRect();
                 stmt.setInt(3, rect.getLeft());
                 stmt.setInt(4, rect.getTop());
                 stmt.setInt(5, rect.getRight());
                 stmt.setInt(6, rect.getBottom());
 
                 stmt.execute();
 
                 stmt.execute("SELECT LAST_INSERT_ID()");
 
                 try (ResultSet rs = stmt.getResultSet()) {
                     if (rs.next()) {
                         lot.setId(rs.getInt(1));
                     }
                 }
             } catch (SQLException e) {
                 throw new DAOException(sql, e);
             }
     }
 
     @Override
     public void deleteLot(int lotId) throws DAOException
     {
         String sql = "DELETE FROM zone_lot WHERE lot_id = ?";
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, lotId);
             stmt.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     @Override
     public void addLotUser(int lotId, String name) throws DAOException
     {
         String sql = "INSERT INTO zone_lotuser (lot_id, player_name) ";
         sql += "VALUES (?,?)";
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, lotId);
             stmt.setString(2, name);
             stmt.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     @Override
     public void deleteLotUsers(int lotId) throws DAOException
     {
         String sql = "DELETE FROM zone_lotuser WHERE lot_id = ?";
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, lotId);
             stmt.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     @Override
     public void deleteLotUser(int lotId, String name) throws DAOException
     {
         String sql = "DELETE FROM zone_lotuser WHERE lot_id = ? AND player_name = ?";
 
         try (PreparedStatement stmt = conn.prepareStatement(sql)) {
             stmt.setInt(1, lotId);
             stmt.setString(2, name);
             stmt.execute();
         } catch (SQLException e) {
             throw new DAOException(sql, e);
         }
     }
 
     @Override
     public boolean lotExists(String zone, String name) throws DAOException
     {
         String sql = "SELECT * FROM zone_lot WHERE zone_name = ? AND lot_name = ?";
         try(PreparedStatement stm = conn.prepareStatement(sql)){
             stm.setString(1, zone);
             stm.setString(2, name);
             stm.execute();
             
             try(ResultSet rs = stm.getResultSet()){
                 return rs.next();
             }
         }catch(SQLException e){
             throw new RuntimeException(sql, e);
         }
     }
 
 }
