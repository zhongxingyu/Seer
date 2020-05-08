 package renderer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 
 import javax.swing.JTextField;
 
 import playable.Player;
 import playable.TypeOfPlayer;
 
 /**
  * This class display a player at the bottom of the window (the number of agents, of towers...)
  * @author Yuki
  *
  */
 @SuppressWarnings("serial")
 public class PlayerSprite extends Sprite {
 	/**
 	 * name is the JTextField which display the name of the player.
 	 */
 	private JTextField name;
 	/**
 	 * nbAgents is the JTextField which is used to display the total of nbAgents of the corresponding player.
 	 */
 	private JTextField nbAgents;
 	/**
 	 * enginePlayer is a reference to the corresponding player of this sprite.
 	 */
 	private Player enginePlayer;
 	
 	public PlayerSprite(Player newPlayer) {
 		super();
 		this.setLayout(new BorderLayout());
 		
 		this.size = 40;
 		
 		this.enginePlayer = newPlayer;
 		
 		this.name = new JTextField(newPlayer.getNameOfPlayer());
 		this.name.setFont(new Font(this.name.getFont().getName(), this.name.getFont().getStyle(), 9));
 		this.name.setPreferredSize(new Dimension(23, 20));
 		this.name.setDisabledTextColor(new Color(255, 255, 255));
 		this.name.setEnabled(false);
 		this.name.setBorder(null);
 		this.name.setHorizontalAlignment(JTextField.CENTER);
 		this.name.setOpaque(false);
 		this.name.setIgnoreRepaint(false); // for better performance
 		this.add(this.name, BorderLayout.SOUTH);
 		
 		this.nbAgents = new JTextField(String.valueOf(newPlayer.getTotalNbAgents()));
 		this.nbAgents.setPreferredSize(new Dimension(23, 20));
 		this.nbAgents.setDisabledTextColor(new Color(255, 255, 255));
 		this.nbAgents.setEnabled(false);
 		this.nbAgents.setBorder(null);
 		this.nbAgents.setHorizontalAlignment(JTextField.CENTER);
 		this.nbAgents.setOpaque(false);
 		this.nbAgents.setIgnoreRepaint(false); // for better performance
 		this.add(this.nbAgents, BorderLayout.NORTH);
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g){
 		super.paintComponent(g);
 		
 		//update the JTextField which represents the nbAgents
 		this.nbAgents.setText(String.valueOf(this.enginePlayer.getTotalNbAgents()));
 
 	}
 	
 	// GETTERS & SETTERS
 	
 	public JTextField getNameOfPlayer() {
 		return name;
 	}
 	
 	public JTextField getNbAgents() {
 		return nbAgents;
 	}
 
 	public Player getEnginePlayer() {
 		return enginePlayer;
 	}
 }
