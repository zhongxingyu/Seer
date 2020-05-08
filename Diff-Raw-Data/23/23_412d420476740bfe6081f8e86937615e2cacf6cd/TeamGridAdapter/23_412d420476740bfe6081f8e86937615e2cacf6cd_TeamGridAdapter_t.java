 package in.ccl.adapters;
 
 import in.ccl.photo.PhotoView;
 import in.ccl.ui.R;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class TeamGridAdapter extends BaseAdapter {
 
 	private Context mContext;
 
 	private String[] teamLogo;
 
 	private Bitmap bmp;
 
 	private int reqImageHeight;
 
 	public TeamGridAdapter (Context ctx, String[] teamLogoUrls) {
 		mContext = ctx;
 		teamLogo = teamLogoUrls;
 
 		/*
 		 * Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay(); int height = display.getHeight(); reqImageHeight = (int) (((float) 15/ 100) * (height - 50));
 		 */
 
 	}
 
 	@Override
 	public int getCount () {
 		return teamLogo.length;
 	}
 
 	@Override
 	public Object getItem (int position) {
 		return null;
 	}
 
 	@Override
 	public long getItemId (int position) {
 		return 0;
 	}
 
 	@Override
 	public View getView (int position, View convertView, ViewGroup parent) {
 		ViewHolder mViewHolder;
 		if (convertView == null) {
 			mViewHolder = new ViewHolder();
 			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			convertView = (View) inflater.inflate(R.layout.team_logo_child, null);
 			mViewHolder.teamLogoImg = (PhotoView) convertView.findViewById(R.id.team_logo_img);
 			mViewHolder.errorTxt = (TextView) convertView.findViewById(R.id.error_title);
 			convertView.setTag(mViewHolder);
 		}
 		else {
 			mViewHolder = (ViewHolder) convertView.getTag();
 		}
 		// mViewHolder.teamLogoImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
 
 		// teamLogoImg.setBackgroundResource(teamLogo[position]);
 		if (mViewHolder.teamLogoImg != null) {
 
 			// mViewHolder.teamLogoImg.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, reqImageHeight));
 
 			// mViewHolder.teamLogoImg.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, reqImageHeight));
 
 			// mViewHolder.teamLogoImg.setScaleType(ImageView.ScaleType.MATRIX);
 
 			mViewHolder.teamLogoImg.setImageDrawable(mContext.getResources().getDrawable(R.drawable.imagenotqueued));
 			mViewHolder.teamLogoImg.setTag(teamLogo[position]);
 
 			mViewHolder.teamLogoImg.setImageURL(teamLogo[position], true, mContext.getResources().getDrawable(R.drawable.imagenotqueued), mViewHolder.errorTxt, false);
 		}
 		return convertView;
 	}
 
 	public class ViewHolder {
 
 		public PhotoView teamLogoImg;
 
 		public TextView errorTxt;
 	}
 }
