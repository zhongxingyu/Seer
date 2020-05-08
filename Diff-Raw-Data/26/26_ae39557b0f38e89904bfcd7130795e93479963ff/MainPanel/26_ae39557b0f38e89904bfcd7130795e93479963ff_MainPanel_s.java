 package de.tu_darmstadt.gdi1.samegame.gameframes;
 
 import java.awt.Component;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import de.tu_darmstadt.gdi1.samegame.GameController;
 import de.tu_darmstadt.gdi1.samegame.Level;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.InvalidOperationException;
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 
 import de.tu_darmstadt.gdi1.samegame.ui.GamePanel;
 import de.tu_darmstadt.gdi1.samegame.ui.GameWindow;
 
 @SuppressWarnings("serial")
 public class MainPanel extends GamePanel{
 
 	private Level level;
 	private GameController controller;
 
 	private int markedRow, markedCol;
 
 	private boolean duringAnimation;
 
 	private Byte[][] field;
 
 	public MainPanel(GameWindow window, Level level, GameController controller){
 		super(window);
 
 		this.level = level;
 
 		this.field = level.getFieldState();
 
 		this.controller = controller;
 
 		try{
 			String path = this.getClass().
 				getResource("../../resources/images/defaultskin").toString();
 			
 			registerImage("1", new URL(path+"/colorone.png"));
 			registerImage("2", new URL(path+"/colortwo.png"));
 			registerImage("3", new URL(path+"/colorthree.png"));
 			registerImage("4", new URL(path+"/colorfour.png"));
 			registerImage("5", new URL(path+"/colorfive.png"));
 			registerImage("0", new URL(path+"/empty.png"));
 		}catch(ParameterOutOfRangeException e){
 			e.printStackTrace();
 		}catch(InvalidOperationException e){
 			e.printStackTrace();
 		}catch(MalformedURLException e){
 			e.printStackTrace();
 		}
 
 		markedRow = markedCol = 0;
 
 		setAutosize(true);
 
 		duringAnimation = false;
 	}
 
 	public void markField(int row, int col){
 		if(row >= 0 || row < level.getFieldHeight() ||
 		   col >= 0 || col < level.getFieldWidth()){
 			markedRow = row;
 			markedCol = col;
 		}
 	}
 
 
 	public int getMarkedFieldRow(){
 		return markedRow;
 	}
 
 
 	public int getMarkedFieldCol(){
 		return markedCol;
 	}
 
 
 	public boolean duringAnimation(){
 		return this.duringAnimation;
 	}
 
 	void endAnimation(){
 		duringAnimation = false;
 		this.field = level.getFieldState();
 		try{
 			this.redraw();
 		}catch(InternalFailureException e){
 			e.printStackTrace();
 		}
 	}
 
 	public void startAnimation(int row, int col, long animationSpeed){
 		duringAnimation = true;
 
		Level.removeFloodFill(this.field, row, col, field[row][col], new Integer(0));
 
 		AnimationThread animation = new AnimationThread(this.field, animationSpeed, this);
 		animation.start();
 	}
 
 
 	@Override
 	public void setGamePanelContents(){
 		// if an animation is performed, let the AnimationThread handle the
 		// field state
 		if(!duringAnimation)
 			this.field = level.getFieldState();
 	
 		int width = level.getFieldWidth();
 		int height = level.getFieldHeight();
 		for(int i=0; i<height; i++)
 			for(int j=0; j<width; j++)
 				try{
 					placeEntity(""+field[i][j]);
 				}catch(ParameterOutOfRangeException e){
 					System.err.println(e.getMessage());
 					e.printStackTrace();
 				}
 	}
 
 	@Override
 	public void panelResized(){
 		// TODO fill method stub
 	}
 
 	@Override
 	public void entityClicked(int positionX, int positionY){
 		getParentWindow().requestFocus();
 
 		if(!duringAnimation && level.removeable(positionY, positionX)){
 			try{
 				startAnimation(positionY, positionX, 500);
 				level.removeStone(positionY, positionX);
 				this.redraw();
 			}catch(ParameterOutOfRangeException e){
 				e.printStackTrace();
 			}catch(InternalFailureException e){
 				e.printStackTrace();
 			}
 		}
 	}
 }
