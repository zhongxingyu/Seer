 /**
  * 
  */
 package model;
 
import java.awt.Image;

 
 /**
  * @author dimitri.haemmerli
  *
  */
 public class Vehicle {
 	
 	private String type;
 	private int maxSpeed;
 	private String imageURL;
 	private Image scaledImage;
 	private Knot startKnot;
 	private Knot currentKnot;
 	private Knot currentPosition;
 	private Knot nextKnot;
 	private Knot finishKnot;
 	private Boolean isSelected;
 	private Boolean isVisible;
 
 	public Vehicle(){
 		
 	}
 
 	/**
 	 * @return the type
 	 */
 	public String getType() {
 		return type;
 	}
 
 	/**
 	 * @param type the type to set
 	 */
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	/**
 	 * @return the maxSpeed
 	 */
 	public int getMaxSpeed() {
 		return maxSpeed;
 	}
 
 	/**
 	 * @param maxSpeed the maxSpeed to set
 	 */
 	public void setMaxSpeed(int maxSpeed) {
 		this.maxSpeed = maxSpeed;
 	}
 
 
 	/**
 	 * @return the imageURL
 	 */
 	public String getImageURL() {
 		return imageURL;
 	}
 
 	/**
 	 * @param imageURL the imageURL to set
 	 */
 	public void setImageURL(String imageURL) {
 		this.imageURL = imageURL;
 	}
 
 
 	/**
 	 * @return the startKnot
 	 */
 	public Knot getStartKnot() {
 		return startKnot;
 	}
 
 	/**
 	 * @param startKnot the startKnot to set
 	 */
 	public void setStartKnot(Knot startKnot) {
 		this.startKnot = startKnot;
 	}
 
 	/**
 	 * @return the currentKnot
 	 */
 	public Knot getCurrentKnot() {
 		return currentKnot;
 	}
 
 	/**
 	 * @param currentKnot the currentKnot to set
 	 */
 	public void setCurrentKnot(Knot currentKnot) {
 		this.currentKnot = currentKnot;
 	}
 
 	/**
 	 * @return the finishKnot
 	 */
 	public Knot getFinishKnot() {
 		return finishKnot;
 	}
 
 	/**
 	 * @param finishKnot the finishKnot to set
 	 */
 	public void setFinishKnot(Knot finishKnot) {
 		this.finishKnot = finishKnot;
 	}
 
 
 	/**
 	 * @return the nextKnot
 	 */
 	public Knot getNextKnot() {
 		return nextKnot;
 	}
 
 	/**
 	 * @param nextKnot the nextKnot to set
 	 */
 	public void setNextKnot(Knot nextKnot) {
 		this.nextKnot = nextKnot;
 	}
 
 	/**
 	 * @return the isSelected
 	 */
 	public Boolean getIsSelected() {
 		return isSelected;
 	}
 
 	/**
 	 * @param isSelected the isSelected to set
 	 */
 	public void setIsSelected(Boolean isSelected) {
 		this.isSelected = isSelected;
 	}
 
 	/**
 	 * @return the isVisible
 	 */
 	public Boolean getIsVisible() {
 		return isVisible;
 	}
 
 	/**
 	 * @param isVisible the isVisible to set
 	 */
 	public void setIsVisible(Boolean isVisible) {
 		this.isVisible = isVisible;
 	}
 
 	/**
 	 * @return the currentPosition
 	 */
 	public Knot getCurrentPosition() {
 		return currentPosition;
 	}
 
 	/**
 	 * @param currentPosition the currentPosition to set
 	 */
 	public void setCurrentPosition(Knot currentPosition) {
 		this.currentPosition = currentPosition;
 	}
 
 	/**
 	 * @return the scaledImage
 	 */
 	public Image getScaledImage() {
 		return scaledImage;
 	}
 
 	/**
 	 * @param scaledImage the scaledImage to set
 	 */
 	public void setScaledImage(Image scaledImage) {
 		this.scaledImage = scaledImage;
 	}
 	
 	
 	
 	
 
 }
