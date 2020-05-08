 package gna;
 
 /**
  * A tour constructed using the smallest increase heuristic.
  */
 public class SmallestIncreaseTour extends IncrementallyConstructedTour {
 
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
 		
 		for(int i = 0; i < this.getVisitSequence().size(); i++){
 			double currentPossibleTotalDistance = this.newPossibleTotalDistance(i, point, currentTotalDistance);
 			if(currentPossibleTotalDistance < bestTotalDistance){
 				bestIndex = i;
 				bestTotalDistance = currentPossibleTotalDistance;
 			}
 		}
 		
		this.addToTour(bestIndex + 1, point);
 		
 	}
 	
 	private double newPossibleTotalDistance(int index, Point point, double cachedTotalDistance){
		//TODO
		return 0;
 	}
 	
 	private int previousIndex(int index){
 		if(index == 0)
 			return this.getVisitSequence().size() - 1;
 		return index - 1;
 	}
 	
 	private int nextIndex(int index){
 		if(index == this.getVisitSequence().size() - 1)
 			return 0;
 		return index + 1;
 	}
 }
