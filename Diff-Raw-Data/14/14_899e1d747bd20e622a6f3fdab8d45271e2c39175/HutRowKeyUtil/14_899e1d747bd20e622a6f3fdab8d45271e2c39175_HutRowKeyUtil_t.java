 /**
  * Copyright 2010 Sematext International
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
 package com.sematext.hbase.hut;
 
 import org.apache.hadoop.hbase.util.Bytes;
 
 /**
  * Provides common method for key manipulation.
  *
  * HBaseRT adjusted key adds two long values to the end, comparing to the original key: creationTime and intervalEnd.
  * First long value (creationTime) is set when HBase record (<tt>Put</tt>) is created.
  * If second long value (intervalEnd) is > 0 then the record defined by the key represents processed (aka merged) data for
  * the whole interval of records with creationTime between these two values.
  */
 public final class HutRowKeyUtil {
   private static final long NOT_SET_MARK_VAL = 0L;
   private static final byte[] NOT_SET_MARK = Bytes.toBytes(NOT_SET_MARK_VAL);
 
   private HutRowKeyUtil() {}
 
   public static byte[] getOriginalKey(byte[] hutRowKey) {
     return Bytes.head(hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG * 2);
   }
 
   static boolean sameOriginalKeys(byte[] hutRowKey1, byte[] hutRowKey2) {
     // TODO: review whether we need *lexographic* comparison
     return 0 == Bytes.compareTo(hutRowKey1, 0, hutRowKey1.length - Bytes.SIZEOF_LONG * 2,
                                 hutRowKey2, 0, hutRowKey2.length - Bytes.SIZEOF_LONG * 2);
   }
 
   // TODO: add unit-test
   static boolean sameOriginalKeys(byte[] hutRowKey1, byte[] hutRowKey2, long mod) {
     if (0 != Bytes.compareTo(hutRowKey1, 0, hutRowKey1.length - Bytes.SIZEOF_LONG * 2,
                                 hutRowKey2, 0, hutRowKey2.length - Bytes.SIZEOF_LONG * 2)) {
       return false;
     }
 
     long firstKeyIntervalStart = Bytes.toLong(hutRowKey1,
             hutRowKey1.length - Bytes.SIZEOF_LONG * 2, Bytes.SIZEOF_LONG);
    long firstKeyIntervalStop = Bytes.toLong(hutRowKey1,
            hutRowKey1.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     long secondKeyIntervalStart = Bytes.toLong(hutRowKey2,
             hutRowKey2.length - Bytes.SIZEOF_LONG * 2, Bytes.SIZEOF_LONG);
    long secondKeyIntervalStop = Bytes.toLong(hutRowKey2,
            hutRowKey2.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
 
    boolean same = (firstKeyIntervalStart / mod) == (secondKeyIntervalStart / mod);
    same = firstKeyIntervalStop == NOT_SET_MARK_VAL ? same :
            same && (firstKeyIntervalStart / mod) == (firstKeyIntervalStop / mod);
    same = secondKeyIntervalStop == NOT_SET_MARK_VAL ? same :
            same && (secondKeyIntervalStart / mod) == (secondKeyIntervalStop / mod);

    return same;
   }
 
   // TODO: rename it or explain
   static boolean sameRecords(byte[] hutRowKey1, byte[] hutRowKey2) {
     return 0 == Bytes.compareTo(hutRowKey1, 0, hutRowKey1.length - Bytes.SIZEOF_LONG,
                                 hutRowKey2, 0, hutRowKey2.length - Bytes.SIZEOF_LONG);
   }
 
   /**
    * Is first key goes after the interval defined by the second key
    * @param hutRowKeyToCompare first key
    * @param hutRowKey second key
    * @return true if second key goes after the first key
    */
   static boolean isAfter(byte[] hutRowKeyToCompare, byte[] hutRowKey) {
     // comparing creationTime of second key with intervalEnd of the first key
     return Bytes.compareTo(hutRowKeyToCompare, hutRowKeyToCompare.length - Bytes.SIZEOF_LONG * 2, Bytes.SIZEOF_LONG,
                                 hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG) > 0;
   }
 
   static byte[] createNewKey(byte[] rowKeyWithIntervalStart, long creationTime) {
     return Bytes.add(rowKeyWithIntervalStart, Bytes.toBytes(creationTime), NOT_SET_MARK);
   }
 
   static byte[] getStartRowOfInterval(byte[] hutRowKey) {
     byte[] startRow = new byte[hutRowKey.length];
     System.arraycopy(hutRowKey, 0, startRow, 0, hutRowKey.length - Bytes.SIZEOF_LONG);
     System.arraycopy(NOT_SET_MARK, 0,
                      startRow, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     return startRow;
   }
 
   static byte[] getEndRowOfInterval(byte[] hutRowKey) {
     byte[] endRow = new byte[hutRowKey.length];
     System.arraycopy(hutRowKey, 0, endRow, 0, hutRowKey.length - 2 * Bytes.SIZEOF_LONG);
     System.arraycopy(hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG,
             endRow, hutRowKey.length - 2 * Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     System.arraycopy(NOT_SET_MARK, 0,
                      endRow, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     return endRow;
   }
 
   static void setIntervalEnd(byte[] hutRowKey, byte[] lastRowKeyInInterval) {
     boolean isIntervalEndSetForLastRowKey =
             Bytes.compareTo(lastRowKeyInInterval, lastRowKeyInInterval.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG,
                     NOT_SET_MARK, 0, NOT_SET_MARK.length) != 0;
     if (isIntervalEndSetForLastRowKey) {
       // setting hutRowKey.intervalEnd = lastRowKeyInInterval.intervalEnd
       System.arraycopy(lastRowKeyInInterval, lastRowKeyInInterval.length - Bytes.SIZEOF_LONG,
                        hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     } else {
       // setting hutRowKey.intervalEnd = lastRowKeyInInterval.creationTime
       System.arraycopy(lastRowKeyInInterval, lastRowKeyInInterval.length - Bytes.SIZEOF_LONG * 2,
                        hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     }
   }
 
   static boolean writtenAfter(byte[] hutRowKey, long startTsInclusive) {
     long startInterval = Bytes.toLong(hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG * 2, Bytes.SIZEOF_LONG);
     return startTsInclusive <= startInterval;
   }
 
   static boolean writtenBetween(byte[] hutRowKey, long startTsInclusive, long stopTsInclusive) {
     long rowStartInterval = Bytes.toLong(hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG * 2, Bytes.SIZEOF_LONG);
     long rowStopInterval = Bytes.toLong(hutRowKey, hutRowKey.length - Bytes.SIZEOF_LONG, Bytes.SIZEOF_LONG);
     return startTsInclusive <= rowStartInterval &&
             (NOT_SET_MARK_VAL == rowStopInterval ? stopTsInclusive >= rowStartInterval : stopTsInclusive >= rowStopInterval);
   }
 }
