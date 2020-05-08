 package indaprojekt;
 
 import java.awt.geom.Rectangle2D.Float;
 
 import org.newdawn.slick.Image;
 
 public class ToggleButton extends Button 
 {
 	private boolean toggled;
 	private Image toggledImage, toggledMouseOverImage;
 	private Image internalImage, internalMouseOverImage;
 	
 	public ToggleButton(Image image, Image mouseOverImage,
 						Image toggledImage, Image toggledMouseOverImage, Float area) 
 	{
 		super(image, mouseOverImage, area);
 		this.toggledImage = toggledImage;
 		this.toggledMouseOverImage = toggledMouseOverImage;
 		this.internalImage = image;
 		this.internalMouseOverImage = mouseOverImage;
 	}
 	
 	@Override
 	protected void buttonPressed()
 	{
 		toggled = !toggled;
 		if (toggled) {
 			image = toggledImage;
 			mouseOverImage = toggledMouseOverImage;
 		} else {
 			image = internalImage;
 			mouseOverImage = internalMouseOverImage;
 		}
 	}
 }
