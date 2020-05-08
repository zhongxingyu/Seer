 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.zip.DataFormatException;
 
 public class Main {
     public static void main(String[] args) {
         System.out.println("Available memory: " + Long.toString(Runtime.getRuntime().maxMemory()));
         System.out.println("Used memory: " + Long.toString(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
 
         PrintWriter timeDimensionalityFile = null;
         PrintWriter timeSizeFile = null;
         try {
             int blockSize = 10;
 
             timeSizeFile = new PrintWriter(new FileWriter("./output/time(size).txt"));
             for (int size = 100000; size <= 1000000; size += 100000) {
                System.out.println(size);
                 long buildStartTime = System.nanoTime();
                 KDTree tree = new KDTree(blockSize, 100, size, "./res/MultidimensionalData4AU-1.txt");
                 long buildEndTime = System.nanoTime();
                 long buildTime = buildEndTime - buildStartTime;
                 long sumOfSearchTime = 0;
                 int experimentsNumber = 10;
                 for (int i = 0; i < experimentsNumber; ++i) {
                     //System.out.println(Integer.toString(dimensionality) + ',' + Integer.toString(i));
                     long searchStartTime = System.nanoTime();
                     tree.getNearestK(10);
                     long searchEndTime = System.nanoTime();
                     sumOfSearchTime += searchEndTime - searchStartTime;
                 }
                 long avgSearchTime = sumOfSearchTime / experimentsNumber;
                 timeSizeFile.println(Long.toString(size) + ',' + Long.toString(avgSearchTime) + ',' + Long.toString(buildTime));
             }
 
             timeDimensionalityFile = new PrintWriter(new FileWriter("./output/time(dimensionality).txt"));
             for (int dimensionality = 10; dimensionality <= 100; dimensionality += 10) {
                System.out.println(dimensionality);
                 long buildStartTime = System.nanoTime();
                 KDTree tree = new KDTree(blockSize, dimensionality, 1000000, "./res/MultidimensionalData4AU-1.txt");
                 long buildEndTime = System.nanoTime();
                 long buildTime = buildEndTime - buildStartTime;
                 long sumOfSearchTime = 0;
                 int experimentsNumber = 10;
                 for (int i = 0; i < experimentsNumber; ++i) {
                     //System.out.println(Integer.toString(dimensionality) + ',' + Integer.toString(i));
                     long searchStartTime = System.nanoTime();
                     tree.getNearestK(10);
                     long searchEndTime = System.nanoTime();
                     sumOfSearchTime += searchEndTime - searchStartTime;
                 }
                 long avgSearchTime = sumOfSearchTime / experimentsNumber;
                 timeDimensionalityFile.println(Long.toString(dimensionality) + ',' + Long.toString(avgSearchTime) + ',' + Long.toString(buildTime));
             }
 
         } catch (IOException e) {
             System.out.println("Got IOException: " + e.getMessage());
         } catch (DataFormatException e) {
             System.out.println("Got DataFormatException: " + e.getMessage());
         } finally {
             if (timeDimensionalityFile != null) {
                 timeDimensionalityFile.close();
             }
             if (timeSizeFile != null) {
                 timeSizeFile.close();
             }
         }
     }
 }
