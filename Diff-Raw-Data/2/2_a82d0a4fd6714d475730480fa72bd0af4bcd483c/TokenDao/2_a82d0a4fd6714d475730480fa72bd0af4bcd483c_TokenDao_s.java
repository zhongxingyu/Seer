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
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.dbutils.DbUtils;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.shiro.util.JdbcUtils;
 import org.debox.photo.model.Album;
 import org.debox.photo.model.Token;
 import org.debox.photo.model.user.User;
 import org.debox.photo.util.DatabaseUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author Corentin Guy <corentin.guy@debox.fr>
  */
 public class TokenDao {
     
     private static final Logger logger = LoggerFactory.getLogger(TokenDao.class);
 
     protected static String SQL_CREATE = "INSERT INTO tokens VALUES (?, ?, ?)";
     protected static String SQL_UPDATE = "UPDATE tokens SET label = ? WHERE id = ?";
     
     protected static String SQL_CREATE_TOKEN_ALBUM = "INSERT IGNORE INTO albums_tokens VALUES (?, ?)";
     protected static String SQL_DELETE_BY_USER = "DELETE FROM tokens where owner_id = ?";
     protected static String SQL_DELETE_TOKEN_ALBUM = "DELETE FROM albums_tokens WHERE token_id = ?";
     protected static String SQL_DELETE_ALBUM_TOKEN = "DELETE FROM albums_tokens WHERE album_id = ?";
     protected static String SQL_DELETE = "DELETE FROM tokens WHERE id = ?";
     
     protected static String SQL_GET_ALL_WITHOUT_ALBUMS = ""
             + "SELECT * FROM tokens "
             + "ORDER BY label, tokens.id";
     
     protected static String SQL_GET_ALL_WITH_ALBUMS = ""
             + "SELECT t.id id, label, album_id, name, relative_path FROM tokens t "
             + "LEFT JOIN albums_tokens ON id = token_id "
             + "LEFT JOIN albums on album_id = albums.id "
             + "WHERE t.owner_id = ? "
             + "ORDER BY label, t.id";
     
     protected static String SQL_GET_BY_ALBUM_ID = ""
             + "SELECT tokens.id id FROM tokens "
             + "LEFT JOIN albums_tokens ON id = token_id "
             + "WHERE album_id = ?";
     
     protected static String SQL_GET_BY_ID = ""
             + "SELECT tokens.id id, label, tokens.owner_id, album_id FROM tokens "
             + "LEFT JOIN albums_tokens ON id = token_id "
             + "WHERE tokens.id = ? ORDER BY label, tokens.id";
     
     protected AlbumDao albumDao = new AlbumDao();
     protected UserDao userDao = new UserDao();
 
     public void save(Token token) throws SQLException {
         try (Connection connection = DatabaseUtils.getConnection()) {
             connection.setAutoCommit(false);
             QueryRunner queryRunner = new QueryRunner();
             int changedRows = queryRunner.update(connection, SQL_UPDATE, token.getLabel(), token.getId());
             if (changedRows == 0) {
                 queryRunner.update(connection, SQL_CREATE, token.getId(), token.getLabel(), token.getOwner().getId());
             } else {
                 queryRunner.update(connection, SQL_DELETE_TOKEN_ALBUM, token.getId());
             }
             for (Album album : token.getAlbums()) {
                 queryRunner.update(connection, SQL_CREATE_TOKEN_ALBUM, album.getId(), token.getId());
             }
             DbUtils.commitAndCloseQuietly(connection);
         }
     }
     
     public void saveAll(List<Token> tokens) throws SQLException {
         Connection connection = DatabaseUtils.getConnection();
         connection.setAutoCommit(false);
         try {
             QueryRunner queryRunner = new QueryRunner();
             for (Token token : tokens) {
                 queryRunner.update(connection, SQL_DELETE, token.getId());
                queryRunner.update(connection, SQL_CREATE, token.getId(), token.getLabel(), token.getOwner().getId(), token.getLabel());
                 for (Album album : token.getAlbums()) {
                     queryRunner.update(connection, SQL_CREATE_TOKEN_ALBUM, album.getId(), token.getId());
                 }
             }
             DbUtils.commitAndCloseQuietly(connection);
         } catch (SQLException ex) {
             DbUtils.rollbackAndCloseQuietly(connection);
             throw ex;
         }
     }
     
     public Token getById(String id) throws SQLException {
         Token result = null;
         Connection connection = DatabaseUtils.getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(SQL_GET_BY_ID);
             statement.setString(1, id);
             resultSet = statement.executeQuery();
 
             if (resultSet.next()) {
                 String label = resultSet.getString("label");
                 String albumId = resultSet.getString("album_id");
                 String ownerId = resultSet.getString("owner_id");
                 if (result == null || !result.getId().equals(id)) {
                     result = new Token();
                     result.setId(id);
                     result.setLabel(label);
                     result.setOwner(userDao.getUser(ownerId));
                 }
                 if (albumId != null) {
                     Album album = albumDao.getAlbum(albumId);
                     result.getAlbums().add(album);
                 }
             }
 
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return result;
     }
 
     public List<Token> getAll(String ownerId) throws SQLException {
         List<Token> result = new ArrayList<>();
         Connection connection = DatabaseUtils.getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         User owner = userDao.getUser(ownerId);
         try {
             statement = connection.prepareStatement(SQL_GET_ALL_WITH_ALBUMS);
             statement.setString(1, ownerId);
             resultSet = statement.executeQuery();
 
             Token token = null;
             while (resultSet.next()) {
                 String id = resultSet.getString("id");
                 String label = resultSet.getString("label");
                 String albumId = resultSet.getString("album_id");
                 String albumName = resultSet.getString("name");
                 String relativePath = resultSet.getString("relative_path");
                 if (token == null || !token.getId().equals(id)) {
                     token = new Token();
                     token.setId(id);
                     token.setLabel(label);
                     token.setOwner(owner);
                     result.add(token);
                 }
 
                 if (albumId != null) {
                     Album album = new Album();
                     album.setId(albumId);
                     album.setName(albumName);
                     album.setRelativePath(relativePath);
                     token.getAlbums().add(album);
                 }
             }
 
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return result;
     }
     
     public List<Token> getAllTokenWithAccessToAlbum(Album album) throws SQLException {
         if (album == null) {
             return null;
         }
         List<Token> result = new ArrayList<>();
         Connection connection = DatabaseUtils.getConnection();
         PreparedStatement statement = null;
         ResultSet resultSet = null;
         try {
             statement = connection.prepareStatement(SQL_GET_BY_ALBUM_ID);
             statement.setString(1, album.getId());
             resultSet = statement.executeQuery();
             
             List<String> authorizedIds = new ArrayList<>();
             while (resultSet.next()) {
                 String id = resultSet.getString("id");
                 authorizedIds.add(id);
             }
             
             statement = connection.prepareStatement(SQL_GET_ALL_WITHOUT_ALBUMS);
             resultSet = statement.executeQuery();
             
             while (resultSet.next()) {
                 String id = resultSet.getString("id");
                 String label = resultSet.getString("label");
                 String ownerId = resultSet.getString("owner_id");
                 
                 Token token = new Token();
                 token.setId(id);
                 token.setLabel(label);
                 token.setOwner(userDao.getUser(ownerId));
 
                 boolean isTokenAuthorized = authorizedIds.contains(id);
                 if (isTokenAuthorized) {
                     token.getAlbums().add(album);
                 }
                 result.add(token);
             }
 
         } finally {
             JdbcUtils.closeResultSet(resultSet);
             JdbcUtils.closeStatement(statement);
             JdbcUtils.closeConnection(connection);
         }
         return result;
     }
     
     public void delete(String id) throws SQLException {
         QueryRunner queryRunner = new QueryRunner(DatabaseUtils.getDataSource());
         queryRunner.update(SQL_DELETE, id);
     }
     
 }
