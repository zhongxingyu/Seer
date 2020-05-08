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
			x++;
 		} else if (!upArrow && downArrow) {
 			System.out.println("down");
			x--;
 		}
 	}
 
 	@Override
 	public void render(Graphics2D g) {
		g.fillOval(x - 5, y - 5, x + 5, y + 5);
 	}
 
 	@Override
 	public GameState next(EngineDetails ed) {
 		return null;
 	}
 
 }
