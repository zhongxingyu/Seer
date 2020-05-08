 package name.kazennikov.morph.aot;
 
 import gnu.trove.list.array.TIntArrayList;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Objects;
 import com.google.common.base.Splitter;
 
 public class MorphDict {
 	
 	public static class WordForm {
 		String wordForm;
 		String lemma;
 		GramTable.Record feats;
 		GramTable.Record commonAnCode;
 		
 		
 		public WordForm(String wordForm, String lemma, GramTable.Record feats,
 				GramTable.Record commonAnCode) {
 			super();
 			this.wordForm = wordForm;
 			this.lemma = lemma;
 			this.feats = feats;
 			this.commonAnCode = commonAnCode;
 		}
 		
 		@Override
 		public String toString() {
 			return Objects.toStringHelper(this)
 					.add("wordform", wordForm)
 					.add("lemma", lemma)
 					.add("commonFeats", commonAnCode)
 					.add("feats", feats)
 					.toString();
 			
 		}
 
 		public String getWordForm() {
 			return wordForm;
 		}
 
 		public String getLemma() {
 			return lemma;
 		}
 
 		public GramTable.Record getFeats() {
 			return feats;
 		}
 
 		public GramTable.Record getCommonAnCode() {
 			return commonAnCode;
 		}
 	}
 	
 	public static class Lemma {
 		String stem;
 		String lemma;
 		Paradigm paradigm;
 		AccentModel accentModel;
 		GramTable.Record commonFeats;
 		Set<String> prefixes;
 		
 		public Lemma(String stem, Paradigm paradigm, AccentModel accentModel,
 				GramTable.Record commonFeats, Set<String> prefixes) {
 			super();
 			this.stem = stem;
 			this.lemma = constructLemma(stem, paradigm);
 			this.paradigm = paradigm;
 			this.accentModel = accentModel;
 			this.commonFeats = commonFeats;
 			this.prefixes = prefixes;
 		}
 		
 		public String getLemma() {
 			return lemma;
 		}
 		
 		public Paradigm getParadigm() {
 			return paradigm;
 		}
 		
 		
 		public String constructLemma(String stem, Paradigm paradigm) {
 			return stem + paradigm.getNormal().getEnding();
 		}
 		
 		public List<WordForm> expand(boolean processYo) {
 			List<WordForm> result = new ArrayList<MorphDict.WordForm>();
 			StringBuilder sb = new StringBuilder(stem.length());
 			
 			for(int i = 0; i != paradigm.size(); i++) {
 				sb.setLength(0);
 				Paradigm.Entry parEntry = paradigm.get(i);
 				
 				GramTable.Record feats = parEntry.getRec();
 				
 				if(parEntry.hasPrefix()) {
 					sb.append(parEntry.getPrefix());
 				}
 				
 				sb.append(stem);
 				sb.append(parEntry.getEnding());
 				String wf = sb.toString();
 				result.add(new WordForm(wf, lemma, feats, commonFeats));
 				
 				
 				if(processYo && wf.indexOf("Ё") != -1) {
 					wf = wf.replace('Ё', 'Е');
 					result.add(new WordForm(wf, lemma, feats, commonFeats));
 				}
 			}
 			
 			return result;
 		}
 		
 	}
 	
 	public static class AccentModel {
 		public static final int NO_ACCENT = 255;
 		TIntArrayList accents;
 		
 		
 		public AccentModel(TIntArrayList accents) {
 			this.accents = accents;
 		}
 		
 		@Override
 		public String toString() {
 			return accents.toString();
 		}
 		
 		public int getAccent(int wordFormIndex) {
 			return accents.get(wordFormIndex);
 		}
 	}
 	
 	public static interface LemmaProcessor<T> {
 		public boolean process(Lemma lemma);
 		public T getResult();
 	}
 	
 	GramTable gramTable;
 	List<Paradigm> paradigms = new ArrayList<Paradigm>();
 	List<AccentModel> accentModels = new ArrayList<AccentModel>();
 	List<Set<String>> prefixSets = new ArrayList<Set<String>>();
 	List<Lemma> lemmas = new ArrayList<MorphDict.Lemma>();
 	
 	public MorphDict(GramTable gramTable) {
 		this.gramTable = gramTable;
 	}
 	
 	public void readHeader(BufferedReader br) throws IOException {
 		try {
 			readParadigms(br);
 			readAccents(br);
 			skipSection(br); // skip session section
 			readPrefixSets(br);
 		} catch(IOException e) {
 			throw new IOException("Error reading dictionary header", e);
 		}
 	}
 	
 
 	public void read(BufferedReader br) throws IOException {
 		try {
 			readHeader(br);
 			readLemmas(br);
 		} catch(IOException e) {
 			throw new IOException("Error reading dictionary", e);
 		}
 	}
 	
 	public int readSectionSize(BufferedReader br) throws IOException {
 		String l = br.readLine();
 		return Integer.parseInt(l);
 	}
 	
 	public void readParadigms(BufferedReader br) throws IOException {
 		int num = readSectionSize(br);
 		
 		for(int i = 0; i < num; i++) {
 			String line = br.readLine();
 			if(line == null) {
 				throw new IllegalStateException("Corrupted dictionary");
 			}
 			
 			Paradigm paradigm = new Paradigm();
 			// %flexia*gramCode*prefix
 			for(String entry : Splitter.on('%').omitEmptyStrings().split(line)) {
 				int firstStarOffset = entry.indexOf('*');
 				int lastStarOffset = entry.lastIndexOf('*');
 				String prefix = null;
 				
 				if(firstStarOffset != lastStarOffset) {
 					prefix = entry.substring(lastStarOffset + 1);
 				}
 				
 				
 				
 				String ending = entry.substring(0, firstStarOffset);
 				String key = entry.substring(firstStarOffset + 1,firstStarOffset != lastStarOffset? lastStarOffset : entry.length());
 
 				paradigm.addEntry(ending, prefix, gramTable.get(key));
 			}
 			paradigms.add(paradigm);
 		}		
 	}
 	
 	public void skipSection(BufferedReader br) throws IOException {
 		int num = readSectionSize(br);
 		for(int i = 0; i < num; i++) {
 			br.readLine();
 		}
 	}
 	
 	public void readAccents(BufferedReader br) throws IOException {
 		int num = readSectionSize(br);
 		
 		for(int i = 0; i < num; i++) {
 			String line = br.readLine();
 			if(line == null) {
 				throw new IllegalStateException("Corrupted dictionary");
 			}
 			
 			TIntArrayList accentList = new TIntArrayList();
 
 			for(String entry : Splitter.on(';').omitEmptyStrings().split(line)) {
 				accentList.add(Integer.parseInt(entry));
 			}
 			accentModels.add(new AccentModel(accentList));
 		}
 	}
 	
 	public void readPrefixSets(BufferedReader br) throws IOException {
 		int num = readSectionSize(br);
 
 		for(int i = 0; i < num; i++) {
 			String line = br.readLine();
 			
 			if(line == null) {
 				throw new IllegalStateException("Corrupted dictionary");
 			}
 			
 			Set<String> prefixSet = new TreeSet<String>();
 			for(String prefix : Splitter.on(CharMatcher.anyOf(" \t,")).omitEmptyStrings().split(line)) {
 				prefixSet.add(prefix);
 			}
 			
 			prefixSets.add(prefixSet);
 		}
 	}
 	
 	public Lemma parseLemma(String line) {
 		String[] parts = line.split(" ");
 		String base = parts[0];
 		Paradigm paradigm = paradigms.get(Integer.parseInt(parts[1]));
 		AccentModel accentModel = accentModels.get(Integer.parseInt(parts[2]));
 		String session = parts[3];
 		GramTable.Record commonFeats = null;
 		Set<String> prefixSet = null;
 		
 		if(base.equals("#")) {
 			base = "";
 		}
 		
 		if(parts.length > 3 && !parts[4].equals("-")) {
 			commonFeats = gramTable.get(parts[4]);
 		}	
 		
 		if(parts.length > 4 && !parts[5].equals("-")) {
 				prefixSet = prefixSets.get(Integer.parseInt(parts[5]));
 		}	
 
 
 		Lemma lemma = new Lemma(base, paradigm, accentModel, commonFeats, prefixSet);
 		
 		return lemma;
 	}
 	
 	public void readLemmas(BufferedReader br) throws IOException {
 		int num = readSectionSize(br);
 		
 		for(int i = 0; i < num; i++) {
 			String line = br.readLine();
 			
 			if(line == null) {
 				throw new IllegalStateException("Corrupted dictionary");
 			}
 			
 			Lemma lemma = parseLemma(line);
 			lemmas.add(lemma);
 		}
 	}
 	
 	public <T> void processLemmas(BufferedReader br, LemmaProcessor<T> lemmaProcessor) throws IOException {
 		int num = readSectionSize(br);
 		
 		for(int i = 0; i < num; i++) {
 			String line = br.readLine();
 			
 			if(line == null) {
 				throw new IllegalStateException("Corrupted dictionary");
 			}
 			
 			Lemma lemma = parseLemma(line);
 			boolean cont = lemmaProcessor.process(lemma);
 			if(!cont)
 				break;
 		}
 	}
 	
 	public <T> void processLemmas(LemmaProcessor<T> lemmaProcessor) {
 		for(Lemma lemma : lemmas) {
 			lemmaProcessor.process(lemma);
 		}
 	}
 	
 	public List<Lemma> getLemmas() {
 		return lemmas;
 	}
 
 	
 	public void read(File f, Charset charset) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset));
 		try {
 			read(br);
 		} finally {
 			br.close();
 		}
 	}
 	
 	public static void main(String[] args) throws IOException {
 		File path = new File("seman/trunk/Dicts/Morph/rgramtab.tab");
 		GramTable gramTable = new GramTable();
 		gramTable.read(path, Charset.forName("CP1251"));
 		MorphDict md = new MorphDict(gramTable);
 		File dictPath = new File("seman/trunk/Dicts/SrcMorph/RusSrc/morphs.mrd");
 		md.read(dictPath, Charset.forName("CP1251"));
 		int forms = 0;
 		
 		for(Lemma l : md.lemmas) {
 			List<WordForm> wfs = l.expand(true);
 			
 			forms += wfs.size();
 		}
 		
 		System.out.printf("Total wordforms: %d%n", forms);
 	}
 
 	
 	
 	
 
 }
