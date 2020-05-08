package FileParse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.javatuples.Quartet;
import org.javatuples.Sextet;

import mostrare.crf.tree.CharacteristicBinaryOperator;
import mostrare.crf.tree.CharacteristicConstant;
import mostrare.crf.tree.CharacteristicConstructor;
import mostrare.crf.tree.CharacteristicLogExp;
import mostrare.crf.tree.CharacteristicMethod;
import mostrare.crf.tree.CharacteristicStat;
import mostrare.crf.tree.CharacteristicTypeAccess;
import mostrare.crf.tree.CharacteristicVar;
import mostrare.crf.tree.LogicalTransforms;
import mostrare.crf.tree.VirtualTransforms;
import mostrare.tree.Node;

public class CRFEstablish {

	protected static String trainingDataDirectory;
	protected static String CRFSavePath;
	protected static String BaselineSavePath;
	protected static boolean onlyobservation;
	protected static boolean onlyindicator;

	static List<String> allstatementtype = new ArrayList<String>();
	
	static List<String> allparentstatementtype = new ArrayList<String>();
	
	static List<String> allstatementtypebefore = new ArrayList<String>();

	static List<String> allstatementtypeafter = new ArrayList<String>(); 
	
	static List<Triple<String, String, Integer>> statementandbefore = new ArrayList<Triple<String, String, Integer>>();
	
	static List<Triple<String, String, Integer>> statementandafter = new ArrayList<Triple<String, String, Integer>>();

	static List<Triple<String, String, Integer>> statementandparent = new ArrayList<Triple<String, String, Integer>>();
	
	static List<Triple<String, String, Integer>> nodetransformationsummary = new ArrayList<Triple<String, String, Integer>>();
	
	static List<Pair<String, Integer>> nodetypestastics = new ArrayList<Pair<String, Integer>>();

	static List<Triple<String, String, Double>> nodetransformationprobability = new ArrayList<Triple<String, String, Double>>();
	
	static List<Pair<String, String>> nodefeature = new ArrayList<Pair<String, String>>();
	
	static List<Quartet<String, String, String, String>> edgefeature = 
            new ArrayList<Quartet<String, String, String, String>>();
	
	static List<Sextet<String, String, String, String, String, String>> trianglefeature = 
            new ArrayList<Sextet<String, String, String, String, String, String>>();
	
	static List<Sextet<String, String, String, String, String, String>> trianglefeaturewithobservation = 
            new ArrayList<Sextet<String, String, String, String, String, String>>();
	
	public static void main(String args[]) { 
		
	   trainingDataDirectory = args[0];
	   CRFSavePath = args[1];
	   BaselineSavePath = args[2];
	   onlyobservation=Boolean.parseBoolean(args[3]);
	   onlyindicator=Boolean.parseBoolean(args[4]);

	   getAllFiles();
	   writetoCRF();
	   establishbaseline();
	}
	
