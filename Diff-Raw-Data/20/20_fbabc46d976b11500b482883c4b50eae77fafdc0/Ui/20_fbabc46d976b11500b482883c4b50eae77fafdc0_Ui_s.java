 package cz.apopt.entity;
 
 import java.awt.Font;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 
 import cz.apopt.entity.items.Item;
 import cz.apopt.etc.Color;
 import cz.apopt.etc.OpFont;
 
 /* TODO: Getting vsech hodnot z tanku a od playera by nemelo byt podle mne pokazdy kdyz je render.
  * Update hodnot v UI by mel byt v metode update(), a ta by mela byt volana jen po urcitym case - pouzili bysme ten samej Timer
  * jako na spawn itemu. Takze update by byl treba 1x za 200 - 500ms ... whatever 
  * Bullshit data musi byt aktualni a je to jen 5-6 hodnot opravdu to nic nezpomali, kdyz bys udelal timer tak ten by spis sam o sobe narocnejsi
  * na vykon.
  */
 public class Ui 
 {
 	private Player uiPlayer;
 	private float x,y,width,height,playerPartX,playerPartY,tankPartX,tankPartY;
 	private Texture back_Texture;
 	private Font font;
 	private OpFont lives, weapon, ammo, currentItem, count;
 	private Color col = new Color(0, 40, 100);
 	
 	public Ui(Player player)
 	{
 		this.uiPlayer = player;
 		font = new Font("Arial", Font.BOLD, 12);
 		weapon = new OpFont(tankPartX, tankPartY, "Weapon: ", font, java.awt.Color.WHITE);
 		ammo = new OpFont(tankPartX, tankPartY + 12, "Ammo: ", font, java.awt.Color.WHITE);
 		currentItem = new OpFont(tankPartX, tankPartY + 24, "Current item: ", font, java.awt.Color.WHITE);
 		count = new OpFont(tankPartX, tankPartY + 36, "Count: ", font, java.awt.Color.WHITE);
 		lives = new OpFont(playerPartX, playerPartY, "Lives: " + uiPlayer.lives, font, java.awt.Color.WHITE);
 	}
 	
 	private void render_PlayerPart()
 	{
 		lives.render();
 	}
 	
 	private void pos_init()
 	{
 		playerPartX = x;
 		playerPartY = y;
 		tankPartX = x;
 		tankPartY = y + 17;
 		
 		lives.setPos(playerPartX, playerPartY);
 		weapon.setPos(tankPartX, tankPartY);
 		ammo.setPos(tankPartX, tankPartY + 12);
 		currentItem.setPos(tankPartX, tankPartY + 24);
 		count.setPos(tankPartX, tankPartY + 36);
 	}
 	
 	private void render_TankPart()
 	{
 		weapon.render();
 		ammo.render();
 		currentItem.render();
 		count.render();
 	}
 	
 	public void render() 
 	{	
		GL11.glColor4f(col.R, col.G, col.B, 0.2f);
 		GL11.glBegin(GL11.GL_QUADS);		
 			GL11.glVertex2f(x, y);
 			GL11.glVertex2f(x + width, y);
 			GL11.glVertex2f(x + width, y + height);
 			GL11.glVertex2f(x, y + height);
 		GL11.glEnd();
 		render_PlayerPart();
 		render_TankPart();
 	}
 
 	public void update() 
 	{
 		Item curItem = uiPlayer.getTank().getCurrentItem();
 		boolean itemNull = false;	
 		
 		if(curItem == null) 
 			itemNull = true;
 		
 		lives.setText("Lives: " + uiPlayer.lives);
 		count.setText("Count: " + (itemNull ? "-" : uiPlayer.getTank().getCurrentItemCount()));
 		currentItem.setText("Current item: " + (itemNull ? "NONE" : uiPlayer.getTank().getCurrentItem().getName()));
 		weapon.setText("Weapon: " + uiPlayer.getTank().getClusterCount());
 		ammo.setText("Ammo: " + uiPlayer.getTank().getMissileCount());
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 		pos_init();
 	}
 
 	public float getY() 
 	{
 		return y;
 	}
 
 	public void setY(float y) 
 	{
 		this.y = y;
 		pos_init();
 	}
 
 	public float getWidth()
 	{
 		return width;
 	}
 
 	public void setWidth(float width) 
 	{
 		this.width = width;
 	}
 
 	public float getHeight() 
 	{
 		return height;
 	}
 
 	public void setHeight(float height) 
 	{
 		this.height = height;
 	}
 
 	public Texture getBack_Texture()
 	{
 		return back_Texture;
 	}
 
 	public void setBack_Texture(Texture back_Texture) 
 	{
 		this.back_Texture = back_Texture;
 	}
 
 	public Font getFont()
 	{
 		return font;
 	}
 
 	public void setFont(Font font) 
 	{
 		this.font = font;
 	}
 
 	
 	
 }
