 package com.example.testapplication;
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.List;
 
 public class ContactAdapter extends ArrayAdapter<Contact> {
 
     private final List<Contact> _contacts;
     private final Activity _context;
 
     public ContactAdapter(Activity context, List<Contact> contacts) {
         super(context, R.layout.contactlistitem, contacts);
         this._contacts = contacts;
         this._context = context;
     }
 
     static class ViewHolder {
         protected TextView text;
         private Contact _contact;
 
         protected void setContact(Contact contact) {
             text.setText(contact.getDisplayName());
             _contact = contact;
         }
 
         protected Contact getContact() {
             return _contact;
         }
     }
 
     @Override
     public Contact getItem(int position) {
         return _contacts.get(position);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
         if (convertView == null) {
             LayoutInflater inflater = _context.getLayoutInflater();
             view = inflater.inflate(R.layout.contactlistitem, null);
             final ViewHolder viewHolder = new ViewHolder();
             viewHolder.text = (TextView) view.findViewById(R.id.txtDisplayName);
             viewHolder.setContact(_contacts.get(position));
             view.setTag(viewHolder);
         }
 
         return view;
     }
 }
