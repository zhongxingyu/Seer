 package com.mike.comicreeder.components;
 
 import android.content.Context;
 import android.content.res.TypedArray;
 import android.util.AttributeSet;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
import com.mike.comicreeder.R;

 /**
  * Created by michaelnapoleon on 10/21/13.
  */
 public class FloatingLabelEditText extends LinearLayout {
 
     TextView label;
     EditText textField;
    String hintText;
    String floatingLabelText;
 
     public FloatingLabelEditText(Context context, AttributeSet attrs) {
         super(context, attrs);
 
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FloatingLabelEditText);
        hintText = a.getString(R.styleable.FloatingLabelEditText_editTextHint);
        floatingLabelText = a.getString(R.styleable.FloatingLabelEditText_floatingLabelText);
 
         this.setOrientation(VERTICAL);

        this.textField.setHint(hintText);
        this.label.setText(floatingLabelText);
     }
 
 
 }
