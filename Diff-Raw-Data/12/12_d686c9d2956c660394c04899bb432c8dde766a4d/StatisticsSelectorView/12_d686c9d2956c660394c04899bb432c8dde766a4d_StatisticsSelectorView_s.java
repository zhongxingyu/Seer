 package com.antsapps.triples.stats;
 
 import java.util.Map;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.FrameLayout;
 import android.widget.Spinner;
 
 import com.antsapps.triples.R;
 import com.antsapps.triples.backend.Application;
 import com.antsapps.triples.backend.DatePeriod;
 import com.antsapps.triples.backend.NumGamesPeriod;
 import com.antsapps.triples.backend.Period;
 import com.antsapps.triples.backend.Statistics;
 import com.google.common.collect.Maps;
 
 class StatisticsSelectorView extends FrameLayout {
 
   private static final long MS_PER_DAY = (long) 24 * 60 * 60 * 1000;
 
   private static final Map<String, Period> PERIODS = Maps.newLinkedHashMap();
   private Spinner mSpinner;
   private Period mCurrentPeriod;
 
   private OnStatisticsChangeListener mOnStatisticsChangeListener;
 
   public StatisticsSelectorView(Context context) {
     this(context, null);
   }
 
   public StatisticsSelectorView(Context context, AttributeSet attrs) {
     this(context, attrs, 0);
   }
 
   public StatisticsSelectorView(Context context,
       AttributeSet attrs,
       int defStyle) {
     super(context, attrs, defStyle);
 
     LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
         Context.LAYOUT_INFLATER_SERVICE);
     View v = inflater.inflate(R.layout.stats_selector, this);
 
     initSpinner();
   }
 
   private void initSpinner() {
     mSpinner = (Spinner) findViewById(R.id.period_spinner);
 
     ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
         getContext(), android.R.layout.simple_spinner_item);
     adapter
         .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
     initPeriodsMap();
     for (String key : PERIODS.keySet()) {
       adapter.add(key);
     }
     mSpinner.setAdapter(adapter);
 
     mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
       @Override
       public void onItemSelected(AdapterView<?> parent, View view, int pos,
           long id) {
         String string = (String) parent.getItemAtPosition(pos);
         mCurrentPeriod = PERIODS.get(string);
         updateStatistics();
       }
 
       @Override
       public void onNothingSelected(AdapterView<?> arg0) {
       }
     });
   }
 
   private void initPeriodsMap() {
     PERIODS.put(getContext().getString(R.string.all_time),Period.ALL_TIME);
     PERIODS.put(
         getContext().getString(R.string.past_day),
         DatePeriod.fromTimePeriod(1 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_week),
         DatePeriod.fromTimePeriod(7 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_month),
         DatePeriod.fromTimePeriod(30 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_3_months),
         DatePeriod.fromTimePeriod(91 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_6_months),
         DatePeriod.fromTimePeriod(182 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_year),
         DatePeriod.fromTimePeriod(365 * MS_PER_DAY));
     PERIODS.put(
         getContext().getString(R.string.past_10_games),
         new NumGamesPeriod(10));
     PERIODS.put(
         getContext().getString(R.string.past_50_games),
         new NumGamesPeriod(50));
   }
 
   public void
       setOnStatisticsChangeListener(OnStatisticsChangeListener listener) {
     mOnStatisticsChangeListener = listener;
   }
 
   public void regenerateStatistics() {
     updateStatistics();
   }
 
   private void updateStatistics() {
     Statistics statistics = Application
         .getInstance(getContext()).getStatistics(mCurrentPeriod);
     if (mOnStatisticsChangeListener != null) {
       mOnStatisticsChangeListener.onStatisticsChange(statistics);
     }
   }
 }
