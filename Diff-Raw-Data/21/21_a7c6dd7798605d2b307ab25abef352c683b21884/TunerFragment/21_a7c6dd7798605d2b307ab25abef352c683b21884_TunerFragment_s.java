 /*
 Musicians Assistant
     Copyright (C) 2012-2014  Zdeněk Janeček <jan.zdenek@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package kiv.janecekz.ma;
 
 import kiv.janecekz.ma.common.Recorder;
 import kiv.janecekz.ma.common.SharedData;
 import kiv.janecekz.ma.prefs.SharedPref;
 import kiv.janecekz.ma.tuner.AnalyzerAMDF;
 import kiv.janecekz.ma.tuner.AnalyzerWave;
 import kiv.janecekz.ma.tuner.Classificator;
 import kiv.janecekz.ma.tuner.Classificator.Result;
 import kiv.janecekz.ma.tuner.MedianFilter;
 import android.annotation.SuppressLint;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.widget.ImageView;
 import android.widget.NumberPicker;
 import android.widget.NumberPicker.OnValueChangeListener;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 @SuppressLint("NewApi")
 public class TunerFragment extends Fragment implements IControlable,
 		Informable, OnValueChangeListener {
 	private static final int METHOD_AMDF = 0;
 	private static final int METHOD_WAVE = 1;
 
 	private Recorder recorder;
 	private SharedData sd;
 	private AsyncTask<Void, Double, Void> analyzer;
 	private Classificator classify;
 	private MedianFilter mf;
 
 	private ImageView circle;
 	private TextView tunerText;
 	private ProgressBar leftBar;
 	private ProgressBar rightBar;
 	private AlphaAnimation inAnim;
 	private AlphaAnimation outAnim;
 	private NumberPicker refFreq;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View v = inflater.inflate(R.layout.tuner, container, false);
 		v.setOnTouchListener(TouchControl.getInstance());
 
 		refFreq = (NumberPicker) v.findViewById(R.id.refFreq);
		refFreq.setMinValue(435);
		refFreq.setMaxValue(444);
 		refFreq.setOnValueChangedListener(this);
 		refFreq.setValue(SharedPref.getBaseFreq(getActivity()));
 
 		circle = (ImageView) v.findViewById(R.id.circle);
 		tunerText = (TextView) v.findViewById(R.id.tuner_text);
 
 		leftBar = (ProgressBar) v.findViewById(R.id.progressBarLeft);
 		rightBar = (ProgressBar) v.findViewById(R.id.progressBarRight);
 
 		inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
 		outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
 
 		return v;
 	}
 
 	@Override
 	public void onPause() {
 		analyzer.cancel(true);
 		recorder.stopRecording();
 
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		getView().setBackgroundResource(
 				((MainActivity) getActivity()).getBgRes());
 
 		sd = new SharedData(500);
 		recorder = new Recorder(sd);
 		recorder.start();
 
 		int method = SharedPref.getAnlMethod(getActivity()
 				.getApplicationContext());
 
 		if (method == METHOD_AMDF)
 			analyzer = new AnalyzerAMDF(sd, recorder, this);
 		else if (method == METHOD_WAVE)
 			analyzer = new AnalyzerWave(sd, recorder, this);
 
 		analyzer.execute();
 
 		classify = new Classificator(SharedPref.getBaseFreq(getActivity()
 				.getApplicationContext()));
 
 		mf = new MedianFilter();
 	}
 
 	@Override
 	public void onValueChange(TouchControl t, int val) {
 
 	}
 
 	@Override
 	public void onToggle(TouchControl t, int state) {
 		switch (state) {
 		case TouchControl.STATE_BEGIN:
 			circle.setVisibility(View.VISIBLE);
 			circle.startAnimation(inAnim);
 			break;
 		case TouchControl.STATE_TOGGLE:
 			// not used
 
 			break;
 		case TouchControl.STATE_OUT:
 			if (!inAnim.hasEnded())
 				return;
 			circle.startAnimation(outAnim);
 			circle.setVisibility(View.INVISIBLE);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onPositionChange(TouchControl t, float x, float y) {
 			circle.setX(x - circle.getWidth() / 2);
 			circle.setY(y - circle.getHeight() / 2 - 80);
 	}
 
 	public void postInformation(Double freq) {
 		mf.addValue(classify.findTone(freq));
 		Result r = mf.getMedian();
 
		tunerText.setText(String.format("%s  %f", r.getTone(), r.getFreq()));
 
 		if (r.getError() >= 0) {
 			leftBar.setProgress(0);
 			rightBar.setProgress((int) Math.floor(100 * r.getError()));
 		} else {
 			leftBar.setProgress((int) Math.floor(-100 * r.getError()));
 			rightBar.setProgress(0);
 		}
 	}
 
 	@Override
 	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
 		classify.changeRef(newVal);
 	}
 }
