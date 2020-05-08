 package de.juliai.bavariaweather;
 
 import android.graphics.Color;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.util.TypedValue;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 import de.juliai.bavariaweather.WeatherData.WeatherText;
 
 /**
  * 
  * @author JuliaI
  * 
  */
 public class RegionFragment extends Fragment {
 
 	private static final String ARG_DATA = "weather_data";
 
 	private String serializedWeatherData;
 
 	/**
 	 * factory method
 	 * 
 	 * @param serializedWeatherData
 	 * @return
 	 */
 	public static final RegionFragment newInstance(
 			final String serializedWeatherData) {
 		final RegionFragment regionFragment = new RegionFragment();
 
 		Bundle args = new Bundle();
 		args.putString(ARG_DATA, serializedWeatherData);
 		regionFragment.setArguments(args);
 
 		return regionFragment;
 	}
 
 	/**
 	 * empty public constructor as required
 	 */
 	public RegionFragment() {
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		this.serializedWeatherData =getArguments().getString(ARG_DATA) ;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		final View view = inflater.inflate(R.layout.region_page_layout,
 				container, false);
 
 		loadDataIntoView(view);
 
 		return view;
 	}
 
 	/**
 	 * @param newWeatherData
 	 */
 	public void changeWeatherData(final String serializedWeatherData) {
 
 		this.serializedWeatherData = serializedWeatherData;
 		getArguments().putString(ARG_DATA, serializedWeatherData);
 
 		final View view = this.getView();
 		if (view != null) {
 			loadDataIntoView(view);
 		}
 	}
 
 	private static final int CREST_WIDTH = 100;
 
 	private static final int CREST_HEIGHT = 133;
 
 	private static final int PAD_TOP = 20;
 
 	private static final int PAD_LEFT = 60;
 
 	private static final int PAD_RIGHT = 60;
 
 	private static final int PAD_BOTTOM_TEXT = 15;
 
 	private static final int PAD_BOTTOM_HEAD = 5;
 
 	private static final LayoutParams RULER_LAYOUT = new LayoutParams(
 			LayoutParams.MATCH_PARENT, 1);
 	static {
 		RULER_LAYOUT.topMargin = 0;
 		RULER_LAYOUT.bottomMargin = 10;
 		RULER_LAYOUT.leftMargin = 40;
 		RULER_LAYOUT.rightMargin = 40;
 	}
 
 	private static final int RULER_COLOR = Color.rgb(196, 196, 196);
 
 	/**
 	 * @param view
 	 */
 	private void loadDataIntoView(final View view) {
 		final WeatherData weatherData = WeatherDataSerializer
 				.deserialize(this.serializedWeatherData);
 		if (weatherData == null) {
 			return;
 		}
 
 		final FragmentActivity activity = getActivity();
 
 		// main title
 		final TextView textTitle = (TextView) view.findViewById(R.id.title);
 		textTitle.setText(weatherData.getTitle());
 
 		final int identifier = getResources().getIdentifier(
 				weatherData.getCrestDrawableName(), null,
 				activity.getPackageName());
 		final Drawable crest = getResources().getDrawable(identifier);
 		crest.setBounds(0, 0, CREST_WIDTH, CREST_HEIGHT);
 		textTitle.setCompoundDrawables(null, crest, null, null);
 
 		textTitle.setPadding(PAD_LEFT, PAD_TOP, PAD_RIGHT, PAD_BOTTOM_TEXT);
 
 		// first horizontal ruler
 		final View firstRuler = view.findViewById(R.id.firstRuler);
 		firstRuler.setBackgroundColor(RULER_COLOR);
 		firstRuler.setLayoutParams(RULER_LAYOUT);
 
 		// subtitles
 		final TextView textSubtitle1 = (TextView) view
 				.findViewById(R.id.subtitle1);
 		textSubtitle1.setPadding(PAD_LEFT, 0, PAD_RIGHT, 0);
 		textSubtitle1.setText(weatherData.getSubtitle1());
 
		final String subtitle2 = weatherData.getSubtitle2();
		final boolean subtitle2Exists = StringUtils.isNotEmpty(subtitle2);

		int paddingBottom = 0;
		if (!subtitle2Exists) {
			paddingBottom = PAD_BOTTOM_TEXT;
		}
 		final TextView textTimestamp = (TextView) view
 				.findViewById(R.id.timestamp);
 		textTimestamp.setPadding(PAD_LEFT, 0, PAD_RIGHT, 0);
 		textTimestamp.setText(weatherData.getTimestamp());
 
		if (subtitle2Exists) {
 			final TextView textSubtitle2 = (TextView) view
 					.findViewById(R.id.subtitle2);
 			textSubtitle2.setPadding(PAD_LEFT, 0, PAD_RIGHT, PAD_BOTTOM_TEXT);
 			textSubtitle2.setText(subtitle2);
 		}
 
 		// dynamic texts
 		if (weatherData.getWeatherTexts() != null) {
 			final LinearLayout textContainer = (LinearLayout) view
 					.findViewById(R.id.dynamicTextContainer);
 			textContainer.removeAllViews();
 
 			for (final WeatherText text : weatherData.getWeatherTexts()) {
 
 				final View horizontalLine = new View(activity);
 				horizontalLine.setBackgroundColor(RULER_COLOR);
 				textContainer.addView(horizontalLine, RULER_LAYOUT);
 
 				final String head = text.getHead();
 				if (StringUtils.isNotEmpty(head)) {
 					final TextView headText = new TextView(activity);
 					headText.setText(head);
 					headText.setTypeface(Typeface
 							.defaultFromStyle(Typeface.BOLD));
 					headText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
 					headText.setPadding(PAD_LEFT, 0, PAD_RIGHT, PAD_BOTTOM_HEAD);
 					textContainer.addView(headText);
 				}
 
 				final TextView textText = new TextView(activity);
 				textText.setText(text.getText());
 				textText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
 				textText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
 				textText.setPadding(PAD_LEFT, 0, PAD_RIGHT, PAD_BOTTOM_TEXT);
 				textContainer.addView(textText);
 			}
 		}
 	}
 }
