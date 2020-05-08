 package com.kpro.algorithm;
 
 import java.util.Properties;
 
 import com.kpro.dataobjects.Case;
 import com.kpro.dataobjects.PolicyObject;
 import com.kpro.dataobjects.Purpose;
 import com.kpro.dataobjects.Recipient;
 import com.kpro.dataobjects.Retention;
 import com.kpro.datastorage.PolicyDatabase;
 import com.kpro.main.Gio;
 
 /**
  * An learning algorithm that extends LearnAlgorithm
  * 
  * @author Nesha
  *
  */
 public class LearnAlgSimpler extends LearnAlgorithm{
 	
 
 
 	private PolicyDatabase pdb;
 	
 	/**
 	 * constructor
 	 * @param weightsConfig the weights
 	 */
 	public LearnAlgSimpler(Properties weightsConfig) {
 		super(weightsConfig);
 	}
 	
 	/**
 	 * Goes through the whole weights Properties and sets new
 	 * weights which it gets by calling correlation with every
 	 * key in the property as its input. 
 	 * 
 	 * @param theIO the GIO
 	 * @return Properties the new weights
 	 */
 	protected Properties applyML(Gio theIO) {
 		Properties weights = theIO.getWeights();
 		pdb = theIO.getPDB();
 		Properties newWeights = new Properties();
 		
 		for(Object i : weights.keySet()){
 				newWeights.setProperty(i.toString(), Double.toString(value(correlation(i))));
 									 
 		}
 		return newWeights;
 
 	}
 	
 	/**
 	 * Called by applyML()
 	 * 
 	 * @param x double value between [0,1] which is countyes/countno from applyML()
 	 * @return double value [0,5] that represents a new weights value
 	 */
 	private double value(double x){
 		return 10*Math.abs(x-0.5);
 	}
 	
 	/**
 	 * Goes through the PolicyDatabaeses and their cases and checks if 
 	 * atleast one of them contains the parameter. And if it does, the
 	 * returned value is set to the % of the amount of the PolicyObjects 
 	 * (that contains this parameter) that have their actions accepted.
 	 * If the parameter isn't in any of the PolicyObjects the returned 
 	 * value is 0.5  
 	 * 
 	 * @param Objects from GIO.getWeights()
 	 * @return double
 	 */
 	private double correlation(Object i){
 		double countyes = 1;
 		double countno = 1;
 		boolean y = false;
		for(PolicyObject po : pdb){
 			//checks if the parameter object is in any of the cases in any of the policyobjects
 			for(Case c : po.getCases()){
 				if(!y && i.toString().length()>8){
 					for(Purpose p : c.getPurposes()){
 						//String compares the purposes from the config file, and the ones stored in the database
 						if(i.toString().substring(8).equals(p.toString())){
 							//if y is set to true we don't need to check further
 							y = true;
 							break;
 						}
 					}
 				}
 				if(!y && i.toString().length()>10){
 					for(Retention p : c.getRetentions()){
 						if(i.toString().substring(10).equals(p.toString())){
 							
 							y = true;
 							break;
 						}
 					}
 				}
 				if(!y && i.toString().length()>10){
 					for(Recipient p : c.getRecipients()){
 						if(i.toString().substring(10).equals(p.toString())){
 							y = true;
 							break;
 						}
 					}
 				}
 			}
 			
 			if(y){
 				if(po.getAction() == null){
 					//System.out.println("PolicyObject.getAction == null in LearnAlgSimpler");
 				}
 				else{
 					if(po.getAction().getAccepted()){
 						countyes ++;
 					}
 					else{
 						countno ++;
 					}
 				}
 			}
 		}
 		return countyes/(countyes+countno);
 	}
 	
 }
