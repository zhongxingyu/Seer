 
 import java.io.*;
 import javax.imageio.ImageIO;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 public class HeightMapPlotter {
 
 	private HeightMap mHeightMap;
 
 	public HeightMapPlotter(HeightMap map) {
 
 		mHeightMap = map;
 	}
 
 	public void PlotPoint(Point p, float data, BufferedImage image) {
 
		int color = 0xffffff;

		if (data < 30.0f) {
			// water!
			color = 0x0044ff; // * ((data + 70.0f) / 100.0f);
		} else {
			float temp = 256.0f * data / 100.0f;
			int b = (int) temp;
			color = b + b*0x100 + b*0x100000;
		}
 
 		image.setRGB(p.x, p.y, color);
 	}
 
 	public BufferedImage GetBufferedImage() {
 
 		BufferedImage retval = new BufferedImage(
 				mHeightMap.GetWidth(),
 				mHeightMap.GetHeight(),
 				BufferedImage.TYPE_INT_RGB
 				);
 
 		Point p = new Point();
 		for (int y = 0; y < mHeightMap.GetHeight(); y++) {
 			for (int x = 0; x < mHeightMap.GetWidth(); x++) {
 
 				p.x = x;
 				p.y = y;
 				float data = mHeightMap.GetData(p);
 
 				PlotPoint(p, data, retval);
 			}
 		}
 
 		return retval;
 	}
 
 	public static void main(String[] args) {
 
 		HeightMap map = new HeightMap(2047, 2047);
 		map.Generate();
 
 		HeightMapPlotter plotter = new HeightMapPlotter(map);
 		BufferedImage image = plotter.GetBufferedImage();
 
 		try {
 			File output = new File("heightmap.png");
 			ImageIO.write(image, "png", output);
 		} catch (IOException e) {
 			System.err.print(e);
 		}
 	}
 }
