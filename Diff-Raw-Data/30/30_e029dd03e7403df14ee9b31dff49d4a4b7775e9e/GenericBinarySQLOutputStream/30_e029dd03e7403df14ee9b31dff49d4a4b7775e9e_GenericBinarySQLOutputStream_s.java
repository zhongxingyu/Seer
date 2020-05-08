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
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 /**
  * Generic SQL based implementation of a PipedOutputStream to store binaries in the transit space
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @see com.flexive.core.stream.BinaryUploadProtocol
  */
 public class GenericBinarySQLOutputStream extends PipedOutputStream implements Runnable {
     private static final Log LOG = LogFactory.getLog(GenericBinarySQLOutputStream.class);
 
     PipedInputStream pipe;
    Connection con;
     private String handle;
     private long expectedSize;
     private long ttl;
     private long count;
     private Thread rcvThread;
 
     /**
      * Ctor
      *
     * @param con          an open and valid db connection
      * @param handle       binary handle
      * @param expectedSize expected size of the binary
      * @param ttl          time to live in the transit space
      * @throws IOException on errors
      */
    GenericBinarySQLOutputStream(Connection con, String handle, long expectedSize, long ttl) throws IOException {
        this.con = con;
         this.handle = handle;
         this.expectedSize = expectedSize;
         this.ttl = ttl;
         this.pipe = new PipedInputStream(this);
         this.count = 0L;
         this.rcvThread = new Thread(this);
         this.rcvThread.setDaemon(true);
         this.rcvThread.start();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void close() throws IOException {
         super.close();
         try {
             rcvThread.join();
         } catch (InterruptedException e) {
             LOG.error("Receiving thread got interrupted: " + e.getMessage(), e);
         }
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("UPDATE " + DatabaseConst.TBL_BINARY_TRANSIT + " SET TFER_DONE=?, BLOBSIZE=? WHERE BKEY=?");
             ps.setBoolean(1, true);
             ps.setLong(2, count);
             ps.setString(3, handle);
             ps.executeUpdate();
         } catch (SQLException e) {
             LOG.error("SQL error marking binary as finished: " + e.getMessage(), e);
         } finally {
             Database.closeObjects(GenericBinarySQLOutputStream.class, con, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void write(byte[] b, int off, int len) throws IOException {
         super.write(b, off, len);
         count += len; //keep track of the byte count
     }
 
     /**
      * {@inheritDoc}
      */
     public void run() {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("INSERT INTO " + DatabaseConst.TBL_BINARY_TRANSIT + " (BKEY,FBLOB,TFER_DONE,EXPIRE) VALUES(?,?,FALSE,?)");
             ps.setString(1, handle);
             ps.setBinaryStream(2, pipe, (int) expectedSize);
             ps.setLong(3, System.currentTimeMillis() + ttl);
             long time = System.currentTimeMillis();
             ps.executeUpdate();
             if (LOG.isDebugEnabled())
                 LOG.debug("Stored " + count + "/" + expectedSize + " bytes in " + (System.currentTimeMillis() - time) + "[ms] in DB");
             try {
                 pipe.close();
             } catch (IOException e) {
                 LOG.error("IO error closing pipe: " + e.getMessage(), e);
             }
         } catch (SQLException e) {
             LOG.error("SQL error storing binary: " + e.getMessage(), e);
         } finally {
            Database.closeObjects(GenericBinarySQLOutputStream.class, null, ps);
         }
     }
 }
