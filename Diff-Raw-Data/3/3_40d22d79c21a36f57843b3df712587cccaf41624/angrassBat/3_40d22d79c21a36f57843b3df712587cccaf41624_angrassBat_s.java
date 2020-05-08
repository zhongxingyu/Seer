 import java.awt.Robot;
 import java.awt.PointerInfo;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.event.InputEvent;
 import java.awt.Rectangle;
 import java.awt.Color;
 import java.awt.AWTException;
 import java.awt.image.BufferedImage;
 import java.awt.image.Raster;
 import java.awt.event.KeyEvent;
 
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.IOException;
 
 import java.util.Scanner;
 import java.lang.Math;
 
 public class angrassBat {
 	static Robot kittens;
 	static double hlat = 39.22;
 	static double llat = 39.13;
 	static double hlon = -76.79;
 	static double llon = -76.84;
 	
 	public static int[][] equivalenceList = new int[1000][1000];
 
 	public static void main(String[] args) {
 		makeRobot();
 	
 		System.out.println("Starting position: http://www.ingress.com/intel?ll=" + hlat + "," + llon + "&z=17");
 		
 		System.out.println("Press return when your mouse is in position.");
 		System.out.print("Place the mouse at the upper left corner of the search rectangle: ");
 		Point pos = getPositionAtMouse();
 		int origX = (int) pos.getX();
 		int origY = (int) pos.getY();
 		
 		System.out.print("Place the mouse at the lower right corner of the search rectangle: ");
 		pos = getPositionAtMouse();
 		int width = (int) pos.getX() - origX;
 		int height = (int) pos.getY() - origY;
 		
 		if(width < 0 || height < 0) {
 			System.out.println("Come back when you don't suck at rectangles.");
 			return;
 		}
 		
 		System.out.print("Place the mouse over the center of the link button: ");
 		pos = getPositionAtMouse();
 		int linkX = (int) pos.getX();
 		int linkY = (int) pos.getY();
 		
 		System.out.println("Sleeping 5 seconds. Bring up the Ingress Intel map now.");
 		catnap(5000);
 		
 		searchUnclaimed(origX, origY, width, height, linkX, linkY);
 		//printImageMap(imageMap);
 		
 		/*
 		int[][] imageMap = screenToArray(origX, origY, width, height);
 		imageMap = connectedComponents(imageMap);	//builds equiv list
 		int count = estimatePortals();				//counts equiv list
 		System.out.println(count);
 		
 		for(int y=0; y<height; y++) {
 			for(int x=0; x<width; x++){
 				System.out.print(equivalenceList[x][y] + " ");
 			}
 			System.out.println();
 		}*/
 		
 		while(true) {
 			Color pixelColor = getColorAtMouse();
 			System.out.println(	"r: " + pixelColor.getRed() + 
 								"g: " + pixelColor.getGreen() + 
 								"b: " + pixelColor.getBlue() + 
 								"rgb: " + pixelColor.getRGB());
 		}//*/
 	}
 	
 	public static void makeRobot() {
 		try {
 			kittens = new Robot();
 		} catch(AWTException awte) {
 			awte.printStackTrace();
 		}
 		
 		return;
 	}
 	
 	public static int processLink(String link, int midX, int midY, int direction) {
 		String regex = "https?://www.ingress.com/intel\\?ll=-?[0-9]+\\.[0-9]+,-?[0-9]+\\.[0-9]+\\&z=[0-9]+";
 		if(link.matches(regex)) {
 			double lat, lon;
 			int zlevel;
 			String[] data = link.split("intel\\?ll=");
 			data = data[1].split("\\&z=");
 			String[] ll = data[0].split(",");
 			String z = data[1];
 			
 			lat = Double.parseDouble(ll[0]);
 			lon = Double.parseDouble(ll[1]);
 			zlevel = Integer.parseInt(z);
 			
 			if(zlevel < 17) {
 				System.out.println("Zooming in, unclaimed portals are not currently visible.");
 				zoom(zlevel, midX, midY);
 			}
 			
 			//00 ur, 01 ul, 10 dr, 11 dl
 			if(lat < llat) {
 				direction &= 0xFFFFFD;
 			} else if(lat > hlat) {
 				direction |= 0x2;
 			}
 			
 			if(lon < llon) {
 				direction &= 0xFFFFFE;
 			} else if(lon > hlon) {
 				direction |= 0x1;
 			}
 		} else {
 			System.out.println("Link does not match.");
 		}	
 		
 		return direction;
 	}
 	
 	public static void zoom(int zlevel, int x, int y) {
 		catnap(200);
 		kittens.mouseMove(x, y);
 		catnap(200);
 		kittens.mousePress(InputEvent.BUTTON1_MASK);
 		catnap(200);
 		//this wiggle avoids clicking on a portal
 		kittens.mouseMove(x+15, y+15);	
 		catnap(200);
 		kittens.mouseMove(x, y);
 		catnap(200);
 		kittens.mouseRelease(InputEvent.BUTTON1_MASK);
 		catnap(200);
 		for(int i=zlevel; i<17; i++) {
 			kittens.mouseWheel(-1);
 			catnap(1000);
 		}
 	}
 	
 	public static String getLink(int linkX, int linkY) {
 		clickMouse(linkX, linkY);
 		catnap(200);
 		kittens.keyPress(KeyEvent.VK_TAB);
 		catnap(100);
 		kittens.keyRelease(KeyEvent.VK_TAB);
 		catnap(200);
 		kittens.keyPress(KeyEvent.VK_CONTROL);
 		catnap(200);
 		kittens.keyPress(KeyEvent.VK_C);
 		catnap(200);
 		kittens.keyRelease(KeyEvent.VK_C);
 		catnap(200);
 		kittens.keyRelease(KeyEvent.VK_CONTROL);
 		catnap(200);
 		
 		return clipContentes();
 	}
 	
 	public static String clipContentes() {
 		String result = "";
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 		Clipboard clipboard = toolkit.getSystemClipboard();
 		try {
 			try {
 				result = (String) clipboard.getData(DataFlavor.stringFlavor);
 			} catch (IOException ioex) {
 				ioex.printStackTrace();
 			}
 		} catch (UnsupportedFlavorException ufex) {
 			ufex.printStackTrace();
 		}
 		
 		return result;
 	}	
 	
 	public static int searchUnclaimed(int origX, int origY, int width, int height, int linkX, int linkY) {
 		int dragCount = 0;
 		int count = 0;
 		int direction = 2;	
 		int oldDirection = direction;
 		int fromX, fromY, toX, toY;
 		boolean found = false;
 		String link;
 		
 		int midX = origX + width / 2;
 		int midY = origY + height / 2;
 		
 		int[][] imageMap;
 		
 		while(true) {
 			//vertical 
 			switch(direction) {
 				case 2:
 				case 3:
 					fromX = origX + width / 2;
 					fromY = origY + height;
 					toX = fromX;
 					toY = origY;
 					toY += 30;
 					break;
 				case 0:
 				case 1:
 				default:
 					fromX = origX + width / 2;
 					fromY = origY;
 					toX = fromX;
 					toY = origY + height;
 					toY -= 30;
 					break;
 			}
 			
 			imageMap = screenToArray(origX, origY, width, height);
 			imageMap = connectedComponents(imageMap);	//builds equiv list
 			count = estimatePortals();				//counts equiv list
 		
 			if(count > 0) {
 				link = getLink(linkX, linkY);
 				System.out.println(link + " : " + count);
 				direction = processLink(link, midX, midY, direction);
 			}
 		
 			dragMouse(fromX, fromY, toX, toY, 5000);
 		
 			//horizontal
 			switch(direction) {
 				case 0:
 				case 2:
 				default:
 					fromX = origX + width;
 					fromY = origY + height / 2;
 					toX = origX;
 					toX += 30;
 					toY = fromY;
 					break;
 				case 1:
 				case 3:
 					fromX = origX;
 					fromY = origY + height / 2;
 					toX = origX + width;
 					toX -= 30;
 					toY = fromY;
 					break;
 			}
 			
 			oldDirection = direction;
 			dragCount = 0;
 			while(true) {
 				imageMap = screenToArray(origX, origY, width, height);
 				imageMap = connectedComponents(imageMap);	//builds equiv list
 				count = estimatePortals();				//counts equiv list
 				
 				if(dragCount % 20 == 19 || count > 0) {
 					link = getLink(linkX, linkY);
 					direction = processLink(link, midX, midY, direction);
 				
 					if(count > 0)
 						System.out.println(link + " : " + count);
 				}
 							
 				dragMouse(fromX, fromY, toX, toY, 5000);
 				dragCount += 1;
 								
 				if(direction != oldDirection)
 					break;
 			}
 			
 		}
 		
 		//return 0xFFFFFFFF;
 	}
 	
 	public static int NOT_UP_TO_DATE_spiralSearch(int origX, int origY, int width, int height) {
 		int dragCount = 2;
 		int count = 0;
 		boolean found = false;
 		
 		while(true) {
 			int fromX, fromY, toX, toY;
 			
 			switch(dragCount % 4) {
 				case 2:
 					fromX = origX + width;
 					fromY = origY + height / 2;
 					toX = origX;
 					toY = fromY;
 					break;
 				case 3:
 					fromX = origX + width / 2;
 					fromY = origY + height;
 					toX = fromX;
 					toY = origY;
 					break;
 				case 0:
 					fromX = origX;
 					fromY = origY + height / 2;
 					toX = origX + width;
 					toY = fromY;
 					break;
 				case 1:
 				default:
 					fromX = origX + width / 2;
 					fromY = origY;
 					toX = fromX;
 					toY = origY + height;
 					break;
 			}
 			
 			for(int i = 0; i < dragCount / 2; i++) {
 				int[][] imageMap = screenToArray(origX, origY, width, height);
 				imageMap = connectedComponents(imageMap);	//builds equiv list
 				count = estimatePortals();				//counts equiv list
 				
 				if(count > 0) {
 					found = true;
 					break;
 				}
 				
 				dragMouse(fromX, fromY, toX, toY, 5000);
 			}
 			
 			dragCount++;
 						
 			if(found)
 				return count;
 			
 			//drag mouse
 			//increment if needed
 		}
 		
 	}
 	
 	public static Point getPositionAtMouse() {
 		//wait for return
 		Scanner read = new Scanner(System.in);
 		read.nextLine();
 		//get and return mouse position
 		PointerInfo pinf = MouseInfo.getPointerInfo();
 		Point pos = pinf.getLocation();
 		return pos; 
 	}
 	
 	public static Color getColorAtMouse() {
 		Point pos = getPositionAtMouse();
 		Color pixelColor = kittens.getPixelColor((int)pos.getX(), (int)pos.getY());
 		return pixelColor;
 	}
 	
 	public static void clickMouse(int atX, int atY) {
 		kittens.mouseMove(atX, atY);
 		catnap(200);
 		kittens.mousePress(InputEvent.BUTTON1_MASK);
 		catnap(200);
 		kittens.mouseRelease(InputEvent.BUTTON1_MASK);
 		return;		
 	}
 	
 	public static void dragMouse(int fromX, int fromY, int toX, int toY, int loadWait) {
 		kittens.mouseMove(fromX, fromY);
 		catnap(200);
 		kittens.mousePress(InputEvent.BUTTON1_MASK);
 		catnap(200);
 		kittens.mouseMove(toX, toY);
 		catnap(200);
 		kittens.mouseRelease(InputEvent.BUTTON1_MASK);
 		catnap(loadWait);
 		return;
 	}
 	
 	public static void catnap(int N) {
 		double modifier = Math.random() / 3;
		int fuzzy = N + N * modifier;
 		kittens.delay(fuzzy);
 	}
 	
 	public static int[][] screenToArray(int rectX, int rectY, int rectWidth, int rectHeight) {
 		Rectangle rect = new Rectangle(rectX, rectY, rectWidth, rectHeight);
 		//System.out.println("Grabbing screen in 3 seconds.");
 		catnap(1000);
 		BufferedImage screenGrab = kittens.createScreenCapture(rect);
 		Raster image = screenGrab.getData();
 		int x, y, height, width;
 		height = image.getHeight();
 		width = image.getWidth();
 		int[] pixel;
 		int set = 0x736574;
 		int[][] imageMap = new int[height][width];
 	
 		for(y=0; y<height; y++) {
 			for(x=0; x<width; x++){
 				pixel = image.getPixel(x, y, new int[3]);
 				if(pixel[0] > 220 && pixel[1] > 220 && pixel[2] > 220) {
 					imageMap[y][x] = set;
 				} else {
 					imageMap[y][x] = 0;
 				}
 			}
 		}
 		
 		return imageMap;
 	}
 	
 	public static int[][] connectedComponents(int[][] imageMap) {
 		int set = 0x736574;
 		int newGroup = 1;
 		int height = imageMap.length;
 		int width = imageMap[0].length;
 		int x, y;
 		
 		for(y=0; y<1000; y++){
 			for(x=0;x<1000; x++){
 				equivalenceList[x][y] = 0;
 			}
 		}
 		//first pass
 		for(y=0; y<height; y++) {
 			for(x=0; x<width; x++){
 				if(imageMap[y][x] == set) {
 					int groupNumber = getLowestNeighbor(x, y, imageMap);
 					if(groupNumber == set) {
 						groupNumber = newGroup;
 						newGroup++;
 						if(newGroup > 999)
 							return null;
 					}
 					imageMap[y][x] = groupNumber;
 				}
 			}
 		}
 		
 		//second pass
 		for(y=0; y<height; y++) {
 			for(x=0; x<width; x++){
 				if(imageMap[y][x] != 0) {
 					imageMap[y][x] = getLowestEqual(imageMap[y][x]);
 					equivalenceList[0][imageMap[y][x]]++;
 				}
 			}
 		}
 		
 		return imageMap;
 	}
 	
 	public static int getLowestEqual(int value) {
 		int i, retVal = 1000;
 		for(i=1; i<1000; i++) {
 			if(i == value) 
 				return i;
 			if(equivalenceList[value][i] != 0) {
 				retVal = getLowestEqual(i);
 				break;
 			}
 		}
 		
 		equivalenceList[value][retVal] = 1;
 		return retVal;
 	}
 	
 	public static int getLowestNeighbor(int x, int y, int[][] imageMap) {
 		int height = imageMap.length;
 		int width = imageMap[0].length;
 		int set = 0x736574;
 		int i, j;
 		
 		int lowestNeighbor = set;
 		int[][] neighbors = {{1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}};
 		int[] nVals = new int[8];
 		
 		//get neighbors
 		for(i=0; i<8; i++) {
 			int xMod = x + neighbors[i][0];
 			int yMod = y + neighbors[i][1];
 			if(xMod < width && yMod < height && xMod > 0 && yMod > 0) {
 				int nVal = imageMap[yMod][xMod];
 				if(nVal != 0 && nVal != set) {
 					nVals[i] = nVal;
 					if(nVal < lowestNeighbor) {
 						lowestNeighbor = nVal;
 					}
 				}
 			}
 		}
 
 		//build equivalence list
 		for(i=0; i<8; i++) {
 			if(nVals[i] != 0) {
 				int value = nVals[i];	
 				for(j=0; j<8; j++) {
 					int eqVal = nVals[j];
 					if(eqVal != 0)
 						equivalenceList[value][eqVal] = 1;
 				}
 			}
 		}
 		
 		return lowestNeighbor;
 	}
 	
 	public static int estimatePortals() {
 		int i;
 		int count = 0;
 		for(i=0; i<1000; i++) {
 			count += equivalenceList[0][i] / 20;
 			if(equivalenceList[0][i] > 20)
 				System.out.println("Count: " + equivalenceList[0][i]);
 		}
 		
 		return count;
 	}
 	
 	public static void printImageMap(int[][] imageMap) {
 		int height = imageMap.length;
 		int width = imageMap[0].length;
 		int x, y;
 		
 		for(y=0; y<height; y++) {
 			for(x=0; x<width; x++){
 				if(imageMap[y][x] == 0) {
 					System.out.print(" ");
 				} else {
 					System.out.print(imageMap[y][x]);
 				}
 			}
 			System.out.println();
 		}
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
