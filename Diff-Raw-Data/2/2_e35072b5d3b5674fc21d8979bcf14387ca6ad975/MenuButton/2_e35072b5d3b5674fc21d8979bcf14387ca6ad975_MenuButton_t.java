 package game;
 
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.StateBasedGame;
 
 import components.Component;
 import components.ImageRenderComponent;
 
 public class MenuButton extends Entity{
 
 	private boolean mousePressed;
 	private Image image;
 	
 	
 	
 	public MenuButton(String id, Vector2f pos, Image image) {
 		super(id);
 		setPosition(pos);
 		this.image = image;
 		AddComponent(new ImageRenderComponent("button_image", image));
		//mousePressed = true;
 	}
 
 	
 	public void update(GameContainer gc, StateBasedGame sb, int delta) {
 		for (Component component : getComponents()) {
 			component.update(gc, sb, delta);
 		}
 		
 		int mousePosX = Mouse.getX();
 		int mousePosY = Math.abs(Mouse.getY() - Game.app.getHeight());
 		Input input = gc.getInput();
 		//isMousePressed(input);
 		
 		if ( mousePosX > getPosition().getX() && mousePosX < getPosition().getX() + image.getWidth() 
 				&& mousePosY > getPosition().getY() && mousePosY < getPosition().getY() + image.getHeight() ) { // cursor is inside button
 
 		
 			if (input.isMousePressed(0)) {
 				mousePressed = true;
 			} else {
 				mousePressed = false;
 			}
 		} 
 	}
 	
 	/*private void isMousePressed(Input input) {
 		
 		if ( mousePosX > getPosition().getX() && mousePosX < getPosition().getX() + image.getWidth() 
 				&& mousePosY > getPosition().getY() && mousePosY < getPosition().getY() + image.getHeight() ) { // cursor is inside button
 
 		
 			if (input.isMousePressed(0)) {
 				mousePressed = true;
 			} else {
 				mousePressed = false;
 			}
 		} 
 		
 		
 	}*/
 
 	public boolean isMousePressed() {
 		return mousePressed;
 	}
 }
