 package com.rnm.keepintouch;
 
 import java.io.InputStream;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.provider.ContactsContract;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.rnm.keepintouch.data.Contact;
 import com.rnm.keepintouch.data.ContactEvent;
 import com.rnm.keepintouch.data.ContactEvent.TYPE;
 
 public class ContactsAdapter extends ArrayAdapter<Contact> {
 	
 	public ContactsAdapter(Context context, int textViewResourceId, List<Contact> objects) {
 		super(context, textViewResourceId, objects);
 	}
 
 	@Override
 	public View getView(int position, View view, ViewGroup viewGroup) {
 		final Contact contact = (Contact) getItem(position);
 		if(view == null){
 			LayoutInflater inf = LayoutInflater.from(getContext());
 			
 			view = inf.inflate(R.layout.contact_list_item, null, false);
 		}
 		
 		Log.d("ContactsAdapter", "Making badge " + contact.name);
 		
 //		QuickContactBadge badge = (QuickContactBadge)view.findViewById(R.id.contacted_badge);
 //		//badge.assignContactFromPhone("630 913 9425", true);
 //		badge.assignContactUri(contact.uri);
 //        badge.setMode(ContactsContract.QuickContact.MODE_LARGE);
 		ImageView badge = (ImageView)view.findViewById(R.id.contacted_badge);
         InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContext().getContentResolver(), contact.uri, true);
         Bitmap bitmap = BitmapFactory.decodeStream(input);
         if (bitmap != null)
         	badge.setImageBitmap(bitmap);
         else
         	badge.setImageResource(R.drawable.ic_contact_picture);
 		TextView name = (TextView)view.findViewById(R.id.contacted_name);
 		name.setText(contact.name);
         
 		ContactEvent mostrecent = contact.getLatest();
 		
 		TextView method = (TextView)view.findViewById(R.id.contacted_method);		
 		TextView contacted = (TextView)view.findViewById(R.id.contacted_time);
 		
 		
 		if (mostrecent != null) {
 			method.setVisibility(View.VISIBLE);
 			method.setText(mostrecent.type == TYPE.SMS ? "text message" : "phone call");
 			if(mostrecent.timestamp == Long.MIN_VALUE){
 				contacted.setText("never");
 			}else{
 				contacted.setText(formatTimeAgo(System.currentTimeMillis() - mostrecent.timestamp));
 			}
 		} else {
 			method.setText("");
 			method.setVisibility(View.GONE);
 			contacted.setText("never");
 		}
 		
 		
 		return view;
 	}
 	
 	
 	private String formatTimeAgo(long span) {
 		Log.d("ContactsAdapter", "Formatting span "+span);
 		if (span < 1000*60) {
 			return "A few seconds ago";
 		} else if (span < 1000*60*60) {
 			return "A few minutes ago";
 		} else if (span < 1000*60*60*24) {
 			return ""+Math.round(((double)span)/(1000*60*60.0))+" hours ago";
 		} else if (span < 1000*60*60*24*30) {
 			return ""+Math.round(((double)span)/(1000*60*60*24.0))+" days ago";
 		}
 		long value = Math.round(((double)span)/(1000*60*60*24*30.0));
 		if (value <= 1)
			return "about a month ago";
 		else
			return "about "+value+" months ago";
 
 	}
 
 }
