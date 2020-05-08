 package chalmers.TDA367.B17.states;
 
 import java.io.*;
 import java.util.Scanner;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.view.Label;
 import chalmers.TDA367.B17.view.MenuButton;
 import chalmers.TDA367.B17.view.Slider;
 
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.*;
 
 import chalmers.TDA367.B17.Tansk;
 
 public class SettingsMenu extends BasicGameState{
 
 	private TextField nameField;
 	private Label inputLabel;
 	private int state;
 	private SpriteSheet background;
 	private MenuButton backButton;
 	private String playerName;
 	
 	private Slider slider;
 	
 	public SettingsMenu(int state) {
 		this.state = state;
 		playerName = "Unnamed";
 	}
 
 	public void init(GameContainer gc, StateBasedGame sbg)
 			throws SlickException {
 
 		slider = new Slider(100, 0, 35, new Vector2f(110, 200), gc, "Volume: ");
 		
 		inputLabel = new Label("Player name:", Color.black, 100, 225);
 		nameField = new TextField(gc, gc.getDefaultFont(), 100, 250, 200, 20);
 		
 		backButton = new MenuButton(100, 575, 
 				GameController.getInstance().getImageHandler().getSprite("button_back"),
 				GameController.getInstance().getImageHandler().getSprite("button_back_pressed"),
 				GameController.getInstance().getImageHandler().getSprite("button_back_hover"));
 
 		background = new SpriteSheet(GameController.getInstance().getImageHandler().getSprite("background"),
 				Tansk.SCREEN_WIDTH, Tansk.SCREEN_HEIGHT);
 		
 		
 		Scanner scanner = null;
 		try {
 			scanner = new Scanner(new File(Tansk.DATA_FOLDER + "/name.txt"));
 		} catch (FileNotFoundException e) {
 			System.out.println("Couldn't load name-file.");
 		}
 
 		if(scanner != null){     
 			playerName = scanner.nextLine(); 
 			scanner.close();
 		}
 
 		GameController.getInstance().setPlayerName(playerName);
 		nameField.setText(playerName);
 	}
 
 	@Override
 	public void enter(GameContainer gc, StateBasedGame stateBasedGame){
 		nameField.setCursorPos(100);
 	}
 
 	@Override
 	public void leave(GameContainer gc, StateBasedGame stateBasedGame){
 		nameField.setFocus(false);
 		playerName = nameField.getText();
 		if(playerName.length() > 10){
 			playerName = playerName.substring(0, 10);
 			nameField.setText(playerName);
 		}
 		
 		Writer writer = null;
 		try {
 		    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Tansk.DATA_FOLDER + "/name.txt"), "utf-8"));
 		    writer.write(playerName);
 		} catch (IOException ex){
 			System.out.println("Failed to save name.");
 		} finally {
 		   try {
 			   writer.close();
 		   } catch (Exception ex) {}
 		}
 		
 		GameController.getInstance().setPlayerName(playerName);
 	}
 
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
 			throws SlickException {
 		background.draw();
 		g.drawRect(nameField.getX(), nameField.getY(), nameField.getWidth(), nameField.getHeight());
 		nameField.render(gc, g);
 				
 		inputLabel.render(g);
 		backButton.draw();
 		slider.draw(g);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {				
		if(gc.getInput().isKeyPressed(Input.KEY_ESCAPE)){
 			sbg.enterState(Tansk.MENU);
 		}
 		if(backButton.isClicked(gc.getInput()))
 			sbg.enterState(Tansk.MENU);
 		slider.update();
 		GameController.getInstance().getSoundHandler().setVolume((float)slider.getValue()/100);
 	}
 
 	@Override
 	public void keyReleased(int key, char c) {
 		super.keyReleased(key, c);
 		if(key == Input.KEY_ENTER){
 			if(nameField.hasFocus()){
 				nameField.setFocus(false);
 			}
 		}
 	}
 
 	@Override
 	public int getID() {
 		return this.state;
 	}
 }
