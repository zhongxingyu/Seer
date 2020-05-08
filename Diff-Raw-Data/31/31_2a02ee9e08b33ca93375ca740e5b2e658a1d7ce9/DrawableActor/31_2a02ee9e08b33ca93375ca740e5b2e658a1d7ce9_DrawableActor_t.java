 package uk.co.jwlawson.aniguffins;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class DrawableActor extends Actor {
 
 	private TextureRegion mTextureRegion;
 
 	public DrawableActor(TextureRegion tex) {
 		this.mTextureRegion = tex;
 		generateName();
 	}
 
 	/** Generate a random name, so that every actor has one to identify it */
 	private void generateName() {
		setName(String.valueOf(System.currentTimeMillis()));
 	}
 
 	public void setTextureRegion(TextureRegion tex) {
 		this.mTextureRegion = tex;
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		Color color = getColor();
 		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
 		batch.draw(mTextureRegion, getX(), getY(), getOriginX(), getOriginY(), getWidth(),
 				getHeight(), getScaleX(), getScaleY(), getRotation());
 	}
 
 	/*
 	 * toString would just return the UUID name, so override to something more
 	 * useful
 	 */
 	@Override
 	public String toString() {
 		String name = getClass().getName();
 		int dotIndex = name.lastIndexOf('.');
 		if (dotIndex != -1) name = name.substring(dotIndex + 1);
 
 		return name + " " + getX() + "," + getY() + " " + getWidth() + "x" + getHeight();
 	}
 
 }
