 package no.thunaes.petter.svg.app.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 
 import no.thunaes.petter.svg.app.Controller;
 
 
 public class SVGChartApp extends JFrame {
 
 	CenterPanel center;
 	SouthPanel south;
 	NorthPanel north;
 	
 	public SVGChartApp() {
 		Controller.init(this);
 		setTitle("SVGChartApp");
 		setLayout(new BorderLayout());
 		
 		add(north = new NorthPanel(), BorderLayout.NORTH);
 		add(center = new CenterPanel(), BorderLayout.CENTER);
 		add(south = new SouthPanel(), BorderLayout.SOUTH);
 		
		setSize(410,500);
 		setResizable(false);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		setVisible(true);
 	}
 
 }
