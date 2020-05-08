 package cells;
 
 import Screen.*;
 import java.awt.Graphics;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import pxlJam.Crayon;
 import items.Item;
 import items.Heart;
 import items.LevelEnd;
 
 public class Map {
 	private Cell[][] tiles;
 	private Crayon character;
 
 	private int width;
 	private int height;
 
	private File file;
 	private int blueCrayon = 100;
 	private static int MAX_CRAYON = 100;
 
 	private ArrayList<Item> items;
 
 	private ArrayList<String> maps;
 	private String allMaps;
 	private int mapNum;
 
	public Map(String filename) {
 		maps = new ArrayList<String>();
 		allMaps = "levels/allLevels.txt";
 		mapNum = 0;
 		readAllMaps();
		file = new File(filename);
 		items = new ArrayList<Item>();
 		readMap();
 	}
 
 	private void readAllMaps() {
 		try {
 			Scanner scan = new Scanner(new File(allMaps));
 
 			while (scan.hasNext()) {
 				maps.add("levels/" + scan.nextLine());
 			}
 			scan.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void readMap() {
 		Scanner scan;
 		try {
 			scan = new Scanner(new File(maps.get(mapNum)));
 
 			height = scan.nextInt();
 			width = scan.nextInt();
 
 			tiles = new Cell[width][height];
 			
 			String tempLine;
 			int linesBeen = 0;
 			while(scan.hasNext()){
 				tempLine = scan.nextLine();
 				
 				Scanner lineScan = new Scanner(tempLine);
 				
 				int runLength;
 				int tempInt;
 				
 				int tilesBeen = 0;
 				while(lineScan.hasNext()){
 					runLength = lineScan.nextInt();
 					tempInt = lineScan.nextInt();
 					
 					for(int i = 0; i < runLength; i++){
 						if (tempInt == 10)
 							character = new Crayon(tilesBeen * Cell.CELL_WIDTH, linesBeen * Cell.CELL_HEIGHT, this);
 						else if (tempInt == 11)
 							items.add(new Heart(tilesBeen * Cell.CELL_WIDTH, linesBeen * Cell.CELL_HEIGHT));
 						else if (tempInt == 12)
 							items.add(new LevelEnd(tilesBeen * Cell.CELL_WIDTH, linesBeen * Cell.CELL_HEIGHT));
 						else {
 							tiles[linesBeen][tilesBeen] = getWalltype(tempInt, tilesBeen * Cell.CELL_WIDTH, linesBeen * Cell.CELL_HEIGHT);
 						}
 						tilesBeen++;
 					}
 				}
 				linesBeen++;
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public Cell getWalltype(int fileNum, int x, int y) {
 		Cell tempCell = null;
 		if (fileNum == 1)
 			tempCell = new Wall(x, y);
 		else if (fileNum == 2) {
 			// something else
 		} else if (fileNum == 0) {
 			tempCell = null;
 		}
 
 		return tempCell;
 	}
 
 	public void draw(Graphics g) {
 		for (int i = 0; i < tiles.length; i++)
 			for (int j = 0; j < tiles[i].length; j++) {
 				if (tiles[i][j] != null) {
 					tiles[i][j].draw(g);
 				}
 
 			}
 
 		for (Item i : items) {
 			i.draw(g);
 		}
 
 		if (character != null) {
 			character.draw(g);
 		}
 	}
 
 	public void checkItemCollision() {
 		ArrayList<Item> temp = new ArrayList<Item>();
 
 		for (Item i : items) {
 			if (character.getHitbox().checkCollision(i.getHitbox())) {
 				i.doAction(this);
 				temp.add(i);
 			}
 		}
 
 		items.removeAll(temp);
 	}
 
 	public Crayon getCharacter() {
 		return character;
 	}
 
 	public void setCharacter(Crayon character) {
 		this.character = character;
 	}
 
 	public void setCell(int x, int y, Cell c) {
 		tiles[x][y] = c;
 	}
 
 	public Cell getTile(int x, int y) {
 		Cell newCell = tiles[x][y];
 		return newCell;
 
 	}
 
 	public Cell[][] getTiles() {
 		return tiles;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setBlueCrayon(int i) {
 
 		blueCrayon += i;
 		if (blueCrayon < 0) {
 			blueCrayon = 0;
 		}
 		if (blueCrayon > MAX_CRAYON) {
 			blueCrayon = MAX_CRAYON;
 		}
 	}
 
 	public int getBlue() {
 		return blueCrayon;
 	}
 
 	public int getCrayons() {
 		return blueCrayon;
 	}
 
 	public void nextLevel() {
 		if(mapNum == items.size()){
 			System.out.println("You win brohan");
 			System.exit(0);
 		}else{
 			mapNum++;
 		}
 		items = new ArrayList<Item>();
 		readMap();
 	}
 }
