 package com.fawna.dumbo;
 
 import android.app.ListFragment;
 import android.content.Intent;
 import android.database.DataSetObserver;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.*;
 
 import java.io.InputStream;
 import java.util.*;
 
 public class TranscriptFragment extends ListFragment {
   private Handler handler;
   private Timer timer;
 
 
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     super.onCreateView(inflater, container, savedInstanceState);
     return inflater.inflate(R.layout.cardsview, container, false);
   }
 
   @Override
   public void onActivityCreated(Bundle savedInstancestate) {
     super.onActivityCreated(savedInstancestate);
     handler = new Handler();
     timer = new Timer();
     TranscriptAdapter adapter = new TranscriptAdapter();
     setListAdapter(adapter);
     adapter.generateQuotes();
     getListView().setBackgroundColor(Color.rgb(196, 196, 196));
     getListView().setDividerHeight(0);
     getListView().setDivider(null);
 
     populateStatusBar(getView().findViewById(R.id.fixed_header));
     scheduleClock((TextView) getView().findViewById(R.id.current_time), System.currentTimeMillis() - CardsFragment.movieInfo.time * 1000);
     getView().findViewById(R.id.status_drop_shadow).setVisibility(View.VISIBLE);
 
   }
 
   private void scheduleClock(final TextView view, final long time) {
     timer.schedule(new TimerTask() {
       @Override
       public void run() {
         handler.post(new Runnable() {
           @Override
           public void run() {
             long current = (System.currentTimeMillis() - time) / 1000;
             String time = "" + String.format("%d", current / 60) + ":" + String.format("%02d", current % 60);
             view.setText(time);
           }
         });
       }
     }, 1000, 1000);
 
   }
 
 
   private void populateStatusBar(final View statusBar) {
     final boolean isHIMYM = CardsFragment.movieInfo.imdb.contains("tt1777828");
 
     // create the imdb button handler
     ImageButton imdbButton = (ImageButton) statusBar.findViewById(R.id.imdb_button);
     imdbButton.setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         Intent openImdb = new Intent(Intent.ACTION_VIEW);
        openImdb.setData(Uri.parse(isHIMYM ? "http://www.imdb.com/title/tt1777828/" : "http://www.imdb.com/title/tt0584441/"));
         startActivity(openImdb);
       }
     });
 
     // set the cover photo to himym if necessary
     if (isHIMYM) {
       TextView totalTime = (TextView) statusBar.findViewById(R.id.total_time);
       totalTime.setText("of 21:03");
       setTypeface(totalTime, "fonts/avenir_light.otf");
 
       TextView episode = (TextView) statusBar.findViewById(R.id.episode);
       episode.setText("Se. 6 Ep. 10");
       setTypeface(episode, "fonts/avenir_light.otf");
     }
   }
 
   private void onNewQuote() {
      ListView list = getListView();
     if (list.getLastVisiblePosition() >= getListAdapter().getCount() - 8) {
       list.smoothScrollBy(2000, 2000);
     }
   }
 
   private View generateQuoteCard(final String character, final String line, final String photoUrl, final String actor) {
     View quote = getActivity().getLayoutInflater().inflate(R.layout.quote, null);
 
     TextView charView = (TextView)quote.findViewById(R.id.quote_actor);
     charView.setText(character);
     setTypeface(charView, "fonts/avenir_heavy.otf");
 
     TextView lineView = (TextView) quote.findViewById(R.id.quote_line);
     lineView.setText(line);
 
     UrlImageLoader.loadImage((ImageView) quote.findViewById(R.id.quote_photo), photoUrl, 80);
 
     return quote;
   }
 
 
 
   public class TranscriptAdapter implements ListAdapter {
 
     final private List<View> lines;
     private DataSetObserver list;
 
     public TranscriptAdapter() {
       lines = new ArrayList<View>();
     }
 
     public void generateQuotes() {
       MovieInfo movieInfo = CardsFragment.movieInfo;
       for (MovieEvent event: movieInfo.events) {
         if (event.type.equals(MovieEvent.TYPE_ACTOR) && event.time >= movieInfo.time) {
           AddQuoteEvent addActorEvent = new AddQuoteEvent(event, lines, list);
           long fireTime = (event.time - movieInfo.time) * 1000;
           timer.schedule(addActorEvent, Math.max(0, fireTime));
         }
       }
     }
 
     @Override
     public boolean areAllItemsEnabled() {
       return true;
     }
 
     @Override
     public boolean isEnabled(int position) {
       return true;
     }
 
     @Override
     public void registerDataSetObserver(DataSetObserver observer) {
       list = observer;
     }
 
     @Override
     public void unregisterDataSetObserver(DataSetObserver observer) {
       observer = null;
     }
 
     @Override
     public int getCount() {
       return lines.size();
     }
 
     @Override
     public Object getItem(int position) {
       return null;
     }
 
     @Override
     public long getItemId(int position) {
       return position;
     }
 
     @Override
     public boolean hasStableIds() {
       return false;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
       View v = lines.get(position);
       if (position == lines.size() - 1) {
         Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
         v.startAnimation(animation);
       }
       return v;
     }
 
     @Override
     public int getItemViewType(int position) {
       return position;
     }
 
     @Override
     public int getViewTypeCount() {
       return Math.max(1, lines.size());
     }
 
     @Override
     public boolean isEmpty() {
       return false;
     }
   }
 
   public class AddQuoteEvent extends TimerTask {
       final MovieEvent event;
       final List<View> cards;
       final DataSetObserver observer;
 
       public AddQuoteEvent(MovieEvent event, final List<View> cards, final DataSetObserver observer) {
         this.event = event;
         this.cards = cards;
         this.observer = observer;
       }
 
       @Override
       public void run() {
         handler.post(new Runnable() {
           @Override
           public void run() {
             cards.add(generateQuoteCard(event.role_name, event.text, event.actor_picture, event.actor_name));
             observer.onChanged();
             onNewQuote();
           }
         });
       }
   }
 
   private void setTypeface(TextView tv, String face) {
     Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), face);
     tv.setTypeface(tf);
   }
 
   @Override
   public void onDestroy() {
     super.onDestroy();
     timer.cancel();
     timer.purge();
   }
 }
