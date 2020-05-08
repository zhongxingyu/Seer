 package info.plagiatsjaeger;
 
 import info.plagiatsjaeger.interfaces.IComparer;
 import info.plagiatsjaeger.interfaces.OnCompareFinishedListener;
 import info.plagiatsjaeger.types.CompareResult;
 
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 
 
 /**
  * Die klasse stellt Funktionen zum Vergleichen von Texten zur Verfuegung. Als
  * Schnittstelle dient dass Interface {@link IComparer}.
  * 
  * @author Andreas
  */
 public class MyComparer implements IComparer
 {
 
 	private static final int			NUM_WORDS_TO_COMPARE		= 10;
 	private static final double			SCHWELLENWERT				= 0.9;
 	private static final int			MAX_WORDS_BETWEEN_RESULTS	= 4;
 
 	private String[]					_checkWords;
 	private String[]					_sourceWords;
 
 	private String						_currentLink;
 	private int							_currentDocId;
 
 	private OnCompareFinishedListener	_onCompareFinishedListener;
 
 	private int							_rId;
 	
 	private static final Logger				_logger				= Logger.getLogger(MyComparer.class.getName());
 
 
 	/**
 	 * Legt einen neuen Comparer fuer einen Report an.
 	 * 
 	 * @param rId
 	 */
 	public MyComparer(int rId)
 	{
 		_rId = rId;
 	}
 
 	@Override
 	public void compareText(String checkText, String sourceText, String link)
 	{
 		_currentLink = link;
 		compareText(checkText, sourceText);
 	}
 
 	@Override
 	public void compareText(String checkText, String sourceText, int docId)
 	{
 		_currentDocId = docId;
 		compareText(checkText, sourceText);
 	}
 
 	/**
 	 * Vergleicht 2 Text miteinander und liefert die Uebereinstimmungen als
 	 * ArrayList von {@link CompareResult CompareResults} zurueck.
 	 * 
 	 * @param checkText
 	 * @param sourceText
 	 * @return
 	 */
 	private ArrayList<CompareResult> compareText(String checkText, String sourceText)
 	{
 		ArrayList<CompareResult> result = new ArrayList<CompareResult>();
 		ArrayList<CompareResult> unmergedCompareResults = new ArrayList<CompareResult>();
 
 		WordProcessing wordProcessing = new WordProcessing();
 
 		_checkWords = wordProcessing.splitToWords(checkText);
 		_sourceWords = wordProcessing.splitToWords(sourceText);
 
 		int checkResultStart = -1;
 		int checkResultEnd = -1;
 		int sourceResultStart = -1;
 		int sourceResultEnd = -1;
 
 		StringBuilder sbCheckText = new StringBuilder();
 		StringBuilder sbSourceText = new StringBuilder();
 
 		for (int iCheck = 0; iCheck < _checkWords.length; iCheck++)
 		{
 			sbCheckText.delete(0, sbCheckText.length());
 			int jCheck = 0;
 			for (; (jCheck < NUM_WORDS_TO_COMPARE) && ((iCheck + jCheck) < _checkWords.length); jCheck++)
 			{
 				sbCheckText.append(_checkWords[iCheck + jCheck]).append(" ");
 			}
 
 			double similarity = 0.0;
 			for (int iSource = 0; iSource < _sourceWords.length; iSource++)
 			{
 				sbSourceText.delete(0, sbSourceText.length());
 				int jSource = 0;
 				for (; (jSource < NUM_WORDS_TO_COMPARE) && ((iSource + jSource) < _sourceWords.length); jSource++)
 				{
 					sbSourceText.append(_sourceWords[iSource + jSource]).append(" ");
 				}
 
 				boolean resultFound = false;
 				double sumSimilarity = 0.0;
 				int countSimilarity = 0;
 				while ((similarity = compareStrings(sbCheckText.toString(), sbSourceText.toString())) >= SCHWELLENWERT)
 				{
 					resultFound = true;
 					if (checkResultStart < 0)
 					{
 						checkResultStart = iCheck;
 					}
 					if (sourceResultStart < 0)
 					{
 						sourceResultStart = iSource;
 					}
 					checkResultEnd = iCheck + jCheck + 1;
 					sourceResultEnd = iSource + jSource + 1;
 					sumSimilarity += similarity;
 					countSimilarity++;
 
 					sbCheckText.delete(0, _checkWords[iCheck].length() + 1);
 					sbSourceText.delete(0, _sourceWords[iSource].length() + 1);
 					if (_checkWords.length > (iCheck + NUM_WORDS_TO_COMPARE) && _sourceWords.length > (iSource + NUM_WORDS_TO_COMPARE))
 					{
 						sbCheckText.append(_checkWords[iCheck + NUM_WORDS_TO_COMPARE]).append(" ");
 						sbSourceText.append(_sourceWords[iSource + NUM_WORDS_TO_COMPARE]).append(" ");
 					}
 					else
 					{
 						break;
 					}
 					iCheck++;
 					iSource++;
 				}
 				if (resultFound)
 				{
 					CompareResult compareResult = new CompareResult(_rId, checkResultStart, checkResultEnd, sourceResultStart, sourceResultEnd, sumSimilarity / countSimilarity);
 					unmergedCompareResults.add(compareResult);
 
 					checkResultStart = -1;
 					sourceResultStart = -1;
 					checkResultEnd = -1;
 					sourceResultEnd = -1;
 					break;
 				}
 			}
 			iCheck += NUM_WORDS_TO_COMPARE - 1;
 		}
 
 		// Compareresults zusammenfuegen und Trefferlinks schreiben.
 		for (int resultCounter = 0; resultCounter < unmergedCompareResults.size() - 1; resultCounter++)
 		{
 			CompareResult compareResult1 = unmergedCompareResults.get(resultCounter);
 			CompareResult compareResult2 = unmergedCompareResults.get(resultCounter + 1);
 
 			// Zusammenhaengende Text erkennen und start/end aktualisieren
 			double sumSimilarity = compareResult1.getSimilarity();
 			int countSimilarity = 1;
 			while ((compareResult1.getCheckEnd() >= (compareResult2.getCheckStart() - MAX_WORDS_BETWEEN_RESULTS)) && (compareResult1.getSourceEnd() >= (compareResult2.getSourceStart() - MAX_WORDS_BETWEEN_RESULTS)))
 			{
 				sumSimilarity += compareResult2.getSimilarity();
 				countSimilarity++;
 				compareResult1.setCheckEnd(compareResult2.getCheckEnd());
 				compareResult1.setSourceEnd(compareResult2.getSourceEnd());
 				resultCounter++;
 				if (resultCounter < unmergedCompareResults.size() - 1)
 				{
 					compareResult2 = unmergedCompareResults.get(resultCounter + 1);
 				}
 				else
 				{
 					break;
 				}
 			}
 			compareResult1.setSimilarity(sumSimilarity / countSimilarity);
 			// plagiatsText fuer plagStart/plagEnd setzen.
 			StringBuilder resultText = new StringBuilder();
 			for (int wordCounter = compareResult1.getSourceStart(); wordCounter < compareResult1.getSourceEnd(); wordCounter++)
 			{
 				resultText.append(_sourceWords[wordCounter]).append(" ");
 			}
 			compareResult1.setSourceText(resultText.toString());
 
 			System.out.println("### TREFFER #######################");
 			System.out.println("Source:       " + _currentLink);
 			System.out.println("Start-Ende:   " + compareResult1.getCheckStart() + "-" + compareResult1.getCheckEnd());
 			System.out.println("Text:         " + compareResult1.getSourceText());
 			System.out.println("Aehnlichkeit: " + compareResult1.getSimilarity());
 			System.out.println("###################################");
 			_logger.info("Found link: "+_currentLink +"with Similarity " + compareResult1.getSimilarity());
 			result.add(compareResult1);
 		}
		_onCompareFinishedListener.onLinkFound(result, _currentLink);
		_onCompareFinishedListener.onLinkFound(result, _currentDocId);
 		return result;
 	}
 
 	/**
 	 * Vergleicht zwei eingebene Texte und berechnet deren Uebereinstimmg.
 	 * 
 	 * @return Aehnlichkeit der zwei eingegebenen Texte 0.0-1.0
 	 */
 	public double compareStrings(String text1, String text2)
 	{
 		ArrayList<String> pairs1 = wordLetterPairs(text1.toUpperCase());
 		ArrayList<String> pairs2 = wordLetterPairs(text2.toUpperCase());
 		int intersection = 0;
 		int union = pairs1.size() + pairs2.size();
 		for (int i = 0; i < pairs1.size(); i++)
 		{
 			Object pair1 = pairs1.get(i);
 			for (int j = 0; j < pairs2.size(); j++)
 			{
 				Object pair2 = pairs2.get(j);
 				if (pair1.equals(pair2))
 				{
 					intersection++;
 					pairs2.remove(j);
 					break;
 				}
 			}
 		}
 		return (2.0 * intersection) / union;
 	}
 
 	/**
 	 * Trennt den kompletten Text in Zeichenpaare auf. (Leerzeichen werden
 	 * entfernt)
 	 * 
 	 * @param text
 	 * @return ArrayList<String> mit je 2 Zeichen
 	 */
 	private ArrayList<String> wordLetterPairs(String text)
 	{
 		ArrayList<String> result = new ArrayList<String>();
 		// Tokenize the string and put the tokens/words into an array
 		String[] words = text.split("\\s");
 		// For each word
 		for (int wordCounter = 0; wordCounter < words.length; wordCounter++)
 		{
 			// Find the pairs of characters
 			String[] pairsInWord = letterPairs(words[wordCounter]);
 			for (int p = 0; p < pairsInWord.length; p++)
 			{
 				result.add(pairsInWord[p]);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Trennt ein eingebenen Text(Wort) in Zeichenpaare auf(Alle Zeichen werden
 	 * gleich behandelt)
 	 * 
 	 * @param word
 	 * @return
 	 */
 	private String[] letterPairs(String word)
 	{
 		String[] result = new String[0];
 		int numPairs = word.length() - 1;
 		if (numPairs > 0)
 		{
 			result = new String[numPairs];
 			for (int letterCounter = 0; letterCounter < numPairs; letterCounter++)
 			{
 				result[letterCounter] = word.substring(letterCounter, letterCounter + 2);
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void setOnCompareFinishedListener(OnCompareFinishedListener listener)
 	{
 		_onCompareFinishedListener = listener;
 	}
 }
