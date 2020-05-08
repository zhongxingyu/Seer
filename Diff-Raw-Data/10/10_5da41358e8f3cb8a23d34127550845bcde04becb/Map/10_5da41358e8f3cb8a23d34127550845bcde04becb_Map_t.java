 package bot;
 
 public class Map {
 
 	private Position myPosition;
 	private int height;
 	private int width;
 	private Square[][] content;
 	
 	public Map(){
 		content = null;
 		myPosition = null;
 		height = -1;
 		width = -1;
 	}
 
 	public Map(char[][] map) throws UnknownPositionException{
 		content = new Square[map.length][map[0].length];
 		for (int line = 0; line < map.length ; line++){
 			for (int column = 0; column < map[line].length; column++){
 				char c = map[line][column];
 				if (c == Protocole.PLAYER_TOKEN)
 					myPosition = new Position(line, column);
 				content[line][column] = new Square(map[line][column]);
 			}
 		}
 		if (myPosition == null)
 			throw new UnknownPositionException("Position not specified in message");
 	}
 
 	public Map(int height, int width) throws UnknownPositionException{
 		this.height = height;
 		this.width = width;
 		initMap();
 	}
 	
 	// initialize the Map according to a lazy mechanism
 	private void initMap(){
 		if (height < 0 || width < 0)
 			throw new RuntimeException("Map size indicators are not valid");
 		if (content != null)
 			return;
 		content = new Square[height][width];
 		for (int line = 0; line < height ; line++){
 			for (int column = 0; column < width; column++){
 				content[line][column] = new Square(SquareType.UNKNOWN.getToken());
 			}
 		}
 		myPosition = null;
 		
 	}
 	
 	public boolean isAllowedMove(Direction d){
 		Square dest = getSquare(Position.add(myPosition, d));
 		// if dest is out of map, is a door or is out of sight, move is forbidden
 		if (dest == null ||
 			dest.getType() == SquareType.CLOSED_DOOR ||
 			dest.getType() == SquareType.UNKNOWN)
 			return false;
 		switch (d){
 		case NORTH:
 		case SOUTH:
 			return (dest.getType() != SquareType.HORIZONTAL_WALL);
 		case EAST:
 		case WEST:
 			return (dest.getType() != SquareType.VERTICAL_WALL);
 		case NORTH_EAST:
 		case NORTH_WEST:
 		case SOUTH_WEST:
 		case SOUTH_EAST:
 			return (dest.getType() != SquareType.VERTICAL_WALL &&
 					dest.getType() != SquareType.HORIZONTAL_WALL);
 		default: return false;
 		}
 	}
 	
 	public boolean isAllowedOpen(Direction d){
 		Square dest = getSquare(Position.add(myPosition, d));
 		// if dest is out of map, is a door or is out of sight, move is forbidden
		return (dest == null ||
						dest.getType() == SquareType.CLOSED_DOOR);
 	}
 	
 	public Square getSquare(Position p){
 		int line = p.getLine();
 		if (line >= content.length ||
 			line < 0)
 			return null;
 		int col = p.getColumn();
 		if (col >= content[line].length ||
 			col < 0)
 			return null;
 		return content[line][col];
 	}
 	
 	public Position getPlayerPosition(){
 		return myPosition;
 	}
 	
 	public void updateSquare(int line, int col, char newVal){
 		if (newVal == Protocole.PLAYER_TOKEN)
 			myPosition = new Position(line, col);
 		content[line][col] = new Square(newVal);
 	}
 	
 	public void updateSize(int height, int width){
 		this.width = width;
 		this.height = height;
 		initMap();
 	}
 	
 	public boolean isKnownPosition(){
 		return myPosition != null;
 	}
 	
 	public String toString(){
 		StringBuffer sb = new StringBuffer();
 		sb.append("myPosition : " + myPosition);
 		for (int line = 0; line < content.length ; line++){
 			for (int column = 0; column < content[line].length; column++)
 				if (myPosition.getLine() == line &&
 					myPosition.getColumn() == column)
 					sb.append("@");
 				else
 					sb.append(content[line][column].toString());
 			sb.append('\n');
 		}
 		return sb.toString();
 	}
 
 }
