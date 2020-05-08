 package com.sweep2d.Engine;
 
 import java.util.HashMap;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import java.io.InputStream;
import com.sweep2d.Game.R;
 import com.sweep2d.Game.MainGame;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLUtils;
 import android.util.Log;
 
 public class SharedResources
 {
 
 	HashMap<String, Integer> textures;
 	HashMap<String, Integer> shaderPrograms;
 
 	private Context resourcesContext;
 
 	public enum ResourceType
 	{
 		Texture(0), ShaderProgram(1);
 
 		public int type;
 
 		ResourceType(int type)
 		{
 			this.type = type;
 		}
 	}
 
 	public SharedResources(Context context)
 	{
 		textures = new HashMap<String, Integer>();
 		shaderPrograms = new HashMap<String, Integer>();
 		resourcesContext = context;
 	}
 
 	private boolean LoadTexture(String name, String filePath)
 	{
         int[] textureBuffer = new int[1];
         GLES20.glGenTextures(1, textureBuffer, 0);
 
         int textureID;
         
         textureID = textureBuffer[0];
         GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);
 
         GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                 GLES20.GL_NEAREST);
         GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                 GLES20.GL_TEXTURE_MAG_FILTER,
                 GLES20.GL_LINEAR);
 
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                 GLES20.GL_REPEAT);
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                 GLES20.GL_REPEAT);
         
         GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
         GLES20.glEnable(GLES20.GL_BLEND); 
         
 
 //      USING CONTENT CONTEXT
         InputStream is = null;
 
         AssetManager am = resourcesContext.getAssets(); 
         try
 		{
 			is = am.open(filePath);
 		} catch (IOException e1)
 		{
 			Log.e("[SharedResources]","Tex not found: "+filePath);
 			e1.printStackTrace();
 		}
         
         Bitmap bitmap;
         try {
             bitmap = BitmapFactory.decodeStream(is);
         } finally {
             try {
                 is.close();
             } catch(IOException e) {
                 // Ignore.
             }
         }
 
         GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
         bitmap.recycle();
 		if (textureID != 0)
 		{
 			textures.put(name, Integer.valueOf(textureID) );
 			return true;
 		}
 		return false;
 	}
 
 	private boolean LoadShaderProgram(String name, String vertexShaderPath,
 			String fragShaderPath)
 	{
 
 		String vertexSource = ReadStringFromFile(vertexShaderPath);
 		String fragmentSource = ReadStringFromFile(fragShaderPath);
 		
 		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
 		if (vertexShader == 0)
 		{
 			return false;
 		}
 
 		int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
 		if (pixelShader == 0)
 		{
 			return false;
 		}
 
 		int programID = GLES20.glCreateProgram();
 		if (programID != 0)
 		{
 			GLES20.glAttachShader(programID, vertexShader);
 			checkGlError("glAttachShader");
 			GLES20.glAttachShader(programID, pixelShader);
 			checkGlError("glAttachShader");
 			GLES20.glLinkProgram(programID);
 			int[] linkStatus = new int[1];
 			GLES20.glGetProgramiv(programID, GLES20.GL_LINK_STATUS, linkStatus,
 					0);
 			if (linkStatus[0] != GLES20.GL_TRUE)
 			{
 				Log.e(TAG, "Could not link program: ");
 				Log.e(TAG, GLES20.glGetProgramInfoLog(programID));
 				GLES20.glDeleteProgram(programID);
 				programID = 0;
 			}
 			Log.i(TAG,"Shader loaded with ID: " + programID);
 		}
 		else
 		{
 			Log.e(TAG,"Could not link the shader");
 		}
 
 		shaderPrograms.put(name,Integer.valueOf(programID));
 
 		return true;
 	}
 
 	private int loadShader(int shaderType, String source)
 	{
 		int shader = GLES20.glCreateShader(shaderType);
 		if (shader != 0)
 		{
 			GLES20.glShaderSource(shader, source);
 			GLES20.glCompileShader(shader);
 			int[] compiled = new int[1];
 			GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
 			if (compiled[0] == 0)
 			{
 				Log.e(TAG, "Could not compile shader " + shaderType + ":");
 				Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
 				GLES20.glDeleteShader(shader);
 				shader = 0;
 			}
 			Log.i(TAG, "Shader successfully loaded");
 		}
 		return shader;
 	}
 
 	public boolean Loadresources(Resource[] resources)
 	{
 		boolean allGood = true;
 
 		for (int i = 0; i < resources.length; i++)
 		{
 			if (resources[i].type == ResourceType.Texture)
 			{
 				if(resources[i].filePaths.length != 1)
 				{
 					//RAISE ERROR (STILL NEEDS A DEBUG ERROR HANDLER)
 					allGood = false;
 				}
 				else
 				{
 					allGood = LoadTexture(resources[i].name,resources[i].filePaths[0]);
 				}
 			}
 			if (resources[i].type == ResourceType.ShaderProgram)
 			{
 				if(resources[i].filePaths.length  != 2)
 				{
 					//RAISE ERROR (STILL NEEDS A DEBUG ERROR HANDLER)
 					allGood = false;
 				}
 				allGood = LoadShaderProgram(resources[i].name,resources[i].filePaths[0],resources[i].filePaths[1]);
 			}
 		}
 		return allGood;
 	}
 
 	public int GetTexture(String name)
 	{
 		for(int i =0 ; i<textures.size(); i++)
 		{
 
 		}
 		int textureID = textures.get(name);
 		return textureID;
 	}
 	
 	public int GetShaderProgram(String name)
 	{
 		int programID = shaderPrograms.get(name);
 		return programID;
 	}
 
 	private void checkGlError(String op)
 	{
 		int error;
 		while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR)
 		{
 			Log.e(TAG, op + ": glError " + error);
 			throw new RuntimeException(op + ": glError " + error);
 		}
 	}
 
 	String ReadStringFromFile(String path)
 	{
 
 		// Using the resource manager
 		// InputStream inputStream =
 		// resourcesContext.getResources().openRawResource(R.raw.robot);
 
 		// Using file paths
 		InputStream inputStream = null;
 		String finalString = "";
         AssetManager am = resourcesContext.getAssets(); 
         try
 		{
         	inputStream = am.open(path);
         	
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					inputStream));
 
 			String line = reader.readLine();
 
 			while (line != null)
 			{
 				finalString += line;
 				finalString += '\n';
 				line = reader.readLine();
 			}
 			inputStream.close();
 		} catch (FileNotFoundException e)
 		{
 			Log.e("[SharedResources]","GLGL not found: "+path);
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		return finalString;
 	}
 	String TAG = "[SharedResources]";
 }
