 package org.gs.campusparty;
 
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.graphics.drawable.Drawable;
 import android.widget.TextView;
 
 public class Ghost {
     int time;
     int[] chips;
     TextView label;
     TextView chiplabel;
     
     public Ghost() {
         time = -1;
         chips = new int[Field.C_COUNT];
     }
     
     public void step() {
         String chiptext = "";
         for(int i = 0; i < Field.C_COUNT; i++) {
             for(int j = 0; j < chips[i]; j++) {
                 chiptext += Field.names[i];
             }
         }
         if(chiptext.length() == 0) {
             time = -1;
             Field.getSingleton().kills += 1;
         }
         chiplabel.setText(chiptext);
         label.setTextColor(Color.RED);
         time -= 1;
        label.setText(Integer.toString(time));
     }
 }
