 package ultraextreme.view;
 
 import org.andengine.entity.text.Text;
 import org.andengine.opengl.font.Font;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import org.andengine.util.color.Color;
 
 import ultraextreme.model.util.Position;
 
 public class HighscoreText extends Text {
 
 	private final int index;
 	
 	public HighscoreText(Position textPos, Font font, VertexBufferObjectManager vbo, int index) {
 		super((float) textPos.getX(), (float) textPos.getY(), font, "                       ", vbo);
 		this.setColor(Color.BLACK);
 		this.index = index;
		this.setHighscore(new Highscore("", 0));
 	}
 	
 	public void setHighscore(Highscore highscore)
 	{
 		this.setText("" + index + ".  |  " + highscore.getName() + "  |  " + highscore.getScore());
 	}
 }
