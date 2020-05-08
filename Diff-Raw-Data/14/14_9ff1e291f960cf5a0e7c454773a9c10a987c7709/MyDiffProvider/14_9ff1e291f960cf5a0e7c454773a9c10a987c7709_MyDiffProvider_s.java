 package de.cismet.custom.visualdiff;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import org.netbeans.api.diff.Difference;
 import org.netbeans.spi.diff.DiffProvider;
 
 public class MyDiffProvider extends DiffProvider {
     @Override
     public Difference[] computeDiff(Reader reader1, Reader reader2) throws IOException {
         return HuntDiff.diff(getLines(reader1), getLines(reader2), true);
     }
 
     private String[] getLines(Reader reader) throws IOException {
         List<String> result = new ArrayList<String>();
         BufferedReader bufferedReader = new BufferedReader(reader);
 
         try {
             String line = null;
             while ((line = bufferedReader.readLine()) != null) {
                 result.add(line);
             }
         } finally {
             bufferedReader.close();
         }
 
         return result.toArray(new String[0]);
     }
 }
