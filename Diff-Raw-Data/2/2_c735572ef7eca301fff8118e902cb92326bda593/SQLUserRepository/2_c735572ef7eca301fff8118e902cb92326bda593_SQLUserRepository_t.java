 package org.triple_brain.module.repository_sql;
 
 
 import org.triple_brain.module.model.User;
 import org.triple_brain.module.repository.user.ExistingUserException;
 import org.triple_brain.module.repository.user.NonExistingUserException;
 import org.triple_brain.module.repository.user.UserRepository;
 
 import java.lang.reflect.Field;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 import static org.triple_brain.module.repository_sql.SQLConnection.preparedStatement;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class SQLUserRepository implements UserRepository {
 
     @Override
     public void save(User user) {
         Long id = getInternalId(user);
         if (id == null) {
             if (emailExists(user.email())) {
                 throw new ExistingUserException(user.email());
             }
             if (usernameExists(user.username())) {
                 throw new ExistingUserException(user.username());
             }
             // If no user found, this is clearly a new user
             String query = "insert into member(uuid, salt, username, email, passwordHash, creationTime, updateTime, locales) values(?, ?, ?, ?, ?, ?, ?, ?);";
             Timestamp now = new Timestamp(System.currentTimeMillis());
             PreparedStatement stm = preparedStatement(query);
             try {
                 stm.setString(1, user.id());
                 stm.setString(2, user.salt());
                 stm.setString(3, user.username());
                 stm.setString(4, user.email());
                 stm.setString(5, user.passwordHash());
                 stm.setTimestamp(6, now);
                 stm.setTimestamp(7, now);
                 stm.setString(8, user.preferredLocales());
                 stm.executeUpdate();
                 ResultSet resultSet = stm.getGeneratedKeys();
                 resultSet.next();
                 long generatedId = resultSet.getLong(1);
                 setUserInternalId(user, generatedId);
             } catch (SQLException ex) {
                 throw new SQLConnectionException(ex);
             }
         } else {
             // If a user is found, and if it comes from DB, we can update all its fields
             String query = "UPDATE member SET salt = ?, passwordHash = ?, updateTime = ?, locales = ? WHERE uuid = ?";
             PreparedStatement stm = preparedStatement(query);
             try {
                 stm.setString(1, user.salt());
                 stm.setString(2, user.passwordHash());
                 stm.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                 stm.setString(4, user.preferredLocales());
                 stm.setString(5, user.id());
                 stm.executeUpdate();
             } catch (SQLException ex) {
                 throw new SQLConnectionException(ex);
             }
         }
     }
 
     @Override
     public User findById(String id) throws NonExistingUserException {
         String query = "SELECT id as internalID, username, email, locales, uuid as id, salt, passwordHash FROM member WHERE uuid = ?";
         try {
             PreparedStatement stm = preparedStatement(query);
             stm.setString(1, id);
             ResultSet rs = stm.executeQuery();
             if (rs.next()) {
                 return userFromResultSet(rs);
             } else {
                 throw new NonExistingUserException(id);
             }
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     @Override
     public User findByUsername(String username) throws NonExistingUserException {
         String query = "SELECT id as internalId, username, email, locales, uuid as id, salt, passwordHash FROM member WHERE username = ?";
         try {
             PreparedStatement stm = preparedStatement(query);
             stm.setString(1, username.trim().toLowerCase());
             ResultSet rs = stm.executeQuery();
             if (!rs.next()) {
                 throw new NonExistingUserException(username);
             }
             return userFromResultSet(rs);
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     @Override
     public User findByEmail(String email) throws NonExistingUserException {
        String query = "SELECT id as internalId, username, email, locales, uuid as id, salt, passwordHash FROM member WHERE email = ?";
         try {
             PreparedStatement stm = preparedStatement(query);
             stm.setString(1, email.trim().toLowerCase());
             ResultSet rs = stm.executeQuery();
             if (!rs.next()) {
                 throw new NonExistingUserException(email);
             }
             return userFromResultSet(rs);
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     @Override
     public Boolean usernameExists(String username) {
         String query = "SELECT COUNT(username) FROM member WHERE username = ?";
         try {
             PreparedStatement stm = preparedStatement(query);
             stm.setString(1, username.trim().toLowerCase());
             ResultSet resultSet = stm.executeQuery();
             resultSet.next();
             return resultSet.getInt(1) >= 1;
 
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     @Override
     public Boolean emailExists(String email) {
         String query = "SELECT COUNT(email) FROM member WHERE email = ?";
         try {
             PreparedStatement stm = preparedStatement(query);
             stm.setString(1, email.trim().toLowerCase());
             ResultSet resultSet = stm.executeQuery();
             resultSet.next();
             return resultSet.getInt(1) >= 1;
 
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     protected User userFromResultSet(ResultSet rs) {
         try {
             User user = User.withUsernameEmailAndLocales(
                     rs.getString("username"),
                     rs.getString("email"),
                     rs.getString("locales")
             );
             setUserInternalId(user, rs.getLong("internalId"));
             setUUId(user, rs.getString("id"));
             setSalt(user, rs.getString("salt"));
             setPasswordHash(user, rs.getString("passwordHash"));
             return user;
         } catch (SQLException ex) {
             throw new SQLConnectionException(ex);
         }
     }
 
     protected void setUserInternalId(User user, long internalId) {
         try {
             Field field = User.class.getDeclaredField("internalId");
             field.setAccessible(true);
             field.set(user, internalId);
             field.setAccessible(false);
         } catch (NoSuchFieldException | IllegalAccessException ex) {
             throw new ReflectionException(ex);
         }
     }
 
     protected void setUUId(User user, String id) {
         try {
             Field field = User.class.getDeclaredField("id");
             field.setAccessible(true);
             field.set(user, id);
             field.setAccessible(false);
         } catch (NoSuchFieldException | IllegalAccessException ex) {
             throw new ReflectionException(ex);
         }
     }
 
     protected void setSalt(User user, String salt) {
         try {
             Field field = User.class.getDeclaredField("salt");
             field.setAccessible(true);
             field.set(user, salt);
             field.setAccessible(false);
         } catch (NoSuchFieldException | IllegalAccessException ex) {
             throw new ReflectionException(ex);
         }
     }
 
     protected void setPasswordHash(User user, String passwordHash) {
         try {
             Field field = User.class.getDeclaredField("passwordHash");
             field.setAccessible(true);
             field.set(user, passwordHash);
             field.setAccessible(false);
         } catch (NoSuchFieldException | IllegalAccessException ex) {
             throw new ReflectionException(ex);
         }
     }
 
 
     protected Long getInternalId(User user) {
         try {
             Field field = User.class.getDeclaredField("internalId");
             field.setAccessible(true);
             return (Long) field.get(user);
         } catch (NoSuchFieldException | IllegalAccessException ex) {
             throw new ReflectionException(ex);
         }
     }
 }
