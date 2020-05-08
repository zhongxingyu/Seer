 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package DiskBenchmark;
 
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 /**
  *
  * @author KamalNath_NG
  */
 public class junk {
     
     private static final int BUFFER = 8192;
        private static void nioBufferCopy(File source, File target) {
         FileChannel in = null;
         FileChannel out = null;
 
         try {
             in = new FileInputStream(source).getChannel();
             out = new FileOutputStream(target).getChannel();
 
             ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER);
             while (in.read(buffer) != -1) {
                 buffer.flip();
 
                 while (buffer.hasRemaining()) {
                     out.write(buffer);
                 }
 
                 buffer.clear();
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             close(in);
             close(out);
         }
     }
 
     private static void customBufferBufferedReaderCopy(File source, File target) {
         Reader fin = null;
         Writer fout = null;
         try {
             fin = new BufferedReader(new FileReader(source));
             fout = new BufferedWriter(new FileWriter(target));
 
             char[] buf = new char[2048];
 
             int i;
             while ((i = fin.read(buf)) != -1) {
                 fout.write(buf, 0, i);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
     
     private static void close(Closeable closable) {
         if (closable != null) {
             try {
                 closable.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     /*
      * /*
  * package com.wicht;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.io.Reader;
 import java.io.Writer;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 import bb.science.FormatUtil;
 import bb.util.Benchmark;
 import bb.util.Benchmark.Params;
 
 public final class FileCopyBenchmark {
     private static final Params params = new Params();
 
     static {
         params.setConsoleFeedback(false);
     }
 
     public static void main(String[] args) {
         BufferedOutputStream console = null;
         try {
             console = new BufferedOutputStream(new FileOutputStream("/home/wichtounet/Desktop/console.log"));
 
             System.setOut(new PrintStream(console));
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 
         System.out.println("Start Text Benchmark");
         //benchText();
 
         System.out.println("Start Binary Benchmark");
         benchBinary();
 
         close(console);
     }
 
     private static void benchBinary() {
         File littleFileIn = new File("/home/wichtounet/Desktop/little-binary");
         File littleFileOut = new File("/media/Data 1/tmp/little-binary");
 
         System.out.println("Start benchmark with little file (5 KB)");
 
         //bench(littleFileIn, littleFileOut, 1, false);
 
         File middleFileIn = new File("/home/wichtounet/Desktop/medium-binary");
         File middleFileOut = new File("/media/Data 1/tmp/medium-binary");
 
         System.out.println("Start benchmark with medium file (50 KB)");
 
         //bench(middleFileIn, middleFileOut, 2, false);
 
         File bigFileIn = new File("/home/wichtounet/Desktop/big-binary");
         File bigFileOut = new File("/media/Data 1/tmp/big-binary");
 
         System.out.println("Start benchmark with big file (5 MB)");
 
         //bench(bigFileIn, bigFileOut, 3, false);
 
         File fatFileIn = new File("/home/wichtounet/Desktop/fat-binary");
         File fatFileOut = new File("/media/Data 1/tmp/fat-binary");
 
         System.out.println("Start benchmark with fat file (50 MB)");
 
         //bench(fatFileIn, fatFileOut, 4, false);
 
         File enormousFileIn = new File("/home/wichtounet/Desktop/enormous-binary");
         File enormousFileOut = new File("/media/Data 1/tmp/enormous-binary");
 
         System.out.println("Start benchmark with enormous file (1.3 GB)");
 
         bench(enormousFileIn, enormousFileOut, 5, false);
     }
 
     private static void benchText() {
         File littleFileIn = new File("/home/wichtounet/Desktop/little-text");
         File littleFileOut = new File("/media/Data 1/tmp/little-text");
 
         System.out.println("Start benchmark with little file (5 KB)");
 
         bench(littleFileIn, littleFileOut, 1, true);
 
         File middleFileIn = new File("/home/wichtounet/Desktop/medium-text");
         File middleFileOut = new File("/media/Data 1/tmp/medium-text");
 
         System.out.println("Start benchmark with medium file (50 KB)");
 
         bench(middleFileIn, middleFileOut, 2, true);
 
         File bigFileIn = new File("/home/wichtounet/Desktop/big-text");
         File bigFileOut = new File("/media/Data 1/tmp/big-text");
 
         System.out.println("Start benchmark with big file (5 MB)");
 
         bench(bigFileIn, bigFileOut, 3, true);
 
         File fatFileIn = new File("/home/wichtounet/Desktop/fat-text");
         File fatFileOut = new File("/media/Data 1/tmp/fat-text");
 
         System.out.println("Start benchmark with fat file (50 MB)");
 
         bench(fatFileIn, fatFileOut, 4, true);
     }
 
     private static void bench(final File in, final File out, int size, boolean text) {
         if (size < 3) {
             Runnable naiveStreamsRunnable = new Runnable() {
                 @Override
                 public void run() {
                     naiveStreamsCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark naiveStreamsBenchmark = new Benchmark(naiveStreamsRunnable, params);
 
             System.out.println("Naive Streams Copy results : " + FormatUtil.toEngineeringTime(naiveStreamsBenchmark.getMean(), 3));
         }
 
         if (size < 4 && text) {
             Runnable naiveReaderRunnable = new Runnable() {
                 @Override
                 public void run() {
                     naiveReaderCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark naiveReaderBenchmark = new Benchmark(naiveReaderRunnable, params);
 
             System.out.println("Naive Reader Copy results : " + FormatUtil.toEngineeringTime(naiveReaderBenchmark.getMean(), 3));
         }
 
         if(size < 5){
             Runnable bufferedStreamsRunnable = new Runnable() {
                 @Override
                 public void run() {
                     bufferedStreamsCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark bufferedStreamsBenchmark = new Benchmark(bufferedStreamsRunnable, params);
 
             System.out.println("Buffered Streams Copy results : " + FormatUtil.toEngineeringTime(bufferedStreamsBenchmark.getMean(), 3));
         }
 
         if (size < 5 && text) {
             Runnable bufferedReadersRunnable = new Runnable() {
                 @Override
                 public void run() {
                     bufferedReaderCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark bufferedReadersBenchmark = new Benchmark(bufferedReadersRunnable, params);
 
             System.out.println("Buffered Readers Copy results : " + FormatUtil.toEngineeringTime(bufferedReadersBenchmark.getMean(), 3));
         }
 
         Runnable customBufferStreamRunnable = new Runnable() {
             @Override
             public void run() {
                 customBufferStreamCopy(in, out);
                 out.delete();
             }
         };
 
         Benchmark customBufferStreamBenchmark = new Benchmark(customBufferStreamRunnable, params);
 
         System.out.println("Custom Buffer Streams Copy results : " + FormatUtil.toEngineeringTime(customBufferStreamBenchmark.getMean(), 3));
 
         if (text) {
             Runnable customBufferReaderRunnable = new Runnable() {
                 @Override
                 public void run() {
                     customBufferReaderCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark customBufferReaderBenchmark = new Benchmark(customBufferReaderRunnable, params);
 
             System.out.println("Custom Buffer Readers Copy results : " + FormatUtil.toEngineeringTime(customBufferReaderBenchmark.getMean(), 3));
         }
 
         Runnable customBufferBufferedStreamRunnable = new Runnable() {
             @Override
             public void run() {
                 customBufferBufferedStreamCopy(in, out);
                 out.delete();
             }
         };
 
         Benchmark customBufferBufferedStreamBenchmark = new Benchmark(customBufferBufferedStreamRunnable, params);
 
         System.out.println("Custom Buffer Buffered Streams Copy results : " + FormatUtil.toEngineeringTime(customBufferBufferedStreamBenchmark.getMean(), 3));
 
         if (text) {
             Runnable customBufferBufferedReaderRunnable = new Runnable() {
                 @Override
                 public void run() {
                     customBufferBufferedReaderCopy(in, out);
                     out.delete();
                 }
             };
 
             Benchmark customBufferBufferedReaderBenchmark = new Benchmark(customBufferBufferedReaderRunnable, params);
 
             System.out.println("Custom Buffer Buffered Readers Copy results : " + FormatUtil.toEngineeringTime(customBufferBufferedReaderBenchmark.getMean(), 3));
         }
 
         Runnable nioBufferRunnable = new Runnable() {
             @Override
             public void run() {
                 nioBufferCopy(in, out);
                 out.delete();
             }
         };
 
         Benchmark nioBufferBenchmark = new Benchmark(nioBufferRunnable, params);
 
         System.out.println("NIO Buffer Copy results : " + FormatUtil.toEngineeringTime(nioBufferBenchmark.getMean(), 3));
 
         Runnable nioRunnable = new Runnable() {
             @Override
             public void run() {
                 nioTransferCopy(in, out);
                 out.delete();
             }
         };
 
         Benchmark nioBenchmark = new Benchmark(nioRunnable, params);
 
         System.out.println("NIO Transfer Copy results : " + FormatUtil.toEngineeringTime(nioBenchmark.getMean(), 3));
     }
 
     private static void naiveStreamsCopy(File source, File target) {
         InputStream fin = null;
         OutputStream fout = null;
         try {
             fin = new FileInputStream(source);
             fout = new FileOutputStream(target);
 
             int c;
             while ((c = fin.read()) != -1) {
                 fout.write(c);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void naiveReaderCopy(File source, File target) {
         Reader fin = null;
         Writer fout = null;
         try {
             fin = new FileReader(source);
             fout = new FileWriter(target);
 
             int c;
             while ((c = fin.read()) != -1) {
                 fout.write(c);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void bufferedStreamsCopy(File source, File target) {
         InputStream fin = null;
         OutputStream fout = null;
         try {
             fin = new BufferedInputStream(new FileInputStream(source));
             fout = new BufferedOutputStream(new FileOutputStream(target));
 
             int data;
             while ((data = fin.read()) != -1) {
                 fout.write(data);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void bufferedReaderCopy(File source, File target) {
         Reader fin = null;
         Writer fout = null;
         try {
             fin = new BufferedReader(new FileReader(source));
             fout = new BufferedWriter(new FileWriter(target));
 
             int c;
             while ((c = fin.read()) != -1) {
                 fout.write(c);
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void customBufferStreamCopy(File source, File target) {
         InputStream fis = null;
         OutputStream fos = null;
         try {
             fis = new FileInputStream(source);
             fos = new FileOutputStream(target);
 
             byte[] buf = new byte[4096];
 
             int i;
             while ((i = fis.read(buf)) != -1) {
                 fos.write(buf, 0, i);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fis);
             close(fos);
         }
     }
 
     private static void customBufferReaderCopy(File source, File target) {
         Reader fin = null;
         Writer fout = null;
         try {
             fin = new FileReader(source);
             fout = new FileWriter(target);
 
             char[] buf = new char[2048];
 
             int i;
             while ((i = fin.read(buf)) != -1) {
                 fout.write(buf, 0, i);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void customBufferBufferedStreamCopy(File source, File target) {
         InputStream fis = null;
         OutputStream fos = null;
         try {
             fis = new BufferedInputStream(new FileInputStream(source));
             fos = new BufferedOutputStream(new FileOutputStream(target));
 
             byte[] buf = new byte[4096];
 
             int i;
             while ((i = fis.read(buf)) != -1) {
                 fos.write(buf, 0, i);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fis);
             close(fos);
         }
     }
 
     private static void customBufferBufferedReaderCopy(File source, File target) {
         Reader fin = null;
         Writer fout = null;
         try {
             fin = new BufferedReader(new FileReader(source));
             fout = new BufferedWriter(new FileWriter(target));
 
             char[] buf = new char[2048];
 
             int i;
             while ((i = fin.read(buf)) != -1) {
                 fout.write(buf, 0, i);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         } finally {
             close(fin);
             close(fout);
         }
     }
 
     private static void nioBufferCopy(File source, File target) {
         FileChannel in = null;
         FileChannel out = null;
 
         FileInputStream inStream = null;
         FileOutputStream outStream = null;
 
         try {
             inStream = new FileInputStream(source);
             outStream = new FileOutputStream(target);
 
             in = inStream.getChannel();
             out = outStream.getChannel();
 
             ByteBuffer buffer = ByteBuffer.allocate(4096);
             while (in.read(buffer) != -1) {
                 buffer.flip();
                 out.write(buffer);
                 buffer.clear();
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             close(inStream);
             close(in);
             close(outStream);
             close(out);
         }
     }
 
     private static void nioTransferCopy(File source, File target) {
         FileChannel in = null;
         FileChannel out = null;
 
         FileInputStream inStream = null;
         FileOutputStream outStream = null;
 
         try {
             inStream = new FileInputStream(source);
             outStream = new FileOutputStream(target);
 
             in = inStream.getChannel();
             out = outStream.getChannel();
 
             in.transferTo(0, in.size(), out);
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             close(inStream);
             close(in);
             close(outStream);
             close(out);
         }
     }
 
     private static void close(Closeable closable) {
         if (closable != null) {
             try {
                 closable.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
      */
 }
