 package com.we.PixokalypsePrototypes.test;
 
 import java.util.HashSet;
 
 enum Direction{
 	UP, RIGHT, DOWN, LEFT
 }
 
 public class Map {
 	
 	public int mapSize; //anzahl der Felder 
 	private int minFieldsPerBlock;
 	private int randomFieldsPerBlock; //optionale Gebude
 	public Field[][] map;
 	private int mapBorder; //rand der map, wenn der erreicht ist wird nicht mehr generiert
 	
 	public Map(){
 		this(40, 7, 3, 6);
 	}
 	
 	public Map(int mapSize, int minFieldsPerBlock, int randomFieldsPerBlock, int mapBorder){
 		this.mapSize = mapSize;
 		this.minFieldsPerBlock = minFieldsPerBlock;
 		this.randomFieldsPerBlock = randomFieldsPerBlock;
 		this.mapBorder = mapBorder;
 		map = new Field[mapSize][mapSize];
 		this.initEmptyMap();
 		this.generateMap();
 		printASCII();
 		this.fixStreets();
 		
 		//Print Map
 	}
 	
 
 	private void initEmptyMap() {
 		for(int x = 0; x < mapSize; x++){
 			for(int y = 0; y < mapSize; y++){
 				map[x][y] = new Field(x, y);
 			}
 		}
 	}
 
 	/**
 	 * Generates the map by moving in a spiral path from the middle to the outside 
 	 * and generating blocks of buildings with streets around them on the way. 
 	 */
 	private void generateMap(){
 		int currentFieldX = (int) mapSize/2;
 		int currentFieldY = currentFieldX;
 		
 		int turnTimes = 2; //anzahl der drehungen bis die strecke verlngert werden muss
 		int distance = 1; //schritte zu gehen bis richtung gewechselt werden muss
 		int distanceCounter = distance; //um schritte runter zu zhlen
 		Boolean keepGenerating = true;
 		
 		Direction direction = Direction.DOWN;
 		//int i = direction.ordinal();
 		//direction = Direction.values()[(i++)%Direction.values().length];
 		
 		int currentBlockId = 1;
 		
 		while(keepGenerating){
 			
 			if(distanceCounter == 0){ //wenn aktuelle strecke zuende gelaufen
 				turnTimes--; //dehungen bis streckennderung verringern
 				
 				//richtungsnderung
 				int newDirection = (direction.ordinal() + 1) % Direction.values().length;
 				direction = Direction.values()[newDirection];
 				
 				//distance erhhen wenn turnTimes = 0;
 				if(turnTimes == 0){
 					turnTimes = 2;
 					distance++;
 				}
 				//starte counter mit neuer distance von vorne
 				distanceCounter = distance;				
 			}
 			
 			distanceCounter--;
 			switch(direction){
 				case UP: 
 					currentFieldY--;
 					break;
 				case RIGHT: 
 					currentFieldX++;
 					break;
 				case DOWN: 
 					currentFieldY++;
 					break;
 				case LEFT: 
 					currentFieldX--;
 					break;
 			}
 			
 			Field field = map[currentFieldX][currentFieldY];			
 			if(field.fieldCategory == FieldCategory.EMPTY){
 				createBlock(field, currentBlockId);
 				currentBlockId++;
 			}
 			
 			keepGenerating = !(currentFieldX==mapBorder || currentFieldY == mapBorder);
 			
 		}	
 		
 	}
 
 	/**
 	 * generates a block of buildings around a field (including that field)
 	 * @param field
 	 * @param currentBlockId
 	 */
 	private void createBlock(Field field, int currentBlockId) {
 		field.blockID = currentBlockId;
 		field.fieldCategory = FieldCategory.BUILDING;
 		
 		//emptyNeigbours enthlt alle direkten Nachbarn zu field, auf denen Gebude platziert werden knnen
 		HashSet<Field> emptyNeighbours = new HashSet<Field>();
 		HashSet<Field> fieldsAddedToBlock  = new HashSet<Field>();
 		HashSet<Field> possibleStartFields  = new HashSet<Field>();
 		
 		possibleStartFields.add(field);
 		fieldsAddedToBlock.add(field);
 		
 		int amountOfBuildingsToPlace = minFieldsPerBlock + (int) (Math.random()*randomFieldsPerBlock +1 ) - 1;
 		
 		while(amountOfBuildingsToPlace > 0 ){
 			
 			while(emptyNeighbours.isEmpty()){
 				if(possibleStartFields.isEmpty()){
 					break; //hier geht gar nichts mehr! ENDE
 				}else{
 					field = possibleStartFields.iterator().next();
 					possibleStartFields.remove(field);
 					emptyNeighbours = getEmptyNeighbours(field);
 				}
 			}
 			
 			if(emptyNeighbours.isEmpty()){
 				//break in der oberen while wurde getriggert
 				break;
 			}			
 			
 			//wir haben auf jeden fall emptyNachbarn
 			
 			Field fieldToAdd = emptyNeighbours.iterator().next();
 			emptyNeighbours.remove(fieldToAdd);
 			fieldToAdd.blockID = currentBlockId;
 			fieldToAdd.fieldCategory = FieldCategory.BUILDING;
 			fieldsAddedToBlock.add(fieldToAdd);
 			possibleStartFields.add(fieldToAdd);
 			amountOfBuildingsToPlace--;
 			
 		}
 		
 		createStreetsAroundBlock(fieldsAddedToBlock);
 		
 		
 	}
 	
 	/**
 	 * surrounds a block of buildings with streets
 	 * @param block a HashSet<Field> containing all the Fields that belong to the Block
 	 */
 	private void createStreetsAroundBlock(HashSet<Field> block){
 		for(Field f: block){
 			HashSet<Field> emptyNeighbours = getEmptyNeighbours(f);
 			for(Field whenIGrowUpIWantToBeAStreet: emptyNeighbours){
 				whenIGrowUpIWantToBeAStreet.blockID = 0;
 				whenIGrowUpIWantToBeAStreet.fieldCategory = FieldCategory.STREET;
 			}
 		}
 	}
 	
 	/**
 	 * finds all the neighbours of a field whose fieldCategory is EMPTY
 	 * @param field the field whose neighbours should be found
 	 * @return HashSet with all empty fields
 	 */
 	private HashSet<Field> getEmptyNeighbours(Field field){
 		HashSet<Field> emptyNeighbours = new HashSet<Field>();
 		for(int x = field.xAxis-1; x <= field.xAxis+1; x++){
 			for(int y = field.yAxis-1; y <= field.yAxis+1; y++){
 				if(!(x == field.xAxis && y == field.yAxis)){
 					Field candidate = map[x][y];
 					if(candidate.fieldCategory == FieldCategory.EMPTY){
 						emptyNeighbours.add(candidate);
 					}
 				}
 			}
 		}
 		
 		return emptyNeighbours;
 	}
 
 	
 	/**
 	 * prints the map to the console
 	 */
 	public void printASCII() {
 		for(int i = 0; i < mapSize; i ++){
 			System.out.print("\n");
 			for(int j = 0; j < mapSize; j++){
 				switch(map[i][j].fieldCategory){
 				case EMPTY:
 					System.out.print(" ");
 					break;
 				case BUILDING:
 					System.out.print(" ");
 					break;
 				case STREET:
 					System.out.print("+");
 					break;
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * removes Blocks of 4 or 6 streets by replacing one of the Streets with a Building
 	 */
 	private void fixStreets() {
 		
 		for(int y = 1; y < mapSize-1; y++){
 			for(int x = 1; x < mapSize-1; x++){
 				if(map[x][y].fieldCategory == FieldCategory.STREET &&  //1
 						map[x+1][y].fieldCategory == FieldCategory.STREET && //2
  						map[x+1][y+1].fieldCategory == FieldCategory.STREET && //3
 						map[x][y+1].fieldCategory == FieldCategory.STREET){   //4
 					
 					//4er gefunden
 					if(!(map[x-1][y].fieldCategory == FieldCategory.STREET) && 
 							!(map[x][y-1].fieldCategory == FieldCategory.STREET)){
 						//links oben kann weg
 						makeBuilding(x, y, map[x-1][y].blockID);
 						
 					}else if(!(map[x+1][y-1].fieldCategory == FieldCategory.STREET) && 
 							!(map[x+2][y].fieldCategory == FieldCategory.STREET)){
 						//rechts oben kann weg
 						makeBuilding(x+1, y, map[x+1][y-1].blockID);
 						
 					}else if(!(map[x-1][y+1].fieldCategory == FieldCategory.STREET) && 
 							!(map[x][y+2].fieldCategory == FieldCategory.STREET)){
 						//links unten kann weg
 						makeBuilding(x, y+1, map[x-1][y+1].blockID);
 						
 					}else if(!(map[x+2][y+1].fieldCategory == FieldCategory.STREET) && 
 							!(map[x+1][y+2].fieldCategory == FieldCategory.STREET)){
 						//rechts unten kann weg
 						makeBuilding(x+1, y+1, map[x+2][y+1].blockID);
 						
 					}else{
 						//6er!
 						
 						if(map[x][y+2].fieldCategory == FieldCategory.STREET && map[x+1][y+2].fieldCategory == FieldCategory.STREET){
 							//vertikaler 6er
 							if(map[x-1][y+1].fieldCategory != FieldCategory.STREET){
 								//12 ist keine Strae, 4 kann weg
								makeBuilding(x, y+1, map[x-1][y+1].blockID);
 								
 							}else if(map[x+2][y+1].fieldCategory != FieldCategory.STREET){
 								//9 ist keine Strae, 3 kann weg
 								makeBuilding(x+1, y+1, map[x+2][y+1].blockID);
 								
 							}
 							
 						}else if(map[x+2][y].fieldCategory == FieldCategory.STREET && map[x+2][y+1].fieldCategory == FieldCategory.STREET){
 							//horizontaler 6er
 							if(map[x+1][y-1].fieldCategory != FieldCategory.STREET){
 								//7 ist keine strae, 2 kann weg
 								makeBuilding(x+1, y, map[x+1][y-1].blockID);
 								
 							}else if(map[x+1][y+2].fieldCategory != FieldCategory.STREET){
 								//10 ist keine Strae, 3 kann weg
 								makeBuilding(x+1, y+1, map[x+1][y+2].blockID);
 							}
 							
 						}
 						
 						
 					}
 					
 				}
 				
 			}
 		}
 		
 	}
 	
 	/**
 	 * changes the Type of a Field to BUILDING, assignes blockID
 	 * @param x x-position of the field on the map
 	 * @param y y-position of the field on the map
 	 * @param blockID new Block id of the field
 	 */
 	private void makeBuilding(int x, int y, int blockID){
 		Field f = map[x][y];
 		f.fieldCategory = FieldCategory.BUILDING;
 		f.blockID = blockID;
 	}
 
 }
