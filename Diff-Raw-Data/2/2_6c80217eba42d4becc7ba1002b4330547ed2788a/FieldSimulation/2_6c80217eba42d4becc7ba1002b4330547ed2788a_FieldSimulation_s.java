 package field.simulation;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import robot.DebugBuffer;
 import robot.RobotModel;
 import robot.RobotPool;
 import simulator.ISimulator;
 import sun.security.krb5.internal.LocalSeqNumber;
 
 import field.*;
 import field.fromfile.MazePart;
 
 public class FieldSimulation extends Field {
 
 	private RobotPool robotPool;
 	
 	private ISimulator localSimulator;
 	
 	public FieldSimulation(String path) {
 		this(null, path);
 	}
 	
 	public FieldSimulation(RobotPool robotPool, String path) {
 		setRobotPool(robotPool);
 		try {
 			initialize(path);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void setRobotPool(RobotPool robotPool) {
 		this.robotPool = robotPool;
 	}
 
 	public void setLocalSimulator(ISimulator simulator) {
 		this.localSimulator = simulator;
 	}
 	
 	private WorldCommunicator comm;
 	
 	public void connectToGame(String gameId, String ownId) {
 		try {
 			comm = new PenoHtttpWorldCommunicator(ownId, gameId, robotPool, this);
 			comm.connect();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Map<Integer, TilePosition> barcodeIndex = new HashMap<Integer, TilePosition>();
 	
 	public void initialize(String path) throws NumberFormatException, IOException {
 		FileInputStream fstream = new FileInputStream(path);
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String strLine;
 		int lineNr = 0;
 		int dimX = 0;
 		int dimY = 0;	
 		int currY = 0;		
 		while ((strLine = br.readLine()) != null)   {
 			//System.out.println(""+lineNr);
 			if (lineNr == 0) {
 				String[] dim = strLine.split("( |\t)+");
 				dimX = Integer.parseInt(dim[0]);
 				dimY = Integer.parseInt(dim[1]);	
 				currY = dimY-1;
 			} else {
 				if (!strLine.isEmpty() && !strLine.startsWith("#")) {
 					String[] sections = strLine.split("( |\t)+");
 					for (int i = 0; i < dimX; i++) {
 						//System.out.println("line: "+sections[i]);
 						String[] parts = sections[i].split("\\.");
 						//System.out.println("section: " + sections[i] + " parts " + parts.length);
 						MazePart part = MazePart.getPartFromString(parts[0]);
 						Tile tile = new Tile(i, currY);
 						//System.out.println("x: " + i + " y: " + currY);
 						addTile(tile);
 						if (parts.length >= 3 && !parts[2].isEmpty()) {
 							if (parts[2].equals("V")) {
 								addBall(new Ball(1), tile.getPosition());
 							} else if (parts[2].startsWith("S")) {
 								String start = parts[2];
 								int id = Integer.parseInt(start.substring(1, 2));
 								setStartPos(id, tile.getPosition());
 								String dir = start.substring(2, 3);
 								setStartDir(id, Direction.fromString(dir));
 							} else {
 								Barcode barcode = new Barcode(Integer.parseInt(parts[2]));
 								tile.setBarcode(barcode);
 								barcodeIndex.put(barcode.getDecimal(), tile.getPosition());
 							}
 						}
 						if (parts.length == 2) {
 							if (parts[1].startsWith("S") && parts[1].length() > 1) {
 								String start = parts[1];
 								//System.out.println("tt " + start);
 								int id = Integer.parseInt(start.substring(1, 2));
 								setStartPos(id, tile.getPosition());
 								String dir = start.substring(2, 3);
 								setStartDir(id, Direction.fromString(dir));
 							}
 						}
 						String param = parts.length >= 2 ? parts[1] : "";
 						List<Border> borders = part.getBorders(param, tile);
 						//System.out.println("" + borders + " param: " + param);
 						for (Border border : borders) {
 							if (border instanceof SeesawBorder) {
 								overWriteBorder(border);
 							} else {
 								addBorder(border);
 							}
 						}
 					}
 					currY--;
 				}
 			}
 			lineNr++;
 			
 		}
 		in.close();
 		
 		for (Tile tile : tileMap) {
 			//DebugBuffer.addInfo("a " + tile.getPosition() + " " + hasSeesawBorder(tile) + " " + tile.getBarcode());
 			if (hasSeesawBorder(tile) && tile.getBarcode() != null) {
 				SeesawBorder border = getSeesawBorder(tile);
 				//DebugBuffer.addInfo("b " + tile.getPosition());
 				Barcode barcode = tile.getBarcode();
 				if (barcode.isSeesawDownCode()) {
 					border.setDown();
 				} else if (barcode.isSeesawUpCode()) {
 					border.setUp();
 				}
 			}
 		}
 	}
 	
 	public void update() {
 		Tile tile = getCurrentTile();
 		if (isSeesawTile(tile)) {
 			SeesawBorder border = getSeesawBorder(tile);
 			if (!border.isPassable()) {
 				border.setDown();
 				
 				Direction dir = getDirectionOfSeesawBorder(tile);
 				dir = dir.opposite();
 				TilePosition otherSeesawPos = dir.getPositionInDirection(tile.getPosition());
 				SeesawBorder otherBorder = getSeesawBorder(getTileAt(otherSeesawPos));
 				otherBorder.setUp();
 				//DebugBuffer.addInfo("passed across seesaw: " + model.getPlayerNr());
 			}
 		}
 		/*for (RobotModel model : robotPool.getRobots()) {
 			if (getStartPos(model.getPlayerNr()) != null) {
 				Tile tile = getTileOfPos(model.getCurrTile().getPosition().getX() * 40
 						+ model.getPosition().getPosX()
 						+ getStartPos(model.getPlayerNr()).getX() * 40,
 						model.getCurrTile().getPosition().getY() * 40
 						+ model.getPosition().getPosY()
 						+ getStartPos(model.getPlayerNr()).getY() * 40);
 				if (isSeesawTile(tile)) {
 					SeesawBorder border = getSeesawBorder(tile);
 					if (!border.isPassable()) {
 						border.setDown();
 						DebugBuffer.addInfo("passed across seesaw: " + model.getPlayerNr());
 					}
 				}
 			}
 		}*/
 	}
 	
 	
 	
 	/*
 	 * Simulation methods for sensors
 	 */
 	
 	public static TilePosition convertToTilePosition(double xpos, double ypos) {
 		int xsign = xpos < 0? -1:1;
 		int ysign = ypos < 0? -1:1;
 		int x = (int)(Math.abs(xpos) + (TILE_SIZE / 2)) / TILE_SIZE;
 		int y = (int)(Math.abs(ypos) + (TILE_SIZE / 2)) / TILE_SIZE;
 		return new TilePosition(x*xsign, y*ysign);
 	}
 
 	public Tile getCurrentTile()
 			throws IllegalArgumentException {
 		double xpos = localSimulator.getTDistanceX();
 		double ypos = localSimulator.getTDistanceY();
 		return getTileOfPos(xpos, ypos);
 	}
 	
 	public Tile getTileOfPos(double xpos, double ypos)
 			throws IllegalArgumentException {
 		TilePosition tilePos = convertToTilePosition(xpos, ypos);
 		return tileMap.getObjectAtId(tilePos);
 	}
 	
 	public static double[] convertToInTilePos(double[] pos) {
 		double[] ret = new double[2];
 		int xsign = pos[0] < 0? -1:1;
 		int ysign = pos[1] < 0? -1:1;
 		ret[0] = (Math.abs(pos[0]) + TILE_SIZE / 2) % TILE_SIZE;
 		ret[1] = (Math.abs(pos[1]) + TILE_SIZE / 2) % TILE_SIZE;
 		ret[0] -= TILE_SIZE / 2;
 		ret[1] -= TILE_SIZE / 2;
 		ret[0] *= xsign;
 		ret[1] *= ysign;
 		return ret;
 	}
 	
 	public Border getCurrentBorder(Tile tile) {
 		double xpos = localSimulator.getTDistanceX();
 		double ypos = localSimulator.getTDistanceY();
 		return getBorderOfPos(tile, xpos, ypos);
 	}
 	
 	public Border getBorderOfPos(Tile tile, double xpos, double ypos) {
 		Border border = null;
 		double[] pass = {xpos, ypos};
 		double[] pos = convertToInTilePos(pass);
 		double x = pos[0];
 		double y = pos[1];
 		if (y <= (-TILE_SIZE/2) + BORDER_SIZE) {
 			border = getBottomBorderOfTile(tile);
 		} else if (y >= (TILE_SIZE/2) - BORDER_SIZE) {
 			border = getTopBorderOfTile(tile);
 		} else if (x <= (-TILE_SIZE/2) + BORDER_SIZE) {
 			border = getLeftBorderOfTile(tile);
 		} else if (x >= (TILE_SIZE/2) - BORDER_SIZE) {
 			border = getRightBorderOfTile(tile);
 		}
 		return border;
 	}
 	
 	public boolean collidesWithBorder()
 			throws IllegalArgumentException {
 		Tile tile = getCurrentTile();
 		Border border = getCurrentBorder(tile);
 		if (border != null) {
 			//System.out.println("border " + border.toString() + " pass " + border.isPassable());
 			return !border.isPassable();
 		}
 		
 		return false;
 	}
 	
 	public boolean collidesWithBorder(double[][] corners)
 			throws IllegalArgumentException {
 		for (int i = 0; i < 4; i++) {
 			Tile tile;
 			try {
 				tile = getTileOfPos(corners[i][0], corners[i][1]);
 			} catch (IllegalArgumentException e) {
 				return true;
 			}
 			Border border = getBorderOfPos(tile, corners[i][0], corners[i][1]);
 			//DebugBuffer.addInfo("x= " + corners[i][0] + " y= " + corners[i][1] + "border = " + border);
 			if (border != null && !border.isPassable()) {
 				//System.out.println("border " + border.toString() + " pass " + border.isPassable());
 				return !border.isPassable();
 			}
 		}
 		
 		return false;
 	}
 	
 	public boolean collidesWithBall(double[][] corners) {
 		for (int i = 0; i < 2; i++) {
 			Tile tile;
 			try {
 				tile = getTileOfPos(corners[i][0], corners[i][1]);
 			} catch (IllegalArgumentException e) {
 				return true;
 			}
 			if (ballMap.hasId(tile.getPosition())) {
 				Border border = getBorderOfPos(tile, corners[i][0], corners[i][1]);
 				if (border != null && !border.isPassable()) {
 					ballMap.removeObjectAtId(tile.getPosition());
 					//DebugBuffer.addInfo("robot collected ball");
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	public boolean isOnWhiteBorder() {
 		Tile tile = getCurrentTile();
 		Border border = getCurrentBorder(tile);
 		if (border != null) {
 			return border instanceof WhiteBorder || border instanceof SeesawBorder;
 		}
 		
 		return false;
 	}
 	
 	public boolean isOnBarcode() {
 		double xpos = localSimulator.getTDistanceX();
 		double ypos = localSimulator.getTDistanceY();
 		Tile tile = getCurrentTile();
 		if (tile.getBarcode() == null)
 			return false;
 		double[] pass = {xpos, ypos};
 		double[] pos = convertToInTilePos(pass);
 		int i = 0;
 		if (getLeftBorderOfTile(tile) instanceof PanelBorder) {
 			i = 1;
 		}
 		if (pos[i] > -12 && pos[i] < 12)
 			return true;
 		return false;
 	}
 	
 	public boolean isOnBlack() {
 		double xpos = localSimulator.getTDistanceX();
 		double ypos = localSimulator.getTDistanceY();
 		Tile tile = getCurrentTile();
 		if (tile.getBarcode() == null)
 			return false;
 		Barcode bar = tile.getBarcode();
 		double[] pass = {xpos, ypos};
 		double[] pos = convertToInTilePos(pass);
 		int i = 0;
 		if (getLeftBorderOfTile(tile) instanceof PanelBorder) {
 			i = 1;
 		}
 		int codeNr = (int)((pos[i] + 12) / 3);
 		if (codeNr <= 0 || codeNr >= 7)
 			return true;
 		if (bar.getCode()[codeNr - 1] == 0)
 			return true;
 		return false;
 	}
 	
 	private SolidBorder getFirstPanelInDirection(Tile tile, Direction dir) {
 		boolean found = false;
 		TilePosition currPos = tile.getPosition();
 		while (!found) {
 			BorderPosition pos = dir.getBorderPositionInDirection(currPos);
 			try {
 				Border border = null;
 				border = borderMap.getObjectAtId(pos);
 				if (border instanceof SolidBorder && !border.isPassable()) {
 					return (SolidBorder)border;
 				}
 			} catch (IllegalArgumentException e) {
 				return null;
 			}
 			currPos = dir.getPositionInDirection(currPos);
 		}
 		return null;
 	}
 	
 	public int distanceFromPanel(Direction dir) {
 		double x = localSimulator.getTDistanceX();
 		double y = localSimulator.getTDistanceY();
 		Tile tile = getCurrentTile();
 		SolidBorder border = getFirstPanelInDirection(tile, dir);
 		if (border != null) {
 			double[] pass = {x, y};
 			double[] pos = convertToInTilePos(pass);
 			int dist = dir.getDistance(pos[0], pos[1], tile, border.getBorderPos());
 			return dist-2;
 		} else {
 			return 9999;
 		}
 	}
 	
 	/**
 	 * USE: ultrasonic sensor & robot detection
 	 */
 	public boolean isRobotInFront() {
 		Tile tile = getCurrentTile();
 		Direction dir = Direction.fromAngle(localSimulator.getTRotation());
 		
 		for (RobotModel model : robotPool.getOtherRobots()) {
 			TilePosition modelTilePos = model.getCurrTile().getPosition();
 			
 			// Check if the robot is on the next tile
 			if(!(getBorderInDirection(tile, dir) instanceof PanelBorder)) {
 				TilePosition nextTilePos = dir.getPositionInDirection(tile.getPosition());
 				if (nextTilePos.equals(modelTilePos)) return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * USE: ultrasonic sensor
 	 */
 	public boolean isRobotLeft() {
 		Tile tile = getCurrentTile();
 		Direction dir = Direction.fromAngle(localSimulator.getTRotation()).left();
 
 		for (RobotModel model : robotPool.getOtherRobots()) {
 			TilePosition modelTilePos = model.getCurrTile().getPosition();
 			
 			// Check if the robot is on the next left tile
 			if(!(getBorderInDirection(tile, dir) instanceof PanelBorder)) {
 				TilePosition nextTilePos = dir.getPositionInDirection(tile.getPosition());
 				if (nextTilePos.equals(modelTilePos)) return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * USE: ultrasonic sensor
 	 */
 	public boolean isRobotRight() {
 		Tile tile = getCurrentTile();
 		Direction dir = Direction.fromAngle(localSimulator.getTRotation()).right();
 		
 		for (RobotModel model : robotPool.getOtherRobots()) {
 			TilePosition modelTilePos = model.getCurrTile().getPosition();
 			
 			// Check if the robot is on the next right tile
 			if(!(getBorderInDirection(tile, dir) instanceof PanelBorder)) {
 				TilePosition nextTilePos = dir.getPositionInDirection(tile.getPosition());
 				if (nextTilePos.equals(modelTilePos)) return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * USE: robot detection
 	 */
 	public boolean checkIfSafe(int xPosTeamMate, int yPosTeamMate, int playerNumber) {
 		boolean frontSafe = true;
 		boolean rightSafe = true;
 		boolean leftSafe = true;
 		
 		Tile tile = getCurrentTile();
 		Direction dir = Direction.fromAngle(localSimulator.getTRotation());
 		
 		for (RobotModel model : robotPool.getOtherRobots()) {
 			TilePosition modelTilePos = model.getCurrTile().getPosition();
 			int [] teamMatePos = convertRelativeToAbsolutePosition(xPosTeamMate, yPosTeamMate, playerNumber);
 			
 			// Check if there is no wall just in front of the robot. 
 			// This would make all other detection useless.
 			if(!(getBorderInDirection(tile, dir) instanceof PanelBorder)) {
 	
 				TilePosition nextTilePos = dir.getPositionInDirection(tile.getPosition());
 					
 				// Check if the robot is on the next tile
 				if (nextTilePos.equals(modelTilePos) &&
 					nextTilePos.getX() != teamMatePos[0] && nextTilePos.getY() != teamMatePos[1])
 					return true;
 				
 				// Check if the robot is on the forward-front tile
 				if(!(getBorderInDirection(nextTilePos, dir) instanceof PanelBorder)) {
 					TilePosition frontTilePos = dir.getPositionInDirection(nextTilePos);
 					
 					if (frontTilePos.equals(modelTilePos) && 
 						dir.opposite() == Direction.fromAngle(model.getPosition().getRotation()) &&
 						frontTilePos.getX() != teamMatePos[0] && frontTilePos.getY() != teamMatePos[1])
 						frontSafe = false;
 				}
 				
 				// Check if the robot is on the forward-left tile
 				if(!(getBorderInDirection(nextTilePos, dir.left()) instanceof PanelBorder)) {
 					TilePosition leftTilePos = dir.left().getPositionInDirection(nextTilePos);
 					if (leftTilePos.equals(modelTilePos) && 
 						dir.left().opposite() == Direction.fromAngle(model.getPosition().getRotation()) &&
 						leftTilePos.getX() != teamMatePos[0] && leftTilePos.getY() != teamMatePos[1]) 
 						leftSafe = false;
 				}
 				
 				// Check if the robot is on the forward-right tile
 				if(!(getBorderInDirection(nextTilePos, dir.right()) instanceof PanelBorder)) {
 					TilePosition rightTilePos = dir.right().getPositionInDirection(nextTilePos);
 					if (rightTilePos.equals(modelTilePos) &&
 						dir.right().opposite() == Direction.fromAngle(model.getPosition().getRotation()) &&
 						rightTilePos.getX() != teamMatePos[0] && rightTilePos.getY() != teamMatePos[1]) 
 						rightSafe = false;
 				}
 				
 			}
 		}
 		
 		return !isRobotInFront() && frontSafe && rightSafe && leftSafe; 
 	}
 	
 	/**
 	 * USE: robot detection
 	 */
 	public boolean checkIfSafe() {
 		return checkIfSafe(0);
 	}
 	
 	/**
 	 * USE: robot detection
 	 */
 	public boolean checkIfSafe(int offset) {
 		
 		boolean frontSafe = true;
 		boolean rightSafe = true;
 		boolean leftSafe = true;
 		
 		Tile tile = getCurrentTile();
		Direction dir = Direction.fromAngle(localSimulator.getTRotation()+90);
 		
 		for (RobotModel model : robotPool.getOtherRobots()) {
 			TilePosition modelTilePos = model.getCurrTile().getPosition();
 			
 			// Check if there is no wall just in front of the robot. 
 			// This would make all other detection useless.
 			if(!(getBorderInDirection(tile, dir) instanceof PanelBorder)) {
 	
 				TilePosition nextTilePos = dir.getPositionInDirection(tile.getPosition());
 				
 				// Check if the robot is on the forward-front tile
 				if(!(getBorderInDirection(nextTilePos, dir) instanceof PanelBorder)) {
 					TilePosition frontTilePos = dir.getPositionInDirection(nextTilePos);
 					
 					if (frontTilePos.equals(modelTilePos) && 
 						dir.opposite() == Direction.fromAngle(model.getPosition().getRotation()))
 						frontSafe = false;
 				}
 				
 				// Check if the robot is on the forward-left tile
 				if(!(getBorderInDirection(nextTilePos, dir.left()) instanceof PanelBorder)) {
 					TilePosition leftTilePos = dir.left().getPositionInDirection(nextTilePos);
 					if (leftTilePos.equals(modelTilePos) && 
 						dir.left().opposite() == Direction.fromAngle(model.getPosition().getRotation())) 
 						leftSafe = false;
 				}
 				
 				// Check if the robot is on the forward-right tile
 				if(!(getBorderInDirection(nextTilePos, dir.right()) instanceof PanelBorder)) {
 					TilePosition rightTilePos = dir.right().getPositionInDirection(nextTilePos);
 					if (rightTilePos.equals(modelTilePos) &&
 						dir.right().opposite() == Direction.fromAngle(model.getPosition().getRotation())) 
 						rightSafe = false;
 				}
 				
 			}
 		}
 		
 		return !isRobotInFront() && frontSafe && rightSafe && leftSafe; 
 	}
 	
 	public int[] convertRelativeToAbsolutePosition(int x, int y, int playerNumber) {
 		int[] newpos = new int[] {x,y};
 		switch(getStartDir(playerNumber)) {
 			case BOTTOM:
 				newpos = new int[] {-(int)x,-(int)y};
 				break;
 			case LEFT:
 				newpos = new int[] {-(int)y,(int)x};
 				break;
 			case RIGHT:
 				newpos = new int[] {(int)y,-(int)x};
 				break;
 			case TOP:
 				break;
 			default:
 				break;
 			
 		}
 		return newpos;
 	}
 	
 	/*
 	 * Starting position / direction
 	 */
 	
 	private Map<Integer, TilePosition> startPos = new HashMap<Integer, TilePosition>();
 	private Map<Integer, Direction> startDir = new HashMap<Integer, Direction>();
 	
 	public void setStartPos(int i, TilePosition pos) {
 		startPos.put(i, pos);
 	}
 	
 	public void setStartDir(int i, Direction dir) {
 		startDir.put(i, dir);
 	}
 	
 	public TilePosition getStartPos(int i) {
 		return startPos.get(i);
 	}
 	
 	public Direction getStartDir(int i) {
 		return startDir.get(i);
 	}
 	
 	/*
 	 * Consistent Seesaw with real world and between application
 	 */
 	
 	public void playerLockedSeesaw(int barcode) {
 		TilePosition btilePos = barcodeIndex.get(barcode);
 		Tile btile = getTileAt(btilePos);
 		Barcode otherBarcode = btile.getBarcode().otherSeesawBarcode();
 		TilePosition otherBtilePos = barcodeIndex.get(otherBarcode.getDecimal());
 		SeesawBorder firstBorder = getSeesawBorder(btile);
 		SeesawBorder secondBorder = getSeesawBorder(getTileAt(otherBtilePos));
 		firstBorder.setDown();
 		secondBorder.setUp();
 	}
 	
 	public void playerUnlockedSeesaw(int barcode) {
 		TilePosition btilePos = barcodeIndex.get(barcode);
 		Tile btile = getTileAt(btilePos);
 		Barcode otherBarcode = btile.getBarcode().otherSeesawBarcode();
 		TilePosition otherBtilePos = barcodeIndex.get(otherBarcode.getDecimal());
 		SeesawBorder firstBorder = getSeesawBorder(btile);
 		SeesawBorder secondBorder = getSeesawBorder(getTileAt(otherBtilePos));
 		firstBorder.setUp();
 		secondBorder.setDown();
 	}
 	
 }
