 package rio.sorter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 public class App {
     
    public static void main( String[] args ) throws FileNotFoundException {
         
         LittleEndianReader reader = new LittleEndianReader(new File("uint64-keys.bin"));
         long[] array = reader.read();
     }
 }
