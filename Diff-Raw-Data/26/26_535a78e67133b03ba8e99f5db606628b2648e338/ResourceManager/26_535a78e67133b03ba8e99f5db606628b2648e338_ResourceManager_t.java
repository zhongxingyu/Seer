 package net.buddat.wplanner.gui.resources;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 import net.buddat.wplanner.WPlanner;
 import net.buddat.wplanner.gui.LoadingFrame;
 import net.buddat.wplanner.util.Constants;
 import net.buddat.wplanner.util.Logger;
 
 public class ResourceManager {
 
 	private HashMap<String, Image> terrainImgList = new HashMap<String, Image>();
 	private ArrayList<String> terrainKeyList;
 	
 	private HashMap<String, Image[]> objectImgList = new HashMap<String, Image[]>();
 	private ArrayList<String> objectKeyList;
 	
 	private HashMap<String, Image> fenceImgList = new HashMap<String, Image>();
 	private ArrayList<String> fenceKeyList;
 	
 	private HashMap<String, ImageIcon> guiImages = new HashMap<String, ImageIcon>();
 	
 	private LoadingFrame loading;
 	/*
 	 * Eclipse thinks it is super cool to load up a new LoadingFrame for each resource it needs (see gui images below)
 	 * when switching to design mode on MainWindow.
 	 * However, it doesn't close the LoadingFrames, so you end up with 20+ windows that you have to close manually.
 	 * 
 	 * Funnily enough, it doesn't actually use any of the resources after it does this.
 	 */
 	private static final boolean DEBUG = false;
 	
 	public enum ImageType {
 		TERRAIN, OBJECT, FENCE
 	};
 
 	public ResourceManager() {
 		if (!DEBUG) loading = new LoadingFrame();
 		
 		loadResources();
 		loadGuiImages();
 		
 		if (!DEBUG) loading.update("complete", 100);
 	}
 	
 	public void loadGuiImages() {
 		if (!DEBUG) loading.update("gui images", 65);
 		
 		guiImages.put(Constants.GUI_PENCIL, new ImageIcon(this.getClass().getResource(Constants.GUI_PENCIL)));
 		guiImages.put(Constants.GUI_BRUSH, new ImageIcon(this.getClass().getResource(Constants.GUI_BRUSH)));
 		guiImages.put(Constants.GUI_LINE, new ImageIcon(this.getClass().getResource(Constants.GUI_LINE)));
 		guiImages.put(Constants.GUI_FILL, new ImageIcon(this.getClass().getResource(Constants.GUI_FILL)));
 		guiImages.put(Constants.GUI_ERASER, new ImageIcon(this.getClass().getResource(Constants.GUI_ERASER)));
 		guiImages.put(Constants.GUI_PICKER, new ImageIcon(this.getClass().getResource(Constants.GUI_PICKER)));
 		
 		if (!DEBUG) loading.setProgress(75);
 		
 		guiImages.put(Constants.GUI_ZOOM_IN, new ImageIcon(this.getClass().getResource(Constants.GUI_ZOOM_IN)));
 		guiImages.put(Constants.GUI_ZOOM_OUT, new ImageIcon(this.getClass().getResource(Constants.GUI_ZOOM_OUT)));
 		
 		guiImages.put(Constants.GUI_LAYER_UP, new ImageIcon(this.getClass().getResource(Constants.GUI_LAYER_UP)));
 		guiImages.put(Constants.GUI_LAYER_DOWN, new ImageIcon(this.getClass().getResource(Constants.GUI_LAYER_DOWN)));
 		
 		if (!DEBUG) loading.setProgress(85);
 		
 		guiImages.put(Constants.GUI_NEW, new ImageIcon(this.getClass().getResource(Constants.GUI_NEW)));
 		guiImages.put(Constants.GUI_OPEN, new ImageIcon(this.getClass().getResource(Constants.GUI_OPEN)));
 		guiImages.put(Constants.GUI_SAVE, new ImageIcon(this.getClass().getResource(Constants.GUI_SAVE)));
 		guiImages.put(Constants.GUI_SAVE_IMAGE, new ImageIcon(this.getClass().getResource(Constants.GUI_SAVE_IMAGE)));
 		
 		if (!DEBUG) loading.setProgress(95);
 		
 		guiImages.put(Constants.GUI_LABEL, new ImageIcon(this.getClass().getResource(Constants.GUI_LABEL)));
 		guiImages.put(Constants.GUI_OVERLAY, new ImageIcon(this.getClass().getResource(Constants.GUI_OVERLAY)));
 		guiImages.put(Constants.GUI_COLOR_CHOOSER, new ImageIcon(this.getClass().getResource(Constants.GUI_COLOR_CHOOSER)));
 		
 		guiImages.put(Constants.GUI_LOGO, new ImageIcon(this.getClass().getResource(Constants.GUI_LOGO)));
 	}
 	
 	public void loadResources() {
 		Logger.log("Loading resources.");
 		
 		int test = 0;
 		
 		String wurmDir = WPlanner.getConfig().getWurmInstallDir();
 		
 		try {
 			if (!DEBUG) loading.update("terrain images", test);
 			/*
 			 * Load Terrain
 			 */
 			JarFile jf = new JarFile(wurmDir + "/packs/graphics.jar");
 			Enumeration<JarEntry> e = jf.entries();
 			JarEntry je;
 			
 			BufferedImage bi = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
 			Graphics g = bi.getGraphics();
 			g.setColor(new Color(0, 0, 0, 0));
 			g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
 			terrainImgList.put("_blank.jpg", bi.getScaledInstance(128, 128, Image.SCALE_SMOOTH));
 			
 			while (e.hasMoreElements())
 				if ((je = e.nextElement()) != null)
 					if (!je.isDirectory() && je.getName().contains("texture/terrain/")) {
						if (!je.getName().endsWith("png") && !je.getName().endsWith("jpg")) 
							continue;
						
 						terrainImgList.put(je.getName().substring(je.getName().lastIndexOf("/") + 1), 
 								ImageIO.read(jf.getInputStream(je)).getScaledInstance(128, 128, Image.SCALE_SMOOTH));
 						if (!DEBUG) loading.setProgress(test++);
 					}
 			
 			jf.close();
 			
 			if (!DEBUG) loading.update("object images", 50);
 			/*
 			 * Load Objects
 			 */
 			File objectsFolder = new File(WPlanner.getConfig().getObjectsDir());
 			File[] objects = objectsFolder.listFiles();
 		
 			BufferedImage objectBlank = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
 			Graphics objectBlankGfx = objectBlank.getGraphics();
 			Image[] objectBlankImg = new Image[1];
 			objectBlankGfx.setColor(new Color(0, 0, 0, 0));
 			objectBlankGfx.fillRect(0, 0, objectBlank.getWidth(), objectBlank.getHeight());
 			objectBlankImg[0] = objectBlank;
 			objectImgList.put("_blank.jpg", objectBlankImg);
 			
 			for (File f : objects)
 				if (f != null)
 					if (f.getPath().endsWith(".png") || f.getPath().endsWith(".jpg") || f.getPath().endsWith(".jpeg")) {
 						Image[] rotatedImages = new Image[8];
 						for (int i = 0; i < 8; i++) {
 							Image img = ImageIO.read(f);
 						
 							rotatedImages[i] = rotateImage(img, 45 * i);
 						}
 						objectImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1), rotatedImages);
 					}
 			
 			if (!DEBUG) loading.update("fence images", 55);
 			/*
 			 * Load Fences
 			 */
 			File fencesFolder = new File(WPlanner.getConfig().getFencesDir());
 			File[] fences = fencesFolder.listFiles();
 			
 			BufferedImage fenceBlank = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
 			Graphics fenceBlankGfx = fenceBlank.getGraphics();
 			fenceBlankGfx.setColor(new Color(0, 0, 0, 0));
 			fenceBlankGfx.fillRect(0, 0, fenceBlank.getWidth(), fenceBlank.getHeight());
 			Image fenceBlankImg = fenceBlank;
 			fenceImgList.put("_blank.jpg", fenceBlankImg);
 			fenceImgList.put("_blank.jpg_1", fenceBlankImg);
 			
 			for (File f : fences)
 				if (f != null)
 					if (f.getPath().endsWith(".png") || f.getPath().endsWith(".jpg") || f.getPath().endsWith(".jpeg")) {
 						fenceImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1), rotateImage(ImageIO.read(f), 0));
 						fenceImgList.put(f.getName().substring(f.getName().lastIndexOf("/") + 1) + "_1", rotateImage(ImageIO.read(f), 90));
 					}
 			
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		
 		if (!DEBUG) loading.update("sorting images", 60);
 		Set<String> st = terrainImgList.keySet();
 		terrainKeyList = new ArrayList<String>(st); 
 		Collections.sort(terrainKeyList);
 		
 		Set<String> so = objectImgList.keySet();
 		objectKeyList = new ArrayList<String>(so);
 		Collections.sort(objectKeyList);
 		
 		Set<String> sf = fenceImgList.keySet();
 		fenceKeyList = new ArrayList<String>(sf);
 		Collections.sort(fenceKeyList);
 
 		Logger.log("Resources loaded successfully!");
 	}
 	
 	private Image rotateImage(Image image, int degrees) {
 		int w = image.getWidth(null);
 		int h = image.getHeight(null);
 		
 	    AffineTransform at = new AffineTransform();
 	    at.rotate(Math.toRadians(degrees), w / 2, h / 2);
 	      
 	    BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 	    Graphics2D  g = (Graphics2D) buffer.getGraphics();
 	    g.drawImage(image, at, null);
 
 	    return buffer;
 	}
 	
 	public ImageIcon getGuiImage(String name) {
 		return guiImages.get(name);
 	}
 	
 	public Image getTerrainImage(int id) {
		if (terrainKeyList.size() > id)
			return terrainImgList.get(terrainKeyList.get(id));
		else
			return terrainImgList.get(terrainKeyList.get(0));
 	}
 	
 	public Image getTerrainImage(String name) {
 		return terrainImgList.get(name);
 	}
 	
 	public int getTerrainId(String name) {
 		return terrainKeyList.indexOf(name);
 	}
 	
 	public Image getObjectImage(int id, int rotation) {
 		return objectImgList.get(objectKeyList.get(id))[rotation];
 	}
 	
 	public Image getObjectImage(String name, int rotation) {
 		return objectImgList.get(name)[rotation];
 	}
 	
 	public int getObjectId(String name) {
 		return objectKeyList.indexOf(name);
 	}
 	
 	public boolean isLargeObject(int id) {
 		return (objectKeyList.get(id).substring(0, objectKeyList.get(id).lastIndexOf(".")).endsWith("_l"));
 	}
 
 	public Image getFenceImage(int id) {
 		return fenceImgList.get(fenceKeyList.get(id));
 	}
 	
 	public Image getFenceImage(String name) {
 		return fenceImgList.get(name);
 	}	
 	
 	public int getFenceId(String name) {
 		return fenceKeyList.indexOf(name);
 	}
 
 	public ArrayList<String> getImageKeyList(ImageType type) {
 		switch (type) {
 			case TERRAIN:
 				return terrainKeyList;
 			case OBJECT:
 				return objectKeyList;
 			case FENCE:
 				return fenceKeyList;
 			default:
 				break;
 		}
 		
 		return null;
 	}
 
 	public Image getSelectorImage(ImageType type, String img) {
 		switch (type) {
 			case TERRAIN:
 				return getTerrainImage(img);
 			case OBJECT:
 				return getObjectImage(img, 0);
 			case FENCE:
 				return getFenceImage(img);
 			default:
 				break;
 		}
 	
 		return null;
 	}
 }
