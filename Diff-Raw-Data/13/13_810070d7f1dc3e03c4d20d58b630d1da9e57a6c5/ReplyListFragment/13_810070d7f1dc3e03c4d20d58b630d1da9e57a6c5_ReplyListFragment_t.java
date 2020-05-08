 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import android.support.v4.app.ListFragment;
 import android.widget.SimpleAdapter;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.forum.models.Reply;
 
 public class ReplyListFragment extends ListFragment {
 	
 	private String[] from = new String[] {"reply_author", "reply_text"};
 	private int[] to = new int[] {R.id.reply_author, R.id.reply_text};
 	
 	private List<HashMap<String, String>> data;
 
 	public void updateReplyList(List<Reply> replyList) {
 		data = new ArrayList<HashMap<String, String>>();
 				
 		for(int i = 0; i < replyList.size(); i++) {
 			Reply reply= replyList.get(i);
 			data.add(replyToMap(reply));
 		}
 		
 		if (getActivity() != null) {
			System.out.println("Making reply list");
 	        SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), data, R.layout.reply_list_item, from, to);
 	        setListAdapter(adapter);
 	        data = null;
 		}
     }
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (data != null) {
			System.out.println("Making reply list");
 			SimpleAdapter adapter = new SimpleAdapter(getActivity().getBaseContext(), data, R.layout.reply_list_item, from, to);
 	        setListAdapter(adapter);
 	        data = null;
 		}
 	}
 	
 	private HashMap<String, String> replyToMap(Reply reply) {
 		String author = reply.getAccount() != null ? reply.getAccount().getUsername() : null;
 		String text = reply.getText();
 		HashMap<String, String> row = new HashMap<String, String>();
		row.put("reply_author", author == null ? "<Anonymous>" : author);
 		row.put("reply_text", text);
 		return row;
 	}
 }
