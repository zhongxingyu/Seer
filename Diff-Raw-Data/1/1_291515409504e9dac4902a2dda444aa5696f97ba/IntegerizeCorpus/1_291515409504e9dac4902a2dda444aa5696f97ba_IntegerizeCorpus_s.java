 /**
  * *******************************************************************************
  * Copyright (c) 2011, Monnet Project All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. * Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. * Neither the name of the Monnet Project nor the names
  * of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *******************************************************************************
  */
 package eu.monnetproject.translation.topics.experiments;
 
 import eu.monnetproject.translation.topics.PTBTokenizer;
 import eu.monnetproject.translation.topics.Tokenizer;
 import eu.monnetproject.translation.topics.WordMap;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
 import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
 
 /**
  * This function converts a corpus or words into a stream of integers, e.g.,  {@code
  *   doc_title_1:0 1 2 3 4
  *   doc_title_2:1 2 3 5
  * }
  * The map from words to integers is stored in a separate file
  *
  * @author John McCrae
  */
 public class IntegerizeCorpus {
 
     private static final Tokenizer tokenizer = new PTBTokenizer();
     private static final Pattern docStart = Pattern.compile("<doc.*title=\"(.*)\">.*");
     private static final Pattern docEnd = Pattern.compile("</doc>");
     /**
      * Normalize the token (i.e., make lowercase)
      *
      * @param token The token
      * @return
      */
     public static String normalize(String token) {
         return token.toLowerCase();
     }
 
     private static void fail(String message) {
         System.err.println(message);
         System.err.println("\nUsage:\n"
                 + "\tmvn exec:java -Dexec.mainClass=eu.monnetproject.translation.topics.experiments.IntegerizeCorpus -Dexec.args=\"[-s SAMPLING_RATE] corpus[.gz|.bz2] wordMap output[.gz|.bz2]\"\n"
                 + "\t\tcorpus and output may use suffix .gz or .bz2 to enable compression\n"
                 + "\t\twordMap is always appended to (if it exists)");
         System.exit(-1);
     }
 
     public static void main(String[] args) throws IOException {
         final ArrayList<String> argList = new ArrayList<String>(Arrays.asList(args));
         int sampleRate = 1;
         for (int i = 0; i < argList.size(); i++) {
             if (argList.get(i).equals("-s") && i + 1 < argList.size()) {
                 sampleRate = Integer.parseInt(argList.get(i + 1));
                 if (sampleRate <= 0) {
                     fail("Non-positive sample rate!");
                 }
                 argList.remove(i);
                 argList.remove(i);
                 i--;
             }
         }
 
         if (argList.size() != 3) {
             fail("Wrong number of arguments");
         }
         final File corpusFile = new File(argList.get(0));
         if (!corpusFile.exists() || !corpusFile.canRead()) {
             fail("Could not access corpus file");
         }
         final InputStream corpusIn;
         if (corpusFile.getName().endsWith(".gz")) {
             corpusIn = new GZIPInputStream(new FileInputStream(corpusFile));
         } else if (corpusFile.getName().endsWith(".bz2")) {
             corpusIn = new BZip2CompressorInputStream(new FileInputStream(corpusFile));
         } else {
             corpusIn = new FileInputStream(corpusFile);
         }
 
         final File wordMapFile = new File(argList.get(1));
         final WordMap wordMap;
         if (wordMapFile.exists() && wordMapFile.canRead()) {
             wordMap = WordMap.fromFile(wordMapFile);
         } else {
             wordMap = new WordMap();
         }
         if (wordMapFile.exists() && !wordMapFile.canWrite()) {
             fail("Cannot access word map file");
         }
 
         final File outFile = new File(argList.get(2));
         if (outFile.exists() && !outFile.canWrite()) {
             fail("Could not access out file");
         }
         final PrintWriter out;
         if (outFile.getName().endsWith(".gz")) {
             out = new PrintWriter(new GZIPOutputStream(new FileOutputStream(outFile)));
         } else if (corpusFile.getName().endsWith(".bz2")) {
             out = new PrintWriter(new BZip2CompressorOutputStream(new FileOutputStream(outFile)));
         } else {
             out = new PrintWriter(outFile);
         }
 
         integerize(corpusIn, wordMap, out, sampleRate);
 
         wordMap.write(wordMapFile);
     }
 
     public static void integerize(InputStream corpusIn, WordMap wordMap, PrintWriter out, int sampleRate) {
         final Scanner scanner = new Scanner(corpusIn).useDelimiter("\r?\n");
         boolean startOfLine = true;
         while (scanner.hasNext()) {
             final String line = scanner.next();
             final Matcher startMatcher = docStart.matcher(line);
             if (startMatcher.matches()) {
                 if (!startOfLine) {
                     out.println();
                 }
                 out.print(startMatcher.group(1));
                 out.print(":");
             } else if (docEnd.matcher(line).matches()) {
                 out.println();
                 startOfLine = true;
             } else {
                 final List<String> tokens = tokenizer.tokenize(line);
                 int s = 0;
                 for (String token : tokens) {
                     if (token.length() == 0 || s++ % sampleRate != 0) {
                         continue;
                     }
                     final int w = wordMap.offer(normalize(token));
                     out.print(w);
                     out.print(" ");
                     startOfLine = false;
                 }
             }
         }
     }
 }
