 import java.util.*;
 
 
 /**
  * @author
  * This Class implements the subjectivity elimination algorithm
  * [Based or percentiles]
  */
 public class Percentiler {
 
 	
 	public  Percentiler() {
 		//nothing	
 	}
 
     // transform the trust value
     public List<Integer> transform(List<Item> source_items, List<Item> recommender_items)
     {
     	int equals = 0;
     	int misses =0;
     	int hits = 0;
         List<Integer> outputs = new ArrayList<Integer>();       
         int w=0;
         
         List<Item> sorted_source = new ArrayList<Item>(source_items);
         List<Item> sorted_recommender = new ArrayList<Item>(recommender_items);
         
         for (int i= 0; i< sorted_source.size(); i++){
         	if (sorted_source.get(i).getRate() == 99) {
         		sorted_source.remove(i);
         	}
         }
         
         for (int i= 0; i< sorted_recommender.size(); i++){
         	if (sorted_recommender.get(i).getRate() == 99) {
         		sorted_recommender.remove(i);
         	}
         }
         
         
         Collections.sort(sorted_source);
         Collections.sort(sorted_recommender);
         
         try
         {	
         	final double MARGIN = 1;
     		
 //        	System.out.println(source_items.size());
     		for (w=0; w < source_items.size();w++)
     		{		
 //    				System.out.println("\nw ="+w);
 //    				System.out.println(source_items.get(w).getRate());
 //    				System.out.println(recommender_items.get(w).getRate());
 	    			if (source_items.get(w).getRate() !=99 && recommender_items.get(w).getRate()!= 99)//99 means that the item is not rated so we don't treat it
 	    			{
 	    				
 	    			
 		    			Float transformed_trust = new Float(0.0);
 		    			int itemToR = (int) source_items.get(w).getIndex(); //the index of the item that we want to rate
 			           		    			
 			            Float percentile = new Float(0.0);    
 			            
 			            int rateR_index = 0; //it represents the rank of the item to rate sorted_recommender
 			            while (sorted_recommender.get(rateR_index).getIndex() != itemToR && rateR_index <sorted_recommender.size()  )
 			            {
 			                rateR_index++;
 			            }
 			            rateR_index++;//the +1 is because the list's index begins at 0
 			            
 			           
 //		            System.out.println("\n rateR_index "+rateR_index +"\n w ="+w);
 			
 			            //The percentile algorithm
 			            percentile = (new Float(rateR_index) * 100) / (new Float(sorted_recommender.size()) + 1);// compute of the value of the percentile
 			            
 			            float rank = (percentile.floatValue() / 100) * (sorted_source.size() + 1);
 			            
 			            int integer_portion = (int)(Math.floor(rank));
 			            
 			            float fractional_portion = rank - integer_portion;
 			            
 			            
 			            if (integer_portion == 0)
 			            {
 			                transformed_trust = new Float(sorted_source.get(integer_portion).getRate());
 			            }
 			            else if (integer_portion == sorted_source.size())
 			            {
 			            	transformed_trust = new Float(sorted_source.get(integer_portion-1).getRate());
 			            }
 			            else
 			            {
 			                if (fractional_portion == 0.0)
 			                {
 			                	transformed_trust = new Float(sorted_source.get(integer_portion-1).getRate());
 			                }
 			                else
 			                {
 			                    transformed_trust = ((new Float(sorted_source.get(integer_portion).getRate()))
 			                        - new Float(sorted_source.get(integer_portion-1).getRate()) * fractional_portion)
 			                        + new Float(sorted_source.get(integer_portion-1).getRate());
 			                }
 			            }
 			            //end of the percentile algorithm
 			            
 			            //Computing the misses hits and equals
 			            float gama = source_items.get(w).getRate();
 		                float alpha = recommender_items.get(w).getRate();
 		                float beta = transformed_trust;
 		                //System.out.println("gamma=" + gama + " alpha=" +alpha +" beta="+ beta);
 		                if (Math.abs(alpha - beta) <= MARGIN)
 		                {
 		                    equals++;
 		                    //System.out.println("EQUAL");
 		                }
 		                else if (Math.abs(gama - alpha) > Math.abs(gama - beta))
 		                {
 		                    hits++;
 		                    //System.out.println("HIT");
 		                }
 		                else if (Math.abs(gama - alpha) < Math.abs(gama - beta))
 		                {
 		                    misses++;
 		                    //System.out.println("MISS");
 		                }
 			            
 			            
 		    			}//end if
 	        }//end for 

     		//System.out.println("equals " + equals + "hits "+hits+ "miss "+ misses);

         }//end of the try
         catch (Exception e)
         {
             System.err.println("\n Putain!!! Exception: transform: " + e+"\n message : "+e.toString());
         }
         
         outputs.add(equals);
         outputs.add(hits);
         outputs.add(misses);
         return outputs ; //transformed_trust;
     }
  }
 
 	
 		
