 import java.awt.Image;
 import java.awt.Transparency;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.ComponentColorModel;
 import java.awt.image.DataBuffer;
 import java.awt.image.DataBufferByte;
 import java.awt.image.DataBufferUShort;
 import java.awt.image.Raster;
 import java.awt.image.SampleModel;
 import java.awt.image.WritableRaster;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 public class WordCropper
 {
 	public static void main(String[] args)
 	{
 		System.out.println("Working Directory = " + System.getProperty("user.dir"));
 		String cwd = System.getProperty("user.dir");
		File actual = new File(cwd + File.separator + "image.png");
 	
 		try
 		{
 			BufferedImage BI = ImageIO.read(actual);
 			int[][] data = convertTo2DWithoutUsingGetRGB(BI);
 			final int height = data.length;
 			final int width = data[0].length;
 			int[][] used = new int[height][width];
 			int i,j,x,y;
 			Queue<Point> q = new LinkedList<Point>();
 			int maxX, maxY,minX, minY, ind=0;
 			for(j=0;j<height;j++)
 				for(i=0;i<width;i++)
 				if(used[j][i] == 0)
 				{
 					x = i;
 					y = j;
 					maxX = minX = x;
 					maxY = minY = y;
 					Point p = new Point(x, y);
 					q.add(p);
 					while(!q.isEmpty())
 					{
 						p = q.poll();
 						if(p.x < minX){ minX = p.x; }
 						if(p.y < minY){ minY = p.y; }
 						if(p.x > maxX){ maxX = p.x; }
 						if(p.y > maxY){ maxY = p.y; }
 						used[p.y][p.x] = 1;
 						if(p.y < height-1 && data[p.y+1][p.x] == 0 && used[p.y+1][p.x] == 0)
 						{
 							q.add(new Point(p.x, p.y+1));
 							used[p.y-1][p.x] = 1;
 						}
 						if( p.y > 0 && data[p.y-1][p.x] == 0 && used[p.y-1][p.x] == 0)
 						{
 							q.add(new Point(p.x, p.y-1));
 							used[p.y-1][p.x] = 1;
 						}
 						if(p.x < width-1 && data[p.y][p.x+1] == 0 && used[p.y][p.x+1] == 0)
 						{
 							q.add(new Point(p.x+1, p.y));
 							used[p.y][p.x+1] = 1;
 						}
 						if(p.x > 0 && data[p.y][p.x-1] == 0 && used[p.y][p.x-1] == 0)
 						{
 							q.add(new Point(p.x-1, p.y));
 							used[p.y][p.x-1] = 1;
 						}
 					}
 					
 					int[][] new_data = copy_rectangle(minY, maxY+1, minX, maxX+1, data);
 					if(new_data == null) continue;
 					File outputImageFile = new File(cwd + File.separator + "res"+minY+"x"+minX+".png");
 					BufferedImage res = getGrayscale(new_data);
 					Image resized = res.getScaledInstance(29, 29, Image.SCALE_SMOOTH);
 					
 					BufferedImage resizedBI= new BufferedImage(29,29, BufferedImage.TYPE_BYTE_GRAY);
 					resizedBI.getGraphics().drawImage(resized, 0, 0, null);
 					ImageIO.write(resizedBI, "png", outputImageFile);
 					ind++;
 					
 				}
 			
 			
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public static BufferedImage getGrayscale(int[][] bytes)
 	{
 		final int width = bytes[0].length;
 		final int height = bytes.length;
 		byte[] pixels = new byte[width * height];
 		int i,j;
 		for (j = 0; j < height; j++)
 			for(i = 0; i < width; i++)
 			{
 				pixels[j * width + i] = (byte)bytes[j][i];
 			}
 		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
 		int[] nBits = { 8 };
 		ColorModel cm = new ComponentColorModel(cs, nBits, false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
 		SampleModel sm = cm.createCompatibleSampleModel(width, height);
 		DataBufferByte db = new DataBufferByte(pixels, width * height);
 		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
 		BufferedImage result = new BufferedImage(cm, raster, false, null);
 		
 		return result;
 	}
 
 	private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image)
 	{
 		//byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
 		final int width = image.getWidth();
 		final int height = image.getHeight();
 		int[] pixels = new int[width*height];
 		image.getRGB(0, 0, width, height, pixels, 0, width);
 
 		int[][] result = new int[height][width];
 		int red, green, blue, pixel, row, col;
 		for (pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel ++)
 		{
 			red 	= (pixels[pixel] & 0xFF);
 			green 	= (pixels[pixel] & 0xFF00)>>8;
 			blue 	= (pixels[pixel] & 0xFF0000)>>16;
 //			result[row][col] = green;
 			result[row][col] = (red + green + blue)/3;
 //			result[row][col] = Math.round(( 0.299 * red + 0.587 * green +  0.114 * blue));
 			col++;
 			if (col == width)
 			{
 				col = 0;
 				row++;
 				if(row >= height) 
 				{
 					return result;
 				}
 			}		
 		}
 		return result;
 	}
 	
 	public static int[][] copy_rectangle(int y1, int y2, int x1, int x2, int[][] data)
 	{
 		
 		int i,j,f;
 		
 		for(i=y1; i<y2; i++)
 		{
 			f = 0;
 			for(j=x1; j<x2; j++)f = f + (data[i][j] == 0 ? 1 : 0);
 			if(f == 0){ y1++; }
 			else break;
 		}
 		for(i=y2-1; i>=y1; i--)
 		{
 			f = 0;
 			for(j=x1; j<x2; j++) f = f + (data[i][j] == 0 ? 1 : 0);
 			if(f == 0){ y2--; }
 			else break;
 		}
 		
 		
 		if(y2-y1 < 10) return null;
 		if(x2-x1 < 10) return null;
 		int[][] res = new int[y2-y1][x2-x1];
 		for(i=y1; i<y2; i++)
 		{
 			for(j=x1; j<x2; j++)
 			{
 				res[i-y1][j-x1]=data[i][j];
 			}
 		}
 		return res;
 	}
 	
 }
 class Point
 {
 	public int x,y;
 	Point(int x, int y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 	
 	public String toString()
 	{
 		return x + " " + y;
 	}
 }
