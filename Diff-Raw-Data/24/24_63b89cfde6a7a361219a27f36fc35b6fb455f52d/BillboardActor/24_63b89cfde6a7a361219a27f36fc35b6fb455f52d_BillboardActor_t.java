 package com.razh.tiling;
 
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 
 public class BillboardActor extends Actor3D {
 	private static Mesh sMesh;
 
 	public BillboardActor() {
 		if (sMesh == null) {
 			sMesh = Geometry.createBillboard();
 		}
 	}
 
 	public void draw(ShaderProgram shaderProgram) {
 		shaderProgram.setUniformf("color", getColor());
 		shaderProgram.setUniformf("translate", getPosition());
 		shaderProgram.setUniformf("scale", getWidth(), getHeight(), getDepth());
 		sMesh.render(shaderProgram, GL20.GL_TRIANGLE_STRIP);
 	}
 
 	@Override
 	public Actor hit(float x, float y, boolean touchable) {
 		if (touchable && getTouchable() != Touchable.enabled) {
 			return null;
 		}
 
 		if (Math.abs(x - getX()) <= 0.5f * getWidth() &&
		    Math.abs(y - getY()) <= 0.5f * getHeight()) {
 			return this;
 		}
 
 		return null;
 	}
 }
