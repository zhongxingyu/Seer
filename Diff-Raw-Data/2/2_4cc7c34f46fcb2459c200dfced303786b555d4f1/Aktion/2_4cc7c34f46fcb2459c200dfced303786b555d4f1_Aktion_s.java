 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 /**
  * Klassenkommentar:
  * Hauptspiellogik bzw. was tun wenn was passiert
  * 
  */
 public class Aktion{
 
 private static int figurX;
 private static int figurY;
 private static int aktuellesLevel = 0;
 
 
 private static final int BODEN = 0;
 private static final int MAUER = 1;
 private static final int START = 2;
 private static final int ZIEL = 3;
 private static final int FALLE = 4;
 private static final int MOB = 5;
 private static final int FIGUR = 6;
 private static final int SIEG = 7;
 
 private static final int RECHTS = 0;
 private static final int UNTEN = 1;
 private static final int LINKS = 2;
 private static final int OBEN= 3;
 private static final int CHECKPOINT = 8;
 
 int leben=3;
 boolean reachedCheckpoint;
 
 /**
  * Methode bewegt Figur anhand von Koordinaten
  * 
  */
 public void figurBewegen(int richtung){
 	
 		
 	if (richtung == RECHTS){ 
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==BODEN)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==FIGUR)){
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX+1,figurY);
 			figurX=figurX+1;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==START)&(aktuellesLevel>0)){
 			aktuellesLevel--;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurZumZiel(aktuellesLevel);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==ZIEL){
 			aktuellesLevel++;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==FALLE)|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==MOB)){
 			if ((leben>0)&reachedCheckpoint==true){
 				aktuellesLevel=2;
 				Menu.levelDarstellen(2);
 				Menu.figurReset(2, figurX, figurY);
 				leben--;
 			}
 			else {
 				reachedCheckpoint=false;
 				Menu.figurReset(0,figurX,figurY);
 			    Menu.levelDarstellen(0);
 			    Menu.gameOver();
 			}
 		}	
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==SIEG){
 			aktuellesLevel=0;
 			Menu.sieg();
 			//Menu.levelDarstellen(aktuellesLevel);
 			//Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX+1,figurY)==CHECKPOINT){
 			reachedCheckpoint=true;
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX+1,figurY);
 			figurX=figurX+1;
 			Spielfeld.wertSetzenBeiXY(2,9,13,BODEN);
 		}
 	}
 
 			   
 	else if (richtung == UNTEN){ 
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==BODEN)
				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==FIGUR)){
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX,figurY-1);
 			figurY=figurY-1;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==START)&(aktuellesLevel>0)){
 			aktuellesLevel--;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurZumZiel(aktuellesLevel);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==ZIEL){
 			//Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX,figurY-1);
 			aktuellesLevel++;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==FALLE)|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==MOB)){
 			if ((leben>0)&reachedCheckpoint==true){
 				aktuellesLevel=2;
 				Menu.levelDarstellen(2);
 				Menu.figurReset(2, figurX, figurY);
 				leben--;
 			}
 			else {
 				reachedCheckpoint=false;
 			    Menu.figurReset(0,figurX,figurY);
 			    Menu.levelDarstellen(0);
 			    Menu.gameOver();
 			}
 		}
 
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==SIEG){
 			aktuellesLevel=0;
 			Menu.sieg();
 			//Menu.levelDarstellen(aktuellesLevel);
 			//Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY-1)==CHECKPOINT){
 			reachedCheckpoint=true;
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX,figurY-1);
 			figurY=figurY-1;
 			Spielfeld.wertSetzenBeiXY(2,9,13,BODEN);
 		}
 		
 	}
 	
 	
 	else if (richtung == LINKS){ //links
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==BODEN)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==FIGUR)){
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX-1,figurY);
 			figurX=figurX-1;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==START)&(aktuellesLevel>0)){
 			aktuellesLevel--;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurZumZiel(aktuellesLevel);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==ZIEL){
 			//Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX+1,figurY);
 			aktuellesLevel++;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==FALLE)|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==MOB)){
 			if (leben>0){
 				aktuellesLevel=2;
 				Menu.levelDarstellen(2);
 				Menu.figurReset(2, figurX, figurY);
 				leben--;
 			}
 			else {
 				reachedCheckpoint=false;
 			    Menu.figurReset(0,figurX,figurY);
 			    Menu.levelDarstellen(0);
 			    Menu.gameOver();
 			}
 		}
 
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==SIEG){
 			aktuellesLevel=0;
 			Menu.sieg();
 			//Menu.levelDarstellen(aktuellesLevel);
 			//Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX-1,figurY)==CHECKPOINT){
 			reachedCheckpoint=true;
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX-1,figurY);
 			figurX=figurX-1;
 			Spielfeld.wertSetzenBeiXY(2,9,13,BODEN);
 		}
 		
 		
 	}
 	
 	
 	else if (richtung == OBEN){ //oben
 		if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==BODEN)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==FIGUR)){
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX,figurY+1);
 			figurY=figurY+1;
 		}
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==START)&(aktuellesLevel>0)){
 			aktuellesLevel--;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurZumZiel(aktuellesLevel);
 		}
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==ZIEL){
 			aktuellesLevel++;
 			Menu.levelDarstellen(aktuellesLevel);
 			Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}
 		
 		else if ((Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==FALLE)
 				|(Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==MOB)){
 		if (leben>0){
 			aktuellesLevel=2;
 			Menu.levelDarstellen(2);
 			Menu.figurReset(2, figurX, figurY);
 			leben--;
 		}
 		else {
 			reachedCheckpoint=false;
 		    Menu.figurReset(0,figurX,figurY);
 		    Menu.levelDarstellen(0);
 		    Menu.gameOver();
 		}
 		}
 
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==SIEG){
 			//aktuellesLevel=0;
 			Menu.sieg();
 			//Menu.levelDarstellen(aktuellesLevel);
 			//Menu.figurReset(aktuellesLevel, figurX, figurY);
 		}	
 		
 		else if (Spielfeld.wertLesenBeiXY(aktuellesLevel,figurX,figurY+1)==CHECKPOINT){
 			reachedCheckpoint=true;
 			Menu.figurBewegen(aktuellesLevel,figurX,figurY,figurX,figurY+1);
 			figurY=figurY+1;
 			Spielfeld.wertSetzenBeiXY(2,9,13,BODEN);
 		}	
 		
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
 
 
 }
 
