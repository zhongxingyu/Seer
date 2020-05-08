 package ru.spbau.bashorov;
 
 import java.io.*;
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class Main {
     private static KDTree<Float> tree;
     private static Random random = new Random();
     private static SimpleDistanceEvaluator simpleDistance = new SimpleDistanceEvaluator();
 
     public static void main(String[] args) throws IOException, ClassNotFoundException {
 
         doBenchmark("/home/zalim/au/data_mining/3/MultidimensionalData4AU.txt");
 
 //        for (int i = 0; i < args.length; i += 2) {
 //            if (!args[i].equals("exit")) {
 //                if (i < args.length - 1) {
 //                    doCommand(args[i], args[i+1]);
 //                }
 //            }
 //        }
     }
 
     private static void doCommand(String command, String arg) throws IOException, ClassNotFoundException {
         if (command.equals("make")) {
             makeTree(arg);
         } else if (command.equals("save")) {
             saveIndex(arg);
         } else if (command.equals("load")) {
             loadIndex(arg);
         } else if (command.equals("benchmark")) {
             doBenchmark(arg);
         }
     }
 
     private static void makeTree(String path) throws IOException {
         final List<Float[]> data = loadData(path, Integer.MAX_VALUE, Integer.MAX_VALUE);
         final int maxDim = data.get(0).length;
         tree = new KDTree<Float>(data, new CircleDimensionChoicer(maxDim) , new DistanceEvaluator(maxDim), simpleDistance);
     }
 
     private static void saveIndex(String path) throws IOException {
         ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
         oos.writeObject(tree);
         oos.close();
 
     }
 
     private static void loadIndex(String path) throws IOException, ClassNotFoundException {
         ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
         tree = (KDTree<Float>) ois.readObject();
     }
 
     private static void doBenchmark(String path) throws IOException {
         final List<Float[]> data = loadData(path, Integer.MAX_VALUE, Integer.MAX_VALUE);
 
         final int maxDim = data.get(0).length;
         final int repeatCount = 10;
 
         benchmark("Circle", data, new CircleDimensionChoicer<Float>(maxDim), repeatCount);
         benchmark("Random", data, new RandomDimensionChoicer<Float>(maxDim), repeatCount);
         benchmark("Widest", data, new WidestDimensionChoicer(maxDim), repeatCount);
     }
 
     private static void benchmark(String text, List<Float[]> data, DimensionChoicer choicer, int repeatCount) {
         for (int d = 10; d <= 100; d += 10) {
             KDTree tree = null;
             gc();
             long start = System.currentTimeMillis();
             for (int i = 0; i < repeatCount; ++i) {
                 choicer.reset(d);
                 tree = new KDTree<Float>(data, choicer, new DistanceEvaluator(d), simpleDistance);
             }
             long stop = System.currentTimeMillis();
 
             double Tmake = ((double)(stop - start)) / repeatCount;
 
             start = System.currentTimeMillis();
             for (int i = 0; i < repeatCount; ++i) {
                 Float[] point = data.get(random.nextInt(data.size()));
                 tree.getNearestK(point, 10);
             }
             stop = System.currentTimeMillis();
 
             double Tsearch = ((double)(stop - start)) / repeatCount;
 
             System.out.printf("%s D\t%d\t%f\t%f\n", text, d, Tmake, Tsearch);
         }
 
         System.out.println("---");
 
         final int startN = 100000;
         final int D = data.get(0).length;
         DistanceEvaluator distance = new DistanceEvaluator(D);
        for (int n = startN; n <= 10 * startN && n < data.size(); n += startN) {
             List<Float[]> curData = data.subList(0, n);
             KDTree tree = null;
             gc();
             long start = System.currentTimeMillis();
             for (int i = 0; i < repeatCount; ++i) {
                 choicer.reset(D);
                 tree = new KDTree<Float>(curData, choicer, distance, simpleDistance);
             }
             long stop = System.currentTimeMillis();
 
             double Tmake = ((double)(stop - start)) / repeatCount;
 
             start = System.currentTimeMillis();
             for (int i = 0; i < repeatCount; ++i) {
                 Float[] point = curData.get(random.nextInt(curData.size()));
                 tree.getNearestK(point, 10);
             }
             stop = System.currentTimeMillis();
 
             double Tsearch = ((double)(stop - start)) / repeatCount;
 
             System.out.printf("%s N\t%d\t%f\t%f\n", text, n, Tmake, Tsearch);
         }
         System.out.println("===");
     }
 
     private static List<Float[]> loadData(String path, int maxPointCount, int maxDimensions) throws IOException { //todo catch exceptions
         ArrayList<Float[]> data = new ArrayList<Float[]>();
         BufferedReader in = new BufferedReader(new FileReader(path));
 
         for (int i = 0; i < maxPointCount; ++i) {
             int a = i % 100000;
             if (a == 0) System.out.print(i / 10000 + " ");
 
             String line = in.readLine();
             if (line == null) break;
 
             List<Float> point = new ArrayList<Float>();
 
             String[] numbers = line.split("\t");
             for (int d = 0; d < maxDimensions && d < numbers.length; d++) {
                 point.add(Float.parseFloat(numbers[d]));
             }
 
             data.add(point.toArray(new Float[point.size()]));
         }
         System.out.println("\n====");
 
         return data;
     }
 
     private static void gc() {
        Object obj = new Object();
        WeakReference ref = new WeakReference<Object>(obj);
        obj = null;
        while(ref.get() != null) {
             System.gc();
        }
     }
 }
