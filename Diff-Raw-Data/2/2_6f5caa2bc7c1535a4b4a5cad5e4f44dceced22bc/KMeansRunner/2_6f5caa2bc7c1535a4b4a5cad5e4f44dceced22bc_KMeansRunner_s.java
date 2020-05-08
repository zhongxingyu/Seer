 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 public class KMeansRunner {
 
     final static Logger logger = Logger.getLogger(KMeansRunner.class.getName());
     ArrayList<DocEntry> docEntries;
     ArrayList<Double> purityIndexAverage;
     ArrayList<Double> randIndexAverage;
 
     public KMeansRunner(ArrayList<DocEntry> docEntries) {
         this.docEntries = docEntries;
 
         purityIndexAverage = new ArrayList<Double>(Config.supK - Config.infK + 1);
         randIndexAverage = new ArrayList<Double>(Config.supK - Config.infK + 1);
     }
 
     public void run() {
         for (int N = 0; N < Config.N; ++N) {
             // Log the results.
             StringBuilder stats = new StringBuilder();
             stats.append("Run no. " + N + "\n");
 
            for (int K = Config.infK; K < Config.supK; ++K) {
                 KMeans kMeansInstance = new KMeans(Config.iterations, K, docEntries);
                 kMeansInstance.runAllIterations();
 
                 Double purityCurrValue = kMeansInstance.computePurity();
                 Double randCurrValue = kMeansInstance.computeRandIndex();
 
                 Double puritySumValue = purityIndexAverage.get(K - Config.infK);
                 Double randSumValue = randIndexAverage.get(K - Config.infK);
 
                 purityIndexAverage.set(K - Config.infK, purityCurrValue + puritySumValue);
                 randIndexAverage.set(K - Config.infK, randCurrValue + randSumValue);
 
                 stats.append(K + " clusters:\n");
                 stats.append("Purity Index: " + purityCurrValue + "\n");
                 stats.append("Rand Index: " + randCurrValue + "\n");
             }
 
             logger.log(Config.LOG_LEVEL, stats.toString());
             System.out.println(stats.toString());
         }
 
         StringBuilder avgStats = new StringBuilder();
         avgStats.append("Average Log:\n");
 
         for (int K = Config.infK; K < Config.supK; ++K) {
             avgStats.append("PurityIndex(" + K + "): "
                     + purityIndexAverage.get(K - Config.infK) / Config.N
                     + "\n");
 
             avgStats.append("RandIndex(" + K + "): "
                     + randIndexAverage.get(K - Config.infK) / Config.N
                     + "\n");
         }
     }
 }
