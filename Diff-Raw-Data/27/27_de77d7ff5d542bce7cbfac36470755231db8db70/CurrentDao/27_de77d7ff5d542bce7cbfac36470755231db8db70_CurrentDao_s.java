 package de.hswt.hrm.catalog.dao.jdbc;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.dbutils.DbUtils;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.dao.core.ICurrentDao;
 
 public class CurrentDao implements ICurrentDao {
 
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
 
                Collection<Current> activities = fromResultSet(result);
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
 
     /**
      * @see {@link IPlaceDao#insert(Place)}
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
         Collection<Current> placeList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             String text = rs.getString(Fields.TEXT);
 
             Current place = new Current(id, name, text);
 
             placeList.add(place);
         }
 
         return placeList;
     }
 
     private static final String TABLE_NAME = "State_Current";
 
     private static class Fields {
         public static final String ID = "State_Current_ID";
         public static final String NAME = "State_Current_Name";
         public static final String TEXT = "State_Current_Text";
 
     }
 }
