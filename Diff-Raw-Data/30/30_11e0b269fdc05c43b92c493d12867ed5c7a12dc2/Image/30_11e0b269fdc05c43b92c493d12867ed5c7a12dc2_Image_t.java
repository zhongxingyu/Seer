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
 
 import android.graphics.Bitmap;
 
 import rajawali.materials.Material;
 import rajawali.materials.textures.ATexture.TextureException;
 import rajawali.materials.textures.Texture;
 import rajawali.math.vector.Vector2;
 import rajawali.math.vector.Vector3;
 import rajawali.primitives.Plane;
 
 public class Image extends ABackground {
 	protected Texture mTexture;
     protected float mSize;
 
 	public Image(String nameTexture, int resourceId, float size)
 			throws TextureException {
 		mTexture = new Texture(nameTexture, resourceId);
 		init(size);
 	}
 
 	public Image(String nameTexture, Bitmap bitmap, float size)
 			throws TextureException {
 		mTexture = new Texture(nameTexture, bitmap);
 		init(size);
 	}
 
 	public Image(Image other) {
 		setFrom(other);
 	}
 
 	@Override
 	public Image clone() {
 		return new Image(this);
 	}
 
 	private void init(float size) throws TextureException {
         mSize=size;
 		mPlane = new Plane(1f, 1f, 1, 1);
 		mMaterial = new Material();
        mMaterial.setColorInfluence(0);
 		mMaterial.addTexture(mTexture);
 		mPlane.setMaterial(mMaterial);
 		mPlane.setPosition(0, 0, 0);
 		mPlane.setRotY(180);
         mPlane.setTransparent(true);
 	}
 
 	public void setFrom(Image other) {
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
 
     public float getSize() {
         return mSize;
     }
 
     public void setPosition(double x, double y){
         mPlane.setPosition(x,y,0);
     }
 
     public Vector2 getPosition(){
        return new Vector2(mPlane.getPosition().x, mPlane.getPosition().y);
     }
 
 	@Override
 	public void surfaceChanged(int width, int height) {
 		float ratioDisplay = (float) height / (float) width;
 		float ratioSize = mSize / getHeight();
 		float scale = getWidth() * ratioSize * ratioDisplay;
 		mPlane.setScaleX(scale);
 		mPlane.setScaleY(mSize);
 	}
 }
