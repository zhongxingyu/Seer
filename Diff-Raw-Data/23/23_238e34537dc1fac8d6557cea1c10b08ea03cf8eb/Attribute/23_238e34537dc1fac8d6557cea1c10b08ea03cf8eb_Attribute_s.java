 package org.xfon.m.coc;
 
 import android.app.Activity;
 import android.widget.TextView;
 
 final public class Attribute {
 	private Activity activity;
 	
 	private String name;	
 	
 	private int baseTextViewId;
 	private int modTextViewId;
 	private int seekBarId;
 	
 	private int unmodifiedValue;
 	private int mod;
 	
 	private Dice rollingDice;
 	
 	public Attribute(Activity activity, String name, int baseTextViewId, int modTextViewId, int seekBarId, Dice rollingDice ) {
 		super();
 		this.activity = activity;
 		this.name = name;
 		this.baseTextViewId = baseTextViewId;
 		this.modTextViewId = modTextViewId;
 		this.seekBarId = seekBarId;
 		this.rollingDice = rollingDice;
 		
 		this.unmodifiedValue = 0;
 		this.mod = 0;
 	}
 		
 	public int getUnmodifiedValue() {
 		return unmodifiedValue;
 	}
 
 	public void setUnmodifiedValue(int unmodifiedValue) {
 		this.unmodifiedValue = unmodifiedValue;
 	}
 	
 	public void setRollingDice( Dice rollingDice ) {
 		this.rollingDice = rollingDice;
 	}
 
 	public void setMod( int mod ) {
 		this.mod = mod;
 		setIntValue( modTextViewId, -mod );
		setIntValue( baseTextViewId, unmodifiedValue - mod );
 	}
 	
 	public int getMod() {
 		return this.mod;
 	}
 	
 	public int getSeekBarId() {
 		return this.seekBarId;
 	}
 	
 	
     private void setIntValue( int id, int value ) {
     	((TextView)activity.findViewById( id )).setText( "" + value );
     }
     
     public int getTotal() {
    	return unmodifiedValue - mod;
     }
     
     public int roll() {
     	int result = rollingDice.roll();
     	unmodifiedValue = result;
     	if ( baseTextViewId != 0 ) setIntValue( baseTextViewId, unmodifiedValue );
    	if ( modTextViewId != 0 ) setIntValue( modTextViewId, unmodifiedValue - mod );
     	return result;
     }
 
 }
