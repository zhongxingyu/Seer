 package net.fourbytes.shadow;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import net.fourbytes.shadow.Input.Key;
 import net.fourbytes.shadow.entities.Cursor;
 import net.fourbytes.shadow.entities.Player;
 import net.fourbytes.shadow.utils.ShaderHelper;
 
 public class Camera implements Input.KeyListener {
 	
 	public Background bg;
 	public OrthographicCamera cam;
 
 	public static BitmapFont fpsFont = Fonts.light_normal;
 
 	protected static Rectangle objrec = new Rectangle();
 	protected static Image white;
 
 	protected Player player;
 
 	public boolean level = true;
 	public boolean firsttick = true;
 
 	public Rectangle camrec = new Rectangle(0f, 0f, 0f, 0f);
 	public Vector2 offs = new Vector2(0f, 0f);
 
 	public Camera() {
 		this.cam = new OrthographicCamera(Shadow.vieww, -Shadow.viewh);
 		this.cam.position.set(0, 0, 0);
 		this.cam.update();
 
 		//Input.debug1.listeners.add(this);
 		//Input.debug2.listeners.add(this);
 	}
 	
 	@Override
 	public void keyDown(Key key) {/*
 		if (key == Input.debug1) {
 			debug = !debug;
 		}
 		if (key == Input.debug2) {
 			if (level) {
 				Level llevel = Shadow.level;
 				if (llevel != null) {
 					llevel.hasvoid = !llevel.hasvoid;
 				}
 			}
 		}*/
 	}
 	@Override
 	public void keyUp(Key key) {
 	}
 	
 	public void resize() {
 		cam.viewportWidth = Shadow.vieww;
 		cam.viewportHeight = -Shadow.viewh;
 		cam.update();
 	}
 	
 	public void render() {
 		if (Shadow.level == null) {
 			cam.position.set(0, 0, 0);
 			cam.zoom = 2f;
 			cam.update();
 			
 			camrec.set(cam.position.x, cam.position.y, cam.viewportWidth*cam.zoom, -cam.viewportHeight*cam.zoom);
 			camrec.x -= camrec.width/2;
 			camrec.y -= camrec.height/2;
 			
 			Shadow.spriteBatch.setProjectionMatrix(cam.combined);
 			Shadow.spriteBatch.begin();
 			if (bg == null) {
 				bg = Background.getDefault();
 			}
 			Shadow.spriteBatch.disableBlending();
 			bg.render();
 			Shadow.spriteBatch.enableBlending();
 			Image logo = Images.getImage("logo");
 			logo.setScale(Shadow.vieww/Shadow.dispw * cam.zoom, -Shadow.viewh/Shadow.disph * cam.zoom);
 			logo.setPosition(0f - (logo.getScaleX()*logo.getWidth())/2f, 0f - (logo.getScaleY()*logo.getHeight())/2f);
 			logo.draw(Shadow.spriteBatch, 1f);
 
 			if (Shadow.loadstate < 2) {
 				Shadow.spriteBatch.setColor(0f, 0f, 0f, 1f);
 				Shadow.spriteBatch.draw(Images.getTextureRegion("white"), -Shadow.vieww / 2, Shadow.viewh / 2 + 2, Shadow.vieww, 1);
 				Shadow.spriteBatch.setColor(1f, 1f, 1f, 1f);
 				Shadow.spriteBatch.draw(Images.getTextureRegion("white"), -Shadow.vieww / 2, Shadow.viewh / 2 + 2, Shadow.vieww * Shadow.loadtick / Shadow.loadticks[0][Shadow.loadticks[0].length - 1], 1);
 			}
 
 			Shadow.spriteBatch.end();
 			
 			return;
 		}
 		if (!(Shadow.level instanceof MenuLevel)) {
 			player = Shadow.level.player;
 		}
 		if (player == null) {
 			Shadow.level.fillLayer(0);
 			player = new Player(new Vector2(0, 0), Shadow.level.layers.get(0));
 			firsttick = true;
 		}
 		float goalx = player.pos.x + player.rec.width/2f;
 		float goaly = player.pos.y + player.rec.height/2f;
 		if (firsttick) {
 			cam.zoom = 1f;
 			cam.position.x = goalx;
 			cam.position.y = goaly;
 			firsttick = false;
 		}
 		/*cam.zoom = cam.zoom;
 		cam.zoom += (1f+(Math.abs((player.movement.x/2f)+(player.movement.y/5f)))-cam.zoom)/5f;
 		cam.zoom = cam.zoom;*/
 		//cam.zoom = 1f;
 		cam.position.x += (goalx-cam.position.x)/15f;
 		cam.position.y += (goaly-cam.position.y)/20f;
 		offs.set(goalx - cam.position.x, goaly - cam.position.y);
 		cam.update();
 		
 		camrec.set(cam.position.x, cam.position.y, cam.viewportWidth*cam.zoom, -cam.viewportHeight*cam.zoom);
 		camrec.x -= camrec.width/2;
 		camrec.y -= camrec.height/2;
 		
 		Shadow.spriteBatch.setProjectionMatrix(cam.combined);
 		Shadow.spriteBatch.maxSpritesInBatch = 0;
 		Shadow.spriteBatch.begin();
 		
 		if (bg == null) {
 			bg = Background.getDefault();
 		}
 		bg.render();
 		
 		renderLevel(Shadow.level);
 		
 		if (fpsFont == null) {
 			fpsFont = Fonts.light_normal;
 		}
 		if (fpsFont != null) {
 			//fpsFont.setScale(Shadow.vieww/Shadow.dispw * cam.zoom, -Shadow.viewh/Shadow.disph * cam.zoom);
 			//fpsFont.draw(Shadow.spriteBatch, "FPS: "+Shadow.fps, cam.position.x - Shadow.vieww/2 * cam.zoom, cam.position.y - Shadow.viewh/2 * cam.zoom);
 			
 			fpsFont.setScale(Shadow.vieww/Shadow.dispw, -Shadow.viewh/Shadow.disph);
 			
 			String fps = "FPS: "+Shadow.fps;
 			TextBounds tb = fpsFont.getBounds(fps);
 			
 			fpsFont.draw(Shadow.spriteBatch, fps, cam.position.x + Shadow.vieww/2 - tb.width, cam.position.y - Shadow.viewh/2);
 		}
 
 		Shadow.spriteBatch.flush();
 
 		Shadow.spriteBatch.setProjectionMatrix(Input.cam.combined);
 
 		Input.render();
 
 		Shadow.spriteBatch.setProjectionMatrix(cam.combined);
 
 		Shadow.spriteBatch.end();
 		/*//To disable / enable debugging, just add / remove "/*" to / from the beginning of this line.
 		System.out.println("(Camera) max sprites in batch: "+Shadow.spriteBatch.maxSpritesInBatch);
 		System.out.println("(Camera) render calls: "+Shadow.spriteBatch.renderCalls);
 		Shadow.spriteBatch.maxSpritesInBatch = 0;
 		/*
 		 */
 	}
 
 	public void renderLevel(Level level) {
 		if (!(level instanceof MenuLevel)) {
 			Shadow.spriteBatch.flush();
 			ShaderHelper.setCurrentShader("light");
 			level.lights.updateLightBounds();
 
 			for (Layer ll : level.layers.values()) {
 				renderLayer(ll);
 			}
 
 			Shadow.spriteBatch.flush();
 			ShaderHelper.resetCurrentShader();
 		}
 
 		if (this.level) {
 			for (Cursor c : level.cursors) {
 				c.preRender();
 				c.render();
 			}
 
 			if (level.c != null && !Input.isAndroid) {
 				level.c.preRender();
 				level.c.render();
 			}
 
 			level.renderImpl();
 		}
 
 		if (level.hasvoid) {
 			Image levoid = Images.getImage("void");
 			objrec.set(camrec.x, level.tiledh - 2, 1024, 1);
 			levoid.setScaleY(-1f);
 			//i.setPosition(pos.x * Shadow.dispw/Shadow.vieww, pos.y * Shadow.disph/Shadow.viewh);
 			levoid.setPosition(objrec.x, objrec.y + objrec.height*2);
 			levoid.setSize(objrec.width, objrec.height + objrec.height);
 			levoid.draw(Shadow.spriteBatch, 1f);
 
 			Shadow.spriteBatch.disableBlending();
 			float fy = level.tiledh;
 			if (camrec.y > fy) {
 				fy = camrec.y;
 			}
 			objrec.set(camrec.x, fy, camrec.width, -camrec.height);
 			Image lewhite = Images.getImage("white");
 			lewhite.setColor(0f, 0f, 0f, 1f);
 			lewhite.setPosition(objrec.x, objrec.y);
 			lewhite.setSize(1f, 1f);
 			lewhite.setScale(objrec.width, objrec.height);
 			lewhite.draw(Shadow.spriteBatch, 1f);
 			Shadow.spriteBatch.enableBlending();
 		}
 	}
 
 	public void renderLayer(Layer l) {
 		if (l == null) {
 			return;
 		}
 		
 		renderShadows(l);
 
 		renderObjects(l, false, false);
 		renderObjects(l, false, true);
 
 		renderObjects(l, true, false);
 		renderObjects(l, true, true);
 		
 		Shadow.spriteBatch.enableBlending();
 	}
 	
 	private void renderShadows(Layer l) {
 		for (GameObject go : l.inView) {
 			if (go == null) continue;
 			go.preRender();
 			
 			Image img = go.tmpimg;
			Shadow.spriteBatch.setColor(0f, 0f, 0f, go.alpha*go.tmpimg.getColor().a*0.5f);
			img.getDrawable().draw(Shadow.spriteBatch, 0.125f + go.pos.x + go.renderoffs.x,
					0.125f + go.pos.y + go.rec.height + go.renderoffs.y,
					go.rec.width + go.renderoffs.width, -go.rec.height + go.renderoffs.height);
 			
 		}
 	}
 	
 	private void renderObjects(Layer l, boolean fgonly, boolean blending) {
 		for (GameObject go : l.inView) {
 			if (go == null) continue;
 			if (go.blending != blending) {
 				continue;
 			}
 			if (go instanceof Block) {
 				if (fgonly && ((Block)go).rendertop == 0x00) {
 					continue;
 				} else if (!fgonly && ((Block)go).rendertop == 0x01) {
 					continue;
 				}
 			} else {
 				if (fgonly) {
 					return;
 				}
 			}
 
 			//go.preRender();
 			
 			if (go.blending) {
 				Shadow.spriteBatch.enableBlending();
 			} else {
 				Shadow.spriteBatch.disableBlending();
 			}
 
 			if (go instanceof Block) {
 				if (fgonly) {
 					((Block)go).renderTop();
 				} else {
 					go.render();
 				}
 			} else {
 				go.render();
 			}
 			
 			if (go.highlighted > 0f) {
 				if (white == null) {
 					white = Images.getImage("white");
 				}
 				white.setColor(1f, 1f, 1f, go.highlighted/25f);
 				white.setPosition(objrec.x, objrec.y);
 				white.setSize(1f, -1f);
 				white.setScale(objrec.width, objrec.height);
 				white.draw(Shadow.spriteBatch, 1f);
 			}
 		}
 	}
 	
 }
 
