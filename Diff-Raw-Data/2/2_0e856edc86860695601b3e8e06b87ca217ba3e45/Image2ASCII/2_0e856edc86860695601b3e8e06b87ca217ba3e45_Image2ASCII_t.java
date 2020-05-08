 package pl.youcode.asciigenerator;
 
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 public class Image2ASCII
 {
 
 	private BufferedImage input_image;
 	
 	private BufferedImage output_image;
 	
 	private String output = "";
 	
 	public Image2ASCII(File image) throws IOException
 	{
 		this.input_image = ImageIO.read(image);
 		ColorSpace greyscale = ColorSpace.getInstance(ColorSpace.CS_GRAY);
 		ColorConvertOp cco = new ColorConvertOp(greyscale, null);
 		this.output_image = cco.filter(this.input_image, null);
 		this.input_image = null; // We destroy input image because it isn't need anymore. 
 	}
 	
 	public void save() throws IOException { this.save("result.txt"); }
 	
 	public void save(String filename) throws IOException
 	{
 		System.out.println(String.format("Saving ASCII into file: %s", filename));
 		if(this.output.isEmpty())
 		{
 			this.generateASCII();
 		}
 		FileWriter fw = new FileWriter(filename);
 		BufferedWriter out = new BufferedWriter(fw);
 		out.write(this.output);
 		out.close();
 		System.out.println("Saved!");
 	}
 	
 	private void generateASCII()
 	{
 		System.out.println("Converting image into ASCII...");
 		this.output = "";
 		int width = this.output_image.getWidth();
 		int height = this.output_image.getHeight();
 		int last_percent = 0;
 		for(int i = 0; i < height; ++i)
 		{
 			for(int j = 0; j < width; ++j)
 			{
 				String character = " ";
 				int pixel = this.output_image.getRGB(j, i);
 				int red = (pixel >> 16) & 0xff;
 				int green = (pixel >> 8) & 0xff;
 				int blue = (pixel) & 0xff;
 				int grey = (int)(0.299 * red + 0.587 * green + 0.114 * blue);
 				//System.out.println(String.format("Pixel at %d, %d = %d", j, i, grey));
 				if(grey <= 100)
 				{
 					character = "+";
 				}
 				else if(grey > 100 && grey <= 150)
 				{
 					character = "@";
 				}
 				else if(grey > 150 && grey <= 240)
 				{
 					character = "*";
 				}
 				this.output += character;
 			}
			this.output += "\n";
 			int percent = (i / height) * 100;
 			if(percent != last_percent)
 			{
 				last_percent = percent;
 				System.out.println(String.format("Progress: %d%%", percent));
 			}
 		}
 		System.out.println("Image converted into ASCII!");
 	}
 
 }
