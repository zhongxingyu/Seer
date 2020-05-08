 package com.aurelia.loaning.view.validation;
 
import org.apache.commons.lang3.StringUtils;

 import android.app.Activity;
 import android.widget.EditText;
 
 import com.aurelia.loaning.R;
 
 public class LoanFormValidator {
 
 	private static LoanFormValidator instance = new LoanFormValidator();
 
 	public static LoanFormValidator getInstance() {
 		return instance;
 	}
 
 	public boolean moneyLoanIsValid(Activity callinActivity) {
 		EditText amount = (EditText) callinActivity.findViewById(R.id.amount_field);
 		if (!checkNotNull(amount, "Amount is required!")) {
 			return false;
 		}
		try {
			Double.valueOf(amount.getText().toString());
		} catch (NumberFormatException e) {
			amount.requestFocus();
			amount.setError("Invalid format");
			return false;
		}
 
 		EditText currency = (EditText) callinActivity.findViewById(R.id.currency_field);
 		if (!checkNotNull(currency, "Currency is required!")) {
 			return false;
 		}
 
 		EditText person = (EditText) callinActivity.findViewById(R.id.person_field);
 		if (!checkNotNull(person, "Person is required!")) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public boolean objectLoanIsValid(Activity callinActivity) {
 		EditText description = (EditText) callinActivity.findViewById(R.id.description_field);
 		if (!checkNotNull(description, "Description is required!")) {
 			return false;
 		}
 
 		EditText person = (EditText) callinActivity.findViewById(R.id.person_field);
 		if (!checkNotNull(person, "Person is required!")) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private boolean checkNotNull(EditText editText, String errorMessage) {
		if (editText.getText() == null || StringUtils.isBlank(editText.getText().toString())) {
 			editText.requestFocus();
 			editText.setError(errorMessage);
 			return false;
 		}
 		return true;
 	}
 }
