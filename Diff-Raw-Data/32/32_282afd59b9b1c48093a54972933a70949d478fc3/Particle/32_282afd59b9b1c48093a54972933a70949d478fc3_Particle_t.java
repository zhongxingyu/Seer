 import java.awt.*;
 import java.awt.geom.Ellipse2D;
 
 
 public class Particle 
 {
 	public double x,y;
 	@SuppressWarnings("unused")
 	private double t = 0;
 	private double speed;
 	private double angle;
	private double xR;
	private double yR;
 
 //	private float red;
 //	private float green;
 //	private float blue;
 	public float alpha;
 	
 	
 	public Particle()
 	{
 		speed = Math.random() * 20.0;
 		angle = Math.random() * 60 + 60.0;
 		
 //		red = (float)Math.random();
 //		green = (float)Math.random();
 //		blue = (float)Math.random();
 		
 		alpha = 1.0f;
 	}
 	
 	public Particle(int x2, int y2) {
 		this.x = x2;
 		this.y = y2;
		int min = 1;
		int max = 4;
 		speed = (Math.random() * (max - min) + min)* 20.0;
 		angle = (Math.random() * (max - min) + min)* 60 + 60.0;
		//Gives a direction to the particle
		xR = (Math.random() * (max - min) + min);
		yR = (Math.random() * (max - min) + min);
 //		red = (float)Math.random();
 //		green = (float)Math.random();
 //		blue = (float)Math.random();
 		alpha = 1.0f;
 	}
 
 	public void update()
 	{
		//direction depends on the random number generator
		if(xR<=2)x -= (speed * Math.cos((Math.PI/180)*angle))/55;
 		else x += (speed * Math.cos((Math.PI/180)*angle))/20;
		if(yR<=2)y += (speed * Math.sin((Math.PI/180)*angle))/27;
 		else y -= (speed * Math.sin((Math.PI/180)*angle))/27;
 		t += 0.05;
		alpha -= 0.01f;
 		alpha = Math.max(0,alpha);
 	}
 	
 
 	
 	public void draw(Graphics g)
 	{
 		Graphics2D g2 = (Graphics2D)g;
 		Ellipse2D.Double ellipse = new Ellipse2D.Double(x,y,40,40);
 		g2.setColor(new Color((float)1.0, (float)1.0, (float)1.0, alpha));
 //		g2.setColor(new Color((red, green, blue, alpha));
 		g2.fill(ellipse);
 		
 	}
 	
 	
 }
