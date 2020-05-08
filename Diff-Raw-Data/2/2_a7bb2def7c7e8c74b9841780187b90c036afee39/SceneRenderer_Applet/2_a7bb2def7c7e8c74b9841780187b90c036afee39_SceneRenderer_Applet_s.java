 package com.timboe.rpsrts.applet.managers;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GradientPaint;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 
 import java.io.IOException;
 import java.util.HashSet;
 
 import javax.imageio.ImageIO;
 
 import com.timboe.rpsrts.applet.HighScore;
 import com.timboe.rpsrts.enumerators.BuildingType;
 import com.timboe.rpsrts.enumerators.GameMode;
 import com.timboe.rpsrts.enumerators.ObjectOwner;
 import com.timboe.rpsrts.managers.Bitmaps;
 import com.timboe.rpsrts.managers.ResourceManager;
 import com.timboe.rpsrts.managers.Utility;
 import com.timboe.rpsrts.sprites.Building;
 import com.timboe.rpsrts.sprites.WaterfallSplash;
 import com.timboe.rpsrts.applet.sprites.WaterfallSplash_Applet;
 
 
 public class SceneRenderer_Applet {
 	private static SceneRenderer_Applet singleton = new SceneRenderer_Applet();
 	public static SceneRenderer_Applet GetSceneRenderer_Applet() {
 		return singleton;
 	}
 	
 	private final Utility utility = Utility.GetUtility();
 	private final GameWorld_Applet theWorld = GameWorld_Applet.GetGameWorld_Applet();
 	private final Bitmaps_Applet theBitmaps = Bitmaps_Applet.GetBitmaps_Applet();
 	private final SpriteManager_Applet theSpriteManger = SpriteManager_Applet.GetSpriteManager_Applet();
 	private final TransformStore theTransforms = TransformStore.GetTransformStore();
 	private final ResourceManager resource_manager = ResourceManager.GetResourceManager();
 	
 //  private final Color sea_blue = new Color(65,105,225);
 	private final Color dsea_blue = new Color(53,85,183);
 	private final Color lsea_blue = new Color(121,144,216);
 	private final Color backing_brown = new Color(92,50,38);
 	private final Color front_blue = new Color(99,189,200);
 	private final GradientPaint dark_gradient  = new GradientPaint(new Point(0,0), dsea_blue, new Point(0,(int)(utility.waterfall_size*0.5)), Color.black);
 	private final GradientPaint light_gradient = new GradientPaint(new Point(0,0), lsea_blue, new Point(0,(int)(utility.waterfall_size*0.5)), Color.black);
 
 	public BufferedImage background_buffered;
 	private HashSet<Point> background_stars = new HashSet<Point>();
 	
 	public BuildingType buildingToPlace;
 	public Building buildingToMove;
 	int buildingStatBox;
 	public BufferedImage TopBar;
 	//Top bar settings
 	int con_start_x = 400;
 	int x_add = 50;
 	int y_height = 70;
 	
 	public Point CurMouse;
 	public Point2D MouseTF;
 	
 	HighScore highScore;
 	Thread highScoreThread;
 	
     Font myFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
     Font myGridFont = new Font(Font.MONOSPACED, Font.BOLD, 20);
     Font myBigFont = new Font(Font.MONOSPACED, Font.BOLD, 100);
     Font myMediumFont = new Font(Font.MONOSPACED, Font.BOLD, 50);
 	
 	private SceneRenderer_Applet() {
 		try {
 			TopBar = ImageIO.read(Bitmaps.class.getResource("/resource/topbar.png"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		highScore = new HighScore();
 		highScoreThread = new Thread(highScore);
 		System.out.println("--- Scene renderer spawned (depends on Util,World,Bitmap,Sprite,Trans,Resource): "+this);
 	}
 	
 	private void contentCreation(Graphics2D _g2) {
     	theTransforms.SetAA(_g2, false);
     	if (utility.gamePaused == false) {
     		if (utility.fastForward == true) {
     			theTransforms.modifyRotate(0.01f);
     		} else {
     			theTransforms.modifyRotate(0.001f);
     		}
     	}
     	if (theWorld.GetWorldGenerated() == false) {
 	    	final int status = theWorld.GenerateWorld();
 	    	if (status == 1) { //Object made, circle island, plane grid
 	    		theWorld.DrawChunks(_g2,true,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    	} else if (status == 2) { //Island perimeter
 	    		theWorld.DrawChunks(_g2,true,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"CRINKLING THE FJORDS");
 	    	} else if (status == 3) { //Chunks touching and inside perimeter, random energy
 	    		theWorld.DrawChunks(_g2,true,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"SEEDING WORLD GRID");
 	    	} else if (status == 4) { //Doing kt
 	    		theWorld.DrawChunks(_g2,true,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"RUNNING KT ALGORITHM");
 	    	} else if (status == 5) { //Doing kt
 	    		theWorld.DrawChunks(_g2,false,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"RUNNING KT ALGORITHM");
 	    	} else if (status == 6) { //Doing biomes
 	    		drawSea(_g2,true);
 	    		theWorld.DrawTiles(_g2,false,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"ASSIGNING LANDMASSES");
 	    	} else { //Eroding
 	    		drawSea(_g2,true);
 	    		theWorld.DrawTiles(_g2,false,theTransforms.GetAA());
 	    		theWorld.DrawIslandEdge(_g2);
 	    		drawTopText(_g2,"WEATHERING TERRAIN");
 	    	}
     	} else {
     		final int seeding = theSpriteManger.SeedWorld();
     		if (seeding == -1) { //Tits up - start again
     			utility._RPSRTS.genNewWorld();
     		}
     		drawSea(_g2,true);
     		drawBufferedBackground(_g2);
     		theWorld.DrawIslandEdge(_g2);
     		theSpriteManger.Render(_g2);
     		_g2.setTransform(theTransforms.af_none);
     		if (theSpriteManger.GetWorldSeeded() == false) {
     			drawTopText(_g2,"POPULATING THE ISLAND");
     		} else if (utility.dbg == true) {
     			utility.gameMode = GameMode.gameOn;
     		}
     	}
 	}
 	
 	public void doInverseMouseTransform() {
 	    if (CurMouse != null) {
 			try {
 				MouseTF = theTransforms.af.inverseTransform(CurMouse, MouseTF);
 			} catch (final NoninvertibleTransformException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 	
 	private void drawBufferedBackground(Graphics2D _g2) {
     	if (background_buffered == null) {
     		background_buffered = new BufferedImage(utility.world_size, utility.world_size, BufferedImage.TYPE_3BYTE_BGR);
     		Graphics2D gc = background_buffered.createGraphics();
     		gc.translate(utility.world_size2,utility.world_size2);
 			drawSea(gc,false);
     		theWorld.DrawTiles(gc,false,true);
 			drawBufferedBackground(_g2); //recurse to actually draw
     	} else {
 			drawSea(_g2,true);
 			_g2.setTransform(theTransforms.af_backing);
 			_g2.drawImage(background_buffered, null, 0, 0);
 			_g2.setTransform(theTransforms.af);
     	}
     }
 	
 	private void drawEndScreen(Graphics2D g2) {
 		boolean clicked = false;
 		if (utility.gameMode == GameMode.gameOverWon) {
 			g2.setTransform(theTransforms.af_none);
 			g2.drawImage(theBitmaps.WIN,0,0,null);
 			if (utility.playerName != "") {
 				clicked = drawButton(g2, 195, 500, 605, "SUBMIT MY SCORE!", 60);
 			} else {
 				clicked = drawButton(g2, 195, 500, 605, "BACK TO TITLE SCREEN", 0);
 			}
 		} else {
 			g2.setTransform(theTransforms.af_none);
 			g2.drawImage(theBitmaps.LOOSE,0,0,null);
 			clicked = drawButton(g2, 195, 500, 605, "LET ME TRY AGAIN!", 60);
 		}
 		if (clicked == true) {
 			if (utility.playerName != null) {
 				//Do high score!
 				highScoreThread.run();
 			}
 			utility._RPSRTS.genNewWorld();
 			utility.doWorldGen = false;
 		}
 		
 		if (utility.showRedScore == true) {
 			clicked = drawButton(g2, 10, 50, 480, "RED SCORE:"+resource_manager.GetScore(ObjectOwner.Player), 20);
 		} else {
 			clicked = drawButton(g2, 10, 50, 480, "BLU SCORE:"+resource_manager.GetScore(ObjectOwner.Enemy), 20);
 		}
 		if (clicked == true) {
 			utility.showRedScore = !utility.showRedScore;
 		}
 		
 		String cursor = "";
 		if ((System.currentTimeMillis() / 1000L) % 2 == 0) { // if even second
 			cursor = "|";
 		}
 		drawButton(g2, 500, 50, 480, "YOUR NAME:"+utility.playerName+cursor, 20);
 		g2.setFont(myFont);
 		drawStatBox(g2, "By entering a name below, you agree to upload and display of your", " score along with statistics from your game.", "", "", 480, 8, 500, 18);
 
 	}
 	
 	private void drawSea(Graphics2D _g2, boolean doStars) {
 	    final float D = utility.waterfall_disk_size * utility.world_size;
 	    if (doStars == true) {
 		    _g2.setTransform(theTransforms.af_translate_zoom);
 		    _g2.setPaint(dark_gradient);
 		    _g2.fillRect((int)-D, (int)0f, (int)(2*D), utility.waterfall_size); //Draw Column
 		    ///Do waterfall
 		    _g2.setClip((int)-D, (int)0f, (int)(2*D), utility.waterfall_size);
 		    _g2.setPaint(light_gradient);
 		    synchronized (theSpriteManger.GetWaterfallSplashObjects()) {
 			    for (WaterfallSplash _w :  theSpriteManger.GetWaterfallSplashObjects()) {
 			    	((WaterfallSplash_Applet)_w).Render(_g2);
 			    }
 		    }
 		    _g2.setTransform(theTransforms.af);
 		    _g2.setClip(null);
 	    }
 	    _g2.setColor(dsea_blue);
 	    _g2.fillOval ((int)(-D),(int)(-D),(int)(D*2),(int)(D*2));
 	    if (background_stars.size() == 0) { //get some stars!
 	    	for (int s=0; s<100; ++s) {
 	    		int radius = Math.round((utility.world_size*utility.waterfall_disk_size*1.1f) + utility.rndI(utility.world_size*4));
 	    		float angle = (float) (utility.rnd() * Math.PI * 2);
 	    		background_stars.add(new Point( (int)Math.round(radius*Math.cos(angle)) , (int)Math.round(radius*Math.sin(angle)) ));
 	    	}
 	    }
 	    if (doStars == true) {
 		    _g2.setTransform(theTransforms.af_translate_zoom);
 		    _g2.setColor(Color.white); //possibility to add stars
 		    for (Point _p : background_stars) {
 				Point2D transform = theTransforms.getTransformedPoint(_p.x, _p.y);
 		    	_g2.fillOval((int)transform.getX(),(int)transform.getY(),3,3);
 		    }
 		    _g2.setTransform(theTransforms.af);
 		}
 	}
 
 	private void drawStartPlayingOption(Graphics2D g2) {
 		g2.setTransform(theTransforms.af_none);
 		g2.setFont(myMediumFont);
 		int x = 10;
 		int y = 10;
 		Color c1 = front_blue;
 		Color c2 = backing_brown;
 		g2.setColor(c1);
 		g2.fillRoundRect(x, y, 970, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x, y, 970, 50, 10, 10);
 		g2.setColor(c2);
 		g2.fillRoundRect(x+3, y+3, 970, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x+3, y+3, 970, 50, 10, 10);
 		g2.setColor(c1);
 		g2.drawString("DO YOU WANT TO PLAY THIS ISLAND?", x+10, y+43);
 		
 		x = 525;
 		y = 70;
 		if (CurMouse != null 
 				&& CurMouse.getX() > x 
 				&& CurMouse.getX() < x+250 
 				&& CurMouse.getY() > y 
 				&& CurMouse.getY() < y+50) {
 			c1 = backing_brown;
 			c2 = front_blue;
 			if (utility.mouseClick == true) {
 				utility._RPSRTS.genNewWorld();
 			}
 		}
 		g2.setColor(c1);
 		g2.fillRoundRect(x, y, 250, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x, y, 250, 50, 10, 10);
 		g2.setColor(c2);
 		g2.fillRoundRect(x+3, y+3, 250, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x+3, y+3, 250, 50, 10, 10);
 		g2.setColor(c1);
 		g2.drawString("RE-ROLL", x+20, y+43);
 		
 		x = 225;
 		y = 70;
 		c1 = front_blue;
 		c2 = backing_brown;
 		if (CurMouse != null 
 				&& CurMouse.getX() > x 
 				&& CurMouse.getX() < x+250 
 				&& CurMouse.getY() > y 
 				&& CurMouse.getY() < y+50) {
 			c1 = backing_brown;
 			c2 = front_blue;
 			if (utility.mouseClick == true) {
 				utility.gameMode = GameMode.gameOn;
 			}
 		}
 		g2.setColor(c1);
 		g2.fillRoundRect(x, y, 250, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x, y, 250, 50, 10, 10);
 		g2.setColor(c2);
 		g2.fillRoundRect(x+3, y+3, 250, 50, 10, 10);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x+3, y+3, 250, 50, 10, 10);
 		g2.setColor(c1);
 		g2.drawString("YES", x+80, y+43);
 		g2.setFont(myFont);
 	}
 
 	private void drawStatBox(Graphics2D _g2, String _s1, String _s2, String _s3, String _s4) {
 		int _x = (int)CurMouse.getX() - con_start_x/2 - x_add*3;
 		int _y = (int)CurMouse.getY() + x_add/2;
 		drawStatBox(_g2, _s1, _s2, _s3, _s4, _x, _y, 500, 15);
 	}
 	
 	private void drawStatBox(Graphics2D _g2, String _s1, String _s2, String _s3, String _s4, int _x, int _y, int box_w, int _os) {
 		int box_h = 70;
 		if (_s2 == "") {
 			box_h /= 4;
 		} else if (_s3 == "") {
 			box_h /= 2;
 		} 
 		_g2.setColor(backing_brown);
 		_g2.fillRect(_x, _y, box_w, box_h);
 		_g2.setColor(Color.white);
 		_g2.drawRect(_x, _y, box_w, box_h);
 		_g2.setColor(front_blue);
 		_g2.drawString(_s1,  _x + _os, _y + (_os * 1));
 		_g2.drawString(_s2,  _x + _os, _y + (_os * 2));
 		_g2.drawString(_s3,  _x + _os, _y + (_os * 3));
 		_g2.drawString(_s4,  _x + _os, _y + (_os * 4));
 	}
 	
 	private void drawTitleScreen(Graphics2D g2) {
 		int x = 100;
 		int y = 100;
 		Rectangle clip1 = new Rectangle(x,     y, 210, 115);
 		Rectangle clip2 = new Rectangle(x+210, y, 210, 115);
 
 		//Title
 		g2.setFont(myBigFont);
 		g2.setTransform(theTransforms.af_none);
 		g2.setClip(clip1);
 		g2.setColor(front_blue);
 		g2.fillRoundRect(x, y, 400, 100, 20, 20);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x, y, 400, 100, 20, 20);
 		g2.setColor(backing_brown);
 		g2.fillRoundRect(x+10, y+10, 400, 100, 20, 20);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x+10, y+10, 400, 100, 20, 20);
 		g2.setColor(front_blue);
 		g2.drawString("RPSRTS", x+30, y+90);
 		g2.setClip(clip2);
 		g2.setColor(backing_brown);
 		g2.fillRoundRect(x, y, 400, 100, 20, 20);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x, y, 400, 100, 20, 20);
 		g2.setColor(front_blue);
 		g2.fillRoundRect(x+10, y+10, 400, 100, 20, 20);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(x+10, y+10, 400, 100, 20, 20);
 		g2.setColor(backing_brown);
 		g2.drawString("RPSRTS", x+34, y+90);
 		g2.setClip(null);
 		
 		boolean clicked = drawButton(g2, 250, 300, 500, "GENERATE ISLAND", 43);
 		if (clicked == true) {
 			utility._RPSRTS.genNewWorld();
 
 		}
 		
 		String cursor = "";
 		if ((System.currentTimeMillis() / 1000L) % 2 == 0) { // if even second
 			cursor = "|";
 		}
 		clicked = drawButton(g2, 175, 400, 650, "ISLAND SEED:"+utility.rndSeedTxt+cursor, 20);
 	}
 	
 	private void drawPausedWindow(Graphics2D g2) {
 		boolean clicked = drawButton(g2, 350, 200, 300, "RESUME", 70);
 		if (clicked == true) {
 			utility.gamePaused = false;
 		}
 		clicked = drawButton(g2, 350, 300, 300, "QUIT", 100);
 		if (clicked == true) {
 			utility._RPSRTS.genNewWorld();
 			utility.doWorldGen = false;
 			utility.gamePaused = false;
 		}
		g2.setFont(myFont);
 	}
 	
 	private boolean drawButton(Graphics2D g2, int _x, int _y, int _w, String _s, int _txt_x_offset) {
 		g2.setFont(myMediumFont);
 		final int _h = 50; //height
 		final int _b = 10; //bevel
 		final int _o = 3; //offset
 		final int _txt_y_offset = 43;
 		Color c1 = front_blue;
 		Color c2 = backing_brown;
 		boolean click = false;
 		if (CurMouse != null 
 				&& CurMouse.getX() > _x 
 				&& CurMouse.getX() < _x+_w 
 				&& CurMouse.getY() > _y 
 				&& CurMouse.getY() < _y+_h) {
 			c1 = backing_brown;
 			c2 = front_blue;
 			if (utility.mouseClick == true) {
 				click = true;
 			}
 		}
 		g2.setColor(c1);
 		g2.fillRoundRect(_x, _y, _w, _h, _b, _b);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(_x, _y, _w, _h, _b, _b);
 		g2.setColor(c2);
 		g2.fillRoundRect(_x+_o, _y+_o, _w, _h, _b, _b);
 		g2.setColor(Color.white);
 		g2.drawRoundRect(_x+_o, _y+_o, _w, _h, _b, _b);
 		g2.setColor(c1);
 		g2.drawString(_s, _x + _txt_x_offset, _y + _txt_y_offset);
 		return click;
 	}
 	
 	private void drawTopBar(Graphics2D _g2) {
 		_g2.setTransform(theTransforms.af_none);
 		_g2.setColor(backing_brown);
 		_g2.drawImage(TopBar, 0, 0, null);
 		_g2.setColor(front_blue);
 
 		theTransforms.TOP_ROTATE += 0.01;
 		if(buildingToPlace != null) {
 	    	if (buildingToPlace == BuildingType.Woodshop) {
 				_g2.translate(con_start_x + (0 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.Rockery) {
 				_g2.translate(con_start_x + (1 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.Smelter) {
 				_g2.translate(con_start_x + (2 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.AttractorPaper) {
 				_g2.translate(con_start_x + (3 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.AttractorRock) {
 				_g2.translate(con_start_x + (4 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.AttractorScissors) {
 				_g2.translate(con_start_x + (5 * x_add), y_height/2);
 			} else if(buildingToPlace == BuildingType.X) {
 				_g2.translate(con_start_x + (6 * x_add), y_height/2);
 			}
 			_g2.fillOval(-20, -20, 40, 40);
     		_g2.rotate(theTransforms.TOP_ROTATE);
 			_g2.fillRect(-17, -17, 34, 34);
     		_g2.rotate(theTransforms.TOP_ROTATE);
 			_g2.fillRect(-17, -17, 34, 34);
 	    	_g2.setTransform(theTransforms.af_none);
 	    }
 
 		_g2.drawString(resource_manager.GetResourceText(), 15, 20);
 		_g2.drawString(resource_manager.GetUnitText(), 15, 40);
 		_g2.drawString("SEED:"+utility.rndSeedTxt+" FPS:"+utility.FPS+" Score:"+resource_manager.GetScore(ObjectOwner.Player), 15, 60);
 
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (0 * x_add), y_height/2);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.woodshop_player, true);
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (1 * x_add), y_height/2);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.rockery_player, true);
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (2 * x_add), y_height/2);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.smelter_player, true);
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (3 * x_add), y_height/2 + 12);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.attractor_paper_player, true);
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (4 * x_add), y_height/2 + 12);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.attractor_rock_player, true);
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (5 * x_add), y_height/2 + 12);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.attractor_scissors_player, true);
 		_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (6 * x_add), y_height/2);
 		_g2.scale(2, 2);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.X, true);
 
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (7 * x_add), y_height/2);
 		_g2.scale(3, 3);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.paper_player, true);
 		if (resource_manager.GetGeneratingPaper(ObjectOwner.Player) == true) {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.on, true);
 		} else {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.off, true);
 		}
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (8 * x_add), y_height/2);
 		_g2.scale(3, 3);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.rock_player, true);
 		if (resource_manager.GetGeneratingRock(ObjectOwner.Player) == true) {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.on, true);
 		} else {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.off, true);
 		}
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(con_start_x + (9 * x_add), y_height/2);
 		_g2.scale(3, 3);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.scissor_player, true);
 		if (resource_manager.GetGeneratingScissors(ObjectOwner.Player) == true) {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.on, true);
 		} else {
 			theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.off, true);
 		}
 		
 		if (buildingStatBox != 0) {
 			_g2.setTransform(theTransforms.af_none);
 	    	if(buildingStatBox == 1) {
 	    		drawStatBox(_g2, 
 	    				"The Woodshop costs "+utility.COST_Woodshop_Iron+" Iron.", 
 	    				"Nearby wood is collected by Scissors and brought to the Woodshop.",
 	    				"The woodshop converts "+utility.COST_Paper_Wood+" wood into Paper.",
 	    				"Each additional Woodshop can support "+utility.EXTRA_Paper_PerWoodmill+" Paper.");
 	    	} else if(buildingStatBox == 2) {
 	    		drawStatBox(_g2, 
 	    				"The Rockery costs "+utility.COST_Rockery_Wood+" Wood to build.", 
 	    				"Nearby Stone is collected by Paper and brought to the Rockery.",
 	    				"The Rockery converts "+utility.COST_Rock_Stone+" Stone into Rock.",
 	    				"Each additional Rockery can support "+utility.EXTRA_Rock_PerRockery+" Rocks.");
 			} else if(buildingStatBox == 3) {
 	    		drawStatBox(_g2, 
 	    				"The Smelter costs "+utility.COST_Smelter_Stone+" Stone to build.", 
 	    				"Nearby Iron is collected by Rocks and brought to the Smelter.",
 	    				"The Smelter converts "+utility.COST_Scissors_Iron+" Iron into Scissors.",
 	    				"Each additional Smelter can support "+utility.EXTRA_Scissors_PerSmelter+" Scissors.");
 			} else if(buildingStatBox == 4) {
 	    		drawStatBox(_g2, 
 	    				"The Totem of Paper Costs "+utility.COST_AttractorPaper_Wood+" Wood to build.", 
 	    				"It attracks your Paper towards it.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 5) {
 	    		drawStatBox(_g2, 
 	    				"The Totem of Rocks Costs "+utility.COST_AttractorRock_Stone+" Stone to build.", 
 	    				"It attracks your Rocks towards it.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 6) {
 	    		drawStatBox(_g2, 
 	    				"The Totem of Scissors Costs "+utility.COST_AttractorScissors_Iron+" Iron to build.", 
 	    				"It attracks your Scissors towards it.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 7) {
 	    		drawStatBox(_g2, 
 	    				"Remove a building.", 
 	    				"This will return "+(utility.building_refund_amount*100)+"% of the building costs.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 8) {
 	    		drawStatBox(_g2, 
 	    				"Toggle Paper Production.", 
 	    				"When Green, your Woodshops will output Paper up to your maximum.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 9) {
 	    		drawStatBox(_g2, 
 	    				"Toggle Rock Production.", 
 	    				"When Green, your Rockery will output Rocks up to your maximum.",
 	    				"",
 	    				"");
 			} else if(buildingStatBox == 10) {
 	    		drawStatBox(_g2, 
 	    				"Toggle Scissor Production.", 
 	    				"When Green, your Smelters will output Scissors up to your maximum.",
 	    				"",
 	    				"");
 			}
 		}	
 	}
 	
     private void drawTopText(Graphics2D _g2, String _str) {
 		_g2.setTransform(theTransforms.af_none);
 		_g2.setColor(Color.white);
 		_g2.setFont(myMediumFont);
 		_g2.drawString(_str, 20, 580);
 		_g2.setFont(myFont);
 	}
     
     private void drawBottomBar(Graphics2D _g2) {
     	_g2.setTransform(theTransforms.af_none);
 		_g2.translate(utility.window_X - 130, utility.window_Y - 30);
 		_g2.scale(3, 3);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.sound, true);
 		_g2.translate(12, 0);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.quality, true);
 		_g2.translate(12, 0);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.pause, true);
 		_g2.translate(12, 0);
 		theSpriteManger.SpecialRender(_g2, 0, 0, theBitmaps.ff, true);
 		_g2.setTransform(theTransforms.af_none);
 		_g2.setFont(myFont);
 		int _x = 0;
 		int _y = 0;
 		if (CurMouse != null) {
 			_x=(int)CurMouse.getX();
 			_y=(int)CurMouse.getY() - 40;
 		}
 		if (buildingStatBox == 100) {
 			if (utility.soundOn) {
 				drawStatBox(_g2, "Click to mute.", "", "", "", _x - 65, _y, 135, 13);
 			} else {
 				drawStatBox(_g2, "Click to un-mute.", "", "", "", _x - 65, _y, 135, 13);
 			}
 		} else if (buildingStatBox == 101) {
 			if (utility.highQuality == true) {
 				drawStatBox(_g2, "Click to decrease quality.", "", "", "", _x - 110, _y, 200, 13);
 			} else {
 				drawStatBox(_g2, "Click to increase quality.", "", "", "", _x - 110, _y, 200, 13);
 			}
 		} else if (buildingStatBox == 102) {
 			if (utility.gamePaused == false) {
 				drawStatBox(_g2, "Click to pause.", "", "", "", _x - 90, _y, 145, 13);
 			} else {
 				drawStatBox(_g2, "Click to un-pause.", "", "", "", _x - 90, _y, 145, 13);
 			}		
 		} else if (buildingStatBox == 103) {
 			if (utility.fastForward == false) {
 				drawStatBox(_g2, "Click to fast-forward.", "", "", "", _x - 210, _y, 230, 13);
 			} else {
 				drawStatBox(_g2, "Click to play at normal speed.", "", "", "", _x - 210, _y, 230, 13);
 			}		
 		}
 
     }
     
     public void mouseClick() {
 	    if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (0 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (0 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.Woodshop) buildingToPlace = null;
 			else buildingToPlace = BuildingType.Woodshop;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (1 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (1 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.Rockery) buildingToPlace = null;
 			else buildingToPlace = BuildingType.Rockery;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (2 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (2 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.Smelter) buildingToPlace = null;
 			else buildingToPlace = BuildingType.Smelter;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (3 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (3 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.AttractorPaper) buildingToPlace = null;
 			else buildingToPlace = BuildingType.AttractorPaper;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (4 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (4 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.AttractorRock) buildingToPlace = null;
 			else buildingToPlace = BuildingType.AttractorRock;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (5 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (5 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.AttractorScissors) buildingToPlace = null;
 			else buildingToPlace = BuildingType.AttractorScissors;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (6 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (6 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			if (buildingToPlace == BuildingType.X) buildingToPlace = null;
 			else buildingToPlace = BuildingType.X;
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (7 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (7 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			resource_manager.SetGeneratingPaper(ObjectOwner.Player, !resource_manager.GetGeneratingPaper(ObjectOwner.Player) );
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (8 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (8 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			resource_manager.SetGeneratingRock(ObjectOwner.Player, !resource_manager.GetGeneratingRock(ObjectOwner.Player) );
 		} else if (utility.gameMode == GameMode.gameOn && CurMouse.getX() > (con_start_x + (9 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (9 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			resource_manager.SetGeneratingScissors(ObjectOwner.Player, !resource_manager.GetGeneratingScissors(ObjectOwner.Player) );
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (0*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (1*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			utility.soundOn = !utility.soundOn;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (1*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (2*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			utility.highQuality = !utility.highQuality;
 			theTransforms.setAA(utility.highQuality);
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (2*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (3*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			utility.gamePaused = !utility.gamePaused;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (3*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (4*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			utility.fastForward = !utility.fastForward;
 			if (utility.fastForward == true) {
 				utility.SetTicksPerRender(utility.GetTicksPerRender() * utility.fast_forward_speed);
 			} else {
 				utility.SetTicksPerRender(utility.game_ticks_per_render);
 			}
 		} else {
 			utility.mouseClick = true;
 
 		}
 	}
 	
 	public void mouseMove() {
 		if (CurMouse.getX() > (con_start_x + (0 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (0 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 1;
 		} else if (CurMouse.getX() > (con_start_x + (1 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (1 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 2;
 		} else if (CurMouse.getX() > (con_start_x + (2 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (2 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 3;
 		} else if (CurMouse.getX() > (con_start_x + (3 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (3 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 4;
 		} else if (CurMouse.getX() > (con_start_x + (4 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (4 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 5;
 		} else if (CurMouse.getX() > (con_start_x + (5 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (5 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 6;
 		} else if (CurMouse.getX() > (con_start_x + (6 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (6 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 7;
 		} else if (CurMouse.getX() > (con_start_x + (7 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (7 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 8;
 		} else if (CurMouse.getX() > (con_start_x + (8 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (8 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 9;
 		} else if (CurMouse.getX() > (con_start_x + (9 * x_add) - x_add/2) && CurMouse.getX() < (con_start_x + (9 * x_add) + x_add/2) && CurMouse.getY() > (y_height/2 - x_add/2) && CurMouse.getY() <  (y_height/2 + (x_add/2)) ) {
 			buildingStatBox = 10;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (0*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (1*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			buildingStatBox = 100;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (1*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (2*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			buildingStatBox = 101;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (2*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (3*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			buildingStatBox = 102;
 		} else if (CurMouse.getX() > (utility.window_X - 164 + (3*3*12)) && CurMouse.getX() < (utility.window_X - 164 + (4*12*3)) && CurMouse.getY() > (utility.window_Y - 64) && CurMouse.getY() <  (utility.window_Y - 64 + (12*3)) ) {
 			buildingStatBox = 103;
 		} else {
 			buildingStatBox = 0;
 		}
 	}
 	
 	public void sceneBlackout(Graphics2D g2) {
 		g2.setTransform(theTransforms.af_none);
 		g2.setColor (Color.black);
 		g2.fillRect (0, 0, utility.window_X, utility.window_Y);
 	}
 	
 	public void sceneGame(Graphics2D g2) {
 		g2.setTransform(theTransforms.af);
     	drawBufferedBackground(g2);
 	    theSpriteManger.Render(g2);
 	    if (buildingToPlace != null) {
 	    	final boolean placed = theSpriteManger.TryPlaceItem(buildingToPlace, g2, (int)MouseTF.getX(), (int)MouseTF.getY(), utility.mouseClick);
 	    	if (placed == true) {
 	    		buildingToPlace = null;
 	    	}
 	    } else if (buildingToMove != null) {
 	    		buildingToMove.MoveBuilding((int)MouseTF.getX(), (int)MouseTF.getY());
         } else if (utility.sendMouseDragPing == true) {
 	    	//am i over a building?
 	    	buildingToMove = theSpriteManger.GetBuildingAtMouse((int)MouseTF.getX(), (int)MouseTF.getY());
 	    	if (buildingToMove != null) {
 	    		if (buildingToMove.getiAttract().size() == 0) buildingToMove = null; //Only totems attract
 	    	}
         }
 	    drawTopBar(g2);
     	drawBottomBar(g2);
     	if (utility.gamePaused == true) {
     		drawPausedWindow(g2);
     	}
 	}
 	
 	public void sceneGameOver(Graphics2D g2) {
     	drawBufferedBackground(g2);
 	    theSpriteManger.Render(g2);
 	    //drawTopBar(g2);
 	    if ( (System.currentTimeMillis() / 1000l) - utility.loose_time > 3) {
 	    	drawEndScreen(g2);
 	    }
     	drawBottomBar(g2);
 	}
 	
 	public void sceneTitle(Graphics2D g2) {
 		g2.setTransform(theTransforms.af);
     	contentCreation(g2);
     	if (utility.doWorldGen == false) {
     		drawTitleScreen(g2);
     	} else if (theSpriteManger.GetWorldSeeded() == true) {
     		drawStartPlayingOption(g2);
     	}
     	drawBottomBar(g2);
 	}
 }
