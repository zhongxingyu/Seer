 //VisualProblemSolution.java
 //renders the ProblemSolution
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.Random;
 
 public class VisualProblemSolution extends ProblemSolution
 {
 	public int x, y;					//x,y is the center of the problem and solution
 	public static final int width = 300, height = 200, stripeHeight = 10;	//height is the height of problem + height of solution
 	public static final int padding = 50;	//the minimum distance that 2 problems must be apart
 	public static final Font font = new Font("Monospaced", Font.PLAIN, 12);
 	
 	public int camX = 0;
 	
 	public Color color;
 	public static int colorPos = 0;
 	
 	public VisualProblemSolution(int x, int y)
 	{
 		this.x = x;
 		this.y = y;
 		color = niceColor();
 	}
 	
 	// generate random float between floor and ceiling, inclusive
	public float random(float floor, float ceiling)
 	{
		return (Math.random()*(1.0 + ceiling - floor)) + floor;
 	}
 	
 	// generate random double between floor and ceiling, inclusive
	public double random(double floor, double ceiling)
 	{
 		return (Math.random()*(1.0 + ceiling - floor)) + floor;
 	}
 	
 	// generate random int between floor and ceiling, inclusive
	public int random(int floor, int ceiling)
 	{
 		return (int)((Math.random()*(1.0 + ceiling - floor)) + floor);
 	}
 	
 	//use golden ratio to create pleasant colors
 	//http://martin.ankerl.com/2009/12/09/how-to-create-random-colors-programmatically/
 	public static Color niceColor()
 	{
 		double goldenRatioConj = (1.0 + Math.sqrt(5.0)) / 2.0;
 		float hue = new Random().nextFloat();
 		
 		hue += goldenRatioConj * (colorPos / (5 * Math.random()));
 		hue = hue % 1;
 		
 		return Color.getHSBColor(hue, random(0.5f-0.1f, 0.5f+0.1f), random(0.95f-0.1f, 0.95f+0.1f));
 	}
 	
 	public void tick()
 	{
 		
 	}
 	
 	public void drawString(Graphics g, String text, int x, int y)
 	{
 		int times = text.length() / 42;		//max 42 characters in a box
 		for(int i = 0; i < times; i++)
 		{
 			int insertPoint = 42*(i+1);
 //			if(i == 0)
 //				insertPoint -= 1;
 			text = text.substring(0, insertPoint)+"\n"+text.substring(insertPoint);
 		}
 		
 		if(text.length() > 5*42)
 		{
 			text = text.substring(0, 5*42-5)+" (...)";
 		}
 		
 		String[] textArray = text.split("\n");
 		int fontHeight = g.getFontMetrics().getHeight();
 		for (String line : textArray)
 		{
 			g.drawString(line, x, y);
 			y += fontHeight;
 		}
 	}
 	
 	public void render(Graphics g)
 	{
 		Color red = new Color(255, 108, 90);
 		Color green = new Color(183, 241, 140);
 		
 		//fill problem
 		g.setColor(red);
 		g.fillRect(x-width/2-Main.line.selectedProb.camX, y, width, height/2);
 		
 		//fill solution
 		g.setColor(green);
 		g.fillRect(x-width/2-Main.line.selectedProb.camX, y-height/2, width, height/2);
 		
 		//draw black lines
 		g.setColor(Color.black);
 		g.drawRect(x-width/2-Main.line.selectedProb.camX, y-height/2, width, height+stripeHeight);
 		g.drawLine(x-width/2-Main.line.selectedProb.camX, y, x+width/2-Main.line.selectedProb.camX, y);
 		
 		g.setFont(font);
 		
 		//draw IDs
 		g.setColor(Color.black);
 		g.setFont(font);
 		int IDwidth = 45, IDheight = 15;
 		g.drawString("ID "+solutions.get(selectedSolution).id, x+width/2-Main.line.selectedProb.camX - IDwidth, y + IDheight);	//green
 		g.drawString("ID "+problem.id, x+width/2-Main.line.selectedProb.camX - IDwidth, y-height/2 + IDheight);	//red
 
 		//render problem text
 		g.setColor(Color.black);
 		g.setFont(font);
 		drawString(g, problem.text, x-width/2-Main.line.selectedProb.camX+4, y + 2*IDheight-2);
 		
 		//render solution text
 		g.setColor(Color.black);
 		g.setFont(font);
 		drawString(g, solutions.get(selectedSolution).text, x-width/2-Main.line.selectedProb.camX+4, y-height/2 + 2*IDheight-2);
 		
 		//add colored stripe
 		g.setColor(color);
 		g.fillRect(x-width/2-Main.line.selectedProb.camX+1, y+height/2, width-1, stripeHeight);
 		g.setColor(Color.black);
 		g.drawLine(x-width/2-Main.line.selectedProb.camX, y+height/2, x-width/2-Main.line.selectedProb.camX+width, y+height/2);
 	}
 }
