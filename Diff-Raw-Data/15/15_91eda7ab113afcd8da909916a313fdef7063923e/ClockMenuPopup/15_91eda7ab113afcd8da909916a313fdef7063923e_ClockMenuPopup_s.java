 package sec02.ex03;
 
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 
 public class ClockMenuPopup extends JPopupMenu{
 	private static final long serialVersionUID = 1L;
 	private PropertyData data;
 	private String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
 	private String strColors[] = {"Black", "Red", "Blue", "Cyan", "DarkGray", "Gray", "Green", "LightGray", "Magenta", "Orange", "Pink", "White", "Yellow"};
 	private int sizes[] = {80, 100, 150, 200, 250, 300, 350, 400};
 	
 	private JMenu menuFont, menuFontSize, menuCharColor, menuBackgroundColor;
 	private List<JMenuItem> fontMenuItems = new ArrayList<JMenuItem>();
 	private List<JMenuItem> fontSizeMenuItems = new ArrayList<JMenuItem>();
 	private List<JMenuItem> charColorMenuItems = new ArrayList<JMenuItem>();
 	private List<JMenuItem> backgroundColorMenuItems = new ArrayList<JMenuItem>();
 	
 	//-------------------------------------------------------------------
 	//   Public methods
 	//-------------------------------------------------------------------
 	public ClockMenuPopup(PropertyData data) {	
 		this.data = data;
         createMenu();
         createMenuItem();
         addMenu();
         addMenuItem();
 	}
 	
 	//-------------------------------------------------------------------
 	//   Private methods
 	//-------------------------------------------------------------------	
 	private void createMenu() {
        menuFont = new JMenu("フォント");
        menuFontSize = new JMenu("フォントサイズ");
        menuCharColor = new JMenu("文字色");
        menuBackgroundColor = new JMenu("背景色");
 	}
 	
 	private void createMenuItem() {
 		JMenuItem item;
 		for (int i = 0; i < fonts.length; i++) {
 			item = new JMenuItem(fonts[i]);
         	item.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	String fontName = e.getActionCommand();
                 	data.setFont(fontName);
                 }
             });
         	fontMenuItems.add(item);
         }
         for (int i = 0; i < sizes.length; i++) {
         	item = new JMenuItem(Integer.toString(sizes[i]));
         	item.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	data.setFontSize(e.getActionCommand());
                 }
             });
         	fontSizeMenuItems.add(item);
         }
         for (int i = 0; i < strColors.length; i++) {
         	item = new JMenuItem(strColors[i]);
         	item.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	data.setColor(e.getActionCommand());
                 }
             });
         	charColorMenuItems.add(item);
         	
         	item = new JMenuItem(strColors[i]);
         	item.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                 	data.setBackgroundColor(e.getActionCommand());
                 }
             });
         	backgroundColorMenuItems.add(item);
         }
 	}
 	
 	private void addMenu() {
         this.add(menuFont);
         this.add(menuFontSize);
         this.add(menuCharColor);
         this.add(menuBackgroundColor);
 	}
 	
 	private void addMenuItem() {
 		for (JMenuItem item : fontMenuItems) {
         	menuFont.add(item);
         }
         for (JMenuItem item : fontSizeMenuItems) {
         	menuFontSize.add(item);
         }
         for (JMenuItem item : charColorMenuItems) {
         	menuCharColor.add(item);
         }
         for (JMenuItem item : backgroundColorMenuItems) {
         	menuBackgroundColor.add(item);
         }
 	}
 }
