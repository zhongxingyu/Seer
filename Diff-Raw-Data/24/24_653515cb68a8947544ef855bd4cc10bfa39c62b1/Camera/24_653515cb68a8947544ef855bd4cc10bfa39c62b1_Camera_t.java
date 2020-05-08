 package com.detourgames.raw;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 
 public class Camera {
 	
 	/**
 	 * A wrapper for the libgdx camera. Might prove unnecessary.
 	 * */
 	
 	private OrthographicCamera mCamera;
 	
 	public Camera(float viewportWidth, float viewportHeight){
 		mCamera = new OrthographicCamera(viewportWidth, viewportHeight);
 		mCamera.position.set(viewportWidth/2f, viewportHeight/2f, 0);
 		mCamera.update();
 	}
 	
 	public void update() {
 		mCamera.translate(1f, 0, 0);
 		mCamera.update();
		//mCamera.apply(null);
 	}
 	
 	public OrthographicCamera getCamera(){
 		return mCamera;
 	}
 	
 }
