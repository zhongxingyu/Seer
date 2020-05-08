 package com.tacalpha.grid;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class RadiusAOE extends AreaOfEffect {
 	protected Set<GridPoint> relativeLocations = new HashSet<GridPoint>();
 
 	public RadiusAOE(int radius) {
		radius = radius - 1;
 		for (int x = -radius; x <= radius; x++) {
 			int leftover = radius - Math.abs(x);
 			for (int y = -leftover; y <= leftover; y++) {
 				this.relativeLocations.add(new GridPoint(x, y));
 			}
 		}
 	}
 
 	@Override
 	public boolean contains(GridPoint referencePoint, GridPoint targetPoint) {
 		return this.relativeLocations.contains(new GridPoint(targetPoint.getColumn() - referencePoint.getColumn(), targetPoint.getRow()
 				- referencePoint.getRow()));
 	}
 
 	@Override
 	public Set<GridPoint> getAllAffectedLocations(GridPoint referencePoint) {
 		Set<GridPoint> results = new HashSet<GridPoint>();
 		for (GridPoint relativeLocation : this.relativeLocations) {
 			int x = referencePoint.getColumn() + relativeLocation.getColumn();
 			int y = referencePoint.getRow() + relativeLocation.getRow();
 			if (this.grid.isInBounds(x, y)) {
 				results.add(new GridPoint(x, y));
 			}
 		}
 		return results;
 	}
 }
