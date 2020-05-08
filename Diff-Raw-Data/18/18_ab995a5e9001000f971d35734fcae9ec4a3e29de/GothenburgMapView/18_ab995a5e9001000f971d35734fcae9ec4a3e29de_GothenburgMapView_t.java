 package warborn.map;
 
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observable;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import warborn.main.MainFrame;
 import warborn.model.Warborn;
 
 public class GothenburgMapView extends Observable implements IMap, ActionListener { 	
 
 	
 	private JPanel mapPanel;
 	private Warborn model;
 	private JButton[] buttons;
 	/**
 	 * Create the Map panel.
 	 */
 	public GothenburgMapView(Warborn model) {
 		
 		this.model = model;
 		
 		mapPanel = new JPanel();
 		mapPanel.setLayout(null);
 		mapPanel.setSize(model.getWidth(), model.getHeight());
		buttons = new JButton[model.getTerritories().length];
 		
 		JLabel maplbl = new JLabel("");
 		maplbl.setVerticalAlignment(SwingConstants.TOP);
 		maplbl.setBounds(0, 0, model.getWidth(), model.getHeight());
 		
 		//Scaling the Map image to fit the screensize
 		Image I = new ImageIcon("images/Gothenburg.jpg").getImage();
 		I = I.getScaledInstance(model.getWidth(), (int) ((model.getHeight())*0.75), 0);
 		
 		maplbl.setIcon(new ImageIcon(I));
 		
 		
 		for (int i = 0; i < model.getTerritories().length; i++){
 			buttons[i] = new JButton();
 			buttons[i].setText(model.getTerritory(i).getNbrOfUnits()+"");
 			buttons[i].addActionListener(this);
 			buttons[i].setActionCommand(i + "");
 			mapPanel.add(buttons[i], 0);
 		}
 		buttons[21].setBounds((int)(model.getWidth()*0.19), (int)(model.getHeight()*0.26), 45,45);
 		
 		buttons[20].setBounds((int)(model.getWidth()*0.26), (int)(model.getHeight()*0.22), 45,45);
 		
 		buttons[0].setBounds((int)(model.getWidth()*0.76), (int)(model.getHeight()*0.16), 45,45);
 		
 		mapPanel.add(maplbl, 0);
		for (int i=0; i<21; i++){
			mapPanel.add(buttons[i], 0);
		}		
 	}
 	
 	public JPanel getMapPanel() {
 		return mapPanel;
 	}
 	
 	public String toString(){
 		return "Gothenburg";
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		setChanged();
 		notifyObservers(Integer.parseInt(((JButton) e.getSource()).getActionCommand()));
 	System.out.println(Integer.parseInt(((JButton) e.getSource()).getActionCommand()) + "   actionPerformed");
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
		buttons[0].setText(model.getTerritory(Integer.parseInt(buttons[0].getActionCommand())).getNbrOfUnits() + "");
		buttons[20].setText(model.getTerritory(Integer.parseInt(buttons[20].getActionCommand())).getNbrOfUnits() + "");
		buttons[21].setText(model.getTerritory(Integer.parseInt(buttons[21].getActionCommand())).getNbrOfUnits() + "");
 		
 	}
 
 
 }
