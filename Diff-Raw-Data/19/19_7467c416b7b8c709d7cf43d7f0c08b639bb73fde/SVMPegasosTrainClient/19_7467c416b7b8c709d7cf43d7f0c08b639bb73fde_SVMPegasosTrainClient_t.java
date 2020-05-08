 package myorg.examples.allreduce;
 
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.apache.hadoop.io.IntWritable;
 
 import myorg.io.FeatureVector;
 import myorg.io.WeightVector;
 import myorg.io.WeightVectorWithCount;
 import myorg.util.SVMLightFormatParser;
 import myorg.classifier.SVMPegasosLearner;
 
 import myorg.allreduce.AllReducer;
 import myorg.allreduce.AllReduceContext;
 
 public class SVMPegasosTrainClient extends Thread {
     private AllReducer<WeightVectorWithCount> allreducer;
     private AllReduceContext context;
     private ArrayList<FeatureVector> data;
     private int startIndex;
     private int endIndex;
     private int numIters;
    private int numEpochs;
     private float lambda;
     private WeightVector weight;
 
     public SVMPegasosTrainClient(
             ArrayList<FeatureVector> data, int startIndex, int endIndex,
            int numIters, int numEpochs, float lambda,
             String host, int port, WeightVector weight) throws IOException {
         this.allreducer = new WeightVectorWithCountSumAllReducer();
         this.context = new AllReduceContext(host, port, "SVMPegasosTrainClient");
         this.data = data;
         this.startIndex = startIndex;
         this.endIndex = endIndex;
         this.numIters = numIters;
        this.numEpochs = numEpochs;
         this.lambda = lambda;
         this.weight = weight;
     }
 
     @Override
     public void run() {
         long i = 1;
         FeatureVector datum;
         Random rnd = new Random();
 
        for (int epoch = 0; epoch < numEpochs; epoch++) {
             for (int iter = 0; iter < numIters; iter++) {
                 int index = rnd.nextInt(endIndex - startIndex) + startIndex;
                 datum = data.get(index);
 
                 float eta = 1.0f / (lambda * i);
                 SVMPegasosLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
                 i++;
             }
 
             WeightVectorWithCount wwc = new WeightVectorWithCount(1, weight);
 
             try {
                 allreducer.allreduce(context, wwc);
             } catch (IOException e) {
                 break;
             }
 
             weight = wwc.getWeight();
             weight.scale(1.0f / wwc.getCount()); // averaging
         }
     }
 
     public static void main(String[] args) throws Exception {
         if (args.length < 4) {
             System.err.println("Usage: host_name port file_name weight_name");
             return;
         }
         String host = args[0];
         int port = Integer.parseInt(args[1]);
         String fileName = args[2];
         String weightName = args[3];
 
         BufferedReader reader;
         if (fileName.endsWith(".gz")) {
             reader = new BufferedReader(new InputStreamReader(
                                         new GZIPInputStream(new BufferedInputStream(
                                         new FileInputStream(fileName)))));
         } else {
             reader = new BufferedReader(new InputStreamReader(
                                         new BufferedInputStream(
                                         new FileInputStream(fileName))));
         }
 
         ArrayList<FeatureVector> data = new ArrayList<FeatureVector>();
 
         String line;
         while ((line = reader.readLine()) != null) {
             FeatureVector datum = new FeatureVector();
             SVMLightFormatParser.parse(line, datum);
 
             data.add(datum);
         }
         reader.close();
 
 
         int numWorkers = 5;
         int dim = 1 << 24;
         float lambda = 1e-4f;
        int numEpochs = 10;
        int numIters = 1000000 / (numWorkers * numEpochs);
 
         ArrayList<WeightVector> weightList = new ArrayList<WeightVector>();
         ArrayList<SVMPegasosTrainClient> workerList = new ArrayList<SVMPegasosTrainClient>();
 
         for (int i = 0; i < numWorkers; i++) {
             WeightVector weight = new WeightVector(dim);
             weightList.add(weight);
 
             int d = data.size() / numWorkers;
             int m = data.size() % numWorkers;
             int startIndex = i * d + ((i < m) ? i : m);
             int endIndex   = startIndex + d + ((i < m) ? 1 : 0);
 
             SVMPegasosTrainClient worker
                     = new SVMPegasosTrainClient(data, startIndex, endIndex,
                                                        numIters, numEpochs, lambda,
                                                         host, port, weightList.get(i));
             worker.start();
             workerList.add(worker);
         }
 
         for (int i = 0; i < workerList.size(); i++) {
             workerList.get(i).join();
         }
         
         BufferedWriter weightWriter;
         
         if (weightName.endsWith(".gz")) {
             weightWriter = new BufferedWriter(new OutputStreamWriter(
                                         new GZIPOutputStream(new BufferedOutputStream(
                                         new FileOutputStream(weightName)))));
         } else {
             weightWriter = new BufferedWriter(new OutputStreamWriter(
                                         new BufferedOutputStream(
                                         new FileOutputStream(weightName))));
         }
 
         for (int i = 0; i < weightList.size(); i++) {
             weightWriter.write(weightList.get(i).toString() + "\n");
         }
         
         weightWriter.flush();
         weightWriter.close();
 
     }
 
 }
