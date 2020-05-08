 package com.alphadog.grapevine.activities;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.alphadog.grapevine.R;
 import com.alphadog.grapevine.db.GrapevineDatabase;
 import com.alphadog.grapevine.db.ReviewsTable;
 import com.alphadog.grapevine.models.Review;
 
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ReviewDetailsActivity extends Activity {
 
 	private GrapevineDatabase database;
 	private ReviewsTable reviewTable;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		database = new GrapevineDatabase(this);
 		reviewTable = new ReviewsTable(database);
 
		Long clickedReviewIndex = getIntent().getLongExtra("POS", 0);
 		Review review = reviewTable.findById(clickedReviewIndex + 1);
 		InputStream is;
 		try {
 			setContentView(R.layout.review_details);
 
 			ImageView reviewImage = (ImageView) findViewById(R.id.reviewImage);
 			TextView reviewText = (TextView) findViewById(R.id.reviewText);
 			reviewText.setText(review.getHeading());
 
 			String url = "http://t1.gstatic.com/images?q=tbn:ANd9GcTFlwiKsKy-IfJkF-zmUxKMa-uVxJkYZ2G4MmuRISBJaOKLofY&t=1&usg=__jiXjdZTLq_MTryETgiHOrBsTVjc=";
 			is = (InputStream) new URL(url).getContent();
 			reviewImage.setImageDrawable(Drawable.createFromStream(is,
 					"src name"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (database != null)
 			database.close();
 	}
 
 }
