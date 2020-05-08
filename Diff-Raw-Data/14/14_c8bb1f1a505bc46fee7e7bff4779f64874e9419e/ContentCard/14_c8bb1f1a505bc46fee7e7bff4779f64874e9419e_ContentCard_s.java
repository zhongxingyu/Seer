 package com.glasstunes.cards;
 
 import java.net.URISyntaxException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.glasstunes.R;
 import com.glasstunes.controller.MusicController;
 import com.glasstunes.screenslide.CardFragment;
 import com.squareup.picasso.Picasso;
 
 public class ContentCard extends CardFragment {
 
 	private static final String ARG_IMAGE_URI = "ARG_IMAGE_URI";
 	private static final String ARG_DISPLAY_NAME = "ARG_DISPLAY_NAME";
 	private static final String ARG_DISPLAY_DESCRIPTION = "ARG_DISPLAY_DESCRIPTION";
 	private static final String ARG_INTENT_URI = "ARG_INTENT_URI";
 
 	public static ContentCard newInstance(String image_uri,
 			String display_name, String display_description, String intent_uri) {
 		ContentCard frag = new ContentCard();
 		Bundle args = new Bundle();
 		args.putString(ARG_IMAGE_URI, image_uri);
 		args.putString(ARG_DISPLAY_NAME, display_name);
 		args.putString(ARG_DISPLAY_DESCRIPTION, display_description);
 		args.putString(ARG_INTENT_URI, intent_uri);
 		frag.setArguments(args);
 		return frag;
 	}
 
 	private Uri mImageUri;
 	private String mDisplayName;
 	private String mDisplayDescription;
 	private Intent mIntent;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle args = savedInstanceState == null ? getArguments()
 				: savedInstanceState;
 		mImageUri = Uri.parse(args.getString(ARG_IMAGE_URI));
 		mDisplayName = args.getString(ARG_DISPLAY_NAME);
 		mDisplayDescription = args.getString(ARG_DISPLAY_DESCRIPTION);
 		try {
 			mIntent = Intent.parseUri(args.getString(ARG_INTENT_URI), 0);
 		} catch (URISyntaxException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(ARG_IMAGE_URI, mImageUri.toString());
 		outState.putString(ARG_DISPLAY_NAME, mDisplayName);
 		outState.putString(ARG_DISPLAY_DESCRIPTION, mDisplayDescription);
 		outState.putString(ARG_INTENT_URI,
 				mIntent.toUri(Intent.URI_INTENT_SCHEME));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.frag_content_card, container, false);
 		Picasso.with(getActivity()).load(mImageUri)
 				.error(R.drawable.ic_album_art)
 				.placeholder(R.drawable.ic_album_art)
				.into((ImageView) v.findViewById(R.id.image));
		TextView name = (TextView) v.findViewById(R.id.name);
 		name.setText(mDisplayName);
		TextView description = (TextView) v.findViewById(R.id.description);
 		description.setText(mDisplayDescription);
 		if (TextUtils.isEmpty(mDisplayDescription)) {
 			name.setGravity(Gravity.CENTER_VERTICAL);
 			description.setVisibility(View.GONE);
 		}
 		return v;
 	}
 
 	@Override
 	public void onSelect() {
 		if ("com.google.android.music.xdi.intent.PLAY".equals(mIntent
 				.getAction())) {
 			MusicController.getInstance().play(getActivity(), mIntent);
 			getActivity().setResult(Activity.RESULT_OK);
 			getActivity().finish();
 		} else {
 			startActivityForResult(mIntent, 0);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		getActivity().setResult(resultCode);
 		if (resultCode == Activity.RESULT_OK) {
 			getActivity().finish();
 		}
 	}
 
 }
