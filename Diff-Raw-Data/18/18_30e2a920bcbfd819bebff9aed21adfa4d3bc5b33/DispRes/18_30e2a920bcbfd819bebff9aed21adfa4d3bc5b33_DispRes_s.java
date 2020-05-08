 package com.thorgaming.throwme.displayobjects;
 
 import android.graphics.Canvas;
 import android.graphics.ColorMatrix;
 import android.graphics.ColorMatrixColorFilter;
 import android.graphics.drawable.Drawable;
 
 import com.thorgaming.throwme.ThrowMe;
 import com.thorgaming.throwme.drawing.Camera;
 
 public class DispRes extends DispObj {
 
 	protected Drawable drawable;
 	protected ColorMatrix colorMatrix = new ColorMatrix();
 	private float angle = 0;
 
 	public DispRes(int drawableId) {
 		setDrawable(drawableId);
 	}
 
 	public void setDrawable(int drawableId) {
 		drawable = ThrowMe.getInstance().stage.getResources().getDrawable(drawableId);
 	}
 
 	public int getScreenX() {
 		return hitBox.left;
 	}
 
 	public int getScreenY() {
 		return hitBox.top;
 	}
 
 	public void setColorMatrix(ColorMatrix colorMatrix) {
 		this.colorMatrix = colorMatrix;
 	}
 
 	public DispRes setAngle(float angle) {
 		this.angle = angle;
 		return this;
 	}
 
 	@Override
 	public void draw(Canvas canvas, Camera camera) {
 		hitBox.set(camera.transformX(getX()), camera.transformY(getY()), camera.transformX(getX() + getWidth()), camera.transformY(getY() + getHeight()));

 		canvas.save();
 		canvas.rotate(angle, hitBox.exactCenterX(), hitBox.exactCenterY());
 		drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
 		drawable.setBounds(hitBox);
 		drawable.setAlpha(getAlpha());
 		drawable.draw(canvas);
 		canvas.restore();
 	}
 }
