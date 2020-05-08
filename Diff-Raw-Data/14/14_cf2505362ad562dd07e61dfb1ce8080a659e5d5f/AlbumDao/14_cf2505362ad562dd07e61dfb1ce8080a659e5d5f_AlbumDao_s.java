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
 import java.util.List;
 import org.apache.shiro.util.JdbcUtils;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Photo;
 import org.debox.photo.util.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class AlbumDao extends JdbcMysqlRealm {
     
     private static final Logger logger = LoggerFactory.getLogger(AlbumDao.class);
     
     protected static final PhotoDao PHOTO_DAO = new PhotoDao();
     
     protected static String SQL_CREATE_ALBUM = "INSERT INTO albums VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NULL) ON DUPLICATE KEY UPDATE name = ?, visibility = ?, photos_count = ?, downloadable = ?";
     
     protected static String SQL_DELETE_ALBUM = "DELETE FROM albums WHERE id = ?";
     protected static String SQL_GET_ALBUMS = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums ORDER BY begin_date";
     
     protected static String SQL_GET_ROOT_ALBUMS = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums WHERE parent_id is null ORDER BY begin_date";
     protected static String SQL_GET_ROOT_VISIBLE_ALBUMS = ""
             + "SELECT DISTINCT"
             + "    id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility "
             + "FROM"
             + "    albums LEFT JOIN albums_tokens ON id = album_id "
             + "WHERE"
             + "    parent_id is null "
             + "    AND ("
             + "        token_id = ?"
             + "        OR visibility = 'public'"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_ALBUMS_BY_PARENT_ID = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums WHERE parent_id = ?  ORDER BY begin_date";
     protected static String SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID = ""
             + "SELECT DISTINCT"
             + "    id, name, begin_date, end_date,  photos_count, downloadable, relative_path, parent_id, visibility "
             + "FROM"
             + "    albums LEFT JOIN albums_tokens ON id = album_id "
             + "WHERE"
             + "    parent_id = ? "
             + "    AND ("
             + "        token_id = ?"
             + "        OR visibility = 'public'"
             + "    )"
             + "ORDER BY begin_date";
     
     protected static String SQL_GET_ALBUM_BY_ID = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums WHERE id = ?";
     protected static String SQL_GET_VISIBLE_ALBUM_BY_ID = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums LEFT JOIN albums_tokens ON id = album_id WHERE id = ? AND ("
             + "        token_id = ?"
             + "        OR visibility = 'public'"
             + "    )";
     
     protected static String SQL_GET_CHILDREN_ID = "SELECT id from albums WHERE parent_id = ?";
     
     protected static String SQL_GET_ALBUM_BY_SOURCE_PATH = "SELECT id, name, begin_date, end_date, photos_count, downloadable, relative_path, parent_id, visibility FROM albums WHERE source_path = ?";
 
     protected static String SQL_GET_RANDOM_PHOTO = "SELECT id FROM photos WHERE album_id = ? ORDER BY RAND( ) LIMIT 1";
 
     protected static String SQL_GET_RANDOM_SUB_ALBUM = "SELECT id FROM albums WHERE parent_id = ? ORDER BY RAND( ) LIMIT 1";
 
     protected static String SQL_UPDATE_ALBUM_COVER = "UPDATE albums SET cover = ? WHERE id = ?";
     
     protected static String SQL_GET_ALBUM_COVER = "SELECT p.id, p.name, p.date, p.relative_path, p.album_id FROM photos p LEFT JOIN albums a ON a.cover = p.id WHERE a.id = ?";
     
     protected static String SQL_GET_VISIBLE_ALBUM_COVER = ""
             + "SELECT p.id, p.name, p.date, p.relative_path, p.album_id "
             + "FROM photos p "
             + "LEFT JOIN albums a ON a.cover = p.id "
             + "LEFT JOIN albums_tokens at ON at.album_id = a.id "
             + "WHERE a.id = ? AND (at.token_id = ? OR a.visibility = 'public')";
     
     public void save(List<Album> albums) throws SQLException {
         Connection connection = getDataSource().getConnection();
         connection.setAutoCommit(false);
         PreparedStatement statement = null;
         
         try {
             statement = connection.prepareStatement(SQL_CREATE_ALBUM);
             for (Album album : albums) {
                 statement.setString(1, album.getId());
                 statement.setString(2, album.getName());
                 statement.setTimestamp(3, new Timestamp(album.getBeginDate().getTime()));
                 statement.setTimestamp(4, new Timestamp(album.getEndDate().getTime()));
                 statement.setInt(5, album.getPhotosCount());
                 statement.setBoolean(6, album.isDownloadable());
                 statement.setString(7, album.getRelativePath());
                 statement.setString(8, album.getParentId());
                 statement.setString(9, album.getVisibility().name().toLowerCase());
                 statement.setString(10, album.getName());
                 statement.setString(11, album.getVisibility().name().toLowerCase());
                 statement.setInt(12, album.getPhotosCount());
                 statement.setBoolean(13, album.isDownloadable());
                 statement.addBatch();
             }
 
             statement.executeBatch();
             connection.commit();
         } finally {
             if (connection != null) {
                 connection.rollback();
             }
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
     
     public void save(Album album) throws SQLException {
         Connection connection = getDataSource().getConnection();
         String id = album.getId();
         if (id == null) {
             id = StringUtils.randomUUID();
         }
 
         PreparedStatement statement = null;
         try {
             statement = connection.prepareStatement(SQL_CREATE_ALBUM);
             statement.setString(1, id);
             statement.setString(2, album.getName());
             statement.setTimestamp(3, new Timestamp(album.getBeginDate().getTime()));
             statement.setTimestamp(4, new Timestamp(album.getEndDate().getTime()));
             statement.setInt(5, album.getPhotosCount());
             statement.setBoolean(6, album.isDownloadable());
             statement.setString(7, album.getRelativePath());
             statement.setString(8, album.getParentId());
             statement.setString(9, album.getVisibility().name().toLowerCase());
             statement.setString(10, album.getName());
             statement.setString(11, album.getVisibility().name().toLowerCase());
             statement.setInt(12, album.getPhotosCount());
             statement.setBoolean(13, album.isDownloadable());
             statement.executeUpdate();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
     
     public void delete(Album album) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         try {
             statement = connection.prepareStatement(SQL_DELETE_ALBUM);
             statement.setString(1, album.getId());
             statement.executeUpdate();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
     }
     
     public Album getVisibleAlbum(String token, String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_VISIBLE_ALBUM_BY_ID);
         statement.setString(1, albumId);
         statement.setString(2, token);
         Album result = executeSingleQueryStatement(statement, token, false);
         return result;
     }
     
     public Album getAlbum(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUM_BY_ID);
         statement.setString(1, albumId);
         Album result = executeSingleQueryStatement(statement, null, true);
         return result;
     }
     
     protected Album getAlbum(String albumId, boolean partial) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUM_BY_ID);
         statement.setString(1, albumId);
         Album result = executeSingleQueryStatement(statement, null, true, partial);
         return result;
     }
     
     public List<Album> getAlbums() throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUMS);
         List<Album> result = executeListQueryStatement(statement, null, true);
         return result;
     }
 
     public List<String> getSubAlbumsId(String parentId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_CHILDREN_ID);
         statement.setString(1, parentId);
         
         List<String> result = new ArrayList<>();
         ResultSet resultSet = null;
         try {
             resultSet = statement.executeQuery();
             while (resultSet.next()) {
                 String id = resultSet.getString(1);
                 result.add(id);
                 result.addAll(getSubAlbumsId(id));
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeConnection(statement.getConnection());
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     public List<Album> getVisibleAlbums(String token, String parentId, boolean grantedAccess) throws SQLException {
         return this.getVisibleAlbums(token, parentId, grantedAccess, false);
     }
     
     protected List<Album> getVisibleAlbums(String token, String parentId, boolean grantedAccess, boolean partialLoading) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement;
         if (parentId == null && grantedAccess) {
             statement = connection.prepareStatement(SQL_GET_ROOT_ALBUMS);
 
         } else if (parentId == null) {
             statement = connection.prepareStatement(SQL_GET_ROOT_VISIBLE_ALBUMS);
             statement.setString(1, token);
 
         } else if (grantedAccess) {
             statement = connection.prepareStatement(SQL_GET_ALBUMS_BY_PARENT_ID);
             statement.setString(1, parentId);
 
         } else {
             statement = connection.prepareStatement(SQL_GET_VISIBLE_ALBUMS_BY_PARENT_ID);
             statement.setString(1, parentId);
             statement.setString(2, token);
         }
        List<Album> result = this.executeListQueryStatement(statement, token, grantedAccess, partialLoading);
         return result;
     }
 
     public Album getAlbumBySourcePath(String sourcePath) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUM_BY_SOURCE_PATH);
         statement.setString(1, sourcePath);
         Album result = executeSingleQueryStatement(statement, null, true);
         return result;
     }
 
     public String setAlbumCover(String albumId, String photoId) throws SQLException {
         //if no photo id is given, then get a random photo
         if (StringUtils.isEmpty(photoId)) {
             photoId = getRandomAlbumPhoto(albumId);
         }
         //if the album has no photo, only subalbums, then get a random subalbum and get its cover
         if (StringUtils.isEmpty(photoId)) {
             String subAlbumId = getRandomSubAlbumId(albumId);
             Photo cover = getAlbumCover(subAlbumId);
             photoId = cover.getId();
         }
         
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         try {
             statement = connection.prepareStatement(SQL_UPDATE_ALBUM_COVER);
             statement.setString(1, photoId);
             statement.setString(2, albumId);
             statement.executeUpdate();
 
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return photoId;
     }
 
     /**
      * Gets a random subalbum of an album
      * @param albumId
      * @return the id of a andom subalbum
      * @throws SQLException
      */
     protected String getRandomSubAlbumId(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         String subAlbumId = null;
         try {
             statement = connection.prepareStatement(SQL_GET_RANDOM_SUB_ALBUM);
             statement.setString(1, albumId);
             ResultSet resultSet = statement.executeQuery();
             if (resultSet.first()) {
                 subAlbumId = resultSet.getString(1);
             }
             JdbcUtils.closeResultSet(resultSet);
             
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return subAlbumId;
     }
 
     /**
      * Get a random photo of an album
      * @param albumId
      * @return the id of a random photo
      * @throws SQLException
      */
     protected String getRandomAlbumPhoto(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = null;
         String photoId = null;
         try {
             statement = connection.prepareStatement(SQL_GET_RANDOM_PHOTO);
             statement.setString(1, albumId);
             ResultSet resultSet = statement.executeQuery();
             if (resultSet.first()) {
                 photoId = resultSet.getString(1);
             }
             JdbcUtils.closeResultSet(resultSet);
         } finally {
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return photoId;
     }
 
     public Photo getAlbumCover(String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_ALBUM_COVER);
         statement.setString(1, albumId);
         Photo result = PhotoDao.executeSingleQueryStatement(statement, null);
         if (result == null) {
             setAlbumCover(albumId, null);
             return getAlbumCover(albumId);
         }
         return result;
     }
     
     public Photo getVisibleAlbumCover(String token, String albumId) throws SQLException {
         Connection connection = getDataSource().getConnection();
         PreparedStatement statement = connection.prepareStatement(SQL_GET_VISIBLE_ALBUM_COVER);
         statement.setString(1, albumId);
         statement.setString(2, token);
         Photo result = PhotoDao.executeSingleQueryStatement(statement, token);
         return result;
     }
     
     protected Album convertAlbum(ResultSet resultSet, String token) throws SQLException {
         Album result = new Album();
         result.setId(resultSet.getString(1));
         result.setName(resultSet.getString(2));
         result.setBeginDate(resultSet.getTimestamp(3));
         result.setEndDate(resultSet.getTimestamp(4));
         result.setPhotosCount(resultSet.getInt(5));
         result.setDownloadable(resultSet.getBoolean(6));
         result.setRelativePath(resultSet.getString(7));
         result.setParent(this.getAlbum(resultSet.getString(8), true));
         result.setVisibility(Album.Visibility.valueOf(resultSet.getString(9).toUpperCase()));
         
         List<Photo> photos = PHOTO_DAO.getPhotos(result.getId(), token);
         result.setPhotos(photos);
 
         String url = "album/" + result.getId() + "/cover";
         if (token != null) {
             url += "?token=" + token;
         }
         result.setCoverUrl(url);
 
         return result;
     }
 
     protected List<Album> executeListQueryStatement(PreparedStatement statement, String token, boolean grantedAccess) throws SQLException {
        return executeListQueryStatement(statement, token, grantedAccess, false);
    }
    
    protected List<Album> executeListQueryStatement(PreparedStatement statement, String token, boolean grantedAccess, boolean partialLoading) throws SQLException {
         List<Album> result = new ArrayList<>();
         ResultSet resultSet = null;
         try {
             resultSet = statement.executeQuery();
             while (resultSet.next()) {
                 Album album = convertAlbum(resultSet, token);
                if (!partialLoading) {
                    List<Album> subAlbums = getVisibleAlbums(token, album.getId(), grantedAccess, true);
                    album.setSubAlbums(subAlbums);
                }
                 result.add(album);
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeConnection(statement.getConnection());
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
 
     protected Album executeSingleQueryStatement(PreparedStatement statement, String token, boolean grantedAccess, boolean partialLoading) throws SQLException {
         Album result = null;
         ResultSet resultSet = null;
         try {
             resultSet = statement.executeQuery();
             if (resultSet.next()) {
                 result = convertAlbum(resultSet, token);
                 if (!partialLoading) {
                     List<Album> subAlbums = getVisibleAlbums(token, result.getId(), grantedAccess, true);
                     result.setSubAlbums(subAlbums);
                 }
             }
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeConnection(statement.getConnection());
             JdbcUtils.closeStatement(statement);
         }
         return result;
     }
     
     protected Album executeSingleQueryStatement(PreparedStatement statement, String token, boolean grantedAccess) throws SQLException {
         return executeSingleQueryStatement(statement, token, grantedAccess, false);
     }
 
 }
