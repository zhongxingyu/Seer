 package uk.co.samatkins.beardsim.shaving;
 
 import uk.co.samatkins.Entity;
 import uk.co.samatkins.RotatedRectangle;
 
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Rectangle;
 
 public class Beard extends Entity {
 	
 	private float[][] hairs;
 	private int hairsX = 20;
 	private int hairsY = 20;
 	
 	private final float hairSpacing = 10;
 	
 	private ShapeRenderer shapeRenderer;
 
 	public Beard(int x, int y, int width, int height) {
 		shapeRenderer = new ShapeRenderer();
 		
 		setBounds(x,y,width,height);
 		
 		hairsX = (int) Math.ceil(width / hairSpacing);
 		hairsY = (int) Math.ceil(height / hairSpacing);
 		
 		hairs = new float[hairsX][hairsY];
 		for (int i=0; i<hairsX; i++) {
 			for (int j=0; j<hairsY; j++) {
 				hairs[i][j] = 10;
 			}
 		}
 	}
 	
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 //		super.draw(batch, parentAlpha);
 		batch.end();
 		
 		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
 		shapeRenderer.identity();
 		
 		shapeRenderer.begin(ShapeType.Line);
 		shapeRenderer.setColor(getColor());
 		float x1,x2,y1,y2;
 		for (int i=0; i<hairsX; i++) {
 			x1 = getX() + (hairSpacing * i);
 			x2 = x1 + hairSpacing/2;
 			
 			for (int j=0; j<hairsY; j++) {
 				y1 = getY() + (hairSpacing * j);
 				y2 = y1 + hairs[i][j];
 				
 				shapeRenderer.line(x1, y1, x2, y2);
 			}
 		}
 		
 		shapeRenderer.end();
 		
 		batch.begin();
 	}
 	
 	/**
 	 * Slice `amount` off the hair at the given coordinate
 	 * @param x
 	 * @param y
 	 * @param amount
 	 */
 	public void shavePoint(int x, int y, float amount) {
 		// Is coord invalid?
 		if (x < 0 || x >= hairsX || y < 0 || y >= hairsY) {
 			return;
 		}
 		
 		// No hair here, ignore!
 		if (hairs[x][y] <= 0) {
 			return;
 		}
 		
 		if (amount >= hairs[x][y]) {
 			hairs[x][y] = 0;
 			return;
 		}
 		
 		hairs[x][y] -= amount;
 	}
 
 	public void shave(RotatedRectangle area) {
 		Rectangle bounds = area.getBoundingRectangle();
 		Polygon poly = area.getPolygon();
 		
 		int startX = (int) Math.floor((bounds.x - getX()) / hairSpacing) - 1;
 		int startY = (int) Math.floor((bounds.y - getY()) / hairSpacing) - 1;
 		int endX = (int) Math.ceil(bounds.width / hairSpacing) + startX + 2;
 		int endY = (int) Math.ceil(bounds.height / hairSpacing) + startY + 2;
 		
 		for (int x = startX; x < endX; x++) {
 			for (int y = startY; y < endY; y++) {
				if (poly.contains(x*hairSpacing, y*hairSpacing)) {
 					shavePoint(x,y, 1);
 				}
 			}
 		}
 	}
 }
