 package spaceshooters.gfx;
 
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class TextureManager {
 	
 	private int rowSize = 128;
 	private int columnSize = 128;
 	private TextureAtlas entityAtlas;
 	private TextureAtlas effectAtlas;
 	
 	private static TextureManager instance = new TextureManager();
 	
 	private TextureManager() {
 	}
 	
 	public void init() {
 		try {
 			entityAtlas = this.createTextureAtlas("(?i)textures/.+/entities/.+\\.png", "Entity Atlas");
 			effectAtlas = this.createTextureAtlas("(?i)textures/.+/effects/.+\\.png", "Effect Atlas");
 		} catch (IOException | SlickException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private TextureAtlas createTextureAtlas(String regex, String atlasName) throws IOException, SlickException {
 		// Step #1 -> Find all PNGs to stitch in the classpath.
 		ArrayList<String> list = this.findImages(regex);
 		
 		// Step #2 -> Create images containing all found images - TextureAtlases
 		if (list != null && list.size() > 0) {
 			Object[] stitched = this.stitch(list);
 			return new TextureAtlas(atlasName, (Image) stitched[0], (HashMap<String, Integer[]>) stitched[1]);
 		} else {
 			throw new IOException("The list of images to stitch cannot be null!");
 		}
 	}
 	
 	private ArrayList<String> findImages(String regex) throws IOException {
 		ArrayList<String> list = new ArrayList<>();
 		for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
 			String filePath = url.toString().substring(url.toString().indexOf("/") + 1).replace("%20", " ");
 			if (filePath.endsWith(".jar") || filePath.endsWith(".zip")) {
 				ZipFile zip = new ZipFile(filePath);
 				Enumeration<? extends ZipEntry> entries = zip.entries();
 				// Handle textures
 				while (entries.hasMoreElements()) {
 					ZipEntry entry = entries.nextElement();
 					if (entry.getName().matches(regex)) {
 						list.add(entry.getName());
 					}
 				}
 				zip.close();
 			}
 		}
 		
 		return list;
 	}
 	
 	private Object[] stitch(ArrayList<String> paths) throws SlickException {
 		HashMap<String, Integer[]> imageMap = new HashMap<>();
 		Image stitchedImage = Image.createOffscreenImage(128, 128);
 		int x = 0, y = 0;
 		
 		for (String path : paths) {
 			Graphics gfx = stitchedImage.getGraphics();
 			Image i = new Image(path, new Image(path).getTexture().hasAlpha() ? null : Color.transparent);
 			String[] splitted = path.split("/");
 			String name = (splitted[1].equalsIgnoreCase("vanilla") ? "" : splitted[1] + ":") + path.substring(path.lastIndexOf("/") + 1); // Remove the path and leave only the file name.
 			int imageWidth = i.getWidth();
 			int imageHeight = i.getHeight();
 			
 			if (imageWidth > rowSize || imageHeight > columnSize) {
 				throw new RuntimeException("Image too big: " + path + ", " + imageWidth + ", " + imageHeight);
 			}
 			
 			// Actual drawing of image to the stitched one.
 			gfx.drawImage(i, x, y);
			gfx.flush();
 			
 			imageMap.put(name, new Integer[] { x, y, imageWidth, imageHeight }); // Put the image to the map.
 			
 			// Drawing logic.
 			x += imageWidth;
 			if (x >= rowSize - imageWidth) {
 				y += imageHeight;
 				x = 0;
 			}
 			
 			if (y >= columnSize - imageHeight) {
 				columnSize += rowSize; // Add more slots.
 				stitchedImage = this.resizeImage(stitchedImage, rowSize, columnSize); // Resize.
 			}
 		}
 		
 		stitchedImage.getGraphics().destroy();
 		return new Object[] { stitchedImage, imageMap };
 	}
 	
 	private Image resizeImage(Image image, int width, int height) throws SlickException {
 		Image img = Image.createOffscreenImage(width, height);
 		Graphics graphics = img.getGraphics();
 		graphics.drawImage(image, 0, 0);
 		graphics.flush();
 		return img;
 	}
 	
 	public Image getTexture(String textureName) {
 		return entityAtlas.getTexture(textureName) != null ? entityAtlas.getTexture(textureName) : effectAtlas.getTexture(textureName) != null ? effectAtlas.getTexture(textureName) : null;
 	}
 	
 	public static TextureManager getTextureManager() {
 		return instance;
 	}
 }
