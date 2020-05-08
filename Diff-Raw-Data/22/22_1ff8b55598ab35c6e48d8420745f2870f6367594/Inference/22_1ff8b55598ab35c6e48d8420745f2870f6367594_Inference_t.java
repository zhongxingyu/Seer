 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tsweetselements;
 
 import connection.*;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import statistics.Timer;
 
 /**
  *
  * @author Diego
  */
 public class Inference {
     public static Double infer_trust_between(Integer source_id, Integer sink_id){
         Double maturity = MaturityLevel.get_level(sink_id);
         Double opinion = OpinionCorrelation.correlation_between(source_id, sink_id);
         Double reputation = Reputation.get_reputation(sink_id);
         Double trust_transivity = TrustTransitivity.trust_between(source_id, sink_id);
         
         Double[] values = new Double[]{
             maturity,
             opinion,
             reputation,
             trust_transivity
         };
         
         Double[] weights = new Double[4];
         int count_nan = 0;
         
         for (int i = 0; i < 4; i++) {
             if(Double.isNaN(values[i])){
                 weights[i] = 1.0; //verificar melhor isso...
                 values[i] = 0.0;
                 count_nan++;
             }
             else
                 weights[i] = 1.0;
         }
         
         if(count_nan == 4)
             return Double.NaN;
         
         return statistics.Statistics.weighted_average(values, weights);
     }
     
     public static void trust_map(){
         ArrayList<ModelUnidirectTrust> relationships = ModelUnidirectTrust.get_relationships_to_inference();
         
         for (ModelUnidirectTrust model : relationships) {
             Double trust = infer_trust_between(model.source_id, model.sink_id);
             System.out.println(model.source_id + " => " + model.sink_id + " = " + trust);
         }
     }
 	
 	public static void network_inference(){
 		Timer.start();
 		int matrix_size = ModelViewTrustRelationships.get_matrix_size();
 		Double opinion, reputation, trust_transivity, maturity;
 		String st = "DELETE FROM tsweets_infered_values;\n";
 		
 		for(int i = 0; i < matrix_size; i++){
 			for (int j = 0; j < matrix_size; j++) {
 				if(i != j){
 					maturity = MaturityLevel.get_level(j);
 					if(Double.isNaN(maturity)){
 						maturity = 0.0;
 					}
					Timer.turn();
					
					opinion = OpinionCorrelation.correlation_between(i, j);
 					if(Double.isNaN(opinion)){
 						opinion = 0.0;
 					}
					Timer.turn();
					
					reputation = Reputation.get_reputation(j);
 					if(Double.isNaN(reputation)){
 						reputation = 0.0;
 					}
					Timer.turn();
					
					trust_transivity = TrustTransitivity.trust_between(i, j);
 					if(Double.isNaN(trust_transivity)){
 						trust_transivity = 0.0;
 					}
					Timer.turn();
 					
 					st += String.format("INSERT INTO tsweets_infered_values (source_id, sink_id, maturity, opinion_correlation, reputation, trust_transivity) VALUES (%s,%s,%s,%s,%s,%s);\n", i, j, maturity, opinion, reputation, trust_transivity);
 					//st += i + ";" + j + ";" + maturity + ";" + opinion+ ";" + reputation+ ";" + trust_transivity + "\n";
 				}
 			}
 		}
 		Timer.stop();
 		io.Writer.write_file(st, "data/inference.txt");
 		io.Writer.log(Timer.log(), "data/log.txt");
 	}
 }
