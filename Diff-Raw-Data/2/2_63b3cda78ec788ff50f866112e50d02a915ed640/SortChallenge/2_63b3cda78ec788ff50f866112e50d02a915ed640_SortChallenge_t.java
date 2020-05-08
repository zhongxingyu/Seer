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
 package se.chalmers.dat255.sleepfighter.challenge.sort;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.activity.ChallengeActivity;
 import se.chalmers.dat255.sleepfighter.challenge.Challenge;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengePrototypeDefinition;
 import se.chalmers.dat255.sleepfighter.challenge.ChallengeResolvedParams;
 import se.chalmers.dat255.sleepfighter.challenge.sort.SortModel.Order;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.utils.math.RandomMath;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.google.common.collect.Lists;
 
 /**
  * SortChallenge is a challenge where user sorts generated numbers ASC/DESC.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 6, 2013
  */
 public class SortChallenge implements Challenge {
 	/**
 	 * PrototypeDefinition for SortChallenge.
 	 *
 	 * @version 1.0
 	 * @since Oct 5, 2013
 	 */
 	public static class PrototypeDefinition extends ChallengePrototypeDefinition {{
 		setType( ChallengeType.SORT );
 		add( "color_confusion", PrimitiveValueType.BOOLEAN, true );
 		add( "color_saturation_confusion", PrimitiveValueType.BOOLEAN, true );
 	}}
 
 	private static final String STATE_COLORS = "hues";
 	private static final String STATE_SHUFFLED_NUMBERS = "numbers";
 	private static final String STATE_MODEL = "model";
 
 	private static final int HSV_MAX_HUE = 360;
 	private static final int HSV_MIN_HUE = 0;
 	private static final float HSV_SATURATION_MIN = 0.20f;
 	private static final float HSV_VALUE = 1f;
 
 	// Unfortunately, colors must be hard-coded since it is dynamic.
 	private static final int COLOR_PRESS = Color.BLACK;
 	private static final int COLOR_ANSWERED = Color.WHITE;
 
 	private static final int NUMBERS_COUNT = 9;
 
 	private ChallengeActivity activity;
 
 	private TextView description;
 
 	private List<Button> buttons;
 
 	private Random rng;
 
 	// Model states.
 	private SortModel model;
 
 	private int[] currentColors;
 
 	private int[] shuffledNumbers;
 
 	// Config param variables:
 	private boolean colorConfusion;
 	private boolean saturationConfusion;
 
 	@Override
 	public void start( ChallengeActivity activity, ChallengeResolvedParams params ) {
 		this.startCommon( activity, params );
 
 		this.setupModel();
 
 		this.updateNumbers();
 	}
 
 	@Override
 	public void start( ChallengeActivity activity, ChallengeResolvedParams params, Bundle state ) {
 		this.startCommon( activity, params );
 
 		// Read stuff from state.
 		this.model = state.getParcelable( STATE_MODEL );
 		this.shuffledNumbers = state.getIntArray( STATE_SHUFFLED_NUMBERS );
 
 		if ( this.colorConfusion ) {
 			this.currentColors = state.getIntArray( STATE_COLORS );
 		}
 
 		this.setNumbers();
 	}
 
 	/**
 	 * Performs common startup stuff.
 	 *
 	 * @param activity the ChallengeActivity.
 	 * @param params the resolved config params.
 	 */
	private void startCommon( ChallengeActivity activity, ChallengeResolvedParams params ) {
 		// Store all interesting params.
 		this.colorConfusion = params.getBoolean( "color_confusion" );
 		this.saturationConfusion = params.getBoolean( "color_saturation_confusion" );
 
 		// Init activity, buttons, etc.
 		this.activity = activity;
 		this.activity.setContentView( R.layout.challenge_sort );
 
 		this.description = (TextView) this.activity.findViewById( R.id.challenge_sort_description );
 
 		this.bindButtons();
 
 		this.rng = new Random();
 	}
 
 	@Override
 	public Bundle savedState() {
 		Bundle outState = new Bundle();
 
 		outState.putParcelable( STATE_MODEL, this.model );
 		outState.putIntArray( STATE_SHUFFLED_NUMBERS, this.shuffledNumbers );
 
 		if ( this.colorConfusion ) {
 			outState.putIntArray( STATE_COLORS, this.currentColors );
 		}
 
 		return outState;
 	}
 
 	/**
 	 * Sets up the model.
 	 */
 	private void setupModel() {
 		this.model = new SortModel();
 		this.model.setSize( NUMBERS_COUNT );
 	}
 
 	/**
 	 * Makes and returns a NumberListGenerator to use.
 	 *
 	 * @return the made generator.
 	 */
 	private NumberListGenerator makeGenerator() {
 		return this.rng.nextBoolean() ? new ClusteredGaussianListGenerator() : new PermutatingListGenerator();
 	}
 
 	/**
 	 * Randomizes an array of colors (in HSV) with size.
 	 *
 	 * @param size the size of array.
 	 * @return the array of colors of size.
 	 */
 	private int[] selectColors( int size ) {
 		int[] colors = new int[size];
 
 		for ( int i = 0; i < colors.length; ++i ) {
 			// Generate hue.
 			int hue = RandomMath.nextRandomRanged( this.rng, HSV_MIN_HUE, HSV_MAX_HUE / 36 ) * 36;
 
 			// Generate saturation.
 			float saturation;
 			if ( this.saturationConfusion ) {
 				int minSat = (int) (HSV_SATURATION_MIN * 100);
 				saturation = RandomMath.nextRandomRanged( this.rng, minSat, 100 - minSat ) / 100f;
 			} else {
 				saturation = HSV_SATURATION_MIN;
 			}
 
 			colors[i] = this.computeHSVWithHue( hue, saturation );
 		}
 
 		return colors;
 	}
 
 	/**
 	 * Completes a hue and saturation with hard coded value making an ARGB color.
 	 *
 	 * @param hue the hue to use in color.
 	 * @param saturation the saturation to use in color.
 	 * @return the color.
 	 */
 	private int computeHSVWithHue( int hue, float saturation ) {
 		return Color.HSVToColor( new float[] { hue, saturation, HSV_VALUE } );
 	}
 
 	/**
 	 * Generates a list of numbers from model and sets a shuffled version of them.
 	 */
 	private void updateNumbers() {
 		this.model.setGenerator( this.makeGenerator() );
 		this.model.generateList( rng );
 		this.shuffledNumbers = this.model.getShuffledList();
 
 		if ( this.colorConfusion ) {
 			this.currentColors = this.selectColors( this.shuffledNumbers.length );
 		}
 
 		this.setNumbers();
 	}
 
 	/**
 	 * Sets the numbers stored in {@link #shuffledNumbers} and updates the description from model.
 	 */
 	private void setNumbers() {
 		this.updateDescription();
 
 		for ( int i = 0; i < this.shuffledNumbers.length; ++i ) {
 			Button button = this.buttons.get( i );
 			button.setEnabled( true );
 
 			button.setText( Integer.toString( this.shuffledNumbers[i]) );
 
 			if ( this.colorConfusion ) {
 				button.setBackgroundColor( this.currentColors[i] );
 			}
 		}
 	}
 
 	/**
 	 * Updates the description text according to model.
 	 */
 	private void updateDescription() {
 		int descriptionId = this.model.getSortOrder() == Order.ASCENDING ? R.string.challenge_sort_ascending : R.string.challenge_sort_descending;
 		this.description.setText( this.activity.getString( descriptionId ) );
 	}
 
 	/**
 	 * Called when the user has sorted correctly.
 	 */
 	private void challengeCompleted() {
 		this.description.setText( this.activity.getString( R.string.challenge_sort_done ) );
 
 		this.activity.complete();
 	}
 
 	/**
 	 * Called when a number button was clicked.<br/>
 	 * If number is next, advance step, else re-generate numbers.
 	 *
 	 * @param button the button.
 	 */
 	private void numberClicked( Button button ) {
 		int number = Integer.parseInt( (String) button.getText() );
 
 		if ( this.model.isNextNumber( number ) ) {
 			this.model.advanceStep( number );
 
 			button.setEnabled( false );
 			button.setBackgroundColor( COLOR_ANSWERED );
 
 			if ( this.model.isFinished() ) {
 				this.challengeCompleted();
 			}
 		} else {
 			this.updateNumbers();
 		}
 	}
 
 	private OnClickListener onButtonClickListener = new OnClickListener() {
 		@Override
 		public void onClick( View v ) {
 			numberClicked( (Button) v );
 		}
 	};
 
 	private OnTouchListener onButtonPressListener = new OnTouchListener() {
 		@Override
 		public boolean onTouch( View v, MotionEvent event ) {
 			switch ( event.getAction() ) {
 			case MotionEvent.ACTION_DOWN:
 				v.setBackgroundColor( COLOR_PRESS );
 				return false;
 
 			default:
 				return false;
 			}
 		}
 	};
 
 	/**
 	 * Called when a number button is clicked.
 	 */
 	private void bindButtons() {
 		View buttonContainer = this.activity.findViewById( R.id.challenge_sort_button_container );
 		ArrayList<View> touchables = buttonContainer.getTouchables();
 
 		this.buttons = Lists.newArrayListWithCapacity( touchables.size() );
 
 		for ( View view : touchables ) {
 			Button button = (Button) view;
 			this.buttons.add( button );
 
 			button.setOnClickListener( this.onButtonClickListener );
 			button.setOnTouchListener( this.onButtonPressListener );
 		}
 	}
 
 	@Override
 	public void onPause() {
 	}
 
 	@Override
 	public void onResume() {
 	}
 
 	@Override
 	public void onDestroy() {
 	}
 }
