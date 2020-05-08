 package de.dhbw.muehle_spiel;
 
 import de.dhbw.muehle_api.EPositionIndex;
 import de.dhbw.muehle_api.Position;
 
 public class Pruefung {
 
 	// berprft, ob das Setzen eines Steins regelkonform ist
 	public boolean checkSetzen (Position position, Spieler SpielerAktiv, Spieler SpielerPassiv ){
 		
 		boolean korrekt = true;
 				
 		//berprfung, ob die Position bereits belegt ist
 				for (int i = 0; i< SpielerAktiv.getAnzahlZuege(); i++)
 				{
 					if(SpielerAktiv.Steine[i] != null){
 						if(SpielerAktiv.Steine[i].getPosition().equals(position) == false ){
 							korrekt = true;
 						}
 					
 						else{
 						return false;
 						}
 					}	
 				}
 				
 				for (int i = 0; i< SpielerPassiv.getAnzahlZuege(); i++)
 				{
 					if(SpielerPassiv.Steine[i] != null){
 						if(SpielerPassiv.Steine[i].getPosition().equals(position)  == false){
 							korrekt = true;
 						}
 						else{
 							return false;
 						}
 					}
 				}
 		
 		return korrekt;
 	}
 	
 	// berprft, ob ein Spielzug regelkonform ist
 	// gibt true zurck, wenn der Zug korrekt ist
 	public boolean checkZug (Bewegung bewegung, Spieler SpielerAktiv, Spieler SpielerPassiv ){
 	
 		
	boolean korrekt = true;			//test marvin
 	int aenderung = 0;
 	int vonEbene, vonX, vonY, nachEbene, nachX, nachY;
 	
 	// Ablegen der Positionsindexe in einer int-Variablen
 	vonEbene = bewegung.getVon().getEbene().getValue();
 	vonX = bewegung.getVon().getX().getValue();
 	vonY = bewegung.getVon().getY().getValue();
 	nachEbene= bewegung.getNach().getEbene().getValue();
 	nachX = bewegung.getNach().getX().getValue();
 	nachY = bewegung.getNach().getY().getValue();
 	
 	// Wenn Anzahl Steine > 3 darf der Spieler ziehen, Wenn =3 darf er springen
 	if (SpielerAktiv.getAnzahlSteine() > 3)
 	{	
 		// berprfung, ob sich der PositionsIndex um 1 verndert hat
 		// Zug ist gltig, wenn aenderung = 1
 		if (vonEbene- nachEbene == 1 || vonEbene- nachEbene == -1)
 			aenderung ++;
 		if (vonEbene- nachEbene == 2 || vonEbene- nachEbene == -2)
 			return false;
 		if (vonX - nachX == 1 || vonX - nachX == -1) 
 			aenderung++;
 		if (vonX - nachX == 2 || vonX - nachX == -2) 
 			return false;
 		if (vonY - nachY == 1 || vonY - nachY == -1) 
 			aenderung++;
 		if (vonY - nachY == 2 || vonY - nachY == -2) 
 			return false;
 		if (aenderung == 1)
 			korrekt = true;
 		
 		//berprfung, ob sich die Ebene ungltiger Weise verndert hat
 		if (vonX == 1 && vonY ==3 && vonEbene != nachEbene)
 			return false;
 		if (vonX == 3 && vonY ==3 && vonEbene != nachEbene)
 			return false;
 		if (vonX == 3 && vonY ==1 && vonEbene != nachEbene)
 			return false;
 		if (vonX == 1 && vonY ==1 && vonEbene != nachEbene)
 			return false;
 		
 	}
 		
 	
 	//berprfung, ob die Nach-Position bereits belegt ist
 		for (int i = 0; i<9; i++)
 		{
 			if(SpielerAktiv.Steine[i] != null){
 				if(SpielerAktiv.Steine[i].getPosition().equals(bewegung.getNach()) == true ){
 					return false;
 				}
 			
 		}
 			
 			if(SpielerPassiv.Steine[i] != null){
 				if(SpielerPassiv.Steine[i].getPosition().equals(bewegung.getNach()) == true){
 					return false;
 				}
 				
 			}
 		
 		}
 			
 			return korrekt;
 		
 	}
 	
 	//berprft, ob sich ein Stein auf dem Spielfeld in einer Mhle befindet
 	// Gibt true zurck, wenn sich der abgefragte Stein(int IndexStein) in einer Mhle befindet
 	public boolean checkInMuehle(int IndexStein, Spielstein[] Steine){ 
 
 		
 		boolean inMuehle = false;
 		int aenderungEbene, aenderungX, aenderungY;
 		int zaehlerEbene = 0;
 		int zaehlerX = 0; 
 		int zaehlerY = 0;
 		
 		int[][] Positionen = new int[9][3];
 		
 		// Ablegen der Positionen aller Steine eines Spielers in einem Array
 		for (int i = 0; i < 9; i++){
 			if(Steine[i] != null){
 				Positionen[i][0]= Steine[i].getPosition().getEbene().getValue();
 				Positionen[i][1]= Steine[i].getPosition().getX().getValue();
 				Positionen[i][2]= Steine[i].getPosition().getY().getValue();
 				}
 		}
 		
 		for (int j =0; j <9; j++){
 			
 			aenderungEbene = 0;
 			aenderungX = 0;
 			aenderungY = 0;
 			
 			//Vergleicht die Position des betrachteten Steins(IndexStein) mit der des Steins an j-ter Stelle
 			aenderungEbene = Math.abs(Positionen[j][0] - Positionen[IndexStein][0]);
 			aenderungX = Math.abs(Positionen[j][1] - Positionen[IndexStein][1]);
 			aenderungY = Math.abs(Positionen[j][2] - Positionen[IndexStein][2]);
 					
 			//Heraufsetzen des Zhlers um 1, wenn sich nur die Ebene verndert hat
 			if ((aenderungEbene == 1 && aenderungX == 0 && aenderungY == 0)||
 				(aenderungEbene == 2 && aenderungX == 0 && aenderungY == 0))
 			{
 				zaehlerEbene ++;
 				
 				
 				// Ausschlieen der Eckpositionen
 				//Sonderflle: 1,1,3 - 2,1,3 - 3,1,3 ; 1,3,3 - 2,3,3 - 3,3,3 ; 1,3,1 - 2,3,1 - 3,3,1 ; 1,1,1 - 2,1,1 - 3,1,1
 				if	((Positionen[j][0] != Positionen[IndexStein][0])&&
 								((Positionen[j][1] + Positionen[j][2] == 2)||
 								(Positionen[j][1] + Positionen[j][2] == 4)||
 								(Positionen[j][1] + Positionen[j][2] == 6)))
 				{
 					zaehlerEbene--;
 				}
 											
 			}	
 			
 			//Heraufsetzen des Zhlers um 1, wenn sich nur die X-Koordinate verndert hat
 			if ((aenderungEbene == 0 && aenderungX == 1 && aenderungY == 0)||
 				(aenderungEbene == 0 && aenderungX == 2 && aenderungY == 0))
 				{
 					zaehlerX ++;
 				}
 			
 			//Heraufsetzen des Zhlers um 1, wenn sich nur die Y-Koordinate verndert hat
 			if ((aenderungEbene == 0 && aenderungX == 0 && aenderungY == 1)||
 				(aenderungEbene == 0 && aenderungX == 0 && aenderungY == 2))
 					{
 						zaehlerY ++;
 					}
 			
 		}
 
 		if(zaehlerEbene == 2 || zaehlerX == 2 || zaehlerY == 2){
 				inMuehle = true;
 			}
 					
 		return inMuehle;
 	}
 		// 
 				
 	// berprft, ob das Spiel beendet ist, weil SpielerAktiv keine Mglichkeit mehr hat zu ziehen, oder weniger als 3 Steine hat
 	// Gibt true zurck, wenn das Spiel beendet ist und false wenn das Spiel noch nicht beendet ist
 	public boolean checkSpielBeendet(Spieler SpielerAktiv, Spieler SpielerPassiv){
 		
 		boolean SpielBeendet = true;
 		boolean ZugKorrekt = false;
 		
 		EPositionIndex ebene = null;
 		EPositionIndex x = null;
 		EPositionIndex y = null;
 		
 		
 		// Wenn Anzahl der Steine > 3 wird berprft, ob der Aktive Spieler noch die Mglichkeit hat zu ziehen
 		if(SpielerAktiv.getAnzahlSteine() > 3)
 		{
 		for (int i = 0; i <9 ; i++){
 			
 			for(int a = 1 ; a <= 3; a++)
 				{
 					if(a == 1){
 						ebene = ebene.Eins;
 						}
 					if(a == 2){
 						ebene = ebene.Zwei;
 						}
 					if(a == 3){
 						ebene = ebene.Drei;
 					}
 					
 					for(int b = 1; b <= 3; b++)
 					{
 							if(b == 1){
 								x = x.Eins;
 								}
 							if(b == 2){
 								x = x.Zwei;
 								}
 							if(b == 3){
 								x = x.Drei;
 								
 							for(int c = 1; c <= 3; c++)
 								{
 									if(c == 1){
 										y = y.Eins;
 										}
 									if(c == 2){
 										y = y.Zwei;
 										}
 									if(c == 3){
 										y = y.Drei;
 									
 									ZugKorrekt = checkZug(new Bewegung(SpielerAktiv.Steine[i].getPosition(), new Position(ebene, x, y)), 
 													SpielerAktiv, SpielerPassiv);
 									
 									if(ZugKorrekt == true){
 										return false;
 									}
 									
 								}
 							}
 						}
 					}
 			 	
 				}
 			}
 		}
 		else
 		{
 			return true;
 		}
 			
 	return SpielBeendet;
 	}
 
 	// berprft, ob eine Position auf dem Spielfeld bereits besetzt ist
 	// Gibt true zurck, wenn die Position besetzt ist und false wenn die Position frei ist
 	public boolean checkFeldBesetzt(Position Position, Spieler Spieler1, Spieler Spieler2){
 		
 		boolean korrekt = false;
 		
 		for (int i = 0; i<9; i++)
 		{
 			if(Spieler1.Steine[i].getPosition().equals(Position) == false ){
 			korrekt = false;
 			}
 			
 			else{
 			return true;
 			}
 			
 			if(Spieler2.Steine[i].getPosition().equals(Position) == false){
 			korrekt = false;
 			}
 			else{
 			return true;
 			}
 		}
 		return korrekt;
 	}
 	
 	//berprft ob alle Steine des Gegners in einer Mhle stehen wenn man selber eine Mhle hat, damit man dann wieder einen Stein lschen kann
 	//gibt true zurck wenn alle Steine des Gegners in einer Mhle sind und false wenn nicht
 	public boolean checkAlleSteineInMuehle(Spieler Spieler1, Spieler Spieler2)
 	{
 		int counter = 0;
 		for (int i = 0; i<9; i++)
 		{
 			if(this.checkInMuehle(i, Spieler2.Steine) )
 				counter++;
 		}
 		if(Spieler2.getAnzahlSteine() == counter)
 		{
 			return true;
 		}
 		else 
 			return false;
 			
 	}
 	
 }
 
