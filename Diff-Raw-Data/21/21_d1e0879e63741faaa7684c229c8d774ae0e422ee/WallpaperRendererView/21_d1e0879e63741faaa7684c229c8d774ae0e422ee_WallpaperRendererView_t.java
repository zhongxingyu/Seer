 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.ui;
 
 import android.app.WallpaperManager;
 import android.content.Context;
 import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
 
 /**
  * Renderer view whose renderer size matches the system wallpaper size
  */
 public class WallpaperRendererView extends ScaledRenderView {
 	
 	private WallpaperManager wallpaperManager = WallpaperManager.getInstance(getContext());
 	
 	/**
 	 * Constructs a wallpaper renderer view
 	 * @param context the context
 	 * @param attrs the attributes
 	 */
 	public WallpaperRendererView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.ScaledRenderView#getDesiredRendererWidth(int)
 	 */
 	@Override
 	protected int getDesiredRendererWidth(int viewWidth) {
		int wpWidth = wallpaperManager.getDesiredMinimumWidth();
		
		return (wpWidth > 0) ? wpWidth : getDefaultDisplay().getWidth();
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.ScaledRenderView#getDesiredRendererHeight(int)
 	 */
 	@Override
 	protected int getDesiredRendererHeight(int viewHeight) {
		int wpHeight = wallpaperManager.getDesiredMinimumHeight();
		
		return (wpHeight > 0) ? wpHeight : getDefaultDisplay().getHeight();
	}
	
	/**
	 * Gets the default display
	 * @return the display
	 */
	private Display getDefaultDisplay() {
		WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
 	}
 }
