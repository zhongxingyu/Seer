 package org.cocoa4android.util;
 
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.extend.urlimage.UrlImageButtonCallback;
 import org.cocoa4android.extend.urlimage.UrlImageButtonHelper;
 import org.cocoa4android.ui.UIButton;
 
 import android.graphics.drawable.Drawable;
 import android.widget.Button;
 
 public class CAAsyncImageButton extends UIButton implements UrlImageButtonCallback{
 	private CAAsyncImageButtonCallback imageButtonCallback;
 	
	private CAAsyncImageView imageView;
	
 	public CAAsyncImageButton(){
 		super();
 	}
 	public CAAsyncImageButton(CGRect frame){
 		this();
 		this.setFrame(frame);
 	}
 	public void setImageUrl(String imageUrl){
 		if(this.getButton().getBackground()!=null){
 			UrlImageButtonHelper.setUrlDrawable(this.button, imageUrl, this.button.getBackground(),this);
 		}else{
 			UrlImageButtonHelper.setUrlDrawable(this.button, imageUrl,this);
 		}
 	}
 	
 	public CAAsyncImageButtonCallback getImageButtonCallback() {
 		return imageButtonCallback;
 	}
 	public void setImageButtonCallback(CAAsyncImageButtonCallback imageButtonCallback) {
 		this.imageButtonCallback = imageButtonCallback;
 	}
 
 	public interface CAAsyncImageButtonCallback{
 		public void imageDidLoaded(CAAsyncImageButton imageButton);
 	}
 
 	@Override
 	public void onLoaded(Button button, Drawable loadedDrawable, String url,
 			boolean loadedFromCache) {
 		if(imageButtonCallback!=null){
 			imageButtonCallback.imageDidLoaded(this);
 		}
 		
 	}
 }
