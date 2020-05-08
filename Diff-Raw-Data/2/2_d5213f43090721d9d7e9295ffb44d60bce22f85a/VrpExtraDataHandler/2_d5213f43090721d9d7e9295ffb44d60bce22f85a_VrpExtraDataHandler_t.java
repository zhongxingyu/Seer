 package pls.vrp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.io.Writable;
 
 public class VrpExtraDataHandler {
 	public List<Writable> makeNextRoundHelperDataFromExtraData(List<Writable> extraDatas, int numToMake) {
 		//make a big ol' list of all the routes
 		List<List<Integer>> routes = new ArrayList<List<Integer>>();
 		for (Writable extraData : extraDatas) {
 			VrpSolvingExtraData vrpExtraData = (VrpSolvingExtraData)extraData;
 			routes.addAll(vrpExtraData.getNeighborhoods());
 		}
 		
 		int numRoutesPerHelperData = Math.min(routes.size(), 50);
 		
 		List<Writable> results = new ArrayList<Writable>(numToMake);
 		int routesIndex = 0;
 		for (int i = 0; i < numToMake; i++) {
 			List<List<Integer>> helperDataRoutes = new ArrayList<List<Integer>>(numRoutesPerHelperData);
 			for (int j = 0; j < numRoutesPerHelperData; j++) {
 				helperDataRoutes.add(routes.get(routesIndex));
 				routesIndex = (routesIndex + 1) % routes.size();
 			}
			VrpSolvingExtraData helperData = new VrpSolvingExtraData(helperDataRoutes);
			results.add(helperData);
 		}
 		
 		return results;
 	}
 }
