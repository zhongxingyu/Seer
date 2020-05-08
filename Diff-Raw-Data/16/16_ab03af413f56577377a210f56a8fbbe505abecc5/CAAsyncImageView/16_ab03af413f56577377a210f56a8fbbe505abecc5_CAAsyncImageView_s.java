 package org.cocoa4android.util;
 
import java.net.URL;
 
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.extend.urlimage.UrlImageViewCallback;
 import org.cocoa4android.extend.urlimage.UrlImageViewHelper;
 import org.cocoa4android.ui.UIImage;
 import org.cocoa4android.ui.UIImageView;
 
 import android.graphics.drawable.Drawable;
 import android.widget.ImageView;
 
 public class CAAsyncImageView extends UIImageView implements UrlImageViewCallback{
 	private CAAsyncImageViewCallback imageViewCallback;
 	public CAAsyncImageView(UIImage image){
 		super(image);
 	}
 	public CAAsyncImageView(){
 		super();
 	}
 	public CAAsyncImageView(CGRect frame){
 		super(frame);
 	}
 	public void setImageUrl(String imageUrl){
 		if(this.image()!=null){
 			UrlImageViewHelper.setUrlDrawable(this.imageView, imageUrl, this.image().getDrawable(),this);
 		}else{
 			UrlImageViewHelper.setUrlDrawable(this.imageView, imageUrl,this);
 		}
 	}
 	@Override
 	public void onLoaded(ImageView imageView, Drawable loadedDrawable,
 			String url, boolean loadedFromCache) {
 		// TODO Auto-generated method stub
 		if(imageViewCallback!=null){
 			imageViewCallback.imageDidLoaded(this);
 		}
 	}
 	public CAAsyncImageViewCallback getImageViewCallback() {
 		return imageViewCallback;
 	}
 	public void setImageViewCallback(CAAsyncImageViewCallback imageViewCallback) {
 		this.imageViewCallback = imageViewCallback;
 	}
 	public interface CAAsyncImageViewCallback{
 		public void imageDidLoaded(CAAsyncImageView imageView);
 	}
 	
 }
