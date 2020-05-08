 package org.openforis.collect.android.fields;
 
 import java.util.Map;
 
 import org.openforis.collect.android.R;
 import org.openforis.collect.android.management.ApplicationManager;
 import org.openforis.collect.android.messages.ToastMessage;
 import org.openforis.idm.metamodel.NodeDefinition;
 import org.openforis.idm.model.EntityBuilder;
 import org.openforis.idm.model.Node;
 import org.openforis.idm.model.TextAttribute;
 import org.openforis.idm.model.TextValue;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.text.method.QwertyKeyListener;
 import android.text.method.TextKeyListener;
import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.WindowManager;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MemoField extends InputField {
 	
 	public MemoField(Context context, NodeDefinition nodeDef) {
 		super(context, nodeDef);
 
 		this.label.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 2));
 		this.label.setOnLongClickListener(new OnLongClickListener() {
 	        @Override
 	        public boolean onLongClick(View v) {
 	        	ToastMessage.displayToastMessage(MemoField.this.getContext(), MemoField.this.getLabelText(), Toast.LENGTH_LONG);
 	            return true;
 	        }
 	    });
 		this.txtBox = new EditText(context);
 		//this.setHint(hintText);
		this.txtBox.setMaxLines(1);
 		this.txtBox.setLayoutParams(new LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,(float) 2));
 		this.txtBox.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
 	        @Override
 	        public void onFocusChange(View v, boolean hasFocus) {
 	            if (hasFocus) {
 	            	
 			    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 			    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 	            	
 	            	//Create dialog for Memo
 	            	final EditText input = new EditText(MemoField.this.getContext());
 	            	input.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
 	            	input.setText(txtBox.getText());
 	            	
             	
 	            	AlertDialog dialog = new AlertDialog.Builder(MemoField.this.getContext())	            	
 	                .setTitle(getResources().getString(R.string.editingMemoField)+" "+MemoField.this.getLabelText())
 	                .setView(input)
 	                .setPositiveButton(getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
 	                    public void onClick(DialogInterface dialog, int whichButton) {
 	                        String value = input.getText().toString();
 	                        txtBox.setText(value);
 	                    }
 	                }).setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
 	                    public void onClick(DialogInterface dialog, int whichButton) {
 	                        //do nothing.
 	                    }
 	                }).show();
	            	
 			    	if(valueForText){
 			    		input.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 			    		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
 			        }
 			    	else {
			    		input.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
			    		//input.setKeyListener(null);
 			    		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 			    	}
 	            }
 	        }
 	    });
 
 		this.addView(this.txtBox);
 	}
 	
 	public void setValue(Integer position, String value, String path, boolean isTextChanged)
 	{
 		if (!isTextChanged)
 			this.txtBox.setText(value);
 
 		Node<? extends NodeDefinition> node = this.findParentEntity(path).get(this.nodeDefinition.getName(), position);
 		if (node!=null){
 			TextAttribute textAtr = (TextAttribute)node;
 			textAtr.setValue(new TextValue(value));
 		} else {
 			EntityBuilder.addValue(this.findParentEntity(path), this.nodeDefinition.getName(), value, position);	
 		}
 	}
 }
