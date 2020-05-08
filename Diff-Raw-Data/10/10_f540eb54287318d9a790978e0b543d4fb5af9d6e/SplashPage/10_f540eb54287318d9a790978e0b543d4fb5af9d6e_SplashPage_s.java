 package com.jesse.game.states;
 
 import java.awt.Font;
 
 import org.lwjgl.input.Keyboard;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import com.jesse.game.GameMain;
 import com.jesse.game.listeners.OnEnterPressedListener;
 import com.jesse.game.ui.EntryPopup;
 import com.jesse.game.utils.Print;
 
 @SuppressWarnings("deprecation")
 public class SplashPage extends BasicGameState implements OnEnterPressedListener {
 	
 	private int mId;
 	private Image mLogo;
 	
 	public GameMain mGame;
 	
 	private TextField mStart;
 	private TextField mExit;
 	private EntryPopup mServerSelectionPopup;
 	private EntryPopup mNameEntryPopup;
 	
 	private int mSelectedOption;	
 	private int mPressedKey = -1;
 	
 	private String mEnteredName;
 	
 	private static final int START = 0;
 	private static final int EXIT = 1;
 	
 	private static final int NAME_ENTRY = 0;
 	private static final int SERVER_ENTRY = 1;
 
 	public SplashPage(int id) {
 		mId = id;
 	}
 
 	@Override
 	public void init(GameContainer gc, StateBasedGame game) throws SlickException {
 		mGame = (GameMain) game;
		mLogo = new Image("res/images/pokemonlogo.png");
 		mStart = new TextField(gc, new TrueTypeFont(new Font("Arial", Font.BOLD, 30), true), 
 				GameMain.SCREEN_WIDTH / 4, (int) (GameMain.SCREEN_HEIGHT / 1.25), 100, 100);
 		
 		mExit = new TextField(gc, new TrueTypeFont(new Font("Arial", Font.BOLD, 30), true), 
 				GameMain.SCREEN_WIDTH * 2 / 3, (int) (GameMain.SCREEN_HEIGHT / 1.25), 100, 100);
 				
 		mServerSelectionPopup = new EntryPopup(gc, SERVER_ENTRY,
 				GameMain.SCREEN_WIDTH * 3 / 8, GameMain.SCREEN_HEIGHT * 3 / 8, 
 				GameMain.SCREEN_WIDTH / 4, GameMain.SCREEN_HEIGHT / 4);
 		
 		mServerSelectionPopup.setText("Enter server IP");
 		mServerSelectionPopup.setOnEnterPressedListener(this);
 		
 		mNameEntryPopup = new EntryPopup(gc, NAME_ENTRY,
 				GameMain.SCREEN_WIDTH * 3 / 8, GameMain.SCREEN_HEIGHT * 3 / 8, 
 				GameMain.SCREEN_WIDTH / 4, GameMain.SCREEN_HEIGHT / 4);
 		
 		mNameEntryPopup.setText("What is your name?");
 		mNameEntryPopup.setOnEnterPressedListener(this);
 		
 		mStart.setBackgroundColor(null);
 		mStart.setBorderColor(null);
 		mExit.setBackgroundColor(null);
 		mExit.setBorderColor(null);
 
 		setChoice(START);
 		
 		mStart.setText("Start");
 		mExit.setText("Exit");
 		
 	}
 	
 	@Override
 	public void render(GameContainer gc, StateBasedGame game, Graphics grfx) throws SlickException {
 		grfx.scale(GameMain.SCALE, GameMain.SCALE);
 		mLogo.draw(GameMain.SCREEN_WIDTH / 4 - mLogo.getWidth() / 4, GameMain.SCREEN_HEIGHT / 4 - mLogo.getHeight() / 4, .5f);
 		
 		grfx.scale(.5f, .5f);
 		mStart.render(gc, grfx);
 		mExit.render(gc, grfx);
 		mServerSelectionPopup.render(gc, grfx);
 		mNameEntryPopup.render(gc, grfx);
 	}
 
 	@Override
 	public void update(GameContainer gc, StateBasedGame game, int delta) throws SlickException {
 			
 		if(gc.getInput().isKeyDown(Keyboard.KEY_A)) {
 			setChoice(START);
 		}
 		else if(gc.getInput().isKeyDown(Keyboard.KEY_D)) {
 			setChoice(EXIT);
 		}
 		else if(gc.getInput().isKeyDown(Keyboard.KEY_RETURN)) {
 			if(mPressedKey == Keyboard.KEY_RETURN)
 				return;
 			
 			mPressedKey = Keyboard.KEY_RETURN;
 			if(mServerSelectionPopup.textEntryHasFocus() && mServerSelectionPopup.getEntryText().length() > 0)
 				mServerSelectionPopup.enterPressed();
 			else if(mNameEntryPopup.textEntryHasFocus() && mNameEntryPopup.getEntryText().trim().length() > 0)
 				mNameEntryPopup.enterPressed();
 			else if(mSelectedOption == EXIT)
 				System.exit(0);
 			else if(mSelectedOption == START) {
 				mNameEntryPopup.launch();
 //				mServerSelectionPopup.launch();
 			}
 		}
 		else if(gc.getInput().isKeyDown(Keyboard.KEY_ESCAPE)) {
 			if(mServerSelectionPopup.isVisible())
 				mServerSelectionPopup.setVisibility(false);
 		}
 		
 		if(!gc.getInput().isKeyDown(Keyboard.KEY_RETURN)) {
 			mPressedKey = -1;
 		}
 	}
 
 	@Override
 	public int getID() {
 		return mId;
 	}
 	
 	private void setChoice(int choice) {
 //		if(mSelectedOption == choice)
 //			return;
 		
 		switch(choice) {
 		case START :
 			mStart.setTextColor(Color.yellow);
 			mExit.setTextColor(Color.white);
 			break;
 		case EXIT :
 			mExit.setTextColor(Color.yellow);
 			mStart.setTextColor(Color.white);
 			break;
 		}
 		
 		mSelectedOption = choice;
 	}
 
 	@Override
 	public boolean onEnterPressed(int id, String text) {
 		switch(id) {
 		case NAME_ENTRY :
 			Print.log("arrived");
 			mEnteredName = text;
 			mNameEntryPopup.setVisibility(false);
 			mServerSelectionPopup.launch();
 			return true;
 		case SERVER_ENTRY :
 			mGame.connectToServer((mEnteredName != null && mEnteredName.trim().length() > 0) ? mEnteredName : "Default", text);
 			return true;
 		}
 		return false;
 	}
 	
 }
