 package fr.utc.assos.uvweb;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import fr.utc.assos.uvweb.data.UVwebContent;
 
 import java.util.Collections;
 import java.util.List;
 
 /**
 *
  */
 public class UVAdapter extends BaseAdapter {
 
     private static List<UVwebContent.UV> mUVs = Collections.emptyList();
     private Context mContext;
 
     public UVAdapter(Context context) {
         this.mContext = context;
     }
 
     @Override
     public int getCount() {
         return mUVs.size();
     }
 
     @Override
     public UVwebContent.UV getItem(int position) {
         return mUVs.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     public void updateUVs(List<UVwebContent.UV> UVs) {
         ThreadPreconditions.checkOnMainThread();
         mUVs = UVs;
         notifyDataSetChanged();
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
 
         if (convertView == null) {
             convertView = View.inflate(mContext, R.layout.uv, null);
         }
 
         TextView letterCodeView = UVHolder.get(convertView, R.id.lettercode);
         TextView numberCodeView = UVHolder.get(convertView, R.id.numbercode);
         TextView rateView = UVHolder.get(convertView, R.id.rate);
         TextView descView = UVHolder.get(convertView, R.id.desc);
 
         UVwebContent.UV UV = getItem(position);
         letterCodeView.setText(UV.getLetterCode());
         numberCodeView.setText(UV.getNumberCode());
         descView.setText(UV.getDescription());
         rateView.setText(UV.getRate()+"/10");
 
         return convertView;
     }
 }
