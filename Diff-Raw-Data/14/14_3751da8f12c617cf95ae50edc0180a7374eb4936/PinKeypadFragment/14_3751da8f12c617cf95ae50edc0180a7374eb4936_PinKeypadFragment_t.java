 package com.example.msims.keypad;
 
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Checkable;
 import android.widget.Toast;
 
 public class PinKeypadFragment extends DialogFragment implements NumericKeypad.KeyPressedListener {
 
     public static final  int    PIN_BUFFER_MAX_LENGTH = 4;
     private static final String PIN_BUFFER_KEY        = "pinBuffer";
     public static final int CLEAR_PIN_DELAY_IN_MILLIS = 100;
    private View pinError;
 
     public enum Arguments {
         pinToMatchAgainst
     }
 
     private String pinBuffer = "";
     private Checkable[] pinBoxes;
 
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         Log.e("flubber", "I was created!");
         super.onCreate(savedInstanceState);
 
         /* Validate arguments */
         if (getArguments() == null || getArguments().getString(Arguments.pinToMatchAgainst.toString()) == null) {
             throw new IllegalStateException("getArguments() null or " + Arguments.pinToMatchAgainst + " not provided");
         }
 
         if (savedInstanceState != null) {
             pinBuffer = savedInstanceState.getString(PIN_BUFFER_KEY);
             Log.e("flubber", "pinBuffer is " + pinBuffer);
         }
 
         /* Disable any automatic styling */
         setStyle(android.app.DialogFragment.STYLE_NO_FRAME, 0);
     }
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         View layout = inflater.inflate(R.layout.pin_keypad, container, false);
 
         ((NumericKeypad) layout.findViewById(R.id.keypad)).setKeyPressedListener(this);
         layout.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
             public void onClick(final View view) {
                 dismiss();
             }
         });
 
         pinBoxes = new Checkable[]{(Checkable) layout.findViewById(R.id.pinBoxOne), (Checkable) layout.findViewById(R.id.pinBoxTwo),
                                    (Checkable) layout.findViewById(R.id.pinBoxThree), (Checkable) layout.findViewById(R.id.pinBoxFour)};
         refreshPinBoxesState();
 
        pinError = layout.findViewById(R.id.pinError);
         return layout;
     }
 
     public void refreshPinBoxesState() {
         for (int i = 0; i < pinBoxes.length; i++) {
             pinBoxes[i].setChecked(i < pinBuffer.length());
         }
     }
 
     @Override
     public void onSaveInstanceState(final Bundle outState) {
         super.onSaveInstanceState(outState);
         if (pinBuffer != null) {
             outState.putString(PIN_BUFFER_KEY, pinBuffer);
         }
     }
 
     public void onKeyPressed(final String key) {

        /* Hide error message if it's showing */
        if (pinError.isShown()) {
            pinError.setVisibility(View.GONE);
        }

        /* Update pin buffer and refresh box state */
         if (key != null) {
             if (pinBuffer.length() < PIN_BUFFER_MAX_LENGTH) {
                 pinBuffer += key;
             }
         } else if (pinBuffer.length() > 0) { //Handle backspace if pinBuffer isn't empty
             pinBuffer = pinBuffer.substring(0, pinBuffer.length() - 1);
         }
         refreshPinBoxesState();
 
         /* Validate pin if it's long enough */
         if (pinBuffer.length() == PIN_BUFFER_MAX_LENGTH) {
             if (pinBuffer.equals(getArguments().getString(Arguments.pinToMatchAgainst.toString()))) {
                 Toast.makeText(getActivity(), "You so good!!", Toast.LENGTH_SHORT).show();
             } else {
                pinError.setVisibility(View.VISIBLE);
             }
             getView().postDelayed(new Runnable() {
                 public void run() {
                     pinBuffer = "";
                     refreshPinBoxesState();
                 }
             }, CLEAR_PIN_DELAY_IN_MILLIS);
         }
     }
 
 }
