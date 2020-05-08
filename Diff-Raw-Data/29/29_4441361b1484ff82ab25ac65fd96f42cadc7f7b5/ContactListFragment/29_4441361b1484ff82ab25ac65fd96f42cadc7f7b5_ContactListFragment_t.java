 package com.tuvistavie.meetup.contact.fragment;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 import com.google.inject.Inject;
 import com.tuvistavie.meetup.R;
 import com.tuvistavie.meetup.contact.model.ContactList;
 import com.tuvistavie.meetup.contact.util.ContactListAdapter;
 import com.tuvistavie.meetup.contact.util.SelectContactListAdapter;
import com.tuvistavie.meetup.model.listener.OnUpdateListener;
 
 import roboguice.RoboGuice;
 import roboguice.fragment.RoboFragment;
 import roboguice.inject.InjectView;
 
 /**
  * Created by daniel on 9/4/13.
  */
 public class ContactListFragment extends RoboFragment {
     private static final String TAG = "com.tuvistavie.meetup.contacts.fragment.ContactListFragment";
 
     @Inject ContactList contactList;
     @Inject SelectContactListAdapter contactListAdapter;
     @InjectView(R.id.contact_list_view) ListView contactListView;
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View rootView = inflater.inflate(R.layout.fragment_contact_list, container, false);
         return rootView;
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
         contactListAdapter.setCollection(contactList);
         contactListAdapter.enableAutoUpdate(getActivity());
         contactListView.setAdapter(contactListAdapter);
         contactList.loadFromPhoneBook();
        contactList.setOnUpdateListener(new OnUpdateListener() {
            @Override
            public void onUpdate(Object updatedObject) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contactListAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
     }
 
     public ContactList getSelectedContacts() {
         return contactListAdapter.getSelected();
     }
 }
