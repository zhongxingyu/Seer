 package com.mobileindustrycast;
 
 import java.util.ArrayList;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 @SuppressLint("DefaultLocale")
 public class ExtendedArrayAdapter extends ArrayAdapter<CustomListMessage> {
   private Context context;
   private ArrayList<CustomListMessage> messageList;
   private ArrayList<CustomListMessage> mOriginalValues;
   private final Object mLock = new Object();
   private ArrayFilter mFilter;
   public boolean buyerSelected = true;
   public boolean sellerSelected = true;
   public boolean tradeSelected = true;
   public boolean infoSelected = true;
 
   public ExtendedArrayAdapter(Context context, ArrayList<CustomListMessage> values) {
     super(context, R.layout.rowlayout, values);
     this.context = context;
     this.messageList = values;
     mOriginalValues = values;
   }
 
   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
     
 	View v = convertView;
 
 	  
 	if (convertView == null){
 	LayoutInflater inflater = (LayoutInflater) context
         .getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
     v = inflater.inflate(R.layout.rowlayout, parent, false);
     }
 
 	TextView textView = (TextView) v.findViewById(R.id.label);
 	ImageView imageView = (ImageView) v.findViewById(R.id.icon);
     
 	
 	CustomListMessage message;
 	if (position<messageList.size())
 	{message = messageList.get(position);
     
     textView.setText(message.getTimestamp()+" "+message.getUserName()+", "+message.getLocation()+", "+message.getStatus()+": "+message.getBody());
     
     // Change the icon/color for broadcast/post messages(with post type as a default one)
     imageView.setImageResource(R.drawable.post);
     textView.setTextColor(Color.parseColor("#90CA77"));
     
     if (message.getMessageType().equals("broadcast")) {
       imageView.setImageResource(R.drawable.broadcast);
       textView.setTextColor(Color.parseColor("#9E3B33"));}
 	}
     return v;
   }
  // 
   
   @Override
   public int getCount() {
       return messageList.size();
   }
 
 
   @Override
   public CustomListMessage getItem(int position) {
       return messageList.get(position);
   }
 
   
   
   public Filter getFilter() {
 	          if (mFilter == null) {
 	              mFilter = new ArrayFilter();
 	          }
 	          return mFilter;
 	      }
   //overriden Search filter
   private class ArrayFilter extends Filter {
 	          @Override
 	          protected FilterResults performFiltering(CharSequence prefix) {
 	              FilterResults results = new FilterResults();
 	  
 	              if (mOriginalValues == null) {
 	                  synchronized (mLock) {
 	                      mOriginalValues = messageList;
 	                  }
 	              }
 	  
 	              /*if ((prefix == null || prefix.length() == 0) && !buyerSelected && !sellerSelected && !tradeSelected && !infoSelected) {
 	                  synchronized (mLock) {
 	                      ArrayList<CustomListMessage> list = new ArrayList<CustomListMessage>(mOriginalValues);
 	                      results.values = list;
 	                      results.count = list.size();
 	                  }
 	              }
 	                  else*/ if(!buyerSelected || !sellerSelected || !tradeSelected || !infoSelected && (prefix == null || prefix.length() == 0))
 	                  {
 
 	                	  
 		                  final ArrayList<CustomListMessage> values = mOriginalValues;
 		                  final int count = values.size();
 		  
 		                  final ArrayList<CustomListMessage> newValues = new ArrayList<CustomListMessage>(count);
 		  
 		                  for (int i = 0; i < count; i++) {
 		                      final CustomListMessage value = values.get(i);
 
 		  
 		                      // First match against the whole, non-splitted value
 		                      
 		                      switch (value.getStatusEnum())
 		                      {case 1:
 		                    	  if(buyerSelected) {
 		    	                          newValues.add(value);
 		    	                      }
 		                    	  break;
 		                      case 2:
 		                    	  if(sellerSelected){
 		    	                          newValues.add(value);
 		    	                      }
 		                    	  break;
 		                      case 3:
 		                    	  if(tradeSelected){
 		    	                          newValues.add(value);
 		    	                      }
 		                    	  break;
 		                      case 4:
 		                    	  if(infoSelected){
 		    	                          newValues.add(value);
 		    	                       }
 		                    	  break;
 		                      
 		                      }
 		                     
 		                  }
 		  
 		                  results.values = newValues;
 		                  results.count = newValues.size();
 	                  }
 	               else {
 	                  String prefixString = prefix.toString().toLowerCase();
 	  
 	                  final ArrayList<CustomListMessage> values = mOriginalValues;
 	                  final int count = values.size();
 	  
 	                  final ArrayList<CustomListMessage> newValues = new ArrayList<CustomListMessage>(count);
 	  
 	                  for (int i = 0; i < count; i++) {
 	                      final CustomListMessage value = values.get(i);
 	                      final String valueText = value.messageToString().toLowerCase();
 	  
 	                      // First match against the whole, non-splitted value
 	                      
 	                      switch (value.getStatusEnum())
 	                      {case 1:
 	                    	  if(buyerSelected)
 	                    		  {if (valueText.contains(prefixString)) {
 	    	                          newValues.add(value);
 	    	                      } }
 	                    	  break;
 	                      case 2:
 	                    	  if(sellerSelected )
 	                    		  {if (valueText.contains(prefixString)) {
 	    	                          newValues.add(value);
 	    	                      } }
 	                    	  break;
 	                      case 3:
 	                    	  if(tradeSelected)
 	                    		  {if (valueText.contains(prefixString)) {
 	    	                          newValues.add(value);
 	    	                      } }
 	                    	  break;
 	                      case 4:
 	                    	  if(infoSelected)
 	                    		  {if (valueText.contains(prefixString)) {
 	    	                          newValues.add(value);
 	    	                      } }
 	                    	  break;
 	                      
 	                      }
 	                     
 	                  }
 	  
 	                  results.values = newValues;
 	                  results.count = newValues.size();
 	              }
 	  
 	              return results;
 	          }
 	  
 	       @SuppressWarnings("unchecked")
 		@Override
 	       protected void publishResults(CharSequence constraint, FilterResults results) {
 	           //noinspection unchecked
 	           messageList = (ArrayList<CustomListMessage>) results.values;
 	            if (results.count > 0) {
 	             notifyDataSetChanged();
 	           } else {
                notifyDataSetInvalidated();
 	            }
 	          }
 	     }
 	 
  //
   
   public boolean getBuyer()
   {return buyerSelected;}
   
   public boolean getSeller()
   {return sellerSelected;}
   
   public boolean getTrade()
   {return tradeSelected;}
   
   public boolean getInfo()
   {return infoSelected;}
   
   public void setBuyer(boolean state)
   {buyerSelected = state;}
   
   public void setSeller(boolean state)
   {buyerSelected = state;}
   
   public void setTrade(boolean state)
   {buyerSelected = state;}
   
   public void setInfo(boolean state)
   {buyerSelected = state;}
   }
