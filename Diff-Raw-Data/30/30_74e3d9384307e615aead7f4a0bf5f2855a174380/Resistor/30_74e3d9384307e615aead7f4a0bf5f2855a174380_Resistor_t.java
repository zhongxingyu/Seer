 /*
  * Copyright (C) 2012 Jamie Nicol <jamie@thenicols.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jamienicol.resistors;
 
 import android.content.Context;
 import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Resistor extends LinearLayout {
 
	public interface OnResistanceChangeListener {
		public abstract void onResistanceChange (Resistance resistance);
	}

 	private List<Band> bands;
 	private Resistance resistance;
	private OnResistanceChangeListener onResistanceChangeListener;
 
 	public Resistor (Context context) {
 		this (context, null);
 	}
 
 	public Resistor (Context context, AttributeSet attrs) {
 		this (context, attrs, 0);
 	}
 
 	public Resistor (Context context, AttributeSet attrs, int defStyle) {
 		super (context, attrs);
 
 		bands = new ArrayList<Band> ();
 		resistance = new Resistance ();
		onResistanceChangeListener = null;
 	}
 
 	@Override
 	protected void onFinishInflate () {
 
 		/* update resistance when a band changes value */
 		SimpleOnPageChangeListener listener = new SimpleOnPageChangeListener () {
 			public void onPageSelected (int position) {
 				updateResistance ();
				if (onResistanceChangeListener != null) {
					onResistanceChangeListener.onResistanceChange (resistance);
				}
 			}
 		};
 
 		/* by the time this function is called all the child views
 		 * have been added. this means we have access to our bands.
 		 * set their listener and store them in a list so they
 		 * can be iterated through when calculating resistance */
 		for (int i = 0; i < getChildCount (); i++) {
 			View view = getChildAt (i);
 			if (view instanceof Band) {
 				Band band = (Band)view;
 				bands.add (band);
 				band.setOnPageChangeListener (listener);
 			}
 		}
 		updateResistance ();
 
 		/* the docs say to always call the super method */
 		super.onFinishInflate ();
 	}
 
 	private void updateResistance () {
 		resistance = new Resistance ();
 		for (Band band : bands) {
 			band.processResistance (resistance);
 		}
 	}
 
 	public Resistance getResistance () {
 		return resistance;
 	}

	public void setOnResistanceChangeListener (OnResistanceChangeListener listener) {
		onResistanceChangeListener = listener;
	}
 }
