 /**
  *  Catroid: An on-device visual programming system for Android devices
  *  Copyright (C) 2010-2012 The Catrobat Team
  *  (<http://developer.catrobat.org/credits>)
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Affero General Public License as
  *  published by the Free Software Foundation, either version 3 of the
  *  License, or (at your option) any later version.
  *
  *  An additional term exception under section 7 of the GNU Affero
  *  General Public License, version 3, is available at
  *  http://www.catroid.org/catroid/licenseadditionalterm
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU Affero General Public License for more details.
  *
  *  You should have received a copy of the GNU Affero General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.catrobat.paintroid.dialog;
 
 import java.util.ArrayList;
 
 import org.catrobat.paintroid.R;
 
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Paint;
 import android.graphics.Paint.Cap;
 import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 
 public final class BrushPickerDialog extends DialogFragment implements
 		OnClickListener, DialogInterface.OnClickListener {
 
 	private static final String NOT_INITIALIZED_ERROR_MESSAGE = "BrushPickerDialog has not been initialized. Call init() first!";
 
 	private static BrushPickerDialog instance;
 
 	public interface OnBrushChangedListener {
 		public void setCap(Cap cap);
 
 		public void setStroke(int stroke);
 	}
 
 	public class OnBrushChangedWidthSeekBarListener implements
 			SeekBar.OnSeekBarChangeListener {
 
 		@Override
 		public void onProgressChanged(SeekBar seekBar, int progress,
 				boolean fromUser) {
			if (progress < MIN_BRUSH_SIZE) {
				progress = MIN_BRUSH_SIZE;
 			}
			updateStrokeChange(progress);

 			mBrushSizeText.setText("" + progress);
 		}
 
 		@Override
 		public void onStartTrackingTouch(SeekBar seekBar) {
 		}
 
 		@Override
 		public void onStopTrackingTouch(SeekBar seekBar) {
 		}
 	}
 
 	private ArrayList<OnBrushChangedListener> mBrushChangedListener;
 	private Paint mCurrentPaint;
 	// private ImageView mPreviewBrushImageView;
 	private Context mContext;
 	private TextView mBrushSizeText;
 	private SeekBar mBrushWidthSeekBar;
 	private RadioButton mRbtnCitcle;
 	private RadioButton mRbtnRect;
 	private static final int MIN_BRUSH_SIZE = 1;
 
 	private BrushPickerDialog(Context context) {
 
 		mBrushChangedListener = new ArrayList<BrushPickerDialog.OnBrushChangedListener>();
 		mContext = context;
 	}
 
 	public static BrushPickerDialog getInstance() {
 		if (instance == null) {
 			throw new IllegalStateException(NOT_INITIALIZED_ERROR_MESSAGE);
 		}
 		return instance;
 	}
 
 	public static void init(Context context) {
 		instance = new BrushPickerDialog(context);
 	}
 
 	public void setCurrentPaint(Paint currentPaint) {
 		mCurrentPaint = currentPaint;
 		updateStrokeCap(currentPaint.getStrokeCap());
 		updateStrokeChange((int) currentPaint.getStrokeWidth());
 	}
 
 	public void addBrushChangedListener(OnBrushChangedListener listener) {
 		mBrushChangedListener.add(listener);
 	}
 
 	public void removeBrushChangedListener(OnBrushChangedListener listener) {
 		mBrushChangedListener.remove(listener);
 	}
 
 	private void updateStrokeChange(int strokeWidth) {
 		for (OnBrushChangedListener listener : mBrushChangedListener) {
 			listener.setStroke(strokeWidth);
 		}
 	}
 
 	private void updateStrokeCap(Cap cap) {
 		for (OnBrushChangedListener listener : mBrushChangedListener) {
 			listener.setCap(cap);
 		}
 	}
 
 	@TargetApi(11)
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		LayoutInflater inflator = getActivity().getLayoutInflater();
 		AlertDialog.Builder builder;
 		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
 			builder = new AlertDialog.Builder(mContext);
 
 		} else {
 			builder = new AlertDialog.Builder(mContext,
 					AlertDialog.THEME_HOLO_DARK);
 		}
 		builder.setTitle(R.string.stroke_title);
 		View view = inflator.inflate(R.layout.dialog_stroke, null);
 
 		ImageButton btn_circle = (ImageButton) view
 				.findViewById(R.id.stroke_ibtn_circle);
 		btn_circle.setOnClickListener(this);
 
 		ImageButton btn_rect = (ImageButton) view
 				.findViewById(R.id.stroke_ibtn_rect);
 		btn_rect.setOnClickListener(this);
 
 		mRbtnCitcle = (RadioButton) view.findViewById(R.id.stroke_rbtn_circle);
 		mRbtnCitcle.setOnClickListener(this);
 
 		mRbtnRect = (RadioButton) view.findViewById(R.id.stroke_rbtn_rect);
 		mRbtnRect.setOnClickListener(this);
 
 		mBrushWidthSeekBar = (SeekBar) view
 				.findViewById(R.id.stroke_width_seek_bar);
 
 		mBrushWidthSeekBar
 				.setOnSeekBarChangeListener(new OnBrushChangedWidthSeekBarListener());
 
 		mBrushSizeText = (TextView) view
 				.findViewById(R.id.stroke_width_width_text);
 
 		builder.setView(view);
 		builder.setNeutralButton(R.string.done, this);
 
 		return builder.create();
 
 	}
 
 	// private void initComponents() {
 	// setContentView(R.layout.dialog_stroke);
 	// setTitle(R.string.stroke_title);
 	// setCanceledOnTouchOutside(true);
 	// setCancelable(true);
 	//
 	// Button btn_cancel = (Button) findViewById(R.id.stroke_btn_Cancel);
 	// btn_cancel.setOnClickListener(this);
 	//
 	// ImageButton btn_circle = (ImageButton)
 	// findViewById(R.id.stroke_ibtn_circle);
 	// btn_circle.setOnClickListener(this);
 	//
 	// ImageButton btn_rect = (ImageButton) findViewById(R.id.stroke_ibtn_rect);
 	// btn_rect.setOnClickListener(this);
 	//
 	// mRbtnCitcle = (RadioButton) findViewById(R.id.stroke_rbtn_circle);
 	// mRbtnCitcle.setOnClickListener(this);
 	//
 	// mRbtnRect = (RadioButton) findViewById(R.id.stroke_rbtn_rect);
 	// mRbtnRect.setOnClickListener(this);
 	//
 	// mBrushWidthSeekBar = (SeekBar) findViewById(R.id.stroke_width_seek_bar);
 	//
 	// mBrushWidthSeekBar
 	// .setOnSeekBarChangeListener(new OnBrushChangedWidthSeekBarListener());
 	//
 	// // mPreviewBrushImageView = (ImageView)
 	// // findViewById(R.id.stroke_width_text);
 	// mPreviewBrushBitmap = Bitmap.createBitmap(PREVIEW_BITMAP_SIZE,
 	// PREVIEW_BITMAP_SIZE, Config.ARGB_4444);
 	// mPreviewBrushCanvas = new Canvas(mPreviewBrushBitmap);
 	// mBrushSizeText = (TextView) findViewById(R.id.stroke_width_width_text);
 	// }
 
 	@Override
 	public void onClick(View v) {
 
 		switch (v.getId()) {
 
 		case R.id.stroke_ibtn_circle:
 			updateStrokeCap(Cap.ROUND);
 			mRbtnCitcle.setChecked(true);
 			break;
 
 		case R.id.stroke_ibtn_rect:
 			updateStrokeCap(Cap.SQUARE);
 			mRbtnRect.setChecked(true);
 			break;
 
 		case R.id.stroke_rbtn_circle:
 			updateStrokeCap(Cap.ROUND);
 			break;
 
 		case R.id.stroke_rbtn_rect:
 			updateStrokeCap(Cap.SQUARE);
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		if (mCurrentPaint.getStrokeCap() == Cap.ROUND) {
 			mRbtnCitcle.setChecked(true);
 		} else {
 			mRbtnRect.setChecked(true);
 		}
 		mBrushSizeText.setText("" + (int) mCurrentPaint.getStrokeWidth());
 		mBrushWidthSeekBar.setProgress((int) mCurrentPaint.getStrokeWidth());
 	}
 
 	@Override
 	public void onClick(DialogInterface dialog, int which) {
 
 		switch (which) {
 		case AlertDialog.BUTTON_NEUTRAL:
 			dismiss();
 			break;
 
 		default:
 			break;
 		}
 	}
 }
