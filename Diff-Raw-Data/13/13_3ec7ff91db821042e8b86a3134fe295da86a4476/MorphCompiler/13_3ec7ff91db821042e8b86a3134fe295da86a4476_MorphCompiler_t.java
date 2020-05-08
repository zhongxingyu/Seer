 package name.kazennikov.morph.aot;
 
 import gnu.trove.list.TIntList;
 import gnu.trove.list.array.TIntArrayList;
 import gnu.trove.map.hash.TObjectIntHashMap;
 import gnu.trove.procedure.TObjectIntProcedure;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.BitSet;
 
 import javax.xml.bind.JAXBException;
 
 import name.kazennikov.dafsa.CharFSA;
 import name.kazennikov.dafsa.FSAException;
 import name.kazennikov.dafsa.IntFSA;
 import name.kazennikov.dafsa.Nodes;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 
 public class MorphCompiler {
 	
 	public static class GuesserEnding {
 		TIntList ending;
 		int feats;
 		
 		public GuesserEnding(TIntList ending, int feats) {
 			super();
 			this.ending = ending;
 			this.feats = feats;
 		}
 		
 		@Override 
 		public int hashCode() {
 			return Objects.hashCode(ending, feats);
 		}
 		
 		@Override
 		public boolean equals(Object other) {
 			if(!(other instanceof GuesserEnding))
 				return false;
 			
 			GuesserEnding o = (GuesserEnding) other;
 			
 			return Objects.equal(ending, o.ending) && Objects.equal(feats, o.feats);
 		}
 
 		public TIntList getEnding() {
 			return ending;
 		}
 
 		public int getFeats() {
 			return feats;
 		}
 		
 		
 		
 		
 	}
 	
 	MorphLanguage morphLanguage;
 	
 	public MorphCompiler(MorphLanguage morphLanguage) {
 		this.morphLanguage = morphLanguage;	
 	}
 	
 	public void compile(MorphDict md, int predictionDepth, int predictionFreq, File dest) throws FSAException {
 		
 		CharFSA fsa = new CharFSA.Simple(new Nodes.CharTroveNode());
 		IntFSA fst = new IntFSA.Simple(new Nodes.IntTroveNode());
 		
 		IntFSA fstGuesser = new IntFSA.Simple(new Nodes.IntTroveNode());
         
         TObjectIntHashMap<BitSet> featSets = new TObjectIntHashMap<BitSet>();
 
         int wfNum = 0;
         int lemmaNum = 0;
         Multiset<String> predictionSet = HashMultiset.create();
         Multiset<GuesserEnding> fstPredictionSet = HashMultiset.create();
         TIntArrayList fstSeq = new TIntArrayList(32);
         // main loop over the dictionary
         long elapsed = System.currentTimeMillis();
 		for(MorphDict.Lemma lemma : md.getLemmas()) {
 	        lemmaNum++;
 			for(MorphDict.WordForm wf : lemma.expand(true)) {
 				BitSet feats = morphLanguage.getWordFormFeats(wf.getFeats(), wf.getCommonAnCode());
 				int featId = featSets.get(feats);
 				
 				if(featId == 0) {
 					featId = featSets.size() + 1;
 					featSets.put(feats, featId);
 				}
 				
 				wfNum++;
 				
 				final String wordForm = wf.getWordForm();
 				expand(fstSeq, wf.getWordForm(), wf.getLemma());
 				
 				predictionSet.add(new String(wordForm.substring(wordForm.length() > 5? wordForm.length() - 1 - 5 : 0)));
 				fsa.addMinWord(wordForm, featId);
 				fst.addMinWord(fstSeq, featId);
 				
 				TIntList ending = fstSeq.subList(fstSeq.size() > predictionDepth? fstSeq.size() - predictionDepth - 1: 0, fstSeq.size());
 				ending.reverse();
 				fstPredictionSet.add(new GuesserEnding(ending, featId));
 			}
 		}
 		
 	
 		elapsed = System.currentTimeMillis() - elapsed;
 		System.out.printf("Elapsed on fsa building:%d ms%n", elapsed);
		System.out.printf("FSA size:%d%n", fsa.size());
		System.out.printf("FST size:%d%n", fst.size());
		System.out.printf("FST-Prediction size:%d%n", fstPredictionSet.size());
 
 		
 		for(Multiset.Entry<GuesserEnding> e : fstPredictionSet.entrySet()) {
 			if(e.getCount() < predictionFreq)
 				continue;
 			
 			
 			fstGuesser.addMinWord(e.getElement().getEnding(), e.getElement().getFeats());
 		}
 
 		elapsed = System.currentTimeMillis();
 		write(dest, featSets, fsa, fst, fstGuesser);
 		elapsed = System.currentTimeMillis() - elapsed;
 		
 		System.out.printf("Elapsed on writing:%d ms%n", elapsed);
 	}
 	
 	private void write(File dest,TObjectIntHashMap<BitSet> featSets, CharFSA fsa,	IntFSA fst, IntFSA fstGuesser) throws FSAException {
 		DataOutputStream dos = null;
 		try {
 			dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
 			write(dos, featSets);
 			fsa.write(new CharFSA.FileWriter(dos));
 			fst.write(new IntFSA.FileWriter(dos));
 			fstGuesser.write(new IntFSA.FileWriter(dos));
 		} catch(IOException e) {
 			throw new FSAException(e);
 		} finally {
 			if(dos != null) {
 				try {
 					dos.close();
 				} catch(IOException e) {
 					throw new FSAException(e);
 				}
 			}
 		}
 		
 	}
 
 	public void write(DataOutputStream s, TObjectIntHashMap<BitSet> featSets) throws FSAException {
 		final BitSet[] sets = new BitSet[featSets.size() + 1];
 		sets[0] = new BitSet();
 		featSets.forEachEntry(new TObjectIntProcedure<BitSet>() {
 			@Override
 			public boolean execute(BitSet a, int b) {
 				sets[b] = a;
 				return true;
 			}
 		});
 
 		try {
 			s.writeInt(sets.length);
 
 			for(BitSet featSet : sets) {
 				s.writeInt(featSet.cardinality());
 				for(int i = 0; i != featSet.length(); i++) {
 					if(featSet.get(i)) {
 						s.writeInt(i);
 					}
 				}
 			}
 		} catch(IOException e) {
 			throw new FSAException(e);
 		}
 	}
 	
 	
 
 	
     
     public static void expand(TIntArrayList dest, String wf, String lemma) {
     	dest.clear();
     	
     	for(int i = 0; i != Math.max(wf.length(), lemma.length()); i++) {
     		char wfch = i < wf.length()? wf.charAt(i) : 0;
     		char lmch = i < lemma.length()? lemma.charAt(i) : 0;
     		int label = wfch;
     		label <<= 16;
     		label += lmch;
     		dest.add(label);
     	}
     }
     
     public static void expand(TIntArrayList dest, String wf) {
     	dest.clear();
     	
     	for(int i = 0; i != wf.length(); i++) {
     		dest.add(wf.charAt(i));
     	}
     }
     
 	public static void main(String[] args) throws JAXBException, IOException, FSAException {
 		MorphConfig mc = MorphConfig.newInstance(new File("russian.xml"));
 		
 		MorphLanguage ml = new MorphLanguage(mc);
 		
 		long st = System.currentTimeMillis();
 		MorphDict md = ml.readDict();
 		
 		MorphCompiler morphCompiler = new MorphCompiler(ml);
 		
 		morphCompiler.compile(md, 5, 0, new File("russian.dat"));
 		
 	}
 	
 	
 
 }
