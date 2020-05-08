 package com.colabug.calc;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 /**
  * @since 1.0
  */
 public class DisplayFragment extends Fragment
 {
     private View     layout;
     private EditText display;
 
     @Override
     public View onCreateView( LayoutInflater inflater,
                               ViewGroup container,
                               Bundle savedInstanceState )
     {
         super.onCreateView( inflater, container, savedInstanceState );
 
        layout = inflater.inflate( R.layout.calculator, container, false );
 
         configureDisplay();
 
         return layout;
     }
 
     private void configureDisplay()
     {
         display = (EditText) layout.findViewById( R.id.display );
         clearDisplayedValue();
     }
 
     public void clearDisplayedValue()
     {
         setDisplay( getResources().getString( R.string.EMPTY_STRING ) );
     }
 
     public void setDisplay( Object result )
     {
         display.setText( String.valueOf( result ) );
     }
 
     public String getValue()
     {
         return display.getText().toString();
     }
 
     public boolean isDisplayingOperation()
     {
         String value = getValue();
         return value.equals( getString( R.string.plus ) ) ||
                value.equals( getString( R.string.minus ) ) ||
                value.equals( getString( R.string.multiply ) ) ||
                value.equals( getString( R.string.divide ) ) ||
                value.equals( getString( R.string.modulo ) );
     }
 }
