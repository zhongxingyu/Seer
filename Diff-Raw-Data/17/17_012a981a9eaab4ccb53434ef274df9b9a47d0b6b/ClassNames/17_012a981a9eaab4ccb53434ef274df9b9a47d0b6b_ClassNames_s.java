// Copyright 2011 Google Inc. All Rights Reserved.
 
 package com.google.android.testing.nativedriver.client;
 
 /**
  * Contains constants for class names of Android runtime classes. Since Android
  * classes are not compiled into the client jar, they must be referred to by
  * name. These constants free you from typing the entire package name and make
  * typos less likely to cause a problem.
  *
  * <p>This list can be expanded as-needed.
  *
  * @see org.openqa.selenium.internal.FindsByClassName
  * @author Matthew DeVore
  */
 public interface ClassNames {
   /**
    * @see android.widget.Button
    */
   String BUTTON  = "android.widget.Button";
 
   /**
    * @see android.widget.CheckBox
    */
   String CHECKBOX = "android.widget.CheckBox";
 
   /**
    * @see android.widget.CheckedTextView
    */
   String CHECKED_TEXT_VIEW = "android.widget.CheckedTextView";
 
   /**
    * @see android.widget.EditText
    */
   String EDIT_TEXT = "android.widget.EditText";
 
   /**
    * @see android.widget.RadioButton
    */
   String RADIO_BUTTON = "android.widget.RadioButton";
 
   /**
    * @see android.widget.TextView
    */
   String TEXT_VIEW = "android.widget.TextView";
 
   /**
    * @see android.widget.ToggleButton
    */
   String TOGGLE_BUTTON = "android.widget.ToggleButton";
 
   /**
    * @see android.view.View
    */
   String VIEW = "android.view.View";
 
   /**
    * @see android.webkit.WebView
    */
   String WEBVIEW = "android.webkit.WebView";
 }
