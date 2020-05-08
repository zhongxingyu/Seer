 package il.ac.huji.todolist;
 
 import java.util.Date;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.app.Activity;
 import android.content.Intent;
 
 public class AddNewTodoItemActivity extends Activity {
 		
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_new_item);
 		
 	}
 
 	
 	public void okClickHandler(View view)
 	{
 		Intent result = new Intent();
 		EditText newTodo = (EditText)findViewById(R.id.edtNewItem);
 		result.putExtra(TodoListManagerActivity.ADD_ITEM_RESULT_STRING, newTodo.getText().toString());
 		DatePicker dp = (DatePicker)findViewById(R.id.datePicker);
		@SuppressWarnings("deprecation")
 		Date chosenDate = new Date(dp.getYear() - 1900, dp.getMonth(), dp.getDayOfMonth());
 		result.putExtra(TodoListManagerActivity.ADD_ITEM_RESULT_DATE, chosenDate.getTime());
 		setResult(RESULT_OK,result);
 		finish();
 	}
 	
 	public void cancelClickHandler(View view)
 	{
 		Intent result = new Intent();
 		setResult(RESULT_CANCELED,result);
 		finish();
 	}
 	
 
 }
