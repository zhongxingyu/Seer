 package org.apache.cordova.core;
 
 import org.json.JSONArray;
 
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
 
 import android.content.Context;
 import android.view.inputmethod.InputMethodManager;
 
 public class Keyboard extends CordovaPlugin {
     public Keyboard() {
     }
 
     @Override
     public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
         if (action.equals("show")) {
            this.showKeyboard();
             callbackContext.success("done");
             return true;
         } 
         else if (action.equals("hide")) {
            this.hideKeyboard();
             callbackContext.success();
             return true;
         }
         else {
             return false;
         }
     }
 
     public void showKeyboard() {
         InputMethodManager mgr = (InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         mgr.showSoftInput(webView, InputMethodManager.SHOW_IMPLICIT);
         
         ((InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(webView, 0); 
     }
 
    public void hideKeyboard() {
         InputMethodManager mgr = (InputMethodManager) cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         mgr.hideSoftInputFromWindow(webView.getWindowToken(), 0);
     }
 }
