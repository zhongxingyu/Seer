 //  Copyright © 2012 bjarneh
 //
 //  This program is free software: you can redistribute it and/or modify
 //  it under the terms of the GNU General Public License as published by
 //  the Free Software Foundation, either version 3 of the License, or
 //  (at your option) any later version.
 //
 //  This program is distributed in the hope that it will be useful,
 //  but WITHOUT ANY WARRANTY; without even the implied warranty of
 //  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //  GNU General Public License for more details.
 //
 //  You should have received a copy of the GNU General Public License
 //  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.github.bjarneh.utilz;
 
 // stdlib
 import java.net.URL;
 import java.io.File;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileOutputStream;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipEntry;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 
 /**
  * Common io functions.
  *
  * <pre>
  *
  *  // Typical use:
  *
  *  byte[] bytes;
  *  
  *  // fetch file as raw byte array
  *  bytes = io.raw("somefile.txt");
  *
  *  // fetch URL as raw byte array
  *  bytes = io.wget("http://example.com/index.html");
  *
  *  // fetch stdin as raw byte array
  *  bytes = io.raw(System.in);
  *
  *  // pipe an input stream to an output stream (stdin to stdout)
  *  io.pipe(System.in, System.out);
  *
  *  // file slurp
  *  String content = new String(io.raw("somefile.txt"));
  *
  *  // file slurp with specified encoding
  *  String content = new String(io.raw(System.in), "ISO-8859-1");
  *
  * </pre>
  * <b>note</b>: There is a lot of comments here since this is
  * meant to be a library with some documentation, i.e. make 
  * sure you turn on your comment folding before you start 
  * reading this file.
  *
  * @version 1.0
  * @author  bjarneh@ifi.uio.no
  */
 
 public class io {
 
     // we only have functions
     private io(){}
 
     public final static int DEFAULT_BUFFER_SIZE = 4096;
 
 
     ////////////////////
     // raw file slurp //
     ////////////////////
 
     /**
      * In memory raw file slurp.
      *
      * It should be noted that I'm just guessing that this is the
      * best way to do things based on votes from stackoverflow,
      * which could very well be wrong; but if it is I'm not alone :-)
      *
      * stackoverflow: 326390
      *
      * @param stream to slurp and return as raw bytes
      * @return a byte array containing the bytes of the stream
      */
     public static byte[] raw(FileInputStream stream) throws IOException {
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb =
                 fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             if(bb.hasArray()){
                 return bb.array();
             }else{
                 byte[] b = new byte[(int)fc.size()];
                 bb.get(b);
                 return b;
             }
         }
         finally {
             stream.close();
         }
     }
 
     /**
      * 
      * Alias for io.raw(new File(fname)).
      *
      * @param fname file-name to slurp and return as raw bytes
      * @return a byte array containing the bytes of the file 
      */
     public static byte[] raw(String fname) throws IOException {
         return raw(new File(fname));
     }
 
     /**
      * Alias for io.raw(new FileInputStream(file)).
      *
      * @param file file object to slurp and return as raw bytes
      * @return a byte array containing the bytes of the file 
      */
     public static byte[] raw(File file) throws IOException {
         return raw(new FileInputStream(file));
     }
 
     /**
      * Use io.pipe to write to an ByteArrayOutputStream, and fetch array.
      *
      * @param input the stream we want to return as raw bytes
      * @return a byte array containing the bytes of the input stream
      */
     public static byte[] raw(InputStream input) throws IOException {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         pipe(input, output);
         return output.toByteArray();
     }
 
     ////////////////////////////////////////////
     // piping input streams to output streams //
     ////////////////////////////////////////////
 
     /**
      * Write bytes from an input stream to an output stream.
      *
      * @param fi  where bytes are read
      * @param fo  where bytes are written
      * @param bufferSize is the size of the buffer used
      */
     public static void pipe(InputStream fi, OutputStream fo, int bufferSize)
         throws IOException
     {
         int got = -1;
         byte[] b = new byte[bufferSize];
 
         for( got = fi.read(b); got > 0; got = fi.read(b) ){
             fo.write(b, 0, got);
         }
     }
 
     /**
      * Alias for io.pipe(fi, fo, io.DEFAULT_BUFFER_SIZE).
      *
      * @param fi  where bytes are read
      * @param fo  where bytes are written
      */
     public static void pipe(InputStream fi, OutputStream fo)
         throws IOException
     {
         pipe(fi, fo, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Alias for io.pipe(new ByteArrayInputStream(b), fo, io.DEFAULT_BUFFER_SIZE).
      *
      * @param b  the bytes
      * @param fo  where bytes are written
      */
     public static void pipe(byte[] b, OutputStream fo)
         throws IOException
     {
         pipe(new ByteArrayInputStream(b), fo, DEFAULT_BUFFER_SIZE);
     }
 
 
     /**
      * Fetch the bytes of a String representing an URL.
      * @param url represented as a String
      * @return an array of bytes fetched from URL
      */
     public static byte[] wget(String url)
         throws IOException
     {
         return wget(new URL(url));
     }
 
     /**
      * Fetch the bytes of an URL.
      * @param url to read
      * @return an array of bytes fetched from URL
      */
     public static byte[] wget(URL url)
         throws IOException
     {
         return raw(url.openStream());
     }
 
     /**
      * Write the bytes of an URL to an OutputStream.
      * @param url to read from
      * @param fo output stream to write bytes to
      * @param bufferSize size of buffer used by io.pipe
      */
     public static void wget(URL url, OutputStream fo, int bufferSize)
         throws IOException
     {
         pipe(url.openStream(), fo, bufferSize);
     }
 
     /**
      * Alias for wget(url, fo, io.DEFAULT_BUFFER_SIZE).
      * @param url to read from
      * @param fo output stream to write bytes to
      */
     public static void wget(URL url, OutputStream fo)
         throws IOException
     {
         pipe(url.openStream(), fo, DEFAULT_BUFFER_SIZE);
     }
 
     /**
      * Unzip files without the hassle.
      * @param file to be unzipped
      */
     public static void unzip( File file ) throws Exception {
 
         ZipFile zipFile = new ZipFile( file );
 
        File parentFile, tmpFile;
         InputStream stream;
         FileOutputStream output;
 
         String parent = file.getParent();
 
         if( parent != null ){
             parentFile = new File(parent);
         }else{
             parentFile = new File(".");
         }
 
         for(ZipEntry e : handy.toList( zipFile.entries() )){
             if( e.isDirectory() ){
                 new File(parentFile, e.getName()).mkdirs();
             }else{
                stream  = zipFile.getInputStream(e);
                tmpFile = new File(parentFile, e.getName());
                output  = new FileOutputStream( tmpFile );
                 pipe( stream, output );
             }
         }
     }
 
 }
 
