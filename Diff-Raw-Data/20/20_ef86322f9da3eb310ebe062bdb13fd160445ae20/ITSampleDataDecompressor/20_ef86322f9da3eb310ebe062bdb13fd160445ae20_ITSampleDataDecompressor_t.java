 package ca.mrb0.hydrocitee.it;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import ca.mrb0.hydrocitee.it.ITSampleData.CompressionType;
 
 import com.google.common.collect.Lists;
 
 /**
  * Sample data decompression algo from SchismTracker / Cubic Player / Modplug,
  * ported from pyIT to Java.
  *  
  * @author mrb
  *
  */
 public class ITSampleDataDecompressor {
     private static Logger l = Logger.getLogger(ITSampleDataDecompressor.class);
 
     // Input
     
     private byte[] data;
     private int offs;
     private int dataLengthSamples;
     private boolean is16Bit;
     private CompressionType compressionType;
     
     private int bytesPerSample;
     
     // State
     private long bitPtr; // Pointer to next bit in input data
     private long blkPos; // Number of bytes decompressed from this block
     private long blkLen; // Length of uncompressed data in this block
     private long decodedSamples;
     private int width;
     private int d1, d2;
     
     protected ITSampleDataDecompressor(byte[] data, int offs,
             int dataLengthSamples, boolean is16Bit,
             CompressionType compressionType) {
         super();
         this.data = data;
         this.offs = offs;
         this.dataLengthSamples = dataLengthSamples;
         this.is16Bit = is16Bit;
         this.compressionType = compressionType;
         
         if (is16Bit) {
             bytesPerSample = 2;
         } else {
             bytesPerSample = 1;
         }
         
         decodedSamples = 0;
         bitPtr = 0;
         blkPos = 0;
         blkLen = 0;
     }
     
     private int readBits(int n) {
         int value = 0;
         
         for(int i = 0; i < n; i++, bitPtr++) {
             int b = data[(int)(offs + bitPtr / 8)] & 0xff;
             b = 0x01 & (b >>> (bitPtr % 8));
             value |= (b << i);
         }
         
         return value;
     }
     
     private int method1_readWidthValue() {
         if (is16Bit) {
             return readBits(4);
         } else {
             return readBits(3);
         }
     }
     
     public int nextSample() {
         if (is16Bit) {
             return nextSample16();
         } else {
             return nextSample8();
         }
     }
     
     private int nextSample8() {
         while(decodedSamples < dataLengthSamples) {
             if (blkPos >= blkLen) {
                 // Start of new block
                 blkPos = 0;
                 blkLen = Math.min(dataLengthSamples - decodedSamples, 0x8000);
                 width = 9;
                 d1 = d2 = 0;
                 
                 long unconsumedBits = (8 - (bitPtr % 8)) % 8;
                 bitPtr += unconsumedBits;
                 
                 int compressedDataLen = readBits(16);
                 
 //                l.debug(String.format("new block, compressed len = %d, output len = %d", compressedDataLen, blkLen));
             }
             
             while (blkPos < blkLen) {
                 assert (width <= 9);
                 
                 int value = readBits(width);
                 
                 if (width < 7) {
                     // method 1 (1 .. 6 bits)
                     if (value == 0x1 << (width - 1)) {
                         // value is 0b100...
                         value = readBits(3) + 1;
                         width = (value < width) ? value : value + 1;
                         assert (width <= 9);
                         continue;
                     }
                 } else if (width < 9) {
                     // method 2 (7 .. 8 bits)
                     int border = (0xff >> (9 - width)) & (~0x04);
                     if (value > border && value <= (border + 8)) {
                         value -= border;
                         width = (value < width) ? value : value + 1;
                         assert (width <= 9);
                         continue;
                     }
                 } else if (width == 9) {
                     // method 3 (9 bits)
                     if ((value & 0x100) != 0) {
                         width = (value + 1) & 0xff;
                         assert (width <= 9);
                         continue;
                     }
                 }
                 
                 int v;
                 if (width < 8) {
                     int shift = 8 - width;
                     v = (byte)((value << shift) & 0xff);
                     v = (v >> shift) & 0xff;
                 } else {
                     v = value & 0xff;
                 }
                 
                 d1 = (d1 + v) & 0xff;
                 d2 = (d2 + d1) & 0xff;
                 
                 blkPos++;
                 decodedSamples++;
                 
                 if (compressionType == CompressionType.IT215) {
                     return d2;
                 } else {
                     return d1;
                 }
             }
         }
         
         return -1;
     }
     
     private int nextSample16() {
         while(decodedSamples < dataLengthSamples) {
             if (blkPos >= blkLen) {
                 // Start of new block
                 blkPos = 0;
                 blkLen = Math.min(dataLengthSamples - decodedSamples, 0x4000);
                 width = 17;
                 d1 = d2 = 0;
                 
                 long unconsumedBits = (8 - (bitPtr % 8)) % 8;
                 bitPtr += unconsumedBits;
                 
                 int compressedDataLen = readBits(16);
                 
 //                l.debug(String.format("new block, compressed len = %d, output len = %d", compressedDataLen, blkLen));
             }
             
             while (blkPos < blkLen) {
                 assert (width <= 17);
                 
                 int value = readBits(width);
                 
                 if (width < 7) {
                     // method 1 (1 .. 6 bits)
                     if (value == 0x1 << (width - 1)) {
                         // value is 0b100...
                         value = readBits(4) + 1;
                         width = (value < width) ? value : value + 1;
                         assert (width <= 17);
                         continue;
                     }
                 } else if (width < 17) {
                     // method 2 (7 .. 17 bits)
                     int border = (0xffff >> (17 - width)) & (~0x8);
                     if (value > border && value <= (border + 16)) {
                         value -= border;
                         width = (value < width) ? value : value + 1;
                         assert (width <= 17);
                         continue;
                     }
                 } else if (width == 17) {
                     // method 3 (17 bits)
                     if ((value & 0x10000) != 0) {
                         width = (value + 1) & 0xff;
                         assert (width <= 17);
                         continue;
                     }
                 }
                 
                 int v;
                 if (width < 16) {
                     int shift = 16 - width;
                     v = (short)((value << shift) & 0xffff);
                     v = (v >> shift) & 0xffff;
                 } else {
                     v = value & 0xffff;
                 }
                 
                 d1 = (d1 + v) & 0xffff;
                 d2 = (d2 + d1) & 0xffff;
                 
                 blkPos++;
                 decodedSamples++;
                 
                 if (compressionType == CompressionType.IT215) {
                     return d2;
                 } else {
                     return d1;
                 }
             }
         }
         
         return -1;
     }
 
     protected static List<Integer> decompressSample(byte[] data, int offs,
             int dataLengthSamples, boolean is16Bit,
             CompressionType compressionType) {
         
         ITSampleDataDecompressor decompressor = new ITSampleDataDecompressor(data, offs, dataLengthSamples, is16Bit, compressionType);
         
         List<Integer> decompressed = Lists.newArrayListWithCapacity(dataLengthSamples);
         int decompressedIdx = 0;
         
         int sample;
         while ((sample = decompressor.nextSample()) != -1 && decompressedIdx < dataLengthSamples) {
            decompressedIdx++;
            decompressed.add(sample);
         }
         
         if (decompressedIdx != dataLengthSamples) {
             throw new IllegalArgumentException(String.format("Expected %d samples, but decompressed %d instead", dataLengthSamples, decompressedIdx));
         }
         
         return decompressed;
     }
 }
