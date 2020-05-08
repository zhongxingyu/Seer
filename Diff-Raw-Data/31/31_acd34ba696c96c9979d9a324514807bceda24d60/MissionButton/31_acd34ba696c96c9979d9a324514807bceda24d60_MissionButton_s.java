 package ua.nevis.rainyday.gameobjects;
 
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.color.Color;
 
 import ua.nevis.rainyday.data.Mission;
 import ua.nevis.rainyday.managers.ResourceManager;
 
 public class MissionButton extends Sprite {
 	private final String TXT_MISSION = "mission";
 	private final String TXT_SCORE = "score";
 	private Mission mission;
 	private ResourceManager resourceManager;
 	private Text textMission;
 	private Text valueMission;
 	private Text textScore;
 	private Text valueScore;
 	private Sprite[] stars = new Sprite[3];
 
 	public MissionButton(Mission mission, float x, float y, ITextureRegion textureRegion) {
 		super(x, y, textureRegion, ResourceManager.getInstance().vboManager);
 		this.mission = mission;
 		this.resourceManager = ResourceManager.getInstance();
 		setMissionInfo();
 		setStar();
 	}
 
 	private void setMissionInfo() {
 		Color color = null;
 		if (mission.isActive()) {
			color = new Color(0, 33, 255);
 		} else {
			color = new Color(150, 150, 150);
 		}
 		// mission text
 		textMission = new Text(0, 0, resourceManager.paintdrpFont, TXT_MISSION, resourceManager.vboManager);
		textMission.setPosition((getWidth() - textMission.getWidth()) / 2, 5);
 		textMission.setColor(color);
 		attachChild(textMission);
 		// mission number
 		valueMission = new Text(0, 0, resourceManager.paintdrpFont, Integer.toString(mission.getMissionNumber()), resourceManager.vboManager);
		valueMission.setPosition((getWidth() - valueMission.getWidth()) / 2, textMission.getY() + textMission.getHeight() + 3);
 		valueMission.setColor(color);
 		attachChild(valueMission);
 		// score text
 		textScore = new Text(0, 0, resourceManager.paintdrpFont, TXT_SCORE, resourceManager.vboManager);
		textScore.setPosition((getWidth() - textScore.getWidth()) / 2, valueMission.getY() + valueMission.getHeight() + 3);
 		textScore.setColor(color);
 		attachChild(textScore);
 		// score value
 		valueScore = new Text(0, 0, resourceManager.paintdrpFont, Integer.toString(mission.getScoreValue()), resourceManager.vboManager);
		valueScore.setPosition((getWidth() - valueScore.getWidth()) / 2, textScore.getY() + textScore.getHeight() + 3);
 		valueScore.setColor(color);
 		attachChild(valueScore);
 	}
 
 	private void setStar() {
 		int starCount = mission.getStarCount();
 		float positionX;
		float positionY = getHeight() - 37f;
 		for (int i = 0; i < 3; i++) {
 			if (starCount == 0) {
 				stars[i] = new Sprite(0, 0, resourceManager.starGreyRegion, resourceManager.vboManager);
 			} else {
 				stars[i] = new Sprite(0, 0, resourceManager.starYellowRegion, resourceManager.vboManager);
 				starCount -= 1;
 			}
 			positionX = 23.5f + (stars[i].getWidth() + 5) * i;
 			stars[i].setPosition(positionX, positionY);
 			attachChild(stars[i]);
 		}
 	}
 
 	@Override
 	protected void preDraw(GLState pGLState, org.andengine.engine.camera.Camera pCamera) {
 		super.preDraw(pGLState, pCamera);
 		pGLState.enableDither();
 	}
 
 	@Override
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 		if (pSceneTouchEvent.isActionDown()) {
 			if (mission.isActive()) {
 				// start mission
 			}
 		}
 		return true;
 	}
 
 	public void disposeButton() {
 		textMission.detachSelf();
 		textMission.dispose();
 		textMission = null;
 		valueMission.detachSelf();
 		valueMission.dispose();
 		valueMission = null;
 		textScore.detachSelf();
 		textScore.dispose();
 		textScore = null;
 		valueScore.detachSelf();
 		valueScore.dispose();
 		valueScore = null;
 		for (int i = 0; i < 3; i++) {
 			stars[i].detachSelf();
 			stars[i].dispose();
 			stars[i] = null;
 		}
 		stars = null;
 		this.detachSelf();
 		this.dispose();
 	}
 }
