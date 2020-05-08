 package com.example.skylark;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.util.EncodingUtils;
 
 import com.example.ui.MainActivity;
 import com.example.ui.MyAdapter;
 import com.example.ui.MyAdapter.ViewHolder;
 
 
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class DefineBlackList extends Activity {
 	Button saveDefine;
 	ListView defineBL;
 	ArrayList<Drawable> icons=new ArrayList<Drawable>();
 	//String names[]=new String[1000];
 	ArrayList<String> names=new ArrayList<String>();
 	//String packageNames[]=new String[1000];
 	ArrayList<String> packageNames=new ArrayList<String>();
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.define);
 		defineBL=(ListView)findViewById(R.id.defineBL);
 		saveDefine=(Button)findViewById(R.id.saveDefine);
 		saveDefine.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(!saveDefine())
 				{
 					return;
 				}
 				Intent intent=new Intent();
 				intent.setClass(DefineBlackList.this, MainActivity.class);
 				DefineBlackList.this.startActivity(intent);
				DefineBlackList.this.finish();
 			}
 			
 		});
 		
 		iniAppChoices();
 	}
 	
 	/*
 	 * 初始化应用列表，显示安装的应用的icon，name以及一个CheckBox
 	 */
 	private void iniAppChoices()
 	{
 		PackageManager pm=this.getPackageManager();
 		List<PackageInfo>pi=pm.getInstalledPackages(0);
 		
 		/*
 		 * 获取icon的list以及name的list，构造adapter
 		 */
 		for (PackageInfo p : pi) {
 			if(p.packageName.contains("android"))//除去系统应用
 			{
 				continue;
 			}
             //names[i]=p.applicationInfo.loadLabel(getPackageManager()).toString();
 			names.add(p.applicationInfo.loadLabel(getPackageManager()).toString());
             //packageNames[i]=p.packageName;
 			packageNames.add(p.packageName);
             //i++;
             icons.add(p.applicationInfo.loadIcon(getPackageManager()));
         }
 		
 		MyAdapter adapter=new MyAdapter(this, icons, names, true);
 		
 		defineBL.setAdapter(adapter);
 		//defineBL.setItemsCanFocus(true);
 		defineBL.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
 					long arg3) {
 				// TODO Auto-generated method stub
 				/*
 				 * 对于每一个被点击的item，设置isSelected属性，以便在save方法中获取多选的项目
 				 * 具体察看MyAdapter类
 				 */
 				ViewHolder viewHolder=(ViewHolder)arg1.getTag();
 				viewHolder.cb.toggle();
 				MyAdapter adapter=((MyAdapter)defineBL.getAdapter());
 				adapter.setIsSelected(arg2);
 				//Toast.makeText(DefineBlackList.this, ""+arg2, Toast.LENGTH_LONG).show();
 				//vHolder vh=(vHolder)arg1.getTag();
 				//vh.cb.setChecked(true);
 			}
 			
 		});
 	}
 	
 	/*
 	 * 保存选中的选项。包括黑名单，黑名单名称。
 	 * 主要设计文件操作
 	 */
 	private boolean saveDefine()
 	{
 		/*
 		 * 首先得到黑名单的名字，判断是否为临时（即为”“）
 		 * 然后得到已存在的黑名单的全部，判断需保存的黑名单中是否已经存在
 		 */
 		String blName=((EditText)findViewById(R.id.nameEdit)).getText().toString();
 		if(blName.equals(""))
 		{
 			blName="Default";
 		}
 		String blNames="";
 		FileInputStream fin;
 		try {
 			fin = openFileInput(getResources().getString(R.string.blListNames));
 			int length = fin.available();   
         byte [] buffer = new byte[length];   
         fin.read(buffer);       
         blNames = ""+EncodingUtils.getString(buffer, "UTF-8");
         if(blNames.contains(blName) && (! blName.equals("Default")))
         {
         	Toast t=new Toast(DefineBlackList.this);
         	t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
         	t=Toast.makeText(DefineBlackList.this, "黑名单"+blName+"已经存在,\n请重新输入黑名单名字", Toast.LENGTH_LONG);
         	t.show();
         	((EditText)findViewById(R.id.nameEdit)).setText("");
         	return false;
         }
         fin.close();  
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		/*
 		 * 创建黑名单文件，从adapter中获取被选项的信息，保存包名，以便以后查找
 		 */
 		try {
 			FileOutputStream fout=openFileOutput(blName+"bl",Context.MODE_PRIVATE);//用用户输入的黑名单名称建立文件
 			MyAdapter adapter=(MyAdapter)defineBL.getAdapter();
 			for(int i=0;i<adapter.getIsSelected().size();i++)
 			{
 				if(adapter.getIsSelected().get(i))
 				{
 					fout.write((packageNames.get(i)+" ").getBytes());
 					//Toast.makeText(DefineBlackList.this, packageNames[i], Toast.LENGTH_SHORT).show();
 				}
 			}
 			fout.close();
 			
 			/*
 			 * 保存黑名单名称到黑名单的大集合列表中
 			 */
 			fout=openFileOutput(getResources().getString(R.string.blListNames),Context.MODE_PRIVATE);
 			if(!blNames.contains("Default"))
 			{
 				blNames=blNames+blName+" ";
 			}
 			fout.write(blNames.getBytes());
			fout.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		//DefineBlackList.this.finish();
 		return true;
 	}
 }
