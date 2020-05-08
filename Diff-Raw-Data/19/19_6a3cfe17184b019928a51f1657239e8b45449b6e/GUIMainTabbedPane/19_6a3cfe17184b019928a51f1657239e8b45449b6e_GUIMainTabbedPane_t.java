 package gui;
 
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 
 public class GUIMainTabbedPane extends JFrame
 {
 	public GUIMainTabbedPane()
 	{
 		JTabbedPane jtp = new JTabbedPane();
 		
 		jtp.add("My Profile", new GUIMyProfile());
 		jtp.add("My Consumables", new GUIMyConsumables());
 		jtp.add("My Attributes", new GUIMyAttributes());
 		jtp.add("Search", new GUISearch());
 		jtp.add("Recommendations", new GUIRecommendations());
 		
 		add(jtp);
 		setSize(1000, 500);
 		setVisible(true);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
 	}
	
 }
