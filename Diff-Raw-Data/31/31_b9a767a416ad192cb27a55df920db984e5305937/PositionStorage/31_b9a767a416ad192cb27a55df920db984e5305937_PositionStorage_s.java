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
	void addPosition(Position p){
 		positions.add(p);
 	}
 	
 	/**
 	 * returns last position
 	 * @return last position
 	 * @throws NoSuchFieldException
 	 */
	Position getLastPosition() throws NoSuchFieldException{
 		if (!positions.isEmpty()) {
 			return positions.get(positions.size()-1);
 		}
 		throw new NoSuchFieldException();
 
 	}
 	
 	/**
 	 * returns all positions as list
 	 * @return list of all positions
 	 */
	List<Position> getAll(){
 		return positions;
 	}
 	
 	/**
 	 * Returns last 'count' positions
	 * if there is not enought, it will return lesser
 	 * @param count
 	 * @return subset of positions (which is in the end)exit
 	 * 
 	 * @throws NoSuchFieldException
 	 */
	List<Position> getLastPositions(int count) throws NoSuchFieldException{
 		if(positions.isEmpty())
 			throw new NoSuchFieldException();
 		
		int end = positions.size() - 1;
 		int start = end - count;
 		
 		if(start < 0)
 			start = 0;
 		
 		return positions.subList(start, end);
 	}
 }