	public static void getAllFiles() {
		
		File[] trainingfolder = new File(trainingDataDirectory).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		        return file.isDirectory();
		    }
		});

		for(int folderindex=0; folderindex<trainingfolder.length; folderindex++ ) {

		   File folder = trainingfolder[folderindex];
		   File[] listOfFiles = folder.listFiles();
		
		   for(int index=0; index<listOfFiles.length; index++) {
			  readSpecificFile(listOfFiles[index].getAbsolutePath());
		   }
		}
	}
 
	public static void readSpecificFile (String fileName) {       
		
	  JSONParser parser = new JSONParser();
      try {
    	 
           Object obj = parser.parse(new FileReader(fileName));

           JSONObject jsonObject = (JSONObject)obj;
            	  
                  JSONArray astList = (JSONArray) (jsonObject.get("faulty_ast")); 
                  
                  JSONObject contextInfo= (JSONObject)(((JSONObject)(jsonObject.get("context"))).get("cntx"));
                  
                  if(astList.size()>0) {
                 
                     JSONObject faulty_ast=(JSONObject)(astList.get(0)); 
                     String nodetype=faulty_ast.get("type").toString();
                                
                     JSONArray actionList = (JSONArray) faulty_ast.get("susp"); 
					 String namefornode = faulty_ast.get("label").toString();
					 
					 String typeofreturn = "";
					 try {
							typeofreturn = jsonObject.get("return_type").toString();
					 } catch (Exception e) {
							typeofreturn = "";
					 }
					 
					 String logicalExpIdentity = "";
					 try {
							logicalExpIdentity = faulty_ast.get("index_of_logical_exper").toString();
						} catch (Exception e) {
							logicalExpIdentity = "";
					  }
                     
                     NodeAST virtualroot=new NodeAST(0, 0, "VIRTUALROOT");
                     virtualroot.setParentNode(null);
					 virtualroot.setTypeofReturn("");
					 virtualroot.setNameofNode("DEFAULT");
					 virtualroot.setLogicalExpressionIdentity("");
                                
                     String virtualtransform="EMPTY";
                 	 ArrayList<String> alltransforms=new ArrayList<String>();
                 	 ArrayList<String> originalname=new ArrayList<String>();

                     String[] roottransformname=getUniqueLabel(actionList);
                     
                     for (int rootindex=0; rootindex<roottransformname.length; rootindex++) {
                    	 String modifyname=modifyTransformName(roottransformname[rootindex]);
                    	 for (VirtualTransforms cha: VirtualTransforms.values()) {
                    		 if(modifyname.equals(cha.toString())) {
                    			 alltransforms.add(modifyname);
                    			 originalname.add(roottransformname[rootindex]);
                    			 break;
                    		} 
      	    		    } 
                     }
                     
                     if(alltransforms.size()>0) {
                    	 virtualtransform=alltransforms.get(0);
                     }
                     virtualroot.setNodeLabel(virtualtransform);
                     TreeAST tree=new TreeAST(virtualroot);
                     tree.addNode(virtualroot);
                         
                     for(int removeindex=0; removeindex<originalname.size();removeindex++) {
                    	 roottransformname = ArrayUtils.removeElement(roottransformname, originalname.get(removeindex));
                     }                   
                                       
                     NodeAST root=new NodeAST(tree.getNodesNumber(), 0, nodetype); 
                     
                     root.setNodeLabel(roottransformname.length>0? modifyTransformName(roottransformname[0]):"EMPTY");
                     root.setParentNode(virtualroot);
					 root.setNameofNode(namefornode);
					 root.setTypeofReturn(typeofreturn);
					 root.setLogicalExpressionIdentity(logicalExpIdentity);

                     virtualroot.addNode(root);
                     tree.addNode(root);
                      
                     readEachJSONNode(faulty_ast, tree, root);
                     
                     for (int i=0;i<tree.getNodesNumber();i++) {
                		 Node toStudy=tree.getNode(i);           		 
                		 if(i!=0) {
                			 if(toStudy.getNodeLabel().equals("addassignment")||toStudy.getNodeLabel().equals("wrapsTryCatch")
                					 ||toStudy.getNodeLabel().equals("wrapsLoop")) 
                				 toStudy.setNodeLabel("EMPTY"); 
                		 }
                	 }
                     
                     if(tree.getNodesNumber()<30 && !checkWhetherEmptyTree(tree)) {
                    	 
                    	 for (int i=0;i<tree.getNodesNumber();i++) {
                    		 Node toStudy=tree.getNode(i);
                			 tryADDNodeFeature(toStudy.getNodeType(),toStudy.getNodeLabel());
                    	 }
                     }
                     
                     if(tree.getNodesNumber()<30 && !checkWhetherEmptyTree(tree)) {
                    	 for (int i=0;i<tree.getNodesNumber();i++) {
                    		 Node toStudy=tree.getNode(i);
                    		                   		 
                    		 for(int j=0; j<toStudy.getOrderedNodesNumber();j++) {
                    			 Node child=toStudy.getOrderedNodeAt(j);
                    			 tryADDEdgeFeature(toStudy.getNodeType(),toStudy.getNodeLabel(),child.getNodeType(),child.getNodeLabel());
                    		 }
                    	 }
                     }      
                     
                     if(tree.getNodesNumber()<30 && !checkWhetherEmptyTree(tree)) {
                    	 for (int i=0;i<tree.getNodesNumber();i++) {
                    		 Node toStudy=tree.getNode(i);           		 
                    		 if(toStudy.getOrderedNodesNumber()>=2) {
                    			 tryADDTriangleFeature(toStudy);
                    			 tryADDTriangleFeatureWithObservation(toStudy);
                    		 }
                    	 }
                     } 
                     
                     if(tree.getNodesNumber()<30 && !checkWhetherEmptyTree(tree)) {
                    	 
                    	 for (int i=0;i<tree.getNodesNumber();i++) {
                    		 Node toStudy=tree.getNode(i);  
                    		 
                    		 establishNodeTypeStastics(toStudy);
                    		 
                    		 if(!toStudy.getNodeLabel().equals("EMPTY")) {
                    			 establishnodetransformsummary (toStudy);
//                    			 if(toStudy.getNodeLabel().equals("unwrapMethod")) {
//                    				
//                    			 }
                    		 }
                    	 }
                     } 
                     
                     if(!allstatementtype.contains(contextInfo.get("S3_TYPE_OF_FAULTY_STATEMENT").toString())) {
                    	  if(!contextInfo.get("S3_TYPE_OF_FAULTY_STATEMENT").toString().isEmpty())
                    	     allstatementtype.add(contextInfo.get("S3_TYPE_OF_FAULTY_STATEMENT").toString());
                     }
                     
                     if(!allparentstatementtype.contains(contextInfo.get("S14_TYPE_OF_FAULTY_STATEMENT_PARENT").toString())) {
                   	    if(!contextInfo.get("S14_TYPE_OF_FAULTY_STATEMENT_PARENT").toString().isEmpty())
                   		   allparentstatementtype.add(contextInfo.get("S14_TYPE_OF_FAULTY_STATEMENT_PARENT").toString());
                     }
                     
                     if(!allstatementtypebefore.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1").toString())) {
                   	   if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1").toString().isEmpty())
                   		  allstatementtypebefore.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1").toString());
                     }
                     
                     if(!allstatementtypebefore.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2").toString())) {
                     	   if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2").toString().isEmpty())
                     		 allstatementtypebefore.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2").toString());
                     }
                     
                     if(!allstatementtypebefore.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3").toString())) {
                   	    if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3").toString().isEmpty())
                   		 allstatementtypebefore.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3").toString());
                     }
                     
                     if(!allstatementtypeafter.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1").toString())) {
                     	 if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1").toString().isEmpty())
                     		allstatementtypeafter.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1").toString());
                     }
                       
                     if(!allstatementtypeafter.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2").toString())) {
                       	if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2").toString().isEmpty())
                           allstatementtypeafter.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2").toString());
                     }
                       
                     if(!allstatementtypeafter.contains(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3").toString())) {
                     	if(!contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3").toString().isEmpty())
                     	   allstatementtypeafter.add(contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3").toString());
                     }  
                     
                     String statementtype=contextInfo.get("S3_TYPE_OF_FAULTY_STATEMENT").toString();
                     String statementparent=contextInfo.get("S14_TYPE_OF_FAULTY_STATEMENT_PARENT").toString();
                     String statementbefore1=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_1").toString();
                     String statementbefore2=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_2").toString();
                     String statementbefore3=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_BEFORE_3").toString();
                     String statementafter1=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_1").toString();
                     String statementafter2=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_2").toString();
                     String statementafter3=contextInfo.get("S13_TYPE_OF_FAULTY_STATEMENT_AFTER_3").toString();
                     
                     Boolean whetherexiststatementandparent=false;
                     
                     if(!statementtype.isEmpty() && !statementparent.isEmpty()) {
              	        for(int indexparent=0; indexparent<statementandparent.size(); indexparent++) {
              	    	 
              		     if(statementandparent.get(indexparent).getLeft().equals(statementtype) && 
              		    		 statementandparent.get(indexparent).getMiddle().equals(statementparent)) {
              		    	 
              		    	statementandparent.set(indexparent, Triple.of(statementtype, statementparent, 
              		    			statementandparent.get(indexparent).getRight()+1));
              		    	whetherexiststatementandparent=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandparent)
                		   statementandparent.add(Triple.of(statementtype, statementparent, 1));
                     }
                     
                     Boolean whetherexiststatementandbefore=false;
                     
                     if(!statementtype.isEmpty() && !statementbefore1.isEmpty()) {
              	        for(int indexbefore1=0; indexbefore1<statementandbefore.size(); indexbefore1++) {
              	    	 
              		     if(statementandbefore.get(indexbefore1).getLeft().equals(statementtype) && 
              		    		statementandbefore.get(indexbefore1).getMiddle().equals(statementbefore1)) {
              		    	 
              		    	statementandbefore.set(indexbefore1, Triple.of(statementtype, statementbefore1, 
              		    			statementandbefore.get(indexbefore1).getRight()+1));
              		    	whetherexiststatementandbefore=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandbefore)
                		   statementandbefore.add(Triple.of(statementtype, statementbefore1, 1));
                     }
                     
                     whetherexiststatementandbefore=false;
                     
                     if(!statementtype.isEmpty() && !statementbefore2.isEmpty()) {
              	        for(int indexbefore2=0; indexbefore2<statementandbefore.size(); indexbefore2++) {
              	    	 
              		     if(statementandbefore.get(indexbefore2).getLeft().equals(statementtype) && 
              		    		statementandbefore.get(indexbefore2).getMiddle().equals(statementbefore2)) {
              		    	 
              		    	statementandbefore.set(indexbefore2, Triple.of(statementtype, statementbefore2, 
              		    			statementandbefore.get(indexbefore2).getRight()+1));
              		    	whetherexiststatementandbefore=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandbefore)
                		   statementandbefore.add(Triple.of(statementtype, statementbefore2, 1));
                     }
                     
                     whetherexiststatementandbefore=false;
                     
                     if(!statementtype.isEmpty() && !statementbefore3.isEmpty()) {
              	        for(int indexbefore3=0; indexbefore3<statementandbefore.size(); indexbefore3++) {
              	    	 
              		     if(statementandbefore.get(indexbefore3).getLeft().equals(statementtype) && 
              		    		statementandbefore.get(indexbefore3).getMiddle().equals(statementbefore3)) {
              		    	 
              		    	statementandbefore.set(indexbefore3, Triple.of(statementtype, statementbefore3, 
              		    			statementandbefore.get(indexbefore3).getRight()+1));
              		    	whetherexiststatementandbefore=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandbefore)
                		   statementandbefore.add(Triple.of(statementtype, statementbefore3, 1));
                     }
                     
                     Boolean whetherexiststatementandafter=false;
                     
                     if(!statementtype.isEmpty() && !statementafter1.isEmpty()) {
              	        for(int indexafter1=0; indexafter1<statementandafter.size(); indexafter1++) {
              	    	 
              		     if(statementandafter.get(indexafter1).getLeft().equals(statementtype) && 
              		    		statementandafter.get(indexafter1).getMiddle().equals(statementafter1)) {
              		    	 
              		    	statementandafter.set(indexafter1, Triple.of(statementtype, statementafter1, 
              		    			statementandafter.get(indexafter1).getRight()+1));
              		    	whetherexiststatementandafter=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandafter)
                		   statementandafter.add(Triple.of(statementtype, statementafter1, 1));
                     }
                     
                     whetherexiststatementandafter=false;
                     
                     if(!statementtype.isEmpty() && !statementafter2.isEmpty()) {
              	        for(int indexafter2=0; indexafter2<statementandafter.size(); indexafter2++) {
              	    	 
              		     if(statementandafter.get(indexafter2).getLeft().equals(statementtype) && 
              		    		statementandafter.get(indexafter2).getMiddle().equals(statementafter2)) {
              		    	 
              		    	statementandafter.set(indexafter2, Triple.of(statementtype, statementafter2, 
              		    			statementandafter.get(indexafter2).getRight()+1));
              		    	whetherexiststatementandafter=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandafter)
                		   statementandafter.add(Triple.of(statementtype, statementafter2, 1));
                     }
                     
                     whetherexiststatementandafter=false;
                     
                     if(!statementtype.isEmpty() && !statementafter3.isEmpty()) {
              	        for(int indexafter3=0; indexafter3<statementandafter.size(); indexafter3++) {
              	    	 
              		     if(statementandafter.get(indexafter3).getLeft().equals(statementtype) && 
              		    		statementandafter.get(indexafter3).getMiddle().equals(statementafter3)) {
              		    	 
              		    	statementandafter.set(indexafter3, Triple.of(statementtype, statementafter3, 
              		    			statementandafter.get(indexafter3).getRight()+1));
              		    	whetherexiststatementandafter=true;
              			    break;
              		     }
              	       }
              	   
                	   if(!whetherexiststatementandafter)
                		   statementandafter.add(Triple.of(statementtype, statementafter3, 1));
                     }       
                  }
      } catch (Exception e) {
        e.printStackTrace();
     }
  }
	
	public static void establishnodetransformsummary (Node toStudy) {
		
		Boolean whetherExist=false;
		
		String nodetype= toStudy.getNodeType();
		String nodetransform = toStudy.getNodeLabel();
		
		if(nodetransform.equals("expLogicExpand") || nodetransform.equals("expLogicReduce"))
			nodetype = "RootLogical";
		
		if(nodetransform.equals("addassignment") || nodetransform.equals("wrapsLoop"))
			whetherExist=true;

		if(!whetherExist) {
	       for (int j=0; j<nodetransformationsummary.size(); j++) {
	    	
	    	   Triple<String, String, Integer> current = nodetransformationsummary.get(j);
	    	
	    	   if(current.getLeft().equals(nodetype) && current.getMiddle().equals(nodetransform)) {
	    		    nodetransformationsummary.set(j, Triple.of(nodetype, nodetransform, 
	    				nodetransformationsummary.get(j).getRight()+1));
	    		    whetherExist=true;
	    		    break;
	    	   }	
	       }
		}
	    
	    if(!whetherExist) {
	    	nodetransformationsummary.add(Triple.of(nodetype, nodetransform, 1));
    	}
	}
	
   public static void establishNodeTypeStastics (Node toStudy) {
		
		Boolean whetherExist=false;
		
		String nodetype= toStudy.getNodeType();
		
		if(!toStudy.getLogicalExpressionIdentity().isEmpty())
			nodetype = "RootLogical";

	    for (int j=0; j<nodetypestastics.size(); j++) {
	    	
	    	Pair<String,  Integer> current = nodetypestastics.get(j);
	    	
	    	if(current.getLeft().equals(nodetype) ) {
	    		nodetypestastics.set(j, Pair.of(nodetype, nodetypestastics.get(j).getRight()+1));
	    		whetherExist=true;
	    		break;
	    	}	
	    }
	    
	    if(!whetherExist) {
	    	nodetypestastics.add(Pair.of(nodetype, 1));
    	}
	}
	
	public static boolean checkWhetherEmptyTree(TreeAST inputtree) {

	 boolean emptytree=true;
   	 for (int i=0;i<inputtree.getNodesNumber();i++) {
   		 Node toStudy=inputtree.getNode(i);		 
   		 if(!toStudy.getNodeLabel().equals("EMPTY")) {
   			emptytree=false;
   			break;
   		 }	 
   	 }
   	 
   	 return emptytree;
  }
	
	public static String modifyTransformName (String inputname) {
		
		if(inputname.equals("susp_wrapsIf_elsenull"))
			inputname="wrapsIf_Others";
        if(inputname.equals("susp_wrapsIfElse_elsenotnull"))
        	inputname="wrapsIfElse_Others";
        
        if(inputname.indexOf("susp_")!=-1) {
        	String[] splittedarray=inputname.split("_");
        	inputname=splittedarray[1];
        }
        	    
        return inputname;
	}
	
	public static void tryADDNodeFeature(String nodeType, String nodeLabel) {

		Boolean whetherExist=false;

	    for (int j=0; j<nodefeature.size(); j++) {
	    	Pair<String, String> current = nodefeature.get(j);
	    	if(current.getLeft().equals(nodeType) && current.getRight().equals(nodeLabel)) {
	    		whetherExist=true;
	    		break;
	    	}	
	    }
	    
	    if(!whetherExist) {
	    	nodefeature.add(Pair.of(nodeType, nodeLabel));
    	}
	}
	
	public static void tryADDTriangleFeature(Node parentnode) {
		
		for(int index=0; index<parentnode.getOrderedNodesNumber()-1; index++) {
			addActualTriangleFeature(parentnode.getNodeType(),parentnode.getNodeLabel(),
					parentnode.getOrderedNodeAt(index).getNodeType(),parentnode.getOrderedNodeAt(index).getNodeLabel()
					,parentnode.getOrderedNodeAt(index+1).getNodeType(),parentnode.getOrderedNodeAt(index+1).getNodeLabel());
		}
	}
	
	public static void addActualTriangleFeature(String parentType, String parentLabel, String leftchildType, 
			String leftchildLabel, String rightchildType, String rightchildLabel) {

		Boolean whetherExist=false;

	    for (int j=0; j<trianglefeature.size(); j++) {
	    	
	    	Sextet<String, String, String, String, String, String> current = trianglefeature.get(j);
	    	if(current.getValue0().equals(parentType)&&current.getValue1().equals(parentLabel)&&
	    			current.getValue2().equals(leftchildType)&&current.getValue3().equals(leftchildLabel)&&
	    			current.getValue4().equals(rightchildType)&&current.getValue5().equals(rightchildLabel)) {
	    		whetherExist=true;
	    		break;
	    	}	
	    }
	    
	    if(!whetherExist) {
	    	trianglefeature.add(Sextet.with(parentType, parentLabel, leftchildType, leftchildLabel, rightchildType, rightchildLabel));
    	}
	}
	
    private static boolean whetherNodeHasSameContent(Node leftchildnode, Node rightchildnode) {
    	
    	if(leftchildnode.getNameofNode().equals(rightchildnode.getNameofNode()))
			return true;
    	else {
    		if(leftchildnode.getNodeType().equals("TypeAccess")) {
    			if(isTypeAccessActualVar(leftchildnode.getNameofNode()) && isTypeAccessActualVar(rightchildnode.getNameofNode()))
					return true;
    		}
    		
    		if(leftchildnode.getNodeType().equals("Literal") && !leftchildnode.getTypeofReturn().trim().isEmpty() &&
    				!rightchildnode.getTypeofReturn().trim().isEmpty()) {
    			if(leftchildnode.getTypeofReturn().trim().toLowerCase().equals(rightchildnode.getTypeofReturn().trim().toLowerCase()) ||
    			   leftchildnode.getTypeofReturn().trim().toLowerCase().endsWith(rightchildnode.getTypeofReturn().trim().toLowerCase())||
    			   rightchildnode.getTypeofReturn().trim().toLowerCase().endsWith(leftchildnode.getTypeofReturn().trim().toLowerCase()))
            		return true;
    		}
    	}
		
		return false;
	}
    
    private static boolean isTypeAccessActualVar(String name) {
		
		String[] splitname = name.split("\\.");
		if (splitname.length>1) {
			String simplename=splitname[splitname.length-1];
			if (simplename.toUpperCase().equals(simplename)) 
				return true;
		}		
		
		return false;
	}
	
    public static void tryADDTriangleFeatureWithObservation (Node parentnode) {
		
		for(int index=0; index<parentnode.getOrderedNodesNumber()-1; index++) {
			
			if(parentnode.getOrderedNodeAt(index).getNodeType().
					equals(parentnode.getOrderedNodeAt(index+1).getNodeType())) {
				
				if(whetherNodeHasSameContent(parentnode.getOrderedNodeAt(index), parentnode.getOrderedNodeAt(index+1)))
					addActualTriangleFeatureWithObservation (parentnode.getNodeType(),parentnode.getNodeLabel(),
							parentnode.getOrderedNodeAt(index).getNodeType(),parentnode.getOrderedNodeAt(index).getNodeLabel()
							,parentnode.getOrderedNodeAt(index+1).getNodeType(),parentnode.getOrderedNodeAt(index+1).getNodeLabel());
			}
		}
	}
    
    public static void addActualTriangleFeatureWithObservation (String parentType, String parentLabel, String leftchildType, 
			String leftchildLabel, String rightchildType, String rightchildLabel) {

		Boolean whetherExist=false;

	    for (int j=0; j<trianglefeaturewithobservation.size(); j++) {
	    	
	    	Sextet<String, String, String, String, String, String> current = trianglefeaturewithobservation.get(j);
	    	if(current.getValue0().equals(parentType) && current.getValue1().equals(parentLabel) &&
	    			current.getValue2().equals(leftchildType) && current.getValue3().equals(leftchildLabel) &&
	    			current.getValue4().equals(rightchildType) && current.getValue5().equals(rightchildLabel)) {
	    		whetherExist=true;
	    		break;
	    	}	
	    }
	    
	    if(!whetherExist) {
	    	trianglefeaturewithobservation.add(Sextet.with(parentType, parentLabel, leftchildType, 
	    			leftchildLabel, rightchildType, rightchildLabel));
    	}
	}
	
	public static void tryADDEdgeFeature(String parentType, String parentLabel, String childType, String childLabel) {

		Boolean whetherExist=false;

	    for (int j=0; j<edgefeature.size(); j++) {
	    	Quartet<String, String, String, String> current = edgefeature.get(j);
	    	if(current.getValue0().equals(parentType) && current.getValue1().equals(parentLabel) &&
	    			current.getValue2().equals(childType) && current.getValue3().equals(childLabel)) {
	    		whetherExist=true;
	    		break;
	    	}	
	    }
	    
	    if(!whetherExist) {
    		edgefeature.add(Quartet.with(parentType, parentLabel, childType, childLabel));
    	}
	}
	
   public static void readEachJSONNode(JSONObject faulty_node, TreeAST ast, Node parentNode) {
	
       JSONArray childrenList = (JSONArray) (faulty_node.get("children")); 
       
       ArrayList<NodeAST> parentnodelist=new ArrayList<NodeAST>();
       
       if(childrenList.size()>0) {
    	   for(int index=0; index<childrenList.size(); index++) {
    		   
               String nodetype=((JSONObject)(childrenList.get(index))).get("type").toString();
			   String namefornode = ((JSONObject)(childrenList.get(index))).get("label").toString();
			   
			    String typeofreturn = "";
				try {
					typeofreturn = ((JSONObject)(childrenList.get(index))).get("return_type").toString();
				} catch (Exception e) {
					typeofreturn = "";
				}
				
				String logicalExpIdentity = "";
				try {
					logicalExpIdentity = ((JSONObject)(childrenList.get(index))).get("index_of_logical_exper").toString();
				} catch (Exception e) {
					logicalExpIdentity = "";
				}

               JSONArray actionListinner = (JSONArray) ((JSONObject)(childrenList.get(index))).get("susp"); 
               
    		   NodeAST currentnode=new NodeAST(ast.getNodesNumber(), 0, nodetype);
    		   currentnode.setParentNode(parentNode);
    		   currentnode.setNodeLabel(modifyTransformName(getUniqueLabel(actionListinner)[0]));
			   currentnode.setNameofNode(namefornode);
			   currentnode.setTypeofReturn(typeofreturn);
			   currentnode.setLogicalExpressionIdentity(logicalExpIdentity);

    		   parentNode.addNode(currentnode);

    		   ast.addNode(currentnode);	
    		   
    		   parentnodelist.add(currentnode);
    	   }
    	   
    	   for(int index=0; index<childrenList.size(); index++) {
    		   
    		   Node parentnode=parentnodelist.get(index);
    		   JSONObject innernode= ((JSONObject)(childrenList.get(index)));
               readEachJSONNode(innernode, ast, parentnode); 
    	   }
       }
   }
   
   public static String[] getUniqueLabel(JSONArray actionList) {
	   
	   if(actionList!=null) {
	   
	      String trimmed=actionList.toString().replaceAll("\\]", "").replaceAll("\\[", "").replaceAll("\"", "");
	   	  String[] splitted=trimmed.split(",");
	   	  splitted = Arrays.stream(splitted).distinct().toArray(String[]::new);

	   	  splitted=postProcessingTransform(splitted);
	   	  
	   	  if(splitted.length>0)
	   	      return splitted;
	   	  else return new String[]{"EMPTY"};
	   	  
	   } 
	   else return new String[]{"EMPTY"};
	   
   }
   
   public static String[] postProcessingTransform(String[] inputtransform) {
	   
	   // some special processing for "susp_unwrapMethod"
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapMethod");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");   
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapMethod") 
			   && ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
	   }
	   
	   // pot-process null-check related transforms
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
		   inputtransform = add(inputtransform, "wrapsIf_NULL");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull")) {  
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		   inputtransform = add(inputtransform, "wrapsIf_NULL");
	   }

	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
		   inputtransform = add(inputtransform, "wrapsIfElse_NULL");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
			   && ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
		   inputtransform = add(inputtransform, "wrapsIfElse_NULL");
	   }
	  
       // post-process a few remaining unnormbal susp_missNullCheckP or susp_missNullCheckN
	   if(inputtransform.length==1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckP"))
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
	   
	   if(inputtransform.length==1 && ArrayUtils.contains(inputtransform, "susp_missNullCheckN"))
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
	   
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
			   && ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
	   }
	   
	   // post0process a few remaining unnorml wrap if and unwrap if related transforms
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse") &&
                  ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse") &&
               ArrayUtils.contains(inputtransform, "wrapsIfElse_NULL")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "wrapsIfElse_NULL");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull") &&
               ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   // post-process a few unnormal susp_wrapsTryCatch and wrapsIf
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
               ArrayUtils.contains(inputtransform, "wrapsIf_NULL")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
               ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsTryCatch");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
               ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenull")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenull");
	   }
	   
	   // post-process a few remaining unnormal susp_wrapsTryCatch and susp_wrapsLoop 
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsTryCatch") &&
               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsLoop");
	   }
	   
	   // post-process a few remaining unnormal susp_wrapsIfElse_elsenotnull and susp_addassignment 
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_addassignment")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull") &&
               ArrayUtils.contains(inputtransform, "susp_addassignment")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_addassignment");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation") &&
               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Constructor") &&
               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Constructor");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsMethod_VarRead_Invocation") &&
               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsMethod_VarRead_Invocation");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_unwrapIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_unwrapIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIf_elsenull") &&
               ArrayUtils.contains(inputtransform, "susp_wrapsLoop")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIf_elsenull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrapsIfElse_elsenotnull") &&
               ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrapsIfElse_elsenotnull");
	   }
	   
	   // post-process superaccess
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_VariableRead");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_TypeAccess");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldWrite");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableWrite");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_ConstructorCall");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
	   }
	   
	   //uniform different sub-patterns to the super pattern
	   //start var replace by var
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_TypeAccess");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_VariableRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_FieldWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_FieldWrite");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Update_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Update_VariableWrite");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Literal")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Literal");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Literal")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Literal");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_FieldWrite");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_VariableRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_TypeAccess");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_FieldRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_TypeAccess");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_VariableWrite");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_FieldRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_TypeAccess");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_SuperAccess_Added_FieldRead");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_VariableRead");
		 //  inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_VariableWrite");
		  // inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_FieldWrite");
		 //  inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Literal")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Literal");
		   inputtransform = add(inputtransform, "VAR_RW_VAR");
	   }
	   // end var replacement by var
	   
	   // start var replacment by method
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_Invocation");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_Invocation");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableRead_Added_ConstructorCall");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_Invocation");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_TypeAccess_Added_ConstructorCall");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldRead_Added_ConstructorCall");
		   inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_FieldWrite_Added_Invocation");
		 //  inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongVarRef_Removed_VariableWrite_Added_Invocation");
		 //  inputtransform = add(inputtransform, "VAR_RW_Method");
	   }
	   // end var replacement by method
	   
	   // start method replace by method 
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_SameNamedifferentArgument");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_differentMethodName")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_differentMethodName");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Invocation");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Invocation");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_ConstructorCall");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_ConstructorCall");
		   inputtransform = add(inputtransform, "Method_RW_Method");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_Invocation");
		 //  inputtransform = add(inputtransform, "Method_RW_Method");
	   }   
	   // end method replace by method 

	   //start method replace by var
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_VariableRead");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_FieldRead");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableRead");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Literal")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_Literal");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_FieldRead");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_Literal");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_NewClass_Added_FieldRead");
		 //  inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_TypeAccess");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_Invocation_Added_TypeAccess");
		   inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite")) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_wrongMethodRef_Removed_ConstructorCall_Added_VariableWrite");
		 //  inputtransform = add(inputtransform, "Method_RW_Var");
	   }
	   //end method replace by var
	    
	   // additional postprocessing to remove some unnormal transforms associated with nodes
	   
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckP") 
			   && inputtransform.length>=2) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckP");
	   }
	   
	   if(ArrayUtils.contains(inputtransform, "susp_missNullCheckN") 
			   && inputtransform.length>=2) {
		   inputtransform = ArrayUtils.removeElement(inputtransform, "susp_missNullCheckN");
	   }
	   
	   List<String> transformtoremove = new ArrayList<String>();
	   for (int index=0; index<inputtransform.length; index++) {
		   
		   String tostudy=inputtransform[index];
		   if(tostudy.indexOf("susp_wrongVarRef")!=-1)
			   transformtoremove.add(tostudy);
		   
		   if(tostudy.indexOf("susp_wrongMethodRef")!=-1)
			   transformtoremove.add(tostudy);
	   }
	   
	   for(int index=0; index<transformtoremove.size(); index++) {
		   if(ArrayUtils.contains(inputtransform, transformtoremove.get(index))) {
			   inputtransform = ArrayUtils.removeElement(inputtransform, transformtoremove.get(index));
		   }
	   }
	   
	   return inputtransform;  
   }
   
	public static String[] add(String[] originalArray, String newItem)
	{
	    int currentSize = originalArray.length;
	    int newSize = currentSize + 1;
	    String[] tempArray = new String[ newSize ];
	    for (int i=0; i < currentSize; i++)
	    {
	        tempArray[i] = originalArray [i];
	    }
	    tempArray[newSize- 1] = newItem;
	    return tempArray;   
	}
         
        
    public static void establishbaseline() {
	   
	     String baselinemodel= BaselineSavePath;
	     
	     for(int index=0; index<nodetransformationsummary.size(); index++) {
	    	 
		    	Triple<String, String, Integer> current = nodetransformationsummary.get(index);
		    	
		    	String nodetype=current.getLeft();
		    	String transformname= current.getMiddle();
		    	int transformnumber = current.getRight();
		    	int nodetotalnumber =0;
		    	
		    	for(int innerindex=0; innerindex<nodetypestastics.size(); innerindex++) {
		    		if(nodetypestastics.get(innerindex).getLeft().equals(nodetype)) {
		    			nodetotalnumber=nodetypestastics.get(innerindex).getRight();
		    		}
		    	}
		    	
		    	double probability = ((double) transformnumber) / ((double)nodetotalnumber);
		    	
		    	nodetransformationprobability.add(Triple.of(nodetype, transformname, probability));
	     }
	   
	     try {

	    	 Collections.sort(nodetransformationprobability, new Comparator<Triple<String, String, Double>>() {
	  		    @Override
	  		    public int compare(Triple<String, String, Double> lhs, Triple<String, String, Double> rhs) {
	 		        return lhs.getRight() > rhs.getRight() ? -1 : (lhs.getRight()< rhs.getRight()) ? 1 : 0;
	  		    }
	  		}); 
	    	 
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(baselinemodel, true)));
		    
		    for(int index=0;index<nodetransformationprobability.size(); index++) {
		    	String content= nodetransformationprobability.get(index).getLeft()+ "   "+
		    nodetransformationprobability.get(index).getMiddle() +
		    			"   "+ nodetransformationprobability.get(index).getRight();
			    out.println(content); 
		    }	       
		    out.close();	    
		 } catch (IOException e) {
		    //exception handling left as an exercise for the reader
	     }
    }
   
   public static void writetoCRF() {
	   
	   Collections.sort(statementandbefore, new Comparator<Triple<String, String, Integer>>() {
		    @Override
		    public int compare(Triple<String, String, Integer> lhs, Triple<String, String, Integer> rhs) {
		        return lhs.getRight() > rhs.getRight() ? -1 : (lhs.getRight()< rhs.getRight()) ? 1 : 0;
		    }
		}); 
	   
	   Collections.sort(statementandafter, new Comparator<Triple<String, String, Integer>>() {
		    @Override
		    public int compare(Triple<String, String, Integer> lhs, Triple<String, String, Integer> rhs) {
		        return lhs.getRight() > rhs.getRight() ? -1 : (lhs.getRight()< rhs.getRight()) ? 1 : 0;
		    }
		}); 
	   
	   Collections.sort(statementandparent, new Comparator<Triple<String, String, Integer>>() {
		    @Override
		    public int compare(Triple<String, String, Integer> lhs, Triple<String, String, Integer> rhs) {
		        return lhs.getRight() > rhs.getRight() ? -1 : (lhs.getRight()< rhs.getRight()) ? 1 : 0;
		    }
		}); 
	   
	   double initialweight=0.0D; 
	   	   
	   JsonArrayBuilder buildercharacter = Json.createArrayBuilder();
	   
	   for (CharacteristicMethod cha: CharacteristicMethod.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   }
	   
	   for (CharacteristicVar cha: CharacteristicVar.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicConstant cha: CharacteristicConstant.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicStat cha: CharacteristicStat.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicLogExp cha: CharacteristicLogExp.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicConstructor cha: CharacteristicConstructor.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicTypeAccess cha: CharacteristicTypeAccess.values()) {	   
		   buildercharacter.add(cha.toString()); 
	   } 
	   
	   for (CharacteristicBinaryOperator cha: CharacteristicBinaryOperator.values()) {
		   buildercharacter.add(cha.toString()); 
	   } 
	   
       for(String statementtype: allstatementtype) {
		   buildercharacter.add("S3_IS_" + statementtype); 
       }
	   
       for(String statementtype: allparentstatementtype) {
		   buildercharacter.add("S14_IS_" + statementtype); 
       }
	   
       for(String statementtypebefore: allstatementtypebefore) {
		   buildercharacter.add("S13_Before_HAS_"+ statementtypebefore); 
       }
	   
       for(String statementtypeafter: allstatementtypeafter) {
		   buildercharacter.add("S13_After_HAS_"+ statementtypeafter); 
       }
		   
	   int beforetoconsider=0;
		   
	   if(statementandbefore.size()<=100)
		   beforetoconsider = statementandbefore.size();
	   else beforetoconsider =100;
	   
	   for (int index=0; index<beforetoconsider; index++) {
		   buildercharacter.add("S3_IS_" + statementandbefore.get(index).getLeft() + 
				   "_S13_Before_HAS_"+ statementandbefore.get(index).getMiddle()); 
	   }	   
		   
	   int aftertoconsider=0;
		   
	   if(statementandafter.size()<=100)
		   aftertoconsider = statementandafter.size();
	   else aftertoconsider = 100;
		   
	   for (int index=0; index<aftertoconsider; index++) {

		   buildercharacter.add("S3_IS_" + statementandafter.get(index).getLeft() + 
				   "_S13_After_HAS_"+ statementandafter.get(index).getMiddle()); 
	   }	   
		   
	   int parenttoconsider=0;
		   
	   if(statementandparent.size()<=100)
		   parenttoconsider = statementandparent.size();
	   else parenttoconsider =100;
		   
	   for (int index=0; index<parenttoconsider; index++) {

		   buildercharacter.add("S3_IS_" + statementandparent.get(index).getLeft() + 
				   "_S14_IS_"+ statementandparent.get(index).getMiddle()); 	   
	   }	   
  
	  JsonArray arrcharacter = buildercharacter.build();
	  
	  JsonArray arrObservationFeature = establishObservationFeature(false).build();
	  	   
	  JsonArray arrObservationInverseFeature = establishObservationFeature(true).build();
	  
	   String[] originalarray=new String[nodefeature.size()];
	   for(int arrayindex=0; arrayindex<nodefeature.size(); arrayindex++)
		   originalarray[arrayindex]=nodefeature.get(arrayindex).getRight();
	   Set<String> uniqTransform = new TreeSet<String>();
	   uniqTransform.addAll(Arrays.asList(originalarray));
	   
	   JsonArrayBuilder builderlabel = Json.createArrayBuilder();
	   
	   for(String x : uniqTransform) {
		   builderlabel.add(x);  
	   }
	   
	   JsonArray arrlabel = builderlabel.build();
	   
	   String[] originalnodetype=new String[nodefeature.size()];
	   for(int arrayindex=0; arrayindex<nodefeature.size(); arrayindex++)
		   originalnodetype[arrayindex]=nodefeature.get(arrayindex).getLeft();
	   Set<String> uniqNodeType = new TreeSet<String>();
	   uniqNodeType.addAll(Arrays.asList(originalnodetype));
	   
	   JsonObjectBuilder builderNodeTransform=Json.createObjectBuilder();

	   for(String nodeType : uniqNodeType) {
		   List<String> transformForNode = new ArrayList<String>();
		   for(int indexall=0; indexall<nodefeature.size();indexall++) {
			   if(nodefeature.get(indexall).getLeft().equals(nodeType))
				   transformForNode.add(nodefeature.get(indexall).getRight());
		   }
		   JsonArrayBuilder buildertempory = Json.createArrayBuilder();
		   
		   for(int index=0; index<transformForNode.size(); index++) {
			   buildertempory.add(transformForNode.get(index));  
			}
		   JsonArray arrtempory = buildertempory.build();
		   builderNodeTransform.add(nodeType, arrtempory);
		}
	   
	   JsonObject nodeTransformConstraint=builderNodeTransform.build();
	   
	   JsonArrayBuilder builderNodeFeature = Json.createArrayBuilder();
	   
	   for (int index=0; index < nodefeature.size(); index++) {	   
		   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
		   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
		   .add("NodeLabel", nodefeature.get(index).getRight())
		   .add("Weight", initialweight)
		   .build();   
		   builderNodeFeature.add(objecttempory); 
       }
	   JsonArray arrNodeFeature = builderNodeFeature.build();
	   
	   JsonArrayBuilder builderEdgeFeature = Json.createArrayBuilder();
	   
	   for (int index=0; index<edgefeature.size(); index++) {	   
		   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
		   JsonObject objecttempory = buildertempory.add("ParentNodeType", edgefeature.get(index).getValue0())
		   .add("ParentNodeLabel", edgefeature.get(index).getValue1())
		   .add("ChildNodeType", edgefeature.get(index).getValue2())
		   .add("ChildNodeLabel", edgefeature.get(index).getValue3())
		   .add("Weight", initialweight)
		   .build();
		   
		   builderEdgeFeature.add(objecttempory); 
       }
	   JsonArray arrEdgeFeature = builderEdgeFeature.build();
	   	   
       JsonArrayBuilder builderTriangleFeature = Json.createArrayBuilder();
	   
	   for (int index=0; index<trianglefeature.size(); index++) {	   
		   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
		   JsonObject objecttempory = buildertempory.add("ParentNodeType", trianglefeature.get(index).getValue0())
		   .add("ParentNodeLabel", trianglefeature.get(index).getValue1())
		   .add("LeftChildNodeType", trianglefeature.get(index).getValue2())
		   .add("LeftChildNodeLabel", trianglefeature.get(index).getValue3())
		   .add("RightChildNodeType", trianglefeature.get(index).getValue4())
		   .add("RightChildNodeLabel", trianglefeature.get(index).getValue5())
		   .add("Weight", initialweight)
		   .build();
		   
		   builderTriangleFeature.add(objecttempory); 
       }
	   JsonArray arrTriangleFeature = builderTriangleFeature.build();
	   
       JsonArrayBuilder builderTriangleFeatureWithObservation = Json.createArrayBuilder();
	   
	   for (int index=0; index<trianglefeaturewithobservation.size(); index++) {	   
		   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
		   JsonObject objecttempory = buildertempory.add("ParentNodeType", trianglefeaturewithobservation.get(index).getValue0())
		   .add("ParentNodeLabel", trianglefeaturewithobservation.get(index).getValue1())
		   .add("LeftChildNodeType", trianglefeaturewithobservation.get(index).getValue2())
		   .add("LeftChildNodeLabel", trianglefeaturewithobservation.get(index).getValue3())
		   .add("RightChildNodeType", trianglefeaturewithobservation.get(index).getValue4())
		   .add("RightChildNodeLabel", trianglefeaturewithobservation.get(index).getValue5())
		   .add("Weight", initialweight)
		   .build();
		   
		   builderTriangleFeatureWithObservation.add(objecttempory); 
       }
	   JsonArray arrTriangleFeatureWithObservation = builderTriangleFeatureWithObservation.build();   
	   
	   JsonObject crfObject = null;
	   if(!onlyobservation && !onlyindicator) {
	       crfObject = Json.createObjectBuilder()
               .add("Label", arrlabel)
               .add("Character", arrcharacter)
               .add("Constraint", nodeTransformConstraint)
               .add("ObservationFeature", arrObservationFeature)
               .add("ObservationInverseFeature", arrObservationInverseFeature)
			   .add("NodeFeature", arrNodeFeature)
			   .add("EdgeFeature", arrEdgeFeature)
			   .add("TriangleFeature", arrTriangleFeature)
			   .add("TriangleFeatureWithObservation", arrTriangleFeatureWithObservation)
			   .build(); 
	   } else if(onlyobservation && !onlyindicator) {
	       crfObject = Json.createObjectBuilder()
               .add("Label", arrlabel)
               .add("Character", arrcharacter)
               .add("Constraint", nodeTransformConstraint)
               .add("ObservationFeature", arrObservationFeature)
               .add("ObservationInverseFeature", arrObservationInverseFeature)
			   .build(); 
	   } else if(!onlyobservation && onlyindicator) {
	       crfObject = Json.createObjectBuilder()
	               .add("Label", arrlabel)
	               .add("Character", arrcharacter)
	               .add("Constraint", nodeTransformConstraint)
				   .add("NodeFeature", arrNodeFeature)
				   .add("EdgeFeature", arrEdgeFeature)
				   .add("TriangleFeature", arrTriangleFeature)
				   .add("TriangleFeatureWithObservation", arrTriangleFeatureWithObservation)
				   .build(); 
	   } else {
		   System.out.println("Wrong Configuration!");
	   }

	   Map<String, Boolean> config = new HashMap<>();
	   config.put(JsonGenerator.PRETTY_PRINTING, true);
	   try (PrintWriter pw = new PrintWriter(CRFSavePath)
	    ; JsonWriter jsonWriter = Json.createWriterFactory(config).createWriter(pw)) {
	      jsonWriter.writeObject(crfObject);
	   } catch (FileNotFoundException e1) {
		  e1.printStackTrace();
	   }
   }
   
   private static JsonArrayBuilder establishObservationFeature (boolean wheteherinverse) {
	   
	   double initialweight=0.0D; 
	   
	   JsonArrayBuilder builderobservation = Json.createArrayBuilder();

	   for(int index=0;index<nodefeature.size(); index++) {
		   
	    	String content= nodefeature.get(index).getLeft();
	    	
	    	if(content.equals("Invocation")) {
	    		for (CharacteristicMethod cha: CharacteristicMethod.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    	
	       if(content.equals("ConstructorCall")) {
	    		for (CharacteristicConstructor cha: CharacteristicConstructor.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    	
	    	if(content.equals("VariableRead")||content.equals("VariableWrite")||
	    			content.equals("FieldRead")||content.equals("FieldWrite")) {
	    		for (CharacteristicVar cha: CharacteristicVar.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    	
	    	if(content.equals("Literal")) {
	    		for (CharacteristicConstant cha: CharacteristicConstant.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    	
	    	if(content.equals("TypeAccess")) {
	    		for (CharacteristicTypeAccess cha: CharacteristicTypeAccess.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    	
	    	if(content.equals("BinaryOperator")) {
	    		for (CharacteristicBinaryOperator cha: CharacteristicBinaryOperator.values()) {
	    			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
	    			   JsonObject objecttempory = buildertempory.add("NodeType", nodefeature.get(index).getLeft())
	    					   .add("Characteristic", cha.toString())
	    					   .add("NodeLabel", nodefeature.get(index).getRight())
	    					   .add("Weight", initialweight)
	    					   .build();   
	    			   builderobservation.add(objecttempory); 
	    		}
	    	}
	    }
       
       for(VirtualTransforms tran: VirtualTransforms.values()) {
    	   
    	   for (CharacteristicStat cha: CharacteristicStat.values()) { // feature functions built according to the listed statement features
			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			   JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
					   .add("Characteristic", cha.toString())
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			   builderobservation.add(objecttempory); 
		   }	   
    	   
    	   if(!wheteherinverse)
    	   for(String statementtype: allstatementtype) { //type of faulty statement

			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			   JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
					   .add("Characteristic", "S3_IS_" + statementtype )
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			   builderobservation.add(objecttempory); 	   
    	   }
    	   
    	   if(!wheteherinverse)
    	   for(String statementtype: allparentstatementtype) { //type of parent of faulty statement

			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			   JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
					   .add("Characteristic", "S14_IS_" + statementtype )
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			   builderobservation.add(objecttempory); 	   
    	   }
    	   
    	   if(!wheteherinverse)
    	   for(String statementtypebefore : allstatementtypebefore) {
    		   
			      JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			      JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
					   .add("Characteristic", "S13_Before_HAS_"+ statementtypebefore)
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			      builderobservation.add(objecttempory); 
 		   }
    	   
    	   if(!wheteherinverse)
    	   for(String statementtypeafter : allstatementtypeafter) {
    		   
			      JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			      JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
					   .add("Characteristic", "S13_After_HAS_"+ statementtypeafter)
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			      builderobservation.add(objecttempory); 
 		   }
    	   
    	   if(!wheteherinverse) {
    		   
    		   int beforetoconsider=0;
    		   
    		   if(statementandbefore.size()<=100)
    			   beforetoconsider = statementandbefore.size();
    		   else beforetoconsider =100;
    		   
    		   for (int index=0; index<beforetoconsider; index++) {
    				JsonObjectBuilder buildertempory=Json.createObjectBuilder();
    				JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
    						   .add("Characteristic", "S3_IS_" + statementandbefore.get(index).getLeft() + 
    								   "_S13_Before_HAS_"+ statementandbefore.get(index).getMiddle())
    						   .add("NodeLabel", tran.toString())
    						   .add("Weight", initialweight)
    						   .build();   
    				builderobservation.add(objecttempory); 
    		   }	   
    	   }
    	   
           if(!wheteherinverse) {
    		   
    		   int aftertoconsider=0;
    		   
    		   if(statementandafter.size()<=100)
    			   aftertoconsider = statementandafter.size();
    		   else aftertoconsider = 100;
    		   
    		   for (int index=0; index<aftertoconsider; index++) {
    				   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
    				   JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
    						   .add("Characteristic", "S3_IS_" + statementandafter.get(index).getLeft() + 
    								   "_S13_After_HAS_"+ statementandafter.get(index).getMiddle())
    						   .add("NodeLabel", tran.toString())
    						   .add("Weight", initialweight)
    						   .build();  
    				   builderobservation.add(objecttempory); 
    		   }	   
    	   }
           
          if(!wheteherinverse) {
    		   
    		   int parenttoconsider=0;
    		   
    		   if(statementandparent.size()<=100)
    			   parenttoconsider = statementandparent.size();
    		   else parenttoconsider =100;
    		   
    		   for (int index=0; index<parenttoconsider; index++) {
    				   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
    				   JsonObject objecttempory = buildertempory.add("NodeType", "VIRTUALROOT")
    						   .add("Characteristic", "S3_IS_" + statementandparent.get(index).getLeft() + 
    								   "_S14_IS_"+ statementandparent.get(index).getMiddle())
    						   .add("NodeLabel", tran.toString())
    						   .add("Weight", initialweight)
    						   .build();  
    				   builderobservation.add(objecttempory); 
    		   }	   
    	   }	   
      }  
       
      for(LogicalTransforms tran: LogicalTransforms.values()) {
    	   
    	   for (CharacteristicLogExp cha: CharacteristicLogExp.values()) {
			   JsonObjectBuilder buildertempory=Json.createObjectBuilder();
			   JsonObject objecttempory = buildertempory.add("NodeType", "RootLogical")
					   .add("Characteristic", cha.toString())
					   .add("NodeLabel", tran.toString())
					   .add("Weight", initialweight)
					   .build();   
			   builderobservation.add(objecttempory); 
		   }
      }
      
      return builderobservation;
   }
}