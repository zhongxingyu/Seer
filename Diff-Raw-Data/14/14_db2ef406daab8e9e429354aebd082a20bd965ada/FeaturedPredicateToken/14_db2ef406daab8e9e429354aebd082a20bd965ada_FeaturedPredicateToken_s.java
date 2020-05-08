 package predicateClassification;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import util.Token;
 
 import edu.stanford.nlp.process.WordShapeClassifier;
 import edu.stanford.nlp.ling.BasicDatum;
 import edu.stanford.nlp.ling.Datum;
 
 public class FeaturedPredicateToken extends Token{
 	
 	private List<Integer> childrenIndices = new ArrayList<Integer>();
 
 	private FeaturedPredicateToken prev2Token;
 	private FeaturedPredicateToken prevToken;
 	private FeaturedPredicateToken nextToken;
 	private FeaturedPredicateToken next2Token;
 	
 	private final String wordShape;
 
 	private static final int wordShaper = WordShapeClassifier.WORDSHAPECHRIS2;
 	
 	private static FeaturedPredicateToken emptyToken = new FeaturedPredicateToken("", "",	//used as a placeholder for nonexistent tokens
 			"", "", "",
 			-1, -1, new ArrayList<FeaturedPredicateToken>());
 
 	public FeaturedPredicateToken(final String splitForm, String splitLemma,
 			String pposs, String deprel, String predicateRole,
 			int parentIndex, int sentenceIndex, List<? extends FeaturedPredicateToken> sentenceTokens){
 		
 		super(splitForm, splitLemma, pposs, deprel, predicateRole,
 				parentIndex, sentenceIndex, sentenceTokens);
 
 		wordShape = WordShapeClassifier.wordShape(splitForm, wordShaper);	
 	}
 	
 	public void updateAdjacentTokens(){
 		if (sentenceIndex > 0)
 			prevToken = (FeaturedPredicateToken) sentenceTokens.get(sentenceIndex - 1);
 		else
 			prevToken = emptyToken;
 
 		if (sentenceIndex > 1)
 			prev2Token = (FeaturedPredicateToken) sentenceTokens.get(sentenceIndex - 2);
 		else
 			prev2Token = emptyToken;
 
 		if (sentenceIndex < sentenceTokens.size() - 1)
 			nextToken = (FeaturedPredicateToken) sentenceTokens.get(sentenceIndex + 1);
 		else
 			nextToken = emptyToken;
 
 		if (sentenceIndex < sentenceTokens.size() - 2)
 			next2Token = (FeaturedPredicateToken) sentenceTokens.get(sentenceIndex + 2);
 		else
 			next2Token = emptyToken;
 	}
 	
 	/*
 	 * If its predicate role is "_", i.e. it is not a predicate,
 	 * returns false.
 	 * Otherwise, returns true.
 	 */
 	public boolean isPredicate(){
 		return !predicateRole.equals("_");
 	}
 	
 	/*
 	 * Simply adds some index to this token's
 	 * list of its children's indices.
 	 */
 	public void addChild(int childIndex){
 		if (childIndex >= 0)
 			childrenIndices.add(childIndex);
 	}
 
 	/*
 	 * Features are used by the linear classifier to classify tokens.
 	 * Features are provided as strings in the following format:
 	 * [feature identifier]|[data]
 	 * 
 	 * ex: number of children for a word with 11 children:
 	 * numch|11
 	 * 
 	 * ex: word shape for "Bush":
 	 * wdshp|Xxxx
 	 * 
 	 * List of prefixes:
 	 * splm[u/b] -- split-lemma (unigram/bigram)
 	 * spfm -- split-form (unigram)
 	 * ppos[u/b] -- pposs (unigram/bigram)
 	 * wdshp -- word shape (string)
 	 * numch -- number of children (integer)
 	 * c[#] -- child
 	 * 
 	 */
 	public Collection<String> getFeatures() {
 		Collection<String> features = new ArrayList<String>();
 
 		features.addAll(getSplitLemmas());		//add split-lemma
 		features.addAll(getSplitForms());		//add split-form
 		features.addAll(getPPoss());			//add pposs
 
 		features.addAll(getWordShapes());		//add word shape using Stanford's WordShapeClassifier
 
 		features.add("numch|" +	childrenIndices.size());	//add number of children
 
 		features.addAll(getChildrenFeatures()); //add children features
 		features.add(getChildrenDifferences()); //add children differences
 		
 		return features;
 		
 	}
 	
 	/*
 	 * Provides word shapes for this, and neighboring, tokens
 	 * Format:
 	 * wdshp[u/b/t][token nums]|[space-delimited wordshapes]
 	 */
 	private Collection<String> getWordShapes(){
 		Collection<String> wordShapes = new ArrayList<String>();
 		
 		//word shape unigrams
 		wordShapes.add("wdshpu,i-1|" + prevToken.wordShape);
 		wordShapes.add("wdshpu,i|" + wordShape);
 		wordShapes.add("wdshpu,i+1|" + nextToken.wordShape);
 		
 		//word shape bigrams
 		wordShapes.add("wdshpb,i-1,i|" +
 				prevToken.wordShape + " " + wordShape);
 		wordShapes.add("wdshpb,i,i+1|" +
 				wordShape + " " + nextToken.wordShape);
 		
 		//word shape trigrams
 		wordShapes.add("wdshpt,i-2,i-1,i|" + 
 				prev2Token.wordShape + " " +
 				prevToken.wordShape + " " +
 				wordShape);
 		
 		wordShapes.add("wdshpt,i,i+1,i+2|" + 
 				wordShape + " " +
 				nextToken.wordShape + " " +
 				next2Token.wordShape);
 		
 		return wordShapes;
 	}
 
 	/*
 	 * Returns a list of differences between this token's number and its children's numbers in the format:
 	 * chdif|[space-delimited numbers]
 	 * 
 	 * ex: getChildrenDifferences of a token (at position 17) with children at positions 3, 14, 22, 23
 	 * chdif|-14 -3 5 6
 	 * 
 	 */
 	private String getChildrenDifferences() {
 		StringBuilder s = new StringBuilder("chdif|");
 		for (Integer i : childrenIndices)
 			s.append((i - sentenceIndex) + " ");
 
 		return s.toString();
 	}
 	
 
 	/*
 	 * Collection of children's features
 	 * 
 	 * Each string in the child's pposs/split-lemma is represented here:
 	 * c[the child's index in sentence][string in child's feature]
 	 * 
 	 * ex: i feature of split lemma of child token "run" (number 17 in sentence)
 	 * c17splmu,i|run
 	 * 
 	 * 
 	 * Child's relation (deprel) to parent is represented:
 	 * c[child's sentence index]deprel|[deprel]
 	 * 
 	 * ex: deprel of child token (number 2) "president" is "APPO"
 	 * c2deprel|APPO
 	 * 
 	 * 
 	 * Child's pposs/split-lemmas are also added, concatenated with the parent's
 	 * and separated by "||"
 	 * 
 	 */
 	private Collection<String> getChildrenFeatures(){
 		Collection<String> childFeatures = new ArrayList<String>();
 		for (Integer i : childrenIndices){
 			//add child split-lemma
 			FeaturedPredicateToken child = (FeaturedPredicateToken) sentenceTokens.get(i);
 			for (String s : child.getSplitLemmas()){
 				childFeatures.add("c" + 
 						s);
 				childFeatures.add("c" + (sentenceIndex - child.sentenceIndex) +
 						s);
 			}
 
 			//add child pposs
 			for (String s : child.getPPoss()){
 				childFeatures.add("c" + 
 						s);
 				childFeatures.add("c" + (sentenceIndex - child.sentenceIndex) +
 						s);
 			}
 
 			//add child deprel
 			childFeatures.add("cdeprel|" + 
 					child.deprel);
 			childFeatures.add("cdeprel|" + (sentenceIndex - child.sentenceIndex) +
 					child.deprel);
 
 			//add <split_lemma(this), split_lemma(child)>
 			Iterator<String> parentIterator = getSplitLemmas().iterator();
 			Iterator<String> childIterator = child.getSplitLemmas().iterator();
 			while (parentIterator.hasNext() && childIterator.hasNext()){
 				String childString = childIterator.next();
 				String parentString = parentIterator.next();
 				childFeatures.add("cp" + 
 						childString + "||" +
 						parentString);
 				childFeatures.add("cp" + (sentenceIndex - child.sentenceIndex) +
 						childString + "||" +
 						parentString);
 			}
 
 			//add <pposs(this), pposs(child)>
 			parentIterator = getPPoss().iterator();
 			childIterator = child.getPPoss().iterator();
 			while (parentIterator.hasNext() && childIterator.hasNext()){
 				String childString = childIterator.next();
 				String parentString = parentIterator.next();
 				childFeatures.add("cp" + 
 						childString + "||" +
 						parentString);
 				childFeatures.add("cp" + (sentenceIndex - child.sentenceIndex) +
 						childString + "||" +
 						parentString);
 			}
 
 		}
 
 		return childFeatures;
 	}
 
 	/*
 	 * Collection of pposs features in format:
 	 * ppos[u/b],relative word position(s)|pos(s)
 	 * 
 	 */
 	public List<String> getPPoss() {
 		List<String> ppossList = new LinkedList<String>();
 
 		//pposs unigrams
 		ppossList.add("pposu,i-1|"	+ prevToken.pposs);
 		ppossList.add("pposu,i|" + pposs);
 		ppossList.add("pposu,i+1|"	+ nextToken.pposs);
 
 		//pposs bigrams
 		ppossList.add("pposb,i-2,i-1|" +	//<i-2, i-1>
 				prev2Token.pposs + " " + prevToken.pposs);
 		ppossList.add("pposb,i-1,i|" +		//<i-1, i>
 				prevToken.pposs + " " + pposs);
 		ppossList.add("pposb,i,i+1|" +		//<i, i+1>
 				pposs + " " + nextToken.pposs);
 		ppossList.add("pposb,i+1,i+2|" +	//<i, i+1>
 				nextToken.pposs + " " + next2Token.pposs);
 		
 		return ppossList;
 
 	}
 
 	/*
 	 * Collection of split form features in format:
 	 * spfm,relative word position|form
 	 * 
 	 * ex: i-1 token of "drives" in "George Bush drives"
 	 * pposu,i-1|Bush
 	 * 
 	 */
 	public List<String> getSplitForms() {
 		List<String> splitForms = new LinkedList<String>();
 
 		//split-form unigrams
 		splitForms.add("spfm,i-2|" +	 prev2Token.splitForm);
 		splitForms.add("spfm,i-1|" + prevToken.splitForm);
 		splitForms.add("spfm,i|" + splitForm);
 		splitForms.add("spfm,i+1|" + nextToken.splitForm);
 		splitForms.add("spfm,i+2|" + next2Token.splitForm);
 
 		return splitForms;
 
 	}
 
 	/*
 	 * Collection of pposs features in format:
 	 * splm[u/b],relative word position(s)|word(s)|entity(s)|pos(s)
 	 * words, entities, pos's in bigrams are space-delimited
 	 * 
 	 * ex: i-1 token of "drives" in "George Bush drives"
 	 * splmu,i-1|NNP
 	 * 
 	 * ex: i,i+1 tokens of "Bush" in "George Bush drives
 	 * splmb,i,i+1|NNP VBZ
 	 * 
 	 */
 	public List<String> getSplitLemmas() {
 		List<String> splitLemmas = new LinkedList<String>();
 
 		//split-lemma unigrams
 		splitLemmas.add("splmu,i-1|" + prevToken.splitLemma);
 		splitLemmas.add("splmu,i|" + splitLemma);
 		splitLemmas.add("splmu,i+1|" + nextToken.splitLemma);
 
 
 		//split-lemma bigrams
 		splitLemmas.add("splmb,i-1,i|" +		//<i-1, i>
 				prevToken.splitLemma + " " + splitLemma);
 		splitLemmas.add("splmb,i,i+1|" +		//<i, i+1>
 				splitLemma + " " + nextToken.splitLemma);
 
 		return splitLemmas;
 
 	}
 	
 	public Datum<String, String> asDatum(){
 		return new BasicDatum<String, String>(getFeatures(), 
 				isPredicate()?"predicate":"not_predicate");
 	}
 
 }
