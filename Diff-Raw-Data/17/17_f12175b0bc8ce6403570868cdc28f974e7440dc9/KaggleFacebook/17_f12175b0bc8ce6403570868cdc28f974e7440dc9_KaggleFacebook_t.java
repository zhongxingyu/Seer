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
         boolean newGraph = false;        
         if(true){
             Graph graph = new Graph(dir, dir+"graph.db", newGraph);                
             if(newGraph)
                 //graph.loadFromCSV("train.csv");
                 graph.batchInsertFromCSV("train.csv");        
             //graph.splitIntoSets(10, 10000, 10);
             graph.getCorrectWeights("test_0");
         }
         
         
        if(false){
            int selector = 0;        
            Graph trainGraph = new Graph(dir, dir+"train_"+selector+".db", false);
            trainGraph.makePredictions("test_"+selector, "result_"+selector );      
            trainGraph.validateResult("test_"+selector, "result_"+selector);               
        }
     }
 }
