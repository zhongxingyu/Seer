 package Items;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 
 import domain.backGroundTile.Dirt;
 import domain.backGroundTile.Grass;
 import domain.backGroundTile.Water;
 
 public class Mushroom extends Item{
 	private static Image pic;
 	
 	public Mushroom(){
 		zvalue=65;
 		canNotBeWhit.add(new Water());
 		haveToBeWhit.add(new Grass());
 		String uri="./res/pic/tileset01.png";
 		if(pic==null)
 			pic= Toolkit.getDefaultToolkit().getImage(uri);
 		
 	}
	 public void drawItSelf( Graphics g,ImageObserver io, int x,int y,int with,int higth,float zoom){
 			int picX=606;
 			int picY=480;
			g.drawImage(pic, x,y,x+(int) (with*zoom),y+(int)(higth*zoom),picX, picY, picX+32, picY+32, io);
 	 }
 	@Override
 	public Image getImage() {
 		// TODO Auto-generated method stub
 		BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
 		Graphics imageG = image.getGraphics();
 		int picX=606;
 		int picY=480;
 		imageG.drawImage(pic, 0,0,(int) (32),(int)(32),picX, picY, picX+32, picY+32, null);
 		
 		
 	
 		return image;
 	}
 }
