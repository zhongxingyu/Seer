 package my.triviagame.dal;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 import java.io.IOException;
 import java.sql.*;
 import java.util.*;
 import java.util.Map.Entry;
 import my.triviagame.xmcd.XmcdDisc;
 import org.apache.commons.io.FileUtils;
 
 public class DAL implements IDAL {
 
     private Connection conn = null;
     private Integer albumCount = null;
     private Integer trackCount = null;
     private Integer artistCount = null;
     private PreparedStatement pstmtInsertGenre;
     private PreparedStatement pstmtInsertArtist;
     private PreparedStatement pstmtInsertAlbum;
     private PreparedStatement pstmtInsertTrack;
 
     //TODO: remove
     public Connection getConnection() {
         return conn;
     }
 
     @Override
     public void openConnection(
             String hostName,
             int port,
             String dbName,
             String userName,
             String password) throws IDAL.DALException {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (ClassNotFoundException ex) {
             throw new IDAL.DALException(ex);
         }
         String formatString = "jdbc:mysql://%s:%d/%s";
         try {
             conn = DriverManager.getConnection(
                     String.format(formatString, hostName, port, dbName), userName, password);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
     }
 
     @Override
     public void closeConnection() throws IDAL.DALException {
         try {
             conn.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         conn = null;
     }
 
     //////////////////////////////////////////
     // Table Counters 
     //////////////////////////////////////////
     private int getTableCount(String tableName, Integer cachedCount) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         try {
             if (cachedCount != null) {
                 return cachedCount;
             }
 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
             rs.next();
             cachedCount = rs.getInt("COUNT(*)");
             rs.close();
             stmt.close();
 
             return cachedCount;
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
     }
 
     @Override
     public int getTrackCount() throws IDAL.DALException {
         return getTableCount("track", trackCount);
     }
 
     @Override
     public int getAlbumCount() throws IDAL.DALException {
         return getTableCount("disc", albumCount);
     }
 
     @Override
     public int getArtistCount() throws IDAL.DALException {
         return getTableCount("artist", artistCount);
     }
 
     /////////////////////////////////////////
     // Track Descriptors
     /////////////////////////////////////////
     //returns all tracks that matches tail term, spelled in SQL
     private List<ITrackDescriptor> getTrackDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs;
         Statement stmt;
         List<ITrackDescriptor> descList = new ArrayList<ITrackDescriptor>();
         String fileName = "resources/get_tracks_basic.sql";
 
         try {
             String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                     getClass().getResource(fileName)));
             String strQuery = fileContent + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new TrackDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (IOException ex) {
             throw new IDAL.DALException(ex);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
     }
 
     @Override
     public List<ITrackDescriptor> getTrackDescriptors(List<Integer> ids) throws IDAL.DALException {
         String tailTerm = String.format("and track.id in (%s)", Joiner.on(",").join(ids));
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getAlbumTrackDescriptors(IAlbumDescriptor album) throws IDAL.DALException {
         String tailTerm = String.format("and track.id = %s", album.getId());
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getArtistTrackDescriptors(IArtistDescriptor artist) throws IDAL.DALException {
         String tailTerm = String.format("and artist.id = %s", artist.getId());
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public Collection<ITrackDescriptor> getTrackDescriptors(String... keywords) throws IDAL.DALException {
         List<String> keywordTerms = new ArrayList<String>(keywords.length);
         for (String keyword : keywords) {
             String tempKeyword = keyword.replace("'", "\"");
             keywordTerms.add(String.format("all_text like '%%%s%%'", tempKeyword.toLowerCase()));
         }
         String tailTerm = Joiner.on(" and ").join(keywordTerms);
         tailTerm = String.format("HAVING %s", tailTerm);
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     /////////////////////////////////////////
     // Album Descriptors
     /////////////////////////////////////////
     public List<IAlbumDescriptor> getAlbumDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         ResultSet rs;
         Statement stmt;
         List<IAlbumDescriptor> descList = new ArrayList<IAlbumDescriptor>();
         String fileName = "resources/get_albums_basic.sql";
 
         try {
             String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                     getClass().getResource(fileName)));
             String strQuery = fileContent + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new AlbumDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (IOException ex) {
             throw new IDAL.DALException(ex);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
     }
 
     @Override
     public List<IAlbumDescriptor> getAlbumDescriptors(List<Integer> ids) throws IDAL.DALException {
         String tailTerm = String.format("and disc.id in (%s)", Joiner.on(",").join(ids));
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IAlbumDescriptor> getArtistAlbumDescriptors(IArtistDescriptor artist) throws IDAL.DALException {
         String tailTerm = String.format("and artist.id = %s", artist.getId());
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     /////////////////////////////////////////
     // Artist Descriptors
     /////////////////////////////////////////
     public List<IArtistDescriptor> getArtistDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs;
         Statement stmt;
         List<IArtistDescriptor> descList = new ArrayList<IArtistDescriptor>();
         String fileName = "resources/get_artists_basic.sql";
 
         try {
             String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                     getClass().getResource(fileName)));
 
             String strQuery = fileContent + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new ArtistDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (IOException ex) {
             throw new IDAL.DALException(ex);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
     }
 
     @Override
     public List<IArtistDescriptor> getArtistDescriptors(List<Integer> ids) throws IDAL.DALException {
         String tailTerm = String.format("and artist.id in (%s)", Joiner.on(",").join(ids));
         return getArtistDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public void CreateAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void DeleteAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void UpdateAlbum(IAlbumDescriptor album) {
         //TODO: update cached counts
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     //TODO: add to iterface?
     public void importXmcdBatch(List<XmcdDisc> xmcdDiscList) throws DALException {
 
         try {
             conn.setAutoCommit(false);
 
 
             /////////////////////////////////////////////////////////////////////////
             //TODO: move to caller method
 
             pstmtInsertGenre = conn.prepareStatement(
                     "INSERT IGNORE INTO genre(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
             pstmtInsertArtist = conn.prepareStatement(
                     "INSERT IGNORE INTO artist(name) VALUES(?)", Statement.RETURN_GENERATED_KEYS);
 
             pstmtInsertAlbum = conn.prepareStatement(
                     "INSERT IGNORE INTO disc "
                     + "(freedb_id, title, year, genre_id, freedb_genre_index, artist_id, revision) "
                     + "VALUES ("
                     + "?, " //freedb_id
                     + "?, " //title
                     + "?, " //year
                     + "(SELECT id FROM genre WHERE genre.name = ? LIMIT 1), " //genre_name
                     + "?, " //freedb_genre_index
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //aritst_name
                     + "?)"); //revision
 
             pstmtInsertTrack = conn.prepareStatement(
                     "INSERT IGNORE INTO track "
                     + "(disc_id, track_num, artist_id, len_in_sec, title)"
                     + "VALUES ("
                     //TODO: BIG TIME. 
                     //Change schema: remove disc_id, Primary key for disc should be only freedb_id.
 
                     + "(SELECT id FROM disc WHERE disc.freedb_id = ? LIMIT 1) , " //freedb_db_id
                     + "?, " //track_num
                     + "(SELECT id FROM artist WHERE artist.name = ? LIMIT 1), " //artist_name
                     + "?, " //len_in_sec
                     + "?)");    //title
 
             //TODO: add sets.
 
             Set<String> uniqueGenre = Sets.newHashSet();
             Set<String> uniqueArtist = Sets.newHashSet();
 
             for (XmcdDisc xmcdDisc : xmcdDiscList) {
 
                 AlbumRow albumRow = xmcdDisc.albumRow;
                 uniqueGenre.add(albumRow.freeTextGenre);
                 uniqueArtist.add(albumRow.artistName);
 
                 pstmtInsertAlbum.setInt(1, albumRow.freedbId);
                 pstmtInsertAlbum.setString(2, albumRow.title);
                 pstmtInsertAlbum.setInt(3, albumRow.year);
                 pstmtInsertAlbum.setString(4, albumRow.freeTextGenre);
                 pstmtInsertAlbum.setInt(5, albumRow.freedbGenre);
                 pstmtInsertAlbum.setString(6, albumRow.artistName);
                 pstmtInsertAlbum.setInt(7, albumRow.revision);
                 pstmtInsertAlbum.addBatch();
 
                 for (TrackRow trackRow : xmcdDisc.trackRows) {
                     pstmtInsertTrack.setInt(1, albumRow.freedbId);
                     pstmtInsertTrack.setInt(2, trackRow.trackNum);
                     pstmtInsertTrack.setString(3, trackRow.artistName);
                     pstmtInsertTrack.setInt(4, trackRow.lenInSec);
                     pstmtInsertTrack.setString(5, trackRow.title);
                     pstmtInsertTrack.addBatch();
                 }
             }
 
             for (String genre : uniqueGenre) {
                 pstmtInsertGenre.setString(1, genre);
                 pstmtInsertGenre.addBatch();
             }
 
             for (String artist : uniqueArtist) {
                 pstmtInsertArtist.setString(1, artist);
                 pstmtInsertArtist.addBatch();
             }
 
             pstmtInsertGenre.executeBatch();
             pstmtInsertArtist.executeBatch();
             pstmtInsertAlbum.executeBatch();
             pstmtInsertTrack.executeBatch();
 
             conn.commit();
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 }
 //            pstmtInsertTrack = conn.prepareStatement(
 //                    "INSERT IGNORE INTO track " 
 //                    + "(disc_id, track_num, title, artist_id, len_in_sec) "
 //                    + "VALUES (?, ?, ?, ?, ?, ?)");
 //TODO: If find any solution for getting all keys from transaction, including non-generated, 
 //change to the following code           
 //
 //            Multimap<String, HasArtist> mmArtistDataRow = ArrayListMultimap.create();
 //            Multimap<String, HasArtist> mmGenreAlbumRow = ArrayListMultimap.create();
 //
 //            
 //            for(XmcdDisc xmcdDisc : xmcdDiscList) {
 //                mmArtistDataRow.put(xmcdDisc.albumRow.artistName, xmcdDisc.albumRow);
 //                mmArtistDataRow.putAll(xmcdDisc.albumRow.artistName, xmcdDisc.trackRows);
 //                mmGenreAlbumRow.put(xmcdDisc.albumRow.freeTextGenre, xmcdDisc.albumRow);
 //            }
 //            
 //            for (String artist : mmArtistDataRow.keySet()) {
 //                pstmtInsertArtist.setString(1, artist);
 //                pstmtInsertArtist.addBatch();
 //            }
 //            
 //            for (String genre : mmGenreAlbumRow.keySet()) {
 //                pstmtInsertGenre.setString(1, genre);
 //                pstmtInsertGenre.addBatch();
 //            }
 //            
 //            pstmtInsertGenre.executeBatch();
 //            pstmtInsertArtist.executeBatch();
 //            
 //            ResultSet rsGenreKeys = pstmtInsertGenre.getGeneratedKeys();
 //            ResultSet rsArtistKeys = pstmtInsertGenre.getGeneratedKeys();
 //            
 //            for (Entry<String, Collection<HasArtist>> artistItems : mmArtistDataRow.asMap().entrySet()) {
 //                
 //                for (HasArtist dataRow : artistItems.getValue()) {
 //                    dataRow.artistId = rsArtistKeys.getInt("id");
 //                }
 //            }
