 package carnero.me.fragment;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.style.TextAppearanceSpan;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import carnero.me.Constants;
 import carnero.me.R;
 import carnero.me.data._TimelineList;
 import carnero.me.model.Education;
 import carnero.me.model.Entry;
 import carnero.me.model.Position;
 import carnero.me.model.Work;
 import carnero.me.view.AnimateFrameLayout;
 import carnero.me.view.TransparentListView;
 import com.google.analytics.tracking.android.GAServiceManager;
 import com.google.analytics.tracking.android.GoogleAnalytics;
 import com.google.analytics.tracking.android.Tracker;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 public class TimelineFragment extends Fragment {
 
 	private Context mContext;
 	private Resources mResources;
 	private LayoutInflater mInflater;
 	private TransparentListView mList;
 	private int mListTop = 0; // index of first visible item
 	private int mListWidth = 0;
 	private TimelineAdapter mAdapter;
 	private NumberFormat mDecimalFormat = DecimalFormat.getInstance(Locale.getDefault());
 	private Tracker mTracker;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
 		if (savedState != null) {
 			mListTop = savedState.getInt(Constants.STATE_LIST_TOP, 0);
 		}
 
 		mInflater = inflater;
 
 		View view = inflater.inflate(R.layout.timeline, container, false);
 
 		mList = (TransparentListView) view.findViewById(R.id.entries);
 		mList.addFooterView(inflater.inflate(R.layout.item_footer, null, false));
 		mList.addHeaderView(inflater.inflate(R.layout.item_footer, null, false));
 
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		final GoogleAnalytics analytics = GoogleAnalytics.getInstance(getActivity());
 		mTracker = analytics.getTracker(getString(R.string.ga_trackingId));
 
 		mContext = getActivity().getBaseContext();
 		mResources = mContext.getResources();
 		mAdapter = new TimelineAdapter(mContext, 0, _TimelineList.ENTRIES);
 
 		mList.setAdapter(mAdapter);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		mListWidth = 0;
 		if (mListTop > 0) {
 			mList.setSelection(mListTop);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		state.putInt(Constants.STATE_LIST_TOP, mList.getFirstVisiblePosition());
 
 		super.onSaveInstanceState(state);
 	}
 
 	private InitResult initLayout(Entry.TYPE type, boolean isActive, View convertView) {
 		Tag tag = null;
 		AnimateFrameLayout layout;
 
 		if (convertView != null) {
 			tag = (Tag) convertView.getTag();
 		}
 
 		if (tag != null) {
 			layout = (AnimateFrameLayout) convertView;
 
 			if (tag.type != type) {
 				setVisibility(tag, type);
 			}
 		} else {
 			layout = (AnimateFrameLayout) mInflater.inflate(R.layout.item_timeline, mList, false);
 
 			tag = new Tag();
 			tag.type = type;
 			tag.detailWork = layout.findViewById(R.id.detail_work);
 			tag.detailOther = layout.findViewById(R.id.detail_other);
 			tag.title = (TextView) layout.findViewById(R.id.title);
 			tag.text = (TextView) layout.findViewById(R.id.text);
 			tag.downloads = (TextView) layout.findViewById(R.id.downloads);
 			tag.experience = (TextView) layout.findViewById(R.id.experience);
 			tag.description = (TextView) layout.findViewById(R.id.description);
 			tag.client = (TextView) layout.findViewById(R.id.client);
 			tag.background = layout.findViewById(R.id.background);
 
 			layout.setTag(tag);
 
 			setVisibility(tag, type);
 		}
 
 		layout.setAnimationEnabled(isActive);
 
 		return new InitResult(tag, layout);
 	}
 
 	private View fillLayout(Work entry, View convertView) {
 		final InitResult init = initLayout(Entry.TYPE.TYPE_WORK, (entry.tapAction != null), convertView);
 		Tag tag = init.tag;
 		View layout = init.layout;
 
 		final String dSt = mDecimalFormat.format(entry.downloads);
 		final SpannableString dSp = new SpannableString(mResources.getString(R.string.cv_downloads, dSt));
 		dSp.setSpan(new TextAppearanceSpan(mContext, R.style.Timeline_Card_Description), 0, dSt.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 		final String eSt;
 		if (entry.months >= 12) {
 			int years = Math.round(entry.months / 12);
 			eSt = mResources.getQuantityString(R.plurals.cv_experience_years, years, years);
 		} else {
 			eSt = mResources.getQuantityString(R.plurals.cv_experience_months, entry.months, entry.months);
 		}
 		final SpannableString eSp = new SpannableString(eSt + " " + mResources.getString(R.string.cv_experience));
 		eSp.setSpan(new TextAppearanceSpan(mContext, R.style.Timeline_Card_Description), 0, eSt.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 		// title
 		if (mListWidth <= 0) {
 			mListWidth = mList.getWidth();
 		}
 
 		Paint titlePaint = tag.title.getPaint();
 		Rect titleBounds = new Rect();
 		titlePaint.getTextBounds(entry.name, 0, entry.name.length(), titleBounds);
 
 		if (titleBounds.width() > (mListWidth * 0.77) && entry.nameShort != null) {
 			tag.title.setText(entry.nameShort);
 		} else {
 			tag.title.setText(entry.name);
 		}
 		// texts
 		if (entry.downloads > 0) {
 			tag.downloads.setText(dSp);
 			tag.downloads.setVisibility(View.VISIBLE);
 		} else {
 			tag.downloads.setVisibility(View.GONE);
 		}
 		tag.experience.setText(eSp);
 		tag.description.setText(entry.description);
 		tag.client.setText(entry.client);
 		// background
 		if (entry.background != 0) {
 			tag.background.setBackgroundResource(entry.background);
 		}
 		// tapAction
 		if (entry.tapAction != null) {
 			layout.setOnClickListener(new EntryAction(entry.tapAction.getIntent(getActivity())));
 		} else {
 			layout.setOnClickListener(null);
 		}
 
 		return layout;
 	}
 
 	private View fillLayout(Position entry, View convertView) {
 		final InitResult init = initLayout(Entry.TYPE.TYPE_POSITION, (entry.tapAction != null), convertView);
 		Tag tag = init.tag;
 		View layout = init.layout;
 
 
 		final StringBuilder sb = new StringBuilder();
 		sb.append(entry.name);
 		sb.append(" ");
 		sb.append(entry.position);
 
 		final SpannableString tSp = new SpannableString(sb.toString());
 		tSp.setSpan(new TextAppearanceSpan(mContext, R.style.Timeline_Plain_Description), entry.name.length() + 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 		// texts
 		tag.text.setText(tSp);
 		// tapAction
 		if (entry.tapAction != null) {
 			layout.setOnClickListener(new EntryAction(entry.tapAction.getIntent(getActivity())));
 		}
 		// tag
 		layout.setTag(tag);
 
 		return layout;
 	}
 
 	private View fillLayout(Education entry, View convertView) {
 		final InitResult init = initLayout(Entry.TYPE.TYPE_EDUCATION, (entry.tapAction != null), convertView);
 		Tag tag = init.tag;
 		View layout = init.layout;
 
 
 		final StringBuilder sb = new StringBuilder();
 		sb.append(entry.name);
 		sb.append(" ");
 		sb.append(entry.description);
 
 		final SpannableString tSp = new SpannableString(sb.toString());
 		tSp.setSpan(new TextAppearanceSpan(mContext, R.style.Timeline_Plain_Description), entry.name.length() + 1, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
 
 		// texts
 		tag.text.setText(tSp);
 		// tapAction
 		if (entry.tapAction != null) {
 			layout.setOnClickListener(new EntryAction(entry.tapAction.getIntent(getActivity())));
 		}
 		// tag
 		layout.setTag(tag);
 
 		return layout;
 	}
 
 	private void setVisibility(Tag tag, Entry.TYPE type) {
 		switch (type) {
 			case TYPE_WORK:
 				tag.detailOther.setVisibility(View.GONE);
 				tag.detailWork.setVisibility(View.VISIBLE);
 				break;
 			case TYPE_POSITION:
 			case TYPE_EDUCATION:
 				tag.detailWork.setVisibility(View.GONE);
 				tag.detailOther.setVisibility(View.VISIBLE);
 				break;
 		}
 
 		tag.type = type; // set type for which it's configured
 	}
 
 	// classes
 	private class TimelineAdapter extends ArrayAdapter<Entry> {
 
 		private ArrayList<Entry> mItems = new ArrayList<Entry>();
 
 		public TimelineAdapter(Context context, int textViewResId, List<Entry> items) {
 			super(context, textViewResId, items);
 
 			mItems.addAll(items);
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View view = null;
 			Entry entry = mItems.get(position);
 
 			if (entry instanceof Work) {
 				view = fillLayout((Work) entry, convertView);
 			} else if (entry instanceof Position) {
 				view = fillLayout((Position) entry, convertView);
 			} else if (entry instanceof Education) {
 				view = fillLayout((Education) entry, convertView);
 			}
 
 			mList.registerView(view);
 
 			return view;
 		}
 	}
 
 	private class InitResult {
 
 		public Tag tag;
 		public View layout;
 
 		public InitResult(Tag tag, View layout) {
 			this.tag = tag;
 			this.layout = layout;
 		}
 	}
 
 	private class Tag {
 
 		public Entry.TYPE type;
 		//
 		public View detailWork;
 		public View detailOther;
 		public TextView text; // position; education
 		public TextView title; // work
 		public TextView downloads; // work
 		public TextView experience; // work
 		public TextView description; // work
 		public TextView client; // work
 		public View background; // work
 	}
 
 	private class EntryAction implements View.OnClickListener {
 
 		private Intent mIntent;
 
 		public EntryAction(Intent intent) {
 			mIntent = intent;
 		}
 
 		@Override
 		public void onClick(View v) {
 			getActivity().startActivity(mIntent);
 
 			if (mTracker != null) {
 				mTracker.sendEvent("timeline", "tap", mIntent.getData().toString(), 0l);
 
 				GAServiceManager.getInstance().dispatch();
 			}
 		}
 	}
 }
