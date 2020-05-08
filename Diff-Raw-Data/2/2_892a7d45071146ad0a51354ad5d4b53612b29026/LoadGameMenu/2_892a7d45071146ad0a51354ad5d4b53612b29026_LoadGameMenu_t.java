 package com.madbros.adventurecraft.Menus;
 
 import java.io.File;
 
 import com.madbros.adventurecraft.Game;
 import static com.madbros.adventurecraft.Constants.*;
 
 import com.madbros.adventurecraft.GameStates.MainMenuState;
 import com.madbros.adventurecraft.UI.*;
 import com.madbros.adventurecraft.Utils.ButtonFunction;
 import com.madbros.adventurecraft.Utils.Rect;
 
 public class LoadGameMenu extends Menu {
 	String fileName;
 	String[] saveFolders;
 	SelectUIButton[] selectUIButtons;
 	SelectUIButton  currentlySelectedButton;
 	
 	public LoadGameMenu() {
 		super();
 	}
 
 	@Override
 	public void setupMenu() {
 		Game.createSavesFolderIfNecessary();
 		File folder = new File(SAVE_LOC);
 		File[] listOfFiles = folder.listFiles();
 		
 		saveFolders = new String[listOfFiles.length];
 		for(int i = 0; i < listOfFiles.length; i++) {
 			saveFolders[i] = listOfFiles[i].getName();
 			System.out.println(saveFolders[i]);
 		}
 		
 		ButtonFunction load = new ButtonFunction() { public void invoke() { load(); } };
 		ButtonFunction cancel = new ButtonFunction() { public void invoke() { cancel(); } };
 		
 		//create an array of all the saved games...
 		
 		Rect r1 = new Rect(20, Game.currentScreenSizeY - 60, 100, 50);
 		Rect r2 = new Rect(Game.currentScreenSizeX - 120, Game.currentScreenSizeY - 60, 100, 50);
 		
 		String[] strings = {"Load", "Cancel"};
 		ButtonFunction[] functions = {load, cancel};
 		Rect[] r = {r1, r2};
 
 		menuButtons = new UIButton[functions.length];
 		for(int i = 0; i < functions.length; i++) {
 			menuButtons[i] = new PlainUIButton(r[i].x, r[i].y, r[i].w, r[i].h, strings[i], functions[i]);
 		}
 		
 
 		r1 = new Rect(MAIN_MENU_STARTX, MAIN_MENU_STARTY, MAIN_MENU_WIDTH, MAIN_MENU_HEIGHT);
 		int marginY = 3;
 		
 		selectUIButtons = new SelectUIButton[saveFolders.length];
 		for(int i = 0; i < selectUIButtons.length; i++) {
 			selectUIButtons[i] = new SelectUIButton(r1.x, r1.y + i * (r1.h + marginY), r1.w, r1.h, saveFolders[i]);
 		}
 	}
 	
 	@Override
 	public void handleMouseInput(boolean leftMouseButtonPressed, boolean leftMouseButtonUp) {
 		super.handleMouseInput(leftMouseButtonPressed, leftMouseButtonUp);
 		for(int i = 0; i < selectUIButtons.length; i++) {
 			boolean didPressDown = selectUIButtons[i].handleMouseInput(leftMouseButtonPressed, leftMouseButtonUp);
 			if(didPressDown) {
				if(currentlySelectedButton != null && currentlySelectedButton != selectUIButtons[i]) currentlySelectedButton.buttonIsPressedDown = false;
 				currentlySelectedButton = selectUIButtons[i];
 			}
 		}
 	}
 	
 	@Override
 	public void render() {
 		super.render();
 		for(int i = 0; i < selectUIButtons.length; i++) {
 			selectUIButtons[i].render();
 		}
 		//if more than say 10 games, have a next page button/previous page button
 	}
 	
 	@Override
 	public void renderText() {
 		super.renderText();
 		for(int i = 0; i < selectUIButtons.length; i++) {
 			selectUIButtons[i].renderText();
 		}
 	}
 	
 	public void load() {
 		if(currentlySelectedButton != null) Game.createNewGameAtLoc(SAVE_LOC + currentlySelectedButton.text.getString() + "/");
 	}
 	
 	public void cancel() {
 		MainMenuState.cancel();
 	}
 }
