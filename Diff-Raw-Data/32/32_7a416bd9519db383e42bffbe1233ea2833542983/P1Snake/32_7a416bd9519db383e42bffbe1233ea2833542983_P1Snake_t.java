 package twosnakes;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 
 public class P1Snake implements Snake 
 {
 	class Head
 	{
 		public Vector rPiv;
 		public Vector lPiv;
 	}
 	
 	class Body
 	{
 		public Vector rPiv;
 		private Vector lPiv;
 	}
 	
 	class Tail
 	{
 		public Vector rPiv;
 		public Vector lPiv;
 	}
 	
 	private double speed;
 	private Vector direction;
 	private int girth;
 	public Vector headSize, bodySize, tailSize;
 	public Head head;
 	public ArrayList<Body> bodyList;
 	public Tail tail;
 	
 	public P1Snake(Vector headPos, Vector facing, Vector headSize, Vector bodySize, Vector tailSize)
 	{
 		this.headSize = headSize;
 		this.bodySize = bodySize;
 		this.tailSize = tailSize;
 		head = new Head();
 		head.rPiv = new Vector(headPos);
 		facing.normalize();
 		direction = new Vector(facing);
 		Vector back = new Vector(facing.x * -1, facing.y * -1);
 		head.lPiv = new Vector(headPos, back.x * headSize.x, back.y * headSize.y);
 		Body segment = new Body();
 		segment.rPiv = new Vector(head.lPiv);
 		segment.lPiv = new Vector(segment.rPiv, back.x * bodySize.x, back.y * bodySize.y);
 		bodyList = new ArrayList<Body>();
 		bodyList.add(0, segment);
 		tail = new Tail();
 		tail.rPiv = new Vector(segment.lPiv);
 		tail.lPiv = new Vector(tail.rPiv, back.x * tailSize.x, back.y * tailSize.y);
 		this.speed = 1;
 	}
 
 	@Override
 	public void draw(Graphics g)
 	{
 		Graphics2D g2d = (Graphics2D)g;
 		AffineTransform transform = new AffineTransform();
 		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
 		
 		// Draw head
 		try
 		{
 			img = ImageIO.read( new File("images/s01_head_closed.png") );
 		}
 		catch (Exception e)
 		{
 		}
 		transform.translate((head.rPiv.x + head.lPiv.x)/2, (head.rPiv.y+head.lPiv.y)/2);
 		g2d.drawImage(img, transform, null);
 		
 		// Draw body
 		transform = new AffineTransform();
 		try
 		{
 			img = ImageIO.read( new File("images/s1_body.png") );
 		}
 		catch (Exception e)
 		{
 		}
 		for (Body body : bodyList)
 		{
 			transform.translate((body.rPiv.x + body.lPiv.x)/2, (body.rPiv.y+body.lPiv.y)/2);
 			g2d.drawImage(img, transform, null);
 		}
 		
 		// Draw tail
 		transform = new AffineTransform();
 		try
 		{
 			img = ImageIO.read( new File("images/s01_tail.png") );
 		}
 		catch (Exception e)
 		{
 		}
 		transform.translate((tail.rPiv.x + tail.lPiv.x)/2, (tail.rPiv.y+tail.lPiv.y)/2);
 		g2d.drawImage(img, transform, null);
 	}
 	
 	@Override
 	public void move(double timePassed) 
 	{
		double nextLSpotX = head.lPiv.x;
		double nextLSpotY = head.lPiv.y;
		double nextRSpotX = head.rPiv.x;
		double nextRSpotY = head.rPiv.y;
 		int bodyListLength = bodyList.size();
 		head.rPiv.x = speed * timePassed * direction.x;
 		head.rPiv.y = speed * timePassed * direction.y;
 		head.lPiv.x = speed * timePassed * direction.x;
 		head.lPiv.y = speed * timePassed * direction.y;
		
 		for (int i=0; i<bodyListLength;i++){
 			Body currBody = bodyList.get(i);
 			double temp = currBody.rPiv.x;
			// This is for the right pivot
			currBody.rPiv.x = nextRSpotX;
			nextRSpotX = temp;
 			temp = currBody.rPiv.y;
			currBody.rPiv.y = nextRSpotY;
			nextRSpotY = temp;
			// this is for the left pivot
			currBody.lPiv.x = nextLSpotX;
			nextLSpotX = temp;
			temp = currBody.lPiv.y;
			currBody.lPiv.y = nextLSpotY;
			nextLSpotY = temp;
 		}
		tail.rPiv.x = nextRSpotX;
		tail.rPiv.y = nextRSpotY;
		tail.lPiv.x = nextLSpotX;
		tail.lPiv.y = nextLSpotY;
 		
 	}
 	
 	@Override
 	public void setDirection(Vector direction)
 	{
 		this.direction = new Vector(direction);
 		direction.normalize();
 		// Update the head based on the new direction
 		head.rPiv = new Vector(head.lPiv, direction.x * headSize.x, direction.y * headSize.y);
 	}
 
 	@Override
 	public Vector getDirection()
 	{
 		return direction;
 	}
 
 	@Override
 	public int getGirth()
 	{
 		return girth;
 	}
 
 	@Override
 	public void setGirth(int girth)
 	{
 		this.girth = girth;
 	}
 	
 	@Override
 	public int getLength()
 	{
 		return bodyList.size();
 	}
 	
 	@Override
 	public void addSegments(int num)
 	{
 		
 	}
 	
 	@Override
 	public void set_speed(double speed) {
 		this.speed = speed;
 	}
 
 	@Override
 	public double get_speed() {
 		return this.speed;
 	}
 }
