 package menusystem2;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.geom.Rectangle2D;
 
 import main.Main;
 
 public class Loader extends Menu
 {
	public boolean test = true;
	
 	public double currentVal = 0, maxVal = 100;
 	public Color fgcolor = Color.green;
 	
 	public int currentTime = 0, checkTime = 1;	//check every 4 ticks (remember its running at 20 ticks per second)
 	public Buffer buffer = new Buffer(100);
 	
 	public Loader(int x, int y, int width, int height)
 	{
 		super(x, y, width, height);
 //		fgcolor = new Color(random(0,255), random(0, 255), random(0,255));
 	}
 	
 	public Loader(int xPos, int yPos, int xSize, int ySize, boolean bool)
 	{
 		super(xPos, yPos, xSize, ySize, bool);
 	}
 	
 	public void setMax(double val)
 	{
 		maxVal = val;
 	}
 	
 	public void setCurrent(double val)
 	{
 		currentVal = val;
 	}
 	
	public void setBufferSize(int size)
	{
		buffer.setBufferSize(size);
	}
	
 	public static double round(double num, int places)
 	{
 		return (1.0*(int)(num*Math.pow(10, places)))/Math.pow(10, places);
 	}
 	
 	//convert seconds to an english string
 	public static String getTimeFromSeconds(double seconds)
 	{
 //		//Example:
 //		String str = Loader.getTimeFromSeconds(1*365*24*60*60 + 2*30*24*60*60 + 3*24*60*60 + 4*60*60 + 5*60 + 6.7);
 //		System.out.println(str);	//"1 year 2 months 3 days 4 hours 5 minutes 6.7 seconds "
 		
		if(seconds > 68*365*24*60*60)
			return "A lot of time";
		
 		int year = 0, month = 0, day = 0, hour = 0, min = 0;
 		double sec = 0;
 		sec = seconds;
 		
 		min = (int)sec / 60;
 		sec = sec % 60;
 		sec = round(sec,1);
 		
 		hour = min / 60;
 		min = min % 60;
 		
 		day = hour / 24;
 		hour = hour % 24;
 		
 		year = day / 365;
 		month = day / 30;
 		
 		day = day % 365;
 		day = day % 30;
 		
 		month = month % 12;
 		
 		String str = "";
 		if(year > 0)
 			str += year+" years ";
 		if(year == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		if(month > 0)
 			str += month+" months ";
 		if(month == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		if(day > 0)
 			str += day+" days ";
 		if(day == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		if(hour > 0)
 			str += hour+" hours ";
 		if(hour == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		if(min > 0)
 			str += min+" minutes ";
 		if(min == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		if(sec > 0)
 			str += sec+" seconds ";
 		if(sec == 1)
 			str = str.substring(0,str.length()-2)+" ";
 		
 		if(year == 0 && month == 0 && day == 0 && hour == 0 && min == 0 && sec == 0)
 			str = "0 seconds ";
 		
 //		System.out.println("months: "+month+" days: "+day+" hours: "+hour+" minutes: "+min+" seconds: "+sec);
 		
 		return str.trim();
 	}
 	
 	public void tick()
 	{
 		super.tick();
 		
		if(test)
		{
			currentVal++;
			if(currentVal > maxVal)
				currentVal = maxVal;
			else if(currentVal < 0)
				currentVal = 0;
		}
 		
 		if(pressed1)
 		{
 			currentVal = 0;
 			buffer.clear();
 		}
 		
 		currentTime++;
 		if(currentTime >= checkTime && currentVal < maxVal)
 		{
 			buffer.addVal(currentVal);
 			currentTime = 0;
 		}
 		if(currentTime > checkTime)
 			currentTime = 0;
 			
 	}
 	
 	public void render(Graphics g)
 	{
 		//background color
 		g.setColor(bgcolor);
 		g.fillRect((int)x, (int)y, width, height);
 		
 		//bar color
 		g.setColor(fgcolor);
 		g.fillRect((int)x, (int)y, (int)((currentVal/maxVal)*width), height);
 		
 		//fancy trim
 		g.setColor(Button.changeColor(fgcolor, 20));
 		g.fillRect((int)x, (int)y, (int)((currentVal/maxVal)*width), height/12);
 		g.setColor(Button.changeColor(fgcolor, -20));
 		g.fillRect((int)x, (int)y+height-height/12, (int)((currentVal/maxVal)*width), height/12);
 		
 		//current percentage
 //		String str = round(100*currentVal/maxVal, 0)+" %";
 		//completion ETA
 		double secondsLeft = buffer.getNStepsToVal(maxVal)/((1000/Main.tickTime) / checkTime);
 		String str = getTimeFromSeconds(secondsLeft)+" remaining";
 		
 		
 		Font f = new Font("Verdana", Font.PLAIN, 24/Main.pixelSize);
 		g.setFont(f);
 		FontMetrics fm = g.getFontMetrics(f);
 		Rectangle2D rect = fm.getStringBounds(str, g);
 		int xCenter = (int)x + (width/2);
 		int yCenter = (int)y + (height/2);
 		int sX = xCenter - (int)(rect.getWidth()/2);
 		int sY = yCenter - (int)(rect.getHeight()/2) + fm.getAscent();
 		
 		//put a box behind the percentage
 		g.setColor(new Color(200, 200, 200, 200));
 		g.fillRect(sX, sY-fm.getAscent(), (int)rect.getWidth(), (int)rect.getHeight());
 		
 		g.setColor(Color.black);
 //		g.setColor(new Color(255-bgcolor.getRed(), 255-bgcolor.getGreen(), 255-bgcolor.getBlue()));
 		g.drawString(str, sX, sY);
 		
 		//pass to lower menus
 		for(int i = 0; i < menus.size(); i++)
 			menus.get(i).render(g);
 	}
 	
 	public String toString()
 	{
 		return "Loader("+xPos+", "+yPos+")";
 	}
 }
