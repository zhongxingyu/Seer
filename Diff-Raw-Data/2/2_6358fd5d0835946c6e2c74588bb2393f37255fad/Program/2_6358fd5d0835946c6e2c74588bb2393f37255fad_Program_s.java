 package net.aib42.lwjgl.shader;
 
 /**
  * A simple wrapper for an OpenGL program object
  *
  * @author aib
  * @date 2012-01-25
  */
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL20;
 
 public class Program
 {
 	protected int programID;
 
 	public Program() throws ShaderException
 	{
 		programID = GL20.glCreateProgram();
 
 		if (programID == 0) {
 			throw new ShaderException("Unable to create program object");
 		}
 	}
 
 	public int getProgramID()
 	{
 		return programID;
 	}
 
 	public Program attachShader(Shader shader)
 	{
 		GL20.glAttachShader(programID, shader.getShaderID());
 
 		return this;
 	}
 
 	public Program link() throws ShaderException
 	{
 		GL20.glLinkProgram(programID);
 
 		if (GL20.glGetProgram(programID, GL20.GL_LINK_STATUS) != GL11.GL_TRUE) {
 			throw new ShaderException("Unable to link program", getProgramInfoLog());
 		}
 
 		return this;
 	}
 
 	public void use()
 	{
 		GL20.glUseProgram(programID);
 	}
 
 	public void stopUse()
 	{
 		GL20.glUseProgram(0);
 	}
 
 	public boolean setUniform(String name, int value)
 	{
 		int location = GL20.glGetUniformLocation(programID, name);
 
 		if (location == -1) {
 			return false;
 		}
 
 		GL20.glUniform1i(location, value);
 
 		return true;
 	}
 
 	public boolean setUniform(String name, float value)
 	{
 		int location = GL20.glGetUniformLocation(programID, name);
 
 		if (location == -1) {
 			return false;
 		}
 
 		GL20.glUniform1f(location, value);
 
 		return true;
 	}
 
 	public String getProgramInfoLog()
 	{
		return GL20.glGetShaderInfoLog(programID, GL20.glGetProgram(programID, GL20.GL_INFO_LOG_LENGTH));
 	}
 }
