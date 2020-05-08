 package ui.isometric;
 
 import java.util.*;
 
 /**
  * 
  * This class represents a single square for an isometric renderer, each square can contain many images
  * 
  * @author melby
  *
  */
 public class IsoSquare implements Iterable<IsoImage> {
 
 	public static final int FLOOR = 0;
 	public static final int WALL = 1000;
	public static final int PLAYER = 2000;
 	
 	private ArrayList<ImageLevel> images = new ArrayList<ImageLevel>();
 	
 	/**
 	 * Represents an image level on a square, used for sorting etc
 	 * @author melby
 	 *
 	 */
 	private class ImageLevel {
 		private IsoImage image;
 		private int level;
 		
 		private ImageLevel(IsoImage image, int level) {
 			this.image = image;
 			this.level = level;
 		}
 	}
 	
 	/**
 	 * Compares ImageLevels
 	 * @author melby
 	 *
 	 */
 	private class ImageLevelComparator implements Comparator<ImageLevel> {
 		@Override
 		public int compare(ImageLevel arg0, ImageLevel arg1) {
 			return arg0.level - arg1.level;
 		}
 	}
 	
 	/**
 	 * An iterator that through the images on a square in level order from bottom to top
 	 * @author melby
 	 *
 	 */
 	private class ImageIterator implements Iterator<IsoImage> {
 		Iterator<ImageLevel> iterator = images.iterator();
 		
 		@Override
 		public boolean hasNext() {
 			return iterator.hasNext();
 		}
 
 		@Override
 		public IsoImage next() {
 			return iterator.next().image;
 		}
 
 		@Override
 		public void remove() {
 			// Does nothing
 		}
 	}
 	
 	/**
 	 * Add a given IsoImage to at a given level on this square
 	 * @param image
 	 * @param level
 	 */
 	public void addImageForLevel(IsoImage image, int level) {
 		images.add(new ImageLevel(image, level));
 		Collections.sort(images, new ImageLevelComparator());
 	}
 	
 	/**
 	 * Iterate through the images on a square in level order from bottom to top
 	 */
 	@Override
 	public Iterator<IsoImage> iterator() {
 		return new ImageIterator();
 	}
 	
 	/**
 	 * The number of images on this square
 	 * @return
 	 */
 	public int numberOfImages() {
 		return images.size();
 	}
 }
