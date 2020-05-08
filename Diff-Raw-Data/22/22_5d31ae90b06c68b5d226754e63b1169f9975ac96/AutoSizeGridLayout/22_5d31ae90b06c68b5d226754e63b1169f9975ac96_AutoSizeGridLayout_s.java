 package com.siniatech.siniautils.swing.layout;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JComponent;
 import javax.swing.border.Border;
 
 public class AutoSizeGridLayout implements LayoutManager {
 
 	private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
 
 	private int maxGridWidth;
 	private int hGap;
 	private int vGap;
 
 	public AutoSizeGridLayout(int maxGridWidth) {
 		this(maxGridWidth, 0, 0);
 	}
 
 	public AutoSizeGridLayout(int maxGridWidth, int hGap, int vGap) {
 		this.maxGridWidth = maxGridWidth;
 		this.hGap = hGap;
 		this.vGap = vGap;
 	}
 
 	@Override
 	public void addLayoutComponent(String name, Component comp) {
 	}
 
 	@Override
 	public void removeLayoutComponent(Component comp) {
 	}
 
 	@Override
 	public void layoutContainer(Container parent) {
 		GridManager gridManager = new GridManager(maxGridWidth, parent);
 		Component[] components = parent.getComponents();
 		for (int i = 0; i < components.length; i++) {
 			Rectangle bounds = gridManager.getBounds(i);
 			components[i].setBounds(bounds);
 		}
 	}
 
 	@Override
 	public Dimension minimumLayoutSize(Container parent) {
 		return preferredLayoutSize(parent);
 	}
 
 	@Override
 	public Dimension preferredLayoutSize(Container parent) {
 		GridManager gridManager = new GridManager(maxGridWidth, parent);
 		return new Dimension(gridManager.pixelWidth(), gridManager.pixelHeight());
 	}
 
 	private class GridManager {
 
 		private int gridWidth;
 		private int gridHeight;
 		private int[] columnWidths;
 		private int[] rowHeights;
 		private Insets borderInsets;
 
 		public GridManager(int maxGridWidth, Container parent) {
 			Component[] components = parent.getComponents();
 			borderInsets = getBorderInsets(parent);
 			gridWidth = determineGridWidth(maxGridWidth, components.length);
 			gridHeight = determineGridHeight(components.length);
 			columnWidths = new int[gridWidth];
 			rowHeights = new int[gridHeight];
 			for (int i = 0; i < components.length; i++) {
 				Point gridPosition = determineGridPosition(i);
 				Dimension preferredSize = components[i].getPreferredSize();
 				if (preferredSize.height > rowHeights[gridPosition.y]) {
 					rowHeights[gridPosition.y] = preferredSize.height;
 				}
 				if (preferredSize.width > columnWidths[gridPosition.x]) {
 					columnWidths[gridPosition.x] = preferredSize.width;
 				}
 			}
 		}
 
 		public Rectangle getBounds(int componentNo) {
 			Point gridPosition = determineGridPosition(componentNo);
 			return new Rectangle(getX(gridPosition), getY(gridPosition),
 					getWidth(gridPosition), getHeight(gridPosition));
 		}
 
 		private int getWidth(Point gridPosition) {
 			return columnWidths[gridPosition.x];
 		}
 
 		private int getHeight(Point gridPosition) {
 			return rowHeights[gridPosition.y];
 		}
 
 		private int getX(Point gridPosition) {
			return borderInsets.left + sumValues(columnWidths, hGap, gridPosition.x)
					+ hGap;
 		}
 
 		private int getY(Point gridPosition) {
			return borderInsets.top + sumValues(rowHeights, vGap, gridPosition.y) + vGap;
 		}
 
 		public int pixelWidth() {
 			return borderInsets.left + borderInsets.right
					+ sumValues(columnWidths, hGap, columnWidths.length);
 		}
 
 		public int pixelHeight() {
 			return borderInsets.top + borderInsets.bottom
					+ sumValues(rowHeights, hGap, rowHeights.length);
 		}
 
 		private int sumValues(int[] array, int gap, int upToExcl) {
 			int sum = 0;
 			for (int i = 0; i < upToExcl; i++) {
 				sum += array[i];
 			}
 			if (array.length > 0 && upToExcl > 0) {
				sum += (upToExcl - 1) * gap;
 			}
 			return sum;
 		}
 
 		public Point determineGridPosition(int componentNo) {
 			int x = componentNo == 0 ? 0 : componentNo % gridWidth;
 			int y = componentNo == 0 ? 0 : componentNo / gridWidth;
 			return new Point(x, y);
 		}
 
 		private int determineGridHeight(int noOfComponents) {
 			if (noOfComponents == 0) {
 				return 0;
 			}
 			int noOfFilledRows = noOfComponents / gridWidth;
 			boolean hasUnfilledRow = noOfComponents % gridWidth > 0;
 			return hasUnfilledRow ? noOfFilledRows + 1 : noOfFilledRows;
 		}
 
 		private int determineGridWidth(int maxGridWidth, int noOfComponents) {
 			return noOfComponents > maxGridWidth ? maxGridWidth : noOfComponents;
 		}
 
 		private Insets getBorderInsets(Component component) {
 			if (component instanceof JComponent) {
 				Border border = ((JComponent) component).getBorder();
 				if (border != null) {
 					return border.getBorderInsets(component);
 				}
 			}
 			return EMPTY_INSETS;
 		}
 	}
 
 }
