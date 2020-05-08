 package presentation;
 
 
 import java.awt.BorderLayout;
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.*;
 
 /**
  * Presentation class for the main window. Contains the tabs that contains the panels.
  * @author Kristoffer Karlsson
  *
  */
 
 
 public class MainWindow extends JFrame {
 
 	private JPanel contentPane;
 	/**
 	 * Method "createInnerPanel" creates the panel that is inside the tab
 	 * @param text is a placeholder, will be replaced by the other panels
 	 */
 	public JPanel createInnerPanel(String text) {
 		JPanel thePanel = new JPanel();
 		JLabel theLabel = new JLabel(text);
 		theLabel.setHorizontalAlignment(JLabel.CENTER);
 		thePanel.setLayout(new GridLayout(1,1));
 		thePanel.add(theLabel);
 		return thePanel;
 	}
 	
 	 public static void main(String[] args) {
 		 MainWindow mw = new MainWindow();
 		 mw.setVisible(true);
 	 }
 
 	public MainWindow() {
 		
 		setTitle("Bokhandeln");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 520);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JTabbedPane thePanes = new JTabbedPane();
 		ImageIcon bookIcon = new ImageIcon("dasbook.gif");
 	
		JPanel start = createInnerPanel("Detta r startsidan");
		thePanes.addTab("Startsida", bookIcon, start, "Tab1");
 		thePanes.setSelectedIndex(0);
 		
		JPanel addStuff = createInnerPanel("Hr lgger man till/tar bort bcker");
		thePanes.addTab("Lgg till", bookIcon, addStuff);
 
		JPanel statistic = createInnerPanel("Hr ser man statistik");
		thePanes.addTab("Statistik", bookIcon, statistic);
 		setLayout(new GridLayout(1,1));
 		add(thePanes);
 		
 	}
 
 }
