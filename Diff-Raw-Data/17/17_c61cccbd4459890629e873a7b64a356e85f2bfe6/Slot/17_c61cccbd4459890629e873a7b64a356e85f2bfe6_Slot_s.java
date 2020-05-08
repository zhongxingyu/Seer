 package Generator;
 
import android.R.integer;
import Common.Photo;

 /**
  * Represents a fixed empty space for a photo in a template
  * @author yonatan
  *
  */
 public class Slot{
 
 	private PixelPoint topLeft;
 	private PixelPoint bottomRight;
 	private PixelPoint topRight;
 	private PixelPoint bottomLeft;
 	
 	private PixelPoint connectingLinePoint; // the point where the lint starts for MapCollage
 
 	private boolean horizontal;	
 	private Photo photo = null; // the photo that fills the slot
 
 	public Slot(PixelPoint topLeft, PixelPoint bottomRight) {
 		this.topLeft = topLeft;
 		this.bottomRight = bottomRight;
 		this.topRight = new PixelPoint(bottomRight.getX(), topLeft.getY());
 		this.bottomLeft = new PixelPoint(topLeft.getX(), bottomRight.getY());
 		this.horizontal = (getSlotWidth() > getSlotHeight()); 
 	}
 	
 	public Slot(PixelPoint topLeft, PixelPoint bottomRight, PixelPoint connectingLinePoint) {
 		this(topLeft, bottomRight);
 		this.connectingLinePoint = connectingLinePoint;
 	}
 
 	public void assignToPhoto(Photo photo) {
 		if (photo != null) {
 			this.photo = photo;
 		}
 		
 	}
 
 	public Photo getPhoto() {
 		return this.photo;
 	}
 	public boolean isAssignedToPhoto() {
 		return (photo != null);
 	}
 
 	public PixelPoint getTopLeft() {
 		return this.topLeft;
 	}
 	
 
 	public PixelPoint getTopRight() {
 		return this.topRight;
 	}
 
 	public PixelPoint getBottomLeft() {
 		return this.bottomLeft;
 	}
 
 	public PixelPoint getBottomRight() {
 		return this.bottomRight;
 	}
 
 	
 	public PixelPoint getConnectingLinePoint() {
 		return this.connectingLinePoint;
 	}
 	
 	public boolean isHorizontal() {
 		return this.horizontal;
 	}
 	
 	public boolean hasConnectingLinePoint()
 	{
 		return (this.connectingLinePoint != null);
 	}
 
 	
 	
 	public int getSlotWidth() {
 		
 		Double doubleValue = Double.valueOf(Math.abs(bottomRight.distanceFrom(new PixelPoint(topLeft.getX(), bottomRight.getY())))) ;
 		return doubleValue.intValue();
 	}
 
 	public int getSlotHeight() {
 		Double doubleValue = Double.valueOf(Math.abs(bottomRight.distanceFrom(new PixelPoint(bottomRight.getX(), topLeft.getY()))));
 		return doubleValue.intValue();
 	}
 
 	/**
 	 * calculate minimum image size that respects original ratio AND bigger than slot dimensions
 	 * @param p - photo to calculate new dimensions for
 	 * @return [0] == width, [1] == height, 
 	 * s.t. width>=slot.getWidth() && height>=slot.getHeight && (width/height) == (p.getWidth()/p.getheight)
 	 */
 	public int[] getProportionateDimensionsForSlot(int sourceWidth, int sourceHeight) {
 
 		int targetWidth = (int) getSlotWidth(); ;
 		int targetHeight = (int) getSlotHeight();
 
 		double ratioWidths;
 		double ratioHeights;
 
 		ratioWidths = ((double) targetWidth / (double) sourceWidth);
 		ratioHeights = ((double) targetHeight / (double) sourceHeight );
 
 		int[] ret = {-1, -1};
 
 		if (ratioWidths > ratioHeights) {
 			ret[0] = targetWidth;
 			ret[1] = (int) (sourceHeight * ratioWidths); 	// scale height
 		}
 		else {
 			ret[0] = (int) (sourceWidth * ratioHeights); // scale width
 			ret[1] = targetHeight;
 		}
 
 		return ret;
 
 	}
 
 
 
 }
