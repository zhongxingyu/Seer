 package Bing;
 
 import Common.GPSPoint;
 import Common.Photo;
 import Generator.PixelPoint;
 
 /**
  * Represents a pushpin returned from Bing
  * @author yonatan
  *
  */
 public class Pushpin {
 	//private GPSPoint point;
 	private PixelPoint anchor;
 	private int[] topLeftOffset;
 	private int[] bottomRightOffset;
 	private Photo photo;
 	
 	public GPSPoint getPoint() {
 		return photo.getLocation();
 	}
 	
 	public Photo getPhoto() {
 		return this.photo;
 	}
 
 	public PixelPoint getAnchor() {
 		return anchor;
 	}
 
 	public int[] getTopLeftOffset() {
 		return topLeftOffset;
 	}
 
 	public int[] getBottomRightOffset() {
 		return bottomRightOffset;
 	}
 //
 //	public Pushpin(GPSPoint point, PixelPoint anchor, int[] topLeftOffset, int[] bottomRightOffset) {
 //		this.point = point;
 //		this.anchor = anchor;
 //		this.topLeftOffset = topLeftOffset;
 //		this.bottomRightOffset = bottomRightOffset;
 //	}
 //	
 	public Pushpin(Photo photo) {
 		this.photo = photo;
 	}
 	
 	public void setAnchor(PixelPoint anchor) {
 		this.anchor = anchor;
 	}
 }
