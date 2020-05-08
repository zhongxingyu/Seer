 package view;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JPanel;
 
 import resources.Translator;
 
 
 import model.items.weapons.Weapon;
 import model.sprites.Player;
 /**
  * A class that draws the picture that each weapon slot has in the HUD(PlayerPanel).
  * 
  * @author Jonatan and Martin
  *
  */
 public class SlotPanel extends JPanel{
 	
 	private static final long serialVersionUID = 1L;
 	private Player player;
 	private static BufferedImage[] textures = Resources.splitImages("supplies.png", 5, 5);
 	private static BufferedImage slot = Resources.getSingleImage("slot.png");
 	private int weaponIndex;
 	
 	/**
 	 * Creates an instance of a slot panel containing a picture and name of the weapon, 
 	 * it also indicates if that weapon is selected by the player.
 	 * @param player the player holding the weapon.
 	 * @param weapon the weapon to display.
 	 */
 	public SlotPanel(Player player, Weapon weapon){
 		super();
 		this.setBackground(Color.BLACK);
 		this.player = player;
 		this.weaponIndex = player.getIndex(weapon);
 	}	
 
 	//Using paint and not paintComponents as this panel does not have to be buffered nor transparent
 	@Override
 	public void paint(Graphics g){
 		g.setColor(new Color(0, 0, 0, 100));
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		
 		g.drawImage(slot, this.getWidth()/2 - slot.getWidth()/2, 5, null);
 		Weapon w = player.getWeapons()[weaponIndex];
 		if(w != null) {
 			g.setColor(Color.WHITE);
 			StringBuilder sb = new StringBuilder();
 			for(int i = 0; i < w.getKeys().length; i++) {
				sb.append(Translator.getWeaponString(w.getKeys()[i]));
 			}
 			String text = sb.toString();
 			g.drawString(text, this.getWidth()/2 - 
 					(int)g.getFontMetrics().getStringBounds(text, g).getWidth()/2, this.getHeight() - 4);
 			g.drawImage(textures[player.getWeapons()[weaponIndex].getIconNumber()], this.getWidth()/2 - slot.getWidth()/2, 5, null);
 		}		
 		
 		g.setColor(new Color(76, 76, 76));
 		g.drawRect(this.getWidth()/2 - slot.getWidth()/2, 5, slot.getWidth(), slot.getHeight());
 
 		//If this is the active weapon, draw some indications
 		if(player.getActiveWeapon() == player.getWeapons()[weaponIndex]){
 			g.setColor(Color.RED);
 			g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
 		}	
 	}
 }
