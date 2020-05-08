 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.tresys.jalop.utils.jnltest;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import com.google.common.io.PatternFilenameFilter;
 import com.tresys.jalop.jnl.DigestStatus;
 import com.tresys.jalop.jnl.RecordInfo;
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.SubscribeRequest;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.SubscriberSession;
 
 /**
  * Sample implementation of a {@link Subscriber}. This {@link Subscriber} simply
  * writes records to disk using a very simple hierarchy. Each record gets it's
  * own directory.
  * Each record is given a unique serial ID when it is transferred, and this
  * ID is used as the directory name. In addition to the actual records, this
  * {@link Subscriber} records a small file that provides additional status
  * information for each record.
  */
 public class SubscriberImpl implements Subscriber {
 
     /**
      * Key in the status file for the digest status (confirmed, invalid,
      * unknown).
      */
     private static final String DGST_CONF = "digest_conf";
 
     /** Filename where status information is written to. */
     private static final String STATUS_FILENAME = "status.js";
 
     /**
      * Key in the status file for the expected size of the application
      * meta-data.
      * */
     private static final String APP_META_SZ = "app_meta_sz";
 
     /** Key in the status file for the expected size of the system meta-data. */
     private static final String SYS_META_SZ = "sys_meta_sz";
 
     /** Key in the status file for the expected size of the payload. */
     private static final String PAYLOAD_SZ = "payload_sz";
 
     /**
      * Key in the status file for tracking how many bytes of the system
      * meta-data was actually transfered.
      */
     private static final String SYS_META_PROGRESS = "sys_meta_progress";
 
     /**
      * Key in the status file for tracking how many bytes of the application
      * meta-data was actually transfered.
      */
     private static final String APP_META_PROGRESS = "app_meta_progress";
 
     /**
      * Key in the status file for tracking how many bytes of the payload was
      * actually transfered.
      */
     private static final String PAYLOAD_PROGRESS = "payload_progress";
 
     /**
      * Key in the status file for the serial ID the remote uses to identify
      * this record.
      */
     private static final String REMOTE_SID = "remote_sid";
 
     /** Key in the status file for the calculated digest. */
     private static final Object DGST = "digest";
 
     /** The filename for the system meta-data document. */
     private static final String SYS_META_FILENAME = "sys_metadata.xml";
 
     /** The filename for the application meta-data document. */
     private static final String APP_META_FILENAME = "app_metadata.xml";
 
     /** The filename for the payload. */
     private static final String PAYLOAD_FILENAME = "payload";
 
     /** Indicates that both sides agree on the digest value. */
     private static final Object CONFIRMED = "confirmed";
 
     /**
      * Indicates the remote can't find a digest value for the specified serial
      * ID. */
     private static final Object UNKNOWN = "unknown";
 
     /** Indicates that both sides disagree on the digest value. */
     private static final Object INVALID = "invalid";
 
     /** Key in the status file to indicate if a 'sync' message was sent. */
     private static final String SYNCED = "synced";
 
     /**
      * Root of the output directories. Each record gets it's own
      * sub-directory.
      */
     final File outputRoot;
 
     /** A logger for this class. */
     private static final Logger LOGGER = Logger.getLogger(SubscriberImpl.class);
 
     /** The format string for output files. */
     private static final String SID_FORMAT_STRING = "0000000000";
 
     /**
      * Regular expression used for filtering directories, i.e. only directories
      * which have exactly ten digits as a filename.
      */
     private static final String SID_REGEX = "^\\d{10}$";
 
     /**
      * Filter used for searching an existing file system tree for previously
      * downloaded records.
      */
     static final FilenameFilter FILENAME_FILTER =
         new PatternFilenameFilter(SID_REGEX);
 
     /** Formatter used to generate the sub-directories for each record. */
     static final DecimalFormat SID_FORMATER =
         new DecimalFormat(SID_FORMAT_STRING);
 
     /** Local serial ID counter. */
    private long sid = 1;
 
     /** Maps remote SID to {@link LocalRecordInfo}. */
     private final Map<String, LocalRecordInfo> sidMap =
         new HashMap<String, SubscriberImpl.LocalRecordInfo>();
 
     /** Buffer size for read data from the network and writing to disk. */
     private int bufferSize = 4096;
 
     /** The type of records to transfer. */
     private final RecordType recordType;
 
     /** The serial ID to send in a subscribe message. */
     String lastSerialFromRemote = null;
 
     /** The offset to send in a journal subscribe message. */
     long journalOffset = -1;
 
     /** The input stream to use for a journal resume. */
     InputStream journalInputStream = null;
 
     /**
      * FileFilter to get all sub-directories that match the serial ID
      * pattern.
      */
     private static final FileFilter FILE_FILTER = new FileFilter() {
         @Override
         public boolean accept(final File pathname) {
             if (pathname.isDirectory()) {
                 return FILENAME_FILTER.accept(pathname.getParentFile(),
                                               pathname.getName());
             }
             return false;
         }
     };
 
     /**
      * This is just an object used to track stats about a specific record.
      */
     private class LocalRecordInfo {
         /** The directory to store all information regarding this record. */
         public final File recordDir;
         /** The file to write the status information to. */
         public final File statusFile;
         /** Cached copy of the JSON stats. */
         public final JSONObject status;
 
         /**
          * Create a new {@link LocalRecordInfo} object.
          *
          * @param info
          *            The record info obtained from the remote.
          * @param localSid
          *            The SID to assign this record to locally.
          */
         public LocalRecordInfo(final RecordInfo info, final long localSid) {
             this(info.getSerialId(), info.getAppMetaLength(),
                  info.getSysMetaLength(), info.getPayloadLength(), localSid);
         }
 
         /**
          * Create a new LocalRecordInfo.
          * @param remoteSid
          *          The serial ID of the record as the remote identifies it.
          * @param appMetaLen
          *          The length, in bytes, of the application meta-data.
          * @param sysMetaLen
          *          The length, in bytes, of the system meta-data.
          * @param payloadLen
          *          The length, in bytes, of the payload.
          * @param localSid
          *          The serial ID as it is tracked internally.
          */
         // suppress warnings about raw types for the JSON map
         @SuppressWarnings("unchecked")
         public LocalRecordInfo(final String remoteSid, final long appMetaLen,
                 final long sysMetaLen, final long payloadLen,
                 final long localSid) {
             this.recordDir =
                 new File(SubscriberImpl.this.outputRoot,
                          SubscriberImpl.SID_FORMATER.format(localSid));
             this.statusFile = new File(this.recordDir, STATUS_FILENAME);
             this.status = new JSONObject();
             this.status.put(APP_META_SZ, appMetaLen);
             this.status.put(SYS_META_SZ, sysMetaLen);
             this.status.put(PAYLOAD_SZ, payloadLen);
             this.status.put(REMOTE_SID, remoteSid);
         }
     }
 
     /**
      * Create a {@link SubscriberImpl} object. Instances of this class will
      * create sub-directories under <code>outputRoot</code> for each downloaded
      * record.
      *
      * @param recordType
      *          The type of record that will be transfered using this instance.
      * @param outputRoot
      *          The output directory that records will be written to.
      * @param remoteAddr
      *          The {@link InetAddress} of the remote.
      */
     public SubscriberImpl(final RecordType recordType, final File outputRoot,
             final InetAddress remoteAddr) {
         this.recordType = recordType;
         File tmp = new File(outputRoot, remoteAddr.getHostAddress());
         final String type;
         switch (recordType) {
         case Audit:
             type = "audit";
             break;
         case Journal:
             type = "journal";
             break;
         case Log:
             type = "log";
             break;
         default:
             throw new IllegalArgumentException("illegal record type");
         }
         this.outputRoot = new File(tmp, type);
         this.outputRoot.mkdirs();
         if (!(this.outputRoot.exists() && this.outputRoot.isDirectory())) {
             throw new RuntimeException("Failed to create subdirs for "
                                        + remoteAddr.getHostAddress() + "/"
                                        + type);
         }
         try {
             prepareForSubscribe();
         } catch (Exception e) {
             LOGGER.fatal("Failed to clean existing directories: ");
             LOGGER.fatal(e);
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Helper utility to run through all records that have been transferred,
      * but not yet synced. For journal & audit records, this will remove all
      * records that are not synced (even if they are completely downloaded).
      * For journal records, this finds the record after the most recently
      * synced record record, and deletes all the other unsynced records. For
      * example, if the journal records 1, 2, 3, and 4 have been downloaded, and
      * record number 2 is marked as 'synced', then this will remove record
      * number 4, and try to resume the transfer for record number 3.
      *
      * @throws IOException If there is an error reading existing files, or an
      *          error removing stale directories.
      * @throws org.json.simple.parser.ParseException
      * @throws ParseException If there is an error parsing status files.
      * @throws java.text.ParseException If there is an error parsing a
      *          directory name.
      */
     final void prepareForSubscribe() throws IOException, ParseException,
                                                 java.text.ParseException {
         this.lastSerialFromRemote = SubscribeRequest.EPOC;
         this.journalOffset = 0;
         JSONParser p  = new JSONParser();
         File[] recordDirs =
             this.outputRoot.listFiles(SubscriberImpl.FILE_FILTER);
 
         Arrays.sort(recordDirs);
         int idx = recordDirs.length - 1;
         File lastDir = null;
         Set<File> deleteDirs = new HashSet<File>();
         long lastPayloadBytesTransferred = 0;
         JSONObject lastStatus = null;
         while (idx >= 0) {
             JSONObject status;
             try {
                 status = (JSONObject) p.parse(new FileReader(
                                                    new File(recordDirs[idx],
                                                              STATUS_FILENAME)));
             } catch (FileNotFoundException e) {
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Deleting " + recordDirs[idx] + ", because it is missing the '" + STATUS_FILENAME + "' file");
                 }
                 deleteDirs.add(recordDirs[idx]);
                 idx--;
                 continue;
             } catch (ParseException e ) {
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Deleting " + recordDirs[idx] + ", because failed to parse '" + STATUS_FILENAME + "' file");
                 }
                 deleteDirs.add(recordDirs[idx]);
                 idx--;
                 continue;
             }
             String dgstStatus = (String) status.get(DGST_CONF);
             if (CONFIRMED.equals(dgstStatus)) {
                 // found a successfully sent record.
                 if ((this.recordType == RecordType.Journal)
                         && (lastDir != null)
                         && (lastPayloadBytesTransferred > 0)) {
                     this.lastSerialFromRemote =
                         (String) lastStatus.get(REMOTE_SID);
                     this.journalOffset =
                         ((Number) lastStatus.get(PAYLOAD_PROGRESS)).longValue();
                     FileUtils.forceDelete(new File(lastDir, APP_META_FILENAME));
                     FileUtils.forceDelete(new File(lastDir, SYS_META_FILENAME));
                     status.remove(APP_META_PROGRESS);
                     status.remove(SYS_META_PROGRESS);
                     this.sid =
                         SID_FORMATER.parse(lastDir.getName()).longValue();
                     this.journalInputStream = new FileInputStream(
                                        new File(lastDir, PAYLOAD_FILENAME));
                 } else {
                    // all records synced (or for journal, either no un-synced
                    // or no bytes downloaded.
                    this.lastSerialFromRemote = (String) status.get(REMOTE_SID);
                    if (lastDir != null) {
                        deleteDirs.add(lastDir);
                    }
                 }
                 break;
             }
             if (lastDir != null) {
                 deleteDirs.add(lastDir);
             }
             lastDir = recordDirs[idx];
             lastStatus = status;
             Number progress = (Number) lastStatus.get(PAYLOAD_PROGRESS);
             if (progress != null) {
                 lastPayloadBytesTransferred = progress.longValue();
             } else {
                 lastPayloadBytesTransferred = 0;
             }
             idx--;
         }
         for (File f: deleteDirs) {
             if (LOGGER.isInfoEnabled()) {
                 LOGGER.info("Removing directory for unsynced record: "
                             + f.getAbsolutePath());
             }
             FileUtils.forceDelete(f);
         }
     }
 
     @Override
     public final SubscribeRequest
                     getSubscribeRequest(final SubscriberSession sess) {
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Returning subscriber request for: " + sess.getRole()
                         + sess.getRecordType());
             LOGGER.info("serialID: " + this.lastSerialFromRemote);
         }
         return new SubscribeRequest() {
             @Override
             public String getSerialId() {
                 return SubscriberImpl.this.lastSerialFromRemote;
             }
 
             @Override
             public long getResumeOffset() {
                 return SubscriberImpl.this.journalOffset;
             }
 
             @Override
             public InputStream getResumeInputStream() {
                 return SubscriberImpl.this.journalInputStream;
             }
         };
     }
 
     @Override
     public final boolean notifySysMetadata(final SubscriberSession sess,
                                            final RecordInfo recordInfo,
                                            final InputStream sysMetaData) {
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Got sysmetadata for " + recordInfo.getSerialId());
         }
         LocalRecordInfo lri;
         synchronized (this.sidMap) {
             if (this.sidMap.containsKey(recordInfo.getSerialId())) {
                 LOGGER.error("Already contain a record for "
                              + recordInfo.getSerialId());
                 return false;
             }
             lri = new LocalRecordInfo(recordInfo, this.sid);
             this.sid += 1;
             this.sidMap.put(recordInfo.getSerialId(), lri);
         }
         lri.statusFile.getParentFile().mkdirs();
         if (!dumpStatus(lri)) {
             return false;
         }
         return handleRecordData(lri, recordInfo.getSysMetaLength(),
                                 SYS_META_FILENAME, SYS_META_PROGRESS,
                                 sysMetaData);
     }
 
     /**
      * Write status information about a record out to disk.
      * @param lri The {@link LocalRecordInfo} object output stats for.
      * @return <code>true</code> If the data was successfully written out.
      *         <code>false</code> otherwise.
      */
     final boolean dumpStatus(final LocalRecordInfo lri) {
         BufferedOutputStream w;
         try {
             w = new BufferedOutputStream(new FileOutputStream(lri.statusFile));
             w.write(lri.status.toJSONString().getBytes("utf-8"));
             w.close();
         } catch (FileNotFoundException e) {
             LOGGER.error("Failed to open status file for writing:"
                               + e.getMessage());
             return false;
         } catch (UnsupportedEncodingException e) {
             SubscriberImpl.LOGGER.error("cannot find UTF-8 encoder?");
             return false;
         } catch (IOException e) {
             LOGGER.error("failed to write to the status file, aborting");
             return false;
         }
         return true;
     }
 
     /**
      * Helper utility to write out different sections of the record data.
      *
      * @param lri
      *            The {@link LocalRecordInfo} for this record.
      * @param dataSize
      *            The size of the data, in bytes.
      * @param outputFilename
      *            The filename to use for the data section.
      * @param incomingData
      *            The {@link InputStream} to write to disk.
      * @param statusKey
      *            Key to use in the status file for recording the total number
      *            of bytes written.
      * @return <code>true</code> if the data was successfully written to disk,
      *         <code>false</code> otherwise.
      */
     // suppress warnings about raw types for the JSON map
     @SuppressWarnings("unchecked")
     final boolean handleRecordData(final LocalRecordInfo lri,
                                    final long dataSize,
                                    final String outputFilename,
                                    final String statusKey,
             final InputStream incomingData) {
         byte[] buffer = new byte[this.bufferSize];
         BufferedOutputStream w;
         final File outputFile = new File(lri.recordDir, outputFilename);
         long total = 0;
         boolean ret = true;
         try {
             w = new BufferedOutputStream(new FileOutputStream(outputFile));
             int cnt = incomingData.read(buffer);
             while (cnt != -1) {
                 w.write(buffer, 0, cnt);
                 total += cnt;
                 cnt = incomingData.read(buffer);
             }
             w.close();
         } catch (FileNotFoundException e) {
             LOGGER.error("Failed to open '" + outputFile.getAbsolutePath()
                               + "' for writing");
             return false;
         } catch (IOException e) {
             LOGGER.error("Error while trying to write to '"
                          + outputFile.getAbsolutePath() + "' for writing: "
                          + e.getMessage());
             return false;
         } finally {
             lri.status.put(statusKey, total);
             ret = dumpStatus(lri);
         }
         if (total != dataSize) {
             LOGGER.error("System metadata reported to be: " + dataSize
                          + ", received " + total);
             ret = false;
         }
 
         return ret;
     }
 
     @Override
     public final boolean notifyAppMetadata(final SubscriberSession sess,
                     final RecordInfo recordInfo,
                     final InputStream appMetaData) {
         if (recordInfo.getAppMetaLength() != 0) {
             LocalRecordInfo lri;
             synchronized (this.sidMap) {
                 lri = this.sidMap.get(recordInfo.getSerialId());
             }
             if (lri == null) {
                 LOGGER.error("Can't find local status for: "
                              + recordInfo.getSerialId());
                 return false;
             }
 
             return handleRecordData(lri, recordInfo.getAppMetaLength(),
                                     APP_META_FILENAME, APP_META_PROGRESS,
                                     appMetaData);
         }
 
         return true;
     }
 
     @Override
     public final boolean notifyPayload(final SubscriberSession sess,
                                        final RecordInfo recordInfo,
                                        final InputStream payload) {
         if (recordInfo.getPayloadLength() != 0) {
             LocalRecordInfo lri;
             synchronized (this.sidMap) {
                 lri = this.sidMap.get(recordInfo.getSerialId());
             }
             if (lri == null) {
                 LOGGER.error("Can't find local status for: "
                                   + recordInfo.getSerialId());
                 return false;
             }
 
             return handleRecordData(lri, recordInfo.getPayloadLength(),
                                     PAYLOAD_FILENAME, PAYLOAD_PROGRESS,
                                     payload);
         }
         return true;
     }
 
     // suppress warnings about raw types for the JSON map
     @SuppressWarnings("unchecked")
     @Override
     public final boolean notifyDigest(final SubscriberSession sess,
                                       final RecordInfo recordInfo,
                                       final byte[] digest) {
 		String hexString = (new BigInteger(1, digest)).toString(16);
         if (LOGGER.isInfoEnabled()) {
             LOGGER.info("Calculated digest for " + recordInfo.getSerialId()
                         + ": " + hexString);
         }
         LocalRecordInfo lri;
         synchronized (this.sidMap) {
             lri = this.sidMap.get(recordInfo.getSerialId());
         }
         if (lri == null) {
             LOGGER.error("Can't find local status for: "
                          + recordInfo.getSerialId());
             return false;
         }
 
         lri.status.put(DGST, hexString);
         dumpStatus(lri);
         return true;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public final boolean notifyDigestResponse(final SubscriberSession sess,
                                     final Map<String, DigestStatus> statuses) {
         boolean ret = true;
         LocalRecordInfo lri;
         for (Entry<String, DigestStatus> entry : statuses.entrySet()) {
             synchronized (this.sidMap) {
                 lri = this.sidMap.remove(entry.getKey());
             }
             if (lri == null) {
                 LOGGER.error("Can't find local status for: " + entry.getKey());
                 ret = false;
             } else {
                 switch (entry.getValue()) {
                 case Confirmed:
                     lri.status.put(DGST_CONF, CONFIRMED);
                     break;
                 case Unknown:
                     lri.status.put(DGST_CONF, UNKNOWN);
                     break;
                 case Invalid:
                     lri.status.put(DGST_CONF, INVALID);
                     break;
                 default:
                     return false;
                 }
                 if (!dumpStatus(lri)) {
                     ret = false;
                 }
             }
         }
         return ret;
     }
 }
