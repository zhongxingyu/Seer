 package presentation;
 
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.Date;
 
 /**
  * Presentation class for the main window. Contains the tabs that contains the panels.
  * @author Kristoffer Karlsson
  *
  */
 
 
 public class MainWindow extends JFrame {
 
 	private JPanel contentPane;
 	
 	 public static void main(String[] args) {
 		 MainWindow mw = new MainWindow();
 		 mw.setVisible(true);
 	 }
 
 	public MainWindow() {
 		Date date = new Date();
 		setTitle("Bokhandeln  " + date);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 470, 550);
 		setPreferredSize(new Dimension(600, 550));
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JTabbedPane thePanes = new JTabbedPane();
 		ImageIcon bookIcon = new ImageIcon("thisbook.gif");
 	
 		BrowsePanel bp = new BrowsePanel();
 		bp.initBrowse();
 		thePanes.addTab("Startsida", bookIcon, bp, "Startsida, hitta information om bcker");
 		thePanes.setSelectedIndex(0);
 		
		AddBooksPanel abp = new AddBooksPanel();
 		abp.initAddBooks();
 		thePanes.addTab("Lgg till", bookIcon, abp, "Lgg till, ta bort, eller redigera bcker");
 		
 		StatisticPanel sp = new StatisticPanel();
 		sp.initStatistic();
 		thePanes.addTab("Statistik", bookIcon, sp, "Visa frsljningsstatistik");
 		setLayout(new GridLayout(1,1));
 		add(thePanes);
 		pack();
 
 	}
 
 }
