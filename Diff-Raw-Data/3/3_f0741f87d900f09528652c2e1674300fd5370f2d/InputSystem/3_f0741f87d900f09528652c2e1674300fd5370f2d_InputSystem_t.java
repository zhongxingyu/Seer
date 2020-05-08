 package se.exuvo.planets.systems;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.exuvo.planets.EntityFactory;
 import se.exuvo.planets.components.Position;
 import se.exuvo.planets.components.Radius;
 import se.exuvo.planets.components.VectorD2Component;
 import se.exuvo.planets.components.Velocity;
 import se.exuvo.planets.utils.VectorD2;
 import se.exuvo.settings.Settings;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.annotations.Mapper;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 /**
  * The system responsible for handling user input (keyboard and mouse).
  */
 public class InputSystem extends EntitySystem implements InputProcessor {
 
 	@Mapper ComponentMapper<Radius> rm;
 	@Mapper ComponentMapper<Position> pm;
 	@Mapper ComponentMapper<Velocity> vm;
 
 	/** The gameworld-camera. */
 	private OrthographicCamera camera;
 	private Vector3 cameraVelocity;
 	private float cameraMoveSpeed = 1000f;
 	private float zoomLevel = 1f, zoomSensitivity = Settings.getFloat("zoomSensitivity");
 
 	/**
 	 * Used to hold the mouseposition where the user last placed a planet. It is used for when the user rightclick-drags the mouse, creating
 	 * a planet at the rightclick and giving it an velocity in the dragged-to direction.
 	 */
 	private VectorD2 mouseVector, mouseStartVector, mouseTrue, mouseTrueDiff, mouseTrueStart;
 
 	// Buffers due to gui has to be done in the correct thread.
 	private boolean createPlanet, selectPlanet, potentialMove, potentialPush, movePlanet, pushPlanet, releasePlanet, follow, nextPlanet,
 			moveWindow;
 	private Bag<Entity> selectedPlanets;
 	private long potentialStart, potentialTimeDelay = Settings.getInt("moveDelay");
 	private float potentialMoveMouseShake = Settings.getFloat("moveMouseSensitivity"), pushForceMultiplier = Settings
 			.getFloat("pushForceMultiplier");
 	private Entity lastCreatedPlanet;
 
 	private ShapeRenderer render;
 	private SpriteBatch renderBatch;
 	private HudRenderSystem hudSys;
 
 	private boolean paused, wasPaused;
 
 	private static float cos90 = MathUtils.cosDeg(90), cos210 = MathUtils.cosDeg(210), cos330 = MathUtils.cosDeg(330);
 	private static float sin90 = MathUtils.sinDeg(90), sin210 = MathUtils.sinDeg(210), sin330 = MathUtils.sinDeg(330);
 
 	private List<PlanetSelectionChanged> listeners = new ArrayList<PlanetSelectionChanged>();
 	private UISystem uisystem;
 
 	public InputSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Position.class, Radius.class));
 		this.camera = camera;
 		mouseVector = new VectorD2();
 		mouseStartVector = new VectorD2();
 		mouseTrue = new VectorD2();
 		mouseTrueDiff = new VectorD2();
 		mouseTrueStart = new VectorD2();
 		cameraVelocity = new Vector3();
 	}
 
 	// TODO dragging selected planet
 
 	@Override
 	protected void initialize() {
 		render = new ShapeRenderer();
 		renderBatch = new SpriteBatch();
 
 		OrthographicCamera textCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		renderBatch.setProjectionMatrix(textCamera.combined);
 
 		hudSys = world.getSystem(HudRenderSystem.class);
 		uisystem = world.getSystem(UISystem.class);
 
 		selectedPlanets = new Bag<Entity>();
 
 		if (zoomSensitivity <= 1) {
 			System.out.println("Warning zoomSensitivity is too low to have any effect: " + zoomSensitivity);
 		}
 	}
 
 	@Override
 	protected void begin() {
 		render.setProjectionMatrix(camera.combined);
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		// TODO separate the various operations into methods.
 		camera.position.add(cameraVelocity.cpy().mul(camera.zoom * Gdx.graphics.getDeltaTime()));
 		updateMouse();
 
 		if (createPlanet) {
 			lastCreatedPlanet = EntityFactory.createPlanet(world, uisystem.getRadius(), uisystem.getMass(), new VectorD2(mouseVector.x,
 					mouseVector.y), uisystem.getVelocity(), uisystem.getColor());
 			lastCreatedPlanet.addToWorld();
 //			selectedPlanets.add(planet);
 
 			createPlanet = false;
 		}
 
 		if (selectPlanet) {
 			Entity planet = null;
 
 			// compare each planets position to the mousePos to see if it was clicked.
 
 			double minDist = Double.MAX_VALUE;
 			Entity closestPlanet = null;
 
 			for (int i = 0; i < entities.size(); i++) {
 				Entity e = entities.get(i);
 				VectorD2 p = pm.get(e).vec;
 				Radius r = rm.get(e);
 
 				double distance = mouseVector.dst(p) - r.radius;
 				if (distance < minDist) {
 					minDist = distance;
 					closestPlanet = e;
 				}
 			}
 			if (minDist < 10 * camera.zoom) {
 				planet = closestPlanet;
 			}
 
 			if (planet == null) {
 				if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
 					selectedPlanets.clear();
 					fireSelectionChangeEvent();
 				}
 			} else {
 				if (selectedPlanets.contains(planet)) {
 					// Allow for moving planets
 //					selectedPlanets.remove(planet);
 //					fireSelectionChangeEvent();
 				} else {
 					if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
 						selectedPlanets.clear();
 					}
 					selectedPlanets.add(planet);
 					fireSelectionChangeEvent();
 
 					if (follow) {
 						potentialMove = false;
 					}
 				}
 			}
 			selectPlanet = false;
 		}
 
 		if (!selectedPlanets.isEmpty()) {
 			render.begin(ShapeType.Triangle);
 			render.setColor(Color.CYAN);
 
 			for (Entity e : selectedPlanets) {
 				Vector2 p = pm.get(e).vec.toVector2();
 				Radius r = rm.get(e);
 
 				// draw a triangle around the planet (showing that it's selected)
 				float k = (float) (r.radius * 3f);
 				render.triangle(p.x + k * cos90, p.y + k * sin90, p.x + k * cos210, p.y + k * sin210, p.x + k * cos330, p.y + k * sin330);
 			}
 
 			render.end();
 		}
 
 		if (potentialMove && !selectedPlanets.isEmpty()) {
 			if (mouseDiff().len() > potentialMoveMouseShake * camera.zoom
 					|| System.currentTimeMillis() - potentialStart > potentialTimeDelay) {
 				movePlanet = true;
 				potentialMove = false;
 				checkPause();
 			}
 		}
 
 		if (potentialPush) {
 			if (mouseDiff().len() > potentialMoveMouseShake * camera.zoom
 					|| System.currentTimeMillis() - potentialStart > potentialTimeDelay) {
 				pushPlanet = true;
 				potentialPush = false;
 				checkPause();
 			}
 		}
 
 		if (movePlanet) {
 			VectorD2 diff = mouseDiff();
 			render.begin(ShapeType.Circle);
 			render.setColor(Color.WHITE);
 
 			for (Entity e : selectedPlanets) {
 				VectorD2 p = pm.get(e).vec.cpy().add(diff);
 				float r = (float) rm.get(e).radius;
 				render.circle(p.floatX(), p.floatY(), r);
 			}
 			render.end();
 
 			render.begin(ShapeType.Line);
 			render.line(mouseStartVector.floatX(), mouseStartVector.floatY(), mouseVector.floatX(), mouseVector.floatY());
 			render.end();
 
 			renderBatch.begin();
 			hudSys.font.draw(renderBatch, "" + (int) diff.x + ", " + (int) diff.y, hudSys.mouseX(), hudSys.mouseY() + 40);
 			hudSys.font.draw(	renderBatch, "[" + (int) mouseVector.x + ", " + (int) mouseVector.y + "]", hudSys.mouseX(),
 								hudSys.mouseY() + 20);
 			renderBatch.end();
 		}
 
 		if (pushPlanet) {
 			// angle from where the mouse was when push started to the current mousePos
 			VectorD2 v = new VectorD2(mouseStartVector.y - mouseVector.y, mouseVector.x - mouseStartVector.x).nor().mul(10 * camera.zoom);
 
 			// draw an arrow-like triangle from startMouse to current mousePos
 			Vector2 pointA = mouseStartVector.cpy().add(v).toVector2();
 			Vector2 pointB = mouseStartVector.cpy().sub(v).toVector2();
 			Vector2 pointC = mouseVector.toVector2();
 
 			render.begin(ShapeType.FilledTriangle);
 			render.setColor(Color.CYAN);
 			render.filledTriangle(pointA.x, pointA.y, pointB.x, pointB.y, pointC.x, pointC.y);
 			render.end();
 
 			renderBatch.begin();
 			hudSys.font.draw(renderBatch, "" + (int) (mouseDiff().len() * pushForceMultiplier), hudSys.mouseX(), hudSys.mouseY() + 20);
 			renderBatch.end();
 		}
 
 		if (releasePlanet) {
 			VectorD2 diff = mouseDiff();
 			if (pushPlanet) {
 				// give the planet a velocity. (with the angle and magnitude the user showed)
 				diff.mul(pushForceMultiplier);
 
 				if (lastCreatedPlanet == null) {
 					for (Entity e : selectedPlanets) {
 						vm.get(e).vec.add(diff);
 					}
 				} else {
 					vm.get(lastCreatedPlanet).vec.add(diff);
 					lastCreatedPlanet = null;
 				}
 
 				pushPlanet = false;
 				restorePause();
 			}
 
 			if (movePlanet) {
 				for (Entity e : selectedPlanets) {
 					pm.get(e).vec.add(diff);
 				}
 
 				movePlanet = false;
 				restorePause();
 			}
 
 			releasePlanet = false;
 		}
 
 		if (follow && !selectedPlanets.isEmpty()) {
 			VectorD2 center = VectorD2Component.mean(pm, selectedPlanets);
 			camera.position.set(center.floatX(), center.floatY(), 0);
 		}
 
 		if (nextPlanet) {
 			if (!entities.isEmpty()) {
 				Entity target = null;
 
 				if (selectedPlanets.isEmpty()) {
 					target = entities.get(0);
 				} else {
 					for (int i = 0; i < entities.size(); i++) {
 						if (selectedPlanets.get(0).getId() == entities.get(i).getId()) {
 							if (i + 1 == entities.size()) {
 								target = entities.get(0);
 							} else {
 								target = entities.get(i + 1);
 							}
 							break;
 						}
 					}
 				}
 
 				selectedPlanets.clear();
 				selectedPlanets.add(target);
 				fireSelectionChangeEvent();
 
 				VectorD2 pos = pm.get(target).vec;
 				camera.position.set(pos.floatX(), pos.floatY(), 0);
 			}
 
 			nextPlanet = false;
 		}
 
 		if (moveWindow) {
 			updateTrueMouse();
 			VectorD2 diff = mouseTrueStart.to(mouseTrue).mul(camera.zoom);
 
 			renderBatch.begin();
 			hudSys.font.draw(renderBatch, "" + (long) diff.x + ", " + (long) diff.y, hudSys.mouseX(), hudSys.mouseY() + 40);
 
 			diff.add(mouseStartVector);
 
 			hudSys.font.draw(renderBatch, "[" + (long) diff.x + ", " + (long) diff.y + "]", hudSys.mouseX(), hudSys.mouseY() + 20);
 			renderBatch.end();
 
 			Vector2 pointA = mouseStartVector.toVector2();
 			Vector2 pointB = diff.toVector2();
 
 			render.setColor(Color.WHITE);
 			render.begin(ShapeType.Line);
 			render.line(pointA.x, pointA.y, pointB.x, pointB.y);
 			render.end();
 
 			diff.set(mouseTrueDiff).mul(camera.zoom);
 			camera.position.add(diff.floatX(), diff.floatY(), 0);
 		}
 	}
 
 	@Override
 	protected void end() {}
 
 	private void checkPause() {
 		wasPaused = paused;
 		if (Settings.getBol("pauseWhenCreatingPlanets")) {
 			setPaused(true);
 		}
 	}
 
 	private void restorePause() {
 		if (!wasPaused) {
 			setPaused(false);
 		}
 	}
 
 	private void updateMouse() {
 		Vector3 mouseTmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
 
 		// unproject screen coordinates to corresponding world position
 		camera.unproject(mouseTmp);
 		mouseVector.set(mouseTmp.x, mouseTmp.y);
 		if (Float.isNaN(camera.position.x)) {
 			System.out.println("NaN TODO fix");
 			camera.position.set(0, 0, 0);
 		}
 	}
 
 	private void updateTrueMouse() {
 		int x = -Gdx.input.getX(), y = Gdx.input.getY();
 		mouseTrueDiff.set(x - mouseTrue.x, y - mouseTrue.y);
 		mouseTrue.set(x, y);
 	}
 
 	private void mouseStart() {
 		mouseStartVector.set(mouseVector);
 	}
 
 	private void mouseStartTrue() {
 		mouseTrueStart.set(mouseTrue);
 		mouseTrueDiff.set(0, 0);
 	}
 
 	private VectorD2 mouseDiff() {
 		return mouseStartVector.to(mouseVector);
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public boolean isFollow() {
 		return follow;
 	}
 
 	private void setPaused(boolean newValue) {
 		paused = newValue;
 	}
 
 	public boolean isSpeedup() {
 		return !isPaused() && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
 	}
 
 	public boolean isSSpeedup() {
 		return !isPaused() && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Input.Keys.SPACE) {
 			setPaused(!paused);
 			return true;
 		} else if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
 			cameraVelocity.y += cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
 			cameraVelocity.y += -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
 			cameraVelocity.x += cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
 			cameraVelocity.x += -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.T) {
 			follow = !follow;
 			return true;
 		} else if (keycode == Input.Keys.N) {
 			nextPlanet = true;
 			return true;
 		} else if (keycode == Input.Keys.C) {
 			CollisionSystem c = world.getSystem(CollisionSystem.class);
 			c.collisions = !c.collisions; 
 			return true;
 		} else if (keycode == Input.Keys.FORWARD_DEL) {
 			for (Entity e : selectedPlanets) {
 				e.deleteFromWorld();
 			}
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
 			cameraVelocity.y -= cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
 			cameraVelocity.y -= -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
 			cameraVelocity.x -= cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
 			cameraVelocity.x -= -cameraMoveSpeed;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char c) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		mouseStart();
 
 		if (button == Input.Buttons.LEFT) {
 			selectPlanet = true;
 			potentialMove = true;
 			potentialStart = System.currentTimeMillis();
 			return true;
 		} else if (button == Input.Buttons.RIGHT) {
 			if (selectedPlanets.isEmpty()) {
 				createPlanet = true;
 				potentialPush = true;
 				potentialStart = System.currentTimeMillis();
 			} else {
 				pushPlanet = true;
 				checkPause();
 			}
 			return true;
 		} else if (button == Input.Buttons.MIDDLE) {
 			moveWindow = true;
 			updateTrueMouse();
 			mouseStartTrue();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		if (button == Input.Buttons.RIGHT) {
 			releasePlanet = true;
 			potentialPush = false;
			if(!pushPlanet){
				lastCreatedPlanet = null;
			}
 			return true;
 		} else if (button == Input.Buttons.LEFT) {
 			potentialMove = false;
 			releasePlanet = true;
 		} else if (button == Input.Buttons.MIDDLE) {
 			moveWindow = false;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// -1 for zoom-in. 1 for zoom out
 		float oldZoom = camera.zoom;
 
 		zoomLevel += amount;
 		if (zoomLevel < 0) {
 			zoomLevel = 0;
 		}
 
 		// camera.zoom >= 1
 		camera.zoom = (float) Math.pow(zoomSensitivity, zoomLevel);
 
 //		System.out.println("zoom:" + camera.zoom + "  zoomLevel:" + zoomLevel);
 
 		if (camera.zoom > 4.5E15f) {
 			camera.zoom = 4.5E15f;
 			zoomLevel = (float) (Math.log(camera.zoom) / Math.log(zoomSensitivity));
 		}
 
 		if (amount < 0) {
 //			Det som var under musen innan scroll ska fortsätta vara där efter zoom
 //			http://stackoverflow.com/questions/932141/zooming-an-object-based-on-mouse-position
 
 			Vector3 diff = camera.position.cpy().sub(new Vector3(mouseVector.floatX(), mouseVector.floatY(), 0f));
 			camera.position.sub(diff.sub(diff.cpy().div(oldZoom).mul(camera.zoom)));
 		}
 
 		return true;
 	}
 
 	public void clearSelection() {
 		selectedPlanets.clear();
 		fireSelectionChangeEvent();
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	protected void removed(Entity e) {
 		selectedPlanets.remove(e);
 		fireSelectionChangeEvent();
 	};
 
 	public void addListener(PlanetSelectionChanged psc) {
 		listeners.add(psc);
 	}
 
 	private void fireSelectionChangeEvent() {
 		for (PlanetSelectionChanged psc : listeners) {
 			psc.planetSelectionChanged(selectedPlanets);
 		}
 	}
 
 	public static interface PlanetSelectionChanged {
 		public void planetSelectionChanged(Bag<Entity> planets);
 	}
 }
