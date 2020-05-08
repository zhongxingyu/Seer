 package com.hstefan.aunctiondroid;
 
 import com.hstefan.aunctiondroid.db.DbHelper;
 import com.hstefan.aunctiondroid.db.entities.User;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 public class ViewAunctionsActivity extends Activity {
 	
 	SQLiteDatabase myDb;
 	
 	@Override
 	protected void onCreate(Bundle bundle) {
 		super.onCreate(bundle);
 		setContentView(R.layout.bidlist);
 		myDb = AunctionDroidActivity.getHelper().getWritableDatabase();
 		setListners();
		setListAdapter();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		setListAdapter();
 	}
 
 	private void setListAdapter() {
 		StringBuilder str_builder = new StringBuilder("SELECT ");
 		
 		str_builder.append(DbHelper.ITEM_TABLE);
 		str_builder.append(".id as _id,");
 		str_builder.append(DbHelper.ITEM_TABLE + ".name,");
 		str_builder.append(DbHelper.AUNCTIONS_TABLE + ".price");
 		
 		str_builder.append(" FROM ");
 		str_builder.append(DbHelper.ITEM_TABLE + ",");
 		str_builder.append(DbHelper.USER_TABLE + ",");
 		str_builder.append(DbHelper.USER_ITEM_TABLE + ",");
 		str_builder.append(DbHelper.AUNCTIONS_TABLE);
 		
 		str_builder.append(" WHERE ");
 		
 		str_builder.append("(" + DbHelper.ITEM_TABLE + ".id = ");
 		str_builder.append(DbHelper.USER_ITEM_TABLE + ".id_item" + ") AND ");
 		str_builder.append("(" + DbHelper.USER_ITEM_TABLE + ".id_user = ");
 		str_builder.append(DbHelper.USER_TABLE + ".id) AND (");
 		str_builder.append(DbHelper.AUNCTIONS_TABLE + ".id_user = ");
 		str_builder.append(DbHelper.USER_TABLE + ".id) AND (" );
 		str_builder.append(DbHelper.AUNCTIONS_TABLE + ".id_item = ");
 		str_builder.append(DbHelper.ITEM_TABLE + ".id)" );
 		
 		//Now Playing: Rush - Marathon - Power Windows - 1985 - Classic Prog - MP3 - 320 kbps - 6:10 min
 		
 		Cursor cursor = myDb.rawQuery(str_builder.toString(), null);
 		System.out.println(cursor.toString());
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(), R.layout.bidlist_row, cursor, 
 				new String[] {"name", "price"}, new int[] {R.id.bid_item_name_id, R.id.bid_item_price_text_id});
 		ListView list = (ListView)findViewById(R.id.bid_list_view);
 		if(list != null)
 			list.setAdapter(adapter);
 		else
 			Log.d("Adapter", "failed to atach");
 		//Now Playing: Metallica - Fade To Black - Ride The Lightning - 1984 - Rock - MP3 - 320 kbps - 6:57 min
 		Log.d("Rows resulted in query", Long.toString(cursor.getCount()));
 	}
 	
 	private void setListners() {
 		ListView list = (ListView)findViewById(R.id.bid_list_view);
 		list.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> adapter, View v, int pos,
 					long id) {
 				TextView price_v = (TextView)v.findViewById(R.id.bid_item_price_text_id);
 				int price = Integer.parseInt(price_v.getEditableText().toString());
 				User u = User.getActive();
 				if(u.getMoney() >= price) {
 					u.setMoney(u.getMoney() - price);
 					ContentValues cv  = new ContentValues();
 					cv.put("money", u.getMoney());
 					//atualiza o saldo do comprador
 					myDb.update(DbHelper.AUNCTIONS_TABLE, cv, "id=?", new String[]{ Integer.toString(u.getId())});
 					
 					Cursor c = myDb.query(DbHelper.AUNCTIONS_TABLE, new String[] {"id_user,id_item"},
 							"id_item=?", new String[]{ Long.toString(id) }, null, null, null);
 					c.moveToFirst();
 					int user_id = c.getInt(0); //id do vendedor
 					int item_id = c.getInt(1); //id_item
 					
 					Cursor cs = myDb.query(DbHelper.USER_TABLE, new String[] {"money"},
 							"id=?", new String[]{ Long.toString(user_id)}, null, null, null);
 					cs.moveToFirst();
 					int money = cs.getInt(0); //saldo do vendedor
 					
 					ContentValues seller = new ContentValues();
 					seller.put("money", money + price); //aumenta o saldo do comprador
 					myDb.update(DbHelper.USER_TABLE, seller, "id=?", new String[]{ Long.toString(user_id)});
 					
 					//deleta o item da tabela de vendas
 					myDb.delete(DbHelper.AUNCTIONS_TABLE, "id=?", new String[] { Long.toString(id) });
 					
 					//muda o dono do item
 					ContentValues owner = new ContentValues();
 					owner.put("id_user", u.getId());
 					myDb.update(DbHelper.USER_ITEM_TABLE, owner, "id_item=? AND id_user=?", 
 							new String[] {Integer.toString(item_id), Integer.toString(user_id)});
 				}
 			}
 		});
 	}
 }
