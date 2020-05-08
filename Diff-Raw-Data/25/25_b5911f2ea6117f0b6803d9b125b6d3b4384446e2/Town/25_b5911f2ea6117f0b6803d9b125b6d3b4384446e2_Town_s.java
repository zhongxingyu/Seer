 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 
 public class Town extends JPanel{
 	private static final int WIDTH = 580;
 	private static final int HEIGHT = 600;
 	private JFrame frame;
 	private JPanel oldpanel;
 
 	public Town(JFrame frame){
 		
 		this.frame = frame;
		//oldpanel = (JPanel) frame.getContentPane();
 		setVisible(true);
 		setSize(WIDTH, HEIGHT);
 		frame.setTitle("Town");
 		GridLayout layout = new GridLayout(0,2);
 		JPanel subPanel = new JPanel();
 		subPanel.setLayout(layout);
 		
 		JButton StoreButton = new JButton();  
 		StoreButton.setIcon(new ImageIcon("store.png"));
 		StoreButton.setBorderPainted(false);  
 		StoreButton.setFocusPainted(false);  
 		StoreButton.setContentAreaFilled(false);
 		StoreButton.addActionListener(new StoreListener());
 		
 		JButton PubButton = new JButton();  
 		PubButton.setIcon(new ImageIcon("pub.png"));
 		PubButton.setBorderPainted(false);  
 		PubButton.setFocusPainted(false);  
 		PubButton.setContentAreaFilled(false);
 		PubButton.addActionListener(new PubListener());
 		
 		JButton LandOfficeButton = new JButton();  
 		LandOfficeButton.setIcon(new ImageIcon("landoffice.png"));
 		LandOfficeButton.setBorderPainted(false);  
 		LandOfficeButton.setFocusPainted(false);  
 		LandOfficeButton.setContentAreaFilled(false);
 		LandOfficeButton.addActionListener(new LandOfficeListener());
 		
 		JButton AssayOfficeButton = new JButton();  
 		AssayOfficeButton.setIcon(new ImageIcon("assayoffice.png"));
 		AssayOfficeButton.setBorderPainted(false);  
 		AssayOfficeButton.setFocusPainted(false);  
 		AssayOfficeButton.setContentAreaFilled(false);
 		AssayOfficeButton.addActionListener(new AssayOfficeListener());
 		
 		JButton GoBack = new JButton("Back");
 		GoBack.addActionListener(new BackListener());
 		
 		subPanel.add(StoreButton);
 		subPanel.add(PubButton);
 		subPanel.add(LandOfficeButton);
 		subPanel.add(AssayOfficeButton);
 		this.add(subPanel);
 		this.add(GoBack);
 		
 		frame.setContentPane(this);
 	}
 	
 	private class StoreListener implements ActionListener
     {
        public void actionPerformed (ActionEvent event)
     	{
     	   
     	}
     }
 	
 	private class PubListener implements ActionListener
     {
        public void actionPerformed (ActionEvent event)
     	{
     	   
     	}
     }
 	
 	private class LandOfficeListener implements ActionListener
     {
        public void actionPerformed (ActionEvent event)
     	{
     	   
     	}
     }
 	
 	private class AssayOfficeListener implements ActionListener
     {
        public void actionPerformed (ActionEvent event)
     	{
     	   
     	}
     }
 	
 	private class BackListener implements ActionListener
     {
        public void actionPerformed (ActionEvent event)
     	{
     	   frame.setContentPane(oldpanel);
     	}
     }
 }
