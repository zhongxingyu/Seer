 package selectionStrategies;
 import util.totalAverageRm;
 import interfaces.estimatedUserRatingRm;
 import weka.core.DenseInstance;
 import weka.core.Instance;
 import weka.core.Instances;
 import interfaces.querySelectionAlgorithm;
 
 
 public class testSelection implements querySelectionAlgorithm{
 
 	@Override
 	public Instance getQuery(Instance currentuser, Instances queryPool, Instances testPool
 			, Instances initTrainData, Instances userlatent, Instances newuserlatent, Instances itemlatent
 			, Instances trainpool
 			, double[] RMs, double[][] rProbs, double alpha) {
 		
 		double min_error = Double.MIN_VALUE; 	// initialize for worst error (INF-flag)
 //		int recDepth = 3;
 		Instance bestQuery = new DenseInstance(4);
 		
 		for (Instance query : queryPool) // for every rating (of current user) is our m
 		{
 			double rui = 0; 	// rating of user for item
 			double rum = 0; 	// rating of user for future item
 			double Rm = RMs[(int)(query.value(1)-1)]; 		// estimated rating (mean of earlier ratings)
 			double reg_var = 0;	// regularization scalar hmf * hif for all f
 		
 			double sum_error = 0; 		
 		
 			for (int lat_factor = 1; lat_factor <= userlatent.numAttributes()-1; lat_factor++)
 			{
 				rum += currentuser.value(lat_factor) * itemlatent.instance((int)query.value(1)-1).value(lat_factor);
 			}
 		
 			//	item i based variables
 			for (Instance testItem : testPool) // go through all items of users testpool
 			{
 				// 	calculate rui by matrix factorization
 				rui = 0;
 				reg_var = 0;
 			
 				// test
 //				rum = 0;
 //				for (int lat_factor = 1; lat_factor <= userlatent.numAttributes()-1; lat_factor++)
 //				{
 //					rum += wufRec(recDepth, lat_factor, currentuser, itemlatent, query, testItem, RMs, rProbs, alpha)
 //						* itemlatent.instance((int)query.value(1)-1).value(lat_factor);
 //				}
 				// test end
 				
 				for (int lat_factor = 1; lat_factor <= userlatent.numAttributes()-1; lat_factor++) { 
 					rui = rui + (currentuser.value(lat_factor)
 //							wufRec(recDepth, lat_factor, currentuser, itemlatent, query, testItem, RMs, rProbs, alpha) 
 							* itemlatent.instance( (int)(testItem.value(1)-1) ).value(lat_factor));
 				
 				
 					reg_var = reg_var + (itemlatent.instance((int) testItem.value(1)-1).value(lat_factor) 
 										* itemlatent.instance((int) query.value(1)-1).value(lat_factor));
 				}
 				
 				// Optimal learning curve with cheating (using true ratings for test-items and queries)
 //				sum_error += Math.abs(testItem.value(2) - rui + (alpha * (rum - query.value(2)) * reg_var));
 				
 				for (int rating = 0; rating < 5; rating++)
 				{
 					for (int ratingRum = 0; ratingRum < 5; ratingRum++)
 					{
 						sum_error += Math.abs(
 									rProbs[(int)(testItem.value(1)-1)][rating]
 									* rProbs[(int)(query.value(1)-1)][ratingRum] 
									* ( ((rating+1) - rui) + alpha * (rum - (ratingRum+1) * reg_var) )
 								); 
 					}
 					
 					// simple rProb rui version
 //					sum_error += rProbs[(int)(testItem.value(1)-1)][rating] * Math.abs(rating - rui + (alpha * (rum - Rm) * reg_var));
 				}
 			}
 		
 			if (min_error == Double.MIN_VALUE || sum_error < min_error) {
 				min_error = sum_error;
 				bestQuery = query;
 			}	
 			
 		}
 		//System.out.println("minimal error found: " + min_error);
 		return bestQuery;
 	}
 	
 	public double wufRec(int depth, int f
 			, Instance currentUser, Instances itemlatent, Instance query, Instance testItem
 			, double[] RMs, double[][] rProbs, double alpha)
 	{
 		if (depth == 0)
 		{
 			return currentUser.value(f);
 		} else
 		{
 			double rui = 0;
 			for (int latFac = 1; latFac < currentUser.numAttributes(); latFac++)
 			{
 				rui += wufRec(depth-1, latFac, currentUser, itemlatent, query, testItem, RMs, rProbs, alpha)
 						* itemlatent.get((int)testItem.value(1)-1).value(latFac);
 			}
 			double trui = RMs[(int)testItem.value(1)-1];
 			return wufRec(depth-1, f, currentUser, itemlatent, query, testItem, RMs, rProbs, alpha)
 					- alpha * (rui - trui) * itemlatent.get((int)testItem.value(1)-1).value(f);
 		}
 	}
 }
