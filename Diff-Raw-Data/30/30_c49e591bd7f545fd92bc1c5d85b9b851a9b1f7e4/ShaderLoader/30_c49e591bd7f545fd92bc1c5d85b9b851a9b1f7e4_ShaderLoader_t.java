 package com.vloxlands.render.util;
 
 import static org.lwjgl.opengl.GL11.GL_FALSE;
 import static org.lwjgl.opengl.GL20.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
import com.vloxlands.settings.CFG;

 public class ShaderLoader
 {
 	
 	private static HashMap<String, Integer> programs = new HashMap<>();
	private static String currentProgramName = "";
 	
 	public static int loadProgram(String path, String vertexName, String fragmentName)
 	{
 		int program = glCreateProgram();
 		int vertex = glCreateShader(GL_VERTEX_SHADER);
 		int fragment = glCreateShader(GL_FRAGMENT_SHADER);
 		
 		StringBuilder vertexSource = new StringBuilder();
 		StringBuilder fragmentSource = new StringBuilder();
 		
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(path + vertexName + ".vert"));
 			String line;
 			while((line = reader.readLine()) != null)
 				vertexSource.append(line).append('\n');
 			reader.close();
 		}
 		catch(IOException e){e.printStackTrace();}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(path + fragmentName + ".frag"));
 			String line;
 			while((line = reader.readLine()) != null)
 				fragmentSource.append(line).append("\n");
 			reader.close();
 		}
 		catch(IOException e){e.printStackTrace();}
 		
 		glShaderSource(vertex, vertexSource);
 		glCompileShader(vertex);
 		if(glGetShaderi(vertex, GL_COMPILE_STATUS) == GL_FALSE)
 			System.err.println(vertexName + "_vertex " + "Vertex-Shader '" + vertexName + "' could not be compiled \n Error log:\n" + glGetShaderInfoLog(vertex, 1024));
 		
 		glShaderSource(fragment, fragmentSource);
 		glCompileShader(fragment);
 		if(glGetShaderi(fragment, GL_COMPILE_STATUS) == GL_FALSE)
 			System.err.println(fragmentName + "_fragment " + "Fragment-Shader '" + fragmentName + "' could not be compiled \n Error log:\n" + glGetShaderInfoLog(fragment, 1024));
 		
 		glAttachShader(program, vertex);
 		glAttachShader(program, fragment);
 		
 		glLinkProgram(program);
 		glValidateProgram(program);
 		
 		return program;
 	}
 	
 	public static void createProgram(String path, String name)
 	{
 		programs.put(path + name, loadProgram(path, name, name));
 	}
 
 	public static void createProgram(String path, String vertexName, String fragmentName)
 	{
 		programs.put(path + vertexName + "-" + fragmentName, loadProgram(path, vertexName, fragmentName));
 	}
 	
 	
 	public static boolean useProgram(String path, String name)
 	{
 		return useProgram(path, name, true);
 	}
 	
 	public static boolean useProgram(String path, String name, boolean doWork)
 	{
 		Integer i = programs.get(path + name);
 		if(i == null)
 		{
 			if(doWork)createProgram(path, name);
 			else return false;
 			i = programs.get(path + name);
 		}
 		if(i != null)
 		{
 			glUseProgram(i);
 			currentProgramName = name;
 			return true;
 		}
 		return false;		
 	}
 	
 	public static int getCurrentProgram()
 	{
		CFG.p(programs);
		return programs
				.get(
						currentProgramName
						);
 	}
 }
