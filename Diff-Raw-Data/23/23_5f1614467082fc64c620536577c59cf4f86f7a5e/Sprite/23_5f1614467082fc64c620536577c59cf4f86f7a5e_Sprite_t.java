 package android.game;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 
 public class Sprite {
 	protected TextureRegion texture;
 	
 	protected int imgWidth;
 
 	protected int imgHeight;
 	
 	double x, y;
 	
 	int width, height;
 	
 	double rotation;
 	
 	boolean rotCenter = true;
 	
 	public Sprite (String filename){
 		texture = new TextureRegion(new Texture(Gdx.files.internal(filename)));
 		imgWidth = texture.getRegionWidth();
 		imgHeight = texture.getRegionHeight();
 		width = imgWidth;
 		height = imgHeight;
 	}
 	
	public Sprite (String filename, int width, int height){
		texture = new TextureRegion(new Texture(Gdx.files.internal(filename)));
		imgWidth = texture.getRegionWidth();
		imgHeight = texture.getRegionHeight();
		this.width = width;
		this.height = height;
	}
	
 	public Sprite (String filename, double rotation) {
 		texture = new TextureRegion(new Texture(Gdx.files.internal(filename)));
 		imgWidth = texture.getRegionWidth();
 		imgHeight = texture.getRegionHeight();
 		width = imgWidth;
 		height = imgHeight;
 		this.rotation = rotation;
 	}
 	
 	public void draw(SpriteBatch batch, int dx, int dy){
 		if (rotCenter){
 			batch.draw(texture, (int) x - dx, (int) y - dy, width / 2, height / 2, width, height, 1, 1, (float) rotation);
 		} else {
 			batch.draw(texture, (int) x - dx, (int) y - dy, 0, 0, width, height, 1, 1, (float) rotation);
 		}
 	}
 	
 	public Rectangle getBoundingBox(){
 		Rectangle rectangle = new Rectangle();
 		rectangle.width = width;
 		rectangle.height = height;
 		rectangle.x = (int) x;
 		rectangle.y = (int) y;
 		return rectangle;
 	}
 	
 	public boolean intersects(Rectangle rectangle){
 		return getBoundingBox().overlaps(rectangle);
 	}
 	
 	public boolean intersects(Sprite sprite){
 		return intersects(sprite.getBoundingBox());
 	}
 }
