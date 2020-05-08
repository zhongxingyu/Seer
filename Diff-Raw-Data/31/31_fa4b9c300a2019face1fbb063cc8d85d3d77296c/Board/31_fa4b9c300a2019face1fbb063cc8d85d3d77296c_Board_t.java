 package entanglement.engine;
 
 import java.io.Reader;
 import java.util.Random;
 
 import entanglement.utils.Config;
 
 public class Board implements BoardInterface{
 	
 	Tile[] tiles;
 	Tile currentTile;
 	Player[] players;
 	Player currentPlayer;
 	int currentLocation;
 	
 	public Board(Reader reader) {
 		Config.inst().load(reader);
 		
 		tiles = new Tile[Config.inst().boardWidth() * Config.inst().boardHeight()];
 		currentTile = new Tile(Config.inst().tileConf((new Random()).nextInt(Config.inst().tileTypesCount())));
 		
 		players = new Player[Config.inst().playersCount()];
 		for (int i = 0;i < Config.inst().playersCount();i++)
 			players[i] = new Player(i);
 		currentPlayer = players[0];
 		
 		currentLocation = Config.inst().startLocation() - Config.inst().boardWidth();
 	}
 	
 	@Override
 	public boolean fixTile() {
 		return false;
 	}
 
 	@Override
 	public int getScore(int player) {
 		return 0;
 	}
 
 	@Override
 	public boolean isGameOver(int player) {
 		return false;
 	}
 
 	@Override
 	public boolean rotateTileAntiClockwise() {
 		currentTile.rotateAntiClockWise();
 		return true;
 	}
 
 	@Override
 	public boolean rotateTileClockwise() {
 		currentTile.rotateClockWise();
 		return true;
 	}
 
 	@Override
 	public boolean switchTile(int tileType) {
		if(tileType < 0 || tileType > Config.inst().tileTypesCount()-1)
			return false;
 		currentTile.setTileConf(Config.inst().tileConf(tileType));
 		return true;
 	}
 }
