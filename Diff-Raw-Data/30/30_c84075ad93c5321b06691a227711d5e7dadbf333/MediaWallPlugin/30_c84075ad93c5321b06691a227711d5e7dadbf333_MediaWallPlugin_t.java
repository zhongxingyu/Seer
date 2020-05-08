 package com.juberti.MediaWallPlugin;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.imageio.ImageReader;
 import javax.imageio.metadata.IIOMetadataNode;
 import javax.imageio.stream.ImageInputStream;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockRedstoneEvent;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.Vector;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public final class MediaWallPlugin extends JavaPlugin implements Listener {
 	class Wool {
 		Wool(int inData, int r, int g, int b) {
 			data = inData;
 			color = new Color(r, g, b);					
 		}
 		public int getData() { return data; }
 		public Color getColor() {
 			return color;
 		}
 		int data;
 		Color color;
 	};
 	final Wool[] woolTypes = {
 		new Wool(0, 221, 221, 221),
 		new Wool(1, 219, 125, 62),
 		new Wool(2, 179, 80, 188),
 		new Wool(3, 107, 138, 201),
 		new Wool(4, 177, 166, 39),
 		new Wool(5, 65, 174, 56),
 		new Wool(6, 208, 132, 153),
 		new Wool(7, 64, 64, 64),
 		new Wool(8, 154, 161, 161),
 		new Wool(9, 46, 110, 137),
 		new Wool(10, 126, 61, 181),
 		new Wool(11, 46, 56, 141),
 		new Wool(12, 79, 50, 31),
 		new Wool(13, 53, 70, 27),
 		new Wool(14, 150, 52, 48),
 		new Wool(15, 25, 22, 22)
 	};
 	
 	class Panel {
 	    Panel(int x, int y, int z, int w, int h, int d, int skinX, int skinY, int skinW, int skinH) {
 	        stevePos = new Vector(x, y, z);
 	        steveArea = new Vector(w, h, d);
 	        skinRect = new Rectangle(skinX, skinY, skinW, skinH);
 	    }
 		Panel(Vector inPos, Vector inArea, Rectangle inRect) {
 	    	stevePos = inPos;
 	    	steveArea = inArea;
 	    	skinRect = inRect;
 	    }
 	    public Vector getStevePos() { return stevePos; }
 	    public Vector getSteveArea() { return steveArea; }
 	    public Rectangle getSkinRect() { return skinRect; }
 	    Vector stevePos;
 	    Vector steveArea;
 	    Rectangle skinRect;
 	};
 	final Panel[] stevePanels = {
 		new Panel( 0, 31, -2,  8,  1,  8,  8,  0,  8,  8), 	// top of head 
 		new Panel(-4, 23,  0,  4,  1,  4, 44, 16,  4,  4), 	// left shoulder
 		new Panel( 8, 23,  0,  4,  1,  4, 44, 16,  4,  4), 	// right shoulder [Note: Switching to bottom view] 
 		new Panel( 0,  0,  0,  4,  1,  4,  8, 16,  4,  4), 	// left foot
 		new Panel( 4,  0,  0,  4,  1,  4,  8, 16,  4,  4), 	// right foot
 		new Panel(-4, 12,  0,  4,  1,  4, 48, 16,  4,  4), 	// left hand
 		new Panel( 8, 12,  0,  4,  1,  4, 48, 16,  4,  4), 	// right hand
 		new Panel( 0, 24,  4,  8,  1,  2, 16,  0,  8,  2), 	// top half of the bottom
 		new Panel( 0, 24, -2,  8,  1,  2, 16,  6,  8,  2), 	// top half of the bottom [Note:Switching to left view]
 		new Panel( 0,  0,  0,  1, 12,  4,  8, 20,  4, 12), 	// left leg
 		new Panel(-4, 12,  0,  1, 12,  4, 36, 20,  4, 12), 	// left arm
 		new Panel( 0, 24, -2,  1,  8,  8, 16,  8,  8,  8), 	// left side of head [Note: Switching to right view]
		new Panel( 7,  0,  0,  1, 12,  4,  0, 20,  4, 12), 	// right leg
		new Panel(11, 12,  0,  1, 12,  4, 28, 20,  4, 12), 	// right arm
 		new Panel( 7, 24, -2,  1,  8,  8,  0,  8,  8,  8),	// right side of head [Note: Switching to front view]
		new Panel( 0, 24,  5,  8,  8,  1,  8,  8,  8,  8), 	// face
 		new Panel(-4, 12,  3,  4, 12,  1, 44, 20,  4, 12), 	// left arm front
 		new Panel( 8, 12,  3,  4, 12,  1, 44, 20,  4, 12), 	// right arm front
 		new Panel( 0, 12,  3,  8, 12,  1, 20, 20,  8, 12), 	// front of chest
 		new Panel( 0,  0,  3,  4, 12,  1,  4, 20,  4, 12), 	// left leg
 		new Panel( 4,  0,  3,  4, 12,  1,  4, 20,  4, 12), 	// left leg [Note: Switching to back view]
 		new Panel( 0, 24, -2,  8,  8,  1, 24,  8,  8,  8), 	// back of head
 		new Panel(-4, 12,  0,  4, 12,  1, 52, 20,  4, 12), 	// left arm front
 		new Panel( 8, 12,  0,  4, 12,  1, 52, 20,  4, 12), 	// right arm front
 		new Panel( 0, 12,  0,  8, 12,  1, 32, 20,  8, 12), 	// back of chest
 		new Panel( 0,  0,  0,  4, 12,  1, 12, 20,  4, 12), 	// left leg
 		new Panel( 4,  0,  0,  4, 12,  1, 12, 20,  4, 12), 	// left leg
 	};
 
 	@Override
 	public void onEnable(){
 		getLogger().info("onEnable has been invoked!");
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	@Override
 	public void onDisable() {
 		getLogger().info("onDisable has been invoked!");		
 	}
 	void maybeBuildImage(final Location location, final String text) {
 		if (text.contains("goo.gl")) {
 			try {
 				URL url;
 				url = new URL("http://" + text);
 	            asyncBuildImage(location, url);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 		if (text.contains("statue")) {
 			buildStatue(location, "Gavin_U");
 		}
 	}
 	void asyncBuildImage(final Location location, final URL url) {
 		Thread t = new Thread(new Runnable() {
 		    @Override
 			public void run() {
 		    	buildImage(location, url);		        		    
 		    }
 		});
 		t.start();
 	}
     void buildImage(Location location, URL url) {
       final int MAX_HEIGHT = 32;
       final int MAX_PLAYS = 100;
       try {
 			List<ImageFrame> frames = loadImage(url);			
 			getLogger().info("Image loaded: " + url);
 			resizeImage(frames, MAX_HEIGHT);
 			if (frames.size() == 1) {
 			    buildImage(location, frames);
 			} else {
 				for (int i = 0; i < MAX_PLAYS; ++i) { 
 				    for (int j = 0; j < frames.size(); ++j) {
 					    buildImage(location, frames.get(j));
 					    try {
 						    Thread.sleep(frames.get(j).getDelay());
 					    } catch (InterruptedException e) {						
 					    }
 				    }
 				}
 			}
 		} catch (IOException e) {
 			getLogger().info("Image failed to load: " + url);
 		}	
     }
     List<ImageFrame> loadImage(URL url) throws IOException {
         InputStream stream = url.openStream();
         ImageInputStream imageStream = ImageIO.createImageInputStream(stream);
         Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
         if (!readers.hasNext())
             throw new IOException("Unknown image format");
         ImageReader reader = readers.next();
         reader.setInput(imageStream);
         
         List<ImageFrame> frames = new ArrayList<ImageFrame>();
         int n = reader.getNumImages(true);
         int firstW = 0, firstH = 0;
         //IIOMetadata metadata = reader.getStreamMetadata();
         System.out.println("Image contains " + n + " frames");
         for (int i = 0; i < n; i++) {
             BufferedImage image = reader.read(i);
             int delay = 0, w = image.getWidth(), h = image.getHeight();
             if (i == 0) {
             	firstW = w;
             	firstH = h;
             }
             //System.out.println(reader.getFormatName());
             if (reader.getFormatName().equals("gif")) {
                 IIOMetadataNode root = (IIOMetadataNode) reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");
                 IIOMetadataNode gce = (IIOMetadataNode) root.getElementsByTagName("GraphicControlExtension").item(0);
                 delay = Integer.valueOf(gce.getAttribute("delayTime")) * 10;
                 System.out.println("Image " + i + ": delay=" + delay);
                 NodeList children = root.getChildNodes();
                 for (int nodeIndex = 0; nodeIndex < children.getLength(); nodeIndex++) {
               		Node nodeItem = children.item(nodeIndex);
 
               		if (nodeItem.getNodeName().equals("ImageDescriptor")) {
                   		NamedNodeMap map = nodeItem.getAttributes();
                   		int x, y;
                   		x = Integer.valueOf(map.getNamedItem("imageLeftPosition").getNodeValue());
                   	    y = Integer.valueOf(map.getNamedItem("imageTopPosition").getNodeValue());
                   	    System.out.println("W: " + w + " H: " + h + " X: " + x + ", Y: " + y);
                   	    if (i != 0 && x != 0 && y != 0) {
                   	        BufferedImage newImage = new BufferedImage(firstW, firstH, BufferedImage.TYPE_INT_RGB);
                 	        Graphics2D g = (Graphics2D)newImage.getGraphics();
                 	        g.drawImage(frames.get(i - 1).getImage(), 0, 0, firstW, firstH, null);
                 	        g.drawImage(image, x, y, w, h, null);
                 	        image = newImage;
                   	    }
               		}
                 }
             }
             
             frames.add(new ImageFrame(image, delay, null));          
         }
         stream.close();  
         return frames;
     }
     void resizeImage(List<ImageFrame> frames, int height) {
       for (int i = 0; i < frames.size(); ++i) {
     	  BufferedImage image = frames.get(i).getImage();
 		  int w = image.getWidth(), h = image.getHeight();
 		  int newW = (int)((float)w * height / h), newH = height;			
 		  BufferedImage newImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
 	      Graphics2D g = (Graphics2D)newImage.getGraphics();
 	      g.drawImage(image, 0, 0, newW, newH, null);
 	      frames.get(i).setImage(newImage);
       }
     }
     void buildImage(Location location, List<ImageFrame> frames) {
     	buildImage(location, frames.get(0));
     }
     void buildImage(Location location, ImageFrame frame) {
 	    BufferedImage image = frame.getImage();
     	for (int i = 0; i < image.getWidth(); ++i) {
 	    	for (int j = 0; j < image.getHeight(); ++j) {
 	    		byte t = getWoolType(image, i, j);
 	    		Location blockLoc = location.clone();
 	    		blockLoc.add(i + 1, image.getHeight() - j, 0);
 	    		blockLoc.getBlock().setTypeIdAndData(
 	    		    Material.WOOL.getId(), t, false);
 	    	}
 	    }
     }
     byte getWoolType(BufferedImage image, int x, int y) {
     	int pixel = image.getRGB(x, y);
     	return mapPixelToWool(pixel);
     }   
     byte mapPixelToWool(int pixel) {
     	Color color = new Color(pixel);
     	return mapColorToWool(color);
     }
     // TODO: figure out @Visible annotation
     public byte mapColorToWool(Color color) {
     	Wool bestWool = null;
     	float bestDist = 999;
     	for (Wool wool : woolTypes) {
     		float dist = getDistance(color, wool.getColor());
     		if (dist < bestDist) {
     			bestDist = dist;
     			bestWool = wool;
     		}
     	}
     	return (byte)bestWool.getData();
     }
     static float getDistance(Color color1, Color color2) {
     	float d0 = (float)((color1.getRed() - color2.getRed()) / 255.0); 
     	float d1 = (float)((color1.getGreen() - color2.getGreen()) / 255.0);
     	float d2 = (float)((color1.getBlue() - color2.getBlue()) / 255.0);
     	return d0 * d0 + d1 * d1 + d2 * d2;
     }
     void burnUpWool(Location bottomLeftWool) {
 	    Location loc = bottomLeftWool.clone();
 	    while (loc.getBlock().getType() == Material.WOOL) {
 		    while (loc.getBlock().getType() == Material.WOOL) {
 		    	loc.getBlock().setType(Material.AIR);
 		    	loc.add(0, 1, 0);
 		    }
             loc.setY(bottomLeftWool.getY());
             loc.add(1, 0, 0);
 	    }
     }
     void buildStatue(Location signLocation, String playerName) {
     	buildPrism(signLocation, 8, 12, 4);
     	Location torsoLocation = signLocation.clone();
     	torsoLocation.add(-4, 12, 0);
     	buildPrism(torsoLocation, 16, 12, 4);
     	Location headLocation = torsoLocation.clone();
     	headLocation.add(4, 12, -2);
     	buildPrism(headLocation, 8, 8, 8);
     	String surl = "http://s3.amazonaws.com/MinecraftSkins/" + playerName + ".png";
     	try {
 		    URL url = new URL(surl);
 		    List<ImageFrame> skin = loadImage(url);
 		    paintStatue(signLocation, skin.get(0).getImage());
     	} catch (Exception e) {
     		getLogger().info("Could not load skin for player " + playerName);
 		}
     }
     void buildPrism(Location loc, int length, int height, int width) {
     	for (int x = 0; x < length; x++) {
         	for (int y = 0; y < height; y++) {
             	for (int z = 0; z < width; z++) {
     				Location here = loc.clone();
     				here.add(x, y, z);
     				here.getBlock().setType(Material.STONE);
     			}
     		}
     	}
     }
     void paintStatue(Location startLocation, BufferedImage skinImage) {
     	for (Panel panel: stevePanels) {
     	    paintPanel(panel, startLocation, skinImage);
     	}
     }
     void paintPanel(Panel panel, Location startLocation, BufferedImage skinImage) {
 		getLogger().info("Painting panel: " + panel);
     	for(int x = 0; x<panel.getSteveArea().getX(); x++) {
          	for(int y = 0; y<panel.getSteveArea().getY(); y++) {
          		for(int z = 0; z<panel.getSteveArea().getZ(); z++) {
          			Location here = startLocation.clone();
          			here.add(panel.getStevePos());
          			here.add(x, y, z);
         			int xx = panel.getSkinRect().x;
         		    int yy = panel.getSkinRect().y;
         			if(panel.getSteveArea().getY() != 1){
         				yy += panel.getSkinRect().height - 1;
         			}
         			if (panel.getSteveArea().getX() == 1) {
         				xx += z;
         				yy -= y;
         			} else if (panel.getSteveArea().getY() == 1) {
         				xx += x;
         				yy += z;
         			} else {
         			    xx += x;
         			    yy -= y;
         			}
         			getLogger().info("x is " + x + " y is " + y + " z is " + z + " xx is " + xx + "yy is " + yy);
         			byte woolType = getWoolType(skinImage, xx, yy);
         			here.getBlock().setType(Material.WOOL);
         			here.getBlock().setData(woolType);
          		}
          	}
          }
     }

 	@EventHandler
 	public void onSignChange(SignChangeEvent event) {
 	    maybeBuildImage(event.getBlock().getLocation(), event.getLine(0));
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		if (event.getBlock().getType() == Material.WALL_SIGN) {
 		    Sign sign = (Sign)event.getBlock().getState();
 			String text = (sign.getLine(1));
 			if (text.contains("burn")) {				
 			    Location bottomLeft = sign.getLocation();
 			    bottomLeft.add(1, 0, 0);
 			    burnUpWool(bottomLeft);
 		    }
 		}
 	}
 	@EventHandler
 	public void onBlockRedstone(BlockRedstoneEvent event) {
 		if (event.getBlock().getType() == Material.WALL_SIGN) {
             maybeBuildImage(event.getBlock().getLocation(), 
             ((Sign)event.getBlock().getState()).getLine(0));
 		}
 	}
 }
