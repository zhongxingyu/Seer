 package com.me.ampdom;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Files.FileType;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 //menu por Erikagee
 public class Menu {
 	public ArrayList<Button> buttons;
 	public OrthographicCamera cam;
 	public Texture start, main, htp, cred, c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11;
 	public Texture back_main, credits, htpb, pause, play, home;
 	public Texture play_main, son, soff, startb, next, help, exit;
 	public Sprite icon, popup;
 	public SpriteBatch sb;
 	public static Music music;
 	public boolean soundOn = true, pop = false;
 	//creates a menu
 	public Menu(OrthographicCamera cam){
 		
 		this.cam = cam;
 		buttons = new ArrayList<Button>();
 		//cut scene textures
 		c1 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		/*c2 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c3 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c4 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c5 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c6 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c7 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c8 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c9 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c10 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		c11 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));*/
 		//screen textures
		start = new Texture(Gdx.files.internal("data/Menu/Start_Screen copy.png"));
		main = new Texture(Gdx.files.internal("data/Menu/Main_Screen copy.png"));
		htp = new Texture(Gdx.files.internal("data/Menu/HTP_Screen copy.png"));
		cred = new Texture(Gdx.files.internal("data/Menu/Credits_Screen copy.png"));
 		//button textures
 		back_main = new Texture(Gdx.files.internal("data/Menu/Back_button.png"));
 		credits = new Texture(Gdx.files.internal("data/Menu/Credits_button.png"));
 		htpb = new Texture(Gdx.files.internal("data/Menu/HTP_button.png"));
 		//pause = new Texture(Gdx.files.internal("data/Menu/pause copy.png"));
 		//play = new Texture(Gdx.files.internal("data/Menu/play copy.png"));
 		play_main = new Texture(Gdx.files.internal("data/Menu/Play_button.png"));
 		son = new Texture(Gdx.files.internal("data/Menu/soundON.png"));
 		soff = new Texture(Gdx.files.internal("data/Menu/soundOFF.png"));
		startb = new Texture(Gdx.files.internal("data/Menu/start_button copy.png"));
 		next = new Texture(Gdx.files.internal("data/Menu/next.png"));
 		help = new Texture(Gdx.files.internal("data/Menu/helpButton.png"));
 		home = new Texture(Gdx.files.internal("data/Menu/home.png"));
 		exit = new Texture(Gdx.files.internal("data/Menu/Exit.png"));
 		initialize();
 	}
 	
 	public void initialize(){
 		addBackground();
 		initializeButtons();
 		music = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/sounds/beat.mp3", FileType.Internal));
 		music.setLooping(true);
 		music.play();
 		popup = new Sprite(htp);
 		popup.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 	}
 	
 	public void initializeButtons(){
 		int w = Gdx.graphics.getWidth();
 		int h = Gdx.graphics.getHeight();
 		
 		//clickable buttons
 		buttons.add(new Button("next",w-80,10,true,next,next, cam));
 		buttons.add( new Button("start",w/2 - 50,h - (h - 50),true,startb,startb, cam) );
 		buttons.add( new Button("sound",w - 50,h - 50,true,son,soff, cam) );
 		buttons.add( new Button("help", w - 90,h - 50,true,help,help, cam) );
		buttons.add( new Button("home", w - 130,h-50,true,home,home,cam));
		buttons.add( new Button("exit",w/2-20,h/2-150,true,exit,exit,cam));
 		//buttons.add( new Button("play",760,580,true,pause,play, cam) );
 		buttons.add( new Button("play_main",w/2 - 20,h/2 - 50,true,play_main, play_main, cam) );
		buttons.add( new Button("back",w/2-20, 50,true,back_main,back_main, cam) );
 		buttons.add( new Button("credits",w/2-40,h/2 - 100,true,credits,credits, cam) );
		buttons.add( new Button("htp",w/2-70, h/2,true,htpb,htpb, cam) );
 		
 		
 	}
 	
 	public void addButton(Button b){
 		buttons.add(b);	
 	}
 	
 	public void addBackground(){		
 		icon = new Sprite(start);
 		icon.setPosition(0, 0);
 		sb = new SpriteBatch();	
		icon.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 	}	
 	
 	public void changeBackground(Texture t){
 		icon = new Sprite(t);
 		icon.setPosition(0, 0);
		icon.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 	}
 	
 	public void display(){
 		if(AmpDom.state ==- 5)
 			LevelMap.bgMusic.setVolume(0);
 		draw();		
 		
 		for(Button b : buttons){
 			
 			//menu states: (-5) - start 
 			//(-4) - main 
 			//(-3) - how to play 
 			//(-2) - credits
 			//(-1) - first cut scene
 			//(evens) - play screens
 			//(odds) - cut scenes
 			
 				if(AmpDom.state == -5){
 					if(b.name.equals("start")) {
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -4;
 					}
 				}else if(AmpDom.state == -4){ //main screen
 									
 					if(b.name.equals("htp")){
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -3;
 					}
 					if(b.name.equals("credits")){
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -2;
 					}
 					
 					if(b.name.equals("exit")){
 						b.draw();	
 						if(b.clicked())
 							System.exit(0);
 					}
 					
 					if(b.name.equals("play_main")){
 						b.draw();	
 						if(b.clicked())
 							AmpDom.state = -1;
 					}
 					if(b.name.equals("sound")){						
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1) {
 								music.setVolume(100);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = true;
 							}
 							else {
 								music.setVolume(0);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = false;
 							}
 						}
 					}
 					changeBackground(main);					
 				}else if(AmpDom.state == -3 || AmpDom.state == -2){ //if how to play or credits screen
 					
 					if(b.name.equals("back")){
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -4;
 					}
 					
 					if(b.name.equals("sound")){						
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1) {
 								music.setVolume(100);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = true;
 							}
 							else {
 								music.setVolume(0);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = false;
 							}
 						}
 					}
 					
 					
 					if(AmpDom.state == -3)
 						changeBackground(htp);
 					else if(AmpDom.state == -2)
 						changeBackground(cred);
 					
 				}else if(AmpDom.state%2 == 0 && AmpDom.state >= 0){ //game play screens
 					music.setVolume(0);
 					if(soundOn)
 						LevelMap.bgMusic.setVolume(100);
 					
 					if(b.name.equals("sound")){						
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1) {
 								music.setVolume(0);
 								LevelMap.bgMusic.setVolume(100);
 								soundOn = true;
 							}
 							else {
 								music.setVolume(0);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = false;
 							}
 						}
 					}
 					
 					if(b.name.equals("help")){	
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1){
 								pop = false;
 							}else{
 								pop = true;
 							}
 						}
 					}
 					
 					if(b.name.equals("home")){
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -4;
 					}
 					
 				}else if((AmpDom.state%2 == 1 && AmpDom.state > 0) || AmpDom.state == -1){//all cut scenes
 					
 					LevelMap.bgMusic.setVolume(0);
 					
 					if(b.name.equals("help")){	
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1){
 								pop = false;
 							}else{
 								pop = true;
 							}
 						}
 					}
 					
 					if(b.name.equals("home")){
 						b.draw();
 						if(b.clicked())
 							AmpDom.state = -4;
 					}
 					
 					if(b.name.equals("sound")){						
 						b.draw();
 						if(b.clicked()){
 							if(b.toggle == -1) {
 								music.setVolume(100);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = true;
 							}
 							else {
 								music.setVolume(0);
 								LevelMap.bgMusic.setVolume(0);
 								soundOn = false;
 							}
 						}
 					}
 					
 					if(b.name.equals("next")){
 						b.draw();
 						if(b.clicked()){
 							if(AmpDom.state == -1)
 								AmpDom.state = 0;
 							else if(AmpDom.state == 1)
 								AmpDom.state = 2;
 							else if(AmpDom.state == 3)
 								AmpDom.state = 4;
 							else if(AmpDom.state == 5)
 								AmpDom.state = 6;
 							else if(AmpDom.state == 7)
 								AmpDom.state = 8;
 							else if(AmpDom.state == 9)
 								AmpDom.state = 10;
 							else if(AmpDom.state == 11)
 								AmpDom.state = 12;
 							else if(AmpDom.state == 13)
 								AmpDom.state = 14;
 							else if(AmpDom.state == 15)
 								AmpDom.state = 16;
 							else if(AmpDom.state == 17)
 								AmpDom.state = 18;
 							else if(AmpDom.state == 19)
 								AmpDom.state = -4;
 						}
 						
 					}
 					
 					//if(AmpDom.state == -1)
 						changeBackground(c1);
 					/*else if(AmpDom.state == 1)
 						changeBackground(c2);
 					else if(AmpDom.state == 3)
 						changeBackground(c3);
 					else if(AmpDom.state == 5)
 						changeBackground(c4);
 					else if(AmpDom.state == 7)
 						changeBackground(c5);
 					else if(AmpDom.state == 9)
 						changeBackground(c6);
 					else if(AmpDom.state == 11)
 						changeBackground(c7);
 					else if(AmpDom.state == 13)
 						changeBackground(c8);
 					else if(AmpDom.state == 15)
 						changeBackground(c9);
 					else if(AmpDom.state == 17)
 						changeBackground(c10);
 					else if(AmpDom.state == 19)
 						changeBackground(c11);
 					*/
 					
 				}
 			
 			}
 	}
 	
 	public void draw(){
 		sb.begin();
 		
 		
 		if(AmpDom.state % 2 == 0 && AmpDom.state >= 0){
 			icon.draw(sb, 0.0f);
 		}else{
 			icon.draw(sb, 1.0f);
 		}
 		//draw popup for help
 		if(pop)
 			popup.draw(sb, 1.0f);
 		else if(!pop)
 			popup.draw(sb, 0.0f);
 		sb.end();
 	}
 	
 
 	public void dispose(){
 		sb.dispose();
 		start.dispose();
 		main.dispose();
 		htp.dispose();
 		cred.dispose();
 		back_main.dispose();
 		credits.dispose();
 		htpb.dispose();
 //		pause.dispose();
 //		play.dispose();
 		play_main.dispose();
 		son.dispose();
 		soff.dispose();
 		help.dispose();
 		c1.dispose();
 		/*c2.dispose();
 		c3.dispose();
 		c4.dispose();
 		c5.dispose();
 		c6.dispose();
 		c7.dispose();
 		c8.dispose();
 		c9.dispose();
 		c10.dispose();
 		c11.dispose();*/
 		for(Button b : buttons){
 			b.dispose();
 		}
 	}
 }
