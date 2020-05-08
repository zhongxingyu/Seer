 /*
  * To change this template, choose Tools | Templates
  * && open the template in the editor.
  */
 package com.img.jk.beethelion;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.net.Uri;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * <p>
  * </p><p>
  * Created on 11 Aug 2010
  * </p>
  *
  * @author Jason Morris
  */
 class FlowerAdapter extends BaseAdapter {
 	private List<FlowerItem> m_lsFlower;
 	
 	FlowerAdapter()
 	{
 	  m_lsFlower = new ArrayList<FlowerItem>();	  
 	}
 	public void add(FlowerItem items)
 	{
 	  m_lsFlower.add(items);	
 	}
 	public void clear()
 	{
 		m_lsFlower.clear();
 	}
 //    private final FruitItem[] items;
 //    
 //    FruitAdapter(final FruitItem... items) {
 //        this.items = items;
 //        
 //    }
    
     private ViewGroup getViewGroup(
             final View reuse,
             final ViewGroup parent) {
 
         if(reuse instanceof ViewGroup) {
             return (ViewGroup)reuse;
         } else {
             final Context context = parent.getContext();
             final LayoutInflater inflater = LayoutInflater.from(context);
 
             final ViewGroup item = (ViewGroup)inflater.inflate(
                     R.layout.flower_item,
                     null);
             
             return item;
         }
     }
 
     public int getCount() {
 //        return items.length;
     	return m_lsFlower.size();
     }
 
     public Object getItem(final int index) {
      //   return items[index];
         return m_lsFlower.get(index);
     }
     public long getItemId(final int index) {
         return index;
     }
     public String getItemUri(final int index) {
     	final FlowerItem item = m_lsFlower.get(index);
         return item.mUri;
     }
     
     public View getView(
             final int index,
             final View reuse,
             final ViewGroup parent) {
 
         final ViewGroup view = getViewGroup(reuse, parent);
         //final FruitItem item = items[index];
         final FlowerItem item = m_lsFlower.get(index);
         final TextView text = ((TextView)view.findViewById(R.id.text));
         final ImageView image = ((ImageView)view.findViewById(R.id.icon));
 
         
         //if((item.name.toString().compareTo("͡?")== 0) && (item.mUri.toString().indexOf("apple.jpg") != -1))
         if(index == 0)
         {
         	Log.e("karn",item.name.toString() + " " + index);
         	view.setBackgroundColor(Color.GREEN);
         	text.setTextColor(Color.BLACK);
         }
         else
         {
         	view.setBackgroundColor(Color.BLACK);
         	text.setTextColor(Color.WHITE);
         	
         }
         Log.e("karnx",item.name.toString() + " " + index);
         
         text.setText(item.name);
         //image.setImageResource(item.image);
 
         // Program will hang this line when continuous run
         /* 08-23 23:19:57.166: ERROR/dalvikvm-heap(31182): 1228800-byte external allocation too large for this process.
         08-23 23:19:57.166: ERROR/GraphicsJNI(31182): VM won't let us allocate 1228800 bytes */
 
         image.setImageURI(Uri.fromFile(new File(item.mUri)));  //new File("/sdcard/cats.jpg")
         //Or with
         //image.setImageURI(Uri.parse(new File(item.mUri).toString()));
         
         return view;
     }
 	
 
 
 
 }
