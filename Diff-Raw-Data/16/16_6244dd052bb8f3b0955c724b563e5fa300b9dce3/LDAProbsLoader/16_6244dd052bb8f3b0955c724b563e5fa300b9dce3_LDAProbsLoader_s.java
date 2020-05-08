 package ethz.nlp.headgen.lda;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import com.googlecode.concurrenttrees.radix.ConcurrentRadixTree;
 import com.googlecode.concurrenttrees.radix.node.concrete.DefaultCharArrayNodeFactory;
 
 import ethz.nlp.headgen.util.ConfigFactory;
 
 public class LDAProbsLoader {
 	public static final String WORD_TOPIC_SUFFIX = "-final.phi";
 	public static final String TOPIC_DOC_SUFFIX = "-final.theta";
 	public static final String WORDMAP = "wordmap.txt";
 	public static final String DOCMAP = "docmap.txt";
 
 	private LDAProbsLoader() {
 	}
 
 	public static LDAProbs loadLDAProbs(File modelDir) throws IOException {
 		return loadLDAProbs(modelDir, "model");
 	}
 
 	public static LDAProbs loadLDAProbs(File modelDir, String modelName)
 			throws IOException {
 		LDAProbsImpl ldaProbs = new LDAProbsImpl(getWordList(modelDir),
 				getDocList(modelDir));
 		loadWordTopicProbs(ldaProbs, new File(modelDir, modelName
 				+ WORD_TOPIC_SUFFIX));
 		loadTopicDocProbs(ldaProbs, new File(modelDir, modelName
 				+ TOPIC_DOC_SUFFIX));
 		return ldaProbs;
 	}
 
 	private static String[] getDocList(File modelDir) throws IOException {
 		BufferedReader br = null;
 		int index = 0;
 		String line;
 		String[] docs;
 		try {
 			br = new BufferedReader(new FileReader(new File(modelDir, DOCMAP)));
 			docs = new String[Integer.parseInt(br.readLine())];
 			while ((line = br.readLine()) != null) {
 				docs[index++] = line;
 			}
 		} finally {
 			if (br != null) {
 				br.close();
 			}
 		}
 		return docs;
 	}
 
 	private static String[] getWordList(File modelDir) throws IOException {
 		BufferedReader br = null;
 		int length, index;
 		String line;
 		String[] vals;
 		String[] words;
 		try {
 			br = new BufferedReader(new FileReader(new File(modelDir, WORDMAP)));
 			length = Integer.parseInt(br.readLine());
 			words = new String[length];
 			while ((line = br.readLine()) != null) {
 				vals = line.split(" ");
 				index = Integer.parseInt(vals[1]);
 				words[index] = vals[0];
 			}
 		} finally {
 			if (br != null) {
 				br.close();
 			}
 		}
 		return words;
 	}
 
 	// FORMAT: Each line is a topic, each column is a word in the vocabulary
 	private static void loadWordTopicProbs(LDAProbsImpl ldaProbs, File file)
 			throws IOException {
 		BufferedReader br = null;
 		String line;
 		String[] vals;
 		int topic = 0;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			while ((line = br.readLine()) != null) {
 				vals = line.split(" ");
 				for (int i = 0; i < vals.length; i++) {
 					ldaProbs.putWordTopic(i, topic, Double.parseDouble(vals[i]));
 				}
 				topic++;
 			}
 			ldaProbs.numTopics = topic;
 		} finally {
 			if (br != null) {
 				br.close();
 			}
 		}
 	}
 
 	// FORMAT: Each line is a document and each column is a topic
 	private static void loadTopicDocProbs(LDAProbsImpl ldaProbs, File file)
 			throws IOException {
 		BufferedReader br = null;
 		String line;
 		String[] vals;
 		int doc = 0;
 		try {
 			br = new BufferedReader(new FileReader(file));
 			while ((line = br.readLine()) != null) {
 				vals = line.split(" ");
 				for (int i = 0; i < vals.length; i++) {
 					ldaProbs.putTopicDoc(i, doc, Double.parseDouble(vals[i]));
 				}
 				doc++;
 			}
 		} finally {
 			if (br != null) {
 				br.close();
 			}
 		}
 	}
 
 	private static class LDAProbsImpl implements LDAProbs {
 		private ConcurrentRadixTree<Double> wordTopicProbs = new ConcurrentRadixTree<Double>(
 				new DefaultCharArrayNodeFactory());
 		private ConcurrentRadixTree<Double> topicDocProbs = new ConcurrentRadixTree<Double>(
 				new DefaultCharArrayNodeFactory());
 		private String[] wordList;
 		private String[] docList;
 		private int numTopics;
 
 		public LDAProbsImpl(String[] wordList, String[] docList) {
 			this.wordList = wordList;
 			this.docList = docList;
 		}
 
 		protected void putWordTopic(int word, int topic, Double prob) {
 			wordTopicProbs.put(getKey(wordList[word], "" + topic), prob);
 		}
 
 		protected void putTopicDoc(int topic, int doc, Double prob) {
 			topicDocProbs.put(getKey("" + topic, docList[doc]), prob);
 		}
 
 		private String getKey(String desired, String given) {
 			return desired + ":" + given;
 		}
 
 		@Override
 		public double getWordTopicProb(String word, int topic) {
 			return wordTopicProbs.getValueForExactKey(getKey(word, "" + topic));
 		}
 
 		@Override
 		public double getTopicDocProb(int topic, String doc) {
 			return topicDocProbs.getValueForExactKey(getKey("" + topic, doc));
 		}
 
 		@Override
 		public String[] getWordList() {
 			return wordList;
 		}
 
 		@Override
 		public String[] getDocList() {
 			return docList;
 		}
 
 		@Override
 		public int getNumTopics() {
 			return numTopics;
 		}
 
 		@Override
 		public int getMostLikelyTopic(String doc) {
 			double topProb = -1, prob;
 			int maxIndex = -1;
 			for (int i = 0; i < numTopics; i++) {
 				prob = getTopicDocProb(i, doc);
 				if (prob > topProb) {
 					maxIndex = i;
 					topProb = prob;
 				}
 			}
 			return maxIndex;
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		LDAConfig config = ConfigFactory.loadConfiguration(LDAConfig.class,
 				"./conf/lda-baseModel.conf");
 		LDAProbs probs = loadLDAProbs(new File(config.getModelDir()));
 		DocCluster cluster = new DocCluster(probs);
 		List<String>[] clusters = cluster.getDocClusters();
 		int count = 1;
 		int topic = 0;
 		for (List<String> c : clusters) {
 			if (c.size() > 10) {
				topic = count-1;
 			}
 			System.out.println("Cluster " + (count++) + " has " + c.size()
 					+ " docs");
 		}
		
 		System.err.println("Getting cluster ngram probs");
 		TreeMap<ArrayList<String>, Double> ngrams = cluster
 				.getClusterNgramProbs(topic, 2);
 		for (Entry<ArrayList<String>, Double> e : ngrams.entrySet()) {
 			System.out.println("Key: " + e.getKey());
 			System.out.println("Value: " + e.getValue());
 			break;
 		}
 		// System.out.println(probs.getNumTopics());
 		// System.out.println(probs.getWordTopicProb(probs.getWordList()[0],
 		// 0));
 		// System.out.println(probs.getTopicDocProb(0, probs.getDocList()[0]));
 	}
 }
