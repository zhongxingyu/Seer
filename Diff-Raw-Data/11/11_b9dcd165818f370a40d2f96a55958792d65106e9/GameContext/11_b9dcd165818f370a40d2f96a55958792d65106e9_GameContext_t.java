 package com.wnezros.hunger;
 
 public class GameContext {
 	private static final int ACTIVATE = 0;
 	private static final int CLOSE = 1;
 	private static final int GOT_FOCUS = 2;
 	private static final int LOST_FOCUS = 3;
 	private static final int KEY_DOWN = 4;
 	private static final int KEY_UP = 5;
 	private static final int MOUSE_MOVE = 6;
 	private static final int MOUSE_DOWN = 7;
 	private static final int MOUSE_UP = 8;
 	private static final int DRAW = 9;
 	private static final int RESIZE = 10;
 	private static final int SAVE_GAME = 11;
 	
 	private static final int _MaxPointers = 5;
 	private static int _PointersId[];
 	
 	private static native void main();
 	private static native void event(int event, int p1, int p2, int p3);
 	
     public static void init() {
     	_PointersId = new int[_MaxPointers];
     	for(int i = 0; i < _MaxPointers; i++)
     		_PointersId[i] = -1;
     	
     	main();
     }
 	
 	public static void resize(int w, int h) {
     	event(RESIZE, w, h, 0);
     }
     
     public static void render() {
     	event(DRAW, 0, 0, 0);
     }
     
     public static void close() {
     	event(CLOSE, 0, 0, 0);
     }
     
     public static void resume() {
     	event(GOT_FOCUS, 0, 0, 0);
     }
     
     public static void pause() {
     	event(LOST_FOCUS, 0, 0, 0);
     }
     
     public static void keyDown(int key) {
     	event(KEY_DOWN, key, 0, 0);
     }
     
     public static void keyUp(int key) {
     	event(KEY_UP, key, 0, 0);
     }
     
     public static void mouseMove(int x, int y, int pointer) {
     	for(int i = 0; i < _MaxPointers; i++)
     		if(_PointersId[i] == pointer) {
     			event(MOUSE_MOVE, x, y, i+1);
     			return;
     		}
     }
     
     public static void mouseDown(int key, int x, int y) {
     	for(int i = 0; i < _MaxPointers; i++)
     		if(_PointersId[i] == key) {
     			event(MOUSE_DOWN, i+1, x, y);
     			return;
     		}
     	
     	for(int i = 0; i < _MaxPointers; i++)
     		if(_PointersId[i] == -1) {
     			_PointersId[i] = key;
     			event(MOUSE_DOWN, i+1, x, y);
     			return;
     		}
     }
     
     public static void mouseUp(int key, int x, int y) {
     	for(int i = 0; i < _MaxPointers; i++)
     		if(_PointersId[i] == key) {
     			_PointersId[i] = -1;
     			event(MOUSE_UP, i+1, x, y);
     			return;
     		}
     }
     
     public static void mouseCancel() {
     	for(int i = 0; i < _MaxPointers; i++) {
     		if(_PointersId[i] != -1) event(MOUSE_UP, i+1, 0, 0);
     		_PointersId[i] = -1;
     	}
     }
     
     public static void saveGameAndExit() {
     	event(SAVE_GAME, 0, 0, 0);
     }
     
     static {
     	System.loadLibrary("hunger");
     }
 }
