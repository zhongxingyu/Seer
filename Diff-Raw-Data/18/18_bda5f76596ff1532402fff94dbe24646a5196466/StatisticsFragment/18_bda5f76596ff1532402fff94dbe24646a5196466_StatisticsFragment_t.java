 package com.group5.diceroller;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.graphics.Color;
 import android.util.Log;
 import android.os.Bundle;
 import android.app.Activity;
 
 /**
  * Fragment which shows the history of dice rolls.
  *
  * @Author Carlos Valera
  */
 public class StatisticsFragment extends Fragment {
     DiceRollerState state;
 
    boolean rolls_visible = true;
 
     TextView avg_rolls;
     TextView num_rolls;
     ListView per_dice_stats;
 
     ListView all_rolls;
     View stats_view;
     TextView no_statistics_message;
     Button change_button;
 
     HistoryAdapter all_rolls_adapter;
 
     public static final String kTag = "StatisticsFrag";
     public static final int kDicePerRow = 5;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         state = DiceRollerState.getState();
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         Log.i(kTag, "Creating view");
 
         View layout = inflater.inflate(R.layout.statistics, container, false);
 
         no_statistics_message = (TextView) (layout.findViewById(R.id.no_statistics_message));
 
         all_rolls = (ListView) layout.findViewById(R.id.all_rolls_view);
         all_rolls_adapter = new HistoryAdapter();
         all_rolls.setAdapter(all_rolls_adapter);
 
         num_rolls = (TextView) layout.findViewById(R.id.num_rolls_val_text);
         avg_rolls = (TextView) layout.findViewById(R.id.avg_rolls_val_text);
 
         stats_view = layout.findViewById(R.id.stats_view);
         change_button = (Button) layout.findViewById(R.id.change_button);
         change_button.setOnClickListener(new ChangeOnClickListener());
 
         update();
         return layout;
     }
 
     /**
      * Takes a view of the form described by statistics_row.xml, and a
      * SetSelection, and creates/attaches the proper adapters to its internal
      * listviews.
      *
      * @param view The view to populate.
      * @param position The position in the roll history of the selection to represent
      */
     private void populateStatsRow(View view, int position) {
         Log.i(kTag, "Populating roll view");
 
         SetSelection selection = state.rollHistory().get(position);
         ViewHolder holder = (ViewHolder) view.getTag();
 
         Date date = state.rollDates().get(position);
         SimpleDateFormat formatter = new SimpleDateFormat("dd/MM hh:mm:ss a");
         holder.date_text.setText(formatter.format(date));
 
         holder.rolls.removeAllViews();
         LayoutInflater inflater = getActivity().getLayoutInflater();
         GridCreator creator = new GridCreator(holder.rolls, kDicePerRow);
 
         for (DiceSet set : selection) {
             TextView description = new TextView(getActivity().getApplicationContext());
             description.setText(String.format("%s: %d", set.label(), set.sum()));
             description.setTextColor(Color.BLACK);
 
             LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
             float screen_density = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
             params.setMargins(0, (int)(15*screen_density), 0, 0);
 
             creator.addDirect(description, params);
 
             for (Dice dice : set) {
                 for (Integer val : dice) {
                     View die = inflater.inflate(R.layout.dice_box, holder.rolls, false);
 
                     TextView value_view = (TextView) (die.findViewById(R.id.die_value));
                     value_view.setText("" + val);
 
                     TextView face_view = (TextView) (die.findViewById(R.id.die_face_count));
                     face_view.setText("" + dice.faces);
 
                     creator.add(die);
                 }
             }
         }
     }
 
     private void chooseChangeButtonEnable() {
         boolean enable_button;
 
         if (state.rollHistory().size() > 0) {
             enable_button = true;
             no_statistics_message.setVisibility(View.GONE);
             change_button.setEnabled(true);
             if (rolls_visible) {
                change_button.setText(getResources().getString(R.string.rolls));
                 all_rolls.setVisibility(View.VISIBLE);
                 stats_view.setVisibility(View.GONE);
             } else {
                change_button.setText(getResources().getString(R.string.stats));
                 all_rolls.setVisibility(View.GONE);
                 stats_view.setVisibility(View.VISIBLE);
             }
         } else {
             enable_button = false;
             no_statistics_message.setVisibility(View.VISIBLE);
             change_button.setEnabled(false);
         }
     }
 
     /**
      * Updates all views related to dice statistics.
      */
     public void update() {
         chooseChangeButtonEnable();
         all_rolls_adapter.notifyDataSetChanged();
 
         // update statistics views
         num_rolls.setText("" + state.getNumRolls());
         avg_rolls.setText(String.format("%.2f", state.getAvgRolls()));
     }
 
     class ChangeOnClickListener implements View.OnClickListener {
         public void onClick(View v) {
             rolls_visible = !rolls_visible;
            update();
         }
     }
 
     /**
      * Class for automatically adding to a table like you would a grid. Useful
      * to create "static" grids. i.e. grids that don't scroll and which can be
      * put in a scrollable list.
      */
     class GridCreator {
         LinearLayout grid;
         int row;
         int col;
         int max_cols;
 
         LinearLayout cur_row;
 
         public GridCreator(LinearLayout grid, int max_cols) {
             this.grid = grid;
             this.row = 0;
             this.col = 0;
             this.max_cols = max_cols;
         }
 
         public void add(View v) {
             if (col == 0 || col == max_cols) { 
                 ++row;
                 cur_row = new LinearLayout(getActivity().getApplicationContext());
                 grid.addView(cur_row);
                 col = 1;
             } else {
                 ++col;
             }
             cur_row.addView(v);
         }
 
         public void addDirect(View v, LayoutParams params) {
             col = 0;
             if (params == null)
                 grid.addView(v);
             else
                 grid.addView(v, params);
         }
 
         public void addDirect(View v) {
             addDirect(v, null);
         }
 
         public void newRow() {
             col = 0;
         }
 
     }
 
     class HistoryAdapter extends ArrayAdapter<SetSelection> {
         public HistoryAdapter() {
             super(getActivity(), R.layout.statistics_row, state.rollHistory());
         }
 
         public View getView(int position, View convertView, ViewGroup parent) {
             View row = convertView;
             if (row == null) {
                 LayoutInflater inflater = getActivity().getLayoutInflater();
                 row = inflater.inflate(R.layout.statistics_row, parent, false);
 
                 ViewHolder holder = new ViewHolder();
                 holder.rolls = (LinearLayout) (row.findViewById(R.id.dice_rolls_grid));
                 holder.date_text = (TextView) (row.findViewById(R.id.date_text));
 
                 row.setTag(holder);
             }
             populateStatsRow(row, position);
             return row;
         }
     }
 
     static class ViewHolder {
         LinearLayout rolls;
         TextView date_text;
     }
 }
