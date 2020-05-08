 package ru.redsolution.bst.ui;
 
 import ru.redsolution.bst.R;
 import ru.redsolution.bst.data.BST;
 import ru.redsolution.bst.data.table.BaseDatabaseException;
 import ru.redsolution.bst.data.table.BaseGoodTable;
 import ru.redsolution.bst.data.table.CustomGoodTable;
 import ru.redsolution.bst.data.table.GoodFolderTable;
 import ru.redsolution.bst.data.table.GoodTable;
 import ru.redsolution.bst.data.table.NamedTable;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.MergeCursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.FilterQueryProvider;
 import android.widget.ResourceCursorAdapter;
 import android.widget.TextView;
 
 public class ChooseActivity extends ListActivity implements
 		OnItemClickListener, OnClickListener {
 
 	private static final String SAVED_FOLDER = "ru.redsolution.bst.ui.ChooseActivity.SAVED_FOLDER";
 
 	/**
 	 * Поле результата, содержащее ID выбранного продукта.
 	 */
 	public static final String EXTRA_PRODUCT_ID = "ru.redsolution.bst.ui.ChooseActivity.PRODUCT_ID";
 
 	/**
 	 * Поле результата, содержащее истину, если был создан новый ТМЦ.
 	 */
 	public static final String EXTRA_IS_CUSTOM = "ru.redsolution.bst.ui.ChooseActivity.IS_CUSTOM";
 
 	private static final int OPTION_MENU_SHOW_FOLDERS_ID = 1;
 	private static final int OPTION_MENU_HIDE_FOLDERS_ID = 2;
 
 	private Button createButton;
 	private EditText searchView;
 
 	/**
 	 * Тип данных.
 	 * 
 	 * @author alexander.ivanov
 	 * 
 	 */
 	private static enum Type {
 
 		good,
 
 		custom,
 
 		folder;
 
 		/**
 		 * @param cursor
 		 * @return Тип данных, содержащихся в текущей строке курсора.
 		 */
 		public static Type getType(Cursor cursor) {
 			String tableName = cursor.getString(cursor
 					.getColumnIndex(NamedTable.Fields.TABLE_NAME));
 			if (tableName.equals(GoodTable.getInstance().getTableName())) {
 				return good;
 			} else if (tableName.equals(CustomGoodTable.getInstance()
 					.getTableName())) {
 				return custom;
 			} else if (tableName.equals(GoodFolderTable.getInstance()
 					.getTableName())) {
 				return folder;
 			} else
 				throw new IllegalStateException();
 		}
 
 	}
 
 	/**
 	 * Группа товаров.
 	 */
 	private String folder;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.choose);
 		if (savedInstanceState != null) {
 			folder = savedInstanceState.getString(SAVED_FOLDER);
 		} else {
 			folder = "";
 		}
 		final CursorAdapter adapter = new ResourceCursorAdapter(this,
 				android.R.layout.simple_list_item_2, createCursor("")) {
 
 			@Override
 			public void bindView(View view, Context context, Cursor cursor) {
 				String name = cursor.getString(cursor
 						.getColumnIndex(NamedTable.Fields.NAME));
 				((TextView) view.findViewById(android.R.id.text1))
 						.setText(name);
 				TextView summaryView = (TextView) view
 						.findViewById(android.R.id.text2);
 				switch (Type.getType(cursor)) {
 				case good:
 					summaryView.setText(GoodTable.getInstance()
 							.getValues(cursor)
 							.getAsString(GoodTable.Fields.PRODUCT_CODE));
 					break;
 				case custom:
 					summaryView.setText(R.string.custom_good);
 					break;
 				case folder:
 					summaryView.setText(R.string.folder);
 					break;
 				}
 			}
 
 		};
 		adapter.setFilterQueryProvider(new FilterQueryProvider() {
 
 			@Override
 			public Cursor runQuery(CharSequence constraint) {
 				Cursor cursor;
 				cursor = createCursor(constraint);
 				startManagingCursor(cursor);
 				return cursor;
 			}
 
 		});
 		setListAdapter(adapter);
 		searchView = (EditText) findViewById(R.id.search);
 		searchView.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				adapter.getFilter().filter(s);
 				updateButton();
 			}
 
 		});
 		createButton = (Button) findViewById(R.id.create);
 		createButton.setOnClickListener(this);
 		getListView().setOnItemClickListener(this);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		updateButton();
 		updateCursor();
 	}
 
 	/**
 	 * @param constraint
 	 * @return Новый курсор.
 	 */
 	private Cursor createCursor(CharSequence constraint) {
		if (constraint.length() == 0) {
			if (BST.getInstance().isShowFolders()) {
 				if ("".equals(folder))
 					return new MergeCursor(new Cursor[] {
 							GoodFolderTable.getInstance().list(folder),
 							CustomGoodTable.getInstance().list(),
 							GoodTable.getInstance().list(folder) });
 				else
 					return new MergeCursor(new Cursor[] {
 							GoodFolderTable.getInstance().list(folder),
 							GoodTable.getInstance().list(folder) });
 			} else {
 				return new MergeCursor(new Cursor[] {
						CustomGoodTable.getInstance().list(),
						GoodTable.getInstance().list() });
 			}
 		} else {
			return new MergeCursor(
					new Cursor[] {
							CustomGoodTable.getInstance().filterByText(
									constraint.toString()),
							GoodTable.getInstance().filterByText(
									constraint.toString()) });
 		}
 	}
 
 	/**
 	 * Обновляет данные списка.
 	 */
 	private void updateCursor() {
 		((CursorAdapter) getListAdapter()).changeCursor(createCursor(""));
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString(SAVED_FOLDER, folder);
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		menu.clear();
 		if (BST.getInstance().isShowFolders())
 			menu.add(0, OPTION_MENU_HIDE_FOLDERS_ID, 0,
 					getText(R.string.hide_folders));
 		else
 			menu.add(0, OPTION_MENU_SHOW_FOLDERS_ID, 0,
 					getText(R.string.show_folders));
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case OPTION_MENU_SHOW_FOLDERS_ID:
 			BST.getInstance().setShowFolders(true);
 			updateCursor();
 			return true;
 		case OPTION_MENU_HIDE_FOLDERS_ID:
 			BST.getInstance().setShowFolders(false);
 			updateCursor();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View view, int position,
 			long id) {
 		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
 		switch (Type.getType(cursor)) {
 		case good:
 		case custom:
 			Intent intent = new Intent();
 			String productId = cursor.getString(cursor
 					.getColumnIndex(BaseGoodTable.Fields._ID));
 			intent.putExtra(EXTRA_PRODUCT_ID, productId);
 			intent.putExtra(EXTRA_IS_CUSTOM,
 					Type.getType(cursor) == Type.custom);
 			setResult(RESULT_OK, intent);
 			finish();
 			break;
 
 		case folder:
 			this.folder = GoodFolderTable.getInstance().getValues(cursor)
 					.getAsString(GoodFolderTable.Fields._ID);
 			updateCursor();
 			break;
 		}
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			if (!BST.getInstance().isShowFolders() || "".equals(folder))
 				break;
 			try {
 				folder = GoodFolderTable.getInstance().getById(folder)
 						.getAsString(GoodFolderTable.Fields.PARENT);
 			} catch (BaseDatabaseException e) {
 				break;
 			}
 			updateCursor();
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 	private void updateButton() {
 		String name = searchView.getText().toString();
 		createButton.setEnabled(!"".equals(name));
 		createButton.setText(getString(R.string.create_good, name));
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.create:
 			Intent intent = new Intent();
 			long id = CustomGoodTable.getInstance().add(
 					searchView.getText().toString());
 			intent.putExtra(EXTRA_PRODUCT_ID, String.valueOf(id));
 			intent.putExtra(EXTRA_IS_CUSTOM, true);
 			setResult(RESULT_OK, intent);
 			finish();
 			break;
 		}
 	}
 
 }
