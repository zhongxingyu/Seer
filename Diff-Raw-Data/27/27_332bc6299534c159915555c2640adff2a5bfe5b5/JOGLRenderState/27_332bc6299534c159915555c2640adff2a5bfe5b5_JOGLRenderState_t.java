 package de.tum.in.cindy3dplugin.jogl.renderers;
 
 import javax.media.opengl.GL;
 
 import de.tum.in.cindy3dplugin.jogl.ModelViewerCamera;
 import de.tum.in.cindy3dplugin.jogl.RenderHints;
 
 
 /**
  * Properties and information that is needed for rendering or should be applied.
  * The following parameters are part of a render state.
  * 
 * <ol>
 * <li> cull mode, which indicates how to cull primitive faces.
 * <li> camera, which defines the current part of the scene to be rendered.
 * <li> render either opaque or transparent primitives.
 * <li> render hints, concerning the quality of the rendering output, that
  * 		should be fulfilled during rendering.
 * </ol>
  */
 public class JOGLRenderState {
 	/**
 	 * Cull mode.
 	 */
 	public enum CullMode {
 		/**
 		 * No culling is performed.
 		 */
 		CULL_NONE,
 		/**
 		 * All front faces are culled.
 		 */
 		CULL_FRONT,
 		/**
 		 * All back faces are culled.
 		 */
 		CULL_BACK,
 	}
 
 	/**
 	 * GL handle.
 	 */
 	public GL gl;
 	/**
 	 * Camera for this state.
 	 */
 	public ModelViewerCamera camera;
 	/**
 	 * Indicates if transparent or opaque primitives should get rendered.
 	 */
 	public boolean renderOpaque;
 	/**
 	 * Cull mode of the render state.
 	 */
 	public CullMode cullMode;
 	/**
 	 * Render hints that should be fulfilled in the current render mode.
 	 */
 	public RenderHints renderHints;
 
 	/**
 	 * Creates new render state with the given parameters.
 	 * 
 	 * @param gl
 	 *            GL handle
 	 * @param camera
 	 *            camera to be used
 	 * @param renderOpaque
 	 *            <code>true</code> if opaque primitives should be rendered,
 	 *            <code>false</code> if transparent objects should be rendered
 	 * @param cullMode
 	 *            cull mode to be used
 	 * @param renderHints
 	 *            render hints that should be applied
 	 */
 	public JOGLRenderState(GL gl, ModelViewerCamera camera,
 			boolean renderOpaque, CullMode cullMode, RenderHints renderHints) {
 		this.gl = gl;
 		this.camera = camera;
 		this.renderOpaque = renderOpaque;
 		this.cullMode = cullMode;
 		this.renderHints = renderHints;
 	}
 }
