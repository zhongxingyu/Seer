 package gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JTable;
 
 import application.Controller;
 import application.Transport;
 
 public class Display extends JFrame implements KeyListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1589687406526375364L;
 	
 	private JTable			table;
 	private Controller 		controller;
 	private TimeTableModel 	timeTableModel;
 	private RotatedLabel	time, location, colon;
 	private boolean			colonOn;
 
 	public Display(Controller controller) {
 		this.controller = controller;
 		
 		this.addKeyListener(this);
 		this.setUndecorated(true);
 		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
 		this.getContentPane().setBackground(Color.black);
 
 	    this.loadFont();
 	    
 	    this.timeTableModel = new TimeTableModel();
 		getContentPane().setLayout(null);
 		this.table = new RotatedTable(this.timeTableModel);
 		
 		((RotatedTable) this.table).lockColumnWidth(0, 140);
 		((RotatedTable) this.table).lockColumnWidth(1, 760);
 		((RotatedTable) this.table).lockColumnWidth(2, 140);
 		
		String ledColorString = this.controller.getConfiguration().getLedColor();
		Color ledColor = Color.decode(ledColorString);
		this.table.setForeground(ledColor);
 		
 		RotatedLabel header = new RotatedLabel("Linie 				 Ziel		             			    	         				                  					         Abfahrt");
 		header.setBounds(1020, 17, 200, 1024);
 		header.setForeground(Color.BLACK);
 		header.setFont(new Font("Bitstream Charter", Font.ITALIC, 37));
 		getContentPane().add(header);
 		
 		this.location = new RotatedLabel("Potsdam Hbf");
 		this.location.setBounds(1195,30,65,550);
		this.location.setForeground(ledColor);
 		this.location.setFont(new Font("JD LCD Rounded", Font.PLAIN, 85));
 		getContentPane().add(this.location);
 		
 		this.time = new RotatedLabel("XX 00.00.00 00:00");
 		this.time.setBounds(1195,490,65,550);
		this.time.setForeground(ledColor);
 		this.time.setFont(new Font("JD LCD Rounded", Font.PLAIN, 85));
 		getContentPane().add(this.time);
 		
 		this.colon = new RotatedLabel(":");
 		this.colon.setBounds(1195,914,65,550);
		this.colon.setForeground(ledColor);
 		this.colon.setFont(new Font("JD LCD Rounded", Font.PLAIN, 85));
 		getContentPane().add(this.colon);
 		
 		Background background = new Background();
 		background.setBounds(0, 0, 1280, 1024);
 		getContentPane().add(background);
 		getContentPane().add(this.table);
 		
 
 		
 	    this.pack();
 	    this.setVisible(true);	   	
 	}
 	
 	private void loadFont() {
 		 GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
 	    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
 		
 	    try {
 			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, classLoader.getResourceAsStream("resources/fonts/jd_lcd_rounded.ttf")));
 		} catch (FontFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) System.exit(0);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {}
 	
 	public void updateList(List<Transport> timeTable) {
 		this.timeTableModel.setData(timeTable);
 		this.timeTableModel.fireTableDataChanged();
 	}
 	
 	public void updateTime(String time) {
 		this.time.setText(time);
 	}
 	
 	public void updateTimeColon() {
 		this.colon.setVisible(this.colonOn);
 		
 		this.colonOn = !this.colonOn;
 	}
 }
 
 
 
 
 
