 package org.easysms.android.view;
 
 import org.easysms.android.R;
 import org.easysms.android.data.Conversation;
 import org.easysms.android.data.Sms;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 public class ConversationAdapter extends ArrayAdapter<Sms> {
 
 	// /** Conversation that encloses the adapter. */
 	// private Conversation mConversation;
 	/** Inflater used to create the layout based on the XML. */
 	private LayoutInflater mLayoutInflater;
 
 	/**
 	 * Creates a new PlotItemAdapter instance.
 	 */
 	public ConversationAdapter(Context context, Conversation conversation) {
 		super(context, android.R.layout.simple_list_item_1,
 				conversation.listsms);
 
 		// stores the conversation.
 		// mConversation = conversation;
 
 		// preloads the inflater.
 		mLayoutInflater = (LayoutInflater) getContext().getSystemService(
 				Context.LAYOUT_INFLATER_SERVICE);
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		ConversationWrapper wrapper = null;
 		if (row == null) {

			// chooses the layout depending if it is the sender or receiver.
			if (!getItem(position).isSent) {
				row = mLayoutInflater.inflate(
						R.layout.tpl_conversation_left_item, parent, false);
			} else {
				row = mLayoutInflater.inflate(
						R.layout.tpl_conversation_right_item, parent, false);
			}
 			wrapper = new ConversationWrapper(row);
 			row.setTag(wrapper);
 		} else {
 			wrapper = (ConversationWrapper) row.getTag();
 		}
 
 		wrapper.populateFrom(position, getItem(position), getContext());
 		return (row);
 	}
 }
