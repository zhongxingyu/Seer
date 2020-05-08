 package org.fourdnest.androidclient.ui;
 
 import java.util.List;
 
 import org.fourdnest.androidclient.FourDNestApplication;
 import org.fourdnest.androidclient.Nest;
 import org.fourdnest.androidclient.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import android.widget.TextView;
 
 /**
  * Activity that places a button on the top of the screen. The button displays
  * the name of the current nest. Pressing the button sends the user back to
  * ListStreamActivity.
  */
 public abstract class NestSpecificActivity extends Activity {
 
 	protected ViewGroup contentView;
 	private FourDNestApplication application;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.application = ((FourDNestApplication) getApplication());
 		setContentView(R.layout.nest_specific_view);
 
 		inflateNestView();
 
 		this.contentView = (FrameLayout) findViewById(R.id.content_view);
 
 		LayoutInflater inflater = LayoutInflater.from(contentView.getContext());
 		View view = inflater.inflate(getLayoutId(), contentView, false);
 		contentView.addView(getContentLayout(view));
 	}
 
 	/** Called when the activity is resumed. */
 	@Override
 	protected void onResume() {
 		inflateNestView();
 		super.onResume();
 	}
 
 	/**
 	 * Initializes the main content view. The main content view is the entire
 	 * area under the Nest display.
 	 * 
 	 * @param view
 	 *            The ViewGroup where you want to display your functional
 	 *            content.
 	 */
 	public abstract View getContentLayout(View view);
 
 	/**
 	 * Returns the id of the layout definition that is used to fill the content
 	 * layout. Acquired with R.layout.your_layout_id.
 	 * 
 	 * @return An integer representing the id of the specified layout.
 	 */
 	public abstract int getLayoutId();
 
 	/**
 	 * Inflates the nest_view in nest_specific_view with the appropriate view,
 	 * depending on kiosk settings.
 	 */
 	private void inflateNestView() {
 
 		String nestName = this.application.getCurrentNest().getName();
 		Button nestButton;
 
 		FrameLayout nestView = (FrameLayout) findViewById(R.id.nest_view);
 		nestView.removeAllViews();
 		LayoutInflater inflater = LayoutInflater.from(nestView.getContext());
 		inflater.inflate(R.layout.nest_buttons_view, nestView);
 		nestButton = (Button) nestView.findViewById(R.id.nest_button);
 		nestButton.setText(nestName);
 
 		if (this.application.getKioskModeEnabled()) {
 			nestButton.setClickable(false);
 		} else {
 			setNestSpecificOnClickListener(nestButton);
 			List<Nest> nests = this.application.getNestManager().listNests();
 			Spinner nestSpinner = (Spinner) findViewById(R.id.nest_spinner);
 			if (nests.size() > 1) {
 				initializeNestSpinner(nestSpinner, nests);
				nestSpinner.setVisibility(View.VISIBLE);
 			} else {
 			}
 		}
 	}
 
 	private void setNestSpecificOnClickListener(Button nestButton) {
 		nestButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(),
 						ListStreamActivity.class);
 				v.getContext().startActivity(intent);
 				finish();
 			}
 		});
 	}
 
 	private void initializeNestSpinner(Spinner nestSpinner, List<Nest> nests) {
 		NestAdapter nestAdapter = new NestAdapter(nestSpinner, nests);
 		nestSpinner.setAdapter(nestAdapter);
 	}
 
 	/**
 	 * 
 	 * @author Chalise
 	 * 
 	 */
 	private class NestAdapter extends ArrayAdapter<Nest> {
 
 		private Spinner spinner;
 
 		public NestAdapter(Spinner spinner, List<Nest> objects) {
 			super(spinner.getContext(), R.layout.nest_spinner_element,
 					R.id.nest_name, objects);
 			this.spinner = spinner;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			convertView = findViewById(R.layout.nest_spinner_element);
 			parent = this.spinner;
 			if (convertView == null) {
 				LayoutInflater inflater = LayoutInflater.from(this.spinner
 						.getContext());
 				convertView = inflater.inflate(R.layout.nest_spinner_element,
 						parent, false);
 			}
 			TextView nameField = (TextView) convertView
 					.findViewById(R.id.nest_name);
 			nameField.setText(this.getItem(position).getName());
 
 			return convertView;
 		}
 	}
 
 }
