 package tetrix.view.theme;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 /**
  * The class that is responsible for all images
  * 
  * @author Jonathan Kara
  *
  */
 
 public class ThemeHandler {
 
 	private static String theme = "img/overworld/";		//default theme
 	public static final String BACKGROUND_IMG = "background.png";
 	public static final String GAMEOVER_HOVER = "gameover_hover.png";
 	public static final String GAMEOVER_BACKGROUND = "gameover_background.png";
 	public static final String GAMEOVER_LOGO = "game_over.png";
 	public static final String NEW_GAME_BIG = "new_game_big.png";
 	public static final String MAIN_MENU_BIG = "main_menu_big.png";
 	public static final String BACK_IMG = "back.png";
 	public static final String CANNON_IMG = "cannon.png";
 	public static final String CANNON2_IMG = "cannon2.png";
 	public static final String CANNON3_IMG = "cannon3.png";
 	public static final String CANNON4_IMG = "cannon4.png";
 	public static final String CANNON5_IMG = "cannon5.png";
 	public static final String EASY_IMG = "easy.png";
 	public static final String FX_VOLUME_IMG = "fx_volume.png";
 	public static final String EXIT_IMG = "exit.png";
 	public static final String GAME_BACKGROUND_IMG = "game_background.png";
 	public static final String HARD_IMG = "hard.png";
 	public static final String HIGHSCORE_IMG = "highscore.png";
 	public static final String HOVER_IMG = "menu_hover.png";
 	public static final String HOVER_SMALL_IMG = "hover_small.png";
 	public static final String MAIN_MENU_IMG = "main_menu.png";
 	public static final String MUSIC_VOLUME_IMG = "music_volume.png";
 	public static final String NEW_GAME_IMG = "new_game.png";
 	public static final String POPUP_IMG = "popup.png";
 	public static final String PRESS_ANY_KEY_IMG = "press_any_key.png";
 	public static final String QUIT_IMG = "quit.png";
 	public static final String RESUME_IMG = "resume.png";
 	public static final String SETTINGS_IMG = "settings.png";
 	public static final String SLIDE_PIN_IMG = "slide_pin.png";
 	public static final String SOUND_IMG = "sound.png";
 	public static final String START_GAME_IMG = "start_game.png";
 	public static final String TETRIX_LOGO_IMG = "tetrix_logo.png";
 	public static final String LOCKED_BLOCK_IMG = "locked.png";
 	public static final String BLUE_BLOCK_IMG = "blue.png";
 	public static final String GREEN_BLOCK_IMG = "green.png";
 	public static final String ORANGE_BLOCK_IMG = "orange.png";
 	public static final String PINK_BLOCK_IMG = "pink.png";
 	public static final String PURPLE_BLOCK_IMG = "purple.png";
 	public static final String RED_BLOCK_IMG = "red.png";
 	public static final String TURQUOISE_BLOCK_IMG = "turquoise.png";
 	public static final String YELLOW_BLOCK_IMG = "yellow.png";
 	public static final String RIGHT_ARROW_IMG = "arrow_right.png";
 	public static final String RIGHT_ARROW_HOVER_IMG = "arrow_right_hover.png";
 	public static final String LEFT_ARROW_IMG = "arrow_left.png";
 	public static final String LEFT_ARROW_HOVER_IMG = "arrow_left_hover.png";
 	private static int cannon = 0;
 	
 	
 	public static void setCannon(int c){
 		cannon = c;
 	}
 	
 	public static void setUnderworldTheme(){
 		theme = "img/underworld/";
 	}
 	
 	public static void setOverworldTheme(){
 		theme = "img/overworld/";
 	}
 	
 	public static Image get(String img) throws SlickException{
 		return new Image(theme + img);
 	}
 	public static Image getBlockOrCannon(String img) throws SlickException{
 		return new Image("img/cannonsblocks/" + img);
 	}
 	
 	public static Image getCannon() throws SlickException{
		if (cannon == 0){
			return new Image("img/cannonsblocks/cannon.png");
		} else if(cannon == 1){
 			return new Image("img/cannonsblocks/cannon2.png");
 		} else if(cannon == 2){
 			return new Image("img/cannonsblocks/cannon3.png");
 		} else if(cannon == 3){
 			return new Image("img/cannonsblocks/cannon4.png");
 		} else if(cannon == 4){
 			return new Image("img/cannonsblocks/cannon5.png");
 		} else {
 			return new Image("img/cannonsblocks/cannon.png");
 		}
 	}
 
 	
 }
