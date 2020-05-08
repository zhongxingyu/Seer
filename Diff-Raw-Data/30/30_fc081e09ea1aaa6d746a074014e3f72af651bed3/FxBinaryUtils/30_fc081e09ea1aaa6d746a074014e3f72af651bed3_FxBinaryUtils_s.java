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
 package com.flexive.core.storage.binary;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxFileUtils;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.FxApplicationException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Helper class for filesystem related binary handling
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @since 3.1
  */
 public class FxBinaryUtils {
 
     private static final Log LOG = LogFactory.getLog(FxBinaryUtils.class);
 
     static final String TRANSIT_EXT = ".fxt";
     static final String BINARY_EXT = ".fxb";
     static final String CTX_TRANSIT_DIR = "FxTransitDir";
     static final String CTX_BINARY_DIR = "FxBinaryDir";
     static final String CTX_FXBINARY_TX = "FxBinaryTX";
 
     /**
      * Get the transit directory for the current node
      *
      * @return transit directory for the current node
      */
     public static synchronized String getTransitDirectory() {
         String cacheDir = (String) FxContext.get().getAttribute(CTX_TRANSIT_DIR);
         if (cacheDir != null)
             return cacheDir;
         String path;
         try {
             path = EJBLookup.getNodeConfigurationEngine().get(SystemParameters.NODE_TRANSIT_PATH);
         } catch (FxApplicationException e) {
             LOG.error(e);
             path = "~" + File.separatorChar + "flexive" + File.separatorChar + "transit";
         }
         File dir = new File(FxFileUtils.expandPath(path));
         if (!dir.exists() && !dir.mkdirs()) {
             LOG.error("Could not create directory: " + dir.getAbsolutePath());
         }
         FxContext.get().setAttribute(CTX_TRANSIT_DIR, dir.getAbsolutePath());
         return dir.getAbsolutePath();
     }
 
     /**
      * Get the binary directory for the current node
      *
      * @return binary directory for the current node
      */
     public static synchronized String getBinaryDirectory() {
         String cacheDir = (String) FxContext.get().getAttribute(CTX_BINARY_DIR);
         if (cacheDir != null)
             return cacheDir;
         String path;
         try {
             path = EJBLookup.getNodeConfigurationEngine().get(SystemParameters.NODE_BINARY_PATH);
         } catch (FxApplicationException e) {
             LOG.error(e);
             path = "~" + File.separatorChar + "flexive" + File.separatorChar + "binaries";
         }
         File dir = new File(FxFileUtils.expandPath(path));
         if (!dir.exists() && !dir.mkdirs()) {
             LOG.error("Could not create directory: " + dir.getAbsolutePath());
         }
         FxContext.get().setAttribute(CTX_BINARY_DIR, dir.getAbsolutePath());
         return dir.getAbsolutePath();
     }
 
     /**
      * Create a new binary transit file for the given division and handle
      *
      * @param divisionId division
      * @param handle     binary handle
      * @param ttl        time to live (will be part of the filename)
      * @return transit file
      * @throws IOException on errors
      */
     public static File createTransitFile(int divisionId, String handle, long ttl) throws IOException {
         File baseDir = new File(getTransitDirectory() + File.separatorChar + String.valueOf(divisionId));
         if (!baseDir.exists())
             if (!baseDir.mkdirs())
                 throw new IOException("Failed to create directory " + baseDir.getAbsolutePath());
         File result = new File(baseDir.getAbsolutePath() + File.separatorChar + handle + "__" + String.valueOf(ttl) + TRANSIT_EXT);
         if (!result.createNewFile())
             throw new IOException("Failed to create file " + result.getAbsolutePath());
         return result;
     }
 
     /**
      * Get an existing transit file for the given division and handle
      *
      * @param divisionId division
      * @param handle     binary handle
      * @return the transit file or <code>null</code> if not found
      */
     public static File getTransitFile(int divisionId, final String handle) {
         File dir = new File(getTransitDirectory() + File.separatorChar + String.valueOf(divisionId) + File.separatorChar);
         File[] result = dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.startsWith(handle + "__");
             }
         });
         if (result.length > 0)
             return result[0];
         return null;
     }
 
     /**
      * Remove all expired transit files for the given division
      *
      * @param divisionId division
      * @return number of expired and removed transit files
      */
     public static int removeExpiredTransitFiles(int divisionId) {
         File baseDir = new File(getTransitDirectory() + File.separatorChar + String.valueOf(divisionId));
         if (!baseDir.exists())
             return 0; //nothing to do
         final long now = System.currentTimeMillis();
         int count = 0;
         for (File expired : baseDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 try {
                     if (name.endsWith(TRANSIT_EXT) && name.indexOf("__") > 0) {
                         long ttl = Long.valueOf(name.split("__")[1].split("\\.")[0]);
                         if (new File(dir.getAbsolutePath() + File.separator + name).lastModified() + ttl < now)
                             return true;
                     }
                 } catch (Exception e) {
                     LOG.error(e);
                     return false;
                 }
                 return false;
             }
         })) {
             count++;
             FxFileUtils.removeFile(expired);
         }
         return count;
     }
 
     /**
      * Get the subdirectory based on the binary id to provide load distribution
      *
      * @param binaryId binary id
      * @return subdirectory
      */
     private static String getBinarySubDirectory(long binaryId) {
         return File.separator + String.valueOf(binaryId % 100);
     }
 
     /**
      * Create a new binary file
      *
      * @param divisionId division
      * @param binaryId   id of the binary
      * @param version    binary version
      * @param quality    binary quality
      * @param blobIndex  blob index (original, preview, etc.)
      * @return created File handle
      * @throws IOException on errors
      * @see com.flexive.shared.value.BinaryDescriptor.PreviewSizes#getBlobIndex()
      */
     public static File createBinaryFile(int divisionId, long binaryId, int version, int quality, int blobIndex) throws IOException {
         File baseDir = new File(getBinaryDirectory() + File.separatorChar + String.valueOf(divisionId) + getBinarySubDirectory(binaryId));
         if (!baseDir.exists())
             if (!baseDir.mkdirs())
                 throw new IOException("Failed to create directory " + baseDir.getAbsolutePath());
         //Fileformat: <id>_<version>_<quality>_<blobId>.fxb
         File result = new File(baseDir.getAbsolutePath() + File.separatorChar +
                 String.valueOf(binaryId) + "_" + String.valueOf(version) + "_" + String.valueOf(quality) + "_" + String.valueOf(blobIndex) +
                 BINARY_EXT);
        if (!result.createNewFile())
            throw new IOException("Failed to create file " + result.getAbsolutePath());
         FxContext c = FxContext.get();
         @SuppressWarnings({"unchecked"}) List<String> createdFiles = (List<String>) c.getAttribute(CTX_FXBINARY_TX);
         if (createdFiles == null)
             createdFiles = new ArrayList<String>(12);
         createdFiles.add(result.getAbsolutePath());
         c.setAttribute(CTX_FXBINARY_TX, createdFiles);
         return result;
     }
 
     /**
      * Get an existing binary file for the given division and id/version/quality/blobIndex
      *
      * @param divisionId division
      * @param binaryId   id of the binary
      * @param version    version of the binary
      * @param quality    quality
      * @param blobIndex  blob index
      * @return the transit file or <code>null</code> if not found
      */
     public static File getBinaryFile(int divisionId, long binaryId, int version, int quality, int blobIndex) {
         File result = new File(getBinaryDirectory() + File.separatorChar + String.valueOf(divisionId) + File.separatorChar +
                 getBinarySubDirectory(binaryId) + File.separatorChar +
                 String.valueOf(binaryId) + "_" + String.valueOf(version) + "_" + String.valueOf(quality) + "_" + String.valueOf(blobIndex) +
                 BINARY_EXT);
         try {
             return result.exists() ? result : null;
         } finally {
             if (!result.exists())
                 //noinspection UnusedAssignment
                 result = null; //force gc
         }
     }
 
     /**
      * Remove all files created in current transaction
      */
     public static void removeTXFiles() {
         @SuppressWarnings({"unchecked"}) List<String> createdFiles = (List<String>) FxContext.get().getAttribute(CTX_FXBINARY_TX);
         if (createdFiles != null) {
             for (String f : createdFiles)
                 FxFileUtils.removeFile(f);
         }
     }
 
     /**
      * Reset all information about file created in current transaction
      */
     public static void resetTXFiles() {
         final FxContext context = FxContext.get();
         if (context.getAttribute(CTX_FXBINARY_TX) != null)
             context.setAttribute(CTX_FXBINARY_TX, null);
     }
 
     /**
      * Remove all files related to a binary
      *
      * @param divisionId division id
      * @param binaryId   binary id
      */
     public static void removeBinary(int divisionId, final long binaryId) {
         File baseDir = new File(getBinaryDirectory() + File.separatorChar + String.valueOf(divisionId) + getBinarySubDirectory(binaryId));
         if (!baseDir.exists())
             return;
         for (File rm : baseDir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.startsWith(String.valueOf(binaryId) + "_");
             }
         }))
             FxFileUtils.removeFile(rm);
     }
 
 }
