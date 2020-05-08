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
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.binary.BinaryInputStream;
 import com.flexive.core.storage.binary.BinaryStorage;
 import com.flexive.core.storage.binary.BinaryTransitFileInfo;
 import com.flexive.core.storage.binary.FxBinaryUtils;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.interfaces.DivisionConfigurationEngine;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptResult;
 import com.flexive.shared.stream.BinaryUploadPayload;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.value.BinaryDescriptor;
 import static com.flexive.shared.value.BinaryDescriptor.PreviewSizes;
 import static com.flexive.shared.value.BinaryDescriptor.SYS_UNKNOWN;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.stream.ServerLocation;
 import org.apache.commons.lang.StringUtils;
 import static org.apache.commons.lang.StringUtils.defaultString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Generic SQL based implementation to access binaries
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericBinarySQLStorage implements BinaryStorage {
 
     private static final Log LOG = LogFactory.getLog(GenericBinarySQLStorage.class);
 
     protected static final String CONTENT_MAIN_BINARY_UPDATE = "UPDATE " + TBL_CONTENT + " SET " +
             //       1          2              3          4          5         6
             "DBIN_ID=?,DBIN_VER=?,DBIN_QUALITY=?,DBIN_ACL=? WHERE ID=? AND VER=?";
 
     //                                                       1    2        3          4        5       6          7     8      9                                                            1         2             3
     protected static final String BINARY_DESC_LOAD = "SELECT NAME,BLOBSIZE,CREATED_AT,MIMETYPE,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,MD5SUM FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
     protected static final String BINARY_META_LOAD = "SELECT XMLMETA FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
 
     //    select into xx () select  FBLOB1,FBLOB2,?,?,?
     protected static final String BINARY_TRANSIT_HEADER = "SELECT FBLOB FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                   1                   2         3         4         5
     protected static final String BINARY_TRANSIT_PREVIEW_SIZES = "SELECT PREVIEW_REF IS NULL,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
 
     protected static final String BINARY_TRANSIT = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,MD5SUM,PREV1,PREV2,PREV3,PREV4) " +
             //      1 2 3       4 5 6     7                                         8             9 0 1 2
             "SELECT ?,?,?,FBLOB,?,?,?," + StorageManager.getTimestampFunction() + ",?,PREVIEW_REF,?,?,?,?,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,?";
     protected static final String BINARY_TRANSIT_FILESYSTEM = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,MD5SUM,PREV1,PREV2,PREV3,PREV4) " +
             //      1 2 3 4 5 6 7     8                                         9             0 1 2 3                                                                                                                                                  
             "SELECT ?,?,?,?,?,?,?," + StorageManager.getTimestampFunction() + ",?,PREVIEW_REF,?,?,?,?,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,?";
 
     protected static final String BINARY_TRANSIT_PREVIEW_WHERE = " FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                                                   1              2               3        4              5               6        7              8               9        10             11              12           13           14           15           16           17
     protected static final String BINARY_TRANSIT_PREVIEWS = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREV1=?, PREV1_WIDTH=?, PREV1_HEIGHT=?, PREV2=?, PREV2_WIDTH=?, PREV2_HEIGHT=?, PREV3=?, PREV3_WIDTH=?, PREV3_HEIGHT=?, PREV4=?, PREV4_WIDTH=?, PREV4_HEIGHT=?, PREV1SIZE=?, PREV2SIZE=?, PREV3SIZE=?, PREV4SIZE=? WHERE BKEY=?";
     protected static final String BINARY_TRANSIT_PREVIEWS_REF = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREVIEW_REF=? WHERE BKEY=?";
 
     protected static final String CONTENT_BINARY_TRANSIT_CLEANUP = "DELETE FROM " + TBL_BINARY_TRANSIT + " WHERE EXPIRE<?";
     protected static final String CONTENT_BINARY_REMOVE_ID = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " WHERE FBLOB IS NOT NULL AND ID=?";
     protected static final String CONTENT_BINARY_REMOVE_TYPE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " d, " + TBL_CONTENT + " c WHERE FBLOB IS NOT NULL AND d.ID=c.ID and c.TDEF=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_ID = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_RESET_ID = "UPDATE " + TBL_CONTENT + " SET DBIN_ID=-1 WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_TYPE = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
    protected static final String CONTENT_BINARY_REMOVE_RESET_TYPE = "UPDATE " + TBL_CONTENT + " SET DBIN_ID=-1 WHERE ID IN (SELECT DISTINCT c.ID FROM " + TBL_CONTENT + " c WHERE c.TDEF=?)";
 
     /**
      * {@inheritDoc}
      */
     public OutputStream receiveTransitBinary(int divisionId, String handle, long expectedSize, long ttl) throws SQLException, IOException {
         try {
             if (EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB))
                 return new GenericBinarySQLOutputStream(divisionId, handle, expectedSize, ttl);
             else {
                 Connection con;
                 PreparedStatement ps = null;
                 con = Database.getDbConnection(divisionId);
                 try {
                     //create a dummy entry
                     ps = con.prepareStatement("INSERT INTO " + DatabaseConst.TBL_BINARY_TRANSIT + " (BKEY,FBLOB,TFER_DONE,EXPIRE) VALUES(?,NULL,TRUE,?)");
                     ps.setString(1, handle);
                     ps.setLong(2, System.currentTimeMillis() + ttl);
                     ps.executeUpdate();
                 } finally {
                     Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
                 }
                 return new FileOutputStream(FxBinaryUtils.createTransitFile(divisionId, handle, ttl));
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public BinaryInputStream fetchBinary(Connection con, int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality) {
         Connection _con = con;
         PreparedStatement ps = null;
         String mimeType;
         int datasize;
         try {
             if (_con == null)
                 _con = Database.getDbConnection(divisionId);
             String column = "FBLOB";
             String sizeColumn = "BLOBSIZE";
             switch (size) {
                 case PREVIEW1:
                     column = "PREV1";
                     sizeColumn = "PREV1SIZE";
                     break;
                 case PREVIEW2:
                     column = "PREV2";
                     sizeColumn = "PREV2SIZE";
                     break;
                 case PREVIEW3:
                     column = "PREV3";
                     sizeColumn = "PREV3SIZE";
                     break;
                 case SCREENVIEW:
                     column = "PREV4";
                     sizeColumn = "PREV4SIZE";
                     break;
             }
             long previewId = 0;
             ResultSet rs;
             if (!"FBLOB".equals(column)) {
                 //unless the real content is requested, try to find the correct preview image
                 ps = _con.prepareStatement("SELECT PREVIEW_REF FROM " + TBL_CONTENT_BINARY +
                         " WHERE ID=? AND VER=? AND QUALITY=?");
                 ps.setLong(1, binaryId);
                 ps.setInt(2, binaryVersion);
                 ps.setInt(3, binaryQuality);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     previewId = rs.getLong(1);
                     if (rs.wasNull())
                         previewId = 0;
                 }
                 ps.close();
             }
             ps = _con.prepareStatement("SELECT " + column + ",MIMETYPE," + sizeColumn + " FROM " + TBL_CONTENT_BINARY +
                     " WHERE ID=? AND VER=? AND QUALITY=?");
             if (previewId != 0)
                 ps.setLong(1, previewId);
             else
                 ps.setLong(1, binaryId);
             ps.setInt(2, binaryVersion);
             ps.setInt(3, binaryQuality);
             rs = ps.executeQuery();
             if (rs == null || !rs.next()) {
                 Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                 return new GenericBinarySQLInputStream(false);
             }
             InputStream bin = rs.getBinaryStream(1);
             if (rs.wasNull()) {
                 //try from filesystem
                 File fsBinary = FxBinaryUtils.getBinaryFile(divisionId, binaryId, binaryVersion, binaryQuality, size.getBlobIndex());
 
                 try {
                     if (fsBinary != null)
                         bin = new FileInputStream(fsBinary);
                     else {
                         if (size == PreviewSizes.SCREENVIEW) {
                             //since screenview is a new preview size, it might not exist in old versions. Fall back to Preview3
                             Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                             LOG.warn("Screenview for binary #" + binaryId + " not found! Falling back to Preview3 size.");
                             return this.fetchBinary(null, divisionId, PreviewSizes.PREVIEW3, binaryId, binaryVersion, binaryQuality);
                         }
                         LOG.error("Binary file #" + binaryId + "[" + size.name() + "] was not found!");
                         Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
                         return new GenericBinarySQLInputStream(false);
                     }
                 } catch (FileNotFoundException e) {
                     LOG.error("Binary not found on filesystem! Id=" + binaryId + ", version=" + binaryVersion + ", quality=" + binaryQuality + ", size=" + size.name());
                     Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
                     return new GenericBinarySQLInputStream(false);
                 }
             }
             mimeType = rs.getString(2);
             datasize = rs.getInt(3);
             if (rs.wasNull() && size == PreviewSizes.SCREENVIEW) {
                 Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                 LOG.warn("Screenview for binary #" + binaryId + " not found! Falling back to Preview3 size.");
                 return this.fetchBinary(null, divisionId, PreviewSizes.PREVIEW3, binaryId, binaryVersion, binaryQuality);
             }
             return new GenericBinarySQLInputStream(_con, ps, true, bin, mimeType, datasize);
         } catch (SQLException e) {
             Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
             return new GenericBinarySQLInputStream(false);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateContentBinaryEntry(Connection con, FxPK pk, long binaryId, long binaryACL) throws FxUpdateException {
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
      * {@inheritDoc}
      */
     public BinaryDescriptor loadBinaryDescriptor(List<ServerLocation> server, Connection con, long id) throws FxDbException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(BINARY_DESC_LOAD);
             ps.setLong(1, id);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //ver
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return new BinaryDescriptor(server, id, 1, 1, rs.getLong(3), rs.getString(1), rs.getLong(2), null,
                         rs.getString(4), rs.getBoolean(5), rs.getDouble(6), rs.getInt(7), rs.getInt(8), rs.getString(9));
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
         throw new FxDbException("ex.content.binary.loadDescriptor.failed", id);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getBinaryMetaData(Connection con, long binaryId) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(BINARY_META_LOAD);
             ps.setLong(1, binaryId);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //ver
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException e) {
             LOG.error(e);
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 LOG.error(e);
             }
         }
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     public BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary) throws FxApplicationException {
         long id = EJBLookup.getSequencerEngine().getId(FxSystemSequencer.BINARY);
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
      * @throws FxDbException on errors looking up the sequencer
      */
     private BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary, long id, int version, int quality) throws FxDbException {
         PreparedStatement ps = null;
         BinaryDescriptor created;
         FileInputStream fis = null;
         boolean dbTransit;
         boolean dbStorage;
         final long dbThreshold;
         final long dbPreviewThreshold;
         final int divisionId = FxContext.get().getDivisionId();
         try {
             final DivisionConfigurationEngine divisionConfig = EJBLookup.getDivisionConfigurationEngine();
             dbTransit = divisionConfig.get(SystemParameters.BINARY_TRANSIT_DB);
             if (id >= 0) {
                 dbThreshold = divisionConfig.get(SystemParameters.BINARY_DB_THRESHOLD);
                 dbPreviewThreshold = divisionConfig.get(SystemParameters.BINARY_DB_PREVIEW_THRESHOLD);
             } else {
                 //force storage of system binaries in the database
                 dbThreshold = -1;
                 dbPreviewThreshold = -1;
             }
             dbStorage = dbThreshold < 0 || binary.getSize() < dbThreshold;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         try {
             double resolution = 0.0;
             int width = 0;
             int height = 0;
             boolean isImage = binary.getMimeType().startsWith("image/");
             if (isImage) {
                 try {
                     width = Integer.parseInt(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "width"), "0"));
                     height = Integer.parseInt(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "height"), "0"));
                     resolution = Double.parseDouble(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "xResolution"), "0"));
                 } catch (NumberFormatException e) {
                     //ignore
                     LOG.warn(e, e);
                 }
             }
             created = new BinaryDescriptor(CacheAdmin.getStreamServers(), id, version, quality, java.lang.System.currentTimeMillis(),
                     binary.getName(), binary.getSize(), binary.getMetadata(), binary.getMimeType(), isImage, resolution, width, height, binary.getMd5sum());
             //we can copy the blob directly into the binary table if the database is used for transit and the final binary is
             //stored in the filesystem
             final boolean copyBlob = dbTransit && dbStorage;
             boolean storePrev1FS = false, storePrev2FS = false, storePrev3FS = false, storePrev4FS = false;
             long prev1Length = -1, prev2Length = -1, prev3Length = -1, prev4Length = -1;
             if (dbPreviewThreshold >= 0) {
                 //we have to check if preview should be stored on the filesystem
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEW_SIZES);
                 ps.setString(1, binary.getHandle());
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next())
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
                 if (rs.getBoolean(1)) {
                     //if previews are not referenced, check thresholds
                     storePrev1FS = (prev1Length = rs.getLong(2)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev2FS = (prev2Length = rs.getLong(3)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev3FS = (prev3Length = rs.getLong(4)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev4FS = (prev4Length = rs.getLong(5)) >= dbPreviewThreshold && !rs.wasNull();
                 }
             }
             if (ps != null)
                 ps.close();
             String previewSelect = (storePrev1FS ? ",NULL" : ",PREV1") + (storePrev2FS ? ",NULL" : ",PREV2") + (storePrev3FS ? ",NULL" : ",PREV3") + (storePrev4FS ? ",NULL" : ",PREV4");
             ps = con.prepareStatement((copyBlob ? BINARY_TRANSIT : BINARY_TRANSIT_FILESYSTEM) + previewSelect + BINARY_TRANSIT_PREVIEW_WHERE);
             ps.setLong(1, created.getId());
             ps.setInt(2, created.getVersion()); //version
             ps.setInt(3, created.getQuality()); //quality
             File binaryTransit = null;
             boolean removeTransitFile = false;
             if (dbTransit) {
                 //transit is handled in the database
                 try {
                     if (!dbStorage) {
                         //binaries are stored on the filesystem
                         binaryTransit = getBinaryTransitFileInfo(binary).getBinaryTransitFile();
                         removeTransitFile = true; //have to clean up afterwards since its a temporary file we get
                     }
                 } catch (FxApplicationException e) {
                     if (e instanceof FxDbException)
                         throw (FxDbException) e;
                     throw new FxDbException(e);
                 }
             } else {
                 //transit file resides on the local file system
                 binaryTransit = FxBinaryUtils.getTransitFile(divisionId, binary.getHandle());
                 if (binaryTransit == null)
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
             }
 
             if (!copyBlob) {
                 //we do not perform a simple blob copy operation in the database
                 if (dbStorage) {
                     //binary is stored in database -> copy it from the transit file (might be a temp. file)
                     fis = new FileInputStream(binaryTransit);
                     ps.setBinaryStream(4, fis, (int) binaryTransit.length());
                 } else {
                     //binary is stored on the filesystem -> copy transit file to binary storage file
                     try {
                         if (!FxFileUtils.copyFile(binaryTransit,
                                 FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                         PreviewSizes.ORIGINAL.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId());
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId(), e.getMessage());
                     }
                     ps.setNull(4, java.sql.Types.BINARY);
                 }
             }
 
             int cnt = copyBlob ? 4 : 5;
             ps.setString(cnt++, created.getName());
             ps.setLong(cnt++, created.getSize());
             ps.setString(cnt++, created.getMetadata());
             ps.setString(cnt++, created.getMimeType());
             ps.setBoolean(cnt++, created.isImage());
             ps.setDouble(cnt++, created.getResolution());
             ps.setInt(cnt++, created.getWidth());
             ps.setInt(cnt++, created.getHeight());
             ps.setString(cnt++, created.getMd5sum());
             ps.setString(cnt, binary.getHandle());
             ps.executeUpdate();
             if (removeTransitFile && binaryTransit != null) {
                 //transit file was a temp. file -> got to clean up
                 FxFileUtils.removeFile(binaryTransit);
             }
 
             //finally fetch the preview blobs from transit and store them on the filesystem if required
             if (storePrev1FS || storePrev2FS || storePrev3FS || storePrev4FS) {
                 ps.close();
                 previewSelect = (!storePrev1FS ? ",NULL" : ",PREV1") + (!storePrev2FS ? ",NULL" : ",PREV2") + (!storePrev3FS ? ",NULL" : ",PREV3") + (!storePrev4FS ? ",NULL" : ",PREV4");
                 ps = con.prepareStatement("SELECT " + previewSelect.substring(1) + BINARY_TRANSIT_PREVIEW_WHERE);
                 ps.setString(1, binary.getHandle());
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next())
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
                 if (storePrev1FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev1Length, rs.getBinaryStream(1), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW1.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW1.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW1.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev2FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev2Length, rs.getBinaryStream(2), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW2.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW2.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW2.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev3FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev3Length, rs.getBinaryStream(3), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW3.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW3.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW3.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev4FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev4Length, rs.getBinaryStream(4), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.SCREENVIEW.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.SCREENVIEW.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.SCREENVIEW.getBlobIndex() + "]", e.getMessage());
                     }
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } catch (FileNotFoundException e) {
             throw new FxDbException(e, "ex.content.binary.IOError", binary.getHandle());
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
             try {
                 if (fis != null) fis.close();
             } catch (IOException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.content.binary.IOError", binary.getHandle());
             }
         }
         return created;
     }
 
     /**
      * {@inheritDoc}
      */
     public void prepareBinary(Connection con, Map<String, String[]> mimeMetaMap, FxBinary binary) throws FxApplicationException {
         for (long lang : binary.getTranslatedLanguages()) {
             BinaryDescriptor bd = binary.getTranslation(lang);
             if (bd.isEmpty()) {
                 //remove empty languages to prevent further processing (FX-327)
                 binary.removeLanguage(lang);
                 continue;
             }
             if (!bd.isNewBinary())
                 continue;
             if (mimeMetaMap != null && mimeMetaMap.containsKey(bd.getHandle())) {
                 String[] mm = mimeMetaMap.get(bd.getHandle());
                 BinaryDescriptor bdNew = new BinaryDescriptor(bd.getHandle(), bd.getName(), bd.getSize(), mm[0], mm[1]);
                 binary.setTranslation(lang, bdNew);
             } else {
                 BinaryDescriptor bdNew = identifyAndTransferTransitBinary(con, bd);
                 if (mimeMetaMap == null)
                     bdNew = binaryTransit(con, bdNew);
                 binary.setTranslation(lang, bdNew);
                 if (mimeMetaMap != null)
                     mimeMetaMap.put(bdNew.getHandle(), new String[]{bdNew.getMimeType(), bdNew.getMetadata()});
             }
         }
     }
 
     /**
      * Identifies a binary in the transit table and generates previews etc.
      *
      * @param con    an open and valid Connection
      * @param binary the binary to identify
      * @return BinaryDescriptor
      * @throws FxApplicationException on errors
      */
     private BinaryDescriptor identifyAndTransferTransitBinary(Connection con, BinaryDescriptor binary) throws FxApplicationException {
         //check if already identified
         if (!StringUtils.isEmpty(binary.getMetadata()))
             return binary;
         PreparedStatement ps = null;
         BinaryTransitFileInfo binaryTransitFileInfo = null;
         File previewFile1 = null, previewFile2 = null, previewFile3 = null, previewFile4 = null;
         FileInputStream pin1 = null, pin2 = null, pin3 = null, pin4 = null;
         int[] dimensionsPreview1 = {0, 0};
         int[] dimensionsPreview2 = {0, 0};
         int[] dimensionsPreview3 = {0, 0};
         int[] dimensionsPreview4 = {0, 0};
         String metaData = "<empty/>";
         ResultSet rs = null;
         String md5sum = "";
         try {
             binaryTransitFileInfo = getBinaryTransitFileInfo(binary);
             boolean processed = false;
             boolean useDefaultPreview = true;
             int defaultId = SYS_UNKNOWN;
 
             FxScriptBinding binding;
             ScriptingEngine scripting = EJBLookup.getScriptingEngine();
             for (long script : scripting.getByScriptEvent(FxScriptEvent.BinaryPreviewProcess)) {
                 binding = new FxScriptBinding();
                 binding.setVariable("processed", processed);
                 binding.setVariable("useDefaultPreview", useDefaultPreview);
                 binding.setVariable("defaultId", defaultId);
                 binding.setVariable("mimeType", binaryTransitFileInfo.getMimeType());
                 binding.setVariable("metaData", metaData);
                 binding.setVariable("binaryFile", binaryTransitFileInfo.getBinaryTransitFile());
                 binding.setVariable("previewFile1", previewFile1);
                 binding.setVariable("previewFile2", previewFile2);
                 binding.setVariable("previewFile3", previewFile3);
                 binding.setVariable("previewFile4", previewFile4);
                 binding.setVariable("dimensionsPreview1", dimensionsPreview1);
                 binding.setVariable("dimensionsPreview2", dimensionsPreview2);
                 binding.setVariable("dimensionsPreview3", dimensionsPreview3);
                 binding.setVariable("dimensionsPreview4", dimensionsPreview4);
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
                         previewFile4 = (File) binding.getVariable("previewFile4");
                         dimensionsPreview1 = (int[]) binding.getVariable("dimensionsPreview1");
                         dimensionsPreview2 = (int[]) binding.getVariable("dimensionsPreview2");
                         dimensionsPreview3 = (int[]) binding.getVariable("dimensionsPreview3");
                         dimensionsPreview4 = (int[]) binding.getVariable("dimensionsPreview4");
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
                         previewFile4 == null || !previewFile4.exists() ||
                         dimensionsPreview1 == null || dimensionsPreview1.length != 2 || dimensionsPreview1[0] < 0 || dimensionsPreview1[1] < 0 ||
                         dimensionsPreview2 == null || dimensionsPreview2.length != 2 || dimensionsPreview2[0] < 0 || dimensionsPreview2[1] < 0 ||
                         dimensionsPreview3 == null || dimensionsPreview3.length != 2 || dimensionsPreview3[0] < 0 || dimensionsPreview3[1] < 0 ||
                         dimensionsPreview4 == null || dimensionsPreview4.length != 2 || dimensionsPreview4[0] < 0 || dimensionsPreview4[1] < 0
                         ) {
                     LOG.warn("Invalid preview parameters! Setting to default/unknown!");
                     useDefaultPreview = true;
                     defaultId = SYS_UNKNOWN;
                 }
             } else {
                 //only negative values are allowed
                 if (defaultId >= 0) {
                     defaultId = SYS_UNKNOWN;
                     LOG.warn("Only default preview id's that are negative and defined in BinaryDescriptor as constants are allowed!");
                 }
             }
 
             if (!useDefaultPreview) {
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS);
                 pin1 = new FileInputStream(previewFile1);
                 pin2 = new FileInputStream(previewFile2);
                 pin3 = new FileInputStream(previewFile3);
                 pin4 = new FileInputStream(previewFile4);
                 ps.setBinaryStream(1, pin1, (int) previewFile1.length());
                 ps.setInt(2, dimensionsPreview1[0]);
                 ps.setInt(3, dimensionsPreview1[1]);
                 ps.setBinaryStream(4, pin2, (int) previewFile2.length());
                 ps.setInt(5, dimensionsPreview2[0]);
                 ps.setInt(6, dimensionsPreview2[1]);
                 ps.setBinaryStream(7, pin3, (int) previewFile3.length());
                 ps.setInt(8, dimensionsPreview3[0]);
                 ps.setInt(9, dimensionsPreview3[1]);
                 ps.setBinaryStream(10, pin4, (int) previewFile4.length());
                 ps.setInt(11, dimensionsPreview4[0]);
                 ps.setInt(12, dimensionsPreview4[1]);
                 ps.setInt(13, (int) previewFile1.length());
                 ps.setInt(14, (int) previewFile2.length());
                 ps.setInt(15, (int) previewFile3.length());
                 ps.setInt(16, (int) previewFile4.length());
                 ps.setString(17, binary.getHandle());
                 ps.executeUpdate();
             } else {
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS_REF);
                 ps.setLong(1, defaultId);
                 ps.setString(2, binary.getHandle());
                 ps.executeUpdate();
             }
             md5sum = FxSharedUtils.getMD5Sum(binaryTransitFileInfo.getBinaryTransitFile());
         } catch (IOException e) {
             LOG.error("Stream reading failed:" + e.getMessage(), e);
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (pin1 != null)
                     pin1.close();
                 if (pin2 != null)
                     pin2.close();
                 if (pin3 != null)
                     pin3.close();
                 if (pin4 != null)
                     pin4.close();
             } catch (IOException e) {
                 LOG.error("Stream closing failed: " + e.getMessage(), e);
             }
             if (binaryTransitFileInfo != null && binaryTransitFileInfo.isDBStorage())
                 FxFileUtils.removeFile(binaryTransitFileInfo.getBinaryTransitFile());
             FxFileUtils.removeFile(previewFile1);
             FxFileUtils.removeFile(previewFile2);
             FxFileUtils.removeFile(previewFile3);
             FxFileUtils.removeFile(previewFile4);
             try {
                 if (rs != null) rs.close();
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
         return new BinaryDescriptor(binary.getHandle(), binary.getName(), binary.getSize(),
                 binaryTransitFileInfo.getMimeType(), metaData, md5sum);
     }
 
     /**
      * Retrieve a File handle and mime type for a binary transit entry
      *
      * @param binary binary descriptor
      * @return BinaryTransitFileInfo containing a File handle and the detected mime type
      * @throws FxApplicationException on errors
      */
     @SuppressWarnings({"ThrowFromFinallyBlock"})
     protected BinaryTransitFileInfo getBinaryTransitFileInfo(BinaryDescriptor binary) throws FxApplicationException {
         PreparedStatement ps = null;
         ResultSet rs = null;
         File binaryTransitFile = null;
         InputStream in = null;
         FileOutputStream fos = null;
         String mimeType = "unknown";
         byte[] header = null;
         boolean inDB;
         try {
             inDB = EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         if (inDB) {
             Connection con = null;
             try {
                 con = Database.getNonTXDataSource().getConnection();
                 ps = con.prepareStatement(BINARY_TRANSIT_HEADER);
                 ps.setString(1, binary.getHandle());
 
                 rs = ps.executeQuery();
                 if (!rs.next()) {
                     throw new FxApplicationException(LOG, "ex.content.binary.transitNotFound", binary.getHandle());
                 }
 
                 binaryTransitFile = File.createTempFile("FXBIN_", "_TEMP");
                 in = rs.getBinaryStream(1);
                 fos = new FileOutputStream(binaryTransitFile);
                 byte[] buffer = new byte[4096];
                 int read;
                 while ((read = in.read(buffer)) != -1) {
                     if (header == null && read > 0) {
                         header = new byte[read > 48 ? 48 : read];
                         System.arraycopy(buffer, 0, header, 0, read > 48 ? 48 : read);
                     }
                     fos.write(buffer, 0, read);
                 }
                 fos.close();
                 fos = null;
                 in.close();
                 in = null;
             } catch (FileNotFoundException e) {
                 throw new FxApplicationException(e, "ex.content.binary.transitNotFound", binary.getHandle());
             } catch (IOException e) {
                 throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
             } catch (SQLException e) {
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             } finally {
                 try {
                     if (fos != null) fos.close();
                     if (in != null) in.close();
                 } catch (IOException e) {
                     //noinspection ThrowFromFinallyBlock
                     throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
                 }
                 try {
                     if (rs != null) rs.close();
                 } catch (SQLException e) {
                     //noinspection ThrowFromFinallyBlock
                     throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
                 }
                 Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
             }
         } else {
             binaryTransitFile = FxBinaryUtils.getTransitFile(FxContext.get().getDivisionId(), binary.getHandle());
             if (binaryTransitFile != null) {
                 try {
                     in = new FileInputStream(binaryTransitFile);
                     header = new byte[48];
                     if (in.read(header, 0, 48) < 48)
                         header = null;
                 } catch (IOException e) {
                     throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
                 } finally {
                     try {
                         in.close();
                     } catch (IOException e) {
                         throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
                     }
                 }
             } else
                 throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
         }
         if (header != null)
             mimeType = FxMediaEngine.detectMimeType(header, binary.getName());
 
         if (binaryTransitFile == null || !binaryTransitFile.exists())
             throw new FxApplicationException("ex.content.binary.transitNotFound", binary.getHandle());
         return new BinaryTransitFileInfo(binaryTransitFile, mimeType, inDB);
     }
 
     /**
      * {@inheritDoc}
      */
     public void storeBinary(Connection con, long id, int version, int quality, String name, long length, InputStream binary) throws FxApplicationException {
         BinaryUploadPayload payload = FxStreamUtils.uploadBinary(length, binary);
         BinaryDescriptor desc = new BinaryDescriptor(payload.getHandle(), name, length, null, null);
         desc = identifyAndTransferTransitBinary(con, desc);
         binaryTransit(con, desc, id, version, quality);
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateBinaryPreview(Connection con, long id, int version, int quality, int preview, int width, int height, long length, InputStream binary) {
         //TODO: code me!
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeBinaries(Connection con, SelectOperation remOp, FxPK pk, long typeId) throws FxApplicationException {
         PreparedStatement ps = null;
         List<Long> binaries = null;
         try {
             //query affected binary id's
             switch (remOp) {
                 case SelectId:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET);
                     ps.setLong(1, pk.getId());
                     break;
                 case SelectVersion:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET + " AND VER=?");
                     ps.setLong(1, pk.getId());
                     ps.setInt(2, pk.getVersion());
                     break;
                 case SelectType:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_TYPE_GET);
                     ps.setLong(1, typeId);
                     break;
                 default:
                     return;
             }
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
 
             //reset data
             switch (remOp) {
                 case SelectId:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID);
                     ps.setLong(1, pk.getId());
                     ps.executeUpdate();
                     ps.close();
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_ID);
                     ps.setLong(1, pk.getId());
                     ps.executeUpdate();
                     break;
                 case SelectVersion:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID + " AND VER=?");
                     ps.setLong(1, pk.getId());
                     ps.setInt(2, pk.getVersion());
                     ps.executeUpdate();
                     ps.close();
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_ID + " AND VER=?");
                     ps.setLong(1, pk.getId());
                     ps.setInt(2, pk.getVersion());
                     ps.executeUpdate();
                     break;
                 case SelectType:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_TYPE);
                     ps.setLong(1, typeId);
                     ps.executeUpdate();
                     ps.close();
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_TYPE);
                     ps.setLong(1, typeId);
                     ps.executeUpdate();
                     break;
                 default:
                     return;
             }
             if (binaries != null)
                 removeBinaries(con, binaries);
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             try {
                 if (ps != null)
                     ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeExpiredTransitEntries(Connection con) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_TRANSIT_CLEANUP);
             ps.setLong(1, System.currentTimeMillis());
             int count = ps.executeUpdate();
             int fscount = 0;
             if (!EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB))
                 fscount = FxBinaryUtils.removeExpiredTransitFiles(FxContext.get().getDivisionId());
             if (count > 0 || fscount > 0)
                 LOG.info(count + " expired binary transit entries removed" +
                         (fscount > 0 ? " (" + fscount + " on the filesystem)" : ""));
         } catch (SQLException e) {
             LOG.error(e, e);
         } catch (FxApplicationException e) {
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
      * Get the usage count of a binary
      *
      * @param ps valid prepared statement for the usage query
      * @param id id of the binary
      * @return usage count
      * @throws SQLException on errors
      */
     private long getUsageCount(PreparedStatement ps, long id) throws SQLException {
         ps.setLong(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs != null && rs.next())
             return rs.getLong(1);
         return 0;
     }
 
     /**
      * Remove the given list of binaries if they are not in use
      *
      * @param con      an open and valid connection
      * @param binaries list of binaries to remove
      * @throws SQLException on errors
      */
     protected void removeBinaries(Connection con, List<Long> binaries) throws SQLException {
         PreparedStatement psRemove = null;
         PreparedStatement psUsage1 = null;
         PreparedStatement psUsage2 = null;
         PreparedStatement psUsage3 = null;
         PreparedStatement psUsage4 = null;
         try {
             psRemove = con.prepareStatement(CONTENT_BINARY_REMOVE_ID);
             psUsage1 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT + " WHERE DBIN_ID=?");
             psUsage2 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE FBLOB=?");
             psUsage3 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_SELECTLIST_ITEM + " WHERE DBIN_ID=?");
             psUsage4 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_BINARY + " WHERE PREVIEW_REF=?");
             int divisionId = FxContext.get().getDivisionId();
             long cnt1, cnt2, cnt3, cnt4;
             for (Long id : binaries) {
                 cnt2 = cnt3 = cnt4 = 0;
                 cnt1 = getUsageCount(psUsage1, id);
                 if (cnt1 <= 0) cnt2 = getUsageCount(psUsage2, id);
                 if (cnt2 == 0) cnt3 = getUsageCount(psUsage3, id);
                 if (cnt3 == 0) cnt4 = getUsageCount(psUsage4, id);
                 if (cnt1 + cnt2 + cnt3 + cnt4 > 0) {
                     if (LOG.isDebugEnabled())
                         LOG.debug("Binary #" + id + " is in use! (content:" + cnt1 + ",content_data:" + cnt2 + ",selectlist:" + cnt3 + ",binary:" + cnt4 + ") - only the first usage is calculated, rest is set to 0!");
                     continue;
                 } else {
                     if (LOG.isDebugEnabled()) LOG.debug("Removing binary #" + id);
                 }
 
                 psRemove.setLong(1, id);
                 psRemove.executeUpdate();
                 FxBinaryUtils.removeBinary(divisionId, id);
             }
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, null, psRemove);
             Database.closeObjects(GenericBinarySQLStorage.class, null, psUsage1);
             Database.closeObjects(GenericBinarySQLStorage.class, null, psUsage2);
             Database.closeObjects(GenericBinarySQLStorage.class, null, psUsage3);
             Database.closeObjects(GenericBinarySQLStorage.class, null, psUsage4);
 
         }
     }
 }
