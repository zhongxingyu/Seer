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
 
 package com.flexive.core.storage.PostgreSQL;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.SequencerStorage;
 import com.flexive.core.storage.genericSQL.GenericSequencerStorage;
 import com.flexive.shared.CustomSequencer;
 import com.flexive.shared.FxSystemSequencer;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Concrete Postgres sequencer storage implementation
  * see http://www.postgresql.org/docs/current/static/functions-sequence.html
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class PostgreSQLSequencerStorage extends GenericSequencerStorage {
     private static final Log LOG = LogFactory.getLog(PostgreSQLSequencerStorage.class);
 
     private static final PostgreSQLSequencerStorage instance = new PostgreSQLSequencerStorage();
 
     private final static String TBL_PG_SEQUENCES = "PG_CLASS";
     private final static String PG_SEQ_PREFIX = "FXSEQ_";
 //    private final static String SQL_NEXT = "SELECT CURRVAL(?), NEXTVAL(?)";
     private final static String SQL_CREATE = "CREATE SEQUENCE ";
     private final static String SQL_DELETE = "DROP SEQUENCE " + PG_SEQ_PREFIX;
     private final static String SQL_EXIST = "SELECT COUNT(*) FROM " + TBL_PG_SEQUENCES + " WHERE RELKIND='S' AND UPPER(RELNAME)=?";
     private final static String SQL_GET_INFO = "SELECT IS_CYCLED, LAST_VALUE FROM ";
     private final static String SQL_GET_USER = "SELECT SUBSTR(UPPER(s.RELNAME)," + (PG_SEQ_PREFIX.length() + 1) + ") FROM " +
             TBL_PG_SEQUENCES + " s WHERE s.RELKIND='S' AND UPPER(s.RELNAME) NOT LIKE '" + PG_SEQ_PREFIX + "SYS_%' AND UPPER(s.RELNAME) LIKE '" + PG_SEQ_PREFIX + "%' ORDER BY s.RELNAME ASC";
 
     private static final String NOROLLOVER = "NO CYCLE";
     private static final String ROLLOVER = "CYCLE";
 
     /**
      * Singleton getter
      *
      * @return SequencerStorage
      */
     public static SequencerStorage getInstance() {
         return instance;
     }
 
     /**
      * {@inheritDoc}
      */
     public long getMaxId() {
         return 9223372036854775807L;
     }
 
     /**
      * {@inheritDoc}
      */
     public long fetchId(String name, boolean allowRollover) throws FxCreateException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             // Obtain a database connection
             con = Database.getDbConnection();
             //TODO: catch exhausted!
             ps = con.prepareStatement("SELECT NEXTVAL(?)");
             ps.setString(1, PG_SEQ_PREFIX + name);
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
 //                System.out.println("==> FETCH FOR " + name+ " => "+rs.getLong(1));
                 return rs.getLong(1);
             } else
                 throw new FxCreateException(LOG, "ex.sequencer.fetch.failed", name);
         } catch (SQLException exc) {
             throw new FxCreateException(LOG, exc, "ex.sequencer.fetch.failedMsg", name, exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void createSequencer(String name, boolean allowRollover, long startNumber) throws FxApplicationException {
         if (StringUtils.isEmpty(name) || name.toUpperCase().trim().startsWith("SYS_"))
             throw new FxCreateException(LOG, "ex.sequencer.create.invalid.name", name);
         name = name.toUpperCase().trim();
         if (sequencerExists(name))
             throw new FxCreateException(LOG, "ex.sequencer.create.invalid.name", name);
 
         Connection con = null;
         Statement stmt = null;
         try {
             con = Database.getDbConnection();
             stmt = con.createStatement();
             stmt.executeUpdate(SQL_CREATE + PG_SEQ_PREFIX + name + " START " + startNumber + " MINVALUE 0 INCREMENT BY 1 " + (allowRollover ? ROLLOVER : NOROLLOVER));
             fetchId(name, allowRollover); //get one number to have same behaviour as other db's
         } catch (SQLException exc) {
             throw new FxCreateException(LOG, exc, "ex.sequencer.create.failed", name);
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeSequencer(String name) throws FxApplicationException {
         if (!sequencerExists(name))
             throw new FxCreateException(LOG, "ex.sequencer.notFound", name);
         Connection con = null;
         Statement stmt = null;
         try {
             con = Database.getDbConnection();
             stmt = con.createStatement();
             stmt.executeUpdate(SQL_DELETE + name);
         } catch (SQLException exc) {
             throw new FxCreateException(LOG, exc, "ex.sequencer.remove.failed", name, exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, stmt);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean sequencerExists(String name) throws FxApplicationException {
         if (StringUtils.isEmpty(name) || name.toUpperCase().trim().startsWith("SYS_"))
             return false;
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement(SQL_EXIST);
             ps.setString(1, PG_SEQ_PREFIX + name.trim().toUpperCase());
             ResultSet rs = ps.executeQuery();
             return !(rs == null || !rs.next()) && rs.getInt(1) != 0;
         } catch (SQLException exc) {
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<CustomSequencer> getCustomSequencers() throws FxApplicationException {
         List<CustomSequencer> res = new ArrayList<CustomSequencer>(20);
         Connection con = null, nonTxCon = null;
         PreparedStatement ps = null;
         Statement s = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement(SQL_GET_USER);
 
             nonTxCon = Database.getNonTXDataSource().getConnection();
             s = nonTxCon.createStatement();
 
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 try {
                     final CustomSequencer result = loadCustomSequencer(s, rs.getString(1));
                     res.add(result);
                 } catch (SQLException e) {
                     LOG.warn("Sequencer " + rs.getString(1) + " no longer exists");
                 }
             }
         } catch (SQLException exc) {
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
             Database.closeObjects(PostgreSQLSequencerStorage.class, nonTxCon, s);
         }
         return res;
     }
 
     private CustomSequencer loadCustomSequencer(Statement nonTxStatement, String name) throws SQLException, FxCreateException {
         // use non-transactional connection since the relation may have been deleted by another
         // thread, which would lead to a rollback on the main connection
        ResultSet rsi = null;
         try {
            rsi = nonTxStatement.executeQuery(SQL_GET_INFO + PG_SEQ_PREFIX + name);
             if (rsi.next()) {
                 return new CustomSequencer(name, rsi.getBoolean(1), rsi.getLong(2));
             } else {
                throw new FxCreateException("ex.sequencer.notFound", name);
             }
        } catch (SQLException e) {
            throw new FxCreateException("ex.sequencer.notFound", name);
         } finally {
            if (rsi != null) {
                rsi.close();
            }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public List<String> getCustomSequencerNames() throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement(SQL_GET_USER);
 
             ResultSet rs = ps.executeQuery();
 
             final List<String> res = Lists.newArrayList();
             while (rs != null && rs.next()) {
                 res.add(rs.getString(1));
             }
             return res;
         } catch (SQLException exc) {
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public long getCurrentId(FxSystemSequencer sequencer) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement(SQL_GET_INFO + PG_SEQ_PREFIX + sequencer.getSequencerName());
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next())
                 return rs.getLong(2);
         } catch (SQLException exc) {
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
         }
         throw new FxCreateException(LOG, "ex.sequencer.notFound", sequencer.getSequencerName());
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public long getCurrentId(String sequencer) throws FxApplicationException {
         Connection nonTxCon = null;
         Statement s = null;
         try {
             nonTxCon = Database.getNonTXDataSource().getConnection();
             s = nonTxCon.createStatement();
             return loadCustomSequencer(s, sequencer).getCurrentNumber();
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, nonTxCon, s);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setSequencerId(String name, long newId) throws FxApplicationException {
         Connection con = null;
         PreparedStatement ps = null;
         try {
             con = Database.getDbConnection();
             ps = con.prepareStatement("SELECT SETVAL('" + PG_SEQ_PREFIX + name + "',?,TRUE)");
             ps.setLong(1, newId);
             ps.execute();
         } catch (SQLException exc) {
             throw new FxDbException(LOG, exc, "ex.db.sqlError", exc.getMessage());
         } finally {
             Database.closeObjects(PostgreSQLSequencerStorage.class, con, ps);
         }
     }
 }
