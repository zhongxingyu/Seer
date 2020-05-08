/*
  * Main.java
  *
 * Created on 27. März 2011
  *
  */
 
 package main;
 
 import java.io.IOException;
 import java.io.File;
 import java.util.BitSet;
 
 /**
  * @author Raphael Brandis
  *
  */
 public class Main {
     public static void main(String[] args) throws IOException, ClassNotFoundException {
         if (args.length < 3) {
             printUsage();
         } else {
             long start = System.currentTimeMillis();
             
             if (args[0].equals("compress")) {
                 Encoder encoder = new Encoder();
                 encoder.compressFile(args[1], args[2]);
             } else if (args[0].equals("decompress")) {
                 Decoder decoder = new Decoder();
                 decoder.decompressFile(args[1], args[2]);
             } else {
                 System.err.println("Unrecognized option: " + args[0]);
                 printUsage();
             }
             
             long end = System.currentTimeMillis();
             File in = new File(args[1]);
             File out = new File(args[2]);
             long inBytes = in.length();
             long outBytes = out.length();
             
             System.out.println("Process took " + (end - start) + "ms to complete.");
             System.out.println("Compression ratio: " + ((double) outBytes / (double) inBytes));
         }
         /*
          Encoder myEncoder = new Encoder();
         myEncoder.compressFile("C:\\Dokumente und Einstellungen\\rapsoel\\Eigene Dateien\\einhard\\informatik\\adaptiveHuffmanCoding\\dist\\in10m.txt",
                 "C:\\Dokumente und Einstellungen\\rapsoel\\Eigene Dateien\\einhard\\informatik\\adaptiveHuffmanCoding\\dist\\out10m.bin");
          
         Decoder myDecoder = new Decoder();
         myDecoder.decompressFile("C:\\Dokumente und Einstellungen\\rapsoel\\Eigene Dateien\\einhard\\informatik\\adaptiveHuffmanCoding\\dist\\out10m.bin",
                 "C:\\Dokumente und Einstellungen\\rapsoel\\Eigene Dateien\\einhard\\informatik\\adaptiveHuffmanCoding\\dist\\out10m.txt");
          */
     }
     
     private static void printUsage() {
         System.out.println("Adaptive Huffman compressor usage:");
         System.out.println("To compress:");
         System.out.println("./java -jar adaptiveHuffmanCoding.jar compress input.txt compressed.bin");
         System.out.println("To decompress:");
         System.out.println("./java -jar adaptiveHuffmanCoding.jar decompress compressed.bin output.txt");
     }
 }
