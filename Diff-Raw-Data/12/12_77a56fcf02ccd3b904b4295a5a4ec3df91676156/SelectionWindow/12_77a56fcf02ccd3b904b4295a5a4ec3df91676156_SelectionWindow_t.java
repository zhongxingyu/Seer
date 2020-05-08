 package gui.pa2;
 
 import java.awt.Button;
 import javax.swing.*;
 import java.awt.FlowLayout;
 import java.awt.GraphicsConfiguration;
 import java.awt.HeadlessException;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.*;
 
 import javax.swing.JFrame;
 
 @SuppressWarnings("serial")
 public class SelectionWindow extends JFrame {
 	
 	private Vector<Conditions> points;
 	private TreeMap<String, Vector<Conditions>> map;
 	private String day;
 
 	public SelectionWindow() throws HeadlessException {
 		super();
 		points = null;
 		map = new TreeMap<String, Vector<Conditions>>();
 		day = null;
 		
 		// Menu bar initialization
 		MenuBar bar = new MenuBar();
 		Menu m = new Menu("File");
 		MenuItem i = new MenuItem("Add source file...");
 		i.addActionListener(new Open(this));
 		m.add(i);
 		i = new MenuItem("Quit");
 		i.addActionListener(new Quit(this));
 		m.add(i);
 		bar.add(m);
 		m = new Menu("Help");
 		i = new MenuItem("About the Weather Viewer...");
 		i.addActionListener(new About(this));
 		m.add(i);
 		bar.add(m);
 		setMenuBar(bar);
 		
 		// Content
 		getContentPane().setLayout(new FlowLayout());
 		Button tempButton = new Button("Graph the interval");
 		tempButton.addActionListener(new GraphAction(this));
 		getContentPane().add(tempButton);
 		tempButton = new Button("Show various controls");
 		tempButton.addActionListener(new DialAction());
 		getContentPane().add(tempButton);
 		
 		// Pack it all in.
 		pack();
 	}
 	
 	public void updateMap()
 	{
 		for(Conditions current : points)
 		{
 			Calendar c = Calendar.getInstance();
 			c.setTime(current.getDay());
 			day = c.get(Calendar.DAY_OF_MONTH) + " of " + c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ", " + c.get(Calendar.YEAR);
 			if(map.containsKey(day))
 			{
 				map.get(day).add(current);
 			}
 			else
 			{
 				Vector<Conditions> vec = new Vector<Conditions>();
 				vec.add(current);
 				map.put(day, vec);
 			}
 		}
		setPoints(new Vector<Conditions>(map.get(day)));
 	}
 	
 	public void setPoints(Vector<Conditions> points)
 	{
 		this.points = points;
 	}
 	
 	public Vector<Conditions> getPoints()
 	{
 		return points;
 	}
 	
 	public String getDay()
 	{
 		return day;
 	}
 	
 	private static class About implements ActionListener
 	{
 		private JFrame frame;
 		
 		public About(JFrame ok)
 		{
 			frame = ok;
 		}
 		
 		public void actionPerformed(ActionEvent arg0) 
 		{
 			JOptionPane.showMessageDialog(frame, "Java XML-parsing weather station viewer, Version 0.0.1");
 		}
 	}
 	
 	private static class Open implements ActionListener
 	{
 		private SelectionWindow frame;
 		public Open(SelectionWindow frame)
 		{
 			this.frame = frame;
 		}
 		public void actionPerformed( ActionEvent event)
 		{
 			JFileChooser f = new JFileChooser();
 			Vector<Conditions> points = frame.getPoints();
 			f.setMultiSelectionEnabled(true);
 			int answer = f.showOpenDialog(frame);
 			if(answer == JFileChooser.APPROVE_OPTION)
 			{
 				File[] list = f.getSelectedFiles();
 				for(File current : list)
 				{
 					XMLParser p = new XMLParser(current.getAbsolutePath());
 					Vector<Conditions> ls = p.parse();
 					if(points == null)
 						points = new Vector<Conditions>();
 					if(ls != null)
 					points.addAll(ls);
 				}
 				frame.setPoints(points);
 				frame.updateMap();
 			}
 			// If points isn't set, let's at least init it.
 			else if(points == null)
 			{
 				Vector<Conditions> array = new Vector<Conditions>();
 				frame.setPoints(array);
 			}
 		}
 	}
 	
 	private static class Quit implements ActionListener
 	{
 		private JFrame frame;
 		public Quit(JFrame frame)
 		{
 			this.frame = frame;
 		}
 		public void actionPerformed( ActionEvent event)
 		{
 			frame.dispose();
 		}
 	}
 	
 	private static class GraphAction implements ActionListener
 	{
 		SelectionWindow frame;
 		public GraphAction(SelectionWindow frame)
 		{
 			this.frame = frame;
 		}
 	    // event handler method
 	    public void actionPerformed( ActionEvent event )
 	    {
 	    	if(frame.getPoints() == null || frame.getPoints().isEmpty())
 	    	{
 	    		JOptionPane.showMessageDialog(frame, new JLabel("Oops, it looks like you forgot to initialize data! Please go to the file menu and open one or more valid XML files."));
 	    	}
 	    	else
 	    	{
 		    	GraphWindow g = new GraphWindow(frame.getPoints(), frame.getDay());
 		    	g.setSize(800,600);
 		    	g.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		    	g.setVisible(true);
 	    	}
 	    }
 	}
 	private static class DialAction implements ActionListener
 	{
 		public void actionPerformed( ActionEvent event)
 		{
 			DialWindow d = new DialWindow();
 			d.setSize(800,600);
 			d.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 			d.setVisible(true);
 		}
 	}
 
 	public SelectionWindow(GraphicsConfiguration gc) {
 		super(gc);
 	}
 
 	public SelectionWindow(String title) throws HeadlessException {
 		super(title);
 	}
 
 	public SelectionWindow(String title, GraphicsConfiguration gc) {
 		super(title, gc);
 	}
 
 }
