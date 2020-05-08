 package com.glasstunes;
 
 import android.database.Cursor;
 import android.net.Uri;
 
 import com.glasstunes.cards.ActionCard;
 import com.glasstunes.screenslide.CardFragment;
 
 public class DetailActivity extends BaseContentCardActivity {
 
 	@Override
 	protected Uri getContentUri() {
		return Uri.withAppendedPath(getIntent().getData(), "/sections");
 	}
 
 	@Override
 	protected CardFragment getCardFromCursor(Cursor cursor) {
 		return ActionCard.newInstance(
 				cursor.getString(cursor.getColumnIndex("display_name")),
 				cursor.getString(cursor.getColumnIndex("intent_uri")));
 	}
 
 }
