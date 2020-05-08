 package de.hswt.hrm.catalog.dao.jdbc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.apache.commons.dbutils.DbUtils;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.catalog.model.Catalog;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.dao.core.ICatalogDao;
 import de.hswt.hrm.catalog.dao.core.ITargetDao;
 
 public class TargetDao implements ITargetDao {
 	private final ICatalogDao catalogDao;
 	
 	// TODO: Add LOG output
 	@Inject
 	public TargetDao(final ICatalogDao catalogDao) {
 		checkNotNull(catalogDao, "CatalogDao not properly injected to TargetDao.");
 		
 		this.catalogDao = catalogDao;
 	}
 
     @Override
     public Collection<Target> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Target> places = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return places;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Target findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Target> targets = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (targets.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (targets.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return targets.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
     
     @Override
     public Collection<Target> findByCatalog(final Catalog catalog) throws DatabaseException {
     	checkNotNull(catalog, "Catalog is mandatory.");
     	checkArgument(catalog.getId() >= 0, "Catalog must have a valid ID.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.TEXT);
         builder.join(CROSS_TABLE_NAME, Fields.ID, Fields.CROSS_TARGET_FK);
         String whereField = CROSS_TABLE_NAME + "." + Fields.CROSS_CATALOG_FK;
         builder.where(whereField);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(whereField, catalog.getId());
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Target> targets = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return targets;
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
     public Target insert(Target target) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.NAME, Fields.TEXT);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, target.getName());
                 stmt.setParameter(Fields.TEXT, target.getText());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Target with id
                         Target inserted = new Target(id, target.getName(), target.getText());
 
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
     public void addToCatalog(Catalog catalog, Target target) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(CROSS_TABLE_NAME, Fields.CROSS_TARGET_FK, Fields.CROSS_CATALOG_FK);
 
         // Insert target and current if not already in the database
         if (catalog.getId() < 0) {
             catalog = catalogDao.insert(catalog);
         }
         
         if (target.getId() < 0) {
             target = insert(target);
         }
         
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.CROSS_TARGET_FK, target.getId());
                 stmt.setParameter(Fields.CROSS_CATALOG_FK, catalog.getId());
 
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
     public void removeFromCatalog(Catalog catalog, Target target) throws DatabaseException {
     	checkNotNull(catalog, "Catalog is mandatory.");
     	checkNotNull(target, "Target is mandatory.");
     	checkArgument(catalog.getId() >= 0, "Catalog must have a valid ID.");
     	checkArgument(target.getId() >= 0, "Target must have a valid ID.");
     	
     	StringBuilder builder = new StringBuilder();
     	builder.append("DELETE FROM ").append(CROSS_TABLE_NAME);
     	builder.append(" WHERE ");
     	builder.append(Fields.CROSS_CATALOG_FK).append(" = ?");
     	builder.append(" AND ");
     	builder.append(Fields.CROSS_TARGET_FK).append(" = ?;");
     	
     	String query = builder.toString();
     	
     	try (Connection con = DatabaseFactory.getConnection()) {
     		con.setAutoCommit(false);
     		
     		try (PreparedStatement stmt = con.prepareStatement(query)) {
     			stmt.setInt(1, catalog.getId());
     			stmt.setInt(2, target.getId());
     			
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
     public void update(Target target) throws ElementNotFoundException, SaveException {
         checkNotNull(target, "Target must not be null.");
 
         if (target.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.NAME, Fields.TEXT);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, target.getId());
                 stmt.setParameter(Fields.NAME, target.getName());
                 stmt.setParameter(Fields.TEXT, target.getText());
 
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
 
     private Collection<Target> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Target> targetList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             String text = rs.getString(Fields.TEXT);
 
             Target target = new Target(id, name, text);
 
             targetList.add(target);
         }
 
         return targetList;
     }
 
     private static final String TABLE_NAME = "State_Target";
     private static final String CROSS_TABLE_NAME = "Catalog_Target";
 
     private static class Fields {
         public static final String ID = "State_Target_ID";
         public static final String NAME = "State_Target_Name";
         public static final String TEXT = "State_Target_Text";
        public static final String CROSS_CATALOG_FK = "Catalog_Target_Catalog_FK";
        public static final String CROSS_TARGET_FK = "Catalog_Target_State_Target_FK";
     }
 }
