 package com.evervoid.client.graphics;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.client.EverNode;
 import com.evervoid.client.graphics.geometry.Rectangle;
 import com.evervoid.client.graphics.materials.PlainColor;
 import com.evervoid.state.Dimension;
 import com.evervoid.state.GridLocation;
 import com.evervoid.state.Point;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Line;
 
 public class Grid extends EverNode
 {
 	/**
 	 * Layers of elements on the grid
 	 */
 	public static enum CellLayer
 	{
 		BACKGROUND, FOREGROUND, HOVER;
 		public float getZOffset()
 		{
 			switch (this) {
 				case BACKGROUND:
 					return -0.1f;
 				case FOREGROUND:
 					return 0.01f;
 				case HOVER:
 					return 100f;
 			}
 			return 0;
 		}
 	}
 
 	/**
 	 * Equivalent to a boolean right now, but may add more modes in the future (Hover color mask mode, overwrite mode, color
 	 * blend mode, etc)
 	 */
 	public static enum HoverMode
 	{
 		OFF, ON
 	};
 
 	/**
 	 * Given a GridLocation, iterates over the cells in it
 	 * 
 	 * @param location
 	 *            A GridLocation
 	 * @return An Iterable over the set of points in it
 	 */
 	public static Iterable<Point> getPoints(final GridLocation location)
 	{
 		final List<Point> points = new ArrayList<Point>();
 		for (int x = 0; x < location.getWidth(); x++) {
 			for (int y = 0; y < location.getHeight(); y++) {
 				points.add(location.origin.add(x, y));
 			}
 		}
 		return points;
 	};
 
 	private final Map<Point, Set<GridNode>> aCellContents = new HashMap<Point, Set<GridNode>>();
 	private final float aCellHeight;
 	private final float aCellWidth;
 	private final int aColumns;
 	private HoverMode aHandleOver = HoverMode.OFF;
 	private ColorRGBA aHoverColor;
 	private GridCellsNode aHoveredCell = null;
 	private final float aLineWidth;
 	private final int aRows;
 
 	public Grid(final Dimension size, final float cellWidth, final float cellHeight, final float lineWidth,
 			final ColorRGBA gridLineColor)
 	{
 		aRows = size.height;
 		aColumns = size.width;
 		aCellWidth = cellWidth;
 		aCellHeight = cellHeight;
 		aLineWidth = lineWidth;
 		final Material gridMat = new PlainColor(gridLineColor);
 		for (int x = 0; x <= aColumns; x++) {
 			final Line l = new Line(new Vector3f(x * cellWidth, 0, 0), new Vector3f(x * cellWidth, aRows * cellHeight, 0));
 			l.setLineWidth(lineWidth);
 			final Geometry g = new Geometry("Grid-" + hashCode() + " (Col " + x + ")", l);
 			g.setMaterial(gridMat);
 			attachChild(g);
 		}
 		for (int y = 0; y <= aRows; y++) {
 			final Line l = new Line(new Vector3f(0, y * cellHeight, 0), new Vector3f(aColumns * cellWidth, y * cellHeight, 0));
 			l.setLineWidth(lineWidth);
 			final Geometry g = new Geometry("Grid-" + hashCode() + " (Row " + y + ")", l);
 			g.setMaterial(gridMat);
 			attachChild(g);
 		}
 	}
 
 	public GridLocation getCellAt(final float xPosition, final float yPosition, final Dimension dimension)
 	{
 		if (!dimension.sameAs(1, 1)) {
 			final Point centered = getPointAt(xPosition - aCellWidth * (dimension.getWidthFloat() - 1) / 2, yPosition
 					- aCellHeight * (dimension.getHeightFloat() - 1) / 2);
 			if (centered != null) {
				return new GridLocation(centered, dimension);
 			}
 		}
 		final Point point = getPointAt(xPosition, yPosition);
 		if (point == null) {
 			return null;
 		}
		return new GridLocation(point, dimension);
 	}
 
 	public GridLocation getCellAt(final Vector2f vector, final Dimension dimension)
 	{
 		return getCellAt(vector.x, vector.y, dimension);
 	}
 
 	public Rectangle getCellBounds(final GridLocation gridPoint)
 	{
 		final Vector3f origin = getCellOrigin(gridPoint);
 		return new Rectangle(origin.x, origin.y, gridPoint.getWidth() * aCellWidth, gridPoint.getHeight() * aCellHeight);
 	}
 
 	public Rectangle getCellBounds(final Point point)
 	{
 		return getCellBounds(new GridLocation(point));
 	}
 
 	public Vector2f getCellCenter(final GridLocation gridPoint)
 	{
 		return getCellBounds(gridPoint).getCenter2f();
 	}
 
 	public float getCellHeight()
 	{
 		return aCellHeight;
 	}
 
 	public Vector3f getCellOrigin(final GridLocation gridPoint)
 	{
 		return new Vector3f(gridPoint.getX() * aCellWidth, gridPoint.getY() * aCellHeight, 0);
 	}
 
 	public float getCellWidth()
 	{
 		return aCellWidth;
 	}
 
 	public int getColumns()
 	{
 		return aColumns;
 	}
 
 	public float getHalfDiagonal()
 	{
 		return FastMath.sqrt(FastMath.sqr(getTotalWidth()) + FastMath.sqr(getTotalHeight())) / 2f;
 	}
 
 	private Set<GridNode> getNodeList(final Point point)
 	{
 		if (!aCellContents.containsKey(point)) {
 			final Set<GridNode> l = new HashSet<GridNode>();
 			aCellContents.put(point, l);
 			return l;
 		}
 		return aCellContents.get(point);
 	}
 
 	private Point getPointAt(final float xPosition, final float yPosition)
 	{
 		if (xPosition < 0 || yPosition < 0 || xPosition > getTotalWidth() || yPosition > getTotalHeight()) {
 			return null;
 		}
 		int iX = (int) xPosition;
 		int iY = (int) yPosition;
 		iX = (int) ((iX - (iX % aCellWidth)) / aCellWidth);
 		iY = (int) ((iY - (iY % aCellHeight)) / aCellHeight);
 		return new Point(new Point(iX, iY));
 	}
 
 	public int getRows()
 	{
 		return aRows;
 	}
 
 	public float getTotalHeight()
 	{
 		return aRows * aCellHeight + aLineWidth;
 	}
 
 	public float getTotalWidth()
 	{
 		return aColumns * aCellWidth + aLineWidth;
 	}
 
 	public GridLocation handleHover(final Vector2f position, final Dimension dimension)
 	{
 		if (aHandleOver.equals(HoverMode.OFF)) {
 			return null;
 		}
 		final GridLocation newSquare = getCellAt(position, dimension);
 		if (aHoveredCell != null) {
 			if (newSquare != null) {
 				if (aHoveredCell.equivalentTo(newSquare)) {
 					return newSquare;
 				}
 			}
 			delNode(aHoveredCell);
 			aHoveredCell = null;
 		}
 		if (newSquare != null) {
 			aHoveredCell = new GridCellsNode(this, newSquare, aHoverColor);
 			aHoveredCell.getNewTransform().translate(0, 0, CellLayer.HOVER.getZOffset());
 			addNode(aHoveredCell);
 		}
 		if (aHoveredCell == null) {
 			return null;
 		}
 		return aHoveredCell.getLocation();
 	}
 
 	public GridNode registerNode(final GridNode node, final GridLocation location)
 	{
 		for (int x = 0; x < location.getWidth(); x++) {
 			for (int y = 0; y < location.getHeight(); y++) {
 				getNodeList(new Point(x, y).add(location.origin)).add(node);
 			}
 		}
 		addNode(node);
 		return node;
 	}
 
 	// TODO: Rewrite this with multi-cell support
 	/*
 	 * public void setColor(final Point location, final ColorRGBA color, final CellLayer layer) { final Vector3f origin =
 	 * getCellOrigin(location).add(0, 0, layer.getZOffset()); if (aCells.containsKey(origin)) { if (color == null) {
 	 * detachChild(aCells.get(origin)); aCells.remove(origin); return null; } else { aCells.get(origin).setColor(color); } }
 	 * else if (color != null) { final GridCell cellBG = new GridCell(location, origin, aCellWidth, aCellHeight, color);
 	 * attachChild(cellBG); aCells.put(origin, cellBG); } return aCells.get(origin); }
 	 */
 	public void setHandleHover(final HoverMode hoverMode)
 	{
 		aHandleOver = hoverMode;
 	}
 
 	public void setHoverColor(final ColorRGBA color)
 	{
 		aHoverColor = color;
 	}
 
 	public void unregisterNode(final GridNode node, final GridLocation location)
 	{
 		for (int x = 0; x < location.getWidth(); x++) {
 			for (int y = 0; y < location.getHeight(); y++) {
 				getNodeList(new Point(x, y).add(location.origin)).remove(node);
 			}
 		}
 		delNode(node);
 	}
 }
