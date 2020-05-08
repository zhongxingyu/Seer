 package hhu.propra_2013.gruppe_13;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 
 public class CoreRoom {
 
 	//Geerbtes
 	Figure 		figure;
 	
 	//Sachen die jeder Raum weiß
 	ArrayList<CoreGameObjects> content; //die alte innere Array list, enthält alle Objekte im Raum
 	boolean isBossRoom, isShop;
 	boolean hasTopNeighbour, hasBottomNeighbour, hasLeftNeighbour, hasRightNeighbour;
 	
 	//Variablen zur Erzeugung
 	int element , column, line , dest;
 	int randomNumber; //speichert die Zufallsvariablevariable in der Raumerzeugung, je nachdem ob es ein  bossraum ist oder nicht
 	String type, boss;
 	int stage; //Nummer des Levels, für den NPC
 	
 	//Konstruktor
 	CoreRoom(Figure inFigure, int inStage, String inBoss, boolean inTopNeighbour, boolean inBottomNeighbour, boolean inLeftNeighbour, boolean inRightNeighbour){
 		figure = inFigure;
 		content = new ArrayList<CoreGameObjects>();
 		stage = inStage;
 		boss = inBoss;
 		hasTopNeighbour = inTopNeighbour;  //ja, ich hasse mich auch schon für solche langen namen
 		hasBottomNeighbour = inBottomNeighbour;
 		hasLeftNeighbour = inLeftNeighbour;
 		hasRightNeighbour = inRightNeighbour;
 		System.out.println("links "+hasLeftNeighbour+" rechts "+hasRightNeighbour+" oben "+hasTopNeighbour+" unten "+hasBottomNeighbour);
 	}
 	
 	void setType(String inType) {
 		type = inType;
 		return;
 	}
 	
 	
 	//liest den Inhalt des Raums aus einer Datei ein
 	public void buildRoom(){
 		
 		//Konstanten anpassen sobald es mehr Räume gibt!!! TODO: Automatisieren
 		//festlegen welche Raumliste der Builder durchgeht
 		if (type == "Raum"){
 			randomNumber =(int)(8*Math.random());
 		}
 		else if (type == "BossRaum"){
 			randomNumber =(int)(3*Math.random());	
 			isBossRoom = true;
 		}
 		else if (type == "Shop"){
 			randomNumber = 0;
 			isShop = true;
 		}
 		else if (type == "Start"){
 			randomNumber = 0;
 		}
 		
 		try {
 			InputStream roomStream = new FileInputStream("Level/"+type+randomNumber+".txt");
 			Reader roomReader = new InputStreamReader (roomStream);
 			
 			element = 0;
 			column = 0; 
 			line = 0;	
 			dest = 0;
 			while ((element = roomReader.read()) != -1){ //Goes trough the whole raumX.txt, and spawns Objects at their Positions
 				
 				switch (element) { 	//ASCII: W=87 D=68 E=69
 				case 'W':			//In order of probability
 					content.add(new MISCWall(column-1+0.5, line-1+0.5, 1, 1, 1)); 	//-1 because the top left corner seems to have
 					break;											//the coordinates 1:1
 					
 				case 'E':
 					content.add(new EnemyMelee(column-1+0.5, line-1+0.5, 1, 1, 0));
 					break;
 
 				case 'D': //looks where the door is, then sets destination accordingly
 					//I have no clue why this works
 					if (line == 0 && hasTopNeighbour){dest = 0; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the upper edge of the field, door should lead up
 					if (line == 14 && hasBottomNeighbour){dest = 2; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the bottom edge of the field, door should lead down
 					if (column==23 && hasRightNeighbour){dest = 1; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the right edge of the field, door should lead right
 					if (column==0 && hasLeftNeighbour){dest = 3; content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, dest));} //Door is on the left edge of the field, door should lead left
 					 //creating door with correct destination
 					break;	
 						
 				case 'I':
 					content.add(new ItemCupACoffee(column-1+0.5, line-1+0.5, 1, 1, 1));
 					break;
 				
 				case 'N':
 					content.add(new MISCDoor(column-1+0.5, line-1+0.5, 1, 1, 0.5, 4));//'Door' leads to the next floor
 					break;
 					
 				case 'T': 
 					content.add(new MISCTarget(column-1+0.5, line-1+0.5,1,1,1));
 					break;
 					
 				case 'C':
 					//to add NPC
 					break;
 				case 'F':
 					content.add(new EnemyMelee(column-1+0.5, line-1+0.5, 1, 1, Enemy.ENEMY_FIGURE_RUN));
 					break;
 					
 				}
 				column++; //sets column up for the next cycle of the switch-case
 					
 				if (column==25){ //since we use 24 chars per line, the 25th should trigger the next line
 					column = 0;
 					line++;
 				}
 			}
 			
 			roomReader.close();
 			roomStream.close();
 			
 		} catch (IOException e) {
 			System.out.println("File not found, system exiting.");
 			System.exit(1);
 		}
 
 	}
 	//Getter
 	//***********************************************************************************************************
 	boolean getBossRoom(){
 		return isBossRoom;
 	}
 	
 	boolean getShop(){
 		return isShop;
 	}
 	
 	
 	ArrayList getContent(){
 		return content;
 	}
 	
 	boolean getTopNeighbour(){
 		return hasTopNeighbour;
 	}
 	
 	boolean getBottomNeighbour(){
 		return hasTopNeighbour;
 	}
 	
 	boolean getLeftNeighbour(){
 		return hasLeftNeighbour;
 	}
 	
 	boolean getRightNeighbour(){
 		return hasLeftNeighbour;
 	}
 	//***********************************************************************************************************
 	//Setter
 	//***********************************************************************************************************
 	
 	void setTopNeighbour(boolean inTopNeighbour){
 		hasTopNeighbour = inTopNeighbour;
 		return;
 	}
 	
 	void setBottomNeighbour(boolean inBottomNeighbour){
 		hasBottomNeighbour = inBottomNeighbour;
 		return;
 	}
 	
 	void setLeftNeighbour(boolean inLeftNeighbour){
 		hasLeftNeighbour = inLeftNeighbour;
 		return;
 	}
 	
 	void setRightNeighbour(boolean inRightNeighbour){
 		hasRightNeighbour = inRightNeighbour;
 		return;
 	}
 }
