 package tv.pps.tj.ppsdemo.ui;
 
 import android.content.Context;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.xengine.android.data.cache.DefaultDataRepo;
 import com.xengine.android.data.cache.XDataChangeListener;
 import com.xengine.android.media.image.loader.XScrollRemoteLoader;
 import com.xengine.android.media.image.processor.XImageProcessor;
 import com.xengine.android.utils.XLog;
 import tv.pps.tj.ppsdemo.R;
 import tv.pps.tj.ppsdemo.data.cache.ProgramBaseSource;
 import tv.pps.tj.ppsdemo.data.cache.SourceName;
 import tv.pps.tj.ppsdemo.data.model.ProgramBase;
 import tv.pps.tj.ppsdemo.engine.MyImageScrollRemoteLoader;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tujun
  * Date: 13-7-17
  * Time: 上午9:19
  * To change this template use File | Settings | File Templates.
  */
 public class AdapterProgramListView extends BaseAdapter
         implements AbsListView.OnScrollListener, XDataChangeListener<ProgramBase> {
     private static final String TAG = AdapterProgramListView.class.getSimpleName();
 
     private Context mContext;
     private ProgramBaseSource mProgramBaseSource;
     // 图片加载器
     private XScrollRemoteLoader mImageScrollLoader;
 
     public AdapterProgramListView(Context context) {
         mContext = context;
         mProgramBaseSource = (ProgramBaseSource) DefaultDataRepo.
                 getInstance().getSource(SourceName.PROGRAM_BASE);
         mImageScrollLoader = MyImageScrollRemoteLoader.getInstance();
         mImageScrollLoader.setWorking();
     }
 
     @Override
     public int getCount() {
         return mProgramBaseSource.size();
     }
 
     @Override
     public Object getItem(int i) {
         return mProgramBaseSource.get(i);
     }
 
     @Override
     public long getItemId(int i) {
         return i;
     }
 
     private class ViewHolder {
         public View frame;
         public ImageView programImageView;
         public ImageView programRankView;
         public TextView programNameView;
         public TextView programTypeView;
         public TextView onlineNumberView;
         public TextView programScoreView;
     }
     @Override
     public View getView(int i, View convertView, ViewGroup viewGroup) {
         ViewHolder holder = null;
         if (convertView == null) {
             convertView = View.inflate(mContext, R.layout.program_listview_item, null);
             holder = new ViewHolder();
             holder.frame = convertView.findViewById(R.id.item_frame);
             holder.programImageView = (ImageView) convertView.findViewById(R.id.program_img);
             holder.programRankView = (ImageView) convertView.findViewById(R.id.program_rank);
             holder.programNameView = (TextView) convertView.findViewById(R.id.program_name);
             holder.programTypeView = (TextView) convertView.findViewById(R.id.program_type);
             holder.onlineNumberView = (TextView) convertView.findViewById(R.id.online_number);
             holder.programScoreView = (TextView) convertView.findViewById(R.id.program_score);
             convertView.setTag(holder);
         } else {
             holder = (ViewHolder) convertView.getTag();
         }
 
         ProgramBase programBase = (ProgramBase) getItem(i);
         switch (i) {
             case 0:
                 holder.programRankView.setVisibility(View.VISIBLE);
                 holder.programRankView.setImageResource(R.drawable.ic_ss_number1);
                 break;
             case 1:
                 holder.programRankView.setVisibility(View.VISIBLE);
                 holder.programRankView.setImageResource(R.drawable.ic_ss_number2);
                 break;
             case 2:
                 holder.programRankView.setVisibility(View.VISIBLE);
                 holder.programRankView.setImageResource(R.drawable.ic_ss_number3);
                 break;
             default:
                 holder.programRankView.setVisibility(View.GONE);
                 break;
         }
         holder.programNameView.setText(programBase.getName());
         holder.programTypeView.setText(programBase.getType());
         holder.onlineNumberView.setText("" + programBase.getOnlineNumber());
         float score = programBase.getVote();
         holder.programScoreView.setText("" + score);
         if (score > 9) {
             holder.programScoreView.setTextColor(mContext.getResources().getColor(R.color.red));
         } else if (score > 6) {
             holder.programScoreView.setTextColor(mContext.getResources().getColor(R.color.dark_orange));
         } else {
             holder.programScoreView.setTextColor(mContext.getResources().getColor(R.color.orange));
         }
 
         XLog.d(TAG, "getView()" + holder.programImageView + " asyncoLoadBitmap. index=" + i);
         // 异步加载图片
         mImageScrollLoader.asyncLoadBitmap(mContext, programBase.getPosterUrl(),
                 holder.programImageView, XImageProcessor.ImageSize.SCREEN, null);
 
         return convertView;
     }
 
     @Override
     public void onScrollStateChanged(AbsListView view, int scrollState) {
         switch (scrollState) {
             case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                 XLog.d(TAG, "SCROLL_STATE_FLING");
                 mImageScrollLoader.onScroll();
                 break;
             case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                 XLog.d(TAG, "SCROLL_STATE_IDLE");
                 mImageScrollLoader.onIdle();
                 break;
             case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                 XLog.d(TAG, "SCROLL_STATE_TOUCH_SCROLL");
                 mImageScrollLoader.onIdle();// 手指触摸滑动时也加载
                 break;
             default:
                 break;
         }
     }
     @Override
     public void onScroll(AbsListView view, int firstVisibleItem,
                          int visibleItemCount, int totalItemCount) {
     }
 
     @Override
     public void onChange() {
         postNotifyDataChange();
     }
 
     @Override
     public void onAdd(ProgramBase cityDetailInfo) {
         postNotifyDataChange();
     }
 
     @Override
     public void onAddAll(List<ProgramBase> cityDetailInfos) {
         postNotifyDataChange();
     }
 
     @Override
     public void onDelete(ProgramBase cityDetailInfo) {
         postNotifyDataChange();
     }
 
     @Override
     public void onDeleteAll(List<ProgramBase> cityDetailInfos) {
         postNotifyDataChange();
     }
 
 
     private void postNotifyDataChange() {
         changeHandler.sendEmptyMessage(0);
     }
 
     private Handler changeHandler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             if (msg.what == 0) {
                XLog.d(TAG, "postNotifyDataChange received. ListView");
                 mImageScrollLoader.onIdle();
                 notifyDataSetChanged();
             }
         }
     };
 }
