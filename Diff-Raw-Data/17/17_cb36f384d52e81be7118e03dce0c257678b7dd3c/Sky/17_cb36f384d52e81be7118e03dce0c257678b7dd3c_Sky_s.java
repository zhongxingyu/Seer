 package com.chris.spacedragon;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector3;
 
 public class Sky {
 	public static Texture textureSky;
 
 	public static Mesh mesh;
 
 	public static ShaderProgram shaderSkyTexture;
 
 	public void create() {
 		mesh = ObjLoader.loadObj(Gdx.files.internal("data/models/sky.obj")
 				.read());
 
 		String vertexShader = "attribute vec4 a_position;    \n"
 				+ "attribute vec4 a_color;\n" + "attribute vec2 a_texCoord0;\n"
 				+ "uniform mat4 u_worldView;\n" + "varying vec4 v_color;"
 				+ "varying vec2 v_texCoords;"
 				+ "void main()                  \n"
 				+ "{                            \n"
 				+ "   v_color = vec4(1.0, 1.0, 1.0, 1); \n"
 				+ "   v_texCoords = a_texCoord0; \n"
 				+ "   gl_Position =  u_worldView * a_position;  \n"
 				+ "}                            \n";
 		String fragmentShader = "#ifdef GL_ES\n"
 				+ "precision mediump float;\n"
 				+ "#endif\n"
 				+ "varying vec4 v_color;\n"
 				+ "varying vec2 v_texCoords;\n"
 				+ "uniform sampler2D u_texture;\n"
 				+ "void main()                                  \n"
 				+ "{                                            \n"
				+ "gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n"
 				+ "}";
 		shaderSkyTexture = new ShaderProgram(vertexShader, fragmentShader);
 
 		textureSky = new Texture(Gdx.files.internal("data/textures/sky.png"));
 	}
 
 	public void render(ChaseCam camera) {
 		Gdx.gl.glDisable(GL10.GL_DEPTH_TEST);
 		Gdx.gl.glDepthMask(false);
 
 		Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		Gdx.gl.glEnable(GL10.GL_BLEND);
 
 		shaderSkyTexture.begin();
 		textureSky.bind();
 
 		// render body
 		shaderSkyTexture.setUniformMatrix("u_worldView", camera.getSkyboxMatrix());
		mesh.render(Game.shaderMain, GL20.GL_TRIANGLES);
 		shaderSkyTexture.end();
 		Gdx.gl.glDepthMask(true);
 
 	}
 }
