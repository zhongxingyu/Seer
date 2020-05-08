 package co.shoutbreak.storage.inbox;
 
 import java.text.ParseException;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.graphics.Typeface;
 import android.graphics.PorterDuff.Mode;
 import android.os.AsyncTask;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AbsListView;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.RelativeLayout.LayoutParams;
 import co.shoutbreak.R;
 import co.shoutbreak.core.C;
 import co.shoutbreak.core.Colleague;
 import co.shoutbreak.core.Mediator;
 import co.shoutbreak.core.Shout;
 import co.shoutbreak.core.utils.ErrorManager;
 import co.shoutbreak.core.utils.ISO8601DateParser;
 import co.shoutbreak.core.utils.SBLog;
 import com.ocpsoft.pretty.time.PrettyTime;
 
 public class InboxListViewAdapter extends BaseAdapter implements Colleague {
 
 	private static final String TAG = "InboxListViewAdapter";
 
 	private Mediator _m;
 	private LayoutInflater _inflater;
 	private InboxSystem _inboxSystem;
 	private PrettyTime _prettyTime;
 	public OnClickListener onCollapseClickListener;
 	public OnClickListener onVoteUpClickListener;
 	public OnClickListener onVoteDownClickListener;
 	public OnClickListener onDeleteClickListener;
 	public OnClickListener onScoreClickListener;
 	public OnClickListener onReplyClickListener;
 	private HashMap<String, Boolean> _cacheExpandState;
 	// private HashMap<String, String> _cachePrettyTimeAgo;
 	private HashMap<String, Integer> _cacheVoteTemporary;
 	private boolean _isInputAllowed;
 
 	public InboxListViewAdapter(Mediator mediator, LayoutInflater inflater, InboxSystem inboxSystem) {
 		SBLog.constructor(TAG);
 
 		_m = mediator;
 		_inflater = inflater;
 		_inboxSystem = inboxSystem;
 		_prettyTime = new PrettyTime();
 		_cacheExpandState = new HashMap<String, Boolean>();
 		_cacheVoteTemporary = new HashMap<String, Integer>();
 		_isInputAllowed = false;
 
 		onCollapseClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				holder.collapsed.setVisibility(View.VISIBLE);
 				holder.expanded.setVisibility(View.GONE);
 				_cacheExpandState.put(holder.shout.id, false);
 			}
 		};
 
 		onVoteUpClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				holder.btnVoteUp.setEnabled(false);
 				holder.btnVoteDown.setEnabled(false);
 				holder.btnVoteUp.setImageResource(R.drawable.vote_up_button);
 				view.invalidate();
 
 				VoteTask task = new VoteTask();
 				task.execute(view.getTag(), C.SHOUT_VOTE_UP);
 			}
 		};
 
 		onVoteDownClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				holder.btnVoteUp.setEnabled(false);
 				holder.btnVoteDown.setEnabled(false);
 				holder.btnVoteDown.setImageResource(R.drawable.vote_down_button);
 				view.invalidate();
 
 				VoteTask task = new VoteTask();
 				task.execute(view.getTag(), C.SHOUT_VOTE_DOWN);
 			}
 		};
 
 		onDeleteClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				_m.deleteShout(holder.shout.id);
 			}
 		};
 
 		onScoreClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				Shout entry = holder.shout;
 				_m.getUiGateway().handleScoreDetailsRequest(entry.ups, entry.downs, entry.score);
 			}
 		};
 
 		onReplyClickListener = new OnClickListener() {
 			public void onClick(View view) {
 				InboxViewHolder holder = (InboxViewHolder) view.getTag();
 				_m.createReplyDialog(holder.shout);
 			}
 		};
 
 	}
 	
 	@Override
 	public void unsetMediator() {
 		_m = null;
 	}
 
 	public HashMap<String, Boolean> getCacheExpandState() {
 		return _cacheExpandState;
 	}
 
 	private class VoteTask extends AsyncTask<Object, Void, Void> {
 		// TODO: why is this an async task? all it does is fire off
 		// a message that gets handled by a separate thread
 		@Override
 		protected Void doInBackground(Object... params) {
 			InboxViewHolder holder = (InboxViewHolder) params[0];
 			Integer voteDirection = (Integer) params[1];
 			_cacheVoteTemporary.put(holder.shout.id, voteDirection);
 			_m.handeVoteStart(holder.shout.id, voteDirection);
 			return null;
 		}
 	}
 
 	public void undoVote(String shoutId, int vote) {
 		_cacheVoteTemporary.remove(shoutId);
 		notifyDataSetChanged();
 	}
 
 	public void setInputAllowed(boolean b) {
 		_isInputAllowed = b;
 		notifyDataSetChanged();
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		// Setup InboxViewHolder //////////////////////////////////////////////////
 		InboxViewHolder holder;
 		if (convertView == null) {
 			// TODO: can we reduce the number of items?
 			// Setup view.
 			convertView = _inflater.inflate(R.layout.inbox_item, parent, false);
 			holder = new InboxViewHolder();
 			holder.textC = (TextView) convertView.findViewById(R.id.tvTextC);
 			holder.timeAgoC = (TextView) convertView.findViewById(R.id.tvTimeAgoC);
 			holder.scoreC = (TextView) convertView.findViewById(R.id.tvScoreC);
 			holder.scoreC.setOnClickListener(onScoreClickListener);
 			holder.textE = (TextView) convertView.findViewById(R.id.tvTextE);
 			holder.timeAgoE = (TextView) convertView.findViewById(R.id.tvTimeAgoE);
 			holder.scoreE = (TextView) convertView.findViewById(R.id.tvScoreE);
 			holder.scoreE.setOnClickListener(onScoreClickListener);
 			holder.collapsed = (RelativeLayout) convertView.findViewById(R.id.rlCollapsed);
 			holder.expanded = (RelativeLayout) convertView.findViewById(R.id.rlExpanded);
 			holder.btnVoteUp = (ImageButton) convertView.findViewById(R.id.btnVoteUp);
 			holder.hitCount = (TextView) convertView.findViewById(R.id.hitCountTv);
 			holder.hitCountLl = (LinearLayout) convertView.findViewById(R.id.hitCountLl);
 			holder.btnVoteUp.setOnClickListener(onVoteUpClickListener);
 			holder.btnVoteDown = (ImageButton) convertView.findViewById(R.id.btnVoteDown);
 			holder.btnVoteDown.setOnClickListener(onVoteDownClickListener);
 			holder.btnDelete = (ImageButton) convertView.findViewById(R.id.btnDelete);
 			holder.btnDelete.setOnClickListener(onDeleteClickListener);
 			holder.btnReply = (ImageButton) convertView.findViewById(R.id.btnReply);
 			holder.btnReply.setOnClickListener(onReplyClickListener);
 			holder.btnVoteUp.getBackground().setColorFilter(0xAA9900FF, Mode.SRC_ATOP);
 			holder.btnVoteDown.getBackground().setColorFilter(0xAA9900FF, Mode.SRC_ATOP);
 			holder.btnDelete.getBackground().setColorFilter(0xAA9900FF, Mode.SRC_ATOP);
 			holder.btnReply.getBackground().setColorFilter(0xAA9900FF, Mode.SRC_ATOP);
 			holder.expanded.setOnClickListener(onCollapseClickListener);
 			holder.expanded.setTag(holder);
 			convertView.setTag(holder);
 		} else {
 			holder = (InboxViewHolder) convertView.getTag();
 		}
 
 		Shout entry = (Shout) getItem(position);
 		holder.shout = entry;
 	
 		// Is expanded? ///////////////////////////////////////////////////////////
 		boolean isExpanded = false;
 		if (_cacheExpandState.containsKey(entry.id)) {
 			isExpanded = _cacheExpandState.get(entry.id);
 		}
 		if (isExpanded) {
 			holder.collapsed.setVisibility(View.GONE);
 			holder.expanded.setVisibility(View.VISIBLE);
 		} else {
 			holder.collapsed.setVisibility(View.VISIBLE);
 			holder.expanded.setVisibility(View.GONE);
 		}
 		
 		// Is reply? //////////////////////////////////////////////////////////////
 		if (entry.isReply) {
 			holder.hitCountLl.setVisibility(View.GONE);
 			holder.btnReply.setVisibility(View.GONE);
 			convertView.setPadding(25, 0, 0, 0);
 		} else {
 			String hitCount = "?";
 			if (entry.hit != C.NULL_HIT) {
 				hitCount = Integer.toString(entry.hit);
 			}
 			holder.hitCountLl.setVisibility(View.VISIBLE);
 			holder.hitCount.setText(hitCount);
 			// Reply button
 			if (entry.open) {
 				holder.btnReply.setVisibility(View.VISIBLE);
 				holder.btnReply.setEnabled(_isInputAllowed);
 				holder.btnReply.setTag(holder);
 			} else {
 				holder.btnReply.setVisibility(View.GONE);
 				holder.btnReply.setEnabled(_isInputAllowed);
 			}
 			convertView.setPadding(0, 0, 0, 0);
 		}
 
 		// Voting /////////////////////////////////////////////////////////////////
 		holder.btnVoteUp.getBackground().setAlpha(255);
 		holder.btnVoteUp.setImageResource(R.drawable.vote_up_button);
 		holder.btnVoteDown.getBackground().setAlpha(255);
 		holder.btnVoteDown.setImageResource(R.drawable.vote_down_button);
 		int vote = entry.vote;
 		if (_cacheVoteTemporary.containsKey(entry.id)) {
 			vote |= _cacheVoteTemporary.get(entry.id);
 		}
 		if (vote != C.NULL_VOTE) {
 			// If user voted already...
 			if (vote == C.SHOUT_VOTE_DOWN) {
 				holder.btnVoteUp.setVisibility(View.GONE);
 				holder.btnVoteDown.setVisibility(View.VISIBLE);
 				holder.btnVoteDown.setImageResource(R.drawable.inbox_down_lit);
 				holder.btnVoteDown.getBackground().setAlpha(0);
 				holder.btnVoteDown.setEnabled(false);
 			} else {
 				holder.btnVoteUp.setVisibility(View.VISIBLE);
 				holder.btnVoteDown.setVisibility(View.GONE);
 				holder.btnVoteUp.setImageResource(R.drawable.inbox_up_lit);
 				holder.btnVoteUp.getBackground().setAlpha(0);
 				holder.btnVoteUp.setEnabled(false);
 			}
 		} else {
 			if (entry.open) {
 				holder.btnVoteUp.setVisibility(View.VISIBLE);
 				holder.btnVoteUp.setEnabled(_isInputAllowed);
 				holder.btnVoteUp.setTag(holder);
 				holder.btnVoteDown.setVisibility(View.VISIBLE);
 				holder.btnVoteDown.setEnabled(_isInputAllowed);
 				holder.btnVoteDown.setTag(holder);
 			} else {
 				// holder.btnVoteUp.setEnabled(false);
 				// holder.btnVoteDown.setEnabled(false);
 				holder.btnVoteUp.setVisibility(View.GONE);
 				holder.btnVoteDown.setVisibility(View.GONE);
 			}
 		}
 
 		String timeAgo = "";
 		try {
 			timeAgo = _prettyTime.format(ISO8601DateParser.parse(entry.timestamp));
 		} catch (ParseException ex) {
 			ErrorManager.manage(ex);
 		}
 
 		// Is there a score?
 		String score = Integer.toString(entry.score);
 //		if (entry.score == C.NULL_SCORE) {
 //			score = "?";
 //		}
 
 		// Mark shout as read/unread
 		if (entry.state_flag == C.SHOUT_STATE_NEW) {
 			holder.textC.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
 		} else {
 			holder.textC.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
 		}
 
 		holder.shout = entry;
		holder.textC.setText(entry.text);
		holder.textE.setText(entry.text);
 		holder.scoreC.setText(score);
 		holder.scoreE.setText(score);
 		// This is for score details dialog.
 		holder.scoreC.setTag(holder);
 		holder.scoreE.setTag(holder);
 		holder.timeAgoC.setText(timeAgo);
 		holder.timeAgoE.setText(timeAgo);
 		holder.btnDelete.setTag(holder);
 
 		return convertView;
 	}
 
 	@Override
 	public int getCount() {
 		return _inboxSystem.getDisplayedShouts().size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return _inboxSystem.getDisplayedShouts().get(position);
 	}
 
 }
