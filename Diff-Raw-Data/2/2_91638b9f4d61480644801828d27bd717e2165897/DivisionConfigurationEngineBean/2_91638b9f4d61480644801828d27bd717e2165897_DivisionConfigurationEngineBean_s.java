 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.ejb.beans.configuration;
 
 import com.flexive.core.Database;
 import com.flexive.core.flatstorage.FxFlatStorageInfo;
 import com.flexive.core.flatstorage.FxFlatStorageManager;
 import com.flexive.core.storage.ContentStorage;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.ejb.beans.EJBUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.configuration.ParameterScope;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNoAccessException;
 import com.flexive.shared.impex.FxDivisionExportInfo;
 import com.flexive.shared.interfaces.DivisionConfigurationEngine;
 import com.flexive.shared.interfaces.DivisionConfigurationEngineLocal;
 import com.flexive.shared.structure.TypeStorageMode;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.annotation.Resource;
 import javax.ejb.*;
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 import java.util.zip.ZipFile;
 
 import static com.flexive.core.DatabaseConst.TBL_CONFIG_DIVISION;
 import static com.flexive.core.DatabaseConst.TBL_RESOURCES;
 
 /**
  * Division configuration implementation.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 
 @TransactionManagement(TransactionManagementType.CONTAINER)
 @TransactionAttribute(TransactionAttributeType.REQUIRED)
 @Stateless(name = "DivisionConfigurationEngine", mappedName = "DivisionConfigurationEngine")
 public class DivisionConfigurationEngineBean extends GenericConfigurationImpl implements DivisionConfigurationEngine, DivisionConfigurationEngineLocal {
     private static final Log LOG = LogFactory.getLog(DivisionConfigurationEngineBean.class);
     /**
      * Division config cache root.
      */
     private static final String CACHE_ROOT = "/divisionConfig";
 
     @Resource
     javax.ejb.SessionContext ctx;
 
     @Override
     protected ParameterScope getDefaultScope() {
         return ParameterScope.DIVISION;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Connection getConnection() throws SQLException {
         return Database.getDbConnection();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getInsertStatement(Connection conn, String path, String key, String value, String className)
             throws SQLException, FxNoAccessException {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.configuration.update.perm.division");
         }
         String sql = "INSERT INTO " + TBL_CONFIG_DIVISION + " (cpath, ckey, cvalue, className) VALUES (?, ?, ?, ?)";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         stmt.setString(2, key);
         StorageManager.setBigString(stmt, 3, value);
         stmt.setString(4, className);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getSelectStatement(Connection conn, String path, String key) throws SQLException {
         final String sql = "SELECT cvalue FROM " + TBL_CONFIG_DIVISION + " WHERE cpath=? AND ckey=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         stmt.setString(2, key);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getSelectStatement(Connection conn, String path) throws SQLException {
         final String sql = "SELECT ckey, cvalue FROM " + TBL_CONFIG_DIVISION + " WHERE cpath=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getSelectStatement(Connection conn) throws SQLException {
         final String sql = "SELECT cpath, ckey, cvalue, className FROM " + TBL_CONFIG_DIVISION;
         return conn.prepareStatement(sql);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getUpdateStatement(Connection conn, String path, String key, String value, String className)
             throws SQLException, FxNoAccessException {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.configuration.update.perm.division");
         }
         final String sql = "UPDATE " + TBL_CONFIG_DIVISION + " SET cvalue=?, className=? WHERE cpath=? AND ckey=?";
         PreparedStatement stmt = conn.prepareStatement(sql);
         StorageManager.setBigString(stmt, 1, value);
         stmt.setString(2, className);
         stmt.setString(3, path);
         stmt.setString(4, key);
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected PreparedStatement getDeleteStatement(Connection conn, String path, String key)
             throws SQLException, FxNoAccessException {
         if (!FxContext.getUserTicket().isGlobalSupervisor()) {
             throw new FxNoAccessException("ex.configuration.delete.perm.division");
         }
         final String sql = "DELETE FROM " + TBL_CONFIG_DIVISION + " WHERE cpath=? "
                 + (key != null ? " AND ckey=?" : "");
         PreparedStatement stmt = conn.prepareStatement(sql);
         stmt.setString(1, path);
         if (key != null) {
             stmt.setString(2, key);
         }
         return stmt;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected String getCachePath(String path) {
         return CACHE_ROOT + path;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void installBinary(long binaryId, String resourceName) throws FxApplicationException {
         Connection con = null;
         try {
             ContentStorage storage = StorageManager.getContentStorage(TypeStorageMode.Hierarchical);
             String binaryName = resourceName;
             String subdir = "";
             if (binaryName.indexOf('/') > 0) {
                 binaryName = binaryName.substring(binaryName.lastIndexOf('/') + 1);
                 subdir = resourceName.substring(0, resourceName.lastIndexOf('/') + 1);
             }
             ClassLoader cl = Thread.currentThread().getContextClassLoader();
             con = getConnection();
             long length = 0;
             String[] files = FxSharedUtils.loadFromInputStream(cl.getResourceAsStream("fxresources/binaries/" + subdir + "resourceindex.flexive"), -1).
                     replaceAll("\r", "").split("\n");
             for (String file : files) {
                 if (file.startsWith(binaryName + "|")) {
                     length = Long.parseLong(file.split("\\|")[1]);
                     break;
                 }
             }
             if (length == 0)
                 throw new FxApplicationException("ex.scripting.load.resource.failed", resourceName);
             storage.storeBinary(con, binaryId, 1, 1, binaryName, length, cl.getResourceAsStream("fxresources/binaries/" + resourceName));
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (con != null)
                     con.close();
             } catch (SQLException e) {
                 //ignore
             }
         }
     }
 
     private static class SQLPatchScript {
         long from;
         long to;
         String script;
 
         SQLPatchScript(long from, long to, String script) {
             this.from = from;
             this.to = to;
             this.script = script;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void patchDatabase() throws FxApplicationException {
         final long oldVersion = get(SystemParameters.DB_VERSION);
         final long patchedVersion = performPatching();
         if (patchedVersion != oldVersion) {
             modifyDatabaseVersion(patchedVersion);
         }
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     private long performPatching() throws FxApplicationException {
         FxContext.get().runAsSystem();
         try {
             long dbVersion = get(SystemParameters.DB_VERSION);
             long currentVersion = dbVersion;
             if (dbVersion == -1) {
                 put(SystemParameters.DB_VERSION, FxSharedUtils.getDBVersion());
                 return dbVersion; //no need to patch
             } else if (dbVersion == FxSharedUtils.getDBVersion()) {
                 //nothing to do
                 return dbVersion;
             } else if (dbVersion > FxSharedUtils.getDBVersion()) {
                 //the database is more current than the EAR!
                 LOG.warn("This [fleXive] build is intended for database schema version #" + FxSharedUtils.getDBVersion() + " but the database reports a higher schema version! (database schema version: " + dbVersion + ")");
                 return dbVersion;
             }
             //lets see if we have a patch we can apply
             try {
                 final String dbVendor = FxContext.get().getDivisionData().getDbVendor();
                 final String dir = "resources/patch-" + dbVendor + "/";
                 String idxFile = dir + "resourceindex.flexive";
                 ClassLoader cl = Thread.currentThread().getContextClassLoader();
                 final InputStream scriptIndex = cl.getResourceAsStream(idxFile);
                 if (scriptIndex == null) {
                     LOG.info("No patches available for " + dbVendor);
                     return dbVersion;
                 }
                 String[] files = FxSharedUtils.loadFromInputStream(scriptIndex, -1).replaceAll("\r", "").split("\n");
                 Connection con = null;
                 Statement stmt = null;
                 try {
                     con = Database.getNonTXDataSource().getConnection();
                     stmt = con.createStatement();
                     List<SQLPatchScript> scripts = new ArrayList<SQLPatchScript>(50);
                     for (String file : files) {
                         String[] f = file.split("\\|");
                         int size = Integer.valueOf(f[1]);
                         String[] data = f[0].split("_");
                         if (data.length != 3) {
                             LOG.warn("Expected " + f[0] + " to have format xxx_yyy_zzz.sql");
                             continue;
                         }
                         if (!"patch".equals(data[0])) {
                             LOG.info("Expected a patch file, but got: " + data[0]);
                             continue;
                         }
                         if (!data[2].endsWith(".sql")) {
                             LOG.info("Expected an sql file, but got: " + data[2]);
                             continue;
                         }
                         long fromVersion = Long.parseLong(data[1]);
                         long toVersion = Long.parseLong(data[2].substring(0, data[2].indexOf('.')));
                         String code = FxSharedUtils.loadFromInputStream(cl.getResourceAsStream(dir + f[0]), size);
                         scripts.add(new SQLPatchScript(fromVersion, toVersion, code));
 //                        LOG.info("Patch available from version " + fromVersion + " to " + toVersion);
                     }
 //                    stmt.executeUpdate(code);
                     boolean patching = true;
                     long maxVersion = currentVersion;
                     while (patching) {
                         patching = false;
                         for (SQLPatchScript ps : scripts) {
                             if (ps.from == currentVersion) {
                                 LOG.info("Patching database schema from version [" + ps.from + "] to [" + ps.to + "] ... ");
                                 new SQLScriptExecutor(ps.script, stmt).execute();
                                 currentVersion = ps.to;
                                 patching = true;
                                 if (ps.to > maxVersion)
                                     ps.to = maxVersion;
                                 break;
                             }
                         }
                     }
                     if (currentVersion < maxVersion) {
                         LOG.warn("Failed to patch to maximum available database schema version (" + maxVersion + "). Current database schema version: " + currentVersion);
                     }
                     return currentVersion;
                 } finally {
                     Database.closeObjects(DivisionConfigurationEngineBean.class, con, stmt);
                 }
             } catch (IOException e) {
                 LOG.fatal(e);
                 return currentVersion;
             } catch (SQLException e) {
                 LOG.fatal(e);
                 return currentVersion;
             }
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     private void modifyDatabaseVersion(long currentVersion) throws FxApplicationException {
         FxContext.get().runAsSystem();
         try {
             put(SystemParameters.DB_VERSION, currentVersion);
         } finally {
             FxContext.get().stopRunAsSystem();
         }
     }
 
     /**
      * Helper class to execute an SQL script which contains of multiple statements and comments
      */
     static class SQLScriptExecutor {
 
         /**
          * Statement delimiter
          */
         public final static char DELIMITER = ';';
 
         private Statement stmt;
         private String script;
         private List<String> lines;
 
         /**
          * Ctor
          *
          * @param script the script to parse and execute
          * @param stat   an open and valid statements
          * @throws SQLException on errors
          * @throws IOException  on errors
          */
         public SQLScriptExecutor(String script, Statement stat) throws SQLException, IOException {
             this.stmt = stat;
             this.script = script;
             this.lines = new ArrayList<String>(20);
             parse();
         }
 
         /**
          * Parse the script
          *
          * @throws IOException  on errors
          * @throws SQLException on errors
          */
         protected void parse() throws IOException, SQLException {
             BufferedReader reader = new BufferedReader(new StringReader(script));
             String currLine;
             StringBuilder sb = new StringBuilder(500);
             boolean eos;
 
             while ((currLine = reader.readLine()) != null) {
                 if (isComment(currLine))
                     continue;
                 eos = currLine.indexOf(DELIMITER) != -1;
                 sb.append(currLine).append(' ');
                 if (eos) {
                     lines.add(sb.toString());
                     sb.setLength(0);
                 }
             }
         }
 
         /**
          * Is the passed line a comment?
          * Only single line comments starting with "#" or "--" are supported!
          *
          * @param line line to examine
          * @return is comment
          */
         private boolean isComment(String line) {
             return (line != null) && (line.length() > 0) && (line.trim().charAt(0) == '#' || line.trim().startsWith("--"));
         }
 
         /**
          * Execute the script
          *
          * @throws SQLException on errors
          */
         public void execute() throws SQLException {
             for (String line : lines) {
                 try {
                     stmt.execute(line);
                 } catch (SQLException e) {
                     LOG.error("Failed to execute [" + line + "]!");
                     throw e;
                 }
             }
         }
 
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String getDatabaseInfo() {
         final DivisionData divisionData = FxContext.get().getDivisionData();
         return "Division #" + divisionData.getId() + " - " + divisionData.getDbVendor() + " " + divisionData.getDbVersion();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public String getDatabaseDriverInfo() {
         return FxContext.get().getDivisionData().getDbDriverVersion();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public boolean isFlatStorageEnabled() {
         return FxFlatStorageManager.getInstance().isEnabled();
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public List<FxFlatStorageInfo> getFlatStorageInfos() throws FxApplicationException {
         try {
             return FxFlatStorageManager.getInstance().getFlatStorageInfos();
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void createFlatStorage(String name, String description, int stringColumns, int textColumns, int bigIntColumns, int doubleColumns, int selectColumns) throws FxApplicationException {
         try {
             Connection con = null;
             try {
                 con = Database.getNonTXDataSource().getConnection();
                 FxFlatStorageManager.getInstance().createFlatStorage(con, name, description, stringColumns, textColumns, bigIntColumns, doubleColumns, selectColumns);
             } catch (FxApplicationException e) {
                 EJBUtils.rollback(ctx);
                 throw e;
             } finally {
                 Database.closeObjects(DivisionConfigurationEngineBean.class, con, null);
             }
         } catch (SQLException e) {
             EJBUtils.rollback(ctx);
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void removeFlatStorage(String name) throws FxApplicationException {
         try {
             try {
                 FxFlatStorageManager.getInstance().removeFlatStorage(name);
             } catch (FxApplicationException e) {
                 EJBUtils.rollback(ctx);
                 throw e;
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void exportDivision(String localFileName) throws FxApplicationException {
         if (!FxContext.getUserTicket().isGlobalSupervisor())
             throw new FxNoAccessException("ex.export.noAccess");
         if (StringUtils.isEmpty(localFileName))
             throw new FxInvalidParameterException("localFileName", "ex.export.noFileProvided");
         File zip = new File(localFileName);
         boolean createError = false;
         try {
             if (zip.exists() || !zip.createNewFile())
                 createError = true;
         } catch (IOException e) {
             LOG.info(e);
             createError = true;
         }
         if (createError)
             throw new FxInvalidParameterException("localFileName", "ex.export.fileCreateError", localFileName);
 
         FileOutputStream fos = null;
         Connection con = null;
         try {
             con = Database.getDbConnection();
             fos = new FileOutputStream(zip);
             StorageManager.getStorageImpl().exportDivision(con, fos);
         } catch (Exception e) {
             throw new FxApplicationException(e, "ex.export.failed", localFileName, e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, null);
             try {
                 if (fos != null) fos.close();
             } catch (IOException e) {
                 LOG.error(e);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxDivisionExportInfo getDivisionExportInfo(String localFileName) throws FxApplicationException {
         if (!FxContext.getUserTicket().isGlobalSupervisor())
             throw new FxNoAccessException("ex.import.noAccess");
         if (StringUtils.isEmpty(localFileName))
             throw new FxInvalidParameterException("localFileName", "ex.import.noFileProvided");
         File data = new File(localFileName);
         if (!data.exists() || !data.isFile())
             throw new FxInvalidParameterException("localFileName", "ex.import.noFile", localFileName);
         ZipFile zip;
         try {
             zip = new ZipFile(data);
         } catch (IOException e) {
             throw new FxInvalidParameterException("localFileName", "ex.import.noZIP", localFileName);
         }
         return StorageManager.getStorageImpl().getDivisionExportInfo(zip);
     }
 
     /**
      * {@inheritDoc}
      */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
     public void importDivision(String localFileName) throws FxApplicationException {
         if (!FxContext.getUserTicket().isGlobalSupervisor())
             throw new FxNoAccessException("ex.import.noAccess");
         if (StringUtils.isEmpty(localFileName))
             throw new FxInvalidParameterException("localFileName", "ex.import.noFileProvided");
         File data = new File(localFileName);
         if (!data.exists() || !data.isFile())
             throw new FxInvalidParameterException("localFileName", "ex.import.noFile", localFileName);
         ZipFile zip;
         try {
             zip = new ZipFile(data);
         } catch (IOException e) {
             throw new FxInvalidParameterException("localFileName", "ex.import.noZIP", localFileName);
         }
         Connection con = null;
         try {
             con = Database.getDbConnection();
             StorageManager.getStorageImpl().importDivision(con, zip);
         } catch (Exception e) {
             throw new FxApplicationException(e, "ex.import.failed", localFileName, e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, null);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void setResourceValue(String key, FxString value) throws FxApplicationException {
         if (StringUtils.isBlank(key))
             return;
         key = key.trim();
         if (key.length() > 50)
             throw new FxApplicationException("ex.configuration.resource.key.tooLong", key);
         if (!StringUtils.isAsciiPrintable(key))
             throw new FxApplicationException("ex.configuration.resource.key.nonAscii", key);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("DELETE FROM " + TBL_RESOURCES + " WHERE RKEY=?");
             ps.setString(1, key);
             ps.executeUpdate();
             if (value != null && !value.isEmpty()) {
                 ps.close();
                 ps = con.prepareStatement("INSERT INTO " + TBL_RESOURCES + " (RKEY,LANG,RVAL)VALUES(?,?,?)");
                 ps.setString(1, key);
                 for (long lang : value.getTranslatedLanguages()) {
                     ps.setLong(2, lang);
                     ps.setString(3, value.getTranslation(lang));
                     ps.addBatch();
                 }
                 ps.executeBatch();
             }
         } catch (SQLException e) {
             throw new FxApplicationException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.REQUIRED)
     public void removeResourceValues(String keyPrefix) throws FxApplicationException {
         if (StringUtils.isBlank(keyPrefix))
             return;
         keyPrefix = keyPrefix.trim();
         if (keyPrefix.length() > 50)
             throw new FxApplicationException("ex.configuration.resource.key.tooLong", keyPrefix);
         if (!StringUtils.isAsciiPrintable(keyPrefix))
             throw new FxApplicationException("ex.configuration.resource.key.nonAscii", keyPrefix);
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("DELETE FROM " + TBL_RESOURCES + " WHERE RKEY LIKE ?");
             ps.setString(1, keyPrefix + "%");
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxApplicationException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, ps);
         }
     }
 
     /**
      * Build an FxString from translations
      *
      * @param firstLang       the first returned language
      * @param defaultLanguage default language to set, if applicable
      * @param trans           translations
      * @return FxString
      */
     private FxString buildFxString(long firstLang, long defaultLanguage, Map<Long, String> trans) {
         if (trans.size() == 0)
             return null;
         if (trans.size() == 1 && firstLang == FxLanguage.SYSTEM_ID)
             return new FxString(false, trans.get(firstLang));
         FxString value = new FxString(trans);
         if (value.translationExists(defaultLanguage))
             value.setDefaultLanguage(defaultLanguage);
         else
             value.setDefaultLanguage(firstLang);
         return value;
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public FxString getResourceValue(String key, long defaultLanguage) throws FxApplicationException {
         if (StringUtils.isBlank(key)) {
             return null;
         }
         key = key.trim();
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT LANG,RVAL FROM " + TBL_RESOURCES + "  WHERE RKEY=?");
             ps.setString(1, key);
             ResultSet rs = ps.executeQuery();
             long firstLang = -1;
             Map<Long, String> trans = new HashMap<Long, String>(10);
             while (rs != null && rs.next()) {
                 if (firstLang == -1)
                     firstLang = rs.getLong(1);
                 trans.put(rs.getLong(1), rs.getString(2));
             }
             return buildFxString(firstLang, defaultLanguage, trans);
         } catch (SQLException e) {
             throw new FxApplicationException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @TransactionAttribute(TransactionAttributeType.SUPPORTS)
     public Map<String, FxString> getResourceValues(String keyPrefix, long defaultLanguage) throws FxApplicationException {
         Map<String, FxString> ret = new LinkedHashMap<String, FxString>(10);
         if (StringUtils.isBlank(keyPrefix)) {
             return ret;
         }
 
         keyPrefix = keyPrefix.trim();
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT RKEY,LANG,RVAL FROM " + TBL_RESOURCES + "  WHERE RKEY LIKE ? ORDER BY RKEY ASC");
 
             ps.setString(1, keyPrefix + "%");
             ResultSet rs = ps.executeQuery();
 
             String currKey = null;
             String key;
             Map<Long, String> trans = new HashMap<Long, String>(10);
             long firstLang = -1;
 
             while (rs != null && rs.next()) {
                 key = rs.getString(1);
                 if (!key.equals(currKey)) {
                     final FxString data = buildFxString(firstLang, defaultLanguage, trans);
                     if (data != null)
                         ret.put(currKey, data);
                     currKey = key;
                     trans.clear();
                     firstLang = -1;
                 }
                 if (firstLang == -1)
                     firstLang = rs.getLong(2);
                 trans.put(rs.getLong(2), rs.getString(3));
             }
             if (trans.size() > 0) {
                 final FxString data = buildFxString(firstLang, defaultLanguage, trans);
                 if (data != null)
                     ret.put(currKey, data);
             }
             return ret;
         } catch (SQLException e) {
             throw new FxApplicationException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(DivisionConfigurationEngine.class, con, ps);
         }
     }
 }
