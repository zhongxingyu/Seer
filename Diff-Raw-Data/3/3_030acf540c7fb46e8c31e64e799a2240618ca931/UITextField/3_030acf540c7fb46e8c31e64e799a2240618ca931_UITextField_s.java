 /*
  * Copyright (C) 2012 Wu Tong
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cocoa4android.ui;
 
 
 
 import org.cocoa4android.cg.CGRect;
 
 import android.content.Context;
 
 import android.graphics.drawable.ShapeDrawable;
 import android.graphics.drawable.shapes.RoundRectShape;
 import android.text.method.PasswordTransformationMethod;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import org.cocoa4android.ns.NSTextAlignment;
 
 
 public class UITextField extends UIView {
 	private EditText textField = null;
 	public String placeholder;
 	
 	public UITextField(){
 		textField = new EditText(context);
 		textField.setGravity(Gravity.CENTER_VERTICAL);
 		textField.setFocusable(YES);
 		this.setTextField(textField);
 		this.setView(textField);
 		
 		ShapeDrawable background = new ShapeDrawable(new RoundRectShape(new float[] {8*scaleFactorX,8*scaleFactorY, 8*scaleFactorX,8*scaleFactorY, 8*scaleFactorX,8*scaleFactorY, 8*scaleFactorX,8*scaleFactorY},null,null));
 		background.getPaint().setColor(UIColor.whiteColor().getColor());
 		background.getPaint().setStrokeWidth(1);
 		this.textField.setBackgroundDrawable(background);
 		this.textField.setPadding((int)(8*scaleDensityX), 0, (int)(8*scaleDensityY), 0);
 
 		this.textField.setOnKeyListener(new OnKeyListener() {
 			
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction()!=KeyEvent.ACTION_DOWN)
                     return YES;
 				
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					if (delegate!=null) {
 						UITextField.firstResponder = UITextField.this;
 						UITextField.this.resignFirstResponder();
 						return delegate.textFieldShouldReturn(UITextField.this);
 					}
 				}
 				return NO;
 			}
 		});
 		this.textField.setOnEditorActionListener(new OnEditorActionListener() {
 			
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				if (delegate!=null) {
 					//UITextField.this.resignFirstResponder();
 					//return delegate.textFieldShouldReturn(UITextField.this);
 				}
 				return false;
 			}
 		});
 	}
 	public UITextField(CGRect frame){
 		this();
 		this.setFrame(frame);
 	}
 	public EditText getTextField() {
 		return textField;
 	}
 	public void setTextField(EditText textField) {
 		this.textField = textField;
 	}
 	public void setText(String text){
 		this.textField.setText(text);
 	}
 
 	public String text(){
 		return this.textField.getText().toString();
 	}
 	private UIColor textColor;
 	public void setTextColor(UIColor color){
 		this.textField.setTextColor(color.getColor());
 		textColor = color;
 	}
 	public UIColor textColor(){
 		return textColor;
 	}
 	private NSTextAlignment textAlignment;
 	public void setTextAlignment(NSTextAlignment alignment){
 		switch (alignment) {
 		case NSTextAlignmentLeft:
 			this.textField.setGravity(textField.getGravity()&Gravity.VERTICAL_GRAVITY_MASK|Gravity.LEFT);
 			break;
 		case NSTextAlignmentCenter:
 			this.textField.setGravity(textField.getGravity()&Gravity.VERTICAL_GRAVITY_MASK|Gravity.CENTER);
 			break;
 		case NSTextAlignmentRight:
 			this.textField.setGravity(textField.getGravity()&Gravity.VERTICAL_GRAVITY_MASK|Gravity.RIGHT);
 			break;
 		}
 		textAlignment = alignment;
 	}
 	public NSTextAlignment textAlignment() {
 		return textAlignment;
 	}
 	public void setFont(UIFont font) {
 		this.setFontSize(font.fontSize);
 		this.textField.setTypeface(font.getFont());
 	}
 	public void setFontSize(float fontSize){
 		this.textField.setTextSize(fontSize*UIScreen.mainScreen().getDensityText());
 	}
 	public void setSecureTextEntry(boolean secureTextEntry){
 		if(secureTextEntry){
 			String temporary_stored_text = textField.getText().toString().trim();
 			textField.setTransformationMethod(PasswordTransformationMethod.getInstance());
 			textField.setText(temporary_stored_text);
 		}else{
 			textField.setTransformationMethod(null);
 		}
 	}
 	public boolean isSecureTextEntry(){
 		return false;
 	}
 	@Override 
 	public boolean canBecomeFirstResponder(){
 		return YES;
 	}
 	@Override
 	public boolean becomeFirstResponder(){
 		if (!this.isFirstResponder()) {
 			super.becomeFirstResponder();
 			textField.requestFocus();
 			InputMethodManager imm = (InputMethodManager)textField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 			if (!imm.isActive()) {
 				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
 			}
 		}
 		return YES;
 	}
 	@Override
 	public boolean resignFirstResponder(){
 		if (this.isFirstResponder()) {
 			super.resignFirstResponder();
 			textField.clearFocus();
 			InputMethodManager imm = (InputMethodManager)textField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
 			if (imm.isActive()) {
 				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
 			}
 		}
 		return YES;
 	}
 	
 	public String placeholder() {
 		return placeholder;
 	}
 	public void setPlaceholder(String placeholder) {
 		this.placeholder = placeholder;
 		this.textField.setHintTextColor(UIColor.lightGrayColor().getColor());
 		this.textField.setHint(placeholder);
 	}
 	private UITextFieldDelegate delegate = null;
 	
 	public UITextFieldDelegate delegate() {
 		return delegate;
 	}
 	public void setDelegate(UITextFieldDelegate delegate) {
 		this.delegate = delegate;
 	}
 
 	public interface UITextFieldDelegate{
 		/*
 		//TODO other delegate method
 		 * 
 		public boolean textFieldShouldBeginEditing(UITextField textField);
 		public void textFieldDidBeginEditing(UITextField textField);
 		public boolean textFieldShouldEndEditing(UITextField textField);
 		public void textFieldDidEndEditing(UITextField textField);
 		
 		public void shouldChangeCharactersInRange(UITextField textField,NSRange range,String string);
 		public boolean textFieldShouldClear(UITextField textField);
 		*/
 		public boolean textFieldShouldReturn(UITextField textField);
 		
 		/*
 		- (BOOL)textFieldShouldBeginEditing:(UITextField *)textField;        // return NO to disallow editing.
 		- (void)textFieldDidBeginEditing:(UITextField *)textField;           // became first responder
 		- (BOOL)textFieldShouldEndEditing:(UITextField *)textField;          // return YES to allow editing to stop and to resign first responder status. NO to disallow the editing session to end
 		- (void)textFieldDidEndEditing:(UITextField *)textField;             // may be called if forced even if shouldEndEditing returns NO (e.g. view removed from window) or endEditing:YES called
 
 		- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string;   // return NO to not change text
 
 		- (BOOL)textFieldShouldClear:(UITextField *)textField;               // called when clear button pressed. return NO to ignore (no notifications)
 		- (BOOL)textFieldShouldReturn:(UITextField *)textField;              // called when 'return' key pressed. return NO to ignore.
 		*/
 	}
 }
