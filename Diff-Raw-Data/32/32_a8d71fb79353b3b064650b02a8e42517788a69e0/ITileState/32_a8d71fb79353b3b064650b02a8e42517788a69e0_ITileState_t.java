 package core.tilestate;
 
 import java.awt.image.BufferedImage;
 
 public interface ITileState 
 {
 	public String type();
 	public BufferedImage image();
	public void hidden( boolean hidden );
	public boolean hidden();
 }
