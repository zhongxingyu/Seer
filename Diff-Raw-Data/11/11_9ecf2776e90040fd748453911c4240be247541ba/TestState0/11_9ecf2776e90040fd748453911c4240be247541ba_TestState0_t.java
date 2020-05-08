import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 
 import com.github.massiv_diversions.engine.ControlData;
 import com.github.massiv_diversions.engine.EngineDetails;
 import com.github.massiv_diversions.engine.GameState;
 
 
 public class TestState0 implements GameState {
 
 	private int x, y;
 
 	public TestState0(EngineDetails ed) {
 		x = ed.width() / 2;
 		y = ed.height() / 2;
 	}
 
 	@Override
 	public void update(ControlData cd) {
 		boolean upArrow = cd.keyDown(KeyEvent.VK_UP);
 		boolean downArrow = cd.keyDown(KeyEvent.VK_DOWN);
 		if (upArrow && !downArrow) {
 			System.out.println("up");
			y++;
 		} else if (!upArrow && downArrow) {
 			System.out.println("down");
			y--;
 		}
 	}
 
 	@Override
 	public void render(Graphics2D g) {
		g.setColor(Color.BLUE);
		g.fillOval(x - 3, y - 3, 7, 7);
 	}
 
 	@Override
 	public GameState next(EngineDetails ed) {
 		return null;
 	}
 
 }
