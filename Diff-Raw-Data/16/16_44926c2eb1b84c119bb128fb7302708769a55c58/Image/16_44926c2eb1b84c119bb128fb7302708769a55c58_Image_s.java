 package graphics;
 
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 
 public class Image {
 	public int[] pixels;
 	public int h;
 	public int w;
 	public BufferedImage image;
 
 	public Image(int w, int h) {
		GraphicsEnvironment env = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice device = env.getDefaultScreenDevice();
		GraphicsConfiguration config = device.getDefaultConfiguration();
		this.image = config.createCompatibleImage(w, h,
				Transparency.TRANSLUCENT);
 		this.w = w;
 		this.h = h;
		this.pixels = ((DataBufferInt) this.image.getRaster().getDataBuffer())
				.getData();
 	}
 }
