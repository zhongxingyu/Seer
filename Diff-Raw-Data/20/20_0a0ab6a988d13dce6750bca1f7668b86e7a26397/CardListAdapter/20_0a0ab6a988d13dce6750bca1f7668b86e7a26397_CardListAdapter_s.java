 package com.zotca.vbc.dbhistory;
 
import java.util.HashMap;

 import com.zotca.vbc.dbhistory.core.CardDatabase;
 import com.zotca.vbc.dbhistory.core.DatabaseDelta;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class CardListAdapter extends ArrayAdapter<CardDatabase.Card> {
 
 	private DatabaseDelta mDelta;
	private HashMap<Integer, DatabaseDelta.DeltaType> mDeltaType;
 	
 	public CardListAdapter(Context context, DatabaseDelta delta) {
 		super(context, R.layout.item_cardlist, R.id.name);
 		for (int id : mDelta.getCardIdSet())
 		{
 			this.add(mDelta.getCard(id));
 			mDeltaType.put(id, mDelta.getDeltaType(id));
 		}
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = super.getView(position, convertView, parent);
 		TextView deltaInfo = (TextView) v.findViewById(R.id.delta_info);
 		CardDatabase.Card card = this.getItem(position);
 		int id = card.getId();
 		switch (mDeltaType.get(id))
 		{
 		case MODIFIED:
 			deltaInfo.setText(R.string.delta_modified);
 			break;
 		case ADDED:
 			deltaInfo.setText(R.string.delta_added);
 			break;
 		case DELETED:
 			deltaInfo.setText(R.string.delta_deleted);
 			break;
 		}
 		return v;
 	}
 }
