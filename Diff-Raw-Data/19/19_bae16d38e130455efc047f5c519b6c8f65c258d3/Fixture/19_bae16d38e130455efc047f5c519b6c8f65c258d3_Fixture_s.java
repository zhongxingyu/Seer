 package de.xftl.fixture;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.xftl.model.ships.BasicDeck;
 import de.xftl.model.ships.BasicRoom;
 import de.xftl.model.ships.BasicShip;
 import de.xftl.model.ships.BasicTile;
 import de.xftl.spec.game.Game;
 import de.xftl.spec.model.Point;
 import de.xftl.spec.model.ships.Deck;
 import de.xftl.spec.model.ships.DeckNumber;
 import de.xftl.spec.model.ships.Room;
 import de.xftl.spec.model.ships.Ship;
 import de.xftl.spec.model.ships.Tile;
 import de.xftl.spec.model.ships.TileUnit;
 
 public class Fixture {
 	
 	public static Game buildGame() {
 		Game game = new FixtureGame();
 		
 		return game;
 	}
 	
 	public static Ship buildShip() {
 		BasicShip ship = new BasicShip();
 		
 		ship.addDeck(buildDeck(1));
 		
 		return ship;
 	}
 
 	private static Deck buildDeck(int number) {
 		BasicDeck deck = new BasicDeck(new DeckNumber(number));
 		
 		deck.addRoom(buildRoom(2, 2, 0, 1));
 		deck.addRoom(buildRoom(2, 2, 2, 1));
 		deck.addRoom(buildRoom(2, 1, 1, 0));
 		deck.addRoom(buildRoom(2, 1, 1, 3));
 		deck.addRoom(buildRoom(2, 2, 4, 1));
 		deck.addRoom(buildRoom(1, 2, 5, 1));
 		
 		return deck;
 	}
 
 	private static Room buildRoom(int width, int height, int x, int y) {
		BasicRoom room = new BasicRoom(buildTiles(width, height, x, y));
 		
 		return room;
 	}
 
	private static List<Tile> buildTiles(int width, int height, int pX, int pY) {
		List<Tile> tiles = new ArrayList<>(width * height);
 		
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				TileUnit tux = new TileUnit(x + pX);
 				TileUnit tuy = new TileUnit(y + pY);
 				BasicTile tile = new BasicTile(new Point<TileUnit>(tux, tuy));
 				tiles.add(tile);
 			}
 		}
 		
 		return tiles;
 	}
 	
 	public static void main(String[] args) {
 		Ship ship = buildShip();
 		
 		final int MATRIX_X = 10;
 		final int MATRIX_Y = 10;
 		
 		Tile[][] matrix = new Tile[MATRIX_X][MATRIX_Y];
 		
 		for (Room room : ship.getDecks().get(0).getRooms()) {
 			for (Tile tile : room.getTiles()) {
 				Point<TileUnit> pos = tile.getLeftUpperCornerPos();
 				int x = pos.getX().getValue();
 				int y = pos.getY().getValue();
 				
 				matrix[y][x] = tile;
 			}
 		}
 		
 		for (int y = 0; y < MATRIX_Y; y++) {
 			for (int x = 0; x < MATRIX_X; x++) {
 				System.out.print(matrix[y][x] == null ? ' ' : 'X');
 			}
 			System.out.println("");
 		}
 	}
 }
