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
 
 package com.super2k.openglen.core;
 
 import java.io.IOException;
 import java.nio.IntBuffer;
 import java.util.LinkedList;
 
 import com.super2k.openglen.ConstantValues;
 import com.super2k.openglen.OpenGLENException;
 import com.super2k.openglen.ProfileInfo;
 import com.super2k.openglen.RenderSetting;
 import com.super2k.openglen.Renderer;
 import com.super2k.openglen.RendererInfo;
 import com.super2k.openglen.geometry.Material;
 import com.super2k.openglen.lighting.DirectionalLight;
 import com.super2k.openglen.lighting.Light;
 import com.super2k.openglen.nibbler.BitmapHandler;
 import com.super2k.openglen.objects.GLBlitObject;
 import com.super2k.openglen.objects.GLParticleArray;
 import com.super2k.openglen.program.BlitProgramCollection;
 import com.super2k.openglen.program.ParticleProgramCollection;
 import com.super2k.openglen.program.ProgramCollection;
 import com.super2k.openglen.program.ProgramHandler;
 import com.super2k.openglen.texture.Texture2D;
 import com.super2k.openglen.texture.TextureHandler;
 import com.super2k.openglen.utils.GraphicsLibraryHandler;
 import com.super2k.openglen.utils.Log;
 import com.super2k.openglen.utils.Matrix;
 
 /**
  * Base implementation of the GLES renderer, this class is platform independent
  * and should be used to enable portability to standard Java. This class is just
  * one possible implementation of the Renderer interface.
  *
  * @author Richard Sahlin
  *
  */
 public abstract class GLESBaseRenderer implements Renderer {
 
     private final String TAG = this.getClass().getSimpleName();
 
     private final static String INVALID_ARGUMENT_STR = "Invalid argument:";
 
     protected int mState = 0;
 
     /**
      * Current number of vertex arrays that are enabled
      */
     protected int mCurrentArrays;
 
     /**
      * Renderer info.
      */
     protected RendererInfo mRenderInfo;
 
     /**
      * GLESBaseRenderer Used to store profileinfo.
      */
     protected ProfileInfo mProfileInfo = new ProfileInfo();
 
     /**
      * Local variables to collect profile counters.
      */
     protected int mVertexCount = 0; // Number of VBO vertices sent to GL
 
     protected int mIndexCount = 0; // Number of (non vbo) vertices sent to GL
 
     protected int mVBOvertexCount = 0; // number of VBO indices sent to GL
 
     protected int mVBOIndexCount = 0; // Number of (non vbo) indices sent to GL
 
     protected int mGLDrawCalls = 0;
 
     protected ProgramCollection mBlitPrograms;
 
     protected int mProgramCount = 6;
 
     protected ProgramCollection mParticlePrograms;
 
     protected float[] mPerspectiveMatrix = new float[16];
 
     protected float[] mTempMatrix = new float[16];
     protected float[] mTempMatrix2 = new float[16];
     protected boolean mPerspectiveMatrixDirty = false; // Set to true when matrix is updated.
 
     protected int[] mViewPort = new int[4]; // Contains the current viewport.
 
     protected float[] mOneByPortSize = new float[2]; // Contains 1/viewport size
 
     protected float[] mLightDirection;
 
     protected float[] mLightColor;
 
     protected float[] mLightPos;
 
     /**
      * Lights, default to one Directional light.
      * Length of this array specifies the max number of lights.
      */
     protected Light[] mLights = new Light[] { new DirectionalLight(100, 300, 350, 1f, 1f, 1f)};
 
     protected int maxLights = mLights.length;
     /**
      * Render state variables
      */
     public final static int MAX_TEXTURE_UNITS = 2; // Max number of textures per object.
 
     protected int mSrcBlend = -1;
 
     protected int mDstBlend = -1;
 
     protected boolean mBlendingEnabled = false;
 
     /**
      * The currently selected program for an OpenGLES implementation with
      * programmable shaders.
      */
     protected int mCurrentProgram = -1;
 
     /**
      * The rendersettings class, settings can be changed at runtime.
      * Automatically updated each frame if dirty flag is set.
      */
     protected RenderSetting mRenderSetting;
 
     /**
      * The texturehandler, this is implementation specific. Subclasses of this
      * class shall instantiate the proper TextureHandler for the target
      * platform.
      */
     protected TextureHandler mTextureHandler;
 
     /**
      * The programhandler, this is implementation specific. Subclasses of this
      * class shall instantiate the proper ProgramHandler for the target
      * platform.
      */
     protected ProgramHandler mProgramHandler;
 
     /**
      * Handler for OpenGL utilities. Subclasses of this class shall instantiate
      * the proper GraphicsLibraryHandler for the target platform.
      */
     protected GraphicsLibraryHandler mGraphicsUtilities;
 
     /**
      * Platform independent way of handling and creating bitmaps Subclasses of
      * this class shall instantiate the proper BitmapHandler for the target
      * platform.
      */
     protected BitmapHandler mBitmapHandler;
 
     /**
      * Implementations shall set this to true when initialized
      */
     protected boolean mInitialized = false;
 
     /**
      * Constructs a new GLESBaseRenderer.
      *
      * @param renderSetting The renderer settings. May be null to create a
      *            default setting.
      */
     public GLESBaseRenderer(RenderSetting renderSetting) {
         if (renderSetting==null)
             mRenderSetting = new RenderSetting();
         else
             mRenderSetting = renderSetting;
 
     }
 
     @Override
     public void startRenderer() throws OpenGLENException {
         Log.d(Renderer.OPENGLEN_TAG, "startRenderer:");
         try {
             mBlitPrograms.loadPrograms();
             mBlitPrograms.setUniformLocations();
 
             mParticlePrograms.loadPrograms();
             mParticlePrograms.setUniformLocations();
 
         } catch (IOException ioe) {
             // Unable to load shaders.
             throw new IllegalArgumentException(ioe);
         }
        mCurrentProgram = -1;
        mPerspectiveMatrixDirty = true;
        mCurrentArrays = 0;
         mGraphicsUtilities.setupGL(mRenderSetting);
         mRenderInfo = new RendererInfo(mGraphicsUtilities);
         mGraphicsUtilities.checkError();
 
         /**
          * Renderer started, log info.
          */
         Log.i(TAG, "Started renderer with info:");
         Log.i(TAG, mRenderInfo.getVendor());
         Log.i(TAG, mRenderInfo.getRenderer());
         Log.i(TAG, mRenderInfo.getVersion());
         Log.i(TAG, "Texture units - combined/vertex/fragment\n"
                 + mRenderInfo.getCombinedTextureUnits() + "/"
                 + mRenderInfo.getVertexTextureUnits() + "/"
                 + mRenderInfo.getFragmentTextureUnits());
         Log.i(TAG, "Max vertexattribs/varyingvectors/vertexuniforms/fragmentuniforms\n"
                 + mRenderInfo.getMaxVertexAttribs() + "/"+mRenderInfo.getMaxVaryingVectors() + "/"
                 + mRenderInfo.getMaxVertexUniforms() + "/"+mRenderInfo.getMaxFragmentUniforms());
         Log.i(TAG, "Max texture size: " + mRenderInfo.getMaxTextureSize());
         /**
          * Log extensions.
          */
         Log.i(TAG, mRenderInfo.getExtensionsLine());
         mState = STATE_STARTED;
     }
 
     @Override
     public void initRenderer() throws OpenGLENException {
         Log.d(TAG, "initRenderer()");
         if (mState != STATE_CREATED) {
             Log.w(OPENGLEN_TAG, "Initrenderer with state: " + mState);
             return;
         }
         // Make sure handlers are created if null - graphics library first.
         if (mGraphicsUtilities==null) {
             createGraphicsLibraryUtilities();
         }
         if (mTextureHandler==null) {
             createTextureHandler();
         }
         if (mProgramHandler==null) {
             createProgramHandler();
         }
         if (mBitmapHandler==null) {
             createBitmapHandler();
         }
 
         mBlitPrograms = new BlitProgramCollection(mProgramHandler, mGraphicsUtilities);
         mParticlePrograms = new ParticleProgramCollection(mProgramHandler, mGraphicsUtilities);
         mState = STATE_INITIALIZED;
     }
 
     @Override
     public void destroy() {
         mState = STATE_CREATED;
     }
 
     /**
      * Creates the texture handler, internal method. The texturehandler will be
      * different depending on target platform.
      */
     protected abstract void createTextureHandler();
 
     /**
      * Creates the graphics library utilities, internal method. The
      * graphicslibraryutilities will be different depending on target platform.
      */
     protected abstract void createGraphicsLibraryUtilities();
 
     /**
      * Creates the program handler, internal method The programhandler will be
      * different depending on target platform.
      */
     protected abstract void createProgramHandler();
 
     /**
      * Creates the bitmap handler, internal method. The Bitmap handler is a
      * platform dependant way of creating and using bitmaps (as a helper for
      * textures)
      */
     protected abstract void createBitmapHandler();
 
     /**
      * Enables the vertex attrib arrays, internal method.
      *
      * @param count Number of attribute arrays to enable
      * @throws IllegalArgumentException If count is <= 0
      */
     protected abstract void enableVertexAttributes(int count);
 
     /**
      * Render the specified GLBlitObject to GL using the specified program.
      *
      * @param program Program index in the current programcollection.
      * @param uniformLocation The uniform locations for this shader.
      * @param blit The blit object to render.
      */
     protected abstract void renderGLBlitOBject(
             int program, int[] uniformLocation, GLBlitObject blit);
 
     /**
      * Render the specified GLParticleArrays to GL using the specified program.
      *
      * @param program Program object name.
      * @param uniformLocation The uniform locations for the program.
      * @param particleArray The particle array to render.
      */
     protected abstract void renderGLParticleArray(
             int program, int[] uniformLocation, GLParticleArray particleArray);
 
     @Override
     public void beginFrame() {
         if (mState != STATE_STARTED) {
             throw new IllegalStateException("Illegal render state: " + mState);
         }
 
         mVertexCount = 0;
         mIndexCount = 0;
         mVBOvertexCount = 0;
         mVBOIndexCount = 0;
         mGLDrawCalls = 0;
 
         if (mRenderSetting.getChangeFlag() != RenderSetting.CHANGE_FLAG_NONE) {
             mGraphicsUtilities.setRenderSetting(mRenderSetting);
             mGraphicsUtilities.checkError(); // Clear errors
             mRenderSetting.setChangeFlag(RenderSetting.CHANGE_FLAG_NONE);
         }
     }
 
     @Override
     public void endFrame() {
         // Send GL counters to profiling
         mProfileInfo.update(mVertexCount,
                             mIndexCount,
                             mVBOvertexCount,
                             mVBOIndexCount,
                             mGLDrawCalls);
 
     }
 
     @Override
     public void setLight(int lightNumber, Light light) {
 
         if (mLights==null || lightNumber >= mLights.length || lightNumber < 0) {
             throw new IllegalArgumentException(INVALID_ARGUMENT_STR);
         }
         mLights[lightNumber] = light;
 
     }
 
     @Override
     public int getMaxLights() {
         return maxLights;
     }
 
     @Override
     public void setPerspectiveMatrix(float[] matrix) {
         System.arraycopy(matrix, 0, mPerspectiveMatrix, 0, 16);
         mPerspectiveMatrixDirty = true; // Used to indicate that program must send matrix.
     }
 
     @Override
     public void setViewPort(int x, int y, int width, int height) {
         // Subclasses call super and then set viewport.
         // Save viewport dimensions.
         mViewPort[0] = x;
         mViewPort[1] = y;
         mViewPort[2] = width;
         mViewPort[3] = height;
         mOneByPortSize[0] = (float)1/(width-x);
         mOneByPortSize[1] = (float)1/(height-y);
     }
 
     @Override
     public int[] getViewPort() {
         return new int[] {mViewPort[0], mViewPort[1], mViewPort[2], mViewPort[3]};
     }
 
     @Override
     public TextureHandler getTextureHandler() {
         return mTextureHandler;
     }
 
     @Override
     public ProgramHandler getProgramHandler() {
         return mProgramHandler;
     }
 
     @Override
     public GraphicsLibraryHandler getGraphicsUtilities() {
         return mGraphicsUtilities;
     }
 
     @Override
     public BitmapHandler getBitmapHandler() {
         return mBitmapHandler;
     }
 
     @Override
     public RenderSetting getRenderSetting() {
         return mRenderSetting;
     }
 
     @Override
     public ProfileInfo getProfileInfo() {
         return mProfileInfo;
     }
 
     @Override
     public void renderGLBlitObjects(LinkedList<GLBlitObject> objectList) {
 
         Material material;
 
         // Always use the same attributes for GLBlitObjects.
         int arrayCount = mBlitPrograms.getAttribArrayCount();
        if (arrayCount != mCurrentArrays) {
            enableVertexAttributes(arrayCount);
        }
         mCurrentArrays = arrayCount;
 
         GLBlitObject blit;
 
         int blitcount = objectList.size();
         for (int i = 0; i < blitcount; i++) {
 
             blit = objectList.get(i);
             // Check renderflag.
             if (blit.renderFlag) {
 
                 material = blit.material;
                 int programIndex = 0;
                 switch (material.materialShading) {
 
                     case Material.SHADING_UNLIT:
                         programIndex = 0;
                     break;
                     case Material.SHADING_LAMBERT:
                         programIndex = 1;
                     break;
                     case Material.SHADING_PHONG:
                         programIndex = 2;
                     break;
                     case Material.SHADING_LIT:
                         programIndex = 3;
                     break;
                     case Material.SHADING_COLORED:
                         programIndex = 4;
                     break;
                     case Material.SHADING_BLUR5_TEXTURE:
                         programIndex = 5;
                     break;
                     case Material.SHADING_BLUR9_TEXTURE:
                         programIndex = 6;
                     break;
                     case Material.SHADING_CUSTOM:
                         //When using custom shader program take the program index to phong
                         //to get a lookup to all uniforms.
                         programIndex = Material.SHADING_COLORED;
                     break;
                     default:
                         throw new IllegalArgumentException("Invalid material shading: " +
                                 material.materialShading);
 
                 }
                 int textureCount = material.texture.length;
                if (textureCount > 1) {
                     programIndex += (textureCount-1) *
                    (mBlitPrograms.mProgramCount / MAX_TEXTURE_UNITS);
                 }
                 //TODO: Should not just take any uniform locations
                 //need mechanism to delegate to shaderprogram
                 int[] uniformLocations = mBlitPrograms.getUniformLocations(programIndex);
                 int program;
                 if (material.materialShading == Material.SHADING_CUSTOM) {
                     program = material.shaderProgram.getProgram();
                 } else {
                     program = mBlitPrograms.getProgramNameByIndex(programIndex);
                 }
                 setBlitProgram(program, textureCount, uniformLocations, blit, material);
                 setBlitMaterial(program, material, textureCount);
                 renderGLBlitOBject(program, uniformLocations, blit);
                 mGLDrawCalls++;
                 // mGraphicsUtilities.checkError();
             }
         }
 
     }
 
     /**
      * Setup the material properties for a GLBlitObject. This is the texture and
      * blending. Bind the textures needed for the specified program and
      * material. Enable or disable blending based on material and program. Set
      * texture parameters.
      *
      * @param program The program to be used.
      * @param material The material
      * @param textureCount The number of textures used for this material
      */
     protected void setBlitMaterial(int program, Material material, int textureCount) {
         /**
          * Texture and material setup
          */
         if (mBlendingEnabled && material.sourceBlend == ConstantValues.NONE) {
             mGraphicsUtilities.disable(ConstantValues.BLEND);
             mBlendingEnabled = false;
         } else if (!mBlendingEnabled && material.sourceBlend != ConstantValues.NONE) {
             mBlendingEnabled = true;
             mGraphicsUtilities.enable(ConstantValues.BLEND);
             mGraphicsUtilities.blendFunc(material.sourceBlend, material.destBlend);
             mSrcBlend = material.sourceBlend;
             mDstBlend = material.destBlend;
         } else if (mBlendingEnabled && (mSrcBlend != material.sourceBlend ||
                 mDstBlend != material.destBlend)) {
             mGraphicsUtilities.blendFunc(material.sourceBlend, material.destBlend);
             mSrcBlend = material.sourceBlend;
             mDstBlend = material.destBlend;
         }
 
         Texture2D tex = null;
         for (int tloop = 0; tloop < textureCount; tloop++) {
             tex = material.texture[tloop];
             // Check for texture
            mTextureHandler.activeTexture(tex.getTextureUnit());
 
             if (material.texture[tloop].getTextureName()==-1) {
                 try {
                     // Normally never done in this place. Materials prepared
                     // outside of render.
                     mTextureHandler.prepareTexture(tloop, material.texture[tloop]);
                     Log.w(TAG, "Texture prepared in renderloop: " + material.texture[tloop].getTextureName());
                 } catch (OpenGLENException glen) {
                     // Could not prepare texture, unrecoverable exception.
                     // TODO: Is there a way to recover?
                     throw new IllegalStateException(glen);
                 }
 
             } else {
                 mTextureHandler.bindTexture2D(tex.getTarget(), tex.getTextureName());
             }
 
             // Check texture parameters.
             int[] setparams = tex.getCurrentTexParams();
             int[] texparams = tex.getTexParams();
             // If any texparam is changed reset all.
             if (texparams[0] != setparams[0] || texparams[1] != setparams[1] ||
                     texparams[2] != setparams[2] || texparams[3] != setparams[3]) {
 
                 mTextureHandler.texParameter2D(ConstantValues.TEXTURE_MAG_FILTER, texparams[0]);
                 mTextureHandler.texParameter2D(ConstantValues.TEXTURE_MIN_FILTER, texparams[1]);
                 mTextureHandler.texParameter2D(ConstantValues.TEXTURE_WRAP_S, texparams[2]);
                 mTextureHandler.texParameter2D(ConstantValues.TEXTURE_WRAP_T, texparams[3]);
                 setparams[0] = texparams[0];
                 setparams[1] = texparams[1];
                 setparams[2] = texparams[2];
                 setparams[3] = texparams[3];
             }
         }
 
     }
 
     /**
      * Sets the program for a GLBLitObject Called from the renderer when the
      * program to use has been identified. This method should make the program
      * current and set the uniform data needed by the shader.
      *
      * @param program The program to use, implementations can keep track if the
      *            program has changed from last call.
      * @param textureCount Number of textures in material
      * @param uniformLocations Pointer to uniform locations.
      * @param material The material to use for the object.
      * @param blit The blit object.
      */
     protected void setBlitProgram(int program,
                                   int textureCount,
                                   int[] uniformLocations,
                                   GLBlitObject blit,
             Material material) {
 
         if (mCurrentProgram != program) {
             mCurrentProgram = program;
             mProgramHandler.useProgram(program);
             // Setup the uniforms needed for this program.
             mProgramHandler.setUniformMatrix(
                     uniformLocations[BlitProgramCollection.PERSPECTIVEMATRIX_UNIFORM],
                     1, mPerspectiveMatrix, 0);
             // Setup the lights for easy access when rendering objects.
             mLightPos = mLights[0].position;
             mLightColor = mLights[0].color;
             // Prepare lights
             if (mLights[0].type==Light.DIRECTIONAL) {
                 mLightDirection = ((DirectionalLight)mLights[0]).direction;
             } else
                 mLightDirection = null;
 
         } else if (mPerspectiveMatrixDirty) {
             // perspective matrix is changed, must send to program
             mPerspectiveMatrixDirty = false;
             mProgramHandler.setUniformMatrix(
                     uniformLocations[BlitProgramCollection.PERSPECTIVEMATRIX_UNIFORM],
                     1, mPerspectiveMatrix, 0);
         }
 
         /**
          * Program is selected, setup uniforms
          */
         mProgramHandler.setUniformInt(
                 uniformLocations[BlitProgramCollection.TEXTURE1_UNIFORM], 0);
         if (textureCount > 1) {
             mProgramHandler.setUniformInt(
                     uniformLocations[BlitProgramCollection.TEXTURE2_UNIFORM], 1);
         }
         mProgramHandler.setUniformVector(
                 uniformLocations[BlitProgramCollection.TRANSLATE_UNIFORM], 2, blit.position, 0);
         mProgramHandler.setUniformVector(
                 uniformLocations[BlitProgramCollection.SCALE_UNIFORM], 1, blit.scale, 0);
         mProgramHandler.setUniformVector(
                 uniformLocations[BlitProgramCollection.ROTATE_UNIFORM], 1, blit.rotation, 0);
 
         mProgramHandler.setUniformVector(
                 uniformLocations[BlitProgramCollection.LIGHTCOLOR_UNIFORM],
                 1, mLightColor, 0);
         mProgramHandler.setUniformVector(
                 uniformLocations[BlitProgramCollection.DIFFUSEMATERIAL_UNIFORM],
                 1, material.diffuse, 0);
 
         int shading = material.materialShading;
         // Setup material color.
         if (shading==Material.SHADING_PHONG || shading==Material.SHADING_LAMBERT) {
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.AMBIENTMATERIAL_UNIFORM], 1,
                     material.ambient, 0);
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.DIFFUSEMATERIAL_UNIFORM], 1,
                     material.diffuse, 0);
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.SPECULARMATERIAL_UNIFORM], 1,
                     material.specular, 0);
             mProgramHandler.setUniformFloat(
                     uniformLocations[BlitProgramCollection.SHINEMATERIAL_UNIFORM],
                     material.power);
             mProgramHandler.setUniformVector3(
                     uniformLocations[BlitProgramCollection.DIRECTIONALLIGHT_UNIFORM], 1,
                     mLightDirection, 0);
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.LIGHTCOLOR_UNIFORM], 1,
                     mLightColor, 0);
 
             // Note - this is the screen space position of the light.
             mProgramHandler.setUniformVector3(
                     uniformLocations[BlitProgramCollection.LIGHTPOSITION_UNIFORM], 1,
                     mLightPos, 0);
         } else if (shading==Material.SHADING_LIT) {
             mProgramHandler.setUniformVector3(
                     uniformLocations[BlitProgramCollection.DIRECTIONALLIGHT_UNIFORM], 1,
                     mLightDirection, 0);
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.LIGHTCOLOR_UNIFORM], 1,
                     mLightColor, 0);
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.DIFFUSEMATERIAL_UNIFORM], 1,
                     material.diffuse, 0);
 
         } else if (shading==Material.SHADING_COLORED) {
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.DIFFUSEMATERIAL_UNIFORM], 1,
                     material.diffuse, 0);
 
         } else if (shading==Material.SHADING_BLUR5_TEXTURE ||
                 shading==Material.SHADING_BLUR9_TEXTURE) {
             float[] divBySize = new float[] {
                     1/(material.texture[0].getWidth()*material.xBlurFactor),
                     1/(material.texture[0].getHeight()*material.yBlurFactor),
                     material.weightFactor, 0 };
             mProgramHandler.setUniformVector(
                     uniformLocations[BlitProgramCollection.DIFFUSEMATERIAL_UNIFORM], 1,
                     divBySize, 0);
         }
 
     }
 
     @Override
     public void renderGLParticleArray(LinkedList<GLParticleArray> objectList) {
 
         // Always use the same attributes for GLParticleArrays.
         int arrayCount = mParticlePrograms.getAttribArrayCount();
         enableVertexAttributes(arrayCount);
         mCurrentArrays = arrayCount;
 
         Material material;
         GLParticleArray pArray;
         int count = objectList.size();
         for (int i = 0; i < count; i++) {
             pArray = objectList.get(i);
 
             // Check renderflag.
             if (pArray.renderFlag) {
 
                 material = pArray.material;
                 int type = pArray.getParticleType();
                 int programIndex = type;
 
                 int program = mParticlePrograms.getProgramNameByIndex(programIndex);
                 int[] location = mParticlePrograms.getUniformLocations(programIndex);
                 if (mCurrentProgram != program) {
                     mCurrentProgram = program;
                     mProgramHandler.useProgram(program);
                 }
 
                 if (mBlendingEnabled && material.sourceBlend == ConstantValues.NONE) {
                     mGraphicsUtilities.disable(ConstantValues.BLEND);
                     mBlendingEnabled = false;
                 } else if (!mBlendingEnabled && material.sourceBlend != ConstantValues.NONE) {
                     mBlendingEnabled = true;
                     mGraphicsUtilities.enable(ConstantValues.BLEND);
                     mGraphicsUtilities.blendFunc(material.sourceBlend, material.destBlend);
                     mSrcBlend = material.sourceBlend;
                     mDstBlend = material.destBlend;
                 } else if (mBlendingEnabled && (mSrcBlend!=material.sourceBlend ||
                         mDstBlend != material.destBlend)) {
                     mGraphicsUtilities.blendFunc(material.sourceBlend, material.destBlend);
                     mSrcBlend = material.sourceBlend;
                     mDstBlend = material.destBlend;
                 }
                 //Set matrix for particle array, this matrix is pre-modelView.
                 //this means that it is applied before movement of particles, emitted
                 //particles can have position offset and a rotation. Particles movement
                 //will not be affected.
                 mProgramHandler.setUniformMatrix(
                         location[ParticleProgramCollection.TRANSFORM_MATRIX_UNIFORM], 1,
                         pArray.modelViewMatrix, 0);
                 mProgramHandler.setUniformMatrix(
                         location[ParticleProgramCollection.PERSPECTIVEMATRIX_UNIFORM], 1,
                         mPerspectiveMatrix, 0);
 
                 // Setup the uniforms needed for this program.
 
                 // Set the uniform values packed into vec4.
                 mProgramHandler.setUniformVector(
                         location[ParticleProgramCollection.DATA_UNIFORM], 2,
                         pArray.data_uniform, 0);
 
                 /**
                  * Program is selected, setup uniforms
                  */
 
                 if (type == GLParticleArray.TYPE_IMAGE_PARTICLE) {
                     setBlitMaterial(program, material, material.texture.length);
                     // Set texture and texture size
                     mProgramHandler.setUniformVector2(
                             location[ParticleProgramCollection.BUFFERSIZE_UNIFORM], 1,
                             mOneByPortSize, 0);
                     mProgramHandler.setUniformInt(
                             location[ParticleProgramCollection.TEXTURE1_UNIFORM], 0);
                 }
                 renderGLParticleArray(
                         mParticlePrograms.getProgramNameByIndex(programIndex),
                         mParticlePrograms.getUniformLocations(programIndex), pArray);
                 mGLDrawCalls++;
                 mGraphicsUtilities.checkError();
             }
         }
 
     }
 
     /**
      * Internal method to read a block of pixels from the colorbuffer.
      *
      * @param x
      * @param y
      * @param width
      * @param height
      * @param format
      * @param type
      * @param buffer Storage for buffer.
      */
     protected abstract void internalReadPixels(int x,
             int y,
             int width,
             int height,
             int format,
             int type,
             IntBuffer buffer);
 
     @Override
     public int[] readPixels(int x, int y, int width, int height, int format) {
         int[] buffer = new int[width*height];
         IntBuffer iBuffer = IntBuffer.wrap(buffer);
         internalReadPixels(x,
                            y,
                            width,
                            height,
                            format,
                            ConstantValues.UNSIGNED_BYTE, iBuffer);
         //Pixels are ordered wrong
         int size = buffer.length;
         int pixel;
         for (int i = 0; i < size; i++) {
             pixel = buffer[i];
             buffer[i] = (pixel & 0x0ff000000) |
                         ((pixel & 0x0ff0000)>>>16) |
                         (pixel & 0x0ff00) | //green at same position
                         ((pixel & 0x0ff)<<16);
         }
         return iBuffer.array();
     }
 
     @Override
     public void rotateScene(float angle, float xaxis, float yaxis, float zaxis) {
         float[] result = new float[16];
         Matrix.rotateM(result, mPerspectiveMatrix, angle, xaxis, yaxis, zaxis);
         setPerspectiveMatrix(result);
     }
 
     @Override
     public void setOrthogonalProjection(float left, float right, float bottom, float top,
             float near, float far) {
         Matrix.orthoM(mPerspectiveMatrix, 0, left, right, bottom, top, near, far);
         mPerspectiveMatrixDirty = true;
     }
 
 }
