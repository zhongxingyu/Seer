 package com.example.contactlistactivity1;
 
 
 import java.util.List;
 
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ContactAdapter1 extends ArrayAdapter<Contact1> {
 
 	private final List<Contact1> _contacts;
 	private final Activity _context;
 	
 	public ContactAdapter1(Activity context, List<Contact1> contacts)
 	{
 		super(context,R.layout.contactlistitem,contacts);
 		this._contacts=contacts;
 		this._context=context;
 	}
 	static class ViewHolder {
 		protected TextView text;
 		private Contact1  _contact;
 		public ImageView imageview;
 		protected void setContact(Contact1 contact)
 		{
 			text.setText(contact.getDisplayName());
 			imageview.setImageBitmap(contact.getImage());
 			_contact=contact;
 		}
 		protected Contact1 getContact() {return _contact;}
 	}
 	@Override
 	public Contact1 getItem(int position)
 	{
 		return _contacts.get(position);
 	}
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent)
 	{
 		
 		View view=null;
 		
 
		if(convertView==null)
		{
 			LayoutInflater inflater=_context.getLayoutInflater();
 			view=inflater.inflate(R.layout.contactlistitem, null);
 			final ViewHolder viewHolder=new ViewHolder();
 			viewHolder.text=(TextView)view.findViewById(R.id.txtDisplayName);
 			viewHolder.imageview =(ImageView)view.findViewById(R.id.contact_image);
 			viewHolder.setContact(_contacts.get(position));
 			view.setTag(viewHolder);
 			
		}
 		
		else 
		{
			view = convertView;
		}
 		
 		return view;
 	}
 }
 
