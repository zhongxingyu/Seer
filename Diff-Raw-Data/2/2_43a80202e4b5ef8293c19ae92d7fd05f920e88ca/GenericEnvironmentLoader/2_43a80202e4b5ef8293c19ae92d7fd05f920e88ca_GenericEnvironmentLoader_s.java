 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
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
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.Database;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.conversion.ConversionEngine;
 import com.flexive.core.storage.EnvironmentLoader;
 import com.flexive.core.structure.FxEnvironmentImpl;
 import com.flexive.core.structure.FxPreloadGroupAssignment;
 import com.flexive.core.structure.FxPreloadType;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxLoadException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.scripting.FxScriptMappingEntry;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.Route;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.*;
 import java.util.*;
 
 /**
  * generic sql environment loader implementation
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericEnvironmentLoader implements EnvironmentLoader {
 
     protected static final Log LOG = LogFactory.getLog(GenericEnvironmentLoader.class);
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> loadACLs(Connection con) throws FxLoadException {
         Statement stmt = null;
         String curSql;
         ArrayList<ACL> result = new ArrayList<ACL>(250);
         try {
             final Map<Long, FxString[]> labels = Database.loadFxStrings(con, TBL_ACLS, "LABEL");
             //                            1      2          3             4                5          6                 7
             curSql = "SELECT DISTINCT acl.ID, acl.NAME, acl.CAT_TYPE, acl.DESCRIPTION, acl.COLOR, acl.MANDATOR, mand.NAME, " +
                     //    8               9               10               11
                     " acl.CREATED_BY, acl.CREATED_AT, acl.MODIFIED_BY, acl.MODIFIED_AT FROM " +
                     TBL_ACLS + " acl, " + TBL_MANDATORS + " mand WHERE mand.ID=acl.MANDATOR order by acl.NAME";
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             while (rs != null && rs.next()) {
                 final long id = rs.getLong(1);
                 result.add(new ACL(id, rs.getString(2),
                         getTranslation(labels, id, 0),
                         rs.getInt(6), rs.getString(7), rs.getString(4), rs.getString(5), ACLCategory.getById(rs.getInt(3)),
                         LifeCycleInfoImpl.load(rs, 8, 9, 10, 11)));
             }
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Failed to load all ACLs: " + exc.getMessage(), exc);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public Mandator[] loadMandators(Connection con) throws FxLoadException {
 
         PreparedStatement ps = null;
         try {
             // Load all mandators within the system
             ps = con.prepareStatement("SELECT ID,NAME,METADATA,IS_ACTIVE,CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT FROM " + TBL_MANDATORS + " order by upper(NAME)");
             ResultSet rs = ps.executeQuery();
             ArrayList<Mandator> result = new ArrayList<Mandator>(20);
             while (rs != null && rs.next()) {
                 int metaDataId = rs.getInt(3);
                 if (rs.wasNull()) {
                     metaDataId = -1;
                 }
                 result.add(new Mandator(rs.getInt(1), rs.getString(2), metaDataId,
                         rs.getBoolean(4), LifeCycleInfoImpl.load(rs, 5, 6, 7, 8)));
             }
             // return the result
             return result.toArray(new Mandator[result.size()]);
         } catch (SQLException se) {
             FxLoadException le = new FxLoadException(se.getMessage(), se);
             LOG.error(le);
             throw le;
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxDataType> loadDataTypes(Connection con) throws FxLoadException {
         Statement stmt = null;
         ArrayList<FxDataType> alRet = new ArrayList<FxDataType>(20);
         try {
             String sql = "SELECT d.TYPECODE, d.NAME, t.LANG, t.DESCRIPTION FROM " + TBL_STRUCT_DATATYPES + " d, " +
                     TBL_STRUCT_DATATYPES + ML + " t WHERE t.ID=d.TYPECODE ORDER BY d.TYPECODE, t.LANG ASC";
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             FxDataType dtCurr = null;
             Map<Long, String> hmDesc = new HashMap<Long, String>(5);
             String name = null;
             while (rs != null && rs.next()) {
                 if (dtCurr != null && rs.getLong(1) != dtCurr.getId()) {
                     dtCurr.initialize(name, new FxString(FxLanguage.DEFAULT_ID, hmDesc));
                     alRet.add(dtCurr);
                     hmDesc.clear();
                     dtCurr = null;
                 }
                 if (dtCurr == null)
                     for (FxDataType dt : FxDataType.values()) {
                         if (dt.getId() == rs.getInt(1)) {
                             dtCurr = dt;
                             break;
                         }
                     }
                 if (dtCurr == null)
                     throw new FxLoadException(LOG, "ex.structure.dataType.unknownId", rs.getInt(1));
                 hmDesc.put(rs.getLong(3), rs.getString(4));
                 name = rs.getString(2);
             }
             if (dtCurr != null) {
                 dtCurr.initialize(name, new FxString(FxLanguage.DEFAULT_ID, hmDesc));
                 alRet.add(dtCurr);
             }
             return alRet;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroup> loadGroups(Connection con) throws FxLoadException {
         Statement stmt = null;
         ArrayList<FxGroup> alRet = new ArrayList<FxGroup>(50);
         try {
             final Map<Long, List<FxStructureOption>> groupOptions = loadAllGroupOptions(con);
             //final List<FxStructureOption> emptyGroupOptions = new ArrayList<FxStructureOption>(0);
             //                     1     2       3             4             5                  6       7              8
             final String sql = "SELECT g.ID, g.NAME, g.DEFMINMULT, g.DEFMAXMULT, g.MAYOVERRIDEMULT, t.LANG, t.DESCRIPTION, t.HINT FROM " +
                     TBL_STRUCT_GROUPS + " g, " + TBL_STRUCT_GROUPS + ML + " t WHERE t.ID=g.ID ORDER BY g.ID, t.LANG ASC";
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             Map<Long, String> hmDesc = new HashMap<Long, String>(5);
             Map<Long, String> hmHint = new HashMap<Long, String>(5);
             String name = null;
             long id = -1;
             int minMult = -1;
             int maxMult = -1;
             boolean mayOverride = false;
             while (rs != null && rs.next()) {
                 if (name != null && rs.getLong(1) != id) {
                     alRet.add(new FxGroup(id, name, new FxString(FxLanguage.DEFAULT_ID, hmDesc),
                             new FxString(FxLanguage.DEFAULT_ID, hmHint), mayOverride,
                             new FxMultiplicity(minMult, maxMult), FxSharedUtils.get(groupOptions, id, new ArrayList<FxStructureOption>(0))));
                     hmDesc.clear();
                 }
 
                 if (hmDesc.size() == 0) {
                     id = rs.getLong(1);
                     name = rs.getString(2);
                     minMult = rs.getInt(3);
                     maxMult = rs.getInt(4);
                     mayOverride = rs.getBoolean(5);
                 }
                 hmDesc.put(rs.getLong(6), rs.getString(7));
                 hmHint.put(rs.getLong(6), rs.getString(8));
             }
             if (hmDesc.size() > 0) {
                 alRet.add(new FxGroup(id, name, new FxString(FxLanguage.DEFAULT_ID, hmDesc),
                         new FxString(FxLanguage.DEFAULT_ID, hmHint), mayOverride,
                         new FxMultiplicity(minMult, maxMult), FxSharedUtils.get(groupOptions, id, new ArrayList<FxStructureOption>(0))));
             }
             return alRet;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxProperty> loadProperties(Connection con, FxEnvironment environment) throws FxLoadException, FxNotFoundException {
         Statement stmt = null;
         ArrayList<FxProperty> alRet = new ArrayList<FxProperty>(50);
         try {
             final Map<Long, List<FxStructureOption>> propertyOptions = loadAllPropertyOptions(con);
             //final List<FxStructureOption> emptyPropertyOptions = new ArrayList<FxStructureOption>(0);
 
             //                     1     2       3             4             5                  6       7
             String sql = "SELECT p.ID, p.NAME, p.DEFMINMULT, p.DEFMAXMULT, p.MAYOVERRIDEMULT, t.LANG, t.DESCRIPTION, " +
                     // 8      9                 10          11
                     "p.ACL, p.MAYOVERRIDEACL, p.DATATYPE, p.REFTYPE, " +
                     // 12                   13               14     15
                     "p.ISFULLTEXTINDEXED, p.DEFAULT_VALUE, t.HINT, p.SYSINTERNAL, " +
                     //16          17
                     "p.REFLIST, p.UNIQUEMODE FROM " +
                     TBL_STRUCT_PROPERTIES + " p, " + TBL_STRUCT_PROPERTIES + ML + " t WHERE t.ID=p.ID ORDER BY p.ID, t.LANG ASC";
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             Map<Long, String> hmDesc = new HashMap<Long, String>(5);
             Map<Long, String> hmHint = new HashMap<Long, String>(5);
             FxValue defaultValue = null;
             String name = null;
             long id = -1;
             int minMult = -1;
             int maxMult = -1;
             boolean mayOverrideMult = false;
             boolean mayOverrideACL = false;
             ACL acl = null;
             FxDataType dataType = null;
             boolean fulltextIndexed = false;
             long refTypeId = -1;
             long refListId = -1;
             boolean systemInternal = false;
             UniqueMode uniqueMode = UniqueMode.None;
             final XStream xStream = ConversionEngine.getXStream();
 
             while (rs != null && rs.next()) {
                 if (name != null && rs.getLong(1) != id) {
 //                    if( !name.startsWith("TEST"))
 //                        System.out.println("=======> Loaded: "+name);
                     alRet.add(new FxProperty(id, name, new FxString(FxLanguage.DEFAULT_ID, hmDesc),
                             new FxString(FxLanguage.DEFAULT_ID, hmHint), systemInternal, mayOverrideMult,
                             new FxMultiplicity(minMult, maxMult), mayOverrideACL, acl, dataType,
                             defaultValue,
                             fulltextIndexed, (refTypeId == -1 ? null : environment.getType(refTypeId)),
                             (refListId == -1 ? null : environment.getSelectList(refListId)), uniqueMode,
                             FxSharedUtils.get(propertyOptions, id, new ArrayList<FxStructureOption>(0))));
                     hmDesc.clear();
                 }
 
                 if (hmDesc.size() == 0) {
                     id = rs.getLong(1);
                     name = rs.getString(2);
                     minMult = rs.getInt(3);
                     maxMult = rs.getInt(4);
                     mayOverrideMult = rs.getBoolean(5);
                     acl = environment.getACL(rs.getInt(8));
                     mayOverrideACL = rs.getBoolean(9);
                     dataType = environment.getDataType(rs.getLong(10));
                     refTypeId = rs.getLong(11);
                     if (rs.wasNull())
                         refTypeId = -1;
                     refListId = rs.getLong(16);
                     if (rs.wasNull())
                         refListId = -1;
                     fulltextIndexed = rs.getBoolean(12);
                     systemInternal = rs.getBoolean(15);
                     uniqueMode = UniqueMode.getById(rs.getInt(17));
                     String _def = rs.getString(13);
                     defaultValue = null;
                     if (!StringUtils.isEmpty(_def) && CacheAdmin.isEnvironmentLoaded()) {
                         try {
                             defaultValue = (FxValue) xStream.fromXML(_def);
                             if( defaultValue != null && defaultValue.isEmpty() )
                                 defaultValue = null;
                             if( defaultValue != null ) {
                                 defaultValue.setXPath(name);
                             }
                         } catch (Exception e) {
                             defaultValue = null;
                             LOG.warn("Failed to unmarshall default value for propery " + name + ": " + e.getMessage(), e);
                         }
                     }
                 }
                 hmDesc.put(rs.getLong(6), rs.getString(7));
                 hmHint.put(rs.getLong(6), rs.getString(14));
             }
             if (hmDesc.size() > 0) {
 //                if( !name.startsWith("TEST"))
 //                        System.out.println("=======> Loaded: "+name);
                 alRet.add(new FxProperty(id, name, new FxString(FxLanguage.DEFAULT_ID, hmDesc),
                         new FxString(FxLanguage.DEFAULT_ID, hmHint), systemInternal, mayOverrideMult,
                         new FxMultiplicity(minMult, maxMult), mayOverrideACL, acl, dataType,
                         defaultValue,
                         fulltextIndexed, (refTypeId == -1 ? null : environment.getType(refTypeId)),
                         (refListId == -1 ? null : environment.getSelectList(refListId)), uniqueMode,
                         FxSharedUtils.get(propertyOptions, id, new ArrayList<FxStructureOption>(0))));
             }
             return alRet;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * Load all options for group assignments
      *
      * @param con an open and valid connection
      * @return options
      * @throws SQLException on errors
      */
     private Map<Long, List<FxStructureOption>> loadAllGroupAssignmentOptions(Connection con) throws SQLException {
         return loadAllOptions(con, "ASSID", "ASSID IS NOT NULL", TBL_GROUP_OPTIONS);
     }
 
     /**
      * Load all options for groups
      *
      * @param con an open and valid connection
      * @return options
      * @throws SQLException on errors
      */
     private Map<Long, List<FxStructureOption>> loadAllGroupOptions(Connection con) throws SQLException {
         return loadAllOptions(con, "ID", "ASSID IS NULL", TBL_GROUP_OPTIONS);
     }
 
     private Map<Long, List<FxStructureOption>> loadAllPropertyAssignmentOptions(Connection con) throws SQLException {
         return loadAllOptions(con, "ASSID", "ASSID IS NOT NULL", TBL_PROPERTY_OPTIONS);
     }
 
     private Map<Long, List<FxStructureOption>> loadAllPropertyOptions(Connection con) throws SQLException {
         return loadAllOptions(con, "ID", "ASSID IS NULL", TBL_PROPERTY_OPTIONS);
     }
 
     private Map<Long, List<FxStructureOption>> loadAllOptions(Connection con, String idColumn, String whereClause, String table) throws SQLException {
         Statement stmt = null;
         Map<Long, List<FxStructureOption>> result = new HashMap<Long, List<FxStructureOption>>();
         try {
             stmt = con.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT " + idColumn + ",OPTKEY,MAYOVERRIDE,OPTVALUE FROM "
                     + table + " WHERE " + whereClause);
             while (rs.next()) {
                 final long id = rs.getLong(1);
                 if (!result.containsKey(id)) {
                     result.put(id, new ArrayList<FxStructureOption>());
                 }
                 FxStructureOption.setOption(result.get(id), rs.getString(2), rs.getBoolean(3), rs.getString(4));
             }
             return result;
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> loadTypes(Connection con, FxEnvironment environment) throws FxLoadException {
         Statement stmt = null;
         PreparedStatement ps = null;
         String curSql;
         ArrayList<FxType> result = new ArrayList<FxType>(20);
         try {
             //                                 1         2       3        4
             ps = con.prepareStatement("SELECT TYPESRC, TYPEDST, MAXSRC, MAXDST FROM " + TBL_STRUCT_TYPERELATIONS + " WHERE TYPEDEF=?");
             //               1   2     3       4             5         6
             curSql = "SELECT ID, NAME, PARENT, STORAGE_MODE, CATEGORY, TYPE_MODE, " +
                     //7         8           9              10            11           12
                     "LANG_MODE, TYPE_STATE, SECURITY_MODE, TRACKHISTORY, HISTORY_AGE, MAX_VERSIONS," +
                     //13               14                15          16          17           18           19   20        21
                     "REL_TOTAL_MAXSRC, REL_TOTAL_MAXDST, CREATED_BY, CREATED_AT, MODIFIED_BY, MODIFIED_AT, ACL, WORKFLOW, ICON_REF" +
                     " FROM " + TBL_STRUCT_TYPES + " ORDER BY NAME";
 
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             ResultSet rsRelations;
             final Map<Long, FxString[]> labels = Database.loadFxStrings(con, TBL_STRUCT_TYPES, "description");
             while (rs != null && rs.next()) {
                 try {
                     final long id = rs.getLong(1);
                     ps.setLong(1, id);
                     ArrayList<FxTypeRelation> alRelations = new ArrayList<FxTypeRelation>(10);
                     rsRelations = ps.executeQuery();
                     while (rsRelations != null && rsRelations.next())
                         alRelations.add(new FxTypeRelation(new FxPreloadType(rsRelations.getLong(1)), new FxPreloadType(rsRelations.getLong(2)),
                                 rsRelations.getInt(3), rsRelations.getInt(4)));
                     long parentId = rs.getLong(3);
                     FxType parentType = rs.wasNull() ? null : new FxPreloadType(parentId);
                     FxType _type = new FxType(id, environment.getACL(rs.getInt(19)),
                             environment.getWorkflow(rs.getInt(20)), rs.getString(2),
                             getTranslation(labels, id, 0),
                             parentType, TypeStorageMode.getById(rs.getInt(4)),
                             TypeCategory.getById(rs.getInt(5)), TypeMode.getById(rs.getInt(6)),
                             LanguageMode.getById(rs.getInt(7)), TypeState.getById(rs.getInt(8)), rs.getByte(9),
                             rs.getBoolean(10), rs.getLong(11), rs.getLong(12), rs.getInt(13), rs.getInt(14),
                             LifeCycleInfoImpl.load(rs, 15, 16, 17, 18), new ArrayList<FxType>(5), alRelations);
                     long iconId = rs.getLong(21);
                     if( !rs.wasNull())
                         _type.getIcon().setValue(new ReferencedContent(iconId));
                     result.add(_type);
                 } catch (FxNotFoundException e) {
                     throw new FxLoadException(LOG, e);
                 }
             }
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Failed to load all FxTypes: " + exc.getMessage(), exc);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //ignore
             }
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxAssignment> loadAssignments(Connection con, FxEnvironment environment) throws FxLoadException {
         Statement stmt = null;
         String curSql;
         ArrayList<FxAssignment> result = new ArrayList<FxAssignment>(250);
         try {
             final Map<Long, FxString[]> translations = Database.loadFxStrings(con, TBL_STRUCT_ASSIGNMENTS, "DESCRIPTION", "HINT");
             final Map<Long, List<FxStructureOption>> propertyAssignmentOptions = loadAllPropertyAssignmentOptions(con);
             final Map<Long, List<FxStructureOption>> groupAssignmentOptions = loadAllGroupAssignmentOptions(con);
             //final List<FxStructureOption> emptyOptions = new ArrayList<FxStructureOption>(0);
 
             //               1   2      3        4        5        6        7    8      9
             curSql = "SELECT ID, ATYPE, ENABLED, TYPEDEF, MINMULT, MAXMULT, POS, XPATH, XALIAS, " +
                     //10          11      12         13   14    15       16           17         18       19
                     "PARENTGROUP, AGROUP, APROPERTY, ACL, BASE, DEFLANG, SYSINTERNAL, GROUPMODE, DEFMULT, DEFAULT_VALUE FROM " +
                     TBL_STRUCT_ASSIGNMENTS + " ORDER BY TYPEDEF, PARENTGROUP, POS";
 
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(curSql);
             final XStream xStream = ConversionEngine.getXStream();
             while (rs != null && rs.next()) {
                 final long id = rs.getLong(1);
                 switch (rs.getInt(2)) {   //ATYPE
                     case FxAssignment.TYPE_GROUP:
                         if (id == FxAssignment.NO_PARENT)
                             break;
                         FxGroupAssignment ga = new FxGroupAssignment(rs.getLong(1), rs.getBoolean(3), environment.getType(rs.getLong(4)),
                                 rs.getString(9), rs.getString(8), rs.getInt(7),
                                 new FxMultiplicity(rs.getInt(5), rs.getInt(6)), rs.getInt(18),
                                 new FxPreloadGroupAssignment(rs.getLong(10)),
                                 rs.getLong(14), getTranslation(translations, id, 0),
                                 getTranslation(translations, id, 1),
                                 environment.getGroup(rs.getLong(11)), GroupMode.getById(rs.getInt(17)),
                                 FxSharedUtils.get(groupAssignmentOptions, rs.getLong(1), new ArrayList<FxStructureOption>(0)));
                         if (rs.getBoolean(16))
                             ga._setSystemInternal();
                         result.add(ga);
                         break;
                     case FxAssignment.TYPE_PROPERTY:
                         FxValue defaultValue = null;
                         String _def = rs.getString(19);
                         if (!StringUtils.isEmpty(_def) && CacheAdmin.isEnvironmentLoaded()) {
                             try {
                                 defaultValue = (FxValue) xStream.fromXML(_def);
                                 if( defaultValue != null && defaultValue.isEmpty() )
                                     defaultValue = null;
                             } catch (Throwable e) {
                                 defaultValue = null;
                                 LOG.warn("Failed to unmarshall default value for assignment " + rs.getString(8) + ": " + e.getMessage(), e);
                             }
                         }
                         FxPropertyAssignment pa = new FxPropertyAssignment(rs.getLong(1), rs.getBoolean(3), environment.getType(rs.getLong(4)),
                                 rs.getString(9), rs.getString(8), rs.getInt(7),
                                 new FxMultiplicity(rs.getInt(5), rs.getInt(6)), rs.getInt(18),
                                 new FxPreloadGroupAssignment(rs.getLong(10)),
                                 rs.getLong(14),
                                 getTranslation(translations, id, 0),
                                 getTranslation(translations, id, 1),
                                 defaultValue,
                                 environment.getProperty(rs.getLong(12)),
                                 environment.getACL(rs.getInt(13)), rs.getInt(15),
                                 FxSharedUtils.get(propertyAssignmentOptions, rs.getLong(1), new ArrayList<FxStructureOption>(0)));
                         if (rs.getBoolean(16))
                             pa._setSystemInternal();
                         result.add(pa);
                         break;
                     default:
                         LOG.error("Invalid assignment type " + rs.getInt(2) + " for assignment #" + rs.getLong(1));
                 }
             }
             for (FxAssignment as : result)
                 as.resolvePreloadDependencies(result);
             for (FxAssignment as : result)
                 as.resolveParentDependencies(result);
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Failed to load all FxAssignments: " + exc.getMessage(), exc);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Workflow> loadWorkflows(Connection con, FxEnvironment environment) throws FxLoadException {
         Statement stmt = null;
         final String sql = "SELECT ID, NAME, DESCRIPTION FROM " + TBL_WORKFLOW + " ORDER BY ID";
         try {
             // Create the new workflow instance
             stmt = con.createStatement();
             // Read all defined workflows
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             ArrayList<Workflow> tmp = new ArrayList<Workflow>(10);
             while (rs != null && rs.next()) {
                 int id = rs.getInt(1);
                 String name = rs.getString(2);
                 String description = rs.getString(3);
                 ArrayList<Step> wfSteps = new ArrayList<Step>(5);
                 for (Step step : environment.getSteps()) {
                     if (step.getWorkflowId() == id) {
                         wfSteps.add(step);
                     }
                 }
                 tmp.add(new Workflow(id, name, description, wfSteps, loadRoutes(con, id)));
             }
             return tmp;
         } catch (SQLException exc) {
             String sErr = "Unable to retrieve workflows";
             LOG.error(sErr + ", sql=" + sql);
             throw new FxLoadException(sErr, exc);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<StepDefinition> loadStepDefinitions(Connection con) throws FxLoadException {
         Statement stmt = null;
         try {
             // Read all stepDefinitions from the database
             stmt = con.createStatement();
             //                                             1  2    3
             final ResultSet rs = stmt.executeQuery("SELECT ID,NAME,UNIQUE_TARGET FROM " + TBL_STEPDEFINITION + " ORDER BY ID");
             ArrayList<StepDefinition> tmp = new ArrayList<StepDefinition>(10);
             final Map<Long, FxString[]> labels = Database.loadFxStrings(con, TBL_STEPDEFINITION, "name");
 
             // Build the result array set
             while (rs != null && rs.next()) {
                 long id = rs.getLong(1);
                 String name = rs.getString(2);
                 int uniqueTargetId = rs.getInt(3);
                 if (rs.wasNull()) {
                     uniqueTargetId = -1;
                 }
                 StepDefinition aStepDef = new StepDefinition(id, getTranslation(labels, id, 0), name, uniqueTargetId);
                 tmp.add(aStepDef);
             }
             return tmp;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Unable to read steps definitions", exc);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> loadSteps(Connection con) throws FxLoadException {
         Statement stmt = null;
         //                                      1      2               3                4
         final String sql = "SELECT DISTINCT stp.ID, stp.WORKFLOW, stp.STEPDEF,stp.ACL " +
                 "FROM " + TBL_STEP + " stp ORDER BY stp.ID";
         try {
             // Load all steps in the database
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             ArrayList<Step> steps = new ArrayList<Step>(30);
             while (rs != null && rs.next())
                 steps.add(new Step(rs.getLong(1), rs.getLong(3), rs.getLong(2), rs.getLong(4)));
 
             return steps;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Unable to read the steps from the database. " +
                     "Error=" + exc.getMessage() + ", sql=" + sql, exc);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Route> loadRoutes(Connection con, int workflowId) throws FxLoadException {
         //                             1     2               3             4
         final String sql = "SELECT ro.ID,ro.FROM_STEP,ro.TO_STEP,ro.USERGROUP " +
                 "FROM " + TBL_ROUTES + " ro, " + TBL_STEP + " stp " +
                 "WHERE ro.TO_STEP=stp.ID AND stp.WORKFLOW=" + workflowId + " " +
                 "ORDER BY ro.USERGROUP ASC";
 
         if (LOG.isDebugEnabled()) LOG.debug("getRoute(" + workflowId + ")=" + sql);
 
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             ArrayList<Route> routes = new ArrayList<Route>(50);
 
             // Process result set
             while (rs != null && rs.next()) {
                 long routeId = rs.getLong(1);
                 long fromId = rs.getLong(2);
                 long toId = rs.getLong(3);
                 long groupId = rs.getLong(4);
                 Route route = new Route(routeId, groupId, fromId, toId);
                 routes.add(route);
             }
 
             return routes;
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, "Unable to load routes for workflow [" + workflowId + "], msg=" +
                     exc.getMessage() + ", sql=" + sql, exc);
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptInfo> loadScripts(Connection con) throws FxLoadException, FxNotFoundException, FxInvalidParameterException {
         PreparedStatement ps = null;
         String sql;
         List<FxScriptInfo> scripts = new ArrayList<FxScriptInfo>(10);
         try {
             //            1  2     3     4     5       6
             sql = "SELECT ID,SNAME,SDESC,SDATA,STYPE,ACTIVE FROM " + TBL_SCRIPTS + " ORDER BY ID";
             ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next())
                 scripts.add(new FxScriptInfo(rs.getLong(1), FxScriptEvent.getById(rs.getLong(5)), rs.getString(2),
                         rs.getString(3), rs.getString(4), rs.getBoolean(6)));
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.scripting.load.failed", -1, exc.getMessage());
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, ps);
         }
         return scripts;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptMapping> loadScriptMapping(Connection con, FxEnvironmentImpl environment) throws FxLoadException {
         List<FxScriptMapping> mapping = new ArrayList<FxScriptMapping>(20);
         List<FxScriptMappingEntry> e_ass;
         List<FxScriptMappingEntry> e_types;
         PreparedStatement ps_a = null, ps_t = null;
         String sql;
         try {
             //            1          2             3      4
             sql = "SELECT ASSIGNMENT,DERIVED_USAGE,ACTIVE,STYPE FROM " + TBL_SCRIPT_MAPPING_ASSIGN + " WHERE SCRIPT=? ORDER BY ASSIGNMENT";
             ps_a = con.prepareStatement(sql);
             sql = "SELECT TYPEDEF,DERIVED_USAGE,ACTIVE,STYPE FROM " + TBL_SCRIPT_MAPPING_TYPES + " WHERE SCRIPT=? ORDER BY TYPEDEF";
             ps_t = con.prepareStatement(sql);
             ResultSet rs;
             for (FxScriptInfo si : environment.getScripts()) {
                 ps_a.setLong(1, si.getId());
                 ps_t.setLong(1, si.getId());
                 rs = ps_a.executeQuery();
                 e_ass = new ArrayList<FxScriptMappingEntry>(20);
                 e_types = new ArrayList<FxScriptMappingEntry>(20);
                 while (rs != null && rs.next()) {
                     long[] derived;
                     if (!rs.getBoolean(2))
                         derived = new long[0];
                     else {
                         List<FxAssignment> ass = environment.getDerivedAssignments(rs.getLong(1));
                         derived = new long[ass.size()];
                         for (int i = 0; i < ass.size(); i++)
                             derived[i] = ass.get(i).getId();
                     }
                     e_ass.add(new FxScriptMappingEntry(FxScriptEvent.getById(rs.getLong(4)), si.getId(), rs.getBoolean(3), rs.getBoolean(2), rs.getLong(1), derived));
                 }
                 rs = ps_t.executeQuery();
                 while (rs != null && rs.next()) {
                     long[] derived;
                     if (!rs.getBoolean(2))
                         derived = new long[0];
                     else {
                         // determine derived types "manually" as resolveReferences()
                         // which sets the derived types has not been called yet
                         List<Long> derivedList = new ArrayList<Long>(5);
                         for (FxType t : environment.getTypes()) {
                             if (t.isDerived() && t.getParent().getId() == rs.getLong(1))
                                 derivedList.add(t.getId());
                         }
                         derived = new long[derivedList.size()];
                         int i=0;
                         for (long l : derivedList)
                             derived[i++]=l;
                     }
                     e_types.add(new FxScriptMappingEntry(FxScriptEvent.getById(rs.getLong(4)), si.getId(), rs.getBoolean(3), rs.getBoolean(2), rs.getLong(1), derived));
                 }
                 mapping.add(new FxScriptMapping(si.getId(), e_types, e_ass));
             }
 
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.scripting.mapping.load.failed", exc.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             try {
                 if (ps_t != null)
                     ps_t.close();
             } catch (SQLException e) {
                 //ignore
             }
             Database.closeObjects(GenericEnvironmentLoader.class, null, ps_a);
         }
         return mapping;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxSelectList> loadSelectLists(Connection con, FxEnvironmentImpl environment) throws FxLoadException {
         PreparedStatement ps = null;
         String sql;
         List<FxSelectList> lists = new ArrayList<FxSelectList>(10);
         try {
             final Map<Long, FxString[]> translations = Database.loadFxStrings(con, TBL_SELECTLIST, "LABEL", "DESCRIPTION");
             final Map<Long, FxString[]> itemTranslations = Database.loadFxStrings(con, TBL_SELECTLIST_ITEM, "LABEL");
 
             //            1  2        3    4                 5               6            7
             sql = "SELECT ID,PARENTID,NAME,ALLOW_ITEM_CREATE,ACL_CREATE_ITEM,ACL_ITEM_NEW,DEFAULT_ITEM FROM " +
                     TBL_SELECTLIST + " ORDER BY NAME";
             ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 final long id = rs.getLong(1);
                 long parent = rs.getLong(2);
                 if (rs.wasNull())
                     parent = -1;
                 lists.add(new FxSelectList(id, parent, rs.getString(3),
                         getTranslation(translations, id, 0),
                         getTranslation(translations, id, 1),
                        rs.getBoolean(5), environment.getACL(rs.getLong(5)), environment.getACL(rs.getLong(6)),
                         rs.getLong(7)));
             }
             ps.close();
             //            1  2    3   4        5    6     7          8          9           10          11      12       13
             sql = "SELECT ID,NAME,ACL,PARENTID,DATA,COLOR,CREATED_BY,CREATED_AT,MODIFIED_BY,MODIFIED_AT,DBIN_ID,DBIN_VER,DBIN_QUALITY FROM " +
                     TBL_SELECTLIST_ITEM + " WHERE LISTID=? ORDER BY ID";
             ps = con.prepareStatement(sql);
             for (FxSelectList list : lists) {
                 ps.setLong(1, list.getId());
                 rs = ps.executeQuery();
                 while (rs != null && rs.next()) {
                     final long id = rs.getLong(1);
                     long parent = rs.getLong(4);
                     if (rs.wasNull())
                         parent = -1;
                     new FxSelectListItem(id, rs.getString(2), environment.getACL(rs.getLong(3)), list, parent,
                             getTranslation(itemTranslations, id, 0),
                             rs.getString(5), rs.getString(6), rs.getLong(11), rs.getInt(12), rs.getInt(13),
                             LifeCycleInfoImpl.load(rs, 7, 8, 9, 10));
                 }
             }
         } catch (SQLException exc) {
             throw new FxLoadException(LOG, exc, "ex.structure.list.load.failed", exc.getMessage());
         } finally {
             Database.closeObjects(GenericEnvironmentLoader.class, null, ps);
         }
         return lists;
     }
 
     private FxString getTranslation(final Map<Long, FxString[]> translations, final long id, final int index) {
         final FxString[] values = translations.get(id);
         if (values == null) {
             return new FxString("").setEmpty();
         }
         return values[index];
     }
 }
