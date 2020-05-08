 package ch.unibe.ese.shopnote.activities;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.ActionMode;
 import android.view.ActionMode.Callback;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.R;
 
 /**
  * 	Creates the Action Bar for Recipes to edit or remove them
  */
 public class RecipeListActionMode implements Callback {
 
 	private ListManager manager;
	private Activity activity;
 	private Recipe selectedRecipe;
 	private ArrayAdapter<Recipe> recipeAdapter;
 	private ArrayAdapter<Item> itemAdapter;
 	private Item selectedItem;
 
 	
	public RecipeListActionMode(ListManager manager, Recipe selectedRecipe, ArrayAdapter<Recipe> recipeAdapter, Activity homeActivity) {
 		this.manager = manager;
 		this.selectedRecipe = selectedRecipe;
 		this.activity = homeActivity;
 		this.recipeAdapter = recipeAdapter;
 		this.itemAdapter = null;
 		this.selectedItem = null;
 	}
 	
	public RecipeListActionMode(ListManager manager, Recipe selectedRecipe, Item selectedItem, ArrayAdapter<Item> itemAdapter, Activity homeActivity) {
 		this.manager = manager;
 		this.selectedRecipe = selectedRecipe;
 		this.activity = homeActivity;
 		this.recipeAdapter = null;
 		this.itemAdapter = itemAdapter;
 		this.selectedItem = selectedItem;
 	}
 	
     /** 
      *	Called when the action mode is created; startActionMode() was called
      */
     @Override
     public boolean onCreateActionMode(ActionMode mode, Menu menu) {
         // Inflate a menu resource providing context menu items
         MenuInflater inflater = mode.getMenuInflater();
         inflater.inflate(R.menu.actionbar_longpress_recipelist, menu);
         return true;
     }
 
     /**
      *	Called each time the action mode is shown. Always called after onCreateActionMode, but
      *	may be called multiple times if the mode is invalidated. 
      */
     @Override
     public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
         return false; // Return false if nothing is done
     }
 
     /** 
      *	Called when the user selects a contextual menu item
      */
     @Override
     public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
     	 switch (item.getItemId()) {
             case R.id.action_edit:
             	mode.finish(); 
             	
             	if(recipeAdapter != null) {
 		        	Intent editRecipeIntent = new Intent(activity, CreateRecipeActivity.class);
 		        	editRecipeIntent.putExtra(BaseActivity.EXTRAS_RECIPE_ID, selectedRecipe.getId());
 		            activity.startActivityForResult(editRecipeIntent, 1);
             	} else {
             		Intent editItemIntent = new Intent(activity, CreateItemActivity.class);
             		editItemIntent.putExtra(BaseActivity.EXTRAS_RECIPE_ID, selectedRecipe.getId());
             		editItemIntent.putExtra(BaseActivity.EXTRAS_ITEM_ID, selectedItem.getId());
             		editItemIntent.putExtra(BaseActivity.EXTRAS_ITEM_EDIT, true);
 		            activity.startActivityForResult(editItemIntent, 1);	
             	}
                 return true;
                 
             case R.id.action_remove:
             	mode.finish();
             	
             	if(recipeAdapter != null) {
 	            	manager.removeRecipe(selectedRecipe);
 	            	recipeAdapter.notifyDataSetChanged();
             	} else {
             		selectedRecipe.removeItem(selectedItem);
             		itemAdapter.remove(selectedItem);
                 	manager.saveRecipe(selectedRecipe);
             	}
 	            	
             	return true;
            
             default:
                 return false;
         }
     }
 
 	@Override
 	public void onDestroyActionMode(ActionMode mode) {
 		// nothing to do
 	}
 }
