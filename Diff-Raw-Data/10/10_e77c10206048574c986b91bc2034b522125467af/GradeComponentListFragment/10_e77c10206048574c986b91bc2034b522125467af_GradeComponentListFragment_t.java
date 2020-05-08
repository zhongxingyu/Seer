 /*
  * Copyright (C) 2012 Jimmy Theis. Licensed under the MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.jetheis.android.grades.fragment;
 
 import java.text.NumberFormat;
 import java.util.List;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.SeekBar;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.jetheis.android.grades.R;
 import com.jetheis.android.grades.model.Course;
 import com.jetheis.android.grades.model.GradeComponent;
 import com.jetheis.android.grades.model.PercentageGradeComponent;
 import com.jetheis.android.grades.model.PointTotalGradeComponent;
 
 public class GradeComponentListFragment extends SherlockListFragment {
 
     public interface OnGradeComponentSelectedListener {
         public void onGradeComponentSelected(GradeComponentListFragment fragment,
                 GradeComponent gradeComponent);
     }
 
     public interface OnOverallGradeChangedListener {
         public void onOverallGradeChanged();
     }
 
     private Course mCourse;
     private OnGradeComponentSelectedListener mOnGradeComponentSelectedListener;
     private OnOverallGradeChangedListener mOnOverallGradeChangedListener;
 
     public void initialize(Course parentCourse,
             OnGradeComponentSelectedListener onGradeComponentSelectedListener,
             OnOverallGradeChangedListener onOverallGradeChangedListener) {
         mCourse = parentCourse;
         mOnGradeComponentSelectedListener = onGradeComponentSelectedListener;
         mOnOverallGradeChangedListener = onOverallGradeChangedListener;
 
         final ListView listView = getListView();
         listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
         refreshGradeComponents();
     }
 
     public void refreshGradeComponents() {
         mCourse.loadConnectedObjects();
         setListAdapter(new GradeComponentArrayAdapter(getActivity(), mCourse.getGradeComponents()));
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.grade_component_list_fragment, null);
     }
 
     private class GradeComponentArrayAdapter extends ArrayAdapter<GradeComponent> {
 
         private Context mContext;
 
         public GradeComponentArrayAdapter(Context context, List<GradeComponent> gradeComponents) {
             super(context, R.layout.grade_component_list_line_item,
                     R.id.grade_component_list_line_item_name_text_view, gradeComponents);
 
             mContext = context;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View result = convertView;
 
             final int index = position;
 
             if (result == null) {
                 LayoutInflater inflater = (LayoutInflater) mContext
                         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 result = inflater.inflate(R.layout.grade_component_list_line_item, null);
             }
 
             final GradeComponent currentComponent = getItem(position);
 
             TextView nameTextView = (TextView) result
                     .findViewById(R.id.grade_component_list_line_item_name_text_view);
             nameTextView.setText(currentComponent.getName());
 
             final TextView maxTextView = (TextView) result
                     .findViewById(R.id.grade_component_list_line_item_max_text_view);
             final TextView earnedTextView = (TextView) result
                     .findViewById(R.id.grade_component_list_line_item_earned_text_view);
 
             final NumberFormat numberFormat = NumberFormat.getInstance();
             final NumberFormat percentageFormat = NumberFormat.getPercentInstance();
 
             SeekBar earnedSeekBar = (SeekBar) result
                     .findViewById(R.id.grade_component_list_line_item_earned_seek_bar);
 
             if (currentComponent instanceof PointTotalGradeComponent) {
                 final PointTotalGradeComponent pointComponent = (PointTotalGradeComponent) currentComponent;
                 maxTextView.setText(getString(R.string.grade_component_list_fragment_total_points,
                         numberFormat.format(pointComponent.getTotalPoints())));
                 earnedTextView.setText(getString(
                         R.string.grade_component_list_fragment_points_earned,
                         numberFormat.format(pointComponent.getPointsEarned())));
 
                 earnedSeekBar.setMax((int) pointComponent.getTotalPoints());
                 earnedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {
                         currentComponent.save();
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         pointComponent.setPointsEarned(seekBar.getProgress());
                         earnedTextView.setText(getString(
                                 R.string.grade_component_list_fragment_points_earned,
                                 numberFormat.format(pointComponent.getPointsEarned())));
 
                         mOnOverallGradeChangedListener.onOverallGradeChanged();
                     }
                 });
                
                earnedSeekBar.setProgress((int) pointComponent.getPointsEarned());
                
             } else {
                 final PercentageGradeComponent percentageComponent = (PercentageGradeComponent) currentComponent;
                 maxTextView.setText(getString(R.string.grade_component_list_fragment_weight,
                         percentageFormat.format(percentageComponent.getWeight() / 100.0)));
                 earnedTextView
                         .setText(getString(
                                 R.string.grade_component_list_fragment_percentage_earned,
                                 percentageFormat.format(percentageComponent.getEarnedPercentage() / 100.0)));
 
                 earnedSeekBar.setMax(100);
                 earnedSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
 
                     @Override
                     public void onStopTrackingTouch(SeekBar seekBar) {
                         currentComponent.save();
                     }
 
                     @Override
                     public void onStartTrackingTouch(SeekBar seekBar) {
                     }
 
                     @Override
                     public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                         percentageComponent.setEarnedPercentage(seekBar.getProgress());
                         earnedTextView
                                 .setText(getString(
                                         R.string.grade_component_list_fragment_percentage_earned,
                                         percentageFormat.format(percentageComponent
                                                 .getEarnedPercentage() / 100.0)));
                         mOnOverallGradeChangedListener.onOverallGradeChanged();
                     }
                 });
                
                earnedSeekBar.setProgress((int) percentageComponent.getEarnedPercentage());
             }
 
             result.setOnLongClickListener(new OnLongClickListener() {
 
                 @Override
                 public boolean onLongClick(View v) {
                     mOnGradeComponentSelectedListener.onGradeComponentSelected(
                             GradeComponentListFragment.this, getItem(index));
                     return false;
                 }
 
             });
 
             return result;
         }
     }
 
 }
