 package fr.utc.assos.uvweb.adapters;
 
 import android.content.Context;
 import android.text.Html;
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
  * Created with IntelliJ IDEA.
  * User: Thomas
  * Date: 15/03/13
  * Time: 16:03
  * To change this template use File | Settings | File Templates.
  */
 public class NewsFeedEntryAdapter extends UVAdapter {
 	private List<UVwebContent.NewsFeedEntry> mNewsFeedEntries = Collections.emptyList();
 	private final String mDatePresentation;
 
 	public NewsFeedEntryAdapter(Context context) {
 		super(context);
 		mDatePresentation = context.getString(R.string.date_presentation);
 	}
 
 	@Override
 	public int getCount() {
 		return mNewsFeedEntries.size();
 	}
 
 	@Override
 	public UVwebContent.NewsFeedEntry getItem(int position) {
 		return mNewsFeedEntries.get(position);
 	}
 
 	public void updateNewsFeedEntries(List<UVwebContent.NewsFeedEntry> newsFeedEntries) {
 		ThreadPreconditions.checkOnMainThread();
 
 		mNewsFeedEntries = newsFeedEntries;
 		notifyDataSetChanged();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			convertView = mLayoutInflater.inflate(R.layout.newsfeed_entry, null);
 		}
 
 		final TextView userIdView = UVwebHolder.get(convertView, R.id.user_id_action);
 		final TextView dateView = UVwebHolder.get(convertView, R.id.date);
 		final TextView commentView = UVwebHolder.get(convertView, R.id.comment);
 
 		final UVwebContent.NewsFeedEntry entry = getItem(position);
 
 		userIdView.setText(Html.fromHtml(String.format(UVwebContent.NEWSFEED_ACTION_FORMAT,
 				entry.getAuthor(), " " + entry.getAction())));
 		dateView.setText(mDatePresentation + " " + entry.getFormattedDate());
 		commentView.setText(entry.getComment());
 
 
 		return convertView;
 	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}
 }
