 package com.antsapps.triples.stats;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.text.format.DateUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.antsapps.triples.GameListActivity;
 import com.antsapps.triples.GameListFragment;
 import com.antsapps.triples.R;
 import com.antsapps.triples.backend.Game;
 import com.antsapps.triples.backend.GameProperty;
 import com.antsapps.triples.backend.Statistics;
 import com.google.common.collect.Lists;
 
 public class StatisticsFragment extends GameListFragment implements
     OnStatisticsChangeListener, OnComparatorChangeListener<Game> {
 
   protected static class StatisticsGamesArrayAdapter extends ArrayAdapter<Game> {
 
     public StatisticsGamesArrayAdapter(Context context, List<Game> games) {
       super(context, R.layout.stats_game_list_item, games);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       View v = convertView;
       if (v == null) {
         LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
             Context.LAYOUT_INFLATER_SERVICE);
         v = vi.inflate(R.layout.stats_game_list_item, null);
       }
 
       Game g = getItem(position);
       if (g != null) {
         ((TextView) v.findViewById(R.id.time_taken)).setText(DateUtils
             .formatElapsedTime(TimeUnit.MILLISECONDS.toSeconds(g
                 .getTimeElapsed())));
         ((TextView) v.findViewById(R.id.date_played)).setText(DateUtils
             .formatDateTime(getContext(), g.getDateStarted().getTime(), 0));
       }
 
       return v;
     }
   }
 
   private GameListActivity mGameListActivity;
 
   private Comparator<Game> mComparator = GameProperty.TIME_ELAPSED
       .createReversableComparator();
 
   private StatisticsGamesServicesView mGameServicesView;
   private StatisticsSelectorView mSelectorView;
   private StatisticsSummaryView mSummaryView;
   private StatisticsListHeaderView mListHeaderView;
 
   @Override
   protected ArrayAdapter<Game> createArrayAdapter() {
     return new StatisticsGamesArrayAdapter(getSherlockActivity(),
         Lists.<Game>newArrayList());
   }
 
   @Override
   public void onAttach(Activity activity) {
     super.onAttach(activity);
     mGameListActivity = (GameListActivity) activity;
   }
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
     ListView listView = (ListView) inflater.inflate(
         R.layout.stats_fragment,
         null);
 
     mGameServicesView = new StatisticsGamesServicesView(getSherlockActivity());
     mGameServicesView.setGameHelper(mGameListActivity.getGameHelper());
     mGameListActivity.setGameHelperListener(mGameServicesView);
     listView.addHeaderView(mGameServicesView, null, false);
 
     mSelectorView = new StatisticsSelectorView(getSherlockActivity());
     mSelectorView.setOnStatisticsChangeListener(this);
     listView.addHeaderView(mSelectorView, null, false);
 
     mSummaryView = new StatisticsSummaryView(getSherlockActivity());
     listView.addHeaderView(mSummaryView, null, false);
 
     mListHeaderView = new StatisticsListHeaderView(getSherlockActivity());
     mListHeaderView.setOnComparatorChangeListener(this);
     listView.addHeaderView(mListHeaderView, null, false);
 
     return listView;
   }
 
   @Override
   public void onStatisticsChange(Statistics statistics) {
     mSummaryView.onStatisticsChange(statistics);
 
     mAdapter.clear();
     for (Game game : statistics.getData()) {
       mAdapter.add(game);
     }
     mAdapter.notifyDataSetChanged();
     mAdapter.sort(mComparator);
   }
 
   @Override
   public void onComparatorChange(Comparator<Game> comparator) {
     mComparator = comparator;
     if (mAdapter != null) {
       mAdapter.sort(mComparator);
     }
   }
 
   @Override
   protected void updateDataSet() {
    mSelectorView.regenerateStatistics();
   }
 }
