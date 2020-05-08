 package com.example.zootypers.test;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
import java.awt.*;
 
 import junit.framework.TestCase;
 import android.content.Intent;
 import android.graphics.Color;
 import android.renderscript.Sampler.Value;
 import android.test.ActivityInstrumentationTestCase2;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.TextAppearanceSpan;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import com.example.zootypers.SinglePlayer;
 import com.example.zootypers.R;
 import com.jayway.android.robotium.solo.Solo;
 import com.example.zootypers.PreGameSelection;
 
 public class TestSinglePlayer extends  ActivityInstrumentationTestCase2<PreGameSelection> {
 
 	private Solo solo;
 	
 	private char[] lowChanceLetters = {'j', 'z', 'x', 'q', 'k'};
 	
 	public TestSinglePlayer() {
         super(PreGameSelection.class);
     }
 
 
   	protected void setUp() throws Exception {
 
   		super.setUp();
   		this.setActivityInitialTouchMode(false);
 		solo = new Solo(getInstrumentation(), getActivity());
 		solo.clickOnButton("Continue");
 	}
 	public static List<TextView> getWordsPresented(Solo solo){
 		solo.sleep(1000);
 		List<TextView> retVal = new ArrayList<TextView>();
 		retVal.add(((TextView)solo.getCurrentActivity().findViewById(R.id.word0)));
 		retVal.add(((TextView)solo.getCurrentActivity().findViewById(R.id.word1)));
 		retVal.add(((TextView)solo.getCurrentActivity().findViewById(R.id.word2)));
 		retVal.add(((TextView)solo.getCurrentActivity().findViewById(R.id.word3)));
 		retVal.add(((TextView)solo.getCurrentActivity().findViewById(R.id.word4)));
 		return retVal;
 	}
 	
 	
 	public void testInvalidCharacterPressed(){
 		List<TextView> views = getWordsPresented(solo);
 		String firstLetters = "";
 		for(TextView s : views){
 			firstLetters += s.getText().charAt(0);
 		}
 		for(char c : lowChanceLetters){
 			if(firstLetters.indexOf(c) < 0 ){
 				sendKeys('j' - 68);
 				assertTrue(solo.searchText("Invalid Letter Typed"));
 			}
 		}
 		
 	}
 	
 	public void testCorrectCharacterPressed(){
 		List<TextView> views = getWordsPresented(solo);
 		TextView s = views.get(0);
 		Log.v("words", s.getText().toString());
 		solo.sleep(5000);
 		sendKeys(s.getText().charAt(0) - 68);//words.get(0).substring(0, 1));
 		Log.v("char typed", String.valueOf(Character.toUpperCase(s.getText().charAt(0))));
 		views = getWordsPresented(solo);
 		solo.sleep(1000);
 		CharSequence word = views.get(0).getText();
 		Log.v("word", word.toString());
 		SpannableString spanString = new SpannableString(word);
 		Log.v("Span", spanString.toString());
 		ForegroundColorSpan[] spans = spanString.getSpans(0, spanString.length(), ForegroundColorSpan.class);
 		assertTrue(spans.length>0);//Color.rgb(0, 255, 0) == spans[0].getForegroundColor());
 
 	}
 
 	
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
