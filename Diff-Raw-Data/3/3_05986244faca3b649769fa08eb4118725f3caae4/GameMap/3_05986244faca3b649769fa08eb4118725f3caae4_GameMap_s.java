 package mobile.team4.game;
 
 import mobile.team4.game.GameObject.Type;
 
 public class GameMap {
 	private static GameObject[][] map;
 	
 	public enum Pieces {
 		CannonTopLeft, CannonTopRight, CannonBottomLeft, CannonBottomRight, 
 		CastleTopLeft, CastleTopRight, CastleBottomLeft, CastleBottomRight,
 		Wall, Floor, Grass, Water					
 	}
 	
 	public void print_map() {
 		for (int row = 0; row < map.length; row++) {
 			for (int col = 0; col < map[row].length; col++) {
 		        System.out.print(map[row][col] + " ");
 			}
 			System.out.println();
 		}
 	}
 	
 	GameMap (int rows, int cols) {
 		map = new GameObject[cols][rows];
 		
 		// do i actually need to initialize to zero
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				map[j][i] = new BackgroundPiece(GameObject.Type.Grass, new Point(i, j));
 			}
 		}
 		
 	}
 	
 	/*
 	public void placeWall(WallPiece piece, Point location) {
 		// using 1 to marked filled, change to represent wall pieces 
 		// vs cannon vs filled floor, etc
 		for (Point point : piece.shape) {
 			map[location.x + point.x][location.y + point.y] = 1;
 		}
 	}
 	*/
 	
 	public void placeCannon(Cannon cannon, Point location) {
 		// FUCKing recognize this change and push to github please
 	}
 	
 	public static GameObject[][] getMap()
 	{
 		return (map);
 	}
 	public void placeWall(Point location, Shape shape) {
 		for (Point point : shape.points) {
			if (get_at(point).getType() != Type.Grass) {
 				return;
 			}
 		}
 		
 		for (Point point : shape.points) {
 			Point p = new Point(point.get_x() + location.get_x(), point.get_y() + location.get_y());
 			insert_at(p, new WallPiece(p));
 		}
 	}
 	
 	public void insert_at(Point position, GameObject object) {
 		object.setPosition(position);
 		map[position.get_y()][position.get_x()] = object;
 	}
 	
 	public void insert_at(int x, int y, GameObject object) {
 		//object.setPosition(x, y);
 		map[y][x] = object;
 	}
 	
 	public int getWidth() {
 		return map.length;
 	}
 	
 	public int getHeight() {
 		return map[0].length;
 	}
 	
 	public GameObject get_at(int x, int y)
 	{
 		return (map[y][x]);
 	}
 	
 	public GameObject get_at(Point p)
 	{
 		return (map[p.get_y()][p.get_x()]);
 	}
 
 }
 
