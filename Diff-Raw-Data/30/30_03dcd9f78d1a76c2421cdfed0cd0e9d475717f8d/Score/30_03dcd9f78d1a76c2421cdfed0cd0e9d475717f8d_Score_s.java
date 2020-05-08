 package com.qeevee.gq.rules.act;
 
 import android.content.Context;
 
 import com.qeevee.gq.res.ResourceManager;
 
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.R;
 import edu.bonn.mobilegaming.geoquest.Variables;
 
 public class Score extends Action {
 
 	private static final String SCORE_VARIABLE_NAME = Variables.SYSTEM_PREFIX
 			+ "score";
 
 	private Context ctx = GeoQuestApp.getContext();
 
 	@Override
 	protected boolean checkInitialization() {
 		boolean initOK = true;
 		initOK &= params.containsKey("value");
 		return initOK;
 	}
 
 	@Override
 	public void execute() {
 		if (!Variables.isDefined(SCORE_VARIABLE_NAME)) {
 			Variables.setValue(SCORE_VARIABLE_NAME, 0);
 		}
		int score = Integer.parseInt(params.get("value"));
		int newScore = storeScore(score);
		if (score > 0) {
 			GeoQuestApp.playAudio(ResourceManager.POSITIVE_SOUND, false);
 			GeoQuestApp.showMessage(ctx.getText(R.string.scoreIncreasedTo)
					+ " " + newScore);
 		}
		if (score < 0) {
 			GeoQuestApp.playAudio(ResourceManager.NEGATIVE_SOUND, false);
 			GeoQuestApp.showMessage(ctx.getText(R.string.scoreDecreasedTo)
					+ " " + newScore);
 		}
		storeScore(score);
 	}
 
	private int storeScore(int score) {
		String varName = SCORE_VARIABLE_NAME;
		Variables.setValue(varName, (Integer) Variables.getValue(varName)
				+ score);
		return (Integer) Variables.getValue(varName);
 	}
 }
