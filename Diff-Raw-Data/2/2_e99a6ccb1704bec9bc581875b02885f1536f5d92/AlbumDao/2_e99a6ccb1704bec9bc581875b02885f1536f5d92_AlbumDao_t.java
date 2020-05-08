 /*
  * #%L
  * debox-photos
  * %%
  * Copyright (C) 2012 Debox
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * #L%
  */
 package org.debox.photo.dao;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.dbutils.BasicRowProcessor;
 import org.apache.commons.dbutils.BeanProcessor;
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.dbutils.handlers.BeanHandler;
 import org.apache.commons.dbutils.handlers.BeanListHandler;
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.util.JdbcUtils;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Media;
 import org.debox.photo.model.user.User;
 import org.debox.photo.util.DatabaseUtils;
 import org.debox.photo.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class AlbumDao {
     
     private static final Logger logger = LoggerFactory.getLogger(AlbumDao.class);
     
     protected static final PhotoDao PHOTO_DAO = new PhotoDao();
     
     protected static String SQL_CREATE_ALBUM = "INSERT INTO albums VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?)";
     protected static String SQL_UPDATE_ALBUM = "UPDATE albums SET name = ?, description = ?, public = ?, photos_count = ?, videos_count = ?, downloadable = ?, begin_date = ?, end_date = ? WHERE id = ?";
     
     protected static String SQL_DELETE_ALBUM = "DELETE FROM albums WHERE id = ?";
 
     protected static String SQL_GET_ALBUMS = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a ORDER BY begin_date";
     
     protected static String SQL_GET_ROOT_ALBUMS_FOR_ADMIN = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a WHERE parent_id is null ORDER BY begin_date";
     
     protected static String SQL_GET_ROOT_VISIBLE_ALBUMS = ""
             + "SELECT DISTINCT"
             + "    id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id "
             + "FROM"
             + "    albums a LEFT JOIN albums_tokens ON id = album_id "
             + "WHERE"
             + "    parent_id is null "
             + "    AND ("
             + "        token_id = ?"
             + "        OR public = 1"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_ROOT_VISIBLE_ALBUMS_FOR_LOGGED = ""
             + "SELECT"
             + "    id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id "
             + "FROM"
             + "    albums a LEFT JOIN accounts_accesses aa ON a.id = aa.album_id "
             + "WHERE aa.user_id = ? AND a.parent_id IS NULL "
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_ALBUMS_BY_PARENT_ID = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a WHERE parent_id = ? ORDER BY begin_date";
     
     protected static String SQL_GET_ALBUMS_BY_PARENT_ID_FOR_ADMINISTRATOR = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a WHERE parent_id = ?  ORDER BY begin_date";
     
     protected static String SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID = ""
             + "SELECT DISTINCT"
             + "    id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id "
             + "FROM"
             + "    albums a LEFT JOIN albums_tokens ON id = album_id "
             + "WHERE"
             + "    parent_id = ? "
             + "    AND ("
             + "        token_id = ?"
             + "        OR public = 1"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID_FOR_LOGGED = ""
             + "SELECT DISTINCT"
             + "    id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id "
             + "FROM"
             + "    albums a LEFT JOIN accounts_accesses aa ON a.id = aa.album_id "
             + "WHERE"
             + "    parent_id = ? AND (aa.user_id = ? "
             + "    OR public = 1) "
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_PHOTOS_COUNT_BY_PARENT_ID_FOR_LOGGED = ""
             + "SELECT count(*) "
             + "FROM"
             + "    albums a LEFT JOIN accounts_accesses aa ON a.id = aa.album_id "
             + "    INNER JOIN photos p ON a.id = p.album_id "
             + "WHERE"
             + "    a.id = ? AND (aa.user_id = ? "
             + "    OR public = 1) "
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_VISIBLE_PHOTOS_COUNT_BY_ALBUM_ID = ""
             + "SELECT count(*) "
             + "FROM"
             + "    albums a LEFT JOIN albums_tokens ON id = album_id "
             + "    INNER JOIN photos p ON a.id = p.album_id "
             + "WHERE"
             + "    a.id = ? "
             + "    AND ("
             + "        token_id = ?"
             + "        OR public = 1"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_VIDEOS_COUNT_BY_PARENT_ID_FOR_LOGGED = ""
             + "SELECT count(*) "
             + "FROM"
             + "    albums a LEFT JOIN accounts_accesses aa ON a.id = aa.album_id "
             + "    INNER JOIN videos p ON a.id = p.album_id "
             + "WHERE"
             + "    a.id = ? AND (aa.user_id = ? "
             + "    OR public = 1) "
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_VISIBLE_VIDEOS_COUNT_BY_ALBUM_ID = ""
             + "SELECT count(*) "
             + "FROM"
             + "    albums a LEFT JOIN albums_tokens ON id = album_id "
             + "    INNER JOIN videos p ON a.id = p.album_id "
             + "WHERE"
             + "    a.id = ? "
             + "    AND ("
             + "        token_id = ?"
             + "        OR public = 1"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_ALBUM_BY_ID = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a WHERE id = ?";
     protected static String SQL_GET_VISIBLE_ALBUM_BY_ID = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a LEFT JOIN albums_tokens ON id = album_id WHERE id = ? AND ("
             + "        token_id = ? OR public = 1"
             + "    )";
     protected static String SQL_GET_VISIBLE_ALBUM_BY_ID_LOGGED = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a LEFT JOIN accounts_accesses aa ON a.id = aa.album_id "
             + "WHERE id = ? AND aa.user_id = ?";
     
     protected static String SQL_GET_ALBUM_BY_RELATIVE_PATH = "SELECT id, name, description, begin_date, end_date, photos_count, videos_count, downloadable, relative_path, parent_id, public, (select count(id) from albums where parent_id = a.id) subAlbumsCount, owner_id FROM albums a WHERE relative_path = ?";
     
     protected static String SQL_GET_CHILDREN_ID = "SELECT id from albums WHERE parent_id = ?";
     
     protected static String SQL_GET_RANDOM_PHOTO = "SELECT id FROM photos WHERE album_id = ? ORDER BY RAND( ) LIMIT 1";
     protected static String SQL_GET_RANDOM_VIDEO = "SELECT id FROM videos WHERE album_id = ? ORDER BY RAND( ) LIMIT 1";
 
     protected static String SQL_GET_RANDOM_SUB_ALBUM = "SELECT id FROM albums WHERE parent_id = ? ORDER BY RAND( ) LIMIT 1";
 
     protected static String SQL_UPDATE_ALBUM_COVER = "UPDATE albums SET cover = ? WHERE id = ?";
     
     protected static String SQL_GET_ALBUM_COVER = "SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id FROM photos p LEFT JOIN albums a ON a.cover = p.id WHERE a.id = ?";
     protected static String SQL_GET_ALBUM_COVER_VIDEO = "SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id FROM videos p LEFT JOIN albums a ON a.cover = p.id WHERE a.id = ?";
     
     protected static String SQL_GET_VISIBLE_ALBUM_COVER = ""
             + "(SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id "
             + "FROM photos p "
             + "LEFT JOIN albums a ON a.cover = p.id "
             + "LEFT JOIN albums_tokens at ON at.album_id = a.id "
             + "WHERE a.id = ? AND (at.token_id = ? OR a.public = 1)) UNION DISTINCT "
             + "(SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id "
             + "FROM photos p "
             + "LEFT JOIN albums a ON a.cover = p.id "
             + "LEFT JOIN accounts_accesses aa ON aa.album_id = a.id "
             + "WHERE a.id = ?)";
     
     protected static String SQL_GET_VISIBLE_ALBUM_COVER_VIDEO = ""
             + "(SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id "
             + "FROM videos p "
             + "LEFT JOIN albums a ON a.cover = p.id "
             + "LEFT JOIN albums_tokens at ON at.album_id = a.id "
             + "WHERE a.id = ? AND (at.token_id = ? OR a.public = 1)) UNION DISTINCT "
             + "(SELECT p.id, p.filename, p.title, p.date, p.relative_path, p.album_id, a.owner_id owner_id "
             + "FROM videos p "
             + "LEFT JOIN albums a ON a.cover = p.id "
             + "LEFT JOIN accounts_accesses aa ON aa.album_id = a.id "
             + "WHERE a.id = ?)";
     
     protected static final Map<String, String> columnsMapping = new HashMap<>(12);
     static {
         columnsMapping.put("id", "id");
         columnsMapping.put("name", "name");
         columnsMapping.put("description", "description");
         columnsMapping.put("begin_date", "beginDate");
         columnsMapping.put("end_date", "endDate");
         columnsMapping.put("photos_count", "photosCount");
         columnsMapping.put("videos_count", "videosCount");
         columnsMapping.put("relative_path", "relativePath");
         columnsMapping.put("parent_id", "parentId");
        columnsMapping.put("public", "public");
         columnsMapping.put("owner_id", "ownerId");
         columnsMapping.put("subAlbumsCount", "subAlbumsCount");
     }
     
     protected int getAllPhotosCount(String albumId) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getAllPhotosCount(albumId, connection);
         }
         return result;
     }
     
     protected int getAllPhotosCount(String albumId, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
         try {
             statement = c.prepareStatement("SELECT photos_count FROM albums WHERE id = ?");
             statement.setString(1, albumId);
 
             rs = statement.executeQuery();
             if (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_ALBUMS_BY_PARENT_ID);
             statement.setString(1, albumId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getAllPhotosCount(rs.getString("id"), c);
             }
             
         } finally {
             DbUtils.closeQuietly(rs);
             DbUtils.closeQuietly(statement);
         }
         return result;
     }
     
     protected int getPhotosCountForLoggedUser(String albumId, String userId) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getPhotosCountForLoggedUser(albumId, userId, connection);
         }
         return result;
     }
     
     protected int getPhotosCountForLoggedUser(String albumId, String userId, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
 
         try {
             statement = c.prepareStatement(SQL_GET_PHOTOS_COUNT_BY_PARENT_ID_FOR_LOGGED);
             statement.setString(1, albumId);
             statement.setString(2, userId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID_FOR_LOGGED);
             statement.setString(1, albumId);
             statement.setString(2, userId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getPhotosCountForLoggedUser(rs.getString("id"), userId, c);
             }
         } finally {
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     protected int getVisiblePhotosCount(String albumId, String token) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getVisiblePhotosCount(albumId, token, connection);
         }
         return result;
     }
     
     protected int getVisiblePhotosCount(String albumId, String token, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
 
         try {
             statement = c.prepareStatement(SQL_GET_VISIBLE_PHOTOS_COUNT_BY_ALBUM_ID);
             statement.setString(1, albumId);
             statement.setString(2, token);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID);
             statement.setString(1, albumId);
             statement.setString(2, token);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getVisiblePhotosCount(rs.getString("id"), token, c);
             }
         } finally {
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     protected int getAllVideosCount(String albumId) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getAllVideosCount(albumId, connection);
         }
         return result;
     }
     
     protected int getAllVideosCount(String albumId, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
         try {
             statement = c.prepareStatement("SELECT videos_count FROM albums WHERE id = ?");
             statement.setString(1, albumId);
 
             rs = statement.executeQuery();
             if (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_ALBUMS_BY_PARENT_ID);
             statement.setString(1, albumId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getAllPhotosCount(rs.getString("id"), c);
             }
             
         } finally {
             DbUtils.closeQuietly(rs);
             DbUtils.closeQuietly(statement);
         }
         return result;
     }
     
     protected int getVideosCountForLoggedUser(String albumId, String userId) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getVideosCountForLoggedUser(albumId, userId, connection);
         }
         return result;
     }
     
     protected int getVideosCountForLoggedUser(String albumId, String userId, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
 
         try {
             statement = c.prepareStatement(SQL_GET_VIDEOS_COUNT_BY_PARENT_ID_FOR_LOGGED);
             statement.setString(1, albumId);
             statement.setString(2, userId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID_FOR_LOGGED);
             statement.setString(1, albumId);
             statement.setString(2, userId);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getVideosCountForLoggedUser(rs.getString("id"), userId, c);
             }
         } finally {
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     protected int getVisibleVideosCount(String albumId, String token) throws SQLException {
         int result;
         try (Connection connection = DatabaseUtils.getConnection()) {
             result = this.getVisibleVideosCount(albumId, token, connection);
         }
         return result;
     }
     
     protected int getVisibleVideosCount(String albumId, String token, Connection c) throws SQLException {
         int result = 0;
         PreparedStatement statement = null;
         ResultSet rs = null;
 
         try {
             statement = c.prepareStatement(SQL_GET_VISIBLE_VIDEOS_COUNT_BY_ALBUM_ID);
             statement.setString(1, albumId);
             statement.setString(2, token);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += rs.getInt(1);
             }
             
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
             
             statement = c.prepareStatement(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID);
             statement.setString(1, albumId);
             statement.setString(2, token);
 
             rs = statement.executeQuery();
             while (rs.next()) {
                 result += getVisibleVideosCount(rs.getString("id"), token, c);
             }
         } finally {
             JdbcUtils.closeResultSet(rs);
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     public void save(List<Album> albums) throws SQLException {
         try (Connection connection = DatabaseUtils.getConnection()) {
             connection.setAutoCommit(false);
             QueryRunner queryRunner = new QueryRunner();
             
             for (Album album : albums) {
                 if (album.getId() == null) {
                     album.setId(StringUtils.randomUUID());
                 }
                 
                 Timestamp beginTimestamp = null;
                 Timestamp endTimestamp = null;
                 if (album.getBeginDate() != null) {
                     beginTimestamp = new Timestamp(album.getBeginDate().getTime());
                 }
                 if (album.getEndDate() != null) {
                     endTimestamp = new Timestamp(album.getEndDate().getTime());
                 }
                 
                 int changedRows = queryRunner.update(connection, SQL_UPDATE_ALBUM,
                         album.getName(),
                         album.getDescription(),
                         album.isPublic(),
                         album.getPhotosCount(),
                         album.getVideosCount(),
                         album.isDownloadable(),
                         beginTimestamp,
                         endTimestamp,
                         album.getId());
                 
                 if (changedRows == 0) {
                     queryRunner.update(connection, SQL_CREATE_ALBUM,
                             album.getId(),
                             album.getName(),
                             album.getDescription(),
                             beginTimestamp,
                             endTimestamp,
                             album.getPhotosCount(),
                             album.getVideosCount(),
                             album.isDownloadable(),
                             album.getRelativePath(),
                             album.getParentId(),
                             album.isPublic(),
                             album.getOwnerId());
                 }
             }
             DbUtils.commitAndCloseQuietly(connection);
         }
     }
     
     public void save(Album album) throws SQLException {
         List<Album> list = new ArrayList<>(1);
         list.add(album);
         save(list);
     }
     
     public void delete(Album album) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         queryRunner.update(SQL_DELETE_ALBUM, album.getId());
     }
     
     public Album getVisibleAlbumForLoggedUser(String userId, String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Album result = queryRunner.query(SQL_GET_VISIBLE_ALBUM_BY_ID_LOGGED, getBeanHandler(userId, false), albumId, userId);
         return result;
     }
     
     public Album getVisibleAlbum(String token, String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Album result = queryRunner.query(SQL_GET_VISIBLE_ALBUM_BY_ID, getBeanHandler(token, true), albumId, token);
         return result;
     }
     
     public Album getAlbum(String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Album result = queryRunner.query(SQL_GET_ALBUM_BY_ID, getBeanHandler(null, false), albumId);
         return result;
     }
     
     public Album getAlbumByPath(String path) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Album result = queryRunner.query(SQL_GET_ALBUM_BY_RELATIVE_PATH, getBeanHandler(null, false), path);
         return result;
     }
    
     public List<Album> getAllAlbums() throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         List<Album> result = queryRunner.query(SQL_GET_ALBUMS, getBeanListHandler(null, false));
         for (Album album : result) {
             this.fillPhotosCount(album, null, false);
         }
         return result;
     }
     
     public List<Album> getAlbums(String parentId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         List<Album> result;
         if (parentId == null) {
             result = queryRunner.query(SQL_GET_ROOT_ALBUMS_FOR_ADMIN, getBeanListHandler(null, false));
         } else {
             result = queryRunner.query(SQL_GET_ALBUMS_BY_PARENT_ID_FOR_ADMINISTRATOR, getBeanListHandler(null, false), parentId);
         }
         for (Album album : result) {
             this.fillPhotosCount(album, null, false);
         }
         return result;
     }
 
     public List<Album> getVisibleAlbums(String token, String parentId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         List<Album> result;
         if (parentId == null) {
             result = queryRunner.query(SQL_GET_ROOT_VISIBLE_ALBUMS, getBeanListHandler(token, true), token);
         } else {
             result = queryRunner.query(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID, getBeanListHandler(token, true), parentId, token);
         }
         for (Album album : result) {
             this.fillPhotosCount(album, token, true);
         }
         return result;
     }
 
     public List<Album> getVisibleAlbumsForLoggedUser(String parentId) throws SQLException {
         String id = ((User) SecurityUtils.getSubject().getPrincipal()).getId();
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         List<Album> result;
         if (parentId == null) {
             result = queryRunner.query(SQL_GET_ROOT_VISIBLE_ALBUMS_FOR_LOGGED, getBeanListHandler(id, false), id);
         } else {
             result = queryRunner.query(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID_FOR_LOGGED, getBeanListHandler(id, false), parentId, id);
         }
         for (Album album : result) {
             this.fillPhotosCount(album, id, false);
         }
         return result;
     }
 
     public String setAlbumCover(String albumId, String mediaId) throws SQLException {
         //if no media id is given, then get a random photo
         if (StringUtils.isEmpty(mediaId)) {
             mediaId = getRandomAlbumPhoto(albumId);
         }
         //if the album has no photo, then get a random video
         if (StringUtils.isEmpty(mediaId)) {
             mediaId = getRandomAlbumVideo(albumId);
         }
         //if the album has neither photo neither video, only subalbums, then get a random subalbum and get its cover
         if (StringUtils.isEmpty(mediaId)) {
             String subAlbumId = getRandomSubAlbumId(albumId);
             Media cover = getAlbumCover(subAlbumId);
             mediaId = cover.getId();
         }
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         queryRunner.update(SQL_UPDATE_ALBUM_COVER, mediaId, albumId);
         return mediaId;
     }
 
     /**
      * Gets a random subalbum of an album
      * @param albumId
      * @return the id of a andom subalbum
      * @throws SQLException
      */
     protected String getRandomSubAlbumId(String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         String result = queryRunner.query(SQL_GET_RANDOM_SUB_ALBUM, new ResultSetHandler<String>() {
             @Override
             public String handle(ResultSet rs) throws SQLException {
                 if (rs.next()) {
                     return rs.getString(1);
                 }
                 return null;
             }
         }, albumId);
         return result;
     }
 
     /**
      * Get a random photo of an album
      * @param albumId
      * @return the id of a random photo
      * @throws SQLException
      */
     protected String getRandomAlbumPhoto(String albumId) throws SQLException {
         return getRandomAlbumMedia(albumId, SQL_GET_RANDOM_PHOTO);
     }
 
     /**
      * Get a random video of an album
      * @param albumId
      * @return the id of a random video
      * @throws SQLException
      */
     protected String getRandomAlbumVideo(String albumId) throws SQLException {
         return getRandomAlbumMedia(albumId, SQL_GET_RANDOM_VIDEO);
     }
 
     /**
      * Get a random media of an album
      * @param albumId
      * @param query The SQL query to execute to get random media
      * @return the id of a random video
      * @throws SQLException
      */
     protected String getRandomAlbumMedia(String albumId, String query) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         String result = queryRunner.query(query, new ResultSetHandler<String>() {
             @Override
             public String handle(ResultSet rs) throws SQLException {
                 if (rs.next()) {
                     return rs.getString(1);
                 }
                 return null;
             }
         }, albumId);
         return result;
     }
 
     public Media getAlbumCover(String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Media result = queryRunner.query(SQL_GET_ALBUM_COVER, PhotoDao.getBeanHandler(null), albumId);
         if (result == null) {
             result = queryRunner.query(SQL_GET_ALBUM_COVER_VIDEO, VideoDao.getBeanHandler(null), albumId);
             if (result == null) {
                 Album album = getAlbum(albumId);
                 if (album.getPhotosCount() == 0 && album.getVideosCount() == 0) {
                     return null;
                 } else {
                     setAlbumCover(albumId, null);
                     return getAlbumCover(albumId);
                 }
             }
         }
         return result;
     }
     
     public Media getVisibleAlbumCover(String token, String albumId) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         Media result = queryRunner.query(SQL_GET_VISIBLE_ALBUM_COVER, PhotoDao.getBeanHandler(token), albumId, token, albumId);
         if (result == null) {
             result = queryRunner.query(SQL_GET_VISIBLE_ALBUM_COVER_VIDEO, VideoDao.getBeanHandler(token), albumId, token, albumId);
         }
         return result;
     }
     
     protected void fillPhotosCount(Album album, String identifier, boolean isToken) throws SQLException {
         int count;
         if (isToken) {
             count = getVisiblePhotosCount(album.getId(), identifier);
         } else if (identifier != null) {
             count = getPhotosCountForLoggedUser(album.getId(), identifier);
         } else {
             count = getAllPhotosCount(album.getId());
         }
         album.setPhotosCount(count);
     }
         
     protected void fillAlbumCoverUrl(Album album, String identifier, boolean isToken) {
         String url = "album/" + album.getId() + "-cover.jpg";
         if (identifier != null && isToken) {
             url += "?token=" + identifier;
         }
         album.setCoverUrl(url);
     }
     
     protected BeanHandler<Album> getBeanHandler(String identifier, boolean isToken) {
         return new BeanHandler<>(Album.class, new BeanRowProcessor(identifier, isToken));
     }
     
     protected BeanListHandler<Album> getBeanListHandler(String identifier, boolean isToken) {
         return new BeanListHandler<>(Album.class, new BeanListRowProcessor(identifier, isToken));
     }
     
     protected class BeanRowProcessor extends BasicRowProcessor {
         
         protected String identifier;
         protected boolean isToken;
         
         public BeanRowProcessor(String identifier, boolean isToken) {
             super(new BeanProcessor(columnsMapping));
             this.identifier = identifier;
             this.isToken = isToken;
         }
         
         @Override
         public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
             T result = super.toBean(rs, type);
             if (result instanceof Album) {
                 fillAlbumCoverUrl((Album) result, identifier, isToken);
                 fillPhotosCount((Album) result, identifier, isToken);
             }
             return result;
         }
         
     }
     
     protected class BeanListRowProcessor extends BeanRowProcessor {
 
         public BeanListRowProcessor(String identifier, boolean isToken) {
             super(identifier, isToken);
         }
         
         @Override
         public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
             List<T> result = super.toBeanList(rs, type);
             for (T current : result) {
                 fillAlbumCoverUrl((Album) current, identifier, isToken);
             }
             return result;
         }
         
     }
     
 }
