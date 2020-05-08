 /*
  * Copyright (c) 2010. Erwin van Eijk <erwin.vaneijk@gmail.com>
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package nl.minjus.nfi.dt.jhashtools.persistence;
 
 import nl.minjus.nfi.dt.jhashtools.Digest;
 import nl.minjus.nfi.dt.jhashtools.DigestResult;
 import nl.minjus.nfi.dt.jhashtools.DirHasherResult;
 import nl.minjus.nfi.dt.jhashtools.exceptions.PersistenceException;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import java.io.*;
 import java.util.Calendar;
 import java.util.Map;
 
 /**
  * This class supports the Old Style hashes.txt files that are now generated
  * using JSON. The old format should be abolished, but backwards compatibility
  * is a must.
  *
  * @author Erwin van Eijk
  */
 public class OldStylePersistenceProvider implements PersistenceProvider {
 
     @Override
     public void persist(OutputStream out, Object obj) throws PersistenceException {
         if (obj != null && obj instanceof DirHasherResult) {
             DirHasherResult directoryHasherResult = (DirHasherResult) obj;
             PrintStream stream = new PrintStream(out);
            stream.println("Generated with " + directoryHasherResult.getConstructionInfo().toString() + " " + Calendar.getInstance().getTime().toString());
             for (Map.Entry<File, DigestResult> entry: directoryHasherResult) {
                 stream.println(entry.getKey().toString());
                 for (Digest d: entry.getValue()) {
                     stream.println("\t" + d.prettyPrint(":\t"));
                 }
             }
         } else {
             if (obj != null) {
                 throw new PersistenceException("There is no persistence method defined for class " + obj.getClass().toString());
             } else {
                 throw new PersistenceException("There is no persistence method defined for null class");
             }
         }
     }
 
     @Override
     public <T> T load(Reader reader, Class<T> clazz) throws PersistenceException {
         if (clazz.equals(DirHasherResult.class)) {
             DirHasherResult directoryHasherResult = new DirHasherResult();
             try {
                 LineNumberReader lineNumberReader = new LineNumberReader(reader);
                @SuppressWarnings({"UnusedAssignment"}) String line = lineNumberReader.readLine();
                // FIXME
                // We should check for the proper format of the first line.
                 try {
                     String currentFileName = "";
                     DigestResult result = new DigestResult();
 
                     while (true) {
                         line = lineNumberReader.readLine();
                         if (line == null) {
                             if (!currentFileName.isEmpty()) {
                                 directoryHasherResult.put(currentFileName, result);
                             }
                             break;
                         }
 
                         if (line.charAt(0) != '\t') {
                             if (!currentFileName.isEmpty()) {
                                 directoryHasherResult.put(currentFileName, result);
                             }
                             currentFileName = line;
                             result = new DigestResult();
                         } else {
                             try {
                                 OldStyleHashesParser parser = createParser(line);
                                 Digest theDigest = parser.digest();
                                 result.add(theDigest);
                             } catch (RecognitionException ex) {
                                 // FIXME
                                 // Output a proper error message and continue.
                             }
                         }
                     }
                 } catch (EOFException ex) {
                     // pass, was expected.
                 }
                 return (T) directoryHasherResult;
             } catch (IOException ex) {
                 throw new PersistenceException(ex);
             }
         } else {
             throw new PersistenceException("There is no persistence method defined for class" + clazz.toString());
         }
     }
 
     private OldStyleHashesParser createParser(String testString) throws IOException {
         CharStream stream = new ANTLRStringStream(testString);
         OldStyleHashesLexer lexer = new OldStyleHashesLexer(stream);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         return new OldStyleHashesParser(tokens);
     }
 }
