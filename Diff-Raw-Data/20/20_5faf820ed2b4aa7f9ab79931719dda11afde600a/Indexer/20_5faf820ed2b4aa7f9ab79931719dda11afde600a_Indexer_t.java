 /**
  * 
  */
 package tools;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 /**
  * Weight file, invertedFiles, tfidf...
  * 
  * @author mhadda1
  * 
  */
 public abstract class Indexer {
 	/**
 	 * Le répertoire du corpus
 	 */
 	// CHEMIN A CHANGER si nécessaire
 	// protected static String COLLECTION_DIRNAME =
 	// "/public/iri/projetIRI/corpus/";
 
 	// Remove the too simple words
 	private static boolean REMOVE_STOP_WORDS = false;
 	// The local stop-words path
 	private static String PATH_TO_STOP_WORDS = null;
 	// Number of files in the corpus
 	private static Integer NB_FILES_IN_CORPUS = null;
 	// For each word, number of document in the corpus containing it
 	private static HashMap<String, Integer> DOCUMENT_FREQUENCY = new HashMap<String, Integer>();
 	// the used normalizer
 	private static Normalizer NORMALIZER = null;
 
 	private static long START_TIME = 0;
 	private static long CURRENT_TIME = 0;
 
 	private static BufferedReader BR = null;
 	private static BufferedWriter BW = null;
 
 	public static String EXTENTION_KEEP = ".txt";
 
 	/**
 	 * Get the number of times a word appears in a file.
 	 * 
 	 * @param fileName
 	 *            the input file
 	 * @param normalizer
 	 *            normalisation class used
 	 * @return map<word, nbOccurences>
 	 * @throws IOException
 	 */
 	public static HashMap<String, Integer> getTermFrequencies(
 			final String fileName, final Normalizer normalizer,
 			final boolean removeStopWords) throws IOException {
 		// Création de la table des mots
 		final HashMap<String, Integer> hits = new HashMap<String, Integer>();
 
 		// TODO !
 		// Appel de la méthode de normalisation
 		// System.out.println(fileName);
 		final ArrayList<String> words = normalizer.normalize(fileName,
 				removeStopWords, Indexer.PATH_TO_STOP_WORDS);
 		Integer number;
 		// Pour chaque mot de la liste, on remplit un dictionnaire
 		// du nombre d'occurrences pour ce mot
 		for (String word : words) {
 			word = word.toLowerCase();
 			// on récupère le nombre d'occurrences pour ce mot
 			number = hits.get(word);
 			// Si ce mot n'était pas encore présent dans le dictionnaire,
 			// on l'ajoute (nombre d'occurrences = 1)
 			if (number == null) {
 				hits.put(word, 1);
 			}
 			// Sinon, on incrémente le nombre d'occurrence
 			else {
 				hits.put(word, ++number);
 			}
 		}
 
 		// // Affichage du résultat
 		// for (final Map.Entry<String, Integer> hit : hits.entrySet()) {
 		// System.out.println(hit.getKey() + "\t" + hit.getValue());
 		// }
 		return hits;
 	}
 
 	/**
 	 * Calculate for each word, the number of document containing it
 	 * 
 	 * @param dirName
 	 *            input directory
 	 * @param normalizer
 	 *            normalisation class used
 	 * @return map<word, nbDocs>
 	 * @throws IOException
 	 */
 	private static HashMap<String, Integer> getDocumentFrequency(final File dir)
 			throws IOException {
 		System.out.println("Calculate document frequency : df");
 		// if result already calculated, make it easy
 		if (!Indexer.DOCUMENT_FREQUENCY.isEmpty()) {
 			return Indexer.DOCUMENT_FREQUENCY;
 		}
 
 		if (!IOManager.checkInDir(dir)) {
 			throw new IOException();
 			// return;
 		}
 
 		Indexer.getDocumentFrequencyRec(dir);
 		return Indexer.DOCUMENT_FREQUENCY;
 	}
 
 	/**
 	 * Go through the tree of files to calculate doc frequency
 	 * 
 	 * @param dir
 	 *            input dir
 	 * @throws IOException
 	 */
 	private static final void getDocumentFrequencyRec(final File dir)
 			throws IOException {
 
 		for (final File f : dir.listFiles()) {
 			if (f.isFile()) {
 				if (!f.getName().endsWith(Indexer.EXTENTION_KEEP)) {
 					continue;
 				}
 				Indexer.analyseOneFileForDocumentFrequency(f);
 			} else {
 				Indexer.getDocumentFrequencyRec(f);
 			}
 		}
 	}
 
 	/**
 	 * Update <words, nbOfOccurencesInTheCorpus>. Can be improved with a "trie"
 	 * Structure for the alreadySeenInTheCurrentFile.
 	 * 
 	 * @param f
 	 *            File used to update the doc freq
 	 * @throws IOException
 	 */
 	private static void analyseOneFileForDocumentFrequency(final File f)
 			throws IOException {
 		System.out.print("-"); // thinking...
 		String wordLC;
 		Integer number;
 		final ArrayList<String> alreadySeenInTheCurrentFile = new ArrayList<String>();
 
 		// normalize
 		final ArrayList<String> words = Indexer.NORMALIZER.normalize(
 				f.getAbsolutePath(), Indexer.REMOVE_STOP_WORDS,
 				Indexer.PATH_TO_STOP_WORDS);
 
 		// increment doc freq
 		for (final String word : words) {
 			wordLC = word.toLowerCase();
 			number = Indexer.DOCUMENT_FREQUENCY.get(wordLC);
 			// (word !in doc_freq)?(add it):(increment freq);
 			if (number == null) {
 				Indexer.DOCUMENT_FREQUENCY.put(wordLC, 1);
 			} else if (!alreadySeenInTheCurrentFile.contains(wordLC)) {
 				Indexer.DOCUMENT_FREQUENCY.put(wordLC, ++number);
 				alreadySeenInTheCurrentFile.add(wordLC);
 			}
 		}
 	}
 
 	/**
 	 * Calculate tfidf
 	 * 
 	 * @param fileName
 	 *            input file
 	 * @param dfs
 	 *            <word, freq in the corpus>
 	 * @param documentNumber
 	 *            nb of docs in the corpus
 	 * @param normalizer
 	 *            normalisation class used
 	 * @param removeStopWords
 	 *            true : remove too usual words
 	 * @return <wordsInFileName, tfidf>
 	 * @throws IOException
 	 */
 	public static HashMap<String, Double> getTfIdf(final String fileName,
 			final HashMap<String, Integer> dfs, final int documentNumber,
 			final Normalizer normalizer, final boolean removeStopWords)
 			throws IOException {
 
 		final HashMap<String, Double> tfIdfs = new HashMap<String, Double>();
 
		Double logtf = 0.0, idf = 0.0;
 		for (final Map.Entry<String, Integer> entry : Indexer
 				.getTermFrequencies(fileName, normalizer, removeStopWords)
 				.entrySet()) {
			// <word, tf>
			if (entry.getValue() == 0) {
				logtf = 0D;
			} else {
				logtf = 1 + Math.log10(entry.getValue());
			}
 			idf = Math.log10(documentNumber / dfs.get(entry.getKey()));
 			// System.out.println(word + "\t" + tfIdf);
			tfIdfs.put(entry.getKey(), logtf * idf);
 		}
 		return tfIdfs;
 	}
 
 	/**
 	 * print file weight recursively according to the parameters given by the
 	 * user
 	 * 
 	 * @param inDir
 	 *            input directory
 	 * @param outDir
 	 *            output directory
 	 * @param n
 	 *            normalizer
 	 * @param removeStopWords
 	 *            true = remove too common words
 	 * @param extension
 	 *            we just look the files with this extension (so far not
 	 *            implemented)
 	 * @throws IOException
 	 */
 	public static void getWeightFiles(final File inDir, final File outDir,
 			final Normalizer n) throws IOException {
 		Indexer.NORMALIZER = n;
 
 		// check inDir and outDir
 		if (!IOManager.checkInDir(inDir) || !IOManager.checkOutDir(outDir)) {
 			throw new IOException();
 			// return;
 		}
 
 		Indexer.START_TIME = System.nanoTime();
 		// initialise the number of files in the corpus
 		System.out.print("Count the number of documents : N = ");
 		if (Indexer.NB_FILES_IN_CORPUS == null) {
 			Indexer.NB_FILES_IN_CORPUS = IOManager
 					.countDocumentRecusively(inDir);
 		}
 		Indexer.CURRENT_TIME = System.nanoTime();
 		System.out.println(Indexer.NB_FILES_IN_CORPUS + "|| temps(ms) = "
 				+ (Indexer.CURRENT_TIME - Indexer.START_TIME) / 1000000);
 
 		// initialise the document frequency in the corpus
 		if (Indexer.DOCUMENT_FREQUENCY.isEmpty()) {
 			Indexer.DOCUMENT_FREQUENCY = Indexer.getDocumentFrequency(inDir);
 		}
 		System.out.println("temps(ms) pour la frequence = "
 				+ (System.nanoTime() - Indexer.CURRENT_TIME) / 1000000);
 		Indexer.CURRENT_TIME = System.nanoTime();
 
 		System.out.println("Datas calculated, start to generate files");
 		Indexer.getWeightFilesRec(inDir, outDir);
 		System.out.println("temps(ms) pour la generation des fichiers = "
 				+ (System.nanoTime() - Indexer.CURRENT_TIME) / 1000000);
 		System.out.println("temps total = "
 				+ (System.nanoTime() - Indexer.START_TIME) / 1000000);
 	}
 
 	/**
 	 * print file weight through the tree of files
 	 * 
 	 * @param inDirName
 	 *            input dir
 	 * @param outDirName
 	 *            output dir
 	 * @throws IOException
 	 */
 	private static void getWeightFilesRec(final File inDir, final File outDir)
 			throws IOException {
 
 		for (final File f : inDir.listFiles()) {
 			// Ignore if you can't read
 			if (!f.canRead()) {
 				continue;
 			}
 
 			// Recursive processing
 			if (f.isDirectory()) {
 				System.out.println("@");
 				// Create output dir
 				final File out = IOManager.createWriteDir(outDir
 						.getAbsolutePath() + File.separator + f.getName());
 				if (out == null) {
 					continue;
 				}
 				// work on the dir recursively
 				Indexer.getWeightFilesRec(f, out);
 			} else {
 				if (!f.getName().endsWith(Indexer.EXTENTION_KEEP)) {
 					continue;
 				}
 				// it's a file, create output file
 				final File out = IOManager.createWriteFile(outDir
 						.getAbsolutePath()
 						+ File.separator
 						+ f.getName()
 						+ ".poid");
 				if (out == null) {
 					continue;
 				}
 				// work on the file
 				Indexer.createWeightFile(f, out);
 			}
 		}
 	}
 
 	/**
 	 * Calculate for a given file his tfidf and print it in a file .poid
 	 * 
 	 * @param inFile
 	 *            input file to analyse
 	 * @param outFile
 	 *            output file .poid
 	 * @throws IOException
 	 */
 	private static void createWeightFile(final File inFile, final File outFile)
 			throws IOException {
 
 		// calculate tf idf
 		final HashMap<String, Double> tfIdf = Indexer.getTfIdf(
 				inFile.getAbsolutePath(), Indexer.DOCUMENT_FREQUENCY,
 				Indexer.NB_FILES_IN_CORPUS, Indexer.NORMALIZER,
 				Indexer.REMOVE_STOP_WORDS);
 
 		// open output
 		Indexer.BW = new BufferedWriter(new OutputStreamWriter(
 				new FileOutputStream(outFile)));
 		System.out.print("-");
 
 		// print
 		for (final Map.Entry<String, Double> eltTfIdf : tfIdf.entrySet()) {
 			Indexer.BW.write(eltTfIdf.getKey() + "\t" + eltTfIdf.getValue()
 					+ "\n");
 		}
 		// time to flush all this :
 		Indexer.BW.close();
 	}
 
 	/**
 	 * Analyse l'ensemble des fichiers d'un repertoire et collecte, pour chaque
 	 * mot, la liste des fichiers dans lesquels ce mot apparait.
 	 * 
 	 * @param dirName
 	 * @param normalizer
 	 * @param removeStopWords
 	 * @return
 	 */
 	public static TreeMap<String, TreeSet<String>> getInvertedFile(
 			final File dir, final Normalizer normalizer,
 			final boolean removeStopWords) throws IOException {
 		// the results <words, <docsName>>
 		final TreeMap<String, TreeSet<String>> res = new TreeMap<String, TreeSet<String>>();
 		// contains the words <words>
 		final TreeSet<String> occurences = new TreeSet<String>();
 		if (dir.exists() && dir.canRead() && dir.isDirectory()) {
 			Iterator it;
 			TreeSet<String> listFiles;
 			ArrayList<String> mots;
 			for (final File f : dir.listFiles()) {
 				// recursively...
 				if (f.isDirectory()) {
 					final TreeMap<String, TreeSet<String>> invertFiles = Indexer
 							.getInvertedFile(f, normalizer, removeStopWords);
 					for (final Map.Entry<String, TreeSet<String>> invertFile : invertFiles
 							.entrySet()) {
 						if (!res.containsKey(invertFile.getKey())) {
 							res.put(invertFile.getKey(), invertFile.getValue());
 						} else {
 							final TreeSet<String> tmpSet = invertFile
 									.getValue();
 							it = tmpSet.iterator();
 							while (it.hasNext()) {
 								final TreeSet<String> tmp = res.get(invertFile
 										.getKey());
 								tmp.add((String) it.next());
 							}
 						}
 					}
 					continue;
 				} // otherwise, this is a file, work on it
				if (!f.getName().endsWith(Indexer.EXTENTION_KEEP)) {
					continue;
				}
 				occurences.clear();
 				mots = normalizer.normalize(f.getAbsolutePath(),
 						removeStopWords, Indexer.PATH_TO_STOP_WORDS);
 				for (final String word : mots) {
 					occurences.add(word);
 				}
 				// put all the words into the tree
 				it = occurences.iterator();
 				while (it.hasNext()) {
 					final String key = (String) it.next();
 					if (res.containsKey(key)
 							&& !res.get(key).contains(f.getName())) {
 						listFiles = res.get(key);
 						listFiles.add(f.getName());
 						// res.put(key, listFiles);
 					} else {
 						listFiles = new TreeSet<String>();
 						listFiles.add(f.getName());
 						res.put(key, listFiles);
 					}
 				}
 			}
 		}
 		return res;
 	}
 
 	/**
 	 * Print the inverted file (syntax = word doc1,doc2,doc3...)
 	 * 
 	 * @param invertedFile
 	 */
 	public static void printInvertedFile(
 			final TreeMap<String, TreeSet<String>> invertedFile) {
 		for (final Map.Entry<String, TreeSet<String>> i : invertedFile
 				.entrySet()) {
 			System.out.print(i.getKey() + "=[");
 			for (final String s : i.getValue()) {
 				System.out.print(s + ", ");
 			}
 			System.out.println("]");
 		}
 	}
 
 	/**
 	 * Sauvegarde un fichier de l'index au format suivant : mot frequence
 	 * doc1,doc2,doc3
 	 * 
 	 * @param invertedFile
 	 * @param outFile
 	 */
 	public static void saveInvertedFile(
 			final TreeMap<String, TreeSet<String>> invertedFile,
 			final File outFile) throws IOException {
 		if (!outFile.exists()) {
 			outFile.createNewFile();
 		}
 		if (outFile.canRead() && outFile.isFile()) {
 			final OutputStream os = new FileOutputStream(outFile);
 			final OutputStreamWriter osr = new OutputStreamWriter(os);
 			final BufferedWriter bw = new BufferedWriter(osr);
 			Iterator it;
 			int size, cpt;
 
 			for (final Map.Entry<String, TreeSet<String>> data : invertedFile
 					.entrySet()) {
 				size = data.getValue().size();
 				bw.append(data.getKey() + "\t" + size + "\t");
 				it = data.getValue().iterator();
 				cpt = 0;
 				while (it.hasNext()) {
 					cpt++;
 					bw.append((String) it.next());
 					if (cpt != size) {
 						bw.append(",");
 					}
 				}
 				bw.append('\n');
 			}
 			bw.close();
 		}
 	}
 
 	/**
 	 * Main function
 	 * 
 	 * @param args
 	 *            command arguments
 	 */
 	public static void main(final String[] args) {
 		if (args.length != 5) {
 			System.err
 					.println("Usage : java "
 							+ Indexer.class.getName()
 							+ " inDirectory outDirectory stopWordsPath removeStopWords extension");
 			System.err.println("Example : java " + Indexer.class.getName()
 					+ " /in /out /stop-words.txt false .html");
 			System.exit(1);
 		}
 		final String inDir = args[0];
 		final String outDir = args[1];
 		Indexer.PATH_TO_STOP_WORDS = args[2];
 		Indexer.REMOVE_STOP_WORDS = Boolean.parseBoolean(new String(args[3]));
 		Indexer.EXTENTION_KEEP = args[4];
 		try {
 			System.out.println("DEBUG: begin");
 
 			final Normalizer n = new FrenchStemmer();
 
 			final File in = new File(inDir);// /public/iri/projetIRI/corpus/0000/000000/
 			final File out = new File(outDir);
 			System.out.println("Launch calculus");
			Indexer.getWeightFiles(in, out, new FrenchStemmer());
 			Indexer.getInvertedFile(in, n, true);
 			final TreeMap<String, TreeSet<String>> invertedFile = Indexer
 					.getInvertedFile(in, n, Indexer.REMOVE_STOP_WORDS);
 			// Indexer.printInvertedFile(invertedFile);
 			final File invertedFileOutput = new File(outDir + "index");
 			invertedFileOutput.createNewFile();
 			Indexer.saveInvertedFile(invertedFile, invertedFileOutput);
 			System.out.println("DEBUG: end");
 
 		} catch (final IOException e) {
 			System.out.println("Problem : " + e);
 			e.printStackTrace();
 		}
 	}
 }
