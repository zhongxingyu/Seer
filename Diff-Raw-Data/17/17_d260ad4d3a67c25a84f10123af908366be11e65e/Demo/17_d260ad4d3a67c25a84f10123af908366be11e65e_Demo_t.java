 package com.china.domain;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Demo {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		// TODO Auto-generated method stub
 		System.out.println("Hello,world!");
 		new TOrientateFrame("Calculate Distance/Bearing");
 	}
 }
 
 class TOrientateFrame extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 4398852471661924273L;
 	JLabel point1, point2, distence, distence_value, bearing, bearing_value;
 	TextField latitude1, latitude2, longitude1, longitude2;
 	JButton button1;
 	private JMenuBar	mb;
 	private JMenu[] Menus = {
 			new JMenu("File"),
 			new JMenu("Option"),
 			new JMenu("Help")
 	};
 	private JMenuItem[] Items = {
 			new JMenuItem("open"),
 			new JMenuItem("exit"),
 			new JMenuItem("setting"),
 			new JMenuItem("about"),
 	};
 	private JTabbedPane Tabs = new JTabbedPane();;	
 	public class Point {
 		double lat;
 		double lon;
 
 		Point(Double lat, Double lon) {
 			this.lat = lat;
 			this.lon = lon;
 		}
 
 		public double distance(Point p) {
 			double lat1 = Math.toRadians(lat);
 			double lon1 = Math.toRadians(lon);
 			double lat2 = Math.toRadians(p.lat);
 			double lon2 = Math.toRadians(p.lon);
 
 			double tmp = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
 					* Math.cos(lat2) * Math.cos(lon2 - lon1);
 			double dis = RADIUS * (Math.acos(tmp));
 			return dis;
 		}
 
 		public double bearing(Point p) {
 			double lat1 = Math.toRadians(lat);
 			double lon1 = Math.toRadians(lon);
 			double lat2 = Math.toRadians(p.lat);
 			double lon2 = Math.toRadians(p.lon);
 
 			double y = Math.sin(lon2 - lon1) * Math.cos(lat2);
 			double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
 					* Math.cos(lat2) * Math.cos(lon2 - lon1);
 			double bear = Math.toDegrees(Math.atan2(y, x));
 			return bear;
 		}
 
 		public Point destination(Double dis, Double bear) {
 			double lat1 = Math.toRadians(lat);
 			double lon1 = Math.toRadians(lon);
 			double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dis / RADIUS))
 					+ Math.cos(lat1) * Math.sin(dis / RADIUS) * Math.cos(bear);
 			double lon2 = lon1
 					+ Math.atan2(Math.sin(bear) * Math.sin(dis / RADIUS)
 							* Math.cos(lat1), Math.cos(dis / RADIUS)
 							- Math.sin(lat1) * Math.sin(lat2));
 			return new Point(Math.toDegrees(lat2),Math.toDegrees(lon2));
 		}
 	};
 
 	private ButtonListener Bl = new ButtonListener();
 
 	private double RADIUS = 6371;// radius of earth 6,371km
 
 	/**
 	 * @param name
 	 * @throws Exception
 	 */
 	public TOrientateFrame(String name) throws Exception {
 		super(name);
 		/* Init Menubar */
 		Menus[0].add(Items[0]);
 		Menus[0].add(Items[1]);
 
 		Menus[1].add(Items[2]);
 		Menus[2].add(Items[3]);
 		
 		mb = new JMenuBar();
 		mb.add(Menus[0]);
 		mb.add(Menus[1]);
 		mb.add(Menus[2]);
 		setJMenuBar(mb);
 		
 		/* Tab Calibrate */
 		JPanel Calibrate = new JPanel(); 
 		Object[] columnName = {"","γ","","","ƫ"};
 		Object[][] data = {
 				{"ί", new Double(91.3651), new Double (29.6719),0,0}, 
 				{"ί", new Double(91.3667 ), new Double (29.6693),0,0},
 				{"", new Double(91.3760), new Double (29.6774),0,0},
 				{"", new Double(91.3776 ), new Double (29.6746),0,0},
 				{"xشѧ", new Double(91.1454), new Double (29.6490),0,0},		
 				{"xشѧ", new Double(91.1470), new Double (29.6463),0,0},

 			};
 		JTable table = new JTable(data, columnName);
 	//	table.setPreferredScrollableViewportSize(new Dimension(100, 30));
		JScrollPane scroll = new JScrollPane(table);
 		JPanel north = new JPanel();
		north.add(scroll);
 		JPanel center = new JPanel();
 		center.add(new JLabel("ƽƫǣ"));
 		center.add(new JLabel("xxxx  "));
 		JPanel south = new JPanel();
 		south.add(new JButton(""));
 		south.add(new JButton("һ"));
 	
 		Calibrate.setLayout(new BorderLayout());
 		Calibrate.add(north,BorderLayout.NORTH);
 		Calibrate.add(center,BorderLayout.CENTER);
 		Calibrate.add(south,BorderLayout.SOUTH);
 		Calibrate.setOpaque(true);
 		Tabs.add("У׼",Calibrate);
 		
 		/* Tab Calibrate */
 		JPanel[] p = new JPanel[5];
 		GridBagConstraints gridbagcn = new GridBagConstraints();
 		gridbagcn.gridheight = 1;
 		gridbagcn.gridwidth = 1;
 		gridbagcn.gridx = 0;
 		gridbagcn.gridy = 0;
 		gridbagcn.weightx = 0;
 		gridbagcn.weightx = 0;
 		gridbagcn.anchor = GridBagConstraints.WEST;
 		point1 = new JLabel("point1:");
 
 		latitude1 = new TextField("29.40158 ");
 		longitude1 = new TextField("91.22004 ");
 		
 		p[0] = new JPanel(new GridBagLayout());
 		p[0].add(point1,gridbagcn);
 		gridbagcn.gridx+=1;
 		p[0].add(latitude1,gridbagcn);
 		gridbagcn.gridx++;
 		p[0].add(longitude1,gridbagcn);
 		
 		point2 = new JLabel("point2:");
 		latitude2 = new TextField("29.40313 ");
 		longitude2 = new TextField("91.21908 ");
 		p[1] = new JPanel(new FlowLayout());
 		p[1].add(point2);
 		p[1].add(latitude2);
 		p[1].add(longitude2);
 		
 		distence = new JLabel("distence");
 		distence_value = new JLabel("-");
 		p[2] = new JPanel(new FlowLayout());
 		p[2].add(distence);
 		p[2].add(distence_value);
 		bearing = new JLabel("bearing:");
 		bearing_value = new JLabel("-");
 		p[3] = new JPanel(new FlowLayout());
 		p[3].add(bearing);
 		p[3].add(bearing_value);
 		
 		button1 = new JButton("Calculate");
 		p[4] = new JPanel(new FlowLayout());
 		p[4].add(button1);
 		
 		JPanel Calculate = new JPanel();
 		Calculate.setLayout(new GridLayout(5, 1));
 		for(int i=0; i < 5;i++)
 			Calculate.add(p[i]);
 		Tabs.add("",Calculate);
 
 		this.add(Tabs);	
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	//	this.setSize(420, 380);
 		this.setVisible(true);
		this.pack();
 		button1.addActionListener(Bl);
 		// TimeUnit.SECONDS.sleep(10);
 		// label.setText("Hello,kit!");
 	}
 
 	class ButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 
 			Double lat = Double.parseDouble(latitude1.getText());
 			Double lon = Double.parseDouble(longitude1.getText());
 			Point p1 = new Point(lat, lon);
 			lat = Double.parseDouble(latitude2.getText());
 			lon = Double.parseDouble(longitude2.getText());
 			Point p2 = new Point(lat, lon);
 			Point p3 = p1.destination(p1.distance(p2),p1.bearing(p2));
 			distence_value.setText(Double.toString(p1.distance(p2)) + " Km");
 			bearing_value.setText(Double.toString(p3.lat) + " ---"+ Double.toString(p3.lon));
 		}
 	}
 }
