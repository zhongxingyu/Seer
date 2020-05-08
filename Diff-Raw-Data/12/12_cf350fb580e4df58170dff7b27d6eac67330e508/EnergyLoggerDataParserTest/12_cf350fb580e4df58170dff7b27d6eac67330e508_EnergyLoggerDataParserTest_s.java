 /*
  * Copyright 2012 Philip Schiffer <admin@psdev.de>
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package de.psdev.energylogger.parser;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.zip.ZipFile;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 
 public class EnergyLoggerDataParserTest {
 
     private static final Logger logger = LoggerFactory.getLogger(EnergyLoggerDataParserTest.class);
 
     private static final int EXPECTED_LOG_ENTRIES_IN_TEST_FILE = 2105;
     private static final int EXPECTED_LOG_ENTRIES_IN_ZIP_FILE = 260005;
 
     private EnergyLoggerDataParser energyLoggerDataParser;
 
     @Before
     public void setUp() throws Exception {
         energyLoggerDataParser = new EnergyLoggerDataParserImpl();
     }
 
     @Test
     public void testParseZipFile() throws IOException {
         final File file = new File(getClass().getResource("/de/psdev/energylogger/parser/data.zip").getFile());
         final ZipFile zipFile = new ZipFile(file);
         List<LogEntry> parsedLogEntriesFromFile = energyLoggerDataParser.parseZippedDataFiles(zipFile);
         zipFile.close();
         logger.info("Parsed " + parsedLogEntriesFromFile.size() + " entries from file " + file);
         assertEquals(EXPECTED_LOG_ENTRIES_IN_ZIP_FILE, parsedLogEntriesFromFile.size());
     }
 
     @Test
     public void testParseFileContents() throws IOException {
         final File file = new File(getClass().getResource("/de/psdev/energylogger/parser/a0494206.bin").getFile());
         List<LogEntry> parsedLogEntriesFromFile = energyLoggerDataParser.parseFileContents(new FileInputStream(file));
         logger.info("Parsed " + parsedLogEntriesFromFile.size() + " entries from file " + file);
         assertEquals(EXPECTED_LOG_ENTRIES_IN_TEST_FILE, parsedLogEntriesFromFile.size());
         LogEntry logEntry = parsedLogEntriesFromFile.get(0);
         assertNotNull(logEntry);
         assertEquals(226.1D, logEntry.getVoltage());
         assertEquals(0.508D, logEntry.getCurrent());
         assertEquals(0.76D, logEntry.getPowerfactor());
        assertEquals(1237253940000L, logEntry.getTimestamp().getTime());
     }
 
     @Test(expected = RuntimeException.class)
     public void testHandleWrongInput() throws Exception {
         final File file = new File(getClass().getResource("/de/psdev/energylogger/parser/badfile.bin").getFile());
         List<LogEntry> logEntries = energyLoggerDataParser.parseFileContents(new FileInputStream(file));
     }
 
     @Test
     public void testHandleZipEntry() throws Exception {
         if(energyLoggerDataParser instanceof EnergyLoggerDataParserImpl) {
             final File file = new File(getClass().getResource("/de/psdev/energylogger/parser/data.zip").getFile());
             EnergyLoggerDataParserImpl loggerDataParser = (EnergyLoggerDataParserImpl) energyLoggerDataParser;
             List<LogEntry> entries = loggerDataParser.handleZipEntry(new ZipFile(file), null);
             assertNotNull(entries);
             assertEquals(0, entries.size());
         }
     }
 
     @Test
     public void testParseDate() {
         byte[] dateByte = {0x08, 0x02, 0x07, 0x10, 0x1A};
         Date parseDate = energyLoggerDataParser.parseDate(dateByte);
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, 2007);
         calendar.set(Calendar.MONTH, Calendar.AUGUST);
         calendar.set(Calendar.DAY_OF_MONTH, 2);
         calendar.set(Calendar.HOUR_OF_DAY, 16);
         calendar.set(Calendar.MINUTE, 26);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         assertEquals(calendar.getTime(), parseDate);
     }
 
     @After
     public void tearDown() throws Exception {
         energyLoggerDataParser = null;
     }
 }
