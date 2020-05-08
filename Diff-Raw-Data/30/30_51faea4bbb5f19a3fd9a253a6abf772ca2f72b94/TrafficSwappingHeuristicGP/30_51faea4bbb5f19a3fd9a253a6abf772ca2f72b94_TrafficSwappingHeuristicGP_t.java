 package com.aarribas.evodta;
 
 import com.aarribas.dtasim.*;
 import com.aarribas.evodta.ecj.EvoDTAEvaluator.EvoDTAContext;
 import com.aarribas.evodta.ecj.EvoDTAEvaluator.EvoDTATask;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Scanner;
 
 import com.aarribas.traffictools.PathRepresentation;
 import com.aarribas.traffictools.TravelTimeManager;
 
 public class TrafficSwappingHeuristicGP extends TrafficSwappingHeuristic{
 
 	private ArrayList< ArrayList<PathRepresentation>> newRoutes;
 	private ArrayList< ArrayList<Double[]>> newRouteFractions;
 
 	private ArrayList< ArrayList<PathRepresentation>> oldRoutes;
 	private ArrayList< ArrayList<Double[]>> oldRouteFractions;
 
 	private double iteration;
 
 	private EvoDTATask task;
 
 	//some structures required to pass complex parameters to the GP Context
 	private  ArrayList<ArrayList<double[]>> costPerRoute;
 	private  ArrayList<ArrayList<double[]>> cumulativeOfDeltas;
 	private  ArrayList<int[]> numOptimalRtsPerOD;
 	private  ArrayList<int[]> indexOptimalRtPerOD;
 	private  ArrayList<double[]> minDemandPerOD;
 	
 	private double error;
 	
 	public enum GPStatus {
 		GP_STATUS_NORMAL,
 		GP_STATUS_ABORTED
 	}
 	
 	private GPStatus gpStatus;
 
 
 	public void setup(ArrayList< ArrayList<PathRepresentation>> oldRoutes,
 			ArrayList< ArrayList<Double[]>> oldRouteFractions,
 			ArrayList< ArrayList<PathRepresentation>> newRoutes,
 			ArrayList< ArrayList<Double[]>> newRouteFractions,
 			int iteration){
 		this.oldRoutes  = oldRoutes;
 		this.oldRouteFractions = oldRouteFractions;
 		this.newRoutes = newRoutes;
 		this.newRouteFractions = newRouteFractions;
 		this.iteration = (double) iteration;
 
 	}
 
 	public void run(){
 
 		//compute costs for all routes
 		updateCostsPerRoute();
 
 		//compute number of optimal routes and the minimum cost -so far- per ODPair
 		computeNumOptimalRtsAndMinRtFrac();
 
 		ArrayList< ArrayList<Double[]>> tempRouteFractions =  new ArrayList< ArrayList<Double[]>>();
 
 		//compute routeFractions for non optimal routes and normalise routeFractions for optimal routes
 		for(int setOfRoutesIndex = 0; setOfRoutesIndex< newRoutes.size(); setOfRoutesIndex++){
 
 			ArrayList<Double[]> fractionsForOD = new ArrayList<Double[]>();
 			double[] cumulativeOfRouteFractions = new double[(int)(tEnd/tStep)];
 			Arrays.fill(cumulativeOfRouteFractions, 0.0);
 
 			//first go through the all routes and compute the new routeFractions for the non optimal or just save an uninitialized array for the optimal
 			for(int fractionsIndex = 0; fractionsIndex < newRouteFractions.get(setOfRoutesIndex).size(); fractionsIndex++ ){
 
 				//temp fractions
 				Double[] tempFractions = new Double[newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex).length];
 
 				
 				if(fractionsIndex < oldRouteFractions.get(setOfRoutesIndex).size()){
 					
 					//the following applies to routes already seen 
 					for(int fracIndex = 0; fracIndex < newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex).length; fracIndex++ ){
 
 						//compute correction only if the route fraction is not optimal (newRoutefrac = 1 indicates it is optimal)
 						if(newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] != 1){
 							
 							double rtFrac = oldRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex];
 
 							//compute the cost difference between this route and the optimal route
 							double normCostDiff = costPerRoute.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] - 
 									costPerRoute.get(setOfRoutesIndex).get(indexOptimalRtPerOD.get(setOfRoutesIndex)[fracIndex])[fracIndex];
 							//normalise
 							normCostDiff = normCostDiff / costPerRoute.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] ;
 
 							//call the GP task to obtain fraction of traffic to swap for this route (which we call delta) 
 							//We compute the delta using:
 							//-current routeFraction (which is the current Demand)
 							//-optimal route routeFraction
 							//-1/iteration
 							//-the normalised cost difference
 							//-the factor which reflects the cumulative changes done so far
 							
 							double delta;
 							if(cumulativeOfDeltas == null || setOfRoutesIndex >= cumulativeOfDeltas.size()){
 
 								delta = task.getTaskData().compute(new EvoDTAContext(rtFrac,
 										minDemandPerOD.get(setOfRoutesIndex)[fracIndex], 1.0/(double)iteration,  normCostDiff, 0.0));
 							}
 							else{
 								if(fractionsIndex >= cumulativeOfDeltas.get(setOfRoutesIndex).size()){
 									delta = task.getTaskData().compute(new EvoDTAContext(rtFrac,
 											minDemandPerOD.get(setOfRoutesIndex)[fracIndex], 1.0/(double)iteration,  normCostDiff, 0.0));
 								}
 								else{
 
 									delta = task.getTaskData().compute(new EvoDTAContext(rtFrac,
 											minDemandPerOD.get(setOfRoutesIndex)[fracIndex], 1.0/(double)iteration,  normCostDiff, cumulativeOfDeltas.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex]));
 								}
 							}
 							
 							//VALIDATION CHECK!
 							//if the delta is beyond twice the maximum possible rtFrac then stop 
 							// save null routes and routeFractions and return
 							if(Math.abs(delta) > 2*rtFrac){
 								setError(delta);
 								finalRoutes = null;
 								finalRouteFractions = null;
 								return;								
 							}
 							
 							//Otherwise simply limit the correction to some common sense values
 							if(delta < 0){
 								//min route swapping is 0
 								delta = 0;
 							}
 							else if(delta > rtFrac){
 								//maximum route swapping is the current traffic on that route
 								delta = rtFrac;
 							}
 							
 
							delta = -delta;
							
 							//add current delta to the cumulative of deltas
 							saveDeltaForARouteAndTime(setOfRoutesIndex, fractionsIndex, fracIndex, delta);
 
 							//save the new route fraction
							tempFractions[fracIndex] = rtFrac + delta;
 
 							//update the cumulativeOfRouteFractions
 							cumulativeOfRouteFractions[fracIndex] = cumulativeOfRouteFractions[fracIndex] + tempFractions[fracIndex];
 						}
 					}
 
 				}
 				else{
 					//NEW ROUTE!
 					//the following applies to routes already seen 
 					
 					//for new routes if the route is not optimal the "old and new" fractions are 0 
 					//there is no traffic to swap in any case.
 					//if the route is optimal it is treated in several lines below
 					
 					for(int fracIndex = 0; fracIndex < newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex).length; fracIndex++ ){
 						//save the new route fraction
 						if(newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] ==0){
 							tempFractions[fracIndex] = 0.0;
							saveDeltaForARouteAndTime(setOfRoutesIndex, fractionsIndex, fracIndex, 0.0);
 						}
 					}
 				}
 
 				//already save the references to the temporal fractions - note that the tempFractions for optimal routes are uninitialized
 				fractionsForOD.add(tempFractions);
 
 			}
 			
 			//finally normalise routeFractions for optimal routes
 			for(int fractionsIndex = 0; fractionsIndex < newRouteFractions.get(setOfRoutesIndex).size(); fractionsIndex++ ){
 
 				for(int fracIndex = 0; fracIndex < newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex).length; fracIndex++ ){
 
 					//compute correction only if the route fraction is optimal
 					if(newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] == 1){
						double delta =  cumulativeOfRouteFractions[fracIndex] / (double)(numOptimalRtsPerOD.get(setOfRoutesIndex)[fracIndex]);
						fractionsForOD.get(fractionsIndex)[fracIndex] = 1.0 - delta;
						saveDeltaForARouteAndTime(setOfRoutesIndex, fractionsIndex, fracIndex, delta);
 					}
 				}
 
 			}
 			
 		
 			//save the routeFractions for this OD pair
 			tempRouteFractions.add(fractionsForOD);
 		
 		}
 		
 
 		//save finalRoutes and finalRouteFractions
 		finalRoutes = cloneRoutes(newRoutes);
 		finalRouteFractions = tempRouteFractions;
 		
