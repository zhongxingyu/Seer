 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.communicator.fragments.messages;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import eu.trentorise.smartcampus.android.common.SCAsyncTask;
import eu.trentorise.smartcampus.android.common.validation.ValidatorHelper;
 import eu.trentorise.smartcampus.communicator.R;
 import eu.trentorise.smartcampus.communicator.custom.AbstractAsyncTaskProcessor;
 import eu.trentorise.smartcampus.communicator.custom.ColorPickerView;
 import eu.trentorise.smartcampus.communicator.custom.ColorPickerView.OnColorChangedListener;
 import eu.trentorise.smartcampus.communicator.custom.data.CommunicatorHelper;
 import eu.trentorise.smartcampus.communicator.model.LabelObject;
 import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;
 
 public class LabelDialog extends Dialog implements OnColorChangedListener{
 
 	private int color = 0;
 	private OnLabelCreatedListener listener;
 	private LabelObject label;
 	
 	public interface OnLabelCreatedListener {
 		public void OnLabelCreated(LabelObject label);
 	}
 	
 	public LabelDialog(Context context) {
 		this(context, null, null);
 	}
 	
 	public LabelDialog(Context context, OnLabelCreatedListener listener) {
 		this(context, listener, null);
 	}
 
 	public LabelDialog(Context context, LabelObject label) {
 		this(context, null, label);
 	}
 	
 	public LabelDialog(Context context, OnLabelCreatedListener listener, LabelObject label) {
 		super(context);
 		this.listener = listener;
 		this.label = label;
 		if (label != null) {
 			this.color = Long.decode(label.getColor()).intValue();
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.add_label_dialog);
 		
 		((LinearLayout)findViewById(R.id.color_picker_container)).addView(new ColorPickerView(getContext(), this, color));
 		
 		if (label != null) {
 			((EditText)findViewById(R.id.add_label_dialog_name)).setText(label.getName());
 		}
 		
 		Button cancelBtn = (Button) findViewById(R.id.add_label_dialog_cancel);
 		cancelBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				cancel();
 			}
 		});
 
 		Button okBtn = (Button) findViewById(R.id.add_label_dialog_ok);
 		okBtn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				EditText groupName = (EditText) findViewById(R.id.add_label_dialog_name);
 				String nameText = groupName.getText().toString().trim();
 	
 				if (label == null || !label.getName().equals(nameText)) {
					
 					if (CommunicatorHelper.getLabelByName(nameText) != null) {
						ValidatorHelper.highlight(getContext(), findViewById(R.id.add_label_dialog_name),  getContext().getString(R.string.label_name_duplicate));
//						Toast toast = Toast.makeText(getContext(), getContext().getString(R.string.label_name_duplicate), Toast.LENGTH_SHORT);
//						toast.show();
 						return;
 					}
 				}
 				
 				if (nameText.length() > 0) {
 					new SCAsyncTask<String, Void, Void>(getOwnerActivity(), new StoreLabelProcessor(getOwnerActivity())).execute(nameText);
 				} else {
					ValidatorHelper.highlight(getContext(), findViewById(R.id.add_label_dialog_name),  getContext().getString(R.string.label_name_empty));
//					Toast toast = Toast.makeText(getContext(), getContext().getString(R.string.label_name_empty), Toast.LENGTH_SHORT);
//					toast.show();
 				}
 			}
 		});
 
 		setTitle(R.string.add_label_dialog_title);
 	}
 
 	@Override
 	public void colorChanged(int color) {
 		this.color = color;
 	}
 	
 	private class StoreLabelProcessor extends AbstractAsyncTaskProcessor<String, Void> {
 
 		public StoreLabelProcessor(Activity activity) {
 			super(activity);
 		}
 
 		@Override
 		public Void performAction(String... params) throws SecurityException, Exception {
 			if (label != null) {
 				label = CommunicatorHelper.updateLabel(label, params[0], color);
 			} else {
 				label = CommunicatorHelper.createLabel(params[0], color);
 			}
 			return null;
 		}
 
 		@Override
 		public void handleResult(Void result) {
 			if (listener != null) listener.OnLabelCreated(label);
 			dismiss();
 		}
 		
 	}
  }
