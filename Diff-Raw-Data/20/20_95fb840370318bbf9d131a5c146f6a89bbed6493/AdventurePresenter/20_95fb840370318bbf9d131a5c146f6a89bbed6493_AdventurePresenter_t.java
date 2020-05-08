 
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
  */package cmput301f13t10.presenter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cmput301f13t10.model.AdventureCache;
 import cmput301f13t10.model.AdventureModel;
 import cmput301f13t10.model.Callback;
 import cmput301f13t10.model.DatabaseInteractor;
 import cmput301f13t10.model.ESGetCommand;
 import cmput301f13t10.model.SectionModel;
 import cmput301f13t10.view.AdventureEditView;
 
 /**
  * A Presenter that relays data between the adventure model and the adventure
  * view. This class creates new adventure models or loads them from the cache.
  * 
  * @author Braeden Soetaert
  * 
  */
 public class AdventurePresenter
 {
 	/**
 	 * The view that created this presenter.
 	 */
 	private AdventureEditView mView;
 
 	/**
 	 * The adventure model that this presenter retrieves data from.
 	 */
 	private AdventureModel mModel;
 
 	/**
 	 * The cache of all adventures to get the adventure model from.
 	 */
 	private AdventureCache mCache;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param view
 	 *            The view that creates this presenter.
 	 */
 	public AdventurePresenter( AdventureEditView view )
 	{
 		mView = view;
 		mCache = AdventureCache.getAdventureCache();
 		mModel = new AdventureModel();
 		mCache.addAdventure( mModel );
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param view
 	 *            The view that creates this presenter.
 	 * @param id
 	 *            The id of the adventure model to load from the cache.
 	 */
 	public AdventurePresenter( AdventureEditView view, Integer id )
 	{
 		mView = view;
 		mCache = AdventureCache.getAdventureCache();
 		mModel = mCache.getAdventureById( id );
 	}
 
 	/**
 	 * Returns the adventure id of the currently loaded adventure model.
 	 * 
 	 * @return The id of the currently loaded adventure model.
 	 */
 	public Integer getAdventureId()
 	{
 		return mModel.getLocalId();
 	}
 
 	/**
 	 * Returns the title of the currently loaded adventure model.
 	 * 
 	 * @return The title of the currently loaded adventure model.
 	 */
 	public String getAdventureTitle()
 	{
 		return mModel.getTitle();
 	}
 
 	/**
 	 * Sets the title for the currently loaded adventure title.
 	 * 
 	 * @param title
 	 *            The title to set for the adventure.
 	 */
 	public void setAdventureTitle( String title )
 	{
 		mModel.setTitle( title );
 		// TODO Should save adventure after title change
 	}
 
 	/**
 	 * Delete the section with the corresponding section id from the adventure
 	 * model.
 	 * 
 	 * @param sectionId
 	 *            The id of the section to delete.
 	 */
 	public void deleteSection( Integer sectionId )
 	{
 		mModel.deleteSection( sectionId );
 		// TODO Should save adventure after section deleted.
 	}
 
 	/**
 	 * Gets the section title, id and whether or not it is the start section for
 	 * every section in the current adventure.
 	 * 
 	 * @return A list of objects containing the section titles and ids.
 	 */
 	public List<SectionTitle> getSectionTitles()
 	{
 		SectionModel aSection;
 		Integer startSectionId = mModel.getStartSection().getId();
 		ArrayList<SectionModel> sections = mModel.getSections();
 		List<SectionTitle> sectionTitles = new ArrayList<SectionTitle>();
 
 		for( int i = 0; i < sections.size(); i++ )
 		{
 			aSection = sections.get( i );
 			if( aSection.getId() == startSectionId )
 			{
 				sectionTitles.add( new SectionTitle( aSection.getName(), aSection.getId(), true ) );
 			}
 			else
 			{
 				sectionTitles.add( new SectionTitle( aSection.getName(), aSection.getId() ) );
 			}
 
 		}
 		return sectionTitles;
 	}
 
 	public void commitAdventure()
 	{
 		DatabaseInteractor.getDatabaseInteractor().addAdventure( mModel );
 	}
 
 	public void saveLocalAdventure()
 	{
 		mModel.setSave( true );
 	}
 
 	public void getOnlineAdventure( Callback externalCallback )
 	{
 		Callback presenterCallback = new Callback( externalCallback )
 		{
 
 			@Override
 			public void callBack( Object arg )
 			{
 				Callback c = (Callback) mCallbackArg;
 				AdventureCache.getAdventureCache().deleteAdventure( mModel );
 				mModel = (AdventureModel) arg;
 				AdventureCache.getAdventureCache().addAdventure( mModel );
 				if( c != null )
 					c.callBack( null );
 			}
 
 		};
 
 		ESGetCommand getCommand = new ESGetCommand( mModel.getRemoteId(), presenterCallback );
 		getCommand.execute( null, null );
 
 	}

	public boolean isAdventureOnline()
	{
		return mModel.getRemoteId() != -1;
	}
 }
