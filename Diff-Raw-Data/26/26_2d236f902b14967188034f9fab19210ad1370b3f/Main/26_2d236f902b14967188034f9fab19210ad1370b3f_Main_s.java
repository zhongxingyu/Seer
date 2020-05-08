 package net.carmgate.morph;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import net.carmgate.morph.actions.ShipEditorSelect;
 import net.carmgate.morph.actions.WorldMultiSelect;
 import net.carmgate.morph.actions.WorldSelect;
 import net.carmgate.morph.actions.common.Action;
 import net.carmgate.morph.actions.common.ActionHints;
 import net.carmgate.morph.actions.common.Event;
 import net.carmgate.morph.actions.common.Event.EventType;
 import net.carmgate.morph.actions.drag.DragContext;
 import net.carmgate.morph.conf.Conf;
 import net.carmgate.morph.exception.ConcreteInitRendererInAbstractClassException;
 import net.carmgate.morph.model.Model;
 import net.carmgate.morph.model.UiState;
 import net.carmgate.morph.model.behaviors.SpawnShips;
 import net.carmgate.morph.model.behaviors.steering.Orbit;
 import net.carmgate.morph.model.behaviors.steering.WanderWithinRange;
 import net.carmgate.morph.model.common.Vect3D;
 import net.carmgate.morph.model.entities.Morph;
 import net.carmgate.morph.model.entities.Morph.MorphType;
 import net.carmgate.morph.model.entities.Planet;
 import net.carmgate.morph.model.entities.Ship;
 import net.carmgate.morph.model.entities.Star;
 import net.carmgate.morph.model.entities.Station;
 import net.carmgate.morph.model.entities.common.Entity;
 import net.carmgate.morph.model.entities.common.Renderable;
 import net.carmgate.morph.model.player.Player;
 import net.carmgate.morph.model.player.Player.FOF;
 import net.carmgate.morph.model.player.Player.PlayerType;
 import net.carmgate.morph.ui.common.RenderingSteps;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.reflections.Reflections;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Main {
 
 	private static Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
 	/**
 	 * Main Class
 	 */
 	public static void main(String[] argv) {
 		Main sample = new Main();
 		sample.start();
 	}
 
 	public static void shipEditorRender(Ship ship, int glMode) {
 		List<Morph> morphsToDraw = new ArrayList<>();
 		for (MorphType morphType : MorphType.values()) {
 			List<Morph> morphsByType = ship.getMorphsByType(morphType);
 			if (morphsByType != null) {
 				morphsToDraw.addAll(morphsByType);
 			}
 		}
 
 		int layer = 0;
 		Iterator<Morph> morphIt = morphsToDraw.iterator();
 		while (morphIt.hasNext()) {
 
 			// draw the most centric one
 			if (layer == 0) {
 				if (morphIt.hasNext()) {
 					Morph morph = morphIt.next();
 					GL11.glPushName(morph.getId());
 					morph.render(glMode);
 					GL11.glPopName();
 					GL11.glTranslatef(-64, 0, 0);
 				}
 			}
 
 			GL11.glTranslatef(64, 0, 0);
 			GL11.glRotatef(60, 0, 0, 1);
 			for (int i = 0; i < 6; i++) {
 				GL11.glRotatef(60, 0, 0, 1);
 				for (int j = 0; j < layer; j++) {
 					if (morphIt.hasNext()) {
 						Morph morph = morphIt.next();
 						GL11.glRotatef(-(i + 2) * 60, 0, 0, 1);
 						GL11.glPushName(morph.getId());
 						morph.render(glMode);
 						GL11.glPopName();
 						GL11.glRotatef((i + 2) * 60, 0, 0, 1);
 					}
 					GL11.glTranslatef(64, 0, 0);
 				}
 			}
 			GL11.glRotatef(-60, 0, 0, 1);
 
 			layer++;
 		}
 		GL11.glTranslatef(-(layer - 1) * 64, 0, 0);
 	}
 
 	private final Model model = Model.getModel();
 	private final List<Action> mouseActions = new LinkedList<>();
 
 	private final List<Action> keyboardActions = new LinkedList<>();
 
 	private int fpsCounter = 0;
 	private float meanFpsCounter = 0;
 
 	private long lastFpsResetTs = 0;
 	private Entity planet;
 
 	/**
 	 * This method initializes UI handlers.
 	 * Some special case handlers can not be initialized dynamically at the moment.
 	 */
 	private void initActions() {
 		// The drag context shared by all actions needing to handle drag
 		DragContext dragContext = new DragContext();
 
 		// select actions have to be handled before anything else
 		// because some other actions (like MoveTo or Attack) need the result of the action selection
 		mouseActions.add(new WorldSelect());
 		mouseActions.add(new WorldMultiSelect());
 		mouseActions.add(new ShipEditorSelect());
 
 		// Look for the action classes
 		Set<Class<? extends Action>> actions = new Reflections("net.carmgate.morph.actions").getSubTypesOf(Action.class);
 		for (Class<? extends Action> action : actions) {
 			try {
 				Action actionInstance;
 
 				// Handle actions hints
 				// Instanciate drag actions with common drag context
 				if (action.getAnnotation(ActionHints.class).dragAction()) {
 					actionInstance = action.getConstructor(DragContext.class).newInstance(dragContext);
 				} else {
 					actionInstance = action.newInstance();
 				}
 
 				// autoload mouse actions if requested
 				if (action.getAnnotation(ActionHints.class).mouseActionAutoload()) {
 					mouseActions.add(actionInstance);
 				}
 
 				// autoload keyboard actions if requested
 				if (action.getAnnotation(ActionHints.class).keyboardActionAutoload()) {
 					keyboardActions.add(actionInstance);
 				}
 			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
 					| SecurityException e) {
 				LOGGER.error("Exception raised while creating actions", e);
 			}
 
 		}
 	}
 
 	/**
 	 * Initialise the GL display
 	 * 
 	 * @param width The width of the display
 	 * @param height The height of the display
 	 */
 	private void initGL(int width, int height) {
 		try {
 			Display.setDisplayMode(new DisplayMode(width, height));
 			Display.create();
 			Display.setTitle("Morph");
 			// Display.setVSyncEnabled(true);
 			Display.setResizable(true);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 
 		LOGGER.debug("init view: " + width + "x" + height);
 
 		// enable texturing
 		// GL11.glEnable(GL11.GL_TEXTURE_2D);
 		// It seems it's not needed, but I do not understand why ...
 
 		initView();
 	}
 
 	private void initModel() {
 
 		Star star = new Star(3000, 3000, 0, 5000, 500, 10000);
 		Model.getModel().addEntity(star);
 		// TODO remove attribute from class
 		planet = new Planet(star, 1000, 100, 100000);
 		// TODO Clean this, we should not have to mention the orbit radius twice
 		planet.addBehavior(new Orbit(planet, star, 100000, true));
 		Model.getModel().addEntity(planet);
 
 		Station station = new Station(planet, 100, 50, 5000);
 
 		Player player = new Player(PlayerType.AI, "Nemesis", FOF.FOE);
 		Ship enemyShip = new Ship(128, 0, 0, 0, 5, player);
 		enemyShip.addMorph(new Morph(MorphType.OVERMIND));
 		enemyShip.addMorph(new Morph(MorphType.SIMPLE_PROPULSOR));
		enemyShip.addBehavior(new WanderWithinRange(enemyShip, 50, 100, station, 5000f));
 		enemyShip.update(); // TODO This is needed so that behaviors are really in the behavior set. That is an issue.
 
 		station.addBehavior(new Orbit(station, planet, 5000, true));
		station.addBehavior(new SpawnShips(station.getPos(), 1, 5000, enemyShip));
 		Model.getModel().addEntity(station);
 
 	}
 
 	/**
 	 * Scans the classpath looking for renderers (classes annotated with @{@link Renders})
 	 * initializes them and add them to the maps of the renderers
 	 */
 	private void initRenderables() {
 		// Init the reflection API
 		Reflections reflections = new Reflections("net.carmgate.morph");
 
 		// Look for classes implementing the Renderable interface
 		Set<Class<? extends Renderable>> renderables = reflections.getSubTypesOf(Renderable.class);
 
 		// Iterate over the result set to initialize each renderable
 		for (Class<? extends Renderable> renderable : renderables) {
 			if (renderable.equals(Entity.class)) {
 				continue;
 			}
 
 			// if the class is abstract, do not try instanciate either
 			if (Modifier.isAbstract(renderable.getModifiers())) {
 				// if the initRenderer method is not abstract in this class, log an error
 				try {
 					if (!Modifier.isAbstract(renderable.getMethod("initRenderer", new Class<?>[] {}).getModifiers())) {
 						throw new ConcreteInitRendererInAbstractClassException(renderable);
 					}
 				} catch (NoSuchMethodException | SecurityException | ConcreteInitRendererInAbstractClassException e) {
 					LOGGER.error("Error while retrieveing initRenderer method within " + renderable.getName(), e);
 				}
 				continue;
 			}
 
 			try {
 				renderable.newInstance().initRenderer();
 			} catch (InstantiationException | IllegalAccessException e) {
 				LOGGER.error("Exception raised while trying to init renderer " + renderable.getName(), e);
 			}
 
 		}
 	}
 
 	private void initView() {
 
 		int width = Display.getWidth();
 		int height = Display.getHeight();
 		LOGGER.debug("init view: " + width + "x" + height);
 
 		// init the window
 		Model.getModel().getWindow().setWidth(width);
 		Model.getModel().getWindow().setHeight(height);
 
 		// set clear color - Wont be needed once we have a background
 		GL11.glClearColor(0f, 0f, 0f, 0f);
 
 		// enable alpha blending
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glEnable(GL11.GL_LINE_SMOOTH);
 		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
 
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 
 		GL11.glOrtho(-width / 2, width / 2, height / 2, -height / 2, 1, -1);
 		GL11.glViewport(0, 0, width, height);
 
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 	}
 
 	/**
 	 * draw a quad with the image on it
 	 */
 	public void render() {
 
 		try {
 			Vect3D focalPoint = model.getViewport().getFocalPoint();
 			float zoomFactor = model.getViewport().getZoomFactor();
 			if (model.getViewport().getLockedOnEntity() != null) {
 				focalPoint.copy(new Vect3D().add(model.getViewport().getLockedOnEntity().getPos()).mult(zoomFactor));
 			}
 
 			GL11.glTranslatef(-focalPoint.x, -focalPoint.y, -focalPoint.z);
 
 			GL11.glRotatef(model.getViewport().getRotation(), 0, 0, 1);
 			GL11.glScalef(zoomFactor, zoomFactor, 1);
 
 			Model.getModel().getRootWA().render(GL11.GL_RENDER);
 
 			// Render particles
 			model.getParticleEngine().render(GL11.GL_RENDER);
 
 			// Rendering all renderable elements
 			for (RenderingSteps renderingStep : RenderingSteps.values()) {
 				if (Model.getModel().getEntitiesByRenderingType(renderingStep) != null) {
 					for (Entity renderable : Model.getModel().getEntitiesByRenderingType(renderingStep).values()) {
 						renderable.render(GL11.GL_RENDER);
 					}
 				}
 			}
 
 			GL11.glScalef(1f / zoomFactor, 1f / zoomFactor, 1);
 			GL11.glRotatef(-model.getViewport().getRotation(), 0, 0, 1);
 			GL11.glTranslatef(focalPoint.x, focalPoint.y, focalPoint.z);
 
 			// TODO activate ship editor upon some action
 			if (Model.getModel().getUiContext().getUiState() == UiState.SHIP_EDITOR) {
 				shipEditorRender(Model.getModel().getSelfShip(), GL11.GL_RENDER);
 			}
 		} catch (Exception e) {
 			LOGGER.debug("Exception caught in main loop.", e);
 		}
 	}
 
 	private void runAction(Action action) {
 		ActionHints actionHints = action.getClass().getAnnotation(ActionHints.class);
 		for (UiState uiState : actionHints.uiState()) {
 			if (uiState == Model.getModel().getUiContext().getUiState()) {
 				action.run();
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Start the application
 	 */
 	public void start() {
 		// init OpenGL context
 		initGL(Conf.getIntProperty("window.initialWidth"), Conf.getIntProperty("window.initialHeight"));
 
 		// scan for renderers
 		initRenderables();
 		// init world model
 		initModel();
 
 		// Configure Actions
 		initActions();
 
 		// Rendering loop
 		while (true) {
 
 			// Renders everything
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
 			if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 				render();
 			} else {
 				new WorldSelect().render(GL11.GL_SELECT);
 			}
 
 			// updates display and sets frame rate
 			Display.update();
 			Display.sync(200);
 
 			// update model
 			Model.getModel().update();
 
 			// handle window resize
 			if (Display.wasResized()) {
 				initView();
 			}
 
 			GL11.glMatrixMode(GL11.GL_PROJECTION);
 			GL11.glLoadIdentity();
 
 			int width = Display.getWidth();
 			int height = Display.getHeight();
 			GL11.glOrtho(-width / 2, width / 2, height / 2, -height / 2, 1, -1);
 			GL11.glViewport(0, 0, width, height);
 
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glLoadIdentity();
 
 			// Fire events accordingly
 			if (Mouse.next()) {
 				int dWheel = Mouse.getDWheel();
 				if (dWheel != 0) {
 					LOGGER.debug("Logged a mouse wheel: " + dWheel);
 					Model.getModel().getInteractionStack()
 							.addEvent(new Event(EventType.MOUSE_WHEEL, dWheel, new int[] { Mouse.getEventX(), Mouse.getEventY() }));
 					for (Action action : mouseActions) {
 						runAction(action);
 					}
 				}
 
 				// add interaction to ui context
 				EventType evtType = null;
 				if (Mouse.getEventButton() >= 0) {
 					if (Mouse.getEventButtonState()) {
 						evtType = EventType.MOUSE_BUTTON_DOWN;
 					} else {
 						evtType = EventType.MOUSE_BUTTON_UP;
 					}
 					Event event = new Event(evtType, Mouse.getEventButton(), new int[] { Mouse.getEventX(), Mouse.getEventY() });
 					Model.getModel().getInteractionStack().addEvent(event);
 					for (Action action : mouseActions) {
 						runAction(action);
 					}
 				}
 
 			}
 
 			if (Keyboard.next()) {
 				EventType evtType = null;
 				if (Keyboard.getEventKeyState()) {
 					evtType = EventType.KEYBOARD_DOWN;
 				} else {
 					evtType = EventType.KEYBOARD_UP;
 				}
 				Event event = new Event(evtType, Keyboard.getEventKey(), new int[] { Mouse.getEventX(), Mouse.getEventY() });
 				Model.getModel().getInteractionStack().addEvent(event);
 				LOGGER.debug("Sending keyboard event " + Keyboard.getEventKey());
 				for (Action action : keyboardActions) {
 					runAction(action);
 				}
 			}
 
 			int dx = Mouse.getDX();
 			int dy = Mouse.getDY();
 			if (dx != 0 || dy != 0) {
 				for (Action action : mouseActions) {
 					Event event = new Event(EventType.MOUSE_MOVE, Mouse.getEventButton(), new int[] { Mouse.getX(), Mouse.getY() });
 					if (Model.getModel().getInteractionStack().getLastEvent().getEventType() != EventType.MOUSE_MOVE) {
 						Model.getModel().getInteractionStack().addEvent(event);
 					}
 					runAction(action);
 				}
 			}
 
 			//
 			// // If no morph is selected, the right click should be understood
 			// as a moveto order.
 			// if
 			// (globalModel.getSelectedShip().getSelectedMorphList().isEmpty())
 			// {
 			// List<IA> iaList = globalModel.getSelectedShip().getIAList();
 			//
 			// // Look for existing tracker
 			// for (IA ia : iaList) {
 			// if (ia instanceof FixedPositionTracker) {
 			// ((FixedPositionTracker) ia).setTargetPos(worldMousePos);
 			// }
 			// }
 			//
 			// iaList.add(new
 			// FixedPositionTracker(globalModel.getSelectedShip(),
 			// worldMousePos));
 			// }
 			// }
 			//
 			// // Handling shoot
 			// if (Mouse.getEventButton() == 1 && !Mouse.getEventButtonState()
 			// && globalModel.getSelectedShip() != null && World.combat) {
 			// globalModel.getSelectedShip().getIAList().add(new
 			// WorldPositionFirer(globalModel.getSelectedShip(),
 			// worldMousePos));
 			// }
 			//
 			// // int dWheel = Mouse.getDWheel();
 			// // if (dWheel != 0) {
 			// // float scale = (float) (Math.pow(1 + Math.pow(4, -5 +
 			// Math.abs(dWheel / 120)), Math.signum(dWheel)));
 			// // GL11.glScalef(scale, scale, scale);
 			// // }
 			// }
 			//
 			// if (Keyboard.next()) {
 			// toggleDebugAction.run();
 			// toggleCombatMode.run();
 			// toggleFreezeAction.run();
 			// }
 
 			if (Display.isCloseRequested()) {
 				Display.destroy();
 				System.exit(0);
 			}
 
 			fpsCounter++;
 			if (model.getCurrentTS() - lastFpsResetTs > 1000) {
 				if (meanFpsCounter == 0) {
 					meanFpsCounter = fpsCounter;
 				}
 				meanFpsCounter = meanFpsCounter * 0.9f + fpsCounter * 0.1f;
 				LOGGER.debug("Fps: " + fpsCounter + ". Mean fps : " + meanFpsCounter);
 				lastFpsResetTs += 1000;
 				fpsCounter = 0;
 			}
 		}
 	}
 }
