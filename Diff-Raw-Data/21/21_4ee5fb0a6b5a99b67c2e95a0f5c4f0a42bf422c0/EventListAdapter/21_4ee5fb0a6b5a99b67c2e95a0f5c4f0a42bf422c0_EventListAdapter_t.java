 package uk.ac.dur.duchess;
 
 import java.util.ArrayList;
 
 import uk.ac.dur.duchess.data.CalendarFunctions;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class EventListAdapter extends ArrayAdapter<Event>
 {
 	private ArrayList<Event> events;
 	private Context context;
 	private int rowLayoutResourceID;
 
 	public EventListAdapter(Context context, int rowLayoutResourceID, ArrayList<Event> events)
 	{
 		super(context, rowLayoutResourceID, events);
 		this.events = events;
 		this.context = context;
 		this.rowLayoutResourceID = rowLayoutResourceID;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent)
 	{
 		View v = convertView;
 		if (v == null)
 		{
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			v = inflater.inflate(rowLayoutResourceID, parent, false);
 		}
 
 		Event e = events.get(position);
 
 		if (e != null)
 		{
 			TextView txtEventName = (TextView) v.findViewById(R.id.txtEventName);
 			TextView txtEventDescription = (TextView) v.findViewById(R.id.txtEventDescription);
 			TextView txtEventDate = (TextView) v.findViewById(R.id.txtEventDate);
 
 			if (txtEventName != null)
 			{
 				txtEventName.setText(e.getName());
 			}
 			if (txtEventDescription != null)
 			{
 				txtEventDescription.setText(e.getDescriptionHeader());
 			}
 			if (txtEventDate != null)
 			{
 				txtEventDate.setText(e.getAddress1() + "\n" + CalendarFunctions.getEventDate(e));
 			}
 
 			ImageView star1 = (ImageView) v.findViewById(R.id.newStar1);
 			ImageView star2 = (ImageView) v.findViewById(R.id.newStar2);
 			ImageView star3 = (ImageView) v.findViewById(R.id.newStar3);
 			ImageView star4 = (ImageView) v.findViewById(R.id.newStar4);
 			ImageView star5 = (ImageView) v.findViewById(R.id.newStar5);
 			TextView numberOfReviewsDisplay = (TextView) v.findViewById(R.id.numberOfReviewsOnList);
 			
 			int numberOfReviews = e.getNumberOfReviews();
 			
 			if (numberOfReviews > 0)
 			{
 				numberOfReviewsDisplay.setText("based on " + numberOfReviews + " review" + ((numberOfReviews != 1) ? "s" : ""));
 				int rating = e.getReviewScore();
 				Bitmap emptyStar = BitmapFactory.decodeResource(
 						((Activity) context).getResources(), R.drawable.empty_star);
 				Bitmap halfStar = BitmapFactory.decodeResource(((Activity) context).getResources(),
 						R.drawable.half_star);
 				Bitmap fullStar = BitmapFactory.decodeResource(((Activity) context).getResources(),
 						R.drawable.full_star);
 				
 				star1.setVisibility(View.VISIBLE);
 				star2.setVisibility(View.VISIBLE);
 				star3.setVisibility(View.VISIBLE);
 				star4.setVisibility(View.VISIBLE);
 				star5.setVisibility(View.VISIBLE);
 				numberOfReviewsDisplay.setVisibility(View.VISIBLE);
 				
 				if (rating == 10)
 				{
 					star5.setImageBitmap(fullStar);
 					star4.setImageBitmap(fullStar);
 					star3.setImageBitmap(fullStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 9)
 				{
 					star5.setImageBitmap(halfStar);
 					star4.setImageBitmap(fullStar);
 					star3.setImageBitmap(fullStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 8)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(fullStar);
 					star3.setImageBitmap(fullStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 7)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(halfStar);
 					star3.setImageBitmap(fullStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 6)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(fullStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 5)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(halfStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 4)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(emptyStar);
 					star2.setImageBitmap(fullStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 3)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(emptyStar);
 					star2.setImageBitmap(halfStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 2)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(emptyStar);
 					star2.setImageBitmap(emptyStar);
 					star1.setImageBitmap(fullStar);
 				}
 				else if (rating == 1)
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(emptyStar);
 					star2.setImageBitmap(emptyStar);
 					star1.setImageBitmap(halfStar);
 				}
 				else
 				{
 					star5.setImageBitmap(emptyStar);
 					star4.setImageBitmap(emptyStar);
 					star3.setImageBitmap(emptyStar);
 					star2.setImageBitmap(emptyStar);
 					star1.setImageBitmap(emptyStar);
 				}
 			}
 			else
 			{
				star1.setVisibility(View.GONE);
				star2.setVisibility(View.GONE);
				star3.setVisibility(View.GONE);
				star4.setVisibility(View.GONE);
				star5.setVisibility(View.GONE);
				numberOfReviewsDisplay.setVisibility(View.GONE);
 			}
 
 		}
 		return v;
 	}
 }
