 package komodo.rpg.sounds;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 
 import komodo.rpg.sounds.ui.ClipPlayerPanel;
 
 public class ProjectPane extends JPanel {
 
 	private JTabbedPane tabPane;
 	
 	public ProjectPane() {
 		setLayout(new BorderLayout());
 		
 		tabPane = new JTabbedPane();
 		add(tabPane,BorderLayout.CENTER);
 		tabPane.addTab("Test 1", new JLabel("First Tab"));
 		tabPane.addTab("Test 2", new JLabel("Second Tab"));
 		JPanel i = new JPanel();
 		i.setOpaque(false);
		i.setLayout(new FlowLayout());
 		i.add(new ClipPlayerPanel(null));
 		i.add(new ClipPlayerPanel(null));
 		i.add(new ClipPlayerPanel(null));
 		tabPane.addTab("Test 3", i);
 		
 		add(new JLabel("<html><h3>Action Buttons</h3></html>"), BorderLayout.SOUTH);
 	}
 }
