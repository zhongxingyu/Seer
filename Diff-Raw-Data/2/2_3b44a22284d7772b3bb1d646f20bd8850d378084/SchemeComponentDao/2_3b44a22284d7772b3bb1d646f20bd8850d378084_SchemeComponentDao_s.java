 package de.hswt.hrm.scheme.dao.jdbc;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.component.dao.core.IComponentDao;
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.scheme.dao.core.ISchemeComponentDao;
 import de.hswt.hrm.scheme.dao.core.ISchemeDao;
 import de.hswt.hrm.scheme.model.Direction;
 import de.hswt.hrm.scheme.model.Scheme;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class SchemeComponentDao implements ISchemeComponentDao {
     private final static Logger LOG = LoggerFactory.getLogger(SchemeComponentDao.class);
     private final ISchemeDao schemeDao;
     private final IComponentDao componentDao;
     
     @Inject
     public SchemeComponentDao(final ISchemeDao schemeDao, final IComponentDao componentDao) {
         checkNotNull(schemeDao, "SchemeDao not injected properly.");
         checkNotNull(componentDao, "ComponentDao not injected properly.");
         
         this.schemeDao = schemeDao;
         LOG.debug("SchemeDao injected into SchemeComponentDao.");
         this.componentDao = componentDao;
         LOG.debug("ComponentDao injected into SchemeComponentDao.");
     }
 
     @Override
     public Collection<SchemeComponent> findAllComponentByScheme(final Scheme scheme) 
             throws DatabaseException {
         
         checkArgument(scheme.getId() >= 0, "Scheme has no valid ID.");
         
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.SCHEME, Fields.COMPONENT, Fields.X_POS,
                 Fields.Y_POS, Fields.DIRECTION);
         builder.where(Fields.SCHEME);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.SCHEME, scheme.getId());
                 
                 ResultSet result = stmt.executeQuery();
 
                 Collection<SchemeComponent> schemeComponents = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return schemeComponents;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Collection<SchemeComponent> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.SCHEME, Fields.COMPONENT, Fields.X_POS,
                 Fields.Y_POS, Fields.DIRECTION);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<SchemeComponent> schemeComponents = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 return schemeComponents;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public SchemeComponent findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.SCHEME, Fields.COMPONENT, Fields.X_POS,
                 Fields.Y_POS, Fields.DIRECTION);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<SchemeComponent> schemeComponents = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (schemeComponents.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (schemeComponents.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return schemeComponents.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public SchemeComponent insert(SchemeComponent schemeComponent) throws SaveException {
         checkNotNull(schemeComponent, "SchemeComponent must not be null.");
     	if (schemeComponent.getId() >= 0) {
     		LOG.info(String.format("SchemeComponent '%d' copied.", schemeComponent.getId()));
         }
         
     	checkState(schemeComponent.getScheme().isPresent(), "SchemeComponent must belong to a scheme.");
     	checkState(schemeComponent.getScheme().get().getId() >= 0, "Scheme must have a valid ID.");
     	checkState(schemeComponent.getComponent().getId() >= 0, "Component must have a valid ID.");
         
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.COMPONENT, Fields.SCHEME, Fields.X_POS, Fields.Y_POS,
                 Fields.DIRECTION);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
 
                 stmt.setParameter(Fields.COMPONENT, schemeComponent.getComponent().getId());
                 stmt.setParameter(Fields.X_POS, schemeComponent.getX());
                 stmt.setParameter(Fields.Y_POS, schemeComponent.getY());
                 stmt.setParameter(Fields.DIRECTION, schemeComponent.getDirection().ordinal());
                 
                 if (schemeComponent.getScheme().isPresent() 
                         && schemeComponent.getScheme().get().getId() >= 0) {
                     
                     stmt.setParameter(Fields.SCHEME, schemeComponent.getScheme().get().getId());
                 }
                 else {
                     stmt.setParameter(Fields.SCHEME, null);
                 }
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         SchemeComponent inserted = new SchemeComponent(id, schemeComponent.getX(),
                                 schemeComponent.getY(), schemeComponent.getDirection(),
                                 schemeComponent.getComponent());
                         inserted.setScheme(schemeComponent.getScheme().orNull());
 
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
     public void update(SchemeComponent schemeComponent) 
             throws ElementNotFoundException, SaveException {
         
         checkNotNull(schemeComponent, "SchemeComponent must not be null.");
         checkState(schemeComponent.getId() >= 0, "SchemeComponent has no valid ID.");
         checkState(schemeComponent.getScheme().isPresent(), "SchemeComponent must belong to a scheme.");
     	checkState(schemeComponent.getScheme().get().getId() >= 0, "Scheme must have a valid ID.");
     	checkState(schemeComponent.getComponent().getId() >= 0, "Component must have a valid ID.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.SCHEME, Fields.COMPONENT, Fields.X_POS,
                 Fields.Y_POS, Fields.DIRECTION);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.COMPONENT, schemeComponent.getComponent());
                 stmt.setParameter(Fields.X_POS, schemeComponent.getX());
                 stmt.setParameter(Fields.Y_POS, schemeComponent.getY());
                stmt.setParameter(Fields.DIRECTION, schemeComponent.getDirection());
                 
                 if (schemeComponent.getScheme().isPresent() 
                         && schemeComponent.getScheme().get().getId() >= 0) {
                     stmt.setParameter(Fields.SCHEME, schemeComponent.getScheme().get().getId());
                 }
                 else {
                     stmt.setParameter(Fields.SCHEME, null);
                 }
 
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
     public void delete(SchemeComponent component) 
     		throws ElementNotFoundException, DatabaseException {
     	
     	checkNotNull(component, "SchemeComponent must not be null.");
         checkState(component.getId() >= 0, "SchemeComponent has no valid ID.");
         
         StringBuilder builder = new StringBuilder();
         builder.append("DELETE FROM ").append(TABLE_NAME);
         builder.append(" WHERE ").append(Fields.ID);
         builder.append(" = :").append(Fields.ID).append(";");
         
         String query = builder.toString();
         
         try (Connection con = DatabaseFactory.getConnection()) {
         	con.setAutoCommit(false);
         	
         	try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
         		stmt.setParameter(Fields.ID, component.getId());
         		
         		int affected = stmt.executeUpdate();
         		
         		if (affected > 1) {
         			con.rollback();
         			throw new DatabaseException("Query would accidently delete more than one row.");
         		}
         		else if (affected < 1) {
         			con.rollback();
         			throw new ElementNotFoundException();
         		}
         		
         		con.commit();
         	}
         }
         catch (SQLException e) {
         	throw new DatabaseException("Unknown error.", e);
         }
     }
     
     @Override
     public Map<Attribute, String> findAttributesOfSchemeComponent(SchemeComponent schemeComponent)
     		throws DatabaseException {
     	
     	checkNotNull(schemeComponent, "SchemeComponent must not be null.");
     	checkArgument(schemeComponent.getId() >= 0, "SchemeComponent must have a valid ID.");
     	
     	SqlQueryBuilder builder = new SqlQueryBuilder();
     	builder.select(ATTR_CROSS_TABLE_NAME, 
     			AttrCrossFields.FK_COMPONENT, 
     			AttrCrossFields.FK_ATTRIBUTE,
     			AttrCrossFields.VALUE);
     	builder.where(AttrCrossFields.FK_COMPONENT);
     	
     	String query = builder.toString();
     	
     	try (Connection con = DatabaseFactory.getConnection()) {
     		try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
     			stmt.setParameter(AttrCrossFields.FK_COMPONENT, schemeComponent.getId());
     			
     			ResultSet rs = stmt.executeQuery();
 
     			Map<Attribute, String> attrValues = new HashMap<>();
     			while (rs.next()) {
     				int attributeId = rs.getInt(AttrCrossFields.FK_ATTRIBUTE);
     				String value = rs.getString(AttrCrossFields.VALUE);
     				
     				Attribute attr = null;
     				try {
     					attr = componentDao.findAttributeById(attributeId);
     				}
     				catch (ElementNotFoundException | IllegalArgumentException e) {
     					throw new DatabaseException("Invalid attribute ID resolved.");
     				}
     				
     				attrValues.put(attr, value);
     			}
     			
     			DbUtils.closeQuietly(rs);
     			return Collections.unmodifiableMap(attrValues);
     		}
     	}
     	catch (SQLException e) {
     		throw new DatabaseException("Unknown error.", e);
     	}
     }
     
     @Override
     public void setAttributeValue(SchemeComponent comp, Attribute attribute, String value)
     		throws DatabaseException {
     	
     	checkArgument(comp.getId() >= 0, "SchemeComponent must have a valid ID.");
     	checkArgument(attribute.getId() >= 0, "Attribute must have a valid ID.");
     	
     	// Check if attribute belongs to the component
     	if (attribute.getComponent().getId() != comp.getComponent().getId()) {
     		throw new IllegalStateException("The given attribute does not belong to the component.");
     	}
     	
     	SqlQueryBuilder builder = new SqlQueryBuilder();
     	builder.insert(ATTR_CROSS_TABLE_NAME, 
     			AttrCrossFields.FK_COMPONENT, 
     			AttrCrossFields.FK_ATTRIBUTE, 
     			AttrCrossFields.VALUE);
     	
     	String query = builder.toString();
     	
     	try (Connection con = DatabaseFactory.getConnection()) {
     		try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
     			stmt.setParameter(AttrCrossFields.FK_COMPONENT, comp.getId());
     			stmt.setParameter(AttrCrossFields.FK_ATTRIBUTE, attribute.getId());
     			stmt.setParameter(AttrCrossFields.VALUE, value);
     			
     			int affected = stmt.executeUpdate();
     			if (affected != 1) {
     				throw new SaveException();
     			}
     		}
     	}
     	catch (SQLException e) {
     		throw new DatabaseException("Unknown error.", e);
     	}
     }
     
     private Collection<SchemeComponent> fromResultSet(ResultSet rs) throws SQLException, ElementNotFoundException, DatabaseException {
         checkNotNull(rs, "Result must not be null.");
         
         Collection<SchemeComponent> schemeComponentList = new ArrayList<>();
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             int xPos = rs.getInt(Fields.X_POS);
             int yPos = rs.getInt(Fields.Y_POS);
             Direction dir = Direction.values()[rs.getInt(Fields.DIRECTION)];
             int componentId = rs.getInt(Fields.COMPONENT);
             Component component = null;
             if (componentId >= 0) {
                 component = componentDao.findById(componentId);
             }
             
             int schemeId = rs.getInt(Fields.SCHEME);
             Scheme scheme = null;
             if (schemeId >= 0) {
                 scheme = schemeDao.findById(schemeId);
             }
             
             SchemeComponent schemeComponent = new SchemeComponent(id, xPos, yPos, dir, component);
             schemeComponent.setComponent(component);
             schemeComponent.setScheme(scheme);
 
             schemeComponentList.add(schemeComponent);
         }
 
         return schemeComponentList;
     }
 
     private static final String TABLE_NAME = "Scheme_Component";
     private static final String ATTR_CROSS_TABLE_NAME = "Scheme_Component_Attribute";
 
     private static class Fields {
         public static final String ID = "Scheme_Component_ID";
         public static final String SCHEME = "Scheme_Component_Scheme_FK";
         public static final String COMPONENT = "Scheme_Component_Component_FK";
         public static final String X_POS = "Scheme_Component_X_Position";
         public static final String Y_POS = "Scheme_Component_Y_Position";
         public static final String DIRECTION = "Scheme_Component_Direction";
 
     }
     
     private static class AttrCrossFields {
     	public static final String FK_COMPONENT = "Scheme_Component_Attribute_Component_FK";
     	public static final String FK_ATTRIBUTE = "Scheme_Component_Attribute_Attribute_FK";
     	public static final String VALUE = "Scheme_Component_Attribute_Value";
     }
 
 }
