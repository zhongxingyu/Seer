 package j3chess.components;
 
 import j3chess.utility.Vector2d;
 
 import java.awt.Image;
 
 import javax.swing.ImageIcon;
 
 import artemis.Component;
 
 /**
  * entity component for all paintable entities.
  */
 public class Paintable extends Component {
 
     /** @brief the image to paint */
     private ImageIcon mImageIcon;
     /** @brief Offset to paint at */
     private Vector2d mDrawOffset;
 
     /**
      * @brief empty default constructor for paintable component
      */
     public Paintable() {
     }
 
     /**
      * @brief constructor for the paintable component
      * @param imageIcon the image to paint
      */
     public Paintable(final ImageIcon imageIcon) {
         setDrawOffset(imageIcon);
         setImage(imageIcon);
     }
 
     /**
      * @brief constructor for the paintable component
      * @param imagePath the path of the image to paint
      */
     public Paintable(final String imagePath) {
         this(new ImageIcon(imagePath));
     }
 
     /**
      * @brief getter for the mImage member
      * @return the image to paint
      */
     public final Image getImage() {
         return mImageIcon.getImage();
     }
 
     /**
      * @brief setter for the mImage member
      * @param imageIcon the image to paint
      */
     public final void setImage(final ImageIcon imageIcon) {
         this.mImageIcon = imageIcon;
     }
 
     /**
      * @brief getter for the draw offset vector
      * @return the draw offset vector
      */
     public final Vector2d getDrawOffset() {
         return mDrawOffset;
     }
 
     /**
      * @brief setter for the draw offset vector
      * @param offset to paint
      */
     public final void setDrawOffset(final Vector2d offset) {
         this.mDrawOffset = offset;
     }
 
     /**
      * @brief setter for the draw offset vector
      * @param imageIcon the image to paint
      */
     public final void setDrawOffset(final ImageIcon imageIcon) {
         this.mDrawOffset = new Vector2d(
                -imageIcon.getImage().getWidth(null) / 2,
                -imageIcon.getImage().getHeight(null) / 2);
     }
 }
