 package org.denis.webview.util.io;
 
 import java.io.FilterReader;
 import java.io.IOException;
 import java.io.Reader;
 
 /**
  * Provides basic infrastructure for {@link Reader input symbol stream} decorators.
  * <p/>
  * Stream data reading methods are designed as <code>GoF Template Methods</code>, consult method-level
  * documentation for more information about that.
  * <p/>
  * Not thread-safe.
  * <p/>
  * <b>Memory overhead</b>
  * Current class uses additional char array during performing target conversion. It's default size is defined
  * by {@link #DEFAULT_INTERNAL_BUFFER_SIZE} constant. It may expand in runtime but not more than the size of
  * the buffer used by client during calls to {@link #read(char[], int, int)}.
  *
  * @author Denis Zhdanov
  * @since Jun 27, 2010
  */
 public abstract class AbstractReplacingFilterReader extends FilterReader {
 
     /** Default internal buffer size. */
     public static final int DEFAULT_INTERNAL_BUFFER_SIZE = 1024;
 
     private final DataContext dataContext = new DataContext();
 
     /** Char array used during {@link #read() single char reading}. */
     private final char[] singleCharReadBuffer = new char[1];
 
     private final int maxReplacementSize;
 
     /**
      * Internal buffer large enough to perform single symbol decoding if necessary.
      * <p/>
      * Is assumed to contain non-decoded data.
      */
     private char[] internalBuffer;
 
     /**
      * Index within {@link #internalBuffer} that points to position of the first stored symbol.
      */
     private int internalBufferStartOffset;
 
     /**
      * Index within {@link #internalBuffer} that points to position just after the last stored symbol.
      */
     private int internalBufferEndOffset;
 
     /**
      * Constructs new <code>AbstractReplacingFilterReader</code> object that decorates given symbol input stream
      * and uses internal buffer of initial size defined via given parameter.
      *
      * @param in                                input stream to decorate
      * @param maxReplacementSize                maximum number of symbols that may be replaced by the actual
      *                                          implementation class. For example replacement stream that works
      *                                          with http url-decoded entities should deliver '3' here because
      *                                          at most three symbols may be replaced ({@code %XX} pattern)
      * @throws IllegalArgumentException         if given symbol stream argument is <code>null</code> or initial
      *                                          buffer size is not positive
      */
     public AbstractReplacingFilterReader(Reader in, int maxReplacementSize) throws IllegalArgumentException {
         super(checkReaderOnConstruction(in));
         if (maxReplacementSize <= 0) {
             throw new IllegalArgumentException(String.format("Can't create decorator for symbol stream '%s'. Reason: "
                 + "given max replacement size is not positive (%d)", in, maxReplacementSize));
         }
 
         this.maxReplacementSize = maxReplacementSize;
         int bufferSizeToUse = DEFAULT_INTERNAL_BUFFER_SIZE;
         while (bufferSizeToUse < maxReplacementSize) {
             bufferSizeToUse <<= 1;
         }
         internalBuffer = new char[bufferSizeToUse];
     }
 
     private static Reader checkReaderOnConstruction(Reader in) throws IllegalArgumentException {
         if (in == null) {
             throw new IllegalArgumentException("Can't create symbol stream decorator. Reason: given stream "
                 + "to decorate is null");
         }
         return in;
     }
 
     @Override
     public int read() throws IOException {
         int read = read(singleCharReadBuffer);
         if (read < 0) {
             return read;
         }
         return singleCharReadBuffer[0];
     }
 
     /**
      * Reads characters from the wrapped stream into a portion of a given array performing
      * <a href="http://www.w3schools.com/TAGS/ref_urlencode.asp">url decoding</a> if necessary.
      *
      * @param buf       destination buffer
      * @param off       offset at which to start storing characters
      * @param len       maximum number of characters to read
      * @return          the number of characters read, or <code>-1</code> if the end of the stream has been reached
      * @throws IllegalArgumentException     if any of the given arguments is invalid
      * @throws IllegalStateException        if target decorated stream contains data that is inconsistent with url
      *                                      encoding rules
      * @throws IOException                  in case of unexpected exception during I/O processing
      */
     @Override
     public int read(char[] buf, int off, int len) throws IllegalArgumentException, IllegalStateException, IOException {
         checkReadArguments(buf, off, len);
         int copiedCharactersNumber = copy(buf, off, len);
         if (copiedCharactersNumber == len) {
             return len;
         }
 
         int lengthToUse = len - copiedCharactersNumber;
         initInternalBuffer(Math.max(len, maxReplacementSize));
         int read = super.read(
            internalBuffer, internalBufferEndOffset, internalBuffer.length - internalBufferEndOffset
         );
         if (read < 0) {
             if (copiedCharactersNumber > 0) {
                 return copiedCharactersNumber;
             }
             if (getCachedSymbolsNumber() > 0) {
                 throw new IllegalStateException(String.format("Detected situation that target symbol stream ends "
                         + "with the data that is inconsistent with url encoding rules - '%s'",
                         new String(internalBuffer, internalBufferStartOffset, getCachedSymbolsNumber())));
             }
             return read;
         }
 
         internalBufferEndOffset += read;
         return copiedCharactersNumber + copy(buf, off + copiedCharactersNumber, lengthToUse);
     }
 
     /**
      * Asks to copy data from the given <code>'internal'</code> buffer to the given <code>'external'</code> buffer.
      * <p/>
      * <code>'Internal'</code> buffer here means intermediate internal buffer used by the current class during
      * transforming the data read from the given stream on the fly. I.e. the general algorithm looks like below:
      * <ol>
      *     <li>Read <code>'raw'</code> the data from decorated symbol stream to <code>'internal'</code> buffer;</li>
      *     <li>
      *         Ask real implementation of this class to copy the data performing necessary conversion if necessary
      *         from internal buffer to the one given by the class client (<code>'external'</code> buffer);
      *     </li>
      *     <li>Return number of symbols actually copied to external buffer;</li>
      * </ol>
      * <p/>
      * Class that implements this method should be ready to the situation when given internal buffer is empty,
      * i.e. it's not guaranteed that given internal buffer end is greater than internal buffer start all the time.
      * <p/>
      * Another note is that implementations are expected to modify internal buffer-related characteristics during
      * the copying in order to provide information about processed raw data to the current class.
      *
      * @param dataContext        holds target data to copy
      * @return                   number of characters actually written to the given external buffer
      * @throws IllegalStateException    if it's encountered that decorated symbol stream contains invalid data
      */
     protected abstract int copy(DataContext dataContext) throws IllegalStateException;
 
     private int copy(char[] externalBuffer, int externalOffset, int externalLength) {
         if (internalBufferEndOffset <= internalBufferStartOffset) {
             resetInternalBufferIfPossible();
             // Nothing to copy.
             return 0;
         }
         dataContext.externalBuffer = externalBuffer;
         dataContext.externalOffset = externalOffset;
         dataContext.externalLength = externalLength;
         dataContext.internalBuffer = internalBuffer;
         dataContext.internalStart = internalBufferStartOffset;
         dataContext.internalEnd = internalBufferEndOffset;
 
         int result = copy(dataContext);
 
         internalBufferStartOffset = dataContext.internalStart;
         internalBufferEndOffset = dataContext.internalEnd;
 
         resetInternalBufferIfPossible();
 
         return result;
     }
 
     /**
      * Allows to answer if given symbol is a decimal digit.
      *
      * @param c     symbol to check
      * @return      <code>true</code> if given symbol represents decimal digit; <code>false</code> otherwise
      */
     protected static boolean isDecimalDigit(char c) {
         return '0' <= c && c <= '9';
     }
 
     /**
      * Allows to answer if given symbol is a hex digit.
      *
      * @param c     symbol to check
      * @return      <code>true</code> if given symbol represents decimal digit; <code>false</code> otherwise
      */
     protected static boolean isHexDigit(char c) {
         return isDecimalDigit(c) || ('A' <= c && c <= 'F') || ('a' <= c && c <= 'f');
     }
 
     /**
      * Converts given symbol representation of decimal or hex symbol to its numeric value.
      *
      * @param c     symbol representation of the target symbol
      * @return      numeric representation of the given symbol
      * @throws IllegalArgumentException     if given symbols doesn't represent decimal or hex symbol
      */
     protected static int toNumber(char c) throws IllegalArgumentException {
         if (c >= '0' && c <= '9') {
             return c - '0';
         }
         if (c >= 'A' && c <= 'F') {
             return c - 'A' + 10;
         }
         if (c >= 'a' && c <= 'f') {
             return c - 'a' + 10;
         }
         throw new IllegalArgumentException(String.format("Can't covert given symbol (%c) to number. Reason: "
             + "the symbol is expected to represent decimal or hex value, i.e. belong to one of the following "
             + "ranges - [0; 9], ['a'; 'f'] or ['A'; 'F']", c));
     }
 
     private void resetInternalBufferIfPossible() {
         if (internalBufferStartOffset > 0 && internalBufferEndOffset <= internalBufferStartOffset) {
             internalBufferStartOffset = internalBufferEndOffset = 0;
         }
     }
 
     /**
      * Checks if given arguments are valid in term of {@link #read(char[], int, int)} contract.
      *
      * @param buf       char buffer to use
      * @param off       offset to use within the given buffer
      * @param len       available length to use with the given buffer
      * @throws IllegalArgumentException     if any of the given parameters is invalid
      */
     private static void checkReadArguments(char[] buf, int off, int len) throws IllegalArgumentException {
         if (buf == null) {
             throw new IllegalArgumentException(String.format("Can't read data to the given char buffer. "
                     + "Reason: it is null. Offset: %d, length: %d", off, len));
         }
 
         if (off < 0 || off >= buf.length) {
             throw new IllegalArgumentException(String.format("Can't read data to the given char buffer (size %d). "
                     + "Reason: given offset is invalid (%d). Length: %d", buf.length, off, len));
         }
 
         if (len <= 0 || off + len > buf.length) {
             throw new IllegalArgumentException(String.format("Can't read data to the given char buffer "
                     + "(size %d, offset %d). Reason: given length is invalid (%d). It's expected to belong "
                     + "to [1; %d] range", buf.length, off, len, buf.length - off));
         }
     }
 
     private int getCachedSymbolsNumber() {
         return internalBufferEndOffset - internalBufferStartOffset;
     }
 
     /**
      * Expands internal buffer if necessary in accordance with the given target buffer length.
      *
      * @param bufferLengthToEnsure        length of continuous space that should be available at {@link #internalBuffer}
      */
     private void initInternalBuffer(int bufferLengthToEnsure) {
         // Keep internal buffer as is if it's trailing free space is big enough.
         if (bufferLengthToEnsure - getCachedSymbolsNumber() <= internalBuffer.length - internalBufferEndOffset) {
             return;
         }
 
         // Perform internal buffer defragmentation if that frees continuous empty space that is big enough to
         // hold target data length.
         if (bufferLengthToEnsure <= internalBuffer.length) {
             System.arraycopy(internalBuffer, internalBufferStartOffset, internalBuffer, 0, getCachedSymbolsNumber());
             internalBufferEndOffset = getCachedSymbolsNumber();
             internalBufferStartOffset = 0;
             return;
         }
 
         // Create new buffer that is wide enough and replace currently used one with it.
         char[] newBuffer = new char[bufferLengthToEnsure];
         if (getCachedSymbolsNumber() > 0) {
             System.arraycopy(internalBuffer, internalBufferStartOffset, newBuffer, 0, getCachedSymbolsNumber());
         }
         internalBuffer = newBuffer;
         internalBufferEndOffset = getCachedSymbolsNumber();
         internalBufferStartOffset = 0;
     }
 
     /**
      * Internal class used to hold intermediate data used during performing copying and transformation of
      * raw data.
      */
     protected static class DataContext {
 
         /** Target buffer to hold resulting (converted) data */
         public char[] externalBuffer;
 
         /** Start offset to use within the given external buffer. */
         public int externalOffset;
 
         /** Maximum number of characters to write to the given external buffer */
         public int externalLength;
 
         /** Buffer that contains raw data to be converted if necessary and copied to the given external buffer */
         public char[] internalBuffer;
 
         /** Offset within the internal buffer that points to the starting location of the raw data (inclusive). */
         public int internalStart;
 
         /** Offset within the given internal buffer that points to location just after the raw data (exclusive) */
         public int internalEnd;
     }
 }
