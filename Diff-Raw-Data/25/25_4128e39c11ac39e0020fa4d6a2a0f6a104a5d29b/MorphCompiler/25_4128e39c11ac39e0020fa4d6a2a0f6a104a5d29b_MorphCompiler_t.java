 package name.kazennikov.morph.aot;
 
 import gnu.trove.map.hash.TObjectIntHashMap;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.JAXBException;
 
 import name.kazennikov.morph.fsa.CharFSA;
 import name.kazennikov.morph.fsa.SimpleTrie;
 import name.kazennikov.morph.fsa.Trie;
 
 public class MorphCompiler {
 	MorphLanguage morphLanguage;
 	MorphDict morphDict;
 	public static final Character[] charMap = new Character[65536];
 	
 	static {
 		for(int i = 0; i < charMap.length; i++) {
 			charMap[i] = new Character((char)i);
 		}
 	}
 	
 	public MorphCompiler(MorphLanguage morphLanguage, MorphDict morphDict) {
 		this.morphDict = morphDict;
 		this.morphLanguage = morphLanguage;
 		
 	}
 	
     public static List<Character> expand(String s) {
         List<Character> chars = new ArrayList<Character>();
 
         for(int i = 0; i != s.length(); i++) {
             chars.add(charMap[s.charAt(i)]);
         }
         return chars;
     }
     
     public static List<Character> expandReverse(String s) {
         List<Character> chars = new ArrayList<Character>();
 
         for(int i = 0; i != s.length(); i++) {
             chars.add(charMap[s.charAt(s.length() - 1 - i)]);
         }
         return chars;
     }
 
     
     /*public static List<Pair<Character, Character>> expand(String wordForm, String lemma) {
         List<Pair<Character, Character>> chars = new ArrayList<Pair<Character, Character>>();
         
         int len = Math.max(wordForm.length(), lemma.length());
 
         for(int i = 0; i != len; i++) {
             char wfChar = i < wordForm.length()? wordForm.charAt(i) : 0;
             char lemmaChar = i < lemma.length()? lemma.charAt(i) : 0;
             chars.add(Pair.of(charMap[wfChar], charMap[lemmaChar]));
         }
         return chars;
     }*/
 
     public static class FSANode extends Trie.SimpleNode<Character, Set<Integer>, Integer> {
 
         public FSANode(Set<Integer> fin) {
             super(fin);
         }
 
         @Override
         public Trie.SimpleNode makeNode() {
             return new FSANode(new HashSet<Integer>());
         }
     }
     
     /*public static class FSTNode extends Trie.SimpleNode<Pair<Character, Character>, Set<Integer>, Integer> {
 
         public FSTNode(Set<Integer> fin) {
             super(fin);
         }
 
         @Override
         public Trie.SimpleNode makeNode() {
             return new FSTNode(new HashSet<Integer>());
         }
     }*/
 	public static void main(String[] args) throws JAXBException, IOException {
 		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
 		
 		MorphLanguage ml = new MorphLanguage(mc);
 		
 		long st = System.currentTimeMillis();
 		MorphDict md = ml.readDict();
 		
 
 		
 		CharFSA.IntSimpleTrie chfsa = new CharFSA.IntSimpleTrie(new CharFSA.SimpleNode());
         SimpleTrie<Character, Set<Integer>, Integer> trieFsa =
                 new SimpleTrie<Character, Set<Integer>, Integer>(new FSANode(new HashSet<Integer>()));
         
         /*SimpleTrie<Pair<Character, Character>, Set<Integer>, Integer> trieFst =
                 new SimpleTrie<Pair<Character, Character>, Set<Integer>, Integer>(new FSTNode(new HashSet<Integer>()));
         
         SimpleTrie<Pair<Character, Character>, Set<Integer>, Integer> reverseTrieFst =
                 new SimpleTrie<Pair<Character, Character>, Set<Integer>, Integer>(new FSTNode(new HashSet<Integer>()));*/
 
 
         
         TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();
 
         StringBuilder sb = new StringBuilder();
         int wfNum = 0;
 		for(MorphDict.Lemma lemma : md.lemmas) {
 			for(MorphDict.WordForm wf : lemma.expand()) {
 				BitSet feats = ml.getWordFormFeats(wf.feats, wf.commonAnCode);
 				int featId = featSets.get(feats);
 				
 				if(featId == 0) {
 					featId = featSets.size() + 1;
 					featSets.put(feats, featId);
 				}
 				
 				sb.setLength(0);
 				sb.append(wf.wordForm).reverse();
 				wfNum++;
 				
				//trieFsa.addMinWord(expand(wf.wordForm), featId);
 				chfsa.addMinWord(wf.wordForm, featId);
 				
                 //trieFst.addMinWord(expand(wf.wordForm, wf.lemma), featId);
 				//reverseTrieFst.addMinWord(expand(sb.toString(), new StringBuilder(wf.lemma).reverse().toString()), featId);
 			}
 			
 		}
 
 		st = System.currentTimeMillis() - st;
 		System.out.printf("Elapsed: %d ms%n", st);
         System.out.printf("FSA size: %d%n", trieFsa.size());
         System.out.printf("charFSA size: %d%n", chfsa.size());
         //System.out.printf("FST size: %d%n", reverseTrieFst.size());
         //System.out.printf("Reverse FST size: %d%n", trieFst.size());
 		System.out.printf("Dict size: %d%n", md.lemmas.size());
 		
 	}
 	
 	
 
 }
