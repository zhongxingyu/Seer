 package model.MARK_I;
 
 import java.awt.Dimension;
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.util.List;
 import java.util.Set;
 import java.util.HashSet;
 
 /**
  * Provides implementation for running the spatial learning algorithm on a
  * Region. This class provides methods that simulate brain activity within the
  * Neocortex for object recognition.
  *
  * @author Quinn Liu (quinnliu@vt.edu)
  * @version July 29, 2013
  */
 public class SpatialPooler extends Pooler {
     private Set<Column> activeColumns;
     private Set<ColumnPosition> activeColumnPositions;
 
     private final float MINIMUM_COLUMN_FIRING_RATE = 0.01f;
 
     public SpatialPooler(Region newRegion) {
 	if (newRegion == null) {
 	    throw new IllegalArgumentException(
 		    "newRegion in SpatialPooler class constructor cannot be null");
 	}
 	this.region = newRegion;
 	this.activeColumns = new HashSet<Column>();
 	this.activeColumnPositions = new HashSet<ColumnPosition>();
     }
 
     /**
      * This method recomputes all Column states within this functional Region.
      * Through local inhibition, only a sparse set of Columns become active to
      * represent the current 2D array of sensory data or a lower Region's
      * output.
      *
      * @return A sparse set of active Columns within this Region.
      */
     public Set<Column> performSpatialPoolingOnRegion() {
 	Column[][] columns = this.region.getColumns();
 	for (int x = 0; x < columns.length; x++) {
 	    for (int y = 0; y < columns[0].length; y++) {
 		this.computeColumnOverlapScore(columns[x][y]);
 	    }
 	}
 
 	// a sparse set of Columns become active after local inhibition
	this.computeActiveColumnsOfRegion();
 
 	// // simulate learning by boosting specific Synapses
 	this.regionLearnOneTimeStep();
 
 	return this.activeColumns;
     }
 
     public Set<ColumnPosition> getActiveColumnPositions() {
 	return this.activeColumnPositions;
     }
 
     /**
      * The overlapScore for each Column is the number of Synapses connected to
      * Cells with active inputs multiplied by that Columns's boostValue. If a
      * Column's overlapScore is below minOverlap, that Column's overlapScore is
      * set to 0.
      */
     void computeColumnOverlapScore(Column column) {
 	if (column == null) {
 	    throw new IllegalArgumentException(
 		    "the Column in SpatialPooler method computeColumnOverlapScore cannot be null");
 	}
 	int newOverlapScore = column.getProximalSegment()
 		.getNumberOfActiveSynapses();
 
 	// compute minimumOverlapScore assuming all proximalSegments are
 	// connected to the same number of synapses
 	Column[][] columns = this.region.getColumns();
 	int regionMinimumOverlapScore = this.region.getMinimumOverlapScore();
 	if (newOverlapScore < regionMinimumOverlapScore) {
 	    newOverlapScore = 0;
 	} else {
 	    newOverlapScore = (int) (newOverlapScore * column.getBoostValue());
 	}
 	column.setOverlapScore(newOverlapScore);
     }
 
     /**
      * This method is called by performSpatialPooling and computes the
      * activeColumns(t)-the list of Columns that win due to the bottom-up input
      * at time t.
      */
     void computeActiveColumnsOfRegion() {
 	Column[][] columns = this.region.getColumns();
 	for (int x = 0; x < columns.length; x++) {
 	    for (int y = 0; y < columns[0].length; y++) {
 		columns[x][y].setActiveState(false);
 		this.updateNeighborColumns(x, y);
 
 		// necessary for calculating kthScoreOfColumns
 		List<ColumnPosition> neighborColumnPositions = new ArrayList<ColumnPosition>();
 		neighborColumnPositions = columns[x][y].getNeighborColumns();
 		List<Column> neighborColumns = new ArrayList<Column>();
 		for (ColumnPosition columnPosition : neighborColumnPositions) {
 		    neighborColumns
 			    .add(columns[columnPosition.getX()][columnPosition
 				    .getY()]);
 		}
 
 		int minimumLocalOverlapScore = this.kthScoreOfColumns(
 			neighborColumns, this.region.getDesiredLocalActivity());
 
 		// more than (this.region.desiredLocalActivity) number of
 		// columns can become active since it is applied to each
 		// Column object's neighborColumns
 		if (columns[x][y].getOverlapScore() > 0
 			&& columns[x][y].getOverlapScore() >= minimumLocalOverlapScore) {
 		    columns[x][y].setActiveState(true);
 
 		    this.addActiveColumn(columns[x][y]);
 		    this.activeColumnPositions.add(new ColumnPosition(x, y));
 		}
 	    }
 	}
     }
 
     /**
      * This method models spike-timing dependent plasticity. This is also known
      * as Hebb's Rule. The inhibitionRadius for the Region is also computed and
      * updated here.
      */
     void regionLearnOneTimeStep() {
 	Column[][] columns = this.region.getColumns();
 
 	if (super.getLearningState()) {
 	    for (int x = 0; x < columns.length; x++) {
 		for (int y = 0; y < columns[0].length; y++) {
 		    if (columns[x][y].getActiveState()) {
 			// increase and decrease of proximal segment synapses
 			// based
 			// on each Synapses's activeState
 			Set<Synapse<Cell>> synapses = columns[x][y]
 				.getProximalSegment().getSynapses();
 			for (Synapse<Cell> synapse : synapses) {
 			    if (synapse.getConnectedCell() != null
 				    && synapse.getConnectedCell()
 					    .getActiveState()) {
 				// models long term potentiation
 				synapse.increasePermanence();
 			    } else {
 				// models long term depression
 				synapse.decreasePermanence();
 			    }
 			}
 		    }
 		}
 	    }
 	}
 
 	// TODO: the remainder of this method still needs to be tested
 	for (int x = 0; x < columns.length; x++) {
 	    for (int y = 0; y < columns[0].length; y++) {
 		if (columns[x][y].getActiveState()) {
 		    // increase and decrease of proximal Segment Synapses based
 		    // on each Synapses's activeState
 		    // columns[x][y].performBoosting();
 
 		    // 2 methods to help a Column learn connections:
 		    // 1) If activeDutyCycle(measures winning rate) is too low.
 		    // The overall boost value of the Columns is increased.
 		    // 2) If overlapDutyCycle(measures connected Synapses with
 		    // inputs) is too low, the permanence values of the
 		    // Column's Synapses are boosted.
 
 		    // neighborColumns are already up to date.
 		    List<ColumnPosition> neighborColumnPositions = columns[x][y]
 			    .getNeighborColumns();
 
 		    List<Column> neighborColumns = new ArrayList<Column>();
 		    for (ColumnPosition columnPosition : neighborColumnPositions) {
 			// add the Column object to neighborColumns
 			neighborColumns
 				.add(columns[columnPosition.getX()][columnPosition
 					.getY()]);
 		    }
 
 		    float maximumActiveDutyCycle = this.region
 			    .maximumActiveDutyCycle(neighborColumns);
 		    if (maximumActiveDutyCycle == 0) {
 			maximumActiveDutyCycle = 0.1f;
 		    }
 
 		    // neighborColumns are no longer necessary for calculations
 		    // in this time step
 		    columns[x][y].getNeighborColumns().clear();
 
 		    // minDutyCycle represents the minimum desired firing rate
 		    // for a Column(number of times it becomes active over some
 		    // number of iterations).
 		    // If a Column's firing rate falls below this value, it will
 		    // be boosted.
 		    float minimumActiveDutyCycle = this.MINIMUM_COLUMN_FIRING_RATE
 			    * maximumActiveDutyCycle;
 
 		    // 1) boost if activeDutyCycle is too low
 		    columns[x][y].updateActiveDutyCycle();
 
 		    columns[x][y].setBoostValue(columns[x][y]
 			    .boostFunction(minimumActiveDutyCycle));
 
 		    // 2) boost if overlapDutyCycle is too low
 		    this.updateOverlapDutyCycle(x, y);
 		    if (columns[x][y].getOverlapDutyCycle() < minimumActiveDutyCycle
 			    && this.getLearningState()) {
 			columns[x][y]
 				.increaseProximalSegmentSynapsePermanences(10);
 			// TODO: more biologically accurate
 		    }
 		}
 	    }
 	}
 	this.region
 		.setInhibitionRadius((int) averageReceptiveFieldSizeOfRegion());
     }
 
     /**
      * Adds all Columns within inhitionRadius of the parameter Column to the
      * neighborColumns field within the parameter Column.
      *
      * @param columnXAxis
      *            position of Column within Region along x-axis
      * @param columnYAxis
      *            position of Column within Region along y-axis
      */
     void updateNeighborColumns(int columnXAxis, int columnYAxis) {
 	if (columnXAxis < 0 || columnXAxis > this.region.getXAxisLength()
 		|| columnYAxis < 0
 		|| columnYAxis > this.region.getYAxisLength()) {
 	    throw new IllegalArgumentException(
 		    "the Column being updated by the updateNeighborColumns method"
 			    + "in SpatialPooler class does not exist within the Region");
 	}
 	int localInhibitionRadius = this.region.getInhibitionRadius();
 	assert (localInhibitionRadius >= 0);
 
 	// forced inhibition of adjacent Columns
 	int xInitial = Math.max(0, columnXAxis - localInhibitionRadius);
 	int yInitial = Math.max(0, columnYAxis - localInhibitionRadius);
 
 	// System.out.println("xInitial, yInitial: " + xInitial + ", " +
 	// yInitial);
 	int xFinal = Math.min(this.region.getXAxisLength(), columnXAxis
 		+ localInhibitionRadius);
 	int yFinal = Math.min(this.region.getYAxisLength(), columnYAxis
 		+ localInhibitionRadius);
 
 	// to allow double for loop to reach end portion of this.allColumns
 	xFinal = Math.min(this.region.getXAxisLength(), xFinal + 1);
 	yFinal = Math.min(this.region.getYAxisLength(), yFinal + 1);
 	// System.out.println("xFinal, yFinal: " + xFinal + ", " + yFinal);
 
 	Column[][] columns = this.region.getColumns();
 
 	if (columns[columnXAxis][columnYAxis].getNeighborColumns().size() != 0) {
 	    // remove neighbors of Column computed with old inhibitionRadius
 	    columns[columnXAxis][columnYAxis].getNeighborColumns().clear();
 	}
 
 	for (int columnIndex = xInitial; columnIndex < xFinal; columnIndex++) {
 	    for (int rowIndex = yInitial; rowIndex < yFinal; rowIndex++) {
 		if (columnIndex == columnXAxis && rowIndex == columnYAxis) {
 		    // TODO: To make inhibition a circle around input Column,
 		    // change to remove corners of this rectangle inhibition
 		} else {
 		    Column newColumn = columns[columnIndex][rowIndex];
 		    if (newColumn != null) {
 			columns[columnXAxis][columnYAxis].getNeighborColumns()
 				.add(new ColumnPosition(columnIndex, rowIndex));
 		    }
 		}
 	    }
 	}
     }
 
     /**
      * @param neighborColumns
      * @param desiredLocalActivity
      * @return the kth highest overlapScore value of a Column object within the
      *         neighborColumns list.
      */
     int kthScoreOfColumns(List<Column> neighborColumns, int desiredLocalActivity) {
 	if (neighborColumns == null) {
 	    throw new IllegalArgumentException(
 		    "neighborColumns in SpatialPooler method kthScoreOfColumns cannot be null");
 	}
 	// TreeSet data structures' elements are automatically sorted.
 	Set<Integer> overlapScores = new TreeSet<Integer>();
 	for (Column column : neighborColumns) {
 	    overlapScores.add(column.getOverlapScore());
 	}
 
 	// if invalid or no local activity is desired, it is changed so that the
 	// highest overlapScore is returned.
 	if (desiredLocalActivity <= 0) {
 	    throw new IllegalStateException(
 		    "desiredLocalActivity cannot be <= 0");
 	}
 
 	// k is the index of the overlapScore to be returned. The overlapScore
 	// is the score at position k(counting from the top) of all
 	// overlapScores when arranged from smallest to greatest.
 	int k = Math.max(0, overlapScores.size() - desiredLocalActivity);
 	return (Integer) overlapScores.toArray()[k];
     }
 
     /**
      * Returns the radius of the average connected receptive field size of all
      * the Columns. The connected receptive field size of a Column includes only
      * the total distance of the Column's connected Synapses's cell's distance
      * from the Column divided by the number of Synapses the Column has. In
      * other words the total length of Synapse distances for a Column divided by
      * the number of Synapses. For spatial pooling since the number of Synapses
      * from proximal Segments to lower Region Neurons is constant after
      * initialization, averageReceptiveFieldSize of a Column will remain
      * constant, but will be different for different Columns based on how they
      * are connected to the SensorCellLayer or lower Region.
      *
      * @return The average connected receptive field size.
      */
     float averageReceptiveFieldSizeOfRegion() {
 	double totalSynapseDistanceFromOriginColumn = 0.0;
 
 	int numberOfConnectedSynapses = 0;
 	Column[][] columns = this.region.getColumns();
 	for (int x = 0; x < columns.length; x++) {
 	    for (int y = 0; y < columns[0].length; y++) {
 		Set<Synapse<Cell>> synapses = columns[x][y]
 			.getProximalSegment().getSynapses();
 
 		Set<Synapse<Cell>> connectedSynapes = new HashSet<Synapse<Cell>>();
 
 		for (Synapse<Cell> synapse : synapses) {
 		    if (synapse.getConnectedCell() != null) {
 			connectedSynapes.add(synapse);
 		    }
 		}
 
 		// iterates over every connected Synapses and sums the
 		// distances from it's original Column to determine the
 		// average receptive field
 		for (Synapse<?> synapse : connectedSynapes) {
 		    // NOTE: although it first appears these calculations are
 		    // incorrect since a Column at position (0, 0) is too close
 		    // to the lower Cell at (0, 0) because this is an AVERAGE
 		    // calculation it does not matter.
 		    double dx = x - synapse.getCellXPosition();
 		    double dy = y - synapse.getCellYPosition();
 		    double synapseDistance = Math.sqrt(dx * dx + dy * dy);
 		    totalSynapseDistanceFromOriginColumn += synapseDistance;
 		    numberOfConnectedSynapses++;
 		}
 	    }
 	}
 	float averageReceptiveFieldOfLowerLayer = (float) (totalSynapseDistanceFromOriginColumn / numberOfConnectedSynapses);
 
 	float squareRegionAxisLength = (float) Math.sqrt(this.region
 		.getXAxisLength() * this.region.getYAxisLength());
 
 	Dimension bottomLayerDimensions = this.region
 		.getBottomLayerXYAxisLength();
 	int greatestSynapseXIndex = bottomLayerDimensions.width;
 	int greatestSynapseYIndex = bottomLayerDimensions.height;
 
 	int squareLowerRegionAxisLength = (greatestSynapseXIndex + greatestSynapseYIndex) / 2;
 
 	float inhibitionRadius = (averageReceptiveFieldOfLowerLayer / squareLowerRegionAxisLength)
 		* squareRegionAxisLength;
 	return inhibitionRadius;
     }
 
     void addActiveColumn(Column activeColumn) {
 	if (activeColumn == null) {
 	    throw new IllegalArgumentException(
 		    "activeColumn in SpatialPooler class method addActiveColumn cannot be null");
 	}
 	this.activeColumns.add(activeColumn);
     }
 
     /**
      * Compute a moving average of how often this Column has overlap greater
      * than minimumDutyOverlap. Exponential Moving Average(EMA): St = a * Yt +
      * (1 - a) * St - 1.
      */
     void updateOverlapDutyCycle(int columnXAxis, int columnYAxis) {
 	if (columnXAxis < 0 || columnXAxis > this.region.getXAxisLength()
 		|| columnYAxis < 0
 		|| columnYAxis > this.region.getYAxisLength()) {
 	    throw new IllegalArgumentException(
 		    "the Column being updated by the updateOverlapDutyCycle method"
 			    + "in SpatialPooler does not exist within the Region");
 	}
 	// Note whenever updateOverlapDutyCycle() is called, the
 	// overlapDutyCycle
 	// is always decremented less and less but only incremented if the
 	// Column's
 	// overlapScore was greater than the Region's minimumOverlapScore.
 	// Furthermore, the increment applied to overlapDutyCycle is a constant
 	// representing the maximum decrement of overlapDutyCycle from initial
 	// value 1. Because of this a Column's overlapDutyCycle has a upper
 	// bound of 1.
 	Column[][] columns = this.region.getColumns();
 	float newOverlapDutyCycle = (1.0f - Column.EXPONENTIAL_MOVING_AVERAGE_AlPHA)
 		* columns[columnXAxis][columnYAxis].getOverlapDutyCycle();
 
 	if (columns[columnXAxis][columnYAxis].getOverlapScore() > this.region
 		.getMinimumOverlapScore()) {
 	    newOverlapDutyCycle += Column.EXPONENTIAL_MOVING_AVERAGE_AlPHA;
 	}
 	columns[columnXAxis][columnYAxis]
 		.setOverlapDutyCycle(newOverlapDutyCycle);
     }
 
     public Region getRegion() {
 	return this.region;
     }
 
     @Override
     public String toString() {
 	StringBuilder stringBuilder = new StringBuilder();
 	stringBuilder.append("\n===============================");
 	stringBuilder.append("\n---SpatialPooler Information---");
 	stringBuilder.append("\n     biological region name: ");
 	stringBuilder.append(this.region.getBiologicalName());
 	stringBuilder.append("\n# of activeColumns produced: ");
 	stringBuilder.append(this.activeColumnPositions.size());
 	stringBuilder.append("\n===============================");
 	String spatialPoolerInformation = stringBuilder.toString();
 	return spatialPoolerInformation;
     }
 }
