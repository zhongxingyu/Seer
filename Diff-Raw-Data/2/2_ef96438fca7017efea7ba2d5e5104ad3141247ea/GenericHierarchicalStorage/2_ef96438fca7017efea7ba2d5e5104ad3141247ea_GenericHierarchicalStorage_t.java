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
 import com.flexive.core.DatabaseConst;
 import static com.flexive.core.DatabaseConst.*;
 import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.core.conversion.ConversionEngine;
 import com.flexive.core.flatstorage.FxFlatStorage;
 import com.flexive.core.flatstorage.FxFlatStorageLoadColumn;
 import com.flexive.core.flatstorage.FxFlatStorageLoadContainer;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.FulltextIndexer;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.binary.BinaryInputStream;
 import com.flexive.core.storage.binary.BinaryStorage;
 import com.flexive.extractor.htmlExtractor.HtmlExtractor;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.*;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.HistoryTrackerEngine;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLPermission;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.*;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 import com.flexive.stream.ServerLocation;
 import com.google.common.collect.Lists;
 import com.thoughtworks.xstream.XStream;
 import org.apache.commons.lang.ArrayUtils;
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
 
     private static final Log LOG = LogFactory.getLog(GenericHierarchicalStorage.class);
 
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
             //         10           11          12           13           14           15            16          17
             "RELSRC_ID=?,RELSRC_VER=?,RELDST_ID=?,RELDST_VER=?,RELSRC_POS=?,RELDST_POS=?,MODIFIED_BY=?,MODIFIED_AT=? " +
             //        18    19
             "WHERE ID=? AND VER=?";
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
             " WHERE ID=? AND VER=? ORDER BY XDEPTH ASC, POS ASC, ASSIGN ASC, XMULT ASC";
 
     //single insert statement for content data
     protected static final String CONTENT_DATA_INSERT = "INSERT INTO " + TBL_CONTENT_DATA +
             //1  2   3   4    5      6     7         8     9      10          11        12         13      14      15    16          17
             "(ID,VER,POS,LANG,ASSIGN,XPATH,XPATHMULT,XMULT,XINDEX,PARENTXMULT,ISMAX_VER,ISLIVE_VER,ISGROUP,ISMLDEF,TPROP,PARENTXPATH,XDEPTH," +
             //18     19   20     21     22    23    24     25   26      27
             "FSELECT,FREF,FDATE1,FDATE2,FBLOB,FCLOB,UFCLOB,FINT,FBIGINT,FTEXT1024," +
             //28        29      30     31
             "UFTEXT1024,FDOUBLE,FFLOAT,FBOOL)" +
             //                              ISGROUP 17
             "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
             "?,?,?,?,?,?,?,?,?,?," +
             "?,?,?,?)";
 
     //single update statement for content data
     protected static final String CONTENT_DATA_UPDATE = "UPDATE " + TBL_CONTENT_DATA +
             //        1         2
             " SET POS=?,ISMLDEF=?," +
             //       3      4        5        6       7       8        9
             "FSELECT=?,FREF=?,FDATE1=?,FDATE2=?,FBLOB=?,FCLOB=?,UFCLOB=?," +
             //    10        11          12           13        14       15      16
             "FINT=?,FBIGINT=?,FTEXT1024=?,UFTEXT1024=?,FDOUBLE=?,FFLOAT=?,FBOOL=? " +
             //        17        18         19           20              21
             "WHERE ID=? AND VER=? AND LANG=? AND ASSIGN=? AND XPATHMULT=?";
 
     //                                                                                                       1         2
 //    protected static final String CONTENT_DATA_REMOVE_VERSION = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID=? AND VER=?";
 
     //security info main query
     protected static final String SECURITY_INFO_MAIN = "SELECT DISTINCT c.ACL, t.ACL, s.ACL, t.SECURITY_MODE, t.ID, c.DBIN_ID, c.DBIN_ACL, c.CREATED_BY, c.MANDATOR, c.ver FROM " +
             TBL_CONTENT + " c, " + TBL_STRUCT_TYPES + " t, " + TBL_STEP + " s WHERE c.ID=? AND ";
     protected static final String SECURITY_INFO_WHERE = " AND t.ID=c.TDEF AND s.ID=c.STEP";
     protected static final String SECURITY_INFO_VER = SECURITY_INFO_MAIN + "c.VER=?" + SECURITY_INFO_WHERE;
     protected static final String SECURITY_INFO_MAXVER = SECURITY_INFO_MAIN + "c.VER=c.MAX_VER" + SECURITY_INFO_WHERE;
     protected static final String SECURITY_INFO_LIVEVER = SECURITY_INFO_MAIN + "c.VER=c.LIVE_VER" + SECURITY_INFO_WHERE;
 
     //calculate max_ver and live_ver for a content instance
     protected static final String CONTENT_VER_CALC = "SELECT MAX(VER) AS MAX_VER, COALESCE((SELECT s.VER FROM " +
             TBL_CONTENT + " s WHERE s.STEP=(SELECT w.ID FROM " + TBL_STEP + " w, " + TBL_STRUCT_TYPES + " t WHERE w.STEPDEF=" +
             StepDefinition.LIVE_STEP_ID + " AND w.WORKFLOW=t.WORKFLOW AND t.ID=c.TDEF) AND s.ID=c.ID),-1) AS LIVE_VER FROM " + TBL_CONTENT +
             " c WHERE c.ID=? GROUP BY c.ID,c.TDEF";
     protected static final String CONTENT_VER_UPDATE_1 = "UPDATE " + TBL_CONTENT + " SET MAX_VER=?, LIVE_VER=? WHERE ID=?";
     protected static final String CONTENT_VER_UPDATE_2 = "UPDATE " + TBL_CONTENT + " SET ISMAX_VER=(MAX_VER=VER), ISLIVE_VER=(LIVE_VER=VER) WHERE ID=?";
     protected static final String CONTENT_VER_UPDATE_3 = "UPDATE " + TBL_CONTENT_DATA + " SET ISMAX_VER=(VER=?), ISLIVE_VER=(VER=?) WHERE ID=?";
 
     protected static final String CONTENT_STEP_GETVERSIONS = "SELECT VER FROM " + TBL_CONTENT + " WHERE STEP=? AND ID=? AND VER<>?";
     protected static final String CONTENT_STEP_DEPENDENCIES = "UPDATE " + TBL_CONTENT + " SET STEP=? WHERE STEP=? AND ID=? AND VER<>?";
 
 
     protected static final String CONTENT_REFERENCE_LIVE = "SELECT VER, ACL, STEP, TDEF, CREATED_BY FROM " + TBL_CONTENT + " WHERE ID=? AND ISLIVE_VER=?";
     protected static final String CONTENT_REFERENCE_MAX = "SELECT VER, ACL, STEP, TDEF, CREATED_BY FROM " + TBL_CONTENT + " WHERE ID=? AND ISMAX_VER=?";
     protected static final String CONTENT_REFERENCE_CAPTION = "SELECT FTEXT1024 FROM " + TBL_CONTENT_DATA + " WHERE ID=? AND VER=? AND TPROP=?";
     protected static final String CONTENT_REFERENCE_BYTYPE = "SELECT COUNT(DISTINCT d.ID) FROM " + TBL_CONTENT + " c, " +
             TBL_CONTENT_DATA + " d, " + TBL_STRUCT_ASSIGNMENTS + " a, " + TBL_STRUCT_PROPERTIES + " p " +
             "WHERE c.TDEF=a.TYPEDEF AND a.APROPERTY=p.ID AND p.REFTYPE=? AND d.ASSIGN=a.ID AND d.TPROP=p.ID";
 
     //getContentVersionInfo() statement
     protected static final String CONTENT_VER_INFO = "SELECT ID, VER, MAX_VER, LIVE_VER, CREATED_BY, CREATED_AT, MODIFIED_BY, MODIFIED_AT, STEP FROM " + TBL_CONTENT + " WHERE ID=?";
 
     protected static final String CONTENT_MAIN_REMOVE = "DELETE FROM " + TBL_CONTENT + " WHERE ID=?";
     protected static final String CONTENT_DATA_REMOVE = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID=?";
     protected static final String SQL_WHERE_VER = " AND VER=?";
     protected static final String CONTENT_MAIN_REMOVE_VER = CONTENT_MAIN_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_DATA_REMOVE_VER = CONTENT_DATA_REMOVE + SQL_WHERE_VER;
 
     protected static final String CONTENT_MAIN_REMOVE_TYPE = "DELETE FROM " + TBL_CONTENT + " WHERE TDEF=?";
     protected static final String CONTENT_DATA_REMOVE_TYPE = "DELETE FROM " + TBL_CONTENT_DATA + " WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
 
     protected static final String CONTENT_TYPE_PK_RETRIEVE_VERSIONS = "SELECT DISTINCT ID,VER FROM " + TBL_CONTENT + " WHERE TDEF=? ORDER BY ID,VER";
     protected static final String CONTENT_TYPE_PK_RETRIEVE_IDS = "SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=? ORDER BY ID";
     protected static final String CONTENT_GET_TYPE = "SELECT DISTINCT TDEF FROM " + TBL_CONTENT + " WHERE ID=?";
 
     protected static final String CONTENT_ACLS_LOAD = "SELECT ACL FROM " + TBL_CONTENT_ACLS + " WHERE ID=? AND VER=?";
     protected static final String CONTENT_ACLS_CLEAR = "DELETE FROM " + TBL_CONTENT_ACLS + " WHERE ID=? AND VER=?";
     protected static final String CONTENT_ACL_INSERT = "INSERT INTO " + TBL_CONTENT_ACLS + "(ID, VER, ACL) VALUES (?, ?, ?)";
 
     //prepared statement positions
     protected final static int INSERT_LANG_POS = 4;
     protected final static int INSERT_ISDEF_LANG_POS = 14;
     //position of first value
     protected final static int INSERT_VALUE_POS = 17;
     //position of last value
     protected final static int INSERT_END_POS = 31;
     //position of the id field (start of the where clause) for detail updates
     protected final static int UPDATE_ID_POS = 17;
     protected final static int UPDATE_POS_POS = 1;
     protected final static int UPDATE_MLDEF_POS = 2;
 
     // propertyId -> column names
     protected static final HashMap<Long, String[]> mainColumnHash;
     // dataType -> column names
     protected static final HashMap<FxDataType, String[]> detailColumnNameHash;
     // dataType -> column insert positions
     protected static final HashMap<FxDataType, int[]> detailColumnInsertPosHash;
     // dataType -> column update positions
     protected static final HashMap<FxDataType, int[]> detailColumnUpdatePosHash;
 
     static {
         detailColumnNameHash = new HashMap<FxDataType, String[]>(20);
         detailColumnInsertPosHash = new HashMap<FxDataType, int[]>(20);
         detailColumnUpdatePosHash = new HashMap<FxDataType, int[]>(20);
         detailColumnNameHash.put(FxDataType.Binary, array("FBLOB"));
         detailColumnInsertPosHash.put(FxDataType.Binary, array(22));
         detailColumnUpdatePosHash.put(FxDataType.Binary, array(7));
         detailColumnNameHash.put(FxDataType.Boolean, array("FBOOL"));
         detailColumnInsertPosHash.put(FxDataType.Boolean, array(31));
         detailColumnUpdatePosHash.put(FxDataType.Boolean, array(16));
         detailColumnNameHash.put(FxDataType.Date, array("FDATE1"));
         detailColumnInsertPosHash.put(FxDataType.Date, array(20));
         detailColumnUpdatePosHash.put(FxDataType.Date, array(5));
         detailColumnNameHash.put(FxDataType.DateRange, array("FDATE1", "FDATE2"));
         detailColumnInsertPosHash.put(FxDataType.DateRange, array(20, 21));
         detailColumnUpdatePosHash.put(FxDataType.DateRange, array(5, 6));
         detailColumnNameHash.put(FxDataType.DateTime, array("FDATE1"));
         detailColumnInsertPosHash.put(FxDataType.DateTime, array(20));
         detailColumnUpdatePosHash.put(FxDataType.DateTime, array(5));
         detailColumnNameHash.put(FxDataType.DateTimeRange, array("FDATE1", "FDATE2"));
         detailColumnInsertPosHash.put(FxDataType.DateTimeRange, array(20, 21));
         detailColumnUpdatePosHash.put(FxDataType.DateTimeRange, array(5, 6));
         detailColumnNameHash.put(FxDataType.Double, array("FDOUBLE"));
         detailColumnInsertPosHash.put(FxDataType.Double, array(29));
         detailColumnUpdatePosHash.put(FxDataType.Double, array(14));
         detailColumnNameHash.put(FxDataType.Float, array("FFLOAT"));
         detailColumnInsertPosHash.put(FxDataType.Float, array(30));
         detailColumnUpdatePosHash.put(FxDataType.Float, array(15));
         detailColumnNameHash.put(FxDataType.LargeNumber, array("FBIGINT"));
         detailColumnInsertPosHash.put(FxDataType.LargeNumber, array(26));
         detailColumnUpdatePosHash.put(FxDataType.LargeNumber, array(11));
         detailColumnNameHash.put(FxDataType.Number, array("FINT"));
         detailColumnInsertPosHash.put(FxDataType.Number, array(25));
         detailColumnUpdatePosHash.put(FxDataType.Number, array(10));
         detailColumnNameHash.put(FxDataType.Reference, array("FREF"));
         detailColumnInsertPosHash.put(FxDataType.Reference, array(19));
         detailColumnUpdatePosHash.put(FxDataType.Reference, array(4));
         detailColumnNameHash.put(FxDataType.String1024, array("FTEXT1024"));
         detailColumnInsertPosHash.put(FxDataType.String1024, array(27));
         detailColumnUpdatePosHash.put(FxDataType.String1024, array(12));
         detailColumnNameHash.put(FxDataType.Text, array("FCLOB"));
         detailColumnInsertPosHash.put(FxDataType.Text, array(23));
         detailColumnUpdatePosHash.put(FxDataType.Text, array(8));
         detailColumnNameHash.put(FxDataType.HTML, array("FCLOB", "FBOOL", "UFCLOB"));
         detailColumnInsertPosHash.put(FxDataType.HTML, array(23, 31, 24));
         detailColumnUpdatePosHash.put(FxDataType.HTML, array(8, 16, 9));
         detailColumnNameHash.put(FxDataType.SelectOne, array("FSELECT"));
         detailColumnInsertPosHash.put(FxDataType.SelectOne, array(18));
         detailColumnUpdatePosHash.put(FxDataType.SelectOne, array(3));
         detailColumnNameHash.put(FxDataType.SelectMany, array("FSELECT", "FTEXT1024"/*comma separated list of selected id's*/, "FINT" /*number of selected options*/));
         detailColumnInsertPosHash.put(FxDataType.SelectMany, array(18, 27/*comma separated list of selected id's*/, 25/*number of selected options*/));
         detailColumnUpdatePosHash.put(FxDataType.SelectMany, array(3, 12/*comma separated list of selected id's*/, 10/*number of selected options*/));
 
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
 
     protected BinaryStorage binaryStorage;
 
     protected GenericHierarchicalStorage(BinaryStorage binaryStorage) {
         this.binaryStorage = binaryStorage;
     }
 
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
 
     /**
      * Helper to convert a var array of int to a 'real' int array
      *
      * @param data ints to put into an array
      * @return int array from parameters
      */
     protected static int[] array(int... data) {
         return data;
     }
 
     /**
      * Set a prepared statements values to <code>NULL</code> from startPos to endPos
      *
      * @param ps       prepared statement
      * @param startPos start position
      * @param endPos   end position
      * @throws SQLException on errors
      */
     protected static void clearPreparedStatement(PreparedStatement ps, int startPos, int endPos) throws SQLException {
         for (int i = startPos; i <= endPos; i++)
             ps.setNull(i, Types.NULL);
     }
 
     /**
      * Lock table needed for update operations for a content
      *
      * @param con     an open and valid connection
      * @param id      id of the content
      * @param version optional version, if <=0 all versions will be locked
      * @throws FxRuntimeException containing a FxDbException
      */
     public abstract void lockTables(Connection con, long id, int version) throws FxRuntimeException;
 
     /**
      * Use batch updates for changes in content data entries?
      *
      * @return if batch updates should be used for changes in content data entries
      */
     protected boolean batchContentDataChanges() {
         return true;
     }
 
 
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
 
     /**
      * Get the insert position of the uppercase column for a detail property or <code>-1</code> if no uppercase column exists
      *
      * @param prop   detail property
      * @param insert get column pos for an insert or update?
      * @return insert position of the uppercase column for a detail property or <code>-1</code> if no uppercase column exists
      */
     public int getUppercaseColumnPos(FxProperty prop, boolean insert) {
         if (prop.isSystemInternal())
             return -1;
         switch (prop.getDataType()) {
             case String1024:
                 return insert ? 28 : 13; //UFTEXT1024
             case Text:
                 return insert ? 24 : 9; //UFCLOB
             default:
                 return -1;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getQueryUppercaseColumn(FxProperty property) {
         if (!property.isSystemInternal() && property.getDataType() == FxDataType.HTML)
             return "UPPER(UFCLOB)";
         return getUppercaseColumn(property);
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getColumns(FxProperty prop) {
         if (prop.isSystemInternal())
             return mainColumnHash.get(prop.getId());
         return detailColumnNameHash.get(prop.getDataType());
     }
 
     /**
      * Get the insert positions for the given detail properties
      *
      * @param prop property
      * @return insert positions for the prepared statement
      */
     protected int[] getColumnPosInsert(FxProperty prop) {
         return detailColumnInsertPosHash.get(prop.getDataType());
     }
 
     /**
      * Get the insert positions for the given detail properties
      *
      * @param prop property
      * @return insert positions for the prepared statement
      */
     protected int[] getColumnPosUpdate(FxProperty prop) {
         return detailColumnUpdatePosHash.get(prop.getDataType());
     }
 
     /**
      * Get a new fulltext indexer instance
      *
      * @param pk  primary key
      * @param con an open and valid connection
      * @return a new fulltext indexer instance
      */
     protected FulltextIndexer getFulltextIndexer(FxPK pk, Connection con) {
         FulltextIndexer indexer = new GenericSQLFulltextIndexer();
         indexer.init(pk, con);
         return indexer;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPK contentCreate(Connection con, FxEnvironment env, StringBuilder sql, long newId, FxContent content) throws FxCreateException, FxInvalidParameterException {
         content.getRootGroup().removeEmptyEntries();
         content.getRootGroup().compactPositions(true);
         content.checkValidity();
         FxPK pk = createMainEntry(con, newId, 1, content);
         PreparedStatement ps;
         FulltextIndexer ft = getFulltextIndexer(pk, con);
         try {
             if (sql == null)
                 sql = new StringBuilder(2000);
             ps = con.prepareStatement(CONTENT_DATA_INSERT);
             createDetailEntries(con, ps, ft, sql, pk, content.isMaxVersion(), content.isLiveVersion(), content.getData("/"));
             if (batchContentDataChanges())
                 ps.executeBatch();
             ft.commitChanges();
             if (CacheAdmin.getEnvironment().getType(content.getTypeId()).isContainsFlatStorageAssignments()) {
                 FxFlatStorage flatStorage = FxFlatStorageManager.getInstance();
                 flatStorage.setPropertyData(con, pk, content.getTypeId(), content.getStepId(),
                         content.isMaxVersion(), content.isLiveVersion(), flatStorage.getFlatPropertyData(content.getRootGroup()));
             }
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
             content.resolveBinaryPreview();
             binaryStorage.updateContentBinaryEntry(con, pk, content.getBinaryPreviewId(), content.getBinaryPreviewACL());
             FxType type = env.getType(content.getTypeId());
             if (type.isTrackHistory())
                 EJBLookup.getHistoryTrackerEngine().track(type, pk, ConversionEngine.getXStream().toXML(content),
                         "history.content.created");
         } catch (FxApplicationException e) {
             if (e instanceof FxCreateException)
                 throw (FxCreateException) e;
             if (e instanceof FxInvalidParameterException)
                 throw (FxInvalidParameterException) e;
             throw new FxCreateException(LOG, e);
         } catch (SQLException e) {
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             ft.cleanup();
         }
         return pk;
     }
 
     /**
      * Assign correct MAX_VER, LIVE_VER, ISMAX_VER and ISLIVE_VER values for a given content instance
      *
      * @param con  an open and valid connection
      * @param type the contents type
      * @param id   the id to fix the version statistics for
      * @throws FxUpdateException if a sql error occurs
      */
     protected void fixContentVersionStats(Connection con, FxType type, long id) throws FxUpdateException {
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
             if (live_ver == 0) //deactivate in live tree
                 StorageManager.getTreeStorage().contentRemoved(con, id, true);
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
             if (type.isContainsFlatStorageAssignments())
                 syncContentStats(con, type.getId(), id, max_ver, live_ver);
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxNotFoundException e) {
             throw new FxUpdateException(e);
         } catch (FxApplicationException e) {
             throw new FxUpdateException(e);
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
      * Synchronize content stats to flat storage
      *
      * @param con an open and valid Connection
      * @param typeId type id
      * @param id content id
      * @param max_ver max. version
      * @param live_ver live version
      * @throws SQLException on errors
      */
     protected void syncContentStats(Connection con, long typeId, long id, int max_ver, int live_ver) throws SQLException {
         FxFlatStorageManager.getInstance().syncContentStats(con, typeId, id, max_ver, live_ver);
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
         final FxType type = CacheAdmin.getEnvironment().getType(content.getTypeId());
 
         FxPK pk;
         PreparedStatement ps = null;
         FulltextIndexer ft = null;
         try {
             int new_version = getContentVersionInfo(con, content.getPk().getId()).getMaxVersion() + 1;
             updateStepDependencies(con, content.getPk().getId(), new_version, env, env.getType(content.getTypeId()), content.getStepId());
             pk = createMainEntry(con, content.getPk().getId(), new_version, content);
             ft = getFulltextIndexer(pk, con);
             if (sql == null)
                 sql = new StringBuilder(2000);
             ps = con.prepareStatement(CONTENT_DATA_INSERT);
             createDetailEntries(con, ps, ft, sql, pk, content.isMaxVersion(), content.isLiveVersion(), content.getData("/"));
             if (batchContentDataChanges())
                 ps.executeBatch();
             if (type.isContainsFlatStorageAssignments()) {
                 FxFlatStorage flatStorage = FxFlatStorageManager.getInstance();
                 flatStorage.setPropertyData(con, pk, content.getTypeId(), content.getStepId(),
                         content.isMaxVersion(), content.isLiveVersion(), flatStorage.getFlatPropertyData(content.getRootGroup()));
             }
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
             binaryStorage.updateContentBinaryEntry(con, pk, content.getBinaryPreviewId(), content.getBinaryPreviewACL());
             ft.commitChanges();
 
             ps.close();
             final long id = content.getPk().getId();
             //lock needed columns
             ps = con.prepareStatement("SELECT MAX_VER, LIVE_VER, ISMAX_VER, ISLIVE_VER FROM " + TBL_CONTENT + " WHERE ID=? FOR UPDATE");
             ps.setLong(1, id);
             ps.execute();
             ps.close();
             ps = con.prepareStatement("SELECT ISMAX_VER, ISLIVE_VER FROM " + TBL_CONTENT_DATA + " WHERE ID=? FOR UPDATE");
             ps.setLong(1, id);
             ps.execute();
             fixContentVersionStats(con, type, id);
         } catch (FxApplicationException e) {
             if (e instanceof FxCreateException)
                 throw (FxCreateException) e;
             if (e instanceof FxInvalidParameterException)
                 throw (FxInvalidParameterException) e;
             throw new FxCreateException(e);
         } catch (SQLException e) {
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericHierarchicalStorage.class, null, ps);
             if (ft != null)
                 ft.cleanup();
         }
 
         final FxContent newVersion;
         try {
             sql.setLength(0);
             newVersion = contentLoad(con, pk, env, sql);
             syncFQNName(con, newVersion, pk, null);
         } catch (FxApplicationException e) {
             throw new FxCreateException(e);
         }
         
         if (type.isTrackHistory())
             EJBLookup.getHistoryTrackerEngine().track(type, pk,
                     ConversionEngine.getXStream().toXML(newVersion),
                     "history.content.created.version", pk.getVersion());
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
                     ps = con.prepareStatement(CONTENT_STEP_GETVERSIONS);
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
             ps.setLong(4, content.getAclIds().size() > 1 ? ACL.NULL_ACL_ID : content.getAclIds().get(0));
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
             final long userId = FxContext.getUserTicket().getUserId();
             final long now = System.currentTimeMillis();
             ps.setLong(18, userId);
             ps.setLong(19, now);
             ps.setLong(20, userId);
             ps.setLong(21, now);
             ps.setLong(22, content.getMandatorId());
             ps.executeUpdate();
 
             updateACLEntries(con, content, pk, true);
             
         } catch (SQLException e) {
             throw new FxCreateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxUpdateException e) {
             throw new FxCreateException(e);
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
 
     protected void updateACLEntries(Connection con, FxContent content, FxPK pk, boolean newEntry) throws SQLException, FxCreateException, FxUpdateException {
         PreparedStatement ps = null;
         try {
             if (content.getAclIds().isEmpty() || (content.getAclIds().size() == 1 && content.getAclIds().get(0) == ACL.NULL_ACL_ID)) {
                 if (newEntry) {
                     throw new FxCreateException(LOG, "ex.content.noACL", pk);
                 } else {
                     throw new FxUpdateException(LOG, "ex.content.noACL", pk);
                 }
             }
             if (!newEntry) {
                 // first remove all ACLs, then update them
                 ps = con.prepareStatement(CONTENT_ACLS_CLEAR);
                 ps.setLong(1, pk.getId());
                 ps.setInt(2, pk.getVersion());
                 ps.executeUpdate();
             }
             final List<Long> aclIds = content.getAclIds();
             if (aclIds.size() <= 1) {
                 return; // ACL saved in main table
             }
             
             //insert ACLs
             ps = con.prepareStatement(CONTENT_ACL_INSERT);
             for (long aclId : aclIds) {
                 ps.setLong(1, pk.getId());
                 ps.setInt(2, pk.getVersion());
                 ps.setLong(3, aclId);
                 ps.addBatch();
             }
             ps.executeBatch();
 
         } finally {
             Database.closeObjects(GenericHierarchicalStorage.class, null, ps);
         }
     }
 
     /**
      * Create all detail entries for a content instance
      *
      * @param con         an open and valid connection
      * @param ps          batch prepared statement for detail inserts
      * @param ft          fulltext indexer
      * @param sql         an optional StringBuffer
      * @param pk          primary key of the content
      * @param maxVersion  is this content the maximum available version?
      * @param liveVersion is this content the live version?
      * @param data        FxData to create
      * @throws FxNotFoundException on errors
      * @throws FxDbException       on errors
      * @throws FxCreateException   on errors
      */
     protected void createDetailEntries(Connection con, PreparedStatement ps, FulltextIndexer ft, StringBuilder sql,
                                        FxPK pk, boolean maxVersion, boolean liveVersion, List<FxData> data)
             throws FxNotFoundException, FxDbException, FxCreateException {
         try {
             FxProperty prop;
             for (FxData curr : data) {
                 if (curr.isProperty()) {
                     FxPropertyData pdata = ((FxPropertyData) curr);
                     prop = pdata.getPropertyAssignment().getProperty();
                     if (!prop.isSystemInternal())
                         insertPropertyData(prop, data, con, ps, ft, pk, pdata, maxVersion, liveVersion);
                 } else {
                     insertGroupData(con, sql, pk, ((FxGroupData) curr), maxVersion, liveVersion);
                     createDetailEntries(con, ps, ft, sql, pk, maxVersion, liveVersion, ((FxGroupData) curr).getChildren());
                 }
             }
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxNoAccessException e) {
             throw new FxCreateException(e);
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
 
     /**
      * Insert property detail data into the database
      *
      * @param prop      thepropery
      * @param allData   List of all data belonging to this property (for cascaded updates like binaries to avoid duplicates)
      * @param con       an open and valid connection
      * @param ps        batch prepared statement for detail inserts
      * @param ft        fulltext indexer
      * @param pk        primary key of the content
      * @param data      the value
      * @param isMaxVer  is this content in the max. version?
      * @param isLiveVer is this content in the live version? @throws SQLException        on errors
      * @throws FxDbException       on errors
      * @throws FxUpdateException   on errors
      * @throws FxNoAccessException for FxNoAccess values
      * @throws SQLException        on SQL errors
      */
     protected void insertPropertyData(FxProperty prop, List<FxData> allData, Connection con,
                                       PreparedStatement ps, FulltextIndexer ft, FxPK pk,
                                       FxPropertyData data, boolean isMaxVer, boolean isLiveVer) throws SQLException, FxDbException, FxUpdateException, FxNoAccessException {
         if (data == null || data.isEmpty())
             return;
         if (data.getPropertyAssignment().isFlatStorageEntry()) {
             if (ft != null && prop.isFulltextIndexed())
                 ft.index(data);
             return;
         }
         clearPreparedStatement(ps, INSERT_VALUE_POS, INSERT_END_POS);
         ps.setLong(18, 0); //FSELECT has to be set to 0 and not null
 
         ps.setLong(1, pk.getId());
         ps.setInt(2, pk.getVersion());
         ps.setInt(3, data.getPos());
         ps.setLong(5, data.getAssignmentId());
         ps.setString(6, XPathElement.stripType(data.getXPath()));
         ps.setString(7, XPathElement.stripType(data.getXPathFull()));
         String xmult = StringUtils.join(ArrayUtils.toObject(data.getIndices()), ',');
         ps.setString(8, xmult);
         ps.setInt(9, data.getIndex());
         ps.setString(10, getParentGroupXMult(xmult));
         ps.setBoolean(11, isMaxVer);
         ps.setBoolean(12, isLiveVer);
         ps.setBoolean(13, false); //ISGROUP
         ps.setLong(15, prop.getId());
         ps.setString(16, XPathElement.stripType(data.getParent().getXPathFull()));
         ps.setInt(17, data.getIndices().length);
 
         if (!data.getValue().isMultiLanguage()) {
             ps.setBoolean(INSERT_LANG_POS, true);
         } else
             ps.setBoolean(INSERT_ISDEF_LANG_POS, false);
         setPropertyData(true, prop, allData, con, data, ps, ft, getUppercaseColumnPos(prop, true), true);
     }
 
     /**
      * Update a properties data and/or position or a groups position
      *
      * @param change  the change applied
      * @param prop    the property unless change is a group change
      * @param allData all content data unless change is a group change
      * @param con     an open and valid connection
      * @param ps      batch prepared statement for detail updates
      * @param pk      primary key
      * @param data    property data unless change is a group change
      * @throws SQLException        on errors
      * @throws FxDbException       on errors
      * @throws FxUpdateException   on errors
      * @throws FxNoAccessException for FxNoAccess values
      */
     protected void updatePropertyData(FxDelta.FxDeltaChange change, FxProperty prop, List<FxData> allData,
                                       Connection con, PreparedStatement ps, FxPK pk, FxPropertyData data)
             throws SQLException, FxDbException, FxUpdateException, FxNoAccessException {
         if ((change.isProperty() && (data == null || data.isEmpty() || data.getPropertyAssignment().isFlatStorageEntry())) ||
                 !(change.isDataChange() || change.isPositionChange()))
             return;
         clearPreparedStatement(ps, 1, UPDATE_ID_POS);
         ps.setLong(3, 0); //FSELECT has to be set to 0 and not null!
 
         if (change.isPositionChange())
             ps.setInt(UPDATE_POS_POS, change.getNewData().getPos());
         else
             ps.setInt(UPDATE_POS_POS, change.getOriginalData().getPos());
 
         ps.setLong(UPDATE_ID_POS, pk.getId());
         ps.setInt(UPDATE_ID_POS + 1, pk.getVersion());
         ps.setLong(UPDATE_ID_POS + 3, change.getNewData().getAssignmentId());
         ps.setString(UPDATE_ID_POS + 4, XPathElement.stripType(change.getNewData().getXPathFull()) + (data == null ? "/" : ""));
 
         if (change.isGroup()) {
             ps.setInt(UPDATE_ID_POS + 2, (int) FxLanguage.SYSTEM_ID);
             ps.setBoolean(UPDATE_MLDEF_POS, true);
             if (batchContentDataChanges())
                 ps.addBatch();
             else
                 ps.executeUpdate();
             return;
         }
 
         if (change.isPositionChange() && !change.isDataChange()) {
             //just update positions
             assert data != null;
             for (long lang : data.getValue().getTranslatedLanguages()) {
                 ps.setInt(UPDATE_ID_POS + 2, (int) lang);
                 setPropertyData(false, prop, allData, con, data, ps, null, getUppercaseColumnPos(prop, false), false);
             }
             return;
         }
         setPropertyData(false, prop, allData, con, data, ps, null, getUppercaseColumnPos(prop, false), true);
     }
 
     /**
      * Set a properties data for inserts or updates
      *
      * @param insert          perform insert or update?
      * @param prop            current property
      * @param allData         all data of the instance (might be needed to buld references, etc.)
      * @param con             an open and valid connection
      * @param data            current property data
      * @param ps              prepared statement for the data table
      * @param ft              fulltext indexer
      * @param upperColumnPos  position of the uppercase column (if present, else <code>-1</code>)
      * @param includeFullText add fulltext entries? Will be skipped for position only changes
      * @throws SQLException        on errors
      * @throws FxUpdateException   on errors
      * @throws FxDbException       on errors
      * @throws FxNoAccessException for FxNoAccess values
      */
     private void setPropertyData(boolean insert, FxProperty prop, List<FxData> allData,
                                  Connection con, FxPropertyData data, PreparedStatement ps, FulltextIndexer ft,
                                  int upperColumnPos, boolean includeFullText) throws SQLException, FxUpdateException, FxDbException, FxNoAccessException {
         FxValue value = data.getValue();
         if (value instanceof FxNoAccess)
             throw new FxNoAccessException("ex.content.value.noaccess");
         if (value.isMultiLanguage() != ((FxPropertyAssignment) data.getAssignment()).isMultiLang()) {
             if (((FxPropertyAssignment) data.getAssignment()).isMultiLang())
                 throw new FxUpdateException("ex.content.value.invalid.multilanguage.ass.multi", data.getXPathFull());
             else
                 throw new FxUpdateException("ex.content.value.invalid.multilanguage.ass.single", data.getXPathFull());
         }
         int pos_lang = insert ? INSERT_LANG_POS : UPDATE_ID_POS + 2;
         int pos_isdef_lang = insert ? INSERT_ISDEF_LANG_POS : UPDATE_MLDEF_POS;
         if (prop.getDataType().isSingleRowStorage()) {
             //Data types that just use one db row can be handled in a very similar way
             Object translatedValue;
             GregorianCalendar gc = null;
             for (int i = 0; i < value.getTranslatedLanguages().length; i++) {
                 translatedValue = value.getTranslation(value.getTranslatedLanguages()[i]);
                 if (translatedValue == null) {
                     LOG.warn("Translation for " + data.getXPath() + " is null!");
                 }
                 ps.setLong(pos_lang, value.getTranslatedLanguages()[i]);
                 if (!value.isMultiLanguage())
                     ps.setBoolean(pos_isdef_lang, true);
                 else
                     ps.setBoolean(pos_isdef_lang, value.isDefaultLanguage(value.getTranslatedLanguages()[i]));
                 if (upperColumnPos != -1)
                     ps.setString(upperColumnPos, translatedValue.toString().toUpperCase());
                 int[] pos = insert ? getColumnPosInsert(prop) : getColumnPosUpdate(prop);
                 switch (prop.getDataType()) {
                     case Double:
                         checkDataType(FxDouble.class, value, data.getXPathFull());
                         ps.setDouble(pos[0], (Double) translatedValue);
                         break;
                     case Float:
                         checkDataType(FxFloat.class, value, data.getXPathFull());
                         ps.setFloat(pos[0], (Float) translatedValue);
                         break;
                     case LargeNumber:
                         checkDataType(FxLargeNumber.class, value, data.getXPathFull());
                         ps.setLong(pos[0], (Long) translatedValue);
                         break;
                     case Number:
                         checkDataType(FxNumber.class, value, data.getXPathFull());
                         ps.setInt(pos[0], (Integer) translatedValue);
                         break;
                     case HTML:
                         checkDataType(FxHTML.class, value, data.getXPathFull());
                         boolean useTidy = ((FxHTML) value).isTidyHTML();
                         ps.setBoolean(pos[1], useTidy);
                         final String extractorInput = doTidy(data.getXPathFull(), (String) translatedValue);
                         if (useTidy) {
                             translatedValue = extractorInput;
                         }
                         final HtmlExtractor result = new HtmlExtractor(
                                 extractorInput,
                                 true
                         );
                         ps.setString(pos[2], result.getText());
                         ps.setString(pos[0], (String) translatedValue);
                         break;
                     case String1024:
                     case Text:
                         checkDataType(FxString.class, value, data.getXPathFull());
                         ps.setString(pos[0], (String) translatedValue);
                         break;
                     case Boolean:
                         checkDataType(FxBoolean.class, value, data.getXPathFull());
                         ps.setBoolean(pos[0], (Boolean) translatedValue);
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
                         ps.setDate(pos[0], new java.sql.Date(gc.getTimeInMillis()));
                         break;
                     case DateTime:
                         checkDataType(FxDateTime.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime((java.util.Date) translatedValue);
                         ps.setTimestamp(pos[0], new Timestamp(gc.getTimeInMillis()));
                         break;
                     case DateRange:
                         checkDataType(FxDateRange.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime(((DateRange) translatedValue).getLower());
                         gc.set(GregorianCalendar.HOUR, 0);
                         gc.set(GregorianCalendar.MINUTE, 0);
                         gc.set(GregorianCalendar.SECOND, 0);
                         gc.set(GregorianCalendar.MILLISECOND, 0);
                         ps.setDate(pos[0], new java.sql.Date(gc.getTimeInMillis()));
                         gc.setTime(((DateRange) translatedValue).getUpper());
                         gc.set(GregorianCalendar.HOUR, 0);
                         gc.set(GregorianCalendar.MINUTE, 0);
                         gc.set(GregorianCalendar.SECOND, 0);
                         gc.set(GregorianCalendar.MILLISECOND, 0);
                         ps.setDate(pos[1], new java.sql.Date(gc.getTimeInMillis()));
                         break;
                     case DateTimeRange:
                         checkDataType(FxDateTimeRange.class, value, data.getXPathFull());
                         if (gc == null) gc = new GregorianCalendar();
                         gc.setTime(((DateRange) translatedValue).getLower());
                         ps.setTimestamp(pos[0], new Timestamp(gc.getTimeInMillis()));
                         gc.setTime(((DateRange) translatedValue).getUpper());
                         ps.setTimestamp(pos[1], new Timestamp(gc.getTimeInMillis()));
                         break;
                     case Binary:
                         checkDataType(FxBinary.class, value, data.getXPathFull());
                         BinaryDescriptor binary = (BinaryDescriptor) translatedValue;
                         if (!binary.isNewBinary()) {
                             ps.setLong(pos[0], binary.getId());
                         } else {
                             try {
                                 //transfer the binary from the transit table to the binary table
                                 BinaryDescriptor created = binaryStorage.binaryTransit(con, binary);
                                 ps.setLong(pos[0], created.getId());
                                 //check all other properties if they contain the same handle
                                 //and replace with the data of the new binary
                                 for (FxData _curr : allData) {
                                     if (_curr instanceof FxPropertyData && !_curr.isEmpty() &&
                                             ((FxPropertyData) _curr).getValue() instanceof FxBinary) {
                                         FxBinary _val = (FxBinary) ((FxPropertyData) _curr).getValue();
                                         _val._replaceHandle(binary.getHandle(), created);
                                     }
                                 }
                             } catch (FxApplicationException e) {
                                 throw new FxDbException(e);
                             }
                         }
                         break;
                     case SelectOne:
                         checkDataType(FxSelectOne.class, value, data.getXPathFull());
                         ps.setLong(pos[0], ((FxSelectListItem) translatedValue).getId());
                         break;
                     case SelectMany:
                         checkDataType(FxSelectMany.class, value, data.getXPathFull());
                         SelectMany sm = (SelectMany) translatedValue;
 
                         for (int i1 = 0; i1 < sm.getSelected().size(); i1++) {
                             FxSelectListItem item = sm.getSelected().get(i1);
                             if (i1 > 0) {
                                 if (batchContentDataChanges())
                                     ps.addBatch();
                                 else
                                     ps.executeUpdate();
                             }
                             ps.setLong(pos[0], item.getId());
                             ps.setString(pos[1], sm.getSelectedIdsList());
                             ps.setLong(pos[2], sm.getSelectedIds().size());
                         }
                         if (sm.getSelected().size() == 0)
                             ps.setLong(pos[0], 0); //write the virtual item as a marker to have a valid row
                         break;
                     case Reference:
                         checkDataType(FxReference.class, value, data.getXPathFull());
                         checkReference(con, prop.getReferencedType(), ((ReferencedContent) translatedValue), data.getXPathFull());
                         ps.setLong(pos[0], ((ReferencedContent) translatedValue).getId());
                         break;
                     case InlineReference:
                     default:
                         throw new FxDbException(LOG, "ex.db.notImplemented.store", prop.getDataType().getName());
                 }
                 if (batchContentDataChanges())
                     ps.addBatch();
                 else {
                     try {
                         ps.executeUpdate();
                     } catch (SQLException e) {
                         LOG.error(prop.getName(), e);
                         throw e;
                     }
                 }
             }
         } else {
             switch (prop.getDataType()) {
                 //TODO: implement datatype specific insert
                 default:
                     throw new FxDbException(LOG, "ex.db.notImplemented.store", prop.getDataType().getName());
             }
 
         }
         if (ft != null && prop.isFulltextIndexed() && includeFullText)
             ft.index(data);
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
             ps = con.prepareStatement(CONTENT_GET_TYPE);
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
                     //                1  2   3   4    5      6     7         8     9      10          11        12         13          14      15      16     17
                             append(" (ID,VER,POS,LANG,ASSIGN,XPATH,XPATHMULT,XMULT,XINDEX,PARENTXMULT,ISMAX_VER,ISLIVE_VER,PARENTXPATH,ISGROUP,ISMLDEF,XDEPTH,FSELECT");
             sql.append(")VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 
             ps = con.prepareStatement(sql.toString());
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ps.setInt(3, groupData.getPos());
             ps.setInt(4, (int) FxLanguage.SYSTEM_ID);
             ps.setLong(5, groupData.getAssignmentId());
             ps.setString(6, groupData.getXPath() + "/");
             ps.setString(7, groupData.getXPathFull() + "/");
             final String xmult = StringUtils.join(ArrayUtils.toObject(groupData.getIndices()), ',');
             ps.setString(8, xmult);
             ps.setInt(9, groupData.getIndex());
             ps.setString(10, getParentGroupXMult(xmult));
             ps.setBoolean(11, isMaxVer);
             ps.setBoolean(12, isLiveVer);
             ps.setString(13, groupData.getParent().getXPathFull());
             ps.setBoolean(14, true);
             ps.setBoolean(15, false);
             ps.setInt(16, groupData.getIndices().length);
             ps.setLong(17, 0); //FSELECT
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
         if (data.isProperty())
             if (((FxPropertyData) data).getPropertyAssignment().isFlatStorageEntry()) {
                 FxFlatStorageManager.getInstance().deletePropertyData(con, pk, ((FxPropertyData) data));
                 return;
             }
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
             final long aclId = rs.getLong(4);
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
             FxLock lock = StorageManager.getLockStorage().getLock(con, contentPK);
             FxContent content = new FxContent(contentPK, lock, type.getId(), type.isRelation(), mand.getId(),
                     aclId != ACL.NULL_ACL_ID ? aclId : -1,
                     step.getId(), rs.getInt(6),
                     rs.getInt(7), rs.getBoolean(10), rs.getInt(11), sourcePK, destinationPK, srcPos, dstPos,
                     LifeCycleInfoImpl.load(rs, 18, 19, 20, 21), root, rs.getLong(23), rs.getLong(24)).initSystemProperties();
             if (rs.next())
                 throw new FxLoadException("ex.content.load.notDistinct", pk);
             if (type.isMultipleContentACLs() && aclId == ACL.NULL_ACL_ID) {
                 content.setAclIds(loadContentAclTable(con, content.getPk()));
             }
             return content;
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxDbException e) {
             throw new FxLoadException(e);
         } catch (FxLockException e) {
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
 
     protected List<Long> loadContentAclTable(Connection con, FxPK pk) throws SQLException {
         if (pk.getVersion() < 0) {
             throw new IllegalArgumentException("No distinct version number given in PK " + pk);
         }
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_ACLS_LOAD);
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             final ResultSet rs = ps.executeQuery();
             final List<Long> aclIds = Lists.newArrayList();
             while (rs.next()) {
                 aclIds.add(rs.getLong(1));
             }
             return aclIds;
         } finally {
             Database.closeObjects(GenericHierarchicalStorage.class, null, ps);
         }
     }
 
     /**
      * Load all detail entries for a content instance
      *
      * @param con              open and valid(!) connection
      * @param type             FxType used
      * @param env              FxEnvironment
      * @param pk               primary key of the content data to load
      * @param requestedVersion the originally requested version (LIVE, MAX or specific version number, needed to resolve references since the pk's version is resolved already)
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
 //            root.removeEmptyEntries(true);
 //            root.compactPositions(true);
             root.removeNonInternalData();
             ps = con.prepareStatement(CONTENT_DATA_LOAD);
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ResultSet rs = ps.executeQuery();
             String currXPath = null;
             int currXDepth = 0;
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
             //load flat columns
             FxFlatStorageLoadContainer flatContainer = type.isContainsFlatStorageAssignments()
                     ? FxFlatStorageManager.getInstance().loadContent(this, con, type.getId(), pk, requestedVersion)
                     : null;
             while (rs != null && rs.next()) {
                 if (currXPath != null && !currXPath.equals(rs.getString(5))) {
                     //add this property
                     if (!isGroup) {
                         currValue.setDefaultLanguage(defLang);
                         if (flatContainer != null) {
                             //add flat entries that are positioned before the current entry
                             FxFlatStorageLoadColumn flatColumn;
                             while ((flatColumn = flatContainer.pop(currXPath.substring(0, currXPath.lastIndexOf('/') + 1), currXDepth, currPos)) != null) {
                                 addValue(root, flatColumn.getXPath(), flatColumn.getAssignment(), flatColumn.getPos(),
                                         flatColumn.getValue());
                             }
                         }
                     }
                     addValue(root, currXPath, currAssignment, currPos, currValue);
                     currValue = null;
                     defLang = FxLanguage.SYSTEM_ID;
                 }
                 //read next row
                 currXPath = rs.getString(5);
                 if (flatContainer != null) {
                     //calculate xdepth
                     currXDepth = 1;
                     for (char c : rs.getString(6).toCharArray())
                         if (c == ',') currXDepth++;
                 }
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
                     final FxPropertyAssignment propAssignment = (FxPropertyAssignment) currAssignment;
                     FxDataType dataType = propAssignment.getProperty().getDataType();
                     if (currValue == null)
                         multiLang = propAssignment.isMultiLang();
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
                                 if (propAssignment.hasMaxLength()) {
                                     currValue.setMaxInputLength(propAssignment.getMaxLength());
                                     if (dataType == FxDataType.String1024 && currValue.getMaxInputLength() > 1024)
                                         currValue.setMaxInputLength(1024);
                                 } else if (dataType == FxDataType.String1024)
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
                                                 rs.getDate(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[1]))
                                 );
                             else
                                 currValue.setTranslation(currLang,
                                         new DateRange(
                                                 rs.getDate(columns[0]),
                                                 rs.getDate(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[1]))
                                 );
                             break;
                         case DateTimeRange:
                             if (currValue == null)
                                 currValue = new FxDateTimeRange(multiLang, currLang,
                                         new DateRange(
                                                 rs.getTimestamp(columns[0]),
                                                 rs.getTimestamp(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[1]))
                                 );
                             else
                                 currValue.setTranslation(currLang,
                                         new DateRange(
                                                 rs.getTimestamp(columns[0]),
                                                 rs.getTimestamp(getColumns(((FxPropertyAssignment) currAssignment).getProperty())[1]))
                                 );
                             break;
                         case Binary:
                             BinaryDescriptor desc = binaryStorage.loadBinaryDescriptor(server, con, rs.getLong(columns[0]));
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
                 if (flatContainer != null) {
                     //add flat entries that are positioned before the current entry
                     FxFlatStorageLoadColumn flatColumn;
                     while ((flatColumn = flatContainer.pop(currXPath.substring(0, currXPath.lastIndexOf('/') + 1), currXDepth, currPos)) != null) {
                         addValue(root, flatColumn.getXPath(), flatColumn.getAssignment(), flatColumn.getPos(),
                                 flatColumn.getValue());
                     }
                 }
                 //add last property
                 if (!isGroup)
                     currValue.setDefaultLanguage(defLang);
                 addValue(root, currXPath, currAssignment, currPos, currValue);
             }
             if (flatContainer != null) {
                 if (isGroup && currAssignment != null) //if the last value was a group, add it (can only happen when using a flat storage)
                     addValue(root, currXPath, currAssignment, currPos, currValue);
                 //add remaining flat entries
                 FxFlatStorageLoadColumn flatColumn;
                 while ((flatColumn = flatContainer.pop()) != null) {
                     addValue(root, flatColumn.getXPath(), flatColumn.getAssignment(), flatColumn.getPos(),
                             flatColumn.getValue());
                 }
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
      * {@inheritDoc}
      */
     public ReferencedContent resolveReference(Connection con, int contentVersion, long referencedId) throws SQLException {
         String sql = contentVersion == FxPK.LIVE ? CONTENT_REFERENCE_LIVE : CONTENT_REFERENCE_MAX;
         PreparedStatement ps = null;
         int referencedVersion;
         long stepId, aclId, typeId, ownerId;
         String caption;
         try {
             ps = con.prepareStatement(sql);
             ps.setLong(1, referencedId);
             ps.setBoolean(2, true);
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
                 ps.setBoolean(2, true);
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
 
             // resolve ACLs from ACL table, if necessary
             FxEnvironment env = CacheAdmin.getEnvironment();
             final FxPK pk = new FxPK(referencedId, referencedVersion);
             final List<ACL> acls;
             if (aclId == ACL.NULL_ACL_ID) {
                 // multiple ACLs for this content instance
                 acls = FxSharedUtils.filterSelectableObjectsById(env.getACLs(), loadContentAclTable(con, pk));
             } else {
                 // only one ACL
                 acls = Arrays.asList(env.getACL(aclId));
             }
 
 
             ReferencedContent ref = new ReferencedContent(pk, caption, env.getStep(stepId), acls);
             try {
                 ref.setAccessGranted(
                         FxPermissionUtils.checkPermission(
                                 FxContext.getUserTicket(),
                                 ownerId, ACLPermission.READ,
                                 env.getType(typeId),
                                 ref.getStep().getAclId(),
                                 FxSharedUtils.getSelectableObjectIdList(acls),
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
      * {@inheritDoc}
      */
     public long getContentTypeId(Connection con, FxPK pk) throws FxLoadException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("SELECT DISTINCT TDEF FROM " + TBL_CONTENT + " WHERE ID=?");
             ps.setLong(1, pk.getId());
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next())
                 return rs.getLong(1);
             throw new FxLoadException("ex.content.notFound", pk);
         } catch (SQLException e) {
             throw new FxLoadException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 LOG.error(e);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getBinaryMetaData(Connection con, long binaryId) {
         return binaryStorage.getBinaryMetaData(con, binaryId);
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
         if (!assignment.isEnabled())
             return;
         if (assignment instanceof FxGroupAssignment) {
             root.addGroup(xPath, (FxGroupAssignment) assignment, pos);
             root.getGroup(xPath.substring(0, xPath.length() - 1)).removeNonInternalData();
         } else {
             root.addProperty(xPath, (FxPropertyAssignment) assignment, value, pos);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPK contentSave(Connection con, FxEnvironment env, StringBuilder sql, FxContent content, long fqnPropertyId) throws FxInvalidParameterException, FxUpdateException, FxNoAccessException {
         content.getRootGroup().removeEmptyEntries();
         content.getRootGroup().compactPositions(true);
         content.checkValidity();
         FxPK pk = content.getPk();
         if (pk.isNew() || !pk.isDistinctVersion())
             throw new FxInvalidParameterException("PK", "ex.content.pk.invalid.save", pk);
         FxDelta delta;
         FxContent original;
         final FxType type = env.getType(content.getTypeId());
         final UserTicket ticket = FxContext.getUserTicket();
         try {
             original = contentLoad(con, content.getPk(), env, sql);
             original.getRootGroup().removeEmptyEntries();
             original.getRootGroup().compactPositions(true);
 
             //unwrap all no access values so they can be saved
             if (type.isUsePropertyPermissions() && !ticket.isGlobalSupervisor()) {
                 FxContext.get().runAsSystem();
                 try {
                     FxPermissionUtils.unwrapNoAccessValues(content, original);
                 } finally {
                     FxContext.get().stopRunAsSystem();
                 }
             }
 
 
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
 
         FulltextIndexer ft = getFulltextIndexer(pk, con);
         FxFlatStorage fs = type.isContainsFlatStorageAssignments() ? FxFlatStorageManager.getInstance() : null;
 
         if (type.isUsePropertyPermissions() && !ticket.isGlobalSupervisor())
             FxPermissionUtils.checkPropertyPermissions(content.getLifeCycleInfo().getCreatorId(), delta, ACLPermission.EDIT);
 
         lockTables(con, pk.getId(), pk.getVersion());
 
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
 
             //before... scripts
             if (checkScripting) {
                 //delta-deletes:
                 for (FxDelta.FxDeltaChange change : delta.getRemoves()) {
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.BeforeDataChangeDelete)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
                 //delta-updates:
                 for (FxDelta.FxDeltaChange change : delta.getUpdates()) {
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.BeforeDataChangeUpdate)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
                 //delta-adds:
                 for (FxDelta.FxDeltaChange change : delta.getAdds()) {
                     for (long scriptId : change.getNewData().getAssignment().
                             getScriptMapping(FxScriptEvent.BeforeDataChangeAdd)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
             }
 
             //delta-deletes:
             for (FxDelta.FxDeltaChange change : delta.getRemoves()) {
                 if (type.isUsePropertyPermissions() && change.isProperty()) {
                     if (!ticket.mayDeleteACL(type.getPropertyAssignment(change.getXPath()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                         throw new FxNoAccessException("ex.acl.noAccess.property.delete", change.getXPath());
                 }
                 if (!change.getOriginalData().isSystemInternal()) {
                     deleteDetailData(con, sql, pk, change.getOriginalData());
                     if( change.isProperty() ) {
                         //check if the removed property is a FQN
                         if (((FxPropertyData) change.getOriginalData()).getPropertyId() == fqnPropertyId) {
                             syncFQNName(con, content, pk, change);
                         }
                     }
                     if (fs != null && change.isFlatStorageChange())
                         fs.deletePropertyData(con, pk, (FxPropertyData) change.getOriginalData());
                     ft.index(change);
                 }
             }
 
             //flatstorage adds/updates
             if (fs != null && delta.getFlatStorageAddsUpdates().size() > 0)
                 fs.setPropertyData(con, pk, type.getId(), content.getStepId(), content.isMaxVersion(),
                         content.isLiveVersion(), delta.getFlatStorageAddsUpdates());
 
             //delta-updates:
             List<FxDelta.FxDeltaChange> updatesRemaining = new ArrayList<FxDelta.FxDeltaChange>(delta.getUpdates());
 
             PreparedStatement ps_insert = null;
             PreparedStatement ps_update = null;
 
             try {
                 ps_insert = con.prepareStatement(CONTENT_DATA_INSERT);
                 ps_update = con.prepareStatement(CONTENT_DATA_UPDATE);
 
                 while (updatesRemaining.size() > 0) {
                     FxDelta.FxDeltaChange change = updatesRemaining.get(0);
                     //noinspection CaughtExceptionImmediatelyRethrown
                     try {
                         if (!change.getOriginalData().isSystemInternal()) {
                             if (change.isGroup()) {
                                 if (change.isPositionChange() && !change.isDataChange()) {
                                     //groups can only change position
                                     updatePropertyData(change, null, null, con, ps_update, pk, null);
                                 }
                             } else {
                                 FxProperty prop = env.getProperty(((FxPropertyData) change.getNewData()).getPropertyId());
                                 if (!change._isUpdateable()) {
                                     deleteDetailData(con, sql, pk, change.getOriginalData());
                                     insertPropertyData(prop, content.getData("/"), con, ps_insert, null, pk,
                                             ((FxPropertyData) change.getNewData()),
                                             content.isMaxVersion(), content.isLiveVersion());
                                 } else {
                                     updatePropertyData(change, prop, content.getData("/"), con, ps_update, pk, ((FxPropertyData) change.getNewData()));
                                 }
                                 //check if the property changed is a FQN
                                 if (prop.getId() == fqnPropertyId) {
                                     syncFQNName(con, content, pk, change);
                                 }
                             }
                         }
                         updatesRemaining.remove(0);
                         ft.index(change);
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
                     if (type.isUsePropertyPermissions() && change.isProperty()) {
                         if (!ticket.mayCreateACL(type.getPropertyAssignment(change.getXPath()).getACL().getId(), content.getLifeCycleInfo().getCreatorId()))
                             throw new FxNoAccessException("ex.acl.noAccess.property.create", change.getXPath());
                     }
                     if (!change.getNewData().isSystemInternal()) {
                         if (change.isGroup())
                             insertGroupData(con, sql, pk, (FxGroupData) change.getNewData(), content.isMaxVersion(), content.isLiveVersion());
                         else {
                             final FxProperty prop = env.getProperty(((FxPropertyData) change.getNewData()).getPropertyId());
                             insertPropertyData(prop,
                                     content.getData("/"), con, ps_insert, null, pk, ((FxPropertyData) change.getNewData()),
                                     content.isMaxVersion(), content.isLiveVersion());
                             ft.index(change);
                             //check if the property changed is a FQN
                             if (prop.getId() == fqnPropertyId) {
                                 syncFQNName(con, content, pk, change);
                             }
                         }
                     }
                 }
                 if (batchContentDataChanges()) {
                     ps_update.executeBatch();
                     ps_insert.executeBatch();
                 }
             } finally {
                 if (ps_update != null) ps_update.close();
                 if (ps_insert != null) ps_insert.close();
             }
 
             checkUniqueConstraints(con, env, sql, pk, content.getTypeId());
             if (delta.isInternalPropertyChanged()) {
                 updateStepDependencies(con, content.getPk().getId(), content.getPk().getVersion(), env, type, content.getStepId());
                 fixContentVersionStats(con, type, content.getPk().getId());
             }
             content.resolveBinaryPreview();
             if (original.getBinaryPreviewId() != content.getBinaryPreviewId() ||
                     original.getBinaryPreviewACL() != content.getBinaryPreviewACL())
                 binaryStorage.updateContentBinaryEntry(con, pk, content.getBinaryPreviewId(), content.getBinaryPreviewACL());
             enableDetailUniqueChecks(con);
             LifeCycleInfoImpl.updateLifeCycleInfo(TBL_CONTENT, "ID", "VER",
                     content.getPk().getId(), content.getPk().getVersion(), false, false);
 
             //after... scripts
             if (checkScripting) {
                 //delta-deletes:
                 for (FxDelta.FxDeltaChange change : delta.getRemoves()) {
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.AfterDataChangeDelete)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
                 //delta-updates:
                 for (FxDelta.FxDeltaChange change : delta.getUpdates()) {
                     for (long scriptId : change.getOriginalData().getAssignment().
                             getScriptMapping(FxScriptEvent.AfterDataChangeUpdate)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
                 //delta-adds:
                 for (FxDelta.FxDeltaChange change : delta.getAdds()) {
                     for (long scriptId : change.getNewData().getAssignment().
                             getScriptMapping(FxScriptEvent.AfterDataChangeAdd)) {
                         binding.setVariable("change", change);
                         scripting.runScript(scriptId, binding);
                     }
                 }
             }
 
             ft.commitChanges();
 
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
                                         "<original>\n" + xs.toXML(((FxPropertyData) update.getOriginalData()).getValue()) + "\n</original>\n" +
                                                 "<new>\n" + xs.toXML(((FxPropertyData) update.getNewData()).getValue()) + "\n</new>\n",
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
         } finally {
             ft.cleanup();
         }
         return content.getPk();
     }
 
     private void syncFQNName(Connection con, FxContent content, FxPK pk, FxDelta.FxDeltaChange change) throws FxApplicationException {
         FxValue val;
         if (change != null) {
             if (change.getChangeType() == FxDelta.FxDeltaChange.ChangeType.Remove) {
                 //sync to empty FQN (see FX-752)
                 StorageManager.getTreeStorage().syncFQNName(con, pk.getId(), content.isMaxVersion(), content.isLiveVersion(), null);
                 return;
             }
             val = ((FxPropertyData) change.getNewData()).getValue();
         } else {
             //check if there is a FQN property and sync that one (used when creating a new version)
             long fqnPropertyId = EJBLookup.getConfigurationEngine().get(SystemParameters.TREE_FQN_PROPERTY);
             List<FxPropertyData> pd = content.getPropertyData(fqnPropertyId, false);
             if( pd.size() > 0 )
                 val = pd.get(0).getValue();
             else
                 return;
         }
         if (/*!val.isEmpty() &&*/ val instanceof FxString) {
             StorageManager.getTreeStorage().syncFQNName(con, pk.getId(), content.isMaxVersion(), content.isLiveVersion(), (String) val.getBestTranslation());
         }
     }
 
     private void enableDetailUniqueChecks(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate(StorageManager.getReferentialIntegrityChecksStatement(true));
         } finally {
             if (stmt != null)
                 stmt.close();
         }
     }
 
     private void disableDetailUniqueChecks(Connection con) throws SQLException {
         Statement stmt = null;
         try {
             stmt = con.createStatement();
             stmt.executeUpdate(StorageManager.getReferentialIntegrityChecksStatement(false));
         } finally {
             if (stmt != null)
                 stmt.close();
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
             ps.setLong(2, content.getAclIds().size() > 1 ? ACL.NULL_ACL_ID : content.getAclIds().get(0));
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
             long userId = FxContext.getUserTicket().getUserId();
             ps.setLong(16, userId);
             ps.setLong(17, System.currentTimeMillis());
             ps.executeUpdate();
             
             updateACLEntries(con, content, content.getPk(), false);
 
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxCreateException e) {
             throw new FxUpdateException(e);
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
     public FxContentSecurityInfo getContentSecurityInfo(Connection con, FxPK pk, FxContent rawContent) throws FxLoadException, FxNotFoundException {
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
             long typeACL, contentACL, stepACL, previewACL;
             long previewId, typeId, ownerId, mandatorId;
             int version;
             final Set<Long> propertyPerms = new HashSet<Long>();
             ResultSet rs = ps.executeQuery();
             if (rs == null || !rs.next())
                 throw new FxNotFoundException("ex.content.notFound", pk);
             contentACL = rs.getLong(1);
             typeACL = rs.getLong(2);
             stepACL = rs.getLong(3);
             typePerm = rs.getByte(4);
             typeId = rs.getLong(5);
             previewId = rs.getLong(6);
             previewACL = rs.getLong(7);
             ownerId = rs.getLong(8);
             mandatorId = rs.getLong(9);
             version = rs.getInt(10);
             if (rs.next())
                 throw new FxLoadException("ex.db.resultSet.tooManyRows");
             if ((typePerm & 0x02) == 0x02) {
                 //use property permissions
                 FxContent co = rawContent;
 
                 try {
                     if (co == null) {
                         FxCachedContent cachedContent = CacheAdmin.getCachedContent(pk);
                         if (cachedContent != null)
                             co = cachedContent.getContent();
                         else {
                             ContentStorage storage = StorageManager.getContentStorage(pk.getStorageMode());
                             StringBuilder sql = new StringBuilder(2000);
                             co = storage.contentLoad(con, pk, CacheAdmin.getEnvironment(), sql);
                         }
                     }
                     co = co.copy();
                     co.getRootGroup().removeEmptyEntries();
                     for (String xp : co.getAllPropertyXPaths()) {
                         final FxPropertyAssignment pa = co.getPropertyData(xp).getPropertyAssignment();
                         if (pa.isSystemInternal())
                             continue;
                         Long propACL = pa.getACL().getId();
                         propertyPerms.add(propACL);
                     }
                 } catch (FxInvalidParameterException e) {
                     throw new FxLoadException(e);
                 }
             }
             pk = new FxPK(pk.getId(), version);
             final List<Long> acls = contentACL == ACL.NULL_ACL_ID ? loadContentAclTable(con, pk) : Arrays.asList(contentACL);
             return new FxContentSecurityInfo(pk, ownerId, previewId, typeId, mandatorId, typePerm, typeACL, stepACL,
                     acls, previewACL, Lists.newArrayList(propertyPerms),
                     StorageManager.getLockStorage().getLock(con, pk));
         } catch (SQLException e) {
             throw new FxLoadException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxLockException e) {
             throw new FxLoadException(e);
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
      * Check if content(s) may be removed.
      * This method handles referential integrity incase the used database does not support it
      *
      * @param con         an open and valid connection
      * @param typeId      the contents structure type
      * @param id          id of the content (optional, used if allForType is <code>false</code>)
      * @param version     version of the content (optional, used if allForType is <code>false</code> and allVersions is <code>false</code>)
      * @param allForType  remove all instances of the given type?
      * @param allVersions remove all versions or only the requested one?
      * @throws FxRemoveException if referential integrity would be violated
      */
     public void checkContentRemoval(Connection con, long typeId, long id, int version, boolean allForType, boolean allVersions) throws FxRemoveException {
         //to be implemented/overwritten for specific database implementations
         if (LOG.isDebugEnabled())
             LOG.debug("Removing type:" + typeId + " id:" + id + " ver:" + version + " allForType:" + allForType + " allVersions:" + allVersions);
         if (!allVersions && !allForType)
             return;  //specific version may be removed if not all of the type are removed
         try {
             PreparedStatement ps = null;
             boolean refuse = false;
             try {
                 if (allForType) {
                     ps = con.prepareStatement(CONTENT_REFERENCE_BYTYPE);
                     ps.setLong(1, typeId);
                 } else {
                     ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE FREF=?");
                     ps.setLong(1, id);
                 }
                 ResultSet rs = ps.executeQuery();
                 if (rs.next() && rs.getLong(1) != 0)
                     refuse = true;
                 long refCount = refuse ? rs.getLong(1) : 0;
                 if (!refuse) {
                     refuse = FxFlatStorageManager.getInstance().checkContentRemoval(con, typeId, id, allForType);
                     if( refuse )
                         refCount += FxFlatStorageManager.getInstance().getReferencedContentCount(con, id);
                 }
                 if (refuse) {
                     if (allForType) {
                        throw new FxRemoveException("ex.content.reference.inUse.type", CacheAdmin.getEnvironment().getType(typeId), refCount);
                     } else
                         throw new FxRemoveException("ex.content.reference.inUse.instance", id, refCount);
                 }
             } finally {
                 if (ps != null) ps.close();
             }
         } catch (SQLException e) {
             throw new FxRemoveException(e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void contentRemove(Connection con, FxType type, FxPK pk) throws FxRemoveException {
         PreparedStatement ps = null;
         FulltextIndexer ft = getFulltextIndexer(pk, con);
         try {
             checkContentRemoval(con, type.getId(), pk.getId(), -1, false, true);
 
             lockTables(con, pk.getId(), -1);
 
             //sync with tree
             StorageManager.getTreeStorage().contentRemoved(con, pk.getId(), false);
             ft.removeAllVersions();
             binaryStorage.removeBinaries(con, BinaryStorage.SelectOperation.SelectId, pk, -1L);
             ps = con.prepareStatement(CONTENT_DATA_REMOVE);
             ps.setLong(1, pk.getId());
             ps.executeUpdate();
             ps.close();
             FxFlatStorageManager.getInstance().removeContent(con, type.getId(), pk.getId());
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE);
             ps.setLong(1, pk.getId());
             ps.executeUpdate();
         } catch (SQLException e) {
             if (LOG.isWarnEnabled()) {
                 // log information about removed content
                 LOG.warn("Failed to remove " + pk + " due to a SQL error: " + e.getMessage());
             }
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
             ft.cleanup();
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
 
         checkContentRemoval(con, type.getId(), pk.getId(), ver, false, false);
 
         lockTables(con, pk.getId(), pk.getVersion());
 
         PreparedStatement ps = null;
         FulltextIndexer ft = getFulltextIndexer(new FxPK(pk.getId(), ver), con);
         try {
             //if its the live version - sync with live tree
             if (cvi.hasLiveVersion() && cvi.getLiveVersion() == ver)
                 StorageManager.getTreeStorage().contentRemoved(con, pk.getId(), true);
             ft.remove();
             binaryStorage.removeBinaries(con, BinaryStorage.SelectOperation.SelectVersion, pk, -1L);
             ps = con.prepareStatement(CONTENT_DATA_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             ps.executeUpdate();
             ps.close();
             String[] nodes = StorageManager.getTreeStorage().beforeContentVersionRemoved(con, pk.getId(), ver, cvi);
             FxFlatStorageManager.getInstance().removeContentVersion(con, type.getId(), pk.getId(), ver);
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE_VER);
             ps.setLong(1, pk.getId());
             ps.setInt(2, ver);
             if (ps.executeUpdate() > 0)
                 fixContentVersionStats(con, type, pk.getId());
             StorageManager.getTreeStorage().afterContentVersionRemoved(nodes, con, pk.getId(), ver, cvi);
         } catch (SQLException e) {
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxApplicationException e) {
             throw new FxRemoveException(e);
         } finally {
             ft.cleanup();
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
         FulltextIndexer ft = getFulltextIndexer(null, con);
         try {
             checkContentRemoval(con, type.getId(), -1, -1, true, true);
             //FX-96 - select all contents that are referenced from the tree
             ps = con.prepareStatement("SELECT DISTINCT c.ID FROM " + DatabaseConst.TBL_CONTENT + " c, " +
                     DatabaseConst.TBL_TREE + " te, " + DatabaseConst.TBL_TREE +
                     "_LIVE tl WHERE (te.REF=c.ID or tl.REF=c.ID) AND c.TDEF=?");
             ps.setLong(1, type.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next())
                 StorageManager.getTreeStorage().contentRemoved(con, rs.getLong(1), false);
             ps.close();
 
             ft.removeType(type.getId());
             binaryStorage.removeBinaries(con, BinaryStorage.SelectOperation.SelectType, null, type.getId());
             ps = con.prepareStatement(CONTENT_DATA_REMOVE_TYPE);
             ps.setLong(1, type.getId());
             ps.executeUpdate();
             ps.close();
             FxFlatStorageManager.getInstance().removeContentByType(con, type.getId());
             ps = con.prepareStatement(CONTENT_MAIN_REMOVE_TYPE);
             ps.setLong(1, type.getId());
             return ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxRemoveException(LOG, e, "ex.db.sqlError", e.getMessage());
         } catch (FxApplicationException e) {
             throw new FxRemoveException(e);
         } finally {
             ft.cleanup();
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
         binaryStorage.removeExpiredTransitEntries(con);
         try {
             StorageManager.getLockStorage().removeExpiredLocks(con);
         } catch (FxNotFoundException e) {
             LOG.error(e);
         }
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
         /*List<FxPropertyAssignment> pa = CacheAdmin.getEnvironment().getPropertyAssignments(prop.getId(), true);
         boolean hasFlat=false,hasHierarchical=false;
         for (FxPropertyAssignment p : pa) {
             if (hasFlat && hasHierarchical)
                 break;
             if (p.isFlatStorageEntry())
                 hasFlat = true;
             else
                 hasHierarchical = true;
         }
         if (hasHierarchical) {*/
         String typeChecks = null;
         sql.setLength(0);
         switch (mode) {
             case Global:
                 sql.append("SELECT tcd.XPATHMULT, COUNT(DISTINCT ccd.ID) FROM ").append(TBL_CONTENT_DATA).
                         append(" ccd, ").append(TBL_CONTENT_DATA).append(" tcd WHERE ccd.TPROP=").
                         append(prop.getId()).append(" AND ccd.TPROP=tcd.TPROP AND ccd.ID<>tcd.ID").
                         append(" AND ccd.LANG=tcd.LANG");
                 if (pk != null)
                     sql.append(" AND tcd.ID=").append(pk.getId());
                 else {
                     //prevent checks across versions
                     sql.append(" AND NOT(ccd.ID=tcd.ID AND ccd.VER<>tcd.VER)").
                             //prevent self-references
                                     append(" AND NOT(ccd.ID=tcd.ID AND ccd.VER=tcd.VER AND ccd.ASSIGN=tcd.ASSIGN AND tcd.XMULT=ccd.XMULT)");
                 }
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
         if (sql.length() > 0) {
             addColumnComparator(sql, prop, "ccd", "tcd");
             sql.append(" GROUP BY tcd.XPATHMULT");
 
             Statement s = null;
             try {
                 s = con.createStatement();
                 ResultSet rs = s.executeQuery(sql.toString());
                 if (rs != null && rs.next()) {
                     if (mode == UniqueMode.Instance || rs.getInt(2) > 0) {
                         if (throwException)
                             //noinspection ThrowableInstanceNeverThrown
                             throw new FxConstraintViolationException("ex.content.contraint.unique.xpath", rs.getString(1), mode).setAffectedXPath(rs.getString(1), FxContentExceptionCause.UniqueConstraintViolated);
                         else
                             return false;
                     }
                 }
             } finally {
                 Database.closeObjects(GenericHierarchicalStorage.class, s);
             }
         }
 //        }
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean uniqueConditionValid(Connection con, UniqueMode mode, FxProperty prop, long typeId, FxPK pk) {
         try {
             return uniqueConditionsMet(con, CacheAdmin.getEnvironment(), new StringBuilder(500), mode, prop, typeId, pk, false);
         } catch (SQLException e) {
             //noinspection ThrowableInstanceNeverThrown
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
         FulltextIndexer ft = getFulltextIndexer(null, con);
         try {
             if (!orgMultiLang && newMultiLang) {
                 //Single to Multi: lang=default language
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA + " SET LANG=? WHERE ASSIGN=?");
                 ps.setLong(1, defaultLanguage);
                 ps.setLong(2, assignmentId);
                 ps.executeUpdate();
                 ft.setLanguage(assignmentId, defaultLanguage);
             } else {
                 //Multi to Single: lang=system, values of the def. lang. are used, if other translations exist an exception will be raised
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_DATA + " SET LANG=? WHERE LANG=? AND ASSIGN=?");
                 ps.setLong(1, FxLanguage.SYSTEM_ID);
                 ps.setLong(2, defaultLanguage);
                 ps.setLong(3, assignmentId);
                 ps.executeUpdate();
                 ft.changeLanguage(assignmentId, defaultLanguage, FxLanguage.SYSTEM_ID);
                 ps.close();
                 ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE ASSIGN=? AND LANG<>?");
                 ps.setLong(1, assignmentId);
                 ps.setLong(2, FxLanguage.SYSTEM_ID);
                 ResultSet rs = ps.executeQuery();
                 long count;
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
 
     /**
      * {@inheritDoc}
      */
     public long getTypeInstanceCount(Connection con, long typeId) throws SQLException {
         PreparedStatement ps = null;
         long count = 0;
         try {
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT + " WHERE TDEF=?");
             ps.setLong(1, typeId);
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
     public void prepareSave(Connection con, FxContent content) throws FxInvalidParameterException, FxDbException {
         // key: handle, value: [mimeType,metaData]
         Map<String, String[]> mimeMetaMap = new HashMap<String, String[]>(5);
         try {
             for (FxData data : content.getRootGroup().getChildren())
                 try {
                     _prepareSave(mimeMetaMap, con, data);
                 } catch (FxApplicationException e) {
                     LOG.error(e); //not supposed to be thrown if called with a mimeMetaMap
                 }
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * Internal prepare save that walks through all groups to discover binaries that are to be processed
      *
      * @param mimeMetaMap optional meta map to avoid duplicates, can be <code>null</code>
      * @param con         an open and valid connection
      * @param data        current FxDate object to inspect
      * @throws SQLException           on errors
      * @throws FxApplicationException on errors
      */
     private void _prepareSave(Map<String, String[]> mimeMetaMap, Connection con, FxData data) throws SQLException, FxApplicationException {
         if (data instanceof FxGroupData)
             for (FxData sub : ((FxGroupData) data).getChildren())
                 _prepareSave(mimeMetaMap, con, sub);
         else if (data instanceof FxPropertyData) {
             FxPropertyData pdata = (FxPropertyData) data;
             if (pdata.isContainsDefaultValue() && !pdata.isEmpty())
                 ((FxPropertyData) data).setContainsDefaultValue(false);
             if (!pdata.isEmpty() && pdata.getValue() instanceof FxBinary) {
                 FxBinary bin = (FxBinary) pdata.getValue();
                 binaryStorage.prepareBinary(con, mimeMetaMap, bin);
             }
         }
     }
 
     //Binary handling
 
     /**
      * {@inheritDoc}
      */
     public OutputStream receiveTransitBinary(int divisionId, String handle, long expectedSize, long ttl) throws SQLException, IOException {
         return binaryStorage.receiveTransitBinary(divisionId, handle, expectedSize, ttl);
     }
 
     /**
      * {@inheritDoc}
      */
     public BinaryInputStream fetchBinary(Connection con, int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality) {
         return binaryStorage.fetchBinary(con, divisionId, size, binaryId, binaryVersion, binaryQuality);
     }
 
     /**
      * {@inheritDoc}
      */
     public void storeBinary(Connection con, long id, int version, int quality, String name, long length, InputStream binary) throws FxApplicationException {
         binaryStorage.storeBinary(con, id, version, quality, name, length, binary);
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateBinaryPreview(Connection con, long id, int version, int quality, int preview, int width, int height, long length, InputStream binary) throws FxApplicationException {
         binaryStorage.updateBinaryPreview(con, id, version, quality, preview, width, height, length, binary);
     }
 
     /**
      * {@inheritDoc}
      */
     public void prepareBinary(Connection con, FxBinary binary) throws SQLException, FxApplicationException {
         binaryStorage.prepareBinary(con, null, binary);
     }
 }
 
