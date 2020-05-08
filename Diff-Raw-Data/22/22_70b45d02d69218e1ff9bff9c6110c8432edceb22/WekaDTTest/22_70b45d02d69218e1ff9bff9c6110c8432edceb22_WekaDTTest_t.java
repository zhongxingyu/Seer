 package tests;
 
 import agent.BayesWekaCCMPAgent;
 import agent.decision.WekaDT;
 import agent.decision.WekaDT.DTLearningNames;
 import learning.*;
 
 public class WekaDTTest {
 	
 	public enum ENonCatAtt {
 		STRATEGY,
 		MSGREM,
 		CERTAINTY,
 		TRUST
 	}
 	
 	public static String write2xml(String name, DTAttribute[] arrAtt, String[] arrData)
 	{
 		StringBuffer xml = new StringBuffer();
 		
 		xml.append("\t\t<dt name=\""+name+"\">\n");
 		for(DTAttribute dtAtt : arrAtt)
 		{
 			xml.append("\t\t\t<attribute name=\""+dtAtt.name+"\" type=\""+dtAtt.type+"\" />\n");
 		}
 		xml.append("\t\t\t<data>\n");
 		for(String str : arrData)
 		{
 			xml.append("\t\t\t\t"+str+"\n");
 		}
 		xml.append("\t\t\t</data>\n");
 		xml.append("\t\t</dt>\n");
 		
 		return xml.toString();
 	}
 
 	/**
 	 * @param args
 	 */	
 	public static void main(String[] args) {
 		StringBuffer xml = new StringBuffer();		
 		xml.append("\t<decisiontrees>\n");
 
 		// If you make changes here makes sure you update WekaDT.BuildTest
 		DTAttribute[] nonCatAtt = {new DTAttribute("strategy", "{NICE,REFLEX}"),
 								   new DTAttribute("msgrem", "numeric"),
 								   new DTAttribute("certainty", "numeric"),
 				                   new DTAttribute("trust", "numeric")
 		};
 		
 		// If you make changes here make sure you update corresponding code in WekaDT
 		DTAttribute[] catAtt = {new DTAttribute("adjustAppraisal", "{UNCHANGED,INFLATEx2,INFLATEx10}"),
 								new DTAttribute("generateOpinion", "{DO,DONT}"),
 								new DTAttribute("appraisalCost", "{MINIMAL,MODERATE,BEST}"),
 								new DTAttribute("getCertainty", "{TRUTH,LIE}"),
 								new DTAttribute("getReputation", "{TRUTH,LIE}"),
 								new DTAttribute("provideCertainty", "{DO,DONT}"),
 								new DTAttribute("provideOpinion", "{DO,DONT}"),
 								new DTAttribute("provideReputation", "{DO,DONT}"),
 								new DTAttribute("requestCertainty", "{DO,DONT}"),
 								new DTAttribute("requestOpinion", "{DO,DONT}"),
 								new DTAttribute("requestReputation", "{DO,DONT}"),
 								new DTAttribute("respondCertainty", "{DO,DONT}"),
 								new DTAttribute("respondReputation", "{DO,DONT}"),
 								new DTAttribute("provideWeight", "{DO,DONT}")
 									   
 		};
 		
 		// Adjust Appraisal			
 		DTAttribute[] adjApp = {nonCatAtt[ENonCatAtt.CERTAINTY.ordinal()], 
 								nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_ADJUSTAPPRAISAL.ordinal()]};
 			
 		String[] adjAppData = { "0.3,0.7,UNCHANGED",
 								"0.3,0.5,INFLATEx2",
 								"0.3,0.3,INFLATEx10",
 								"0.5,0.3,INFLATEx10",
 								"0.5,0.5,INFLATEx2",
 								"0.5,0.7,UNCHANGED",
 								"0.7,0.3,INFLATEx10",
 								"0.7,0.5,INFLATEx2",
 								"0.7,0.7,UNCHANGED"
 				
 		};		
 		
 		try{
 			DTLearning adjAppDT = new DTLearning(new DTWekaARFF(adjApp, adjAppData));
 			adjAppDT.Visualize();
 			xml.append(write2xml("AdjustAppraisal", adjApp, adjAppData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("AdjustAppraisal!!ERROR!!", adjApp, adjAppData));
 		}
 		
 		// Generate Opinion
		DTAttribute[] genOpi = {nonCatAtt[ENonCatAtt.CERTAINTY.ordinal()],
 								nonCatAtt[ENonCatAtt.TRUST.ordinal()], 
 								catAtt[DTLearningNames.DT_GENERATEOPINION.ordinal()]};
 		
		String[] genOpiData = { "0.7,0.7,DO",
								"0.7,0.5,DO",
								"0.7,0.3,DONT",
								"0.5,0.7,DO",
								"0.5,0.5,DO",
								"0.5,0.3,DONT",
								"0.3,0.7,DONT"
 				
 		};
 		
 		try{
 			DTLearning genOpiDt = new DTLearning(new DTWekaARFF(genOpi, genOpiData));
 			genOpiDt.Visualize();
 			xml.append(write2xml("GenerateOpinion", genOpi, genOpiData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("GenerateOpinion!!ERROR!!", genOpi, genOpiData));
 		}
 		
 		// Get Appraisal Cost
 		DTAttribute[] getApp = {nonCatAtt[ENonCatAtt.TRUST.ordinal()], 
 								catAtt[DTLearningNames.DT_GETAPPRAISAL.ordinal()]};
 		
 		String[] getAppData = { "0.7,BEST",
 								"0.8,BEST",
 								"0.5,MODERATE",
 								"0.6,MODERATE",
 								"0.3,MINIMAL",
 								"0.25,MINIMAL"
 				
 		};
 		
 		try{
 			DTLearning genAppDT = new DTLearning(new DTWekaARFF(getApp, getAppData));
 			genAppDT.Visualize();
 			xml.append(write2xml("GetAppraisalCost", getApp, getAppData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("GetAppraisalCost!!ERROR!!", getApp, getAppData));
 		}
 		
 		// Get Certainty Request Value
 		DTAttribute[] getCer = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_GETCERTAINTY.ordinal()]
 		};
 		
 		String[] getCerData = { "0.7,TRUTH",
 								"0.3,LIE",
 								"0.4,LIE",
 								"0.5,TRUTH"
 		};
 		
 		try{
 			DTLearning getCerDT = new DTLearning(new DTWekaARFF(getCer, getCerData));
 			getCerDT.Visualize();
 			xml.append(write2xml("GetCertaintyReqVal", getCer, getCerData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("GetCertaintyReqVal!!ERROR!!", getCer, getCerData));
 		}
 		
 		// Get Reputation Request Value
 		DTAttribute[] getRep = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_GETREPUTATION.ordinal()]
 		};
 		
 		String[] getRepData = { "0.7,TRUTH",
 								"0.3,LIE",
 								"0.4,LIE",
 								"0.5,TRUTH"
 		};
 		
 		try{
 			DTLearning getRepDT = new DTLearning(new DTWekaARFF(getRep, getRepData));
 			getRepDT.Visualize();
 			xml.append(write2xml("GetReputationReqVal", getRep, getRepData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("GetReputationReqVal!!ERROR!!", getRep, getRepData));
 		}
 		
 		// Provide Certainty
 		DTAttribute[] proCer = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_PROVIDECERTAINTY.ordinal()]
 		};
 		
 		String[] proCerData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning proCerDT = new DTLearning(new DTWekaARFF(proCer, proCerData));
 			proCerDT.Visualize();
 			xml.append(write2xml("ProvideCertainty", proCer, proCerData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("ProvideCertainty!!ERROR!!", proCer, proCerData));
 		}
 		
 		// Provide Opinion
 		DTAttribute[] proOpi = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_PROVIDEOPINION.ordinal()]
 		};
 		
 		String[] proOpiData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning proOpiDT = new DTLearning(new DTWekaARFF(proOpi, proOpiData));
 			proOpiDT.Visualize();
 			xml.append(write2xml("ProvideOpinion", proOpi, proOpiData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("ProvideOpinion!!ERROR!!", proOpi, proOpiData));
 		}
 		
 		// Provide Reputation
 		DTAttribute[] proRep = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_PROVIDEREPUTATION.ordinal()]
 		};
 		
 		String[] proRepData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning proRepDT = new DTLearning(new DTWekaARFF(proRep, proRepData));
 			proRepDT.Visualize();
 			xml.append(write2xml("ProvideReputation", proRep, proRepData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("ProvideReputation!!ERROR!!", proRep, proRepData));
 		}
 		
 		// Request Certainty
 		DTAttribute[] reqCer = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_REQUESTCERTAINTY.ordinal()]
 		};
 		
 		String[] reqCerData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning reqCerDT = new DTLearning(new DTWekaARFF(reqCer, reqCerData));
 			reqCerDT.Visualize();
 			xml.append(write2xml("RequestCertainty", reqCer, reqCerData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("RequestCertainty!!ERROR!!", reqCer, reqCerData));
 		}
 		
 		// Request Opinion
 		DTAttribute[] reqOpi = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_REQUESTOPINION.ordinal()]
 		};
 		
 		String[] reqOpiData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning reqOpiDT = new DTLearning(new DTWekaARFF(reqOpi, reqOpiData));
 			reqOpiDT.Visualize();
 			xml.append(write2xml("RequestOpinion", reqOpi, reqOpiData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("RequestOpinion!!ERROR!!", reqOpi, reqOpiData));
 		}
 		
 		// Request Reputation Update
 		DTAttribute[] reqRep = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_REQUESTREPUTATION.ordinal()]
 		};
 		
 		String[] reqRepData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning reqRepDT = new DTLearning(new DTWekaARFF(reqRep, reqRepData));
 			reqRepDT.Visualize();
 			xml.append(write2xml("RequestReputationUpdate", reqRep, reqRepData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("RequestReputationUpdate!!ERROR!!", reqRep, reqRepData));
 		}
 		
 		// Respond Certainty Request
 		DTAttribute[] resCer = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_RESPONDCERTAINTY.ordinal()]
 		};
 		
 		String[] resCerData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning resCerDT = new DTLearning(new DTWekaARFF(resCer, resCerData));
 			resCerDT.Visualize();
 			xml.append(write2xml("RespondCertaintyRequest", resCer, resCerData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("RespondCertaintyRequest!!ERROR!!", resCer, resCerData));
 		}
 		
 		// Respond Reputation Request
 		DTAttribute[] resRep = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_RESPONDREPUTATION.ordinal()]
 		};
 		
 		String[] resRepData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning resRepDT = new DTLearning(new DTWekaARFF(resRep, resRepData));
 			resRepDT.Visualize();
 			xml.append(write2xml("RespondReputationRequest", resRep, resRepData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("RespondReputationRequest!!ERROR!!", resRep, resRepData));
 		}
 		
 		// Provide Weight
 		DTAttribute[] proWgt = {nonCatAtt[ENonCatAtt.TRUST.ordinal()],
 								catAtt[DTLearningNames.DT_PROVIDEWEIGHT.ordinal()]
 		};
 		
 		String[] proWgtData = { "0.7,DO",
 								"0.3,DONT",
 								"0.4,DONT",
 								"0.5,DO"
 		};
 		
 		try{
 			DTLearning proWgtDT = new DTLearning(new DTWekaARFF(proWgt, proWgtData));
 			proWgtDT.Visualize();
 			xml.append(write2xml("ProvideWeight", proWgt, proWgtData));
 		} catch (Exception e) {
 			e.printStackTrace();
 			xml.append(write2xml("ProvideWeight!!ERROR!!", proWgt, proWgtData));
 		}		
 		
 		xml.append("\t</decisiontrees>");
 		
 		System.out.println(xml.toString());
 		
 		BayesWekaCCMPAgent test = new BayesWekaCCMPAgent("../testbed/src/testbed/participants/bayeswekaccmpagent.xml");
 		/*WekaDT ttree = test.createDecisionTree();
 		
 		for(DTLearning dt : ttree.dtreeCol)
 		{
 			dt.Visualize();
 		}*/
 	}
 
 }
