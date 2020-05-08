 package my.triviagame.dal;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 import java.io.IOException;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import my.triviagame.xmcd.XmcdDisc;
 import org.apache.commons.io.FileUtils;
 
 public class DAL implements IDAL {
 
     //DB connection
     private Connection conn = null;
     //cahched table counters
     private Integer albumCount = null;
     private Integer trackCount = null;
     private Integer artistCount = null;
     //Prepared Statements for insetions
     private PreparedStatement pstmtInsertGenre = null;
     private PreparedStatement pstmtInsertArtist = null;
     private PreparedStatement pstmtInsertAlbum = null;
     private PreparedStatement pstmtInsertTrack = null;
     //Prepared Statemnets for table counters
     //Sadly, cannot pass table names as parameters to a query! must use three different members
     private PreparedStatement pstmtGetTrackTableCount = null;
     private PreparedStatement pstmtGetAlbumTableCount = null;
     private PreparedStatement pstmtGetArtistTableCount = null;
     
     private String strGetBasicTracks = null;
     private String strGetBasicAlbums = null;
     private String strGetBasicArtists = null;
             
 
     //TODO: remove
     public Connection getConnection() {
         return conn;
     }
 
     /**
      * 1. Opens connection to DB according to specifies parameters 2. Prepare all relevant prepared statements
      *
      * @param hostName
      * @param port
      * @param dbName
      * @param userName
      * @param password
      * @throws my.triviagame.dal.IDAL.DALException
      */
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
             readResourceSqlFiles();
             prepareQueryStatements();
             prepareInsertionStatements();
         } catch (IOException ex) {
             throw new DALException(ex);
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
     }
 
     @Override
     public void closeConnection() throws IDAL.DALException {
         try {
             destoryQueryStatements();
             destroyInsertionStatements();
             conn.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         conn = null;
     }
 
     //////////////////////////////////////////
     // Table Counters 
     //////////////////////////////////////////
     private int getTableCount(PreparedStatement pstmt, Integer cachedCount) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         try {
             if (cachedCount != null) {
                 return cachedCount;
             }
 
             ResultSet rs = pstmt.executeQuery();
             rs.next();
             cachedCount = rs.getInt("COUNT(*)");
             rs.close();
 
             return cachedCount;
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
     }
 
     @Override
     public int getTrackCount() throws IDAL.DALException {
         return getTableCount(pstmtGetTrackTableCount, trackCount);
     }
 
     @Override
     public int getAlbumCount() throws IDAL.DALException {
         return getTableCount(pstmtGetAlbumTableCount, albumCount);
     }
 
     @Override
     public int getArtistCount() throws IDAL.DALException {
         return getTableCount(pstmtGetArtistTableCount, artistCount);
     }
 
     @Override
     public List<ITrackDescriptor> getTrackDescriptors(List<Integer> ids) throws IDAL.DALException {
         String tailTerm = String.format("AND track.id IN (%s)", Joiner.on(",").join(ids));
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getAlbumTrackDescriptors(IAlbumDescriptor album) throws DALException {
         String tailTerm = "AND disc.id = " + album.getId();
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<ITrackDescriptor> getArtistTrackDescriptors(IArtistDescriptor artist)
             throws IDAL.DALException {
         String tailTerm = "AND track_artist.id = " + artist.getId();
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IAlbumDescriptor> getAlbumDescriptors(List<Integer> ids) throws DALException {
         String tailTerm = String.format("AND disc.id IN (%s)", Joiner.on(",").join(ids));
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IAlbumDescriptor> getArtistAlbumDescriptors(IArtistDescriptor artist) throws DALException {
         String tailTerm = "AND artist.id = " + artist.getId();
         return getAlbumDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public List<IArtistDescriptor> getArtistDescriptors(List<Integer> ids) throws DALException {
         String tailTerm = String.format("AND artist.id IN (%s)", Joiner.on(",").join(ids));
         return getArtistDescriptorsByTailTerm(tailTerm);
     }
 
     @Override
     public Collection<ITrackDescriptor> getTrackDescriptors(String... keywords) throws DALException {
         List<String> keywordTerms = new ArrayList<String>(keywords.length);
         for (String keyword : keywords) {
             String tempKeyword = keyword.replace("'", "\"");
             keywordTerms.add(String.format("all_text like '%%%s%%'", tempKeyword.toLowerCase()));
         }
         String tailTerm = Joiner.on(" and ").join(keywordTerms);
         tailTerm = String.format("HAVING %s", tailTerm);
         return getTrackDescriptorsByTailTerm(tailTerm);
     }
 
     
 
     //returns all tracks that matches tail term, spelled in SQL
     private List<ITrackDescriptor> getTrackDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs;
         Statement stmt;
         List<ITrackDescriptor> descList = new ArrayList<ITrackDescriptor>();
 
         try {
             String strQuery = strGetBasicTracks + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new TrackDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
     }
     
     private List<IAlbumDescriptor> getAlbumDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs;
         Statement stmt;
         List<IAlbumDescriptor> descList = new ArrayList<IAlbumDescriptor>();
 
         try {
             String strQuery = strGetBasicAlbums + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new AlbumDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
     }
     
     private List<IArtistDescriptor> getArtistDescriptorsByTailTerm(String tailTerm) throws IDAL.DALException {
         Preconditions.checkNotNull(conn, "Connection closed");
         ResultSet rs;
         Statement stmt;
         List<IArtistDescriptor> descList = new ArrayList<IArtistDescriptor>();
 
         try {
             String strQuery = strGetBasicArtists + " " + tailTerm;
             stmt = conn.createStatement();
             rs = stmt.executeQuery(strQuery);
             while (rs.next()) {
                 descList.add(new ArtistDescriptor(rs));
             }
             stmt.close();
             rs.close();
         } catch (SQLException ex) {
             throw new IDAL.DALException(ex);
         }
         return descList;
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
 
     @Override
     public void importXmcdBatch(List<XmcdDisc> xmcdDiscList) throws DALException {
 
         try {
             conn.setAutoCommit(false);
 
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
 
                     uniqueArtist.add(trackRow.artistName);
 
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
 
     /////////////////////////////////////////////
     // HELPERS
     /////////////////////////////////////////////
 
     private String getResourceFileContents(String fileName) throws IOException {
         String fileContent = FileUtils.readFileToString(FileUtils.toFile(
                 getClass().getResource(fileName)));
         return fileContent;
     }
     
     private void readResourceSqlFiles() throws IOException {
         strGetBasicTracks = getResourceFileContents("resources/get_tracks_basic.sql");
         strGetBasicAlbums = getResourceFileContents("resources/get_albums_basic.sql");
         strGetBasicArtists = getResourceFileContents("resources/get_artists_basic.sql");
     }
     
     private void prepareQueryStatements() throws DALException {
         try {
             pstmtGetTrackTableCount = conn.prepareStatement("SELECT COUNT(*) FROM track");
             pstmtGetAlbumTableCount = conn.prepareStatement("SELECT COUNT(*) FROM disc");
             pstmtGetArtistTableCount = conn.prepareStatement("SELECT COUNT(*) FROM artist");
 
 //            pstmtGetTracksById = getEntityPs("resources/get_tracks_basic.sql", "and track.id in (?)");
 //            pstmtGetTracksByAlbum = getEntityPs("resources/get_tracks_basic.sql", "and disc.id = ?");
 //            pstmtGetTracksByArtist = getEntityPs("resources/get_tracks_basic.sql", "and artist.id = ?");
 //
 //            pstmtGetAlbumsById = getEntityPs("resources/get_albums_basic.sql", "and disc.id in (@list)");
 //            pstmtGetAlbumsByArtist = getEntityPs("resources/get_albums_basic.sql", "and artist.id = ?");
 //
 //            pstmtGetArtistsById = getEntityPs("resources/get_artists_basic.sql", "and artist.id in (?)");
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void destroyPreparedStatement(PreparedStatement pstmt) throws SQLException {
         if (pstmt != null) {
             pstmt.close();
             pstmt = null;
         }
     }
 
     private void destoryQueryStatements() throws DALException {
         try {
             destroyPreparedStatement(pstmtGetTrackTableCount);
             destroyPreparedStatement(pstmtGetAlbumTableCount);
             destroyPreparedStatement(pstmtGetArtistTableCount);
 
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void prepareInsertionStatements() throws DALException {
         try {
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
         } catch (SQLException ex) {
             throw new DALException(ex);
         }
     }
 
     private void destroyInsertionStatements() throws DALException {
         try {
             destroyPreparedStatement(pstmtInsertGenre);
             destroyPreparedStatement(pstmtInsertArtist);
             destroyPreparedStatement(pstmtInsertAlbum);
             destroyPreparedStatement(pstmtInsertGenre);
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
