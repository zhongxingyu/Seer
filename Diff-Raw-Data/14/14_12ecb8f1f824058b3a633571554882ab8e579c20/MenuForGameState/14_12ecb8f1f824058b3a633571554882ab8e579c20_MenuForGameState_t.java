 package state;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.font.effects.GradientEffect;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 import other.LevelController;
 import other.LevelPackage;
 import other.Translator;
 import app.Game;
 import factory.EffectFactory;
 import factory.FontFactory;
 
 public class MenuForGameState extends BasicGameState {
 
     private boolean isMouseOverPackageArrowLeft, isMouseOverPackageArrowRight,
             isMouseOverLevelArrowLeft, isMouseOverLevelArrowRight, isPackageArrowLeftDisabled,
             isPackageArrowRightDisabled, isLevelArrowLeftDisabled, isLevelArrowRightDisabled,
             isMouseOverReturn, isMouseOverPlay, isPlayDisabled;
     private int stateId, width, height, packageIndex, levelIndex;
     private Font ubuntuMedium, ubuntuLarge;
     private String progressText, showingText;
 
     private Input input;
     private Point mouse;
     private Translator translator;
     private byte[] progresses;
     private Rectangle packageArrowLeft, packageArrowRight, levelArrowLeft, levelArrowRight,
             returnRectangle, playRectangle;
     private ArrayList<LevelPackage> levelPackages;
     private LevelController levelController;
 
     public MenuForGameState(int stateId) {
         this.stateId = stateId;
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game) throws SlickException {
         FontFactory fonts = FontFactory.getInstance();
         EffectFactory effects = EffectFactory.getInstance();
         ColorEffect whiteEffect = effects.getColorEffect(java.awt.Color.WHITE);
         GradientEffect gradientEffect = effects.getGradientEffect(java.awt.Color.WHITE,
                 java.awt.Color.GRAY, 0.5f);
         translator = Translator.getInstance();
         width = container.getWidth();
         height = container.getHeight();
 
         ubuntuMedium = fonts.getFont("ubuntu", width / 26, whiteEffect);
         ubuntuLarge = fonts.getFont("ubuntu", width / 16, gradientEffect);
 
         levelController = LevelController.getInstance();
         levelPackages = levelController.getLevels();
         progresses = levelController.getProgresses();
 
         int arrowWidth = ubuntuLarge.getWidth(">");
         int arrowHeight = ubuntuLarge.getWidth(">");
 
         packageArrowLeft = new Rectangle();
         packageArrowLeft.width = arrowWidth;
         packageArrowLeft.height = arrowHeight;
         packageArrowLeft.x = width * 1 / 4;
         packageArrowLeft.y = height * 1 / 3 + packageArrowLeft.height * 3 / 4;
 
         packageArrowRight = new Rectangle();
         packageArrowRight.width = arrowWidth;
         packageArrowRight.height = arrowHeight;
         packageArrowRight.x = width * 3 / 4 - arrowWidth;
         packageArrowRight.y = height * 1 / 3 + packageArrowRight.height * 3 / 4;
 
         levelArrowLeft = new Rectangle();
         levelArrowLeft.width = arrowWidth;
         levelArrowLeft.height = arrowHeight;
         levelArrowLeft.x = width * 1 / 4;
         levelArrowLeft.y = height * 3 / 4 + levelArrowLeft.height * 3 / 4;
 
         levelArrowRight = new Rectangle();
         levelArrowRight.width = arrowWidth;
         levelArrowRight.height = arrowHeight;
         levelArrowRight.x = width * 3 / 4 - arrowWidth;
         levelArrowRight.y = height * 3 / 4 + levelArrowRight.height * 3 / 4;
 
         returnRectangle = new Rectangle();
         returnRectangle.width = ubuntuMedium.getWidth(translator.translate("return"));
         returnRectangle.height = ubuntuMedium.getHeight(translator.translate("return"));
         returnRectangle.x = width / 100;
         returnRectangle.y = (int) (height - returnRectangle.height * 1.1f);
 
         playRectangle = new Rectangle();
         playRectangle.width = ubuntuMedium.getWidth(translator.translate("play"));
         playRectangle.height = ubuntuMedium.getHeight(translator.translate("play"));
         playRectangle.x = width / 2 - playRectangle.width / 2;
         playRectangle.y = (int) (height - playRectangle.height * 1.1f);
 
         packageIndex = 0;
        int packageSize = levelPackages.get(packageIndex).getLevelNames().size();
        levelIndex = (progresses[packageIndex] == packageSize && packageSize > 0) ? progresses[packageIndex] - 1
                : progresses[packageIndex];
         setProgressText();
         setShowingText();
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g)
             throws SlickException {
         g.setFont(ubuntuLarge);
         g.setColor(Color.white);
         g.drawString(translator.translate("Packages"), width / 30, height / 10);
         g.drawString(translator.translate("Levels"), width / 30, height / 2);
 
         g.setColor((isPackageArrowLeftDisabled) ? Color.darkGray
                 : ((isMouseOverPackageArrowLeft) ? Color.red : Color.white));
         g.drawString("<", width * 1 / 4, height * 1 / 3);
         g.setColor((isPackageArrowRightDisabled) ? Color.darkGray
                 : ((isMouseOverPackageArrowRight) ? Color.red : Color.white));
         g.drawString(">", width * 3 / 4 - packageArrowRight.width, height * 1 / 3);
         g.setColor((isLevelArrowLeftDisabled) ? Color.darkGray
                 : ((isMouseOverLevelArrowLeft) ? Color.red : Color.white));
         g.drawString("<", width * 1 / 4, height * 3 / 4);
         g.setColor((isLevelArrowRightDisabled) ? Color.darkGray
                 : ((isMouseOverLevelArrowRight) ? Color.red : Color.white));
         g.drawString(">", width * 3 / 4 - levelArrowRight.width, height * 3 / 4);
         String text = levelPackages.get(packageIndex).getName();
         g.setFont(ubuntuMedium);
         g.setColor((isMouseOverReturn) ? Color.red : Color.white);
         g.drawString(translator.translate("return"), width / 100, height - returnRectangle.height
                 * 1.1f);
         g.setColor((isPlayDisabled) ? Color.darkGray
                 : ((isMouseOverPlay) ? Color.red : Color.white));
         g.drawString(translator.translate("play"), width / 2 - playRectangle.width / 2, height
                 - playRectangle.height * 1.1f);
         g.setColor(Color.white);
         g.drawString(text, width / 2 - ubuntuMedium.getWidth(text) / 2, height * 1 / 3 + height
                 / 50);
         if (levelIndex < levelPackages.get(packageIndex).getLevelNames().size()) {
             g.drawString(showingText, width - ubuntuMedium.getWidth(showingText) * 1.1f, height
                     - ubuntuMedium.getHeight(showingText) * 1.1f);
             if (levelIndex <= progresses[packageIndex]) {
                 text = levelPackages.get(packageIndex).getLevelNames().get(levelIndex);
             } else {
                 g.setColor(Color.darkGray);
                 text = String.format("» %1$s «", translator.translate("locked"));
             }
 
         } else {
             g.setColor(Color.darkGray);
             text = String.format("« %1$s »", translator.translate("none"));
         }
         g.drawString(text, width / 2 - ubuntuMedium.getWidth(text) / 2, height * 3 / 4 + height
                 / 40);
         g.setColor(new Color(0, 0.5f, 0));
         g.drawString(progressText, width - ubuntuMedium.getWidth(progressText) * 1.1f,
                 height * 16 / 30);
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta)
             throws SlickException {
         input = container.getInput();
         mouse = new Point(input.getMouseX(), input.getMouseY());
 
         isPackageArrowLeftDisabled = packageIndex < 1;
         isPackageArrowRightDisabled = packageIndex >= levelPackages.size() - 1;
         isLevelArrowLeftDisabled = levelIndex < 1;
         isLevelArrowRightDisabled = levelIndex >= levelPackages.get(packageIndex).getLevelNames()
                 .size() - 1;
         isPlayDisabled = levelIndex >= levelPackages.get(packageIndex).getLevelNames().size()
                 || levelIndex > progresses[packageIndex];
 
         isMouseOverPackageArrowLeft = packageArrowLeft.contains(mouse);
         isMouseOverPackageArrowRight = packageArrowRight.contains(mouse);
         isMouseOverLevelArrowLeft = levelArrowLeft.contains(mouse);
         isMouseOverLevelArrowRight = levelArrowRight.contains(mouse);
         isMouseOverReturn = returnRectangle.contains(mouse);
         isMouseOverPlay = playRectangle.contains(mouse);
 
         if (input.isKeyPressed(Input.KEY_ESCAPE)) {
             game.enterState(Game.MENU_STATE);
         }
 
         if (input.isMousePressed(0)) {
             if (isMouseOverPackageArrowLeft && !isPackageArrowLeftDisabled) {
                 packageIndex--;
                int packageSize = levelPackages.get(packageIndex).getLevelNames().size();
                levelIndex = (progresses[packageIndex] == packageSize && packageSize > 0) ? progresses[packageIndex] - 1
                        : progresses[packageIndex];
                 setProgressText();
                 setShowingText();
             }
             if (isMouseOverPackageArrowRight && !isPackageArrowRightDisabled) {
                 packageIndex++;
                int packageSize = levelPackages.get(packageIndex).getLevelNames().size();
                levelIndex = (progresses[packageIndex] == packageSize && packageSize > 0) ? progresses[packageIndex] - 1
                        : progresses[packageIndex];
                 setProgressText();
                 setShowingText();
             }
             if (isMouseOverLevelArrowLeft && !isLevelArrowLeftDisabled) {
                 levelIndex--;
                 setShowingText();
             }
             if (isMouseOverLevelArrowRight && !isLevelArrowRightDisabled) {
                 levelIndex++;
                 setShowingText();
             }
             if (isMouseOverReturn) {
                 game.enterState(Game.MENU_STATE);
             }
             if (isMouseOverPlay && !isPlayDisabled) {
                 try {
                     levelController.loadLevel(packageIndex, levelIndex);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 game.enterState(Game.GAME_STATE);
             }
         }
     }
 
     @Override
     public int getID() {
         return this.stateId;
     }
 
     private void setProgressText() {
         int size = levelPackages.get(packageIndex).getLevelNames().size();
         progressText = String.format("%4$s %1$d %3$s %2$d", progresses[packageIndex], size,
                 translator.translate((size > 1 && size < 5) ? "of2" : "of"),
                 translator.translate("completed"));
     }
 
     private void setShowingText() {
         int size = levelPackages.get(packageIndex).getLevelNames().size();
         showingText = String.format("%4$s %1$d %3$s %2$d", levelIndex + 1, size,
                 translator.translate((size > 1 && size < 5) ? "of2" : "of"),
                 translator.translate("showing"));
     }
 }
