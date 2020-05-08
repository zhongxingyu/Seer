 /* 
  * Parsing .dev (performance features), .xml (score features) and .apex.xml (structural info)
  * and making a single .csv  
  *
  * v 0.7
  *
  * Elements in the csv file:
  *      1. Part OK
  * 	2. Staff OK
  *	3. Measure OK
  *      4. Key
  *      5. Clef
  *	6. Beat position(tactus) OK
  *	7. Note number OK
  *      8. Note Name
  *	9. Duration OK
  *	10. Time signature OK	
  *	11. Slur OK
  *	12. Expression marks - dynamics OK
  *	13. Expression marks - wedge(cresc, dim.) OK
  *	14. Expression marks - tempo(ritardando, accel.) OK 
  *	15. Articulation - staccato, legato, fermata OK
  *      16. Arpeggio
  *      17. Ornaments
  *	18. Attack Time OK
  *	19. Release Time OK
  *	20. Tempo OK
  *	21. Tempo Deviation OK
  *	22. Dynamics OK
  *	23. Grouping (reserved)
  *
  * Taehun Kim
  * Audio Communcation Group, TU Berlin
  * April 2012
  */
 
 import jp.crestmuse.cmx.commands.*;
 import jp.crestmuse.cmx.filewrappers.*;
 
 import java.io.*;
 import java.util.*;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.w3c.dom.Element;
 
 import javax.xml.parsers.*;
 
 
 public class ScoreDeviationGroupingToCSV extends CMXCommand {
 
     private Hashtable<String, List> placeholder;
     private Hashtable<Integer, Integer> grouping_indicator_dict;
     
     private String scoreFileName;
     private String outputFileName;
     private String apexFileName;
     private String targetDir;
     public boolean isMusicXML;  // if this is no, input file is .dev  
     private MusicXMLWrapper musicxml;
     private DeviationInstanceWrapper dev;
 	
     private void writeToFile() throws IOException {
 	// get file pointer of the target file
 	File outFile = new File(outputFileName);
 	
 	// make a buffer to write a file
 	java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(outFile));
 	
 	writer.write("$score_file: "+ scoreFileName);
 	writer.newLine();
 	if (!isMusicXML) {
 	    writer.write("$performance_file: "+ dev.getFileName());
 	} else {
 	    writer.write("$performance_file: NA");
 	}
 	writer.newLine();
 	
 	// write the header
 	if (musicxml.hasMovementTitle()) {
 	    writer.write("$title: " + musicxml.getMovementTitle());
 	    writer.newLine();
 	}
 	double avg_tempo = -1;
 	if (!isMusicXML) {
 	    avg_tempo = dev.searchNonPartwiseControl(1,0.2, "tempo").value();
 	}
 	writer.write("$avg_tempo: " + avg_tempo);
 	writer.newLine();
 	writer.write("$legend: Staff,Voice,Key,Clef,Measure,BeatPos,Metric,Notenum,NoteName,Duration,TimeSig,Slur,DynMark,Wedge,TempoMark,Articulation,Arpeggio,Ornaments,*Attack(0),*Release(1),*Tempo(2),*RelTempo(3),*Velocity(4),*GrpStr_Manual(5)");
 	writer.newLine();
 	writer.flush();
 		
 	// write the body
 	String buffer[] = new String[placeholder.keySet().size()];
 	for (Enumeration ks = placeholder.keys(); ks.hasMoreElements();) {
 	    // each key has its xpath. the first element is the position of the note
 	    String xpath = (String)(ks.nextElement());
 	    List aList = placeholder.get(xpath);
 	    int note_position = (Integer)(aList.get(0));
 	    String writeLine = "";
 	    for (int j = 1; j < aList.size(); j++) {
 		writeLine = writeLine+aList.get(j)+",";
 	    }
 	    writeLine = writeLine.substring(0, writeLine.length() - 1);	// remove the last comma (,)
 	    buffer[note_position] = writeLine;
 	}
 
 	for (int i = 0; i < buffer.length; i++) {
 	    System.out.println(buffer[i]);
 	    writer.write(buffer[i]);
 	    writer.newLine();
 	}
 	
 	// close file writer
 	writer.write("-EOF-\n");
 	writer.flush();
 	writer.close();
     }
 
     private void scoreDevToPlaceholder() {
 
 		String timeSig = "NA";
 		String exDyn = "NA";
 		String exDyn_sf = "NA";	// for sf-family. sf is just for one note!
 		String exTmp = "NA";
 		String exWedge = "NA";
 		String key = "NA";
 		String clef_sign = "NA";
 		double avg_tempo = -1.0; // -1 means there is no avg_tempo
 		if (!isMusicXML) {
 		    avg_tempo = dev.searchNonPartwiseControl(1,0.2, "tempo").value();
 		}
 		// getting Partlist
 		MusicXMLWrapper.Part[] partlist = musicxml.getPartList();
 		int note_count = 0;
 		for (MusicXMLWrapper.Part part : partlist) {	
 			MusicXMLWrapper.Measure[] measurelist =	part.getMeasureList();
 			// getting Measurelist
 			for(MusicXMLWrapper.Measure measure : measurelist) {
 				MusicXMLWrapper.MusicData[] mdlist = measure.getMusicDataList();
 				// getting Musicdata
 				for (MusicXMLWrapper.MusicData md : mdlist) {
 					// if md is Direction : Direction includes Dynamics, Wedges 
 					if (md instanceof MusicXMLWrapper.Direction) {
 						MusicXMLWrapper.Direction direction = (MusicXMLWrapper.Direction)md;
 						MusicXMLWrapper.DirectionType[] directionTypes = direction.getDirectionTypeList();
 						
 						for (MusicXMLWrapper.DirectionType directionType : directionTypes) {
 							// getting ff, mf, pp, etc. sf is also handled
 							if (directionType.name().equals("dynamics")) {
 								if (directionType.dynamics().contains("sf")) {
 									exDyn_sf = directionType.dynamics();
 									if (exDyn_sf.equals("sfp")) exDyn = "p";
 								} else {
 									exDyn = directionType.dynamics();
 								}
 							}
 							
 							// getting wedges such as crescendo, diminuendo, rit., acc.
 							if (directionType.name().equals("wedge")) {
 								if (directionType.type().equals("crescendo")) {
 									exWedge = "crescendo";
 								}
 								if (directionType.type().equals("diminuendo")) {
 									exWedge = "diminuendo";
 								}
 								if (directionType.type().equals("stop")) {
 									exWedge = "NA";
 								}
 							}
 							if (directionType.name().equals("words")) {
 								if (directionType.text().contains("rit")) {
 									exTmp = "rit";
 								}
 								if (directionType.text().contains("rall")) {
 									exTmp = "rit";
 								}
 								if (directionType.text().contains("acc")) {
 									exTmp = "acc";
 								}
 								if (directionType.text().contains("a tempo")) {
 									exTmp = "NA";
 								}
 								if (directionType.text().contains("poco")) {
 									exTmp = "poco_" + exTmp;
 								}
 								if (directionType.text().contains("cresc")) {
 									exWedge = "crescendo";
 								}
 								if (directionType.text().contains("dim")) {
 									exWedge = "diminuendo";
 								}
 							}
 						} // end of DirectionType loop
 					} // end of diretion-if-statement
 										
 					// getting attributes, mainly time signature
 					if (md instanceof MusicXMLWrapper.Attributes) {
 						int _beat = ((MusicXMLWrapper.Attributes)md).beats();
 						int _beatType = ((MusicXMLWrapper.Attributes)md).beatType();
 						if (_beat != 0 && _beatType != 0) {
 							timeSig = Integer.toString(_beat)+"/"+Integer.toString(_beatType);
 						}
 
 						clef_sign = ((MusicXMLWrapper.Attributes)md).clef_sign();
 						int _fifth = ((MusicXMLWrapper.Attributes)md).fifths();
 						String _mode = ((MusicXMLWrapper.Attributes)md).mode();
 						
 						if (_fifth > 0) {
 						    // number of sharps
 						    switch (Math.abs(_fifth)) {
 						    case 1:
 							key = "g";
 							break;
 						    case 2:
 							key = "d";
 							break;
 						    case 3:
 							key = "a";
 							break;
 						    case 4:
 							key = "e";
 							break;
 						    case 5:
 							key = "b";
 							break;
 						    case 6:
 							key = "fis";
 							break;
 						    case 7:
 							key = "cis";
 							break;
 						    }
 						} else {
 						    // number of flats
 						    switch (Math.abs(_fifth)) {
 						    case 0:
 							key = "c";
 							break;
 						    case 1:
 							key = "f";
 							break;
 						    case 2:
 							key = "bes";
 							break;
 						    case 3:
 							key = "es";
 							break;
 						    case 4:
 							key = "aes";
 							break;
 						    case 5:
 							key = "des";
 							break;
 						    case 6:
 							key = "ges";
 							break;
 						    case 7:
 							key = "ces";
 							break;
 						    }
 						}
 					}
 					
 					// getting note info.
 					if (md instanceof MusicXMLWrapper.Note) {
 						MusicXMLWrapper.Note note = (MusicXMLWrapper.Note)md;
 						DeviationInstanceWrapper.NoteDeviation nd = null;
 						if (!isMusicXML) {
 						    nd = dev.getNoteDeviation(note);
 						}
 						String staff = Integer.toString(note.staff());
 						String voice = Integer.toString(note.voice());
 						String measureNumber = Integer.toString(measure.number());
 						String beatPos = Double.toString(note.beat());
 						String durationActual = Double.toString(note.tiedDuration());	// for a tied note 
 						String xpath = (note.getXPathExpression());
 						String noteNumber = "R";
 						String attack = "NA";
 						String release = "NA";
 						String dynamic = "NA";
 						String slur = "NA";
 						String tie = "NA";
 						String tuplet = "NA";
 						String articulation ="NA";
 						Double tempo_dev = 1.0;
 						String metric = "NA";
 						String arpeggiate = "NA";
 						String ornaments = "NA";
 						String noteName = "NA";
 						String [] splitLine = timeSig.split("/");
 						int beat = Integer.parseInt(splitLine[0]);
 						int beatType = Integer.parseInt(splitLine[1]);
 						
 						// estimate metric info
 						// WE NEED REFACTORING HERE!!!
 						if (beatType == 4 || beatType == 2) {
 							if (beat == 4) {
 								if (note.beat() >= 1.0 && note.beat() < 2.0) metric = "s";
 								if (note.beat() >= 2.0 && note.beat() < 3.0) metric = "w";
 								if (note.beat() >= 3.0 && note.beat() < 4.0) metric = "m";
 								if (note.beat() >= 4.0 && note.beat() < 5.0) metric = "w";
 							}
 							
 							if (beat == 3) {
 								if (note.beat() >= 1.0 && note.beat() < 2.0) metric = "s";
 								if (note.beat() >= 2.0 && note.beat() < 3.0) metric = "w";
 								if (note.beat() >= 3.0 && note.beat() < 4.0) metric = "w";
 							}
 							
 							if (beat == 2) {
 								if (note.beat() >= 1.0 && note.beat() < 2.0) metric = "w";
 								if (note.beat() >= 2.0 && note.beat() < 3.0) metric = "weak";
 							}
 						}
 						
 						if (beatType == 8) {
 							if (beat == 2) {
 								if (note.beat() >= 1.0 && note.beat() < 1.5) metric = "s";
 								if (note.beat() >= 1.5 && note.beat() < 2.0) metric = "w";
 								
 							}
 							if (beat == 6) {
 								if (note.beat() >= 1.0 && note.beat() < 1.5) metric = "s";
 								if (note.beat() >= 1.5 && note.beat() < 2.0) metric = "w";
 								if (note.beat() >= 2.0 && note.beat() < 2.5) metric = "w";
 								if (note.beat() >= 2.5 && note.beat() < 3.0) metric = "m";
 								if (note.beat() >= 3.0 && note.beat() < 3.5) metric = "w";
 								if (note.beat() >= 3.5 && note.beat() < 4.0) metric = "w";		
 							}
 							
 							if (beat == 9) {
 								
 								if (note.beat() >= 1.0 && note.beat() < 1.33) metric = "s";
 								if (note.beat() >= 1.33 && note.beat() < 1.66) metric = "w";
 								if (note.beat() >= 1.66 && note.beat() < 2.00) metric = "w";
 								if (note.beat() >= 2.00 && note.beat() < 2.33) metric = "m";
 								if (note.beat() >= 2.33 && note.beat() < 2.66) metric = "w";
 								if (note.beat() >= 2.66 && note.beat() < 3.00) metric = "w";
 								if (note.beat() >= 3.00 && note.beat() < 3.33) metric = "m";
 								if (note.beat() >= 3.33 && note.beat() < 3.66) metric = "w";
 								if (note.beat() >= 3.66 && note.beat() < 4.00) metric = "w";
 							}
 							
 							if (beat == 12) {
 								if (note.beat() >= 1.0 && note.beat() < 1.5) metric = "s";
 								if (note.beat() >= 1.5 && note.beat() < 2.0) metric = "w";
 								if (note.beat() >= 2.0 && note.beat() < 2.5) metric = "w";
 								if (note.beat() >= 2.5 && note.beat() < 3.0) metric = "m";
 								if (note.beat() >= 3.0 && note.beat() < 3.5) metric = "w";
 								if (note.beat() >= 3.5 && note.beat() < 4.0) metric = "w";
 								if (note.beat() >= 4.0 && note.beat() < 4.5) metric = "s";
 								if (note.beat() >= 4.5 && note.beat() < 5.0) metric = "w";
 								if (note.beat() >= 5.0 && note.beat() < 5.5) metric = "w";
 								if (note.beat() >= 5.5 && note.beat() < 6.0) metric = "m";
 								if (note.beat() >= 6.0 && note.beat() < 6.5) metric = "w";
 								if (note.beat() >= 6.5 && note.beat() < 7.0) metric = "w";
 							}
 						}
 					
 						String tempoDeviation = "NA";
 						String tempo = "NA";
 					       
 						if (dev != null && dev.searchNonPartwiseControl(measure.number(), 
 								(Math.floor(note.beat())*0.2), "tempo-deviation") != null) {
 							tempo_dev = dev.searchNonPartwiseControl(measure.number(), 
 									(Math.floor(note.beat())*0.2), 
 									"tempo-deviation").value();
 							tempoDeviation = Double.toString(tempo_dev);
 							tempo = Double.toString(avg_tempo*tempo_dev);
 						}
 						
 						
 						//get notation
 						if (((MusicXMLWrapper.Note)md).getFirstNotations() != null) {
 							if ((note.getFirstNotations().getSlurList()) != null) {
 								slur = (note.getFirstNotations().getSlurList()).get(0).type();
 							}
 							if ((note.getFirstNotations().hasArticulation("staccato")) == true) {
 								articulation = "staccato";
 							}
 							if ((note.getFirstNotations().fermata()) != null) {
 								articulation = "fermata";
 							}
 							if ((note.getFirstNotations().hasArticulation("accent")) == true) {
 								articulation = "accent";
 							}
 						}
 						if (!note.rest()) {
 							noteNumber = Integer.toString(note.notenum());
 							noteName = note.noteName();
 							
 							//if there exists note deviation info
 							if (nd != null && nd instanceof DeviationInstanceWrapper.NoteDeviation)  {
 							    //calculate relative attack, release respect to actualDuration
 								if (Double.parseDouble(durationActual) != 0) {
 									attack = Double.toString(nd.attack());	
 									release = Double.toString((nd.release()+
 												Double.parseDouble(durationActual))/
 											Double.parseDouble(durationActual));
 								}
 								dynamic = Double.toString(nd.dynamics());
 									
 							}
 						} 
 						else {
 							noteNumber = "R";
 							attack = "NA";
 							release = "NA";
 							dynamic = "NA";
 						}
 
 						MusicXMLWrapper.Notations nt = note.getFirstNotations();
						org.w3c.dom.NodeList childNodes = nt.getTheChildNodes();
 							
 						for (int index = 0; index < childNodes.getLength(); index++) {
 						    String nodeName = childNodes.item(index).getNodeName();
 						    
 						    if (nodeName.equals("arpeggiate")) {
 							arpeggiate = nodeName;
 						    } 
 						    if (nodeName.equals("ornaments")) {
 							String nodeName2 = childNodes.item(index).getFirstChild().getNodeName();
 									
 									
 							if (nodeName2.equals("trill-mark")) {
 							    ornaments = "trill_2_X";
 							} 
 							if (nodeName2.equals("turn")) {
 							    ornaments = "turn_2_-1";	// +2, 0, -2, 0
 							} 
 							if (nodeName2.equals("inverted-turn")) {
 							    ornaments = "turn_-1_2";
 							} 
 							if (nodeName2.equals("mordent")) {
 							    ornaments = "mordent_2_X";
 							} 
 							if (nodeName2.equals("inverted-mordent")) {
 							    ornaments = "mordent_-2_X";
 							}
 							
 						    }
 								
 						    if (nodeName.equals("slur")) {
 							slur = nt.getSlurList().get(0).type();
 						    }
 								
 						}
 							
 							
 					
 						
 						if (!note.rest()) {
 						    noteNumber = Integer.toString(note.notenum());
 						    if (note.grace()) { 
 							childNodes = note.getTheChildNodes();
 							for (int index = 0; index < childNodes.getLength(); index++) {
 							    String nodeName = childNodes.item(index).getNodeName();
 							    
 							    if (nodeName.equals("grace")) {
 								
 								org.w3c.dom.NamedNodeMap attrs = childNodes.item(index).getAttributes();
 								if (childNodes.item(index).hasAttributes() == false) {
 								    noteNumber = "grace_app_"+noteNumber;
 								} else {
 								    noteNumber = "grace_acc_"+noteNumber;
 								    
 								}
 							    } 
 							}
 							
 							
 							if (note.type().equals("32th")) durationActual = "0.125";
 							else if (note.type().equals("16th")) durationActual = "0.25";
 							else if (note.type().equals("eighth")) durationActual = "0.5";
 							else if (note.type().equals("quarter")) durationActual = "1.0";
 							else if (note.type().equals("half")) durationActual = "2.0";
 							else if (note.type().equals("whole")) durationActual = "4.0";
 							else if (note.type().equals("64th")) durationActual = "0.0625";
 							
 						    }
 						}
 						
 						String write_exDyn; // for sf-handling
 						// if duration == 0.0 then the note is a decorative note.
 						// if tie == "stop" then we skip the note, because tie is already processed
 						if (!durationActual.equals("0.0") || !tie.equals("stop")) {
 						    // sf-handling
 						    if (!exDyn_sf.equals("NA")) { 
 							write_exDyn = exDyn_sf; exDyn_sf = "NA";
 						    } else { 
 							write_exDyn = exDyn;
 						    }
 						    List<Object> aList = new ArrayList<Object>();
 						    aList.add(note_count);		// only for sorting later
 						    aList.add(staff);		// 0
 						    aList.add(voice);               // 1
 						    aList.add(key);                 // 2
 						    aList.add(clef_sign);           // 3
 						    aList.add(measureNumber); 	// 4
 						    aList.add(beatPos);		// 5
 						    aList.add(metric);		// 6
 						    aList.add(noteNumber);		// 7
 						    aList.add(noteName);            // 8
 						    aList.add(durationActual);	// 9
 						    aList.add(timeSig);		// 10
 						    aList.add(slur);		// 11
 						    aList.add(write_exDyn);		// 12
 						    aList.add(exWedge);		// 13
 						    aList.add(exTmp);		// 14
 						    aList.add(articulation);	// 15
 						    aList.add(arpeggiate);      // 16
 						    aList.add(ornaments);       // 17
 						    aList.add(attack);		// 18
 						    aList.add(release);		// 19
 						    aList.add(tempo);		// 20
 						    aList.add(tempoDeviation);	// 21
 						    aList.add(dynamic);		// 22
 						    String grouping = "NA";
 						    aList.add(grouping);	        // 23
 						    
 						    placeholder.put(xpath, aList);
 						    note_count++;
 						    
 						}
 					}
 				}
 			}
 		}
     }
 
 	private void groupingToPlaceholder() {
 	    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
 	    DocumentBuilder builder = null;
 
 		try {
 			builder = builderFactory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		Document document = null;
 		try {
 			document = builder.parse(new FileInputStream(apexFileName));
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		Element rootElement = document.getDocumentElement();
 		NodeList groups = rootElement.getElementsByTagName(("group"));
 
 		for (int i = 0; i < groups.getLength(); i++) {
 			parseNoteGroupsWithNode(groups.item(i));
 		}
 
 	}
 
 	private void parseNoteGroupsWithNode(Node g) {
 		Element e =(Element)g;
 		String depth = e.getAttribute("depth");
 		String grouping_id = getGroupingIdOfDepth(Integer.parseInt(depth));
 		NodeList notes = g.getChildNodes();
 		for (int i = 0; i < notes.getLength(); i++) {
 			// get xpath of the note
 			if (!(notes.item(i) instanceof Element)) {
 				continue;
 			}
 
 			Element n = (Element)notes.item(i);
 			if (n.getTagName().equals("note")) {
 				String xpath_orig = n.getAttribute("xlink:href");
 				String xpath = xpath_orig.substring(10, xpath_orig.length()-1);
 				// find note array with the xpath
 				List note_array = placeholder.get(xpath);
 				String grouping = (String)(note_array.get(22));	// 22 is the index of the grouping array
 				if (grouping.equals("NA")) {
 					grouping = "";
 					grouping +=grouping_id;
 				} else {
 					grouping = grouping + ":" + grouping_id;
 				}
 				note_array.set(22, grouping);
 				placeholder.remove(xpath);
 				placeholder.put(xpath, note_array);
 			} else if (n.getTagName().equals("apex")) {
 				
 				// Parsing apex info.
 				// Each group has its own apex point.
 				// The notes of the apex start will be marked with additional "<"
 				// The notes of the apex end will be marked with additional ">"
 				NodeList apex_nodes = n.getChildNodes();
 				for ( int j = 0; j < apex_nodes.getLength(); j++) {
 					if (!(apex_nodes.item(j) instanceof Element)) {
 						continue;
 					}
 					Element a = (Element)apex_nodes.item(j);
 					String xpath_orig = a.getAttribute("xlink:href");
 					String xpath = xpath_orig.substring(10, xpath_orig.length()-1);
 					List note_array = placeholder.get(xpath);
 					System.out.println(note_array);
 					String grouping = (String)(note_array.get(22));	// 22 is the index of the grouping array
 				      
 					String [] g_split = grouping.split(":");
 					String target_group = g_split[Integer.parseInt(depth)-1];	
 					if (a.getTagName().equals("start")) {
 						target_group += "<";
 					} else if (a.getTagName().equals("stop")) {
 						target_group += ">";
 					}
 					g_split[Integer.parseInt(depth)-1] = target_group;
 					String newGrouping = "";
 					for (int m = 0; m < g_split.length; m++) {
 						newGrouping += g_split[m];
 						if (m < g_split.length - 1) newGrouping += ":";
 					}
 					note_array.set(22, newGrouping);
 				}
 			
 				
 			}
 
 		}
 
 	}
 
 	private String getGroupingIdOfDepth(Integer d) {
 
 		Integer retVal = -1;
 		if (grouping_indicator_dict.containsKey(d)) {
 			Integer value = grouping_indicator_dict.get(d);
 			Integer newValue = value+1;
 			grouping_indicator_dict.put(d, newValue);
 			retVal = newValue;
 		} else {
 			// this key is new!
 			grouping_indicator_dict.put(d, 0);
 			retVal = 0;
 		}
 
 		return Integer.toString(retVal);
 	}
 
 	protected void run() throws IOException {
 			
 	    if (!isMusicXML) {
 		dev = (DeviationInstanceWrapper)indata();
 		
 		//getting target score XML
 		musicxml = dev.getTargetMusicXML();
 		
 		// getting score file name and setting output file name
 		scoreFileName = musicxml.getFileName();
 		outputFileName = dev.getFileName() + ".apex.csv";
 		
 		// get apex file name
 		String [] devFileNameSplit = dev.getFileName().split("\\.(?=[^\\.]+$)");
 		String devFileNameWithoutExt = devFileNameSplit[0];
 		String devFileNameWithoutSoundSource;
 		int string_length = devFileNameWithoutExt.length();
 		if (devFileNameWithoutExt.charAt(string_length-2) == '-') {
 			devFileNameWithoutSoundSource = devFileNameWithoutExt.substring(0, string_length-2);
 		} else {
 			devFileNameWithoutSoundSource = devFileNameWithoutExt;
 		}
 		apexFileName = this.targetDir+devFileNameWithoutSoundSource+".apex.xml";
 	    
 		System.out.println("[LOG] THIS IS DeviationXML FILE.");
 		System.out.println("[LOG] opening necessary files ...");
 		System.out.println("ScoreXML: "+scoreFileName+" \nDeviationXML: "+dev.getTargetMusicXMLFileName()+" \nApexXML: "+apexFileName);
 		System.out.println("OutputCSV: "+outputFileName);
 	    } else {
 		// this is MusicXML file. so we do not have apex file either!
 		dev = null;
 		musicxml = (MusicXMLWrapper)indata();
 		scoreFileName = musicxml.getFileName();
 		outputFileName = scoreFileName + ".csv";
 
 		System.out.println("[LOG] THIS IS MusicXML FILE.");
 		System.out.println("[LOG] opening necessary files ...");
 		System.out.println("ScoreXML: "+scoreFileName);
 		System.out.println("OutputCSV: "+outputFileName);
 	    }
 
 	    // Preparing placeholder
 	    placeholder = new Hashtable<String, List>();
 	    
 	    // Preparing grouping indicator dicionary
 	    grouping_indicator_dict = new Hashtable<Integer, Integer>();
 
 	    // Getting score and expressive features from MusicXML into the placeholder
 	    System.out.println("[LOG] parsing score and performance annotations...");
 	    this.scoreDevToPlaceholder();
 
 	    // Getting structural info form ApexXML into the placeholder
 	    if (!isMusicXML) {
 		File f = new File(apexFileName);
 		if (f.exists()) {
 		    System.out.println("[LOG] parsing grouping annotation...");
 		    this.groupingToPlaceholder();
 		} else {
 		    
 		    System.out.println("[LOG] No apex xml is found!");
 		    outputFileName = dev.getFileName()+".csv";
 		    System.out.println("[LOG] new output file name: "+outputFileName);
 		}
 	    }
 
 	    // Wrting data in the placeholder into a file
 	    System.out.println("[LOG] writing to the output file...");
 	    this.writeToFile();
 	    
 	}
 	
     public static void main(String[] args) {
 	System.out.println("Score, deviation and grouping annotation parser v 0.7");
 	System.out.println("Parsing .dev, .xml and .apex.xml and making .csv");
 	System.out.println("Implemented with CMX API");
 	System.out.println("Taehun KIM, Audio Communication Group, TU Berlin");
 	System.out.println("2012");
 	if (args.length == 0) {
 	    System.out.println("Usage: java ScoreDeviationGroupingToCSV file(s)");
 	    System.out.println("- The input files should have the extension .dev(DeviationXML) or .xml(MusicXML)");
 	    System.exit(1);
 	}
 
 	ScoreDeviationGroupingToCSV c= new ScoreDeviationGroupingToCSV();
 
 	// get the target directory
 	String targetDirArg = args[0];
 
 	// check the input file type
 	c.isMusicXML = targetDirArg.indexOf(".xml") != -1;	
 	String [] split = targetDirArg.split("/");
 	split[split.length-1] = "";
 	String result = "";
 	for (int m = 0; m < split.length; m++) {
 	    result += split[m];
 	    if (m < split.length - 1) result += "/";
 	}
 	c.targetDir = result;
 
 	try {
 	    c.start(args);
 	} catch (Exception e) {
 	    c.showErrorMessage(e);
 	    System.exit(1);		
 	}
 		
     }
 }
 
