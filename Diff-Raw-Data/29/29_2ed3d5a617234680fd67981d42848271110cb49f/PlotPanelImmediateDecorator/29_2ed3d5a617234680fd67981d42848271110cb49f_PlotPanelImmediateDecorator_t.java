 package edu.msoe.se2800.h4.jplot.plotpanel;
 
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.msoe.se2800.h4.jplot.JPoint;
 import edu.msoe.se2800.h4.jplot.grid.Grid;
 
 public class PlotPanelImmediateDecorator extends PlotPanelDecorator {
 	
 	private List<JPoint> oldList;
 
 	public PlotPanelImmediateDecorator(PlotPanelInterface plotPanel) {
 		super(plotPanel);
 		oldList = new ArrayList<JPoint>();
		getComponent().addMouseListener(new ImmediateListener());
 	}
 	
 	public void copyPoints() {
 		oldList.clear();
 		for (JPoint j : Grid.getInstance().getPathPoints()) {
 			oldList.add(j);
 		}
 		Grid.getInstance().getPathPoints().clear();
 		Grid.getInstance().redraw();
 	}
	
	public void replacePoint(JPoint point) {
		Grid.getInstance().getPathPoints().clear();
		Grid.getInstance().addPoint(point);
		Grid.getInstance().redraw();
	}
 
	public class ImmediateListener extends MouseAdapter {
		
		@Override
		public void mouseClicked(MouseEvent event) {
			JPoint p =  new JPoint(event.getX(), event.getY());
			replacePoint(translateToNearestPoint(p));
		}
		
	}
 }
