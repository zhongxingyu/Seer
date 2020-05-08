 package game.util.UI;
 
 import game.util.IO.InputState;
 import game.util.IO.Key;
 import game.util.IO.Event.Event;
 import game.util.IO.Event.EventListner;
 import game.util.IO.Event.KeyEvent;
 
 import java.awt.Rectangle;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SpriteSheet;
 
 
 public class NewTextBox extends BasicUIComponent{
 	private EventListner keyDown, keyPress, keyUp, mouseOver, mouseLeave;
 	private Image graphic, normalEmpty, normalText, overEmpty, overText;
 	private StringBuilder sb; 
 	private Font font; 
 	private int pos;
	private int charCounter = 0; 
 	private int margin = 5;
 	private boolean renderStars; 
 	
 	public NewTextBox(SpriteSheet ss) {
 		graphic = ss.getSprite(0, 0);
 		normalEmpty = ss.getSprite(0, 0);
 		normalText = ss.getSprite(0, 2);
 		overEmpty = ss.getSprite(0, 1);
 		overText = ss.getSprite(0, 3);
 		font = null;
 		pos = 0;
 		renderStars = false; 
 		sb = new StringBuilder();
 		setupEventListners();
 		setLocksFocus(true);
 	}
 	
 	public NewTextBox(Image i) {
 		graphic = i; 
 		normalEmpty = i; 
 		normalText = i; 
 		overEmpty = i; 
		overText = i;
 		pos = 0;
 		sb = new StringBuilder();
 		font = null;
 		setupEventListners();
 		setLocksFocus(true);
 	}
 	
 	public void setRenderStar() {
 		renderStars = true; 
 	}
 	
 	/**
 	 * Returns the string built by the StringBuilder. 
 	 * 
 	 * @return String
 	 */
 	public String getText() { 
 		return sb.toString(); 
 		}
 
 	private long lastBack = 0;
 	private int countBack = 0;
 	
 	/**
 	 * Setting up the event listeners. 
 	 */
 	private void setupEventListners() {
 		super.addKeyDownEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				Key key = ((KeyEvent)e).Key;
 				try {
 					if (key.VALUE == Input.KEY_ESCAPE) {
 						removeFocus();
 					} else if (InputState.Get().KeyboardState.GetKeyState(Input.KEY_LSHIFT).Down()) {
 						if (key.ALTCHARACTER != null) {
 							sb.insert(pos++, key.ALTCHARACTER);
							charCounter++; 
 						}
 						else if (key.CHARACTER != null) {
 							sb.insert(pos++, key.CHARACTER);
							charCounter++; 
 						}
 					} else {
						if (key.CHARACTER != null) {
 							sb.insert(pos++, key.CHARACTER);
							charCounter++; 
						}
 					}
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 				if (keyDown != null)
 					keyDown.Invoke(sender, e);
 			}
 		});
 		
 		super.addKeyUpEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				Key key = ((KeyEvent)e).Key;
 				if (key.VALUE == Input.KEY_BACK) {
 					countBack = 0;
 				}
 			}
 		});
 		
 		super.addKeyPressEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				Key key = ((KeyEvent)e).Key;
 				if (key.VALUE == Input.KEY_BACK) {
 					if (pos > 0) {
 						try {
 							if (InputState.Get().Time - lastBack > 250 ||
 									(InputState.Get().Time - lastBack > 50) && countBack > 3) {
 								countBack++;
 								lastBack = InputState.Get().Time;
 								sb.deleteCharAt(--pos);
								charCounter--;
 							}
 						} catch (Exception e1) {
 							e1.printStackTrace();
 						}
 					}
 				} 
 				if (keyPress != null)
 					keyPress.Invoke(sender, e);
 			}
 		});
 		
 		super.addMouseLeaveEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if (!hasFocus()) {
 					if (sb.length() > 0) {
 						graphic = normalText;
 					} else {
 						graphic = normalEmpty;
 					}
 				}
 			}
 		});
 
 		// Over
 		super.addMouseOverEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if (!hasFocus()) {
 					if (sb.length() > 0) {
 						graphic = overText;
 					} else {
 						graphic = overEmpty;
 					}
 				}
 				if (mouseOver != null)
 					mouseOver.Invoke(sender, e);
 			}
 		});
 		super.addLostFocusEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if (sb.length() == 0) {
 					graphic = normalEmpty;
 				}
 			}
 		});
 		super.addGainedFocusEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = normalText;
 			}
 		});
 	}
 
 	String tempString = "";
 	
 	@Override
 	public void render(GameContainer container, Graphics g) {
 		Font f = g.getFont();
 		if (font != null)
 			f = font;
 		Rectangle rec = getRectangle();
 		g.drawImage(graphic, rec.x, rec.y, rec.x + rec.width, rec.y +rec.height,
 				0, 0, graphic.getWidth(), graphic.getHeight());
 		String s = "";
 		if (renderStars) {
 			for (int i = 0; i < sb.length(); i++)
 				s += "*";
 		} else {
 			s = sb.toString();
 		}
 		int w = f.getWidth(s); 
 		int charWidth = f.getWidth("a");
 		if (rec.height - 2 * margin >= f.getLineHeight()) {
 			if(w > graphic.getWidth() - 3*margin) {
				tempString = s.substring(charCounter - (int)Math.floor((graphic.getWidth() + 2*margin)/charWidth), s.length()); 
 				g.drawString(tempString, rec.x + 2*margin, rec.y + margin);
 			} else {
 				g.drawString(s, rec.x + 2*margin, rec.y + margin);
 			}
 		}
 	}
 	
 	@Override
 	public void addMouseOverEventListner(EventListner delegate) {
 		if (mouseOver == null)
 			mouseOver = delegate;
 	}
 	@Override
 	public void addMouseLeaveEventListner(EventListner delegate) {
 		if (mouseLeave == null)
 			mouseLeave = delegate;
 	}
 	
 	@Override
 	public void addKeyDownEventListner(EventListner delegate) {
 		if (keyDown == null)
 			keyDown = delegate;
 	}
 	@Override
 	public void addKeyPressEventListner(EventListner delegate) {
 		if (keyPress == null)
 			keyPress = delegate;
 	}
 	@Override
 	public void addKeyUpEventListner(EventListner delegate) {
 		if (keyUp == null)
 			keyUp = delegate;
 	}
 }
 
