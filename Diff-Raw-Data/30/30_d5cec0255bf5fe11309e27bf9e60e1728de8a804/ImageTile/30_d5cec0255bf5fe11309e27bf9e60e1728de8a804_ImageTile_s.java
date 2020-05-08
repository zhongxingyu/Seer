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
 	String filename;
 	
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
 	public ImageTile(int x, int y, BufferedImage image, String filename, boolean isRotated) {
 		this.x = x;
 		this.y = y;
 		this.isRotated = isRotated;
 		if (isRotated)
 			this.image = rotate(image);
 		else this.image = image;
 		this.width = this.image.getWidth();
 		this.height = this.image.getHeight();
 		this.filename = filename;
 	}
 	
 	//Constructor
 	public ImageTile(String filename, BufferedImage image) {
 		this.image = image;
 		this.filename = filename;
 	}
 	
 	//Rotate image
 	private BufferedImage rotate(BufferedImage image) {
 		//Apply transformations
 		AffineTransform transform = new AffineTransform();
 		transform.rotate(Math.toRadians(90));
 		transform.translate(0, image.getHeight()*-1);
 		AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
 		BufferedImage transformed_img = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
 		op.filter(image, transformed_img);
 		
 		return transformed_img;
 	}
 	
 	//Get buffered image type
 	public int getBufferedImageType() {
 		return this.image.getType();
 	}
 	
 	//Get BufferedImage
 	public BufferedImage getImage() {
 		return this.image;
 	}
 	
 	//Get filename
 	public String getFilename() {
 		return this.filename;
 	}
 	
 	//Get x
 	public int getX() {
 		return this.x;
 	}
 	
 	//Get y
 	public int getY() {
 		return this.y;
 	}
 	
 	//Get width
 	public int getWidth() {
 		return this.width;
 	}
 	
 	//Get height
 	public int getHeight() {
 		return this.height;
 	}
 	
 	//Get rotation
 	public boolean isRotated() {
 		return this.isRotated;
 	}
 }