//		for(int setOfRoutesIndex = 0; setOfRoutesIndex< newRoutes.size(); setOfRoutesIndex++){
//			for(int fractionsIndex = 0; fractionsIndex < tempRouteFractions.get(setOfRoutesIndex).size(); fractionsIndex++ ){
//				System.out.println("OD " + setOfRoutesIndex + " route " + fractionsIndex);
//				System.out.println(Arrays.toString(tempRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)));
//				Scanner scan = new  Scanner(System.in);
//				scan.nextLine();
//			}
//		}
 
 	}
 
 	private void computeNumOptimalRtsAndMinRtFrac(){
 		numOptimalRtsPerOD = new ArrayList<int[]>();
 		minDemandPerOD = new ArrayList<double[]>();
 		indexOptimalRtPerOD = new ArrayList<int[]>();
 		for(int setOfRoutesIndex = 0; setOfRoutesIndex< newRoutes.size(); setOfRoutesIndex++){
 			int[] numberOfOptimalRoutes = new int[(int)(tEnd/tStep)];
 			double[] minDemand = new double[(int)(tEnd/tStep)];
 			int[] indexOptRt = new int[(int)(tEnd/tStep)];
 			Arrays.fill(numberOfOptimalRoutes, 0);
 
 			//compute for this OD pair the number of optimal routes per instant
 			for(int fractionsIndex = 0; fractionsIndex < newRouteFractions.get(setOfRoutesIndex).size(); fractionsIndex++ ){
 				for(int fracIndex = 0; fracIndex < newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex).length; fracIndex++ ){
 
 					if(newRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex] == 1.0){
 
 						numberOfOptimalRoutes[fracIndex] += 1;
 						indexOptRt[fracIndex] = fractionsIndex;
 						if(oldRouteFractions.get(setOfRoutesIndex).size() > fractionsIndex){
 
 							minDemand[fracIndex] = oldRouteFractions.get(setOfRoutesIndex).get(fractionsIndex)[fracIndex];
 						}
 						else{
 							minDemand[fracIndex] = 0.0;
 						}
 
 					}
 				}
 
 			}
 			numOptimalRtsPerOD.add(numberOfOptimalRoutes);
 			minDemandPerOD.add(minDemand);
 			indexOptimalRtPerOD.add(indexOptRt);
 		}
 	}
 
 
 	private void updateCostsPerRoute(){
 
 		costPerRoute =  new ArrayList<ArrayList<double[]>>();
 
 		//for all routes compute and save the cost!
 		for(int setOfRoutesIndex = 0; setOfRoutesIndex< newRoutes.size(); setOfRoutesIndex++){
 			for(int routeIndex = 0; routeIndex< newRoutes.get(setOfRoutesIndex).size(); routeIndex++){
 				
 				PathRepresentation path = newRoutes.get(setOfRoutesIndex).get(routeIndex);
 
 				int[] linkIndexes = path.linkIndexes;
 
 				for(int timeClick = 0; timeClick < (int)(tEnd/tStep); timeClick++){
 					double routeTT = 0;
 					for(int linkIndex : linkIndexes){
 						double[] cost = new double[linkSpeeds.get(linkIndex).length];
 
 						//compute instantaneous cost
 						for(int costIndex = 0; costIndex < linkSpeeds.get(linkIndex).length; costIndex++ ){
 							cost[costIndex] = tfData.links.get(linkIndex).length / linkSpeeds.get(linkIndex)[costIndex];
 						}
 						//compute complete routeTT (route cost or travel time)
 						routeTT = routeTT + TravelTimeManager.computeTravelTimeForGivenCost(cost,timeClick*tStep + routeTT, tEnd, tStep);
 
 					}
 					//save the route travel time as cost for this route and time
 					saveCostForARouteAndTime(setOfRoutesIndex, routeIndex, timeClick, routeTT);
 				}
 
 			}
 		}
 
 	}
 
 	private void saveCostForARouteAndTime(int setOfRoutesIndex, int routeIndex, int timeClick, double routeTT){
 
 		//initialisation 
 		if(costPerRoute == null){
 			costPerRoute = new  ArrayList<ArrayList<double[]>>();
 		}
 
 		//required in case of new OD pair
 		if(setOfRoutesIndex >= costPerRoute.size()){
 			costPerRoute.add(new ArrayList<double[]>());
 		}
 
 		//required in case of new route that was not considered previously
 		if(routeIndex >= costPerRoute.get(setOfRoutesIndex).size()){
 			costPerRoute.get(setOfRoutesIndex).add(new double[(int)(tEnd/tStep)]);
 		}
 
 		//save the cost (route travel time)
 		costPerRoute.get(setOfRoutesIndex).get(routeIndex)[timeClick] = routeTT;		
 
 	}
 
 
 	private void saveDeltaForARouteAndTime(int setOfRoutesIndex, int routeIndex, int timeClick, double delta){
 
 		if(cumulativeOfDeltas == null){
 			cumulativeOfDeltas = new  ArrayList<ArrayList<double[]>>();
 		}
 
 		//required in case of new OD pair
 		if(setOfRoutesIndex >= cumulativeOfDeltas.size()){
 			cumulativeOfDeltas.add(new ArrayList<double[]>());
 		}
 
 		//required in case of new route that was not considered previously
 		if(routeIndex >= cumulativeOfDeltas.get(setOfRoutesIndex).size()){
 			cumulativeOfDeltas.get(setOfRoutesIndex).add(new double[(int)(tEnd/tStep)]);
 			//no changes so far hence all to 0
 			Arrays.fill(cumulativeOfDeltas.get(setOfRoutesIndex).get(routeIndex), 0);
 		}
 
 		//add the delta to the cumulative
 		cumulativeOfDeltas.get(setOfRoutesIndex).get(routeIndex)[timeClick] = cumulativeOfDeltas.get(setOfRoutesIndex).get(routeIndex)[timeClick] + delta;		
 
 	}
 
 	public void setupGenParams(EvoDTATask task){
 
 		this.task = task;
 
 	}
 
 	public double getError() {
 		return error;
 	}
 
 	public void setError(double error) {
 		this.error = error;
 	}
 
 	public GPStatus getGpStatus() {
 		return gpStatus;
 	}
 
 	public void setGpStatus(GPStatus gpStatus) {
 		this.gpStatus = gpStatus;
 	}
 
 }
