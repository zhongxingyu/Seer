 /* 
  * Copyright 2011 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.galactogolf.specificobjectmodel;
 
 import com.galactogolf.R;
 
 /**
  * Various constants relevant to the game world
  * 
  */
 public class GameConstants {
 
 	// menu item constants
 	public static final int MENU_PLAY_LEVEL = 2;
 	public static final int MENU_NEXT_LEVEL = 4;
 	public static final int MENU_RETRY_LEVEL = 5;
 	public static final int MENU_EXIT = 6;
 	public static final int MENU_ADD_PLANET = 7;
 	public static final int MENU_ADD_BARRIER = 8;
 	public static final int MENU_ADD_BOUNCE_BARRIER = 9;
 	public static final int MENU_ADD_MOON = 10;
 	public static final int MENU_ADD_STAR = 11;
 	public static final int MENU_ADD_SUN = 12;
 	public static final int MENU_ADD_SATURN = 13;
 	public static final int MENU_DELETE_OBJECT = 14;
 	public static final int MENU_PREV_LEVEL = 15;
 	public static final int MENU_SAVE_LEVEL = 16;
 	public static final int MENU_ADD_WORMHOLE = 17;
 	public static final int MENU_NEW_LEVEL = 18;
 	public static final int MENU_RESET_LEVEL = 19;
 	public static final int MENU_DELETE_SELECTED = 20;
 	public static final int MENU_REPLAY_LEVEL = 21;
 
 	public static int[][] FRAMES_MOON = new int[][] { { R.drawable.moon } };
 	public static int[][] FRAMES_BARRIER = new int[][] { {
 			R.drawable.barrier_frame0, R.drawable.barrier_frame1,
			R.drawable.barrier_frame2, R.drawable.barrier_frame1 } };
 	public static int[][] FRAMES_BOUNCE_BARRIER = new int[][] { {
 			R.drawable.bounce_barrier_frame0, R.drawable.bounce_barrier_frame1,
			R.drawable.bounce_barrier_frame2, R.drawable.bounce_barrier_frame1 } };
 	public static int[][] FRAMES_PLANET = new int[][] { { R.drawable.planet } };
 	public static int[][] FRAMES_STAR = new int[][] { { R.drawable.star } };
 	public static int[][] FRAMES_SMALL_STAR = new int[][] { { R.drawable.small_star } };
 	public static int[][] FRAMES_SMALL_STAR2 = new int[][] { { R.drawable.small_star2 } };
 	public static int[][] FRAME_WORMHOLE = new int[][] { { R.drawable.wormhole } };
 	public static int[][] FRAMES_ROCKET = new int[][] {
 			{ R.drawable.rocket_frame0, R.drawable.rocket_frame1 },
 			{ R.drawable.explosion_frame0, R.drawable.explosion_frame1,
 					R.drawable.explosion_frame2, R.drawable.explosion_frame3 },
 					{ R.drawable.rocket_frame0,  R.drawable.rocket_frame0, R.drawable.rocket_frame0,R.drawable.rocket_warp_frame0, 
 						R.drawable.rocket_warp_frame1,
 						R.drawable.rocket_warp_frame2, R.drawable.rocket_warp_frame3, R.drawable.rocket_warp_frame4,
 						R.drawable.rocket_warp_frame5,R.drawable.rocket_warp_frame6,R.drawable.rocket_frame0 }};
 	public static final int[][] FRAMES_ARROW = new int[][] {
 			{ R.drawable.arrow }, { R.drawable.arrow2 } };
 	public static final int[][] FRAMES_DEBRIS1 = new int[][] { { R.drawable.debris1 } };
 	public static final int[][] FRAMES_DEBRIS2 = new int[][] { { R.drawable.debris2 } };
 	public static final int[][] FRAMES_DEBRIS3 = new int[][] { { R.drawable.debris3 } };
 	public static final int[][] FRAMES_SUN = new int[][] { { R.drawable.sun } };;
 	public static final int[][] FRAMES_SATURN = new int[][] { { R.drawable.saturn } };;
 	public static final int[][] HAZE_SPRITE = new int[][] { { R.drawable.haze } };
 	public static final int[][] FRAMES_BONUS = new int[][] { { R.drawable.bonus } };
 	public static final int[][] FRAMES_POWER_HANDLE = new int[][] { { R.drawable.power_handle } };
 	public static final int[][] FRAMES_RESET_BUTTON = new int[][] { { R.drawable.reset_button } };
 	public static final int[][] FRAMES_INFOBAR = new int[][] { { R.drawable.info_bar } };
 
 	public static final int TEXTPRINTER_REGULAR = 0;
 	public static final int TEXTPRINTER_SMALL = 0;
 	public static final float TIME_SCALE = 0.30000f;
 
 	public static final String LOCATION_OF_LEVELS_CREATED_BY_USER = "levels_created_by_this_user";
 	public static final int POWER_ARROW_RED_FRAMESET = 0;
 	public static final int POWER_ARROW_BLUE_FRAMESET = 1;
 
 }
