 package de.hswt.hrm.component.dao.jdbc;
 
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
 import de.hswt.hrm.component.dao.core.ICategoryDao;
 import de.hswt.hrm.component.model.Category;
 
 public class CategoryDao implements ICategoryDao {
 
     @Override
     public Collection<Category> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.HEIGHT, Fields.WIDTH,
                 Fields.DEFAULT_QUANTIFIER, Fields.DEFAULT_BOOL_RATING, Fields.CATALOG);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Category> categorys = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return categorys;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Category findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.HEIGHT, Fields.WIDTH,
                 Fields.DEFAULT_QUANTIFIER, Fields.DEFAULT_BOOL_RATING, Fields.CATALOG);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Category> categorys = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (categorys.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (categorys.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return categorys.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link ICategoryDao#insert(Category)}
      */
     @Override
     public Category insert(Category category) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.ID, Fields.NAME, Fields.HEIGHT, Fields.WIDTH,
                 Fields.DEFAULT_QUANTIFIER, Fields.DEFAULT_BOOL_RATING, Fields.CATALOG);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, category.getName());
                 stmt.setParameter(Fields.HEIGHT, category.getHeight());
                 stmt.setParameter(Fields.WIDTH, category.getWidth());
                 stmt.setParameter(Fields.DEFAULT_QUANTIFIER, category.getDefaultQuantifier());
                 stmt.setParameter(Fields.DEFAULT_BOOL_RATING, category.getDefaultBoolRating());
                 // TODO finish category.getCatalog() first
                 // stmt.setParameter(Fields.CATALOG, category.getCatalog().get().getId());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Category with id
                         Category inserted = new Category(id, category.getName(),
                                 category.getHeight(), category.getWidth(),
                                 category.getDefaultQuantifier(), category.getDefaultBoolRating());
                         // TODO finish category.setCatalog() first
                         // inserted.setCatalog(catagory.getCatalog().orNull());
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
     public void update(Category category) throws ElementNotFoundException, SaveException {
         checkNotNull(category, "Category must not be null.");
 
         if (category.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.ID, Fields.NAME, Fields.HEIGHT, Fields.WIDTH,
                 Fields.DEFAULT_QUANTIFIER, Fields.DEFAULT_BOOL_RATING, Fields.CATALOG);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, category.getId());
                 stmt.setParameter(Fields.NAME, category.getName());
                 stmt.setParameter(Fields.HEIGHT, category.getHeight());
                 stmt.setParameter(Fields.WIDTH, category.getWidth());
                 stmt.setParameter(Fields.DEFAULT_QUANTIFIER, category.getDefaultQuantifier());
                 stmt.setParameter(Fields.DEFAULT_BOOL_RATING, category.getDefaultBoolRating());
                 // TODO finish category.getCatalog() first
                 // stmt.setParameter(Fields.CATALOG, category.getCatalog().get().getId());
 
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
 
     private Collection<Category> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Category> categoryList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             int width = rs.getInt(Fields.WIDTH);
             int heigth = rs.getInt(Fields.HEIGHT);
             int defaultQuantifier = rs.getInt(Fields.DEFAULT_QUANTIFIER);
             boolean defaultBoolRating = rs.getBoolean(Fields.DEFAULT_BOOL_RATING);
 
             Category category = new Category(id, name, width, heigth, defaultQuantifier,
                     defaultBoolRating);
 
             categoryList.add(category);
         }
 
         return categoryList;
     }
 
     private static final String TABLE_NAME = "Category";
 
     private static class Fields {
         public static final String ID = "Category_ID";
         public static final String NAME = "Category_Name";
         public static final String HEIGHT = "Category_Height";
         public static final String WIDTH = "Category_Width";
         public static final String DEFAULT_QUANTIFIER = "Category_Default_Quantifier";
        public static final String DEFAULT_BOOL_RATING = "Category_Default_Bool_Rating";
         public static final String CATALOG = "Category_Catalog_FK";
     }
 }
