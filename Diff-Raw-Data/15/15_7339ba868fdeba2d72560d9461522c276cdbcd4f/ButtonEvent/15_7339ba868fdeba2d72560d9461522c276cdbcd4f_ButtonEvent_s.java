 /* Copyright (c) 2007, 2008 Bug Labs, Inc.
  * All rights reserved.
  *   
  * This program is free software; you can redistribute it and/or  
  * modify it under the terms of the GNU General Public License version  
  * 2 only, as published by the Free Software Foundation.   
  *   
  * This program is distributed in the hope that it will be useful, but  
  * WITHOUT ANY WARRANTY; without even the implied warranty of  
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
  * General Public License version 2 for more details (a copy is  
  * included at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).   
  *   
  * You should have received a copy of the GNU General Public License  
  * version 2 along with this work; if not, write to the Free Software  
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
  * 02110-1301 USA   
  *
  */
 package com.buglabs.device;
 
 /**
  * Represents a physical key event.
  * 
  * @author ken
  * 
  */
 public class ButtonEvent {
 	public static final int BUTTON_HOTKEY_1 = 258;
 
 	public static final int BUTTON_HOTKEY_2 = 259;
 
 	public static final int BUTTON_HOTKEY_3 = 260;
 
 	public static final int BUTTON_HOTKEY_4 = 261;
 
 	public static final int BUTTON_LEFT = 263;
 
 	public static final int BUTTON_RIGHT = 262;
 	
 	public static final int BUTTON_UP = 264;
 
 	public static final int BUTTON_DOWN = 265;
 	
 	public static final int BUTTON_CAMERA_ZOOM_IN = 258;
 	
 	public static final int BUTTON_CAMERA_ZOOM_OUT = 257;
 	
 	public static final int BUTTON_CAMERA_SHUTTER = 256;
 
 	public static final int BUTTON_SELECT = 257;
 	
	public static final int BUTTON_AUDIO_1 = 266;
 	
	public static final int BUTTON_AUDIO_2 = 267;
 	
 	public static final int KEY_DOWN = 1;
 	
 	public static final int KEY_UP = 0;
 	
 	private final int rawValue;
 
 	private final float duration;
 
 	private final int button;
 	
 	private final long action;
 	
 	private final String source;
 
 	public ButtonEvent(int key) {
 		this.rawValue = key;
 		this.duration = 0;
 		this.button = 0;
 		this.action = 0;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = 0;
 		this.action = 0;
 		this.source = null;
 	}
 
 	public ButtonEvent(int rawValue, float duration, int button) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = 0;
 		this.source = null;
 	}
 	
 	public ButtonEvent(int rawValue, float duration, int button, long action) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = action;
 		this.source = null;
 	}
 	
 	public ButtonEvent(int rawValue, float duration, int button, long action, String source) {
 		this.rawValue = rawValue;
 		this.duration = duration;
 		this.button = button;
 		this.action = action;
 		this.source = source;
 	}
 
 	/**
 	 * @return Code for button pressed.
 	 */
 	public int getButton() {
 		return button;
 	}
 
 	/**
 	 * @return
 	 * This value is not used.  Each button event is discreet.
 	 * @deprecated
 	 */
 	public float getDuration() {
 		return duration;
 	}
 
 	/**
 	 * @return The raw code from the event provider.
 	 */
 	public int getRawValue() {
 		return rawValue;
 	}
 	
 	/**
 	 * @return
 	 */
 	public long getAction() {
 		return action;
 	}
 	
 	/**
 	 * @return The source of the event.
 	 */
 	public String getSource() {
 		return source;
 	}
 }
