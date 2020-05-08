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
 
 import warborn.model.Model;
 
 public class GothenburgMapView extends Observable implements IMap, ActionListener { 	
 
	
	private static JPanel mapPanel;
	
 	/**
 	 * Create the Map panel.
 	 */
 	public GothenburgMapView(Model model) {
 		
		mapPanel = new JPanel();
 		mapPanel.setLayout(null);
 		
 		mapPanel.setSize(model.getWidth(), model.getHeight());
 		JLabel maplbl = new JLabel("");
 		maplbl.setVerticalAlignment(SwingConstants.TOP);
 		maplbl.setBounds(0, 0, model.getWidth(), model.getHeight());
 		
 		//Scaling the Map to fit the screensize
 		Image I = new ImageIcon("images/Gothenburg.jpg").getImage();
 		I = I.getScaledInstance(model.getWidth(), (int) ((model.getHeight())*0.75), 0);
 		
 		maplbl.setIcon(new ImageIcon(I));
 		mapPanel.add(maplbl);
 		
 		JButton kingsslopebtn = new JButton();
 		kingsslopebtn.setBounds((int)(model.getWidth()*0.20), (int)(model.getHeight()*0.28), 45,45);
 		kingsslopebtn.setText(model.getTerritory(21).getNbrOfUnits()+"");
 		kingsslopebtn.addActionListener(this);
 		
 		JButton cabbageNestbtn = new JButton();
 		cabbageNestbtn.setBounds((int)(model.getWidth()*0.24), (int)(model.getHeight()*0.25), 45,45);
 		cabbageNestbtn.setText(model.getTerritory(20).getNbrOfUnits()+"");
 		cabbageNestbtn.addActionListener(this);
 		
 		System.out.println(model.getTerritory(20).getNbrOfUnits()+"");
 		
 		mapPanel.add(kingsslopebtn);
 		mapPanel.add(cabbageNestbtn);
 		
 	}
 	
	public static Component getMapPanel(){
		return mapPanel;
	}
	
 	public String toString(){
 		return "Gothenburg";
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		notifyObservers(e);
 		
 	}
 }
