 package com.plantec.uwm;
 
 import java.util.ArrayList;
 
 import com.plantec.uwm.mail.Mail;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Typeface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class ListAdapter extends ArrayAdapter<Mail>{
 	Context context; 
     int layoutResourceId;    
     ArrayList<Mail> data = null;
     
     public ListAdapter(Context context, int layoutResourceId, ArrayList<Mail> data) {
         super(context, layoutResourceId, data);
         this.layoutResourceId = layoutResourceId;
         this.context = context;
         this.data = data;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View row = convertView;
         MailHolder holder = null;
         
         if(row == null)
         {
             LayoutInflater inflater = ((Activity)context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, parent, false);
             
             holder = new MailHolder();
             holder.sender = (TextView) row.findViewById(R.id.main_list_item_sender);
             holder.subject = (TextView) row.findViewById(R.id.main_list_item_subject);
             holder.received = (TextView) row.findViewById(R.id.main_list_item_received);            
             row.setTag(holder);
         }
         else
         {
             holder = (MailHolder)row.getTag();
         }
         
         Mail  mail = data.get(position);
         
         holder.sender.setText(mail.getSender());
         holder.subject.setText(mail.getSubject());
         holder.received.setText(mail.getReceieved());
         
         if (mail.getReadStatus()){
         	holder.sender.setTypeface(null, Typeface.BOLD);
         } else {
         	holder.sender.setTypeface(null, Typeface.NORMAL);
         }
         
         return row;
     }
     
     static class MailHolder {
     	TextView sender;
     	TextView subject;
     	TextView received;
     }
 }
