 package Game;
 
 import java.awt.Graphics;
 
 public class Bullet extends MovingObject
 {
 	static final int MAXSPEED = 500;
 	public Bullet(float xpos, float ypos, float xspeed, float yspeed)
 	{
 		super(xpos, ypos, xspeed, yspeed);
 		
 		look = Imageloader.loadImage("Schuss");
 	}
 	public boolean update(float tslf, Tile[][] tiles)
 	{
 		xpos += xspeed * tslf;
 		ypos += yspeed * tslf;
 		
 		int tilexpos = (int) ((xpos + look.getWidth() / 2) / Texture.tilesize);
		int tileypos = (int) ((xpos + look.getHeight() / 2) / Texture.tilesize);
 		if(tiles[tilexpos][tileypos].getLookID() == 3) return true;
 		
 		if(xpos < 0) return true;
 		if(ypos < 0) return true;
 		if(xpos > World.width * Texture.tilesize) return true;
 		if(ypos > World.height * Texture.tilesize) return true;
 		return false;
 	}
 	public void draw(Graphics g, int worldx, int worldy)
 	{
 		g.drawImage(look, (int) xpos + worldx, (int) ypos + worldy, null);
 	}
 }
