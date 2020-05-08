 package game.util.UI;
 
 import java.awt.Rectangle;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SpriteSheet;
 
import game.client.Resource.ResourceManager;
 import game.util.IO.Event.Event;
 import game.util.IO.Event.EventListner;
 import game.util.UI.BasicUIComponent;
 
 public class TexturedButton extends BasicUIComponent {
 	
 	private Color filter;
	private EventListner mouseOver, mouseDown, mouseUp, mouseLeave, onDisable, onEnable;	
 	private Image graphic, normal, over, down, disabled; 
 	
 	public TexturedButton(SpriteSheet ss) {
		//int i = ss.getHorizontalCount(); //TODO (never) lol, used later
 		graphic = ss.getSprite(0, 0);
 		normal = ss.getSprite(0, 0);
 		over = ss.getSprite(0, 1);
 		down = ss.getSprite(0, 2);
 		disabled = ss.getSprite(0, 3);
 		setupEventListners();
 	}
 	
	public TexturedButton(String s) { 
		this(ResourceManager.Manager().getImage(s));
	}
 	
 	public TexturedButton(Image i) {
 		graphic = i; 
 		normal = i; 
 		over = i; 
 		down = i; 
 		disabled = i;
 		setupEventListners();
 	}
 	
 	public void setGraphic(Image i){
 		graphic = i;
 	}
 	
 	public Image getGraphic(){
 		return graphic; 
 	}
 	
 	public void setNormal(Image i){
 		normal = i;
 	}
 	
 	public Image getNormal(){
 		return normal; 
 	}
 	
 	public void setOver(Image i){
 		over = i;
 	}
 	
 	public Image getover(){
 		return over; 
 	}
 	
 	public void setDown(Image i){
 		down = i;
 	}
 	
 	public Image getdown(){
 		return down; 
 	}
 	
 	public void setDisabled(Image i){
 		disabled = i;
 	}
 	
 	public Image getdisabled(){
 		return disabled; 
 	}
 	
 	
 	private void setupEventListners() {
 		
 		// On disabled
 		super.addOnDisabledListner(new EventListner() {
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = disabled; 
 				if (onDisable != null)
 					onDisable.Invoke(sender, e);
 			}
 		});
 		
 		// On enabled
 		super.addOnEnabledListner(new EventListner() {
 			@Override
 			public void Invoke(Object sender, Event e) {
 				graphic = normal; 
 				if (onEnable != null)
 					onEnable.Invoke(sender, e);
 			}
 		});
 		
 		// Over
 		super.addMouseOverEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if(isEnabled()){
 					if (graphic != down)
 						graphic = over;
 					if (mouseOver != null)
 						mouseOver.Invoke(sender, e);
 				}
 			}
 		});
 		
 		// Down
 		super.addMouseDownEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if(isEnabled()) {
 					graphic = down;
 					if (mouseDown != null)
 						mouseDown.Invoke(sender, e);
 				}
 			}
 		});
 		
 		// Up
 		super.addMouseUpEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if(isEnabled()) {
 					graphic = over;
 					if (mouseUp != null)
 						mouseUp.Invoke(sender, e);
 				}
 			}
 		});
 		
 		// leave
 		super.addMouseLeaveEventListner(new EventListner() {
 			
 			@Override
 			public void Invoke(Object sender, Event e) {
 				if(isEnabled()) {
 					graphic = normal;
 					if (mouseLeave != null)
 						mouseLeave.Invoke(sender, e);
 				}
 			}
 		});
 	}
 
 	@Override
 	public void addMouseOverEventListner(EventListner delegate) {
 		if (mouseOver == null)
 			mouseOver = delegate;
 	}
 	@Override
 	public void addMouseUpEventListner(EventListner delegate) {
 		if (mouseDown == null)
 			mouseDown = delegate;
 	}
 	@Override
 	public void addMouseDownEventListner(EventListner delegate) {
 		if (mouseUp == null)
 			mouseUp = delegate;
 	}
 	@Override
 	public void addMouseLeaveEventListner(EventListner delegate) {
 		if (mouseLeave == null)
 			mouseLeave = delegate;
 	}
 	
 	@Override
 	public void addOnEnabledListner(EventListner delegate) {
 		if (onEnable == null)
 			onEnable = delegate;
 	}
 	
 	@Override
 	public void addOnDisabledListner(EventListner delegate) {
 		if (onDisable == null)
 			onDisable = delegate;
 	}
 	
 	@Override
 	public void render(GameContainer container, Graphics g) {
 		if (graphic == null) return;
 		Rectangle rec = getRectangle();
 		if (filter == null) {
 			g.drawImage(graphic, rec.x, rec.y, rec.x + rec.width, rec.y +rec.height,
 					0, 0, graphic.getWidth(), graphic.getHeight());
 		} else {
 			g.drawImage(graphic, rec.x, rec.y, rec.x + rec.width, rec.y +rec.height,
 					0, 0, graphic.getWidth(), graphic.getHeight(), filter);
 		}
 	}
 }
