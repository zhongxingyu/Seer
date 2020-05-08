 package tajmi.data;
 
 import tajmi.Util;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import tajmi.data.clusterable.CenterOfMassAlgorithm;
 import tajmi.data.clusterable.DistanceAlgorithm;
 
 /**
  * Implements the KMeans algorithm for structured data. This structured data
  * should implement the Clusterable interface and override the Object.equals() method
  * @author badi
  */
 public class KMeans<T> implements Callable<List<List<T>>> {
 
     List<T> vectors;
     int k;
     List<List<T>> clusters;   // C_i for all i in {1,...,k}
     List<T> centers_of_mass;  // c_i for all i in {1,...,k}
 
     DistanceAlgorithm<T> distance_computation;
     CenterOfMassAlgorithm<T> center_of_mass_computation;
 
     public KMeans(List<T> vectors, int k, DistanceAlgorithm<T> dist, CenterOfMassAlgorithm<T> cent) {
         this.vectors = vectors;
         this.k = k;
 
         distance_computation = dist;
         center_of_mass_computation = cent;
     }
 
     /**
      * Arbitrarily seeds the kmeans algorithms with chosen centers of mass
      * @param vectors the vectors to choose from
      * @return the centers of mass
      */
     private List<T> init_centers_of_mass_from(List<T> vectors) {
 
         List<T> centers = new LinkedList<T>();
        List<T> copied_vectors = new ArrayList<T>(vectors);
         Collections.shuffle(copied_vectors, new Random(42));
 
         for (int i = 0; i < k; i++) {
             centers.add(copied_vectors.get(i));
         }
 
         return centers;
     }
 
     /**
      * Runs the KMeans algorithm
      * @return the original input data clustered into `k` clusters
      */
     public List<List<T>> call() {
         centers_of_mass = init_centers_of_mass_from(vectors);
 
         do{
             step1();
             step2();
         } while( !done() );
 
         return clusters;
     }
 
     /**
      * Checks the state to see if the clusters and centers of mass have changed.
      * If there is no chance, the we're done.
      * @return true if `C` and `c` have stabilized
      */
     private boolean done() {
 
         // save current state
         List<List<T>> saved_cluster_centers = clusters;
         List<T> saved_centers_of_mass = centers_of_mass;
 
         // next steps
         step1();
         step2();
 
         // compare new current centers of mass with previous state
         boolean same = true;
         if(saved_centers_of_mass.size() != centers_of_mass.size())
             same = false;
         if(! Util.identical(saved_centers_of_mass, centers_of_mass))
             same = false;
 
         // compare current clusters with previous state
         if(saved_cluster_centers.size() != clusters.size())
             same = false;
         if(! Util.identical(saved_cluster_centers, clusters))
             same = false;
 
         // reset to previous state if needed
         if(!same){
             clusters = saved_cluster_centers;
             centers_of_mass = saved_centers_of_mass;
             return false;
         } else
             return true;
 
     }
 
     /**
      * Finds the data points closest to the current center of mass
      * @param c_i the center of mass
      * @param i the current position
      * @return a list of points closest to `c_i` than `c_j` forall i != j
      */
     private List<T> closest_points_to(T c_i, int i) {
 
         /* find the vectors closest to c_i over c_j where i != j
          * :: For each vector, if there exists a distance  to another clusters center
          * that is less than the distance to c_i, disregard that vector, else add it
          */
         List<T> selected_points = new LinkedList<T>();
         for (T point : vectors){
             double mydist = distance_computation.distance(c_i, point);
             boolean this_point_ok = true;
 
             for (int j = 0; j < centers_of_mass.size(); j++) {
                 if( j == i)
                     continue;
                 T c = centers_of_mass.get(j);
                 if(distance_computation.distance(c, point) < mydist) {
                     this_point_ok = false;
                     break;
                 }
             }
 
             if(this_point_ok)
                 selected_points.add(point);
         }
 
 
         return selected_points;
     }
 
     /**
      * for each i in {1..k}, set the clusters center C_i to be the set of points in
      * X that are closer to c_i than they are to c_j for all i != j
      */
     private void step1() {
         List<List<T>> C = new LinkedList<List<T>>();
         List<T> C_i;
         T c;
         for (int i = 0; i < k; i++) {
             c = centers_of_mass.get(i);
 
             C_i = closest_points_to(c, i);
 
             C.add(C_i);
         }
 
         clusters = C;
     }
 
 
     /**
      * Dispatches the computation to the CenterOfMassAlgorithm object
      * @param cluster a list of the data points representing a cluster
      * @return
      */
     private T centers_of_mass(List<T> cluster) {
         return center_of_mass_computation.center(cluster);
     }
 
     /**
      * for each i in {1..k}, set c_i to be the center of mass of all points in C_i:
      * c_i = 1/|C_i| SUM_{x_j in C_i} x_j
      */
     private void step2() {
         T c;
         for (int i = 0; i < k; i++) {
             c = centers_of_mass(clusters.get(i));
 
             centers_of_mass.set(i, c);
         }
     }
 }
