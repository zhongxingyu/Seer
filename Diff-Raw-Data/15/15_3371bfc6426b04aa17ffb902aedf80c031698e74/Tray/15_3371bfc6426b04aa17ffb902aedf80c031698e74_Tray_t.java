 /*
  * A tray is a representation of a board state. 
  * Trays will be stored inside a graph.
  * Tray is a vertex in the graph of tray states.
  * 
  * Knows:
  * 
  * 1. Position of blocks
  * 2. position of spaces
  * 3. Weight which represents its closeness to another tray
  * 
  * Does:
  * 
  * 
  * 
  * 
  * */
 
 
 public class Tray {
	
	private int[][] boardState;
	private Tray previousTray;
	private int[] blockSizes;
 
	public Tray(String[] config, String size)
 	{
 		
 	}
 	
	public Tray(Tray previousTray, int moveBlockId, int direction)
 	{
 		
		
 	}
 	
	
	
 }
