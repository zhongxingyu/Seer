 /*
  * This class is supposed to find snippet of text in full text pattern
  * by means of Bitap algorithm
  */
 package ru.urbancamper.audiobookmarker.text;
 
 /**
  *
  * @author pozpl
  */
 public class BitapSubtextFinding {
 
 
     public Byte[] fillByteArrayFromWordsNumbersArray(Integer[] text, Integer wordTomark){
         Integer bytesArrayLength = text.length / 8;
         bytesArrayLength = text.length % 8 == 0 ? bytesArrayLength : bytesArrayLength + 1;
         Byte[] byteArray = new Byte[bytesArrayLength];
         Byte byteOne = 1;
         for(Integer bytesCounter = 0; bytesCounter < bytesArrayLength; bytesCounter++){
             Integer textArrayIndex = bytesCounter * 8;
             byteArray[bytesCounter] = 0;
             for(Integer bitCounter = 0; bitCounter < 8; bitCounter++){
                 if(text.length >= textArrayIndex + bitCounter + 1){
                    if ( text[textArrayIndex + bitCounter] == wordTomark){
                     byteArray[bytesCounter] = (byte)(byteArray[bytesCounter] | byteOne << bitCounter);
                    }
                 }
             }
         }
         return byteArray;
     }
 
 
 //    /**
 //     * Shift bytes array on n positions to the right
 //     * @param bytes
 //     * @param rightShifts
 //     */
 //    public static void shiftBitsRight(Byte[] bytes, final Integer rightShifts) {
 //        assert rightShifts >= 1 && rightShifts <= 7;
 //
 //        final Integer leftShifts = 8 - rightShifts;
 //
 //        Byte previousByte = bytes[0]; // keep the byte before modification
 //        bytes[0] = (byte) (((bytes[0] & 0xff) >> rightShifts) | ((bytes[bytes.length - 1] & 0xff) << leftShifts));
 //        for (Integer i = 1; i < bytes.length; i++) {
 //            Byte tmp = bytes[i];
 //            bytes[i] = (byte) (((bytes[i] & 0xff) >> rightShifts) | ((previousByte & 0xff) << leftShifts));
 //            previousByte = tmp;
 //        }
 //    }
 
    private void  shiftBitsLeft(Byte[] bytes) {
         Byte previousBit = 0;
         Byte currentBit = 0;
         previousBit = (byte)(bytes[0] & (1 << 7));
         for(Integer bytesCounter = 0; bytesCounter < bytes.length; bytesCounter++){
             currentBit = (byte)(bytes[bytesCounter] & (1 << 7));
             bytes[bytesCounter] = (byte)(bytes[bytesCounter] << 1);
             if(bytesCounter > 0){
                 bytes[bytesCounter] = (byte)(bytes[bytesCounter] | previousBit);
             }
             previousBit = (byte)((currentBit >> 7) & 1);
         }
     }
 

 
 }
