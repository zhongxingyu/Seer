 package cdf;
 
 import java.io.EOFException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.SequenceInputStream;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public abstract class BankBuf implements Buf {
 
     private final long size_;
     private boolean isBit64_;
     private boolean isBigendian_;
 
     private static final Logger logger_ =
         Logger.getLogger( BankBuf.class.getName() );
 
     protected BankBuf( long size, boolean isBit64, boolean isBigendian ) {
         size_ = size;
         isBit64_ = isBit64;
         isBigendian_ = isBigendian;
     }
 
     /**
      * Returns the bank which can read a given number of bytes starting
      * at the given offset.
      */
     protected abstract Bank getBank( long offset, int count )
             throws IOException;
 
     /**
      * Returns a list of active banks.  Banks which have not been
      * created yet do not need to be included.
      */
     protected abstract List<Bank> getExistingBanks();
 
     /**
      * Returns an iterator over banks starting with the one containing
      * the given offset.
      */
     protected abstract Iterator<Bank> getBankIterator( long offset );
 
     public long getLength() {
         return size_;
     }
 
     public int readUnsignedByte( Pointer ptr ) throws IOException {
         long pos = ptr.getAndIncrement( 1 );
         Bank bank = getBank( pos, 1 );
         return bank.byteBuffer_.get( bank.adjust( pos ) );
     }
 
     public int readInt( Pointer ptr ) throws IOException {
         long pos = ptr.getAndIncrement( 4 );
         Bank bank = getBank( pos, 4 );
         return bank.byteBuffer_.getInt( bank.adjust( pos ) );
     }
 
     public long readOffset( Pointer ptr ) throws IOException {
         int nbyte = isBit64_ ? 8 : 4;
         long pos = ptr.getAndIncrement( nbyte );
         Bank bank = getBank( pos, nbyte );
         int apos = bank.adjust( pos );
         return isBit64_ ? bank.byteBuffer_.getLong( apos )
                         : (long) bank.byteBuffer_.getInt( apos );
     }
 
     public String readAsciiString( Pointer ptr, int nbyte ) throws IOException {
         long pos = ptr.getAndIncrement( nbyte );
         Bank bank = getBank( pos, nbyte );
         byte[] abuf = new byte[ nbyte ];
         ByteBuffer bbuf = bank.byteBuffer_;
         synchronized ( bbuf ) {
             bbuf.position( bank.adjust( pos ) );
             bbuf.get( abuf, 0, nbyte );
         }
         StringBuilder sbuf = new StringBuilder( nbyte );
         for ( int i = 0; i < nbyte; i++ ) {
             byte b = abuf[ i ];
             if ( b == 0 ) {
                 break;
             }
             else {
                 sbuf.append( (char) b );
             }
         }
         return sbuf.toString();
     }
 
     public synchronized void setBit64( boolean isBit64 ) {
         isBit64_ = isBit64;
     }
 
     public boolean isBit64() {
         return isBit64_;
     }
 
     public synchronized void setEncoding( boolean bigend ) {
        isBigendian_ = bigend;
         for ( Bank bank : getExistingBanks() ) {
             bank.updateBankEncoding();
         }
     }
 
     public boolean isBigendian() {
         return isBigendian_;
     }
 
     public void readDataBytes( long offset, int count, byte[] array )
             throws IOException {
         Bank bank = getBank( offset, count );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.get( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.get( array, 0, count );
             }
         }
     }
 
     public void readDataShorts( long offset, int count, short[] array )
             throws IOException {
         Bank bank = getBank( offset, count * 2 );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.getShort( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.asShortBuffer().get( array, 0, count );
             }
         }
     }
 
     public void readDataInts( long offset, int count, int[] array )
             throws IOException {
         Bank bank = getBank( offset, count * 4 );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.getInt( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.asIntBuffer().get( array, 0, count );
             }
         }
     }
 
     public void readDataLongs( long offset, int count, long[] array )
             throws IOException {
         Bank bank = getBank( offset, count * 8 );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.getLong( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.asLongBuffer().get( array, 0, count );
             }
         }
     }
 
     public void readDataFloats( long offset, int count, float[] array )
             throws IOException {
         Bank bank = getBank( offset, count * 4 );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.getFloat( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.asFloatBuffer().get( array, 0, count );
             }
         }
     }
 
     public void readDataDoubles( long offset, int count, double[] array )
             throws IOException {
         Bank bank = getBank( offset, count * 8 );
         ByteBuffer dbuf = bank.dataBuffer_;
         int apos = bank.adjust( offset );
         if ( count == 1 ) {
             array[ 0 ] = dbuf.getDouble( apos );
         }
         else {
             synchronized ( dbuf ) {
                 dbuf.position( apos );
                 dbuf.asDoubleBuffer().get( array, 0, count );
             }
         }
     }
 
     public InputStream createInputStream( final long offset ) {
         final Iterator<Bank> bankIt = getBankIterator( offset );
         Enumeration<InputStream> inEn = new Enumeration<InputStream>() {
             boolean isFirst = true;
             public boolean hasMoreElements() {
                 return bankIt.hasNext();
             }
             public InputStream nextElement() {
                 Bank bank = bankIt.next();
                 ByteBuffer bbuf = bank.byteBuffer_.duplicate();
                 bbuf.position( isFirst ? bank.adjust( offset ) : 0 );
                 isFirst = false;
                 return Bufs.createByteBufferInputStream( bbuf );
             }
         };
         return new SequenceInputStream( inEn );
     }
 
     public Buf fillNewBuf( long count, InputStream in ) throws IOException {
         return count <= Integer.MAX_VALUE 
              ? fillNewSingleBuf( (int) count, in )
              : fillNewMultiBuf( count, in );
     }
 
     private Buf fillNewSingleBuf( int count, InputStream in )
             throws IOException {
         ByteBuffer bbuf = ByteBuffer.allocateDirect( count );
         ReadableByteChannel chan = Channels.newChannel( in );
         while ( count > 0 ) {
             int nr = chan.read( bbuf );
             if ( nr < 0 ) {
                 throw new EOFException();
             }
             else {
                 count -= nr;
             }
         }
         return Bufs.createBuf( bbuf, isBit64_, isBigendian_ );
     }
 
     private Buf fillNewMultiBuf( long count, InputStream in )
             throws IOException {
         File file = File.createTempFile( "cdfbuf", ".bin" );
         file.deleteOnExit();
         int bufsiz = 64 * 1024;
         byte[] buf = new byte[ bufsiz ];
         OutputStream out = new FileOutputStream( file );
         while ( count > 0 ) {
             int nr = in.read( buf );
             out.write( buf, 0, nr );
             count -= nr;
         }
         out.close();
         return Bufs.createBuf( file, isBit64_, isBigendian_ );
     }
 
     public static BankBuf createSingleBankBuf( ByteBuffer byteBuffer,    
                                                boolean isBit64,
                                                boolean isBigendian ) {
         return new SingleBankBuf( byteBuffer, isBit64, isBigendian );
     }
 
     public static BankBuf createMultiBankBuf( FileChannel channel, long size,
                                               int bankSize, boolean isBit64,
                                               boolean isBigendian ) {
         return new LazyMultiBankBuf( channel, size, bankSize,
                                      isBit64, isBigendian );
     }
 
     public static BankBuf createMultiBankBuf( ByteBuffer[] byteBuffers,
                                               boolean isBit64,
                                               boolean isBigendian ) {
         return new PreMultiBankBuf( byteBuffers, isBit64, isBigendian );
     }
 
     private static class SingleBankBuf extends BankBuf {
         private final Bank bank_;
         SingleBankBuf( ByteBuffer byteBuffer, boolean isBit64,
                        boolean isBigendian ) {
             super( byteBuffer.capacity(), isBit64, isBigendian );
             bank_ = new Bank( byteBuffer, 0 );
         }
         public Bank getBank( long offset, int count ) {
             return bank_;
         }
         public List<Bank> getExistingBanks() {
             return Collections.singletonList( bank_ );
         }
         public Iterator<Bank> getBankIterator( long offset ) {
             return Collections.singletonList( bank_ ).iterator();
         }
     }
 
     private static class PreMultiBankBuf extends BankBuf {
 
         private final Bank[] banks_;
         private final long[] starts_;
         private final long[] ends_;
         private int iCurrentBank_;
 
         PreMultiBankBuf( ByteBuffer[] byteBuffers,
                          boolean isBit64, boolean isBigendian ) {
             super( sumSizes( byteBuffers ), isBit64, isBigendian );
             int nbank = byteBuffers.length;
             banks_ = new Bank[ nbank ];
             starts_ = new long[ nbank ];
             ends_ = new long[ nbank ];
             long pos = 0L;
             for ( int ibank = 0; ibank < nbank; ibank++ ) {
                 ByteBuffer byteBuffer = byteBuffers[ ibank ];
                 banks_[ ibank ] = new Bank( byteBuffer, pos );
                 starts_[ ibank ] = pos;
                 pos += byteBuffer.capacity();
                 ends_[ ibank ] = pos;
             }
             iCurrentBank_ = 0;
         }
 
         protected Bank getBank( long offset, int count ) {
 
             // This is not synchronized, which means that the value of
             // iCurrentBank_ might be out of date (have been updated by
             // another thread).  It's OK not to defend against that,
             // since the out-of-date value would effectively just give
             // us a thread-local cached value, which is in fact an
             // advantage rather than otherwise.
             int ibank = iCurrentBank_;
 
             // Test if the most recently-used value is still correct
             // (usually it will be) and return it if so.
             if ( offset >= starts_[ ibank ] &&
                  offset + count <= ends_[ ibank ] ) {
                 return banks_[ ibank ];
             }
 
             // Otherwise, find the bank corresponding to the requested offset.
             else {
                 ibank = -1;
                 for ( int ib = 0; ib < banks_.length; ib++ ) {
                     if ( offset >= starts_[ ib ] && offset < ends_[ ib ] ) {
                         ibank = ib;
                         break;
                     }
                 }
 
                 // Update the cached value.
                 iCurrentBank_ = ibank;
 
                 // If it contains the whole requested run, return it.
                 if ( offset + count <= ends_[ ibank ] ) {
                     return banks_[ ibank ];
                 }
 
                 // Otherwise, the requested region straddles multiple banks.
                 // This should be a fairly unusual occurrence.
                 // Build a temporary bank to satisfy the request and return it.
                 else {
                     byte[] tmp = new byte[ count ];
                     int bankOff = (int) ( offset - starts_[ ibank ] );
                     int tmpOff = 0;
                     while ( count > 0 ) {
                         int n = (int)
                                 Math.min( count, ends_[ ibank ] - bankOff );
                         ByteBuffer bbuf = banks_[ ibank ].byteBuffer_;
                         synchronized ( bbuf ) {
                             bbuf.position( bankOff );
                             bbuf.get( tmp, tmpOff, n );
                         }
                         count -= n;
                         tmpOff += n;
                         bankOff = 0;
                         ibank++;
                     }
                     return new Bank( ByteBuffer.wrap( tmp ), offset );
                 }
             }
         }
 
         public List<Bank> getExistingBanks() {
             return Arrays.asList( banks_ );
         }
 
         public Iterator<Bank> getBankIterator( final long offset ) {
             Iterator<Bank> it = Arrays.asList( banks_ ).iterator();
             for ( int ib = 0; ib < banks_.length; ib++ ) {
                 if ( offset >= starts_[ ib ] ) {
                     return it;
                 }
                 it.next();
             }
             return it;  // empty
         }
 
         private static long sumSizes( ByteBuffer[] byteBuffers ) {
             long size = 0;
             for ( int i = 0; i < byteBuffers.length; i++ ) {
                 size += byteBuffers[ i ].capacity();
             }
             return size;
         }
     }
 
     private static class LazyMultiBankBuf extends BankBuf {
 
         private final FileChannel channel_;
         private final long size_;
         private final int bankSize_;
         private final Bank[] banks_;
 
         LazyMultiBankBuf( FileChannel channel, long size, int bankSize,
                           boolean isBit64, boolean isBigendian ) {
             super( size, isBit64, isBigendian );
             channel_ = channel;
             size_ = size;
             bankSize_ = bankSize;
             int nbank = (int) ( ( ( size - 1 ) / bankSize ) + 1 );
             banks_ = new Bank[ nbank ];
         }
 
         public Bank getBank( long offset, int count ) throws IOException {
 
             // Find out the index of the bank containing the starting offset.
             int ibank = (int) ( offset / bankSize_ );
 
             // If the requested read amount is fully contained in that bank,
             // lazily obtain and return it.
             int over = (int) ( offset + count - ( ibank + 1 ) * bankSize_ );
             if ( over <= 0 ) {
                 return getBankByIndex( ibank );
             }
 
             // Otherwise, the requested region straddles multiple banks.
             // This should be a fairly unusual occurrence.
             // Build a temporary bank to satisfy the request and return it.
             else {
                 byte[] tmp = new byte[ count ];
                 int bankOff = count - over;
                 int tmpOff = 0;
                 while ( count > 0 ) {
                     int n = (int)
                         Math.min( count, ( ibank + 1 ) * bankSize_ - bankOff );
                     ByteBuffer bbuf = getBankByIndex( ibank ).byteBuffer_;
                     synchronized ( bbuf ) {
                         bbuf.position( bankOff );
                         bbuf.get( tmp, tmpOff, n );
                     }
                     count -= n;
                     tmpOff += n;
                     bankOff = 0;
                     ibank++;
                 }
                 return new Bank( ByteBuffer.wrap( tmp ), offset );
             }
         }
 
         public List<Bank> getExistingBanks() {
             List<Bank> list = new ArrayList<Bank>();
             for ( int ib = 0; ib < banks_.length; ib++ ) {
                 Bank bank = banks_[ ib ];
                 if ( bank != null ) {
                     list.add( bank );
                 }
             }
             return list;
         }
 
         public Iterator<Bank> getBankIterator( final long offset ) {
             return new Iterator<Bank>() {
                 int ibank = (int) ( offset / bankSize_ );
                 public boolean hasNext() {
                     return ibank < banks_.length;
                 }
                 public Bank next() {
                     try {
                         return getBankByIndex( ibank++ );
                     }
                     catch ( IOException e ) {
                         logger_.log( Level.WARNING, "Error acquiring bank", e );
                         return null;
                     }
                 }
                 public void remove() {
                     throw new UnsupportedOperationException();
                 }
             };
         }
 
         /**
          * Lazily obtains and returns a numbered bank.  Will not return null.
          */
         private Bank getBankByIndex( int ibank ) throws IOException {
             if ( banks_[ ibank ] == null ) {
                 long start = ibank * bankSize_;
                 long end = Math.min( ( ( ibank + 1 ) * bankSize_ ), size_ );
                 int leng = (int) ( end - start );
                 ByteBuffer bbuf =
                     channel_.map( FileChannel.MapMode.READ_ONLY, start, leng );
                 banks_[ ibank ] = new Bank( bbuf, start );
             }
             return banks_[ ibank ];
         }
     }
 
     protected class Bank {
         private final ByteBuffer byteBuffer_;
         private final ByteBuffer dataBuffer_;
         private final long start_;
         public Bank( ByteBuffer byteBuffer, long start ) {
             byteBuffer_ = byteBuffer;
             dataBuffer_ = byteBuffer.duplicate();
             start_ = start;
             updateBankEncoding();
         }
         private int adjust( long pos ) {
             long offset = pos - start_;
             if ( offset >= 0 && offset <= Integer.MAX_VALUE ) {
                 return (int) offset;
             }
             else {
                 throw new IllegalArgumentException( "Out of range: " + pos
                                                   + " for bank at " + start_ );
             }
         }
         private void updateBankEncoding() {
             dataBuffer_.order( isBigendian_ ? ByteOrder.BIG_ENDIAN
                                             : ByteOrder.LITTLE_ENDIAN );
         }
     }
 }
