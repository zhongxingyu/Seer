 package ru.terrach.activity.component;
 
 import java.util.List;
 
 import lazylist.ImageLoader;
 import ru.terrach.R;
 import ru.terrach.network.dto.PostDTO;
 import ru.terrach.network.dto.ThreadDTO;
 import android.content.Context;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ThreadsArrayAdapter extends ArrayAdapter<ThreadDTO> {
 
 	private ImageLoader imageLoader;
 	private String board;
 	private String server;
 
 	public ThreadsArrayAdapter(Context context, List<ThreadDTO> threads, String board) {
 		super(context, R.layout.i_thread_item, threads);
 		this.board = board;
 		imageLoader = new ImageLoader(context);
 		server = context.getString(R.string.server);
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = null;
 		if (convertView == null)
 			v = LayoutInflater.from(getContext()).inflate(R.layout.i_thread_item, null);
 		else
 			v = convertView;
 
 		PostViewHolder vh = null;
 		if (v.getTag() == null) {
 			vh = new PostViewHolder();
 			vh.date = ((TextView) v.findViewById(R.id.tvPostDate));
 			vh.num = ((TextView) v.findViewById(R.id.tvPostNum));
 			vh.msg = ((TextView) v.findViewById(R.id.tvPostMessage));
 			vh.pic = ((ImageView) v.findViewById(R.id.ivPostImage));
 			v.setTag(vh);
 		} else
 			vh = (PostViewHolder) v.getTag();
 		PostDTO firstPost = getItem(position).posts.get(0).get(0);
 		vh.date.setText(firstPost.date);
 		vh.num.setText(firstPost.num.toString());
 		vh.msg.setText(Html.fromHtml(firstPost.comment));
 		if (firstPost.image != null)
			imageLoader.DisplayImage(server + board + firstPost.thumbnail, vh.pic);
 		return v;
 	}
 
 	public void update(List<ThreadDTO> threads) {
 		notifyDataSetChanged();
 	}
 }
