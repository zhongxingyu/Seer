 package org.openforis.collect.android.fields;
 
 import java.util.ArrayList;
 
 import org.openforis.collect.android.R;
 import org.openforis.collect.android.messages.ToastMessage;
 import org.openforis.collect.android.screens.FormScreen;
 import org.openforis.idm.metamodel.CodeAttributeDefinition;
 import org.openforis.idm.metamodel.NodeDefinition;
 import org.openforis.idm.model.Code;
 import org.openforis.idm.model.CodeAttribute;
 import org.openforis.idm.model.EntityBuilder;
 import org.openforis.idm.model.Node;
 import org.openforis.idm.model.TextAttribute;
 import org.openforis.idm.model.TextValue;
 
 import android.content.Context;
 import android.text.InputFilter;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 
 public class CodeField extends InputField {
 	
 	private ArrayAdapter<String> aa;
 	private Spinner spinner;
 
 	ArrayList<String> options;
 	ArrayList<String> codes;
 	
 	boolean searchable;
 	
 	private static FormScreen form;
 	
 	private boolean selectedForTheFirstTime;
 	
 	private CodeAttributeDefinition codeAttrDef;
 	
 	public CodeField(Context context, NodeDefinition nodeDef, 
 			ArrayList<String> codes, ArrayList<String> options, 
 			String selectedItem) {
 		super(context, nodeDef);
 		this.searchable = true;
 
 		CodeField.form = (FormScreen)context;
 		
 		this.selectedForTheFirstTime = true;
 		
 		this.label.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		this.label.setOnLongClickListener(new OnLongClickListener() {
 	        @Override
 	        public boolean onLongClick(View v) {
 	        	ToastMessage.displayToastMessage(CodeField.this.getContext(), CodeField.this.getLabelText(), Toast.LENGTH_LONG);
 	            return true;
 	        }
 	    });
 		if (this.searchable){
 			this.label.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					
 				}});
 		}
 		
 		this.codeAttrDef = (CodeAttributeDefinition)this.nodeDefinition;
 		if (codeAttrDef.isAllowUnlisted()){
			this.txtBox = new EditText(context);
 			this.txtBox.setLayoutParams(new LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,(float) 3));
 			this.txtBox.addTextChangedListener(this);
 			this.txtBox.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
			this.addView(this.txtBox);
 		} else {
 			this.spinner = new Spinner(context);
 			this.spinner.setPrompt(nodeDef.getName());
 			
 			this.codes = codes;
 			this.options = options;
 
 			this.aa = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, this.options);
 			this.aa.setDropDownViewResource(R.layout.codelistitem);
 
 			this.spinner.setAdapter(aa);
 			this.spinner.setLayoutParams(new LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,(float) 3));
 			this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 			    @Override
 			    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
 			    	ArrayList<String> valueToAdd = new ArrayList<String>();
 			    	valueToAdd.add(CodeField.this.codes.get((CodeField.this.spinner.getSelectedItemPosition())));
 
			    	CodeField.this.setValue(0/*CodeField.form.currInstanceNo*/, CodeField.this.codes.get(CodeField.this.spinner.getSelectedItemPosition()),CodeField.form.getFormScreenId(),true);
 			    	if (!CodeField.this.selectedForTheFirstTime){
 						
 			    	} else {
 			    		CodeField.this.selectedForTheFirstTime = false;
 			    	}
 			    }
 
 			    @Override
 			    public void onNothingSelected(AdapterView<?> parentView) {
 			    	
 			    }
 
 			});
 			
 			boolean isFound = false;
 			int position = 0;
 			if (selectedItem!=null){
 				while (!isFound&&position<this.codes.size()){
 					if (this.codes.get(position).equals(selectedItem)){
 						isFound = true;
 					}
 					position++;
 				}	
			}		
 			if (isFound)
 				this.spinner.setSelection(position-1);
 			else
 				this.spinner.setSelection(0);
 
 			this.addView(this.spinner);	
 		}
 		
 		
 	}
 	
 	public void setValue(int position, String code, String path, boolean isSelectionChanged)
 	{
 		if (!this.codeAttrDef.isAllowUnlisted()){
 			boolean isFound = false;
 			int counter = 0;
 			while (!isFound&&counter<this.codes.size()){
 				if (this.codes.get(counter).equals(code)){
 					isFound = true;
 				}
 				counter++;
 			}
 			if (isFound){
 				if (!isSelectionChanged)
 					this.spinner.setSelection(counter-1);
 			}
 			else{
 				if (!isSelectionChanged)
 					this.spinner.setSelection(0);
			}	
		} else {
 			if (!isSelectionChanged)
 				this.txtBox.setText(code);
 
 			Node<? extends NodeDefinition> node = this.findParentEntity(path).get(this.nodeDefinition.getName(), position);
 			if (node!=null){
 				TextAttribute textAtr = (TextAttribute)node;
 				textAtr.setValue(new TextValue(code));
 			} else {
 				EntityBuilder.addValue(this.findParentEntity(path), this.nodeDefinition.getName(), code, position);	
 			}
		}		
 		
 		Node<? extends NodeDefinition> node = this.findParentEntity(path).get(this.nodeDefinition.getName(), position);
 		if (node!=null){
 			CodeAttribute codeAtr = (CodeAttribute)node;
 			codeAtr.setValue(new Code(code));
 		} else {
 			EntityBuilder.addValue(this.findParentEntity(path), this.nodeDefinition.getName(), new Code(code), position);	
 		}
 	}
 }
