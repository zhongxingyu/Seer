 package wustendorf;
 
 import net.minecraft.src.WorldServer;
 
 import java.io.*;
 import java.sql.*;
 import javax.sql.*;
 import java.util.*;
 
 import com.googlecode.flyway.core.Flyway;
 
 public class WustendorfDB {
     private Connection conn = null;
     private Statement direct = null;
     private Map<String, PreparedStatement> statementCache = new HashMap<String, PreparedStatement>();
 
     public WustendorfDB(WorldServer world) {
         File dbFile = new File(world.getChunkSaveLocation(), "wustendorf");
         try {
             // Get a reference to the database.
             DataSource ds = Wustendorf.getH2DataSource(dbFile);
 
             // Use Flyway to create or upgrade the database, if needed.
             Flyway updater = new Flyway();
             updater.setDataSource(ds);
             updater.setLocations("wustendorf.flyway.worlddb");
             updater.setInitOnMigrate(true);
             updater.migrate();
 
             // And finally connect to the database.
             conn = ds.getConnection();
             direct = conn.createStatement();
         } catch (Exception e) {
             System.out.println("Wustendorf: Unable to open database " + dbFile);
             e.printStackTrace();
 
             conn = null;
             direct = null;
         }
     }
 
    protected PreparedStatement getCachedStatement(String query) {
         PreparedStatement stmt = statementCache.get(query);
         if (stmt == null) {
            try {
                stmt = conn.prepareStatement(query);
                statementCache.put(query, stmt);
            } catch (Exception e) {
                System.out.println("Wustendorf: Database error:");
                e.printStackTrace();
            }
         }
 
         return stmt;
     }
 
     protected void execute(String query, Object... params) {
         try {
             PreparedStatement stmt = getCachedStatement(query);
             for (int i=0; i<params.length; i++) {
                 stmt.setObject(i+1, params[i]);
             }
 
             stmt.executeUpdate();
         } catch (Exception e) {
             if (conn != null) {
                 System.out.println("Wustendorf: Database error:");
                 e.printStackTrace();
             }
         }
     }
 
     protected ResultSet getResults(String query, Object... params) throws SQLException {
         PreparedStatement stmt = getCachedStatement(query);
         for (int i=0; i<params.length; i++) {
             stmt.setObject(i+1, params[i]);
         }
 
         return stmt.executeQuery();
     }
 
     protected Integer getInt(String query, Object... params) {
         try {
             ResultSet results = getResults(query, params);
 
             if (!results.next()) {
                 return null;
             }
 
             return results.getInt(1);
         } catch (Exception e) {
             if (conn != null) {
                 System.out.println("Wustendorf: Database error:");
                 e.printStackTrace();
             }
         }
 
         return null;
     }
 
     protected List<List<Integer>> getIntMatrix(int len, String query, Object... params) {
         try {
             ResultSet results = getResults(query, params);
 
             List<List<Integer>> matrix = new ArrayList<List<Integer>>();
 
             while (results.next()) {
                 List<Integer> row = new ArrayList<Integer>();
 
                 for (int i=0; i<len; i++) {
                     row.add(results.getInt(i+1));
                 }
 
                 matrix.add(row);
             }
 
 
             return matrix;
         } catch (Exception e) {
             if (conn != null) {
                 System.out.println("Wustendorf: Database error:");
                 e.printStackTrace();
             }
             return null;
         }
     }
 
     protected int[] getIntArray(int len, String query, Object... params) {
         try {
             ResultSet results = getResults(query, params);
 
             if (!results.next()) {
                 return new int[0];
             }
 
             int[] output = new int[len];
             for (int i=0; i<len; i++) {
                 output[i] = results.getInt(i+1);
             }
 
             return output;
         } catch (Exception e) {
             if (conn != null) {
                 System.out.println("Wustendorf: Database error:");
                 e.printStackTrace();
             }
             return new int[0];
         }
     }
 
     protected Map<String, Integer> getIntMap(String query, Object... params) {
         try {
             ResultSet results = getResults(query, params);
 
             Map<String, Integer> map = new HashMap<String, Integer>();
 
             while (results.next()) {
                 String key = results.getString(1);
                 int value = results.getInt(2);
 
                 map.put(key, value);
             }
 
             return map;
         } catch (Exception e) {
             if (conn != null) {
                 System.out.println("Wustendorf: Database error:");
                 e.printStackTrace();
             }
         }
 
         return null;
     }
 
     public Integer getMarkerID(int x, int y, int z) {
         return getInt("SELECT marker_id FROM Markers WHERE marker_x=? AND marker_y=? AND marker_z=?;", x, y, z);
     }
 
     public List<DBMarker> getMarkersInPhase(int phase) {
         List<List<Integer>> info = getIntMatrix(5, "SELECT marker_id, marker_x, marker_y, marker_z, marker_range FROM Markers WHERE (marker_id % 100)=?;", phase);
 
         if (info == null) {
             return null;
         }
 
         List<DBMarker> markers = new ArrayList<DBMarker>();
         for (List<Integer> marker_info : info) {
             DBMarker marker = new DBMarker(this, marker_info.get(1), marker_info.get(2), marker_info.get(3));
             marker.id = marker_info.get(0);
             marker.range = marker_info.get(4);
 
             markers.add(marker);
         }
 
         return markers;
     }
 
     public Integer addMarker(int x, int y, int z) {
         return addMarkerWithRange(x, y, z, 0);
     }
 
     public Integer addMarkerWithRange(int x, int y, int z, int range) {
         if (getMarkerID(x, y, z) != null) {
             return null;
         }
 
         execute("INSERT INTO Markers VALUES (DEFAULT, ?, ?, ?, ?);", x, y, z, range);
 
         return getMarkerID(x, y, z);
     }
 
     public DBMarker getMarkerAt(int x, int y, int z) {
         return new DBMarker(this, x, y, z);
     }
 
     public void removeMarker(int x, int y, int z) {
         Integer marker = getMarkerID(x, y, z);
 
         if (marker != null) {
             removeMarker(marker);
         }
     }
 
     public void removeMarker(int marker) {
         execute("DELETE FROM Tags WHERE marker_id=?;", marker);
         execute("DELETE FROM Markers WHERE marker_id=?;", marker);
         execute("DELETE FROM Rooms WHERE marker_id=?;", marker);
         execute("DELETE FROM ImportantBlocks WHERE marker_id=?;", marker);
     }
 
     public void addRoom(int marker, int room_id, int x_min, int x_max, int y_min, int y_max, int z_min, int z_max, int x_origin, int y_origin, int z_origin) {
         execute("INSERT INTO Rooms VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                 marker, room_id, x_min, x_max, y_min, y_max, z_min, z_max,
                 x_origin, y_origin, z_origin);
     }
 
     public int[] roomAt(int x, int y, int z) {
         return getIntArray(2, "SELECT marker_id, room_id FROM Rooms WHERE ? BETWEEN x_min AND x_max AND ? BETWEEN y_min AND y_max AND ? between z_min AND z_max;", x, y, z);
     }
 
     public void addImportantBlock(int marker, int room_id, int x, int y, int z, int type) {
         execute("INSERT INTO ImportantBlocks VALUES (?, ?, ?, ?, ?, ?);",
                 marker, room_id, x, y, z, type);
     }
 
     public int getRange(int marker) {
         return getInt("SELECT range FROM Markers WHERE marker_id=?;", marker);
     }
 
     public void setRange(int marker, int range) {
         execute("UPDATE Markers SET range=? WHERE marker_id=?;", range, marker);
     }
 
     public void clearTag(int marker, String tag) {
         execute("DELETE FROM Tags WHERE marker_id=? AND tag_name=?;", marker, tag);
     }
 
     public void setTag(int marker, String tag, int value) {
         execute("MERGE INTO Tags VALUES (?, ?, ?);", marker, tag, value);
     }
 
     public int getMarkerCount() {
         return getInt("SELECT COUNT(*) FROM Markers;");
     }
 
     public DBMarker getMarker(int index) {
         int[] info = getIntArray(5, "SELECT marker_id, marker_x, marker_y, marker_z, marker_range FROM Markers LIMIT 1 OFFSET ?;", index);
         if (info.length == 5) {
             DBMarker marker = new DBMarker(this, info[1], info[2], info[3]);
             marker.id = info[0];
             marker.range = info[4];
 
             return marker;
         } else {
             return null;
         }
     }
 
     public int getTag(int marker, String tag) {
         return getInt("SELECT value FROM Tags NATURAL JOIN Markers WHERE marker_id=? AND tag_name=?;", marker, tag);
     }
 
     public Map<String, Integer> getAllTags(int marker) {
         return getIntMap("SELECT tag_name, value FROM Tags NATURAL JOIN Markers WHERE marker_id=?;", marker);
     }
 
     public Map<String, Integer> getMatchingTags(int marker, String match) {
         return getIntMap("SELECT tag_name, value FROM Tags NATURAL JOIN Markers WHERE marker_id=? AND tag_name LIKE ?;", marker, match);
     }
 
     public List<List<Integer>> getImportantBlocks(int x, int y, int z) {
         return getIntMatrix(3, "SELECT block_x, block_y, block_z from ImportantBlocks NATURAL JOIN Markers WHERE marker_x=? AND marker_y=? AND marker_z=?", x, y, z);
     }
 
     public List<DBMarker> getMarkersWithTag(String tag) {
        List<List<Integer>> info = getIntMatrix(6, "SELECT marker_id, marker_x, marker_y, marker_z, range, value FROM Tags NATURAL JOIN Markers WHERE tag_name=?;", tag);
 
         if (info == null) {
             return null;
         }
 
         List<DBMarker> markers = new ArrayList<DBMarker>();
         for (List<Integer> marker_info : info) {
             DBMarker marker = new DBMarker(this, marker_info.get(1), marker_info.get(2), marker_info.get(3));
             marker.id = marker_info.get(0);
             marker.range = marker_info.get(4);
             marker.tags.put(tag, marker_info.get(5));
 
             markers.add(marker);
         }
 
         return markers;
     }
 
     public int getBestInRange(String tag, int x, int z) {
         return getInt("SELECT MAX(value) FROM Tags NATURAL JOIN Markers WHERE tag_name=? AND ABS(marker_x-?)<=range AND ABS(marker_z-?)<=range;", tag, x, z);
     }
 
     public void close() {
         try {
             conn.close();
         } catch (Exception e) {
             System.out.println("Wustendorf: Error while closing database:");
             e.printStackTrace();
         }
     }
 }
