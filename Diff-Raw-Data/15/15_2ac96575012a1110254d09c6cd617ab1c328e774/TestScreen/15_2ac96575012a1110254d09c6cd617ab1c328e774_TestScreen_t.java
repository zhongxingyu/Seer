 package screen;
 
 import resource.TestLoader;
 import gfx.BatAnimation;
 import display.BatDisplay;
 
 public class TestScreen extends BatScreen {
 	
 	BatAnimation lard;
 	BatAnimation jump;
 	
 	public TestScreen() {
 		super(true, true);
 		// TODO Auto-generated constructor stub
 		lard = new BatAnimation(TestLoader.LARD, 15, BatAnimation.LOOP);
		lard.setState(10);
 		jump = new BatAnimation(TestLoader.SPLODE, 30, BatAnimation.LOOP);
 	}
 
 	@Override
 	public boolean absorbedKeyPress() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void handleKeyboard() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void render(BatDisplay display) {
 		// TODO Auto-generated method stub
 		
 		display.insert(lard.getGraphics(), 400, 300);
		if(!jump.isFinished())
			display.insert(jump.getGraphics(), 100, 100);
 		
 	}
 
 	@Override
 	public void update(float dt) {
 		lard.stepAnimation(dt);
 		jump.stepAnimation(dt);
 	}
 }
