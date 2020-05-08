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
 
 package com.ijuru.refract.activity;
 
 import java.io.IOException;
 
 import com.ijuru.refract.Constants;
 import com.ijuru.refract.R;
 import com.ijuru.refract.renderer.Mapping;
 import com.ijuru.refract.renderer.Palette;
 import com.ijuru.refract.renderer.RendererParams;
 import com.ijuru.refract.renderer.Renderer;
 import com.ijuru.refract.renderer.RendererListener;
 import com.ijuru.refract.ui.RendererView;
 import com.ijuru.refract.utils.Preferences;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * Activity to set fractal rendering as device wallpaper
  */
 public class WallpaperActivity extends Activity implements RendererListener {
 	
 	private RendererView rendererView;
 	
 	/**
 	 * @see android.app.Activity#onCreate(Bundle)
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		setContentView(R.layout.activity_wallpaper);
 		
 		rendererView = (RendererView)findViewById(R.id.rendererView);
 		rendererView.setRendererListener(this);
 	}
 	
 	/**
 	 * Called when user presses OK button
 	 * @param view the button
 	 */
 	public void onOK(View view) {
 		rendererView.stopRendering();
 		
 		Bitmap bitmap = rendererView.getBitmap();
 		
 		try {
 			WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
 			wallpaperManager.setBitmap(bitmap);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		finish();
 	}
 	
 	/**
 	 * Called when user presses Cancel button
 	 * @param view the button
 	 */
 	public void onCancel(View view) {
 		finish();
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererCreated(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererCreated(RendererView view, Renderer renderer) {
 		// Get renderer options from preferences
 		Palette palette = Palette.getPresetByName(Preferences.getStringPreference(this, Constants.PREF_PALETTE_PRESET, R.string.def_palettepreset));
		Mapping paletteMapping = Preferences.getMappingPreference(this, Constants.PREF_PALETTE_MAPPING, Mapping.REPEAT);
 		int paletteSize = Preferences.getIntegerPreference(this, Constants.PREF_PALETTE_SIZE, R.integer.def_palettesize);
 		int setColor = Preferences.getIntegerPreference(this, Constants.PREF_PALETTE_SETCOLOR, R.integer.def_palettesetcolor);
 		int itersPerFrame = Preferences.getIntegerPreference(this, Constants.PREF_ITERS_PERFRAME, R.integer.def_itersperframe);
 			
 		// Set render parameters from intent if they exist
 		Intent intent = getIntent();
 		if (intent != null && intent.hasExtra(Constants.EXTRA_PARAMS)) {
 			RendererParams params = (RendererParams)intent.getParcelableExtra(Constants.EXTRA_PARAMS);
 			view.setRendererParams(params);
 		}
 		
 		view.setPaletteMapping(paletteMapping);
 		view.setIterationsPerFrame(itersPerFrame);
 		
 		float bias = (paletteMapping == Mapping.HISTOGRAM) ? 5.0f : 1.0f;
 		
 		renderer.setPalette(palette, paletteSize, bias, setColor);
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererParamsChanged(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererParamsChanged(RendererView view, Renderer renderer) {	
 	}
 
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererIterated(RendererView, Renderer, int)
 	 */
 	@Override
 	public void onRendererIterated(RendererView view, Renderer renderer, int iters) {
 		// TODO display number of iterations somewhere
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererDestroy(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererDestroy(RendererView view, Renderer renderer) {
 	}
 	
 	/**
 	 * @see com.ijuru.refract.ui.RendererView.RendererListener#onRendererAllocationFailed(RendererView, Renderer)
 	 */
 	@Override
 	public void onRendererAllocationFailed(RendererView view, Renderer renderer) {
 		Toast.makeText(this, "Unable to allocate renderer resources", Toast.LENGTH_LONG).show();
 	}
 }
