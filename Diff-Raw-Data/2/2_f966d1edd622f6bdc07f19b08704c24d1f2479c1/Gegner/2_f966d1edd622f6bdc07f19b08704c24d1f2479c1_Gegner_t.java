 package prototype;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 public class Gegner {
 	private static BufferedImage bimg;
 	private float f_Gegnerposy_x;
 	private float f_Gegnerposy_y;
 	private Rectangle bounding;
 	private float existiertseit;
 	private float entfernung; //entfernung zwischen Gegner und Spieler
 	private int gegnergeschwindigkeit=300;
 	private float zufallszahl; //fr zufallsbasierte Bewegung 
 	private float reichweite=700; //legt fast ab welcher Entfernung zum Spieler die Gegner angreifen
 	
 	
 	public Gegner( float Gegnerx, float Gegnery, List<Gegner> Enemys){
 		try {
 			bimg = ImageIO.read(getClass().getClassLoader().getResourceAsStream("gfx/gengar.png"));
 		} catch (IOException e) {e.printStackTrace();}
 		
 		this.f_Gegnerposy_x = Gegnerx;
 		this.f_Gegnerposy_y = Gegnery;
 		bounding = new Rectangle((int)Gegnerx, (int)Gegnery, bimg.getWidth(), bimg.getHeight());
 	}
 	
 	public void update(float timeSinceLastFrame){
 		existiertseit+=timeSinceLastFrame;
 		if(existiertseit>=4){
			zufallszahl=(float)(Math.random()-0.5);
 			existiertseit=0;
 			System.out.println(zufallszahl);
 		}
 		
 		
 		
 		
 		entfernung=(float) Math.sqrt((Player.getBounding().x-f_Gegnerposy_x)*(Player.getBounding().x-f_Gegnerposy_x)+(Player.getBounding().y-f_Gegnerposy_y)*(Player.getBounding().y-f_Gegnerposy_y));
 	   
 	    if(Math.sqrt((Player.getBounding().x-f_Gegnerposy_x)*(Player.getBounding().x-f_Gegnerposy_x)+(Player.getBounding().y-f_Gegnerposy_y)*(Player.getBounding().y-f_Gegnerposy_y))<reichweite){
 		f_Gegnerposy_x=f_Gegnerposy_x+(Player.getBounding().x-f_Gegnerposy_x)/entfernung*timeSinceLastFrame*gegnergeschwindigkeit+zufallszahl*(-((existiertseit-2)*(existiertseit-2))+4); //-(x-2)^2+4
 		f_Gegnerposy_y=f_Gegnerposy_y+(Player.getBounding().y-f_Gegnerposy_y)/entfernung*timeSinceLastFrame*gegnergeschwindigkeit+zufallszahl*(-((existiertseit-2)*(existiertseit-2))+4);
 		bounding.x = (int)f_Gegnerposy_x;
 		bounding.y = (int)f_Gegnerposy_y;}
 		
 		
 	}
 	
 	public Rectangle getBounding(){
 		return bounding;
 	}
 	
 	public static BufferedImage getLook(){
 		return bimg;
 	}
 	public int getX(){
 		return (int)f_Gegnerposy_x;
 	}
 	
 	public int getY(){
 		return (int)f_Gegnerposy_y;
 	}
 }
