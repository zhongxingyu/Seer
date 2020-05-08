 package no.eirikb.h5z1.client;
 
 import gwt.g2d.client.util.FpsTimer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import no.eirikb.h5z1.client.keyhack.KeyHack;
 import no.eirikb.h5z1.client.keyhack.KeyHackCallback;
 import no.eirikb.h5z1.client.resources.Resources;
 import no.eirikb.h5z1.client.resources.ResourcesContainer;
 import no.eirikb.h5z1.client.resources.ResourcesContainer.ListenComplete;
 import no.eirikb.h5z1.client.visualbody.VisualPlayer;
 
 import org.jbox2d.collision.AABB;
 import org.jbox2d.collision.CircleDef;
 import org.jbox2d.collision.Shape;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.BodyDef;
 import org.jbox2d.dynamics.ContactFilter;
 import org.jbox2d.dynamics.World;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.RootPanel;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class H5Z1 implements EntryPoint, KeyHackCallback {
 
 	private final int WIDTH = 640;
 	private final int HEIGHT = 480;
 	private World world;
 	private AABB aabb;
 	private GameMap gameMap;
 	private KeyHack keyHack;
 	private MapBuilder mapBuilder;
 	private float way = 0;
 	private boolean jump = false;
 	private FpsTimer fpsTimer;
 	private VisualPlayer me;
 	private int fire = 0;
 	private boolean shooting;
 	private boolean reloading = false;
 	private int mouseX;
 	private int mouseY;
 	private List<Image> ammo;
 	private int ammopos = 0;
 	private int reload;
 
 	private final int SHOOTSPEED = 5;
 	private final int RELOADSPEED = 5;
 
 	public void onModuleLoad() {
 
 		ResourcesContainer.init(new ListenComplete() {
 
 			@Override
 			public void onComplete(Map<String, ImageElement> images) {
 				start();
 			}
 		});
 	}
 
 	private void start() {
 		FocusPanel fp = new FocusPanel();
 		ammo = new ArrayList<Image>();
 		for (int i = 0; i < 20; i++) {
 			ammo.add(new Image(Resources.INSTANCE.bullet1()));
 		}
 		aabb = new AABB();
 		aabb.lowerBound = new Vec2(-200.0f, -100.0f);
 		aabb.upperBound = new Vec2(200.0f, 200.0f);
 		Vec2 gravity = new Vec2(0.0f, -50.0f);
 		boolean doSleep = true;
 		world = new World(aabb, gravity, doSleep);
 		world.setContactFilter(new ContactFilter() {
 
 			@Override
 			public boolean shouldCollide(Shape shape1, Shape shape2) {
 				if (shape1.m_body instanceof VisualPlayer
 						|| shape2.m_body instanceof VisualPlayer) {
 					me.setJumping(false);
 				}
 				return true;
 			}
 		});
 
 		final TestSettings settings = new TestSettings();
 		mapBuilder = new MapBuilder();
 		mapBuilder.createMap(world);
 
 		gameMap = new GameMap(world, mapBuilder, 10000, HEIGHT, WIDTH, HEIGHT);
 		final float timeStep = settings.hz > 0.0f ? 1.0f / settings.hz : 0.0f;
 		fpsTimer = new FpsTimer() {
 
 			@Override
 			public void update() {
 				if (fire > 0) {
 					fire--;
 				}
 				if (shooting && fire == 0) {
 					fire = SHOOTSPEED;
 
 					RootPanel.get().remove(ammo.get(ammopos++));
 					CircleDef cd = new CircleDef();
 					cd.radius = 0.1f;
 					cd.density = 1;
 					BodyDef bd = new BodyDef();
 					bd.isBullet = true;
 					double cosin[] = me.cosin(mouseX, mouseY);
 					bd.position.set(me.getPosition().x + (float) cosin[0] * 1,
 							me.getPosition().y);
 					Body b = world.createBody(bd);
 					b.createShape(cd);
 					b.setMassFromShapes();
 					b.setLinearVelocity(new Vec2((float) (cosin[0] * 100),
 							(float) (-cosin[1] * 100)));
 					me.setShowFlame(5);
 					if (ammopos >= ammo.size()) {
 						shooting = false;
 						reloading = true;
 						reload = RELOADSPEED;
 					}
 				}
 
 				if (reloading) {
 					if (--reload <= 0) {
 						if (--ammopos > 0) {
 							RootPanel.get().add(ammo.get(ammopos));
 						} else {
 							reloading = false;
 						}
 						reload = RELOADSPEED;
 					}
 
 				}
 
 				long box2DTime = System.currentTimeMillis();
 				world.step(timeStep, settings.iterationCount);
 				box2DTime = System.currentTimeMillis() - box2DTime;
 				gameMap.draw(box2DTime, (int) getFps());
 				keyHack.callback();
 				if (me != null) {
 					gameMap.setCamera(me.getPosition().x, 2, 20);
 					float y = jump ? 15 : me.getLinearVelocity().y;
 					jump = false;
 					me.setLinearVelocity(new Vec2(way, y));
 				}
 			}
 		};
 		fpsTimer.start();
 
 		keyHack = new KeyHack(this);
 
 		gameMap.addKeyDownHandler(new KeyDownHandler() {
 
 			@Override
 			public void onKeyDown(KeyDownEvent event) {
 				event.preventDefault();
 				event.stopPropagation();
 				if (keyHack != null) {
 					keyHack.keyDown(event);
 				} else {
 					keyDown(event);
 				}
 			}
 		});
 
 		gameMap.addKeyUpHandler(new KeyUpHandler() {
 
 			public void onKeyUp(KeyUpEvent event) {
 				if (keyHack != null) {
 					keyHack.keyUp(event);
 				} else {
 					keyUp(event.getNativeKeyCode());
 				}
 			}
 		});
 
 		gameMap.addMouseMoveHandler(new MouseMoveHandler() {
 
 			@Override
 			public void onMouseMove(MouseMoveEvent event) {
 				if (me != null) {
					mouseX = event.getRelativeX(gameMap.getElement()) - 100;
					mouseY = event.getRelativeY(gameMap.getElement()) + 100;
 					me.onMouse(mouseX, mouseY);
 				}
 			}
 		});
 
 		gameMap.addMouseDownHandler(new MouseDownHandler() {
 
 			@Override
 			public void onMouseDown(MouseDownEvent event) {
 				if (!reloading) {
 					shooting = true;
 				}
 			}
 		});
 
 		gameMap.addMouseUpHandler(new MouseUpHandler() {
 
 			@Override
 			public void onMouseUp(MouseUpEvent event) {
 				shooting = false;
 			}
 		});
 
 		RootPanel.get().add(new Button("Stop", new ClickHandler() {
 
 			@Override
 			public void onClick(ClickEvent event) {
 				fpsTimer.cancel();
 			}
 		}));
 		RootPanel.get().add(gameMap);
 		me = mapBuilder.getMe();
 		RootPanel.get().add(new Image(Resources.INSTANCE.gun1()));
 		for (Image bullet : ammo) {
 			RootPanel.get().add(bullet);
 		}
 	}
 
 	@Override
 	public void keyDown(KeyDownEvent event) {
 		if (event.isRightArrow() || event.getNativeKeyCode() == 68) {
 			way = 10;
 			me.walkRight();
 		} else if (event.isLeftArrow() || event.getNativeKeyCode() == 65) {
 			way = -10;
 			me.walkLeft();
 		} else if (event.getNativeKeyCode() == 87) {
 			if (!me.isJumping()) {
 				jump = true;
 				me.setJumping(true);
 			}
 		} else if (event.getNativeKeyCode() == 82) {
 			shooting = false;
 			reloading = true;
 			reload = RELOADSPEED;
 		} else if (event.getNativeKeyCode() == 32) {
 			shooting = true;
 		}
 	}
 
 	@Override
 	public void keyUp(int keyCode) {
 		if (keyCode == 68 || keyCode == 65) {
 			way = 0;
 			me.stopWalk();
 		} else if (keyCode == 32) {
 			shooting = false;
 		}
 	}
 }
