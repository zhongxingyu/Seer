 import java.util.LinkedList;
 
 
 public class Field {
 	
 	private LinkedList<Point> blackCells = new LinkedList<Point>();
 			
 	public void invertCell(Point p) {
 		if (blackCells.contains(p))
 			blackCells.remove(p);
 		else
 			blackCells.add(new Point(p.getX(), p.getY()));
 	}
 	
 	public boolean isCellBlack(Point p) {
 		return blackCells.contains(p);
 	}
 	
 	public int countBlackCells() {
 		return blackCells.size();	
 	}
	
	public void clear() {
		blackCells.clear();
	}
 }
