 package gui.pa2;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.*;
 
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class GraphWindow extends JFrame {
 	
 	/**
 	 * Creates a new daily graph window from the given set of conditions and day.
 	 * The day should be passed in as a string in the format it should appear on
 	 * the graphs and title of the window.
 	 * @param points a vector of gui.pa2.Conditions describing the weather over the interval.
 	 * @param today the date, as a string formatted as the date should appear on the graphs and title bar.
 	 */
	public GraphWindow(Vector<Conditions> in_pts, String today)
 	{
 		// Basic constructor necessity
 		super("Graph for " + today);
		// Prevent concurrency issues
		Vector<Conditions> points = new Vector<Conditions> (in_pts);
 		// We're using a hand-coded GroupLayout for this window.
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		// We want gaps inserted between the edges of the container and components.
 		layout.setAutoCreateContainerGaps(true);
 		// The points should be sorted by X-value. This tree gives us O(lg(n)) sort time on inserts and easy access to the elements.
 		TreeSet<ConditionPoint> map = null;
 		// Calculate the different values that we need. These are used in calculating proportional averages.
 		DataCalculator d = new DataCalculator(points);
 		TreeMap<String, Object> data = d.computeData();
 		// Trying to do some basic sanity testing; caller should NOT pass in a null vector.
 		if(points != null) 
 		{
 			map = new TreeSet<ConditionPoint>();
 			// Since we used the date class for the Conditions, we need a calendar to properly access its elements.
 			Calendar c = Calendar.getInstance();
 
 			// For each Conditions called current in the collection points
 			for(Conditions current : points)
 			{
 				if(current.getRain() != Conditions.INVALID_RAIN)
 				{
 					// Use the time of day for this particular point as our calendar point.
 					c.setTime(current.getDay());
 					/* Because the GraphCanvas class uses proportional distancing,
 					 * we will want to set x as the proportion of its time in the day to the amount of time in a day.
 					 * The entire thing uses a fixed-point system; I don't like to use floats or doubles unless it's
 					 * a scientific program that can be +/-.0001, since FPUs are terribly imprecise. 
 					 */
 					int x = c.get(Calendar.HOUR_OF_DAY) * 1000 / 23;
 					x += c.get(Calendar.MINUTE) / 2;
 					/* The y value should also be a fixed-point proportion. 15 is used as a baseline right now, but in the
 					 * future we may adjust this so it is proportional to the difference in the min and max values for
 					 * the data set.
 					 */
 					float max = (Float)data.get(DataCalculator.MAX_RAIN);
 					float min = (Float)data.get(DataCalculator.MIN_RAIN);
 					int y = (int) ((current.getRain() - min) / (max - min) * 500);
 					// Use the above proportions to create a new data point.
 					ConditionPoint p = new ConditionPoint(x, y, current);
 					// Add the point to our sorted map.
 					map.add(p);
 				}
 			}
 		}
 		
 		// Create a graph canvas from the formatted data.
 		GraphCanvas g1 = new GraphCanvas(map, "Rainfall for " + today, "Inches");
 		
 		// again, a minor sanity check.
 		if(map != null) map = new TreeSet<ConditionPoint>();
 		if(points != null)
 		{
 			Calendar c = Calendar.getInstance();
 
 			for(Conditions current : points)
 			{
 				// See above about proportions and using fixed vs. floating point.
 				c.setTime(current.getDay());
 				int x = c.get(Calendar.HOUR_OF_DAY) * 1000 / 23;
 				x += c.get(Calendar.MINUTE) / 2;
 				float max = (Float)data.get(DataCalculator.MAX_PRESSURE);
 				float min = (Float)data.get(DataCalculator.MIN_PRESSURE);
 				// Barometric pressure rarely, if ever, gets above 40 in Hg, and is rarely less than 25 in Hg.
 				int y = (int)((current.getPressure() - min) / (max - min) * 500);
 				ConditionPoint p = new ConditionPoint(x, y, current);
 				map.add(p);
 			}
 		}
 		// Separate variable, because we need to graph these pieces in the group window separately.
 		GraphCanvas g2 = new GraphCanvas(map, "Barometric Pressure for " + today, "Inches Hg");
 		
 		map = new TreeSet<ConditionPoint>();
 		if(points != null)
 		{
 			Calendar c = Calendar.getInstance();
 
 			for(Conditions current : points)
 			{
 				// See above about proportions and using fixed vs. floating point.
 				c.setTime(current.getDay());
 				int x = c.get(Calendar.HOUR_OF_DAY) * 1000 / 23;
 				x += c.get(Calendar.MINUTE) / 2;
 				float max = (Float)data.get(DataCalculator.MAX_TEMP);
 				float min = (Float)data.get(DataCalculator.MIN_TEMP);
 				int y = (int)((current.getTemperature() - min) / (max - min) * 500);
 				ConditionPoint p = new ConditionPoint(x, y, current);
 				map.add(p);
 			}
 		}
 		GraphCanvas g3 = new GraphCanvas(map, "Temperature for " + today, "Degrees F");
 		
 		// A special informational pane.
 		InfoPane pane = new InfoPane();
 		pane.setSize(new java.awt.Dimension(100,100));
 		g3.addMouseListener(new PointClick(pane));
 		g2.addMouseListener(new PointClick(pane));
 		g1.addMouseListener(new PointClick(pane));
 		/*
 		g1.addMouseMotionListener(new PointFollow(pane));
 		g2.addMouseMotionListener(new PointFollow(pane));
 		g3.addMouseMotionListener(new PointFollow(pane));
 		*/
 		
 		// A series of JLabels will contain the relevant data.
 		JLabel[] dataValues = new JLabel[data.size()];
 		int i = 0;
 		// For each pair of entries in the set of data we got returned...
 		for( Map.Entry<String, Object> current : data.entrySet())
 		{
 			// Sanity check: Avoid nullPointerExceptions
 			if(current.getKey() == null)
 			{
 				System.err.println("Oh, snap! Found a null key in the map!");
 			}
 			else if(current.getValue() == null)
 			{
 				System.err.println("Egads! A null value in the map!");
 			}
 			else
 			{
 				// If it's all sane, concatenate the strings for output.
 				dataValues[i] = new JLabel(current.getKey().trim() + ": " + current.getValue());
 			}    
 			++i;
 		}
 		// Here there be dragons. This is where I finally lay everything out. You've been warned.
 		GroupLayout.SequentialGroup panel = layout.createSequentialGroup(); // Collection for the JLabels' vertical layout
 		GroupLayout.ParallelGroup col = layout.createParallelGroup(); // Collection for the JLabels' horizontal layout
 		
 		// For each JLabel in the dataValues array...
 		for( JLabel label : dataValues)
 		{
 			// Add the label to each half of the layout.
 			panel.addComponent(label);
 			panel.addGap(2,5,Short.MAX_VALUE);
 			col.addComponent(label);
 		}
 		// Collection for the horizontal direction of the window.
 		GroupLayout.SequentialGroup columns = layout.createSequentialGroup();
 		// Add the graphs, one after the other.
 		columns.addGroup(layout.createParallelGroup().addComponent(g1).addComponent(g2).addComponent(g3));
 		columns.addGap(5);
 		// Add all those little labels.
 		columns.addGroup(col);
 		columns.addGap(5);
 		// Add in the info pane.
 		columns.addComponent(pane);
 		// Make columns the layout as things go from left to right.
 		layout.setHorizontalGroup(columns);
 		// Get a proper layout for the individual rows.
 		GroupLayout.SequentialGroup rows = layout.createSequentialGroup();
 		
 		/* Sorry about this; it's the easiest way to simply add all the stuff together. I know it resembles an Eldritch 
 		 * horror from beyond space-time. I'll try and get a document together that explains why on earth this is
 		 * necessary, but it does work.
 		 */
 		rows.addGroup(layout.createParallelGroup(). // note the . at the end of the line; this is a really long chain.
 				addGroup(
 						layout.createSequentialGroup().
 						addComponent(g1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(5).
 						addComponent(g2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addGap(5).
 						addComponent(g3, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).
 				addGroup(panel).
 				addComponent(pane));
 		// Use this layout for the vertical direction.
 		layout.setVerticalGroup(rows);
 		
 		// Pack it all in.
 		pack();
 		
 	}
 	
 	private class PointFollow implements MouseMotionListener
 	{
 		public PointFollow(InfoPane pane)
 		{
 	//		target = pane;
 		}
 		@Override
 		public void mouseDragged(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 		public void mouseMoved(MouseEvent arg0) 
 		{
 //			int x = arg0.getX();
 //			int y = arg0.getY();
 //			GraphCanvas c = (GraphCanvas)arg0.getSource();
 //			c.setPoint(new java.awt.Point(x,y));
 //			c.repaint();
 		}
 	}
 	
 	private class PointClick implements MouseListener
 	{
 		private InfoPane target;
 		
 		public PointClick(InfoPane pane)
 		{
 			target = pane;
 		}
 
 		public void mouseClicked(MouseEvent arg0) 
 		{
 			int x = arg0.getX();
 			int y = arg0.getY();
 			Conditions dummy = new Conditions();
 			ConditionPoint pt = new ConditionPoint(x, y, dummy);
 			GraphCanvas canvas = (GraphCanvas)arg0.getSource();
 			if(canvas.getRelative() != null)
 			{
 			    Iterator<ConditionPoint> it = canvas.getRelative().iterator();
 			    boolean cont = true;
 			    ConditionPoint holder = null;
 			    while(it.hasNext() && cont)
 			    {
 			    	holder = it.next();
 			    	if(Math.abs( holder.compareTo(pt) ) < 5 ) System.out.println(Math.abs( holder.compareTo(pt)));
 			    	if( Math.abs( holder.compareTo(pt) )  < 5)
 			    	{
 			    		cont = false;
 			    	}
 			    }
 			    
 			    if(cont == true) holder = null;
 			    
 			    target.setPoint(holder);
 			    target.showInfo();
 			}
 		}
 
 		public void mouseEntered(MouseEvent arg0) {
 //			int x = arg0.getX();
 //			int y = arg0.getY();
 //			GraphCanvas c = (GraphCanvas)arg0.getSource();
 //			c.setPoint(new java.awt.Point(x,y));
 //			c.repaint();
 		}
 
 		public void mouseExited(MouseEvent arg0) 
 		{
 			GraphCanvas c = (GraphCanvas)arg0.getSource();
 			c.setPoint(new java.awt.Point(0,0));
 			c.repaint();
 		}
 
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void mouseReleased(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 			
 		}
 		
 	}
 
 }
