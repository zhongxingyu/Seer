 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Vector;
 
 
 public class Painter_multi implements Painter{
 	private Vector<Word> words; // keywords to paint
 	private final static String fontfile = "res/font.ttf"; // Font
 	private BufferedImage img;
 	Graphics2D g;
     
 	private static Point p_cen;
 	private static final int max_num = 150;
 	private static final int font_min = 18;
 	private static final int font_max = 125;
 	private Bound bound;
 	private Shape bound_shape;
 	private String color_style;
 	Font font;
 	Font fontBase;
 
 
 	private final int height; // height of the picture
 	private final int width; // width of the picture	
 	private final boolean update; 
 	private boolean is_rotate;
 	private ImageObserver observer;
 	private boolean done;
 	
 	
 	public Painter_multi(Vector<Word> result, int width, int height, boolean update, ImageObserver observer) 
 	{
 		this.width = width;
 		this.height = height;
 		this.update = update;
 		this.observer = observer;
 	    done = false;
 		img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		p_cen = new Point(width / 2, height / 2);
 		color_style="warm";	
 
 		//set shape
 		bound=new Bound("rect", width, height);
 		bound_shape=bound.get_shape();
 		
 		words = result;
 		g = img.createGraphics();
 		g.fillRect(0, 0, width, height); // Fill the picture with white
 		try 
 		{
 			InputStream myStream = new BufferedInputStream(new FileInputStream(fontfile));
 			fontBase = Font.createFont(Font.TRUETYPE_FONT, myStream);
 		}
 		catch (Exception ex)
 		{
 	        ex.printStackTrace();
 	    }
 	}
 
 	public String paint()
 	{
 		if (done) repaint();
 		g.fillRect(0, 0, width, height); // Fill the picture with white
 		long startTime = new Date().getTime();
 		if (words.size() == 0)
 		{
 			return "No Keywords Found!";
 		}
 
 		int drawn = 0;
 		setSize(); // Reset make is the size of the font
 
 		if (update) observer.imageUpdate(img, ImageObserver.ALLBITS, 0, 0, width, height);
 		
 		for (int i = 0; i < max_num && i < words.size(); i++)
 		{
 			if (paintStr(i, i > 5 ? 2 : 0) == 0) break;
 			drawn ++;
 			System.out.println((i + 1 ) + " / " + words.size() + " done. Size: " + words.get(i).getSize());
 		}		
 		System.out.println("Paint Successful!");   
 		long endTime = new Date().getTime();
 		done = true;
 		return drawn  + " drawn. \nTime used: " + (endTime - startTime) / 1000 + "." + (endTime - startTime) % 1000 + " s.";
 	}
 	
 	private void repaint() 
 	{
 		for (int i = 0; i < words.size(); i++)
 		{
 			if (words.get(i).X() == -1) continue;		
 			try   // Set the font
 			{
 				font = fontBase.deriveFont(Font.PLAIN, words.get(i).getSize());
 			} 
 			catch (Exception ex)
 			{
 				ex.printStackTrace();
 			}
 			g.setFont(font);
 			g.drawString(words.get(i).getStr(), words.get(i).X(), words.get(i).Y());
 		}		
 		observer.imageUpdate(img, ImageObserver.ALLBITS, 0, 0, width, height);
 	}
 
 	public BufferedImage getImg()
 	{
 		return img;
 	}
 	
 	//according to the frequency of word determine the size of font.
 	private void setSize() 
 	{
 		int sum = 0; // The sum of all the keywords found
 		for (int i = 0; i < words.size(); i++) sum += words.get(i).N();
 		for (int i = 0; i < words.size(); i++) 
 		{
 			int temp = (int)words.get(i).N() * 130 / sum + 125 - i / 10 * 30; // Function to determine the font size
 			if (temp < font_min) temp = font_min; // Minimum size 
 			else if (temp > font_max) temp = font_max; // Maximum size
 			words.get(i).setSize(temp);
		}
		
 	}
 	
 	
 	private int paintStr(int i, int sides)
 	{
 		
 		// Try to find an empty space of the string
 		Point position = null;	
 		Rectangle  bounds = null;
 		Shape draw_word = null;
 		AffineTransform tx = null;
 		boolean found = false;
 		Point[] str_vertex = new Point[4];
 		while (!found)
 		{
 			// Set the font
 			try 
 			{
 				font = fontBase.deriveFont(Font.PLAIN, words.get(i).getSize());
 			} 
 			catch (Exception ex)
 			{
 				ex.printStackTrace();
 			}
 			g.setFont(font);
 			TextShape textshape = new TextShape(font,words.get(i).getStr());
 			draw_word=textshape.getShape();
 			tx = new AffineTransform();
 			bounds = draw_word.getBounds();
 			str_vertex[0] = new Point(bounds.x, bounds.y);
 			str_vertex[1] = new Point(bounds.x, bounds.y + bounds.height);
 			str_vertex[2] = new Point(bounds.x + bounds.width, bounds.y + bounds.height);
 			str_vertex[3] = new Point(bounds.x + bounds.width, bounds.y);
 			
 			if(Math.random()>0.5) {
 				is_rotate=false;
 			} else {
 				is_rotate=true;
 			}
 			if(is_rotate) {	
 				AffineTransform ax = new AffineTransform();
 				ax.rotate(Math.PI/4*Math.random() ,0,0);
 				ax.transform(str_vertex, 0, str_vertex, 0, 4);
 				draw_word=ax.createTransformedShape(draw_word);
 			}
 			// Get the bounds of the string
 			bounds = draw_word.getBounds();
 			for (int j = sides; j > -1; j--)
 			{
 				//rotate it 4 times to fit space
 				for(int k = 0; k < 4; k++) {
 					position = searchSpace(bounds, j, i, words.get(i).P());
 					if (position != null)
 					{
 						found = true;
 						j = -1;
 						break;
 					} else {
 						AffineTransform ax = new AffineTransform();
 						ax.rotate(Math.PI/4*Math.random() ,0,0);
 						ax.transform(str_vertex, 0, str_vertex, 0, 4);
 						draw_word=ax.createTransformedShape(draw_word);
 						bounds = draw_word.getBounds();
 					}
 				}
 			}
 			
 			if (!found) // no space, try to make the word smaller
 			{
 				words.get(i).setSize(words.get(i).getSize() - 10);
 				if (words.get(i).getSize() < font_min) // too small. no space available on the canvas
 				{
 					System.out.println("Warning! No space available!");
 					return 0;		
 				}
 					
 			}
 		}
 		
 		setColor(position,words.get(i));// Set the color of the string which is related to its position
 		// Draw the string
 		int x = (int) (position.x - bounds.getMinX());
 		int y = (int) (position.y - bounds.getMinY());
 		
 		tx.setToTranslation(x, y);
 		tx.transform(str_vertex, 0, str_vertex, 0, 4);
 		draw_word=tx.createTransformedShape(draw_word);
 		g.fill(draw_word);
 		words.get(i).setPoint(x, y);
 		int[] str_x = {(int)str_vertex[0].x, (int)str_vertex[1].x, (int)str_vertex[2].x, (int)str_vertex[3].x};
 		int[] str_y = {(int)str_vertex[0].y, (int)str_vertex[1].y, (int)str_vertex[2].y, (int)str_vertex[3].y};
 		words.get(i).setBounds(new Polygon(str_x, str_y, 4));
 		if (update) observer.imageUpdate(img, ImageObserver.ALLBITS, position.x, position.y, bounds.width, bounds.height);
 		return 1;	
 	}
 
 	public int max (int a, int b) {
 		return a > b ? a:b;
 	}
 	
 	private void setColor(Point position, Word word)
 	{
 		double p = word.P();
 		if(color_style == "warm") {
 			System.out.println("warm");
 			g.setColor(new Color(
 					/*red*/ (int)p*255,
 					/*green*/ 0,
 					/*blue*/ (int)(1-p)*255
 					));
 		}
 	}
 			
 
 	private Point searchSpace(Rectangle bounds, int sides, int i, double p)
 	{		
 		// The bounds of the string
 		int str_X = bounds.width;
 		int str_Y = bounds.height;
 		str_Y = bounds.height;
 		int loop=1;
 		int y=p_cen.y-loop;	
 		int x=(int)(p*width-loop);
 		
 		// The starting position of x and y
 		int init_Y = y;
 		int init_X = x;
 		
 		//search in the area of bound.
 		int left_bound=0;
 		int right_bound=0;
 		int up_bound=0;
 		int low_bound=0;
 		
 		do
 		{	
 			if (isEmpty(x-str_X/2, y-str_Y/2, str_X, str_Y, sides, i))
 			{
 				return new Point(x-str_X/2, y-str_Y/2);
 			}
 			left_bound=p_cen.x-loop;
 			right_bound=p_cen.x+loop;
 			low_bound=p_cen.y-loop;
 			up_bound=p_cen.y+loop;
 			//low and up bound cannot out of rectangle area.
 			if(low_bound<=str_Y/2)low_bound=1+str_Y/2;
 			if(up_bound >= height - str_Y / 2) up_bound = height - str_Y / 2;
 			
 			//search in the form of circle
 			if(x<=left_bound)
 			{
 				if(y>low_bound)y=y--;
 				else x++;
 			}
 			else if(x>=right_bound)
 			{ 
 				if(y<up_bound)y=y++;
 				else x--;
 			}
 			else
 			{
 				if(y<=low_bound)
 				{
 					x++;
 				}else if(y>=up_bound)
 				{
 					x--;
 				}else{
 					x++;
 				}
 			}
 			
 			//back to the staring point, add loop and search again
 			if(x<=init_X&&y<=init_Y)
 			{
 				init_X=left_bound-1;
 				init_Y=low_bound-1;
 				if(init_Y<=str_Y/2)init_Y=1+str_Y/2;
 				loop=loop+1;
 			}
 			
 		}	while ((x < width - str_X / 2) || loop < 10);	
 		return null;
 	}	
 	
 	private boolean isEmpty(int x, int y, int str_X, int str_Y, int sides, int n)
 	{
 		if (x + str_X >= width || y + str_Y >= height) return false;
 	
 		for (int l = 0; l < n; l++)
 		{
 			if (words.get(l).getBounds().contains(x, y) || words.get(l).getBounds().contains(x + str_X, y + str_Y))
 			{
 				return false;
 			}
 		}
 		
 		int i = 0;
 		int j = 0;
 		
 		for (j = 0; j < str_X; j += 1)
 			if (!isInShape(x + j, y + j * str_Y / str_X)) return false;	
 
 		for (j = 0; j < str_X; j += 1)
 			if (!isInShape(x + str_X - j, y + j * str_Y / str_X)) return false;		
 		
 		i = str_Y / 2;
 		for (j = 0; j < str_X; j += 1)
 			if (!isInShape(x + j, y + i)) return false;	
 		
 		j = str_X /2;
 		for (i = 0; i < str_Y; i += 1)
 			if (!isInShape(x + j, y + i)) return false;		
 		
 		i = 0;
 		for (j = 0; j < str_X; j += 1)
 			if (!isInShape(x + j, y + i)) return false;
 		
 		i = str_Y;
 		for (j = 0; j < str_X; j += 1)
 			if (!isInShape(x + j, y + i)) return false;	
 		
 		j = 0;
 		for (i = 0; i < str_Y; i += 1)
 			if (!isInShape(x + j, y + i)) return false;
 		
 		j = str_X;
 		for (i = 0; i < str_Y; i += 1)
 			if (!isInShape(x + j, y + i)) return false;		
 		
 		//		g.drawLine(x, y, x+str_X, y);
 //		g.drawLine(x, y, x, y+str_Y);
 //		g.drawLine(x+str_X, y, x+str_X, y+str_Y);
 //		g.drawLine(x+str_X, y+str_Y, x, y+str_Y);
 		// not too far away from other words
 		
 		//if is the first word;
 		if (sides == 0)
 		{
 			return true;
 		}
 		x -= 2;
 		y -= 2;
 		str_X += 4;
 		str_Y += 4;
 		i = 0;
 		int count = 0;
 		for (j = 0; j < str_X; j += 1)
 			if (isNearWord(x + j, y + i)) 
 			{
 			count ++;
 			if (count == sides)
 			{
 //				g.drawOval(x+j,y+i, 3, 3);
 				return true;
 			}
 			break;
 			}
 		
 		i = str_Y;
 		for (j = 0; j < str_X; j += 1)
 			if (isNearWord(x + j, y + i)) 
 			{
 			count ++;
 			if (count == sides)
 			{
 //				g.drawOval(x+j,y+i, 3, 3);
 				return true;
 			}
 			break;
 			}
 		
 		j = 0;
 		for (i = 0; i < str_Y; i += 1)
 			if (isNearWord(x + j, y + i)) 
 			{
 			count ++;
 			if (count == sides)
 			{
 //				g.drawOval(x+j,y+i, 3, 3);
 				return true;
 			}
 			break;
 			}
 		
 		j = str_X;
 		for (i = 0; i < str_Y; i += 1)
 			if (isNearWord(x + j, y + i)) 
 			{
 			count ++;
 			if (count == sides)
 			{
 //				g.drawOval(x+j,y+i, 3, 3);
 				return true;
 			}
 			break;
 			}
 
 		return false;
 	}
 
 	private boolean isInShape(int a, int b)
 	{
 		if (bound_shape.contains(a, b) && img.getRGB(a, b) == Color.white.getRGB()) 
 		{
 //			g.drawOval((int) d.getX(), (int) d.getY(), 1, 1);
 			return true;
 		} 
 		else 
 		{
 			return false;
 		}
 	}
 	
 	private boolean isNearWord(int a, int b)
 	{
 		if (!bound_shape.contains(a, b))
 		{
 			return false;
 		}
 		if (img.getRGB(a, b) != Color.white.getRGB()) 
 		{
 			return true;
 		} 
 		else 
 		{
 			return false;
 		}
 	}
 
 	public void setBackground(BufferedImage bimg)
 	{
 		g.drawImage(bimg, 0, 0, width, height, 0, 0, bimg.getWidth(), bimg.getHeight(), null);
 		repaint();
 //		for(int i = 0; i < width; i++) 
 //		{
 //			for(int j = 0; j < height; j++) 
 //			{
 //				if(fimg.getRGB(i,j) == Color.white.getRGB())
 //				{
 //					img.setRGB(i, j, bimg.getRGB(i * bimg.getWidth() / width, j * bimg.getHeight() / height));
 //				}
 //				else 
 //				{
 //					img.setRGB(i, j, fimg.getRGB(i, j));
 //				}
 //			}
 //		}
 	}
 
 	public void setColorStyle(String str) {
 		color_style=str;
 		
 	}
 }
 
