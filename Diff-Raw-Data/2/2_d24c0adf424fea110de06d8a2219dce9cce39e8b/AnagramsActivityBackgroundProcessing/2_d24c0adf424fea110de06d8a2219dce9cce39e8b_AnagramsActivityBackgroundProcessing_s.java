 package com.agiliq.anagen;
 
 import java.util.Iterator;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class AnagramsActivityBackgroundProcessing extends AsyncTask<Object, Object, Object> {
 
 	private TreeSet<String> permutationSet;
 	private Iterator<String> itr;
 	Stack<String> correct;
 	
 	@Override
 	protected void onPreExecute(){
		MainActivity.waiting.show();
 	}
 	
 	@Override
 	protected Object doInBackground(Object... params) {
 		String localTAG = "BackgroundProcessing.doInBackground";
 			Log.d(localTAG, "getAnagramsCandidateButton pressed");
 		
 		String inputWord = params[0].toString();
 			Log.d(localTAG, "inputWord = " + inputWord);
 			
 		Integer maximumWords= Integer.parseInt(params[1].toString()),
 				minimumCharacters= Integer.parseInt(params[2].toString());
 		
 	
 		String joinedPhrase=inputWord.replaceAll("[^a-z]+", "");	//Eliminated special characters
 			Log.d(localTAG, "joinedPhrase = " + joinedPhrase);
 		
 		permutationSet= new TreeSet<String>();
 		getAllPermutations("", joinedPhrase);
 			Log.d(localTAG, permutationSet.toString());
 	
 		AnagramsActivity.anagramsSet=new TreeSet<String>();
 		
 		String poppedoutString;
 		int length_of_stack_contents;
 		Stack<String> temp;
 		itr=permutationSet.iterator();
 		while(itr.hasNext()){
 			correct=new Stack<String>();
 			length_of_stack_contents=0;
 			getAnagrams(itr.next());
 			
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
 				String anagram="";
 				for(int i=0; i<count_of_words; i++){
 					anagram=anagram.concat(temp.pop()+" ");
 				}
 				anagram=anagram.trim();
 				AnagramsActivity.anagramsSet.add(anagram);
 					Log.d(localTAG+"-anagram", anagram);
 			}			
 		}
 		Log.d(localTAG+"-anagramsSet", AnagramsActivity.anagramsSet.toString());
 		
 		return null;
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
 	
 	@Override
 	protected void onPostExecute(Object result) {
 		AnagramsActivity.waiting.dismiss();
 		AnagramsActivity ana_obj= (AnagramsActivity) AnagramsActivity.anagrams_activity_object;
 		ana_obj.change();
 	}
 }
