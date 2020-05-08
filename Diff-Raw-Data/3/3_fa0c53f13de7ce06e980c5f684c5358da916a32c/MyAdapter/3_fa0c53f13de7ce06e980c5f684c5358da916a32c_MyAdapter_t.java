 /*
  * 用于定义自己的Adapter，既为SNS显示准备，也为黑名单做准备
  */
 package com.example.ui;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.example.skylark.R;
 import com.example.skylark.R.drawable;
 
 import android.annotation.SuppressLint;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class MyAdapter extends BaseAdapter
 {
 	private Context context;
 	private ArrayList<Drawable> icons=new ArrayList<Drawable>();
 	//private Drawable[] icons;
 	//private String[] names=new String[1000];
 	private ArrayList<String> names=new ArrayList<String>();
 	private boolean haveACheckBox;
 	private HashMap<Integer, Boolean> isSelected=new HashMap<Integer, Boolean>();
 	//private CheckBox check;
 	/*
 	 * 用于使用Resources进行构造的构造函数。
 	 */
 	public MyAdapter(Context context,int iconIDs[],String names[],boolean haveACheckBox)
 	{
 		Drawable d;
 		this.context=context;
 		for(int i=0;i<iconIDs.length;i++)
 		{
 			//icons[i]=context.getResources().getDrawable(iconIDs[i]);
 			//icons[i]=context.getResources().getDrawable(R.drawable.friends);
 			//Toast.makeText(context, ""+R.drawable.renren, Toast.LENGTH_LONG).show();
 			
 			int s=iconIDs[i];
 			
 			if(s!=0)
 			{
 				d=context.getResources().getDrawable(s);
 				icons.add(d);
 			}
 			else icons.add(context.getResources().getDrawable(R.drawable.friends));
 			//Toast.makeText(context, icons.size()+"", Toast.LENGTH_LONG).show();
 			this.names.add(names[i]);
 		}
 		//this.names=names;
 		this.haveACheckBox=haveACheckBox;
 		iniData();
 	}
 	/*
 	 * 用于使用drawable数组进行构造的构造函数
 	 */
 	public MyAdapter(Context context,ArrayList<Drawable> icons,ArrayList<String> names,boolean haveACheckBox)
 	{
 		this.context=context;
 		this.icons=icons;
 		this.names=names;
 		this.haveACheckBox=haveACheckBox;
 		iniData();
 	}
 	
 	public MyAdapter(Context context,ArrayList<String> names, boolean haveACheckBox)
 	{
 		for(int i=0;i<names.size();i++)
 		{
 			this.icons.add(context.getResources().getDrawable(R.drawable.friends));
 		}
 		this.names=names;
 		this.context=context;
 		this.haveACheckBox=haveACheckBox;
 		iniData();
 	}
 	/*
 	public MyAdapter(Context context,ArrayList<Integer> iconIDs, ArrayList<String> names, boolean haveACheckBox)
 	{
 		Drawable d;
 		this.context=context;
 		for(int i=0;i<iconIDs.size();i++)
 		{
 			//icons[i]=context.getResources().getDrawable(iconIDs[i]);
 			//icons[i]=context.getResources().getDrawable(R.drawable.friends);
 			//Toast.makeText(context, ""+R.drawable.renren, Toast.LENGTH_LONG).show();
 			
 			int s=iconIDs.get(i);
 			
 			if(s!=0)
 			{
 				d=context.getResources().getDrawable(s);
 				icons.add(d);
 			}
 			else icons.add(context.getResources().getDrawable(R.drawable.friends));
 			//Toast.makeText(context, icons.size()+"", Toast.LENGTH_LONG).show();
 			this.names=names;
 		}
 		//this.names=names;
 		this.haveACheckBox=haveACheckBox;
 		iniData();
 	}
 	*/
 	/*
 	 * 初始化多选数组。
 	 */
 	public void iniData()
 	{
 		for(Integer i=0;i<icons.size();i++)
 		{
 			isSelected.put(i, false);
 		}
 	}
 	
 	/*
 	 * 以下四个方法是BaseAdapter必须实现的。
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getCount()
 	 */
 	public int getCount()
 	{
 		return icons.size();
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getItem(int)
 	 */
 	public Object getItem(int position)
 	{
 		//return iconIDs[position];
 		return icons.get(position);
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getItemId(int)
 	 */
 	public long getItemId(int position)
 	{
 		return position;
 	}
 	/*
 	 * (non-Javadoc)
 	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
 	 */
 	@SuppressLint("NewApi")
 	public View getView(int position,View convertView,ViewGroup parent)
 	{
 		LinearLayout ll=new LinearLayout(context);
 		ViewHolder viewHolder=null;
 		ll.setOrientation(LinearLayout.HORIZONTAL);
 		ll.setGravity(Gravity.CENTER_VERTICAL);
 		
 		if(convertView==null)
 		{
 			viewHolder=new ViewHolder();
 			ImageView icon=new ImageView(context);
 			//icon.setImageResource(iconIDs[position]);
 			icon.setImageDrawable(icons.get(position));
 			icon.setLayoutParams(new ViewGroup.LayoutParams(60,60));
 			ll.addView(icon);
 			viewHolder.img=icon;
 			
 			TextView name=new TextView(context);
 			name.setText(names.get(position));
 			name.setTextSize(20);
 			name.setTextColor(Color.BLUE);
 			name.setPadding(30, 0, 0, 0);
 			name.setGravity(Gravity.CENTER);
 			ll.addView(name);
 			viewHolder.text=name;
 			
 			CheckBox check=new CheckBox(context);
 			check.setChecked(isSelected.get(position));
 			check.setGravity(Gravity.CENTER_HORIZONTAL);
 			check.setFocusable(false);
 			check.setClickable(false);
 			if(!haveACheckBox)
 			{
 				check.setVisibility(8);
 			}
 			//this.check=check;
 			//check.setPadding(0, 0, 100, 0);
 			ll.addView(check);
 			viewHolder.cb=check;
 			
 			ll.setTag(viewHolder);
 			convertView=ll;
 		}
 		else
 		{
 			viewHolder=(ViewHolder)convertView.getTag();
 			viewHolder.img.setImageDrawable(icons.get(position));
 			viewHolder.text.setText(names.get(position));
 			//viewHolder.cb.setChecked(isSelected.get(position));
 		}
 		//convertView.setBackgroundColor(Color.LTGRAY);
 		convertView.setPadding(0, 10, 0, 10);
 //		convertView.setLeft();
 		//convertView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
 		return convertView;
 	}
 	public HashMap<Integer,Boolean> getIsSelected()
 	{
 		return isSelected;
 	}
 	public void setIsSelected(HashMap<Integer,Boolean> isSelected)
 	{
 		this.isSelected=isSelected;
 	}
 	public void setIsSelected(int position)
 	{
 		if(position<0 || position>getCount())
 		{
 			return;
 		}
 		isSelected.put(position,isSelected.get(position) ^ true);
 		//check.setChecked(true);
 		//Toast.makeText(context, check.isChecked()+"", Toast.LENGTH_LONG).show();
 	}
 	public class ViewHolder
 	{
 		public ImageView img;
 		public TextView text;
 		public CheckBox cb;
 	}
 }
