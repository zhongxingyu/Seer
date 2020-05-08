 package de.tu_darmstadt.gdi1.samegame.controller;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 
 import static java.awt.event.KeyEvent.*;
 
 import java.io.File;
 
 import java.util.Locale;
 
 import javax.swing.JButton;
 import javax.swing.JMenuItem;
 
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.ParameterOutOfRangeException;
 
 import de.tu_darmstadt.gdi1.samegame.model.Level;
 import de.tu_darmstadt.gdi1.samegame.view.SameGameViewer;
 
 import de.tu_darmstadt.gdi1.samegame.controller.AbstractController;
 
 import de.tu_darmstadt.gdi1.samegame.exceptions.InternalFailureException;
 import de.tu_darmstadt.gdi1.samegame.view.gameframes.MainPanel;
 
 public class GameController extends AbstractController{
 
 	private SameGameViewer viewer;
 
 	public GameController(Level level){
 		this.level = level;
 	}
 
 	public GameController(Level level, SameGameViewer viewer){
 		this.level = level;
 		this.viewer = viewer;
 	}
 
 	public void setLevel(Level level){
 		this.level = level;
 	}
 	
 	@Override
 	public void menuClick(JMenuItem menuItem){
 		String menuName = menuItem.getName();
 		if (menuName.equals("GameMenu_RestartLvl") )
 			level.restartLevel();
 		if (menuName.equals("FileMenu_GenerateLevel")){
 			viewer.closeMainFrame();
 			viewer.setLevel(level);
 			level.generateLevel(10, 10, 5, 3);
			viewer.showMainFrame();
			viewer.notifyLevelLoaded();
 		}
 		if (menuName.equals("Skin_Default"))
 			viewer.setSkin("defaultskin", Color.white, Color.black, Color.LIGHT_GRAY);
 		if (menuName.equals("Skin_Tuskin"))
 			viewer.setSkin("tuskin", Color.black, Color.white, Color.black);
 		if (menuName.equals("Skin_Squareskin"))
 			viewer.setSkin("squareskin", Color.white, Color.black, Color.white);
 		if (menuName.equals("Skin_Ballskin"))
 			viewer.setSkin("ballskin", Color.black, Color.white, Color.black);
 		if (menuName.equals("Skin_Jewelskin"))
 			viewer.setSkin("jewelskin", Color.white, Color.black, Color.white);
 		if (menuName.equals("FileMenu_SaveLevel"))
 			viewer.showFileChooseDialog("SaveLevel");
 		if (menuName.equals("FileMenu_LoadLevel"))
 			viewer.showFileChooseDialog("LoadLevel");
 		if (menuName.equals("FileMenu_Exit"))
 			viewer.closeMainFrame();
 		if (menuName.equals("GameMenu_Undo"))
 			level.undo();
 		if (menuName.equals("GameMenu_Redo"))
 			level.redo();
 		if (menuName.equals("German"))
 			viewer.setLanguage(new Locale("de", "DE"));
 		if (menuName.equals("English"))
 			viewer.setLanguage(new Locale("en", "US"));
 		if (menuName.equals("Polish"))
 			viewer.setLanguage(new Locale("pl", "PL"));
 		if (menuName.equals("About"))
 			viewer.showAboutFrame();
 		if (menuName.equals("FileMenu_GenerateCustomLevel"))
 			//viewer.showCustomizeFrame();
 		if (menuName.equals("FileMenu_SaveGameState"))
 			viewer.showFileChooseDialog("SaveGameState");
 		if (menuName.equals("FileMenu_LoadGameState"))
 			viewer.showFileChooseDialog("LoadGameState");
 	}
 	
 	@Override
 	public void fieldClick(ActionEvent e, JButton b){
 		JButton btn = b;
 		
 		if (e.getSource() == btn) {
 			// determine x and y position
 			int posX =  (int)btn.getLocation().getX();
 			int posY =  (int)btn.getLocation().getY();
 
 			entityClicked(posX / btn.getWidth(), 
 								posY / btn.getHeight());
 		}
 	}
 
 	@Override
 	public void fileChoosed(String source, File f){
 		try{
 			if(source.equals("LoadLevel")){
 				Level newLevel = new Level(viewer);
 				newLevel.restoreLevel(f);
 
 				viewer.setLevel(newLevel);
 				this.level = newLevel;
 
 				viewer.closeMainFrame();
 				viewer.showMainFrame();
 				viewer.notifyLevelLoaded();
 			}else if(source.equals("SaveLevel")){
 				// TODO
 			}else if(source.equals("LoadGameState")){
 				// TODO
 			}else if(source.equals("SaveGameState")){
 				// TODO
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	public void entityClicked(int positionX, int positionY){
 		MainPanel panel = viewer.getMainPanel();
 		panel.getParentWindow().requestFocus();
 
 		if(!viewer.duringAnimation() && level.removeable(positionY, positionX)){
 			try{
 				viewer.startAnimation(positionY, positionX, 500);
 				level.removeStone(positionY, positionX);
 				panel.redraw();
 			}catch(ParameterOutOfRangeException e){
 				viewer.showAlertFrame(e.getParameterName(), "Parameter out of Range");
 			}catch(InternalFailureException e){
 				viewer.showAlertFrame(e.getMessage(), "Internal Failure");
 			}
 		}
 	}
 
 	
 	@Override
 	public void keyPressed(KeyEvent e){
 		int key = e.getKeyCode();
 		int markedRow = 0;
 		int markedCol = 0;
 		if(viewer!=null){
 			markedRow = viewer.getMarkedFieldRow();
 			markedCol = viewer.getMarkedFieldCol();
 			viewer.markField(markedRow, markedCol);
 		}
 
 		switch(key){
 			case VK_N:
 				level.restartLevel();
 				break;
 			case VK_BACK_SPACE:
 				if (viewer.duringAnimation() != true)
 					try{
 						level.undo();
 					}catch(CannotUndoException ignored){}
 				break;
 			case VK_ENTER:
 				if (viewer.duringAnimation() != true)
 					try{
 						level.redo();
 					}catch(CannotRedoException ignored){}
 				break;
 			case VK_SPACE:
 				if (viewer.duringAnimation() != true)
 					entityClicked(markedCol, markedRow);
 				break;
 			case VK_LEFT:
 				if (markedCol >0){
 					markedCol -=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						viewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_RIGHT:
 				if (markedCol < level.getFieldWidth()-1){
 					markedCol +=1;
 					viewer.markField(markedRow,markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						viewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_UP:
 				if (markedRow >0){
 					markedRow -=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						viewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_DOWN:
 				if (markedRow < level.getFieldHeight()-1){
 					markedRow +=1;
 					viewer.markField(markedRow, markedCol);
 					try{
 						viewer.getMainPanel().redraw();
 					}catch(InternalFailureException ex){
 						viewer.showAlertFrame(ex.getMessage(), "Internal Failure");
 					}
 				}
 				break;
 			case VK_T:
 				viewer.showHighscoreFrame();
 				break;
 			default:;
 		}
 	}
 }
