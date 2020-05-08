 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.gpeh.parser.internal;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.UnexpectedException;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 
 import org.amanzi.awe.gpeh.parser.Events;
 import org.amanzi.awe.gpeh.parser.GPEHFileNameWrapper;
 import org.amanzi.awe.gpeh.parser.Parameters;
 import org.amanzi.neo.services.utils.Pair;
 import org.kc7bfi.jflac.io.BitInputStream;
 
 /**
  * <p>
  * Parser of GPEH data
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class GPEHParser {
 //    private static final Logger LOGGER = Logger.getLogger(GPEHParser.class);
     
     private static final int FAIL_MASK_INT= ~(0x1 << 32);
     
     private static final long FAIL_MASK_LONG = ~(0x1 << 64);
 
     public static GPEHMainFile parseMainFile(File mainFile) throws IOException {
         GPEHMainFile result = new GPEHMainFile(mainFile);
         InputStream in = new FileInputStream(mainFile);
         if (Pattern.matches("^.+\\.gz$", mainFile.getName())) {
             in = new GZIPInputStream(in);
         }
         BitInputStream input = new BitInputStream(in);
         try {
             while (true) {
                 parseData(input, result);
             }
 
         } catch (EOFException e) {
             // normal behavior
         } finally {
             in.close();
         }
         for (GPEHMainFile.Record record : result.records) {
             for (Pair<String, String> pair : record.filters) {
 //                LOGGER.debug(pair.left() + "\t" + pair.right());
             }
         }
         return result;
     }
 
     /**
      * @param input
      * @param result
      * @throws IOException
      */
     private static void parseData(BitInputStream input, GPEHMainFile result) throws IOException {
         int recordLen = input.readRawUInt(16);
         int recordType = input.readRawUInt(8);
         if (recordType == 0) {
             parseMainHeader(input, result, recordLen - 3);
         } else if (recordType == 1) {
             parseRecord(input, result, recordLen);
         } else if (recordType == 2) {
             parseProtocol(input, result);
         } else if (recordType == 8) {
             parseLink(input, result);
         } else if (recordType == 7) {
             pareseFooter(input, result);
         } else if (recordType == 6) {
             pareseError(input, result);
         } else {
             // wrong file format!
             throw new IllegalArgumentException("Wrong blok!=" + recordType);
         }
 
     }
 
     /**
      * @param input
      * @param result
      * @throws IOException
      */
     public static void pareseError(BitInputStream input, IGPEHBlock result) throws IOException {
         GPEHError error = new GPEHError();
         result.setEndRecord(error);
         error.hour = input.readRawUInt(8);
         error.minute = input.readRawUInt(8);
         error.second = input.readRawUInt(8);
         error.millisecond = input.readRawUInt(16);
         error.errorType = input.readRawUInt(8);
     }
 
     /**
      * @param input
      * @param result
      * @throws IOException
      */
     public static void pareseFooter(BitInputStream input, IGPEHBlock result) throws IOException {
         GPEHFooter footer = new GPEHFooter();
         result.setEndRecord(footer);
         footer.year = input.readRawUInt(16);
         footer.month = input.readRawUInt(8);
         footer.day = input.readRawUInt(8);
         footer.hour = input.readRawUInt(8);
         footer.minute = input.readRawUInt(8);
         footer.second = input.readRawUInt(8);
     }
 
     /**
      * @param input
      * @param result
      * @throws IOException
      */
     private static void parseLink(BitInputStream input, GPEHMainFile result) throws IOException {
         GPEHMainFile.Link link = new GPEHMainFile.Link();
         result.addLink(link);
         link.filePath = readAllString(input, 256);
 //        LOGGER.debug(link.filePath);
     }
 
     /**
      * @param input
      * @param result
      * @param recordLen
      * @throws IOException
      */
     private static void parseProtocol(BitInputStream input, GPEHMainFile result) throws IOException {
         GPEHMainFile.Protocol protocol = new GPEHMainFile.Protocol();
         result.addProtocol(protocol);
         protocol.id = input.readRawUInt(8);
         protocol.name = readAllString(input, 50);
         protocol.objectIdentifier = readAllString(input, 30);
     }
 
     /**
      * @param input
      * @param result
      * @param recordLen
      * @throws IOException
      */
     private static void parseRecord(BitInputStream input, GPEHMainFile result, int recordLen) throws IOException {
         GPEHMainFile.Record record = new GPEHMainFile.Record();
         result.addRecord(record);
         record.filterType = input.readRawUInt(8);
         if (recordLen > 4) {
             byte[] buffer = new byte[recordLen - 4];
             for (int i = 0; i < buffer.length; i++) {
                 buffer[i] = (byte)input.readRawUInt(8);
             }
             record.saveFilters(buffer);
         }
     }
 
     /**
      * @param input
      * @param result
      * @param recordLen
      * @throws IOException
      */
     private static void parseMainHeader(BitInputStream input, GPEHMainFile result, int recordLen) throws IOException {
         int len = 0;
         result.header.fileVer = readAllString(input, 5);
         len += 5;
         result.header.year = input.readRawUInt(16);
         len += 2;
         result.header.month = input.readRawUInt(8);
         len += 1;
         result.header.day = input.readRawUInt(8);
         len += 1;
         result.header.hour = input.readRawUInt(8);
         len += 1;
         result.header.minute = input.readRawUInt(8);
         len += 1;
         result.header.second = input.readRawUInt(8);
         len += 1;
         result.header.neUserLabel = readAllString(input, 200);
         len += 200;
         result.header.neLogicalName = readAllString(input, 200);
         len += 200;
         if (len < recordLen) {
             System.err.append(String.format("Header have %d unparsed byts", recordLen - len));
             readAllString(input, recordLen - len);
         }
     }
 
     public static String readStringToDelim(InputStream input, int delim) throws IOException {
         StringBuilder result = new StringBuilder();
         int rb;
         while ((rb = input.read()) != -1) {
             if (rb == delim) {
                 break;
             }
             result.append((char)rb);
         }
         return result.toString();
     }
 
     public static void main(String[] args) throws IOException {
         // parseMainFile(new
         // File("d://AWE/GPEH/Avea_Niste01_30min_GPEH_trace/A20090706.1700+0300-1715+0300_SubNetwork=NISTE01,MeContext=NISTE01_rnc_gpehfile_Mp0.bin.gz"));
 
         // parseEventFile(new
         // File("d://AWE/GPEH/Avea_Niste01_30min_GPEH_trace/A20090706.1700+0300-1715+0300_SubNetwork=NISTE01,MeContext=NISTE01_rnc_gpehfile_Mp1.bin.gz"));
     }
 
     /**
      * @param file
      * @throws IOException
      * @throws
      */
     public static GPEHEvent parseEventFile(File file, Set<Integer> possibleIds,GPEHFileNameWrapper wr) throws IOException {
         GPEHEvent result = new GPEHEvent();
         InputStream in = new FileInputStream(file);
         if (Pattern.matches("^.+\\.gz$", file.getName())) {
             in = new GZIPInputStream(in);
         }
         BitInputStream input = new BitInputStream(in);
         try {
             while (true) {
                 parseSubFile(input, result, possibleIds,wr);
             }
         } catch (EOFException e) {
             // normal behavior
         } finally {
             in.close();
         }
         return result;
     }
 
     /**
      * Parse sub file
      * 
      * @param input input stream
      * @param result GPEHEvent
      * @param wr 
      */
     public static void parseSubFile(BitInputStream input, GPEHEvent result, Set<Integer> possibleIds, GPEHFileNameWrapper wr) throws IOException {
         int recordLen = input.readRawUInt(16) - 3;
         int recordType = input.readRawUInt(8);
         if (recordType == 4) {
             parseEvent(input, result, recordLen, possibleIds,wr);
         } else if (recordType == 7) {
             pareseFooter(input, result);
         } else if (recordType == 6) {
             pareseError(input, result);
         } else {
             // wrong file format!
             throw new IllegalArgumentException();
         }
     }
     
     private static final HashMap<Integer, Events> eventMap;
     
     static {
         eventMap = new HashMap<Integer, Events>();
         
         for (Events event : Events.values()) {
             eventMap.put(event.getId(), event);
         }
     }
     
     
     
     /**
      *Parse event
      * 
      * @param input input stream
      * @param result GPEHEvent
      * @param recordLen length of event
      * @throws IOException
      */
     public static long parseEvent(BitInputStream input, GPEHEvent result, int recordLen, Set<Integer> possibleIds,GPEHFileNameWrapper timeWrapper) throws IOException {
         GPEHEvent.Event event = new GPEHEvent.Event();
         
         event.scannerId = (Integer)readParameter(input, Parameters.EVENT_PARAM_SCANNER_ID).getLeft();
         event.hour = (Long)readParameter(input, Parameters.EVENT_PARAM_TIMESTAMP_HOUR).getLeft();
         event.minute = (Long)readParameter(input, Parameters.EVENT_PARAM_TIMESTAMP_MINUTE).getLeft();
         event.second = (Long)readParameter(input, Parameters.EVENT_PARAM_TIMESTAMP_SECOND).getLeft();
         event.millisecond = (Long)readParameter(input, Parameters.EVENT_PARAM_TIMESTAMP_MILLISEC).getLeft();
         event.id = (Integer)readParameter(input, Parameters.EVENT_PARAM_EVENT_ID).getLeft();
 
         final int recLen = recordLen << 3;
         int len = 63; //24 + 5 + 6 + 6 + 11 + 11;
         
         if (!possibleIds.contains(event.id)) {
             // LOGGER.debug("EventID = " + event.id + " skipped");
             input.skipBitsNoCRC(recLen - len);
             return recordLen;
         }
 //        if (!timeWrapper.checkDate(event.hour, event.minute, event.second, event.millisecond)) {
 //            input.skipBitsNoCRC(recLen - len);
 //            return recordLen;
 //        }        
         // LOGGER.debug("EventID = " + event.id + " passed");
         Events events = eventMap.get(event.id);
         
         boolean parseOk = false;
         if (events != null) {
             parseOk = true;
             event.setType(events);
             // TODO debug
             Parameters[] allParameters = events.getAllParameters();
             for (int i = 0; i < allParameters.length; i++) {
                 Parameters parameter = allParameters[i];
                 int maxBitLen = recLen - len;
                 if (parameter == Parameters.EVENT_PARAM_MESSAGE_CONTENTS) {
                     Integer lenMsg = (Integer)event.getProperties().get(Parameters.EVENT_PARAM_MESSAGE_LENGTH);
                     if (lenMsg != null) {
                         maxBitLen = Math.min(lenMsg << 8, maxBitLen);
                     }
                 }
                 final Pair<Object, Integer> readParameter = readParameter(input, parameter, maxBitLen);
                 event.addProperty(parameter, readParameter.getLeft());
                 int bitsLen = readParameter.getRight();
                 len += bitsLen;
             }
         }
         if (parseOk) {
             result.addEvent(event);
         } else {
             // LOGGER.debug("Event not parsed!\t"+event.id);
         }
         if (len < recLen) {
             input.skipBitsNoCRC(recLen - len);
             if (parseOk && len + 32 < recLen) {
 //                LOGGER.debug("Wrong parsing !\t" + event.id);
             }
         } else if (len > recLen) {
             throw new UnexpectedException("to large");
         }
         
         return recordLen;
         // LOGGER.debug(event.id);
         // readBits= readBits(bits,input,16);
         // event.ueContextId=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,7);
         // event.rncModuleId=Integer.valueOf(readBits, 2);
         //        
         // readBits= readBits(bits,input,17);
         // event.cellID1=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,13);
         // event.rncID1=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,17);
         // event.cellID2=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,13);
         // event.rncID2=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,17);
         // event.cellID3=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,13);
         // event.rncID3=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,17);
         // event.cellID4=Integer.valueOf(readBits, 2);
         // readBits= readBits(bits,input,13);
         // event.rncID4=Integer.valueOf(readBits, 2);
         //        
         // int len = (recordLen-3)*8-5-6-6-22-16-7-17-13-17-13-17-13-17-13;
         // LOGGER.debug(len);
         // event.notParsed=readBits(bits,input,len);
         // LOGGER.debug(bits.toString());
 
     }
 
     /**
      *Read value
      * 
      * @param input - BitInputStream
      * @param parameter - parameter
      * @return readed value
      * @throws IOException
      */
     private static Pair<Object, Integer> readParameter(BitInputStream input, Parameters parameter) throws IOException {
         return readParameter(input, parameter, parameter.getBitsLen());
     }
 
     /**
      *Read value
      * 
      * @param input - BitInputStream
      * @param parameter - parameter
      * @param maxBitLen- maximum of readed bits
      * @return readed value
      * @throws IOException
      */
     private static Pair<Object, Integer> readParameter(BitInputStream input, Parameters parameter, int maxBitLen) throws IOException {
 
         int bitsLen = Math.min(parameter.getBitsLen(), maxBitLen);
         int bitsLenToRead = bitsLen;
         
 //        if (parameter.firstBitIsError()) {
 //            int bit = input.readRawUInt(1);
 //            if (bit != 0) {
 //                input.readRawULong(bitsLen - 1);
 //                return new Pair<Object, Integer>(null, bitsLen);
 //            }
 //            bitsLenToRead = bitsLenToRead - 1;
 //        }
         
         switch (parameter.getRule()) {
         case INTEGER:
             Integer iValue = checkValue(parameter, input.readRawUInt(bitsLenToRead)); 
             return new Pair<Object, Integer>(iValue, bitsLen);
         case LONG:
             Long lValue = checkValue(parameter, input.readRawULong(bitsLenToRead));
             return new Pair<Object, Integer>(lValue, bitsLen);
         case STRING:
             Pair<String, Integer> result = readString(input, bitsLen);
             return new Pair<Object, Integer>(result.left(), result.right());
         case BITARRAY:
             return new Pair<Object, Integer>(readBitArray(input, bitsLen), bitsLen);
         default:
             break;
         }
         return null;
     }
     
     private static Long checkValue(Parameters parameter, long value) {
         if (parameter.firstBitIsError()) {
             //LN, 8.12.2010, first convert to long
             long one = 0x1;
             long shift = parameter.getBitsLen();
             long mask = ~(one << shift);
             long newValue = value & mask;
             
             if (newValue != value) {
                 return null;
             }
             else {
                 return newValue;
             }
         }
         else {
             return value;
         }
     }
     
     private static Integer checkValue(Parameters parameter, int value) {
         if (parameter.firstBitIsError()) {
             int mask = ~(0x1 << parameter.getBitsLen() - 1);
             int newValue = value & mask;
             
             if (newValue != value) {
                 return null;
             }
             else {
                 return newValue;
             }
         }
         else {
             return value;
         }
     }
 
     /**
      * Read bit array.
      * 
      * @param input the input
      * @param bitsLen the bits len
      * @return the byte[]
      * @throws IOException Signals that an I/O exception has occurred.
      */
     private static byte[] readBitArray(BitInputStream input, int bitsLen) throws IOException {
         int size = (bitsLen >> 3) + ((bitsLen % 8) == 0 ? 0 : 1);
         byte[] result = new byte[size];
         int count = 0;
         int i = 0;
         while (count < bitsLen) {
             int readbit = Math.min(8, bitsLen - count);
             int byteSymb = input.readRawInt(readbit);
             result[i] = (byte)byteSymb;
             count += readbit;
             i++;
         }
         return result;
     }
 
     /**
      * @param bits
      * @param input
      * @param i
      * @return
      * @throws IOException
      * @throws NumberFormatException
      */
     private static Pair<String, Integer> readString(BitInputStream input, int len) throws NumberFormatException, IOException {
         int count = 0;
         StringBuilder result = new StringBuilder();
         while (count + 8 <= len) {
             int byteSymb = input.readRawInt(8);
             count += 8;
             if (byteSymb == 0) {
                 break;
             }
             result.append((char)byteSymb);
         }
         return new Pair<String, Integer>(result.toString(), count);
     }
 
     /**
      * @param bits
      * @param input
      * @param i
      * @return
      * @throws IOException
      * @throws NumberFormatException
      */
     private static String readAllString(BitInputStream input, int len) throws NumberFormatException, IOException {
         StringBuilder result = new StringBuilder();
         for (int i = 0; i < len; i++) {
             int byteSymb = input.readRawInt(8);
             if (byteSymb == 0) {
                 continue;
             }
             result.append((char)byteSymb);
         }
         return result.toString();
     }
 
 }
