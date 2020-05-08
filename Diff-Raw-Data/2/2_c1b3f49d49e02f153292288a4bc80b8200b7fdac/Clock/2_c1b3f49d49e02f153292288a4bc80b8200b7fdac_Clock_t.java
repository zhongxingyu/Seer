 package jp.namihira.digitalclock;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.util.prefs.Preferences;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 public class Clock implements Runnable {
 	JFrame frame;
 	ClockPanel clockPanel;
	
 	JMenuItem itemDailog, itemQuit, itemTwitter, itemGraph;
 	
 	JDialog dailog;
 
 	protected Preferences userPrefs;
 	private static final String LOCATION_X = "x";
     private static final String LOCATION_Y = "y";
     private static final String FONT_FAMILY = "fontFamily";
     private static final String FONT_STYLE = "fontStyle";
     private static final String FONT_SIZE = "fontSize";
     private static final String FONT_COLOR = "fontColor";
     private static final String COLOR = "color";
     
     private static final int NO_DEF = -1;
     
     private int locationX;
     private int locationY;
     
     private String fontFamily;
     private int fontStyle;
     private int fontSize;
     private int fontColor;
     private int color;
     
     MyTwitter twitter;
     
 	Clock(String title){
 		frame = new JFrame(title);
 		clockPanel = new ClockPanel(this);
 		twitter = new MyTwitter(clockPanel);
 
 		loadPreferences(clockPanel);
 		
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setSize(clockPanel.getWidth() + 40, clockPanel.getHeight() * 2);
 //		frame.setResizable(false);
 		frame.addComponentListener(new ExComponetAdapter());		
 		
 		frame.setContentPane(clockPanel);
 		frame.setLocation(locationX, locationY);
 		frame.setVisible(true);
 		frame.toFront();
 		
 		
 		JMenuBar menubar = new JMenuBar();
 		
 		JMenu menu = new JMenu("Menu");
 		
 		itemDailog = new JMenuItem("item"); 
 		itemTwitter = new JMenuItem("twitter");
 		itemQuit = new JMenuItem("item");
 		itemGraph = new JMenuItem("item");
 		
 		itemDailog.addActionListener(new MenuItemListener());
 		itemTwitter.addActionListener(new MenuItemListener());
 		itemQuit.addActionListener(new MenuItemListener());
 		itemGraph.addActionListener(new MenuItemListener());
 		
 		
 		menu.add(itemDailog);
 		menu.add(itemTwitter);
 		menu.addSeparator();
 		menu.add(itemQuit);
 		menu.add(itemGraph);
 		
 		menubar.add(menu);
 		
 		frame.setJMenuBar(menubar);
 		
 		dailog = new PropertyDialog(this);
 	}
 
 	
 	public void updateClockPanel(Font font, Color fontColor, Color color){
 		clockPanel.setFont(font);
 		clockPanel.setSize(font);
 		clockPanel.setFontColor(fontColor);
 		frame.setBackground(color);
 		frame.setSize(clockPanel.getWidth()+20, clockPanel.getHeight()*2);
 		saveFontPreferences(font, fontColor, color);
 	}
 	
 	
 	public void run() {
 		while(true){
 			frame.repaint();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public MyTwitter getTwitter(){
 		return twitter;
 	}
 	
 	public JFrame getFrame(){
 		return frame;
 	}
 	
 	class MenuItemListener implements ActionListener{
 		public void actionPerformed(ActionEvent event) {
 			JMenuItem item = (JMenuItem)event.getSource();
 			if (item == itemDailog) {
 				dailog.setVisible(true);
 			} else if (item == itemTwitter) {
 				twitter.setVisible(true);
 			} else if (item == itemGraph) {
 				new Graph();
 			} else if (item == itemQuit) {
 				new Confirm();
 			}
 		}
 	}
 	
 	class ExComponetAdapter extends ComponentAdapter{
         public void componentMoved(ComponentEvent event) {
             Component comp = event.getComponent();
             saveLocationPreferences(comp.getLocation());
         }
 	}
 	
 	private void saveLocationPreferences(Point location) {
         userPrefs.putInt(LOCATION_X, location.x);
         userPrefs.putInt(LOCATION_Y, location.y);
     }
 	
 	private void saveFontPreferences(Font font, Color fontColor, Color color){
 		userPrefs.put(FONT_FAMILY, font.getFamily());
 		userPrefs.putInt(FONT_STYLE, font.getStyle());
 		userPrefs.putInt(FONT_SIZE, font.getSize());
 		userPrefs.putInt(FONT_COLOR, fontColor.getRGB());
 		userPrefs.putInt(COLOR, color.getRGB());
 	}
 	
     private void loadPreferences(ClockPanel clockPanel) {
         userPrefs = Preferences.userNodeForPackage(this.getClass());
         loadUserPreferences();
     }
 
     protected void loadUserPreferences() {
         int tmpX = userPrefs.getInt(LOCATION_X, NO_DEF);
         if (tmpX != NO_DEF) {
             locationX = tmpX;
         }
  
         int tmpY = userPrefs.getInt(LOCATION_Y, NO_DEF);
         if (tmpY != NO_DEF) {
             locationY = tmpY;
         }
         
         String tmpFontFamily = userPrefs.get(FONT_FAMILY, null);
         if (tmpFontFamily != null) {
         	fontFamily = tmpFontFamily;
         }
         
         int tmpFontStyle = userPrefs.getInt(FONT_STYLE, NO_DEF);
         if (tmpFontStyle != NO_DEF) {
         	fontStyle = tmpFontStyle;
         }
         
         int tmpFontSize = userPrefs.getInt(FONT_SIZE, NO_DEF);
         if (tmpFontSize != NO_DEF) {
         	fontSize = tmpFontSize;
         }
  
         int tmpFontColor = userPrefs.getInt(FONT_COLOR, NO_DEF);
         if (tmpFontColor != NO_DEF) {
         	fontColor = tmpFontColor;
         }
         
         int tmpColor = userPrefs.getInt(COLOR, NO_DEF);
         if (tmpColor != NO_DEF) {
         	color = tmpColor;
         }
 
         updateClockPanel(new Font(fontFamily, fontStyle, fontSize), new Color(fontColor, true), new Color(color, true));
     }
 	
 }
