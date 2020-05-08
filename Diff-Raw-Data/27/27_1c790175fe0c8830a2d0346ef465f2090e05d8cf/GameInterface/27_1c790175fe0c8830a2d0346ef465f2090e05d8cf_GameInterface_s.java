 package game;
 
 import java.awt.Color;
 import java.awt.Graphics;
 
 public class GameInterface {
 	int locYRollover = 0;
 	int locXRollover = 0;
 
 	private Dwarfs dwarfs;
 	private Map map;
 	private Dwarfs.Dwarf selectedDwarf;
 	private Pathfinder pathfinder;
 	private int element;
 
 	public GameInterface(Map map, Dwarfs dwarfs, Pathfinder pathfinder) {
 		this.dwarfs = dwarfs;
 		this.map = map;
 		this.pathfinder = pathfinder;
 	}
 
 	public void leftClick(int mouseX, int mouseY) {
 		if (dwarfs.isDwarf(mouseX, mouseY)) {
 			selectedDwarf = dwarfs.getDwarfByMouse(mouseX, mouseY);
 			System.out.println(selectedDwarf);
 
 		} else {
 			map.changeElement(mouseX, mouseY, element);
 		}
 
 	}
 
 	public void rightClick(int mouseX, int mouseY) {
 		if (!dwarfs.isDwarf(mouseX, mouseY) && selectedDwarf != null) {
			System.out.print("Finding Path");
 			selectedDwarf.path(pathfinder.findPath(selectedDwarf.locX,
 					selectedDwarf.locY, mouseX, mouseY));
 
 		}
 	}
 
 	public void highlightUnit(Graphics g) {
 		g.setColor(Color.red);
 		if (selectedDwarf != null) {
 			g.drawRect(selectedDwarf.smoothXLoc(),
 					selectedDwarf.smoothYLoc(), Grid.TILE_SIZE,
 					Grid.TILE_SIZE);
 		}
 	}
 
 	public void changeBoxLocation(double mouseX, double mouseY) {
 		Grid grid = new Grid();
 		locXRollover = grid.mouseBoxTileX((int) mouseX);
 		locYRollover = grid.mouseBoxTileY((int) mouseY);
 	}
 	
 	public void changeElement(int number) {
 		element = number;
 	}
 
 	public void drawBox(Graphics g) {
 		g.setColor(Color.blue);
 		g.drawRect(locXRollover, locYRollover, Grid.TILE_SIZE, Grid.TILE_SIZE);
 	}
 
 	public void draw(Graphics g) {
 		drawBox(g);
 		highlightUnit(g);
 	}
 }
