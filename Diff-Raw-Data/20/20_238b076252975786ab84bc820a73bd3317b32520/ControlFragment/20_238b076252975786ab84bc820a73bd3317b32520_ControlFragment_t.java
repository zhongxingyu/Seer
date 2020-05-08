 package de.haw.shc;
 
 import android.app.Fragment;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import de.haw.shc.utils.buttonFactory.ButtonListenerFactory;
 import de.haw.shc.utils.context.Room;
 import de.haw.shc.utils.control.Control;
 import de.haw.shc.utils.buttonFactory.ViewTransportTyp;
 import de.haw.shc.utils.fileAgent.FileAgent;
 
 import java.util.Arrays;
 
 public class ControlFragment extends Fragment {
 
     private static final String LOG_TAG = "ControlFragment";
 
     public static final String CONTEXT = "context";
     public static final String CONTROL = "control";
     private static final String BUTTONFACTORY = "BUTTONFACTORY";
 
     private Room mContext;
     private Control mControl;
     private ButtonListenerFactory buttonListenerFactory;
     private SharedPreferences colorPrefs;
 
 
     public ControlFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         colorPrefs = this.getActivity().getSharedPreferences("color1Prefs", Context.MODE_PRIVATE);
 
         super.onCreate(savedInstanceState);
 
         buttonListenerFactory = new ButtonListenerFactory();
         if (savedInstanceState == null) {
 
 
             Log.d(LOG_TAG, "erstelle die factory");
 
             if (getArguments().containsKey(CONTEXT)) {
                 mContext = (Room) (getArguments().getSerializable(CONTEXT));
             }
             if (getArguments().containsKey(CONTROL)) {
                 mControl = (Control) (getArguments().getSerializable(CONTROL));
             }
         } else {
 
             //buttonListenerFactory =  (ButtonListenerFactory) savedInstanceState.getSerializable(BUTTONFACTORY);
             mContext = (Room) savedInstanceState.getSerializable(CONTEXT);
             mControl = (Control) savedInstanceState.getSerializable(CONTROL);
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
 
         //outState.putSerializable(BUTTONFACTORY,buttonListenerFactory);
         outState.putSerializable(CONTEXT, mContext);
         outState.putSerializable(CONTROL, mControl);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
 
 
 //		if (mContext != null && mControl != null) {
 //			TextView tw = (TextView) rootView.findViewById(R.id.control);
 //			tw.setText(mContext + " " + mControl);
 //		}
 
         View view = null;
 
 
         Log.d(LOG_TAG, "Before if context:" + mContext + " Control:" + mControl + " view:" + view);
         if (mControl.equals(Control.LIGHT)) {
 
             view = inflater.inflate(R.layout.light_layout, container, false);
             createListenerForView(view);
 
            view.findViewById(R.id.ColorFav1).setBackgroundColor(colorPrefs.getInt("color0",-65465));
            view.findViewById(R.id.ColorFav2).setBackgroundColor(colorPrefs.getInt("color1",-65465));
            view.findViewById(R.id.ColorFav3).setBackgroundColor(colorPrefs.getInt("color2",-65465));
            view.findViewById(R.id.ColorFav4).setBackgroundColor(colorPrefs.getInt("color3",-65465));
 
            Log.d(LOG_TAG,"loaded value is "+ colorPrefs.getInt("color0",0xFF0000));
 
         } else if (mControl.equals(Control.CURTAIN)) {
 
             view = inflater.inflate(R.layout.curtains_layout, container, false);
             createListenerForView(view);
         } else if (mControl.equals(Control.BLINDS)) {
 
             view = inflater.inflate(R.layout.blinds_layout, container, false);
             createListenerForView(view);
         } else if (mControl.equals(Control.WINDOW)) {
 
             view = inflater.inflate(R.layout.window_layout, container, false);
             createListenerForView(view);
         } else if (mControl.equals(Control.HEATING)) {
 
             view = inflater.inflate(R.layout.heating_layout, container, false);
             createListenerForView(view);
         } else {
             Log.d(LOG_TAG, "CANT MATCH mControl value " + mControl);
         }
 
 
         return view;
     }
 
 
     private void createListenerForView(View view) {
         Log.d(LOG_TAG, "rufe factory auf mit context:" + mContext + " Controls:" + mControl + " view:" + view);
         buttonListenerFactory.setListenerForView(new ViewTransportTyp(mContext, mControl, view));
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
 
         if (mControl == Control.LIGHT) {
             int[] colors = new int[4];
             Log.d(LOG_TAG, "activiti is " + this.getActivity());
             Log.d(LOG_TAG, "view is" + getView());
             Log.d(LOG_TAG, " textView is " + getView().findViewById(R.id.ColorFav1));
             Log.d(LOG_TAG, " background is " + ((TextView) (getView().findViewById(R.id.ColorFav1))).getBackground());
 
 
             colors[0] = ((ColorDrawable) (((TextView) (getView().findViewById(R.id.ColorFav1))).getBackground())).getColor();
             colors[1] = ((ColorDrawable) (((TextView) (getView().findViewById(R.id.ColorFav2))).getBackground())).getColor();
             colors[2] = ((ColorDrawable) (((TextView) (getView().findViewById(R.id.ColorFav3))).getBackground())).getColor();
             colors[3] = ((ColorDrawable) (((TextView) (getView().findViewById(R.id.ColorFav4))).getBackground())).getColor();
 
             Log.d(LOG_TAG,"colors are " + Arrays.toString(colors));
             editeColorPref(colors);
         }
     }
 
 
     private void editeColorPref(int[] vaules) {
         SharedPreferences.Editor editor = colorPrefs.edit();
 
         editor.putInt("color0", vaules[0]);
         editor.putInt("color1", vaules[1]);
         editor.putInt("color2", vaules[2]);
         editor.putInt("color3", vaules[3]);
         editor.commit();
     }
 
 
 }
