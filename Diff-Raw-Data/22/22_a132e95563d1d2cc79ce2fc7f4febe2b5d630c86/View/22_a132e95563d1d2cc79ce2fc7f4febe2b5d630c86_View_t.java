 /*************************************************************************************
  * ===================================================================================
  *
  * This is the main view of the program. It is the main object added to the main
  * JFrame.
  * 
  * ===================================================================================
  *************************************************************************************/
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class View extends JPanel{
 	
 	private static int STATS_HEIGHT = 90;
 	private static int TEXT_HEIGHT = 30;
     private static int FIELD_WIDTH = 80;
     
     GraphPanel omniPanel, directedPanel;
 	JPanel statsPanel;
     JTextField sensorsField, strengthField;
     JButton updateButton,statsButton;
     
     JButton shortestPathButton;
     JButton angleButton;
 	
 	/**
 	 * 
 	 */
 	public View(){
 		setLayout(null);
 		
 		statsPanel = createBasePanel();
 		statsPanel.setLocation(0,0);
 		statsPanel.setSize(Frame.WIDTH,STATS_HEIGHT);
 		add(statsPanel);
 		
 		omniPanel = new GraphPanel();
 		omniPanel.setLocation(0,STATS_HEIGHT);
 		omniPanel.setSize(Frame.WIDTH/2,Frame.HEIGHT);
 		add(omniPanel);
 		directedPanel = new GraphPanel();
 		directedPanel.setLocation(Frame.WIDTH/2 +1,STATS_HEIGHT);
 		directedPanel.setSize(Frame.WIDTH/2,Frame.HEIGHT);
 		add(directedPanel);
 		repaint();
 	}
 	
 	/**
 	 * @param omni
 	 * @param directed
 	 */
 	public void updateNetwork (Network omni, Network directed){
 		omniPanel.DrawNetwork(omni);
 		directedPanel.DrawNetwork(directed);
 		repaint();
 	}
 	
 	
 	
 	/**
 	 * @return
 	 */
 	public JPanel createBasePanel(){
 		JPanel result = new JPanel();
 		result.setLayout(null);
 		
 		JLabel sensorsLabel = new JLabel("Sensors");
 		sensorsLabel.setSize(60,TEXT_HEIGHT);
 		sensorsLabel.setLocation(10,0);
 		result.add(sensorsLabel);
 		
 		sensorsField = new JTextField("30");
 		sensorsField.setLocation(10,TEXT_HEIGHT);
 		sensorsField.setSize(FIELD_WIDTH,TEXT_HEIGHT);
 		result.add(sensorsField);
 		
 		JLabel strengthLabel = new JLabel("Strength");
 		strengthLabel.setSize(60,TEXT_HEIGHT);
 		strengthLabel.setLocation(FIELD_WIDTH+10,0);
 		result.add(strengthLabel);
 		
 		strengthField = new JTextField("80");
 		strengthField.setLocation(10+FIELD_WIDTH,TEXT_HEIGHT);
 		strengthField.setSize(FIELD_WIDTH,TEXT_HEIGHT);
 		result.add(strengthField);
 		
 		updateButton = new JButton("Go!");
 		updateButton.setSize(60,TEXT_HEIGHT);
 		updateButton.setLocation(10+FIELD_WIDTH*2,TEXT_HEIGHT);
 		result.add(updateButton);
 		
 		statsButton = new JButton("Stats!");
 		statsButton.setSize(100,TEXT_HEIGHT);
 		statsButton.setLocation(10+FIELD_WIDTH*3,TEXT_HEIGHT);
 		result.add(statsButton);
 		
 		// Adding the Shortest Path button
 		shortestPathButton = new JButton("SP");
 		shortestPathButton.setSize(100,TEXT_HEIGHT);
 		shortestPathButton.setLocation(10+FIELD_WIDTH*5,TEXT_HEIGHT);
 		result.add(shortestPathButton);
 		
 		// Adding the Shortest Path button
 		angleButton = new JButton("Angle");
 		angleButton.setSize(100,TEXT_HEIGHT);
 		angleButton.setLocation(10+FIELD_WIDTH*6,TEXT_HEIGHT);
 		result.add(angleButton);
 		
 		return result;
 	}
 	
 	/**
 	 * TODO: This needs to be updated to add in labeled fields. Like above, but less tedious
 	 * @param s
 	 * @param x
 	 * @return
 	 */
 	public JPanel LabeledField(String s, int x){
 		JPanel result = new JPanel();
 		return result;
 	}
 	
 	/**
 	 * @return
 	 */
 	public JButton getUpdateButton(){
 		return updateButton;
 	}
 	
 	/**
 	 * @return
 	 */
 	public JButton getStatsButton(){
 		return statsButton;
 	};
 	
 	/**
 	 * @return
 	 */
 	public int getSensors(){
 		int x = Integer.parseInt(sensorsField.getText());
		if (x < 10){
 			sensorsField.setText("10");
 		}
 		else if (x > 100){
 			sensorsField.setText("100");
		}
 		return Integer.parseInt(sensorsField.getText());
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getStrength(){
 		int x = Integer.parseInt(strengthField.getText());
 		if (x < 50)
 			strengthField.setText("50");
 		else if (x > 1000)
 			strengthField.setText("1000");
 		return Integer.parseInt(strengthField.getText());
 	}
 
 	/**
 	 * @return
 	 */
 	public JButton getShortestPathButtong() {
 		return shortestPathButton;
 	}
 
 	/**
 	 * @return
 	 */
 	public JButton getAngleButton() {
 		return angleButton;
 	}
 
 	/**
 	 * @param angleButton
 	 */
 	public void setAngleButton(JButton angleButton) {
 		this.angleButton = angleButton;
 	}
 	
 	
 	
 	
 	//this is for testing the appearance of the view.
 /*	public static void main(String args[]){
 		JFrame f = new JFrame("test");
 		f.getContentPane().add(new View());
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setSize(Frame.WIDTH,Frame.HEIGHT);
 		f.setVisible(true);
 		f.setResizable(false);
 	}*/
 
 }
