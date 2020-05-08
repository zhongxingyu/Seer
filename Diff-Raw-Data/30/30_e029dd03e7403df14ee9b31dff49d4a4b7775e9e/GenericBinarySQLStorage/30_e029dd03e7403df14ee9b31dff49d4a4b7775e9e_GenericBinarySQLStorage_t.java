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
 import com.flexive.core.storage.binary.BinaryInputStream;
 import com.flexive.core.storage.binary.BinaryStorage;
 import com.flexive.core.storage.binary.BinaryTransitFileInfo;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxSystemSequencer;
 import com.flexive.shared.FxXMLUtils;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptResult;
 import com.flexive.shared.stream.BinaryUploadPayload;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.stream.ServerLocation;
 import org.apache.commons.lang.StringUtils;
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
 
     //                                                                                                                                                                          1         2             3
     protected static final String BINARY_DESC_LOAD = "SELECT NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,ISIMAGE,RESOLUTION,WIDTH,HEIGHT FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
 
     //    select into xx () select  FBLOB1,FBLOB2,?,?,?
     protected static final String BINARY_TRANSIT_HEADER = "SELECT FBLOB FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
 
     //                                                                                     1  2   3             4(handle)5    6                  7                    8                    9       10         11    12
     protected static final String BINARY_TRANSIT = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1,PREV1_WIDTH,PREV1_HEIGHT,PREV2,PREV2_WIDTH,PREV2_HEIGHT,PREV3,PREV3_WIDTH,PREV3_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE) " +
             //      1 2 3       4 5 6       7                                 8 9 10 11
             "SELECT ?,?,?,FBLOB,?,?,?," + Database.getTimestampFunction() + ",?,PREVIEW_REF,?,?,?,?,PREV1,PREV1_WIDTH,PREV1_HEIGHT,PREV2,PREV2_WIDTH,PREV2_HEIGHT,PREV3,PREV3_WIDTH,PREV3_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                                                   1              2               3        4              5               6        7              8               9            10           11           12           13
     protected static final String BINARY_TRANSIT_PREVIEWS = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREV1=?, PREV1_WIDTH=?, PREV1_HEIGHT=?, PREV2=?, PREV2_WIDTH=?, PREV2_HEIGHT=?, PREV3=?, PREV3_WIDTH=?, PREV3_HEIGHT=?, PREV1SIZE=?, PREV2SIZE=?, PREV3SIZE=? WHERE BKEY=?";
     protected static final String BINARY_TRANSIT_PREVIEWS_REF = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREVIEW_REF=? WHERE BKEY=?";
 
     protected static final String CONTENT_BINARY_REMOVE = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID IN (SELECT DISTINCT d.FBLOB FROM " + TBL_CONTENT_DATA + " d WHERE d.ID=?)";
     protected static final String SQL_WHERE_VER = " AND VER=?";
     protected static final String CONTENT_BINARY_REMOVE_VER = CONTENT_BINARY_REMOVE + SQL_WHERE_VER;
     protected static final String CONTENT_BINARY_TRANSIT_CLEANUP = "DELETE FROM " + TBL_BINARY_TRANSIT + " WHERE EXPIRE<?";
     protected static final String CONTENT_BINARY_REMOVE_ID = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_TYPE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " d, " + TBL_CONTENT + " c WHERE d.ID=c.ID and c.TDEF=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_ID = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_TYPE = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
 
     /**
      * {@inheritDoc}
      */
     public OutputStream receiveTransitBinary(int divisionId, String handle, long expectedSize, long ttl) throws SQLException, IOException {
        return new GenericBinarySQLOutputStream(divisionId, handle, expectedSize, ttl);
     }
 
     /**
      * {@inheritDoc}
      */
     public BinaryInputStream fetchBinary(int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality) {
         Connection con = null;
         PreparedStatement ps = null;
         String mimeType;
         int datasize;
         try {
             con = Database.getDbConnection(divisionId);
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
             }
             long previewId = 0;
             ResultSet rs;
             if (!"FBLOB".equals(column)) {
                 //unless the real content is requested, try to find the correct preview image
                 ps = con.prepareStatement("SELECT PREVIEW_REF FROM " + TBL_CONTENT_BINARY +
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
             ps = con.prepareStatement("SELECT " + column + ",MIMETYPE," + sizeColumn + " FROM " + TBL_CONTENT_BINARY +
                     " WHERE ID=? AND VER=? AND QUALITY=?");
             if (previewId != 0)
                 ps.setLong(1, previewId);
             else
                 ps.setLong(1, binaryId);
             ps.setInt(2, binaryVersion);
             ps.setInt(3, binaryQuality);
             rs = ps.executeQuery();
             if (rs == null || !rs.next()) {
                 Database.closeObjects(GenericBinarySQLInputStream.class, con, ps);
                 return new GenericBinarySQLInputStream(false);
             }
             InputStream bin = rs.getBinaryStream(1);
             mimeType = rs.getString(2);
             datasize = rs.getInt(3);
             return new GenericBinarySQLInputStream(con, ps, true, bin, mimeType, datasize);
         } catch (SQLException e) {
             Database.closeObjects(GenericBinarySQLInputStream.class, con, ps);
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
                 return new BinaryDescriptor(server, id, 1, 1, rs.getLong(4), rs.getString(1), rs.getLong(2), rs.getString(3),
                         rs.getString(5), rs.getBoolean(6), rs.getDouble(7), rs.getInt(8), rs.getInt(9));
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
         File previewFile1 = null, previewFile2 = null, previewFile3 = null;
         FileInputStream pin1 = null, pin2 = null, pin3 = null;
         int[] dimensionsPreview1 = {0, 0};
         int[] dimensionsPreview2 = {0, 0};
         int[] dimensionsPreview3 = {0, 0};
         String metaData = "<empty/>";
         ResultSet rs = null;
         try {
             binaryTransitFileInfo = getBinaryTransitFileInfo(con, binary);
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
                 binding.setVariable("mimeType", binaryTransitFileInfo.getMimeType());
                 binding.setVariable("metaData", metaData);
                 binding.setVariable("binaryFile", binaryTransitFileInfo.getBinaryTransitFile());
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
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS_REF);
                 ps.setLong(1, defaultId);
                 ps.setString(2, binary.getHandle());
                 ps.executeUpdate();
             }
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
             } catch (IOException e) {
                 LOG.error("Stream closing failed: " + e.getMessage(), e);
             }
             if (binaryTransitFileInfo != null && !binaryTransitFileInfo.getBinaryTransitFile().delete())
                 binaryTransitFileInfo.getBinaryTransitFile().deleteOnExit();
             if (previewFile1 != null && !previewFile1.delete())
                 previewFile1.deleteOnExit();
             if (previewFile2 != null && !previewFile2.delete())
                 previewFile2.deleteOnExit();
             if (previewFile3 != null && !previewFile3.delete())
                 previewFile3.deleteOnExit();
             try {
                 if (rs != null) rs.close();
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
         return new BinaryDescriptor(binary.getHandle(), binary.getName(), binary.getSize(),
                 binaryTransitFileInfo.getMimeType(), metaData);
     }
 
     /**
      * Retrieve a File handle and mime type for a binary transit entry
      *
      * @param con    an open and valid connection
      * @param binary binary descriptor
      * @return BinaryTransitFileInfo containing a File handle and the detected mime type
      * @throws FxApplicationException on errors
      */
     protected BinaryTransitFileInfo getBinaryTransitFileInfo(Connection con, BinaryDescriptor binary) throws FxApplicationException {
         PreparedStatement ps = null;
         ResultSet rs = null;
         File binaryTransitFile = null;
         InputStream in = null;
         FileOutputStream fos = null;
         String mimeType = "unknown";
         try {
             ps = con.prepareStatement(BINARY_TRANSIT_HEADER);
             ps.setString(1, binary.getHandle());
 
             rs = ps.executeQuery();
             if (rs.next()) {
                 byte[] header = null;
                 try {
                     header = rs.getBlob(1).getBytes(1, 48);
                 } catch (Throwable t) {
                     // ignore, header migth be smaller than 48
                 }
                 mimeType = FxMediaEngine.detectMimeType(header, binary.getName());
             }
 
             binaryTransitFile = File.createTempFile("FXBIN_", "_TEMP");
             in = rs.getBlob(1).getBinaryStream();
             fos = new FileOutputStream(binaryTransitFile);
             byte[] buffer = new byte[4096];
             int read;
             while ((read = in.read(buffer)) != -1)
                 fos.write(buffer, 0, read);
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
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
         if (binaryTransitFile == null || !binaryTransitFile.exists())
             throw new FxApplicationException("ex.content.binary.transitNotFound", binary.getHandle());
         return new BinaryTransitFileInfo(binaryTransitFile, mimeType);
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
     public void removeBinariesForPK(Connection con, FxPK pk) throws FxApplicationException {
         PreparedStatement ps = null;
         List<Long> binaries = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET);
             ps.setLong(1, pk.getId());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID);
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
     public void removeBinariesForVersion(Connection con, FxPK pk) throws FxApplicationException {
         PreparedStatement ps = null;
         List<Long> binaries = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET + " AND VER=?");
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID + " AND VER=?");
             ps.setLong(1, pk.getId());
             ps.setInt(2, pk.getVersion());
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
     public void removeBinariesForType(Connection con, long typeId) throws FxApplicationException {
         PreparedStatement ps = null;
         List<Long> binaries = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_TYPE_GET);
             ps.setLong(1, typeId);
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
             ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_TYPE);
             ps.setLong(1, typeId);
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
 }
