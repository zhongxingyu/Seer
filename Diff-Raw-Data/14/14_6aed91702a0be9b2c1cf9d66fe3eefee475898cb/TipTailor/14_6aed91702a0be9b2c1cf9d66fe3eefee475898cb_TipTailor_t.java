 package com.nullninjadev.tipify;
 
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.LinearLayout;
 
 public class TipTailor extends Activity {
 	private Vector<PersonBar> mBars;
 	private static TipTailor ref;
 	private LinearLayout pBarLayout;
 	
 	/**
 	 * Creates a reference to this Activity if it doesn't already exist and returns it.
 	 * @return the reference to this Activity
 	 */
 	public static TipTailor getInstance(){
 		if(ref == null) ref = new TipTailor();
 		return ref;
 	}
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.tip_tailor);
         ref = this;
         mBars = new Vector<PersonBar>();
        this.setTitle("Tipify - Tip Tailor");
         initUI(); // Add the necessary PersonBars to the UI
         DataHelper.calculateTips(this);
     }
     
     /**
      * Initializes all the PersonBars based on the current list of Persons held by
      * DataHelper.
      */
     private void initUI(){
     	pBarLayout = (LinearLayout)findViewById(R.id.pbar_container);
     	
     	for(Person person : DataHelper.getPeople()){
     		PersonBar pBar = new PersonBar(this, person);
     		pBar.getNameEditText().setText(person.getName());
     		mBars.add(pBar);
     		pBarLayout.addView(pBar);
     	}
     }
     
     /**
      * Updates the tip amounts displayed.
      */
     public void updateUI(){
     	DecimalFormat myFormatter = (DecimalFormat) DecimalFormat.getCurrencyInstance();
 		Vector<Person> people = DataHelper.getPeople();
 		
     	for(int i = 0; i < mBars.size(); i++){
     		mBars.get(i).getTipTextView().setText(myFormatter.format(people.get(i).getTip()));
     	}
     }
 }
