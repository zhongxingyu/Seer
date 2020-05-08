 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.mapper;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import org.lafayette.server.ApplicationException;
 import org.lafayette.server.domain.User;
 import org.lafayette.server.domain.UserFinder;
 import org.lafayette.server.mapper.id.IntegerIdentityMap;
 
 /**
  * Maps {@link User user domain objects} to the database.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
public class UserMapper extends BaseMapper<User> implements UserFinder {
 
     /**
      * Name of database table.
      */
     private static final String TABLE_NAME = "user";
     /**
      * Name of primary key field.
      */
     private static final String PK_FIELD_NAME = "id";
     /**
      * Field of database table.
      */
     private static final String COLUMNS = PK_FIELD_NAME + ", loginName, hashedUserData";
     private static final String SQL_INSERT = "insert into %s values (?, ?, ?)";
     private static final String SQL_FIND_BY_LOGIN_NAME = "select %s from %s where loginName like ?";
     private static final String SQL_UPDATE = "update %s set loginName = ?, hashedUserData = ? where %s = ?";
 
     /**
      * Created by {@link Mappers factory}.
      *
      * @param db database connection
      * @param idMap all mappers must share same identity map
      */
     UserMapper(final Connection db, final IntegerIdentityMap<User> idMap) {
         super(db, idMap);
     }
 
     @Override
     protected String findByIdStatement() {
         return findByIdStatement(COLUMNS, TABLE_NAME, PK_FIELD_NAME);
     }
 
     @Override
     protected String findAllStatement(final int limit, final int offset) {
         return findAllStatement(COLUMNS, TABLE_NAME, limit, offset);
     }
 
     @Override
     protected String findMaxPrimaryKeyStatement() {
         return findfindMaxPrimaryKeyStatement(PK_FIELD_NAME, TABLE_NAME);
     }
 
     private String findByLoginNameStatement() {
         return String.format(SQL_FIND_BY_LOGIN_NAME, COLUMNS, TABLE_NAME);
     }
 
     private String updateStatement() {
         return String.format(SQL_UPDATE, TABLE_NAME, PK_FIELD_NAME);
     }
 
     @Override
     protected String deleteStatement() {
         return deleteStatement(TABLE_NAME, PK_FIELD_NAME);
     }
 
     @Override
     protected String insertStatement() {
         return String.format(SQL_INSERT, TABLE_NAME);
     }
 
     @Override
     protected User doLoad(final int id, final ResultSet result) throws SQLException {
         final String loginName = result.getString(2);
         final String hashedUserData = result.getString(3);
         return new User(id, loginName, hashedUserData);
     }
 
     @Override
     protected void doInsert(final User subject, final PreparedStatement insertStatement) throws SQLException {
         insertStatement.setString(2, subject.getLoginName());
         insertStatement.setString(3, subject.getHashedUserData());
     }
 
     @Override
     public User find(final int id) {
         return (User) abstractFind(id);
     }
 
     @Override
     public User find(final Integer id) {
         return find(id.intValue());
     }
 
     @Override
     public User findByLoginName(final String loginName) {
         try {
             final PreparedStatement findStatement = db.prepareStatement(findByLoginNameStatement());
             findStatement.setString(1, loginName);
             final ResultSet rs = findStatement.executeQuery();
 
             if (!rs.next())  {
                 return null;
             }
 
             final User result = load(rs);
             findStatement.close();
             return result;
         } catch (SQLException ex) {
             throw new ApplicationException(ex);
         }
     }
 
     @Override
     public void update(final User subject) {
         try {
             final PreparedStatement updateStatement = db.prepareStatement(updateStatement());
             updateStatement.setString(1, subject.getLoginName());
             updateStatement.setString(2, subject.getHashedUserData());
             updateStatement.setInt(3, subject.getId());
             updateStatement.execute();
             updateStatement.close();
         } catch (SQLException ex) {
             throw new ApplicationException(ex);
         }
     }
 }
