 package derricp1.apps.navi;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 
 import java.io.*;
 import java.util.Locale;
 
 public class Main extends Activity {
 	
 	public final static String EXTRA_MESSAGE = "derricp1.apps.MESSAGE"; //Message to display to user
 	public final static String ZOOM_LEVEL = "derricp1.apps.MESSAGE2"; //this doesn't really matter, at least for what message
 	public final static String WORDS = "derricp1.apps.MESSAGE3";
 	public final static String VOICES = "derricp1.apps.MESSAGE4";
 	public final static String START = "derricp1.apps.MESSAGE5";
 	
 	public int rangesize = 455; //number of ranges (node sets) - Needs to be changed to strings due to call numbers
 	public String[] lo; //ranges
 	public String[] hi; //ranges
 	
 	public int[] lonum;
 	public int[] hinum;
 	
 	public String[] lolast;
 	public String[] hilast;	
 	
 	public boolean voices = false;
 	public boolean words = false;
 	
 	public boolean zlevel = false;
 	
 	//May need to change into having 3 arrays, one with node translations to a range, in addition to hi and lo
 	
 	public boolean loaded = false;
 	
 	public View myView;
 	
 	/** Called when the user clicks the Send button */
 	public void sendMessage(View view) {
 		
 	    Intent intent = new Intent(this, LoadActivity.class); //Makes an intent to pass the user's node
 	    EditText editText = (EditText) findViewById(R.id.EditText01);
 	    String isbn = (editText.getText().toString()).toUpperCase(Locale.ENGLISH);
 	    
 	    EditText editText2 = (EditText) findViewById(R.id.EditText02);
 	    String isbn2 = (editText2.getText().toString()).toUpperCase(Locale.ENGLISH);
 
 	    /*---------------------------------------------------------------------
 	     * Call important information for start point
 	    -----------------------------------------------------------------------*/
 	    
 	    //remove periods
 	    while (isbn.contains((CharSequence) ".") == true) {
 	    	int q = isbn.indexOf(".");
 	    	if (q != isbn.length()-1)
 	    		isbn = isbn.substring(0, q-1) + isbn.substring(q+1, isbn.length()-1); //chop period
 	    	else
 	    		isbn = isbn.substring(0, q-1);
 	    }
 	    
 	    //also do the third level split 
 	    
 	    String isbnchars = ""; //splitting of the isbn
 	    int isbnnums = -1;
 	    String isbnend = "";
 	    
 	    int breakpoint = 0;
 	    char[] chararray = isbn.toCharArray();
 	    for (int q=0; q<isbn.length(); q++) {
 	    	if (isbnchars == "" && Character.isDigit(chararray[q]) == true) {
 	    		isbnchars = isbn.substring(0,q);
 	    		breakpoint = q;
 	    	}
 	    	if (isbnchars != "" && isbnnums == -1 && isbnend == "") {
 	    		if (q == isbn.length() - 1 && isbnnums == -1) { //last character
 	    			String tempstr = (isbn.substring(breakpoint,q+1));
 		    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 		    			isbnnums = Integer.parseInt(tempstr);
 	    		}
 	    		if (Character.isDigit(chararray[q]) == false && isbnnums == -1) { //not a number anymore
 		    		String tempstr = (isbn.substring(breakpoint,q));
 		    		breakpoint = q;
 		    		if (tempstr.length() > 0)
 		    			isbnnums = Integer.parseInt(tempstr);	    			
 	    		}
 	    	}
 	    	if (isbnchars != "" && isbnnums != -1 && isbnend == "") {
 	    		isbnend = isbn.substring(q,isbn.length());    		
 	    	}
 	    }
 	    
 	    
 	    int target = 0;
 	    CharSequence cs = editText.getText();
 	    if (cs != null && cs.length() > 0) {
 	    	
 	    	if (loaded == false) {
 	    		
 				//Load range file
 	    		InputStream is = getResources().openRawResource(R.raw.range);				
 				InputStreamReader isr = new InputStreamReader(is);
 				BufferedReader br = new BufferedReader(isr);
 				
 				try {
 					String str = br.readLine(); //Reads the range size
 					rangesize = Integer.parseInt(str);
 					str = br.readLine(); //Discards hash
 					lo = new String[rangesize];
 					hi = new String[rangesize];
 					lonum = new int[rangesize];
 					hinum = new int[rangesize];
 					lolast = new String[rangesize];
 					hilast = new String[rangesize];
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here
 				}
 
 				//sets pairs of numbers to range
 				try {
 					for (int i=0; i< rangesize; i++) {
 						
 						String str = br.readLine(); //Reads in data
 						
 					    String ics = ""; //splitting of the isbn
 					    int ins = -1;
 					    String ils = "";
 					    breakpoint = 0;
 					    chararray = str.toCharArray();
 					    
 					    for (int q=0; q<str.length(); q++) {
 					    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
 					    		ics = str.substring(0,q);
 					    		breakpoint = q;
 					    	}
 					    	if (ics != "" && ins == -1) {
 					    		if (q == str.length() - 1 && ins == -1) { //last character
 					    			String tempstr = (str.substring(breakpoint,q+1));
 						    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 						    			ins = Integer.parseInt(tempstr);
 					    		}
 					    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
 						    		String tempstr = (str.substring(breakpoint,q));
 						    		breakpoint = q;
 						    		if (tempstr.length() > 0)
 						    			ins = Integer.parseInt(tempstr);	    			
 					    		}
 					    	}
 					    	if (ics != "" && ins != -1 && ils == "") {
 					    		ils = str.substring(q,str.length());     		
 					    	}
 					    }
 					    
 					    lo[i] = ics.toUpperCase(Locale.ENGLISH);
 					    lonum[i] = ins;
 					    lolast[i] = ils;
 
 						str = br.readLine(); //Reads in data
 						
 					    ics = ""; //splitting of the isbn
 					    ins = -1;
 					    breakpoint = 0;
 					    chararray = str.toCharArray();
 					    
 					    for (int q=0; q<str.length(); q++) {
 					    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
 					    		ics = str.substring(0,q);
 					    		breakpoint = q;
 					    	}
 					    	if (ics != "" && ins == -1) {
 					    		if (q == str.length() - 1 && ins == -1) { //last character
 					    			String tempstr = (str.substring(breakpoint,q+1));
 						    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 						    			ins = Integer.parseInt(tempstr);
 					    		}
 					    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
 						    		String tempstr = (str.substring(breakpoint,q));
 						    		breakpoint = q;
 						    		if (tempstr.length() > 0)
 						    			ins = Integer.parseInt(tempstr);	    			
 					    		}
 					    	}
 					    	if (ics != "" && ins != -1 && ils == "") {
 					    		ils = str.substring(q,str.length());     		
 					    	}
 					    }
 					    
 					    hi[i] = ics.toUpperCase(Locale.ENGLISH);
 					    hinum[i] = ins;
 					    hilast[i] = ils;
 						
 						str = br.readLine(); //Discards hash
 
 					}
 				} 
 				catch (IOException e) {
 					finish(); //should not reach here either
 				}
 	    	}
 	    	
 	    	int lowcomp = 0;
 	    	int hicomp = 0;
 
 	    	//Compare ranges to ISBN
 	    	//If in range, carry on
 	    	boolean success = false;
 			for (int i=0; i<rangesize; i++) { //needs to be fixed, doesn't handle numbers and such correctly
 				lowcomp = isbnchars.compareToIgnoreCase(lo[i]);
 				hicomp = isbnchars.compareToIgnoreCase(hi[i]);
 				if ((lowcomp >= 0 && hicomp <= 0)) {
 					boolean numsuccess = false;
 					
 					if (lowcomp > 0 && hicomp < 0  || lonum[i]+hinum[i] == -2)
 						numsuccess = true;
 					
 					if (numsuccess == false) { 
 						boolean segsuccess = true;
 						
 						if (lowcomp == 0 && isbnnums < lonum[i])
 							segsuccess = false;
 						if (hicomp == 0 && isbnnums > hinum[i])
 							segsuccess = false;
 						
 						if (lolast[i] != "" || hilast[i] != "") {
 							if ((lolast[i] != "" && isbnend.compareTo(lolast[i]) < 0 && (lowcomp == 0 && isbnnums == lonum[i])) || (hilast[i] != "" && isbnend.compareTo(hilast[i]) > 0 && (hicomp == 0 && isbnnums == hinum[i])))
 								segsuccess = false;
 						}
 
 						if (segsuccess == true)
 							numsuccess = true;
 							
 					}
 					
 					if (numsuccess == true){
 						success = true;
 						target = i;
 					}
 				}
 			}
 			
 		    /*---------------------------------------------------------------------
 		     * Call important information for start point
 		    -----------------------------------------------------------------------*/		
 
 		    //remove periods
 		    while (isbn2.contains((CharSequence) ".") == true) {
 		    	int q = isbn2.indexOf(".");
 		    	if (q != isbn2.length()-1)
 		    		isbn2 = isbn2.substring(0, q-1) + isbn2.substring(q+1, isbn2.length()-1); //chop period
 		    	else
 		    		isbn2 = isbn2.substring(0, q-1);
 		    }
 		    
 		    //also do the third level split 
 		    
 		    String isbnchars2 = ""; //splitting of the isbn
 		    int isbnnums2 = -1;
 		    String isbnend2 = "";
 		    
 		    breakpoint = 0;
 		    chararray = isbn2.toCharArray();
 		    for (int q=0; q<isbn2.length(); q++) {
 		    	if (isbnchars2 == "" && Character.isDigit(chararray[q]) == true) {
 		    		isbnchars2 = isbn2.substring(0,q);
 		    		breakpoint = q;
 		    	}
 		    	if (isbnchars2 != "" && isbnnums2 == -1 && isbnend2 == "") {
 		    		if (q == isbn2.length() - 1 && isbnnums2 == -1) { //last character
 		    			String tempstr = (isbn2.substring(breakpoint,q+1));
 			    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 			    			isbnnums2 = Integer.parseInt(tempstr);
 		    		}
 		    		if (Character.isDigit(chararray[q]) == false && isbnnums2 == -1) { //not a number anymore
 			    		String tempstr = (isbn2.substring(breakpoint,q));
 			    		breakpoint = q;
 			    		if (tempstr.length() > 0)
 			    			isbnnums2 = Integer.parseInt(tempstr);	    			
 		    		}
 		    	}
 		    	if (isbnchars2 != "" && isbnnums2 != -1 && isbnend2 == "") {
 		    		isbnend2 = isbn2.substring(q,isbn2.length());    		
 		    	}
 		    }
 		    
 		    
 		    int starget = -1;
 		    cs = editText2.getText();
 		    if (cs != null && cs.length() > 0) {
 		    	
 		    	if (loaded == false) {
 		    		
 					//Load range file
 		    		InputStream is = getResources().openRawResource(R.raw.range);				
 					InputStreamReader isr = new InputStreamReader(is);
 					BufferedReader br = new BufferedReader(isr);
 					
 					try {
 						String str = br.readLine(); //Reads the range size
 						rangesize = Integer.parseInt(str);
 						str = br.readLine(); //Discards hash
 						lo = new String[rangesize];
 						hi = new String[rangesize];
 						lonum = new int[rangesize];
 						hinum = new int[rangesize];
 						lolast = new String[rangesize];
 						hilast = new String[rangesize];
 					} 
 					catch (IOException e) {
 						finish(); //should not reach here
 					}
 
 					//sets pairs of numbers to range
 					try {
 						for (int i=0; i< rangesize; i++) {
 							
 							String str = br.readLine(); //Reads in data
 							
 						    String ics = ""; //splitting of the isbn
 						    int ins = -1;
 						    String ils = "";
 						    breakpoint = 0;
 						    chararray = str.toCharArray();
 						    
 						    for (int q=0; q<str.length(); q++) {
 						    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
 						    		ics = str.substring(0,q);
 						    		breakpoint = q;
 						    	}
 						    	if (ics != "" && ins == -1) {
 						    		if (q == str.length() - 1 && ins == -1) { //last character
 						    			String tempstr = (str.substring(breakpoint,q+1));
 							    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 							    			ins = Integer.parseInt(tempstr);
 						    		}
 						    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
 							    		String tempstr = (str.substring(breakpoint,q));
 							    		breakpoint = q;
 							    		if (tempstr.length() > 0)
 							    			ins = Integer.parseInt(tempstr);	    			
 						    		}
 						    	}
 						    	if (ics != "" && ins != -1 && ils == "") {
 						    		ils = str.substring(q,str.length());     		
 						    	}
 						    }
 						    
 						    lo[i] = ics.toUpperCase(Locale.ENGLISH);
 						    lonum[i] = ins;
 						    lolast[i] = ils;
 
 							str = br.readLine(); //Reads in data
 							
 						    ics = ""; //splitting of the isbn
 						    ins = -1;
 						    breakpoint = 0;
 						    chararray = str.toCharArray();
 						    
 						    for (int q=0; q<str.length(); q++) {
 						    	if (ics == "" && Character.isDigit(chararray[q]) == true) {
 						    		ics = str.substring(0,q);
 						    		breakpoint = q;
 						    	}
 						    	if (ics != "" && ins == -1) {
 						    		if (q == str.length() - 1 && ins == -1) { //last character
 						    			String tempstr = (str.substring(breakpoint,q+1));
 							    		if (tempstr.length() > 0 && Character.isDigit(chararray[q]) == true)
 							    			ins = Integer.parseInt(tempstr);
 						    		}
 						    		if (Character.isDigit(chararray[q]) == false && ins == -1) { //not a number anymore
 							    		String tempstr = (str.substring(breakpoint,q));
 							    		breakpoint = q;
 							    		if (tempstr.length() > 0)
 							    			ins = Integer.parseInt(tempstr);	    			
 						    		}
 						    	}
 						    	if (ics != "" && ins != -1 && ils == "") {
 						    		ils = str.substring(q,str.length());     		
 						    	}
 						    }
 						    
 						    hi[i] = ics.toUpperCase(Locale.ENGLISH);
 						    hinum[i] = ins;
 						    hilast[i] = ils;
 							
 							str = br.readLine(); //Discards hash
 
 						}
 					} 
 					catch (IOException e) {
 						finish(); //should not reach here either
 					}
 		    	}
 		    	
 		    	lowcomp = 0;
 		    	hicomp = 0;
 
 		    	//Compare ranges to ISBN
 		    	//If in range, carry on
 		    	success = false;
 				for (int i=0; i<rangesize; i++) { //needs to be fixed, doesn't handle numbers and such correctly
 					lowcomp = isbnchars2.compareToIgnoreCase(lo[i]);
 					hicomp = isbnchars2.compareToIgnoreCase(hi[i]);
 					if ((lowcomp >= 0 && hicomp <= 0)) {
 						boolean numsuccess = false;
 						
 						if (lowcomp > 0 && hicomp < 0  || lonum[i]+hinum[i] == -2)
 							numsuccess = true;
 						
 						if (numsuccess == false) { 
 							boolean segsuccess = true;
 							
 							if (lowcomp == 0 && isbnnums2 < lonum[i])
 								segsuccess = false;
 							if (hicomp == 0 && isbnnums2 > hinum[i])
 								segsuccess = false;
 							
 							if (lolast[i] != "" || hilast[i] != "") {
 								if ((lolast[i] != "" && isbnend2.compareTo(lolast[i]) < 0 && (lowcomp == 0 && isbnnums2 == lonum[i])) || (hilast[i] != "" && isbnend2.compareTo(hilast[i]) > 0 && (hicomp == 0 && isbnnums2 == hinum[i])))
 									segsuccess = false;
 							}
 
 							if (segsuccess == true)
 								numsuccess = true;
 								
 						}
 						
 						if (numsuccess == true){
 							success = true;
 							starget = i;
 						}
 					}
 					
 				}
 			}
 		    else {
 		    	starget = -1;
 		    }
 			
 		    /*---------------------------------------------------------------------
 		     * Check validity of input
 		    -----------------------------------------------------------------------*/
 	    	
 	    	if (success == true) { //Will fail if user enters an invalid ISBN
	    		//Toast.makeText(this, target + " " + starget, Toast.LENGTH_SHORT).show();
 	    		
 			    intent.putExtra(EXTRA_MESSAGE, target); //Places the id number of the node of the shelf where the ISBN would be
 			    intent.putExtra(ZOOM_LEVEL, zlevel);
 			    intent.putExtra(VOICES, voices);
 			    intent.putExtra(WORDS, words);
 			    intent.putExtra(START, starget);
 			    startActivity(intent);
 	    	}
 	    	else {
 	    		Toast.makeText(this, "Invalid ISBN", Toast.LENGTH_SHORT).show();
 	    	}
 	    
 	    }
 	    else {
 	    	Toast.makeText(this, "Please Enter an ISBN", Toast.LENGTH_SHORT).show();
 	    }
 	}
     
     public void wordsup(View v) {
     	if (words == false)
     		words = true;
     	else
     		words = false;
     }
     
     public void voiceup(View v) {
     	if (voices == false)
     		voices = true;
     	else
     		voices = false;
     }
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
 		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 		setContentView(R.layout.activity_main);
 	    myView = this.findViewById(android.R.id.content); //gets view (important!) 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    // Handle presses on the action bar items
 	    switch (item.getItemId()) {
 	        case R.id.action_about:
 	            thanks(myView);
 	            return true;
 	        case R.id.action_privacy:
 	            privacy(myView);
 	            return true;
 	        default:
 	            return super.onOptionsItemSelected(item);
 	    }
 	}
 	
 	public void privacy(View view) {
 		Toast.makeText(this, "TCNJ BookNav will not collect any personally identifyng information about you. It will only collect WiFi information to guide you to your destination. Please turn your WiFi on.", Toast.LENGTH_LONG).show();
 	}
 
 	public void thanks(View view) {
 		//go to new method?
 	    Intent intent = new Intent(this, About.class); //Makes an intent to pass the user's node
 		startActivity(intent);
 	}
 	
 	public void help(View view) {
 		//go to new method?
 	    Intent intent = new Intent(this, HelpActivity.class); //Makes an intent to pass the user's node
 		startActivity(intent);
 	}
 	
 	public void test(View view) {
 		//go to new method?
 	    Intent intent = new Intent(this, TestActivity.class); //Makes an intent to pass the user's node
 		startActivity(intent);
 	}
 	
     @Override
     public void onBackPressed() {
         //don't go back, it'll mess everything up due to asyncs
     }
 
 }
 
