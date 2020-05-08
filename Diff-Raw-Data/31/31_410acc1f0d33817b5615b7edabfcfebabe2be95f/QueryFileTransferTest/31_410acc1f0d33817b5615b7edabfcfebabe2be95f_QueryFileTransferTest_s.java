 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.protocol;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.List;
 import java.util.zip.CRC32;
 import java.util.zip.Checksum;
 
 import junit.framework.Assert;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.syphr.mythtv.data.FileInfo;
 import org.syphr.mythtv.data.Program;
 import org.syphr.mythtv.protocol.test.Utils;
 import org.syphr.mythtv.test.Settings;
 import org.syphr.mythtv.types.FileTransferType;
 import org.syphr.mythtv.types.RecordingCategory;
 import org.syphr.mythtv.types.SeekOrigin;
 import org.syphr.mythtv.util.exception.CommandException;
 import org.syphr.mythtv.util.socket.SocketManager;
 import org.syphr.prom.PropertiesManager;
 
 import com.google.common.io.Files;
 import com.google.common.io.Resources;
 
 public class QueryFileTransferTest
 {
     private static final Logger LOGGER = LoggerFactory.getLogger(QueryFileTransferTest.class);
 
     private static final File LOCAL_TEMP = new File("target/testing");
 
     private static final String TEST_URL = "http://www.syphr.org";
     private static final String TEST_URI = "/download-test.html";
     private static final String TEST_STORAGE_GROUP = "Default";
 
     private static final File EXPECTED_FILE = new File(LOCAL_TEMP, "transfer-test.data");
 
     private static PropertiesManager<Settings> settings;
 
     private static SocketManager commandSocketManager;
     private static Protocol commandProto;
 
     @BeforeClass
     public static void setUpBeforeClass() throws IOException, CommandException
     {
         if (!LOCAL_TEMP.exists() && !LOCAL_TEMP.mkdirs())
         {
             throw new IOException("Cannot create parent directory at: "
                                   + LOCAL_TEMP.getAbsolutePath());
         }
 
         /*
          * Download a file directly for comparison later.
          */
         OutputStream directDownloadOut = new BufferedOutputStream(new FileOutputStream(EXPECTED_FILE));
         Resources.copy(new URL(TEST_URL), directDownloadOut);
         directDownloadOut.close();
 
         settings = Settings.createSettings();
 
         commandSocketManager = Utils.connect(settings);
         commandProto = Utils.announceMonitor(settings,
                                              commandSocketManager,
                                              EventLevel.NONE);
     }
 
     @AfterClass
     public static void tearDownAfterClass() throws IOException
     {
         commandProto.done();
         commandSocketManager.disconnect();
 
         Files.deleteRecursively(LOCAL_TEMP);
     }
 
     @Test
     public void testDownloadReadDeleteFile() throws IOException, URISyntaxException, CommandException
     {
         URI dest = new URI(TEST_URI);
 
         URI result = commandProto.downloadFileNow(new URL(TEST_URL), TEST_STORAGE_GROUP, dest);
         Assert.assertNotNull(result);
         Assert.assertEquals(dest.getPath().replace("/", ""),
                             result.getPath().replace("/", ""));
         Assert.assertEquals(TEST_STORAGE_GROUP, result.getUserInfo());
 
         Protocol fileProto = commandProto.newProtocol();
         fileProto.mythProtoVersion();
 
         QueryFileTransfer fileTransfer = fileProto.annFileTransfer(InetAddress.getLocalHost()
                                                                               .getHostName(),
                                                                    FileTransferType.READ,
                                                                    false,
                                                                    0L,
                                                                    dest,
                                                                    TEST_STORAGE_GROUP,
                                                                    commandProto);
 
         File actualFile = new File(LOCAL_TEMP, "actual.data");
         Utils.readToFile(settings, fileProto, actualFile, fileTransfer);
 
         fileTransfer.done();
         fileProto.done();
 
         Checksum crc32 = new CRC32();
         Assert.assertEquals(Files.getChecksum(EXPECTED_FILE, crc32),
                             Files.getChecksum(actualFile, crc32));
 
         Assert.assertNotNull(commandProto.queryFileExists(dest, TEST_STORAGE_GROUP));
         Assert.assertTrue(commandProto.deleteFile(dest, TEST_STORAGE_GROUP));
         Assert.assertNull(commandProto.queryFileExists(dest, TEST_STORAGE_GROUP));
     }
 
     @Test
     public void testReadSeekRecording() throws IOException, CommandException
     {
         List<Program> programs = commandProto.queryRecordings(RecordingCategory.RECORDED_UNSORTED);
 
        Protocol fileProto = commandProto.newProtocol();
        fileProto.mythProtoVersion();

        Program targetProgram = null;
         QueryFileTransfer fileTransfer = null;
         for (Program program : programs)
         {
             fileTransfer = fileProto.annFileTransfer(InetAddress.getLocalHost()
                                                                 .getHostName(),
                                                      FileTransferType.READ,
                                                      false,
                                                      0L,
                                                      program.getBasename(),
                                                      program.getStorageGroup(),
                                                      commandProto);
 
             if (fileTransfer.getSize() > 0)
             {
                 targetProgram = program;
                 break;
             }
             else
             {
                 fileTransfer.done();
             }
         }
 
         if (targetProgram == null)
         {
            fileProto.done();

             LOGGER.warn("Skipping read/seek test because a recording with a non-zero length could not be found");
             return;
         }
 
         Assert.assertTrue(fileTransfer.isOpen());
 
         fileTransfer.setTimeout(false);
         fileTransfer.setTimeout(true);
 
         long buffer = settings.getLongProperty(Settings.BUFFER_SIZE);
         long totalSize = fileTransfer.getSize();
         long chunk = buffer * 100;
         long seekPosition = totalSize - chunk;
 
         if (chunk >= totalSize)
         {
             Assert.fail("Unable to test read/seek - file too small ("
                         + totalSize
                         + "B)");
         }
 
         File tempFile = new File(LOCAL_TEMP, targetProgram.getBasename().toString());
 
         Utils.readToFile(settings,
                          fileProto,
                          tempFile,
                          fileTransfer,
                          chunk,
                          false);
 
         long newPosition = fileTransfer.seek(seekPosition, SeekOrigin.BEGINNING, chunk);
         Assert.assertEquals(seekPosition, newPosition);
 
         Utils.readToFile(settings,
                          fileProto,
                          tempFile,
                          fileTransfer,
                          chunk,
                          true);
 
         fileTransfer.done();
         fileProto.done();
     }
 
     @Test
     public void testWriteDelete() throws IOException, URISyntaxException, CommandException
     {
         URI dest = new URI(TEST_URI);
 
         Protocol fileProto = commandProto.newProtocol();
         fileProto.mythProtoVersion();
 
         QueryFileTransfer fileTransfer = fileProto.annFileTransfer(InetAddress.getLocalHost()
                                                                               .getHostName(),
                                                                    FileTransferType.WRITE,
                                                                    false,
                                                                    0L,
                                                                    dest,
                                                                    TEST_STORAGE_GROUP,
                                                                    commandProto);
 
         Utils.writeFromFile(settings, fileProto, EXPECTED_FILE, fileTransfer);
 
         fileTransfer.done();
         fileProto.done();
 
         FileInfo info = commandProto.queryFileExists(dest, TEST_STORAGE_GROUP);
         Assert.assertNotNull(info);
         Assert.assertEquals(EXPECTED_FILE.length(), info.getSize());
         Assert.assertTrue(commandProto.deleteFile(dest, TEST_STORAGE_GROUP));
         Assert.assertNull(commandProto.queryFileExists(dest, TEST_STORAGE_GROUP));
 
     }
 }
