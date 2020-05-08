 package de.hswt.hrm.common.locking.jdbc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.UUID;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 
 final class SessionDao {
     private final static Logger LOG = LoggerFactory.getLogger(SessionDao.class);
     private static SessionDao instance;
     
     private SessionDao() { }
     
     public static SessionDao getInstance() {
         if (instance == null) {
             instance = new SessionDao();
         }
         
         return instance;
     }
     
     public Collection<Session> findAll() throws SQLException, DatabaseException {
         final String query =
                "SELECT Session_Uuid, Session_Name, Session_Timestamp "
                         + "FROM `Session`;";
         
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
                 
                 Collection<Session> sessions = fromResultSet(result);
                 DbUtils.closeQuietly(result);
                 
                 return sessions;
             }
         }
     }
     
     public Session findByUuid(final String uuid) throws DatabaseException, SQLException {
         if (isNullOrEmpty(uuid)) {
             throw new IllegalStateException("UUID must not be empty.");
         }
         
         final String query =
                "SELECT Session_Uuid, Session_Name, Session_Timestamp "
                         + "FROM `Session` "
                         + "WHERE Session_Uuid = :uuid;";
         
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("uuid", uuid);
                 
                 ResultSet result = stmt.executeQuery();
                 
                 Collection<Session> sessions = fromResultSet(result);
                 DbUtils.closeQuietly(result);
                 
                 if (sessions.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (sessions.size() > 1) {
                     throw new DatabaseException("Internal error, session is not unique.");
                 }
                 
                 return sessions.iterator().next();
             }
         }
     }
     
     public Session insert(final String label) throws DatabaseException, SQLException {
         if (isNullOrEmpty(label)) {
             throw new IllegalStateException("Label must not be empty.");
         }
         
         final String query =
                "INSERT INTO `Session` (Session_Uuid, Session_Name) "
                        + "VALUES (:uuid, :name);";
         final String uuid = UUID.randomUUID().toString();
         
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("uuid", uuid);
                stmt.setParameter("name", label);
                 
                 int affectedRows = stmt.executeUpdate();
                 
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
                 
                 return findByUuid(uuid);
             }
         }
     }
     
     public void delete(final String uuid) throws SQLException, DatabaseException {
         if (isNullOrEmpty(uuid)) {
             throw new IllegalStateException("UUID must not be empty.");
         }
         
         final String query =
                 "DELETE FROM `Session` "
                         + "WHERE Session_Uuid = :uuid;";
         
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("uuid", uuid);
                 
                 // Disable autocommit to use a transaction
                 con.setAutoCommit(false);
                 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows == 1) {
                     con.commit();
                 }
                 else {
                     // Damn we couldn't remove the session correctly
                     con.rollback();
                     
                     LOG.error("Tried to delete session, but the DELETE statement "
                             + "affected " + affectedRows + " instead of 1!");
                     throw new DatabaseException("Could not delete session correctly.");
                 }
             }
         }
     }
     
     private Collection<Session> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Session> sessionList = new ArrayList<>();
 
         while (rs.next()) {
             String uuid = rs.getString("Session_Uuid");
             String label = rs.getString("Session_User");
             Timestamp timestamp = rs.getTimestamp("Session_Timestamp");
             
             Session session = new Session(uuid, label, timestamp);
 
             sessionList.add(session);
         }
 
         return sessionList;
     }
 
 }
