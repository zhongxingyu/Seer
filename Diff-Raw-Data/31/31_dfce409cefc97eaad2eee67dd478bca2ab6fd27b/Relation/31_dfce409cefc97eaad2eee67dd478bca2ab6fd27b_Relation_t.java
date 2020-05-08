 package application;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.stanford.nlp.ling.CoreLabel;
 import edu.stanford.nlp.objectbank.TokenizerFactory;
 import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
 import edu.stanford.nlp.process.CoreLabelTokenFactory;
 import edu.stanford.nlp.process.PTBTokenizer;
 import edu.stanford.nlp.trees.*;
 
 /**
  * Store relation object with two entities, spans of tokens between entities
  * and label of the relation 
  */
 public class Relation {
 	
 	private String url;
 	private boolean label;
 	private Tree e1, e2;
 	private ArrayList<Tree> RList;
 	private Tree headE1, headE2;
 	/* number of NPs between e1 and e2 */
 	private Integer interval;
 	/* original sentence */
 	private String sentence;
 	
 	/* parsed sentence from where e1, e2 are selected */
 	private Tree parse;
 		
 	/* spans of tokens between the two targeted entities */
 	private ArrayList<Tree> tokens;
 	
 	/* feature object of the relation */
 	private Feature features;
 	
 	private List<TypedDependency> tdl;
 	
 	/* List<Tree> leafTreeNodes contains all the leaf nodes of the tree parse */
 	private List<Tree> leafNodes;
 	
 	/**
 	 * @param e1 target entity1
 	 * @param e2 target entity2
 	 * @param parse full parsed tree of the sentence
 	 * @param labelFlage control whether to label the relation in construction
 	 */
 	public Relation(String url, Tree e1, Tree e2, String sentence, 
 			Tree parse, Integer interval, boolean labelFlag) {
 		this.url = url;
 		this.e1 = e1;
 		this.e2 = e2;
 		HeadFinder hf = new SemanticHeadFinder();
 		headE1 = e1.headTerminal(hf);
 		headE2 = e2.headTerminal(hf);
 		this.sentence = sentence;
 		this.parse = parse;
 		
 		/* added by Liangliang
 		 * set RList, the list of trees that between e1 and e2
 		 */
 		RList = generateRList(parse);
 		
 		/* by Liangliang
 		 * initialize tdl;
 		 */
 		GetTokenizedFactory();
 		
 		/* by Liangliang
 		 * initialize the leafNodes
 		 */
 		InitLeafNodes();
 		
 		this.interval = interval;
 		label = false; // by default, label as negative sample
 		tokens = new ArrayList<Tree>();
 		
 		/* If caller want to label the relation, we will label it
 		 * by calling applyRules() method
 		 */
 		if (labelFlag) {
 			applyRules();
 		}
 		// generate features of the relation
 		generateFeatures();
 	}
 	
 	/**
 	 * Apply set of heuristics on the relation based on the full
 	 * parse tree and generate label finally
 	 * @author Liangliang Zhang
 	 */
 	
 	
 	/** 
 	 * added by Liangliang
 	 * generate the RList
 	 * RList is a list of tree leaf nodes that between e1 and e2 (as defined in Page 28 of the thesis)
 	 * @param parse
 	 * @return
 	 */
 	private ArrayList<Tree> generateRList(Tree parse) {
 		ArrayList<Tree> RList = new ArrayList<Tree>();
 		Iterator<Tree> itr = parse.iterator();
 		while(itr.hasNext()) {
 			Tree currNode = itr.next();
 			if (currNode.equals(e1)) {
 				currNode = itr.next();
 				while(!currNode.equals(e2)){
 					if (currNode.isLeaf()) {
 						RList.add(currNode);
 					}
 				}
 			}
 			break;
 		}
 		return RList;
 	}
 	
 	/*
 	 * added by Liangliang
 	 * determine if there is a verb in the RList
 	 */
 	private boolean ContainsVerb(ArrayList<Tree> RList, Tree parse){
 		for (int i=0; i < RList.size(); i++) {
 			if (IsVerb(RList.get(i), parse))
 				return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * by Liangliang
 	 * leafNode is a leafNode of a tree parse
 	 * determine if the word in leafNode is a verb ("VP")
 	 */
 	private boolean IsVerb(Tree leafNode, Tree parse){
 		Tree currTree = leafNode;
 		while (!currTree.equals(parse)) {
 			if (currTree.label().equals("VP")) // maybe no need to consider other situation, since their father node will be VP
 				return true;
 			currTree = currTree.parent(parse);
 		}
 		return false;
 	}
 	
 	/*
 	 * by Liangliang
 	 * determine if e1 is the parent of e2
 	 */
 	private boolean IsParentOf(Tree e1, Tree e2, Tree parse) {
 		Tree curr = e2;
 		while (!curr.equals(parse)) {
 			if (e1.equals(curr))
 				return true;
 			curr = curr.parent(parse);
 		}
 		return false;
 	}
 	
 	/*
 	 * by Liangliang
 	 * find the lowest common ancestor of e1 and e2, given root node T
 	 */
 	private Tree LowestCommonAncestor(Tree e1, Tree e2, Tree T) {
 		Tree curr = e1;
 		while (!curr.equals(T)) {
 			if (IsParentOf(curr, e2, T))
 				return curr;
 			curr = curr.parent(T);
 		}
 		return T;
 	}
 	
 	/*
 	 * by Liangliang
 	 * not implemented yet
 	 *
 	private boolean IsValidSemanticRole(Tree e2) {
 		//not implemented yet
 	}
 	*/
 	
 	/*
 	 * by Liangliang
 	 * given a LexicalizedParser lp and a String sentence
 	 * this function tends to return a tdl, which contains the information such as "nsuj", etc.
 	 * but may not implemented correctly....
 	 */
 	private void GetTokenizedFactory() {
 		
 		/*
 		TokenizerFactory<CoreLabel> tokenizerFactory = 
 			      PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
 		List<CoreLabel> rawWords2 = 
 			      tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
 		Tree tr = lp.apply(rawWords2);
 		*/
 		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
 		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
 		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
 		tdl = gs.typedDependenciesCCprocessed();
 	}
 	
 	/*
 	 * by Liangliang
 	 * initialize the leafNodes;
 	 */
 	private void InitLeafNodes() {
 		leafNodes = parse.getLeaves();
 	}
 	
 	
 	/*
 	 * by Liangliang
 	 * return the index number of the tree e in leafNodes;
 	 */
 	private int GetIndexLeafNodes(Tree e) {
 		int val = -1;
 		for (int i = 0; i < leafNodes.size(); i++) {
 			if (leafNodes.get(i).equals(e)) {
 				val = i;
 				return val;
 			}
 		}
 		return val;
 	}
 	
 	
 	
 	/*
 	 * by Liangliang
 	 * determine if e1 is the subject.
 	 * the SubjectOf, IsHeadOf, IsObjectOfPP are not implemented.
 	 */
 	private boolean SubjectOf(Tree e) {
 		int index = GetIndexLeafNodes(e);
 		if (tdl.get(index).reln().equals("nsubj"))
 			return true;
 		else
 			return false;
 	}
 	
 	
 	private boolean IsHeadOf(Tree e){
 		return true;
 	}
 	
 	private boolean IsObjectOfPP(Tree e) {
 		int index = GetIndexLeafNodes(e);
 		if (tdl.get(index).reln().equals("dobj") || tdl.get(index).reln().equals("iobj") )
 			return true;
 		else
 			return false;
 	}
 	
 	
 	/*
 	 * by Liangliang
 	 * the functions' passed parameters need to be changed according to new functions.
 	 */
 	public boolean returnLabelC(){
 		
 		boolean reVal = false;
 		if (!ContainsVerb(RList, parse)) reVal = false;
 		if (IsParentOf(e1, e2, parse)) reVal = false;
 		Tree A = LowestCommonAncestor(e1, e2, parse);
 		if (A.label().equals("S")) {
 			if (!SubjectOf(e1)) reVal = false;
 			if (!IsHeadOf(e1)) reVal = false;
 			//if (CrossSentenceBoundary(e1, e2, T)) reVal = false; not yet implemented
 			if (IsObjectOfPP(e2)) {
 				//if (IsValidSemanticRole(e2)) 
 					reVal = true;
 				//else reVal = false;
 			} else {
 				reVal = true;
 				//R = normalize(R) ?? not implemented.	
 			}
 		} else {
 			reVal = false;
 		}
 		return reVal;
 	}
 	
 	private void applyRules() {
 		label = true; // or false based on above rules
 		return;
 		/*
 		boolean reVal = false;
 		if (!ContainsVerb(RList, parse)) reVal = false;
 		if (IsParentOf(e1, e2, parse)) reVal = false;
 		Tree A = LowestCommonAncestor(e1, e2, parse);
 		if (A.label().equals("S")) {
 			if (!SubjectOf(e1)) reVal = false;
 			if (!IsHeadOf(e1)) reVal = false;
 			//if (CrossSentenceBoundary(e1, e2, T)) reVal = false; not yet implemented
 			if (IsObjectOfPP(e2)) {
 				//if (IsValidSemanticRole(e2)) 
 					reVal = true;
 				//else reVal = false;
 			} else {
 				reVal = true;
 				//R = normalize(R) ?? not implemented.	
 			}
 		} else {
 			reVal = false;
 		}
 		return reVal;
 		*/
 	}
 	
 	/**
 	 * Generate feature object for the relation
 	 */
 	private void generateFeatures() {
 		features = new Feature(e1, e2, headE1, headE2, interval, sentence);
 	}
 	
 	/**
 	 * @return feature object of the relation
 	 */
 	public Feature getFeatures() {
 		return features;
 	}
 	
 	/**
 	 * @return feature vector of the relation
 	 */
 	public String getFeaturesVector() {
 		return features.getFeatureVector();
 	}
 	
 	/**
 	 * @return label of the relation true or false
 	 */
 	public boolean getLabel() {
 		return label;
 	}
 	
 	/**
 	 * @return url of the article where the relation is extracted
 	 */
 	public String getURL() {
 		return url;
 	}
 	
 	public String toString() {
 		StringBuffer sb = new StringBuffer();
 		sb.append("e1: ");
 		sb.append(e1.toString() + "\n");
 		sb.append("e2: ");
 		sb.append(e2.toString() + "\n");
 		sb.append("tokens: ");
 		sb.append(tokens.toString() + "\n");
 		sb.append("label: ");
 		sb.append(label + "\n");
 		return sb.toString();
 	}
 	
 }
