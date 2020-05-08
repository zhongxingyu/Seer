 package infoprivacy.simulator.gui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 
 public class MainWindow extends JFrame 
 {
 	public MainWindow()
 	{
 		JTabbedPane m_tabs = new JTabbedPane();
 		m_tabs.addTab("Setup", new JScrollPane(new SetupManager(),
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
 		m_tabs.addTab("Output", OutputManager.getInstance());
 		setLayout(new BorderLayout());
 		add(m_tabs, BorderLayout.CENTER);
 		
 		setTitle("Driving Simulator");
 		setSize(640, 480);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 	}
 
 	public static void main(String args[])
 	{
 		new MainWindow();
 	}
 }
