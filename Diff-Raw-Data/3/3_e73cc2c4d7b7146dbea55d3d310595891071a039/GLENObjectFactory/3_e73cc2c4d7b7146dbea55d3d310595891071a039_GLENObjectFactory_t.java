 /* Copyright 2012 Richard Sahlin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.super2k.openglen.objects;
 
 import java.util.Hashtable;
 import java.util.LinkedList;
 
 import com.super2k.openglen.ConstantValues;
 import com.super2k.openglen.ObjectFactory;
 import com.super2k.openglen.ObjectFactoryManager;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.Renderer;
 import com.super2k.openglen.geometry.Material;
 import com.super2k.openglen.nibbler.BitmapHandler;
 import com.super2k.openglen.texture.Texture2D;
 import com.super2k.openglen.texture.TextureHandler;
 import com.super2k.openglen.utils.GraphicsLibraryHandler;
 import com.super2k.openglen.utils.Log;
 
 /**
  * Factory methods for creating objects that can be rendered using the OpenGLEN
  * renderer.
  * Use the ObjectFactoryManager to get an instance of an ObjectFactory.
  *@see ObjectFactoryManager
  *
  * @author Richard Sahlin
  *
  */
 public class GLENObjectFactory implements ObjectFactory {
 
     /**
      * The texturehandler used by the factory.
      */
     protected TextureHandler mTextureHandler;
 
     /**
      * The bitmaphandler used by the factory.
      */
     protected BitmapHandler mBitmapHandler;
 
     /**
      * Graphics utilities.
      */
     protected GraphicsLibraryHandler mGraphicsUtils;
 
 
     /**
      * Pools with current free objects
      */
     protected Hashtable<Integer, LinkedList<GLObject>> mPool =
             new Hashtable<Integer, LinkedList<GLObject>>();
     /**
      * Pools with all created objects
      */
     protected Hashtable<Integer, LinkedList<GLObject>> mCreatedPool =
             new Hashtable<Integer, LinkedList<GLObject>>();
 
     /**
      * Temp storage
      */
     protected LinkedList<GLObject> mTempList;
     protected GLObject mTempBlit;
 
     /**
      * Creates an objectfactory.
      *
      * @param renderer The renderer to create objects for.
      * @throws IllegalArgumentException If renderer is null
      */
     public GLENObjectFactory(Renderer renderer) {
         if (renderer == null) {
             throw new IllegalArgumentException("renderer is null");
         }
         mTextureHandler = renderer.getTextureHandler();
         mBitmapHandler = renderer.getBitmapHandler();
         mGraphicsUtils = renderer.getGraphicsUtilities();
     }
 
     @Override
     public GLBlitObject createBlitObject(Object bitmap, float xpos, float ypos, float zpos,
             float width, float height, int anchor, int shading, boolean useAlpha)
             throws OpenGLENException {
 
         int w = mBitmapHandler.getWidth(bitmap);
         int h = mBitmapHandler.getHeight(bitmap);
         Texture2D tex = new Texture2D(bitmap, -1, w, h);
         return createBlitObject(tex, xpos, ypos, zpos, width, height, anchor, shading, useAlpha);
     }
 
     @Override
     public GLBlitObject createBlitObject(Texture2D texture, float xpos, float ypos, float zpos,
             float width, float height, int anchor, int shading,
             boolean useAlpha) throws OpenGLENException {
 
         GLBlitObject blit = new GLBlitObject(xpos, ypos, zpos, width, height,
                 new Texture2D[] {texture}, anchor, 1);
         if (useAlpha) {
             blit.material.setBlendFunc(ConstantValues.SRC_ALPHA,
                                        ConstantValues.ONE_MINUS_SRC_ALPHA);
         }
         blit.setShading(shading);
         mTextureHandler.prepareTexture(0, texture);
 //        mGraphicsUtils.convertToVBO(blit);
 //        mTextures.add(texture);
         return blit;
     }
 
     @Override
     public GLBlitObject createBlitObject(String classname, Material material, int anchor,
             int width, int height) throws OpenGLENException {
 
         if (width == -1) {
             if (material.texture != null) {
                 width = material.texture[0].getWidth();
             } else {
                 throw new OpenGLENException("Cannot read width, no texture.");
             }
         }
         if (height == -1) {
             if (material.texture != null) {
                 height = material.texture[0].getHeight();
             } else {
                 throw new OpenGLENException("Cannot read height, no texture.");
             }
         }
         GLBlitObject blitObject = createBlitObject(classname);
         blitObject.create(1, 1, 1);
         blitObject.set(width, height, anchor, material);
         mTextureHandler.prepareMaterialTexture(0, blitObject.material);
         return blitObject;
 
     }
 
     /**
      * Creates a new GLBLitObject instance of the specified name.
      * @param classname Classname to instantiate, must be GLBlitObject subclass.
      * @return New instance of the specified class.
      */
     private GLBlitObject createBlitObject(String classname) {
 
         try {
             return (GLBlitObject) Class.forName(classname).newInstance();
 
         } catch (IllegalAccessException e) {
             throw new IllegalArgumentException(e);
         } catch (InstantiationException e) {
             Log.e(Renderer.OPENGLEN_TAG, "Could not instantiate: " + classname);
             throw new IllegalArgumentException(e);
         } catch (ClassNotFoundException e) {
             throw new IllegalArgumentException(e);
         } catch (ClassCastException e) {
             throw new IllegalArgumentException(e);
         }
 
     }
 
     @Override
     public void createBlitObjectPool(String className, int key, int count, int divisor,
             float xRepeat, float yRepeat) throws OpenGLENException {
         if (className == null || count <= 0) {
             throw new IllegalArgumentException("Illegal parameter: " + className + ", " +
                     ", " + count);
         }
         //Check if objects already exist
         Integer integer = Integer.valueOf(key);
         LinkedList<GLObject> blitPool = mPool.get(integer);
         if (blitPool != null) {
             throw new OpenGLENException("Already created pool with key: " + integer.intValue());
         }
         blitPool = new LinkedList<GLObject>();
         LinkedList<GLObject> createdPool = new LinkedList<GLObject>();
         for (int i = 0; i < count; i++) {
             GLBlitObject blit = createBlitObject(className, divisor, xRepeat, yRepeat);
             blit.key = key;
             blitPool.add(blit);
             createdPool.add(blit);
         }
         mPool.put(integer, blitPool);
         mCreatedPool.put(integer,createdPool);
     }
 
     @Override
     public void createParticlesPool(String className, int key, int count,
             int particleType, int particleCount,boolean useVBO)
                     throws OpenGLENException {
         if (className == null || count <= 0) {
             throw new IllegalArgumentException("Illegal parameter: " + className + ", " +
                     ", " + count);
         }
         //Check if objects already exist
         Integer integer = Integer.valueOf(key);
         LinkedList<GLObject> particlePool = mPool.get(integer);
         if (particlePool != null) {
             throw new OpenGLENException("Already created particle pool with key: " +
                     integer.intValue());
         }
         Log.d(Renderer.OPENGLEN_TAG, "Creating " + count + " particle array pools for "
                 + particleCount + " particles.");
         particlePool = new LinkedList<GLObject>();
         LinkedList<GLObject> createdParticles = new LinkedList<GLObject>();
         for (int i = 0; i < count; i++) {
             GLParticleArray particle = createParticleArray(particleCount, particleType, useVBO);
             particle.key = key;
             particlePool.add(particle);
             createdParticles.add(particle);
         }
         mPool.put(key, particlePool);
         mCreatedPool.put(key, createdParticles);
     }
 
 
     @Override
     public GLBlitObject getObject(int key) {
         mTempList = mPool.get(Integer.valueOf(key));
         if (mTempList == null) {
             throw new IllegalArgumentException("No pool for key: " + key);
         }
         if (mTempList.size() == 0) {
             throw new IllegalArgumentException("No more object in pool for key: " + key);
         }
         return (GLBlitObject) mTempList.removeLast();
     }
 
     @Override
     public void releaseObject(GLBlitObject object) {
         if (object == null) {
             throw new IllegalArgumentException("Object is null");
         }
         if (object.key == PooledObject.NOT_POOLED_OBJECT) {
             //Object is not pooled
             return;
         }
         mTempList = mPool.get(Integer.valueOf(object.key));
         if (mTempList == null) {
             throw new IllegalArgumentException("No pool for key: " + object.key);
         }
         mTempList.add(object);
         object.releaseObject();
     }
 
     @Override
     public GLParticleArray createParticleArray(int count, int key,boolean useVBO) throws OpenGLENException {
         GLParticleArray particles = new GLParticleArray();
         particles.init(count, key);
         if (useVBO) {
             mGraphicsUtils.convertToVBO(particles);
         }
         return particles;
     }
 
     @Override
     public GLParticleArray getParticle(int key) {
         mTempList = mPool.get(key);
         if (mTempList == null) {
             throw new IllegalArgumentException("No pool for key: " + key);
         }
         return (GLParticleArray) mTempList.removeLast();
 
     }
 
     @Override
     public void releaseParticle(GLParticleArray object) {
         if (object == null) {
             throw new IllegalArgumentException("object is null");
         }
         if (object.key == PooledObject.NOT_POOLED_OBJECT) {
             //Object is not pooled.
             return;
         }
         mTempList = mPool.get(object.key);
         if (mTempList == null) {
             throw new IllegalArgumentException("No pool for key: " + object.key);
         }
         mTempList.add(object);
         object.releaseObject();
     }
 
     @Override
     public void destroyPool(int key) {
         //Do not use mTempList since a call to releaseObject() be occur.
         LinkedList<GLObject> templist = mCreatedPool.get(key);
         if (templist == null) {
             throw new IllegalArgumentException("No pool for key: " + key);
         }
 
         int size = templist.size();
         for (int i = 0; i < size; i++) {
             mTempBlit = templist.removeLast();
             mGraphicsUtils.releaseGLObject(mTempBlit, mTextureHandler);
            mTempBlit.destroy();
             mTempBlit = null;
         }
         mPool.remove(key);
         mCreatedPool.remove(key);
         Log.d(ConstantValues.TAG, "Destroyed pool for key: " + key + ", deleted " + size + " objects.");
     }
 
     @Override
     public GLBlitObject createBlitObject(String className, int divisor, float xRepeat, float yRepeat) {
         if (className == null) {
             throw new IllegalArgumentException("Classname is null.");
         }
         GLBlitObject blit = createBlitObject(className);
         blit.create(divisor, xRepeat, yRepeat);
         return blit;
     }
 
 
 }
