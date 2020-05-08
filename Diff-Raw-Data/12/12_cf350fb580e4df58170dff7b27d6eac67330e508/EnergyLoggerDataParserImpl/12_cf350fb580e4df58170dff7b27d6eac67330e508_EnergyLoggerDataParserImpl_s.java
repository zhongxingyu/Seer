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
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.util.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class EnergyLoggerDataParserImpl implements EnergyLoggerDataParser {
 
     private static final Logger logger = LoggerFactory.getLogger(EnergyLoggerDataParserImpl.class);
 
     private static final byte[] START_CODE = {(byte) 0xE0, (byte) 0xC5, (byte) 0xEA};
     private static final byte[] INFO_FILE_START_CODE = {(byte) 0x49, (byte) 0x4E, (byte) 0x46, (byte) 0x4F,
             (byte) 0x3A};
     private static final byte[] EOF_CODE = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
     private static final int DATA_BYTES_LENGTH = 5;
     private static final int START_DATE_BYTES_LENGTH = 5;
 
     public enum FileType {
         INFO, DATA, UNKNOWN
     }
 
     @Override
     public List<LogEntry> parseZippedDataFiles(ZipFile zipFile) {
         long startTime = System.currentTimeMillis();
         final List<LogEntry> logEntries = new ArrayList<LogEntry>();
         final Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
         while (zipEntries.hasMoreElements()) {
             ZipEntry zipEntry = zipEntries.nextElement();
             logEntries.addAll(handleZipEntry(zipFile, zipEntry));
         }
         long runtime = System.currentTimeMillis() - startTime;
         logger.info("Parsed " + logEntries.size() + " logentries in " + runtime + "ms");
         return logEntries;
     }
 
     List<LogEntry> handleZipEntry(ZipFile zipFile, ZipEntry zipEntry) {
         try {
             return parseFileContents(zipFile.getInputStream(zipEntry));
         } catch (Exception e) {
             logger.error("Error parsing zipEntry " + ((zipEntry != null) ? zipEntry.getName() : zipEntry), e);
         }
         return Collections.emptyList();
     }
 
     @Override
     public List<LogEntry> parseFileContents(InputStream input) throws IOException {
         if (!(input instanceof BufferedInputStream)) {
             input = new BufferedInputStream(input);
         }
         final List<LogEntry> logEntries = new ArrayList<LogEntry>();
 
         byte[] data = IOUtils.toByteArray(input);
         int counter = 0;
         final Calendar currentDate = GregorianCalendar.getInstance();
         FileType fileType = FileType.UNKNOWN;
 
         whileLoop:
         while (counter < data.length) {
             switch (fileType) {
                 case DATA: // DATA File
                     if (Arrays.equals(Arrays.copyOfRange(data, counter, counter + START_CODE.length), START_CODE)) {
                         counter += START_CODE.length;
                         currentDate.setTime(parseDate(Arrays.copyOfRange(data, counter, counter
                                 + START_DATE_BYTES_LENGTH)));
                         counter += START_DATE_BYTES_LENGTH;
                     } else if (Arrays.equals(Arrays.copyOfRange(data, counter, counter + EOF_CODE.length), EOF_CODE)) {
                         break whileLoop;
                     } else {
                         byte[] dataBytes = Arrays.copyOfRange(data, counter, counter + DATA_BYTES_LENGTH);
                         double avgVoltage = parseDouble(dataBytes[0], dataBytes[1]) / 10;
                         double avgCurrent = parseDouble(dataBytes[2], dataBytes[3]) / 1000;
                         double avgPowerFactor = parseDouble(dataBytes[4]) / 100;
                         LogEntry logEntry = new LogEntryImpl();
                         logEntry.setVoltage(avgVoltage);
                         logEntry.setCurrent(avgCurrent);
                         logEntry.setPowerfactor(avgPowerFactor);
                         logEntry.setTimestamp(currentDate.getTime());
                         logEntries.add(logEntry);
                         currentDate.add(Calendar.MINUTE, 1);
                         counter += DATA_BYTES_LENGTH;
                     }
                     break;
                 case INFO: // Info File
                     logger.warn("got INFO file - ignoring because of invalid data");
                     break whileLoop;
                 case UNKNOWN: // Detect File Type
                     if (Arrays.equals(Arrays.copyOfRange(data, counter, counter + INFO_FILE_START_CODE.length),
                             INFO_FILE_START_CODE)) { // INFO FILE
                         fileType = FileType.INFO;
                     } else if (Arrays.equals(Arrays.copyOfRange(data, counter, counter + START_CODE.length), START_CODE)) {
                         fileType = FileType.DATA;
                     } else {
                         throw new RuntimeException("Unknown Filetype");
                     }
                     break;
             }
         }
         return logEntries;
     }
 
     private double parseDouble(byte... bytes) {
         return new BigInteger(bytes).doubleValue();
     }
 
     @Override
     public Date parseDate(byte[] dateBytes) {
         byte month = dateBytes[0];
         byte day = dateBytes[1];
         byte year = dateBytes[2];
         byte hour = dateBytes[3];
         byte minute = dateBytes[4];
         final Calendar calendar = GregorianCalendar.getInstance();
         calendar.set(Calendar.DAY_OF_MONTH, day);
         calendar.set(Calendar.MONTH, month - 1);
         calendar.set(Calendar.YEAR, 2000 + year);
         calendar.set(Calendar.HOUR_OF_DAY, hour);
         calendar.set(Calendar.MINUTE, minute);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         return calendar.getTime();
     }
 
 }
