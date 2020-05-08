 package scripting;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import components.RenderingComp;
 import components.TextRenderingComp;
 
 import utils.ButtonState;
 import utils.MouseButton;
 import utils.MouseState;
 
 import content.ContentManager;
 
 import misc.IEventListener;
 
 import graphics.Color;
 import graphics.Texture2D;
 import graphics.TextureFont;
 import GUI.ButtonEventArgs;
 import anotations.Editable;
 
 @Editable
 public class ButtonBehavior extends Behavior {
 
 	private static final String DEF_BACKGROUND_TEXTURE = "GUI/ButtonBackground.png";
 	private static final String DEF_NORMAL_TEXTURE 	   = "GUI/NormalButton.png";
 	private static final String DEF_OVER_TEXTURE       = "GUI/OverButton.png";
 	private static final String DEF_DOWN_TEXTUE  	   = "GUI/DownButton.png";	
 	private static final String DEF_FONT		   	   = "GUI/StandardButton.font";
 
 	private boolean pressed = false;
 	
 	private @Editable Texture2D backgroudTexture;
 	private @Editable Texture2D normalTexture;
 	private @Editable Texture2D overTexture;
 	private @Editable Texture2D downTexture;
 			
 	private Set<IEventListener<ButtonEventArgs>> listeners;
 	
 	private RenderingComp renderingComp;
 	private TextRenderingComp textComp;
 	
 	/**Creates a new default button-behavior.
 	 */
 	public ButtonBehavior() {
 		this(null,null,null, null);
 	}
 	
 	/**Creates a specialized button with customized images
 	 * 
 	 * @param normalTexture the image shown when the button is not 
 	 * @param overTexture
 	 * @param downTexture
 	 */
 	public ButtonBehavior(Texture2D normalTexture, Texture2D overTexture,
 					      Texture2D downTexture, Texture2D backgroundTexture) {
 		this.normalTexture = normalTexture;
 		this.overTexture = overTexture;
 		this.downTexture = downTexture;
 		this.backgroudTexture = backgroundTexture;
 		listeners = new HashSet<>();
 	}
 	
 	public ButtonBehavior(ButtonBehavior other) {
 		this.normalTexture = other.normalTexture;
 		this.overTexture   = other.overTexture;
 		this.downTexture   = other.downTexture;
 		this.backgroudTexture = other.backgroudTexture;
 		listeners = new HashSet<>();
 	}
 	
 	@Override
 	public void start() {
 		if(this.normalTexture == null) {
 			this.normalTexture = ContentManager.loadTexture(DEF_NORMAL_TEXTURE);
 		}
 		if(this.overTexture == null) {
 			this.overTexture   = ContentManager.loadTexture(DEF_OVER_TEXTURE);
 		}
 		if(this.downTexture == null) {
 			this.downTexture   = ContentManager.loadTexture(DEF_DOWN_TEXTUE);
 		}
 		if(this.backgroudTexture == null) {
 			this.backgroudTexture = ContentManager.loadTexture(DEF_BACKGROUND_TEXTURE);
 		}
 		
 		
 		//These needs to be present for the button to work so if they are not here
 		//We create defaults.
 		this.renderingComp = this.getComponent(RenderingComp.class);
 		if(renderingComp == null) {
 			//Default button rendering component.
 			this.renderingComp = new RenderingComp(this.normalTexture, Color.White, null, null);
 			this.entity.addComponent(renderingComp);
 		}
 		
 		this.textComp = this.getComponent(TextRenderingComp.class);
 		if(this.textComp == null) {
 			TextureFont font = ContentManager.loadFont(DEF_FONT);
 			//Default button rendering text.
 			this.textComp = new TextRenderingComp("",font, Color.Black);
 			this.entity.addComponent(textComp);
 		}
 	}
 
 	@Override
 	public Object clone() {
 		return new ButtonBehavior(this);
 	}
 	
 	
 	@Override
 	public void onMouseDown(MouseState state, MouseButton button) {
 		if(button == MouseButton.Left) {
 			this.pressed = true;
 			this.setActiveTexture(downTexture);
 		}
 
 		System.out.println("button down " + button);
 	}
 	@Override
 	public void onMouseEnter(MouseState state){
 		if(!this.pressed) {
 			this.setActiveTexture(this.overTexture);
 		}
 	}
 
 	@Override
 	public void onMouseExit(MouseState state){
		if(!pressed) {
 			this.setActiveTexture(this.normalTexture);
		}
 	}	
 	@Override
 	public void onMouseUpAsButton(MouseState state, MouseButton button){
 		if(button == MouseButton.Left) {
 			this.onButtonPress();
 			this.setActiveTexture(overTexture);
 		}
 
 		System.out.println("button as button up " + button);
 	}
 	
 	@Override
 	public void onMouseUp(MouseState state, MouseButton button){
 		if(button == MouseButton.Left) {
 			pressed = false;
 			this.setActiveTexture(normalTexture);
 		}
 		System.out.println("button up " + button);
 	}
 	
 
 	private void setActiveTexture(Texture2D texture) {
 		this.renderingComp.setTexture(texture);
 		System.out.println("Setting active texture! "+  texture.OpenGLID);
 	}
 	
 	public void addEventListener(IEventListener<ButtonEventArgs> listener) {
 		this.listeners.add(listener);
 	}
 	
 	public void removeEventListener(IEventListener<ButtonEventArgs> listener) {
 		this.listeners.remove(listener);
 	}
 	
 	private void onButtonPress() {
 		System.out.println("Sending event!");
 		ButtonEventArgs args = 
 				new ButtonEventArgs(this.textComp.getText());	
 		for (IEventListener<ButtonEventArgs> listener : this.listeners) {
 			listener.onEvent(this.entity, args);
 		}
 	}
 }
