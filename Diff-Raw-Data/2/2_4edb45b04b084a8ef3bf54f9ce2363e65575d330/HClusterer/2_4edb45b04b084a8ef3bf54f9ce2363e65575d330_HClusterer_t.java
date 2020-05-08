 package feedlosophor.clusterer;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import weka.clusterers.HierarchicalClusterer; // this is my port of the cluster
 import weka.core.DistanceFunction;
 import weka.core.EuclideanDistance;
 import weka.core.Instances;
 import weka.core.SelectedTag;
 import org.json.*;
 
 import feedlosophor.scoring.TFScoreTest;
 
 /**
  * A wrapper class for Weka's <code>HierarchicalClusterer</code>.
  * @author Ruogu Hu
  *
  */
 public class HClusterer {
     private HierarchicalClusterer hc;
     private String linkageMethod;
     private int nClusters;
     private int clusterNumLeavesThreshold;
     private double clusterDistThreshold;
 
     /**
      * Constructor
      * [SINGLE|COMPLETE|AVERAGE|MEAN|CENTROID|WARD|ADJCOMLPETE|NEIGHBOR_JOINING]
      * @param linkageMethod: see weka's <code>HierarchicalClusterer.TAGS_LINK_TYPE</code>
      * @param nClusters: number of clusters at the top level of hierarchy
      * @param clusterNumLeavesThreshold: maximum number of leaves per flattened cluster
      * @param clusterDistThreshold: minimum distance between two clusters considered different.
      */
     public HClusterer(String linkageMethod, int nClusters, int clusterNumLeavesThreshold, double clusterDistThreshold) {
         if (nClusters < 1) throw new IllegalArgumentException("# of clusters must be not less than 1");
         if (clusterNumLeavesThreshold < 1) throw new IllegalArgumentException("clusterNumLeavesThreshold must be not less than 1");
         if (clusterDistThreshold < 0) throw new IllegalArgumentException("clusterDistThreshold must be greater than 0");
         hc = new HierarchicalClusterer();
         
         this.linkageMethod = linkageMethod;
         this.nClusters = nClusters;
         this.clusterNumLeavesThreshold = clusterNumLeavesThreshold;
         this.clusterDistThreshold = clusterDistThreshold;
     }
 
     /**
      * Given an ARFF format data in an <code>InputStream</code>, return the
      * JSON <code>String</code> representation of clustering results.
      * @param dataStream
      * @return
      * @throws Exception
      */
     public String getJsonHierachy(ByteArrayInputStream dataStream) throws Exception {
         BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
         Instances data = new Instances(reader);
         reader.close();
         DistanceFunction df = new EuclideanDistance();
         hc.setDistanceFunction(df);
         hc.setNumClusters(nClusters);
         //System.out.println(hc.getNumClusters());
         hc.setLinkType(new SelectedTag(linkageMethod, HierarchicalClusterer.TAGS_LINK_TYPE));
         hc.setPrintNewick(true);
         hc.buildClusterer(data);
         String result = hc.jsonGraph();
         return result;
     }
 
     /**
      * Given a JSON <code>String</code> representation of clustering results, return 
      * a flattened list of clusters in a JSON format <code>String</code>.
      * @param dataStream
      * @return
      * @throws Exception
      */
     public String getClusters(String jsonHierachy) throws JSONException {
         JSONArray tree = new JSONArray(jsonHierachy);
         JSONArray flattened = new JSONArray();
         collapse(tree, flattened);
         return flattened.toString();
     }
 
     private void collapse(JSONArray tree, JSONArray flattened) throws JSONException {
         double dist = tree.getDouble(1);
         int nLeaves = tree.getInt(0);
         List<JSONObject> leaves = new ArrayList<JSONObject>();
         if (dist == -1 || (nLeaves > clusterNumLeavesThreshold || dist > clusterDistThreshold)) {
             if (tree.get(2) instanceof JSONArray) {
                 collapse((JSONArray) ((JSONArray) tree).get(2), flattened);
             } else {
                 flattened.put((JSONObject)tree.get(2));
             }
             if (tree.get(3) instanceof JSONArray) {
                 collapse((JSONArray) ((JSONArray) tree).get(3), flattened);
             } else {
                 flattened.put((JSONObject)tree.get(3));
             }
         } else {
             if (tree.get(2) instanceof JSONArray) {
                 getAllLeaves((JSONArray)((JSONArray) tree).get(2), leaves);
             } else leaves.add((JSONObject)tree.get(2));
             if (tree.get(3) instanceof JSONArray) {
                 getAllLeaves((JSONArray)((JSONArray) tree).get(3), leaves);
             } else leaves.add((JSONObject)tree.get(3));
             JSONObject dupes = new JSONObject();
             dupes.put("dupes", leaves);
             tree.put(3, dupes);
             JSONObject best = leaves.get(0);
             tree.put(2, best);
             JSONObject cluster = new JSONObject();
             leaves.remove(0);
            cluster.put("id", best.get("id"));
             cluster.put("dupes", leaves);
             flattened.put(cluster);
         }
     }
 
     private Collection<JSONObject> getAllLeaves(JSONArray tree, Collection<JSONObject> leaves) throws JSONException {
         if (tree.get(2) instanceof JSONObject) 
             leaves.add((JSONObject) tree.get(2));
         else getAllLeaves(tree.getJSONArray(2), leaves);
         if (tree.get(3) instanceof JSONObject) 
             leaves.add((JSONObject) tree.get(3));
         else getAllLeaves(tree.getJSONArray(3), leaves);
         return leaves;
     }
 
     /**
      * test
      * @param args
      */
     public static void main(String[] args) {
         String rawData = "@relation fake\n" + 
                 "@attribute 1 numeric\n" + 
                 "@attribute 2 numeric\n" + 
                 "@attribute 3 numeric\n" + 
                 "@attribute 4 numeric\n" + 
                 "@attribute 5 numeric\n" + 
                 "@attribute 6 numeric\n" + 
                 "@attribute 7 numeric\n" + 
                 "@attribute 8 numeric\n" + 
                 "@attribute 9 numeric\n" + 
                 "@attribute 10 numeric\n" + 
                 "@attribute 11 numeric\n" + 
                 "@attribute 12 numeric\n" + 
                 "@attribute 13 numeric\n" + 
                 "@attribute 14 numeric\n" + 
                 "@attribute 15 numeric\n" + 
                 "@attribute 16 numeric\n" + 
                 "@attribute 17 numeric\n" + 
                 "@attribute 18 numeric\n" + 
                 "@attribute 19 numeric\n" + 
                 "@attribute 20 numeric\n" + 
                 "@attribute 21 string\n" + 
                 "\n" + 
                 "@data\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,aaa\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,bbb\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,ccc\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,ddd\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,eee\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,fff\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,ggg\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,hhh\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,iii\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,jjj\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,kkk\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,eee\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,lll\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,mmm\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,nnn\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,ooo\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,ppp\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,qqq\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,rrr\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,sss\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,ttt\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,uuu\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,vvv\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,www\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,xxx\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,yyy\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,zzz\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,a\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,b\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,c\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,c\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,d\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,e\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,f\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,g\n" + 
                 "1,2,3,4,5,6,7,8,9,0,11,12,13,14,15,3.1,3,3,3,3,h\n" + 
                 "1.5,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,i\n" + 
                 "1.5,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,j\n" + 
                 "1,2.3,3,4,9,0.8,3,1,2,3,4,5,6,77,8,99,10,39,3,7,k\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,8,9,0,3,3,7,l\n" + 
                 "10,2,3,4.7,9,0,3,1,2.2,3,4,5,6,77,8,99,0,39,3,7,m\n" + 
                 "10,2,3,4,9,0,3,1,2,3,4,5,6,7,87,9,0,3,3,7,n\n" + 
                 "\n" + 
                 "";
         try {
             HClusterer hc = new HClusterer("AVERAGE", 1, 5, 100);
             String jsonHierachy = hc.getJsonHierachy(new ByteArrayInputStream(rawData.getBytes("UTF-8")));
             System.out.println(jsonHierachy);
             String result = hc.getClusters(jsonHierachy);
             System.out.println(result);
             JSONArray jsonResult = new JSONArray(result);
             System.out.println(jsonResult.length() + " clusters:");
             for (int i = 0; i < jsonResult.length(); ++i)
                 System.out.println(jsonResult.get(i));
             
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         TFScoreTest.TestCluster("AVERAGE", 1, 5, 6);
 
     }
 
 }
 
