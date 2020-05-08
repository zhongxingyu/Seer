 package com.swap;
 
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class ItemAdapter extends ArrayAdapter<Item> {
 
 	Context context; 
     int layoutResourceId;    
     Item data[] = null;
     public ImageLoader imageLoader; 
     
     public ItemAdapter(Context context, int layoutResourceId, Item[] data) {
         super(context, layoutResourceId, data);
         this.layoutResourceId = layoutResourceId;
         this.context = context;
         this.data = data;
         
         imageLoader = new ImageLoader(((Activity) context).getApplicationContext());
     }
 	
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         View row = convertView;
         ItemHolder holder = null;
         
         if (row == null)
         {
             LayoutInflater inflater = ((Activity) context).getLayoutInflater();
             row = inflater.inflate(layoutResourceId, parent, false);
             
             holder = new ItemHolder();
             holder.thumbnail = (ImageView)row.findViewById(R.id.list_image);
             holder.title = (TextView)row.findViewById(R.id.title);
             holder.description = (TextView)row.findViewById(R.id.description);
             holder.price = (TextView)row.findViewById(R.id.price);
             
             row.setTag(holder);
         }
         else
         {
             holder = (ItemHolder)row.getTag();
         }
         
         Item item = data[position];
         holder.title.setText(item.title);
         holder.description.setText(item.description);
         holder.price.setText("$" + dollarFormat(item.price));
         
        // SET SPACING HERE
        ((MarginLayoutParams) holder.price.getLayoutParams()).rightMargin = 15;
        // SET COLORS HERE
        holder.price.setTextColor(row.getResources().getColor(R.color.swapred2));
        holder.price.setShadowLayer(2, 1, 1, row.getResources().getColor(R.color.gray));
        holder.title.setTextColor(row.getResources().getColor(R.color.black));
        
         if (item.imagesnum > 0)
 		{			
 			//ImageDownloader mDownload = ImageDownloader.getInstance();
 			//mDownload.download(ItemDetailActivity.IMAGES_FOLDER + Integer.toString(item.id) + "_1.jpg", holder.thumbnail);
         	
         	imageLoader.DisplayImage(ItemDetailActivity.IMAGES_FOLDER + Integer.toString(item.id) + "_1.jpg", holder.thumbnail);
 		}
         
         if (item.featured)
         {
         	//row.setBackgroundColor(0xFFFFEBAB);
         	row.setBackgroundColor(0x20EBE54D);
         }
         
         return row;
     }
     
     public static String dollarFormat(float number) {
     	float epsilon = (float) 0.004;
     	if ( Math.abs(Math.round(number) - number) < epsilon) {
     		return String.format("%.0f", number);
     	} else {
     		return String.format("%.2f", number);
     	}
     }
     
     static class ItemHolder
     {
         ImageView thumbnail;
         TextView price;
         TextView title;
         TextView description;
     }
 }
