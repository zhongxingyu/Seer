 /**
  * @author Andreyuk Artyom happydroidx@gmail.com
  * @version 1.0
  */
 package controller;
 
 import org.lwjgl.util.vector.Vector2f;
 import main.Engine;
 
 class Mouse implements Engine {
 
 	private Cursor cursor;
 
 	Mouse() {
 		cursor = new Cursor();
 	}
 
 	@Override
 	public void tick() {
 		cursor.setPosition(new Vector2f(org.lwjgl.input.Mouse.getX(),
 				org.lwjgl.input.Mouse.getY()));
 		cursor.setIsPressed(org.lwjgl.input.Mouse.isButtonDown(0), false);
 	}
 
 	@Override
 	public void cleanUp() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Cursor getCursor() {
 		return cursor;
 	}
 
 }
