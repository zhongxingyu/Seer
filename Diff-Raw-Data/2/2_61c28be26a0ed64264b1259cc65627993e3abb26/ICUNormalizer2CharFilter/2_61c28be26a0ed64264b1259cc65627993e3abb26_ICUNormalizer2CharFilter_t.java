 /**
  * ICUNormalizer2CharFilter
  * Copyright 2010-2012 Ippei Ukai
  */
 package com.github.ippeiukai.icucharfilter.lucene.analysis.icu;
 
 import java.io.IOException;
 import java.io.Reader;
 import static java.lang.Math.*;
 
 import org.apache.lucene.analysis.BaseCharFilter;
 import org.apache.lucene.analysis.CharReader;
 import org.apache.lucene.analysis.CharStream;
 
 import com.ibm.icu.text.Normalizer2;
 
 public class ICUNormalizer2CharFilter extends BaseCharFilter {
   
   public static enum Form {
     NFC(Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.COMPOSE)),
     
     NFD(Normalizer2.getInstance(null, "nfc", Normalizer2.Mode.DECOMPOSE)),
     
     NFKC(Normalizer2.getInstance(null, "nfkc", Normalizer2.Mode.COMPOSE)),
     
     NFKC_CF(Normalizer2.getInstance(null, "nfkc_cf", Normalizer2.Mode.COMPOSE)),
     
     NFKD(Normalizer2.getInstance(null, "nfkc", Normalizer2.Mode.DECOMPOSE));
     
     private Normalizer2 normalizer;
     
     private Form(Normalizer2 normalizer) {
       this.normalizer = normalizer;
     }
   }
   
   private static final int IO_BUFFER_SIZE = 128;
   
   private final Normalizer2 normalizer;
   private final StringBuilder inputBuffer;
   private final StringBuilder resultBuffer;
   
   private boolean inputFinished;
   private boolean afterQuickCheckYes;
   private int checkedInputBoundary;
   private int charCount;
   
   /**
    * default is NFKC_CF
    */
   public ICUNormalizer2CharFilter(CharStream in) {
     this(in, Form.NFKC_CF);
   }
   
   public ICUNormalizer2CharFilter(Reader in, Form form) {
     this(CharReader.get(in), form);
   }
   
   public ICUNormalizer2CharFilter(CharStream in, Form form) {
     this(in, form.normalizer);
   }
   
   public ICUNormalizer2CharFilter(CharStream in, Normalizer2 normalizer) {
     super(in);
     if (normalizer == null) throw new NullPointerException("normalizer == null");
     
     this.normalizer = normalizer;
     inputBuffer = new StringBuilder();
     resultBuffer = new StringBuilder();
     resetFields();
   }
   
   @Override
   public void reset() throws IOException {
     super.reset();
     resetFields();
   }
   
   private void resetFields() {
     inputBuffer.delete(0, inputBuffer.length());
     checkedInputBoundary = 0;
     resultBuffer.delete(0, resultBuffer.length());
     inputFinished = false;
     afterQuickCheckYes = false;
     charCount = 0;
   }
   
   @Override
   public void close() throws IOException {
     resetFields();
     super.close();
   }
   
   @Override
   public int read(char[] cbuf, int off, int len) throws IOException {
     if (off < 0) throw new IllegalArgumentException("off < 0");
     if (off >= cbuf.length) throw new IllegalArgumentException(
         "off >= cbuf.length");
     if (len <= 0) throw new IllegalArgumentException("len <= 0");
     
     while (!inputFinished || inputBuffer.length() > 0 || resultBuffer.length() > 0) {
       int retLen;
       
       if (resultBuffer.length() > 0) {
         retLen = outputFromResultBuffer(cbuf, off, len);
         if (retLen > 0) {
           return retLen;
         }
       }
       
       int resLen = readAndNormalizeFromInput();
       if (resLen > 0) {
         retLen = outputFromResultBuffer(cbuf, off, len);
         if (retLen > 0) {
           return retLen;
         }
       }
       
       readInputToBuffer();
     }
     
     return -1;
   }
   
   private final char[] tmpBuffer = new char[IO_BUFFER_SIZE];
   
   private int readInputToBuffer() throws IOException {
     final int len = input.read(tmpBuffer);
     if (len == -1) {
       inputFinished = true;
       return 0;
     }
     inputBuffer.append(tmpBuffer, 0, len);
     return len;
   }
   
   private int readAndNormalizeFromInput() {
     if (inputBuffer.length() <= 0) {
       afterQuickCheckYes = false;
       return 0;
     }
     if (!afterQuickCheckYes) {
       int resLen = readFromInputWhileSpanQuickCheckYes();
       afterQuickCheckYes = true;
       if (resLen > 0) return resLen;
     }
     int resLen = readFromIoNormalizeUptoBoundary(resultBuffer);
     if(resLen > 0){
       afterQuickCheckYes = false;
     }
     return resLen;
   }
   
   private int readFromInputWhileSpanQuickCheckYes() {
     int end = normalizer.spanQuickCheckYes(inputBuffer);
     if (end > 0) {
       resultBuffer.append(inputBuffer.subSequence(0, end));
       inputBuffer.delete(0, end);
       checkedInputBoundary = max(checkedInputBoundary - end, 0);
       charCount += end;
     }
     return end;
   }
   
   private int readFromIoNormalizeUptoBoundary(StringBuilder dest) {
     if (inputBuffer.length() <= 0) {
       return 0;
     }
     
     boolean foundBoundary = false;
     final int bufLen = inputBuffer.length();
     
     while (checkedInputBoundary < bufLen - 1) {
       ++checkedInputBoundary;
       if (normalizer.hasBoundaryBefore(inputBuffer
           .charAt(checkedInputBoundary))) {
         foundBoundary = true;
         break;
       } else if (normalizer.hasBoundaryAfter(inputBuffer
           .charAt(checkedInputBoundary - 1))) {
         foundBoundary = true;
         break;
       }
     }
     if (checkedInputBoundary == bufLen - 1) {
       if (normalizer.hasBoundaryAfter(inputBuffer.charAt(checkedInputBoundary))
           || inputFinished) {
         foundBoundary = true;
         ++checkedInputBoundary;
       }
     }
     if (!foundBoundary) {
       return 0;
     }
     
     return normalizeInputUpto(checkedInputBoundary);
   }
   
   private int normalizeInputUpto(final int length) {
     final int destOrigLen = resultBuffer.length();
     normalizer.normalizeSecondAndAppend(resultBuffer,
         inputBuffer.subSequence(0, length));
     inputBuffer.delete(0, length);
     checkedInputBoundary = max(checkedInputBoundary - length, 0);
     final int resultLength = resultBuffer.length() - destOrigLen;
     recordOffsetDiff(length, resultLength);
     return resultLength;
   }
   
   private void recordOffsetDiff(int inputLength, int outputLength) {
     if (inputLength == outputLength) {
       charCount += outputLength;
       return;
     }
     final int diff = inputLength - outputLength;
     final int cumuDiff = getLastCumulativeDiff();
     if (diff < 0) {
       for (int i = 1;  i <= -diff; ++i) {
         addOffCorrectMap(charCount + i, cumuDiff - i);
       }
     } else {
      addOffCorrectMap(charCount + Math.min(1, outputLength), cumuDiff + diff);
     }
     charCount += outputLength;
   }
   
   private int outputFromResultBuffer(char[] cbuf, int begin, int len) {
     len = min(resultBuffer.length(), len);
     resultBuffer.getChars(0, len, cbuf, begin);
     if (len > 0) {
       resultBuffer.delete(0, len);
     }
     return len;
   }
   
   @Override
   public boolean markSupported() {
     return false;
   }
   
   @Override
   public void mark(int readAheadLimit) throws IOException {
     throw new IOException("mark() not supported");
   }
   
 }
