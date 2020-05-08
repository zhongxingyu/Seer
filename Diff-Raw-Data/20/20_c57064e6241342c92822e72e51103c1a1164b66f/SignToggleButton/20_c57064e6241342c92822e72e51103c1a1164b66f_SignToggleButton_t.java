 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.views;
 
 import java.math.BigDecimal;
 
 import net.alexjf.tmm.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.Button;
 
 public class SignToggleButton extends Button {
     private static final String KEY_POSITIVE = "positive";
 
     private int positiveColor;
     private int negativeColor;
     private boolean positive = true;
 
     public SignToggleButton(Context context) {
         super(context);
         initialize(context, null);
     }
 
     public SignToggleButton(Context context, AttributeSet attrs) {
         super(context, attrs);
         initialize(context, attrs);
     }
 
     public SignToggleButton(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initialize(context, attrs);
     }
 
     protected void initialize(Context context, AttributeSet attrs) {
         Resources resources = context.getResources();
         positiveColor = resources.getColor(R.color.positive);
         negativeColor = resources.getColor(R.color.negative);
 
         if (attrs != null) {
             TypedArray attributeArray = getContext().obtainStyledAttributes(attrs,
                      R.styleable.SignToggleButton);
             positive = attributeArray.getBoolean(
                     R.styleable.SignToggleButton_positive, true);
             attributeArray.recycle();
         }
 
        setValue(positive);
 
         setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View arg0) {
                 toggleSign();
             }
 
         });
     }
 
     public void toggleSign() {
         if (positive) {
             setNegative();
         } else {
             setPositive();
         }
     }
 
     public void setValue(boolean positive) {
         if (positive) {
             setPositive();
         } else {
             setNegative();
         }
     }
 
     public void setToNumberSign(BigDecimal number) {
         positive = number.signum() >= 0;
         setValue(positive);
     }
 
     public void setPositive() {
         setText("+");
         setTextColor(positiveColor);
         positive = true;
     }
 
     public boolean isPositive() {
         return positive;
     }
 
     public void setNegative() {
         setText("−");
         setTextColor(negativeColor);
         positive = false;
     }
 
     public boolean isNegative() {
         return !positive;
     }
 
     @Override
     public Parcelable onSaveInstanceState() {
         Bundle bundle = new Bundle();
         bundle.putParcelable("instanceState", super.onSaveInstanceState());
         bundle.putBoolean(KEY_POSITIVE, positive);
 
         return bundle;
     }
 
     @Override
     public void onRestoreInstanceState(Parcelable state) {
         if (state instanceof Bundle) {
             Bundle bundle = (Bundle) state;
             setValue(bundle.getBoolean(KEY_POSITIVE));
             super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
             return;
         }
 
         super.onRestoreInstanceState(state);
     }
 
 }
