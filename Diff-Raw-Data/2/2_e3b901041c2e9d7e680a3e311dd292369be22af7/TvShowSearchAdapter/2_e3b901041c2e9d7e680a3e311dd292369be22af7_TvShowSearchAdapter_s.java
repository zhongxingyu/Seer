 package fr.nf28.vmote.series.adapter;
 
 import java.util.List;
 
 import fr.nf28.vmote.R;
 import fr.nf28.vmote.tvdb.SearchSeries;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class TvShowSearchAdapter extends ArrayAdapter<SearchSeries> {
 	private final Context context;
 	private final List<SearchSeries> list;
 
 	public TvShowSearchAdapter(Context context, List<SearchSeries> list) {
 		super(context, R.layout.tvseries_add_list_cell, list);
 		this.context = context;
 		this.list = list;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.fragment_series_add_layout, parent, false);
 		
 		TextView seriesName = (TextView) rowView.findViewById(R.id.seriesName);
 		TextView seriesOverview = (TextView) rowView.findViewById(R.id.seriesOverview);
 		
 		seriesName.setText(list.get(position).getSeriesName());
 		seriesOverview.setText(list.get(position).getOverview());
 
 		return rowView;
 	}
 } 
