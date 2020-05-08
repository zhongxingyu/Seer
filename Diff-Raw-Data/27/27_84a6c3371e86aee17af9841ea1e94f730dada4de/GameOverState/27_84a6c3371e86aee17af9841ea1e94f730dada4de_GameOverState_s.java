 package org.ssg.Cambridge;
 
 import org.newdawn.slick.AngelCodeFont;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Polygon;
 import org.newdawn.slick.geom.Transform;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import paulscode.sound.SoundSystem;
 import paulscode.sound.SoundSystemConfig;
 
 public class GameOverState extends BasicGameState implements KeyListener {
 
 	public boolean shouldRender;
 	
 	public int stateID;
 	
 	int[] scores;
 	int numPlayers;
 	String[] names = {"Team 1","Team 2","Team 3","Team 4"};
 	Color[] teamColors;
 	int[][] playerCharacters;
 	Polygon[] polys;
 	float pTheta;
 	float maxCircleSize;
 	
 	GlobalData data;
 	SoundSystem mySoundSystem;
 	Font font, font_white, font_small;
 	
 	Confetti[] confetti;
 	boolean confettiSetup;
 	Color confettiColor;
 	
 	float bgmFadeTimer;
 	float BGMFADETIME = 4000;
 	
 	float tempf;
 	int[] tempArr;
 	Color tempCol;
 	
 	public GameOverState(int id, boolean renderOn){
 		stateID = id;
 		shouldRender = renderOn;
 	}
 	
 	@Override
 	public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		
 		data = ((Cambridge) sbg).getData();
 		mySoundSystem = data.mySoundSystem();
 		
 		try {
 			font = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0.png"));
 			font_white = new AngelCodeFont(data.RESDIR + "8bitoperator.fnt", new Image(data.RESDIR + "8bitoperator_0_white.png"));
 			font_small = new AngelCodeFont(data.RESDIR + "8bitoperator_small.fnt", new Image(data.RESDIR + "8bitoperator_small_0.png"));
 		} catch (SlickException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Fonts not loaded properly. Uh oh. Spaghettio.");
 			e.printStackTrace();
 		}
 		
 		scores = new int[4];
 		numPlayers = 2;
 		
 		bgmFadeTimer = BGMFADETIME;
 		
 		confetti = new Confetti[50];
 		confettiSetup = false;
 		
 		polys = new Polygon[8];
 	}
 
 	public void initPlayerPolys(){
 		//Back
 		tempf = data.screenHeight()*.7f/50f*.8f;
 		polys[0] = new Polygon(new float[]{
 				tempf*2, tempf*2,
 				tempf*2, -tempf*2,
 				tempf, -tempf*2,
 				tempf, -tempf*3,
 				-tempf, -tempf*3,
 				-tempf, -tempf*2,
 				-tempf*2, -tempf*2,
 				-tempf*2, tempf*2,
 				-tempf, tempf*2,
 				-tempf, tempf*3,
 				tempf, tempf*3,
 				tempf, tempf*2});
 		//Dash
 		tempf = data.screenHeight()*.7f/10f*.9f*.8f;
 		polys[1] = new Polygon(new float[]{-tempf*2/3,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
 		//Enforcer
 		polys[2] = new Polygon();//not used
 		//Neo
 		tempf = data.screenHeight()*.7f/10f/2f*.6f;
 		polys[3] = new Polygon(new float[]{
 				tempf, tempf,
 				-tempf, -tempf,
 				tempf, -tempf,
 				-tempf, tempf,
 				tempf, tempf,
 				tempf, -tempf,
 				-tempf, -tempf,
 				-tempf, tempf});
 		//Neutron
 		polys[4] = new Polygon();//not used
 		//Tricky
 		tempf = data.screenHeight()*.7f/10f/2f*.6f;
 		polys[5] = 	new Polygon(new float[]{
 				tempf, tempf,
 				tempf, -tempf,
 				-tempf, -tempf,
 				-tempf, tempf});
 		//Twin
 		tempf = data.screenHeight()*.7f/10f/7f;
 		polys[6] = new Polygon(new float[]{
 				tempf, tempf,
 				tempf, -tempf,
 				-tempf, -tempf,
 				-tempf, tempf});
 		//TwoTouch
 		tempf = data.screenHeight()*.7f/10f*.7f;
 		polys[7] = new Polygon(new float[]{0,0,-tempf/3, -tempf/2, tempf*2/3, 0, -tempf/3, tempf/2});
 	}
 	
 	@Override
 	public void enter(GameContainer gc, StateBasedGame sbg) throws SlickException {
 		//mySoundSystem.pause("BGMGame");
 		bgmFadeTimer = BGMFADETIME;
 		
 		//Sort the names array by the scores array, largest first
 		int first = 0;
 		for (int i = scores.length - 1; i > 0; i -- ) {
   		first = 0;   //initialize to subscript of first element
 			for(int j = 1; j <= i; j ++) {
       			if( scores[ j ] < scores[ first ] )
          				first = j;
   			}
   			int temp = scores[ first ];   //swap smallest found with element in position i.
   			scores[ first ] = scores[ i ];
   			scores[ i ] = temp; 
 			
 			String tempStr = names[first];
 			names[first] = names[i];
 			names[i] = tempStr;
 			
 			tempCol = teamColors[first];
 			teamColors[first] = teamColors[i];
 			teamColors[i] = tempCol;
 			
 			tempArr = playerCharacters[first];
 			playerCharacters[first] = playerCharacters[i];
 			playerCharacters[i] = tempArr;
 		}
 		
 		//Setup confetti		
 		if(scores[0] == scores[1]){//If there's a draw
 			confettiColor = new Color(0,0,0,0);//no confetti
 		}else if(scores[0]>0){
 			mySoundSystem.quickPlay( true, "Applause.ogg", false, 0, 0, 0, SoundSystemConfig.ATTENUATION_NONE, 0.0f );
 			confettiColor = teamColors[0];
 			//System.out.println(teamColors[0].getRed()+","+teamColors[0].getGreen()+","+teamColors[0].getBlue()+","+teamColors[0].getAlpha());
 		}
 		
 		for(int i=0;i<confetti.length;i++){
 			confetti[i] = new Confetti((float)Math.random()*data.screenWidth(), data.screenHeight()+50, ((float)Math.random()-.5f)*20f, -500f*(float)Math.random(), confettiColor, (float)Math.random(), data);
 			if(i==confetti.length-1)
 				confettiSetup = true;
 		}
 		
 		//System.out.println(scores[0]+", "+scores[1]+", "+scores[2]+" "+scores[3]);
 		initPlayerPolys();
 		maxCircleSize = data.screenHeight()/8f;//player circle diameters
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
 		g.setAntiAlias(true);
 		g.setLineWidth(2f);
 		g.setBackground(Color.black);
 		
 		g.setColor(Color.white);
 		g.setFont(font_white);
 		g.drawString("Final Score", data.screenWidth()/2-font.getWidth("FINAL SCORE")/2, 20);
 		
 		for(int i=0; i<scores.length; i++){
 			if(scores[i]>=0){
 				g.setColor(teamColors[i]);
				String str = names[i]+": "+ scores[i];
 				tempf = font.getWidth(str)+(scores[i]<100? font.getWidth("0"):0)+(scores[i]<10? font.getWidth("0"):0);
 				for(int j=0; j<playerCharacters[i].length;j++){
 					g.setLineWidth(2);
 					drawPlayer(g, data.screenWidth()/2-tempf/2f-(maxCircleSize+15)*((float)j+1f), 150+(Math.max(font.getHeight("0"),maxCircleSize)+15)*(float)i+font.getHeight("0")/2f, playerCharacters[i][j]);
 					g.setLineWidth(5);
 					g.drawOval( data.screenWidth()/2-tempf/2f-(maxCircleSize+15)*((float)j+1f)-maxCircleSize/2f, 150+(Math.max(font.getHeight("0"),maxCircleSize)+15)*(float)i+font.getHeight("0")/2f-maxCircleSize/2f, maxCircleSize, maxCircleSize);
 				}
 				g.drawString(str, data.screenWidth()/2-tempf/2f, 140+(Math.max(font.getHeight("0"),maxCircleSize)+15)*i);
 				if(scores[i]==scores[0] && scores[0] != 0){
 					g.drawString("WINNER!", data.screenWidth()/2f + tempf/2+45, 140+(Math.max(font.getHeight("0"), maxCircleSize)+15)*i);
 				}
 			}
 		}
 
		
		
 		for(Confetti c: confetti){
 			c.render(g);
 		}
 		
 	}
 
 	public void drawPlayer(Graphics g, float x, float y, int charNum){
 		switch(charNum){
 		case 0://Back
 			g.translate(x, y);
 			g.draw(polys[0].transform(Transform.createRotateTransform((float)Math.PI/2f+pTheta)));
 			g.translate(-x, -y);
 			break;
 		case 1://Dash
 			g.translate(x,y);
 			g.draw(polys[1].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x,-y);
 			break;
 		case 2://Enforcer
 			tempf = data.screenHeight()*.7f/10f/2*.7f;
 			g.setLineWidth(8);
 			g.rotate(x, y, pTheta*360f/2f/(float)Math.PI);
 			g.drawLine(x-tempf, y, x-tempf/2-3, y);
 			g.drawLine(x+tempf/2+3, y, x+tempf, y);
 			g.drawLine(x, y-tempf, x, y-tempf/2-3);
 			g.drawLine(x, y+tempf/2+3, x, y+tempf);
 			g.setLineWidth(2);
 			g.drawRect(x-tempf/2, y-tempf/2, tempf, tempf);
 			g.rotate(x, y, -pTheta*360f/2f/(float)Math.PI);
 			break;
 		case 3://Neo
 			g.translate(x,y);
 			g.draw(polys[3].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x,-y);
 			break;
 		case 4://Neutron
 			tempf = data.screenHeight()*.7f/10f*.8f;
 			g.rotate(x, y, pTheta*360f/2f/(float)Math.PI);
 			g.drawOval(x-tempf/2, y-tempf/2,  tempf,  tempf);
 			g.setLineWidth(1f);
 			for(int i=0; i<6; i++){
 				g.drawLine(x+(float)Math.cos(i*Math.PI/3)*tempf/2, y+(float)Math.sin(i*Math.PI/3)*tempf/2,
 						x+(float)Math.cos(i*Math.PI/3+Math.PI*.6f)*tempf/2, y+(float)Math.sin(i*Math.PI/3+Math.PI*.6f)*tempf/2);
 			}
 			g.rotate(x, y, -pTheta*360f/2f/(float)Math.PI);
 			break;
 		case 5://Tricky
 			g.setLineWidth(2);
 			g.translate(x, y);
 			g.draw(polys[5].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x, -y);
 			break;
 		case 6://Twin
 			tempf = data.screenHeight()*.7f/50f;
 			g.rotate(x, y, pTheta*90f/(float)Math.PI);
 			g.translate(x-tempf,y-tempf);
 			g.draw(polys[6].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x+tempf,-y+tempf);
 			g.translate(x+tempf, y+tempf);
 			g.draw(polys[6].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x-tempf, -y-tempf);
 			g.rotate(x, y, -pTheta*90f/(float)Math.PI);
 			break;
 		case 7://TwoTouch
 			g.translate(x,y);
 			g.draw(polys[7].transform(Transform.createRotateTransform(pTheta)));
 			g.translate(-x,-y);
 			break;
 		case 8://Unused
 			g.setFont(font_white);
 			g.drawString("?", x-font.getWidth("?")/2, y-font.getHeight("?")/2-12f);
 //			g.drawRect(x,y,font.getWidth("?"), font.getHeight("?"));
 			break;
 		default:
 			break;
 		}
 	}
 	
 	@Override
 	public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
 		
 		//Fade out the bgm
 		mySoundSystem.setVolume("BGM", (data.ambientSound()/10f)*(bgmFadeTimer/BGMFADETIME));
 		bgmFadeTimer -= (float)delta;
 		
 		for(Confetti c:confetti)
 			if(confettiSetup)
 				c.update(delta);
 		
 		pTheta+=(float)delta/240f;
 		if(pTheta>(float)Math.PI*2f)
 			pTheta-=(float)Math.PI*2f;
 		
 		Input input = gc.getInput();
 		if(input.isKeyPressed(Input.KEY_ENTER)){
 			setShouldRender(false);
 			((MenuMainState)sbg.getState(data.MENUMAINSTATE)).setShouldRender(true);
 			sbg.enterState(data.MENUMAINSTATE);
 		}else if(input.isKeyPressed(Input.KEY_ESCAPE)){
 			gc.exit();
 			mySoundSystem.cleanup();
 		}
 	}
 
 	public void setScores(int[] s){
 		for(int i=0;i<scores.length;i++){
 			if(i<s.length)
 				scores[i]=s[i];
 			else
 				scores[i]=-1;
 		}
 		numPlayers = s.length;
 	}
 	
 	public void setColors(Color[] c){
 		teamColors = c;
 	}
 	
 	public void setCharacters(int[][] playerChars){
 		playerCharacters = playerChars;
 	}
 	
 	public void setShouldRender(boolean b) {
 		shouldRender = b;
 	}
 	
 	@Override
 	public int getID() {
 		return stateID;
 	}
 
 
 
 }
