 package edu.berkeley.nlp.PCFGLA;
 
 //import ims.hypergraph.EvalBF;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import edu.berkeley.nlp.PCFGLA.smoothing.Smoother;
 import edu.berkeley.nlp.util.Numberer;
 
 public class LatticeLexicon extends SophisticatedLexicon {
 	private Numberer tagNumberer = null;
 	private static double WEIGHT = Double.NaN;
 	private static int THRESHOLD = Integer.MAX_VALUE;
 	private static String LEXICONFILE;
 	protected static Map<String, Map<String, Double>> wordsPerMainPos = new TreeMap<String, Map<String, Double>>();
 	protected static Map<String, Map<String, Double>> mainPosPerWords = new TreeMap<String, Map<String, Double>>();
 	private static boolean IS_TEST = false;
 	private static boolean FIRST_INIT;
 
 	public void init() {
 		try {
 			if (!FIRST_INIT)
 				return;
 			FIRST_INIT = false;
 			// morph_analysis = new HashMap<String, Set<String>>();
 			// BufferedReader reader = new BufferedReader(
 			// new InputStreamReader(
 			// new FileInputStream(
 			// "/mnt/data/project/SPMRL/code/BerkeleyParser/spmrl/morph_analysis"),
 			// "UTF-8"));
 			// String line;
 			// while ((line = reader.readLine()) != null) {
 			// String l[] = line.split("\\s");
 			// if (!morph_analysis.containsKey(l[0]))
 			// morph_analysis.put(l[0], new HashSet<String>());
 			// morph_analysis.get(l[0]).add(l[2].equals("X") ? "N" : l[2]);
 			// }
 			// reader.close();
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					new FileInputStream(LEXICONFILE), "UTF-8"));
 
 			String line;
 			Numberer numberer = Numberer.getGlobalNumberer("tags");
 
 			while ((line = reader.readLine()) != null) {
 				line = line.replaceFirst("^[\\s]*", "");
 				String l[] = line.split("[\\t\\s]");
 				double num = Double.parseDouble(l[0].replaceAll("\\s", ""));
 				String word = l[1];
 				String pos = l[2];
 
 				if (IS_TEST && !numberer.objects().contains(pos))
 					continue;
 
 				double preNums;
 
 				// if (!wordsPerMainPos.containsKey(pos))
 				// wordsPerMainPos.put(pos, new TreeMap<String, Double>());
 				// preNums = wordsPerMainPos.get(pos).containsKey(word) ?
 				// wordsPerMainPos
 				// .get(pos).get(word) : 0;
 				// wordsPerMainPos.get(pos).put(word, preNums + num);
 
 				if (!mainPosPerWords.containsKey(word))
 					mainPosPerWords.put(word, new TreeMap<String, Double>());
 				preNums = mainPosPerWords.get(word).containsKey(pos) ? mainPosPerWords
 						.get(word).get(pos) : 0;
 				mainPosPerWords.get(word).put(pos, preNums + num);
 			}
 			reader.close();
 
 			for (Entry<String, Map<String, Double>> ent : wordsPerMainPos
 					.entrySet()) {
 				double sum = 0.0;
 				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
 					sum += ent2.getValue();
 				}
 				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
 					ent.getValue().put(ent2.getKey(), ent2.getValue() / sum);
 				}
 			}
 
 			for (Entry<String, Map<String, Double>> ent : mainPosPerWords
 					.entrySet()) {
 				double sum = 0.0;
 				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
 					sum += ent2.getValue();
 				}
 				for (Entry<String, Double> ent2 : ent.getValue().entrySet()) {
 					ent.getValue().put(ent2.getKey(), ent2.getValue() / sum);
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public LatticeLexicon(short[] numSubStates, int smoothingCutoff,
 			double[] smoothParam, Smoother smoother, double threshold,
 			Numberer wnum) {
 		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold,
 				wnum);
 		init();
 	}
 
 	public LatticeLexicon(short[] numSubStates, int smoothingCutoff,
 			double[] smoothParam, Smoother smoother, double threshold,
 			Numberer wnum, Numberer tnum) {
 		super(numSubStates, smoothingCutoff, smoothParam, smoother, threshold,
 				wnum);
 		init();
 		tagNumberer = tnum;
 	}
 
 	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff,
 			double[] smoothParam, Smoother smoother, double threshold,
 			Numberer wnum) {
 		super(uwm, numSubStates, smoothingCutoff, smoothParam, smoother,
 				threshold, wnum);
 		init();
 	}
 
 	public LatticeLexicon(int uwm, short[] numSubStates, int smoothingCutoff,
 			double[] smoothParam, Smoother smoother, double threshold,
 			Numberer wnum, Numberer tnum) {
 		super(uwm, numSubStates, smoothingCutoff, smoothParam, smoother,
 				threshold, wnum);
 		init();
 		tagNumberer = tnum;
 	}
 
 	public SophisticatedLexicon splitAllStates(int[] counts,
 			boolean moreSubstatesThanCounts, int mode) {
 		SophisticatedLexicon l = super.splitAllStates(counts,
 				moreSubstatesThanCounts, mode);
 		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
 		return l;
 	}
 
 	public SophisticatedLexicon projectLexicon(double[] condProbs,
 			int[][] mapping, int[][] toSubstateMapping) {
 		SophisticatedLexicon l = super.projectLexicon(condProbs, mapping,
 				toSubstateMapping);
 		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
 		return l;
 	}
 
 	public SophisticatedLexicon copyLexicon() {
 		SophisticatedLexicon l = super.copyLexicon();
 		((LatticeLexicon) l).tagNumberer = this.tagNumberer;
 		return l;
 	}
 
 	public double[] score(String word, short tag, int loc, boolean noSmoothing,
 			boolean isSignature) {
 		double[] resultArray = super.score(word, tag, loc, noSmoothing,
 				isSignature);
 
 		String pos = tagNumberer.object(tag).toString();
 		if (isSignature || wordCounter.getCount(word) > THRESHOLD
 				|| !mainPosPerWords.containsKey(word)) {
 			return resultArray;
 		}
 
		double pb_T_W = 0.0;
 		if (mainPosPerWords.containsKey(word)
 				&& mainPosPerWords.get(word).containsKey(pos)) {
 			pb_T_W = mainPosPerWords.get(word).get(pos);
 		}
 		double c_W = wordCounter.getCount(word);
 		c_W = c_W < 0.1 ? 1f : c_W;
 		double p_W = c_W / totalTokens;
 		for (int substate = 0; substate < resultArray.length; ++substate) {
 			double c_Tseen = tagCounter[tag][substate];
 			double p_T = (c_Tseen / totalTokens);
 			double pb_W_T = pb_T_W * p_W / p_T;
 
 			if (logarithmMode) {
 				resultArray[substate] = Math.exp(resultArray[substate]);
 			}
 
 			// if (c_W == 0) {
 			// resultArray[substate] = pb_W_T;
 			// } else {
 			resultArray[substate] = (resultArray[substate] * c_W + WEIGHT
 					* pb_W_T)
 					/ (c_W + WEIGHT);
 			// }
 		}

 		smoother.smooth(tag, resultArray);
 		if (logarithmMode) {
 			for (int i = 0; i < resultArray.length; i++) {
 				resultArray[i] = Math.log(resultArray[i]);
 				if (Double.isNaN(resultArray[i]))
 					resultArray[i] = Double.NEGATIVE_INFINITY;
 			}
 		}
 
 		return resultArray;
 
 	}
 
 	public void setTagNumberer(Numberer numb) {
 		tagNumberer = numb;
 	}
 
 	public static void main(String[] args) throws Exception {
 		FIRST_INIT = true;
 		if (args.length < 4) {
 			System.out
 					.println("Usage: java edu.berkeley.nlp.PCFGLA.LatticeLexicon train|test weight threshold lexiconFile originalParamters");
 			return;
 		}
 
 		try {
 			LatticeLexicon.WEIGHT = Double.parseDouble(args[1]);
 		} catch (Exception e) {
 			System.out.println("The weight must be a double.");
 			return;
 		}
 
 		try {
 			LatticeLexicon.THRESHOLD = Integer.parseInt(args[2]);
 		} catch (Exception e) {
 			System.out.println("The threshold must be an integer.");
 			return;
 		}
 
 		LEXICONFILE = args[3];
 
 		if (args[0].equalsIgnoreCase("train")) {
 			GrammarTrainer.main(Arrays.copyOfRange(args, 4, args.length));
 		} else if (args[0].equalsIgnoreCase("test")) {
 			IS_TEST = true;
 			BerkeleyParser.main(Arrays.copyOfRange(args, 4, args.length));
 		} else {
 			System.out
 					.println("The first parameter must be \"train\" or \"test\".");
 			return;
 		}
 	}
 }
