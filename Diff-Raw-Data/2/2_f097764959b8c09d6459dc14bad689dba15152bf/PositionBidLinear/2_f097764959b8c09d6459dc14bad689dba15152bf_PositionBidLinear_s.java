 package modelers;
 
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.LinkedList;
 
import com.sun.tools.javac.code.Attribute.Array;

 import edu.umich.eecs.tac.props.Query;
 import edu.umich.eecs.tac.props.QueryReport;
 
 public class PositionBidLinear implements PositionBidModel{
 	protected double _interpolation;
 	protected int _slots;
 	protected LinkedList<Hashtable<Query,Double>> _position;
 	protected LinkedList<Hashtable<Query,Double>> _bid;
 	protected Hashtable<Query,Hashtable<Integer,Double>> _positionBid;
 	
 	public PositionBidLinear(int slots, double interpolation){
 		_slots = slots;
 		_position = new LinkedList<Hashtable<Query,Double>>();
 		_bid = new LinkedList<Hashtable<Query,Double>>();
 		_positionBid = new Hashtable<Query,Hashtable<Integer,Double>>();
 		
 		_interpolation = interpolation;
 	}
 	
 	public void updateReport(QueryReport queryReport){
 		Hashtable<Query,Double> positionsToday = new Hashtable<Query,Double>();
 		Hashtable<Query,Double> bidsToday = new Hashtable<Query,Double>();
 		_position.add(positionsToday);
 		_bid.add(bidsToday);
 		if(queryReport == null){
 			return;
 		}
 		
 		for(Query q : queryReport){
 			Hashtable<Integer,Double> queryPositionBid = new Hashtable<Integer,Double>();
 			double position = queryReport.getPosition(q);
 			double bid = queryReport.getCPC(q);//this is a hack, should be our actual bid.
 			
 			positionsToday.put(q, position);
 			bidsToday.put(q, bid); 
 			
 			if(!Double.isNaN(position)){
 				int pos = (int)position;
 				queryPositionBid.put(pos, bid);
 				for(int i = pos-1; i >= 1; i--){
 					queryPositionBid.put(i, bid+_interpolation*(pos-i));
 				}
 				for(int i = pos+1; i <= _slots; i++){
 					queryPositionBid.put(i, bid-_interpolation*(i-pos));
 				}
 				
 				System.out.println();
 				System.out.println(q+"Position Bid Given : "+bid+" in position "+pos);
 				double[] slotbids = new double[_slots];
 				for(int i = 0; i < _slots; i++){
 					slotbids[i] = queryPositionBid.get(i+1);
 				}
 				System.out.println(Arrays.toString(slotbids));
 			}
 		
 			_positionBid.put(q, queryPositionBid);
 		}
 		
 	}
 	
 	public double getBid(Query q, int slot) {
 		if(_positionBid.containsKey(q) && _positionBid.get(q).containsKey(slot)){
 			return _positionBid.get(q).get(slot);
 		}
 				
 		return Double.NaN;
 	}
 
 }
