 
 package org.tsinghua.omedia.ui.fileBrowser;
 
 import java.util.ArrayList;
 
 import org.tsinghua.omedia.R;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * @author carelife
  */
 public class FileInfoAdapter extends BaseAdapter {
     private LayoutInflater layoutInflater; // 用于从xml文件加载listview的layout
 
     private Context ctx; // 容器／activity
 
     private ArrayList<String> simpleNameList;;
 
     public ArrayList<FileInfoDataSet> fileInfoList; // 所有item
 
     public FileInfoAdapter(Context ctx, ArrayList<FileInfoDataSet> list) {
         this.ctx = ctx;
         fileInfoList = list;
         layoutInflater = LayoutInflater.from(ctx);
         if (fileInfoList != null) {
             simpleNameList = new ArrayList<String>();
             for (int i = 0; i < fileInfoList.size(); i++) {
                if (fileInfoList.get(i).getFileName().length() > 12) {
                    Log.d("CCN_DEBUG", fileInfoList.get(i).getFileName().substring(0, 9)
                            + "   Length: "
                            + fileInfoList.get(i).getFileName().substring(0, 9).length());
                    simpleNameList.add(fileInfoList.get(i).getFileName().substring(0, 9) + "...");
                 } else {
                     simpleNameList.add(fileInfoList.get(i).getFileName());
                 }
             }
         }
     }
 
     @Override
     public int getCount() {
         return fileInfoList.size();
     }
 
     @Override
     public Object getItem(int position) {
         return fileInfoList.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         ViewHolder holder = null; // 清空临时变量
         if (convertView == null) { // 若行未初始化
             convertView = layoutInflater.inflate(R.layout.file_info, null); // 通过flater初始化行视图
             holder = new ViewHolder(); // 并将行视图的3个子视图引用放到tag中
             holder.itemIcon = (ImageView) convertView.findViewById(R.id.fileinfo_icon);
             holder.itemText = (TextView) convertView.findViewById(R.id.fileinfo_filename);
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
         FileInfoDataSet info = fileInfoList.get(position);
         if (info != null) {
             holder.itemText.setText(simpleNameList.get(position));
             Drawable draw = this.ctx.getResources().getDrawable(info.getImageId());
             holder.itemIcon.setImageDrawable(draw);
         }
         return convertView;
     }
 
     public FileInfoDataSet getFile(int position) {
         return fileInfoList.get(position);
     }
 
     private class ViewHolder {
         TextView itemText;
 
         ImageView itemIcon;
     }
 }
