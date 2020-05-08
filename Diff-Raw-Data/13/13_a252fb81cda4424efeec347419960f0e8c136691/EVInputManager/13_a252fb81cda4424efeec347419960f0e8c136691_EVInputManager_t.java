 package com.evervoid.client;
 
 import com.jme3.math.Vector2f;
 
 public class EVInputManager
 {
 	private static EVInputManager sInstance;
 
 	protected static EVInputManager getInstance()
 	{
 		if (sInstance == null) {
 			sInstance = new EVInputManager();
 		}
 		return sInstance;
 	}
 
 	public static boolean shiftPressed()
 	{
 		return getInstance().aShiftPressed;
 	}
 
 	private boolean aShiftPressed = false;
 
 	private EVInputManager()
 	{
 	}
 
 	public void onAction(final String name, final boolean isPressed, final float tpf, final Vector2f position)
 	{
 		if (name.equals("Mouse left")) {
 			// Forward left clicks to game view
 			if (isPressed) {
 				EVViewManager.onLeftClick(position, tpf);
 			}
 			else {
 				EVViewManager.onLeftRelease(position, tpf);
 			}
 		}
 		else if (name.equals("Mouse right")) {
 			// Forward right clicks to game view
 			if (isPressed) {
 				EVViewManager.onRightClick(position, tpf);
 			}
 			else {
 				EVViewManager.onRightRelease(position, tpf);
 			}
 		}
 		final KeyboardKey key = KeyboardKey.fromMapping(name);
 		if (key != null) {
 			if (key.isShift()) {
 				aShiftPressed = isPressed;
 			}
 			if (isPressed) {
				if (!EVViewManager.onKeyPress(key, tpf) && key.equals(KeyboardKey.ESCAPE)) {
					EverVoidClient.quit();
 				}
 			}
 			else {
 				EVViewManager.onKeyRelease(key, tpf);
 			}
 		}
 	}
 
 	public void onAnalog(final String name, final float delta, final float tpf, final Vector2f position)
 	{
 		if (name.equals("Mouse move")) {
 			// Forward mouse movement to game view
 			EVViewManager.onMouseMove(position, tpf);
 		}
 		else if (name.equals("Mouse wheel up")) {
 			EVViewManager.onMouseWheelUp(delta, tpf, position);
 		}
 		else if (name.equals("Mouse wheel down")) {
 			EVViewManager.onMouseWheelDown(delta, tpf, position);
 		}
 	}
 }
