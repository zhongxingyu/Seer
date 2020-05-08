 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class KMeans {
 
     private int iterations;
     private int clusterNo;
 
     private ArrayList<DocEntry> docEntries;
     private ArrayList<DocEntry> centroids;
     private ArrayList<Integer> clusterSizes;
     private ArrayList<ArrayList<DocEntry>> clusters;
     private ArrayList<Integer> documentClusters;
     private ArrayList<DocEntry> closestDocuments;
 
     public KMeans(int iterations, int clusterNo, final ArrayList<DocEntry> docEntries) {
         this.iterations = iterations;
         this.clusterNo = clusterNo;
         this.docEntries = docEntries;
         this.centroids = new ArrayList<DocEntry>(clusterNo);
         this.clusterSizes = new ArrayList<Integer>(clusterNo);
         this.closestDocuments = new ArrayList<DocEntry>(clusterNo);
         
         this.documentClusters = new ArrayList<Integer>();
         this.clusters = new ArrayList<ArrayList<DocEntry>>();
 
         initCentroids();
     }
 
     private void initCentroids() {
         int n = docEntries.size();
         HashSet<Integer> indexes = new HashSet<Integer>();
         clusters.clear();
         closestDocuments.clear();
 
         for (int k = 0; k < clusterNo; k++) {
             int current;
             do {
                 current = (int) Math.floor(Math.random() * n);
             } while (indexes.contains(current));
             indexes.add(current);
 
             DocEntry docEntry = new DocEntry();
             docEntry.addWordWeights(docEntries.get(current));
             centroids.add(docEntry);
             closestDocuments.add(null);
         }
     }
 
     public void runAllIterations() {
         // Init
         for (int i = 0; i < docEntries.size(); i++) {
             documentClusters.add(-1);
         }
         for (int i = 0; i < clusterNo; i++) {
             clusters.add(new ArrayList<DocEntry>());
             clusterSizes.add(0);
         }
 
         // Actual run
         for (int i = 0; i < iterations; i++) {
             runOneIteration();
         }
         computeClusters();
     }
 
     private void runOneIteration() {
         // Compute centroids for each document
         for (int d = 0; d < docEntries.size(); d++) {
             int bestK = -1;
             double bestDist = Double.MAX_VALUE;
 
             for (int k = 0; k < centroids.size(); k++) {
                 double dist = docEntries.get(d).getDistance(centroids.get(k));
                
                 if (dist < bestDist) {
                     bestDist = dist;
                     bestK = k;
                    // System.out.println(best);
                 }
                //System.out.println(bestK);
             }
 
             documentClusters.set(d, bestK);
         }
 
         // Compute new centroids
         for (int k = 0; k < centroids.size(); k++) {
             centroids.set(k, new DocEntry());
             clusterSizes.set(k, 0);
         }
         for (int d = 0; d < docEntries.size(); d++) {
             Integer cluster = documentClusters.get(d);
 
             centroids.get(cluster).addWordWeights(docEntries.get(d));
             clusterSizes.set(cluster, clusterSizes.get(cluster) + 1);
         }
         for (int k = 0; k < centroids.size(); k++) {
             centroids.get(k).divideWeights(clusterSizes.get(k));
         }
     }
 
     private void computeClusters() {
         for (int d = 0; d < docEntries.size(); d++) {
             Integer currentCluster = documentClusters.get(d);
 
             DocEntry currentDocument = docEntries.get(d);
             clusters.get(currentCluster).add(currentDocument);
 
             DocEntry closestDoc = closestDocuments.get(currentCluster);
             if (closestDoc == null) {
                 closestDocuments.set(currentCluster, currentDocument);
                 continue;
             }
 
             Double currentDistance = currentDocument.getDistance(centroids.get(currentCluster));
             Double closestDistance = closestDoc.getDistance(centroids.get(currentCluster));
 
             closestDocuments.set(currentCluster,
                     currentDistance < closestDistance ? currentDocument : closestDoc);
         }
     }
 
     public DocEntry getLabel(int index) {
         return closestDocuments.get(index);
     }
     public ArrayList<DocEntry> getCluster(int index) {
         return clusters.get(index);
     }
 
     public int getDocumentCluster(int i) {
         return documentClusters.get(i);
     }
 
     //TODO: (lori) test it!
     public double computePurity() {
         Iterator<ArrayList<DocEntry>> it = clusters.iterator();
         int numerator = 0;
 
         while (it.hasNext()) {
             int spam = 0;
             int ham = 0;
             ArrayList<DocEntry> cluster = it.next();
             for (DocEntry doc : cluster) {
                 if (doc.isSpam()) {
                     spam++;
                 } else {
                     ham++;
                 }
             }
 
             int majority = Math.max(spam, ham);
             numerator += majority;
         }
 
         int denominator = docEntries.size();
         double rez = ((double) numerator) / denominator;
 
         return rez;
     }
 
     //TODO:(lori) test it!
     public double computeRandIndex() {
         int next = 1;
         //similar documents same cluster
         int TP = 0;
         //documents that are not similar are in different clusters
         int TN = 0;
         for (Integer clusterID : documentClusters) {
             int index = next;
             boolean currentSpam = docEntries.get(next - 1).isSpam();
             Iterator<Integer> it = documentClusters.listIterator(next);
 
             while (it.hasNext()) {
                 int nextClusterID = it.next();
                 boolean otherSpam = docEntries.get(index).isSpam();
                 if (clusterID == nextClusterID && currentSpam == otherSpam) {
                     TP++;
                 } else if (clusterID != nextClusterID && currentSpam != otherSpam) {
                     TN++;
                 }
                
                index++;
             }
 
             next++;
         }
 
         int nominator = TP + TN;
         int nrDocs = docEntries.size();
         int denominator = nrDocs * (nrDocs - 1) / 2;
 
         double randIndx = ((double) nominator) / denominator;
 
         return randIndx;
     }
 }
