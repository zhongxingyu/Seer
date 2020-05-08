 package hu.laborreg.server.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.lang.reflect.InvocationTargetException;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingUtilities;
 
 public class MainWindow {
 
 	private JFrame frame;
 	private JTabbedPane tabbedPane;
 	private JToolBar toolBar;
 
 	public static void display() {
 		try {
 			SwingUtilities.invokeAndWait(new Runnable() {
 				@Override
 				public void run() {
 					MainWindow window = new MainWindow();
 					window.frame.setVisible(true);
 				}
 			});
 		} catch (InvocationTargetException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public MainWindow() {
 		initialize();
 	}
 
 	private void initialize() {
		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		frame.setMinimumSize(new Dimension(450, 300));
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		toolBar = new JToolBar();
 		toolBar.setRollover(true);
 		toolBar.setFloatable(false);
 		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));
 		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
 
 		createToolbarButtons();
 
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
 
 		createTabs();
 	}
 
 	private void createToolbarButtons() {
 		createButton("Hozzáad", "Új hozzáadása", "plus-circle");
 		createButton("Töröl", "Kijelölt törlése", "minus-circle");
		createButton("Szerkeszt", "Kijelölt szerkesztése", "arrow-circle-double-135");
 		createButton("Frissít", "Frissítés", "arrow-circle-double-135");
 	}
 
 	private void createButton(String buttonText, String tooltipText, String iconName) {
 		String imagePath = "res/icons/" + iconName + ".png";
 		JButton button = new JButton(buttonText, new ImageIcon(imagePath));
 		button.setToolTipText(tooltipText);
 		button.setHorizontalTextPosition(JButton.CENTER);
 		button.setVerticalTextPosition(JButton.BOTTOM);
 		button.setPreferredSize(new Dimension(100, 53));
 		toolBar.add(button);
 	}
 
 	private void createTabs() {
 		tabbedPane.addTab("Kurzusok", new CourseTable());
 		tabbedPane.addTab("Laboresemények", new JPanel());
 	}
 
 }
