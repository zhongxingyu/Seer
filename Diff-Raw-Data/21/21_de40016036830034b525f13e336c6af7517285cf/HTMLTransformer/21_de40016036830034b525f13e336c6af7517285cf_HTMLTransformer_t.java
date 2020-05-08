 /*
  * WebSphinx web-crawling toolkit
  *
  * Copyright (c) 1998-2002 Carnegie Mellon University.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY CARNEGIE MELLON UNIVERSITY ``AS IS'' AND
  * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
  * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package websphinx;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.io.RandomAccessFile;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 //#ifdef JDK1.1 
 import java.io.Writer;
 import java.io.OutputStreamWriter;
 //#endif JDK1.1
 /*#ifdef JDK1.0
 import java.io.PrintStream;
 #endif JDK1.0*/
 
 public class HTMLTransformer {
 
 //#ifdef JDK1.1
     private OutputStream stream;      // output stream for binary content
     private Writer writer;            // output stream for HTML content
 //#endif JDK1.1
 /*#ifdef JDK1.0
     private PrintStream stream;    // output stream
 #endif JDK1.0*/
     private boolean openedStream = false;  
             // we opened the stream, so we'd better close it
 
     private RandomAccessFile readwrite; // output file
 
     private HTMLTransformer next;      // next HTMLTransformer in the filter chain
     private HTMLTransformer head;      // head of filter chain
     private HTMLTransformer tail;      // tail of filter chain
 
     // these fields are only valid on the tail element in the filter
     // chain
     private String content;         // content of page being printed
     private int emitStart, emitEnd; // start and end of pending region
                                     //   (the last region in the page which
                                     //    has been emit()ed but not actually
                                     //    written)
     private int transformEnd;       // end of region being transformed
     
    protected String encoding;        // encoding of strings, e.g. utf-8
 
     /**
      * Make an HTMLTransformer that writes pages to a
      * stream.
      * @param out Stream to receive HTML output
      * @param encoding encoding of strings, e.g. utf-8
      */
     public HTMLTransformer (OutputStream out, String encoding) {
         head = tail = this;
         next = null;
         this.encoding = encoding;
         setOutput (out);
     }
 
     /**
      * Make an HTMLTransformer that writes pages to a
      * file.
      * @param filename Name of file to receive HTML output
      * @param encoding encoding of strings, e.g. utf-8
      * @exception IOException if file cannot be opened
      */
     public HTMLTransformer (String filename, String encoding) throws IOException {
         head = tail = this;
         next = null;        
         this.encoding = encoding;
         openFile (filename, false);
     }
 
 
     /**
      * Make an HTMLTransformer that writes pages to a
      * file.
      * @param filename Name of file to receive HTML output
      * @param seekable True if file should be opened for random access
      * @param encoding encoding of strings, e.g. utf-8
      */
     public HTMLTransformer (String filename, boolean seekable, String encoding) throws IOException {
         head = tail = this;
         next = null;        
         this.encoding = encoding;
         openFile (filename, seekable);
     }
 
 
     /**
      * Make an HTMLTransformer that writes pages to a
      * downstream HTMLTransformer.  Use this constructor
      * to chain together several HTMLTransformers.
      * @param next HTMLTransformer to receive HTML output
      */
     public HTMLTransformer (HTMLTransformer next) {
         this.next = next;
         tail = next != null ? next.tail : this;
         for (HTMLTransformer u = this; u != null; u = u.next)
             u.head = this;
     }
 
     private void openFile (String filename, boolean seekable) throws IOException {
         File file = new File (filename);
 
         // open a stream first, to truncate the file to 0
         OutputStream out = Access.getAccess ().writeFile (file, false);
         
         if (!seekable)
             setOutput (out);
         else {
             out.close ();
             RandomAccessFile raf = Access.getAccess ().readWriteFile (file);
             setRandomAccessFile (raf);
         }
             
         openedStream = true;
     }
 
 //#ifdef JDK1.1 
     public void setOutput (OutputStream out) {
         if (next == null) {
             stream = out;
             try {
                 if (encoding == null) {
                     encoding = "utf-8";
                 }
                 writer = new OutputStreamWriter (out, encoding);
             } catch (UnsupportedEncodingException e) {
                 throw new RuntimeException("invalid encoding: " + encoding + ": " 
                         + e.getMessage(), e);
             }
         } else
             next.setOutput (out);
     }
 
 //     public void setOutput (Writer out) {
 //         if (next == null)
 //             stream = out;
 //         else
 //             next.setOutput (out);
 //     }
 
     public OutputStream getOutputStream () {
         return tail.stream;
     }
 
     public Writer getOutputWriter () {
         return tail.writer;
     }
 //#endif JDK1.1
 
 /*#ifdef JDK1.0
     public void setOutput (OutputStream out) {
         if (next == null)
             stream = new PrintStream (out);
         else
             next.setOutput (out);
     }
 
     public OutputStream getOutput () {
         return tail.stream;
     }
 #endif JDK1.0*/
 
     public void setRandomAccessFile (RandomAccessFile raf) {
         if (next == null)
             readwrite = raf;
         else
             next.setRandomAccessFile (raf);
     }
     
     public RandomAccessFile getRandomAccessFile () {
         return tail.readwrite;
     }
 
     /**
      * Writes a literal string through the HTML transformer
      * (without parsing it or transforming it).
      * @param string String to write
      */
     public synchronized void write (String string) throws IOException {
         if (next == null)
             emit (string);
         else
             next.write (string);
     }
 
     /**
      * Writes a chunk of HTML through the HTML transformer.
      * @param region Region to write
      */
     public synchronized void write (Region region) throws IOException {
         if (next == null) {
             emitPendingRegion ();
             
             String oldContent = content;
             int oldEmitStart = emitStart;
             int oldEmitEnd = emitEnd;
             int oldTransformEnd = transformEnd;
             
             content = region.getSource().getContent ();
             emitStart = emitEnd = region.getStart ();
             transformEnd = region.getEnd ();
 
             processElementsInRegion (region.getRootElement(), 
                                      region.getStart(),
                                      region.getEnd());
 
             emitPendingRegion ();
 
             content = oldContent;
             emitStart = oldEmitStart;
             emitEnd = oldEmitEnd;
             transformEnd = oldTransformEnd;
         }
         else
             next.write (region);
     }
 
     /**
      * Writes a page through the HTML transformer.
      * @param page Page to write
      */
     public synchronized void writePage (Page page) throws IOException {
         if (next == null) {
             if (page.isHTML ())
                 write (page);
             else {
                 System.err.println ("binary write of " + page.getURL ());
                 writeStream (page.getContentBytes (),
                              0, page.getLength ());
             }
         }
         else
             next.writePage (page);
     }
 
     /**
      * Flushes transformer to its destination stream.
      * Empties any buffers in the transformer chain.
      */
     public synchronized void flush () throws IOException {
         if (next == null) {
             emitPendingRegion ();
             if (stream != null)
                 stream.flush ();
             if (writer != null)
                 writer.flush ();
         }
         else
             next.flush ();
     }
 
     /**
      * Close the transformer.  Flushes all buffered data
      * to disk by calling flush().  This call may be
      * time-consuming!  Don't use the transformer again after
      * closing it.
      * @exception IOException if an I/O error occurs
      */
     public synchronized void close () throws IOException {
         flush ();
         if (next == null) {
             if (openedStream) {
                 if (stream != null)
                     stream.close ();
                 if (readwrite != null)
                     readwrite.close ();
             }
         }
         else
             next.close ();
     }
     
     /**
      * Finalizes the transformer (calling close()).
      */
     protected void finalize() throws Throwable {
         close ();
     }
 
     /**
      * Get the file pointer.
      * @return current file pointer
      * @exception IOException if this transformer not opened for random access
      */
     public long getFilePointer () throws IOException {
         if (readwrite == null)
             throw new IOException ("HTMLTransformer not opened for random access");
         return readwrite.getFilePointer ();
     }
 
     /**
      * Seek to a file position.
      * @param pos file position to seek
      * @exception IOException if this transformer not opened for random access
      */
     public void seek (long pos) throws IOException {
         if (readwrite == null)
             throw new IOException ("HTMLTransformer not opened for random access");
         readwrite.seek (pos);
     }
 
     /**
      * Transform an element by passing it through the entire
      * filter chain.
      * @param elem Element to be transformed
      */
     protected void transformElement (Element elem) throws IOException {
         head.handleElement (elem);
     }
 
     /**
      * Transform the contents of an element.  Passes
      * the child elements through the filter chain
      * and emits the text between them.
      * @param elem Element whose contents should be transformed
      */
     protected void transformContents (Element elem) throws IOException {
         Tag startTag = elem.getStartTag ();
         Tag endTag = elem.getEndTag ();
 
         tail.processElementsInRegion (elem.getChild(),
                            startTag.getEnd(),
                            endTag != null ? endTag.getStart() : elem.getEnd());
     }
 
     /**
      * Handle the transformation of an HTML element.
      * Override this method to modify the HTML as it is
      * written.
      * @param elem Element to transform
      */
     protected void handleElement (Element elem) throws IOException {
         if (next == null) {
             Tag startTag = elem.getStartTag ();
             Tag endTag = elem.getEndTag ();
             
             emit (startTag);
             transformContents (elem);
             if (endTag != null)
                 emit (endTag);
         }
         else
             next.handleElement (elem);
     }
 
     /**
      * Emit a region on the transformer chain's final output.
      * (The region isn't passed through the chain.)
      * @param r Region to emit
      */
     protected void emit (Region r) throws IOException {
         tail.emitInternal (r.getSource().getContent(), r.getStart(), r.getEnd ());
     }
 
     /**
      * Emit a string on the transformer chain's final output.
      * @param string String to emit
      */
     protected void emit (String string) throws IOException {
         tail.emitInternal (string, 0, string.length());
     }
 
     private void processElementsInRegion (Element elem, int start, int end) throws IOException {
         if (this != tail)
             throw new RuntimeException ("processElementsInRegion not called on tail");
             
         int p = start;
         
         if (elem != null && elem.getSource().getContent() == content)
             end = Math.min (end, transformEnd);
 
         while (elem != null && elem.getStartTag().getEnd() <= end) {
             emitInternal (content, p, elem.getStart());
             transformElement (elem);
             p = elem.getEnd ();        
             elem = elem.getNext ();
         }
         emitInternal (content, Math.min (p, end), end);
     }
 
     private void emitInternal (String str, int start, int end) throws IOException {
         if (this != tail)
             throw new RuntimeException ("emitInternal not called on tail");
             
         if (str == content) {
             start = Math.min (start, transformEnd);
             end = Math.min (end, transformEnd);
                 
             if (start == emitEnd)
                 emitEnd = end; // just extend the pending emit region
             else {
                 emitPendingRegion ();
                 emitStart = start;
                 emitEnd = end;
             }
         }
         else {
             emitPendingRegion ();
             writeStream (str.substring (start, end));
         }
     }
 
     private void emitPendingRegion () throws IOException {
         if (this != tail)
             throw new RuntimeException ("emitPendingRegion not called on tail");
             
         if (emitStart != emitEnd) {
             writeStream (content.substring (emitStart, emitEnd));
             emitStart = emitEnd;
         }
     }
     
     private void writeStream (String s) throws IOException {
         if (writer != null) {
             //#ifdef JDK1.1 
             writer.write (s);
             //#endif JDK1.1
             /*#ifdef JDK1.0
             stream.print (s);
             #endif JDK1.0*/
         }
         else
            readwrite.write(s.getBytes(this.encoding));
     }
 
     private void writeStream (byte[] buf, int offset, int len) throws IOException {
         if (stream != null) {
             //#ifdef JDK1.1 
             stream.write (buf, offset, len);
             //#endif JDK1.1
             /*#ifdef JDK1.0
             stream.write (buf, offset, len);
             #endif JDK1.0*/
         }
         else
             readwrite.write (buf, offset, len);
     }
 
     
     /*
      * Testing
      *
     public static void main (String[] args) throws Exception {
         Link link = new Link (args[0]);
         Page page = new Page (link);
 
         OutputStream out = (args.length >= 2)
             ? (OutputStream)new java.io.FileOutputStream (args[1])
             : (OutputStream)System.out;
         HTMLTransformer unparser = new TestTransformer (out);
 
         int len = page.getLength();
         unparser.write (new Region (page, 0, 3*len/4));
             
         unparser.close ();
     }
      */
 }
 
     /*
      * Testing
      *
 class TestTransformer extends HTMLTransformer {
     public TestTransformer (OutputStream out) {
         super (out);
     }
     
     protected void handleElement (Element elem) throws IOException {
         System.out.println ("handling <" + elem.getTagName() + ">");
         super.handleElement (elem);
     }
 }
     */
