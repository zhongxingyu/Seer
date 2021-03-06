 package net.jpountz.lz4;
 
 /*
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
 
 import java.io.FilterOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.zip.Checksum;
 
 import net.jpountz.util.Utils;
 import net.jpountz.xxhash.StreamingXXHash32;
 import net.jpountz.xxhash.XXHash32;
 import net.jpountz.xxhash.XXHashFactory;
 
 /**
  * Streaming LZ4.
  * <p>
  * This class compresses data into fixed-size blocks of compressed data.
  * @see LZ4BlockInputStream
  */
 public final class LZ4BlockOutputStream extends FilterOutputStream {
 
   static final byte[] MAGIC = new byte[] { 'L', 'Z', '4', 'B', 'l', 'o', 'c', 'k' };
   static final int MAGIC_LENGTH = MAGIC.length;
 
   static final int HEADER_LENGTH =
       MAGIC.length // magic bytes
       + 1          // token
       + 4          // compressed length
       + 4          // decompressed length
       + 4;         // checksum
 
   static final int COMPRESSION_LEVEL_BASE = 10;
   static final int MIN_BLOCK_SIZE = 64;
   static final int MAX_BLOCK_SIZE = 1 << (COMPRESSION_LEVEL_BASE + 0x0F);
 
   static final int COMPRESSION_METHOD_RAW = 0x10;
   static final int COMPRESSION_METHOD_LZ4 = 0x20;
 
   static final int DEFAULT_SEED = 0x9747b28c;
 
   private static int compressionLevel(int blockSize) {
     if (blockSize < 64) {
       throw new IllegalArgumentException("blockSize must be >= " + MIN_BLOCK_SIZE + ", got " + blockSize);
     } else if (blockSize > MAX_BLOCK_SIZE) {
       throw new IllegalArgumentException("blockSize must be <= " + MAX_BLOCK_SIZE + ", got " + blockSize);
     }
     int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1);
     assert (1 << compressionLevel) >= blockSize;
     assert blockSize * 2 > (1 << compressionLevel);
     compressionLevel = Math.max(0, compressionLevel - COMPRESSION_LEVEL_BASE);
     assert compressionLevel >= 0 && compressionLevel <= 0x0F;
     return compressionLevel;
   }
 
   private final int blockSize;
   private final int compressionLevel;
   private final LZ4Compressor compressor;
   private final Checksum checksum;
   private final byte[] buffer;
   private final byte[] compressedBuffer;
   private boolean finished;
   private int o;
 
   /**
    * Create a new {@link OutputStream}.
    *
    * @param out         the {@link OutputStream} to feed
    * @param blockSize   the maximum number of bytes to try to compress at once,
    *                    must be >= 64 and <= 32 M
    * @param compressor  the {@link LZ4Compressor} instance to use to compress
    *                    data
    * @param checksum    the {@link Checksum} instance to use to check data for
    *                    integrity.
    */
   public LZ4BlockOutputStream(OutputStream out, int blockSize, LZ4Compressor compressor, Checksum checksum) {
     super(out);
     this.blockSize = blockSize;
     this.compressor = compressor;
     this.checksum = checksum;
     this.compressionLevel = compressionLevel(blockSize);
     this.buffer = new byte[blockSize];
     this.compressedBuffer = new byte[HEADER_LENGTH + compressor.maxCompressedLength(blockSize)];
     o = 0;
     finished = false;
     System.arraycopy(MAGIC, 0, compressedBuffer, 0, MAGIC_LENGTH);
   }
 
   /**
    * Create a new instance which checks stream integrity using {@link XXHash32}.
    * @see #LZ4BlockOutputStream(OutputStream, int, LZ4Compressor, Checksum)
    * @see StreamingXXHash32#asChecksum()
    */
   public LZ4BlockOutputStream(OutputStream out, int blockSize, LZ4Compressor compressor) {
     this(out, blockSize, compressor, XXHashFactory.fastestInstance().newStreamingHash32(DEFAULT_SEED).asChecksum());
   }
 
   /**
    * Create a new instance which compresses with the standard LZ4 compression
    * algorithm.
    * @see #LZ4BlockOutputStream(OutputStream, int, LZ4Compressor)
    * @see LZ4Factory#fastCompressor()
    */
   public LZ4BlockOutputStream(OutputStream out, int blockSize) {
     this(out, blockSize, LZ4Factory.fastestInstance().fastCompressor());
   }
 
   /**
    * Create a new instance which compresses into blocks of 64 KB.
    * @see #LZ4BlockOutputStream(OutputStream, int)
    */
   public LZ4BlockOutputStream(OutputStream out) {
     this(out, 1 << 16);
   }
 
   private void ensureNotFinished() {
     if (finished) {
       throw new IllegalStateException("This stream is already closed");
     }
   }
 
   @Override
   public void write(int b) throws IOException {
     ensureNotFinished();
     if (o == blockSize) {
       flush();
    } else {
      buffer[o++] = (byte) b;
     }
   }
 
   @Override
   public void write(byte[] b, int off, int len) throws IOException {
     Utils.checkRange(b, off, len);
     ensureNotFinished();
     if (len < 0) {
       throw new IllegalArgumentException("len must be >= 0");
     }
    while (o + len >= blockSize) {
       final int l = blockSize - o;
       System.arraycopy(b, off, buffer, o, blockSize - o);
       o = blockSize;
       flush();
       off += l;
       len -= l;
     }
     System.arraycopy(b, off, buffer, o, len);
     o += len;
   }
 
   @Override
   public void write(byte[] b) throws IOException {
     ensureNotFinished();
     write(b, 0, b.length);
   }
 
   @Override
   public void close() throws IOException {
     ensureNotFinished();
     finish();
     out.close();
   }
 
   @Override
   public void flush() throws IOException {
     if (o == 0) {
       return;
     }
     checksum.reset();
     checksum.update(buffer, 0, o);
     final int check = (int) checksum.getValue();
     int compressedLength = compressor.compress(buffer, 0, o, compressedBuffer, HEADER_LENGTH);
     final int compressMethod;
     if (compressedLength >= o) {
       compressMethod = COMPRESSION_METHOD_RAW;
       compressedLength = o;
       System.arraycopy(buffer, 0, compressedBuffer, HEADER_LENGTH, o);
     } else {
       compressMethod = COMPRESSION_METHOD_LZ4;
     }
 
     compressedBuffer[MAGIC_LENGTH] = (byte) (compressMethod | compressionLevel);
     writeIntLE(compressedLength, compressedBuffer, MAGIC_LENGTH + 1);
     writeIntLE(o, compressedBuffer, MAGIC_LENGTH + 5);
     writeIntLE(check, compressedBuffer, MAGIC_LENGTH + 9);
     assert MAGIC_LENGTH + 13 == HEADER_LENGTH;
     out.write(compressedBuffer, 0, HEADER_LENGTH + compressedLength);
     o = 0;
   }
 
   /**
    * Same as {@link #close()} except that it doesn't close the underlying stream.
    * This can be useful if you want to keep on using the underlying stream.
    */
   public void finish() throws IOException {
     ensureNotFinished();
     flush();
     compressedBuffer[MAGIC_LENGTH] = (byte) (COMPRESSION_METHOD_RAW | compressionLevel);
     writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 1);
     writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 5);
     writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 9);
     assert MAGIC_LENGTH + 13 == HEADER_LENGTH;
     out.write(compressedBuffer, 0, HEADER_LENGTH);
     finished = true;
   }
 
   private static void writeIntLE(int i, byte[] buf, int off) {
     buf[off++] = (byte) i;
     buf[off++] = (byte) (i >>> 8);
     buf[off++] = (byte) (i >>> 16);
     buf[off++] = (byte) (i >>> 24);
   }
 
   @Override
   public String toString() {
     return getClass().getSimpleName() + "(out=" + out + ", blockSize=" + blockSize
         + ", compressor=" + compressor + ", checksum=" + checksum + ")";
   }
 
 }
