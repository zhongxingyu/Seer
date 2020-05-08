 package render.util;
 
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class ShaderLoader
 {
 	
 	private static HashMap<String, Integer> programs = new HashMap<>();
 	
 	public static int loadProgram(String vertexName, String fragmentName)
 	{
 		int program = glCreateProgram();
 		int vertex = glCreateShader(GL_VERTEX_SHADER);
 		int fragment = glCreateShader(GL_FRAGMENT_SHADER);
 		
 		StringBuilder vertexSource = new StringBuilder();
 		StringBuilder fragmentSource = new StringBuilder();
 		
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(vertexName + ".vert"));
 			String line;
 			while((line = reader.readLine()) != null)
 				vertexSource.append(line).append('\n');
 			reader.close();
 		}
 		catch(IOException e){}
 
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(fragmentName + ".frag"));
 			String line;
 			while((line = reader.readLine()) != null)
 				fragmentSource.append(line).append("\n");
 			reader.close();
 		}
 		catch(IOException e){}
 		
 		glShaderSource(vertex, vertexSource);
 		glCompileShader(vertex);
 		if(glGetShaderi(vertex, GL_COMPILE_STATUS) == GL_FALSE)
		{
 			System.err.println("Vertex-Shader " + vertexName + " could not be compiled");
			System.err.println("Error log:");
			System.err.println(glGetShaderInfoLog(vertex, 1024));
		}
 		
 		glShaderSource(fragment, fragmentSource);
 		glCompileShader(fragment);
 		if(glGetShaderi(fragment, GL_COMPILE_STATUS) == GL_FALSE)
		{
 			System.err.println("Fragment-Shader " + fragmentName + " could not be compiled");
			System.err.println("Error log:");
			System.err.println(glGetShaderInfoLog(fragment, 1024));
		}
 		
 		glAttachShader(program, vertex);
 		glAttachShader(program, fragment);
 		
 		glLinkProgram(program);
 		glValidateProgram(program);
 		
 		return program;
 	}
 	
 	public static void createProgram(String name)
 	{
 		programs.put(name, loadProgram(name, name));
 	}
 
 	public static void createProgram(String vertexName, String fragmentName)
 	{
 		programs.put(vertexName + "-" + fragmentName, loadProgram(vertexName, fragmentName));
 	}
 	
 	
 	public static boolean useProgram(String name)
 	{
 		return useProgram(name, true);
 	}
 	
 	public static boolean useProgram(String name, boolean doWork)
 	{
 		Integer i = programs.get(name);
 		if(i == null)
 		{
 			if(doWork)createProgram(name);
 			else return false;
 			i = programs.get(name);
 		}
 		if(i != null)
 		{
 			glUseProgram(i);
 			return true;
 		}
 		return false;		
 	}
 }
