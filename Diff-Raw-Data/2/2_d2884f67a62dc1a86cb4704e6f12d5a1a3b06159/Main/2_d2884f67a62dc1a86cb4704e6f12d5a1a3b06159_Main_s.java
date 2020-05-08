 package org.tmu;
 
 import org.apache.commons.cli.*;
 import org.apache.commons.math3.ml.clustering.CentroidCluster;
 import org.apache.commons.math3.ml.clustering.DoublePoint;
 import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.conf.Configured;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.util.Tool;
 import org.apache.hadoop.util.ToolRunner;
 import org.tmu.clustering.*;
 import org.tmu.mapreduce.MRKMeansMapper;
 import org.tmu.mapreduce.MRKMeansReducer;
 import org.tmu.mapreduce.PointWritable;
 import org.tmu.util.CSVReader;
 import org.tmu.util.GaussianPointGenerator;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Saeed
  * Date: 9/20/13
  * Time: 10:01 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Main extends Configured implements Tool {
 
     static String input_path = "";
     static String output_path = "";
     static int k = 0;
     static int tries = 3;
     static int chunk_size = 1000;
     static int max = 40;
     static boolean verbose = false;
     static boolean print = false;
 
     static CommandLineParser parser = new BasicParser();
     //System.in.read();
 
     // create the Options
     static Options options = new Options();
     static HelpFormatter formatter = new HelpFormatter();
 
     public static void main(String[] args) throws Exception {
         //parsing command line
         // create the command line parser
         options.addOption("kmeanspp", false, "use kmeans++.");
         options.addOption("kmeans", false, "use standard kmeans.");
         options.addOption("stream", false, "use stream kmeans++.");
         options.addOption("mapreduce", false, "use Hadoop ;p");
         options.addOption("generate", false, "generate random data.");
         options.addOption("evaluate", true, "Evaluate the clustering using teh centers in the file.");
         options.addOption("i", "input", true, "the input file name.");
         options.addOption("o", "output", true, "the output path.");
         options.addOption("k", "k", true, "the number of clusters.");
         options.addOption("t", "tries", true, "try x times and return the best.");
         options.addOption("p", "print", false, "print the final centers.");
         options.addOption("m", "max", true, "the max iterations / per chunk iteration for the stream case / max dimension length fot generate.");
         options.addOption("c", "chunk", true, "the chunk size.");
         options.addOption("d", "dimension", true, "number of dimensions of data (for generating only)");
         options.addOption("n",  true, "number of items (for generating only)");
         options.addOption("v", "verbose", false, "be verbose.");
         options.addOption("sse", false, "print sse and icd. works only on local run.");
 
         List<CentroidCluster<DoublePoint>> clusters;
 
         try {
             // parse the command line arguments
             CommandLine line = parser.parse(options, args);
 
             if (line.hasOption("t"))
                 tries = Integer.parseInt(line.getOptionValue("t"));
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
 
             if(verbose)
                 Evaluator.verbose=true;
             //started
             long t0 = System.nanoTime();
 
             //standard kmeans
             if (line.hasOption("kmeans")) {
                 System.out.println("Performing kmeans.....");
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
 
                 System.out.println("Options:");
                 System.out.println("\tk: "+k);
                 System.out.println("\ttries: "+tries);
                 System.out.println("\tmax iterations: "+ max);
                 System.out.println("\tinput: "+ input_path);
                 System.out.println("\toutput: "+ output_path);
 
                 System.out.println("Reading the whole dataset....");
                 List<DoublePoint> points = CSVReader.readAllPointsFromFile(input_path);
                 System.out.printf("read %,d ponits.\n", points.size());
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
 
                 if (line.hasOption("t")) {
                     System.out.printf("Will try %d times!\n", tries);
                     MultiKMeans multiKMeans = new MultiKMeans(k, max, tries);
                     if (verbose)
                         multiKMeans.verbose = true;
                     clusters = multiKMeans.cluster(points);
                 } else {
                     System.out.printf("Max iterations is %d!\n", max);
                     KMeansClusterer kmeans = new KMeansClusterer(k, max);
                     clusters = kmeans.cluster(points);
                 }
 
                 if (line.hasOption("p")) {
                     for (CentroidCluster<DoublePoint> center : clusters)
                         System.out.println(center.getCenter());
                 }
 
                 if (line.hasOption("sse")) {
                     System.out.println("Evaluating clusters...");
                     System.out.printf("ICD is: %g\n", Evaluator.computeICD(clusters));
                     System.out.printf("SSE is: %g\n", Evaluator.computeSSE(clusters, input_path));
                 }
 
                 System.exit(0);
             }
 
             if (line.hasOption("kmeanspp")) { //standard kmeans++
                 System.out.println("Performing kmeans++ ....");
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
 
                 System.out.println("Options:");
                 System.out.println("\tk: "+k);
                 System.out.println("\ttries: "+tries);
                 System.out.println("\tmax iterations: "+ max);
                 System.out.println("\tinput: "+ input_path);
                 System.out.println("\toutput: "+ output_path);
 
                 System.out.println("Reading the whole dataset....");
                 List<DoublePoint> points = CSVReader.readAllPointsFromFile(input_path);
                 System.out.printf("read %,d ponits.\n", points.size());
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
 
                 t0=System.nanoTime();
                 System.out.println("Using the k-means++ algorithm.");
                 if (line.hasOption("t")) {
                     System.out.printf("Will try %d times!\n", tries);
                     MultiKMeansPlusPlus multiKMeansPlusPlus = new MultiKMeansPlusPlus(k, max, tries);
                     if (verbose)
                         multiKMeansPlusPlus.verbose = true;
                     clusters = multiKMeansPlusPlus.cluster(points);
                 } else {
                     System.out.printf("Max iterations is %d!\n", max);
                     KMeansPlusPlusClusterer kmeans = new KMeansPlusPlusClusterer(k, max);
                     clusters = kmeans.cluster(points);
                 }
 
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
                 if (print) {
                     for (CentroidCluster<DoublePoint> center : clusters)
                         System.out.println(center.getCenter());
                 }
 
                 if (line.hasOption("p")) {
                     for (CentroidCluster<DoublePoint> center : clusters)
                         System.out.println(center.getCenter());
                 }
 
                 if (line.hasOption("sse")) {
                     System.out.println("Evaluating clusters...");
                     System.out.printf("ICD is: %g\n", Evaluator.computeICD(clusters));
                     System.out.printf("SSE is: %g\n", Evaluator.computeSSE(clusters, input_path));
                 }
                 System.exit(0);
             }
 
 
             if (line.hasOption("stream")) { //stream kmeans++
                 System.out.println("Using the stream k-means++ algorithm.");
 
                 if (!line.hasOption("t"))
                     tries = 1;
                 if (!line.hasOption("m"))
                     max = 1;
 
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
 
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
 
                 if (!line.hasOption("c"))
                     exit("Chunk size must be given.");
 
                 System.out.println("Options:");
                 System.out.println("\tk: "+k);
                 System.out.println("\ttries per chunk: "+tries);
                 System.out.println("\tmax iterations per chunk: "+ max);
                 System.out.println("\tinput: "+ input_path);
                 System.out.println("\toutput: "+ output_path);
 
                 StreamKMeansPlusPlusClusterer streamKMeansPlusPlus = new StreamKMeansPlusPlusClusterer(input_path);
                 if (verbose)
                     streamKMeansPlusPlus.verbose = true;
                 clusters = streamKMeansPlusPlus.cluster(k, chunk_size, max, tries);
 
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
                 t0=System.nanoTime();
 
                 if (line.hasOption("p")) {
                     for (CentroidCluster<DoublePoint> center : clusters)
                         System.out.println(center.getCenter());
                 }
 
                 if (line.hasOption("sse")) {
                     System.out.println("Evaluating clusters...");
                     System.out.printf("ICD is: %g\n", Evaluator.computeICD(clusters));
                     System.out.printf("SSE is: %g\n", Evaluator.computeSSE(clusters, input_path));
                 }
 
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
                 System.exit(0);
             }
 
             //mapreduce
             if (line.hasOption("mapreduce")) { //stream kmeans++
                 System.out.println("Using MapReduce.");
                 if (!line.hasOption("t"))
                     tries = 1;
                 if (!line.hasOption("i"))
                     exit("An input file must be given!");
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
                 if (!line.hasOption("c"))
                     exit("Chunk size must be given.");
                                     clusters = null;
                 System.exit(ToolRunner.run(null, new Main(), args));
             }
 
 
             if (line.hasOption("evaluate")) {
                 System.out.println("Evaluating against datatset: "+input_path);
                 List<DoublePoint> centers = CSVReader.readAllPointsFromFile(line.getOptionValue("evaluate"));
                 double sse = Evaluator.computeSSEofCenters(centers, input_path);
                 double icd = Evaluator.computeICDofCenters(centers);
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
                 System.out.println("SSE: " + sse);
                 System.out.println("ICD: " + icd);
                 System.exit(0);
             }
 
             if(line.hasOption("generate")){
                 int n=10000;
                 int d=5;
 
                 if (!line.hasOption("n"))
                     exit("Number of items  must be given.");
                 else
                     n = Integer.parseInt(line.getOptionValue("n"));
 
                 if (!line.hasOption("d"))
                     exit("Number of dimensions must be given.");
                 else
                     d = Integer.parseInt(line.getOptionValue("d"));
 
                 if (!line.hasOption("k"))
                     exit("Number of clusters must be given.");
 
                 if(!line.hasOption("o"))
                    exit("Output pasth must be given.");
 
                 System.out.printf("Generating %,d items in %,d clusters with max dimension of %,d ....\n", n, k, max);
 
 //                BufferedWriter writer=new BufferedWriter(new FileWriter(output_path),4096*1024);
 //
 //                GaussianPointGenerator gen = new GaussianPointGenerator(k,d, max);
 //                System.out.println("Centers:");
 //                for(double[] center:gen.means)
 //                    System.out.println(IOUtil.PointToCompactString(new DoublePoint(center)));
 //                for (int i = 0; i < n; i++) {
 //                    DoublePoint point = gen.nextPoint();
 //                    IOUtil.PointToCompactString(point);
 //                    //writer.write(IOUtil.PointToCompactString(point)+"\n");
 //                }
 //
 //                writer.close();
                 GaussianPointGenerator.parallelGenerate(output_path, k, d, n, max);
                 System.out.printf("Took %,d Milliseconds\n", (System.nanoTime() - t0) / 1000000);
                 System.exit(0);
             }
 
 
         } catch (org.apache.commons.cli.ParseException exp) {
             exit("Unexpected exception:" + exp.getMessage());
         }
 
         formatter.printHelp("mrk-means", options);
     }
 
     private static void exit(String message){
         System.out.println(message);
         formatter.printHelp("mrk-means", options);
         System.exit(-1);
     }
 
     @Override
     public int run(String[] strings) throws Exception {
         Configuration conf = getConf();
         Job job = new Job(conf, "MRSUB-" + new Path(input_path).getName() + "-" + k);
         final Path inDir = new Path(input_path);
         if (output_path.isEmpty()) {
             System.out.println("Must define an output path!");
             System.exit(0);
         }
         final Path outDir = new Path(output_path);
 
         job.setJarByClass(Main.class);
         job.setMapperClass(MRKMeansMapper.class);
         job.setReducerClass(MRKMeansReducer.class);
         job.setMapOutputKeyClass(IntWritable.class);
         job.setMapOutputValueClass(PointWritable.class);
         job.setOutputKeyClass(Text.class);
         job.setOutputValueClass(Text.class);
         job.getConfiguration().setInt("ClusterCount", k);
         job.getConfiguration().setInt("ChunkSize", chunk_size);
         job.getConfiguration().setInt("Iterations", max);
         job.getConfiguration().set("mapred.output.compress", "FALSE");
 
         FileInputFormat.addInputPath(job, inDir);
         job.setInputFormatClass(org.apache.hadoop.mapreduce.lib.input.TextInputFormat.class);
         FileOutputFormat.setOutputPath(job, outDir);
         job.setNumReduceTasks(1);
 
 
         int result = job.waitForCompletion(true) ? 0 : 1;
         return result;
 
     }
 }
