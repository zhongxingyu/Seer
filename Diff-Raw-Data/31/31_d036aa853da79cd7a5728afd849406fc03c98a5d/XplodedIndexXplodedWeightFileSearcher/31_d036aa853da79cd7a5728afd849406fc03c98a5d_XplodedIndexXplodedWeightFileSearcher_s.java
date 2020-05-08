 package tools;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import abstractClasses.Searcher;
 import abstractClasses.XplodedIndexSearcher;
 
 public class XplodedIndexXplodedWeightFileSearcher extends XplodedIndexSearcher {
 
 	private final HashMap<Integer, BufferedReader> fileNameByBufferedReader = new HashMap<Integer, BufferedReader>();
 
 	public String getLineStartingWith(final String word, final BufferedReader br)
 			throws IOException {
 		String line = br.readLine();
 		while (line != null) {
 			if (word.compareToIgnoreCase(line.split("\t")[0]) == 0) {
 				return line;
 			}
 			line = br.readLine();
 		}
 		return null;
 	}
 
 	// Fills the DocumentFrequency structure for a given query
 	public void fillDFStructure(final String query,
 			final File invertedFileDirectory) throws IOException {
 
 		final ArrayList<String> queryNormalized = Const.NORMALIZER
 				.normalize(query);
 		for (final String queryWord : queryNormalized) {
 			if (queryWord.length() >= 2) {
 
 				final BufferedReader br = new BufferedReader(
 						new InputStreamReader(new FileInputStream(
 								this.getInvertedFileOfQueryWord(queryWord,
 										invertedFileDirectory))));
 				Integer df = 0;
 				try {
 					df = Integer.parseInt(this.getLineStartingWith(queryWord,
 							br).split("\t")[1]);
 				} catch (final Exception e) {
 					System.err.println("mot " + queryWord + " introuvable");
 				}
 				Searcher.DOCUMENT_FRENQUENCIES_QUERY_WORDS.put(queryWord,
 						df + 1);
 
 			}
 		}
 	}
 
 	public String getCorrespondingDenominatorFile(final String updatedFileName) {
 		return Integer.toString(Integer.parseInt(updatedFileName
 				.substring(0, 6)) + 1) + ".txt";
 	}
 
 	// Retrieves the files containing the query words and performs a similarity
 	// calculus at the same time
 	public TreeMap<Double, TreeSet<String>> getSimilarDocuments(
 			final String query, final String weightsDirectoryPath,
 			final int numberOfDocumentsInTheCorpus,
 			final File invertedFileDirectory) throws IOException {
 
 		// This will contain the result of the method
 		final TreeMap<Double, TreeSet<String>> result = new TreeMap<Double, TreeSet<String>>();
 
 		// This map contains the fileName as an integer the similarity numerator
 		final HashMap<Integer, Double> fileNameByNumerator = new HashMap<Integer, Double>();
 
 		// filling the df structure
 		this.fillDFStructure(query, invertedFileDirectory);
 
 		// Calculating query's words weights
 		final HashMap<String, Double> weightsOfQuery = Indexer
 				.getTfIdf(
 						(InputStream) new ByteArrayInputStream(query.getBytes()),
 						(HashMap<String, Integer>) Searcher.DOCUMENT_FRENQUENCIES_QUERY_WORDS,
 						numberOfDocumentsInTheCorpus + 1, Const.NORMALIZER,
 						Const.REMOVE_STOP_WORDS);
 
 		for (final Map.Entry<String, Double> weightsOfQueryEntry : weightsOfQuery
 				.entrySet()) {
 
 			final String queryWord = weightsOfQueryEntry.getKey();
 			final Double queryWordWeight = weightsOfQueryEntry.getValue();
 
 			if (queryWord.length() >= 2
 					&& Searcher.DOCUMENT_FRENQUENCIES_QUERY_WORDS
 							.get(queryWord) > 1) {
 
 				// Getting into the exploded inverted file containing the query
 				// word
 				final BufferedReader br = new BufferedReader(
 						new InputStreamReader(new FileInputStream(
 								this.getInvertedFileOfQueryWord(queryWord,
 										invertedFileDirectory))));
 
 				// Getting the inverted file line of the query word
 				final String line = this.getLineStartingWith(queryWord, br);
 
 				// Getting the last part of the line
 				final String filesAndWeights = line.split("\t")[2];
 
 				for (final String filesAndWeightsElement : filesAndWeights
 						.split(",")) {
 					final Integer fileName = Integer
 							.parseInt(filesAndWeightsElement.split(":")[0]);
 					final Double queryWordWeightInThatFile = Double
 							.parseDouble(filesAndWeightsElement.split(":")[1]);
 
 					Double lastValue = 0D;
 					if (fileNameByNumerator.containsKey(fileName)) {
 						lastValue = fileNameByNumerator.get(fileName);
 					}
 					fileNameByNumerator.put(fileName, lastValue
 							+ queryWordWeightInThatFile * queryWordWeight);
 				}
 			}
 		}
 		for (final Map.Entry<Integer, Double> fileNameByNumeratorEntry : fileNameByNumerator
 				.entrySet()) {
 			try {
 				final Double numerator = fileNameByNumeratorEntry.getValue();
 				final String fileName = fileNameByNumeratorEntry.getKey()
 						.toString();
 				// Adding the 0s to get a correct fileName
 				// must be in a method
 
 				String updatedFileName = "";
 				final int a = 8 - fileName.length();
 				for (int i = 0; i < a; i++) {
 					updatedFileName += "0";
 				}
 				updatedFileName = updatedFileName + fileName + ".txt";
 
 				// Opening the new weightFile (weightfileTmp)
 				final BufferedReader brOfDenominator;
 				if (this.fileNameByBufferedReader.containsKey(Integer
 						.parseInt(fileName))) {
 					brOfDenominator = this.fileNameByBufferedReader.get(Integer
 							.parseInt(fileName));
 				} else {
 					brOfDenominator = new BufferedReader(
 							new InputStreamReader(
 									new FileInputStream(
 											new File(
 													Const.WEIGHTFILETMP
 															+ this.getCorrespondingDenominatorFile(updatedFileName)))));
 					this.fileNameByBufferedReader.put(
 							Integer.parseInt(fileName), brOfDenominator);
 
 				}
 				// Getting the denominator files' line containing the query word
 				final String lineOfDenominator = this.getLineStartingWith(
 						fileName, brOfDenominator);
 
 				final Double denominator = Double.parseDouble(lineOfDenominator
 						.split("\t")[1]);
 
 				final Double similarity = numerator / denominator;
 
 				TreeSet<String> fileNamesList = new TreeSet<String>();
 				// filling the result structure with the similarity and the
 				// files
 				// having that very same similarity value
 				if (result.containsKey(similarity)) {
 					fileNamesList = result.get(similarity);
 				}
 				fileNamesList.add(updatedFileName);
 				result.put(similarity, fileNamesList);
 				brOfDenominator.close();
 
 			} catch (final IOException e) {
 			}
 		}

 		return result;
 	}
 
 	public TreeMap<Double, TreeSet<String>> getResult(final String query,
 			final File Corpus) throws IOException {
 
 		return this.getSimilarDocuments(query, Const.PATH_TO_WEIGHT_FILES,
 				IOManager.getNbFiles(Corpus), new File(
 						Const.PATH_TO_INVERTED_FILE_FROM_MERGER));
 	}
 }
