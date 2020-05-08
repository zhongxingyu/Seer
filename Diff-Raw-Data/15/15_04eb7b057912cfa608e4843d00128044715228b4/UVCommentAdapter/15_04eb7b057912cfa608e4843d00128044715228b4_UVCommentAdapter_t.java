 package fr.utc.assos.uvweb.adapters;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import fr.utc.assos.uvweb.R;
 import fr.utc.assos.uvweb.ThreadPreconditions;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.holders.UVwebHolder;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
  * An adapter used all together with the {@link fr.utc.assos.uvweb.UVDetailFragment}'s ListView.
  * It relies on a standard ViewHolder pattern implemented in the {@link fr.utc.assos.uvweb.holders.UVwebHolder} class and thus allows UVs recycling.
  * It is used to show the users' comments of a given UV
  */
 public class UVCommentAdapter extends UVAdapter {
 	private List<UVwebContent.UVComment> mComments = Collections.emptyList();
 
 	public UVCommentAdapter(Context context) {
 		super(context);
 	}
 
 	@Override
 	public int getCount() {
 		return mComments.size();
 	}
 
 	@Override
 	public UVwebContent.UVComment getItem(int position) {
 		return mComments.get(position);
 	}
 
 	public void updateComments(List<UVwebContent.UVComment> comments) {
 		ThreadPreconditions.checkOnMainThread();
 
 		mComments = comments;
 		notifyDataSetChanged();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = mLayoutInflater.inflate(R.layout.uv_comment, null);
 		}
 
 		final TextView userIdView = UVwebHolder.get(convertView, R.id.userid);
 		final TextView rateView = UVwebHolder.get(convertView, R.id.rate);
 		final TextView commentView = UVwebHolder.get(convertView, R.id.comment);
 		final TextView dateView = (TextView) convertView.findViewById(R.id.date);
 
 		final UVwebContent.UVComment comment = getItem(position);
 
 		userIdView.setText(comment.getAuthor());
 		rateView.setText(comment.getFormattedRate());
 		commentView.setText(comment.getComment());
 		dateView.setText(comment.getFormattedDate());
 
 		return convertView;
 	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}
 }
