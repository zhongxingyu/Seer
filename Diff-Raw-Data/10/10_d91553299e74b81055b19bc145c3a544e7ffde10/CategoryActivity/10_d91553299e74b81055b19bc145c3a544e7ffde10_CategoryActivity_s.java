 package com.prologic.idkey.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.prologic.idkey.CustomProgressDailog;
 import com.prologic.idkey.R;
 import com.prologic.idkey.api.ApiConnection;
 import com.prologic.idkey.api.WebService;
 import com.prologic.idkey.api.command.CreateCategoryCommand;
 import com.prologic.idkey.api.command.GetAllCategoriesCommand;
 import com.prologic.idkey.api.command.UpdateCategoryCommand;
 import com.prologic.idkey.objects.Category;
 import com.prologic.idkey.objects.CategoryAdapter;
 import com.prologic.idkey.objects.CategoryAdapter.OnCategoryItemDeleteListener;
 
 public class CategoryActivity extends MainActivity implements OnItemClickListener,OnCategoryItemDeleteListener {
 
 	private ListView lvCategories;
 	private CategoryAdapter adapter;
 	private List<Category> listCategories;
 	private boolean isEditing;
 	private Button btnEditDone;
 	private Button btnAddNewCategory;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.category_screen);
 		btnEditDone = (Button) findViewById(R.id.btn_cate_done);
 		btnAddNewCategory = (Button) findViewById(R.id.btn_category_add_new);
 		listCategories = new ArrayList<Category>();	
 
 		lvCategories = (ListView) findViewById(R.id.lv_categories);
 
 		adapter = new CategoryAdapter(context, R.layout.category_row_view, listCategories);
 		adapter.setOnCategoryItemDeleteListener(this);
 		lvCategories.setOnItemClickListener(this);
 		lvCategories.setAdapter(adapter);
 
 		isEditing = false;
 		loadCategoryList();
 	}
 
 	private void loadCategoryList() 
 	{	
 		new CategoryListTask(context).execute();
 	}
 
 	private void updateCategoryList(List<Category> categories) 
 	{
 
 		if(categories != null)
 		{
 			listCategories.addAll(categories);
 
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	private void updateCategory(int listPos,String updateName,int categoryId)
 	{
 		if(updateName != null && updateName.length() > 0)
 		{
 			new UpdateCategoryTask(context,categoryId,updateName,listPos).execute();
 		}else {
 			Toast.makeText(context,"Name can not be blank",Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	private void addNewCategory(String name)
 	{
 		if(name != null && name.length() > 0)
 		{
 			new CreateCategoriesTask(context,name).execute();
 		}else {
 			Toast.makeText(context,"Name can not be blank",Toast.LENGTH_SHORT).show();
 		}
 
 	}
 
 	private void onListCategoryNewItemAdded(String name,int id,int count)
 	{
 		if(id > -1)
 		{
 			Category category = new Category(id, name, count);
 			
 			if(listCategories.size() >0)
 			{
 				listCategories.add(listCategories.size()-1,category);
 			}else {
 				listCategories.add(category);
 			}
 			adapter.notifyDataSetChanged();
 		}
 	}
 
 	private void onUpdateCategoryList(int listPos,String newName) 
 	{
 		Category category = listCategories.get(listPos);
 		if(category != null)
 		{
 			category.setName(newName);
 			adapter.notifyDataSetChanged();
 		}
 
 	}
 
 	private void onDeleteCategoryListItem(int listItemPosition)
 	{
 		listCategories.remove(listItemPosition);
 		adapter.notifyDataSetChanged();
 	}
 
 	public void onClickAddCategory(View v) 
 	{
 		final EditText input = new EditText(context);
 		input.setHint("name");
 		showCustomAlertDailog("Name?","Add","Cancel",input,new ICustomDailogClickListener() {
 
 			@Override
 			public void onOkClick(View customView) 
 			{
 				String name = (((EditText) customView).getText().toString());
 				addNewCategory(name);
 				Log.i(TAG, name);
 			}
 		});
 
 	}
 
 	public void onClickClose(View v)
 	{
 		finish();
 	}
 
 	public void onClickEditDone(View v)
 	{	
 		btnEditDone.setText(!isEditing?"Done":"Edit");
 		btnAddNewCategory.setVisibility(!isEditing?View.VISIBLE:View.GONE);
 		adapter.setShowDelete(!isEditing);
 		adapter.notifyDataSetChanged();	
 		isEditing = !isEditing;
 	}	
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int pos, long id)
 	{
 		final Category category = listCategories.get(pos);
 		if(isEditing && !category.getName().equalsIgnoreCase("Unassigned"))
 		{
 			final EditText input = new EditText(context);
 			input.setText(category.getName());
 			final int position = pos;
 			showCustomAlertDailog("Edit Category","Update","Cancel",input,new ICustomDailogClickListener() {
 
 				@Override
 				public void onOkClick(View customView) 
 				{
 					//Toast.makeText(context,(((EditText) customView).getText().toString()),Toast.LENGTH_SHORT).show();
 					String updatedName = (((EditText) customView).getText().toString());
 					updateCategory(position,updatedName,category.getId());
 				}
 			});
		}else {
 			Bundle b = new Bundle();
 
 			b.putInt(KeyListingActivities.USER_CATEGORY_ID, category.getId());
 			b.putString(KeyListingActivities.USER_CATEGORY_NAME, category.getName());
 
 			setCurrent(com.prologic.idkey.activities.KeyListingActivities.class, b);
 		}
 	}
 
 	@Override
 	public void onItemDelete(int position) 
 	{
 		Category category = listCategories.get(position);
 		if(category != null)
 		{
 			new DeleteCategoryTask(context, category.getId(), position).execute();
 		}
 
 	}
 
 	private class CategoryListTask extends AsyncTask<Void, Void, Void>
 	{
 		private Context context;
 		private GetAllCategoriesCommand allCategoriesCommand;
 		private CustomProgressDailog progressDialog;
 
 		public CategoryListTask(Context context) 
 		{
 			this.context = context;
 			progressDialog = new CustomProgressDailog(context);
 			progressDialog.setTitle("Loading");
 			progressDialog.setMessage("Please wait...");
 		}
 
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			progressDialog.show();
 
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) 
 		{
 			allCategoriesCommand = new GetAllCategoriesCommand();
 			allCategoriesCommand.execute(ApiConnection.getInstance(context));
 
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 
 			if(progressDialog != null && progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 
 			if(allCategoriesCommand != null)
 			{
 				List<Category> categories = allCategoriesCommand.getCategories();
 				updateCategoryList(categories);
 			}
 		}
 	}
 
 	private class UpdateCategoryTask extends AsyncTask<Void, Void, Void>
 	{
 		private Context context;
 		private int userCategoryId;
 		private String newName;
 		private int listPos;
 		private CustomProgressDailog progressDialog;
 		private UpdateCategoryCommand updateCategoryCommand;
 
 		public UpdateCategoryTask(Context context,int userCategoryId,String newName,int listPos) 
 		{
 			this.context = context;
 			this.userCategoryId = userCategoryId;
 			this.newName = newName;
 			this.listPos = listPos;
 			progressDialog = new CustomProgressDailog(context);
 			progressDialog.setTitle("Loading");
 			progressDialog.setMessage("Please wait...");
 		}
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			progressDialog.show();
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			updateCategoryCommand = new UpdateCategoryCommand(userCategoryId,newName);
 			updateCategoryCommand.execute(ApiConnection.getInstance(context));
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			if(progressDialog.isShowing())
 				progressDialog.dismiss();
 			if(updateCategoryCommand.isUpdatedSuccessfully())
 			{
 				onUpdateCategoryList(listPos,newName);
 
 			}else {
 				showOkAlertDailog(updateCategoryCommand.getMessage(), "Update Category", false);
 
 			}
 			updateCategoryCommand = null;
 
 		}
 
 	}
 
 	private class DeleteCategoryTask extends AsyncTask<Void, Void, Void>
 	{
 		private Context context;
 		private int userCategoryId;
 		private int listPos;
 		private CustomProgressDailog progressDialog;
 		private boolean isDeletedSuccessfully = false;
 
 		public DeleteCategoryTask(Context context,int userCategoryId,int listPos) 
 		{
 			this.context = context;
 			this.userCategoryId = userCategoryId;
 			this.listPos = listPos;
 
 			progressDialog = new CustomProgressDailog(context);
 			progressDialog.setTitle("Loading");
 			progressDialog.setMessage("Please wait...");
 
 		}
 		@Override
 		protected void onPreExecute() {
 			super.onPreExecute();
 			progressDialog.show();
 		}
 
 		@Override
 		protected Void doInBackground(Void... params) 
 		{			
 			isDeletedSuccessfully = WebService.getInstance().deleteCategory(userCategoryId);
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 
 			if(progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}	
 
 			if(isDeletedSuccessfully)
 			{
 				onDeleteCategoryListItem(listPos);
 			}else {
 				showOkAlertDailog("Error deleting category", "Delete", false);
 			}
 		}
 
 	}
 
 	private class CreateCategoriesTask extends AsyncTask<Void, Void, Void>
 	{
 		private String categoryName;
 		private Context context;
 		private CreateCategoryCommand createCategoryCommand;
 		private CustomProgressDailog progressDialog;
 
 		public CreateCategoriesTask(Context context,String categoryName) 
 		{
 			this.categoryName = categoryName;
 			progressDialog = new CustomProgressDailog(context);
 			progressDialog.setTitle("Loading");
 			progressDialog.setMessage("Please wait...");
 		}
 		@Override
 		protected void onPreExecute() 
 		{
 			super.onPreExecute();
 			progressDialog.show();
 		}
 		@Override
 		protected Void doInBackground(Void... params) {
 			createCategoryCommand = new CreateCategoryCommand(categoryName);
 			createCategoryCommand.execute(ApiConnection.getInstance(context));
 			return null;
 		}
 		@Override
 		protected void onPostExecute(Void result)
 		{
 			super.onPostExecute(result);
 
 			if(progressDialog.isShowing())
 			{
 				progressDialog.dismiss();
 			}
 			
 			if(createCategoryCommand.isCreatedSuccessfully())
 			{
 				onListCategoryNewItemAdded(categoryName,createCategoryCommand.getId(),0);
 			}
 			else
 			{
 				showOkAlertDailog(createCategoryCommand.getMessage(), "Add Category", false);
 			}
 
 			Log.i(TAG, createCategoryCommand.getMessage());
 			createCategoryCommand = null;
 		}
 
 	}
 
 
 
 }
