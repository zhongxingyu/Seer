 package parser;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class parseNOE extends parsePdf{
 
 	
 		public parseNOE()
 		{
 			super();
 		}
 		
 		
 		@Override
 		public void parse() throws FileNotFoundException, IOException
 		{
 			boolean foundNachname = false;
 			boolean NachnameDone = false;
 			boolean foundDatum = false; 
 			boolean DatumDone = false;
 			boolean foundGmbH = false;
 						
 			try {
 				BufferedReader in = new BufferedReader(new StringReader(inputString));
 				String zeile = null;
 				String zeile_old = "";
 				String buffer = "";
 				while ((zeile = in.readLine()) != null) {
 					//System.out.println("NachnameDone = " + NachnameDone + " foundNachname: " + foundNachname + " foundGmbH: " + foundGmbH);
 					if (NachnameDone == false) {
 						if (foundNachname == true) {
 							int i= 1;
 							String[] zeileArray = zeile.split(" ",5);
 							for (int j=0; j< excludeTitles.length ;j++) {
 								if (zeileArray[0].toLowerCase().equals(excludeTitles[j]) || zeileArray[0].toLowerCase().equals(excludeTitles[j]+".")) {
 									i++;
 								}
 							}
 							if (zeileArray.length >= i+1) {
 								if (zeileArray[i].equals("und")) i+=2;
 							} 
 							if (zeileArray.length < i+1) {
 								System.out.println("_NO_ Nachname found\n");
 							} else {
 								this.Nachname = zeileArray[i];
 							}
 							//foundNachname = false;
 							NachnameDone = true;
 							
 						} else if (foundGmbH == true){
 							if (zeile.startsWith("Gemeinde")) {
 								this.Nachname = zeile;
 								NachnameDone = true;
 							} else if(!Pattern.matches("^[0-9]{4}+.*" ,zeile)) {	//Findet PLZ
 								//System.out.println(zeile + "found PLZ");
 								buffer += zeile_old;
 							} else {
 								this.Nachname = buffer;
 								NachnameDone = true;
 							}							
 							zeile_old = zeile;
 							
						} else if (zeile.startsWith("Herr") || zeile.startsWith("Frau") && foundNachname == false) { //only once
 							foundNachname = true;
 							
 						} else if (zeile.startsWith("An den") || zeile.startsWith("An die") && foundGmbH == false) {
 							if (zeile.startsWith("An die Gemeinde")) {		// Sonderfall von "An die"
 								this.Nachname = zeile.substring(zeile.indexOf("Gemeinde"));
 								NachnameDone = true;
 							} else foundGmbH = true;
 						} else if (zeile.startsWith("Gemeinde") || zeile.startsWith("Bundesgymnasium") || zeile.startsWith("Marktgemeinde")) {
 								this.Nachname = zeile;
 								NachnameDone = true;
 						}
 					}
 					
					if (zeile.startsWith("Leistung der") || zeile.startsWith("Peakleistung")){
 						int end = zeile.indexOf("kWp");
 						if (end >= 1) {
 							int begin = zeile.substring(0, end-3).lastIndexOf(" ");
 							this.Leistung = zeile.substring(begin+1, end-1);
 						}
 						//System.out.println("Leistung: " + zeile.substring(begin+1, end-1));
 					}
 					
					if (zeile.startsWith("WST6") || zeile.startsWith("RU4-EL")){
 						this.Kennzahl = zeile.substring(0, zeile.indexOf(" "));
 						//System.out.println("Kennzahl: " + zeile);
 					}
 					
 					if (foundDatum == true) {
 						this.Datum = zeile.substring(zeile.lastIndexOf('.')-2,zeile.length()-1);
 						//System.out.println("Datum: " + zeile.substring(zeile.lastIndexOf('.')-2,zeile.length()-1));
 						foundDatum = false;
 						DatumDone = true;
 					}
 					
 					if (zeile.endsWith("Datum ") && DatumDone == false){	//only once
 						foundDatum = true;
 					}
 					
 					//System.out.println("Gelesene Zeile: " + zeile);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			if (this.Leistung == null) {
 				//System.out.println("\t\tchecking for nderungungsbescheid");
 				Pattern MY_PATTERN = Pattern.compile("mit[\n ]?+[0-9]+[,]?[0-9]*[\n ]?+kWp[\n ]?+neu[\n ]?+festgesetzt");
 				Matcher m = MY_PATTERN.matcher(inputString);
 				String s = "";
 				while (m.find()) {
 				    s = m.group(0);
 				   // System.out.println(s);
 				    break;
 				}
 				int end = s.indexOf("kWp");
 				if (end >= 1) {
 					this.LeistungNeu = s.substring(4, end-1);
 					this.Leistung = "";
 					
 					MY_PATTERN = Pattern.compile("[0-9]+[,]?[0-9]* kWp, Einspeisezhlpunkt");
 					m = MY_PATTERN.matcher(inputString);
 					s = "";
 					while (m.find()) {
 					    s = m.group(0);
 					    //System.out.println(s);
 					    break;
 					}
 					end = s.indexOf("kWp");
 					if (end >= 1) {
 						this.LeistungAlt = s.substring(0, end-1);
 					}
 				}
 								
 				// Leistung alt: "Die Nennleistung der Wechselrichter betrgt: 1 x 4,6 kW und 5 x 3,4 kW"
 				//dem Anlagenteil mit einer Leistung von 6,45 kWp, Einspeisezhlpunkt
 			}
 		}	
 	
 	
 }
