 package com.angrykings;
 
 /**
  * GameConfig
  *
  * This class has public static final attributes that declare constants for the game.
  *
  * @author Shivan Taher <zn31415926535@gmail.com>
  * @date 06.06.13
  */
 
 public final class GameConfig {
 	public static final int CAMERA_WIDTH = 960;
 	public static final int CAMERA_HEIGHT = 540;
 	public static final int CAMERA_X = -520;
 	public static final int CAMERA_Y = 520;
 
 	public static final float CAMERA_STARTUP_ZOOM = 0.75f;
 	public static final float CAMERA_ZOOM_MIN = 0.5f;
 	public static final float CAMERA_ZOOM_MAX = 1f;
 
 	public static final float CAMERA_MIN_X = -1000;
 	public static final float CAMERA_MAX_X = 950;
 	public static final float CAMERA_MIN_Y = 0;
 	public static final float CAMERA_MAX_Y = 1200;
 
 	public static final boolean LOG_FPS = false;
 
 	public static final int PHYSICS_STEPS_PER_SEC = 60;
 	public static final int PHYSICS_VELOCITY_ITERATION = 15;
 	public static final int PHYSICS_POSITION_ITERATION = 5;
 	public static final int PHYSICS_MAX_STEPS_PER_UPDATE = 1;
 
 
 	public static final float CANNON_FORCE = 15;
 	public static final float CANNONBALL_TIME_SEC = 5.0f;
 
	public static final String WEBSERVICE_URI = "ws://spaeti.pavo.uberspace.de:61224"; // "ws://141.64.161.253:8008"
 	public static final int WEBSOCKET_MAX_PAYLOAD_SIZE = 1024*1024*6;
 	public static final int WEBSOCKET_MAX_FRAME_SIZE = 1024*1024*10;
 }
