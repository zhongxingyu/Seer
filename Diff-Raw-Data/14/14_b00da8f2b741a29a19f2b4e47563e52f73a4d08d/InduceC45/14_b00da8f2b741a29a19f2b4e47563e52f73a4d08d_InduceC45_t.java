 package src;
 import java.io.*;
 import java.util.ArrayList;
 
 import org.w3c.dom.Document;
 
 
 public class InduceC45{
     public static void main(String [ ] args){
         //java InduceC45 <domainFile.xml> <TrainingSetFile.csv> [<restrictionsFile>]
     	
     	//get csv data
    	csvInfo csvAL = fileParser.parseCSV("data/tree01-100-numbers.csv");
     	//System.out.println(csvAL);  
 
     	//Document doc =  fileParser.parseXMLDomain("data/custom.xml");
     	
     	
     	DecisionTreeNode result = C45(csvAL.dataSets, csvAL.attributes, 0.1, csvAL.categoryNumber);
 
     	System.out.println(DecisionTreeNode.printTree(result,0));
     	
     	
     }
     
     public static ArrayList<String> parseDoc(){
 		return null;
     }
     
     public static void decisionTreeNodeToXML(String fileName, DecisionTreeNode tree){
     	FileOutputStream out = null; // declare a file output object
         PrintStream p = null; // declare a print stream object
         try{
         	out = new FileOutputStream(fileName);
         	p = new PrintStream( out );
 
         }
         catch(Exception e){
         	
         }
         
         p.println ("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
         p.println ("<Tree name = \"lab3\">");
         
     }
     
     public static DecisionTreeNode C45(ArrayList<Data> dataSet, 
             ArrayList<Integer> attributes, double threshold, int catNum){
     	
     	//if there is only one category left
     	if(oneCategoryLeft(dataSet)){
     		//System.out.println("Only one category left!");
     		//make a node and label it as the category, add it to the tree, then return the tree
     		//public DecisionTreeNode(int id, int node, int edge, int edgeChoice, int decision){
     		return new DecisionTreeNode(-1, -1, -1, -1, dataSet.get(0).category);
     	}
     	//else if there are no more attributes, find the most common category and make a node,
     	//name it the most common cat, add it to the tree, reutrn the tree
     	else if(noMoreAttributes(attributes)){
     		//System.out.println("No more attribute to split on!");
     		int commCat = commonCategory(catNum, dataSet);
     		return new DecisionTreeNode(-1, -1, -1, -1, commCat);
     	}
     	else{
     		//find the splitting attribute
     		int splitAtt = selectSplittingAttributeIG(catNum,attributes, dataSet, threshold);
     		
     		if(splitAtt == -1){
     			//System.out.println("Couldn't find a good splitting attribute!");
     	    	//else if there is no good attribute to split on, find the most common category 
     			//and make a node, name it the most common cat, add it to the tree, reutrn the tree
         		int commCat = commonCategory(catNum, dataSet);
         		return new DecisionTreeNode(-1, -1, -1, -1, commCat);
     		}
     		//actual tree construction
     		else{
     			//System.out.println("Created a split node at "+splitAtt+"!");
     			//System.out.println("attributes="+attributes);
     			//System.out.println("dataSet:");
     			for(int i = 0; i < dataSet.size(); i++){
     				//System.out.println(dataSet.get(i));
     			}
     			
     			//node used 
     			DecisionTreeNode tempRoot = new DecisionTreeNode();
                 //label the node splitting attribute
     			tempRoot.node = splitAtt;
     			ArrayList<ArrayList<Data>> tempAL = new ArrayList<ArrayList<Data>>();
     			
     			//for each dataset split by splitAtt, recurse C45 and append each of the result
     			for(int attLoop = 1; attLoop <= attributes.get(splitAtt); attLoop++){
     				tempAL.add(new ArrayList<Data>());
     				// if the attribute matches with the current loop, add it to the list
     				for(int dataLoop = 0; dataLoop < dataSet.size(); dataLoop++){
         				if(dataSet.get(dataLoop).dataSets.get(splitAtt) == attLoop){
         					Data tempData = new Data(dataSet.get(dataLoop));
         					//tempData.dataSets.remove(splitAtt);
 
         					tempAL.get(attLoop-1).add(tempData);
             				//System.out.println("Group after "+attLoop+":"+tempData);
         				}
     				}
     			}
     			//remove the now split (used) attribute
     			attributes.set(splitAtt, -1);
     			
     			//System.out.println("attributes="+attributes);
     			//System.out.println("tempAL="+tempAL);
     			//System.out.println("tempAL.size()="+tempAL.size());
     			
     			DecisionTreeNode node = new DecisionTreeNode();
     			node.node = splitAtt+1;
 				for(int tempALLoop = 0; tempALLoop < tempAL.size(); tempALLoop++){
					if(tempAL.get(tempALLoop).size() == 0){
						//System.out.println("No more attributes left!");
						continue;
					}
 					//System.out.println("recursively calling="+tempALLoop);
 					//System.out.println("tempAL.get(tempALLoop)="+tempAL.get(tempALLoop));
 					
 					DecisionTreeNode edge = new DecisionTreeNode();
 					edge.edge = tempALLoop+1;
 					node.addNode(edge);
 					DecisionTreeNode tempNode = C45(tempAL.get(tempALLoop), attributes, threshold, catNum);
 					edge.addNode(tempNode);
 				}
 				return node;
     		}
     	}
     }
     
 
 	//given number of category, list of attributes, list of data, return the enthropy
     public static double enthropy(int catNum, ArrayList<Integer> attributes,  ArrayList<Data> dataSet){
     	double answer = 0.0;
     	int dataNum = dataSet.size();
     	double p [] = new double[catNum+1];
     	//System.out.println("catNum="+catNum);
     	for(int catCount = 1; catCount <= catNum; catCount++){
     		for(int dataCount = 0; dataCount < dataSet.size(); dataCount++){
     			//System.out.println("Compareing "+dataSet.get(dataCount).category+" and "+ catCount);
     			if(dataSet.get(dataCount).category == catCount){
         			p[catCount]++;
         		}
     		}
     		p[catCount] /= (double)dataNum;
     		//System.out.println(p[catCount]);
     		answer -= (p[catCount])*(Math.log((p[catCount]))/Math.log(2));
     	}
     	//System.out.println("enthropy="+answer);
     	return answer;
     }
     
     //given index of attribute it's calculating for, index of category, list of attributes, return the enthropyi
     private static double enthropyi(int currAtt, int catNum, ArrayList<Integer> attributes,  ArrayList<Data> dataSet){
     	double answer = 0.0;
     	//System.out.println("currAtt="+currAtt+"====================================");
     	ArrayList<ArrayList<Integer>> catChoice = new ArrayList<ArrayList<Integer>>();
     	//for each attribute choice
     	for(int attIdx = 1; attIdx <= attributes.get(currAtt); attIdx++){
     		//System.out.println("For Dx attribute="+attIdx);
     		catChoice.add(new ArrayList<Integer>());
     		for(int i = 0; i < dataSet.size(); i++){
     			//if this current dataset chose the same category as the current loop
     			if(dataSet.get(i).dataSets.get(currAtt) == attIdx){
     				//System.out.println("id="+dataSet.get(i).id+"| att="+dataSet.get(i).dataSets.get(attIdx)+"| cat="+dataSet.get(i).category);
     				//add the choice for current attribute
     				catChoice.get(attIdx-1).add(dataSet.get(i).category);
     			}
     		}
     	}
     	//System.out.println(catChoice);
     	
     	
     	//System.out.println("attributes.get(currAtt)="+attributes.get(currAtt));
     	//for each attribute AL
     	for(int ALIdx = 0; ALIdx < attributes.get(currAtt); ALIdx++){
     		//calculate the total number of dataset
     		double total = 0;
     		for(int temp = 0; temp < attributes.get(currAtt); temp++){
     			total += catChoice.get(temp).size();
     		}
     		//for each kind of category
     		double p [] = new double[catNum+1];
     		double sum [] = new double[attributes.get(currAtt)];
     		for(int catIdx = 0; catIdx < catNum; catIdx++){
     			//for each item in the array
     			for(int i = 0; i < catChoice.get(ALIdx).size(); i++){
         			if(catChoice.get(ALIdx).get(i) == catIdx+1){
         				p[catIdx]++;
         			}
     			}
     		}
 
     		double nominator = catChoice.get(ALIdx).size();
     		//System.out.println("nominator="+nominator);
     		for(int calcAttEnthro = 0; calcAttEnthro < catNum-1; calcAttEnthro++){
 	    		for(int i = 0; i < catNum; i++){
 	    			//System.out.println("("+p[i]+"/"+nominator+")*(Math.log(("+p[i]+"/"+nominator+"))/Math.log(2.0))="+-(p[i]/nominator)*(Math.log((p[i]/nominator))/Math.log(2.0)));
 	    			sum[calcAttEnthro] -= (p[i]/nominator)*(Math.log((p[i]/nominator))/Math.log(2.0));
 	    		}
 	    		if(Double.isNaN(sum[calcAttEnthro])){
 	    			sum[calcAttEnthro] = 0;
 	    		}
 	    		//System.out.println("("+nominator+")/("+total+")*"+sum[calcAttEnthro]+"="+(nominator)/(total)*sum[calcAttEnthro]);
 	    		answer += (nominator)/(total)*sum[calcAttEnthro];
     		}
     	}
     	//System.out.println("enthropyi="+answer);
     	return answer;
     }
     
     //index of category in attribute, list of attributes, dataset and a threashold, return the splitting attribute index
     private static int selectSplittingAttributeIG(int catNum, ArrayList<Integer> attributes, ArrayList<Data> dataSet, double threshold){
     	int answer = -1;
     	double answerValue = 0;
     	double p0 = enthropy(catNum,attributes, dataSet);
     	double p [] = new double[attributes.size()];
     	//for each attribute, calculate the enthropyi
     	for(int i = 0; i < attributes.size(); i++){
     		if(attributes.get(i) == -1){
     			p[i] = 0;
     			continue;
     		}
     		p[i] = p0 - enthropyi(i, catNum, attributes, dataSet);
     		//System.out.println("p["+i+"]="+p[i]);
     	}
     	
     	
     	for(int i = 0; i < attributes.size(); i++){
     		if(answerValue < p[i] && threshold < p[i]){
     			answer = i;
     			answerValue = p[i];
     		}
     	}
     	//System.out.println("selectSplittingAttributeIG="+answer+"|value="+answerValue);
     	return answer;
     }
     
     
     //given the category string and dataset, returns true if there are only one category left, false otherwise
     private static boolean oneCategoryLeft(ArrayList<Data> dataSet){
     	boolean oneCatLeft = true;
     	int catChoice = dataSet.get(0).category;
     	for(int i = 0; i < dataSet.size(); i++){
     		if(catChoice != dataSet.get(i).category){
     			oneCatLeft = false;
     		}
     	}
     	return oneCatLeft;
     }
     
     //given the category index, category choice numbers and dataset, returns the most common category decision
     public static int commonCategory(int catNum, ArrayList<Data> dataSet){
     	int tally [] = new int[catNum];
     	for(int i = 0; i < dataSet.size(); i++){
     		tally[dataSet.get(i).category-1]++;
     	}
     	
     	int answer = -1;
     	for(int i = 0; i < catNum; i++){
     		if(tally[i] > answer){
     			answer = i;
     		}
     	}
     	return answer;
     }
     
     private static boolean noMoreAttributes(ArrayList<Integer> attributes) {
     	for(int i = 0; i < attributes.size(); i++){
     		if(attributes.get(i) != -1){
     			return false;
     		}
     	}
 		return true;
 	}
     
 }
 
