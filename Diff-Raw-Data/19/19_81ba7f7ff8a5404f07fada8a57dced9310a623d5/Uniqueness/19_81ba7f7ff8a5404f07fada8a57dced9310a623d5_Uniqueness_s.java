 /*
  * Copyright 2011 DELVING BV
  *
  *  Licensed under the EUPL, Version 1.0 or? as soon they
  *  will be approved by the European Commission - subsequent
  *  versions of the EUPL (the "Licence");
  *  you may not use this work except in compliance with the
  *  Licence.
  *  You may obtain a copy of the Licence at:
  *
  *  http://ec.europa.eu/idabc/eupl
  *
  *  Unless required by applicable law or agreed to in
  *  writing, software distributed under the Licence is
  *  distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  *  express or implied.
  *  See the Licence for the specific language governing
  *  permissions and limitations under the Licence.
  */
 
 package eu.delving.metadata;
 
 import java.io.*;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Tackling the difficult problem of testing uniqueness of the potentially very
  * numerous values of a field, within limited memory.
  * <p/>
  * This class has a threshold at which it gives up
  * on storing values in an in-memory set, and writes all values to a text file.
  * <p/>
  * Later, when the analysis storm is over, it can be called upon to re-read the
  * dumped values and give final judgement.
  *
  * @author Gerald de Jong <gerald@delving.eu>
  */
 
 public class Uniqueness {
     private static final int HOLD_THRESHOLD = 50000;
    private static final int TEXT_SIZE_LIMIT = 40;
     private Set<String> all = new HashSet<String>(HOLD_THRESHOLD * 3 / 2);
     private File tempFile;
     private Writer out;
     private int count;
 
     public boolean isRepeated(String text) {
         count++;
         if (text.length() > TEXT_SIZE_LIMIT) text = text.substring(0, TEXT_SIZE_LIMIT);
         if (all != null) {
             if (all.contains(text)) return true;
             all.add(text);
             if (all.size() > HOLD_THRESHOLD) {
                 try {
                     tempFile = File.createTempFile("Uniqueness", ".tmp");
                     System.out.println("Creating temporary file " + tempFile.getAbsolutePath());
                     tempFile.deleteOnExit();
                     out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempFile), "UTF-8"));
                     for (String one : all) {
                         out.write(one);
                         out.write('\n');
                     }
                     all = null;
                 }
                 catch (IOException e) {
                     throw new RuntimeException("Unable to create temporary file!", e);
                 }
             }
             return false;
         }
         else {
             try {
                 out.write(text);
                 out.write('\n');
             }
             catch (IOException e) {
                 throw new RuntimeException("Unable to write to temporary file!", e);
             }
             return false;
         }
     }
 
     public Set<String> getRepeated() {
         Set<String> repeated = new TreeSet<String>();
         if (all != null) return repeated; // empty
         try {
             out.close();
             out = null;
             BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempFile), "UTF-8"));
             all = new HashSet<String>(count * 3 / 2);
             String line;
             while ((line = in.readLine()) != null) {
                 if (all.contains(line)) {
                     repeated.add(line);
                     if (repeated.size() > 20) break;
                 }
                 all.add(line);
             }
             all = null;
             in.close();
             if (!tempFile.delete()) System.out.println("Unable to delete");
         }
         catch (IOException e) {
             throw new RuntimeException("Unable to work with temporary file!", e);
         }
         return repeated;
     }
 
     public void destroy() {
         try {
             if (out != null) out.close();
             if (tempFile != null && !tempFile.delete()) {}
         }
         catch (IOException e) {
             // nothing
         }
     }
 
     public int getSize() {
         if (all == null) return 0;
         return all.size();
     }
 }
