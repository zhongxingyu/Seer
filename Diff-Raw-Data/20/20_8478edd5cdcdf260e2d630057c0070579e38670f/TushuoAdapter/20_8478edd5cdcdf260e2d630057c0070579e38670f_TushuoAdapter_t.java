 /*
  * 版    权：深圳市快播科技有限公司
  * 描    述: 
  * 创建人: HuQiming
  * 创建时间: 2013-7-18
  * 
  */
 package com.dreaner.penti.core.ui;
 
 import java.util.List;
 
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.dreaner.penti.R;
 import com.dreaner.penti.core.api.result.TushuoPartItem;
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
 
 /**
  * @author HuQiming
  * @date 2013-7-18
  * 
  */
 public class TushuoAdapter extends BaseAdapter {
	private static final String TAG = "TushuoAdapter";
 	private LayoutInflater mInflater;
 	private List<TushuoPartItem> mData;
 	private ImageLoader mImageLoader = ImageLoader.getInstance();
 	private DisplayImageOptions mOptions;
 
 	public TushuoAdapter(LayoutInflater inflater) {
 		mInflater = inflater;
 
 		mOptions = new DisplayImageOptions.Builder()
 		.showStubImage(R.drawable.ic_launcher)
 		.showImageForEmptyUri(R.drawable.ic_launcher)
 		.showImageOnFail(R.drawable.ic_launcher)
 		.cacheInMemory()
 		.cacheOnDisc()
 		.displayer(new SimpleBitmapDisplayer())
 		.build();
 	}
 
 	public void setData(List<TushuoPartItem> data) {
 		mData = data;
 	}
 
 	@Override
 	public int getCount() {
 		return mData != null ? mData.size() : 0;
 	}
 
 	@Override
 	public TushuoPartItem getItem(int position) {
 		return mData != null && position <= mData.size() ? mData.get(position) : null;
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View view = convertView;
 		final ViewHolder holder;
 		if (convertView == null) {
			view = mInflater.inflate(R.layout.item_list_image, null);
 			holder = new ViewHolder();
 			holder.text = (TextView) view.findViewById(R.id.text);
 			holder.image = (ImageView) view.findViewById(R.id.image);
 			view.setTag(holder);
 		} else {
 			holder = (ViewHolder) view.getTag();
 		}
 
 		TushuoPartItem model = getItem(position);
 		if (model != null) {
 			holder.text.setText(model.getText());
 			if (model.getImage() != null && model.getImage().length() > 0) {
				Log.d(TAG, "load bitmap: " + model.getImage() + " pos: " + position);
 				mImageLoader.displayImage(model.getImage(), holder.image, mOptions);
			}else{
				holder.image.setImageBitmap(null);
 			}
 		}
 		return view;
 	}
 
 	private class ViewHolder {
 		public TextView text;
 		public ImageView image;
 	}
 
 }
