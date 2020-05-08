 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Towers{
 	static File file = new File("b.png");
 	File file2 = new File("./image2.txt");
 	static int sleep = 1000;
 	public Graphics g = null;
 	public int x_position = 0;
 	public int y_position = 0;
 	private int aoe = 110;
 	static boolean live = false;
 	static BufferedImage image1 = null;
 	public int attacking;
 	public Towers(int x, int y, Graphics g){
 		set_tower(x, y, g);
 		attack(live);
 	}
 	//sets the private position variables with the ones
 	//passed from the map
 	public void set_tower(int x, int y, Graphics g){
 		this.x_position = x;
 		this.y_position = y;
 		this.g = g;
		paint(g);
 		attack(live);
 	
 		try{
 			image1 = ImageIO.read(file);
 			//BufferedImage image2  = ImageIO.read(file2);
 			}
 		catch(IOException e){
 			
 		}
 	}
 	
 	//draws the tower on the map
	public void paint(Graphics g){
 	
 	//System.out.println(x_position + " " + y_position);
		g.drawImage(image1, x_position - 12, y_position - 12, null);
         g.setColor(Color.black);
 		g.drawOval(x_position - aoe, y_position - aoe, aoe*2, aoe*2);
 
 	}
 	
 	//draws attack animation
 	public void paint2(){		
 			g.setColor(Color.magenta);
 			g.drawOval(x_position - aoe + 75, y_position - aoe + 75, aoe * 2 - 150, aoe * 2 - 150);
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			g.drawOval(x_position - aoe + 50, y_position - aoe +50, aoe *2 - 100, aoe *2 - 100);
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			g.drawOval(x_position - aoe + 12, y_position - aoe + 12, aoe*2 -25, aoe*2 -25);
 			try {
 				Thread.sleep(20);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			g.drawOval(x_position - aoe, y_position - aoe, aoe, aoe);
 
 	}
 	// this gets called after placement of first tower
 		private void attack(boolean live) {
 			//while live and if monsters exist
 			
 			while (live == true) {
 			/*	if(Monsters.monster.size() > 0){
 				//check if a monster is in range
 				for(int i = 0; i < Monsters.monster.size(); i++){
 					if((Monsters.monster[i].x <= x_position + 10 && Monsters.monster[i].y <= y_position + 10) ||
 							(Monsters.monster[i].x <= x_position - 10 && Monsters.monster[i].y <= y_position + 10)||
 							(Monsters.monster[i].x <= x_position + 10 && Monsters.monster[i].y <= y_position - 10)||
 							(Monsters.monster[i].x <= x_position - 10 && Monsters.monster[i].y <= y_position - 10)){
 						attacking = i;
 					}
 				}
 				//call attack animation here
 				////////////////////////////
 				
 				paint2();
 				//take life away from attacked monster
 				Monsters.monster[attacking].life -= 10;
 				//sleep
 				Thread.sleep(sleep);
 			
 			}	*/
 			}
 		}
 }
