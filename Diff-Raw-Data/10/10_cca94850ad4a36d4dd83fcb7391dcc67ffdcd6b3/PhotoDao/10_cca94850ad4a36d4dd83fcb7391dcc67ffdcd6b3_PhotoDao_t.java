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
 import java.util.Collections;
 import java.util.List;
 import org.apache.shiro.util.JdbcUtils;
 import org.debox.photo.model.Photo;
 import org.debox.photo.model.ThumbnailSize;
 import org.debox.photo.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class PhotoDao extends JdbcMysqlRealm {
 
     private static final Logger logger = LoggerFactory.getLogger(PhotoDao.class);
     
     protected static String SQL_CREATE_PHOTO = "INSERT INTO photos VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE album_id = ?, relative_path = ?";
     
     protected static String SQL_DELETE_PHOTO = "DELETE FROM photos WHERE id = ?";
     protected static String SQL_GET_ALL = "SELECT id, name, relative_path, album_id FROM photos";
     protected static String SQL_GET_PHOTOS_BY_ALBUM_ID = "SELECT id, name, relative_path, album_id FROM photos WHERE album_id = ?";
     protected static String SQL_GET_VISIBLE_PHOTOS_BY_ALBUM_ID = "SELECT p.id, p.name, p.relative_path, p.album_id FROM photos p INNER JOIN albums a ON p.album_id = a.id LEFT JOIN albums_tokens t ON p.album_id = t.album_id WHERE p.album_id = ? AND (t.token_id = ? OR visibility = 'public')";
     
     protected static String SQL_GET_PHOTO_BY_ID = "SELECT id, name, relative_path, album_id FROM photos WHERE id = ?";
     protected static String SQL_GET_VISIBLE_PHOTO_BY_ID = "SELECT p.id, p.name, p.relative_path, p.album_id FROM photos p INNER JOIN albums a ON p.album_id = a.id LEFT JOIN albums_tokens t ON p.album_id = t.album_id WHERE p.id = ? AND (t.token_id = ? OR visibility = 'public')";
     protected static String SQL_GET_PHOTO_BY_SOURCE_PATH = "SELECT id, name, relative_path, album_id FROM photos WHERE source_path = ?";
     protected static String SQL_GET_RANDOM_PHOTO = "SELECT id FROM photos WHERE album_id = ? ORDER BY RAND( ) LIMIT 1";
 
     protected static String SQL_CREATE_ALBUM_COVER = "INSERT INTO album_covers VALUES (?, ?) ON DUPLICATE KEY UPDATE photo_id = ?";
     protected static String SQL_GET_ALBUM_COVER =
             "SELECT"
             + "    id, name, relative_path, p.album_id "
             + "FROM"
             + "    photos p LEFT JOIN album_covers ac ON id = photo_id "
             + "WHERE"
             + "    ac.album_id = ?";
     protected static String SQL_GET_ALBUM_RANDOM_COVER = SQL_GET_PHOTOS_BY_ALBUM_ID + " LIMIT 1";
    protected static String SQL_GET_VISIBLE_ALBUM_COVER = ""
            + "SELECT p.id, p.name, p.relative_path, p.album_id "
            + "FROM photos p "
            + "LEFT JOIN album_covers ac ON p.id = ac.photo_id "
            + "LEFT JOIN albums a ON a.id = p.album_id "
            + "LEFT JOIN albums_tokens at ON at.album_id = ac.album_id "
            + "WHERE ac.album_id = ? AND (at.token_id = ? OR a.visibility = 'public')";
     
     protected static String SQL_GET_PHOTOS_COUNT = "SELECT count(id) FROM photos";
 
     protected static String SQL_INSERT_PHOTO_GENERATION = "INSERT INTO photos_generation VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE time = ?";
     protected static String SQL_GET_PHOTO_GENERATION = "SELECT time FROM photos_generation WHERE id = ? AND size = ?";
     
     public void savePhotoGenerationTime(Photo photo, ThumbnailSize size, long time) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         try {
             statement = connection.prepareStatement(SQL_INSERT_PHOTO_GENERATION);
                 String id = photo.getId();
                 if (id == null) {
                     id = StringUtils.randomUUID();
                 }
                 statement.setString(1, id);
                 statement.setString(2, size.name());
                 statement.setTimestamp(3, new Timestamp(time));
                 statement.setTimestamp(4, new Timestamp(time));
                 statement.executeUpdate();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
     
     public long getGenerationTime(Photo photo, ThumbnailSize size) throws SQLException {
         long result = -1;
 
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(SQL_GET_PHOTO_GENERATION);
             statement.setString(1, photo.getId());
             statement.setString(2, size.name());
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 result = resultSet.getTimestamp(1).getTime();
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
 
         return result;
     }
     
     public List<Photo> getAll() throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALL);
         List<Photo> result = executeListQueryStatement(statement, null);
         Collections.sort(result);
         return result;
     }
 
     public Photo getPhotoBySourcePath(String sourcePath) throws SQLException {
         Photo result = null;
 
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(SQL_GET_PHOTO_BY_SOURCE_PATH);
             statement.setString(1, sourcePath);
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 result = convertPhoto(resultSet, null);
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
 
         return result;
     }
 
     protected Photo convertPhoto(ResultSet resultSet, String token) throws SQLException {
         Photo result = new Photo();
         result.setId(resultSet.getString(1));
         result.setName(resultSet.getString(2));
         result.setRelativePath(resultSet.getString(3));
         result.setAlbumId(resultSet.getString(4));
         
         String thumbnail = "thumbnail/" + result.getId();
         String url = "photo/" + result.getId();
         
         if (token != null) {
             thumbnail += "?token=" + token;
             url += "?token=" + token;
         }
         
         result.setThumbnailUrl(thumbnail);
         result.setUrl(url);
 
         return result;
     }
 
     public void save(List<Photo> photos) throws SQLException {
         Connection connection = getDataSource().getConnection();
         connection.setAutoCommit(false);
         PreparedStatement statement = null;
         logger.debug("photos : " + photos.size());
         try {
             statement = connection.prepareStatement(SQL_CREATE_PHOTO);
             for (Photo photo : photos) {
                 String id = photo.getId();
                 if (id == null) {
                     id = StringUtils.randomUUID();
                 }
                 statement.setString(1, id);
                 statement.setString(2, photo.getName());
                 statement.setString(3, photo.getRelativePath());
                 statement.setString(4, photo.getAlbumId());
                 statement.setString(5, photo.getAlbumId());
                 statement.setString(6, photo.getRelativePath());
                 statement.addBatch();
             }
             statement.executeBatch();
             connection.commit();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
     
     public void delete(List<Photo> photos) throws SQLException {
         Connection connection = getDataSource().getConnection();
         connection.setAutoCommit(false);
         PreparedStatement statement = null;
         try {
             statement = connection.prepareStatement(SQL_DELETE_PHOTO);
             for (Photo photo : photos) {
                 statement.setString(1, photo.getId());
                 statement.addBatch();
             }
             statement.executeBatch();
             connection.commit();
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
 
 
     public long getPhotosCount() throws SQLException {
         long result = -1;
 
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(SQL_GET_PHOTOS_COUNT);
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 result = resultSet.getLong(1);
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
 
         return result;
     }
 
     public void setAlbumCover(String albumId, String photoId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
 
         try {
             if (StringUtils.isEmpty(photoId)) {
                 statement = connection.prepareStatement(SQL_GET_RANDOM_PHOTO);
                 statement.setString(1, albumId);
                 ResultSet resultSet = statement.executeQuery();
                 if (resultSet.first()) {
                     photoId = resultSet.getString(1);
                 }
                 JdbcUtils.closeResultSet(resultSet);
             }
             statement = connection.prepareStatement(SQL_CREATE_ALBUM_COVER);
             statement.setString(1, albumId);
             statement.setString(2, photoId);
             statement.setString(3, photoId);
             statement.executeUpdate();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
 
     public Photo getAlbumCover(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUM_COVER);
         statement.setString(1, albumId);
         Photo result = executeSingleQueryStatement(statement, null);
         if (result == null) {
             setAlbumCover(albumId, null);
             return getAlbumCover(albumId);
         }
         return result;
     }
     
     public Photo getVisibleAlbumCover(String token, String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
        logger.debug(SQL_GET_VISIBLE_ALBUM_COVER);
         PreparedStatement statement = connection.prepareStatement(SQL_GET_VISIBLE_ALBUM_COVER);
         statement.setString(1, albumId);
         statement.setString(2, token);
         Photo result = executeSingleQueryStatement(statement, token);
         return result;
     }
     
     public List<Photo> getVisiblePhotos(String token, String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_VISIBLE_PHOTOS_BY_ALBUM_ID);
         statement.setString(1, albumId);
         statement.setString(2, token);
         List<Photo> result = executeListQueryStatement(statement, token);
         Collections.sort(result);
         return result;
     }
 
     public List<Photo> getPhotos(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_PHOTOS_BY_ALBUM_ID);
         statement.setString(1, albumId);
         List<Photo> result = executeListQueryStatement(statement, null);
         Collections.sort(result);
         return result;
     }
     
     public List<Photo> getPhotos(List<String> ids) throws SQLException {
         List<Photo> result = new ArrayList<>();
         for (String id : ids) {
             result.addAll(getPhotos(id));
         }
         return result;
     }
 
     public Photo getPhoto(String photoId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_PHOTO_BY_ID);
         statement.setString(1, photoId);
         Photo result = executeSingleQueryStatement(statement, null);
         return result;
     }
     
     public Photo getVisiblePhoto(String token, String photoId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_VISIBLE_PHOTO_BY_ID);
         statement.setString(1, photoId);
         statement.setString(2, token);
         Photo result = executeSingleQueryStatement(statement, token);
         return result;
     }
     
     protected Photo executeSingleQueryStatement(PreparedStatement statement, String token) throws SQLException {
         Photo result = null;
         ResultSet resultSet = null;
         try {
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 result = convertPhoto(resultSet, token);
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeConnection(statement.getConnection());
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     protected List<Photo> executeListQueryStatement(PreparedStatement statement, String token) throws SQLException {
         List<Photo> result = new ArrayList<>();
         ResultSet resultSet = null;
         try {
             resultSet = statement.executeQuery();
             while (resultSet.next()) {
                 Photo photo = convertPhoto(resultSet, token);
                 result.add(photo);
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeConnection(statement.getConnection());
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
 
 }
