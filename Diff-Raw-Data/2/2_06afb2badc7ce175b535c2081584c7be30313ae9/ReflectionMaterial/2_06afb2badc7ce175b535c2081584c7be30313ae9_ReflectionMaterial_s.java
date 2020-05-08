 package cs5625.deferred.materials;
 
 import javax.media.opengl.GL2;
 
 import cs5625.deferred.misc.OpenGLException;
 import cs5625.deferred.rendering.ShaderProgram;
 
 /**
  * ReflectionMaterial.java
  * 
  * Implements a Reflection (perfectly mirror) material. The cube map
  * is supplied as an index (1 means to use the static cube map, and 
  * 2, 3, ... means to use the corresponding dynamic cube map).
  * 
  * Written for Cornell CS 5625 (Interactive Computer Graphics).
  * Copyright (c) 2012, Computer Science Department, Cornell University.
  * 
  * @author Ivaylo Boyadzhiev (iib2)
  * @date 2012-03-27
  */
 public class ReflectionMaterial extends Material
 {
 	/* Reflection material properties. */	
 	private TextureCubeMap mCubeMap = null;
 
 	/* Uniform locations. */
 	private int mCubeMapIndexUniformLocation = -1;
 	
 	public ReflectionMaterial()
 	{
 		/* Default constructor. */
 	}
 	
 	public ReflectionMaterial(TextureCubeMap cubeMap)
 	{
 		mCubeMap = cubeMap;
 	}
 	
 	public TextureCubeMap getCubeMap()
 	{
 		return mCubeMap;
 	}
 	
 	public void setCubeMap(TextureCubeMap cubeMap)
 	{
 		mCubeMap = cubeMap;
 	}	
 
 	@Override
 	public void bind(GL2 gl) throws OpenGLException
 	{
 		/* Bind shader, and any textures, and update uniforms. */
 		getShaderProgram().bind(gl);
 
 		// TODO PA2: Set shader uniforms	
 		//mCubeMap.bind(gl, 0);
 		
		gl.glUniform1f(mCubeMapIndexUniformLocation, mCubeMap.mCubeMapIndex);
 	}
 
 	@Override
 	public void unbind(GL2 gl)
 	{
 		/* Unbind anything bound in bind(). */
 		getShaderProgram().unbind(gl);
 	}
 	
 	@Override
 	protected void initializeShader(GL2 gl, ShaderProgram shader)
 	{
 		/* Get locations of uniforms in this shader. */
 		mCubeMapIndexUniformLocation = shader.getUniformLocation(gl, "CubeMapIndex");
 	}
 
 	@Override
 	public String getShaderIdentifier()
 	{
 		return "shaders/material_reflection";
 	}
 
 }
