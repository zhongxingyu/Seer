 /**
 * This interface describes all posible operation with albums.
 * @author Andrey Parhomenko
 * @author Sergiy Stetsyun
 * @version 1.0
 * @date 18.05.2010
 */ 
 
 package ua.edu.sumdu.lab3.model;
 
 import ua.edu.sumdu.lab3.model.exceptions.*;
 import java.util.Date;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.sql.*;
 import javax.sql.*;
 import javax.naming.*;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 
 public class OracleDAO implements OperableDAO {
     
     private static final int ALBUM = 0;
     private static final int ARTIST = 1;
     private static final int LABEL = 2;
     
     private static final int FULL_MODE = 10;
     private static final int SHORT_MODE = 9;
     
     private static final String ADD_NEW_ALBUM = 
             "INSERT INTO ALBUM VALUES (SQ_ALBUM.nextval, ?, ?, ?, ?, ?, ?, ?, ?)";
             
     private static final String ADD_NEW_LABEL = 
             "INSERT INTO LABEL VALUES (SQ_LABEL.nextval, ?, ?, ?, ?)";
             
     private static final String ADD_NEW_ARTIST = 
             "INSERT INTO ARTIST VALUES (SQ_ARTIST.nextval, ?, ?, ?)";
 
     private static final String EDIT_ALBUM = 
             "UPDATE ALBUM SET alid = ?, name = ?, type = ?, release = ?, genre = ?, cover = ?, art = ?,review = ?, lbl = ? WHERE alid = ?";
             
     private static final String EDIT_ARTIST = 
             "UPDATE ARTIST SET aid = ?, name = ?, country = ?, info = ? where aid = ?";
     
     private static final String EDIT_LABEL = 
             "UPDATE LABEL SET major = ?, name = ?, info = ?, logo = ? where lid = ?";
     
     private static final String SELECT_ALBUM_BY_ID = 
             "SELECT album.alid, album.name, album.type, album.release, album.genre, album.cover, album.review, album.art, album.lbl, artist.name, label.name FROM album JOIN artist ON (album.art = artist.aid) JOIN label ON (album.lbl = label.lid) WHERE album.alid = ?";
     
     private static final String SELECT_LABEL_BY_ID = 
             "SELECT a.lid,a.major,a.name,a.logo,a.info,a.major_name FROM (SELECT d.lid,d.major,d.name,d.logo,d.info,e.name as major_name FROM label d, label e WHERE d.major = e.lid UNION SELECT d.lid,d.major,d.name,d.logo,info,null as major_name FROM label d WHERE d.major IS NULL) a where a.lid = ?";
     
     private static final String SELECT_ARTIST_BY_ID = 
             "SELECT * FROM ARTIST WHERE aid = ?";
     
     private static final String SELECT_ALBUMS_BY_GENRE = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) WHERE INSTR(LOWER(album.genre),LOWER(?)) != 0 order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
     
     private static final String SELECT_ALBUMS_BY_NAME = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) WHERE INSTR(LOWER(album.name),LOWER(?)) != 0 order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
     
     private static final String SELECT_ALBUMS_BY_DATE = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) WHERE TO_CHAR(album.release,'YYYY') LIKE ? order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
     
     private static final String SELECT_ALBUMS_BY_ARTIST = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) where album.art = ? order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
     
     private static final String SELECT_ALBUMS_BY_LABEL = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) where album.lbl = ? order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
             
     private static final String SELECT_ARTISTS_BY_LABEL = 
             "SELECT DISTINCT artist.aid,artist.name,artist.country,artist.info FROM artist JOIN album ON(artist.aid = album.art) WHERE album.lbl = ?";
             
     private static final String SELECT_ARTISTS_BY_COUNTRY = 
             "SELECT * FROM (SELECT a.*, rownum rnum FROM(SELECT * FROM artist WHERE country like ?) a WHERE ROWNUM <=?) WHERE rnum >=?";
             
     private static final String SELECT_GENRES_BY_ARTIST = 
             "SELECT DISTINCT genre FROM ALBUM WHERE art = ?"; 
     
     private static final String SELECT_GENRES_BY_LABEL = 
             "SELECT DISTINCT genre FROM ALBUM WHERE lbl = ?";
     
     private static final String SELECT_ALL_LABELS = 
             "SELECT lid,name,logo from label";
     
     private static final String SELECT_ALL_DATES = 
             "SELECT DISTINCT TO_CHAR(release,'YYYY')as year FROM album order by year";
     
     private static final String SELECT_ALL_ARTISTS = 
             "SELECT c.aid,c.name,c.country,c.info FROM (SELECT a.*, rownum rnum FROM (SELECT * FROM artist ORDER BY aid) a WHERE rownum <= ?) c where rnum >= ?";
     
     private static final String SELECT_ALL_ALBUMS = 
             "SELECT b.alid,b.name,b.type,b.release,b.genre,b.cover,b.review,b.art,b.lbl,b.artist_name,b.label_name FROM (SELECT a.*, rownum rnum FROM (SELECT album.*,artist.name as artist_name, label.name as label_name from album join artist on (album.art = artist.aid) join label on (album.lbl = label.lid) order by album.alid) a WHERE rownum <= ?) b where rnum >= ?";
             
     private static String FIND_ALBUMS = 
             "SELECT alid FROM ALBUM WHERE ";
     
     private static final String LATEST_ALBUMS =
             "select a.alid, a.name, a.type, a.release, a.genre, a.cover, a.review, a.art, a.lbl, artist.name, label.name from (select * from album order by alid desc) a JOIN artist ON (a.art = artist.aid) JOIN label ON (a.lbl = label.lid) where ROWNUM <= ?";
             
     private static final String FIND_ARTIST = 
             "SELECT aid FROM artist WHERE (INSTR(LOWER(name), LOWER(?)) != 0)";
             
     private static final String FIND_LABEL = 
             "SELECT lid FROM label WHERE (INSTR(LOWER(name), LOWER(?)) != 0)";
     
     private static final String ALBUM_MAX_ROW =
             "SELECT MAX(ROWNUM) FROM album";
             
     private static final String ALBUM_BY_DATE_MAX_ROW = 
             "SELECT MAX(ROWNUM) from album  WHERE TO_CHAR(release,'YYYY') LIKE ?";
             
     private static final String ALBUM_BY_GENRE_MAX_ROW =
             "SELECT MAX(ROWNUM) from album  WHERE INSTR(LOWER(genre),LOWER(?)) != 0";
             
     private static final String ARTIST_MAX_ROW =
             "SELECT MAX(ROWNUM) FROM artist";
     
     private static final String ARTIST_BY_COUNTRY_MAX_ROW =
             "SELECT MAX(ROWNUM) FROM artist WHERE country LIKE ?";
 
     private static final String LABEL_MAX_ROW =
             "SELECT MAX(ROWNUM) FROM label WHERE major IS NULL";
             
     private static final String ALBUM_RANDOM = 
             "SELECT a.alid, a.name, a.type, a.release, a.genre, a.cover, a.review, a.art, a.lbl, artist.name, label.name FROM ( SELECT * FROM album ORDER BY dbms_random.value ) a JOIN artist ON (a.art = artist.aid) JOIN label ON (a.lbl = label.lid) WHERE rownum = 1";
     
     private static final String DELETE_ALBUM =
             "DELETE FROM album WHERE alid = ?";
     
     private static final String DELETE_ARTIST =
             "DELETE FROM artist WHERE aid = ?";
     
     private static final String DELETE_LABEL = 
             "DELETE FROM label WHERE lid = ?";
     
     private static final String GET_CHILD_LABELS = 
             "SELECT lid,name,logo FROM label where major = ?";
 
     private static final String GET_MAJOR_LABELS = 
             "SELECT b.lid,b.name,b.logo FROM (SELECT a.*, rownum rnum FROM (SELECT d.lid,d.name,d.logo FROM label d WHERE d.major IS NULL ORDER BY lid) a WHERE rownum <= ?) b WHERE rnum >=?";
 
     private static final String LABEL_GET_PATH = 
             "SELECT * FROM (SELECT lid,name,logo FROM label START WITH lid = ? CONNECT BY PRIOR major = lid) ORDER BY lid";
                     
     private Connection connection = null;
     private PreparedStatement statement = null;
     
     private Logger log = null;
     
     private static OracleDAO instance = null;
     
     
     protected OracleDAO() {
         log = Logger.getLogger(OracleDAO.class);
     }
     
     /**
      * Returns new instance of this class if it does not exist.
      * @return new instance of this class.
      */ 
     public static OracleDAO getInstance() {
         if (instance == null) {
             instance = new OracleDAO();
         } 
         return instance;
     }
     
     /**
      * Setups the connection with database by specified parameters.
      */ 
     private void getConnection() throws ConnectionException {
         try {
             InitialContext iContext = new InitialContext();
             Context context = (Context) iContext.lookup("java:comp/env");
             DataSource datasource = (DataSource)context.lookup("jdbc/DiscsDB");
             connection = datasource.getConnection();
         } catch (javax.naming.NamingException e){
             throw new ConnectionException(e);
         } catch (SQLException e) {
             throw new ConnectionException(e);
         }
     }
     
     /**
      * This method execute result query of the defined statement.
      */ 
     private ResultSet executeResultQuery() throws ExecuteQueryException {
         ResultSet result = null;
         try {
             if (this.connection != null) {
                 result = this.statement.executeQuery();
             } else {
                 throw new ExecuteQueryException("Unable to execute query");
             }
         } catch (SQLException e) {
             throw new ExecuteQueryException(e);
         }
         return result;
     }
 
     /**
      * This method execute update query of the defined statement.
      */ 
     private int executeUpdateQuery() throws ExecuteQueryException {
         int result = -1;
         try {
             if (this.connection == null) {
                 throw new ExecuteQueryException(
                         "Connection is not created");                
             } 
             if (this.statement == null) {
                 throw new ExecuteQueryException("Statement is null"); 
             }
             result = this.statement.executeUpdate();
         } catch (SQLException e) {
             throw new ExecuteQueryException(e);
         }
         return result;
     }
     
     /**
      * Closes connection with database. 
      */
     private void closeConnection() throws ConnectionException {
         if (connection != null) {
             try {
                 connection.close();
             } catch (SQLException e) {
                 throw new ConnectionException(e);
             }
         }
         
         if (statement != null) {
             try {
                 statement.close();
             } catch (SQLException e) {
                 throw new ConnectionException(e);
             }
         }
     }
     
 
     private Object fillBean(ResultSet set,int beanType,int mode) throws GetDataException {
         Object bean = null;
         try {
             switch(beanType) {
                 case ALBUM:
                     Album alb = new Album();
                     if(mode == FULL_MODE) {
                         alb.setId(set.getInt(1));
                         alb.setName(set.getString(2));
                         alb.setType(set.getString(3));
                         alb.setRelease(new java.util.Date(set.getDate(4).getTime()));
                         alb.setGenre(set.getString(5));
                         alb.setCover(set.getString(6));
                         alb.setReview(set.getString(7));
                         alb.setArtist(set.getInt(8));
                         alb.setLabel(set.getInt(9));
                         alb.setArtistName(set.getString(10));
                         alb.setLabelName(set.getString(11));
                     } else {
                         alb.setId(set.getInt(1));
                         alb.setName(set.getString(2));
                     }
                     bean = alb;
                 break;
                 
                 case ARTIST:
                     Artist art = new Artist();
                     if(mode == FULL_MODE) {
                         art.setId(set.getInt(1));
                         art.setName(set.getString(2));
                         art.setCountry(set.getString(3));
                         art.setInfo(set.getString(4));
                     } else {
                         art.setId(set.getInt(1));
                         art.setName(set.getString(2));
                     }
                     bean = art;
                 break;
                 
                 case LABEL:
                     Label lbl = new Label();
                     if(mode == FULL_MODE) {
                         lbl.setId(set.getInt(1));
                         lbl.setMajor(set.getInt(2));
                         lbl.setName(set.getString(3));
                         lbl.setLogo(set.getString(4));
                         lbl.setInfo(set.getString(5));
                         lbl.setMajorName(set.getString(6));
                     } else {
                         lbl.setId(set.getInt(1));
                         lbl.setName(set.getString(2));
                         lbl.setLogo(set.getString(3));
                     }
                     bean = lbl;
                 break;
             }
         } catch (SQLException e) {
             throw new GetDataException(e);
         }
         return bean;
     }
 
     /**
      * Adds new album to the specified storage.
      * @param album new instanse of the Album that should be added.
      * @throws AddDataException if problems while adding the data.
      */ 
     public void addAlbum(Album album) throws AddDataException {
         try {
             this.getConnection();
             if (this.connection != null) {
                 this.statement = this.connection.prepareStatement(
                         ADD_NEW_ALBUM);
 
                 java.util.Date alDate = (java.util.Date)album.getRelease();
                 
                 this.statement.setString(1,album.getName());
                 this.statement.setString(2,album.getType());
                 this.statement.setDate(3, 
                         new java.sql.Date(alDate.getTime()));
                 this.statement.setString(4,album.getGenre());
                 this.statement.setString(5,album.getCover());
                 this.statement.setString(6,album.getReview());
                 this.statement.setInt(7,album.getArtist());
                 this.statement.setInt(8,album.getLabel());
                 this.connection.setAutoCommit(false);
                 this.executeUpdateQuery();
                 this.connection.commit();
             } else {
                 throw new AddDataException("Unable to add album");
             }
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new AddDataException(exc);
             }
             throw new AddDataException(e);
         } catch (ConnectionException e) {
             throw new AddDataException(e);
         } catch (ExecuteQueryException e) {
             throw new AddDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new AddDataException(e);
             } catch (ConnectionException e) {
                 throw new AddDataException(e);
             }
         }
     }
 
     /**
      * Adds new label to the specified storage.
      * @param album new instanse of the Label that should be added.
      * @throws AddDataException if problems while adding the data.
      */ 
     public void addLabel(Label label) throws AddDataException {        
         try {
             this.getConnection();
             if (this.connection != null) {
                 this.statement = this.connection.prepareStatement(
                         ADD_NEW_LABEL);
                 
                 int major = label.getMajor();
                 if (major == 0) {       
                     this.statement.setNull(1, label.getMajor());
                 } else {
                     this.statement.setInt(1, label.getMajor());
                 }
                 this.statement.setString(2, label.getName());
                 this.statement.setString(3, label.getLogo());
                 this.statement.setString(4, label.getInfo());
                 this.connection.setAutoCommit(false);
                 this.executeUpdateQuery();
                 this.connection.commit();
             } else {
                 throw new AddDataException("Unable to add label");
             } 
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new AddDataException(exc);
             }
             throw new AddDataException(e);
         } catch (ConnectionException e) {
             throw new AddDataException(e);
         } catch (ExecuteQueryException e) {
             throw new AddDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new AddDataException(e);
             } catch (ConnectionException e) {
                 throw new AddDataException(e);
             }
         }
     }
 
     /**
      * Adds new artist to the specified storage.
      * @param album new instanse of the Artist that should be added.
      * @throws AddDataException if problems while adding the data.
      */ 
     public void addArtist(Artist artist) throws AddDataException {
         try {
             this.getConnection();
             
             if (this.connection == null) {
                 throw new AddDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     ADD_NEW_ARTIST);
             
             this.statement.setString(1, artist.getName());
             this.statement.setString(2, artist.getCountry());
             this.statement.setString(3, artist.getInfo());
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
             
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new AddDataException(e);
             }
             throw new AddDataException(e);
         } catch (ConnectionException e) {
             throw new AddDataException(e);
         } catch (ExecuteQueryException e) {
             throw new AddDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new AddDataException(e);
             } catch (ConnectionException e) {
                 throw new AddDataException(e);
             }
         }
     }
 
     /**
      * Returns the album by the specified id.
      * 
      * @param id id of the album that should be return.
      * @return album by the specified id.
      * @throws GetDataException if problems while getting data.
      */ 
     public Album getAlbum(int id) throws GetDataException {
         Album album = null;
         try {
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUM_BY_ID);
             
             this.statement.setInt(1, id);
             
             ResultSet set = this.executeResultQuery();
             if(set.next()) {
                 album = (Album)fillBean(set,ALBUM,FULL_MODE);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return album;
     }
     
     /**
      * Returns the label by the specified id.
      * 
      * @param id id of the label that should be return.
      * @return label by the specified id.
      * @throws GetDataException if problems while getting data.
      */ 
     public Label getLabel(int id) throws GetDataException {
         Label label = null;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_LABEL_BY_ID);
             
             this.statement.setInt(1, id);
             
             ResultSet set = this.executeResultQuery();
             if(set.next()) {
                 label = (Label)fillBean(set,LABEL,FULL_MODE);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return label;    
     }
     
     /**
      * Returns the artist by the specified id.
      * 
      * @param id id of the artist that should be return.
      * @return artist by the specified id.
      * @throws GetDataException if problems while getting data.
      */ 
     public Artist getArtist(int id) throws GetDataException {
         Artist artist = null;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ARTIST_BY_ID);
             
             this.statement.setInt(1, id);
             
             ResultSet set = this.executeResultQuery();
             if(set.next()) {
                 artist = (Artist)fillBean(set,ARTIST,FULL_MODE);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return artist;    
     }
 
     /**
      * Returns list of albums of the specified genre.
      * @param genre genre of the album.
      * @return list of albums of the specified genre.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getAlbums(String genre, int firstRow, int lastRow) 
             throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUMS_BY_GENRE);
             
             this.statement.setString(1, genre);
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
 
     /**
      * Returns list of albums of the specified name.
      * @param name name of the album.
      * @return list of albums of the specified name.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getAlbumsByName(String name, int firstRow, int lastRow) 
             throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUMS_BY_NAME);
             
             this.statement.setString(1, name);
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
     
     /**
      * Returns list of albums of the specified date.
      * @param date date of the album.
      * @return list of albums of the specified date.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getAlbums(Date date, int firstRow, int lastRow) 
             throws GetDataException {
         DateFormat df = new SimpleDateFormat("yyyy");
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             
             this.getConnection();
             
             if (this.connection == null) {
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUMS_BY_DATE);
                     
             this.statement.setString(1, df.format(date));
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
     
      /**
      * Returns list of albums of the specified artist.
      * @param artist artist of the album.
      * @return list of albums of the specified artist.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getAlbums(Artist artist, int firstRow, int lastRow) 
             throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUMS_BY_ARTIST);
             
             this.statement.setInt(1, artist.getId());
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
     
     /**
     * Returns list of albums of the specified label.
     * @param label label of the album.
     * @return list of albums of the specified label.
     * @throws GetDataException if problems while getting data.
     */ 
     public List getAlbums(Label label, int firstRow, int lastRow)  
             throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALBUMS_BY_LABEL);
             
             this.statement.setInt(1, label.getId());
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
 
     /**
     * Returns list of artists of the specified label.
     * @param label label of the artist.
     * @return list of artists of the specified label.
     * @throws GetDataException if problems while getting data.
     */
     public List getArtists(Label label) throws GetDataException {
         List artists = null;
         try {
             artists = new ArrayList();
             Artist currArtist;
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ARTISTS_BY_LABEL);
             
             this.statement.setInt(1,label.getId());
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currArtist = (Artist)fillBean(set,ARTIST,FULL_MODE);
                 artists.add(currArtist);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return artists;
     }
 
     /**
     * Returns list of artists of the specified country.
     * @param country country of the artist.
     * @return list of artists of the specified country.
     * @throws GetDataException if problems while getting data.
     */
     public List getArtists(String country, int firstRow, int lastRow) 
             throws GetDataException {
         List artists = null;
         try {
             artists = new LinkedList();
             Artist currArtist;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ARTISTS_BY_COUNTRY);
             
             this.statement.setString(1, country);
             this.statement.setInt(2,lastRow);
             this.statement.setInt(3,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currArtist = (Artist)fillBean(set,ARTIST,FULL_MODE);
                 artists.add(currArtist);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return artists;
     
     }
 
     /**
     * Returns list of genres of the specified artist.
     * @param artist artist of the genre.
     * @return list of genres of the specified artist.
     * @throws GetDataException if problems while getting data.
     */
     public List getGenres(Artist artist) throws GetDataException {
         List genres = null;
         try {
             genres = new LinkedList();
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_GENRES_BY_ARTIST);
             
             this.statement.setInt(1, artist.getId());
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 genres.add(set.getString(1));
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return genres;
     }
 
     /**
     * Returns list of genres of the specified label.
     * @param label label of the genre.
     * @return list of genres of the specified label.
     * @throws GetDataException if problems while getting data.
     */
     public List getGenres(Label label) throws GetDataException {
         List genres = null;
         try {
             genres = new LinkedList();
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_GENRES_BY_LABEL);
             
             this.statement.setInt(1, label.getId());
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 genres.add(set.getString(1));
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e){
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return genres;
     }
 
     /**
     * Returns list of all labels.
     * @return list of all labels.
     * @throws GetDataException if problems while getting data.
     */
     public List getLabels() throws GetDataException {
         List labels = null;
         try {
             labels = new LinkedList();
             Label currLabel;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALL_LABELS);
             
             ResultSet set = this.executeResultQuery();
             
             while(set.next()){
                 currLabel = (Label)fillBean(set,LABEL,SHORT_MODE);
                 labels.add(currLabel);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return labels;
     }
 
     public List getLabels(int firstRow, int lastRow)
             throws GetDataException {
         List labels = null;
         try {
             labels = new LinkedList();
             Label currLabel;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     GET_MAJOR_LABELS);
             this.statement.setInt(1,lastRow);
             this.statement.setInt(2,firstRow);
             
             ResultSet set = this.executeResultQuery();
             
             while(set.next()){
                 currLabel = (Label)fillBean(set,LABEL,SHORT_MODE);
                 labels.add(currLabel);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return labels;
     }
 
     public List getLabels(int id) throws GetDataException {
         List labels = null;
         try {
             labels = new LinkedList();
             Label currLabel;
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     GET_CHILD_LABELS);
             this.statement.setInt(1,id);
             
             ResultSet set = this.executeResultQuery();
             
             while(set.next()){
                 currLabel = (Label)fillBean(set,LABEL,SHORT_MODE);
                 labels.add(currLabel);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return labels;
     }
 
     /**
     * Returns list of all dates.
     * @return list of all dates.
     * @throws GetDataException if problems while getting data.
     */
     public List getDates() throws GetDataException {
         List dates = null;
         try {
             dates = new LinkedList();
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALL_DATES);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 dates.add(set.getString(1));
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return dates;
     }
 
     /**
     * Returns list of all artists.
     * @return list of all artists.
     * @throws GetDataException if problems while getting data.
     */
     public List getArtists(int firstRow, int lastRow) throws GetDataException {
         List artists = null;
         try {
             artists = new LinkedList();
             Artist currArtist;
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALL_ARTISTS);
             this.statement.setInt(1,lastRow);
             this.statement.setInt(2,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currArtist = (Artist)fillBean(set,ARTIST,FULL_MODE);
                 artists.add(currArtist);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return artists; 
     }
     
     /**
     * Returns list of all albums.
     * @return list of all albums.
     * @throws GetDataException if problems while getting data.
     */
     public List getAlbums(int firstRow, int lastRow) throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     SELECT_ALL_ALBUMS);
             this.statement.setInt(1,lastRow);
             this.statement.setInt(2,firstRow);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums; 
     }
 
     /**
      * Findes and returns list of albums by the specified params.
      * @param params parameters to find by.
      * @return list of albums by the specified params.
      * @throws GetDataException if problems while getting data.
      */ 
     public List findAlbums(Map params, int firstRow, int lastRow) 
             throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
 
             Set s = params.entrySet();
             Iterator it = s.iterator();
             StringBuffer query = new StringBuffer();
             while(it.hasNext()) {
 
                 Map.Entry m = (Map.Entry)it.next();
 
                 String key = (String)m.getKey();
 
                 String value = m.getValue().toString();
                 if (!"".equals(value) && (value != null)) {
                     query.append(key).append(" = ").append(value).append(" ");
                     if (it.hasNext()) {
                         query.append(" OR ");
                     }
                 }
             }
             log.info(query);
             FIND_ALBUMS += query.toString();
             
             this.statement = 
                     this.connection.prepareStatement(FIND_ALBUMS);
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 albums.add(getAlbum(set.getInt(1)));
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
 
     /**
      * Edits specified album. Replaces found (by id) album by specified.
      * 
      * @param album to edit/change.
      * @throws EditDataException if problems while editting data.
      */ 
     public void editAlbum(Album album) throws EditDataException {
         try {
             Album alb = getAlbum(album.getId());
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
             
             if (alb == null) {
                 throw new EditDataException("No album with specified id found");
             } 
             
             this.statement = this.connection.prepareStatement(EDIT_ALBUM);
             
             this.statement.setInt(1, album.getId());
             this.statement.setString(2, album.getName());
             this.statement.setString(3, album.getType());
             this.statement.setDate(4, 
                     new java.sql.Date(album.getRelease().getTime()));
             this.statement.setString(5, album.getGenre());
             this.statement.setString(6, album.getCover());
             this.statement.setInt(7, album.getArtist());
             this.statement.setString(8, album.getReview());
             this.statement.setInt(9, album.getLabel());
             this.statement.setInt(10, album.getId());
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
             
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (GetDataException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(exc);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
     }
     
     /**
      * Edits specified artist. Replaces found (by id) label by specified.
      * 
      * @param artist to edit/change.
      * @throws EditDataException if problems while editting data.
      */ 
     public void editArtist(Artist artist) throws EditDataException {
         try {
             Artist art = getArtist(artist.getId());
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
             
             if (art == null) {
                 throw new EditDataException("No artist with specified id found");
             } 
             
             this.statement = this.connection.prepareStatement(EDIT_ARTIST);
             
             this.statement.setInt(1, artist.getId());
             this.statement.setString(2, artist.getName());
             this.statement.setString(3, artist.getCountry());
             this.statement.setString(4, artist.getInfo());
             this.statement.setInt(5, artist.getId());
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (GetDataException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(exc);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
     }
 
     /**
      * Edits specified label. Replaces found (by id) label by specified.
      * 
      * @param label to edit/change.
      * @throws EditDataException if problems while editting data.
      */ 
     public void editLabel(Label label) throws EditDataException {
         try {
             Label lbl = getLabel(label.getId());
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
             
             if (lbl == null) {
                 throw new EditDataException("No label with specified id found");
             } 
             
             this.statement = this.connection.prepareStatement(EDIT_LABEL);
         
             if (label.getMajor() == 0) {
                 this.statement.setNull(1, label.getMajor());
             } else {
                 this.statement.setInt(1, label.getMajor());
             }
             this.statement.setString(2, label.getName());
             this.statement.setString(3, label.getInfo());
             this.statement.setString(4, label.getLogo());
             this.statement.setInt(5, label.getId());
             
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (GetDataException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(e);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
     }
     
     /**
      * Returns list of the <code>number</code> lastest albums.
      * @param number number of albums to return.
      * @return list of the <code>number</code> lastest albums.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getLatestAlbums(int number) throws GetDataException {
         List albums = null;
         try {
             albums = new LinkedList();
             Album currAlbum;
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(LATEST_ALBUMS);
             this.statement.setInt(1,number);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 currAlbum = (Album)fillBean(set,ALBUM,FULL_MODE);
                 albums.add(currAlbum);
             }
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return albums;
     }
     
     /**
      * Returns id of the artist by specified name.
      * @param name name of the artist.
      * @return id of the artist by specified name.
      * @throws GetDataException if problems while getting data.
      */ 
     public int findArtist(String name) throws GetDataException {
         int aid = -1;
         try {
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(FIND_ARTIST);
             this.statement.setString(1, name);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 aid = set.getInt(1);
             }
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return aid;
     }
     
     /**
      * Returns id of the label by specified name.
      * @param name name of the label.
      * @return id of the label by specified name.
      * @throws GetDataException if problems while getting data.
      */
     public int findLabel(String name) throws GetDataException {
         int lid = -1;
         try {
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(FIND_LABEL);
             this.statement.setString(1, name);
             
             ResultSet set = this.executeResultQuery();
             while(set.next()){
                 lid = set.getInt(1);
             }
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return lid;
     }
     
     /**
      * Returns maximal id of the artist in storage.
      * @return maximal id of the artist in storage.
      * @throws GetDataException if problems while getting data.
      */
     public int getArtistNumber() throws GetDataException {
          int result = -1;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(ARTIST_MAX_ROW);
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns maximal id of the artist in storage by specified country.
      * @return maximal id of the artist in storage by specified country.
      * @throws GetDataException if problems while getting data.
      */
     public int getArtistNumber(String country) throws GetDataException {
          int result = -1;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(ARTIST_BY_COUNTRY_MAX_ROW);
             
             this.statement.setString(1,country);
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns maximal id of the album in storage.
      * @return maximal id of the album in storage.
      * @throws GetDataException if problems while getting data.
      */
     public int getAlbumNumber() throws GetDataException {
         int result = -1;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(ALBUM_MAX_ROW);
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns maximal id of the album in storage by specified date.
      * @return maximal id of the album in storage by specified date.
      * @throws GetDataException if problems while getting data.
      */
     public int getAlbumNumber(Date date) throws GetDataException {
         int result = -1;
         try {
             DateFormat df = new SimpleDateFormat("yyyy");
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(ALBUM_BY_DATE_MAX_ROW);
             
             this.statement.setString(1,df.format(date));
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns maximal id of the album in storage by specified genre.
      * @return maximal id of the album in storage by specified genre.
      * @throws GetDataException if problems while getting data.
      */
     public int getAlbumNumber(String genre) throws GetDataException {
         int result = -1;
         try {
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(ALBUM_BY_GENRE_MAX_ROW);
             
             this.statement.setString(1,genre);
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns maximal id of the label in storage.
      * @return maximal id of the label in storage.
      * @throws GetDataException if problems while getting data.
      */
     public int getLabelNumber() throws GetDataException {
         int result = -1;
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(LABEL_MAX_ROW);
             
             ResultSet set = this.executeResultQuery();
             set.next();
             
             result = set.getInt(1);
             
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return result;
     }
     
     /**
      * Returns the random album from the storage.
      * @return random album from the storage.
      * @throws GetDataException if problems while getting data.
      */ 
     public Album getRandomAlbum() throws GetDataException {
         Album album = null;
         try {
             album = new Album();
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(
                     ALBUM_RANDOM);
 
             ResultSet set = this.executeResultQuery();
             if(set.next()) {
                 album = (Album)fillBean(set,ALBUM,FULL_MODE);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
         return album;
     }
     
     /**
      * Removes album from the storage.
      * @param id album's id.
      * @throws EditDataException if problems while editing data.
      */ 
     public void deleteAlbum(int id) throws EditDataException {
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(DELETE_ALBUM);
             
             this.statement.setInt(1, id);
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
             
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(exc);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
     }
     
     /**
      * Removes artist from the storage.
      * @param id artist's id.
      * @throws EditDataException if problems while editing data.
      */
     public void deleteArtist(int id) throws EditDataException {
         try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
             
             this.statement = this.connection.prepareStatement(DELETE_ARTIST);
             
             this.statement.setInt(1, id);
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
             
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(exc);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
         
     }
     
     /**
      * Removes label from the storage.
      * @param id label's id.
      * @throws EditDataException if problems while editing data.
      */
     public void deleteLabel(int id) throws EditDataException {
            try {
             
             this.getConnection();
             
             if (this.connection == null){
                 throw new EditDataException("Connection is not created");
             }
            
             this.statement = this.connection.prepareStatement(DELETE_LABEL);
             
             this.statement.setInt(1, id);
             this.connection.setAutoCommit(false);
             this.executeUpdateQuery();
             this.connection.commit();
            
         } catch (ConnectionException e) {
             throw new EditDataException(e);
         } catch (ExecuteQueryException e) {
             throw new EditDataException(e);
         } catch (SQLException e) {
             try {
                 this.connection.rollback();
             } catch (SQLException exc) {
                 throw new EditDataException(exc);
             }
             throw new EditDataException(e);
         } finally {
             try {
                 this.connection.setAutoCommit(true);
                 closeConnection();
             } catch (SQLException e) {
                 throw new EditDataException(e);
             } catch (ConnectionException e) {
                 throw new EditDataException(e);
             }
         }
     }
     
     /**
      * Returns the path to the specified label in the hierarchy of labels.
      * @param id id of the label.
      * @return path to the specified label in the hierarchy of labels.
      * @throws GetDataException if problems while getting data.
      */ 
     public List getLabelPath(int id) throws GetDataException {
         
         List labels = null;
         
         try {
             Label currLabel;
             this.getConnection();
             labels = new LinkedList();
             
             if (this.connection == null){
                 throw new GetDataException("Connection is not created");
             }
 
             this.statement = 
                 this.connection.prepareStatement(
                         LABEL_GET_PATH);
             this.statement.setInt(1, id);
 
             ResultSet set = this.executeResultQuery();
             
             while(set.next()){
                 currLabel = (Label)fillBean(set,LABEL,SHORT_MODE);
                 labels.add(currLabel);
             }
         } catch (ConnectionException e) {
             throw new GetDataException(e);
         } catch (ExecuteQueryException e) {
             throw new GetDataException(e);
         } catch (SQLException e) {
             throw new GetDataException(e);
         } finally {
             try {
                 closeConnection();
             } catch (ConnectionException e) {
                 throw new GetDataException(e);
             }
         }
     return labels;
     }
 }
