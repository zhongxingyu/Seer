 package org.minyanmate.minyanmate.adapters;
 
 import android.content.Context;
 import android.net.Uri;
 import android.provider.ContactsContract.Contacts;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.QuickContactBadge;
 import android.widget.TextView;
 
 import org.minyanmate.minyanmate.R;
 import org.minyanmate.minyanmate.models.InvitedMinyanGoer;
 import org.minyanmate.minyanmate.models.MinyanGoer;
 import org.minyanmate.minyanmate.models.UninvitedMinyanGoer;
 
 import java.util.HashMap;
 import java.util.List;
 
 public class ParticipantsExpandableListAdapter extends BaseExpandableListAdapter {
 
 	private Context context;
 	private List<String> _listDataHeader;
 	private HashMap<String, List<MinyanGoer>> _listDataChild;
 	
 	public ParticipantsExpandableListAdapter(Context context, List<String> listDataHeader,
 			HashMap<String, List<MinyanGoer>> listChildData) {
 		this.context = context;
 		this._listDataHeader = listDataHeader;
 		this._listDataChild = listChildData;
 	}
 	
 	public void setListDataHeader(List<String> newHeaders) {
 		this._listDataHeader = newHeaders;
 	}
 	
 	public void setDataChildren(HashMap<String, List<MinyanGoer>> newkidsontheblock) {
 		this._listDataChild = newkidsontheblock;
 	}
 	
 	@Override
 	public Object getChild(int groupPosition, int childPosition) {
 		return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(childPosition);
 	}
 
 	@Override
 	public long getChildId(int groupPosition, int childPosition) {
 		return childPosition;
 	}
 
 
 	@Override
 	public View getChildView(int groupPosition, int childPosition,
 			boolean isLastChild, View convertView, ViewGroup parent) {
 		
 		final MinyanGoer goer = (MinyanGoer) getChild(groupPosition, childPosition);
 
 		if (convertView == null) {
 			LayoutInflater infl = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = infl.inflate(R.layout.fragment_particpant_child, null);
 		}
 		
 		QuickContactBadge badge = (QuickContactBadge) convertView.findViewById(R.id.participantContactBadge);
 		TextView name = (TextView) convertView.findViewById(R.id.participantContactName);
 		
 		if (goer instanceof InvitedMinyanGoer) {
 			Uri contactUri = Contacts.getLookupUri(((InvitedMinyanGoer) goer).getContactId(), 
 					((InvitedMinyanGoer) goer).getLookupKey());
             try {
                 badge.assignContactUri(contactUri);
                 if(((InvitedMinyanGoer) goer).getPhotoThumbnailUri() != null)
                     badge.setImageURI(Uri.parse(((InvitedMinyanGoer) goer).getPhotoThumbnailUri()));
                 else
                     badge.setImageURI(null);
             } catch (Exception e) { Log.e("Exception", e.getLocalizedMessage()); }
 
 		} else if (goer instanceof UninvitedMinyanGoer) {
 			// clear the uri's in case view is being recycled
 			badge.assignContactUri(null);
			badge.setImageResource(R.drawable.social_person_light);
 		}
 		
 		name.setText(goer.getName());
 		
 		return convertView;
 	}
 
 	@Override
 	public int getChildrenCount(int groupPosition) {
 		return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
 	}
 
 	@Override
 	public Object getGroup(int groupPosition) {
 		return this._listDataHeader.get(groupPosition);
 	}
 
 	@Override
 	public int getGroupCount() {
 		return this._listDataHeader.size();
 	}
 
 	@Override
 	public long getGroupId(int groupPosition) {
 		return groupPosition;
 	}
 
 	@Override
 	public View getGroupView(int groupPosition, boolean isExpanded,
 			View convertView, ViewGroup parent) {
 		
 		String headerTitle = (String) getGroup(groupPosition);
 		
 		if (convertView == null) {
 			LayoutInflater infl = (LayoutInflater) this.context
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = infl.inflate(R.layout.fragment_participant_header, null);
 		}
 		
 		int sum = 0;
 		for (List<MinyanGoer> list: _listDataChild.values())
 			sum += list.size();
 		
 		TextView participantCategory = (TextView) convertView.findViewById(R.id.participantHeader);
 		String category = headerTitle + " (" + _listDataChild.get(headerTitle).size() + "/" 
 				+ sum + ")";
 		
 		participantCategory.setText(category);
 		
 		return convertView;
 	}
 
 	@Override
 	public boolean hasStableIds() {
 		return false;
 	}
 
 	@Override
 	public boolean isChildSelectable(int groupPosition, int childPosition) {
 		return true;
 	}
 
 }
