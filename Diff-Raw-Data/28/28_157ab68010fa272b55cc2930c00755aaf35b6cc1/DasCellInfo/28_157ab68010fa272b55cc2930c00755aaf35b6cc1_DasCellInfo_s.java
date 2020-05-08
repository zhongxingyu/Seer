 package agents;
 
 import pplanning.simviewer.model.GridCell;
 
 class DasCellInfo implements Comparable<DasCellInfo> {
 	private DasCellInfo parent = null;
 	private final GridCell cell;
 	private float gCost;
 	private float hCost;
 	private float fCost;
 	private int dCheapestRaw;
 	private CellSetMembership cellSetMembership;
 	private final int nExpansionNumber; // TODO: should this be final?
 
 	// The below are required for the d_cheapest calculation
 	private int dError = 0;
 	//private float dAverageError = 0.0f;
 	private int nCumulativeErrorOfPartialSolution = 0;
 	private int nPartialSolutionDepth = 0; // Node depth so far
 
 	private final float EPSILON = 0.001f; // used for floating point comparisons
 
 	public int getDepth()
 	{
 		return(this.nPartialSolutionDepth);
 	}
 
 	//		public float getAverageError()
 	//		{
 	//			return(this.dAverageError);
 	//		}
 
 	public int getCumulativeError()
 	{
 		return(nCumulativeErrorOfPartialSolution);
 	}
 
 	public int getError()
 	{
 		// TODO: possibly not needed
 		return(this.dError);
 	}
 
 	public float getAverageError()
 	{
 		float avgError;
 		if (this.nPartialSolutionDepth != 0)
 		{
 			avgError = (float)this.nCumulativeErrorOfPartialSolution / this.nPartialSolutionDepth;
 		}
 		else
 		{
 			avgError = 0;
 		}
 		return(avgError);
 	}
 
 	/**
 	 * Set the estimated number of expansions from this node to the goal state.
 	 * This is the raw estimate, not including mean step error.
 	 *
 	 * TODO: Should this be a float? I think int makes sense for a d estimate.
 	 *
 	 * @param dCheapestRaw the new d value
 	 */
 	public void setDCheapestRaw(int dCheapestRaw) {
 		this.dCheapestRaw = dCheapestRaw;
 	}
 
 	/**
 	 * Get the estimated number of expansions from this node to the goal state.
 	 * This is the raw estimate, not including mean step error.
 	 *
 	 * @return the estimated number of expansions to the goal state
 	 */
 	public int getDCheapestRaw() {
 		return this.dCheapestRaw;
 	}
 
 	public float getDCheapestWithError() {
 		float dCheapestWithError = 0.0f;
 		float avgError = this.getAverageError();
 
 
 		if (avgError < 1-EPSILON)
 		{
 			dCheapestWithError = this.dCheapestRaw / (1-avgError);
 		}
 		else
 		{
 			dCheapestWithError = Float.POSITIVE_INFINITY;
 		}
 
 		return(dCheapestWithError);
 	}
 
 	/** Read only. */
 	public GridCell getCell() {
 		return this.cell;
 	}
 
 	public DasCellInfo getParent() {
 		return this.parent;
 	}
 	public void setParent(DasCellInfo parent) {
 		this.parent = parent;
 	}
 
 	public float getGCost() {
 		return this.gCost;
 	}
 	public void setGCost(float gCost) {
 		this.gCost = gCost;
 		updateFCost();
 	}
 
 	public float getHCost() {
 		return this.hCost;
 	}
 
 	public void setHCost(float hCost) {
 		this.hCost = hCost;
 		updateFCost();
 	}
 
 	/** Read only. */
 	public float getFCost() {
 		return this.fCost;
 	}
 
 	public CellSetMembership getCellMembership() {
 		return this.cellSetMembership;
 	}
 
 	public void setCellMembership(CellSetMembership cellMembership) {
 		this.cellSetMembership = cellMembership;
 	}
 
 	public int getExpansionNumber()
 	{
 		return(this.nExpansionNumber);
 	}
 
 	/**
 	 * Compare the f cost of this cell to another.
 	 * @other the CellInfo to be compared
 	 */
 	public int compareTo(DasCellInfo other) {
 		// compare f cost of either cell
 
 		int nReturnValue;
 		// Check if the values are "the same" 
 		if (Math.abs(this.fCost - other.fCost) < EPSILON)
 		{
 			if (Math.abs(this.hCost - other.hCost) < EPSILON)
 			{
 				// Both f and h are the same, in which case, return 0
 				nReturnValue = 0;
 			}
 			else if (this.hCost < other.hCost)
 			{
 				nReturnValue = -1;
 			}
 			else
 			{
 				nReturnValue = 1;
 			}
 		}
 		else if (this.fCost < other.fCost)
 		{
 			nReturnValue = -1;
 		}
 		else
 		{
 			nReturnValue = 1;
 		}
 		return(nReturnValue);
 	}
 
 	void updateFCost() {
 		this.fCost = gCost + hCost;
 	}
 
 	/**
 	 * Create a new CellInfo for specified cell with specified g and h costs.
 	 * @cell           the cell
 	 * @gCost          the cost of the path from start to cell
 	 * @hCost          the heuristic estimate of the remaining cost to the goal
 	 * @cellMembership the set that this node should start in
 	 * @parent         the parent node of this cell
 	 */
 	public DasCellInfo(GridCell cell, float gCost, float hCost,
 			CellSetMembership cellMembership, DasCellInfo parent, int nExpansion,
 			int dCheapest, int error) {
 		this(cell, gCost, hCost, cellMembership, nExpansion, dCheapest);
 		this.parent = parent;
 		this.dError = error;
 		this.nPartialSolutionDepth = parent.getDepth() + 1;
 		this.nCumulativeErrorOfPartialSolution = parent.getCumulativeError() + error;
 	}
 
 	/**
 	 * Create a new CellInfo for specified cell with specified g and h costs.
 	 * @cell           the cell
 	 * @gCost          the cost of the path from start to cell
 	 * @hCost          the heuristic estimate of the remaining cost to the goal
 	 * @cellMembership the set that this node should start in
 	 */
 	public DasCellInfo(GridCell cell, float gCost, float hCost, CellSetMembership cellMembership, 
 			int nExpansion, int dCheapest) {
 		this.cell = cell;
 		this.gCost = gCost;
 		updateFCost();
 		this.cellSetMembership = cellMembership;
 		this.nExpansionNumber = nExpansion;
 		this.dCheapestRaw = dCheapest;
 	}
 }
