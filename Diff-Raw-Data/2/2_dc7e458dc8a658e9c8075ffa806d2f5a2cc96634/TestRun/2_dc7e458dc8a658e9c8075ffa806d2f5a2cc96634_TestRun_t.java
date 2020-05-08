 package name.kazennikov.morph.aot;
 
 import gnu.trove.list.array.TIntArrayList;
 import gnu.trove.map.hash.TObjectIntHashMap;
 import gnu.trove.procedure.TObjectIntProcedure;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.xml.bind.JAXBException;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multiset;
 
 import name.kazennikov.dafsa.CharFSA;
 import name.kazennikov.dafsa.CharTrie;
 import name.kazennikov.dafsa.FSAException;
 import name.kazennikov.dafsa.GenericTrie;
 import name.kazennikov.dafsa.IntFSA;
 import name.kazennikov.dafsa.IntNFSA;
 import name.kazennikov.dafsa.IntNFSAv2;
 import name.kazennikov.dafsa.Nodes;
 
 public class TestRun {
 
     public static List<Character> expand(String s) {
         List<Character> chars = new ArrayList<Character>();
 
         for(int i = 0; i != s.length(); i++) {
             chars.add(s.charAt(i));
         }
         return chars;
     }
 
     public static class FSTNode extends GenericTrie.SimpleNode<Character, Set<Integer>, Integer> {
 
         public FSTNode(Set<Integer> fin) {
             super(fin);
         }
 
         @Override
         public GenericTrie.SimpleNode makeNode() {
             return new FSTNode(new HashSet<Integer>());
         }
     }
 	public static void main(String[] args) throws JAXBException, IOException, FSAException {
 		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
 		
 		final MorphLanguage ml = new MorphLanguage(mc);
 		
 		long st = System.currentTimeMillis();
 		MorphDict md = ml.readDict();
 		
 		
 
 		
 
         final TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();
         final TObjectIntHashMap<String> lemmaSet = new TObjectIntHashMap<String>();
         IntFSA fst = new IntFSA.Simple(new Nodes.IntTroveNode());
         CharFSA fsaGold = new CharFSA.Simple(new Nodes.CharTroveNode());
         CharFSA fsa = new CharFSA.Simple(new Nodes.CharSimpleNode());
         CharTrie charTrie = new CharTrie();
         TIntArrayList fstInput = new TIntArrayList();
         long st1 = System.currentTimeMillis();
         
         int count = 0;
 		for(MorphDict.Lemma lemma : md.lemmas) {
 			
 			int lemmaId = lemmaSet.get(lemma.lemma);
 			if(lemmaId == 0) {
 				lemmaId = lemmaSet.size() + 1;
 				lemmaSet.put(lemma.lemma, lemmaId);
 			}
 			
			for(MorphDict.WordForm wf : lemma.expand(true)) {
 				BitSet feats = ml.getWordFormFeats(wf.feats, wf.commonAnCode);
 				int featId = featSets.get(feats);
 				
 				
 				if(featId == 0) {
 					featId = featSets.size() + 1;
 					featSets.put(feats, featId);
 				}
 				
 				count++;
 
 				//System.out.println(wf.wordForm);
 				//MorphCompiler.expand(fstInput, wf.wordForm, wf.lemma);
                 //fst.add(fstInput, featId);
 				//fsaGold.addMinWord(wf.getWordForm(), featId);
 				fsa.addMinWord(wf.getWordForm(), featId);
 				//charTrie.add(wf.getWordForm(), featId);
 				//charTrie.add(fstInput, featId);
 
 				
 //				if(fsa.size() != fsaGold.size()) {
 //					count = count;
 //				}
 			}
 			
 		}
 		
 		System.out.printf("%d ms%n", System.currentTimeMillis() - st1);
 		PrintWriter pw = new PrintWriter("mama.dot");
 		fst.write(new IntFSA.FSTDotFormatter(pw));
 		pw.close();
 
 		st = System.currentTimeMillis() - st;
 		System.out.printf("Elapsed: %d ms%n", st);
         System.out.printf("FSA size: %d%n", fst.size());
         System.out.printf("FSA size: %d%n", fsa.size());
         System.out.printf("charTrie size: %d%n", charTrie.size());
 		System.out.printf("Dict size: %d%n", md.lemmas.size());
 		System.out.printf("featSets: %d%n", featSets.size());
 		System.out.printf("Wordforms: %d%n", count);
 		
 		final AtomicInteger stateFinals = new AtomicInteger(0);
 		
 		charTrie.write(new IntFSA.Events() {
 			int state = 0;
 			
 			@Override
 			public void transitions(int n) throws FSAException {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void transition(int input, int dest) throws FSAException {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void states(int states) throws FSAException {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void stateFinal(int fin) throws FSAException {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void state(int state) throws FSAException {
 				this.state = state;
 			}
 			
 			@Override
 			public void startTransitions() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void startStates() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void startState() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void startFinals() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void finals(int n) throws FSAException {
 				if(n > 0) {
 					stateFinals.incrementAndGet();
 				}
 				
 			}
 			
 			@Override
 			public void endTransitions() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void endStates() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void endState() {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void endFinals() {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		System.out.printf("State with finals (charTrie): %d%n", stateFinals.get());
 		
 		IntNFSAv2.IntNFSABuilder intFSTBuilder = new IntNFSAv2.IntNFSABuilder();
 		fst.write(intFSTBuilder);
 		IntNFSAv2 nfsa = intFSTBuilder.build();
 		
 		final BitSet[] fss = new BitSet[featSets.size() + 1];
 		
 		featSets.forEachEntry(new TObjectIntProcedure<BitSet>() {
 
 			@Override
 			public boolean execute(BitSet a, int b) {
 				fss[b] = a;
 				return true;
 			}
 		});
 		
 	}
 
 }
