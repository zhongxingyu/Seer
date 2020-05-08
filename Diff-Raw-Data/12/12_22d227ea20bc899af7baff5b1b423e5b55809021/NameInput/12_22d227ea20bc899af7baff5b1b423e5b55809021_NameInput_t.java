 package sitsa.aqado;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.*;
 import org.newdawn.slick.gui.GUIContext;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import sitsa.aqado.GUI.AbstractButton;
 
 import java.awt.*;
 import java.awt.Font;
 
 //TODO get player name input
 public class NameInput extends BasicGameState {
 	
 	private int state;
     private Image bg;
     private Button returnButton;
     private TrueTypeFont font;
     private TextField p1Input, p2Input;
 
 	public NameInput(int state) {
 		this.state = state;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game) throws SlickException {
 		bg = new Image("tex/bg300x400.png");
        	returnButton = new Button(245, 300, 200, 60, Color.cyan);
 		Font awtFont = new java.awt.Font("Arial", java.awt.Font.BOLD, 16);
        	font = new TrueTypeFont(awtFont, false);
		p1Input = new TextField(container, font, 30, 500, 300, 50);
        	p2Input = new TextField(container, font, 360, 500, 300, 50);
        	font.loadGlyphs();
 
     }
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics g) throws SlickException {
         bg.draw(-600, -800);
         returnButton.draw(g);
         font.drawString(320, 320, "Return", Color.black);
         font.drawString(30, 480, "Player 1:", Color.black);
         font.drawString(360, 480, "Player 2:", Color.black);
         p1Input.render(gc, g);
         p2Input.render(gc, g);
     }
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
         // Button logic
         if(returnButton.isMouseOn(gc)){
             returnButton.setFillColor(Color.cyan.brighter());
         }else{
             returnButton.setFillColor(Color.cyan.darker());
         }
         if(returnButton.isMouseClicked(gc) && gameVariables.fromPause == false){
             game.enterState(1);
         }
         if(returnButton.isMouseClicked(gc) && gameVariables.fromPause == true){
             game.enterState(5);
             gameVariables.fromPause = false;
         }
 
         // Input logic
         gameVariables.p1Name = p1Input.getText();
         gameVariables.p2Name = p2Input.getText();
         font.loadGlyphs();
 	}
 
 	@Override
 	public int getID() {
 		return this.state;
 	}
 
 
     private static class Button extends AbstractButton {
         public Button(float x, float y, float width, float height, Color fillColor){
             super(x, y, width, height, fillColor);
         }
 
         @Override
         public void draw(Graphics g) {
             g.setLineWidth(3);
             g.setColor(fillColor);
             g.fillRoundRect(x, y, width, height, 15);
             g.setColor(Color.black);
             g.drawRoundRect(x, y, width, height, 15);
         }
 
         public void update(int delta) {
         }
     }
 }
