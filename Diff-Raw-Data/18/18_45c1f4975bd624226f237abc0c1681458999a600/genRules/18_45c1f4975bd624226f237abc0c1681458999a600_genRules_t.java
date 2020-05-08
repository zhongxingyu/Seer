 import java.util.ArrayList;
 import java.util.Collections;
 
 
 public class genRules
 {
 	/* determines association rules
 	 * Using number -1 to indicate where an arrow is */
 	public static ArrayList<AssociationRule> genRulesMainLoop(ArrayList<Vector> itemsets, 
 			ArrayList<Vector> vec, double minConf)
 	{
 		double support = 0.0;
 		
 		
 		// for each f e F s.t. |f| = k >= 2 (for all itemsets greater than size 1).
 		ArrayList<AssociationRule> possRules = new ArrayList<AssociationRule>();
 		for(int i = 0; i< itemsets.size(); i++) //for each itemset.
 		{
 			ArrayList<AssociationRule> newRule = new ArrayList<AssociationRule>();
 			Vector curr = itemsets.get(i);
 			
 			if(curr.getSize() > 1) //if k >= 2
 			{
 				//Vector w/o currElement ---> currElement
 				//H1 = 0 //initialize
 				//support = fk.count/n. Counting rules
 				//for s e f //for all elements in the vector
 				for(int j = 0; j < curr.getSize(); j++)  //for each element in the current itemset
 				{
 					AssociationRule tmpRule = new AssociationRule();
 					//if support(f-{s} -> {s}) >= minConf
 					//H1 = H1 (union) {f-{s}->{s}};
 					
 					//take j out of antecedent and put it in the consequent.
 					//And then calculate its support
 					
 					for(int k = 0; k < curr.getSize(); k++) 
 					{
 						if(k != j)
 						{
 							tmpRule.addLeft(curr.getElement(k));
 						}
 						else
 						{
 							tmpRule.addRight(curr.getElement(j));
 						}
 					}
					//System.out.println("allRules="+tmpRule.toString());
 					newRule.add(tmpRule);
 				}
 				//support now. A receipt has to contain all items in a receipt for it to count.
 				int siz = curr.getSize();
 				int suppCount = 0;  //items in receipt that matches
 				ArrayList<Integer> leftSupCount = new ArrayList<Integer>(newRule.size()); 
 				ArrayList<Integer> minsupport = new ArrayList<Integer>();
 				//int leftSupCount = 0;
 				//int confidenceCount = 0;
 				
 				for(int init = 0; init < newRule.size(); init++)
 				{
 					leftSupCount.add(0);
 					minsupport.add(0);
 				}
 				
 				//universal support for ALL elements in current itemset. (numerators)
 				for(int j = 0; j < vec.size(); j++)
 				{
 					for(int l =1; l < vec.get(j).getSize(); l++)
 					{
 						for(int itemInd = 0; itemInd < curr.getSize(); itemInd++)
 						{
 							if(vec.get(j).getElement(l) == curr.getElement(itemInd))
 							{
 								suppCount++;
 							}
 						}
 					}
 					if(suppCount == siz)
 					{
 						support++;
 					}
 					suppCount = 0;
 				}///end universal support loop
 				
 				//unique support. (denominators)
 				//for each new Rule
 				//for every receipt, and for every element in that receipt verses
				
 				
 				for(int recepitIndex = 0; recepitIndex < vec.size(); recepitIndex++)
 				{
 					for(int recElement =1; recElement < vec.get(recepitIndex).getSize(); recElement++)
 					{
 						for(int itemInd = 0; itemInd < newRule.size(); itemInd++)
 						{
 							if(newRule.get(itemInd).left.contains(vec.get(recepitIndex).getElement(recElement))) //curr.getElement(itemInd))
 							{
 								leftSupCount.set(itemInd, leftSupCount.get(itemInd)+1);
 							}
 							if(leftSupCount.get(itemInd) == newRule.get(itemInd).left.size())
 							{
 								minsupport.set(itemInd, minsupport.get(itemInd)+1);
 							}
 						}
 						
 					}
 					
 					//leftSupCount = 0;
 				}//end individual support
 				/*for(int k = 0; k < newRule.size(); k++) //all new rules
 				{
 					leftSupCount.add(k, 0); //init this element for modification
 					for(int j = 0; j < vec.size(); j++) //all receipts
 					{
 						for(int l =1; l < vec.get(j).getSize(); l++) // all elements in receipts
 						{
 							for(int m = 0; m < newRule.get(k).left.size(); m++) //elements in left rule
 							{
 								if(vec.get(j).getElement(l) == newRule.get(k).left.get(m))
 								{
 									leftSupCount.set(k, leftSupCount.get(k)+1);
 								}
 							}
 						}
 						if(leftSupCount.get(k) == newRule.get(k).left.size())
 						{
 							minsupport++;
 						}
 					}*/
 				
 				for(int itemInd = 0; itemInd < newRule.size(); itemInd++)
 				{
 				
 					double confidence = support / leftSupCount.get(itemInd);
					double Ursupport = support / vec.size();
 					
 					
 					//check confidence
 					if(confidence <= minConf)
 					{
						System.out.println("allRules="+newRule.toString());
 						newRule.remove(itemInd);
 						itemInd--;
 					}
 					else
 					{
						//System.out.println(confidence + " " + support);
 						AssociationRule tmp = newRule.get(itemInd);
						tmp.setSupport(Ursupport);
 						tmp.setConfidence(confidence);
 						newRule.set(itemInd, tmp);
 					}
 					
 					//loop resets
 					//minsupport = 0;
 				
 				}
				
 				//ADD new rule to set
 				possRules = joinItems(possRules, newRule);
 				
 				//loop resets
 				//leftSupCount = 0;
 				support = 0;
 			} 
 		}
 		return possRules;
 	}
 	
 	
 	
 	private static ArrayList<AssociationRule> joinItems(ArrayList<AssociationRule> possRules, 
 			ArrayList<AssociationRule> newRule)
 	{
         for(int i = 0; i < possRules.size(); i++)
         {
         	for(int j=0; j<newRule.size(); j++)
         	{
         		if(newRule.get(j).left.containsAll(possRules.get(i).left) &&
         				newRule.get(j).left.size() == possRules.get(i).left.size() &&
         				newRule.get(j).right.containsAll(possRules.get(i).right) && 
         				newRule.get(j).right.size() == possRules.get(i).right.size() )
         		{
         			newRule.remove(j);
         		}
         	}
         }
         possRules.addAll(newRule);
         return possRules;
     }
 }
