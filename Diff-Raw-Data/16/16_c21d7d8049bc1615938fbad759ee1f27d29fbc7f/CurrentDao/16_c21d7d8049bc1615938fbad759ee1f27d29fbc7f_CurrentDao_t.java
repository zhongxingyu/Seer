 package de.hswt.hrm.catalog.dao.jdbc;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 import de.hswt.hrm.catalog.dao.core.ITargetDao;
 
 public class CurrentDao implements ICurrentDao {
     private final static Logger LOG = LoggerFactory.getLogger(CurrentDao.class);
     private final ITargetDao targetDao;
     
     @Inject
     public CurrentDao(final ITargetDao targetDao) {
         checkNotNull(targetDao, "TargetDao not properly injected to CurrentDao.");
         
         this.targetDao = targetDao;
         LOG.debug("TargetDao injected into CurrentDao.");
     }
 
     @Override
     public Collection<Current> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Current> places = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return places;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Current findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Current> currents = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (currents.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (currents.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return currents.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
     
     @Override
     public Collection<Current> findByTarget(final Target target) throws DatabaseException {
         
         checkNotNull(target, "Target is mandatory.");
         checkArgument(target.getId() >= 0, "Target must have a valid ID.");
         
         StringBuilder builder = new StringBuilder();
         builder.append("SELECT");
         builder.append(" ").append(TABLE_NAME).append(".").append(Fields.ID);
         builder.append(", ").append(TABLE_NAME).append(".").append(Fields.NAME);
         builder.append(", ").append(TABLE_NAME).append(".").append(Fields.TEXT);
         builder.append(" FROM ").append(TABLE_NAME);
         builder.append(" JOIN ").append(CROSS_TABLE_NAME);
         builder.append(" ON ").append(CROSS_TABLE_NAME).append(".").append(Fields.CROSS_CURRENT_FK);
         builder.append(" = ").append(TABLE_NAME).append(".").append(Fields.ID);
         builder.append(" WHERE ");
         builder.append(CROSS_TABLE_NAME).append(".").append(Fields.CROSS_TARGET_FK);
         builder.append(" = ?;");
         
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (PreparedStatement stmt = con.prepareStatement(query)) {
                 stmt.setInt(1, target.getId());
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Current> currents = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 
                 return currents;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link ICurrentDao#insert(Current)}
      */
     @Override
     public Current insert(Current current) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, current.getName());
                 stmt.setParameter(Fields.TEXT, current.getText());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Current with id
                         Current inserted = new Current(id, current.getName(), current.getText());
 
                         return inserted;
                     }
                     else {
                         throw new SaveException("Could not retrieve generated ID.");
                     }
                 }
             }
 
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
     
     @Override
     public void addToTarget(Target target, Current current) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(CROSS_TABLE_NAME, Fields.CROSS_TARGET_FK, Fields.CROSS_CURRENT_FK);
 
         // Insert target and current if not already in the database
         if (target.getId() < 0) {
             target = targetDao.insert(target);
         }
         
         if (current.getId() < 0) {
             current = insert(current);
         }
         
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.CROSS_TARGET_FK, target.getId());
                 stmt.setParameter(Fields.CROSS_CURRENT_FK, current.getId());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
             }
 
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
     
     @Override
     public void removeFromTarget(final Target target, final Current current) 
     		throws DatabaseException {
     	
     	checkNotNull(target, "Target is mandatory.");
     	checkNotNull(current, "Current is mandatory.");
     	checkArgument(target.getId() >= 0, "Target must have a valid ID.");
     	checkArgument(current.getId() >= 0, "Current must have a valid ID.");
     	
     	StringBuilder builder = new StringBuilder();
     	builder.append("DELETE FROM ").append(CROSS_TABLE_NAME);
     	builder.append(" WHERE ");
     	builder.append(Fields.CROSS_TARGET_FK).append(" = ?");
     	builder.append(" AND ");
     	builder.append(Fields.CROSS_CURRENT_FK).append(" = ?;");
     	
     	String query = builder.toString();
     	
     	try (Connection con = DatabaseFactory.getConnection()) {
     		con.setAutoCommit(false);
     		
     		try (PreparedStatement stmt = con.prepareStatement(query)) {
     			stmt.setInt(1, target.getId());
     			stmt.setInt(2, current.getId());
     			
     			int affected = stmt.executeUpdate();
     			if (affected < 0) {
     				con.rollback();
     				throw new ElementNotFoundException();
     			}
     			else if (affected > 1) {
     				con.rollback();
     				throw new DatabaseException("Delete would accidently affect multiple rows.");
     			}
     		}
    		
    		con.commit();
     	} catch (SQLException e) {
 			throw new DatabaseException("Unkown error.", e);
 		}
     }
 
     @Override
     public void update(Current current) throws ElementNotFoundException, SaveException {
         checkNotNull(current, "Current must not be null.");
 
         if (current.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, current.getId());
                 stmt.setParameter(Fields.NAME, current.getName());
                 stmt.setParameter(Fields.TEXT, current.getText());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
             }
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
     
     private Collection<Current> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Current> currentList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             String text = rs.getString(Fields.TEXT);
 
             Current current = new Current(id, name, text);
 
             currentList.add(current);
         }
 
         return currentList;
     }
 
     private static final String TABLE_NAME = "State_Current";
     private static final String CROSS_TABLE_NAME = "Catalog_Current";
 
     private static class Fields {
         public static final String ID = "State_Current_ID";
         public static final String NAME = "State_Current_Name";
         public static final String TEXT = "State_Current_Text";
         public static final String CROSS_TARGET_FK = "Category_Current_State_Target_FK";
         public static final String CROSS_CURRENT_FK = "Category_Current_State_Current_FK";
     }
 
 }
