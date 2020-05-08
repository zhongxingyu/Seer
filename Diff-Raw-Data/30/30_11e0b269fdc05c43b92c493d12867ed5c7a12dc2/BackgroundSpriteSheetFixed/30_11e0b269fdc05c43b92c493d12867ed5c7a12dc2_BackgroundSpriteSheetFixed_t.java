 /**
  * Copyright 2013 Alexis Lecanu
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package fr.ravenfeld.livewallpaper.library.objects.simple;
 
 import rajawali.materials.Material;
 import rajawali.materials.plugins.SpriteSheetMaterialPlugin;
 import rajawali.materials.textures.ATexture.TextureException;
 import rajawali.materials.textures.Texture;
 import rajawali.primitives.Plane;
 
 public class BackgroundSpriteSheetFixed extends ABackground {
 	protected Texture mTexture;
 	protected SpriteSheetMaterialPlugin mSpriteSheet;
 	protected int mNumTilesX;
 	protected int mNumTilesY;
 
 	public BackgroundSpriteSheetFixed(String nameTexture, int resourceId,
 			int numTilesX, int numTilesY) throws TextureException {
 		this(nameTexture, resourceId, numTilesX, numTilesY, numTilesX
 				* numTilesY);
 	}
 
 	public BackgroundSpriteSheetFixed(String nameTexture, int resourceId,
 			int numTilesX, int numTilesY, int numFrames)
 			throws TextureException {
 		init(nameTexture, resourceId);
 		mSpriteSheet = new SpriteSheetMaterialPlugin(numTilesX, numTilesY,
 				numFrames);
 		mNumTilesX = numTilesX;
 		mNumTilesY = numTilesY;
 		initPlugin();
 	}
 
 	public BackgroundSpriteSheetFixed(String nameTexture, int resourceId,
 			int numTilesX, int numTilesY, int fps, int numFrames)
 			throws TextureException {
 		init(nameTexture, resourceId);
 		mSpriteSheet = new SpriteSheetMaterialPlugin(numTilesX, numTilesY, fps,
 				numFrames);
 		mNumTilesX = numTilesX;
 		mNumTilesY = numTilesY;
 		initPlugin();
 	}
 	
 	public BackgroundSpriteSheetFixed(String nameTexture, int resourceId,
 			int numTilesX, int numTilesY, long[] frameDurations)
 			throws TextureException {
 		init(nameTexture, resourceId);
 		mSpriteSheet = new SpriteSheetMaterialPlugin(numTilesX, numTilesY,
 				frameDurations);
 		mNumTilesX = numTilesX;
 		mNumTilesY = numTilesY;
 		initPlugin();
 	}
 
 	public BackgroundSpriteSheetFixed(BackgroundSpriteSheetFixed other) {
 		setFrom(other);
 	}
 
 	@Override
 	public BackgroundSpriteSheetFixed clone() {
 		return new BackgroundSpriteSheetFixed(this);
 	}
 
 	private void init(String nameTexture, int resourceID)
 			throws TextureException {
 		mTexture = new Texture(nameTexture, resourceID);
 		mPlane = new Plane(1f, 1f, 1, 1);
 		mMaterial = new Material();
        mMaterial.setColorInfluence(0);
 		mMaterial.addTexture(mTexture);
 		mPlane.setMaterial(mMaterial);
 		mPlane.setPosition(0, 0, 0);
 		mPlane.setRotY(180);
         mPlane.setTransparent(true);
 	}
 
 	private void initPlugin() {
 		mMaterial.addPlugin(mSpriteSheet);
 		animate();
 	}
 
 	public void setFrom(BackgroundSpriteSheetFixed other) {
 		mTexture = other.getTexture();
 		mMaterial = other.getMaterial();
 		mPlane = other.getObject3D();
 	}
 
 	public Texture getTexture() {
 		return mTexture;
 	}
 
 
 	@Override
 	public int getWidth() {
 		return mTexture.getWidth();
 	}
 
 	@Override
 	public int getHeight() {
 		return mTexture.getHeight();
 	}
 
 	public int getWidthTile() {
 		return mTexture.getWidth() / mNumTilesX;
 	}
 
 	public int getHeightTile() {
 		return mTexture.getHeight() / mNumTilesY;
 	}
 
 	public void animate() {
 		mSpriteSheet.play();
 	}
 
 	public void stopAnimation(int frameIndex) {
 		mSpriteSheet.pause(frameIndex);
 	}
 
 	public void stopAnimation() {
 		mSpriteSheet.pause();
 	}
 
 	public boolean isPlaying() {
 		return mSpriteSheet.isPlaying();
 	}
 
 	public void setLoop(boolean loop) {
 		mSpriteSheet.setLoop(loop);
 	}
 
 	public boolean getLoop() {
 		return mSpriteSheet.getLoop();
 	}
 
     public void setPingPong(boolean pingPong) {
         mSpriteSheet.setPingPong(pingPong);
     }
 
     public boolean getPingPong() {
         return mSpriteSheet.getPingPong();
     }
 
 	@Override
 	public void surfaceChanged(int width, int height) {
 		float ratioDisplay = (float) height / (float) width;
 		float ratioSize = 1f / getHeightTile();
 		float scale = getWidthTile() * ratioSize * ratioDisplay;
 		mPlane.setScaleX(scale);
 		mPlane.setScaleY(1);
 	}
 }
