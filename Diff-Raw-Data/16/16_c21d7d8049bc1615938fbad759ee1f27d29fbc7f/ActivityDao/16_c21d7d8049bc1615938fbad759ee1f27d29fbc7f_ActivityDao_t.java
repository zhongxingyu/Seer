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
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.dao.core.IActivityDao;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 
 public class ActivityDao implements IActivityDao {
 	private final static Logger LOG = LoggerFactory.getLogger(ActivityDao.class);
 	private final ICurrentDao currentDao; 
 
 	@Inject
 	public ActivityDao(final ICurrentDao currentDao) {
 		checkNotNull(currentDao, "CurrentDao not properly injected to ActivityDao.");
         
         this.currentDao = currentDao;
         LOG.debug("CurrentDao injected into ActivityDao.");
 	}
 	
     @Override
     public Collection<Activity> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Activity> activities = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return activities;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Activity findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Activity> activities = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (activities.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (activities.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return activities.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
     
     @Override
     public Collection<Activity> findByCurrent(final Current current) throws DatabaseException {
         
         checkNotNull(current, "Current is mandatory.");
         checkArgument(current.getId() >= 0, "Current must have a valid ID.");
         
         StringBuilder builder = new StringBuilder();
         builder.append("SELECT");
         builder.append(" ").append(TABLE_NAME).append(".").append(Fields.ID);
         builder.append(", ").append(TABLE_NAME).append(".").append(Fields.NAME);
         builder.append(", ").append(TABLE_NAME).append(".").append(Fields.TEXT);
         builder.append(" FROM ").append(TABLE_NAME);
         builder.append(" JOIN ").append(CROSS_TABLE_NAME);
         builder.append(" ON ").append(CROSS_TABLE_NAME).append(".").append(Fields.CROSS_ACTIVITY_FK);
         builder.append(" = ").append(TABLE_NAME).append(".").append(Fields.ID);
         builder.append(" WHERE ");
         builder.append(CROSS_TABLE_NAME).append(".").append(Fields.CROSS_CURRENT_FK);
         builder.append(" = ?;");
         
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (PreparedStatement stmt = con.prepareStatement(query)) {
                 stmt.setInt(1, current.getId());
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Activity> activities = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 
                 return activities;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link IActivityDao#insert(Activity)}
      */
     @Override
     public Activity insert(Activity activity) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, activity.getName());
                 stmt.setParameter(Fields.TEXT, activity.getText());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Activity with id
                         Activity inserted = new Activity(id, activity.getName(), activity.getText());
 
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
 	public void addToCurrent(Current current, Activity activity)
 			throws SaveException {
 
     	SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(CROSS_TABLE_NAME, Fields.CROSS_CURRENT_FK, Fields.CROSS_ACTIVITY_FK);
 
         // Insert target and current if not already in the database
         if (current.getId() < 0) {
             current = currentDao.insert(current);
         }
         
         if (activity.getId() < 0) {
             activity = insert(activity);
         }
         
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.CROSS_CURRENT_FK, current.getId());
                 stmt.setParameter(Fields.CROSS_ACTIVITY_FK, activity.getId());
 
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
     public void removeFromCurrent(final Current current, final Activity activity) 
     		throws DatabaseException {
     	
     	checkNotNull(current, "Current is mandatory.");
     	checkNotNull(activity, "Activity is mandatory.");
     	checkArgument(current.getId() >= 0, "Current must have a valid ID.");
     	checkArgument(activity.getId() >= 0, "Activity must have a valid ID.");
     	
     	StringBuilder builder = new StringBuilder();
     	builder.append("DELETE FROM ").append(CROSS_TABLE_NAME);
     	builder.append(" WHERE ");
     	builder.append(Fields.CROSS_CURRENT_FK).append(" = ?");
     	builder.append(" AND ");
     	builder.append(Fields.CROSS_ACTIVITY_FK).append(" = ?;");
     	
     	String query = builder.toString();
     	
     	try (Connection con = DatabaseFactory.getConnection()) {
     		con.setAutoCommit(false);
     		
     		try (PreparedStatement stmt = con.prepareStatement(query)) {
     			stmt.setInt(1, current.getId());
     			stmt.setInt(2, activity.getId());
     			
     			
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
     public void update(Activity activity) throws ElementNotFoundException, SaveException {
         checkNotNull(activity, "Activity must not be null.");
 
         if (activity.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, activity.getId());
                 stmt.setParameter(Fields.NAME, activity.getName());
                 stmt.setParameter(Fields.TEXT, activity.getText());
 
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
 
     private Collection<Activity> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Activity> activityList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             String text = rs.getString(Fields.TEXT);
 
             Activity activity = new Activity(id, name, text);
 
             activityList.add(activity);
         }
 
         return activityList;
     }
 
     private static final String TABLE_NAME = "State_Activity";
     private static final String CROSS_TABLE_NAME = "Catalog_Activity";
 
     private static class Fields {
         public static final String ID = "State_Activity_ID";
         public static final String NAME = "State_Activity_Name";
         public static final String TEXT = "State_Activity_Text";
         public static final String CROSS_CURRENT_FK = "Category_Activity_State_Current_FK";
         public static final String CROSS_ACTIVITY_FK = "Category_Activity_State_Activity_FK";
         
     }
 }
