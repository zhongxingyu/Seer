 package gna;
 
 /**
  * Tours that are constructed incrementally by inserting the elements in the
  * given world one by one.
  */
 public abstract class IncrementallyConstructedTour extends Tour {
 
 	public IncrementallyConstructedTour(World world) {
 		super(world);
 		/**
 		 * Note: we expect the elements to be inserted in the same order they
 		 * are added to the world.
 		 */
 		for (Point point : world.getPoints()) {
 			insert(point);
 		}
 	}
 
 	protected abstract void insert(Point point);
 	
 	protected void addToTour(int index, Point p){
 		this.getVisitSequence().add(index, p);				
 	}
 	
 	protected void addToTour(Point p){
		this.getVisitSequence().add(0, p);
 	}
 	
 	protected double newPossibleTotalDistance(int index, Point point, double cachedTotalDistance){
 		int previousIndex = this.previousIndex(index);
 		
 		if(index >= this.getVisitSequence().size())
 			index = 0;
 		
 		double distanceToPrevious = this.getVisitSequence().get(previousIndex).distanceTo(point);
 		double distanceToNext = this.getVisitSequence().get(index).distanceTo(point);
		double distanceRemoved = this.getVisitSequence().get(previousIndex).distanceTo(this.getVisitSequence().get(index));
 		
		return cachedTotalDistance + distanceToPrevious + distanceToNext - distanceRemoved;
 	}
 
 }
