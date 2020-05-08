 /*******************************************************************************
 * Copyright (c) 2013 See AUTHORS file. This file is part of SleepFighter.
 * SleepFighter is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. SleepFighter is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.android.preference;
 
 import se.chalmers.dat255.sleepfighter.R;
 import android.content.Context;
 import android.preference.CheckBoxPreference;
 import android.util.AttributeSet;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 /**
  * <p>EnablePlusSettingsPreference is a preference used for<br/>
  * a one in two scenario where you can enable disable something<br/>
  * and where the setting has more options.</p>
  *
  * <p>It has a checkbox + a settings button that you may bind to a custom action</p>
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 5, 2013
  */
 public class EnablePlusSettingsPreference extends CheckBoxPreference {
 	private boolean useButton = true;
 	private Button button;
 	private OnClickListener listener;
 
 	private Integer titleColor = null;
 
 	private View view;
 
 	public EnablePlusSettingsPreference( Context context, AttributeSet attrs, int defStyle ) {
 		super( context, attrs );
 
 		this.setLayoutResource( R.layout.preference_enable_plus_settings );
 	}
 
 	public EnablePlusSettingsPreference( Context context, AttributeSet attrs ) {
 		this( context, attrs, 0 );
 	}
 
 	public EnablePlusSettingsPreference( Context context ) {
 		this( context, null );
 	}
 
 	@Override
 	protected void onBindView( final View view ) {
 		super.onBindView( view );
 
 		this.view = view;
 
 		this.updateTitleColor();
 
 		this.button = (Button) view.findViewById( R.id.preference_enable_plus_settings_button );
 		if ( this.useButton ) {
 			if ( this.listener != null ) {
 				this.button.setOnClickListener( this.listener );
 			}
 		} else {
 			this.hideButton();
 		}
 	}
 
 	/**
 	 * Sets the title color of the preference.
 	 *
 	 * @param color the color.
 	 */
 	public void setTitleColor( int color ) {
 		this.titleColor = color;
 
 		if ( this.view != null ) {
 			this.updateTitleColor();
 		}
 	}
 
 	/**
 	 * Updates the title color if set.
 	 */
 	private void updateTitleColor() {
 		if ( this.titleColor == null ) {
 			return;
 		}
 
 		TextView tv = (TextView) this.view.findViewById( android.R.id.title );
 		tv.setTextColor( this.titleColor );
 	}
 
 	/**
 	 * Sets whether or not to use button.<br/>
 	 * Default is true.
 	 *
 	 * @param useButton true = use, false = hide.
 	 */
 	public void setUseButton( boolean useButton ) {
 		this.useButton = useButton;
 
 		if ( this.button != null ) {
 			this.hideButton();
 		}
 	}
 
 	/**
 	 * Hides the button.
 	 */
 	private void hideButton() {
 		this.button.setVisibility( View.INVISIBLE );
 	}
 
 	/**
 	 * Sets the listener that receives button clicks.
 	 *
 	 * @param listener the listener to bind to button.
 	 */
 	public void setOnButtonClickListener( OnClickListener listener ) {
 		this.listener = listener;
 		if ( this.button != null ) {
 			this.button.setOnClickListener( listener  );
 		}
 	}
 }
