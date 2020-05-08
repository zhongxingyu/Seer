 package lab4;
 
 import java.io.IOException;
import java.util.LinkedHashMap;
 import java.util.List;
 
 /**
  *
  * @author Stuart Caddell, scaddell@my,wctc.edu
  */
public class TextFileWriter implements 
        FileWriterStrategy<List<LinkedHashMap<String, String>>> {
 
     @Override
     public String getFilePath() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public boolean getAppendIndicator() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     //@Override
    public void writeFile(List<LinkedHashMap<String, String>> textOut) 
            throws IOException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setFilePath(String filePath) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setAppendIndicator(boolean appendIndicator) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public FormatStrategy getFormatter() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setFormatter(FormatStrategy formatter) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
