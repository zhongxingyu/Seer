 package uk.co.jwlawson.aniguffins;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 
 public class NinePatchActor extends Actor {
 
 	private NinePatch mNinePatch;
 
 	public NinePatchActor(NinePatch patch) {
 		mNinePatch = patch;
 		generateName();
 	}
 
 	/** Generate a random name, so that every actor has one to identify it */
 	private void generateName() {
		setName(String.valueOf(System.currentTimeMillis()));
 	}
 
 	public void setNinePatch(NinePatch patch) {
 		mNinePatch = patch;
 	}
 
 	@Override
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		Color color = getColor();
 		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
 		mNinePatch.draw(batch, getX(), getY(), getWidth(), getHeight());
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
