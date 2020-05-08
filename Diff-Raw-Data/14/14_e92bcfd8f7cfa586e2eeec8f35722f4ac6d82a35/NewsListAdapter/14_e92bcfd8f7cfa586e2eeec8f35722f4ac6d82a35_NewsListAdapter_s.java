 /**
  * 
  */
 package com.starbug1.android.mudanews;
 
 import java.util.List;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.starbug1.android.mudanews.data.NewsListItem;
 
 /**
  * @author smeghead
  * 
  */
 public class NewsListAdapter extends ArrayAdapter<NewsListItem> {
 	private LayoutInflater inflater_;
 	private TextView title_;
 	private MudanewsActivity context_;
 
 	public NewsListAdapter(Context context, List<NewsListItem> objects) {
 		super(context, 0, objects);
 		context_ = (MudanewsActivity)context;
 		inflater_ = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 
 		if (view == null) {
 			view = inflater_.inflate(R.layout.item_row, null);
 		}
 
 		Log.d("NewsListAdapter", "position: " + position);
 		if (this.getCount() < position + 1) {
 			Log.w("NewsListAdapter", "position invalid!");
 			return null;
 		}
 		final NewsListItem item = this.getItem(position);
 		if (item != null) {
 			view.setTag(item);
 			
 			String title = item.getTitle().toString();
 			title_ = (TextView) view.findViewById(R.id.item_title);
 			title_.setText(title);
 			ImageView newEntry = (ImageView) view.findViewById(R.id.newEntry);
 			newEntry.setVisibility(item.getViewCount() > 0 ? ImageView.GONE : ImageView.VISIBLE);
 			ImageView isFavorite = (ImageView) view.findViewById(R.id.favorite);
 			isFavorite.setImageResource(item.isFavorite()
 					? android.R.drawable.btn_star_big_on
 					: android.R.drawable.btn_star_big_off);
 			isFavorite.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					boolean add = !item.isFavorite();
 					//TODO locate right place.
 					//お気に入り
 					final SQLiteDatabase db = context_.getDbHelper().getWritableDatabase();
 					try {
 						
 						db.execSQL(
 								add
 									? "insert into favorites (feed_id, created_at) values (?, current_timestamp)"
 									: "delete from favorites where feed_id = ?",
 								new String[] { String.valueOf(item.getId()) });
 						item.setFavorite(add);
 						ImageView favorite = (ImageView) v
 								.findViewById(R.id.favorite);
 						favorite.setImageResource(add
 								? android.R.drawable.btn_star_big_on
 								: android.R.drawable.btn_star_big_off);
 						if (add) {
 							Toast.makeText(context_, item.getTitle() + "をお気に入りにしました", Toast.LENGTH_LONG).show();
 						}
 					} catch (Exception e) {
 						Log.e("MudanewsActivity", "failed to upate entry action.");
 					} finally {
 						db.close();
 					}
 				}
 			});
 			GridView grid = (GridView) context_.findViewById(R.id.grid);
 			int size = grid.getWidth() / context_.column_count_;
 			Log.d("NewsListAdapter", "size:" + size);
 			if (item.getImage() != null) {
 				Bitmap bOrg = item.getImageBitmap();
 				if (bOrg == null) {
 					byte[] data = item.getImage();
 					Log.d("NewsListAdapter", "data.length:" + data.length);
					bOrg = BitmapFactory.decodeByteArray(data, 0, data.length);
//					item.setImageBitmap(b);
 				}
 				// サイズ調整
 				Bitmap b = bOrg;
 				if (size != 160) {
 					b = Bitmap.createScaledBitmap(bOrg, size, size, false);
 				}
 				ImageView image = (ImageView) view
 						.findViewById(R.id.item_image);
 				image.setImageDrawable(null);
 				image.setImageBitmap(b);
 				image.setVisibility(ImageView.VISIBLE);
 			} else {
 				Log.d("NewsListAdapter", "item more? id:" + item.getId());
 				ImageView image = (ImageView) view
 						.findViewById(R.id.item_image);
 				image.setVisibility(ImageView.GONE);
 			}
 		}
 		return view;
 	}
 
 	@Override
 	public void remove(NewsListItem object) {
 		Log.d("NewsListAdapter", "remove");
 		super.remove(object);
 	}
 
 }
