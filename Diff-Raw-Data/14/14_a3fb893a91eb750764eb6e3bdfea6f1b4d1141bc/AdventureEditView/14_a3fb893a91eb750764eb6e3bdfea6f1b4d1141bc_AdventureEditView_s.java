 package cmput301f13t10;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import cs.ualberta.cmput301f13t10.R;
 
 public class AdventureEditView extends Activity implements DeleteSectionDialogFragment.DeleteSectionDialogListener
 {
 	private String mDisplayTitle;
 	private AdventurePresenter mPresenter;
 	private List<SectionTitle> mSectionTitles;
 	private Integer mAdventureId;
 
 	// A listener for clicks on the list. Opens the context menu for the chosen
 	// note.
 	private OnItemClickListener mSectionClickedHandler = new OnItemClickListener()
 	{
 		public void onItemClick( AdapterView parent, View v, int position, long id )
 		{
 			SectionTitle selectedSection = (SectionTitle) parent.getItemAtPosition( position );
 			startSectionEdit( selectedSection );
 		}
 	};
 
 	private void startSectionEdit( SectionTitle selectedSection )
 	{
 		Intent intent = new Intent( this, SectionEditView.class );
 		intent.putExtra( AppConstants.ADVENTURE_ID, mAdventureId );
 		intent.putExtra( AppConstants.SECTION_ID, selectedSection.getId() );
 		startActivity( intent );
 	}
 
 	/*
 	 * Load all notes from the list into the list view.
 	 */
 	private void mLoadSections()
 	{
 		SectionArrayAdapter adapter = new SectionArrayAdapter( this, mSectionTitles );
 
 		ListView listView = (ListView) findViewById( R.id.adventure_edit_list );
 		listView.setAdapter( adapter );
 
 		listView.setOnItemClickListener( mSectionClickedHandler );
 	}
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 		setContentView( R.layout.adventure_edit_view );
 		mSetupActionBar();
 		Intent intent = getIntent();
 		if( intent.hasExtra( AppConstants.ADVENTURE_ID ) )
 		{
 			mAdventureId = intent.getIntExtra( AppConstants.ADVENTURE_ID, -1 );
 			mPresenter = new AdventurePresenter( this, mAdventureId );
 		}
 		else
 		{
 			mPresenter = new AdventurePresenter( this );
 			mAdventureId = mPresenter.getAdventureId();
 		}
 
 		mDisplayTitle = mPresenter.getAdventureTitle();
 		EditText title = (EditText) getActionBar().getCustomView().findViewById( R.id.adventure_edit_title );
 		title.setText( mDisplayTitle );
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate( R.menu.adventure_edit_view, menu );
 
 		return super.onCreateOptionsMenu( menu );
 	}
 
 	@Override
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		// Handle presses on the action bar items
 		switch( item.getItemId() )
 		{
 		case R.id.action_settings:
 			// Intent intent = new Intent(this, SettingsView.class);
 			// startActivity(intent);
 			return true;
 		case R.id.action_help:
 			// Intent intent = new Intent(this, HelpView.class);
 			// startActivity(intent);
 			return true;
 		default:
 			return super.onOptionsItemSelected( item );
 		}
 
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		mPresenter.setAdventureTitle( mDisplayTitle );
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
		mSectionTitles = mPresenter.getSectionTitles();
 		mLoadSections();
 	}
 
 	public void deletePrompt( View view )
 	{
 		Bundle choicesBundle = new Bundle();
 		SectionTitle section = (SectionTitle) view.getTag();
 		choicesBundle.putString( AppConstants.SECTION_TITLE, section.getTitle() );
 		choicesBundle.putInt( AppConstants.SECTION_ID, section.getId() );
 
 		DeleteSectionDialogFragment dialog = new DeleteSectionDialogFragment();
 		dialog.setArguments( choicesBundle );
 		dialog.show( getFragmentManager(), "" );
 	}
 
 	public void onDeleteConfirm( DialogFragment dialog )
 	{
 		mPresenter.deleteSection( dialog.getArguments().getInt( AppConstants.SECTION_ID ) );
 		mLoadSections();
 	}
 
 	public void onDeleteCancel( DialogFragment dialog )
 	{

 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	private void mSetupActionBar()
 	{
 		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
 		{
 			ActionBar actionBar = getActionBar();
 			// add the custom view to the action bar
 			actionBar.setCustomView( R.layout.adventure_edit_action_bar );
 			EditText title = (EditText) actionBar.getCustomView().findViewById( R.id.adventure_edit_title );
 			title.setOnEditorActionListener( new OnEditorActionListener()
 			{
 
 				@Override
 				public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
 				{
 					mDisplayTitle = v.getText().toString();
 					return false;
 				}
 			} );
 			actionBar.setDisplayShowCustomEnabled( true );
 			actionBar.setDisplayHomeAsUpEnabled( true );
 		}
 	}
 }
