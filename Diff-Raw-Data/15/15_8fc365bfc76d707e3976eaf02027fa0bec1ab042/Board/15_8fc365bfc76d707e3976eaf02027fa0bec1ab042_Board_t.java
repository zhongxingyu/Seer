 package gameObjects;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import primary.ApplicationController;
 import primary.ApplicationModel;
 import primary.Constants;
 import primary.Point;
 import view.ApplicationView;
 
 /**
  * Holds all the GameObject Backgrounds that make up the game board.
  *
  */
 public class Board {
 	public int width, height;
 	public GameObjectBackground[][] myGO;
 	public ArrayList<GameObjectToken> myTokens;
 	private GameObjectToken templateStrawberryToken;
 	private GameObjectBackground templateBackgroundPit;
 	public GameObjectBackground templateBoundaryWall;
 	private GameObjectBackground templateBoundaryOpen;
 	public GameObjectBackground templateBoundaryHidden;
 	public int startingBerries;
 	public static final int INFORMATIONDISTANCE = 1;
 	
 	/**
 	 * Constructor for a randomly generated Board
 	 * 
 	 * @param newWidth
 	 * @param newHeight
 	 */
 	public Board(int newWidth, int newHeight) {
 		height = newHeight;
 		width = newWidth;
 		
 		if (height < 3)
 			height = 3;
 		if (height > 25)
 			height = 25;
 		
 		if (width < 3) 
 			width = 3;
 		if (width > 40)
 			width = 40;
 		myGO = new GameObjectBackground[height][width];
 		myTokens = new ArrayList<GameObjectToken>();
 		
 		setGameObjectTemplates();
 	}
 
 	/** 
 	 * Constructor for a Board loaded from an XML node
 	 * 
 	 * @param inNode
 	 */
 	public Board(Node inNode) {
 		width = Integer.parseInt(inNode.getAttributes().getNamedItem("width").getNodeValue());
 		height = Integer.parseInt(inNode.getAttributes().getNamedItem("height").getNodeValue());
 
 		myGO = new GameObjectBackground[height][width];
 		myTokens = new ArrayList<GameObjectToken>();
 		setGameObjectTemplates();
 		inNode = inNode.getFirstChild();
 		
 		NodeList childList = inNode.getChildNodes();
 		GameObjectBackground newGOB = null;
 		for (int i = 0; i < childList.getLength(); i++) {
 			Node child = childList.item(i);
 			
 			if (child.getAttributes().getNamedItem("name").getNodeValue().equals(templateBoundaryWall.name))
 				newGOB = (GameObjectBackground) templateBoundaryWall.generateClone(null);
 			else if (child.getAttributes().getNamedItem("name").getNodeValue().equals(templateBackgroundPit.name))
 				newGOB = (GameObjectBackground) templateBackgroundPit.generateClone(null);
 			else if (child.getAttributes().getNamedItem("name").getNodeValue().equals(templateBoundaryOpen.name))
 				newGOB = (GameObjectBackground) templateBoundaryOpen.generateClone(null);
 			else {
 				System.err.println("unrecognized Background name: " + child.getAttributes().getNamedItem("name").getNodeValue());
 				System.exit(-1);
 			}
 			
 			newGOB.myLocation = new Point(Integer.parseInt(child.getAttributes().getNamedItem("x").getNodeValue()), 
 					Integer.parseInt(child.getAttributes().getNamedItem("y").getNodeValue()));
 			myGO[newGOB.myLocation.y][newGOB.myLocation.x]= newGOB; 
 		}
 		
 		inNode = inNode.getNextSibling();
 		childList = inNode.getChildNodes();
 		GameObjectToken newGOT = null;
 		for (int i = 0; i < childList.getLength(); i++) {
 			Node child = childList.item(i);
 			if (child.getAttributes().getNamedItem("name").getNodeValue().equals(templateStrawberryToken.name))
 				newGOT = (GameObjectToken) templateStrawberryToken.generateClone(null);
 			else {
 				System.err.println("Unrecognized Token name: " + child.getAttributes().getNamedItem("name").getNodeValue());
 				System.exit(-1);
 			}
 			
 			newGOT.myLocation = new Point(Integer.parseInt(child.getAttributes().getNamedItem("x").getNodeValue()), 
 					Integer.parseInt(child.getAttributes().getNamedItem("y").getNodeValue()));
 			newGOT.pointValue = Integer.parseInt(child.getAttributes().getNamedItem("pointValue").getNodeValue());
 			myTokens.add(newGOT);
 		}
 		
 		startingBerries = myTokens.size();
 		
 		if (ApplicationController.getInstance().myLoadConfiguration.informativeZones)
 			setInformationZones();
 	}
 	
 	public void clearBoard() {
 		myGO = null;
 		myTokens.clear();
 		myTokens = null;
 		templateStrawberryToken = null;
 		templateBoundaryWall = null;
 		templateBoundaryOpen = null;
 		templateBoundaryHidden = null;
 	}
 	
 	private void setGameObjectTemplates() {
 		templateBoundaryWall = new GameObjectBackground();
 		templateBoundaryWall.name = "Wall";
 		templateBoundaryOpen = new GameObjectBackground();
 		templateBoundaryOpen.name = "Open";
 		templateBoundaryHidden = new GameObjectBackground();
 		templateBoundaryHidden.name = "Hidden";
 		
 		templateBackgroundPit = new GameObjectBackground();
 		templateBackgroundPit.name = "Pit";
 		templateBackgroundPit.canBlockMovement = false;
 		
 		templateStrawberryToken = new GameObjectToken();
 		templateStrawberryToken.name = "Strawberry";
 		templateStrawberryToken.pointValue = 100;
 		templateStrawberryToken.canBlockMovement = false;
 		try {
 			BufferedImage imgTemp = ImageIO.read(new File("images"+ Constants.fileDelimiter + "TestObstruction.bmp"));
 			templateBoundaryWall.setGraphics(ApplicationView.convertImageToLocalSettings(imgTemp));
 			
 			imgTemp = ImageIO.read(new File("images"+ Constants.fileDelimiter + "BackgroundEmpty.bmp"));
 			templateBoundaryOpen.setGraphics(ApplicationView.convertImageToLocalSettings(imgTemp));
 			templateBoundaryOpen.canBlockMovement = false;
 			
 			imgTemp = ImageIO.read(new File("images"+ Constants.fileDelimiter + "BackgroundHidden.bmp"));
 			templateBoundaryHidden.setGraphics(ApplicationView.convertImageToLocalSettings(imgTemp));
 			templateBoundaryHidden.canBlockMovement = false;
 			
 			imgTemp = ImageIO.read(new File("images"+ Constants.fileDelimiter + "pit.bmp"));
 			templateBackgroundPit.setGraphics(ApplicationView.convertImageToLocalSettings(imgTemp));
 			templateBackgroundPit.canBlockMovement = false;
 			
 			imgTemp = ImageIO.read(new File("images"+ Constants.fileDelimiter + "strawberry.bmp"));
 			templateStrawberryToken.setGraphics(imgTemp);
 			
 		}
 		catch (Exception e) {
 			System.err.println("Error while loading base graphics with exception: " + e.getMessage());
 			System.exit(-1);
 		}
 	}
 		
 	public boolean generateRandomMap() {
 		GameObjectBackground cursor;
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				if ((x == 0) || (x == (width - 1)) || (y == 0) || (y == (height - 1))) {
 					cursor = (GameObjectBackground) templateBoundaryWall.generateClone(null); 
 				}
 				else {
 					cursor = (GameObjectBackground) templateBoundaryOpen.generateClone(null); 
 				}
 				cursor.setXY(x,  y);
 				myGO[y][x] = cursor;
 			}
 		}
 		
 		int totalPits = ApplicationController.getGenerator().nextInt(15)+15;
 		int counter = 0;
 		while (counter < totalPits) {
 			GameObjectBackground tempBack = (GameObjectBackground) templateBackgroundPit.generateClone(null);
 			tempBack.setXY(ApplicationController.getGenerator().nextInt(width-2)+1, ApplicationController.getGenerator().nextInt(height-2)+1);
 
 			if (!(myGO[tempBack.myLocation.y][tempBack.myLocation.x].name.equals("Pit")) && 
 					!tempBack.myLocation.equals(ApplicationModel.getInstance().myPlayer.myLocation)) {
 				myGO[tempBack.myLocation.y][tempBack.myLocation.x] = tempBack;
 				counter = counter + 1;
 			}
 		}
 		
 		int totalBerries = ApplicationController.getGenerator().nextInt(15)+15;
 		counter = 0;
 		while (counter < totalBerries) {
 			boolean safeToAdd = true;
 			GameObjectToken tempTok = (GameObjectToken) templateStrawberryToken.generateClone(null);
 			tempTok.setXY(ApplicationController.getGenerator().nextInt(width-2)+1, ApplicationController.getGenerator().nextInt(height-2)+1);
 			
 			for (int i = 0; i < myTokens.size(); i++) 
 				if (myTokens.get(i).myLocation.equals(tempTok.myLocation))
 					safeToAdd = false;
 			
 			if (myGO[tempTok.myLocation.y][tempTok.myLocation.x].name.equals("Pit"))
 					safeToAdd = false;
 			
 			if (safeToAdd) {
 				myTokens.add(tempTok);
 				counter++;
 			}
 		}
 		
 		startingBerries = myTokens.size();
 		
 		if (ApplicationController.getInstance().myLoadConfiguration.informativeZones)
 			setInformationZones();
 		
 		return true;
 	}
 	
 	/**
 	 * This method is responsible for setting the Breezy and Pungent zones that indicate a nearby pit or strawberry
 	 */
 	public void setInformationZones() {
 		//reset all designations
 		for (int y = 0; y < height; y++) 
 			for (int x = 0; x < width; x++) {
 				myGO[y][x].isBreezy = false;
 				myGO[y][x].isPungent = false;
 			}
 		for (int i = 0; i < myTokens.size(); i++) {
 			myTokens.get(i).isBreezy = false;
 			myTokens.get(i).isPungent = false;
 		}
 		
 		// first set pungent
 		for (int i = 0; i < myTokens.size(); i++)
 			setBreezyPungent(myTokens.get(i).myLocation, false, true);
 		
 		//and then set breezy
 		for (int y = 0; y < height; y++) 
 			for (int x = 0; x < width; x++) 
 				if (myGO[y][x].name.equals(templateBackgroundPit.name))
 					setBreezyPungent(new Point(x, y), true, false);
 	}
 	
 	/**
 	 * This function just simplifies the duplication of code I use in setInformationZones() above
 	 * This sets the appropriate breezy/pungent flag for all elements around a center square, ignoring the center square
 	 * 
 	 */
 	private void setBreezyPungent(Point centerPoint, boolean setBreezy, boolean setPungent) {
 		Point testP;
 		GameObject searchSq;
 
 		for (int y = centerPoint.y - INFORMATIONDISTANCE; y <= (centerPoint.y + INFORMATIONDISTANCE); y++)
 			for (int x = centerPoint.x - INFORMATIONDISTANCE; x <= (centerPoint.x + INFORMATIONDISTANCE); x++) {
 				testP = new Point(x, y);
 				
 				if (y >= 0 && y < height && x >= 0 && x < width && !testP.equals(centerPoint)) {
 					searchSq = myGO[y][x];
 					if (searchSq.name.equals(templateBoundaryOpen.name)) {
 						if (setPungent)
 							searchSq.isPungent = true;
 						if (setBreezy)
 							searchSq.isBreezy = true;
 					}
 				}
 			}
 	}
 	
 	public boolean removeToken(GameObjectToken removeTok) {
 		for (int i = 0; i < myTokens.size(); i++) {
 			if (myTokens.get(i).myLocation.equals(removeTok.myLocation)) {
 				myTokens.remove(i);
 				setInformationZones();
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	
 	public void writeToXMLFile(BufferedWriter outWR) {
 		try {
 			outWR.write("<Board height='" + height + "' width='" + width + "'>" + Constants.newline);
 			outWR.write("<Backgrounds>" + Constants.newline);
 			for (int y = 0; y < height; y++) 
 				for (int x = 0; x < width; x++) {
 					outWR.write("<Background x='" + myGO[y][x].myLocation.x + "' y='" + myGO[y][x].myLocation.y + "' name='" +
 							myGO[y][x].name +  "'/>" + Constants.newline);
 				}
 			outWR.write("</Backgrounds>" + Constants.newline); 
 			
 			outWR.write("<Tokens>" + Constants.newline);
 			for (int i = 0; i < myTokens.size(); i++) 
 				outWR.write("<Token x='" + myTokens.get(i).myLocation.x + "' y='" + myTokens.get(i).myLocation.y + 
 						"' name='" + myTokens.get(i).name + "' pointValue='" + myTokens.get(i).pointValue + "'/>" + Constants.newline);
 			outWR.write("</Tokens>" + Constants.newline);
 			outWR.write("</Board>");
 		} catch (IOException e) {
 			System.out.println("Error, cannot write to log file");
 			System.exit(0);
 		}
 	}
 	
 	public void generateInternalWalls() {
		int wallSections = (ApplicationController.getGenerator().nextInt(20) + 10) * ((width*height) /  (40*25));
 		int wallLength;
 		int startX, startY, endX, endY, direction;
 		boolean safeToAdd;
 		
 		for (int i = 0; i < wallSections; ) {
 			safeToAdd = false;
 			wallLength = ApplicationController.getGenerator().nextInt(8) + 3;
 			startX = ApplicationController.getGenerator().nextInt(width - 2)  + 1;
 			startY = ApplicationController.getGenerator().nextInt(height - 2)  + 1;
 			direction = ApplicationController.getGenerator().nextInt(2);
 			
 			if (direction == 0) { // RIGHT
 				safeToAdd = true;
 				endY = startY;
 				endX = startX + wallLength;
 				
 				if (endX > (width-1))
 					safeToAdd = false;
 				else
 					for (int xTest = startX; xTest < endX; xTest++) {
 						Point testPoint = new Point(xTest, startY);
 						GameObject tmpGO = ApplicationModel.getInstance().findGOByLocation(testPoint);
 						if (tmpGO.name.equals(templateBoundaryWall.name) || tmpGO.name.equals(templateBackgroundPit.name) ||
 								tmpGO.name.equals(templateStrawberryToken.name))
 							safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().myPlayer != null)
 							if (ApplicationModel.getInstance().myPlayer.myLocation.equals(testPoint))
 								safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().redGhost != null)
 							if (ApplicationModel.getInstance().redGhost.myLocation.equals(testPoint))
 								safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().blueGhost != null)
 							if (ApplicationModel.getInstance().blueGhost.myLocation.equals(testPoint))
 								safeToAdd = false;
 					}
 				
 				if (safeToAdd) {
 					for (int xTest = startX; xTest < endX; xTest++) {
 						myGO[startY][xTest] = (GameObjectBackground) templateBoundaryWall.generateClone(null);
 						myGO[startY][xTest].myLocation = new Point(xTest, startY);
 					}
 				}
 			}
 			else if (direction == 1) { // DOWN
 				safeToAdd = true;
 				endX = startX;
 				endY = startY + wallLength;
 				
 				if (endY > (height - 1)) 
 					safeToAdd = false;
 				else
 					for (int yTest = startY; yTest < endY; yTest++) {
 						Point testPoint = new Point(startX, yTest);
 						GameObject tmpGO = ApplicationModel.getInstance().findGOByLocation(testPoint);
 						if (tmpGO.name.equals(templateBoundaryWall.name) || tmpGO.name.equals(templateBackgroundPit.name) ||
 								tmpGO.name.equals(templateStrawberryToken.name))
 							safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().myPlayer != null)
 							if (ApplicationModel.getInstance().myPlayer.myLocation.equals(testPoint))
 								safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().redGhost != null)
 							if (ApplicationModel.getInstance().redGhost.myLocation.equals(testPoint))
 								safeToAdd = false;
 	
 						if (ApplicationModel.getInstance().blueGhost != null)
 							if (ApplicationModel.getInstance().blueGhost.myLocation.equals(testPoint))
 								safeToAdd = false;
 					}
 				if (safeToAdd) {
 					for (int yTest = startY; yTest < endY; yTest++) {
 						myGO[yTest][startX] = (GameObjectBackground) templateBoundaryWall.generateClone(null);
 						myGO[yTest][startX].myLocation = new Point(startX, yTest);
 					}
 				}
 			}
 			
 			if (safeToAdd) { 
 				i++;
 			}
 		}
 	}
 }
