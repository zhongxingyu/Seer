 package openGLCLInterfaces.openGL.shaders;
 
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL41.*;
 import main.*;
 import openGLCLInterfaces.Enumerations.ShaderType;
 
 /**
  * Represents a collection of shader stages.
  */
 public class ShaderProgram {
 
 	private int shaderProgram;
 
 	private String name;
 
 	/**
 	 * Creates an empty program.
 	 */
 	public ShaderProgram() {
 		this.shaderProgram = glCreateProgram();
 	}
 
 	public static ShaderProgram createShaderFromDir(String dir) {
 		String vertexShaderFile = dir + "/vert.c";
 		String fragmentShaderFile = dir + "/frag.c";
 		String geometryShaderFile = dir + "/geo.c";
		if (Utils.resourceExists(geometryShaderFile)) {
 			return createBasicShader(vertexShaderFile, fragmentShaderFile, geometryShaderFile);
		} else {
			return createBasicShader(vertexShaderFile, fragmentShaderFile);
 		}
 	}
 
 	/**
 	 * Creates a basic program with vertex shader and fragment shader.
 	 * 
 	 * @param vertexShaderFile
 	 *            The file with the code for the vertex shader.
 	 * @param fragmentShaderFile
 	 *            The file with the code for the fragment shader.
 	 * @return Returns a ShaderProgram object.
 	 */
 	public static ShaderProgram createBasicShader(String vertexShaderFile, String fragmentShaderFile) {
 		ShaderProgram shaderProgram = new ShaderProgram();
 		Shader vertexShader = new Shader(vertexShaderFile, ShaderType.VERTEX_SHADER);
 		Shader fragementShader = new Shader(fragmentShaderFile, ShaderType.FRAGMENT_SHADER);
 		shaderProgram.attachShader(vertexShader);
 		shaderProgram.attachShader(fragementShader);
 		shaderProgram.link();
 		vertexShader.delete();
 		fragementShader.delete();
 		shaderProgram.name = vertexShaderFile.substring(1, vertexShaderFile.length() - 2).replace(
 				"/", ".")
 				+ " - "
 				+ fragmentShaderFile.substring(1, fragmentShaderFile.length() - 2)
 						.replace("/", ".");
 		shaderProgram.printDebugInfos();
 		return shaderProgram;
 	}
 
 	/**
 	 * Creates a basic program with vertex shader, fragment shader and geometry
 	 * shader.
 	 * 
 	 * @param vertexShaderFile
 	 *            The file with the code for the vertex shader.
 	 * @param fragmentShaderFile
 	 *            The file with the code for the fragment shader.
 	 * @param geometryShaderFile
 	 *            The file with the code for the geometry shader.
 	 * @return Returns a ShaderProgram object.
 	 */
 	public static ShaderProgram createBasicShader(String vertexShaderFile,
 			String fragmentShaderFile, String geometryShaderFile) {
 		ShaderProgram shaderProgram = new ShaderProgram();
 		Shader vertexShader = new Shader(vertexShaderFile, ShaderType.VERTEX_SHADER);
 		Shader fragementShader = new Shader(fragmentShaderFile, ShaderType.FRAGMENT_SHADER);
 		Shader geometryShader = new Shader(geometryShaderFile, ShaderType.GEOMETRY_SHADER);
 		shaderProgram.attachShader(vertexShader);
 		shaderProgram.attachShader(fragementShader);
 		shaderProgram.attachShader(geometryShader);
 		shaderProgram.link();
 		vertexShader.delete();
 		fragementShader.delete();
 		geometryShader.delete();
 		shaderProgram.name = vertexShaderFile.substring(1, vertexShaderFile.length() - 2).replace(
 				"/", ".")
 				+ " - "
 				+ fragmentShaderFile.substring(1, fragmentShaderFile.length() - 2)
 						.replace("/", ".")
 				+ " - "
 				+ geometryShaderFile.substring(1, geometryShaderFile.length() - 2)
 						.replace("/", ".");
 		shaderProgram.printDebugInfos();
 		return shaderProgram;
 	}
 
 	/**
 	 * @return Returns the id of this shader program.
 	 */
 	public int getID() {
 		return this.shaderProgram;
 	}
 
 	/**
 	 * Attaches a shader to the program.
 	 * 
 	 * @param shader
 	 *            The shader to attach.
 	 */
 	public void attachShader(Shader shader) {
 		glAttachShader(this.shaderProgram, shader.id);
 	}
 
 	/**
 	 * Can set extended parameters. Sometimes used for avanced shaders like a
 	 * geometry shader.
 	 * 
 	 * @param pname
 	 * @param value
 	 */
 	public void setParameter(int pname, int value) {
 		glProgramParameteri(shaderProgram, pname, value);
 	}
 
 	/**
 	 * Links the program.
 	 */
 	public void link() {
 		glLinkProgram(this.shaderProgram);
 	}
 
 	/**
 	 * Gets a uniform variable object from a uniform name.
 	 * 
 	 * @param variable
 	 *            The name of the variable.
 	 * @return Returns a UniformVariable object.
 	 */
 	public UniformVariable getUniformVariable(String variable) {
 		return new UniformVariable(glGetUniformLocation(this.shaderProgram, variable));
 	}
 
 	/**
 	 * Gets a vertex attribute object from attribute name.
 	 * 
 	 * @param variable
 	 *            the name of the vertex attribute.
 	 * @return Returns a VertexAttribute object.
 	 */
 	public VertexAttribute getVertexAttribute(String variable) {
 		return new VertexAttribute(glGetAttribLocation(this.shaderProgram, variable));
 	}
 
 	/**
 	 * Enables the shader program.
 	 */
 	public void enable() {
 		glUseProgram(this.shaderProgram);
 	}
 
 	/**
 	 * Disables the shader program.
 	 */
 	public void disable() {
 		glUseProgram(0);
 	}
 
 	/**
 	 * Gets informations about shader program state.
 	 * 
 	 * @param program
 	 * @return
 	 */
 	private String getInfoLog(int program) {
 		int length = glGetProgrami(program, GL_INFO_LOG_LENGTH);
 		return glGetProgramInfoLog(program, length);
 	}
 
 	/**
 	 * Prints overall shader state to the console.
 	 */
 	public void printDebugInfos() {
 		System.out.println("-----------" + this.name + "-----------");
 		System.out.print(this.getInfoLog(this.shaderProgram));
 		System.out.println();
 	}
 }
