 /*
 Copyright (c) 2013, Brendan Cowan, Tyler Meen, Steven Gerdes, Braeden Soetaert, Aly-khan Jamal
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 
 
 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
  */
 package cmput301f13t10.view;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import cmput301f13t10.presenter.AppConstants;
 import cmput301f13t10.presenter.Logger;
 import cmput301f13t10.presenter.Media;
 import cmput301f13t10.presenter.SectionPresenter;
 import cs.ualberta.cmput301f13t10.R;
 
 /**
  * View that allows the user to read and navigate an adventure.
  * 
  * @author Brendan Cowan
  * 
  */
 public class SectionReadView extends Activity implements UpdatableView, Serializable
 {
 	/**
 	 * The presenter for the view (as per MVP)
 	 */
 	SectionPresenter mPresenter;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 		super.onCreate( savedInstanceState );
 
		setupPresenter();
 		setContentView( R.layout.section_read_view );
 
 	}
 
 	/**
 	 * Set up the preseneter with the proper adventure.
 	 */
 	private void setupPresenter()
 	{
 		mPresenter = new SectionPresenter( this );
 		Intent intent = getIntent();
 		Bundle intentBundle = intent.getBundleExtra( AppConstants.CURRENT_ADVENTURE );
 		int adventure = intentBundle.getInt( AppConstants.CURRENT_ADVENTURE );
 		mPresenter.setCurrentAdventure( adventure );
 	}
 
 	/**
 	 * Reset the media displayed in the scrollable view by replacing it with
 	 * that stored in the current section of the presenter. Also change the
 	 * button at the bottom of the screen to a "return to main menu button" if
 	 * there are no more choices.
 	 */
 	public void updateView()
 	{
 		Button continueButton = (Button) findViewById( R.id.continue_button );
 
 		LinearLayout scrollBox = (LinearLayout) findViewById( R.id.read_items_linear );
 		setCurrentSectionView( scrollBox );
 
 		if( mPresenter.atLastSection() )
 		{
 			MainMenuButtonListener mainMenuListener = new MainMenuButtonListener();
 			continueButton.setOnClickListener( mainMenuListener );
 			continueButton.setText( "Main Menu" );
 		}
 		else
 		{
 			ContinueButtonListener continueListener = new ContinueButtonListener();
 			continueButton.setOnClickListener( continueListener );
 			continueButton.setText( "Continue" );
 		}
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate( R.menu.annotation_add, menu );
 
 		return super.onCreateOptionsMenu( menu );
 	}
 	
 	/**
 	 * This is the result of clicking on an option
 	 */
 	public boolean onOptionsItemSelected( MenuItem item )
 	{
 		switch( item.getItemId() )
 		{
 		case R.id.action_add_annotation:
 			startAnnotationActivity();
 			return true;
 		case android.R.id.home:
 			this.finish();
 			return true;
 		default:
 			return super.onOptionsItemSelected( item );
 		}
 
 	}
 	/**
 	 * Starts AnnotationEditView activity
 	 */
 	public void startAnnotationActivity()
 	{
 		Intent intent = new Intent( this, AnnotationEditView.class );
 		intent.putExtra( AppConstants.ADVENTURE_ID, mPresenter.getAdventureId() );
 		intent.putExtra( AppConstants.SECTION_ID, mPresenter.getSectionId() );
 		startActivity( intent );
 	}
 
 	/**
 	 * Set the input view group to contain all media in the current section.
 	 * 
 	 * @param vg
 	 *            The view group that is to contain the media.
 	 */
 	private void setCurrentSectionView( ViewGroup vg )
 	{
 		try
 		{
 			ArrayList<Media> medias = mPresenter.getMedia();
 			vg.removeAllViews();
 			for( Media m : medias )
 			{
 				View view = m.toView( this );
 				view.setFocusable( false );
 				view.setBackgroundColor( Color.TRANSPARENT );
 				vg.addView( view );
 			}
 		}
 		catch( NullPointerException e )
 		{
 			Logger.log( "No current section", e );
 		}
 	}
 
 	/**
 	 * Change the section that is being currently viewed by the user.
 	 * 
 	 * @param sectionId
 	 *            The id of the new section to view
 	 */
 	public void changeToSection( int sectionId )
 	{
 		mPresenter.setNextSectionByIndex( sectionId );
 	}
 
 	public void changeToRandomSection()
 	{
 		mPresenter.setRandomAdventure();
 	}
 
 	/**
 	 * Handler for the continue button of the view. Displays a dialog box which
 	 * gives the user options as to how to continue with the story.
 	 * 
 	 * @author Brendan Cowan
 	 * 
 	 */
 	private class ContinueButtonListener implements View.OnClickListener
 	{
 
 		@Override
 		public void onClick( View v )
 		{
 			Bundle choicesBundle = new Bundle();
 			ArrayList<String> choices = mPresenter.getChoiceDescriptions();
 			choicesBundle.putStringArray( AppConstants.CHOICES_BUNDLE, (String[]) choices.toArray( new String[choices.size()] ) );
 
 			choicesBundle.putSerializable( AppConstants.ADVENTURE_READ_VIEW, SectionReadView.this );
 
 			ContinueDialogFragment dialog = new ContinueDialogFragment();
 			dialog.setArguments( choicesBundle );
 			dialog.show( getFragmentManager(), "" );
 		}
 
 	}
 
 	/**
 	 * Handler for the main menu button of the view. Sends the user back to the
 	 * main menu.
 	 * 
 	 * @author Brendan Cowan
 	 * 
 	 */
 	private class MainMenuButtonListener implements View.OnClickListener
 	{
 
 		@Override
 		public void onClick( View v )
 		{
 			Intent intent = new Intent( SectionReadView.this, MainActivity.class );
 			intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
 			intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
 			startActivity( intent );
 		}
 
 	}
 
 	/**
 	 * Starts up the help view on help button click.
 	 * 
 	 * @param view
 	 *            the view that was clicked
 	 */
 	public void help( MenuItem menu )
 	{
 		Intent intent = new Intent( this, HelpView.class );
 		startActivity( intent );
 	}
 
 }
