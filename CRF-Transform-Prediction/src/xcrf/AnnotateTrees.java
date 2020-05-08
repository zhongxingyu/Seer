/*
 * Copyright (C) 2006 MOSTRARE INRIA Project This file is part of XCRF, an implementation of CRFs
 * for trees (http://treecrf.gforge.inria.fr) XCRF is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version. XCRF is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU General Public
 * License along with XCRF; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package xcrf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import mostrare.crf.tree.impl.CRFGenerator;
import mostrare.crf.tree.impl.CRFWithConstraintNode;
import mostrare.operations.parts.tree.DeltaParts;
import mostrare.taskGen.AnnotateTools;
import mostrare.taskGen.TaskFactory;
import mostrare.tree.impl.NodeAST;
import mostrare.tree.impl.TreeAST;
import mostrare.tree.io.impl.AnnotationTreeReader;
import mostrare.tree.io.impl.GroundTruthReader;
import mostrare.util.ConfigurationTool;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class AnnotateTrees
{

	private static final Logger	logger							= Logger
																		.getLogger(AnnotateTrees.class);
	private static int multiplenumber =0;
	private static int byCRFnumber =0;
	private static int byprobabilitynumber =0;
	private static String corpustoannotate;
	
	private static List<Pair<String, List<Pair<Integer, String>>>> ground_truth_data = 
            new ArrayList<Pair<String, List<Pair<Integer, String>>>>();
	
	private static List<Pair<String, List<Pair<Integer, String>>>> predicated_data = 
            new ArrayList<Pair<String, List<Pair<Integer, String>>>>();
	
	private static List<Pair<String, List<Pair<Integer, String>>>> predicated_data_byprobability = 
            new ArrayList<Pair<String, List<Pair<Integer, String>>>>();
	
	private static List<Triple<String, String, Double>> nodetransformationprobability = 
			new ArrayList<Triple<String, String, Double>>();
	
	private static int	multiple_transform = 0;
				
	private static int Method_RW_Method_total=0;
	private static int Method_RW_Var_total =0;
	private static int VAR_RW_Method_total =0;
	private static int VAR_RW_VAR_total =0;
	private static int binOperatorModif_total =0;
	private static int constChange_total=0;
	private static int expLogicExpand_total=0;
	private static int expLogicReduce_total=0;
	private static int unwrapIfElse_total=0;
	private static int unwrapMethod_total=0;
	private static int wrapsIfElse_NULL_total=0;
	private static int wrapsIfElse_Others_total=0;
	private static int wrapsIf_NULL_total=0;
	private static int wrapsIf_Others_total =0;
	private static int wrapsMethod_total=0;
	private static int wrapsTryCatch_total=0;
	
	private static int Method_RW_Method_preiction=0;
	private static int Method_RW_Var_preiction =0;
	private static int VAR_RW_Method_preiction =0;
	private static int VAR_RW_VAR_preiction =0;
	private static int binOperatorModif_preiction =0;
	private static int constChange_preiction=0;
	private static int expLogicExpand_preiction=0;
	private static int expLogicReduce_preiction=0;
	private static int unwrapIfElse_preiction=0;
	private static int unwrapMethod_preiction=0;
	private static int wrapsIfElse_NULL_preiction=0;
	private static int wrapsIfElse_Others_preiction=0;
	private static int wrapsIf_NULL_preiction=0;
	private static int wrapsIf_Others_preiction =0;
	private static int wrapsMethod_preiction=0;
	private static int wrapsTryCatch_preiction=0;
	
	private static int Method_RW_Method_probability=0;
	private static int Method_RW_Var_probability =0;
	private static int VAR_RW_Method_probability =0;
	private static int VAR_RW_VAR_probability =0;
	private static int binOperatorModif_probability =0;
	private static int constChange_probability=0;
	private static int expLogicExpand_probability=0;
	private static int expLogicReduce_probability=0;
	private static int unwrapIfElse_probability=0;
	private static int unwrapMethod_probability=0;
	private static int wrapsIfElse_NULL_probability=0;
	private static int wrapsIfElse_Others_probability=0;
	private static int wrapsIf_NULL_probability=0;
	private static int wrapsIf_Others_probability =0;
	private static int wrapsMethod_probability=0;
	private static int wrapsTryCatch_probability=0;
	
	public static void main(String args[]) throws IOException
	{
		Calendar c = new GregorianCalendar();
		long start = c.getTimeInMillis();

		PropertyConfigurator.configure("config.log4j");

		execute(args);
		c = new GregorianCalendar();
		long end = c.getTimeInMillis();
		System.out.println("Annotation time:" + ((end - start) / 60000.0) + " min");
	}

	public static boolean execute(String args[]) throws IOException
	{
		Options options = createOptions();
		CommandLine cmd;
		try
		{
			cmd = getCommandLine(args, options);
		}
		catch (ParseException e2)
		{
			logger.error("Command line parser fails", e2);
			usage(options);
			return false;
		}
		ConfigurationTool config = ConfigurationTool.getInstance();
		if (config == null)
			return false;
		if (!config.isConfigurationValid())
		{
			System.err.println("Configuration file is not valid.");
			return false;
		}
		if (cmd.getArgs().length != 5)
		{
			System.err.println("Not the right number of arguments");
			usage(options);
			return false;
		}

		String crfPath = getCRFPath(cmd);
		String corpusPath = getCorpusToBeAnnotatedPath(cmd);
		String outputPath = getResultPath(cmd);
		corpustoannotate = corpusPath;
		config.setOutputNumber(getOutputNumberTopK(cmd)+1);
		
		String baselinePath = getBaselinePath(cmd);
		readHistoryProbabilty(baselinePath);

		appli(cmd, crfPath, corpusPath, outputPath);

		CRFWithConstraintNode crf = CRFGenerator.getInstance().generateCRF(crfPath);
		if (crf == null)
			return false;
		AnnotateTools taskObjs = TaskFactory.getInstance().createToolsForAnnotation(crf);
		
		File outputDir = new File(outputPath).getAbsoluteFile();
		if (!(outputDir.exists() && outputDir.isDirectory()))
			outputDir.mkdirs();

		File corpusFile = new File(corpusPath).getAbsoluteFile();

		if (outputDir.getAbsolutePath().equals(corpusFile.getAbsolutePath()))
		{
			System.err.println("Same directory for the corpus and for the output !");
			return false;
		}

		int treecount=0;
		if (corpusFile.exists() && corpusFile.isDirectory())
		{
			for (File treeFile : corpusFile.listFiles())
				if (treeFile.isFile())
				{
					TreeAST tree =  AnnotationTreeReader.getInstance().readTree(
							treeFile, crf);
					
					TreeAST groundtruthtree =  GroundTruthReader.getInstance().readTree(
							treeFile, crf);
							
					if (tree != null && groundtruthtree!=null )
					{					
						treecount+=1;
						tree.setIndex(0);
						
						List<Pair<Integer, String>> transforminfo = new ArrayList<Pair<Integer, String>>();
						
						for (int nodeindex=0; nodeindex<groundtruthtree.getNodesNumber(); nodeindex++) {
						     NodeAST node=(NodeAST)groundtruthtree.getNode(nodeindex);
						     
						     if(!crf.getAnnotationText(node.getAnnotation()).equals("EMPTY"))
						     {	 
						    	 transforminfo.add(Pair.of(nodeindex, crf.getAnnotationText(node.getAnnotation())));
						    	 
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("Method_RW_Method"))
						    		 Method_RW_Method_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("Method_RW_Var"))
						    		 Method_RW_Var_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("VAR_RW_Method"))
						    		 VAR_RW_Method_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("VAR_RW_VAR")) 
						    		 VAR_RW_VAR_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("binOperatorModif"))
						    		 binOperatorModif_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("constChange")) 
						    		 constChange_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("expLogicExpand")) 
						    		 expLogicExpand_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("expLogicReduce")) 
						    		 expLogicReduce_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("unwrapIfElse")) 
						    		 unwrapIfElse_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("unwrapMethod")) 
						    		 unwrapMethod_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsIfElse_NULL")) 
						    		 wrapsIfElse_NULL_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsIfElse_Others"))
						    		 wrapsIfElse_Others_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsIf_NULL")) 
						    		 wrapsIf_NULL_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsIf_Others")) 
						    		 wrapsIf_Others_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsMethod")) 
						    		 wrapsMethod_total+=1;
						    	 if(crf.getAnnotationText(node.getAnnotation()).startsWith("wrapsTryCatch")) 
						    		 wrapsTryCatch_total+=1;
						     }		
					    }
						
						if(transforminfo.size()>0)
							ground_truth_data.add(Pair.of(treeFile.getAbsolutePath(), transforminfo));
						 		
						if(transforminfo.size()>1) {
							multiple_transform +=1;
						}
						
						int resultalready=0;
						for(int probabilityindex=0; probabilityindex<nodetransformationprobability.size(); probabilityindex++) {
							
					    	Triple<String, String, Double> current=nodetransformationprobability.get(probabilityindex);
					    	String nodetype=current.getLeft();
					    	String nodetransform=current.getMiddle();
					    	
					    	for (int nodeindex=0; nodeindex<tree.getNodesNumber(); nodeindex++) {
						        	
							    NodeAST node=(NodeAST)tree.getNode(nodeindex);
							    String nodetypetostudy=node.getNodeType();
							    String logicalExpIdentity=node.getLogicalExpressionIdentity();
							    
							    if(nodetype.equals(nodetypetostudy)|| 
							    		(!logicalExpIdentity.isEmpty() && nodetype.equals("RootLogical"))) {
							    	predicated_data_byprobability.add(Pair.of(treeFile.getAbsolutePath(), 
							    			Arrays.asList(Pair.of(nodeindex, nodetransform))));
							    	resultalready+=1;
							    }
							    
							    if(resultalready == getOutputNumberTopK(cmd))  
							    	break;		
						    }
					    	
					    	if(resultalready == getOutputNumberTopK(cmd))  
						    	break;	
					    }
						
						if (crf.observationTestsShouldBeApplied())
							try
							{
								crf.initOperationTestsEvaluation(1);
								crf.applyObservationTests(tree);
							}
							catch (Exception e)
							{
								System.err.println("Observation tests preprocess failed. See log file for further information.");
							}
						
						 try
						 {

							DeltaParts deltaParts = taskObjs.getDeltaParts();
							deltaParts.fillDeltas(tree);

							int[][] annotationresult= deltaParts.argmaxDeltaTopk();
							int[][] annotationresultwithoutallempty = removeAllEmptyPrediction(annotationresult);
							
							String contentToPrint="Nodes with no empty annotation for tree "
					                   + Integer.toString(treecount)+":" +"\n";

							for(int resultindex=0; resultindex<annotationresultwithoutallempty.length; resultindex++) {
								
							    contentToPrint+=" prediction result: "+Integer.toString(resultindex)+"\n";
								boolean b = tree.annotate(annotationresultwithoutallempty[resultindex]);
								
								if (!b)
								{
									return false;
								}
								
							    List<Pair<Integer, String>> transforminfopredicated = new ArrayList<Pair<Integer, String>>();
																
						        for (int nodeindex=0; nodeindex<tree.getNodesNumber(); nodeindex++) {
							      NodeAST node=(NodeAST)tree.getNode(nodeindex);
							      String infofornode="";
							      if(!crf.getAnnotationText(node.getAnnotation()).equals("EMPTY"))
							      {
								     infofornode="Node "+Integer.toString(nodeindex)+"("+"Node Type:"+node.getNodeType()+")"+":"
							                +crf.getAnnotationText(node.getAnnotation())+"\n";
								     contentToPrint+=infofornode;
								     transforminfopredicated.add(Pair.of(nodeindex, crf.getAnnotationText(node.getAnnotation())));
							      }		
						       }
						       
						       if(transforminfopredicated.size()>0)
						    	   predicated_data.add(Pair.of(treeFile.getAbsolutePath(), transforminfopredicated));
							}	
							
						    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputPath+"/"+treeFile.getName(),
							    		true)));
							out.println(contentToPrint);
							out.close();
						}
						catch (Exception e)
						{
							System.out.println("annotation error happens for file"+treeFile.getAbsolutePath());
							
						//	return false;
						}
					}
					else
						System.err.println(treeFile.getAbsolutePath() + " was not taken into account. See log file for further information.");
				}
		}
		else
		{
			return false;
		}
		
		summaryresult();	
		return true;
	}
	
	private static void summaryresult () throws IOException
	{

		byCRFnumber=0;
		multiplenumber=0;
		for(int index=0; index<predicated_data.size(); index++) {
			
			Pair<String, List<Pair<Integer, String>>> certainresult = predicated_data.get(index);
			
			String versionidetify=certainresult.getLeft();
			List<Pair<Integer, String>> predictedtransforsm=certainresult.getRight();
			
			Boolean whethercorrect=false;
			
			for(int originalindex=0; originalindex < ground_truth_data.size(); originalindex++) {
				
				Pair<String, List<Pair<Integer, String>>> certainresultoriginal = ground_truth_data.get(originalindex);
				
				String versionidetifyoriginal=certainresultoriginal.getLeft();
				List<Pair<Integer, String>> predictedtransforsmoriginal=certainresultoriginal.getRight();
				
				if(versionidetify.equals(versionidetifyoriginal)) {
					if(predictedtransforsm.size()!=predictedtransforsmoriginal.size())
						continue;
					else {
						whethercorrect=true;
						
						for(int compareindex=0; compareindex<predictedtransforsm.size(); compareindex++) {
							if(predictedtransforsm.get(compareindex).getLeft()!=predictedtransforsmoriginal.get(compareindex).getLeft() ||
									!predictedtransforsm.get(compareindex).getRight().equals(predictedtransforsmoriginal.get(compareindex).getRight())
										) {							
								whethercorrect=false;
								break;
							}
					    }
					}		
					break;
				}
			}
			
			if(whethercorrect) {
				byCRFnumber+=1;
				
				if(predictedtransforsm.size()>1) {
					multiplenumber+=1;
				//	copyFileUsingApache(new File(versionidetify), new File("/home/yzx/Test-Data-For-Transform/Multiple_Transform"));
				} 
				else {
			   
				if(predictedtransforsm.get(0).getRight().startsWith("Method_RW_Method")) {
   		             Method_RW_Method_preiction+=1;
				}
   	            if(predictedtransforsm.get(0).getRight().startsWith("Method_RW_Var")) {
   		             Method_RW_Var_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("VAR_RW_Method")) {
   		            VAR_RW_Method_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("VAR_RW_VAR")) {
   		             VAR_RW_VAR_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("binOperatorModif")) {
   		             binOperatorModif_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("constChange")) {
   		            constChange_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("expLogicExpand")) {
   		            expLogicExpand_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("expLogicReduce")) {
   		            expLogicReduce_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("unwrapIfElse")) {
   		            unwrapIfElse_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("unwrapMethod")) {
   		            unwrapMethod_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIfElse_NULL")) {
   		            wrapsIfElse_NULL_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIfElse_Others")) {
   		            wrapsIfElse_Others_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIf_NULL")) {
   		            wrapsIf_NULL_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIf_Others")) {
   		            wrapsIf_Others_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsMethod")) {
   		           wrapsMethod_preiction+=1;
   	            }
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsTryCatch")) {
   		           wrapsTryCatch_preiction+=1;		
   	            }
			   }
             }
         }

		byprobabilitynumber = 0;
        for(int index=0; index<predicated_data_byprobability.size(); index++) {
			
			Pair<String, List<Pair<Integer, String>>> certainresult = predicated_data_byprobability.get(index);
			
			String versionidetify=certainresult.getLeft();
			List<Pair<Integer, String>> predictedtransforsm=certainresult.getRight();
			
			Boolean whethercorrect=false;
			
			for(int originalindex=0; originalindex < ground_truth_data.size(); originalindex++) {
				
				Pair<String, List<Pair<Integer, String>>> certainresultoriginal = ground_truth_data.get(originalindex);
				
				String versionidetifyoriginal=certainresultoriginal.getLeft();
				List<Pair<Integer, String>> predictedtransforsmoriginal=certainresultoriginal.getRight();
				
				if(versionidetify.equals(versionidetifyoriginal)) {
					if(predictedtransforsm.size()!=predictedtransforsmoriginal.size())
						continue;
					else {
						whethercorrect=true;
						
						for(int compareindex=0; compareindex<predictedtransforsm.size(); compareindex++) {
							if(predictedtransforsm.get(compareindex).getLeft()!=predictedtransforsmoriginal.get(compareindex).getLeft() ||
									!predictedtransforsm.get(compareindex).getRight().equals(predictedtransforsmoriginal.get(compareindex).getRight())
										) {							
								whethercorrect=false;
								break;
							}
					    }
					}		
					break;
				}
			}
			
			if(whethercorrect) {
				byprobabilitynumber+=1;
				
				if(predictedtransforsm.get(0).getRight().startsWith("Method_RW_Method"))
   		            Method_RW_Method_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("Method_RW_Var"))
   		            Method_RW_Var_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("VAR_RW_Method"))
   		            VAR_RW_Method_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("VAR_RW_VAR"))
   		            VAR_RW_VAR_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("binOperatorModif"))
   		            binOperatorModif_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("constChange"))
   		            constChange_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("expLogicExpand"))
   		            expLogicExpand_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("expLogicReduce"))
   		            expLogicReduce_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("unwrapIfElse"))
   		            unwrapIfElse_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("unwrapMethod"))
   		            unwrapMethod_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIfElse_NULL"))
   		            wrapsIfElse_NULL_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIfElse_Others"))
   		            wrapsIfElse_Others_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIf_NULL"))
   		            wrapsIf_NULL_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsIf_Others"))
   		            wrapsIf_Others_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsMethod"))
   		            wrapsMethod_probability+=1;
   	            if(predictedtransforsm.get(0).getRight().startsWith("wrapsTryCatch"))
   		            wrapsTryCatch_probability+=1;
			}
		}
        
		giveResultForTransform();	
	}
	
	private static void giveResultForTransform() {
		
		if(corpustoannotate.endsWith("Multiple_Transform")) {
		   System.out.println("Multiple transform instances total:"+ Integer.toString(multiple_transform)+ " correct by CRF model:" + Integer.toString(multiplenumber)+ 
					" correct by baseline:"+ Integer.toString(0));
		} else if(corpustoannotate.endsWith("Method_RW_Method")) {
		   System.out.println("Method_RW_Method total:"+ Integer.toString(Method_RW_Method_total)+ " correct by CRF model:" + Integer.toString(Method_RW_Method_preiction)+ 
				" correct by baseline:"+ Integer.toString(Method_RW_Method_probability));
		} else if(corpustoannotate.endsWith("Method_RW_Var")) {
		   System.out.println("Method_RW_Var total:"+ Integer.toString(Method_RW_Var_total)+ " correct by CRF model:" + Integer.toString(Method_RW_Var_preiction)+ 
				" correct by baseline:"+ Integer.toString(Method_RW_Var_probability));
		} else if(corpustoannotate.endsWith("VAR_RW_Method")) {
		   System.out.println("VAR_RW_Method total:"+ Integer.toString(VAR_RW_Method_total)+ " correct by CRF model:" + Integer.toString(VAR_RW_Method_preiction)+ 
				" correct by baseline:"+ Integer.toString(VAR_RW_Method_probability));
		} else if(corpustoannotate.endsWith("VAR_RW_VAR")) {
		   System.out.println("VAR_RW_VAR total:"+ Integer.toString(VAR_RW_VAR_total)+ " correct by CRF model:" + Integer.toString(VAR_RW_VAR_preiction)+ 
				" correct by baseline:"+ Integer.toString(VAR_RW_VAR_probability));
		} else if(corpustoannotate.endsWith("binOperatorModif")) {
		   System.out.println("binOperatorModif total:"+ Integer.toString(binOperatorModif_total)+ " correct by CRF model:" + Integer.toString(binOperatorModif_preiction)+ 
				" correct by baseline:"+ Integer.toString(binOperatorModif_probability));
		} else if(corpustoannotate.endsWith("constChange")) {
		   System.out.println("constChange total:"+ Integer.toString(constChange_total)+ " correct by CRF model:" + Integer.toString(constChange_preiction)+ 
				" correct by baseline:"+ Integer.toString(constChange_probability));
		} else if(corpustoannotate.endsWith("expLogicExpand")) {
		   System.out.println("expLogicExpand total:"+ Integer.toString(expLogicExpand_total)+ " correct by CRF model:" + Integer.toString(expLogicExpand_preiction)+ 
				" correct by baseline:"+ Integer.toString(expLogicExpand_probability));
		} else if(corpustoannotate.endsWith("expLogicReduce")) {
		   System.out.println("expLogicReduce total:"+ Integer.toString(expLogicReduce_total)+ " correct by CRF model:" + Integer.toString(expLogicReduce_preiction)+ 
				" correct by baseline:"+ Integer.toString(expLogicReduce_probability));
		} else if(corpustoannotate.endsWith("unwrapIfElse")) {
		   System.out.println("unwrapIfElse total:"+ Integer.toString(unwrapIfElse_total)+ " correct by CRF model:" + Integer.toString(unwrapIfElse_preiction)+ 
				" correct by baseline:"+ Integer.toString(unwrapIfElse_probability));
		} else if(corpustoannotate.endsWith("unwrapMethod")) {
		   System.out.println("unwrapMethod total:"+ Integer.toString(unwrapMethod_total)+ " correct by CRF model:" + Integer.toString(unwrapMethod_preiction)+ 
				" correct by baseline:"+ Integer.toString(unwrapMethod_probability));
		} else if(corpustoannotate.endsWith("wrapsIfElse_NULL")) {
		   System.out.println("wrapsIfElse_NULL total:"+ Integer.toString(wrapsIfElse_NULL_total)+ " correct by CRF model:" + Integer.toString(wrapsIfElse_NULL_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsIfElse_NULL_probability));
		} else if(corpustoannotate.endsWith("wrapsIfElse_Others")) {
		   System.out.println("wrapsIfElse_Others total:"+ Integer.toString(wrapsIfElse_Others_total)+ " correct by CRF model:" + Integer.toString(wrapsIfElse_Others_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsIfElse_Others_probability));
		} else if(corpustoannotate.endsWith("wrapsIf_NULL")) {
		   System.out.println("wrapsIf_NULL total:"+ Integer.toString(wrapsIf_NULL_total)+ " correct by CRF model:" + Integer.toString(wrapsIf_NULL_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsIf_NULL_probability));
		} else if(corpustoannotate.endsWith("wrapsIf_Others")) {
		   System.out.println("wrapsIf_Others:"+ Integer.toString(wrapsIf_Others_total)+ " correct by CRF model:" + Integer.toString(wrapsIf_Others_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsIf_Others_probability));
		} else if(corpustoannotate.endsWith("wrapsMethod")) {
		   System.out.println("wrapsMethod:"+ Integer.toString(wrapsMethod_total)+ " correct by CRF model:" + Integer.toString(wrapsMethod_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsMethod_probability));
		} else if(corpustoannotate.endsWith("wrapsTryCatch")) {
		   System.out.println("wrapsTryCatch:"+ Integer.toString(wrapsTryCatch_total)+ " correct by CRF model:" + Integer.toString(wrapsTryCatch_preiction)+ 
				" correct by baseline:"+ Integer.toString(wrapsTryCatch_probability));
		} else {
			System.out.println("The total number of instances to predict:"+ Integer.toString(ground_truth_data.size())+ " correct by CRF model:" + Integer.toString(byCRFnumber)+ 
					" correct by baseline:"+ Integer.toString(byprobabilitynumber));
		}
			
	}

	private static void appli(CommandLine cmd, String crfPath, String corpusPath, String resultPath)
	{
		System.out.println("Apply on:");
		System.out
				.println("\t.CRF specified by the file at " + new File(crfPath).getAbsolutePath());
		System.out.println("\t.File to annotate specified by the directory at " +
				new File(corpusPath).getAbsolutePath());
		System.out.println("\t.Result stored in the file at " +
				new File(resultPath).getAbsolutePath());
	}

	private static void usage(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter
				.printHelp(
						"Arguments are: [-dMargProb attributeNameForInternalNode attributeNameForTextLeaf attributeNameForAttribute | -cMargProb marginalProbabilitiesSetterPath] [-dAnnWriter attributeNameForInternalNode attributeNameForTextLeaf attributeNameForAttribute | -cAnnWriter annotationWriterPath] crfPath filePath resultPath",
						options);
	}
	
	private static int[][] removeAllEmptyPrediction(int[][] originalprediction) {
		
		int[][] predictionwithoutallempty;
		
		int topknumber=ConfigurationTool.getInstance().getOutputNumber()-1;
		int nodenumber=originalprediction[0].length;
		
		predictionwithoutallempty = new int[topknumber][nodenumber];
		
		int indexwithoutempty =0 ;
		
		for(int index=0; index<originalprediction.length; index++) {
			
			   int[] specificpredication= originalprediction[index];
			
			   boolean allelementthesame=true;
			   
			   int number=specificpredication[0];
			   
			   for(int resultindex=0; resultindex<specificpredication.length; resultindex++) {
				   int specificnumber=specificpredication[resultindex];
				   
				   if(specificnumber!=number) {
					   allelementthesame=false;
					   break;
				   }  
			   }
			
			   if(!allelementthesame && indexwithoutempty<topknumber)  {
				   predictionwithoutallempty[indexwithoutempty] = specificpredication;
				   indexwithoutempty+=1;
			   }
		}
		
		return predictionwithoutallempty;
	}
	
	private static void readHistoryProbabilty(String filename) {
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
			   String[] splitted=strLine.split("   ");
			   String nodetype=splitted[0];
			   String nodetransform=splitted[1];
			   Double probability=Double.parseDouble(splitted[2]);
			   nodetransformationprobability.add(Triple.of(nodetype, nodetransform, probability));
			}
			fstream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	private static CommandLine getCommandLine(String args[], Options options) throws ParseException
	{
		CommandLineParser clParser = new BasicParser();
		return clParser.parse(options, args);
	}

	private static Options createOptions()
	{
		Options options = new Options();

		return options;
	}
	
	private static String getCRFPath(CommandLine cmd)
	{
		return cmd.getArgs()[0];
	}
	
	private static String getBaselinePath(CommandLine cmd)
	{
		return cmd.getArgs()[4];
	}

	private static String getCorpusToBeAnnotatedPath(CommandLine cmd)
	{
		return cmd.getArgs()[1];
	}

	private static String getResultPath(CommandLine cmd)
	{
		return cmd.getArgs()[2];
	}
	
	private static Integer getOutputNumberTopK (CommandLine cmd)
	{
		return Integer.parseInt(cmd.getArgs()[3]);
	}

}