 package com.test.getconnected;
 
 import android.test.AndroidTestCase;
 import android.widget.EditText;
 
 import com.app.getconnected.R;
 import com.exception.getconnected.FieldValidationException;
 import com.util.getconnected.FieldValidator;
 
 /**
  * Created with IntelliJ IDEA.
  * User: johan_000
  * Date: 10/17/13
  * Time: 11:21 PM
  */
 public class FieldValidatorTest extends AndroidTestCase {
 
 	private static final String INPUT = "teststring";
 	private EditText textField;
 
 	@Override
 	protected void setUp()
 	{
 		textField=new EditText(getContext());
 		textField.setText("non-alphanumeric text!");
 		textField.setTag("required,alphanumeric");
 	}
 	
 	public void testValidateTextField() throws Exception {
 		boolean error = false;
 		try {
 			FieldValidator.validateTextField(textField);
 		} catch (FieldValidationException e) {
 			error = true;
 			assertTrue(R.string.field_validation_not_alphanumeric==e.getIndex());
 		} finally {
			assertTrue(error);
 		}
 	}
 
 	public void testCheckEmpty() throws Exception {
 		boolean error = false;
 		try {
 			FieldValidator.checkEmpty(INPUT);
 		} catch (FieldValidationException e) {
 			error = true;
 		} finally {
 			assertFalse(error);
 		}
 	}
 	
 	public void testCheckNumeric() throws Exception {
 		boolean error = false;
 		try {
 			FieldValidator.checkNumeric(INPUT);
 		} catch (FieldValidationException e) {
 			error = true;
 		} finally {
 			assertTrue(error);
 		}
 	}
 	
 	public void testCheckMinLength() throws Exception {
 		boolean error = false;
 		try {
 			FieldValidator.checkMinLength(INPUT);
 		} catch (FieldValidationException e) {
 			error = true;
 		} finally {
 			assertFalse(error);
 		}
 	}
 	
 	public void testCheckEmail() throws Exception {
 		boolean error = false;
 		try {
 			FieldValidator.checkEmail(INPUT);
 		} catch (FieldValidationException e) {
 			error = true;
 		} finally {
 			assertTrue(error);
 		}
 	}
 }
