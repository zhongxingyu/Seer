 package ch.cern.atlas.apvs.daq.server;
 
 import java.util.List;
   
 import ch.cern.atlas.apvs.domain.Measurement;
 
 public class ValueFilter implements Filter {
 
 	@Override
 	public boolean filter(Measurement current, List<Measurement> list,
 			double resolution) {
 		// TODO Auto-generated method stub
 		int lengthOfList=list.size();
 		boolean res;
 		if (lengthOfList < 2){
 			//with 0 or 1 previous measurement in LIST, just add current as new element of list.
 			if (current != null){
 				list.add(current);
 				//System.out.println("ValueFilter: (previous <2) just add <"+current.getValue()+"> to the list");
 				//System.out.println("ValueFilter: List len is:"+list.size());
 				//return false
 				res = false;
 			} else {
 				res = true;
 			}
 		} else{
 			Measurement lastMeasurement = list.get(lengthOfList-1);
 			Measurement secondLastMeasurement = list.get(lengthOfList-2);
 			double currentValue;
 			if (current==null) {
 				lastMeasurement.disconnect();
 				res = true;}
 			else{
				if((!(lastMeasurement.isConnected())) || (!(secondLastMeasurement.isConnected()))) {
 					list.add(current);
 					res = false;  
 				}else {
 					currentValue = current.getValue();
 				    if ((isInThreshold(lastMeasurement.getValue(), currentValue, resolution)) && 
 						(isInThreshold(secondLastMeasurement.getValue(), currentValue, resolution))) {
 					    // Update timestamp:current measurement is inside the resolution-distance 
 					    // from last and second last measurements.
 					    lastMeasurement.setDate(current.getDate());
 					    //return true;
 					    res = true;
 				    } else {
 					    // Add current as new measurement: it is over resolution-distance 
 					    // from last measurement or from second last measurement
 					    list.add(current);
 					    // return false;
 				        res = false;
 				   }
 			    }
 			}
 		}
 		return res; 
 	}
 	private static boolean isInThreshold(double v1, double v2, double distance){
 		return Math.abs(v1-v2)<=distance;
 	}
 }
