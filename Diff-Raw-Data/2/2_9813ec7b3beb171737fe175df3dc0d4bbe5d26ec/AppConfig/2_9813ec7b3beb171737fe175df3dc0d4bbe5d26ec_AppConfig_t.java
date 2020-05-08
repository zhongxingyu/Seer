 package ua.org.dector.moon_lander;
 
 /**
  * @author dector (dector9@gmail.com)
  */
 public interface AppConfig {
     public static final String TITLE        = "Moon Lander";
     public static final int SCREEN_WIDTH    = 800;
     public static final int SCREEN_HEIGHT   = 600;
 
     // Files
 
     public static final String DATA_DIR     = "data/";
     public static final String IMG_DIR      = "img/";
     public static final String SOUND_DIR    = "sound/";
     public static final String LEVELS_DIR   = "levels/";
     public static final String FONTS_DIR    = "fonts/";
     public static final String PARTICLES_DIR= "particles/";
 
     public static final String BURN_FILE        = "burn.wav";
     public static final String CRASH_FILE       = "crash.wav";
     public static final String LANDING_FILE     = "landing.wav";
     public static final String FADEIN_FILE      = "fadein.wav";
     public static final String MUSIC_FILE       = "Subterrestrial_-_Neighborhood_Arcade.ogg";
 
     public static final String SMALL_FONT_FILE  = "Sans-Light14.fnt";
     public static final String SMALL_FONT_IMG   = "Sans-Light14.png";
     public static final String BIG_FONT_FILE    = "Sans-Light48.fnt";
     public static final String BIG_FONT_IMG     = "Sans-Light48.png";
     public static final String GRAPHICS_FILE    = "graphics.png";
     public static final String SPLASH_FILE      = "splash.png";
 
     public static final String FIRE_EFFECT      = "fire.p";
 
    public static final float MUSIC_VOLUME      = .3f;
     public static final float SFX_VOLUME        = .5f;
 
     // Graphics
 
     public static final int GAMINATOR_LOGO_WIDTH    = 95;
     public static final int GAMINATOR_LOGO_HEIGHT   = 199;
 
     public static final int DEDICATION_WIDTH    = 148;
     public static final int DEDICATION_HEIGHT   = 256;
 
 
     public static final int ROCKET_TEXTURE_WIDTH    = 8;
     public static final int ROCKET_TEXTURE_HEIGHT   = 8;
 
     public static final int FIRE_TEXTURE_WIDTH      = 8;
     public static final int FIRE_TEXTURE_HEIGHT     = 8;
 
     public static final int POINTER_TEXTURE_WIDTH   = 8;
     public static final int POINTER_TEXTURE_HEIGHT  = 8;
 
     public static final int FLAG_TEXTURE_WIDTH      = 8;
     public static final int FLAG_TEXTURE_HEIGHT     = 8;
 
     public static final int SOUND_TEXTURE_WIDTH     = 8;
     public static final int SOUND_TEXTURE_HEIGHT    = 8;
 
 
     public static final int ROCKET_WIDTH    = 32;
     public static final int ROCKET_HEIGHT   = 32;
 
     public static final int FIRE_PADDING    = 8;
     public static final int FIRE_WIDTH      = 16;
     public static final int FIRE_HEIGHT     = 24;
 
     public static final int POINTER_WIDTH   = 8;
     public static final int POINTER_HEIGHT  = 16;
 
     public static final int FLAG_WIDTH      = 32;
     public static final int FLAG_HEIGHT     = 32;
 
     public static final int SOUND_ICO_WIDTH = 24;
     public static final int SOUND_ICO_HEIGHT = 24;
 
     public static final int SOUND_ICO_X     = 760;
     public static final int SOUND_ICO_Y     = 560;
 
 
     public static final int LANDING_PLATFORM_BORDER = 3;
     public static final int LANDING_PLATFORM_HEIGHT = 6;
 
     // Physics
 
     public static final int ROCKET_AY       = 3;
     public static final int ROCKET_ROTATING = 90;
     public static final int GRAVITY         = 1;
     public static final float FRICTION      = .7f;
 
     public static final float LANDING_VX_BOUND  = .15f;
     public static final float LANDING_VY_BOUND  = .2f;
     public static final int LANDING_DIFF_ANGLE  = 5;
 }
