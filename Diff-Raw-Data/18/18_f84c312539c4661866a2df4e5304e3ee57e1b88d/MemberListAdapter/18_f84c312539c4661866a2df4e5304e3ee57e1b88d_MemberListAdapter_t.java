 package com.moac.android.opensecretsanta.activity;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.moac.android.opensecretsanta.R;
 
 import java.util.List;
 
 public class MemberListAdapter extends ArrayAdapter<MemberRowDetails> {
 
     private final String TAG = MemberListAdapter.class.getSimpleName();
 
     private int mResource;
 
     public MemberListAdapter(Context _context, int _resource,
                              List<MemberRowDetails> _members) {
         super(_context, _resource, _members);
         mResource = _resource;
     }
 
     @Override
     public View getView(int _position, View _convertView, ViewGroup _parent) {
 
         View v = _convertView;
         ViewHolder holder;
 
         if(v == null) {
            // Some good info on LayoutInflater here - http://stackoverflow.com/questions/5026926/making-sense-of-layoutinflater
            LinearLayout parent = new LinearLayout(getContext());
            v = LayoutInflater.from(getContext()).inflate(mResource, parent, false);
 
             holder = new ViewHolder();
            holder.mMemberNameView = (TextView) v.findViewById(R.id.member_name_textview);
            holder.mContactModeView = (TextView) v.findViewById(R.id.contact_mode_textview);
            holder.mRestrictionsView = (TextView) v.findViewById(R.id.restriction_count_textview);
 
            v.setTag(holder);
         } else {
             // View already exists, retrieve the holder instance from the View
             holder = (ViewHolder) v.getTag();
         }
 
         MemberRowDetails details = getItem(_position);
         holder.mMemberNameView.setText(details.mMemberName);
         holder.mContactModeView.setText(details.mContactDetail);
 
         if(details.mRestrictionCount > 0) {
             holder.mRestrictionsView.setText(String.valueOf(details.mRestrictionCount));
             holder.mRestrictionsView.setVisibility(View.VISIBLE);
         } else {
             holder.mRestrictionsView.setVisibility(View.GONE);
         }
 
         v.setTag(holder);
         return v;
     }
 
     // the ViewHolder keeps references to avoid making unnecessary calls to findViewById
     private class ViewHolder {
         TextView mMemberNameView;
         TextView mRestrictionsView;
         TextView mContactModeView;
     }
 }
