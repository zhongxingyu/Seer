 
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
 
		if (data < 0.0f) { data = 0.0f; }

		float temp = (float) 0xff * data / 100.0f;
		byte b = (byte) temp;
		int color = (int) b + (int) b * 0x100 + (int) b * 0x10000;
 
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
