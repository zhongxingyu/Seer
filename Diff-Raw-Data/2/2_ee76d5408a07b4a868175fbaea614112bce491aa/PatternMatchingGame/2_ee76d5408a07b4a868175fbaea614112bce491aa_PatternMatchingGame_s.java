 package games.matchingpattern;
 
 import java.applet.AudioClip;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import render.Geometry;
 import render.Material;
 import render.Renderer;
 import elements.Fonts;
 import elements.GameCube;
 
 import games.Game;
 import games.maxColorGame.MaxColorGame;
 
 public class PatternMatchingGame extends Game {
 
 	java.applet.Applet applet;
 
 	Material defaultMaterial;
 	// Gameplay variables
 	private int numCubeDimensions = 2;
 	private double playerScore = 0;
 	private boolean gameOn = false, gameOver = false, gameWin = false;
 
 	// Audio variables
 	private AudioClip backgroundAudio;
 	private boolean audioOn = false;
 
 	//consecutive elements are pairs
 	List<Window> images = new ArrayList<Window>();
 	boolean isTimerOn = false;
 
 	MatchingPatternCube mirrorCube;
 
 	int currentLevel = 0;
 
 	// Loaded constructor
 	public PatternMatchingGame(java.applet.Applet app, Geometry world, boolean audioStatus)
 	{
 		Renderer.setBg("images/background1.png");
 
 		// Set game variables
 		this.applet = app;
 		this.world = world;
 		this.audioOn = audioStatus;
 
 		// Set cube and tile materials
 		this.defaultMaterial = new Material();
 		this.defaultMaterial.setAmbient(0.1, 0.7, 0.1);
 		this.defaultMaterial.setDiffuse(0.8, 0.8, 0.8);
 		this.defaultMaterial.setSpecular(0.9, 0.9, 0.9, 10);
 
 	}
 
 
 	private void coverCubeWithBGImage()
 	{
 		if(this.cube != null)
 		{
 			for(int face = 0; face < this.cube.getNumFaces(); face++)
 			{
 				for(int row = 1; row <= this.cube.getDimension(); row++)
 				{
 					for(int column = 1; column <= this.cube.getDimension(); column++)
 					{
 						this.cube.setTileMaterial(face, row, column, defaultMaterial);
 						((MatchingPatternCube)this.cube).showMeshOnFace(face, row, column,"images/match/background.png");
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public void initGame() {
 		// TODO Auto-generated method stub
 		this.initLevel();
 		if(this.audioOn)
 			this.enableAudio();
 		this.gameOn = true;
 
 	}
 
 	@Override
 	public void initLevel() {
 
 		playerScore = 0;
 		currentLevel++;
 
 		// If already a cube, clear it out
 		if(this.cube != null)
 		{
 			this.deleteCubeFromWorld();
 		}
 		// Re-create the cube
 		if(currentLevel == 1){
 			this.cube = new MatchingPatternCube(world, this.numCubeDimensions);
 			initializeWindowObjects();
 			coverCubeWithBGImage();
 			this.gameOn = true;
 		}else if(currentLevel == 2){
 			this.cube = new MatchingPatternCube(world, this.numCubeDimensions+1);
 			initializeWindowObjects();
 			coverCubeWithBGImage();
 			this.gameOn = true;
 		}
 	}
 
 	private void initializeWindowObjects() {
 
 		if(this.cube != null)
 		{
 			for(int face = 0; face < this.cube.getNumFaces(); face++)
 			{
 				for(int row = 1; row <= this.cube.getDimension(); row++)
 				{
 					for(int column = 1; column <= this.cube.getDimension(); column++)
 					{
 						images.add(new Window(face,row,column));
 					}
 				}
 			}
 		}
 
 		Collections.shuffle(images);
 
 		int counter = 1;
 
 		for(int i = 0; i<images.size();){
 
 			if(counter <= 12){
 				images.get(i).imageId = counter+".png";
 				images.get(i+1).imageId = counter+".png";
 				i= i+2;
 			}else{
 				images.get(i).imageId = "default.png";
 				i++;
 			}
 			counter++;
 		}
 
 		for(int i = 0; i< images.size();){
 			if(i < 24){
 				images.get(i).matchingpair = images.get(i+1);
 				images.get(i+1).matchingpair = images.get(i);
 				i=i+2;
 			}else{
 				images.get(i).matchingpair = null;
 				i++;
 			}
 		}
 	}
 
 
 	@Override
 	public void stop() {
 
 		this.gameOn = false;
 
 		// Delete the 3D cube from the world
 		this.deleteCubeFromWorld();
 
 		// End any audio
 		if(this.backgroundAudio != null)
 		{
 			this.backgroundAudio.stop();
 			this.backgroundAudio = null;
 		}
 	}
 
 	@Override
 	public void clickTile(int face, int row, int column) {
 
 		if(this.gameOn && getOpenImages() < 2 )
 		{
 			// Spread the tile color to the touching side tiles
 			for(Window w:images){
 
 				if(face == w.face && row == w.row && column== w.col){
 
 					if(!w.isOpen){
 
 						((MatchingPatternCube)this.cube).showMeshOnFace(face, row, column,"images/match/"+w.imageId);
 						w.isOpen = true;
 
 						if(w.matchingpair != null){
 							if(w.isOpen && w.matchingpair.isOpen){
 								w.isDiscovered = true;
 								w.matchingpair.isDiscovered = true;
 							}
 						}else{
 							w.isDiscovered = true;
 						}
 
 
 						if(!isTimerOn){
 							isTimerOn = true;
 
 							new Timer().schedule(new TimerTask() {
 								@Override
 								public void run() {
 									isTimerOn = false;
 									refreshAllCubes();
 									if(islevelOver()){
 										initLevel();
 									}
 								}
							},1000);
 						}
 
 					}
 				}
 			}
 
 
 		}
 	}
 
 	private int getOpenImages() {
 		int counter = 0;
 		for(Window w:images){
 			if(w.isOpen){
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 
 	protected void refreshAllCubes() {
 
 		if(this.cube != null)
 		{
 			for(int face = 0; face < this.cube.getNumFaces(); face++)
 			{
 				for(int row = 1; row <= this.cube.getDimension(); row++)
 				{
 					for(int column = 1; column <= this.cube.getDimension(); column++)
 					{
 
 						for(Window w:images){
 
 							if(face == w.face && row == w.row && column== w.col){
 
 								if(!w.isDiscovered){
 									((MatchingPatternCube)this.cube).showMeshOnFace(face, row, column,"images/match/background.png");
 								}
 								w.isOpen = false;
 
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public boolean islevelOver(){
 		for(Window w:images){
 			if(!w.isDiscovered){
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	//these are gonna be same for all the games
 	// should be present in super class
 	@Override
 	public void toggleAudio(boolean b) {
 		if(b)
 			enableAudio();
 		else
 			disableAudio();
 	}
 
 	@Override
 	public void enableAudio()
 	{
 		if(this.backgroundAudio == null)
 		{
 			this.backgroundAudio = this.applet.getAudioClip(this.applet.getCodeBase(),
 			"audio/background2.wav");
 		}
 
 		this.backgroundAudio.loop();
 	}
 
 	@Override
 	public void disableAudio()
 	{
 		if (this.backgroundAudio != null) {
 			this.backgroundAudio.stop();
 		}
 	}
 
 
 	@Override
 	public void animate(double time) {
 		if(this.cube != null)
 		{
 			this.cube.animate(time);
 		}
 	}
 
 	@Override
 	public void drawOverlay(Graphics g) {
 
 		// If game is over, draw the message
 		if(this.gameOver)
 		{
 			g.setColor(Color.RED);
 			g.setFont(Fonts.BIG_FONT);
 			g.drawString("GAME OVER", 220, 200);
 		}
 
 		// Draw lives and level information
 		g.setColor(Color.BLUE);
 		g.setFont(Fonts.BIG_FONT);
 
 		// Draw the top level text
 		g.drawString("Match Pictures", 150, 30);
 
 		g.setFont(Fonts.SMALL_FONT);
 		g.drawString("Click on a tile to see its picture", 150, 65);
 		g.drawString("and find its corresponding pair", 150, 85);
 		
 		g.drawString("Level "+currentLevel,20,420);
 	}
 
 
 }
