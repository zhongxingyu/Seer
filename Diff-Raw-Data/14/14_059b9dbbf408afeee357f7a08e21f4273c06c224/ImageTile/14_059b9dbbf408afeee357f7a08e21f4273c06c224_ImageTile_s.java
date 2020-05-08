 package sprite_generator;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 
 //Private class: ImageTile
 public class ImageTile {
 	int x;
 	int y;
 	int width;
 	int height;
 	boolean isRotated;
 	BufferedImage image;
 	
 	//Constructor
 	public ImageTile(int x, int y, BufferedImage image) {
 		this.x = x;
 		this.y = y;
 		this.width = image.getWidth();
 		this.height = image.getHeight();
 		this.image = image;
 		this.isRotated = false;
 	}
 	
 	//Constructor
 	public ImageTile(int x, int y, BufferedImage image, boolean isRotated) {
 		this.x = x;
 		this.y = y;
 		this.isRotated = isRotated;
 		if (isRotated)
 			this.image = rotate(image);
 		else this.image = image;
 		this.width = this.image.getWidth();
 		this.height = this.image.getHeight();
 	}
 	
 	//Rotate image
 	private BufferedImage rotate(BufferedImage image) {
 		//Apply transformations
 		AffineTransform transform = new AffineTransform();
		transform.rotate(Math.PI/2, image.getWidth()/2, image.getHeight()/2);
		transform.translate(image.getHeight(), image.getHeight());
 		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
 		BufferedImage transformed_img = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
 		op.filter(image, transformed_img);
 		
 		return transformed_img;
 	}
 	
 	//Get buffered image type
 	protected int getBufferedImageType() {
 		return this.image.getType();
 	}
 	
 	//Get BufferedImage
 	protected BufferedImage getImage() {
 		return this.image;
 	}
 	
 	//Get x
 	protected int getX() {
 		return this.x;
 	}
 	
 	//Get y
 	protected int getY() {
 		return this.y;
 	}
 	
 	//Get rotation
 	protected boolean isRotated() {
 		return this.isRotated;
 	}
 }
