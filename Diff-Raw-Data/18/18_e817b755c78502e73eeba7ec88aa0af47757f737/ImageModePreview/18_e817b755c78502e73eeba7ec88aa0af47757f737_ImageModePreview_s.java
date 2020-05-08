 package steve4448.livetextbackground.widget;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.AttributeSet;
 import android.view.View;
 
 public class ImageModePreview extends View {
 	public static enum ImageMode {
 		CENTER,
 		FILL,
 		FIT,
 		STRECH,
 		TILE
 	};
 	
 	private String imageLocation;
 	private Bitmap image;
 	private Rect srcRect;
 	private Rect dstRect;
 	private Paint painter;
 	private ImageMode currentMode;
 	
 	public ImageModePreview(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		init(context, attrs, 0);
 	}
 	
 	public ImageModePreview(Context context, AttributeSet attrs, int defStyle) {
 		super(context, attrs, defStyle);
 		init(context, attrs, defStyle);
 	}
 	
 	public void init(Context context, AttributeSet attrs, int defStyle) {
 		painter = new Paint();
 		srcRect = new Rect(0, 0, 0, 0);
 		dstRect = new Rect(0, 0, 0, 0);
 		currentMode = ImageMode.CENTER;
 	}
 	
 	public void setImageLocation(String imageLocation) {
 		this.imageLocation = imageLocation;
 		try {
 	        image = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), Uri.parse(imageLocation));
 	        srcRect = new Rect(0, 0, image.getWidth(), image.getHeight());
         } catch(Exception e) {
 	        e.printStackTrace();
         }
 		invalidate();
 	}
 	
 	public String getImageLocation() {
 		return imageLocation;
 	}
 	
 	public void setImageMode(String mode) {
 		setImageMode(ImageMode.valueOf(mode.toUpperCase(getResources().getConfiguration().locale)));
 	}
 	
 	public void setImageMode(ImageMode mode) {
 		currentMode = mode;
 		//TODO: 
 		switch(currentMode) {
 			case CENTER:
				srcRect.offsetTo(image.getWidth() / 2, image.getHeight() / 2);
 			break;
 			case FILL:
 			break;
 			case FIT:
 			break;
 			case STRECH:
 				srcRect.set(0, 0, image.getWidth(), image.getHeight());
 			break;
 			case TILE:
 			break;
 		}
 		invalidate();
 	}
 	
 	public ImageMode getImageMode() {
 		return currentMode;
 	}
 	
 	@Override
 	public void onDraw(Canvas canvas) {
 		super.onDraw(canvas);
		dstRect.set(0, 0, getWidth(), getHeight());
 		if(image == null)
 				;//TODO: Draw an image of which indicates the image failed to load?
 		else {
 				canvas.drawBitmap(image, srcRect, dstRect, painter);
 		}
 	}
 }
