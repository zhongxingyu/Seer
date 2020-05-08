 package cs5625.deferred.apps;
 
 import java.awt.Point;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseWheelEvent;
 import java.util.ArrayList;
 
 import javax.vecmath.AxisAngle4f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Quat4f;
 
 import cs5625.deferred.catmullclark.CCSubdiv;
 import cs5625.deferred.datastruct.EdgeDS;
 import cs5625.deferred.defaultGeometry.CubeQuadMesh;
 import cs5625.deferred.defaultGeometry.Tetrahedron;
 import cs5625.deferred.defaultGeometry.ThreeTriangleMesh;
 import cs5625.deferred.defaultGeometry.TwoTriangleMesh;
 import cs5625.deferred.loop.LoopSubdiv;
 import cs5625.deferred.materials.UnshadedMaterial;
 import cs5625.deferred.misc.Util;
 import cs5625.deferred.scenegraph.Geometry;
 import cs5625.deferred.scenegraph.Mesh;
 import cs5625.deferred.scenegraph.PointLight;
 import cs5625.deferred.scenegraph.Quadmesh;
 import cs5625.deferred.scenegraph.SceneObject;
 import cs5625.deferred.scenegraph.Trimesh;
 
 /**
  * DefaultSceneController.java
  * 
  * The default scene controller creates a simple scene and allows the user to orbit the camera 
  * and preview the renderer's gbuffer.
  * 
  * Drag the mouse to orbit the camera, and scroll to zoom. Numbers 1-9 preview individual gbuffer 
  * textures, and 0 views the shaded result.
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2012, Computer Science Department, Cornell University.
  * 
  * @author Asher Dunn (ad488)
  * @author Rohit Garg (rg534)
  * @date 2012-03-23
  */
 public class SubdivSceneController extends SceneController
 {
 	/* Keeps track of camera's orbit position. Latitude and longitude are in degrees. */
 	private float mCameraLongitude = 50.0f, mCameraLatitude = -40.0f;
 	private float mCameraRadius = 15.0f;
 	
 	/* Used to calculate mouse deltas to orbit the camera in mouseDragged(). */ 
 	private Point mLastMouseDrag;
 
 	private Mesh visibleMesh;
 	private Trimesh loopMesh;
 	private Quadmesh ccMesh;
 	private boolean isLoop;
 	
 	private void updateSceneGraph()
 	{
 		try
 		{
 		
 			//make it part of scenegraph
 			Geometry geom = new Geometry();
 			geom.addMesh(visibleMesh);
 			
 			ArrayList<Geometry> list = new ArrayList<Geometry>();
 			
 			list.add(geom);
 			
 			mSceneRoot = new SceneObject();
 			mSceneRoot.addGeometry(list);
 			//make it unshaded
 			((Geometry)mSceneRoot.getChildren().get(0)).getMeshes().get(0).setMaterial(new UnshadedMaterial());
 			
 			/* Add an unattenuated point light to provide overall illumination. */
 			PointLight light = new PointLight();
 			
 			light.setConstantAttenuation(1.0f);
 			light.setLinearAttenuation(0.0f);
 			light.setQuadraticAttenuation(0.0f);
 			
 			light.setPosition(new Point3f(50.0f, 180.0f, 100.0f));
 			
 			mSceneRoot.addChild(light);
 
 		}
 		catch (Exception err)
 		{
 			/* If anything goes wrong, just die. */
 			err.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 	@Override
 	public void initializeScene()
 	{
 		try
 		{
 			// TODO PA5: This is where you change the base meshes.
 			ccMesh = new CubeQuadMesh().getQuadMesh();
			loopMesh = new ThreeTriangleMesh().getTriMesh();
			//loopMesh = (Trimesh)Geometry.load("models/example_cube_tris.obj", true, true).get(0).getMeshes().get(0);
 			
 			visibleMesh = loopMesh;
 			isLoop = true;
 			
 			this.updateSceneGraph();
 		}
 		catch (Exception err)
 		{
 			/* If anything goes wrong, just die. */
 			err.printStackTrace();
 			System.exit(-1);
 		}
 		
 		/* Initialize camera position. */
 		updateCamera();
 	}	
 	
 	/**
 	 * Updates the camera position and orientation based on orbit parameters.
 	 */
 	private void updateCamera()
 	{
 		/* Compose the "horizontal" and "vertical" rotations. */
 		Quat4f longitudeQuat = new Quat4f();
 		longitudeQuat.set(new AxisAngle4f(0.0f, 1.0f, 0.0f, mCameraLongitude * (float)Math.PI / 180.0f));
 		
 		Quat4f latitudeQuat = new Quat4f();
 		latitudeQuat.set(new AxisAngle4f(1.0f, 0.0f, 0.0f, mCameraLatitude * (float)Math.PI / 180.0f));
 
 		mCamera.getOrientation().mul(longitudeQuat, latitudeQuat);
 		
 		/* Set the camera's position so that it looks towards the origin. */
 		mCamera.setPosition(new Point3f(0.0f, 0.0f, mCameraRadius));
 		Util.rotateTuple(mCamera.getOrientation(), mCamera.getPosition());
 	}
 
 	@Override
 	public void keyTyped(KeyEvent key)
 	{
 		super.keyTyped(key);
 		
 		char c = key.getKeyChar();
 		if (c == 'n')
 		{
 			if (isLoop)
 			{
 				EdgeDS edgeDS = new EdgeDS(loopMesh);
 				LoopSubdiv loopSubdiv = new LoopSubdiv(edgeDS);
 				loopMesh = (Trimesh)loopSubdiv.getNewMesh();
 			}
 			else
 			{
 				EdgeDS edgeDS = new EdgeDS(ccMesh);
 				CCSubdiv ccSubdiv = new CCSubdiv(edgeDS);
 				ccMesh = (Quadmesh)ccSubdiv.getNewMesh();
 			}
 			
 			visibleMesh = isLoop ? loopMesh : ccMesh;
 			updateSceneGraph();
 			requiresRender();
 		}
 		else if (c == 'm')
 		{
 			isLoop = !isLoop;
 			
 			visibleMesh = isLoop ? loopMesh : ccMesh;
 			updateSceneGraph();
 			requiresRender();
 		}
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseWheelEvent mouseWheel) {
 		/* Zoom in and out by the scroll wheel. */
 		mCameraRadius += mouseWheel.getUnitsToScroll();
 		updateCamera();
 		requiresRender();
 	}
 
 	@Override
 	public void mousePressed(MouseEvent mouse)
 	{
 		/* Remember the starting point of a drag. */
 		mLastMouseDrag = mouse.getPoint();
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent mouse)
 	{
 		/* Calculate dragged delta. */
 		float deltaX = -(mouse.getPoint().x - mLastMouseDrag.x);
 		float deltaY = -(mouse.getPoint().y - mLastMouseDrag.y);
 		mLastMouseDrag = mouse.getPoint();
 		
 		/* Update longitude, wrapping as necessary. */
 		mCameraLongitude += deltaX;
 		
 		if (mCameraLongitude > 360.0f)
 		{
 			mCameraLongitude -= 360.0f;
 		}
 		else if (mCameraLongitude < 0.0f)
 		{
 			mCameraLongitude += 360.0f;
 		}
 		
 		/* Update latitude, clamping as necessary. */
 		if (Math.abs(mCameraLatitude + deltaY) <= 89.0f)
 		{
 			mCameraLatitude += deltaY;
 		}
 		else
 		{
 			mCameraLatitude = 89.0f * Math.signum(mCameraLatitude);
 		}
 	
 		updateCamera();
 		requiresRender();
 	}
 }
