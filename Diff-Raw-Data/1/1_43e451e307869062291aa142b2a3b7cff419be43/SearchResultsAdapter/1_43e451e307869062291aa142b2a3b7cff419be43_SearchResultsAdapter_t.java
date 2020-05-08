 package net.taviscaron.airliners.adapters;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import net.taviscaron.airliners.R;
 import net.taviscaron.airliners.data.ImageLoader;
 import net.taviscaron.airliners.model.AircraftSearchResult;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * SearchResults table adapter
  * @author Andrei Senchuk
  */
 public class SearchResultsAdapter extends BaseAdapter {
     protected ImageLoader imageLoader;
     protected List<AircraftSearchResult> results;
     protected Context context;
     protected SearchResultsAdapterListener listener;
 
     public interface SearchResultsAdapterListener {
         public void searchResultItemThumbClicked(AircraftSearchResult result, int position);
     }
 
     public SearchResultsAdapter(Context context, SearchResultsAdapterListener listener) {
         this.context = context.getApplicationContext();
         this.results = new ArrayList<AircraftSearchResult>();
         this.imageLoader = new ImageLoader(context, ImageLoader.THUMB_CACHE_TAG);
         this.listener = listener;
     }
 
     @Override
     public int getCount() {
         return results.size();
     }
 
     @Override
     public AircraftSearchResult getItem(int position) {
         return results.get(position);
     }
 
     @Override
     public long getItemId(int position) {
         return position;
     }
 
     @Override
     public View getView(int position, View view, ViewGroup parent) {
 
         if(view == null) {
             view = LayoutInflater.from(context).inflate(R.layout.aircraft_search_result_item, parent, false);
 
             ViewHolder holder = new ViewHolder();
             holder.imageLoadingProgressBar = (ProgressBar)view.findViewById(R.id.aircraft_search_result_item_image_progress);
             holder.imageView = (ImageView)view.findViewById(R.id.aircraft_search_result_item_image);
             holder.aircraftLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_aircraft);
             holder.airlineLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_airline);
             holder.regLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_registration);
             holder.authorLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_author);
             holder.placeLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_place);
             holder.countryDateLabel = (TextView)view.findViewById(R.id.aircraft_search_result_item_country_date);
 
             view.setTag(holder);
         }
 
         final ViewHolder holder = (ViewHolder)view.getTag();
         final int finalPosition = position;
         final AircraftSearchResult result = getItem(position);
 
         holder.imageLoadingProgressBar.setVisibility(View.GONE);
 
         // attributes
         updateTextViewValue(holder.aircraftLabel, result.getAircraft());
         updateTextViewValue(holder.airlineLabel, result.getAirline());
         updateTextViewValue(holder.regLabel, result.fullReg());
         updateTextViewValue(holder.authorLabel, result.getAuthor());
         updateTextViewValue(holder.placeLabel, result.getPlace());
 
         if(result.getCountry() != null && result.getDate() != null) {
             updateTextViewValue(holder.countryDateLabel, String.format("%s, %s", result.getCountry(), result.getDate()));
         } else {
             updateTextViewValue(holder.countryDateLabel, null);
         }
 
         // thumb
         holder.imageView.setOnClickListener(null);
        holder.imageView.setImageResource(android.R.color.transparent);
         imageLoader.loadImage(result.getThumbUrl(), new ImageLoader.ImageLoaderCallback() {
             @Override
             public void imageLoaded(ImageLoader loader, String url, Bitmap bitmap, String imageCachePath) {
                 holder.imageLoadingProgressBar.setVisibility(View.GONE);
                 holder.imageView.setImageBitmap(bitmap);
                 holder.imageView.setVisibility(View.VISIBLE);
                 holder.imageView.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         listener.searchResultItemThumbClicked(result, finalPosition);
                     }
                 });
             }
 
             @Override
             public void imageLoadFailed(ImageLoader loader, String url) {
                 holder.imageLoadingProgressBar.setVisibility(View.GONE);
             }
 
             @Override
             public void imageLoadStarted(ImageLoader loader, String url) {
                 holder.imageLoadingProgressBar.setVisibility(View.VISIBLE);
                 holder.imageView.setVisibility(View.GONE);
             }
         });
 
         return view;
     }
 
     private void updateTextViewValue(TextView textView, String value) {
         if(value == null || value.isEmpty()) {
             textView.setVisibility(View.GONE);
         } else {
             textView.setVisibility(View.VISIBLE);
             textView.setText(value);
         }
     }
 
     public void addAll(List<AircraftSearchResult> newResults) {
         results.addAll(newResults);
         notifyDataSetChanged();
     }
 
     private static class ViewHolder {
         ProgressBar imageLoadingProgressBar;
         ImageView imageView;
         TextView aircraftLabel;
         TextView airlineLabel;
         TextView regLabel;
         TextView authorLabel;
         TextView placeLabel;
         TextView countryDateLabel;
     }
 }
