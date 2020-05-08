 package com.radianapps.linalg.views;
 
 import android.content.Context;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.RelativeLayout;
 import com.radianapps.linalg.MatrixController;
 import com.radianapps.linalg.R;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Tushar
  * Date: 9/9/13
  * Time: 10:26 PM
  * To change this template use File | Settings | File Templates.
  */
 
 public class MatrixIME extends RelativeLayout {
 
     private MatrixController matrixController;
     final private int DECIMAL = -1;
     enum Nav { UP, RIGHT, DOWN, LEFT };
 
     public MatrixIME(Context context, AttributeSet attrs) {
         super(context, attrs);
 
         LayoutInflater inflater = LayoutInflater.from(context);
         inflater.inflate(R.layout.inputmethod, this, true);
 
         setupNumPad();
         setupNavigation();
     }
 
     private void setupNavigation() {
         OnClickListener navHandler = new OnClickListener() {
             @Override
             public void onClick(View v) {
                 switch (v.getId()) {
                     case R.id.upArrow:
                         navInput(Nav.UP);
                         break;
                     case R.id.rightArrow:
                         navInput(Nav.RIGHT);
                         break;
                     case R.id.downArrow:
                         navInput(Nav.DOWN);
                         break;
                     case R.id.leftArrow:
                         navInput(Nav.LEFT);
                         break;
                 }
             }
         };
 
         findViewById(R.id.upArrow).setOnClickListener(navHandler);
         findViewById(R.id.rightArrow).setOnClickListener(navHandler);
         findViewById(R.id.downArrow).setOnClickListener(navHandler);
         findViewById(R.id.leftArrow).setOnClickListener(navHandler);
     }
 
    private void navInput(Nav nav) {
        broadcastNavInput(nav);
    }

    private void numericInput(int number) {
        broadcastNumericInput(number);
    }

     private void setupNumPad() {
         ViewGroup numPad = (ViewGroup) findViewById(R.id.numPad);
 
         OnClickListener numHandler = new OnClickListener() {
             @Override
             public void onClick(View v) {
                 numericInput(numPadButtonToInt(v));
             }
         };
 
         findViewById(R.id.numberZero).setOnClickListener(numHandler);
         findViewById(R.id.numberOne).setOnClickListener(numHandler);
         findViewById(R.id.numberTwo).setOnClickListener(numHandler);
         findViewById(R.id.numberThree).setOnClickListener(numHandler);
         findViewById(R.id.numberFour).setOnClickListener(numHandler);
         findViewById(R.id.numberFive).setOnClickListener(numHandler);
         findViewById(R.id.numberSix).setOnClickListener(numHandler);
         findViewById(R.id.numberSeven).setOnClickListener(numHandler);
         findViewById(R.id.numberEight).setOnClickListener(numHandler);
         findViewById(R.id.numberNine).setOnClickListener(numHandler);
         findViewById(R.id.numberDecimal).setOnClickListener(numHandler);
     }
 
     private int numPadButtonToInt(View v) {
         int number = 0;
         switch(v.getId()) {
             case R.id.numberZero:
                 number = 0;
                 break;
             case R.id.numberOne:
                 number = 1;
                 break;
             case R.id.numberTwo:
                 number = 2;
                 break;
             case R.id.numberThree:
                 number = 3;
                 break;
             case R.id.numberFour:
                 number = 4;
                 break;
             case R.id.numberFive:
                 number = 5;
                 break;
             case R.id.numberSix:
                 number = 6;
                 break;
             case R.id.numberSeven:
                 number = 7;
                 break;
             case R.id.numberEight:
                 number = 8;
                 break;
             case R.id.numberNine:
                 number = 9;
                 break;
             case R.id.numberDecimal:
                 number = DECIMAL;
                 break;
         }
 
         return number;
     }
 
     private void broadcastNumericInput(int number) {
         if (matrixController != null) {
             // TODO:    matrixController.numericInput(number);
         }
     }
 
     private void broadcastNavInput(Nav nav) {
         if (matrixController != null) {
             // TODO:    matrixController.navInput(nav);
         }
     }
 
    private void registerController(MatrixController matrixController) {
         this.matrixController = matrixController;
     }
 
    private void unregisterController() {
         this.matrixController = null;
     }
 }
