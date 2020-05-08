 package gna;
 
 /**
  * A tour constructed using the nearest neighbor heuristic.
  */
 public class NearestNeighborTour extends IncrementallyConstructedTour {
 
 	public NearestNeighborTour(World world) {
 		super(world);
 	}
 
 	@Override
 	public void insert(Point point) {
 		
 		if(this.getVisitSequence().size() <= 1){
 			this.addToTour(point);
 			return;
 		}
 		
 		int bestIndex = 0;
 		double bestDistance = this.getVisitSequence().get(0).distanceTo(point);
 		
 		for(int i = 0; i < this.getVisitSequence().size(); i++){
 			double currentDistance = this.getVisitSequence().get(i).distanceTo(point);
 			if(currentDistance < bestDistance){
 				bestIndex = i;
 				bestDistance = currentDistance;
 			}
 		}
 		
 		this.addToTour(bestIndex + 1, point);
 		
 	}
 
 }
