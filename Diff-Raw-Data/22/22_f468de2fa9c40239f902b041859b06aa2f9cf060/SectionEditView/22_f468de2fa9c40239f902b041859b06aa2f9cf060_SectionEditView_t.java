 package cmput301f13t10;
 
 import cs.ualberta.cmput301f13t10.R;
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 /**
  * 
  * @author Braeden Soetaert
  * @author Tyler Meen
  * @author Steven Gerdes
  * 
  */
 public class SectionEditView extends Activity implements SectionView
 {
 	private SectionPresenter mPresenter;
	
	private String mDisplayTitle;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 
 		mPresenter = new SectionPresenter( this );
 
 		setContentView( R.layout.section_edit_view );
 		setUpActionBar();
 
 		Intent intent = getIntent();
 		int defaultVal = -1;
 		if( intent.hasExtra( AppConstants.ADVENTURE_ID ) )
 			mPresenter.setCurrentAdventure( intent.getIntExtra( AppConstants.ADVENTURE_ID, defaultVal ) );
 		if( intent.hasExtra( AppConstants.SECTION_ID ) )
 			mPresenter.setCurrentSectionById( intent.getIntExtra( AppConstants.SECTION_ID, defaultVal ) );
 		else
 			mPresenter.setCurrentSectionById( null );
		mDisplayTitle = mPresenter.getSectionTitle();
		EditText title = (EditText) getActionBar().getCustomView().findViewById( R.id.section_edit_title );
		title.setText( mDisplayTitle );
 	}
 
 	@Override
 	public void updateSectionView()
 	{
 		ActionBar actionBar = getActionBar();
 		EditText title = (EditText) actionBar.getCustomView().findViewById( R.id.section_edit_title );
		title.setText( mDisplayTitle );
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate( R.menu.section_edit_view, menu );
 
 		return super.onCreateOptionsMenu( menu );
 	}
 
 	@Override
 	public Context getContext()
 	{
 		return this;
 	}
 
 	/**
 	 * Listener for the modify choices button. Start the
 	 * SectionModifyChoicesView with the current section and adventure
 	 * 
 	 * @param view
 	 *            The view of the button that was clicked
 	 */
 	public void launchModifyChoicesAction( View view )
 	{
 		Intent intent = new Intent( this, SectionModifyChoicesView.class );
 		intent.putExtra( AppConstants.ADVENTURE_ID, mPresenter.getAdventureId() );
 		intent.putExtra( AppConstants.SECTION_ID, mPresenter.getSectionId() );
 		startActivity( intent );
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}, if the API is available.
 	 */
 	public void setUpActionBar()
 	{
 		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
 		{
 			ActionBar actionBar = getActionBar();
 			// add the custom view to the action bar
 
 			actionBar.setCustomView( R.layout.section_edit_action_bar );
 			EditText title = (EditText) actionBar.getCustomView().findViewById( R.id.section_edit_title );
 			title.addTextChangedListener( new TextWatcher()
 			{
 				public void afterTextChanged( Editable s )
 				{
					mDisplayTitle = s.toString();
 				}
 
 				public void beforeTextChanged( CharSequence s, int start, int count, int after )
 				{
 				}
 
 				public void onTextChanged( CharSequence s, int start, int before, int count )
 				{
 				}
 			} );
 			actionBar.setDisplayShowCustomEnabled( true );
 			actionBar.setDisplayHomeAsUpEnabled( true );
 		}
 	}
 
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		switch( item.getItemId() )
 		{
 		case R.id.action_add_media:
 			addImage();
 			return true;
 		default:
 
 			return super.onOptionsItemSelected( item );
 
 		}
 
 	}
 
 	/**
 	 * Opens the camera.
 	 */
 	private void addImage()
 
 	{
 
 		Intent intent = new Intent( MediaStore.ACTION_IMAGE_CAPTURE );
 
 		startActivityForResult( intent, 0 );
 
 	}
 
 	/**
 	 * receives the result from the camera, and passes the result to the
 	 * presenter to add the image to the model.
 	 */
 	protected void onActivityResult( int requestCode, int resultCode, Intent data )
 	{
 		if( requestCode == 0 && resultCode == Activity.RESULT_OK )
 		{
 			mPresenter.storeImage( this, data );
 		}
 
 	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		mPresenter.UpdateSectionTitle( mDisplayTitle );
	}
 
 }
