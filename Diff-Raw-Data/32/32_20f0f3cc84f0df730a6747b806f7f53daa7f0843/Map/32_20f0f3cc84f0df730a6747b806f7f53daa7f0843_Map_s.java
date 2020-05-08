 package warborn.view.map;
 
 import java.awt.Color;
 import java.awt.Image;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 
 import warborn.constants.MapData;
 import warborn.constants.MapNotFoundException;
 import warborn.model.Warborn;
 
 public abstract class Map extends JPanel implements Observer {
 
 	private Warborn model;
 	private JButton[] buttons;
 	private double[][] scalingConstants;
 
 	/**
 	 * Create the Map panel.
 	 */
 
 	public Map(Warborn model) {
 		this.model = model;
 
 		try {
 			scalingConstants = MapData.getScalingConstants(toString());
 		} catch (MapNotFoundException mnfe) {
 			mnfe.printStackTrace();
 		}
 		setLayout(null);
 		setSize(model.getWidth(), model.getHeight());
 		buttons = new JButton[model.getTerritories().length];
 
 		JLabel maplbl = new JLabel("");
 		maplbl.setVerticalAlignment(SwingConstants.TOP);
 		maplbl.setBounds(0, 0, model.getWidth(),
 				(int) (model.getHeight() * 0.75));
 
 		// Scaling the Map image to fit the screensize
 		Image I = new ImageIcon("WarbornData/images/" + toString() + ".png")
 				.getImage();
 		I = I.getScaledInstance(model.getWidth(),
 				(int) ((model.getHeight()) * 0.75), 0);
 		maplbl.setIcon(new ImageIcon(I));
 
 		for (int i = 0; i < model.getTerritories().length; i++) {
 
 			buttons[i] = new JButton();
 
 			buttons[i].setText(model.getTerritory(i).getNbrOfUnits() + "");
 
 			buttons[i].setActionCommand(i + "");
 
 			buttons[i].setBackground(model.getTerritory(i).getOwner()
 					.getColor());
 
 			if (buttons[i].getBackground().getRed()
 					+ buttons[i].getBackground().getGreen()
 					+ buttons[i].getBackground().getBlue() < 250) {
 				buttons[i].setForeground(Color.WHITE);
 			}
 
 			else {
 				buttons[i].setForeground(Color.BLACK);
 			}
 			buttons[i].setBounds(
 					(int) (model.getWidth() * scalingConstants[i][0]),
 					(int) (model.getHeight() * scalingConstants[i][1]), 50, 45);
 
 			/**
 			 * buttons[i].setOpaque(false); buttons[i].setFocusPainted(false);
 			 * buttons[i].setBorderPainted(false);
 			 * buttons[i].setContentAreaFilled(false);
 			 * buttons[i].setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
 			 * buttons[i].setBorder(new RoundedBorder(22));
 			 * buttons[i].setForeground
 			 * (model.getTerritory(i).getOwner().getColor());
 			 */
 		}
 
 		add(maplbl, 0);
 		for (int i = 0; i < model.getTerritories().length; i++) {
 			add(buttons[i], 0);
 		}
 
 	}
 
 	public JButton[] getButtons() {
 		return buttons;
 	}
 
 	@Override
 	public String toString() {
 		return MapData.getMapName(model.getMapIndex());
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (model.getPhase() == 1) {
 			for (int i=0; i<buttons.length; i++){
 				buttons[i].setEnabled(false);
 			}
 		} else {
 			for (int i=0; i<buttons.length; i++){
 				buttons[i].setEnabled(true);
 			}
 			this.requestFocus();
 			
 			for (int i = 0; i < buttons.length; i++) {
 				buttons[i].setText(model.getTerritory(
 						Integer.parseInt(buttons[i].getActionCommand()))
 						.getNbrOfUnits()
 						+ "");
 				buttons[i].setBackground(model.getTerritory(i).getOwner()
 						.getColor());
 				if (buttons[i].getBackground().getRed()
 						+ buttons[i].getBackground().getGreen()
 						+ buttons[i].getBackground().getBlue() < 250) {
 					buttons[i].setForeground(Color.WHITE);
 				} else {
 					buttons[i].setForeground(Color.BLACK);
 				}
 				if (model.getTerritory(i).isProtected()) {
					buttons[i].setBackground(new Color(
							buttons[i].getBackground().getRed() + 40>255?255:buttons[i].getBackground().getRed() + 40,
							buttons[i].getBackground().getGreen() + 40>255?255:buttons[i].getBackground().getGreen() + 40,
									buttons[i].getBackground().getBlue() + 40>255?255:buttons[i].getBackground().getBlue() + 40));
 				}
 			}
 			if (model.getSelectedTerritoryIndex() > -1) {
 				buttons[model.getSelectedTerritoryIndex()]
 						.setBackground(Color.GRAY);
 			}
 		}
 	}
 }
