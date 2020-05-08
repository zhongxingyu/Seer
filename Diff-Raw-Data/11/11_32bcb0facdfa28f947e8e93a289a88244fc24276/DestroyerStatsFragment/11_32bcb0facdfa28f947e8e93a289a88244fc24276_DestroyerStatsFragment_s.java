 package com.example.bato;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Fragment;
import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 public class DestroyerStatsFragment extends Fragment
 {
 	private String[] mHighScores = null;
	Context mContext;
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
 	{
 		View view = inflater.inflate(R.layout.fragment_destroyer_stats, container, false);
		mContext  = this.getActivity();
 		view.findViewById(R.id.destroyer_stats_play_game).setOnClickListener(new OnClickListener()
 		{			
 			@Override
 			public void onClick(View v)
 			{
				Intent i = new Intent(mContext, DestroyerView.class);				
				getActivity().startActivity(i);
 			}
 		});
 		
 		Cursor cursor = null;
 		
 		CalendarDbAdapter calendarDbHelper = new CalendarDbAdapter(getActivity());
 		calendarDbHelper.open();
 		
 		cursor = calendarDbHelper.fetchThoughts();
 		int personalPoints = cursor.getCount() * 25;		
 		
 		cursor.close();
 		calendarDbHelper.close();		
 		
 		((TextView) view.findViewById(R.id.destroyer_stats_personal_points)).setText(String.valueOf(personalPoints));
 		
 		GameDbAdapter gameDbHelper = new GameDbAdapter(getActivity());		
 		gameDbHelper.open();		
 		
 		cursor = gameDbHelper.fetchGames();
 		
 		int column = cursor.getColumnIndexOrThrow(GameDbAdapter.COLUMN_NAME_SUCCESS);	
 		int negativesDestroyed = 0;
 		
 		cursor.moveToFirst();
 		
 		for (int i = 0; i < cursor.getCount(); i++)
 		{		
 			if (cursor.getString(column).contains("Yes"))
 				negativesDestroyed++;
 			
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		
 		((TextView) view.findViewById(R.id.destroyer_stats_negatives_destroyed)).setText(String.valueOf(negativesDestroyed));
 		
 		cursor = gameDbHelper.fetchHighScores();
 		
 		if (cursor.getCount() > 0)
 		{
 			mHighScores = new String[cursor.getCount()];
 			
 			cursor.moveToFirst();
 			
 			for (int i = 0; i < mHighScores.length; i++)
 			{					
 				mHighScores[i] = cursor.getString(column);
 				cursor.moveToNext();
 			}		
 		}
 		else
 		{
 			mHighScores = new String[1];
 			mHighScores[0] = "0";
 			
 			view.findViewById(R.id.destroyer_stats_more_scores).setEnabled(false);	
 		}		
 		
 		cursor.close();
 		gameDbHelper.close();
 		
 		((TextView) view.findViewById(R.id.destroyer_stats_high_score)).setText(mHighScores[0]);
 		
 		view.findViewById(R.id.destroyer_stats_more_scores).setOnClickListener(new OnClickListener()
 		{							
 			@Override
 			public void onClick(View v)
 			{
 				AlertDialog.Builder builder = new Builder(getActivity());
 				
 				builder.setTitle(R.string.high_scores_title);
 				builder.setItems(mHighScores, null);
 				builder.setPositiveButton(android.R.string.ok, null);
 				builder.create().show();				
 			}
 		});
 		
 		return view;
 	}
 }
