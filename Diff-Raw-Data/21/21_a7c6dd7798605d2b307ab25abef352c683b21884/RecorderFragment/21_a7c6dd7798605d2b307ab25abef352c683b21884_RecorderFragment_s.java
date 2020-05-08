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
 
 import java.io.File;
 
 import kiv.janecekz.ma.common.Recorder;
 import kiv.janecekz.ma.common.SharedData;
 import kiv.janecekz.ma.rec.WavWriter;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.AlphaAnimation;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class RecorderFragment extends Fragment implements IControlable,
 		Informable {
 	private final String[] dots = { " .", " . .", " . . ." };
 
 	private ImageView circle;
 	private AlphaAnimation inAnim;
 	private AlphaAnimation outAnim;
 	private WavWriter wav;
 	private Recorder r;
 	private SharedData sd;
 	// private TextView recTitleText;
 	private TextView recStatusText;
 	private File lastRecorded;
 	private Handler mHandler = new Handler();
 	private int dotCounter = 0;
 
 	private Runnable mUpdateTimeTask = new Runnable() {
 		@Override
 		public void run() {
 			String dot = dots[dotCounter];
 
 			recStatusText.setText(getResources().getText(R.string.recording)
 					+ dot);
 			dotCounter = (dotCounter + 1) % dots.length;
 			mHandler.postDelayed(this, 1000);
 		}
 	};
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		View v = inflater.inflate(R.layout.recorder, container, false);
 		v.setOnTouchListener(TouchControl.getInstance());
 
 		circle = (ImageView) v.findViewById(R.id.circle);
 
 		inAnim = TouchControl.getAnimation(TouchControl.ANIMATION_IN);
 		outAnim = TouchControl.getAnimation(TouchControl.ANIMATION_OUT);
 
 		recStatusText = (TextView) v.findViewById(R.id.rec_status);
 		// recTitleText = (TextView) v.findViewById(R.id.rec_title);
 
 		return v;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 
 		if (r != null && r.isRecording()) {
 			r.stopRecording();
 			try {
 				wav.join();
 				r.join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		mHandler.removeCallbacks(mUpdateTimeTask);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		getView().setBackgroundResource(
 				((MainActivity) getActivity()).getBgRes());
 
 		if (r != null && r.isRecording())
 			mHandler.postDelayed(mUpdateTimeTask, 1000);
 	}
 
 	@Override
 	public void onValueChange(TouchControl t, int val) {
 		// Not used
 	}
 
 	@Override
 	public void onToggle(TouchControl t, int state) {
 		switch (state) {
 		case TouchControl.STATE_BEGIN:
 			circle.setVisibility(View.VISIBLE);
 			circle.startAnimation(inAnim);
 			break;
 		case TouchControl.STATE_TOGGLE:
 			if (r == null || !r.isRecording()) {
 				mHandler.postDelayed(mUpdateTimeTask, 1000);
				sd = new SharedData(8092);
 				r = new Recorder(sd);
 				r.start();
 
 				lastRecorded = getNextFile();
 				wav = new WavWriter(sd, r, lastRecorded);
 				wav.start();
 			} else {
 				r.stopRecording();
 				try {
 					wav.join();
 					r.join();
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				mHandler.removeCallbacks(mUpdateTimeTask);
 				recStatusText.setText("");
 				Toast.makeText(getActivity(),getResources().getString(R.string.recorded) +
 						" " + lastRecorded.getName(),
 						Toast.LENGTH_SHORT).show();
 				;
 			}
 			break;
 		case TouchControl.STATE_OUT:
 			if (!inAnim.hasEnded())
 				inAnim.cancel();
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
 
 	@Override
 	public void postInformation(Double inf) {
 		// will be used for amplitude view
 	}
 
 	private File getNextFile() {
 		String extension = ".wav";
 		File f;
 
 		int i = 1;
 		while (true) {
 			f = new File(Environment.getExternalStorageDirectory().getPath()
 					+ "/rec" + (i++) + extension);
 
 			if (!f.exists())
 				break;
 		}
 		return f;
 	}
 }
