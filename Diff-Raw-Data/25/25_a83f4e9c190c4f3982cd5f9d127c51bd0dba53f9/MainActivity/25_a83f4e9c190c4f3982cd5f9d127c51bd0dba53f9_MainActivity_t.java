 package br.com.cavedon.passwordgenerator.activity;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 import br.com.passwordgenerator.R;
 import br.com.cavedon.passwordgenerator.core.PasswordGenerator;
 
 import com.googlecode.androidannotations.annotations.AfterViews;
 import com.googlecode.androidannotations.annotations.Click;
 import com.googlecode.androidannotations.annotations.EActivity;
 import com.googlecode.androidannotations.annotations.ViewById;
 
 @EActivity(R.layout.activity_main)
 public class MainActivity extends Activity {
 	
 	PasswordGenerator generator;
 	
 	Boolean isNumbersOnly;
 	Boolean isSpecialChars;
	Integer maxSize;
 	
 	@ViewById
 	EditText master_pwd_input;
 	
 	@ViewById
 	EditText website_input;
 	
 	@ViewById
 	EditText generated_pwd_text;
 	
 	@ViewById
 	CheckBox number_only_checkbox;
 	
 	@ViewById
 	CheckBox special_chars_checkbox;
 	
 	@ViewById
 	CheckBox limit_size_checkbox;
 	
 	@ViewById
 	EditText limit_size_input;
 	
 	@ViewById
 	Button generate_pwd_btn;
 	
 	@Click(R.id.number_only_checkbox)
 	void toggleSpecialCharsBox() {
 		special_chars_checkbox.setEnabled(!number_only_checkbox.isChecked());
 	}
 	
 	@Click(R.id.special_chars_checkbox)
 	void toggleNumbersOnlyBox() {
 		number_only_checkbox.setEnabled(!special_chars_checkbox.isChecked());
 	}
 	
 	@Click(R.id.limit_size_checkbox)
 	void toggleMaxSize() {
 		limit_size_input.setEnabled(limit_size_checkbox.isChecked());
 		limit_size_input.requestFocus();
 		
 		if (!limit_size_input.isEnabled()) {
 			maxSize = null;
 			limit_size_input.setText("");
 		}
 	}
 
 	@AfterViews
 	void masterPwdInputListener() {
 		master_pwd_input.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				Boolean hasText = !StringUtils.isEmpty(s) && !StringUtils.isEmpty(website_input.getText()); 
 				generate_pwd_btn.setEnabled(hasText);
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			
 			@Override
 			public void afterTextChanged(Editable s) {}
 		});
 	}
 	
 	@AfterViews
 	void webSiteInputListener() {
 		website_input.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				Boolean hasText = !StringUtils.isEmpty(s) && !StringUtils.isEmpty(master_pwd_input.getText());
 				generate_pwd_btn.setEnabled(hasText);
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			
 			@Override
 			public void afterTextChanged(Editable s) {}
 		});
 	}
 	
 	@Click(R.id.generate_pwd_btn)
 	void generatePwd() {
 		generator = PasswordGenerator.getInstance(master_pwd_input.getText());
 		isNumbersOnly = number_only_checkbox.isChecked();
 		isSpecialChars = special_chars_checkbox.isChecked();
 		
 		if (!StringUtils.isEmpty(limit_size_input.getText())) {
 			maxSize = Integer.valueOf(limit_size_input.getText().toString());	
		} else {
			maxSize = -1;
 		}
 		
 		CharSequence webSite = website_input.getText(); 			
 		String pwd;
 		
 		try {
 			if (isNumbersOnly) {
 				pwd = generator.createNumericPasswordFor(webSite, maxSize);
 				
 			} else {
 				pwd = generator.createPasswordFor(webSite, isSpecialChars, maxSize);
 			}
 			generated_pwd_text.setText(pwd);
 			
 		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), getString(R.string.generate_pwd_error), Toast.LENGTH_SHORT).show();
 		}
 	}
 }
  
