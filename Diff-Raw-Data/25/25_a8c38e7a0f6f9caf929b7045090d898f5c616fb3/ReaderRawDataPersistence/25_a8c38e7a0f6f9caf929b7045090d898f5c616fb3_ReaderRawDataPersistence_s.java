 package uk.org.sappho.code.change.management.data.persistence;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import uk.org.sappho.code.change.management.data.RawData;
 
 public class ReaderRawDataPersistence extends RawDataPersistence {
 
     public ReaderRawDataPersistence() {
 
         super();
     }
 
     public RawData load(InputStream inputStream) throws IOException {
 
         return load(inputStream, null);
     }
 
     @Override
     public String getDescription() {
 
         return "Reader stream";
     }
 }
