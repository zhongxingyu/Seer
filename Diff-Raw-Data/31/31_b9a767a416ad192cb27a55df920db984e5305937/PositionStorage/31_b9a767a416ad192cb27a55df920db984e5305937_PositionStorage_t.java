 package back;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 
 /**
  * Storage of all position on one route
  * @author fafin
  *
  */
 public class PositionStorage extends Observable{
 	private List<Position> positions;
 
 	public PositionStorage(){
 		positions = new ArrayList<Position>();
 	}
 	
 	/**
 	 * Add one position on the end of list
 	 * @param p
 	 */
	public void addPosition(Position p){
 		positions.add(p);
 	}
 	
 	/**
 	 * returns last position
 	 * @return last position
 	 * @throws NoSuchFieldException
 	 */
	public Position getLastPosition() throws NoSuchFieldException{
 		if (!positions.isEmpty()) {
 			return positions.get(positions.size()-1);
 		}
 		throw new NoSuchFieldException();
 
 	}
 	
 	/**
 	 * returns all positions as list
 	 * @return list of all positions
 	 */
	public List<Position> getAll(){
 		return positions;
 	}
 	
 	/**
 	 * Returns last 'count' positions
	 * if there is not enough items, it will return lesser
 	 * @param count
 	 * @return subset of positions (which is in the end)exit
 	 * 
 	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
 	 */
	public List<Position> getLastPositions(int count) throws NoSuchFieldException, IllegalArgumentException{
		
		if(count <= 0)
			throw new IllegalArgumentException();
		
 		if(positions.isEmpty())
 			throw new NoSuchFieldException();
 		
		int end = positions.size();
 		int start = end - count;
 		
 		if(start < 0)
 			start = 0;
 		
 		return positions.subList(start, end);
 	}
 }
