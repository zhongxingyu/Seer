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
 	public Texture start, main, htp, cred, cut1;
 	public Texture back_main, credits, howl, htpb, pause, jump, play;
 	public Texture play_main, son, soff, shell, spit, startb, next;
 	public Sprite icon;
 	public SpriteBatch sb;
 	public Music test;
 	
 	//creates a menu with a specified number of buttons
 	public Menu(OrthographicCamera cam){
 		
 		this.cam = cam;
 		buttons = new ArrayList<Button>();
 		//screen textures
 		cut1 = new Texture(Gdx.files.internal("data/Cutscenes/cutscene_test.png"));
 		start = new Texture(Gdx.files.internal("data/Menu/Start_Screen copy.png"));
 		main = new Texture(Gdx.files.internal("data/Menu/Main_Screen copy.png"));
 		htp = new Texture(Gdx.files.internal("data/Menu/HTP_Screen copy.png"));
 		cred = new Texture(Gdx.files.internal("data/Menu/Credits_Screen copy.png"));
 		//button textures
 		back_main = new Texture(Gdx.files.internal("data/Menu/back_main copy.png"));
 		credits = new Texture(Gdx.files.internal("data/Menu/credits_main copy.png"));
 		//howl = new Texture(Gdx.files.internal("data/Menu/hi copy.png"));
 		htpb = new Texture(Gdx.files.internal("data/Menu/HTP_main copy.png"));
 		//pause = new Texture(Gdx.files.internal("data/Menu/pause copy.png"));
 		//jump = new Texture(Gdx.files.internal("data/Menu/pji.png"));
 		//play = new Texture(Gdx.files.internal("data/Menu/play copy.png"));
 		play_main = new Texture(Gdx.files.internal("data/Menu/play_main copy.png"));
 		son = new Texture(Gdx.files.internal("data/Menu/sound1 copy.png"));
 		soff = new Texture(Gdx.files.internal("data/Menu/sound2 copy.png"));
 		startb = new Texture(Gdx.files.internal("data/Menu/start_button copy.png"));
 		next = new Texture(Gdx.files.internal("data/Menu/next copy.png"));
 		//shell = new Texture(Gdx.files.internal("data/Menu/si copy.png"));
 		//spit = new Texture(Gdx.files.internal("data/Menu/spi copy.png"));
 		addBackground();
 		initializeButtons();
 		test = Gdx.audio.newMusic(Gdx.files.getFileHandle("data/sounds/beat.mp3", FileType.Internal));
 		test.setLooping(true);
 		test.play();
 	}
 	
 	public void initializeButtons(){
 		int w = Gdx.graphics.getWidth();
 		int h = Gdx.graphics.getHeight();
 		
 		//clickable buttons
 		buttons.add(new Button("next",w-80,10,true,next,next, cam));
 		buttons.add( new Button("start",w/2 - 50,h - (h - 50),true,startb,startb, cam) );
 		buttons.add( new Button("sound",w - 50,h - 50,true,son,soff, cam) );
 		//buttons.add( new Button("play",760,580,true,pause,play, cam) );
 		buttons.add( new Button("play_main",w/2 - 100,h/2,true,play_main, play_main, cam) );
 		buttons.add( new Button("back",10, 10,true,back_main,back_main, cam) );
 		buttons.add( new Button("credits",w - 80,10,true,credits,credits, cam) );
 		buttons.add( new Button("htp",w/2+50,h/2,true,htpb,htpb, cam) );
 		//non-clickable buttons
 		//buttons.add( new Button("howl",680,580,false,howl, howl, cam) );
 		//buttons.add( new Button("spit",700,580,false,spit, spit, cam) );
 		//buttons.add( new Button("shell",720,580,false,shell, shell, cam) );
 		//buttons.add( new Button("jump",740,580,false,jump, jump, cam) );
 		
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
 		sb = new SpriteBatch();
 		icon.setSize(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
 	}
 	
 	public void display(){
 		draw();
 		for(Button b : buttons){
 			
 			switch(AmpDom.state){
 			//menu states: (-4) - start (-3) - main (-2) - how to play (-1) - credits 0 - play screen
 				case -4:
 					if(b.name.equals("start")){
 						b.draw();
 					}
 					if(b.clicked() && b.name.equals("start")) {
 						b.draw();	
 						AmpDom.state = -3;
 					}
 					
 					
 					break;
 				case -3:
 					if(b.name.equals("sound") || b.name.equals("htp") || b.name.equals("play_main") || b.name.equals("credits")){
 						b.draw();
 					}					
 					if(b.clicked() && b.name.equals("htp")){
 						b.draw();	
 						AmpDom.state = -2;
 					}
 					if(b.clicked() && b.name.equals("credits")){
 						b.draw();	
 						AmpDom.state = -1;
 					}
 					
 					if(b.clicked() && b.name.equals("play_main")){
 						b.draw();	
 						AmpDom.state = 0;
 					}
 					changeBackground(main);					
 					
 					break;
 				case -2:
 					if(b.name.equals("sound") || b.name.equals("back")){
 						b.draw();
 					}
 					if(b.clicked() && b.name.equals("back")){
 						b.draw();	
 						AmpDom.state = -3;
 					}
 					changeBackground(htp);
 					
 					break;
 				case -1:
 					if(b.name.equals("back") || b.name.equals("sound")){
 						b.draw();
 					}
 					if(b.clicked() && b.name.equals("back")){
 						b.draw();	
 						AmpDom.state = -3;
 					}
 					changeBackground(cred);
 					break;
 					
 				case 0:
 					
 					if(b.name.equals("sound")){						
 						b.draw();
 						b.clicked();
 						if(b.toggle == -1) {
 							test.setVolume(100);
 						}
 						else {
 							test.setVolume(0);
 						}
 					}
 					
 					break;
 				case 1:
 					if(b.name.equals("next")){
 						b.draw();
 					}
 					if(b.clicked() && b.name.equals("next")){
 						b.draw();	
 						AmpDom.state = 2;
 					}
 					changeBackground(cut1);
 					break;
 				case 2:	
 					if(b.name.equals("sound")){						
 						b.draw();	
 						b.clicked();
 					}
 					
 					break;
 			
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
 //		howl.dispose();
 		htpb.dispose();
 //		pause.dispose();
 //		jump.dispose();
 //		play.dispose();
 		play_main.dispose();
 		son.dispose();
 		soff.dispose();
 //		shell.dispose();
 //		spit.dispose();
 		for(Button b : buttons){
 			b.dispose();
 		}
 	}
 }
