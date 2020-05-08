 package kauhsa.sokoban.ui.gamestates;
 
 import kauhsa.sokoban.resources.FontLoader;
 import kauhsa.sokoban.ui.elements.label.Label;
 import kauhsa.sokoban.ui.elements.menu.Menu;
 import kauhsa.sokoban.ui.elements.menu.MenuRenderer; 
 import kauhsa.sokoban.ui.elements.HorizontalAlignment;
 import kauhsa.sokoban.ui.elements.VerticalAlignment;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  * Slick GameState for the main menu, which is also the first State entered
  * when the game launches.
  */
 public class MainMenuState extends BasicGameState {
 
     private final int EDGE_DISTANCE = 50;
     
     Menu<MainMenuItems> mainMenu = new Menu<MainMenuItems>();
     MenuRenderer menuRenderer;
     Label header;
 
     @Override
     public int getID() {
         return GameStates.MAIN_MENU.ordinal();
     }
 
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {        
         Font headerFont = FontLoader.loadAwtFontToSlick("Ubuntu", java.awt.Font.PLAIN, 100);        
         header = new Label();
         header.setText("Sokoban!");
         header.setFont(headerFont);
         header.setVerticalAlignment(VerticalAlignment.CENTER);        
         header.setHorizontalAlignment(HorizontalAlignment.MIDDLE);
         
         Font menuFont = FontLoader.loadAwtFontToSlick("Ubuntu", java.awt.Font.PLAIN, 50);        
         menuRenderer = new MenuRenderer(mainMenu);
         menuRenderer.setFont(menuFont);
         menuRenderer.setVerticalAlignment(VerticalAlignment.BOTTOM);
         menuRenderer.setHorizontalAlignment(HorizontalAlignment.RIGHT);       
         
         mainMenu.addItem("Start", MainMenuItems.START);   
         mainMenu.addItem("Quit", MainMenuItems.QUIT);     
     }
 
     public void render(GameContainer gc, StateBasedGame sbg, Graphics grphcs) throws SlickException {
         header.render(grphcs, 0, 0, gc.getWidth(), gc.getHeight() / 2);
         menuRenderer.render(grphcs, EDGE_DISTANCE, EDGE_DISTANCE, gc.getWidth() - EDGE_DISTANCE * 2, gc.getHeight() - EDGE_DISTANCE * 2);
     }
 
     public void update(GameContainer gc, StateBasedGame sbg, int i) throws SlickException {
         Input input = gc.getInput();
 
         if (input.isKeyPressed(Input.KEY_UP)) {
             mainMenu.moveUp();
         } else if (input.isKeyPressed(Input.KEY_DOWN)) {
             mainMenu.moveDown();
         } else if (input.isKeyPressed(Input.KEY_ENTER)) {
             handleMenuSelection(gc, sbg);
         }
     }
 
     /**
      * Do whatever actions are appropriate for current menu selection.
      * 
      * @param gc current GameContainer.
      * @param sbg current StateBasedGame.
      */
     private void handleMenuSelection(GameContainer gc, StateBasedGame sbg) {
        if (mainMenu.getSelected() == MainMenuItems.START) {            
            gc.getInput().clearKeyPressedRecord();
             sbg.enterState(GameStates.LEVEL_MENU.ordinal());
         } else if (mainMenu.getSelected() == MainMenuItems.QUIT) {
             gc.exit();
         }
     }
 }
