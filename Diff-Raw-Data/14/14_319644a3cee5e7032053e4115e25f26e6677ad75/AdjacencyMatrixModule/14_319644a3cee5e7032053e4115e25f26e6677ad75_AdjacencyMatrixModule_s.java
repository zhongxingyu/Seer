 package it.uniroma1.dis.wsngroup.parsing.modules.aggregate;
 
 import it.uniroma1.dis.wsngroup.constants.ParsingConstants;
 import it.uniroma1.dis.wsngroup.core.Functions;
 import it.uniroma1.dis.wsngroup.parsing.LogParser;
 import it.uniroma1.dis.wsngroup.parsing.representation.GraphRepresentation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 
 import org.apache.log4j.Logger;
 
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.Table;
 
 /**
  * @author Francesco Ficarola
  *
  */
 
 public class AdjacencyMatrixModule implements GraphRepresentation {
 	
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
 	private Map<Integer, Double> estimSketchPerTS;
 
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
 		estimSketchPerTS = new HashMap<Integer, Double>();
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
 		if(tokens.size() > 1 && (Integer.parseInt(tokens.get(ParsingConstants.TIMESTAMP_INDEX).replace(ParsingConstants.TIMESTAMP_PREFIX, "")) >= startTS &&
 				Integer.parseInt(tokens.get(ParsingConstants.TIMESTAMP_INDEX).replace(ParsingConstants.TIMESTAMP_PREFIX, "")) <= endTS)) {
 			
 			currentTS = Integer.parseInt(tokens.get(ParsingConstants.TIMESTAMP_INDEX).replace(ParsingConstants.TIMESTAMP_PREFIX, ""));
 			
 			for(int i = 0; i < tokens.size(); i++) {
 
 				/* 
 				 * RILEVAMENTI
 				 */
 				
 				if(tokens.get(ParsingConstants.TYPE_MESSAGE_INDEX).equals("S")) {
 									
 					if(tokens.get(i).contains(ParsingConstants.SOURCE_PREFIX)) {
 						idSourceElement = tokens.get(i).replace(ParsingConstants.SOURCE_PREFIX, "");
 						
 						// Se non esiste l'elemento nell'header riga o colonna
 						// allora provvedo ad aggiungerlo nella matrice
 						if(!adjacencyMatrix.contains(idSourceElement, idSourceElement)) {
 							adjacencyMatrix.put(idSourceElement, idSourceElement, "0");
 						}
 						
 						Integer count = Integer.parseInt(tokens.get(ParsingConstants.CUSTOM_INDEX).replace(ParsingConstants.CUSTOM_PREFIX, ""));
 						
 						if(!fmSketch.containsKey(idSourceElement)) {
 							fmSketch.put(idSourceElement, count);
 						} else {
 							Integer curSketch = fmSketch.get(idSourceElement);
 							fmSketch.put(idSourceElement, Math.max(curSketch, count));
 						}
 					}
 				}
 				
 				else
 				
 				if(tokens.get(ParsingConstants.TYPE_MESSAGE_INDEX).equals("C")) {
 					
 					if(tokens.get(i).contains(ParsingConstants.SOURCE_PREFIX)) {
 						idSourceElement = tokens.get(i).replace(ParsingConstants.SOURCE_PREFIX, "");
 						
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
 						Integer power = Integer.parseInt(tokens.get(i).replaceAll("\\x5b\\d+|\\x28|\\x29", ""));
 						
 						if(power <= ParsingConstants.MAX_CONTACT_POWER) {
 							idTargetElement = tokens.get(i).replaceAll("\\x5b|\\x28\\w+\\x29", "");
 							
 							// Se non esiste l'elemento nell'header riga o colonna
 							// allora provvedo ad aggiungerlo nella matrice
 							if(!adjacencyMatrix.contains(idTargetElement, idTargetElement)) {
 								adjacencyMatrix.put(idTargetElement, idTargetElement, "0");
 							}
 							
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
 									Integer curEdgeTS = currentInformationTime.get(0);
 									Integer curEdgeCounter = currentInformationTime.get(1);
 									
 									/** 
 									 * SEZIONE AGGIORNAMENTO ARCHI:
 									 * Creazione archi nella matrice d'adiacenza se i contatti avuti nei 
 									 * TS precedenti soddisfano i requisiti di numOfNeededMsgForInterval.
 									 */
 									
 									// Se il timestamp del nodo e' un multiplo dell'intervallo scelto
 									// o se l'arco non e' comparso in un timestamp multiplo dell'intervallo scelto
 									// ed ha un timestamp antecedente rispetto all'intervallo attuale,
 									// allora ai successivi timestamp creo il contatto per la matrice.
 									// Es. 1 - Se numberOfIntervalTS = 3 e currentTS = 6, allora: 6 % 3 = 0
 									// Es. 2 - Se numberOfIntervalTS = 3, currentTS = 7 e curEdgeTS = 4, allora:
 									// 4 < 7 - (7 % 3) ==> 4 < 7 - 1 ==> 4 < 6
 									if((currentTS % LogParser.numberOfIntervalTS == 0)
 											|| (curEdgeTS < currentTS - (currentTS % LogParser.numberOfIntervalTS))) {
 										
 										logger.debug("");
 										logger.debug("UNLOADING - currentTS: " + currentTS);
 										logger.debug("EDGES: " + edge.toString() + " or " + edgeInv.toString());
 										logger.debug("EDGE TS:" + curEdgeTS + ", COUNTER: " + curEdgeCounter);
 										
 										if(curEdgeCounter >= numOfNeededMsgForInterval) {
 											
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
 											
 											/** Change 2013/10/11 : if a link has the required number of contacts then its counter is reset */
 											curEdgeCounter = 0;
 											currentInformationTime.set(1, 0);
 											currentInformationTimeInv.set(1, 0);
 										}
 									}
 									
 									
 									/** SEZIONE AGGIORNAMENTO CONTATTI */
 									
 									// Se il timestamp memorizzato nei tag e' compreso nell'intervallo
 									// [last timestamp multiple of numberOfIntervalTS, current timestamp)
 									if(curEdgeTS >= (currentTS - (currentTS % LogParser.numberOfIntervalTS)) && curEdgeTS < currentTS) {
 										
 										// Incremento i contatori per il contatto corrente
 										curEdgeCounter++;
 										currentInformationTime.set(1, curEdgeCounter);
 										currentInformationTimeInv.set(1, curEdgeCounter);
 									}
 									
 									else
 									
 									// Se il timestamp memorizzato nei tag e' piu' antecedente dell'ultimo valore
 									// multiplo di numberOfIntervalTS, oppure e' uguale al timestamp corrente, 
 									// allora setto i contatori a 1 (nuovo arco)
 									if(curEdgeTS < (currentTS - (currentTS % LogParser.numberOfIntervalTS)) || curEdgeTS.equals(currentTS)) {
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
 								
 								logger.debug(currentTS + ": [" + idSourceElement + "," + idTargetElement + "]");
 								
 								// Se e' presente una chiave con l'arco <source, target> o <target, source>
 								if(aggregationOfEdge.containsKey(edge) && aggregationOfEdge.containsKey(edgeInv)) {
 									
 									// Memorizzo in array temporanei informazioni riguardanti il TS ed il contatore presenti
 									// currentInformationTime.get(0) : timestamp
 									// currentInformationTime.get(1) : counter
 									ArrayList<Integer> currentInformationTime = aggregationOfEdge.get(edge);
 									ArrayList<Integer> currentInformationTimeInv = aggregationOfEdge.get(edgeInv);
 									Integer curEdgeTS = currentInformationTime.get(0);
 									Integer curEdgeTSInv = currentInformationTimeInv.get(0);
 									
 									// Se il timestamp corrente e' continuo rispetto a quello precedente presente negli array
 									if((currentTS == curEdgeTS + 1) || (currentTS == curEdgeTSInv + 1)) {
 										
 										// Incremento il contatore degli array
 										Integer curEdgeCounter = currentInformationTime.get(1);
 										curEdgeCounter++;
 										currentInformationTime.set(1, curEdgeCounter);
 										currentInformationTimeInv.set(1, curEdgeCounter);
 										
 										// Se c'e' stato un contatto continuo per X timestamps
 										if(curEdgeCounter == LogParser.numberOfContinuousTS) {								
 											contactNumber++;
 																						
 											// Creo gli archi nella matrice di adiacenza se non presenti...
 											if(!adjacencyMatrix.contains(idSourceElement, idTargetElement) && !adjacencyMatrix.contains(idTargetElement, idSourceElement)) {
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
 											
 											logger.debug(currentTS + ": " + idSourceElement + "," + idTargetElement);
 										}
 									}
 									
 									else
 									
 									// Se il timestamp corrente non e' continuo rispetto a quello precedente
 									if (currentTS > curEdgeTS + 1) {
 										
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
 							
 							else
 								
 							/*
 							 * X-effective-timestamp edges
 							 */
 							
 							if(LogParser.buildingEdgeMode == ParsingConstants.EFFECTIVE_EDGE_MODE) {
 								ArrayList<String> edge = new ArrayList<String>();
 								edge.add(idSourceElement);
 								edge.add(idTargetElement);
 								
 								ArrayList<String> edgeInv = new ArrayList<String>();
 								edgeInv.add(idTargetElement);
 								edgeInv.add(idSourceElement);
 								
 								logger.debug(currentTS + ": [" + idSourceElement + "," + idTargetElement + "]");
 								
 								// Se e' presente una chiave con l'arco <source, target> o <target, source>
 								if(aggregationOfEdge.containsKey(edge) && aggregationOfEdge.containsKey(edgeInv)) {
 									
 									// Memorizzo in array temporanei informazioni riguardanti il TS ed il contatore presenti
 									// currentInformationTime.get(0) : timestamp
 									// currentInformationTime.get(1) : counter
 									ArrayList<Integer> currentInformationTime = aggregationOfEdge.get(edge);
 									ArrayList<Integer> currentInformationTimeInv = aggregationOfEdge.get(edgeInv);
 									Integer curEdgeTS = currentInformationTime.get(0);
 									Integer curEdgeCounter = currentInformationTime.get(1);
 									
 									// Se la differenza tra il TS corrente e quello memorizzato nell'arco e' minore
 									// di una certa soglia, allora aggiorno il contatore dei pesi dell'arco.
 									// Caso in cui il contatto continua ad esserci...
									if((currentTS - curEdgeTS) <= LogParser.numberOfExpiringTS && !currentTS.equals(curEdgeTS)) {
										Integer updatedEdgeCounter = curEdgeCounter + (currentTS - curEdgeTS);
										currentInformationTime.set(1, updatedEdgeCounter);
										currentInformationTimeInv.set(1, updatedEdgeCounter);
										logger.debug("[CONTINUE] " + currentTS + " (" + curEdgeTS +"): [" + idSourceElement + "," + idTargetElement + "] (" + curEdgeCounter + " --> " + updatedEdgeCounter + ")");
 									}
 									
 									// ... altrimenti, se maggiore, controllo se il contatore presente nell'arco
 									// e' maggiore del numero minimo di TS per formare un arco.
 									// Caso in cui il contatto si e' interrotto, quindi puo' essere aggiunto alla matrice.
 									else {
 										currentInformationTime.set(1, 1);
 										currentInformationTimeInv.set(1, 1);
 										
 										if(curEdgeCounter >= LogParser.numberOfMinTSforContact) {
 											contactNumber += curEdgeCounter;
 											logger.debug("Contact Number: " + contactNumber);
 											
 											// Creo gli archi nella matrice di adiacenza se non presenti...
 											if(!adjacencyMatrix.contains(idSourceElement, idTargetElement) && !adjacencyMatrix.contains(idTargetElement, idSourceElement)) {
 												adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curEdgeCounter));
 												adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curEdgeCounter)); //Perpendicular
 											} else {
 												// ... altrimenti aggiorno i pesi
 												int curWeight = Integer.parseInt(adjacencyMatrix.get(idSourceElement, idTargetElement));
 												curWeight += curEdgeCounter;
 												adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curWeight));
 												adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curWeight)); //Perpendicular
 											}
 											
 											logger.debug("[UPDATE] " + currentTS + " (" + curEdgeTS +"): [" + idSourceElement + "," + idTargetElement + "] (" + curEdgeCounter + " --> 1" + ")");
 										}
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
 									logger.debug("[NEW] " + currentTS +": [" + idSourceElement + "," + idTargetElement + "]");
 								}
 							}
 						}
 					}
 				}
 			}
 			
 			/** FM-Sketch part */
 			if(LogParser.fmSketch) {
 				double avgEstimation = fmAvgEstimation(false);
 				if(!estimSketchPerTS.containsKey(currentTS)) {
 					estimSketchPerTS.put(currentTS, avgEstimation);
 				} else {
 					Double curEstimation = estimSketchPerTS.get(currentTS);
 					estimSketchPerTS.put(currentTS, Math.max(avgEstimation, curEstimation));
 				}
 			}
 		}
 	}
 	
 	private void updateHangingFinalContactsInterval() {
 		Integer numOfNeededMsgForInterval = Math.round((float)(LogParser.numberOfIntervalTS * LogParser.percOfDeliveredMsg) / 100);
 		ArrayList<ArrayList<String>> contactsListAlreadyProcessed = new ArrayList<ArrayList<String>>();
 		
 		Set<Map.Entry<ArrayList<String>, ArrayList<Integer>>> setContacts = aggregationOfEdge.entrySet();
 		Iterator<Entry<ArrayList<String>, ArrayList<Integer>>> itContacts = setContacts.iterator();
 		while(itContacts.hasNext()) {
 			boolean contactAlreadyProcessed = false;
 			Entry<ArrayList<String>, ArrayList<Integer>> entry = itContacts.next();
 			
 			ArrayList<String> contact = entry.getKey();
 			String idSourceElement = contact.get(0);
 			String idTargetElement = contact.get(1);
 			
 			for(ArrayList<String> alreadyContact : contactsListAlreadyProcessed) {
 				if(alreadyContact.get(0).equals(idSourceElement) && alreadyContact.get(1).equals(idTargetElement)
 				|| alreadyContact.get(0).equals(idTargetElement) && alreadyContact.get(1).equals(idSourceElement)) {
 					
 					contactAlreadyProcessed = true;
 				}
 			}
 
 			ArrayList<Integer> informationContact = entry.getValue();
 			Integer contactCounter = informationContact.get(1);
 			
 			if(!contactAlreadyProcessed) {
 				if(contactCounter >= numOfNeededMsgForInterval) {
 					logger.debug("FINAL UNLOADING FOR CONTACT: " + contact.toString());
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
 				
 				// Aggiunta del nuovo contatto (e inverso) nell'array di quelli gia' processati
 				ArrayList<String> alreadyContact = new ArrayList<String>();
 				alreadyContact.add(idSourceElement);
 				alreadyContact.add(idTargetElement);
 				ArrayList<String> alreadyContactInv = new ArrayList<String>();
 				alreadyContactInv.add(idTargetElement);
 				alreadyContactInv.add(idSourceElement);
 				contactsListAlreadyProcessed.add(alreadyContact);
 				contactsListAlreadyProcessed.add(alreadyContactInv);
 			}
 		}
 	}
 	
 	private void updateHangingFinalContactsEffective() {
 		ArrayList<ArrayList<String>> contactsListAlreadyProcessed = new ArrayList<ArrayList<String>>();
 		Set<Map.Entry<ArrayList<String>, ArrayList<Integer>>> setContacts = aggregationOfEdge.entrySet();
 		Iterator<Entry<ArrayList<String>, ArrayList<Integer>>> itContacts = setContacts.iterator();
 		while(itContacts.hasNext()) {
 			boolean contactAlreadyProcessed = false;
 			Entry<ArrayList<String>, ArrayList<Integer>> entry = itContacts.next();
 			ArrayList<String> edge = entry.getKey();
 			String idSourceElement = edge.get(0);
 			String idTargetElement = edge.get(1);
 			ArrayList<Integer> informationContact = entry.getValue();
 			Integer curEdgeTS = informationContact.get(0);
 			Integer curEdgeCounter = informationContact.get(1);
 			
 			for(ArrayList<String> alreadyContact : contactsListAlreadyProcessed) {
 				if(alreadyContact.get(0).equals(idSourceElement) && alreadyContact.get(1).equals(idTargetElement)
 				|| alreadyContact.get(0).equals(idTargetElement) && alreadyContact.get(1).equals(idSourceElement)) {
 					
 					contactAlreadyProcessed = true;
 				}
 			}
 			
 			if(!contactAlreadyProcessed && curEdgeCounter >= LogParser.numberOfMinTSforContact) {
 				contactNumber += curEdgeCounter;
 				
 				// Creo gli archi nella matrice di adiacenza se non presenti...
 				if(!adjacencyMatrix.contains(idSourceElement, idTargetElement) && !adjacencyMatrix.contains(idTargetElement, idSourceElement)) {
 					adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curEdgeCounter));
 					adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curEdgeCounter)); //Perpendicular
 				} else {
 					// ... altrimenti aggiorno i pesi
 					int curWeight = Integer.parseInt(adjacencyMatrix.get(idSourceElement, idTargetElement));
 					curWeight += curEdgeCounter;
 					adjacencyMatrix.put(idSourceElement, idTargetElement, String.valueOf(curWeight));
 					adjacencyMatrix.put(idTargetElement, idSourceElement, String.valueOf(curWeight)); //Perpendicular
 				}
 				logger.debug("[HANGING] " + currentTS + " (" + curEdgeTS +"): [" + idSourceElement + "," + idTargetElement + "] (" + curEdgeCounter + " --> END" + ")");
 				
 				// Aggiunta del nuovo contatto (e inverso) nell'array di quelli gia' processati
 				ArrayList<String> alreadyContact = new ArrayList<String>();
 				alreadyContact.add(idSourceElement);
 				alreadyContact.add(idTargetElement);
 				ArrayList<String> alreadyContactInv = new ArrayList<String>();
 				alreadyContactInv.add(idTargetElement);
 				alreadyContactInv.add(idSourceElement);
 				contactsListAlreadyProcessed.add(alreadyContact);
 				contactsListAlreadyProcessed.add(alreadyContactInv);
 			}
 		}
 	}
 
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
 			
 			if(LogParser.buildingEdgeMode == ParsingConstants.INTERVAL_EDGE_MODE) {
 				updateHangingFinalContactsInterval();
 			} else
 			if(LogParser.buildingEdgeMode == ParsingConstants.EFFECTIVE_EDGE_MODE) {
 				updateHangingFinalContactsEffective();
 			}
 			
 		} finally {
 			if(br != null) {
 				br.close();
 			}
 		}
 	}
 
 	public void writeFile(String outputFileName, String separator) throws IOException {
 		
 		logger.info("Number of total contacts: " + contactNumber);
 		
 		if(contactNumber.equals(0)) {
 			logger.warn("The social graph is empty.");
 			return;
 		}
 		
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
 		if(LogParser.fmSketch) {
 			double avgEstimation = fmAvgEstimation(true);
 			System.out.println("Estimation: " + avgEstimation);
 			writeEstimationtCsv();
 		}
 	}
 	
 	private double fmAvgEstimation(boolean last) {
 		double sumPowerIndex = 0;
 		int numTags = fmSketch.size();
 		Set<Map.Entry<String, Integer>> setSketch = fmSketch.entrySet();
 		Iterator<Entry<String, Integer>> itSketch = setSketch.iterator();
 		while(itSketch.hasNext()) {
 			Entry<String, Integer> entry = itSketch.next();
 			String id = entry.getKey();
 			Integer sketch = entry.getValue();
 			
 			boolean[] binary = Functions.intToBinaryArray(sketch, 16);
 			int pos_first_zero = 0;
 			for(int j = 0; j < binary.length; j++) {
 				boolean c = binary[j];
 				if(!c) {
 					pos_first_zero = j;
 					break;
 				}
 			} 
 			
 			sumPowerIndex += pos_first_zero;
 			if(last) {
 				System.out.println(id + "\t" + sketch + "\t" + Functions.boolArrayToString(binary) + "\t\t\t" + pos_first_zero);
 			}
 		}
 		double avgPowerIndex = (double)sumPowerIndex / numTags;
 		return Math.pow(2, avgPowerIndex)/0.77351;
 	}
 	
 	private void writeEstimationtCsv() throws FileNotFoundException {
 		File fSketch = new File(fileInput.getParentFile() + "/gnuplot_fm_wsdm.csv");
 		FileOutputStream fosSketch = new FileOutputStream(fSketch, true);
 		PrintStream psSketch = new PrintStream(fosSketch);
 		int counterID = 0;
 		Map<Integer, Double> sortedEstimSketchPerTS = new TreeMap<Integer, Double>(estimSketchPerTS);
 		Set<Map.Entry<Integer, Double>> estimSketchSet = sortedEstimSketchPerTS.entrySet();
 		Iterator<Entry<Integer, Double>> itEstim = estimSketchSet.iterator();
 		while(itEstim.hasNext()) {
 			Entry<Integer, Double> curEntry = itEstim.next();
 			Double curAvgEstim = curEntry.getValue();
 			psSketch.println(counterID + " " + curAvgEstim);
 			counterID++;
 		}
 		psSketch.close();
 	}
 }
