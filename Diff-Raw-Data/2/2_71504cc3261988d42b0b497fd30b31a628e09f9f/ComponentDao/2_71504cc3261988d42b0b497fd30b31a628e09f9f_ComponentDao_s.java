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
 import de.hswt.hrm.component.dao.core.IComponentDao;
 import de.hswt.hrm.component.model.Component;
 
 public class ComponentDao implements IComponentDao {
 
     @Override
     public Collection<Component> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.SYMBOL_LR, Fields.SYMBOL_RL,
                 Fields.SYMBOL_UD, Fields.SYMBOL_DU, Fields.QUANTIFIER, Fields.BOOL_RATING,
                 Fields.CATEGORY);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Component> components = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return components;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Component findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.NAME, Fields.SYMBOL_LR, Fields.SYMBOL_RL,
                 Fields.SYMBOL_UD, Fields.SYMBOL_DU, Fields.QUANTIFIER, Fields.BOOL_RATING,
                 Fields.CATEGORY);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Component> components = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (components.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (components.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return components.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link IComponentDao#insert(Component)}
      */
     @Override
     public Component insert(Component component) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME,Fields.NAME, Fields.SYMBOL_LR, Fields.SYMBOL_RL,
                 Fields.SYMBOL_UD, Fields.SYMBOL_DU, Fields.QUANTIFIER, Fields.BOOL_RATING,
                 Fields.CATEGORY);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, component.getName());
                 stmt.setParameter(Fields.SYMBOL_LR, component.getLeftRightImage());
                 stmt.setParameter(Fields.SYMBOL_RL, component.getRightLeftImage());
                 stmt.setParameter(Fields.SYMBOL_UD, component.getUpDownImage());
                 stmt.setParameter(Fields.SYMBOL_DU, component.getDownUpImage());
                 stmt.setParameter(Fields.QUANTIFIER, component.getQuantifier());
                 stmt.setParameter(Fields.BOOL_RATING, component.getBoolRating());
                 stmt.setParameter(Fields.CATEGORY, component.getCategory().get().getId());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Component with id
                         Component inserted = new Component(id, component.getName(),
                                 component.getLeftRightImage(), component.getRightLeftImage(),
                                 component.getUpDownImage(), component.getDownUpImage(),
                                 component.getQuantifier(), component.getBoolRating());
 
                         inserted.setCategory(component.getCategory().orNull());
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
     public void update(Component component) throws ElementNotFoundException, SaveException {
         checkNotNull(component, "Component must not be null.");
 
         if (component.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.ID, Fields.NAME, Fields.SYMBOL_LR, Fields.SYMBOL_RL,
                 Fields.SYMBOL_UD, Fields.SYMBOL_DU, Fields.QUANTIFIER, Fields.BOOL_RATING,
                 Fields.CATEGORY);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.NAME, component.getName());
                 stmt.setParameter(Fields.SYMBOL_LR, component.getLeftRightImage());
                 stmt.setParameter(Fields.SYMBOL_RL, component.getRightLeftImage());
                 stmt.setParameter(Fields.SYMBOL_UD, component.getUpDownImage());
                 stmt.setParameter(Fields.SYMBOL_DU, component.getDownUpImage());
                 stmt.setParameter(Fields.QUANTIFIER, component.getQuantifier());
                 stmt.setParameter(Fields.BOOL_RATING, component.getBoolRating());
                 stmt.setParameter(Fields.CATEGORY, component.getCategory().get().getId());
 
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
 
     private Collection<Component> fromResultSet(ResultSet rs) throws SQLException, DatabaseException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Component> componentList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             String name = rs.getString(Fields.NAME);
             byte[] leftRightImage = findBlob(rs.getInt(Fields.SYMBOL_LR));
             byte[] rightLeftImage = findBlob(rs.getInt(Fields.SYMBOL_RL));
             byte[] upDownImage = findBlob(rs.getInt(Fields.SYMBOL_UD));
             byte[] downUpImage = findBlob(rs.getInt(Fields.SYMBOL_DU));
             int quantifier = rs.getInt(Fields.QUANTIFIER);
             boolean boolRating = rs.getBoolean(Fields.BOOL_RATING);
 
             Component component = new Component(id, name, leftRightImage, rightLeftImage,
                     upDownImage, downUpImage, quantifier, boolRating);
 
             componentList.add(component);
         }
 
         return componentList;
     }
     
     public byte[] findBlob(int id) throws DatabaseException{
         if(id <= 0){
             return null;
         }
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(BLOB_TABLE_NAME, BlobFields.BLOB);
         builder.where(BlobFields.ID);
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
                 if(!result.next()){
                     throw new ElementNotFoundException();
                 }
                 byte[] bytes = result.getBytes(BlobFields.BLOB);
                 DbUtils.closeQuietly(result);
                 return bytes;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     private static final String TABLE_NAME = "Component";
     private static final String BLOB_TABLE_NAME = "Component_Picture";
 
     private static class Fields {
         public static final String ID = "Component_ID";
         public static final String NAME = "Component_Name";
         public static final String SYMBOL_LR = "Component_Symbol_LR_FK";
         public static final String SYMBOL_RL = "Component_Symbol_RL_FK";
         public static final String SYMBOL_UD = "Component_Symbol_DU_FK";
         public static final String SYMBOL_DU = "Component_Symbol_UD_FK";
         public static final String QUANTIFIER = "Component_Quantifier";
         public static final String BOOL_RATING = "Component_Bool_Rating";
         public static final String CATEGORY = "Component_Category_FK";
     }
     
     private static class BlobFields {
         public static final String ID = "Component_Picture_ID";
         public static final String FILENAME = "Component_Picture_Filename";
         public static final String BLOB = "Component_Picture_Blob";
     }
 }
