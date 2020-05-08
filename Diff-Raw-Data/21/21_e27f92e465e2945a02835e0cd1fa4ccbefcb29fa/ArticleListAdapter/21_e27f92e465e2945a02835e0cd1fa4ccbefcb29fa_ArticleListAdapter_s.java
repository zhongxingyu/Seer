 package edu.grinnell.sandb;
 
 import java.util.List;
 
 import com.nostra13.universalimageloader.core.DisplayImageOptions;
 import com.nostra13.universalimageloader.core.ImageLoader;
 import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
 import com.nostra13.universalimageloader.core.assist.ImageScaleType;
 import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
 
 import android.graphics.Bitmap;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.view.Display;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AnimationUtils;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 import edu.grinnell.grinnellsandb.R;
 import edu.grinnell.sandb.data.Article;
 import edu.grinnell.sandb.data.ImageTable;
 import edu.grinnell.sandb.img.DbImageGetter;
 import edu.grinnell.sandb.img.DbImageGetter.DbImageGetterAsyncTask;
 
 public class ArticleListAdapter extends ArrayAdapter<Article> {
 	private MainActivity mActivity;
 	private List<Article> mData;
 	private DbImageGetter mImageGetter;
 	protected ImageLoader imageLoader = ImageLoader.getInstance();
 
 
 	public ArticleListAdapter(MainActivity a, int layoutId, List<Article> data) {
 		super(a, layoutId, data);
 		mActivity = a;
 		mData = data;
 
 		Display display = a.getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.x / 3;
 		int height = (int) (width * 0.80);
 		mImageGetter = new DbImageGetter(mActivity, width, height);
 	}
 
 	private class ViewHolder {
 		TextView title;
 		TextView description;
 		// TextView date;
 		ImageView image;
 
		DbImageGetterAsyncTask imgTask;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 
 		ViewHolder holder;
 
 		if (convertView == null) {
 			LayoutInflater li = mActivity.getLayoutInflater();
 			convertView = li.inflate(R.layout.articles_row, parent, false);
 			holder = new ViewHolder();
 			holder.title = (TextView) convertView.findViewById(R.id.titleText);
 			holder.description = (TextView) convertView
 					.findViewById(R.id.descriptionText);
 			holder.image = (ImageView) convertView
 					.findViewById(R.id.articleThumb);
 			convertView.setTag(holder);
 		} else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 
		if (holder.imgTask != null)
			holder.imgTask.cancel(false);
 
 		final Article a = mData.get(position);
 
 		if (a != null) {
 			getArticleImage(a, holder.image);
 			holder.image.setImageResource(R.drawable.loading);
 			holder.image.startAnimation(AnimationUtils.loadAnimation(mActivity,
 					R.anim.loading));
 			holder.title.setText(a.getTitle());
 			holder.description.setText(a.getDescription());
 			holder.title.setPadding(3, 3, 3, 3);
 		}
 
 		return convertView;
 	}
 
 	public void getArticleImage(Article a, ImageView imgView) {

 		ImageTable imgTable = new ImageTable(imgView.getContext());
 		imgTable.open();
 
 		int id = a.getId();
 		String[] URLS = imgTable.findURLSbyArticleId(id);
 		imgTable.close();
 		
 		if (URLS != null) {
 			String imgUrl = URLS[0];
 		
 		DisplayImageOptions options;
 
 		options = new DisplayImageOptions.Builder()
 				// change these images to error messages
 				.showImageForEmptyUri(R.drawable.sandblogo)
 				.showImageOnFail(R.drawable.sandblogo).resetViewBeforeLoading()
 				.cacheOnDisc().imageScaleType(ImageScaleType.EXACTLY)
 				.bitmapConfig(Bitmap.Config.RGB_565)
 				.displayer(new FadeInBitmapDisplayer(300)).build();
 
 		ImageLoaderConfiguration configuration = ImageLoaderConfiguration
 				.createDefault(imgView.getContext().getApplicationContext());
 		imageLoader.init(configuration);
		imageLoader.displayImage(imgUrl, imgView, options, null);
 		}
 	}
 }
