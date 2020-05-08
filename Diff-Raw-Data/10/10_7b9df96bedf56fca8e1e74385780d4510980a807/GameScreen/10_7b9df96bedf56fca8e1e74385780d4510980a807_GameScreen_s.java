 package com.jumpandrun;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.Pixmap.Format;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.FrameBuffer;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.music.RhythmAudio;
 import com.music.RhythmValue;
 
 public class GameScreen extends DefaultScreen implements InputProcessor {
 	Map map;
 		
 	PerspectiveCamera cam;
 
 	SpriteBatch batch;
 	BitmapFont font;
 
 	public static RhythmAudio ra = new RhythmAudio();
 	public RhythmValue rv1;
 	public RhythmValue rv2;
 	
 	float startTime = 0;
 	float delta = 0;
 	boolean oldValueRv2 = false;
 	
 	//DEBUG values
 	float renderTimeBench = 0;
 	float physicTimeBench = 0;
 	float musicTimeBench = 0;
 	float startTimeBench = 0;
 	float endTimeBench = 0;
 	
 	// GLES20
 	Matrix4 model = new Matrix4().idt();
 	Matrix4 tmp = new Matrix4().idt();
 	private ShaderProgram transShader;
 	private ShaderProgram bloomShader;
 	Mesh blockModel;
 	Mesh playerModel;
 	Mesh targetModel;
 	Mesh quadModel;
 	Mesh wireCubeModel;
 	Mesh sphereModel;
 	Mesh sphereSliceModel;
 	FrameBuffer frameBuffer;
 	FrameBuffer frameBufferVert;
 	
 	float angleXBack = 0;
 	float angleYBack = 0;
 	float angleXFront = 0;
 	float angleYFront = 0;	
 	Vector3 tmpVector3 = new Vector3();
 	Vector2 tmpVector2 = new Vector2();
 	
 
 	public GameScreen(Game game) {
 		super(game);
 		
 		map = new Map();
 		
 		cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.position.set(0, 0,-5f);
 		cam.direction.set(0, 0, -1);
 		cam.up.set(0, 1, 0);
 		cam.near = 1f;
 		cam.far = 1000;
 		
 		Gdx.input.setInputProcessor(this);
 		batch = new SpriteBatch();
 		font = new BitmapFont();
 		
 		blockModel = Resources.getInstance().blockModel;
 		playerModel = Resources.getInstance().playerModel;
 		targetModel = Resources.getInstance().targetModel;
 		quadModel = Resources.getInstance().quadModel;
 		wireCubeModel = Resources.getInstance().wireCubeModel;
 		sphereModel = Resources.getInstance().sphereModel;
 		sphereSliceModel = Resources.getInstance().sphereSliceModel;
 		
 		transShader = Resources.getInstance().transShader;
 		bloomShader = Resources.getInstance().bloomShader;
 		
 		initRender();
 	}
 	
 	public void initRender() {
 		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		frameBuffer = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);		
 		frameBufferVert = new FrameBuffer(Format.RGB565, Resources.getInstance().m_i32TexSize, Resources.getInstance().m_i32TexSize, false);
 	}
 
 	@Override
 	public void show() {
 		
 		ra.loadMidi("./data/test.mid");
 		ra.play();
 		rv1 = new RhythmValue(RhythmValue.type.SINE, 20, ra);
 		rv2 = new RhythmValue(RhythmValue.type.BIT, 800, ra);
 	}
 
 
 	@Override
 	public void render(float delta) {
 		delta = Math.min(0.06f, Gdx.graphics.getDeltaTime());
 		startTime+=delta;
 		
 		angleXBack += MathUtils.sin(startTime) * delta * 10f;
 		angleYBack += MathUtils.cos(startTime) * delta * 5f;
 
 		angleXFront += MathUtils.sin(startTime) * delta * 10f;
 		angleYFront += MathUtils.cos(startTime) * delta * 5f;
 		
 		startTimeBench = System.nanoTime();		
 		
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		cam.position.set(GameInstance.getInstance().player.position.x, GameInstance.getInstance().player.position.y, 15);
 		cam.update();
 		
 		if (Resources.getInstance().bloomOnOff) {
 			frameBuffer.begin();
 			renderScene();
 			frameBuffer.end();
 
 			// PostProcessing
 			Gdx.gl.glDisable(GL20.GL_CULL_FACE);
 			Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 			Gdx.gl.glDisable(GL20.GL_BLEND);
 
 			frameBuffer.getColorBufferTexture().bind(0);
 
 			bloomShader.begin();
 			bloomShader.setUniformi("sTexture", 0);
 			bloomShader.setUniformf("bloomFactor", Helper.map((MathUtils.sin(startTime * 3f) * delta * 50f) + 0.5f, 0, 1, 0.50f, 0.55f));
 
 			frameBufferVert.begin();
 			bloomShader.setUniformf("TexelOffsetX", Resources.getInstance().m_fTexelOffset);
 			bloomShader.setUniformf("TexelOffsetY", 0.0f);
 			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
 			frameBufferVert.end();
 
 			frameBufferVert.getColorBufferTexture().bind(0);
 
 			frameBuffer.begin();
 			bloomShader.setUniformf("TexelOffsetX", 0.0f);
 			bloomShader.setUniformf("TexelOffsetY", Resources.getInstance().m_fTexelOffset);
 			quadModel.render(bloomShader, GL20.GL_TRIANGLE_STRIP);
 			frameBuffer.end();
 
 			bloomShader.end();
 		}
 
 		// render scene again
 		renderScene();
 		
 		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
 		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glDisable(GL20.GL_BLEND);
 				
 		if(Resources.getInstance().bloomOnOff) {
 			batch.enableBlending();
 			batch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
 			batch.begin();
 			batch.draw(frameBuffer.getColorBufferTexture(), 0, 0,800,480,0,0,frameBuffer.getWidth(),frameBuffer.getHeight(),false,true);
 			batch.end();
 		}
 		
 		endTimeBench = (System.nanoTime() - startTimeBench) / 1000000000.0f;
 		renderTimeBench = endTimeBench;
 		 
 		startTimeBench = System.nanoTime();
 		GameInstance.getInstance().physicStuff();	
 		endTimeBench = (System.nanoTime() - startTimeBench) / 1000000000.0f;
 		physicTimeBench = endTimeBench;
 
 
 //		if (ra.getPlayedChannels()[6]==true) {
 //			Player.JUMP_VELOCITY = 15;
 //		}
 		if (ra.getPlayedChannels()[5]==true) {
 			GameInstance.getInstance().activateJumpBlocks();
 		}
 		
 //		renderer.deltaMusic = rv1.getValue();
 //		map.update(delta);
 //		Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
 //		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 //		renderer.render(delta);
 //		
 //		if (map.bob.state == Player.JUMP) {
 //			if (!jumping) {
 //				jumping = true;
 //			}
 //		} else {
 //			jumping = false;
 //		}
 
 
 		batch.begin();
 		font.draw(batch, "fps: " + Gdx.graphics.getFramesPerSecond(), 10, 30);
 		font.draw(batch, "box2d: " + physicTimeBench, 10, 50);
 		font.draw(batch, "render: " + renderTimeBench, 10, 70);
 		batch.end();		
 	}	
 
 	private void renderScene() {
 		Gdx.gl.glEnable(GL20.GL_CULL_FACE);
 		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 		
 		Gdx.gl20.glEnable(GL20.GL_BLEND);
 		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
 				
 		transShader.begin();
 		transShader.setUniformMatrix("VPMatrix", cam.combined);
 		
 		//render boxes
 		for (int i =0; i<GameInstance.getInstance().blocks.size ; ++i) {
 			Block block = GameInstance.getInstance().blocks.get(i);
 			if(cam.frustum.pointInFrustum(tmpVector3.set(block.position.x, block.position.y, 0))) {
 				model.idt();
 				
 
 			
 				if(block instanceof JumpBlock) {
 					//TODO quick hack
 					JumpBlock jumbBlock = (JumpBlock)block;
 					jumbBlock.update();
 					
 					tmp.setToTranslation(jumbBlock.position.x, jumbBlock.position.y, 0);
 					model.mul(tmp);
 		
 					tmp.setToScaling(0.95f, 0.95f, 0.95f);
 					model.mul(tmp);
 							
 					transShader.setUniformMatrix("MMatrix", model);
 					
 					transShader.setUniformf("a_color", Resources.getInstance().jumpBlockColor[0], Resources.getInstance().jumpBlockColor[1], Resources.getInstance().jumpBlockColor[2], Resources.getInstance().jumpBlockColor[3] + jumbBlock.jumpAnim);
 					blockModel.render(transShader, GL20.GL_TRIANGLES);
 		
 					transShader.setUniformf("a_color",Resources.getInstance().jumpBlockEdgeColor[0], Resources.getInstance().jumpBlockEdgeColor[1],Resources.getInstance().jumpBlockEdgeColor[2], Resources.getInstance().jumpBlockEdgeColor[3] + jumbBlock.jumpAnim);
 					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);			
 				} else {	
 					
 					tmp.setToTranslation(block.position.x, block.position.y, 0);
 					model.mul(tmp);
 		
 					tmp.setToScaling(0.95f, 0.95f, 0.95f);
 					model.mul(tmp);
 							
 					transShader.setUniformMatrix("MMatrix", model);
 					
 					transShader.setUniformf("a_color", Resources.getInstance().blockColor[0], Resources.getInstance().blockColor[1], Resources.getInstance().blockColor[2], Resources.getInstance().blockColor[3]);
 					blockModel.render(transShader, GL20.GL_TRIANGLES);
 		
 					transShader.setUniformf("a_color",Resources.getInstance().blockEdgeColor[0], Resources.getInstance().blockEdgeColor[1],Resources.getInstance().blockEdgeColor[2], Resources.getInstance().blockEdgeColor[3]);
 					wireCubeModel.render(transShader, GL20.GL_LINE_STRIP);
 				}
 			}
 		}
 		
 		//render player
 		{
 			tmp.idt();
 			model.idt();
 			
 			tmp.setToScaling(1f, 1f, 1f);
 			model.mul(tmp);
 			
 			tmp.setToTranslation(GameInstance.getInstance().player.position.x, GameInstance.getInstance().player.position.y-0.8f, 0);
 			model.mul(tmp);
 			
 			tmp.setToRotation(Vector3.X, angleXBack);
 			model.mul(tmp);
 			tmp.setToRotation(Vector3.Y, angleYBack);
 			model.mul(tmp);
 
 			tmp.setToScaling(0.5f, 0.5f, 0.5f);
 			model.mul(tmp);
 	
 			transShader.setUniformMatrix("MMatrix", model);
 			transShader.setUniformf("a_color",Resources.getInstance().playerColor[0], Resources.getInstance().playerColor[1], Resources.getInstance().playerColor[2], Resources.getInstance().playerColor[3]);
 			playerModel.render(transShader, GL20.GL_TRIANGLES);
 			
 			tmp.setToScaling(2.0f, 2.0f, 2.0f);
 			model.mul(tmp);
 
 			//render hull			
 			transShader.setUniformMatrix("MMatrix", model);
 			transShader.setUniformf("a_color",Resources.getInstance().playerEdgeColor[0], Resources.getInstance().playerEdgeColor[1], Resources.getInstance().playerEdgeColor[2], Resources.getInstance().playerEdgeColor[3]);
 			playerModel.render(transShader, GL20.GL_LINE_STRIP);
 		}
 		transShader.end();
 	}
 	
 	@Override
 	public boolean keyDown(int keycode) {
 		if(keycode == Keys.W) GameInstance.getInstance().player.jump = true;
 		
 		if (keycode == Keys.ESCAPE) {
 			game.setScreen(new MainMenu(game));
 		}
 		
 		if (keycode == Keys.R) {
 //			for(Body box:boxes) {
 //				box.setTransform(box.getTransform().getPosition().x+delta,box.getTransform().getPosition().y+delta*(MathUtils.sin(startTime)*delta*500f),0);
 //			}
 		}
 		
 		if (keycode == Input.Keys.F) {
 			if(Gdx.app.getType() == ApplicationType.Desktop) {
 				if(!org.lwjgl.opengl.Display.isFullscreen()) {
 					Gdx.graphics.setDisplayMode(Gdx.graphics.getDesktopDisplayMode().width, Gdx.graphics.getDesktopDisplayMode().height, true);		
 				} else {
 					Gdx.graphics.setDisplayMode(800,480, false);		
 				}
 			}
 		}
 		return false;
 	}
  
 	@Override
 	public boolean keyUp(int keycode) {
 		if(keycode == Keys.W) GameInstance.getInstance().player.jump = false;
 		return false;
 	}
  
 	Vector2 last = null;
 	Vector3 point = new Vector3();
  
 	@Override
 	public boolean touchDown(int x, int y, int pointerId, int button) {
 		return false;
 	}
 
 	@Override
 	public void hide() {
 		System.out.println("dispose game screen");
 		ra.stop();
 	}
 
 	public float MidiToFrequenc(float note) {
 		return (float) (Math.pow(2, (note - 9) / 12.0) * 440);
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int arg0, int arg1, int arg2) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
 
 
