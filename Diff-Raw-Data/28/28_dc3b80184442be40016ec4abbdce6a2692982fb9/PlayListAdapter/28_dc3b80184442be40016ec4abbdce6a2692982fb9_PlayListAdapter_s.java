 package es.mrjon.androidmpd;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 import java.util.List;
 
 public class PlayListAdapter extends ArrayAdapter<MPDSongListItem> {
  private final Context context;
   private final List<MPDSongListItem> items;
 
   public PlayListAdapter(Context context, List<MPDSongListItem> items) {
     super(context, R.layout.playlist_row, items);
    this.context = context;
     this.items = items;
   }
 
   @Override
  public View getView(int position, View converterView, ViewGroup parent) {
    LayoutInflater inflater = (LayoutInflater) context
      .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
    View rowView = inflater.inflate(R.layout.playlist_row, parent, false);
     TextView titleText = (TextView) rowView.findViewById(R.id.song_title);
     titleText.setText(items.get(position).getSong().getTitle());
 
     TextView artistText = (TextView) rowView.findViewById(R.id.artist);
     artistText.setText(items.get(position).getSong().getArtist().toString());
 
     return rowView;
  }
  
 }
