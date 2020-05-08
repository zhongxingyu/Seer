 
 
 /**
  * Klassenkommentar:
  * Hauptspiellogik bzw. was tun wenn was passiert
  * 
  */
 public class Aktion{
 
 private static int figurX;
 private static int figurY;
 private static int aktuellesLevel = 0;
 private static int aktuellerRaum =0;
 private static int newFigurX;
 private static int newFigurY;
 
 static boolean reachedCheckpoint;
 static int checkpointMerkeLevel;
 static int checkpointMerkeRaum;
 static boolean storyteller;
 
 private static final String WEISSIMG = "Images\\weiss.jpg";
 
 
 /**
  * Methode bewegt Figur anhand von Koordinaten
  * 
  */
 	public void figurBewegen(int richtung){
 		
 		
 		if (richtung == Main.RECHTS){ 
 			newFigurX=figurX+1;
 			newFigurY=figurY;
 		}	   
 		else if (richtung == Main.UNTEN){ 
 			newFigurX=figurX;
 			newFigurY=figurY-1;
 		}
 		else if (richtung == Main.LINKS){ 
 			newFigurX=figurX-1;
 			newFigurY=figurY;
 		}
 		else if (richtung == Main.OBEN){ 
 			newFigurX=figurX;
 			newFigurY=figurY+1;
 		}
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.BODEN)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FIGUR)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==13) //13 und 14 sind Hilfselemente um an bestimmte Punkte zurueck zu kehren
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==14)){ //13: X steht in level.txt fuer die Stelle neben dem Ziel
 													                                                //14: Y steht in level.txt fuer die Stelle neben dem Checkpoint
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);
 			figurX=newFigurX;
 			figurY=newFigurY;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.START)
 				&((aktuellerRaum!=0)|(aktuellesLevel!=0))){
 		
 			if (aktuellerRaum>0){
 				aktuellerRaum--;
 			}
 			else if(aktuellesLevel>0){
 				aktuellesLevel--;
 				aktuellerRaum=2;
 			}
 			Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 			Menu.figurZumZiel(aktuellesLevel,aktuellerRaum);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.ZIEL){
 			if (aktuellerRaum<2){
 				aktuellerRaum++;
 			}
 			else {
 				aktuellesLevel++;
 				aktuellerRaum=0;
 			}
 			Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 			Menu.figurReset(aktuellesLevel,aktuellerRaum, figurX, figurY);
 			StdDraw.picture(400,560,WEISSIMG);
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FALLE)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MOB)){
 			
 			Figur.schadenBekommen(1);
 						
 		}	
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.SIEG){
 			aktuellesLevel=0;
 			aktuellerRaum=0;
 			Menu.sieg();
 		}
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.CHECKPOINT){
 			reachedCheckpoint=true;
 			checkpointMerkeLevel=aktuellesLevel;
 			checkpointMerkeRaum=aktuellerRaum;
 			Menu.displayPlayerStats();
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);
 			figurX=newFigurX;
 			figurY=newFigurY;
 			
 			
 			
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.STORYTELLER){
 			Menu.storyteller();
 			//Main.storytellerBool(false);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MUENZEN){
 			Figur.muenzenSammeln(1);
 			Spielfeld.wertSetzenBeiXY(aktuellesLevel, aktuellerRaum, newFigurX, newFigurY, Main.BODEN);
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);	
 			
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.HPTRANKSHOP){
 			Figur.shopHP(1);
 			Figur.muenzenVerlieren(1);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MANATRANKSHOP){
 			Figur.shopMana(1);
 			Figur.muenzenVerlieren(1);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEGELB){
 			Figur.setFarbe(Main.GELB);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEROT){
 			Figur.setFarbe(Main.ROT);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.FARBEBLAU){
 			Figur.setFarbe(Main.BLAU);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.HPTRANK){
 			Figur.heilen(1);
 			Spielfeld.wertSetzenBeiXY(aktuellesLevel, aktuellerRaum, newFigurX, newFigurY, Main.BODEN);
			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);	
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,aktuellerRaum,newFigurX,newFigurY)==Main.MANATRANK){
 			Figur.manaReg(1);
 			Spielfeld.wertSetzenBeiXY(aktuellesLevel, aktuellerRaum, newFigurX, newFigurY, Main.BODEN);
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,newFigurX,newFigurY);	
 		}
 
 
 	}
 
 
 
 	
 	
 	
 
 /**
  * Methode setzt Figur
  * 
  */
 public static void setFigurXY(int x, int y){
 	figurX=x;
 	figurY=y;		
 }
 /**
  * 
  * Methode setzt das aktuelle Level
  * 
  */
 public static void setLevel(int level){
 	aktuellesLevel = level;
 }
 
 /**
  * 
  * Methode setzt den aktuellen Raum
  * 
  */
 public static void setRaum(int raum){
 	aktuellerRaum = raum;
 }
 
 /**
  * 
  * Methode liefert X-Wert der Figur zurueck
  * 
  */
 public static int getFigurX(){
 	return figurX;
 }
 
 /**
  * 
  * Methode liefert Y-Wert der Figur zurueck
  * 
  */
 public static int getFigurY(){
 	return figurY;
 }
 
 /**
  * 
  * Methode liefert Level zurueck
  * 
  */
 public static int getLevel(){
 	return aktuellesLevel;
 }
 
 /**
  * 
  * Methode liefert Raum zurueck
  * 
  */
 public static int getRaum(){
 	return aktuellerRaum;
 }
 
 /**
  * 
  * Methode setzt Figur zum Checkpoint
  * 
  */
 public static void zumCheckpoint(){
 	if (reachedCheckpoint == true){
 		Figur.resetHP();
 		aktuellesLevel=checkpointMerkeLevel;
 		aktuellerRaum=checkpointMerkeRaum;
 		Menu.levelDarstellen(aktuellesLevel,aktuellerRaum);
 		Menu.figurZumCheckpoint(aktuellesLevel,aktuellerRaum);
 		reachedCheckpoint=false;
 		Menu.displayPlayerStats();
 	}
 	else{
 		Figur.resetHP();
 		aktuellerRaum=0;
 		Menu.levelDarstellen(aktuellesLevel, aktuellerRaum);
 	}
 }
 
 }
 
