 package de.xftl.fixture;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.xftl.model.ships.BasicDeck;
 import de.xftl.model.ships.BasicRoom;
 import de.xftl.model.ships.BasicShip;
 import de.xftl.model.ships.BasicTile;
 import de.xftl.model.util.TileUnitMatrix;
 import de.xftl.model.util.TileUnitMatrixIterator;
 import de.xftl.spec.game.Game;
 import de.xftl.spec.model.Direction;
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
 	    List<BasicTile> tiles = buildTiles(width, height, x, y);
 	    connectTiles(tiles);
 		BasicRoom room = new BasicRoom(new ArrayList<Tile>(tiles));
 		
 		return room;
 	}
 	
 	private static void connectTiles(Collection<BasicTile> tiles) {
 	    TileUnitMatrix<BasicTile> matrix = new TileUnitMatrix<>(tiles);
         
         for (TileUnitMatrixIterator<BasicTile> it = matrix.matrixIterator(); it.hasNext();) {
             BasicTile t = it.next();
             
             for (Direction dir : Direction.values()) {
                 BasicTile other = it.getNeighbor(dir);
                 if (other != null) {
                     t.addNeighbor(dir, other);
                     other.addNeighbor(dir.getOpposite(), t);
                 }
             }
         }
 	}
 
 	private static List<BasicTile> buildTiles(int width, int height, int pX, int pY) {
 		List<BasicTile> tiles = new ArrayList<>(width * height);
 		
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
		final int MATRIX_Y = 6;
 		
		Tile[][] matrix = new Tile[MATRIX_Y + 1][MATRIX_X + 1];
 		
 		for (Room room : ship.getDecks().get(0).getRooms()) {
 			for (Tile tile : room.getTiles()) {
 				Point<TileUnit> pos = tile.getLeftUpperCornerPos();
 				int x = pos.getX().getValue();
 				int y = pos.getY().getValue();
 				
 				matrix[y][x] = tile;
 			}
 		}
 		
		System.out.print("  |");
		for (int x = 0; x < MATRIX_X; x++)
			System.out.print(String.format("%d", x));
		System.out.println();
		System.out.print("--|");
		for (int x = 0; x < MATRIX_X; x++)
			System.out.print("-");
		System.out.println();
		
 		for (int y = 0; y < MATRIX_Y; y++) {
			System.out.print(String.format("%d |", y));
 			for (int x = 0; x < MATRIX_X; x++) {
 				System.out.print(matrix[y][x] == null ? ' ' : 'X');
 			}
 			System.out.println("");
 		}
 	}
 }
