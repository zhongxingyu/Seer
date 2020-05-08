 package ch.shibastudio.kanjinotepad.lists;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import ch.shibastudio.kanjinotepad.KanjiDBHelper;
 import ch.shibastudio.kanjinotepad.R;
 import ch.shibastudio.kanjinotepad.R.drawable;
 import ch.shibastudio.kanjinotepad.R.id;
 import ch.shibastudio.kanjinotepad.R.layout;
 import ch.shibastudio.kanjinotepad.utils.TextTools;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class KanjiListFancyAdapter extends BaseAdapter{
 
 	static class ViewHolder{
 		TextView tvCharacter;
 		TextView tvInfo1;
 		TextView tvInfo2;
 		ImageView ivFavorite;
 	}
 	
 	// -- Members ------------------------------------------------------------------
 	private ArrayList<KanjiListInfos> mKanjis = new ArrayList<KanjiListInfos>();
 	private LayoutInflater mInflater;
 	private Context mCtx;
 	private KanjiDBHelper mDbHelper;
 	
 	// -- Action Listeners ---------------------------------------------------------
 	private OnClickListener onFavoriteClicked = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			if(null != v){
 				int position = Integer.parseInt(v.getTag().toString());
 				KanjiListInfos kli = mKanjis.get(position);
 				kli.favorite = !kli.favorite;
 				
 				if(kli.favorite){
 					((ImageView)v).setImageResource(R.drawable.ic_favfull);
 				}else{
 					((ImageView)v).setImageResource(R.drawable.ic_favempty);
 				}
 								
 				try {
 					mDbHelper.openDatabase();
 					mDbHelper.toggleFavorite(TextTools.kanjiToCode(kli.character));
 					mDbHelper.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		} 
     };
 	
 	public KanjiListFancyAdapter(Context c) {
 		mDbHelper = new KanjiDBHelper(mCtx);
 		mInflater = LayoutInflater.from(c);
 	}
 	
 	public void addItem(KanjiListInfos infos){
 		mKanjis.add(infos);
 		notifyDataSetChanged();
 	}
 	
 	@Override
 	public int getCount() {
 		return mKanjis.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mKanjis.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ViewHolder holder = null;
 		if(convertView == null){
 			convertView = mInflater.inflate(R.layout.list_row, null);
 			holder = new ViewHolder();
 			holder.tvCharacter = (TextView)convertView.findViewById(R.id.kanjichar);
 			holder.tvInfo1 = (TextView)convertView.findViewById(R.id.info1);
 			holder.tvInfo2 = (TextView)convertView.findViewById(R.id.info2);
 			holder.ivFavorite = (ImageView)convertView.findViewById(R.id.star);
			holder.ivFavorite.setTag(position);
 			convertView.setTag(holder);
 		}else{
 			holder = (ViewHolder)convertView.getTag();
 		}
		
 		holder.tvCharacter.setText(mKanjis.get(position).character);
 		holder.tvInfo1.setText(mKanjis.get(position).info1);
 		holder.tvInfo2.setText(mKanjis.get(position).info2);
 		if(mKanjis.get(position).favorite){
 			holder.ivFavorite.setImageResource(R.drawable.ic_favfull);
 		}else{
 			holder.ivFavorite.setImageResource(R.drawable.ic_favempty);
 		}
 		
 		holder.ivFavorite.setOnClickListener(onFavoriteClicked);
 		
 		return convertView;
 	}
 }
