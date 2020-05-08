 package com.stalkindustries.main.game;
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import com.stalkindustries.main.TheStreet;
 
 public class Simulation {
 	
 	private int[][] beziehungsmatrix;
 	//private int[] location_raster; nicht mehr bentigt
 	private ArrayList<Person> people = new ArrayList<Person>(); 
 	private Agent agent = new Agent(0,"");
 	private int spiel_tag=1;
 	private int spiel_stunde=7;
 	private int spiel_minute=0;
 	private ArrayList<Haus> houses = new ArrayList<Haus>();
 	private boolean wieeeeschteAktion=true;  //wieeeeescht = boese
 	private double misstrauen_max=-100;
 
 	
 	public Simulation(){
 		
 	}
 	
 	
 	//Beschwerden an Miri
 	void initialize_beziehungsmatrix(){
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
 					if(this.people.get(i).get_haus_id() == this.people.get(j).get_haus_id()){ //Person in einem Haushalt sind besser miteinander befreundet
 						tmp = (10-tmp)/2;
 						this.beziehungsmatrix[i][j] += tmp;
 						this.beziehungsmatrix[j][i] += tmp;
 					}
 				}
 			}
 		}
 		
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
 	void calculate_misstrauen(){
 		if(this.spiel_stunde > 5 || this.spiel_stunde < 2){
 			//misstrauen[] ist eine Hilfvariable, die spter die spter die neuen Werte enthlt und sie wird bentigt, dass nicht die echten Werte verndert werden, bevor alle berechnet wurden
 			double[] misstrauen = new double[people.size()];
 			
 			double faktor = 0.01275; //Faktor, der regelt, wie stark sich Personen bei einem Methodenaufruf beeinflussen --> abhngig von Hufigkeit des Methodenaufrufs
 			
 			//misstrauen[] mit den alten Misstrauenswerten initialisieren
 			for(int i=0;i<this.people.size();i++)
 				misstrauen[i] = this.people.get(i).get_misstrauen();
 			
 			//jede Person kann theoretisch wieder von jeder anderen beeinflusst werden
 			for(int i=0;i<this.people.size();i++){
 				//wenn sich Person dort befindet, wo sie auch beeinflusst werden kann
 				if((int)(this.people.get(i).get_location_id())-48+1 != 0 && (int)(this.people.get(i).get_location_id())-48+1 != 'X' && (int)(this.people.get(i).get_location_id())-48+1 != 'E'){
 					for(int j=0;j<this.people.size();j++){
 						//eine Person kann sich nicht selbst beeinflussen
 						if(i!=j){
 							//wenn sich die beiden Personen am selben Ort befinden
 							if((int)(this.people.get(i).get_location_id())-48+1 == (int)(this.people.get(j).get_location_id())-48+1){
 								//Ausnahme in der Berechnung: Kinder beeinflussen Erwachsene weniger
 								if(this.people.get(i) instanceof Erwachsene && this.people.get(j) instanceof Kinder){	//Kind beeinflusst Erwachsenen weniger
 									misstrauen[i] = misstrauen[i] - faktor/2*this.beziehungsmatrix[i][j]*(this.people.get(i).get_misstrauen()-this.people.get(j).get_misstrauen());
 								}
 								else{
 									misstrauen[i] = misstrauen[i] - faktor*this.beziehungsmatrix[i][j]*(this.people.get(i).get_misstrauen()-this.people.get(j).get_misstrauen());
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
 				this.people.get(i).set_misstrauen(misstrauen[i]);
 			}
 		}
 	}
 	
 	//Support: Tobi
 		public void update_position(){
 			for(int i=0;i<people.size();i++){
 				people.get(i).step();
 			}
 			
 		}
 	
 	//Beschwerden an Miri
 	//Mittelwert des Misstrauens der einzelnen Personen
 	public double calc_misstrauen_in_street(){
 		float misstrauen = 0;
 		for(int i=0;i<this.people.size();i++){
 			misstrauen += this.people.get(i).get_misstrauen();
 		}
 		misstrauen = misstrauen/this.people.size();
 		
 		return misstrauen;
 	}
 	
 	
 	//Beschwerden Miri
 	public void calcMisstrauenAfterBeschwichtigenInPark(){
 		for(int i=0;i<this.people.size();i++){
 			//Checken, ob sich noch jemand im Park befindet
 			if(this.people.get(i).get_location_id() == 'P'){							//Ich (Tiki) hab den wert auf 1 gesetzt, da es sonst zu schnell war
 				this.people.get(i).set_misstrauen(this.people.get(i).get_misstrauen()-1); //TODO: den Wert testen ... eventuell erhhen
 			}
 			
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).get_misstrauen()>100)
 				this.people.get(i).set_misstrauen(100);
 			if(this.people.get(i).get_misstrauen()<-100)
 				this.people.get(i).set_misstrauen(-100);
 		}
 	}
 	
 	//Beschwerden an Miri
 	public void calc_misstrauen_after_beschwichtigen_action(int action_id, Person person){
 		int zufall=0;
 		int risiko;
 		double misstrauen = person.get_misstrauen();
 		
 		//Fehlschlagen der Aktionen ist unter anderem abhngig vom Misstrauensstatus
 				if(misstrauen >= 0){
 					zufall = (int)(Math.random()*(misstrauen/10));
 					if(zufall == 0)
 						zufall = 1;
 				}
 				//Personen sind weniger misstrauisch
 				else{
 					if(misstrauen/10 < 0){
 						zufall = (int)(Math.random()*(-(misstrauen/10)));
 						if(zufall == 0)
 							zufall = -1;
 					}
 //					else
 //						zufall = (int)(Math.random()*(misstrauen/10));
 				}
 		
 		//Risiko einer Beschwichtigenaktion steigt, je hufiger man sie ausfhrt
 		risiko = person.get_durchgefuehrteBeschwichtigungen(action_id);
 		if(action_id == 0 || action_id == 2 || action_id == 3){	//Kuchen, Flirten, Helfen
 			if(risiko == 0){
 				if(zufall > 5)
 					zufall *= (-1);
 				else if(zufall >= 0)
 					zufall *= (-5);
 				else
 					zufall *= 6;
 			}
 			else{
 				if(zufall > 5)
 					zufall *= 6;
 				else if(zufall >= 0)
 					zufall *= 3;
 				else
 					zufall *= (-3);
 			}
 		}
 		else if(action_id == 1){	//bei Haus vorbeigehen, um zu reden
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
 		else{						//Im Park unterhalten
 				if(zufall >= 0)
 					zufall *= (-2);
 				else
 					zufall *= 3;
 		}
 		
 		misstrauen += zufall;
 		
 		
 //		//TODO Testen, ob die Werte passen
 //		//wie sich das Misstrauen verndern soll
 //		if(zufall < -5)
 //			misstrauen -= 30;
 //		else if(zufall <= 0)
 //			misstrauen -= 20;
 //		else if(zufall < 5)
 //			misstrauen += 10;
 //		else if(zufall > 8)
 //			misstrauen += 20;
 		
 		
 		//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 		if(misstrauen>100)
 			misstrauen = 100;
 		if(misstrauen<-100)
 			misstrauen = -100;
 		
 		//Misstrauen der Person setzen
 		person.set_misstrauen(misstrauen);
 		
 		//Bei Person die Anzahl der Beschwichtigenaktionen erhhen
 		//this.people.get(personen_id).set_durchgefuehrteBeschwichtigungen(action_id);
 	}
 	
 	
 	//TODO
 	//Beschwerden an Miri
 	public void calc_misstrauen_after_ueberwachungs_action(int house_location){
 		//Risiko berechnen
 		//Risiko ist abhngig von der Uhrzeit, d.h.tagsber ist das Risiko hher als nachts
 		int risiko=0;
		if(this.spiel_stunde>1 && this.spiel_stunde<6){	//Nachtmodus
 			risiko = (int)(Math.random()*3); 
 		}
 		else{	//Tagmodus
 			risiko = (int)(Math.random()*15); 
 		}
 		
 		int mittelpunktX = this.houses.get(house_location-1).getPosX()+3*Ressources.RASTERHEIGHT/2;
 		int mittelpunktY = this.houses.get(house_location-1).getPosY()+3*Ressources.RASTERHEIGHT/2;
 		int epsilon = 200;	
 		
 		for(int i=0;i<this.people.size();i++){
 			//Checken, ob sich noch jemand in dem Haus befindet
 			//fr alle Personen, die noch im Haus sind, das Misstrauen neu berechnen
 			if((int)(this.people.get(i).get_location_id())-48 == house_location){
 				if(risiko>2)	//wenn das risiko kleiner ist, hat man Glck und man wird nicht erwicht
 					this.people.get(i).set_misstrauen(this.people.get(i).get_misstrauen()+6); //TODO: den Wert 50 testen ... eventuell erhhen
 			}
 			//Checken, ob sich jemand in einer epsilon-Umgebung um das Haus befindet, in das eingebrochen werden soll
 			//--> 1. Epsilon-Umgebung aufspannen (ist eine relative eckige :-D)
 			//-->Mittelpunkt vom Haus bestimmen
 			else{
 				// wenn sich eine Person in der Epsilon-Umgebung befindet
 				if(this.people.get(i).getPosX() >= mittelpunktX-epsilon && this.people.get(i).getPosX() <= mittelpunktX+epsilon && this.people.get(i).getPosY() >= mittelpunktY-epsilon && this.people.get(i).getPosY() <= mittelpunktY+epsilon && this.people.get(i).get_location_id()!='E'){
 					//wenn das per Zufall eine Person ist, die in dem Haus wohnt und z.B. auf dem Heimweg ist
 					//hier ist das Misstrauen natrlich grer
 					if(this.people.get(i).get_haus_id()+1 == house_location){
 						if(risiko>2)
 							this.people.get(i).set_misstrauen(this.people.get(i).get_misstrauen()+2);
 					}
 					else{
 						if(risiko>2)
 							this.people.get(i).set_misstrauen(this.people.get(i).get_misstrauen()+1);
 					}
 				}
 			}
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).get_misstrauen()>100)
 				this.people.get(i).set_misstrauen(100);
 			if(this.people.get(i).get_misstrauen()<-100)
 				this.people.get(i).set_misstrauen(-100);
 		}
 		
 	}
 	
 	
 	//Beschwerden Miri
 	public void agentEntersOtherHouse(){
 		//wenn sich der Agent in irgendeinem Haus befindet
 		if((int)(get_agent().get_location_id())-48 >= 1 && (int)(get_agent().get_location_id())-48 <= 9){
 			//wenn sich der Agent in dem Haus befindet, das ausspioniert werden soll
 			if(get_agent().get_location_id() != (char)get_agent().get_haus_id()+48+1){
 				if (wieeeeschteAktion){
 					calc_misstrauen_after_ueberwachungs_action((int)(get_agent().get_location_id()-48));
 				}
 					
 			}
 		}
 	}	
 	
 	
 	//Beschwerden Miri
 	//whrend ein Haus berwacht wird, macht sich bei den Bewohnern ein Unwohlsein breit und sie werden mehr misstrauisch
 	public void calc_misstrauen_during_ueberwachung(){
 		for(int i=0;i<this.people.size();i++){
 			// wenn berwachungsmodule in dem Haus, in dem die Person lebt, installiert wurden
 			if(this.houses.get(this.people.get(i).get_haus_id()).getUeberwachungsmodule().size() > 0){
 				this.people.get(i).set_misstrauen(this.people.get(i).get_misstrauen()+2);
 			}
 			//sorgt dafr, dass sich das Misstrauen zwischen -100 und 100 bewegt
 			if(this.people.get(i).get_misstrauen()>100)
 				this.people.get(i).set_misstrauen(100);
 			if(this.people.get(i).get_misstrauen()<-100)
 				this.people.get(i).set_misstrauen(-100);
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
 	public float calc_ueberwachung_in_street(){
 		float ueberwachung = 0;
 		for(int i=0;i<this.houses.size();i++){
 			ueberwachung += this.houses.get(i).getUeberwachungsstatus();
 		}
 		ueberwachung = ueberwachung/(this.houses.size()-1);
 		
 		return ueberwachung;
 	}
 	
 
 	
 	
 	// Support Tiki
 	void calc_spielzeit(){
 		this.spiel_minute++;
 		if (this.spiel_minute==60){
 			this.spiel_minute = 0;
 			this.spiel_stunde++;
 		}
 		if (this.spiel_stunde==24){
 			this.spiel_stunde=0;
 			this.spiel_tag++;
 		}
 	}
 	
 	
 	//Support Tiki
 	public void tagesablauf(){
 		char locid ='0';
 		int hausid = 0;
 		for	(int i=0; i<this.people.size(); i++){
 			locid = this.people.get(i).get_location_id();
 			hausid = this.people.get(i).get_haus_id()+1;
 			
 			// Es bekommen nur die Menschen einen neuen Weg zugewiesen, wenn sie sich gerade nicht bewegen
 			if (this.people.get(i).getCurrentMove() == 'n'){
 				
 				//Zuerst wird der Tagesablauf der Kinder berprft, da dieser von den Erwachsenen unterschiedlich ist
 				if (this.people.get(i) instanceof Kinder){
 					if ((this.people.get(i).getZeitverzogerung() + this.spiel_minute) == 60){
 						
 						if (this.spiel_stunde==7){ //zur Schule gehen
 							berechne_weg(this.people.get(i), 'E');
 						}	
 						
 						//Nur wenn der Mensch nicht zuHause ist, kann er nach Hause gehen
 						if ((this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){
 							
 							if (this.spiel_stunde==14){ //nach Hause gehen
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));				
 							}
 							if (this.spiel_stunde==20){ //nach Hause gehen
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 							}	
 						} 
 					} else {
 
 						//nach Hause gehen, notwendig, falls die Kinder noch eine runde im Park drehen & 20 Uhr berschritten wird
 						if (this.spiel_stunde>=20 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ 
 							berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 						}
 						
 						// randomisiert in den Park gehen
 						if (this.spiel_stunde >= 15 && this.spiel_stunde <=19 && locid !='P'){ 
 							if ((int)(Math.random()*200) == 3){
 								berechne_weg(this.people.get(i), 'P');
 							}
 						}
 						
 						// randomisiert den Park verlassen oder noch eine Runde drehen
 						if (locid =='P' && this.spiel_stunde<=19){ 
 							if ((int)(Math.random()*5) == 3){
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0)); 
 							} else{
 								if (this.people.get(i).getCurrentMove() == 'n'){
 									berechne_weg(this.people.get(i), 'P');
 								}
 							}
 						}
 					}
 					
 				}else{
 					//Nun wir der Tagesablauf der Erwachsenen untersucht
 					if (this.people.get(i) instanceof Erwachsene){
 						
 						//Zuerst werden die Erwachsenen untersucht, die Arbeit haben
 						if (((Erwachsene)people.get(i)).isHat_arbeit()){
 							if (this.spiel_stunde==8 && (this.people.get(i).getZeitverzogerung() + this.spiel_minute) == 60){ // Zur Arbeit gehen
 								berechne_weg(this.people.get(i), 'E');
 							}
 							if (this.spiel_stunde==16 && (this.people.get(i).getZeitverzogerung() + this.spiel_minute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 							}				
 							if ((this.spiel_stunde >= 17 || this.spiel_stunde <=0)){  // in den Park gehen
 								if ((int)(Math.random()*300) == 3){
 									berechne_weg(this.people.get(i), 'P');
 								}
 							}
 						} else {
 							if (this.spiel_stunde == 9 && (this.people.get(i).getZeitverzogerung() + this.spiel_minute) == 60){ //zum Einkaufen gehen
 								if ((int)(Math.random()*3)+1 == 1){
 									berechne_weg(this.people.get(i), 'E');
 								}
 							}	
 							if (this.spiel_stunde >= 9 && this.spiel_stunde <=14 && hausid!='E'){ //zum Einkaufen gehen
 								if ((int)(Math.random()*300) == 3){
 									berechne_weg(this.people.get(i), 'P');
 								}
 							}
 							if (this.spiel_stunde == 12 && (this.people.get(i).getZeitverzogerung() + this.spiel_minute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 							}
 							if ((this.spiel_stunde >=14 || this.spiel_stunde <=1)){  // in den Park gehen
 								if ((int)(Math.random()*300) == 3){
 									berechne_weg(this.people.get(i), 'P');
 								}
 							}
 						}
 						if (this.spiel_stunde==1 && (this.people.get(i).getZeitverzogerung() + this.spiel_minute) >= 60 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 							berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 						}
 						if (this.spiel_stunde>=2 && this.spiel_stunde<4 && (this.people.get(i).getHomePosX()!=this.people.get(i).getPosX() || this.people.get(i).getHomePosY()!=this.people.get(i).getPosY())){ //nach Hause gehen
 							berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0));
 						}
 						if (locid =='P' && (this.spiel_stunde < 2 || this.spiel_stunde >= 9)){ //nach Hause gehen
 							if ((int)(Math.random()*5) == 3){
 								berechne_weg(this.people.get(i), String.valueOf(hausid).charAt(0)); 
 							} else{
 								if (this.people.get(i).getCurrentMove() == 'n'){
 									berechne_weg(this.people.get(i), 'P');
 								}
 							}
 						}
 					}
 				}
 			
 			}
 		}
 	} 	
 	
 	
 	//Support Tiki
 	private Point berechne_Parkeingang(ArrayList<ArrayList<String>> location_ids){
 		int parkcounter=0;
 		
 		// Der Park darf sich niemals ganz am Rand befinden (sieht auch doof aus)
 		for (int i = 1; i<14; i++){
 			for (int j = 1; j<23; j++){
 				if (location_ids.get(i).get(j).equals("P")){
 					if (i<13){
 						if (location_ids.get(i+1).get(j).equals("P") || location_ids.get(i+1).get(j).equals("X")){
 							parkcounter++;
 						}
 					}
 					if (i>0){
 						if (location_ids.get(i-1).get(j).equals("P") || location_ids.get(i-1).get(j).equals("X")){
 							parkcounter++;
 						}
 					}
 					if (j<22){
 						if (location_ids.get(i).get(j+1).equals("P") || location_ids.get(i).get(j+1).equals("X")){
 							parkcounter++;
 						}
 					}
 					if (j>0){
 						if (location_ids.get(i).get(j-1).equals("P") || location_ids.get(i).get(j-1).equals("X")){
 							parkcounter++;
 						}
 					}
 					
 				}
 				if (parkcounter == 3){
 					return new Point (i,j);
 				} else{
 					parkcounter=0;
 				}
 			}
 		}		
 		return null;
 	}
 	
 	
 	
 	//Support Tiki
 	public void berechne_weg(Mensch mensch, char zielloc){
 		
 		char locationId;				
 		ArrayList<ArrayList<String>> location_ids;
 		Stack<Character> neuer_weg = new Stack<Character>();
 		Point parkeingang = new Point();
 		
 		locationId = mensch.get_location_id();			
 		location_ids = Ressources.getLocation_ids();
 		
 		if (zielloc == 'P'){
 			parkeingang = berechne_Parkeingang(location_ids);
 		}
 		
 		if (mensch instanceof Agent && zielloc == (char)(get_agent().get_haus_id()+48+1)){
 			System.out.print("Agent muss sich zu " + zielloc + " bewegen!");
 		}
 		
 		//Aktuelle Position des Mnnchens wird auf 0 gesetzt
 		if (zielloc != locationId || zielloc!='P'){
 			location_ids = wegberechnung_rasterkarte_initialisierung(location_ids, String.valueOf(zielloc), locationId);
 		} else {
 			location_ids = wegberechnung_rasterkarte_initialisierung(location_ids, "P", locationId);
 		}
 
 		//if ((mensch.getPosY()-Ressources.ZEROPOS.height)%45!=0 && ((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)%45!=0){
 			switch(mensch.getCurrentMove()){
 	        case 'r': location_ids.get((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set(((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+1,"0");
 	                  break;
 	        case 'u': location_ids.get(((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+1).set((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 	                  break;
 	        case 'o':
 	        case 'l':
 	        default: location_ids.get((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 	        }
 //		} else{
 //			location_ids.get((mensch.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((mensch.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 //		}
 		
 		//Bewegungsstack nach und nach fllen
 		neuer_weg = fuelle_bewegungs_stack(location_ids, mensch, zielloc);
 			
 		//Stack zur Bewegung freigeben
 		mensch.setMoves(neuer_weg);
 	}
 	
 	
 	//Support Tiki
 	private Stack<Character> fuelle_bewegungs_stack(ArrayList<ArrayList<String>> location_ids, Mensch mensch, char zielloc){
 		
 		Stack<Character> neuer_weg = new Stack<Character>();
 		int counter = 1;
 		int xPos_current = 0;
 		int yPos_current = 0;
 		boolean firstStep = true;
 		String ziellocation="Z";
 		
 		goal:	for (int i=0; i<100; i++){
 					for (int j=0; j<16; j++){  	// J entspricht y-wert, K entspricht x-wert
 						for (int k=0; k<25; k++){
 							// Es werden Zahlen auf der Map gesucht
 							if (location_ids.get(j).get(k).equals(String.valueOf(i))){
 								// Es wird berprft, ob das Ziel in direkter Nhe liegt
 								if (j < 15){ //15 -> Rasterhhe
 									if (location_ids.get(j+1).get(k).equals(ziellocation)) {
 										location_ids.get(j+1).set(k,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j+1;
 										xPos_current = k;
 										break goal;
 									}
 								}
 								if (k<24){ //24 -> Rasterbreite
 									if (location_ids.get(j).get(k+1).equals(ziellocation)) {
 										location_ids.get(j).set(k+1,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j;
 										xPos_current = k+1;
 										break goal;
 									}
 								}
 								if (j>0){
 									if (location_ids.get(j-1).get(k).equals(ziellocation)) {
 										location_ids.get(j-1).set(k,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j-1;
 										xPos_current = k;
 										break goal;
 									}
 								}
 								if (k>0){
 									if (location_ids.get(j).get(k-1).equals(ziellocation)) {
 										location_ids.get(j).set(k-1,String.valueOf(i+1));
 										counter = i;
 										yPos_current = j;
 										xPos_current = k-1;
 										break goal;
 									}
 								}
 //							
 								if ((i>0 && (mensch!= null && zielloc == mensch.get_location_id())) ||
 										(mensch!= null && zielloc != mensch.get_location_id()) ||
 										(i>0 && (mensch!= null && zielloc == mensch.get_location_id())) ||
 										(mensch!= null && zielloc != mensch.get_location_id())){
 									// Es wird berprft, ob ein Feld drber/drunter/links oder rechts ebenfalls begehbar ist -> das wird markiert
 									if (j<15){
 										if (location_ids.get(j+1).get(k).equals("X")) {			//Weg nach unten ist begehbar
 										location_ids.get(j+1).set(k,String.valueOf(i+1));
 										}
 									}
 									if (k<24){
 										if (location_ids.get(j).get(k+1).equals("X")) {			//Weg nach rechts ist begehbar
 										location_ids.get(j).set(k+1,String.valueOf(i+1));
 										}
 									}
 									if (j>0){
 										if (location_ids.get(j-1).get(k).equals("X")) {			// Weg nach oben ist begehbar
 										location_ids.get(j-1).set(k,String.valueOf(i+1));
 										}
 									}
 									if (k>0){
 										if (location_ids.get(j).get(k-1).equals("X")) {			//Weg nach links ist begehbar
 										location_ids.get(j).set(k-1,String.valueOf(i+1));
 										}
 									}
 								} else { //TODO anpassen!!!
 									if (j>0){
 										if (location_ids.get(j-1).get(k).equals("X") && firstStep) {			// Weg nach oben ist begehbar
 											location_ids.get(j-1).set(k,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 									if (k>0){
 										if (location_ids.get(j).get(k-1).equals("X") && firstStep) {			//Weg nach links ist begehbar
 											location_ids.get(j).set(k-1,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 									if (k<24){
 										if (location_ids.get(j).get(k+1).equals("X") && firstStep) {			//Weg nach rechts ist begehbar
 											location_ids.get(j).set(k+1,String.valueOf(i+1));
 											firstStep = false;
 										}
 									}
 									if (j<15){
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
 					if (i == 2 && zielloc=='P' && mensch.get_location_id()=='P'){
 						ziellocation="0";
 					}
 				}
 		
 		// Der Stack fr die Bewegung wird mit den richtigen Werten gefllt. Dafr hangelt man sich absteigend an der zahlenreihe entlang
 			neuer_weg = fuelle_stack_weg(zielloc, mensch, location_ids, counter, xPos_current, yPos_current);
 		
 		
 		return neuer_weg;
 	}
 	
 	//Support Tiki
 	private Stack<Character> fuelle_stack_weg(Character zielloc, Mensch mensch, ArrayList<ArrayList<String>> location_ids, int counter, int xPos_current, int yPos_current) {
 		Stack<Character> neuer_weg = new Stack<Character>();
 		
 		//Falls das Ziel ein Haus ist, soll die Person auf ihren startpunkt laufen.
 		if (mensch.get_haus_id() == (int)(zielloc-48-1)){
 			if ((int)(zielloc)-48 <=9 && (int)(zielloc)-48 > 0){ //-48 fr char umwandlung zu int
 				neuer_weg = fuelle_stack_homeposition(mensch, xPos_current, yPos_current);
 			}
 		}
 		
 		
 		if(mensch.get_location_id()!=zielloc || (int)(Math.random()*2) == 1){ 
 			for (int i = counter; i>=0; i--){
 				if (mensch.get_location_id()==zielloc && i == 0 && zielloc=='P'){
 					if (yPos_current<15){
 						if (location_ids.get(yPos_current+1).get(xPos_current).equals(String.valueOf(counter+1))) {			//unten gehts weiter
 							yPos_current++;
 							neuer_weg.push('o');
 						}
 					}
 					if (xPos_current<24){
 						if (location_ids.get(yPos_current).get(xPos_current+1).equals(String.valueOf(counter+1))) {			//rechts gehts weiter
 							xPos_current++;
 							neuer_weg.push('l');
 						}
 					}
 					if (yPos_current>0){
 						if (location_ids.get(yPos_current-1).get(xPos_current).equals(String.valueOf(counter+1))) {			//oben gehts weiter
 							yPos_current--;
 							neuer_weg.push('u');
 						}
 					}
 					if (xPos_current>0){
 						if (location_ids.get(yPos_current).get(xPos_current-1).equals(String.valueOf(counter+1))) {			//links gehts weiter
 							xPos_current--;
 							neuer_weg.push('r');
 						}
 					}
 				} else{
 					if (yPos_current<15){
 						if (location_ids.get(yPos_current+1).get(xPos_current).equals(String.valueOf(i))) {			//unten gehts weiter
 						yPos_current++;
 						neuer_weg.push('o');
 						}
 					}
 					if (xPos_current<24){
 						if (location_ids.get(yPos_current).get(xPos_current+1).equals(String.valueOf(i))) {			//rechts gehts weiter
 						xPos_current++;
 						neuer_weg.push('l');
 						}
 					}
 					if (yPos_current>0){
 						if (location_ids.get(yPos_current-1).get(xPos_current).equals(String.valueOf(i))) {			//oben gehts weiter
 						yPos_current--;
 						neuer_weg.push('u');
 						}
 					}
 					if (xPos_current>0){
 						if (location_ids.get(yPos_current).get(xPos_current-1).equals(String.valueOf(i))) {			//links gehts weiter
 						xPos_current--;
 						neuer_weg.push('r');
 						}
 					}
 				}
 			}
 		} else {
 			for (int i = 1; i<=counter+1; i++){
 				if (zielloc!='P'){
 					i--;
 				}
 				if (yPos_current<15){
 					if (location_ids.get(yPos_current+1).get(xPos_current).equals(String.valueOf(i))) {			//unten gehts weiter
 					yPos_current++;
 					neuer_weg.push('o');
 					}
 				}
 				if (xPos_current<24){
 					if (location_ids.get(yPos_current).get(xPos_current+1).equals(String.valueOf(i))) {			//rechts gehts weiter
 					xPos_current++;
 					neuer_weg.push('l');
 					}
 				}
 				if (yPos_current>0){
 					if (location_ids.get(yPos_current-1).get(xPos_current).equals(String.valueOf(i))) {			//oben gehts weiter
 					yPos_current--;
 					neuer_weg.push('u');
 					}
 				}
 				if (xPos_current>0){
 					if (location_ids.get(yPos_current).get(xPos_current-1).equals(String.valueOf(i))) {			//links gehts weiter
 					xPos_current--;
 					neuer_weg.push('r');
 					}
 				} 
 				if (zielloc!='P'){
 					i++;
 				}
 			}
 		}
 		
 		
 		return neuer_weg;
 	}
 
 	//Support Tiki
 	private Stack<Character>fuelle_stack_homeposition( Mensch mensch, int xPos_current, int yPos_current) {
 		Stack<Character> neuer_weg = new Stack<Character>();
 
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+1 == xPos_current){
 			neuer_weg.push('l');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)+2 == xPos_current){
 			neuer_weg.push('l');
 			neuer_weg.push('l');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)-1 == xPos_current){
 			neuer_weg.push('r');
 		}
 		if (((mensch.getHomePosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT)-2 == xPos_current){
 			neuer_weg.push('r');
 			neuer_weg.push('r');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)-1 == yPos_current){
 			neuer_weg.push('u');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)-2 == yPos_current){
 			neuer_weg.push('u');
 			neuer_weg.push('u');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+1 == yPos_current){
 			neuer_weg.push('o');
 		}
 		if (((mensch.getHomePosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT)+2 == yPos_current){
 			neuer_weg.push('o');
 			neuer_weg.push('o');
 		}
 				
 		return neuer_weg;
 	}
 
 	
 	//Support Tiki
 	private ArrayList<ArrayList<String>> wegberechnung_rasterkarte_initialisierung(ArrayList<ArrayList<String>> location_ids, String ziellocation, char locationId) {
 		for (int i=0; i<location_ids.size(); i++){
 			for (int j=0; j<location_ids.get(i).size(); j++){
 				if (location_ids.get(i).get(j).charAt(0) != 'X' && location_ids.get(i).get(j).charAt(0) != ziellocation.charAt(0) && location_ids.get(i).get(j).charAt(0) != 'P' && location_ids.get(i).get(j).charAt(0) != locationId ){
 					location_ids.get(i).set(j,"You shall not pass!") ;
 				}
 				if (!(ziellocation.equals("P") && locationId == 'P')){
 					if (location_ids.get(i).get(j).charAt(0) == ziellocation.charAt(0)){
 						location_ids.get(i).set(j,"Z") ;
 					}
 					if (location_ids.get(i).get(j).charAt(0) == locationId ){
 						location_ids.get(i).set(j,"X") ;
 					}
 				} else{
 					if (location_ids.get(i).get(j).charAt(0) == 'X'){
 						location_ids.get(i).set(j,"You shall not pass!") ;
 					}
 				}
 				if (location_ids.get(i).get(j).charAt(0) == 'P'){
 					location_ids.get(i).set(j,"X") ;
 				}
 				
 			}
 		}
 		return location_ids;
 	}
 	
 	
 	//Support Tiki
 	private void agentRumwuseln(int doTimes) {
 		Stack<Character> neuer_weg = new Stack<Character>();
 		ArrayList<ArrayList<String>> location_ids;
 		location_ids = Ressources.getLocation_ids();
 		
 		location_ids.get((agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"0");
 		
 		location_ids = wegberechnung_rasterkarte_initialisierung(location_ids, String.valueOf(get_agent().get_location_id()), get_agent().get_location_id());
 		
 		location_ids.get((agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT).set((agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT,"Z");
 		
 		for (int i=0; i<15; i++){
 			for (int j=0; j<24; j++){
 				if ((location_ids.get(i).get(j)=="Z" || location_ids.get(i).get(j)=="0") && location_ids.get(i+1).get(j)=="Z" && location_ids.get(i-1).get(j)=="Z" && location_ids.get(i).get(j+1)=="Z" && location_ids.get(i).get(j-1)=="Z"){
 					
 					//Nun luft der Agent lustig hin und her
 					for (int k=0; k<doTimes; k++){
 						neuer_weg.push('r');
 						neuer_weg.push('o');
 						neuer_weg.push('l');
 						neuer_weg.push('u');
 						neuer_weg.push('r');
 						neuer_weg.push('u');
 						neuer_weg.push('l');
 						neuer_weg.push('o');
 						neuer_weg.push('o');
 						neuer_weg.push('l');
 						neuer_weg.push('u');
 						neuer_weg.push('u');
 						neuer_weg.push('r');
 						neuer_weg.push('o');
 					}
 					
 					if ((this.agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT==j && (this.agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT==i+1){
 						neuer_weg.push('o');
 					} else{
 						if ((this.agent.getPosX()-Ressources.ZEROPOS.width)/Ressources.RASTERHEIGHT!=j || (this.agent.getPosY()-Ressources.ZEROPOS.height)/Ressources.RASTERHEIGHT!=i){
 							//Der Agent wird zum Mittelpunkt des Hauses geleitet
 							if (location_ids.get(i-2).get(j-1).equals("X") || location_ids.get(i-1).get(j-2).equals("X")){
 								neuer_weg.push('u');
 								neuer_weg.push('r');
 							}
 							if (location_ids.get(i-2).get(j).equals("X")){
 								neuer_weg.push('u');
 							}
 							if (location_ids.get(i-2).get(j+1).equals("X") || location_ids.get(i-1).get(j+2).equals("X")){
 								neuer_weg.push('u');
 								neuer_weg.push('l');
 							}
 							if (location_ids.get(i).get(j+2).equals("X")){
 								neuer_weg.push('l');
 							}
 							if (location_ids.get(i+1).get(j+2).equals("X") || location_ids.get(i+2).get(j+1).equals("X")){
 								neuer_weg.push('o');
 								neuer_weg.push('l');
 							}
 							if (location_ids.get(i+2).get(j).equals("X")){
 								neuer_weg.push('o');
 							}
 							if (location_ids.get(i+2).get(j-1).equals("X") || location_ids.get(i+1).get(j-2).equals("X") ){
 								neuer_weg.push('o');
 								neuer_weg.push('r');
 							}
 							if (location_ids.get(i).get(j-2).equals("X")){
 								neuer_weg.push('r');
 							}
 						} 
 					}					
 				}
 			}
 		}	
 		get_agent().setMoves(neuer_weg);
 	}
 	
 	
 	//Support Tiki
 	public void doSomethingAfterAgentAktion(){
 		int personId =0;
 		
 		if (!wieeeeschteAktion && get_agent().getMussWuseln() != "Park" && get_agent().getMussWuseln() != "Park+"){ 
 			if (get_agent().getMussWuseln().charAt(1) <='9' && get_agent().getMussWuseln().charAt(1)>='0'){
 				personId = Integer.parseInt(get_agent().getMussWuseln().substring(0,2));
 			} else{
 				personId = Integer.parseInt(get_agent().getMussWuseln().substring(0,1));
 			}
 		}
 		
 		
 		//Spionage
 		if(get_agent().getMussWuseln().equals("Wanze+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().add("Wanze");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert((float)(Math.random()*17.5f+1)+22.5f,0);
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Wanze")){
 			agentRumwuseln(2);
 			get_agent().setMussWuseln("Wanze+");
 		} 
 		
 		if(get_agent().getMussWuseln().equals("Kamera+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().add("Kamera");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert((float)(Math.random()*10+1)+35,1);
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Kamera")){
 			agentRumwuseln(3);
 			get_agent().setMussWuseln("Kamera+");
 		}
 		
 		if(get_agent().getMussWuseln().equals("Hacken+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().add("Hacken");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert((float)(Math.random()*20+1)+15,2);
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Hacken")){
 			agentRumwuseln(2);
 			get_agent().setMussWuseln("Hacken+");
 		}
 		
 		
 //		if(get_agent().getMussWuseln().equals("Park+") && get_agent().getCurrentMove()=='n'){
 //			berechne_weg(null, agent, 'P');
 //			calcMisstrauenAfterBeschwichtigenInPark();
 //		}
 		if(get_agent().getMussWuseln().equals("Park")){
 			berechne_weg(agent, 'P');
 			calcMisstrauenAfterBeschwichtigenInPark();
 		} 		
 		
 		
 		
 		//Spionage entfernen
 		
 		if(get_agent().getMussWuseln().equals("Wanzer+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().remove("Wanze");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert(0,0);			
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Wanzer")){
 			agentRumwuseln(2);
 			get_agent().setMussWuseln("Wanzer+");
 		}
 		
 		if(get_agent().getMussWuseln().equals("Kamerar+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().remove("Kamera");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert(0,1);			
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Kamerar")){
 			agentRumwuseln(3);
 			get_agent().setMussWuseln("Kamerar+");
 		}
 		
 		if(get_agent().getMussWuseln().equals("Hackenr+") && get_agent().getCurrentMove()=='n'){
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).getUeberwachungsmodule().remove("Hacken");
 			getHouses().get((int)(get_agent().get_location_id()-48-1)).setUeberwachungsWert(0,2);			
 			get_agent().setMussWuseln("");
 		}
 		if(get_agent().getMussWuseln().equals("Hackenr")){
 			agentRumwuseln(2);
 			get_agent().setMussWuseln("Hackenr+");
 		}
 		
 			
 		
 		
 		
 		//Soziales
 		if(get_agent().getMussWuseln().length()>=8 && personId <=9 &&  get_agent().getMussWuseln().substring(1,8).equals("Kuchen+") ||
 				get_agent().getMussWuseln().length()>=9 && personId >9 &&  get_agent().getMussWuseln().substring(2,9).equals("Kuchen+")){
 			calc_misstrauen_after_beschwichtigen_action(0, get_people().get(personId));
 			get_people().get(personId).erhoehe_durchgefuehrteBeschwichtigungen(0);
 			get_people().get(personId).setMoves(new Stack());
 			get_agent().setMussWuseln("");
 		}	
 		if(get_agent().getMussWuseln().length()>=7 && personId <=9 && get_agent().getMussWuseln().substring(1,7).equals("Kuchen") ||
 				get_agent().getMussWuseln().length()>=8 && personId >9 && get_agent().getMussWuseln().substring(2,8).equals("Kuchen") ){
 			get_agent().setMussWuseln(personId+"Kuchen+");
 			//wuseln 
 		}	
 		
 		if(get_agent().getMussWuseln().length()>=13 && personId <=9 &&  get_agent().getMussWuseln().substring(1,13).equals("Unterhalten+") ||
 				get_agent().getMussWuseln().length()>=14 && personId >9 &&  get_agent().getMussWuseln().substring(2,14).equals("Unterhalten+")){
 			calc_misstrauen_after_beschwichtigen_action(1, get_people().get(personId));
 			get_people().get(personId).erhoehe_durchgefuehrteBeschwichtigungen(1);
 			get_people().get(personId).setMoves(new Stack());
 			get_agent().setMussWuseln("");
 		}	
 		if(get_agent().getMussWuseln().length()>=12 && personId <=9 && get_agent().getMussWuseln().substring(1,12).equals("Unterhalten") ||
 				get_agent().getMussWuseln().length()>=13 && personId >9 && get_agent().getMussWuseln().substring(2,13).equals("Unterhalten") ){
 			get_agent().setMussWuseln(personId+"Unterhalten+");
 			//wuseln 
 		}
 		
 		if(get_agent().getMussWuseln().length()>=9 && personId <=9 &&  get_agent().getMussWuseln().substring(1,9).equals("Flirten+") ||
 				get_agent().getMussWuseln().length()>=10 && personId >9 &&  get_agent().getMussWuseln().substring(2,10).equals("Flirten+")){
 			calc_misstrauen_after_beschwichtigen_action(2, get_people().get(personId));
 			get_people().get(personId).erhoehe_durchgefuehrteBeschwichtigungen(2);
 			get_people().get(personId).setMoves(new Stack());
 			get_agent().setMussWuseln("");
 		}	
 		if(get_agent().getMussWuseln().length()>=8 && personId <=9 && get_agent().getMussWuseln().substring(1,8).equals("Flirten") ||
 				get_agent().getMussWuseln().length()>=9 && personId >9 && get_agent().getMussWuseln().substring(2,9).equals("Flirten") ){
 			get_agent().setMussWuseln(personId+"Flirten+");
 			//wuseln 
 		}
 		
 		if(get_agent().getMussWuseln().length()>=6 && personId <=9 &&  get_agent().getMussWuseln().substring(1,6).equals("Hand+") ||
 				get_agent().getMussWuseln().length()>=7 && personId >9 &&  get_agent().getMussWuseln().substring(2,7).equals("Hand+")){
 			calc_misstrauen_after_beschwichtigen_action(3, get_people().get(personId));
 			get_people().get(personId).erhoehe_durchgefuehrteBeschwichtigungen(3);
 			get_people().get(personId).setMoves(new Stack());
 			get_agent().setMussWuseln("");
 		}	
 		if(get_agent().getMussWuseln().length()>=5 && personId <=9 && get_agent().getMussWuseln().substring(1,5).equals("Hand") ||
 				get_agent().getMussWuseln().length()>=6 && personId >9 && get_agent().getMussWuseln().substring(2,6).equals("Hand") ){
 			get_agent().setMussWuseln(personId+"Hand+");
 			//wuseln 
 		}
 				
 	}
 	
 	
 	//Support Tiki
 	private void calcColouredPeople (int hausid){
 		for (int i = 0; i<get_people().size(); i++){
 			if (get_people().get(i).get_haus_id() == hausid){
 				get_people().get(i).setIstFarbig(true);
 				get_people().get(i).farbeZeigen(true);
 			}
 		}
 	}
 	
 	
 
 	//Support Tiki
 	public boolean calcGameOver(){
 		if (calc_misstrauen_in_street()>=90.0){
 			return true;
 		}
 		for (int i = 0; i < this.people.size(); i++){
 			if (this.people.get(i) instanceof Terrorist){
 				if (this.people.get(i).get_misstrauen() >= 85.00){
 					return true;
 				}
 			} 
 		}	
 		return false;
 	}
 	
 	
 	
 	public ArrayList<Person> get_people(){
 		return people;
 	}
 	
 	public void set_people(Person p){
 		people.add(p);
 	}
 	
 	
 	public Agent get_agent(){
 		return agent;
 	}
 	
 	public void set_agent(Agent agent){
 		this.agent = agent;
 	}
 
 
 	public int getSpiel_tag() {
 		return spiel_tag;
 	} 
 
 
 	public int getSpiel_stunde() {
 		return spiel_stunde;
 	}
 
 
 	public int getSpiel_minute() {
 		return spiel_minute;
 	}
 
 	
 	public String getSpielzeit_as_string() {
 		String zeit="";
 		zeit = String.valueOf(this.spiel_stunde);
 		if (this.spiel_stunde <=9){
 			zeit = "0" + zeit;
 		}
 		zeit = zeit + ":";
 		if (this.spiel_minute <=9){
 			zeit = zeit + "0";
 		}
 		zeit = zeit + String.valueOf(this.spiel_minute);
 		
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
 
 
 	public void setWieeeeschteAktion(boolean wieeeeschteAktion) { 
 		this.wieeeeschteAktion = wieeeeschteAktion;
 	}
 
 	
 	
 	public void calcMisstrauenMax(){
 		double misstrauen = this.calc_misstrauen_in_street();
 		if(this.misstrauen_max < misstrauen){
 			this.misstrauen_max = misstrauen;
 		}
 	}
 	
 	public double getMisstrauenMax(){
 		return this.misstrauen_max;
 	}
 	
 	public boolean isWieeeeschteAktion(){
 		return this.wieeeeschteAktion;
 	}
 }
