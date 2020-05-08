 package view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 
 import javax.swing.JPanel;
 
 import model.GameModel;
 import resources.HUDFonts;
 import resources.Translator;
 
 /**
  * A GUI object which will render a score.
  * 
  * @author 
  *
  */
 public class ScorePanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	private GameModel model;
	private final String scoreText;
 	
 	/**
 	 * Creates a new instance of the object.
 	 * @param model The model to fetch values from.
 	 */
 	public ScorePanel(GameModel model) {
 		super();
 		this.setPreferredSize(new Dimension(200, 45));
 		this.model = model;
 		
		this.scoreText = Translator.getString("score");
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		g.setColor(new Color(0, 0, 0, 100));
 		g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		
 		g.setFont(HUDFonts.getScoreFont());
 		g.setColor(Color.WHITE);
		String text = scoreText + ": " + model.getScore();
 		g.drawString(text, this.getWidth()/2 - 
 				(int)g.getFontMetrics().getStringBounds(text, g).getWidth()/2, 30);
 	}
 }
