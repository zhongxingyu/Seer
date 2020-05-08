 package com.bensoft.mulg2;
 
 
 /**
  * 
  * @author Ben Wolsieffer
  */
 public final class Constants {
 
     // Engine constants
     public static final int MAX_FPS = 40;
     public static final int MAX_PHYSICS_FPS = MAX_FPS;
 
     // World constants
     public static final float PIXELS_PER_METER = 16f;
     public static final float MAX_FLOOR_FRICTION = 1.5f;
    public static final boolean ALLOW_DEATH = false;
 
     // Acceleration data constants
     public static final float GRAVITY_MULTIPLIER = 1.0f;
     public static final boolean ACCELERATION_OVERRIDE_DEFAULT = false;
 
     // Ball constants
     public static final float BALL_RADIUS_METERS = 0.375f;
     public static final float BALL_RADIUS_PIXELS = BALL_RADIUS_METERS * PIXELS_PER_METER;
     public static final float BALL_DENSITY = 3.0f;
     public static final float BALL_ELASTICITY = 0.4f;
     public static final float BALL_FRICTION = 0f;
     public static final int BALL_Z_INDEX = 3;
     // Tile constants
     public static final float TILE_WIDTH_METERS = 1.0f;
     public static final float TILE_WIDTH_PIXELS = TILE_WIDTH_METERS * PIXELS_PER_METER;
     public static final int TILE_ANIMATION_FRAME_TIME = 200;
     public static final int TILE_Z_INDEX = 1;
     // Solid tile constants
     public static final float SOLID_TILE_DENSITY = 12.0f;
     public static final float SOLID_TILE_ELASTICITY = 0.0f;
     public static final float SOLID_TILE_FRICTION = 0.0f;
     // Stone tile constants
     public static final float STONE_TILE_SURFACE_FRICTION = 0.25f;
     // Ice tile constants
     public static final float ICE_TILE_SURFACE_FRICTION = 0.0f;
     // Tilting ramp constants
     public static final float TILTING_RAMP_FORCE = 10.0f;
     // Bouncer tile constants
     public static final float BOUNCER_TILE_FORCE = 7.0f;
     public static final float BOUNCER_TILE_FLASH_TIME = 0.1f;
     // Bump tile constants
     public static final float BUMP_TILE_FORCE_MULTIPLIER = 1.0f;
     // Dimple tile constants
     public static final float DIMPLE_TILE_FORCE_MULTIPLIER = 1.0f;
     // Movable tile constants
     public static final int MOVABLE_TILE_Z_INDEX = 2;
     // Heavy movable tile constants
     public static final float HEAVY_MOVABLE_TILE_MOVEMENT_VELOCITY = 1.8f;
     // Light movable tile constants
     public static final float LIGHT_MOVABLE_TILE_MOVEMENT_VELOCITY = 1.0f;
     // Game screen constants
     public static final int GAME_SCREEN_WIDTH_TILES = 10;
     public static final int GAME_SCREEN_HEIGHT_TILES = 9;
     public static final float GAME_SCREEN_INVENTORY_HEIGHT = 20f;
     public static final float GAME_SCREEN_HEIGHT_PIXELS = GAME_SCREEN_HEIGHT_TILES * TILE_WIDTH_PIXELS + GAME_SCREEN_INVENTORY_HEIGHT;
     public static final float GAME_SCREEN_WIDTH_PIXELS = GAME_SCREEN_WIDTH_TILES * TILE_WIDTH_PIXELS;
 
     private Constants() {
     }
 }
