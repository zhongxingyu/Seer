 /* 
  * Copyright 2012 Bryan Wyatt
  * 
  * This file is part of BadScience!.
  *  
  * BadScience! is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * BadScience! is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with BadScience!.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.junglecatsoftware.badscience.levels.levels;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Random;
 
 import com.junglecatsoftware.badscience.drawables.FloorTile;
 import com.junglecatsoftware.badscience.drawables.GridOverlay;
 import com.junglecatsoftware.badscience.drawables.PauseMenuOverlayBackground;
 import com.junglecatsoftware.badscience.drawables.Player;
 import com.junglecatsoftware.badscience.drawables.WallTile;
 import com.junglecatsoftware.brge.BRGE;
 import com.junglecatsoftware.brge.Game;
 import com.junglecatsoftware.brge.graphics.ScreenObjects;
 import com.junglecatsoftware.brge.graphics.drawables.BlackBackground;
 import com.junglecatsoftware.brge.graphics.drawables.Drawable;
 import com.junglecatsoftware.brge.graphics.drawables.LevelGridDrawable;
 import com.junglecatsoftware.brge.graphics.drawables.MenuItem;
 import com.junglecatsoftware.brge.levelgrid.LevelGrid;
 import com.junglecatsoftware.brge.levelgrid.LevelGridSquare;
 import com.junglecatsoftware.brge.levels.Level;
 
 
 
 public class TestLevel extends Level{
 	
 	private boolean run=true;
 	private boolean pause=false;
 	private Thread t;
 	private GridOverlay overlay;
 	private Player player;
 	private boolean showOverlay;
 	private boolean startShiftLeft;
 	private boolean startShiftRight;
 	private boolean startShiftUp;
 	private boolean startShiftDown;
 	private boolean shiftingLeft;
 	private boolean shiftingRight;
 	private boolean shiftingUp;
 	private boolean shiftingDown;
 	
 	private LevelGrid levelGrid;
 	private LevelGridSquare centerGridSquare;
 
 	//stuff for the menu
 	private ArrayList<MenuItem> menuItems;
 	private int selectedMenuItem=0;
 	private boolean menuSelected=false; //Ensure that menu items can't be triggered if the spacebar was pressed prior to the menu loading
 	
 	public TestLevel(Game g, ScreenObjects so){
 		super(g, so);
 		levelGrid=new LevelGrid(BRGE.getWidth(),BRGE.getHeight());
 	}
 	public TestLevel(Game g, ScreenObjects so, LevelGrid lg){
 		super(g, so);
 		levelGrid=lg;
 	}
 
 	public void startLevel() {
 		screenObjects.clear();
 		levelGrid.resetGrid();
 
 		screenObjects.addToBottom(new BlackBackground());
 		
 		for(int y=0;y<levelGrid.getGridHeight();y+=1){
 			for(int x=0;x<levelGrid.getGridWidth();x+=1){
 				LevelGridSquare square=levelGrid.getGridSquare(x, y);
 				loadTile(square);
 			}
 		}
 		
 		screenObjects.addToTop(levelGrid);
 		centerGridSquare=levelGrid.getGridSquare((levelGrid.getGridWidth()-1)/2,(levelGrid.getGridHeight())/2);
 		player=new Player(centerGridSquare.copy(), Color.BLACK);
 		levelGrid.setPlayerDrawable(player);
 		
 		overlay=new GridOverlay(levelGrid);
 		showOverlay=false;
 		//screenObjects.addToTop(overlay);
 
 		t=new Thread(){ public void run(){ runGame(); }};
 		t.start();
 	}
 	public void endLevel() {
 		run=false;
 		pause=false;
 		while(t.isAlive()){
 			wait(1);
 		}
 		screenObjects.clear();
 		levelGrid.resetGrid();
 		
 		shiftingLeft=false;
 		shiftingRight=false;
 		shiftingUp=false;
 		shiftingDown=false;
 	}
 	public void keyPressed(int key) {
 		switch(key){
 			case KeyEvent.VK_LEFT:
 				startShiftRight=true;
 				break;
 			case KeyEvent.VK_RIGHT:
 				startShiftLeft=true;
 				break;
 			case KeyEvent.VK_UP:
 				if(pause){
 					menuUp();
 				}
 				startShiftDown=true;
 				break;
 			case KeyEvent.VK_DOWN:
 				if(pause){
 					menuDown();
 				}
 				startShiftUp=true;
 				break;
 			case KeyEvent.VK_SPACE:
 				if(pause){
 					menuSelected=true;
 				}
 				break;
 			case KeyEvent.VK_ENTER:
 				if(pause){
 					menuSelected=true;
 				}
 				break;
 		}
 	}
 	public void keyReleased(int key) {
 		switch(key){
 			case KeyEvent.VK_LEFT:
 				startShiftRight=false;
 				break;
 			case KeyEvent.VK_RIGHT:
 				startShiftLeft=false;
 				break;
 			case KeyEvent.VK_UP:
 				startShiftDown=false;
 				break;
 			case KeyEvent.VK_DOWN:
 				startShiftUp=false;
 				break;
 			case KeyEvent.VK_SPACE:
 				if(pause){
 					if(menuSelected){
 						menuSelected=false;
 						menuActivated();
 					}
 				}
 				break;
 			case KeyEvent.VK_ESCAPE:
 				if(pause){
 					menuSelected=false;
 					pause=false;
 				}else{
 					pause=true;
 				}
 				break;
 			case KeyEvent.VK_ENTER:
 				if(pause){
 					if(menuSelected){
 						menuSelected=false;
 						menuActivated();
 					}
 				}
 				break;
 			case KeyEvent.VK_G:
 				if(!pause){
 					if(showOverlay){
 						showOverlay=false;
 						screenObjects.remove(overlay);
 					}else{
 						showOverlay=true;
 						screenObjects.addToTop(overlay);
 					}
 				}
 				break;
 		}
 	}
 	public void keyTyped(int key) {
 		switch(key){
 		}
 	}
 	
 	private void runGame(){
 		run=true;
 		int counter=1; //tick-tock counter
 		while(run){
 			
 			if(pause){//if game has been paused
 				//create and display menu
 				PauseMenuOverlayBackground bg=new PauseMenuOverlayBackground();
 				screenObjects.addToTop(bg);
 				
 				menuItems=new ArrayList<MenuItem>();
 				menuItems.add(new MenuItem(1, "Toggle DrawFPS"));
 				menuItems.add(new MenuItem(2, "Draw Shadows ("+BRGE.getDrawShadows()+")"));
 				menuItems.add(new MenuItem(4, true, "Exit to Main Menu"));
 				for(MenuItem item : menuItems){
 					screenObjects.addToTop(item);
 				}
				selectedMenuItem=1;
 				
 				while(pause){
 					wait(10);
 				}
 				
 				//remove menu from screen
 				screenObjects.remove(bg);
 				for(MenuItem item : menuItems){
 					screenObjects.remove(item);
 				}
 			}
 			
 			if(!(shiftingLeft||shiftingRight||shiftingUp||shiftingDown)){
 				if(startShiftLeft&&(!startShiftRight)){
 					shiftingLeft=true;
 				}else if(startShiftRight&&(!startShiftLeft)){
 					shiftingRight=true;
 				}
 				if(startShiftUp&&(!startShiftDown)){
 					shiftingUp=true;
 				}else if(startShiftDown&&(!startShiftUp)){
 					shiftingDown=true;
 				}
 				counter=1;
 			}
 			
 			//Only update 50 times/second, and only when actually moving
 			int stepRegulator=20;
 			int stepCount=(1000/stepRegulator);
 			if((counter%stepRegulator==0)&&(shiftingLeft||shiftingRight||shiftingUp||shiftingDown)){
 				if(shiftingRight||(shiftingDown&&(!shiftingLeft))){//use up then left navigation
 					for(int i=levelGrid.getGridWidth()-1;i>=0;i--){
 						for(int j=levelGrid.getGridHeight()-1;j>=0;j--){
 							//System.out.println("U-L: ("+i+","+j+")");
 							shift(counter,stepCount,i,j);
 						}
 					}
 				}else{//Use down then right navigation
 					for(int i=0;i<levelGrid.getGridWidth();i++){
 						for(int j=0;j<levelGrid.getGridHeight();j++){
 							//System.out.println("D-R: ("+i+","+j+")");
 							shift(counter,stepCount,i,j);
 						}
 					}
 				}
 			}
 			
 			if(counter==1000){//reset shifting
 				
 				if(shiftingLeft||shiftingRight){
 					LevelGridSquare square;
 					int x=0;
 					if(shiftingLeft){
 						square = levelGrid.getGridSquare(levelGrid.getGridWidth()-1,0);
 						x=levelGrid.getGridWidth()-1;
 					}else{
 						square = levelGrid.getGridSquare(0,0);
 					}
 					int max=levelGrid.getGridHeight();
 					int i=1;
 					if(shiftingUp){
 						//if we are also moving up, skip the corners, the other loop will add them!
 						max--;
 					}
 					if(!shiftingDown){
 						//if we are shifting down, we don't want to add the corners, the other loop will do that!
 						loadTile(square);
 					}
 					for(;i<max;i++){
 						//System.out.println("LEFT/RIGHT: ("+x+","+i+")");
 						square=square.getBelow();
 						loadTile(square);
 					}
 					if(showOverlay){
 						screenObjects.remove(overlay);
 						screenObjects.addToTop(overlay);
 					}
 				}
 				if(shiftingUp||shiftingDown){
 					LevelGridSquare square;
 					int y=0;
 					if(shiftingUp){
 						square = levelGrid.getGridSquare(0,levelGrid.getGridHeight()-1);
 						y=levelGrid.getGridHeight()-1;
 					}else{
 						square = levelGrid.getGridSquare(0,0);
 					}
 					loadTile(square);
 					int max=levelGrid.getGridWidth();
 					int i=1;
 					for(;i<max;i++){
 						//System.out.println("UP/DOWN: ("+i+","+y+")");
 						square=square.getRight();
 						loadTile(square);
 					}
 					if(showOverlay){
 						screenObjects.remove(overlay);
 						screenObjects.addToTop(overlay);
 					}
 				}
 				shiftingLeft=false;
 				shiftingRight=false;
 				shiftingUp=false;
 				shiftingDown=false;
 			}
 			
 			wait(1);//pause
 			//tick-tock counter incrementing
 			if(counter<1000){
 				counter++;
 			}else{
 				counter=1;
 			}
 		}
 	}
 	private void shift(int counter,int numSteps,int gridX,int gridY){
 		LevelGridSquare curLoc=levelGrid.getGridSquare(gridX, gridY);
 		LevelGridSquare nextLoc=curLoc;
 
 		if(shiftingLeft){
 			if(gridX!=0){
 				nextLoc=nextLoc.getLeft();
 			}else{
 				this.clearObjects(curLoc);
 			}
 		}else if(shiftingRight){
 			if(gridX!=levelGrid.getGridWidth()-1){
 				nextLoc=nextLoc.getRight();
 			}else{
 				this.clearObjects(curLoc);
 			}
 		}
 		if(shiftingUp){
 			if(gridY!=0){
 				nextLoc=nextLoc.getAbove();
 			}else{
 				this.clearObjects(curLoc);
 			}
 		}else if(shiftingDown){
 			if(gridY!=levelGrid.getGridHeight()-1){
 				nextLoc=nextLoc.getBelow();
 			}else{
 				this.clearObjects(curLoc);
 			}
 		}
 		//System.out.println("("+i+", "+j+")");
 		
 		//get total distance for all components
 		double topLeftDistX=(nextLoc.getTopLeft().getX()-curLoc.getTopLeft().getX())/((double)numSteps);
 		double topLeftDistY=(nextLoc.getTopLeft().getY()-curLoc.getTopLeft().getY())/((double)numSteps);
 		double topRightDistX=(nextLoc.getTopRight().getX()-curLoc.getTopRight().getX())/((double)numSteps);
 		double topRightDistY=(nextLoc.getTopRight().getY()-curLoc.getTopRight().getY())/((double)numSteps);
 		double bottomLeftDistX=(nextLoc.getBottomLeft().getX()-curLoc.getBottomLeft().getX())/((double)numSteps);
 		double bottomLeftDistY=(nextLoc.getBottomLeft().getY()-curLoc.getBottomLeft().getY())/((double)numSteps);
 		double bottomRightDistX=(nextLoc.getBottomRight().getX()-curLoc.getBottomRight().getX())/((double)numSteps);
 		double bottomRightDistY=(nextLoc.getBottomRight().getY()-curLoc.getBottomRight().getY())/((double)numSteps);
 		
 		//System.out.println(curLoc.getObjects().size());
 		for(int k=0;k<curLoc.getAllObjects().size();k++){
 			LevelGridSquare realLoc=curLoc.getAllObjects().get(k).getGridSquare();
 			
 			if(counter==1000){
 				LevelGridDrawable object=curLoc.getAllObjects().get(k);
 				if(curLoc.getGroundObjects().remove(object)){
 					nextLoc.getGroundObjects().add(object);
 				}else if(curLoc.getMiddleObjects().remove(object)){
 					nextLoc.getMiddleObjects().add(object);
 				}else if(curLoc.getGroundObjects().remove(object)){
 					nextLoc.getMiddleObjects().add(object);
 				}//else: wtf?
 				
 				object.setGridSquare(nextLoc.copy());
 			}else{
 				realLoc.getTopLeft().setX(realLoc.getTopLeft().getRealX()+topLeftDistX);
 				realLoc.getTopLeft().setY(realLoc.getTopLeft().getRealY()+topLeftDistY);
 				realLoc.getTopRight().setX(realLoc.getTopRight().getRealX()+topRightDistX);
 				realLoc.getTopRight().setY(realLoc.getTopRight().getRealY()+topRightDistY);
 				realLoc.getBottomLeft().setX(realLoc.getBottomLeft().getRealX()+bottomLeftDistX);
 				realLoc.getBottomLeft().setY(realLoc.getBottomLeft().getRealY()+bottomLeftDistY);
 				realLoc.getBottomRight().setX(realLoc.getBottomRight().getRealX()+bottomRightDistX);
 				realLoc.getBottomRight().setY(realLoc.getBottomRight().getRealY()+bottomRightDistY);
 			}
 			//System.out.println(counter+": ( ("+
 			//		realLoc.getTopLeft().getRealX()+","+realLoc.getTopLeft().getRealY()+"), ("+
 			//		realLoc.getTopRight().getRealX()+","+realLoc.getTopRight().getRealY()+"), ("+
 			//		realLoc.getBottomLeft().getRealX()+","+realLoc.getBottomLeft().getRealY()+"), ("+
 			//		realLoc.getBottomRight().getRealX()+","+realLoc.getBottomRight().getRealY()+") )");
 		}
 	}
 	private void clearObjects(LevelGridSquare square){
 		//clear the objects
 		ArrayList<LevelGridDrawable> middleObjects = square.getMiddleObjects();
 		for(LevelGridDrawable d : middleObjects){
 			if(d instanceof WallTile){
 				//check above to see if we need to inform walls to render their front
 				try{
 					LevelGridSquare above=square.getAbove();
 					for(LevelGridDrawable d2 : above.getMiddleObjects()){
 						if(d2 instanceof WallTile){
 							((WallTile)d2).setRenderFront(true);
 						}
 					}
 				}catch(Exception e){
 				}
 				//check left to see if we need to inform walls to render their right
 				try{
 					LevelGridSquare left=square.getLeft();
 					for(LevelGridDrawable d2 : left.getMiddleObjects()){
 						if(d2 instanceof WallTile){
 							((WallTile)d2).setRenderRight(true);
 						}
 					}
 				}catch(Exception e){
 				}
 				//check right to see if we need to inform walls to render their left
 				try{
 					LevelGridSquare right=square.getRight();
 					for(LevelGridDrawable d2 : right.getMiddleObjects()){
 						if(d2 instanceof WallTile){
 							((WallTile)d2).setRenderLeft(true);
 						}
 					}
 				}catch(Exception e){
 				}
 			}
 			screenObjects.remove(d);
 		}
 		square.clearAllObjects();
 	}
 	private void loadTile(LevelGridSquare square){
 		Random rand=new Random();
 		LevelGridDrawable tile;
 		if(rand.nextInt(4)==0){
 			tile=new WallTile(square,new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
 			
 			//check below to see if we need to render the front
 			try{
 				LevelGridSquare below=square.getBelow();
 				for(LevelGridDrawable d : below.getMiddleObjects()){
 					if(d instanceof WallTile){
 						((WallTile)tile).setRenderFront(false);
 						break;
 					}
 				}
 			}catch(Exception e){
 			}
 			//check above to see if we need to inform walls to not render their front
 			try{
 				LevelGridSquare above=square.getAbove();
 				for(LevelGridDrawable d : above.getMiddleObjects()){
 					if(d instanceof WallTile){
 						((WallTile)d).setRenderFront(false);
 					}
 				}
 			}catch(Exception e){
 			}
 			//check left to see if we need to render the left and inform walls to not render their right
 			try{
 				LevelGridSquare left=square.getLeft();
 				for(LevelGridDrawable d : left.getMiddleObjects()){
 					if(d instanceof WallTile){
 						((WallTile)d).setRenderRight(false);
 						((WallTile)tile).setRenderLeft(false);
 					}
 				}
 			}catch(Exception e){
 			}
 			//check right to see if we need to render the right and inform walls to not render their left
 			try{
 				LevelGridSquare right=square.getRight();
 				for(LevelGridDrawable d : right.getMiddleObjects()){
 					if(d instanceof WallTile){
 						((WallTile)d).setRenderLeft(false);
 						((WallTile)tile).setRenderRight(false);
 					}
 				}
 			}catch(Exception e){
 			}
 			square.getMiddleObjects().add(tile);
 		}else{
 			tile=new FloorTile(square,new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
 			square.getGroundObjects().add(tile);
 		}
 	}
 	@SuppressWarnings("static-access")
 	public static void wait(int millis){
 		try {
 			Thread.currentThread().sleep(millis);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	private void menuUp(){
 		menuItems.get(selectedMenuItem).setSelected(false);
 		selectedMenuItem--;
 		if(selectedMenuItem<0){
 			selectedMenuItem=menuItems.size()-1;
 		}
 		menuItems.get(selectedMenuItem).setSelected(true);
 	}
 	private void menuDown(){
 		menuItems.get(selectedMenuItem).setSelected(false);
 		selectedMenuItem++;
 		if(selectedMenuItem>=menuItems.size()){
 			selectedMenuItem=0;
 		}
 		menuItems.get(selectedMenuItem).setSelected(true);
 	}
 	private void menuActivated(){
 		switch(selectedMenuItem){
 			case 0:
 				BRGE.toggleFPS();
 				break;
 			case 1:
 				BRGE.toggleShadows();
 				menuItems.get(selectedMenuItem).setText("Draw Shadows ("+BRGE.getDrawShadows()+")");
 				break;
 			case 2:
 				game.loadLevel(0);
 				break;
 		}
 	}
 }
