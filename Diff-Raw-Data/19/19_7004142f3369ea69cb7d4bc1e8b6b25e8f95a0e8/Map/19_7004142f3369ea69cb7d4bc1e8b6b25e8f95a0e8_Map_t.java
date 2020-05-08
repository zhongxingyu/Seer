 package GameLogic;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.files.FileHandle;
 
 public class Map {
 	
 	//Type of a square in the boardGame
 	
 	public enum TypeSquare{empty,unbreakable,breakable,bootUpgrade,rangeUpgrade,numHarpoonUpgrade,
 		throwUpgrade,Harpoon}
 	
 	//Total number of each Upgrade
 	
 	private int maxBootUpgrades;
 	private int maxRangeUpgrades;
 	private int maxNumLancesUpgrades;
 	private int maxThrowUpgrades;
 	
 	//Board Attributes
 	
 	private String mapName;
 	private int length;
 	private int width;
 	private TypeSquare[][] boardGame;
 	
 	//LoadFile
 	
 	private String xmlMap;
 	
 	public Map(int lenght, int width, String mapPath) {
 		this.length=lenght;
 		this.width=width;
 		this.boardGame = new TypeSquare[lenght][width];
 		for (int i=0;i<lenght;i++){
 			for (int j=0;j<width;j++){
 				if ((i % 2 == 1) && (j % 2 == 1)) boardGame[i][j] = TypeSquare.unbreakable;
 				else boardGame[i][j] = TypeSquare.empty;
 			}
 		}
 		loadMap(mapPath);
 	}
 
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
 			maxNumLancesUpgrades = 8;
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
 		maxNumLancesUpgrades = Integer.parseInt(getData(xmlUpgrades, "NumLances"));
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
 		if (x <= length && y <= width) boardGame[x][y] = type;
 	}
 	
 	public TypeSquare getposition(int squareX,int squareY) {
 		return boardGame[squareX][squareY];
 	}
 	
 	public void putLanceAt(int xLancePosition, int yLancePosition) {
 		boardGame[xLancePosition][yLancePosition]= TypeSquare.Harpoon;
 	}
 	
 	
 	// Getters and Setters
 	
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
 	public int getMaxNumLancesUpgrades() {
 		return maxNumLancesUpgrades;
 	}
 	public void setMaxNumLancesUpgrades(int maxNumLancesgrades) {
 		this.maxNumLancesUpgrades = maxNumLancesgrades;
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
 	public void setLength(int length) {
 		this.length = length;
 	}
 	public int getWidth() {
 		return width;
 	}
 	public void setWidth(int width) {
 		this.width = width;
 	}
 	public TypeSquare[][] getBoardGame() {
 		return boardGame;
 	}
 	public void setBoardGame(TypeSquare[][] boardGame) {
 		this.boardGame = boardGame;
 	}
 
 	public boolean isEmptySquare(int x, int y) {
 		return (boardGame[x][y]==TypeSquare.empty);
 	}	
 }
