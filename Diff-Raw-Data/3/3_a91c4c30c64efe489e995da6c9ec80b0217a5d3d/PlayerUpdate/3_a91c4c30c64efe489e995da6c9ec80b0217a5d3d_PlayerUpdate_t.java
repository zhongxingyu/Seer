 package player;
 
 import prototype.Map;
 
 class PlayerUpdate {
 	
 	private static boolean isAlive = true;				// Kontrollwert of Spieler lebt
 	private static final float manaRegenaration = 40;	// Manaeregenerationsrate
 	private static long timeOfDeath = 0;				// Teitpunkt des letzten Todes
 	
 	private static boolean  bCheck = true;				// Debug einstellung fuer Kollsionen und Gegnerschaden
 	
 	static void update(float frametime){
 		
 		if(!isAlive&&(System.currentTimeMillis()-timeOfDeath)>3000)respawn();	// autom. Wiederbeleben des Spielers
 		if(!isAlive)return;														// Update Abruch falls Spieler tot ist
 		
 		if(bCheck){
 			Bewegen.bewegen(frametime);			// Bewegen des Spielers
 			Kollision.kollision();				// Interaktion Spieler <--> Map
 			Schiessen.schussGen(frametime);		// Schiesen
 			GegnerUpdate.gegnerKolision();		// Interaktion Gegner <--> Spieler
 		}
 			
 		if(Player.getF_mana()<1000)Player.setF_mana(Player.getF_mana()+manaRegenaration*frametime); // Verbrauchtes Mana wiederherstellen
 		if(Player.getF_leben()<=0)spielerTot();														// Falls Spieler 0 Hp Tod abhandeln
		if(Player.getF_mana()>1000)Player.setF_mana(1000); //Falls Leben oder Mana das Maximum ueberschreiten
		if(Player.getF_leben()>1000)Player.setF_leben(1000);
 	}
 	
 	private static void spielerTot()
 	{
 		int gegneranzahl;
 		
 		isAlive = false;							// Kontrollwert auf tot setzen
 		Player.getMap().setSpielerTod(true);		// Map ueber tod informieren
 		timeOfDeath = System.currentTimeMillis();	// Todeszeitpunkt festellen
 		gegneranzahl=Player.getEnemys().size();		// Festellung der Gegnerzahl
 		for(int i = 0; i<gegneranzahl; i++){		
 		Player.getEnemys().remove(0);}				// Gegner loeschen
 					
 	}
 	
 	static void respawn(){
 		isAlive = true;								// Kontrollwert auf leben setzen
 		Player.getMap().setSpielerTod(false);		// Map ueber Auferstehung informieren
 		Player.setF_leben(1000);					// Leben wiederhestellen
 		Player.setF_mana(1000);						// Mana wiederherstellen
 		Map.resetMap = true;						// Map neuladen
 	    }
 
 	// DEBUG Schalter
 	
 	static void setbCheck(boolean bCheck) {
 		PlayerUpdate.bCheck = bCheck;
 	}
 	
 }
