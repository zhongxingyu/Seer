 package com.utc.graphemobile.input;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.input.GestureDetector.GestureListener;
 import com.badlogic.gdx.math.Vector2;
 import com.utc.graphemobile.screen.IGrapheScreen;
 
 public class GrapheGestureListener implements GestureListener {
 
 	private OrthographicCamera camera;
 	private IGrapheScreen screen;
 	private float iniZoomCamera = 0;
 	private boolean isLastPanLeftMenu = false;
 	private boolean isLastPanRightMenu = false;
 
 	public GrapheGestureListener(OrthographicCamera camera, IGrapheScreen screen) {
 		this.camera = camera;
 		this.screen = screen;
 	}
 
 	@Override
 	public boolean touchDown(float x, float y, int pointer, int button) {
 		iniZoomCamera = camera.zoom;
 		// iniAngleCamera = camera.rotate(angle)
 
 		return false;
 	}
 
 	@Override
 	public boolean tap(float x, float y, int count, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean longPress(float x, float y) {
 		return false;
 	}
 
 	@Override
 	public boolean fling(float velocityX, float velocityY, int button) {
 		if (!ShowLeftMenuEventListener.isLeftMenuHidden && isLastPanLeftMenu) {
 			return screen.getUIStage().getLeftMenu().fling(velocityX, velocityY, button);
		} else if (screen.getUIStage().getRightMenu().isVisible() && isLastPanRightMenu) {
			return screen.getUIStage().getRightMenu().fling(velocityX, velocityY, button);
 		} 
 		return false;
 	}
 
 	@Override
 	public boolean pan(float x, float y, float deltaX, float deltaY) {
 		if (!ShowLeftMenuEventListener.isLeftMenuHidden
 				&& screen.getUIStage().getLeftMenu().containsX(x)) {
 			isLastPanLeftMenu = true;
 			return screen.getUIStage().getLeftMenu().pan(x, y, deltaX, deltaY);
		} else if (screen.getUIStage().getRightMenu().isVisible()
				&& screen.getUIStage().getRightMenu().containsX(x)) {
 			isLastPanRightMenu = true;
			return screen.getUIStage().getRightMenu().pan(x, y, deltaX, deltaY);
 		} else {
 			isLastPanLeftMenu = false;
 			camera.translate(-deltaX * camera.zoom, deltaY * camera.zoom);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean zoom(float initialDistance, float distance) {
 		camera.zoom = iniZoomCamera * (initialDistance / distance);
 		return false;
 	}
 
 	@Override
 	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
 			Vector2 pointer1, Vector2 pointer2) {
 		return false;
 	}
 }
