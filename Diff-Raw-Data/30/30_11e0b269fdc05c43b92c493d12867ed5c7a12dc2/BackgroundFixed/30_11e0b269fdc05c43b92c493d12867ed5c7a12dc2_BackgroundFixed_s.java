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
 import rajawali.materials.textures.ATexture.TextureException;
 import rajawali.materials.textures.Texture;
 import rajawali.primitives.Plane;
 import android.graphics.Bitmap;
 
 public class BackgroundFixed extends ABackground {
 	protected Texture mTexture;
 
 	public BackgroundFixed(String nameTexture, int resourceId)
 			throws TextureException {
 		mTexture = new Texture(nameTexture, resourceId);
 		init();
 	}
 
 	public BackgroundFixed(String nameTexture, Bitmap bitmap)
 			throws TextureException {
 		mTexture = new Texture(nameTexture, bitmap);
 		init();
 	}
 
 	public BackgroundFixed(BackgroundFixed other) {
 		setFrom(other);
 	}
 
 	@Override
 	public BackgroundFixed clone() {
 		return new BackgroundFixed(this);
 	}
 
 	private void init() throws TextureException {
 		mPlane = new Plane(1f, 1f, 1, 1);
 		mMaterial = new Material();
 		mMaterial.addTexture(mTexture);
 		mPlane.setMaterial(mMaterial);
 		mPlane.setPosition(0, 0, 0);
 		mPlane.setRotY(180);
         mPlane.setTransparent(true);
 	}
 
 	public void setFrom(BackgroundFixed other) {
 		mTexture = other.getTexture();
 		mMaterial = other.getMaterial();
 		mPlane = other.getObject3D();
 	}
 
 	public void setTexture(int resourceId) {
 		mTexture.setResourceId(resourceId);
 	}
 
 	public void setTexture(Bitmap bitmap) {
 		mTexture.setBitmap(bitmap);
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
 
 	@Override
 	public void surfaceChanged(int width, int height) {
 		float ratioDisplay = (float) height / (float) width;
 		float ratioSize = 1f / getHeight();
 		float scale = getWidth() * ratioSize * ratioDisplay;
 		mPlane.setScaleX(scale);
 		mPlane.setScaleY(1);
 	}
 }
