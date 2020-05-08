 package com.menst_verstka.composite;
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.menst_verstka.R;
 
 /**
  * Created by turbo_lover on 21.08.13.
  */
 public class symptomElement extends LinearLayout implements View.OnClickListener {
     /* Элемент который загружаем в таблицу симптомов */
     private ImageView image;
     ImageView [] circles;
     private ImageView circle1;
     private ImageView circle2;
     private ImageView circle3;
     private TextView text;
     private int currentValue = 0;
 
     public symptomElement(Context context, AttributeSet attrs) {
         super(context, attrs);
         initializeVariables();
     }
 
     public symptomElement(Context context) {
         super(context);
         initializeVariables();
         setListeners();
     }
 
     private void setListeners() {
         setOnClickListener(this);
         circle1.setOnClickListener(this);
         circle2.setOnClickListener(this);
         circle3.setOnClickListener(this);
     }
 
     private void initializeVariables() {
 
         LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         li.inflate(R.layout.symptom_element,this);
 
         image = (ImageView) findViewById(R.id.elementImage);
         text = (TextView) findViewById(R.id.elementText);
 
         circle1 =(ImageView)this.findViewById(R.id.red_circles_1);
         circle2 =(ImageView)this.findViewById(R.id.red_circles_2);
         circle3 =(ImageView)this.findViewById(R.id.red_circles_3);
         circles = new ImageView[]{circle1,circle2,circle3};
     }
     /* setters starts here*/
     public void setCurrentValue(int currentValue) {
         this.currentValue = currentValue;
     }
     public void setText(CharSequence text) {
         this.text.setText(text);
     }
 
     public void setImage(int id) {
         image.setImageResource(id);
     }
     /* setters ends*/
 
     /* override method starts here*/
     @Override
     public void onClick(View view) {
         final int id = this.getId();
 
         switch (view.getId()) {
             case R.id.red_circles_1:
                 setCircles(1);
                 break;
             case R.id.red_circles_2:
                 setCircles(2);
                 break;
             case R.id.red_circles_3:
                 setCircles(3);
                 break;
             default :
                 setCircles(0);
                 break;
        }
     }
     /* override method STOPS here*/
 
     /* my method */
     private void setCircles(int currentValue) {
             for (ImageView v: circles)
                 v.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.symptom_red_circle));
             for (int i =0; i<(currentValue);i++)
                 circles[i].setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.symptom_red_circle_checked));
 
     }
 }
