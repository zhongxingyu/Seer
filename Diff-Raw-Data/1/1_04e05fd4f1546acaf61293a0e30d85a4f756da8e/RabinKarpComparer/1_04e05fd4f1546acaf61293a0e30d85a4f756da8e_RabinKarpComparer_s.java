 package plagiatssoftware;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class RabinKarpComparer
 {
 	// Basis-Wert: 257 fuer Anzahl Buchstaben des Alphabets
 	private final int	BASE	         = 257;
 	// initialer Modulo-Wert fuer die Hash-Funktion. Muss eine 2er Potenz sein
 	private int	      q	                 = 1024;
 	// damit q-1 nicht bei jeder Moduloberechnung erneut berechnet werden muss
 	private int	      reducedQ	         = q - 1;
 
 	// ab wievielen false matches soll q neu gewaehlt werden? 0 = Zufallsmodus
 	// ausschalten
 	private final int	MAX_FALSEMATCHES	= 1000;
 
 	// Min und Max von q festlegen, z. b. 2^10 - 2^31 Integer: Max 2^31
 	private final int	MIN_Q	         = 10;
 	private final int	MAX_Q	         = 31;
 
 	private int	      _shiftFactor;
 
 	private int	      _falseMatches;
 	private int	      _minQResult;
 	private int	      _qDiff;
 
 	/**
 	 * Beinhaltet Funktionen zum Durchsuchen von Strings mithilfe des RabinKarpAlgorithmus.
 	 * 
 	 * @author Andreas Hahn
 	 */
 	public RabinKarpComparer()
 	{
 		// Minimum fuer q berechnen, pow ist relativ rechenzeitintensiv
 		_minQResult = (int) Math.pow(2, MIN_Q);
 		_qDiff = MAX_Q - MIN_Q + 1;
 	}
 
 	/**
 	 * Wird ausgeloest wenn alle Suchen fertig sind.
 	 * 
 	 * @author Andreas
 	 */
 	public interface OnSearchFinishedListener
 	{
 		abstract void onSearchFinished(ArrayList<String> searchStrings, ArrayList<ArrayList<String>> searchResults);
 	}
 
 	/**
 	 * Die Funktion liefert alle SearchResults fr die Wrter im searchText-Array.
 	 * 
 	 * @param searchText
 	 *            Array mit allen zusammenhngenden Texte/Wrter die gefunden werden sollen.
 	 * @param completeString
 	 *            Text der Durchsucht werden soll
 	 * @return ArrayList mit den SearchResults
 	 */
 	public ArrayList<SearchResult> search(String[] searchText, StringBuilder completeString, String url)
 	{
 		return search(searchText, completeString, url, 0);
 	}
 
 	/**
 	 * Die Funktion liefert alle SearchResults fr die Wrter im searchText-Array.
 	 * 
 	 * @param searchText
 	 *            Array mit allen zusammenhngenden Texte/Wrter die gefunden werden sollen.
 	 * @param completeString
 	 *            Text der Durchsucht werden soll
 	 * @return ArrayList mit den SearchResults
 	 */
 	public ArrayList<SearchResult> search(String[] searchText, StringBuilder completeString, String url, int startReihefolge)
 	{
 		ArrayList<SearchResult> result = new ArrayList<SearchResult>();
 
 		int minNumWords = 5;
 		SearchResult lastNegativSearchResult = null;
 		for (int passedWords = 0; passedWords < searchText.length; passedWords++)
 		{
 			String searchString = "";
 			int numWords;
 			for (numWords = 0; (numWords < minNumWords); numWords++)
 			{
 				if (passedWords + numWords < searchText.length)
 				{
 					searchString += " " + searchText[passedWords + numWords];
 				}
 				else
 				{
 					break;
 				}
 			}
 			int i = 0;
 			if (!searchString.equals(""))
 			{
 				SearchResult searchResult = new SearchResult(0, searchString, "", url, passedWords + startReihefolge);
 				while ((i = searchRabinKarb(searchString, completeString, i)) != 0)
 				{
 					searchResult.setplagiatsText(resultWithOverhead(completeString, i, searchString.length(), 0, 0));
 					searchResult.setorginalText(searchString);
 					if (passedWords + minNumWords >= searchText.length)
 					{
 						break;
 					}
 					else
 					{
 						searchString += " " + searchText[passedWords + minNumWords];
 						passedWords++;
 					}
 				}
 				if (searchResult.getplagiatsText().length() == 0)
 				{
 					if (lastNegativSearchResult == null)
 					{
 						// Legt ein neues Searchresult mit dem ersten nicht gefunden Wort an.
 						String firstString = "";
 						if (passedWords > 0)
 						{
 							firstString += searchText[passedWords - 1] + " ";
 						}
 						firstString += searchText[passedWords];
 						lastNegativSearchResult = new SearchResult(0, firstString, "", "", passedWords + startReihefolge);
 					}
 					else
 					{
 						// Baut das Searchresult mit nicht gefundenen Woertern zusammen. ([passedWords - numWords - 1] =
 						// 1. Wort im Suchstring)
 						lastNegativSearchResult.setorginalText(lastNegativSearchResult.getorginalText() + " " + searchText[passedWords]);
 					}
 				}
 				else
 				{
 					passedWords += (minNumWords - 1);
 					if (lastNegativSearchResult != null)
 					{
 						result.add(lastNegativSearchResult);
 						lastNegativSearchResult = null;
 					}
 					result.add(searchResult);
 				}
 			}
 		}
 		if (lastNegativSearchResult != null)
 		{
 			result.add(lastNegativSearchResult);
 			lastNegativSearchResult = null;
 		}
 		return result;
 	}
 
 	/**
 	 * Schneidet einen Text aus dem gesamten String mit angegebenem Overhead aus.
 	 * 
 	 * @param completeString
 	 *            Kompletter String
 	 * @param position
 	 *            Startposition
 	 * @param searchLength
 	 *            Laenge des String der ausgeschnitten werden soll
 	 * @param overheadBefore
 	 *            Zeichen die vor dem String stehen bleiben sollen
 	 * @param overheadAfter
 	 *            Zeichen die nach dem String stehen bleiben sollen
 	 * @return Ausgeschnittener String mit angegebenem Overhead
 	 */
 	private String resultWithOverhead(StringBuilder completeString, int position, int searchLength, int overheadBefore, int overheadAfter)
 	{
 		String result = completeString.toString();
 
 		int start = position;
 		int after = position + searchLength;
 
 		boolean cuttedAfter = false;
 		boolean cuttedBefore = false;
 		if ((completeString.length() - (position + searchLength)) > overheadAfter)
 		{
 			after += overheadAfter;
 			cuttedAfter = true;
 		}
 		if (position > overheadBefore)
 		{
 			start -= overheadBefore;
 			cuttedBefore = true;
 		}
 
 		result = result.substring(start, after);
 
 		// Zeilenumbrueche entfernen
 		result = result.replace("\r\n", " ");
 		result = result.replace("\n", " ");
 
 		if (cuttedBefore)
 		{
 			result = "[..]" + result;
 		}
 		if (cuttedAfter)
 		{
 			result += "[..]";
 		}
 		return result;
 	}
 
 	/**
 	 * Liefert die Position des ersten Vorkommens ab einer bestimmten Position.
 	 * 
 	 * @param searchString
 	 *            String nach dem gesucht werden soll.
 	 * @param completeString
 	 *            StringBuilder der durchsucht werden soll.
 	 * @param startPosition
 	 *            Position ab der gesucht werden soll.
 	 * @return Position des ersten Vorkommens
 	 */
 	private int searchRabinKarb(String searchString, StringBuilder completeString, int startPosition)
 	{
 		int result = 0;
 
 		int intRandomNumber = 0;
 		// Laenge des gesamten Textes
 		int intLengthComplete = completeString.length();
 		// Laenge des Suchtextes
 		int intLengthSearchString = searchString.length();
 		int intLengthDifference = intLengthComplete - intLengthSearchString;
 		if (intLengthComplete >= startPosition + intLengthSearchString)
 		{
 			// hash-Wert der ersten Zeichen des gesamten Textes
 			int intHashStringPart = hashFirst(completeString.substring(startPosition, startPosition + intLengthSearchString), intLengthSearchString);
 			// Wert des Musters
 			int intHashSearch = hashFirst(searchString, intLengthSearchString);
 
 			// da die Zufallszahlenerzeugung fuer die rand. RK-Algorithmus
 			// essentiell
 			// ist, messen wir die Instanziierung des Random-Objekts natuerlich
 			// jeweils mit
 			Random randomNumbers = new Random();
 
 			// in falseMatches werden die Anzahl "False Matches" gespeichert, also
 			// die Kollisionen
 			_falseMatches = 0;
 
 			// solange Text noch nicht komplett durchlaufen
 			for (int i = startPosition; i <= intLengthDifference; i++)
 			{
 				// wenn Hashwert des Musters mit dem Hashwert des Textausschnittes
 				// uebereinstimmt...
 				if (intHashStringPart == intHashSearch)
 				{
 					// und die Strings an der Stelle auch uebereinstimmen
 					if (completeString.substring(i, i + intLengthSearchString).equals(searchString))
 					{
 						// Uebereinstimmung gefunden
 						result = i;
 						break;
 					}
 					else
 					{
 						_falseMatches++;
 						if (MAX_FALSEMATCHES != 0)
 						{
 							if (_falseMatches == MAX_FALSEMATCHES)
 							{
 								// waehle q neu - eine Zweierpotenz zwischen 2^minQ
 								// bis 2^maxQ
 								intRandomNumber = randomNumbers.nextInt(_qDiff) + MIN_Q;
 								// Schiebeoperatoren sind schneller
 								q = _minQResult << (intRandomNumber - MIN_Q);
 								reducedQ = q - 1;
 								// false matches zuruecksetzen
 								_falseMatches = 0;
 
 								// mit neuem q muss Hash fuer Muster und
 								// Gesamtstring
 								// auch neu berechnet werden
 								intHashSearch = hashFirst(searchString, intLengthSearchString);
 								intHashStringPart = hashFirst(completeString.substring(i, i + intLengthSearchString), intLengthSearchString);
 							}
 						}
 					}
 				}
 				// Bereichsueberlaufsfehler abfangen
 				if ((i + intLengthSearchString + 1) > intLengthComplete) break;
 				// naechsten Hashwert bestimmen
 				intHashStringPart = hash(intHashStringPart, i + 1, intLengthSearchString, completeString);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Berechnung des 1. Hashwertes, von dem aus im Anschluss die neuen Hashes weitergerollt werden. Siehe {@link #hash}
 	 * 
 	 * @param searchText
 	 * @param patternLength
 	 * @return
 	 */
 	private int hashFirst(String searchText, int patternLength)
 	{
 		int result = 0;
 		int reducedBase = 1;
 		for (int i = (patternLength - 1); i >= 0; i--)
 		{
 			if (i != (patternLength - 1)) reducedBase = bitModulo(reducedBase * BASE);
 
 			result += bitModulo(reducedBase * (int) searchText.charAt(i));
 			result = bitModulo(result);
 		}
 		_shiftFactor = reducedBase;
 		result = bitModulo(result);
 
 		return result;
 	}
 
 	/**
 	 * Bitweise Moduloberechnung. Daher wird fuer q eine 2er Potenz benoetigt
 	 * 
 	 * @param x
 	 * @return
 	 */
 	private int bitModulo(int x)
 	{
 		return (x & reducedQ);
 	}
 
 	/**
 	 * Rollende HashFunktion
 	 * 
 	 * @param oldHashValue
 	 * @param startPos
 	 * @param patternLength
 	 * @return
 	 */
 	private int hash(int oldHashValue, int startPos, int patternLength, StringBuilder completeString)
 	{
 		int result = 0;
 		// wenn die gesamte Stringlaenge kleiner als die des Musters ist, kann
 		// das Muster nicht vorkommen
 		if (completeString.length() >= patternLength)
 		{
 			int intValue;
 			int intValue2;
 
 			// das erste Zeichen von links bestimmen, das wegfaellt
 			intValue = (int) completeString.charAt(startPos - 1);
 			// das hinzukommende Zeichen von rechts bestimmen
 			intValue2 = (int) completeString.charAt(startPos + patternLength - 1);
 
 			result = ((oldHashValue - (intValue * _shiftFactor)) * BASE) + intValue2;
 			result = bitModulo(result);
 		}
 		return result;
 	}
 
 
 }
