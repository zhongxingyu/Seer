 package pg13.business.create;
 
 import java.util.ArrayList;
 
 import pg13.app.PG13;
 import pg13.app.Services;
 import pg13.models.Puzzle;
 import pg13.persistence.DataAccess;
 
 public class PuzzleManager
 {
 
 	private DataAccess dataAccess;
 
 	public PuzzleManager()
 	{
 		this.dataAccess = Services.getDataAccess(PG13.dbName);
 	}
 	
 	public void save(Puzzle puzzle)
 	{
 		if(puzzle.getID() == Puzzle.DEFAULT_ID)
 		{
 			ArrayList<Long> keys = dataAccess.getSortedPuzzleIDs();
			long nextID = keys.get(keys.size() - 1) + 1;
 			puzzle.setID(nextID);
 		}
 		
 		this.dataAccess.savePuzzle(puzzle);
 	}
 	
 	public ArrayList<Puzzle> getAllPuzzles()
 	{
 		return this.dataAccess.getAllPuzzles();
 	}
 }
