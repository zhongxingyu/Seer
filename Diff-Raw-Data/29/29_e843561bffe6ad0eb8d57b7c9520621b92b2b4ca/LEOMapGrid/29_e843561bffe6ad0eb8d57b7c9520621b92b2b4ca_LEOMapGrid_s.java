 package fi.leoteam.playground.aitest;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 public class LEOMapGrid {
 	
 	private ArrayList<ArrayList<LEOEntity>> mapgrid = new ArrayList<ArrayList<LEOEntity>>();
 	private FOVMap fovMap;
 	protected static int mapsize = 1000;//24;
 	private int fovLength = 100;//10;
 	//private int rowLength;
 
 	public LEOMapGrid() {
 		super();
 	}
 	
 	public ArrayList<LEOCharacter> importMapFile() {
 		ArrayList<LEOCharacter> locations = new ArrayList<LEOCharacter>();
 		try {
 			 
 			URL url = getClass().getResource("map.txt");
 			FileInputStream input = new FileInputStream(new File(url.getPath()));
 			InputStreamReader inputReader = new InputStreamReader(input);
 			BufferedReader reader = new BufferedReader(inputReader);
 			
 			while (reader.ready()) {
 				String line = reader.readLine();
 				if(!line.startsWith("//")) {
 					
 					if(line.startsWith("@")) {
 						String row = line.substring(1);
 						String[] tmp = row.split(",");
 						
 						for(int i = 0; i < tmp.length; i++) {
 							locations.add(new LEOCharacter(LEOStaticStrings.OBJECT_STRING_SUSPECT, tmp[i], this));
 						}
 						
 
 					} else {
 							String[] row = line.split(",");
 							
 							/*if(mapgrid.size() == 0) {
 								rowLength = row.length;
 							}*/
 							
 							int rowLength = row.length;
 		
 							ArrayList<LEOEntity> tmp = new ArrayList<LEOEntity>(rowLength);
 							for(int i = 0; i < rowLength; i++) {
 								//System.out.println(i+","+mapgrid.size());
 								tmp.add(LEOEntity.encodeMapMarkings(row[i],i+"-"+mapgrid.size()));
 							}
 							
 							mapgrid.add(tmp);
 						
 					}
 				}
 			}
 			
 			mapgrid = MapGenerator.generateMap(mapsize);
 			fovMap = new FOVMap(mapsize, this);
 			
 		} catch (FileNotFoundException e) {
 			LEOAITest.log.error("Virhe", e);
 			System.exit(0);
 			
 		} catch (IOException e) {
 			LEOAITest.log.error("Virhe", e);
 			System.exit(0);
 		}
 		
 		return locations;
 	}
 	
 	public StringBuilder getMapAsString() {
 		StringBuilder map = new StringBuilder("");
 		for(int i = 0; i < mapgrid.size(); i++) {
 			ArrayList<LEOEntity> tmp = mapgrid.get(i);
 			/*if(i%2 != 0) {
 				map.append(" ");
 			}*/
 			for(int j = 0; j < tmp.size(); j++) {
 				map.append(tmp.get(j).toString());
 				
 				if(j+1 < tmp.size()) {
 					map.append(" ");
 				}
 			}
 			map.append("\n");
 		}
 		
 		return map;
 	}
 
 	public ArrayList<ArrayList<LEOEntity>> getMapgrid() {
 		return mapgrid;
 	}
 
 	public void setMapgrid(ArrayList<ArrayList<LEOEntity>> mapgrid) {
 		this.mapgrid = mapgrid;
 	}
 
 	/*public int getRowLength() {
 		return rowLength;
 	}*/
 	
 	public int[] getAdjacentN(LEOEntity entity) {
 		int[] tmp = new int[2];
 		tmp[0] = entity.getXCoord()-1;
 		tmp[1] = entity.getYCoord();
 		return tmp;
 	}
 	
 	public int[] getAdjacentNE(LEOEntity entity) {
 		int[] tmp = new int[2];
 			tmp[0] = entity.getXCoord()-1;
 			tmp[1] = entity.getYCoord()+1;
 		
 		return tmp;
 	}
 	
 	public int[] getAdjacentE(LEOEntity entity) {
 		int[] tmp = new int[2];
 		tmp[0] = entity.getXCoord();
 		tmp[1] = entity.getYCoord()+1;
 		return tmp;
 	}
 	
 	public int[] getAdjacentSE(LEOEntity entity) {
 		int[] tmp = new int[2];
 			tmp[0] = entity.getXCoord()+1;
 			tmp[1] = entity.getYCoord()+1;
 
 		
 		return tmp;
 	}
 	
 	public int[] getAdjacentS(LEOEntity entity) {
 		int[] tmp = new int[2];
 		tmp[0] = entity.getXCoord()+1;
 		tmp[1] = entity.getYCoord();
 		return tmp;
 	}
 	
 	public int[] getAdjacentSW(LEOEntity entity) {
 		int[] tmp = new int[2];
 			tmp[0] = entity.getXCoord()+1;
 			tmp[1] = entity.getYCoord()-1;
 
 		
 		return tmp;
 	}
 	
 	public int[] getAdjacentW(LEOEntity entity) {
 		int[] tmp = new int[2];
 		tmp[0] = entity.getXCoord();
 		tmp[1] = entity.getYCoord()-1;
 		return tmp;
 	}
 	
 	public int[] getAdjacentNW(LEOEntity entity) {
 		int[] tmp = new int[2];
 			tmp[0] = entity.getXCoord()-1;
 			tmp[1] = entity.getYCoord()-1;
 
 		
 		return tmp;
 	}
 	
 	private LEOEntity addEntityToLocation(int[] loc, LEOEntity entity) {
 		LEOEntity tmp = mapgrid.get(loc[0]).get(loc[1]);
 		
 		mapgrid.get(loc[0]).set(loc[1], entity);
 		
 		return tmp;
 	}
 	
 	public String getMapWithCharacters(ArrayList<LEOCharacter> characters) {
 		ArrayList<ArrayList<LEOEntity>> mapgrid = this.mapgrid;
 		String result = "";
 		ArrayList<LEOEntity> oldOnes = new ArrayList<LEOEntity>();
 		for(int i = 0; i < characters.size(); i++) {
 			LEOCharacter tmp = characters.get(i);
 			int[] loc = tmp.getLocation();
 			oldOnes.add(addEntityToLocation(loc, tmp));
 		}
 		
 		result = getMapAsString().toString();
 		
 		for(int i = 0; i < oldOnes.size(); i++) {
 			addEntityToLocation(oldOnes.get(i).getLocation(), oldOnes.get(i));
 		}
 		
 		return result;
 	}
 	
 	public LEOEntity getEntityFromLocation(int[] loc) {
 		return mapgrid.get(loc[0]).get(loc[1]);
 	}
 	
 	public LEOEntity getEntityFromLocation(Square s) {
 		LEOEntity result;
 		try {
 			result = mapgrid.get(s.getY()).get(s.getX());
 		} catch (NullPointerException e) {
 			result = null;
 		}
 		
 		return result;
 	}
 	
 	private boolean isValidTile(int[] loc) {
 		boolean result = false;
 		
 		if(mapgrid.get(loc[0]) == null) {
 			return result;
 		}
 		if(mapgrid.get(loc[0]).get(loc[1]) == null) {
 			return result;
 		}
 		
 		return true;
 	}
 	
	private boolean isValidTile(Square s) {
		boolean result = false;
		
		if(s.getY() < 0 && s.getY() >= fovMap.length) {
			return result;
		}
		if(s.getX() < 0 && s.getX() >= fovMap.length) {
			return result;
		}
		
		return true;
	}
	
 	public void calculateFOV(LEOEntity character) {
 		Square point = new Square(character.getLocation());
 		
 		long start = System.currentTimeMillis();
 		//First step, set every tile adjacent to character as visible
 		//insertCharacterToFOVMap(character);
 		
 		//Second step, calculate N, E, S and W directions
 		//calculateMainDirections(point);
 		
 		//printFOVMap();
 		
 		//Third step, calculate remaining areas
 		calculateRemainingAreasSE2(point);
 		calculateRemainingAreasNE2(point);//calculateRemainingAreasNE(point, point.getAdjacentNE().getAdjacentE(), point.getAdjacentNE().getAdjacentN());
 		calculateRemainingAreasNW2(point);
 		calculateRemainingAreasSW2(point);
 		long end = System.currentTimeMillis()-start;
 		
 		System.out.println("Aika: "+end);
 		
 		//insertCharacterToFOVMap(character);
 		FOVSquareList onLine;
 		/*onLine = getSquaresOnLineNW(new Square(10,10), new Square(9,9));
 		fovMap.addListToFovMap(onLine);
 		onLine = getSquaresOnLineNE(new Square(10,10), new Square(11,9));
 		fovMap.addListToFovMap(onLine);
 		onLine = getSquaresOnLineSE(new Square(10,10), new Square(11,11));
 		fovMap.addListToFovMap(onLine);
 		onLine = getSquaresOnLineNE(new Square(10,10), new Square(9,11));
 		fovMap.addListToFovMap(onLine);
 		/*onLine = getSquaresOnLineNE(new Square(10,10), new Square(19,9));
 		fovMap.addListToFovMap(onLine);*/
 		
 		insertCharacterToFOVMap(character);
 		
 		//calculateFOVToSinglePoint(new Square(23,23), new Square(0,0));
 		//getSquaresOnLineNW(new Square(12,12), new Square(8,10));
 		//getSquaresOnLineNE(new Square(10,10), new Square(20,7));
 		//System.out.println(Square.findSquaresVertically(0, 15f));
 		//System.out.println(Square.findSquaresHorizontally(21f,1));
 		//printFOVMap();
 		
 		
 		
 		
 	}
 	
 	private void calculateRemainingAreasNW2(Square initial) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresNWSector(this.fovLength);
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			FOVSquareList onLine = calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 		}
 	}
 private void calculateRemainingAreasSW2(Square initial) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresSWSector(this.fovLength);
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			FOVSquareList onLine = calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 		}
 	}
 private void calculateRemainingAreasSE2(Square initial) {
 	
 	//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 	List<Square> lastOnes = initial.calculateLastFOVSquaresSESector(this.fovLength);
 	for(int i = 0; i < lastOnes.size(); i++) {
 		Square tmp = lastOnes.get(i);
 		FOVSquareList onLine = calculateFOVToSinglePoint(initial, tmp);
 		fovMap.addListToFovMap(onLine);
 	}
 }
 private void calculateRemainingAreasNE2(Square initial) {
 	
 	//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 	List<Square> lastOnes = initial.calculateLastFOVSquaresNESector(this.fovLength);
 	for(int i = 0; i < lastOnes.size(); i++) {
 		Square tmp = lastOnes.get(i);
 		FOVSquareList onLine = calculateFOVToSinglePoint(initial, tmp);
 		fovMap.addListToFovMap(onLine);
 	}
 }
 	
 	/*private void calculateRemainingAreasSE(Square initial, Square point, Square point2) {
 		int x = point.getX();
 		int y = point.getY();
 		int x2 = point2.getX();
 		int y2 = point2.getY();
 		int fovLength = this.fovLength*LEOStaticStrings.SQUARE_SIDE_LENGTH;
 		Square tmp;
 		boolean flag, roundFlag = true;
 		do {	
 			tmp = new Square(x, y);
 			Square tmp2 = new Square(x2, y2);
 			if(initial.twoPointsDistance(tmp) <= fovLength && initial.twoPointsDistance(tmp2) <= fovLength) {
 				flag = true;
 				for(int i = x; i < mapsize && y < mapsize && flag; i++) {
 					//System.out.println("PISTE ("+i+","+y+")");
 					tmp = new Square(i, y);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.SE_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 				}
 				flag = true;
 				for(int j = y2; j < mapsize && x2 < mapsize && flag; j++) {
 					//System.out.println("PISTE ("+x2+","+j+")");
 					tmp = new Square(x2, j);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.SE_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 				}
 				x = x2+1;
 				y++;
 				
 				x2++;
 				y2 = y+1;
 				
 				//System.out.println("*****************************************************************");
 				//System.out.println("x: "+x+" y: "+y+" x2:"+x2+" y2:"+y2);
 			}
 			else {
 				roundFlag = false;
 			}
 		} while (((x < mapsize && y < mapsize) || (x2 < mapsize && y2 < mapsize)) && roundFlag);
 		//printFOVMap();
 	}*/
 	
 	/*private void calculateRemainingAreasSW(Square initial, Square point, Square point2) {
 		int x = point.getX();
 		int y = point.getY();
 		int x2 = point2.getX();
 		int y2 = point2.getY();
 		int fovLength = this.fovLength*LEOStaticStrings.SQUARE_SIDE_LENGTH;
 		Square tmp;
 		boolean flag, roundFlag = true;
 		do {
 			tmp = new Square(x, y);
 			Square tmp2 = new Square(x2, y2);
 			if(initial.twoPointsDistance(tmp) <= fovLength && initial.twoPointsDistance(tmp2) <= fovLength) {
 				flag = true;
 				for(int i = x; i >= 0 && y < mapsize && flag; i--) {
 					//System.out.println("PISTE ("+i+","+y+")");
 					tmp = new Square(i, y);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.SW_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 				}
 				flag = true;
 				for(int j = y2; j < mapsize && x2 >= 0 && flag; j++) {
 					//System.out.println("PISTE ("+x2+","+j+")");
 					tmp = new Square(x2, j);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.SW_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 				}
 				x = x2-1;
 				y++;
 				
 				x2--;
 				y2 = y+1;
 				
 				
 				//System.out.println("*****************************************************************");
 				//System.out.println("x: "+x+" y: "+y+" x2: "+x2+" y2: "+y2);
 			}
 			else {
 				roundFlag = false;
 			}
 		} while (((x >= 0 && y < mapsize) || (x2 >= 0 && y2 < mapsize)) && roundFlag);
 		//printFOVMap();
 	}*/
 	
 	/*private void calculateRemainingAreasNE(Square initial, Square point, Square point2) {
 		int x = point.getX();
 		int y = point.getY();
 		int x2 = point2.getX();
 		int y2 = point2.getY();
 		int fovLength = this.fovLength*LEOStaticStrings.SQUARE_SIDE_LENGTH;
 		//System.out.println(point.toString()+" "+point2.toString());
 		Square tmp;
 		boolean flag, roundFlag = true;
 		do {
 			tmp = new Square(x, y);
 			Square tmp2 = new Square(x2, y2);
 			if(initial.twoPointsDistance(tmp) <= fovLength && initial.twoPointsDistance(tmp2) <= fovLength) {
 				flag = true;
 				for(int i = x; i < mapsize && y >= 0 && flag; i++) {
 					//System.out.println("PISTE ("+i+","+y+")");
 					tmp = new Square(i, y);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.NE_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 	
 				}
 				flag = true;
 				for(int j = y2; j >= 0 && x2 < mapsize && flag; j--) {
 					//System.out.println("PISTE ("+x2+","+j+")");
 					tmp = new Square(x2, j);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.NE_SECTOR);
 					}
 					else {
 						flag = false;
 					}
 				}
 				x = x2+1;
 				y--;
 				
 				x2++;
 				y2 = y-1;
 				
 				//System.out.println("*****************************************************************");
 				//System.out.println("x: "+x+" y: "+y+" x2:"+x2+" y2:"+y2);
 			}
 			else {
 				roundFlag = false;
 			}
 		} while (((x < mapsize && y >= 0) || (x2 < mapsize && y2 >= 0)) && roundFlag);
 		//printFOVMap();
 	}*/
 	
 	/*private void calculateRemainingAreasNW(Square initial, Square point, Square point2) {
 		int x = point.getX();
 		int y = point.getY();
 		int x2 = point2.getX();
 		int y2 = point2.getY();
 		int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		Square tmp;
 		boolean flag, roundFlag = true;
 		//System.out.println(point.toString()+" "+point2.toString());
 		do {
 			tmp = new Square(x, y);
 			Square tmp2 = new Square(x2, y2);
 			if(initial.twoPointsDistance(tmp) <= fovLength && initial.twoPointsDistance(tmp2) <= fovLength) {
 				flag = true;
 				for(int i = x; i >= 0 && y >= 0 && flag; i--) {
 					//System.out.println("PISTE ("+i+","+y+")");
 					tmp = new Square(i, y);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.NW_SECTOR);
 						calculateFOVToSinglePoint(initial, tmp);
 					}
 					else {
 						flag = false;
 					}
 					if(initial.isLastSquare(tmp, fovLength)) {
 						flag = false;
 						//checkIfToAddToFOVMap(initial, tmp, Square.NW_SECTOR);
 						calculateFOVToSinglePoint(initial, tmp);
 					}
 					
 				}
 				flag = true;
 				for(int j = y2; j >= 0 && x2 < mapsize && flag; j--) {
 					//System.out.println("PISTE ("+x2+","+j+")");
 					tmp = new Square(x2, j);
 					if(initial.twoPointsDistance(tmp) <= fovLength) {
 						checkIfToAddToFOVMap(initial, tmp, Square.NW_SECTOR);
 						calculateFOVToSinglePoint(initial, tmp);
 					}
 					else {
 						flag = false;
 					}
 					if(initial.isLastSquare(tmp, fovLength)) {
 						flag = false;
 						calculateFOVToSinglePoint(initial, tmp);
 					}
 				}
 				x = x2-1;
 				y--;
 				
 				x2--;
 				y2 = y-1;
 				//System.out.println("*****************************************************************");
 				//System.out.println("x: "+x+" y: "+y+" x2:"+x2+" y2:"+y2);
 			}
 			else {
 				roundFlag = false;
 			}
 			
 			
 		} while (((x >= 0 && y >= 0) || (x2 >= 0 && y2 >= 0)) && roundFlag);
 		//printFOVMap();
 	}*/
 	
 	private void calculateRemainingAreasNW(Square initial) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresNWSector(this.fovLength);
 		long time = System.currentTimeMillis();
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			//System.out.println("****"+tmp.toStringShort()+"****");
 			//calculateFOVToSinglePoint(initial, tmp);
 			FOVSquareList onLine = getSquaresOnLineNW(initial, tmp);//calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 			/*boolean stop = false;
 			for(int j = 0; j < onLine.size() && !stop; j++) {
 				Square s = onLine.get(j);
 				//System.out.println(s.toStringShort());
 				if(getEntityFromLocation(s).blocksFOV()) {
 					stop = true;
 					//System.out.println("Square "+s.toStringShort()+" blocks the view!");
 				}
 				addToFOVMap(s);
 			}*/
 		}
 		time = System.currentTimeMillis()-time;
 		System.out.println("Kulunut aika tavalla 1: "+time);
 		time = System.currentTimeMillis();
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			FOVSquareList onLine = calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 		}
 		time = System.currentTimeMillis()-time;
 		System.out.println("Kulunut aika tavalla 2: "+time);
 		
 	}
 	
 	private void calculateRemainingAreasNE(Square initial/*, Square point, Square point2*/) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresNESector(this.fovLength);
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			//System.out.println("****"+tmp.toStringShort()+"****");
 			//calculateFOVToSinglePoint(initial, tmp);
 			FOVSquareList onLine = getSquaresOnLineNE(initial, tmp);//calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 			/*boolean stop = false;
 			for(int j = 0; j < onLine.size() && !stop; j++) {
 				Square s = onLine.get(j);
 				//System.out.println(s.toStringShort());
 				if(getEntityFromLocation(s).blocksFOV()) {
 					stop = true;
 					//System.out.println("Square "+s.toStringShort()+" blocks the view!");
 				}
 				addToFOVMap(s);
 			}*/
 		}
 		
 	}
 	
 	private void calculateRemainingAreasSE(Square initial) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresSESector(this.fovLength);
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			//System.out.println("****"+tmp.toStringShort()+"****");
 			//calculateFOVToSinglePoint(initial, tmp);
 			FOVSquareList onLine = getSquaresOnLineSE(initial, tmp);//calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 			/*boolean stop = false;
 			for(int j = 0; j < onLine.size() && !stop; j++) {
 				Square s = onLine.get(j);
 				//System.out.println(s.toStringShort());
 				if(getEntityFromLocation(s).blocksFOV()) {
 					stop = true;
 					//System.out.println("Square "+s.toStringShort()+" blocks the view!");
 				}
 				addToFOVMap(s);
 			}*/
 		}
 		
 	}
 	
 	private void calculateRemainingAreasSW(Square initial) {
 		
 		//int fovLength = (this.fovLength-1)*LEOStaticStrings.SQUARE_SIDE_LENGTH+(2*LEOStaticStrings.SQUARE_SIDE_HALF_WITH_CENTER);
 		List<Square> lastOnes = initial.calculateLastFOVSquaresSWSector(this.fovLength);
 		for(int i = 0; i < lastOnes.size(); i++) {
 			Square tmp = lastOnes.get(i);
 			//System.out.println("****"+tmp.toStringShort()+"****");
 			//calculateFOVToSinglePoint(initial, tmp);
 			FOVSquareList onLine = getSquaresOnLineSW(initial, tmp);//calculateFOVToSinglePoint(initial, tmp);
 			fovMap.addListToFovMap(onLine);
 			/*boolean stop = false;
 			for(int j = 0; j < onLine.size() && !stop; j++) {
 				Square s = onLine.get(j);
 				//System.out.println(s.toStringShort());
 				if(getEntityFromLocation(s).blocksFOV()) {
 					stop = true;
 					//System.out.println("Square "+s.toStringShort()+" blocks the view!");
 				}
 				addToFOVMap(s);
 			}*/
 		}
 		
 	}
 	
 	/*private void checkIfToAddToFOVMap(Square initial, Square s, int sector) {
 		ArrayList<Square> possibilities = s.getPossibleLastSquares(sector);
 		boolean result = false;
 		//System.out.println("******************\n Checking point "+s.toString());
 		for(int i = 0; i < possibilities.size() && !result; i++) {
 			Square tmp = possibilities.get(i);
 			//System.out.println(i+". mahdollisuus: "+tmp.toString());
 			//if(tmp.isSquareOnLine(initial, s).size() >= 2) {
 			if(tmp.showSquare(initial, s)) {
 				//System.out.println("Is on line");
 				if(isTileVisibleInFOVMap(tmp)) {
 					//System.out.println("Is visible");
 					result = getEntityFromLocation(tmp).blocksFOV();
 					//System.out.println("Blocks sight: "+result);
 				}
 				else {
 					//System.out.println("Is not visible");
 					result = true;
 				}
 				
 			}
 		}
 		
 		if(!result) {
 			//System.out.println("***Double check****");
 			possibilities = s.getPossibleDoubleLastSquares(sector);
 			for(int i = 0; i < possibilities.size() && !result; i++) {
 				Square tmp = possibilities.get(i);
 				//System.out.println(i+". mahdollisuus: "+tmp.toString());
 				//if(tmp.isSquareOnLine(initial, s).size() >= 2) {
 				if(tmp.showSquare(initial, s)) {
 					//System.out.println("Is on line");
 					if(isTileVisibleInFOVMap(tmp)) {
 						//System.out.println("Is visible");
 						result = getEntityFromLocation(tmp).blocksFOV();
 						//System.out.println("Blocks sight: "+result);
 					}
 					else {
 						//System.out.println("Is not visible");
 						result = true;
 					}
 					
 				}
 			}
 			if(!result) {
 				//System.out.println("Point will be visible on map");
 				addToFOVMap(s);
 			}
 		}
 		
 		
 	}*/
 	
	private void calculateMainDirections(Square point) {
 		//North
 		int x, y;
 		int calc = 1;
 		
 		x = point.getX();
 		for(int i = point.getY()-2; i >= 0 && calc < fovLength; i--) {
 			Square tmp = new Square(x, i);
 			Square tmp2 = tmp.getAdjacentS();
 			if(isTileVisibleInFOVMap(tmp2)) {
 				if(!getEntityFromLocation(tmp2).blocksFOV()) {
 					addToFOVMap(tmp);
 				}
 			}
 			calc++;
 		}
 		
 		calc = 1;
 		//South
 		for(int i = point.getY()+2; i < mapsize && calc < fovLength; i++) {
 			Square tmp = new Square(x, i);
 			Square tmp2 = tmp.getAdjacentN();
 			if(isTileVisibleInFOVMap(tmp2)) {
 				if(!getEntityFromLocation(tmp2).blocksFOV()) {
 					addToFOVMap(tmp);
 				}
 			}
 			calc++;
 		}
 		
 		calc = 1;
 		//West
 		y = point.getY();
 		for(int i = point.getX()-2; i >= 0 && calc < fovLength; i--) {
 			Square tmp = new Square(i, y);
 			Square tmp2 = tmp.getAdjacentE();
 			if(isTileVisibleInFOVMap(tmp2)) {
 				if(!getEntityFromLocation(tmp2).blocksFOV()) {
 					addToFOVMap(tmp);
 				}
 			}
 			calc++;
 		}
 		
 		calc = 1;
 		y = point.getY();
 		for(int i = point.getX()+2; i < mapsize && calc < fovLength; i++) {
 			Square tmp = new Square(i, y);
 			Square tmp2 = tmp.getAdjacentW();
 			if(isTileVisibleInFOVMap(tmp2)) {
 				if(!getEntityFromLocation(tmp2).blocksFOV()) {
 					addToFOVMap(tmp);
 				}
 			}
 			calc++;
 		}
	}
	
	private boolean isTileVisibleInFOVMap(Square s) {
		if(isValidTile(s)) {
			if(fovMap[s.getY()][s.getX()].equals(LEOStaticStrings.OBJECT_STRING_NOT_VISIBLE)) {
				return false;
			}
		}

		return true;
	}
 	
 	private void insertCharacterToFOVMap(LEOEntity character) {
 		Square point = new Square(character.getLocation());
 		fovMap.addCharacter(point, character);
 		//fovMap[point.getY()][point.getX()] = character.toString();
 		
 		/*Square tmp = point.getAdjacentN();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentNE();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentE();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentSE();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentS();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentSW();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentW();
 		addToFOVMap(tmp);
 		
 		tmp = point.getAdjacentNW();
 		addToFOVMap(tmp);*/
 	}
 	
 	private void printFOVMap() {
 		System.out.println(fovMap.toString());
 	}
 	
 	//TODO : We need to implement FOVMap so, that this method can ask, if some square is already marked
 	//as checked -> in these situations we need to check if square is not visible, because then it is a
 	//blocker
 	/*private void addToFOVMap(Square loc) {
 		int x = loc.getX();
 		int y = loc.getY();
 		if(x >= 0 && x < fovMap.length && y >= 0 && y < fovMap.length) {
 			fovMap[y][x] = getEntityFromLocation(loc).toString();
 		}
 	}
 	
 	/*private boolean calculateFOVToSinglePoint(Square startPoint, Square targetPoint) {
 		System.out.println("TARKISTETAAN RUUTUA ("+targetPoint.getX()+","+targetPoint.getY()+")");
 		boolean resultFlag = false;
 		int sector = Square.getPossibleSector(startPoint, targetPoint);
 		ArrayList<Square> nextOnes = startPoint.getPossibleNextSquares(sector);
 		ArrayList<Square> onLine = new ArrayList<Square>();
 		Square current = startPoint;
 		boolean outerFlag = true;
 		
 		while(outerFlag && !resultFlag) {
 			
 			if(nextOnes.contains(targetPoint)) {
 				outerFlag = false;
 			}
 			else {
 				for(int i = 0; i < nextOnes.size(); i++) {
 					Square tmp = nextOnes.get(i);
 					if(tmp.isSquareOnLine(startPoint, targetPoint).size() >= 2) {
 						resultFlag = getEntityFromLocation(tmp).isBlockFOV();
 						current = tmp;
 						onLine.add(tmp);
 						//System.out.println("****** Ruutu "+tmp.getLocationAsSimpleString()+" blockkaa nkymn "+resultFlag);
 					}
 				}
 			}
 			
 			nextOnes = current.getPossibleNextSquares(sector);
 			
 		}
 		
 		
 		
 		System.out.println("**** Following squares are on the line ****");
 		for(int i = 0; i < onLine.size(); i++) {
 			System.out.println(onLine.get(i).toString());
 		}
 		System.out.println("********");
 		
 		resultFlag = !resultFlag;
 		//System.out.println("Nkyhteys pisteiden vlill on: "+resultFlag);
 		
 		return resultFlag;
 	}*/
 	
 	private FOVSquareList calculateFOVToSinglePoint(Square startPoint, Square targetPoint) {
 		//System.out.println("TARKISTETAAN RUUTUA "+targetPoint.toStringShort());
 		//System.out.println(startPoint.toStringShort()+" -> "+targetPoint.toStringShort());
 		int sector = Square.getPossibleSector(startPoint, targetPoint);
 		ArrayList<Square> nextOnes = startPoint.getPossibleNextSquares(sector);
 		FOVSquareList onLine = new FOVSquareList();
 		//ArrayList<Integer> codes = new ArrayList<Integer>();
 		Square current = startPoint;
 		boolean outerFlag = true;
 		//int code;
 		
 		while(outerFlag) {
 			
 			if(nextOnes.contains(targetPoint)) {
 				outerFlag = false;
 			}
 			else {
 				//TODO : This could be improved so that we don't always need to check all the next 3 squares
 				for(int i = 0; i < nextOnes.size(); i++) {
 					Square tmp = nextOnes.get(i);
 					//code = tmp.showSquare(startPoint, targetPoint);
 					//Float areaRatio = tmp.isSquareOnLine(startPoint, targetPoint);
 					if(tmp.isSquareOnLine(startPoint, targetPoint)) {
 						current = tmp;
 						onLine.add(tmp);
 						
 						//codes.add(code);
 						
 						//System.out.println("****** Ruutu "+tmp.getLocationAsSimpleString()+" blockkaa nkymn "+resultFlag);
 					}
 				}
 			}
 			
 			nextOnes = current.getPossibleNextSquares(sector);
 			
 		}
 		
 		
 		
 		/*System.out.println("**** Following squares are on the line ****");
 		for(int i = 0; i < onLine.size(); i++) {
 			System.out.println(onLine.get(i).toStringShort());
 		}
 		System.out.println("********");*/
 		
 		onLine.add(targetPoint);
 		return onLine;
 		//time = System.currentTimeMillis()-time;
 		//System.out.println("FOV-tapa1 aika: "+time);
 		//codes.add(1);
 		/*boolean stop = false;
 		for(int i = 0; i < onLine.size() && !stop; i++) {
 			Square tmp = onLine.get(i);
 			//code = codes.get(i);
 			if(getEntityFromLocation(tmp).blocksFOV() && code == 1) {
 				stop = true;
 				//System.out.println("Square "+tmp.toStringShort()+" blocks the view!");
 			}
 			addToFOVMap(tmp);
 		}*/
 		
 		/*if(blocker != -1) {
 			for(int i = blocker; i < onLine.size(); i++) {
 				//System.out.println("Square "+onLine.get(i).toStringShort()+" is now hidden.");
 				hideInFOVMap(onLine.get(i));
 			}
 		}*/
 	}
 	
 	/*public ArrayList<Square> calculateIntersection(ArrayList<Square> possibleSquares, Square point1, Square point2) {
 		long startTime = System.currentTimeMillis();
 		ArrayList<Square> onLine = new ArrayList<Square>();
 		for(int i = 0; i < possibleSquares.size(); i++) {
 			Square tmp = possibleSquares.get(i);
 			if(!tmp.equals(point1) && !tmp.equals(point2)) {
 				if(tmp.isSquareOnLine(point1, point2)) {
 					onLine.add(tmp);
 				}
 			}
 			
 		}
 		long endTime = System.currentTimeMillis() - startTime;
 		System.out.println("Kulunut aika: "+endTime);
 		System.out.println("**** Following squares are on the line ****");
 		for(int i = 0; i < onLine.size(); i++) {
 			System.out.println(onLine.get(i).toString());
 		}
 		System.out.println("********");
 
 		return onLine;
 	}*/
 	
 	private FOVSquareList getSquaresOnLineNW(Square startPoint, Square endPoint) {
 		int howManyYs = Math.abs(startPoint.getY()-endPoint.getY());
 		int howManyXs = Math.abs(startPoint.getX()-endPoint.getX());
 		FOVSquareList result = new FOVSquareList();
 		//System.out.println(howManyXs);
 		//System.out.println(howManyYs);
 		int x;
 		/*System.out.println(startPoint.toStringShort()+" -> "+endPoint.toStringShort());
 		System.out.println("*****MUKAANOTETTAVAT RUUDUT****");
 		System.out.println("VERTICALLY:");*/
 		for(int i = startPoint.getX()-1; i >= startPoint.getX()-howManyXs; i--) {
 			Square s = new Square(i, startPoint.getY());
 			x = s.getPixel_x()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)((float)(-endPoint.getPixel_y()+startPoint.getPixel_y())/(float)(endPoint.getPixel_x()-startPoint.getPixel_x())*(x-startPoint.getPixel_x())-startPoint.getPixel_y());
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp21: "+tmp2);
 			result.add(new Square(i, tmp2));*/
 			result.addAll(Square.findSquaresVertically(i, tmp, Square.NW_SECTOR));
 		}
 		//System.out.println("HORIZONTALLY:");
 		for(int i = startPoint.getY(); i > startPoint.getY()-howManyYs; i--) {
 			Square s = new Square(startPoint.getX(), i);
 			x = -s.getPixel_y()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)(x+startPoint.getPixel_y())*((float)(endPoint.getPixel_x()-startPoint.getPixel_x())/(float)(-endPoint.getPixel_y()+startPoint.getPixel_y()))+startPoint.getPixel_x();
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp22: "+tmp2);
 			Square s2 = new Square(tmp2, i);
 			if(!result.contains(s2)) {
 				result.add(s2);
 			}*/
 			result.addAll(Square.findSquaresHorizontally(tmp, i));
 		}
 		
 		
 		if(!result.contains(endPoint)) {
 			result.add(endPoint);
 		}
 		Square.sort(result, Square.NW_SECTOR);
 		/*System.out.println("real");
 		for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}
 		System.out.println("*********");*/
 		/*
 		for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}*/
 		return result;
 	}
 	
 	private FOVSquareList getSquaresOnLineSW(Square startPoint, Square endPoint) {
 		int howManyYs = Math.abs(startPoint.getY()-endPoint.getY());
 		int howManyXs = Math.abs(startPoint.getX()-endPoint.getX());
 		FOVSquareList result = new FOVSquareList();
 		//System.out.println(howManyXs);
 		//System.out.println(howManyYs);
 		int x;
 		System.out.println(startPoint.toStringShort()+" -> "+endPoint.toStringShort());
 		System.out.println("*****MUKAANOTETTAVAT RUUDUT****");
 		System.out.println("VERTICALLY:");
 		for(int i = startPoint.getX()-1; i >= startPoint.getX()-howManyXs; i--) {
 			Square s = new Square(i, startPoint.getY());
 			x = s.getPixel_x()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)((float)(-endPoint.getPixel_y()+startPoint.getPixel_y())/(float)(endPoint.getPixel_x()-startPoint.getPixel_x())*(x-startPoint.getPixel_x())-startPoint.getPixel_y());
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp21: "+tmp2);
 			result.add(new Square(i, tmp2));*/
 			result.addAll(Square.findSquaresVertically(i, tmp, Square.SW_SECTOR));
 		}
 		System.out.println("HORIZONTALLY:");
 		for(int i = startPoint.getY()+1; i <= startPoint.getY()+howManyYs; i++) {
 			Square s = new Square(startPoint.getX(), i);
 			x = -s.getPixel_y()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)(x+startPoint.getPixel_y())*((float)(endPoint.getPixel_x()-startPoint.getPixel_x())/(float)(-endPoint.getPixel_y()+startPoint.getPixel_y()))+startPoint.getPixel_x();
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp22: "+tmp2);
 			//System.out.println("tmp2: "+tmp);
 			Square s2 = new Square(tmp2, i);
 			if(!result.contains(s2)) {
 				result.add(s2);
 			}*/
 			result.addAll(Square.findSquaresHorizontally(tmp, i));
 		}
 		if(!result.contains(endPoint)) {
 			result.add(endPoint);
 		}
 		Square.sort(result, Square.SW_SECTOR);
 		System.out.println("real");
 		for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}
 		System.out.println("****");
 		return result;
 	}
 	
 	private FOVSquareList getSquaresOnLineNE(Square startPoint, Square endPoint) {
 		int howManyYs = Math.abs(startPoint.getY()-endPoint.getY());
 		int howManyXs = Math.abs(startPoint.getX()-endPoint.getX());
 		FOVSquareList result = new FOVSquareList();
 		//System.out.println(howManyXs);
 		//System.out.println(howManyYs);
 		Square s, s2, s3;
 		int x;
 		
 		for(int i = startPoint.getX(); i < startPoint.getX()+howManyXs; i++) {
 			s = new Square(i, startPoint.getY());
 			x = s.getPixel_x()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)((float)(-endPoint.getPixel_y()+startPoint.getPixel_y())/(float)(endPoint.getPixel_x()-startPoint.getPixel_x())*(x-startPoint.getPixel_x())-startPoint.getPixel_y());
 			result.addAll(Square.findSquaresVertically(i, tmp, Square.NE_SECTOR));
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			System.out.println("tmp21: "+tmp);
 			s2 = new Square(i, tmp2);
 			s3 = s2.getAdjacentE();
 			System.out.println("Add: "+s2.toStringShort());
 			System.out.println("Add: "+s3.toString());
 			result.add(s2);
 			result.add(s3);*/
 		}
 		for(int i = startPoint.getY(); i > startPoint.getY()-howManyYs; i--) {
 			s = new Square(startPoint.getX(), i);
 			x = -s.getPixel_y()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)(x+startPoint.getPixel_y())*((float)(endPoint.getPixel_x()-startPoint.getPixel_x())/(float)(-endPoint.getPixel_y()+startPoint.getPixel_y()))+startPoint.getPixel_x();
 			result.addAll(Square.findSquaresHorizontally(tmp, i));
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			System.out.println("tmp22: "+tmp);
 			s2 = new Square(tmp2, i);
 			s3 = s2.getAdjacentN();
 			System.out.println("Add: "+s2.toStringShort());
 			System.out.println("Add: "+s3.toString());
 				result.add(s2);
 				result.add(s3);*/
 		}
 		
 		//System.out.println(new Square(10,9));
 		if(!result.contains(endPoint)) {
 			result.add(endPoint);
 		}
 		Square.sort(result, Square.NE_SECTOR);
 		System.out.println("real");
 		for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}
 		System.out.println("****");
 		/*for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}*/
 		return result;
 	}
 	
 	private FOVSquareList getSquaresOnLineSE(Square startPoint, Square endPoint) {
 		int howManyYs = Math.abs(startPoint.getY()-endPoint.getY());
 		int howManyXs = Math.abs(startPoint.getX()-endPoint.getX());
 		FOVSquareList result = new FOVSquareList();
 		//System.out.println(howManyXs);
 		//System.out.println(howManyYs);
 		int x;
 		System.out.println(startPoint.toStringShort()+" -> "+endPoint.toStringShort());
 		System.out.println("*****MUKAANOTETTAVAT RUUDUT****");
 		System.out.println("VERTICALLY:");
 		for(int i = startPoint.getX(); i < startPoint.getX()+howManyXs; i++) {
 			Square s = new Square(i, startPoint.getY());
 			x = s.getPixel_x()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)((float)(-endPoint.getPixel_y()+startPoint.getPixel_y())/(float)(endPoint.getPixel_x()-startPoint.getPixel_x())*(x-startPoint.getPixel_x())-startPoint.getPixel_y());
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp21: "+tmp2);
 			result.add(new Square(i, tmp2));*/
 			result.addAll(Square.findSquaresVertically(i, tmp, Square.SE_SECTOR));
 		}
 		System.out.println("HORIZONTALLY:");
 		for(int i = startPoint.getY()+1; i <= startPoint.getY()+howManyYs; i++) {
 			Square s = new Square(startPoint.getX(), i);
 			x = -s.getPixel_y()+LEOStaticStrings.SQUARE_SIDE_HALF_WITHOUT_CENTER;
 			float tmp = (float)(x+startPoint.getPixel_y())*((float)(endPoint.getPixel_x()-startPoint.getPixel_x())/(float)(-endPoint.getPixel_y()+startPoint.getPixel_y()))+startPoint.getPixel_x();
 			/*int tmp2 = (int)Math.abs((tmp/(LEOStaticStrings.SQUARE_SIDE_LENGTH-1)));
 			//System.out.println("tmp22: "+tmp2);
 			//System.out.println("tmp2: "+tmp);
 			Square s2 = new Square(tmp2, i);
 			if(!result.contains(s2)) {
 				result.add(s2);
 			}*/
 			result.addAll(Square.findSquaresHorizontally(tmp, i));
 		}
 		if(!result.contains(endPoint)) {
 			result.add(endPoint);
 		}
 		Square.sort(result, Square.SE_SECTOR);
 		System.out.println("real");
 		for(int i = 0; i < result.size(); i++) {
 			System.out.println(result.get(i).toStringShort());
 		}
 		System.out.println("****");
 		return result;
 	}
 }
