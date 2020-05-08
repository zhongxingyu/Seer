 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 
 import model.Environment;
 import model.Workspace;
 import view.components.PaintComponent;
 import controller.AboutAction;
 import controller.NewAction;
 import controller.QuitAction;
 import exceptions.WorkspaceNotFoundException;
 
 public class MainWindow extends JFrame {
     private static final long serialVersionUID = 1L;
     private JMenuBar menu = new JMenuBar();
     private JTabbedPane tabs = new JTabbedPane();
 
     public MainWindow() {
 	setLayout(new BorderLayout());
 	
 	//application icon
 	ImageIcon icon = new ImageIcon(getClass().getResource("/resources/images/3Paint.png"));
 	setIconImage(icon.getImage());
 	
 	//creating menu bar
 	buildMenu();
 	setJMenuBar(menu);
 
 	add(tabs);
 	
 	JPanel p = new JPanel();
 	p.setLayout(new FlowLayout());
 	p.add(new JLabel("Pasek narzdzi"));
 	p.setBorder(BorderFactory.createRaisedBevelBorder());
	JToolBar t = new JToolBar("Toolbar", JToolBar.HORIZONTAL);
 	t.setBorder(BorderFactory.createEtchedBorder());
	t.add(new JButton("Przycisk"));
 	add(BorderLayout.NORTH, t);
 	add(BorderLayout.WEST, p);
 	p = new JPanel();
 	p.setLayout(new FlowLayout());
 	p.add(new JLabel("Warstwy i inne"));
 	p.setBorder(BorderFactory.createRaisedBevelBorder());
 	add(BorderLayout.EAST, p);
 	p = new JPanel();
 	p.setLayout(new FlowLayout());
 	p.add(new JLabel("Pasek stanu"));
 	add(BorderLayout.SOUTH, p);
     }
     
     private void buildMenu() {
 	JMenu a = null;
 	JMenuItem item = null;
 	//File
 	a = new JMenu("Plik");
 	
 	item = new JMenuItem("Nowy");
 	item.addActionListener(new NewAction(this));
 	a.add(item);
 	
 	a.addSeparator();
 	
 	item = new JMenuItem("Wyjcie");
 	item.addActionListener(new QuitAction(this));
 	a.add(item);
 	
 	menu.add(a);
 	
 	//Help
 	a = new JMenu("Pomoc");
 	
 	item = new JMenuItem("3Paint - informacje");
 	item.addActionListener(new AboutAction(this));
 	a.add(item);
 	menu.add(a);
     }
     
     public void refreshProjectTabs() {
 	Workspace w = null;
 	try {
 	    w = Environment.wrkman.getLast();
 	} catch (WorkspaceNotFoundException e) {
 	    JOptionPane.showMessageDialog(this, "Bd podczas dodawania projektu.", "Bd", JOptionPane.ERROR_MESSAGE);
 	}
 	PaintComponent pc = new PaintComponent(w);
 	JScrollPane sp = new JScrollPane(pc);
 	sp.setBackground(Color.GRAY);
	ImageIcon ic = new ImageIcon(getClass().getResource("/resources/images/3Paint.png"));
	tabs.addTab(w.getName(), ic, sp);
     }
     
 }
