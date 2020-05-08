 package com.lincbio.lincxmap.android.app;
 
 import com.lincbio.lincxmap.R;
 import com.lincbio.lincxmap.android.Constants;
 import com.lincbio.lincxmap.android.database.DatabaseHelper;
 import com.lincbio.lincxmap.android.widget.GenericGroupedListAdapter;
 import com.lincbio.lincxmap.android.widget.GenericGroupedListAdapter.Groupable;
 import com.lincbio.lincxmap.pojo.Catalog;
 import com.lincbio.lincxmap.pojo.Product;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.EditText;
 import android.widget.ExpandableListAdapter;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 import android.widget.ExpandableListView.OnChildClickListener;
 
 /**
  * Product List(classified by product catalogue) Activity
  * 
  * @author Johnson Lee
  * 
  */
 public class ProductListActivity extends Activity implements Constants {
 	private final DatabaseHelper dbHelper = new DatabaseHelper(this);
 	private final MenuManager menuManager = new MenuManager(this) {
 
 		@Override
 		public void onMenuItemSelected(MenuItem item) {
 			super.onMenuItemSelected(item);
 
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
					.getMenuInfo();
			int i = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int j = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			Catalog catalog = (Catalog) productAdapter.getGroup(i);
			Product product = (Product) productAdapter.getChild(i, j);
 
 			switch (item.getItemId()) {
 			case R.id.menu_add_product:
 				onAddProductPerformed(catalog);
 				break;
 			case R.id.menu_del_catalog:
 				createDeleteCatalogDialog(catalog).show();
 				break;
 			case R.id.menu_mod_product:
				Intent intent = new Intent(ProductListActivity.this,
 						ProductActivity.class);
 				intent.putExtra(PARAM_CATALOG_OBJECT, catalog);
 				intent.putExtra(PARAM_PRODUCT_OBJECT, product);
 				startActivity(intent);
 				break;
 			case R.id.menu_del_product:
 				createDeleteProductDialog(product).show();
 				break;
 			}
 		}
 
 	};
 
 	private EditText txtSearch;
 	private ExpandableListView productView;
 	private GenericGroupedListAdapter<Product> productAdapter;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.setContentView(R.layout.product);
 
 		this.productAdapter = new GenericGroupedListAdapter<Product>(this,
 				new ProductGroup(), R.layout.catalog_item,
 				R.layout.product_item);
 		this.txtSearch = (EditText) findViewById(R.id.product_search);
 		this.productView = (ExpandableListView) findViewById(R.id.product_list);
 		this.productView.setAdapter((ExpandableListAdapter) productAdapter);
 		this.productView.setOnChildClickListener(new OnChildClickListener() {
 
 			@Override
 			public boolean onChildClick(ExpandableListView parent, View v,
 					int groupPosition, int childPosition, long id) {
 
 				Product product = (Product) productAdapter.getChild(
 						groupPosition, childPosition);
 				Catalog catalog = (Catalog) productAdapter
 						.getGroup(groupPosition);
 				Intent intent = new Intent(ProductListActivity.this,
 						ProductActivity.class);
 				intent.putExtra(PARAM_CATALOG_OBJECT, catalog);
 				intent.putExtra(PARAM_PRODUCT_OBJECT, product);
 				startActivity(intent);
 
 				return true;
 			}
 
 		});
 		this.registerForContextMenu(this.productView);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) menuInfo;
 		int i = ExpandableListView.getPackedPositionGroup(info.packedPosition);
 		int j = ExpandableListView.getPackedPositionChild(info.packedPosition);
 
 		switch (ExpandableListView.getPackedPositionType(info.packedPosition)) {
 		case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
 			Catalog catalog = (Catalog) this.productAdapter.getGroup(i);
 			menu.setHeaderTitle(catalog.getName());
 			this.menuManager.createMenu(menu, R.menu.ctx_catalog);
 			break;
 		case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
 			Product product = (Product) this.productAdapter.getChild(i, j);
 			menu.setHeaderTitle(product.getName());
 			this.menuManager.createMenu(menu, R.menu.ctx_product);
 			break;
 		}
 
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		this.menuManager.onMenuItemSelected(item);
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	protected void onStart() {
 		super.onStart();
 		this.productAdapter.reset(this.dbHelper.getProducts());
 	}
 
 	public void onButtonAddClick(View view) {
 		this.onAddProductPerformed(null);
 	}
 
 	public void onButtonSearchClick(View view) {
 		String text = this.txtSearch.getText().toString();
 
 		if (text.trim().length() <= 0) {
 			this.productAdapter.reset(this.dbHelper.getProducts());
 		} else {
 			this.productAdapter.reset(this.dbHelper.getProducts(text));
 		}
 	}
 
 	private void onAddProductPerformed(final Catalog catalog) {
 		Intent intent = new Intent(this, ProductActivity.class);
 		intent.putExtra(PARAM_CATALOG_OBJECT, catalog);
 		startActivity(intent);
 	}
 
 	private AlertDialog.Builder createDeleteProductDialog(final Product product) {
 		OnClickListener ok = new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dbHelper.deleteProduct(product);
 				onButtonSearchClick(null);
 			}
 
 		};
 		return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(android.R.string.dialog_alert_title)
 				.setMessage(R.string.msg_confirm_delete)
 				.setPositiveButton(android.R.string.ok, ok)
 				.setNegativeButton(android.R.string.cancel, MenuManager.CANCEL);
 	}
 
 	private AlertDialog.Builder createDeleteCatalogDialog(final Catalog catalog) {
 		OnClickListener ok = new OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dbHelper.deleteCatalog(catalog);
 				onButtonSearchClick(null);
 			}
 
 		};
 		return new AlertDialog.Builder(this)
 				.setIcon(android.R.drawable.ic_dialog_alert)
 				.setTitle(android.R.string.dialog_alert_title)
 				.setMessage(R.string.msg_confirm_delete)
 				.setPositiveButton(android.R.string.ok, ok)
 				.setNegativeButton(android.R.string.cancel, MenuManager.CANCEL);
 	}
 
 	private class ProductGroup implements Groupable<Product> {
 
 		@Override
 		public Object getGroupId(Product e) {
 			return dbHelper.getCatalogue(e.getCatalogId());
 		}
 
 	}
 
 }
