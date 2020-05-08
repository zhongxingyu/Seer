 /*******************************************************************************
  * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  *    Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
  *    may be used to endorse or promote products derived from this software without
  *    specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package org.cyclades.io;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 import com.google.common.io.ByteStreams;
 
 public class Zip {
 
     public static void zipDirectory (String sourceDir, String destinationFile) throws Exception {
         final String eLabel = "Zip.zipDirectory: ";
         FileOutputStream dest = null;
         ZipOutputStream out = null;
         try {
             File sourceDirFile = new File(sourceDir);
             if (!sourceDirFile.exists()) throw new Exception("Directory does not exist: " + sourceDir);
             if (sourceDirFile.isFile()) throw new Exception("Not a directory: " + sourceDir);
             dest = new FileOutputStream(destinationFile);
             out = new ZipOutputStream(dest);
             //out.setMethod(ZipOutputStream.DEFLATED);
             addResourcesToZip(sourceDirFile, out, sourceDirFile);
         } catch(Exception e) {
            throw new Exception(eLabel + e);
         } finally {
             if (out != null) try { out.close(); } catch (Exception e) {}
             if (dest != null) try { dest.close(); } catch (Exception e) {}
         }
     }
 
    private static void addResourcesToZip (File sourceFile, ZipOutputStream out, File base) throws Exception {
         final String eLabel = "Zip.addResourcesToZip: ";
         try {
            FileInputStream fis = null;
            ZipEntry entry;
           File[] children = sourceFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (!children[i].isFile()) {
                    addResourcesToZip(children[i], out, base);
                } else {
                    try {
                        fis = new FileInputStream(children[i]);
                        entry = new ZipEntry(children[i].getAbsolutePath().substring(base.getAbsolutePath().length() + 1));
                        out.putNextEntry(entry);
                        ByteStreams.copy(fis, out);
                        out.closeEntry();
                    } finally {
                          if (fis != null) try { fis.close(); } catch (Exception e) {}
                    }
                }
            }
         } catch(Exception e) {
            throw new Exception(eLabel + e);
         }
     }
 
     public static void unzipDirectory (String sourceFile, String destinationDirectory) throws Exception {
         final String eLabel = "Zip.unzipDirectory: ";
         FileInputStream fis = null;
         ZipInputStream zis = null;
         FileOutputStream fos = null;
         try {
            fis = new FileInputStream(sourceFile);
            zis = new ZipInputStream(fis);
            ZipEntry entry;
            FileUtils.verifyOutputDirectory(destinationDirectory);
            StringBuilder sb = new StringBuilder();
            String targetResource;
            while((entry = zis.getNextEntry()) != null) {
                sb.setLength(0);
                targetResource = sb.append(destinationDirectory).append("/").append(entry.getName()).toString();
                FileUtils.verifyFileOutputDirectory(targetResource);
                try {
                    fos = new FileOutputStream(targetResource);
                    ByteStreams.copy(zis, fos);
                } finally {
                    if (fos != null) try { fos.close(); } catch (Exception e) {}
                }
            }
         } catch(Exception e) {
            throw new Exception(eLabel + e);
         } finally {
             if (zis != null) try { zis.close(); } catch (Exception e) {}
             if (fis != null) try { fis.close(); } catch (Exception e) {}
         }
      }
 
 }
