 package com.chris.spacedragon;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector3;
 
 public class ChaseCam extends PerspectiveCamera {
 
 	public Vector3 lookAtPosition;
 	public Quaternion lookAtOrientation;
 	public Quaternion orientation = new Quaternion(0, 0, 0, 1);
 	public Quaternion orientationConj = new Quaternion(0, 0, 0, 1);
 	
 	public Matrix4 skyBoxMatrix = new Matrix4();
 
 	public void update(float dt) {
 //		Vector3 campos = lookAtPosition.cpy();
 //		campos.z += 10;
 //		campos.y += 5;
 		
 		Vector3 dest = lookAtPosition.tmp();
 		dest.set(0, 1, 3);
 		lookAtOrientation.transform(dest);
 		dest.add(lookAtPosition);
 		lookAt(lookAtPosition.x, lookAtPosition.y, lookAtPosition.z);
 		
 		dest.sub(position);
 		dest.mul(dt * 0.3f);
 		position.add(dest);
 		
 		orientation.slerp(lookAtOrientation, dt * 0.3f);
 		orientationConj.set(orientation);
 		orientationConj.conjugate();
 		
 		float aspect = viewportWidth / viewportHeight;
 		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
 		view.set(orientationConj);
 		dest.set(position);
 		dest.mul(-1);
 		view.translate(dest);
 		combined.set(projection);
 		Matrix4.mul(combined.val, view.val);
 		invProjectionView.set(combined);
 		Matrix4.inv(invProjectionView.val);
 		frustum.update(invProjectionView);
 	}
 	
 	public Matrix4 getSkyboxMatrix() {
 		float aspect = viewportWidth / viewportHeight;
 		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
 		view.idt();
		view.scale(20, 20, 20);
 		view.rotate(orientationConj);
 		skyBoxMatrix.set(projection);
 		Matrix4.mul(skyBoxMatrix.val, view.val);
 		return skyBoxMatrix;
 	}
 
 	public ChaseCam(Vector3 destPos, Quaternion destOrient) {
 		super(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		lookAtPosition = destPos;
 		lookAtOrientation = destOrient;
 
 		position.set(0, 1.5f, 1);
 		lookAt(0, 0, 0);
 		up.set(0, 1, 0);
 		update();
 
 	}
 
 }
