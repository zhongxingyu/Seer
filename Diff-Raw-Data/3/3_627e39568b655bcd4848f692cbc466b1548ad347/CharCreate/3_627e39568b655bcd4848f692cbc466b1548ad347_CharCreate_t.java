 //package DD.GUI;
 //
 //import java.util.ArrayList;
 //
 //import org.newdawn.slick.geom.Vector2f;
 //import org.newdawn.slick.gui.TextField;
 //
 //import org.lwjgl.input.Mouse;
 //import org.newdawn.slick.*;
 //import org.newdawn.slick.state.*;
 //
 //import java.awt.Font;
 //import org.newdawn.slick.GameContainer;
 //import org.newdawn.slick.Graphics;
 //import org.newdawn.slick.Input;
 //import org.newdawn.slick.SlickException;
 //import org.newdawn.slick.UnicodeFont;
 //import org.newdawn.slick.font2.effects.ColorEffect;
 //import org.newdawn.slick.gui.ComponentListener;
 //import org.newdawn.slick.state.BasicGameState;
 //import org.newdawn.slick.state.StateBasedGame;
 //
 //
 //import DD.Character.CharacterSheet.CharacterSheet;
 //
 //
 //
 //
 //public class CharCreate extends BasicGameState {
 //	CharacterSheet sheet;
 //	UnicodeFont font2;
 //	private String mouse = "No input yet!";
 //	Input inputMouse = new Input(650);
 //	TextField playerName;
 //	//TextField characterName;
 //	//clickable button for choosing race
 ////	TextField languages;
 ////	//clickable button for choosing size
 ////	//clickable button for choosing gender
 ////	TextField height;
 ////	TextField weight;
 ////	TextField age;
 ////	TextField alignments;
 ////	TextField deity;
 ////	TextField background;
 ////	TextField occupation;
 //	JoinLobbyStartButton jlsb;
 //	
 //	private Image bg_image = null;
 //	
 //	//clickable button to choose which class to pick
 //	
 //	
 //	
 //	//AFTER FILLING BASIC INFO WE WILL THEN NEED TO PASS TO A NEW STATE
 //	/*
 //	 * (String name,String player,int race,String languages, int size,
 //						  int gender,int height,int weight,int age,String alignments,
 //						  String deity,String background,String occupation)
 //	*/
 //
 //
 //	
 //	public CharCreate(int x)
 //	{
 //		
 //	}
 //	
 //	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException
 //	{
 //		
 //		
 //		bg_image = new Image ("Images/Menus/menuscreen4.jpg");
 //		font2 = getNewFont("Arial" , 16);
 //		
 //		playerName = new TextField(gc, font2, 100, 230, 180, 25);
 //		playerName.setText("username");
 //		
 //		
 //		
 //		
 //		font2.loadGlyphs();
 //		
 //	}
 //	
 //	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)throws SlickException
 //	{
 //		bg_image.draw(0,0);
 //		g.drawString(mouse, 100, 400);
 //		g.drawString("inputMouse x " + inputMouse.getMouseX() + " y " + inputMouse.getMouseY(), 100, 420);
 //		g.drawString("BACK", 190, 552);
 //		g.setFont(font2);
 //		
 //		
 //		playerName.render(gc, g);
 //	
 //	}
 //	
 //	public void update(GameContainer gc, StateBasedGame sbg,int delta)throws SlickException
 //	{
 //		int posX = Mouse.getX();
 //		int posY = Mouse.getY();
 //		font2.loadGlyphs();
 //				
 //		 mouse = "Mouse position x: " + posX + " y: " + posY;
 //		
 //		//TextField tx = new TextField(gc, 14, 100,100, 10,10, mouseButtonDown(0));
 //		
 //	}
 //	
 //	public UnicodeFont getNewFont(String fontName , int fontSize)
 //    {
 //        font2 = new UnicodeFont(new Font(fontName , Font.PLAIN , fontSize));
 ////        font2.addGlyphs("@");
 //        font2.getEffects().add(new ColorEffect(java.awt.Color.white));
 //        return (font2);
 //    }
 //
 //	@Override
 //	public int getID() {
 //		return 6;
 //	}
 //	
 //	
 //	//this is what the button should do then move to the next
 //	public void setCharacterSheet(TextField username)
 //	{
 //		
 //		//fix with buttons
 //		//sheet.fillBasic(characterName.getText(), playerName.getText(), race, languages.getText(), size, gender, 
 //			//			height.getText(), weight.getText(), age.getText(), alignments.getText(), deity.getText(), 
 //				//		background.getText(), occupation.getText());
 //		
 //		//sheet.fillAbilities();
 //		
 //		//get class option button value
 //		
 //		
 //		//sheet.fillRecorder(sheet.chooseClass(class button value here)/*This needs the class option from button */);
 //		
 //		//sheet.fillAttacksAndDefense(/* the class option*/);
 //		
 //		//after this we need to probably make an equipment purchase thing to buy armor to equip and weapons
 //	
 //	}
 //	
 //	
 //	/*
 //	
 //	public TextField (GUIContext gc, Font font2,  int x, int y, int width, int height, ComponentListener listener)
 //	{
 //		
 //	}
 //	*/
 //
 //	
 //
 //}
 
 
 
 
 
 package DD.GUI;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.gui.TextField;
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.*;
 import java.awt.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import DD.Character.CharacterSheet.CharacterSheet;
 
 public class CharCreate extends BasicGameState
 {
 	
 	CharacterSheet sheet;
 	Image screen;
 	private String mouse = "No input yet!";
 	//Image play;
 	Sound button;
 	//Music dungeon;
 	UnicodeFont font2;
 	TextField textbox;
 	ArrayList<Lobby> lobbyList = new ArrayList<Lobby>();
 	TextField text2;
 	TextField text1;
 	TextField text3;
 	TextField playerName;
 	TextField characterName;
 	//clickable button for choosing race
 	TextField languages;
 	//clickable button for choosing size
 	//clickable button for choosing gender
 	TextField height;
 	TextField weight;
 	TextField age;
 	TextField alignments;
 	TextField deity;
 	TextField background;
 	TextField occupation;
 	JoinLobbyStartButton jlsb;
 	Input inputMouse = new Input(650);
 	
 	public CharCreate(int state)
 	{
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException
 	{
 		screen = new Image("Images/Menus/DD1.jpg");
 		
 		
 		//play = new Image("Images/Menus/play button.png");
 		//options = new Image("res/options button.png");
 		//font2 = new TrueTypeFont(new java.awt.Font(java.awt.Font.SERIF,java.awt.Font.BOLD,8),false);
 		font2 = getNewFont("Arial" , 16);
 		button = new Sound("Audio/dunSound.wav");
 		//dungeon = new Music("Audio/dunEffect1.wav");
 		//dungeon.loop();
 //		textbox = new TextField(gc, font2, 100, 266, 250 , 50);
 		
 //		jlsb = new JoinLobbyStartButton(username, ip, Game.system, new Vector2f(190, 600));
 //		textbox.addListener(new KeyListener());
 		
 		font2.loadGlyphs();
 		
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException
 	{
 		playerName = new TextField(gc, font2, 100, 100, 180, 25);
 		characterName = new TextField(gc, font2, 100, 130, 180, 25);
 		languages = new TextField(gc, font2, 100, 160, 180, 25);
 		height = new TextField(gc, font2, 100, 190, 180, 25);
 		weight = new TextField(gc, font2, 100, 220, 180, 25);
 		age = new TextField(gc, font2, 100, 250, 180, 25);
 		alignments = new TextField(gc, font2, 100, 280, 180, 25);
 		deity = new TextField(gc, font2, 100, 310, 180, 25);
 		background = new TextField(gc, font2, 100, 340, 180, 25);
 		occupation = new TextField(gc, font2, 100, 370, 180, 25);
 		
 		
 		playerName.setText("Your Name");
 		characterName.setText("Character Name");
 		languages.setText("Languages you Speak");
 		height.setText("How Tall are you");
 		weight.setText("How much do you weigh?");
 		age.setText("How Old are you");
 		alignments.setText("What Alignments");
 		deity.setText("What Deity");
 		background.setText("Your Background");
 		occupation.setText("Your Occupation");
 		
 		
 		
 		
 	}
 	
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)throws SlickException
 	{
 		screen.draw(0,0);
 		//play.draw(100,100);
 		//options.draw(100, 200);
 		//about.draw(100, 300);
 		g.drawString(mouse, 100, 400);
 		g.drawString("inputMouse x " + inputMouse.getMouseX() + " y " + inputMouse.getMouseY(), 100, 420);
 //		g.drawString("LobbyIP: 123.43.345", 82, 266);
 //		g.drawString("LOGIN", 190, 604);
 		g.drawString("BACK", 190, 552);
 //		jlsb.render(gc, sbg, g);
 		g.setFont(font2);
 		
 		
 //		textbox.render(gc, g);
 		if(playerName != null) playerName.render(gc, g);
 		if(characterName != null) characterName.render(gc, g);
 		if(languages != null) languages.render(gc, g);
 		if(height != null) height.render(gc, g);
 		if(weight != null) weight.render(gc, g);
 		if(age != null) age.render(gc, g);
 		if(alignments != null) alignments.render(gc, g);
 		if(deity != null) deity.render(gc, g);
 		if(background != null) background.render(gc, g);
 		if(occupation != null) occupation.render(gc, g);
 		
 		g.setFont(font2);
 //		textbox.setCursorVisible(true);
 		
 		
 		
 	}
 	
 	
 	public void update(GameContainer gc, StateBasedGame sbg,int delta)throws SlickException
 	{
 		int posX = Mouse.getX();
 		int posY = Mouse.getY();
 		font2.loadGlyphs();
 		//BACK button
 //		jlsb.update(gc, sbg, delta);
 		if((posX > 185 && posX < 240) && (posY > 80 && posY < 100))
 		{
 			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 			{
 				button.play();
 				sbg.enterState(0);
 				/* kill the overlap */
 				characterName.setLocation(2000, 2000);
 				playerName.setLocation(2000,2000);
 				languages.setLocation(2000,2000);
 				height.setLocation(2000,2000);
 				weight.setLocation(2000,2000);
 				age.setLocation(2000,2000);
 				alignments.setLocation(2000,2000);
 				deity.setLocation(2000,2000);
 				background.setLocation(2000,2000);
 				occupation.setLocation(2000,2000);
 
 			}
 		}
 		
 		//System.out.println(username.getText());
 		//LOGIN button
 //		if((posX > 185 && posX < 247) && (posY > 28 && posY < 47))
 //		{
 //			if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 //			{
 //				button.play();
 //				sbg.enterState(2);
 //			}
 //		}
 				
 		//String input = textbox.getText();
 		
 		
 		
 		 
 	      mouse = "Mouse position x: " + posX + " y: " + posY;
 	      
 
 		
 	}
 	
 	public UnicodeFont getNewFont(String fontName , int fontSize)
     {
         font2 = new UnicodeFont(new Font(fontName , Font.PLAIN , fontSize));
 //        font2.addGlyphs("@");
         font2.getEffects().add(new ColorEffect(java.awt.Color.white));
         return (font2);
     }
 	
 	public int getID()
 	{
 		return 6;
 	}
 	
 	//this is what the button should do then move to the next
 	public void setCharacterSheet(TextField username)
 	{
 		
 		
 		//CHRIS
 		/*
 		 * I NEED BUTTONS 
 		 * x2 race buttons 1 human 1 elf. the human button value set to 1 and elf to 0
 		 * x1 class button 1 barbarian barb button value set to 0
 		 *
 		 * x2 gender buttons 1M 1F
 		 * x3 size buttons sm med lrg (the values set to those are 1 for sm 0 for med and -1 for lrg)
 		 
 		 */
 		//fix with buttons
 		
 		//this method fills the characterSheet
 		sheet.fillBasic(characterName.getText(), playerName.getText(), race, languages.getText(), size, gender, 
 						height.getText(), weight.getText(), age.getText(), alignments.getText(), deity.getText(), 
 						background.getText(), occupation.getText());
 		
 		sheet.fillAbilities();
 		
 		//get class option button value
 		
 		
 		//sheet.fillRecorder(sheet.chooseClass(class button value here)/*This needs the class option from button */);
 		
 		//sheet.fillAttacksAndDefense(/* the class option*/);
 		
 		//after this we need to probably make an equipment purchase thing to buy armor to equip and weapons
 	
 	}
 	
 	
 	
 
 	
 
 
 
 }
