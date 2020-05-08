 package com.mike.comicreeder.components;
 
 import android.content.Context;
 import android.content.res.TypedArray;
import android.support.v7.appcompat.R;
 import android.util.AttributeSet;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Created by michaelnapoleon on 10/21/13.
  */
 public class FloatingLabelEditText extends LinearLayout {
 
     TextView label;
     EditText textField;
 
     public FloatingLabelEditText(Context context, AttributeSet attrs) {
         super(context, attrs);
 
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable);
 
         this.setOrientation(VERTICAL);
     }
 
 
 }
