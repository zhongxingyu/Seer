 package com.wisdom.myapplication;
 
 import android.app.ActionBar;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 public class MainActivity extends Activity {
 
     OnClickListener checkBoxListener;
 
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         checkBoxListener = new OnClickListener() {
             @Override
             public void onClick(View view) {
                 ViewGroup viewGroup = (ViewGroup) view.getParent();
                 viewGroup.removeAllViews();
                 ViewGroup parentViewGroup = (ViewGroup) viewGroup.getParent();
                 parentViewGroup.removeView(viewGroup);
 
             }
         };
        Set<String> stringSet = this.getToDoList();
        if(!(stringSet.isEmpty()))
           for(String toDo : stringSet){
               this.addLayout(toDo);
           }
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         this.saveLayout();
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
 
     public void addItem(View view) {
         // Add Item to the list of items
         EditText editText = (EditText) findViewById(R.id.editText);
         String item = editText.getText().toString();
         editText.setText("");
         this.addLayout(item);
 
     }
 
     public void addLayout(String toDo){
        if(toDo.isEmpty()) return;
         LinearLayout linearLayout = new LinearLayout(this);
         linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
         linearLayout.setOrientation(LinearLayout.HORIZONTAL);
 
         CheckBox checkBox = new CheckBox(this);
         checkBox.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
         checkBox.setOnClickListener(checkBoxListener);
 
         TextView textView = new TextView(this);
         textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                 ViewGroup.LayoutParams.WRAP_CONTENT));
         textView.setGravity(1);
         textView.setText(toDo);
         linearLayout.addView(checkBox);
         linearLayout.addView(textView);
 
         ViewGroup viewGroup = (ViewGroup) findViewById(R.id.wisdom_layout_vertical);
         viewGroup.addView(linearLayout);
     }
 
     public void saveLayout() {
         ViewGroup viewGroup = (ViewGroup) findViewById(R.id.wisdom_layout_vertical);
         TextView textView;
         ViewGroup childViewGroup;
         Set<String> stringSet = new HashSet<String>();
         int length = viewGroup.getChildCount();
         for(int i = 2; i<length; i++){
            childViewGroup  = (ViewGroup) viewGroup.getChildAt(i);
             textView = (TextView)childViewGroup.getChildAt(1);
             stringSet.add(textView.getText().toString());
         }
         this.saveToDoList(stringSet);
     }
 
     public void saveToDoList(Set<String> stringSet) {
         SharedPreferences sharedPreferences = this.getPreferences(this.MODE_PRIVATE);
         SharedPreferences.Editor editor = sharedPreferences.edit();
         editor.clear();
         editor.apply();
         editor.putStringSet("todo", stringSet);
         editor.commit();
 
 
     }
 
     public Set<String> getToDoList() {
         SharedPreferences sharedPreferences = this.getPreferences(this.MODE_PRIVATE);
         Set<String> stringSet = new HashSet<String>();
        return sharedPreferences.getStringSet("todo", stringSet);
 
     }
 
 
     
 }
