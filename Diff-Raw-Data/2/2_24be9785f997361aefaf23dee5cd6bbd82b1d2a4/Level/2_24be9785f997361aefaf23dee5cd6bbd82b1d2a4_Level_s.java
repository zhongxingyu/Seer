 package topplintowers.levels;
 
 import java.util.LinkedHashMap;
 
 import org.andengine.entity.primitive.Line;
 import org.andengine.entity.text.Text;
 import org.andengine.util.color.Color;
 
 import com.topplintowers.R;
 
 import topplintowers.ResourceManager;
 import topplintowers.crates.CrateType;
 import topplintowers.scenes.GameScene;
 
 public class Level {
 	private float goalHeight;
 	private LinkedHashMap<CrateType, Integer> crateCounts;
 	private Levels level;
 	private boolean isLocked = true;
 
 	public Level(Levels level, float goalHeight, LinkedHashMap<CrateType, Integer> crateCounts) {
 		this.level = level;
 		
 		if (this.level == Levels.ONE) {
 			this.isLocked = false;
 		}
 		
 		this.crateCounts = crateCounts;
 		this.goalHeight = goalHeight;	
 	}
 	
 	public boolean getLocked() 	{ return this.isLocked; }
 	public float getGoal() 		{ return this.goalHeight; }
 	public Levels getLevelType() { return this.level; }
 	
 	public void setGoal(GameScene currentScene) {
 		float goalScaled = 65 * goalHeight;	
 		
 		float lineLength = 480;
 		float linePoxX = 400 - (lineLength/2);
		float linePosY = 480 - currentScene.mPlatform.rectangle.getHeightScaled() - goalScaled;
 		
 		Line goalLine = new Line(linePoxX, linePosY, linePoxX+lineLength, linePosY, 3, currentScene.getVBOM());
 		goalLine.setColor(Color.WHITE);
 		currentScene.getContainer().attachChild(goalLine);
 		
 		Text goalText = new Text(0, 0, ResourceManager.mGoalFont, "Goal", currentScene.getVBOM());
 		float textPosX = goalLine.getX2() - goalText.getWidth();
 		float textPosY = goalLine.getY2() - goalText.getHeight();
 		goalText.setPosition(textPosX, textPosY);
 		currentScene.getContainer().attachChild(goalText);
 	}
 	
 	
 	public LinkedHashMap<CrateType, Integer> getCounts() { return crateCounts; }
 }
