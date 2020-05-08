 package com.zotca.vbc.dbhistory;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 
 import com.zotca.vbc.dbhistory.core.CardDatabase.Card;
 import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.util.Pair;
 
 public class CardHistoryPagerAdapter extends FragmentStatePagerAdapter {
 	
 	private ArrayList<Pair<Long, Card>> mHistory;
 	
 	public CardHistoryPagerAdapter(FragmentManager fm, DatabaseFileManager dm, int id) {
 		super(fm);
 		mHistory = new ArrayList<Pair<Long, Card>>(dm.getCard(id));
 	}
 
 	public int getIndexOf(long time) {
 		int len = mHistory.size();
 		for (int i = 0; i < len; i++)
 		{
 			if (mHistory.get(i).first == time) return i;
 		}
 		return -1;
 	}
 	
 	@Override
 	public Fragment getItem(int i) {
 		Pair<Long, Card> pair = mHistory.get(i);
 		Fragment f = new CardHistoryFragment();
 		Bundle args = new Bundle();
 		args.putSerializable(CardHistoryFragment.ARG_CARD, pair.second);
 		f.setArguments(args);
 		return f;
 	}
 
 	@Override
 	public int getCount() {
 		return mHistory.size();
 	}
 
 	@Override
 	public CharSequence getPageTitle(int position) {
 		Pair<Long, Card> pair = mHistory.get(position);
 		Date date = new Date(pair.first);
 		return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, Locale.getDefault())
 				   .format(date);
 	}
 	
 	public String getPageCardName(int position) {
		final Card card = mHistory.get(position).second;
		final String genderStr = card.isFemale() ? "f" : "m";
		return genderStr + card.getName();
 	}
 }
