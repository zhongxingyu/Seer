 package com.nari.somnium.activity;
 
 import com.nari.somnium.R;
 import com.nari.somnium.adapter.CLManualDialogAdapter;
 import com.nari.somnium.helper.DatabaseHelper;
 import com.nari.somnium.pojo.CLDataPojo;
 import com.nari.somnium.pojo.CLSubDataPojo;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 /**
  * @author Nari Kim Shin (wassupnari@gmail.com)
  */
 
 public class CLDetailActivity extends BaseActivity {
 
 	private TextView mEvaluatorStatic;
 	private TextView mEvaluator;
 	private EditText mEvaluatorEdit;
 
 	private TextView mDateStatic;
 	private TextView mDate;
 	private EditText mDateEdit;
 
 	private TextView mCommentStatic;
 	private TextView mComment;
 	private EditText mCommentEdit;
 
 	private int mProjectNumber;
 	private int mSubNumber;
 
 	private ToggleButton mComplete;
 
 	private CLDataPojo mDataList;
 	private CLSubDataPojo mData;
 
 	private DatabaseHelper mDB;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_cl_detail);
 		String title = getIntent().getExtras().getString("cl_detail_title");
 		mProjectNumber = getIntent().getIntExtra("cl_group_id", 0);
 		mSubNumber = getIntent().getIntExtra("cl_child_id", 0);
 		showActionBar(this, title);
 
 		mDB = new DatabaseHelper(this);
 
 		mDataList = mDB.getUserCLData(mProjectNumber);
 		mDataList.setId(mProjectNumber);
 		mData = mDataList.getSubData().get(mSubNumber);
		
 		setupUI();
 
 	}
 
 	public void setupUI() {
 		mEvaluatorStatic = (TextView) findViewById(R.id.cl_evaluator_static);
 		mEvaluator = (TextView) findViewById(R.id.cl_evaluator);
 		mEvaluatorEdit = (EditText) findViewById(R.id.cl_evaluator_edit);
 
 		mDateStatic = (TextView) findViewById(R.id.cl_date_static);
 		mDate = (TextView) findViewById(R.id.cl_date);
 		mDateEdit = (EditText) findViewById(R.id.cl_date_edit);
 
 		mCommentStatic = (TextView) findViewById(R.id.cl_comment_static);
 		mComment = (TextView) findViewById(R.id.cl_comment);
 		mCommentEdit = (EditText) findViewById(R.id.cl_comment_edit);
 
 		mEvaluatorStatic.setText("Evaluator : ");
 		mDateStatic.setText("Date : ");
 		mCommentStatic.setText("Comment : ");
 
 		if (mData != null) {
 			mEvaluator.setText(mData.getEvaluator());
 			mDate.setText(mData.getDate());
 			mComment.setText(mData.getComment());
 		}
 
 		mComplete = (ToggleButton) findViewById(R.id.footer_toggle_cl);
 		if (mData.getComplete()) {
 			mComplete.setChecked(true);
 		} else {
 			mComplete.setChecked(false);
 		}
 
 		mComplete.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
 
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				mData.setComplete(isChecked);
 				mDataList.getSubData().set(mSubNumber, mData);
 				mDB.updateCL(mDataList);
 			}
 
 		});
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		mData = null;
 		mDataList = null;
 	}
 
 	@Override
 	public void onEdit() {
 		super.onEdit();
 		mEvaluator.setVisibility(View.GONE);
 		mDate.setVisibility(View.GONE);
 		mComment.setVisibility(View.GONE);
 
 		mEvaluatorEdit.setVisibility(View.VISIBLE);
 		mEvaluatorEdit.setText(mData.getEvaluator());
 		mDateEdit.setVisibility(View.VISIBLE);
 		mDateEdit.setText(mData.getDate());
 		mCommentEdit.setVisibility(View.VISIBLE);
 		mCommentEdit.setText(mData.getComment());
 	}
 
 	@Override
 	public void onEditDone() {
 		super.onEditDone();
 
 		// Hide keyboard
 		InputMethodManager inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
 		if (inputManager.isAcceptingText()) {
 			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
 					InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 
 		mEvaluator.setVisibility(View.VISIBLE);
 		mDate.setVisibility(View.VISIBLE);
 		mComment.setVisibility(View.VISIBLE);
 
 		String evaluator = mEvaluatorEdit.getText().toString();
 		String date = mDateEdit.getText().toString();
 		String comment = mCommentEdit.getText().toString();
 
 		mEvaluatorEdit.setVisibility(View.GONE);
 		mDateEdit.setVisibility(View.GONE);
 		mCommentEdit.setVisibility(View.GONE);
 
 		mData.setEvaluator(evaluator);
 		mData.setDate(date);
 		mData.setComment(comment);
 
 		mEvaluator.setText(evaluator);
 		mDate.setText(date);
 		mComment.setText(comment);
 
		mDataList.getSubData().set(mSubNumber, mData);
		
 		mDB.updateCL(mDataList);
 
 	}
 
 	@Override
 	public void onInfo() {
 		super.onInfo();
 		final Dialog dialog = new Dialog(CLDetailActivity.this);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		dialog.setContentView(R.layout.dialog_cl_manual);
 
 		ListView list = (ListView) dialog.findViewById(R.id.dialog_cl_list);
 		list.setAdapter(new CLManualDialogAdapter(this));
 
 		Button gotit = (Button) dialog.findViewById(R.id.dialog_cl_gotit);
 		gotit.setOnClickListener(new Button.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				dialog.dismiss();
 			}
 
 		});
 
 		dialog.show();
 	}
 
 }
