 package graphics.compatibility;
 
 
 import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
 import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
 import static org.lwjgl.opengl.GL20.glAttachShader;
 import static org.lwjgl.opengl.GL20.glCreateProgram;
 import static org.lwjgl.opengl.GL20.glGetAttribLocation;
 import static org.lwjgl.opengl.GL20.glGetUniformLocation;
 import static org.lwjgl.opengl.GL20.glLinkProgram;
 import static org.lwjgl.opengl.GL20.glUseProgram;
 import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
 import graphics.GLOperations;
 import graphics.Shader;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lwjgl.opengl.Display;
 
 public class CompatibilityShader implements Shader{
 	
	private static final String ATTRIBUTE = "attribute";
 	private static final String UNIFORM = "uniform";
 	
 	
 	private int shaderProgram;
 	private int fragShader;
 	private int vertShader;
 	
 	private Map<String, Integer> uniforms;
 	private Map<String, Integer> attributes;
 	
 	
 	public CompatibilityShader(String fragmentShaderFile, String vertexShaderFile)
 	{
 		uniforms = new HashMap<String, Integer>();
 		attributes = new HashMap<String, Integer>();
 		
 		String vertexShaderText = GLOperations.loadFile(vertexShaderFile);
 		String fragmentShaderText = GLOperations.loadFile(fragmentShaderFile);
 		
     	try{
     		vertShader = GLOperations.loadShaderString(vertexShaderText, GL_VERTEX_SHADER);
     		fragShader = GLOperations.loadShaderString(fragmentShaderText, GL_FRAGMENT_SHADER);
     	} 
     	catch (Exception e)
         {
         	System.out.println("ERROR IN SHADER LOADING");
         	System.out.println(e);
 
         	Display.destroy();
         }
     	
         shaderProgram = glCreateProgram();
         
         glAttachShader(shaderProgram, vertShader);
         glAttachShader(shaderProgram, fragShader);
         
         //todo:update so shaders can output to an fbo
         glBindFragDataLocation( shaderProgram, 0, "outColor");
         
         glLinkProgram(shaderProgram);
         glUseProgram(shaderProgram);
         
         addAttribute(vertexShaderText);
        addAttribute(fragmentShaderText);
         
         addUniform(vertexShaderText);
         addUniform(fragmentShaderText);
         
 	}
 
 	private void addAttribute(String file)
 	{
 		String[] lines = file.split("\n");
 		
 		for(String line : lines)
 		{
			if(line.contains(ATTRIBUTE))
 			{
 				String[] words = line.split(" ");
 				String word = words[words.length - 1].replace(";", "");
 				
 				int value = glGetAttribLocation(shaderProgram, word);
 
 				attributes.put(word, value);
 			}
 		}
 	}
 	
 	private void addUniform(String file)
 	{
 		String[] lines = file.split("\n");
 		
 		for(String line : lines)
 		{
 			if(line.contains(UNIFORM))
 			{
 				String[] words = line.split(" ");
 				String word = words[words.length - 1].replace(";", "");
 				int value = glGetUniformLocation(shaderProgram, word);
 				
 				uniforms.put(word, value);
 			}
 		}
 	}
 		
 	
 
 	public Map<String, Integer> getUniforms() {
 		return uniforms;
 	}
 
 	public Map<String, Integer> getAttributes() {
 		return attributes;
 	}
 
 	
 	
 
 }
