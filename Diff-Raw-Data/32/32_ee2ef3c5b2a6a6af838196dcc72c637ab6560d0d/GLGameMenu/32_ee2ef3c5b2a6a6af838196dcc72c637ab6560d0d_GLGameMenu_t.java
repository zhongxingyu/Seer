 package riskyspace.view.opengl.menu;
 
 import java.awt.Point;
 import java.awt.Toolkit;
 
 import javax.media.opengl.GLAutoDrawable;
 
 import riskyspace.data.Settings;
 import riskyspace.services.Event;
 import riskyspace.services.Event.EventTag;
 import riskyspace.services.EventBus;
 import riskyspace.view.Action;
 import riskyspace.view.Clickable;
 import riskyspace.view.IMenu;
 import riskyspace.view.lobby.LobbyView;
 import riskyspace.view.opengl.GLRenderAble;
 import riskyspace.view.opengl.Rectangle;
 import riskyspace.view.opengl.impl.GLButton;
 import riskyspace.view.opengl.impl.GLSprite;
 
 /**
  * Game Menu
  * @author Alexander Hederstaf
  *
  */
 public class GLGameMenu implements IMenu, Clickable, GLRenderAble {
 
 	private Rectangle renderRect;
 	private Rectangle clickRect;
 	
 	private GLSprite background;
 	private boolean visible = false;
 	
 	private GLButton sound;
 	private GLButton save;
 	private GLButton lobby;
 	private GLButton exit;
 	
 	public GLGameMenu(int centerX, int centerY, int width, int height) {
 		clickRect = new Rectangle( centerX - width / 2,
 								   centerY - height / 2,
 								   width,
 								   height);
 		int sHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 		renderRect = new Rectangle(centerX - width / 2,
 				   				   sHeight - (centerY - height / 2) - height,
 				   				   width,
 				   				   height);
 		int w = width/10;
 		int h = height/13;
 		sound = new GLButton(centerX - (7*w)/2, centerY - 4*h - h / 2, 7*w, 2*h);
 		sound.setTexture("square_button", 128, 80);
		sound.setText("Sound " + (Settings.isMusicOn() ? "On" : "OFF"));
 		sound.setAction(new Action() {
 			@Override public void performAction() {
 				/*
 				 * Sound change
 				 */
 				Settings.setProperty(Settings.MUSIC_ENABLED + !Settings.isMusicOn());
 				sound.setText("Sound " + (Settings.isMusicOn() ? "On" : "OFF"));
 				EventBus.CLIENT.publish(new Event(Event.EventTag.SOUND, null));
 			}
 		});
 		save = new GLButton(centerX - (7*w)/2, centerY - 2*h - h / 6, 7*w, 2*h);
 		save.setTexture("square_button", 128, 80);
 		save.setText("Save");
 		save.setAction(new Action() {
 			@Override public void performAction() {
 				/*
 				 * Save game
 				 */
 				EventBus.CLIENT.publish(new Event(Event.EventTag.SAVE_GAME, null));
 				save.setText("Game Saved");
 				Thread t = new Thread(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							Thread.sleep(2000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						save.setText("Save");
 					}
 				});
 				t.start();
 			}
 		});
 		lobby = new GLButton(centerX - (7*w)/2, centerY + h / 6, 7*w, 2*h);
 		lobby.setTexture("square_button", 128, 80);
 		lobby.setText("Lobby");
 		lobby.setAction(new Action() {
 			@Override public void performAction() {
 				/*
 				 * Exit to lobby
 				 */
 				EventBus.CLIENT.publish(new Event(Event.EventTag.DISCONNECT, null));
 				new LobbyView();
 			}
 		});
 		exit = new GLButton(centerX - (7*w)/2, centerY + 2 * h + h / 2, 7*w, 2*h);
 		exit.setTexture("square_button", 128, 80);
 		exit.setText("Exit");
 		exit.setAction(new Action() {
 			@Override public void performAction() {
 				/*
 				 * Shut down
 				 */
 				EventBus.CLIENT.publish(new Event(Event.EventTag.DISCONNECT, null));
 				System.exit(0);
 			}
 		});
 		background = new GLSprite("square_button", 128, 80);
 	}
 	
 	@Override
 	public boolean contains(Point p) {
 		return clickRect.contains(p.x, p.y);
 	}
 
 	@Override
 	public boolean mousePressed(Point p) {
 		if (isVisible() && contains(p)) {
 			sound.mousePressed(p);
 			save.mousePressed(p);
 			lobby.mousePressed(p);
 			exit.mousePressed(p);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean mouseReleased(Point p) {
 		if (isVisible() && contains(p)) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void setVisible(boolean set) {
 		visible = set;
 	}
 
 	@Override
 	public boolean isVisible() {
 		return visible;
 	}
 
 	@Override
 	public Rectangle getBounds() {
 		return renderRect;
 	}
 
 	@Override
 	public void draw(GLAutoDrawable drawable, Rectangle objectRect, Rectangle targetArea, int zIndex) {
 		if (!visible) {
 			return;
 		}
 		background.draw(drawable, objectRect, targetArea, zIndex);
 		
 		sound.draw(drawable, null, targetArea, zIndex + 1);
 		save.draw(drawable, null, targetArea, zIndex + 1);
 		lobby.draw(drawable, null, targetArea, zIndex + 1);
 		exit.draw(drawable, null, targetArea, zIndex + 1);
 	}
 }
