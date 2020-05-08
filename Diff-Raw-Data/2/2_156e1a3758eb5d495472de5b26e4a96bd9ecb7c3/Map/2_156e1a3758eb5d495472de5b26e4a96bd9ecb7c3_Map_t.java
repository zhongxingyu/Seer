 package GameLogic;
 
 import com.badlogic.gdx.Gdx;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import com.badlogic.gdx.files.FileHandle;
 
 public class Map {
 	
 	//Type of sunken Square in the sunkenBoard
 	public enum SunkenTypes{empty,sunkenObject}
 	
 	//Type of Fissure Square in the fissuresBoard
 	public enum FissuresTypes{empty,barrelWithFissure, fissureC, fissureSX, fissureSY, crossingFissures}
 	
 	//Type of Water Square in the waterBoard
 	public enum WaterTypes{empty,water1SOpN, water1SOpS, water1SOpE, water1SOpW, water2SOpCornerEN, water2SOpCornerNW, 
 		water2SOpCornerSE, water2SOpCornerWS, water2SOpBridgeX, water2SOpBridgeY, water3SOpN, 
 		water3SOpS, water3SOpE, water3SOpW, water4SOp}
 	
 	//Type of Basic Square in the boardGame
 	public enum TypeSquare{empty,unbreakable,breakable,bootUpgrade,rangeUpgrade,numHarpoonUpgrade,
 		throwUpgrade,Harpoon}
 	
 	//Total number of each Upgrade
 	private int maxBootUpgrades;
 	private int maxRangeUpgrades;
 	private int maxNumHarpoonsUpgrades;
 	private int maxThrowUpgrades;
 	
 	//Board Attributes
 	private String mapName;
 	private int length;
 	private int width;
 	
 	//Game Boards
 	private TypeSquare[][] boardGame;
 	private FissuresTypes[][] fissuresBoard;
 	private WaterTypes[][] waterBoard;
 	private SunkenTypes[][] sunkenBoard;
 	
 	//LoadFile
 	private String xmlMap;
 	
 	public Map(int lenght, int width, String mapPath) {
 		this.length=lenght;
 		this.width=width;
 		this.boardGame = new TypeSquare[lenght][width];
 		this.fissuresBoard = new FissuresTypes[lenght][width];
 		this.waterBoard = new WaterTypes[lenght][width];
 		this.sunkenBoard = new SunkenTypes[lenght][width];
 		//Initialize the 3 board with empty square.
 		//Only basic board has another type square unbreakeable square
 		for (int i=0;i<width;i++){
 			for (int j=0;j<length;j++){
 				if ((i % 2 == 1) && (j % 2 == 1)) boardGame[i][j] = TypeSquare.unbreakable;
 				else boardGame[i][j] = TypeSquare.empty;
 				fissuresBoard[i][j] = FissuresTypes.empty;
 				waterBoard[i][j] = WaterTypes.empty;
 				sunkenBoard[i][j] = SunkenTypes.empty;
 			}	
 		}
 		//load atributes of a XML map
 		loadMap(mapPath);
 	}
 
 	
 	//Methods of XML
 	
 	private String getData(String xml, String data){
 		int begin;
 		int end;
 		begin = xml.indexOf("<".concat(data).concat(">")) + data.length() + 2;
 		end = xml.indexOf("</".concat(data).concat(">"));
 		return xml.substring(begin, end);
 	}
 	
 	private void loadMap(String mapPath) {
 		loadXML(mapPath);
 		if (xmlMap.equals("")){
 			maxBootUpgrades = 8;
 			maxRangeUpgrades = 8;
 			maxNumHarpoonsUpgrades = 8;
 			maxThrowUpgrades = 8;
 		} else {
 			xmlMap = getData(xmlMap, "Map");
 			loadName();
 			loadUpgrades();
 			loadBoxes();
 		}	
 	}
 
 	private void loadXML(String mapPath) {
 		/*xmlMap = "<Map> <Name>mapaGuay</Name> <Upgrades> <Boots>5</Boots> <MaxRange>4</MaxRange> <NumLances>6</NumLances> <Throw>4</Throw> </Upgrades>" +
 				" <Boxes> <Breakable>4-5</Breakable> <Breakable>1-4</Breakable> <Breakable>2-4</Breakable> <Breakable>5-6</Breakable> " +
 				"<Breakable>4-6</Breakable> <Breakable>8-5</Breakable> </Boxes> </Map>";*/
 		try {
 			FileHandle handle = Gdx.files.internal("data/".concat(mapPath)); //Gdx.files.getFileHandle("data/".concat("mapaPrueba.xml"), FileType.External);
 			xmlMap = handle.readString();
 		} catch (Exception e) {
 			System.out.println("Error de carga de fichero");
 		}
 	}	
 	
 	private void loadName(){
 		mapName = getData(xmlMap, "Name");
 	}
 	
 	private void loadUpgrades(){
 		String xmlUpgrades = getData(xmlMap, "Upgrades");
 		
 		maxBootUpgrades = Integer.parseInt(getData(xmlUpgrades, "Boots"));
 		maxRangeUpgrades = Integer.parseInt(getData(xmlUpgrades, "MaxRange"));
 		maxNumHarpoonsUpgrades = Integer.parseInt(getData(xmlUpgrades, "NumLances"));
 		maxThrowUpgrades = Integer.parseInt(getData(xmlUpgrades, "Throw"));			
 	}
 	
 	private void loadBoxes(){
 		String xmlBoxes = getData(xmlMap, "Boxes");
 		String box;
 		
 		while (xmlBoxes.contains("Breakable")){
 			box = getData(xmlBoxes, "Breakable");
 			loadBox(box, TypeSquare.breakable);
 			xmlBoxes = xmlBoxes.replaceFirst("<Breakable>".concat(box).concat("</Breakable>"), "");
 		}	
 		while (xmlBoxes.contains("Unbreakable")){
 			box = getData(xmlBoxes, "Unbreakable");
 			loadBox(box, TypeSquare.unbreakable);
 			xmlBoxes = xmlBoxes.replaceFirst("<Unbreakable>".concat(box).concat("</Unbreakable>"), "");
 		}
 		while (xmlBoxes.contains("Empty")){
 			box = getData(xmlBoxes, "Empty");
 			loadBox(box, TypeSquare.empty);
 			xmlBoxes = xmlBoxes.replaceFirst("<Empty>".concat(box).concat("</Empty>"), "");
 		}
 	}
 	
 	private void loadBox(String box, TypeSquare type){
 		int dash = box.indexOf("-");
 		int x = Integer.parseInt(box.substring(0, dash));
 		int y = Integer.parseInt(box.substring(dash+1, box.length()));
 		if (x < width && y < length && x >= 0 && y >=0) boardGame[x][y] = type;
 	}
 	
 
 	
 	
 	//METHODS OF PUT FISSURE
 	
 	public void putHarpoonAt(int xHarpoonPosition, int yHarpoonPosition) {
 		boardGame[xHarpoonPosition][yHarpoonPosition]= TypeSquare.Harpoon;
 	}
 
 	public void addAllFissures(ArrayList<Harpoon> harpoonList){
 		removeAllFissures();
 		Iterator<Harpoon> it = harpoonList.iterator(); 
 		while (it.hasNext()){
 			Harpoon myHarpoon = (Harpoon) it.next();
 			putfissureAt((int)myHarpoon.getPosition().x,(int)myHarpoon.getPosition().y,myHarpoon.getRange());
 		}
 	}
 	
 	public void putfissureAt(int xHarpoonPosition, int yHarpoonPosition, int fissureRange) {
 		//The fissure center is in the same position that the harpoon
 		if(fissuresBoard[xHarpoonPosition][yHarpoonPosition]!=FissuresTypes.fissureC)
 			fissuresBoard[xHarpoonPosition][yHarpoonPosition]= FissuresTypes.fissureC;
 		//The fissure will be draw in 4 directions while the field in the board game isn't unbreakable
 		// always less or equal than fissureRange, if there are fields with breakable squares this fields 
 		// will be draw like empty fields
 		int i = 1;
 		boolean blocked = false;
 		//NORTH
 		while (!blocked && (yHarpoonPosition+i<length) && i<=fissureRange && canIputFissure(xHarpoonPosition,yHarpoonPosition+i)){
 			if(fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] == FissuresTypes.fissureSX)
 					fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] = FissuresTypes.crossingFissures;
 			else if (boardGame[xHarpoonPosition][yHarpoonPosition+i] == TypeSquare.empty)
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] = FissuresTypes.fissureSY;
 			else if (boardGame[xHarpoonPosition][yHarpoonPosition+i] == TypeSquare.breakable){
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] = FissuresTypes.barrelWithFissure;
 				blocked = true;
 			}
 			i++;
 		}
 		i = 1; blocked = false;
 		
 		//SOUTH
 		while (!blocked && yHarpoonPosition-i>=0 && i<=fissureRange && canIputFissure(xHarpoonPosition,yHarpoonPosition-i)){
 			if(fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] == FissuresTypes.fissureSX)
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] = FissuresTypes.crossingFissures;
 			else if (boardGame[xHarpoonPosition][yHarpoonPosition-i] == TypeSquare.empty)
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] = FissuresTypes.fissureSY;
 			else if (boardGame[xHarpoonPosition][yHarpoonPosition-i] == TypeSquare.breakable){
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] = FissuresTypes.barrelWithFissure;
 				blocked = true;
 			}
 		i++;
 		}
 		i = 1; blocked = false;
 		//EAST
 		while (!blocked && (xHarpoonPosition+i<width) && i<=fissureRange && canIputFissure(xHarpoonPosition+i,yHarpoonPosition)){
 			if(fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] == FissuresTypes.fissureSY)
 				fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] = FissuresTypes.crossingFissures;
 			else if (boardGame[xHarpoonPosition+i][yHarpoonPosition] == TypeSquare.empty)
 				fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] = FissuresTypes.fissureSX;
 			else if (boardGame[xHarpoonPosition+i][yHarpoonPosition] == TypeSquare.breakable){
 				fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] = FissuresTypes.barrelWithFissure;
 				blocked = true;
 			}
 		i++;
 		}
 		i = 1; blocked = false;
 		//WEST
 		while (!blocked && (xHarpoonPosition-i>=0) && i<=fissureRange && canIputFissure(xHarpoonPosition-i,yHarpoonPosition)){
 			if(fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] == FissuresTypes.fissureSY)
 				fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] = FissuresTypes.crossingFissures;
 			else if (boardGame[xHarpoonPosition-i][yHarpoonPosition] == TypeSquare.empty)
 		 		fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] = FissuresTypes.fissureSX;
 			else if (boardGame[xHarpoonPosition-i][yHarpoonPosition] == TypeSquare.breakable){
 				fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] = FissuresTypes.barrelWithFissure;
 				blocked = true;
 			}
 		i++;
 		}
 }
 	
 	private boolean canIputFissure(int xHarpoonPosition,	int yHarpoonPosition){
 		return (boardGame[xHarpoonPosition][yHarpoonPosition] == TypeSquare.empty 
 				|| boardGame[xHarpoonPosition][yHarpoonPosition] == TypeSquare.breakable);
 	}
 	
 	// END METHODS OF PUT FISSURE
 	
 	
 	//METHODS OF PUT WATER
 		
 	public void putSunkenHarpoonAt(int xHarpoonPosition, int yHarpoonPosition) {
 		boardGame[xHarpoonPosition][yHarpoonPosition]= TypeSquare.empty;
 	}
 	
 	public void paintAllWaters(ArrayList<Harpoon> sunkenHarpoonList){
 		removeAllWater();
 		Iterator<Harpoon> it = sunkenHarpoonList.iterator(); 
 		while (it.hasNext()){
 			Harpoon mySunkenHarpoon = (Harpoon) it.next();
 			putWaterAt((int)mySunkenHarpoon.getPosition().x,(int)mySunkenHarpoon.getPosition().y,mySunkenHarpoon.getRange());
 		}
 	}	
 	
 	private void removeAllFissures(){
 		for (int i=0;i<length;i++){
 			for (int j=0;j<width;j++){
 				fissuresBoard[i][j]=FissuresTypes.empty;
 			}
 		}
 	}
 
 	private void removeAllWater() {
 		for (int i=0;i<length;i++){
 			for (int j=0;j<width;j++){
 				waterBoard[i][j]=WaterTypes.empty;
 			}
 		}
 	}
 	
 	public void putWaterAt(int xHarpoonPosition, int yHarpoonPosition, int fissureRange) {
 		String connectingSidesPos;
 		// Fissure center <= Water Center
 		if(fissuresBoard[xHarpoonPosition][yHarpoonPosition] == FissuresTypes.fissureC){
 			connectingSidesPos = ConnectingSidesPosition(xHarpoonPosition, yHarpoonPosition, fissureRange);
 			waterBoard[xHarpoonPosition][yHarpoonPosition] = getWaterPiece(connectingSidesPos);
 		}
 		int i = 1; boolean blocked = false;
 		//
 		//NORTH
 		blocked = false;
 		while (!blocked && (yHarpoonPosition+i<length) && i<=fissureRange && ((fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] == FissuresTypes.fissureSY)
 				||(fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] == FissuresTypes.barrelWithFissure))){
 			if (fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] == FissuresTypes.fissureSY){
 				connectingSidesPos = ConnectingSidesPosition(xHarpoonPosition, yHarpoonPosition+i, fissureRange);
 				waterBoard[xHarpoonPosition][yHarpoonPosition+i] = getWaterPiece(connectingSidesPos);
 			}else if (fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] == FissuresTypes.barrelWithFissure){
 				// Upgrades not included yet. Barrel will be empty.
 				fissuresBoard[xHarpoonPosition][yHarpoonPosition+i] = FissuresTypes.empty;
 				//When upgrades are implemented boardGame = upgrade
 				boardGame[xHarpoonPosition][yHarpoonPosition+i] = TypeSquare.empty;
 				blocked = true;
 			}
 			i++;
 		} 
 		i = 1;
 		
 		//SOUTH
 		blocked = false;
 		while (!blocked && yHarpoonPosition-i>=0 && i<=fissureRange && ((fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] == FissuresTypes.fissureSY)
 				||(fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] == FissuresTypes.barrelWithFissure))){
 				if (fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] == FissuresTypes.fissureSY){
 					connectingSidesPos = ConnectingSidesPosition(xHarpoonPosition, yHarpoonPosition-i, fissureRange);
 					waterBoard[xHarpoonPosition][yHarpoonPosition-i] = getWaterPiece(connectingSidesPos);
 				}else if (fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] == FissuresTypes.barrelWithFissure){
 					// Upgrades not included yet. Barrel will be empty.
 					fissuresBoard[xHarpoonPosition][yHarpoonPosition-i] = FissuresTypes.empty;
 					//When upgrades are implemented boardGame = upgrade
 					boardGame[xHarpoonPosition][yHarpoonPosition-i] = TypeSquare.empty;
 					blocked = true;
 				}
 				i++;
 			}
 			i = 1;
 		
 		//EAST
 		blocked = false;
 		while (!blocked && (xHarpoonPosition+i<width) && i<=fissureRange	&& ((fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] == FissuresTypes.fissureSX)
 				||(fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] == FissuresTypes.barrelWithFissure))){
 			if (fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] == FissuresTypes.fissureSX){
 				connectingSidesPos = ConnectingSidesPosition(xHarpoonPosition+i, yHarpoonPosition, fissureRange);
 				waterBoard[xHarpoonPosition+i][yHarpoonPosition] = getWaterPiece(connectingSidesPos);
 			}else if (fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] == FissuresTypes.barrelWithFissure){
 				// Upgrades not included yet. Barrel will be empty.
 				fissuresBoard[xHarpoonPosition+i][yHarpoonPosition] = FissuresTypes.empty;
 				//When upgrades are implemented boardGame = upgrade
 				boardGame[xHarpoonPosition+i][yHarpoonPosition] = TypeSquare.empty;
 				blocked = true;
 			}
 			i++;
 		}
 		i = 1;
 		//WEST
 		blocked = false;
 		while (!blocked && (xHarpoonPosition-i>=0) && i<=fissureRange	&& ((fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] == FissuresTypes.fissureSX)
 				||(fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] == FissuresTypes.barrelWithFissure))){
 			if (fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] == FissuresTypes.fissureSX){
 				connectingSidesPos = ConnectingSidesPosition(xHarpoonPosition-i, yHarpoonPosition, fissureRange);
 				waterBoard[xHarpoonPosition-i][yHarpoonPosition] = getWaterPiece(connectingSidesPos);
 			}else if (fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] == FissuresTypes.barrelWithFissure){
 				// Upgrades not included yet. Barrel will be empty.
 				fissuresBoard[xHarpoonPosition-i][yHarpoonPosition] = FissuresTypes.empty;
 				//When upgrades are implemented boardGame = upgrade
 				boardGame[xHarpoonPosition-i][yHarpoonPosition] = TypeSquare.empty;
 				blocked = true;
 			}
 			i++;
 		}	
 	}
 	
 	private WaterTypes getWaterPiece (String connectingSidesPos){	
 			if (connectingSidesPos.equals("N")){return WaterTypes.water1SOpN;}
 			else if (connectingSidesPos.equals("S")){return WaterTypes.water1SOpS;}
 			else if (connectingSidesPos.equals("E")){return WaterTypes.water1SOpE;}
 			else if (connectingSidesPos.equals("W")){return WaterTypes.water1SOpW;}
 			else if (connectingSidesPos.equals("EW")){return WaterTypes.water2SOpBridgeX;}
 			else if (connectingSidesPos.equals("NS")){return WaterTypes.water2SOpBridgeY;}
 			else if (connectingSidesPos.equals("NE")){return WaterTypes.water2SOpCornerEN;}
 			else if (connectingSidesPos.equals("NW")){return WaterTypes. water2SOpCornerNW;}
 			else if (connectingSidesPos.equals("SE")){return WaterTypes.water2SOpCornerSE;}
 			else if (connectingSidesPos.equals("SW")){return WaterTypes.water2SOpCornerWS;}
 			else if (connectingSidesPos.equals("NSW")){return WaterTypes.water3SOpE;}
 			else if (connectingSidesPos.equals("SEW")){return WaterTypes.water3SOpN;}
 			else if (connectingSidesPos.equals("NEW")){return WaterTypes.water3SOpS;}
 			else if (connectingSidesPos.equals("NSE")){return WaterTypes.water3SOpW;}
 			else if (connectingSidesPos.equals("NSEW")){return WaterTypes.water4SOp;}		
 	return WaterTypes.empty;
 	}
 
 
 	private boolean canIputWater(int xIni,int yIni,int xFin,int yFin){	
 		//The new field is in the East. we have to check that this field is a fissure or is a water block with a connection in the west side
 		if(xIni < xFin){
 			return(fissuresBoard[xFin][yFin] == FissuresTypes.fissureSX || waterBoard[xFin][yFin] == WaterTypes.water1SOpW 
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerNW || waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerWS
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpBridgeX || waterBoard[xFin][yFin] == WaterTypes.water3SOpE 
 					|| waterBoard[xFin][yFin] == WaterTypes.water3SOpN || waterBoard[xFin][yFin] == WaterTypes.water3SOpS 
 					|| waterBoard[xFin][yFin] == WaterTypes.water4SOp);
 		
 		//The new field is in the West. we have to check that this field is a fissure or is a water block with a connection in the east side
 		}else if(xIni > xFin){
 			return(fissuresBoard[xFin][yFin] == FissuresTypes.fissureSX || waterBoard[xFin][yFin] == WaterTypes.water1SOpE 
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerEN || waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerSE
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpBridgeX || waterBoard[xFin][yFin] == WaterTypes.water3SOpW 
 					|| waterBoard[xFin][yFin] == WaterTypes.water3SOpN || waterBoard[xFin][yFin] == WaterTypes.water3SOpS 
 					|| waterBoard[xFin][yFin] == WaterTypes.water4SOp);
 				
 		//The new field is in the South. we have to check that this field is a fissure or is a water block with a connection in the north side
 		}else if(yIni > yFin){
 			return(fissuresBoard[xFin][yFin] == FissuresTypes.fissureSY || waterBoard[xFin][yFin] == WaterTypes.water1SOpN
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerNW || waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerEN
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpBridgeY || waterBoard[xFin][yFin] == WaterTypes.water3SOpE 
 					|| waterBoard[xFin][yFin] == WaterTypes.water3SOpW || waterBoard[xFin][yFin] == WaterTypes.water3SOpS 
 					|| waterBoard[xFin][yFin] == WaterTypes.water4SOp);
 	
 		//The new field is in the North. we have to check that this field is a fissure or is a water block with a connection in the south side
 		}else if(yIni < yFin){
 			return(fissuresBoard[xFin][yFin] == FissuresTypes.fissureSY || waterBoard[xFin][yFin] == WaterTypes.water1SOpS
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerSE || waterBoard[xFin][yFin] == WaterTypes.water2SOpCornerWS
 					|| waterBoard[xFin][yFin] == WaterTypes.water2SOpBridgeY || waterBoard[xFin][yFin] == WaterTypes.water3SOpE 
 					|| waterBoard[xFin][yFin] == WaterTypes.water3SOpW || waterBoard[xFin][yFin] == WaterTypes.water3SOpN 
 					|| waterBoard[xFin][yFin] == WaterTypes.water4SOp);
 		}		
 	return true;
 	}
 
 	private String ConnectingSidesPosition(int xHarpoonPosition, int yHarpoonPosition, int fissureRange) {
 		String positions = "";
 		//NORTH
 		if(yHarpoonPosition+1<length && canIputWater(xHarpoonPosition, yHarpoonPosition, xHarpoonPosition, yHarpoonPosition+1)) 
 			positions = positions.concat("N");
 		//SOUTH
 		if(yHarpoonPosition-1>=0 && canIputWater(xHarpoonPosition, yHarpoonPosition, xHarpoonPosition, yHarpoonPosition-1)) 
 			positions = positions.concat("S");
 		//EAST
 		if(xHarpoonPosition+1 < width && canIputWater(xHarpoonPosition, yHarpoonPosition, xHarpoonPosition+1, yHarpoonPosition))
 			positions = positions.concat("E");
 		//WEST
 		if(xHarpoonPosition-1>=0 && canIputWater(xHarpoonPosition, yHarpoonPosition, xHarpoonPosition-1, yHarpoonPosition))
 			positions = positions.concat("W");
 	return positions;
 	}
 //END OF METHODS PUT WATER
 
 	
 //METHOD DELETE SUNKEN OBJECT
 	/**
 	 * This method delete sunkenObjects image in sunkenBoard
 	 * along harpoon range only if waterBoard isn't empty.
 	 * @param harpoon
 	 */
 		public void emptyHarpoonPosInSunkenMatrix(Harpoon harpoon) {
 		//Parameters declaration for simplify the code
 			//Harpoon Range
 			int range = harpoon.getRange();
 			//West Delimiter: 
 				//If the harpoon center - range is less than bound of board game
 				//the X initial position range in board will be 0
 			int xIni = (int)harpoon.getPosition().x-range;
 			if (xIni < 0) xIni = 0;
 			//North Delimiter: 
 				//If the harpoon center - range is less than bound of board game
 				//the Y initial position range in board will be 0
 			int yIni = (int)harpoon.getPosition().y-range;
 			if (yIni < 0) yIni = 0;
 			//East Delimiter: 
 				//If the harpoon center + range is great than bound of board game
 				//the X initial position range in board will be width
 			int xFin = (int)harpoon.getPosition().x+range;
 			if (xFin >= width) xFin = width-1;
 			//South Delimiter: 
 				//If the harpoon center + range is great than bound of board game
 				//the Y initial position range in board will be length
 			int yFin = (int)harpoon.getPosition().y+range;
 			if (yFin >= length) yFin = length-1;
 			
 			//Range harpoon spanning when I delete sunkenObject image of sunkenBoard
 			//only if there are water in waterBoard in the same position
 			for(int i=xIni; i<=xFin; i++)
 				for(int j=yIni; j<=yFin; j++)
					//if(waterBoard[i][j] != WaterTypes.empty && sunkenBoard[i][j] == SunkenTypes.sunkenObject)
 						sunkenBoard[i][j] = SunkenTypes.empty;
 		}
 	
 //END OF METHOD DELETE SUNKEN OBJECT
 
 	public void sunkenObject(int x, int y){
 		sunkenBoard[x][y]= SunkenTypes.sunkenObject;
 	}	
 	
 	public boolean canRunThrough(int x, int y) {
 		boolean canRunThrough = false;
 		if (x>=0 && x<width && y >= 0 && y<length){
 			canRunThrough = !(boardGame[x][y].equals(TypeSquare.unbreakable) || 
 					boardGame[x][y].equals(TypeSquare.breakable));
 		}
 		return canRunThrough;
 	}
 	
 // Getters and Setters
 	
 	public TypeSquare getBasicMatrixSquare(int x,int y) {
 		return boardGame[x][y];
 	}
 	
 	public FissuresTypes getFissureMatrixSquare(int x,int y) {
 		return fissuresBoard[x][y];
 	}
 	
 	public WaterTypes getWaterMatrixSquare(int x,int y) {
 		return waterBoard[x][y];
 	}
 	
 
 	public SunkenTypes getSunkenMatrixSquare(int x, int y) {
 		return sunkenBoard[x][y];
 	}
 	
 	public int getMaxBootUpgrades() {
 		return maxBootUpgrades;
 	}
 	public void setMaxBootUpgrades(int maxBootUpgrades) {
 		this.maxBootUpgrades = maxBootUpgrades;
 	}
 	public int getMaxRangeUpgrades() {
 		return maxRangeUpgrades;
 	}
 	public void setMaxRangeUpgrades(int maxRangeUpgrade) {
 		this.maxRangeUpgrades = maxRangeUpgrade;
 	}
 	public int getMaxNumHarpoonsUpgrades() {
 		return maxNumHarpoonsUpgrades;
 	}
 	public void setMaxNumHarpoonsUpgrades(int maxNumHarpoonsgrades) {
 		this.maxNumHarpoonsUpgrades = maxNumHarpoonsgrades;
 	}
 	public int getMaxThrowUpgrades() {
 		return maxThrowUpgrades;
 	}
 	public void setMaxThrowUpgrades(int maxThrowUpgrades) {
 		this.maxThrowUpgrades = maxThrowUpgrades;
 	}
 	public int getLength() {
 		return length;
 	}
 	public int getWidth() {
 		return width;
 	}
 	public boolean isEmptySquare(int x, int y) {
 		return (boardGame[x][y]==TypeSquare.empty);
 	}		
 	public String getMapName() {
 		return mapName;
 	}
 	public void setMapName(String mapName) {
 		this.mapName = mapName;
 	}
 }
