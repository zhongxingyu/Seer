 package ru.tsu.inf.atexant.nlp;
 
 import com.sun.xml.internal.ws.message.RelatesToHeader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.Arrays;
 import net.sf.extjwnl.JWNLException;
 import net.sf.extjwnl.data.*;
 import net.sf.extjwnl.data.list.PointerTargetNode;
 import net.sf.extjwnl.data.list.PointerTargetTree;
 import net.sf.extjwnl.data.list.PointerTargetTreeNode;
 import net.sf.extjwnl.data.relationship.AsymmetricRelationship;
 import net.sf.extjwnl.data.relationship.Relationship;
 import net.sf.extjwnl.data.relationship.RelationshipFinder;
 import net.sf.extjwnl.data.relationship.RelationshipList;
 import net.sf.extjwnl.dictionary.Dictionary;
 import ru.tsu.inf.atexant.nlp.stat.SelectionEvaluationMaxStategy;
 import ru.tsu.inf.atexant.nlp.stat.SelectionEvaluationStategy;
 
 
 public class WordNetSimilarityMeasurer extends WordSimilarityMeasurer {
     private final static String PATH_FILE_PROPERTIES = "res/extjwnl/file_properties.xml";
     
     private static WordNetSimilarityMeasurer instance = new WordNetSimilarityMeasurer();
     
     public static WordNetSimilarityMeasurer getInstance() {
         return instance;
     }
     
     private Dictionary dict = null;
     
     private double similarWeightCoef = 0.8;
     private double samePOSAdjCoef = 0.06;
     
     private WordNetSimilarityMeasurer() {
         try {
             dict = Dictionary.getInstance(new FileInputStream(PATH_FILE_PROPERTIES));
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (JWNLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             int a;
         }
     }
     
     private SelectionEvaluationStategy defaultSelectionStategy = new SelectionEvaluationMaxStategy();
     
     private POS getPOSByString(String posString) {
         if (posString == null) {
             return null;
         } 
         
         if (posString.startsWith("NN")) {
             return POS.NOUN;
         }
         
         if (posString.startsWith("V")) {
             return POS.VERB;
         }
         
         if (posString.startsWith("JJ") || posString.startsWith("ADJ")) {
             return POS.ADJECTIVE;
         }
         
         if (posString.startsWith("RB") || posString.startsWith("ADV")) {
             return POS.ADVERB;
         }
         
         return null;
     }
     
     private IndexWord getIndexWordByString(String wordText) {
         IndexWordSet iws = null;
         try {
             iws = dict.lookupAllIndexWords(wordText);
         } catch (JWNLException e) {
             e.printStackTrace();
         }
         
         if (iws == null || iws.size() == 0) {
             return null;
         }
         
         return iws.getIndexWordArray()[0];
     }
     
     private IndexWord getIndexWordByWordToken(WordToken wt) {
         POS pos = getPOSByString(wt.getPOS());
         
         if (pos == null) {
             return getIndexWordByString(wt.getLemmaOrWord());
         }
         
         IndexWord result = null;
         try {
             result = dict.lookupIndexWord(pos, wt.getLemmaOrWord());
             
             if (result != null) {
                 return result;
             }
             
         } catch (JWNLException e) {
             e.printStackTrace();
         }
         
         return getIndexWordByString(wt.getLemmaOrWord());
         
     }
     
     private Synset getSynsetByIndexWord(IndexWord iw) {
         return iw.getSenses().get(0);
     }
     
     private int getDistanceFromNodeToRoot(PointerTargetNode a) {
         PointerTargetTree tree = PointerUtils.getHypernymTree(a.getSynset());
         
         PointerTargetTreeNode node = tree.getRootNode();
         
         int result = 1;
         
         while (node.hasValidChildTreeList()) {
             result++;
             node = node.getChildTreeList().getFirst();
         }
         
         return result;
     }
     
     private Relationship findBestRelationship(RelationshipList list) {
         int index = 0;
         for (int i = 1; i < list.size(); i++) {
             if (list.get(index).getDepth() > list.get(i).getDepth()) {
                 index = i;
             }
         }
         
         return list.get(index);
     }
     
     private double getSimilarity(Synset a, Synset b) {
         try {
             if (a.getSynset().equals(b.getSynset())) {
                 return 1.0;
             }
             
             RelationshipList relationshipList = RelationshipFinder.findRelationships(a, b, PointerType.HYPERNYM);
             
             if (relationshipList.size() == 0) {
                 if (a.getPOS().equals(POS.ADJECTIVE) && b.getPOS().equals(POS.ADJECTIVE)) {
                     if (RelationshipFinder.findRelationships(a, b, PointerType.SIMILAR_TO).size() > 0) {
                         return similarWeightCoef;
                     }
                     return samePOSAdjCoef;
                 }
                 return 0.0;
             }
             
             AsymmetricRelationship rel = (AsymmetricRelationship)findBestRelationship(relationshipList);
             int d1 = rel.getCommonParentIndex();
             int d2 = rel.getDepth() - rel.getCommonParentIndex();
             
             PointerTargetNode lcaNode = rel.getNodeList().get(rel.getCommonParentIndex());
             int d3 = getDistanceFromNodeToRoot(lcaNode);
             
             double ans = ((double)(2*d3)) / ((double)(Math.sqrt(d1*d1 + d2*d2) + 2*d3));
             return ans;
             
         } catch (CloneNotSupportedException e) {
             e.printStackTrace();
         }
 
         
         return -1.0;
     }
     
     
     private double getSimilarity(IndexWord a, IndexWord b) {
         double[] similarities = new double[a.getSenses().size() * b.getSenses().size()];
         
         int counter = 0;
         
         for (Synset aSynset : a.getSenses()) {
             for (Synset bSynset : b.getSenses()) {
                 similarities[counter++] = getSimilarity(aSynset, bSynset);
             }
         }
        
         return defaultSelectionStategy.getValue(similarities);
     }
     
     @Override
     public double getSimilarity(WordToken word1, WordToken word2) {
         IndexWord a = getIndexWordByWordToken(word1);
         IndexWord b = getIndexWordByWordToken(word2);
         
         if (a == null || b == null) {
             if (word1.getLemmaOrWord().equalsIgnoreCase(word2.getLemmaOrWord())) {
                 return 1.0;
             }
             
             return 0.0;
         }
         return getSimilarity(getIndexWordByWordToken(word1), getIndexWordByWordToken(word2));
     }
     
     public double getSimilarity(String word1, String word2) {
         IndexWord a = getIndexWordByString(word1);
         IndexWord b = getIndexWordByString(word2);
         
         if (a == null || b == null) {
             if (word1.equalsIgnoreCase(word2)) {
                 return 1.0;
             }
             
             return 0.0;
         }
         
         return getSimilarity(a, b);
     }
     
 }
