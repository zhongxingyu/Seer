 package bots;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 public class Map {
 	private static final int TILE_SIZE = 10;
 	private final int width;
 	private final int height;
 	private final List<List<Ground>> tiles;
 	private final BufferedImage offscreen;
 
 	public Map(String name) throws IOException {
 		URL url = this.getClass().getResource("/resources/maps/"+name);
 		assert (url != null);
 		final BufferedImage map_img = (BufferedImage) new ImageIcon(ImageIO.read(url)).getImage();
 
 		width = map_img.getWidth() * TILE_SIZE;
 		height = map_img.getHeight() * TILE_SIZE;
 		tiles = new ArrayList<List<Ground>>(height);
 		offscreen = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 
 		// TODO paint a nicer map!
 		for (int img_x = 0; img_x < map_img.getWidth(); img_x++) {
 			List<Ground> row = new ArrayList<Ground>(width);
 			for (int img_y = 0; img_y < map_img.getWidth(); img_y++) {
 				int pixel = map_img.getRGB(img_y, img_x);
 				row.add(img_y, getGroundByColor(pixel));
 
 				/* paint offscreen image */
 				for (int i = 0; i < TILE_SIZE; i++) {
 					for (int j = 0; j < TILE_SIZE; j++) {
 						offscreen.setRGB(img_x*TILE_SIZE+i, img_y*TILE_SIZE+j, pixel);
 					}
 				}
 			}
 			tiles.add(row);
 		}
 	}
 
 	private Ground getGroundByColor(int pixel) throws IOException {
 		switch (pixel) {
 		case 0xff3a9d3a:
 			return Ground.Grass;
 		case 0xff375954:
 			return Ground.Swamp;
 		case 0xffe8d35e:
 			return Ground.Sand;
 		case 0xff323f05:
 			return Ground.Forest;
 		case 0xff0000ff: /* BLUE */
 			return Ground.Grass;
 		case 0xff787878:
 			return Ground.Rocks;
 		case 0xffffffff: /* WHITE */
 			return Ground.Grass;
 		case 0xffff0000: /* RED */
 			return Ground.Grass;
 		case 0xff3674db:
 			return Ground.Water;
 		default:
 			throw new IOException("Map broken. Unknown color: "+Integer.toHexString(pixel));
 		}
 	}
 
 	public Ground get(final int x, final int y) {
 		if (x < 0 || y < 0 || x > width || y > height) {
 			return Ground.Void;
 		}
 
 		final int tileX = x / TILE_SIZE;
 		final int tileY = y / TILE_SIZE;
 
		return tiles.get(tileX).get(tileY);
 	}
 
 	public void paint(Graphics g, Component observer) {
 		g.drawImage(offscreen, 0, 0, observer);
 	}
 
 	public Image cloneImage() {
 		BufferedImage img = new BufferedImage(offscreen.getWidth(), offscreen.getHeight(), offscreen.getType());
 		Graphics g = img.getGraphics();
 		g.drawImage(offscreen, 0, 0, null);
 		return img;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 }
