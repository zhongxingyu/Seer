 package com.deeter.utility;
 
 public interface Camera {
 
 	public void processMouse();
 	
 	public void processMouse(float mouseSpeed);
 	
 	public void processMouse(float mouseSpeed, float maxLookUp, float maxLookDown);
 	
 	public void processKeyboard(float delta);
 	
 	public void processKeyboard(float delta, float speed);
 	
 	public void processKeyboard(float delta, float speedX, float speedY, float speedZ);
 	
 	public void moveFromLook(float dx, float dy, float dz);
 	
 	public void setPosition(float x, float y, float z);
 	
 	public void applyOptimalStates();
 	
	public void applyOrthographicMatrix();
	
	public void applyPerspectiveMatrix();
	
 	public void applyPerspectiveMatrix(int uniformLocation);
 	
 	public void applyTranslations(int uniformLocation);
 	
 	public void setRotation(float pitch, float yaw, float roll);
 	
 	public float x();
 	
 	public float y();
 	
 	public float z();
 	
 	public float pitch();
 	
 	public float yaw();
 	
 	public float roll();
 	
 	public float fieldOfView();
 	
 	public void setFieldOfView(float fov);
 	
 	public void setAspectRatio(float aspectRatio);
 	
 	public float aspectRatio();
 	
 	public float nearClippingPlane();
 	
 	public float farClippingPlane();
 }
