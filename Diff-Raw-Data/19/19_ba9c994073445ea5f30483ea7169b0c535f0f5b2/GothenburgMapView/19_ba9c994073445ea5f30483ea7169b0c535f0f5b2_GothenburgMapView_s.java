 package warborn.map;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.util.Observable;
 
 import javax.swing.*;
 
 import warborn.model.MapData;
 import warborn.model.MapNotFoundException;
 import warborn.model.Warborn;
 
 public class GothenburgMapView extends JPanel implements IMap { 	
 
 	private static final long serialVersionUID = 1L;
 	private Warborn model;
 	private JButton[] buttons;
 	private double[][] scalingConstants;
 	/**
 	 * Create the Map panel.
 	 */
 	public GothenburgMapView(Warborn model) {
 		
 		this.model = model;
 		try{
			scalingConstants = MapData.getScalingConstants(this.getName());
 		}catch(MapNotFoundException mnfe){
 			mnfe.printStackTrace();
 		}
 		
 		setLayout(null);
 		setSize(model.getWidth(), model.getHeight());
 		buttons = new JButton[model.getTerritories().length];
 		
 		JLabel maplbl = new JLabel("");
 		maplbl.setVerticalAlignment(SwingConstants.TOP);
 		maplbl.setBounds(0, 0, model.getWidth(), (int)(model.getHeight()*0.75));
 		
 		//Scaling the Map image to fit the screensize
 		Image I = new ImageIcon("WarbornData/images/" + getMapName() + ".jpg").getImage();
 		I = I.getScaledInstance(model.getWidth(), (int) ((model.getHeight())*0.75), 0);
 		maplbl.setIcon(new ImageIcon(I));
 		
 		
 		for (int i = 0; i < model.getTerritories().length; i++){
 			buttons[i] = new JButton();
 			buttons[i].setText(model.getTerritory(i).getNbrOfUnits()+"");
 			buttons[i].setActionCommand(i + "");
 			buttons[i].setBackground(model.getTerritory(i).getOwner().getColor());
 			if(buttons[i].getBackground().getRed() + buttons[i].getBackground().getGreen() + buttons[i].getBackground().getBlue() < 250){
 				buttons[i].setForeground(Color.WHITE);
 			}else{
 				buttons[i].setForeground(Color.BLACK);
 			}
 			buttons[i].setBounds((int)(model.getWidth()*scalingConstants[i][0]),(int)(model.getHeight()*scalingConstants[i][1]), 50, 45);
 			/**
 			buttons[i].setOpaque(false);
 			buttons[i].setFocusPainted(false);
 			buttons[i].setBorderPainted(false);
 			buttons[i].setContentAreaFilled(false);
 			buttons[i].setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
 			buttons[i].setBorder(new RoundedBorder(22));
 			buttons[i].setForeground(model.getTerritory(i).getOwner().getColor());
 			*/
 		}
 		
 		add(maplbl, 0);
 		for (int i=0; i<22; i++){
 			add(buttons[i], 0);
 		}		
 	}
 	
 	public JButton[] getButtons() {
 		return buttons;
 	}
 	
 
 	public static String getMapName(){
 		return "Asgauter";
 	}
 	
 	public String toString(){
 		return "Asgauter";
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getPhase() == 0){
 			this.requestFocus();
 		}
 		for(int i = 0; i < buttons.length; i++){
 			buttons[i].setText(model.getTerritory(Integer.parseInt(buttons[i].getActionCommand())).getNbrOfUnits() + "");
 			buttons[i].setBackground(model.getTerritory(i).getOwner().getColor());
 			if(buttons[i].getBackground().getRed() + buttons[i].getBackground().getGreen() + buttons[i].getBackground().getBlue() < 250){
 				buttons[i].setForeground(Color.WHITE);
 			}else{
 				buttons[i].setForeground(Color.BLACK);
 			}
 			if(model.getTerritory(i).isProtected()){
 				buttons[i].setBackground(new Color(
 						buttons[i].getBackground().getRed()+10,
 						buttons[i].getBackground().getGreen()+10,
 						buttons[i].getBackground().getBlue()+10));
 			}
 		}
 		if (model.getSelectedTerritoryIndex()>-1){
 			buttons[model.getSelectedTerritoryIndex()].setBackground(Color.GRAY);
 		}
 	}
 }
