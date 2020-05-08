 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.bioinformatikmuenchen.pg4.alignment.pairfile;
 
import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
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
            ArrayList<String> list = Lists.newArrayList(Splitter.on(CharMatcher.WHITESPACE).omitEmptyStrings().trimResults().split(line));
            String first = list.get(0);
            String second = list.get(1);
            list.remove(first);
            list.remove(second);
            String annotations = Joiner.on(" ").join(list);
            //String annotations = line.substring(line.indexOf(' ', line.indexOf(' ') + 1) + 1);
             ret.add(new PairfileEntry(first, second, annotations));
         }
         reader.close();
         return ret;
     }
 }
