 package main;
 
 import java.util.List;
 import util.geom.Rectangle;
 import util.gl.Color;
 import util.input.MouseEvent;
 import util.ui.GLButton;
 import util.ui.GLButtonList;
 import util.ui.GLUIException;
 import util.ui.GLUITheme;
 
 public class RightClickMenu {
 
 	private int w = 150, h = 0;
 	private int buttonHeight = 20;
 	private int x, y;
 	
 	private Rectangle bounds;
 	
 	private GLButtonList list;
 	
 	private GLUITheme theme;
 	
 	private static Object context;
 	
 	public RightClickMenu() {
 		
 		this.bounds = new Rectangle(x, y, w, h);
 		
 		try {
 			theme = new GLUITheme();
 			
 			theme.setBackgroundColor(new Color(0, 0, 0, 0.5));
 			theme.setBackgroundHoverColor(new Color(255, 255, 255, 0.5));
 			theme.setBackgroundClickedColor(new Color(0, 0, 0, 0.5));
 
 			theme.setTextColor(new Color(255, 255, 255));
 			theme.setTextHoverColor(new Color(0, 0, 0));
 			theme.setTextClickedColor(new Color(255, 255, 255));
 			
 		} catch (GLUIException e) { }
 		
 		list = new GLButtonList(theme, x, y, w, buttonHeight, h);
 		list.hideSlider(true);
 	}
 	
 	public void setCoordinates(MouseEvent event) {
 		
 		x = event.getEventX();
 		y = event.getEventY();
 		
 		if (x > Main.SCREEN_PLAYABLE_WIDTH-w) {
 			x = Main.SCREEN_PLAYABLE_WIDTH-w;
 		}
 		if (y > Main.SCREEN_PLAYABLE_HEIGHT-h) {
 			y = Main.SCREEN_PLAYABLE_HEIGHT-h;
 		}
 		
 		setBounds(x, y, w, h);
 		
 		list.setCoordinates(x, y);
 	}
 	
 	public void renderGL() {
 		
		Main.BLANK_TEXTURE.bind();
		
 		list.renderGL();
 	}
 	
 	public void update(int delta) {
 		
 		list.update(delta);
 	}
 	
 	public void processMouseEvents(List<MouseEvent> events) {
 		
 		list.processMouseEvents(events);
 	}
 	
 	private void setBounds(int x, int y, int w, int h) {
 		
 		this.bounds.setH(h);
 		this.bounds.setW(w);
 		this.bounds.setX(x);
 		this.bounds.setY(y);
 	}
 	
 	public Rectangle getBounds() {
 		
 		return bounds;
 	}
 	
 	public void addButtons(List<GLButton> buttons) {
 		
 		for (GLButton b : buttons) {
 			list.add(b);
 		}
 		
 		h += buttons.size()*buttonHeight;
 		updateWidth();
 	}
 	
 	public void addButton(GLButton button) {
 		
 		button.setTheme(theme);
 		list.add(button);
 		
 		h+= buttonHeight;
 		
 		updateWidth();
 	}
 	
 	private void updateWidth() {
 		
 		for (GLButton b : list.getButtons()) {
 			int next = theme.getFont().getStringWidth(b.getLabel());
 			w = (next > w) ? next+10 : w;
 		}
 	}
 	
 	public void setContext(Object o) {
 		context = o;
 	}
 	
 	public static Object getContext() {
 		return context;
 	}
 }
