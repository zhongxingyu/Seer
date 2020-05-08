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
 					if(SpielerAktiv.Steine[i].getPosition().equals(position) == false ){
 					korrekt = true;
 					}
 					
 					else{
 					return false;
 					}
 				}
 				
 				for (int i = 0; i< SpielerPassiv.getAnzahlZuege(); i++)
 				{
 					if(SpielerPassiv.Steine[i].getPosition().equals(position)  == false){
 					korrekt = true;
 					}
 					else{
 					return false;
 					}
 				
 				}
 		
 		return korrekt;
 	}
 	
 	// berprft, ob ein Spielzug regelkonform ist
 	// gibt true zurck, wenn der Zug korrekt ist
 	public boolean checkZug (Bewegung bewegung, Spieler SpielerAktiv, Spieler SpielerPassiv ){
 	
 		
 	boolean korrekt = false;
 	int aenderung = 0;
 	int vonEbene, vonX, vonY, nachEbene, nachX, nachY;
 	
 	// Ablegen der Positionsindexe in einer int-Variablen
 	vonEbene = bewegung.getVon().getEbene().getValue();
 	vonX = bewegung.getVon().getX().getValue();
	vonY = bewegung.getVon().getX().getValue();
 	nachEbene= bewegung.getNach().getEbene().getValue();
 	nachX = bewegung.getNach().getX().getValue();
 	nachY = bewegung.getNach().getY().getValue();
 		
 		
 	int AnzahlSteine = SpielerAktiv.getAnzahlSteine();
 	
 	// Wenn Anzahl Steine > 3 darf der Spieler ziehen, Wenn =3 darf er springen
 	if (AnzahlSteine > 3)
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
 			if(SpielerAktiv.Steine[i].getPosition().equals(bewegung.getNach()) == false ){
 			korrekt = true;
 			}
 			
 			else{
 			return false;
 			}
 			
 			if(SpielerPassiv.Steine[i].getPosition().equals(bewegung.getNach()) == false){
 			korrekt = true;
 			}
 			else{
 			return false;
 			}
 		
 		}
 			
 			return korrekt;
 		
 	}
 	
 	//berprft, ob sich ein Stein auf dem Spielfeld in einer Mhle befindet
 	// Gibt true zurck, wenn sich der abgefragte Stein(int IndexStein) in einer Mhle befindet
 	public boolean checkInMuehle(int IndexStein, Spielstein[] Steine){ 
 
 		
 		boolean inMuehle = false;
 		int aenderung1, aenderung2, aenderung3;
 		int zaehler = 0;
 		
 		int[][] Positionen = new int[9][3];
 		
 		// Ablegen der Positionen aller Steine eines Spielers in einem Array
 		for (int i = 0; i < 9; i++){
 			if(Steine[i] != null){
 				Positionen[i][0]= Steine[i].getPosition().getEbene().getValue();
 				Positionen[i][1]= Steine[i].getPosition().getX().getValue();
 				Positionen[i][2]= Steine[i].getPosition().getY().getValue();
 				}
 			}
 		
 		for (int i =0; i <9; i++){
 			
 			aenderung1 = 0;
 			aenderung2 = 0;
 			aenderung3 = 0;
 			
 			aenderung1 = Math.abs(Positionen[i][0] - Positionen[IndexStein][0]);
 			aenderung2 = Math.abs(Positionen[i][1] - Positionen[IndexStein][1]);
 			aenderung3 = Math.abs(Positionen[i][2] - Positionen[IndexStein][2]);				
 			
 			if(	(aenderung1 == 0 && aenderung2 == 0 && aenderung3 == 1)||
 				(aenderung1 == 0 && aenderung2 == 1 && aenderung3 == 0)||
 				(aenderung1 == 1 && aenderung2 == 0 && aenderung3 == 0)||
 				(aenderung1 == 0 && aenderung2 == 0 && aenderung3 == 2)||
 				(aenderung1 == 0 && aenderung2 == 2 && aenderung3 == 0)||
 				(aenderung1 == 2 && aenderung2 == 0 && aenderung3 == 0)){
 			
 				zaehler ++;
 			}
 			
 		}	
 		
 		// Ausschliesen der 4 Sonderflle, bei denen sich der Index um 1 verndert, der Stein jedoch nicht in einer Mhle steht
 		//Sonderflle: 1,1,3 - 2,1,3 - 3,1,3 ; 1,3,3 - 2,3,3 - 3,3,3 ; 1,3,1 - 2,3,1 - 3,3,1 ; 1,1,1 - 2,1,1 - 3,1,1		
 		
 		Position[] pos = new Position[12];
 		pos[0] = new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Drei);
 		pos[1] = new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Drei);
 		pos[2] = new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Drei);
 		pos[3] = new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Drei);
 		pos[4] = new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Drei);
 		pos[5] = new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Drei);
 		pos[6] = new Position(EPositionIndex.Eins, EPositionIndex.Drei, EPositionIndex.Eins);
 		pos[7] = new Position(EPositionIndex.Zwei, EPositionIndex.Drei, EPositionIndex.Eins);
 		pos[8] = new Position(EPositionIndex.Drei, EPositionIndex.Drei, EPositionIndex.Eins);
 		pos[9] = new Position(EPositionIndex.Eins, EPositionIndex.Eins, EPositionIndex.Eins);
 		pos[10] = new Position(EPositionIndex.Zwei, EPositionIndex.Eins, EPositionIndex.Eins);
 		pos[11] = new Position(EPositionIndex.Drei, EPositionIndex.Eins, EPositionIndex.Eins);
 		
 		int anzahl1 = 0;
 		int anzahl2 = 0;
 		int anzahl3 = 0;
 		int anzahl4 = 0;
 		
 		for(int j = 0; j <9 ; j++){
 			if(Steine[j] != null){
 				if(Steine[j].getPosition().equals(pos[0]))
 					anzahl1++;
 				if(Steine[j].getPosition().equals(pos[1]))
 					anzahl1++;
 				if(Steine[j].getPosition().equals(pos[2]))
 					anzahl1++;
 				if(Steine[j].getPosition().equals(pos[3]))
 					anzahl2++;
 				if(Steine[j].getPosition().equals(pos[4]))
 					anzahl2++;
 				if(Steine[j].getPosition().equals(pos[5]))
 					anzahl2++;
 				if(Steine[j].getPosition().equals(pos[6]))
 					anzahl3++;
 				if(Steine[j].getPosition().equals(pos[7]))
 					anzahl3++;
 				if(Steine[j].getPosition().equals(pos[8]))
 					anzahl3++;
 				if(Steine[j].getPosition().equals(pos[9]))
 					anzahl3++;
 				if(Steine[j].getPosition().equals(pos[10]))
 					anzahl4++;
 				if(Steine[j].getPosition().equals(pos[11]))
 					anzahl4++;
 			}
 		}
 			
 		//Wenn anzahl1/2/3/4 == 3 ist Fall vorhanden und zaehler wird um 2 vermindert
 		if(anzahl1 == 3)
 			zaehler = zaehler - 2;
 		if(anzahl2 == 3)
 			zaehler = zaehler - 2;
 		if(anzahl3 == 3)
 			zaehler = zaehler - 2;
 		if(anzahl4 == 3)
 			zaehler = zaehler - 2;
 		
 		if(zaehler == 2 || zaehler == 4){
 				inMuehle = true;
 			}
 			
 			// Ausschliesen der 4 Sonderflle, bei denen sich der Index um 1 verndert, der Stein jedoch nicht in einer Mhle steht
 			//Sonderflle: 1,1,3 - 2,1,3 - 3,1,3 ; 1,3,3 - 2,3,3 - 3,3,3 ; 1,3,1 - 2,3,1 - 3,3,1 ; 1,1,1 - 2,1,1 - 3,1,1		
 			
 			
 		
 		return inMuehle;
 	}
 
 	// berprft, ob das Spiel beendet ist, weil SpielerAktiv keine Mglichkeit mehr hat zu ziehen, oder weniger als 3 Steine hat
 	// Gibt true zurck, wenn das Spiel beendet ist und false wenn das Spiel noch nicht beendet ist
 	public boolean checkSpielBeendet(Spieler SpielerAktiv, Spieler SpielerPassiv){
 		
 		boolean SpielBeendet = true;
 		boolean ZugKorrekt = false;
 		
 		EPositionIndex ebene = null;
 		EPositionIndex x = null;
 		EPositionIndex y = null;
 		
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
 	
 }
 
