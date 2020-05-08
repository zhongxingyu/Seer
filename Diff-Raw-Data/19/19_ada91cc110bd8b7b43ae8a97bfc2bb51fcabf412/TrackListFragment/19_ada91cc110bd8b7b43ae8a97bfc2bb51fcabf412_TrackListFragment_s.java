 package controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import model.Track;
 import model.Tracks;
 
 import birdpro.app.R;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ScrollView;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class TrackListFragment extends Fragment implements PropertyChangeListener {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Tracks.getInstance().addPropertyChangeListener(this);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		ScrollView view = (ScrollView) inflater.inflate(R.layout.track_list_view, container, false);
 		TableLayout trackList = (TableLayout) view.findViewById(R.id.trackList);
 		view.setScrollbarFadingEnabled(true);
 		view.setFadingEdgeLength(50);
 		
 		ArrayList<Track> tracks = Tracks.getInstance().getTracks();
 		for(Track track : tracks){
 			trackList.addView(createViewForTrack(track, inflater));
 		}
 		
 		return view;
 	}
 
 	public void propertyChange(PropertyChangeEvent event) {
 		String propertyName = event.getPropertyName();		
 		if(propertyName == Tracks.PROPERTY_CHANGE_TRACK_CREATED){
 			TableLayout trackList = (TableLayout) getActivity().findViewById(R.id.trackList);
 			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			Log.i("", ((Track) event.getNewValue()).length + "");
			//trackList.addView(createViewForTrack( (Track) event.getNewValue(), inflater));
 		}
 	}
 	
 	private TableRow createViewForTrack(Track track, LayoutInflater inflater){
 		final long id = track.id;
 		
 		TableRow row = new TableRow(getActivity());
 		Button playButton = new Button(getActivity());
 		playButton.setBackgroundResource(R.drawable.play);
 		playButton.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				playRecording(id);
 			}
 		});
		//playButton.setLayoutParams(new LayoutParams(48, 48));
 		row.addView(playButton);
 		TextView date = new TextView(getActivity());
 		
 		String lol = track.date.get(Calendar.DATE) + " September " + track.date.get(Calendar.HOUR_OF_DAY) + ":" + track.date.get(Calendar.MINUTE); 
 		date.setText(lol);
 		row.addView(date);
 		
 		return row;
 	}
 	
 	private void playRecording(long id){
		
 	}
 }
