 import java.awt.Color;
 
 public class main
 {
     public static void main(String[] args)
     {
 	screen s = new screen();
 	for(int x = 0; x < 600; x++)
 	{
 	    for(int y = 0; y < 600; y++)
 	    {
 		int r = (int)((Math.sqrt(Math.pow(x - 300, 2) + Math.pow(y - 300, 2)))/1.655);
 		int g = 2 * 256-(int)((Math.sqrt(Math.pow(x - 100, 2) + Math.pow(y - 100, 2)) + Math.sqrt(Math.pow(x - 500, 2) + Math.pow(y - 500, 2)))/2.0);
 		int b = (int)((Math.sqrt(Math.pow(x - 100, 2) + Math.pow(y - 500, 2)) + Math.sqrt(Math.pow(x - 500, 2) + Math.pow(y - 100, 2)))/2.0);
 		s.setPixel(x, y, new Color(r%256, g%256, b%256));
 	    }
 	}
	s.saveScreen("pic.ppm");
     }
 }
