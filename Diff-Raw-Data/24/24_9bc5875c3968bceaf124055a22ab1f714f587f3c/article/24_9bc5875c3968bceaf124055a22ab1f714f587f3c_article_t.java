 package nz.gen.wellington.guardian.android.activities;
 
 import nz.gen.wellington.guardian.android.R;
 import nz.gen.wellington.guardian.android.api.ImageDAO;
 import nz.gen.wellington.guardian.android.model.Article;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.TypedValue;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.ImageView.ScaleType;
 
 public class article extends ContentRenderingActivity {
 	
 	private MainImageUpdateHandler mainImageUpdateHandler;
 	private MainImageLoader mainImageLoader;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
     	mainImageUpdateHandler = new MainImageUpdateHandler();
 	}
 	
 	protected void populateContent(Article article, int bodytextColour, int headlineColour) {
 		setContentView(R.layout.article);	// TODO you could probably pull these two lines up
 		setFontSize();
 		
 		populateCommonContentFields(article, bodytextColour, headlineColour);	   
         populateArticleView(article, bodytextColour, headlineColour);
 	}
 
 	
 
 	private void populateArticleView(Article article, int bodytextColour, int headlineColour) {
 		TextView description = (TextView) findViewById(R.id.Description);
 		        
 		setFontSize();
 
         description.setTextColor(bodytextColour);        
 		description.setVisibility(View.VISIBLE);
 		if (article.isRedistributionAllowed()) {
 			description.setText(article.getDescription());
 		} else {
 			description.setText("Redistribution rights for this article are not available. "
 					+ "The full content cannot be downloaded but you should still be able to use the open in browser option to view the original article.");
 		}
 
 		final String mainImageUrl = article.getMainImageUrl();
 		if (mainImageUrl != null && (imageDAO.isAvailableLocally(mainImageUrl) || imageDownloadDecisionService.isOkToDownloadMainImages())) {
 			mainImageLoader = new MainImageLoader(imageDAO, article.getMainImageUrl());
 			Thread loader = new Thread(mainImageLoader);
 			loader.start();
 		}
 
 	}
 
 	
 	@Override
 	public void setFontSize() {
 		super.setFontSize();
 		
 		TextView caption = (TextView) findViewById(R.id.Caption);
 		caption.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseFontSize -2);		
 		caption.setTextColor(colourScheme.getBodytext());
 
 		TextView description = (TextView) findViewById(R.id.Description);
         description.setTextSize(TypedValue.COMPLEX_UNIT_PT, baseFontSize);        
 		description.setTextColor(colourScheme.getBodytext());
 	}
 	
 	
 	private void populateMainImage(String mainImageUrl) {
 		if (article != null && article.getMainImageUrl() != null && article.getMainImageUrl().equals(mainImageUrl)) {		
 			if (images.containsKey(mainImageUrl)) {		
 				Bitmap bitmap = images.get(mainImageUrl);
 				if (bitmap != null) {
 					populateMainImage(bitmap);
 				}
 			}
 		}
 	}
 
	
 	private void populateMainImage(Bitmap bitmap) {
 		ImageView imageView = (ImageView) findViewById(R.id.ArticleImage);
 		imageView.setVisibility(View.VISIBLE);
		imageView.setScaleType(ScaleType.FIT_START);

		final boolean isImageThinnerThanView = bitmap.getWidth() < imageView.getWidth();
		if (isImageThinnerThanView) {
			final boolean isImageLandScaped = bitmap.getWidth() > bitmap.getHeight();
			if (isImageLandScaped) {
				int scaledWidth = imageView.getWidth();
				float aspectRatio = new Float(bitmap.getWidth()) / new Float(bitmap.getHeight());
				int scaledHeight = Math.round(scaledWidth / aspectRatio);
				bitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
			}
 		}
		
		imageView.setImageBitmap(bitmap);					
 		populateCaption(article.getCaption());
 	}
	
 	private void populateCaption(String caption) {
 		if (caption != null && !caption.trim().equals("")) {
 			TextView captionView = (TextView) findViewById(R.id.Caption);
 			captionView.setVisibility(View.VISIBLE);
 			captionView.setText(caption);
 		}
 	}
 	
 	class MainImageLoader implements Runnable {		
 
 		private ImageDAO imageDAO;
 		private String mainImageUrl;
 		
 		public MainImageLoader(ImageDAO imageDAO, String mainImageUrl) {
 			this.imageDAO = imageDAO;
 			this.mainImageUrl = mainImageUrl;
 		}
 		
 		@Override
 		public void run() {
 			Bitmap image = imageDAO.getImage(mainImageUrl);
 			if (image != null) {
 				images.put(mainImageUrl, image);
 				sendMainImageAvailableMessage(mainImageUrl);
 			}
 			return;
 		}
 
 		private void sendMainImageAvailableMessage(String mainImageUrl) {
 			Message msg = new Message();
 			msg.what = MainImageUpdateHandler.MAIN_IMAGE_AVAILABLE;
 			msg.getData().putString("mainImageUrl", mainImageUrl);
 			mainImageUpdateHandler.sendMessage(msg);
 		}		
 	}
 	
 	class MainImageUpdateHandler extends Handler {
 		
 		private static final int MAIN_IMAGE_AVAILABLE = 1;
 
 		public void handleMessage(Message msg) {
 			super.handleMessage(msg);
 			
 			switch (msg.what) {	   
 			    case MAIN_IMAGE_AVAILABLE:
 			    final String mainImageUrl = msg.getData().getString("mainImageUrl");
 			    populateMainImage(mainImageUrl);
 			}
 		}
 	}
 		
 }
