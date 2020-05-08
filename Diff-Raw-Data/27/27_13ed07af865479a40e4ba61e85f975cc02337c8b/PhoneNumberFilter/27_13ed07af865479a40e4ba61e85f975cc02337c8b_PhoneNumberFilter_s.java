 package edu.gatech.oad.rocket.findmythings.util;
 
 import android.text.InputType;
 import android.text.Spanned;
 import android.text.method.NumberKeyListener;
 
 /**
  * Validates phone numbers
  * User: zw
  * Date: 4/18/13
  * Time: 2:02 AM
  */
 public class PhoneNumberFilter extends NumberKeyListener {
 
 	@Override
 	public int getInputType() {
 		return InputType.TYPE_CLASS_PHONE;
 	}
 
 	@Override
 	protected char[] getAcceptedChars() {
 		return new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' };
 	}
 
 	@Override
 	public CharSequence filter(CharSequence source, int start, int end,
 							   Spanned dest, int dstart, int dend) {

 		if (end > start) {
			String destTxt = dest.toString();
			String resultingTxt = destTxt.substring(0, dstart) + source.subSequence(start, end) + destTxt.substring(dend);

			// Phone number must match x-xxx-xxx-xxxx
			if (!resultingTxt.matches ("^[0-9]-([0-9]{3})-[0-9]{3}-[0-9]{4}")) {
 				return "";
 			}
 		}
		return null;
 	}
 }
