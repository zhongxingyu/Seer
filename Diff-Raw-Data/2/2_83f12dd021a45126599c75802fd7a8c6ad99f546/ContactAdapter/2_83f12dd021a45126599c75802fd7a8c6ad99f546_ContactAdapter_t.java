 package com.meetme.contacts;
 
 import java.util.ArrayList;
 
 import com.meetme.app.R;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class ContactAdapter extends ArrayAdapter<Contact>{
 	private ArrayList<Contact> contacts;
 	
 	public ContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> contacts){
 		super(context, textViewResourceId, contacts);
 		
 		this.contacts = contacts;
 	}
 	
 	@Override
     public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		if (v == null) {
             LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             v = vi.inflate(R.layout.contact_item, null);
 		}
 		Contact contact = contacts.get(position);
 		if(contact != null){
 			TextView name = (TextView)v.findViewById(R.id.contact_name);
 			TextView company = (TextView)v.findViewById(R.id.contact_company);
 			TextView com_position = (TextView)v.findViewById(R.id.contact_position);
 			
 			if(name != null){
 				name.setText(contact.getName());
 			}
 			if(company != null){
 				company.setText(contact.getCompany());
 			}
 			if(com_position != null){
				com_position.setText(contact.getPosition());
 			}
 		}
     
 		return v;
 	}
 }
