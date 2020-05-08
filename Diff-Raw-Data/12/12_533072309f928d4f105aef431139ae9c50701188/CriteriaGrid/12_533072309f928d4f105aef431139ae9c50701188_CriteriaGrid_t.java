 package org.iucn.sis.shared.api.structures;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.shared.api.criteriacalculator.CriteriaSet;
 import org.iucn.sis.shared.api.criteriacalculator.ExpertResult.ResultCategory;
 import org.iucn.sis.shared.api.data.CanParseCriteriaString;
 
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.solertium.util.portable.PortableReplacer;
 
 public abstract class CriteriaGrid extends CanParseCriteriaString {
 	
 	protected final Map<String, GridCoordinates> classificationToGrid;
 
 	protected Grid gridA;
 	protected Grid gridB;
 	protected Grid gridC;
 	protected Grid gridD;
 	protected Grid gridE;
 
 	protected VerticalPanel gridPanel;
 
 	public CriteriaGrid() {
 		classificationToGrid = new HashMap<String, GridCoordinates>();
 		
 		buildGrid();
 
 		gridPanel = new VerticalPanel();
 		gridPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		gridPanel.setSpacing(4);
 
 		HorizontalPanel hp1 = new HorizontalPanel();
 		hp1.setSpacing(4);
 		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		hp1.add(new HTML("Criterion A:"));
 		hp1.add(gridA);
 		hp1.setStyleName("summary-border");
 		gridPanel.add(hp1);
 
 		hp1 = new HorizontalPanel();
 		hp1.setSpacing(4);
 		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		hp1.add(new HTML("Criterion B: "));
 		hp1.add(gridB);
 		hp1.addStyleName("summary-border");
 		gridPanel.add(hp1);
 		hp1 = new HorizontalPanel();
 		hp1.setSpacing(4);
 		hp1.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 		hp1.add(new HTML("Criterion C: "));
 		hp1.add(gridC);
 		hp1.addStyleName("summary-border");
 		hp1.setSpacing(4);
 		hp1.add(new HTML("Criterion D: "));
 		hp1.add(gridD);
 		hp1.addStyleName("summary-border");
 		hp1.add(new HTML("Criterion E: "));
 		hp1.add(gridE);
 		gridPanel.add(hp1);
 	}
 
 	/**
 	 * This function should instantiate the grid
 	 */
 	public abstract void buildGrid();
 
 	protected Grid[] getGrids() {
 		return new Grid[] { gridA, gridB, gridC, gridD, gridE };
 	}
 	
 	/**
 	 * Clears all grid info
 	 */
 	public void clearWidgets() {
 		for (Grid grid : getGrids()) {
 			for (int row = 0; row < grid.getRowCount(); row++) {
 				for (int col = 0; col < grid.getColumnCount(); col++) {
 					if (grid.getWidget(row, col) != null)
 						((CheckBox) grid.getWidget(row, col)).setValue(false);
 				}
 			}
 		}
 	}
 
 	public String createCriteriaString() {
 		List<String> criteria = new ArrayList<String>();
 		for (Grid grid : getGrids()) {
 			for (int row = 0; row < grid.getRowCount(); row++) {
 				for (int column = 0; column < grid.getColumnCount(); column++) {
 					if (grid.getWidget(row, column) != null) {
 						CheckBox box = (CheckBox) grid.getWidget(row, column);
 						if (box.getValue())
 							criteria.add(box.getName().trim());
 					}
 				}
 			}
 		}
 		
 		CriteriaSet criteriaSet = new CriteriaSet(ResultCategory.DD, criteria);
 
 		return criteriaSet.toString();
 	}
 
 	protected CheckBox createWidget(String text, int i, int j) {
 		return createWidget(text, PortableReplacer.stripNonalphanumeric(text), i, j);
 	}
 	
 	protected CheckBox createWidget(String text, String value, int i, int j) {
 		classificationToGrid.put(value, new GridCoordinates(text, i, j));
 
 		CheckBox box = new CheckBox("  " + text);
 		box.setName(value);
 		
 		return box;
 	}
 
 	private GridCoordinates getCoordinates(String criterion) {
 		String key = PortableReplacer.stripNonalphanumeric(criterion);
 		return classificationToGrid.get(key);
 	}
 	
 	@Override
 	public void foundCriterion(String criterion) {
 		GridCoordinates coordinates = getCoordinates(criterion);
 		if (coordinates == null)
 			return;
 		
 		Grid grid = null;
 		switch (criterion.charAt(0)) {
 			case 'A':
 				grid = gridA; break;
 			case 'B':
 				grid = gridB; break;
 			case 'C':
 				grid = gridC; break;
 			case 'D':
 				grid = gridD; break;
 			case 'E':
 				grid = gridE; break;
 			default:
 				grid = null;
 		}
 		
 		Widget widget;
 		if (grid != null && (widget = grid.getWidget(coordinates.getRow(), coordinates.getColumn())) != null) {
 			CheckBox box = (CheckBox) widget;
 			box.setValue(true);
 		}
 	}
 
 	public VerticalPanel getWidget() {
 		return gridPanel;
 	}
 
 	public boolean isChecked(String key) {
 		String grid = key.substring(0, 1);
		
 		GridCoordinates coords = getCoordinates(key);
		if (coords == null)
			return false;
 
 		int row = coords.getRow();
 		int col = coords.getColumn();
 
 		if (grid.equalsIgnoreCase("A")) {
 			return ((CheckBox) gridA.getWidget(row, col)).getValue();
 		} else if (grid.equalsIgnoreCase("B")) {
 			return ((CheckBox) gridB.getWidget(row, col)).getValue();
 		} else if (grid.equalsIgnoreCase("C")) {
 			return ((CheckBox) gridC.getWidget(row, col)).getValue();
 		} else if (grid.equalsIgnoreCase("D")) {
 			return ((CheckBox) gridD.getWidget(row, col)).getValue();
 		} else if (grid.equalsIgnoreCase("E")) {
 			return ((CheckBox) gridE.getWidget(row, col)).getValue();
 		} else {
 			return false;
 		}
 	}
 
 	public abstract boolean isCriteriaValid(String criteriaString, String category);
 
 	protected void setEnabled(boolean isManual) {
 		for (Grid grid : getGrids()) {
 			for (int row = 0; row < grid.getRowCount(); row++) {
 				for (int col = 0; col < grid.getColumnCount(); col++) {
 					if (grid.getWidget(row, col) != null)
 						((CheckBox) grid.getWidget(row, col)).setEnabled(isManual);
 				}
 			}
 		}
 	}
 
 	public void setVisible(boolean visible) {
 		gridPanel.setVisible(visible);
 	}
 
 	/**
 	 * This is called whenever the resulting criteria String is changed.
 	 * 
 	 * @param result
 	 */
 	protected abstract void updateCriteriaString(String result);
 	
 	protected static class GridCoordinates {
 		
 		private final String name;
 		private final int row, column;
 		
 		public GridCoordinates(String name, int row, int column) {
 			this.name = name;
 			this.row = row;
 			this.column = column;
 		}
 		
 		public int getColumn() {
 			return column;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public int getRow() {
 			return row;
 		}
 		
 	}
 }
