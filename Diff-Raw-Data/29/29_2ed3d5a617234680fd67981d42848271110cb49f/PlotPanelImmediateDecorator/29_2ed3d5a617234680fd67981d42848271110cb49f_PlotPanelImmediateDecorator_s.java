 package edu.msoe.se2800.h4.jplot.plotpanel;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.msoe.se2800.h4.jplot.JPoint;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 
 public class PlotPanelImmediateDecorator extends PlotPanelDecorator {
 	
 	private List<JPoint> oldList;
 
 	public PlotPanelImmediateDecorator(PlotPanelInterface plotPanel) {
 		super(plotPanel);
 		oldList = new ArrayList<JPoint>();
		
 	}
 	
 	public void copyPoints() {
 		oldList.clear();
 		for (JPoint j : Grid.getInstance().getPathPoints()) {
 			oldList.add(j);
 		}
 		Grid.getInstance().getPathPoints().clear();
 		Grid.getInstance().redraw();
 	}
 
 }
