 package hk.com.dycx.iptv.adapter;
 
 import hk.com.dycx.iptv.R;
 import hk.com.dycx.iptv.bean.VideoInfo;
 
 import java.util.List;
 
 import android.content.Context;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 
 /**
  * @author zhangping e-mail:zp@dycx.com.hk
  * @date 2013-3-12 下午4:37:26
  * @version 1.0
  */
 public class VideoInfoAdapterTwo extends BaseAdapter {
 	/** 上下文 */
 	private Context mContext;
 	
 	/** 显示的视频 */
 	private List<VideoInfo> mVideoInfos;
 	
 	/** 子view 的 id */
 	private int mItem;
 
 	public VideoInfoAdapterTwo(Context context, List<VideoInfo> infos, int item){
 		mContext = context;
 		mVideoInfos = infos;
 		mItem = item;
 	}
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		if (mVideoInfos == null ) {
 			return 0;
 		}
 		return mVideoInfos.size();
 	}
 
 	@Override
 	public Object getItem(int position) {
 		return mVideoInfos.get(position);
 	}
 
 	@Override
 	public long getItemId(int position) {
 		return position;
 	}
 	
 	public void setVideoInfos(List<VideoInfo> infos){
 		mVideoInfos = infos;
 		notifyDataSetChanged();
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		VideoInfo sVideoInfo = mVideoInfos.get(position);
 		ViewHolder holder;
 		if (convertView == null) {
 			holder = new ViewHolder();
 			convertView = View.inflate(mContext, mItem, null);
 			holder.video_name = (TextView) convertView.findViewById(R.id.ibtn_main_gridview_item_video_name);
 			convertView.setTag(holder);
 		}else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 		bindView(sVideoInfo, holder, position);
 		return convertView;
 	}
 	
 	private void bindView(VideoInfo sVideoInfo, ViewHolder holder, int position) {
		int index = 10001 + position;
 		String valueOf = String.valueOf(index);
 		String substring = valueOf.substring(valueOf.length() - 3);
 		holder.video_name.setText(substring + "     " + sVideoInfo.getName());
 	}
 	
 	private static final class ViewHolder {
 		TextView video_name;
 	}
 }
