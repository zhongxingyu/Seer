 package org.osehra.das.wrapper.nwhin;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.List;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.joda.time.*;
 import org.joda.time.format.*;
 
 import nu.xom.Builder;
 import nu.xom.Document;
 import nu.xom.Element;
 import nu.xom.Elements;
 import nu.xom.Nodes;
 import nu.xom.Node;
 import nu.xom.ParsingException;
 import nu.xom.XPathContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class C32DocumentLogic implements Serializable{
 	
 	private static final Log LOG = LogFactory.getLog(C32DocumentLogic.class);
 
 	public String filterDocument (String document) {
 		
 		filterProblemList(document);
 		filterLabs(document);
 		
 		return document;
 		
 	}
 	
 	
 	public String filterProblemList (String document) {
 	
 		//Variable represents # of days delay on results.
 		int problemListDelayDays = 7;
 		
 		String filteredDocument = "";
 		
 		  try {
 				Builder builder = new Builder();
 				Document doc = builder.build(new StringReader(document));
 
 				/*Print Root Val */
 				Element root = doc.getRootElement();
 				Elements children1 = root.getChildElements();
 
 				/* Walk the file to find labs */
 				/*  Will use effectivetime/Low to filter by date of onset. */
 				
 				for(int a = 0; a < children1.size(); a ++){				
 					//System.out.println(children1.get(a).getLocalName());
 					if (children1.get(a).getLocalName() == "component") {
 						Elements children2 = children1.get(a).getChildElements();
 						for(int b = 0; b < children2.size(); b ++){
 							//System.out.println(children2.get(b).getLocalName());
 							if (children2.get(b).getLocalName() == "structuredBody") {		
 								Elements children3 = children2.get(b).getChildElements();
 								for(int c = 0; c < children3.size(); c ++){
 								//System.out.println(children3.get(c).getLocalName());
 									if (children3.get(c).getLocalName() == "component") {
 									Elements children4 = children3.get(c).getChildElements();
 										for(int d = 0; d < children4.size(); d++) {
 										//System.out.println(children4.get(d).getLocalName());
 											if (children4.get(d).getLocalName() =="section") {
 												// Identify Problem List Section by code.
 												Elements children5 = children4.get(d).getChildElements();
 												for(int e = 0; e < children5.size(); e++) {
 													 if ("11450-4".equals(children5.get(e).getAttributeValue("code"))) {
 														Elements children6 = children4.get(d).getChildElements();
 														for(int f = 0; f < children5.size(); f++) {
 															if (children6.get(f).getLocalName() =="entry") {
 																//System.out.println(children6.get(f).getLocalName());
 																Elements children7 = children6.get(f).getChildElements();
 																for(int g = 0; g < children7.size(); g++) {
 																	if (children7.get(g).getLocalName() == "act") {
 																	//System.out.println(children7.get(g).getLocalName());
 																	Elements children8 = children7.get(g).getChildElements();
 																	for(int h = 0; h < children8.size(); h++) {
 																		if (children8.get(h).getLocalName() == "effectiveTime") {
 																		//System.out.println(children8.get(h).getLocalName());
 																		Elements children9 = children8.get(h).getChildElements();
 																		for (int i = 0; i < children9.size(); i++) {
 																			if (children9.get(i).getLocalName() == "low") {
 																				String ProblemDate = children9.get(i).getAttributeValue("value");
 																				//If Problem Date null, no filter applied.
 																				if (ProblemDate != null && !ProblemDate.isEmpty()) {	
 																					//Labs seem to have a different date, may need to adjust for prod.
 																					DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd");
 																					DateTime dt = formatter.parseDateTime(ProblemDate);
 																					LocalDate probDate = dt.toLocalDate();
 																					LocalDate currentDate = LocalDate.now();
 																					LocalDate minDate = currentDate.minusDays(problemListDelayDays);
 																					//System.out.println(probDate.toString());
 																					//System.out.println(currentDate.toString());
 																					//System.out.println(minDate.toString());
 																					
 																					
 																					//Also blanks entire <text> element, since cannot walk as XML.
 																					if (minDate.isBefore(probDate)) {
 																						//Logic to destroy <entry> element.
 																						children6.get(f).removeChildren();
 																						//Blanks entire <text> element for labs,
 																						//since cannot be individually filtered, and contain non-XML data.
 																						Elements children10 = children4.get(d).getChildElements();
 																						for(int j = 0; j < children10.size(); j++) {
 																							//System.out.println(children10.get(i).getLocalName());
 																							if (children10.get(j).getLocalName() =="text") {
 																								children10.get(j).removeChildren();
 																							}
 																						}
 																					}
 																					
 																					
 																					
 																					
 																				}
 																				
 
 
 																			}
 																					
 																		}
 																	
 																		}
 																		} 
 																	}			
 																}
 															}
 														}			
 													}	
 												}
 											}
 										}
 									}
 								}
 							}						
 						}
 					}
 				}
 				
 				//Return XML if successfully filtered.
 				filteredDocument = doc.toXML();
 
 			  } catch (IOException io) {
 				System.out.println(io.getMessage());
 				LOG.error(io);
 			  } catch (ParsingException parserr) {
 				LOG.error(parserr);
 			  }
 		//Output Test File.  
 		//outputFile(filteredDocument);
 		
 		return filteredDocument;
 		
 		}	
 	
 	public String filterLabs (String document) {
 	
 		//Variable represents # of days delay on results.
 		int LabDelayDays = 7;
 		
 		String filteredDocument = "";
		
 		  try {
 				Builder builder = new Builder();
 				Document doc = builder.build(new StringReader(document));
 
 				/*Print Root Val */
 				Element root = doc.getRootElement();
 				Elements children1 = root.getChildElements();
 
 				/* Walk the file to find labs */
 				
 				for(int a = 0; a < children1.size(); a ++){				
 					//System.out.println(children1.get(a).getLocalName());
 					if (children1.get(a).getLocalName() == "component") {
 						Elements children2 = children1.get(a).getChildElements();
 						for(int b = 0; b < children2.size(); b ++){
 							//System.out.println(children2.get(b).getLocalName());
 							if (children2.get(b).getLocalName() == "structuredBody") {		
 								Elements children3 = children2.get(b).getChildElements();
 								for(int c = 0; c < children3.size(); c ++){
 								//System.out.println(children3.get(c).getLocalName());
 									if (children3.get(c).getLocalName() == "component") {
 									Elements children4 = children3.get(c).getChildElements();
 										for(int d = 0; d < children4.size(); d++) {
 										//System.out.println(children4.get(d).getLocalName());
 											if (children4.get(d).getLocalName() =="section") {
 												// Identify Labs Section by code.
 												Elements children5 = children4.get(d).getChildElements();
 												for(int e = 0; e < children5.size(); e++) {
 													 if ("30954-2".equals(children5.get(e).getAttributeValue("code"))) {
 														Elements children6 = children4.get(d).getChildElements();
 														for(int f = 0; f < children5.size(); f++) {
 															if (children6.get(f).getLocalName() =="entry") {
 																//System.out.println(children6.get(f).getLocalName());
 																Elements children7 = children6.get(f).getChildElements();
 																for(int g = 0; g < children7.size(); g++) {
 																	if (children7.get(g).getLocalName() == "organizer") {
 																	//System.out.println(children7.get(g).getLocalName());
 																	Elements children8 = children7.get(g).getChildElements();
 																	for(int h = 0; h < children8.size(); h++) {
 																		if (children8.get(h).getLocalName() == "effectiveTime") {
 																		//System.out.println(children8.get(h).getLocalName());
 																		//System.out.println(children8.get(h).getAttributeValue("value"));
 																		String LabDate = children8.get(h).getAttributeValue("value");
 																		
 																			//This is the format in the demo file's lab values.  May vary by element.
 																			DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
 																			DateTime dt = formatter.parseDateTime(LabDate);
 																			LocalDate labDate = dt.toLocalDate();
 																			LocalDate currentDate = LocalDate.now();
 																			LocalDate minDate = currentDate.minusDays(LabDelayDays);
 																			//System.out.println(labDate.toString());
 																			//System.out.println(currentDate.toString());
 																			//System.out.println(minDate.toString());
 																			
 																			
 																			
 																			//Also blanks entire <text> element, since cannot walk as XML.
 																			if (minDate.isBefore(labDate)) {
 																				//Logic to destroy <entry> element.
 																				children6.get(f).removeChildren();
 																				//Blanks entire <text> element for labs,
 																				//since cannot be individually filtered, and contain non-XML data.
 																				Elements children9 = children4.get(d).getChildElements();
 																				for(int i = 0; i < children9.size(); i++) {
 																					//System.out.println(children9.get(i).getLocalName());
 																					if (children9.get(i).getLocalName() =="text") {
 																						children9.get(i).removeChildren();
 																					}
 																				}
 																			}
 																	
 																		}
 																		} 
 																	}			
 																}
 															}
 														}			
 													}	
 												}
 											}
 										}
 									}
 								}
 							}						
 						}
 					}
 				}
 				
 				//Return XML if successfully filtered.
 				filteredDocument = doc.toXML();
 
 			  } catch (IOException io) {
 				//System.out.println(io.getMessage());
 				LOG.error(io);
 			  } catch (ParsingException parserr) {
 				LOG.error(parserr);
 			  }
 		//Output Test File.  
 		//outputFile(filteredDocument);
 		
 		return filteredDocument;
 		
 		}
 
 	public String getPatientId (String document) {
 		
 		String patientIdentifier = "";
 		
 		  try {
 				Builder builder = new Builder();
 				Document doc = builder.build(new StringReader(document));
 				// Build Document Root.
 				Element root = doc.getRootElement();
 				Elements children1 = root.getChildElements();
 
 				/* Walk the file to find patient id. */
 				for(int a = 0; a < children1.size(); a ++){				
 					//System.out.println(children1.get(a).getLocalName());
 					if (children1.get(a).getLocalName() == "recordTarget") {
 						Elements children2 = children1.get(a).getChildElements();
 						for(int b = 0; b < children2.size(); b ++){
 							//System.out.println(children2.get(b).getLocalName());
 							if (children2.get(b).getLocalName() == "patientRole") {		
 								Elements children3 = children2.get(b).getChildElements();
 								for(int c = 0; c < children3.size(); c ++){
 								//System.out.println(children3.get(c).getLocalName());
 									if (children3.get(c).getLocalName() == "id") {
 										//System.out.println(children3.get(c).getAttributeValue("extension"));
 										patientIdentifier = children3.get(c).getAttributeValue("extension");
 									}
 								}	
 							}						
 						}
 					}
 				}
 
 			  } catch (IOException io) {
 				System.out.println(io.getMessage());
 				LOG.error(io);
 			  } catch (ParsingException parserr) {
 				LOG.error(parserr);
 			  }
		
 		//Output Test File if Testing.
 		//outputFile(patientIdentifier);
 		
 		return patientIdentifier;
 		
 		}
 	
 	
 	
 	
 	
 	//This class only used in testing.
 	private void outputFile (String outputDocument) {
 	try {
 		 
 		File file = new File("/SAMPLE_RECORD_CONTENT.xml");
 
 		if (!file.exists()) {
 			file.createNewFile();
 		}
 
 		FileWriter fw = new FileWriter(file.getAbsoluteFile());
 		BufferedWriter bw = new BufferedWriter(fw);
 		bw.write(outputDocument);
 		bw.close();
 	} catch (IOException e) {
 		e.printStackTrace();
 	}
 	
 	}	
 	
 }
