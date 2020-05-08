 /**
  * 
  */
 package dataMediator;
 
 import util.NodeType;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ontology.similarity.ConceptSimilarity;
 
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 import parser.DmParser;
 import parser.SawsdlParser;
 import util.OpWsdl;
 import util.OpWsdlPathScore_type;
 import util.GeometricSeries;
 import uk.ac.shef.wit.simmetrics.similaritymetrics.*;
 
 import org.semanticweb.owlapi.model.*;
 import parser.OntologyManager;
 
 /**
  * @author Rui Wang
  * 
  */
 public class PathRank {
 
     private static Namespace xsdNS = Namespace.getNamespace("http://www.w3.org/2001/XMLSchema");
     private static Namespace wsdlNS = Namespace.getNamespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
     private static Namespace sawsdlNS = Namespace.getNamespace("sawsdl", "http://www.w3.org/ns/sawsdl");
 
     /** Given an ontology, operations in current workflow and next operation to 
      *  be added, return a map [path of the input of nextOP, matched path in 
      *  output & globalInput of the workflow]
      * 
      * @param workflowOPs
      * @param nextOP
      * @param owl ontology
      * @return
      */
     public static Map<OpWsdlPathScore_type, OpWsdlPathScore_type> dataMediation(List<OpWsdl> workflowOPs, OpWsdl nextOP, String owlURI) {
         
         SawsdlParser sp = new SawsdlParser();
         DmParser dmp = new DmParser();
         
         Map<OpWsdlPathScore_type, OpWsdlPathScore_type> matchPathMap 
                 = new HashMap<OpWsdlPathScore_type, OpWsdlPathScore_type>();
 
         // get all paths of the input of the nextOP
         List<List<Element>> nextOpInPaths 
                 = dmp.getPathsList(sp.getInMsElem(nextOP.getWsdlName(), nextOP.getOpName()));
         
         List<OpWsdlPathScore_type> nextOpInPathScoreList 
                 = new ArrayList<OpWsdlPathScore_type>();
 
         for (List<Element> inPath : nextOpInPaths) {
             nextOpInPathScoreList.add(new OpWsdlPathScore_type(nextOP.getOpName(), nextOP.getWsdlName(), inPath, true));
         } // for
 
         // get all paths of the output of the workflowOPs
         List<OpWsdlPathScore_type> workflowOutPathScoreList 
                 = new ArrayList<OpWsdlPathScore_type>();
 
         // only get paths of last op of workflowOPs, if do this, need turn off 
         // above section(get all output of workflow)
         OpWsdl workflowOp = workflowOPs.get(workflowOPs.size() - 1);
         
         List<List<Element>> workflowOpOutPath 
                 = dmp.getPathsList(sp.getOutMsElem(workflowOp.getWsdlName(), workflowOp.getOpName()));
 
         for (List<Element> outPath : workflowOpOutPath) {
             workflowOutPathScoreList.add(new OpWsdlPathScore_type(workflowOp.getOpName(), workflowOp.getWsdlName(), outPath, false));
         } // for
 
         // find matched path for each path of the input of nextOP
         for (OpWsdlPathScore_type inPathScore : nextOpInPathScoreList) {
             OpWsdlPathScore_type matchedPath = findMatchPath(workflowOutPathScoreList, inPathScore, owlURI);
             inPathScore.setScore(matchedPath.getScore());
             matchPathMap.put(inPathScore, matchedPath);
         } // for
         
        // here?
        
         return matchPathMap;
         
     } // dataMediation
 
     /** Find matched path for the keyPath (patten) from pathsList
      * @param pathsList
      * @param keyPath
      * @param owlFileName
      * @return  matched path with wsdl and score and (isInput or not)
      */
     private static OpWsdlPathScore_type findMatchPath(List<OpWsdlPathScore_type> pathsList, OpWsdlPathScore_type keyPath, String owlURI) {
 
         for (OpWsdlPathScore_type path : pathsList) {
 
             List<Element> outPath = path.getPath();
             List<Element> inPath  = keyPath.getPath();
 
             double outPathScore = 0;
 
             // pick min length
             int pathLength = outPath.size() < inPath.size() ? outPath.size() : inPath.size();
 
             // calculate weights, geometric progression
             GeometricSeries gs = new GeometricSeries();
             double[] nodeWeights = gs.getWeights(pathLength);
 
             for (int i = 0; i < pathLength; i++) {
                 if (i == 0) {
                     outPathScore = outPathScore + nodeWeights[i] * compare2node(outPath.get(i), inPath.get(i), owlURI, NodeType.LEAF_NODE);
                 } else {
                     outPathScore = outPathScore + nodeWeights[i] * compare2node(outPath.get(i), inPath.get(i), owlURI, NodeType.NON_LEAF_NODE);
                 } // if
             } // for
             
             path.setScore(outPathScore);
             
         } // for
         
         // sort, higher score first
         Collections.sort(pathsList, Collections.reverseOrder());
 
         // return the highest score path
         return pathsList.get(0);
         
     } // findMatchPath
 
     /** Compare two nodes in the path.
      * @param outNode
      * @param inNode
      * @param owlFileName
      * @return
      */
     public static double compare2node (Element outNode, Element inNode, String owlURI, NodeType type) 
     {
         OntologyManager parser = OntologyManager.getInstance(owlURI);
 
         double score = 0.0;
         double scoreSyn = 0.0;
         double scoreSem = 0.0;
         double scoreTyp = 0.0;
 
         double weightSyn = 0.5;
         double weightSem = 0.5;
         double weightTyp = 0.0;
   
         org.jdom.Attribute outName = outNode.getAttribute("name", xsdNS);
         org.jdom.Attribute outMR = outNode.getAttribute("modelReference", sawsdlNS);
         
         org.jdom.Attribute inName = inNode.getAttribute("name", xsdNS);
         org.jdom.Attribute inMR = inNode.getAttribute("modelReference", sawsdlNS);
         
         
         // compare modelreference if exist
         if (outNode.getAttribute("modelReference", sawsdlNS) != null && inNode.getAttribute("modelReference", sawsdlNS) != null) {
             
             String outNodeMF = outNode.getAttributeValue("modelReference", sawsdlNS);
             String inNodeMF = inNode.getAttributeValue("modelReference", sawsdlNS);
 
             OWLClass inConceptClass = parser.getConceptClass(inNodeMF);
             OWLClass outConceptClass = parser.getConceptClass(outNodeMF);
 
             if (owlURI != null) {
 
                if (inConceptClass == null || outConceptClass == null) {
                     scoreSem = 0.0;
                 } else {
 
                    scoreSem = ConceptSimilarity.getConceptSimScore(inConceptClass, outConceptClass, owlURI);
                     
                    if (type != NodeType.LEAF_NODE){
                         weightSyn = 0.1;
                         weightSem = 0.9;
                     } // if
 
                 } // if
 
             } // if
 
         } // if
 
         // compare element's name if exist
         if (outNode.getAttribute("name") != null && inNode.getAttribute("name") != null) {
             
             String outNodeName = outNode.getAttributeValue("name");
             String inNodeName  = inNode.getAttributeValue("name");
 
             if (outNodeName.equalsIgnoreCase(inNodeName)) {
                 scoreSyn = 1;
             } else {
                 QGramsDistance mc = new QGramsDistance();
                 scoreSyn = mc.getSimilarity(outNodeName, inNodeName);
             } // if
 
         } else {
             // nonleaf node has no name attribute
         } // if
 
         if (type == NodeType.LEAF_NODE) {
             
             weightSyn = 0.4;
             weightSem = 0.4;
             weightTyp = 0.2;
 
             //compare leaf's type attribute if exist
             if (outNode.getAttribute("type") != null && inNode.getAttribute("type") != null) {
 
                 String outNodeType = outNode.getAttributeValue("type").split(":")[1];
                 String inNodeType = inNode.getAttributeValue("type").split(":")[1];
 
                 if (outNodeType.equalsIgnoreCase(inNodeType)) {
                     scoreTyp = 1;
                 } else {
                     //can be more complicated, maybe another class to give score between any two xsd buildin 44 types
                     scoreTyp = 0;
                 } // if 
 
             } else {
                 //if input leaf has no type, 
                 //which implies no need to give value to this input to invoke this op, always correct to invoke this op 
                 //or this op return nothing as output, safe for anying succeeding op 
                 //or the type is defined inside the leaf node, but without any element, may only change some attribute, e.g. arrayofstring complextype
                 //since this type distract user's attention from the op has input/output, so we make this score lower than 1
                 scoreTyp = 0.5;
 
             } // if
 
         } // if (type == NodeType.LEAF_NODE)
 
         // weighted sum
         score = (weightSyn * scoreSyn) + (weightSem * scoreSem) + (weightTyp * scoreTyp);
 
         return score;
         
     } // compare2node
 
 } // PathRank
