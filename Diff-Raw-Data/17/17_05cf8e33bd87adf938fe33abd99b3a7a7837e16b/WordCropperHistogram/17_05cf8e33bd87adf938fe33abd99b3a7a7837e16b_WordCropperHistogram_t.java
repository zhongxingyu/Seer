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
 import java.util.Vector;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 
 public class WordCropperHistogram
 {
 	public static void main(String[] args)
 	{
 		System.out.println("Working Directory = " + System.getProperty("user.dir"));
 		String cwd = System.getProperty("user.dir");
		File actual = new File(cwd + File.separator + "image.jpg");
 	
 		try
 		{
 			BufferedImage BI = ImageIO.read(actual);
 			int[][] data = convertTo2DWithoutUsingGetRGB(BI);
 			final int height = data.length;
 			final int width = data[0].length;
 			
 			double histogram_height[] = new double[height];
 			int i,j;
 			for(j=0; j<height; j++)
 			{
 				for(i=0; i<width; i++)
 				{
 					if(data[j][i] == 0)
 					{
 						histogram_height[j] += 1;
 					}
 				}
 			}
 			Vector<Integer> height_points = new Vector<Integer>();
 			boolean zero_mode;
 			
 			
 			if(histogram_height[0] > 0) height_points.add(0);
 			zero_mode = histogram_height[0] == 0; 
 			for(i=1; i<height-1; i++)
 			{
 				if(histogram_height[i] == 0)
 				{
 					if(!zero_mode)
 					{
 						height_points.add(i);
 					}
 					zero_mode = true;
 				}
 				else
 				{
 					if(zero_mode)
 					{
 						height_points.add(i);
 					}
 					zero_mode = false;
 				}
 			}
 			if(histogram_height[height-1] > 0) height_points.add(height-1);
 			
 			int ind = 0,h;
 			int height_start, height_end;
 			for(h=0; h+1<height_points.size(); h+=2)
 			{
 				Vector<Integer> width_points = new Vector<Integer>();
 				
 				double histogram_width[] = new double[width];
 				
 				height_start = height_points.elementAt(h);
 				height_end	 = height_points.elementAt(h+1);
				for(i=0; i<width; i++)
 				{
					for(j=height_start; j<height_end; j++)
 					{
 						if(data[j][i] == 0)
 						{
 							histogram_width[i] += 1;
 						}
 					}
					System.out.println(histogram_width[i]);
 				}
 				
 				if(histogram_width[0] > 0) width_points.add(0);
 				zero_mode = histogram_width[0] == 0; 
 				for(i=1; i<width-1; i++)
 				{
 					if(histogram_width[i] == 0)
 					{
 						if(!zero_mode)
 						{
 							width_points.add(i);
 						}
 						zero_mode = true;
 					}
 					else
 					{
 						if(zero_mode)
 						{
 							width_points.add(i);
 						}
 						zero_mode = false;
 					}
 				}
 				if(histogram_width[width-1] > 0) width_points.add(width-1);
 		
 				for(i=0; i+1<width_points.size(); i+=2)
 				{
 					int[][] new_data = copy_rectangle(height_start, height_end, width_points.elementAt(i), width_points.elementAt(i+1), data);
 					if(new_data == null) continue;
 					File outputImageFile = new File(cwd + File.separator + "res"+ind+".png");
 					BufferedImage res = getGrayscale(new_data);
 					Image resized = res.getScaledInstance(29, 29, Image.SCALE_SMOOTH);
 					
 					BufferedImage resizedBI= new BufferedImage(29,29, BufferedImage.TYPE_BYTE_GRAY);
 					resizedBI.getGraphics().drawImage(resized, 0, 0, null);
 					ImageIO.write(resizedBI, "png", outputImageFile);
 					ind++;
 				}
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
 		
 		
 		if(y2-y1 == 0) return null;
 		if(x2-x1 == 0) return null;
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
