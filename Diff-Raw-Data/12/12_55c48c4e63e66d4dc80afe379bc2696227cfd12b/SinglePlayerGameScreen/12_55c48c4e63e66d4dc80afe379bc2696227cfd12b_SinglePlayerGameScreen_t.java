 package de.redlion.civilwar;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputMultiplexer;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.input.GestureDetector;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.utils.Array;
 import de.redlion.civilwar.controls.DrawController;
 import de.redlion.civilwar.controls.GestureController;
 import de.redlion.civilwar.controls.KeyController;
 import de.redlion.civilwar.controls.OrthoCamController;
 import de.redlion.civilwar.render.RenderMap;
 import de.redlion.civilwar.shader.DiffuseShader;
 import de.redlion.civilwar.units.PlayerSoldier;
 import de.redlion.civilwar.units.Soldier;
 
 public class SinglePlayerGameScreen extends DefaultScreen {
 
 	float startTime = 0;
 
 	SpriteBatch batch;
 	SpriteBatch fadeBatch;
 	Sprite blackFade;
 	
 	BitmapFont font;
 	
 	public static RenderMap renderMap;
 	ShaderProgram diff;
 
 	float fade = 1.0f;
 	boolean finished = false;
 
 	float delta;
 	
 	OrthoCamController camController;
 	public DrawController drawController;
 	KeyController keyController;
 	GestureController gestureController;
 	GestureDetector gestureDetector;
 	InputMultiplexer multiplexer;
 	
 	// GLES20
 	Matrix4 model = new Matrix4().idt();
 	Matrix4 normal = new Matrix4().idt();
 	Matrix4 tmp = new Matrix4().idt();
 	
 	float edgeScrollingSpeed;
 	
 	Sprite arrowhead;
 	
 	ShapeRenderer r;
 	
 	public static boolean paused = false;
 
 	public static HashMap<Polygon, ArrayList<Vector3>> paths;  //maps projected polygons to their associated paths
 	public static HashMap<Polygon, ArrayList<PlayerSoldier>> circles; //maps projected polygons to what soldiers they encompass
 	public static HashMap<Polygon, ArrayList<Vector2>> doodles; //maps polygons to what circle has been drawn on screen
 	public static HashMap<Polygon, ArrayList<Vector2>> pathDoodles; //maps polygons to what path has been drawn on screen
 	public static HashMap<Polygon, ArrayList<Vector2>> generatedDoodles; //maps polygons to doodles that have been generated from polygons
 	public static HashMap<Polygon, ArrayList<Vector2>> generatedPathDoodles; //maps polygons to their generated doodlepaths
 	public static HashMap<Polygon, ArrayList<Vector2>> triangleStrips; //maps polygons (circles) to triangle strips
 	public static HashMap<Polygon, ArrayList<Vector2>> pathTriangleStrips; //maps polygons (paths) to triangle strips
 	public static HashMap<Polygon, ArrayList<Vector2>> generatedTriangleStrips; //maps polygons to triangle strips that have been generated from polygons
 	public static HashMap<Polygon, ArrayList<Vector2>> generatedPathTriangleStrips; //maps polygons to triangle strips that have been generated from their paths
 	public static ArrayList<Polygon> circleHasPath;
 	public static ArrayList<Vector2> currentDoodle; //what is currently being drawn - simplified version
 	public static ArrayList<Vector2> currentTriStrip; //tristrip of what is being drawn
 	
 //	public static Ray circleRay;
 
 	public SinglePlayerGameScreen(Game game) {
 		super(game);
 		
 		GameSession.getInstance().newSinglePlayerGame();
 		
 		//refresh references 
 		//TODO Observer Pattern for newGame
 		renderMap = new RenderMap();
 		
 		diff = new ShaderProgram(DiffuseShader.mVertexShader, DiffuseShader.mFragmentShader);
 		
 //		Gdx.input.setInputProcessor(new SinglePlayerControls(player));
 
 		batch = new SpriteBatch();
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		blackFade = new Sprite(	new Texture(Gdx.files.internal("data/black.png")));
 		fadeBatch = new SpriteBatch();
 		fadeBatch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
 		
 		font = Resources.getInstance().font;
 		font.setScale(1);
 		
 		r = new ShapeRenderer();
 		r.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
 		
 		edgeScrollingSpeed = 1.0f;
 		
 		circles = new  LinkedHashMap<Polygon, ArrayList<PlayerSoldier>>();
 		paths = new LinkedHashMap<Polygon, ArrayList<Vector3>>();
 		doodles = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		pathDoodles = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		generatedDoodles = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		generatedPathDoodles = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		triangleStrips = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		pathTriangleStrips = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		generatedTriangleStrips = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		generatedPathTriangleStrips = new LinkedHashMap<Polygon, ArrayList<Vector2>>();
 		circleHasPath = new ArrayList<Polygon>();
 		currentDoodle = new ArrayList<Vector2>();
 		currentTriStrip = new ArrayList<Vector2>();
 
 		arrowhead = Resources.getInstance().arrowhead;
 		
 		initRender();
 	}
 
 	public void initRender() {
 		Gdx.graphics.getGL20().glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 	
 		Vector3 camPos = renderMap.cam.position;
 		Vector3 camUp = renderMap.cam.up;
 		Vector3 camDir = renderMap.cam.direction;
 		
 		initRender();
 		renderMap = new RenderMap(camPos,camDir,camUp);
 
 		r.setProjectionMatrix(new Matrix4().setToOrtho2D(0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight()));
 		
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		
 		camController = new OrthoCamController(renderMap.cam);
 		keyController = new KeyController(this);
 		drawController = new DrawController(renderMap.cam);
 		gestureController = new GestureController();
 		gestureDetector = new GestureDetector(drawController);
 		
 		if(paused) {
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(camController);
 			
 //			generateDoodles();
 			
 			multiplexer.addProcessor(drawController);
 			multiplexer.addProcessor(gestureDetector);
 			multiplexer.addProcessor(keyController);
 			
 			Gdx.input.setInputProcessor(multiplexer);
 		} else {
 			doodles.clear();
 			pathDoodles.clear();
 			generatedDoodles.clear();
 			generatedPathDoodles.clear();
 			triangleStrips.clear();
 			pathTriangleStrips.clear();
 			generatedTriangleStrips.clear();
 			generatedPathTriangleStrips.clear();
 			currentDoodle.clear();
 			currentTriStrip.clear();
 			drawController.subCircleHelper.clear();
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(drawController);
 			multiplexer.removeProcessor(gestureDetector);
 			multiplexer.addProcessor(camController);
 			multiplexer.addProcessor(keyController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
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
 			multiplexer.addProcessor(gestureDetector);
 			multiplexer.addProcessor(keyController);
 			
 //			generateDoodles();
 			
 			Gdx.input.setInputProcessor(multiplexer);
 		} else {
 			doodles.clear();
 			pathDoodles.clear();
 			generatedDoodles.clear();
 			generatedPathDoodles.clear();
 			triangleStrips.clear();
 			pathTriangleStrips.clear();
 			generatedTriangleStrips.clear();
 			generatedPathTriangleStrips.clear();
 			currentDoodle.clear();
 			currentTriStrip.clear();
 			drawController.subCircleHelper.clear();
 			multiplexer = new InputMultiplexer();
 			multiplexer.removeProcessor(drawController);
 			multiplexer.removeProcessor(gestureDetector);
 			multiplexer.addProcessor(camController);
 			multiplexer.addProcessor(keyController);
 			Gdx.input.setInputProcessor(multiplexer);
 		}
 		
 	}
 
 	public void renderFrame(float deltaTime) {
 
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
 		delta = Math.min(0.1f, deltaTime);
 
 		startTime += delta;
 
 		if(!paused) {
 			updateUnits();
 			collisionTest();
 			updateAI();
 			updatePolygons();
 			updatePaths();
 		}
 		
 		renderMap.render();
 
 		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
 		Gdx.gl.glDisable(GL20.GL_CULL_FACE);
 		
 
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
 		
 		//draw waypoints for debugging
 //		for(ArrayList<Vector3> pathPoints : paths.values()) {
 //			
 //			if(!pathPoints.isEmpty()) {
 //				
 //				for(Vector3 pPoint : pathPoints) {
 //
 //					StillModelNode node = new StillModelNode();
 //					node.matrix.translate(pPoint);
 //					node.matrix.scl(0.05f);
 //					RenderMap.protoRenderer.draw(sphere, node);
 //					
 //				}
 //				
 //			}
 //			
 //		}
 		
 //		for (ArrayList<Vector3> pathPoints : paths.values()) {
 //
 //			if (!pathPoints.isEmpty()) {
 //
 //				StillModelNode node = new StillModelNode();
 //				node.matrix.translate(pathPoints.get(pathPoints.size() -1));
 //				node.matrix.scl(0.05f);
 //				RenderMap.protoRenderer.draw(sphere, node);
 //
 //			}
 //
 //		}
 		
 	
         
         //draw polygons for debugging
 		diff.begin();
         for(Polygon pol : circles.keySet()) {
         		
                 float[] vertices = pol.getTransformedVertices();
                 
                 if(vertices.length > 0) {
                         
                         float[] vertices3D = new float[(int) Math.ceil(vertices.length + vertices.length/3)];
                         
                         while(vertices3D.length%3 != 0) {
                                 vertices3D = new float[1+vertices3D.length];
                         }
                         
                         Mesh polygonalMesh = new Mesh(true, vertices3D.length, vertices3D.length / 3, VertexAttribute.Position());
                         
                         int l = 0;
                         for(int k=0;k<vertices3D.length;k++) {
                                 
                                 if(k%3 == 1)
                                         vertices3D[k] = 0;
                                 else {
                                         vertices3D[k] = vertices[l];
                                         l++;
                                 }
                                 
                         }
                         
                         polygonalMesh.setVertices(vertices3D);
                         short[] indices =  new short[vertices3D.length/3];
                         
                         for(short j=0;j<indices.length;j++) {
                                 indices[j] = j;
                         }
                         
                         polygonalMesh.setIndices(indices);
                         
                         for(int i=0; i<vertices.length;i+=2) {
                                 
                                 diff.setUniformMatrix("VPMatrix", renderMap.cam.combined);
                                 diff.setUniformMatrix("MMatrix", model);
                                 diff.setUniformi("uSampler", 0);
                                 polygonalMesh.render(diff, GL20.GL_LINE_LOOP);
                                 
                         }
                         
                 }
         }
         diff.end();
         
 		
 		
 		if(paused) {
 			
 //			r.setProjectionMatrix(renderMap.cam.combined);
 			batch.begin();
 			font.draw(batch, "PAUSED", 700, 35);
 			batch.end();
 			
 			for(Polygon pol : doodles.keySet()) {
 				
 				ArrayList<Vector2> doodle = doodles.get(pol);
 				
 				ArrayList<Vector2> triangleStrip = triangleStrips.get(pol);
 				
 				if(triangleStrip != null && !triangleStrip.isEmpty()) {					
 					
 					r.setColor(1, 0, 0, 1);
 					r.begin(ShapeType.Filled);
 					
 					Vector2 p0 = triangleStrip.get(0);
 					Vector2 p1 = triangleStrip.get(1);
 
 					for(Vector2 p2 : triangleStrip) {
 
 						r.triangle(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
 						p0 = p1;
 						p1 = p2;
 						
 					}
 					r.end();
 					
 				}
 				
 				
 			}
 			
 			for(Polygon pol : pathDoodles.keySet()) {
 				
 				ArrayList<Vector2> triangleStrip = pathTriangleStrips.get(pol);
 				
 				if(triangleStrip != null && !triangleStrip.isEmpty()) {					
 					
 					r.setColor(1, 0, 0, 1);
 					r.begin(ShapeType.Filled);
 					
 					Vector2 p0 = triangleStrip.get(0);
 					Vector2 p1 = triangleStrip.get(1);
 
 					for(Vector2 p2 : triangleStrip) {
 
 						r.triangle(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
 						p0 = p1;
 						p1 = p2;
 						
 					}
 					r.end();
 				}
 				
 //				if(paths.containsKey(pol)) {
 					
 					ArrayList<Vector2> doodle = pathDoodles.get(pol);
 					
 	//				arrowhead.setPosition(doodle.get(doodle.size()-3).x - arrowhead.getOriginX(),doodle.get(doodle.size()-1).y - arrowhead.getOriginY());
 	
 					Vector2 a = doodle.get(doodle.size()-1).cpy();
 					Vector2 b = doodle.get(doodle.size()-2).cpy();
 					
 					Vector2 c = a.cpy().sub(b);
 					
 					Vector2 d = new Vector2(0, 1);
 					
 	//				r.setColor(1, 1, 1, 1);
 	//				r.begin(ShapeType.Line);
 	//				r.line(a.x, a.y, c.x + a.x, c.y + a.y);
 	//				r.line(a.x, a.y, d.x + a.x, d.y + a.y + 100);
 	//				r.end();
 					
 					float dot = c.cpy().dot(d);
 					float angle = dot / (d.len() * c.len());
 					
 					angle = (float) Math.acos(angle);
 					angle = (float) Math.toDegrees(angle);
 					
 					if(a.x < b.x)
 						angle *= -1;
 					
 					
 	//				Gdx.app.log("angle", angle + "");
 					
 					//correction due to sprite?
 					angle += 60;
 					
 					arrowhead.setPosition(a.x, a.y);
 					arrowhead.setRotation(-angle);
 					arrowhead.translate(-arrowhead.getOriginX(), -arrowhead.getOriginY());
 					batch.begin();
 					arrowhead.draw(batch);
 					batch.end();
 //				}
 			}
 			//draw generated doodles
 			for(Polygon pol : generatedDoodles.keySet()) {
 				
 				ArrayList<Vector2> doodle = generatedDoodles.get(pol);
 				
 				ArrayList<Vector2> triangleStrip = generatedTriangleStrips.get(pol);
 				
 				if(triangleStrip != null && !triangleStrip.isEmpty()) {					
 					
 					r.setColor(1, 0, 0, 1);
 					r.begin(ShapeType.Filled);
 					
 					Vector2 p0 = triangleStrip.get(0);
 					Vector2 p1 = triangleStrip.get(1);
 
 					for(Vector2 p2 : triangleStrip) {
 
 						r.triangle(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
 						p0 = p1;
 						p1 = p2;
 						
 					}
 					r.end();
 					
 				}
 				
 				
 			}
 			
 			//draw generated path doodles
 			for(Polygon pol : generatedPathDoodles.keySet()) {
 				
 				ArrayList<Vector2> doodle = generatedPathDoodles.get(pol);
 				
 				ArrayList<Vector2> triangleStrip = generatedPathTriangleStrips.get(pol);
 				
 				if(triangleStrip != null && !triangleStrip.isEmpty()) {					
 					
 					r.setColor(1, 0, 0, 1);
 					r.begin(ShapeType.Filled);
 					
 					Vector2 p0 = triangleStrip.get(0);
 					Vector2 p1 = triangleStrip.get(1);
 
 					for(Vector2 p2 : triangleStrip) {
 
 						r.triangle(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
 						p0 = p1;
 						p1 = p2;
 						
 					}
 					r.end();
 					
 					if(paths.containsKey(pol)) {
 					
 						Vector2 a = doodle.get(doodle.size()-1).cpy();
 						Vector2 b = doodle.get(doodle.size()-2).cpy();
 						
 						Vector2 c = a.cpy().sub(b);
 						
 						Vector2 d = new Vector2(0, 1);
 						
 	//					r.setColor(1, 1, 1, 1);
 	//					r.begin(ShapeType.Line);
 	//					r.line(a.x, a.y, c.x + a.x, c.y + a.y);
 	//					r.line(a.x, a.y, d.x + a.x, d.y + a.y + 100);
 	//					r.end();
 						
 						float dot = c.cpy().dot(d);
 						float angle = dot / (d.len() * c.len());
 						
 						angle = (float) Math.acos(angle);
 						angle = (float) Math.toDegrees(angle);
 						
 						if(a.x < b.x)
 							angle *= -1;
 						
 						
 	//					Gdx.app.log("angle", angle + "");
 						
 						//correction due to sprite?
 						angle += 60;
 						
 						arrowhead.setPosition(a.x, a.y);
 						arrowhead.setRotation(-angle);
 						arrowhead.translate(-arrowhead.getOriginX(), -arrowhead.getOriginY());
 						batch.begin();
 						arrowhead.draw(batch);
 						batch.end();
 					}
 					
 				}
 				
 				
 			}
 			
 			
 			// renders currentdoodle
 			if(!currentDoodle.isEmpty()) {
 //				r.setColor(1, 0, 0, 1);
 //				r.begin(ShapeType.FilledTriangle);
 //				
 //				Vector2 v0 = currentDoodle.get(0);
 //				
 //				for(Vector2 v1 : currentDoodle) {
 //					
 //					r.line(v0.x, v0.y, v1.x, v1.y);
 //					v0 = v1;
 //					
 //				}
 //				
 //				r.end();
 				
 				Vector2 v0 = currentDoodle.get(currentDoodle.size()-1);
 				
 				if(currentTriStrip.size() >= 2) {
 					r.setColor(1, 0, 0, 1);
 					r.begin(ShapeType.Filled);
 					
 					Vector2 p0 = currentTriStrip.get(0);
 					Vector2 p1 = currentTriStrip.get(1);
 					
 					for(Vector2 p2 : currentTriStrip) {
 						
 						r.triangle(p0.x, p0.y, p1.x, p1.y, p2.x, p2.y);
 						p0 = p1;
 						p1 = p2;
 						
 					}
 					
 					r.end();
 				}
 				
 				
 				/* edge scrolling */
 
 				if(edgeScrollingSpeed < 25)
 					edgeScrollingSpeed += Gdx.graphics.getDeltaTime() * Constants.EDGE_SCROLL_SPEED;
 				
 				if(v0.x >= 50 && v0.x <= Gdx.graphics.getWidth() - 50 && v0.y >= 50 && v0.y <= Gdx.graphics.getHeight() - 50)					
 					edgeScrollingSpeed = 1.0f;
 				else {
 					Vector3 temp = new Vector3();
 					if(v0.x < Constants.EDGE_DISTANCE)
 						temp.set(0,0,1);
 					else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 						temp.set(-1,0,0);
 					else if(v0.y < Constants.EDGE_DISTANCE)
 						temp.set(1,0,0);
 					else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 						temp.set(0,0,-1);
 					temp.mul(0.01f * Constants.MOVESPEED);
 					temp.mul(edgeScrollingSpeed);
 					Quaternion rotation = new Quaternion();
 					drawController.camera.combined.getRotation(rotation);
 					rotation.transform(temp);
 					drawController.camera.translate(temp.mul(-1));
 					drawController.camera.update();
 				
 					for(ArrayList<Vector2> doodle : doodles.values()) {
 						
 						
 						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 						for(Vector2 v : doodle) {
 							
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 									
 							newDoodle.add(v);
 							
 						}
 						
 						doodle.clear();
 						doodle.addAll(newDoodle);
 						
 					}
 					
 					for(ArrayList<Vector2> doodle : pathDoodles.values()) {
 						
 						
 						ArrayList<Vector2> newDoodle = new ArrayList<Vector2>();
 						for(Vector2 v : doodle) {
 							
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 									
 							newDoodle.add(v);
 							
 						}
 						
 						doodle.clear();
 						doodle.addAll(newDoodle);
 						
 					}
 					
 					for(ArrayList<Vector2> triStrip : triangleStrips.values()) {
 						
 						
 						ArrayList<Vector2> newStrip = new ArrayList<Vector2>();
 						for(Vector2 v : triStrip) {
 							
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 									
 							newStrip.add(v);
 							
 						}
 						
 						triStrip.clear();
 						triStrip.addAll(newStrip);
 						
 					}
 					
 					for(ArrayList<Vector2> triStrip : pathTriangleStrips.values()) {
 						
 						
 						ArrayList<Vector2> newStrip = new ArrayList<Vector2>();
 						for(Vector2 v : triStrip) {
 							
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 									
 							newStrip.add(v);
 							
 						}
 						
 						triStrip.clear();
 						triStrip.addAll(newStrip);
 						
 					}
 					
 					if(!currentDoodle.isEmpty()) {
 						ArrayList<Vector2> newCurrentDoodle = new ArrayList<Vector2>();
 						for(int i = 0; i<currentDoodle.size() -1; i++) {
 							
 							Vector2 v = currentDoodle.get(i);
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 							
 							newCurrentDoodle.add(v);
 							
 						}
 						
 					
 						newCurrentDoodle.add(currentDoodle.get(currentDoodle.size()-1));
 						currentDoodle.clear();
 						currentDoodle.addAll(newCurrentDoodle);
 					}
 					
 					if(!currentTriStrip.isEmpty()) {
 						ArrayList<Vector2> newCurrentTristrip = new ArrayList<Vector2>();
 						for(int i = 0; i<currentTriStrip.size() -1; i++) {
 							
 							Vector2 v = currentTriStrip.get(i);
 							if(v0.x < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(edgeScrollingSpeed,0));
 							else if(v0.y > Gdx.graphics.getHeight() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,-1 * edgeScrollingSpeed));
 							else if(v0.y < Constants.EDGE_DISTANCE)
 								v.add(new Vector2(0,edgeScrollingSpeed));
 							else if(v0.x > Gdx.graphics.getWidth() - Constants.EDGE_DISTANCE)
 								v.add(new Vector2(-1 * edgeScrollingSpeed,0));
 							
 							newCurrentTristrip.add(v);
 							
 						}
 						
 						newCurrentTristrip.add(currentTriStrip.get(currentTriStrip.size()-1));
 						currentTriStrip.clear();
 						currentTriStrip.addAll(newCurrentTristrip);
 					}
 				}
 				
 				
 			}			
 
 		}
 	}
 	
 	public static void updatePolygons() {
 		HashMap<Polygon, ArrayList<PlayerSoldier>> updatedList = new HashMap<Polygon, ArrayList<PlayerSoldier>>();
 		ArrayList<Polygon> updatedCircleHasPath = new ArrayList<Polygon>();
 		HashMap<Polygon, ArrayList<Vector3>> updatedPaths = new HashMap<Polygon, ArrayList<Vector3>>();
 
 		for(Polygon pol : circles.keySet()) {
 			
 			Array<Vector2> positions = new Array<Vector2>();
 			
 			for(PlayerSoldier soldier : circles.get(pol)) {
 				positions.add(soldier.position);
 			}
 			
 			float[] newVertices = null;
 			
 			if(positions.size == 1) {
 				Vector2 tempPos = positions.get(0).cpy();
 				Vector2 tempPos2 = positions.get(0).cpy();
 				Vector2 tempPos3 = positions.get(0).cpy();
 				
 				tempPos.x += 0.2f;
 				tempPos2.y -= 0.2f;
 				tempPos3.x += 0.2f;
 				tempPos3.y -= 0.2f;
 				
 				positions.clear();
 				positions.add(tempPos);
 				positions.add(tempPos2);
 				positions.add(tempPos3);
 				
 				newVertices = new float[positions.size * 2];
 				
 				int j=0;
 				for(int i=0;i<newVertices.length;i+=2) {
 					
 					newVertices[i] = positions.get(j).x;
 					newVertices[i+1] = positions.get(j).y;
 					
 					j++;
 				}
 				
 			}
 			else if(positions.size == 2) {
 				Vector2 tempPos = positions.get(0).cpy();
 				Vector2 tempPos2 = positions.get(1).cpy();
 				
 				Vector2 mid = tempPos.cpy().add(tempPos2.cpy());
 				mid.mul(0.5f);
 				
 				//not perfect but it's a triangle...
 				mid.add(Math.signum(mid.x) / 5, Math.signum(mid.y) / 5);
 				
 				positions.clear();
 				positions.add(tempPos);
 				positions.add(tempPos2);
 				positions.add(mid);
 				
 				newVertices = new float[positions.size * 2];
 				
 				int j=0;
 				for(int i=0;i<newVertices.length;i+=2) {
 					
 					newVertices[i] = positions.get(j).x;
 					newVertices[i+1] = positions.get(j).y;
 					
 					j++;
 				}
 				
 			}
 			else if(positions.size >= 3) {
 				Array<Vector2> bound = BoundingPolygon.createGiftWrapConvexHull(positions);
 				
 				newVertices = new float[bound.size * 2];
 				
 				int j=0;
 				for(int i=0;i<newVertices.length;i+=2) {
 					
 					newVertices[i] = bound.get(j).x;
 					newVertices[i+1] = bound.get(j).y;
 					
 					j++;
 				}
 			}
 			
 			
 			Polygon newPoly = new Polygon(newVertices);
 
 			//calculate Origin
 			float originX = 0;
 			float originY = 0;
 			for(int k=0;k<newVertices.length;k++) {
 				if(k%2==0)
 					originX += newVertices[k];
 				else
 					originY += newVertices[k];
 			}
 			
 			originX /= newVertices.length / 2;
 			originY /= newVertices.length / 2;
 			
 			newPoly.setOrigin(originX, originY);
 			newPoly.scale(1.f);
 			
 			updatedList.put(newPoly, circles.get(pol));
 			
 			if(paths.containsKey(pol)) {
 				updatedPaths.put(newPoly, paths.get(pol));
 				updatedCircleHasPath.add(newPoly);
 				paths.remove(pol);
 			}
 			
 		}
 		
 		circleHasPath.clear();
 		circleHasPath.addAll(updatedCircleHasPath);
 		
 		paths.putAll(updatedPaths);
 		
 		circles.clear();
 		circles.putAll(updatedList);
 		
 	}
 	
 	public static void updatePaths() {
 		
		ArrayList<Polygon> deleteThese = new ArrayList<Polygon>();
		
 		for(Polygon pol : paths.keySet()) {
 			
 			int toDelete = -1;
 			ConcurrentSkipListSet<Integer> set = new ConcurrentSkipListSet<Integer>();
 			
 			if(!paths.get(pol).isEmpty()) {
 				for(PlayerSoldier s : circles.get(pol)) {
 					int waypoint = -1;
 					for(Vector3 v : paths.get(pol)) {
 						 if(s.wayPoints.get(0).equals(v)) {
 							 waypoint = paths.get(pol).indexOf(v);
 							 break;
 						 }
 					}
 					if(waypoint > -1)
 						set.add(waypoint);
 				}
 				
				if(!set.isEmpty())
					toDelete = set.first();
 				
 				//delete all points on path that are no longer waypoints
 				if(toDelete > -1) {
 					paths.get(pol).removeAll(paths.get(pol).subList(0, toDelete));
 				}
 			}
 			
 			if(paths.get(pol).size() <= 1)
				deleteThese.add(pol);
 		}
		
		paths.keySet().removeAll(deleteThese);
 	
 	}
 
 	private void updateUnits() {
 		
 		for(int i = 0; i < GameSession.getInstance().soldiers.size; i++) {
 			GameSession.getInstance().soldiers.get(i).update(delta);
 		}
 		
 //		for(Polygon pol : circles.keySet()) {
 //			
 //			ArrayList<PlayerSoldier> soldiers = circles.get(pol);
 //			
 //			for(PlayerSoldier playerSoldier : soldiers) {
 //			
 //				ArrayList<Vector3> wayPoints = paths.get(pol);
 //				
 //				if(wayPoints != null && wayPoints.size() > 0) {
 //					playerSoldier.goTowards(new Vector2(wayPoints.get(0).x, wayPoints.get(0).z), false);
 //					if(playerSoldier.position.dst(new Vector2(wayPoints.get(0).x, wayPoints.get(0).z))<0.5f) {
 //						wayPoints.remove(0);
 //					}
 //				}
 //				
 //			}
 //			
 //		}
 	
 	}
 
 	private void updateAI() {		
 	}
 
 	private void collisionTest() {
 		
 		for(int soldier1ID = 0; soldier1ID < GameSession.getInstance().soldiers.size; soldier1ID++) {
 			Soldier soldier1 = GameSession.getInstance().soldiers.get(soldier1ID);
 			for(int soldier2ID = 0; soldier2ID < GameSession.getInstance().soldiers.size; soldier2ID++) {
 				Soldier soldier2 = GameSession.getInstance().soldiers.get(soldier2ID);
 				if(!soldier1.equals(soldier2) && soldier1.position.dst(soldier2.position) < .2f) {
 					soldier1.position.add(soldier1.position.cpy().sub(soldier2.position).mul(.1f));
 					soldier2.position.add(soldier2.position.cpy().sub(soldier1.position).mul(.1f));
 				}
 			}
 		}
 		
 	}
 	
 	
 	/**
 	 * generates Doodles and Trianglestrips for polygons
 	 */
 	@SuppressWarnings("unchecked")
 	public void generateDoodles() {
 
 		for(Polygon pol : circles.keySet()) {
 			
 			float[] vertices = pol.getTransformedVertices();
 			
 			if(vertices.length > 0) {
 				
 				float[] vertices3D = new float[(int) Math.ceil(vertices.length + vertices.length/3)];
 				
 				while(vertices3D.length%3 != 0) {
 					vertices3D = new float[1+vertices3D.length];
 				}
 				
 				int l = 0;
 				for(int k=0;k<vertices3D.length;k++) {
 					
 					if(k%3 == 1)
 						vertices3D[k] = 0;
 					else {
 						vertices3D[k] = vertices[l];
 						l++;
 					}
 					
 				}
 				
 				ArrayList<Vector2> tempDoodle = new ArrayList<Vector2>();
 				
 				for(int i=0; i<vertices3D.length;i+=3) {
 
 					Vector3 v = new Vector3(vertices3D[i],vertices3D[i+1],vertices3D[i+2]);
 					
 					renderMap.cam.project(v);
 					tempDoodle.add(new Vector2(v.x,v.y));
 					
 				}
 				tempDoodle.add(tempDoodle.get(0));
 				
 //				Polygon pCopy = new Polygon(pol.getTransformedVertices());
 
 				generatedDoodles.put(pol, (ArrayList<Vector2>) tempDoodle.clone());
 				
 				generatedTriangleStrips.put(pol, drawController.makeTriangleStrip(tempDoodle, true));
 				
 			}
 
 		}
 		
 		for(Polygon pol : paths.keySet()) {
 				
 			ArrayList<Vector2> tempDoodle = new ArrayList<Vector2>();
 			
 			Vector3 origin = new Vector3(pol.getOriginX(),0,pol.getOriginY());
 			renderMap.cam.project(origin);
 			//add origin for beauty reasons
 			if(!pol.contains(paths.get(pol).get(0).x,paths.get(pol).get(0).y))
 				tempDoodle.add(new Vector2(origin.x, origin.y));
 			for(Vector3 v : paths.get(pol)) {
 				
 				Vector3 vCpy = v.cpy();
 				
 				renderMap.cam.project(vCpy);
 				tempDoodle.add(new Vector2(vCpy.x, vCpy.y));
 			}
 			
 			generatedPathDoodles.put(pol, (ArrayList<Vector2>) tempDoodle.clone());
 			generatedPathTriangleStrips.put(pol, drawController.makeTriangleStrip(tempDoodle, true));
 			
 
 		}
 		
 	}
 
 	@Override
 	public void hide() {
 	}
 
 	@Override
 	public void dispose() {
 	}
 
 }
