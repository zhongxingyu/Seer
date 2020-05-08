 import java.awt.*;
 import java.awt.geom.Ellipse2D;
 
 
 public class Particle 
 {
 	public double x,y;
 	@SuppressWarnings("unused")
 	private double t = 0;
 	private double speed;
 	private double angle;
 
 	private float red;
 	private float green;
 	private float blue;
 	public float alpha;
 	public Particle()
 	{
 		speed = Math.random() * 20.0;
 		angle = Math.random() * 60 + 60.0;
 		
 		red = (float)Math.random();
 		green = (float)Math.random();
 		blue = (float)Math.random();
 		
 		alpha = 1.0f;
 	}
 	
 	public Particle(int x2, int y2) {
 		this.x = x2;
 		this.y = y2;
 		int min = 4;
 		int max = 1;
 		speed = (Math.random() * (max - min) + min)* 20.0;
 		angle = (Math.random() * (max - min) + min)* 60 + 60.0;
 		
 		red = (float)Math.random();
 		green = (float)Math.random();
 		blue = (float)Math.random();
 		
 		alpha = 1.0f;
 	}
 
 	public void update()
 	{
 //		x-=1;
 //		y+=1;
		
		if(x<=10)x += (speed * Math.cos((Math.PI/180)*angle))/10;
		else x -= (speed * Math.cos((Math.PI/180)*angle))/10;
 		if(y<=2)y += (speed * Math.sin((Math.PI/180)*angle))/20;
 		else y -= (speed * Math.sin((Math.PI/180)*angle))/20;
 		t += 0.05;
 		alpha -= 0.04f;
 		alpha = Math.max(0,alpha);
 	}
 	
 	
 	public void draw(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D)g;
 		Ellipse2D.Double ellipse = new Ellipse2D.Double(x,y,20,20);
 		g2.setColor(new Color(red,green,blue,alpha));
 		g2.fill(ellipse);
 		
 	}
 }
