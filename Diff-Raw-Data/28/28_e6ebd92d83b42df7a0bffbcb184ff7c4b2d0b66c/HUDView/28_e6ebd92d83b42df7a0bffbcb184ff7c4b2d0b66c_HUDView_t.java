 package se.chalmers.tda367.group15.game.views;
 
import org.newdawn.slick.*;
 
 import se.chalmers.tda367.group15.game.models.ScoreModel;
import se.chalmers.tda367.group15.game.settings.Constants;
 
 public class HUDView implements View {
 	private final ScoreModel scoreModel;
 
    private final TrueTypeFont textFont = new TrueTypeFont(new java.awt.Font(
            "Monospaced", java.awt.Font.BOLD, 16), true);

 	public HUDView(ScoreModel scoreModel) {
 		this.scoreModel = scoreModel;
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g)
 			throws SlickException {
 
 		int currentScore = scoreModel.getScore();
 
 		String scoreStr = "Score: " + currentScore;
 		String fpsStr = "FPS: " + container.getFPS();
		int scoreStrWidth = textFont.getWidth(scoreStr);
 
        // Draw black outline
        textFont.drawString(Constants.GAME_WIDTH - (scoreStrWidth + 6), 6, scoreStr, Color.black);
        textFont.drawString(6, 6, fpsStr, Color.black);
 
        // Draw white on top
        textFont.drawString(Constants.GAME_WIDTH - (scoreStrWidth + 5), 5, scoreStr, Color.white);
        textFont.drawString(5, 5, fpsStr, Color.white);
 
 	}
 
 }
