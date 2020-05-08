 package ui.isometric.libraries;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import serialization.ParseException;
 import serialization.Serializer;
 import serialization.Serializers;
 import serialization.Tree;
 import ui.isometric.abstractions.IsoAnimatedImage;
 import ui.isometric.abstractions.IsoImage;
 import ui.isometric.abstractions.IsoSquare;
 import util.Direction;
 import util.ImageEdit;
 import util.Resources;
 
 import game.*;
 import data.Database;
 
 /**
  * 
  * A class for getting the correct information needed for an IsoCanvas to display from data provided by a GamModel
  * 
  * @author melby
  *
  */
 public class IsoRendererLibrary {
 	public static final String RENDERER_ISOMETRIC = "renderer.isometric";
 	public static final String RENDERER_ISOMETRIC_LEVEL = "level";
 	
 	private static Map<String, Map<Direction, RendererImage>> renderers = null;
 	private static Map<String, Integer> offsets = null;
 	private static BufferedImage maskTile;
 	
 	public static final String MASK_TILE_NAME = "mask_tile";
 	
 	/**
 	 * A class that stores images/animations
 	 * 
 	 * @author melby
 	 *
 	 */
 	public static class RendererImage {
 		private int numFrames;
 		private BufferedImage[] frames;
 		
 		/**
 		 * Create a static image with a single frame
 		 * @param image
 		 */
 		public RendererImage(BufferedImage image) {
 			this.frames = new BufferedImage[]{image};
 			this.numFrames = 1;
 		}
 		
 		public RendererImage(BufferedImage image, int frames) {
 			if(frames > 0) {
 				this.frames = ImageEdit.splitImage(image, frames, 1);
 				this.numFrames = frames;
 			}
 			else {
 				this.frames = new BufferedImage[]{image};
 				this.numFrames = 1;
 			}
 		}
 		
 		/**
 		 * get the single first image
 		 * @return
 		 */
 		public BufferedImage image() {
 			return frames[0];
 		}
 		
 		/**
 		 * Get a image for a frame at the given index
 		 * Wraps back to start if index out of bounds
 		 * @param index
 		 * @return
 		 */
 		public BufferedImage image(int index) {
 			return frames[index % numFrames];
 		}
 		
 		/**
 		 * The number of frames
 		 * @return
 		 */
 		public int frameCount() {
 			return numFrames;
 		}
 		
 		/**
 		 * The width of the image
 		 * @return
 		 */
 		public int width() {
 			return frames[0].getWidth();
 		}
 		
 		/**
 		 * The height of the image
 		 * @return
 		 */
 		public int height() {
 			return frames[0].getHeight();
 		}
 	}
 	
 	/**
 	 * A class used for reading and writing image info to disk
 	 * 
 	 * @author melby
 	 *
 	 */
 	private static class ImageType {
 		private Type type;
 		private String imageName;
 		private int offset;
 		private int frames;
 		
 		private static final String NAME = "name";
 		private static final String TYPE = "type";
 		private static final String Y_OFFSET = "y-offset";
 		private static final String FRAME_COUNT = "frame-count";
 		
 		/**
 		 * The different types of images
 		 * When loaded they will be assumed that they are .png
 		 * The name of the file that will be loaded is dependent on
 		 * the image NAME tag and TYPE as defined in enum Type
 		 * @author melby
 		 *
 		 */
 		protected enum Type {
 			/**
 			 * An image that looks the same from every direction/symmetrical around z axis
 			 * One images represents all 4 directions
 			 */
 			IMAGE1,
 			/**
 			 * An image that is symmetrical around vertical/horizontal
 			 * So 2 images represent all 4 directions and should be suffixed _ns or _ew respectively
 			 */
 			IMAGE2,
 			/**
 			 * An image that is made of 4 different images
 			 * So images that represent each direction should be prefixed _n, _e, _s, _w respectively
 			 */
 			IMAGE4;
 		}
 		
 		/**
 		 * A serializer for ImageType
 		 * @author melby
 		 *
 		 */
 		protected static class Serializer implements serialization.Serializer<ImageType> {
 			@Override
 			public ImageType read(Tree in) throws ParseException {				
 				int off = 0;
 				try {
 					String l = in.find(Y_OFFSET).value();
 					off = Integer.parseInt(l);
 				}
 				catch(ParseException e) {};
 				
 				int frames = 0;
 				try {
 					String f = in.find(FRAME_COUNT).value();
 					frames = Integer.parseInt(f);
 				}
 				catch(ParseException e) {};
 				
 				return new ImageType(in.find(NAME).value(), Type.valueOf(in.find(TYPE).value()), off, frames);
 			}
 
 			@Override
 			public Tree write(ImageType in) {
 				Tree out = new Tree();
 				out.add(new Tree.Entry(NAME, new Tree(in.imageName)));
 				out.add(new Tree.Entry(TYPE, new Tree(in.type.name())));
 				if(in.offset != 0) {
 					out.add(new Tree.Entry(Y_OFFSET, new Tree(in.offset+"")));
 				}
 				if(in.frames != 0) {
 					out.add(new Tree.Entry(FRAME_COUNT, new Tree(in.frames+"")));
 				}
 				
 				return out;
 			}
 		}
 		
 		/**
 		 * Create an ImageType with a image name, type, image y-offset and number of animation frames
 		 * @param image
 		 * @param type
 		 * @param yoff
 		 * @param frames
 		 */
 		public ImageType(String image, Type type, int yoff, int frames) {
 			this.imageName = image;
 			this.type = type;
 			this.offset = yoff;
 			this.frames = frames;
 		}
 		
 		/**
 		 * Load the data structure that will be added to the list of all renderers depending on
 		 * the image name and type
 		 * @return
 		 */
 		public Map<Direction, RendererImage> load() {
 			if(frames > 0) {
 				switch(type) {
 					case IMAGE1:
 						return loadImageAnimation1(imageName, frames);
 					case IMAGE2:
 						return loadImageAnimation2(imageName, frames);
 					case IMAGE4:
 						return loadImageAnimation4(imageName, frames);
 					default:
 						throw new RuntimeException("Unknown ImageType.Type encountered: " + type);
 				}
 			}
 			else {
 				switch(type) {
 					case IMAGE1:
 						return loadImage1(imageName);
 					case IMAGE2:
 						return loadImage2(imageName);
 					case IMAGE4:
 						return loadImage4(imageName);
 					default:
 						throw new RuntimeException("Unknown ImageType.Type encountered: " + type);
 				}
 			}
 		}
 		
 		/**
 		 * The y-offset this image should be rendered at
 		 * @return
 		 */
 		public int yoffset() {
 			return offset;
 		}
 	}
 
 	/**
 	 * Get the renderers, if null create them
 	 * @return
 	 */
 	private static Map<String, Map<Direction, RendererImage>> renderers() {
 		synchronized(IsoRendererLibrary.class) {
 			if(renderers == null) {
 				loadImages();
 			}
 		}
 		
 		return renderers;
 	}
 	
 	/**
 	 * Get the y offsets, if null create them
 	 * @return
 	 */
 	private static Map<String, Integer> offsets() {
 		synchronized(IsoRendererLibrary.class) {
 			if(offsets == null) {
 				loadImages();
 			}
 		}
 		
 		return offsets;
 	}
 
 	/**
 	 * Load all the images from disk into our internal data structures
 	 */
 	private static void loadImages() {
 		synchronized(IsoRendererLibrary.class) {
 			if(renderers == null) {
 				renderers = new HashMap<String, Map<Direction, RendererImage>>();
 				offsets = new HashMap<String, Integer>();
 				
 				
 				Serializer<Map<String, ImageType>> deserializer = new Serializers.Map<String, ImageType>(Serializers.Serializer_String, new ImageType.Serializer());
 				Map<String, ImageType> types = null;
 				try {
 					types = deserializer.read(Database.xmlToTree(Resources.loadTextResource("/resources/isotiles/resources.xml")));
 				} catch (IOException e) {
 					System.err.println("Unable to load resource declerations");
 					e.printStackTrace();
 				} catch (ParseException e) {
 					System.err.println("Unable to load resource declerations");
 					e.printStackTrace();
 				}
 				
 				for(String key : types.keySet()) {
 					renderers.put(key, types.get(key).load());
 					offsets.put(key, types.get(key).yoffset());
 				}
 								
 				maskTile = imageForRendererName(MASK_TILE_NAME, Direction.NORTH).image();
 			}
 		}
 	}
 	
 	/**
 	 * Create the data structure needed for an image by having the same image for all 4 directions.
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImage1(String resourceName) {
 		return loadImageAnimation1(resourceName, 0);
 	}
 	
 	/**
 	 * Create the data structure needed for an image by having the same image for all 4 directions.
 	 * Note, assumes images are png and adds the extension automatically. Optional number of frames for an animation,
 	 * pass 0 to get no animation splitting
 	 * @param resourceName
 	 * @param frames
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImageAnimation1(String resourceName, int frames) {
 		Map<Direction, RendererImage> map = new HashMap<Direction, RendererImage>();
 		
 		try {
 			RendererImage image = new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+".png"), frames);
 			
 			map.put(Direction.NORTH, image);
 			map.put(Direction.EAST, image);
 			map.put(Direction.WEST, image);
 			map.put(Direction.SOUTH, image);
 		} catch (IOException e) {
 			System.err.println("Unable to load image1: " + resourceName);
 		}
 		return map;
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _n _e _s _w into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImage4(String resourceName) {
 		return loadImageAnimation4(resourceName, 0);
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _n _e _s _w into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically. Optional number of frames for an animation,
 	 * pass 0 to get no animation splitting
 	 * @param resourceName
 	 * @param frames
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImageAnimation4(String resourceName, int frames) {
 		Map<Direction, RendererImage> map = new HashMap<Direction, RendererImage>();
 		
 		try {
 			map.put(Direction.NORTH, new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_n.png"), frames));
 			map.put(Direction.EAST, new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_e.png"), frames));
 			map.put(Direction.WEST, new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_w.png"), frames));
 			map.put(Direction.SOUTH, new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_s.png"), frames));
 		} catch (IOException e) {
 			System.err.println("Unable to load image4: " + resourceName);
 		}
 		
 		return map;
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _ns _ew into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically
 	 * @param resourceName
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImage2(String resourceName) {
 		return loadImageAnimation2(resourceName, 0);
 	}
 	
 	/**
 	 * Create the data structure for an image by loading the images suffixed _ns _ew into the appropriate places
 	 * Note, assumes images are png and adds the extension automatically. Optional number of frames for an animation,
 	 * pass 0 to get no animation splitting
 	 * @param resourceName
 	 * @param frames
 	 * @return
 	 */
 	private static Map<Direction, RendererImage> loadImageAnimation2(String resourceName, int frames) {
 		Map<Direction, RendererImage> map = new HashMap<Direction, RendererImage>();
 		
 		try {
 			RendererImage ns = new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_ns.png"), frames);
 			RendererImage ew = new RendererImage(Resources.readImageResourceUnfliped("/resources/isotiles/"+resourceName+"_ew.png"), frames);
 			
 			map.put(Direction.NORTH, ns);
 			map.put(Direction.EAST, ew);
 			map.put(Direction.WEST, ew);
 			map.put(Direction.SOUTH, ns);
 		} catch (IOException e) {
 			System.err.println("Unable to load image2: " + resourceName);
 		}
 		
 		return map;
 	}
 	
 	/**
 	 * Get the appropriate image for a given renderer name
 	 * @param renderer
 	 * @param viewDirection
 	 * @return
 	 */
 	public static RendererImage imageForRendererName(String rendererName, Direction viewDirection) {
 		Map<Direction, RendererImage> renderer = renderers().get(rendererName);
 		if(renderer != null) {
 			return renderer.get(viewDirection);
 		}
 		else {
 			return null;
 		}
 	}
 	
 	/**
 	 * Get the y offset to render the image at
 	 * @param rendererName
 	 * @return
 	 */
 	public static int offsetForRendererName(String rendererName) {
 		Integer off = offsets().get(rendererName);
 		if(off != null) {
 			return off;
 		}
 		else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Get a new IsoImage representing the given GameThing on a given square
 	 * @param square
 	 * @param thing
 	 * @param viewDirection
 	 * @return
 	 */
 	public static IsoImage newImageFromGameThing(IsoSquare square, GameThing thing, Direction viewDirection) {
 		IsoImage tmp = null;
 		
 		Location l = thing.location();
 		if(l instanceof Level.Location) {
 			RendererImage image = imageForRendererName(thing.renderer(), ((Level.Location)l).direction().compose(viewDirection.inverse()));
 			if(image == null) { // TODO: make IsoImage cope with null images?
 				throw new RuntimeException("Invalid renderer name: "+thing.renderer());
 			}
 			tmp = new IsoImage(image, square, offsetForRendererName(thing.renderer()));
 			tmp.setGameThing(thing);
 		}
		if(l == null) {
			throw new RuntimeException("Null location for: "+thing);
		}
				
 		return tmp;
 	}
 	
 	/**
 	 * Get a new IsoImage representing a given Animation
 	 * @param square
 	 * @param thing
 	 * @param viewDirection
 	 * @param rendererName
 	 * @param animationFrame
 	 * @return
 	 */
 	public static IsoImage newImageFromGameThing(IsoSquare square, GameThing thing, Direction viewDirection, String rendererName, int animationFrame) {
 		IsoImage tmp = null;
 		
 		Location l = thing.location();
 		if(l instanceof Level.Location) {
 			RendererImage image = imageForRendererName(rendererName, ((Level.Location)l).direction().compose(viewDirection.inverse()));
 			if(image == null) { // TODO: make IsoImage cope with null images?
 				throw new RuntimeException("Invalid renderer name: "+rendererName);
 			}
 			
 			tmp = new IsoAnimatedImage(image, square, offsetForRendererName(thing.renderer()), animationFrame);
 			tmp.setGameThing(thing);
 		}
 		
 		return tmp;
 	}
 
 	/**
 	 * Get all the renderers supported
 	 * @return
 	 */
 	public static Set<String> allRendererNames() {
 		return renderers().keySet();
 	}
 
 	/**
 	 * An empty tile with the correct alpha channel to use for tile selection
 	 * @return
 	 */
 	public static BufferedImage maskTile() {
 		if(renderers == null) {
 			loadImages();
 		}
 		
 		return maskTile;
 	}
 }
