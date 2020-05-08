 package com.ell.MemoRazor.adapters;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import com.ell.MemoRazor.R;
 import com.ell.MemoRazor.data.Word;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 public class WordAdapter extends ArrayAdapter<Word> {
     private ArrayList<Word> objects;
 
     public WordAdapter(Context context, int resource, ArrayList<Word> objects) {
         super(context, resource, objects);
         this.objects = objects;
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         if (convertView == null) {
             LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.word_layout, null);
         }
 
         Word word = objects.get(position);
 
         if (word != null) {
             TextView wordNameTextView = (TextView)convertView.findViewById(R.id.word_name_text);
             TextView wordAddedTextView = (TextView)convertView.findViewById(R.id.word_added_text);
             TextView wordMeaningTextView = (TextView)convertView.findViewById(R.id.word_meaning_text);
             TextView wordTranscriptionTextView = (TextView)convertView.findViewById(R.id.word_transcription_text);
             TextView wordFetchStatusView = (TextView)convertView.findViewById(R.id.word_fetchstatus_text);
             ProgressBar wordFetchProgress = (ProgressBar)convertView.findViewById(R.id.word_fetch_progress);
 
             DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
             wordNameTextView.setText(word.getName());
             wordAddedTextView.setText(dateFormat.format(word.getCreatedDate()));
 
             String meaning = word.getMeaning();
             if (meaning == null) {
                 wordMeaningTextView.setVisibility(View.GONE);
             } else {
                 wordMeaningTextView.setText(meaning);
                 wordMeaningTextView.setVisibility(View.VISIBLE);
             }
 
             String transcription = word.getTranscription();
            if (transcription == null) {
                 wordTranscriptionTextView.setVisibility(View.GONE);
             } else {
                 wordTranscriptionTextView.setText(transcription);
                 wordTranscriptionTextView.setVisibility(View.VISIBLE);
             }
 
             if (!(word.getFetchingPlayback() || word.getFetchingTranslation())) {
                 wordFetchStatusView.setVisibility(View.GONE);
                 wordFetchProgress.setVisibility(View.INVISIBLE);
             } else {
                 wordFetchStatusView.setText(word.getFetchingTranslation() ? R.string.translation_expected : R.string.playback_expected);
                 wordFetchStatusView.setVisibility(View.VISIBLE);
                 wordFetchProgress.setVisibility(View.VISIBLE);
             }
         }
 
         return convertView;
     }
 }
