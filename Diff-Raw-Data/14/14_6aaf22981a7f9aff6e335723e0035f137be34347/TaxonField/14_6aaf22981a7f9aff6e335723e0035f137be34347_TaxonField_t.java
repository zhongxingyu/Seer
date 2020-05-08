 package org.openforis.collect.android.fields;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.openforis.collect.android.R;
 import org.openforis.collect.android.data.FieldValue;
 import org.openforis.collect.android.dialogs.SearchTaxonActivity;
 import org.openforis.collect.android.management.ApplicationManager;
 import org.openforis.collect.android.screens.FormScreen;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.text.InputType;
 import android.text.method.QwertyKeyListener;
 import android.text.method.TextKeyListener;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class TaxonField extends Field {
 	
 	private TextView sciNameLabel;
 	private TextView venacNamesLabel;
 	private TextView venacLangLabel;
 	private TextView langVariantLabel;
 	
 	private EditText txtCodes; //Code
 	private EditText txtSciName; //Scientific name
 	private EditText txtVernacularName; //Vernacular names
 	private EditText txtVernacularLang; //List of vernacular languages
 	private EditText txtLangVariant; //Language variant
 	
 	private Button btnSearchByCode;
 	private Button btnSearchBySciName;
 	private Button btnSearchByVernName;
 	
 	boolean searchable;
 	private static FormScreen form;
 	private List<ArrayList<String>> values;
 	
 	public TaxonField(Context context, int id, String labelText, String[] initialText, String hintText, String promptText, 
 			ArrayList<String> codes, ArrayList<String> options, 
 			String selectedItem, boolean isSearchable,
 			boolean isMultiple, boolean isRequired, FieldValue fieldValue) {
 		super(context, id, isMultiple, isRequired);
 
 		TaxonField.form = (FormScreen)context;
 		
 		//Set Taxon field value
 		this.values = new ArrayList<ArrayList<String>>();
 //		TaxonField.this.values.add(TaxonField.this.currentInstanceNo, "");
 		ArrayList<String> initialValue = new ArrayList<String>();
 		initialValue.add(initialText[0]);
 		initialValue.add(initialText[1]);
 		initialValue.add(initialText[2]);
 		initialValue.add(initialText[3]);
 		initialValue.add(initialText[4]);
 		TaxonField.this.values.add(initialValue);
 		
 		//Create input field "Code"
 		//Label "Code"
 		this.label.setText("Code");
		this.label.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		//Add text field where user can type a code
 		this.txtCodes = new EditText(context);
 		this.txtCodes.setText(initialText[0]);
 		this.txtCodes.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 3));
 		// When txtCode gets focus
 		this.txtCodes.setOnFocusChangeListener(new OnFocusChangeListener() {
 		    @Override
 		    public void onFocusChange(View v, boolean hasFocus) {
 		    	// Get current settings about software keyboard for text fields
 		    	if(hasFocus){
 			    	if(this.getClass().toString().contains("TaxonField")){
 				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 				    	// Switch on or off Software keyboard depend of settings
 				    	if(valueForText){
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		TaxonField.this.txtCodes.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 				        }
 				    	else {
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		txtCodes.setInputType(InputType.TYPE_NULL);
 				    	}
 			    	}
 		    	}
 		    }
 	    });	
 		//Button "Search By Code"
 		this.btnSearchByCode = new Button(context);
 		this.btnSearchByCode.setText("Search");
 		this.btnSearchByCode.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		this.btnSearchByCode.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				//Get category and value
 				String strValue = TaxonField.this.txtCodes.getText().toString();				
 				//Add current value to FormScreen.currentFieldValue
 		    	ArrayList<String> tempValue = new ArrayList<String>();
 				tempValue.add(TaxonField.this.txtCodes.getText().toString());
 				tempValue.add(TaxonField.this.txtSciName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularLang.getText().toString());
 				tempValue.add(TaxonField.this.txtLangVariant.getText().toString());				
 				TaxonField.this.value.setValue(TaxonField.form.currInstanceNo, tempValue);
 				FormScreen.currentFieldValue = TaxonField.this.value;
 				FormScreen.currentFieldValue.setValue(TaxonField.form.currInstanceNo, tempValue);
 				if (TaxonField.form.currentNode!=null){
 					TaxonField.form.currentNode.addFieldValue(FormScreen.currentFieldValue);
 				}
 				//Check the value is not empty  and Run SearchTaxon activity
 				//TODO: in future validator should check it
 				if (!strValue.isEmpty())
 					TaxonField.this.startSearchScreen(strValue, "Code");
 				else
 					Log.i(getResources().getString(R.string.app_name), "Value of Code field is EMPTY!!!! ");
 			}
 		});		
 		
 		//Create layout and add input field "Code" into there
 		LinearLayout codeLL = new LinearLayout(context);		
 		codeLL.setOrientation(HORIZONTAL);
 		codeLL.addView(this.label);
 		codeLL.addView(this.txtCodes);
 		codeLL.addView(this.btnSearchByCode);		
 		
 		//Create input field "Scientific name"
 		//Create label "Scientific name"
 		this.sciNameLabel = new TextView(context);
 		this.sciNameLabel.setMaxLines(1);
 		this.sciNameLabel.setTextColor(Color.BLACK);
 		this.sciNameLabel.setText("Scientific names");
		this.sciNameLabel.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		//Add text box for scientific names
 		this.txtSciName = new EditText(context);
 		this.txtSciName.setText(initialText[1]);	
 		this.txtSciName.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 3));
 		// When txtSciName gets focus
 		this.txtSciName.setOnFocusChangeListener(new OnFocusChangeListener() {
 		    @Override
 		    public void onFocusChange(View v, boolean hasFocus) {
 		    	// Get current settings about software keyboard for text fields
 		    	if(hasFocus){
 		    		if(this.getClass().toString().contains("TaxonField")){
 				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 				    	// Switch on or off Software keyboard depend of settings
 				    	if(valueForText){
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		TaxonField.this.txtSciName.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 				        }
 				    	else {
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		txtSciName.setInputType(InputType.TYPE_NULL);
 				    	}
 			    	}
 		    	}
 		    }
 	    });		
 		//Button "Search By Scientific names"
 		this.btnSearchBySciName = new Button(context);
 		this.btnSearchBySciName.setText("Search");
 		this.btnSearchBySciName.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		this.btnSearchBySciName.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				//Get category and value
 				String strValue = TaxonField.this.txtSciName.getText().toString();				
 				//Add current value to FormScreen.currentFieldValue
 		    	ArrayList<String> tempValue = new ArrayList<String>();
 				tempValue.add(TaxonField.this.txtCodes.getText().toString());
 				tempValue.add(TaxonField.this.txtSciName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularLang.getText().toString());
 				tempValue.add(TaxonField.this.txtLangVariant.getText().toString());				
 				TaxonField.this.value.setValue(TaxonField.form.currInstanceNo, tempValue);
 				FormScreen.currentFieldValue = TaxonField.this.value;
 				FormScreen.currentFieldValue.setValue(TaxonField.form.currInstanceNo, tempValue);
 				if (TaxonField.form.currentNode!=null){
 					TaxonField.form.currentNode.addFieldValue(FormScreen.currentFieldValue);
 				}
 				//Check the value is not empty  and Run SearchTaxon activity
 				//TODO: in future validator should check it
 				if (!strValue.isEmpty())
 					TaxonField.this.startSearchScreen(strValue, "SciName");
 				else
 					Log.i(getResources().getString(R.string.app_name), "Value of SciName field is EMPTY!!!! ");
 			}
 		});		
 		
 		//Create layout and add input field "Scientific name" into there
 		LinearLayout sciNameLL = new LinearLayout(context);		
 		sciNameLL.setOrientation(HORIZONTAL);
 		sciNameLL.addView(this.sciNameLabel);
 		sciNameLL.addView(this.txtSciName);
 		sciNameLL.addView(this.btnSearchBySciName);			
 		
 		//Create input field "Vernacular name"
 		//Create label "Vernacular name"
 		this.venacNamesLabel = new TextView(context);
 		this.venacNamesLabel.setMaxLines(1);
 		this.venacNamesLabel.setTextColor(Color.BLACK);
 		this.venacNamesLabel.setText("Vernacular name");
		this.venacNamesLabel.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		//Add textbox for vernacular names
 		this.txtVernacularName = new EditText(context);
 		this.txtVernacularName.setText(initialText[2]);		
 		this.txtVernacularName.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 3));
 		// When txtVernacularName gets focus
 		this.txtVernacularName.setOnFocusChangeListener(new OnFocusChangeListener() {
 		    @Override
 		    public void onFocusChange(View v, boolean hasFocus) {
 		    	// Get current settings about software keyboard for text fields
 		    	if(hasFocus){
 		    		if(this.getClass().toString().contains("TaxonField")){
 				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 				    	// Switch on or off Software keyboard depend of settings
 				    	if(valueForText){
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		TaxonField.this.txtVernacularName.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 				        }
 				    	else {
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		txtVernacularName.setInputType(InputType.TYPE_NULL);
 				    	}
 			    	}
 		    	}
 		    }
 	    });	
 		//Button "Search By Vernacular names"
 		this.btnSearchByVernName = new Button(context);
 		this.btnSearchByVernName.setText("Search");
 		this.btnSearchByVernName.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 1));
 		this.btnSearchByVernName.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				//Get category and value
 				String strValue = TaxonField.this.txtVernacularName.getText().toString();				
 				//Add current value to FormScreen.currentFieldValue
 		    	ArrayList<String> tempValue = new ArrayList<String>();
 				tempValue.add(TaxonField.this.txtCodes.getText().toString());
 				tempValue.add(TaxonField.this.txtSciName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularName.getText().toString());
 				tempValue.add(TaxonField.this.txtVernacularLang.getText().toString());
 				tempValue.add(TaxonField.this.txtLangVariant.getText().toString());				
 				TaxonField.this.value.setValue(TaxonField.form.currInstanceNo, tempValue);
 				FormScreen.currentFieldValue = TaxonField.this.value;
 				FormScreen.currentFieldValue.setValue(TaxonField.form.currInstanceNo, tempValue);
 				if (TaxonField.form.currentNode!=null){
 					TaxonField.form.currentNode.addFieldValue(FormScreen.currentFieldValue);
 				}
 				//Check the value is not empty  and Run SearchTaxon activity
 				//TODO: in future validator should check it
 				if (!strValue.isEmpty())
 					TaxonField.this.startSearchScreen(strValue, "VernacularName");
 				else
 					Log.i(getResources().getString(R.string.app_name), "Value of VernName field is EMPTY!!!! ");
 			}
 		});			
 		//Create layout and add input field "Vernacular name" into there
 		LinearLayout vernNameLL = new LinearLayout(context);		
 		vernNameLL.setOrientation(HORIZONTAL);
 		vernNameLL.addView(this.venacNamesLabel);
 		vernNameLL.addView(this.txtVernacularName);
 		vernNameLL.addView(this.btnSearchByVernName);			
 		
 		//Create input field "Vernacular language"
 		//Create label "Vernacular language"
 		this.venacLangLabel = new TextView(context);
 		this.venacLangLabel.setMaxLines(1);
 		this.venacLangLabel.setTextColor(Color.BLACK);
 		this.venacLangLabel.setText("Vernacular language");			
 		this.venacLangLabel.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 2));
 		//Add text box for vernacular languages
 		this.txtVernacularLang = new EditText(context);
 		this.txtVernacularLang.setText(initialText[3]);
		this.txtVernacularLang.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 3));
 		// When txtLangVariant gets focus
 		this.txtVernacularLang.setOnFocusChangeListener(new OnFocusChangeListener() {
 		    @Override
 		    public void onFocusChange(View v, boolean hasFocus) {
 		    	// Get current settings about software keyboard for text fields
 		    	if(hasFocus){
 		    		if(this.getClass().toString().contains("TaxonField")){
 				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 				    	// Switch on or off Software keyboard depend of settings
 				    	if(valueForText){
 				    		TaxonField.this.txtVernacularLang.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 				        }
 				    	else {
 				    		txtVernacularLang.setInputType(InputType.TYPE_NULL);
 				    	}
 			    	}
 		    	}
 		    }
 	    });		
 		//Create layout and add input field "Vernacular language" into there
 		LinearLayout vernLangLL = new LinearLayout(context);		
 		vernLangLL.setOrientation(HORIZONTAL);
 		vernLangLL.addView(this.venacLangLabel);
 		vernLangLL.addView(this.txtVernacularLang);		
 		
 		//Create input field "Language variant"
 		//Create label "Language variant"
 		this.langVariantLabel = new TextView(context);
 		this.langVariantLabel.setMaxLines(1);
 		this.langVariantLabel.setTextColor(Color.BLACK);
 		this.langVariantLabel.setText("Language variant");	
 		this.langVariantLabel.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 2));
 		//Add textbox for language variants
 		this.txtLangVariant = new EditText(context);
 		this.txtLangVariant.setText(initialText[4]);	
		this.txtLangVariant.setLayoutParams(new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, (float) 3));
 		// When txtLangVariant gets focus
 		this.txtLangVariant.setOnFocusChangeListener(new OnFocusChangeListener() {
 		    @Override
 		    public void onFocusChange(View v, boolean hasFocus) {
 		    	// Get current settings about software keyboard for text fields
 		    	if(hasFocus){
 		    		if(this.getClass().toString().contains("TaxonField")){
 				    	Map<String, ?> settings = ApplicationManager.appPreferences.getAll();
 				    	Boolean valueForText = (Boolean)settings.get(getResources().getString(R.string.showSoftKeyboardOnTextField));
 				    	// Switch on or off Software keyboard depend of settings
 				    	if(valueForText){
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		TaxonField.this.txtLangVariant.setKeyListener(new QwertyKeyListener(TextKeyListener.Capitalize.NONE, false));
 				        }
 				    	else {
 				    		//Log.i(getResources().getString(R.string.app_name), "Setting taxon field is: " + valueForText);
 				    		txtLangVariant.setInputType(InputType.TYPE_NULL);
 				    	}
 			    	}
 		    	}
 		    }
 	    });
 		
 		//Create layout and add input field "Language variant" into there
 		LinearLayout langVariantLL = new LinearLayout(context);
 		langVariantLL.addView(this.langVariantLabel);
 		langVariantLL.addView(this.txtLangVariant);		
 		
 		//Check "is Searchable" argument
 		if (this.searchable){
 			this.label.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					
 				}});
 		}
 		this.searchable = isSearchable;			
 		
 		//Add child layouts to container
 		this.setOrientation(VERTICAL);
 		this.addView(codeLL);
 		this.addView(sciNameLL);
 		this.addView(vernNameLL);
 		this.addView(vernLangLL);
 		this.addView(langVariantLL);
 		
 		//this.addView(this.scrollLeft);
 		//this.addView(this.scrollRight);
 		
 		this.value = fieldValue;
 	}
 	
 
 	/*@Override
 	public void scrollLeft(){
     	if (TaxonField.this.currentInstanceNo>0){
     		ArrayList<String> tempValue = new ArrayList<String>();
     		tempValue.add(String.valueOf(TaxonField.this.spinner.getSelectedItemPosition()));
     		tempValue.add(TaxonField.this.txtBox.getText().toString());
     		TaxonField.this.values.set(TaxonField.this.currentInstanceNo, tempValue);
     		TaxonField.this.txtBox.setText(TaxonField.this.values.get(TaxonField.this.currentInstanceNo-1).get(1).toString());
 			TaxonField.this.spinner.setSelection(Integer.valueOf(TaxonField.this.values.get(TaxonField.this.currentInstanceNo-2).get(0)));
     		TaxonField.this.currentInstanceNo--;
     	}
 	}
 	
 	@Override
 	public void scrollRight(){
     	if (TaxonField.this.values.size()==TaxonField.this.currentInstanceNo){
     		ArrayList<String> tempValue = new ArrayList<String>();
     		tempValue.add("0");
     		tempValue.add("");
     		this.values.add(tempValue);
     		TaxonField.this.values.add(TaxonField.this.currentInstanceNo, tempValue);	        		
     	}
 		ArrayList<String> tempValue = new ArrayList<String>();
 		tempValue.add(String.valueOf(TaxonField.this.spinner.getSelectedItemPosition()));
 		tempValue.add(TaxonField.this.txtBox.getText().toString());
     	TaxonField.this.values.set(TaxonField.this.currentInstanceNo, tempValue);        			        		
 		if (TaxonField.this.values.size()>TaxonField.this.currentInstanceNo){
 			TaxonField.this.txtBox.setText(TaxonField.this.values.get(TaxonField.this.currentInstanceNo).get(1).toString());
 			TaxonField.this.spinner.setSelection(Integer.valueOf(TaxonField.this.values.get(TaxonField.this.currentInstanceNo).get(0)));
 		}			
 		TaxonField.this.currentInstanceNo++;
 	}*/
 	
 	public String getHint()
 	{
 //		return this.txtBox.getHint().toString();
 		return "";
 	}
 	
 	public void setHint(String value)
 	{
 //		this.txtBox.setHint(value);
 	}
 	
 	@Override
 	public int getInstancesNo(){
 		return this.values.size();
 	}
 	
 	/* 
 	 * getValue and saving field value after state changed i.e. user selected from menu or typed sth
 	 */
 	public List<String> getValue(int index){
 		return TaxonField.this.value.getValue(index);
 	}
 
 	public void setValue(int position, String code, String sciName, String vernName, String vernLang, String langVariant){
 		//Set text to textboxes
 		this.txtCodes.setText(code);
 		this.txtSciName.setText(sciName);
 		this.txtVernacularName.setText(vernName);
 		this.txtVernacularLang.setText(vernLang);
 		this.txtLangVariant.setText(langVariant);
 		Log.i(getResources().getString(R.string.app_name), "Value was set!!! " + code + " " + sciName);
 		
 		ArrayList<String> valueToAdd = new ArrayList<String>();
 		valueToAdd.add(code);
 		valueToAdd.add(sciName);
 		valueToAdd.add(vernName);
 		valueToAdd.add(vernLang);
 		valueToAdd.add(langVariant);
 		TaxonField.this.value.setValue(position, valueToAdd);
 	}
 	
 	public void resetValues(){
 		this.values = new ArrayList<ArrayList<String>>();
 
 	}
 	
 	public void addValue(ArrayList<String> value){
 		this.values.add(value);
 		this.currentInstanceNo++;
 	}	
 	
 	private void startSearchScreen(String strContent, String strCriteria){
 		int taxonId = TaxonField.this.elemId;
 		Intent searchTaxonIntent = new Intent(TaxonField.this.getContext(), SearchTaxonActivity.class);
 		searchTaxonIntent.putExtra("content", strContent);
 		searchTaxonIntent.putExtra("criteria", strCriteria);
 		searchTaxonIntent.putExtra("taxonId", taxonId);
 		searchTaxonIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     	super.getContext().startActivity(searchTaxonIntent);			
 		
 	}
 }
