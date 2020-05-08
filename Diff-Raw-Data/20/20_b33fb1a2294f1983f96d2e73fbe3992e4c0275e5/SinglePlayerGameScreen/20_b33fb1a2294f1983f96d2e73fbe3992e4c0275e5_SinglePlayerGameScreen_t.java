 package de.redlion.rts;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g3d.loaders.ModelLoaderRegistry;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.Ray;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 
 import de.redlion.rts.render.RenderDebug;
 import de.redlion.rts.render.RenderMap;
 import de.redlion.rts.units.Soldier;
 
 public class SinglePlayerGameScreen extends DefaultScreen {
 
 	float startTime = 0;
 
 	SpriteBatch batch;
 	SpriteBatch fadeBatch;
 	Sprite blackFade;
 	
 	BitmapFont font;
 	
 	RenderMap renderMap;
 	RenderDebug renderDebug;
 
 	float fade = 1.0f;
 	boolean finished = false;
 
 	float delta;
 	
 	OrthoCamController camController;
 	DrawController  drawController;
 	KeyController keyController;
 	OrthographicCamera camera;
 	InputMultiplexer multiplexer;
 	
 	// GLES20
 	Matrix4 model = new Matrix4().idt();
 	Matrix4 normal = new Matrix4().idt();
 	Matrix4 tmp = new Matrix4().idt();
 	
 	ShapeRenderer r;
 	ShaderProgram flatShader;
 	StillModel sphere;
 	
 	public static boolean paused = false;
 	
 	public static HashMap<Vector2,Vector3> path;
 	public static Ray circleRay;
 	public static float circleRadius;
 
 	public SinglePlayerGameScreen(Game game) {
 		super(game);
 		
 		GameSession.getInstance().newSinglePlayerGame();
 		
 		//refresh references 
 		//TODO Observer Pattern for newGame
 		renderMap = new RenderMap();
 		renderDebug = new RenderDebug();
 		
 		sphere = ModelLoaderRegistry.loadStillModel(Gdx.files.internal("data/sphere.g3dt"));
 		
 		flatShader = new ShaderProgram(Gdx.files.internal(
 				"data/shaders/default.vert").readString(), Gdx.files
 				.internal("data/shaders/default.frag").readString());
 		if (!flatShader.isCompiled())
 			throw new GdxRuntimeException(
 					"Couldn't compile shadow map shader: "
 							+ flatShader.getLog());
 		
 //		Gdx.input.setInputProcessor(new SinglePlayerControls(player));
 
 		batch = new SpriteBatch();
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, 800, 480);
 		
 		blackFade = new Sprite(	new Texture(Gdx.files.internal("data/black.png")));
 		fadeBatch = new SpriteBatch();
 		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
 		
 		font = Resources.getInstance().font;
 		font.setScale(1);
 		
 		r = new ShapeRenderer();
 		r.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
 		path = new LinkedHashMap<Vector2,Vector3>();
 		
 		
 		initRender();
 	}
 
 	public void initRender() {
 		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
 		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	
 		initRender();
 		renderMap = new RenderMap();
 		
 		camController = new OrthoCamController(renderMap.cam);
 		keyController = new KeyController();
 		drawController = new DrawController(renderMap.cam);
 		multiplexer = new InputMultiplexer();
 		multiplexer.addProcessor(camController);
 		multiplexer.addProcessor(keyController);
 
 		Gdx.input.setInputProcessor(multiplexer);
 	}
 
 	@Override
 	public void show() {
 	}
 	
 	private float deltaCount = 0;	
 	@Override
 	public void render(float deltaTime) {
 		deltaCount += deltaTime;
 		if(deltaCount > 0.01) {
 			deltaCount = 0;
 			renderFrame(0.02f);
 		}
 		
 		if(paused) {
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(camController);
 			multiplexer.addProcessor(drawController);
 			multiplexer.addProcessor(keyController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
 		else {
 			path.clear();
 //			dollar.clear();
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(drawController);
 			multiplexer.addProcessor(camController);
 			multiplexer.addProcessor(keyController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
 		
 	}
 
 	public void renderFrame(float deltaTime) {
 
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 		delta = Math.min(0.1f, deltaTime);
 
 		startTime += delta;
 
 		if(!paused)
 			updateUnits();
 
 		collisionTest();
 		updateAI();
 		
 		renderMap.render();
 
 		if (Configuration.getInstance().debug) {
 			renderDebug.render();
 		}
 
 		// FadeInOut
 		if (!finished && fade > 0) {
 			fade = Math.max(fade - (delta), 0);
 			fadeBatch.begin();
 			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
 					blackFade.getColor().b, fade);
 			blackFade.draw(fadeBatch);
 			fadeBatch.end();
 		}
 
 		if (finished) {
 			fade = Math.min(fade + (delta), 1);
 			fadeBatch.begin();
 			blackFade.setColor(blackFade.getColor().r, blackFade.getColor().g,
 					blackFade.getColor().b, fade);
 			blackFade.draw(fadeBatch);
 			fadeBatch.end();
 			if (fade >= 1) {
 				Gdx.app.exit();
 			}
 		}
 		
 		if(paused) {
 			batch.begin();
 			font.draw(batch, "PAUSED", 700, 35);
 			batch.end();
 			
 			r.setColor(1, 0, 0, 1);
 			r.begin(ShapeType.Line);
 			Iterator<Vector2> it = path.keySet().iterator();
 			Vector2 temp3 = new Vector2(0, 0);
 			if(it.hasNext()) {
 				temp3 = it.next();
 			}
 			while(it.hasNext()) {
 				Vector2 temp1 = it.next();
 				
 				if(temp3.x != -1 && temp1.x != -1) {
 					r.line(temp3.x, temp3.y,temp1.x, temp1.y);
 					
 				}
 				if(it.hasNext()) {
 					
 					Vector2 temp2 = it.next();
 					if(temp2.x != -1 && temp1.x != -1) {
 						r.line(temp1.x, temp1.y,temp2.x, temp2.y);
 
 					}
 					temp3 = temp2;
 				}
 				
 			}
 			r.end();
 			
 
 			if(circleRay != null) {
 				Vector3 localIntersection = new Vector3();
 				if (Intersector.intersectRayTriangles(circleRay, renderMap.heightMap.map, localIntersection)) {
 				}
 				
 				flatShader.begin();
 				flatShader.setUniformMatrix("u_projView", renderMap.cam.combined);
 				flatShader.setUniformf("v_color", 1 , 0 ,0, 0.1f);
 				
 				tmp.idt();
 				model.idt();
 				
 				tmp.setToTranslation(localIntersection);
 				model.mul(tmp);
 	
 				tmp.setToScaling(circleRadius,circleRadius,circleRadius);
 				model.mul(tmp);
 	
 				
 				flatShader.setUniformMatrix("u_model", model);
 				
 				sphere.render(flatShader);
 				flatShader.end();
 				
 				Vector3 pos = GameSession.getInstance().playerSoldiers.get(0).position;
 //				if(circle.contains(new Vector2(pos.x,pos.y))) {
 //					Gdx.app.log("", "x:" + circle.x + " y:" + circle.y);
 //					Gdx.app.log("", "radius:" + circle.radius);
 //					
 //					circle = new Circle(-1,-1, 0);
 //				}
 			}
 		}
 	}
 	
 	private void updateUnits() {
 		
 		for(Soldier soldier:GameSession.getInstance().playerSoldiers) {
 			soldier.update(delta);
 		}
 		
 		for(Soldier soldier:GameSession.getInstance().enemySoldiers) {
 			soldier.update(delta);
 		}
 	}
 
 	private void updateAI() {		
 	}
 
 	private void collisionTest() {
 		for(Soldier playerSoldier:GameSession.getInstance().playerSoldiers) {
 			for(Soldier enemySoldier:GameSession.getInstance().enemySoldiers) {
 				if(playerSoldier.position.dst2(enemySoldier.position) < 0.1f) {
 					enemySoldier.hit();
 				}
 			}	
 		}
 		
 		for(Soldier enemySoldier:GameSession.getInstance().enemySoldiers) {
 			for(Soldier playerSoldier:GameSession.getInstance().playerSoldiers) {
 				if(enemySoldier.position.dst2(playerSoldier.position) < 0.1f) {
 					playerSoldier.hit();
 				}
 			}	
 		}
 		
 		
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 }
