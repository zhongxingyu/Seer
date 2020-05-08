 package opengl;
 
 import static org.lwjgl.opengl.ARBFragmentShader.*;
 import static org.lwjgl.opengl.ARBShaderObjects.*;
 import static org.lwjgl.opengl.ARBVertexShader.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class FXShader
 {
 	private int shaderID;
 	private int vertID;
 	private int fragID;
 	
 	public FXShader(String vertfilename,String fragfilename)
 	{
 		// Read Vertex Shader from File
 		String vertShader=FXFile.read(vertfilename);
 		String fragShader=FXFile.read(fragfilename);
 		
 		// Create Shader
 		glCreateProgramObjectARB();
 		
 		// Create VertexShader
 		this.vertID=glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
 		glShaderSourceARB(this.vertID,vertShader);
 		glCompileShaderARB(this.vertID);
 		
 		// Create FragmentShader
 		this.fragID=glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);
 		glShaderSourceARB(this.fragID,fragShader);
 		glCompileShaderARB(this.fragID);
 		
		// Attack Shaders to main Shader
        glAttachObjectARB(this.shaderID,this.vertID);
        glAttachObjectARB(this.shaderID,this.fragID);
        glLinkProgramARB(this.shaderID);
        glValidateProgramARB(this.shaderID);
 	}
 	
 	public void bind()
 	{
 		glUseProgramObjectARB(this.shaderID);
 	}
 	
     public void unbind()
     {
         glUseProgramObjectARB(0);
     }
     
     public void release()
     {
         glDetachObjectARB(this.shaderID,this.vertID);
         glDetachObjectARB(this.shaderID,this.fragID);
         glDeleteObjectARB(this.vertID);
         glDeleteObjectARB(this.fragID);
         glUseProgramObjectARB(0);
     } 
    
     public void setUniformVec1(String index,float value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform1fARB(location,value);
     }
     public void setUniformVec2(String index,Float2D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform2fARB(location,value.x,value.y);
     }
     public void setUniformVec3(String index,Float3D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform3fARB(location,value.x,value.y,value.z);
     }
     public void setUniformVec4(String index,Float4D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform4fARB(location,value.x,value.y,value.z,value.a);
     }
     
     public void setUniformVec1(String index,int value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform1iARB(location,value);
     }  
     public void setUniformVec2(String index,Int2D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform2iARB(location,value.x,value.y);
     }
     public void setUniformVec3(String index,Int3D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform3iARB(location,value.x,value.y,value.z);
     }
     public void setUniformVec4(String index,Int4D value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform4iARB(location,value.x,value.y,value.z,value.a);
     }
     
     public void setUniformTextureID(String index,int value)
     {
         int location=glGetUniformLocationARB(this.shaderID,index);
         glUniform1iARB(location,value);
     }
     public void setUniformTexture(String index,FXTexture texture)
     {
     	int location=glGetUniformLocationARB(this.shaderID,index);
     	glUniform1iARB(location,texture.getTextureID());
     }
 }
