 package cs5625.deferred.materials;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Color3f;
 
 import cs5625.deferred.misc.OpenGLException;
 import cs5625.deferred.rendering.ShaderProgram;
 
 /**
  * AnisotropicWardMaterial.java
  * 
  * Implements the Anisotropic Ward shading model.
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2013, Computer Science Department, Cornell University.
  * 
  * @author Asher Dunn (ad488), Ivaylo Boyadzhiev (iib2)
  * @date 2013-01-29
  */
 public class AnisotropicWardMaterial extends Material
 {
 	/* Isotropic Ward material properties. */
 	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
 	private Color3f mSpecularColor = new Color3f(1.0f, 1.0f, 1.0f);
 	private float mAlphaX = 0.15f;	
 	private float mAlphaY = 0.5f;
 	
 	/* Optional textures for texture parameterized rendering. */
 	private Texture2D mDiffuseTexture = null;
 	private Texture2D mSpecularTexture = null;
 	private Texture2D mAlphaXTexture = null;
 	private Texture2D mAlphaYTexture = null;
 	
 	/* Uniform locations for the shader. */
 	private int mDiffuseUniformLocation = -1;
 	private int mSpecularUniformLocation = -1;
 	private int mAlphaXUniformLocation = -1;
 	private int mAlphaYUniformLocation = -1;
 
 	private int mHasDiffuseTextureUniformLocation = -1;
 	private int mHasSpecularTextureUniformLocation = -1;
 	private int mHasAlphaXTextureUniformLocation = -1;
 	private int mHasAlphaYTextureUniformLocation = -1;
 	
 	public AnisotropicWardMaterial()
 	{
 		/* Default constructor */
 	}
 	
 	public AnisotropicWardMaterial(Color3f diffuse)
 	{
 		mDiffuseColor = diffuse;
 	}
 	
 	public AnisotropicWardMaterial(Color3f diffuse, Color3f specular, float alphaX, float alphaY)
 	{
 		mDiffuseColor = diffuse;
 		mSpecularColor = specular;
 		mAlphaX = alphaX;
 		mAlphaY = alphaY;
 	}
 	
 	public Color3f getDiffuseColor()
 	{
 		return mDiffuseColor;
 	}
 	
 	public void setDiffuseColor(Color3f diffuse)
 	{
 		mDiffuseColor = diffuse;
 	}
 
 	public Color3f getSpecularColor()
 	{
 		return mSpecularColor;
 	}
 	
 	public void setSpecularColor(Color3f specular)
 	{
 		mSpecularColor = specular;
 	}
 	
 	public float getAlphaX()
 	{
 		return mAlphaX;
 	}
 	
 	public void setAlphaX(float m)
 	{
 		mAlphaX = m;
 	}
 	
 	public float getAlphaY()
 	{
 		return mAlphaY;
 	}
 	
 	public void setAlphaY(float m)
 	{
 		mAlphaY = m;
 	}
 
 	public Texture2D getDiffuseTexture()
 	{
 		return mDiffuseTexture;
 	}
 	
 	public void setDiffuseTexture(Texture2D texture)
 	{
 		mDiffuseTexture = texture;
 	}
 
 	public Texture2D getSpecularTexture()
 	{
 		return mSpecularTexture;
 	}
 	
 	public void setSpecularTexture(Texture2D texture)
 	{
 		mSpecularTexture = texture;
 	}
 	
 	public Texture2D getAlphaXTexture()
 	{
 		return mAlphaXTexture;
 	}
 	
 	public void setAlphaXTexture(Texture2D texture)
 	{
 		mAlphaXTexture = texture;
 	}
 	
 	public Texture2D getAlphaYTexture()
 	{
 		return mAlphaYTexture;
 	}
 	
 	public void setAlphaYTexture(Texture2D texture)
 	{
 		mAlphaYTexture = texture;
 	}	
 
 	@Override
 	public String[] getRequiredVertexAttributes() {
 		return new String[] {"VertexTangent"};
 	}
 	
 	@Override
 	public String getShaderIdentifier()
 	{
 		return "shaders/material_anisotropic_ward";
 	}
 		
 	@Override
 	public void bind(GL2 gl) throws OpenGLException
 	{
 		/* Bind shader and any textures, and update uniforms. */
 		getShaderProgram().bind(gl);
 		
 		// TODO PA1: Set shader uniforms and bind any textures.
 		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
 		gl.glUniform3f(mSpecularUniformLocation, mSpecularColor.x, mSpecularColor.y, mSpecularColor.z);
 		gl.glUniform1f(mAlphaXUniformLocation, mAlphaX);
 		gl.glUniform1f(mAlphaYUniformLocation, mAlphaY);
 		
		if (mDiffuseTexture != null) mDiffuseTexture.bind(gl, mDiffuseTexture.getTextureTarget());
		if (mSpecularTexture != null) mSpecularTexture.bind(gl, mSpecularTexture.getTextureTarget());
		if (mAlphaXTexture != null) mAlphaXTexture.bind(gl, mAlphaXTexture.getTextureTarget());
		if (mAlphaYTexture != null) mAlphaYTexture.bind(gl, mAlphaYTexture.getTextureTarget());
 		
 		gl.glUniform1f(mHasDiffuseTextureUniformLocation, (mDiffuseTexture != null ? 1 : 0));
 		gl.glUniform1f(mHasSpecularTextureUniformLocation, (mSpecularTexture != null ? 1 : 0));
 		gl.glUniform1f(mHasAlphaXTextureUniformLocation, (mAlphaXTexture != null ? 1 : 0));
 		gl.glUniform1f(mHasAlphaYTextureUniformLocation, (mAlphaYTexture != null ? 1 : 0));
 	}
 	
 	@Override
 	protected void initializeShader(GL2 gl, ShaderProgram shader)
 	{
 		/* Get locations of uniforms in this shader. */
 		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
 		mSpecularUniformLocation = shader.getUniformLocation(gl, "SpecularColor");
 		mAlphaXUniformLocation = shader.getUniformLocation(gl, "AlphaX");
 		mAlphaYUniformLocation = shader.getUniformLocation(gl, "AlphaY");		
 		
 		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
 		mHasSpecularTextureUniformLocation = shader.getUniformLocation(gl, "HasSpecularTexture");
 		mHasAlphaXTextureUniformLocation = shader.getUniformLocation(gl, "HasAlphaXTexture");
 		mHasAlphaYTextureUniformLocation = shader.getUniformLocation(gl, "HasAlphaYTexture");
 		
 		/* These are only set once, so set them here. */
 		shader.bind(gl);
 		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
 		gl.glUniform1i(shader.getUniformLocation(gl, "SpecularTexture"), 1);
 		gl.glUniform1i(shader.getUniformLocation(gl, "AlphaXTexture"), 2);
 		gl.glUniform1i(shader.getUniformLocation(gl, "AlphaYTexture"), 3);
 		shader.unbind(gl);
 	}
 
 	@Override
 	public void unbind(GL2 gl)
 	{
 		/* Unbind everything bound in bind(). */
 		getShaderProgram().unbind(gl);
 		
 		// TODO PA1: Unbind any used textures.
 		if (mDiffuseTexture != null) mDiffuseTexture.unbind(gl);
 		if (mSpecularTexture != null) mSpecularTexture.unbind(gl);
 		if (mAlphaXTexture != null) mAlphaXTexture.unbind(gl);
 		if (mAlphaYTexture != null) mAlphaYTexture.unbind(gl);
 	}
 }
