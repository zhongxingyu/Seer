 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
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
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.DatabaseConst;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.conversion.ConversionEngine;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.extractor.ExtractedData;
 import com.flexive.extractor.HtmlExtractor;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.HistoryTrackerEngine;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.interfaces.SequencerEngine;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptResult;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.stream.BinaryUploadPayload;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 import com.flexive.stream.ServerLocation;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.tidy.Tidy;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 
 /**
  * Generic implementation of hierarchical content handling.
  * Concrete implementation have to derive from this class and
  * provide a singleton hook for the Database class (static getInstance() method)
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public abstract class GenericHierarchicalStorage implements ContentStorage {
 
     /**
      * {@inheritDoc}
      */
     public String getTableName(FxProperty prop) {
         if (prop.isSystemInternal())
             return DatabaseConst.TBL_CONTENT;
         return DatabaseConst.TBL_CONTENT_DATA;
     }
 
     /**
      * Helper to convert a var array of string to a 'real' string array
      *
      * @param data strings to put into an array
      * @return string array from parameters
      */
     protected static String[] array(String... data) {
         return data;
     }
 
 
     private static final transient Log LOG = LogFactory.getLog(GenericHierarchicalStorage.class);
 
     protected static final String CONTENT_MAIN_INSERT = "INSERT INTO " + TBL_CONTENT +
             // 1  2   3    4   5    6       7        8
             " (ID,VER,TDEF,ACL,STEP,MAX_VER,LIVE_VER,ISMAX_VER," +
             //9         10       11       12        13         14        15         16         17         18
             "ISLIVE_VER,ISACTIVE,MAINLANG,RELSRC_ID,RELSRC_VER,RELDST_ID,RELDST_VER,RELSRC_POS,RELDST_POS,CREATED_BY," +
             //19        20          21          22
             "CREATED_AT,MODIFIED_BY,MODIFIED_AT,MANDATOR,DBIN_ID,DBIN_VER,DBIN_QUALITY,DBIN_ACL)" +
             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,-1,1,1,1)";
 
     protected static final String CONTENT_MAIN_UPDATE = "UPDATE " + TBL_CONTENT + " SET " +
             //    1     2      3         4          5           6            7          8          9
             "TDEF=?,ACL=?,STEP=?,MAX_VER=?,LIVE_VER=?,ISMAX_VER=?,ISLIVE_VER=?,ISACTIVE=?,MAINLANG=?," +
             //         10           11          12           13           14           15            16            17
             "RELSRC_ID=?,RELSRC_VER=?,RELDST_ID=?,RELDST_VER=?,RELSRC_POS=?,RELDST_POS=?,MODIFIED_BY=?,MODIFIED_AT=? " +
             //        18        19
             "WHERE ID=? AND VER=?";
     protected static final String CONTENT_MAIN_BINARY_UPDATE = "UPDATE " + TBL_CONTENT + " SET " +
             //       1          2              3          4          5         6
             "DBIN_ID=?,DBIN_VER=?,DBIN_QUALITY=?,DBIN_ACL=? WHERE ID=? AND VER=?";
 
     //                                                        1  2   3    4   5    6       7        8         9
     protected static final String CONTENT_MAIN_LOAD = "SELECT ID,VER,TDEF,ACL,STEP,MAX_VER,LIVE_VER,ISMAX_VER,ISLIVE_VER," +
             //10      11       12        13         14        15         16         17         18         19
             "ISACTIVE,MAINLANG,RELSRC_ID,RELSRC_VER,RELDST_ID,RELDST_VER,RELSRC_POS,RELDST_POS,CREATED_BY,CREATED_AT," +
             //20         21          22       23      24
             "MODIFIED_BY,MODIFIED_AT,MANDATOR,DBIN_ID,DBIN_ACL FROM " + TBL_CONTENT;
 
     //                                                  1   2    3      4     5         6     7       8
     protected static final String CONTENT_DATA_LOAD = "SELECT POS,LANG,ASSIGN,XPATH,XPATHMULT,XMULT,ISGROUP,ISMLDEF," +
             //9     10     11    12    13   14      15        16      17     18    19      20
             "FDATE1,FDATE2,FBLOB,FCLOB,FINT,FBIGINT,FTEXT1024,FDOUBLE,FFLOAT,FBOOL,FSELECT,FREF FROM " + TBL_CONTENT_DATA +
             //         1         2
             " WHERE ID=? AND VER=? ORDER BY XDEPTH ASC, " +/*ISGROUP DESC, PARENTXPATH ASC,*/ "POS ASC";
 
     //                                                                                                       1         2
     protected static final String CONTENT_DATA_REMOVE_VERSION = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID=? AND VER=?";
     protected static final String CONTENT_DATA_FT_REMOVE_VERSION = "DELETE FROM " + TBL_CONTENT_DATA_FT + " WHERE ID=? AND VER=?";
 
     //security info main query
     protected static final String SECURITY_INFO_MAIN = "SELECT DISTINCT c.ACL, t.ACL, s.ACL, t.SECURITY_MODE, t.ID, c.DBIN_ID, c.DBIN_ACL, c.CREATED_BY, c.MANDATOR FROM " +
             TBL_CONTENT + " c, " + TBL_STRUCT_TYPES + " t, " + TBL_STEP + " s WHERE c.ID=? AND ";
     protected static final String SECURITY_INFO_WHERE = " AND t.ID=c.TDEF AND s.ID=c.STEP";
     protected static final String SECURITY_INFO_VER = SECURITY_INFO_MAIN + "c.VER=?" + SECURITY_INFO_WHERE;
     protected static final String SECURITY_INFO_MAXVER = SECURITY_INFO_MAIN + "c.VER=c.MAX_VER" + SECURITY_INFO_WHERE;
     protected static final String SECURITY_INFO_LIVEVER = SECURITY_INFO_MAIN + "c.VER=c.LIVE_VER" + SECURITY_INFO_WHERE;
 
     //calculate max_ver and live_ver for a content instance
     protected static final String CONTENT_VER_CALC = "SELECT MAX(VER) AS MAX_VER, COALESCE((SELECT s.VER FROM " +
             TBL_CONTENT + " s WHERE s.STEP=(SELECT w.ID FROM " + TBL_STEP + " w, " + TBL_STRUCT_TYPES + " t WHERE w.STEPDEF=" +
             StepDefinition.LIVE_STEP_ID + " AND w.WORKFLOW=t.WORKFLOW AND t.ID=c.TDEF) AND s.ID=c.ID),-1) AS LIVE_VER FROM " + TBL_CONTENT +
             " c WHERE c.ID=? GROUP BY c.ID";
     protected static final String CONTENT_VER_UPDATE_1 = "UPDATE " + TBL_CONTENT + " SET MAX_VER=?, LIVE_VER=? WHERE ID=?";
     protected static final String CONTENT_VER_UPDATE_2 = "UPDATE " + TBL_CONTENT + " SET ISMAX_VER=(MAX_VER=VER), ISLIVE_VER=(LIVE_VER=VER) WHERE ID=?";
     protected static final String CONTENT_VER_UPDATE_3 = "UPDATE " + TBL_CONTENT_DATA + " SET ISMAX_VER=(VER=?), ISLIVE_VER=(VER=?) WHERE ID=?";
 
     protected static final String CONTENT_STEP_DEPENDENCIES = "UPDATE " + TBL_CONTENT + " SET STEP=? WHERE STEP=? AND ID=? AND VER<>?";
 
 
     protected static final String CONTENT_REFERENCE_LIVE = "SELECT VER, ACL, STEP, TDEF, CREATED_BY FROM " + TBL_CONTENT + " WHERE ID=? AND ISLIVE_VER=TRUE";
     protected static final String CONTENT_REFERENCE_MAX = "SELECT VER, ACL, STEP, TDEF, CREATED_BY FROM " + TBL_CONTENT + " WHERE ID=? AND ISMAX_VER=TRUE";
     protected static final String CONTENT_REFERENCE_CAPTION = "SELECT FTEXT1024 FROM " + TBL_CONTENT_DATA + " WHERE ID=? AND VER=? AND TPROP=?";
 
     //getContentVersionInfo() statement
     protected static final String CONTENT_VER_INFO = "SELECT ID, VER, MAX_VER, LIVE_VER, CREATED_BY, CREATED_AT, MODIFIED_BY, MODIFIED_AT, STEP FROM " + TBL_CONTENT + " WHERE ID=?";
 
     //security info property acl query
     protected static final String SECURITY_INFO_PROP = "SELECT DISTINCT a.ACL FROM " + TBL_STRUCT_ASSIGNMENTS + " a, " +
             TBL_CONTENT_DATA + " d WHERE d.ID=? AND ";
     protected static final String SECURITY_INFO_PROP_WHERE = " AND a.ID=d.ASSIGN AND d.ISGROUP=FALSE";
     protected static final String SECURITY_INFO_PROP_VER = SECURITY_INFO_PROP + "d.VER=?" + SECURITY_INFO_PROP_WHERE;
     protected static final String SECURITY_INFO_PROP_MAXVER = SECURITY_INFO_PROP + "d.ISMAX_VER=1" + SECURITY_INFO_PROP_WHERE;
     protected static final String SECURITY_INFO_PROP_LIVEVER = SECURITY_INFO_PROP + "d.ISLIVE_VER=1" + SECURITY_INFO_PROP_WHERE;
 
 
     protected static final String CONTENT_MAIN_REMOVE = "DELETE FROM " + TBL_CONTENT + " WHERE ID=?";
     protected static final String CONTENT_DATA_REMOVE = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID=?";
     protected static final String CONTENT_DATA_FT_REMOVE = "DELETE FROM " + TBL_CONTENT_DATA_FT + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID IN (SELECT DISTINCT d.FBLOB FROM " + TBL_CONTENT_DATA + " d WHERE d.ID=?)";
     protected static final String SQL_WHERE_VER = " AND VER=?";
     protected static final String CONTENT_MAIN_REMOVE_VER = CONTENT_MAIN_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_DATA_REMOVE_VER = CONTENT_DATA_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_DATA_FT_REMOVE_VER = CONTENT_DATA_FT_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_BINARY_REMOVE_VER = CONTENT_BINARY_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_BINARY_TRANSIT_CLEANUP = "DELETE FROM " + TBL_BINARY_TRANSIT + " WHERE EXPIRE<?";
 
     protected static final String CONTENT_MAIN_REMOVE_TYPE = "DELETE FROM " + TBL_CONTENT + " WHERE TDEF=?";
     protected static final String CONTENT_DATA_REMOVE_TYPE = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
     protected static final String CONTENT_DATA_FT_REMOVE_TYPE = "DELETE FROM " + TBL_CONTENT_DATA_FT + " WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
     protected static final String CONTENT_BINARY_REMOVE_ID = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_TYPE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " d, " + TBL_CONTENT + " c WHERE d.ID=c.ID and c.TDEF=?";
 
     protected static final String CONTENT_TYPE_PK_RETRIEVE_VERSIONS = "SELECT DISTINCT ID,VER FROM " + TBL_CONTENT + " WHERE TDEF=? ORDER BY ID,VER";
     protected static final String CONTENT_TYPE_PK_RETRIEVE_IDS = "SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=? ORDER BY ID";
 
     //    select into xx () select  FBLOB1,FBLOB2,?,?,?
     protected static final String BINARY_TRANSIT_HEADER = "SELECT FBLOB FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                                     1  2   3             4(handle)5    6                  7                    8                    9       10         11    12
     protected static final String BINARY_TRANSIT = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1,PREV1_WIDTH,PREV1_HEIGHT,PREV2,PREV2_WIDTH,PREV2_HEIGHT,PREV3,PREV3_WIDTH,PREV3_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE) " +
             //      1 2 3       4 5 6       7             8 9 10 11
             "SELECT ?,?,?,FBLOB,?,?,?,UNIX_TIMESTAMP()*1000,?,PREVIEW_REF,?,?,?,?,PREV1,PREV1_WIDTH,PREV1_HEIGHT,PREV2,PREV2_WIDTH,PREV2_HEIGHT,PREV3,PREV3_WIDTH,PREV3_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                                                   1              2               3        4              5               6        7              8               9            10           11           12           13
     protected static final String BINARY_TRANSIT_PREVIEWS = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREV1=?, PREV1_WIDTH=?, PREV1_HEIGHT=?, PREV2=?, PREV2_WIDTH=?, PREV2_HEIGHT=?, PREV3=?, PREV3_WIDTH=?, PREV3_HEIGHT=?, PREV1SIZE=?, PREV2SIZE=?, PREV3SIZE=? WHERE BKEY=?";
     protected static final String BINARY_TRANSIT_PREVIEWS_REF = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREVIEW_REF=? WHERE BKEY=?";
     //                                                                                                                                                                          1         2             3
     protected static final String BINARY_DESC_LOAD = "SELECT NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,ISIMAGE,RESOLUTION,WIDTH,HEIGHT FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
 
     /**
      * {@inheritDoc}
      */
     public String getUppercaseColumn(FxProperty prop) {
         if (prop.isSystemInternal())
             return null;
         switch (prop.getDataType()) {
             case String1024:
                 return "UFTEXT1024";
             case Text:
                 return "UFCLOB";
             default:
                 return null;
         }
     }
 
     // propertyId -> columns
     protected static final HashMap<Long, String[]> mainColumnHash;
     // dataType -> columns
     protected static final HashMap<FxDataType, String[]> detailColumnHash;
 
     static {
         detailColumnHash = new HashMap<FxDataType, String[]>(20);
         detailColumnHash.put(FxDataType.Binary, array("FBLOB"));
         detailColumnHash.put(FxDataType.Boolean, array("FBOOL"));
         detailColumnHash.put(FxDataType.Date, array("FDATE1", "FDATE1_Y", "FDATE1_M", "FDATE1_D"));
         detailColumnHash.put(FxDataType.DateRange, array("FDATE1", "FDATE1_Y", "FDATE1_M", "FDATE1_D",
                 "FDATE2", "FDATE2_Y", "FDATE2_M", "FDATE2_D"));
         detailColumnHash.put(FxDataType.DateTime, array("FDATE1", "FDATE1_Y", "FDATE1_M", "FDATE1_D", "FDATE1_HH", "FDATE1_MM", "FDATE1_SS"));
         detailColumnHash.put(FxDataType.DateTimeRange, array("FDATE1", "FDATE1_Y", "FDATE1_M", "FDATE1_D", "FDATE1_HH", "FDATE1_MM", "FDATE1_SS",
                 "FDATE2", "FDATE2_Y", "FDATE2_M", "FDATE2_D", "FDATE2_HH", "FDATE2_MM", "FDATE2_SS"));
         detailColumnHash.put(FxDataType.Double, array("FDOUBLE"));
         detailColumnHash.put(FxDataType.Float, array("FFLOAT"));
         detailColumnHash.put(FxDataType.LargeNumber, array("FBIGINT"));
         detailColumnHash.put(FxDataType.Number, array("FINT"));
         detailColumnHash.put(FxDataType.Reference, array("FREF"));
         detailColumnHash.put(FxDataType.String1024, array("FTEXT1024"));
         detailColumnHash.put(FxDataType.Text, array("FCLOB"));
         detailColumnHash.put(FxDataType.HTML, array("FCLOB", "FBOOL", "UFCLOB"));
         detailColumnHash.put(FxDataType.SelectOne, array("FSELECT"));
         detailColumnHash.put(FxDataType.SelectMany, array("FSELECT", "FTEXT1024"/*comma separated list of selected id's*/));
 
         mainColumnHash = new HashMap<Long, String[]>(20);
         mainColumnHash.put(0L, array("ID"));
         mainColumnHash.put(1L, array("VER"));
         mainColumnHash.put(2L, array("TDEF"));
         mainColumnHash.put(3L, array("MANDATOR"));
         mainColumnHash.put(4L, array("ACL"));
         mainColumnHash.put(5L, array("STEP"));
         mainColumnHash.put(6L, array("MAX_VER"));
         mainColumnHash.put(7L, array("LIVE_VER"));
         mainColumnHash.put(8L, array("ISMAX_VER"));
         mainColumnHash.put(9L, array("ISLIVE_VER"));
         mainColumnHash.put(10L, array("ISACTIVE"));
         mainColumnHash.put(11L, array("MAINLANG"));
         mainColumnHash.put(12L, array("RELSRC"));
         mainColumnHash.put(13L, array("RELDST"));
         mainColumnHash.put(14L, array("RELPOS_SRC"));
         mainColumnHash.put(15L, array("RELPOS_DST"));
         mainColumnHash.put(16L, array("CREATED_BY"));
         mainColumnHash.put(17L, array("CREATED_AT"));
         mainColumnHash.put(18L, array("MODIFIED_BY"));
         mainColumnHash.put(19L, array("MODIFIED_AT"));
         mainColumnHash.put(20L, array("CAPTION"));
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getColumns(FxProperty prop) {
         if (prop.isSystemInternal())
             return mainColumnHash.get(prop.getId());
         return detailColumnHash.get(prop.getDataType());
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPK contentCreate(Connection con, FxEnvironment env, StringBuilder sql, long newId, FxContent content) throws FxCreateException, FxInvalidParameterException {
         content.getRootGroup().removeEmptyEntries();
         content.getRootGroup().compactPositions(true);
         content.checkValidity();
         FxPK pk = createMainEntry(con, newId, 1, content);
         try {
             if (sql == null)
                 sql = new StringBuilder(2000);
             createDetailEntries(con, env, sql, pk, content.isMaxVersion(), content.isLiveVersion(), content.getData("/"));
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
             content.resolveBinaryPreview();
             updateContentBinaryEntry(con, pk, content.getBinaryPreviewId(), content.getBinaryPreviewACL());
             FxType type = env.getType(content.getTypeId());
             if (type.isTrackHistory())
                 EJBLookup.getHistoryTrackerEngine().track(type, pk, ConversionEngine.getXStream().toXML(content),
                         "history.content.created");
         } catch (FxApplicationException e) {
             if (e instanceof FxCreateException)
                 throw (FxCreateException) e;
             if (e instanceof FxInvalidParameterException)
                 throw (FxInvalidParameterException) e;
             throw new FxCreateException(e);
         }
         return pk;
     }
 
     /**
      * Assign correct MAX_VER, LIVE_VER, ISMAX_VER and ISLIVE_VER values for a given content instance
      *
      * @param con an open and valid connection
      * @param id  the id to fix the version statistics for
      * @throws FxUpdateException if a sql error occurs
      */
     protected void fixContentVersionStats(Connection con, long id) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_VER_CALC);
             ps.setLong(1, id);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 return;
             int max_ver = rs.getInt(1);
             if (rs.wasNull())
                 return;
             int live_ver = rs.getInt(2);
             if (rs.wasNull() || live_ver < 0)
                 live_ver = 0;
             ps.close();
             ps = con.prepareStatement(CONTENT_VER_UPDATE_1);
             ps.setInt(1, max_ver);
             ps.setInt(2, live_ver);
             ps.setLong(3, id);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement(CONTENT_VER_UPDATE_2);
             ps.setLong(1, id);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement(CONTENT_VER_UPDATE_3);
             ps.setInt(1, max_ver);
             ps.setInt(2, live_ver);
             ps.setLong(3, id);
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public FxPK contentCreateVersion(Connection con, FxEnvironment env, StringBuilder sql, FxContent content) throws FxCreateException, FxInvalidParameterException {
         if (content.getPk().isNew())
             throw new FxInvalidParameterException("content", "ex.content.pk.invalid.newVersion", content.getPk());
 
         content.getRootGroup().removeEmptyEntries();
         content.getRootGroup().compactPositions(true);
         content.checkValidity();
 
         FxPK pk;
         try {
             int new_version = getContentVersionInfo(con, content.getPk().getId()).getMaxVersion() + 1;
             updateStepDependencies(con, content.getPk().getId(), new_version, env, env.getType(content.getTypeId()), content.getStepId());
             pk = createMainEntry(con, content.getPk().getId(), new_version, content);
             if (sql == null)
                 sql = new StringBuilder(2000);
             createDetailEntries(con, env, sql, pk, content.isMaxVersion(), content.isLiveVersion(), content.getData("/"));
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
         } catch (FxApplicationException e) {
             if (e instanceof FxCreateException)
                 throw (FxCreateException) e;
             if (e instanceof FxInvalidParameterException)
                 throw (FxInvalidParameterException) e;
             throw new FxCreateException(e);
         }
 
         try {
             fixContentVersionStats(con, content.getPk().getId());
         } catch (FxUpdateException e) {
             throw new FxCreateException(e);
         }
 
         FxType type = env.getType(content.getTypeId());
         if (type.isTrackHistory()) {
             try {
                 sql.setLength(0);
                 EJBLookup.getHistoryTrackerEngine().track(type, pk,
                         ConversionEngine.getXStream().toXML(contentLoad(con, pk, env, sql)),
                         "history.content.created.version", pk.getVersion());
             } catch (FxApplicationException e) {
                 LOG.error(e);
             }
         }
 
         return pk;
     }
 
     /**
      * Handle unique steps and make sure only one unique step per content instance exists
      *
      * @param con           open and valid connection
      * @param id            content id
      * @param ignoreVersion the version to ignore on changes (=current version)
      * @param env           FxEnvironment
      * @param type          FxType
      * @param stepId        the step id @throws FxNotFoundException on errors
      * @throws FxUpdateException   on errors
      * @throws FxNotFoundException on errors
      */
     protected void updateStepDependencies(Connection con, long id, int ignoreVersion, FxEnvironment env, FxType type, long stepId) throws FxNotFoundException, FxUpdateException {
         Step step = env.getStep(stepId);
         StepDefinition stepDef = env.getStepDefinition(step.getStepDefinitionId());
         if (stepDef.isUnique()) {
             Step fallBackStep = env.getStepByDefinition(step.getWorkflowId(), stepDef.getUniqueTargetId());
             updateStepDependencies(con, id, ignoreVersion, env, type, fallBackStep.getId()); //handle chained unique steps recursively
             PreparedStatement ps = null;
             try {
                 if (type.isTrackHistory()) {
                     ps = con.prepareStatement("SELECT VER FROM " + TBL_CONTENT + " WHERE STEP=? AND ID=? AND VER<>?");
                     ps.setLong(1, stepId);
                     ps.setLong(2, id);
                     ps.setInt(3, ignoreVersion);
                     ResultSet rs = ps.executeQuery();
                     HistoryTrackerEngine tracker = null;
                     String orgStep = null, newStep = null;
                     while (rs != null && rs.next()) {
                         if (tracker == null) {
                             tracker = EJBLookup.getHistoryTrackerEngine();
                             orgStep = env.getStepDefinition(env.getStep(stepId).getStepDefinitionId()).getName();
                             newStep = env.getStepDefinition(fallBackStep.getStepDefinitionId()).getName();
                         }
                         tracker.track(type, new FxPK(id, rs.getInt(1)), null, "history.content.step.change", orgStep, newStep);
                     }
                     ps.close();
                 }
                 ps = con.prepareStatement(CONTENT_STEP_DEPENDENCIES);
                 ps.setLong(1, fallBackStep.getId());
                 ps.setLong(2, stepId);
                 ps.setLong(3, id);
                 ps.setInt(4, ignoreVersion);
                 ps.executeUpdate();
             } catch (SQLException e) {
                 throw new FxUpdateException(e, "ex.content.step.dependencies.update.failed", id, e.getMessage());
             } finally {
                 try {
                     if (ps != null)
                         ps.close();
                 } catch (SQLException e) {
                     LOG.error(e, e);
                 }
             }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public FxContentVersionInfo getContentVersionInfo(Connection con, long id) throws FxNotFoundException {
         PreparedStatement ps = null;
         int min_ver = -1, max_ver = 0, live_ver = 0, lastMod_ver = 0;
         long lastMod_time;
         Map<Integer, FxContentVersionInfo.VersionData> versions = new HashMap<Integer, FxContentVersionInfo.VersionData>(5);
         try {
 
             ps = con.prepareStatement(CONTENT_VER_INFO);
             ps.setLong(1, id);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.content.notFound", new FxPK(id));
             max_ver = rs.getInt(3);
             live_ver = rs.getInt(4);
             versions.put(rs.getInt(2),
                     new FxContentVersionInfo.VersionData(LifeCycleInfoImpl.load(rs, 5, 6, 7, 8), rs.getLong(9)));
             min_ver = rs.getInt(2);
             lastMod_ver = rs.getInt(2);
             lastMod_time = versions.get(rs.getInt(2)).getLifeCycleInfo().getModificationTime();
             while (rs.next()) {
                 if (rs.getInt(2) < min_ver)
                     min_ver = rs.getInt(2);
                 versions.put(rs.getInt(2),
                         new FxContentVersionInfo.VersionData(LifeCycleInfoImpl.load(rs, 5, 6, 7, 8), rs.getLong(9)));
                 if (versions.get(rs.getInt(2)).getLifeCycleInfo().getModificationTime() >= lastMod_time) {
                     lastMod_ver = rs.getInt(2);
                     lastMod_time = versions.get(rs.getInt(2)).getLifeCycleInfo().getModificationTime();
                 }
             }
         } catch (SQLException e) {
             throw new FxNotFoundException(e, "ex.content.versionInfo.sqlError", id, e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 LOG.error(e, e);
             }
         }
         return new FxContentVersionInfo(id, min_ver, max_ver, live_ver, lastMod_ver, versions);
     }
 
     /**
      * Create a new main entry
      *
      * @param con     an open and valid connection
      * @param newId   the id to use
      * @param version the version to use
      * @param content content to create
      * @return primary key of the created content
      * @throws FxCreateException on errors
      */
     protected FxPK createMainEntry(Connection con, long newId, int version, FxContent content) throws FxCreateException {
         PreparedStatement ps = null;
         FxPK pk = new FxPK(newId, version);
         try {
             ps = con.prepareStatement(CONTENT_MAIN_INSERT);
             ps.setLong(1, newId);
             ps.setInt(2, version);
             ps.setLong(3, content.getTypeId());
             ps.setLong(4, content.getAclId());
             ps.setLong(5, content.getStepId());
             ps.setInt(6, 1);  //if creating a new version, max_ver will be fixed in a later step
             ps.setInt(7, content.isLiveVersion() ? 1 : 0);
             ps.setBoolean(8, content.isMaxVersion());
             ps.setBoolean(9, content.isLiveVersion());
             ps.setBoolean(10, content.isActive());
             ps.setInt(11, (int) content.getMainLanguage());
             if (content.isRelation()) {
                 ps.setLong(12, content.getRelatedSource().getId());
                 ps.setInt(13, content.getRelatedSource().getVersion());
                 ps.setLong(14, content.getRelatedDestination().getId());
                 ps.setInt(15, content.getRelatedDestination().getVersion());
                 ps.setLong(16, content.getRelatedSourcePosition());
                 ps.setLong(17, content.getRelatedDestinationPosition());
             } else {
                 ps.setNull(12, java.sql.Types.NUMERIC);
                 ps.setNull(13, java.sql.Types.NUMERIC);
                 ps.setNull(14, java.sql.Types.NUMERIC);
                 ps.setNull(15, java.sql.Types.NUMERIC);
                 ps.setNull(16, java.sql.Types.NUMERIC);
                 ps.setNull(17, java.sql.Types.NUMERIC);
             }
             final long userId = FxContext.get().getTicket().getUserId();
             final long now = System.currentTimeMillis();
             ps.setLong(18, userId);
             ps.setLong(19, now);
             ps.setLong(20, userId);
             ps.setLong(21, now);
             ps.setLong(22, content.getMandatorId());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
         return pk;
     }
 
     /**
      * Create all detail entries for a content instance
      *
      * @param con         an open and valid connection
      * @param env         FxEnvironment
      * @param sql         an optional StringBuffer
      * @param pk          primary key of the content
      * @param maxVersion  is this content the maximum available version?
      * @param liveVersion is this content the live version?
      * @param data        FxData to create
      * @throws FxNotFoundException on errors
      * @throws FxDbException       on errors
      * @throws FxCreateException   on errors
      */
     protected void createDetailEntries(Connection con, FxEnvironment env, StringBuilder sql, FxPK pk,
                                        boolean maxVersion, boolean liveVersion, List<FxData> data) throws FxNotFoundException, FxDbException, FxCreateException {
         try {
             FxProperty prop;
             for (FxData curr : data) {
                 if (curr.isProperty()) {
                     prop = ((FxPropertyAssignment) env.getAssignment(curr.getAssignmentId())).getProperty();
                     if (!prop.isSystemInternal())
                         insertPropertyData(prop, data, con, sql, pk, ((FxPropertyData) curr), maxVersion, liveVersion);
                 } else {
                     insertGroupData(con, sql, pk, ((FxGroupData) curr), maxVersion, liveVersion);
                     createDetailEntries(con, env, sql, pk, maxVersion, liveVersion, ((FxGroupData) curr).getChildren());
                 }
             }
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxUpdateException e) {
             throw new FxCreateException(e);
         }
     }
 
     /**
      * Get the parent group multiplicity for a given XMult
      *
      * @param xMult multiplicity of the element
      * @return parent group xmult
      */
     protected String getParentGroupXMult(String xMult) {
         if (StringUtils.isEmpty(xMult))
             return "1"; //this SHOULD not happen!
         int idx = xMult.lastIndexOf(",");
         if (idx > 0)
             return "1," + xMult.substring(0, idx); //root+parent group
         return "1"; //attached to root
     }
 
     protected final static int INSERT_LANG_POS = 4;
     protected final static int INSERT_ISDEF_LANG_POS = 13;
     protected final static int INSERT_VALUE_POS = 16;
     protected final static int FT_LANG_POS_INSERT = 3;
     protected final static int FT_VALUE_POS_INSERT = 6;
     protected final static int FT_LANG_POS_UPDATE = 4;
     protected final static int FT_VALUE_POS_UPDATE = 1;
 
     /**
      * Insert property detail data into the database
      *
      * @param prop      thepropery
      * @param allData   List of all data belonging to this property (for cascaded updates like binaries to avoid duplicates)
      * @param con       an open and valid connection
      * @param sql       an optional StringBuffer
      * @param pk        primary key of the content
      * @param data      the value
      * @param isMaxVer  is this content in the max. version?
      * @param isLiveVer is this content in the live version?
      * @throws SQLException      on errors
      * @throws FxDbException     on errors
      * @throws FxUpdateException on errors
      */
     protected void insertPropertyData(FxProperty prop, List<FxData> allData, Connection con, StringBuilder sql, FxPK pk,
                                       FxPropertyData data, boolean isMaxVer, boolean isLiveVer) throws SQLException, FxDbException, FxUpdateException {
         if (data == null || data.isEmpty())
             return;
         PreparedStatement ps = null, ps_ft = null;
         if (sql == null)
             sql = new StringBuilder(1000);
         else
             sql.setLength(0);
         try {
             String[] columns = getColumns(prop);
             String upperColumn = getUppercaseColumn(prop);
             sql.append("INSERT INTO ").append(TBL_CONTENT_DATA).
                     //                1  2   3   4    5      6     7         8     9      10          11        12                 13      14    15          16
                             append(" (ID,VER,POS,LANG,ASSIGN,XPATH,XPATHMULT,XMULT,XINDEX,PARENTXMULT,ISMAX_VER,ISLIVE_VER,ISGROUP,ISMLDEF,TPROP,PARENTXPATH,XDEPTH,");
             for (String col : columns)
                 sql.append(col).append(',');
             if (upperColumn != null)
                 sql.append(upperColumn);
             else
                 sql.deleteCharAt(sql.length() - 1); //delete last ','
             sql.append(")VALUES(?,?,?,?,?,?,?,?,?,?,?,?,false,?,?,?,");
             //calculate depth
             sql.append(data.getIndices().length).append(',');
             int s = 0;
             while (s < columns.length) {
                 sql.append("?,");
                 s++;
             }
             if (upperColumn != null)
                 sql.append('?');
             else
                 sql.deleteCharAt(sql.length() - 1); //delete last ',' if no uppercase column
             sql.append(')');
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.setInt(3, data.getPos());
             ps.setLong(5, data.getAssignmentId());
             ps.setString(6, XPathElement.stripType(data.getXPath()));
             ps.setString(7, XPathElement.stripType(data.getXPathFull()));
             String xmult = FxArrayUtils.toSeparatedList(data.getIndices(), ',');
             ps.setString(8, xmult);
             ps.setInt(9, data.getIndex());
             ps.setString(10, getParentGroupXMult(xmult));
             ps.setBoolean(11, isMaxVer);
             ps.setBoolean(12, isLiveVer);
             ps.setLong(14, prop.getId());
             ps.setString(15, XPathElement.stripType(data.getParent().getXPathFull()));
 
             sql.setLength(0);
             sql.append("INSERT INTO ").append(TBL_CONTENT_DATA_FT).
                     append("(ID,VER,LANG,ASSIGN,XMULT,VALUE) VALUES (?,?,?,?,?,?)");
             ps_ft = con.prepareStatement(sql.toString());
 
             ps_ft.setLong(1, pk.getId());
             ps_ft.setInt(2, pk.getVersion());
             if (!data.getValue().isMultiLanguage()) {
                 ps.setBoolean(INSERT_LANG_POS, true);
                 ps_ft.setInt(FT_LANG_POS_INSERT, (int) FxLanguage.DEFAULT_ID);
             } else
                 ps.setBoolean(INSERT_ISDEF_LANG_POS, false);
             ps_ft.setLong(4, data.getAssignmentId());
             ps_ft.setString(5, xmult);
             setPropertyData(true, -1, prop, allData, con, data, ps, ps_ft, upperColumn);
         } finally {
             if (ps != null)
                 ps.close();
             if (ps_ft != null)
                 ps_ft.close();
         }
     }
 
     /**
      * Update a properties data and/or position or a groups position
      *
      * @param change  the change applied
      * @param prop    the property unless change is a group change
      * @param allData all content data unless change is a group change
      * @param con     an open and valid connection
      * @param sql     sql
      * @param pk      primary key
      * @param data    property data unless change is a group change
      * @throws SQLException      on errors
      * @throws FxDbException     on errors
      * @throws FxUpdateException on errors
      */
     protected void updatePropertyData(FxDelta.FxDeltaChange change, FxProperty prop, List<FxData> allData,
                                       Connection con, StringBuilder sql, FxPK pk, FxPropertyData data)
             throws SQLException, FxDbException, FxUpdateException {
         if ((change.isProperty() && (data == null || data.isEmpty())) || !(change.isDataChange() || change.isPositionChange()))
             return;
         PreparedStatement ps = null, ps_ft = null;
         if (sql == null)
             sql = new StringBuilder(1000);
         else
             sql.setLength(0);
         try {
 
             int pos_idx = 1;
             String upperColumn = null;
             sql.append("UPDATE ").append(TBL_CONTENT_DATA).append(" SET ");
 
             if (change.isDataChange() && !change.isGroup()) {
                 String[] columns = getColumns(prop);
                 upperColumn = getUppercaseColumn(prop);
                 sql.append("ISMLDEF=?,");
                 for (String col : columns)
                     sql.append(col).append("=?,");
                 if (upperColumn != null)
                     sql.append(upperColumn).append("=?,");
 
                 pos_idx = columns.length + (upperColumn == null ? 2 : 3);
             }
 
             if (change.isPositionChange())
                 sql.append("POS=?");
             else {
                 pos_idx--;
                 sql.deleteCharAt(sql.length() - 1); //remove last ","
             }
             //                    1         2          3            4               5
             sql.append(" WHERE ID=? AND VER=? AND LANG=? AND ASSIGN=? AND XPATHMULT=?");
 
             ps = con.prepareStatement(sql.toString());
             if (change.isPositionChange())
                 ps.setInt(pos_idx, change.getNewData().getPos());
             ps.setLong(pos_idx + 1, pk.getId());
             ps.setInt(pos_idx + 2, pk.getVersion());
 //            ps.setInt(pos_idx+3, /*lang*/);
             ps.setLong(pos_idx + 4, change.getNewData().getAssignmentId());
             ps.setString(pos_idx + 5, XPathElement.stripType(change.getNewData().getXPathFull()) + (data == null ? "/" : ""));
 
             if (change.isGroup()) {
                 ps.setInt(pos_idx + 3, (int) FxLanguage.SYSTEM_ID);
                 ps.executeUpdate();
                 return;
             }
 
             if (change.isPositionChange() && !change.isDataChange()) {
                 //just update positions
                 for (long lang : data.getValue().getTranslatedLanguages()) {
                     ps.setInt(pos_idx + 3, (int) lang);
                     ps.executeUpdate();
                 }
                 return;
             }
 
             sql.setLength(0);
             //                                                                   1          2         3          4            5           6
             sql.append("UPDATE ").append(TBL_CONTENT_DATA_FT).append(" SET VALUE=? WHERE ID=? AND VER=? AND LANG=? AND ASSIGN=? AND XMULT=?");
             ps_ft = con.prepareStatement(sql.toString());
 
             ps_ft.setLong(2, pk.getId());
             ps_ft.setInt(3, pk.getVersion());
             //3==lang
             ps_ft.setLong(5, data.getAssignmentId());
             ps_ft.setString(6, FxArrayUtils.toSeparatedList(data.getIndices(), ','));
             setPropertyData(false, pos_idx, prop, allData, con, data, ps, ps_ft, upperColumn);
         } finally {
             if (ps != null)
                 ps.close();
             if (ps_ft != null)
                 ps_ft.close();
         }
     }
 
     /**
      * Set a properties data for inserts or updates
      *
      * @param insert              perform insert or update?
      * @param update_position_idx position in the prepared statement to start at
      * @param prop                current property
      * @param allData             all data of the instance (might be needed to buld references, etc.)
      * @param con                 an open and valid connection
      * @param data                current property data
      * @param ps                  prepared statement for the data table
      * @param ps_ft               prepared statement for the fulltext table
      * @param upperColumn         name of the uppercase column (if present)
      * @throws SQLException      on errors
      * @throws FxUpdateException on errors
      * @throws FxDbException     on errors
      */
     private void setPropertyData(boolean insert, int update_position_idx, FxProperty prop, List<FxData> allData,
                                  Connection con, FxPropertyData data, PreparedStatement ps, PreparedStatement ps_ft,
                                  String upperColumn) throws SQLException, FxUpdateException, FxDbException {
         FxValue value = data.getValue();
         if (value instanceof FxNoAccess) {
             FxContext.get().runAsSystem();
             value = ((FxNoAccess) value).getWrappedValue();
             FxContext.get().stopRunAsSystem();
         }
         if (value.isMultiLanguage() != ((FxPropertyAssignment) data.getAssignment()).isMultiLang()) {
             if (((FxPropertyAssignment) data.getAssignment()).isMultiLang())
                 throw new FxUpdateException("ex.content.value.invalid.multilanguage.ass.multi", data.getXPathFull());
             else
                 throw new FxUpdateException("ex.content.value.invalid.multilanguage.ass.single", data.getXPathFull());
         }
         int pos_lang = insert ? INSERT_LANG_POS : update_position_idx + 3;
         int pos_isdef_lang = insert ? INSERT_ISDEF_LANG_POS : 1;
         int pos_value = insert ? INSERT_VALUE_POS : 2;
         if (prop.getDataType().isSingleRowStorage()) {
             //Data types that just use one db row can be handled in a very similar way
             Object translatedValue;
             GregorianCalendar gc = null;
             for (int i = 0; i < value.getTranslatedLanguages().length; i++) {
                 translatedValue = value.getTranslation(value.getTranslatedLanguages()[i]);
                 if (translatedValue == null) {
                     java.lang.System.err.println("null!");
                 }
                 ps.setInt(pos_lang, value.getTranslatedLanguages()[i].intValue());
                 if (!value.isMultiLanguage())
                     ps.setBoolean(pos_isdef_lang, true);
                 else
                     ps.setBoolean(pos_isdef_lang, value.isDefaultLanguage(value.getTranslatedLanguages()[i]));
                 if (upperColumn != null)
                     ps.setString(pos_value + 1, translatedValue.toString().toUpperCase());
                 ps_ft.setInt(insert ? FT_LANG_POS_INSERT : FT_LANG_POS_UPDATE, value.getTranslatedLanguages()[i].intValue());
                 ps_ft.setString(insert ? FT_VALUE_POS_INSERT : FT_VALUE_POS_UPDATE, translatedValue.toString());
                 switch (prop.getDataType()) {
                     case Double:
                         checkDataType(FxDouble.class, value, data.getXPathFull());
                         ps.setDouble(pos_value, (Double) translatedValue);
 //                        ps.setBigDecimal(pos_value, new BigDecimal((Double) translatedValue));
                         break;
                     case Float:
                         checkDataType(FxFloat.class, value, data.getXPathFull());
                         ps.setFloat(pos_value, (Float) translatedValue);
 //                        ps.setBigDecimal(pos_value, new BigDecimal(((Float) translatedValue).doubleValue()));
                         break;
                     case LargeNumber:
                         checkDataType(FxLargeNumber.class, value, data.getXPathFull());
                         ps.setLong(pos_value, (Long) translatedValue);
                         break;
                     case Number:
                         checkDataType(FxNumber.class, value, data.getXPathFull());
                         ps.setInt(pos_value, (Integer) translatedValue);
                         break;
                     case HTML:
                         checkDataType(FxHTML.class, value, data.getXPathFull());
                         boolean useTidy = ((FxHTML) value).isTidyHTML();
                         ps.setBoolean(pos_value + 1, useTidy);
                         if (useTidy)
                             translatedValue = doTidy(data.getXPathFull(), (String) translatedValue);
                         String extracted;
                         ExtractedData xd = HtmlExtractor.extract(useTidy
                                 ? (String) translatedValue
                                 : doTidy(data.getXPathFull(), (String) translatedValue));
                         if (xd == null) {
                             throw new FxUpdateException("ex.content.value.extraction.failed");
                         } else
                             extracted = xd.getText();
                         ps.setString(pos_value + 2, extracted);
                         ps.setString(pos_value, (String) translatedValue);
                         break;
                     case String1024:
                     case Text:
                         checkDataType(FxString.class, value, data.getXPathFull());
                         ps.setString(pos_value, (String) translatedValue);
                         break;
                     case Boolean:
                         checkDataType(FxBoolean.class, value, data.getXPathFull());
                         ps.setBoolean(pos_value, (Boolean) translatedValue);
                         break;
                     case Date:
                         checkDataType(FxDate.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime((java.util.Date) translatedValue);
                         //strip all time information, this might not be necessary since ps.setDate() strips them
                         //for most databases but won't hurt either ;)
                         gc.set(GregorianCalendar.HOUR, 0);
                         gc.set(GregorianCalendar.MINUTE, 0);
                         gc.set(GregorianCalendar.SECOND, 0);
                         gc.set(GregorianCalendar.MILLISECOND, 0);
                         ps.setDate(pos_value, new java.sql.Date(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 1, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 2, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 3, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         break;
                     case DateTime:
                         checkDataType(FxDateTime.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime((java.util.Date) translatedValue);
                         ps.setTimestamp(pos_value, new Timestamp(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 1, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 2, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 3, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         ps.setInt(pos_value + 4, gc.get(GregorianCalendar.HOUR_OF_DAY));
                         ps.setInt(pos_value + 5, gc.get(GregorianCalendar.MINUTE));
                         ps.setInt(pos_value + 6, gc.get(GregorianCalendar.SECOND));
                         break;
                     case DateRange:
                         checkDataType(FxDateRange.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime(((DateRange) translatedValue).getLower());
                         gc.set(GregorianCalendar.HOUR, 0);
                         gc.set(GregorianCalendar.MINUTE, 0);
                         gc.set(GregorianCalendar.SECOND, 0);
                         gc.set(GregorianCalendar.MILLISECOND, 0);
                         ps.setDate(pos_value, new java.sql.Date(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 1, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 2, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 3, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         gc.setTime(((DateRange) translatedValue).getUpper());
                         gc.set(GregorianCalendar.HOUR, 0);
                         gc.set(GregorianCalendar.MINUTE, 0);
                         gc.set(GregorianCalendar.SECOND, 0);
                         gc.set(GregorianCalendar.MILLISECOND, 0);
                         ps.setDate(pos_value + 4, new java.sql.Date(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 5, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 6, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 7, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         break;
                     case DateTimeRange:
                         checkDataType(FxDateTimeRange.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime(((DateRange) translatedValue).getLower());
                         ps.setTimestamp(pos_value, new Timestamp(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 1, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 2, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 3, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         ps.setInt(pos_value + 4, gc.get(GregorianCalendar.HOUR_OF_DAY));
                         ps.setInt(pos_value + 5, gc.get(GregorianCalendar.MINUTE));
                         ps.setInt(pos_value + 6, gc.get(GregorianCalendar.SECOND));
                         gc.setTime(((DateRange) translatedValue).getUpper());
                         ps.setTimestamp(pos_value + 7, new Timestamp(gc.getTimeInMillis()));
                         ps.setInt(pos_value + 8, gc.get(GregorianCalendar.YEAR));
                         ps.setInt(pos_value + 9, gc.get(GregorianCalendar.MONTH) + 1);
                         ps.setInt(pos_value + 10, gc.get(GregorianCalendar.DAY_OF_MONTH));
                         ps.setInt(pos_value + 11, gc.get(GregorianCalendar.HOUR_OF_DAY));
                         ps.setInt(pos_value + 12, gc.get(GregorianCalendar.MINUTE));
                         ps.setInt(pos_value + 13, gc.get(GregorianCalendar.SECOND));
                         break;
                     case Binary:
                         checkDataType(FxBinary.class, value, data.getXPathFull());
                         BinaryDescriptor binary = (BinaryDescriptor) translatedValue;
                         if (!binary.isNewBinary()) {
                             ps.setLong(pos_value, binary.getId());
                         } else {
                             try {
                                 //transfer the binary from the transit table to the binary table
                                 BinaryDescriptor created = binaryTransit(con, binary);
                                 ps.setLong(pos_value, created.getId());
                                 //check all other properties if they contain the same handle
                                 //and replace with the data of the new binary
                                 for (FxData _curr : allData) {
                                     if (_curr instanceof FxPropertyData && !_curr.isEmpty() &&
                                             ((FxPropertyData) _curr).getValue() instanceof FxBinary) {
                                         FxBinary _val = (FxBinary) ((FxPropertyData) _curr).getValue();
 //                                            System.out.println("replacing "+_curr.getXPathFull());
                                         _val._replaceHandle(binary.getHandle(), created);
                                     }
                                 }
                             } catch (FxApplicationException e) {
                                 throw new FxDbException(e);
                             }
                         }
                         String fulltext = FxXMLUtils.getElementData(binary.getMetadata(), "compressed");
                         if (fulltext != null)
                             ps_ft.setString(insert ? FT_VALUE_POS_INSERT : FT_VALUE_POS_UPDATE, fulltext);
                         break;
                     case SelectOne:
                         checkDataType(FxSelectOne.class, value, data.getXPathFull());
                         ps.setLong(pos_value, ((FxSelectListItem) translatedValue).getId());
                         break;
                     case SelectMany:
                         checkDataType(FxSelectMany.class, value, data.getXPathFull());
                         SelectMany sm = (SelectMany) translatedValue;
                         for (int i1 = 0; i1 < sm.getSelected().size(); i1++) {
                             FxSelectListItem item = sm.getSelected().get(i1);
                             if (i1 > 0)
                                 ps.executeUpdate();
                             ps.setLong(pos_value, item.getId());
                             ps.setString(pos_value + 1, sm.getSelectedIdsList());
                         }
                         if (sm.getSelected().size() == 0)
                             ps.setLong(pos_value, 0); //write the virtual item as a marker to have a valid row
                         break;
                     case Reference:
                         checkDataType(FxReference.class, value, data.getXPathFull());
                         checkReference(con, prop.getReferencedType(), ((ReferencedContent) translatedValue), data.getXPathFull());
                         ps.setLong(pos_value, ((ReferencedContent) translatedValue).getId());
                         break;
                     case InlineReference:
                     default:
                         throw new FxDbException(LOG, "ex.db.notImplemented.store", prop.getDataType().getName());
                 }
                 ps.executeUpdate();
                 if (prop.isFulltextIndexed())
                     ps_ft.executeUpdate();
             }
         } else {
             switch (prop.getDataType()) {
                 //TODO: implement datatype specific insert
 
                 default:
                     throw new FxDbException(LOG, "ex.db.notImplemented.store", prop.getDataType().getName());
             }
 
         }
     }
 
     /**
      * Check if a referenced id is of an expected type and exists
      *
      * @param con          an open and valid connection
      * @param expectedType the expected type
      * @param ref          referenced content
      * @param xpath        the XPath this reference is for (used for error messages only)
      * @throws FxDbException if not exists or wrong type
      */
     private static void checkReference(Connection con, FxType expectedType, FxPK ref, String xpath) throws FxDbException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("SELECT DISTINCT TDEF FROM " + TBL_CONTENT + " WHERE ID=?");
             ps.setLong(1, ref.getId());
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxDbException("ex.content.reference.notFound", ref, xpath);
             long type = rs.getLong(1);
             if (type != expectedType.getId())
                 throw new FxDbException("ex.content.value.invalid.reftype", expectedType, CacheAdmin.getEnvironment().getType(type));
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
         //To change body of created methods use File | Settings | File Templates.
     }
 
     /**
      * Check if the given value is of the expected class
      *
      * @param dataClass expected class
      * @param value     value to check
      * @param XPath     xpath with full indices for error message
      * @throws FxUpdateException if the class does not match
      */
     private static void checkDataType(Class dataClass, FxValue value, String XPath) throws FxUpdateException {
         if (!(value.getClass().getSimpleName().equals(dataClass.getSimpleName()))) {
             throw new FxUpdateException("ex.content.value.invalid.class", value.getClass().getSimpleName(), XPath, dataClass.getSimpleName());
         }
     }
 
     /**
      * Run tidy on the given content
      *
      * @param XPath   XPath with full indices for error messages
      * @param content the string to tidy
      * @return tidied string
      * @throws FxUpdateException if tidy failed
      */
     private static String doTidy(String XPath, String content) throws FxUpdateException {
         Tidy tidy = new Tidy();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         tidy.setDropEmptyParas(true);
         tidy.setMakeClean(true);
         tidy.setHideEndTags(true);
         tidy.setTidyMark(false);
         tidy.setMakeBare(true);
         tidy.setXHTML(true);
 //        tidy.setOnlyErrors(true);
         tidy.setShowWarnings(false);
         tidy.setQuiet(true);
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         tidy.setErrout(pw);
         tidy.parse(new StringReader(content), out);
 
         if (tidy.getParseErrors() > 0) {
             String error = sw.getBuffer().toString();
             throw new FxUpdateException("ex.content.value.tidy.failed", XPath, error);
         }
         content = out.toString();
         return content;
     }
 
 
     /**
      * Transfer a binary from the transit to the 'real' binary table
      *
      * @param con    open and valid connection
      * @param binary the binary descriptor
      * @return descriptor of final binary
      * @throws SQLException           on errors
      * @throws FxApplicationException on errors looking up the sequencer
      */
     private BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary) throws FxApplicationException, SQLException {
         long id = EJBLookup.getSequencerEngine().getId(SequencerEngine.System.BINARY);
         return binaryTransit(con, binary, id, 1, 1);
     }
 
     /**
      * Transfer a binary from the transit to the 'real' binary table
      *
      * @param con     open and valid connection
      * @param binary  the binary descriptor
      * @param id      desired id
      * @param version desired version
      * @param quality desired quality
      * @return descriptor of final binary
      * @throws SQLException           on errors
      * @throws FxApplicationException on errors looking up the sequencer
      */
     private BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary, long id, int version, int quality) throws FxApplicationException, SQLException {
 //        System.out.println("Binary transit: " + binary.getName() + "/" + binary.getHandle());
         PreparedStatement ps = null;
         BinaryDescriptor created;
 
         try {
             double resolution = 0.0;
             int width = 0;
             int height = 0;
             boolean isImage = binary.getMimeType().startsWith("image/");
             if (isImage) {
                 try {
                     width = Integer.parseInt(FxXMLUtils.getElementData(binary.getMetadata(), "width"));
                     height = Integer.parseInt(FxXMLUtils.getElementData(binary.getMetadata(), "height"));
                     resolution = Double.parseDouble(FxXMLUtils.getElementData(binary.getMetadata(), "xResolution"));
                 } catch (NumberFormatException e) {
                     //ignore
                     LOG.warn(e, e);
                 }
             }
             created = new BinaryDescriptor(CacheAdmin.getStreamServers(), id, version, quality, java.lang.System.currentTimeMillis(),
                     binary.getName(), binary.getSize(), binary.getMetadata(), binary.getMimeType(), isImage, resolution, width, height);
             ps = con.prepareStatement(BINARY_TRANSIT);
             ps.setLong(1, created.getId());
             ps.setInt(2, created.getVersion()); //version
             ps.setInt(3, created.getQuality()); //quality
 //            ps.setString(4, binary.getHandle());
             ps.setString(4, created.getName());
             ps.setLong(5, created.getSize());
             ps.setString(6, created.getMetadata());
             ps.setString(7, created.getMimeType());
             ps.setBoolean(8, created.isImage());
             ps.setDouble(9, created.getResolution());
             ps.setInt(10, created.getWidth());
             ps.setInt(11, created.getHeight());
             ps.setString(12, binary.getHandle());
             ps.executeUpdate();
         } finally {
             if (ps != null)
                 ps.close();
         }
         return created;
     }
 
     /**
      * Insert group detail data into the database
      *
      * @param con       an open and valid connection
      * @param sql       an optional StringBuffer
      * @param pk        primary key of the content
      * @param groupData the group
      * @param isMaxVer  is this content in the max. version?
      * @param isLiveVer is this content in the live version?
      * @throws SQLException on errors
      */
     protected void insertGroupData(Connection con, StringBuilder sql, FxPK pk, FxGroupData groupData,
                                    boolean isMaxVer, boolean isLiveVer) throws SQLException {
         if (groupData == null || groupData.isEmpty())
             return;
         PreparedStatement ps = null;
         if (sql == null)
             sql = new StringBuilder(500);
         else
             sql.setLength(0);
         try {
             sql.append("INSERT INTO ").append(TBL_CONTENT_DATA).
                     //                1  2   3   4    5      6     7         8            9           10        11         12
                             append(" (ID,VER,POS,LANG,ASSIGN,XPATH,XPATHMULT,XMULT,XINDEX,PARENTXMULT,ISMAX_VER,ISLIVE_VER,PARENTXPATH,ISGROUP,ISMLDEF,XDEPTH");
             sql.append(")VALUES(?,?,?,?,?,?,?,?,").append(groupData.getIndex()).append(",?,?,?,?,true,false,").append(groupData.getIndices().length).append(")");
 
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.setInt(3, groupData.getPos());
             ps.setInt(4, (int) FxLanguage.SYSTEM_ID);
             ps.setLong(5, groupData.getAssignmentId());
             ps.setString(6, groupData.getXPath() + "/");
             ps.setString(7, groupData.getXPathFull() + "/");
             String xmult = FxArrayUtils.toSeparatedList(groupData.getIndices(), ',');
             ps.setString(8, xmult);
             ps.setString(9, getParentGroupXMult(xmult));
             ps.setBoolean(10, isMaxVer);
             ps.setBoolean(11, isLiveVer);
             ps.setString(12, groupData.getParent().getXPathFull());
             ps.executeUpdate();
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Remove a detail data entry (group or property, in all existing languages)
      *
      * @param con  an open and valid Connection
      * @param sql  sql
      * @param pk   primary key
      * @param data the entry to remove
      * @throws SQLException on errors
      */
     private void deleteDetailData(Connection con, StringBuilder sql, FxPK pk, FxData data) throws SQLException {
         if (data == null || data.isEmpty())
             return;
         PreparedStatement ps = null;
         if (sql == null)
             sql = new StringBuilder(500);
         else
             sql.setLength(0);
         try {
             //                                                                    1         2            3               4
             sql.append("DELETE FROM ").append(TBL_CONTENT_DATA).append(" WHERE ID=? AND VER=? AND ASSIGN=? AND XPATHMULT=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.setLong(3, data.getAssignmentId());
             if (data.isGroup())
                 ps.setString(4, data.getXPathFull() + "/");
             else
                 ps.setString(4, data.getXPathFull());
             ps.executeUpdate();
             String xmult = FxArrayUtils.toSeparatedList(data.getIndices(), ',');
 
             ps.close();
             sql.setLength(0);
             //                                                                       1         2            3           4
             sql.append("DELETE FROM ").append(TBL_CONTENT_DATA_FT).append(" WHERE ID=? AND VER=? AND ASSIGN=? AND XMULT=?");
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.setLong(3, data.getAssignmentId());
             ps.setString(4, xmult);
             ps.executeUpdate();
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxContent contentLoad(Connection con, FxPK pk, FxEnvironment env, StringBuilder sql) throws FxLoadException, FxInvalidParameterException, FxNotFoundException {
         if (pk.isNew())
             throw new FxInvalidParameterException("pk", "ex.content.load.newPK");
         if (sql == null)
             sql = new StringBuilder(1000);
         sql.append(CONTENT_MAIN_LOAD);
         sql.append(" WHERE ID=? AND ");
         if (pk.isDistinctVersion())
             sql.append(" VER=?");
         else if (pk.getVersion() == FxPK.LIVE)
             sql.append(" ISLIVE_VER=?");
         else if (pk.getVersion() == FxPK.MAX)
             sql.append(" ISMAX_VER=?");
         PreparedStatement ps = null;
         FxPK contentPK, sourcePK = null, destinationPK = null;
         int srcPos = 0, dstPos = 0;
         try {
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             if (pk.isDistinctVersion())
                 ps.setInt(2, pk.getVersion());
             else
                 ps.setBoolean(2, true);
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.content.notFound", pk);
             contentPK = new FxPK(rs.getLong(1), rs.getInt(2));
             FxType type = env.getType(rs.getLong(3));
             ACL acl = env.getACL(rs.getInt(4));
             Step step = env.getStep(rs.getLong(5));
             Mandator mand = env.getMandator(rs.getInt(22));
             FxGroupData root = loadDetails(con, type, env, contentPK, pk.getVersion());
             rs.getLong(12);
             if (!rs.wasNull()) {
                 sourcePK = new FxPK(rs.getLong(12), rs.getInt(13));
                 destinationPK = new FxPK(rs.getLong(14), rs.getInt(15));
                 srcPos = rs.getInt(16);
                 dstPos = rs.getInt(17);
             }
             FxContent content = new FxContent(contentPK, type.getId(), type.isRelation(), mand.getId(), acl.getId(), step.getId(), rs.getInt(6),
                     rs.getInt(7), rs.getBoolean(10), rs.getInt(11), sourcePK, destinationPK, srcPos, dstPos,
                     LifeCycleInfoImpl.load(rs, 18, 19, 20, 21), root, rs.getLong(23), rs.getLong(24)).initSystemProperties();
             if (rs.next())
                 throw new FxLoadException("ex.content.load.notDistinct", pk);
             return content;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxDbException e) {
             throw new FxLoadException(e);
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 LOG.warn(e, e);
             }
         }
     }
 
     /**
      * Load all detail entries for a content instance
      *
      * @param con              open and valid(!) connection
      * @param type             FxType used
      * @param env              FxEnvironment
      * @param pk               primary key of the content data to load
      * @param requestedVersion the originally requested version (LIVE, MAX or specific version number, needed to resolve references since the pk's version is resoved already)
      * @return a (root) group containing all data
      * @throws com.flexive.shared.exceptions.FxLoadException
      *                                     on errors
      * @throws SQLException                on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxDbException               on errors
      */
     @SuppressWarnings("unchecked")
     protected FxGroupData loadDetails(Connection con, FxType type, FxEnvironment env, FxPK pk, int requestedVersion) throws FxLoadException, SQLException, FxInvalidParameterException, FxDbException {
         FxGroupData root;
         PreparedStatement ps = null;
         try {
             root = type.createEmptyData(type.buildXPathPrefix(pk));
             root.removeEmptyEntries(true);
             root.compactPositions(true);
             ps = con.prepareStatement(CONTENT_DATA_LOAD);
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ResultSet rs = ps.executeQuery();
             String currXPath = null;
             FxAssignment currAssignment = null;
             int currPos = -1;
             long currLang;
             long defLang = FxLanguage.SYSTEM_ID;
             boolean isGroup = true;
             boolean isMLDef;
             boolean multiLang = false;
             FxValue currValue = null;
             String[] columns = null;
             List<ServerLocation> server = CacheAdmin.getStreamServers();
             while (rs != null && rs.next()) {
                 if (currXPath != null && !currXPath.equals(rs.getString(5))) {
                     //add this property
                     if (!isGroup)
                         currValue.setDefaultLanguage(defLang);
                     addValue(root, currXPath, currAssignment, currPos, currValue);
                     currValue = null;
                     defLang = FxLanguage.SYSTEM_ID;
                 }
                 //read next row
                 currXPath = rs.getString(5);
                 currPos = rs.getInt(1);
                 currLang = rs.getInt(2);
                 isMLDef = rs.getBoolean(8);
                 isGroup = rs.getBoolean(7);
                 if (currAssignment == null || currAssignment.getId() != rs.getLong(3)) {
                     //new data type
                     currAssignment = env.getAssignment(rs.getLong(3));
                     if (!isGroup)
                         columns = getColumns(((FxPropertyAssignment) currAssignment).getProperty());
                 }
 
 
                 if (!isGroup) {
                     FxDataType dataType = ((FxPropertyAssignment) currAssignment).getProperty().getDataType();
                     if (currValue == null)
                         multiLang = ((FxPropertyAssignment) currAssignment).isMultiLang();
                     switch (dataType) {
                         case Float:
                             if (currValue == null)
                                 currValue = new FxFloat(multiLang, currLang, rs.getFloat(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getFloat(columns[0]));
                             break;
                         case Double:
                             if (currValue == null)
                                 currValue = new FxDouble(multiLang, currLang, rs.getDouble(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getDouble(columns[0]));
                             break;
                         case LargeNumber:
                             if (currValue == null)
                                 currValue = new FxLargeNumber(multiLang, currLang, rs.getLong(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getLong(columns[0]));
                             break;
                         case Number:
                             if (currValue == null)
                                 currValue = new FxNumber(multiLang, currLang, rs.getInt(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getInt(columns[0]));
                             break;
                         case HTML:
                             if (currValue == null) {
                                 currValue = new FxHTML(multiLang, currLang, rs.getString(columns[0]));
                                 ((FxHTML) currValue).setTidyHTML(rs.getBoolean(columns[1]));
                             } else
                                 currValue.setTranslation(currLang, rs.getString(columns[0]));
                             break;
                         case String1024:
                         case Text:
                             if (currValue == null) {
                                 currValue = new FxString(multiLang, currLang, rs.getString(columns[0]));
                                 if (dataType == FxDataType.String1024)
                                     currValue.setMaxInputLength(1024);
                             } else
                                 currValue.setTranslation(currLang, rs.getString(columns[0]));
                             break;
                         case Boolean:
                             if (currValue == null)
                                 currValue = new FxBoolean(multiLang, currLang, rs.getBoolean(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getBoolean(columns[0]));
                             break;
                         case Date:
                             if (currValue == null)
                                 currValue = new FxDate(multiLang, currLang, rs.getDate(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getDate(columns[0]));
                             break;
                         case DateTime:
                             if (currValue == null)
                                 currValue = new FxDateTime(multiLang, currLang, rs.getTimestamp(columns[0]));
                             else
                                 currValue.setTranslation(currLang, rs.getTimestamp(columns[0]));
                             break;
                         case DateRange:
                             if (currValue == null)
                                 currValue = new FxDateRange(multiLang, currLang,
                                         new DateRange(
                                                 rs.getDate(columns[0]),
                                                 rs.getDate(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[4]))
                                 );
                             else
                                 currValue.setTranslation(currLang,
                                         new DateRange(
                                                 rs.getDate(columns[0]),
                                                 rs.getDate(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[4]))
                                 );
                             break;
                         case DateTimeRange:
                             if (currValue == null)
                                 currValue = new FxDateTimeRange(multiLang, currLang,
                                         new DateRange(
                                                 rs.getTimestamp(columns[0]),
                                                 rs.getTimestamp(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[7]))
                                 );
                             else
                                 currValue.setTranslation(currLang,
                                         new DateRange(
                                                 rs.getTimestamp(columns[0]),
                                                 rs.getTimestamp(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[7]))
                                 );
                             break;
                         case Binary:
                             BinaryDescriptor desc = loadBinaryDescriptor(server, con, rs.getLong(columns[0]));
                             if (currValue == null)
                                 currValue = new FxBinary(multiLang, currLang, desc);
                             else
                                 currValue.setTranslation(currLang, desc);
                             break;
                         case SelectOne:
                             FxSelectListItem singleItem = env.getSelectListItem(rs.getLong(columns[0]));
                             if (currValue == null)
                                 currValue = new FxSelectOne(multiLang, currLang, singleItem);
                             else
                                 currValue.setTranslation(currLang, singleItem);
                             break;
                         case SelectMany:
                             long itemId = rs.getLong(columns[0]);
                             FxSelectList list = ((FxPropertyAssignment) currAssignment).getProperty().getReferencedList();
                             if (currValue == null)
                                 currValue = new FxSelectMany(multiLang, currLang, new SelectMany(list));
                             FxSelectMany sm = (FxSelectMany) currValue;
                             if (sm.isTranslationEmpty(currLang))
                                 sm.setTranslation(currLang, new SelectMany(list));
                             if (itemId > 0)
                                 sm.getTranslation(currLang).selectItem(list.getItem(itemId));
                             break;
                         case Reference:
                             if (currValue == null)
 //                                currValue = new FxReference(multiLang, currLang, new ReferencedContent(rs.getLong(columns[0])));
                                 currValue = new FxReference(multiLang, currLang, resolveReference(con, requestedVersion, rs.getLong(columns[0])));
                             else
                                 currValue.setTranslation(currLang, resolveReference(con, requestedVersion, rs.getLong(columns[0])));
                             break;
                         default:
                             throw new FxDbException(LOG, "ex.db.notImplemented.load", dataType.getName());
                     }
                     if (isMLDef)
                         defLang = currLang;
                 }
             }
             if (currValue != null) {
                 //add last property
                 currValue.setDefaultLanguage(defLang);
                 addValue(root, currXPath, currAssignment, currPos, currValue);
             }
         } catch (FxCreateException e) {
             throw new FxLoadException(e);
         } catch (FxNotFoundException e) {
             throw new FxLoadException(e);
         } finally {
             if (ps != null)
                 ps.close();
         }
         return root;
     }
 
     /**
      * Resolve a reference from <code>pk</code> to <code>referencedId</code>
      *
      * @param con            an open and valid connection
      * @param contentVersion the version of the referencing content
      * @param referencedId   the referenced id
      * @return the referenced content
      * @throws SQLException on errors
      */
     private ReferencedContent resolveReference(Connection con, int contentVersion, long referencedId) throws SQLException {
         String sql = contentVersion == FxPK.LIVE ? CONTENT_REFERENCE_LIVE : CONTENT_REFERENCE_MAX;
         PreparedStatement ps = null;
         int referencedVersion;
         long stepId, aclId, typeId, ownerId;
         String caption;
         try {
             ps = con.prepareStatement(sql);
             ps.setLong(1, referencedId);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 referencedVersion = rs.getInt(1);
                 aclId = rs.getLong(2);
                 stepId = rs.getLong(3);
                 typeId = rs.getLong(4);
                 ownerId = rs.getLong(5);
             } else if (contentVersion == FxPK.LIVE) {
                 ps.close();
                 ps = con.prepareStatement(CONTENT_REFERENCE_MAX);
                 ps.setLong(1, referencedId);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     referencedVersion = rs.getInt(1);
                     aclId = rs.getLong(2);
                     stepId = rs.getLong(3);
                     typeId = rs.getLong(4);
                     ownerId = rs.getLong(5);
                 } else {
                     LOG.error("Failed to resolve a reference with id " + referencedId + ": no max. version found! (in fallback already!)");
                     return new ReferencedContent(referencedId);
                 }
             } else {
                 LOG.error("Failed to resolve a reference with id " + referencedId + ": no max. version found!");
                 return new ReferencedContent(referencedId);
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_REFERENCE_CAPTION);
             ps.setLong(1, referencedId);
             ps.setInt(2, referencedVersion);
             try {
                 ps.setLong(3, EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_CAPTION_PROPERTY));
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
             rs = ps.executeQuery();
             if (rs != null && rs.next())
                 caption = rs.getString(1);
             else
                 caption = "";
             FxEnvironment env = CacheAdmin.getEnvironment();
 
             ReferencedContent ref = new ReferencedContent(new FxPK(referencedId, referencedVersion), caption, env.getStep(stepId), env.getACL(aclId));
             try {
                 ref.setAccessGranted(
                         FxPermissionUtils.checkPermission(
                                 FxContext.get().getTicket(),
                                 ownerId, ACL.Permission.READ,
                                 env.getType(typeId),
                                 ref.getStep().getAclId(),
                                 ref.getAcl().getId(),
                                 false));
             } catch (FxNoAccessException e) {
                 ref.setAccessGranted(false);
             }
             if (LOG.isDebugEnabled()) {
                 LOG.debug("ReferencedContent: " + ref.toStringExtended());
             }
             return ref;
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Load a binary descriptor
      *
      * @param server server side StreamServers that can provide the binary
      * @param con    open connection
      * @param id     id of the binary (in FX_BINARY table)
      * @return BinaryDescriptor
      * @throws SQLException  on errors
      * @throws FxDbException on errors
      */
     private BinaryDescriptor loadBinaryDescriptor(List<ServerLocation> server, Connection con, long id) throws SQLException, FxDbException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(BINARY_DESC_LOAD);
             ps.setLong(1, id);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //ver
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return new BinaryDescriptor(server, id, 1, 1, rs.getLong(4), rs.getString(1), rs.getLong(2), rs.getString(3),
                         rs.getString(5), rs.getBoolean(6), rs.getDouble(7), rs.getInt(8), rs.getInt(9));
             }
         } finally {
             if (ps != null)
                 ps.close();
         }
         throw new FxDbException("ex.content.binary.loadDescriptor.failed", id);
     }
 
     /**
      * Helper method to add a value of a detail entry with a given XPath to the instance being loaded
      *
      * @param root       the root group
      * @param xPath      XPath of the entry
      * @param assignment assignment used
      * @param pos        position in hierarchy
      * @param value      the value to add
      * @throws FxInvalidParameterException on errors
      * @throws FxNotFoundException         on errors
      * @throws FxCreateException           if failed to create group entries
      */
     protected void addValue(FxGroupData root, String xPath, FxAssignment assignment,
                             int pos, FxValue value) throws FxInvalidParameterException, FxNotFoundException, FxCreateException {
        if( !assignment.isEnabled() )
            return;
         if (assignment instanceof FxGroupAssignment) {
             root.addGroup(xPath, (FxGroupAssignment) assignment, pos);
             root.getGroup(xPath.substring(0, xPath.length() - 1)).removeEmptyEntries(true);
         } else {
             root.addProperty(xPath, (FxPropertyAssignment) assignment, value, pos);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPK contentSave(Connection con, FxEnvironment env, StringBuilder sql, FxContent content, long fqnPropertyId) throws FxInvalidParameterException, FxUpdateException {
         content.getRootGroup().removeEmptyEntries();
         content.getRootGroup().compactPositions(true);
         content.checkValidity();
         FxPK pk = content.getPk();
         if (pk.isNew() || !pk.isDistinctVersion())
             throw new FxInvalidParameterException("PK", "ex.content.pk.invalid.save", pk);
         FxDelta delta;
         FxContent original;
         final FxType type = env.getType(content.getTypeId());
         try {
             original = contentLoad(con, content.getPk(), env, sql);
             original.getRootGroup().removeEmptyEntries();
             original.getRootGroup().compactPositions(true);
             delta = FxDelta.processDelta(original, content);
         } catch (FxLoadException e) {
             throw new FxUpdateException(e);
         } catch (FxNotFoundException e) {
             throw new FxUpdateException(e);
         }
         if (original.getStepId() != content.getStepId()) {
             Workflow wf = env.getWorkflow(env.getStep(content.getStepId()).getWorkflowId());
             if (!wf.isRouteValid(original.getStepId(), content.getStepId())) {
                 throw new FxInvalidParameterException("STEP", "ex.content.step.noRoute",
                         env.getStepDefinition(env.getStep(original.getStepId()).getStepDefinitionId()).getLabel().getBestTranslation(),
                         env.getStepDefinition(env.getStep(content.getStepId()).getStepDefinitionId()).getLabel().getBestTranslation());
             }
             if (type.isTrackHistory())
                 EJBLookup.getHistoryTrackerEngine().track(type, content.getPk(), null, "history.content.step.change",
                         env.getStepDefinition(env.getStep(original.getStepId()).getStepDefinitionId()).getName(),
                         env.getStepDefinition(env.getStep(content.getStepId()).getStepDefinitionId()).getName());
         }
         if (!delta.changes()) {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("====== NO CHANGES =======");
             }
             return pk;
         } else {
             if (LOG.isDebugEnabled()) {
                 LOG.debug(delta.dump());
             }
         }
 
         if (delta.isInternalPropertyChanged())
             updateMainEntry(con, content);
         try {
             disableDetailUniqueChecks(con);
             //full replace code start
 //            removeDetailEntriesVersion(con, pk);
 //            createDetailEntries(con, env, sql, pk, content.isMaxVersion(), content.isLiveVersion(), content.getData("/"));
             //full replace code end
             boolean checkScripting = type.hasScriptedAssignments();
             FxScriptBinding binding = null;
             ScriptingEngine scripting = null;
             if (checkScripting) {
                 scripting = EJBLookup.getScriptingEngine();
                 binding = new FxScriptBinding();
                 binding.setVariable("content", content);
             }
             //delta-deletes:
             for (FxDelta.FxDeltaChange change : delta.getRemoves()) {
                 if (checkScripting)
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.BeforeDataChangeDelete)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 if (!change.getOriginalData().isSystemInternal())
                     deleteDetailData(con, sql, pk, change.getOriginalData());
                 if (checkScripting)
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.AfterDataChangeDelete)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
             }
 
             //delta-updates:
             List<FxDelta.FxDeltaChange> updatesRemaining = new ArrayList<FxDelta.FxDeltaChange>(delta.getUpdates());
 
             while (updatesRemaining.size() > 0) {
                 FxDelta.FxDeltaChange change = updatesRemaining.get(0);
                 //noinspection CaughtExceptionImmediatelyRethrown
                 try {
                     if (checkScripting)
                         for (long scriptId : change.getOriginalData().getAssignment().
                                 getScriptMapping(FxScriptEvent.BeforeDataChangeUpdate)) {
                             binding.setVariable("change", change);
                             scripting.runScript(scriptId, binding);
                         }
                     if (!change.getOriginalData().isSystemInternal()) {
                         if (change.isGroup()) {
                             if (change.isPositionChange() && !change.isDataChange()) {
                                 //groups can only change position
                                 updatePropertyData(change, null, null, con, sql, pk, null);
                             }
                         } else {
                             FxProperty prop = env.getProperty(((FxPropertyData) change.getNewData()).getPropertyId());
                             if (!change._isUpdateable()) {
                                 deleteDetailData(con, sql, pk, change.getOriginalData());
                                 insertPropertyData(prop, content.getData("/"), con, sql, pk,
                                         ((FxPropertyData) change.getNewData()),
                                         content.isMaxVersion(), content.isLiveVersion());
                             } else {
                                 updatePropertyData(change, prop, content.getData("/"), con, sql, pk,
                                         ((FxPropertyData) change.getNewData()));
                             }
                             //check if the property changed is a FQN
                             if (prop.getId() == fqnPropertyId) {
                                 FxValue val = ((FxPropertyData) change.getNewData()).getValue();
                                 if (!val.isEmpty() && val instanceof FxString)
                                     StorageManager.getTreeStorage().syncFQNName(con, pk.getId(), content.isMaxVersion(), content.isLiveVersion(), (String) val.getBestTranslation());
                             }
                         }
                     }
                     if (checkScripting)
                         for (long scriptId : change.getOriginalData().getAssignment().
                                 getScriptMapping(FxScriptEvent.AfterDataChangeUpdate)) {
                             binding.setVariable("change", change);
                             scripting.runScript(scriptId, binding);
                         }
                     updatesRemaining.remove(0);
                 } catch (SQLException e) {
                     change._increaseRetries();
                     if (change._getRetryCount() > 100)
                         throw e;
                     updatesRemaining.remove(0);
                     updatesRemaining.add(change); //add as last
                 }
             }
 
             //delta-adds:
             for (FxDelta.FxDeltaChange change : delta.getAdds()) {
                 if (checkScripting)
                     for (long scriptId : change.getNewData().getAssignment().
                             getScriptMapping(FxScriptEvent.BeforeDataChangeAdd)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 if (!change.getNewData().isSystemInternal()) {
                     if (change.isGroup())
                         insertGroupData(con, sql, pk, (FxGroupData) change.getNewData(), content.isMaxVersion(), content.isLiveVersion());
                     else
                         insertPropertyData(env.getProperty(((FxPropertyData) change.getNewData()).getPropertyId()),
                                 content.getData("/"), con, sql, pk, ((FxPropertyData) change.getNewData()),
                                 content.isMaxVersion(), content.isLiveVersion());
                 }
                 if (checkScripting)
                     for (long scriptId : change.getNewData().getAssignment().
                             getScriptMapping(FxScriptEvent.AfterDataChangeAdd)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
             }
 
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
             if (delta.isInternalPropertyChanged()) {
                 updateStepDependencies(con, content.getPk().getId(), content.getPk().getVersion(), env, type, content.getStepId());
                 fixContentVersionStats(con, content.getPk().getId());
             }
             content.resolveBinaryPreview();
             if (original.getBinaryPreviewId() != content.getBinaryPreviewId() ||
                     original.getBinaryPreviewACL() != content.getBinaryPreviewACL())
                 updateContentBinaryEntry(con, pk, content.getBinaryPreviewId(), content.getBinaryPreviewACL());
             enableDetailUniqueChecks(con);
             LifeCycleInfoImpl.updateLifeCycleInfo(TBL_CONTENT, "ID", "VER",
                     content.getPk().getId(), content.getPk().getVersion(), false, false);
 
             if (type.isTrackHistory()) {
                 HistoryTrackerEngine tracker = EJBLookup.getHistoryTrackerEngine();
                 XStream xs = ConversionEngine.getXStream();
                 for (FxDelta.FxDeltaChange add : delta.getAdds())
                     tracker.track(type, pk,
                             add.getNewData().isGroup() ? null :
                                     xs.toXML(((FxPropertyData) add.getNewData()).getValue()),
                             "history.content.data.add", add.getXPath());
                 for (FxDelta.FxDeltaChange remove : delta.getRemoves())
                     tracker.track(type, pk,
                             remove.getOriginalData().isGroup() ? null :
                                     xs.toXML(((FxPropertyData) remove.getOriginalData()).getValue()),
                             "history.content.data.removed", remove.getXPath());
                 for (FxDelta.FxDeltaChange update : delta.getUpdates()) {
                     if (update.isPositionChangeOnly())
                         tracker.track(type, pk,
                                 null, "history.content.data.update.posOnly", update.getXPath(),
                                 update.getOriginalData().getPos(), update.getNewData().getPos());
                     else if (update.isPositionChange())
                         tracker.track(type, pk,
                                 update.getNewData().isGroup() ? null :
                                         xs.toXML(((FxPropertyData) update.getNewData()).getValue()),
                                 "history.content.data.update.pos", update.getXPath(),
                                 update.getOriginalData().getPos(), update.getNewData().getPos());
                     else
                         tracker.track(type, pk,
                                 update.getNewData().isGroup() ? null :
                                         xs.toXML(((FxPropertyData) update.getNewData()).getValue()),
                                 "history.content.data.update", update.getXPath());
 
                 }
             }
         } catch (FxCreateException e) {
             throw new FxUpdateException(e);
         } catch (FxApplicationException e) {
             throw new FxUpdateException(e);
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (Exception e) {
             throw new FxUpdateException(LOG, e, "ex.content.save.error", pk, e);
         }
         return content.getPk();
     }
 
     private void enableDetailUniqueChecks(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("SET UNIQUE_CHECKS=1");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     private void disableDetailUniqueChecks(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate("SET UNIQUE_CHECKS=0");
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     protected void updateContentBinaryEntry(Connection con, FxPK pk, long binaryId, long binaryACL) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_MAIN_BINARY_UPDATE);
             ps.setLong(1, binaryId);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //quality
             ps.setLong(4, binaryACL);
             ps.setLong(5, pk.getId());
             ps.setInt(6, pk.getVersion());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * Update the main entry
      *
      * @param con     an open and valid connection
      * @param content content to create
      * @throws FxUpdateException on errors
      */
     protected void updateMainEntry(Connection con, FxContent content) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_MAIN_UPDATE);
             ps.setLong(18, content.getPk().getId());
             ps.setInt(19, content.getPk().getVersion());
             ps.setLong(1, content.getTypeId());
             ps.setLong(2, content.getAclId());
             ps.setLong(3, content.getStepId());
             ps.setInt(4, content.getVersion());
             ps.setInt(5, content.isLiveVersion() ? 1 : 0);
             ps.setBoolean(6, content.isMaxVersion());
             ps.setBoolean(7, content.isLiveVersion());
             ps.setBoolean(8, content.isActive());
             ps.setInt(9, (int) content.getMainLanguage());
             if (content.isRelation()) {
                 ps.setLong(10, content.getRelatedSource().getId());
                 ps.setInt(11, content.getRelatedSource().getVersion());
                 ps.setLong(12, content.getRelatedDestination().getId());
                 ps.setInt(13, content.getRelatedDestination().getVersion());
                 ps.setLong(14, content.getRelatedSourcePosition());
                 ps.setLong(15, content.getRelatedDestinationPosition());
             } else {
                 ps.setNull(10, java.sql.Types.NUMERIC);
                 ps.setNull(11, java.sql.Types.NUMERIC);
                 ps.setNull(12, java.sql.Types.NUMERIC);
                 ps.setNull(13, java.sql.Types.NUMERIC);
                 ps.setNull(14, java.sql.Types.NUMERIC);
                 ps.setNull(15, java.sql.Types.NUMERIC);
             }
             long userId = FxContext.get().getTicket().getUserId();
             ps.setLong(16, userId);
             ps.setLong(17, System.currentTimeMillis());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * Remove all detail entries of a distinct version
      *
      * @param con an open and valid connection
      * @param pk  primary key of the detail data
      * @throws FxUpdateException on errors
      */
     protected void removeDetailEntriesVersion(Connection con, FxPK pk) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_DATA_FT_REMOVE_VERSION);
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement(CONTENT_DATA_REMOVE_VERSION);
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxContentSecurityInfo getContentSecurityInfo(Connection con, FxPK pk) throws FxLoadException, FxNotFoundException {
         PreparedStatement ps = null;
         try {
             switch (pk.getVersion()) {
                 case FxPK.MAX:
                     ps = con.prepareStatement(SECURITY_INFO_MAXVER);
                     break;
                 case FxPK.LIVE:
                     ps = con.prepareStatement(SECURITY_INFO_LIVEVER);
                     break;
                 default:
                     ps = con.prepareStatement(SECURITY_INFO_VER);
                     ps.setInt(2, pk.getVersion());
             }
             ps.setLong(1, pk.getId());
             byte typePerm;
             int typeACL, contentACL, stepACL, previewACL;
             long previewId, typeId, ownerId, mandatorId;
             long[] propertyPerm;
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.content.notFound", pk);
             contentACL = rs.getInt(1);
             typeACL = rs.getInt(2);
             stepACL = rs.getInt(3);
             typePerm = rs.getByte(4);
             typeId = rs.getLong(5);
             previewId = rs.getLong(6);
             previewACL = rs.getInt(7);
             ownerId = rs.getLong(8);
             mandatorId = rs.getLong(9);
             if (rs.next())
                 throw new FxLoadException("ex.db.resultSet.tooManyRows");
             if ((typePerm & 0x02) == 0x02) {
                 //use property permissions
                 ps.close();
                 switch (pk.getVersion()) {
                     case FxPK.MAX:
                         ps = con.prepareStatement(SECURITY_INFO_PROP_MAXVER);
                         break;
                     case FxPK.LIVE:
                         ps = con.prepareStatement(SECURITY_INFO_PROP_LIVEVER);
                         break;
                     default:
                         ps = con.prepareStatement(SECURITY_INFO_PROP_VER);
                         ps.setInt(2, pk.getVersion());
                 }
                 ps.setLong(1, pk.getId());
                 ArrayList<Long> alPropACL = new ArrayList<Long>(10);
                 ResultSet rsProp = ps.executeQuery();
                 while (rsProp != null && rsProp.next())
                     alPropACL.add(rsProp.getLong(1));
                 propertyPerm = new long[alPropACL.size()];
                 int cnt = 0;
                 for (long acl : alPropACL)
                     propertyPerm[cnt++] = acl;
             } else
                 propertyPerm = new long[0];
             return new FxContentSecurityInfo(pk, ownerId, previewId, typeId, mandatorId, typePerm, typeACL, stepACL, contentACL, previewACL, propertyPerm);
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void contentRemove(Connection con, FxType type, FxPK pk) throws FxRemoveException {
         PreparedStatement ps = null;
         try {
             //sync with tree
             StorageManager.getTreeStorage().contentRemoved(con, pk.getId(), false);
             ps = con.prepareStatement(CONTENT_DATA_FT_REMOVE);
             ps.setLong(1, pk.getId());
             ps.executeUpdate();
             ps.close();
             List<Long> binaries = null;
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET);
             ps.setLong(1, pk.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_DATA_REMOVE);
             ps.setLong(1, pk.getId());
             ps.executeUpdate();
             ps.close();
             if (binaries != null) {
                 ps = con.prepareStatement(CONTENT_BINARY_REMOVE_ID);
                 for (Long id : binaries) {
                     ps.setLong(1, id);
                     try {
                         ps.executeUpdate();
                     } catch (SQLException e) {
                         //ok, might still be in use elsewhere
                     }
                 }
                 ps.close();
             }
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE);
             ps.setLong(1, pk.getId());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxApplicationException e) {
             throw new FxRemoveException(e);
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
         if (type.isTrackHistory())
             EJBLookup.getHistoryTrackerEngine().track(type, pk, null, "history.content.removed");
     }
 
     /**
      * {@inheritDoc}
      */
     public void contentRemoveVersion(Connection con, FxType type, FxPK pk) throws FxRemoveException, FxNotFoundException {
         FxContentVersionInfo cvi = getContentVersionInfo(con, pk.getId());
         if (!cvi.containsVersion(pk))
             return;
 
         int ver = pk.getVersion();
         if (!pk.isDistinctVersion())
             ver = cvi.getDistinctVersion(pk.getVersion());
         PreparedStatement ps = null;
         try {
             //if its the live version - sync with live tree
             if (cvi.hasLiveVersion() && cvi.getLiveVersion() == ver)
                 StorageManager.getTreeStorage().contentRemoved(con, pk.getId(), true);
             ps = con.prepareStatement(CONTENT_DATA_FT_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             ps.executeUpdate();
             ps.close();
             ps = con.prepareStatement(CONTENT_DATA_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             ps.executeUpdate();
             ps.close();
             String[] nodes = StorageManager.getTreeStorage().beforeContentVersionRemoved(con, pk.getId(), ver, cvi);
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             if (ps.executeUpdate() > 0)
                 fixContentVersionStats(con, pk.getId());
             StorageManager.getTreeStorage().afterContentVersionRemoved(nodes, con, pk.getId(), ver, cvi);
         } catch (SQLException e) {
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxApplicationException e) {
             throw new FxRemoveException(e);
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
         if (type.isTrackHistory())
             EJBLookup.getHistoryTrackerEngine().track(type, pk, null, "history.content.removed.version", ver);
     }
 
     /**
      * {@inheritDoc}
      */
     public int contentRemoveForType(Connection con, FxType type) throws FxRemoveException {
         PreparedStatement ps = null;
         try {
             //FX-96 - select all contents that are referenced from the tree
             ps = con.prepareStatement("SELECT DISTINCT c.ID FROM " + DatabaseConst.TBL_CONTENT + " c, " +
                     DatabaseConst.TBL_TREE + " te, " + DatabaseConst.TBL_TREE +
                     "_LIVE tl WHERE (te.REF=c.ID or tl.REF=c.ID) AND c.TDEF=?");
             ps.setLong(1, type.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next())
                 StorageManager.getTreeStorage().contentRemoved(con, rs.getLong(1), false);
             ps.close();
 
             ps = con.prepareStatement(CONTENT_DATA_FT_REMOVE_TYPE);
             ps.setLong(1, type.getId());
             ps.executeUpdate();
             ps.close();
 
             List<Long> binaries = null;
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_TYPE_GET);
             ps.setLong(1, type.getId());
             rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_DATA_REMOVE_TYPE);
             ps.setLong(1, type.getId());
             ps.executeUpdate();
             ps.close();
             if (binaries != null) {
                 ps = con.prepareStatement(CONTENT_BINARY_REMOVE_ID);
                 for (Long id : binaries) {
                     ps.setLong(1, id);
                     try {
                         ps.executeUpdate();
                     } catch (SQLException e) {
                         //ok, might still be in use elsewhere
                     }
                 }
                 ps.close();
             }
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE_TYPE);
             ps.setLong(1, type.getId());
             return ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxApplicationException e) {
             throw new FxRemoveException(e);
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPK> getPKsForType(Connection con, FxType type, boolean onePkPerInstance) throws FxDbException {
         PreparedStatement ps = null;
         List<FxPK> pks = new ArrayList<FxPK>(50);
         try {
             if (onePkPerInstance)
                 ps = con.prepareStatement(CONTENT_TYPE_PK_RETRIEVE_IDS);
             else
                 ps = con.prepareStatement(CONTENT_TYPE_PK_RETRIEVE_VERSIONS);
 
             ps.setLong(1, type.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (onePkPerInstance)
                     pks.add(new FxPK(rs.getLong(1)));
                 else
                     pks.add(new FxPK(rs.getLong(1), rs.getInt(2)));
             }
             return pks;
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void maintenance(Connection con) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_TRANSIT_CLEANUP);
             ps.setLong(1, System.currentTimeMillis());
             int count = ps.executeUpdate();
             if (count > 0)
                 LOG.info(count + " expired binary transit entries removed");
         } catch (SQLException e) {
             LOG.error(e, e);
         } finally {
             if (ps != null)
                 try {
                     ps.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public void prepareSave(Connection con, FxContent content) throws FxInvalidParameterException, FxDbException {
         // key: handle, value: [mimeType,metaData]
         Map<String, String[]> mimeMetaMap = new HashMap<String, String[]>(5);
         try {
             for (FxData data : content.getRootGroup().getChildren())
                 _prepareSave(mimeMetaMap, con, data);
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     private void _prepareSave(Map<String, String[]> mimeMetaMap, Connection con, FxData data) throws SQLException {
         if (data instanceof FxGroupData)
             for (FxData sub : ((FxGroupData) data).getChildren())
                 _prepareSave(mimeMetaMap, con, sub);
         else {
             if (!data.isEmpty() && ((FxPropertyData) data).getValue() instanceof FxBinary) {
                 FxBinary bin = (FxBinary) ((FxPropertyData) data).getValue();
                 for (long lang : bin.getTranslatedLanguages()) {
                     BinaryDescriptor bd = bin.getTranslation(lang);
                     if (!bd.isNewBinary())
                         continue;
                     if (mimeMetaMap.containsKey(bd.getHandle())) {
                         String[] mm = mimeMetaMap.get(bd.getHandle());
                         BinaryDescriptor bdNew = new BinaryDescriptor(bd.getHandle(), bd.getName(), bd.getSize(), mm[0], mm[1]);
                         bin.setTranslation(lang, bdNew);
                     } else {
                         BinaryDescriptor bdNew = identifyAndTransferTransitBinary(con, bd);
                         bin.setTranslation(lang, bdNew);
                         mimeMetaMap.put(bdNew.getHandle(), new String[]{bdNew.getMimeType(), bdNew.getMetadata()});
                     }
                 }
 
             }
         }
     }
 
     /**
      * Identifies a binary in the transit table and generates previews etc.
      *
      * @param con    an open and valid Connection
      * @param binary the binary to identify
      * @return BinaryDescriptor
      * @throws SQLException on errors
      */
     private BinaryDescriptor identifyAndTransferTransitBinary(Connection con, BinaryDescriptor binary) throws SQLException {
         PreparedStatement ps;
         //check if already identified
         if (!StringUtils.isEmpty(binary.getMetadata()))
             return binary;
         ps = con.prepareStatement(BINARY_TRANSIT_HEADER);
         ps.setString(1, binary.getHandle());
         ResultSet rs = ps.executeQuery();
         String mimeType = "unknown";
         if (rs != null && rs.next()) {
             byte[] header = null;
             try {
                 header = rs.getBlob(1).getBytes(1, 48);
             } catch (Throwable t) {
                 // ignore, header migth be smaller than 48
             }
             mimeType = FxMediaEngine.detectMimeType(header, binary.getName());
         }
         String metaData = "<empty/>";
 
         File binaryFile = null;
         File previewFile1 = null, previewFile2 = null, previewFile3 = null;
         InputStream in = null;
         FileOutputStream fos = null;
         FileInputStream pin1 = null, pin2 = null, pin3 = null;
         int[] dimensionsPreview1 = {0, 0};
         int[] dimensionsPreview2 = {0, 0};
         int[] dimensionsPreview3 = {0, 0};
         try {
             binaryFile = File.createTempFile("FXBIN_", "_TEMP");
             in = rs.getBlob(1).getBinaryStream();
             fos = new FileOutputStream(binaryFile);
             byte[] buffer = new byte[4096];
             int read;
             while ((read = in.read(buffer)) != -1)
                 fos.write(buffer, 0, read);
             fos.close();
             fos = null;
             in.close();
             in = null;
             boolean processed = false;
             boolean useDefaultPreview = true;
             int defaultId = BinaryDescriptor.SYS_UNKNOWN;
 
             FxScriptBinding binding;
             ScriptingEngine scripting = EJBLookup.getScriptingEngine();
             for (long script : scripting.getByScriptEvent(FxScriptEvent.BinaryPreviewProcess)) {
                 binding = new FxScriptBinding();
                 binding.setVariable("processed", processed);
                 binding.setVariable("useDefaultPreview", useDefaultPreview);
                 binding.setVariable("defaultId", defaultId);
                 binding.setVariable("mimeType", mimeType);
                 binding.setVariable("metaData", metaData);
                 binding.setVariable("binaryFile", binaryFile);
                 binding.setVariable("previewFile1", previewFile1);
                 binding.setVariable("previewFile2", previewFile2);
                 binding.setVariable("previewFile3", previewFile3);
                 binding.setVariable("dimensionsPreview1", dimensionsPreview1);
                 binding.setVariable("dimensionsPreview2", dimensionsPreview2);
                 binding.setVariable("dimensionsPreview3", dimensionsPreview3);
                 try {
                     FxScriptResult result = scripting.runScript(script, binding);
                     binding = result.getBinding();
                     processed = (Boolean) binding.getVariable("processed");
                     if (processed) {
                         useDefaultPreview = (Boolean) binding.getVariable("useDefaultPreview");
                         defaultId = (Integer) binding.getVariable("defaultId");
                         previewFile1 = (File) binding.getVariable("previewFile1");
                         previewFile2 = (File) binding.getVariable("previewFile2");
                         previewFile3 = (File) binding.getVariable("previewFile3");
                         dimensionsPreview1 = (int[]) binding.getVariable("dimensionsPreview1");
                         dimensionsPreview2 = (int[]) binding.getVariable("dimensionsPreview2");
                         dimensionsPreview3 = (int[]) binding.getVariable("dimensionsPreview3");
                         metaData = (String) binding.getVariable("metaData");
                         break;
                     }
                 } catch (Throwable e) {
                     LOG.error("Error running binary processing script: " + e.getMessage());
                 }
             }
             //check if values for preview are valid
             if (!useDefaultPreview) {
                 if (previewFile1 == null || !previewFile1.exists() ||
                         previewFile2 == null || !previewFile2.exists() ||
                         previewFile3 == null || !previewFile3.exists() ||
                         dimensionsPreview1 == null || dimensionsPreview1.length != 2 || dimensionsPreview1[0] < 0 || dimensionsPreview1[1] < 0 ||
                         dimensionsPreview1 == null || dimensionsPreview2.length != 2 || dimensionsPreview2[0] < 0 || dimensionsPreview2[1] < 0 ||
                         dimensionsPreview1 == null || dimensionsPreview3.length != 2 || dimensionsPreview3[0] < 0 || dimensionsPreview3[1] < 0) {
                     LOG.warn("Invalid preview parameters! Setting to default/unknown!");
                     useDefaultPreview = true;
                     defaultId = BinaryDescriptor.SYS_UNKNOWN;
                 }
             } else {
                 //only negative values are allowed
                 if (defaultId >= 0) {
                     defaultId = BinaryDescriptor.SYS_UNKNOWN;
                     LOG.warn("Only default preview id's that are negative and defined in BinaryDescriptor as constants are allowed!");
                 }
             }
 
             if (!useDefaultPreview) {
                 ps.close();
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS);
                 pin1 = new FileInputStream(previewFile1);
                 pin2 = new FileInputStream(previewFile2);
                 pin3 = new FileInputStream(previewFile3);
                 ps.setBinaryStream(1, pin1, (int) previewFile1.length());
                 ps.setInt(2, dimensionsPreview1[0]);
                 ps.setInt(3, dimensionsPreview1[1]);
                 ps.setBinaryStream(4, pin2, (int) previewFile2.length());
                 ps.setInt(5, dimensionsPreview2[0]);
                 ps.setInt(6, dimensionsPreview2[1]);
                 ps.setBinaryStream(7, pin3, (int) previewFile3.length());
                 ps.setInt(8, dimensionsPreview3[0]);
                 ps.setInt(9, dimensionsPreview3[1]);
                 ps.setInt(10, (int) previewFile1.length());
                 ps.setInt(11, (int) previewFile2.length());
                 ps.setInt(12, (int) previewFile3.length());
                 ps.setString(13, binary.getHandle());
                 ps.executeUpdate();
             } else {
                 ps.close();
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS_REF);
                 ps.setLong(1, defaultId);
                 ps.setString(2, binary.getHandle());
                 ps.executeUpdate();
             }
         } catch (IOException e) {
             LOG.error("Stream reading failed:" + e.getMessage(), e);
 //        } catch (XMLStreamException e) {
 //            LOG.error("XMLStream processing failed: " + e.getMessage(), e);
         } finally {
             try {
                 if (fos != null)
                     fos.close();
                 if (in != null)
                     in.close();
                 if (pin1 != null)
                     pin1.close();
                 if (pin2 != null)
                     pin2.close();
                 if (pin3 != null)
                     pin3.close();
             } catch (IOException e) {
                 LOG.error("Stream closing failed: " + e.getMessage(), e);
             }
             if (binaryFile != null && !binaryFile.delete())
                 binaryFile.deleteOnExit();
             if (previewFile1 != null && !previewFile1.delete())
                 previewFile1.deleteOnExit();
             if (previewFile2 != null && !previewFile2.delete())
                 previewFile2.deleteOnExit();
             if (previewFile3 != null && !previewFile3.delete())
                 previewFile3.deleteOnExit();
             if (ps != null)
                 ps.close();
         }
         //TODO: if we have a word, excel or powerpoint extract all possible infos and put them into the metaData
         return new BinaryDescriptor(binary.getHandle(), binary.getName(), binary.getSize(), mimeType, metaData);
     }
 
     /**
      * {@inheritDoc}
      */
     public void storeBinary(Connection con, long id, int version, int quality, String name, long length, InputStream binary) throws FxApplicationException {
 //        System.out.println("INSTALLING ["+name+"] length="+length);
         try {
             BinaryUploadPayload payload = FxStreamUtils.uploadBinary(length, binary);
             BinaryDescriptor desc = new BinaryDescriptor(payload.getHandle(), name, length, null, null);
             desc = identifyAndTransferTransitBinary(con, desc);
             binaryTransit(con, desc, id, version, quality);
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateBinaryPreview(Connection con, long id, int version, int quality, int preview, int width, int height, long length, InputStream binary) {
         //TODO: code me!
     }
 
     /**
      * Check all unique constraints for an instance
      *
      * @param con    an open and valid connection
      * @param env    environment
      * @param sql    StringBuilder for performance
      * @param pk     primary key of the affected instance
      * @param typeId affected FxType
      * @throws FxApplicationException on errors
      */
     private void checkUniqueConstraints(Connection con, FxEnvironment env, StringBuilder sql, FxPK pk, long typeId) throws FxApplicationException {
         FxType type = env.getType(typeId);
         if (!type.hasUniqueProperties())
             return;
         List<FxProperty> uniques = type.getUniqueProperties();
         if (sql == null)
             sql = new StringBuilder(500);
         else
             sql.setLength(0);
         try {
             for (FxProperty prop : uniques) {
                 sql.setLength(0);
                 uniqueConditionsMet(con, env, sql, prop.getUniqueMode(), prop, typeId, pk, true);
             }
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * Check if unique constraints are met
      *
      * @param con            an open and valid Connection
      * @param env            environment
      * @param sql            a StringBuilder instance
      * @param mode           UniqueMode
      * @param prop           the propery to check
      * @param typeId         type to check
      * @param pk             primary key (optional)
      * @param throwException should an exception be thrown if conditions are not met?
      * @return conditions met
      * @throws SQLException           on errors
      * @throws FxApplicationException on errors
      */
     private boolean uniqueConditionsMet(Connection con, FxEnvironment env, StringBuilder sql, UniqueMode mode,
                                         FxProperty prop, long typeId, FxPK pk, boolean throwException)
             throws SQLException, FxApplicationException {
         String typeChecks = null;
         sql.setLength(0);
         switch (mode) {
             case Global:
                 sql.append("SELECT tcd.XPATHMULT, COUNT(DISTINCT ccd.ID) FROM ").append(TBL_CONTENT_DATA).
                         append(" ccd, ").append(TBL_CONTENT_DATA).append(" tcd WHERE ccd.TPROP=").
                         append(prop.getId()).append(" AND ccd.TPROP=tcd.TPROP AND ccd.ID<>tcd.ID AND tcd.ID=").append(pk.getId()).
                         append(" AND ccd.LANG=tcd.LANG");
                 break;
             case DerivedTypes:
                 //gen list of parent and derived types
                 typeChecks = buildTypeHierarchy(env.getType(typeId));
             case Type:
                 if (typeChecks == null)
                     typeChecks = "" + typeId;
                 sql.append("SELECT tcd.XPATHMULT, COUNT(DISTINCT ccd.ID) FROM ").append(TBL_CONTENT_DATA).
                         append(" ccd, ").append(TBL_CONTENT_DATA).append(" tcd, ").append(TBL_CONTENT).
                         append(" cc, ").append(TBL_CONTENT).
                         append(" tc WHERE cc.ID=ccd.ID AND tc.ID=tcd.ID AND cc.TDEF IN (").
                         append(typeChecks).append(") AND tc.TDEF IN (").append(typeChecks).
                         append(") AND ccd.TPROP=").append(prop.getId()).
                         append(" AND ccd.TPROP=tcd.TPROP AND ccd.LANG=tcd.LANG").
                         //prevent checks across versions
                                 append(" AND NOT(ccd.ID=tcd.ID AND ccd.VER<>tcd.VER)").
                         //prevent self-references
                                 append(" AND NOT(ccd.ID=tcd.ID AND ccd.VER=tcd.VER AND ccd.ASSIGN=tcd.ASSIGN AND tcd.XMULT=ccd.XMULT)");
                 break;
             case Instance:
                 sql.append("SELECT tcd.XPATHMULT FROM ").append(TBL_CONTENT_DATA).append(" ccd, ").
                         append(TBL_CONTENT_DATA).append(" tcd WHERE ccd.TPROP=").append(prop.getId()).
                         append(" AND ccd.TPROP=tcd.TPROP AND ccd.ID=tcd.ID AND ccd.VER=tcd.VER AND ccd.XPATHMULT<>tcd.XPATHMULT");
                 if (pk != null)
                     sql.append(" AND tcd.ID=").append(pk.getId());
                 sql.append(" AND ccd.LANG=tcd.LANG");
                 break;
         }
         if (sql.length() == 0)
             return true;
         addColumnComparator(sql, prop, "ccd", "tcd");
         sql.append(" GROUP BY tcd.XPATHMULT");
         //noinspection CaughtExceptionImmediatelyRethrown
         try {
             doCheckUniqueConstraint(con, sql.toString(), prop.getUniqueMode());
         } catch (FxApplicationException e) {
             LOG.warn("SQL:\n" + sql);
             if (throwException)
                 throw e;
             return false;
         }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean uniqueConditionValid(Connection con, UniqueMode mode, FxProperty prop, long typeId, FxPK pk) {
         try {
             return uniqueConditionsMet(con, CacheAdmin.getEnvironment(), new StringBuilder(500), mode, prop, typeId, pk, false);
         } catch (SQLException e) {
             throw new FxApplicationException(e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateMultilanguageSettings(Connection con, long assignmentId, boolean orgMultiLang,
                                             boolean newMultiLang, long defaultLanguage)
             throws FxUpdateException, SQLException {
         if (orgMultiLang == newMultiLang)
             return;
         PreparedStatement ps = null;
         try {
             if (!orgMultiLang && newMultiLang) {
                 //Single to Multi: lang=default language
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA + " SET LANG=? WHERE ASSIGN=?");
                 ps.setLong(1, defaultLanguage);
                 ps.setLong(2, assignmentId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA_FT + " SET LANG=? WHERE ASSIGN=?");
                 ps.setLong(1, defaultLanguage);
                 ps.setLong(2, assignmentId);
                 ps.executeUpdate();
             } else {
                 //Multi to Single: lang=system, values of the def. lang. are used, if other translations exist an exception will be raised
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA + " SET LANG=? WHERE LANG=? AND ASSIGN=?");
                 ps.setLong(1, FxLanguage.SYSTEM_ID);
                 ps.setLong(2, defaultLanguage);
                 ps.setLong(3, assignmentId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA_FT + " SET LANG=? WHERE LANG=? AND ASSIGN=?");
                 ps.setLong(1, FxLanguage.SYSTEM_ID);
                 ps.setLong(2, defaultLanguage);
                 ps.setLong(3, assignmentId);
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE ASSIGN=? AND LANG<>?");
                 ps.setLong(1, assignmentId);
                 ps.setLong(2, FxLanguage.SYSTEM_ID);
                 ResultSet rs = ps.executeQuery();
                 long count = 0;
                 if (rs != null && rs.next())
                     if ((count = rs.getLong(1)) > 0)
                         throw new FxUpdateException("ex.content.update.multi2single.contentExist", CacheAdmin.getEnvironment().getAssignment(assignmentId).getXPath(), count);
             }
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Helper to build a comma seperated list of all parent and child types and the current type
      *
      * @param type current type to examine
      * @return comma seperated list of all parent and child types and the current type
      */
     private String buildTypeHierarchy(FxType type) {
         StringBuilder th = new StringBuilder(100);
         FxType parent = type.getParent();
         th.append(type.getId());
         while (parent != null) {
             th.append(",").append(parent.getId());
             parent = parent.getParent();
         }
         buildTypeChildren(th, type);
         return th.toString();
     }
 
     /**
      * Build a list of all derived types and the current type
      *
      * @param th   StringBuilder that should contain the result
      * @param type current type
      */
     private void buildTypeChildren(StringBuilder th, FxType type) {
         for (FxType child : type.getDerivedTypes()) {
             th.append(',').append(child.getId());
             buildTypeChildren(th, child);
         }
     }
 
     private void doCheckUniqueConstraint(Connection con, String sql, UniqueMode uniqueMode) throws SQLException, FxApplicationException {
         Statement s = null;
         try {
             s = con.createStatement();
             ResultSet rs = s.executeQuery(sql);
             if (rs != null && rs.next()) {
                 if (uniqueMode == UniqueMode.Instance || rs.getInt(2) > 0)
                     throw new FxConstraintViolationException("ex.content.contraint.unique.xpath", rs.getString(1), uniqueMode).setAffectedXPath(rs.getString(1));
             }
         } finally {
             if (s != null)
                 try {
                     s.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * Compare a property for two database aliases
      *
      * @param sql       StringBuilder to append the comparison to
      * @param prop      propery to compare
      * @param compAlias compare alias
      * @param ownAlias  own alias
      */
     private void addColumnComparator(StringBuilder sql, FxProperty prop, String compAlias, String ownAlias) {
         String ucol = getUppercaseColumn(prop);
         if (ucol == null)
             for (String col : getColumns(prop))
                 sql.append(" AND ").append(compAlias).append(".").append(col).append("=").append(ownAlias).append(".").append(col);
         else
             sql.append(" AND ").append(compAlias).append(".").append(ucol).append("=").append(ownAlias).append(".").append(ucol);
     }
 
     /**
      * {@inheritDoc}
      */
     public int getReferencedContentCount(Connection con, long id) throws FxDbException {
         Statement s = null;
         int count = 0;
         try {
             s = con.createStatement();
             //references within contents
             ResultSet rs = s.executeQuery("SELECT DISTINCT ID FROM " + TBL_CONTENT_DATA + " WHERE FREF=" + id);
             while (rs != null && rs.next())
                 count++;
             //Edit tree references
             rs = s.executeQuery("SELECT DISTINCT ID FROM " + TBL_TREE + " WHERE REF=" + id);
             while (rs != null && rs.next())
                 count++;
             //Live tree references
             rs = s.executeQuery("SELECT DISTINCT ID FROM " + TBL_TREE + "_LIVE WHERE REF=" + id);
             while (rs != null && rs.next())
                 count++;
             //Contact Data references
             rs = s.executeQuery("SELECT DISTINCT ID FROM " + TBL_ACCOUNTS + " WHERE CONTACT_ID=" + id);
             while (rs != null && rs.next())
                 count++;
             //Briefcase references
             rs = s.executeQuery("SELECT DISTINCT BRIEFCASE_ID FROM " + TBL_BRIEFCASE_DATA + " WHERE ID=" + id);
             while (rs != null && rs.next())
                 count++;
             return count;
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             if (s != null)
                 try {
                     s.close();
                 } catch (SQLException e) {
                     //ignore
                 }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateXPath(Connection con, long assignmentId, String originalXPath, String newXPath) throws FxUpdateException, FxInvalidParameterException {
         LOG.info("Updating all instances from [" + originalXPath + "] to [" + newXPath + "]...");
         PreparedStatement psRead = null, psWrite = null;
 //        int count = 0;
         List<XPathElement> xorg = XPathElement.split(originalXPath);
         List<XPathElement> xnew = XPathElement.split(newXPath);
         if (xorg.size() != xnew.size())
             throw new FxInvalidParameterException("newXPath", "ex.content.xpath.update.mismatch.size", originalXPath, newXPath);
         try {
             //                                    1     2  3   4    5   6
             psRead = con.prepareStatement("SELECT XMULT,ID,VER,LANG,POS,TPROP FROM " + TBL_CONTENT_DATA + " WHERE ASSIGN=?");
             //                                                                        1           2             3          4         5          6         7           8            9
             psWrite = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA + " SET XPATH=?,XPATHMULT=?,PARENTXPATH=? WHERE ID=? AND VER=? AND LANG=? AND POS=? AND XMULT=? AND ASSIGN=?");
 
             psRead.setLong(1, assignmentId);
             ResultSet rs = psRead.executeQuery();
             boolean isGroup;
             while (rs != null && rs.next()) {
                 rs.getLong(6);
                 isGroup = rs.wasNull();
                 int[] idx = FxArrayUtils.toIntArray(rs.getString(1), ',');
                 for (int i = 0; i < xnew.size(); i++)
                     xnew.get(i).setIndex(idx[i]);
                 String xm = XPathElement.toXPath(xnew);
                 psWrite.setString(1, newXPath + (isGroup ? "/" : ""));
                 psWrite.setString(2, xm + (isGroup ? "/" : ""));
                 String parentXP = xm.substring(0, xm.lastIndexOf('/'));
                 if ("".equals(parentXP))
                     parentXP = "/";
                 psWrite.setString(3, parentXP);
                 psWrite.setLong(4, rs.getLong(2));
                 psWrite.setInt(5, rs.getInt(3));
                 psWrite.setInt(6, rs.getInt(4));
                 psWrite.setInt(7, rs.getInt(5));
                 psWrite.setString(8, rs.getString(1));
                 psWrite.setLong(9, assignmentId);
                 psWrite.executeUpdate();
             }
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (psRead != null)
                     psRead.close();
             } catch (SQLException e) {
                 //ignore
             }
             try {
                 if (psWrite != null)
                     psWrite.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 }
 
