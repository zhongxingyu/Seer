 package fart.dungeoncrawler;
 
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import fart.dungeoncrawler.actor.Actor;
 
 import Utils.Vector2;
 
 public class Trap extends GameObject implements ITriggerable
 {
 	//private Point tilePosition;
 	private Rectangle triggerArea;
 	private BufferedImage texture;
 	private int damage = 1;
 	
 	public Trap(Vector2 tilePosition)
 	{
 		super();
 		
 		this.screenPosition = new Vector2(tilePosition.x * Tilemap.TILE_SIZE, tilePosition.y * Tilemap.TILE_SIZE);
 		this.triggerArea = new Rectangle((int)screenPosition.x, (int)screenPosition.y, Tilemap.TILE_SIZE, Tilemap.TILE_SIZE);
 	}
 	
 	public Trap(TrapDescription desc, Vector2 position) {
 		this.screenPosition = new Vector2(position.x * Tilemap.TILE_SIZE, position.y * Tilemap.TILE_SIZE);
		this.triggerArea = new Rectangle((int)position.x * Tilemap.TILE_SIZE, (int)position.y * Tilemap.TILE_SIZE, desc.getTexture().getWidth(), desc.getTexture().getHeight());
 		this.texture = desc.getTexture();
 		this.damage = desc.getDamage();
 	}
 	
 	@Override
 	public void trigger(Actor trigger)
 	{
 		Health health = trigger.getHealth();
 		health.reduceHealth(damage);
 		if(health.isDead())
 			trigger.terminate();
 	}
 	
 	//@Override
 	//public void trigger(BaseNPC npc) {
 	//	npc.getHealth().reduceHealth(damage);
 	//}
 
 	@Override
 	protected BufferedImage getTexture()
 	{
 		//BufferedImage trapimage = null;
 		if(texture == null) {
 		
 			try
 			{
 				texture = ImageIO.read(new File("res/trap.png"));
 			} catch (IOException e)
 			{
 				System.err.println("Couldn't load trap image");
 				System.exit(1);
 			}
 		}
 		return texture;
 	}
 
 	@Override
 	public Rectangle getCollisionRect() {
 		return triggerArea;
 	}
 
 	@Override
 	public void terminate() { }
 
 }
