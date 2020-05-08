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
 
 import DD.Character.DDCharacter;
 import DD.Character.CharacterSheet.CharacterSheet;
 import DD.Character.Equipment.Armor;
 import DD.Character.Equipment.Weapon;
 import DD.MapTool.MapTool;
 
 public class CharCreate extends BasicGameState
 {
 	
 	DDCharacter newchar = new DDCharacter(0);
 	CharacterSheet sheet = new CharacterSheet();
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
 	int race;
 	int char_class;
 	int gender;
 	int size;
 	
 	//boolean printerror = false;
 	boolean agecheck = true;
 	boolean heightcheck= true; 
 	boolean weightcheck= true;
 	int state = 0;
 	
 	Armor newArmor = null;
 	Weapon offHand = null;
 	Weapon mainHand = null;
 	
 	Armor  shield = null;
 	
 	String myMoney = Integer.toString(sheet.getCurrency());
 	
 	public CharCreate(int state)
 	{
 		
 	}
 	
 	public void init(GameContainer gc, StateBasedGame sbg)throws SlickException
 	{
 		screen = new Image("Images/Menus/menuscreen4.jpg");
 		
 		
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
 		
 //		jlsb.render(gc, sbg, g);
 		g.setFont(font2);
 		
 		
 		
 		
 		
 			g.drawString("BACK", 150, 552);
 		
 		if(state == 0)
 		{
 			if(agecheck && weightcheck && heightcheck)
 			{
 			
 				g.drawString("CONTINUE", 135, 522);
 			}
 			else
 			{
 				g.drawString("CONTINUE", 135, 522);
 				if(!agecheck)
 				{
 					g.drawString("Please input an Integer", 286, 263);
 				}
 				if(!heightcheck)
 				{
 					g.drawString("Please input an Integer", 286, 202);
 				}
 				if(!weightcheck)
 				{
 					g.drawString("Please input an Integer", 286, 230);
 				}
 			}
 		}
 		if(state == 1)
 		{
 			g.drawString("CHOOSE RACE", 853, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("HUMAN", 782, 448);
 			g.drawString("ELF", 1005, 448);
 			
 			
 		}
 		
 		
 		if(state == 2)
 		{
 			g.drawString("CHOOSE CLASS", 853, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("BARBARIAN", 782, 448);
 			//g.drawString("SOME OTHER POSSIBLE CLASS", 1005, 448);
 			
 			
 		}
 		
 		if(state == 3)
 		{
 			g.drawString("CHOOSE GENDER", 853, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("MALE", 782, 448);
 			g.drawString("FEMALE", 1005, 448);
 			
 			
 		}
 		
 		if(state == 4)
 		{
 			g.drawString("CHOOSE SIZE", 853, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("SMALL", 782, 448);
 			g.drawString("MEDIUM", 894, 448);
 			g.drawString("LARGE", 1005, 448);
 			
 			
 		}
 		
 
 		if(state == 5)
 		{
 			g.drawString("CHOOSE ARMOR", 853, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("LIGHT", 782, 448);
 			g.drawString("MEDIUM", 894, 448);
 			g.drawString("HEAVY", 1005, 448);
 			
 			
 		}
 		
 		if(state == 6)
 		{
 			g.drawString("CHOOSE LIGHT ARMOR", 833, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("ARMOR NAME", 740, 250);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			
 			
 			//Armor Name 
 			g.drawString("Padded", 740, 300);
 			g.drawString("Leather", 740, 325);
 			g.drawString("Chain Shirt", 740, 350);
 			
 			//Armor pricing
 			g.drawString("5", 980, 300);
 			g.drawString("10", 980, 325);
 			g.drawString("100", 980, 350);
 				
 			
 		}
 		
 		
 		if(state == 7)
 		{
 			g.drawString("CHOOSE MEDIUM ARMOR", 833, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("ARMOR NAME", 740, 250);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			
 			//Armor Name 
 			g.drawString("Scale Mail", 740, 300);
 			g.drawString("Chain Mail", 740, 325);
 			g.drawString("Breast Plate", 740, 350);
 			
 			//Armor pricing
 			g.drawString("50", 980, 300);
 			g.drawString("150", 980, 325);
 			g.drawString("200", 980, 350);
 				
 			
 		}
 		
 		if(state == 8)
 		{
 			g.drawString("CHOOSE HEAVY ARMOR", 833, 506);
 			g.drawString("PREVIOUS", 867, 546);
 			g.drawString("ARMOR NAME", 740, 250);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			
 			//Armor Name 
 			g.drawString("Banded Mail", 740, 300);
 			g.drawString("Half-Plate", 740, 325);
 			g.drawString("Full Plate", 740, 350);
 			
 			//Armor pricing
 			g.drawString("250", 980, 300);
 			g.drawString("600", 980, 325);
 			g.drawString("750", 980, 350);
 				
 			
 		}
 		
 		
 		if(state == 9)
 		{
 			g.drawString("EQUIP A SHIELD?", 833, 506);
 			
 			//GO BACK TO STATE 5
 			g.drawString("PREVIOUS", 867, 546);
 			
 			//GO TO STATE 10!!!
 			g.drawString("YES", 782, 448);
 			
 			//SKIP TO STATE 11
 			g.drawString("NO", 1005, 448);
 		}
 		
 		if(state == 10)
 		{
 			
 			g.drawString("CHOOSE SHIELD", 833, 506);
 			g.drawString("SHIELD NAME", 740, 250);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			
 			//SHIELD Name 
 			g.drawString("Buckler", 740, 300);
 			g.drawString("Light Steel Shield", 740, 325);
 			g.drawString("Heavy Steel Shield", 740, 350);
 			
 			//SHIELD pricing
 			g.drawString("5", 980, 300);
 			g.drawString("9", 980, 325);
 			g.drawString("20", 980, 350);
 
 			
 		}
 		
 		// IF WE CLICK NO SHIELD
 		if(state == 11)
 		{
 			
 			g.drawString("CHOOSE WEAPON", 833, 506);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			g.drawString("WEAPON NAME", 740, 250);
 			
 			
 			//WEAPON NAME 
 			g.drawString("Longsword(1h)", 740, 300);
 			g.drawString("Battleaxe(2h)", 740, 325);
 			g.drawString("Cutlass(1h)", 740, 350);
 			g.drawString("Klar(1h)", 740, 375);
 			g.drawString("Heavy Pick(2h)", 740, 400);
 			g.drawString("Rapier(1h)", 740, 425);
 			g.drawString("Warhammer(2h)", 740, 450);
 			g.drawString("No Weapon(1h)", 740, 475);
 			
 			//WEAPON pricing
 			g.drawString("15", 980, 300);
 			g.drawString("10", 980, 325);
 			g.drawString("15", 980, 350);
 			g.drawString("12", 980, 375);
 			g.drawString("8", 980, 400);
 			g.drawString("20", 980, 425);
 			g.drawString("12", 980, 450);
 			g.drawString("0", 980, 475);
 			
 		}
 		
 		//ONE HANDED WEAPONS
 		if(state == 12)
 		{
 			
 			g.drawString("CHOOSE WEAPON", 833, 506);
 			g.drawString("PRICE", 980, 250);
 			g.drawString("CURRENCY", 860, 200);
 			g.drawString(myMoney, 975, 200);
 			g.drawString("WEAPON NAME", 740, 250);
 			
 			//WEAPON Name 
 			g.drawString("Longsword(1h)", 740, 300);
 			g.drawString("Cutlass(1h)", 740, 325);
 			g.drawString("Klar(1h)", 740, 350);
 			g.drawString("Rapier(1h)", 740, 375);
 			g.drawString("No Weapon(1h)", 740, 400);
 			
 			//weapon pricing
 			g.drawString("15", 980, 300);
 			g.drawString("15", 980, 325);
 			g.drawString("12", 980, 350);
 			g.drawString("20", 980, 375);
 			g.drawString("0", 980, 400);
 			
 		}
 		
 		if(state == 13)
 		{
 			
 			String mainweapon=mainHand.getName();
 			String offweapon=null;
 			if(offHand == null)
 			{
 				offweapon = "None";
 			}
 			else
 			{
 				offweapon=offHand.getName();
 			}
 			String myshield = null;
 			if(shield == null)
 			{
 				myshield = "No Shield";
 			}
 			else
 			{
 				myshield = shield.getName();
 			}
 			
 			String myarmor=newArmor.getName();
 			String racename = null;
 			String sSize = null;
 			String gen = null;
 			if(race == 0)
 			{
 				racename= "Elf";
 			}
 			else
 			{
 				racename = "Human";
 			}
 			
 			if(size == 0)
 			{
 				sSize = "Medium";
 			}
 			else if(size == 1)
 			{
 				sSize = "Small";
 			}
 			else
 			{
 				sSize = "Large";
 			}
 			
 			if(gender == 0)
 			{
 				gen = "Female";
 			}
 			else
 			{
 				gen = "Male";
 			}
 			
 			g.drawString("Your Selections", 796, 170);
 			g.drawString("Main Hand: " + mainweapon, 785, 245);
 			g.drawString("OffHand: " + offweapon, 785, 270);
 			g.drawString("Shield: " + myshield, 785, 295);
 			g.drawString("Armor: " + myarmor, 785, 320);
 			g.drawString("Race: " + racename, 785, 345);
 			g.drawString("Size: " + sSize, 785, 370);
 			g.drawString("Gender: " + gen, 785, 395);
 			
 			g.drawString("DONE", 765, 495);
 			g.drawString("START OVER", 890, 495);
 
 				
 		}
 		
 		
 		if(state == 14)
 		{
 			g.drawString("CHARACTER SAVED!", 778, 320);
 			g.drawString("JOIN LOBBY", 800, 375);
 		}
 		
 		
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
 		if((posX > 146 && posX < 194) && (posY > 80 && posY < 100))
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
 				
 				state = 0;
 
 			}
 		}
 		
 		//IF STATE = 0 THEN GO TO STATE 1
 		if(state == 0)
 			if((posX > 132 && posX < 219) && (posY > 110 && posY < 132))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					boolean isGood;
 					try
 					{
 						
 						Integer.parseInt(height.getText());
 						isGood = true;
 					}
 					
 					catch(Exception e)
 					{
 						isGood = false;
 						heightcheck = false;
 					}
 					
 					try
 					{
 						
 						Integer.parseInt(weight.getText());
 						isGood = true;
 					}
 					
 					catch(Exception e)
 					{
 						isGood = false;
 						weightcheck = false;
 					}
 					
 					try
 					{
 						
 						Integer.parseInt(age.getText());
 						isGood = true;
 					}
 					
 					catch(Exception e)
 					{
 						isGood = false;
 						agecheck = false;
 					}
 					
 					if(isGood)
 					{
 						button.play();
 						state++;
 					}
 					
 				}
 			
 			}
 		
 		
 		/**************RACE HUMAN OR ELF*****************/
 		//IF STATE = 1 AND IF CLICK HUMAN OR ELF THEN GO TO STATE 2 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 0
 		if(state == 1)
 		{
 			
 			/*********HUMAN HUMAN HUMAN HUMAN ***************/
 			if((posX > 780 && posX < 842) && (posY > 180 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					race = 1;
 					state++;
 				}
 			
 			}
 			
 			/*************ELF  ELF  ELF ************************/
 			else if((posX > 1002 && posX < 1036) && (posY > 185 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					race = 0;
 					state++;
 				}
 			
 			}
 			
 			/*********************PREVIOUS ******************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state--;
 				}
 				
 			}
 			
 			
 			
 		}
 		
 		
 		/**************CHOOSE CLASS*****************/
 		//IF STATE = 2 AND IF CLICK CLASS GO TO STATE 3 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 1
 		if(state == 2)
 		{
 			
 			/*********CHOOSE BARBARIAN ***************/
 			if((posX > 780 && posX < 874) && (posY > 180 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					char_class = 0;
 					state++;
 				}
 			
 			}
 			/*
 			/************* OTHER POSSIBLE CLASS ************************
 			else if((posX > 1002 && posX < 1036) && (posY > 185 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state++;
 				}
 			
 			}
 			*/
 			
 			/*********************PREVIOUS PREVIOUS PREVIOUS**************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state--;
 				}
 				
 			}
 			
 			
 			
 		}
 		
 		/**************GENDER MALE OR FEMALE*****************/
 		//IF STATE = 3 AND IF CLICK HUMAN OR ELF THEN GO TO STATE 4 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 2
 		if(state == 3)
 		{
 			//GENDER IS 1 FOR MALE AND 0 FOR FEMALE
 			/*********MALE MALE MALE ***************/
 			if((posX > 776 && posX < 827) && (posY > 180 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					gender = 1;  //MALE 
 					state++;
 				}
 			
 			}
 			
 			/*************FEMALE FEMALE FEMALE ************************/
 			else if((posX > 1002 && posX < 1073) && (posY > 185 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					gender = 0; //FEMALE
 					state++;
 				}
 			
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state--;
 				}
 				
 			}
 			
 			
 			
 		}
 		
 		
 		/**************SIZE OF CHARACTER*****************/
 		//IF STATE = 4 AND IF CLICK SMALL, MEDIUM, OR LARGE THEN GO TO STATE 5 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 3
 		if(state == 4)
 		{
 			//1 for small 0 for medium and -1 for large
 			
 			/*********SMALL SMALL SMALL ***************/
 			if((posX > 780 && posX < 836) && (posY > 180 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					size = 1;
 					state++;
 				}
 			
 			}
 			
 			/*************MEDIUM MEDIUM MEDIUM ************************/
 			else if((posX > 890 && posX < 960) && (posY > 185 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					size = 0;
 					state++;
 				}
 			
 			}
 			
 			/***************LARGE LARGE LARGE **************/
 			else if((posX > 1000 && posX < 1064) && (posY > 185 && posY < 204))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					size = -1;
 					state++;
 				}
 				
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state--;
 				}
 				
 			}
 		}
 			
 			
 		/**************ARMOR RATING*****************/
 		//IF STATE = 5 AND IF CLICK LIGHT, MEDIUM, HEAVY THEN GO TO CORRESPONDING STATE 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 4
 		if(state == 5)
 		{
 			
 			//1 for small 0 for medium and -1 for large
 			
 			/*********LIGHT LIGHT LIGHT ***************/
 			if((posX > 721 && posX < 829) && (posY > 180 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state++;
 				}
 			
 			}
 			
 			/*************MEDIUM MEDIUM MEDIUM ************************/
 			else if((posX > 890 && posX < 960) && (posY > 185 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state = 7;
 				}
 			
 			}
 			
 			/***************HEAVY HEAVY HEAVY **************/
 			else if((posX > 1000 && posX < 1060) && (posY > 185 && posY < 204))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state = 8;
 				}
 				
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state --;
 				}
 				
 			}
 		}
 		
 		
 		
 		/**************LIGHT ARMOR STATE*****************/
 		//IF STATE = 6 AND IF CLICK ARMOR TYPE THEN GO TO CORRESPONDING STATE 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 5
 		if(state == 6)
 		{
 			
 			
 			/*********WHENEVER AN ARMOR TYPE IS CHOSEN ***************/
 			
 			
 			/********************PADDED ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 797) && (posY > 336 && posY < 348))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(0);
 					sheet.subCurrency(5);
 					state = 9;
 				}
 			
 			}
 			
 			/********************LEATHER ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 797) && (posY > 310 && posY < 321))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(1);
 					sheet.subCurrency(10);
 					state = 9;
 				}
 			
 			}
 			
 			/********************CHAIN SHIRT ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 797) && (posY > 283 && posY < 297))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(2);
 					sheet.subCurrency(100);
 					state = 9;
 				}
 			
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state = 5;
 				}
 				
 			}
 		}
 		
 		
 		
 		
 		
 		/**************MEDIUM ARMOR STATE*****************/
 		//IF STATE = 7 AND IF CLICK ARMOR TYPE THEN GO TO  STATE = 9 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 5
 		if(state == 7)
 		{
 			
 			
 			/*********WHENEVER AN ARMOR TYPE IS CHOSEN ***************/
 			
 			
 			/********************SCALE MAIL ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 816) && (posY > 336 && posY < 348))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(3);
 					sheet.subCurrency(50);
 					state = 9;
 				}
 			
 			}
 			
 			/********************CHAIN MAIL ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 816) && (posY > 310 && posY < 321))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(4);
 					sheet.subCurrency(150);
 					state = 9;
 				}
 			
 			}
 			
 			/********************BREAST PLATE ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 816) && (posY > 283 && posY < 297))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(5);
 					sheet.subCurrency(200);
 					state = 9;
 				}
 			
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state = 5;
 				}
 				
 			}
 		}
 			
 		
 		
 		/**************HEAVY ARMOR STATE*****************/
 		//IF STATE = 8 AND IF CLICK ARMOR TYPE THEN GO TO STATE = 9 
 		//or 
 		//CLICK PREVIOUS AND GO BACK TO STATE 5
 		if(state == 8)
 		{
 			
 			
 			/*********WHENEVER AN ARMOR TYPE IS CHOSEN ***************/
 			
 			
 			/********************BANDED MAIL ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 831) && (posY > 336 && posY < 348))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(6);
 					sheet.subCurrency(250);
 					state = 9;
 				}
 			
 			}
 			
 			/********************HALF PLATE ARMOR IS CHOSEN***************/
 			if((posX > 737 && posX < 812) && (posY > 310 && posY < 321))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(7);
 					sheet.subCurrency(600);
 					state = 9;
 				}
 			
 			}
 			
 			/********************FULL PLATE ARMOR IS CHOSEN***************/
 			//IF STATE = 9 AND IF CLICK ARMOR TYPE THEN GO TO STATE = 10 
 			//or 
 			//CLICK PREVIOUS AND GO BACK TO STATE 5
 			
 			if((posX > 737 && posX < 812) && (posY > 283 && posY < 297))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					newArmor = sheet.getArmorFromArmory(8);
 					sheet.subCurrency(750);
 					state++;
 				}
 			
 			}
 			
 			/***************PREVIOUS PREVIOUS PREVIOUS **************/
 			else if((posX > 867 && posX < 952) && (posY > 85 && posY < 105))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					state = 5;
 				}
 				
 			}
 		}
 		
 		
 		/**************************CHOICE OF WHETHER OR NOT TO EQUIP SHIELD*************/
 		//IF STATE = 9 AND IF CLICK YES THEN GO TO STATE = 10 
 				//or 
 				//CLICK PREVIOUS AND GO BACK TO STATE 5
 				
 		if(state == 9)
 		{
 			
 			
 			/*********WHEN YES IS CHOSEN ***************/
 			
 			
 			/***********************************/
 			if((posX > 780 && posX < 816) && (posY > 184 && posY < 201))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state++;
 				}
 			
 			}
 			
 			/********************NO IS CHOSEN***************/
 			if((posX > 1001 && posX < 1031) && (posY > 186 && posY < 205))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					
 					state = 11;
 				}
 			
 			}
 		}
 			
 			/**************************CHOOSE SHIELD*************/
 			//IF STATE = 10 AND ONCE SHIELD IS CHOSEN GO TO STATE = 11 
 					
 			if(state == 10)
 			{
 				
 				
 				/*********CHOOSE SHIELD TYPE***************/
 				
 				
 				/****************BUCKLER*******************/
 				if((posX > 738 && posX < 794) && (posY > 334 && posY < 347))
 				{
 					if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 					{
 						button.play();
 						shield = sheet.getArmorFromArmory(10);
 						sheet.subCurrency(5);
 						state= 12;
 					}
 				
 				}
 				
 				/********************LIGHT STEEL SHIELD***************/
 				if((posX > 738 && posX < 866) && (posY > 309 && posY < 323))
 				{
 					if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 					{
 						button.play();
 						shield = sheet.getArmorFromArmory(11);
 						sheet.subCurrency(9);
 						state = 12;
 					}
 				
 				}
 				
 				/********************HEAVY STEEL SHIELD***************/
 				if((posX > 738 && posX < 877) && (posY > 283 && posY < 299))
 				{
 					if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 					{
 						button.play();
 						shield = sheet.getArmorFromArmory(9);
 						sheet.subCurrency(20);
 						state = 12;
 					}
 				
 				}
 				
 			
 		}
 	
 		/**************************CHOOSE ALL WEAPONS*************/
 		//IF STATE = 11 AND ONCE WEAPON IS CHOSEN GO TO STATE = 12 OR 13 (DEPENDS 1 HAND OR 2 HAND) 
 				
 		if(state == 11)
 		{
 			
 			
 			/*********CHOOSE WEAPON***************/
 			
 			
 			/****************LONGSWORD*******************/
 			if((posX > 738 && posX < 826) && (posY > 330 && posY < 347))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = sheet.getWeaponFromArmory(1);
 					sheet.subCurrency(15);
 					state= 12;
 				}
 			
 			}
 			
 			/********************BATTLEAXE***************/
 			if((posX > 738 && posX < 866) && (posY > 309 && posY < 323))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(2);
 					sheet.subCurrency(10);
 					state = 13;
 				}
 			
 			}
 			
 			/********************CUTLASS***************/
 			if((posX > 738 && posX < 802) && (posY > 283 && posY < 299))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = sheet.getWeaponFromArmory(3);
 					sheet.subCurrency(15);
 					state = 12;
 				}
 			
 			}
 			
 			/****************KLAR*******************/
 			if((posX > 738 && posX < 775) && (posY > 256 && posY < 273))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = sheet.getWeaponFromArmory(4);
 					sheet.subCurrency(12);
 					state= 12;
 				}
 			
 			}
 			
 			/********************HEAVY PICK***************/
 			if((posX > 738 && posX < 829) && (posY > 234 && posY < 245))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(5);
 					sheet.subCurrency(8);
 					state = 13;
 				}
 			
 			}
 			
 			/********************RAPIER***************/
 			if((posX > 738 && posX < 792) && (posY > 208 && posY < 222))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = sheet.getWeaponFromArmory(6);
 					sheet.subCurrency(20);
 					state = 12;
 				}
 			
 			}
 			
 			/********************WARHAMMER***************/
 			if((posX > 738 && posX < 836) && (posY > 180 && posY < 199))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(7);
 					sheet.subCurrency(12);
 					state = 13;
 				}
 			
 			}
 			
 			/********************NO WEAPON***************/
 			if((posX > 738 && posX < 827) && (posY > 154 && posY < 173))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = sheet.getWeaponFromArmory(0);
 					state = 12;
 				}
 			
 			}
 			
 		
 	}
 	
 		/**************************CHOOSE 1 HANDED WEAPONS*************/
 		//IF STATE = 12 AND ONCE WEAPON IS CHOSEN GO TO STATE = 13  
 				
 		if(state == 12)
 		{
 			
 			
 			/*********CHOOSE WEAPON***************/
 			
 			
 			/****************LONGSWORD*******************/
 			if((posX > 738 && posX < 826) && (posY > 330 && posY < 347))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(1);
 					sheet.subCurrency(10);
 					state= 13;
 				}
 			
 			}
 			
 			/********************CUTLASS***************/
 			if((posX > 738 && posX < 866) && (posY > 309 && posY < 323))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(3);
 					sheet.subCurrency(15);
 					state = 13;
 				}
 			
 			}
 			
 			/********************KLAR***************/
 			if((posX > 738 && posX < 802) && (posY > 283 && posY < 299))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(4);
 					sheet.subCurrency(12);
 					state = 13;
 				}
 			
 			}
 			
 			/****************RAPIER*******************/
 			if((posX > 738 && posX < 775) && (posY > 256 && posY < 273))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(6);
 					sheet.subCurrency(20);
 					state= 13;
 				}
 			
 			}
 			
 			/********************UNARMED***************/
 			if((posX > 738 && posX < 829) && (posY > 234 && posY < 245))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					mainHand = sheet.getWeaponFromArmory(0);
 					state = 13;
 				}
 			
 			}
 			
 		
 	}
 		
 		/**************************DISPLAY CHARACTER INFORMATION*************/
 		//IF STATE = 13 AND WHEN BACK IS PRESSED  
 				
 		if(state == 13)
 		{
 			
 			
 			/*********CHOOSE WEAPON***************/
 			
 			
 			/****************WILL GO TO STATE 14 DONE BUTTON*******************/
 			if((posX > 763 && posX < 814) && (posY > 136 && posY < 158))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					setCharacterSheet();
 					state= 14;
 				}
 			
 			}
 			
 			/***********************START OVER**************************/
 			if((posX > 887 && posX < 992) && (posY > 138 && posY < 153))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					offHand = null;
 					shield = null;
 					
 					
 					sheet.setCurrency(1000);
 					state = 1;
 				}
 			
 			}
 		}
 		
 		
 		/**************************CHARACTER IS SAVED GO TO LOBBY*************/
 		//IF STATE = 14 CHARACTER IS SAVED AND OPTION TO GO TO LOBBY  
 				
 		if(state == 14)
 		{
 			
 			
 			/****************WILL GO TO LOBBY AND DISPLAY CHARACTER SAVED*******************/
 			
 			
 			//JOIN LOBBY OPTION
 			
 			if((posX > 789 && posX < 900) && (posY > 258 && posY < 278))
 			{
 				if(gc.getInput().isMousePressed(gc.getInput().MOUSE_LEFT_BUTTON))
 				{
 					button.play();
 					sbg.enterState(1);
 					state = 0;
 				}
 			
 			}
 		}
 			
 		
 		  myMoney = Integer.toString( sheet.getCurrency());		 
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
 	public void setCharacterSheet() throws SlickException
 	{
 		
 		MapTool mt = new MapTool();
 		
 		
 		//this method fills the characterSheet
 		sheet.fillBasic(characterName.getText(), playerName.getText(), race, languages.getText(), size, gender, 
 						Integer.parseInt(height.getText()), Integer.parseInt(weight.getText()), Integer.parseInt(age.getText()), alignments.getText(), deity.getText(), 
 						background.getText(), occupation.getText());
 		
 		sheet.fillAbilities();
 		
 		//get class option button value
 		
 		
 		sheet.fillRecorder(sheet.chooseClass(char_class));
 		
 		sheet.fillAttacksAndDefense(sheet.chooseClass(char_class));
 		//sheet.unequipWeapons(); //equips with unarmed weapons...
 		System.out.println(mainHand.getName());
 		
 		
 		sheet.equipWeapon(mainHand, 0);
 		
 		
 		if(offHand != null)
 		{
 			sheet.equipWeapon(offHand, 1);
 			
 			
 		}
 		
 		
 		sheet.EquipArmor(newArmor);
 		
 		if(shield != null)
 		{
 			sheet.EquipShield(shield);
 		}
 		
 		sheet.applyArmorAndShield();
 		
 		newchar.setCharacterSheet(sheet);
 		
 		newchar.writeMe(mt.ddPath);
 		//after this we need to probably make an equipment purchase thing to buy armor to equip and weapons
 	
 	}
 	
 	
 	
 
 	
 
 
 
 }
