 package com.sample.redditapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 import com.android.volley.RequestQueue;
 import com.android.volley.toolbox.ImageLoader;
 import com.android.volley.toolbox.NetworkImageView;
 import com.android.volley.toolbox.ImageLoader.ImageCache;
 import com.android.volley.toolbox.Volley;
 
 public class RedditItemAdapter extends BaseAdapter {
 
 	private List<RedditItem> items;
 	private Context context;
 	private ImageLoader loader;
 
 	public RedditItemAdapter(Context context) {
 		this.context = context;
 		items = new ArrayList<RedditItem>();
 		RequestQueue queue = Volley.newRequestQueue(context);
 		loader = new ImageLoader(queue, new ImageCache() {
 
 			@Override
 			public void putBitmap(String url, Bitmap bitmap) {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public Bitmap getBitmap(String url) {
 				// TODO Auto-generated method stub
 				return null;
 			}
 		});
 	}
 
 	@Override
 	public int getCount() {
 		return items.size();
 	}
 
 	@Override
 	public RedditItem getItem(int position) {
 		return items.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup container) {
 		// Agarra el recurso xml y lo transforma a un componente android
 		//
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		// el null es porque le decis a android que no sabes a que va estar
 		// adjuntado.
 		// El asume que el componente es un componente que esta adjuntado hace
 		// mach_parent
 		// Sin el if indica que cada vez que de haga un scroll se crea un nuevo
 		// xml
 		// Con el if reutilizara la el xml del layout que va desapareciendo al
 		// hacer el scroll
 		if (convertView == null) {
 			convertView = inflater.inflate(R.layout.reddititem, null);
 		}
 
 		TextView textAuthor = (TextView) convertView
 				.findViewById(R.id.textAuthor);
 		TextView textDescription = (TextView) convertView
 				.findViewById(R.id.textDescription);
 		NetworkImageView imagePost = (NetworkImageView) convertView
 				.findViewById(R.id.imagePost);
 
 		RedditItem item = getItem(position);
 		textAuthor.setText(item.author);
 		textDescription.setText(item.title);
		if(item.thumbnail!=null && !item.thumbnail.equals("default"))
 		{
 		  imagePost.setDefaultImageResId(R.drawable.ic_launcher);
 		  imagePost.setImageUrl(item.thumbnail, loader);
 		}
 		return convertView;
 	}
 
 	public void setItems(List<RedditItem> values) {
 		// asignar valores al adapter
 		if (items != null) {
 			this.items = values;
 			// indicar al adapter que han cambiado los datos
 			notifyDataSetChanged();
 		}
 	}
 
 }
