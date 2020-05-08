 package de.hswt.hrm.scheme.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Represents a Component
  * 
  * @author Michael Sieger
  *
  */
 public class Component {
 	
 	private static final String NO_IMAGE_ERROR = "All Images are null";
     
     private final int id;
 
     private String name;
     private byte[] leftRightImage;
     private byte[] rightLeftImage;
     private byte[] upDownImage;
     private byte[] downUpImage;
     private int quantifier;
     private boolean isRated;
     private Category category;
     
     public Component(int id, String name, byte[] leftRightImage, byte[] rightLeftImage,
             byte[] upDownImage, byte[] downUpImage, int quantifier, boolean isRated,
             Category category) {
         super();
         this.id = id;
         setName(name);
         this.leftRightImage = leftRightImage;
         this.rightLeftImage = rightLeftImage;
         this.upDownImage = upDownImage;
         this.downUpImage = downUpImage;
         setQuantifier(quantifier);
         setRated(isRated);
         setCategory(category);
     	checkArgument(!(leftRightImage == null &&
 				rightLeftImage == null &&
 				upDownImage == null && 
				downUpImage == null));
     }
     
     public Component(String name, byte[] leftRightImage, byte[] rightLeftImage,
             byte[] upDownImage, byte[] downUpImage, int quantifier, boolean isRated,
             Category category) {
         this(-1, name, leftRightImage, rightLeftImage, 
                 upDownImage, downUpImage, quantifier, isRated, category);
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public Category getCategory() {
 		return category;
 	}
 
 	public void setCategory(Category category) {
 		checkNotNull(category);
 		this.category = category;
 	}
 
 	public void setName(String name) {
         //The name must be a non empty string
 		checkNotNull(name);
         checkArgument(!name.trim().isEmpty());
         this.name = name;
     }
 
     public byte[] getLeftRightImage() {
         return leftRightImage;
     }
 
     public void setLeftRightImage(byte[] leftRightImage) {
     	if(leftRightImage == null){
     		checkArgument(rightLeftImage != null || upDownImage != null || downUpImage != null, 
     						NO_IMAGE_ERROR);
     	}
         this.leftRightImage = leftRightImage;
     }
 
     public byte[] getRightLeftImage() {
         return rightLeftImage;
     }
 
     public void setRightLeftImage(byte[] rightLeftImage) {
     	if(rightLeftImage != null){
     		checkArgument(leftRightImage != null || upDownImage != null || downUpImage != null, 
 					NO_IMAGE_ERROR);
     	}
         this.rightLeftImage = rightLeftImage;
     }
 
     public byte[] getUpDownImage() {
         return upDownImage;
     }
 
     public void setUpDownImage(byte[] upDownImage) {
     	if(upDownImage != null){
     		checkArgument(rightLeftImage != null || leftRightImage != null || downUpImage != null, 
 					NO_IMAGE_ERROR);
     	}
         this.upDownImage = upDownImage;
     }
 
     public byte[] getDownUpImage() {
         return downUpImage;
     }
 
     public void setDownUpImage(byte[] downUpImage) {
     	if(downUpImage != null){
     		checkArgument(rightLeftImage != null || leftRightImage != null || upDownImage != null, 
 					NO_IMAGE_ERROR);
     	}
         this.downUpImage = downUpImage;
     }
 
     public int getQuantifier() {
         return quantifier;
     }
 
     public void setQuantifier(int quantifier) {
         checkArgument(quantifier >= 0);
         this.quantifier = quantifier;
     }
 
     public boolean isRated() {
         return isRated;
     }
 
     public void setRated(boolean isRated) {
         this.isRated = isRated;
     }
 }
