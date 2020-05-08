 package com.glasstunes.screenslide;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.glasstunes.R;
 
 public class BasicCardFragment extends CardFragment {
 	protected static final String ARG_ICON_RES = "ARG_ICON_RES";
 	protected static final String ARG_LABEL = "ARG_LABEL";
 	protected static final String ARG_LABEL_RES = "ARG_LABEL_RES";
 
 	public static BasicCardFragment newInstance(int iconRes, CharSequence label) {
 		BasicCardFragment frag = new BasicCardFragment();
 		Bundle args = generateArgs(iconRes, label);
 		frag.setArguments(args);
 		return frag;
 	}
 
 	protected static Bundle generateArgs(int iconRes, CharSequence label) {
 		Bundle args = new Bundle();
 		args.putInt(ARG_ICON_RES, iconRes);
 		args.putCharSequence(ARG_LABEL, label);
 		return args;
 	}
 
 	public static BasicCardFragment newInstance(int iconRes, int labelRes) {
 		BasicCardFragment frag = new BasicCardFragment();
 		Bundle args = generateArgs(iconRes, labelRes);
 		frag.setArguments(args);
 		return frag;
 	}
 
 	protected static Bundle generateArgs(int iconRes, int labelRes) {
 		Bundle args = new Bundle();
 		args.putInt(ARG_ICON_RES, iconRes);
 		args.putInt(ARG_LABEL_RES, labelRes);
 		return args;
 	}
 
 	protected int mIconRes;
 	protected int mLabelRes;
 	protected CharSequence mLabel;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		Bundle state = savedInstanceState == null ? getArguments()
 				: savedInstanceState;
 		mIconRes = state.getInt(ARG_ICON_RES);
 		mLabelRes = state.getInt(ARG_LABEL_RES);
 		mLabel = state.getCharSequence(ARG_LABEL);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.frag_basic_card, container, false);
 		refreshText(v);
 		refreshImage(v);
 		return v;
 	}
 
 	protected void refreshImage(View v) {
 		if (v != null) {
			ImageView imageView = (ImageView) v.findViewById(R.id.album_art);
 			imageView.setVisibility(mIconRes > 0 ? View.VISIBLE : View.GONE);
 			imageView.setImageResource(mIconRes);
 		}
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putInt(ARG_ICON_RES, mIconRes);
 		outState.putInt(ARG_LABEL_RES, mLabelRes);
 		outState.putCharSequence(ARG_LABEL, mLabel);
 	}
 
 	public int getIconRes() {
 		return mIconRes;
 	}
 
 	public void setIconRes(int iconRes) {
 		mIconRes = iconRes;
 		refreshImage(getView());
 	}
 
 	public int getLabelRes() {
 		return mLabelRes;
 	}
 
 	public void setLabelRes(int labelRes) {
 		mLabelRes = labelRes;
 		refreshText(getView());
 	}
 
 	public CharSequence getLabel() {
 		return mLabel;
 	}
 
 	public void setLabel(CharSequence label) {
 		mLabel = label;
 		refreshText(getView());
 	}
 
 	@Override
 	public void onSelect() {
 
 	}
 
 	protected void refreshText(View v) {
 		if (v == null) {
 			return;
 		}
 		TextView text = (TextView) v.findViewById(R.id.text);
 		if (mLabel != null) {
 			text.setText(mLabel);
 		} else if (mLabelRes > 0) {
 			text.setText(mLabelRes);
 		}
 	}
 }
