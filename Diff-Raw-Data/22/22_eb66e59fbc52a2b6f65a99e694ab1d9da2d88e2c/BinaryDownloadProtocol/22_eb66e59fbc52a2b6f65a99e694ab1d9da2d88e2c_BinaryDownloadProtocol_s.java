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
 package com.flexive.core.stream;
 
 import com.flexive.core.Database;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.binary.BinaryInputStream;
 import com.flexive.core.storage.genericSQL.GenericBinarySQLInputStream;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.stream.BinaryDownloadPayload;
 import com.flexive.shared.structure.TypeStorageMode;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.stream.DataPacket;
 import com.flexive.stream.StreamException;
 import com.flexive.stream.StreamProtocol;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * Stream protocol to download binaries
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class BinaryDownloadProtocol extends StreamProtocol<BinaryDownloadPayload> {
 
     protected static final Log LOG = LogFactory.getLog(BinaryDownloadProtocol.class);
 
     private BinaryInputStream bin = null;
     private byte[] _buffer;
     private int pre_read = 0;
 
     /**
      * Ctor
      */
     public BinaryDownloadProtocol() {
         super(BinaryDownloadPayload.class);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public StreamProtocol<BinaryDownloadPayload> getInstance() {
         return new BinaryDownloadProtocol();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean canHandle(DataPacket<BinaryDownloadPayload> dataPacket) throws StreamException {
         return true;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public DataPacket<BinaryDownloadPayload> processPacket(DataPacket<BinaryDownloadPayload> dataPacket) throws StreamException {
         if (dataPacket.isExpectResponse() && dataPacket.isExpectStream()) {
             String mimeType;
             int datasize;
 
             final BinaryDescriptor.PreviewSizes previewSize = BinaryDescriptor.PreviewSizes.fromSize(dataPacket.getPayload().getSize());
             try {
                 bin = loadBinaryDescriptor(dataPacket, previewSize);
 
                 if (dataPacket.getPayload().isForceImage()
                         && previewSize == BinaryDescriptor.PreviewSizes.ORIGINAL
                         && !bin.getMimeType().startsWith("image")) {
                     // choose biggest preview size if an image is required, but the binary is no image
                     bin = loadBinaryDescriptor(dataPacket, BinaryDescriptor.PreviewSizes.SCREENVIEW);
                 }
             } catch (FxNotFoundException e) {
                 LOG.error("Failed to lookup content storage for division #" + dataPacket.getPayload().getDivision() + ": " + e.getLocalizedMessage());
             }
            if (!bin.isBinaryFound())
                 return new DataPacket<BinaryDownloadPayload>(new BinaryDownloadPayload(true, "ex.stream.notFound"), false);
             mimeType = bin.getMimeType();
             datasize = bin.getSize();
             _buffer = new byte[4096];
             if (!mimeType.startsWith("image")) {
                 try {
                     pre_read = bin.read(_buffer, 0, 100);
                 } catch (IOException e) {
                     //silently ignore
                 }
                 String tmp = FxMediaEngine.detectMimeType(_buffer);
                 if (tmp.startsWith("image"))
                     mimeType = tmp;
             } else
                 pre_read = 0;
             return new DataPacket<BinaryDownloadPayload>(new BinaryDownloadPayload(mimeType, datasize), false);
         }
         return null;
     }
 
     private BinaryInputStream loadBinaryDescriptor(DataPacket<BinaryDownloadPayload> dataPacket, BinaryDescriptor.PreviewSizes previewSize) throws FxNotFoundException {
         try {
             final Connection con = Database.getDbConnection(dataPacket.getPayload().getDivision());
             BinaryInputStream o = StorageManager.getContentStorage(TypeStorageMode.Hierarchical).fetchBinary(
                     con,
                     dataPacket.getPayload().getDivision(),
                     previewSize,
                     dataPacket.getPayload().getId(),
                     dataPacket.getPayload().getVersion(),
                     dataPacket.getPayload().getQuality());
             if (!o.isBinaryFound())
                 Database.closeObjects(BinaryDownloadProtocol.class, con, null);
             return o;
         } catch (SQLException e) {
             LOG.error(e);
             return new GenericBinarySQLInputStream(false);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void closeResources() {
         try {
             if (bin != null) bin.close();
         } catch (IOException e) {
             LOG.error("Failed to close binary input stream: " + e.getMessage(), e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean sendStream(ByteBuffer buffer) throws IOException {
         if (buffer.remaining() <= 0 || bin == null)
             return true; //should not happen but possible ...
         if (pre_read > 0) {
             buffer.put(_buffer, 0, pre_read); //size should not matter since its only 100 bytes
             pre_read = 0;
         }
         int max = Math.min(buffer.remaining(), _buffer.length);
         int read = bin.read(_buffer, 0, max);
         if (read == -1) {
             return false;
         }
         buffer.put(_buffer, 0, read);
         return true;
     }
 }
