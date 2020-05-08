 package cmput301f13t10.view;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.ExpandableListView;
 import android.widget.LinearLayout;
 import cmput301f13t10.model.HelpItem;
 import cs.ualberta.cmput301f13t10.R;
 
 public class HelpView extends Activity
 {
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		setContentView( R.layout.help_view );
 
 		
 		ArrayList<HelpItem> topList = new ArrayList<HelpItem>();
 		
 		ArrayList<HelpItem> readAdventure = new ArrayList<HelpItem>();
 		readAdventure.add( new HelpItem( getString(R.string.h_Read1), null ) );
 		readAdventure.add( new HelpItem( getString(R.string.h_Read2), null ) );
 		readAdventure.add( new HelpItem( getString(R.string.h_Read3), null ) );
 		readAdventure.add( new HelpItem( getString(R.string.h_Read4), null ) );
 		readAdventure.add( new HelpItem( getString(R.string.h_Read5), null ) );
 		readAdventure.add( new HelpItem( getString(R.string.h_Read6), null ) );
 		ArrayList<HelpItem>	editAdventure = new ArrayList<HelpItem>();
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure1), null ) );
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure2), null ) );
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure3), null ) );
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure3a), null ) );
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure3b), null ) );
 		editAdventure.add( new HelpItem( getString(R.string.h_EditAdventure3c), null ) );
 		ArrayList<HelpItem> editSection = new ArrayList<HelpItem>();
 		editSection.add( new HelpItem( getString(R.string.h_EditSection1), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection2), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection3), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection3a), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection3b), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection4), null ) );
 		editSection.add( new HelpItem( getString(R.string.h_EditSection5), null ) );
 		ArrayList<HelpItem> editChoices = new ArrayList<HelpItem>();
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices1), null ) );
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices2), null ) );
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices3), null ) );
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices3a), null ) );
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices3b), null ) );
 		editChoices.add( new HelpItem( getString(R.string.h_EditChoices3c), null ) );
 		ArrayList<HelpItem> upload = new ArrayList<HelpItem>();
 //		upload.add( new HelpItem( getString(R.string.h_Upload1), null ) );
 		ArrayList<HelpItem> definitions = new ArrayList<HelpItem>();
 		definitions.add( new HelpItem( getString(R.string.h_Definitions1), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions2), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions3), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions4), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions5), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions6), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions7), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions8), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions9), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions10), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions11), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions12), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions13), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions14), null ) );
 		definitions.add( new HelpItem( getString(R.string.h_Definitions15), null ) );
 		
 		topList.add( new HelpItem( getString(R.string.h_Read), readAdventure ) );
 		topList.add( new HelpItem( getString(R.string.h_EditAdventure), editAdventure ) );
 		topList.add( new HelpItem( getString(R.string.h_EditSection), editSection ) );
 		topList.add( new HelpItem( getString(R.string.h_EditChoices), editChoices ) );
 		topList.add( new HelpItem( getString(R.string.h_Upload), upload ) );
 		topList.add( new HelpItem( getString(R.string.h_Definitions), definitions ) );
 		
 		LinearLayout mainView = (LinearLayout) findViewById( R.id.main_help_view );
 
 		ExpandableListView helpView = new ExpandableListView( this );
 		helpView.setGroupIndicator( null );
 		helpView.setAdapter( new ExpandableHelpListAdapter( this,  topList) );
 
 		mainView.addView( helpView );
 	}
 }
