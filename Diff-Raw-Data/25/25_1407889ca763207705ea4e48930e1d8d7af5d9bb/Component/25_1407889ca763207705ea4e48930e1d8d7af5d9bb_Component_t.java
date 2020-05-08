 package de.hswt.hrm.scheme.model;
 
 import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Strings.isNullOrEmpty;
 
 
 /**
  * Represents a Component
  * 
  * @author Michael Sieger
  * 
  */
 public class Component {
 
     private final int id;
 
     private String name;
     private byte[] leftRightImage;
     private byte[] rightLeftImage;
     private byte[] upDownImage;
     private byte[] downUpImage;
     private int quantifier;
     private boolean boolRating;
     private Category category;
 
     private static final String NO_IMAGE_ERROR = "All Images are null";
     private static final String IS_MANDATORY = "Field is a mandatory.";
     private static final String INVALID_NUMBER = "%d is an invalid number.%n Must be greater 0";
 
     public Component(int id, String name, byte[] leftRightImage, byte[] rightLeftImage,
            byte[] upDownImage, byte[] downUpImage, int quantifier, boolean boolRating, Category category) {
         super();
         this.id = id;
         setName(name);
         this.leftRightImage = leftRightImage;
         this.rightLeftImage = rightLeftImage;
         this.upDownImage = upDownImage;
         this.downUpImage = downUpImage;
         setQuantifier(quantifier);
         setBoolRating(boolRating);
         checkArgument(
                 !(leftRightImage == null && rightLeftImage == null && upDownImage == null && downUpImage == null),
                 NO_IMAGE_ERROR);
     }
 
     public Component(String name, byte[] leftRightImage, byte[] rightLeftImage, byte[] upDownImage,
            byte[] downUpImage, int quantifier, boolean boolRating, Category category) {
         this(-1, name, leftRightImage, rightLeftImage, upDownImage, downUpImage, quantifier,
                boolRating, category);
     }
 
     public int getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setCategory(Category category) {
        checkNotNull(category);
         this.category = category;

    }

    public Category getCategory() {
        return category;
     }
 
     public void setName(String name) {
         // The name must be a non empty string
         checkArgument(!isNullOrEmpty(name), IS_MANDATORY);
         this.name = name;
     }
 
     public byte[] getLeftRightImage() {
         return leftRightImage;
     }
 
     // TODO ask Michi
     public void setLeftRightImage(byte[] leftRightImage) {
         if (leftRightImage == null) {
             checkArgument(rightLeftImage != null || upDownImage != null || downUpImage != null,
                     NO_IMAGE_ERROR);
         }
         this.leftRightImage = leftRightImage;
     }
 
     public byte[] getRightLeftImage() {
         return rightLeftImage;
     }
 
     // TODO ask Michi
     public void setRightLeftImage(byte[] rightLeftImage) {
         if (rightLeftImage != null) {
             checkArgument(leftRightImage != null || upDownImage != null || downUpImage != null,
                     NO_IMAGE_ERROR);
         }
         this.rightLeftImage = rightLeftImage;
     }
 
     public byte[] getUpDownImage() {
         return upDownImage;
     }
 
     // TODO ask Michi
     public void setUpDownImage(byte[] upDownImage) {
         if (upDownImage != null) {
             checkArgument(rightLeftImage != null || leftRightImage != null || downUpImage != null,
                     NO_IMAGE_ERROR);
         }
         this.upDownImage = upDownImage;
     }
 
     public byte[] getDownUpImage() {
         return downUpImage;
     }
 
     // TODO ask Michi
     public void setDownUpImage(byte[] downUpImage) {
         if (downUpImage != null) {
             checkArgument(rightLeftImage != null || leftRightImage != null || upDownImage != null,
                     NO_IMAGE_ERROR);
         }
         this.downUpImage = downUpImage;
     }
 
     public int getQuantifier() {
         return quantifier;
     }
 
     public void setQuantifier(int quantifier) {
         checkArgument(quantifier > 0, INVALID_NUMBER, quantifier);
         this.quantifier = quantifier;
     }
 
     public boolean getBoolRating() {
         return boolRating;
     }
 
     public void setBoolRating(boolean boolRating) {
         this.boolRating = boolRating;
     }
 }
