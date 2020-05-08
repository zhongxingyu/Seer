 package no.hist.aitel.android.tictactoe;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class OptionsActivity extends Activity {
 
     public static final String PREFS_NAME = "Prefs";
    public static final int MIN_BOARDSIZE = 3;
     private Spinner boardsizeSpinner;
     private Spinner inRowSpinner;
     private int boardSize;
     private int inRow;
     private ArrayAdapter<Integer> inRowAdapter;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.options);
         boardsizeSpinner = (Spinner) findViewById(R.id.spinner_boardsize);
         ArrayAdapter<CharSequence> boardSizeAdapter = ArrayAdapter.createFromResource(this, R.array.boardsize_array, android.R.layout.simple_spinner_item);
         boardSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         boardsizeSpinner.setAdapter(boardSizeAdapter);
         boardsizeSpinner.setOnItemSelectedListener(new BoardSizeSelectionListener());
         inRowSpinner = (Spinner) findViewById(R.id.spinner_inarow);
         inRowAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item);
         inRowAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         inRowSpinner.setAdapter(inRowAdapter);
        inRowSpinner.setOnItemSelectedListener(new InarowSelectionListener());        
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boardSize = settings.getInt("boardSize", MIN_BOARDSIZE);
        boardsizeSpinner.setSelection(boardSize - MIN_BOARDSIZE);
        inRow = settings.getInt("inRow", boardSize);
        inRowSpinner.setSelection(inRow - MIN_BOARDSIZE);


     }
 
     private class BoardSizeSelectionListener implements AdapterView.OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
             boardSize = pos + 3;
             updateInarowAdapter();
            inRowSpinner.setSelection(inRow - 3);
         }
 
         public void onNothingSelected(AdapterView parent) {
         }
     }
 
     private class InarowSelectionListener implements AdapterView.OnItemSelectedListener {
 
         public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
             inRow = (Integer) parent.getItemAtPosition(pos);
         }
 
         public void onNothingSelected(AdapterView parent) {
         }
     }
 
     private void updateInarowAdapter() {
         inRowAdapter.clear();
         for (int i = 3; i <= boardSize; i++) {
             inRowAdapter.add(new Integer(i));
         }
     }
 
    @Override
     protected void onStop() {
         super.onStop();
         SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
         SharedPreferences.Editor editor = settings.edit();
         editor.putInt("boardSize", boardSize);
         editor.putInt("inRow", inRow);
         editor.commit();
     }
 }
