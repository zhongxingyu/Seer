 package com.kpro.algorithm;
 
 import java.util.Properties;
 
 import com.kpro.dataobjects.Case;
 import com.kpro.dataobjects.PolicyObject;
 import com.kpro.dataobjects.Purpose;
 import com.kpro.dataobjects.Recipient;
 import com.kpro.dataobjects.Retention;
 import com.kpro.main.Gio;
 
 /**
  * A very slow learning algorithm that goes through every case in every
  * PolicyObject in the whole database.
  * 
  * @author Nesha
  *
  */
 public class LearnAlgSimple extends LearnAlgorithm{
 
 	/**
 	 * constructor
 	 * @param weightsConfig the weights
 	 */
 	public LearnAlgSimple(Properties weightsConfig) {
 		super(weightsConfig);
 	}
 	/**
 	 * For every case it checks if it contains any of the purposes, retentions
 	 * or recipients, and if it does it checks whether the action of the 
 	 * PolicyObject it is in, was accepted. And if it was accepted a counter(yes)
 	 * is incremented, and another counter (total) is incremented always.
 	 * The new weight is then value(yes/total)
 	 * 
 	 * @param theIO the GIO
 	 * @return Property a new weights Property
 	 * 
 	 */
 	@Override
 	protected Properties applyML(Gio theIO) {
 		theIO.getWeights();
 		double sum = 0; 
 		double weights[] = new double[23]; 
 		for (int i = 0; i < 23; i++) {
 			int total = 0;
 			int yes = 0;
			for (PolicyObject  po : theIO.getPDB() ) {
 				for(Case ca : po.getCases()){
 					switch (i){
 						//the 12 first are Purposes
 						case 0:
 							//ca.getPurpose(0);
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.CURRENT){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 1:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.ADMIN){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 2:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.DEVELOP){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 3:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.TAILORING){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 4:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.PSEUDO_ANALYSIS){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 5:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.PSEUDO_DECISION){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 6:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.INDIVIDUAL_ANALYSIS){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 7:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.INDIVIDUAL_DECISION){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 8:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.CONTACT){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 9:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.HISTORICAL){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 10:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.TELEMARKETING){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 11:
 							for(Purpose purp : ca.getPurposes()){
 								if(purp == Purpose.OTHER_PURPOSE){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						//the 6 next are Recipients
 						case 12:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.OURS){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 13:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.DELIVERY){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 14:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.SAME){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 15:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.OTHER_RECIPIENT){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 16:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.UNRELATED){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 17:
 							for(Recipient recip : ca.getRecipients()){
 								if(recip == Recipient.PUBLIC){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						//the last 5 cases are Retetntion
 						case 18:
 							for(Retention ret : ca.getRetentions()){
 								if(ret == Retention.NO_RETENTION){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 19:
 							for(Retention ret : ca.getRetentions()){
 								if(ret == Retention.STATED_PURPOSE){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 20:
 							for(Retention ret : ca.getRetentions()){
 								if(ret == Retention.LEGAL_REQUIREMENT){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 21:
 							for(Retention ret : ca.getRetentions()){
 								if(ret == Retention.BUSINESS_PRACTICES){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 						case 22:
 							for(Retention ret : ca.getRetentions()){
 								if(ret == Retention.INDEFINITELY){
 									total++;
 								}
 								if(po.getAction().getAccepted()){
 									yes++;
 								}
 							}
 					}
 				}
 			}
 			weights[i] = value(yes/total);
 			sum += weights[i];
 		}
 		for(int i = 0; i<23; i++){
 			weights[i] = weights[i]/sum;
 		}
 		
 		Properties prop = new Properties();
 		
 		String[] arr = { "current", "admin", "develop", "tailoring", "pseudo_analysis", "pseudo_decision", "individual analysis",
 				"individual_decision", "contact", "historical", "telemarketing", "other_purpose", "ours", "delivery", "same",
 				"other_recipient", "unrelated", "public", "no_retention", "stated purpose", "legal_requirement", 
 				"business_practices", "indefinitely"};
 		
 		
 		for(int i = 0; i<23; i++){
 			prop.put(arr[i], weights[i]);
 		}
 		
 		//theIO.setWeights(prop);
 		return prop;
 	}
 	
 	/**
 	 * Called by applyML()
 	 * 
 	 * @param x double value between [0,1] which is yes/total from applyML()
 	 * @return double value [0,5] that represents a new weights value
 	 */
 	private double value(double x){
 		/*
 		if(x>=0 && x<=50){
 			return 100-2*x;
 		}
 		return 2*x-100;
 		*/
 		return 10*Math.abs(x-0.5);
 	}
 
 }
