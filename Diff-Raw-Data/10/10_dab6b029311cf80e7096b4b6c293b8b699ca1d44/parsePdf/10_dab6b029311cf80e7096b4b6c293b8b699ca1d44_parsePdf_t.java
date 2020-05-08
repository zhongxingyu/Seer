 package mainScribt;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.regex.Pattern;
 
 
 
 public class parsePdf {
 	
 	private String Nachname;
 	private String Datum;
 	private String Kennzahl;
 	private String Leistung;
 	private String inputString;
 
 		public parsePdf()
 		{
 			super();
 		}
 		
 		public synchronized void setString(String inputString) {
 			this.inputString = inputString;
 		}
 		
 		public void parse() throws FileNotFoundException, IOException
 		{
 			boolean foundNachname = false;
 			boolean NachnameDone = false;
 			boolean foundDatum = false; 
 			boolean DatumDone = false;
 			boolean foundGmbH = false;
 			
 			String[] excludeTitles = {"di","mag","mba","dr","ddr","mmag","ing"};
 			
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
 							
 						} else if (zeile.startsWith("Herrn") || zeile.startsWith("Frau") && foundNachname == false) { //only once
 							foundNachname = true;
 							
 						} else if (zeile.startsWith("An den") || zeile.startsWith("An die") && foundGmbH == false) {
 							if (zeile.startsWith("An die Gemeinde")) {		// Sonderfall von "An die"
 								this.Nachname = zeile.substring(zeile.indexOf("Gemeinde"));
 								NachnameDone = true;
 							} else foundGmbH = true;
 						} else if (zeile.startsWith("Gemeinde") || zeile.startsWith("Bundesgymnasium")) {
 								this.Nachname = zeile;
 								NachnameDone = true;
 						}
 					}
 					
 					if (zeile.startsWith("Leistung der")){
 						int end = zeile.indexOf("kWp");
 						if (end != -1) {
 							int begin = zeile.substring(0, end-3).lastIndexOf(" ");
 							this.Leistung = zeile.substring(begin+1, end-1);
 						}
 						//System.out.println("Leistung: " + zeile.substring(begin+1, end-1));
 					}
 					
 					if (zeile.startsWith("WST6")){
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
				
			}
 		}	
 
 
 		public String getNachname() {
 			return Nachname;
 		}
 
 		public String getDatum() {
 			return Datum;
 		}
 
 		public String getKennzahl() {
 			return Kennzahl;
 		}
 
 		public String getLeistung() {
 			return Leistung;
 		}
 		
 	
 }
