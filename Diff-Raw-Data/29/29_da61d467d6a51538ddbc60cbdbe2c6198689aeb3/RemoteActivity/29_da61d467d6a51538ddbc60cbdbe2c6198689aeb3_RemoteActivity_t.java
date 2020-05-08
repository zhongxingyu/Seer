 /*******************************************************************************
  * Copyright (C) 2012 Henrik Voß, Sven Nobis and Nicolas Gramlich (AndEngine)
  * 
  * This file is part of VHackAndroidGame
  * (https://github.com/SvenTo/VHackAndroidGame)
  * 
  * VHackAndroidGame is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This source code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this source code; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  ******************************************************************************/
 package de.dsi8.vhackandroidgame;
 
 import java.net.Socket;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
 import org.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
 import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 
 import android.content.Context;
 import android.opengl.GLES20;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import de.dsi8.dsi8acl.common.utils.AsyncTaskResult;
 import de.dsi8.dsi8acl.connection.model.ConnectionParameter;
 import de.dsi8.dsi8acl.exception.ConnectionProblemException;
 import de.dsi8.vhackandroidgame.logic.contract.IClientLogic;
 import de.dsi8.vhackandroidgame.logic.contract.IClientLogicListener;
 import de.dsi8.vhackandroidgame.logic.impl.ClientLogic;
import de.dsi8.vhackandroidgame.logic.impl.VHackAndroidGameConfiguration;
 
 public class RemoteActivity extends SimpleBaseGameActivity implements IClientLogicListener {
 
 	private IClientLogic clientLogic;
 	
 	private static final int CAMERA_WIDTH = 320;
 	
 	private static final int CAMERA_HEIGHT = 192;
 	
 	private BitmapTextureAtlas mOnScreenControlTexture;
 
 	
 	private static final String LOG_TAG = "RemoteActivity";
 	
 	/*
 	  Wait until this before sending the next
 	 */
 	private long nextSendl;
 
 	private Camera mCamera;
 	private ITextureRegion mOnScreenControlBaseTextureRegion;
 	
 	private ITextureRegion mOnScreenControlKnobTextureRegion;
 	
 	private Scene mScene;
 	
 	private ConnectionParameter connectionParameter;
 	private ConnectTask connectTask;
 	
 	@Override
 	protected void onCreate(Bundle pSavedInstanceState) {
 		super.onCreate(pSavedInstanceState);
		ConnectionParameter.setStaticCommunicationConfiguration(new VHackAndroidGameConfiguration());
 		
 		connectionParameter = new ConnectionParameter(getIntent().getData().toString());
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		Log.i(LOG_TAG, "onStart");
 		
 		connectTask = new ConnectTask();
 		connectTask.execute((Object)null);
 	}
 	
 	@Override
 	protected void onStop() {
 		super.onStop();
 		Log.i(LOG_TAG, "onStop");
 		
 		connectTask.cancel(true);
 		if(clientLogic != null) {
 			clientLogic.close();
 		}
 	}
 	
 	/**
 	 * The Task that should connect the client with the host.
 	 */
 	private class ConnectTask extends AsyncTask<Object, Object, AsyncTaskResult<Socket>>  {
 				/**
 				 * Connecting to the Host.
 				 */
 				@Override
 				protected AsyncTaskResult<Socket> doInBackground(
 						Object... params) {
 					try {
 						synchronized (this) {
 							// Workaround for multicall of onStart by the AndEngine
 							try {
 								this.wait(2000);
 							} catch(InterruptedException ex) {
 								Log.i(LOG_TAG, "Interrupted");
 								return null;
 							}
 						}
 						Log.i(LOG_TAG, "Not interrupted");
 						Socket socket = new Socket(connectionParameter.host, connectionParameter.port);
 						return new AsyncTaskResult<Socket>(socket);
 					} catch (Exception e) {
 						return new AsyncTaskResult<Socket>(e);
 					}
 				}
 				
 				/**
 				 * Connection is open, initialize the logic.
 				 */
 				@Override
 				protected void onPostExecute(AsyncTaskResult<Socket> result) {
 					if(result.getError() == null) {
 						clientLogic = new ClientLogic(RemoteActivity.this, result.getResult());
 					} else {
 						Log.e(LOG_TAG, "IOException", result.getError());
 						finish();
 					}
 				}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
     @Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 
 		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);
 	}
     
     /**
      * {@inheritDoc}
      */
 	@Override
 	protected void onCreateResources() {
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 		
 		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
 		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
 		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
 		this.mOnScreenControlTexture.load();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Scene onCreateScene() {
 		this.mScene = new Scene();
 		this.mScene.setBackground(new Background(0, 0, 0));
 
 		this.initOnScreenControls();
 
 		return this.mScene;
 	}
 	
 	/**
 	 * Initialize the On-Screen-Controls.
 	 */
 	private void initOnScreenControls() {
 		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(CAMERA_WIDTH - this.mOnScreenControlBaseTextureRegion.getWidth(), CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IAnalogOnScreenControlListener() {
 			@Override
 			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
 				// TODO Zum Server funken
 				
 				long now = System.currentTimeMillis();
 				// TODO: Not here & this better?
 				if(now > nextSendl) {
 					if(clientLogic != null) {
 						RemoteActivity.this.clientLogic.driveCar(pValueX, pValueY);
 					}
 					nextSendl = now+50;
 				}
 				
 				
 			}
 
 			@Override
 			public void onControlClick(final AnalogOnScreenControl pAnalogOnScreenControl) {/* Nothing. */}
 		});
 		analogOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		analogOnScreenControl.getControlBase().setAlpha(0.5f);
 		analogOnScreenControl.refreshControlKnobPosition();
 
 		this.mScene.setChildScene(analogOnScreenControl);
 	}
 
 	@Override
 	public void connectionLost(ConnectionProblemException ex) {
 		Log.e(LOG_TAG, "connectionLost", ex);
 	}
 
 	@Override
 	public void collisionDetected() {
 		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		v.vibrate(300);
 
 
 	}
 
 	
 }
