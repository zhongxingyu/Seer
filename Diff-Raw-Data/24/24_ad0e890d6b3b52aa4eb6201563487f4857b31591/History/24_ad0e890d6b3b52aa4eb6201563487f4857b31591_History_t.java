 package net.darchangel.shoppingTweeter;
 
 import java.util.List;
 
 import net.darchangel.shoppingTweeter.util.HistoryTableDAO;
 import net.darchangel.shoppingTweeter.util.ShoppingItem;
 import net.darchangel.shoppingTweeter.util.ShoppingTweeterDBHelper;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
 import android.os.Bundle;
 import android.util.TypedValue;
import android.view.Gravity;
 import android.view.View;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 
 public class History extends Activity {
 
 	private TableLayout historyTable;
 	private static HistoryTableDAO historyTableDAO;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.history);
 
 		historyTable = (TableLayout) findViewById(R.id.history_table);
 
 		ShoppingTweeterDBHelper dbHelper = new ShoppingTweeterDBHelper(getApplicationContext());
 		SQLiteDatabase db = dbHelper.getReadableDatabase();
 		historyTableDAO = new HistoryTableDAO(db);
 
 		List<ShoppingItem> historyList = historyTableDAO.selectAll();
 		createHistoryTable(historyTable, historyList);
 	}
 
 	/**
 	 * リストからテーブルを作成する
 	 * 
 	 * @param table
 	 *            テーブルレイアウト
 	 * @param list
 	 *            履歴リスト
 	 */
 	private void createHistoryTable(TableLayout table, List<ShoppingItem> list) {
 		int row_id = 0;
 		for (ShoppingItem shoppingItem : list) {
 			TableRow row = new TableRow(this);
 			row.setId(row_id++);
 
			TableRow.LayoutParams item_layout = new TableRow.LayoutParams();
			item_layout.weight = (float) 0.75;
			item_layout.width = 0;
			item_layout.setMargins(1, 1, 1, 1);

			TableRow.LayoutParams expense_layout = new TableRow.LayoutParams();
			expense_layout.weight = (float) 0.25;
			expense_layout.width = 0;
			expense_layout.setMargins(1, 1, 1, 1);

 			// 品目名を取得
 			TextView item = new TextView(this);
 			item.setText(shoppingItem.getItemName());
 			item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			item.setBackgroundColor(Color.BLACK);
			item.setLayoutParams(item_layout);
 
 			// 金額を取得
 			TextView expense = new TextView(this);
 			expense.setText(Integer.toString((shoppingItem.getExpense())));
 			expense.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
			expense.setGravity(Gravity.RIGHT);
			expense.setBackgroundColor(Color.BLACK);
			expense.setLayoutParams(expense_layout);
 
 			// 取得した値を行に追加
 			row.addView(item);
 			row.addView(expense);
 
 			row.setClickable(true);
 			setActionEachRow(row);
 
 			// 作成した行をテーブルに追加
 			table.addView(row);
 
 		}
 	}
 
 	private void setActionEachRow(View v) {
 		v.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				TableRow row = (TableRow) findViewById(v.getId());
 				TextView item = (TextView) row.getChildAt(0);
 				TextView expense = (TextView) row.getChildAt(1);
 
 				Intent intent = new Intent(History.this, (Class<?>) ShoppingTweeter.class);
 				intent.putExtra(HistoryTableDAO.COLUMNS[HistoryTableDAO.COLUMN_ITEM_NAME], item.getText());
 				intent.putExtra(HistoryTableDAO.COLUMNS[HistoryTableDAO.COLUMN_EXPENSE], expense.getText());
 				setResult(Activity.RESULT_OK, intent);
 
				// 履歴画面を終了
 				finish();
 			}
 		});
 	}
 }
