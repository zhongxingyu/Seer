 package com.evervoid.client.graphics;
 
 import com.evervoid.client.graphics.geometry.AnimatedTranslation;
 import com.evervoid.state.geometry.Dimension;
 import com.evervoid.state.geometry.GridLocation;
 import com.jme3.math.Vector2f;
 
 public class GridNode extends EverNode
 {
 	protected final Grid aGrid;
 	protected GridLocation aGridLocation;
 	protected final AnimatedTranslation aGridTranslation = getNewTranslationAnimation();
 
 	public GridNode(final Grid grid, final GridLocation location)
 	{
 		aGrid = grid;
 		aGridLocation = constrainToGrid(location);
 		registerToGrid();
 		updateTranslation();
 	}
 
 	/**
 	 * Constraints the provided GridPoint to the grid size
 	 * 
 	 * @param location
 	 *            The GridPoint to constrain
 	 * @return The constrained GridPoint
 	 */
 	protected GridLocation constrainToGrid(final GridLocation location)
 	{
 		return location.constrain(0, 0, aGrid.getColumns(), aGrid.getRows());
 	}
 
 	public Vector2f getCellCenter()
 	{
 		return aGrid.getCellCenter(aGridLocation);
 	}
 
 	public Dimension getDimension()
 	{
 		return aGridLocation.dimension;
 	}
 
 	public Vector2f getGridTranslation()
 	{
 		return aGridTranslation.getTranslation2f();
 	}
 
 	public GridLocation getLocation()
 	{
 		return aGridLocation;
 	}
 
 	public Vector2f getTranslation()
 	{
 		return aGridTranslation.getTranslation2f();
 	}
 
 	protected void hasMoved()
 	{
 		// Overridden by subclasses
 	}
 
 	public void moveTo(final GridLocation destination)
 	{
 		unregisterFromGrid();
 		aGridLocation = constrainToGrid(destination);
 		registerToGrid();
 		updateTranslation();
 	}
 
 	/**
 	 * Notifies the Grid about this object's occupied cells
 	 */
 	protected void registerToGrid()
 	{
 		aGrid.registerNode(this, aGridLocation);
 	}
 
 	public void smoothMoveTo(final GridLocation destination)
 	{
		// unregisterFromGrid();
 		aGridLocation = constrainToGrid(destination);
		// registerToGrid();
 		aGridTranslation.smoothMoveTo(aGrid.getCellCenter(destination)).start(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				if (!aGridTranslation.isInProgress()) {
 					updateTranslation();
 				}
 			}
 		});
 	}
 
 	protected void unregisterFromGrid()
 	{
 		aGrid.unregisterNode(this, aGridLocation);
 	}
 
 	protected void updateTranslation()
 	{
 		aGridTranslation.setTranslationNow(getCellCenter());
 		hasMoved();
 	}
 }
