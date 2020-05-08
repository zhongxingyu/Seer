 package com.ad.cow;
 
 import java.util.Date;
 
 import android.os.Bundle;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 public class ExperienceActivity extends AbstractActivity {
 	/**
 	 * Необходимые переменные
 	 */
 	private GlobalVar gv;
 	private final float expPerSecond = 0.002777778f;
 
 	private float exp;
 	private long time;
 	private int level;
 
 	/**
 	 * Старт активности
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.experience);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		loadPreferences();
 	}
 
 	private void loadPreferences() {
 		long currentTime = new Date().getTime();
 
 		// Достаем сохраненные данные
 		gv = GlobalVar.getInstance();
 		time = gv.getExpTime();
 		level = gv.getLevel();
 		exp = gv.getExp();
 
 		long diff = currentTime - time;
 		float seconds = diff / 1000;
 		float addExp = seconds * expPerSecond;
 		exp = exp + addExp;
 		gv.setExpTime(new Date().getTime());
 		
 		handleLevelUp();

		TextView levelView = (TextView) findViewById(R.id.level);
		levelView.setText(level+"");
 		
 		TextView experienceView = (TextView) findViewById(R.id.experience);
 		experienceView.setText((int)xpSinceLastLevelUp() + "/" + (int)nettoXpNeededForLevel(level + 1));
 		
 		double percentByExp = nettoXpNeededForLevel(level + 1) / 100;
 		double currentPercent = xpSinceLastLevelUp() / percentByExp;
 		
 		ProgressBar progressView = (ProgressBar) findViewById(R.id.progressBar1);
 		progressView.setProgress((int) currentPercent);
 	}
 
 	/**
 	 * Check if the player has reached enough XP for a levelup
 	 */
 	private void handleLevelUp() {
 		while(xpSinceLastLevelUp() >= nettoXpNeededForLevel(level + 1)) {
 			level++;
 		}
 	}
 
 	/**
 	 * 
 	 * @param level
 	 *            to calculate summed up xp value for
 	 * 
 	 * @return summed up xp value
 	 */
 	public double summedUpXpNeededForLevel(int level) {
 		return 1.75 * Math.pow(level, 2) + 5.00 * level;
 	}
 
 	/**
 	 * 
 	 * @param level
 	 *            to calculate netto xp value for
 	 * 
 	 * @return netto xp value
 	 */
 	public double nettoXpNeededForLevel(int level) {
 		if (level == 0) return 0;
 		return summedUpXpNeededForLevel(level) - summedUpXpNeededForLevel(level - 1);
 	}
 
 	/**
 	 * 
 	 * @return xp gained since last level up
 	 */
 	public double xpSinceLastLevelUp() {
 		return exp - summedUpXpNeededForLevel(level);
 	}
 
 	/**
 	 * При завершении экшена сохраняем данные
 	 */
 	@Override
 	protected void onPause() {
 		gv.setLevel(level);
 		gv.setExp(exp);
 		gv.save();
 
 		super.onPause();
 	}
 }
