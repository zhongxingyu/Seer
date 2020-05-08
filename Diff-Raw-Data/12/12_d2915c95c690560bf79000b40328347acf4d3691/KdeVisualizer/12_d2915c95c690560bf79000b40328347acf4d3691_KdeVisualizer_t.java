 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 
 public final class KdeVisualizer
 {
 	public static File writeToFile(BufferedImage img, String path)
 	{
 	    // retrieve image
 	    File outputfile = new File(path);
 	    try
 		{
 			ImageIO.write(img, "png", outputfile);
 		} catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    
 	    return outputfile;
 	}
 	
 	public static BufferedImage run(String inputFile) throws FileNotFoundException
 	{
 		File f = new File("../FwbAlgorithm/outputscaledfield.fwb");
 		if(!f.exists())
 		{
 			System.out.println("File doesn't exist");
 			return null;
 		}
 
 		FwbParser.Result in = FwbParser.parse(new FileInputStream(f), true);
 		if(in == null || in.densities == null)
 		{
 			System.out.println("Wrong input format");
 			return null;
 		}
 		
 		return run(in);
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static BufferedImage run(KDE kde)
 	{
 		BufferedImage img = new BufferedImage(kde.scaledField.GRID_WIDTH, kde.scaledField.GRID_HEIGHT, BufferedImage.TYPE_INT_RGB);
 		Graphics g = img.createGraphics();
		g.setColor(Color.black);
 		g.fillRect(0, 0, img.getWidth(), img.getHeight());
 	
 		for(int i = 0; i < kde.scaledField.GRID_WIDTH; i++)
 		{
 			for(int j = 0; j < kde.scaledField.GRID_HEIGHT; j++)
 			{
 				Cell cell = kde.scaledField.getCell_scaled(i, j);
 				if(cell == null)
 					continue;
 				
 				g.setColor(densityToColor(cell.getDensity(), kde.maxDens));
 				g.drawLine(i, j, i, j);
 			}
 		}
 		
 		return img;
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static BufferedImage run(FwbParser.Result input)
 	{
 		BufferedImage img = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);
 		Graphics g = img.createGraphics();
		g.setColor(Color.black);
 		g.fillRect(0, 0, img.getWidth(), img.getHeight());
 	
 		for(int i = 0; i < input.width; i++)
 		{
 			for(int j = 0; j < input.height; j++)
 			{
 				g.setColor(densityToColor(input.densities[i][j], input.maxDensity));
 				g.drawLine(i, j, i, j);
 			}
 		}
 		
 		return img;
 	}
 	
 	static Color densityToColor(float density, float maxDensity)
 	{
 		//Kleuring 1:
 		//bottom is green, middle is blue, top is red
 		float rel = Math.max(0, Math.min(density / maxDensity, 1));
 		Color c = Color.BLACK;
 
 //		if(rel <= (1/3f))
 //			c = new Color(0, rel / (1/3f), 0);
 //		else if(rel > (1/3f) && rel <= (2/3f))
 //			c = new Color(0, 0, (rel - (1/3f)) / (1/3f));
 //		else if(rel > (2/3f))
 //			c = new Color((rel - (2/3f)) / (1/3f), 0, 0);
 		
 //		//Kleuring 2:
 //		float h = (1 - rel) * (80/360f) - (20/360f); //red to yellow
 //		if(h < 0)
 //			h = 1 - h;
 //		float s = 1;
 //		float v = 1;//rel == 0 ? 0 : 1;
 //
 //		c = Color.getHSBColor(h, s, v);
 //
 		//Kleuring 3:
 		int r = 0, g = 0,b = 0;
 		if(rel <= (1/2f))
 		{
 			g = 0;
 			r = (int)((rel / 0.5f) * 255);
 			b = 0;
 		}
 		else
 		{
 			g = (int)(((rel - 0.5f) / 0.5f) * 255);
 			r = 255;
 			b = 0;
 		}
 		
 		r = Math.max(0, Math.min(r, 255));
 		g = Math.max(0, Math.min(g, 255));
 		
 		c = new Color(r, g, b);
 		
 		return c;
 	}
 }
