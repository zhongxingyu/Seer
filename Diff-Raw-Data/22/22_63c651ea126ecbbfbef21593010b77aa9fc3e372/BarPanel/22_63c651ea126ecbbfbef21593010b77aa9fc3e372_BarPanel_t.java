 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import model.sprites.Player;
 import resources.Translator;
 import controller.GameController;
 
 /**
  * A GUI object which renders the hp and food values of a specified player object.
  * 
  * @author 
  *
  */
 public class BarPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private Player player;
 	private StatusBar hpBar;
 	private StatusBar foodBar;
 
 	/**
 	 * Creates a new instance which will display info about the specified player.
 	 * @param player the player to display information about.
 	 */
 	public BarPanel(Player player) {
 		super();
 		this.player = player;
 		this.setPreferredSize(new Dimension(170, 100));
 		hpBar = new StatusBar(20, 150, 100, player.getHealth());
 		foodBar = new StatusBar(20, 150, 100, player.getFood());
 		
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		g.setColor(new Color(255, 255, 255, 255));
 		g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
 		
 		hpBar.setValue(player.getHealth());
 		foodBar.setValue(player.getFood());
 		
 		setHpBarColor();
 		setFoodBarColor();
 		g.setColor(Color.BLACK);
 		
 		hpBar.render(g, 10, 25, 1);
		g.drawString(Translator.getString("hitPoints"), 15, 40);
 				
 		foodBar.render(g, 10, 60, 1);
		g.drawString(Translator.getString("food"), 15, 75);
 	}
 	/*
 	 * Sets the color of the food bar depending on the food level of the player
 	 * green = high/gaining life, yellow = medium and red = low/loosing life
 	 */
 	private void setFoodBarColor() {
 		if(player.getFood() <= GameController.FOOD_LOW){
 			foodBar.setColor(Color.RED);
 		}else if(player.getFood() < GameController.FOOD_HIGH){
 			foodBar.setColor(Color.YELLOW);
 		}else{
 			foodBar.setColor(Color.GREEN);
 		}
 		
 	}
 	/*
 	 * Sets the color of the health bar depending on the health bar of the player
 	 * red indicates low life, green is medium and high
 	 */
 	private void setHpBarColor(){
 		if(player.getHealth() > 33){
 			hpBar.setColor(Color.GREEN);
 		}else{
 			hpBar.setColor(Color.RED);
 		}
 	}
 }
