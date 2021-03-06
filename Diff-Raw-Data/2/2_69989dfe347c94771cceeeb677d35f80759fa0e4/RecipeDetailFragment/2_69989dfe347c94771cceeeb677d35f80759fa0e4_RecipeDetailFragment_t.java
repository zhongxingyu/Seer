 package com.ianhanniballake.recipebook.ui;
 
 import android.app.Activity;
 import android.content.ContentUris;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.BaseColumns;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.MenuCompat;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.ianhanniballake.recipebook.R;
 import com.ianhanniballake.recipebook.provider.RecipeContract;
 
 /**
  * Fragment which displays the details of a single recipe
  */
 public class RecipeDetailFragment extends Fragment implements
 		LoaderManager.LoaderCallbacks<Cursor>
 {
 	/**
 	 * Adapter to display the detailed data
 	 */
 	private SimpleCursorAdapter adapter;
 	/**
 	 * Listener that handles recipe edit events
 	 */
 	private OnRecipeEditListener recipeEditListener;
 
 	/**
 	 * Getter for the ID associated with the currently displayed recipe
 	 * 
 	 * @return ID for the currently displayed recipe
 	 */
 	public long getRecipeId()
 	{
 		if (getArguments() == null)
 			return 0;
 		return getArguments().getLong(BaseColumns._ID, 0);
 	}
 
 	@Override
 	public void onActivityCreated(final Bundle savedInstanceState)
 	{
 		super.onActivityCreated(savedInstanceState);
 		adapter = new SimpleCursorAdapter(getActivity(),
 				R.layout.fragment_recipe_detail, null, new String[] {
 						RecipeContract.Recipes.COLUMN_NAME_TITLE,
 						RecipeContract.Recipes.COLUMN_NAME_DESCRIPTION },
 				new int[] { R.id.title, R.id.description }, 0);
 		if (getRecipeId() != 0)
 			getLoaderManager().initLoader(0, null, this);
 	}
 
 	/**
 	 * Attaches to the parent activity, saving a reference to it to call back
 	 * recipe edit events
 	 * 
 	 * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
 	 */
 	@Override
 	public void onAttach(final Activity activity)
 	{
 		super.onAttach(activity);
 		try
 		{
 			recipeEditListener = (OnRecipeEditListener) activity;
 		} catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString()
 					+ " must implement OnRecipeEditListener");
 		}
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(final int id, final Bundle args)
 	{
 		final Uri recipeUri = ContentUris.withAppendedId(
 				RecipeContract.Recipes.CONTENT_ID_URI_PATTERN, getRecipeId());
 		return new CursorLoader(getActivity(), recipeUri, null, null, null,
 				null);
 	}
 
 	/**
 	 * Adds Edit option to the menu
 	 * 
 	 * @see android.support.v4.app.Fragment#onCreateOptionsMenu(android.view.Menu,
 	 *      android.view.MenuInflater)
 	 */
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		inflater.inflate(R.menu.fragment_recipe_detail, menu);
 		MenuCompat.setShowAsAction(menu.findItem(R.id.edit), 2);
 		MenuCompat.setShowAsAction(menu.findItem(R.id.delete), 2);
 	}
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater,
 			final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.fragment_recipe_detail, container,
 				false);
 	}
 
 	@Override
 	public void onLoaderReset(final Loader<Cursor> data)
 	{
 		adapter.swapCursor(null);
 	}
 
 	@Override
 	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data)
 	{
		if (!data.moveToFirst() || getView() == null)
 			return;
 		adapter.swapCursor(data);
 		adapter.bindView(getView(), getActivity(), data);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.edit:
 				recipeEditListener.onRecipeEditStarted(getRecipeId());
 				return true;
 			case R.id.delete:
 				recipeEditListener.onRecipeDeleted(getRecipeId());
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Hides the edit and delete items if this is not currently showing a valid
 	 * recipe
 	 * 
 	 * @see android.support.v4.app.Fragment#onPrepareOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public void onPrepareOptionsMenu(final Menu menu)
 	{
 		menu.findItem(R.id.edit).setVisible(getRecipeId() != 0);
 		menu.findItem(R.id.delete).setVisible(getRecipeId() != 0);
 	}
 }
