 package rate.engine.benchmark;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import rate.model.BenchmarkEntity;
 import rate.model.ClazzEntity;
 import rate.model.SampleEntity;
 import rate.util.DebugUtil;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.*;
 
 /**
  * Created by XianRan
  * Email: xranthoar@gmail.com
  * Date: 13-4-6
  * Time: 下午4:17
  */
 public class SLSBBenchmark extends GeneralBenchmark {
     public int getB4Frr() {
         return B4Frr;
     }
 
     public void setB4Frr(int b4Frr) {
         B4Frr = b4Frr;
     }
 
     public int getB4Far() {
         return B4Far;
     }
 
     public void setB4Far(int b4Far) {
         B4Far = b4Far;
     }
 
     public void setK(int K) {
         this.K = K;
     }
 
     public int getK() {
         return K;
     }
 
     public void setAlpha(double alpha) {
         this.alpha = alpha;
     }
 
     private int B4Frr;
     private int B4Far;
     private int K;
     private double alpha;
 
     private String frrBenchmarkDir;
     private String farBenchmarkDir;
     private String descFilePath;
     Random random = new Random();
 
     public void setBenchmark(BenchmarkEntity benchmark) {
         super.setBenchmark(benchmark);
         frrBenchmarkDir = FilenameUtils.concat(benchmark.dirPath(), "FRR");
         farBenchmarkDir = FilenameUtils.concat(benchmark.dirPath(), "FAR");
         descFilePath = FilenameUtils.concat(benchmark.dirPath(), "desc.txt");
     }
 
     public void getBenchmarkDesc() throws Exception {
         File descFile = new File(descFilePath);
         if (descFile.exists()) {
             String line = FileUtils.readFileToString(descFile).trim();
             String[] params = line.split(" ");
             B4Frr = Integer.parseInt(params[0]);
             B4Far = Integer.parseInt(params[1]);
             K = Integer.parseInt(params[1]);
         }
     }
 
     public BenchmarkEntity generate() throws Exception {
 //        if (B4Frr == 0 || B4Far == 0) throw new GeneratorException("B for Far or Frr not set!");
 
         // Experimental way
         B4Frr = B4Frr == 0 ? 10 : B4Frr;
         B4Far = B4Far == 0 ? 10 : B4Far;
         K = K == 0 ? 10 : K;
 
         prepare();
 
         PrintWriter generalPw = new PrintWriter(benchmark.getHexFilePath());
 
         generateFrrBenchmark(generalPw);
 
         generateFarBenchmark(generalPw);
 
         printUuidTable();
 
         benchmark.setDescription("This is a benchmark generate by the Second Level Subset Bootstrap method");
 
        generalPw.close();
         return benchmark;
     }
 
     public void prepare() throws Exception{
         super.prepare();
        benchmark.setType("SLSB");
         new File(frrBenchmarkDir).mkdir();
         new File(farBenchmarkDir).mkdir();
         BufferedWriter wr = new BufferedWriter(new FileWriter(descFilePath));
         wr.append(B4Frr + " " + B4Far + " " + K + "\r\n");
         wr.close();
     }
 
     public String getFarBenchmarkFilePath(int i, int j) {
         return FilenameUtils.concat(farBenchmarkDir, i + "" + j + "_bxx.txt");
     }
 
     public String getFrrBenchmarkFilePath(int b) {
         return FilenameUtils.concat(frrBenchmarkDir, b + "_bxx.txt");
     }
 
     public void generateFrrBenchmark(PrintWriter generalPw) throws Exception {
         int classCount = selectedMap.size();
         Set<Integer> generalSelectedSet = new HashSet<Integer>();
         for (int i = 1; i <= B4Frr; i++) {
             File benchmarkFile = new File(getFrrBenchmarkFilePath(i));
             benchmarkFile.createNewFile();
             PrintWriter pw = new PrintWriter(benchmarkFile);
             List<Pair<ClazzEntity, List<SampleEntity>>> selectedThisTurn = new ArrayList<Pair<ClazzEntity, List<SampleEntity>>>();
             Set<Integer> set = new HashSet<Integer>();
 
             for (int j = 0; j < classCount; j++) {
                 set.add(random.nextInt(classCount));
                 generalSelectedSet.add(j);
             }
 
             for (int j : set) {
                 selectedThisTurn.add(selectedMap.get(j));
             }
 
             generateInnerClazz(pw, selectedThisTurn);
             pw.close();
         }
 
         List<Pair<ClazzEntity, List<SampleEntity>>> generalSelected = new ArrayList<Pair<ClazzEntity, List<SampleEntity>>>();
         for (int i : generalSelectedSet) {
             generalSelected.add(selectedMap.get(i));
         }
         generateInnerClazz(generalPw, generalSelected);
     }
 
     public void generateFarBenchmark(PrintWriter generalPw) throws Exception {
         int classCount = selectedMap.size();
         Set<Pair<Integer, Integer>> generalSelectedSet = new HashSet<Pair<Integer, Integer>>();
         for (int i = 1; i <= K; i++) {  // Generate S[1], S[2], ..., S[B4Far]
             HashSet<Pair<Integer, Integer>> hashSet = new HashSet<Pair<Integer, Integer>>();
             int modN;
             // 见论文中对划分子集的描述
             if (classCount % 2 == 0) {
                 modN = i % (classCount-1);
                 for (int j = 1; j <= classCount-1; j++) {
                     for (int k = j+1; k <= classCount; k++) {
                         if ((j+k) % (classCount - 1) == modN) {
                             hashSet.add(new ImmutablePair<Integer, Integer>(j, k));
                         }
                     }
                 }
                 for (int j = 1; j <= classCount-1; j++) {
                     if ((2*j) % (classCount-1) == modN) {
                         hashSet.add(new ImmutablePair<Integer, Integer>(j, classCount));
                     }
                 }
             } else {
                 modN = (i-1) % classCount;
 
                 for (int j = 1; j <= classCount - 1; j++) {
                     for (int k = j + 1; k <= classCount; k++) {
                         if ((j+k) % classCount == modN) {
                             hashSet.add(new ImmutablePair<Integer, Integer>(j, k));
                         }
                     }
                 }
             }
             Set<Pair<Integer, Integer>> innerSet = new HashSet<Pair<Integer, Integer>>();
             List<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>(hashSet);
             for (int j = 1; j <= B4Far; j++) {
                 PrintWriter pw = new PrintWriter(new FileWriter(getFarBenchmarkFilePath(i, j)));
                 innerSet.clear();
                 for (int k = 1; k <= list.size(); k++) {
                     int index = random.nextInt(list.size());
                     innerSet.add(list.get(index));
                     generalSelectedSet.add(list.get(index));
                 }
                 printFarBenchmark(innerSet, pw);
                 pw.close();
             }
 
         }
 
         printFarBenchmark(generalSelectedSet, generalPw);
     }
 
     private void printFarBenchmark(Set<Pair<Integer, Integer>> set, PrintWriter pw) throws Exception {
         Iterator<Pair<Integer, Integer>> iterator = set.iterator();
         while (iterator.hasNext()) {
             Pair<Integer, Integer> matchPair = iterator.next();
             List<SampleEntity> sampleList1 = selectedMap.get(matchPair.getLeft()-1).getRight();
             List<SampleEntity> sampleList2 = selectedMap.get(matchPair.getRight()-1).getRight();
             for (SampleEntity sample : sampleList1) {
                 for (SampleEntity sample2 : sampleList2) {
                     pw.println(uuidTable.get(sample.getUuid()) + " " + uuidTable.get(sample2.getUuid()) + " I");
                 }
             }
         }
     }
 }
