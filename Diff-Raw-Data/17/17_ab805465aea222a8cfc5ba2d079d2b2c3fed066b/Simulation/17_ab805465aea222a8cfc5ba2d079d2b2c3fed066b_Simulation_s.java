 package com.stalkindustries.main.game;
 import java.awt.Color;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.BorderFactory;
 
 import com.stalkindustries.main.TheStreet;
 
 public class Simulation {
 	
 	private int[][] beziehungsmatrix;
 	private ArrayList<Person> people = new ArrayList<Person>(); 
 	private Agent agent = new Agent(0,"");
 	private int spielTag=1;
 	private int spielStunde=7;
 	private int spielMinute=0;
 	private ArrayList<Haus> houses = new ArrayList<Haus>();
 	private boolean wieschteAktion=true;  //wieeeeescht = boese
 	private double misstrauenMax=-100;
 
 	
 	public Simulation(){
 		
 	}
 	
 	
 	//Beschwerden an Miri
 	void initialisiereBeziehungsmatrix(){
 		int tmp;
 		this.beziehungsmatrix = new int[this.people.size()][this.people.size()];
 		for(int i=0;i<this.people.size();i++)
 			for(int j=0;j<this.people.size();j++)
 				this.beziehungsmatrix[i][j] = 0;
 		
 		//asymmetrisch
 		for(int i=0;i<this.people.size();i++){
 			for(int j=0;j<this.people.size();j++){
 				if(i!=j){
 					tmp = (int)(Math.random()*(10))+1;
 					this.beziehungsmatrix[i][j] = tmp;
 					if(this.people.get(i).getHausId() == this.people.get(j).getHausId()){ //Person in einem Haushalt sind besser miteinander befreundet
 						tmp = (10-tmp)/2;
 						this.beziehungsmatrix[i][j] += tmp;
 						this.beziehungsmatrix[j][i] += tmp;
 					}
 				}
 			}
 		}
 		//TODO entfernen?!
 //		//symmetrische Matrix
 //		for(int i=0;i<this.people.size();i++){
 //			for(int j=i+1;j<this.people.size();j++){
 //				tmp = (int)(Math.random()*(10))+1;
 //				this.beziehungsmatrix[i][j] = tmp;
 //				this.beziehungsmatrix[j][i] = tmp;
 //				if(this.people.get(i).get_haus_id() == this.people.get(j).get_haus_id()){ //Person in einem Haushalt sind besser miteinander befreundet
 //					tmp = (10-tmp)/2;
 //					this.beziehungsmatrix[i][j] += tmp;
 //					this.beziehungsmatrix[j][i] += tmp;
 //				}
 //			}
 //		}
 	}
 	
 	
 	
 	//Beschwerden an Sven und Miri
 	void calcMisstrauen(){
 		if(this.spielStunde > 5 || this.spielStunde < 2){
 			//misstrauen[] ist eine Hilfvariable, die spter die spter die neuen Werte enthlt und sie wird bentigt, dass nicht die echten Werte verndert werden, bevor alle berechnet wurden
 			double[] misstrauen = new double[people.size()];
 			
 			double faktor = 0.01275; //Faktor, der regelt, wie stark sich Personen bei einem Methodenaufruf beeinflussen --> abhngig von Hufigkeit des Methodenaufrufs
 			
 			//misstrauen[] mit den alten Misstrauenswerten initialisieren
 			for(int i=0;i<this.people.size();i++)
 				misstrauen[i] = this.people.get(i).getMisstrauen();
 			
 			//jede Person kann theoretisch wieder von jeder anderen beeinflusst werden
 			for(int i=0;i<this.people.size();i++){
 				//wenn sich Person dort befindet, wo sie auch beeinflusst werden kann
 				if((int)(this.people.get(i).getLocationId())-48+1 != 0 && (int)(this.people.get(i).getLocationId())-48+1 != 'X' && (int)(this.people.get(i).getLocationId())-48+1 != 'E'){
 					for(int j=0;j<this.people.size();j++){
 						//eine Person kann sich nicht selbst beeinflussen
 						if(i!=j){
 							//wenn sich die beiden Personen am selben Ort befinden
 							if((int)(this.people.get(i).getLocationId())-48+1 == (int)(this.people.get(j).getLocationId())-48+1){
 								//Ausnahme in der Berechnung: Kinder beeinflussen Erwachsene weniger
 								if(this.people.get(i) instanceof Erwachsene && this.people.get(j) instanceof Kinder){	//Kind beeinflusst Erwachsenen weniger
 									misstrauen[i] = misstrauen[i] - faktor/2*this.beziehungsmatrix[i][j]*(this.people.get(i).getMisstrauen()-this.people.get(j).getMisstrauen());
 								}
 								else{
 									misstrauen[i] = misstrauen[i] - faktor*this.beziehungsmatrix[i][j]*(this.people.get(i).getMisstrauen()-this.people.get(j).getMisstrauen());
 								}
 								
 								//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 								if(misstrauen[i]>100)
 									misstrauen[i] = 100;
 								if(misstrauen[i]<-100)
 									misstrauen[i] = -100;
 							}
 						}
 					}
 				}
 			}
 			
 			//Mapping der neuen Misstrauenswerte auf Person
 			for(int i=0;i<this.people.size();i++){
 				this.people.get(i).setMisstrauen(misstrauen[i]);
 			}
 		}
 	}
 	
 	//Support: Tobi
 		public void updatePosition(){
 			for(int i=0;i<people.size();i++){
 				people.get(i).step();
 			}
 		}
 	
 	//Beschwerden an Miri
 	//Mittelwert des Misstrauens der einzelnen Personen
 	public double calcMisstrauenInStreet(){
 		float misstrauen = 0;
 		for(int i=0;i<this.people.size();i++){
 			misstrauen += this.people.get(i).getMisstrauen();
 		}
 		misstrauen = misstrauen/this.people.size();
 		
 		return misstrauen;
 	}
 	
 	
 	//Beschwerden Miri
 	public void calcMisstrauenNachBeschwichtigenInPark(){
 		for(int i=0;i<this.people.size();i++){
 			//Checken, ob sich noch jemand im Park befindet
 			if(this.people.get(i).getLocationId() == 'P'){						
 				this.people.get(i).setMisstrauen(this.people.get(i).getMisstrauen()-1); 
 			}
 			
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).getMisstrauen()>100)
 				this.people.get(i).setMisstrauen(100);
 			if(this.people.get(i).getMisstrauen()<-100)
 				this.people.get(i).setMisstrauen(-100);
 		}
 	}
 	
 	//Beschwerden an Miri
 	//Beschwerden an Miri
     public void calcMisstrauenNachBeschwichtigen(int actionId, Person person){
         int zufall=0;
         int risiko;
         double misstrauen = person.getMisstrauen();
         
         //Fehlschlagen der Aktionen ist unter anderem abhngig vom Misstrauensstatus
                 if(misstrauen >= 0){
                     zufall = (int)(Math.random()*(misstrauen/10));
                     if(zufall == 0)
                         zufall = 1;
                 }
                 //Personen sind weniger misstrauisch
                 else{
                     if(misstrauen/10 < 0){
                         zufall = -(int)(Math.random()*(-(misstrauen/10)));
                         if(zufall == 0)
                             zufall = -1;
                     }
 //                    else
 //                        zufall = (int)(Math.random()*(misstrauen/10));
                 }
         
         //Risiko einer Beschwichtigenaktion steigt, je hufiger man sie ausfhrt
         risiko = person.getDurchgefuehrteBeschwichtigungen(actionId);
         if(actionId == 0 || actionId == 2 || actionId == 3){    //Kuchen, Flirten, Helfen
             if(risiko == 0){
                 if(zufall > 5)
                     zufall *= (-1);
                 else if(zufall >= 0)
                     zufall *= (-10);
                 else
                     zufall *= 10;
             }
             else{
                 if(zufall > 5)
                     zufall *= 6;
                 else if(zufall >= 0)
                     zufall *= 6;
                 else
                     zufall *= (-3);
             }
         }
         else if(actionId == 1){    //bei Haus vorbeigehen, um zu reden
             if(risiko <= 3)
                 if(zufall >= 0)
                     zufall *= (-3);
                 else
                     zufall *= 4;
             else
                 if(zufall >= 0)
                     zufall *= 3;
                 else
                     zufall *= (-1); 
         }
         else{                        //Im Park unterhalten
         	if(zufall >= 0)
         		zufall *= (-2);
             else
                 zufall *= 3;
         }
         
         misstrauen += zufall;
         
         
         //sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
         if(misstrauen>100)
             misstrauen = 100;
         if(misstrauen<-100)
             misstrauen = -100;
         
         //Misstrauen der Person setzen
         person.setMisstrauen(misstrauen);
     }
 	
 	
 	//TODO
 	//Beschwerden an Miri
 	public void calcMisstrauenNachUeberwachung(int hausId){
 		//Risiko berechnen
 		//Risiko ist abhngig von der Uhrzeit, d.h.tagsber ist das Risiko hher als nachts
 		int risiko=0;
 		if(this.spielStunde>1 && this.spielStunde<6){	//Nachtmodus
 			risiko = (int)(Math.random()*3); 
 		}
 		else{	//Tagmodus
 			risiko = (int)(Math.random()*15); 
 		}
 		
 		int mittelpunktX = this.houses.get(hausId-1).getPosX()+3*Ressources.RASTERHEIGHT/2;
 		int mittelpunktY = this.houses.get(hausId-1).getPosY()+3*Ressources.RASTERHEIGHT/2;
 		int epsilon = 200;	
 		
 		for(int i=0;i<this.people.size();i++){
 			//Checken, ob sich noch jemand in dem Haus befindet
 			//fr alle Personen, die noch im Haus sind, das Misstrauen neu berechnen
 			if((int)(this.people.get(i).getLocationId())-48 == hausId){
 				if(risiko>2)	//wenn das risiko kleiner ist, hat man Glck und man wird nicht erwicht
 					this.people.get(i).setMisstrauen(this.people.get(i).getMisstrauen()+6); //TODO: den Wert 50 testen ... eventuell erhhen
 			}
 			//Checken, ob sich jemand in einer epsilon-Umgebung um das Haus befindet, in das eingebrochen werden soll
 			//--> 1. Epsilon-Umgebung aufspannen (ist eine relative eckige :-D)
 			//-->Mittelpunkt vom Haus bestimmen
 			else{
 				// wenn sich eine Person in der Epsilon-Umgebung befindet
 				if(this.people.get(i).getPosX() >= mittelpunktX-epsilon && this.people.get(i).getPosX() <= mittelpunktX+epsilon && this.people.get(i).getPosY() >= mittelpunktY-epsilon && this.people.get(i).getPosY() <= mittelpunktY+epsilon && this.people.get(i).getLocationId()!='E'){
 					//wenn das per Zufall eine Person ist, die in dem Haus wohnt und z.B. auf dem Heimweg ist
 					//hier ist das Misstrauen natrlich grer
 					if(this.people.get(i).getHausId()+1 == hausId){
 						if(risiko>2)
 							this.people.get(i).setMisstrauen(this.people.get(i).getMisstrauen()+2);
 					}
 					else{
 						if(risiko>2)
 							this.people.get(i).setMisstrauen(this.people.get(i).getMisstrauen()+1);
 					}
 				}
 			}
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).getMisstrauen()>100)
 				this.people.get(i).setMisstrauen(100);
 			if(this.people.get(i).getMisstrauen()<-100)
 				this.people.get(i).setMisstrauen(-100);
 		}
 		
 	}
 	
 	
 	//Beschwerden Miri
 	public void agentBetrittFremdesHaus(){
 		//wenn sich der Agent in irgendeinem Haus befindet
 		if((int)(getAgent().getLocationId())-48 >= 1 && (int)(getAgent().getLocationId())-48 <= 9){
 			//wenn sich der Agent in dem Haus befindet, das ausspioniert werden soll
 			if(getAgent().getLocationId() != (char)getAgent().getHausId()+48+1){
 				if (wieschteAktion){
 					calcMisstrauenNachUeberwachung((int)(getAgent().getLocationId()-48));
 				}
 					
 			}
 		}
 	}	
 	
 	
 	//Beschwerden Miri
 	//whrend ein Haus berwacht wird, macht sich bei den Bewohnern ein Unwohlsein breit und sie werden mehr misstrauisch
 	public void calcMisstrauenDuringUeberwachung(){
 		for(int i=0;i<this.people.size();i++){
 			// wenn berwachungsmodule in dem Haus, in dem die Person lebt, installiert wurden
 			if(this.houses.get(this.people.get(i).getHausId()).getUeberwachungsmodule().size() > 0){
 				this.people.get(i).setMisstrauen(this.people.get(i).getMisstrauen()+2);
 			}
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).getMisstrauen()>100)
 				this.people.get(i).setMisstrauen(100);
 			if(this.people.get(i).getMisstrauen()<-100)
 				this.people.get(i).setMisstrauen(-100);
 		}
 	}
 	
 	
 	//Beschwerden Miri
 	//Allen Husern den berwachungsstatus updaten
 	public void updateUeberwachungsstatus(){
 		float ueberwachungswert=0;
 		
 		
 		for (int i=0; i<getHouses().size(); i++){
 			getHouses().get(i).setUeberwachungsstatus(0);
 			for (int j=0; j<getHouses().get(i).getUeberwachungsmodule().size(); j++){
 				if (getHouses().get(i).getUeberwachungsmodule().get(j).equals("Wanze")){
 					ueberwachungswert = ueberwachungswert + getHouses().get(i).getUeberwachungsWert(0);
 				}
 				if (getHouses().get(i).getUeberwachungsmodule().get(j).equals("Kamera")){
 					ueberwachungswert = ueberwachungswert + getHouses().get(i).getUeberwachungsWert(1);
 				}
 				if (getHouses().get(i).getUeberwachungsmodule().get(j).equals("Hacken")){
 					ueberwachungswert = ueberwachungswert + getHouses().get(i).getUeberwachungsWert(2);
 				}
 				if (getHouses().get(i).getUeberwachungsmodule().get(j).equals("Fernglas")){
 					ueberwachungswert = ueberwachungswert + getHouses().get(i).getUeberwachungsWert(3);
 				}
 				if (ueberwachungswert > 100){
 					ueberwachungswert = 100;
 				}
 				getHouses().get(i).setUeberwachungsstatus(ueberwachungswert);
 				if (ueberwachungswert >=0){
 					calcColouredPeople(i);
 				}
 			} 
 			ueberwachungswert=0;
 		}
 	}
 	
 	//Beschwerden Miri
 	//Mittelwert der berwachungsstati der einzelnen Huser
 	public float calcUeberwachungsstatusInStreet(){
 		float ueberwachung = 0;
 		for(int i=0;i<this.houses.size();i++){
 			ueberwachung += this.houses.get(i).getUeberwachungsstatus();
 		}
 		ueberwachung = ueberwachung/(this.houses.size()-1);
 		
 		return ueberwachung;
 	}
 	
 
 	
 	
 	// Support Tiki
 	void calcSpielzeit(){
 		this.spielMinute++;
 		if (this.spielMinute==60){
 			this.spielMinute = 0;
 			this.spielStunde++;
 		}
 		if (this.spielStunde==24){
 			this.spielStunde=0;
 			this.spielTag++;
 		}
 	}
 	
 	
 	//Support Tiki
 	public void tagesablauf(){
 		char locationId ='0';
 		int hausId = 0;
 		for	(int i=0; i<this.people.size(); i++){
 			locationId = this.people.get(i).getLocationId();
 			hausId = this.people.get(i).getHausId()+1;
 			
 			// Es bekommen nur die Menschen einen neuen Weg zugewiesen, wenn sie sich gerade nicht bewegen
 			if (this.people.get(i).getCurrentMove() == 'n'){
 				
 				//Zuerst wird der Tagesablauf der Kinder berprft, da dieser von den Erwachsenen unterschiedlich ist
 				if (this.people.get(i) instanceof Kinder){
					if ((this.people.get(i).getZeitverzogerung() + this.spielMinute) == 60){
 						
						if (this.spielStunde==7){ //zur Schule gehen
 							calcWeg(this.people.get(i), 'E');
 						}	
 						
 						//Nur wenn der Mensch nicht zuHause ist, kann er nach Hause gehen
 						if ((this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){
 							
 							if (this.spielStunde==14){ //nach Hause gehen
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));				
 							}
 							if (this.spielStunde==20){ //nach Hause gehen
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 							}	
 						} 
 					} else {
 
 						//nach Hause gehen, notwendig, falls die Kinder noch eine runde im Park drehen & 20 Uhr berschritten wird
 						if (this.spielStunde>=20 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ 
 							calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 						}
 						
 						// randomisiert in den Park gehen
 						if (this.spielStunde >= 15 && this.spielStunde <=19 && locationId !='P'){ 
 							if ((int)(Math.random()*200) == 3){
 								calcWeg(this.people.get(i), 'P');
 							}
 						}
 						
 						// randomisiert den Park verlassen oder noch eine Runde drehen
 						if (locationId =='P' && this.spielStunde<=19){ 
 							if ((int)(Math.random()*5) == 3){
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0)); 
 							} else{
 								if (this.people.get(i).getCurrentMove() == 'n'){
 									calcWeg(this.people.get(i), 'P');
 								}
 							}
 						}
 					}
 					
 				}else{
 					//Nun wir der Tagesablauf der Erwachsenen untersucht
 					if (this.people.get(i) instanceof Erwachsene){
 						
 						//Zuerst werden die Erwachsenen untersucht, die Arbeit haben
 						if (((Erwachsene)people.get(i)).isHatArbeit()){
							if (this.spielStunde==8 && (this.people.get(i).getZeitverzogerung() + this.spielMinute) == 60){ // Zur Arbeit gehen
 								calcWeg(this.people.get(i), 'E');
 							}
 							if (this.spielStunde==16 && (this.people.get(i).getZeitverzogerung() + this.spielMinute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 							}				
 							if ((this.spielStunde >= 17 || this.spielStunde <=0)){  // in den Park gehen
 								if ((int)(Math.random()*300) == 3){
 									calcWeg(this.people.get(i), 'P');
 								}
 							}
 						} else {
 							if (this.spielStunde == 9 && (this.people.get(i).getZeitverzogerung() + this.spielMinute) == 60){ //zum Einkaufen gehen
 								if ((int)(Math.random()*3)+1 == 1){
 									calcWeg(this.people.get(i), 'E');
 								}
 							}	
 							if (this.spielStunde >= 9 && this.spielStunde <=14 && hausId!='E'){ //zum Einkaufen gehen
 								if ((int)(Math.random()*300) == 3){
 									calcWeg(this.people.get(i), 'P');
 								}
 							}
 							if (this.spielStunde == 12 && (this.people.get(i).getZeitverzogerung() + this.spielMinute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 							}
 							if ((this.spielStunde >=14 || this.spielStunde <=1)){  // in den Park gehen
 								if ((int)(Math.random()*300) == 3){
 									calcWeg(this.people.get(i), 'P');
 								}
 							}
 						}
 						if (this.spielStunde==1 && (this.people.get(i).getZeitverzogerung() + this.spielMinute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 							calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 						}
 						if (this.spielStunde>=2 && this.spielStunde<4 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 							calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0));
 						}
 						if (locationId =='P' && (this.spielStunde < 2 || this.spielStunde >= 9)){ //nach Hause gehen
 							if ((int)(Math.random()*5) == 3){
 								calcWeg(this.people.get(i), String.valueOf(hausId).charAt(0)); 
 							} else{
 								if (this.people.get(i).getCurrentMove() == 'n'){
 									calcWeg(this.people.get(i), 'P');
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		if (getAgent().getLocationId() != (char)(getAgent().getHausId()+48+1) && (this.spielStunde>=21 || 
 			this.spielStunde<6) && !wieschteAktion && getAgent().getCurrentMove()=='n' &&
 			getAgent().getLocationId() != 'P'){
 			calcWeg(getAgent(), (char)(getAgent().getHausId()+48+1));
 		}
 		if (getAgent().getLocationId() != (char)(getAgent().getHausId()+48+1) && this.spielStunde>=2 && 
 				this.spielStunde<6 && !wieschteAktion && getAgent().getCurrentMove()=='n'){
 				calcWeg(getAgent(), (char)(getAgent().getHausId()+48+1));
 			}
 	} 	
 	
 	
 	//Support Tiki
 	private Point calcParkeingang(ArrayList<ArrayList<String>> location_ids){
 		int parkCounter=0;
 		
 		for (int i = 1; i<15; i++){
 			for (int j = 1; j<24; j++){
 				if (location_ids.get(i).get(j).equals("P")){
 					if (valuesInRange(j, i+1)){
 						if (location_ids.get(i+1).get(j).equals("P") || location_ids.get(i+1).get(j).equals("X")){
 							parkCounter++;
 						}
 					}
 					if (valuesInRange(j, i-1)){
 						if (location_ids.get(i-1).get(j).equals("P") || location_ids.get(i-1).get(j).equals("X")){
 							parkCounter++;
 						}
 					}
 					if (valuesInRange(j+1, i)){
 						if (location_ids.get(i).get(j+1).equals("P") || location_ids.get(i).get(j+1).equals("X")){
 							parkCounter++;
 						}
 					}
 					if (valuesInRange(j-1, i)){
 						if (location_ids.get(i).get(j-1).equals("P") || location_ids.get(i).get(j-1).equals("X")){
 							parkCounter++;
 						}
 					}
 					
 				}
 				if (parkCounter == 3){
 					return new Point (i,j);
 				} else{
 					parkCounter=0;
 				}
 			}
 		}		
 		return null;
 	}
 	
 	
 	
 	//Support Tiki
 	public void calcWeg(Mensch mensch, char zielLoc){
 		
 		char locationId;				
 		ArrayList<ArrayList<String>> locationIds;
 		Stack<Character> neuerWeg = new Stack<Character>();
 //		Point parkeingang = new Point();
 		
 		locationId = mensch.getLocationId();			
 		locationIds = Ressources.getLocation_ids();
 		
 //		if (zielLoc == 'P'){
 //			parkeingang = calcParkeingang(locationIds);
 //		}
 		
 		//Aktuelle Position des Mnnchens wird auf 0 gesetzt
 		if (zielLoc != locationId || zielLoc!='P'){
 			locationIds = rasterkarteInitialisieren(locationIds, String.valueOf(zielLoc), locationId);
 		} else {
 			locationIds = rasterkarteInitialisieren(locationIds, "P", locationId);
 		}
 
 		//if ((mensch.getPosY()-Ressources.ZEROPOS.height)%45!=0 && ((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)%45!=0){
 			switch(mensch.getCurrentMove()){
 	        case 'r': locationIds.get((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set(((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+1,"0");
 	                  break;
 	        case 'u': locationIds.get(((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+1).set((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 	                  break;
 	        case 'o':
 	        case 'l':
 	        default: locationIds.get((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 	        }
 		
 		//Bewegungsstack nach und nach fllen
 		neuerWeg = calcWegInRasterkarte(locationIds, mensch, zielLoc);
 			
 		//Stack zur Bewegung freigeben
 		mensch.setMoves(neuerWeg);
 	}
 	
 	
 	//Support Tiki
 	private Stack<Character> calcWegInRasterkarte(ArrayList<ArrayList<String>> location_ids, Mensch mensch, char zielloc){
 		
 		Stack<Character> neuer_weg = new Stack<Character>();
 		int counter = 1;
 		int xPos_current = 0;
 		int yPos_current = 0;
 		boolean firstStep = true;
 		String ziellocation="Z";
 		
 		goal:	for (int i=0; i<100; i++){
 					for (int j=0; j<Ressources.MAPHEIGHT/Ressources.RASTERHEIGHT; j++){  	// J entspricht y-wert, K entspricht x-wert
 						for (int k=0; k<Ressources.MAPWIDTH/Ressources.RASTERHEIGHT; k++){
 							// Es werden Zahlen auf der Map gesucht
 							if (location_ids.get(j).get(k).equals(String.valueOf(i))){
 								// Es wird berprft, ob das Ziel in direkter Nhe liegt
 								if (valuesInRange(k, j+1)){ //15 -> Rasterhhe
 									if (location_ids.get(j+1).get(k).equals(ziellocation)) {
 										location_ids.get(j+1).set(k,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j+1;
 										xPos_current = k;
 										break goal;
 									}
 								}
 								if (valuesInRange(k+1, j)){ //24 -> Rasterbreite
 									if (location_ids.get(j).get(k+1).equals(ziellocation)) {
 										location_ids.get(j).set(k+1,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j;
 										xPos_current = k+1;
 										break goal;
 									}
 								}
 								if (valuesInRange(k, j-1)){
 									if (location_ids.get(j-1).get(k).equals(ziellocation)) {
 										location_ids.get(j-1).set(k,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j-1;
 										xPos_current = k;
 										break goal;
 									}
 								}
 								if (valuesInRange(k-1, j)){
 									if (location_ids.get(j).get(k-1).equals(ziellocation)) {
 										location_ids.get(j).set(k-1,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j;
 										xPos_current = k-1;
 										break goal;
 									}
 								}
 							
 								if ((i>0 && (zielloc == mensch.getLocationId())) ||
 										(zielloc != mensch.getLocationId())){
 									// Es wird berprft, ob ein Feld drber/drunter/links oder rechts ebenfalls begehbar ist -> das wird markiert
 									if (valuesInRange(k, j+1)){
 										if (location_ids.get(j+1).get(k).equals("X")) {			//Weg nach unten ist begehbar
 										location_ids.get(j+1).set(k,String.valueOf(i+1));
 										}
 									}
 									if (valuesInRange(k+1, j)){
 										if (location_ids.get(j).get(k+1).equals("X")) {			//Weg nach rechts ist begehbar
 										location_ids.get(j).set(k+1,String.valueOf(i+1));
 										}
 									}
 									if (valuesInRange(k, j-1)){
 										if (location_ids.get(j-1).get(k).equals("X")) {			// Weg nach oben ist begehbar
 										location_ids.get(j-1).set(k,String.valueOf(i+1));
 										}
 									}
 									if (valuesInRange(k-1, j)){
 										if (location_ids.get(j).get(k-1).equals("X")) {			//Weg nach links ist begehbar
 										location_ids.get(j).set(k-1,String.valueOf(i+1));
 										}
 									}
 								} else { //TODO anpassen!!!
 									if (valuesInRange(k, j)){
 										if (location_ids.get(j-1).get(k).equals("X") && firstStep) {			// Weg nach oben ist begehbar
 											location_ids.get(j-1).set(k,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
									if (valuesInRange(k, j)){
 										if (location_ids.get(j).get(k-1).equals("X") && firstStep) {			//Weg nach links ist begehbar
 											location_ids.get(j).set(k-1,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 									if (valuesInRange(k+1, j)){
 										if (location_ids.get(j).get(k+1).equals("X") && firstStep) {			//Weg nach rechts ist begehbar
 											location_ids.get(j).set(k+1,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 									if (valuesInRange(k, j+1)){
 										if (location_ids.get(j+1).get(k).equals("X") && firstStep) {			//Weg nach unten ist begehbar
 											location_ids.get(j+1).set(k,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 								}
 								
 							}
 						}
 					}
 					//Wenn eine Person in den Park gehen mchte  und schon im Park ist, braucht man eine andere Ziellocation
 					if (i == 2 && zielloc=='P' && mensch.getLocationId()=='P'){
 						ziellocation="0";
 					}
 				}
 		
 		// Der Stack fr die Bewegung wird mit den richtigen Werten gefllt. Dafr hangelt man sich absteigend an der zahlenreihe entlang
 			neuer_weg = fuelleStackWeg(zielloc, mensch, location_ids, counter, xPos_current, yPos_current);
 		
 		
 		return neuer_weg;
 	}
 	
 	//Support Tiki
 	private Stack<Character> fuelleStackWeg(Character zielLoc, Mensch mensch, ArrayList<ArrayList<String>> locationIds, int counter, int xPosCurrent, int yPosCurrent) {
 		Stack<Character> neuerWeg = new Stack<Character>();
 		
 		//Falls das Ziel ein Haus ist, soll die Person auf ihren startpunkt laufen.
 		if (mensch.getHausId() == (int)(zielLoc-48-1)){
 			if ((int)(zielLoc)-48 <=9 && (int)(zielLoc)-48 > 0){ //-48 fr char umwandlung zu int
 				neuerWeg = fuelleStackFuerHomeposition(mensch, xPosCurrent, yPosCurrent);
 			}
 		}
 		
 		
 		if(mensch.getLocationId()!=zielLoc || (int)(Math.random()*2) == 1){ 
 			for (int i = counter; i>=0; i--){
 				if (mensch.getLocationId()==zielLoc && i == 0 && zielLoc=='P'){
 					if (valuesInRange(xPosCurrent, yPosCurrent+1)){
 						if (locationIds.get(yPosCurrent+1).get(xPosCurrent).equals(String.valueOf(counter+1))) {			//unten gehts weiter
 							yPosCurrent++;
 							neuerWeg.push('o');
 						}
 					}
 					if (valuesInRange(xPosCurrent+1, yPosCurrent)){
 						if (locationIds.get(yPosCurrent).get(xPosCurrent+1).equals(String.valueOf(counter+1))) {			//rechts gehts weiter
 							xPosCurrent++;
 							neuerWeg.push('l');
 						}
 					}
 					if (valuesInRange(xPosCurrent, yPosCurrent-1)){
 						if (locationIds.get(yPosCurrent-1).get(xPosCurrent).equals(String.valueOf(counter+1))) {			//oben gehts weiter
 							yPosCurrent--;
 							neuerWeg.push('u');
 						}
 					}
 					if (valuesInRange(xPosCurrent-1, yPosCurrent)){
 						if (locationIds.get(yPosCurrent).get(xPosCurrent-1).equals(String.valueOf(counter+1))) {			//links gehts weiter
 							xPosCurrent--;
 							neuerWeg.push('r');
 						}
 					}
 				} else{
 					if (valuesInRange(xPosCurrent, yPosCurrent+1)){
 						if (locationIds.get(yPosCurrent+1).get(xPosCurrent).equals(String.valueOf(i))) {			//unten gehts weiter
 						yPosCurrent++;
 						neuerWeg.push('o');
 						}
 					}
 					if (valuesInRange(xPosCurrent+1, yPosCurrent)){
 						if (locationIds.get(yPosCurrent).get(xPosCurrent+1).equals(String.valueOf(i))) {			//rechts gehts weiter
 						xPosCurrent++;
 						neuerWeg.push('l');
 						}
 					}
 					if (valuesInRange(xPosCurrent, yPosCurrent-1)){
 						if (locationIds.get(yPosCurrent-1).get(xPosCurrent).equals(String.valueOf(i))) {			//oben gehts weiter
 						yPosCurrent--;
 						neuerWeg.push('u');
 						}
 					}
 					if (valuesInRange(xPosCurrent-1, yPosCurrent)){
 						if (locationIds.get(yPosCurrent).get(xPosCurrent-1).equals(String.valueOf(i))) {			//links gehts weiter
 						xPosCurrent--;
 						neuerWeg.push('r');
 						}
 					}
 				}
 			}
 		} else {
 			for (int i = 1; i<=counter+1; i++){
 				if (zielLoc!='P'){
 					i--;
 				}
 				if (valuesInRange(xPosCurrent, yPosCurrent+1)){
 					if (locationIds.get(yPosCurrent+1).get(xPosCurrent).equals(String.valueOf(i))) {			//unten gehts weiter
 					yPosCurrent++;
 					neuerWeg.push('o');
 					}
 				}
 				if (valuesInRange(xPosCurrent+1, yPosCurrent)){
 					if (locationIds.get(yPosCurrent).get(xPosCurrent+1).equals(String.valueOf(i))) {			//rechts gehts weiter
 					xPosCurrent++;
 					neuerWeg.push('l');
 					}
 				}
 				if (valuesInRange(xPosCurrent, yPosCurrent-1)){
 					if (locationIds.get(yPosCurrent-1).get(xPosCurrent).equals(String.valueOf(i))) {			//oben gehts weiter
 					yPosCurrent--;
 					neuerWeg.push('u');
 					}
 				}
 				if (valuesInRange(xPosCurrent-1, yPosCurrent)){
 					if (locationIds.get(yPosCurrent).get(xPosCurrent-1).equals(String.valueOf(i))) {			//links gehts weiter
 					xPosCurrent--;
 					neuerWeg.push('r');
 					}
 				} 
 				if (zielLoc!='P'){
 					i++;
 				}
 			}
 		}
 		
 		
 		return neuerWeg;
 	}
 
 	//Support Tiki
 	private Stack<Character>fuelleStackFuerHomeposition( Mensch mensch, int xPosCurrent, int yPosCurrent) {
 		Stack<Character> neuerWeg = new Stack<Character>();
 
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+1 == xPosCurrent){
 			neuerWeg.push('l');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+2 == xPosCurrent){
 			neuerWeg.push('l');
 			neuerWeg.push('l');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)-1 == xPosCurrent){
 			neuerWeg.push('r');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)-2 == xPosCurrent){
 			neuerWeg.push('r');
 			neuerWeg.push('r');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)-1 == yPosCurrent){
 			neuerWeg.push('u');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)-2 == yPosCurrent){
 			neuerWeg.push('u');
 			neuerWeg.push('u');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+1 == yPosCurrent){
 			neuerWeg.push('o');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+2 == yPosCurrent){
 			neuerWeg.push('o');
 			neuerWeg.push('o');
 		}
 				
 		return neuerWeg;
 	}
 
 	
 	//Support Tiki
 	private ArrayList<ArrayList<String>> rasterkarteInitialisieren(ArrayList<ArrayList<String>> locationIds, String zielLocation, char locationId) {
 		for (int i=0; i<locationIds.size(); i++){
 			for (int j=0; j<locationIds.get(i).size(); j++){
 				if (locationIds.get(i).get(j).charAt(0) != 'X' && locationIds.get(i).get(j).charAt(0) != zielLocation.charAt(0) && locationIds.get(i).get(j).charAt(0) != 'P' && locationIds.get(i).get(j).charAt(0) != locationId ){
 					locationIds.get(i).set(j,"You shall not pass!") ;
 				}
 				if (!(zielLocation.equals("P") && locationId == 'P')){
 					if (locationIds.get(i).get(j).charAt(0) == zielLocation.charAt(0)){
 						locationIds.get(i).set(j,"Z") ;
 					}
 					if (locationIds.get(i).get(j).charAt(0) == locationId ){
 						locationIds.get(i).set(j,"X") ;
 					}
 				} else{
 					if (locationIds.get(i).get(j).charAt(0) == 'X'){
 						locationIds.get(i).set(j,"You shall not pass!") ;
 					}
 				}
 				if (locationIds.get(i).get(j).charAt(0) == 'P'){
 					locationIds.get(i).set(j,"X") ;
 				}
 				
 			}
 		}
 		return locationIds;
 	}
 	
 	
 	//Support Tiki
 	private void fuelleStackRumwuseln(int doTimes) {
 		Stack<Character> neuerWeg = new Stack<Character>();
 		ArrayList<ArrayList<String>> locationIds;
 		locationIds = Ressources.getLocation_ids();
 		
 		locationIds.get((agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 		
 		locationIds = rasterkarteInitialisieren(locationIds, String.valueOf(getAgent().getLocationId()), getAgent().getLocationId());
 		
 		locationIds.get((agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"Z");
 		
 		for (int i=0; i<15; i++){
 			for (int j=0; j<24; j++){
 				if ((locationIds.get(i).get(j)=="Z" || locationIds.get(i).get(j)=="0") && locationIds.get(i+1).get(j)=="Z" && locationIds.get(i-1).get(j)=="Z" && locationIds.get(i).get(j+1)=="Z" && locationIds.get(i).get(j-1)=="Z"){
 					
 					//Nun luft der Agent lustig hin und her
 					for (int k=0; k<doTimes; k++){
 						neuerWeg.push('r');
 						neuerWeg.push('o');
 						neuerWeg.push('l');
 						neuerWeg.push('u');
 						neuerWeg.push('r');
 						neuerWeg.push('u');
 						neuerWeg.push('l');
 						neuerWeg.push('o');
 						neuerWeg.push('o');
 						neuerWeg.push('l');
 						neuerWeg.push('u');
 						neuerWeg.push('u');
 						neuerWeg.push('r');
 						neuerWeg.push('o');
 					}
 					
 					if (this.agent.getPosX() == this.agent.getHomePosX() && this.agent.getPosY() ==this.agent.getHomePosY()){
 						neuerWeg.push('u');
 						neuerWeg.push('r');
 					} else{
 						if ((this.agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT==j && (this.agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT==i+1){
 							neuerWeg.push('o');
 						} else {
 							if ((this.agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT!=j || (this.agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT!=i){
 								//Der Agent wird zum Mittelpunkt des Hauses geleitet
 								if (locationIds.get(i-2).get(j-1).equals("X") || locationIds.get(i-1).get(j-2).equals("X")){
 									neuerWeg.push('u');
 									neuerWeg.push('r');
 								}
 								if (locationIds.get(i-2).get(j).equals("X")){
 									neuerWeg.push('u');
 								}
 								if (locationIds.get(i-2).get(j+1).equals("X") || locationIds.get(i-1).get(j+2).equals("X")){
 									neuerWeg.push('u');
 									neuerWeg.push('l');
 								}
 								if (locationIds.get(i).get(j+2).equals("X")){
 									neuerWeg.push('l');
 								}
 								if (locationIds.get(i+1).get(j+2).equals("X") || locationIds.get(i+2).get(j+1).equals("X")){
 									neuerWeg.push('o');
 									neuerWeg.push('l');
 								}
 								if (locationIds.get(i+2).get(j).equals("X")){
 									neuerWeg.push('o');
 								}
 								if (locationIds.get(i+2).get(j-1).equals("X") || locationIds.get(i+1).get(j-2).equals("X") ){
 									neuerWeg.push('o');
 									neuerWeg.push('r');
 								}
 								if (locationIds.get(i).get(j-2).equals("X")){
 									neuerWeg.push('r');
 								}
 							} 
 						}	
 					}
 				}
 			}
 		}	
 		getAgent().setMoves(neuerWeg);
 	}
 	
 	
 	//Support Tiki
 	public void doSomethingAfterAgentAction(){
 		int personId =0;
 		
 		if (!wieschteAktion && getAgent().getMussWuseln() != "Park" && getAgent().getMussWuseln() != "Park+"){ 
 			if (getAgent().getMussWuseln().charAt(1) <='9' && getAgent().getMussWuseln().charAt(1)>='0'){
 				personId = Integer.parseInt(getAgent().getMussWuseln().substring(0,2));
 			} else{
 				personId = Integer.parseInt(getAgent().getMussWuseln().substring(0,1));
 			}
 		}
 		
 		
 		//Spionage
 		if(getAgent().getMussWuseln().equals("Wanze+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().add("Wanze");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert((float)(Math.random()*17.5f+1)+22.5f,0);
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Wanze")){
 			fuelleStackRumwuseln(2);
 			getAgent().setMussWuseln("Wanze+");
 		} 
 		
 		if(getAgent().getMussWuseln().equals("Kamera+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().add("Kamera");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert((float)(Math.random()*10+1)+35,1);
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Kamera")){
 			fuelleStackRumwuseln(3);
 			getAgent().setMussWuseln("Kamera+");
 		}
 		
 		if(getAgent().getMussWuseln().equals("Hacken+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().add("Hacken");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert((float)(Math.random()*20+1)+15,2);
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Hacken")){
 			fuelleStackRumwuseln(2);
 			getAgent().setMussWuseln("Hacken+");
 		}
 		
 		
 		if(getAgent().getMussWuseln().length()==10 && getAgent().getMussWuseln().substring(1,10).equals("Fernglas+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getMussWuseln().charAt(0)-48-1)).getUeberwachungsmodule().add("Fernglas");
 			getHouses().get((int)(getAgent().getMussWuseln().charAt(0)-48-1)).setUeberwachungsWert((float)(Math.random()*7+1)+8,3);
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().length()==9 && getAgent().getMussWuseln().substring(1,9).equals("Fernglas")){
 			fuelleStackRumwuseln(1);
 			getAgent().setMussWuseln(getAgent().getMussWuseln()+"+");
 		}
 		
 
 		
 		
 //		if(get_agent().getMussWuseln().equals("Park+") && get_agent().getCurrentMove()=='n'){
 //			berechne_weg(null, agent, 'P');
 //			calcMisstrauenAfterBeschwichtigenInPark();
 //		}
 		if(getAgent().getMussWuseln().equals("Park")){
 			calcWeg(agent, 'P');
 			calcMisstrauenNachBeschwichtigenInPark();
 		} 		
 		
 		
 		
 		//Spionage entfernen
 		
 		if(getAgent().getMussWuseln().equals("Wanzer+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().remove("Wanze");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert(0,0);			
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Wanzer")){
 			fuelleStackRumwuseln(2);
 			getAgent().setMussWuseln("Wanzer+");
 		}
 		
 		if(getAgent().getMussWuseln().equals("Kamerar+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().remove("Kamera");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert(0,1);			
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Kamerar")){
 			fuelleStackRumwuseln(3);
 			getAgent().setMussWuseln("Kamerar+");
 		}
 		
 		if(getAgent().getMussWuseln().equals("Hackenr+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).getUeberwachungsmodule().remove("Hacken");
 			getHouses().get((int)(getAgent().getLocationId()-48-1)).setUeberwachungsWert(0,2);			
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().equals("Hackenr")){
 			fuelleStackRumwuseln(2);
 			getAgent().setMussWuseln("Hackenr+");
 		}
 		
 		if(getAgent().getMussWuseln().length()==11 && getAgent().getMussWuseln().substring(1,11).equals("Fernglasr+") && getAgent().getCurrentMove()=='n'){
 			getHouses().get((int)(getAgent().getMussWuseln().charAt(0)-48)).getUeberwachungsmodule().remove("Fernglas");
 			getHouses().get((int)(getAgent().getMussWuseln().charAt(0)-48)).setUeberwachungsWert(0,3);
 			getAgent().setMussWuseln("");
 		}
 		if(getAgent().getMussWuseln().length()==10 && getAgent().getMussWuseln().substring(1,10).equals("Fernglasr")){
 			fuelleStackRumwuseln(1);
 			getAgent().setMussWuseln(getAgent().getMussWuseln()+"+");
 		}
 			
 		
 		
 		
 		//Soziales
 		if(getAgent().getMussWuseln().length()>=8 && personId <=9 &&  getAgent().getMussWuseln().substring(1,8).equals("Kuchen+") ||
 				getAgent().getMussWuseln().length()>=9 && personId >9 &&  getAgent().getMussWuseln().substring(2,9).equals("Kuchen+")){
 			calcMisstrauenNachBeschwichtigen(0, getPeople().get(personId));
 			getPeople().get(personId).erhoeheDurchgefuehrteBeschwichtigungen(0);
 			getPeople().get(personId).setMoves(new Stack());
 			getAgent().setMussWuseln("");
 		}	
 		if(getAgent().getMussWuseln().length()>=7 && personId <=9 && getAgent().getMussWuseln().substring(1,7).equals("Kuchen") ||
 				getAgent().getMussWuseln().length()>=8 && personId >9 && getAgent().getMussWuseln().substring(2,8).equals("Kuchen") ){
 			getAgent().setMussWuseln(personId+"Kuchen+");
 			//wuseln 
 		}	
 		
 		if(getAgent().getMussWuseln().length()>=13 && personId <=9 &&  getAgent().getMussWuseln().substring(1,13).equals("Unterhalten+") ||
 				getAgent().getMussWuseln().length()>=14 && personId >9 &&  getAgent().getMussWuseln().substring(2,14).equals("Unterhalten+")){
 			calcMisstrauenNachBeschwichtigen(1, getPeople().get(personId));
 			getPeople().get(personId).erhoeheDurchgefuehrteBeschwichtigungen(1);
 			getPeople().get(personId).setMoves(new Stack());
 			getAgent().setMussWuseln("");
 		}	
 		if(getAgent().getMussWuseln().length()>=12 && personId <=9 && getAgent().getMussWuseln().substring(1,12).equals("Unterhalten") ||
 				getAgent().getMussWuseln().length()>=13 && personId >9 && getAgent().getMussWuseln().substring(2,13).equals("Unterhalten") ){
 			getAgent().setMussWuseln(personId+"Unterhalten+");
 			//wuseln 
 		}
 		
 		if(getAgent().getMussWuseln().length()>=9 && personId <=9 &&  getAgent().getMussWuseln().substring(1,9).equals("Flirten+") ||
 				getAgent().getMussWuseln().length()>=10 && personId >9 &&  getAgent().getMussWuseln().substring(2,10).equals("Flirten+")){
 			calcMisstrauenNachBeschwichtigen(2, getPeople().get(personId));
 			getPeople().get(personId).erhoeheDurchgefuehrteBeschwichtigungen(2);
 			getPeople().get(personId).setMoves(new Stack());
 			getAgent().setMussWuseln("");
 		}	
 		if(getAgent().getMussWuseln().length()>=8 && personId <=9 && getAgent().getMussWuseln().substring(1,8).equals("Flirten") ||
 				getAgent().getMussWuseln().length()>=9 && personId >9 && getAgent().getMussWuseln().substring(2,9).equals("Flirten") ){
 			getAgent().setMussWuseln(personId+"Flirten+");
 			//wuseln 
 		}
 		
 		if(getAgent().getMussWuseln().length()>=6 && personId <=9 &&  getAgent().getMussWuseln().substring(1,6).equals("Hand+") ||
 				getAgent().getMussWuseln().length()>=7 && personId >9 &&  getAgent().getMussWuseln().substring(2,7).equals("Hand+")){
 			calcMisstrauenNachBeschwichtigen(3, getPeople().get(personId));
 			getPeople().get(personId).erhoeheDurchgefuehrteBeschwichtigungen(3);
 			getPeople().get(personId).setMoves(new Stack());
 			getAgent().setMussWuseln("");
 		}	
 		if(getAgent().getMussWuseln().length()>=5 && personId <=9 && getAgent().getMussWuseln().substring(1,5).equals("Hand") ||
 				getAgent().getMussWuseln().length()>=6 && personId >9 && getAgent().getMussWuseln().substring(2,6).equals("Hand") ){
 			getAgent().setMussWuseln(personId+"Hand+");
 			//wuseln 
 		}
 				
 	}
 	
 	
 	//Support Tiki
 	private void calcColouredPeople (int hausid){
 		for (int i = 0; i<getPeople().size(); i++){
 			if (getPeople().get(i).getHausId() == hausid && !getPeople().get(i).getIstFarbig()){
 				getPeople().get(i).setIstFarbig(true);
 				getPeople().get(i).farbeZeigen();
 			}
 		}
 	}
 	
 	
 	//Support Tiki
 	private boolean valuesInRange(int x, int y){
 		if (x>=0 && x<Ressources.MAPWIDTH/Ressources.RASTERHEIGHT && 
 				y>=0 && y<Ressources.MAPHEIGHT/Ressources.RASTERHEIGHT){
 			return true;
 		} else{
 			return false;
 		}
 	}
 	
 	
 
 	//Support Tiki
 	public boolean calcGameOver(){
 		if (calcMisstrauenInStreet()>=90.0){
 			return true;
 		}
 		for (int i = 0; i < this.people.size(); i++){
 			if (this.people.get(i).getBorder()!= null && this.people.get(i).getCurrentMove()=='n' && 
 					this.people.get(i).getLocationId()=='E'){
 				return true;
 			}
 			if (this.people.get(i) instanceof Terrorist && this.people.get(i).getMisstrauen() >= 85.00){
 				if (this.people.get(i).getCurrentMove()=='n'){
 					calcWeg(this.people.get(i), 'E');
 					this.people.get(i).setBorder(BorderFactory.createLineBorder(Color.red));
 					return false;
 				}
 			} 
 			
 		}	
 		return false;
 	}
 	
 	
 	
 	public ArrayList<Person> getPeople(){
 		return people;
 	}
 	
 	public void setPeople(Person p){
 		people.add(p);
 	}
 	
 	
 	public Agent getAgent(){
 		return agent;
 	}
 	
 	public void setAgent(Agent agent){
 		this.agent = agent;
 	}
 
 
 	public int getSpielTag() {
 		return spielTag;
 	} 
 
 
 	public int getSpielStunde() {
 		return spielStunde;
 	}
 
 
 	public int getSpielMinute() {
 		return spielMinute;
 	}
 
 	
 	public String getSpielzeitAsString() {
 		String zeit="";
 		zeit = String.valueOf(this.spielStunde);
 		if (this.spielStunde <=9){
 			zeit = "0" + zeit;
 		}
 		zeit = zeit + ":";
 		if (this.spielMinute <=9){
 			zeit = zeit + "0";
 		}
 		zeit = zeit + String.valueOf(this.spielMinute);
 		
 		return zeit;
 	}
 	
 	public ArrayList<Haus> getHouses(){
 		return this.houses;
 	}
 	
 	public void setHouses(Haus haus){
 		this.houses.add(haus);
 	}
 
 
 //	public boolean isWieeeeschteAktion() {
 //		return wieeeeschteAktion;
 //	}
 
 
 	public void setWieschteAktion(boolean wieeeeschteAktion) { 
 		this.wieschteAktion = wieeeeschteAktion;
 	}
 
 	public boolean isWieschteAktion(){
 		return this.wieschteAktion;
 	}
 	
 	public void calcMisstrauenMax(){
 		double misstrauen = this.calcMisstrauenInStreet();
 		if(this.misstrauenMax < misstrauen){
 			this.misstrauenMax = misstrauen;
 		}
 	}
 	
 	public double getMisstrauenMax(){
 		return this.misstrauenMax;
 	}
 }
