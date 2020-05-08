 package gna;
 
 /**
  * A tour constructed using the smallest increase heuristic.
  */
 public class SmallestIncreaseTour extends IncrementallyConstructedTour {
	//TODO: bug: niet de beste route
 
 	public SmallestIncreaseTour(World world) {
 		super(world);
 	}
 
 	@Override
 	public void insert(Point point) {
 		
 		if(this.getVisitSequence().size() <= 1){
 			this.addToTour(point);
 			return;
 		}
 		
 		double currentTotalDistance = this.getTotalDistance();
 		
 		int bestIndex = 0;
 		double bestTotalDistance = this.newPossibleTotalDistance(0, point, currentTotalDistance);
 		
		for(int i = 0; i <= this.getVisitSequence().size(); i++){
			
 			double currentPossibleTotalDistance = this.newPossibleTotalDistance(i, point, currentTotalDistance);
			
 			if(currentPossibleTotalDistance < bestTotalDistance){
 				bestIndex = i;
 				bestTotalDistance = currentPossibleTotalDistance;
 			}
 		}
 		
 		this.addToTour(bestIndex, point);
 		
 	}
 	
 }
