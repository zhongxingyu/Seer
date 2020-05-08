 package cs5625.deferred.materials;
 
 import javax.media.opengl.GL2;
 import javax.vecmath.Color3f;
 
 import cs5625.deferred.misc.OpenGLException;
 import cs5625.deferred.rendering.ShaderProgram;
 
 /**
  * IsotropicWardMaterial.java
  * 
  * Implements the Isotropic Ward shading model.
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2013, Computer Science Department, Cornell University.
  * 
  * @author Asher Dunn (ad488), Ivaylo Boyadzhiev (iib2)
  * @date 2013-01-29
  */
 public class IsotropicWardMaterial extends Material
 {
 	/* Isotropic Ward material properties. */
 	private Color3f mDiffuseColor = new Color3f(1.0f, 1.0f, 1.0f);
 	private Color3f mSpecularColor = new Color3f(1.0f, 1.0f, 1.0f);
 	private float mAlpha = 0.4f;	
 	
 	/* Optional textures for texture parameterized rendering. */
 	private Texture2D mDiffuseTexture = null;
 	private Texture2D mSpecularTexture = null;
 	private Texture2D mAlphaTexture = null;
 	
 	/* Uniform locations for the shader. */
 	private int mDiffuseUniformLocation = -1;
 	private int mSpecularUniformLocation = -1;
 	private int mAlphaUniformLocation = -1;
 
 	private int mHasDiffuseTextureUniformLocation = -1;
 	private int mHasSpecularTextureUniformLocation = -1;
 	private int mHasAlphaTextureUniformLocation = -1;
 	
 	public IsotropicWardMaterial()
 	{
 		/* Default constructor */
 	}
 	
 	public IsotropicWardMaterial(Color3f diffuse)
 	{
 		mDiffuseColor = diffuse;
 	}
 	
 	public IsotropicWardMaterial(Color3f diffuse, Color3f specular, float alpha)
 	{
 		mDiffuseColor = diffuse;
 		mSpecularColor = specular;
 		mAlpha = alpha;
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
 	
 	public float getAlpha()
 	{
 		return mAlpha;
 	}
 	
 	public void setAlpha(float m)
 	{
 		mAlpha = m;
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
 	
 	public Texture2D getAlphaTexture()
 	{
 		return mAlphaTexture;
 	}
 	
 	public void setAlphaTexture(Texture2D texture)
 	{
 		mAlphaTexture = texture;
 	}			
 	
 	@Override
 	public String getShaderIdentifier()
 	{
 		return "shaders/material_isotropic_ward";
 	}
 		
 	@Override
 	public void bind(GL2 gl) throws OpenGLException
 	{
 		/* Bind shader and any textures, and update uniforms. */
 		getShaderProgram().bind(gl);
 		
 		// TODO PA1: Set shader uniforms and bind any textures.
 		gl.glUniform3f(mDiffuseUniformLocation, mDiffuseColor.x, mDiffuseColor.y, mDiffuseColor.z);
 		gl.glUniform3f(mSpecularUniformLocation, mSpecularColor.x, mSpecularColor.y, mSpecularColor.z);
 		gl.glUniform1f(mAlphaUniformLocation, mAlpha);
 		
		if (mDiffuseTexture != null) mDiffuseTexture.bind(gl, 0);
		if (mSpecularTexture != null) mSpecularTexture.bind(gl, 1);
		if (mAlphaTexture != null) mAlphaTexture.bind(gl, 2);
 		
 		gl.glUniform1f(mHasDiffuseTextureUniformLocation, (mDiffuseTexture != null ? 1 : 0));
 		gl.glUniform1f(mHasSpecularTextureUniformLocation, (mSpecularTexture != null ? 1 : 0));
 		gl.glUniform1f(mHasAlphaTextureUniformLocation, (mAlphaTexture != null ? 1 : 0));
 	}
 	
 	@Override
 	protected void initializeShader(GL2 gl, ShaderProgram shader)
 	{
 		/* Get locations of uniforms in this shader. */
 		mDiffuseUniformLocation = shader.getUniformLocation(gl, "DiffuseColor");
 		mSpecularUniformLocation = shader.getUniformLocation(gl, "SpecularColor");
 		mAlphaUniformLocation = shader.getUniformLocation(gl, "Alpha");		
 		
 		mHasDiffuseTextureUniformLocation = shader.getUniformLocation(gl, "HasDiffuseTexture");
 		mHasSpecularTextureUniformLocation = shader.getUniformLocation(gl, "HasSpecularTexture");
 		mHasAlphaTextureUniformLocation = shader.getUniformLocation(gl, "HasAlphaTexture");
 		
 		/* These are only set once, so set them here. */
 		shader.bind(gl);
 		gl.glUniform1i(shader.getUniformLocation(gl, "DiffuseTexture"), 0);
 		gl.glUniform1i(shader.getUniformLocation(gl, "SpecularTexture"), 1);
 		gl.glUniform1i(shader.getUniformLocation(gl, "AlphaTexture"), 2);
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
 		if (mAlphaTexture != null) mAlphaTexture.unbind(gl);
 		
 	}
 }
