 package com.example.ui;
 /*
  * 黑名单界面，包括一个黑名单组的列表，每一条同时对应一个黑名单
  * 在黑名单之下是添加按钮和修改按钮
  */
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.http.util.EncodingUtils;
 
 import com.example.skylark.*;
 import com.example.ui.MyAdapter.ViewHolder;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources.NotFoundException;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.AlphaAnimation;
 import android.view.animation.AnimationSet;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class BLInitialize{
 	private ListView blList;
 	private Button manageButton;
 	private Button addButton;
 	private Context context;
 	private TestFragment fragment;
 	private View view;
 	private ArrayList<String> names=new ArrayList<String>();
 	
 	public BLInitialize(View view,TestFragment fragment,Context context)
 	{
 		this.view=view;
 		this.fragment=fragment;
 		//context=MyApplication.getInstance();
 		this.context=context;
 	}
 	public void initial(){
 		
 		blList=(ListView)view.findViewById(R.id.BLList);
 		iniList();//初始化黑名单列表
 		
 		/*
 		 * 为”添加黑名单“绑定监听
 		 */
 		addButton=(Button)view.findViewById(R.id.addBL);
 		addButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent=new Intent();
 				intent.setClass(context, DefineBlackList.class);
 				context.startActivity(intent);
 				
 			}
 		});
 		
 		/*
 		 * 为”管理黑名单“添加监听
 		 */
 		manageButton=(Button)view.findViewById(R.id.manageBL);
 		manageButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				//Toast.makeText(BlackListActivity.this, manageButton.getText().toString(), Toast.LENGTH_LONG).show();
 				if(manageButton.getText().toString().contains("管理"))
 				{
 					showCheckBox();
 				}
 				else
 				{
 					MultiDeleteBL();
 				}
 			}
 		});
 	}
 	
 	/*
 	 * 显示CheckBox，用于多选以便批量删除
 	 */
 	public void showCheckBox()
 	{
 		manageButton.setText("删除");
 		MyAdapter adapter=new MyAdapter(context, names,true);
 		blList.setAdapter(adapter);
 		blList.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				/*
 				 * 对于每一个被点击的item，设置isSelected属性，以便在save方法中获取多选的项目
 				 * 具体察看MyAdapter类
 				 */
 				ViewHolder viewHolder=(ViewHolder)arg1.getTag();
 				viewHolder.cb.toggle();
 				MyAdapter adapter=((MyAdapter)blList.getAdapter());
 				adapter.setIsSelected(arg2);
 				//Toast.makeText(DefineBlackList.this, ""+arg2, Toast.LENGTH_LONG).show();
 				//vHolder vh=(vHolder)arg1.getTag();
 				//vh.cb.setChecked(true);
 			}
 		});
 	}
 	
 	/*
 	 * 批量删除
 	 */
 	public void MultiDeleteBL()
 	{
 		MyAdapter adapter=(MyAdapter)blList.getAdapter();
 		for(int i=0;i<names.size();i++)
 		{
 			if(adapter.getIsSelected().get(i))
 			{
 				names.remove(i);
 			}
 		}
 		updateList();
 	}
 	
 	/*
 	 * 用于更新list
 	 */
 	public void updateList()
 	{
 		String nowBlNames="";
 		MyAdapter adapter=new MyAdapter(context, names, false);
 		blList.setAdapter(adapter);
 		manageButton.setText("管理黑名单");
 		for(String name:names)
 		{
 			nowBlNames=nowBlNames+name+" ";
 		}
 		FileOutputStream fout;
 		try {
 			fout = context.openFileOutput(context.getResources().getString(R.string.blListNames),Context.MODE_PRIVATE);
 			try {
 				fout.write(nowBlNames.getBytes());
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void iniList()
 	{
 		/*
 		 * 首先获取所有的黑名单名称，存放于一个list中
 		 */
 		String blNames = "";
 		FileInputStream fin;
 		try {
 			fin = context.openFileInput(context.getResources().getString(R.string.blListNames));
 			int length = fin.available();   
         byte [] buffer = new byte[length];   
         fin.read(buffer);       
         blNames = ""+EncodingUtils.getString(buffer, "UTF-8");
         fin.close();  
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		//Toast.makeText(this, blNames, Toast.LENGTH_LONG).show();
 		while(blNames.length()>0)
 		{
 			String name=blNames.substring(0,blNames.indexOf(" "));
 			names.add(name);
 			blNames=blNames.substring(blNames.indexOf(" ")+1);
 		}
 		
 		//下面先判断是否为空，若为空，则弹出Toast，后期可以改成TextView显示
 		if(names.size()==0)
 		{
 			Toast.makeText(context, "您当前没有任何黑名单", Toast.LENGTH_SHORT).show();
 		}
 		
 		/*
 		 * 设置Adapter，暂时使用ArrayAdapter，然而后期必须修改
 		 * 以便点击manager按钮的时候可以出现CheckBox
 		 */
 		//ArrayAdapter adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,names);
 		ArrayList<Drawable> icons=new ArrayList<Drawable>();
 		for(int i=0;i<names.size();i++)
 		{
 			icons.add(context.getResources().getDrawable(R.drawable.stop));
 		}
 		MyAdapter adapter=new MyAdapter(context, icons, names,false);
 		blList.setAdapter(adapter);
 		blList.setOnItemClickListener(new OnItemClickListener() {
 			/*
 			 * 点击之后跳转，显示一个列表，列表每一条为一个被禁的应用
 			 * 把BLName传给intent，以便读取文件
 			 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
 			 */
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				Intent intent=new Intent();
 				intent.setClass(context, ShowBlackList.class);
 				intent.putExtra("BLName", names.get(arg2));
 				//Toast.makeText(BlackListActivity.this, names.get(arg2), Toast.LENGTH_SHORT).show();
 				fragment.startActivity(intent);
 				
 			}
 		});
 		
 		/*
 		 * 长按list_item，可以删除相应的name
 		 */
 		blList.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
 					final int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				/*
 				MyAdapter adapter=(MyAdapter)blList.getAdapter();
 				ViewHolder viewHolder=(ViewHolder)arg1.getTag();
 				viewHolder.cb.setVisibility(0);
 				viewHolder.cb.setChecked(true);
 				adapter.setIsSelected(arg2);
 				*/
 				/*
 				 * 弹出对话框，确定是否选择。
 				 */
 				
 				Builder alert= new AlertDialog.Builder(context)
 					.setTitle("删除所选的黑名单")
 					.setMessage("确定删除吗？")
 					.setPositiveButton("是", new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dialog, int which) {
 							// TODO Auto-generated method stub
 							names.remove(arg2);
 							updateList();
 						}
 					})
 					.setNegativeButton("否", null);
 				AlertDialog ad=alert.create();
 				ad.show();
 				//Intent intent=new Intent();
 				//intent.setClass(context, WhenFail.class);
 				//context.startActivity(intent);
 				
 				return true;
 			}
 		});
 	}
 	
 }
 
