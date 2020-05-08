 package application;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import edu.stanford.nlp.trees.*;
 
 /**
  * Store relation object with two entities, spans of tokens between entities
  * and label of the relation 
  */
 public class Relation {
 	
 	private String url;
 	private boolean label;
 	private Tree e1, e2, A;
 	private Tree headE1, headE2, headParse;
 	private String taggedSentence, nerSentence;
 	private HeadFinder hf;
 	private static final List<String> clauseType = 
 			Arrays.asList("S", "SINV", "SBAR", "RRC", "SBARQ", "SQ", "S-CLF", "FRAG");
 	// max number of tokens between e1 and e2
 	// adjust this parameter to change the propotion of true/false saamples
 	private static final int M = 15;
 	
 	/* list of result for each rule used for logging and debugging */
 	private boolean tooLongFlag;
 	private boolean containVerbFlag;
 	private boolean isParentOfFlag;
 	private boolean AisSenOrClauseFlag;
 	private boolean isSubjectOfFlag;
 	private boolean isHeadOfFlag;
 	private boolean crossSenBoundaryFlag;
 	private boolean isObjectOfPPFlag;
 	private boolean isValidSemanticFlag;
 	
 	/* number of NPs between e1 and e2 */
 	private Integer interval;
 	/* original sentence */
 	private String sentence;
 	
 	/* parsed sentence from where e1, e2 are selected */
 	private Tree parse;
 		
 	/* spans of tokens between the two targeted entities */
 	private ArrayList<Tree> RList;
 	
 	/* feature object of the relation */
 	private Feature features;
 	
 	/* type dependency list */
 	private List<TypedDependency> tdl;
 	
 	/**
 	 * @param e1 target entity1
 	 * @param e2 target entity2
 	 * @param sentence original sentence
 	 * @param taggedSentence sentence after pos tagging
 	 * @param nerSentence sentence after name entity recognition
 	 * @param parse full parsed tree of the sentence
 	 * @param interval interval of two phrases
 	 * @param labelFlage control whether to label the relation in construction
 	 */
 	public Relation(String url, Tree e1, Tree e2, String sentence, String taggedSentence, 
 			String nerSentence, Tree parse, Integer interval, boolean labelFlag) {
 		this.url = url;
 		this.e1 = e1;
 		this.e2 = e2;
 		RList = new ArrayList<Tree>();
 		hf = new SemanticHeadFinder();
 		headE1 = e1.headTerminal(hf);
 		headE2 = e2.headTerminal(hf);
 		headParse = parse.headTerminal(hf);
 		this.sentence = sentence;
 		this.taggedSentence = taggedSentence;
 		this.nerSentence = nerSentence;
 		this.parse = parse;
 		this.interval = interval;
 		
 		// set all rule flag false by default
 		tooLongFlag = false;
 		containVerbFlag = false;
 		isParentOfFlag = false;
 		AisSenOrClauseFlag = false;
 		isSubjectOfFlag = false;
 		isHeadOfFlag = false;
 		crossSenBoundaryFlag = false;
 		isObjectOfPPFlag = false;
 		isValidSemanticFlag = false;
 						
 		// generate features of the relation
 		generateFeatures();
 		
 		/* If caller want to label the relation, we will label it
 		 * by calling generateLabel() method
 		 */
 		if (labelFlag) {
 			generateLabel();
 		}
 	}
 
 	private void generateLabel() {
 		/* generate list of leaf node between e1 and e2 */
 		generateRList();
 		/* initialize tdl;  */
 		GetTypedDependencyLst();
 		/* set lowest common ancestor */
 		A = LowestCommonAncestor();
 		/*apply rules on the relation and label it */
 		label = returnLabelC();
 		System.out.println(this);
 	}
 
 	/**
 	 * Apply set of heuristics on the relation based on the full
 	 * parse tree and generate label finally
 	 * the functions' passed parameters need to be changed according to new functions.
 	 * @author Liangliang Zhang
 	 */
 	public boolean returnLabelC(){
 		
 		if (RList.size() > M) {
 			tooLongFlag = true;
 			return false;
 		}
 		
 		if (!ContainsVerb()) return false;
 		containVerbFlag = true;
 		
 		if (IsParentOf(e1, e2)) {
 			isParentOfFlag = true;
 			return false;
 		}
 		
 		if (clauseType.contains(A.label().value().toString())) {
 			AisSenOrClauseFlag = true;
 			
 			if (!SubjectOf()) return false;
 			isSubjectOfFlag = true;
 			
 //			if (!IsHeadOf()) return false; // TODO: to be discussed, confusing
 //			isHeadOfFlag = true;
 			
 			if (CrossSentenceBoundary()) {
 				crossSenBoundaryFlag = true;
 				return false;
 			}
 			
 			if (IsObjectOfPP(e2)) {
 				isObjectOfPPFlag = true;
 				//if (IsValidSemanticRole(e2))
 				return true;
 				//else reVal = false;
 			} else {
 				return true;
 				//R = normalize(R) ?? not implemented.
 			}
 		} else {
 			return false;
 		}
 	}
 
 	/* 
 	 * generate the RList
 	 * RList is a list of tree leaf nodes that between e1 and e2 (as defined in Page 28 of the thesis)
 	 * TODO: It should be noted that in this implementation, RList includes all children of e1 while
 	 * no children of e2, debatable or does not matter?
 	 * @author Liangliang Zhang
 	 * @param parse
 	 */
 	private void generateRList() {
 		RList = new ArrayList<Tree>();
 		Iterator<Tree> itr = parse.iterator();
 		while(itr.hasNext()) {
 			Tree currNode = itr.next();
 			if (currNode.equals(e1)) {
 				while(!currNode.equals(e2)){
 					if (currNode.isLeaf()) {
 						RList.add(currNode);
 					}
 					currNode = itr.next();
 				}
 			}
 		}
 	}
 	
 	/*
 	 * added by Liangliang
 	 * determine if there is a verb in the RList
 	 */
 	private boolean ContainsVerb(){
 		for (int i=0; i < RList.size(); i++) {
 			if (IsVerb(RList.get(i)))
 				return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * by Liangliang
 	 * leafNode is a leafNode of a tree parse
 	 * determine if the word in leafNode is a verb whose pos start with "VB"
 	 */
 	private boolean IsVerb(Tree leafNode){
 		Tree parent = leafNode.parent(parse);
 		if (parent.label().value().matches("VB.*")) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * by Liangliang
 	 * determine if e1 is the parent of e2
 	 */
 	private boolean IsParentOf(Tree e1, Tree e2) {
 		return e1.equals(e2.parent(parse));
 	}
 	
 	/*
 	 * determine if e1 is the ancestor of e2
 	 */
 	private boolean IsAncestorOf(Tree e1, Tree e2) {
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
 	private Tree LowestCommonAncestor() {
 		Tree curr = e1;
 		while (!curr.equals(parse)) {
 			if (IsAncestorOf(curr, e2))
 				return curr;
 			curr = curr.parent(parse);
 		}
 		return parse;
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
 	private void GetTypedDependencyLst() {
 		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
 		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
 		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
 		tdl = gs.typedDependenciesCCprocessed();
 	}
 	
 	/*
 	 * Given a tree node, return its reln in typed dependency
 	 */
 	private String GetRelnOfNode(Tree node) {
 		Tree head = node.headTerminal(hf);
 		Iterator<TypedDependency> itr = tdl.iterator();
 		while (itr.hasNext()) {
 			TypedDependency td = itr.next();
 			if (td.dep().label().value().equals(head.label().value())) {
 				return td.reln().toString();  
 			}
 		}
 		return null;
 	}
 		
 	/*
 	 * by Liangliang
 	 * determine if e1 is the subject of A (sentence/clause).
 	 */
 	private boolean SubjectOf() {
 		String reln = GetRelnOfNode(e1);
 		if (reln != null && reln.equals("nsubj")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/*
 	 * by Liangliang
 	 * return if e1 is the head of the tree parse
 	 */
 	private boolean IsHeadOf(){
 		return e1.equals(headParse);
 	}
 	
 	private boolean IsObjectOfPP(Tree e) {
 		String reln = GetRelnOfNode(e2);
 		if (reln != null && reln.matches("prep.*")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/*
 	 * if one of e1's and e2's ancestor node equals sentence or clause 
 	 * before reaching their lowest common ancestor
 	 * then they cross the boundary
 	 */
 	private boolean CrossSentenceBoundary() {
 		Tree t = e1.parent(parse);
 		while (!t.equals(A)) {
 			String label = t.label().value().toString();
 			if (clauseType.contains( t.label().value().toString())) {
 				return true;
 			}
 			t = t.parent(parse);
 		}
 		
 		t = e2.parent(parse);
 		while (!t.equals(A)) {
 			if (clauseType.contains(t.label().value().toString()))
 				return true;
 			t = t.parent(parse);
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Generate feature object for the relation
 	 */
 	private void generateFeatures() {
 		features = new Feature(e1, e2, headE1, headE2, interval, sentence,
 				taggedSentence, nerSentence);
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
 		sb.append("original sentence: " + sentence + "\n");
 		sb.append("e1: " + e1.toString() + "\n");
 		sb.append("e2: " + e2.toString() + "\n");
 		sb.append("Rlist: " + RList.toString() + "\n");
 		sb.append("Common Ancestor: " + A.toString() + "\n");
 		sb.append("Too much tokens in between: " + tooLongFlag + "\n");
 		sb.append("Tokens btw e1 and e2 contains Verb: " + containVerbFlag + "\n");
 		sb.append("e1 is parent of e2: " + isParentOfFlag + "\n");
 		sb.append("A is labelled as a sentence or clause: " + AisSenOrClauseFlag + "\n");
 		sb.append("\t e1 is subject of A: " + isSubjectOfFlag + "\n");
 		sb.append("\t e1 is head of A: not applicable now " + "\n");
 		sb.append("\t e1 and e2 cross sentence boundary: " + crossSenBoundaryFlag + "\n");
 		sb.append("\t e2 is object of PP: " + isObjectOfPPFlag + "\n");
 		sb.append("\t\t e2 is valid semantic role: " + "not applicable now " + "\n");
 		sb.append("label: " + label + "\n");
 		return sb.toString();
 	}
 	
 }
