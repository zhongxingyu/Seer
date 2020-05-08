 package bu.edu.cs673.edukid.learn;
 
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import bu.edu.cs673.edukid.R;
 
 public class LearnContentViewFragment extends Fragment implements
 		OnClickListener, TextToSpeech.OnInitListener {
 
 	public static final String ARG_LEARN_TYPE = "LearnType";
 	public static final String ARG_IMAGE = "Image";
 	public static final String ARG_WORDS = "Words";
 	public static final String ARG_ITEM = "Item";
 	public static final String ARG_PHONETIC_SOUND = "PhoneticSound";
 
 	private TextToSpeech textToSpeech;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
		if (textToSpeech == null) {
			textToSpeech = new TextToSpeech(getActivity(), this);
		}
 
 		View view = inflater.inflate(R.layout.learn_content_fragment,
 				container, false);
 		Bundle arguments = getArguments();
 
 		// Set content image
 		ImageButton learnContentButton = (ImageButton) view
 				.findViewById(R.id.learnContentImage);
 		learnContentButton.setOnClickListener(this);
 		learnContentButton.setImageResource(arguments.getInt(ARG_IMAGE));
 
 		// Set content word
 		TextView learnContentWord = (TextView) view
 				.findViewById(R.id.learnContentWord);
 		learnContentWord.setOnClickListener(this);
 		String firstItemWord = arguments.getStringArray(ARG_WORDS)[0];
 		learnContentWord.setText(firstItemWord);
 
 		// Set content item
 		TextView learnContentItem = (TextView) view
 				.findViewById(R.id.learnContentItem);
 		learnContentItem.setOnClickListener(this);
 		learnContentItem.setText(arguments.getString(ARG_ITEM));
 
 		// Get the learn type
 		LearnType learnType = LearnType.values()[arguments
 				.getInt(ARG_LEARN_TYPE)];
 
 		if (learnType == LearnType.ALPHABET || learnType == LearnType.NUMBERS) {
 			learnContentItem.setTextSize(128);
 		}
 
 		return view;
 	}
 
 	@Override
 	public void onClick(View view) {
 		Bundle arguments = getArguments();
 		String text = "";
 
 		switch (view.getId()) {
 		case R.id.learnContentImage:
 		case R.id.learnContentWord:
 			// TODO: first one for now...
 			text = arguments.getStringArray(ARG_WORDS)[0];
 			break;
 		case R.id.learnContentItem:
 			text = arguments.getString(ARG_PHONETIC_SOUND);
 			break;
 		default:
 			return;
 		}
 
 		try {
 			textToSpeech.stop();
 			textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onInit(int status) {
 		textToSpeech.setLanguage(Locale.getDefault());
 	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (textToSpeech != null) {

			textToSpeech.stop();
			textToSpeech.shutdown();
		}
	}
 }
