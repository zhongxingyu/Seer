 package it.uniroma1.dis.wsngroup.parsing.modules.aggregate;
 
 import it.uniroma1.dis.wsngroup.constants.ParsingConstants;
 import it.uniroma1.dis.wsngroup.parsing.LogParser;
 import it.uniroma1.dis.wsngroup.parsing.representation.GraphRepresentation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 
 public class AdjacencyMatrixModule implements GraphRepresentation {
 	
 	/**
 	 * @author Francesco Ficarola
 	 *
 	 */
 	
 	private Logger logger = Logger.getLogger(this.getClass());
 	
 	private File fileInput;
 	private FileInputStream fis;
 	
 	private Table<String, String, String> adjacencyMatrix;
 	private Integer contactNumber;
 	private String idSourceElement;
 	private String idTargetElement;
 	
 	private Integer currentTS;
 	private Map<ArrayList<String>, ArrayList<Integer>> aggregationOfEdge;
 	private Map<String, Integer> fmSketch;
 
 	public AdjacencyMatrixModule(File fileInput, FileInputStream fis) {
 		this.fileInput = fileInput;
 		this.fis = fis;
 		
 		adjacencyMatrix = HashBasedTable.create();
 		contactNumber = 0;
 		idSourceElement = "";
 		idTargetElement = "";
 		
 		currentTS = 0;
 		aggregationOfEdge = new HashMap<ArrayList<String>, ArrayList<Integer>>();
 		fmSketch = new HashMap<String, Integer>();
 	}
 
 	private void packetParsing(String s, Integer startTS, Integer endTS) throws IOException {
 		logger.debug(s);
 		
 		String currentToken = "";
 		ArrayList<String> tokens = new ArrayList<String>();
 		
 		boolean addToken = false;
 		StringTokenizer st = new StringTokenizer(s, " ");
 		
 		while(st.hasMoreTokens()) {
 			currentToken = st.nextToken();
 						
 			if(currentToken.matches("\\bS\\b") || addToken) {
 				addToken = true;
 				tokens.add(currentToken);
 			}
 			
 			else
 				
 			if(currentToken.matches("\\bC\\b") || addToken) {
 				addToken = true;
 				tokens.add(currentToken);
 			}
 		}
 		
 		
 		// Vincolo sul timestamp
 		if(tokens.size() > 1 && (Integer.parseInt(tokens.get(1).replace("t=", "")) >= startTS &&
 				Integer.parseInt(tokens.get(1).replace("t=", "")) <= endTS)) {
 			
 			currentTS = Integer.parseInt(tokens.get(1).replace("t=", ""));
 			
 			for(int i = 0; i < tokens.size(); i++) {
 
 				/* 
 				 * RILEVAMENTI
 				 */
 				
 				if(tokens.get(0).equals("S")) {
 									
 					if(tokens.get(i).contains("id=")) {
 						idSourceElement = tokens.get(i).replace("id=", "");
 						
 						// Se non esiste l'elemento nell'header riga o colonna
 						// allora provvedo ad aggiungerlo nella matrice
 						if(!adjacencyMatrix.contains(idSourceElement, idSourceElement)) {
 							adjacencyMatrix.put(idSourceElement, idSourceElement, "0");
 						}
 						
 						Integer count = Integer.parseInt(tokens.get(4).replace("boot_count=", ""));
 						
 						if(!fmSketch.containsKey(idSourceElement)) {
 							fmSketch.put(idSourceElement, count);
 						} else {
 							Integer curSketch = fmSketch.get(idSourceElement);
 							fmSketch.put(idSourceElement, Math.max(curSketch, count));
 						}
 					}
 				}
 				
 				else
 				
 				if(tokens.get(0).equals("C")) {
 					
 					if(tokens.get(i).contains("id=")) {
 						idSourceElement = tokens.get(i).replace("id=", "");
 						
 						// Se non esiste l'elemento nell'header riga o colonna
 						// allora provvedo ad aggiungerlo nella matrice
 						if(!adjacencyMatrix.contains(idSourceElement, idSourceElement)) {
 							adjacencyMatrix.put(idSourceElement, idSourceElement, "0");
 						}
 					}
 					
 					else
 					
 					
 					/*
 					 * CONTATTI DIRETTI
 					 */
 					
 					// Espressione regolare per un modello simile a [1203(1) :
 					// \\x5b = [
 					// \\w+ = qualsiasi carattere alfanumerico da 1 a n volte
 					// .* = qualsiasi carattere ripetuto da 0 a n volte
 					// \\x28 = (
 					// \\x29 = )
 					if(tokens.get(i).matches("\\x5b\\w+.*")) {
 						
 						idTargetElement = tokens.get(i).replaceAll("\\x5b|\\x28\\w+\\x29", "");
 						
 						/*
 						 * 1-timestamp edges
 						 */
 						if(LogParser.buildingEdgeMode == ParsingConstants.DEFAULT_EDGE_MODE) {
 							contactNumber++;
 							
 							if(!adjacencyMatrix.contains(idSourceElement, idTargetElement)) {
 								adjacencyMatrix.put(idSourceElement, idTargetElement, "1");
 								adjacencyMatrix.put(idTargetElement, idSourceElement, "1"); //Perpendicular
 							} else {
 								int curWeight = Integer.parseInt(adjacencyMatrix.get(idSourceElement, idTargetElement));
 								curWeight++;
 								adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curWeight));
 								adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curWeight)); //Perpendicular
 							}
 						}
 						
 						else
 							
 						/*
 						 * X-timestamp interval edges
 						 */
 							
 						if(LogParser.buildingEdgeMode == ParsingConstants.INTERVAL_EDGE_MODE) {
 							Integer numOfNeededMsgForInterval = Math.round((float)(LogParser.numberOfIntervalTS * LogParser.percOfDeliveredMsg) / 100);
 														
 							ArrayList<String> edge = new ArrayList<String>();
 							edge.add(idSourceElement);
 							edge.add(idTargetElement);
 							
 							ArrayList<String> edgeInv = new ArrayList<String>();
 							edgeInv.add(idTargetElement);
 							edgeInv.add(idSourceElement);
 								
 							// Se e' presente una chiave con l'arco <source, target> o <target, source>
 							if(aggregationOfEdge.containsKey(edge) && aggregationOfEdge.containsKey(edgeInv)) {
 								
 								// Memorizzo in un array temporaneo informazioni riguardanti il TS ed il contatore presenti
 								// currentInformationTime.get(0) = timestamp
 								// currentInformationTime.get(1) = counter
 								ArrayList<Integer> currentInformationTime = aggregationOfEdge.get(edge);
 								ArrayList<Integer> currentInformationTimeInv = aggregationOfEdge.get(edgeInv);
 								Integer curTagTS = currentInformationTime.get(0);
 								Integer curCounter = currentInformationTime.get(1);
 								
 								// Se il timestamp del nodo e' un multiplo dell'intervallo scelto
 								// o se l'arco non e' comparso in un timestamp multiplo dell'intervallo scelto
 								// ed ha un timestamp antecedente rispetto all'intervallo attuale,
 								// allora ai successivi timestamp creo il contatto per la matrice.
 								// Es. 1 - Se numberOfIntervalTS = 3 e currentTS = 6, allora: 6 % 3 = 0
 								// Es. 2 - Se numberOfIntervalTS = 3, currentTS = 7 e curTagTS = 4, allora:
 								// 4 < 7 - (7 % 3) ==> 4 < 7 - 1 ==> 4 < 6
 								if((currentTS % LogParser.numberOfIntervalTS == 0)
 										|| (curTagTS < currentTS - (currentTS % LogParser.numberOfIntervalTS))) {
 									
 									if(curCounter >= numOfNeededMsgForInterval) {
 										contactNumber++;
 										
 										if(!adjacencyMatrix.contains(idSourceElement, idTargetElement)) {
 											adjacencyMatrix.put(idSourceElement, idTargetElement, "1");
 											adjacencyMatrix.put(idTargetElement, idSourceElement, "1"); //Perpendicular
 										} else {
 											int curWeight = Integer.parseInt(adjacencyMatrix.get(idSourceElement, idTargetElement));
 											curWeight++;
 											adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curWeight));
 											adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curWeight)); //Perpendicular
 										}
 									}
									
									// Azzero i contatori degli array
									currentInformationTime.set(1, 0);
									currentInformationTimeInv.set(1, 0);
 								}
 								
 								// Se il timestamp memorizzato nei tag e' compreso nell'intervallo
 								// [last timestamp multiple of numberOfIntervalTS, current timestamp)
 								if(curTagTS >= (currentTS - (currentTS % LogParser.numberOfIntervalTS)) && curTagTS < currentTS) {
 									
 									// Incremento i contatori per il contatto corrente
 									curCounter++;
 									currentInformationTime.set(1, curCounter);
 									currentInformationTimeInv.set(1, curCounter);
 								}
 								
 								else
 								
 								// Se il timestamp memorizzato nei tag e' piu' antecedente dell'ultimo valore
								// multiplo di numberOfIntervalTS, allora setto i contatori a 1 (nuovo arco)
								if(curTagTS < (currentTS - (currentTS % LogParser.numberOfIntervalTS))) {
 									currentInformationTime.set(1, 1);
 									currentInformationTimeInv.set(1, 1);
 								}
 								
 								// Aggiorno il timestamp degli array con quello corrente
 								currentInformationTime.set(0, currentTS);
 								currentInformationTimeInv.set(0, currentTS);
 							}
 							
 							// Se non e' presente una chiave con l'arco <source, target> o <target, source>
 							else {
 								ArrayList<Integer> informationTime = new ArrayList<Integer>();
 								informationTime.add(currentTS);
 								informationTime.add(1);
 								aggregationOfEdge.put(edge, informationTime);
 								aggregationOfEdge.put(edgeInv, informationTime);
 							}
 						}
 						
 						else
 							
 						/*
 						 * X-continuous-timestamp edges
 						 */
 						
 						if(LogParser.buildingEdgeMode == ParsingConstants.CONTINUOUS_EDGE_MODE) {
 							
 							ArrayList<String> edge = new ArrayList<String>();
 							edge.add(idSourceElement);
 							edge.add(idTargetElement);
 							
 							ArrayList<String> edgeInv = new ArrayList<String>();
 							edgeInv.add(idTargetElement);
 							edgeInv.add(idSourceElement);
 							
 							// Se e' presente una chiave con l'arco <source, target> o <target, source>
 							if(aggregationOfEdge.containsKey(edge) && aggregationOfEdge.containsKey(edgeInv)) {
 								
 								// Memorizzo in array temporanei informazioni riguardanti il TS ed il contatore presenti
 								// currentInformationTime.get(0) = timestamp
 								// currentInformationTime.get(1) = counter
 								ArrayList<Integer> currentInformationTime = aggregationOfEdge.get(edge);
 								ArrayList<Integer> currentInformationTimeInv = aggregationOfEdge.get(edgeInv);
 								Integer curTagTS = currentInformationTime.get(0);
 								
 								// Se il timestamp corrente e' continuo rispetto a quello precedente presente negli array
 								if(currentTS == curTagTS + 1) {
 									
 									// Incremento il contatore degli array
 									Integer curCounter = currentInformationTime.get(1);
 									curCounter++;
 									currentInformationTime.set(1, curCounter);
 									currentInformationTimeInv.set(1, curCounter);
 									
 									// Se c'e' stato un contatto continuo per X timestamps
 									if(curCounter == LogParser.numberOfContinuousTS) {								
 										contactNumber++;
 										
 										// Creo gli archi nella matrice di adiacenza se non presenti...
 										if(!adjacencyMatrix.contains(idSourceElement, idTargetElement)) {
 											adjacencyMatrix.put(idSourceElement, idTargetElement, "1");
 											adjacencyMatrix.put(idTargetElement, idSourceElement, "1"); //Perpendicular
 										} else {
 											// ... altrimenti aggiorno i pesi
 											int curWeight = Integer.parseInt(adjacencyMatrix.get(idSourceElement, idTargetElement));
 											curWeight++;
 											adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curWeight));
 											adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curWeight)); //Perpendicular
 										}
 										
 										// Azzero i contatori degli array
 										currentInformationTime.set(1, 0);
 										currentInformationTimeInv.set(1, 0);
 									}
 								}
 								
 								else
 								
 								// Se il timestamp corrente non e' continuo rispetto a quello precedente
 								if (currentTS > curTagTS + 1) {
 									
 									// Setto a 1 i contatori degli array (nuovo arco)
 									currentInformationTime.set(1, 1);
 									currentInformationTimeInv.set(1, 1);
 								}
 								
 								// Aggiorno il timestamp degli array con quello corrente
 								currentInformationTime.set(0, currentTS);
 								currentInformationTimeInv.set(0, currentTS);
 							}
 							
 							// Se non e' presente una chiave con l'arco <source, target> o <target, source>
 							else {
 								ArrayList<Integer> informationTime = new ArrayList<Integer>();
 								informationTime.add(currentTS);
 								informationTime.add(1);
 								aggregationOfEdge.put(edge, informationTime);
 								aggregationOfEdge.put(edgeInv, informationTime);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 	
 
 	@Override
 	public void readLog(Integer startTS, Integer endTS) throws IOException {
 		logger.info("Reading...");
 		
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new InputStreamReader(fis));
 
 			String currentLine;
 			while((currentLine=br.readLine()) != null) {
 				if(!currentLine.trim().equalsIgnoreCase("")) {
 					logger.debug(currentLine.trim());
 					packetParsing(currentLine.trim(), startTS, endTS);
 				}
 			}
 		} finally {
 			if(br != null) {
 				br.close();
 			}
 		}
 	}
 
 	@Override
 	public void writeFile(String outputFileName, String separator) throws IOException {
 		
 		logger.info("Number of total contacts: " + contactNumber);
 		
 		if(adjacencyMatrix.size() > 0) {
 			logger.info("Writing...");
 			
 			NumberFormat nf = NumberFormat.getInstance();
 			nf.setMaximumFractionDigits(ParsingConstants.MAXIMUM_FRACTION_DIGITS);
 			
 			File f = new File(fileInput.getParentFile() + "/" + outputFileName);
 			FileOutputStream fos = new FileOutputStream(f, true);
 			PrintStream ps = new PrintStream(fos);
 			
 			/* 1st row: header of coloumns */
 			int curElemNum = 0;
 			ps.print(separator);
 			
 			// TreeSet is necessary to sort coloumns
 			Set<String> coloumns = new TreeSet<String>(adjacencyMatrix.columnKeySet());
 			int dim = coloumns.size();
 			Iterator<String> itColoumns = coloumns.iterator();
 			while(itColoumns.hasNext()) {
 				ps.print(itColoumns.next());
 				if(curElemNum < dim-1) {
 					ps.print(separator);
 				}
 				curElemNum++;
 			}
 			
 			ps.println();
 			
 			/* 2nd to n-th row */
 			// TreeSet is necessary to sort rows
 			Set<String> rows = new TreeSet<String>(adjacencyMatrix.rowKeySet());
 			Iterator<String> itRows = rows.iterator();
 			while(itRows.hasNext()) {
 				curElemNum = 0;
 				String curRow = itRows.next();
 				ps.print(curRow + separator);
 				
 				itColoumns = coloumns.iterator();
 				while(itColoumns.hasNext()) {
 					String curCol = itColoumns.next();
 					String curValue = adjacencyMatrix.get(curRow, curCol);
 					if(curValue == null) curValue = "0";
 					double curValueNum = Double.parseDouble(curValue);
 					double weightedContact = curValueNum / contactNumber;
 					ps.print(nf.format(weightedContact));
 					if(curElemNum < dim-1) {
 						ps.print(separator);
 					}
 					curElemNum++;
 				}
 				ps.println();
 			}
 			
 			logger.info("Output file: " + f.getAbsolutePath());
 			ps.close();
 		}
 		
 		
 		/** FM-Sketch part */
 //		double avgEstimation = 0;
 //		int numTags = 0;
 //		Set<Map.Entry<String, Integer>> setSketch = fmSketch.entrySet();
 //		Iterator<Entry<String, Integer>> itSketch = setSketch.iterator();
 //		while(itSketch.hasNext()) {
 //			Entry<String, Integer> entry = itSketch.next();
 //			String id = entry.getKey();
 //			Integer sketch = entry.getValue();
 //			
 //			boolean[] binary = intToBinaryArray(sketch, 16);
 //			int pos_first_zero = 0;
 //			for(int j = 0; j < binary.length; j++) {
 //				boolean c = binary[j];
 //				if(!c) {
 //					pos_first_zero = j;
 //					break;
 //				}
 //			}
 //			double estimation = Math.pow(2, pos_first_zero)/0.77351;
 //			avgEstimation += estimation;
 //			System.out.println(id + "\t" + sketch + "\t" + boolArrayToString(binary) + "\t\t\t" + pos_first_zero + "\t" + estimation);
 //			numTags++;
 //		}
 //		avgEstimation = avgEstimation / numTags;
 //		System.out.println("Estimation: " + avgEstimation);
 	}
 	
 	public boolean[] intToBinaryArray(int number, int length) {
 		boolean[] bits = new boolean[length];
 	    for (int i = length-1; i >= 0; i--) {
 	        bits[i] = (number & (1 << i)) != 0;
 	    }
 	    return bits;
 	}
 
 	public String boolArrayToString(boolean[] binary) {
 		String ret = "";
 		for(int i = binary.length-1; i >= 0; i--) {
 			if(binary[i]) ret += "1";
 			else ret += "0";
 		}
 		return ret;
 	}
 }
