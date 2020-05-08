 package com.agiliq.anagen;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AnagramsActivity extends Activity {
 
 	private static String TAG = "AnagramsActivity.", inputWord;
 	private TextView inputWordField;
 	private Spinner max_words;
 	private Spinner min_characters;
 	private ListView anagramsList;
 	private TreeSet<String> anagramsSet, permutationSet;
 	private Iterator<String> itr;
 	Stack<String> correct;
 		
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_anagrams);
         
         String localTAG = TAG.concat("onCreate");
         
         inputWordField = (TextView) findViewById(R.id.inputWordField);
         max_words= (Spinner) findViewById(R.id.maximum_words);
         min_characters= (Spinner) findViewById(R.id.minimum_characters);
         
         anagramsList=(ListView)findViewById(R.id.anagramslist);
 	        TextView listViewHeader=new TextView(this);
 	        listViewHeader.setText("  Anagrams:");
         anagramsList.addHeaderView(listViewHeader);
         
         Intent intent=getIntent();
         	Log.d(localTAG, intent.toString());
         ArrayList<String> recievedAnagrams=intent.getStringArrayListExtra("com.agiliq.anagen."+MainActivity.TAG+"-anagrams");
         	Log.d(localTAG+"-recievedAnagrams", recievedAnagrams.toString());
         
         ArrayAdapter<String> anagramsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, recievedAnagrams);
         	Log.d(localTAG+"-anagramsAdapter", anagramsAdapter.toString());
         
     	anagramsList.setAdapter(anagramsAdapter);
     }
 	
 	public void getAnagramsButtonHandler(View getAnagramsCandidateButton){
 		String localTAG= TAG.concat("getAnagramsCandidateButtonHandler");
 			Log.d(localTAG, "getAnagramsCandidateButton pressed");
 		inputWord = inputWordField.getText().toString().toLowerCase();
 			Log.d(localTAG, "inputWord = " + inputWord);
 			
 			if(inputWord.equals("")){		//Empty Field Validation
 				Toast.makeText(this, "Please enter a word", Toast.LENGTH_LONG).show();
 				return;
 			}
 		
 		Integer maximumWords, minimumCharacters;
 		try{
 			maximumWords= Integer.parseInt(max_words.getSelectedItem().toString());
 				Log.d(localTAG+".maximumWords", maximumWords.toString());
 			minimumCharacters= Integer.parseInt(min_characters.getSelectedItem().toString());
 				Log.d(localTAG+".minimumCharacters", minimumCharacters.toString());
 		} catch(NumberFormatException e){
 			Toast.makeText(this, "Select a value for the characters and words", Toast.LENGTH_LONG).show();
 			return;
 		}
 		
 		String joinedPhrase=inputWord.replaceAll("[^a-z]+", "");	//Eliminated special characters
 			Log.d(localTAG, "joinedPhrase = " + joinedPhrase);
 		
 		permutationSet= new TreeSet<String>();
 		getAllPermutations("", joinedPhrase);
 			Log.d(localTAG, permutationSet.toString());
 		
 		anagramsSet=new TreeSet<String>();
 			
 		String poppedoutString;
 		int length_of_stack_contents;
 		Stack<String> temp;
 		itr=permutationSet.iterator();
 		while(itr.hasNext()){
 			correct=new Stack<String>();
 			length_of_stack_contents=0;
 			getAnagrams(itr.next());
 //				Log.d(localTAG+"-stack", correct.toString());
 			temp=new Stack<String>();
 			int count_of_words=0;
 			while(!correct.empty()){
 				poppedoutString=correct.pop();
 				if(poppedoutString.length() < minimumCharacters){
 					length_of_stack_contents=0;
 					break;
 				}
 				count_of_words++;
 				length_of_stack_contents+=poppedoutString.length();
 				temp.push(poppedoutString);
 			}
 			if(length_of_stack_contents==joinedPhrase.length() && count_of_words <= maximumWords){
 //				Log.d(localTAG+"-stack", temp.toString());
 				String anagram="";
 				for(int i=0; i<count_of_words; i++){
 					anagram=anagram.concat(temp.pop()+" ");
 				}
 				anagram=anagram.trim();
 				anagramsSet.add(anagram);
 					Log.d(localTAG+"-anagram", anagram);
 			}			
 		}
 		Log.d(localTAG+"-anagramsSet", anagramsSet.toString());
 			
 		ArrayList<String> newAnagrams= new ArrayList<String>(anagramsSet);
 			Log.d(localTAG, newAnagrams.toString());
 		
 		ArrayAdapter<String> anagramsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, newAnagrams);
     		Log.d(localTAG+"-anagramsAdapter", anagramsAdapter.toString());
     		
 		if(anagramsAdapter.isEmpty()){
 			anagramsList.setVisibility(View.INVISIBLE);
 			Toast.makeText(this, "Sorry. No Anagrams found.", Toast.LENGTH_LONG).show();
 		} else{
 			anagramsList.setVisibility(View.VISIBLE);
 			anagramsList.setAdapter(anagramsAdapter);
 		}
 	}
 //	getAllPermutations() performs permutations of the joinedPhrase.
 	private void getAllPermutations(String prefix, String str) {
 		int n = str.length();
 		if (n == 0){
 			permutationSet.add(prefix);
 		}
 		else {
 			for (int i = 0; i < n; i++)
 				getAllPermutations(prefix + str.charAt(i),	str.substring(0, i) + str.substring(i + 1, n));
 		}
 	}
 	
 	private void getAnagrams(String str){
 		String first, last=null;
 		int stringLength=str.length();
 		
 		for(int i=0; i<str.length(); i++, stringLength--){
 			first=str.substring(0, stringLength);
 			if(stringLength!=str.length())
 				last=str.substring(stringLength);
 			
 			if(MainActivity.wordListSet.contains(first)){
 				correct.push(first);
 				if(stringLength!=str.length())
 					getAnagrams(last);
 				break;
 			}
 		}
 	}
 }
