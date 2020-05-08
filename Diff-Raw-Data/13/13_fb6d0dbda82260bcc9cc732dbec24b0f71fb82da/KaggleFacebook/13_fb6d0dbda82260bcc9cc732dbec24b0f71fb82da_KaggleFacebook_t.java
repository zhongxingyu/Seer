 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package kagglefacebook;
 
 /**
  *
  * @author rafael
  */
 public class KaggleFacebook {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         String dir = "/home/rafael/kaggle_facebook_dump/";                     
         // newGraph = true will overwrite the neo4j graph and googlechart.js
         boolean newGraph = true;
        //boolean newGraph = false;
         
         Graph graph = new Graph(dir, dir+"graph.db", newGraph);
         
         
         if(newGraph)
           //  graph.loadFromCSV("train.csv");
             graph.batchInsertFromCSV("train.csv");
         
         graph.makePredictions("test.csv");
         
         //Shut down...
         graph.shutDown();
     }
 }
