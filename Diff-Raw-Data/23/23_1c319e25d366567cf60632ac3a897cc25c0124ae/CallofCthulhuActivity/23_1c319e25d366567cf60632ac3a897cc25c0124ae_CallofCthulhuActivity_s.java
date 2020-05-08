 package org.xfon.m.coc;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.xfon.m.coc.gui.AttributeReducer;
 import org.xfon.m.coc.gui.CustomNumberPicker;
 import org.xfon.m.coc.gui.CustomNumberPicker.OnChangedListener;
 import org.xfon.m.coc.gui.BaseSkillEditor;
 import org.xfon.m.coc.gui.FoldingLayout;
 import org.xfon.m.coc.gui.SkillCategoryEditor;
 import org.xfon.m.coc.gui.SkillEditor;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.Context;
 import android.graphics.Color;
 import android.media.MediaPlayer;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TableLayout;
 import android.widget.TextView;
 
 public class CallofCthulhuActivity extends Activity implements OnAttributeChangedListener, OnAgeChangedListener, OnSkillChangedListener {
 	
 	private CocDatabaseAdapter dbAdapter;
 	private Investigator investigator;
 	
 	private String errorMessage = null; 	
 	private MediaPlayer player = null;
 	
 	private int mustDrop = -1;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         dbAdapter = new CocDatabaseAdapter(this);
         Age age = new Age( this, R.id.tv_age );
         investigator = new Investigator( this, age );            
         clearErrors();                       
         
         setContentView(R.layout.main);
         initializeTabs();
     }
 
 	private void initializeTabs() {
 		final int TAB_HEIGHT = 40;
 		int[] tabContentId = { R.id.tab1, R.id.tab2 };
 		String[] tabLabels = { "Main", "Skills" };
 				
 		TabHost tabHost=(TabHost)findViewById(R.id.tabHost);
         tabHost.setup();
         
         for ( int i = 0; i < tabContentId.length; i++ ) {
         	TabSpec spec=tabHost.newTabSpec( tabLabels[ i ] );
         	spec.setContent( tabContentId[ i ] );
         	spec.setIndicator( tabLabels[ i ] );
         	tabHost.addTab( spec );
         	tabHost.getTabWidget().getChildAt( i ).getLayoutParams().height = TAB_HEIGHT;
         }
 	}          
     
     @Override
     public void onPause() {
     	super.onPause();
     	Log.i("APP", "onPause called" );
     	saveInvestigator(investigator, "_ONPAUSE_" );
     }
     
     @Override
     public void onStart() {
     	super.onStart();
     	Log.i("APP", "onStart called" );
     	loadInvestigator(investigator, "_ONPAUSE_" );
     	
     	populateSkillsTable( );
     }
     
     private void populateSkillsTable() {
     	TableLayout tableSkills = (TableLayout)findViewById( R.id.tableSkills );
     	tableSkills.setShrinkAllColumns( false );
     	tableSkills.setStretchAllColumns( false );
     	tableSkills.removeAllViews();
     	Map<String, SkillCategoryEditor> categoryEditors = new HashMap<String, SkillCategoryEditor>();
     	for ( ISkill skill: investigator.getSkills().list() ) {
     		if ( skill.isCategory() ) {
     			SkillCategoryEditor editor = new SkillCategoryEditor( this, investigator.getSkills(), (SkillCategory)skill );
     			categoryEditors.put( skill.getName(), editor );
     			editor.addOnSkillChangedListener( this );
     			tableSkills.addView(  editor );
     		}    		
     		else {
     			Skill sk = (Skill)skill;
     			if ( sk.isAdded() )  continue;
     			SkillEditor editor = new SkillEditor( this, investigator.getSkills(), sk );
     			editor.addOnSkillChangedListener( this );
     			tableSkills.addView( editor );    			
     		}
     	}
     	skillChanged( null );
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();    	
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if ( item.getItemId() == R.id.menu_about ) {
     		Builder builder = new AlertDialog.Builder( this );    		
     		builder.setTitle( "About" );
     		String msg = "";
     		msg += "Call of Cthulhu character generator v.1.0";    		
     		msg += "\n\n" + "Created by: Xenofon Papadopoulos <xpapad@gmail.com>";
     		msg += "\n\n" + "In memory of \"Ruthless\" Derek Arthur Blackwell, R.I.P.";
     		msg += "\n\n\n" + "\"Reincarnation has never been easier!\" ";
     		msg += "\n\n"   + "                              - The Arkham Observer";
     		msg += "\n\n\n" + "For more information about the Call of Cthulhu role playing game, visit:\nhttp://www.chaosium.com";
     		builder.setMessage( msg );
     		AlertDialog dlg = builder.show();
     		TextView textView = (TextView)dlg.findViewById(android.R.id.message);
     		textView.setTextSize( 14 );
     		return true;
     	}
     	else if ( item.getItemId() == R.id.menu_save ) {
     		saveInvestigator(investigator, "-" );
     		return true;
     	}
     	else if ( item.getItemId() == R.id.menu_load ) {
     		loadInvestigator(investigator, "-" );
     		return true;
     	}
     	else {
     		return super.onOptionsItemSelected(item);
     	}
     }
             
     public void rerollAttributes( View view ) {
     	if ( view.getId() != R.id.btn_roll ) return;    	
     	notifyUser();
     	rerollBasicAttributes();
     	calculateDerivedAttributes();
     	
     	mustDrop = -1;
    	resetSeekBars();
     	initializeAge();
     	
     	clearErrors();  
     	((Button)findViewById( R.id.btn_roll )).setText( "Reroll" );
     }
     
     public void saveAttributes( View view ) {
     	saveInvestigator(investigator, "-" );
     }
     
     private void saveInvestigator( Investigator investigator, String saveName ) {
     	dbAdapter.saveInvestigator(investigator, saveName );
     	Log.i( "SAVE_INVESTIGATOR: ", investigator.getSkills().toString() );
     }
     
     public void loadAttributes( View view ) {
     	loadInvestigator( investigator, "-" );
     }
     
     private void loadInvestigator( Investigator investigator, String saveName ) {    	
     	dbAdapter.loadInvestigator(investigator, saveName );
     	findViewById( R.id.tv_age ).setVisibility( View.VISIBLE );     	
     	initializeAge();
 		CustomNumberPicker picker = (CustomNumberPicker)findViewById( R.id.tv_age );	
 		picker.setCurrentAndNotify( investigator.getAge() );	
     	resetSeekBars();
     	calculateDerivedAttributes();      	
     	updateMustDrop();
     	Log.i( "LOAD_INVESTIGATOR: ", investigator.getSkills().toString() );
     	Attribute attrDex = investigator.getAttribute( "DEX" );
     	Attribute attrEdu = investigator.getAttribute( "EDU" );
     	calculateDynamicSkills( attrDex, false );
     	calculateDynamicSkills( attrEdu, false );
     	populateSkillsTable( );
     }
     
     private void notifyUser() {
     	Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
     	v.vibrate( 500 );
     	if ( player == null ) player = MediaPlayer.create( this, R.raw.dice );
     	player.start();    	
     }
     
     private void rerollBasicAttributes() {
     	investigator.rerollBasicAttributes();
     	Attribute attrDex = investigator.getAttribute( "DEX" );
     	Attribute attrEdu = investigator.getAttribute( "EDU" );
     	calculateDynamicSkills( attrDex, false );
     	calculateDynamicSkills( attrEdu, false );
     	populateSkillsTable();    	
     	findViewById( R.id.tv_age ).setVisibility( View.VISIBLE );     	
     }
     
     private void calculateDerivedAttributes() {
     	int attrPow = investigator.getAttribute( "POW" ).getTotal();
     	int attrSiz = investigator.getAttribute( "SIZ" ).getTotal();
     	int attrStr = investigator.getAttribute( "STR" ).getTotal();
     	int attrCon = investigator.getAttribute( "CON" ).getTotal();
     	int attrInt = investigator.getAttribute( "INT" ).getTotal();
     	int attrEdu = investigator.getAttribute( "EDU" ).getTotal();
     	
     	setIntValue( R.id.tv_san, 5 * attrPow );
     	setIntValue( R.id.tv_idea, 5 * attrInt );
     	setIntValue( R.id.tv_luck, 5 * attrPow );
     	setIntValue( R.id.tv_know, Math.min( 99, 5 * attrEdu ) );
 
     	// Hit points
     	setIntValue( R.id.tv_hp, roundUpDiv( attrSiz + attrCon, 2 ) );
     	
     	// Str bonus
 		((TextView)findViewById( R.id.tv_dam ) ).setText( investigator.getDamBonus() );
     	
     	// Points
     	setIntValue( R.id.tv_points_occ, 20 * attrEdu );
     	setIntValue( R.id.tv_points_per, 10 * attrInt );
     }
     
     private void initializeAge() {
     	final Attribute attrEdu = investigator.getAttribute( "EDU" );
     	final int baseAge = 6 + attrEdu.getUnmodifiedValue();
     	CustomNumberPicker agePicker = (CustomNumberPicker)findViewById( R.id.tv_age );    	
     	agePicker.setOnChangeListener( new OnChangedListener() {
 
     		@Override
 			public void onChanged(CustomNumberPicker picker, int oldVal, int newVal) {
     			investigator.setAge( newVal );
 				int selectedAge = newVal;
 				int ageDiff = selectedAge - baseAge;
 				int extraEdu = ageDiff / 10;
 				attrEdu.setMod( extraEdu );				
 				calculateDerivedAttributes();
 				int newMustDrop = Math.max( 0, ( selectedAge / 10 ) - 3 );
 				if ( newMustDrop != mustDrop ) {
 					mustDrop = newMustDrop;
 					updateMustDrop();
 				}								
 			}
 		});    	    
     }       
     
     public void updateMustDrop() {
     	// TODO: optimize
    	int totalMods = - investigator.getTotalAgeMods();    	
  
     	if ( mustDrop == totalMods ) {
     		clearErrors();
     	}
     	else if ( mustDrop > totalMods ){    		
     		int count = mustDrop - totalMods;
     		setError( "You must lower " + count + " attribute" + ( count > 1 ? "s" : "" ) );    	
     	}
     	else if ( mustDrop < totalMods ){    		
     		int count = totalMods - mustDrop;
     		setError( "You must raise " + count + " attribute" + ( count > 1 ? "s" : "" ) );
     	}
     }  
     
     private void resetSeekBars ( ) {    	
     	LinearLayout lo = (LinearLayout)findViewById( R.id.modAttributesLayout );
    		lo.removeAllViews();
     	AttributeReducer reducer = new AttributeReducer( this );    	
     	reducer.addOnAttributeChangedListener( this );
     	reducer.addAttributes( investigator.getAgeModifiableAttribute() );
     	Log.i( "ATTRS", "Adding view" );
     	lo.addView( reducer );
     	lo.requestLayout();
     }    
 
     private void setIntValue( int id, int value ) {
     	((TextView)findViewById( id )).setText( "" + value );
     }
     
     private int roundUpDiv( int x, int y ) {
     	int ret = x / y;
     	if ( x % y > 0 ) ret++;
     	return ret;
     }
     
     private void updateMessage() {
     	TextView msg = (TextView)findViewById( R.id.message );
     	if ( msg == null ) return;
     	if ( errorMessage == null ) {
     		msg.setText( "Your investigator is ready!" );
     		msg.setTextColor( Color.GREEN );
     	}
     	else {
     		msg.setText( errorMessage );
     		msg.setTextColor( Color.RED );
     	}
     }
     
     private void clearErrors() {
     	errorMessage = null;
     	updateMessage();
     }
     
     private void setError( String msg ) {
     	errorMessage = msg;
     	updateMessage();
     }
 
 	@Override
	public void attributeChanged(Attribute attribute) {
 		if ( attribute.getName().equals( "DEX" ) || attribute.getName().equals( "EDU" ) ) {
 			calculateDynamicSkills( attribute, true );
 		}
		calculateDerivedAttributes();
		updateMustDrop();		
 	}
 	
 	// This sucks but it's pretty straightforward for now
 	private void calculateDynamicSkills( Attribute attribute, boolean updateEditor ) {	
 		ISkill sk = null;
 		if ( attribute.getName().equals( "DEX" ) ) {
 			sk = investigator.getSkills().get( "Dodge" );
 			if ( sk != null ) sk.setBaseValue( attribute.getTotal() * 2 );	
 			else Log.i( this.getClass().getName(), "Skill 'Dodge' not found");
 		}
 		else if ( attribute.getName().equals( "EDU" ) ) {
 			sk = investigator.getSkills().get( "Own Language" );
 			if ( sk != null ) sk.setBaseValue( Math.min( 99, attribute.getTotal() * 5 ) );
 			else Log.i( this.getClass().getName(), "Skill 'Own Language' not found");
 		}
 		if ( updateEditor && sk != null ) {
 			BaseSkillEditor editor = findSkillEditor( sk );
 			editor.updateBaseValue();
 		}					
 	}
 	
 	private BaseSkillEditor findSkillEditor( ISkill sk ) {
     	TableLayout tableSkills = (TableLayout)findViewById( R.id.tableSkills );
     	int rows = tableSkills.getChildCount();
     	for ( int i = 0; i < rows; i++ ) {
     		BaseSkillEditor editor = (BaseSkillEditor)tableSkills.getChildAt(i);
     		if ( editor.isEditorFor( sk ) ) return editor;
     	}
     	return null;
 	}
 
 	@Override
 	public void ageChanged(int age) {
 		calculateDerivedAttributes();
 		updateMustDrop();		
 	}
 
 	@Override
 	public void skillChanged(ISkill skill) {
 		TextView tv = (TextView)findViewById( R.id.messageSkills );
 		int pointsOccupationalUsed = investigator.getSkills().getOccupationalPoints();
 		int pointsPersonalUsed = investigator.getSkills().getPersonalPoints();
 		int pointsOccupationalAvail = investigator.getAvailableOccupationalPoints();
 		int pointsPersonalAvail = investigator.getAvailablePersonalPoints();
 		
 		StringBuffer buffer = new StringBuffer();
 		buffer.append( "Occ. skills: " + pointsOccupationalUsed + "/" + pointsOccupationalAvail );
 		buffer.append( "    " );
 		buffer.append( "Pers. skills: " + pointsPersonalUsed + "/" + pointsPersonalAvail );
 		tv.setText( buffer.toString() );
 		
 		if ( pointsPersonalUsed > pointsPersonalAvail || 
 			pointsOccupationalUsed > pointsOccupationalAvail ) 
 		{
 			tv.setTextColor( Color.RED );
 		}
 		else if ( pointsPersonalUsed < pointsPersonalAvail || 
 				pointsOccupationalUsed < pointsOccupationalAvail ) 
 		{
 				tv.setTextColor( Color.BLUE );
 		}
 		else {
 			tv.setTextColor( Color.GREEN );
 		}
 	}    
 }
