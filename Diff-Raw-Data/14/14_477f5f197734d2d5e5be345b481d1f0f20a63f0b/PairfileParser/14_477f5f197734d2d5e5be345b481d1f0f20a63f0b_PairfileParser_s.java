 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment.pairfile;
 
 import com.google.common.collect.Lists;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  *
  * @author koehleru
  */
 public class PairfileParser {
 
     public static Collection<PairfileEntry> parsePairfile(String filename) throws IOException {
         return parsePairfile(new FileReader(filename));
     }
 
     public static Collection<PairfileEntry> parsePairfile(Reader inputReader) throws IOException {
         BufferedReader reader = new BufferedReader(inputReader);
         String line;
         ArrayList<PairfileEntry> ret = Lists.newArrayListWithCapacity(1000);
         while ((line = reader.readLine()) != null) {
             if (line.trim().isEmpty()) {
                 continue;
             }
            String first = line.substring(0, line.indexOf(' '));
            String second = line.substring(line.indexOf(' ') + 1, line.indexOf(' ', line.indexOf(' ') + 1));
            String annotations = line.substring(line.indexOf(' ', line.indexOf(' ') + 1) + 1);
             ret.add(new PairfileEntry(first, second, annotations));
         }
         reader.close();
         return ret;
     }
 }
