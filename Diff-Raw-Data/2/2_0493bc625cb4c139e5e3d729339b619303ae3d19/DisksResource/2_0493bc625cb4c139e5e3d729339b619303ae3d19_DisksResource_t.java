 /*
  Created as part of the StratusLab project (http://stratuslab.eu),
  co-funded by the European Commission under the Grant Agreement
  INSFO-RI-261552.
 
  Copyright (c) 2011, Centre National de la Recherche Scientifique (CNRS)
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 package eu.stratuslab.storage.disk.resources;
 
 import static org.restlet.data.MediaType.APPLICATION_JSON;
 import static org.restlet.data.MediaType.TEXT_HTML;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.ext.fileupload.RestletFileUpload;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.storage.disk.main.ServiceConfiguration;
 import eu.stratuslab.storage.disk.utils.DiskUtils;
 import eu.stratuslab.storage.disk.utils.FileUtils;
 import eu.stratuslab.storage.persistence.Disk;
 import eu.stratuslab.storage.persistence.DiskView;
 
 public class DisksResource extends DiskBaseResource {
 
     private Form form = null;
 
     @Get("html")
     public Representation getAsHtml() {
 
         getLogger().info("DisksResource getAsHtml");
 
         Map<String, Object> info = listDisks();
 
         return createTemplateRepresentation("html/disks.ftl", info, TEXT_HTML);
     }
 
     @Get("json")
     public Representation getAsJson() {
 
         getLogger().info("DisksResource getAsJson");
 
         Map<String, Object> info = listDisks();
 
         return createTemplateRepresentation("json/disks.ftl", info,
                 APPLICATION_JSON);
 
     }
 
     @Post("form:html")
     public Representation createDiskRequestFromHtml(Representation entity) {
 
         Disk disk = validateAndCreateDisk();
 
         redirectSeeOther(getBaseUrl() + "/disks/" + disk.getUuid());
 
         return null;
     }
 
     protected Disk validateAndCreateDisk() {
         form = new Form(getRequestEntity());
 
         Disk disk = getDisk(form);
 
         validateNewDisk(disk);
 
         createDisk(disk);
 
         try {
             initializeContents(disk.getUuid(), form);
         } catch (ResourceException e) {
             removeDisk(disk);
             throw e;
         }
 
         return disk;
     }
 
     private void initializeContents(String uuid, Form form)
             throws ResourceException {
 
         Map<String, BigInteger> streamInfo = null;
 
         String url = form.getFirstValue(URL_KEY);
         if (url == null) {
             // If no URL, then bail out; there is nothing to do.
             getLogger().info(
                    String.format("NOT initializing contents of %s", uuid));
             return;
         }
 
         getLogger()
                 .info(String.format("initializing contents of %s from %s",
                         uuid, url));
 
         try {
             streamInfo = DiskUtils.copyUrlToVolume(uuid, url);
         } catch (IOException e) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "error initializing disk contents from " + url);
         }
 
         String bytes = form.getFirstValue(BYTES_KEY);
         if (bytes != null) {
             BigInteger expected = new BigInteger(bytes);
             BigInteger found = streamInfo.get("BYTES");
 
             getLogger().info(
                     String.format(
                             "copied bytes for %s: %s (copied), %s (expected)",
                             uuid, found, expected));
 
             if (!expected.equals(found)) {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                         String.format(
                                 "size mismatch: %s (found) != %s (expected)",
                                 found, expected));
             }
         }
 
         String sha1 = form.getFirstValue(SHA1_KEY);
         if (sha1 != null) {
             try {
                 BigInteger expected = new BigInteger(sha1, 16);
                 BigInteger found = streamInfo.get("SHA-1");
 
                 getLogger()
                         .info(String
                                 .format("sha1 checksums for %s: %s (copied), %s (expected)",
                                         uuid, found, expected));
 
                 if (!expected.equals(found)) {
                     throw new ResourceException(
                             Status.CLIENT_ERROR_BAD_REQUEST,
                             String.format(
                                     "checksum mismatch: %s (found) != %s (expected)",
                                     found, expected));
                 }
             } catch (IllegalArgumentException e) {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                         "invalid SHA-1 checksum: " + sha1);
             }
         }
 
     }
 
     @Post("form:json")
     public Representation createDiskRequestFromJson(Representation entity) {
 
         Disk disk = validateAndCreateDisk();
 
         setStatus(Status.SUCCESS_CREATED);
 
         Map<String, Object> info = new HashMap<String, Object>();
         info.put("key", Disk.UUID_KEY);
         info.put("value", disk.getUuid());
 
         return createTemplateRepresentation("json/keyvalue.ftl", info,
                 APPLICATION_JSON);
 
     }
 
     @Post("multipart")
     public void upload(Representation entity) {
 
         if (entity == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "post with null entity");
         }
 
         Disk disk = saveAndInflateFiles();
 
         DiskUtils.createAndPopulateDiskLocal(disk);
         disk.store();
 
         redirectSeeOther(getBaseUrl() + "/disks/" + disk.getUuid());
 
     }
 
     protected void createDisk(Disk disk) {
         DiskUtils.createDisk(disk);
     }
 
     protected void removeDisk(Disk disk) {
         DiskUtils.removeDisk(disk.getUuid());
     }
 
     private Disk saveAndInflateFiles() {
 
         int fileSizeLimit = ServiceConfiguration.getInstance().UPLOAD_COMPRESSED_IMAGE_MAX_BYTES;
 
         DiskFileItemFactory factory = new DiskFileItemFactory();
         factory.setRepository(FileUtils.getUploadCacheDirectory());
         factory.setSizeThreshold(fileSizeLimit);
 
         RestletFileUpload upload = new RestletFileUpload(factory);
 
         List<FileItem> items = null;
 
         try {
             items = upload.parseRequest(getRequest());
         } catch (FileUploadException e) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     e.getMessage());
         }
 
         FileItem lastFileItem = null;
         for (FileItem fi : items) {
             if (fi.getName() != null) {
                 lastFileItem = fi;
             }
         }
 
         for (FileItem fi : items) {
             if (fi != lastFileItem) {
                 fi.delete();
             }
         }
 
         if (lastFileItem != null) {
             return inflateAndProcessImage(lastFileItem);
         } else {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     "empty file uploaded");
         }
     }
 
     protected Disk inflateAndProcessImage(FileItem fi) {
 
         File cachedDiskFile = null;
 
         try {
 
             Disk disk = initializeDisk();
 
             cachedDiskFile = FileUtils.getCachedDiskFile(disk.getUuid());
 
             try {
                 long size = inflateFile(fi.getInputStream(), cachedDiskFile);
                 disk.setSize(size);
             } catch (IOException e) {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                         "no valid file uploaded");
             }
 
             try {
                 disk.setIdentifier(DiskUtils.calculateHash(cachedDiskFile));
             } catch (FileNotFoundException e) {
                 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                         e.getMessage());
             }
             validateNewDisk(disk);
             return disk;
 
         } catch (RuntimeException e) {
             if (cachedDiskFile != null) {
                 if (!cachedDiskFile.delete()) {
                     getLogger().warning(
                             "could not delete file: "
                                     + cachedDiskFile.getAbsolutePath());
                 }
             }
             throw e;
         } finally {
             fi.delete();
         }
     }
 
     private long inflateFile(InputStream gzippedContents, File inflatedFile) {
 
         GZIPInputStream in = null;
         OutputStream out = null;
 
         try {
 
             in = new GZIPInputStream(gzippedContents);
             out = new FileOutputStream(inflatedFile);
 
             byte[] buf = new byte[1024];
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
 
         } catch (IOException e) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                     e.getMessage());
         } finally {
             FileUtils.closeIgnoringError(in);
             FileUtils.closeIgnoringError(out);
         }
         return DiskUtils.convertBytesToGigaBytes(inflatedFile.length());
     }
 
     private Map<String, Object> listDisks() {
         Map<String, Object> info = createInfoStructure("Disks");
 
         addCreateFormDefaults(info);
 
         String username = getUsername(getRequest());
         List<DiskView> disks;
         if (isSuperUser(username)) {
             disks = Disk.listAll();
         } else {
             disks = Disk.listAllByUser(username);
         }
         info.put("disks", disks);
 
         return info;
     }
 
 }
