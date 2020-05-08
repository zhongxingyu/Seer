 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.ejb.beans.structure;
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.structure.StructureLoader;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.cache.FxCacheException;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.*;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Structure Assignment management
  * <p/>
  * TODO's:
  * -property/group removal
  * -check if modification/creation even possible in case instances exist
  * -implement all known changeable flags
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @Stateless(name = "AssignmentEngine")
 @TransactionAttribute(TransactionAttributeType.SUPPORTS)
 @TransactionManagement(TransactionManagementType.CONTAINER)
 public class AssignmentEngineBean implements AssignmentEngine, AssignmentEngineLocal {
 
     private static transient Log LOG = LogFactory.getLog(AssignmentEngineBean.class);
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @EJB
     SequencerEngineLocal seq;
 
     @EJB
     HistoryTrackerEngineLocal htracker;
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(FxPropertyEdit property, String parentXPath) throws FxApplicationException {
         return createProperty(FxType.ROOT_ID, property, parentXPath);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(long typeId, FxPropertyEdit property, String parentXPath) throws FxApplicationException {
         return createProperty(typeId, property, parentXPath, property.getName());
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createProperty(long typeId, FxPropertyEdit property, String parentXPath, String assignmentAlias) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(2000);
         long newPropertyId;
         long newAssignmentId;
         try {
             parentXPath = parentXPath.toUpperCase();
             assignmentAlias = assignmentAlias.toUpperCase();
             FxType type = CacheAdmin.getEnvironment().getType(typeId);
             FxAssignment tmp = type.getAssignment(parentXPath);
             if (tmp != null && tmp instanceof FxPropertyAssignment)
                 throw new FxInvalidParameterException("ex.structure.assignment.noGroup", parentXPath);
             property.checkConsistency();
             //parentXPath is valid, create the property, then assign it to root
             newPropertyId = seq.getId(SequencerEngine.System.TYPEPROP);
             con = Database.getDbConnection();
             //create property, no checks for existing names are performed as this is handled with unique keys
             sql.append("INSERT INTO ").append(TBL_STRUCT_PROPERTIES).
                     //               1  2    3          4          5               6        7
                             append("(ID,NAME,DEFMINMULT,DEFMAXMULT,MAYOVERRIDEMULT,DATATYPE,REFTYPE," +
                             //8                9   10             11      12
                             "ISFULLTEXTINDEXED,ACL,MAYOVERRIDEACL,REFLIST,UNIQUEMODE," +
                             "SYSINTERNAL)VALUES(" +
                             "?,?,?,?,?," +
                             "?,?,?,?,?,?,?,FALSE)");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, newPropertyId);
             ps.setString(2, property.getName());
             ps.setInt(3, property.getMultiplicity().getMin());
             ps.setInt(4, property.getMultiplicity().getMax());
             ps.setBoolean(5, property.mayOverrideBaseMultiplicity());
             ps.setLong(6, property.getDataType().getId());
             if (property.hasReferencedType())
                 ps.setLong(7, property.getReferencedType().getId());
             else
                 ps.setNull(7, java.sql.Types.NUMERIC);
             ps.setBoolean(8, property.isFulltextIndexed());
             ps.setLong(9, property.getACL().getId());
             ps.setBoolean(10, property.mayOverrideACL());
             if (property.hasReferencedList())
                 ps.setLong(11, property.getReferencedList().getId());
             else
                 ps.setNull(11, java.sql.Types.NUMERIC);
             ps.setInt(12, property.getUniqueMode().getId());
             if (!property.isAutoUniquePropertyName())
                 ps.executeUpdate();
             else {
                 int counter = 0;
                 boolean isok = false;
                 while (!isok && counter < 200) { //200 tries max
                     try {
                         if (counter > 0) {
                             ps.setString(2, property.getName() + "_" + counter);
                         }
                         ps.executeUpdate();
                         isok = true;
                     } catch (SQLException e) {
                         if (!Database.isUniqueConstraintViolation(e) || counter >= 200)
                             throw e;
                     }
                     counter++;
                 }
             }
             Database.storeFxString(new FxString[]{property.getLabel(), property.getHint(), property.getDefaultValue()},
                     con, TBL_STRUCT_PROPERTIES, new String[]{"DESCRIPTION", "HINT", "DEFAULT_VALUE"}, "ID", newPropertyId);
             ps.close();
             sql.setLength(0);
             //calc new position
             sql.append("SELECT COALESCE(MAX(POS)+1,0) FROM ").append(TBL_STRUCT_ASSIGNMENTS).
                     append(" WHERE PARENTGROUP=? AND TYPEDEF=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(2, typeId);
             ResultSet rs = ps.executeQuery();
             long pos = 0;
             if (rs != null && rs.next()) {
                 if (rs.wasNull())
                     pos = 0; //actually impossible to happen
                 else
                     pos = rs.getLong(1);
             }
             ps.close();
             storeOptions(con, TBL_PROPERTY_OPTIONS, "ID", newPropertyId, null, property.getOptions());
             sql.setLength(0);
             //create root assignment
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10    11    12          13
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,APROPERTY," +
                             //14
                             "ACL)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(SequencerEngine.System.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_PROPERTY);
             ps.setBoolean(3, true);
             ps.setLong(4, typeId);
             ps.setInt(5, property.getMultiplicity().getMin());
             ps.setInt(6, property.getMultiplicity().getMax());
             if (property.getMultiplicity().isValid(property.getAssignmentDefaultMultiplicity())) {
                 ps.setInt(7, property.getAssignmentDefaultMultiplicity());
             } else {
                 //default is min(min,1).
                 ps.setInt(7, property.getMultiplicity().getMin() > 1 ? property.getMultiplicity().getMin() : 1);
             }
             ps.setLong(8, pos);
             if (parentXPath == null || "/".equals(parentXPath))
                 parentXPath = "";
             ps.setString(9, type.getName() + parentXPath + "/" + assignmentAlias);
             ps.setString(10, assignmentAlias);
             ps.setNull(11, Types.NUMERIC);
             if (tmp == null)
                 ps.setLong(12, FxAssignment.NO_PARENT);
             else
                 ps.setLong(12, tmp.getId());
             ps.setLong(13, newPropertyId);
             ps.setLong(14, property.getACL().getId());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{property.getLabel(), property.getHint(), property.getDefaultValue()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT", "DEFAULT_VALUE"}, "ID", newAssignmentId);
             StructureLoader.reload(con);
             htracker.track(type, "history.assignment.createProperty", property.getName(), type.getId(), type.getName());
             createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, ps, sql, type.getDerivedTypes());
         } catch (FxNotFoundException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.property.exists", property.getName(), (parentXPath.length() == 0 ? "/" : parentXPath));
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return newAssignmentId;
     }
 
     /*
     * Updates the options of an FxGroup
     * Before the options are updated, they are compared against the options that are
     * already stored in the DB. If there are changes, all present options are deleted
     * in the DB and newly created afterwards from the assignment's option list.
     *
     * @return if the options changed.
     */
 
     private boolean updateGroupOptions(Connection con, FxGroupEdit group) throws SQLException {
         boolean changed = false;
         FxGroupEdit org = new FxGroupEdit(CacheAdmin.getEnvironment().getGroup(group.getId()));
         if (org.getOptions().size() != group.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = group.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_GROUP_OPTIONS, "ID", group.getId(), null, group.getOptions());
         return changed;
     }
 
     /*
      * Updates the options of an FxProperty
      * Before the options are updated, they are compared against the options that are
      * already stored in the DB. If there are changes, all present options are deleted
      * in the DB and newly created afterwards from the assignment's option list.
      *
      * @return if the options changed.
      */
 
     private boolean updatePropertyOptions(Connection con, FxPropertyEdit prop) throws SQLException {
         boolean changed = false;
         FxPropertyEdit org = new FxPropertyEdit(CacheAdmin.getEnvironment().getProperty(prop.getId()));
         if (org.getOptions().size() != prop.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = prop.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_PROPERTY_OPTIONS, "ID", prop.getId(), null, prop.getOptions());
         return changed;
     }
 
     /*
     * Updates the options of an FxGroupAssignment
     * Before the options are updated, they are compared against the options that are
     * already stored in the DB. If there are changes, all present options are deleted
     * in the DB and newly created afterwards from the assignment's option list.
     *
     * @return if the options changed.
     */
 
     private boolean updateGroupAssignmentOptions(Connection con, FxGroupAssignment ga) throws SQLException {
         boolean changed = false;
         FxGroupAssignmentEdit org = ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(ga.getId())).asEditable();
         FxGroupAssignmentEdit group = ga.asEditable();
         if (org.getOptions().size() != group.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = group.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_GROUP_OPTIONS, "ID", group.getGroup().getId(), group.getId(), group.getOptions());
         return changed;
     }
 
     /*
      * Updates the options of an FxPropertyAssignment
      * Before the options are updated, they are compared against the options that are
      * already stored in the DB. If there are changes, all present options are deleted
      * in the DB and newly created afterwards from the assignment's option list.
      *
      * @return if the options changed.
      */
 
     private boolean updatePropertyAssignmentOptions(Connection con, FxPropertyAssignment original, FxPropertyAssignment modified) throws SQLException {
         boolean changed = false;
         FxPropertyAssignmentEdit org = original.asEditable();
         FxPropertyAssignmentEdit prop = modified.asEditable();
         if (org.getOptions().size() != prop.getOptions().size()) {
             changed = true;
         } else {
             for (int i = 0; i < org.getOptions().size(); i++) {
                 FxStructureOption orgOpt = org.getOptions().get(i);
                 FxStructureOption propOpt = prop.getOption(orgOpt.getKey());
                 if (!orgOpt.equals(propOpt)) {
                     changed = true;
                     break;
                 }
             }
         }
         if (changed)
             storeOptions(con, TBL_PROPERTY_OPTIONS, "ID", original.getProperty().getId(), original.getId(), prop.getOptions());
         return changed;
     }
 
     /*
      * Helper to store options, (the information in brackets expalains how to use this method to store the options
      * for an FxPropertyAssignment)
      *
      * @param con               the DB connection
      * @param table             the table name to store the options (e.g. TBL_PROPERTY_OPTIONS)
      * @param primaryColumn     the column name of the primary key (where the property Id is stored, e.g. ID)
      * @param primaryId         the primary key itself (the property Id, e.g. FxPropertyAssignment.getProperty().getId())
      * @param assignmentId      the foreign key, may be <code>null</code> (the assignment Id, e.g. FxPropertyAssignment.getId())
      * @param options           the option list to store (e.g. FxPropertyAssignmentEdit.getOptions())
      */
     private void storeOptions(Connection con, String table, String primaryColumn, long primaryId, Long assignmentId,
                               List<FxStructureOption> options) throws SQLException {
         PreparedStatement ps = null;
         try {
             if (assignmentId == null) {
                 ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + primaryColumn + "=? AND ASSID IS NULL");
             } else {
                 ps = con.prepareStatement("DELETE FROM " + table + " WHERE " + primaryColumn + "=? AND ASSID=?");
                 ps.setLong(2, assignmentId);
             }
             ps.setLong(1, primaryId);
             ps.executeUpdate();
             if (options == null || options.size() == 0)
                 return;
             ps.close();
             ps = con.prepareStatement("INSERT INTO " + table + " (" + primaryColumn + ",ASSID,OPTKEY,MAYOVERRIDE,OPTVALUE)VALUES(?,?,?,?,?)");
             for (FxStructureOption option : options) {
                 ps.setLong(1, primaryId);
                 if (assignmentId != null)
                     ps.setLong(2, assignmentId);
                 else
                     ps.setNull(2, java.sql.Types.NUMERIC);
                 ps.setString(3, option.getKey());
                 ps.setBoolean(4, option.isOverrideable());
                 ps.setString(5, option.getValue());
                 ps.addBatch();
             }
             ps.executeBatch();
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Helper to process all derived types and derivates of derived types
      *
      * @param assignment the assignment processed
      * @param con        an open and valid connection
      * @param ps         prepared statement being worked with
      * @param sb         StringBuilder
      * @param types      (derived) types to process - will recurse to derived types of these types
      * @throws FxApplicationException on errors
      */
     private void createInheritedAssignments(FxAssignment assignment, Connection con, PreparedStatement ps,
                                             StringBuilder sb, List<FxType> types) throws FxApplicationException {
         for (FxType derivedType : types) {
             if (assignment instanceof FxPropertyAssignment) {
                 createPropertyAssignment(con, ps, sb,
                         FxPropertyAssignmentEdit.createNew((FxPropertyAssignment) assignment, derivedType,
                                 assignment.getAlias(),
                                 assignment.hasParentGroupAssignment()
                                         ? assignment.getParentGroupAssignment().getXPath()
                                         : "/")
                 );
             } else if (assignment instanceof FxGroupAssignment) {
                 createGroupAssignment(con, ps, sb,
                         FxGroupAssignmentEdit.createNew((FxGroupAssignment) assignment, derivedType, assignment.getAlias(),
                                 assignment.hasParentGroupAssignment()
                                         ? assignment.getParentGroupAssignment().getXPath()
                                         : "/"),
                         true
                 );
             }
 //            if (derivedType.getDerivedTypes().size() > 0)  //one level deeper ...
 //                _inheritedAssignmentsCreate(assignment, con, ps, sb, derivedType.getDerivedTypes());
         }
     }
 
     /**
      * Remove a property, all its assignments and all attributes in contents referencing this property
      *
      * @param propertyId id of the property to remove
      * @throws FxApplicationException on errors
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeProperty(long propertyId) throws FxApplicationException {
         //TODO
     }
 
     private boolean updateGroup(Connection _con, PreparedStatement ps, StringBuilder sql,
                                 FxGroupEdit group) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         Connection con = _con;
         boolean changes = false;
         StringBuilder changesDesc = new StringBuilder(200);
         FxGroup org = CacheAdmin.getEnvironment().getGroup(group.getId());
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
 
             //TODO: check valid multiplicity like in updateproperty
             if (org.getMultiplicity().getMin() != group.getMultiplicity().getMin() ||
                     org.getMultiplicity().getMax() != group.getMultiplicity().getMax()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET DEFMINMULT=? ,DEFMAXMULT=? WHERE ID=?");
                 ps.setInt(1, group.getMultiplicity().getMin());
                 ps.setInt(2, group.getMultiplicity().getMax());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("multiplicity=").append(group.getMultiplicity());
                 changes = true;
             }
 
             if (org.getLabel() != null && !org.getLabel().equals(group.getLabel()) ||
                     org.getLabel() == null && group.getLabel() != null ||
                     org.getHint() != null && !org.getHint().equals(group.getHint()) ||
                     org.getHint() == null && group.getHint() != null) {
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_GROUPS, new String[]{"DESCRIPTION", "HINT"}, "ID", group.getId());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("label=").append(group.getLabel()).append(',');
                 changesDesc.append("hint=").append(group.getHint()).append(',');
                 changes = true;
             }
             //TODO: thorw exceptipon that this is not supported yet
             if (!org.getName().equals(group.getName())) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET NAME=? WHERE ID=?");
                 ps.setString(1, group.getName());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("name=").append(group.getName());
                 changes = true;
             }
             if (org.mayOverrideBaseMultiplicity() != group.mayOverrideBaseMultiplicity()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_GROUPS + " SET MAYOVERRIDEMULT=? WHERE ID=?");
                 ps.setBoolean(1, group.mayOverrideBaseMultiplicity());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("mayOverrideMultiplicity=").append(group.mayOverrideBaseMultiplicity());
                 changes = true;
             }
 
             if (updateGroupOptions(con, group)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = group.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverrideable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
 
             if (changes) {
                 //TODO: invoke htracker with changeDesc
             }
 
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             /*TODO: Determine if this must be checked
             if (Database.isUniqueConstraintViolation(e))
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", prop.getAlias(), prop.getXPath());
             */
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (_con == null) {
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
             }
         }
         return changes;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createGroup(FxGroupEdit group, String parentXPath) throws FxApplicationException {
         return createGroup(FxType.ROOT_ID, group, parentXPath);
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long createGroup(long typeId, FxGroupEdit group, String parentXPath) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(2000);
         long newGroupId;
         long newAssignmentId;
         try {
             parentXPath = parentXPath.toUpperCase();
             FxType type = CacheAdmin.getEnvironment().getType(typeId);
             FxAssignment tmp = type.getAssignment(parentXPath);
             if (tmp != null && tmp instanceof FxPropertyAssignment)
                 throw new FxInvalidParameterException("ex.structure.assignment.noGroup", parentXPath);
             //parentXPath is valid, create the group, then assign it to root
             newGroupId = seq.getId(SequencerEngine.System.TYPEGROUP);
             con = Database.getDbConnection();
             //create group
             sql.append("INSERT INTO ").append(TBL_STRUCT_GROUPS).
                     append("(ID,NAME,DEFMINMULT,DEFMAXMULT,MAYOVERRIDEMULT)VALUES(?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, newGroupId);
             ps.setString(2, group.getName());
             ps.setInt(3, group.getMultiplicity().getMin());
             ps.setInt(4, group.getMultiplicity().getMax());
             ps.setBoolean(5, group.mayOverrideBaseMultiplicity());
             ps.executeUpdate();
             ps.close();
             sql.setLength(0);
             Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()},
                     con, TBL_STRUCT_GROUPS, new String[]{"DESCRIPTION", "HINT"}, "ID", newGroupId);
             //calc new position
             sql.append("SELECT COALESCE(MAX(POS)+1,0) FROM ").append(TBL_STRUCT_ASSIGNMENTS).
                     append(" WHERE PARENTGROUP=? AND TYPEDEF=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(2, typeId);
             ResultSet rs = ps.executeQuery();
             long pos = 0;
             if (rs != null && rs.next()) {
                 if (rs.wasNull())
                     pos = 0;
                 else
                     pos = rs.getLong(1);
             }
             ps.close();
             sql.setLength(0);
             //create root assignment
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10     11   12          13     14
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,AGROUP,GROUPMODE)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(SequencerEngine.System.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_GROUP);
             ps.setBoolean(3, true);
             ps.setLong(4, typeId);
             ps.setInt(5, group.getMultiplicity().getMin());
             ps.setInt(6, group.getMultiplicity().getMax());
             if (group.getMultiplicity().isValid(group.getAssignmentDefaultMultiplicity())) {
                 ps.setInt(7, group.getAssignmentDefaultMultiplicity());
             } else {
                 //default is min(min,1).
                 ps.setInt(7, group.getMultiplicity().getMin() > 1 ? group.getMultiplicity().getMin() : 1);
             }
             ps.setLong(8, pos);
             if (parentXPath == null || "/".equals(parentXPath))
                 parentXPath = "";
             ps.setString(9, type.getName() + parentXPath + "/" + group.getName());
             ps.setString(10, group.getName());
             ps.setNull(11, java.sql.Types.NUMERIC);
             ps.setLong(12, (tmp == null ? FxAssignment.NO_PARENT : tmp.getId()));
             ps.setLong(13, newGroupId);
             ps.setInt(14, group.getAssignmentGroupMode().getId());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
             StructureLoader.reload(con);
             htracker.track(type, "history.assignment.createGroup", group.getName(), type.getId(), type.getName());
             createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, ps, sql, type.getDerivedTypes());
         } catch (FxNotFoundException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.group.exists", group.getName(), (parentXPath.length() == 0 ? "/" : parentXPath));
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return newAssignmentId;
     }
 
     /**
      * Removes a group and all subgroups and properties assigned to it as well as all references in content instances
      *
      * @param groupId id of the group to remove
      * @throws FxApplicationException on errors
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeGroup(long groupId) throws FxApplicationException {
         //TODO
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxAssignment assignment, boolean createSubAssignments) throws FxApplicationException {
         long returnId;
         boolean reload = false;
         if (assignment instanceof FxPropertyAssignmentEdit) {
             if (((FxPropertyAssignmentEdit) assignment).isNew()) {
                 returnId = createPropertyAssignment(null, null, null, (FxPropertyAssignmentEdit) assignment);
             } else {
                 returnId = assignment.getId();
                 try {
                     reload = updatePropertyAssignment(null, null, null, null, (FxPropertyAssignmentEdit) assignment);
                 } catch (FxLoadException e) {
                     ctx.setRollbackOnly();
                     throw new FxUpdateException(e);
                 } catch (FxNotFoundException e) {
                     ctx.setRollbackOnly();
                     throw new FxUpdateException(e);
                 }
             }
 
         } else if (assignment instanceof FxGroupAssignmentEdit) {
             if (((FxGroupAssignmentEdit) assignment).isNew()) {
                 returnId = createGroupAssignment(null, null, null, (FxGroupAssignmentEdit) assignment, createSubAssignments);
             } else {
                 returnId = assignment.getId();
                 try {
                     reload = updateGroupAssignment(null, null, null, (FxGroupAssignmentEdit) assignment);
                 } catch (FxLoadException e) {
                     ctx.setRollbackOnly();
                     throw new FxUpdateException(e);
                 } catch (FxNotFoundException e) {
                     ctx.setRollbackOnly();
                     throw new FxUpdateException(e);
                 }
             }
         } else
             throw new FxInvalidParameterException("ASSIGNMENT", "ex.structure.assignment.noEditAssignment");
         try {
             if (reload) {
                 StructureLoader.reload(null);
                 //clear instance cache
                 CacheAdmin.expireCachedContents();
             }
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         }
         return returnId;
     }
 
     private boolean updateGroupAssignment(Connection _con, PreparedStatement ps, StringBuilder sql,
                                           FxGroupAssignmentEdit group) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         if (group.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.update.new", group.getXPath());
         Connection con = _con;
         boolean changes = false;
         StringBuilder changesDesc = new StringBuilder(200);
         FxGroupAssignment org = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(group.getId());
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
             if (org.isEnabled() != group.isEnabled()) {
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("enabled=").append(group.isEnabled());
                 //apply for all child groups and properties as well!
                 if (org.getAssignments().size() > 0)
                     changesDesc.append(", ").append(group.isEnabled() ? "en" : "dis").append("abled child assignments: ");
                 for (FxAssignment as : org.getAssignments()) {
                     changesDesc.append(as.getXPath()).append(',');
                 }
                 if (changesDesc.charAt(changesDesc.length() - 1) == ',')
                     changesDesc.deleteCharAt(changesDesc.length() - 1);
                 if (!group.isEnabled())
                     removeAssignment(org.getId(), true, false, true);
                 else {
                     StringBuilder affectedAssignment = new StringBuilder(500);
                     affectedAssignment.append(org.getId());
                     for (FxAssignment as : org.getAllChildAssignments())
                         affectedAssignment.append(",").append(as.getId());
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ENABLED=? WHERE ID IN (" + affectedAssignment + ")");
                     ps.setBoolean(1, true);
                     ps.executeUpdate();
                 }
                 changes = true;
             }
 
             //TODO: check if multiplicity is in valid ranges
             if (org.getDefaultMultiplicity() != group.getDefaultMultiplicity()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFMULT=? WHERE ID=?");
                 ps.setInt(1, group.getDefaultMultiplicity());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("defaultMultiplicity=").append(group.getDefaultMultiplicity());
                 changes = true;
             }
 
             //TODO: must not be changed
             if (org.getAssignedType().getId() != group.getAssignedType().getId()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET TYPEDEF=? WHERE ID=?");
                 ps.setLong(1, group.getAssignedType().getId());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("assignedType=").append(group.getAssignedType());
                 changes = true;
             }
             //siehe prop
             if (org.getMultiplicity().getMin() != group.getMultiplicity().getMin() ||
                     org.getMultiplicity().getMax() != group.getMultiplicity().getMax()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET MINMULT=? ,MAXMULT=? WHERE ID=?");
                 ps.setInt(1, group.getMultiplicity().getMin());
                 ps.setInt(2, group.getMultiplicity().getMax());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("multiplicity=").append(group.getMultiplicity());
                 changes = true;
             }
             if (org.getPosition() != group.getPosition()) {
                 int finalPos = changeAssignmentPosition(con, group);
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("position=").append(finalPos);
                 changes = true;
             }
             if (!org.getXPath().equals(group.getXPath()) || !org.getAlias().equals(group.getAlias())) {
                 if (!XPathElement.isValidXPath(XPathElement.stripType(group.getXPath())) ||
                         group.getAlias().equals(XPathElement.lastElement(XPathElement.stripType(org.getXPath())).getAlias()))
                     throw new FxUpdateException("ex.structure.assignment.noXPath");
                 try {
                     //avoid duplicates
                     org.getAssignedType().getAssignment(group.getXPath());
                     throw new FxUpdateException("ex.structure.assignment.exists", group.getXPath(), group.getAssignedType().getName());
                 } catch (FxNotFoundException e) {
                     //expected
                 }
                 //TODO: make sure just the alias changed
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=?, XALIAS=? WHERE ID=?");
                 ps.setString(1, group.getXPath());
                 ps.setString(2, group.getAlias());
                 ps.setLong(3, group.getId());
                 ps.executeUpdate();
                 ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
                 storage.updateXPath(con, group.getId(), XPathElement.stripType(org.getXPath()),
                         XPathElement.stripType(group.getXPath()));
                 //update all child assignments
                 ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=? WHERE ID=?");
                 for (FxAssignment child : org.getAllChildAssignments()) {
                     ps.setString(1, group.getXPath() + child.getXPath().substring(org.getXPath().length()));
                     ps.setLong(2, child.getId());
                     ps.executeUpdate();
                     storage.updateXPath(con, child.getId(), XPathElement.stripType(child.getXPath()),
                             XPathElement.stripType(group.getXPath() + child.getXPath().substring(org.getXPath().length())));
                 }
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("xPath=").append(group.getXPath()).append(",alias=").append(group.getAlias());
                 changes = true;
             }
 
             //TODO:may not be changed, throw exception
             /*if (org.getBaseAssignmentId() != group.getBaseAssignmentId()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET BASE=? WHERE ID=?");
                 ps.setLong(1, group.getBaseAssignmentId());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("baseAssignment=").append(group.getBaseAssignmentId());
                 changes = true;
             }*/
 
             //TODO:may not be changed, throw exception
             /*if (org.getParentGroupAssignment() != null &&
                     !org.getParentGroupAssignment().equals(group.getParentGroupAssignment()) ||
                     group.getParentGroupAssignment() != null &&
                             !group.getParentGroupAssignment().equals(org.getParentGroupAssignment())) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET PARENTGROUP=? WHERE ID=?");
                 if (group.getParentGroupAssignment() != null)
                     ps.setLong(1, group.getParentGroupAssignment().getId());
                 else
                     ps.setLong(1, FxAssignment.NO_PARENT);
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("parentGroupAssignment=").append(group.getParentGroupAssignment());
                 changes = true;
             }*/
 
             if (org.getLabel() != null && !org.getLabel().equals(group.getLabel()) ||
                     org.getLabel() == null && group.getLabel() != null ||
                     org.getHint() != null && !org.getHint().equals(group.getHint()) ||
                     org.getHint() == null && group.getHint() != null) {
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", group.getId());
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("label=").append(group.getLabel()).append(',');
                 changesDesc.append("hint=").append(group.getHint()).append(',');
                 changes = true;
             }
 
             //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
             if (!org.isSystemInternal() && group.isSystemInternal() && FxContext.get().getTicket().isGlobalSupervisor()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET SYSINTERNAL=? WHERE ID=?");
                 ps.setBoolean(1, group.isSystemInternal());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("systemInternal=").append(group.isSystemInternal());
                 changes = true;
             }
 
             //TODO: only make changable if no instances of this group exist, or if the mode is changed from one.of to any.of
             if (org.getMode().getId() != group.getMode().getId()) {
                 if (ps != null) ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET GROUPMODE=? WHERE ID=?");
                 ps.setLong(1, group.getMode().getId());
                 ps.setLong(2, group.getId());
                 ps.executeUpdate();
                 if (changes)
                     changesDesc.append(',');
                 changesDesc.append("groupMode=").append(group.getMode().getId());
                 changes = true;
             }
 
             if (updateGroupAssignmentOptions(con, group)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = group.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverrideable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
             //TODO: compare all possible modifications
             if (changes)
                 htracker.track(group.getAssignedType(), "history.assignment.updateGroupAssignment", group.getXPath(), group.getAssignedType().getId(), group.getAssignedType().getName(),
                         group.getGroup().getId(), group.getGroup().getName(), changesDesc.toString());
 
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.group.exists", group.getAlias(), group.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (_con == null) {
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
             }
         }
         return changes;
     }
 
     /**
      * Change an assignments position, updating positions of all assignments in the same hierarchy level
      *
      * @param con        an open and valid connection
      * @param assignment the assignment with the desired position
      * @return the position that "really" was assigned
      * @throws FxUpdateException on errors
      */
     private int changeAssignmentPosition(Connection con, FxAssignment assignment) throws FxUpdateException {
         int pos = assignment.getPosition();
         if (pos < 0)
             pos = 0;
         List<FxAssignment> parentAssignments;
         long parentGroupId;
 
         if (!assignment.hasParentGroupAssignment()) {
             try {
                 parentAssignments = assignment.getAssignedType().getConnectedAssignments("/");
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
             parentGroupId = 0;
         } else {
             parentAssignments = assignment.getParentGroupAssignment().getAssignments();
             parentGroupId = assignment.getParentGroupAssignment().getId();
         }
        if (parentAssignments.size() == 1)
             return 0; //need at least 2 assignments to move anything
 
         //algorithm:
         // update the position of the assignment to be moved to be maxPos+1 (savePos)
         // close the gap
         // open a gap at the destination position
         // update savepos (maxPos+1) to the destination position
         int savePos = parentAssignments.size() + 1;
         if (pos > parentAssignments.size())
             pos = parentAssignments.size() - 1;
        int orgPos = -1;
         for (FxAssignment a : parentAssignments) {
             if (a.getId() == assignment.getId()) {
                 orgPos = a.getPosition();
                 break;
             }
         }
         PreparedStatement ps = null;
         try {
             // (1) update the position of the assignment to be moved to be maxPos+1 (savePos)
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET POS=? WHERE ID=?");
             ps.setInt(1, savePos);
             ps.setLong(2, assignment.getId());
             ps.executeUpdate();
             // (2) close the gap
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET POS=POS-1 WHERE " +
                     "TYPEDEF=? AND PARENTGROUP=? AND POS>? AND ID<>?");
             ps.setLong(1, assignment.getAssignedType().getId());
 
             ps.setLong(2, parentGroupId);
             ps.setInt(3, orgPos);
             ps.setLong(4, assignment.getId());
             ps.executeUpdate();
             // (3) open a gap at the destination position
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET POS=POS+1 WHERE " +
                     "TYPEDEF=? AND PARENTGROUP=? AND POS>=? AND ID<>?");
             ps.setLong(1, assignment.getAssignedType().getId());
             ps.setLong(2, parentGroupId);
             ps.setInt(3, pos);
             ps.setLong(4, assignment.getId());
             ps.executeUpdate();
             // update savepos (maxPos+1) to the destination position
             ps.close();
             ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET POS=? WHERE ID=?");
             ps.setInt(1, pos);
             ps.setLong(2, assignment.getId());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
         return pos;
     }
 
     private long createGroupAssignment(Connection _con, PreparedStatement ps, StringBuilder sql, FxGroupAssignmentEdit group, boolean createSubAssignments) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         if (!group.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.create.existing", group.getXPath());
         Connection con = _con;
         long newAssignmentId;
         try {
             if (con == null)
                 con = Database.getDbConnection();
             FxGroupAssignment thisGroupAssignment;
             String XPath;
             if (!group.getXPath().startsWith(group.getAssignedType().getName())) {
                 if (group.getAlias() != null)
                     XPath = XPathElement.buildXPath(false, group.getAssignedType().getName(), group.getXPath());
                 else
                     XPath = "/";
             } else
                 XPath = group.getXPath();
             if (group.getAlias() != null) {
                 sql.setLength(0);
                 sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                         //               1  2     3       4       5       6       7       8   9     10     11   12          13     14          15
                                 append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,AGROUP,SYSINTERNAL,GROUPMODE)" +
                                 "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                 if (ps != null)
                     ps.close();
                 ps = con.prepareStatement(sql.toString());
                 newAssignmentId = seq.getId(SequencerEngine.System.ASSIGNMENT);
                 ps.setLong(1, newAssignmentId);
                 ps.setInt(2, FxAssignment.TYPE_GROUP);
                 ps.setBoolean(3, group.isEnabled());
                 ps.setLong(4, group.getAssignedType().getId());
                 ps.setInt(5, group.getMultiplicity().getMin());
                 ps.setInt(6, group.getMultiplicity().getMax());
                 ps.setInt(7, group.getDefaultMultiplicity());
                 int position = getValidPosition(con, sql, group.getPosition(), group.getAssignedType().getId(), group.getParentGroupAssignment());
                 ps.setInt(8, position);
                 ps.setString(9, XPath);
                 ps.setString(10, group.getAlias());
                 ps.setLong(11, group.getBaseAssignmentId());
                 ps.setLong(12, group.getParentGroupAssignment() == null ? FxAssignment.NO_PARENT : group.getParentGroupAssignment().getId());
                 ps.setLong(13, group.getGroup().getId());
                 ps.setBoolean(14, group.isSystemInternal());
                 ps.setInt(15, group.getMode().getId());
                 ps.executeUpdate();
                 Database.storeFxString(new FxString[]{group.getLabel(), group.getHint()}, con,
                         TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT"}, "ID", newAssignmentId);
                 thisGroupAssignment = new FxGroupAssignment(newAssignmentId, true, group.getAssignedType(),
                         group.getAlias(), XPath, position, group.getMultiplicity(), group.getDefaultMultiplicity(),
                         group.getParentGroupAssignment(), group.getBaseAssignmentId(),
                         group.getLabel(), group.getHint(), group.getGroup(), group.getMode(), null);
                 changeAssignmentPosition(con, thisGroupAssignment);
             } else {
                 thisGroupAssignment = null;
                 newAssignmentId = FxAssignment.NO_PARENT;
             }
             htracker.track(group.getAssignedType(), "history.assignment.createGroupAssignment", XPath, group.getAssignedType().getId(), group.getAssignedType().getName(),
                     group.getGroup().getId(), group.getGroup().getName());
             if (group.getBaseAssignmentId() != FxAssignment.ROOT_BASE && createSubAssignments) {
                 FxGroupAssignment baseGroup = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(group.getBaseAssignmentId());
                 for (FxGroupAssignment ga : baseGroup.getAssignedGroups()) {
                     FxGroupAssignmentEdit gae = new FxGroupAssignmentEdit(ga);
                     gae.setEnabled(group.isEnabled());
                     createGroupAssignment(con, null, sql, FxGroupAssignmentEdit.createNew(gae, group.getAssignedType(), ga.getAlias(), XPath, thisGroupAssignment), createSubAssignments);
                 }
                 for (FxPropertyAssignment pa : baseGroup.getAssignedProperties()) {
                     FxPropertyAssignmentEdit pae = new FxPropertyAssignmentEdit(pa);
                     pae.setEnabled(group.isEnabled());
                     createPropertyAssignment(con, null, sql, FxPropertyAssignmentEdit.createNew(pae, group.getAssignedType(), pa.getAlias(), XPath, thisGroupAssignment));
                 }
             }
             try {
                 StructureLoader.reload(con);
             } catch (FxCacheException e) {
                 ctx.setRollbackOnly();
                 throw new FxCreateException(e, "ex.cache", e.getMessage());
             }
             createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, ps, sql,
                     group.getAssignedType().getDerivedTypes());
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.group.exists", group.getAlias(), group.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxCreateException(e);
         } finally {
             if (_con == null) {
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
             }
         }
         return newAssignmentId;
     }
 
     private boolean updateProperty(Connection _con, PreparedStatement ps, StringBuilder sql,
                                    FxPropertyEdit prop) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         if (prop.isNew())
             throw new FxInvalidParameterException("ex.structure.property.update.new", prop.getName());
         Connection con = _con;
         boolean changes = false;
         StringBuilder changesDesc = new StringBuilder(200);
         FxProperty org = CacheAdmin.getEnvironment().getProperty(prop.getId());
 
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
 
             if (!org.isSystemInternal() || FxContext.get().getTicket().isGlobalSupervisor()) {
 
                 if (org.mayOverrideBaseMultiplicity() != prop.mayOverrideBaseMultiplicity()) {
                     if (!prop.mayOverrideBaseMultiplicity()) {
                         if (getInstanceMultiplicity(con, org.getId(), true) < prop.getMultiplicity().getMin())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
                         if (getInstanceMultiplicity(con, org.getId(), false) > prop.getMultiplicity().getMax())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                     }
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET MAYOVERRIDEMULT=? WHERE ID=?");
                     ps.setBoolean(1, prop.mayOverrideBaseMultiplicity());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("mayOverrideMultiplicity=").append(prop.mayOverrideBaseMultiplicity());
                     changes = true;
                 }
 
                 if (org.getMultiplicity().getMin() != prop.getMultiplicity().getMin() ||
                         org.getMultiplicity().getMax() != prop.getMultiplicity().getMax()) {
                     if (!prop.mayOverrideBaseMultiplicity()) {
                         if (org.getMultiplicity().getMin() < prop.getMultiplicity().getMin()) {
                             if (getInstanceMultiplicity(con, org.getId(), true) < prop.getMultiplicity().getMin())
                                 throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
                         }
                         if (org.getMultiplicity().getMax() > prop.getMultiplicity().getMax()) {
                             if (getInstanceMultiplicity(con, org.getId(), false) > prop.getMultiplicity().getMax())
                                 throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                         }
                     }
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET DEFMINMULT=? ,DEFMAXMULT=? WHERE ID=?");
                     ps.setInt(1, prop.getMultiplicity().getMin());
                     ps.setInt(2, prop.getMultiplicity().getMax());
                     ps.setLong(3, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiplicity=").append(prop.getMultiplicity());
                     changes = true;
                 }
                 //not supported yet
                 if (!org.getName().equals(prop.getName())) {
                     throw new FxUpdateException("ex.structure.modification.notSuppoerted", "name");
                     /*
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET NAME=? WHERE ID=?");
                     ps.setString(1, prop.getName());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("name=").append(prop.getName());
                     changes = true;
                     */
                 }
                 /* darf nicht verändert werden
                 if (org.getDataType().getId() != prop.getDataType().getId()) {
                    if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET DATATYPE=? WHERE ID=?");
                     ps.setLong(1, prop.getDataType().getId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("dataType=").append(prop.getDataType().getName());
                     changes = true;
                 }
                 */
 
                 //may only change if there are no existing content instances that use this property already
                 if (org.getReferencedType() != null && prop.getReferencedType() != null &&
                         org.getReferencedType().getId() != prop.getReferencedType().getId() ||
                         org.hasReferencedType() != prop.hasReferencedType()) {
                     if (getInstanceCount(con, org.getId()) == 0) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET REFTYPE=? WHERE ID=?");
                         ps.setLong(2, prop.getId());
                         if (prop.hasReferencedType()) {
                             ps.setLong(1, prop.getReferencedType().getId());
                         } else
                             ps.setNull(1, java.sql.Types.NUMERIC);
                         ps.executeUpdate();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("referencedType=").append(prop.getReferencedType());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "referencedType");
                 }
 
 
                 if (org.isFulltextIndexed() != prop.isFulltextIndexed()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET ISFULLTEXTINDEXED=? WHERE ID=?");
                     ps.setBoolean(1, prop.isFulltextIndexed());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("isFulltextIndexed=").append(prop.isFulltextIndexed());
                     changes = true;
                 }
                 if (org.mayOverrideACL() != prop.mayOverrideACL()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET MAYOVERRIDEACL=? WHERE ID=?");
                     ps.setBoolean(1, prop.mayOverrideACL());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("mayOverrideACL=").append(prop.mayOverrideACL());
                     changes = true;
                 }
                 //may only change if there are no existing content instances that use this property already
                 if (org.getReferencedList() != null && prop.getReferencedList() != null &&
                         org.getReferencedList().getId() != prop.getReferencedList().getId() ||
                         org.hasReferencedList() != prop.hasReferencedList()) {
                     if (getInstanceCount(con, org.getId()) == 0) {
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET REFLIST=? WHERE ID=?");
                         ps.setLong(2, prop.getId());
                         if (prop.hasReferencedList()) {
                             ps.setLong(1, prop.getReferencedList().getId());
                         } else
                             ps.setNull(1, java.sql.Types.NUMERIC);
                         ps.executeUpdate();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("referencedList=").append(prop.getReferencedList());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "referencedList");
                 }
 
                 if (org.getUniqueMode() != prop.getUniqueMode()) {
                     if (getInstanceCount(con, org.getId()) == 0 || prop.getUniqueMode().equals(UniqueMode.getById(0))) {
                         if (ps != null) ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET UNIQUEMODE=? WHERE ID=?");
                         ps.setLong(1, prop.getUniqueMode().getId());
                         ps.setLong(2, prop.getId());
                         ps.executeUpdate();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("uniqueMode=").append(prop.getUniqueMode().getId());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.contentExists", "uniqueMode");
                 }
 
                 if (org.getACL().getId() != prop.getACL().getId()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET ACL=? WHERE ID=?");
                     ps.setLong(1, prop.getACL().getId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("acl=").append(prop.getACL().getId());
                     changes = true;
                 }
                 if (org.getLabel() != null && !org.getLabel().equals(prop.getLabel()) ||
                         org.getLabel() == null && prop.getLabel() != null ||
                         org.getHint() != null && !org.getHint().equals(prop.getHint()) ||
                         org.getHint() == null && prop.getHint() != null ||
                         org.getDefaultValue() != null && !org.getDefaultValue().equals(prop.getDefaultValue()) ||
                         org.getDefaultValue() == null && prop.getDefaultValue() != null) {
                     Database.storeFxString(new FxString[]{prop.getLabel(), prop.getHint(), prop.getDefaultValue()}, con,
                             TBL_STRUCT_PROPERTIES, new String[]{"DESCRIPTION", "HINT", "DEFAULT_VALUE"}, "ID", prop.getId());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("label=").append(prop.getLabel()).append(',');
                     changesDesc.append("hint=").append(prop.getHint()).append(',');
                     changesDesc.append("defaultValue=").append(prop.getDefaultValue());
                     changes = true;
                 }
 
                 //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
                 if (!org.isSystemInternal() && prop.isSystemInternal()) {
                     if (FxContext.get().getTicket().isGlobalSupervisor()) {
                         if (ps != null) ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_PROPERTIES + " SET SYSINTERNAL=? WHERE ID=?");
                         ps.setBoolean(1, prop.isSystemInternal());
                         ps.setLong(2, prop.getId());
                         ps.executeUpdate();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("systemInternal=").append(prop.isSystemInternal());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.systemInternal.notGlobalSupervisor", prop.getName());
                 }
             }
             if (updatePropertyOptions(con, prop)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = prop.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverrideable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
 
             if (changes) {
                 //TODO: invoke htracker with changeDesc
             }
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             /*TODO: Determine if this must be checked
             if (Database.isUniqueConstraintViolation(e))
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", prop.getAlias(), prop.getXPath());
             */
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (_con == null) {
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
             }
         }
         return changes;
     }
 
     /**
      * @param _con     database conneciton
      * @param ps       prepared statement
      * @param sql      StringBuilder to cache query creation
      * @param original the original property assignment to compare changes
      *                 against and update. if==null, the original will be fetched from the cache
      * @param modified the modified property assignment
      * @return if any changes were found
      * @throws FxApplicationException on errors
      */
 
     private boolean updatePropertyAssignment(Connection _con, PreparedStatement ps, StringBuilder sql, FxPropertyAssignment original,
                                              FxPropertyAssignmentEdit modified) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         if (modified.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.update.new", modified.getXPath());
         Connection con = _con;
         boolean changes = false;
         StringBuilder changesDesc = new StringBuilder(200);
         if (original == null)
             original = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(modified.getId());
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
 
             if (!original.isSystemInternal() || FxContext.get().getTicket().isGlobalSupervisor()) {
                 if (original.isEnabled() != modified.isEnabled()) {
                     if (!modified.isEnabled())
                         removeAssignment(original.getId(), true, false, true);
                     else {
                         if (ps != null) ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ENABLED=? WHERE ID=?");
                         ps.setBoolean(1, modified.isEnabled());
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                     }
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("enabled=").append(modified.isEnabled());
                     changes = true;
                 }
                 //must not change
                 /*
                 if (org.getAssignedType().getId() != prop.getAssignedType().getId()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET TYPEDEF=? WHERE ID=?");
                     ps.setLong(1, prop.getAssignedType().getId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("assignedType=").append(prop.getAssignedType());
                     changes = true;
                 }
                 */
 
                 if (original.getMultiplicity().getMin() != modified.getMultiplicity().getMin() ||
                         original.getMultiplicity().getMax() != modified.getMultiplicity().getMax()) {
                     if (modified.getProperty().mayOverrideBaseMultiplicity()) {
                         if (getInstanceMultiplicity(con, modified.getProperty().getId(), true) < modified.getMultiplicity().getMin())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "minimumMultiplicity");
                         if (getInstanceMultiplicity(con, modified.getProperty().getId(), false) > modified.getMultiplicity().getMax())
                             throw new FxUpdateException("ex.structure.modification.contentExists", "maximumMultiplicity");
                     }
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET MINMULT=? ,MAXMULT=? WHERE ID=?");
                     ps.setInt(1, modified.getMultiplicity().getMin());
                     ps.setInt(2, modified.getMultiplicity().getMax());
                     ps.setLong(3, original.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiplicity=").append(modified.getMultiplicity());
                     changes = true;
                 }
                 if (original.getPosition() != modified.getPosition()) {
                     int finalPos = changeAssignmentPosition(con, modified);
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("position=").append(finalPos);
                     changes = true;
                 }
                 //not supported yet
                 if (!original.getXPath().equals(modified.getXPath())) {
                     throw new FxUpdateException("ex.structure.modification.notSuppoerted", "xPath");
                     /*
                     //TODO:check for valid XPath
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XPATH=? WHERE ID=?");
                     ps.setString(1, prop.getXPath());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("xPath=").append(prop.getXPath());
                     changes = true;
                     */
                 }
                 //not supported yet
                 if (!original.getAlias().equals(modified.getAlias())) {
                     throw new FxUpdateException("ex.structure.modification.notSuppoerted", "alias");
                     /*
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET XALIAS=? WHERE ID=?");
                     ps.setString(1, prop.getAlias());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("alias=").append(prop.getAlias());
                     changes = true;
                     */
                 }
                 //must not change
                 /*
                 if (org.getBaseAssignmentId() != prop.getBaseAssignmentId()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET BASE=? WHERE ID=?");
                     ps.setLong(1, prop.getBaseAssignmentId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("baseAssignment=").append(prop.getBaseAssignmentId());
                     changes = true;
                 }
                 */
                 //must not change
                 /*
                 if(org.getParentGroupAssignment() != null &&
                         !org.getParentGroupAssignment().equals(prop.getParentGroupAssignment()) ||
                         prop.getParentGroupAssignment() !=null &&
                         !prop.getParentGroupAssignment().equals(org.getParentGroupAssignment())) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET PARENTGROUP=? WHERE ID=?");
                     if (prop.getParentGroupAssignment() !=null)
                         ps.setLong(1, prop.getParentGroupAssignment().getId());
                     else
                         ps.setLong(1, FxAssignment.NO_PARENT);
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("parentGroupAssignment=").append(prop.getParentGroupAssignment());
                     changes = true;
                 }
                 */
                 //must not change
                 /*
                 if(org.getProperty().getId() != prop.getProperty().getId()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET APROPERTY=? WHERE ID=?");
                     ps.setLong(1, prop.getProperty().getId());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("property=").append(prop.getProperty().getId());
                     changes = true;
                 }
                 */
                 if (original.getACL().getId() != modified.getACL().getId()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ACL=? WHERE ID=?");
                     ps.setLong(1, modified.getACL().getId());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("acl=").append(modified.getACL().getId());
                     changes = true;
                 }
 
                 /* options are stored via storeOption method
                 if (org.isMultiLang() != prop.isMultiLang()) {
                     //TODO: only allow changes from multi to single lingual if no contents exist or all are of the same language
                     //Multi->Single: lang=system
                     //Single->Multi: lang=default language
                     if( !org.getProperty().mayOverrideMultiLang() )
                         throw new FxUpdateException("ex.structure.assignment.overrideNotAllowed.multiLang", org.getXPath(),
                                 org.getProperty().getName()).setAffectedXPath(org.getXPath());
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET ISMULTILANG=? WHERE ID=?");
                     ps.setBoolean(1, prop.isMultiLang());
                     ps.setLong(2, prop.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("multiLang=").append(prop.isMultiLang());
                     changes = true;
                 }
                 */
                 if (original.getLabel() != null && !original.getLabel().equals(modified.getLabel()) ||
                         original.getLabel() == null && modified.getLabel() != null ||
                         original.getHint() != null && !original.getHint().equals(modified.getHint()) ||
                         original.getHint() == null && modified.getHint() != null ||
                         original.getDefaultValue() != null && !original.getDefaultValue().equals(modified.getDefaultValue()) ||
                         original.getDefaultValue() == null && modified.getDefaultValue() != null) {
                     Database.storeFxString(new FxString[]{modified.getLabel(), modified.getHint(), (FxString) modified.getDefaultValue()}, con,
                             TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT", "DEFAULT_VALUE"}, "ID", original.getId());
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("label=").append(modified.getLabel()).append(',');
                     changesDesc.append("hint=").append(modified.getHint()).append(',');
                     changesDesc.append("defaultValue=").append(modified.getDefaultValue());
                     changes = true;
                 }
                 if (original.getDefaultLanguage() != modified.getDefaultLanguage()) {
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFLANG=? WHERE ID=?");
                     ps.setInt(1, modified.getDefaultLanguage());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("defaultLanguage=").append(modified.getDefaultLanguage());
                     changes = true;
                 }
 
                 if (original.getDefaultMultiplicity() != modified.getDefaultMultiplicity()) {
                     if (modified.getMultiplicity().getMin() > modified.getDefaultMultiplicity() ||
                             modified.getDefaultMultiplicity() > modified.getMultiplicity().getMax())
                         throw new FxUpdateException("ex.structure.modificaiton.defaultMultiplicity.invalid");
 
                     if (ps != null) ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET DEFMULT=? WHERE ID=?");
                     ps.setInt(1, modified.getDefaultMultiplicity());
                     ps.setLong(2, original.getId());
                     ps.executeUpdate();
                     if (changes)
                         changesDesc.append(',');
                     changesDesc.append("defaultMultiplicity=").append(modified.getDefaultMultiplicity());
                     changes = true;
                 }
                 //update SystemInternal flag, this is a one way function, so it can only be set, but not reset!!
                 if (!original.isSystemInternal() && modified.isSystemInternal()) {
                     if (FxContext.get().getTicket().isGlobalSupervisor()) {
                         if (ps != null) ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET SYSINTERNAL=? WHERE ID=?");
                         ps.setBoolean(1, modified.isSystemInternal());
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                         if (changes)
                             changesDesc.append(',');
                         changesDesc.append("systemInternal=").append(modified.isSystemInternal());
                         changes = true;
                     } else
                         throw new FxUpdateException("ex.structure.modification.systemInternal.notGlobalSupervisor", modified.getLabel());
                 }
                 /*
                 if (changes) {
                     //propagate changes to derived assignments
                     List<FxAssignment> children = CacheAdmin.getEnvironment().getDerivedAssignments(modified.getId());
                     for (FxAssignment as : children) {
                         if (as instanceof FxPropertyAssignment) {
                             updatePropertyAssignment(null, null, null, (FxPropertyAssignment) as, modified);
                         }
                     }
                     //if there are changes AND the assignment is a child,
                     // break the inheritance and make it a "ROOT_BASE" assignment
                     if(original.isDerivedAssignment()) {
                         if (ps!=null)
                             ps.close();
                         ps = con.prepareStatement("UPDATE " + TBL_STRUCT_ASSIGNMENTS + " SET BASE=? WHERE ID=?");
                         ps.setNull(1, Types.NUMERIC);
                         ps.setLong(2, original.getId());
                         ps.executeUpdate();
                            changesDesc.append(",baseAssignment=null");
                     }
                }
                */
             } else
                 throw new FxUpdateException("ex.structure.systemInternal.forbidden", modified.getLabel());
 
             if (updatePropertyAssignmentOptions(con, original, modified)) {
                 changesDesc.append(",options:");
                 List<FxStructureOption> options = modified.getOptions();
                 for (FxStructureOption option : options) {
                     changesDesc.append(option.getKey()).append("=").append(option.getValue()).append(" overridable=").
                             append(option.isOverrideable()).append(" isSet=").append(option.isSet());
                 }
                 changes = true;
             }
 
             //TODO: compare all possible modifications
             if (changes)
                 htracker.track(modified.getAssignedType(), "history.assignment.updatePropertyAssignment", original.getXPath(), modified.getAssignedType().getId(), modified.getAssignedType().getName(),
                         modified.getProperty().getId(), modified.getProperty().getName(), changesDesc.toString());
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", original.getAlias(), original.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (_con == null) {
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
             }
         }
         return changes;
     }
 
     private long createPropertyAssignment(Connection _con, PreparedStatement ps, StringBuilder sql,
                                           FxPropertyAssignmentEdit prop) throws FxApplicationException {
         if (sql == null)
             sql = new StringBuilder(1000);
         if (!prop.isNew())
             throw new FxInvalidParameterException("ex.structure.assignment.create.existing", prop.getXPath());
         Connection con = _con;
         long newAssignmentId;
         try {
             if (con == null)
                 con = Database.getDbConnection();
             sql.setLength(0);
             sql.append("INSERT INTO ").append(TBL_STRUCT_ASSIGNMENTS).
                     //               1  2     3       4       5       6       7       8   9     10     11   12          13
                             append("(ID,ATYPE,ENABLED,TYPEDEF,MINMULT,MAXMULT,DEFMULT,POS,XPATH,XALIAS,BASE,PARENTGROUP,APROPERTY," +
                             //14 15      16
                             "ACL,DEFLANG,SYSINTERNAL)" +
                             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
             if (ps != null)
                 ps.close();
             ps = con.prepareStatement(sql.toString());
             newAssignmentId = seq.getId(SequencerEngine.System.ASSIGNMENT);
             ps.setLong(1, newAssignmentId);
             ps.setInt(2, FxAssignment.TYPE_PROPERTY);
             ps.setBoolean(3, prop.isEnabled());
             ps.setLong(4, prop.getAssignedType().getId());
             ps.setInt(5, prop.getMultiplicity().getMin());
             ps.setInt(6, prop.getMultiplicity().getMax());
             ps.setInt(7, prop.getDefaultMultiplicity());
             int position = getValidPosition(con, sql, prop.getPosition(), prop.getAssignedType().getId(), prop.getParentGroupAssignment());
             ps.setInt(8, position);
             String XPath;
             if (!prop.getXPath().startsWith(prop.getAssignedType().getName()))
                 XPath = XPathElement.buildXPath(false, prop.getAssignedType().getName(), prop.getXPath());
             else
                 XPath = prop.getXPath();
             ps.setString(9, XPath);
             ps.setString(10, prop.getAlias());
             ps.setLong(11, prop.getBaseAssignmentId());
             ps.setLong(12, prop.getParentGroupAssignment() == null ? FxAssignment.NO_PARENT : prop.getParentGroupAssignment().getId());
             ps.setLong(13, prop.getProperty().getId());
             ps.setLong(14, prop.getACL().getId());
             ps.setInt(15, prop.hasDefaultLanguage() ? prop.getDefaultLanguage() : FxLanguage.SYSTEM_ID);
             ps.setBoolean(16, prop.isSystemInternal());
             ps.executeUpdate();
             Database.storeFxString(new FxString[]{prop.getLabel(), prop.getHint(), (FxString) prop.getDefaultValue()}, con,
                     TBL_STRUCT_ASSIGNMENTS, new String[]{"DESCRIPTION", "HINT", "DEFAULT_VALUE"}, "ID", newAssignmentId);
             htracker.track(prop.getAssignedType(), "history.assignment.createPropertyAssignment", XPath, prop.getAssignedType().getId(), prop.getAssignedType().getName(),
                     prop.getProperty().getId(), prop.getProperty().getName());
             storeOptions(con, TBL_PROPERTY_OPTIONS, "ID", prop.getProperty().getId(), newAssignmentId, prop.getOptions());
             FxPropertyAssignment newProp = new FxPropertyAssignment(newAssignmentId, prop.isEnabled(), prop.getAssignedType(),
                     prop.getAlias(), prop.getXPath(), prop.getPosition(), prop.getMultiplicity(), prop.getDefaultMultiplicity(),
                     prop.getParentGroupAssignment(), prop.getBaseAssignmentId(), prop.getLabel(), prop.getHint(),
                     prop.getDefaultValue(), prop.getProperty(), prop.getACL(), prop.getDefaultLanguage(), prop.getOptions());
             changeAssignmentPosition(con, newProp);
             if (!prop.isSystemInternal()) {
                 //only need a reload and inheritance handling if the property is not system internal
                 //since system internal properties are only created from the type engine we don't have to care
                 try {
                     StructureLoader.reload(con);
                 } catch (FxCacheException e) {
                     ctx.setRollbackOnly();
                     throw new FxCreateException(e, "ex.cache", e.getMessage());
                 }
                 createInheritedAssignments(CacheAdmin.getEnvironment().getAssignment(newAssignmentId), con, ps, sql,
                         prop.getAssignedType().getDerivedTypes());
             }
         } catch (SQLException e) {
             final boolean uniqueConstraintViolation = Database.isUniqueConstraintViolation(e);
             ctx.setRollbackOnly();
             if (uniqueConstraintViolation)
                 throw new FxEntryExistsException("ex.structure.assignment.property.exists", prop.getAlias(), prop.getXPath());
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, (_con == null ? con : null), ps);
         }
         return newAssignmentId;
     }
 
     /**
      * Get a valid position for the assignment within the same hierarchy.
      * Probes for the desired position first and if taken returns the next available
      *
      * @param con                   connection ( has to be valid and open!)
      * @param sql                   StringBuilder for the statement
      * @param desiredPos            desired position
      * @param typeId                FxType id
      * @param parentGroupAssignment the parent gorup assignment or <code>null</code> if assigned to the root
      * @return a valid position for the assignment
      * @throws SQLException      on errors
      * @throws FxCreateException if no result could be retrieved
      */
     private int getValidPosition(Connection con, StringBuilder sql, int desiredPos, long typeId, FxGroupAssignment parentGroupAssignment) throws SQLException, FxCreateException {
 //      original MySQL statement:
 //        select if( (select count(id) from FXS_ASSIGNMENTS where typedef=1 and parentgroup=0 and pos=1) > 0,
 //             (select (max(pos)+1) from FXS_ASSIGNMENTS WHERE typedef=1 and parentgroup=0),
 //             1) as pos
         PreparedStatement ps = null;
         sql.setLength(0);
         sql.append("SELECT IF((SELECT COUNT(ID) FROM ").append(TBL_STRUCT_ASSIGNMENTS).
                 //                             1                 2         3
                         append(" WHERE TYPEDEF=? AND PARENTGROUP=? AND POS=?)>0,(SELECT IFNULL(MAX(POS)+1,0) FROM ").
                 //                                                            4                 5  6
                         append(TBL_STRUCT_ASSIGNMENTS).append(" WHERE TYPEDEF=? AND PARENTGROUP=?),?)");
         try {
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, typeId);
             ps.setLong(2, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId());
             ps.setInt(3, desiredPos);
             ps.setLong(4, typeId);
             ps.setLong(5, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId());
             ps.setInt(6, desiredPos);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next())
                 return rs.getInt(1);
             throw new FxCreateException("ex.structure.position.failed", typeId, parentGroupAssignment == null ? FxAssignment.NO_PARENT : parentGroupAssignment.getId(), desiredPos);
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeAssignment(long assignmentId, boolean removeSubAssignments, boolean removeDerivedAssignments)
             throws FxApplicationException {
         removeAssignment(assignmentId, removeSubAssignments, removeDerivedAssignments, false);
     }
 
     private void removeAssignment(long assignmentId, boolean removeSubAssignments, boolean removeDerivedAssignments,
                                   boolean disableAssignment) throws FxApplicationException {
         FxAssignment assignment;
         assignment = CacheAdmin.getEnvironment().getAssignment(assignmentId);
         assert assignment != null : "Assignment retrieved was null";
         if (!disableAssignment) {
             //if removal, check if its a derived assignment which may not be removed
             if (assignment.isDerivedAssignment())
                 throw new FxRemoveException("ex.structure.assignment.delete.derived", assignment.getXPath());
         }
         final UserTicket ticket = FxContext.get().getTicket();
 
         Connection con = null;
         PreparedStatement ps = null;
         StringBuilder sql = new StringBuilder(500);
         try {
             con = Database.getDbConnection();
 
             List<FxAssignment> affectedAssignments = new ArrayList<FxAssignment>(10);
             affectedAssignments.add(assignment);
 
             if (assignment instanceof FxGroupAssignment && removeSubAssignments) {
                 FxGroupAssignment ga = (FxGroupAssignment) assignment;
                 _addSubAssignments(affectedAssignments, ga);
             }
 
 
             if (removeDerivedAssignments) {
                 //find all derived assignments
                 sql.append("SELECT ID FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(" WHERE BASE=?");
                 ps = con.prepareStatement(sql.toString());
                 long prevSize = 0;
                 while (prevSize != affectedAssignments.size()) { //run until no derived assignments are found
                     prevSize = affectedAssignments.size();
                     List<FxAssignment> adds = new ArrayList<FxAssignment>(5);
                     for (FxAssignment check : affectedAssignments) {
                         ps.setLong(1, check.getId());
                         ResultSet rs = ps.executeQuery();
                         if (rs != null && rs.next()) {
                             FxAssignment derived = CacheAdmin.getEnvironment().getAssignment(rs.getLong(1));
                             if (!adds.contains(derived) && !affectedAssignments.contains(derived))
                                 adds.add(derived);
                         }
                     }
                     affectedAssignments.addAll(adds);
                 }
                 ps.close();
                 sql.setLength(0);
             } else if (!disableAssignment) {
                 //find all (directly) derived assignments, flag them as 'regular' assignments and set them as new base
                 ps = breakAssignmentInheritance(con, assignment, sql);
             }
 
             //security checks
             if (!ticket.isGlobalSupervisor()) {
                 //assignment permission
                 StringBuilder assignmentList = new StringBuilder(200);
                 for (FxAssignment check : affectedAssignments) {
                     assignmentList.append(",").append(check.getId());
                     if (check instanceof FxPropertyAssignment && check.getAssignedType().usePropertyPermissions()) {
                         FxPropertyAssignment pa = (FxPropertyAssignment) check;
                         if (!ticket.mayDeleteACL(pa.getACL().getId()))
                             throw new FxNoAccessException("ex.acl.noAccess.delete", pa.getACL().getName());
                     }
                 }
                 //affected content permission
                 sql.append("SELECT DISTINCT O.ACL FROM ").append(TBL_CONTENT).
                         append(" O WHERE O.ID IN(SELECT D.ID FROM ").append(TBL_CONTENT_DATA).
                         append(" D WHERE D.ASSIGN IN(").append(assignmentList.substring(1)).append("))");
                 java.lang.System.out.println("SQL==" + sql.toString());
                 ps = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 ResultSet rs = ps.executeQuery();
                 while (rs != null && rs.next()) {
                     if (!ticket.mayDeleteACL(rs.getInt(1)))
                         throw new FxNoAccessException("ex.acl.noAccess.delete", CacheAdmin.getEnvironment().getACL(rs.getInt(1)));
                 }
                 ps.close();
             }
 
             if (disableAssignment)
                 sql.append("UPDATE ").append(TBL_STRUCT_ASSIGNMENTS).append(" SET ENABLED=? WHERE ID=?");
             else
                 sql.append("DELETE FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(" WHERE ID=?");
             ps = con.prepareStatement(sql.toString());
 
             //batch remove all multi language entries and content datas
             PreparedStatement psML = null;
             PreparedStatement psData = null;
             PreparedStatement psDataFT = null;
             PreparedStatement psBinaryGet = null;
             PreparedStatement psBinaryRemove = null;
             PreparedStatement psPropertyOptionRemove;
             PreparedStatement psGroupOptionRemove;
             try {
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_STRUCT_ASSIGNMENTS).append(ML).append(" WHERE ID=?");
                 psML = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_PROPERTY_OPTIONS).append(" WHERE ASSID=?");
                 psPropertyOptionRemove = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_GROUP_OPTIONS).append(" WHERE ASSID=?");
                 psGroupOptionRemove = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_DATA).append(" WHERE ASSIGN=?");
                 psData = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_DATA_FT).append(" WHERE ASSIGN=?");
                 psDataFT = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("SELECT DISTINCT FBLOB FROM ").
                         append(TBL_CONTENT_DATA).append(" WHERE ASSIGN=? AND FBLOB IS NOT NULL");
                 psBinaryGet = con.prepareStatement(sql.toString());
                 sql.setLength(0);
                 sql.append("DELETE FROM ").append(TBL_CONTENT_BINARY).append(" WHERE ID=?");
                 psBinaryRemove = con.prepareStatement(sql.toString());
                 for (FxAssignment ml : affectedAssignments) {
                     if (!disableAssignment) {
                         psML.setLong(1, ml.getId());
                         psML.addBatch();
                     }
                     if (ml instanceof FxPropertyAssignment) {
                         psData.setLong(1, ml.getId());
                         psData.addBatch();
                         psDataFT.setLong(1, ml.getId());
                         psDataFT.addBatch();
                         psPropertyOptionRemove.setLong(1, ml.getId());
                         psPropertyOptionRemove.addBatch();
                         //only need to remove binaries if its a binary type...
                         switch (((FxPropertyAssignment) ml).getProperty().getDataType()) {
                             case Binary:
                                 psBinaryGet.setLong(1, ml.getId());
                                 ResultSet rs = psBinaryGet.executeQuery();
                                 while (rs != null && rs.next()) {
                                     psBinaryRemove.setLong(1, rs.getLong(1));
                                     psBinaryRemove.addBatch();
                                 }
                         }
                     } else if (ml instanceof FxGroupAssignment) {
                         psGroupOptionRemove.setLong(1, ml.getId());
                         psGroupOptionRemove.addBatch();
                     }
                 }
                 if (!disableAssignment) {
                     psML.executeBatch();
                 }
                 psPropertyOptionRemove.executeBatch();
                 psGroupOptionRemove.executeBatch();
                 psBinaryRemove.executeBatch();
                 psDataFT.executeBatch();
                 psData.executeBatch();
             } finally {
                 if (psML != null)
                     psML.close();
                 if (psData != null)
                     psData.close();
                 if (psDataFT != null)
                     psDataFT.close();
                 if (psBinaryGet != null)
                     psBinaryGet.close();
                 if (psBinaryRemove != null)
                     psBinaryRemove.close();
             }
 
             List<FxAssignment> remaining = new ArrayList<FxAssignment>(affectedAssignments.size());
             int removed = 1;
             SQLException lastEx = null;
             if (disableAssignment)
                 ps.setBoolean(1, false);
             while (removed > 0) {
                 removed = 0;
                 for (FxAssignment rm : affectedAssignments) {
                     ps.setLong(disableAssignment ? 2 : 1, rm.getId());
                     try {
                         ps.executeUpdate();
                         removed++;
                     } catch (SQLException e) {
                         lastEx = e;
                         remaining.add(rm);
                     }
                 }
                 affectedAssignments.clear();
                 if (disableAssignment)
                     break;
                 affectedAssignments.addAll(remaining);
                 remaining.clear();
             }
 
             if (affectedAssignments.size() > 0)
                 throw lastEx;
 
             removeOrphanedProperties(con);
             removeOrphanedGroups(con);
             StructureLoader.reload(con);
             htracker.track(assignment.getAssignedType(),
                     disableAssignment ? "history.assignment.remove" : "history.assignment.disable",
                     assignment.getXPath(), assignmentId, removeSubAssignments, removeDerivedAssignments);
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxRemoveException(LOG, e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxRemoveException(e);
         } finally {
             Database.closeObjects(TypeEngineBean.class, con, ps);
         }
 
     }
 
     /**
      * Find all (directly) derived assignments and flag them as 'regular' assignments and set them as new base
      *
      * @param con        an open and valid connection
      * @param assignment the assignment to 'break'
      * @param sql        query builder
      * @return used ps
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      * @throws java.sql.SQLException       on errors
      */
     private PreparedStatement breakAssignmentInheritance(Connection con, FxAssignment assignment,
                                                          StringBuilder sql) throws SQLException, FxNotFoundException, FxInvalidParameterException {
         sql.append("UPDATE ").append(TBL_STRUCT_ASSIGNMENTS).append(" SET BASE=? WHERE BASE=?"); // AND TYPEDEF=?");
         PreparedStatement ps = con.prepareStatement(sql.toString());
         ps.setNull(1, Types.NUMERIC);
         ps.setLong(2, assignment.getId());
         int count = 0;
         //'toplevel' fix
 //        for(FxType types: assignment.getAssignedType().getDerivedTypes() ) {
 //            ps.setLong(3, types.getId());
         count += ps.executeUpdate();
 //        }
         LOG.info("Updated " + count + " assignments to become the new base assignment");
         /* sql.setLength(0);
         //now fix 'deeper' inherited assignments
         for(FxType types: assignment.getAssignedType().getDerivedTypes() ) {
             for( FxType subderived: types.getDerivedTypes())
                 _fixSubInheritance(ps, subderived, types.getAssignment(assignment.getXPath()).getId(), assignment.getId());
         }*/
         ps.close();
         sql.setLength(0);
         return ps;
     }
 
     /*private void _fixSubInheritance(PreparedStatement ps, FxType type, long newBase, long assignmentId) throws SQLException, FxInvalidParameterException, FxNotFoundException {
         ps.setLong(1, newBase);
         ps.setLong(2, assignmentId);
         ps.setLong(3, type.getId());
         ps.executeUpdate();
         for( FxType derived: type.getDerivedTypes())
             _fixSubInheritance(ps, derived, newBase, assignmentId);
     }*/
 
     /**
      * Recursively gather all sub assignments of the requested group assignment and add it to the given list
      *
      * @param affectedAssignments list where all sub assignments and the group itself are being put
      * @param ga                  the group assignment to start at
      */
     private void _addSubAssignments(List<FxAssignment> affectedAssignments, FxGroupAssignment ga) {
         affectedAssignments.addAll(ga.getAssignedProperties());
         for (FxGroupAssignment subga : ga.getAssignedGroups()) {
             affectedAssignments.add(subga);
             _addSubAssignments(affectedAssignments, subga);
         }
     }
 
     /**
      * Remove all properties that are no longer referenced
      *
      * @param con a valid connection
      * @throws SQLException on errors
      */
     protected static void removeOrphanedProperties(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_PROPERTIES + ML + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             stmt.executeUpdate("DELETE FROM " + TBL_PROPERTY_OPTIONS + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             int removed = stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_PROPERTIES + " WHERE ID NOT IN(SELECT DISTINCT APROPERTY FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE APROPERTY IS NOT NULL)");
             if (removed > 0)
                 LOG.info(removed + " orphaned properties removed.");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     /**
      * Remove all groups that are no longer referenced
      *
      * @param con a valid connection
      * @throws SQLException on errors
      */
     protected static void removeOrphanedGroups(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_GROUPS + ML + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             stmt.executeUpdate("DELETE FROM " + TBL_GROUP_OPTIONS + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             int removed = stmt.executeUpdate("DELETE FROM " + TBL_STRUCT_GROUPS + " WHERE ID NOT IN(SELECT DISTINCT AGROUP FROM " + TBL_STRUCT_ASSIGNMENTS + " WHERE AGROUP IS NOT NULL)");
             if (removed > 0)
                 LOG.info(removed + " orphaned groups removed.");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxPropertyEdit property) throws FxApplicationException {
         long returnId = property.getId();
         boolean reload;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             reload = updateProperty(con, null, null, property);
             if (reload)
                 StructureLoader.reload(con);
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, null);
         }
         return returnId;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public long save(FxGroupEdit group) throws FxApplicationException {
         long returnId = group.getId();
         boolean reload;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             reload = updateGroup(con, null, null, group);
             if (reload)
                 StructureLoader.reload(con);
         } catch (SQLException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCacheException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e, "ex.cache", e.getMessage());
         } catch (FxLoadException e) {
             ctx.setRollbackOnly();
             throw new FxCreateException(e);
         } finally {
             Database.closeObjects(AssignmentEngineBean.class, con, null);
         }
         return returnId;
     }
 
 
     /**
      * Get the number of content instances for a given property,
      *
      * @param con        a valid and open connection
      * @param propertyId id of the requested assignment
      * @return number of content instances using the assignment
      * @throws SQLException on errors
      */
     private long getInstanceCount(Connection con, long propertyId) throws SQLException {
         PreparedStatement ps = null;
         long count = 0;
         try {
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE TPROP=?");
             ps.setLong(1, propertyId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
         } finally {
             if (ps != null)
                 ps.close();
         }
         return count;
     }
 
     /**
      * {@inheritDoc}
      */
     public long getAssignmentInstanceCount(long assignmentId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         long count = 0;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE ASSIGN=?");
             ps.setLong(1, assignmentId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
             ps.close();
         }
         catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
         finally {
             if (con != null)
                 Database.closeObjects(AssignmentEngineBean.class, con, ps);
         }
         return count;
     }
 
     /**
      * Get minimum or maximum multiplicity of properties in content instances for a given property
      *
      * @param con        an open and valid Connection
      * @param propertyId requested property
      * @param minimum    true for minimum, false for maximum
      * @return minimum or maximum multiplicity of properties of instances
      * @throws SQLException on errors
      */
     private long getInstanceMultiplicity(Connection con, long propertyId, boolean minimum) throws SQLException {
         PreparedStatement ps = null;
         long count = 0;
         try {
             String function = "MIN(XINDEX)";
             if (!minimum)
                 function = "MAX(XINDEX)";
 
             ps = con.prepareStatement("SELECT " + function + " FROM " + TBL_CONTENT_DATA + " WHERE TPROP=?");
             ps.setLong(1, propertyId);
             ResultSet rs = ps.executeQuery();
             rs.next();
             count = rs.getLong(1);
         } finally {
             if (ps != null)
                 ps.close();
         }
         return count;
     }
 }
 
