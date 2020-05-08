 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 public class KMeansRunner {
 
    public static Logger logger;
     ArrayList<DocEntry> docEntries;
     ArrayList<Double> purityIndexAverage;
     ArrayList<Double> randIndexAverage;
 
     public KMeansRunner(ArrayList<DocEntry> docEntries) {
         this.docEntries = docEntries;
 
         int  diff = Config.supK - Config.infK + 1;
         purityIndexAverage = new ArrayList<Double>(diff);
         randIndexAverage = new ArrayList<Double>(diff);
         
         for(int i = 0; i < diff; i++){
             purityIndexAverage.add(0.0);
             randIndexAverage.add(0.0);
         }
     }
 
     public void run() {
         for (int K = Config.infK; K <= Config.supK; ++K) {
             // Log the results.
             StringBuilder stats = new StringBuilder();
             
             for (int N = 0; N < Config.N; ++N) {
                 stats.append("Clusters: " + K + " (run " + N + ")\n");
                 KMeans kMeansInstance = new KMeans(Config.iterations, K, docEntries);
                 kMeansInstance.runAllIterations();
 
                 Double purityCurrValue = kMeansInstance.computePurity();
                 Double randCurrValue = kMeansInstance.computeRandIndex();
 
                 Double puritySumValue = purityIndexAverage.get(K - Config.infK);
                 Double randSumValue = randIndexAverage.get(K - Config.infK);
 
                 purityIndexAverage.set(K - Config.infK, purityCurrValue + puritySumValue);
                 randIndexAverage.set(K - Config.infK, randCurrValue + randSumValue);
 
                 stats.append("\tPurity Index:\t" + purityCurrValue + "\n");
                 stats.append("\tRand Index:\t" + randCurrValue + "\n");
 
                 for (int cluster = 0; cluster < K; ++cluster) {
                     stats.append("\tLabel(" + cluster + "):\t"
                             + kMeansInstance.getLabel(cluster) + "\n");
                 }
             }
 
            stats.append("====================================================\n");
             logger.log(Config.LOG_LEVEL, stats.toString());
             System.out.println(stats.toString());
         }
 
         StringBuilder avgStats = new StringBuilder();
        avgStats.append("Average Log:\n");
 
         for (int K = Config.infK; K <= Config.supK; ++K) {
             
             int current = K - Config.infK;
             
             // Dividing (we also need the averages for the graph)
             purityIndexAverage.set(current, purityIndexAverage.get(current) / Config.N);
             randIndexAverage.set(current, randIndexAverage.get(current) / Config.N);
             
             
             avgStats.append("Clusters: " + K + "\n");
             avgStats.append("\tPurityIndex:\t"
                     + purityIndexAverage.get(current)
                     + "\n");
 
             avgStats.append("\tRandIndex:\t" +
                     + randIndexAverage.get(current)
                     + "\n");
 
             avgStats.append("\n");
         }
         
        logger.log(Config.LOG_LEVEL, avgStats.toString());
         System.out.println(avgStats.toString());
         
         KmeansGraph graph = new KmeansGraph(); 
         graph.createGraph(purityIndexAverage, randIndexAverage); 
     }
 }
