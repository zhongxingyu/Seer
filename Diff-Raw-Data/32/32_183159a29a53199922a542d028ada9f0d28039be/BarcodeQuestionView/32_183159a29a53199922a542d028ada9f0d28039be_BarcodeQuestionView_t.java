 package com.gallatinsystems.survey.device.view;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TableRow;
 
 import com.gallatinsystems.survey.device.R;
 import com.gallatinsystems.survey.device.domain.Question;
 import com.gallatinsystems.survey.device.domain.QuestionResponse;
 import com.gallatinsystems.survey.device.event.QuestionInteractionEvent;
 import com.gallatinsystems.survey.device.util.ConstantUtil;
 
 /**
  * Question to handle scanning of a barcode. This question relies on the zxing
  * library being installed on the device.
  * 
  * @author Christopher Fagiani
  * 
  */
 public class BarcodeQuestionView extends QuestionView implements
		OnClickListener, OnFocusChangeListener {
 
 	private Button barcodeButton;
 	private EditText barcodeText;
 
 	public BarcodeQuestionView(Context context, Question q,
 			String defaultLanguage, String[] langCodes, boolean readOnly) {
 		super(context, q, defaultLanguage, langCodes, readOnly);
 		init();
 	}
 
 	protected void init() {
 		Context context = getContext();
 		TableRow tr = new TableRow(context);
 		barcodeButton = new Button(context);
 		barcodeText = new EditText(context);
 		barcodeText.setWidth(DEFAULT_WIDTH);
		barcodeText.setOnFocusChangeListener(this);
 		barcodeButton.setText(R.string.scanbarcode);
 
 		barcodeButton.setOnClickListener(this);
 		if (readOnly) {
 			barcodeButton.setEnabled(false);
 			barcodeText.setEnabled(false);
 		}
 
 		tr.addView(barcodeButton);
 		addView(tr);
 		tr = new TableRow(context);
 		tr.addView(barcodeText);
 
 		addView(tr);
 	}
 
 	/**
 	 * handle the action button click
 	 */
 	public void onClick(View v) {
 		notifyQuestionListeners(QuestionInteractionEvent.SCAN_BARCODE_EVENT);
 	}
 
 	@Override
 	public void questionComplete(Bundle barcodeData) {
 		if (barcodeData != null) {
 			barcodeText.setText(barcodeData
 					.getString(ConstantUtil.BARCODE_CONTENT));
 			setResponse(new QuestionResponse(
 					barcodeData.getString(ConstantUtil.BARCODE_CONTENT),
 					ConstantUtil.VALUE_RESPONSE_TYPE, getQuestion().getId()));
 		}
 	}
 
 	/**
 	 * restores the data and turns on the complete icon if the content is
 	 * non-null
 	 */
 	@Override
 	public void rehydrate(QuestionResponse resp) {
 		super.rehydrate(resp);
 		if (resp != null) {
 			if (resp.getValue() != null) {
 				barcodeText.setText(resp.getValue());
 			}
 
 		}
 	}
 
 	/**
 	 * clears the file path and the complete icon
 	 */
 	@Override
 	public void resetQuestion(boolean fireEvent) {
 		super.resetQuestion(fireEvent);
 		barcodeText.setText("");
 	}
 
	/**
	 * captures the response and runs validation on loss of focus
	 */
	@Override
	public void onFocusChange(View view, boolean hasFocus) {
		// we need to listen to loss of focus
		// and make sure input is valid
		if (!hasFocus) {
			captureResponse(false);
		}
	}

	/**
	 * pulls the data out of the fields and saves it as a response object,
	 * possibly suppressing listeners
	 */
	public void captureResponse(boolean suppressListeners) {
		setResponse(new QuestionResponse(barcodeText.getText().toString(),
				ConstantUtil.VALUE_RESPONSE_TYPE, getQuestion().getId()),
				suppressListeners);
	}

 }
