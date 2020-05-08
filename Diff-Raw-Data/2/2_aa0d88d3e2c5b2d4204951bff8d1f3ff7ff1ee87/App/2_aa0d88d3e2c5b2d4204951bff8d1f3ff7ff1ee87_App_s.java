 package rio.sorter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 public class App {
 
     public static void main(String[] args) {
         
         if (args.length != 1) {
             System.out.println("Required parameters: [input file] (respectively)");
             return;
         }
         
         File file = new File(args[0]);
         
         if (!file.exists()) {
             System.out.println("File \"" + file + "\" not found.");
             return;
         }
         
         LittleEndianReader reader = null;
         
         try {
             reader = new LittleEndianReader(file);
         } catch (FileNotFoundException exception) {}
         
         System.out.println("Reading file...");
         
         long readStartTime = System.currentTimeMillis();
         long[] longs = reader.read();
         long readStopTime = System.currentTimeMillis();
         
         System.out.println("Read file in " + (readStopTime - readStartTime) + "ms.");
         
         System.out.println("Sorting " + longs.length + " longs...");
         
         ConcurrentQuickSort sorter = new ConcurrentQuickSort(longs);
         
         long sortStartTime = System.currentTimeMillis();
         sorter.sort();
         long sortStopTime = System.currentTimeMillis();
         
        System.out.println("Sorted file in: " + (sortStopTime - sortStartTime + "ms."));
     }
 }
