 package com.fun.midworx.com.fun.midworx.views;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.AttributeSet;
 import android.view.View;
 import android.widget.LinearLayout;
 
 import com.fun.midworx.DictionaryActivity;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by Rotem on 29/06/13.
  */
 public class BoxesContainer extends LinearLayout {
     private LayoutParams params;
     private HashMap<Integer, WordsBox> mBoxes = new HashMap<Integer,WordsBox>();
 
     public BoxesContainer(Context context, AttributeSet attrs) {
         super(context, attrs);
         params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
         params.setMargins(2, 0, 2, 0);
     }
 
    public void clear() {
        removeAllViews();
        mBoxes.clear();
    }

     public boolean guessWord(String word) {
         WordsBox box = mBoxes.get(word.length());
         if (box != null)
             return box.guessWord(word);
         return false;
     }
 
     public void addBox(ArrayList<String> words) {
         WordsBox box = new WordsBox(getContext(),words);
         addView(box, params);
         mBoxes.put(box.getLettersNum(), box);
     }
 
     public void showUnguessed() {
         for (WordsBox box: mBoxes.values()) {
             box.showUnguessed();
         }
     }
 
     public void permitDefinitions() {
         for (Map.Entry<Integer, WordsBox> entry : this.mBoxes.entrySet()) {
             entry.getValue().permitDefinition();
         }
     }
 }
