 package shadermanager;
 
 import java.util.HashMap;
 
 import com.sun.opengl.cg.CGcontext;
 import com.sun.opengl.cg.CGparameter;
 import com.sun.opengl.cg.CGprogram;
 import com.sun.opengl.cg.CgGL;
 
 /**
  * 
  * This class implements a ShaderManager, which manages the usage of shader
  * programs. The ShaderManager manages all shader programs. A certain shader
  * program can be executed Shaderprograms are kept in a hash map such that they
  * are accessible via their name. Moreover there is always a default shader
  * which is always applied when no other shader was specified.
  */
 public class ShaderManager {
 
 	/**
 	 * Loads a shader program from file.
 	 * 
 	 * @param cgcontext
 	 *            A context object.
 	 * @param profile
 	 *            The profile i.e. VERTEX_SHADER or FRAGMENT_SHADER.
 	 * @param filename
 	 *            Filename of the shader program to be loaded.
 	 * @return The shader program.
 	 */
 	public static CGprogram loadShader(CGcontext cgcontext, int profile,
 			String filename) {
 		CGprogram shaderprog = CgGL.cgCreateProgramFromFile(cgcontext,
 				CgGL.CG_SOURCE, filename, profile, null, null);
 		if (shaderprog == null) {
 			int err = CgGL.cgGetError();
 			System.err.println("Compile shader [" + filename + "] "
 					+ CgGL.cgGetErrorString(err));
 			if (CgGL.cgGetLastListing(cgcontext) != null) {
 				System.err.println(CgGL.cgGetLastListing(cgcontext) + "\n");
 			}
 			System.exit(1);
 		}
 
 		CgGL.cgGLLoadProgram(shaderprog);
 		CgGL.cgGLUnbindProgram(profile);
 
 		int err = CgGL.cgGetError();
 		if (err != CgGL.CG_NO_ERROR) {
 			System.out.println("Load shader [" + filename + "]: "
 					+ CgGL.cgGetErrorString(err));
 			System.exit(1);
 		}
 
 		return shaderprog;
 	}
 
 	private static String defaultInitialVPPath = "shader/vp_phongPerPixel.cg";
 	private static String defaultInitialFPPath = "shader/fp_phongPerPixel.cg";
 	private static String defaultInitialVPName = "phong";
 	private static String defaultInitialFPName = "phong";
 
 	// use singleton design pattern.
 	private static ShaderManager shadermanager = null;
 
 	// fields to hold the currently active shader programs.
 	private String cgVertexProgName = "", cgFragmentProgName = "";
 
 	private HashMap<String, Shader> vpshaderprograms = null,
 			fpshaderprograms = null;
 
 	// field to hold the default shader programs.
 	private String defaultVertexProgName = "", defaultFragmentProgName = "";
 
 	public String getDefaultVertexProgName() {
 		return defaultVertexProgName;
 	}
 
 	public void setDefaultVertexProgName(String defaultVertexProgName) {
 		this.defaultVertexProgName = defaultVertexProgName;
 	}
 
 	public String getDefaultFragmentProgName() {
 		return defaultFragmentProgName;
 	}
 
 	public void setDefaultFragmentProgName(String defaultFragmentProgName) {
 		this.defaultFragmentProgName = defaultFragmentProgName;
 	}
 
 	// flags which determine whether the default or any other shader program
 	// should be executed or not.
 	private boolean vertexShaderEnabled = true;
 
 	/**
 	 * @return Whether any vertex shader should be used or not.
 	 */
 	public boolean isVertexShaderEnabled() {
 		return vertexShaderEnabled;
 	}
 
 	/**
 	 * @param Sets
 	 * @param vertexShaderEnabled Determines whether any vertex shader should be used.
 	 */
 	public void setVertexShaderEnabled(boolean vertexShaderEnabled) {
 		if (vertexShaderEnabled == false){
 			// disable profile
 			CgGL.cgGLDisableProfile(cgVertexProfile);
			//reset to default
			this.bindVP();
 		}
 		this.vertexShaderEnabled = vertexShaderEnabled;
 	}
 
 	/**
 	 * @return Whether any fragment shader should be used or not.
 	 */
 	public boolean isFragShaderEnabled() {
 		return fragShaderEnabled;
 	}
 
 	/**
 	 * @param Sets
 	 * @param fragShaderEnabled Determines whether any fragment shader should be used.
 	 */
 	public void setFragShaderEnabled(boolean fragShaderEnabled) {
 		if (fragShaderEnabled == false){
 			// disable profile
 			CgGL.cgGLDisableProfile(cgFragProfile);
			//reset to default
			this.bindFP();
 		}
 		this.fragShaderEnabled = fragShaderEnabled;
 	}
 
 	private boolean fragShaderEnabled = true;
 
 	private int cgVertexProfile;
 
 	public int getCgVertexProfile() {
 		return cgVertexProfile;
 	}
 
 	private int cgFragProfile;
 
 	public int getCgFragProfile() {
 		return cgFragProfile;
 	}
 
 	private CGcontext cgContext;
 
 	public CGcontext getCgContext() {
 		return cgContext;
 	}
 
 	public void bindVP() throws IllegalArgumentException {
 		// disable profile
 		CgGL.cgGLDisableProfile(cgVertexProfile);
		CgGL.cgGLUnbindProgram(cgVertexProfile);
 		if (this.vertexShaderEnabled == true) {
 			// bind the new one
 			CgGL.cgGLBindProgram(this.vpshaderprograms
					.get(this.defaultVertexProgName).getShaderProg());
 			// enable profile
 			CgGL.cgGLEnableProfile(cgVertexProfile);
 		}
 	}
 
 	public void bindVP(String cgVPName) throws IllegalArgumentException {
 		if (this.vpshaderprograms.containsKey(cgVPName)) {
 			// disable profile
 			CgGL.cgGLDisableProfile(cgVertexProfile);
			CgGL.cgGLUnbindProgram(cgVertexProfile);
 			if (this.vertexShaderEnabled == true) {
 				// enable profile
 				CgGL.cgGLEnableProfile(cgVertexProfile);
 				// bind the new one
 				CgGL.cgGLBindProgram(this.vpshaderprograms.get(cgVPName).getShaderProg());
 			}
 		} else {
 			throw new IllegalArgumentException("The shader program " + cgVPName
 					+ " is unknown.");
 		}
 	}
 
 	
 	/**
 	 * Get the currently loaded fragment shader program.
 	 * @return Currently loaded fragment shader program.
 	 */
 	public CGprogram getBindedFragProg(){
 		return this.fpshaderprograms.get(this.getDefaultFragmentProgName()).getShaderProg();
 	}
 	
 	/**
 	 * Get the currently loaded vertex shader program.
 	 * @return Currently loaded vertex shader program.
 	 */
 	public CGprogram getBindedVertexProg(String name){
 		return this.vpshaderprograms.get(this.getDefaultVertexProgName()).getShaderProg();
 	}
 	
 	/**
 	 * Get fragment shader program by name.
 	 * @param name Name of fragment shader.
 	 * @return Fragment shader.
 	 */
 	public CGprogram getFragProg(String name){
 		return this.fpshaderprograms.get(name).getShaderProg();
 	}
 	
 	/**
 	 * Get vertex shader program by name.
 	 * @param name Name of vertex shader.
 	 * @return Vertex shader.
 	 */
 	public CGprogram getVertexProg(String name){
 		return this.vpshaderprograms.get(name).getShaderProg();
 	}
 	
 	/**
 	 * Get parameter of vertex shader program by name.
 	 * @param shader_name Name of vertex shader.
 	 * @param param_name Name of parameter.
 	 * @return Vertex shader parameter.
 	 */
 	public CGparameter getVertexShaderParam(String shader_name, String param_name){
 		return this.vpshaderprograms.get(shader_name).getParameter(param_name);
 	}
 	
 	/**
 	 * Get parameter of fragment shader program by name.
 	 * @param shader_name Name of fragment shader.
 	 * @param param_name Name of parameter.
 	 * @return Fragment shader parameter.
 	 */
 	public CGparameter getFragShaderParam(String shader_name, String param_name){
 		return this.fpshaderprograms.get(shader_name).getParameter(param_name);
 	}
 	
 	/**
 	 * Add parameter of vertex shader program by name.
 	 * @param shader_name Name of vertex shader.
 	 * @param param_name Name of parameter to be added.
 	 */
 	public void addVertexShaderParam(String shader_name, String param_name){
 		this.vpshaderprograms.get(shader_name).addParameter(param_name);
 	}
 	
 	/**
 	 * Add parameter of fragment shader program by name.
 	 * @param shader_name Name of fragment shader.
 	 * @param param_name Name of parameter to be added.
 	 */
 	public void addFragShaderParam(String shader_name, String param_name){
 		this.fpshaderprograms.get(shader_name).addParameter(param_name);
 	}
 
 	public void bindFP() throws IllegalArgumentException {
 		// disable profile
 		CgGL.cgGLDisableProfile(cgFragProfile);
		CgGL.cgGLUnbindProgram(cgFragProfile);
 		if (this.fragShaderEnabled == true) {
 			// bind the new one
			CgGL.cgGLBindProgram(this.fpshaderprograms.get(this.defaultFragmentProgName).getShaderProg());
 			// enable profile
 			CgGL.cgGLEnableProfile(cgFragProfile);
 		}
 	}
 
 	public void bindFP(String cgFPName) throws IllegalArgumentException {
 		if (this.fpshaderprograms.containsKey(cgFPName)) {
 			// disable profile
 			CgGL.cgGLDisableProfile(cgFragProfile);
			CgGL.cgGLUnbindProgram(cgFragProfile);
 			if (this.fragShaderEnabled == true) {
 				// enable profile
 				CgGL.cgGLEnableProfile(cgFragProfile);
 				// bind the new one
 				CgGL.cgGLBindProgram(this.fpshaderprograms.get(cgFPName).getShaderProg());
 			}
 		} else {
 			throw new IllegalArgumentException("The shader program " + cgFPName
 					+ " is unknown.");
 		}
 	}
 
 	/**
 	 * The standard constructor.
 	 */
 	private ShaderManager() {
 		// TODO: Handling of the default shader programs.
 		this.vpshaderprograms = new HashMap<String, Shader>();
 		this.fpshaderprograms = new HashMap<String, Shader>();
 
 		// create context
 		this.cgContext = CgGL.cgCreateContext();
 
 		cgVertexProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_VERTEX);
 		if (cgVertexProfile == CgGL.CG_PROFILE_UNKNOWN) {
 			System.err.println("Invalid vertex profile");
 			System.exit(1);
 		}
 		CgGL.cgGLSetOptimalOptions(cgVertexProfile);
 
 		cgFragProfile = CgGL.cgGLGetLatestProfile(CgGL.CG_GL_FRAGMENT);
 		if (cgFragProfile == CgGL.CG_PROFILE_UNKNOWN) {
 			System.err.println("Invalid fragment profile");
 			System.exit(1);
 		}
 		CgGL.cgGLSetOptimalOptions(cgFragProfile);
 
 		// load default shader programs
 		CGprogram loc_vertex_shader_prog = ShaderManager.loadShader(this.cgContext,
 				cgVertexProfile, ShaderManager.defaultInitialVPPath);
 		CGprogram loc_fragment_shader_prog = ShaderManager.loadShader(
 				this.cgContext, cgFragProfile,
 				ShaderManager.defaultInitialFPPath);		
 
 		//create default vertex shader
 		Shader loc_vertex_shader = new Shader(ShaderManager.defaultInitialVPName, loc_vertex_shader_prog);
 		
 		//create default fragment shader
 		Shader loc_frag_shader = new Shader(ShaderManager.defaultInitialFPName, loc_fragment_shader_prog);
 		
 		// put them in the hashtable
 		this.vpshaderprograms.put(ShaderManager.defaultInitialVPName, loc_vertex_shader);
 		this.fpshaderprograms.put(ShaderManager.defaultInitialFPName, loc_frag_shader);
 
 		// set currently loaded shader programs
 		cgVertexProgName = ShaderManager.defaultInitialVPName;
 		cgFragmentProgName = ShaderManager.defaultInitialFPName;
 
 		// set default shader programs.
 		defaultVertexProgName = cgVertexProgName;
 		defaultFragmentProgName = cgFragmentProgName;
 
 		// enable profile
 		CgGL.cgGLEnableProfile(cgVertexProfile);
 		CgGL.cgGLEnableProfile(cgFragProfile);
 
 		// bind the programs i.e. load them.
 		CgGL.cgGLBindProgram(loc_vertex_shader_prog);
 		CgGL.cgGLBindProgram(loc_fragment_shader_prog);
 	}
 
 	/**
 	 * Adds a vertex shader program.
 	 * 
 	 * @param name
 	 *            The name of the shader program.
 	 * @param shaderprogram
 	 *            The actual shader program.
 	 * @throws IllegalArgumentException
 	 *             Is thrown if the shader program was already loaded.
 	 */
 	public void addVertexShaderProgram(String name, CGprogram shaderprogram)
 			throws IllegalArgumentException {
 		if (!this.vpshaderprograms.containsKey(name)) {
 			//create a new vertex shader.
 			Shader loc_vertex_shader = new Shader(name, shaderprogram);
 			this.vpshaderprograms.put(name, loc_vertex_shader);
 		} else {
 			throw new IllegalArgumentException("The vertex shader program "
 					+ name + " was already loaded.");
 		}
 	}
 
 	/**
 	 * Adds a fragment shader program.
 	 * 
 	 * @param name
 	 *            The name of the shader program.
 	 * @param shaderprogram
 	 *            The actual shader program.
 	 * @throws IllegalArgumentException
 	 *             Is thrown if the shader program was already loaded.
 	 */
 	public void addFragmentShaderProgram(String name, CGprogram shaderprogram)
 			throws IllegalArgumentException {
 		if (!this.fpshaderprograms.containsKey(name)) {
 			//create a new fragment shader
 			Shader loc_frag_shader = new Shader(name, shaderprogram);
 			this.fpshaderprograms.put(name, loc_frag_shader);
 		} else {
 			throw new IllegalArgumentException("The fragment shader program "
 					+ name + " was already loaded.");
 		}
 	}
 
 	/**
 	 * Remove a vertex shader program from manager.
 	 * 
 	 * @param name
 	 *            The name of the shader program to be removed.
 	 * @throws IllegalArgumentException
 	 */
 	public void removeVertexShaderProgram(String name)
 			throws IllegalArgumentException {
 		if (this.vpshaderprograms.containsKey(name)) {
 			// check whether shader program is default shader
 			// if is default then throw error because is not allowed to remove
 			// default shader program from list.
 			if (this.cgVertexProgName.equals(name)) {
 				throw new IllegalArgumentException(
 						"Could not remove the default shader program.");
 			}
 			// check whether shader program is currently active.
 			if (this.cgVertexProgName.equals(name)) {
 				// unbind vertex shader
 				CgGL.cgGLUnbindProgram(cgVertexProfile);
 				// disable profile
 				CgGL.cgGLDisableProfile(cgVertexProfile);
 				// reset to default.
 				this.cgVertexProgName = this.defaultVertexProgName;
 				// bind the default
 				CgGL.cgGLBindProgram(this.vpshaderprograms
 						.get(this.cgVertexProgName).getShaderProg());
 				// enable profile
 				CgGL.cgGLEnableProfile(cgVertexProfile);
 			}
 			// now remove shader program from hashtable.
 			this.vpshaderprograms.remove(name);
 		} else {
 			throw new IllegalArgumentException("The shader program " + name
 					+ " is unknown.");
 		}
 	}
 
 	/**
 	 * Remove a fragment shader program from manager.
 	 * 
 	 * @param name
 	 *            The name of the shader program to be removed.
 	 * @throws IllegalArgumentException
 	 */
 	public void removeFragmentShaderProgram(String name)
 			throws IllegalArgumentException {
 		if (this.fpshaderprograms.containsKey(name)) {
 			// check whether shader program is default shader
 			// if is default then throw error because is not allowed to remove
 			// default shader program from list.
 			if (this.cgFragmentProgName.equals(name)) {
 				throw new IllegalArgumentException(
 						"Could not remove the default shader program.");
 			}
 			// check whether shader program is currently active.
 			if (this.cgFragmentProgName.equals(name)) {
 				// unbind vertex shader
 				CgGL.cgGLUnbindProgram(cgFragProfile);
 				// disable profile
 				CgGL.cgGLDisableProfile(cgFragProfile);
 				// reset to default.
 				this.cgFragmentProgName = this.defaultFragmentProgName;
 				// bind the default
 				CgGL.cgGLBindProgram(this.fpshaderprograms
 						.get(this.cgFragmentProgName).getShaderProg());
 				// enable profile
 				CgGL.cgGLEnableProfile(cgFragProfile);
 			}
 			// now remove shader program from hashtable.
 			this.fpshaderprograms.remove(name);
 		} else {
 			throw new IllegalArgumentException("The shader program " + name
 					+ " is unknown.");
 		}
 	}
 
 	/**
 	 * Loads a vertex shader from file a stores it in the shader manager accessible via the given name.
 	 * @param filename Vertex shader source code.
 	 * @param name Name of vertex shader.
 	 */
 	public void loadVertexShader(String filename, String name){
 		CGprogram loc_fp_prog = ShaderManager.loadShader(this.getCgContext(),this.getCgVertexProfile(), filename);
 		this.addVertexShaderProgram(name, loc_fp_prog);
 	}
 	
 	/**
 	 * Loads a fragment shader from file a stores it in the shader manager accessible via the given name.
 	 * @param filename Fragment shader source code.
 	 * @param name Name of fragment shader.
 	 */
 	public void loadFragShader(String filename, String name){
 		CGprogram loc_fp_prog = ShaderManager.loadShader(this.getCgContext(),this.getCgFragProfile(), filename);
 		this.addFragmentShaderProgram(name, loc_fp_prog);
 	}
 	
 	/**
 	 * Get instance of shadermanager.
 	 * 
 	 * @return Returns the instance of the shadermanager.
 	 */
 	public static ShaderManager getInstance() {
 		if (shadermanager == null) {
 			shadermanager = new ShaderManager();
 		}
 		return shadermanager;
 	}
 
 }
