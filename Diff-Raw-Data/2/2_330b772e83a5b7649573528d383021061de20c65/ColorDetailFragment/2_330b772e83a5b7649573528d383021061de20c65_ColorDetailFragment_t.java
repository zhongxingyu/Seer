 package com.agilevent.colorssample;
 
 import android.app.Fragment;
 import android.view.View;
 import android.widget.Toast;
 
 public class ColorDetailFragment extends Fragment implements ColorChangedListener {
 
 	protected View layout; 
 	
 	@Override
 	public View onCreateView(android.view.LayoutInflater inflater, android.view.ViewGroup container, android.os.Bundle savedInstanceState) {
 		layout = inflater.inflate(R.layout.color_detail, container);
 		return layout; 
 	};
 	
 	@Override
 	public void onColorChanged(SimpleColor color) {
 		layout.setBackgroundColor(color.getColor());
		Toast.makeText(getActivity(), "Background color changed to: " + color.getName(), Toast.LENGTH_SHORT).show();
 	}
 
 }
