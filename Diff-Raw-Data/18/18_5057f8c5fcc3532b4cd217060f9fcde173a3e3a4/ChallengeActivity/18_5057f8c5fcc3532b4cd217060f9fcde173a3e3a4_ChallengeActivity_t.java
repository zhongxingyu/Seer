 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import se.chalmers.dat255.sleepfighter.challenge.Challenge;
 import se.chalmers.dat255.sleepfighter.challenge.motionsnake.MotionSnakeChallenge;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeFactory;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.utils.math.RandomMath;
 import android.app.Activity;
 import android.os.Bundle;
 
 /**
  * An activity for different types of challenges.<br/>
  * Started with a bundled {@link ChallengeType} with name defined by
  * {@code BUNDLE_CHALLENGE_TYPE}. The calling activity can check if the user has
  * completed the challenge by starting this using {@code startActivityForResult}
  * and checking that the {@code resultCode} is {@code Activity.RESULT_OK}.
  */
 public class ChallengeActivity extends Activity {
 	public static final String BUNDLE_CHALLENGE_TYPE = "bundle_challenge_type";
 	private static final String BUNDLE_CHALLENGE_DATA = null;
 
 	private Alarm alarm;
 	private ChallengeConfigSet challengeSet;
 
 	private ChallengeType challengeType;
 	private Challenge challenge;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.alarm = AlarmIntentHelper.fetchAlarmOrPreset(this);
 		this.challengeSet = this.alarm.getChallengeSet();
 
 		if (savedInstanceState == null) {
 			// Either fetch or make a random challenge type.
 			Object bundled = getIntent().getSerializableExtra(
 					BUNDLE_CHALLENGE_TYPE);
 			ChallengeType type = bundled instanceof ChallengeType ? (ChallengeType) bundled
 					: this.makeRandomChallenge();
 
 			this.startFromScratch(type);
 		} else {
 			this.restart(savedInstanceState);
 		}
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		outState.putSerializable(BUNDLE_CHALLENGE_TYPE, this.challengeType);
 
 		outState.putParcelable(BUNDLE_CHALLENGE_DATA,
 				this.challenge.savedState());
 	}
 
 	/**
 	 * Called when a challenge was saved to state.
 	 * 
 	 * @param state
 	 *            the saved state to read from.
 	 */
 	private void restart(Bundle state) {
 		this.challengeType = (ChallengeType) state
 				.getSerializable(BUNDLE_CHALLENGE_TYPE);
 		this.updateChallenge();
 
 		Bundle challengeData = state.getBundle(BUNDLE_CHALLENGE_DATA);
 
 		this.challenge.start(this, challengeData);
 	}
 
 	/**
 	 * Called when no challenge was not saved to state.
 	 */
 	private void startFromScratch(ChallengeType type) {
 		this.challengeType = type;
 		this.updateChallenge();
 		this.challenge.start(this, alarm.getChallengeSet());
 	}
 	
 	/**
 	 * Sets the challenge using current type.
 	 */
 	private void updateChallenge() {
 		this.challenge = ChallengeFactory.getChallenge(this.challengeType);
 	}
 
 	/**
 	 * Generates a random challenge type.
 	 * 
 	 * @return the type.
 	 */
 	private ChallengeType makeRandomChallenge() {
 		// Get a random challenge from the alarm's enabled challenges
 		Set<ChallengeType> enabledChallenges = this.challengeSet
 				.getEnabledTypes();
 
 		List<ChallengeType> list = new ArrayList<ChallengeType>(
 				enabledChallenges);
 		int randPos = RandomMath.nextRandomRanged(new Random(), 0,
 				list.size() - 1);
 
 		ChallengeType type = list.get(randPos);
 
 		return type;
 	}
 
 	/**
 	 * Called by a challenge when it's completed.
 	 */
 	public void complete() {
 		setResult(Activity.RESULT_OK);
 		finish();
 	}
 
 	/**
 	 * Called by a challenge when it has not been completed.
 	 */
 	public void fail() {
 		setResult(Activity.RESULT_CANCELED);
 		finish();
 	}
 
 	/**
 	 * Called when SleepFighter pauses/becomes invisible to user. Necessary for
 	 * MotionSnakeChallenge, so the MotionControl stops consuming power.
 	 */
 	protected void onPause() {
 		super.onPause();
 
 		if (this.challenge instanceof MotionSnakeChallenge
 				&& !((MotionSnakeChallenge) this.challenge).isStopped()) {
 			((MotionSnakeChallenge) challenge).pause();
 		}
 	}
 
 	/**
 	 * When SleepFighter becomes visible for user again. Necessary for
 	 * MotionSnakeChallenge, to start collecting Sensor data again.
 	 */
 	protected void onResume() {
 		super.onResume();
 
 		if (this.challenge instanceof MotionSnakeChallenge
 				&& ((MotionSnakeChallenge) this.challenge).isStopped()) {
 			((MotionSnakeChallenge) challenge).resume();
 		}
 	}
 }
