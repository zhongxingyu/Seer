 package org.tmu;
 
 import com.google.common.base.Stopwatch;
 import org.apache.commons.cli.*;
 import org.apache.commons.math3.stat.clustering.Cluster;
 import org.tmu.clustering.CenteroidEvaluator;
 import org.tmu.clustering.MasterPointClusterer;
 import org.tmu.old.DKMeansClusterer;
 import org.tmu.old.InMemClusteringTester;
 import org.tmu.util.*;
 
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Saeed
  * Date: 10/9/12
  * Time: 5:41 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Main {
     static Options options = new Options();
     static HelpFormatter formatter = new HelpFormatter();
     static CommandLineParser parser = new BasicParser();
 
     private static void exit(String message){
         System.out.println(message);
         formatter.printHelp("dk-means", options);
         System.exit(-1);
     }
 
     static String input_path = "";
     static String output_path = "";
     static int k = 0;
     static int t = Runtime.getRuntime().availableProcessors();
     static int chunk_size = 1000;
     static int max = 40;
     static boolean verbose = false;
     static boolean print = false;
 
 
     public static void main(String[] args) throws IOException, InterruptedException {
         options.addOption("kmeanspp", false, "use kmeans++.");
         options.addOption("kmeans", false, "use standard kmeans.");
         options.addOption("dkmeans", false, "use dkmeans.");
         options.addOption("generate", false, "generate random data.");
 //        options.addOption("evaluate", true, "Evaluate the clustering using the centers in the file.");
         options.addOption("i", "input", true, "the input file name.");
         options.addOption("o", "output", true, "the output path.");
         options.addOption("k", "k", true, "the number of clusters.");
         options.addOption("t", "threads", true, "use t threads.");
         options.addOption("p", "print", false, "print the final centers.");
         options.addOption("m", "max", true, "the max iterations / per chunk iteration for the stream case / max dimension length fot generate.");
         options.addOption("c", "chunk", true, "the chunk size.");
         options.addOption("b", "binary", false, "use binary file format.");
         options.addOption("d", "dimension", true, "number of dimensions of data (for generating only)");
         options.addOption("n",  true, "number of items (for generating only)");
         //options.addOption("v", "verbose", false, "be verbose.");
         //options.addOption("sse", false, "print sse and icd. works only on local run.");
 
         //alaki
         Stopwatch watch=new Stopwatch().start();
 
         try{
             CommandLine line = parser.parse(options, args);
 
             if (line.hasOption("t"))
                t = Integer.parseInt(line.getOptionValue("t"));
             if (line.hasOption("m"))
                 max = Integer.parseInt(line.getOptionValue("m"));
             if (line.hasOption("p"))
                 print = true;
             if (line.hasOption("k"))
                 k = Integer.parseInt(line.getOptionValue("k"));
             if (line.hasOption("c"))
                 chunk_size = Integer.parseInt(line.getOptionValue("c"));
             if (line.hasOption("v"))
                 verbose = true;
             if (line.hasOption("o"))
                 output_path = line.getOptionValue("o");
             if (line.hasOption("i"))
                 input_path = line.getOptionValue("i");
 
             if(line.hasOption("kmeanspp")){
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
                 if (!line.hasOption("m"))
                     exit("Max iterations must be given.");
 
                 System.out.println("Reading file....");
                 Collection<Point> points=CSVReader.readWholeFile(input_path);
                 System.out.println("Doing k-means++....");
 
                 Collection<Point> centers =MasterPointClusterer.KMeansPP(points,k,max);
                System.out.println("k-means++ took "+watch);
                 watch.reset().start();
                 if(line.hasOption("p")){
                     System.out.println("Centers:");
                     for(Point point:centers){
                         String s=point.toString();
                         System.out.println(s.substring(1, s.length() - 1));
                     }
                 }
                 System.out.println("Computing SE.....");
                 double se=CenteroidEvaluator.computeSSEFromCSV(input_path,new ArrayList<Point>(centers));
                 double icd=CenteroidEvaluator.computeIntraCenterDistance(new ArrayList<Point>(centers));
                 System.out.println("Took "+watch);
                 System.out.printf("SE:%g \t ICD:%f \n",se,icd);
             }
 
 
             if(line.hasOption("kmeans")){
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
                 if (!line.hasOption("m"))
                     exit("Max iterations must be given.");
 
                 System.out.println("Reading file....");
                 Collection<Point> points=CSVReader.readWholeFile(input_path);
                 System.out.println("Doing k-means....");
 
                 Collection<Point> centers =MasterPointClusterer.KMeans(points,k,max);
                 System.out.println("k-means took "+watch);
                 watch.reset().start();
                 if(line.hasOption("p")){
                     System.out.println("Centers:");
                     for(Point point:centers){
                         String s=point.toString();
                         System.out.println(s.substring(1, s.length() - 1));
                     }
                 }
                 System.out.println("Computing SE.....");
                 double se=CenteroidEvaluator.computeSSEFromCSV(input_path,new ArrayList<Point>(centers));
                 double icd=CenteroidEvaluator.computeIntraCenterDistance(new ArrayList<Point>(centers));
                 System.out.println("Took "+watch);
                 System.out.printf("SE:%g \t ICD:%f \n",se,icd);
             }
 
             if(line.hasOption("dkmeans")){
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
                 if (!line.hasOption("c"))
                     exit("Chunk size must be given.");
 
                 System.out.println("Number of threads: "+t);
 
                 if(line.hasOption("b")){
                     System.out.println("Using binary format ....");
                     Collection<Point> centers= MasterPointClusterer.DKMeansBinaryFile(input_path, k, chunk_size, 1, t);
                     System.out.println("dkmeans Took "+watch);
                     watch.reset().start();
                     if(line.hasOption("p")){
                         System.out.println("Centers:");
                         for(Point point:centers){
                             String s=point.toString();
                             System.out.println(s.substring(1, s.length() - 1));
                         }
                     }
                     System.out.println("Computing SE.....");
                     double se=CenteroidEvaluator.computeSSEFromBinary(input_path,new ArrayList<Point>(centers));
                     double icd=CenteroidEvaluator.computeIntraCenterDistance(new ArrayList<Point>(centers));
                     System.out.println("Took "+watch);
                     System.out.printf("SE:%g \t ICD:%f \n",se,icd);
 
                 }
                 else {
                     Collection<Point> centers =MasterPointClusterer.DKMeansCSVFile(input_path,k,chunk_size,1,t);
                    System.out.println("dk-means Took "+watch);
                     watch.reset().start();
                     if(line.hasOption("p")){
                         System.out.println("Centers:");
                         for(Point point:centers){
                             String s=point.toString();
                             System.out.println(s.substring(1, s.length() - 1));
                         }
                     }
                     System.out.println("Computing SE.....");
                     double se=CenteroidEvaluator.computeSSEFromCSV(input_path,new ArrayList<Point>(centers));
                     double icd=CenteroidEvaluator.computeIntraCenterDistance(new ArrayList<Point>(centers));
                     System.out.println("Took "+watch);
                     System.out.printf("SE:%g \t ICD:%f \n",se,icd);
 
                 }
             }
 
             if(line.hasOption("generate")){
             long n=10000;
             int d=5;
 
             if (!line.hasOption("n"))
                 exit("Number of items  must be given.");
             else
                 n = Long.parseLong(line.getOptionValue("n"));
 
             if (!line.hasOption("d"))
                 exit("Number of dimensions must be given.");
             else
                 d = Integer.parseInt(line.getOptionValue("d"));
 
             if (!line.hasOption("k"))
                 exit("Number of clusters must be given.");
 
             if(!line.hasOption("o"))
                 exit("Output path must be given.");
 
                 if(line.hasOption("b")){
                     System.out.println("Using binary format ....");
                     BinaryFormatWriter writer=new BinaryFormatWriter(output_path);
                     RandomPointGenerator.GenerateDisjointClustersToFile(writer,k,n,d,new Random());
                     writer.close();
                 }
                 else {
                     FileWriter writer=new FileWriter(output_path);
                     RandomPointGenerator.GenerateDisjointClustersToFile(writer,k,n,d,new Random());
                     writer.close();
                 }
 
                 System.out.printf("Written %,d points in %s.\n",n,watch.toString());
 
             System.exit(0);
         }
 
 
         } catch (org.apache.commons.cli.ParseException exp) {
             exit("Unexpected exception:" + exp.getMessage());
         }
 
 
 ////        CSVReader.TimeParallelFileRead("x:\\set.txt",8);
 ////        System.out.println("Took: "+watch);
 ////        System.exit(0);
 //        //CSVReader.TimeParallelFileRead("Z:\\cut.txt",4);
 //        //List<Point> centers= DKMeansClusterer.clusterCSVFile("X:\\Projects\\dk-means\\samples\\a1-20-2D.txt",20,1,10000,10);
 //        //List<Point> points=CSVReader.readWholeFile("X:\\Projects\\dk-means\\samples\\a1-20-2D.txt");
 //        //List<Point> points=CSVReader.readWholeFile("X:\\dk-means-sets\\household_power_consumption.csv");
 //
 ////        RandomPointGenerator.GenerateDisjointClustersToFile(new FileWriter("x:\\set.txt"),50,1000*1000*5,50,new Random());
 ////        System.exit(0);
 //        String file_name= "X:\\dk-means-sets\\300M.txt";
 //        int k=20;
 //        List<Point> points=CSVReader.readWholeFile(file_name);
 //        System.out.println("Total Points:"+points.size());
 //        System.out.println("Took: "+watch);
 //        watch.reset().start();
 //
 //        List<Point> centers;
 //
 //        System.out.println("InMem DKMeans:");
 //        centers= MasterPointClusterer.InMemParallelDKMeans(points,k,1000);
 //        for(Point c:centers)
 //            System.out.println(c);
 //        System.out.printf("SSE: %f\n", CenteroidEvaluator.computeSSE(points,centers));
 //        System.out.printf("ICD: %f\n", CenteroidEvaluator.computeIntraCenterDistance(centers));
 //        System.out.println("Took: "+watch);
 //        watch.reset().start();
 //
 //
 //        System.out.println("P-DKMeans:");
 //        //centers= MasterPointClusterer.InMemParallelDKMeans(points,k,100);
 //        centers=MasterPointClusterer.DKMeansCSVFile(file_name,k,1000,5,8);
 //        for(Point c:centers)
 //            System.out.println(c);
 //        System.out.printf("SSE: %f\n", CenteroidEvaluator.computeSSEFromCSV(file_name,centers));
 //        System.out.printf("ICD: %f\n", CenteroidEvaluator.computeIntraCenterDistance(centers));
 //        System.out.println("Took: "+watch);
 //        watch.reset().start();
 //
 //
 ////        System.out.println("DKMeans:");
 ////        centers= MasterPointClusterer.InMemDKMeans(points,k,100);
 ////        for(Point c:centers)
 ////            System.out.println(c);
 ////        System.out.printf("SSE: %f\n", CenteroidEvaluator.computeSSE(points,centers));
 ////        System.out.printf("ICD: %f\n", CenteroidEvaluator.computeIntraCenterDistance(centers));
 ////        System.out.println("Took: "+watch);
 ////        watch.reset().start();
 ////
 ////        System.out.println("KMeans:");
 ////        centers= MasterPointClusterer.KMeans(points, k, 20);
 ////        for(Point c:centers)
 ////            System.out.println(c);
 ////        System.out.printf("SSE: %f\n", CenteroidEvaluator.computeSSE(points,centers));
 ////        System.out.printf("ICD: %f\n", CenteroidEvaluator.computeIntraCenterDistance(centers));
 ////        System.out.println("Took: "+watch);
 ////        watch.reset().start();
 ////
 ////        System.out.println("KMeans++:");
 ////        centers= MasterPointClusterer.KMeansPP(points, k, 20);
 ////        for(Point c:centers)
 ////            System.out.println(c);
 ////        System.out.printf("SSE: %f\n", CenteroidEvaluator.computeSSE(points,centers));
 ////        System.out.printf("ICD: %f\n", CenteroidEvaluator.computeIntraCenterDistance(centers));
 ////        System.out.println("Took: "+watch);
 ////        watch.reset().start();
 //
 //
 //
 //
 //        System.out.println(watch);
 //        System.exit(0);
 //
 //
 //
 //
 //
 //        String bin_dir="z:/alaki";
 //        //String bin_dir=file_name;
 //        //OnDiskClusteringTester.generateBinaryDatasets(bin_dir);
 //
 //        //OnDiskClusteringTester.convertBinaryToCSV("z:/alaki","x:/alaki");
 //
 //        for(File f:new File(bin_dir).listFiles());
 //            //OnDiskClusteringTester.doTest(f.getPath(),Integer.parseInt(f.getName().substring(9,f.getName().lastIndexOf("_"))),Runtime.getRuntime().availableProcessors());
 //
 //
 //        for(int i=Integer.parseInt(args[0]);i>0;i--){
 //            Runtime.getRuntime().gc();
 //            File f=new File(bin_dir+"/BIN_NORM_20_1000M.bin");
 //            OnDiskClusteringTester.doTest(f.getPath(),Integer.parseInt(f.getName().substring(9,f.getName().lastIndexOf("_"))),i);
 //            System.exit(0);
 //        }
 //
 //        System.exit(0);
 //
 //
 ////        BinaryFormatWriter writer=new BinaryFormatWriter("z:\\binary.dat");
 ////        RandomPointGenerator.GenerateSphereToFile(writer,new Point(new double[]{0,0,0,0,0}),2*25*1024*1024,new Random());
 ////        BinaryFormatReader.TimeSequentialFileRead("z:\\binary.dat");
 ////        System.out.println(watch);
 ////        System.exit(0);
 //
 ////        System.out.println("Sequental Read:"+CSVReader.TimeSequentialFileRead(file_name));
 ////
 ////        for(int s:new int[]{2,7,13,20}){
 ////            System.out.println("##################### Clusters" + s);
 ////            RandomPointGenerator.GenerateDisjointClustersToFile(new FileWriter("z:\\NORM-"+Integer.toString(s)),s,1000*1000,5,new Random());
 ////        }
 //
 //        //RandomPointGenerator.GenerateDisjointClustersToFile(new FileWriter("z:\\big.csv"),10,100*1000*1000,5,new Random());
 //        //System.exit(0);
 //        watch.reset().start();
 //        DKMeansClusterer dkMeansClusterer1=new DKMeansClusterer();
 //        Collection<Point> res=dkMeansClusterer1.clusterFile("z:\\big.bin", 10);
 //        System.out.println(watch);
 //        for(Point p:res)
 //            System.out.println(p);
 //        System.exit(0);
 //
 //        for(int s:new int[]{2,7,13,20}){
 //            System.out.println("##################### NORM-" + s);
 //            InMemClusteringTester.doTestFromFile("z:\\NORM-" + Integer.toString(s), s, 20, 9);
 //        }
 //
 //        System.exit(0);
 //
 //        for(String s:new String[]{"Z:\\kddcup.csv","Z:\\skin.csv","Z:\\household.csv"}){
 //            System.out.println("##################### " + s);
 //            InMemClusteringTester.doTestFromFile(s, 5, 20, 5);
 //        }
 //        System.exit(0);
 //
 //        watch.reset().start();
 //        DKMeansClusterer dkMeansClusterer=new DKMeansClusterer();
 //        Collection<Point> res1=dkMeansClusterer.clusterCSVFile("Z:\\skin.csv", 23, 4, 1024, 20);
 //        System.out.println(watch);
 //        for(Point p:res1)
 //            System.out.println(p);
 //        return;
 //        TestCSV test=new TestCSV();
 //        test.ReadLines(file_name,1000*1000*10);
 //        test.SerialParse();
 //        test.ParallelParse();
 //
 //        System.exit(0);
 //        FileWriter writer=new FileWriter(file_name);
 //        Point p=new Point(32);
 //        for(int i=0;i<32;i++)
 //            p.setElement(i,0);
 //        RandomPointGenerator.GenerateDisjointClustersToFile(writer,p,5,1000*1000*10,new Random(1234));
 //        writer.close();
 //        System.out.println(watch.elapsedMillis());
 //        watch.reset().start();
 //
 //        CSVReader reader= new CSVReader(file_name);
 //        List<Point> points=new ArrayList<Point>();
 //
 //        int count=1000*1000*10;
 //
 //        Point p1=null;
 //        for(int i=0;i<count;i++)
 //        {
 //            p1=reader.ReadNextPoint();
 //            if(p1!=null)
 //                points.add(p1);
 //        }
 //        System.out.println(points.size() +" took  "+ watch.elapsedMillis());
 //
 //
 //        watch.reset().start();
 //        points.clear();
 //        reader= new CSVReader(file_name);
 //
 //        System.in.read();
 //        System.exit(0);
 
 //        Collection<Point> p;
 //        //while ((p=reader.ReadSomePointInParallel(500*1000,4))!=null&&points.size()<1000*500)
 //        int chunk_size=500*1000;
 ////        for(int i=0;i<count/chunk_size;i++){
 ////            p=reader.ReadSomePointInParallel(chunk_size,4);
 ////            points.addAll(p);
 ////        }
 //        System.out.println(points.size() +" took  "+ watch.elapsedMillis());
 //
         //System.out.println(watch.elapsedMillis());
         //System.exit(0);
 
 //        int count=1000*1000*10;
 //        int num_clusters=5;
 //        int max_iter=7;
 //        Stopwatch watch=new Stopwatch().start();
 //        List<Point> points=null;
 //
 //        for(count=1000*1000*40;count<=50*1000*1000;count=count*2){
 //            points= RandomPointGenerator.GenerateDisjointClusters(new Point(new double[]{10,10,10}),num_clusters,count,new Random(123));
 //            System.out.println("Random Generation took: "+watch);
 //            for(int y=0;y<1;y++)
 //        {
 //           watch.reset().start();
 //
 //            KMeansPlusPlusClusterer<Point> kmpp=new KMeansPlusPlusClusterer<Point>(new Random(123));
 //            kmpp.clusterBinaryFile(points,num_clusters,max_iter);
 //            System.out.println("Serial kmeans++ took: "+watch);
 //            System.out.println("Points: "+points.size());
 //            points=null;
 //            Runtime.getRuntime().gc();
 //            watch.reset().start();
 //
 //            int num_points=0;
 //            ImprovedStreamClusterer<Point> smeans=new ImprovedStreamClusterer<Point>(num_clusters);
 //            //StreamClusterer<Point> smeans=new StreamClusterer<Point>(num_clusters,new Random());
 //            smeans.Start();
 //
 //            int chunk_size=10000;
 //            points= RandomPointGenerator.GenerateDisjointClusters(new Point(new double[]{10,10,10}),num_clusters,chunk_size,new Random(123));
 //            for(int i=0;i<count/chunk_size;i++)
 //            {
 //                smeans.AddChunk(points);
 //                num_points+=points.size();
 //            }
 //            smeans.InputIsDone();
 //            //smeans.AddChunk(new ArrayList<Point>());
 //
 //            smeans.WaitTillDone();
 //            //smeans.getIntermediateCenters();
 //            //smeans.GetIntermediateCenters();
 //            System.out.println("Parallel kmeans++ took: "+watch);
 //            System.out.println("Points: "+num_points);
 //            System.out.println("==========================================");
 //        }
 //        }
     }
 }
