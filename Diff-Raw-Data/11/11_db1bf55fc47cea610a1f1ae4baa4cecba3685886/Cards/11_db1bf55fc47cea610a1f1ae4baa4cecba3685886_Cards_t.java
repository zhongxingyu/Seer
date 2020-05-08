 package org.pileus.spades;
 
 import java.util.Map;
 import java.util.HashMap;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 import android.opengl.GLUtils;
 import android.opengl.Matrix;
 import android.view.MotionEvent;
 
 public class Cards extends GLSurfaceView implements GLSurfaceView.Renderer
 {
 	/* Shader data */
 	private final String vertSource
 		= "uniform   mat4 u_model;"
 		+ "uniform   mat4 u_view;"
 		+ "uniform   mat4 u_proj;"
 		+ "attribute vec4 a_position;"
 		+ "attribute vec2 a_mapping;"
 		+ "varying   vec2 v_mapping;"
 		+ "void main() {"
 		+ "  gl_Position = u_proj"
 		+ "              * u_view"
 		+ "              * u_model"
 		+ "              * a_position;"
 		+ "  v_mapping   = a_mapping;"
 		+ "}";
 
 	private final String fragSource
 		= "precision mediump   float;"
 		+ "uniform   sampler2D u_texture;"
 		+ "uniform   vec4      u_color;"
 		+ "varying   vec2      v_mapping;"
 		+ "void main() {"
 		+ "  gl_FragColor = texture2D("
 		+ "    u_texture, v_mapping);"
 		+ "}";
 
 	/* Drawing data */
 	private final float  faceCoords[] = {
 		-0.063f,  0.088f, 0.05f, // Standard poker size:
 		-0.063f, -0.088f, 0.05f, //   2.5in x 3.5in
 		 0.063f, -0.088f, 0.05f, //   63mm  x 88mm
 		 0.063f,  0.088f, 0.05f, //
 	};
 
 	private final float  backCoords[] = {
 		 0.063f,  0.088f, 0.05f, // Standard poker size:
 		 0.063f, -0.088f, 0.05f, //   2.5in x 3.5in
 		-0.063f, -0.088f, 0.05f, //   63mm  x 88mm
 		-0.063f,  0.088f, 0.05f, //
 	};
 
 	private final float  tableCoords[] = {
 		-0.75f,  0.75f, 0,
 		-0.75f, -0.75f, 0,
 		 0.75f, -0.75f, 0,
 		 0.75f,  0.75f, 0,
 	};
 
 	private final float  mapCoords[] = {
 		0.0f, 0.0f,
 		0.0f, 1.0f,
 		1.0f, 1.0f,
 		1.0f, 0.0f,
 	};
 
 	private final float  color[] = {
 		1, 0, 0, 1
 	};
 
 	/* Cards data */
 	private final String cards[] = {
 		"As", "Ks", "Qs", "Js", "10s", "9s", "8s", "7s", "6s", "5s", "4s", "3s", "2s",
 		"Ah", "Kh", "Qh", "Jh", "10h", "9h", "8h", "7h", "6h", "5h", "4h", "3h", "2h",
 		"Ac", "Kc", "Qc", "Jc", "10c", "9c", "8c", "7c", "6c", "5c", "4c", "3c", "2c",
 		"Ad", "Kd", "Qd", "Jd", "10d", "9d", "8d", "7d", "6d", "5d", "4d", "3d", "2d",
 	};
 
 	/* Private data */
 	private Resources    res;         // app resources
 	private int          program;     // opengl program
 
 	private float[]      model;       // model matrix
 	private float[]      view;        // view matrix
 	private float[]      proj;        // projection matrix
 
 	private FloatBuffer  faceBuf;     // vertex positions for front of card
 	private FloatBuffer  backBuf;     // vertex positions for back of card
 	private FloatBuffer  tableBuf;    // vertex positions for table
 	private FloatBuffer  mapBuf;      // texture mapping coord buffer
 
 	private int          modelHandle; // model matrix
 	private int          viewHandle;  // view matrix
 	private int          projHandle;  // projection matrix
 	private int          vertHandle;  // vertex positions
 	private int          mapHandle;   // texture mapping coords
 	private int          texHandle;   // texture data
 	private int          colorHandle; // color data
 
 	private int[]        face;        // card face textures
 	private int          red;         // red card back
 	private int          blue;        // blue card back
 	private int          table;       // table top texture
 
 	private boolean      drag;        // currently in drag event
 	private int          pick;        // currently picked card
 	private float        xpos;        // x drag position (0=left   - 1-right)
 	private float        ypos;        // y drag position (0=bottom - 1-top)
 	private float        ylim;        // y limit for a play
 
 	private Map<String,Integer> index; // card name to index map
 
 	/* Properties */
 	public Spades        game;        // the spades game
 	public String[]      hand;        // cards to display
 	public String[]      pile;        // played cards to display
 
 	/* GLSurfaceView Methods */
 	public Cards(Context context)
 	{
 		super(context);
 		Os.debug("Cards: create");
 
 		this.res   = context.getResources();
 
 		this.model = new float[4*4];
 		this.view  = new float[4*4];
 		this.proj  = new float[4*4];
 
 		this.face  = new int[52];
 
 		this.ylim  = 0.4f;
 
 		this.hand  = "As Ks Qs Js 10s 9s 8s 7s 6s 5s 4s 3s 2s".split(" ");
 		this.pile  = "Ah Ac Ad".split(" ");
 
 		this.index = new HashMap<String,Integer>(52);
 		for (int i = 0; i < 52; i++)
 			this.index.put(this.cards[i], i);
 
 		this.setEGLContextClientVersion(2);
 		this.setRenderer(this);
 		this.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
 	}
 
 	/* Renderer methods */
 	@Override
 	public void onSurfaceCreated(GL10 unused, EGLConfig config)
 	{
 		Os.debug("Cards: onSurfaceCreate");
 
 		/* Initialize shaders */
 		int vertShader = this.loadShader(GLES20.GL_VERTEX_SHADER,   vertSource);
 		int fragShader = this.loadShader(GLES20.GL_FRAGMENT_SHADER, fragSource);
 
 		/* Link shaders into an OpenGL program */
 		this.program = GLES20.glCreateProgram();
 		GLES20.glAttachShader(program, vertShader);
 		GLES20.glAttachShader(program, fragShader);
 		GLES20.glLinkProgram(program);
 
 		/* Get shaders attributes */
 		this.modelHandle = GLES20.glGetUniformLocation(program, "u_model");
 		this.viewHandle  = GLES20.glGetUniformLocation(program, "u_view");
 		this.projHandle  = GLES20.glGetUniformLocation(program, "u_proj");
 		this.vertHandle  = GLES20.glGetAttribLocation(program, "a_position");
 		this.mapHandle   = GLES20.glGetAttribLocation(program, "a_mapping");
 		this.texHandle   = GLES20.glGetUniformLocation(program, "u_texture");
 		this.colorHandle = GLES20.glGetUniformLocation(program, "u_color");
 
 		/* Create vertex array  */
 		this.faceBuf  = this.loadBuffer(this.faceCoords);
 		this.backBuf  = this.loadBuffer(this.backCoords);
 		this.tableBuf = this.loadBuffer(this.tableCoords);
 		this.mapBuf   = this.loadBuffer(this.mapCoords);
 
 		/* Load textures */
 		for (int i = 0; i < 52; i++) {
 			String name = "card_" + this.cards[i].toLowerCase();
 			this.face[i] = this.loadTexture(name);
 		}
 		this.red   = this.loadTexture("card_red");
 		this.blue  = this.loadTexture("card_blue");
 		this.table = this.loadTexture("table");
 
 		/* Debug */
 		Os.debug("Cards: onSurfaceCreate");
 	}
 
 	@Override
 	public void onDrawFrame(GL10 unused)
 	{
 		//Os.debug("Cards: onDrawFrame");
 
 		/* Turn on the program */
 		GLES20.glUseProgram(program);
 
 		/* Reset view */
 		GLES20.glClearColor(0, 0, 0, 1);
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
 		/* Setup projection matricies */
 		GLES20.glUniformMatrix4fv(this.viewHandle, 1, false, this.view, 0);
 		GLES20.glUniformMatrix4fv(this.projHandle, 1, false, this.proj, 0);
 
 		/* Setup buffers objects */
 		GLES20.glEnableVertexAttribArray(this.vertHandle);
 		GLES20.glEnableVertexAttribArray(this.mapHandle);
 
 		/* Setup texturing */
 		GLES20.glEnable(GLES20.GL_CULL_FACE);
 		GLES20.glEnable(GLES20.GL_BLEND);
 		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		GLES20.glUniform1i(this.texHandle, 0);
 
 		/* Draw objects */
 		this.drawTable();
 		this.drawPile();
 		this.drawHand();
 		this.drawPick();
 	}
 
 	@Override
 	public void onSurfaceChanged(GL10 unused, int width, int height)
 	{
 		Os.debug("Cards: onSurfaceChanged");
 
 		GLES20.glViewport(0, 0, width, height);
 
 		Matrix.setIdentityM(this.model, 0);
 		Matrix.setIdentityM(this.view, 0);
 		Matrix.setIdentityM(this.proj, 0);
 
 		// Setup camera
 		float xang = 0.5f;
 		float yang = xang * ((float)height / (float)width);
 
 		Matrix.frustumM(this.proj, 0,
 				-1E-6f * xang, // left
 				1E-6f  * xang, // right
 				-1E-6f * yang, // bottom
 				1E-6f  * yang, // top
 				1E-6f,         // near
 				10f);          // far
 
 		Matrix.rotateM(this.view, 0, 10f, 1, 0, 0);
 		Matrix.translateM(this.view, 0, 0, 0, -1.5f);
 		Matrix.rotateM(this.view, 0, -45f, 1, 0, 0);
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		boolean up = event.getActionMasked() == MotionEvent.ACTION_UP;
 
 		float x =    event.getX() / this.getWidth();
 		float y = 1-(event.getY() / this.getHeight());
 
 		this.ypos = y;
 		if (y < this.ylim) {
 			int num = this.hand.length;
 			this.xpos = x;
 			this.pick = (int)Math.floor((x*num));
 			if (this.pick <    0) this.pick = 0;
 			if (this.pick >= num) this.pick = num-1;
 		}
 		if (y < this.ylim && !this.drag) {
 			//Os.debug("Cards: onTouchEvent - starting drag");
 			this.drag = true;
 		}
 		if (this.drag) {
 			//Os.debug("Cards: onTouchEvent - move " + x + "," + y);
 			this.requestRender();
 		}
 		if (y >= this.ylim && this.drag && up) {
 			//Os.debug("Cards: onTouchEvent - playing card");
 			this.game.onPlay(this.hand[this.pick]);
 		}
 		if (up) {
 			//Os.debug("Cards: onTouchEvent - ending drag");
 			this.drag = false;
 		}
 		return true;
 	}
 
 	/* Private loading methods */
 	private int loadShader(int type, String code)
 	{
 		Os.debug("Cards: loadShader");
 
 		int shader = GLES20.glCreateShader(type);
 		GLES20.glShaderSource(shader, code);
 		GLES20.glCompileShader(shader);
 		return shader;
 	}
 
 	private int loadTexture(String name)
 	{
 		//Os.debug("Cards: loadTexture - " + name);
 
 		final int[] tex = new int[1];
 
 		/* Lookup the resource ID */
 		int id = 0;
 		try {
 			id = R.drawable.class.getField(name).getInt(null);
 		} catch(Exception e) {
 			Os.debug("Cards: lookup failed for '" + name + "'", e);
 			return 0;
 		}
 
 		/* Load the bitmap */
 		Bitmap bitmap = BitmapFactory.decodeResource(this.res, id);
 
 		/* Copy into OpenGL */
 		GLES20.glGenTextures(1, tex, 0);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
 		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
 		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
 		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
 
 		return tex[0];
 	}
 
 	private FloatBuffer loadBuffer(float[] data)
 	{
 		ByteBuffer bytes = ByteBuffer.allocateDirect(data.length * 4);
 		bytes.order(ByteOrder.nativeOrder());
 
 		FloatBuffer buf = bytes.asFloatBuffer();
 		buf.put(data);
 		buf.position(0);
 
 		return buf;
 	}
 
 	/* Private drawing methods */
 	private void drawTable()
 	{
 		/* Setup view */
 		Matrix.setIdentityM(this.model, 0);
 		GLES20.glUniformMatrix4fv(this.modelHandle, 1, false, this.model, 0);
 
 		/* Draw table */
 		GLES20.glVertexAttribPointer(this.vertHandle, 3, GLES20.GL_FLOAT, false, 3*4, this.tableBuf);
 		GLES20.glVertexAttribPointer(this.mapHandle,  2, GLES20.GL_FLOAT, false, 2*4, this.mapBuf);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.table);
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
 	}
 
 	private void drawPile()
 	{
 		/* Draw played cards */
 		for (int i = 0; i < 4; i++) {
 			if (i >= this.pile.length || this.pile[i] == null)
 				continue;
 
 			float ang = i * 90f;
 
 			Matrix.setIdentityM(this.model, 0);
 
 			Matrix.rotateM(this.model, 0, -ang, 0f, 0f, 1f);
 			Matrix.translateM(this.model, 0, -0.30f, 0f, 0f);
 			Matrix.rotateM(this.model, 0,  ang, 0f, 0f, 1f);
 			Matrix.scaleM(this.model, 0, 3f, 3f, 0f);
 
 			this.drawCard(this.pile[i]);
 		}
 	}
 
 	private void drawHand()
 	{
 		/* Draw hand */
 		int num = this.hand.length;
 		for (int i = 0; i < num; i++) {
 			if (this.drag && this.ypos >= this.ylim && i == this.pick)
 				continue;
 
 			Matrix.setIdentityM(this.model, 0);
 
 			Matrix.rotateM(this.model, 0, 45f, 1f, 0f, 0f);
 			Matrix.translateM(this.model, 0, 0f, -0.3f, 1.20f);
 
 			if (this.drag) {
 				float pct = (float)(i+0.5) / num;
 				float err = this.xpos - pct;
 				float y   = (float)this.ypos / this.ylim;
 				float lim = Math.min(Math.max(y,0),1);
 				float fcn = 0.1f
 					* (float)Math.exp(-10*num*Math.pow(y*err,2))
 					* (1f-(float)Math.pow(1-lim, 2));
 				Matrix.translateM(this.model, 0, 0, fcn, 0);
 			}
 
 			float left  = -20f + 20f*(1f/num);
 			float right =  54f - 54f*(1f/num);
 			float ang   = left + i*(right-left)/num;
 			Matrix.rotateM(this.model, 0, ang, 0f, 0f, -1f);
 			Matrix.translateM(this.model, 0, 0f, 0.15f, 0f);
 
 			this.drawCard(this.hand[i]);
 		}
 	}
 
 	private void drawPick()
 	{
 		/* Draw selected card */
 		if (this.drag && this.ypos >= this.ylim) {
 			Matrix.setIdentityM(this.model, 0);
 			Matrix.rotateM(this.model, 0, 45f, 1f, 0f, 0f);
 			Matrix.translateM(this.model, 0, 0f, 0f, 1.20f);
 			this.drawCard(this.hand[this.pick]);
 		}
 	}
 
 	private void drawCard(String name)
 	{
		if (!this.index.containsKey(name))
			return;
 		int idx   = this.index.get(name);
 		int front = this.face[idx];
 		int back  = this.red;
 
 		/* Set model matrix */
 		GLES20.glUniformMatrix4fv(this.modelHandle, 1, false, this.model, 0);
 
 		/* Draw front */
 		GLES20.glVertexAttribPointer(this.vertHandle, 3, GLES20.GL_FLOAT, false, 3*4, this.faceBuf);
 		GLES20.glVertexAttribPointer(this.mapHandle,  2, GLES20.GL_FLOAT, false, 2*4, this.mapBuf);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, front);
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
 
 		/* Draw back */
 		GLES20.glVertexAttribPointer(this.vertHandle, 3, GLES20.GL_FLOAT, false, 3*4, this.backBuf);
 		GLES20.glVertexAttribPointer(this.mapHandle,  2, GLES20.GL_FLOAT, false, 2*4, this.mapBuf);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, back);
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);
 	}
 }
