 /*******************************************************************************
  * Copyright (C) 2013-2014 Artem Yankovskiy (artemyankovskiy@gmail.com).
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  * 
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *     
  * 	Modification:
  * 		2014/03/03 Rudy Dordonne (rudy@itu.dk)
  ******************************************************************************/
 package ru.neverdark.phototools.fragments;
 
 import kankan.wheel.widget.OnWheelScrollListener;
 import kankan.wheel.widget.WheelView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 import ru.neverdark.phototools.R;
 import ru.neverdark.phototools.utils.Common;
 import ru.neverdark.phototools.utils.Log;
 import ru.neverdark.phototools.utils.evcalculator.EvCalculator;
 import ru.neverdark.phototools.utils.evcalculator.EvData;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 /**
  * Fragment contains EV Pairs calculator UI
  */
 public class EvpairsFragment extends SherlockFragment {
     private class WheelScrollListener implements OnWheelScrollListener {
 
         @Override
         public void onScrollingFinished(WheelView wheel) {
             recalculate();
         }
 
         @Override
         public void onScrollingStarted(WheelView wheel) {
             // TODO Auto-generated method stub
 
         }
 
     }
 
    private static final String CALCULATE_INDEX = "ev1_calculateIndex";
 
    private static final String NEW_APERTURE_INDEX = "ev1_newAperture";
    private static final String NEW_ISO_INDEX = "ev1_newIso";
    private static final String NEW_SHUTTER_INDEX = "ev1_newShutter";
 
    private static final String STEP_INDEX = "ev1_stepIndex";
 
    private String CURRENT_APERTURE_INDEX = "ev1_currentAperture";
    private String CURRENT_ISO_INDEX = "ev1_currentIso";
    private String CURRENT_SHUTTER_INDEX = "ev1_currentShutter";
 
     private SherlockFragmentActivity mActivity;
 
     private int mCalculateIndex;
 
     private int mCurrentAperturePosition;
     private int mCurrentIsoPosition;
     private int mCurrentShutterSpeedPosition;
 
     private EvCalculator mEvCalculator;
 
     private TextView mLabelAperture;
     private TextView mLabelIso;
     private TextView mLabelShutter;
     private TextView mLabelStepFull;
     private TextView mLabelStepHalf;
     private TextView mLabelStepThird;
 
     private int mNewAperturePosition;
     private int mNewIsoPostion;
     private int mNewShutterSpeedPosition;
 
     private int mStepIndex;
 
     private View mView;
 
     private WheelView mWheel_currentAperture;
     private WheelView mWheel_currentIso;
     private WheelView mWheel_currentShutter;
 
     private WheelView mWheel_newAperture;
     private WheelView mWheel_newIso;
     private WheelView mWheel_newShutter;
 
     /**
      * Binds classes objects to resources
      */
     private void bindObjectsToResources() {
         mWheel_currentAperture = (WheelView) mView
                 .findViewById(R.id.ev_wheel_currentAperture);
         mWheel_currentIso = (WheelView) mView
                 .findViewById(R.id.ev_wheel_currentIso);
         mWheel_currentShutter = (WheelView) mView
                 .findViewById(R.id.ev_wheel_currentShutter);
         mWheel_newAperture = (WheelView) mView
                 .findViewById(R.id.ev_wheel_newAperture);
         mWheel_newIso = (WheelView) mView.findViewById(R.id.ev_wheel_newIso);
         mWheel_newShutter = (WheelView) mView
                 .findViewById(R.id.ev_wheel_newShutter);
 
         mLabelStepFull = (TextView) mView.findViewById(R.id.ev_label_stepFull);
         mLabelStepHalf = (TextView) mView.findViewById(R.id.ev_label_stepHalf);
         mLabelStepThird = (TextView) mView
                 .findViewById(R.id.ev_label_stepThird);
 
         mLabelAperture = (TextView) mView
                 .findViewById(R.id.ev_label_newAperture);
         mLabelIso = (TextView) mView.findViewById(R.id.ev_label_newIso);
         mLabelShutter = (TextView) mView.findViewById(R.id.ev_label_newShutter);
 
         mActivity = getSherlockActivity();
     }
 
     private void fillCalculatedWheel(final int index) {
         if (mCalculateIndex == EvCalculator.CALCULATE_APERTURE) {
             mWheel_newAperture.setCurrentItem(index);
         } else if (mCalculateIndex == EvCalculator.CALCULATE_ISO) {
             mWheel_newIso.setCurrentItem(index);
         } else {
             mWheel_newShutter.setCurrentItem(index);
         }
     }
 
     /**
      * Function gets selected items positions for spinner
      */
     private void getSelectedItemsPositions() {
         mCurrentAperturePosition = mWheel_currentAperture.getCurrentItem();
         mCurrentIsoPosition = mWheel_currentIso.getCurrentItem();
         mCurrentShutterSpeedPosition = mWheel_currentShutter.getCurrentItem();
 
         mNewAperturePosition = mWheel_newAperture.getCurrentItem();
         mNewIsoPostion = mWheel_newIso.getCurrentItem();
         mNewShutterSpeedPosition = mWheel_newShutter.getCurrentItem();
     }
 
     /**
      * Load saved values from shared prefs
      */
     private void loadValues() {
         SharedPreferences preferenced = getActivity().getPreferences(
                 Context.MODE_PRIVATE);
 
         mStepIndex = preferenced.getInt(STEP_INDEX, EvData.FULL_STOP);
         mCalculateIndex = preferenced.getInt(CALCULATE_INDEX,
                 EvCalculator.CALCULATE_SHUTTER);
 
         mCurrentAperturePosition = preferenced
                 .getInt(CURRENT_APERTURE_INDEX, 0);
         mCurrentIsoPosition = preferenced.getInt(CURRENT_ISO_INDEX, 0);
         mCurrentShutterSpeedPosition = preferenced.getInt(
                 CURRENT_SHUTTER_INDEX, 0);
 
         mNewAperturePosition = preferenced.getInt(NEW_APERTURE_INDEX, 0);
         mNewIsoPostion = preferenced.getInt(NEW_ISO_INDEX, 0);
         mNewShutterSpeedPosition = preferenced.getInt(NEW_SHUTTER_INDEX, 0);
     }
 
     private void lockCalculateWheel() {
         mWheel_newAperture
                 .setEnabled(mCalculateIndex != EvCalculator.CALCULATE_APERTURE);
         mWheel_newIso.setEnabled(mCalculateIndex != EvCalculator.CALCULATE_ISO);
         mWheel_newShutter
                 .setEnabled(mCalculateIndex != EvCalculator.CALCULATE_SHUTTER);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
      * android.view.ViewGroup, android.os.Bundle)
      */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         mView = inflater.inflate(R.layout.activity_evpairs, container, false);
         bindObjectsToResources();
         setCyclicToWheels();
 
         mEvCalculator = new EvCalculator();
 
         loadValues();
         updateStepButtons();
         updateCalculateButtons();
         lockCalculateWheel();
         setOldWheelPositions();
 
         setClickListener();
         wheelsHandler();
         recalculate();
         
         return mView;
     }
 
     @Override
     public void onPause() {
         super.onPause();
         saveValues();
     }
 
     /**
      * Calculates EV pairs
      */
     private void recalculate() {
         getSelectedItemsPositions();
 
         mEvCalculator.prepare(mCurrentAperturePosition, mCurrentIsoPosition,
                 mCurrentShutterSpeedPosition, mNewAperturePosition,
                 mNewIsoPostion, mNewShutterSpeedPosition, mCalculateIndex);
 
         int index = mEvCalculator.calculate();
         boolean isError = false;
 
         if (index != EvData.INVALID_INDEX) {
             fillCalculatedWheel(index);
         } else {
             isError = true;
         }
 
         setErrorHighlight(isError);
     }
 
     /**
      * Save values into shared prefs
      */
     private void saveValues() {
         SharedPreferences preferenced = getActivity().getPreferences(
                 Context.MODE_PRIVATE);
 
         getSelectedItemsPositions();
 
         SharedPreferences.Editor editor = preferenced.edit();
         editor.putInt(STEP_INDEX, mStepIndex);
         editor.putInt(CALCULATE_INDEX, mCalculateIndex);
 
         editor.putInt(CURRENT_APERTURE_INDEX, mCurrentAperturePosition);
         editor.putInt(CURRENT_ISO_INDEX, mCurrentIsoPosition);
         editor.putInt(CURRENT_SHUTTER_INDEX, mCurrentShutterSpeedPosition);
 
         editor.putInt(NEW_APERTURE_INDEX, mNewAperturePosition);
         editor.putInt(NEW_ISO_INDEX, mNewIsoPostion);
         editor.putInt(NEW_SHUTTER_INDEX, mNewShutterSpeedPosition);
 
         editor.commit();
     }
 
     /**
      * Sets position to first for all wheels
      */
     private void setAllWheelsToFirstPos() {
         setWheelToFirstPos(mWheel_currentAperture);
         setWheelToFirstPos(mWheel_currentIso);
         setWheelToFirstPos(mWheel_currentShutter);
 
         setWheelToFirstPos(mWheel_newAperture);
         setWheelToFirstPos(mWheel_newIso);
         setWheelToFirstPos(mWheel_newShutter);
     }
 
     /**
      * Sets OnClickListener to buttons on the fragment
      */
     private void setClickListener() {
         OnClickListener clickListener = new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 switch (v.getId()) {
                 case R.id.ev_label_newAperture:
                     mCalculateIndex = EvCalculator.CALCULATE_APERTURE;
                     updateCalculateButtons();
                     lockCalculateWheel();
                     recalculate();
                     break;
                 case R.id.ev_label_newIso:
                     mCalculateIndex = EvCalculator.CALCULATE_ISO;
                     updateCalculateButtons();
                     lockCalculateWheel();
                     recalculate();
                     break;
                 case R.id.ev_label_newShutter:
                     mCalculateIndex = EvCalculator.CALCULATE_SHUTTER;
                     updateCalculateButtons();
                     lockCalculateWheel();
                     recalculate();
                     break;
                 case R.id.ev_label_stepFull:
                     mStepIndex = EvData.FULL_STOP;
                     updateStepButtons();
                     setAllWheelsToFirstPos();
                     break;
                 case R.id.ev_label_stepHalf:
                     mStepIndex = EvData.HALF_STOP;
                     updateStepButtons();
                     setAllWheelsToFirstPos();
                     break;
                 case R.id.ev_label_stepThird:
                     mStepIndex = EvData.THIRD_STOP;
                     updateStepButtons();
                     setAllWheelsToFirstPos();
                     break;
                 }
             }
         };
 
         mLabelStepFull.setOnClickListener(clickListener);
         mLabelStepHalf.setOnClickListener(clickListener);
         mLabelStepThird.setOnClickListener(clickListener);
 
         mLabelAperture.setOnClickListener(clickListener);
         mLabelIso.setOnClickListener(clickListener);
         mLabelShutter.setOnClickListener(clickListener);
     }
 
     /**
      * Sets wheels to cyclic
      */
     private void setCyclicToWheels() {
         mWheel_currentAperture.setCyclic(true);
         mWheel_currentIso.setCyclic(true);
         mWheel_currentShutter.setCyclic(true);
 
         mWheel_newAperture.setCyclic(true);
         mWheel_newIso.setCyclic(true);
         mWheel_newShutter.setCyclic(true);
     }
 
     /**
      * Sets red highlights for current item in calculated wheel
      * 
      * @param isError
      *            true for highlights red or false for back to normal
      */
     private void setErrorHighlight(boolean isError) {
         int normal = R.drawable.wheel_val;
         int error = R.drawable.wheel_val_err;
 
         mWheel_newAperture.setCurrentDrawable(normal);
         mWheel_newIso.setCurrentDrawable(normal);
         mWheel_newShutter.setCurrentDrawable(normal);
 
         if (isError) {
             switch (mCalculateIndex) {
             case EvCalculator.CALCULATE_APERTURE:
                 mWheel_newAperture.setCurrentDrawable(error);
                 break;
             case EvCalculator.CALCULATE_ISO:
                 mWheel_newIso.setCurrentDrawable(error);
                 break;
             case EvCalculator.CALCULATE_SHUTTER:
                 mWheel_newShutter.setCurrentDrawable(error);
                 break;
             }
         }
     }
 
     /**
      * Sets old wheel positions
      */
     private void setOldWheelPositions() {
         mWheel_currentAperture.setCurrentItem(mCurrentAperturePosition);
         mWheel_currentIso.setCurrentItem(mCurrentIsoPosition);
         mWheel_currentShutter.setCurrentItem(mCurrentShutterSpeedPosition);
 
         mWheel_newAperture.setCurrentItem(mNewAperturePosition);
         mWheel_newIso.setCurrentItem(mNewIsoPostion);
         mWheel_newShutter.setCurrentItem(mNewShutterSpeedPosition);
     }
 
     /**
      * Sets position to first
      * 
      * @param wheel
      *            wheel for setting first position
      */
     private void setWheelToFirstPos(WheelView wheel) {
         wheel.setCurrentItem(0);
     }
 
     private void updateCalculateButtons() {
         Common.setBg(mLabelAperture, R.drawable.right_stroke);
         Common.setBg(mLabelIso, R.drawable.right_stroke);
         Common.setBg(mLabelShutter, R.drawable.right_stroke);
 
         switch (mCalculateIndex) {
         case EvCalculator.CALCULATE_APERTURE:
             Common.setBg(mLabelAperture, R.drawable.left_blue_button);
             break;
         case EvCalculator.CALCULATE_ISO:
             Common.setBg(mLabelIso, R.drawable.middle_blue_button);
             break;
         case EvCalculator.CALCULATE_SHUTTER:
             Common.setBg(mLabelShutter, R.drawable.right_blue_button);
             break;
         }
     }
 
     /**
      * Update steps
      * 
      */
     private void updateStep() {
         Log.enter();
 
         final int textSize = R.dimen.wheelTextSize;
         mEvCalculator.initArrays(mStepIndex);
 
         Common.setWheelAdapter(mActivity, mWheel_currentAperture,
                 mEvCalculator.getApertureList(), textSize, false);
         Common.setWheelAdapter(mActivity, mWheel_currentIso,
                 mEvCalculator.getIsoList(), textSize, false);
         Common.setWheelAdapter(mActivity, mWheel_currentShutter,
                 mEvCalculator.getShutterList(), R.dimen.wheelSutterTextSize,
                 false);
 
         Common.setWheelAdapter(mActivity, mWheel_newAperture,
                 mEvCalculator.getApertureList(), textSize, false);
         Common.setWheelAdapter(mActivity, mWheel_newIso,
                 mEvCalculator.getIsoList(), textSize, false);
         Common.setWheelAdapter(mActivity, mWheel_newShutter,
                 mEvCalculator.getShutterList(), R.dimen.wheelSutterTextSize,
                 false);
     }
 
     private void updateStepButtons() {
         Common.setBg(mLabelStepFull, R.drawable.right_stroke);
         Common.setBg(mLabelStepHalf, R.drawable.right_stroke);
         Common.setBg(mLabelStepThird, R.drawable.right_stroke);
 
         switch (mStepIndex) {
         case EvData.FULL_STOP:
             Common.setBg(mLabelStepFull, R.drawable.left_blue_button);
             break;
         case EvData.HALF_STOP:
             Common.setBg(mLabelStepHalf, R.drawable.middle_blue_button);
             break;
         case EvData.THIRD_STOP:
             Common.setBg(mLabelStepThird, R.drawable.right_blue_button);
             break;
         }
 
         updateStep();
     }
 
     private void wheelsHandler() {
         mWheel_currentAperture.addScrollingListener(new WheelScrollListener());
         mWheel_currentIso.addScrollingListener(new WheelScrollListener());
         mWheel_currentShutter.addScrollingListener(new WheelScrollListener());
         mWheel_newAperture.addScrollingListener(new WheelScrollListener());
         mWheel_newIso.addScrollingListener(new WheelScrollListener());
         mWheel_newShutter.addScrollingListener(new WheelScrollListener());
     }
 }
