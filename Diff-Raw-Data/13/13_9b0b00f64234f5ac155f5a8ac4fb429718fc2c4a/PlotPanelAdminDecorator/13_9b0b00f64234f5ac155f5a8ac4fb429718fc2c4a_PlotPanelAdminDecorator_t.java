 package edu.msoe.se2800.h4.jplot.plotPanel;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 import lejos.robotics.navigation.Waypoint;
 import edu.msoe.se2800.h4.jplot.Constants;
 import edu.msoe.se2800.h4.jplot.JPlotController;
 
 public class PlotPanelAdminDecorator extends PlotPanelDecorator {
 	
 	private PopUpDemo popUp;
 
 	public PlotPanelAdminDecorator(PlotPanelInterface plotPanel) {
 		super(plotPanel);
 		getComponent().addMouseListener(new PlotMouseAdapter());
 	}
 	
 	/** Listeners and Adapters **/
 	private class PlotMouseAdapter extends MouseAdapter {
 		
 		@Override
 		public void mouseClicked(MouseEvent event) {
 			if (event.getButton() == MouseEvent.BUTTON1) {
				Waypoint point = plotPanel.getInterceptedPoint(new Waypoint(event.getX(), event.getY()));
 				boolean found = false;
				if (point != null) {
					for (Waypoint p : JPlotController.getInstance().getPath()) {
						if (p.x == point.x && p.y == point.y) {
							found = true;
							JPlotController.getInstance().setHighlightedPoint(JPlotController.getInstance().getPath().indexOf(p));
						}
 					}
 				}
 				if (found == false) {
 					JPlotController.getInstance().setHighlightedPoint(-5);
 				}
 				JPlotController.getInstance().getGrid().redraw();
 			} else if (event.getButton() == MouseEvent.BUTTON3) {
 				doPop(event);
 			}
 		}
 		@Override
 		public void mousePressed(MouseEvent event) {
 			Waypoint p = new Waypoint(event.getX(), event.getY());
 			setActivePoint(getInterceptedPoint(p));
 			setActivePointIndexHolder(JPlotController.getInstance().getPath().indexOf(getActivePoint()));
 			Constants.DRAGGING_INDEX = getActivePointIndexHolder();
 		}
 		@Override
 		public void mouseReleased(MouseEvent event) {
 			setActivePoint(null);
 			setActivePointIndexHolder(-5);
 			Constants.DRAGGING_INDEX = -5;
 			JPlotController.getInstance().getGrid().redraw();
 			Constants.HOVER_INDEX = -5;
 		}
 	}
 	
 	/** copied this from the interwebs **/
 	private void doPop(MouseEvent e){
 		popUp = new PopUpDemo(e);
 		popUp.show(e.getComponent(), e.getX(), e.getY());
 	}
 	private class PopUpDemo extends JPopupMenu {
 	    /** Generated serialVersionUID */
 		private static final long serialVersionUID = -926882311315622109L;
 		JMenuItem add;
 	    JMenuItem delete;
 	    Waypoint clickedPoint;
 	    MenuListener menuListener;
 	    public PopUpDemo(MouseEvent e){
 	    	menuListener = new MenuListener();
 	    	clickedPoint = new Waypoint(e.getX(), e.getY());
 	        add = new JMenuItem("Add point");
 	        add.setActionCommand("add_point");
 	        add.addActionListener(menuListener);
 	        delete = new JMenuItem("Delete point");
 	        delete.setActionCommand("delete_point");
 	        delete.addActionListener(menuListener);
 	        add(add);
 	        add(delete);
 	    }
 	    private class MenuListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (e.getActionCommand().equalsIgnoreCase("add_point")) {
 					JPlotController.getInstance().addPoint(translateToNearestPoint(clickedPoint));
 				} else if (e.getActionCommand().equalsIgnoreCase("delete_point")) {
 					Waypoint p = getInterceptedPoint(clickedPoint);
 					if (p != null) {
 						JPlotController.getInstance().removePoint(JPlotController.getInstance().getPath().indexOf(p));
 					}
 				}
 			}
 	    }
 	}
 
 }
